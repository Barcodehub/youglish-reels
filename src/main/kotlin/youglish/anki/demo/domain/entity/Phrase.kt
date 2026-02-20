package youglish.anki.demo.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Phrase/Word entity registered by users
 */
@Entity
@Table(name = "phrases", indexes = [
    Index(name = "idx_user_active", columnList = "user_id,is_active")
])
data class Phrase(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    var text: String,

    @Column(nullable = false)
    var language: String = "english",

    @Column
    var accent: String? = null,

    @Column(nullable = false)
    var totalVideosAvailable: Int = 0,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var lastUsedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Phrase) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}

