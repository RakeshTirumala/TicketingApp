package adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import purple.lightning.ticketingapp.databinding.SeatItemBinding
import java.util.*


class GridSeatsAdapter(private var list: MutableList<String>,
                       private var selectedEventName: String,
                       private var selectedVenueName: String,
                       private var bookedSeatsMap:Map<String, String>,
                       private var seatsList:MutableList<String>,
                       private val GridonClickListener: GridOnClickListener):
    RecyclerView.Adapter<GridSeatsAdapter.ViewHolder>(){

    private var selectedSeats:MutableList<String> = mutableListOf()
    private var database = FirebaseDatabase
        .getInstance("https://ticketting-afc73-default-rtdb.firebaseio.com/")
    private val c = Calendar.getInstance()
    private val year = c.get(Calendar.YEAR)
    private val month = c.get(Calendar.MONTH)
    private val day = c.get(Calendar.DAY_OF_MONTH)


    class ViewHolder(binding: SeatItemBinding):RecyclerView.ViewHolder(binding.root){
        var tvSeatNo = binding.tvSeatNo
        var cvSeat = binding.cvSeat
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SeatItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var seatNo = list[position]
        holder.tvSeatNo.text = seatNo
        Log.d("[CODE]:", "GRIDSEATS ADAPTER-> BOOKED SEATS lIST: ${seatsList}")
        for(ele in seatsList){
            ele.replace(" ", "")
        }
        Log.d("[CODE]:", "GRIDSEATS ADAPTER-> BOOKED SEATS lIST(AFR): ${seatsList}")
        if(seatsList.contains(" $seatNo") || seatsList.contains("$seatNo") || seatsList.contains("  $seatNo")){
            holder.cvSeat.setCardBackgroundColor(Color.RED.hashCode())
        }else{
            holder.cvSeat.setCardBackgroundColor(Color.WHITE.hashCode())
        }

        // SELECTING SEATS
        holder.cvSeat.setOnClickListener {
            if(selectedSeats.contains(list[position])){
                holder.cvSeat.setCardBackgroundColor(Color.WHITE.hashCode())
                selectedSeats.remove(list[position])
                Log.d("[CODE]:", "SELECTED SEATS: ${selectedSeats}")
            }else{
                holder.cvSeat.setCardBackgroundColor(Color.CYAN.hashCode())
                selectedSeats.add(list[position])
                Log.d("[CODE]:", "SELECTED SEATS: ${selectedSeats}")
            }
            GridonClickListener.GridonClickListener(position, selectedSeats.size, selectedSeats)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface GridOnClickListener{
        fun GridonClickListener(position: Int, size:Int, selectedSeats:MutableList<String>)
    }

}

//        for(ele in seatsList){
//            database.getReference("Events")
//                .child(selectedEventName)
//                .child(selectedVenueName)
//                .child("${day}_${Constants.monthMap[month.toString()]}_${year}")
//                .child(ele)
//                .get()
//                .addOnSuccessListener {
//                    if(it.value == null){
//                        Log.d("[CODE]:", "SEAT ALREADY BOOKED!!!")
//                        holder.cvSeat.setCardBackgroundColor(Color.RED.hashCode())
//                    }else{
//                        Log.d("[CODE]:", "SEAT NOT BOOKED!!!")
//                        holder.cvSeat.setCardBackgroundColor(Color.WHITE.hashCode())
//                    }
//                    Log.d("[CODE]:", "ALREADY BOOKED SEATS: $bookedSeatsMap $seatsList")
//                }.addOnFailureListener {
//                    Log.d("[CODE]:", "FAILED TO FETCH!!!")
//                }
//        }