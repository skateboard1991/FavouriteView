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

    private lateinit var statellitePaint: Paint

    private lateinit var path: Path

    private var spaceBetweenHandAndShoulder = 5

    private var state = STATE_NORMAL

    private lateinit var valueAnimator: ValueAnimator

    private var size = 0

    private var centerX = 0f

    private var centerY = 0f

    private var strokeWithScaleFraction = 1f

    private var statelliteOffsetFraction = 0f

    private var selectedColor = Color.BLACK

    private var normalColor = Color.BLACK

    companion object
    {
        val STATE_SELECTED = 1

        val STATE_CIRCLE = 2

        val STATE_RING = 3

        val STATE_STATELLITE = 4

        val STATE_NORMAL = 0
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)

    init
    {
        if (attrs != null)
        {
            initParse(attrs)
        }
        initPaint()
        initStatellitePaint()
        initClickEvent()
    }

    private fun initParse(attrs: AttributeSet)
    {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FavouriteView)
        selectedColor = typedArray.getColor(R.styleable.FavouriteView_selected_color, Color.BLACK)
        normalColor = typedArray.getColor(R.styleable.FavouriteView_normal_color, Color.BLACK)
        typedArray.recycle()
    }

    private fun initPaint()
    {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = normalColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        path = Path()
        val cornerPathEffect = CornerPathEffect(10f)
        paint.pathEffect = cornerPathEffect
    }

    private fun initStatellitePaint()
    {
        statellitePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        statellitePaint.color = selectedColor
        statellitePaint.style=Paint.Style.FILL
    }

    private fun initAnimator()
    {
        valueAnimator = ValueAnimator.ofFloat(0f, 400f)
        valueAnimator.duration = 500
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
                setState(STATE_SELECTED)
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

            time in 201..300 ->
            {
                scaleX = 1f
                scaleY = 1f
                strokeWithScaleFraction = ((time - 200) / 100f)
                setState(STATE_RING)
                postInvalidate()
            }

            else ->
            {
                statelliteOffsetFraction = ((time - 300) / 100f)
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
                setState(STATE_NORMAL)
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
        prepareToDraw()
        if (canvas != null)
        {
            when (state)
            {
                STATE_NORMAL ->
                {
                    resetPaintColor()
                    drawFinger(canvas)
                }
                STATE_SELECTED ->
                {
                    resetPaintColor()
                    drawFinger(canvas)
                }

                STATE_CIRCLE ->
                {
                    resetPaintColor()
                    drawCircle(canvas)
                }

                STATE_RING ->
                {

                    resetPaintColor()
                    drawRing(canvas)
                    drawFinger(canvas)
                }

                STATE_STATELLITE ->
                {

                    resetPaintColor()
                    drawStatellite(canvas)
                }
            }

        }
    }

    private fun resetPaintColor()
    {
        when
        {

            state == STATE_NORMAL ->
            {
                paint.colorFilter = null
                paint.color = normalColor
            }

            state != STATE_STATELLITE ->
            {
                paint.colorFilter = null
                paint.color = selectedColor
            }

            else ->
            {
                paint.color = selectedColor
                if (statelliteOffsetFraction <= 0.5f)
                {
                    val parameter = 1f
                    statellitePaint.colorFilter = ColorMatrixColorFilter(floatArrayOf(1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, parameter, 0f))
                } else
                {
                    val parameter = 1 - statelliteOffsetFraction
                    statellitePaint.colorFilter = ColorMatrixColorFilter(floatArrayOf(1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, parameter, 0f))
                }
            }
        }
    }


    private fun prepareToDraw()
    {
        path.reset()
        centerX = (width / 2).toFloat()
        centerY = (height / 2).toFloat()
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
    }

    private fun drawRing(canvas: Canvas)
    {
        val radius = (size.toFloat()) / 3
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = ((1 - strokeWithScaleFraction) * radius)
        canvas.drawCircle(centerX, centerY, radius - paint.strokeWidth / 2, paint)
    }

    private fun drawSmallStatellites(canvas: Canvas)
    {
        val bigRadius = (size.toFloat()) / 3
        val smallRadius = (centerY - bigRadius) / 3
        val offset = size / 2 - bigRadius - 2 * smallRadius
        canvas.drawCircle(centerX, centerY - bigRadius - offset * statelliteOffsetFraction, smallRadius, statellitePaint)
        canvas.drawCircle(centerX + bigRadius + offset * statelliteOffsetFraction, centerY, smallRadius, statellitePaint)
        canvas.drawCircle(centerX, centerY + bigRadius + offset * statelliteOffsetFraction, smallRadius, statellitePaint)
        canvas.drawCircle(centerX - bigRadius - offset * statelliteOffsetFraction, centerY, smallRadius, statellitePaint)
    }
}