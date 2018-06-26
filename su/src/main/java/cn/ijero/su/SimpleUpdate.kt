package cn.ijero.su

import android.app.Activity
import cn.ijero.su.check.CheckCallback
import cn.ijero.su.check.CheckFlow
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

        /**
         * 注入Activity上下文对象
         *
         * @author Jero
         */
        fun with(host: Activity): Builder {
            this.host = host
            return this
        }

        /**
         * 配置OkHttp的请求
         *
         * @author Jero
         */
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

    /**
     * 开始检查更新
     *
     * @param callback 更新状态的回调接口
     *
     * @author Jero
     */
    fun check(callback: CheckCallback<T>) {
        val activity = host ?: throw IllegalArgumentException("You must be initialize the builder's 'host' , please use 'builder.with' method.")
        val checkFlow = CheckFlow(activity)
        // Reflect the genericType on CheckCallback
        val inters = callback::class.java.genericInterfaces
        var type: Type? = null
        for (i in 0 until inters.size) {
            val inter = inters[i]
            if (inter is ParameterizedType && (inter.rawType as Class<*>).name == CheckCallback::class.java.name) {
                type = inter.actualTypeArguments[0] as Type
            }
        }

        if (type == null) {
            callback.onCheckFailure(checkFlow, RuntimeException("Read state error, check your CheckCallback's genericType."))
            return
        }

        val adapter = Gson().getAdapter(TypeToken.get(type)) as TypeAdapter<T>

        // build the OkHttpClient
        val okHttpClient = OkHttpClient.Builder().build()

        okHttpClient.newCall(request!!).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                post {
                    callback.onCheckFailure(checkFlow, e)
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                post {
                    if (response == null) {
                        callback.onCheckFailure(checkFlow, RuntimeException("Server not response content!"))
                        return@post
                    }
                    try {
                        val body = response.body()
                        if (body == null) {
                            callback.onCheckFailure(checkFlow, RuntimeException("Server not response content!"))
                            return@post
                        }
                        val t = adapter.read(JsonReader(body.charStream()))
                        callback.onCheckSuccessAndNext(checkFlow, t)
                    } catch (t: Throwable) {
                        callback.onCheckFailure(checkFlow, t)
                        t.printStackTrace()
                    }
                }
            }
        })
    }

}