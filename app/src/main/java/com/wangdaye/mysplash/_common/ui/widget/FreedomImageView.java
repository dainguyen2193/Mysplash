package com.wangdaye.mysplash._common.ui.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash._common.utils.DisplayUtils;

/**
 * Freedom image view.
 * */

public class FreedomImageView extends ImageView {
    // widget
    private Paint paint;

    // data
    private float width = 1;
    private float height = 0.666F;
    private boolean coverMode = false;
    private boolean showShadow = false;

    private String textPosition;
    private static final String POSITION_NONE = "none";
    private static final String POSITION_TOP = "top";
    private static final String POSITION_BOTTOM = "bottom";

    /** <br> life cycle. */

    public FreedomImageView(Context context) {
        super(context);
    }

    public FreedomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize(context, attrs, 0, 0);
    }

    public FreedomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FreedomImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(Context c, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.FreedomImageView, defStyleAttr, defStyleRes);

        this.coverMode = a.getBoolean(R.styleable.FreedomImageView_fiv_cover_mode, false);

        this.textPosition = a.getString(R.styleable.FreedomImageView_fiv_text_position);
        if (TextUtils.isEmpty(textPosition)
                || (!textPosition.equals(POSITION_TOP)
                && !textPosition.equals(POSITION_BOTTOM)
                && !textPosition.equals(POSITION_NONE))) {
            textPosition = POSITION_NONE;
        }

        a.recycle();

        this.paint = new Paint();
    }

    /** <br> UI. */

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int[] size = getMeasureSize(MeasureSpec.getSize(widthMeasureSpec));
        setMeasuredDimension(size[0], size[1]);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showShadow) {
            switch (textPosition) {
                case POSITION_NONE:
                    break;

                case POSITION_TOP:
                    int topTextHeight = (int) new DisplayUtils(getContext()).dpToPx(128);
                    paint.setShader(new LinearGradient(
                            0, 0,
                            0, topTextHeight,
                            new int[] {
                                    Color.argb((int) (255 * 0.25), 0, 0, 0),
                                    Color.argb((int) (255 * 0.09), 0, 0, 0),
                                    Color.argb((int) (255 * 0.03), 0, 0, 0),
                                    Color.argb(0, 0, 0, 0)},
                            null,
                            Shader.TileMode.CLAMP));
                    canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
                    break;

                case POSITION_BOTTOM:
                    int bottomTextHeight = (int) new DisplayUtils(getContext()).dpToPx(72);
                    paint.setShader(new LinearGradient(
                            0, getMeasuredHeight(),
                            0, getMeasuredHeight() - bottomTextHeight,
                            new int[] {
                                    Color.argb((int) (255 * 0.3), 0, 0, 0),
                                    Color.argb((int) (255 * 0.1), 0, 0, 0),
                                    Color.argb((int) (255 * 0.03), 0, 0, 0),
                                    Color.argb(0, 0, 0, 0)},
                            null,
                            Shader.TileMode.CLAMP));
                    canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
                    break;
            }
        }
    }

    /** <br> data. */

    public void setSize(int w, int h) {
        width = w;
        height = h;

        if (getMeasuredWidth() != 0) {
            int[] size = getMeasureSize(getMeasuredWidth());

            ViewGroup.LayoutParams params = getLayoutParams();
            params.width = size[0];
            params.height = size[1];
            setLayoutParams(params);
        }
    }

    public void setShowShadow(boolean show) {
        this.showShadow = show;
    }

    private int[] getMeasureSize(int measureWidth) {
        if (coverMode) {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            float limitHeight = screenHeight
                    - new DisplayUtils(getContext()).dpToPx(280);

            if (1.0 * height / width * screenWidth <= limitHeight) {
                return new int[] {
                        (int) (limitHeight * width / height),
                        (int) limitHeight};
            }
        }
        return new int[] {
                measureWidth,
                (int) (measureWidth * height / width)};
    }
}
