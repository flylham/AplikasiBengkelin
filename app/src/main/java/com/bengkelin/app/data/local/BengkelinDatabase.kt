package com.bengkelin.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bengkelin.app.data.model.User

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class BengkelinDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: BengkelinDatabase? = null

        fun getDatabase(context: Context): BengkelinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BengkelinDatabase::class.java,
                    "bengkelin_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
