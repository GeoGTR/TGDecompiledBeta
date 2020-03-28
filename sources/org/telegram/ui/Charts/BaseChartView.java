package org.telegram.ui.Charts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Charts.ChartPickerDelegate;
import org.telegram.ui.Charts.data.ChartData;
import org.telegram.ui.Charts.view_data.ChartBottomSignatureData;
import org.telegram.ui.Charts.view_data.ChartHeaderView;
import org.telegram.ui.Charts.view_data.ChartHorizontalLinesData;
import org.telegram.ui.Charts.view_data.LegendSignatureView;
import org.telegram.ui.Charts.view_data.LineViewData;
import org.telegram.ui.Charts.view_data.TransitionParams;
import org.telegram.ui.Components.CubicBezierInterpolator;

public abstract class BaseChartView<T extends ChartData, L extends LineViewData> extends View implements ChartPickerDelegate.Listener {
    protected static final boolean ANIMATE_PICKER_SIZES;
    private static final int BOTTOM_SIGNATURE_OFFSET = AndroidUtilities.dp(10.0f);
    public static final int BOTTOM_SIGNATURE_START_ALPHA = AndroidUtilities.dp(10.0f);
    private static final int BOTTOM_SIGNATURE_TEXT_HEIGHT = AndroidUtilities.dp(14.0f);
    private static final int DP_1 = AndroidUtilities.dp(1.0f);
    private static final int DP_12 = AndroidUtilities.dp(12.0f);
    private static final int DP_2 = AndroidUtilities.dp(2.0f);
    private static final int DP_5 = AndroidUtilities.dp(5.0f);
    private static final int DP_6 = AndroidUtilities.dp(6.0f);
    public static final int HORIZONTAL_PADDING = AndroidUtilities.dp(16.0f);
    public static FastOutSlowInInterpolator INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final int LANDSCAPE_END_PADDING = AndroidUtilities.dp(16.0f);
    private static final int PICKER_CAPTURE_WIDTH = AndroidUtilities.dp(24.0f);
    protected static final int PICKER_PADDING = AndroidUtilities.dp(16.0f);
    private static final float SELECTED_LINE_WIDTH = AndroidUtilities.dpf2(1.5f);
    public static final int SIGNATURE_TEXT_HEIGHT = AndroidUtilities.dp(18.0f);
    private static final float SIGNATURE_TEXT_SIZE = AndroidUtilities.dpf2(12.0f);
    public static final boolean USE_LINES = (Build.VERSION.SDK_INT < 28);
    ValueAnimator alphaAnimator;
    ValueAnimator alphaBottomAnimator;
    public boolean animateLegentTo = false;
    float animateToMaxHeight = 0.0f;
    float animateToMinHeight = 0.0f;
    protected float animatedToPickerMaxHeight;
    protected float animatedToPickerMinHeight;
    private Bitmap bottomChartBitmap;
    private Canvas bottomChartCanvas;
    ArrayList<ChartBottomSignatureData> bottomSignatureDate = new ArrayList<>(25);
    protected int bottomSignatureOffset;
    Paint bottomSignaturePaint = new TextPaint(1);
    float bottomSignaturePaintAlpha;
    protected boolean canCaptureChartSelection;
    long capturedTime;
    int capturedX;
    int capturedY;
    int chartActiveLineAlpha;
    public Rect chartArea = new Rect();
    int chartBottom;
    protected boolean chartCaptured = false;
    T chartData;
    public int chartEnd;
    public float chartFullWidth;
    ChartHeaderView chartHeaderView;
    public int chartStart;
    public int chartWidth;
    ChartBottomSignatureData currentBottomSignatures;
    public float currentMaxHeight = 250.0f;
    public float currentMinHeight = 0.0f;
    protected DateSelectionListener dateSelectionListener;
    protected boolean drawPointOnSelection = true;
    Paint emptyPaint = new Paint();
    public boolean enabled = true;
    int endXIndex;
    private ValueAnimator.AnimatorUpdateListener heightUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        public final void onAnimationUpdate(ValueAnimator valueAnimator) {
            BaseChartView.this.lambda$new$0$BaseChartView(valueAnimator);
        }
    };
    int hintLinePaintAlpha;
    ArrayList<ChartHorizontalLinesData> horizontalLines = new ArrayList<>(10);
    boolean invalidatePickerChart = true;
    boolean landscape = false;
    int lastH = 0;
    long lastTime = 0;
    int lastW = 0;
    int lastX;
    int lastY;
    public boolean legendShowing = false;
    public LegendSignatureView legendSignatureView;
    Paint linePaint = new Paint();
    public ArrayList<L> lines = new ArrayList<>();
    Animator maxValueAnimator;
    private ValueAnimator.AnimatorUpdateListener minHeightUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        public final void onAnimationUpdate(ValueAnimator valueAnimator) {
            BaseChartView.this.lambda$new$1$BaseChartView(valueAnimator);
        }
    };
    private float minMaxUpdateStep;
    Path pathTmp = new Path();
    Animator pickerAnimator;
    public ChartPickerDelegate pickerDelegate = new ChartPickerDelegate(this);
    private ValueAnimator.AnimatorUpdateListener pickerHeightUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            BaseChartView.this.pickerMaxHeight = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            BaseChartView baseChartView = BaseChartView.this;
            baseChartView.invalidatePickerChart = true;
            baseChartView.invalidate();
        }
    };
    protected float pickerMaxHeight;
    protected float pickerMinHeight;
    private ValueAnimator.AnimatorUpdateListener pickerMinHeightUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            BaseChartView.this.pickerMinHeight = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            BaseChartView baseChartView = BaseChartView.this;
            baseChartView.invalidatePickerChart = true;
            baseChartView.invalidate();
        }
    };
    Rect pickerRect = new Rect();
    Paint pickerSelectorPaint = new Paint(1);
    public int pickerWidth;
    public int pikerHeight = AndroidUtilities.dp(46.0f);
    boolean postTransition = false;
    Paint ripplePaint = new Paint(1);
    protected int selectedIndex = -1;
    Paint selectedLinePaint = new Paint();
    public float selectionA = 0.0f;
    ValueAnimator selectionAnimator;
    private ValueAnimator.AnimatorUpdateListener selectionAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            BaseChartView.this.selectionA = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            BaseChartView baseChartView = BaseChartView.this;
            baseChartView.legendSignatureView.setAlpha(baseChartView.selectionA);
            BaseChartView.this.invalidate();
        }
    };
    Paint selectionBackgroundPaint = new Paint(1);
    private Animator.AnimatorListener selectorAnimatorEndListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            super.onAnimationEnd(animator);
            BaseChartView baseChartView = BaseChartView.this;
            if (!baseChartView.animateLegentTo) {
                baseChartView.legendShowing = false;
                baseChartView.legendSignatureView.setVisibility(8);
                BaseChartView.this.invalidate();
            }
            BaseChartView.this.postTransition = false;
        }
    };
    public SharedUiComponents sharedUiComponents;
    Paint signaturePaint = new TextPaint(1);
    Paint signaturePaint2 = new TextPaint(1);
    float signaturePaintAlpha;
    private float startFromMax;
    private float startFromMaxH;
    private float startFromMin;
    private float startFromMinH;
    int startXIndex;
    boolean superDraw = false;
    float thresholdMaxHeight = 0.0f;
    protected int tmpI;
    protected int tmpN;
    private final int touchSlop;
    public int transitionMode = 0;
    public TransitionParams transitionParams;
    Paint unactiveBottomChartPaint = new Paint();
    boolean useAlphaSignature = false;
    protected boolean useMinHeight = false;
    Paint whiteLinePaint = new Paint(1);

    public interface DateSelectionListener {
        void onDateSelected(long j);
    }

    public abstract L createLineViewData(ChartData.Line line);

    /* access modifiers changed from: protected */
    public void drawChart(Canvas canvas) {
    }

    /* access modifiers changed from: protected */
    public void drawPickerChart(Canvas canvas) {
    }

    /* access modifiers changed from: protected */
    public void onActionUp() {
    }

    static {
        boolean z = true;
        if (Build.VERSION.SDK_INT <= 21) {
            z = false;
        }
        ANIMATE_PICKER_SIZES = z;
    }

    public /* synthetic */ void lambda$new$0$BaseChartView(ValueAnimator valueAnimator) {
        this.currentMaxHeight = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        invalidate();
    }

    public /* synthetic */ void lambda$new$1$BaseChartView(ValueAnimator valueAnimator) {
        this.currentMinHeight = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        invalidate();
    }

    public BaseChartView(Context context) {
        super(context);
        init();
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.linePaint.setStrokeWidth(1.0f);
        this.selectedLinePaint.setStrokeWidth(SELECTED_LINE_WIDTH);
        this.signaturePaint.setTextSize(SIGNATURE_TEXT_SIZE);
        this.signaturePaint2.setTextSize(SIGNATURE_TEXT_SIZE);
        this.signaturePaint2.setTextAlign(Paint.Align.RIGHT);
        this.bottomSignaturePaint.setTextSize(SIGNATURE_TEXT_SIZE);
        this.bottomSignaturePaint.setTextAlign(Paint.Align.CENTER);
        this.selectionBackgroundPaint.setStrokeWidth(AndroidUtilities.dpf2(6.0f));
        this.selectionBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        setLayerType(2, (Paint) null);
        setWillNotDraw(false);
        LegendSignatureView createLegendView = createLegendView();
        this.legendSignatureView = createLegendView;
        createLegendView.setVisibility(8);
        this.whiteLinePaint.setColor(-1);
        this.whiteLinePaint.setStrokeWidth(AndroidUtilities.dpf2(3.0f));
        this.whiteLinePaint.setStrokeCap(Paint.Cap.ROUND);
        updateColors();
    }

    /* access modifiers changed from: protected */
    public LegendSignatureView createLegendView() {
        return new LegendSignatureView(getContext());
    }

    public void updateColors() {
        if (this.useAlphaSignature) {
            this.signaturePaint.setColor(Theme.getColor("statisticChartSignatureAlpha"));
        } else {
            this.signaturePaint.setColor(Theme.getColor("statisticChartSignature"));
        }
        this.bottomSignaturePaint.setColor(Theme.getColor("statisticChartSignature"));
        this.linePaint.setColor(Theme.getColor("statisticChartHintLine"));
        this.selectedLinePaint.setColor(Theme.getColor("statisticChartActiveLine"));
        this.pickerSelectorPaint.setColor(Theme.getColor("statisticChartActivePickerChart"));
        this.unactiveBottomChartPaint.setColor(Theme.getColor("statisticChartInactivePickerChart"));
        this.selectionBackgroundPaint.setColor(Theme.getColor("windowBackgroundWhite"));
        this.ripplePaint.setColor(Theme.getColor("statisticChartRipple"));
        this.legendSignatureView.recolor();
        this.hintLinePaintAlpha = this.linePaint.getAlpha();
        this.chartActiveLineAlpha = this.selectedLinePaint.getAlpha();
        this.signaturePaintAlpha = ((float) this.signaturePaint.getAlpha()) / 255.0f;
        this.bottomSignaturePaintAlpha = ((float) this.bottomSignaturePaint.getAlpha()) / 255.0f;
        Iterator<L> it = this.lines.iterator();
        while (it.hasNext()) {
            ((LineViewData) it.next()).updateColors();
        }
        if (this.legendShowing) {
            int i = this.selectedIndex;
            long[] jArr = this.chartData.x;
            if (i < jArr.length) {
                this.legendSignatureView.setData(i, jArr[i], this.lines, false);
            }
        }
        this.invalidatePickerChart = true;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (!this.landscape) {
            setMeasuredDimension(View.MeasureSpec.getSize(i), View.MeasureSpec.getSize(i));
        } else {
            setMeasuredDimension(View.MeasureSpec.getSize(i), AndroidUtilities.displaySize.y - AndroidUtilities.dp(56.0f));
        }
        if (getMeasuredWidth() != this.lastW || getMeasuredHeight() != this.lastH) {
            this.lastW = getMeasuredWidth();
            this.lastH = getMeasuredHeight();
            this.bottomChartBitmap = Bitmap.createBitmap(getMeasuredWidth() - (HORIZONTAL_PADDING << 1), this.pikerHeight, Bitmap.Config.ARGB_4444);
            this.bottomChartCanvas = new Canvas(this.bottomChartBitmap);
            this.sharedUiComponents.getPickerMaskBitmap(this.pikerHeight, getMeasuredWidth() - (HORIZONTAL_PADDING * 2));
            measureSizes();
            if (this.legendShowing) {
                moveLegend((this.chartFullWidth * this.pickerDelegate.pickerStart) - ((float) HORIZONTAL_PADDING));
            }
            onPickerDataChanged(false, true, false);
        }
    }

    private void measureSizes() {
        if (getMeasuredHeight() > 0 && getMeasuredWidth() > 0) {
            int measuredWidth = getMeasuredWidth();
            int i = HORIZONTAL_PADDING;
            this.pickerWidth = measuredWidth - (i * 2);
            this.chartStart = i;
            int measuredWidth2 = getMeasuredWidth() - (this.landscape ? LANDSCAPE_END_PADDING : HORIZONTAL_PADDING);
            this.chartEnd = measuredWidth2;
            int i2 = measuredWidth2 - this.chartStart;
            this.chartWidth = i2;
            ChartPickerDelegate chartPickerDelegate = this.pickerDelegate;
            this.chartFullWidth = ((float) i2) / (chartPickerDelegate.pickerEnd - chartPickerDelegate.pickerStart);
            updateLineSignature();
            this.chartBottom = AndroidUtilities.dp(100.0f);
            Rect rect = this.chartArea;
            int i3 = this.chartStart;
            int i4 = HORIZONTAL_PADDING;
            rect.set(i3 - i4, 0, this.chartEnd + i4, getMeasuredHeight() - this.chartBottom);
            if (this.chartData != null) {
                this.bottomSignatureOffset = (int) (((float) AndroidUtilities.dp(20.0f)) / (((float) this.pickerWidth) / ((float) this.chartData.x.length)));
            }
            measureHeightThreshold();
        }
    }

    private void measureHeightThreshold() {
        int measuredHeight = getMeasuredHeight() - this.chartBottom;
        float f = this.animateToMaxHeight;
        if (f != 0.0f && measuredHeight != 0) {
            this.thresholdMaxHeight = (f / ((float) measuredHeight)) * SIGNATURE_TEXT_SIZE;
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (this.superDraw) {
            super.onDraw(canvas);
            return;
        }
        tick();
        int save = canvas.save();
        canvas.clipRect(0, this.chartArea.top, getMeasuredWidth(), this.chartArea.bottom);
        drawBottomLine(canvas);
        this.tmpN = this.horizontalLines.size();
        this.tmpI = 0;
        while (true) {
            int i = this.tmpI;
            if (i >= this.tmpN) {
                break;
            }
            drawHorizontalLines(canvas, this.horizontalLines.get(i));
            this.tmpI++;
        }
        drawChart(canvas);
        this.tmpI = 0;
        while (true) {
            int i2 = this.tmpI;
            if (i2 < this.tmpN) {
                drawSignaturesToHorizontalLines(canvas, this.horizontalLines.get(i2));
                this.tmpI++;
            } else {
                canvas.restoreToCount(save);
                drawBottomSignature(canvas);
                drawPicker(canvas);
                drawSelection(canvas);
                super.onDraw(canvas);
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void tick() {
        float f = this.minMaxUpdateStep;
        if (f != 0.0f) {
            float f2 = this.currentMaxHeight;
            float f3 = this.animateToMaxHeight;
            if (f2 != f3) {
                float f4 = this.startFromMax + f;
                this.startFromMax = f4;
                if (f4 > 1.0f) {
                    this.startFromMax = 1.0f;
                    this.currentMaxHeight = f3;
                } else {
                    float f5 = this.startFromMaxH;
                    this.currentMaxHeight = f5 + ((f3 - f5) * CubicBezierInterpolator.EASE_OUT.getInterpolation(f4));
                }
                invalidate();
            }
            if (this.useMinHeight) {
                float f6 = this.currentMinHeight;
                float f7 = this.animateToMinHeight;
                if (f6 != f7) {
                    float f8 = this.startFromMin + this.minMaxUpdateStep;
                    this.startFromMin = f8;
                    if (f8 > 1.0f) {
                        this.startFromMin = 1.0f;
                        this.currentMinHeight = f7;
                    } else {
                        float f9 = this.startFromMinH;
                        this.currentMinHeight = f9 + ((f7 - f9) * CubicBezierInterpolator.EASE_OUT.getInterpolation(f8));
                    }
                    invalidate();
                }
                invalidate();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void drawBottomSignature(Canvas canvas) {
        float f;
        if (this.chartData != null) {
            this.tmpN = this.bottomSignatureDate.size();
            int i = this.transitionMode;
            if (i == 2) {
                f = 1.0f - this.transitionParams.progress;
            } else if (i == 1) {
                f = this.transitionParams.progress;
            } else {
                f = i == 3 ? this.transitionParams.progress : 1.0f;
            }
            this.tmpI = 0;
            while (true) {
                int i2 = this.tmpI;
                if (i2 < this.tmpN) {
                    int i3 = this.bottomSignatureDate.get(i2).alpha;
                    int i4 = this.bottomSignatureDate.get(this.tmpI).step;
                    if (i4 == 0) {
                        i4 = 1;
                    }
                    int i5 = this.startXIndex - this.bottomSignatureOffset;
                    while (i5 % i4 != 0) {
                        i5--;
                    }
                    int i6 = this.endXIndex - this.bottomSignatureOffset;
                    while (true) {
                        if (i6 % i4 == 0 && i6 >= this.chartData.x.length - 1) {
                            break;
                        }
                        Canvas canvas2 = canvas;
                        i6++;
                    }
                    int i7 = this.bottomSignatureOffset;
                    int i8 = i6 + i7;
                    float f2 = (this.chartFullWidth * this.pickerDelegate.pickerStart) - ((float) HORIZONTAL_PADDING);
                    for (int i9 = i5 + i7; i9 < i8; i9 += i4) {
                        if (i9 >= 0) {
                            long[] jArr = this.chartData.x;
                            if (i9 < jArr.length - 1) {
                                float f3 = ((((float) (jArr[i9] - jArr[0])) / ((float) (jArr[jArr.length - 1] - jArr[0]))) * this.chartFullWidth) - f2;
                                float f4 = f3 - ((float) BOTTOM_SIGNATURE_OFFSET);
                                if (f4 > 0.0f) {
                                    int i10 = this.chartWidth;
                                    int i11 = HORIZONTAL_PADDING;
                                    if (f4 <= ((float) (i10 + i11))) {
                                        int i12 = BOTTOM_SIGNATURE_START_ALPHA;
                                        if (f4 < ((float) i12)) {
                                            this.bottomSignaturePaint.setAlpha((int) (((float) i3) * (1.0f - ((((float) i12) - f4) / ((float) i12))) * this.bottomSignaturePaintAlpha * f));
                                        } else if (f4 > ((float) i10)) {
                                            this.bottomSignaturePaint.setAlpha((int) (((float) i3) * (1.0f - ((f4 - ((float) i10)) / ((float) i11))) * this.bottomSignaturePaintAlpha * f));
                                        } else {
                                            this.bottomSignaturePaint.setAlpha((int) (((float) i3) * this.bottomSignaturePaintAlpha * f));
                                        }
                                        canvas.drawText(this.chartData.getDayString(i9), f3, (float) ((getMeasuredHeight() - this.chartBottom) + BOTTOM_SIGNATURE_TEXT_HEIGHT + AndroidUtilities.dp(3.0f)), this.bottomSignaturePaint);
                                    }
                                }
                            }
                        }
                        Canvas canvas3 = canvas;
                    }
                    Canvas canvas4 = canvas;
                    this.tmpI++;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void drawBottomLine(Canvas canvas) {
        if (this.chartData != null) {
            int i = this.transitionMode;
            float f = 1.0f;
            if (i == 2) {
                f = 1.0f - this.transitionParams.progress;
            } else if (i == 1) {
                f = this.transitionParams.progress;
            } else if (i == 3) {
                f = this.transitionParams.progress;
            }
            this.linePaint.setAlpha((int) (((float) this.hintLinePaintAlpha) * f));
            this.signaturePaint.setAlpha((int) (this.signaturePaintAlpha * 255.0f * f));
            int textSize = (int) (((float) SIGNATURE_TEXT_HEIGHT) - this.signaturePaint.getTextSize());
            int measuredHeight = (getMeasuredHeight() - this.chartBottom) - 1;
            float f2 = (float) measuredHeight;
            canvas.drawLine((float) this.chartStart, f2, (float) this.chartEnd, f2, this.linePaint);
            if (!this.useMinHeight) {
                canvas.drawText("0", (float) HORIZONTAL_PADDING, (float) (measuredHeight - textSize), this.signaturePaint);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void drawSelection(Canvas canvas) {
        T t;
        int i = this.selectedIndex;
        if (i >= 0 && this.legendShowing && (t = this.chartData) != null) {
            int i2 = (int) (((float) this.chartActiveLineAlpha) * this.selectionA);
            ChartPickerDelegate chartPickerDelegate = this.pickerDelegate;
            float f = chartPickerDelegate.pickerEnd;
            float f2 = chartPickerDelegate.pickerStart;
            float f3 = ((float) this.chartWidth) / (f - f2);
            float f4 = (f2 * f3) - ((float) HORIZONTAL_PADDING);
            float[] fArr = t.xPercentage;
            if (i < fArr.length) {
                float f5 = (fArr[i] * f3) - f4;
                this.selectedLinePaint.setAlpha(i2);
                canvas.drawLine(f5, 0.0f, f5, (float) this.chartArea.bottom, this.selectedLinePaint);
                if (this.drawPointOnSelection) {
                    this.tmpN = this.lines.size();
                    int i3 = 0;
                    while (true) {
                        this.tmpI = i3;
                        int i4 = this.tmpI;
                        if (i4 < this.tmpN) {
                            LineViewData lineViewData = (LineViewData) this.lines.get(i4);
                            if (lineViewData.enabled || lineViewData.alpha != 0.0f) {
                                float f6 = this.currentMinHeight;
                                float measuredHeight = ((float) (getMeasuredHeight() - this.chartBottom)) - (((((float) lineViewData.line.y[this.selectedIndex]) - f6) / (this.currentMaxHeight - f6)) * ((float) ((getMeasuredHeight() - this.chartBottom) - SIGNATURE_TEXT_HEIGHT)));
                                lineViewData.selectionPaint.setAlpha((int) (lineViewData.alpha * 255.0f * this.selectionA));
                                this.selectionBackgroundPaint.setAlpha((int) (lineViewData.alpha * 255.0f * this.selectionA));
                                canvas.drawPoint(f5, measuredHeight, lineViewData.selectionPaint);
                                canvas.drawPoint(f5, measuredHeight, this.selectionBackgroundPaint);
                            }
                            i3 = this.tmpI + 1;
                        } else {
                            return;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0074 A[LOOP:0: B:15:0x0072->B:16:0x0074, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x002b  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0031  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void drawHorizontalLines(android.graphics.Canvas r13, org.telegram.ui.Charts.view_data.ChartHorizontalLinesData r14) {
        /*
            r12 = this;
            int[] r0 = r14.values
            int r1 = r0.length
            r2 = 2
            r3 = 1065353216(0x3var_, float:1.0)
            r4 = 1
            if (r1 <= r2) goto L_0x0025
            r5 = r0[r4]
            r6 = 0
            r0 = r0[r6]
            int r5 = r5 - r0
            float r0 = (float) r5
            float r5 = r12.currentMaxHeight
            float r6 = r12.currentMinHeight
            float r5 = r5 - r6
            float r0 = r0 / r5
            double r5 = (double) r0
            r7 = 4591870180066957722(0x3fb999999999999a, double:0.1)
            int r9 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r9 >= 0) goto L_0x0025
            r5 = 1036831949(0x3dcccccd, float:0.1)
            float r0 = r0 / r5
            goto L_0x0027
        L_0x0025:
            r0 = 1065353216(0x3var_, float:1.0)
        L_0x0027:
            int r5 = r12.transitionMode
            if (r5 != r2) goto L_0x0031
            org.telegram.ui.Charts.view_data.TransitionParams r2 = r12.transitionParams
            float r2 = r2.progress
            float r3 = r3 - r2
            goto L_0x003f
        L_0x0031:
            if (r5 != r4) goto L_0x0038
            org.telegram.ui.Charts.view_data.TransitionParams r2 = r12.transitionParams
            float r3 = r2.progress
            goto L_0x003f
        L_0x0038:
            r2 = 3
            if (r5 != r2) goto L_0x003f
            org.telegram.ui.Charts.view_data.TransitionParams r2 = r12.transitionParams
            float r3 = r2.progress
        L_0x003f:
            android.graphics.Paint r2 = r12.linePaint
            int r5 = r14.alpha
            float r5 = (float) r5
            int r6 = r12.hintLinePaintAlpha
            float r6 = (float) r6
            r7 = 1132396544(0x437var_, float:255.0)
            float r6 = r6 / r7
            float r5 = r5 * r6
            float r5 = r5 * r3
            float r5 = r5 * r0
            int r5 = (int) r5
            r2.setAlpha(r5)
            android.graphics.Paint r2 = r12.signaturePaint
            int r5 = r14.alpha
            float r5 = (float) r5
            float r6 = r12.signaturePaintAlpha
            float r5 = r5 * r6
            float r5 = r5 * r3
            float r5 = r5 * r0
            int r0 = (int) r5
            r2.setAlpha(r0)
            int r0 = r12.getMeasuredHeight()
            int r2 = r12.chartBottom
            int r0 = r0 - r2
            int r2 = SIGNATURE_TEXT_HEIGHT
            int r0 = r0 - r2
            boolean r2 = r12.useMinHeight
            r2 = r2 ^ r4
        L_0x0072:
            if (r2 >= r1) goto L_0x009f
            int r3 = r12.getMeasuredHeight()
            int r5 = r12.chartBottom
            int r3 = r3 - r5
            float r3 = (float) r3
            float r5 = (float) r0
            int[] r6 = r14.values
            r6 = r6[r2]
            float r6 = (float) r6
            float r7 = r12.currentMinHeight
            float r6 = r6 - r7
            float r8 = r12.currentMaxHeight
            float r8 = r8 - r7
            float r6 = r6 / r8
            float r5 = r5 * r6
            float r3 = r3 - r5
            int r3 = (int) r3
            int r5 = r12.chartStart
            float r7 = (float) r5
            float r8 = (float) r3
            int r5 = r12.chartEnd
            float r9 = (float) r5
            int r3 = r3 + r4
            float r10 = (float) r3
            android.graphics.Paint r11 = r12.linePaint
            r6 = r13
            r6.drawRect(r7, r8, r9, r10, r11)
            int r2 = r2 + 1
            goto L_0x0072
        L_0x009f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Charts.BaseChartView.drawHorizontalLines(android.graphics.Canvas, org.telegram.ui.Charts.view_data.ChartHorizontalLinesData):void");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x007d A[LOOP:0: B:15:0x007b->B:16:0x007d, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x002b  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0031  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void drawSignaturesToHorizontalLines(android.graphics.Canvas r11, org.telegram.ui.Charts.view_data.ChartHorizontalLinesData r12) {
        /*
            r10 = this;
            int[] r0 = r12.values
            int r1 = r0.length
            r2 = 2
            r3 = 1065353216(0x3var_, float:1.0)
            r4 = 1
            if (r1 <= r2) goto L_0x0025
            r5 = r0[r4]
            r6 = 0
            r0 = r0[r6]
            int r5 = r5 - r0
            float r0 = (float) r5
            float r5 = r10.currentMaxHeight
            float r6 = r10.currentMinHeight
            float r5 = r5 - r6
            float r0 = r0 / r5
            double r5 = (double) r0
            r7 = 4591870180066957722(0x3fb999999999999a, double:0.1)
            int r9 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r9 >= 0) goto L_0x0025
            r5 = 1036831949(0x3dcccccd, float:0.1)
            float r0 = r0 / r5
            goto L_0x0027
        L_0x0025:
            r0 = 1065353216(0x3var_, float:1.0)
        L_0x0027:
            int r5 = r10.transitionMode
            if (r5 != r2) goto L_0x0031
            org.telegram.ui.Charts.view_data.TransitionParams r2 = r10.transitionParams
            float r2 = r2.progress
            float r3 = r3 - r2
            goto L_0x003f
        L_0x0031:
            if (r5 != r4) goto L_0x0038
            org.telegram.ui.Charts.view_data.TransitionParams r2 = r10.transitionParams
            float r3 = r2.progress
            goto L_0x003f
        L_0x0038:
            r2 = 3
            if (r5 != r2) goto L_0x003f
            org.telegram.ui.Charts.view_data.TransitionParams r2 = r10.transitionParams
            float r3 = r2.progress
        L_0x003f:
            android.graphics.Paint r2 = r10.linePaint
            int r5 = r12.alpha
            float r5 = (float) r5
            int r6 = r10.hintLinePaintAlpha
            float r6 = (float) r6
            r7 = 1132396544(0x437var_, float:255.0)
            float r6 = r6 / r7
            float r5 = r5 * r6
            float r5 = r5 * r3
            float r5 = r5 * r0
            int r5 = (int) r5
            r2.setAlpha(r5)
            android.graphics.Paint r2 = r10.signaturePaint
            int r5 = r12.alpha
            float r5 = (float) r5
            float r6 = r10.signaturePaintAlpha
            float r5 = r5 * r6
            float r5 = r5 * r3
            float r5 = r5 * r0
            int r0 = (int) r5
            r2.setAlpha(r0)
            int r0 = r10.getMeasuredHeight()
            int r2 = r10.chartBottom
            int r0 = r0 - r2
            int r2 = SIGNATURE_TEXT_HEIGHT
            int r0 = r0 - r2
            float r2 = (float) r2
            android.graphics.Paint r3 = r10.signaturePaint
            float r3 = r3.getTextSize()
            float r2 = r2 - r3
            int r2 = (int) r2
            boolean r3 = r10.useMinHeight
            r3 = r3 ^ r4
        L_0x007b:
            if (r3 >= r1) goto L_0x00a7
            int r4 = r10.getMeasuredHeight()
            int r5 = r10.chartBottom
            int r4 = r4 - r5
            float r4 = (float) r4
            float r5 = (float) r0
            int[] r6 = r12.values
            r6 = r6[r3]
            float r6 = (float) r6
            float r7 = r10.currentMinHeight
            float r6 = r6 - r7
            float r8 = r10.currentMaxHeight
            float r8 = r8 - r7
            float r6 = r6 / r8
            float r5 = r5 * r6
            float r4 = r4 - r5
            int r4 = (int) r4
            java.lang.String[] r5 = r12.valuesStr
            r5 = r5[r3]
            int r6 = HORIZONTAL_PADDING
            float r6 = (float) r6
            int r4 = r4 - r2
            float r4 = (float) r4
            android.graphics.Paint r7 = r10.signaturePaint
            r11.drawText(r5, r6, r4, r7)
            int r3 = r3 + 1
            goto L_0x007b
        L_0x00a7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Charts.BaseChartView.drawSignaturesToHorizontalLines(android.graphics.Canvas, org.telegram.ui.Charts.view_data.ChartHorizontalLinesData):void");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0079  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00ab  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00e3  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00f5  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x01ce A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x01cf  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x01f7  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0233  */
    /* JADX WARNING: Removed duplicated region for block: B:65:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void drawPicker(android.graphics.Canvas r25) {
        /*
            r24 = this;
            r0 = r24
            r7 = r25
            T r1 = r0.chartData
            if (r1 != 0) goto L_0x0009
            return
        L_0x0009:
            org.telegram.ui.Charts.ChartPickerDelegate r1 = r0.pickerDelegate
            int r2 = r0.pickerWidth
            r1.pickerWidth = r2
            int r1 = r24.getMeasuredHeight()
            int r2 = PICKER_PADDING
            int r8 = r1 - r2
            int r1 = r24.getMeasuredHeight()
            int r2 = r0.pikerHeight
            int r1 = r1 - r2
            int r2 = PICKER_PADDING
            int r9 = r1 - r2
            int r1 = HORIZONTAL_PADDING
            float r2 = (float) r1
            int r3 = r0.pickerWidth
            float r4 = (float) r3
            org.telegram.ui.Charts.ChartPickerDelegate r5 = r0.pickerDelegate
            float r6 = r5.pickerStart
            float r4 = r4 * r6
            float r2 = r2 + r4
            int r2 = (int) r2
            float r4 = (float) r1
            float r6 = (float) r3
            float r5 = r5.pickerEnd
            float r6 = r6 * r5
            float r4 = r4 + r6
            int r4 = (int) r4
            int r5 = r0.transitionMode
            r6 = 1065353216(0x3var_, float:1.0)
            r10 = 1
            if (r5 != r10) goto L_0x0066
            float r5 = (float) r1
            float r11 = (float) r3
            org.telegram.ui.Charts.view_data.TransitionParams r12 = r0.transitionParams
            float r13 = r12.pickerStartOut
            float r11 = r11 * r13
            float r5 = r5 + r11
            int r5 = (int) r5
            float r1 = (float) r1
            float r3 = (float) r3
            float r11 = r12.pickerEndOut
            float r3 = r3 * r11
            float r1 = r1 + r3
            int r1 = (int) r1
            float r3 = (float) r2
            int r5 = r5 - r2
            float r2 = (float) r5
            float r5 = r12.progress
            float r11 = r6 - r5
            float r2 = r2 * r11
            float r3 = r3 + r2
            int r2 = (int) r3
            float r3 = (float) r4
            int r1 = r1 - r4
            float r1 = (float) r1
            float r4 = r6 - r5
            float r1 = r1 * r4
            float r3 = r3 + r1
            int r4 = (int) r3
            goto L_0x0070
        L_0x0066:
            r1 = 3
            if (r5 != r1) goto L_0x0070
            org.telegram.ui.Charts.view_data.TransitionParams r1 = r0.transitionParams
            float r1 = r1.progress
            r11 = r2
            r12 = r4
            goto L_0x0074
        L_0x0070:
            r11 = r2
            r12 = r4
            r1 = 1065353216(0x3var_, float:1.0)
        L_0x0074:
            T r2 = r0.chartData
            r13 = 2
            if (r2 == 0) goto L_0x01f7
            int r2 = r0.transitionMode
            r3 = 0
            if (r2 != 0) goto L_0x00a8
            r2 = 0
        L_0x007f:
            java.util.ArrayList<L> r4 = r0.lines
            int r4 = r4.size()
            if (r2 >= r4) goto L_0x00a8
            java.util.ArrayList<L> r4 = r0.lines
            java.lang.Object r4 = r4.get(r2)
            org.telegram.ui.Charts.view_data.LineViewData r4 = (org.telegram.ui.Charts.view_data.LineViewData) r4
            android.animation.ValueAnimator r5 = r4.animatorIn
            if (r5 == 0) goto L_0x0099
            boolean r5 = r5.isRunning()
            if (r5 != 0) goto L_0x00a3
        L_0x0099:
            android.animation.ValueAnimator r4 = r4.animatorOut
            if (r4 == 0) goto L_0x00a5
            boolean r4 = r4.isRunning()
            if (r4 == 0) goto L_0x00a5
        L_0x00a3:
            r2 = 1
            goto L_0x00a9
        L_0x00a5:
            int r2 = r2 + 1
            goto L_0x007f
        L_0x00a8:
            r2 = 0
        L_0x00a9:
            if (r2 == 0) goto L_0x00e3
            r25.save()
            int r3 = HORIZONTAL_PADDING
            int r4 = r24.getMeasuredHeight()
            int r5 = PICKER_PADDING
            int r4 = r4 - r5
            int r5 = r0.pikerHeight
            int r4 = r4 - r5
            int r5 = r24.getMeasuredWidth()
            int r14 = HORIZONTAL_PADDING
            int r5 = r5 - r14
            int r14 = r24.getMeasuredHeight()
            int r15 = PICKER_PADDING
            int r14 = r14 - r15
            r7.clipRect(r3, r4, r5, r14)
            int r3 = HORIZONTAL_PADDING
            float r3 = (float) r3
            int r4 = r24.getMeasuredHeight()
            int r5 = PICKER_PADDING
            int r4 = r4 - r5
            int r5 = r0.pikerHeight
            int r4 = r4 - r5
            float r4 = (float) r4
            r7.translate(r3, r4)
            r24.drawPickerChart(r25)
            r25.restore()
            goto L_0x00f3
        L_0x00e3:
            boolean r4 = r0.invalidatePickerChart
            if (r4 == 0) goto L_0x00f3
            android.graphics.Bitmap r4 = r0.bottomChartBitmap
            r4.eraseColor(r3)
            android.graphics.Canvas r4 = r0.bottomChartCanvas
            r0.drawPickerChart(r4)
            r0.invalidatePickerChart = r3
        L_0x00f3:
            if (r2 != 0) goto L_0x01ca
            int r2 = r0.transitionMode
            r3 = 1132396544(0x437var_, float:255.0)
            if (r2 != r13) goto L_0x014e
            int r1 = r8 - r9
            int r1 = r1 + r9
            int r1 = r1 >> r10
            float r1 = (float) r1
            int r2 = HORIZONTAL_PADDING
            float r2 = (float) r2
            int r4 = r0.pickerWidth
            float r4 = (float) r4
            org.telegram.ui.Charts.view_data.TransitionParams r5 = r0.transitionParams
            float r14 = r5.xPercentage
            float r4 = r4 * r14
            float r2 = r2 + r4
            android.graphics.Paint r4 = r0.emptyPaint
            float r5 = r5.progress
            float r5 = r6 - r5
            float r5 = r5 * r3
            int r3 = (int) r5
            r4.setAlpha(r3)
            r25.save()
            int r3 = HORIZONTAL_PADDING
            int r4 = r24.getMeasuredWidth()
            int r5 = HORIZONTAL_PADDING
            int r4 = r4 - r5
            r7.clipRect(r3, r9, r4, r8)
            r3 = 1073741824(0x40000000, float:2.0)
            org.telegram.ui.Charts.view_data.TransitionParams r4 = r0.transitionParams
            float r4 = r4.progress
            float r4 = r4 * r3
            float r4 = r4 + r6
            r7.scale(r4, r6, r2, r1)
            android.graphics.Bitmap r1 = r0.bottomChartBitmap
            int r2 = HORIZONTAL_PADDING
            float r2 = (float) r2
            int r3 = r24.getMeasuredHeight()
            int r4 = PICKER_PADDING
            int r3 = r3 - r4
            int r4 = r0.pikerHeight
            int r3 = r3 - r4
            float r3 = (float) r3
            android.graphics.Paint r4 = r0.emptyPaint
            r7.drawBitmap(r1, r2, r3, r4)
            r25.restore()
            goto L_0x01ca
        L_0x014e:
            if (r2 != r10) goto L_0x01ad
            int r1 = r8 - r9
            int r1 = r1 + r9
            int r1 = r1 >> r10
            float r1 = (float) r1
            int r2 = HORIZONTAL_PADDING
            float r2 = (float) r2
            int r4 = r0.pickerWidth
            float r5 = (float) r4
            org.telegram.ui.Charts.view_data.TransitionParams r14 = r0.transitionParams
            float r14 = r14.xPercentage
            float r5 = r5 * r14
            float r2 = r2 + r5
            r5 = 1056964608(0x3var_, float:0.5)
            int r5 = (r14 > r5 ? 1 : (r14 == r5 ? 0 : -1))
            float r4 = (float) r4
            if (r5 <= 0) goto L_0x016c
            float r4 = r4 * r14
            goto L_0x0170
        L_0x016c:
            float r5 = r6 - r14
            float r4 = r4 * r5
        L_0x0170:
            org.telegram.ui.Charts.view_data.TransitionParams r5 = r0.transitionParams
            float r5 = r5.progress
            float r4 = r4 * r5
            r25.save()
            float r5 = r2 - r4
            float r14 = (float) r9
            float r4 = r4 + r2
            float r15 = (float) r8
            r7.clipRect(r5, r14, r4, r15)
            android.graphics.Paint r4 = r0.emptyPaint
            org.telegram.ui.Charts.view_data.TransitionParams r5 = r0.transitionParams
            float r5 = r5.progress
            float r5 = r5 * r3
            int r3 = (int) r5
            r4.setAlpha(r3)
            org.telegram.ui.Charts.view_data.TransitionParams r3 = r0.transitionParams
            float r3 = r3.progress
            r7.scale(r3, r6, r2, r1)
            android.graphics.Bitmap r1 = r0.bottomChartBitmap
            int r2 = HORIZONTAL_PADDING
            float r2 = (float) r2
            int r3 = r24.getMeasuredHeight()
            int r4 = PICKER_PADDING
            int r3 = r3 - r4
            int r4 = r0.pikerHeight
            int r3 = r3 - r4
            float r3 = (float) r3
            android.graphics.Paint r4 = r0.emptyPaint
            r7.drawBitmap(r1, r2, r3, r4)
            r25.restore()
            goto L_0x01ca
        L_0x01ad:
            android.graphics.Paint r2 = r0.emptyPaint
            float r1 = r1 * r3
            int r1 = (int) r1
            r2.setAlpha(r1)
            android.graphics.Bitmap r1 = r0.bottomChartBitmap
            int r2 = HORIZONTAL_PADDING
            float r2 = (float) r2
            int r3 = r24.getMeasuredHeight()
            int r4 = PICKER_PADDING
            int r3 = r3 - r4
            int r4 = r0.pikerHeight
            int r3 = r3 - r4
            float r3 = (float) r3
            android.graphics.Paint r4 = r0.emptyPaint
            r7.drawBitmap(r1, r2, r3, r4)
        L_0x01ca:
            int r1 = r0.transitionMode
            if (r1 != r13) goto L_0x01cf
            return
        L_0x01cf:
            int r1 = HORIZONTAL_PADDING
            float r2 = (float) r1
            float r14 = (float) r9
            int r1 = DP_12
            int r1 = r1 + r11
            float r4 = (float) r1
            float r15 = (float) r8
            android.graphics.Paint r6 = r0.unactiveBottomChartPaint
            r1 = r25
            r3 = r14
            r5 = r15
            r1.drawRect(r2, r3, r4, r5, r6)
            int r1 = DP_12
            int r1 = r12 - r1
            float r2 = (float) r1
            int r1 = r24.getMeasuredWidth()
            int r3 = HORIZONTAL_PADDING
            int r1 = r1 - r3
            float r4 = (float) r1
            android.graphics.Paint r6 = r0.unactiveBottomChartPaint
            r1 = r25
            r3 = r14
            r1.drawRect(r2, r3, r4, r5, r6)
            goto L_0x020b
        L_0x01f7:
            int r1 = HORIZONTAL_PADDING
            float r2 = (float) r1
            float r3 = (float) r9
            int r1 = r24.getMeasuredWidth()
            int r4 = HORIZONTAL_PADDING
            int r1 = r1 - r4
            float r4 = (float) r1
            float r5 = (float) r8
            android.graphics.Paint r6 = r0.unactiveBottomChartPaint
            r1 = r25
            r1.drawRect(r2, r3, r4, r5, r6)
        L_0x020b:
            org.telegram.ui.Charts.BaseChartView$SharedUiComponents r1 = r0.sharedUiComponents
            int r2 = r0.pikerHeight
            int r3 = r24.getMeasuredWidth()
            int r4 = HORIZONTAL_PADDING
            int r4 = r4 * 2
            int r3 = r3 - r4
            android.graphics.Bitmap r1 = r1.getPickerMaskBitmap(r2, r3)
            int r2 = HORIZONTAL_PADDING
            float r2 = (float) r2
            int r3 = r24.getMeasuredHeight()
            int r4 = PICKER_PADDING
            int r3 = r3 - r4
            int r4 = r0.pikerHeight
            int r3 = r3 - r4
            float r3 = (float) r3
            android.graphics.Paint r4 = r0.emptyPaint
            r7.drawBitmap(r1, r2, r3, r4)
            T r1 = r0.chartData
            if (r1 == 0) goto L_0x03a0
            android.graphics.Rect r1 = r0.pickerRect
            r1.set(r11, r9, r12, r8)
            org.telegram.ui.Charts.ChartPickerDelegate r1 = r0.pickerDelegate
            android.graphics.Rect r1 = r1.middlePickerArea
            android.graphics.Rect r2 = r0.pickerRect
            r1.set(r2)
            android.graphics.Path r1 = r0.pathTmp
            android.graphics.Rect r2 = r0.pickerRect
            int r3 = r2.left
            float r14 = (float) r3
            int r4 = r2.top
            int r5 = DP_1
            int r4 = r4 - r5
            float r15 = (float) r4
            int r4 = DP_12
            int r3 = r3 + r4
            float r3 = (float) r3
            int r2 = r2.bottom
            int r2 = r2 + r5
            float r2 = (float) r2
            int r4 = DP_6
            float r5 = (float) r4
            float r4 = (float) r4
            r20 = 1
            r21 = 0
            r22 = 0
            r23 = 1
            r13 = r1
            r16 = r3
            r17 = r2
            r18 = r5
            r19 = r4
            RoundedRect(r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23)
            android.graphics.Paint r2 = r0.pickerSelectorPaint
            r7.drawPath(r1, r2)
            android.graphics.Path r1 = r0.pathTmp
            android.graphics.Rect r2 = r0.pickerRect
            int r3 = r2.right
            int r4 = DP_12
            int r4 = r3 - r4
            float r14 = (float) r4
            int r4 = r2.top
            int r5 = DP_1
            int r4 = r4 - r5
            float r15 = (float) r4
            float r3 = (float) r3
            int r2 = r2.bottom
            int r2 = r2 + r5
            float r2 = (float) r2
            int r4 = DP_6
            float r5 = (float) r4
            float r4 = (float) r4
            r20 = 0
            r21 = 1
            r22 = 1
            r23 = 0
            r13 = r1
            r16 = r3
            r17 = r2
            r18 = r5
            r19 = r4
            RoundedRect(r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23)
            android.graphics.Paint r2 = r0.pickerSelectorPaint
            r7.drawPath(r1, r2)
            android.graphics.Rect r1 = r0.pickerRect
            int r2 = r1.left
            int r3 = DP_12
            int r2 = r2 + r3
            float r2 = (float) r2
            int r4 = r1.bottom
            float r5 = (float) r4
            int r1 = r1.right
            int r1 = r1 - r3
            float r6 = (float) r1
            int r1 = DP_1
            int r4 = r4 + r1
            float r13 = (float) r4
            android.graphics.Paint r14 = r0.pickerSelectorPaint
            r1 = r25
            r3 = r5
            r4 = r6
            r5 = r13
            r6 = r14
            r1.drawRect(r2, r3, r4, r5, r6)
            android.graphics.Rect r1 = r0.pickerRect
            int r2 = r1.left
            int r3 = DP_12
            int r2 = r2 + r3
            float r2 = (float) r2
            int r4 = r1.top
            int r5 = DP_1
            int r5 = r4 - r5
            float r5 = (float) r5
            int r1 = r1.right
            int r1 = r1 - r3
            float r6 = (float) r1
            float r13 = (float) r4
            android.graphics.Paint r14 = r0.pickerSelectorPaint
            r1 = r25
            r3 = r5
            r4 = r6
            r5 = r13
            r6 = r14
            r1.drawRect(r2, r3, r4, r5, r6)
            android.graphics.Rect r1 = r0.pickerRect
            int r2 = r1.left
            int r3 = DP_6
            int r2 = r2 + r3
            float r2 = (float) r2
            int r1 = r1.centerY()
            int r3 = DP_6
            int r1 = r1 - r3
            float r4 = (float) r1
            android.graphics.Rect r1 = r0.pickerRect
            int r5 = r1.left
            int r5 = r5 + r3
            float r5 = (float) r5
            int r1 = r1.centerY()
            int r3 = DP_6
            int r1 = r1 + r3
            float r6 = (float) r1
            android.graphics.Paint r13 = r0.whiteLinePaint
            r1 = r25
            r3 = r4
            r4 = r5
            r5 = r6
            r6 = r13
            r1.drawLine(r2, r3, r4, r5, r6)
            android.graphics.Rect r1 = r0.pickerRect
            int r2 = r1.right
            int r3 = DP_6
            int r2 = r2 - r3
            float r2 = (float) r2
            int r1 = r1.centerY()
            int r3 = DP_6
            int r1 = r1 - r3
            float r4 = (float) r1
            android.graphics.Rect r1 = r0.pickerRect
            int r5 = r1.right
            int r5 = r5 - r3
            float r5 = (float) r5
            int r1 = r1.centerY()
            int r3 = DP_6
            int r1 = r1 + r3
            float r6 = (float) r1
            android.graphics.Paint r13 = r0.whiteLinePaint
            r1 = r25
            r3 = r4
            r4 = r5
            r5 = r6
            r6 = r13
            r1.drawLine(r2, r3, r4, r5, r6)
            org.telegram.ui.Charts.ChartPickerDelegate r1 = r0.pickerDelegate
            org.telegram.ui.Charts.ChartPickerDelegate$CapturesData r1 = r1.getMiddleCaptured()
            android.graphics.Rect r2 = r0.pickerRect
            int r3 = r2.bottom
            int r2 = r2.top
            int r3 = r3 - r2
            int r3 = r3 >> r10
            int r2 = r2 + r3
            if (r1 == 0) goto L_0x0347
            goto L_0x0385
        L_0x0347:
            org.telegram.ui.Charts.ChartPickerDelegate r1 = r0.pickerDelegate
            org.telegram.ui.Charts.ChartPickerDelegate$CapturesData r1 = r1.getLeftCaptured()
            org.telegram.ui.Charts.ChartPickerDelegate r4 = r0.pickerDelegate
            org.telegram.ui.Charts.ChartPickerDelegate$CapturesData r4 = r4.getRightCaptured()
            if (r1 == 0) goto L_0x036c
            android.graphics.Rect r5 = r0.pickerRect
            int r5 = r5.left
            int r6 = DP_5
            int r5 = r5 + r6
            float r5 = (float) r5
            float r6 = (float) r2
            float r13 = (float) r3
            float r1 = r1.aValue
            float r13 = r13 * r1
            int r1 = DP_2
            float r1 = (float) r1
            float r13 = r13 - r1
            android.graphics.Paint r1 = r0.ripplePaint
            r7.drawCircle(r5, r6, r13, r1)
        L_0x036c:
            if (r4 == 0) goto L_0x0385
            android.graphics.Rect r1 = r0.pickerRect
            int r1 = r1.right
            int r5 = DP_5
            int r1 = r1 - r5
            float r1 = (float) r1
            float r2 = (float) r2
            float r3 = (float) r3
            float r4 = r4.aValue
            float r3 = r3 * r4
            int r4 = DP_2
            float r4 = (float) r4
            float r3 = r3 - r4
            android.graphics.Paint r4 = r0.ripplePaint
            r7.drawCircle(r1, r2, r3, r4)
        L_0x0385:
            org.telegram.ui.Charts.ChartPickerDelegate r1 = r0.pickerDelegate
            android.graphics.Rect r1 = r1.leftPickerArea
            int r2 = PICKER_CAPTURE_WIDTH
            int r3 = r11 - r2
            int r2 = r2 >> r10
            int r11 = r11 + r2
            r1.set(r3, r9, r11, r8)
            org.telegram.ui.Charts.ChartPickerDelegate r1 = r0.pickerDelegate
            android.graphics.Rect r1 = r1.rightPickerArea
            int r2 = PICKER_CAPTURE_WIDTH
            int r3 = r2 >> 1
            int r3 = r12 - r3
            int r12 = r12 + r2
            r1.set(r3, r9, r12, r8)
        L_0x03a0:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Charts.BaseChartView.drawPicker(android.graphics.Canvas):void");
    }

    private void setMaxMinValue(int i, int i2, boolean z) {
        setMaxMinValue(i, i2, z, false, false);
    }

    /* access modifiers changed from: protected */
    public void setMaxMinValue(int i, int i2, boolean z, boolean z2, boolean z3) {
        int i3 = i;
        if ((Math.abs(((float) ChartHorizontalLinesData.lookupHeight(i)) - this.animateToMaxHeight) >= this.thresholdMaxHeight && i3 != 0) || ((float) i3) != this.animateToMinHeight) {
            final ChartHorizontalLinesData createHorizontalLinesData = createHorizontalLinesData(i, i2);
            int[] iArr = createHorizontalLinesData.values;
            int i4 = iArr[iArr.length - 1];
            int i5 = iArr[0];
            if (!z3) {
                float f = this.currentMaxHeight;
                float f2 = this.currentMinHeight;
                float f3 = (float) (i4 - i5);
                float f4 = (f - f2) / f3;
                if (f4 > 1.0f) {
                    f4 = f3 / (f - f2);
                }
                float f5 = 0.045f;
                double d = (double) f4;
                if (d > 0.7d) {
                    f5 = 0.1f;
                } else if (d < 0.1d) {
                    f5 = 0.03f;
                }
                boolean z4 = ((float) i4) != this.animateToMaxHeight;
                if (this.useMinHeight && ((float) i5) != this.animateToMinHeight) {
                    z4 = true;
                }
                if (z4) {
                    Animator animator = this.maxValueAnimator;
                    if (animator != null) {
                        animator.removeAllListeners();
                        this.maxValueAnimator.cancel();
                    }
                    this.startFromMaxH = this.currentMaxHeight;
                    this.startFromMinH = this.currentMinHeight;
                    this.startFromMax = 0.0f;
                    this.startFromMin = 0.0f;
                    this.minMaxUpdateStep = f5;
                }
            }
            float f6 = (float) i4;
            this.animateToMaxHeight = f6;
            float f7 = (float) i5;
            this.animateToMinHeight = f7;
            measureHeightThreshold();
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - this.lastTime >= 320 || z2) {
                this.lastTime = currentTimeMillis;
                ValueAnimator valueAnimator = this.alphaAnimator;
                if (valueAnimator != null) {
                    valueAnimator.removeAllListeners();
                    this.alphaAnimator.cancel();
                }
                if (!z) {
                    this.currentMaxHeight = f6;
                    this.currentMinHeight = f7;
                    this.horizontalLines.clear();
                    this.horizontalLines.add(createHorizontalLinesData);
                    createHorizontalLinesData.alpha = 255;
                    return;
                }
                this.horizontalLines.add(createHorizontalLinesData);
                if (z3) {
                    Animator animator2 = this.maxValueAnimator;
                    if (animator2 != null) {
                        animator2.removeAllListeners();
                        this.maxValueAnimator.cancel();
                    }
                    this.minMaxUpdateStep = 0.0f;
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(new Animator[]{createAnimator(this.currentMaxHeight, f6, this.heightUpdateListener)});
                    if (this.useMinHeight) {
                        animatorSet.playTogether(new Animator[]{createAnimator(this.currentMinHeight, f7, this.minHeightUpdateListener)});
                    }
                    this.maxValueAnimator = animatorSet;
                    animatorSet.start();
                }
                int size = this.horizontalLines.size();
                for (int i6 = 0; i6 < size; i6++) {
                    ChartHorizontalLinesData chartHorizontalLinesData = this.horizontalLines.get(i6);
                    if (chartHorizontalLinesData != createHorizontalLinesData) {
                        chartHorizontalLinesData.fixedAlpha = chartHorizontalLinesData.alpha;
                    }
                }
                ValueAnimator createAnimator = createAnimator(0.0f, 255.0f, new ValueAnimator.AnimatorUpdateListener(createHorizontalLinesData) {
                    private final /* synthetic */ ChartHorizontalLinesData f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        BaseChartView.this.lambda$setMaxMinValue$2$BaseChartView(this.f$1, valueAnimator);
                    }
                });
                this.alphaAnimator = createAnimator;
                createAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        BaseChartView.this.horizontalLines.clear();
                        BaseChartView.this.horizontalLines.add(createHorizontalLinesData);
                    }
                });
                this.alphaAnimator.start();
            }
        }
    }

    public /* synthetic */ void lambda$setMaxMinValue$2$BaseChartView(ChartHorizontalLinesData chartHorizontalLinesData, ValueAnimator valueAnimator) {
        chartHorizontalLinesData.alpha = (int) ((Float) valueAnimator.getAnimatedValue()).floatValue();
        Iterator<ChartHorizontalLinesData> it = this.horizontalLines.iterator();
        while (it.hasNext()) {
            ChartHorizontalLinesData next = it.next();
            if (next != chartHorizontalLinesData) {
                next.alpha = (int) ((((float) next.fixedAlpha) / 255.0f) * ((float) (255 - chartHorizontalLinesData.alpha)));
            }
        }
        invalidate();
    }

    /* access modifiers changed from: protected */
    public ChartHorizontalLinesData createHorizontalLinesData(int i, int i2) {
        return new ChartHorizontalLinesData(i, i2, this.useMinHeight);
    }

    /* access modifiers changed from: package-private */
    public ValueAnimator createAnimator(float f, float f2, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{f, f2});
        ofFloat.setDuration(400);
        ofFloat.setInterpolator(INTERPOLATOR);
        ofFloat.addUpdateListener(animatorUpdateListener);
        return ofFloat;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (this.chartData == null) {
            return false;
        }
        if (!this.enabled) {
            this.pickerDelegate.uncapture(motionEvent, motionEvent.getActionIndex());
            getParent().requestDisallowInterceptTouchEvent(false);
            this.chartCaptured = false;
            return false;
        }
        int x = (int) motionEvent.getX(motionEvent.getActionIndex());
        int y = (int) motionEvent.getY(motionEvent.getActionIndex());
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    int i = x - this.lastX;
                    int i2 = y - this.lastY;
                    if (this.pickerDelegate.captured()) {
                        boolean move = this.pickerDelegate.move(x, y, motionEvent.getActionIndex());
                        if (motionEvent.getPointerCount() > 1) {
                            this.pickerDelegate.move((int) motionEvent.getX(1), (int) motionEvent.getY(1), 1);
                        }
                        getParent().requestDisallowInterceptTouchEvent(move);
                        return true;
                    }
                    if (this.chartCaptured) {
                        if ((this.canCaptureChartSelection && System.currentTimeMillis() - this.capturedTime > 200) || Math.abs(i) > Math.abs(i2) || Math.abs(i2) < this.touchSlop) {
                            z = true;
                        }
                        this.lastX = x;
                        this.lastY = y;
                        getParent().requestDisallowInterceptTouchEvent(z);
                        selectXOnChart(x, y);
                    } else if (this.chartArea.contains(this.capturedX, this.capturedY)) {
                        int i3 = this.capturedX - x;
                        int i4 = this.capturedY - y;
                        if (Math.sqrt((double) ((i3 * i3) + (i4 * i4))) > ((double) this.touchSlop) || System.currentTimeMillis() - this.capturedTime > 200) {
                            this.chartCaptured = true;
                            selectXOnChart(x, y);
                        }
                    }
                    return true;
                } else if (actionMasked != 3) {
                    if (actionMasked == 5) {
                        return this.pickerDelegate.capture(x, y, motionEvent.getActionIndex());
                    }
                    if (actionMasked != 6) {
                        return false;
                    }
                    this.pickerDelegate.uncapture(motionEvent, motionEvent.getActionIndex());
                    return true;
                }
            }
            if (this.pickerDelegate.uncapture(motionEvent, motionEvent.getActionIndex())) {
                return true;
            }
            if (this.chartArea.contains(this.capturedX, this.capturedY) && !this.chartCaptured) {
                animateLegend(false);
            }
            this.pickerDelegate.uncapture();
            updateLineSignature();
            getParent().requestDisallowInterceptTouchEvent(false);
            this.chartCaptured = false;
            onActionUp();
            invalidate();
            setMaxMinValue(findMaxValue(this.startXIndex, this.endXIndex), this.useMinHeight ? findMinValue(this.startXIndex, this.endXIndex) : 0, true, true, false);
            return true;
        }
        this.capturedTime = System.currentTimeMillis();
        getParent().requestDisallowInterceptTouchEvent(true);
        if (this.pickerDelegate.capture(x, y, motionEvent.getActionIndex())) {
            return true;
        }
        this.lastX = x;
        this.capturedX = x;
        this.lastY = y;
        this.capturedY = y;
        if (!this.chartArea.contains(x, y)) {
            return false;
        }
        if (this.selectedIndex < 0 || !this.animateLegentTo) {
            this.chartCaptured = true;
            selectXOnChart(x, y);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void selectXOnChart(int i, int i2) {
        T t = this.chartData;
        if (t != null) {
            float f = this.chartFullWidth;
            float f2 = (this.pickerDelegate.pickerStart * f) - ((float) HORIZONTAL_PADDING);
            float f3 = (((float) i) + f2) / f;
            if (f3 < 0.0f) {
                this.selectedIndex = 0;
            } else if (f3 > 1.0f) {
                this.selectedIndex = t.x.length - 1;
            } else {
                int findIndex = t.findIndex(this.startXIndex, this.endXIndex, f3);
                this.selectedIndex = findIndex;
                int i3 = findIndex + 1;
                float[] fArr = this.chartData.xPercentage;
                if (i3 < fArr.length) {
                    if (Math.abs(this.chartData.xPercentage[this.selectedIndex + 1] - f3) < Math.abs(fArr[findIndex] - f3)) {
                        this.selectedIndex++;
                    }
                }
            }
            int i4 = this.selectedIndex;
            int i5 = this.endXIndex;
            if (i4 > i5) {
                this.selectedIndex = i5;
            }
            int i6 = this.selectedIndex;
            int i7 = this.startXIndex;
            if (i6 < i7) {
                this.selectedIndex = i7;
            }
            this.legendShowing = true;
            animateLegend(true);
            moveLegend(f2);
            DateSelectionListener dateSelectionListener2 = this.dateSelectionListener;
            if (dateSelectionListener2 != null) {
                dateSelectionListener2.onDateSelected(getSelectedDate());
            }
            invalidate();
        }
    }

    public void animateLegend(boolean z) {
        moveLegend();
        if (this.animateLegentTo != z) {
            this.animateLegentTo = z;
            ValueAnimator valueAnimator = this.selectionAnimator;
            if (valueAnimator != null) {
                valueAnimator.removeAllListeners();
                this.selectionAnimator.cancel();
            }
            ValueAnimator duration = createAnimator(this.selectionA, z ? 1.0f : 0.0f, this.selectionAnimatorListener).setDuration(200);
            this.selectionAnimator = duration;
            duration.addListener(this.selectorAnimatorEndListener);
            this.selectionAnimator.start();
        }
    }

    public void moveLegend(float f) {
        int i;
        float f2;
        T t = this.chartData;
        if (t != null && (i = this.selectedIndex) != -1 && this.legendShowing) {
            this.legendSignatureView.setData(i, t.x[i], this.lines, false);
            this.legendSignatureView.setVisibility(0);
            this.legendSignatureView.measure(View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), Integer.MIN_VALUE));
            float f3 = (this.chartData.xPercentage[this.selectedIndex] * this.chartFullWidth) - f;
            if (f3 > ((float) ((this.chartStart + this.chartWidth) >> 1))) {
                f2 = f3 - ((float) (this.legendSignatureView.getWidth() + DP_5));
            } else {
                f2 = f3 + ((float) DP_5);
            }
            if (f2 < 0.0f) {
                f2 = 0.0f;
            } else if (((float) this.legendSignatureView.getMeasuredWidth()) + f2 > ((float) getMeasuredWidth())) {
                f2 = (float) (getMeasuredWidth() - this.legendSignatureView.getMeasuredWidth());
            }
            this.legendSignatureView.setTranslationX(f2);
        }
    }

    public int findMaxValue(int i, int i2) {
        int rMaxQ;
        int size = this.lines.size();
        int i3 = 0;
        for (int i4 = 0; i4 < size; i4++) {
            if (((LineViewData) this.lines.get(i4)).enabled && (rMaxQ = ((LineViewData) this.lines.get(i4)).line.segmentTree.rMaxQ(i, i2)) > i3) {
                i3 = rMaxQ;
            }
        }
        return i3;
    }

    public int findMinValue(int i, int i2) {
        int rMinQ;
        int size = this.lines.size();
        int i3 = Integer.MAX_VALUE;
        for (int i4 = 0; i4 < size; i4++) {
            if (((LineViewData) this.lines.get(i4)).enabled && (rMinQ = ((LineViewData) this.lines.get(i4)).line.segmentTree.rMinQ(i, i2)) < i3) {
                i3 = rMinQ;
            }
        }
        return i3;
    }

    public void setData(T t) {
        if (this.chartData != t) {
            invalidate();
            this.lines.clear();
            if (!(t == null || t.lines == null)) {
                for (int i = 0; i < t.lines.size(); i++) {
                    this.lines.add(createLineViewData(t.lines.get(i)));
                }
            }
            clearSelection();
            this.chartData = t;
            if (t != null) {
                if (t.x[0] == 0) {
                    ChartPickerDelegate chartPickerDelegate = this.pickerDelegate;
                    chartPickerDelegate.pickerStart = 0.0f;
                    chartPickerDelegate.pickerEnd = 1.0f;
                } else {
                    this.pickerDelegate.minDistance = getMinDistance();
                    ChartPickerDelegate chartPickerDelegate2 = this.pickerDelegate;
                    float f = chartPickerDelegate2.pickerEnd;
                    float f2 = chartPickerDelegate2.minDistance;
                    if (f - chartPickerDelegate2.pickerStart < f2) {
                        float f3 = f - f2;
                        chartPickerDelegate2.pickerStart = f3;
                        if (f3 < 0.0f) {
                            chartPickerDelegate2.pickerStart = 0.0f;
                            chartPickerDelegate2.pickerEnd = 1.0f;
                        }
                    }
                }
            }
        }
        measureSizes();
        if (t != null) {
            updateIndexes();
            setMaxMinValue(findMaxValue(this.startXIndex, this.endXIndex), this.useMinHeight ? findMinValue(this.startXIndex, this.endXIndex) : 0, false);
            this.pickerMaxHeight = 0.0f;
            this.pickerMinHeight = 2.14748365E9f;
            initPickerMaxHeight();
            this.legendSignatureView.setSize(this.lines.size());
            this.invalidatePickerChart = true;
            updateLineSignature();
            return;
        }
        ChartPickerDelegate chartPickerDelegate3 = this.pickerDelegate;
        chartPickerDelegate3.pickerStart = 0.7f;
        chartPickerDelegate3.pickerEnd = 1.0f;
        this.pickerMinHeight = 0.0f;
        this.pickerMaxHeight = 0.0f;
        this.horizontalLines.clear();
        Animator animator = this.maxValueAnimator;
        if (animator != null) {
            animator.cancel();
        }
        ValueAnimator valueAnimator = this.alphaAnimator;
        if (valueAnimator != null) {
            valueAnimator.removeAllListeners();
            this.alphaAnimator.cancel();
        }
    }

    /* access modifiers changed from: protected */
    public float getMinDistance() {
        T t = this.chartData;
        if (t == null) {
            return 0.1f;
        }
        int length = t.x.length;
        if (length < 5) {
            return 1.0f;
        }
        float f = 5.0f / ((float) length);
        if (f < 0.1f) {
            return 0.1f;
        }
        return f;
    }

    /* access modifiers changed from: protected */
    public void initPickerMaxHeight() {
        Iterator<L> it = this.lines.iterator();
        while (it.hasNext()) {
            LineViewData lineViewData = (LineViewData) it.next();
            if (lineViewData.enabled) {
                int i = lineViewData.line.maxValue;
                if (((float) i) > this.pickerMaxHeight) {
                    this.pickerMaxHeight = (float) i;
                }
            }
            if (lineViewData.enabled) {
                int i2 = lineViewData.line.minValue;
                if (((float) i2) < this.pickerMinHeight) {
                    this.pickerMinHeight = (float) i2;
                }
            }
            float f = this.pickerMaxHeight;
            float f2 = this.pickerMinHeight;
            if (f == f2) {
                this.pickerMaxHeight = f + 1.0f;
                this.pickerMinHeight = f2 - 1.0f;
            }
        }
    }

    public void onPickerDataChanged() {
        onPickerDataChanged(true, false, false);
    }

    public void onPickerDataChanged(boolean z, boolean z2, boolean z3) {
        if (this.chartData != null) {
            ChartPickerDelegate chartPickerDelegate = this.pickerDelegate;
            this.chartFullWidth = ((float) this.chartWidth) / (chartPickerDelegate.pickerEnd - chartPickerDelegate.pickerStart);
            updateIndexes();
            setMaxMinValue(findMaxValue(this.startXIndex, this.endXIndex), this.useMinHeight ? findMinValue(this.startXIndex, this.endXIndex) : 0, z, z2, z3);
            if (this.legendShowing && !z2) {
                animateLegend(false);
                moveLegend((this.chartFullWidth * this.pickerDelegate.pickerStart) - ((float) HORIZONTAL_PADDING));
            }
            invalidate();
        }
    }

    public void onPickerJumpTo(float f, float f2, boolean z) {
        T t = this.chartData;
        if (t != null) {
            if (z) {
                int findStartIndex = t.findStartIndex(Math.max(f, 0.0f));
                int findEndIndex = this.chartData.findEndIndex(findStartIndex, Math.min(f2, 1.0f));
                setMaxMinValue(findMaxValue(findStartIndex, findEndIndex), findMinValue(findStartIndex, findEndIndex), true, true, false);
                animateLegend(false);
                return;
            }
            updateIndexes();
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void updateIndexes() {
        T t = this.chartData;
        if (t != null) {
            int findStartIndex = t.findStartIndex(Math.max(this.pickerDelegate.pickerStart, 0.0f));
            this.startXIndex = findStartIndex;
            int findEndIndex = this.chartData.findEndIndex(findStartIndex, Math.min(this.pickerDelegate.pickerEnd, 1.0f));
            this.endXIndex = findEndIndex;
            ChartHeaderView chartHeaderView2 = this.chartHeaderView;
            if (chartHeaderView2 != null) {
                long[] jArr = this.chartData.x;
                chartHeaderView2.setDates(jArr[this.startXIndex], jArr[findEndIndex]);
            }
            updateLineSignature();
        }
    }

    private void updateLineSignature() {
        int i;
        T t = this.chartData;
        if (t != null && (i = this.chartWidth) != 0) {
            updateDates((int) ((((float) i) / (this.chartFullWidth * t.oneDayPercentage)) / 6.0f));
        }
    }

    private void updateDates(int i) {
        ChartBottomSignatureData chartBottomSignatureData = this.currentBottomSignatures;
        if (chartBottomSignatureData == null || i >= chartBottomSignatureData.stepMax || i <= chartBottomSignatureData.stepMin) {
            int highestOneBit = Integer.highestOneBit(i) << 1;
            ChartBottomSignatureData chartBottomSignatureData2 = this.currentBottomSignatures;
            if (chartBottomSignatureData2 == null || chartBottomSignatureData2.step != highestOneBit) {
                ValueAnimator valueAnimator = this.alphaBottomAnimator;
                if (valueAnimator != null) {
                    valueAnimator.removeAllListeners();
                    this.alphaBottomAnimator.cancel();
                }
                double d = (double) highestOneBit;
                Double.isNaN(d);
                double d2 = 0.2d * d;
                Double.isNaN(d);
                Double.isNaN(d);
                final ChartBottomSignatureData chartBottomSignatureData3 = new ChartBottomSignatureData(highestOneBit, (int) (d + d2), (int) (d - d2));
                chartBottomSignatureData3.alpha = 255;
                if (this.currentBottomSignatures == null) {
                    this.currentBottomSignatures = chartBottomSignatureData3;
                    chartBottomSignatureData3.alpha = 255;
                    this.bottomSignatureDate.add(chartBottomSignatureData3);
                    return;
                }
                this.currentBottomSignatures = chartBottomSignatureData3;
                this.tmpN = this.bottomSignatureDate.size();
                for (int i2 = 0; i2 < this.tmpN; i2++) {
                    ChartBottomSignatureData chartBottomSignatureData4 = this.bottomSignatureDate.get(i2);
                    chartBottomSignatureData4.fixedAlpha = chartBottomSignatureData4.alpha;
                }
                this.bottomSignatureDate.add(chartBottomSignatureData3);
                if (this.bottomSignatureDate.size() > 2) {
                    this.bottomSignatureDate.remove(0);
                }
                ValueAnimator duration = createAnimator(0.0f, 1.0f, new ValueAnimator.AnimatorUpdateListener(chartBottomSignatureData3) {
                    private final /* synthetic */ ChartBottomSignatureData f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        BaseChartView.this.lambda$updateDates$3$BaseChartView(this.f$1, valueAnimator);
                    }
                }).setDuration(200);
                this.alphaBottomAnimator = duration;
                duration.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);
                        BaseChartView.this.bottomSignatureDate.clear();
                        BaseChartView.this.bottomSignatureDate.add(chartBottomSignatureData3);
                    }
                });
                this.alphaBottomAnimator.start();
            }
        }
    }

    public /* synthetic */ void lambda$updateDates$3$BaseChartView(ChartBottomSignatureData chartBottomSignatureData, ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        Iterator<ChartBottomSignatureData> it = this.bottomSignatureDate.iterator();
        while (it.hasNext()) {
            ChartBottomSignatureData next = it.next();
            if (next == chartBottomSignatureData) {
                chartBottomSignatureData.alpha = (int) (255.0f * floatValue);
            } else {
                next.alpha = (int) ((1.0f - floatValue) * ((float) next.fixedAlpha));
            }
        }
        invalidate();
    }

    public void onCheckChanged() {
        ValueAnimator valueAnimator;
        ValueAnimator valueAnimator2;
        ValueAnimator valueAnimator3;
        onPickerDataChanged(true, true, true);
        this.tmpN = this.lines.size();
        int i = 0;
        while (true) {
            this.tmpI = i;
            int i2 = this.tmpI;
            if (i2 >= this.tmpN) {
                break;
            }
            LineViewData lineViewData = (LineViewData) this.lines.get(i2);
            if (lineViewData.enabled && (valueAnimator3 = lineViewData.animatorOut) != null) {
                valueAnimator3.cancel();
            }
            if (!lineViewData.enabled && (valueAnimator2 = lineViewData.animatorIn) != null) {
                valueAnimator2.cancel();
            }
            if (lineViewData.enabled && lineViewData.alpha != 1.0f) {
                ValueAnimator valueAnimator4 = lineViewData.animatorIn;
                if (valueAnimator4 == null || !valueAnimator4.isRunning()) {
                    ValueAnimator createAnimator = createAnimator(lineViewData.alpha, 1.0f, new ValueAnimator.AnimatorUpdateListener(lineViewData) {
                        private final /* synthetic */ LineViewData f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                            BaseChartView.this.lambda$onCheckChanged$4$BaseChartView(this.f$1, valueAnimator);
                        }
                    });
                    lineViewData.animatorIn = createAnimator;
                    createAnimator.start();
                } else {
                    i = this.tmpI + 1;
                }
            }
            if (!lineViewData.enabled && lineViewData.alpha != 0.0f && ((valueAnimator = lineViewData.animatorOut) == null || !valueAnimator.isRunning())) {
                ValueAnimator createAnimator2 = createAnimator(lineViewData.alpha, 0.0f, new ValueAnimator.AnimatorUpdateListener(lineViewData) {
                    private final /* synthetic */ LineViewData f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        BaseChartView.this.lambda$onCheckChanged$5$BaseChartView(this.f$1, valueAnimator);
                    }
                });
                lineViewData.animatorOut = createAnimator2;
                createAnimator2.start();
            }
            i = this.tmpI + 1;
        }
        updatePickerMinMaxHeight();
        if (this.legendShowing) {
            LegendSignatureView legendSignatureView2 = this.legendSignatureView;
            int i3 = this.selectedIndex;
            legendSignatureView2.setData(i3, this.chartData.x[i3], this.lines, true);
        }
    }

    public /* synthetic */ void lambda$onCheckChanged$4$BaseChartView(LineViewData lineViewData, ValueAnimator valueAnimator) {
        lineViewData.alpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.invalidatePickerChart = true;
        invalidate();
    }

    public /* synthetic */ void lambda$onCheckChanged$5$BaseChartView(LineViewData lineViewData, ValueAnimator valueAnimator) {
        lineViewData.alpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.invalidatePickerChart = true;
        invalidate();
    }

    /* access modifiers changed from: protected */
    public void updatePickerMinMaxHeight() {
        int i;
        int i2;
        if (ANIMATE_PICKER_SIZES) {
            Iterator<L> it = this.lines.iterator();
            int i3 = Integer.MAX_VALUE;
            int i4 = 0;
            while (it.hasNext()) {
                LineViewData lineViewData = (LineViewData) it.next();
                if (lineViewData.enabled && (i2 = lineViewData.line.maxValue) > i4) {
                    i4 = i2;
                }
                if (lineViewData.enabled && (i = lineViewData.line.minValue) < i3) {
                    i3 = i;
                }
            }
            if ((i3 != Integer.MAX_VALUE && ((float) i3) != this.animatedToPickerMinHeight) || (i4 > 0 && ((float) i4) != this.animatedToPickerMaxHeight)) {
                this.animatedToPickerMaxHeight = (float) i4;
                Animator animator = this.pickerAnimator;
                if (animator != null) {
                    animator.cancel();
                }
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(new Animator[]{createAnimator(this.pickerMaxHeight, this.animatedToPickerMaxHeight, this.pickerHeightUpdateListener), createAnimator(this.pickerMinHeight, this.animatedToPickerMinHeight, this.pickerMinHeightUpdateListener)});
                this.pickerAnimator = animatorSet;
                animatorSet.start();
            }
        }
    }

    public void setLandscape(boolean z) {
        this.landscape = z;
    }

    public void setHeader(ChartHeaderView chartHeaderView2) {
        this.chartHeaderView = chartHeaderView2;
    }

    public long getSelectedDate() {
        int i = this.selectedIndex;
        if (i < 0) {
            return -1;
        }
        return this.chartData.x[i];
    }

    public void clearSelection() {
        this.selectedIndex = -1;
        this.legendShowing = false;
        this.animateLegentTo = false;
        this.legendSignatureView.setVisibility(8);
        this.selectionA = 0.0f;
    }

    public void selectDate(long j) {
        this.selectedIndex = Arrays.binarySearch(this.chartData.x, j);
        this.legendShowing = true;
        this.legendSignatureView.setVisibility(0);
        this.selectionA = 1.0f;
        moveLegend((this.chartFullWidth * this.pickerDelegate.pickerStart) - ((float) HORIZONTAL_PADDING));
    }

    public long getStartDate() {
        return this.chartData.x[this.startXIndex];
    }

    public long getEndDate() {
        return this.chartData.x[this.endXIndex];
    }

    public void updatePicker(ChartData chartData2, long j) {
        int length = chartData2.x.length;
        long j2 = j - (j % 86400000);
        long j3 = (86400000 + j2) - 1;
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < length; i3++) {
            if (j2 > chartData2.x[i3]) {
                i = i3;
            }
            if (j3 > chartData2.x[i3]) {
                i2 = i3;
            }
        }
        ChartPickerDelegate chartPickerDelegate = this.pickerDelegate;
        float[] fArr = chartData2.xPercentage;
        chartPickerDelegate.pickerStart = fArr[i];
        chartPickerDelegate.pickerEnd = fArr[i2];
    }

    public void moveLegend() {
        moveLegend((this.chartFullWidth * this.pickerDelegate.pickerStart) - ((float) HORIZONTAL_PADDING));
    }

    public void requestLayout() {
        super.requestLayout();
    }

    public static Path RoundedRect(Path path, float f, float f2, float f3, float f4, float f5, float f6, boolean z, boolean z2, boolean z3, boolean z4) {
        path.reset();
        if (f5 < 0.0f) {
            f5 = 0.0f;
        }
        if (f6 < 0.0f) {
            f6 = 0.0f;
        }
        float f7 = f3 - f;
        float f8 = f4 - f2;
        float f9 = f7 / 2.0f;
        if (f5 > f9) {
            f5 = f9;
        }
        float var_ = f8 / 2.0f;
        if (f6 > var_) {
            f6 = var_;
        }
        float var_ = f7 - (f5 * 2.0f);
        float var_ = f8 - (2.0f * f6);
        path.moveTo(f3, f2 + f6);
        if (z2) {
            float var_ = -f6;
            path.rQuadTo(0.0f, var_, -f5, var_);
        } else {
            path.rLineTo(0.0f, -f6);
            path.rLineTo(-f5, 0.0f);
        }
        path.rLineTo(-var_, 0.0f);
        if (z) {
            float var_ = -f5;
            path.rQuadTo(var_, 0.0f, var_, f6);
        } else {
            path.rLineTo(-f5, 0.0f);
            path.rLineTo(0.0f, f6);
        }
        path.rLineTo(0.0f, var_);
        if (z4) {
            path.rQuadTo(0.0f, f6, f5, f6);
        } else {
            path.rLineTo(0.0f, f6);
            path.rLineTo(f5, 0.0f);
        }
        path.rLineTo(var_, 0.0f);
        if (z3) {
            path.rQuadTo(f5, 0.0f, f5, -f6);
        } else {
            path.rLineTo(f5, 0.0f);
            path.rLineTo(0.0f, -f6);
        }
        path.rLineTo(0.0f, -var_);
        path.close();
        return path;
    }

    public void setDateSelectionListener(DateSelectionListener dateSelectionListener2) {
        this.dateSelectionListener = dateSelectionListener2;
    }

    public static class SharedUiComponents {
        private Canvas canvas;
        private boolean invalidate;
        int k;
        private Bitmap pickerRoundBitmap;
        private RectF rectF = new RectF();
        private Paint xRefP;

        public SharedUiComponents() {
            Paint paint = new Paint(1);
            this.xRefP = paint;
            this.k = 0;
            this.invalidate = true;
            paint.setColor(0);
            this.xRefP.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        /* access modifiers changed from: package-private */
        public Bitmap getPickerMaskBitmap(int i, int i2) {
            int i3 = (i + i2) << 10;
            if (i3 != this.k || this.invalidate) {
                this.invalidate = false;
                this.k = i3;
                this.pickerRoundBitmap = Bitmap.createBitmap(i2, i, Bitmap.Config.ARGB_8888);
                this.canvas = new Canvas(this.pickerRoundBitmap);
                this.rectF.set(0.0f, 0.0f, (float) i2, (float) i);
                this.canvas.drawColor(Theme.getColor("windowBackgroundWhite"));
                this.canvas.drawRoundRect(this.rectF, (float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(4.0f), this.xRefP);
            }
            return this.pickerRoundBitmap;
        }

        public void invalidate() {
            this.invalidate = true;
        }
    }
}
