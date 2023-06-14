package com.doroshenko.client;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class TimeLineLayoutGroup_1 extends ViewGroup {
    private float eachHourHeightInDp = 118f;
    private float minimumHeightEachSellPercentage = .25f;
    private int maxChildrenEnd = 0;
    private int numberOfRows = 14;
    private List<String> times = new ArrayList<>();
    private float dividerStartOffset = 108f;

    private Paint dividerPaint;
    private Paint textPaint;
    private int dividerColorId = R.color.divider_color;
    private int dividerTextColorId = R.color.title_color;
    private List<String> dividerTitles;
    public float getMinimumHeightEachSellPercentage() {
        return minimumHeightEachSellPercentage;
    }

    public float getEachHourHeightInDp() {
        return eachHourHeightInDp;
    }

    public TimeLineLayoutGroup_1(Context context) {
        super(context);
        init(null);
    }

    public TimeLineLayoutGroup_1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @SuppressLint({"CustomViewStyleable", "ResourceAsColor"})
    private void init(AttributeSet set) {
        setWillNotDraw(false);
        setLayoutTransition(new LayoutTransition());

        for (int hour = 9; hour <= 22; hour++) {
            String time = String.format("%02d:00", hour);
            times.add(time);
        }

        dividerTitles = new ArrayList<>();
        for (int i = 0; i < numberOfRows; i++) {
            dividerTitles.add(times.get(i));
        }

        if (set == null) {
            dividerPaint = new Paint();
            dividerPaint.setStrokeWidth(1f);
            dividerPaint.setStyle(Paint.Style.FILL);
            dividerPaint.setColor(ContextCompat.getColor(getContext(), dividerColorId));

            textPaint = new Paint();
            textPaint.setColor(ContextCompat.getColor(getContext(), dividerTextColorId));
            textPaint.setTextSize(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 16f,
                    getResources().getDisplayMetrics()
            ));

            return;
        }

        TypedArray ta = getContext().obtainStyledAttributes(set, R.styleable.TimeLineLayout);
        eachHourHeightInDp = ta.getDimension(R.styleable.TimeLineLayout_eachRowHeight, 118f);
        minimumHeightEachSellPercentage = ta.getFloat(R.styleable.TimeLineLayout_minimumPercentage, .25f);
        numberOfRows = ta.getInteger(R.styleable.TimeLineLayout_numberOfRows, 14);
        dividerColorId = ta.getColor(R.styleable.TimeLineLayout_dividerColor, Color.BLACK);
        dividerTextColorId = ta.getColor(R.styleable.TimeLineLayout_dividerTextColor, Color.WHITE);
        dividerStartOffset = ta.getDimension(R.styleable.TimeLineLayout_dividerStartOffset, 108f);
        ta.recycle();

        dividerPaint = new Paint();
        dividerPaint.setStrokeWidth(1f);
        dividerPaint.setStyle(Paint.Style.FILL);
        dividerPaint.setColor(dividerColorId);

        textPaint = new Paint();
        textPaint.setColor(dividerTextColorId);
        textPaint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16f,
                getResources().getDisplayMetrics()
        ));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        post(() -> {
            if (getChildCount() > 0) {
                getChildAt(getChildCount() - 1).setVisibility(View.VISIBLE);
            }
        });
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(
                Math.max(getResources().getDisplayMetrics().widthPixels, maxChildrenEnd + (int) toPx(dividerStartOffset)),
                (int) (toPx(eachHourHeightInDp) * numberOfRows)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDividersAndTimeCaption(canvas);
    }

    private void drawDividersAndTimeCaption(Canvas canvas) {
        float eachHourHeightInPx = toPx(eachHourHeightInDp);
        float dividerInPx = toPx(dividerStartOffset);

        for (int i = 0; i < numberOfRows; i++) {
            float y = i * eachHourHeightInPx;
            Rect rect = new Rect();
            textPaint.getTextBounds(dividerTitles.get(i), 0, dividerTitles.get(i).length(), rect);

            canvas.drawText(
                    dividerTitles.get(i),
                    (dividerInPx - rect.width()) / 2,
                    y + eachHourHeightInPx / 2 + rect.height() / 2,
                    textPaint
            );

            canvas.drawLine(0f, y, getMeasuredWidth(), y, dividerPaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        maxChildrenEnd = 0;
        int dividerStartOffsetPx = (int) toPx(dividerStartOffset);
        for (int i = 0; i < getChildCount(); i++) {
            EventView_1<Event_1> child = (EventView_1<Event_1>) getChildAt(i);
            child.setTop(convertMinuteToPx(child.getEventTime().first));
            child.setLeft(calculateNewChildPlaceLeft(i, child));
            child.setLeft(child.getLeft() < dividerStartOffsetPx ? dividerStartOffsetPx : child.getLeft());
            child.layout(
                    child.getLeft(), child.getTop(),
                    child.getLeft() + child.getMeasuredWidth(),
                    child.getTop() + child.getMeasuredHeight()
            );

            if (child.getRight() > maxChildrenEnd) {
                maxChildrenEnd = child.getRight();
            }
        }
        post(this::requestLayout);
    }

    private int calculateNewChildPlaceLeft(int index, EventView_1<Event_1> child) {
        for (int i = 0; i < index; i++) {
            EventView_1<Event_1> childToCheckForSpace = (EventView_1<Event_1>) getChildAt(i);

            if (!hasCommonTime(child.getEventTime(), childToCheckForSpace.getEventTime())
                    || child.getLeft() + child.getMeasuredWidth() < childToCheckForSpace.getLeft()) {
                continue;
            }

            if (child.getLeft() < childToCheckForSpace.getRight()) {
                child.setLeft(childToCheckForSpace.getRight());
                calculateNewChildPlaceLeft(index, child);
            }
        }
        return child.getLeft();
    }

    private boolean hasCommonTime(Pair<Float, Float> firstRange, Pair<Float, Float> secondRange) {
        return !(firstRange.first >= secondRange.second || firstRange.second <= secondRange.first);
    }

    private int convertMinuteToPx(float startTime) {
        return (int) (startTime * toPx(eachHourHeightInDp));
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    private int toPx(float dp) {
        Resources r = getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}
