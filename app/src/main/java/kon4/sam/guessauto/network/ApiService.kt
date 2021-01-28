package kon4.sam.guessauto.network

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("get_all_score")
    fun get_all_score(): Call<List<User>>

    @GET("update_user_name")
    fun update_user_name(@Query("first_name") first_name: String,@Query("second_name") second_name: String): Call<String>

    @GET("set_new_user")
    fun set_new_user(@Query("user_name") user_name: String): Call<String>

    @GET("get_my_score")
    fun get_my_score(@Query("user_name") user_name: String): Call<String>

    @GET("set_score")
    fun set_score(@Query("user_name") user_name: String, @Query("score") score:String): Call<String>

}