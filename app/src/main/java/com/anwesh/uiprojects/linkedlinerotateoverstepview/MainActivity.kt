package com.anwesh.uiprojects.linkedlinerotateoverstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.anwesh.uiprojects.linerotateoverstepview.LineRotateOverStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view : LineRotateOverStepView = LineRotateOverStepView.create(this)
        fullScreen()
        view.addOnAnimationListener({
            showToast("animation number $it is completed")
        }, {
            showToast("animation number $it is reset")
        })
    }

    fun showToast(txt : String) {
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show()
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}