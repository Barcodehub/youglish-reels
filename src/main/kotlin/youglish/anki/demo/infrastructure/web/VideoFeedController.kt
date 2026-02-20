package youglish.anki.demo.infrastructure.web

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import youglish.anki.demo.application.dto.NextVideoResponse
import youglish.anki.demo.application.dto.UserStatsResponse
import youglish.anki.demo.domain.repository.UserRepository
import youglish.anki.demo.domain.service.VideoFeedService
import youglish.anki.demo.infrastructure.security.CustomUserDetails

/**
 * Main controller for TikTok-style video feed
 */
@RestController
@RequestMapping("/api/feed")
@CrossOrigin(origins = ["*"])
class VideoFeedController(
    private val videoFeedService: VideoFeedService,
    private val userRepository: UserRepository
) {

    /**
     * Get next video for the feed
     * This is the core endpoint that powers the infinite scroll experience
     */
    @GetMapping("/next")
    fun getNextVideo(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<NextVideoResponse> {
        val user = userRepository.findById(userDetails.getId())
            .orElseThrow { IllegalStateException("User not found") }

        val video = videoFeedService.getNextVideo(user)
        return ResponseEntity.ok(video)
    }

    /**
     * Get user statistics
     */
    @GetMapping("/stats")
    fun getUserStats(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<UserStatsResponse> {
        val user = userRepository.findById(userDetails.getId())
            .orElseThrow { IllegalStateException("User not found") }

        val stats = videoFeedService.getUserStats(user)
        return ResponseEntity.ok(stats)
    }

    /**
     * Clean old history (maintenance endpoint)
     */
    @PostMapping("/clean-history")
    fun cleanHistory(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Void> {
        val user = userRepository.findById(userDetails.getId())
            .orElseThrow { IllegalStateException("User not found") }

        videoFeedService.cleanOldHistory(user)
        return ResponseEntity.ok().build()
    }
}

