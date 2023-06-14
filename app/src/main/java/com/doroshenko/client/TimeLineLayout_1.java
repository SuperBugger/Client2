package com.doroshenko.client;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
public class TimeLineLayout_1 extends ScrollView {
    private FrameLayout frameLayout;

    public TimeLineLayout_1(Context context) {
        super(context);
    }

    public TimeLineLayout_1(Context context, AttributeSet attrs) {
        super(context, attrs);
        frameLayout = new FrameLayout(context);
        frameLayout.addView(new TimeLineLayoutGroup_1(context, attrs));
        addView(frameLayout);
        post(() -> frameLayout.requestLayout());
    }

    public <T extends Event_1> void addEvent(EventView_1<T> child) {
        ((ViewGroup) frameLayout.getChildAt(0)).addView(child);
    }

}