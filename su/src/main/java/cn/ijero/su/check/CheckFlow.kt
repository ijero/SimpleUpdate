package cn.ijero.su.check

import android.app.Activity
import cn.ijero.su.R
import cn.ijero.su.download.DownloadInfo
import cn.ijero.su.download.DownloadState
import cn.ijero.su.download.Downloader
import cn.ijero.su.installApk
import cn.ijero.su.post
import cn.ijero.su.show
import cn.ijero.su.ui.UpdateDialog
import com.afollestad.materialdialogs.DialogAction
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class CheckFlow(private val host: Activity) {
    private var dialog: UpdateDialog? = null

    companion object {
        private const val TAG: String = "CheckFlow"
        private const val INTERVAL = 200L
    }

    private var thread: Thread? = null
    private var lastTime = 0L
    private var lastState = DownloadState.INVALID
    private var mIsCancel = false
    private lateinit var downloader: Downloader

    fun download(downloader: Downloader): UpdateDialog {
        if (dialog == null) {
            val builder = UpdateDialog.Builder(host).apply {
                content(downloader.tipContent() ?: "")
                title(host.getString(R.string.str_download_update_title))
                positiveText(host.getString(R.string.str_download))
                autoDismiss(false)
                cancelable(false)
                canceledOnTouchOutside(downloader.cancelable())
                onPositive { _, _ ->
                    mIsCancel = false
                    startDownload()
                }
            }

            dialog = UpdateDialog(builder).apply {
                show()
            }
        } else {
            dialog!!.show()
        }
        this.downloader = downloader
        return dialog!!
    }

    private fun startDownload() {
        dialog?.apply {
            progressBar.show()
            setContent(host.getString(R.string.str_download_prepare))
        }

        // create Download info
        val info = DownloadInfo(downloader.url()!!)
        thread = thread(start = true, isDaemon = true) {
            try {
                info.state = DownloadState.START
                callbackToUI(info)
                (URL(downloader.url()).openConnection() as HttpURLConnection).apply {
                    requestMethod = downloader.method()
                    readTimeout = downloader.timeout()
                    connectTimeout = downloader.timeout()
                    connect()

                    // filename
                    val saveFilename = downloader.filename()
                    val filename = if (saveFilename.isNullOrEmpty()) {
                        url.file.substringAfterLast(File.separatorChar)
                    } else {
                        saveFilename
                    }

                    // path
                    info.path = "${downloader.dir()}${File.separatorChar}$filename"

                    val file = File(info.path)
                    if (file.exists() && file.isFile) {
                        file.delete()
                    }

                    info.total = contentLength.toLong()

                    // start download and save file
                    File(downloader.dir()!!, filename).outputStream().use {
                        val bytes = ByteArray(1024)
                        var len = 0
                        var size = inputStream.read(bytes)
                        while (size != -1) {
                            if (mIsCancel) {
                                info.state = DownloadState.INVALID
                                callbackToUI(info)
                                return@thread
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
            } catch (t: Throwable) {
                t.printStackTrace()
                info.state = DownloadState.ERROR
                callbackToUI(info, t)
            } finally {
                if (thread?.isInterrupted == false) {
                    thread?.interrupt()
                }
            }
        }
    }

    private fun callbackToUI(info: DownloadInfo, t: Throwable? = null) {
        post {
            val current = System.currentTimeMillis()
            if (current - lastTime < INTERVAL && info.state == lastState) {
                return@post
            }

            lastState = info.state
            lastTime = current

            if (downloader.listener()?.onDownload(info, t) == true) {
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