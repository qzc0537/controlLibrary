package com.rhizo.common.base

import android.os.Bundle
import android.os.PersistableBundle
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.gyf.immersionbar.ImmersionBar
import com.rhizo.common.R
import com.rhizo.common.dialog.LoadingDialog
import com.rhizo.common.util.ClickExt.setOnSafeClick
import com.rhizo.common.util.KeyboardUtil
import com.rhizo.common.util.LogUtil.logd
import com.rhizo.common.util.LogUtil.loge
import com.rhizo.common.util.LogUtil.logw

open class BaseActivity : AppCompatActivity() {
    private val LIFECYCLE = "lifecycle"
    private var mLoadingDialog: LoadingDialog? = null
    protected val mFragments = mutableListOf<Fragment>()
    protected val FRAGMENT_COUNT = "fragment_count"


    protected open fun defaultStatusBar(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        "${this.javaClass.name} onCreate".logd(LIFECYCLE)
        if (defaultStatusBar()) {
            ImmersionBar.with(this)
                .statusBarColor(R.color.common_f8f8f8)
                .statusBarDarkFont(true)
                .fitsSystemWindows(true)
                .init()
        }

        val contentView = window?.findViewById<ViewGroup>(android.R.id.content)
        contentView?.let {
            it.setOnSafeClick {
                KeyboardUtil.hideInputMethod(this)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(FRAGMENT_COUNT, mFragments.size)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        "${this.javaClass.name} onResume".logd(LIFECYCLE)
    }

    override fun onStop() {
        super.onStop()
        "${this.javaClass.name} onStop".logd(LIFECYCLE)
    }

    override fun onDestroy() {
        super.onDestroy()
        "${this.javaClass.name} onDestroy".logw(LIFECYCLE)
        dismissLoading()
    }

    fun showLoading(text: String = getString(R.string.loading)): LoadingDialog {
        if (mLoadingDialog == null) {
            mLoadingDialog = LoadingDialog(this)
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

    /**
     * 切换fragment
     */
    protected fun changeFragment(showFragment: Fragment, containerId: Int = R.id.fl_container) {
        val transaction = supportFragmentManager.beginTransaction()
        for (fragment in mFragments) {
            if (fragment == showFragment) {
                if (!fragment.isAdded) {
                    transaction.add(containerId, fragment, fragment.javaClass.simpleName)
                }
                transaction.show(fragment)
            } else {
                if (fragment.isAdded) {
                    transaction.hide(fragment)
                }
            }
        }
        transaction.commitAllowingStateLoss()
    }


}