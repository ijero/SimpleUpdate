package cn.ijero.su.download

interface DownloadListener {
    /**
     * 下载的回调接口方法
     *
     * 如果不希望显示对应状态的dialog，或者已经自己处理，请返回false
     *
     * @return 当返回true时，将继续显示内部的dialog，否则将不显示对应状态的dialog
     *
     * @author Jero
     */
    fun onDownload(info: DownloadInfo, error: Throwable? = null): Boolean
}