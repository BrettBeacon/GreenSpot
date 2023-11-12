package com.csc202.assignment.greenspot

import android.app.Application

class GreenSpotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlantRepository.initialize(this)
    }
}