package org.stepik.android.adaptive.api.user.model

import org.stepik.android.adaptive.data.model.Meta
import org.stepik.android.adaptive.data.model.User

class UsersResponse(
        val meta: Meta,
        val users: List<User>
)