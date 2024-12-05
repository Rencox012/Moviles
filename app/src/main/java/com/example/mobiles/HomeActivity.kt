package com.example.mobiles

import TransaccionesModelFactory
import UsuariosViewModelFactory
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiles.Room.DatabaseApp
import com.example.mobiles.Room.TransaccionEntity
import com.example.mobiles.adaptadores.AdaptadorImagenesHome
import com.example.mobiles.adaptadores.AdaptadorTransacciones
import com.example.mobiles.classes.CreateTransactionRequest
import com.example.mobiles.classes.Transaccion
import com.example.mobiles.utlidades.sweetAlert
import com.examples.mobiles.view.TransaccionesViewModel
import com.examples.mobiles.view.UsuariosViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Calendar

class HomeActivity : ComponentActivity() {

    private var userID = "";

    private val transaccionesViewModel: TransaccionesViewModel by viewModels{
        TransaccionesModelFactory(applicationContext)
    }
    private val usuariosViewModel: UsuariosViewModel by viewModels {
        UsuariosViewModelFactory(applicationContext) // Pasamos el contexto de la aplicación
    // ;
    }
    private lateinit var transaccionesAdapter: AdaptadorTransacciones

    private lateinit var imagesRecyclerView: RecyclerView
    private val selectedImages = mutableListOf<Uri>()
    private val sweetalertManager = sweetAlert()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContentView(R.layout.activity_home)
        // Obtén referencias a los componentes
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menuButton = findViewById<ImageView>(R.id.menuButton)
        val logOffButton = findViewById<TextView>(R.id.logoutButton)



        imagesRecyclerView = findViewById(R.id.images_recycler_view)
        imagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imagesRecyclerView.adapter = AdaptadorImagenesHome(selectedImages)

        val selectImagesButton: Button = findViewById(R.id.select_images_button)
        selectImagesButton.setOnClickListener {
            openImageSelector()
        }


        val historialBoton = findViewById<TextView>(R.id.historial)
        historialBoton.setOnClickListener {
            handleSendToHistory()
        }
        val perfilBoton = findViewById<TextView>(R.id.perfil)
        perfilBoton.setOnClickListener {
            handleSendToPerfil()
        }

        checkIfUserLogged()

        userID = intent.getStringExtra("idUser")!!

        sincronizarTransacciones()


