package adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import purple.lightning.ticketingapp.databinding.HistoryItemBinding
import purple.lightning.ticketingapp.databinding.LinearVenueItemBinding
import reqs.Constants
import java.util.*

class LinearHistoryAdapter(
    private var dateEventVenueList: MutableList<String>,
    private var data:MutableMap<String, MutableMap<String, String>>,
    private var username:String,
    private var LinearonClickListener: LinearHistoryAdapter.LinearOnClickListener):
    RecyclerView.Adapter<LinearHistoryAdapter.ViewHolder>(){
    private val c = Calendar.getInstance()
    private val year = c.get(Calendar.YEAR)
    private val month = c.get(Calendar.MONTH)
    private val day = c.get(Calendar.DAY_OF_MONTH)
    private var database = FirebaseDatabase
        .getInstance("https://ticketting-afc73-default-rtdb.firebaseio.com/")

    class ViewHolder(binding:HistoryItemBinding):RecyclerView.ViewHolder(binding.root){
        val tvEventName = binding.tvEventName
        val tvVenueName = binding.tvVenueName
        val tvSelectedSeats = binding.tvSelectedSeats
        val tvDate = binding.tvDate
        val btnCancelBooking = binding.btnCancelBooking
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearHistoryAdapter.ViewHolder {
        return LinearHistoryAdapter.ViewHolder(
            HistoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LinearHistoryAdapter.ViewHolder, position: Int) {
        val dateEventVenue = dateEventVenueList[position]
        val bookedSeatsByUser:MutableMap<String, String> = data[dateEventVenue] as MutableMap<String, String>
        val temp = dateEventVenue.split("__")
        holder.tvEventName.text = temp[1]
        holder.tvVenueName.text = temp[2]
        holder.tvDate.text = temp[0]
        holder.tvSelectedSeats.text = bookedSeatsByUser.keys.toString()
        if("${day}_${Constants.monthMap[month.toString()]}_${year}" == temp[0]){
            holder.btnCancelBooking.visibility = View.VISIBLE
        }else{
            holder.btnCancelBooking.visibility = View.GONE
        }

        holder.btnCancelBooking.setOnClickListener {
            LinearonClickListener.LinearonClickListener(dateEventVenue, temp, bookedSeatsByUser.keys)
        }
    }

    override fun getItemCount(): Int {
        return dateEventVenueList.size
    }

    interface LinearOnClickListener {
        fun LinearonClickListener(dateEventVenue:String, temp: List<String>, bookedSeatsByUser:MutableSet<String>)
    }


}