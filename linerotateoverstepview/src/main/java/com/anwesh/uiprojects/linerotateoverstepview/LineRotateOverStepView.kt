package com.anwesh.uiprojects.linerotateoverstepview

/**
 * Created by anweshmishra on 27/12/18.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.app.Activity

val nodes : Int = 5
val lines : Int = 4
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val color : Int = Color.parseColor("#2ecc71")
val sizeFactor : Float = 2.6f
val strokeFactor : Int = 90

fun Int.inverse() : Float = 1f / this

fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float =   (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap 