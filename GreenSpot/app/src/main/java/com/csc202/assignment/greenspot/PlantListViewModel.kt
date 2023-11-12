package com.csc202.assignment.greenspot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

private const val TAG = "PlantListViewModel"

class PlantListViewModel : ViewModel() {
    private val plantRepository = PlantRepository.get()

    private val _plants: MutableStateFlow<List<Plant>> = MutableStateFlow(emptyList())
    val plants: StateFlow<List<Plant>>
        get() = _plants.asStateFlow()

    init {
        viewModelScope.launch {
            plantRepository.getPlants().collect {
                _plants.value = it
            }
        }
    }

    suspend fun addPlant(plant: Plant) {
        plantRepository.addPlant(plant)
    }

    suspend fun deletePlant(plant: Plant) {
        plantRepository.deletePlant(plant)
    }
}