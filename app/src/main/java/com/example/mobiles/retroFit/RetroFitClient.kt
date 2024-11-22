package com.example.mobiles.retroFit

import com.example.mobiles.api.TransaccionesApi
import com.example.mobiles.classes.Transaccion
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun main() {
    val transaccionesApi = RetrofitClient.instance.create(TransaccionesApi::class.java)

    // Ejemplo: Obtener el balance total por tipo
    transaccionesApi.getBalanceType("user123", "income").enqueue(object : Callback<List<Transaccion>> {
        override fun onResponse(call: Call<List<Transaccion>>, response: Response<List<Transaccion>>) {
            if (response.isSuccessful) {
                val balanceList = response.body()
                println("Balance obtenido: $balanceList")
            } else {
                println("Error en la respuesta: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<List<Transaccion>>, t: Throwable) {
            println("Error al conectar: ${t.message}")
        }
    })

    // Ejemplo: Obtener todas las transacciones por tipo
    transaccionesApi.getAllTransactions("user123", "expense").enqueue(object : Callback<List<Transaccion>> {
        override fun onResponse(call: Call<List<Transaccion>>, response: Response<List<Transaccion>>) {
            if (response.isSuccessful) {
                val transactions = response.body()
                println("Transacciones obtenidas: $transactions")
            } else {
                println("Error en la respuesta: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<List<Transaccion>>, t: Throwable) {
            println("Error al conectar: ${t.message}")
        }
    })
}