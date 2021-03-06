package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$TL_account_changePhone;
import org.telegram.tgnet.TLRPC$TL_account_sendChangePhoneCode;
import org.telegram.tgnet.TLRPC$TL_auth_cancelCode;
import org.telegram.tgnet.TLRPC$TL_auth_codeTypeCall;
import org.telegram.tgnet.TLRPC$TL_auth_codeTypeFlashCall;
import org.telegram.tgnet.TLRPC$TL_auth_codeTypeSms;
import org.telegram.tgnet.TLRPC$TL_auth_resendCode;
import org.telegram.tgnet.TLRPC$TL_auth_sentCode;
import org.telegram.tgnet.TLRPC$TL_auth_sentCodeTypeApp;
import org.telegram.tgnet.TLRPC$TL_auth_sentCodeTypeCall;
import org.telegram.tgnet.TLRPC$TL_auth_sentCodeTypeFlashCall;
import org.telegram.tgnet.TLRPC$TL_auth_sentCodeTypeSms;
import org.telegram.tgnet.TLRPC$TL_codeSettings;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.tgnet.TLRPC$auth_CodeType;
import org.telegram.tgnet.TLRPC$auth_SentCodeType;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ChangePhoneActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.HintEditText;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.SlideView;
import org.telegram.ui.CountrySelectActivity;

public class ChangePhoneActivity extends BaseFragment {
    /* access modifiers changed from: private */
    public boolean checkPermissions = true;
    /* access modifiers changed from: private */
    public int currentViewNum = 0;
    private View doneButton;
    /* access modifiers changed from: private */
    public Dialog permissionsDialog;
    /* access modifiers changed from: private */
    public ArrayList<String> permissionsItems = new ArrayList<>();
    private AlertDialog progressDialog;
    /* access modifiers changed from: private */
    public int scrollHeight;
    /* access modifiers changed from: private */
    public SlideView[] views = new SlideView[5];

    private class ProgressView extends View {
        private Paint paint = new Paint();
        private Paint paint2 = new Paint();
        private float progress;

        public ProgressView(ChangePhoneActivity changePhoneActivity, Context context) {
            super(context);
            this.paint.setColor(Theme.getColor("login_progressInner"));
            this.paint2.setColor(Theme.getColor("login_progressOuter"));
        }

        public void setProgress(float f) {
            this.progress = f;
            invalidate();
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            float measuredWidth = (float) ((int) (((float) getMeasuredWidth()) * this.progress));
            canvas.drawRect(0.0f, 0.0f, measuredWidth, (float) getMeasuredHeight(), this.paint2);
            canvas.drawRect(measuredWidth, 0.0f, (float) getMeasuredWidth(), (float) getMeasuredHeight(), this.paint);
        }
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        int i = 0;
        while (true) {
            SlideView[] slideViewArr = this.views;
            if (i >= slideViewArr.length) {
                break;
            }
            if (slideViewArr[i] != null) {
                slideViewArr[i].onDestroyActivity();
            }
            i++;
        }
        AlertDialog alertDialog = this.progressDialog;
        if (alertDialog != null) {
            try {
                alertDialog.dismiss();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
            this.progressDialog = null;
        }
        AndroidUtilities.removeAdjustResize(getParentActivity(), this.classGuid);
    }

