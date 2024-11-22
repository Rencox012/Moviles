package com.examples.mobiles.view

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiles.api.Usuarios
import com.example.mobiles.classes.LoginRequest
import com.example.mobiles.classes.RegisterRequest
import com.example.mobiles.classes.Usuario
import com.example.mobiles.classes.updateRequest
import com.example.mobiles.classes.updateWithPasswordRequest
import com.example.mobiles.retroFit.RetrofitClient
import com.example.mobiles.utlidades.DataStoreManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class UsuariosViewModel(context: Context)  : ViewModel() {

    val dataStoreManager = DataStoreManager(context)


    private val usuariosApi = RetrofitClient.instance.create(Usuarios::class.java)

    private val _usuario = MutableLiveData<Usuario>()
    val usuario: LiveData<Usuario> get() = _usuario

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun createUser(usuario: RegisterRequest) {
        usuariosApi.createUser(usuario).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    _usuario.value = response.body()
                } else {
                    _error.value = "Error al crear el usuario: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                _error.value = "Error de conexión: ${t.message}"
            }
        })
    }

    fun getUserById(id: String) {
        usuariosApi.getUserById(id).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    _usuario.value = response.body()
                    // Save the user data
                    saveUserData(response.body()!!)
                } else {
                    _error.value = "Error al obtener el usuario: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                _error.value = "Error de conexión: ${t.message}"
            }
        })
    }

    fun iniciarSesion(email: String, password: String){
        val loginRequest = LoginRequest(email, password)
        usuariosApi.iniciarSesion(loginRequest).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    //obtenemos la información del usuario con su _id
                    getUserById(response.body()?._id ?: "")

                } else {
                    _error.value = "Error al iniciar sesión: ${response.errorBody()?.string()}"

                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                _error.value = "Error de conexión: ${t.message}"

            }
        })
    }

    fun updateUsuario(id: String ,usuario: updateRequest){
        usuariosApi.updateUser(id, usuario).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    _usuario.value = response.body()
                } else {
                    _error.value = "Error al actualizar el usuario: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                _error.value = "Error de conexión: ${t.message}"
            }
        })
    }

    fun updateUsuarioContrasena(id: String, usuario: updateWithPasswordRequest){
        usuariosApi.updateUserWithPassword(id, usuario).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    _usuario.value = response.body()
                } else {
                    _error.value = "Error al actualizar el usuario: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                _error.value = "Error de conexión: ${t.message}"
            }
        })
    }


    private fun saveUserData(user: Usuario) {
        viewModelScope.launch {
            dataStoreManager.saveUser(
                id = user._id,
                username = user.nombre ?: "",
                email = user.correo ?: "",
                foto = user.imagen ?: "",
                contrasena = user.contrasena ?: "",
                alias = user.alias ?: "",
                telefono = user.telefono ?: "",
                direccion = user.direccion ?: "",
                fechaCreacion = user.fechaCreacion ?: "",
            )
        }
    }
}