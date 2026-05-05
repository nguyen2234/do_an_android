package com.example.android_app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class BarChartView extends View {

    private Paint barPaint, textPaint, linePaint, incomeBarPaint;
    private float[] expenseData = {1200000, 800000, 1500000, 500000, 2500000, 1800000, 900000};
    private float[] incomeData = {8000000, 0, 0, 0, 0, 0, 0};
    private String[] labels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
    private float maxVal = 2500000;

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
        barPaint.setColor(Color.parseColor("#634832"));

        incomeBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        incomeBarPaint.setColor(Color.parseColor("#2ECC71"));
        incomeBarPaint.setAlpha(180);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#6B7280"));
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#E5E7EB"));
        linePaint.setStrokeWidth(2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int paddingBottom = 50;
        int paddingTop = 20;
        int chartHeight = height - paddingBottom - paddingTop;

        int barCount = expenseData.length;
        float totalWidth = width;
        float barGroupWidth = totalWidth / barCount;
        float barWidth = barGroupWidth * 0.35f;
        float gap = barGroupWidth * 0.05f;

        // Draw horizontal guide lines
        for (int i = 0; i <= 3; i++) {
            float y = paddingTop + chartHeight - (chartHeight * i / 3f);
            canvas.drawLine(0, y, width, y, linePaint);
        }

        // Draw bars
        for (int i = 0; i < barCount; i++) {
            float centerX = barGroupWidth * i + barGroupWidth / 2f;

            // Expense bar
            float expRatio = expenseData[i] / maxVal;
            float expBarHeight = chartHeight * expRatio;
            float expLeft = centerX - barWidth - gap / 2;
            float expTop = paddingTop + chartHeight - expBarHeight;
            float expRight = centerX - gap / 2;
            float expBottom = paddingTop + chartHeight;

            RectF expRect = new RectF(expLeft, expTop, expRight, expBottom);
            barPaint.setAlpha(i == 4 ? 255 : 180); // Highlight current highest
            canvas.drawRoundRect(expRect, 8, 8, barPaint);

            // Label
            canvas.drawText(labels[i], centerX, height - 10, textPaint);
        }
    }
}
