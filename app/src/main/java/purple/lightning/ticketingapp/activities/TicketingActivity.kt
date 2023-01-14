package purple.lightning.ticketingapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import models.Show
import purple.lightning.ticketingapp.databinding.ActivityTicketingBinding
import reqs.ApiService
import adapters.GridShowAdapter
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import reqs.ServiceGenerator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import room.LoggedInUserEntity
import room.RoomApp
import java.io.File

@Suppress("DEPRECATION")
class TicketingActivity : AppCompatActivity(), GridShowAdapter.GridOnClickListener {
    private var binding: ActivityTicketingBinding? = null
    private lateinit var user: LoggedInUserEntity
    private var storage = FirebaseStorage.getInstance()
    private var bitmap: Bitmap? = null
    private lateinit var allShows:MutableList<Show>
    private var filteredList: MutableList<Show> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketingBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // FETCHING LOCAL USER FROM ROOM
        fetchLoggedInUser()
        val storageReference = storage.reference
        val ref = storageReference?.child("displayPictures/${user!!.username}.jpg")
        var filename = File.createTempFile("tempImg", "jpg")
        ref?.getFile(filename)
            ?.addOnSuccessListener{
                bitmap = BitmapFactory.decodeFile(filename.absolutePath)
                binding?.civLocalUserProfilePic?.setImageBitmap(bitmap)
            }
            ?.addOnFailureListener {
                Log.d("[CODE]:", "FAILED TO RETRIEVE IMAGE FROM FIREBASE")
            }

        val serviceGenerator = ServiceGenerator.buildService(ApiService::class.java)
        val call = serviceGenerator.getAllShows()
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        call.enqueue(object : Callback<MutableList<Show>>{
            override fun onResponse(
                call: Call<MutableList<Show>>,
                response: Response<MutableList<Show>>
            ) {
                if(response.isSuccessful) {
                    Log.d("[CODE]:", response.body().toString())
                    response.body().let {
                        setupShowsInGridView(it!!, progressDialog)
                        allShows = it!!
                    }
                }
                else {
                    Log.d("[CODE]:", "Failed to get")
                }
            }

            override fun onFailure(call: Call<MutableList<Show>>, t: Throwable) {
                t.printStackTrace()
                Log.d("[CODE]:",t.message.toString())
            }

        })

        binding?.btnComedy?.setOnClickListener {
            for(event in allShows){
                if(event.eventType == "Comedy Show"){
                    filteredList.add(event)
                }else{
                    continue
                }
            }
            setupShowsInGridView(filteredList, progressDialog)
            filteredList = mutableListOf()
        }

        binding?.btnMovie?.setOnClickListener {
            for(event in allShows){
                if(event.eventType == "Movie"){
                    filteredList.add(event)
                }else{
                    continue
                }
            }
            setupShowsInGridView(filteredList, progressDialog)
            filteredList = mutableListOf()
        }

        binding?.btnPlay?.setOnClickListener {
            for(event in allShows){
                if(event.eventType == "Play"){
                    filteredList.add(event)
                }else{
                    continue
                }
            }
            setupShowsInGridView(filteredList, progressDialog)
            filteredList = mutableListOf()
        }

        binding?.btnAll?.setOnClickListener {
            setupShowsInGridView(allShows, progressDialog)
        }

        binding?.civLocalUserProfilePic?.setOnClickListener {
            binding?.cvAccount?.visibility = View.VISIBLE
        }
        binding?.ibArrowDown?.setOnClickListener {
            binding?.cvAccount?.visibility = View.GONE
        }
        binding?.tvLogout?.setOnClickListener {
            val loggedInUsersDao = (application as RoomApp).db.loggedInUsersDao()
            lifecycleScope.launch {
                user = loggedInUsersDao.fetchUserByLogStatus(true)
                loggedInUsersDao.updateLoggedInStatus(user.username, false)
            }
            finishAffinity()
            val intent = Intent(this@TicketingActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding?.tvHistory?.setOnClickListener {
            var bundle = Bundle()
            bundle.putString("username", user.username)
            val intent = Intent(this@TicketingActivity, HistoryActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }

    }
    private fun fetchLoggedInUser(){
        val loggedInUsersDao = (application as RoomApp).db.loggedInUsersDao()
        user = loggedInUsersDao.fetchUserByLogStatus(true)
    }

    private fun setupShowsInGridView(shows: MutableList<Show>, progressDialog:ProgressDialog){
        binding?.rvGridShows?.layoutManager = GridLayoutManager(applicationContext,
            2,LinearLayoutManager.VERTICAL, false)
        var rvGridShowAdapter = GridShowAdapter(shows, this, user.username)
        binding?.rvGridShows?.adapter = rvGridShowAdapter
        progressDialog.dismiss()

    }

    override fun GridonClickListener(position: Int) {
        val show = allShows[position]
        var bundle = Bundle()
        bundle.putString("SelectedEventName", show.eventName)
        bundle.putString("username", user.username)
        val intent = Intent(this@TicketingActivity, VenuesActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

}