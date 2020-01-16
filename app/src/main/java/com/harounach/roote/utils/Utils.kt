package com.harounach.roote.utils

class Utils {
    companion object {
        /**
         * Encode user email to use it as a Firebase key
         * (Firebase does not allow "." in the key name)
         */
        fun encodeEmail(userEmail: String) : String{
            return userEmail.replace(".", ",")
        }

        /**
         * Email is being decoded just once to display real email
         */
        fun decodeEmail(userEmail: String) : String{
            return userEmail.replace(",", ".")
        }
    }
}