    public View createView(Context context) {
        this.actionBar.setTitle(LocaleController.getString("AppName", NUM));
        this.actionBar.setBackButtonImage(NUM);
        this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            public void onItemClick(int i) {
                if (i == 1) {
                    ChangePhoneActivity.this.views[ChangePhoneActivity.this.currentViewNum].onNextPressed();
                } else if (i == -1) {
                    ChangePhoneActivity.this.finishFragment();
                }
            }
        });
        this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, NUM, AndroidUtilities.dp(56.0f), LocaleController.getString("Done", NUM));
        AnonymousClass2 r0 = new ScrollView(context) {
            public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z) {
                if (ChangePhoneActivity.this.currentViewNum == 1 || ChangePhoneActivity.this.currentViewNum == 2 || ChangePhoneActivity.this.currentViewNum == 4) {
                    rect.bottom += AndroidUtilities.dp(40.0f);
                }
                return super.requestChildRectangleOnScreen(view, rect, z);
            }

            /* access modifiers changed from: protected */
            public void onMeasure(int i, int i2) {
                int unused = ChangePhoneActivity.this.scrollHeight = View.MeasureSpec.getSize(i2) - AndroidUtilities.dp(30.0f);
                super.onMeasure(i, i2);
            }
        };
        r0.setFillViewport(true);
        this.fragmentView = r0;
        FrameLayout frameLayout = new FrameLayout(context);
        r0.addView(frameLayout, LayoutHelper.createScroll(-1, -2, 51));
        this.views[0] = new PhoneView(this, context);
        this.views[1] = new LoginActivitySmsView(this, context, 1);
        this.views[2] = new LoginActivitySmsView(this, context, 2);
        this.views[3] = new LoginActivitySmsView(this, context, 3);
        this.views[4] = new LoginActivitySmsView(this, context, 4);
        int i = 0;
        while (true) {
            SlideView[] slideViewArr = this.views;
            if (i < slideViewArr.length) {
                slideViewArr[i].setVisibility(i == 0 ? 0 : 8);
                frameLayout.addView(this.views[i], LayoutHelper.createFrame(-1, i == 0 ? -2.0f : -1.0f, 51, AndroidUtilities.isTablet() ? 26.0f : 18.0f, 30.0f, AndroidUtilities.isTablet() ? 26.0f : 18.0f, 0.0f));
                i++;
            } else {
                this.actionBar.setTitle(slideViewArr[0].getHeaderName());
                return this.fragmentView;
            }
        }
    }

    public void onResume() {
        super.onResume();
        AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
    }

    public void onRequestPermissionsResultFragment(int i, String[] strArr, int[] iArr) {
        if (i == 6) {
            this.checkPermissions = false;
            int i2 = this.currentViewNum;
            if (i2 == 0) {
                this.views[i2].onNextPressed();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onDialogDismiss(Dialog dialog) {
        if (Build.VERSION.SDK_INT >= 23 && dialog == this.permissionsDialog && !this.permissionsItems.isEmpty()) {
            getParentActivity().requestPermissions((String[]) this.permissionsItems.toArray(new String[0]), 6);
        }
    }

    public boolean onBackPressed() {
        int i = this.currentViewNum;
        int i2 = 0;
        if (i == 0) {
            while (true) {
                SlideView[] slideViewArr = this.views;
                if (i2 >= slideViewArr.length) {
                    return true;
                }
                if (slideViewArr[i2] != null) {
                    slideViewArr[i2].onDestroyActivity();
                }
                i2++;
            }
        } else {
            if (this.views[i].onBackPressed(false)) {
                setPage(0, true, (Bundle) null, true);
            }
            return false;
        }
    }

    public void onTransitionAnimationEnd(boolean z, boolean z2) {
        if (z) {
            this.views[this.currentViewNum].onShow();
        }
    }

    public void needShowProgress() {
        if (getParentActivity() != null && !getParentActivity().isFinishing() && this.progressDialog == null) {
            AlertDialog alertDialog = new AlertDialog(getParentActivity(), 3);
            this.progressDialog = alertDialog;
            alertDialog.setCanCacnel(false);
            this.progressDialog.show();
        }
    }

    public void needHideProgress() {
        AlertDialog alertDialog = this.progressDialog;
        if (alertDialog != null) {
            try {
                alertDialog.dismiss();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
            this.progressDialog = null;
        }
    }

    public void setPage(int i, boolean z, Bundle bundle, boolean z2) {
        if (i == 3) {
            this.doneButton.setVisibility(8);
        } else {
            if (i == 0) {
                this.checkPermissions = true;
            }
            this.doneButton.setVisibility(0);
        }
        SlideView[] slideViewArr = this.views;
        final SlideView slideView = slideViewArr[this.currentViewNum];
        final SlideView slideView2 = slideViewArr[i];
        this.currentViewNum = i;
        slideView2.setParams(bundle, false);
        this.actionBar.setTitle(slideView2.getHeaderName());
        slideView2.onShow();
        int i2 = AndroidUtilities.displaySize.x;
        if (z2) {
            i2 = -i2;
        }
        slideView2.setX((float) i2);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.setDuration(300);
        Animator[] animatorArr = new Animator[2];
        float[] fArr = new float[1];
        fArr[0] = (float) (z2 ? AndroidUtilities.displaySize.x : -AndroidUtilities.displaySize.x);
        animatorArr[0] = ObjectAnimator.ofFloat(slideView, "translationX", fArr);
        animatorArr[1] = ObjectAnimator.ofFloat(slideView2, "translationX", new float[]{0.0f});
        animatorSet.playTogether(animatorArr);
        animatorSet.addListener(new AnimatorListenerAdapter(this) {
            public void onAnimationStart(Animator animator) {
                slideView2.setVisibility(0);
            }

            public void onAnimationEnd(Animator animator) {
                slideView.setVisibility(8);
                slideView.setX(0.0f);
            }
        });
        animatorSet.start();
    }

    /* access modifiers changed from: private */
    public void fillNextCodeParams(Bundle bundle, TLRPC$TL_auth_sentCode tLRPC$TL_auth_sentCode) {
        bundle.putString("phoneHash", tLRPC$TL_auth_sentCode.phone_code_hash);
        TLRPC$auth_CodeType tLRPC$auth_CodeType = tLRPC$TL_auth_sentCode.next_type;
        if (tLRPC$auth_CodeType instanceof TLRPC$TL_auth_codeTypeCall) {
            bundle.putInt("nextType", 4);
        } else if (tLRPC$auth_CodeType instanceof TLRPC$TL_auth_codeTypeFlashCall) {
            bundle.putInt("nextType", 3);
        } else if (tLRPC$auth_CodeType instanceof TLRPC$TL_auth_codeTypeSms) {
            bundle.putInt("nextType", 2);
        }
        if (tLRPC$TL_auth_sentCode.type instanceof TLRPC$TL_auth_sentCodeTypeApp) {
            bundle.putInt("type", 1);
            bundle.putInt("length", tLRPC$TL_auth_sentCode.type.length);
            setPage(1, true, bundle, false);
            return;
        }
        if (tLRPC$TL_auth_sentCode.timeout == 0) {
            tLRPC$TL_auth_sentCode.timeout = 60;
        }
        bundle.putInt("timeout", tLRPC$TL_auth_sentCode.timeout * 1000);
        TLRPC$auth_SentCodeType tLRPC$auth_SentCodeType = tLRPC$TL_auth_sentCode.type;
        if (tLRPC$auth_SentCodeType instanceof TLRPC$TL_auth_sentCodeTypeCall) {
            bundle.putInt("type", 4);
            bundle.putInt("length", tLRPC$TL_auth_sentCode.type.length);
            setPage(4, true, bundle, false);
        } else if (tLRPC$auth_SentCodeType instanceof TLRPC$TL_auth_sentCodeTypeFlashCall) {
            bundle.putInt("type", 3);
            bundle.putString("pattern", tLRPC$TL_auth_sentCode.type.pattern);
            setPage(3, true, bundle, false);
        } else if (tLRPC$auth_SentCodeType instanceof TLRPC$TL_auth_sentCodeTypeSms) {
            bundle.putInt("type", 2);
            bundle.putInt("length", tLRPC$TL_auth_sentCode.type.length);
            setPage(2, true, bundle, false);
        }
    }

    public class PhoneView extends SlideView implements AdapterView.OnItemSelectedListener {
        /* access modifiers changed from: private */
        public EditTextBoldCursor codeField;
        /* access modifiers changed from: private */
        public HashMap<String, String> codesMap = new HashMap<>();
        /* access modifiers changed from: private */
        public ArrayList<String> countriesArray = new ArrayList<>();
        private HashMap<String, String> countriesMap = new HashMap<>();
        /* access modifiers changed from: private */
        public TextView countryButton;
        /* access modifiers changed from: private */
        public int countryState = 0;
        /* access modifiers changed from: private */
        public boolean ignoreOnPhoneChange = false;
        /* access modifiers changed from: private */
        public boolean ignoreOnTextChange = false;
        /* access modifiers changed from: private */
        public boolean ignoreSelection = false;
        private boolean nextPressed = false;
        /* access modifiers changed from: private */
        public HintEditText phoneField;
        /* access modifiers changed from: private */
        public HashMap<String, String> phoneFormatMap = new HashMap<>();
        /* access modifiers changed from: private */
        public TextView textView;
        /* access modifiers changed from: private */
        public TextView textView2;
        final /* synthetic */ ChangePhoneActivity this$0;
        /* access modifiers changed from: private */
        public View view;

        public void onNothingSelected(AdapterView<?> adapterView) {
        }

        /* JADX WARNING: Illegal instructions before constructor call */
        /* JADX WARNING: Removed duplicated region for block: B:40:0x0332  */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x034f  */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x0363  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public PhoneView(org.telegram.ui.ChangePhoneActivity r22, android.content.Context r23) {
            /*
                r21 = this;
                r1 = r21
                r0 = r22
                r2 = r23
                r1.this$0 = r0
                r1.<init>(r2)
                r3 = 0
                r1.countryState = r3
                java.util.ArrayList r4 = new java.util.ArrayList
                r4.<init>()
                r1.countriesArray = r4
                java.util.HashMap r4 = new java.util.HashMap
                r4.<init>()
                r1.countriesMap = r4
                java.util.HashMap r4 = new java.util.HashMap
                r4.<init>()
                r1.codesMap = r4
                java.util.HashMap r4 = new java.util.HashMap
                r4.<init>()
                r1.phoneFormatMap = r4
                r1.ignoreSelection = r3
                r1.ignoreOnTextChange = r3
                r1.ignoreOnPhoneChange = r3
                r1.nextPressed = r3
                r4 = 1
                r1.setOrientation(r4)
                android.widget.TextView r5 = new android.widget.TextView
                r5.<init>(r2)
                r1.countryButton = r5
                r6 = 1099956224(0x41900000, float:18.0)
                r5.setTextSize(r4, r6)
                android.widget.TextView r5 = r1.countryButton
                r7 = 1082130432(0x40800000, float:4.0)
                int r8 = org.telegram.messenger.AndroidUtilities.dp(r7)
                int r9 = org.telegram.messenger.AndroidUtilities.dp(r7)
                int r10 = org.telegram.messenger.AndroidUtilities.dp(r7)
                int r7 = org.telegram.messenger.AndroidUtilities.dp(r7)
                r5.setPadding(r8, r9, r10, r7)
                android.widget.TextView r5 = r1.countryButton
                java.lang.String r7 = "windowBackgroundWhiteBlackText"
                int r8 = org.telegram.ui.ActionBar.Theme.getColor(r7)
                r5.setTextColor(r8)
                android.widget.TextView r5 = r1.countryButton
                r5.setMaxLines(r4)
                android.widget.TextView r5 = r1.countryButton
                r5.setSingleLine(r4)
                android.widget.TextView r5 = r1.countryButton
                android.text.TextUtils$TruncateAt r8 = android.text.TextUtils.TruncateAt.END
                r5.setEllipsize(r8)
                android.widget.TextView r5 = r1.countryButton
                boolean r8 = org.telegram.messenger.LocaleController.isRTL
                r9 = 5
                r10 = 3
                if (r8 == 0) goto L_0x0080
                r8 = 5
                goto L_0x0081
            L_0x0080:
                r8 = 3
            L_0x0081:
                r8 = r8 | r4
                r5.setGravity(r8)
                android.widget.TextView r5 = r1.countryButton
                java.lang.String r8 = "listSelectorSDK21"
                int r8 = org.telegram.ui.ActionBar.Theme.getColor(r8)
                r11 = 7
                android.graphics.drawable.Drawable r8 = org.telegram.ui.ActionBar.Theme.createSelectorDrawable(r8, r11)
                r5.setBackground(r8)
                android.widget.TextView r5 = r1.countryButton
                r11 = -1
                r12 = 36
                r13 = 0
                r14 = 0
                r15 = 0
                r16 = 1096810496(0x41600000, float:14.0)
                android.widget.LinearLayout$LayoutParams r8 = org.telegram.ui.Components.LayoutHelper.createLinear(r11, r12, r13, r14, r15, r16)
                r1.addView(r5, r8)
                android.widget.TextView r5 = r1.countryButton
                org.telegram.ui.-$$Lambda$ChangePhoneActivity$PhoneView$tPa_0Hp-up7c4Fvo5YvSye5zKCs r8 = new org.telegram.ui.-$$Lambda$ChangePhoneActivity$PhoneView$tPa_0Hp-up7c4Fvo5YvSye5zKCs
                r8.<init>()
                r5.setOnClickListener(r8)
                android.view.View r5 = new android.view.View
                r5.<init>(r2)
                r1.view = r5
                r8 = 1094713344(0x41400000, float:12.0)
                int r11 = org.telegram.messenger.AndroidUtilities.dp(r8)
                int r8 = org.telegram.messenger.AndroidUtilities.dp(r8)
                r5.setPadding(r11, r3, r8, r3)
                android.view.View r5 = r1.view
                java.lang.String r8 = "windowBackgroundWhiteGrayLine"
                int r8 = org.telegram.ui.ActionBar.Theme.getColor(r8)
                r5.setBackgroundColor(r8)
                android.view.View r5 = r1.view
                r11 = -1
                r12 = 1
                r13 = 1082130432(0x40800000, float:4.0)
                r14 = -1047789568(0xffffffffCLASSNAMECLASSNAME, float:-17.5)
                r15 = 1082130432(0x40800000, float:4.0)
                r16 = 0
                android.widget.LinearLayout$LayoutParams r8 = org.telegram.ui.Components.LayoutHelper.createLinear(r11, r12, r13, r14, r15, r16)
                r1.addView(r5, r8)
                android.widget.LinearLayout r5 = new android.widget.LinearLayout
                r5.<init>(r2)
                r5.setOrientation(r3)
                r12 = -2
                r13 = 0
                r14 = 1101004800(0x41a00000, float:20.0)
                r15 = 0
                android.widget.LinearLayout$LayoutParams r8 = org.telegram.ui.Components.LayoutHelper.createLinear(r11, r12, r13, r14, r15, r16)
                r1.addView(r5, r8)
                android.widget.TextView r8 = new android.widget.TextView
                r8.<init>(r2)
                r1.textView = r8
                java.lang.String r11 = "+"
                r8.setText(r11)
                android.widget.TextView r8 = r1.textView
                int r11 = org.telegram.ui.ActionBar.Theme.getColor(r7)
                r8.setTextColor(r11)
                android.widget.TextView r8 = r1.textView
                r8.setTextSize(r4, r6)
                android.widget.TextView r8 = r1.textView
                r11 = -2
                android.widget.LinearLayout$LayoutParams r11 = org.telegram.ui.Components.LayoutHelper.createLinear(r11, r11)
                r5.addView(r8, r11)
                org.telegram.ui.Components.EditTextBoldCursor r8 = new org.telegram.ui.Components.EditTextBoldCursor
                r8.<init>(r2)
                r1.codeField = r8
                r8.setInputType(r10)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                int r11 = org.telegram.ui.ActionBar.Theme.getColor(r7)
                r8.setTextColor(r11)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                int r11 = org.telegram.ui.ActionBar.Theme.getColor(r7)
                r8.setCursorColor(r11)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                r11 = 1101004800(0x41a00000, float:20.0)
                int r12 = org.telegram.messenger.AndroidUtilities.dp(r11)
                r8.setCursorSize(r12)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                r12 = 1069547520(0x3fCLASSNAME, float:1.5)
                r8.setCursorWidth(r12)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                android.graphics.drawable.Drawable r13 = org.telegram.ui.ActionBar.Theme.createEditTextDrawable(r2, r3)
                r8.setBackgroundDrawable(r13)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                r13 = 1092616192(0x41200000, float:10.0)
                int r13 = org.telegram.messenger.AndroidUtilities.dp(r13)
                r8.setPadding(r13, r3, r3, r3)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                r8.setTextSize(r4, r6)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                r8.setMaxLines(r4)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                r13 = 19
                r8.setGravity(r13)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                r14 = 268435461(0x10000005, float:2.5243564E-29)
                r8.setImeOptions(r14)
                android.text.InputFilter[] r8 = new android.text.InputFilter[r4]
                android.text.InputFilter$LengthFilter r15 = new android.text.InputFilter$LengthFilter
                r15.<init>(r9)
                r8[r3] = r15
                org.telegram.ui.Components.EditTextBoldCursor r15 = r1.codeField
                r15.setFilters(r8)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                r15 = 55
                r16 = 36
                r17 = -1055916032(0xffffffffCLASSNAME, float:-9.0)
                r18 = 0
                r19 = 1098907648(0x41800000, float:16.0)
                r20 = 0
                android.widget.LinearLayout$LayoutParams r15 = org.telegram.ui.Components.LayoutHelper.createLinear(r15, r16, r17, r18, r19, r20)
                r5.addView(r8, r15)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                org.telegram.ui.ChangePhoneActivity$PhoneView$1 r15 = new org.telegram.ui.ChangePhoneActivity$PhoneView$1
                r15.<init>(r0)
                r8.addTextChangedListener(r15)
                org.telegram.ui.Components.EditTextBoldCursor r8 = r1.codeField
                org.telegram.ui.-$$Lambda$ChangePhoneActivity$PhoneView$GkSsn6U4wRFQppwnWKMoUCWmKC0 r15 = new org.telegram.ui.-$$Lambda$ChangePhoneActivity$PhoneView$GkSsn6U4wRFQppwnWKMoUCWmKC0
                r15.<init>()
                r8.setOnEditorActionListener(r15)
                org.telegram.ui.Components.HintEditText r8 = new org.telegram.ui.Components.HintEditText
                r8.<init>(r2)
                r1.phoneField = r8
                r8.setInputType(r10)
                org.telegram.ui.Components.HintEditText r8 = r1.phoneField
                int r15 = org.telegram.ui.ActionBar.Theme.getColor(r7)
                r8.setTextColor(r15)
                org.telegram.ui.Components.HintEditText r8 = r1.phoneField
                java.lang.String r15 = "windowBackgroundWhiteHintText"
                int r15 = org.telegram.ui.ActionBar.Theme.getColor(r15)
                r8.setHintTextColor(r15)
                org.telegram.ui.Components.HintEditText r8 = r1.phoneField
                android.graphics.drawable.Drawable r15 = org.telegram.ui.ActionBar.Theme.createEditTextDrawable(r2, r3)
                r8.setBackgroundDrawable(r15)
                org.telegram.ui.Components.HintEditText r8 = r1.phoneField
                r8.setPadding(r3, r3, r3, r3)
                org.telegram.ui.Components.HintEditText r8 = r1.phoneField
                int r7 = org.telegram.ui.ActionBar.Theme.getColor(r7)
                r8.setCursorColor(r7)
                org.telegram.ui.Components.HintEditText r7 = r1.phoneField
                int r8 = org.telegram.messenger.AndroidUtilities.dp(r11)
                r7.setCursorSize(r8)
                org.telegram.ui.Components.HintEditText r7 = r1.phoneField
                r7.setCursorWidth(r12)
                org.telegram.ui.Components.HintEditText r7 = r1.phoneField
                r7.setTextSize(r4, r6)
                org.telegram.ui.Components.HintEditText r6 = r1.phoneField
                r6.setMaxLines(r4)
                org.telegram.ui.Components.HintEditText r6 = r1.phoneField
                r6.setGravity(r13)
                org.telegram.ui.Components.HintEditText r6 = r1.phoneField
                r6.setImeOptions(r14)
                org.telegram.ui.Components.HintEditText r6 = r1.phoneField
                r7 = 1108344832(0x42100000, float:36.0)
                r8 = -1
                android.widget.FrameLayout$LayoutParams r7 = org.telegram.ui.Components.LayoutHelper.createFrame(r8, r7)
                r5.addView(r6, r7)
                org.telegram.ui.Components.HintEditText r5 = r1.phoneField
                org.telegram.ui.ChangePhoneActivity$PhoneView$2 r6 = new org.telegram.ui.ChangePhoneActivity$PhoneView$2
                r6.<init>(r0)
                r5.addTextChangedListener(r6)
                org.telegram.ui.Components.HintEditText r0 = r1.phoneField
                org.telegram.ui.-$$Lambda$ChangePhoneActivity$PhoneView$5fAAHlp_pTj5L3h-M_cgmrBvlfU r5 = new org.telegram.ui.-$$Lambda$ChangePhoneActivity$PhoneView$5fAAHlp_pTj5L3h-M_cgmrBvlfU
                r5.<init>()
                r0.setOnEditorActionListener(r5)
                org.telegram.ui.Components.HintEditText r0 = r1.phoneField
                org.telegram.ui.-$$Lambda$ChangePhoneActivity$PhoneView$1Pi93_oQQ2yizk_1BFx3kQTh6Ho r5 = new org.telegram.ui.-$$Lambda$ChangePhoneActivity$PhoneView$1Pi93_oQQ2yizk_1BFx3kQTh6Ho
                r5.<init>()
                r0.setOnKeyListener(r5)
                android.widget.TextView r0 = new android.widget.TextView
                r0.<init>(r2)
                r1.textView2 = r0
                r2 = 2131624660(0x7f0e02d4, float:1.8876506E38)
                java.lang.String r5 = "ChangePhoneHelp"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r5, r2)
                r0.setText(r2)
                android.widget.TextView r0 = r1.textView2
                java.lang.String r2 = "windowBackgroundWhiteGrayText6"
                int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
                r0.setTextColor(r2)
                android.widget.TextView r0 = r1.textView2
                r2 = 1096810496(0x41600000, float:14.0)
                r0.setTextSize(r4, r2)
                android.widget.TextView r0 = r1.textView2
                boolean r2 = org.telegram.messenger.LocaleController.isRTL
                if (r2 == 0) goto L_0x025d
                r2 = 5
                goto L_0x025e
            L_0x025d:
                r2 = 3
            L_0x025e:
                r0.setGravity(r2)
                android.widget.TextView r0 = r1.textView2
                r2 = 1073741824(0x40000000, float:2.0)
                int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
                float r2 = (float) r2
                r5 = 1065353216(0x3var_, float:1.0)
                r0.setLineSpacing(r2, r5)
                android.widget.TextView r0 = r1.textView2
                r11 = -2
                r12 = -2
                boolean r2 = org.telegram.messenger.LocaleController.isRTL
                if (r2 == 0) goto L_0x0279
                r13 = 5
                goto L_0x027a
            L_0x0279:
                r13 = 3
            L_0x027a:
                r14 = 0
                r15 = 28
                r16 = 0
                r17 = 10
                android.widget.LinearLayout$LayoutParams r2 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r11, (int) r12, (int) r13, (int) r14, (int) r15, (int) r16, (int) r17)
                r1.addView(r0, r2)
                java.util.HashMap r2 = new java.util.HashMap
                r2.<init>()
                java.io.BufferedReader r0 = new java.io.BufferedReader     // Catch:{ Exception -> 0x02e3 }
                java.io.InputStreamReader r5 = new java.io.InputStreamReader     // Catch:{ Exception -> 0x02e3 }
                android.content.res.Resources r6 = r21.getResources()     // Catch:{ Exception -> 0x02e3 }
                android.content.res.AssetManager r6 = r6.getAssets()     // Catch:{ Exception -> 0x02e3 }
                java.lang.String r7 = "countries.txt"
                java.io.InputStream r6 = r6.open(r7)     // Catch:{ Exception -> 0x02e3 }
                r5.<init>(r6)     // Catch:{ Exception -> 0x02e3 }
                r0.<init>(r5)     // Catch:{ Exception -> 0x02e3 }
            L_0x02a5:
                java.lang.String r5 = r0.readLine()     // Catch:{ Exception -> 0x02e3 }
                if (r5 == 0) goto L_0x02df
                java.lang.String r6 = ";"
                java.lang.String[] r5 = r5.split(r6)     // Catch:{ Exception -> 0x02e3 }
                java.util.ArrayList<java.lang.String> r6 = r1.countriesArray     // Catch:{ Exception -> 0x02e3 }
                r7 = 2
                r9 = r5[r7]     // Catch:{ Exception -> 0x02e3 }
                r6.add(r3, r9)     // Catch:{ Exception -> 0x02e3 }
                java.util.HashMap<java.lang.String, java.lang.String> r6 = r1.countriesMap     // Catch:{ Exception -> 0x02e3 }
                r9 = r5[r7]     // Catch:{ Exception -> 0x02e3 }
                r11 = r5[r3]     // Catch:{ Exception -> 0x02e3 }
                r6.put(r9, r11)     // Catch:{ Exception -> 0x02e3 }
                java.util.HashMap<java.lang.String, java.lang.String> r6 = r1.codesMap     // Catch:{ Exception -> 0x02e3 }
                r9 = r5[r3]     // Catch:{ Exception -> 0x02e3 }
                r11 = r5[r7]     // Catch:{ Exception -> 0x02e3 }
                r6.put(r9, r11)     // Catch:{ Exception -> 0x02e3 }
                int r6 = r5.length     // Catch:{ Exception -> 0x02e3 }
                if (r6 <= r10) goto L_0x02d7
                java.util.HashMap<java.lang.String, java.lang.String> r6 = r1.phoneFormatMap     // Catch:{ Exception -> 0x02e3 }
                r9 = r5[r3]     // Catch:{ Exception -> 0x02e3 }
                r11 = r5[r10]     // Catch:{ Exception -> 0x02e3 }
                r6.put(r9, r11)     // Catch:{ Exception -> 0x02e3 }
            L_0x02d7:
                r6 = r5[r4]     // Catch:{ Exception -> 0x02e3 }
                r5 = r5[r7]     // Catch:{ Exception -> 0x02e3 }
                r2.put(r6, r5)     // Catch:{ Exception -> 0x02e3 }
                goto L_0x02a5
            L_0x02df:
                r0.close()     // Catch:{ Exception -> 0x02e3 }
                goto L_0x02e7
            L_0x02e3:
                r0 = move-exception
                org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            L_0x02e7:
                java.util.ArrayList<java.lang.String> r0 = r1.countriesArray
                org.telegram.ui.-$$Lambda$Ds7dtVnGrflEw4-LvNOxA0cDT4Y r5 = org.telegram.ui.$$Lambda$Ds7dtVnGrflEw4LvNOxA0cDT4Y.INSTANCE
                java.util.Collections.sort(r0, r5)
                r5 = 0
                android.content.Context r0 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0304 }
                java.lang.String r6 = "phone"
                java.lang.Object r0 = r0.getSystemService(r6)     // Catch:{ Exception -> 0x0304 }
                android.telephony.TelephonyManager r0 = (android.telephony.TelephonyManager) r0     // Catch:{ Exception -> 0x0304 }
                if (r0 == 0) goto L_0x0308
                java.lang.String r0 = r0.getSimCountryIso()     // Catch:{ Exception -> 0x0304 }
                java.lang.String r0 = r0.toUpperCase()     // Catch:{ Exception -> 0x0304 }
                goto L_0x0309
            L_0x0304:
                r0 = move-exception
                org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            L_0x0308:
                r0 = r5
            L_0x0309:
                if (r0 == 0) goto L_0x032a
                java.lang.Object r0 = r2.get(r0)
                java.lang.String r0 = (java.lang.String) r0
                if (r0 == 0) goto L_0x032a
                java.util.ArrayList<java.lang.String> r2 = r1.countriesArray
                int r2 = r2.indexOf(r0)
                if (r2 == r8) goto L_0x032a
                org.telegram.ui.Components.EditTextBoldCursor r2 = r1.codeField
                java.util.HashMap<java.lang.String, java.lang.String> r6 = r1.countriesMap
                java.lang.Object r0 = r6.get(r0)
                java.lang.CharSequence r0 = (java.lang.CharSequence) r0
                r2.setText(r0)
                r1.countryState = r3
            L_0x032a:
                org.telegram.ui.Components.EditTextBoldCursor r0 = r1.codeField
                int r0 = r0.length()
                if (r0 != 0) goto L_0x0347
                android.widget.TextView r0 = r1.countryButton
                r2 = 2131624862(0x7f0e039e, float:1.8876916E38)
                java.lang.String r3 = "ChooseCountry"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r0.setText(r2)
                org.telegram.ui.Components.HintEditText r0 = r1.phoneField
                r0.setHintText(r5)
                r1.countryState = r4
            L_0x0347:
                org.telegram.ui.Components.EditTextBoldCursor r0 = r1.codeField
                int r0 = r0.length()
                if (r0 == 0) goto L_0x0363
                org.telegram.ui.Components.HintEditText r0 = r1.phoneField
                org.telegram.messenger.AndroidUtilities.showKeyboard(r0)
                org.telegram.ui.Components.HintEditText r0 = r1.phoneField
                r0.requestFocus()
                org.telegram.ui.Components.HintEditText r0 = r1.phoneField
                int r2 = r0.length()
                r0.setSelection(r2)
                goto L_0x036d
            L_0x0363:
                org.telegram.ui.Components.EditTextBoldCursor r0 = r1.codeField
                org.telegram.messenger.AndroidUtilities.showKeyboard(r0)
                org.telegram.ui.Components.EditTextBoldCursor r0 = r1.codeField
                r0.requestFocus()
            L_0x036d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChangePhoneActivity.PhoneView.<init>(org.telegram.ui.ChangePhoneActivity, android.content.Context):void");
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$2 */
        public /* synthetic */ void lambda$new$2$ChangePhoneActivity$PhoneView(View view2) {
            CountrySelectActivity countrySelectActivity = new CountrySelectActivity(true);
            countrySelectActivity.setCountrySelectActivityDelegate(new CountrySelectActivity.CountrySelectActivityDelegate() {
                public final void didSelectCountry(String str, String str2) {
                    ChangePhoneActivity.PhoneView.this.lambda$null$1$ChangePhoneActivity$PhoneView(str, str2);
                }
            });
            this.this$0.presentFragment(countrySelectActivity);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$null$1 */
        public /* synthetic */ void lambda$null$1$ChangePhoneActivity$PhoneView(String str, String str2) {
            selectCountry(str);
            AndroidUtilities.runOnUIThread(new Runnable() {
                public final void run() {
                    ChangePhoneActivity.PhoneView.this.lambda$null$0$ChangePhoneActivity$PhoneView();
                }
            }, 300);
            this.phoneField.requestFocus();
            HintEditText hintEditText = this.phoneField;
            hintEditText.setSelection(hintEditText.length());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$null$0 */
        public /* synthetic */ void lambda$null$0$ChangePhoneActivity$PhoneView() {
            AndroidUtilities.showKeyboard(this.phoneField);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$3 */
        public /* synthetic */ boolean lambda$new$3$ChangePhoneActivity$PhoneView(TextView textView3, int i, KeyEvent keyEvent) {
            if (i != 5) {
                return false;
            }
            this.phoneField.requestFocus();
            HintEditText hintEditText = this.phoneField;
            hintEditText.setSelection(hintEditText.length());
            return true;
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$4 */
        public /* synthetic */ boolean lambda$new$4$ChangePhoneActivity$PhoneView(TextView textView3, int i, KeyEvent keyEvent) {
            if (i != 5) {
                return false;
            }
            onNextPressed();
            return true;
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$5 */
        public /* synthetic */ boolean lambda$new$5$ChangePhoneActivity$PhoneView(View view2, int i, KeyEvent keyEvent) {
            if (i != 67 || this.phoneField.length() != 0) {
                return false;
            }
            this.codeField.requestFocus();
            EditTextBoldCursor editTextBoldCursor = this.codeField;
            editTextBoldCursor.setSelection(editTextBoldCursor.length());
            this.codeField.dispatchKeyEvent(keyEvent);
            return true;
        }

        public void selectCountry(String str) {
            if (this.countriesArray.indexOf(str) != -1) {
                this.ignoreOnTextChange = true;
                String str2 = this.countriesMap.get(str);
                this.codeField.setText(str2);
                this.countryButton.setText(str);
                String str3 = this.phoneFormatMap.get(str2);
                this.phoneField.setHintText(str3 != null ? str3.replace('X', 8211) : null);
                this.countryState = 0;
                this.ignoreOnTextChange = false;
            }
        }

        public void onItemSelected(AdapterView<?> adapterView, View view2, int i, long j) {
            if (this.ignoreSelection) {
                this.ignoreSelection = false;
                return;
            }
            this.ignoreOnTextChange = true;
            this.codeField.setText(this.countriesMap.get(this.countriesArray.get(i)));
            this.ignoreOnTextChange = false;
        }

        public void onCancelPressed() {
            this.nextPressed = false;
        }

        public void onNextPressed() {
            boolean z;
            if (this.this$0.getParentActivity() != null && !this.nextPressed) {
                TelephonyManager telephonyManager = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService("phone");
                boolean z2 = (telephonyManager.getSimState() == 1 || telephonyManager.getPhoneType() == 0) ? false : true;
                if (Build.VERSION.SDK_INT < 23 || !z2) {
                    z = true;
                } else {
                    z = this.this$0.getParentActivity().checkSelfPermission("android.permission.READ_PHONE_STATE") == 0;
                    if (this.this$0.checkPermissions) {
                        this.this$0.permissionsItems.clear();
                        if (!z) {
                            this.this$0.permissionsItems.add("android.permission.READ_PHONE_STATE");
                        }
                        if (!this.this$0.permissionsItems.isEmpty()) {
                            SharedPreferences globalMainSettings = MessagesController.getGlobalMainSettings();
                            if (globalMainSettings.getBoolean("firstlogin", true) || this.this$0.getParentActivity().shouldShowRequestPermissionRationale("android.permission.READ_PHONE_STATE")) {
                                globalMainSettings.edit().putBoolean("firstlogin", false).commit();
                                AlertDialog.Builder builder = new AlertDialog.Builder((Context) this.this$0.getParentActivity());
                                builder.setTitle(LocaleController.getString("AppName", NUM));
                                builder.setPositiveButton(LocaleController.getString("OK", NUM), (DialogInterface.OnClickListener) null);
                                builder.setMessage(LocaleController.getString("AllowReadCall", NUM));
                                ChangePhoneActivity changePhoneActivity = this.this$0;
                                Dialog unused = changePhoneActivity.permissionsDialog = changePhoneActivity.showDialog(builder.create());
                                return;
                            }
                            this.this$0.getParentActivity().requestPermissions((String[]) this.this$0.permissionsItems.toArray(new String[0]), 6);
                            return;
                        }
                    }
                }
                if (this.countryState == 1) {
                    AlertsCreator.showSimpleAlert(this.this$0, LocaleController.getString("ChooseCountry", NUM));
                } else if (this.codeField.length() == 0) {
                    AlertsCreator.showSimpleAlert(this.this$0, LocaleController.getString("InvalidPhoneNumber", NUM));
                } else {
                    TLRPC$TL_account_sendChangePhoneCode tLRPC$TL_account_sendChangePhoneCode = new TLRPC$TL_account_sendChangePhoneCode();
                    String stripExceptNumbers = PhoneFormat.stripExceptNumbers("" + this.codeField.getText() + this.phoneField.getText());
                    tLRPC$TL_account_sendChangePhoneCode.phone_number = stripExceptNumbers;
                    TLRPC$TL_codeSettings tLRPC$TL_codeSettings = new TLRPC$TL_codeSettings();
                    tLRPC$TL_account_sendChangePhoneCode.settings = tLRPC$TL_codeSettings;
                    tLRPC$TL_codeSettings.allow_flashcall = z2 && z;
                    tLRPC$TL_codeSettings.allow_app_hash = ApplicationLoader.hasPlayServices;
                    SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    if (tLRPC$TL_account_sendChangePhoneCode.settings.allow_app_hash) {
                        sharedPreferences.edit().putString("sms_hash", BuildVars.SMS_HASH).commit();
                    } else {
                        sharedPreferences.edit().remove("sms_hash").commit();
                    }
                    if (tLRPC$TL_account_sendChangePhoneCode.settings.allow_flashcall) {
                        try {
                            String line1Number = telephonyManager.getLine1Number();
                            if (!TextUtils.isEmpty(line1Number)) {
                                tLRPC$TL_account_sendChangePhoneCode.settings.current_number = PhoneNumberUtils.compare(stripExceptNumbers, line1Number);
                                TLRPC$TL_codeSettings tLRPC$TL_codeSettings2 = tLRPC$TL_account_sendChangePhoneCode.settings;
                                if (!tLRPC$TL_codeSettings2.current_number) {
                                    tLRPC$TL_codeSettings2.allow_flashcall = false;
                                }
                            } else {
                                tLRPC$TL_account_sendChangePhoneCode.settings.current_number = false;
                            }
                        } catch (Exception e) {
                            tLRPC$TL_account_sendChangePhoneCode.settings.allow_flashcall = false;
                            FileLog.e((Throwable) e);
                        }
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("phone", "+" + this.codeField.getText() + " " + this.phoneField.getText());
                    try {
                        bundle.putString("ephone", "+" + PhoneFormat.stripExceptNumbers(this.codeField.getText().toString()) + " " + PhoneFormat.stripExceptNumbers(this.phoneField.getText().toString()));
                    } catch (Exception e2) {
                        FileLog.e((Throwable) e2);
                        bundle.putString("ephone", "+" + stripExceptNumbers);
                    }
                    bundle.putString("phoneFormated", stripExceptNumbers);
                    this.nextPressed = true;
                    this.this$0.needShowProgress();
                    ConnectionsManager.getInstance(this.this$0.currentAccount).sendRequest(tLRPC$TL_account_sendChangePhoneCode, new RequestDelegate(bundle, tLRPC$TL_account_sendChangePhoneCode) {
                        public final /* synthetic */ Bundle f$1;
                        public final /* synthetic */ TLRPC$TL_account_sendChangePhoneCode f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                            ChangePhoneActivity.PhoneView.this.lambda$onNextPressed$7$ChangePhoneActivity$PhoneView(this.f$1, this.f$2, tLObject, tLRPC$TL_error);
                        }
                    }, 2);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onNextPressed$7 */
        public /* synthetic */ void lambda$onNextPressed$7$ChangePhoneActivity$PhoneView(Bundle bundle, TLRPC$TL_account_sendChangePhoneCode tLRPC$TL_account_sendChangePhoneCode, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
            AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_error, bundle, tLObject, tLRPC$TL_account_sendChangePhoneCode) {
                public final /* synthetic */ TLRPC$TL_error f$1;
                public final /* synthetic */ Bundle f$2;
                public final /* synthetic */ TLObject f$3;
                public final /* synthetic */ TLRPC$TL_account_sendChangePhoneCode f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run() {
                    ChangePhoneActivity.PhoneView.this.lambda$null$6$ChangePhoneActivity$PhoneView(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$null$6 */
        public /* synthetic */ void lambda$null$6$ChangePhoneActivity$PhoneView(TLRPC$TL_error tLRPC$TL_error, Bundle bundle, TLObject tLObject, TLRPC$TL_account_sendChangePhoneCode tLRPC$TL_account_sendChangePhoneCode) {
            this.nextPressed = false;
            if (tLRPC$TL_error == null) {
                this.this$0.fillNextCodeParams(bundle, (TLRPC$TL_auth_sentCode) tLObject);
            } else {
                AlertsCreator.processError(this.this$0.currentAccount, tLRPC$TL_error, this.this$0, tLRPC$TL_account_sendChangePhoneCode, bundle.getString("phone"));
            }
            this.this$0.needHideProgress();
        }

        public void onShow() {
            super.onShow();
            if (this.phoneField == null) {
                return;
            }
            if (this.codeField.length() != 0) {
                AndroidUtilities.showKeyboard(this.phoneField);
                this.phoneField.requestFocus();
                HintEditText hintEditText = this.phoneField;
                hintEditText.setSelection(hintEditText.length());
                return;
            }
            AndroidUtilities.showKeyboard(this.codeField);
            this.codeField.requestFocus();
        }

        public String getHeaderName() {
            return LocaleController.getString("ChangePhoneNewNumber", NUM);
        }
    }

    public class LoginActivitySmsView extends SlideView implements NotificationCenter.NotificationCenterDelegate {
        /* access modifiers changed from: private */
        public ImageView blackImageView;
        /* access modifiers changed from: private */
        public RLottieImageView blueImageView;
        /* access modifiers changed from: private */
        public EditTextBoldCursor[] codeField;
        private LinearLayout codeFieldContainer;
        /* access modifiers changed from: private */
        public int codeTime = 15000;
        private Timer codeTimer;
        /* access modifiers changed from: private */
        public TextView confirmTextView;
        /* access modifiers changed from: private */
        public int currentType;
        private String emailPhone;
        RLottieDrawable hintDrawable;
        /* access modifiers changed from: private */
        public boolean ignoreOnTextChange;
        /* access modifiers changed from: private */
        public double lastCodeTime;
        /* access modifiers changed from: private */
        public double lastCurrentTime;
        /* access modifiers changed from: private */
        public String lastError = "";
        /* access modifiers changed from: private */
        public int length;
        private boolean nextPressed;
        /* access modifiers changed from: private */
        public int nextType;
        private String pattern = "*";
        private String phone;
        /* access modifiers changed from: private */
        public String phoneHash;
        /* access modifiers changed from: private */
        public TextView problemText;
        /* access modifiers changed from: private */
        public ProgressView progressView;
        /* access modifiers changed from: private */
        public String requestPhone;
        final /* synthetic */ ChangePhoneActivity this$0;
        /* access modifiers changed from: private */
        public int time = 60000;
        /* access modifiers changed from: private */
        public TextView timeText;
        /* access modifiers changed from: private */
        public Timer timeTimer;
        /* access modifiers changed from: private */
        public int timeout;
        private final Object timerSync = new Object();
        /* access modifiers changed from: private */
        public TextView titleTextView;
        /* access modifiers changed from: private */
        public boolean waitingForEvent;

        static /* synthetic */ void lambda$onBackPressed$9(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        }

        public boolean needBackButton() {
            return true;
        }

        /* JADX WARNING: Illegal instructions before constructor call */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public LoginActivitySmsView(org.telegram.ui.ChangePhoneActivity r25, android.content.Context r26, int r27) {
            /*
                r24 = this;
                r0 = r24
                r1 = r25
                r2 = r26
                r0.this$0 = r1
                r0.<init>(r2)
                java.lang.Object r3 = new java.lang.Object
                r3.<init>()
                r0.timerSync = r3
                r3 = 60000(0xea60, float:8.4078E-41)
                r0.time = r3
                r3 = 15000(0x3a98, float:2.102E-41)
                r0.codeTime = r3
                java.lang.String r3 = ""
                r0.lastError = r3
                java.lang.String r3 = "*"
                r0.pattern = r3
                r3 = r27
                r0.currentType = r3
                r3 = 1
                r0.setOrientation(r3)
                android.widget.TextView r4 = new android.widget.TextView
                r4.<init>(r2)
                r0.confirmTextView = r4
                java.lang.String r5 = "windowBackgroundWhiteGrayText6"
                int r6 = org.telegram.ui.ActionBar.Theme.getColor(r5)
                r4.setTextColor(r6)
                android.widget.TextView r4 = r0.confirmTextView
                r6 = 1096810496(0x41600000, float:14.0)
                r4.setTextSize(r3, r6)
                android.widget.TextView r4 = r0.confirmTextView
                r7 = 1073741824(0x40000000, float:2.0)
                int r8 = org.telegram.messenger.AndroidUtilities.dp(r7)
                float r8 = (float) r8
                r9 = 1065353216(0x3var_, float:1.0)
                r4.setLineSpacing(r8, r9)
                android.widget.TextView r4 = new android.widget.TextView
                r4.<init>(r2)
                r0.titleTextView = r4
                java.lang.String r8 = "windowBackgroundWhiteBlackText"
                int r10 = org.telegram.ui.ActionBar.Theme.getColor(r8)
                r4.setTextColor(r10)
                android.widget.TextView r4 = r0.titleTextView
                r10 = 1099956224(0x41900000, float:18.0)
                r4.setTextSize(r3, r10)
                android.widget.TextView r4 = r0.titleTextView
                java.lang.String r10 = "fonts/rmedium.ttf"
                android.graphics.Typeface r10 = org.telegram.messenger.AndroidUtilities.getTypeface(r10)
                r4.setTypeface(r10)
                android.widget.TextView r4 = r0.titleTextView
                boolean r10 = org.telegram.messenger.LocaleController.isRTL
                r12 = 3
                if (r10 == 0) goto L_0x007d
                r10 = 5
                goto L_0x007e
            L_0x007d:
                r10 = 3
            L_0x007e:
                r4.setGravity(r10)
                android.widget.TextView r4 = r0.titleTextView
                int r10 = org.telegram.messenger.AndroidUtilities.dp(r7)
                float r10 = (float) r10
                r4.setLineSpacing(r10, r9)
                android.widget.TextView r4 = r0.titleTextView
                r10 = 49
                r4.setGravity(r10)
                int r4 = r0.currentType
                r13 = -2
                if (r4 != r12) goto L_0x012c
                android.widget.TextView r4 = r0.confirmTextView
                boolean r8 = org.telegram.messenger.LocaleController.isRTL
                if (r8 == 0) goto L_0x009f
                r8 = 5
                goto L_0x00a0
            L_0x009f:
                r8 = 3
            L_0x00a0:
                r8 = r8 | 48
                r4.setGravity(r8)
                android.widget.FrameLayout r4 = new android.widget.FrameLayout
                r4.<init>(r2)
                boolean r8 = org.telegram.messenger.LocaleController.isRTL
                if (r8 == 0) goto L_0x00b0
                r8 = 5
                goto L_0x00b1
            L_0x00b0:
                r8 = 3
            L_0x00b1:
                android.widget.LinearLayout$LayoutParams r8 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r13, (int) r13, (int) r8)
                r0.addView(r4, r8)
                android.widget.ImageView r8 = new android.widget.ImageView
                r8.<init>(r2)
                r14 = 2131165888(0x7var_c0, float:1.7946006E38)
                r8.setImageResource(r14)
                boolean r14 = org.telegram.messenger.LocaleController.isRTL
                if (r14 == 0) goto L_0x00fb
                r15 = 64
                r16 = 1117257728(0x42980000, float:76.0)
                r17 = 19
                r18 = 1073741824(0x40000000, float:2.0)
                r19 = 1073741824(0x40000000, float:2.0)
                r20 = 0
                r21 = 0
                android.widget.FrameLayout$LayoutParams r14 = org.telegram.ui.Components.LayoutHelper.createFrame(r15, r16, r17, r18, r19, r20, r21)
                r4.addView(r8, r14)
                android.widget.TextView r8 = r0.confirmTextView
                r14 = -1
                r15 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
                boolean r16 = org.telegram.messenger.LocaleController.isRTL
                if (r16 == 0) goto L_0x00e8
                r16 = 5
                goto L_0x00ea
            L_0x00e8:
                r16 = 3
            L_0x00ea:
                r17 = 1118044160(0x42a40000, float:82.0)
                r18 = 0
                r19 = 0
                r20 = 0
                android.widget.FrameLayout$LayoutParams r14 = org.telegram.ui.Components.LayoutHelper.createFrame(r14, r15, r16, r17, r18, r19, r20)
                r4.addView(r8, r14)
                goto L_0x0231
            L_0x00fb:
                android.widget.TextView r15 = r0.confirmTextView
                r16 = -1
                r17 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
                if (r14 == 0) goto L_0x0106
                r18 = 5
                goto L_0x0108
            L_0x0106:
                r18 = 3
            L_0x0108:
                r19 = 0
                r20 = 0
                r21 = 1118044160(0x42a40000, float:82.0)
                r22 = 0
                android.widget.FrameLayout$LayoutParams r14 = org.telegram.ui.Components.LayoutHelper.createFrame(r16, r17, r18, r19, r20, r21, r22)
                r4.addView(r15, r14)
                r16 = 64
                r17 = 1117257728(0x42980000, float:76.0)
                r18 = 21
                r20 = 1073741824(0x40000000, float:2.0)
                r21 = 0
                r22 = 1073741824(0x40000000, float:2.0)
                android.widget.FrameLayout$LayoutParams r14 = org.telegram.ui.Components.LayoutHelper.createFrame(r16, r17, r18, r19, r20, r21, r22)
                r4.addView(r8, r14)
                goto L_0x0231
            L_0x012c:
                android.widget.TextView r4 = r0.confirmTextView
                r4.setGravity(r10)
                android.widget.FrameLayout r4 = new android.widget.FrameLayout
                r4.<init>(r2)
                android.widget.LinearLayout$LayoutParams r14 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r13, (int) r13, (int) r10)
                r0.addView(r4, r14)
                int r14 = r0.currentType
                java.lang.String r15 = "chats_actionBackground"
                if (r14 != r3) goto L_0x01ac
                android.widget.ImageView r14 = new android.widget.ImageView
                r14.<init>(r2)
                r0.blackImageView = r14
                r11 = 2131166033(0x7var_, float:1.79463E38)
                r14.setImageResource(r11)
                android.widget.ImageView r11 = r0.blackImageView
                android.graphics.PorterDuffColorFilter r14 = new android.graphics.PorterDuffColorFilter
                int r8 = org.telegram.ui.ActionBar.Theme.getColor(r8)
                android.graphics.PorterDuff$Mode r10 = android.graphics.PorterDuff.Mode.MULTIPLY
                r14.<init>(r8, r10)
                r11.setColorFilter(r14)
                android.widget.ImageView r8 = r0.blackImageView
                r17 = -2
                r18 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
                r19 = 51
                r20 = 0
                r21 = 0
                r22 = 0
                r23 = 0
                android.widget.FrameLayout$LayoutParams r10 = org.telegram.ui.Components.LayoutHelper.createFrame(r17, r18, r19, r20, r21, r22, r23)
                r4.addView(r8, r10)
                org.telegram.ui.Components.RLottieImageView r8 = new org.telegram.ui.Components.RLottieImageView
                r8.<init>(r2)
                r0.blueImageView = r8
                r10 = 2131166031(0x7var_f, float:1.7946296E38)
                r8.setImageResource(r10)
                org.telegram.ui.Components.RLottieImageView r8 = r0.blueImageView
                android.graphics.PorterDuffColorFilter r10 = new android.graphics.PorterDuffColorFilter
                int r11 = org.telegram.ui.ActionBar.Theme.getColor(r15)
                android.graphics.PorterDuff$Mode r14 = android.graphics.PorterDuff.Mode.MULTIPLY
                r10.<init>(r11, r14)
                r8.setColorFilter(r10)
                org.telegram.ui.Components.RLottieImageView r8 = r0.blueImageView
                android.widget.FrameLayout$LayoutParams r10 = org.telegram.ui.Components.LayoutHelper.createFrame(r17, r18, r19, r20, r21, r22, r23)
                r4.addView(r8, r10)
                android.widget.TextView r4 = r0.titleTextView
                r8 = 2131627333(0x7f0e0d45, float:1.8881927E38)
                java.lang.String r10 = "SentAppCodeTitle"
                java.lang.String r8 = org.telegram.messenger.LocaleController.getString(r10, r8)
                r4.setText(r8)
                goto L_0x020f
            L_0x01ac:
                org.telegram.ui.Components.RLottieImageView r8 = new org.telegram.ui.Components.RLottieImageView
                r8.<init>(r2)
                r0.blueImageView = r8
                org.telegram.ui.Components.RLottieDrawable r8 = new org.telegram.ui.Components.RLottieDrawable
                r18 = 2131558471(0x7f0d0047, float:1.8742259E38)
                r10 = 1115684864(0x42800000, float:64.0)
                int r20 = org.telegram.messenger.AndroidUtilities.dp(r10)
                int r21 = org.telegram.messenger.AndroidUtilities.dp(r10)
                r22 = 1
                r23 = 0
                java.lang.String r19 = "NUM"
                r17 = r8
                r17.<init>((int) r18, (java.lang.String) r19, (int) r20, (int) r21, (boolean) r22, (int[]) r23)
                r0.hintDrawable = r8
                int r10 = org.telegram.ui.ActionBar.Theme.getColor(r15)
                java.lang.String r11 = "Bubble.**"
                r8.setLayerColor(r11, r10)
                org.telegram.ui.Components.RLottieDrawable r8 = r0.hintDrawable
                int r10 = org.telegram.ui.ActionBar.Theme.getColor(r15)
                java.lang.String r11 = "Phone.**"
                r8.setLayerColor(r11, r10)
                org.telegram.ui.Components.RLottieImageView r8 = r0.blueImageView
                org.telegram.ui.Components.RLottieDrawable r10 = r0.hintDrawable
                r8.setAnimation(r10)
                org.telegram.ui.Components.RLottieImageView r8 = r0.blueImageView
                r17 = -2
                r18 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
                r19 = 51
                r20 = 0
                r21 = 0
                r22 = 0
                r23 = 0
                android.widget.FrameLayout$LayoutParams r10 = org.telegram.ui.Components.LayoutHelper.createFrame(r17, r18, r19, r20, r21, r22, r23)
                r4.addView(r8, r10)
                android.widget.TextView r4 = r0.titleTextView
                r8 = 2131627337(0x7f0e0d49, float:1.8881936E38)
                java.lang.String r10 = "SentSmsCodeTitle"
                java.lang.String r8 = org.telegram.messenger.LocaleController.getString(r10, r8)
                r4.setText(r8)
            L_0x020f:
                android.widget.TextView r4 = r0.titleTextView
                r17 = -2
                r18 = -2
                r19 = 49
                r20 = 0
                r21 = 18
                r22 = 0
                r23 = 0
                android.widget.LinearLayout$LayoutParams r8 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r17, (int) r18, (int) r19, (int) r20, (int) r21, (int) r22, (int) r23)
                r0.addView(r4, r8)
                android.widget.TextView r4 = r0.confirmTextView
                r21 = 17
                android.widget.LinearLayout$LayoutParams r8 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r17, (int) r18, (int) r19, (int) r20, (int) r21, (int) r22, (int) r23)
                r0.addView(r4, r8)
            L_0x0231:
                android.widget.LinearLayout r4 = new android.widget.LinearLayout
                r4.<init>(r2)
                r0.codeFieldContainer = r4
                r8 = 0
                r4.setOrientation(r8)
                android.widget.LinearLayout r4 = r0.codeFieldContainer
                r10 = 36
                android.widget.LinearLayout$LayoutParams r10 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r13, (int) r10, (int) r3)
                r0.addView(r4, r10)
                int r4 = r0.currentType
                if (r4 != r12) goto L_0x0252
                android.widget.LinearLayout r4 = r0.codeFieldContainer
                r10 = 8
                r4.setVisibility(r10)
            L_0x0252:
                org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$1 r4 = new org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$1
                r4.<init>(r0, r2, r1)
                r0.timeText = r4
                int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
                r4.setTextColor(r5)
                android.widget.TextView r4 = r0.timeText
                int r5 = org.telegram.messenger.AndroidUtilities.dp(r7)
                float r5 = (float) r5
                r4.setLineSpacing(r5, r9)
                int r4 = r0.currentType
                r5 = 1097859072(0x41700000, float:15.0)
                r10 = 1092616192(0x41200000, float:10.0)
                if (r4 != r12) goto L_0x02b0
                android.widget.TextView r4 = r0.timeText
                r4.setTextSize(r3, r6)
                android.widget.TextView r4 = r0.timeText
                boolean r6 = org.telegram.messenger.LocaleController.isRTL
                if (r6 == 0) goto L_0x027f
                r6 = 5
                goto L_0x0280
            L_0x027f:
                r6 = 3
            L_0x0280:
                android.widget.LinearLayout$LayoutParams r6 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r13, (int) r13, (int) r6)
                r0.addView(r4, r6)
                org.telegram.ui.ChangePhoneActivity$ProgressView r4 = new org.telegram.ui.ChangePhoneActivity$ProgressView
                r4.<init>(r1, r2)
                r0.progressView = r4
                android.widget.TextView r4 = r0.timeText
                boolean r6 = org.telegram.messenger.LocaleController.isRTL
                if (r6 == 0) goto L_0x0296
                r11 = 5
                goto L_0x0297
            L_0x0296:
                r11 = 3
            L_0x0297:
                r4.setGravity(r11)
                org.telegram.ui.ChangePhoneActivity$ProgressView r4 = r0.progressView
                r17 = -1
                r18 = 3
                r19 = 0
                r20 = 1094713344(0x41400000, float:12.0)
                r21 = 0
                r22 = 0
                android.widget.LinearLayout$LayoutParams r6 = org.telegram.ui.Components.LayoutHelper.createLinear(r17, r18, r19, r20, r21, r22)
                r0.addView(r4, r6)
                goto L_0x02d2
            L_0x02b0:
                android.widget.TextView r4 = r0.timeText
                int r6 = org.telegram.messenger.AndroidUtilities.dp(r7)
                int r11 = org.telegram.messenger.AndroidUtilities.dp(r10)
                r4.setPadding(r8, r6, r8, r11)
                android.widget.TextView r4 = r0.timeText
                r4.setTextSize(r3, r5)
                android.widget.TextView r4 = r0.timeText
                r6 = 49
                r4.setGravity(r6)
                android.widget.TextView r4 = r0.timeText
                android.widget.LinearLayout$LayoutParams r11 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r13, (int) r13, (int) r6)
                r0.addView(r4, r11)
            L_0x02d2:
                org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$2 r4 = new org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$2
                r4.<init>(r0, r2, r1)
                r0.problemText = r4
                java.lang.String r1 = "windowBackgroundWhiteBlueText4"
                int r1 = org.telegram.ui.ActionBar.Theme.getColor(r1)
                r4.setTextColor(r1)
                android.widget.TextView r1 = r0.problemText
                int r2 = org.telegram.messenger.AndroidUtilities.dp(r7)
                float r2 = (float) r2
                r1.setLineSpacing(r2, r9)
                android.widget.TextView r1 = r0.problemText
                int r2 = org.telegram.messenger.AndroidUtilities.dp(r7)
                int r4 = org.telegram.messenger.AndroidUtilities.dp(r10)
                r1.setPadding(r8, r2, r8, r4)
                android.widget.TextView r1 = r0.problemText
                r1.setTextSize(r3, r5)
                android.widget.TextView r1 = r0.problemText
                r2 = 49
                r1.setGravity(r2)
                int r1 = r0.currentType
                if (r1 != r3) goto L_0x0319
                android.widget.TextView r1 = r0.problemText
                r2 = 2131625157(0x7f0e04c5, float:1.8877514E38)
                java.lang.String r3 = "DidNotGetTheCodeSms"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r1.setText(r2)
                goto L_0x0327
            L_0x0319:
                android.widget.TextView r1 = r0.problemText
                r2 = 2131625156(0x7f0e04c4, float:1.8877512E38)
                java.lang.String r3 = "DidNotGetTheCode"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r1.setText(r2)
            L_0x0327:
                android.widget.TextView r1 = r0.problemText
                r2 = 49
                android.widget.LinearLayout$LayoutParams r2 = org.telegram.ui.Components.LayoutHelper.createLinear((int) r13, (int) r13, (int) r2)
                r0.addView(r1, r2)
                android.widget.TextView r1 = r0.problemText
                org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$wEGADCeXx32rTApiOc0EUelkecU r2 = new org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$wEGADCeXx32rTApiOc0EUelkecU
                r2.<init>()
                r1.setOnClickListener(r2)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.<init>(org.telegram.ui.ChangePhoneActivity, android.content.Context, int):void");
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$0 */
        public /* synthetic */ void lambda$new$0$ChangePhoneActivity$LoginActivitySmsView(View view) {
            if (!this.nextPressed) {
                int i = this.nextType;
                if (!((i == 4 && this.currentType == 2) || i == 0)) {
                    resendCode();
                    return;
                }
                try {
                    PackageInfo packageInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    String format = String.format(Locale.US, "%s (%d)", new Object[]{packageInfo.versionName, Integer.valueOf(packageInfo.versionCode)});
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("message/rfCLASSNAME");
                    intent.putExtra("android.intent.extra.EMAIL", new String[]{"sms@stel.com"});
                    intent.putExtra("android.intent.extra.SUBJECT", "Android registration/login issue " + format + " " + this.emailPhone);
                    intent.putExtra("android.intent.extra.TEXT", "Phone: " + this.requestPhone + "\nApp version: " + format + "\nOS version: SDK " + Build.VERSION.SDK_INT + "\nDevice Name: " + Build.MANUFACTURER + Build.MODEL + "\nLocale: " + Locale.getDefault() + "\nError: " + this.lastError);
                    getContext().startActivity(Intent.createChooser(intent, "Send email..."));
                } catch (Exception unused) {
                    AlertsCreator.showSimpleAlert(this.this$0, LocaleController.getString("NoMailInstalled", NUM));
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int i, int i2) {
            RLottieImageView rLottieImageView;
            super.onMeasure(i, i2);
            if (this.currentType != 3 && (rLottieImageView = this.blueImageView) != null) {
                int measuredHeight = rLottieImageView.getMeasuredHeight() + this.titleTextView.getMeasuredHeight() + this.confirmTextView.getMeasuredHeight() + AndroidUtilities.dp(35.0f);
                int dp = AndroidUtilities.dp(80.0f);
                int dp2 = AndroidUtilities.dp(291.0f);
                if (this.this$0.scrollHeight - measuredHeight < dp) {
                    setMeasuredDimension(getMeasuredWidth(), measuredHeight + dp);
                } else if (this.this$0.scrollHeight > dp2) {
                    setMeasuredDimension(getMeasuredWidth(), dp2);
                } else {
                    setMeasuredDimension(getMeasuredWidth(), this.this$0.scrollHeight);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onLayout(boolean z, int i, int i2, int i3, int i4) {
            int i5;
            super.onLayout(z, i, i2, i3, i4);
            if (this.currentType != 3 && this.blueImageView != null) {
                int bottom = this.confirmTextView.getBottom();
                int measuredHeight = getMeasuredHeight() - bottom;
                if (this.problemText.getVisibility() == 0) {
                    int measuredHeight2 = this.problemText.getMeasuredHeight();
                    i5 = (measuredHeight + bottom) - measuredHeight2;
                    TextView textView = this.problemText;
                    textView.layout(textView.getLeft(), i5, this.problemText.getRight(), measuredHeight2 + i5);
                } else if (this.timeText.getVisibility() == 0) {
                    int measuredHeight3 = this.timeText.getMeasuredHeight();
                    i5 = (measuredHeight + bottom) - measuredHeight3;
                    TextView textView2 = this.timeText;
                    textView2.layout(textView2.getLeft(), i5, this.timeText.getRight(), measuredHeight3 + i5);
                } else {
                    i5 = measuredHeight + bottom;
                }
                int measuredHeight4 = this.codeFieldContainer.getMeasuredHeight();
                int i6 = (((i5 - bottom) - measuredHeight4) / 2) + bottom;
                LinearLayout linearLayout = this.codeFieldContainer;
                linearLayout.layout(linearLayout.getLeft(), i6, this.codeFieldContainer.getRight(), measuredHeight4 + i6);
            }
        }

        /* access modifiers changed from: private */
        public void resendCode() {
            Bundle bundle = new Bundle();
            bundle.putString("phone", this.phone);
            bundle.putString("ephone", this.emailPhone);
            bundle.putString("phoneFormated", this.requestPhone);
            this.nextPressed = true;
            this.this$0.needShowProgress();
            TLRPC$TL_auth_resendCode tLRPC$TL_auth_resendCode = new TLRPC$TL_auth_resendCode();
            tLRPC$TL_auth_resendCode.phone_number = this.requestPhone;
            tLRPC$TL_auth_resendCode.phone_code_hash = this.phoneHash;
            ConnectionsManager.getInstance(this.this$0.currentAccount).sendRequest(tLRPC$TL_auth_resendCode, new RequestDelegate(bundle, tLRPC$TL_auth_resendCode) {
                public final /* synthetic */ Bundle f$1;
                public final /* synthetic */ TLRPC$TL_auth_resendCode f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                    ChangePhoneActivity.LoginActivitySmsView.this.lambda$resendCode$3$ChangePhoneActivity$LoginActivitySmsView(this.f$1, this.f$2, tLObject, tLRPC$TL_error);
                }
            }, 2);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$resendCode$3 */
        public /* synthetic */ void lambda$resendCode$3$ChangePhoneActivity$LoginActivitySmsView(Bundle bundle, TLRPC$TL_auth_resendCode tLRPC$TL_auth_resendCode, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
            AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_error, bundle, tLObject, tLRPC$TL_auth_resendCode) {
                public final /* synthetic */ TLRPC$TL_error f$1;
                public final /* synthetic */ Bundle f$2;
                public final /* synthetic */ TLObject f$3;
                public final /* synthetic */ TLRPC$TL_auth_resendCode f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run() {
                    ChangePhoneActivity.LoginActivitySmsView.this.lambda$null$2$ChangePhoneActivity$LoginActivitySmsView(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$null$2 */
        public /* synthetic */ void lambda$null$2$ChangePhoneActivity$LoginActivitySmsView(TLRPC$TL_error tLRPC$TL_error, Bundle bundle, TLObject tLObject, TLRPC$TL_auth_resendCode tLRPC$TL_auth_resendCode) {
            this.nextPressed = false;
            if (tLRPC$TL_error == null) {
                this.this$0.fillNextCodeParams(bundle, (TLRPC$TL_auth_sentCode) tLObject);
            } else {
                AlertDialog alertDialog = (AlertDialog) AlertsCreator.processError(this.this$0.currentAccount, tLRPC$TL_error, this.this$0, tLRPC$TL_auth_resendCode, new Object[0]);
                if (alertDialog != null && tLRPC$TL_error.text.contains("PHONE_CODE_EXPIRED")) {
                    alertDialog.setPositiveButtonListener(new DialogInterface.OnClickListener() {
                        public final void onClick(DialogInterface dialogInterface, int i) {
                            ChangePhoneActivity.LoginActivitySmsView.this.lambda$null$1$ChangePhoneActivity$LoginActivitySmsView(dialogInterface, i);
                        }
                    });
                }
            }
            this.this$0.needHideProgress();
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$null$1 */
        public /* synthetic */ void lambda$null$1$ChangePhoneActivity$LoginActivitySmsView(DialogInterface dialogInterface, int i) {
            onBackPressed(true);
            this.this$0.finishFragment();
        }

        public String getHeaderName() {
            if (this.currentType == 1) {
                return this.phone;
            }
            return LocaleController.getString("YourCode", NUM);
        }

        public void onCancelPressed() {
            this.nextPressed = false;
        }

        public void setParams(Bundle bundle, boolean z) {
            int i;
            int i2;
            Bundle bundle2 = bundle;
            if (bundle2 != null) {
                this.waitingForEvent = true;
                int i3 = this.currentType;
                if (i3 == 2) {
                    AndroidUtilities.setWaitingForSms(true);
                    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didReceiveSmsCode);
                } else if (i3 == 3) {
                    AndroidUtilities.setWaitingForCall(true);
                    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didReceiveCall);
                }
                this.phone = bundle2.getString("phone");
                this.emailPhone = bundle2.getString("ephone");
                this.requestPhone = bundle2.getString("phoneFormated");
                this.phoneHash = bundle2.getString("phoneHash");
                int i4 = bundle2.getInt("timeout");
                this.time = i4;
                this.timeout = i4;
                this.nextType = bundle2.getInt("nextType");
                this.pattern = bundle2.getString("pattern");
                int i5 = bundle2.getInt("length");
                this.length = i5;
                if (i5 == 0) {
                    this.length = 5;
                }
                EditTextBoldCursor[] editTextBoldCursorArr = this.codeField;
                CharSequence charSequence = "";
                int i6 = 8;
                if (editTextBoldCursorArr != null && editTextBoldCursorArr.length == this.length) {
                    int i7 = 0;
                    while (true) {
                        EditTextBoldCursor[] editTextBoldCursorArr2 = this.codeField;
                        if (i7 >= editTextBoldCursorArr2.length) {
                            break;
                        }
                        editTextBoldCursorArr2[i7].setText(charSequence);
                        i7++;
                    }
                } else {
                    this.codeField = new EditTextBoldCursor[this.length];
                    final int i8 = 0;
                    while (i8 < this.length) {
                        this.codeField[i8] = new EditTextBoldCursor(getContext());
                        this.codeField[i8].setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
                        this.codeField[i8].setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
                        this.codeField[i8].setCursorSize(AndroidUtilities.dp(20.0f));
                        this.codeField[i8].setCursorWidth(1.5f);
                        Drawable mutate = getResources().getDrawable(NUM).mutate();
                        mutate.setColorFilter(new PorterDuffColorFilter(Theme.getColor("windowBackgroundWhiteInputFieldActivated"), PorterDuff.Mode.MULTIPLY));
                        this.codeField[i8].setBackgroundDrawable(mutate);
                        this.codeField[i8].setImeOptions(NUM);
                        this.codeField[i8].setTextSize(1, 20.0f);
                        this.codeField[i8].setMaxLines(1);
                        this.codeField[i8].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                        this.codeField[i8].setPadding(0, 0, 0, 0);
                        this.codeField[i8].setGravity(49);
                        if (this.currentType == 3) {
                            this.codeField[i8].setEnabled(false);
                            this.codeField[i8].setInputType(0);
                            this.codeField[i8].setVisibility(8);
                        } else {
                            this.codeField[i8].setInputType(3);
                        }
                        this.codeFieldContainer.addView(this.codeField[i8], LayoutHelper.createLinear(34, 36, 1, 0, 0, i8 != this.length - 1 ? 7 : 0, 0));
                        this.codeField[i8].addTextChangedListener(new TextWatcher() {
                            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                            }

                            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                            }

                            public void afterTextChanged(Editable editable) {
                                int length;
                                if (!LoginActivitySmsView.this.ignoreOnTextChange && (length = editable.length()) >= 1) {
                                    if (length > 1) {
                                        String obj = editable.toString();
                                        boolean unused = LoginActivitySmsView.this.ignoreOnTextChange = true;
                                        for (int i = 0; i < Math.min(LoginActivitySmsView.this.length - i8, length); i++) {
                                            if (i == 0) {
                                                editable.replace(0, length, obj.substring(i, i + 1));
                                            } else {
                                                LoginActivitySmsView.this.codeField[i8 + i].setText(obj.substring(i, i + 1));
                                            }
                                        }
                                        boolean unused2 = LoginActivitySmsView.this.ignoreOnTextChange = false;
                                    }
                                    if (i8 != LoginActivitySmsView.this.length - 1) {
                                        LoginActivitySmsView.this.codeField[i8 + 1].setSelection(LoginActivitySmsView.this.codeField[i8 + 1].length());
                                        LoginActivitySmsView.this.codeField[i8 + 1].requestFocus();
                                    }
                                    if ((i8 == LoginActivitySmsView.this.length - 1 || (i8 == LoginActivitySmsView.this.length - 2 && length >= 2)) && LoginActivitySmsView.this.getCode().length() == LoginActivitySmsView.this.length) {
                                        LoginActivitySmsView.this.onNextPressed();
                                    }
                                }
                            }
                        });
                        this.codeField[i8].setOnKeyListener(new View.OnKeyListener(i8) {
                            public final /* synthetic */ int f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final boolean onKey(View view, int i, KeyEvent keyEvent) {
                                return ChangePhoneActivity.LoginActivitySmsView.this.lambda$setParams$4$ChangePhoneActivity$LoginActivitySmsView(this.f$1, view, i, keyEvent);
                            }
                        });
                        this.codeField[i8].setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            public final boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                                return ChangePhoneActivity.LoginActivitySmsView.this.lambda$setParams$5$ChangePhoneActivity$LoginActivitySmsView(textView, i, keyEvent);
                            }
                        });
                        i8++;
                    }
                }
                ProgressView progressView2 = this.progressView;
                if (progressView2 != null) {
                    progressView2.setVisibility(this.nextType != 0 ? 0 : 8);
                }
                if (this.phone != null) {
                    String format = PhoneFormat.getInstance().format(this.phone);
                    int i9 = this.currentType;
                    if (i9 == 1) {
                        charSequence = AndroidUtilities.replaceTags(LocaleController.getString("SentAppCode", NUM));
                    } else if (i9 == 2) {
                        charSequence = AndroidUtilities.replaceTags(LocaleController.formatString("SentSmsCode", NUM, LocaleController.addNbsp(format)));
                    } else if (i9 == 3) {
                        charSequence = AndroidUtilities.replaceTags(LocaleController.formatString("SentCallCode", NUM, LocaleController.addNbsp(format)));
                    } else if (i9 == 4) {
                        charSequence = AndroidUtilities.replaceTags(LocaleController.formatString("SentCallOnly", NUM, LocaleController.addNbsp(format)));
                    }
                    this.confirmTextView.setText(charSequence);
                    if (this.currentType != 3) {
                        AndroidUtilities.showKeyboard(this.codeField[0]);
                        this.codeField[0].requestFocus();
                    } else {
                        AndroidUtilities.hideKeyboard(this.codeField[0]);
                    }
                    destroyTimer();
                    destroyCodeTimer();
                    this.lastCurrentTime = (double) System.currentTimeMillis();
                    int i10 = this.currentType;
                    if (i10 == 1) {
                        this.problemText.setVisibility(0);
                        this.timeText.setVisibility(8);
                    } else if (i10 == 3 && ((i2 = this.nextType) == 4 || i2 == 2)) {
                        this.problemText.setVisibility(8);
                        this.timeText.setVisibility(0);
                        int i11 = this.nextType;
                        if (i11 == 4) {
                            this.timeText.setText(LocaleController.formatString("CallText", NUM, 1, 0));
                        } else if (i11 == 2) {
                            this.timeText.setText(LocaleController.formatString("SmsText", NUM, 1, 0));
                        }
                        createTimer();
                    } else if (i10 == 2 && ((i = this.nextType) == 4 || i == 3)) {
                        this.timeText.setText(LocaleController.formatString("CallText", NUM, 2, 0));
                        this.problemText.setVisibility(this.time < 1000 ? 0 : 8);
                        TextView textView = this.timeText;
                        if (this.time >= 1000) {
                            i6 = 0;
                        }
                        textView.setVisibility(i6);
                        createTimer();
                    } else if (i10 == 4 && this.nextType == 2) {
                        this.timeText.setText(LocaleController.formatString("SmsText", NUM, 2, 0));
                        this.problemText.setVisibility(this.time < 1000 ? 0 : 8);
                        TextView textView2 = this.timeText;
                        if (this.time >= 1000) {
                            i6 = 0;
                        }
                        textView2.setVisibility(i6);
                        createTimer();
                    } else {
                        this.timeText.setVisibility(8);
                        this.problemText.setVisibility(8);
                        createCodeTimer();
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$setParams$4 */
        public /* synthetic */ boolean lambda$setParams$4$ChangePhoneActivity$LoginActivitySmsView(int i, View view, int i2, KeyEvent keyEvent) {
            if (i2 != 67 || this.codeField[i].length() != 0 || i <= 0) {
                return false;
            }
            EditTextBoldCursor[] editTextBoldCursorArr = this.codeField;
            int i3 = i - 1;
            editTextBoldCursorArr[i3].setSelection(editTextBoldCursorArr[i3].length());
            this.codeField[i3].requestFocus();
            this.codeField[i3].dispatchKeyEvent(keyEvent);
            return true;
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$setParams$5 */
        public /* synthetic */ boolean lambda$setParams$5$ChangePhoneActivity$LoginActivitySmsView(TextView textView, int i, KeyEvent keyEvent) {
            if (i != 5) {
                return false;
            }
            onNextPressed();
            return true;
        }

        /* access modifiers changed from: private */
        public void createCodeTimer() {
            if (this.codeTimer == null) {
                this.codeTime = 15000;
                this.codeTimer = new Timer();
                this.lastCodeTime = (double) System.currentTimeMillis();
                this.codeTimer.schedule(new TimerTask() {
                    public void run() {
                        AndroidUtilities.runOnUIThread(
                        /*  JADX ERROR: Method code generation error
                            jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0005: INVOKE  
                              (wrap: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$4$vCh3KJ8GR57ugM8AZrTUiTH09FY : 0x0002: CONSTRUCTOR  (r0v0 org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$4$vCh3KJ8GR57ugM8AZrTUiTH09FY) = 
                              (r1v0 'this' org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$4 A[THIS])
                             call: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$4$vCh3KJ8GR57ugM8AZrTUiTH09FY.<init>(org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$4):void type: CONSTRUCTOR)
                             org.telegram.messenger.AndroidUtilities.runOnUIThread(java.lang.Runnable):void type: STATIC in method: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.4.run():void, dex: classes.dex
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                            	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                            	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                            	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                            	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                            	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                            	at java.util.ArrayList.forEach(ArrayList.java:1259)
                            	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                            	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                            	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                            	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                            	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                            	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                            	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                            	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                            	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                            	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                            	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                            	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                            	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                            	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                            	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                            	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                            	at java.util.ArrayList.forEach(ArrayList.java:1259)
                            	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                            	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                            	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                            	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                            	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                            	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                            	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                            	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                            	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                            	at jadx.core.codegen.ClassGen.addInnerClass(ClassGen.java:249)
                            	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:238)
                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                            	at java.util.ArrayList.forEach(ArrayList.java:1259)
                            	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                            	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                            	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                            	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                            	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                            	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                            	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                            	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                            	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                            	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                            	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                            	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                            	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0002: CONSTRUCTOR  (r0v0 org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$4$vCh3KJ8GR57ugM8AZrTUiTH09FY) = 
                              (r1v0 'this' org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$4 A[THIS])
                             call: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$4$vCh3KJ8GR57ugM8AZrTUiTH09FY.<init>(org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$4):void type: CONSTRUCTOR in method: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.4.run():void, dex: classes.dex
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                            	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                            	... 83 more
                            Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$4$vCh3KJ8GR57ugM8AZrTUiTH09FY, state: NOT_LOADED
                            	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                            	... 89 more
                            */
                        /*
                            this = this;
                            org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$4$vCh3KJ8GR57ugM8AZrTUiTH09FY r0 = new org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$4$vCh3KJ8GR57ugM8AZrTUiTH09FY
                            r0.<init>(r1)
                            org.telegram.messenger.AndroidUtilities.runOnUIThread(r0)
                            return
                        */
                        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.AnonymousClass4.run():void");
                    }

                    /* access modifiers changed from: private */
                    /* renamed from: lambda$run$0 */
                    public /* synthetic */ void lambda$run$0$ChangePhoneActivity$LoginActivitySmsView$4() {
                        double currentTimeMillis = (double) System.currentTimeMillis();
                        double access$2400 = LoginActivitySmsView.this.lastCodeTime;
                        Double.isNaN(currentTimeMillis);
                        double d = currentTimeMillis - access$2400;
                        double unused = LoginActivitySmsView.this.lastCodeTime = currentTimeMillis;
                        LoginActivitySmsView loginActivitySmsView = LoginActivitySmsView.this;
                        double access$2500 = (double) loginActivitySmsView.codeTime;
                        Double.isNaN(access$2500);
                        int unused2 = loginActivitySmsView.codeTime = (int) (access$2500 - d);
                        if (LoginActivitySmsView.this.codeTime <= 1000) {
                            LoginActivitySmsView.this.problemText.setVisibility(0);
                            LoginActivitySmsView.this.timeText.setVisibility(8);
                            LoginActivitySmsView.this.destroyCodeTimer();
                        }
                    }
                }, 0, 1000);
            }
        }

        /* access modifiers changed from: private */
        public void destroyCodeTimer() {
            try {
                synchronized (this.timerSync) {
                    Timer timer = this.codeTimer;
                    if (timer != null) {
                        timer.cancel();
                        this.codeTimer = null;
                    }
                }
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }

        private void createTimer() {
            if (this.timeTimer == null) {
                Timer timer = new Timer();
                this.timeTimer = timer;
                timer.schedule(new TimerTask() {
                    public void run() {
                        if (LoginActivitySmsView.this.timeTimer != null) {
                            AndroidUtilities.runOnUIThread(
                            /*  JADX ERROR: Method code generation error
                                jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000e: INVOKE  
                                  (wrap: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$uWYyEdeQNnJNf3petF-wYoE_eFw : 0x000b: CONSTRUCTOR  (r0v2 org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$uWYyEdeQNnJNf3petF-wYoE_eFw) = 
                                  (r1v0 'this' org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5 A[THIS])
                                 call: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$uWYyEdeQNnJNf3petF-wYoE_eFw.<init>(org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5):void type: CONSTRUCTOR)
                                 org.telegram.messenger.AndroidUtilities.runOnUIThread(java.lang.Runnable):void type: STATIC in method: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.5.run():void, dex: classes.dex
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                                	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                                	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                                	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                                	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                	at java.util.ArrayList.forEach(ArrayList.java:1259)
                                	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                                	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                                	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                                	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                                	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                                	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                                	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                                	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                                	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                                	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                                	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                                	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                                	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                                	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                                	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                	at java.util.ArrayList.forEach(ArrayList.java:1259)
                                	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                                	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                                	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                                	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                                	at jadx.core.codegen.ClassGen.addInnerClass(ClassGen.java:249)
                                	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:238)
                                	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                	at java.util.ArrayList.forEach(ArrayList.java:1259)
                                	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                                	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                                	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                                	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                                	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                                	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000b: CONSTRUCTOR  (r0v2 org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$uWYyEdeQNnJNf3petF-wYoE_eFw) = 
                                  (r1v0 'this' org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5 A[THIS])
                                 call: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$uWYyEdeQNnJNf3petF-wYoE_eFw.<init>(org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5):void type: CONSTRUCTOR in method: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.5.run():void, dex: classes.dex
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                                	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                                	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                                	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                                	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                                	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                                	... 90 more
                                Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$uWYyEdeQNnJNf3petF-wYoE_eFw, state: NOT_LOADED
                                	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                                	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                                	... 96 more
                                */
                            /*
                                this = this;
                                org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                java.util.Timer r0 = r0.timeTimer
                                if (r0 != 0) goto L_0x0009
                                return
                            L_0x0009:
                                org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$uWYyEdeQNnJNf3petF-wYoE_eFw r0 = new org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$uWYyEdeQNnJNf3petF-wYoE_eFw
                                r0.<init>(r1)
                                org.telegram.messenger.AndroidUtilities.runOnUIThread(r0)
                                return
                            */
                            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.AnonymousClass5.run():void");
                        }

                        /* access modifiers changed from: private */
                        /* renamed from: lambda$run$2 */
                        public /* synthetic */ void lambda$run$2$ChangePhoneActivity$LoginActivitySmsView$5() {
                            double currentTimeMillis = (double) System.currentTimeMillis();
                            double access$3000 = LoginActivitySmsView.this.lastCurrentTime;
                            Double.isNaN(currentTimeMillis);
                            LoginActivitySmsView loginActivitySmsView = LoginActivitySmsView.this;
                            double access$3100 = (double) loginActivitySmsView.time;
                            Double.isNaN(access$3100);
                            int unused = loginActivitySmsView.time = (int) (access$3100 - (currentTimeMillis - access$3000));
                            double unused2 = LoginActivitySmsView.this.lastCurrentTime = currentTimeMillis;
                            if (LoginActivitySmsView.this.time >= 1000) {
                                int access$31002 = (LoginActivitySmsView.this.time / 1000) / 60;
                                int access$31003 = (LoginActivitySmsView.this.time / 1000) - (access$31002 * 60);
                                if (LoginActivitySmsView.this.nextType == 4 || LoginActivitySmsView.this.nextType == 3) {
                                    LoginActivitySmsView.this.timeText.setText(LocaleController.formatString("CallText", NUM, Integer.valueOf(access$31002), Integer.valueOf(access$31003)));
                                } else if (LoginActivitySmsView.this.nextType == 2) {
                                    LoginActivitySmsView.this.timeText.setText(LocaleController.formatString("SmsText", NUM, Integer.valueOf(access$31002), Integer.valueOf(access$31003)));
                                }
                                if (LoginActivitySmsView.this.progressView != null) {
                                    LoginActivitySmsView.this.progressView.setProgress(1.0f - (((float) LoginActivitySmsView.this.time) / ((float) LoginActivitySmsView.this.timeout)));
                                    return;
                                }
                                return;
                            }
                            if (LoginActivitySmsView.this.progressView != null) {
                                LoginActivitySmsView.this.progressView.setProgress(1.0f);
                            }
                            LoginActivitySmsView.this.destroyTimer();
                            if (LoginActivitySmsView.this.currentType == 3) {
                                AndroidUtilities.setWaitingForCall(false);
                                NotificationCenter.getGlobalInstance().removeObserver(LoginActivitySmsView.this, NotificationCenter.didReceiveCall);
                                boolean unused3 = LoginActivitySmsView.this.waitingForEvent = false;
                                LoginActivitySmsView.this.destroyCodeTimer();
                                LoginActivitySmsView.this.resendCode();
                            } else if (LoginActivitySmsView.this.currentType != 2 && LoginActivitySmsView.this.currentType != 4) {
                            } else {
                                if (LoginActivitySmsView.this.nextType == 4 || LoginActivitySmsView.this.nextType == 2) {
                                    if (LoginActivitySmsView.this.nextType == 4) {
                                        LoginActivitySmsView.this.timeText.setText(LocaleController.getString("Calling", NUM));
                                    } else {
                                        LoginActivitySmsView.this.timeText.setText(LocaleController.getString("SendingSms", NUM));
                                    }
                                    LoginActivitySmsView.this.createCodeTimer();
                                    TLRPC$TL_auth_resendCode tLRPC$TL_auth_resendCode = new TLRPC$TL_auth_resendCode();
                                    tLRPC$TL_auth_resendCode.phone_number = LoginActivitySmsView.this.requestPhone;
                                    tLRPC$TL_auth_resendCode.phone_code_hash = LoginActivitySmsView.this.phoneHash;
                                    ConnectionsManager.getInstance(LoginActivitySmsView.this.this$0.currentAccount).sendRequest(tLRPC$TL_auth_resendCode, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x01a1: INVOKE  
                                          (wrap: org.telegram.tgnet.ConnectionsManager : 0x0198: INVOKE  (r1v8 org.telegram.tgnet.ConnectionsManager) = 
                                          (wrap: int : 0x0194: INVOKE  (r1v7 int) = 
                                          (wrap: org.telegram.ui.ChangePhoneActivity : 0x0192: IGET  (r1v6 org.telegram.ui.ChangePhoneActivity) = 
                                          (wrap: org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView : 0x0190: IGET  (r1v5 org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView) = 
                                          (r9v0 'this' org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5 A[THIS])
                                         org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.5.this$1 org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView)
                                         org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this$0 org.telegram.ui.ChangePhoneActivity)
                                         org.telegram.ui.ChangePhoneActivity.access$4200(org.telegram.ui.ChangePhoneActivity):int type: STATIC)
                                         org.telegram.tgnet.ConnectionsManager.getInstance(int):org.telegram.tgnet.ConnectionsManager type: STATIC)
                                          (r0v16 'tLRPC$TL_auth_resendCode' org.telegram.tgnet.TLRPC$TL_auth_resendCode)
                                          (wrap: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$Jxe6tyQCg8333sUKrmOby2S-1BI : 0x019e: CONSTRUCTOR  (r2v6 org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$Jxe6tyQCg8333sUKrmOby2S-1BI) = 
                                          (r9v0 'this' org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5 A[THIS])
                                         call: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$Jxe6tyQCg8333sUKrmOby2S-1BI.<init>(org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5):void type: CONSTRUCTOR)
                                          (2 int)
                                         org.telegram.tgnet.ConnectionsManager.sendRequest(org.telegram.tgnet.TLObject, org.telegram.tgnet.RequestDelegate, int):int type: VIRTUAL in method: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.5.lambda$run$2():void, dex: classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                                        	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                        	at java.util.ArrayList.forEach(ArrayList.java:1259)
                                        	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                        	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                        	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                                        	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                                        	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                        	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                        	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                        	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                                        	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                        	at java.util.ArrayList.forEach(ArrayList.java:1259)
                                        	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                        	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                        	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                                        	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                                        	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                        	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                        	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                        	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                                        	at jadx.core.codegen.ClassGen.addInnerClass(ClassGen.java:249)
                                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:238)
                                        	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                        	at java.util.ArrayList.forEach(ArrayList.java:1259)
                                        	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                        	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                        	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                                        	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                                        	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                        	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                        	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                        	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x019e: CONSTRUCTOR  (r2v6 org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$Jxe6tyQCg8333sUKrmOby2S-1BI) = 
                                          (r9v0 'this' org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5 A[THIS])
                                         call: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$Jxe6tyQCg8333sUKrmOby2S-1BI.<init>(org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5):void type: CONSTRUCTOR in method: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.5.lambda$run$2():void, dex: classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                                        	... 99 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$Jxe6tyQCg8333sUKrmOby2S-1BI, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                                        	... 105 more
                                        */
                                    /*
                                        this = this;
                                        long r0 = java.lang.System.currentTimeMillis()
                                        double r0 = (double) r0
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r2 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        double r2 = r2.lastCurrentTime
                                        java.lang.Double.isNaN(r0)
                                        double r2 = r0 - r2
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r4 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r5 = r4.time
                                        double r5 = (double) r5
                                        java.lang.Double.isNaN(r5)
                                        double r5 = r5 - r2
                                        int r2 = (int) r5
                                        int unused = r4.time = r2
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r2 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        double unused = r2.lastCurrentTime = r0
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r0 = r0.time
                                        r1 = 1065353216(0x3var_, float:1.0)
                                        r2 = 3
                                        r3 = 1000(0x3e8, float:1.401E-42)
                                        r4 = 4
                                        r5 = 0
                                        r6 = 2
                                        if (r0 < r3) goto L_0x00c5
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r0 = r0.time
                                        int r0 = r0 / r3
                                        int r0 = r0 / 60
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r7 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r7 = r7.time
                                        int r7 = r7 / r3
                                        int r3 = r0 * 60
                                        int r7 = r7 - r3
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r3 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r3 = r3.nextType
                                        r8 = 1
                                        if (r3 == r4) goto L_0x0082
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r3 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r3 = r3.nextType
                                        if (r3 != r2) goto L_0x0059
                                        goto L_0x0082
                                    L_0x0059:
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r2 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r2 = r2.nextType
                                        if (r2 != r6) goto L_0x00a2
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r2 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        android.widget.TextView r2 = r2.timeText
                                        r3 = 2131627467(0x7f0e0dcb, float:1.88822E38)
                                        java.lang.Object[] r4 = new java.lang.Object[r6]
                                        java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
                                        r4[r5] = r0
                                        java.lang.Integer r0 = java.lang.Integer.valueOf(r7)
                                        r4[r8] = r0
                                        java.lang.String r0 = "SmsText"
                                        java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r3, r4)
                                        r2.setText(r0)
                                        goto L_0x00a2
                                    L_0x0082:
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r2 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        android.widget.TextView r2 = r2.timeText
                                        r3 = 2131624624(0x7f0e02b0, float:1.8876433E38)
                                        java.lang.Object[] r4 = new java.lang.Object[r6]
                                        java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
                                        r4[r5] = r0
                                        java.lang.Integer r0 = java.lang.Integer.valueOf(r7)
                                        r4[r8] = r0
                                        java.lang.String r0 = "CallText"
                                        java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r3, r4)
                                        r2.setText(r0)
                                    L_0x00a2:
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        org.telegram.ui.ChangePhoneActivity$ProgressView r0 = r0.progressView
                                        if (r0 == 0) goto L_0x01a4
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        org.telegram.ui.ChangePhoneActivity$ProgressView r0 = r0.progressView
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r2 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r2 = r2.time
                                        float r2 = (float) r2
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r3 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r3 = r3.timeout
                                        float r3 = (float) r3
                                        float r2 = r2 / r3
                                        float r1 = r1 - r2
                                        r0.setProgress(r1)
                                        goto L_0x01a4
                                    L_0x00c5:
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        org.telegram.ui.ChangePhoneActivity$ProgressView r0 = r0.progressView
                                        if (r0 == 0) goto L_0x00d6
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        org.telegram.ui.ChangePhoneActivity$ProgressView r0 = r0.progressView
                                        r0.setProgress(r1)
                                    L_0x00d6:
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        r0.destroyTimer()
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r0 = r0.currentType
                                        if (r0 != r2) goto L_0x0102
                                        org.telegram.messenger.AndroidUtilities.setWaitingForCall(r5)
                                        org.telegram.messenger.NotificationCenter r0 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r1 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r2 = org.telegram.messenger.NotificationCenter.didReceiveCall
                                        r0.removeObserver(r1, r2)
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        boolean unused = r0.waitingForEvent = r5
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        r0.destroyCodeTimer()
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        r0.resendCode()
                                        goto L_0x01a4
                                    L_0x0102:
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r0 = r0.currentType
                                        if (r0 == r6) goto L_0x0112
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r0 = r0.currentType
                                        if (r0 != r4) goto L_0x01a4
                                    L_0x0112:
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r0 = r0.nextType
                                        if (r0 == r4) goto L_0x0149
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r0 = r0.nextType
                                        if (r0 != r6) goto L_0x0123
                                        goto L_0x0149
                                    L_0x0123:
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r0 = r0.nextType
                                        if (r0 != r2) goto L_0x01a4
                                        org.telegram.messenger.AndroidUtilities.setWaitingForSms(r5)
                                        org.telegram.messenger.NotificationCenter r0 = org.telegram.messenger.NotificationCenter.getGlobalInstance()
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r1 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r2 = org.telegram.messenger.NotificationCenter.didReceiveSmsCode
                                        r0.removeObserver(r1, r2)
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        boolean unused = r0.waitingForEvent = r5
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        r0.destroyCodeTimer()
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        r0.resendCode()
                                        goto L_0x01a4
                                    L_0x0149:
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        int r0 = r0.nextType
                                        if (r0 != r4) goto L_0x0164
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        android.widget.TextView r0 = r0.timeText
                                        r1 = 2131624626(0x7f0e02b2, float:1.8876437E38)
                                        java.lang.String r2 = "Calling"
                                        java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                                        r0.setText(r1)
                                        goto L_0x0176
                                    L_0x0164:
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        android.widget.TextView r0 = r0.timeText
                                        r1 = 2131627329(0x7f0e0d41, float:1.888192E38)
                                        java.lang.String r2 = "SendingSms"
                                        java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                                        r0.setText(r1)
                                    L_0x0176:
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r0 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        r0.createCodeTimer()
                                        org.telegram.tgnet.TLRPC$TL_auth_resendCode r0 = new org.telegram.tgnet.TLRPC$TL_auth_resendCode
                                        r0.<init>()
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r1 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        java.lang.String r1 = r1.requestPhone
                                        r0.phone_number = r1
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r1 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        java.lang.String r1 = r1.phoneHash
                                        r0.phone_code_hash = r1
                                        org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView r1 = org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.this
                                        org.telegram.ui.ChangePhoneActivity r1 = r1.this$0
                                        int r1 = r1.currentAccount
                                        org.telegram.tgnet.ConnectionsManager r1 = org.telegram.tgnet.ConnectionsManager.getInstance(r1)
                                        org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$Jxe6tyQCg8333sUKrmOby2S-1BI r2 = new org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$Jxe6tyQCg8333sUKrmOby2S-1BI
                                        r2.<init>(r9)
                                        r1.sendRequest(r0, r2, r6)
                                    L_0x01a4:
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.AnonymousClass5.lambda$run$2$ChangePhoneActivity$LoginActivitySmsView$5():void");
                                }

                                /* access modifiers changed from: private */
                                /* renamed from: lambda$null$1 */
                                public /* synthetic */ void lambda$null$1$ChangePhoneActivity$LoginActivitySmsView$5(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                                    if (tLRPC$TL_error != null && tLRPC$TL_error.text != null) {
                                        AndroidUtilities.runOnUIThread(
                                        /*  JADX ERROR: Method code generation error
                                            jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000b: INVOKE  
                                              (wrap: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$AdN2UX4yVZLQY2Lw1twdvDLbPmY : 0x0008: CONSTRUCTOR  (r1v2 org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$AdN2UX4yVZLQY2Lw1twdvDLbPmY) = 
                                              (r0v0 'this' org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5 A[THIS])
                                              (r2v0 'tLRPC$TL_error' org.telegram.tgnet.TLRPC$TL_error)
                                             call: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$AdN2UX4yVZLQY2Lw1twdvDLbPmY.<init>(org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5, org.telegram.tgnet.TLRPC$TL_error):void type: CONSTRUCTOR)
                                             org.telegram.messenger.AndroidUtilities.runOnUIThread(java.lang.Runnable):void type: STATIC in method: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.5.lambda$null$1(org.telegram.tgnet.TLObject, org.telegram.tgnet.TLRPC$TL_error):void, dex: classes.dex
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                                            	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                            	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                            	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                            	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                            	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                            	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                                            	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                                            	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                            	at java.util.ArrayList.forEach(ArrayList.java:1259)
                                            	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                            	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                            	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                                            	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                                            	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                            	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                            	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                                            	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                            	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                                            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                                            	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                                            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                                            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                                            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                                            	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                            	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                            	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                            	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                            	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                            	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                                            	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                                            	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                            	at java.util.ArrayList.forEach(ArrayList.java:1259)
                                            	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                            	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                            	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                                            	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                                            	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                            	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                            	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                                            	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                            	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                                            	at jadx.core.codegen.ClassGen.addInnerClass(ClassGen.java:249)
                                            	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:238)
                                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                            	at java.util.ArrayList.forEach(ArrayList.java:1259)
                                            	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                            	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                            	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                                            	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                                            	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                            	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                            	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                            	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                                            	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                            	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                                            	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                                            	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                            	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                            	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r1v2 org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$AdN2UX4yVZLQY2Lw1twdvDLbPmY) = 
                                              (r0v0 'this' org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5 A[THIS])
                                              (r2v0 'tLRPC$TL_error' org.telegram.tgnet.TLRPC$TL_error)
                                             call: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$AdN2UX4yVZLQY2Lw1twdvDLbPmY.<init>(org.telegram.ui.ChangePhoneActivity$LoginActivitySmsView$5, org.telegram.tgnet.TLRPC$TL_error):void type: CONSTRUCTOR in method: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.5.lambda$null$1(org.telegram.tgnet.TLObject, org.telegram.tgnet.TLRPC$TL_error):void, dex: classes.dex
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                                            	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                                            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                                            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                                            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                                            	... 90 more
                                            Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$AdN2UX4yVZLQY2Lw1twdvDLbPmY, state: NOT_LOADED
                                            	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                                            	... 96 more
                                            */
                                        /*
                                            this = this;
                                            if (r2 == 0) goto L_0x000e
                                            java.lang.String r1 = r2.text
                                            if (r1 == 0) goto L_0x000e
                                            org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$AdN2UX4yVZLQY2Lw1twdvDLbPmY r1 = new org.telegram.ui.-$$Lambda$ChangePhoneActivity$LoginActivitySmsView$5$AdN2UX4yVZLQY2Lw1twdvDLbPmY
                                            r1.<init>(r0, r2)
                                            org.telegram.messenger.AndroidUtilities.runOnUIThread(r1)
                                        L_0x000e:
                                            return
                                        */
                                        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChangePhoneActivity.LoginActivitySmsView.AnonymousClass5.lambda$null$1$ChangePhoneActivity$LoginActivitySmsView$5(org.telegram.tgnet.TLObject, org.telegram.tgnet.TLRPC$TL_error):void");
                                    }

                                    /* access modifiers changed from: private */
                                    /* renamed from: lambda$null$0 */
                                    public /* synthetic */ void lambda$null$0$ChangePhoneActivity$LoginActivitySmsView$5(TLRPC$TL_error tLRPC$TL_error) {
                                        String unused = LoginActivitySmsView.this.lastError = tLRPC$TL_error.text;
                                    }
                                }, 0, 1000);
                            }
                        }

                        /* access modifiers changed from: private */
                        public void destroyTimer() {
                            try {
                                synchronized (this.timerSync) {
                                    Timer timer = this.timeTimer;
                                    if (timer != null) {
                                        timer.cancel();
                                        this.timeTimer = null;
                                    }
                                }
                            } catch (Exception e) {
                                FileLog.e((Throwable) e);
                            }
                        }

                        /* access modifiers changed from: private */
                        public String getCode() {
                            if (this.codeField == null) {
                                return "";
                            }
                            StringBuilder sb = new StringBuilder();
                            int i = 0;
                            while (true) {
                                EditTextBoldCursor[] editTextBoldCursorArr = this.codeField;
                                if (i >= editTextBoldCursorArr.length) {
                                    return sb.toString();
                                }
                                sb.append(PhoneFormat.stripExceptNumbers(editTextBoldCursorArr[i].getText().toString()));
                                i++;
                            }
                        }

                        public void onNextPressed() {
                            if (!this.nextPressed) {
                                String code = getCode();
                                if (TextUtils.isEmpty(code)) {
                                    AndroidUtilities.shakeView(this.codeFieldContainer, 2.0f, 0);
                                    return;
                                }
                                this.nextPressed = true;
                                int i = this.currentType;
                                if (i == 2) {
                                    AndroidUtilities.setWaitingForSms(false);
                                    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
                                } else if (i == 3) {
                                    AndroidUtilities.setWaitingForCall(false);
                                    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveCall);
                                }
                                this.waitingForEvent = false;
                                TLRPC$TL_account_changePhone tLRPC$TL_account_changePhone = new TLRPC$TL_account_changePhone();
                                tLRPC$TL_account_changePhone.phone_number = this.requestPhone;
                                tLRPC$TL_account_changePhone.phone_code = code;
                                tLRPC$TL_account_changePhone.phone_code_hash = this.phoneHash;
                                destroyTimer();
                                this.this$0.needShowProgress();
                                ConnectionsManager.getInstance(this.this$0.currentAccount).sendRequest(tLRPC$TL_account_changePhone, new RequestDelegate(tLRPC$TL_account_changePhone) {
                                    public final /* synthetic */ TLRPC$TL_account_changePhone f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                                        ChangePhoneActivity.LoginActivitySmsView.this.lambda$onNextPressed$7$ChangePhoneActivity$LoginActivitySmsView(this.f$1, tLObject, tLRPC$TL_error);
                                    }
                                }, 2);
                            }
                        }

                        /* access modifiers changed from: private */
                        /* renamed from: lambda$onNextPressed$7 */
                        public /* synthetic */ void lambda$onNextPressed$7$ChangePhoneActivity$LoginActivitySmsView(TLRPC$TL_account_changePhone tLRPC$TL_account_changePhone, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                            AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_error, tLObject, tLRPC$TL_account_changePhone) {
                                public final /* synthetic */ TLRPC$TL_error f$1;
                                public final /* synthetic */ TLObject f$2;
                                public final /* synthetic */ TLRPC$TL_account_changePhone f$3;

                                {
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                    this.f$3 = r4;
                                }

                                public final void run() {
                                    ChangePhoneActivity.LoginActivitySmsView.this.lambda$null$6$ChangePhoneActivity$LoginActivitySmsView(this.f$1, this.f$2, this.f$3);
                                }
                            });
                        }

                        /* access modifiers changed from: private */
                        /* renamed from: lambda$null$6 */
                        public /* synthetic */ void lambda$null$6$ChangePhoneActivity$LoginActivitySmsView(TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, TLRPC$TL_account_changePhone tLRPC$TL_account_changePhone) {
                            int i;
                            int i2;
                            this.this$0.needHideProgress();
                            this.nextPressed = false;
                            if (tLRPC$TL_error == null) {
                                TLRPC$User tLRPC$User = (TLRPC$User) tLObject;
                                destroyTimer();
                                destroyCodeTimer();
                                UserConfig.getInstance(this.this$0.currentAccount).setCurrentUser(tLRPC$User);
                                UserConfig.getInstance(this.this$0.currentAccount).saveConfig(true);
                                ArrayList arrayList = new ArrayList();
                                arrayList.add(tLRPC$User);
                                MessagesStorage.getInstance(this.this$0.currentAccount).putUsersAndChats(arrayList, (ArrayList<TLRPC$Chat>) null, true, true);
                                MessagesController.getInstance(this.this$0.currentAccount).putUser(tLRPC$User, false);
                                this.this$0.finishFragment();
                                NotificationCenter.getInstance(this.this$0.currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged, new Object[0]);
                                return;
                            }
                            this.lastError = tLRPC$TL_error.text;
                            int i3 = this.currentType;
                            if ((i3 == 3 && ((i2 = this.nextType) == 4 || i2 == 2)) || ((i3 == 2 && ((i = this.nextType) == 4 || i == 3)) || (i3 == 4 && this.nextType == 2))) {
                                createTimer();
                            }
                            int i4 = this.currentType;
                            if (i4 == 2) {
                                AndroidUtilities.setWaitingForSms(true);
                                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didReceiveSmsCode);
                            } else if (i4 == 3) {
                                AndroidUtilities.setWaitingForCall(true);
                                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didReceiveCall);
                            }
                            this.waitingForEvent = true;
                            if (this.currentType != 3) {
                                AlertsCreator.processError(this.this$0.currentAccount, tLRPC$TL_error, this.this$0, tLRPC$TL_account_changePhone, new Object[0]);
                            }
                            if (tLRPC$TL_error.text.contains("PHONE_CODE_EMPTY") || tLRPC$TL_error.text.contains("PHONE_CODE_INVALID")) {
                                int i5 = 0;
                                while (true) {
                                    EditTextBoldCursor[] editTextBoldCursorArr = this.codeField;
                                    if (i5 < editTextBoldCursorArr.length) {
                                        editTextBoldCursorArr[i5].setText("");
                                        i5++;
                                    } else {
                                        editTextBoldCursorArr[0].requestFocus();
                                        return;
                                    }
                                }
                            } else if (tLRPC$TL_error.text.contains("PHONE_CODE_EXPIRED")) {
                                onBackPressed(true);
                                this.this$0.setPage(0, true, (Bundle) null, true);
                            }
                        }

                        public boolean onBackPressed(boolean z) {
                            if (!z) {
                                AlertDialog.Builder builder = new AlertDialog.Builder((Context) this.this$0.getParentActivity());
                                builder.setTitle(LocaleController.getString("AppName", NUM));
                                builder.setMessage(LocaleController.getString("StopVerification", NUM));
                                builder.setPositiveButton(LocaleController.getString("Continue", NUM), (DialogInterface.OnClickListener) null);
                                builder.setNegativeButton(LocaleController.getString("Stop", NUM), new DialogInterface.OnClickListener() {
                                    public final void onClick(DialogInterface dialogInterface, int i) {
                                        ChangePhoneActivity.LoginActivitySmsView.this.lambda$onBackPressed$8$ChangePhoneActivity$LoginActivitySmsView(dialogInterface, i);
                                    }
                                });
                                this.this$0.showDialog(builder.create());
                                return false;
                            }
                            TLRPC$TL_auth_cancelCode tLRPC$TL_auth_cancelCode = new TLRPC$TL_auth_cancelCode();
                            tLRPC$TL_auth_cancelCode.phone_number = this.requestPhone;
                            tLRPC$TL_auth_cancelCode.phone_code_hash = this.phoneHash;
                            ConnectionsManager.getInstance(this.this$0.currentAccount).sendRequest(tLRPC$TL_auth_cancelCode, $$Lambda$ChangePhoneActivity$LoginActivitySmsView$DpKFSSONgaqZsHycY3TjQoH5Q.INSTANCE, 10);
                            destroyTimer();
                            destroyCodeTimer();
                            int i = this.currentType;
                            if (i == 2) {
                                AndroidUtilities.setWaitingForSms(false);
                                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
                            } else if (i == 3) {
                                AndroidUtilities.setWaitingForCall(false);
                                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveCall);
                            }
                            this.waitingForEvent = false;
                            return true;
                        }

                        /* access modifiers changed from: private */
                        /* renamed from: lambda$onBackPressed$8 */
                        public /* synthetic */ void lambda$onBackPressed$8$ChangePhoneActivity$LoginActivitySmsView(DialogInterface dialogInterface, int i) {
                            onBackPressed(true);
                            this.this$0.setPage(0, true, (Bundle) null, true);
                        }

                        public void onDestroyActivity() {
                            super.onDestroyActivity();
                            int i = this.currentType;
                            if (i == 2) {
                                AndroidUtilities.setWaitingForSms(false);
                                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
                            } else if (i == 3) {
                                AndroidUtilities.setWaitingForCall(false);
                                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveCall);
                            }
                            this.waitingForEvent = false;
                            destroyTimer();
                            destroyCodeTimer();
                        }

                        public void onShow() {
                            super.onShow();
                            if (this.currentType != 3) {
                                RLottieDrawable rLottieDrawable = this.hintDrawable;
                                if (rLottieDrawable != null) {
                                    rLottieDrawable.setCurrentFrame(0);
                                }
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public final void run() {
                                        ChangePhoneActivity.LoginActivitySmsView.this.lambda$onShow$10$ChangePhoneActivity$LoginActivitySmsView();
                                    }
                                }, 100);
                            }
                        }

                        /* access modifiers changed from: private */
                        /* renamed from: lambda$onShow$10 */
                        public /* synthetic */ void lambda$onShow$10$ChangePhoneActivity$LoginActivitySmsView() {
                            EditTextBoldCursor[] editTextBoldCursorArr = this.codeField;
                            if (editTextBoldCursorArr != null) {
                                int length2 = editTextBoldCursorArr.length - 1;
                                while (true) {
                                    if (length2 < 0) {
                                        break;
                                    } else if (length2 == 0 || this.codeField[length2].length() != 0) {
                                        this.codeField[length2].requestFocus();
                                        EditTextBoldCursor[] editTextBoldCursorArr2 = this.codeField;
                                        editTextBoldCursorArr2[length2].setSelection(editTextBoldCursorArr2[length2].length());
                                        AndroidUtilities.showKeyboard(this.codeField[length2]);
                                    } else {
                                        length2--;
                                    }
                                }
                                this.codeField[length2].requestFocus();
                                EditTextBoldCursor[] editTextBoldCursorArr22 = this.codeField;
                                editTextBoldCursorArr22[length2].setSelection(editTextBoldCursorArr22[length2].length());
                                AndroidUtilities.showKeyboard(this.codeField[length2]);
                            }
                            RLottieDrawable rLottieDrawable = this.hintDrawable;
                            if (rLottieDrawable != null) {
                                rLottieDrawable.start();
                            }
                        }

                        public void didReceivedNotification(int i, int i2, Object... objArr) {
                            EditTextBoldCursor[] editTextBoldCursorArr;
                            if (this.waitingForEvent && (editTextBoldCursorArr = this.codeField) != null) {
                                if (i == NotificationCenter.didReceiveSmsCode) {
                                    editTextBoldCursorArr[0].setText("" + objArr[0]);
                                    onNextPressed();
                                } else if (i == NotificationCenter.didReceiveCall) {
                                    String str = "" + objArr[0];
                                    if (AndroidUtilities.checkPhonePattern(this.pattern, str)) {
                                        this.ignoreOnTextChange = true;
                                        this.codeField[0].setText(str);
                                        this.ignoreOnTextChange = false;
                                        onNextPressed();
                                    }
                                }
                            }
                        }
                    }

                    public ArrayList<ThemeDescription> getThemeDescriptions() {
                        SlideView[] slideViewArr = this.views;
                        PhoneView phoneView = (PhoneView) slideViewArr[0];
                        LoginActivitySmsView loginActivitySmsView = (LoginActivitySmsView) slideViewArr[1];
                        LoginActivitySmsView loginActivitySmsView2 = (LoginActivitySmsView) slideViewArr[2];
                        LoginActivitySmsView loginActivitySmsView3 = (LoginActivitySmsView) slideViewArr[3];
                        LoginActivitySmsView loginActivitySmsView4 = (LoginActivitySmsView) slideViewArr[4];
                        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
                        $$Lambda$ChangePhoneActivity$vYXfF2LbQ54DHqseO18Kk1JC_aE r15 = new ThemeDescription.ThemeDescriptionDelegate() {
                            public final void didSetColor() {
                                ChangePhoneActivity.this.lambda$getThemeDescriptions$0$ChangePhoneActivity();
                            }
                        };
                        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
                        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefault"));
                        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_LISTGLOWCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefault"));
                        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultIcon"));
                        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultTitle"));
                        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSelector"));
                        arrayList.add(new ThemeDescription(phoneView.countryButton, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        arrayList.add(new ThemeDescription(phoneView.countryButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "listSelectorSDK21"));
                        arrayList.add(new ThemeDescription(phoneView.view, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayLine"));
                        arrayList.add(new ThemeDescription(phoneView.textView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        arrayList.add(new ThemeDescription(phoneView.codeField, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        arrayList.add(new ThemeDescription(phoneView.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteInputField"));
                        arrayList.add(new ThemeDescription(phoneView.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteInputFieldActivated"));
                        arrayList.add(new ThemeDescription(phoneView.phoneField, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        arrayList.add(new ThemeDescription(phoneView.phoneField, ThemeDescription.FLAG_HINTTEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteHintText"));
                        arrayList.add(new ThemeDescription(phoneView.phoneField, ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteInputField"));
                        arrayList.add(new ThemeDescription(phoneView.phoneField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteInputFieldActivated"));
                        arrayList.add(new ThemeDescription(phoneView.textView2, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText6"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView.confirmTextView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText6"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView.titleTextView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        if (loginActivitySmsView.codeField != null) {
                            for (int i = 0; i < loginActivitySmsView.codeField.length; i++) {
                                arrayList.add(new ThemeDescription(loginActivitySmsView.codeField[i], ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                                arrayList.add(new ThemeDescription(loginActivitySmsView.codeField[i], ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteInputFieldActivated"));
                            }
                        }
                        arrayList.add(new ThemeDescription(loginActivitySmsView.timeText, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText6"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView.problemText, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueText4"));
                        arrayList.add(new ThemeDescription((View) loginActivitySmsView.progressView, 0, new Class[]{ProgressView.class}, new String[]{"paint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "login_progressInner"));
                        arrayList.add(new ThemeDescription((View) loginActivitySmsView.progressView, 0, new Class[]{ProgressView.class}, new String[]{"paint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "login_progressOuter"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView.blackImageView, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView.blueImageView, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_actionBackground"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView2.confirmTextView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText6"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView2.titleTextView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        if (loginActivitySmsView2.codeField != null) {
                            for (int i2 = 0; i2 < loginActivitySmsView2.codeField.length; i2++) {
                                arrayList.add(new ThemeDescription(loginActivitySmsView2.codeField[i2], ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                                arrayList.add(new ThemeDescription(loginActivitySmsView2.codeField[i2], ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteInputFieldActivated"));
                            }
                        }
                        arrayList.add(new ThemeDescription(loginActivitySmsView2.timeText, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText6"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView2.problemText, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueText4"));
                        arrayList.add(new ThemeDescription((View) loginActivitySmsView2.progressView, 0, new Class[]{ProgressView.class}, new String[]{"paint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "login_progressInner"));
                        arrayList.add(new ThemeDescription((View) loginActivitySmsView2.progressView, 0, new Class[]{ProgressView.class}, new String[]{"paint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "login_progressOuter"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView2.blackImageView, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView2.blueImageView, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_actionBackground"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView3.confirmTextView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText6"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView3.titleTextView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        if (loginActivitySmsView3.codeField != null) {
                            for (int i3 = 0; i3 < loginActivitySmsView3.codeField.length; i3++) {
                                arrayList.add(new ThemeDescription(loginActivitySmsView3.codeField[i3], ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                                arrayList.add(new ThemeDescription(loginActivitySmsView3.codeField[i3], ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteInputFieldActivated"));
                            }
                        }
                        arrayList.add(new ThemeDescription(loginActivitySmsView3.timeText, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText6"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView3.problemText, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueText4"));
                        arrayList.add(new ThemeDescription((View) loginActivitySmsView3.progressView, 0, new Class[]{ProgressView.class}, new String[]{"paint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "login_progressInner"));
                        arrayList.add(new ThemeDescription((View) loginActivitySmsView3.progressView, 0, new Class[]{ProgressView.class}, new String[]{"paint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "login_progressOuter"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView3.blackImageView, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView3.blueImageView, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_actionBackground"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView4.confirmTextView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText6"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView4.titleTextView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        if (loginActivitySmsView4.codeField != null) {
                            for (int i4 = 0; i4 < loginActivitySmsView4.codeField.length; i4++) {
                                arrayList.add(new ThemeDescription(loginActivitySmsView4.codeField[i4], ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                                arrayList.add(new ThemeDescription(loginActivitySmsView4.codeField[i4], ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteInputFieldActivated"));
                            }
                        }
                        arrayList.add(new ThemeDescription(loginActivitySmsView4.timeText, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText6"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView4.problemText, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueText4"));
                        arrayList.add(new ThemeDescription((View) loginActivitySmsView4.progressView, 0, new Class[]{ProgressView.class}, new String[]{"paint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "login_progressInner"));
                        arrayList.add(new ThemeDescription((View) loginActivitySmsView4.progressView, 0, new Class[]{ProgressView.class}, new String[]{"paint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "login_progressOuter"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView4.blackImageView, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView4.blueImageView, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_actionBackground"));
                        arrayList.add(new ThemeDescription(loginActivitySmsView4.blueImageView, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r15, "chats_actionBackground"));
                        return arrayList;
                    }

                    /* access modifiers changed from: private */
                    /* JADX WARNING: Code restructure failed: missing block: B:5:0x000c, code lost:
                        r1 = (org.telegram.ui.LoginActivity.LoginActivitySmsView) r1[r0];
                     */
                    /* renamed from: lambda$getThemeDescriptions$0 */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public /* synthetic */ void lambda$getThemeDescriptions$0$ChangePhoneActivity() {
                        /*
                            r6 = this;
                            r0 = 0
                        L_0x0001:
                            org.telegram.ui.Components.SlideView[] r1 = r6.views
                            int r2 = r1.length
                            if (r0 >= r2) goto L_0x002d
                            r2 = r1[r0]
                            boolean r2 = r2 instanceof org.telegram.ui.LoginActivity.LoginActivitySmsView
                            if (r2 == 0) goto L_0x002a
                            r1 = r1[r0]
                            org.telegram.ui.LoginActivity$LoginActivitySmsView r1 = (org.telegram.ui.LoginActivity.LoginActivitySmsView) r1
                            org.telegram.ui.Components.RLottieDrawable r2 = r1.hintDrawable
                            if (r2 == 0) goto L_0x002a
                            java.lang.String r3 = "chats_actionBackground"
                            int r4 = org.telegram.ui.ActionBar.Theme.getColor(r3)
                            java.lang.String r5 = "Bubble.**"
                            r2.setLayerColor(r5, r4)
                            org.telegram.ui.Components.RLottieDrawable r1 = r1.hintDrawable
                            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r3)
                            java.lang.String r3 = "Phone.**"
                            r1.setLayerColor(r3, r2)
                        L_0x002a:
                            int r0 = r0 + 1
                            goto L_0x0001
                        L_0x002d:
                            return
                        */
                        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChangePhoneActivity.lambda$getThemeDescriptions$0$ChangePhoneActivity():void");
                    }
                }
