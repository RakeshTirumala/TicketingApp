package models

data class Show(
    var eventName:String,
    var eventCost:String,
    var eventGenre:String,
    var eventType:String,
    var availableInVenues: List<Venue>,
    var eventImg: String
)
