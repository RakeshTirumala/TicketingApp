 package purple.lightning.ticketingapp.activities

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import purple.lightning.ticketingapp.databinding.ActivityMainBinding
import room.LoggedInUserEntity
import room.RoomApp

 class MainActivity : AppCompatActivity() {
    private var binding:ActivityMainBinding?=null
     private var user: LoggedInUserEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val animeDrawable = binding?.clMainActivity?.background as AnimationDrawable
        animeDrawable.setEnterFadeDuration(10)
        animeDrawable.setExitFadeDuration(1000)
        animeDrawable.start()

        val timer = object: CountDownTimer(3000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                Log.d("Timer","to start next activity")
            }

            override fun onFinish() {
                val loggedInUsersDao = (application as RoomApp).db.loggedInUsersDao()
                lifecycleScope.launch {
                    user = loggedInUsersDao.fetchUserByLogStatus(true)
                }
                if(user == null) goToLoginActivity() else goToHomeActivity()

            }
        }
        timer.start()
    }

     private fun goToLoginActivity(){
         val intent = Intent(this@MainActivity, LoginActivity::class.java)
         startActivity(intent)
         finish()
     }

     private fun goToHomeActivity(){
         val intent = Intent(this@MainActivity, TicketingActivity::class.java)
         startActivity(intent)
         finish()
     }

     override fun onDestroy() {
         super.onDestroy()
         binding = null
     }
}