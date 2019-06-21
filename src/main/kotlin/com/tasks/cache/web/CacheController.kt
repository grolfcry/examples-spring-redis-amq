package com.tasks.cache.web

import com.tasks.cache.Cache
import org.springframework.core.serializer.DefaultSerializer
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.*
import java.io.ByteArrayOutputStream
import java.io.Serializable

@RestController
class CacheController(private val cache: Cache<Serializable>) {
    @RequestMapping(value = "/cache/{key}", method = [GET])
    fun get(@PathVariable("key") key: String): ByteArray? {
        val result = cache.get(key)
        return serialize(result)
    }

    private fun serialize(result: Serializable?): ByteArray? {
        return if (result != null) {
            if (result is ByteArray)
                result
            else {
                val outputStream = ByteArrayOutputStream()
                DefaultSerializer().serialize(result, outputStream)
                outputStream.toByteArray()
            }
        } else {
            null
        }
    }

    @RequestMapping(value = "/cache/{key}", method = [PUT, POST])
    fun put(@PathVariable("key") key: String, @RequestParam expiry: Long?, @RequestBody value: ByteArray) {
        if (expiry == null)
            cache.put(key, value)
        else
            cache.put(key, value, expiry)

    }

    @RequestMapping(value = "/cache/{key}", method = [DELETE] )
    fun delete(@PathVariable("key") key: String):  ByteArray? {
        var result = cache.delete(key)
        return serialize(result)
    }

}