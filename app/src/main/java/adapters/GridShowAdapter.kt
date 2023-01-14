package adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import models.Show
import purple.lightning.ticketingapp.databinding.GridItemBinding
import java.io.File

class GridShowAdapter(private var list: MutableList<Show>, private val GridonClickListener: GridOnClickListener, private val username:String):
RecyclerView.Adapter<GridShowAdapter.ViewHolder>(){
    private var storage = FirebaseStorage.getInstance()
    private val storageReference = storage.reference
    private lateinit var bitmap: Bitmap

    class ViewHolder(binding: GridItemBinding):RecyclerView.ViewHolder(binding.root){
        var ibShow = binding.ibShow
        var tvShowName = binding.tvShowName
        var clShow = binding.clShow
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            GridItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val show = list[position]
        holder.tvShowName.text = show.eventName
        val ref = storageReference?.child("images/${show.eventImg}")
        var temp = show.eventImg.split(".")
        var filename = File.createTempFile("tempImg", "${temp[1]}")
        ref?.getFile(filename)
            ?.addOnSuccessListener {
                bitmap = BitmapFactory.decodeFile(filename.absolutePath)
                holder.ibShow.setImageBitmap(bitmap)
            }
        holder.clShow.setOnClickListener {
            GridonClickListener.GridonClickListener(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface GridOnClickListener{
        fun GridonClickListener(position: Int)
    }

}