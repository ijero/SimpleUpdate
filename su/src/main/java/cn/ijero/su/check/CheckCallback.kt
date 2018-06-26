package cn.ijero.su.check


interface CheckCallback<in T> {
    fun onCheckSuccessAndNext(checkFlow: CheckFlow, t: T)
    fun onCheckFailure(checkFlow: CheckFlow, throwable: Throwable?)
}