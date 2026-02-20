package youglish.anki.demo.infrastructure.client

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import youglish.anki.demo.application.dto.YouglishVideoInfo
import kotlin.random.Random

/**
 * YouGlish API Client (Simulated)
 *
 * NOTE: YouGlish API is client-side JavaScript only (widget.js).
 * This implementation simulates the backend validation and video fetching
 * that would normally happen in the browser.
 *
 * For production: Consider implementing a headless browser approach or
 * working with YouGlish team for server-side API access.
 */
@Component
class YouglishApiClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Validates if a phrase has videos available in YouGlish
     * In production, this would make an actual API call or headless browser check
     */
    fun validatePhraseHasVideos(text: String, language: String, accent: String?): ValidationResult {
        logger.info("Validating phrase '{}' for language '{}' and accent '{}'", text, language, accent)

        // Simulation: Most English phrases with 2+ words or common words have videos
        val hasVideos = when {
            text.isBlank() -> false
            text.length < 2 -> false
            language.equals("english", ignoreCase = true) && text.split(" ").size >= 2 -> true
            isCommonWord(text) -> true
            else -> Random.nextDouble() > 0.3 // 70% chance for other phrases
        }

        val totalResults = if (hasVideos) {
            Random.nextInt(10, 500) // Simulated result count
        } else {
            0
        }

        return ValidationResult(hasVideos, totalResults)
    }

    /**
     * Fetches a random video for a given phrase
     * In production, this would interact with YouGlish Widget API
     */
    fun fetchRandomVideo(
        text: String,
        language: String,
        accent: String?,
        excludeVideoIds: List<String> = emptyList()
    ): YouglishVideoInfo? {
        logger.info("Fetching random video for phrase '{}', excluding: {}", text, excludeVideoIds)

        val validation = validatePhraseHasVideos(text, language, accent)

        if (!validation.hasVideos) {
            return null
        }

        // Simulate video selection
        var videoId: String
        var attempts = 0
        do {
            videoId = generateSimulatedVideoId(text)
            attempts++
        } while (excludeVideoIds.contains(videoId) && attempts < 10)

        val trackNumber = Random.nextInt(1, validation.totalResults + 1)
        val captionText = generateSimulatedCaption(text)

        return YouglishVideoInfo(
            videoId = videoId,
            trackNumber = trackNumber,
            captionText = captionText,
            totalResults = validation.totalResults
        )
    }

    /**
     * Checks if a specific video track exists
     */
    fun isVideoAvailable(videoId: String): Boolean {
        // In production, verify video exists on YouTube
        return videoId.isNotBlank() && videoId.length == 11
    }

    private fun isCommonWord(text: String): Boolean {
        val commonWords = setOf(
            "hello", "world", "love", "time", "people", "work", "home", "great",
            "good", "important", "understand", "believe", "think", "know", "want",
            "need", "make", "take", "get", "give", "come", "go", "see", "use"
        )
        return commonWords.contains(text.lowercase())
    }

    private fun generateSimulatedVideoId(text: String): String {
        // Generate a realistic-looking YouTube video ID (11 characters)
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
        val hash = text.hashCode().toLong()
        val random = Random(hash)
        return (1..11).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    private fun generateSimulatedCaption(text: String): String {
        val templates = listOf(
            "I think $text is very important.",
            "When you consider $text, you realize...",
            "The concept of $text has been around for years.",
            "We need to talk about $text today.",
            "Have you ever thought about $text?",
            "$text is something we all experience.",
            "Understanding $text can help you grow."
        )
        return templates[Random.nextInt(templates.size)]
    }

    data class ValidationResult(
        val hasVideos: Boolean,
        val totalResults: Int
    )
}

