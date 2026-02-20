package youglish.anki.demo.domain.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import youglish.anki.demo.application.dto.NextVideoResponse
import youglish.anki.demo.application.dto.PreloadVideoInfo
import youglish.anki.demo.application.dto.UserStatsResponse
import youglish.anki.demo.domain.entity.Phrase
import youglish.anki.demo.domain.entity.User
import youglish.anki.demo.domain.entity.VideoHistory
import youglish.anki.demo.domain.repository.PhraseRepository
import youglish.anki.demo.domain.repository.VideoHistoryRepository
import youglish.anki.demo.infrastructure.client.YouglishApiClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * Core service for TikTok-style video feed with anti-repetition logic
 */
@Service
@Transactional
class VideoFeedService(
    private val phraseRepository: PhraseRepository,
    private val videoHistoryRepository: VideoHistoryRepository,
    private val youglishApiClient: YouglishApiClient
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /**
     * Get next video with smart anti-repetition algorithm:
     * 1. Never repeat same phrase consecutively
     * 2. Never repeat same video consecutively
     * 3. Prefer phrases not used recently
     * 4. Random selection from valid candidates
     */
    fun getNextVideo(user: User): NextVideoResponse {
        logger.info("Getting next video for user {}", user.username)

        // Get recent history to avoid repetition
        val recentHistory = videoHistoryRepository.findRecentByUser(user, PageRequest.of(0, 10))
        val recentPhraseIds = recentHistory.take(2).map { it.phrase.id!! }.toSet()
        val recentVideoIds = recentHistory.take(3).map { it.videoId }.toSet()

        // Get candidate phrases (active and not used in last 2 videos)
        val allActivePhrases = phraseRepository.findByUserAndIsActiveTrue(user)

        if (allActivePhrases.isEmpty()) {
            throw IllegalStateException("No active phrases found. Please add some phrases first.")
        }

        // Filter out recently used phrases
        val candidatePhrases = allActivePhrases.filter { it.id !in recentPhraseIds }

        // If all phrases were used recently, use all (means user has very few phrases)
        val finalCandidates = if (candidatePhrases.isEmpty()) allActivePhrases else candidatePhrases

        // Try to get a video from a random phrase
        var attempts = 0
        val maxAttempts = finalCandidates.size * 3

        while (attempts < maxAttempts) {
            attempts++

            // Select random phrase
            val phrase = finalCandidates[Random.nextInt(finalCandidates.size)]

            // Get videos to exclude for this phrase
            val excludeForPhrase = videoHistoryRepository.findRecentVideoIdsByUserAndPhrase(
                user, phrase, PageRequest.of(0, 5)
            ).toSet()

            val allExcluded = excludeForPhrase + recentVideoIds

            // Fetch video from YouGlish
            val videoInfo = youglishApiClient.fetchRandomVideo(
                text = phrase.text,
                language = phrase.language,
                accent = phrase.accent,
                excludeVideoIds = allExcluded.toList()
            )

            if (videoInfo != null) {
                // Save to history
                val history = VideoHistory(
                    user = user,
                    phrase = phrase,
                    videoId = videoInfo.videoId,
                    trackNumber = videoInfo.trackNumber,
                    captionText = videoInfo.captionText
                )
                videoHistoryRepository.save(history)

                // Update phrase last used time
                phrase.lastUsedAt = LocalDateTime.now()
                phraseRepository.save(phrase)

                // Get preload info for next video
                val preloadInfo = getPreloadInfo(user, phrase.id!!, videoInfo.videoId)

                logger.info("Selected video {} for phrase '{}' (track {})",
                    videoInfo.videoId, phrase.text, videoInfo.trackNumber)

                return NextVideoResponse(
                    videoId = videoInfo.videoId,
                    trackNumber = videoInfo.trackNumber,
                    phrase = toPhraseResponse(phrase),
                    captionText = videoInfo.captionText,
                    language = phrase.language,
                    accent = phrase.accent,
                    totalResults = videoInfo.totalResults,
                    preloadNext = preloadInfo
                )
            }
        }

        throw IllegalStateException("Could not find a suitable video after $maxAttempts attempts")
    }

    /**
     * Get information for preloading next video
     */
    private fun getPreloadInfo(user: User, currentPhraseId: Long, currentVideoId: String): PreloadVideoInfo? {
        val activePhrases = phraseRepository.findByUserAndIsActiveTrue(user)
            .filter { it.id != currentPhraseId }

        if (activePhrases.isEmpty()) {
            return null
        }

        val nextPhrase = activePhrases[Random.nextInt(activePhrases.size)]

        return PreloadVideoInfo(
            phraseId = nextPhrase.id!!,
            phraseText = nextPhrase.text,
            estimatedTrackNumber = Random.nextInt(1, nextPhrase.totalVideosAvailable.coerceAtLeast(1) + 1)
        )
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    fun getUserStats(user: User): UserStatsResponse {
        val allPhrases = phraseRepository.findAll().filter { it.user.id == user.id }
        val activePhrases = allPhrases.count { it.isActive }
        val totalVideosWatched = videoHistoryRepository.countByUser(user)

        val today = LocalDateTime.now().toLocalDate()
        val todayStart = today.atStartOfDay()
        val phrasesUsedToday = videoHistoryRepository.findRecentByUser(user, PageRequest.of(0, 1000))
            .filter { it.createdAt >= todayStart }
            .map { it.phrase.id }
            .toSet()
            .size

        return UserStatsResponse(
            totalPhrases = allPhrases.size.toLong(),
            activePhrases = activePhrases.toLong(),
            totalVideosWatched = totalVideosWatched,
            phrasesUsedToday = phrasesUsedToday
        )
    }

    /**
     * Clean old history to prevent database bloat
     * Keeps last 100 entries per user
     */
    fun cleanOldHistory(user: User) {
        val keepCount = 100
        val recentHistory = videoHistoryRepository.findRecentByUser(user, PageRequest.of(0, keepCount))

        if (recentHistory.size >= keepCount) {
            val cutoffDate = recentHistory.last().createdAt
            videoHistoryRepository.deleteOldHistory(user, cutoffDate)
            logger.info("Cleaned old history for user {}", user.username)
        }
    }

    private fun toPhraseResponse(phrase: Phrase) = youglish.anki.demo.application.dto.PhraseResponse(
        id = phrase.id!!,
        text = phrase.text,
        language = phrase.language,
        accent = phrase.accent,
        totalVideosAvailable = phrase.totalVideosAvailable,
        isActive = phrase.isActive,
        createdAt = phrase.createdAt.format(dateFormatter),
        lastUsedAt = phrase.lastUsedAt?.format(dateFormatter)
    )
}

