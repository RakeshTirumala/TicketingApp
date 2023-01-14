package purple.lightning.ticketingapp.activities

import adapters.LinearHistoryAdapter
import adapters.LinearVenuesAdapter
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import purple.lightning.ticketingapp.databinding.ActivityHistoryBinding

@Suppress("DEPRECATION")
class HistoryActivity : AppCompatActivity(), LinearHistoryAdapter.LinearOnClickListener {
    private var binding:ActivityHistoryBinding? = null
    private var database = FirebaseDatabase
        .getInstance("https://ticketting-afc73-default-rtdb.firebaseio.com/")
    private var username:String? = null
    private var dateEventVenue: MutableSet<String> = mutableSetOf()
    private var dateEventVenueList: MutableList<String> = mutableListOf()
    private var data: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val bundle = intent.extras
        username = bundle?.getString("username").toString()
        Log.d("[CODE]:", "USERNAME: ${username!!}")

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        database.getReference("Users")
            .child(username!!)
            .child("booked")
            .get()
            .addOnSuccessListener {
                //SETTING UP LINEAR RV
                if(it.value != null){
                    Log.d("[CODE]:", "FETCHED DATA OF ${username!!}: ${it.value}")
                    data = it.value as MutableMap<String, MutableMap<String, String>>
                    dateEventVenue = data.keys

                    for(ele in dateEventVenue){
                        dateEventVenueList.add(ele)
                    }
                }
                binding?.rvLinearHistory?.layoutManager = LinearLayoutManager(this@HistoryActivity)
                var rvLinearHistoryAdapter = LinearHistoryAdapter(dateEventVenueList, data, username!!,this)
                binding?.rvLinearHistory?.adapter = rvLinearHistoryAdapter
                progressDialog.dismiss()

            }.addOnFailureListener {
                Log.d("[CODE]:", "FAILED TO FETCH!!!")
            }

    }

    override fun LinearonClickListener(dateEventVenue:String, temp: List<String>, bookedSeatsByUser:MutableSet<String>) {
        database.getReference("Users")
            .child(username!!)
            .child("booked")
            .child(dateEventVenue)
            .removeValue()
            .addOnSuccessListener {
                Log.d("[CODE]:", "SUCCESSFULLY DELETED!!")
            }.addOnFailureListener {
                Log.d("[CODE]:", "FAILED TO DELETE!!")
            }
        var mapOfSeatsBooked:MutableMap<String, String>
        database.getReference("Events")
            .child(temp[1])
            .child(temp[2])
            .child(temp[0])
            .get()
            .addOnSuccessListener {
                mapOfSeatsBooked = it.value as MutableMap<String, String>
                Log.d("[CODE]:", "MAP: $mapOfSeatsBooked")
                for(ele in bookedSeatsByUser){
                    mapOfSeatsBooked.remove(ele)
                }
                database.getReference("Events")
                    .child(temp[1])
                    .child(temp[2])
                    .child(temp[0])
                    .setValue(mapOfSeatsBooked)
                    .addOnSuccessListener {
                        Log.d("[CODE]:", "UPDATED THE MAP!")
                        finish()
                    }
            }.addOnFailureListener {
                Log.d("[CODE]:", "FAILED TO UPDATE THE MAP!!")
            }
    }
}