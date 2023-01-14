package adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import models.Venue
import purple.lightning.ticketingapp.databinding.LinearVenueItemBinding
import reqs.ApiService
import reqs.ServiceGenerator

class LinearVenuesAdapter(private var list:List<Venue>,
                          private var eventName:String,
                          private var  showsTimingsOfVenue:MutableMap<String,MutableMap<String, String>>,
                          private var LinearonClickListener: LinearOnClickListener
):
RecyclerView.Adapter<LinearVenuesAdapter.ViewHolder>(){
    private val serviceGenerator = ServiceGenerator.buildService(ApiService::class.java)
    val call = serviceGenerator.getVenueShowTimings()

    class ViewHolder(binding: LinearVenueItemBinding):RecyclerView.ViewHolder(binding.root){
        val tvVenueName = binding.tvVenueName
        val tvVenueLoc = binding.tvVenueLoc
        val tvEventTiming = binding.tvEventTiming
    }

    interface LinearOnClickListener {
        fun LinearonClickListener(venueName:String, eventTiming:String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LinearVenueItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val venue = list[position]
        holder.tvVenueName.text = venue.venueName
        holder.tvVenueLoc.text = venue.venuePlace
        val temp:MutableMap<String, String> = showsTimingsOfVenue[venue.venueName]!!
        holder.tvEventTiming.text = temp[eventName]
        holder.tvEventTiming.setOnClickListener {
            temp[eventName]?.let { it1 ->
                LinearonClickListener.LinearonClickListener(venue.venueName,
                    it1
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}