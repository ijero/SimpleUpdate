package cn.ijero.su.ui

import android.app.Activity
import com.afollestad.materialdialogs.MaterialDialog

class UpdateDialog(builder: MaterialDialog.Builder) : MaterialDialog(builder) {
    class Builder(host: Activity) : MaterialDialog.Builder(host)
}