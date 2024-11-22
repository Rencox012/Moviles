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
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiles.Room.DatabaseApp
import com.example.mobiles.Room.TransaccionEntity
import com.example.mobiles.adaptadores.AdaptadorTransacciones
import com.example.mobiles.classes.CreateTransactionRequest
import com.example.mobiles.classes.Transaccion
import com.example.mobiles.utlidades.sweetAlert
import com.examples.mobiles.view.TransaccionesViewModel
import com.examples.mobiles.view.UsuariosViewModel
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




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)
        // Obtén referencias a los componentes
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menuButton = findViewById<ImageView>(R.id.menuButton)
        val logOffButton = findViewById<TextView>(R.id.logoutButton)

        val historialBoton = findViewById<TextView>(R.id.historial)
        historialBoton.setOnClickListener {
            handleSendToHistory()
        }
        val configBoton = findViewById<TextView>(R.id.configuracion)
        configBoton.setOnClickListener {
            handleSendToConfig()
        }
        val perfilBoton = findViewById<TextView>(R.id.perfil)
        perfilBoton.setOnClickListener {
            handleSendToPerfil()
        }

        checkIfUserLogged()

        userID = intent.getStringExtra("idUser")!!

        sincronizarTransacciones()

        cambiarAlias()
        cambiarFotoPerfil()

        val recyclerView = findViewById<RecyclerView>(R.id.latest_movements_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val cantidadInput = findViewById<TextView>(R.id.amount_input)
        val descripcionInput = findViewById<TextView>(R.id.description_input)
        val imagen_input = findViewById<ImageView>(R.id.image_input)
        imagen_input.setOnClickListener {
            openImageSelector()
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
                            sweetAlert().errorAlert("No se permiten montos negativos", "Error", this@HomeActivity)
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

        transaccionesViewModel.transacciones.observe(this) { transaccion ->
            if (transaccion != null) {
                //insertamos las transacciones en la base de datos local
                lifecycleScope.launch(Dispatchers.IO) {
                    for (transaccion in transaccion) {

                        //antes de insertar, checamos si ya existe el ID remoto en la base ed datos
                        val existe = DatabaseApp.database.transaccionDao().obtenerTransaccionPorIdRemoto(transaccion._id)

                        if(existe != null){
                            continue
                        }

                        val transaccionEntity = TransaccionEntity(
                            idRemoto = transaccion._id,
                            monto = transaccion.monto,
                            tipo = transaccion.tipo,
                            fechaTransac = transaccion.fecha_transac,
                            descripcion = transaccion.descripcion,
                            usuarioId = transaccion.usuario_id,
                            imagen = transaccion.imagen,
                            estado = 1
                        )

                        DatabaseApp.database.transaccionDao().insertarTransaccion(transaccionEntity)
                    }
                    //Actualizamos el balance del usuario
                    handleObtainBalance(intent.getStringExtra("idUser")!!, balanceUsuarioTexto)
                    //Actualizamos la lista de transacciones
                    handleObtainAllLocalTransactions()
                }
            }
        }

        //Cuando se carge la actividad, se obtiene el balance del usuario con el intent extra idUser
        val idUsuario = intent.getStringExtra("idUser")
        if (idUsuario != null) {
            handleObtainBalance(idUsuario, balanceUsuarioTexto)
            handleObtainAllLocalTransactions()
        }

        agregarIngresoBoton.setOnClickListener {
            if(cantidadInput.text.toString().isEmpty()){
                sweetAlert().errorAlert("El monto no puede estar vacío", "Error", this)
                return@setOnClickListener
            }
            if(descripcionInput.text.toString().isEmpty()){
                sweetAlert().errorAlert("La descripción no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            if(imagen_input.drawable == null){
                sweetAlert().errorAlert("La imagen no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            val cantidad = cantidadInput.text.toString().toDouble()
            val descripcion = descripcionInput.text.toString()
            val imagen = convertImageViewToBase64(imagen_input)
            if(cantidad <= 0){
                sweetAlert().errorAlert("El monto debe ser mayor a 0", "Error", this)
                return@setOnClickListener
            }
            if(descripcion.isEmpty()){
                sweetAlert().errorAlert("La descripción no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            if(imagen.isEmpty()){
                sweetAlert().errorAlert("La imagen no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            handleAddTransaction(cantidad, descripcion, "ingreso", imagen)
        }

        agregarGastoBoton.setOnClickListener {
            if(cantidadInput.text.toString().isEmpty()){
                sweetAlert().errorAlert("El monto no puede estar vacío", "Error", this)
                return@setOnClickListener
            }
            if(descripcionInput.text.toString().isEmpty()){
                sweetAlert().errorAlert("La descripción no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            if(imagen_input.drawable == null){
                sweetAlert().errorAlert("La imagen no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            val cantidad = cantidadInput.text.toString().toDouble()
            val descripcion = descripcionInput.text.toString()
            val imagen = convertImageViewToBase64(imagen_input)
            if(cantidad <= 0){
                sweetAlert().errorAlert("El monto debe ser mayor a 0", "Error", this)
                return@setOnClickListener
            }
            if(descripcion.isEmpty()){
                sweetAlert().errorAlert("La descripción no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            if(imagen.isEmpty()){
                sweetAlert().errorAlert("La imagen no puede estar vacía", "Error", this)
                return@setOnClickListener
            }
            handleAddTransaction(cantidad, descripcion, "gasto", imagen)
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
                sweetAlert().errorAlert("Error al obtener el balance", "Error", this@HomeActivity)
                return@launch
            }
            val balance = resultadoIngresos - resultadoGastos
            updateTextView(balanceUsuarioTexto, "$$balance")
        }
    }


    private fun handleAddTransaction(ammount: Double, description: String, type: String, imagen: String = "") {
        lifecycleScope.launch(Dispatchers.IO) {
            val idUsuario = intent.getStringExtra("idUser")
            if (idUsuario != null) {
                // Obtenemos el día, mes y año actual
                val calendar = Calendar.getInstance()
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH) + 1 // Los meses empiezan desde 0
                val year = calendar.get(Calendar.YEAR)

                val fechaTransac = "$year-$month-$day"

                val transaccionEntity = TransaccionEntity(
                    idRemoto = null, // Se asignará después de la sincronización
                    monto = ammount,
                    tipo = type,
                    fechaTransac =fechaTransac,
                    descripcion = description,
                    usuarioId = idUsuario,
                    imagen = imagen,
                    estado = 0 // No sincronizada
                )
                val result = DatabaseApp.database.transaccionDao().insertarTransaccion(transaccionEntity)
                if(result != -1L) {
                    //Actualizamos el balance del usuario
                    handleObtainBalance(idUsuario, findViewById(R.id.total_savings))
                    CoroutineScope(Dispatchers.Main).launch {
                        sweetAlert().successAlert("Transacción guardada", "Éxito", this@HomeActivity)
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
                        sweetAlert().errorAlert("Error al guardar la transacción", "Error", this@HomeActivity)
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
            val transacciones = DatabaseApp.database.transaccionDao().obtenerTransaccionesPorUsuario(intent.getStringExtra("idUser")!!)
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


    private fun sincronizarTransacciones(){
        if (!isNetworkAvailable(this)) {
            sweetAlert().warningAlert("Las transacciones solo se guardaran locamente hasta que haya conexion.", "No hay conexion a internet", this)
            return
        }
        //Empezamos por obtener las transacciones no sincronizadas
        lifecycleScope.launch(Dispatchers.IO) {
            val transaccionesNoSincronizadas =
                DatabaseApp.database.transaccionDao().obtenerTransaccionesNoSincronizadas()
            if (transaccionesNoSincronizadas.isEmpty()) {
                return@launch
            }
            //Obtenemos el id del usuario
            val idUsuario = intent.getStringExtra("idUser")
            if (idUsuario != null) {
                //Iteramos sobre las transacciones no sincronizadas
                for (transaccion in transaccionesNoSincronizadas) {
                    val transaccionEnviar = CreateTransactionRequest(
                        idUsuario,
                        transaccion.monto,
                        transaccion.tipo,
                        transaccion.descripcion ?: "",
                        transaccion.fechaTransac,
                        transaccion.imagen
                    )
                    transaccionesViewModel.insertarTransaccion(transaccionEnviar)
                }
            }
            //Si no hay errores, marcamos las transacciones como sincronizadas
            val ids = transaccionesNoSincronizadas.map { it.id }

            for (id in ids) {
                DatabaseApp.database.transaccionDao().marcarComoSincronizadas(listOf(id))
            }

            //Nuevamente, si no hay errores, actualizamos el balance del usuario
            handleObtainBalance(idUsuario!!, findViewById(R.id.total_savings))
        }
    }

    private fun handleSendToHistory(){
        val intent = Intent(this, HistorialActivity::class.java)
        intent.putExtra("idUser", userID)
        startActivity(intent)
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
           val isUserLogged = usuariosViewModel.dataStoreManager.userLoggedIn.first()
            if (isUserLogged) {
                //Recogemos las transacciones del usuario
                val idUsuario = intent.getStringExtra("idUser")
                if (idUsuario != null) {
                    handleObtainAllTransactions(idUsuario)
                    //una vez obtenidas las transacciones, obtenemos el balance del usuario y cargamos los elementos de la vista
                    handleObtainBalance(idUsuario, findViewById(R.id.total_savings))
                    handleObtainAllLocalTransactions()
                }
            }

        }
    }
    private fun handleSendToPerfil(){
        val intent = Intent(this, PerfilActivity::class.java)
        startActivity(intent)
    }
    private fun handleSendToConfig(){
        val intent = Intent(this, ConfigActivity::class.java)
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
                val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun openImageSelector() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val imageUri = data?.data
            val imagenInput = findViewById<ImageView>(R.id.image_input)
            imagenInput.setImageURI(imageUri)
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }


}

