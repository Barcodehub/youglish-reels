package youglish.anki.demo.domain.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import youglish.anki.demo.domain.entity.Phrase
import youglish.anki.demo.domain.entity.User
import youglish.anki.demo.domain.entity.VideoHistory
import java.time.LocalDateTime

@Repository
interface VideoHistoryRepository : JpaRepository<VideoHistory, Long> {

    @Query("""
        SELECT vh.videoId FROM VideoHistory vh 
        WHERE vh.user = :user 
        AND vh.phrase = :phrase 
        ORDER BY vh.createdAt DESC
    """)
    fun findRecentVideoIdsByUserAndPhrase(
        @Param("user") user: User,
        @Param("phrase") phrase: Phrase,
        pageable: Pageable
    ): List<String>

    @Query("""
        SELECT vh FROM VideoHistory vh 
        WHERE vh.user = :user 
        ORDER BY vh.createdAt DESC
    """)
    fun findRecentByUser(@Param("user") user: User, pageable: Pageable): List<VideoHistory>

    @Modifying
    @Query("DELETE FROM VideoHistory vh WHERE vh.user = :user AND vh.createdAt < :before")
    fun deleteOldHistory(@Param("user") user: User, @Param("before") before: LocalDateTime): Int

    fun countByUser(user: User): Long
}

