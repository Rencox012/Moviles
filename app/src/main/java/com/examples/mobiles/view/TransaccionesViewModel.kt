package com.examples.mobiles.view

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiles.api.TransaccionesApi
import com.example.mobiles.classes.CreateTransactionRequest
import com.example.mobiles.classes.Transaccion
import com.example.mobiles.retroFit.RetrofitClient
import com.example.mobiles.utlidades.DataStoreManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TransaccionesViewModel(context: Context) : ViewModel() {

    private val dataStoreManager = DataStoreManager(context)



    private val transaccionesApi = RetrofitClient.instance.create(TransaccionesApi::class.java)

    private val _transaccion = MutableLiveData<Transaccion>()
    val transaccion: LiveData<Transaccion> get() = _transaccion

    private val _transacciones = MutableLiveData<List<Transaccion>>()
    val transacciones: LiveData<List<Transaccion>> get() = _transacciones



    private val _balanceTotal = MutableLiveData<Double>()
    val balanceTotal: LiveData<Double> get() = _balanceTotal

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error






    fun insertarTransaccion(transaccion : CreateTransactionRequest){
        transaccionesApi.createTransaction(transaccion).enqueue(object : retrofit2.Callback<Transaccion> {
            override fun onResponse(call : retrofit2.Call<Transaccion>, response: retrofit2.Response<Transaccion>) {
                if (response.isSuccessful) {
                    _transaccion.value = response.body()
                } else {
                    _error.value =
                        "Error al crear la transaccion: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call : retrofit2.Call<Transaccion>, t: Throwable) {
                _error.value = "Error de conexión: ${t.message}"
            }
        })
    }

    suspend fun insertarTransaccionSuspend(transaccion: CreateTransactionRequest): Transaccion? {
        return suspendCancellableCoroutine { continuation ->
            transaccionesApi.createTransaction(transaccion).enqueue(object : Callback<Transaccion> {
                override fun onResponse(call: Call<Transaccion>, response: Response<Transaccion>) {
                    if (response.isSuccessful) {
                        continuation.resume(response.body())
                    } else {
                        continuation.resumeWithException(Exception("Error al crear la transacción: ${response.errorBody()?.string()}"))
                    }
                }

                override fun onFailure(call: Call<Transaccion>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

    fun obtenerTransaccionesUsuario(id: String) {
        transaccionesApi.getAllTransactions(id, "mixed").enqueue(object : retrofit2.Callback<List<Transaccion>> {
            override fun onResponse(
                call: retrofit2.Call<List<Transaccion>>,
                response: retrofit2.Response<List<Transaccion>>
            ) {
                if (response.isSuccessful) {
                    _transacciones.value = response.body()
                    updateLoggedVar()
                    Log.d("TransaccionesViewModel", "Transacciones obtenidas: ${response.body()}")
                } else {
                    _error.value = "Error al obtener las transacciones: ${response.errorBody()?.string()}"
                    Log.e("TransaccionesViewModel", "Error al obtener las transacciones: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Transaccion>>, t: Throwable) {
                _error.value = "Error de conexión: ${t.message}"
                Log.e("TransaccionesViewModel", "Error de conexion: ${t.message}")
            }
        })
    }

    fun actualizarTransaccion(id: String, transaccion: Transaccion){
        transaccionesApi.updateTransaction(id, transaccion).enqueue(object : retrofit2.Callback<Transaccion> {
            override fun onResponse(call : retrofit2.Call<Transaccion>, response: retrofit2.Response<Transaccion>) {
                if (response.isSuccessful) {
                    _transaccion.value = response.body()
                } else {
                    _error.value =
                        "Error al actualizar la transaccion: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call : retrofit2.Call<Transaccion>, t: Throwable) {
                _error.value = "Error de conexión: ${t.message}"
            }
        })
    }

    suspend fun actualizarTransaccionSuspend(id: String, transaccion: Transaccion): Transaccion? {
        return suspendCancellableCoroutine { continuation   ->
            transaccionesApi.updateTransaction(id, transaccion).enqueue(object : Callback<Transaccion> {
                override fun onResponse(call: Call<Transaccion>, response: Response<Transaccion>) {
                    if (response.isSuccessful) {
                        continuation.resume(response.body())
                    } else {
                        continuation.resumeWithException(Exception("Error al actualizar la transacción: ${response.errorBody()?.string()}"))
                    }
                }

                override fun onFailure(call: Call<Transaccion>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

    private fun updateLoggedVar(){
        viewModelScope.launch {
            dataStoreManager.actualizarLogged(true)
        }
    }


    fun cleanError(){
        _error.value = ""
    }
    fun getError(): String? {
        return _error.value
    }
    fun clearMessage(){
        _message.value = ""
    }
    fun cleanTransaccion(){
        _transaccion.value = null
    }
    fun getTransaccion(): Transaccion? {
        return _transaccion.value
    }
    fun cleanTransacciones(){
        _transacciones.value = null
    }

}