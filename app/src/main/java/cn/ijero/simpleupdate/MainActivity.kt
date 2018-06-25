package cn.ijero.simpleupdate

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import cn.ijero.su.SimpleUpdate
import cn.ijero.su.CheckCallback
import cn.ijero.su.CheckFlow
import okhttp3.Request

class MainActivity : AppCompatActivity(), CheckCallback<UpdateInfo> {


    companion object {
        private const val CHECK_URL = "http://192.168.199.134:3000"
    }
    override fun onCheckFailure(checkFlow: CheckFlow, throwable: Throwable?) {
        // 检查更新出错的回调
        // TODO

    }
    override fun onCheckSuccessAndNext(checkFlow: CheckFlow, t: UpdateInfo) {
        // 检查更新成功的回调
        // TODO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
