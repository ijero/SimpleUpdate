package cn.ijero.su.download

import okhttp3.Request

class DownloadConfig {
    private var listener: DownloadListener? = null
    private var dir: String? = null
    private var filename: String? = null
    private var tipContent: String? = null
    private var cancelable = false
    private var request: Request? = null
    private var timeout = 0L

    class Builder {
        private var listener: DownloadListener? = null
        private var dir: String? = null
        private var filename: String? = null
        private var tipContent: String? = null
        private var request: Request? = null
        private var cancelable = false
        private var timeout = 10_000L

        fun dialogContent(tipContent: String): Builder {
            this.tipContent = tipContent
            return this
        }


        fun cancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun dir(dir: String): Builder {
            this.dir = dir
            return this
        }

        fun timeout(timeout: Long): Builder {
            this.timeout = timeout
            return this
        }
        fun filename(filename: String): Builder {
            this.filename = filename
            return this
        }

        fun request(request: Request): Builder {
            this.request = request
            return this
        }

        fun listener(listener: DownloadListener): Builder {
            this.listener = listener
            return this
        }

        fun build(): DownloadConfig {
            val request = request ?: throw IllegalArgumentException("DownloadConfig request's configure cannot be null!")
            val dir = dir ?: throw IllegalArgumentException("Download save dir cannot be null!")

            return DownloadConfig().apply {
                this.dir = dir
                this.request = this@Builder.request
                this.timeout = this@Builder.timeout
                this.filename = this@Builder.filename
                this.listener = this@Builder.listener
                this.tipContent = this@Builder.tipContent
                this.cancelable = this@Builder.cancelable
            }
        }
    }

    fun dir() = dir!!
    fun filename() = filename
    fun listener() = listener
    fun tipContent() = tipContent
    fun cancelable() = cancelable
    fun request() = request!!
    fun timeout() = timeout


}