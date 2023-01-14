package room

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SelectedSeatsTable")
data class SelectedSeatsEntity(
    @PrimaryKey @NonNull
    var selectedSeatsStr:String = "")
