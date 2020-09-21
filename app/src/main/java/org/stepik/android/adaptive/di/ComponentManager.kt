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

    val paidContentComponent by lazy {
        appCoreComponent.paidContentComponentBuilder().build()
    }

    val loginComponent by lazy {
        appCoreComponent.loginComponentBuilder().build()
    }
}
