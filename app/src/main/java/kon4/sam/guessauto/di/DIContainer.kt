package kon4.sam.guessauto.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kon4.sam.guessauto.BuildConfig
import kon4.sam.guessauto.data.DBHelper
import kon4.sam.guessauto.data.JsonDbHelper
import kon4.sam.guessauto.network.ApiClient
import kon4.sam.guessauto.network.ApiService
import kon4.sam.guessauto.util.SharedPrefsHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DIContainer {

    @Provides
    @Singleton
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPrefsHelper {
        return SharedPrefsHelper(context)
    }

    @Provides
    @Singleton
    fun provideDBHelper(@ApplicationContext context: Context, sharedPrefsHelper: SharedPrefsHelper): DBHelper {
        return DBHelper(context, sharedPrefsHelper)
    }

    @Provides
    @Singleton
    fun provideJsonDBHelper(sharedPrefsHelper: SharedPrefsHelper, dbHelper: DBHelper): JsonDbHelper {
        return JsonDbHelper(sharedPrefsHelper, dbHelper)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofitInstance(client: OkHttpClient): Retrofit {
        val httpClient = OkHttpClient.Builder()
        val url: String
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            httpClient.addInterceptor(logging)
            url = ApiClient.DEBUG_URL
        } else {
            url = ApiClient.BASE_URL
        }

        return Retrofit.Builder()
            .client(client)
            .baseUrl(url)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}