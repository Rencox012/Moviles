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
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.mobiles.Room.DatabaseApp
import com.example.mobiles.classes.updateRequest
import com.example.mobiles.classes.updateWithPasswordRequest
import com.example.mobiles.utlidades.sweetAlert
import com.examples.mobiles.view.TransaccionesViewModel
import com.examples.mobiles.view.UsuariosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern

class PerfilActivity: ComponentActivity() {
    // Aquí se implementará la funcionalidad de la actividad de perfil
    // Se espera que el usuario pueda ver su información personal y modificarla

    private val transaccionesViewModel: TransaccionesViewModel by viewModels{
        TransaccionesModelFactory(applicationContext)
    }
    private val usuariosViewModel: UsuariosViewModel by viewModels {
        UsuariosViewModelFactory(applicationContext) // Pasamos el contexto de la aplicación
        // ;
    }

    private var userID = ""
    private val sweetalertManager = sweetAlert()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_usuario)

        usuariosViewModel.usuario.observe(this, Observer { usuario ->
            if (usuario != null) {
                sweetalertManager.dismissLoadingAlert()
                sweetalertManager.successAlert("Usuario actualizado correctamente", "Éxito", this)
                //Ahora, actualizamos la informacion del usuario en el DataStore
                CoroutineScope(Dispatchers.IO).launch {
                    usuariosViewModel.dataStoreManager.saveUserObject(usuario)
                }
            }
            else{
                sweetalertManager.dismissLoadingAlert()
                sweetalertManager.errorAlert("Error al actualizar el usuario", "Error", this)
            }
        })

        usuariosViewModel.error.observe(this, Observer { error ->
            if (error != null) {
                sweetalertManager.dismissLoadingAlert()
                sweetalertManager.errorAlert(error, "Error", this)
            }
        })

        cambiarAlias()

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_perfil)
        val menuButton = findViewById<ImageView>(R.id.menuButton)
        // Abre el menú lateral al hacer clic en el botón
        menuButton.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        val inicioBoton = findViewById<TextView>(R.id.inicio)
        inicioBoton.setOnClickListener {
            handleSendToInicio()
        }
        val historialBoton = findViewById<TextView>(R.id.historial)
        historialBoton.setOnClickListener {
            handleSendToHistory()
        }
        val logOffButton = findViewById<TextView>(R.id.logoutButton)
        logOffButton.setOnClickListener {
            logOutUser()
        }

        val actualizarBoton = findViewById<Button>(R.id.actualizarUsuario)
        actualizarBoton.setOnClickListener {
            handleUpdatePerfil()
        }

        val seleccionarFoto = findViewById<Button>(R.id.seleccionarFoto)
        seleccionarFoto.setOnClickListener {
            openImageSelector()
        }



        //Obtenemos la informacion del usuario
        obtainUserInfo()





    }
    fun obtainUserInfo(){
        val fotoPerfil = findViewById<ImageView>(R.id.logoIcon)
        val aliasUsuario = findViewById<EditText>(R.id.usernameInput)
        val nombreUsuario = findViewById<EditText>(R.id.nameInput)
        val correoUsuario = findViewById<EditText>(R.id.emailInput)
        val contrasenaUsuario = findViewById<EditText>(R.id.passwordInput)
        val confirmaContrasenaUsuario = findViewById<EditText>(R.id.confirmPasswordInput)
        val telefonoUsuario = findViewById<EditText>(R.id.phoneInput)
        val direccionUsuario = findViewById<EditText>(R.id.direccionInput)
        //Obtendremos la informacion del usuario en el DataStore
        CoroutineScope(Dispatchers.IO).launch {
            val user = usuariosViewModel.dataStoreManager.getUsuario().first()
            withContext(Dispatchers.Main){
                //Si el usuario no es nulo, llenamos los campos con la informacion
                if(user != null){
                    userID = user._id
                    //Llenamos los campos con la informacion del usuario
                    nombreUsuario.setText(user.nombre)
                    correoUsuario.setText(user.correo)
                    telefonoUsuario.setText(user.telefono)
                    direccionUsuario.setText(user.direccion)
                    aliasUsuario.setText(user.alias)
                    //Si la foto no es nula, la convertimos a Bitmap y la ponemos en el ImageView
                    if(user.imagen != ""){
                        fotoPerfil.setImageBitmap(base64toImage(user.imagen))
                        findViewById<ImageView>(R.id.profileButton).setImageBitmap(base64toImage(user.imagen))
                    }
                }
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
    private fun verificarContra(password: String) : Boolean {
        //Checamos que la contraseña tenga minimo 8 caracteres, una minuscula, una mayuscula, un numero y un caracter especial
        val pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}\$")
        val matcher = pattern.matcher(password)
        if (!matcher.matches()) {
            sweetalertManager.errorAlert("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un caracter especial", "Error", this)
            return false
        }
        return true
    }
    override fun onBackPressed() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun handleUpdatePerfil(){

        //El usuario no se puede actualizar sin conexión a internet
        if(!isNetworkAvailable(this)){
            sweetalertManager.errorAlert("No hay conexión a internet", "Error", this)
            return
        }
        sweetalertManager.loadingAlert("Actualizando usuario", "Cargando", this)

        //Empezamos obteniendo los valores de los campos
        val nombreUsuario = findViewById<EditText>(R.id.nameInput).text.toString()
        val correoUsuario = findViewById<EditText>(R.id.emailInput).text.toString()
        val telefonoUsuario = findViewById<EditText>(R.id.phoneInput).text.toString()
        val direccionUsuario = findViewById<EditText>(R.id.direccionInput).text.toString()
        val aliasUsuario = findViewById<EditText>(R.id.usernameInput).text.toString()
        val contrasenaUsuario = findViewById<EditText>(R.id.passwordInput).text.toString()
        val confirmaContrasenaUsuario = findViewById<EditText>(R.id.confirmPasswordInput).text.toString()
        val fotoPerfil = findViewById<ImageView>(R.id.logoIcon)
        var foto = "";

        if(fotoPerfil.drawable != null){
            //compimimos la imagen antes de convertirla a base64
            val fotoBitmap = (fotoPerfil.drawable as BitmapDrawable).bitmap
            val compressedImage = compressImage(fotoBitmap)
           foto = convertImageBitmapToBase64(compressedImage)
        }



        //Aqui dependera el tipo de request que haremos, si el usuario cambio la contraseña o no.
        //Si el usuario cambio la contraseña, haremos un request con la contraseña nueva
        if(contrasenaUsuario != "" && confirmaContrasenaUsuario != ""){
            //Si las contraseñas no coinciden, mostramos un mensaje de error
            if(contrasenaUsuario != confirmaContrasenaUsuario){
                if(!verificarContra(contrasenaUsuario)){
                    sweetalertManager.dismissLoadingAlert()
                    sweetalertManager.errorAlert("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un caracter especial", "Error", this)
                    return
                }
                sweetalertManager.dismissLoadingAlert()
                sweetalertManager.errorAlert("Las contraseñas no coinciden", "Error", this)
                return
            }
            //Creamos el objeto de request con la contraseña
            val request = updateWithPasswordRequest(
                nombreUsuario,
                correoUsuario,
                foto,
                contrasenaUsuario,
                aliasUsuario,
                direccionUsuario,
                telefonoUsuario
            )
            //Hacemos el request
            usuariosViewModel.updateUsuarioContrasena(userID, request)
        }
        else{
            //Si el usuario no cambio la contraseña, hacemos un request sin la contraseña
            val request = updateRequest(
                nombreUsuario,
                correoUsuario,
                foto,
                aliasUsuario,
                direccionUsuario,
                telefonoUsuario
            )
            //Hacemos el request
            usuariosViewModel.updateUsuario(userID, request)
        }
    }

    private fun compressImage(image: Bitmap): Bitmap {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 1, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
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
    private fun logOutUser(){
        //antes de cerrar sesion, le preguntaremos al usuario si esta seguro
        sweetalertManager.warningAlertWithAction("¿Estás seguro que deseas cerrar sesión?", "Cerrar sesión", this){
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
                val intent = Intent(this@PerfilActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun base64toImage(base64: String): Bitmap {
        val imageBytes = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
    private fun convertImageBitmapToBase64(image: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    private fun convertImageViewToBase64(imageView: ImageView): String {
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        var quality = 1
        var byteArray: ByteArray

        do {
            byteArrayOutputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, byteArrayOutputStream)
            byteArray = byteArrayOutputStream.toByteArray()
            quality -= 10
        } while (byteArray.size >= 0.025 * 1024 * 1024 && quality > 0)

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
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
            val fileSize = getFileSize(imageUri?: Uri.EMPTY)
            if (fileSize > 5 * 1024 * 1024) { // 5MB in bytes
                sweetalertManager.errorAlert("El archivo seleccionado es demasiado grande. Por favor, seleccione un archivo de menos de 5MB.", "Error", this)
            } else {
                val imagenInput = findViewById<ImageView>(R.id.logoIcon)
                imagenInput.setImageURI(imageUri)
            }
        }
    }
    private fun getFileSize(uri: Uri): Long {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return if (cursor != null && cursor.moveToFirst()) {
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            val size = cursor.getLong(sizeIndex)
            cursor.close()
            size
        } else {
            0
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}