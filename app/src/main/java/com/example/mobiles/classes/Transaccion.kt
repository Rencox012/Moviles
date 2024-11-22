package com.example.mobiles.classes

//clase que representa una transaccion. Tiene lo siguiente: Monto, tipo, fecha de transaccion, descripcion, id del usuario y estado
data class Transaccion(
    val _id: String,
    val monto: Double,
    val tipo: String,
    val imagen: String,
    val fecha_transac: String,
    val descripcion: String,
    val usuario_id: String,
    val estado: Number
)

data class CreateTransactionRequest(
    val usuario_id: String,
    val monto: Double,
    val tipo: String,
    val descripcion: String,
    val fecha_transac: String ? = "",
    val imagen: String ? = ""

)
