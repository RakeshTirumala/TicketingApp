package purple.lightning.ticketingapp.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import models.User
import purple.lightning.ticketingapp.R
import purple.lightning.ticketingapp.databinding.ActivityRegisterBinding
import room.LoggedInUserEntity
import room.RoomApp
import java.io.IOError

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {
    private var binding: ActivityRegisterBinding? = null
    private var imageUri: Uri?= null
    private var storage = FirebaseStorage.getInstance()
    private var database = FirebaseDatabase
        .getInstance("https://ticketting-afc73-default-rtdb.firebaseio.com/")
        .getReference("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.cvprofileImage?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        binding?.btnSubmit?.setOnClickListener {
            var username: String = binding?.etSUsername?.text.toString()
            var password: String = binding?.etSpassword?.text.toString()
            var confPassword: String = binding?.etSconfpassword?.text.toString()

            if(username.isNotEmpty() && password.isNotEmpty() && confPassword.isNotEmpty()){
                Log.d("Code: ", "calling insert userdetails function")
                insertUserDetails(username, password, confPassword)
            }else{
                Toast.makeText(this@RegisterActivity, "Something is missing!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data!=null){
            Log.d("RegisterActivity", "photo was selected")
            imageUri= data?.data!!
            Log.d("updated Image uri:", "${imageUri}")
            binding?.cvprofileImage?.setImageURI(imageUri)
        }

    }

    private fun insertUserDetails(username: String, password:String, confPassword: String){
        if(password == confPassword){
            database.child(username).get().addOnSuccessListener {
                if (it.exists()){
                    binding?.usernameTv?.text = "username is taken!"
                    binding?.usernameTv?.visibility = View.VISIBLE
                }else{
                    binding?.usernameTv?.text = "username is available!"
                    binding?.usernameTv?.visibility = View.VISIBLE
                    binding?.usernameTv?.setTextColor(Color.parseColor("#00FF00"))
                    Log.d("Code: ", "calling checkfurtherdetails function")
                    checkFutherDetails(username, password)
                }
            }.addOnFailureListener {
                Toast.makeText(this@RegisterActivity, "Failed!", Toast.LENGTH_SHORT).show()
            }

        }else{
            Toast.makeText(this@RegisterActivity, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            binding?.etSpassword?.text?.clear()
            binding?.etSconfpassword?.text?.clear()
        }
    }

    private fun checkFutherDetails(username:String, password: String){
        var encryptedPassword = encryptPassword(password.uppercase())

        Log.d("Code: ", "User model created!")

        insertImage(username, encryptedPassword)
    }
    private fun insertImage(username: String, encryptedPassword: String){

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Creating account...\uD83E\uDDD1\u200D\uD83D\uDE80")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val storageReference = storage.reference
        val ref = storageReference?.child("displayPictures/" + username + ".jpg")

        ref?.putFile(imageUri!!)
            ?.addOnSuccessListener {
                Log.d("Code: ", "IMAGE IS UPLOADED")
                val user = User(username=username, password = encryptedPassword)

                // INSERTING NEW USER DATA INTO THE DATABASE
                database.child(username).setValue(user).addOnCompleteListener{
                    Log.d("Code: ", "user details inserted!")
                }.addOnFailureListener {
                    Toast.makeText(this@RegisterActivity, "Failed to register!!", Toast.LENGTH_SHORT).show()
                }

                //val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                val userRoom = LoggedInUserEntity(username=username, password = encryptedPassword, isLoggedIn = true)
                val loggedInUsersDao = (application as RoomApp).db.loggedInUsersDao()
                lifecycleScope.launch {
                    loggedInUsersDao.insert(userRoom)
                }
                Log.d("Code: ", "UPLOADED TO ROOM DATABASE")

                //Constants.currentLocallyLoggedInUser = LoggedUser(username=username, password = encryptedPassword, userProfilePic = bitmap)
                // Log.d("[CODE]:", "lOGGEDUSER MODEL INSERTED!")

                if(progressDialog.isShowing) progressDialog.dismiss()

                val intent = Intent(this@RegisterActivity, TicketingActivity::class.java)
                startActivity(intent)
            }
            ?.addOnFailureListener{
                Toast.makeText(this@RegisterActivity, "An Error occured try changing the profile picture", Toast.LENGTH_LONG).show()
                database.child(username).removeValue()
                if(progressDialog.isShowing) progressDialog.dismiss()
            }
    }


    private fun encryptPassword(password: String):String{
        val key: Int = 14
        var cipherText: String = ""
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
                    cipherText = cipherText + temp

                }else{
                    var value = hashmap1.getValue(password[char])
                    value = value + key
                    if(value>29){
                        value = value - 29
                    }
                    var res = hashmap2.getValue(value)
                    cipherText = cipherText + res
                }
            }catch(e: IOError){
                Toast.makeText(this@RegisterActivity, "your password shouldn't include any other special character", Toast.LENGTH_SHORT).show()
                break
            }
        }

        return cipherText
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}