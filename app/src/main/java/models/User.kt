package models

data class User(
    var username: String? = null,
    var password: String?= null,
    var Booked:Map<String, MutableMap<String, MutableMap<String, String>>>? = null
)