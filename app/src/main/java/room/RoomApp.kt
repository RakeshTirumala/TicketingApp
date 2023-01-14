package room

import android.app.Application

class RoomApp:Application() {
    val db by lazy {
        TicketingRoomDatabase.getInstance(this)
    }
}