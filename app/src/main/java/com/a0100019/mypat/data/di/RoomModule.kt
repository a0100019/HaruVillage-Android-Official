package com.a0100019.mypat.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.a0100019.mypat.data.room.walk.WalkDao
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.data.room.Database
import com.a0100019.mypat.data.room.allUser.AllUserDao
import com.a0100019.mypat.data.room.allUser.getAllUserInitialData
import com.a0100019.mypat.data.room.diary.DiaryDao
import com.a0100019.mypat.data.room.diary.getDiaryInitialData
import com.a0100019.mypat.data.room.english.EnglishDao
import com.a0100019.mypat.data.room.english.getEnglishInitialData
import com.a0100019.mypat.data.room.item.ItemDao
import com.a0100019.mypat.data.room.item.getItemInitialData
import com.a0100019.mypat.data.room.pat.PatDao
import com.a0100019.mypat.data.room.koreanIdiom.KoreanIdiomDao
import com.a0100019.mypat.data.room.koreanIdiom.getKoreanIdiomInitialData
import com.a0100019.mypat.data.room.letter.LetterDao
import com.a0100019.mypat.data.room.letter.getLetterInitialData
import com.a0100019.mypat.data.room.area.AreaDao
import com.a0100019.mypat.data.room.area.getAreaInitialData
import com.a0100019.mypat.data.room.knowledge.KnowledgeDao
import com.a0100019.mypat.data.room.knowledge.getKnowledgeInitialData
import com.a0100019.mypat.data.room.pat.getPatInitialData
import com.a0100019.mypat.data.room.photo.PhotoDao
import com.a0100019.mypat.data.room.sudoku.SudokuDao
import com.a0100019.mypat.data.room.sudoku.getSudokuInitialData
import com.a0100019.mypat.data.room.user.getUserInitialData
import com.a0100019.mypat.data.room.walk.getWalkInitialData
import com.a0100019.mypat.data.room.world.WorldDao
import com.a0100019.mypat.data.room.world.getWorldInitialData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    private lateinit var database: Database

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): Database {

        database = Room.databaseBuilder(
            context,
            Database::class.java,
            "database"
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .addCallback(object : RoomDatabase.Callback() {

                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    CoroutineScope(Dispatchers.IO).launch {
                        // ✅ 이미 생성된 database 인스턴스 사용
                        database.userDao().insertAll(getUserInitialData())
                        database.walkDao().insertAll(getWalkInitialData())
                        database.diaryDao().insertAll(getDiaryInitialData())
                        database.englishDao().insertAll(getEnglishInitialData())
                        database.koreanIdiomDao().insertAll(getKoreanIdiomInitialData())
                        database.patDao().insertAll(getPatInitialData())
                        database.itemDao().insertAll(getItemInitialData())
                        database.worldDao().insertAll(getWorldInitialData())
                        database.sudokuDao().insertAll(getSudokuInitialData())
                        database.letterDao().insertAll(getLetterInitialData())
                        database.areaDao().insertAll(getAreaInitialData())
                        database.allUserDao().insertAll(getAllUserInitialData())
                        database.knowledgeDao().insertAll(getKnowledgeInitialData())
                    }
                }
            })
            .build()

        return database
    }

    // ✅ Migration (기존 유저 데이터 유지)
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS knowledge_table (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date TEXT NOT NULL,
                    state TEXT NOT NULL,
                    answer TEXT NOT NULL,
                    meaning TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    // 1. Migration 변수 정의 (버전 3 -> 4)
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS photo_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                localPath TEXT NOT NULL,
                firebaseUrl TEXT NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
            )
        }
    }

    // ===== DAO Providers =====
    @Provides fun provideUserDao(db: Database): UserDao = db.userDao()
    @Provides fun provideWalkDao(db: Database): WalkDao = db.walkDao()
    @Provides fun provideDiaryDao(db: Database): DiaryDao = db.diaryDao()
    @Provides fun provideEnglishDao(db: Database): EnglishDao = db.englishDao()
    @Provides fun provideKoreanIdiomDao(db: Database): KoreanIdiomDao = db.koreanIdiomDao()
    @Provides fun providePatDao(db: Database): PatDao = db.patDao()
    @Provides fun provideItemDao(db: Database): ItemDao = db.itemDao()
    @Provides fun provideWorldDao(db: Database): WorldDao = db.worldDao()
    @Provides fun provideSudokuDao(db: Database): SudokuDao = db.sudokuDao()
    @Provides fun provideLetterDao(db: Database): LetterDao = db.letterDao()
    @Provides fun provideAllUserDao(db: Database): AllUserDao = db.allUserDao()
    @Provides fun provideAreaDao(db: Database): AreaDao = db.areaDao()
    @Provides fun provideKnowledgeDao(db: Database): KnowledgeDao = db.knowledgeDao()
    @Provides fun providePhotoDao(db: Database): PhotoDao = db.photoDao()

}
