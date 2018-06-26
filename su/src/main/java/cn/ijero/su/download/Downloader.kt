package cn.ijero.su.download

class Downloader {
    private var listener: DownloadListener? = null
    private var url: String? = null
    private var dir: String? = null
    private var method: String? = null
    private var filename: String? = null
    private var tipContent: String? = null
    private var timeout = 0
    private var cancelable = false

    class Builder {
        private var listener: DownloadListener? = null
        private var url: String? = null
        private var dir: String? = null
        private var method: String? = null
        private var filename: String? = null
        private var tipContent: String? = null
        private var timeout: Int = 10_000
        private var cancelable = false

        fun tipContent(tipContent: String): Builder {
            this.tipContent = tipContent
            return this
        }

        fun url(url: String): Builder {
            this.url = url
            return this
        }

        fun timeout(timeout: Int): Builder {
            this.timeout = timeout
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

        fun filename(filename: String): Builder {
            this.filename = filename
            return this
        }

        fun method(method: String): Builder {
            this.method = method
            return this
        }

        fun listener(listener: DownloadListener): Builder {
            this.listener = listener
            return this
        }

        fun build(): Downloader {
            val url = url ?: throw IllegalArgumentException("Download URL cannot be null!")
            val dir = dir ?: throw IllegalArgumentException("Download save dir cannot be null!")
            val method = method ?: "GET"

            return Downloader().apply {
                this.url = url
                this.dir = dir
                this.method = method
                this.filename = this@Builder.filename
                this.listener = this@Builder.listener
                this.timeout = this@Builder.timeout
                this.tipContent = this@Builder.tipContent
                this.cancelable = this@Builder.cancelable
            }
        }
    }

    fun url() = url
    fun dir() = dir
    fun filename() = filename
    fun listener() = listener
    fun timeout() = timeout
    fun method() = method
    fun tipContent() = tipContent
    fun cancelable() = cancelable


}