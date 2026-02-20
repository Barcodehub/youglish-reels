package youglish.anki.demo.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Video history to prevent consecutive repetitions
 */
@Entity
@Table(name = "video_history", indexes = [
    Index(name = "idx_user_created", columnList = "user_id,created_at"),
    Index(name = "idx_user_video", columnList = "user_id,video_id,phrase_id")
])
data class VideoHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phrase_id", nullable = false)
    var phrase: Phrase,

    @Column(nullable = false)
    var videoId: String,

    @Column(nullable = false)
    var trackNumber: Int,

    @Column
    var captionText: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VideoHistory) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}

