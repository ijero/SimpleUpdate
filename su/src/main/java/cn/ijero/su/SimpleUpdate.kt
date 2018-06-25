package cn.ijero.su

import android.app.Activity
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import okhttp3.*
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class SimpleUpdate<T> {
    private var host: Activity? = null
    private var request: Request? = null

    inner class Builder {

        private var host: Activity? = null
        private var request: Request? = null

        fun with(host: Activity): Builder {
            this.host = host
            return this
        }

        fun request(request: Request): Builder {
            this.request = request
            return this
        }

        fun build(): SimpleUpdate<T> {
            val host = host
                    ?: throw IllegalArgumentException("You must be initialize the builder's 'host' , please use 'builder.with' method.")

            val request = request
                    ?: throw IllegalArgumentException("You must be initialize the builder's 'request' , please use 'builder.request' method.")

            return SimpleUpdate<T>().apply {
                this.host = host
                this.request = request
            }
        }
    }

    fun check(callback: CheckCallback<T>) {
        val checkFlow = CheckFlow()

        // Reflect the genericType on CheckCallback
        val inters = callback::class.java.genericInterfaces
        var type: Type? = null
        for (i in 0 until inters.size) {
            if (((inters[i] as ParameterizedType).rawType as Class<*>).name == CheckCallback::class.java.name) {
                type = (inters[i] as ParameterizedType).actualTypeArguments[0] as Type
            }
        }

        if (type == null) {
            callback.onCheckFailure(checkFlow, RuntimeException("Read type error, check your CheckCallback's genericType."))
            return
        }

        val adapter = Gson().getAdapter(TypeToken.get(type)) as TypeAdapter<T>

        // build the OkHttpClient
        val okHttpClient = OkHttpClient.Builder().build()

        okHttpClient.newCall(request!!).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                callback.onCheckFailure(checkFlow, e)
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (response == null) {
                    callback.onCheckFailure(checkFlow, RuntimeException("Server not response content!"))
                    return
                }
                try {
                    val body = response.body()
                    if (body == null) {
                        callback.onCheckFailure(checkFlow, RuntimeException("Server not response content!"))
                        return
                    }
                    val t = adapter.read(JsonReader(body.charStream()))
                    callback.onCheckSuccessAndNext(checkFlow, t)
                } catch (t: Throwable) {
                    callback.onCheckFailure(checkFlow, t)
                    t.printStackTrace()
                }

            }
        })
    }

}