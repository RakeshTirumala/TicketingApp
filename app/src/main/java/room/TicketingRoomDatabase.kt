package room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [LoggedInUserEntity::class, SelectedSeatsEntity::class], version=4, exportSchema = true)
abstract class TicketingRoomDatabase: RoomDatabase() {
    abstract fun loggedInUsersDao():LoggedInUserDao
    abstract fun selectedSeatsDao():SelectedSeatsDao
    companion object{
        @Volatile
        private var INSTANCE:TicketingRoomDatabase?=null
        fun getInstance(context: Context):TicketingRoomDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance==null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TicketingRoomDatabase::class.java,
                        "Ticketing_Room_Database"
                    ).allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}