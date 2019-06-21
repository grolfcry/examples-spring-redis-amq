package com.tasks.cache

import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

//@Service
class HashMapCache : Cache<String> {
    val map = ConcurrentHashMap<String, String>()
    override fun get(key: String): String {
        return map[key].orEmpty()
    }

    override fun put(key: String, value: String, expiry:Long) {
        map[key] = value
    }

    override fun delete(key: String): String? {
        return map.remove(key)
    }
}