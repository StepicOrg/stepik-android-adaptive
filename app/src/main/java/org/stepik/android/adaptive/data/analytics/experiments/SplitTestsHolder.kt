package org.stepik.android.adaptive.data.analytics.experiments

import javax.inject.Inject

class SplitTestsHolder
@Inject
constructor(
        splitTests: Set<@JvmSuppressWildcards SplitTest<*>>
)