package org.stepik.android.adaptive.pdd.api

data class MigrationRequest(@JvmField val course: Long,
                            @JvmField val user: Long,
                            @JvmField val exp: Long,
                            @JvmField val streak: Long)