package com.example.mobiles.classes

import com.example.mobiles.Room.TransaccionEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//clase que representa una transaccion. Tiene lo siguiente: Monto, tipo, fecha de transaccion, descripcion, id del usuario y estado
data class Transaccion(
    val _id: String,
    val idLocal: Int ? = 0,
    val monto: Double,
    val tipo: String,
    val fecha_transac: String,
    val descripcion: String,
    val usuario_id: String,
    val imagen: List<String>,
    val estado: Number,
    val activo: Number
) {
    companion object {
        fun fromEntity(entity: TransaccionEntity): Transaccion {
            val imagenListType = object : TypeToken<List<String>>() {}.type
            val imagenList: List<String> = Gson().fromJson(entity.imagen, imagenListType)
            return Transaccion(
                _id = entity.idRemoto.toString(),
                idLocal = entity.id,
                monto = entity.monto,
                tipo = entity.tipo,
                imagen = imagenList,
                fecha_transac = entity.fechaTransac,
                descripcion = entity.descripcion ?: "",
                usuario_id = entity.usuarioId,
                estado = 1,
                activo = entity.activo
            )
        }

        fun toEntity(transaccion: Transaccion): TransaccionEntity {
            val imagenJson = Gson().toJson(transaccion.imagen)
            return TransaccionEntity(
                idRemoto = transaccion._id,
                monto = transaccion.monto,
                tipo = transaccion.tipo,
                fechaTransac = transaccion.fecha_transac,
                descripcion = transaccion.descripcion,
                usuarioId = transaccion.usuario_id,
                imagen = imagenJson,
                estado = 1,
                activo = transaccion.activo.toInt()
            )
        }
    }
}

data class CreateTransactionRequest(
    val usuario_id: String,
    val monto: Double,
    val tipo: String,
    val descripcion: String,
    val fecha_transac: String ? = "",
    val imagen: List<String> ? = emptyList(),
    val estado: Number ? = 1,
    val activo : Number ? = 1

)
{
    companion object {
        fun fromTransaccion(transaccion: Transaccion): CreateTransactionRequest {
            return CreateTransactionRequest(
                usuario_id = transaccion.usuario_id,
                monto = transaccion.monto,
                tipo = transaccion.tipo,
                descripcion = transaccion.descripcion,
                fecha_transac = transaccion.fecha_transac,
                imagen = transaccion.imagen,
                estado = transaccion.estado,
                activo = transaccion.activo
            )
        }
    }

}
