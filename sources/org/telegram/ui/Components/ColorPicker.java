package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.util.Property;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;

public class ColorPicker extends FrameLayout {
    private static final int item_delete = 3;
    private static final int item_edit = 1;
    private static final int item_share = 2;
    private Drawable circleDrawable;
    private Paint circlePaint;
    private boolean circlePressed;
    private ImageView clearButton;
    private EditTextBoldCursor[] colorEditText;
    private LinearGradient colorGradient;
    private float[] colorHSV;
    private boolean colorPressed;
    private Bitmap colorWheelBitmap;
    private Paint colorWheelPaint;
    private int colorWheelWidth;
    private int currentResetType;
    private final ColorPickerDelegate delegate;
    private Paint editTextCirclePaint;
    private ImageView exchangeButton;
    private float[] hsvTemp;
    boolean ignoreTextChange;
    private long lastUpdateTime;
    private Paint linePaint;
    private float maxBrightness;
    private float maxHsvBrightness;
    private ActionBarMenuItem menuItem;
    private float minBrightness;
    private float minHsvBrightness;
    private boolean myMessagesColor;
    private int originalFirstColor;
    private float pressedMoveProgress;
    private TextView resetButton;
    private int selectedEditText;
    private RectF sliderRect = new RectF();
    private Paint valueSliderPaint;

    public interface ColorPickerDelegate {

        public final /* synthetic */ class -CC {
            public static void $default$deleteTheme(ColorPickerDelegate colorPickerDelegate) {
            }

            public static int $default$getDefaultColor(ColorPickerDelegate colorPickerDelegate, int i) {
                return 0;
            }

            public static boolean $default$hasChanges(ColorPickerDelegate colorPickerDelegate) {
                return true;
            }

            public static void $default$openThemeCreate(ColorPickerDelegate colorPickerDelegate, boolean z) {
            }

            public static void $default$rotateColors(ColorPickerDelegate colorPickerDelegate) {
            }
        }

        void deleteTheme();

        int getDefaultColor(int i);

        boolean hasChanges();

        void openThemeCreate(boolean z);

        void rotateColors();

        void setColor(int i, int i2, boolean z);
    }

