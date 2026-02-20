package youglish.anki.demo.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// Auth DTOs
data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    val password: String
)

data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class AuthResponse(
    val token: String,
    val type: String = "Bearer",
    val username: String,
    val email: String
)

// Phrase DTOs
data class CreatePhraseRequest(
    @field:NotBlank(message = "Text is required")
    @field:Size(min = 1, max = 200, message = "Text must be between 1 and 200 characters")
    val text: String,

    @field:NotBlank(message = "Language is required")
    val language: String = "english",

    val accent: String? = null
)

data class PhraseResponse(
    val id: Long,
    val text: String,
    val language: String,
    val accent: String?,
    val totalVideosAvailable: Int,
    val isActive: Boolean,
    val createdAt: String,
    val lastUsedAt: String?
)

data class UpdatePhraseRequest(
    val isActive: Boolean
)

// Video DTOs
data class NextVideoRequest(
    val excludeVideoIds: List<String> = emptyList(),
    val excludePhraseIds: List<Long> = emptyList()
)

data class NextVideoResponse(
    val videoId: String,
    val trackNumber: Int,
    val phrase: PhraseResponse,
    val captionText: String?,
    val language: String,
    val accent: String?,
    val totalResults: Int,
    val preloadNext: PreloadVideoInfo?
)

data class PreloadVideoInfo(
    val phraseId: Long,
    val phraseText: String,
    val estimatedTrackNumber: Int
)

// YouGlish API response (internal)
data class YouglishVideoInfo(
    val videoId: String,
    val trackNumber: Int,
    val captionText: String?,
    val totalResults: Int
)

// Statistics
data class UserStatsResponse(
    val totalPhrases: Long,
    val activePhrases: Long,
    val totalVideosWatched: Long,
    val phrasesUsedToday: Int
)

