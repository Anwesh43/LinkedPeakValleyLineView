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
