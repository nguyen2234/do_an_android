package com.example.android_app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class BarChartView extends View {

    private Paint barPaint, textPaint, linePaint;
    private float[] data = {0, 0, 0, 0, 0, 0, 0};
    private String[] labels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
    private float maxVal = 1000000;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int colorPrimary;
        try {
            colorPrimary = getContext().getResources().getColor(com.example.android_app.R.color.colorPrimary);
        } catch (Exception e) {
            colorPrimary = Color.parseColor("#10B981");
        }
        barPaint.setColor(colorPrimary);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#6B7280"));
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#E5E7EB"));
        linePaint.setStrokeWidth(2f);
    }

    public void setData(float[] newData, String[] newLabels) {
        this.data = newData;
        this.labels = newLabels;
        this.maxVal = 0;
        for (float val : data) {
            if (val > maxVal) maxVal = val;
        }
        if (maxVal == 0) maxVal = 1000000; // Tránh chia cho 0
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int paddingBottom = 60;
        int paddingTop = 40;
        int chartHeight = height - paddingBottom - paddingTop;

        int barCount = data.length;
        if (barCount == 0) return;

        float barGroupWidth = (float) width / barCount;
        float barWidth = barGroupWidth * 0.5f;

        // Draw horizontal guide lines
        for (int i = 0; i <= 4; i++) {
            float y = paddingTop + chartHeight - (chartHeight * i / 4f);
            canvas.drawLine(0, y, width, y, linePaint);
        }

        // Draw bars
        for (int i = 0; i < barCount; i++) {
            float centerX = barGroupWidth * i + barGroupWidth / 2f;

            float ratio = data[i] / maxVal;
            float barHeight = chartHeight * ratio;
            
            float left = centerX - barWidth / 2;
            float top = paddingTop + chartHeight - barHeight;
            float right = centerX + barWidth / 2;
            float bottom = paddingTop + chartHeight;

            if (data[i] > 0) {
                RectF rect = new RectF(left, top, right, bottom);
                // Highlight cột cao nhất hoặc dùng màu mặc định
                barPaint.setAlpha(data[i] == maxVal && maxVal > 0 ? 255 : 180);
                canvas.drawRoundRect(rect, 12, 12, barPaint);
            }

            // Label
            canvas.drawText(labels[i], centerX, height - 15, textPaint);
        }
    }
}
