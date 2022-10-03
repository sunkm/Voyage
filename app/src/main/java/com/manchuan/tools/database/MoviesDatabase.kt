package com.manchuan.tools.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Movies::class], version = 1, exportSchema = true)
abstract class MoviesDatabase : RoomDatabase() {
    abstract fun getMoviesDao(): MoviesDao

    companion object {
        @Volatile
        private var INSTANCE: MoviesDatabase? = null
        fun getInstance(context: Context): MoviesDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, MoviesDatabase::class.java, "movies.db").allowMainThreadQueries()
                .build()
    }
}
