package com.rhizo.common.util

import android.content.Context
import com.rhizo.common.CommonLib

object DisplayUtil {

    fun getDensity(): Float {
        return CommonLib.getContext().resources.displayMetrics.density
    }

    fun getScreenW(): Int {
        return CommonLib.getContext().resources.displayMetrics.widthPixels
    }

    fun getScreenH(): Int {
        return CommonLib.getContext().resources.displayMetrics.heightPixels
    }

    fun dp2px(dp: Float): Int {
        return (dp * getDensity() + 0.5f).toInt()
    }

    fun px2dp(px: Float): Int {
        return (px / getDensity() + 0.5f).toInt()
    }

    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }


}