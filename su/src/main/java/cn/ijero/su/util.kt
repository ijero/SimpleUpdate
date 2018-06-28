package cn.ijero.su

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.content.FileProvider
import android.view.View
import java.io.File

fun Context.installApk(filePath: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    // 由于没有在Activity环境下启动Activity,设置下面的标签
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    val file = File(filePath)
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileProvider", file)
    } else {
        Uri.fromFile(file)
    }
    //Granting Temporary Permissions to a URI
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    this.startActivity(intent)
}

fun post(delay: Long = 0, action: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        action()
    }, delay)
}

fun View?.show() {
    if (this != null && this.visibility != View.VISIBLE) {
        this.visibility = View.VISIBLE
    }
}
fun View?.hide() {
    if (this != null && this.visibility != View.GONE) {
        this.visibility = View.GONE
    }
}