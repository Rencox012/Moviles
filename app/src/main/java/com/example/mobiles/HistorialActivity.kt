package com.example.mobiles

import TransaccionesModelFactory
import UsuariosViewModelFactory
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiles.Room.DatabaseApp
import com.example.mobiles.adaptadores.AdaptadorTransacciones
import com.example.mobiles.classes.Transaccion
import com.example.mobiles.utlidades.sweetAlert
import com.examples.mobiles.view.TransaccionesViewModel
import com.examples.mobiles.view.UsuariosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class HistorialActivity: ComponentActivity() {
    private val transaccionesViewModel: TransaccionesViewModel by viewModels{
        TransaccionesModelFactory(applicationContext)
    }
    private val usuariosViewModel: UsuariosViewModel by viewModels {
        UsuariosViewModelFactory(applicationContext) // Pasamos el contexto de la aplicación
        // ;
    }
    private lateinit var transaccionesAdapter: AdaptadorTransacciones

    private var userID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_historial)

        // Obtenemos el ID del usuario
        userID = intent.getStringExtra("idUser").toString()


        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_historial)
        val menuButton = findViewById<ImageView>(R.id.menuButton)
        val inicioBoton = findViewById<TextView>(R.id.inicio)
        val logOffButton = findViewById<TextView>(R.id.logoutButton)
        val perfilBoton = findViewById<TextView>(R.id.perfil)
        perfilBoton.setOnClickListener {
            handleSendToPerfil()
        }

        cambiarFotoPerfil()
        cambiarAlias()


        val searchButton = findViewById<Button>(R.id.searchButton)


        searchButton.setOnClickListener {
            searchTransactions()
        }

        inicioBoton.setOnClickListener {
            handleSendToInicio()
        }
        // Abre el menú lateral al hacer clic en el botón
        menuButton.setOnClickListener {
            if(!drawerLayout.isDrawerOpen(GravityCompat.START)){
                drawerLayout.openDrawer(GravityCompat.START)
            }
            else{
                drawerLayout.closeDrawer(GravityCompat.END)
            }
        }
        logOffButton.setOnClickListener {
            logOutUser()
        }



        handleObtainAllLocalTransactions()





    }

    fun searchTransactions() {
        val searchInput = findViewById<EditText>(R.id.searchEditText)
        val montoMinimoInput = findViewById<EditText>(R.id.minPriceEditText)
        val montoMaximoInput = findViewById<EditText>(R.id.maxPriceEditText)
        val busqueda = searchInput.text?.toString() ?: null
        val montoMinimo = montoMinimoInput.text.toString().toDoubleOrNull()
        val montoMaximo = montoMaximoInput.text.toString().toDoubleOrNull()

        lifecycleScope.launch(Dispatchers.IO) {

            val transacciones = DatabaseApp.database.transaccionDao().buscarTransacciones(userID, busqueda, montoMinimo, montoMaximo)
            if(transacciones.isEmpty()){
                return@launch
            }
            val transaccionesList = transacciones.map {
                Transaccion(
                    it.id.toString(),
                    it.monto,
                    it.tipo,
                    it.imagen?:"",
                    it.fechaTransac,
                    it.descripcion?:"",
                    it.usuarioId,
                    it.estado
                )
            }

            withContext(Dispatchers.Main){
                val recyclerView = findViewById<RecyclerView>(R.id.transactionsRecyclerView)
                recyclerView.layoutManager = LinearLayoutManager(this@HistorialActivity)
                transaccionesAdapter = AdaptadorTransacciones(transaccionesList)
                recyclerView.adapter = transaccionesAdapter
            }
        }


    }
    private fun handleSendToPerfil(){
        val intent = Intent(this, PerfilActivity::class.java)
        startActivity(intent)
    }

    fun setupDateInputFormatter(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val yyyymmdd = "YYYY/MM/DD"
            private val cal = Calendar.getInstance()

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    val userInput = s.toString().replace(Regex("[^\\d]"), "")
                    val length = userInput.length

                    if (length <= 10) {
                        var formatted = ""
                        var index = 0
                        for (i in yyyymmdd.indices) {
                            if (index >= length) break
                            formatted += if (i == 4 || i == 7) "/" else userInput[index++]
                        }

                        current = formatted
                        editText.setText(current)
                        editText.setSelection(current.length)
                    } else {
                        s?.delete(length - 1, length)
                    }
                }
            }
        })
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
    fun formatDateString(dateString: String): String {
        // Asumimos que el usuario ingresa la fecha en el formato "dd/MM/yyyy"
        val parts = dateString.split("/")
        if (parts.size != 3) {
            throw IllegalArgumentException("Fecha inválida. El formato debe ser dd/MM/yyyy")
        }
        val day = parts[0].padStart(2, '0')
        val month = parts[1].padStart(2, '0')
        val year = parts[2]
        return "$year-$month-$day T00:00:00.000Z"
    }
    // Opción para cerrar el menú lateral si el usuario presiona el botón de retroceso
    override fun onBackPressed() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun handleSendToInicio(){
        val intent = Intent(this, HomeActivity::class.java)
        //enviamos el ID del usuario
        intent.putExtra("idUser", userID)
        startActivity(intent)
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
                val intent = Intent(this@HistorialActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun handleObtainAllLocalTransactions(){
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = intent.getStringExtra("idUser")
            if (userId.isNullOrEmpty()) {
                // Manejar el caso en que el idUser sea nulo o vacío
                return@launch
            }
            val transacciones = DatabaseApp.database.transaccionDao().obtenerTransaccionesPorUsuario(userId)
            if(transacciones.isEmpty()){
                return@launch
            }
            val transaccionesList = transacciones.map {
                Transaccion(
                    it.id.toString(),
                    it.monto,
                    it.tipo,
                    it.imagen?:"",
                    it.fechaTransac,
                    it.descripcion?:"",
                    it.usuarioId,
                    it.estado
                )
            }

            withContext(Dispatchers.Main){
                val recyclerView = findViewById<RecyclerView>(R.id.transactionsRecyclerView)
                recyclerView.layoutManager = LinearLayoutManager(this@HistorialActivity)
                transaccionesAdapter = AdaptadorTransacciones(transaccionesList)
                recyclerView.adapter = transaccionesAdapter
            }
        }
    }



}