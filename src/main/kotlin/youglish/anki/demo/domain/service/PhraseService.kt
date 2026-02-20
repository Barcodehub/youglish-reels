package youglish.anki.demo.domain.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import youglish.anki.demo.application.dto.CreatePhraseRequest
import youglish.anki.demo.application.dto.PhraseResponse
import youglish.anki.demo.application.dto.UpdatePhraseRequest
import youglish.anki.demo.domain.entity.Phrase
import youglish.anki.demo.domain.entity.User
import youglish.anki.demo.domain.repository.PhraseRepository
import youglish.anki.demo.infrastructure.client.YouglishApiClient
import java.time.format.DateTimeFormatter

@Service
@Transactional
class PhraseService(
    private val phraseRepository: PhraseRepository,
    private val youglishApiClient: YouglishApiClient
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun createPhrase(user: User, request: CreatePhraseRequest): PhraseResponse {
        logger.info("Creating phrase '{}' for user {}", request.text, user.username)

        // Validate phrase doesn't already exist for this user
        if (phraseRepository.existsByUserAndText(user, request.text)) {
            throw IllegalArgumentException("Phrase already exists")
        }

        // Validate with YouGlish API
        val validation = youglishApiClient.validatePhraseHasVideos(
            request.text,
            request.language,
            request.accent
        )

        if (!validation.hasVideos) {
            throw IllegalArgumentException("No videos found for this phrase on YouGlish")
        }

        val phrase = Phrase(
            user = user,
            text = request.text,
            language = request.language,
            accent = request.accent,
            totalVideosAvailable = validation.totalResults
        )

        val saved = phraseRepository.save(phrase)
        logger.info("Phrase created successfully: ID={}", saved.id)

        return toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun getUserPhrases(user: User): List<PhraseResponse> {
        return phraseRepository.findByUserAndIsActiveTrue(user)
            .map { toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getPhraseById(user: User, phraseId: Long): PhraseResponse {
        val phrase = phraseRepository.findById(phraseId)
            .orElseThrow { IllegalArgumentException("Phrase not found") }

        if (phrase.user.id != user.id) {
            throw IllegalArgumentException("Phrase does not belong to user")
        }

        return toResponse(phrase)
    }

    fun updatePhrase(user: User, phraseId: Long, request: UpdatePhraseRequest): PhraseResponse {
        val phrase = phraseRepository.findById(phraseId)
            .orElseThrow { IllegalArgumentException("Phrase not found") }

        if (phrase.user.id != user.id) {
            throw IllegalArgumentException("Phrase does not belong to user")
        }

        phrase.isActive = request.isActive
        val updated = phraseRepository.save(phrase)

        logger.info("Phrase {} updated: isActive={}", phraseId, request.isActive)

        return toResponse(updated)
    }

    fun deletePhrase(user: User, phraseId: Long) {
        val phrase = phraseRepository.findById(phraseId)
            .orElseThrow { IllegalArgumentException("Phrase not found") }

        if (phrase.user.id != user.id) {
            throw IllegalArgumentException("Phrase does not belong to user")
        }

        phraseRepository.delete(phrase)
        logger.info("Phrase {} deleted", phraseId)
    }

    private fun toResponse(phrase: Phrase): PhraseResponse {
        return PhraseResponse(
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
}

