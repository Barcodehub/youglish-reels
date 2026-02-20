package youglish.anki.demo.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import youglish.anki.demo.domain.entity.Phrase
import youglish.anki.demo.domain.entity.User

@Repository
interface PhraseRepository : JpaRepository<Phrase, Long> {

    fun findByUserAndIsActiveTrue(user: User): List<Phrase>

    @Query("SELECT COUNT(p) FROM Phrase p WHERE p.user = :user AND p.isActive = true")
    fun countActiveByUser(@Param("user") user: User): Long

    @Query("""
        SELECT p FROM Phrase p 
        WHERE p.user = :user 
        AND p.isActive = true 
        AND p.id NOT IN (
            SELECT vh.phrase.id FROM VideoHistory vh 
            WHERE vh.user = :user 
            AND vh.createdAt > CURRENT_TIMESTAMP - INTERVAL '1 MINUTE'
        )
        ORDER BY FUNCTION('RANDOM')
    """)
    fun findRandomActivePhraseExcludingRecent(@Param("user") user: User): List<Phrase>

    fun existsByUserAndText(user: User, text: String): Boolean
}

