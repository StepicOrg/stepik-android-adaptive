package org.stepik.android.adaptive.di

class ComponentManager(
        private val appCoreComponent: AppCoreComponent
) {

    val statsComponent by lazy {
        appCoreComponent.statsComponentBuilder().build()
    }

    val studyComponent by lazy {
        appCoreComponent.studyComponentBuilder().build()
    }

}