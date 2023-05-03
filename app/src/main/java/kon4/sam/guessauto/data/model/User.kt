package kon4.sam.guessauto.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class User (
    var id:UUID = UUID.randomUUID(),
    var user_name:String? = null,
    var score:Int = 0,
    var device_id:String,
    var picture_url:String? = null
)
