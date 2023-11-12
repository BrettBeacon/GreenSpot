package com.csc202.assignment.greenspot.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.csc202.assignment.greenspot.Plant
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface PlantDao {
    @Query("SELECT * FROM plant")
    fun getPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plant WHERE id=(:id)")
    fun getPlant(id: UUID): Flow<Plant>

    @Update
    fun updatePlant(plant: Plant)

    @Insert
    fun addPlant(plant: Plant)

    @Delete
    fun deletePlant(plant: Plant)

}