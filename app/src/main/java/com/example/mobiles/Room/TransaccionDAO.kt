package com.example.mobiles.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransaccionDAO {

    //insertar transaccion. Si el idRepeticion ya existe, se reemplaza o se actualiza
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarTransaccion(transaccion: TransaccionEntity): Long

    //Query para ver si la transaccion existe checando el idRemoto, si no existe, devuelve null
    @Query("SELECT * FROM transacciones WHERE idRemoto = :idRemoto")
    fun obtenerTransaccionPorIdRemoto(idRemoto: String): TransaccionEntity

    //Query para obtener la transaccion dado el ID local
    @Query("SELECT * FROM transacciones WHERE id = :id")
    fun obtenerTransaccionPorId(id: Int): TransaccionEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarTransacciones(transacciones: List<TransaccionEntity>): List<Long>

    @Query("SELECT * FROM transacciones WHERE usuarioId = :usuarioId AND activo = 1 ORDER BY fechaTransac DESC")
    fun obtenerTransaccionesPorUsuario(usuarioId: String): MutableList<TransaccionEntity>

    @Query("SELECT * FROM transacciones WHERE estado = 0")
    fun obtenerTransaccionesNoSincronizadas(): MutableList<TransaccionEntity>

    @Query("UPDATE transacciones SET estado = 1 WHERE id IN (:ids)")
    fun marcarComoSincronizadas(ids: List<Int>)

    @Query("SELECT SUM(monto) FROM transacciones WHERE tipo = 'ingreso' AND usuarioId = :usuarioId AND activo = 1")
    fun obtenerTotalIngresos(usuarioId: String): Double

    @Query("SELECT SUM(monto) FROM transacciones WHERE tipo = 'gasto' AND usuarioId = :usuarioId AND activo = 1")
    fun obtenerTotalGastos(usuarioId: String): Double

    @Query("DELETE FROM transacciones")
    fun limpiarTransacciones()

    //QUery para borrar logicamente una transaccion, cambiando su estado a 0 y su estado de sincronizacion a
    @Query("UPDATE transacciones SET estado = 0, activo = 0 WHERE id = :id")
    fun borrarTransaccion(id: Int)

    //Query para actualizar el idRemoto de una transaccion
    @Query("UPDATE transacciones SET idRemoto = :idRemoto WHERE id = :id")
    fun actualizarIdRemoto(id: Int, idRemoto: String)

    //query de busqueda con 5 parametros opcionales, una palabra, monto minimo y monto maximo. Estos parametros pueden o no estar presentes, pero siempre habra 1 minimo. Las fechas son string en formadio dia-mes-año
    @Query("""
    SELECT * FROM transacciones 
    WHERE usuarioId = :usuarioId 
    AND (:busqueda IS NULL OR descripcion LIKE '%' || :busqueda || '%')
    AND (:montoMin IS NULL OR monto >= :montoMin) 
    AND (:montoMax IS NULL OR monto <= :montoMax) 
    ORDER BY fechaTransac DESC
""")
    fun buscarTransacciones(
        usuarioId: String,
        busqueda: String?,
        montoMin: Double?,
        montoMax: Double?
    ): MutableList<TransaccionEntity>

    //Query para actualizar una transaccion, dado el ID y una transaccion, tambien ponemos su estado en 0 para ser sincronizado
    @Query("UPDATE transacciones SET monto = :monto, tipo = :tipo, descripcion = :descripcion, imagen = :imagen, estado = 0 WHERE id = :id")
    fun actualizarTransaccion(
        id: Int,
        monto: Double,
        tipo: String,
        descripcion: String,
        imagen: String
    )
}