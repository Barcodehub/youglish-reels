package youglish.anki.demo.infrastructure.web

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import youglish.anki.demo.application.dto.CreatePhraseRequest
import youglish.anki.demo.application.dto.PhraseResponse
import youglish.anki.demo.application.dto.UpdatePhraseRequest
import youglish.anki.demo.domain.repository.UserRepository
import youglish.anki.demo.domain.service.PhraseService
import youglish.anki.demo.infrastructure.security.CustomUserDetails

@RestController
@RequestMapping("/api/phrases")
@CrossOrigin(origins = ["*"])
class PhraseController(
    private val phraseService: PhraseService,
    private val userRepository: UserRepository
) {

    @PostMapping
    fun createPhrase(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: CreatePhraseRequest
    ): ResponseEntity<PhraseResponse> {
        val user = userRepository.findById(userDetails.getId())
            .orElseThrow { IllegalStateException("User not found") }

        val response = phraseService.createPhrase(user, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getUserPhrases(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<PhraseResponse>> {
        val user = userRepository.findById(userDetails.getId())
            .orElseThrow { IllegalStateException("User not found") }

        val phrases = phraseService.getUserPhrases(user)
        return ResponseEntity.ok(phrases)
    }

    @GetMapping("/{phraseId}")
    fun getPhraseById(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable phraseId: Long
    ): ResponseEntity<PhraseResponse> {
        val user = userRepository.findById(userDetails.getId())
            .orElseThrow { IllegalStateException("User not found") }

        val phrase = phraseService.getPhraseById(user, phraseId)
        return ResponseEntity.ok(phrase)
    }

    @PutMapping("/{phraseId}")
    fun updatePhrase(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable phraseId: Long,
        @Valid @RequestBody request: UpdatePhraseRequest
    ): ResponseEntity<PhraseResponse> {
        val user = userRepository.findById(userDetails.getId())
            .orElseThrow { IllegalStateException("User not found") }

        val phrase = phraseService.updatePhrase(user, phraseId, request)
        return ResponseEntity.ok(phrase)
    }

    @DeleteMapping("/{phraseId}")
    fun deletePhrase(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable phraseId: Long
    ): ResponseEntity<Void> {
        val user = userRepository.findById(userDetails.getId())
            .orElseThrow { IllegalStateException("User not found") }

        phraseService.deletePhrase(user, phraseId)
        return ResponseEntity.noContent().build()
    }
}

