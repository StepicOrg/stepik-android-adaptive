package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.ui.fragment.PhotoViewFragment

class PhotoViewActivity : FragmentActivity() {
    private companion object {
        const val pathKey = "pathKey"
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