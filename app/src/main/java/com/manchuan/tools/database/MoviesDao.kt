package com.manchuan.tools.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MoviesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovie(movies: Movies)

    @Query("DELETE FROM Movies WHERE title=:title")
    fun deleteMovie(title: String)

    @Query("SELECT * FROM Movies")
    fun queryAllMovies(): List<Movies>

    @Query("SELECT * FROM Movies WHERE title=:title")
    fun getMovieByTitle(title: String): Movies

}