package com.example.mobiles

import UsuariosViewModelFactory
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.examples.mobiles.view.UsuariosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CargandoActivity : ComponentActivity()  {
    // ViewModel para manejar la lógica de usuarios
    private val usuariosViewModel: UsuariosViewModel by viewModels {
        UsuariosViewModelFactory(applicationContext) // Pasamos el contexto de la aplicación
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Verificar si ya hay un usuario guardado en DataStore
        checkIfUserIsLoggedIn()
    }
    // Verifica si el usuario está guardado en DataStore
    private fun checkIfUserIsLoggedIn() {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = usuariosViewModel.dataStoreManager.userId.first() // Obtén el ID guardado
            if (!userId.isNullOrEmpty()) {
                // Si hay un ID guardado, redirige al HomeActivity
                runOnUiThread {
                    handleSendHome(userId)
                }
            }
            else{
                // Si no hay un ID guardado, redirige al LoginActivity
                runOnUiThread {
                    handleSendLogin()
                }
            }
        }
    }
    private fun handleSendHome(idUser: String){
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("idUser", idUser)
        startActivity(intent)
        finish()
    }
    private fun handleSendLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}