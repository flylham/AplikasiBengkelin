package com.bengkelin.app

import android.app.Application
import com.bengkelin.app.data.local.BengkelinDatabase
import com.bengkelin.app.data.local.TokenManager

/**
 * Application class untuk Bengkelin (Arsitektur REST API & Local Room DB).
 */
class BengkelinApp : Application() {

    lateinit var database: BengkelinDatabase
        private set

    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = BengkelinDatabase.getDatabase(this)
        tokenManager = TokenManager(this)
    }

    companion object {
        lateinit var instance: BengkelinApp
            private set
    }
}
