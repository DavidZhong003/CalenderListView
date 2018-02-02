package com.vanke.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.support.annotation.ColorInt
import android.support.annotation.IntRange
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.Serializable
import java.util.*
import kotlin.math.min


/**
 * 获得年份
 */
private fun Calendar.getYear() = get(Calendar.YEAR)

/**
 * 获得月份
 */
private fun Calendar.getMonth() = get(Calendar.MONTH) + 1

/**
 * 获取一个月有多少天
 */
private fun Calendar.getMonthDays() = getActualMaximum(Calendar.DATE)

/**
 * 转为dp
 */
private fun View.dp(int: Int) = (int * context.resources.displayMetrics.density).toInt()

/**
 * 转为sp
 */
private fun View.sp(int: Int) = (int * context.resources.displayMetrics.scaledDensity).toInt()

/**
 * 转为颜色
 */
private fun toColor(string: String) = Color.parseColor(string)

/**
 * 获取某天是星期几
 * @param firstDayWeek 这个月第一天星期几
 * @return 0周日....
 */
private fun getDayWeek(@IntRange(from = 1, to = 31) day: Int, @IntRange(from = 0, to = 6) firstDayWeek: Int) = (day + firstDayWeek - 1) % 7

/**
 * 获取某天属于第几个礼拜
 */
private fun getWeekIndex(day: Int, @IntRange(from = 0, to = 6) firstDayWeek: Int) = (day + firstDayWeek - 1) / 7

/**
 * 获取今天日期(day)
 */
private fun getTodayNumber() = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

/**
 * 判断某日是否未来
 */
private fun isAfterToday(day: Int) = day > getTodayNumber()

/**
 * 判断某日是否是周末
 */
private fun isWeek(int: Int, @IntRange(from = 0, to = 6) firstDayWeek: Int) = getDayWeek(int, firstDayWeek) == 0 || getDayWeek(int, firstDayWeek) == 6

/**
 * 月份View
 * @author doive
 * on 2018/1/31 16:15
 */

