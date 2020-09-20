package org.stepik.android.adaptive.ui.activity

import org.stepik.android.adaptive.ui.fragment.OnboardingFragment

class IntroActivity : FragmentActivity() {
    override fun createFragment() =
        OnboardingFragment()
}
