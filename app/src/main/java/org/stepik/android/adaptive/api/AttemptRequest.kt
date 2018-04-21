package org.stepik.android.adaptive.api

import org.stepik.android.adaptive.data.model.Attempt

class AttemptRequest(step: Long) {
    val attempt: Attempt = Attempt(step = step)
}
