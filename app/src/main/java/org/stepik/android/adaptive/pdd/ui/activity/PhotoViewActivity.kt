package org.stepik.android.adaptive.pdd.ui.activity

import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import org.stepik.android.adaptive.pdd.Util
import org.stepik.android.adaptive.pdd.ui.fragment.PhotoViewFragment

class PhotoViewActivity : FragmentActivity() {
    companion object {
        val pathKey = "pathKey"
    }

    override fun createFragment(): Fragment {
        val path = intent.getStringExtra(pathKey)
        return PhotoViewFragment.newInstance(path)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Util.isLowAndroidVersion()) {
            window.statusBarColor = resources.getColor(android.R.color.black)
        }
    }
}