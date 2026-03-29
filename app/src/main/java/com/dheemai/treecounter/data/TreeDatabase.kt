package com.dheemai.treecounter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Tree::class, FarmPlot::class], version = 5, exportSchema = false)
abstract class TreeDatabase : RoomDatabase() {
    abstract fun treeDao(): TreeDao
    abstract fun farmPlotDao(): FarmPlotDao

    companion object {
        @Volatile private var INSTANCE: TreeDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trees ADD COLUMN additionalNames TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trees ADD COLUMN plotName TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS plots (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("ALTER TABLE trees ADD COLUMN plotId INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trees ADD COLUMN canvasX REAL NOT NULL DEFAULT -1")
                db.execSQL("ALTER TABLE trees ADD COLUMN canvasY REAL NOT NULL DEFAULT -1")
            }
        }

        fun getInstance(context: Context): TreeDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, TreeDatabase::class.java, "tree_counter.db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
