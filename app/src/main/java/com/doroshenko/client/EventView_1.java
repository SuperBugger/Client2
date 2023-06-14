package com.doroshenko.client;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class EventView_1<T extends Event_1> extends FrameLayout {

    private float startTime = 0f;
    private float endTime = 0f;
    private T event;
    private int marginBetweenItems = 0;
    private int layoutResourceId = 0;
    private SetupViewListener setupViewListener = view -> {};
    private OnClickListener<T> onClickListener = event -> {};

    public EventView_1(Context context, T event, int itemsMargin, int layoutResourceId,
                       SetupViewListener setupView, OnClickListener<T> onClick) {
        super(context);
        this.startTime = event.getStartTime();
        this.endTime = event.getEndTime();
        this.event = event;
        this.marginBetweenItems = itemsMarginToPx(itemsMargin);
        this.layoutResourceId = layoutResourceId;
        this.setupViewListener = setupView;
        this.onClickListener = onClick;
        setVisibility(View.INVISIBLE);
        init();
    }

    public EventView_1(Context context, AttributeSet attrs) {
        super(context, attrs);
        throw new UnsupportedOperationException("Adding EventView from layout not supported in this version.");
    }

    private void init() {
        setPadding(marginBetweenItems, marginBetweenItems, marginBetweenItems, marginBetweenItems);

        View view = View.inflate(getContext(), layoutResourceId, (ViewGroup) getParent());
        view.setOnClickListener(v -> onClickListener.onClick(event));
        addView(view);

        setupViewListener.setupView(view);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        TimeLineLayoutGroup_1 parent = (TimeLineLayoutGroup_1) getParent();
        int oneHourHeight = dpToPx(parent.getEachHourHeightInDp());
        float minDelta = parent.getMinimumHeightEachSellPercentage();
        float calculatedHeight;

        if (endTime - startTime < minDelta) {
            calculatedHeight = oneHourHeight * minDelta;
        } else {
            calculatedHeight = (endTime - startTime) * oneHourHeight;
        }

        super.onMeasure(50, MeasureSpec.makeMeasureSpec((int) calculatedHeight, MeasureSpec.EXACTLY));
    }

    public Pair<Float, Float> getEventTime() {
        return new Pair<>(startTime, endTime);
    }

    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int itemsMarginToPx(int margin) {
        return dpToPx(margin);
    }
    public interface OnClickListener<T extends Event_1> {
        void onClick(T event);
    }
    public interface SetupViewListener {
        void setupView(View view);
    }
}