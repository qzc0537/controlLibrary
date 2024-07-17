package com.rhizo.libcontrol.bean


open class BaseBody(var serialNo: String = "") {

    companion object {
        const val NOT_SUPPORT = 0 //if the operation is not supported by current launcher
        const val SUCCESS = 200 //for reset map succeed
        const val INVALID_ACTION = 400 //for invalid action
        const val PERMISSION_REQUIRED = 403 //for [Permission.MAP] permission required
        const val OPERATION_TIMEOUT = 408 //for operation timeout

        const val MAPPING_FINISHED = 304 //for mapping has been already finished

        const val TOO_BUSY = 429 //too busy
    }
}
