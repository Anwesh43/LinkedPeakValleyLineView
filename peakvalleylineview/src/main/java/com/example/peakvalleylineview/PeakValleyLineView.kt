package com.example.peakvalleylineview

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas

val colors : Array<Int> = arrayOf(
    "#f44336",
    "#3F51B5",
    "#BF360C",
    "#00B8D4",
    "#0D47A1"
).map {
    Color.parseColor(it)
}.toTypedArray()
val delay : Long = 20
val sizeFactor : Float = 11.2f
val strokeFactor : Float = 90f
val backColor : Int = Color.parseColor("#BDBDBD")
val parts : Int = 3
val scGap : Float = 0.02f / parts

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawPeakValleyLine(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = w / sizeFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val y : Float = h * 0.5f
    save()
    translate(w, 0f)
    drawLine(0f, 0f, -size * sf1, y * sf1, paint)
    drawLine(-size, y, -size - (w - 2 * size) * sf2, y, paint)
    drawLine(-w + size, y, -w + size - size * sf3, y * (1 + sf3), paint)
    restore()
}

fun Canvas.drawPVLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawPeakValleyLine(scale, w, h, paint)
}

class PeakValleyLineView(ctx : Context) : View(ctx) {

    private val renderer  : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas )
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
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

    data class PVLNode(var i : Int, val state : State = State()) {

        private var next : PVLNode? = null
        private var prev : PVLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = PVLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawPVLNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : PVLNode {
            var curr : PVLNode? = prev
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

    data class PeakValleyLine(var i : Int) {

        private var curr : PVLNode = PVLNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : PeakValleyLineView) {

        private val animator : Animator = Animator(view)
        private val pvl : PeakValleyLine = PeakValleyLine(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            pvl.draw(canvas, paint)
            animator.animate {
                pvl.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            pvl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity: Activity) : PeakValleyLineView {
            val view : PeakValleyLineView = PeakValleyLineView(activity)
            activity.setContentView(view)
            return view
        }
    }
}