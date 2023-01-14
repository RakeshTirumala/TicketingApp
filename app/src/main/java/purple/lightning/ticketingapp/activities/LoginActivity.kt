package purple.lightning.ticketingapp.activities

import adapters.LocalUserItemAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import purple.lightning.ticketingapp.databinding.ActivityLoginBinding
import room.LoggedInUserDao
import room.LoggedInUserEntity
import room.RoomApp
import java.io.File
import java.io.IOError

class LoginActivity : AppCompatActivity(), LocalUserItemAdapter.OnUserClickListener {
    private var binding: ActivityLoginBinding?=null
    private  var database: DatabaseReference = FirebaseDatabase
        .getInstance("https://ticketting-afc73-default-rtdb.firebaseio.com/")
        .getReference("Users")
    private var storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val loggedInUsersDao = (application as RoomApp).db.loggedInUsersDao()
        val loggedInUsers: List<LoggedInUserEntity> = loggedInUsersDao.fetchAllLoggedInUsers()

        if(loggedInUsers.isNotEmpty()){
            setUpLocalUsersRv(loggedInUsers)
        }else{
            setupLogin()
        }

        binding?.btnSignup?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding?.btnLogin1?.setOnClickListener {
            binding?.etLUsername?.visibility = View.VISIBLE
            binding?.etLPassword?.visibility = View.VISIBLE
            binding?.btnLogin?.visibility = View.VISIBLE
            binding?.rvLocallyStoredUsers?.visibility = View.GONE
            binding?.btnLogin1?.visibility = View.GONE
        }

        binding?.btnLogin?.setOnClickListener {
            val username:String = binding?.etLUsername?.text.toString()
            val password:String = binding?.etLPassword?.text.toString()
            if(username.isNotEmpty() && password.isNotEmpty()){
                checkUserDetails(username, password)
            }else{
                val unicode = 0x1F9D0
                val res = String(Character.toChars(unicode))
                Toast.makeText(this@LoginActivity, "Something is missing ${res}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpLocalUsersRv(loggedInUsers: List<LoggedInUserEntity>){
        binding?.etLUsername?.visibility = View.GONE
        binding?.etLPassword?.visibility = View.GONE
        binding?.btnLogin?.visibility = View.GONE


        binding?.rvLocallyStoredUsers?.visibility = View.VISIBLE

        binding?.rvLocallyStoredUsers?.layoutManager = LinearLayoutManager(this@LoginActivity)

        var rvLocallyStoredUsersAdapter = LocalUserItemAdapter(loggedInUsers, this)
        binding?.rvLocallyStoredUsers?.adapter = rvLocallyStoredUsersAdapter
    }

    private fun setupLogin(){

        binding?.etLUsername?.visibility = View.VISIBLE
        binding?.etLPassword?.visibility = View.VISIBLE
        binding?.btnLogin?.visibility = View.VISIBLE
        binding?.rvLocallyStoredUsers?.visibility = View.GONE
        binding?.btnLogin1?.visibility = View.GONE
    }

    override fun onUserClickListener(position: Int, username: String) {

        val loggedInUsersDao = (application as RoomApp).db.loggedInUsersDao()
        lifecycleScope.launch {
            loggedInUsersDao.updateLoggedInStatus(username, true)
        }

        val intent = Intent(this@LoginActivity, TicketingActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkUserDetails(username:String, password:String){
        var bitmap: Bitmap? = null
        database.child(username).get().addOnSuccessListener {
            if(it.exists()){
                var dbPassword = it.child("password").value.toString()

                var decryptedPassword = decryptPassword(dbPassword.uppercase())

                // var ppl: String = database.child(username).child("profilePicLink").get() as String

                if (decryptedPassword == password.uppercase()){
                    val storageReference = storage.reference
                    val ref = storageReference?.child("images/${username}.jpg")

                    var filename = File.createTempFile("tempImg", "jpg")
                    ref?.getFile(filename)
                        ?.addOnSuccessListener{
                            bitmap = BitmapFactory.decodeFile(filename.absolutePath)
                        }

                    val userRoom = LoggedInUserEntity(username=username, password = dbPassword.uppercase(), isLoggedIn = true)
                    val loggedInUsersDao = (application as RoomApp).db.loggedInUsersDao()
                    addtoLoggedInUsers(userRoom, loggedInUsersDao)

                    val intent = Intent(this@LoginActivity, TicketingActivity::class.java)
                    startActivity(intent)
                    onDestroy()
                }else{
                    Toast.makeText(this@LoginActivity, "invalid details!", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this@LoginActivity, "user not found!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this@LoginActivity, "Failed to fetch!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun addtoLoggedInUsers(userRoom: LoggedInUserEntity, loggedInUsersDao: LoggedInUserDao){
        lifecycleScope.launch {
            loggedInUsersDao.insert(userRoom)
        }
    }

    private fun decryptPassword(password:String):String{
        val key: Int = 14
        var dText: String = ""
        val hashmap1 : HashMap<Char, Int> =  hashMapOf(
            'A' to 1, 'B' to 2,
            'C' to 3, 'D' to 4,
            'E' to 5, 'F' to 6,
            'G' to 7, 'H' to 8,
            'I' to 9, 'J' to 10,
            'K' to 11, 'L' to 12,
            'M' to 13, 'N' to 14,
            'O' to 15, 'P' to 16,
            'Q' to 17, 'R' to 18,
            'S' to 19, 'T' to 20,
            'U' to 21, 'V' to 22,
            'W' to 23, 'X' to 24,
            'Y' to 25, 'Z' to 26,
            '@' to 27, '$' to 28,
            '#' to 29
        )
        val hashmap2: HashMap<Int, Char> =  hashMapOf(
            1 to 'A', 2 to 'B',
            3 to 'C', 4 to 'D',
            5 to 'E', 6 to 'F',
            7 to 'G', 8 to 'H',
            9 to 'I', 10 to 'J',
            11 to 'K', 12 to 'L',
            13 to 'M', 14 to 'N',
            15 to 'O', 16 to 'P',
            17 to 'Q', 18 to 'R',
            19 to 'S', 20 to 'T',
            21 to 'U', 22 to 'V',
            23 to 'W', 24 to 'X',
            25 to 'Y', 26 to 'Z',
            27 to '@', 28 to '$',
            29 to '#'
        )

        val hashmap3 : HashMap<Char, Char> = hashMapOf(
            '0' to '5', '1' to '6',
            '2' to '7', '3' to '8',
            '4' to '9', '5' to '0',
            '6' to '1', '7' to '2',
            '8' to '3', '9' to '4'
        )

        for(char in 0 until password.length){
            try{
                if(password[char].isDigit()){
                    var temp =  hashmap3.getValue(password[char])
                    dText += temp

                }else{
                    var value = hashmap1.getValue(password[char])
                    value -= key

                    if(value<1){
                        value += 29
                    }
                    var res = hashmap2.getValue(value)
                    dText += res
                }
            }catch(e: IOError){
                Toast.makeText(this@LoginActivity, "unknown error!", Toast.LENGTH_SHORT).show()
                break
            }
        }

        return dText
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}