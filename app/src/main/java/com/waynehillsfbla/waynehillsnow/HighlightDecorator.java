package com.waynehillsfbla.waynehillsnow;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.List;

/**
 * Created by Kartik on 4/27/2015.
 */
public class HighlightDecorator implements DayViewDecorator {

    List<CalendarDay> dates;

    public HighlightDecorator(List<CalendarDay> dates) {
        this.dates = dates;
    }

    private static Drawable generateBackgroundDrawable() {
        final int r = 0;
        final float[] outerR = new float[]{r, r, r, r, r, r, r, r};
        final int color = Color.parseColor("#660033");

        RoundRectShape rr = new RoundRectShape(outerR, null, null);

        ShapeDrawable drawable = new ShapeDrawable(rr);
        drawable.setShaderFactory(new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                return new LinearGradient(0, 0, 0, 0, color, color, Shader.TileMode.REPEAT);
            }
        });
        return drawable;
    }

    @Override
    public boolean shouldDecorate(CalendarDay calendarDay) {
        return dates.contains(calendarDay);
    }

    @Override
    public void decorate(DayViewFacade dayViewFacade) {
        Log.e("day view", dayViewFacade.getDate().toString());
        dayViewFacade.setBackgroundUnselected(generateBackgroundDrawable());
    }
}
