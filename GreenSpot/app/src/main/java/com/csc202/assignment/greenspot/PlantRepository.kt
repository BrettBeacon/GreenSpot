package com.csc202.assignment.greenspot

import android.content.Context
import androidx.room.CoroutinesRoom
import androidx.room.Room
import com.csc202.assignment.greenspot.database.PlantDatabase
import com.csc202.assignment.greenspot.database.migration_1_2
import com.csc202.assignment.greenspot.database.migration_2_3
import com.csc202.assignment.greenspot.database.migration_3_4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.util.UUID

private const val DATABASE_NAME = "plant-database"

class PlantRepository private constructor(context: Context, private val coroutineScope: CoroutineScope = GlobalScope) {

    private val database: PlantDatabase = Room.databaseBuilder(
        context.applicationContext,
        PlantDatabase::class.java,
        DATABASE_NAME
    )
        .addMigrations(migration_1_2)
        .addMigrations(migration_2_3)
        .addMigrations(migration_3_4)
    .build()

    fun getPlants(): Flow<List<Plant>> = database.plantDao().getPlants()

    fun getPlant(id: UUID): Flow<Plant> = database.plantDao().getPlant(id)

    fun updatePlant(plant: Plant) {
        coroutineScope.launch {
            database.plantDao().updatePlant(plant)
        }
    }

    fun addPlant(plant: Plant) {
        coroutineScope.launch {
            database.plantDao().addPlant(plant)
        }
    }

    fun deletePlant(plant: Plant) {
        coroutineScope.launch {
            database.plantDao().deletePlant(plant)
        }
    }

    companion object {
        private var INSTANCE: PlantRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = PlantRepository(context)
            }
        }

        fun get(): PlantRepository {
            return INSTANCE ?:
            throw IllegalStateException("PlantRepository must be initialized")
        }
    }
}