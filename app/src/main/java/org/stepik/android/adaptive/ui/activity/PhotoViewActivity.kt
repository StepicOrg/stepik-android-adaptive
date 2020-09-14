package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.ui.fragment.PhotoViewFragment

class PhotoViewActivity : FragmentActivity() {
    companion object {
        const val PATH_KEY = "PATH_KEY"
    }

    override fun createFragment(): Fragment {
        val path = intent.getStringExtra(PATH_KEY)
        return PhotoViewFragment.newInstance(path)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Util.isLowAndroidVersion()) {
            window.statusBarColor = resources.getColor(android.R.color.black)
        }
    }
}