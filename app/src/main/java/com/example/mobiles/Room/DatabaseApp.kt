package com.example.mobiles.Room

import android.app.Application
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseApp: Application() {
    companion object {
        lateinit var database: AppDatabase
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agrega la nueva columna de imagen a la tabla transacciones
                database.execSQL("ALTER TABLE transacciones ADD COLUMN imagen TEXT")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)


    }



}