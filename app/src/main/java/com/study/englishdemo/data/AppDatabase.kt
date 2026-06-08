package com.study.englishdemo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant

@Database(
    entities = [
        WordBookEntity::class,
        WordEntryEntity::class,
        WordProgressEntity::class,
        ReviewLogEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordBookDao(): WordBookDao
    abstract fun wordEntryDao(): WordEntryDao
    abstract fun wordProgressDao(): WordProgressDao
    abstract fun reviewLogDao(): ReviewLogDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "lexrise.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}

class RoomConverters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fromPhase(value: StudyPhase?): String? = value?.name

    @TypeConverter
    fun toPhase(value: String?): StudyPhase? = value?.let(StudyPhase::valueOf)

    @TypeConverter
    fun fromRating(value: ReviewRating?): String? = value?.name

    @TypeConverter
    fun toRating(value: String?): ReviewRating? = value?.let(ReviewRating::valueOf)
}
