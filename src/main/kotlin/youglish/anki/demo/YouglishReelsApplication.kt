package youglish.anki.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * YouGlish Reels - TikTok-style language learning application
 *
 * Features:
 * - Multi-user support with JWT authentication
 * - Infinite scroll video feed
 * - Smart anti-repetition algorithm
 * - YouGlish API integration
 * - Clean architecture (Hexagonal)
 */
@SpringBootApplication
class YouglishReelsApplication

fun main(args: Array<String>) {
	runApplication<YouglishReelsApplication>(*args)
}

