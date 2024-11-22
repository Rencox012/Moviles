package com.example.mobiles.utlidades

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mobiles.classes.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class DataStoreManager(private val context: Context) {

    private val USER_ID = stringPreferencesKey("user_id")
    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_EMAIL = stringPreferencesKey("user_email")
    private val USER_FOTO = stringPreferencesKey("user_foto")
    private val USER_CONTRASENA = stringPreferencesKey("user_contrasena")
    private val USER_LOGGED_IN = stringPreferencesKey("user_logged_in")
    private val USER_ALIAS = stringPreferencesKey("user_alias")
    private val USER_TELEFONO = stringPreferencesKey("user_telefono")
    private val USER_DIRECCION = stringPreferencesKey("user_direccion")
    private val USER_FECHA_CREACION = stringPreferencesKey("user_fecha_creacion")
    //Variable para saber si el usuario ya inicio sesion antes, se usa para recuperar los datos de las transacciones solo una vez.
    val userLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[USER_ID] != null }
    val userId: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_ID] }
    val userName: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_NAME] }
    val userEmail: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_EMAIL] }
    val userFoto: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_FOTO] }
    val userContrasena: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_CONTRASENA] }
    val userAlias: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_ALIAS] }
    val userTelefono: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_TELEFONO] }
    val userDireccion: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_DIRECCION] }
    val fechaCreacion: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_FECHA_CREACION] }

    suspend fun saveUser(id: String, username: String, email: String, foto: String, contrasena: String, alias: String, telefono: String, direccion: String, fechaCreacion: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = id
            prefs[USER_NAME] = username
            prefs[USER_EMAIL] = email
            prefs[USER_FOTO] = foto
            prefs[USER_CONTRASENA] = contrasena
            prefs[USER_LOGGED_IN] = "false"
            prefs[USER_ALIAS] = alias
            prefs[USER_TELEFONO] = telefono
            prefs[USER_DIRECCION] = direccion
            prefs[USER_FECHA_CREACION] = fechaCreacion

        }
    }
    suspend fun saveUserObject(usuario: Usuario) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = usuario._id
            prefs[USER_NAME] = usuario.nombre
            prefs[USER_EMAIL] = usuario.correo
            prefs[USER_FOTO] = usuario.imagen
            prefs[USER_CONTRASENA] = usuario.contrasena
            prefs[USER_LOGGED_IN] = "false"
            prefs[USER_ALIAS] = usuario.alias
            prefs[USER_TELEFONO] = usuario.telefono
            prefs[USER_DIRECCION] = usuario.direccion
            prefs[USER_FECHA_CREACION] = usuario.fechaCreacion
        }
    }

    suspend fun getUsuario(): Flow<Usuario>{
        return context.dataStore.data.map { prefs ->
            Usuario(
                prefs[USER_ID].toString(),
                prefs[USER_NAME].toString(),
                prefs[USER_EMAIL].toString(),
                prefs[USER_CONTRASENA].toString(),
                prefs[USER_FOTO].toString(),
                prefs[USER_FECHA_CREACION].toString(),
                1,
                prefs[USER_ALIAS].toString(),
                prefs[USER_DIRECCION].toString(),
                prefs[USER_TELEFONO].toString()
            )
        }
    }

    suspend fun actualizarLogged(logged: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[USER_LOGGED_IN] = logged.toString()
        }
    }

    fun getUsuarioId(): String? {
        return context.dataStore.data.map { prefs -> prefs[USER_ID] }.toString()
    }
    fun getUsuarioEmail(): String? {
        return context.dataStore.data.map { prefs -> prefs[USER_EMAIL] }.toString()
    }
    fun getUsuarioFoto(): Flow<String?> {
        return userFoto;
    }
    fun getUsuarioContrasena(): String? {
        return context.dataStore.data.map { prefs -> prefs[USER_CONTRASENA] }.toString()
    }
    fun getUsuarioAlias(): Flow<String?> {
        return userAlias
    }
    fun getUsuarioTelefono(): String? {
        return context.dataStore.data.map { prefs -> prefs[USER_TELEFONO] }.toString()
    }
    fun getUsuarioDireccion(): String? {
        return context.dataStore.data.map { prefs -> prefs[USER_DIRECCION] }.toString()
    }



    suspend fun clearUser() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}