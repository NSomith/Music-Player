package com.example.music.other

open class Event<out T>(val data:T) {
    var hasBeenHandled = false
        private set

    fun getContentIfHandled():T?{
        return if(hasBeenHandled){
            null
        }else{
            hasBeenHandled = true
            data
        }
    }

    fun peekContent() = data/* if the user needs to get the data even though it is handled already,
    this class comes in handy then */
}