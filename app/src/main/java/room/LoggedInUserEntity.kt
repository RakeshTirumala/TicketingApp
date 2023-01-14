package room
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LoggedInUsersTable")
data class LoggedInUserEntity(
    @PrimaryKey @NonNull
    var username: String = "",
    var password: String?= null,
    var isLoggedIn:Boolean = true
)
