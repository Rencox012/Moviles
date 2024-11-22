package com.example.mobiles.Room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transacciones")
data class TransaccionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id") val id: Int = 0,
    @ColumnInfo(name="idRemoto") var idRemoto: String? = null,
    @ColumnInfo(name="monto") val monto: Double,
    @ColumnInfo(name="tipo") val tipo: String,
    @ColumnInfo(name="fechaTransac") val fechaTransac: String,
    @ColumnInfo(name="descripcion") val descripcion: String?,
    @ColumnInfo(name="usuarioId") val usuarioId: String,
    @ColumnInfo(name="imagen") val imagen: String?,
    @ColumnInfo(name="estado") var estado: Int = 0 // 0: No sincronizado, 1: Sincronizado
)
