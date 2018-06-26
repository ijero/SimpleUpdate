package cn.ijero.simpleupdate

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import cn.ijero.su.SimpleUpdate
import cn.ijero.su.check.CheckCallback
import cn.ijero.su.check.CheckFlow
import cn.ijero.su.download.DownloadInfo
import cn.ijero.su.download.DownloadListener
import cn.ijero.su.download.DownloadState
import cn.ijero.su.download.Downloader
import okhttp3.Request

class MainActivity : AppCompatActivity(), CheckCallback<UpdateInfo>, DownloadListener {


    companion object {
        private const val CHECK_URL = "http://192.168.199.134:3000/update/check"
        private val SAVE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        private const val TAG = "MainActivity"
    }

    override fun onCheckFailure(checkFlow: CheckFlow, throwable: Throwable?) {
        // 检查更新出错的回调
        throwable ?: return
        Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()

    }

    override fun onDownload(info: DownloadInfo, error: Throwable?): Boolean {
        // 下载apk的监听器
        when (info.state) {
            DownloadState.ERROR -> {
                error?.printStackTrace()
                Toast.makeText(this, "下载出错", Toast.LENGTH_SHORT).show()
            }
            DownloadState.START -> {
                Toast.makeText(this, "开始下载", Toast.LENGTH_SHORT).show()
            }
            DownloadState.PROGRESS -> {
                Log.e(TAG, "progress: soFar = ${info.soFar} , total = ${info.total}")
            }
            DownloadState.COMPLETED -> {
                Toast.makeText(this, "下载完成", Toast.LENGTH_SHORT).show()
            }
            DownloadState.INVALID -> {

            }
        }
        return true
    }

    override fun onCheckSuccessAndNext(checkFlow: CheckFlow, t: UpdateInfo) {
        // 检查更新成功的回调
        if (t.versionCode > versionCode()) {
            val downloader = Downloader.Builder()
                    .url(t.downloadUrl)
                    .dir(SAVE_DIR)
                    .listener(this)
                    .tipContent(t.versionMessage)
                    .build()
            checkFlow.download(downloader)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    fun checkUpdate(view: View) {
        // 构造okHttpRequest
        val okHttpRequest = Request.Builder()
                .url(CHECK_URL) // 检查更新的api接口地址
                .get()
                .build()

        val simpleUpdate = SimpleUpdate<UpdateInfo>().Builder() // 实例化SimpleUpdate时指定接收检查更新返回的Json数据bean
                .with(this) // 注入Activity，显示UI及对话框需要
                .request(okHttpRequest) // 设置okhttp请求对象
                .build()

        // 执行检查更新
        simpleUpdate.check(this)
    }
}
