package cn.ijero.simpleupdate

import android.content.Context
import android.content.pm.PackageManager


fun Context.versionCode(): Int {
    val packageInfo = this.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    return packageInfo.versionCode
}

fun Context.versionName(): String {
    val packageInfo = this.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    return packageInfo.versionName
}
