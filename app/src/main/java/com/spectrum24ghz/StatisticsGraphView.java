package com.spectrum24ghz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatisticsGraphView extends View {

    private Paint textPaint;
    private Paint titlePaint;
    private Paint subtitlePaint;
    private Paint barPaint;
    private Paint gridPaint;
    private Paint bgPaint;

    // Data
    private int[] qualityCounts = new int[4]; // Excellent, Good, Medium, Weak
    private int[] channelCounts = new int[14]; // Ch 1 to 14
    private Map<String, Integer> securityCounts = new LinkedHashMap<>();

    private int maxQualityCount = 0;
    private int maxChannelCount = 0;
    private int totalNetworks = 0;

    // Colors
    private int colorExcellent;
    private int colorGood;
    private int colorMedium;
    private int colorWeak;

    public StatisticsGraphView(Context context) {
        super(context);
        init(context);
    }

    public StatisticsGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#757575"));
        textPaint.setTextSize(32);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.parseColor("#1A1A1A"));
        titlePaint.setTextSize(40);
        titlePaint.setFakeBoldText(true);

        subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtitlePaint.setColor(Color.parseColor("#9E9E9E"));
        subtitlePaint.setTextSize(28);

        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E0E0E0"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#FFFFFF"));
        bgPaint.setStyle(Paint.Style.FILL);

        colorExcellent = ContextCompat.getColor(context, R.color.sig_strong);
        colorGood = ContextCompat.getColor(context, R.color.sig_good);
        colorMedium = ContextCompat.getColor(context, R.color.sig_medium);
        colorWeak = ContextCompat.getColor(context, R.color.sig_weak);
    }

    public void updateData(int[] qCounts, int[] cCounts, Map<String, Integer> sCounts) {
        this.qualityCounts = qCounts != null ? qCounts : new int[4];
        this.channelCounts = cCounts != null ? cCounts : new int[14];
        this.securityCounts.clear();
        if (sCounts != null) {
            this.securityCounts.putAll(sCounts);
        }

        maxQualityCount = 0;
        totalNetworks = 0;
        for (int q : qualityCounts) {
            if (q > maxQualityCount) maxQualityCount = q;
            totalNetworks += q;
        }

        maxChannelCount = 0;
        for (int c : channelCounts) {
            if (c > maxChannelCount) maxChannelCount = c;
        }

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = 2200; // Increased height for extra text
        setMeasuredDimension(width, desiredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        if (width == 0 || totalNetworks == 0) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No hay datos suficientes para estadísticas", width / 2f, 200, textPaint);
            return;
        }

        int paddingH = 60;
        int currentY = 80;

        // --- Resumen General ---
        titlePaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Resumen General", paddingH, currentY, titlePaint);
        currentY += 45;
        canvas.drawText("Se encontraron " + totalNetworks + " redes en tu área.", paddingH, currentY, subtitlePaint);
        currentY += 60;
        canvas.drawLine(paddingH, currentY, width - paddingH, currentY, gridPaint);
        currentY += 80;

        // --- 1. Calidad de Señal (Horizontal Bar Chart) ---
        canvas.drawText("Calidad de Señal", paddingH, currentY, titlePaint);
        currentY += 45;
        canvas.drawText("Indica qué tan fuerte llega el Wi-Fi a tu dispositivo.", paddingH, currentY, subtitlePaint);
        currentY += 70;

        String[] qLabels = {"Excelente", "Buena", "Regular", "Débil"};
        int[] qColors = {colorExcellent, colorGood, colorMedium, colorWeak};
        int barMaxWidth = width - paddingH * 2 - 200;

        for (int i = 0; i < 4; i++) {
            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(qLabels[i], paddingH, currentY + 30, textPaint);

            int count = qualityCounts[i];
            float barW = maxQualityCount > 0 ? (float) count / maxQualityCount * barMaxWidth : 0;
            
            barPaint.setColor(qColors[i]);
            RectF barRect = new RectF(paddingH + 180, currentY, paddingH + 180 + barW, currentY + 40);
            canvas.drawRoundRect(barRect, 8, 8, barPaint);

            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(String.valueOf(count), barRect.right + 20, currentY + 30, textPaint);

            currentY += 80;
        }

        currentY += 60;
        canvas.drawLine(paddingH, currentY, width - paddingH, currentY, gridPaint);
        currentY += 100;

        // --- 2. Distribución de Canales (Vertical Histogram) ---
        canvas.drawText("Uso de Canales", paddingH, currentY, titlePaint);
        currentY += 45;
        canvas.drawText("Canales con menos redes tendrán menos interferencia.", paddingH, currentY, subtitlePaint);
        currentY += 70;

        int histHeight = 400;
        int histWidth = width - paddingH * 2;
        int numChannels = 14;
        float barWidth = (float) histWidth / numChannels - 10;
        
        for (int i = 0; i < numChannels; i++) {
            int count = channelCounts[i];
            float h = maxChannelCount > 0 ? (float) count / maxChannelCount * histHeight : 0;
            float left = paddingH + i * (barWidth + 10);
            float top = currentY + histHeight - h;
            float right = left + barWidth;
            float bottom = currentY + histHeight;

            // Color code channels
            if (i == 0 || i == 5 || i == 10) barPaint.setColor(colorExcellent); // 1, 6, 11
            else barPaint.setColor(colorGood);

            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, 4, 4, barPaint);

            // Channel number below
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(26);
            canvas.drawText(String.valueOf(i + 1), left + barWidth / 2, bottom + 40, textPaint);
            
            // Value above bar if > 0
            if (count > 0) {
                canvas.drawText(String.valueOf(count), left + barWidth / 2, top - 15, textPaint);
            }
        }
        
        currentY += histHeight + 100;
        canvas.drawLine(paddingH, currentY, width - paddingH, currentY, gridPaint);
        currentY += 100;

        // --- 3. Tipos de Seguridad (Horizontal Bars/Stats) ---
        textPaint.setTextSize(32);
        titlePaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Tipos de Seguridad", paddingH, currentY, titlePaint);
        currentY += 45;
        canvas.drawText("Nivel de protección. Evita conectarte a redes 'Abiertas'.", paddingH, currentY, subtitlePaint);
        currentY += 70;

        int secBarMaxWidth = width - paddingH * 2 - 350;
        int maxSecCount = 0;
        for (int val : securityCounts.values()) {
            if (val > maxSecCount) maxSecCount = val;
        }

        for (Map.Entry<String, Integer> entry : securityCounts.entrySet()) {
            String label = entry.getKey();
            int count = entry.getValue();

            textPaint.setTextAlign(Paint.Align.LEFT);
            if (label.length() > 18) label = label.substring(0, 16) + "...";
            canvas.drawText(label, paddingH, currentY + 30, textPaint);

            float barW = maxSecCount > 0 ? (float) count / maxSecCount * secBarMaxWidth : 0;
            
            if (label.toLowerCase().contains("open")) {
                barPaint.setColor(colorWeak);
            } else if (label.toLowerCase().contains("wpa3")) {
                barPaint.setColor(colorExcellent);
            } else {
                barPaint.setColor(Color.parseColor("#448AFF")); // Blue for standard WPA2
            }

            RectF barRect = new RectF(paddingH + 300, currentY, paddingH + 300 + barW, currentY + 40);
            canvas.drawRoundRect(barRect, 8, 8, barPaint);

            textPaint.setTextAlign(Paint.Align.LEFT);
            float percentage = (float) count / totalNetworks * 100;
            String valStr = count + " (" + String.format("%.1f", percentage) + "%)";
            canvas.drawText(valStr, barRect.right + 20, currentY + 30, textPaint);

            currentY += 80;
        }
    }
}
