package cn.ijero.su.download

data class DownloadInfo(
        val url: String,
        var path: String? = null,
        var soFar: Long = 0L,
        var total: Long = 0L,
        var state: DownloadState = DownloadState.INVALID
)