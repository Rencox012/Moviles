package com.example.mobiles.adaptadores

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiles.R
import com.example.mobiles.classes.Transaccion

class AdaptadorTransacciones (private val transacciones: List<Transaccion>) : RecyclerView.Adapter<AdaptadorTransacciones.TransaccionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaccionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaccion_item, parent, false)
        return TransaccionViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: TransaccionViewHolder, position: Int) {
        val transaccion = transacciones[position]
        holder.bind(transaccion)
    }

    override fun getItemCount(): Int {
        return transacciones.size
    }

    class TransaccionViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val montoTextView: TextView = itemView.findViewById(R.id.amount)
        private val tipoTextView: TextView = itemView.findViewById(R.id.type)
        private val descripcionTextView: TextView = itemView.findViewById(R.id.description)
        private val fechaTextView: TextView = itemView.findViewById(R.id.date)
        private val imagenImageView = itemView.findViewById<ImageView>(R.id.Transaction_image)

        fun bind(transaccion: Transaccion) {
            montoTextView.text = "$${transaccion.monto}"
            descripcionTextView.text = transaccion.descripcion
            fechaTextView.text = transaccion.fecha_transac
            //la imagen esta en base64, se debe decodificar, solo si esta presente
            if(transaccion.imagen != ""){
                val decodedByte = decodeBase64(transaccion.imagen)
                imagenImageView.setImageBitmap(decodedByte)
            }

            // Configurar el texto y el color según el tipo
            tipoTextView.text = transaccion.tipo
            val colorRes = if (transaccion.tipo == "ingreso") R.color.verde else R.color.rojo
            tipoTextView.setBackgroundColor(ContextCompat.getColor(context, colorRes))
        }
        private fun decodeBase64(base64: String): Bitmap {
            val decodedString: ByteArray = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            return android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
    }


}