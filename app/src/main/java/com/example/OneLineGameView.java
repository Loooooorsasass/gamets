package com.example;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;

/**
 * Convenience wrapper for {@link com.example.ui.components.OneLineGameView}.
 */
public class OneLineGameView extends com.example.ui.components.OneLineGameView {

    public OneLineGameView(Context context) {
        super(context);
    }

    public OneLineGameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OneLineGameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
