package kon4.sam.guessauto

import android.app.Application
import android.provider.Settings
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kon4.sam.guessauto.data.model.User
import kon4.sam.guessauto.util.RandomString
import timber.log.Timber


@HiltAndroidApp
class App : Application() {


    companion object {
        lateinit var user: User
        lateinit var deviceId: String
        fun createNewUser() {
            user = User(device_id =  deviceId, user_name = "Player_${RandomString.getRandomString(5)}")
        }
    }

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
        Timber.plant(Timber.DebugTree())
        initAmplify()
        initUser()
    }

    private fun initUser() {
        deviceId = Settings.Secure.getString(this.applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
    }
    private fun initAmplify() {
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            val config = AmplifyConfiguration.builder(applicationContext)
                .devMenuEnabled(false)
                .build()
            Amplify.configure(config, applicationContext)
        } catch (error: AmplifyException) {
            Timber.e(error)
        }
    }
}