class MonthView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private val calendar: Calendar

    private var selectDayRange: kotlin.ranges.IntRange = (getTodayNumber()..getTodayNumber())

    private val monthDay: Int
    private var year: Int
    private var month: Int


    @IntRange(from = 0, to = 6)
    private var firstDayWeek: Int

    private var monthTextSize = sp(14)
    private var dayTextSize = sp(16)

    //颜色区域
    @ColorInt
    val WEEK_COLOR = toColor("#e95c54")
    @ColorInt
    val DAY_COLOR = toColor("#333333")
    @ColorInt
    val MONTH_COLOR = toColor("#7d7d7d")
    @ColorInt
    val LINE_COLOR = toColor("#e5e5e5")
    @ColorInt
    val SELECT_TEXT_COLOR = toColor("#ffffff")
    @ColorInt
    val BACK_COLOR = toColor("#04befe")
    @ColorInt
    val DISSELECTABLE_COLOR = toColor("#e5e5e5")

    private var dayPainter: DayDraw = DayDrawer()

    private var simpleDay: SimpleDay

    private val monthPaint: Paint = Paint().apply {
        isAntiAlias = true
        textSize = monthTextSize.toFloat()
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
        color = MONTH_COLOR
    }

    private val dayPaint = Paint().apply {
        isAntiAlias = true
        textSize = dayTextSize.toFloat()
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
        color = DAY_COLOR
    }

    private val backShapePaint = Paint().apply {
        isAntiAlias = true
        textSize = dayTextSize.toFloat()
        style = Paint.Style.FILL
        color = BACK_COLOR
    }

    init {
        calendar = Calendar.getInstance(Locale.getDefault())
        simpleDay = SimpleDay(calendar.getYear(), calendar.getMonth(), 1)
        //获取一个月天数
        monthDay = calendar.getMonthDays()
        year = calendar.getYear()
        month = calendar.getMonth()
        //获取1号星期几
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        firstDayWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

    }

    private var drawWidth = 0f
    private val monthHeight = dp(44)
    private val dayBlockH = dp(49)
    private var dayBlockW = 0.0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //获取view 的宽度
        drawWidth = (w - paddingLeft - paddingRight).toFloat()
        //每个日期块的宽度
        dayBlockW = drawWidth / 7
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        //画月份
        drawMonthTitle(canvas)
        //画日期
        drawDayNumbers(canvas)
    }

    private fun drawDayNumbers(canvas: Canvas) {
        val startX = paddingLeft
        val startY = monthHeight
        (1..monthDay).forEach {
            if (isDayInSelect(it)) {
                //画选择后的背景
                dayPainter.drawSelectDay(canvas, backShapePaint, it, startX + getDayWeek(it, firstDayWeek) * dayBlockW,
                        startY + getWeekIndex(it, firstDayWeek) * dayBlockH.toFloat(),
                        dayBlockW, dayBlockH.toFloat())
            }
            dayPainter.drawDay(canvas, dayPaint.apply { color = getDayPaintTextColor(it) }, it,
                    startX + getDayWeek(it, firstDayWeek) * dayBlockW,
                    startY + getWeekIndex(it, firstDayWeek) * dayBlockH.toFloat(),
                    dayBlockW, dayBlockH.toFloat())
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            //过滤无效位置
            if (event.x < paddingLeft || event.x > width - paddingRight || event.y < monthHeight) {
                return true
            }
            val day = convertLocationToDay(event.x, event.y)

            if (day > 0) {
                dayClickListener?.onDayClick(year, month, day)
//                invalidate()
            }

        }
        return true
    }

    private fun convertLocationToDay(upX: Float, upY: Float): Int {
        //获取第几行
        val row = ((upY - monthHeight) / dayBlockH).toInt()
        //第几列
        val column = ((upX - paddingLeft) / dayBlockW).toInt()
        val day = row * 7 + column - firstDayWeek + 1

        val cX = paddingLeft + column * dayBlockW + dayBlockW / 2
        val cY = monthHeight + row * dayBlockH + dayBlockH / 2

        if ((row == 0 && column < firstDayWeek) || day > monthDay) {
            return 0
        }
        return if (upX in (cX - dayBlockW / 2.5f..cX + dayBlockW / 2.5f) && upY in (cY - dayBlockH / 2.5f..cY + dayBlockH / 2.5f)) {
            //在中心区域附近
            day
        } else {
            -1
        }
    }


    private fun getDayPaintTextColor(day: Int) = when {
        isAfterToday(day) -> DISSELECTABLE_COLOR
        isDayInSelect(day) -> SELECT_TEXT_COLOR
        isWeek(day, firstDayWeek) -> WEEK_COLOR
        else -> DAY_COLOR
    }

    private fun isDayInSelect(day: Int) = day in selectDayRange

    private fun drawMonthTitle(canvas: Canvas) {
        //先两条虚线
        val lineX = drawWidth / 30f
        val lineLength = drawWidth / 3.02f
        val startY = dp(22).toFloat()
        canvas.drawText(getMonthYearText(), drawWidth / 2, startY + monthPaint.fontMetricsInt.bottom, monthPaint.apply { color = MONTH_COLOR })
        //两条线
        canvas.drawLine(lineX, startY, lineX + lineLength, startY, monthPaint.apply { color = LINE_COLOR })
        canvas.drawLine(drawWidth - lineX, startY, drawWidth - lineX - lineLength, startY, monthPaint)
    }

    private fun getMonthYearText(): String = "${calendar.getYear()}年${calendar.getMonth()}月"

    interface OnDayClickListener {
        fun onDayClick(year: Int, month: Int, day: Int)
    }

    var dayClickListener: OnDayClickListener? = null
}

data class SimpleDay(var year: Int, var month: Int, var day: Int) : Serializable

interface DayDraw {
    /**
     * 画天数
     * @param startX 起始x
     * @param startY 起始y
     * @param width  宽度
     * @param height 高度
     */
    fun drawDay(canvas: Canvas, paint: Paint, day: Int, startX: Float, startY: Float, width: Float, height: Float)

    fun drawSelectDay(canvas: Canvas, backShapePaint: Paint, day: Int, startX: Float, startY: Float, width: Float, height: Float)
}

/**
 * 主要画天数实现类
 */
class DayDrawer : DayDraw {
    override fun drawSelectDay(canvas: Canvas, backShapePaint: Paint, day: Int, startX: Float, startY: Float, width: Float, height: Float) {
        canvas.drawCircle(startX + width / 2, startY + height / 2, min(width / 2.5f, height / 2.5f), backShapePaint)
    }

    override fun drawDay(canvas: Canvas, paint: Paint, day: Int, startX: Float, startY: Float, width: Float, height: Float) {
        canvas.drawText(day.toString(), startX + width / 2, startY + height / 2 + paint.fontMetricsInt.bottom, paint)
    }

}
