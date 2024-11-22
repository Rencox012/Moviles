package com.example.mobiles.api
import com.example.mobiles.classes.LoginRequest
import com.example.mobiles.classes.RegisterRequest
import com.example.mobiles.classes.Usuario
import com.example.mobiles.classes.updateRequest
import com.example.mobiles.classes.updateWithPasswordRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path



interface Usuarios {
    // Crear un nuevo usuario
    @POST("usuarios/")
    fun createUser(@Body usuario: RegisterRequest): Call<Usuario>

    // Obtener un usuario por ID
    @GET("usuarios/{id}")
    fun getUserById(@Path("id") id: String): Call<Usuario>

    //actualizar un usuario al modificar sus datos enviandolos en el body
    @PATCH("usuarios/{id}")
    fun updateUserWithPassword(@Path("id") id: String, @Body usuario: updateWithPasswordRequest): Call<Usuario>

    @PATCH("usuarios/{id}")
    fun updateUser(@Path("id") id: String, @Body usuario: updateRequest): Call<Usuario>


    //inicio de sesion
    @POST("sesion/")
    fun iniciarSesion(@Body loginRequest: LoginRequest): Call<Usuario>
}