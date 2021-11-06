package kon4.sam.guessauto.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kon4.sam.guessauto.BuildConfig
import kon4.sam.guessauto.util.SharedPrefsHelper
import kon4.sam.guessauto.data.DBHelper
import kon4.sam.guessauto.network.ApiClient
import kon4.sam.guessauto.network.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

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
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofitInstance(client: OkHttpClient): Retrofit {
        val url = if (BuildConfig.DEBUG) ApiClient.BASE_URL else ApiClient.BASE_URL
        return Retrofit.Builder()
            .client(client)
            .baseUrl(url)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}