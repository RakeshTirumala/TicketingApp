package purple.lightning.ticketingapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import purple.lightning.ticketingapp.databinding.ActivityBookingBinding
import reqs.Constants
import adapters.GridSeatsAdapter
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.PendingIntent.getActivity
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import purple.lightning.ticketingapp.R
import room.RoomApp
import room.SelectedSeatsEntity
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class BookingActivity : AppCompatActivity(), GridSeatsAdapter.GridOnClickListener {
    private var binding: ActivityBookingBinding?=null
    private var eventCost : String = ""
    private var totalCost:Int = 0
    private var username:String = ""
    private var selectedSeatsStr:List<String>? = null
    private var database = FirebaseDatabase
        .getInstance("https://ticketting-afc73-default-rtdb.firebaseio.com/")
    private var seatsBookedByUsers: MutableMap<String, String> = mutableMapOf()

    private val c = Calendar.getInstance()
    private val year = c.get(Calendar.YEAR)
    private val month = c.get(Calendar.MONTH)
    private val day = c.get(Calendar.DAY_OF_MONTH)

    private var bookedSeatsMap: Map<String, String> = mapOf()
    private var seatsList:MutableList<String> = mutableListOf()

    private var pageHeight = 1120
    private var pageWidth = 792

    lateinit var bmp: Bitmap
    lateinit var scaledbmp: Bitmap

    var selectedEventName:String? = null
    var selectedVenueName:String? = null
    var selectedEventTiming:String? =null

    var PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val bundle = intent.extras
        selectedEventName = bundle?.getString("SelectedEventName").toString()
        selectedVenueName = bundle?.getString("selectedVenueName").toString()
        selectedEventTiming = bundle?.getString("selectedEventTiming").toString()
        eventCost = bundle?.getString("eventCost").toString()
        username = bundle?.getString("username").toString()

        Log.d("[CODE]:", "SelectedEventName:${selectedEventName}, selectedVenueName:${selectedVenueName }, selectedEventTiming:${selectedEventTiming}")
        // SETTING UP THE GRID LAYOUT
        database.getReference("Events")
            .child(selectedEventName!!)
            .child(selectedVenueName!!)
            .child("${day}_${Constants.monthMap[month.toString()]}_${year}")
            .get()
            .addOnSuccessListener {
                Log.d("[CODE]:", "BOOKING ACTIVITY->MAP: ${it.value}")
                if(it.value != null){
                    bookedSeatsMap = it.value as MutableMap<String, String>
                    seatsList = bookedSeatsMap.keys.toMutableList()
                }
                Log.d("[CODE]:", "BOOKING ACTIVITY->ALREADY BOOKED SEATS: $bookedSeatsMap $seatsList")
                binding?.rvGridSeating?.layoutManager = GridLayoutManager(applicationContext, 15, GridLayoutManager.VERTICAL, false)
                var rvGridSeatsAdapter = GridSeatsAdapter(
                    Constants.seats,selectedEventName!!,
                    selectedVenueName!!,bookedSeatsMap,seatsList,this)
                binding?.rvGridSeating?.adapter = rvGridSeatsAdapter

            }.addOnFailureListener {
                Log.d("[CODE]:", "FAILED TO FETCH!!!")
            }

        binding?.tvEventName?.text = selectedEventName
        binding?.tvVenue?.text = selectedVenueName
        binding?.tvVenueTiming?.text = selectedEventTiming



        binding?.btnBook?.setOnClickListener {
            // INSERTING EVENT BOOKED BY THE USER
            val selectedSeatsDao = (application as RoomApp).db.selectedSeatsDao()
            lifecycleScope.launch {
                selectedSeatsStr = selectedSeatsDao.fetchAllSelectedSeats()
                Log.d("[CODE]:", "STRING FETCHED FROM THE LOCALDB : ${selectedSeatsStr}")
            }
            Log.d("[CODE]:", "selected seats str: ${selectedSeatsStr!![0]}")
            var selectedSeatsStrToLt: MutableList<String> = mutableListOf()
            var temp2 = ""
            for(ele in selectedSeatsStr!![0]){
                if(ele == '['){
                    continue
                }else if(ele == ',' || ele == ']'){
                    selectedSeatsStrToLt.add(temp2)
                    temp2 = ""
                }else{
                    temp2+="$ele"
                }
            }
            for(ele in selectedSeatsStrToLt){
                seatsBookedByUsers[ele] = username
            }
            Log.d("[CODE]:", "Map: ${seatsBookedByUsers}")
            database.getReference("Users")
                .child(username)
                .child("booked")
                .child("${day}_${Constants.monthMap[month.toString()]}_${year}__${selectedEventName}__${selectedVenueName}")
                .updateChildren(seatsBookedByUsers as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("[CODE]:", "SUCCESSFULLY INSERTED INTO USERS!!!")
                    val selectedSeatsDao = (application as RoomApp).db.selectedSeatsDao()
                    lifecycleScope.launch {
                        selectedSeatsDao.deleteSelectedSeats()
                    }
                }
                .addOnFailureListener {
                    Log.d("[CODE]:", "FAILED TO INSERT!!!")
                }

            // INSERTING BOOKINGS OF AN EVENT
            Log.d("[CODE]:", "Day:$day Month:$month Year:$year")

            database.getReference("Events")
                .child(selectedEventName!!)
                .child(selectedVenueName!!)
                .child("${day}_${Constants.monthMap[month.toString()]}_${year}")
                .updateChildren(seatsBookedByUsers as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("[CODE]:", "SUCCESSFULLY INSERTED EVENTS BOOKINGS")
                    if (checkPermissions()) {
                    Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show()
                    } else {
                    requestPermission()
                    }
                    generatePDF()
                    finish()
                }.addOnFailureListener {
                    Log.d("[CODE]:", "FAILED TO INSERT EVENTS BOOKINGS")
                }

        }

    }
    fun checkPermissions(): Boolean {

        var writeStoragePermission = ContextCompat.checkSelfPermission(
        applicationContext,
        WRITE_EXTERNAL_STORAGE
        )
        var readStoragePermission = ContextCompat.checkSelfPermission(
        applicationContext,
        READ_EXTERNAL_STORAGE
        )
        return writeStoragePermission == PackageManager.PERMISSION_GRANTED
        && readStoragePermission == PackageManager.PERMISSION_GRANTED
        }

    fun requestPermission() {

        ActivityCompat.requestPermissions(
        this,
        arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), PERMISSION_CODE
        )
        }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE) {
            if (grantResults.size > 0) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show()
            } else {
            Toast.makeText(this, "Permission Denied..", Toast.LENGTH_SHORT).show()
            finish()
            }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun generatePDF(){
        var pdfDocument: PdfDocument = PdfDocument()
        var paint: Paint = Paint()
        var title: Paint = Paint()
        bmp = BitmapFactory.decodeResource(resources, R.drawable.default_profile_picture)
        scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false)

        var myPageInfo: PdfDocument.PageInfo? = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var myPage: PdfDocument.Page = pdfDocument.startPage(myPageInfo)
        var canvas: Canvas = myPage.canvas
        canvas.drawBitmap(scaledbmp, 56F, 40F, paint)

        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
        title.textSize = 15F
        title.setColor(ContextCompat.getColor(this, R.color.purple_200))

        canvas.drawText("Ticketing App", 209F, 100F, title)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
        title.setColor(ContextCompat.getColor(this, R.color.purple_200))
        title.textSize = 15F

        title.textAlign = Paint.Align.CENTER
        canvas.drawText("$selectedEventName is booked by $username in $selectedVenueName venue " +
                "at $selectedEventTiming on ${day}_${Constants.monthMap[month.toString()]}_${year}", 396F, 560F, title)
        pdfDocument.finishPage(myPage)

        val file = File(Environment.getExternalStorageDirectory(), "${selectedVenueName}.pdf")
            try {
            pdfDocument.writeTo(FileOutputStream(file))

            Toast.makeText(applicationContext, "PDF file generated..", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
            e.printStackTrace()
                Log.d("[CODE]:", "${e.printStackTrace()}")

            Toast.makeText(applicationContext, "Failed to generate PDF file..", Toast.LENGTH_SHORT)
            .show()
            }
        pdfDocument.close()

    }

    override fun GridonClickListener(position: Int, size: Int, selectedSeats:MutableList<String>) {
        val temp = eventCost.split(".")
        Log.d("[CODE]:", "${temp}")
        binding?.tvTotalCost?.text ="Rs.${temp[1].toInt()*size}"
        totalCost = temp[1].toInt()*size

        val selectedSeatsDao = (application as RoomApp).db.selectedSeatsDao()
        lifecycleScope.launch {
            selectedSeatsDao.deleteSelectedSeats()
            selectedSeatsDao.insert(SelectedSeatsEntity("$selectedSeats"))
            Log.d("[CODE]:", "selected seats deployed in local db: ${selectedSeats}")
        }
    }
}