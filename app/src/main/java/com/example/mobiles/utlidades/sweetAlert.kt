package com.example.mobiles.utlidades

import android.content.Context
import taimoor.sultani.sweetalert2.Sweetalert


//Clase que permitira crear alertas con sweet alert.

class sweetAlert {
    //Metodo que permite crear una alerta de tipo error.
    fun errorAlert(msg: String, title: String, context: Context){
    Sweetalert(context, Sweetalert.ERROR_TYPE)
        .setTitleText(title)
        .setContentText(msg)
        .show()
    }

    fun titleAlert(msg: String, title: String, context: Context){
        Sweetalert(context, Sweetalert.SUCCESS_TYPE)
        .setTitleText(title)
        .setContentText(msg)
        .show()
    }

    fun warningAlert(msg: String, title: String, context: Context){
        Sweetalert(context, Sweetalert.WARNING_TYPE)
        .setTitleText(title)
        .setContentText(msg)
        .setConfirmText("Aceptar")
        .show()
    }

    fun successAlert(msg: String, title: String, context: Context){
        Sweetalert(context, Sweetalert.SUCCESS_TYPE)
        .setTitleText(title)
        .setContentText(msg)
        .show()
    }

    //Metodo que permite crear una alerta de advertencia y asignarle una accion al boton de aceptar.
    fun warningAlertWithAction(msg: String, title: String, context: Context, action: () -> Unit){
        Sweetalert(context, Sweetalert.WARNING_TYPE)
            .setTitleText(title)
            .setContentText(msg)
            .setConfirmText("Yes,delete it!")
            .setConfirmClickListener { sDialog -> sDialog.dismissWithAnimation() }
            .setCancelButton(
                "Salir"
            ) { action() }
            .show()
    }
}