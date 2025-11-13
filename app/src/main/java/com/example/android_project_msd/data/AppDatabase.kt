package com.example.android_project_msd.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao


    companion object { // static in java
        @Volatile // ensures visibility of changes to INSTANCE across threads
        private var INSTANCE: AppDatabase? = null //singleton: one database instance

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // if db instance not null return, else run code after ?: synchronized block to ensure only one thread can create db instance
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "user_database"
                ).fallbackToDestructiveMigration() // delete data if version changes
                 .build()
                INSTANCE = instance // save instance
                instance // return instance
            }
        }
    }
}
