package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attributes: AttributeSet) : View(context, attributes) {
    private var myDrawingPath: CustomPath? = null
    private var myCanvasBitmap: Bitmap? = null
    private var myCanvasPaint: Paint? = null
    private var myDrawingPaint: Paint? = null
    private var myBrushSize: Float = 0.toFloat()
    private var canvas: Canvas? = null
    private var color = Color.BLACK
    private val myPaths = ArrayList<CustomPath>()
    private val myUndoPaths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    // Set the canvas with the black paint and brush size
    private fun setUpDrawing() {
        myDrawingPaint = Paint()
        myDrawingPaint!!.color = color
        myDrawingPaint!!.style = Paint.Style.STROKE
        myDrawingPaint!!.strokeJoin = Paint.Join.ROUND
        myDrawingPaint!!.strokeCap = Paint.Cap.ROUND
        myCanvasPaint = Paint(Paint.DITHER_FLAG)
        myDrawingPath = CustomPath(color, myBrushSize)
    }

    // Undo function
    fun onClickUndo() {
        // If the path is larger than 1
        if (myPaths.size > 0) {
            // Remove the last element of the array
            myUndoPaths.add(myPaths.removeAt(myPaths.size - 1))
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        myCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(myCanvasBitmap!!)
    }

    // TODO: change Canvas to Canvas? if fails
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(myCanvasBitmap!!, 0f, 0f, myCanvasPaint)

        for (path in myPaths) {
            myDrawingPaint!!.strokeWidth = path.brushThickness
            myDrawingPaint!!.color = path.color
            canvas.drawPath(path, myDrawingPaint!!)
        }

        // Check if the canvas is empty or not
        // Set the paint attributes
        if (!myDrawingPath!!.isEmpty) {
            myDrawingPaint!!.strokeWidth = myDrawingPath!!.brushThickness
            myDrawingPaint!!.color = myDrawingPath!!.color
            canvas.drawPath(myDrawingPath!!, myDrawingPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        // Set the path attributes
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                myDrawingPath!!.color = color
                myDrawingPath!!.brushThickness = myBrushSize

                myDrawingPath!!.reset()

                // Check if motion exists
                if (touchX != null) {
                    if (touchY != null) {
                        myDrawingPath!!.moveTo(
                            touchX,
                            touchY
                        )
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        myDrawingPath!!.lineTo(
                            touchX,
                            touchY
                        )
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                myPaths.add(myDrawingPath!!)
                myDrawingPath = CustomPath(color, myBrushSize)
            }

            else -> return false
        }
        invalidate()

        return true
    }

    // Change the brush size
    fun setSizeBrush(newSize: Float) {
        myBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        myDrawingPaint!!.strokeWidth = myBrushSize
    }

    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        myDrawingPaint!!.color = color
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }
}




