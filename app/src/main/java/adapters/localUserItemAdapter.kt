package adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import purple.lightning.ticketingapp.databinding.LocaluserRvItemCustomRowBinding
import room.LoggedInUserEntity
import java.io.File

class LocalUserItemAdapter(private var list: List<LoggedInUserEntity>, private val onUserClickListener: OnUserClickListener) :
    RecyclerView.Adapter<LocalUserItemAdapter.ViewHolder>() {

    private var storage = FirebaseStorage.getInstance()
    private lateinit var bitmap: Bitmap

    class ViewHolder(binding: LocaluserRvItemCustomRowBinding): RecyclerView.ViewHolder(binding.root){
        // val cvLocalUser = binding.cvLocalUser
        val tvlocalUserName = binding.tvlocalUserName
        val civLocalUserProfilePic = binding.civLocalUserProfilePic
        val btnLocalUserLogin = binding.btnLocalUserLogIn
    }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LocaluserRvItemCustomRowBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        val storageReference = storage.reference
        val ref = storageReference?.child("images/${model.username}.jpg")

        var filename = File.createTempFile("tempImg", "jpg")
        ref?.getFile(filename)
            ?.addOnSuccessListener{
                bitmap = BitmapFactory.decodeFile(filename.absolutePath)
                holder.civLocalUserProfilePic.setImageBitmap(bitmap)
            }

        holder.tvlocalUserName.text = model.username
        holder.btnLocalUserLogin.setOnClickListener {
            onUserClickListener.onUserClickListener(position, model.username!!)

        }

    }


    interface OnUserClickListener{
        fun onUserClickListener(position: Int, username:String)
    }

}