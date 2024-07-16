package com.rhizo.common.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.rhizo.common.CommonLib


object ClipboardHelper {
    private var mClipboardManager: ClipboardManager? = null


    /**
     * 初始化剪贴板管理者
     */
    private fun initManager() {
        mClipboardManager =
            CommonLib.getContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    }

    /**
     * 将文本复制到剪贴板
     *
     * @param text 要复制的文本
     */
    fun copyText(text: String) {
        initManager()
        val clipData = ClipData.newPlainText("text", text)
        mClipboardManager?.setPrimaryClip(clipData)
    }

    /**
     * 从剪贴板获取文本
     *
     * @return 剪贴板中的文本
     */
    fun getCopiedText(): String {
        initManager()
        if (mClipboardManager?.hasPrimaryClip() == true) {
            val clipData = mClipboardManager?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text
                return text.toString()
            }
        }
        return ""
    }

}