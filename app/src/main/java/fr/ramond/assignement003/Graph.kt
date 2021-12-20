package fr.ramond.assignement003

import android.content.Context
import android.content.LocusId
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.location.Location
import android.util.AttributeSet
import android.view.View
import java.time.LocalDateTime
import kotlin.math.min

//Custom view, draw the graph
class Graph(context: Context, attrs: AttributeSet) : View(context, attrs) {

    //Global variable
    private lateinit var mPaint : Paint
    private var mIsInit = false
    private lateinit var mPath : Path

    private var mWidth : Int = 0
    private var mHeight : Int = 0
    private var mXUnit : Float = 0.0f
    private var mYUnit : Float = 0.0f
    private lateinit var mBlackPaint : Paint
    //List of speed, given by the ResultDisplay activity
    lateinit var mPoint : MutableList<Double>

    private var nbPoint : Int = 0

    //Initialisation of global variable, some of these value will change again
    private fun init(){
        mPaint = Paint()
        mPath = Path()
        mWidth = width
        mHeight = height

        if(nbPoint == 0) {
            mXUnit = mWidth.toFloat()

            mYUnit = mHeight.toFloat()
        }
        else{
            mXUnit = (mWidth / nbPoint).toFloat()

            mYUnit = (mHeight / nbPoint).toFloat()
        }

        mBlackPaint = Paint()
        mIsInit = true
    }

    //Override the onMeasure method, I need to do this in order to use the custom view in a
    //Scrolling view
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
         val viewWidth = 350
        val viewHeight = 350

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width : Int
        var height : Int

        if(widthMode == MeasureSpec.EXACTLY){
            width = widthSize
        }
        else if(widthMode == MeasureSpec.AT_MOST){
            width = min(viewWidth, widthSize)
        }
        else{
            width = viewWidth
        }

        if(heightMode == MeasureSpec.EXACTLY){
            height = heightSize
        }
        else if(heightMode == MeasureSpec.AT_MOST){
            height = min(viewHeight, heightSize)
        }
        else{
            height = viewHeight
        }
        //Also update global variable
        mWidth = width
        mHeight = height

        setMeasuredDimension(width, height)
    }

    //Starting point of this view, will call all required method
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //Make sure we don't launch init twice
        if(!mIsInit){
            init()
        }
        mBlackPaint.color = Color.BLACK
        mBlackPaint.style = Paint.Style.STROKE
        mBlackPaint.strokeWidth = 10f

        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 10f
        mPaint.color = Color.BLUE

        nbPoint = mPoint.size

        //Update global variable from the last received speed list
        if(nbPoint == 0) {
            mXUnit = mWidth.toFloat()

            mYUnit = mHeight.toFloat()
        }
        else{
            mXUnit = (mWidth / nbPoint).toFloat()

            mYUnit = (mHeight / nbPoint).toFloat()
        }

        //3 step
        //Draw the axis
        drawAxis(canvas, mBlackPaint)

        //Draw the speed line itself
        drawGraphPlot(canvas, mPath, mPaint)

        //Draw the background line
        drawGraphBackground(canvas, mBlackPaint)
    }

    //Very basic method that simply draw 2 lines
    private fun drawAxis(canvas : Canvas, paint : Paint) {
        canvas.drawLine(0f, mHeight.toFloat(), 0f, 0f, paint) // y axis
        canvas.drawLine(0f, mHeight.toFloat(), mWidth.toFloat() , mHeight.toFloat(), paint) // x axis
    }

    //Draw the speed line. This scale with the maximum value of speed in the graph.
    private fun drawGraphPlot(canvas : Canvas, path : Path, paint : Paint){
        mPath.moveTo(0f, mHeight.toFloat())

        val scale = maxSpeed()

        for(i in 0 until nbPoint){
            mPath.lineTo((i*mXUnit), (mHeight - ((mPoint[i] * mHeight)/scale).toFloat()))

            canvas.drawCircle((i*mXUnit), (mHeight - ((mPoint[i] * mHeight)/scale)).toFloat(), 5f, paint)
        }
        canvas.drawPath(mPath, paint)
    }

    //Draw the background line, again, it just draw line one after the other
    private fun drawGraphBackground(canvas : Canvas, paintBlack : Paint) {
        var cx = mXUnit
        var cy = mHeight - mYUnit

        paintBlack.strokeWidth = 1f

        for(i in 0 until nbPoint + 1){
            canvas.drawLine(cx, mYUnit, cx, cy, mBlackPaint)
            cx += mXUnit
        }

        cx = mXUnit

        for(i in 0 until nbPoint +1){
            canvas.drawLine(cx, cy, mWidth - mXUnit, cy, paintBlack)
            cy -= mYUnit
        }
    }

    //Method used in drawing the speed line, used to scale the graphic depending on the max
    //speed
    private fun maxSpeed(): Double {
        var max = 0.0
        for(i in 0 until mPoint.size){
            if(mPoint[i] > max){
                max = mPoint[i]
            }
        }
        return max
    }




}