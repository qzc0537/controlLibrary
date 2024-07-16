package com.rhizo.common.dialog

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import com.rhizo.common.R
import com.rhizo.common.databinding.DialogLoadingBinding

class LoadingDialog(context: Context) :
    Dialog(context, R.style.DialogStyle) {
    private var mBinding: DialogLoadingBinding? = null
    private var mObjectAnimator: ObjectAnimator? = null
    private var mText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_loading,
            null,
            false
        )
        mBinding?.root?.let { setContentView(it) }
        window?.apply {
            setDimAmount(0.5f)
            setCancelable(true)
            setCanceledOnTouchOutside(false)
            setGravity(Gravity.CENTER)
            attributes.width = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        mBinding?.tvLoading?.text = mText
    }

    private fun showAnim() {
        mBinding?.ivLoading?.let {
            if (mObjectAnimator == null) {
                mObjectAnimator = ObjectAnimator.ofFloat(it, "rotation", 0f, 360f).apply {
                    duration = 1000
                    interpolator = LinearInterpolator()
                    repeatCount = ObjectAnimator.INFINITE
                    repeatMode = ObjectAnimator.RESTART
                }
            }
            if (mObjectAnimator?.isRunning == false) {
                mObjectAnimator?.start()
            }
        }
    }

    fun setText(text: String): LoadingDialog {
        mText = text
        mBinding?.tvLoading?.text = text
        return this
    }

    fun setText(@StringRes text: Int): LoadingDialog {
        setText(context.getString(text))
        return this
    }

    override fun show() {
        super.show()
        showAnim()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mObjectAnimator?.cancel()
    }

}