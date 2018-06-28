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
import cn.ijero.su.download.DownloadConfig
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

    override fun onCheckSuccessAndNext(checkFlow: CheckFlow, t: UpdateInfo) {
        // 检查更新成功的回调
        // 准备下载新版本apk安装包
        if (t.versionCode > versionCode()) {
            val request = Request.Builder()
                    .url(t.downloadUrl)
                    .get().build()
            // 构建下载器的配置对象
            val config = DownloadConfig.Builder()
                    .request(request) // 设置OkHttp请求对象
                    .dir(SAVE_DIR) // 设置下载文件目录
                    .timeout(10_000L) // 设置超时时间
                    .listener(this) // 设置下载监听
                    .dialogContent(t.versionMessage) // 设置对话框显示的文本内容
                    .cancelable(true) // 设置对话框是否可取消
                    .build()

            checkFlow.download(config) // 开始下载
        } else {
            Toast.makeText(this, "目前已经是最新版", Toast.LENGTH_SHORT).show()
        }
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

        SimpleUpdate<UpdateInfo>().Builder() // 实例化SimpleUpdate时指定接收检查更新返回的Json数据bean
                .with(this) // 注入Activity，显示UI及对话框需要
                .request(okHttpRequest) // 设置OkHttp请求对象
                .build() // 生成SimpleUpdate实例
                .check(this) // 执行检查更新

    }
}
