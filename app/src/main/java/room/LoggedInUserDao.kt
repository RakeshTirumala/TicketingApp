package room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LoggedInUserDao {
    @Insert
    fun insert(loggedInUserEntity: LoggedInUserEntity)
    @Update
    fun update(loggedInUsersEntity: LoggedInUserEntity)
    @Delete
    fun delete(loggedInUsersEntity: LoggedInUserEntity)

    @Query("SELECT * from `LoggedInUsersTable`")
    fun fetchAllLoggedInUsers():List<LoggedInUserEntity>

    @Query("SELECT * FROM `LoggedInUsersTable` where username=:username")
    fun fetchUserByUsername(username:String): Flow<LoggedInUserEntity>

    @Query("SELECT * FROM `LoggedInUsersTable` where isLoggedIn=:boolval")
    fun fetchUserByLogStatus(boolval:Boolean): LoggedInUserEntity

    @Query("UPDATE `LoggedInUsersTable` SET isLoggedIn=:loggedInStatus where username=:username")
    fun updateLoggedInStatus(username:String, loggedInStatus:Boolean)
}