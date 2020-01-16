package com.harounach.roote.database.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue
import com.harounach.roote.utils.FIREBASE_PROPERTY_TIMESTAMP

data class User(
    val name: String? = "",
    val email: String? = "",
    var accountType: String,
    var timestampJoined: MutableMap<String, Any> = HashMap(),
    var timestampLastChanged: MutableMap<String, Any> = HashMap()
) {
    init {
        val timestampLastChangedObj: MutableMap<String, Any> = HashMap()
        timestampLastChangedObj[FIREBASE_PROPERTY_TIMESTAMP] = ServerValue.TIMESTAMP
        this.timestampLastChanged = timestampLastChangedObj
        this.timestampJoined = timestampLastChangedObj
    }

    @Exclude
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "name" to name!!,
            "email" to email!!,
            "timestampJoined" to timestampJoined,
            "timestampLastChanged" to timestampLastChanged
        )
    }

    @Exclude
    fun getTimestampLastChangedLong() : Long {
        return timestampLastChanged[FIREBASE_PROPERTY_TIMESTAMP] as Long
    }

    @Exclude
    fun getTimestampJoined() : Long? {
        return timestampJoined[FIREBASE_PROPERTY_TIMESTAMP] as Long
    }


}