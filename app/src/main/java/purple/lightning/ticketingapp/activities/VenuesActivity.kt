package purple.lightning.ticketingapp.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import models.Show
import models.Venue
import purple.lightning.ticketingapp.databinding.ActivityVenuesBinding
import reqs.ApiService
import adapters.LinearVenuesAdapter
import reqs.ServiceGenerator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class VenuesActivity : AppCompatActivity(), LinearVenuesAdapter.LinearOnClickListener {
    private var binding: ActivityVenuesBinding? = null
    private var allShows:MutableList<Show>?= null
    private var selectedEventName: String? = null
    private var venuesTimings:MutableMap<String,MutableMap<String,String>>? = null
    private var eventCost: String = ""
    private var username:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVenuesBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val bundle = intent.extras
        selectedEventName = bundle?.getString("SelectedEventName").toString()
        binding?.tvSelectedEvent?.text = selectedEventName
        username = bundle?.getString("username").toString()

        // FETCHING THE ALL SHOWS DATA AGAIN
        val serviceGenerator = ServiceGenerator.buildService2(ApiService::class.java)
        val callAllShows = serviceGenerator.getAllShows()

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        callAllShows.enqueue(object : Callback<MutableList<Show>> {
            override fun onResponse(
                call: Call<MutableList<Show>>,
                response: Response<MutableList<Show>>
            ) {
                if(response.isSuccessful) {
//                    Log.d("[CODE]:", response.body().toString())
                    response.body().let {
                        allShows = it!!
                        progressDialog.dismiss()
                        // FETCHING THE EVENT TIMINGS AT EACH HALL
                        val callShowTimings = serviceGenerator.getVenueShowTimings()
                        callShowTimings.enqueue(object :Callback<MutableMap<String,MutableMap<String, String>>>{
                            override fun onResponse(
                                call: Call<MutableMap<String, MutableMap<String, String>>>,
                                response: Response<MutableMap<String, MutableMap<String, String>>>
                            ) {
                                if(response.isSuccessful){
//                    Log.d("[CODE]:", response.body().toString())
                                    response.body().let{
                                        venuesTimings = it!!
                                        progressDialog.dismiss()
                                        Log.d("[CODE]:", "AllShows:${allShows!!}, ShowTimings:${venuesTimings!!}")
                                        setUpLinearViewOfAvailVenues()

                                    }
                                }
                            }

                            override fun onFailure(call: Call<MutableMap<String, MutableMap<String, String>>>, t: Throwable) {
                                TODO("Not yet implemented")
                            }

                        })
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


    }

    private fun setUpLinearViewOfAvailVenues(){
        Log.d("[CODE]:","INSIDE setUpLinearViewOfAvailVenues")
        Log.d("[CODE]:", "AllShows:${allShows!!}")
        Log.d("[CODE]:", "Show Timings:${venuesTimings!!}")
        for(s in allShows!!){
            if(selectedEventName == s.eventName){
                Log.d("[CODE]:", "SelectedEventName:${s.eventName}")
                eventCost = s.eventCost
                setupRv(s.availableInVenues)
                break
            }else{
                continue
            }
        }
    }

    private fun setupRv(availVenues:List<Venue>){
        binding?.rvLinearVenue?.layoutManager = LinearLayoutManager(this@VenuesActivity)
        Log.d("[CODE]:","INSIDE SETUPRV")
        var rvLinearVenueAdapter = LinearVenuesAdapter(availVenues, selectedEventName!!, venuesTimings!!,this)
        binding?.rvLinearVenue?.adapter = rvLinearVenueAdapter
    }

    override fun LinearonClickListener(venueName:String, eventTiming:String) {
        var bundle = Bundle()
        bundle.putString("SelectedEventName", selectedEventName)
        bundle.putString("selectedVenueName", venueName)
        bundle.putString("selectedEventTiming", eventTiming)
        bundle.putString("eventCost", eventCost)
        bundle.putString("username", username)
        val intent = Intent(this@VenuesActivity, BookingActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }
}