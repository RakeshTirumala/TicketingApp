package reqs


import models.Show
import retrofit2.http.GET
import retrofit2.Call
interface ApiService {
    @GET("/api/allshows")
    fun getAllShows():Call<MutableList<Show>>

    @GET("/api/venueShowTimings")
    fun getVenueShowTimings():Call<MutableMap<String, MutableMap<String, String>>>
}