    public ColorPicker(Context context, boolean z, ColorPickerDelegate colorPickerDelegate) {
        String str;
        Context context2 = context;
        super(context);
        int i = 4;
        this.colorEditText = new EditTextBoldCursor[4];
        this.colorHSV = new float[]{0.0f, 0.0f, 1.0f};
        this.hsvTemp = new float[3];
        this.pressedMoveProgress = 1.0f;
        this.minBrightness = 0.0f;
        this.maxBrightness = 1.0f;
        this.minHsvBrightness = 0.0f;
        this.maxHsvBrightness = 1.0f;
        this.delegate = colorPickerDelegate;
        setWillNotDraw(false);
        this.circleDrawable = context.getResources().getDrawable(NUM).mutate();
        this.circlePaint = new Paint(1);
        this.colorWheelPaint = new Paint(5);
        this.valueSliderPaint = new Paint(5);
        this.editTextCirclePaint = new Paint(1);
        this.linePaint = new Paint();
        this.linePaint.setColor(NUM);
        LinearLayout linearLayout = new LinearLayout(context2);
        linearLayout.setOrientation(0);
        addView(linearLayout, LayoutHelper.createFrame(-1, 54.0f, 51, 22.0f, 0.0f, 22.0f, 0.0f));
        int i2 = 0;
        while (true) {
            str = "windowBackgroundWhiteBlackText";
            if (i2 >= i) {
                break;
            }
            if (i2 == 0 || i2 == 2) {
                this.colorEditText[i2] = new EditTextBoldCursor(context2) {
                    private int lastColor = -1;

                    public boolean onTouchEvent(MotionEvent motionEvent) {
                        if (getAlpha() == 1.0f && motionEvent.getAction() == 0) {
                            if (ColorPicker.this.colorEditText[i2 + 1].isFocused()) {
                                AndroidUtilities.showKeyboard(ColorPicker.this.colorEditText[i2 + 1]);
                            } else {
                                ColorPicker.this.colorEditText[i2 + 1].requestFocus();
                            }
                        }
                        return false;
                    }

                    /* Access modifiers changed, original: protected */
                    public void onDraw(Canvas canvas) {
                        super.onDraw(canvas);
                        int access$100 = ColorPicker.this.getFieldColor(i2 + 1, this.lastColor);
                        this.lastColor = access$100;
                        ColorPicker.this.editTextCirclePaint.setColor(access$100);
                        canvas.drawCircle((float) AndroidUtilities.dp(10.0f), (float) AndroidUtilities.dp(21.0f), (float) AndroidUtilities.dp(10.0f), ColorPicker.this.editTextCirclePaint);
                    }
                };
                this.colorEditText[i2].setBackgroundDrawable(null);
                this.colorEditText[i2].setPadding(AndroidUtilities.dp(28.0f), AndroidUtilities.dp(5.0f), 0, AndroidUtilities.dp(18.0f));
                this.colorEditText[i2].setText("#");
                this.colorEditText[i2].setEnabled(false);
                this.colorEditText[i2].setFocusable(false);
                linearLayout.addView(this.colorEditText[i2], LayoutHelper.createLinear(-2, -1, i2 == 2 ? 39.0f : 0.0f, 0.0f, 0.0f, 0.0f));
            } else {
                this.colorEditText[i2] = new EditTextBoldCursor(context2) {
                    public boolean onTouchEvent(MotionEvent motionEvent) {
                        if (getAlpha() != 1.0f) {
                            return false;
                        }
                        if (isFocused()) {
                            AndroidUtilities.showKeyboard(this);
                            return super.onTouchEvent(motionEvent);
                        }
                        requestFocus();
                        return false;
                    }

                    public boolean getGlobalVisibleRect(Rect rect, Point point) {
                        boolean globalVisibleRect = super.getGlobalVisibleRect(rect, point);
                        rect.bottom += AndroidUtilities.dp(40.0f);
                        return globalVisibleRect;
                    }

                    public void invalidate() {
                        super.invalidate();
                        ColorPicker.this.colorEditText[i2 - 1].invalidate();
                    }
                };
                this.colorEditText[i2].setBackgroundDrawable(null);
                this.colorEditText[i2].setFilters(new InputFilter[]{new LengthFilter(6)});
                this.colorEditText[i2].setPadding(0, AndroidUtilities.dp(5.0f), 0, AndroidUtilities.dp(18.0f));
                this.colorEditText[i2].setHint("8BC6ED");
                linearLayout.addView(this.colorEditText[i2], LayoutHelper.createLinear(71, -1));
                this.colorEditText[i2].addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    }

                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    }

                    public void afterTextChanged(Editable editable) {
                        ColorPicker colorPicker = ColorPicker.this;
                        if (!colorPicker.ignoreTextChange) {
                            colorPicker.ignoreTextChange = true;
                            int i = 0;
                            while (i < editable.length()) {
                                char charAt = editable.charAt(i);
                                if ((charAt < '0' || charAt > '9') && ((charAt < 'a' || charAt > 'f') && (charAt < 'A' || charAt > 'F'))) {
                                    editable.replace(i, i + 1, "");
                                    i--;
                                }
                                i++;
                            }
                            if (editable.length() == 0) {
                                ColorPicker.this.ignoreTextChange = false;
                                return;
                            }
                            ColorPicker colorPicker2 = ColorPicker.this;
                            colorPicker2.setColorInner(colorPicker2.getFieldColor(i2, -1));
                            i = ColorPicker.this.getColor();
                            if (editable.length() == 6) {
                                editable.replace(0, editable.length(), String.format("%02x%02x%02x", new Object[]{Byte.valueOf((byte) Color.red(i)), Byte.valueOf((byte) Color.green(i)), Byte.valueOf((byte) Color.blue(i))}).toUpperCase());
                                ColorPicker.this.colorEditText[i2].setSelection(editable.length());
                            }
                            ColorPicker.this.delegate.setColor(i, i2 == 1 ? 0 : 1, true);
                            ColorPicker.this.ignoreTextChange = false;
                        }
                    }
                });
                this.colorEditText[i2].setOnFocusChangeListener(new -$$Lambda$ColorPicker$driZ199d8cznEBginjwHpCCMwGA(this, i2));
                this.colorEditText[i2].setOnEditorActionListener(-$$Lambda$ColorPicker$dvQ2Se2AeZAVZzn8bejAn986bSg.INSTANCE);
            }
            this.colorEditText[i2].setTextSize(1, 18.0f);
            this.colorEditText[i2].setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
            this.colorEditText[i2].setTextColor(Theme.getColor(str));
            this.colorEditText[i2].setCursorColor(Theme.getColor(str));
            this.colorEditText[i2].setCursorSize(AndroidUtilities.dp(20.0f));
            this.colorEditText[i2].setCursorWidth(1.5f);
            this.colorEditText[i2].setSingleLine(true);
            this.colorEditText[i2].setGravity(19);
            this.colorEditText[i2].setHeaderHintColor(Theme.getColor("windowBackgroundWhiteBlueHeader"));
            this.colorEditText[i2].setTransformHintToHeader(true);
            this.colorEditText[i2].setInputType(524416);
            this.colorEditText[i2].setImeOptions(NUM);
            if (i2 == 1) {
                this.colorEditText[i2].requestFocus();
            } else if (i2 == 2 || i2 == 3) {
                this.colorEditText[i2].setVisibility(8);
            }
            i2++;
            i = 4;
        }
        this.exchangeButton = new ImageView(getContext());
        String str2 = "dialogButtonSelector";
        this.exchangeButton.setBackground(Theme.createSelectorDrawable(Theme.getColor(str2), 1));
        this.exchangeButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(str), Mode.MULTIPLY));
        this.exchangeButton.setScaleType(ScaleType.CENTER);
        this.exchangeButton.setVisibility(8);
        this.exchangeButton.setOnClickListener(new -$$Lambda$ColorPicker$Q_7qiTXUzWsnEuq6-ZZ9ro-xYuA(this));
        addView(this.exchangeButton, LayoutHelper.createFrame(42, 42.0f, 51, 126.0f, 0.0f, 0.0f, 0.0f));
        this.clearButton = new ImageView(getContext());
        this.clearButton.setBackground(Theme.createSelectorDrawable(Theme.getColor(str2), 1));
        this.clearButton.setImageDrawable(new CloseProgressDrawable2());
        this.clearButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(str), Mode.MULTIPLY));
        this.clearButton.setScaleType(ScaleType.CENTER);
        this.clearButton.setVisibility(8);
        this.clearButton.setOnClickListener(new -$$Lambda$ColorPicker$3qnlmHCco11RT0NQHyIBY_wKf_U(this));
        this.clearButton.setContentDescription(LocaleController.getString("ClearButton", NUM));
        addView(this.clearButton, LayoutHelper.createFrame(42, 42.0f, 53, 0.0f, 0.0f, 9.0f, 0.0f));
        this.resetButton = new TextView(context2);
        this.resetButton.setTextSize(1, 15.0f);
        this.resetButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.resetButton.setGravity(17);
        this.resetButton.setPadding(AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(4.0f), 0);
        this.resetButton.setTextColor(Theme.getColor(str));
        addView(this.resetButton, LayoutHelper.createFrame(-2, 36.0f, 53, 0.0f, 3.0f, 14.0f, 0.0f));
        this.resetButton.setOnClickListener(new -$$Lambda$ColorPicker$bp18tJLETqQInYZmyn7bmDD68ps(this));
        if (z) {
            this.menuItem = new ActionBarMenuItem(context2, null, 0, Theme.getColor(str));
            this.menuItem.setLongClickEnabled(false);
            this.menuItem.setIcon(NUM);
            this.menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", NUM));
            this.menuItem.addSubItem(1, NUM, LocaleController.getString("OpenInEditor", NUM));
            this.menuItem.addSubItem(2, NUM, LocaleController.getString("ShareTheme", NUM));
            this.menuItem.addSubItem(3, NUM, LocaleController.getString("DeleteTheme", NUM));
            this.menuItem.setMenuYOffset(-AndroidUtilities.dp(80.0f));
            this.menuItem.setSubMenuOpenSide(2);
            this.menuItem.setDelegate(new -$$Lambda$ColorPicker$PqbXq2JQ2YGuLQ-o-4YhYmxXEh8(this));
            this.menuItem.setAdditionalYOffset(AndroidUtilities.dp(72.0f));
            this.menuItem.setTranslationX((float) AndroidUtilities.dp(6.0f));
            this.menuItem.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(str2), 1));
            addView(this.menuItem, LayoutHelper.createFrame(48, 48.0f, 53, 0.0f, -3.0f, 7.0f, 0.0f));
            this.menuItem.setOnClickListener(new -$$Lambda$ColorPicker$NC6au_gVaoUSC9lyFYAiJyuegBQ(this));
        }
    }

    public /* synthetic */ void lambda$new$0$ColorPicker(int i, View view, boolean z) {
        if (this.colorEditText[3] != null) {
            int i2 = 1;
            if (i == 1) {
                i2 = 0;
            }
            this.selectedEditText = i2;
            setColorInner(getFieldColor(i, -1));
        }
    }

    static /* synthetic */ boolean lambda$new$1(TextView textView, int i, KeyEvent keyEvent) {
        if (i != 6) {
            return false;
        }
        AndroidUtilities.hideKeyboard(textView);
        return true;
    }

    public /* synthetic */ void lambda$new$2$ColorPicker(View view) {
        if (this.exchangeButton.getAlpha() == 1.0f) {
            if (this.myMessagesColor) {
                String obj = this.colorEditText[1].getText().toString();
                String obj2 = this.colorEditText[3].getText().toString();
                this.colorEditText[1].setText(obj2);
                this.colorEditText[1].setSelection(obj2.length());
                this.colorEditText[3].setText(obj);
                this.colorEditText[3].setSelection(obj.length());
            } else {
                this.delegate.rotateColors();
                this.exchangeButton.animate().rotation(this.exchangeButton.getRotation() + 45.0f).setDuration(180).setInterpolator(CubicBezierInterpolator.EASE_OUT).start();
            }
        }
    }

    public /* synthetic */ void lambda$new$3$ColorPicker(View view) {
        EditTextBoldCursor[] editTextBoldCursorArr;
        int color;
        int i = 0;
        Object obj = this.clearButton.getTag() != null ? 1 : null;
        String str = "%02x%02x%02x";
        if (this.myMessagesColor && obj != null) {
            this.colorEditText[1].setText(String.format(str, new Object[]{Byte.valueOf((byte) Color.red(this.originalFirstColor)), Byte.valueOf((byte) Color.green(this.originalFirstColor)), Byte.valueOf((byte) Color.blue(this.originalFirstColor))}).toUpperCase());
            editTextBoldCursorArr = this.colorEditText;
            editTextBoldCursorArr[1].setSelection(editTextBoldCursorArr[1].length());
        }
        toggleSecondField();
        if (this.myMessagesColor && obj == null) {
            this.originalFirstColor = getFieldColor(1, -1);
            color = Theme.getColor("chat_outBubble");
            this.colorEditText[1].setText(String.format(str, new Object[]{Byte.valueOf((byte) Color.red(color)), Byte.valueOf((byte) Color.green(color)), Byte.valueOf((byte) Color.blue(color))}).toUpperCase());
            editTextBoldCursorArr = this.colorEditText;
            editTextBoldCursorArr[1].setSelection(editTextBoldCursorArr[1].length());
        }
        color = getFieldColor(3, -16777216);
        if (obj == null) {
            color = generateGradientColors(getFieldColor(1, 0));
            String toUpperCase = String.format(str, new Object[]{Byte.valueOf((byte) Color.red(color)), Byte.valueOf((byte) Color.green(color)), Byte.valueOf((byte) Color.blue(color))}).toUpperCase();
            this.colorEditText[3].setText(toUpperCase);
            this.colorEditText[3].setSelection(toUpperCase.length());
        }
        ColorPickerDelegate colorPickerDelegate = this.delegate;
        if (obj == null) {
            i = color;
        }
        colorPickerDelegate.setColor(i, 1, true);
        if (obj == null) {
            this.colorEditText[3].requestFocus();
        } else if (this.colorEditText[3].isFocused()) {
            this.colorEditText[1].requestFocus();
        }
    }

    public /* synthetic */ void lambda$new$4$ColorPicker(View view) {
        if (this.resetButton.getAlpha() == 1.0f) {
            this.delegate.setColor(0, -1, true);
            this.resetButton.animate().alpha(0.0f).setDuration(180).start();
            this.resetButton.setTag(null);
        }
    }

    public /* synthetic */ void lambda$new$5$ColorPicker(int i) {
        boolean z = true;
        if (i == 1 || i == 2) {
            ColorPickerDelegate colorPickerDelegate = this.delegate;
            if (i != 2) {
                z = false;
            }
            colorPickerDelegate.openThemeCreate(z);
        } else if (i == 3) {
            this.delegate.deleteTheme();
        }
    }

    public /* synthetic */ void lambda$new$6$ColorPicker(View view) {
        this.menuItem.toggleSubMenu();
    }

    public void hideKeyboard() {
        AndroidUtilities.hideKeyboard(this.colorEditText[this.selectedEditText == 0 ? 1 : 3]);
    }

    private void toggleSecondField() {
        final boolean z = this.clearButton.getTag() != null;
        this.clearButton.setTag(z ? null : Integer.valueOf(1));
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList arrayList = new ArrayList();
        ImageView imageView = this.clearButton;
        Property property = View.ROTATION;
        float[] fArr = new float[1];
        fArr[0] = z ? 45.0f : 0.0f;
        arrayList.add(ObjectAnimator.ofFloat(imageView, property, fArr));
        Object obj = this.colorEditText[2];
        Property property2 = View.ALPHA;
        float[] fArr2 = new float[1];
        fArr2[0] = z ? 0.0f : 1.0f;
        arrayList.add(ObjectAnimator.ofFloat(obj, property2, fArr2));
        obj = this.colorEditText[3];
        Property property3 = View.ALPHA;
        float[] fArr3 = new float[1];
        fArr3[0] = z ? 0.0f : 1.0f;
        arrayList.add(ObjectAnimator.ofFloat(obj, property3, fArr3));
        imageView = this.exchangeButton;
        property3 = View.ALPHA;
        fArr3 = new float[1];
        fArr3[0] = z ? 0.0f : 1.0f;
        arrayList.add(ObjectAnimator.ofFloat(imageView, property3, fArr3));
        if (this.currentResetType == 2 && !z) {
            arrayList.add(ObjectAnimator.ofFloat(this.resetButton, View.ALPHA, new float[]{0.0f}));
        }
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (ColorPicker.this.currentResetType == 2 && !z) {
                    ColorPicker.this.resetButton.setVisibility(8);
                    ColorPicker.this.resetButton.setTag(null);
                }
            }
        });
        animatorSet.playTogether(arrayList);
        animatorSet.setDuration(180);
        animatorSet.start();
        if (z && !this.ignoreTextChange) {
            if (this.minBrightness > 0.0f || this.maxBrightness < 1.0f) {
                setColorInner(getFieldColor(1, -1));
                int color = getColor();
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                this.ignoreTextChange = true;
                String toUpperCase = String.format("%02x%02x%02x", new Object[]{Byte.valueOf((byte) red), Byte.valueOf((byte) green), Byte.valueOf((byte) blue)}).toUpperCase();
                this.colorEditText[1].setText(toUpperCase);
                this.colorEditText[1].setSelection(toUpperCase.length());
                this.ignoreTextChange = false;
                this.delegate.setColor(color, 0, true);
                invalidate();
            }
        }
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x00c0  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x00b3  */
    public void onDraw(android.graphics.Canvas r23) {
        /*
        r22 = this;
        r6 = r22;
        r7 = r23;
        r0 = r6.colorWheelBitmap;
        r8 = NUM; // 0x42580000 float:54.0 double:5.499263994E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r1 = (float) r1;
        r2 = 0;
        r3 = 0;
        r7.drawBitmap(r0, r2, r1, r3);
        r0 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r1 = r6.colorWheelBitmap;
        r1 = r1.getHeight();
        r9 = r0 + r1;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r2 = (float) r0;
        r0 = r22.getMeasuredWidth();
        r3 = (float) r0;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r10 = 1;
        r0 = r0 + r10;
        r4 = (float) r0;
        r5 = r6.linePaint;
        r1 = 0;
        r0 = r23;
        r0.drawRect(r1, r2, r3, r4, r5);
        r0 = r9 + -1;
        r2 = (float) r0;
        r0 = r22.getMeasuredWidth();
        r3 = (float) r0;
        r4 = (float) r9;
        r5 = r6.linePaint;
        r0 = r23;
        r0.drawRect(r1, r2, r3, r4, r5);
        r0 = r6.hsvTemp;
        r1 = r6.colorHSV;
        r11 = 0;
        r2 = r1[r11];
        r0[r11] = r2;
        r2 = r1[r10];
        r0[r10] = r2;
        r12 = 2;
        r13 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r0[r12] = r13;
        r0 = r1[r11];
        r1 = r22.getMeasuredWidth();
        r1 = (float) r1;
        r0 = r0 * r1;
        r1 = NUM; // 0x43b40000 float:360.0 double:5.611943214E-315;
        r0 = r0 / r1;
        r0 = (int) r0;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r1 = (float) r1;
        r2 = r6.colorWheelBitmap;
        r2 = r2.getHeight();
        r2 = (float) r2;
        r3 = r6.colorHSV;
        r3 = r3[r10];
        r3 = r13 - r3;
        r2 = r2 * r3;
        r1 = r1 + r2;
        r1 = (int) r1;
        r2 = r6.circlePressed;
        if (r2 != 0) goto L_0x00e1;
    L_0x0080:
        r2 = NUM; // 0x41800000 float:16.0 double:5.42932517E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r3 = org.telegram.ui.Components.CubicBezierInterpolator.EASE_OUT;
        r4 = r6.pressedMoveProgress;
        r3 = r3.getInterpolation(r4);
        if (r0 >= r2) goto L_0x0099;
    L_0x0090:
        r4 = (float) r0;
        r0 = r2 - r0;
        r0 = (float) r0;
        r0 = r0 * r3;
        r4 = r4 + r0;
    L_0x0097:
        r0 = (int) r4;
        goto L_0x00ac;
    L_0x0099:
        r4 = r22.getMeasuredWidth();
        r4 = r4 - r2;
        if (r0 <= r4) goto L_0x00ac;
    L_0x00a0:
        r4 = (float) r0;
        r5 = r22.getMeasuredWidth();
        r5 = r5 - r2;
        r0 = r0 - r5;
        r0 = (float) r0;
        r0 = r0 * r3;
        r4 = r4 - r0;
        goto L_0x0097;
    L_0x00ac:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r4 = r4 + r2;
        if (r1 >= r4) goto L_0x00c0;
    L_0x00b3:
        r4 = (float) r1;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r5 = r5 + r2;
        r5 = r5 - r1;
        r1 = (float) r5;
        r3 = r3 * r1;
        r4 = r4 + r3;
    L_0x00be:
        r1 = (int) r4;
        goto L_0x00e1;
    L_0x00c0:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r5 = r6.colorWheelBitmap;
        r5 = r5.getHeight();
        r4 = r4 + r5;
        r4 = r4 - r2;
        if (r1 <= r4) goto L_0x00e1;
    L_0x00ce:
        r4 = (float) r1;
        r5 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r8 = r6.colorWheelBitmap;
        r8 = r8.getHeight();
        r5 = r5 + r8;
        r5 = r5 - r2;
        r1 = r1 - r5;
        r1 = (float) r1;
        r3 = r3 * r1;
        r4 = r4 - r3;
        goto L_0x00be;
    L_0x00e1:
        r2 = r0;
        r3 = r1;
        r0 = r6.hsvTemp;
        r4 = android.graphics.Color.HSVToColor(r0);
        r5 = 0;
        r0 = r22;
        r1 = r23;
        r0.drawPointerArrow(r1, r2, r3, r4, r5);
        r0 = r6.sliderRect;
        r1 = NUM; // 0x41b00000 float:22.0 double:5.44486713E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r2 = (float) r2;
        r3 = NUM; // 0x41d00000 float:26.0 double:5.455228437E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = r3 + r9;
        r3 = (float) r3;
        r4 = r22.getMeasuredWidth();
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r4 = r4 - r1;
        r1 = (float) r4;
        r4 = NUM; // 0x42080000 float:34.0 double:5.473360725E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r9 = r9 + r4;
        r4 = (float) r9;
        r0.set(r2, r3, r1, r4);
        r0 = r6.colorGradient;
        if (r0 != 0) goto L_0x0158;
    L_0x011b:
        r0 = r6.hsvTemp;
        r1 = r6.minHsvBrightness;
        r0[r12] = r1;
        r0 = android.graphics.Color.HSVToColor(r0);
        r1 = r6.hsvTemp;
        r2 = r6.maxHsvBrightness;
        r1[r12] = r2;
        r1 = android.graphics.Color.HSVToColor(r1);
        r2 = new android.graphics.LinearGradient;
        r3 = r6.sliderRect;
        r15 = r3.left;
        r4 = r3.top;
        r3 = r3.right;
        r5 = new int[r12];
        r5[r11] = r1;
        r5[r10] = r0;
        r20 = 0;
        r21 = android.graphics.Shader.TileMode.CLAMP;
        r14 = r2;
        r16 = r4;
        r17 = r3;
        r18 = r4;
        r19 = r5;
        r14.<init>(r15, r16, r17, r18, r19, r20, r21);
        r6.colorGradient = r2;
        r0 = r6.valueSliderPaint;
        r1 = r6.colorGradient;
        r0.setShader(r1);
    L_0x0158:
        r0 = r6.sliderRect;
        r1 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r2 = (float) r2;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r1 = (float) r1;
        r3 = r6.valueSliderPaint;
        r7.drawRoundRect(r0, r2, r1, r3);
        r0 = r6.minHsvBrightness;
        r1 = r6.maxHsvBrightness;
        r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1));
        if (r0 != 0) goto L_0x0176;
    L_0x0173:
        r0 = NUM; // 0x3var_ float:0.5 double:5.222099017E-315;
        goto L_0x0181;
    L_0x0176:
        r0 = r22.getBrightness();
        r1 = r6.minHsvBrightness;
        r0 = r0 - r1;
        r2 = r6.maxHsvBrightness;
        r2 = r2 - r1;
        r0 = r0 / r2;
    L_0x0181:
        r1 = r6.sliderRect;
        r2 = r1.left;
        r0 = r13 - r0;
        r1 = r1.width();
        r0 = r0 * r1;
        r2 = r2 + r0;
        r2 = (int) r2;
        r0 = r6.sliderRect;
        r0 = r0.centerY();
        r3 = (int) r0;
        r4 = r22.getColor();
        r5 = 1;
        r0 = r22;
        r1 = r23;
        r0.drawPointerArrow(r1, r2, r3, r4, r5);
        r0 = r6.circlePressed;
        if (r0 != 0) goto L_0x01ca;
    L_0x01a6:
        r0 = r6.pressedMoveProgress;
        r0 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1));
        if (r0 >= 0) goto L_0x01ca;
    L_0x01ac:
        r0 = android.os.SystemClock.uptimeMillis();
        r2 = r6.lastUpdateTime;
        r2 = r0 - r2;
        r6.lastUpdateTime = r0;
        r0 = r6.pressedMoveProgress;
        r1 = (float) r2;
        r2 = NUM; // 0x43340000 float:180.0 double:5.570497984E-315;
        r1 = r1 / r2;
        r0 = r0 + r1;
        r6.pressedMoveProgress = r0;
        r0 = r6.pressedMoveProgress;
        r0 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1));
        if (r0 <= 0) goto L_0x01c7;
    L_0x01c5:
        r6.pressedMoveProgress = r13;
    L_0x01c7:
        r22.invalidate();
    L_0x01ca:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.ColorPicker.onDraw(android.graphics.Canvas):void");
    }

    private int getFieldColor(int i, int i2) {
        try {
            return Integer.parseInt(this.colorEditText[i].getText().toString(), 16) | -16777216;
        } catch (Exception unused) {
            return i2;
        }
    }

    private void drawPointerArrow(Canvas canvas, int i, int i2, int i3, boolean z) {
        int dp = AndroidUtilities.dp(z ? 12.0f : 16.0f);
        this.circleDrawable.setBounds(i - dp, i2 - dp, i + dp, dp + i2);
        this.circleDrawable.draw(canvas);
        this.circlePaint.setColor(-1);
        float f = (float) i;
        float f2 = (float) i2;
        canvas.drawCircle(f, f2, (float) AndroidUtilities.dp(z ? 11.0f : 15.0f), this.circlePaint);
        this.circlePaint.setColor(i3);
        canvas.drawCircle(f, f2, (float) AndroidUtilities.dp(z ? 9.0f : 13.0f), this.circlePaint);
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        if (this.colorWheelWidth != i) {
            this.colorWheelWidth = i;
            this.colorWheelBitmap = createColorWheelBitmap(this.colorWheelWidth, AndroidUtilities.dp(180.0f));
            this.colorGradient = null;
        }
    }

    private Bitmap createColorWheelBitmap(int i, int i2) {
        int i3 = i;
        int i4 = i2;
        Bitmap createBitmap = Bitmap.createBitmap(i3, i4, Config.ARGB_8888);
        float f = (float) i3;
        float f2 = (float) (i4 / 3);
        float f3 = (float) i4;
        this.colorWheelPaint.setShader(new ComposeShader(new LinearGradient(0.0f, f2, 0.0f, f3, new int[]{-1, 0}, null, TileMode.CLAMP), new LinearGradient(0.0f, 0.0f, f, 0.0f, new int[]{-65536, -256, -16711936, -16711681, -16776961, -65281, -65536}, null, TileMode.CLAMP), Mode.MULTIPLY));
        new Canvas(createBitmap).drawRect(0.0f, 0.0f, f, f3, this.colorWheelPaint);
        return createBitmap;
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x0150  */
    /* JADX WARNING: Missing block: B:3:0x000b, code skipped:
            if (r0 != 2) goto L_0x001b;
     */
    /* JADX WARNING: Missing block: B:34:0x010a, code skipped:
            if (r12 <= (r11.sliderRect.bottom + ((float) org.telegram.messenger.AndroidUtilities.dp(7.0f)))) goto L_0x010c;
     */
    public boolean onTouchEvent(android.view.MotionEvent r12) {
        /*
        r11 = this;
        r0 = r12.getAction();
        r1 = 2;
        r2 = 0;
        r3 = 1;
        if (r0 == 0) goto L_0x0020;
    L_0x0009:
        if (r0 == r3) goto L_0x000e;
    L_0x000b:
        if (r0 == r1) goto L_0x0020;
    L_0x000d:
        goto L_0x001b;
    L_0x000e:
        r11.colorPressed = r2;
        r11.circlePressed = r2;
        r0 = android.os.SystemClock.uptimeMillis();
        r11.lastUpdateTime = r0;
        r11.invalidate();
    L_0x001b:
        r12 = super.onTouchEvent(r12);
        return r12;
    L_0x0020:
        r0 = r12.getX();
        r0 = (int) r0;
        r12 = r12.getY();
        r12 = (int) r12;
        r4 = r11.circlePressed;
        r5 = 0;
        r6 = NUM; // 0x42580000 float:54.0 double:5.499263994E-315;
        r7 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        if (r4 != 0) goto L_0x004a;
    L_0x0033:
        r4 = r11.colorPressed;
        if (r4 != 0) goto L_0x00d8;
    L_0x0037:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r6);
        if (r12 < r4) goto L_0x00d8;
    L_0x003d:
        r4 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r8 = r11.colorWheelBitmap;
        r8 = r8.getHeight();
        r4 = r4 + r8;
        if (r12 > r4) goto L_0x00d8;
    L_0x004a:
        r4 = r11.circlePressed;
        if (r4 != 0) goto L_0x0055;
    L_0x004e:
        r4 = r11.getParent();
        r4.requestDisallowInterceptTouchEvent(r3);
    L_0x0055:
        r11.circlePressed = r3;
        r11.pressedMoveProgress = r5;
        r8 = android.os.SystemClock.uptimeMillis();
        r11.lastUpdateTime = r8;
        r4 = r11.colorWheelBitmap;
        r4 = r4.getWidth();
        r0 = java.lang.Math.min(r0, r4);
        r0 = java.lang.Math.max(r2, r0);
        r4 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r8 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r9 = r11.colorWheelBitmap;
        r9 = r9.getHeight();
        r8 = r8 + r9;
        r12 = java.lang.Math.min(r12, r8);
        r12 = java.lang.Math.max(r4, r12);
        r4 = r11.minHsvBrightness;
        r8 = r11.maxHsvBrightness;
        r4 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1));
        if (r4 != 0) goto L_0x008f;
    L_0x008c:
        r4 = NUM; // 0x3var_ float:0.5 double:5.222099017E-315;
        goto L_0x009a;
    L_0x008f:
        r4 = r11.getBrightness();
        r8 = r11.minHsvBrightness;
        r4 = r4 - r8;
        r9 = r11.maxHsvBrightness;
        r9 = r9 - r8;
        r4 = r4 / r9;
    L_0x009a:
        r8 = r11.colorHSV;
        r9 = (float) r0;
        r10 = NUM; // 0x43b40000 float:360.0 double:5.611943214E-315;
        r9 = r9 * r10;
        r10 = r11.colorWheelBitmap;
        r10 = r10.getWidth();
        r10 = (float) r10;
        r9 = r9 / r10;
        r8[r2] = r9;
        r8 = r11.colorHSV;
        r9 = r11.colorWheelBitmap;
        r9 = r9.getHeight();
        r9 = (float) r9;
        r9 = r7 / r9;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = r12 - r6;
        r6 = (float) r6;
        r9 = r9 * r6;
        r6 = r7 - r9;
        r8[r3] = r6;
        r11.updateHsvMinMaxBrightness();
        r6 = r11.colorHSV;
        r8 = r11.minHsvBrightness;
        r9 = r7 - r4;
        r8 = r8 * r9;
        r9 = r11.maxHsvBrightness;
        r9 = r9 * r4;
        r8 = r8 + r9;
        r6[r1] = r8;
        r4 = 0;
        r11.colorGradient = r4;
    L_0x00d8:
        r4 = r11.colorPressed;
        if (r4 != 0) goto L_0x010c;
    L_0x00dc:
        r4 = r11.circlePressed;
        if (r4 != 0) goto L_0x0140;
    L_0x00e0:
        r4 = (float) r0;
        r6 = r11.sliderRect;
        r8 = r6.left;
        r8 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1));
        if (r8 < 0) goto L_0x0140;
    L_0x00e9:
        r8 = r6.right;
        r4 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1));
        if (r4 > 0) goto L_0x0140;
    L_0x00ef:
        r12 = (float) r12;
        r4 = r6.top;
        r6 = NUM; // 0x40e00000 float:7.0 double:5.37751863E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r8 = (float) r8;
        r4 = r4 - r8;
        r4 = (r12 > r4 ? 1 : (r12 == r4 ? 0 : -1));
        if (r4 < 0) goto L_0x0140;
    L_0x00fe:
        r4 = r11.sliderRect;
        r4 = r4.bottom;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = (float) r6;
        r4 = r4 + r6;
        r12 = (r12 > r4 ? 1 : (r12 == r4 ? 0 : -1));
        if (r12 > 0) goto L_0x0140;
    L_0x010c:
        r12 = (float) r0;
        r0 = r11.sliderRect;
        r4 = r0.left;
        r12 = r12 - r4;
        r0 = r0.width();
        r12 = r12 / r0;
        r12 = r7 - r12;
        r0 = (r12 > r5 ? 1 : (r12 == r5 ? 0 : -1));
        if (r0 >= 0) goto L_0x011f;
    L_0x011d:
        r12 = 0;
        goto L_0x0125;
    L_0x011f:
        r0 = (r12 > r7 ? 1 : (r12 == r7 ? 0 : -1));
        if (r0 <= 0) goto L_0x0125;
    L_0x0123:
        r12 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
    L_0x0125:
        r0 = r11.colorHSV;
        r4 = r11.minHsvBrightness;
        r7 = r7 - r12;
        r4 = r4 * r7;
        r5 = r11.maxHsvBrightness;
        r5 = r5 * r12;
        r4 = r4 + r5;
        r0[r1] = r4;
        r12 = r11.colorPressed;
        if (r12 != 0) goto L_0x013e;
    L_0x0137:
        r12 = r11.getParent();
        r12.requestDisallowInterceptTouchEvent(r3);
    L_0x013e:
        r11.colorPressed = r3;
    L_0x0140:
        r12 = r11.colorPressed;
        if (r12 != 0) goto L_0x0148;
    L_0x0144:
        r12 = r11.circlePressed;
        if (r12 == 0) goto L_0x01a0;
    L_0x0148:
        r12 = r11.getColor();
        r0 = r11.ignoreTextChange;
        if (r0 != 0) goto L_0x0196;
    L_0x0150:
        r0 = android.graphics.Color.red(r12);
        r4 = android.graphics.Color.green(r12);
        r5 = android.graphics.Color.blue(r12);
        r11.ignoreTextChange = r3;
        r6 = 3;
        r7 = new java.lang.Object[r6];
        r0 = (byte) r0;
        r0 = java.lang.Byte.valueOf(r0);
        r7[r2] = r0;
        r0 = (byte) r4;
        r0 = java.lang.Byte.valueOf(r0);
        r7[r3] = r0;
        r0 = (byte) r5;
        r0 = java.lang.Byte.valueOf(r0);
        r7[r1] = r0;
        r0 = "%02x%02x%02x";
        r0 = java.lang.String.format(r0, r7);
        r0 = r0.toUpperCase();
        r1 = r11.colorEditText;
        r4 = r11.selectedEditText;
        if (r4 != 0) goto L_0x0187;
    L_0x0186:
        r6 = 1;
    L_0x0187:
        r1 = r1[r6];
        r1 = r1.getText();
        r4 = r1.length();
        r1.replace(r2, r4, r0);
        r11.ignoreTextChange = r2;
    L_0x0196:
        r0 = r11.delegate;
        r1 = r11.selectedEditText;
        r0.setColor(r12, r1, r2);
        r11.invalidate();
    L_0x01a0:
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.ColorPicker.onTouchEvent(android.view.MotionEvent):boolean");
    }

    private void setColorInner(int i) {
        Color.colorToHSV(i, this.colorHSV);
        int defaultColor = this.delegate.getDefaultColor(this.selectedEditText);
        if (defaultColor == 0 || defaultColor != i) {
            updateHsvMinMaxBrightness();
        }
        this.colorGradient = null;
        invalidate();
    }

    public void setColor(int i, int i2) {
        if (!this.ignoreTextChange) {
            this.ignoreTextChange = true;
            int i3 = 3;
            String toUpperCase = String.format("%02x%02x%02x", new Object[]{Byte.valueOf((byte) Color.red(i)), Byte.valueOf((byte) Color.green(i)), Byte.valueOf((byte) Color.blue(i))}).toUpperCase();
            this.colorEditText[i2 == 0 ? 1 : 3].setText(toUpperCase);
            EditTextBoldCursor[] editTextBoldCursorArr = this.colorEditText;
            if (i2 == 0) {
                i3 = 1;
            }
            editTextBoldCursorArr[i3].setSelection(toUpperCase.length());
            this.ignoreTextChange = false;
        }
        setColorInner(i);
        if (i2 == 1 && i != 0 && this.clearButton.getTag() == null) {
            toggleSecondField();
        }
    }

    public void setHasChanges(final boolean z) {
        if ((!z || this.resetButton.getTag() == null) && ((z || this.resetButton.getTag() != null) && this.clearButton.getTag() == null)) {
            this.resetButton.setTag(z ? Integer.valueOf(1) : null);
            AnimatorSet animatorSet = new AnimatorSet();
            ArrayList arrayList = new ArrayList();
            if (z) {
                this.resetButton.setVisibility(0);
            }
            TextView textView = this.resetButton;
            Property property = View.ALPHA;
            float[] fArr = new float[1];
            fArr[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(textView, property, fArr));
            animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    if (!z) {
                        ColorPicker.this.resetButton.setVisibility(8);
                    }
                }
            });
            animatorSet.playTogether(arrayList);
            animatorSet.setDuration(180);
            animatorSet.start();
        }
    }

    public void setType(int i, boolean z, boolean z2, boolean z3, boolean z4, int i2, boolean z5) {
        float f;
        ImageView imageView;
        Property property;
        float[] fArr;
        String str;
        int i3 = i;
        final boolean z6 = z;
        final boolean z7 = z2;
        final boolean z8 = z3;
        this.currentResetType = i3;
        this.myMessagesColor = z4;
        if (this.myMessagesColor) {
            this.exchangeButton.setImageResource(NUM);
            this.exchangeButton.setRotation(0.0f);
        } else {
            this.exchangeButton.setImageResource(NUM);
            this.exchangeButton.setRotation((float) (i2 - 45));
        }
        ActionBarMenuItem actionBarMenuItem = this.menuItem;
        int i4 = 0;
        if (actionBarMenuItem != null) {
            if (i3 == 1) {
                actionBarMenuItem.setVisibility(0);
            } else {
                actionBarMenuItem.setVisibility(8);
            }
        }
        Object obj = null;
        ArrayList arrayList = z5 ? new ArrayList() : null;
        if (!(z7 && z8)) {
            this.colorEditText[1].requestFocus();
        }
        int i5 = 2;
        while (true) {
            f = 1.0f;
            if (i5 >= 4) {
                break;
            }
            if (z5) {
                if (z7) {
                    this.colorEditText[i5].setVisibility(0);
                }
                Object obj2 = this.colorEditText[i5];
                Property property2 = View.ALPHA;
                float[] fArr2 = new float[1];
                if (!(z7 && z8)) {
                    f = 0.0f;
                }
                fArr2[0] = f;
                arrayList.add(ObjectAnimator.ofFloat(obj2, property2, fArr2));
            } else {
                this.colorEditText[i5].setVisibility(z7 ? 0 : 8);
                EditText editText = this.colorEditText[i5];
                if (!(z7 && z8)) {
                    f = 0.0f;
                }
                editText.setAlpha(f);
            }
            this.colorEditText[i5].setTag(z7 ? Integer.valueOf(1) : null);
            i5++;
        }
        if (z5) {
            if (z7) {
                this.exchangeButton.setVisibility(0);
            }
            imageView = this.exchangeButton;
            property = View.ALPHA;
            fArr = new float[1];
            float f2 = (z7 && z8) ? 1.0f : 0.0f;
            fArr[0] = f2;
            arrayList.add(ObjectAnimator.ofFloat(imageView, property, fArr));
        } else {
            this.exchangeButton.setVisibility(z7 ? 0 : 8);
            imageView = this.exchangeButton;
            float f3 = (z7 && z8) ? 1.0f : 0.0f;
            imageView.setAlpha(f3);
        }
        if (z7) {
            this.clearButton.setTag(z8 ? Integer.valueOf(1) : null);
            this.clearButton.setRotation(z8 ? 0.0f : 45.0f);
        }
        if (z5) {
            if (z7) {
                this.clearButton.setVisibility(0);
            }
            imageView = this.clearButton;
            property = View.ALPHA;
            fArr = new float[1];
            fArr[0] = z7 ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(imageView, property, fArr));
        } else {
            this.clearButton.setVisibility(z7 ? 0 : 8);
            this.clearButton.setAlpha(z7 ? 1.0f : 0.0f);
        }
        TextView textView = this.resetButton;
        if (z6) {
            obj = Integer.valueOf(1);
        }
        textView.setTag(obj);
        TextView textView2 = this.resetButton;
        if (i3 == 1) {
            i5 = NUM;
            str = "ColorPickerResetAll";
        } else {
            i5 = NUM;
            str = "ColorPickerReset";
        }
        textView2.setText(LocaleController.getString(str, i5));
        ((LayoutParams) this.resetButton.getLayoutParams()).rightMargin = AndroidUtilities.dp(i3 == 1 ? 14.0f : 61.0f);
        if (!z5) {
            TextView textView3 = this.resetButton;
            if (!z6 || z8) {
                f = 0.0f;
            }
            textView3.setAlpha(f);
            textView3 = this.resetButton;
            if (!z6 || z8) {
                i4 = 8;
            }
            textView3.setVisibility(i4);
        } else if (!z6 || (this.resetButton.getVisibility() == 0 && z8)) {
            arrayList.add(ObjectAnimator.ofFloat(this.resetButton, View.ALPHA, new float[]{0.0f}));
        } else if (!(this.resetButton.getVisibility() == 0 || z8)) {
            this.resetButton.setVisibility(0);
            arrayList.add(ObjectAnimator.ofFloat(this.resetButton, View.ALPHA, new float[]{1.0f}));
        }
        if (arrayList != null && !arrayList.isEmpty()) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(arrayList);
            animatorSet.setDuration(180);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    if (!z6 || z8) {
                        ColorPicker.this.resetButton.setVisibility(8);
                    }
                    if (!z7) {
                        ColorPicker.this.clearButton.setVisibility(8);
                        ColorPicker.this.exchangeButton.setVisibility(8);
                        for (int i = 2; i < 4; i++) {
                            ColorPicker.this.colorEditText[i].setVisibility(8);
                        }
                    }
                }
            });
            animatorSet.start();
        }
    }

    public int getColor() {
        float[] fArr = this.hsvTemp;
        float[] fArr2 = this.colorHSV;
        fArr[0] = fArr2[0];
        fArr[1] = fArr2[1];
        fArr[2] = getBrightness();
        return (Color.HSVToColor(this.hsvTemp) & 16777215) | -16777216;
    }

    private float getBrightness() {
        return Math.max(this.minHsvBrightness, Math.min(this.colorHSV[2], this.maxHsvBrightness));
    }

    private void updateHsvMinMaxBrightness() {
        float f = this.clearButton.getTag() != null ? 0.0f : this.minBrightness;
        float f2 = this.clearButton.getTag() != null ? 1.0f : this.maxBrightness;
        float f3 = this.colorHSV[2];
        if (f == 0.0f && f2 == 1.0f) {
            this.minHsvBrightness = 0.0f;
            this.maxHsvBrightness = 1.0f;
            return;
        }
        float[] fArr = this.colorHSV;
        fArr[2] = 1.0f;
        int HSVToColor = Color.HSVToColor(fArr);
        this.colorHSV[2] = f3;
        f3 = AndroidUtilities.computePerceivedBrightness(HSVToColor);
        this.minHsvBrightness = Math.max(0.0f, Math.min(f / f3, 1.0f));
        this.maxHsvBrightness = Math.max(this.minHsvBrightness, Math.min(f2 / f3, 1.0f));
    }

    public void setMinBrightness(float f) {
        this.minBrightness = f;
        updateHsvMinMaxBrightness();
    }

    public void setMaxBrightness(float f) {
        this.maxBrightness = f;
        updateHsvMinMaxBrightness();
    }

    public void provideThemeDescriptions(List<ThemeDescription> list) {
        List<ThemeDescription> list2 = list;
        int i = 0;
        while (true) {
            EditTextBoldCursor[] editTextBoldCursorArr = this.colorEditText;
            if (i >= editTextBoldCursorArr.length) {
                break;
            }
            list2.add(new ThemeDescription(editTextBoldCursorArr[i], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"));
            list2.add(new ThemeDescription(this.colorEditText[i], ThemeDescription.FLAG_CURSORCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"));
            list2.add(new ThemeDescription(this.colorEditText[i], ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"));
            list2.add(new ThemeDescription(this.colorEditText[i], ThemeDescription.FLAG_HINTTEXTCOLOR | ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "windowBackgroundWhiteBlueHeader"));
            list2.add(new ThemeDescription(this.colorEditText[i], ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"));
            list2.add(new ThemeDescription(this.colorEditText[i], ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"));
            i++;
        }
        list2.add(new ThemeDescription(this.clearButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"));
        list2.add(new ThemeDescription(this.clearButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "dialogButtonSelector"));
        list2.add(new ThemeDescription(this.exchangeButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"));
        list2.add(new ThemeDescription(this.exchangeButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "dialogButtonSelector"));
        ActionBarMenuItem actionBarMenuItem = this.menuItem;
        if (actionBarMenuItem != null) {
            -$$Lambda$ColorPicker$eK23zZ0tDQoHxAcV4d7wl-QCc-o -__lambda_colorpicker_ek23zz0tdqohxacv4d7wl-qcc-o = new -$$Lambda$ColorPicker$eK23zZ0tDQoHxAcV4d7wl-QCc-o(this);
            list2.add(new ThemeDescription(actionBarMenuItem, 0, null, null, null, -__lambda_colorpicker_ek23zz0tdqohxacv4d7wl-qcc-o, "windowBackgroundWhiteBlackText"));
            -$$Lambda$ColorPicker$eK23zZ0tDQoHxAcV4d7wl-QCc-o -__lambda_colorpicker_ek23zz0tdqohxacv4d7wl-qcc-o2 = -__lambda_colorpicker_ek23zz0tdqohxacv4d7wl-qcc-o;
            list2.add(new ThemeDescription(this.menuItem, 0, null, null, null, -__lambda_colorpicker_ek23zz0tdqohxacv4d7wl-qcc-o2, "dialogButtonSelector"));
            list2.add(new ThemeDescription(this.menuItem, 0, null, null, null, -__lambda_colorpicker_ek23zz0tdqohxacv4d7wl-qcc-o2, "actionBarDefaultSubmenuItem"));
            list2.add(new ThemeDescription(this.menuItem, 0, null, null, null, -__lambda_colorpicker_ek23zz0tdqohxacv4d7wl-qcc-o2, "actionBarDefaultSubmenuItemIcon"));
            list2.add(new ThemeDescription(this.menuItem, 0, null, null, null, -__lambda_colorpicker_ek23zz0tdqohxacv4d7wl-qcc-o2, "actionBarDefaultSubmenuBackground"));
        }
    }

    public /* synthetic */ void lambda$provideThemeDescriptions$7$ColorPicker() {
        this.menuItem.setIconColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        Theme.setDrawableColor(this.menuItem.getBackground(), Theme.getColor("dialogButtonSelector"));
        this.menuItem.setPopupItemsColor(Theme.getColor("actionBarDefaultSubmenuItem"), false);
        this.menuItem.setPopupItemsColor(Theme.getColor("actionBarDefaultSubmenuItemIcon"), true);
        this.menuItem.redrawPopup(Theme.getColor("actionBarDefaultSubmenuBackground"));
    }

    private int generateGradientColors(int i) {
        float[] fArr = new float[3];
        Color.colorToHSV(i, fArr);
        if (fArr[1] > 0.5f) {
            fArr[1] = fArr[1] - 0.15f;
        } else {
            fArr[1] = fArr[1] + 0.15f;
        }
        if (fArr[0] > 180.0f) {
            fArr[0] = fArr[0] - 20.0f;
        } else {
            fArr[0] = fArr[0] + 20.0f;
        }
        return Color.HSVToColor(255, fArr);
    }
}
