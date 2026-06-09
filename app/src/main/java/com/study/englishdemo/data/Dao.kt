package com.study.englishdemo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface WordBookDao {
    @Query("SELECT * FROM word_books ORDER BY createdAt DESC")
    fun observeBooks(): Flow<List<WordBookEntity>>

    @Insert
    suspend fun insert(book: WordBookEntity): Long

    @Query("DELETE FROM word_books WHERE id = :bookId")
    suspend fun deleteById(bookId: Long)

    @Query("SELECT * FROM word_books WHERE id = :bookId LIMIT 1")
    suspend fun getById(bookId: Long): WordBookEntity?

    @Query("SELECT * FROM word_books WHERE title = :title LIMIT 1")
    suspend fun findByTitle(title: String): WordBookEntity?

    @Query("UPDATE word_books SET title = :title, description = :description WHERE id = :bookId")
    suspend fun renameBook(bookId: Long, title: String, description: String)

    @Query("UPDATE word_books SET totalWords = :count WHERE id = :bookId")
    suspend fun updateTotalWords(bookId: Long, count: Int)
}

@Dao
interface WordEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WordEntryEntity>): List<Long>

    @Query("SELECT * FROM word_entries WHERE bookId = :bookId ORDER BY term COLLATE NOCASE")
    suspend fun getEntriesForBook(bookId: Long): List<WordEntryEntity>

    @Query("SELECT term FROM word_entries WHERE bookId = :bookId")
    suspend fun getTermsForBook(bookId: Long): List<String>

    @Query("SELECT COUNT(*) FROM word_entries WHERE bookId = :bookId")
    suspend fun countForBook(bookId: Long): Int

    @Query("SELECT * FROM word_entries WHERE id IN (:ids)")
    suspend fun getEntriesByIds(ids: List<Long>): List<WordEntryEntity>

    @Query("UPDATE word_entries SET mnemonic = :text WHERE id = :wordId")
    suspend fun updateMnemonic(wordId: Long, text: String)

    @Query(
        """
        SELECT * FROM word_entries
        WHERE bookId = :bookId
          AND (:keyword = ''
               OR term LIKE '%' || :keyword || '%' COLLATE NOCASE
               OR derivatives LIKE '%' || :keyword || '%' COLLATE NOCASE
               OR translation LIKE '%' || :keyword || '%' COLLATE NOCASE
               OR definition LIKE '%' || :keyword || '%' COLLATE NOCASE)
        ORDER BY term COLLATE NOCASE
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun searchInBook(bookId: Long, keyword: String, limit: Int, offset: Int): List<WordEntryEntity>

    @Query(
        """
        SELECT * FROM word_entries
        WHERE bookId = :bookId AND rootKey = :rootKey AND id != :excludeId
        ORDER BY frq ASC
        LIMIT :limit
        """,
    )
    suspend fun getBookRootSiblings(bookId: Long, rootKey: String, excludeId: Long, limit: Int): List<WordEntryEntity>

    @Query(
        """
        SELECT * FROM word_entries
        WHERE bookId = :bookId AND rootKey != ''
        ORDER BY rootKey ASC, frq ASC, positionInBook ASC
        """,
    )
    suspend fun getClusteredEntries(bookId: Long): List<WordEntryEntity>

    @Query("SELECT COUNT(*) FROM word_entries WHERE bookId = :bookId AND rootKey != ''")
    suspend fun countClusteredForBook(bookId: Long): Int

    @Query(
        """
        SELECT * FROM word_entries
        WHERE bookId = :bookId AND id != :excludeId
        ORDER BY (rootKey = :rootKey AND rootKey != '') DESC, RANDOM()
        LIMIT :limit
        """,
    )
    suspend fun getDistractorPool(bookId: Long, excludeId: Long, rootKey: String, limit: Int): List<WordEntryEntity>
}

@Dao
interface WordProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: WordProgressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progress: List<WordProgressEntity>): List<Long>

    @Update
    suspend fun update(progress: WordProgressEntity)

    @Query("SELECT * FROM word_progress WHERE wordId = :wordId LIMIT 1")
    suspend fun getByWordId(wordId: Long): WordProgressEntity?

    @Query("SELECT * FROM word_progress WHERE phase != 'NEW' AND nextReviewAt <= :now ORDER BY nextReviewAt ASC LIMIT :limit")
    suspend fun getDue(now: Instant, limit: Int): List<WordProgressEntity>

    @Query(
        """
        SELECT wp.* FROM word_progress AS wp
        INNER JOIN word_entries AS we ON we.id = wp.wordId
        WHERE we.bookId = :bookId AND wp.phase != 'NEW' AND wp.nextReviewAt <= :now
        ORDER BY wp.nextReviewAt ASC
        LIMIT :limit
        """,
    )
    suspend fun getDueForBook(bookId: Long, now: Instant, limit: Int): List<WordProgressEntity>

    @Query("SELECT * FROM word_progress WHERE phase = 'NEW' ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getFresh(limit: Int): List<WordProgressEntity>

    @Query(
        """
        SELECT wp.* FROM word_progress AS wp
        INNER JOIN word_entries AS we ON we.id = wp.wordId
        WHERE we.bookId = :bookId AND wp.phase = 'NEW'
        ORDER BY we.positionInBook ASC
        LIMIT :limit
        """,
    )
    suspend fun getFreshForBook(bookId: Long, limit: Int): List<WordProgressEntity>

    @Query("SELECT COUNT(*) FROM word_progress WHERE phase != 'NEW' AND nextReviewAt <= :now")
    suspend fun countDue(now: Instant): Int

    @Query(
        """
        SELECT COUNT(*) FROM word_progress AS wp
        INNER JOIN word_entries AS we ON we.id = wp.wordId
        WHERE we.bookId = :bookId AND wp.phase != 'NEW' AND wp.nextReviewAt <= :now
        """,
    )
    suspend fun countDueForBook(bookId: Long, now: Instant): Int

    @Query("SELECT COUNT(*) FROM word_progress WHERE phase = 'NEW'")
    suspend fun countNew(): Int

    @Query(
        """
        SELECT COUNT(*) FROM word_progress AS wp
        INNER JOIN word_entries AS we ON we.id = wp.wordId
        WHERE we.bookId = :bookId AND wp.phase = 'NEW'
        """,
    )
    suspend fun countNewForBook(bookId: Long): Int

    @Query("SELECT * FROM word_progress WHERE wordId IN (:ids)")
    suspend fun getByWordIds(ids: List<Long>): List<WordProgressEntity>

    @Query(
        """
        SELECT COUNT(*) FROM word_progress AS wp
        INNER JOIN word_entries AS we ON we.id = wp.wordId
        WHERE we.bookId = :bookId AND wp.phase != 'NEW'
        """,
    )
    suspend fun countLearnedForBook(bookId: Long): Int
}

@Dao
interface ReviewLogDao {
    @Insert
    suspend fun insert(log: ReviewLogEntity)

    @Query("SELECT COUNT(*) FROM review_logs WHERE reviewedAt >= :start AND reviewedAt < :end")
    suspend fun countReviewedBetween(start: Instant, end: Instant): Int

    @Query("SELECT reviewedAt FROM review_logs ORDER BY reviewedAt DESC")
    suspend fun getAllReviewTimesDesc(): List<Instant>

    @Query("SELECT reviewedAt FROM review_logs WHERE reviewedAt >= :since ORDER BY reviewedAt ASC")
    suspend fun getReviewTimesSince(since: Instant): List<Instant>

    @Query(
        """
        SELECT rl.wordId AS wordId,
               SUM(CASE WHEN rl.rating = 'AGAIN' THEN 1 ELSE 0 END) AS againCount,
               MAX(rl.reviewedAt) AS lastReviewedAt
        FROM review_logs AS rl
        INNER JOIN word_entries AS we ON we.id = rl.wordId
        WHERE we.bookId = :bookId
        GROUP BY rl.wordId
        HAVING againCount > 0
        ORDER BY againCount DESC, lastReviewedAt DESC
        LIMIT :limit
        """,
    )
    suspend fun getToughWordsForBook(bookId: Long, limit: Int): List<ToughWordRow>
}

data class ToughWordRow(
    val wordId: Long,
    val againCount: Int,
    val lastReviewedAt: Instant?,
)

data class WordWithProgressRow(
    val id: Long,
    val term: String,
    val phonetic: String,
    val definition: String,
    val translation: String,
    val example: String,
    val tags: String,
    val phase: StudyPhase?,
    val familiarity: Int?,
    val streak: Int?,
    val lapses: Int?,
    val easeFactor: Double?,
    val intervalDays: Int?,
    val lastReviewedAt: Instant?,
    val nextReviewAt: Instant?,
)
