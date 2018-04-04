package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.header_levels_screen.*
import org.stepik.android.adaptive.R

class LevelsScreenActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels_screen)

        close.setOnClickListener { finish() }
    }
}