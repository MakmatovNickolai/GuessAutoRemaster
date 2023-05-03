package kon4.sam.guessauto.data.model

import java.util.UUID

data class Car(
    var id: UUID = UUID.randomUUID(),
    var name: String,
    var picture_url: String,
    var similar_cars: String
)
