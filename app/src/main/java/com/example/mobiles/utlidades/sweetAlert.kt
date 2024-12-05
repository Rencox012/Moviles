package com.example.mobiles.utlidades

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import taimoor.sultani.sweetalert2.Sweetalert


//Clase que permitira crear alertas con sweet alert.

class sweetAlert {
    private var loadingDialog: Sweetalert? = null


    //Metodo que permite crear una alerta de tipo error.
    fun errorAlert(msg: String, title: String, context: Context){
        val alert = Sweetalert(context, Sweetalert.ERROR_TYPE)
        alert.setTitleText(title)
        alert.setContentText(msg)
        alert.show()

        Handler(Looper.getMainLooper()).postDelayed({
            alert.dismissWithAnimation()
        }, 2000)

    }

    fun loadingAlert(msg: String, title: String, context: Context) {
        loadingDialog = Sweetalert(context, Sweetalert.PROGRESS_TYPE)
        loadingDialog?.progressHelper?.barColor = Color.parseColor("#A5DC86")
        loadingDialog?.setTitleText(title)
        loadingDialog?.setContentText(msg)
        loadingDialog?.setCancelable(false)
        loadingDialog?.show()
    }

    fun dismissLoadingAlert() {
        loadingDialog?.dismissWithAnimation()
    }

    fun titleAlert(msg: String, title: String, context: Context){
        Sweetalert(context, Sweetalert.SUCCESS_TYPE)
        .setTitleText(title)
        .setContentText(msg)
        .show()
    }

    fun warningAlert(msg: String, title: String, context: Context){
        val dialog = Sweetalert(context, Sweetalert.WARNING_TYPE)
        dialog.setTitleText(title)
        dialog.setContentText(msg)
        dialog.setConfirmText("Aceptar")
        dialog.show()
    }

    fun successAlert(msg: String, title: String, context: Context){
        val dialog = Sweetalert(context, Sweetalert.SUCCESS_TYPE)
        dialog.setTitleText(title)
        dialog.setContentText(msg)
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismissWithAnimation()
        }, 2000)
    }

    //Metodo que permite crear una alerta de advertencia y asignarle una accion al boton de aceptar.
    fun warningAlertWithAction(msg: String, title: String, context: Context, action: () -> Unit){
        Sweetalert(context, Sweetalert.WARNING_TYPE)
            .setTitleText(title)
            .setContentText(msg)
            .setConfirmClickListener { sDialog -> sDialog.dismissWithAnimation() }
            .setCancelButton(
                "Aceptar"
            ) { action() }
            .setConfirmButton("Aceptar") { action() }
            .show()
    }
}