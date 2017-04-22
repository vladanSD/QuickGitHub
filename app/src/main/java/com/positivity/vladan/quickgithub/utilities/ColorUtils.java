package com.positivity.vladan.quickgithub.utilities;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.positivity.vladan.quickgithub.R;




    public class ColorUtils {

        public static int getViewHolderBackgroundColorFromInstance(Context context, int instanceNum) {
            switch (instanceNum) {
                case 1:
                    return ContextCompat.getColor(context, R.color.material100Green);
                case 2:
                    return ContextCompat.getColor(context, R.color.material150Green);
                default:
                    return Color.WHITE;
            }
        }
    }

