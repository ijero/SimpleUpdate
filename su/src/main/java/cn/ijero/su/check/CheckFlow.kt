package cn.ijero.su.check

import android.app.Activity
import cn.ijero.su.R
import cn.ijero.su.download.DownloadConfig
import cn.ijero.su.download.DownloadInfo
import cn.ijero.su.download.DownloadState
import cn.ijero.su.installApk
import cn.ijero.su.post
import cn.ijero.su.show
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class CheckFlow(private val host: Activity) {
    private var dialog: MaterialDialog? = null

    companion object {
        private const val TAG: String = "CheckFlow"
        private const val INTERVAL = 200L
    }

    private var lastTime = 0L
    private var lastState = DownloadState.INVALID
    private var mIsCancel = false
    private lateinit var downloadConfig: DownloadConfig

    fun download(config: DownloadConfig): MaterialDialog {
        if (dialog == null) {
            val builder = MaterialDialog.Builder(host).apply {
                content(config.tipContent() ?: "")
                title(host.getString(R.string.str_download_update_title))
                positiveText(host.getString(R.string.str_download))
                autoDismiss(false)
                cancelable(false)
                canceledOnTouchOutside(config.cancelable())
                onPositive { _, _ ->
                    mIsCancel = false
                    startDownload()
                }
            }

            dialog = builder.show()
        } else {
            dialog!!.show()
        }
        this.downloadConfig = config
        return dialog!!
    }

    private fun startDownload() {
        dialog?.apply {
            progressBar.show()
            setContent(host.getString(R.string.str_download_prepare))
        }

        // create Download info

        val request = downloadConfig.request()
        val info = DownloadInfo(request.url().toString())

        val timeout = downloadConfig.timeout()
        OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .build()
                .newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callbackToUI(info, e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body()
                        if (body == null) {
                            callbackToUI(info, NullPointerException("DownloadConfig cannot handle the data from server"))
                            return
                        }

                        val inputStream = body.byteStream()
                        inputStream.use {
                            // filename
                            val saveFilename = downloadConfig.filename()
                            val filename = if (saveFilename.isNullOrEmpty()) {
                                request.url().toString().substringAfterLast(File.separatorChar)
                            } else {
                                saveFilename
                            }

                            // path
                            info.path = "${downloadConfig.dir()}${File.separatorChar}$filename"

                            val file = File(info.path)
                            if (file.exists() && file.isFile) {
                                file.delete()
                            }

                            info.total = body.contentLength()

                            // start download and save file
                            File(downloadConfig.dir(), filename).outputStream().use {
                                val bytes = ByteArray(1024)
                                var len = 0
                                var size = inputStream.read(bytes)
                                while (size != -1) {
                                    if (mIsCancel) {
                                        info.state = DownloadState.INVALID
                                        callbackToUI(info)
                                        return@use
                                    }
//                             downloading
                                    it.write(bytes, 0, size)
                                    len += size
                                    size = inputStream.read(bytes)
                                    info.soFar = len.toLong()
                                    info.state = DownloadState.PROGRESS
                                    callbackToUI(info)
                                }
                            }
                        }
                        // download done
                        info.state = DownloadState.COMPLETED
                        callbackToUI(info)
                    }
                })
    }

    private fun callbackToUI(info: DownloadInfo, t: Throwable? = null) {
        post {
            val current = System.currentTimeMillis()
            if (current - lastTime < INTERVAL && info.state == lastState) {
                return@post
            }

            lastState = info.state
            lastTime = current

            if (downloadConfig.listener()?.onDownload(info, t) == true) {
                handleDownload(info)
            }
        }
    }

    private fun handleDownload(info: DownloadInfo) {
        when (info.state) {
            DownloadState.START -> {
                dialog?.apply {
                    setContent(host.getString(R.string.str_download_start_download))
                    val cancelStr = host.getString(R.string.str_cancel)
                    if (getActionButton(DialogAction.POSITIVE).text != cancelStr) {
                        setActionButton(DialogAction.POSITIVE, cancelStr)
                        builder.onPositive { _, _ ->
                            mIsCancel = true
                        }
                    }
                }
            }
            DownloadState.PROGRESS -> {
                dialog?.run {
                    setContent(String.format(host.getString(R.string.str_downloading), (info.soFar.toFloat() / info.total * 100).toInt()))
                    val cancelStr = host.getString(R.string.str_cancel)
                    if (getActionButton(DialogAction.POSITIVE).text != cancelStr) {
                        setActionButton(DialogAction.POSITIVE, cancelStr)
                        builder.onPositive { _, _ ->
                            mIsCancel = true
                        }
                    }
                }
            }
            DownloadState.COMPLETED -> {
                dialog?.apply {
                    setContent(host.getString(R.string.str_download_done))
                    setActionButton(DialogAction.POSITIVE, host.getString(R.string.str_install))
                    builder.onPositive { _, _ ->
                        mIsCancel = false
                        host.installApk(info.path!!)
                        dialog?.dismiss()
                    }
                }
            }
            DownloadState.ERROR -> {
                dialog?.apply {
                    progressBar.show()
                    setContent(host.getString(R.string.str_download_error))
                    setActionButton(DialogAction.POSITIVE, host.getString(R.string.str_retry))
                    builder.onPositive { _, _ ->
                        mIsCancel = false
                        startDownload()
                    }
                }
            }
            DownloadState.INVALID -> {
                dialog?.run {
                    setContent(host.getString(R.string.str_canceled))
                    setActionButton(DialogAction.POSITIVE, host.getString(R.string.str_download))
                    builder.onPositive { _, _ ->
                        mIsCancel = false
                        startDownload()
                    }
                }
            }
        }
    }
}