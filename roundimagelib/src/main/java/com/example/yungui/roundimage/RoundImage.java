package com.example.yungui.roundimage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;

import java.lang.ref.WeakReference;

/**
 * Created by yungui on 2017/10/3.
 */

public class RoundImage extends android.support.v7.widget.AppCompatImageView {
    private Paint mPaint;
    //取交集部分的
    private Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
    //用来掩饰的bitmap
    private Bitmap maskBitmap;

    private WeakReference<Bitmap> weakBitmap;

    /**
     * 图片的类型 圆形或者圆角
     */
    private int type;
    public static final int TYPE_ROUND = 0;
    public static final int TYPE_CIRCLE = 1;

    //y圆角大小默认值
    public static final int ROUND_ANGLE_DEFAULT = 10;
    //圆角的大小
    private int roundAngle;

    public RoundImage(Context context) {

        this(context, null);
        intiPaint();
    }

    public RoundImage(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        intiPaint();
        getAttrs(context, attrs);
    }

    public RoundImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        intiPaint();
        getAttrs(context, attrs);
    }

    private void intiPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

    }

    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImage);
        //获取角度
        roundAngle = typedArray.getDimensionPixelOffset(R.styleable.RoundImage_roundAngle,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ROUND_ANGLE_DEFAULT,
                        getResources().getDisplayMetrics()));
        //获取图片类型 ,默认圆形
        type = typedArray.getInt(R.styleable.RoundImage_Type, TYPE_CIRCLE);
        //回收资源
        typedArray.recycle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /**
         * 如果类型是圆形，则强制改变view的宽高一致，以小值为准
         */
        if (type == TYPE_CIRCLE)
        {
            int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
            setMeasuredDimension(width, width);
        }

    }

    @Override
    public void invalidate()
    {
        weakBitmap = null;
        if (maskBitmap != null)
        {
            maskBitmap.recycle();
            maskBitmap = null;
        }
        super.invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas)
    {
        //在缓存中取出bitmap
        Bitmap bitmap = weakBitmap == null ? null : weakBitmap.get();

        if (null == bitmap || bitmap.isRecycled())
        {
            //拿到Drawable
            Drawable drawable = getDrawable();
            //获取drawable的宽和高
            int dWidth = drawable.getIntrinsicWidth();
            int dHeight = drawable.getIntrinsicHeight();

            if (drawable != null)
            {
                //创建bitmap
                bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                        Bitmap.Config.ARGB_8888);
                float scale = 1.0f;
                //将纸张贴在画布上
                Canvas drawCanvas = new Canvas(bitmap);
                //按照bitmap的宽高，以及view的宽高，计算缩放比例；因为设置的src宽高比例可能和imageview的宽高比例不同，这里我们不希望图片失真；
                if (type == TYPE_ROUND)
                {
                    // 如果图片的宽或者高与view的宽高不匹配，计算出需要缩放的比例；缩放后的图片的宽高，一定要大于我们view的宽高；所以我们这里取大值；
                    scale = Math.max(getWidth() * 1.0f / dWidth, getHeight()
                            * 1.0f / dHeight);
                } else
                {
                    scale = getWidth() * 1.0F / Math.min(dWidth, dHeight);
                }
                //根据缩放比例，设置bounds，相当于缩放图片了
                drawable.setBounds(0, 0, (int) (scale * dWidth),
                        (int) (scale * dHeight));
                drawable.draw(drawCanvas);
                if (maskBitmap == null || maskBitmap.isRecycled())
                {
                    maskBitmap = getBitmap();
                }
                // Draw Bitmap.
                mPaint.reset();
                mPaint.setFilterBitmap(false);
                mPaint.setXfermode(xfermode);
                //绘制形状
                drawCanvas.drawBitmap(maskBitmap, 0, 0, mPaint);
                mPaint.setXfermode(null);
                //将准备好的bitmap绘制出来
                canvas.drawBitmap(bitmap, 0, 0, null);
                //bitmap缓存起来，避免每次调用onDraw，分配内存
                weakBitmap = new WeakReference<Bitmap>(bitmap);
            }
        }
        //如果bitmap还存在，则直接绘制即可
        if (bitmap != null)
        {
            mPaint.setXfermode(null);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, mPaint);
            return;
        }

    }
    /**
     * 绘制形状
     * @return
     */
    public Bitmap getBitmap()
    {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);

        if (type == TYPE_ROUND)
        {
            canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()),
                    roundAngle, roundAngle, paint);
        } else
        {
            canvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2,
                    paint);
        }

        return bitmap;
    }
}
