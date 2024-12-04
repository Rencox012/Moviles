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
        val MIGRATION_2_3 = object : Migration(2,3){
            override fun migrate (database: SupportSQLiteDatabase){
                // Add the new column 'activo' to the 'transacciones' table
                database.execSQL("ALTER TABLE transacciones ADD COLUMN activo INTEGER NOT NULL DEFAULT 1")

                // Create a new table with the desired schema
                database.execSQL("""
                    CREATE TABLE transacciones_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        idRemoto TEXT,
                        monto REAL NOT NULL,
                        tipo TEXT NOT NULL,
                        fechaTransac TEXT NOT NULL,
                        descripcion TEXT,
                        usuarioId TEXT NOT NULL,
                        imagen TEXT NOT NULL DEFAULT '[]',
                        estado INTEGER NOT NULL DEFAULT 0,
                        activo INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())

                // Copy the data from the old table to the new table
                database.execSQL("""
                    INSERT INTO transacciones_new (id, idRemoto, monto, tipo, fechaTransac, descripcion, usuarioId, imagen, estado, activo)
                    SELECT id, idRemoto, monto, tipo, fechaTransac, descripcion, usuarioId, imagen, estado, 1
                    FROM transacciones
                """.trimIndent())

                // Remove the old table
                database.execSQL("DROP TABLE transacciones")

                // Rename the new table to the old table name
                database.execSQL("ALTER TABLE transacciones_new RENAME TO transacciones")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)


    }



}