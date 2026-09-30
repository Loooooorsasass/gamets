package com.example;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;

/**
 * Root wrapper for MazeGameView.
 */
public class MazeGameView extends com.example.ui.components.MazeGameView {
    public MazeGameView(Context context) {
        super(context);
    }

    public MazeGameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MazeGameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
