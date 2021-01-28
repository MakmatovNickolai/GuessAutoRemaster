package kon4.sam.guessauto.network

import com.google.gson.annotations.SerializedName

data class User (
    @SerializedName("user_name")
    var user_name:String,
    @SerializedName("score")
    var score:Int
)
