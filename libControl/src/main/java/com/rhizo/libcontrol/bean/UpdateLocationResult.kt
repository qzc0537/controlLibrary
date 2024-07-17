package com.rhizo.libcontrol.bean

data class UpdateLocationResult(val result: Int) : BaseBody() {
    companion object {
        /**
         * 0 if the operation is not supported by current launcher
         * 200 for success
         * 400 invalid parameter
         * 403 for [Permission.MAP] permission required
         * 413 pose out of map
         */
        const val NOT_SUPPORT = 0
        const val SUCCESS = 200
        const val INVALID_PARAMETER = 400
        const val PERMISSION_REQUIRED = 403
        const val OUT_OF_MAP = 413
    }
}
