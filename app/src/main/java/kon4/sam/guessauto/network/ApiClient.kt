package kon4.sam.guessauto.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class ApiClient {
    companion object {
        const val BASE_URL = "https://guessautoapi.herokuapp.com/"
    }
    private lateinit var apiService: ApiService

    fun getApiService(context: Context):ApiService {

        // Initialize ApiService if not initialized yet

        if (!::apiService.isInitialized) {
            val retrofit = Retrofit.Builder()
                    .client(okhttpClient(context))
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            apiService = retrofit.create(ApiService::class.java)
        }

        return apiService
    }

    /**
     * Initialize OkhttpClient with our interceptor
     */
    private fun okhttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()

                .build()
    }

}