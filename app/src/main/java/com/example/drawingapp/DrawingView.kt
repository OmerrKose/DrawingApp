package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class DrawingView(context: Context, attributes: AttributeSet) : View(context, attributes) {
    private var myDrawingPath: CustomPath? = null
    private var myCanvasBitmap: Bitmap? = null
    private var myCanvasPaint: Paint? = null
    private var myDrawingPaint: Paint? = null
    private var myBrushSize: Float = 0.toFloat()
    private var canvas: Canvas? = null
    private var color = Color.BLACK

    init {
        setUpDrawing()
    }

    // Set the canvas with the black paint and brush size
    private fun setUpDrawing() {
        myDrawingPaint = Paint()
        myDrawingPath = CustomPath(color, myBrushSize)
        myDrawingPaint!!.color = color
        myDrawingPaint!!.style = Paint.Style.STROKE
        myDrawingPaint!!.strokeJoin = Paint.Join.ROUND
        myDrawingPaint!!.strokeCap = Paint.Cap.ROUND
        myCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }


}