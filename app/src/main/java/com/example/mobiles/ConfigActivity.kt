
package com.example.mobiles

import android.app.Activity
import UsuariosViewModelFactory
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.mobiles.HomeActivity
import com.example.mobiles.LoginActivity
import com.example.mobiles.PerfilActivity
import com.example.mobiles.R
import com.example.mobiles.Room.DatabaseApp
import com.example.mobiles.utlidades.sweetAlert
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.examples.mobiles.view.UsuariosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConfigActivity :  ComponentActivity() {
    private val usuariosViewModel: UsuariosViewModel by viewModels {
        UsuariosViewModelFactory(applicationContext) // Pasamos el contexto de la aplicación
        // ;
    }

    private var userID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_config)

        // Obtenemos el ID del usuario
        //userID = intent.getStringExtra("idUser").toString()


        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_config)
        val menuButton = findViewById<ImageView>(R.id.menuButton)
        val inicioBoton = findViewById<TextView>(R.id.inicio)
        val logOffButton = findViewById<TextView>(R.id.logoutButton)
        val perfilBoton = findViewById<TextView>(R.id.perfil)
        val historyBoton = findViewById<TextView>(R.id.historial)

        perfilBoton.setOnClickListener {
            handleSendToPerfil()
        }

        cambiarFotoPerfil()
        cambiarAlias()







        inicioBoton.setOnClickListener {
            handleSendToInicio()
        }
        historyBoton.setOnClickListener {
            handleSendToHistory()
        }
        // Abre el menú lateral al hacer clic en el botón
        menuButton.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                drawerLayout.closeDrawer(GravityCompat.END)
            }
        }
        logOffButton.setOnClickListener {
            logOutUser()
        }

    }
    private fun logOutUser(){
        //antes de cerrar sesion, le preguntaremos al usuario si esta seguro
        sweetAlert().warningAlertWithAction("¿Estás seguro que deseas cerrar sesión?", "Cerrar sesión", this){
            //Si el usuario acepta, cerramos sesión
            handleLogOut()
        }
    }
    private fun handleLogOut(){
        //Borramos el usuario de DataStore, luego limpiaremos la base de datos local
        CoroutineScope(Dispatchers.IO).launch {
            usuariosViewModel.dataStoreManager.clearUser()
            DatabaseApp.database.transaccionDao().limpiarTransacciones()
            //Finalmente, redirigimos al LoginActivity
            withContext(Dispatchers.Main){
                val intent = Intent(this@ConfigActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun handleSendToPerfil(){
        val intent = Intent(this, PerfilActivity::class.java)
        startActivity(intent)
    }
    private fun handleSendToHistory(){
        val intent = Intent(this, HistorialActivity::class.java)
        intent.putExtra("idUser", userID)
        startActivity(intent)
    }
    private fun handleSendToInicio(){
        val intent = Intent(this, HomeActivity::class.java)
        //enviamos el ID del usuario
        intent.putExtra("idUser", userID)
        startActivity(intent)
    }

    fun cambiarFotoPerfil(){
        //Obtenemos la foto del dataStore y la convertimos a un bitmap
        lifecycleScope.launch {
            val foto = usuariosViewModel.dataStoreManager.userFoto.first()
            if(foto != null){
                val byteArray = Base64.decode(foto, Base64.DEFAULT)
                val bitmap = byteArray.inputStream().use { BitmapFactory.decodeStream(it) }
                findViewById<ImageView>(R.id.profileButton).setImageBitmap(bitmap)
            }
        }
    }
    fun cambiarAlias() {
        lifecycleScope.launch {
            // Obten el alias del usuario desde el DataStore
            val alias = usuariosViewModel.dataStoreManager.getUsuarioAlias().first()
            // Si el alias no es nulo, actualiza el TextView
            if (alias != null) {
                findViewById<TextView>(R.id.aliasUsuario).text = alias
            }
        }


    }
}