package room

import androidx.room.*

@Dao
interface SelectedSeatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(selectedSeatsEntity: SelectedSeatsEntity)

    @Query("SELECT selectedSeatsStr from `SelectedSeatsTable`")
    fun fetchAllSelectedSeats():List<String>

    @Query("DELETE FROM `SelectedSeatsTable`")
    fun deleteSelectedSeats()

}