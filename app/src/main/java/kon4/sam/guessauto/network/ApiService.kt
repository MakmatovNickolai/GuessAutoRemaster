package kon4.sam.guessauto.network

import kon4.sam.guessauto.data.model.Car
import kon4.sam.guessauto.data.model.User
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("get_top_score")
    fun getTopScoreUsersByPage(@Query("page") page: Int): Call<List<User>>

    @POST("update_user")
    fun updateUser(@Body user: User): Call<String>

    @POST("create_user")
    fun createUser(@Body user: User): Call<String>

    @GET("get_my_info")
    fun getMyInfo(@Query("device_id") device_id: String): Call<User?>

    @GET("get_all_cars")
    fun getAllCars(): Call<List<Car>>
}