package kon4.sam.guessauto.network

import kon4.sam.guessauto.network.model.User
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("get_top_score")
    fun getTopScoreUsersByPage(@Query("page") page: Int): Call<List<User>>

    @GET("update_user_name")
    fun updateUserName(@Query("first_name") first_name: String,
                       @Query("second_name") second_name: String,
                       @Query("device_id") device_id: String): Call<String>

    @GET("set_new_user")
    fun setNewUser(@Query("user_name") user_name: String,
                   @Query("device_id") device_id: String): Call<String>

    @GET("set_score")
    fun setScore(@Query("user_name") user_name: String,
                 @Query("score") score:String,
                 @Query("device_id") device_id: String): Call<String>

    @GET("set_user_image")
    fun setUserImage(@Query("user_name") user_name: String,
                     @Query("url") url: String?,
                     @Query("device_id") device_id: String): Call<String>

    @GET("get_my_info")
    fun getMyInfo(@Query("device_id") device_id: String): Call<User?>
}