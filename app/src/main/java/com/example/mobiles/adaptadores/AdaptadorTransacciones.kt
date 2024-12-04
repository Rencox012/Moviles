package com.example.mobiles.adaptadores

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiles.EditarActivity
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
        private val imagesRecyclerView: RecyclerView = itemView.findViewById(R.id.images_recycler_view)
        private val editarButton: Button = itemView.findViewById(R.id.edit_button) // Botón Editar


        fun bind(transaccion: Transaccion) {
            montoTextView.text = "$${transaccion.monto}"
            descripcionTextView.text = transaccion.descripcion
            fechaTextView.text = transaccion.fecha_transac

            // Set up the RecyclerView for images
            imagesRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            imagesRecyclerView.adapter = ImagesAdapter(transaccion.imagen)

            // Set the text and color based on the type
            tipoTextView.text = transaccion.tipo
            val colorRes = if (transaccion.tipo == "ingreso") R.color.verde else R.color.rojo
            tipoTextView.setBackgroundColor(ContextCompat.getColor(context, colorRes))

            //Set the edit button
            editarButton.setOnClickListener{
                val intent = Intent(context, EditarActivity::class.java)
                intent.putExtra("TRANSACCION_ID", transaccion.idLocal.toString())
                context.startActivity(intent)
            }
        }
        private fun decodeBase64(base64: String): Bitmap {
            val decodedString: ByteArray = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            return android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
    }


}