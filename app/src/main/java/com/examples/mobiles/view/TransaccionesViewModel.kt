package com.examples.mobiles.view

import android.content.Context
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

    fun obtenerTransaccionesUsuario(id: String){
        transaccionesApi.getAllTransactions(id, "mixed").enqueue(object : retrofit2.Callback<List<Transaccion>> {
            override fun onResponse(
                call: retrofit2.Call<List<Transaccion>>,
                response: retrofit2.Response<List<Transaccion>>
            ) {
                if (response.isSuccessful) {
                    _transacciones.value = response.body()
                    updateLoggedVar()

                } else {
                    _error.value = "Error al obtener las transacciones: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Transaccion>>, t: Throwable) {
                _error.value = "Error de conexión: ${t.message}"
            }
        })
    }

    private fun updateLoggedVar(){
        viewModelScope.launch {
            dataStoreManager.actualizarLogged(true)
        }
    }

}