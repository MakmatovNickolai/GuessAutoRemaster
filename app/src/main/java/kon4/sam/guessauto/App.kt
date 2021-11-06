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
import kon4.sam.guessauto.data.DatabaseUser
import timber.log.Timber

@HiltAndroidApp
class App : Application() {


    companion object {
        lateinit var deviceId: String
        var user: DatabaseUser = DatabaseUser("", 0 , "")
    }

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
        Timber.plant(Timber.DebugTree())
        initAmplify()
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
            /*Amplify.Auth.signIn("uploader", "LYtq2sT6@",
                { result ->
                    //result.isSignInComplete
                    //Timber.d("AuthQuickstart, Sign in succeeded - %s", result.toString())
                    //Timber.d("AuthQuickstart, isSignInComplete - %s", result.isSignInComplete.toString())
                },
                { Timber.e("AuthQuickstart, Failed to sign in ") }
            )*/
            //Amplify.Auth.confirmSignIn()
            /*Amplify.Auth.fetchAuthSession(
                { Timber.i("AmplifyQuickstart, Auth session = $it") },
                { Timber.e("AmplifyQuickstart, Failed to fetch auth session - ${it.message}") }
            )*/
        } catch (error: AmplifyException) {
            Timber.e(error)
        }
    }
}