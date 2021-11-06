package kon4.sam.guessauto.network.model

import com.google.gson.annotations.SerializedName
import kon4.sam.guessauto.data.DatabaseUser

data class User (
    @SerializedName("user_name")
    var user_name:String,
    @SerializedName("score")
    var score:Int,
    @SerializedName("url")
    var url:String?
) {
    fun toDatabaseModel(): DatabaseUser {
        return DatabaseUser(this.user_name, this.score, this.url)
    }
}
