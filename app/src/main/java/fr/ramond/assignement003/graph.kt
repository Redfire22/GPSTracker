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

class graph(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private lateinit var mPaint : Paint
    private var mIsInit = false
    private lateinit var mPath : Path

    private var mOriginX : Float = 0.0f
    private var mOriginY : Float = 0.0f
    private var mWidth : Int = 0
    private var mHeight : Int = 0
    private var mXUnit : Float = 0.0f
    private var mYUnit : Float = 0.0f
    private lateinit var mBlackPaint : Paint
    lateinit var mPoint : MutableList<Double>

    private var nbPoint : Int = 0

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

        mWidth = width
        mHeight = height

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
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

        if(nbPoint == 0) {
            mXUnit = mWidth.toFloat()

            mYUnit = mHeight.toFloat()
        }
        else{
            mXUnit = (mWidth / nbPoint).toFloat()

            mYUnit = (mHeight / nbPoint).toFloat()
        }

        drawAxis(canvas, mBlackPaint)

        drawGraphPlot(canvas, mPath, mPaint)
        drawGraphBackground(canvas, mBlackPaint)
    }

    private fun drawAxis(canvas : Canvas, paint : Paint) {
        canvas.drawLine(0f, mHeight.toFloat(), 0f, 0f, paint) // y axis
        canvas.drawLine(0f, mHeight.toFloat(), mWidth.toFloat() , mHeight.toFloat(), paint) // x axis
    }

    private fun drawGraphPlot(canvas : Canvas, path : Path, paint : Paint){
        mPath.moveTo(0f, mHeight.toFloat())

        for(i in 0 until nbPoint){
            mPath.lineTo((i*mXUnit), (mHeight - ((mPoint[i] * mHeight)/10).toFloat()))

            canvas.drawCircle((i*mXUnit), mHeight - (mPoint[i] * mYUnit).toFloat(), 5f, paint)
        }
        canvas.drawPath(mPath, paint)
    }

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



}