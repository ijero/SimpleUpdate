package cn.ijero.simpleupdate

data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val versionMessage: String?,
        val downloadUrl: String
)