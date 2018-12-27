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
import android.graphics.RectF

val nodes : Int = 5
val lines : Int = 4
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val color : Int = Color.parseColor("#2ecc71")
val sizeFactor : Float = 2.6f
val strokeFactor : Int = 90
val backColor : Int = Color.parseColor("#212121")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this

fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float =   (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawProgressiveCircle(x : Float, r : Float, scale : Float, paint : Paint) {
    paint.style = Paint.Style.STROKE
    for (i in 0..1) {
        val sc : Float = scale.divideScale(i, 2)
        save()
        translate(x * (1 - 2 * i), 0f)
        drawArc(RectF(-r, -r, r, r), -90f, 360f * sc, false, paint)
        restore()
    }
}

fun Canvas.drawLRONode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val xGap : Float = (2 * size) / (lines + 1)
    paint.color = color
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(w/2, gap * (i + 1))
    drawProgressiveCircle((w/2 - size/3 - paint.strokeWidth/2), size/3, scale, paint)
    rotate(90f * sc2)
    var x : Float = 0f
    var deg : Float = 0f
    for (j in 0..(lines - 1)) {
        val sc = sc1.divideScale(j, lines)
        if (sc > 0) {
            deg = 180f * sc * (1 -  2 * (j % 2))
        }
        x += Math.floor(xGap.toDouble() * sc).toFloat()
    }
    save()
    translate(-((lines + 1) * 0.5f * xGap), 0f)
    drawLine(0f, 0f, x + xGap, 0f, paint)
    save()
    translate(x + xGap, 0f)
    rotate(deg)
    drawLine(0f, 0f, -xGap, 0f, paint)
    restore()
    restore()
    restore()
}

class LineRotateOverStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)
    private var onAnimationListener : OnAnimationListener? = null

    fun addOnAnimationListener(onComplete: (Int) -> Unit, onReset : (Int) -> Unit) {
        onAnimationListener = OnAnimationListener(onComplete, onReset)
    }

    fun handleAnimationListener(i : Int, scale : Float) {
        when(scale) {
            0f -> onAnimationListener?.onReset?.invoke(i)
            1f -> onAnimationListener?.onComplete?.invoke(i)
        }
    }

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {
        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, lines, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LRONode(var i : Int, val state : State = State()) {
        private var next : LRONode? = null
        private var prev : LRONode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = LRONode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLRONode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LRONode {
            var curr : LRONode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LineRotateOverStep(var i : Int) {

        private val root : LRONode = LRONode(0)
        private var curr : LRONode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb :() -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : LineRotateOverStepView) {
        private val animator : Animator = Animator(view)
        private val lros : LineRotateOverStep = LineRotateOverStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            lros.draw(canvas, paint)
            animator.animate {
                lros.update {i, scl ->
                    animator.stop()
                    view.handleAnimationListener(i, scl)
                }
            }
        }

        fun handleTap() {
            lros.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : LineRotateOverStepView {
            val view : LineRotateOverStepView = LineRotateOverStepView(activity)
            activity.setContentView(view)
            return view
        }
    }

    data class OnAnimationListener(var onComplete : (Int) -> Unit, var onReset : (Int) -> Unit)
}