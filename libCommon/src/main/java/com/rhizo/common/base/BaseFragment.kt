package com.rhizo.common.base

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.rhizo.common.R
import com.rhizo.common.dialog.LoadingDialog
import com.rhizo.common.util.LogUtil.logd
import com.rhizo.common.util.LogUtil.logw

open class BaseFragment : Fragment() {
    private val LIFECYCLE = "lifecycle"
    private var mLoadingDialog: LoadingDialog? = null

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        "${this.javaClass.simpleName} onHiddenChanged:$hidden".logd(LIFECYCLE)
    }

    override fun onResume() {
        super.onResume()
        "${this.javaClass.simpleName} onResume".logd(LIFECYCLE)
    }

    override fun onStop() {
        super.onStop()
        "${this.javaClass.simpleName} onStop".logd(LIFECYCLE)
    }

    override fun onDestroy() {
        super.onDestroy()
        "${this.javaClass.simpleName} onDestroy".logw(LIFECYCLE)
        dismissLoading()
    }

    fun <T> getHostAct(): T? {
        return activity as T
    }

    fun showLoading(text: String = getString(R.string.loading)): LoadingDialog {
        if (mLoadingDialog == null) {
            mLoadingDialog = LoadingDialog(requireContext())
        }
        mLoadingDialog?.setText(text)
        mLoadingDialog?.show()
        return mLoadingDialog!!
    }

    fun showLoading(@StringRes text: Int = R.string.loading): LoadingDialog {
        return showLoading(getString(text))
    }

    fun showLoading(): LoadingDialog {
        return showLoading(R.string.loading)
    }

    fun dismissLoading() {
        mLoadingDialog?.dismiss()
    }

    fun getColor(@ColorRes colorId: Int): Int {
        return ContextCompat.getColor(requireContext(), colorId)
    }

}