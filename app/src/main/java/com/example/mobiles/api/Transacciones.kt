package com.example.mobiles.api
import com.example.mobiles.classes.CreateTransactionRequest
import com.example.mobiles.classes.Transaccion
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TransaccionesApi {

    // Obtener el balance total segun el tipo de transaccion
    @GET("transacciones/type/balance/{id}/{type}")
    fun getBalanceType(
        @Path("id") id: String,
        @Path("type") type: String
    ): Call<List<Transaccion>>

    //obtener todas las transacciones segun el tipo
    @GET("transacciones/all/{id}/{type}")
    fun getAllTransactions(
        @Path("id") id: String,
        @Path("type") type: String
    ): Call<List<Transaccion>>

    //obtener el balance
    @GET("transacciones/balance/{id}")
    fun getBalance(
        @Path("id") id: String
    ): Call<Double>

    //Get a transaction by id
    @GET("transacciones/{id}")
    fun getTransactionById(
        @Path("id") id: String
    ): Call<Transaccion>

    //Crear una transaccion
    @POST("transacciones/")
    fun createTransaction(
        @Body transaccion: CreateTransactionRequest
    ): Call<Transaccion>

    //Borrar una transaccion con el ID
    @DELETE("transacciones/{id}")
    fun deleteTransaction(
        @Path("id") id: String
    ): Call<Transaccion>

    //Actualizar una transaccion
    @PUT("transacciones/{id}")
    fun updateTransaction(
        @Path("id") id: String,
        @Body transaccion: Transaccion
    ): Call<Transaccion>
}