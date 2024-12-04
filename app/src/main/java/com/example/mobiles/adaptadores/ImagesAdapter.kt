package com.example.mobiles.adaptadores

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiles.R

class ImagesAdapter(private val images: List<String>) : RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val base64Image = images[position]
        holder.bind(base64Image)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image_view)

        fun bind(base64Image: String) {
            val bitmap = decodeBase64(base64Image)
            imageView.setImageBitmap(bitmap)
        }

        private fun decodeBase64(base64: String): Bitmap {
            val decodedString: ByteArray = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            return android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
    }
}