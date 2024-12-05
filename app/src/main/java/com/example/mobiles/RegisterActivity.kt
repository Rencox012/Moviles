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
import com.example.mobiles.classes.RegisterRequest
import com.example.mobiles.utlidades.sweetAlert
import com.examples.mobiles.view.UsuariosViewModel

class RegisterActivity : ComponentActivity() {


    private val usuariosViewModel: UsuariosViewModel by viewModels {
        UsuariosViewModelFactory(applicationContext) // Pasamos el contexto de la aplicación
    }
    private val sweetalertManager = sweetAlert()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)


        val username_input = findViewById<EditText>(R.id.usuarioInput)
        val email_input = findViewById<EditText>(R.id.emailInput)
        val password_input = findViewById<EditText>(R.id.passwordInput)
        val validation_password_input = findViewById<EditText>(R.id.confirmPasswordInput)
        val alias_input = findViewById<EditText>(R.id.aliasInput)
        val telefono_input = findViewById<EditText>(R.id.telefonoInput)
        val direccion_input = findViewById<EditText>(R.id.direccionInput)
        val register_button = findViewById<Button>(R.id.registrarse_boton)

        val iniciarSesion = findViewById<TextView>(R.id.LoginTexto)

        // Observa los cambios en el usuario registrado
        usuariosViewModel.usuario.observe(this, Observer { usuario ->
            if (usuario != null) {
                sweetalertManager.dismissLoadingAlert()
                sweetalertManager.successAlert("Bienvenido ${usuario.nombre}", "Registro exitoso", this)
                // Redirige al login
                handleLogin()
            }
        })

        // Observa los errores
        usuariosViewModel.error.observe(this, Observer { error ->
            if (error != null) {
                sweetalertManager.dismissLoadingAlert()
                sweetalertManager.errorAlert(error, "Error", this)
            }
        })

        register_button.setOnClickListener {
            val username = username_input.text.toString()
            val email = email_input.text.toString()
            val password = password_input.text.toString()
            val validationPassword = validation_password_input.text.toString()
            val alias = alias_input.text.toString()
            val telefono = telefono_input.text.toString()
            val direccion = direccion_input.text.toString()


            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || validationPassword.isEmpty() || alias.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
                sweetalertManager.errorAlert("Por favor, llene todos los campos", "Error", this)
                return@setOnClickListener
            }
            if(password != validationPassword){
                sweetalertManager.errorAlert("Las contraseñas no coinciden", "Error", this)
                return@setOnClickListener
            }

            sweetalertManager.loadingAlert("Registrando usuario", "Espere por favor", this)

            handleRegistro(username, email, password, validationPassword, alias, telefono, direccion)
        }

        iniciarSesion.setOnClickListener {
            handleLogin()
        }



    }

    private fun handleRegistro(username: String, email: String, password: String, validationPassword: String
    , alias: String, telefono: String, direccion: String
    ) {

        //Si no hay conexión a internet, se muestra un mensaje de error.
        if (!isNetworkAvailable(this)) {
            sweetalertManager.dismissLoadingAlert()
            sweetalertManager.errorAlert("No hay conexión a internet", "Error", this)
            return
        }
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || validationPassword.isEmpty() || alias.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
            sweetalertManager.dismissLoadingAlert()
            sweetalertManager.errorAlert("Por favor, llene todos los campos", "Error", this)
            return
        }

        if (password != validationPassword) {
            sweetalertManager.dismissLoadingAlert()
            sweetalertManager.errorAlert("Las contraseñas no coinciden", "Error", this)
            return
        }

        val usuario = RegisterRequest(
            username,
            email,
            password,
            alias,
            direccion,
            telefono
        )

        usuariosViewModel.createUser(usuario)

    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun handleLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}

