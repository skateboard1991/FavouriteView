package com.skateboard.favouriteview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View

class FavouriteView(context: Context, attrs: AttributeSet?, defStyle: Int) : View(context, attrs, defStyle)
{
    private lateinit var paint: Paint

    private lateinit var path: Path

    private var spaceBetweenHandAndShoulder = 5

    private var state = 0

    private lateinit var valueAnimator: ValueAnimator

    private var size = 0

    private var centerX = 0f

    private var centerY = 0f

    private var strokeWithScaleFraction=1f

    private var statelliteOffsetFraction=0f

    companion object
    {
        val STATE_SELECTED = 1

        val STATE_CIRCLE = 2

        val STATE_RING=3

        val STATE_STATELLITE = 4

        val STATE_NORMAL = 0
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)

    init
    {
        initPaint()
        initClickEvent()
    }


    private fun initPaint()
    {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = ContextCompat.getColor(context, android.R.color.black)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        path = Path()
        val cornerPathEffect = CornerPathEffect(10f)
        paint.pathEffect = cornerPathEffect
    }

    private fun initAnimator()
    {
        valueAnimator = ValueAnimator.ofFloat(0f, 400f)
        valueAnimator.duration = 400
        valueAnimator.addUpdateListener(updateListener)
        valueAnimator.addListener(object : Animator.AnimatorListener
        {
            override fun onAnimationStart(animation: Animator?)
            {

            }

            override fun onAnimationRepeat(animation: Animator?)
            {

            }


            override fun onAnimationEnd(animation: Animator?)
            {
                scaleX = Math.max(1f, scaleX)
                scaleY = Math.max(1f, scaleY)
                setState(STATE_NORMAL)
            }

            override fun onAnimationCancel(animation: Animator?)
            {

            }


        })
    }

    private val updateListener = ValueAnimator.AnimatorUpdateListener {

        val time = Math.round(it.animatedValue as Float)
        when
        {
            time <= 100.0f ->
            {
                scaleX = Math.max(0f, 1f - time / 100f)
                scaleY = Math.max(0f, 1f - time / 100f)
            }
            time in 101..200 ->
            {
                scaleX = Math.min(1f, (time - 100) / 100f)
                scaleY = Math.min(1f, (time - 100) / 100f)
                setState(STATE_CIRCLE)

            }

            time in 201..300->
            {
                scaleX = 1f
                scaleY = 1f
                strokeWithScaleFraction= ((time-200)/100f)
                setState(STATE_RING)
                postInvalidate()
            }

            else ->
            {
                statelliteOffsetFraction=((time-300)/100f)
                setState(STATE_STATELLITE)
                postInvalidate()
            }
        }
    }

    private fun setState(newState: Int)
    {
        if (state != newState)
        {
            state = newState
            postInvalidate()
        }
    }

    override fun onAttachedToWindow()
    {
        super.onAttachedToWindow()
        initAnimator()
    }



    private fun initClickEvent()
    {
        setOnClickListener {

            if (state == STATE_NORMAL)
            {
                startAnimate()
            } else
            {

            }

        }
    }

    private fun startAnimate()
    {
        if (valueAnimator.isRunning)
        {
            return
        } else
        {
            valueAnimator.start()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        size = Math.min(widthSize, heightSize)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    }

    override fun onDraw(canvas: Canvas?)
    {
        super.onDraw(canvas)
        path.reset()
        centerX = (width / 2).toFloat()
        centerY = (height / 2).toFloat()
        if (canvas != null)
        {
            when (state)
            {
                STATE_NORMAL ->
                {
                    drawFinger(canvas)
                }
                STATE_CIRCLE ->
                {
                    drawCircle(canvas)
                }

                STATE_RING->
                {
                    drawRing(canvas)
                    drawFinger(canvas)
                }

                STATE_STATELLITE ->
                {
                    drawStatellite(canvas)
                }
            }


        }
    }

    private fun drawFinger(canvas: Canvas)
    {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        val fingerWidth = (size / 2).toFloat()
        val fingerHeight = (size / 2).toFloat()
        val centerX = (width / 2).toFloat() + fingerWidth / 8
        val centerY = (height / 2).toFloat() + fingerHeight / 8
        path.addRect(centerX - fingerWidth / 2, centerY - fingerHeight / 4, centerX - fingerWidth / 3, centerY + fingerHeight / 4, Path.Direction.CW)
        path.moveTo(centerX - fingerWidth / 3 + spaceBetweenHandAndShoulder, centerY - fingerHeight / 4)
        path.rLineTo(fingerWidth / 8, 0f)
        path.rLineTo(fingerWidth / 8, -fingerHeight / 2)
        path.rLineTo(fingerWidth / 6, fingerHeight / 4)
        path.rLineTo(-fingerWidth / 8, fingerHeight / 4)
        path.rLineTo(fingerWidth / 2 - fingerWidth / 6 - spaceBetweenHandAndShoulder, 0f)
        path.rLineTo(-fingerWidth / 8, fingerHeight / 2)
        path.lineTo(centerX - fingerWidth / 3 + spaceBetweenHandAndShoulder, centerY + fingerHeight / 4)
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawCircle(canvas: Canvas)
    {
        val radius = (size.toFloat()) / 3
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, radius, paint)
    }

    private fun drawStatellite(canvas: Canvas)
    {
        drawFinger(canvas)
        drawSmallStatellites(canvas)
//        drawRing(canvas)
    }

    private fun drawRing(canvas: Canvas)
    {
        val radius = (size.toFloat()) / 3
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = ((1 - strokeWithScaleFraction) * radius)
        canvas.drawCircle(centerX, centerY, radius-paint.strokeWidth/2, paint)
    }

    private fun drawSmallStatellites(canvas: Canvas)
    {
        paint.style = Paint.Style.FILL
        val bigRadius = (size.toFloat()) / 3
        val smallRadius = (centerY - bigRadius) / 3
        val offset=size/2-bigRadius-2*smallRadius
        canvas.drawCircle(centerX, centerY - bigRadius - offset*statelliteOffsetFraction, smallRadius, paint)
        canvas.drawCircle(centerX + bigRadius + offset*statelliteOffsetFraction , centerX, smallRadius, paint)
        canvas.drawCircle(centerX, centerY + bigRadius + offset*statelliteOffsetFraction , smallRadius, paint)
        canvas.drawCircle(centerX - bigRadius - offset*statelliteOffsetFraction , centerY, smallRadius, paint)
    }
}