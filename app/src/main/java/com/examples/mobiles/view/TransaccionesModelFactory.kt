
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobiles.Room.TransaccionDAO
import com.examples.mobiles.view.TransaccionesViewModel

class TransaccionesModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransaccionesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransaccionesViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
