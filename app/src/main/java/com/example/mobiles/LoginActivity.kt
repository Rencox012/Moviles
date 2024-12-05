package com.example.mobiles

import UsuariosViewModelFactory
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.mobiles.utlidades.sweetAlert
import com.examples.mobiles.view.UsuariosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    // ViewModel para manejar la lógica de usuarios
    private val usuariosViewModel: UsuariosViewModel by viewModels {
        UsuariosViewModelFactory(applicationContext) // Pasamos el contexto de la aplicación
    }
    private var sweetalertManager = sweetAlert()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Verificar si ya hay un usuario guardado en DataStore
        checkIfUserIsLoggedIn()

        setContentView(R.layout.activity_login)


        val email_input = findViewById<EditText>(R.id.email_input)
        val password_input = findViewById<EditText>(R.id.password_input)
        val login_button = findViewById<Button>(R.id.iniciar_sesion_button)
        val registerButton = findViewById<TextView>(R.id.register_here)

        // Observa los cambios en el usuario registrado
        usuariosViewModel.usuario.observe(this, Observer { usuario ->
            if (usuario != null) {
                sweetalertManager.dismissLoadingAlert()
                sweetalertManager.successAlert("Bienvenido ${usuario.nombre}", "Inicio de sesión exitoso", this)
                //enviamos al home
                handleSendHome(usuario._id)
            }
            else{
                sweetalertManager.dismissLoadingAlert()
                sweetalertManager.errorAlert("Error al iniciar sesión", "Error", this)
            }
        })

        // Observa los errores
        usuariosViewModel.error.observe(this, Observer { error ->
            if (error != null) {
            sweetalertManager.dismissLoadingAlert()
               sweetalertManager.errorAlert(error, "Error", this)
            }
        })


        login_button.setOnClickListener(){
            val email = email_input.text.toString()
            val password = password_input.text.toString()

            handleLogin(email, password)
        }

        registerButton.setOnClickListener(){
            handleRegister()
        }
    }
    private fun handleLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            sweetalertManager.errorAlert("Por favor, llena todos los campos", "Error", this)
            return
        }
        if(!isNetworkAvailable(this)){
            sweetalertManager.errorAlert("No hay conexión a internet", "Error", this)
            return
        }

        sweetalertManager.loadingAlert("Iniciando sesión", "Cargando", this)


        // Aquí llamaremos al ViewModel para realizar el inicio de sesión
        usuariosViewModel.iniciarSesion(email, password)

    }

    private fun handleRegister(){
        // Aquí redirigiremos a la actividad de registro
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
    private fun handleSendHome(idUser: String){
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("idUser", idUser)
        startActivity(intent)
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
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }



}