        val recyclerView = findViewById<RecyclerView>(R.id.latest_movements_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val cantidadInput = findViewById<TextView>(R.id.amount_input)
        val descripcionInput = findViewById<TextView>(R.id.description_input)

        // Abre el menú lateral al hacer clic en el botón
        menuButton.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        val balanceUsuarioTexto = findViewById<TextView>(R.id.total_savings)
        val agregarIngresoBoton = findViewById<TextView>(R.id.income_button)
        val agregarGastoBoton = findViewById<TextView>(R.id.expense_button)


        //Funcion para evitar que el usuario pueda ingresar un monto negativo o letras
        cantidadInput.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    //No permitir letras ni caracteres especiales ni nada distinto a un numero
                    if (s.toString().matches(Regex(".*[^0-9.].*"))) {
                        cantidadInput.text = "0.0"
                        return
                    }
                    if (s.toString().isNotEmpty()) {
                        if (s.toString().toDouble() < 0) {
                            cantidadInput.text = null
                            sweetalertManager.errorAlert("No se permiten montos negativos", "Error", this@HomeActivity)
                        }
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // No se necesita implementar
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // No se necesita implementar
                }
            }
        )


        logOffButton.setOnClickListener {
            logOutUser()
        }


        transaccionesViewModel.message.observe(this, Observer { message ->
            if (message != null) {
                balanceUsuarioTexto.text = message
            }
        })

        transaccionesViewModel.transacciones.observe(this) { transacciones ->
            if (transacciones != null) {
                //insertamos las transacciones en la base de datos local
                lifecycleScope.launch(Dispatchers.IO) {
                    for (transaccion in transacciones) {

                        //antes de insertar, checamos si ya existe el ID remoto en la base ed datos
                        val existe = DatabaseApp.database.transaccionDao().obtenerTransaccionPorIdRemoto(transaccion._id)

                        if(existe != null){
                            continue
                        }

                        val transaccionEntity = Transaccion.toEntity(transaccion)

                        DatabaseApp.database.transaccionDao().insertarTransaccion(transaccionEntity)
                    }
                    //Actualizamos el balance del usuario
                    handleObtainBalance(intent.getStringExtra("idUser")!!, balanceUsuarioTexto)
                    //Actualizamos la lista de transacciones
                    handleObtainAllLocalTransactions()
                }
            }
        }


        agregarIngresoBoton.setOnClickListener {
            if(cantidadInput.text.toString().isEmpty()){
                sweetalertManager.errorAlert("El monto no puede estar vacío", "Error", this)
                return@setOnClickListener
            }
            if(descripcionInput.text.toString().isEmpty()){
                sweetalertManager.errorAlert("La descripción no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            //There must be at least one image selected
            if(selectedImages.isEmpty()){
                sweetalertManager.errorAlert("Debe seleccionar al menos una imagen", "Error", this)
                return@setOnClickListener
            }
            val cantidad = cantidadInput.text.toString().toDouble()
            val descripcion = descripcionInput.text.toString()
            if(cantidad <= 0){
                sweetalertManager.errorAlert("El monto debe ser mayor a 0", "Error", this)
                return@setOnClickListener
            }
            if(descripcion.isEmpty()){
                sweetalertManager.errorAlert("La descripción no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            //There has to be at least one image selected
            if(selectedImages.isEmpty()){
                sweetalertManager.errorAlert("Debe seleccionar al menos una imagen", "Error", this)
                return@setOnClickListener
            }
            handleAddTransaction(cantidad, descripcion, "ingreso", selectedImages)
        }

        agregarGastoBoton.setOnClickListener {
            if(cantidadInput.text.toString().isEmpty()){
                sweetalertManager.errorAlert("El monto no puede estar vacío", "Error", this)
                return@setOnClickListener
            }
            if(descripcionInput.text.toString().isEmpty()){
                sweetalertManager.errorAlert("La descripción no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            //There has to be at least one image selected
            if(selectedImages.isEmpty()){
                sweetalertManager.errorAlert("Debe seleccionar al menos una imagen", "Error", this)
                return@setOnClickListener
            }
            val cantidad = cantidadInput.text.toString().toDouble()
            val descripcion = descripcionInput.text.toString()
            if(cantidad <= 0){
                sweetalertManager.errorAlert("El monto debe ser mayor a 0", "Error", this)
                return@setOnClickListener
            }
            if(descripcion.isEmpty()){
                sweetalertManager.errorAlert("La descripción no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            //There has to be at least one image selected
            if(selectedImages.isEmpty()){
                sweetalertManager.errorAlert("Debe seleccionar al menos una imagen", "Error", this)
                return@setOnClickListener
            }
            handleAddTransaction(cantidad, descripcion, "gasto", selectedImages)
        }



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

    private fun handleObtainBalance(idUsuario: String, balanceUsuarioTexto: TextView)  {
        lifecycleScope.launch(Dispatchers.IO) {
            val resultadoIngresos = DatabaseApp.database.transaccionDao().obtenerTotalIngresos(idUsuario)
            val resultadoGastos = DatabaseApp.database.transaccionDao().obtenerTotalGastos(idUsuario)

            if(resultadoIngresos == null || resultadoGastos == null){
                sweetalertManager.errorAlert("Error al obtener el balance", "Error", this@HomeActivity)
                return@launch
            }
            val balance = resultadoIngresos - resultadoGastos
            updateTextView(balanceUsuarioTexto, "$$balance")
        }
    }


    private fun handleAddTransaction(ammount: Double, description: String, type: String, imagen: List<Uri>) {
        sweetalertManager.loadingAlert("Guardando transacción", "Guardando", this)

        lifecycleScope.launch(Dispatchers.IO) {
            val userid = usuariosViewModel.dataStoreManager.getUsuarioId().first()?:""
            if (userid != "") {
                // Obtenemos el día, mes y año actual
                val calendar = Calendar.getInstance()
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH) + 1 // Los meses empiezan desde 0
                val year = calendar.get(Calendar.YEAR)

                val fechaTransac = "$year-$month-$day"


                // Convert from uri to base64 string
                val imagenesString = imagen.map {
                    val inputStream = contentResolver.openInputStream(it)
                    val byteArray = inputStream?.readBytes()
                    val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
                    base64
                }

                // Convert the list of base64 strings to a JSON string
                val imagenJson = Gson().toJson(imagenesString)

                val transaccionEntity = TransaccionEntity(
                    idRemoto = null, // Se asignará después de la sincronización
                    monto = ammount,
                    tipo = type,
                    fechaTransac =fechaTransac,
                    descripcion = description,
                    usuarioId = userid,
                    imagen = imagenJson,
                    estado = 0, // No sincronizada
                    activo = 1
                )
                val result = DatabaseApp.database.transaccionDao().insertarTransaccion(transaccionEntity)
                if(result != -1L) {
                    //Actualizamos el balance del usuario
                    handleObtainBalance(userid, findViewById(R.id.total_savings))
                    CoroutineScope(Dispatchers.Main).launch {
                        sweetalertManager.dismissLoadingAlert()
                        sweetalertManager.successAlert("Transacción guardada", "Éxito", this@HomeActivity)
                    }
                    //actualizamos la lista de transacciones
                    handleObtainAllLocalTransactions()
                    //Limpiamos los campos
                    updateTextView(findViewById(R.id.amount_input), "")
                    updateTextView(findViewById(R.id.description_input), "")

                    sincronizarTransacciones()
                }
                else{
                    CoroutineScope(Dispatchers.Main).launch {
                        sweetalertManager.dismissLoadingAlert()
                        sweetalertManager.errorAlert("Error al guardar la transacción", "Error", this@HomeActivity)
                    }
                }
            }
        }

    }

    private fun updateTextView(textView: TextView, text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            textView.text = text
        }
    }

    private fun updateImageView(imageView: ImageView) {
        CoroutineScope(Dispatchers.Main).launch {
            //set source to empty
            imageView.setImageResource(0)
        }
    }



    private fun handleObtainAllTransactions(idUsuario: String){
        transaccionesViewModel.obtenerTransaccionesUsuario(idUsuario)
    }

    private fun handleObtainAllLocalTransactions(){
        lifecycleScope.launch(Dispatchers.IO) {
            val userid = usuariosViewModel.dataStoreManager.getUsuarioId().first()?:""
            userID = userid
            val transacciones = DatabaseApp.database.transaccionDao().obtenerTransaccionesPorUsuario(userid)
            if(transacciones.isEmpty()){
                return@launch
            }
            val transaccionesList = transacciones.map {
                Transaccion.fromEntity(it)
            }

            withContext(Dispatchers.Main){
                transaccionesAdapter = AdaptadorTransacciones(transaccionesList)
                findViewById<RecyclerView>(R.id.latest_movements_recyclerview).adapter = transaccionesAdapter
            }
            }
    }


    private fun convertImageViewToBase64(imageView: ImageView): String {
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        var quality = 100
        var byteArray: ByteArray

        do {
            byteArrayOutputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, byteArrayOutputStream)
            byteArray = byteArrayOutputStream.toByteArray()
            quality -= 10
        } while (byteArray.size >= 0.025 * 1024 * 1024 && quality > 0)

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    private fun sincronizarTransacciones() {
        if (!isNetworkAvailable(this)) {
            sweetalertManager.warningAlert("Las transacciones solo se guardaran locamente hasta que haya conexion.", "No hay conexion a internet", this)
            return
        }
        // Empezamos por obtener las transacciones no sincronizadas
        lifecycleScope.launch(Dispatchers.IO) {
            val userid = usuariosViewModel.dataStoreManager.getUsuarioId().first() ?: ""
            userID = userid
            val transaccionesNoSincronizadas = DatabaseApp.database.transaccionDao().obtenerTransaccionesNoSincronizadas()
            if (transaccionesNoSincronizadas.isEmpty()) {
                return@launch
            }
            // Obtenemos el id del usuario
            val idUsuario = userid
            if (idUsuario != "") {
                // Iteramos sobre las transacciones no sincronizadas
                for (transaccion in transaccionesNoSincronizadas) {
                    try {
                        // Si la transaccion ya tiene un idRemoto, quiere decir que fue creada, pero hay que actualizarla
                        if (transaccion.idRemoto != null && transaccion.idRemoto != "") {
                            val transaccionAEnviar = Transaccion.fromEntity(transaccion)
                            val transac = transaccionesViewModel.actualizarTransaccionSuspend(transaccion.idRemoto!!, transaccionAEnviar)
                            // Si hubo algun error le avisamos al usuario y cancelamos la sincronización
                            if (transac == null) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    sweetalertManager.errorAlert("Error al sincronizar la transacción", "Error", this@HomeActivity)
                                }
                                return@launch
                            }
                        } else {
                            if(transaccion.activo == 0){
                                continue
                            }
                            val transaccionEnviar = CreateTransactionRequest(
                                usuario_id = idUsuario,
                                monto = transaccion.monto,
                                tipo = transaccion.tipo,
                                descripcion = transaccion.descripcion ?: "",
                                fecha_transac = transaccion.fechaTransac,
                                imagen = Gson().fromJson(transaccion.imagen, object : com.google.gson.reflect.TypeToken<List<String>>() {}.type),
                                estado = 1,
                                activo = transaccion.activo
                            )
                            // Insertar transacción en el servidor usando la función suspend
                            val transac = transaccionesViewModel.insertarTransaccionSuspend(transaccionEnviar)
                            // Si no hubo algun error, tomamos el ID que nos regresa el servidor y lo guardamos en la base de datos local, usando el DAO
                            if (transac != null) {
                                DatabaseApp.database.transaccionDao().actualizarIdRemoto(transaccion.id, transac._id)
                            }
                        }
                    } catch (e: Exception) {
                        CoroutineScope(Dispatchers.Main).launch {
                            sweetalertManager.errorAlert("Error al sincronizar la transacción: ${e.message}", "Error", this@HomeActivity)
                        }
                    }
                }
            }
            // Si no hay errores, marcamos las transacciones como sincronizadas
            val ids = transaccionesNoSincronizadas.map { it.id }
            for (id in ids) {
                DatabaseApp.database.transaccionDao().marcarComoSincronizadas(listOf(id))
            }
            // Nuevamente, si no hay errores, actualizamos el balance del usuario
            handleObtainBalance(userid, findViewById(R.id.total_savings))
        }
    }

    private fun handleSendToHistory(userid: String = "") {
        val intent = Intent(this, HistorialActivity::class.java)
        intent.putExtra("idUser", userID)
        startActivity(intent)
        finish()
    }




    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    // Verifica si el usuario está guardado en DataStore
    private fun checkIfUserLogged() {
        CoroutineScope(Dispatchers.IO).launch {
            //Checamos si el usuario esta loggeado
           val isUserLogged = usuariosViewModel.dataStoreManager.getUserLoggedOn().first()
            if (!isUserLogged) {
                //Recogemos las transacciones del usuario
                if (userID != "") {
                    handleObtainAllTransactions(userID)
                    //una vez obtenidas las transacciones, obtenemos el balance del usuario y cargamos los elementos de la vista
                    handleObtainBalance(userID, findViewById(R.id.total_savings))
                    handleObtainAllLocalTransactions()

                    cambiarAlias()
                    cambiarFotoPerfil()
                }
            }
            else{
                val userid = usuariosViewModel.dataStoreManager.getUsuarioId().first()?:""
                userID = userid
                handleObtainBalance(userid, findViewById(R.id.total_savings))
                handleObtainAllLocalTransactions()

                cambiarAlias()
                cambiarFotoPerfil()
            }

        }
    }
    private fun handleSendToPerfil(){
        val intent = Intent(this, PerfilActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun logOutUser(){
        //antes de cerrar sesion, le preguntaremos al usuario si esta seguro
        sweetalertManager.warningAlertWithAction("¿Estás seguro que deseas cerrar sesión?", "Cerrar sesión", this@HomeActivity){
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
                val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        selectedImages.clear()
        selectedImages.addAll(uris)
        imagesRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun openImageSelector() {
        imagePickerLauncher.launch("image/*")
    }


    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }


}

