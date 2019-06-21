package com.tasks.cache

import java.io.Serializable

interface Cache<T:Serializable> {
    fun get(key:String):T?
    fun put(key: String, value: T, expiry:Long=0)
    fun delete(key: String): Serializable?

    //TODO + check exists??
}