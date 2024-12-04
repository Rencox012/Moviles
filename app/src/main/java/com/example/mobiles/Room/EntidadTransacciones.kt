package com.example.mobiles.Room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters


@Entity(tableName = "transacciones")
@TypeConverters(Convertidores::class)
data class TransaccionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id") val id: Int = 0,
    @ColumnInfo(name="idRemoto") var idRemoto: String? = null,
    @ColumnInfo(name="monto") val monto: Double,
    @ColumnInfo(name="tipo") val tipo: String,
    @ColumnInfo(name="fechaTransac") val fechaTransac: String,
    @ColumnInfo(name="descripcion") val descripcion: String?,
    @ColumnInfo(name="usuarioId") val usuarioId: String,
    @ColumnInfo(name="imagen") val imagen: String = "[]", // Store as JSON string
    @ColumnInfo(name="estado") var estado: Int = 0, // 0: No sincronizado, 1: Sincronizado
    @ColumnInfo(name="activo") var activo: Int = 1
)
