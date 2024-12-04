package com.example.mobiles

import UsuariosViewModelFactory
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiles.Room.DatabaseApp
import com.example.mobiles.adaptadores.ImagesAdapter
import com.example.mobiles.utlidades.sweetAlert
import com.examples.mobiles.view.UsuariosViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditarActivity : ComponentActivity() {


    private var userID = ""
    private var transaccionID = ""

    private lateinit var imagesRecyclerView: RecyclerView
    private val selectedImages = mutableListOf<String>()
    private lateinit var imagesAdapter: ImagesAdapter
    private val usuariosViewModel: UsuariosViewModel by viewModels {
        UsuariosViewModelFactory(applicationContext) // Pasamos el contexto de la aplicación
        // ;
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transac)

        //region Funciones de botones para guardar o eliminar la transacción
        val boton_ingreso = findViewById<Button>(R.id.income_button)
        val boton_gasto = findViewById<Button>(R.id.expense_button)
        val boton_borrar = findViewById<Button>(R.id.delete_button)

        boton_borrar.setOnClickListener {
            eliminarTransaccion()
        }

        //obtenemos el id de la transaccion
        transaccionID = intent.getStringExtra("TRANSACCION_ID").toString()
        obtenerUserID()

        //region Funciones de la barra lateral
        // Obtén referencias a los componentes
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_config)
        val menuButton = findViewById<ImageView>(R.id.menuButton)
        val logOffButton = findViewById<TextView>(R.id.logoutButton)
        val inicioBoton = findViewById<TextView>(R.id.inicio)

        // Abre el menú lateral al hacer clic en el botón
        menuButton.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        logOffButton.setOnClickListener {
            logOutUser()
        }
        inicioBoton.setOnClickListener {
            handleSendToInicio()
        }



        val historialBoton = findViewById<TextView>(R.id.historial)
        historialBoton.setOnClickListener {
            handleSendToHistory()
        }
        val perfilBoton = findViewById<TextView>(R.id.perfil)
        perfilBoton.setOnClickListener {
            handleSendToPerfil()
        }

        //endregion

        // Initialize RecyclerView and Adapter
        imagesRecyclerView = findViewById(R.id.images_recycler_view)
        imagesAdapter = ImagesAdapter(selectedImages)
        imagesRecyclerView.adapter = imagesAdapter
        imagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Obtén la información de la transacción
        obtenerInformacionTransaccion(transaccionID)

        findViewById<Button>(R.id.add_image_button).setOnClickListener {
            openImageSelector()
        }

        cambiarFotoPerfil()
        cambiarAlias()


    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        selectedImages.clear()
        //transformamos las uris a base64
        val base64Images = uris.map { uri ->
            val inputStream = contentResolver.openInputStream(uri)
            val byteArray = inputStream?.readBytes()
            byteArray?.let { Base64.encodeToString(it, Base64.DEFAULT) } ?: ""
        }
        selectedImages.addAll(base64Images)
        imagesAdapter.notifyDataSetChanged()
    }

    private fun openImageSelector() {
        imagePickerLauncher.launch("image/*")
    }

    //region Funciones para actualizar la interfaz con foto y alias
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
    //endregion

    fun obtenerUserID(){
        lifecycleScope.launch {
            val userid = usuariosViewModel.dataStoreManager.getUsuarioId().first()?:""
            userID = userid
        }
    }



    //region Funciones para enviar al usuario a otras actividades
    private fun handleSendToHistory() {
        val intent = Intent(this, HistorialActivity::class.java)
        intent.putExtra("idUser", userID)
        startActivity(intent)
        finish()
    }
    private fun handleSendToPerfil(){
        val intent = Intent(this, PerfilActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun handleSendToInicio(){
        val intent = Intent(this, HomeActivity::class.java)
        //enviamos el ID del usuario
        intent.putExtra("idUser", userID)
        startActivity(intent)
        finish()
    }

    //endregion

    //region Funciones para cerrar sesión
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
                val intent = Intent(this@EditarActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    //endregion

    //region Funciones para manejar la transaccion
    private fun obtenerInformacionTransaccion(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val transaccion = DatabaseApp.database.transaccionDao().obtenerTransaccionPorId(id.toInt())
            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.amount_input).text = transaccion.monto.toString()
                findViewById<TextView>(R.id.description_input).text = transaccion.descripcion

                // Parsear las imágenes
                val imagenesList: List<String> = Gson().fromJson(
                    transaccion.imagen,
                    object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                )


                // Actualizar la lista y el adaptador
                selectedImages.clear()
                selectedImages.addAll(imagenesList)
                imagesAdapter.notifyDataSetChanged() // Notificar al adaptador de los cambios
            }
        }
    }
    private fun eliminarTransaccion() {
        sweetAlert().warningAlertWithAction("¿Estás seguro que deseas eliminar esta transacción?", "Eliminar transacción", this) {
            // Si el usuario acepta, eliminamos la transacción
            handleEliminar()
        }
    }

    private fun handleEliminar(){
        //Funcion que se enviara al sweetAlert
        CoroutineScope(Dispatchers.IO).launch {
            //Usamos el DAO para borrar logicamente la transaccion
            DatabaseApp.database.transaccionDao().borrarTransaccion(transaccionID.toInt())
            //alertamos al usuario que la transaccion fue eliminada, y lo redirigimos al historial
            withContext(Dispatchers.Main){
                sweetAlert().successAlert("Transacción eliminada", "Transacción eliminada", this@EditarActivity)
                handleSendToInicio()
            }
        }

    }


    //endregion
}
