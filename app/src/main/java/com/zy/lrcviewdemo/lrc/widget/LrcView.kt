package com.zy.lrcviewdemo.lrc.widget

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import com.zy.lrcviewdemo.R


/**
 * @Author: fzy
 * @Date: 2022/11/3
 * @Description:增加缩放动画，通过底部增加一个无文本内容的tv并校验其高度是否大于0判定是否滑动到底部
 */
class LrcView(context: Context, attribute: AttributeSet) : LinearLayout(context, attribute) {
    /***
     * 歌词数据源
     */
    private var dataSource: MutableList<String>? = null

    /***
     * 歌词View集合
     */
    private var views: MutableList<TextView> = ArrayList()

    /***
     * 歌词竖直间距
     */
    private var mVertical = 100

    /***
     * 滑出屏幕高度
     */
    var outHeight = 0

    /***
     * 当前播放索引
     */
    var index = 0

    /***
     * 歌词滚动动画
     */
    private var mValueAnimator: ValueAnimator? = null

    /***
     * 正在播放歌词View字体大小动画
     */
    private var mScaleAnimator: ValueAnimator? = null

    /***
     * 正在播放前一句歌词View字体大小动画
     */
    private var mSmallAnimator: ValueAnimator? = null
    private lateinit var animatorSet: AnimatorSet

    /***
     * 手指触摸屏幕起点
     */
    var startY = 0f

    /***
     * 手指触摸屏幕终点
     */
    var endY = 0f

    init {
        orientation = VERTICAL
        mSmallAnimator = ValueAnimator.ofFloat(25f, 18f)
        mSmallAnimator?.run {
            addUpdateListener {
                val curValue = it.animatedValue as Float
                if (index - 1 < views.size - 1) {
                    views[index - 1].textSize = curValue
                    views[index - 1].setTextColor(context.resources.getColor(R.color.black))
                }
            }
        }
        mScaleAnimator = ValueAnimator.ofFloat(18f, 25f)
        mScaleAnimator?.run {
            addUpdateListener {
                val curValue = it.animatedValue as Float
                if (index < views.size - 1) {
                    views[index].textSize = curValue
                    views[index].setTextColor(context.resources.getColor(R.color.color_ff6f31))
                }
            }
        }
    }

    fun bindData(dataSource: MutableList<String>) {
        this.dataSource = dataSource
        addViews()
        (layoutParams as MarginLayoutParams).let {
            mValueAnimator = ValueAnimator.ofInt(it.topMargin, -(outHeight))
            mValueAnimator?.run {
                addUpdateListener { listener ->
                    if (dataSource.size - index >= 5) {
                        val curValue = listener.animatedValue as Int
                        it.topMargin = curValue
                        layoutParams = it//滚动布局
                    }
                }
            }
        }
        animatorSet = AnimatorSet()
        animatorSet.duration = 300
        animatorSet.playTogether(mSmallAnimator, mScaleAnimator, mValueAnimator)
    }


    private fun addViews() {
        dataSource?.run {
            views.clear()
            for ((k, v) in this.withIndex()) {
                val tv = TextView(context)
                tv.text = v
                if (k == 0) {//初始化第一句为正在播放状态
                    tv.textSize = 25f
                    tv.setTextColor(context.resources.getColor(R.color.color_ff6f31))
                } else {//初始化未播放状态字体、色值
                    tv.textSize = 18f
                    tv.setTextColor(context.resources.getColor(R.color.black))
                }
                tv.gravity = Gravity.CENTER
                val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                params.topMargin = mVertical
                addView(tv, params)
                views.add(tv)
            }
        }
        val bottomTv = TextView(context)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        params.topMargin = mVertical
        addView(bottomTv, params)
        views.add(bottomTv)//集合尾部放一个tv,作为是否滑动到底部的判定依据
    }

    /***
     * 更新正在播放的歌词View
     */
    fun updateLineNum(checkIndex: Int) {
        if (checkIndex == 0) {
            return
        }
        if (index >= views.size - 2) {
            return
        }
        (layoutParams as MarginLayoutParams).let {
            (context as Activity).runOnUiThread {
                outHeight = 0
                views.run {
                    if (index >= 1) {
                        for (i in 0 until index) {
                            outHeight += (views[i].measuredHeight + mVertical)//计算所有滑出屏幕歌词View高度
                        }
                    }
                    index++
                }
                mValueAnimator?.setIntValues(it.topMargin, -(outHeight))//动态改变滚动动画的初始值
                animatorSet.start()
            }
        }
    }

    /***
     * 在onTouchEvent中通过改变marginTop产生滚动效果
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.run {
            return when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = this.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    endY = this.rawY
                    (layoutParams as MarginLayoutParams).let {
                        if (it.topMargin >= 0) {//滑动到顶部
                            if (endY < startY) {
                                it.topMargin += (endY - startY).toInt()
                                layoutParams = it
                            }
                        } else if (views[views.size - 1].height != 0) {//滑动到底部
                            if (endY > startY) {
                                it.topMargin += (endY - startY).toInt()
                                layoutParams = it
                            }
                        } else {
                            it.topMargin += (endY - startY).toInt()
                            layoutParams = it
                        }
                    }
                    startY = this.rawY
                    true
                }
                else -> {
                    super.onTouchEvent(event)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animatorSet.cancel()
        mValueAnimator?.run {
            this.cancel()
        }
        mScaleAnimator?.run {
            this.cancel()
        }
        mSmallAnimator?.run {
            this.cancel()
        }
    }

}