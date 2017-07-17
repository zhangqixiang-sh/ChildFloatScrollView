package com.zqx.childfloatscrollview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ScrollView;

/**
 * Created by ZhangQixiang on 2017/7/17.
 * <p>
 * 参考
 * <a>http://blog.csdn.net/coderinchina/article/details/50772957</a>
 * 而改造
 */

public class ChildFloatScrollView extends FrameLayout {

    private ViewGroup mContentLayout;
    private View      mFloatView;
    private int       mFloatViewTop;
    private int       mFloatContainerId;
    private ViewGroup mFloatContainer;

    public ChildFloatScrollView(@NonNull Context context) {
        this(context, null);
    }

    public ChildFloatScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChildFloatScrollView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChildFloatScrollView);
        mFloatContainerId = a.getResourceId(R.styleable.ChildFloatScrollView_floatContainer, -1);
        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //只能有一个子view,且该子view必须是一个ViewGroup
        View child0 = getChildAt(0);
        if (!(child0 instanceof ViewGroup)) {
            throw new RuntimeException(getClass().getSimpleName() + "必须有且仅有一个子ViewGroup!");
        }

        //先移除子view,把其加进一个内部ScrollView后再add这个ScrollView
        mContentLayout = (ViewGroup) child0;
        removeAllViews();
        InnerScrollView scrollView = new InnerScrollView(getContext());
        scrollView.addView(mContentLayout);
        addView(scrollView);

        //得到悬浮View和它在contentLayout中的容器
        if (mFloatContainerId == -1) return;
        mFloatContainer = (ViewGroup) mContentLayout.findViewById(mFloatContainerId);
        mFloatView = mFloatContainer.getChildAt(0);

        //得到悬浮View的top,这点很关键
        mFloatContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFloatContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ViewGroup.LayoutParams lp = mFloatContainer.getLayoutParams();
                lp.height = mFloatContainer.getMeasuredHeight();
                mFloatContainer.setLayoutParams(lp);
                mFloatViewTop = mFloatContainer.getTop();
            }
        });

    }


    class InnerScrollView extends ScrollView {

        public InnerScrollView(Context context) {
            super(context);
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);

            if (t > mFloatViewTop && mFloatView.getParent() == mFloatContainer) {
                //1.滑动距离到达, 2.悬浮View仍附着在ScrollView上
                //切换至悬浮
                mFloatContainer.removeView(mFloatView);
                ChildFloatScrollView.this.addView(mFloatView);
            } else if (t <= mFloatViewTop && mFloatView.getParent() == ChildFloatScrollView.this) {
                //1.滑动距离未到, 2.悬浮View仍处于悬浮状态
                //切换至拆下
                ChildFloatScrollView.this.removeView(mFloatView);
                mFloatContainer.addView(mFloatView);
            }
        }


    }


}
