package com.example.mobiles.classes

data class Usuario(
    val _id: String,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val imagen: String,
    val fechaCreacion: String,
    val estado: Number,
    val alias: String,
    val direccion: String,
    val telefono: String,
)

data class LoginRequest(
    val email: String,
    val password: String
)
data class RegisterRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val alias: String ? = "",
    val direccion: String ? = "",
    val telefono: String ? = ""
)
data class updateRequest(
    val nombre: String,
    val correo: String,
    val imagen: String,
    val alias: String ? = "",
    val direccion: String ? = "",
    val telefono: String ? = ""
)
data class updateWithPasswordRequest(
    val nombre: String,
    val correo: String,
    val imagen: String,
    val contrasena: String,
    val alias: String ? = "",
    val direccion: String ? = "",
    val telefono: String ? = ""
)

