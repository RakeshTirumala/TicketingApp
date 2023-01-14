package reqs

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceGenerator {
    private val client = OkHttpClient.Builder().build()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://ticketingserver.onrender.com/api/allshows/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val retrofit2 = Retrofit.Builder()
        .baseUrl("https://ticketingserver.onrender.com/api/venueShowTimings/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun <T>buildService(service: Class<T>):T{
        return retrofit.create(service)
    }


    fun <T>buildService2(service: Class<T>):T{
        return retrofit2.create(service)
    }
}