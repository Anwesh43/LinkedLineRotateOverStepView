package com.anwesh.uiprojects.linkedlinerotateoverstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linerotateoverstepview.LineRotateOverStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LineRotateOverStepView.create(this)
    }
}
