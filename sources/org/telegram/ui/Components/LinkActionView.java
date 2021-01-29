package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_chatInviteExported;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_inputUserEmpty;
import org.telegram.tgnet.TLRPC$TL_messages_chatInviteImporters;
import org.telegram.tgnet.TLRPC$TL_messages_getChatInviteImporters;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;

public class LinkActionView extends LinearLayout {
    /* access modifiers changed from: private */
    public ActionBarPopupWindow actionBarPopupWindow;
    private final AvatarsContainer avatarsContainer;
    /* access modifiers changed from: private */
    public final TextView copyView;
    private Delegate delegate;
    BaseFragment fragment;
    /* access modifiers changed from: private */
    public final FrameLayout frameLayout;
    private boolean isPublic;
    String link;
    TextView linkView;
    boolean loadingImporters;
    ImageView optionsView;
    float[] point = new float[2];
    /* access modifiers changed from: private */
    public QRCodeBottomSheet qrCodeBottomSheet;
    private final TextView removeView;
    private final TextView shareView;
    /* access modifiers changed from: private */
    public int usersCount;

    public interface Delegate {

        /* renamed from: org.telegram.ui.Components.LinkActionView$Delegate$-CC  reason: invalid class name */
        public final /* synthetic */ class CC {
            public static void $default$editLink(Delegate delegate) {
            }

            public static void $default$removeLink(Delegate delegate) {
            }

            public static void $default$showUsersForPermanentLink(Delegate delegate) {
            }
        }

        void editLink();

        void removeLink();

        void revokeLink();

        void showUsersForPermanentLink();
    }

    public void setPermanent(boolean z) {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public LinkActionView(Context context, BaseFragment baseFragment, BottomSheet bottomSheet, int i, boolean z) {
        super(context);
        Context context2 = context;
        BaseFragment baseFragment2 = baseFragment;
        this.fragment = baseFragment2;
        setOrientation(1);
        FrameLayout frameLayout2 = new FrameLayout(context2);
        this.frameLayout = frameLayout2;
        TextView textView = new TextView(context2);
        this.linkView = textView;
        textView.setPadding(AndroidUtilities.dp(20.0f), AndroidUtilities.dp(18.0f), AndroidUtilities.dp(40.0f), AndroidUtilities.dp(18.0f));
        this.linkView.setTextSize(1, 16.0f);
        frameLayout2.addView(this.linkView);
        ImageView imageView = new ImageView(context2);
        this.optionsView = imageView;
        imageView.setImageDrawable(ContextCompat.getDrawable(context2, NUM));
        this.optionsView.setScaleType(ImageView.ScaleType.CENTER);
        frameLayout2.addView(this.optionsView, LayoutHelper.createFrame(40, 48, 21));
        addView(frameLayout2, LayoutHelper.createLinear(-1, -2, 0, 4, 0, 4, 0));
        LinearLayout linearLayout = new LinearLayout(context2);
        linearLayout.setOrientation(0);
        TextView textView2 = new TextView(context2);
        this.copyView = textView2;
        textView2.setGravity(1);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append("..").setSpan(new ColoredImageSpan(ContextCompat.getDrawable(context2, NUM)), 0, 1, 0);
        spannableStringBuilder.setSpan(new DialogCell.FixedWidthSpan(AndroidUtilities.dp(8.0f)), 1, 2, 0);
        spannableStringBuilder.append(LocaleController.getString("CopyLink", NUM));
        spannableStringBuilder.append(".").setSpan(new DialogCell.FixedWidthSpan(AndroidUtilities.dp(5.0f)), spannableStringBuilder.length() - 1, spannableStringBuilder.length(), 0);
        textView2.setText(spannableStringBuilder);
        textView2.setPadding(AndroidUtilities.dp(10.0f), AndroidUtilities.dp(10.0f), AndroidUtilities.dp(10.0f), AndroidUtilities.dp(10.0f));
        textView2.setTextSize(1, 14.0f);
        textView2.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView2.setLines(1);
        linearLayout.addView(textView2, LayoutHelper.createLinear(0, 40, 1.0f, 0, 4, 0, 4, 0));
        TextView textView3 = new TextView(context2);
        this.shareView = textView3;
        textView3.setGravity(1);
        SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder();
        spannableStringBuilder2.append("..").setSpan(new ColoredImageSpan(ContextCompat.getDrawable(context2, NUM)), 0, 1, 0);
        spannableStringBuilder2.setSpan(new DialogCell.FixedWidthSpan(AndroidUtilities.dp(8.0f)), 1, 2, 0);
        spannableStringBuilder2.append(LocaleController.getString("ShareLink", NUM));
        spannableStringBuilder2.append(".").setSpan(new DialogCell.FixedWidthSpan(AndroidUtilities.dp(5.0f)), spannableStringBuilder2.length() - 1, spannableStringBuilder2.length(), 0);
        textView3.setText(spannableStringBuilder2);
        textView3.setPadding(AndroidUtilities.dp(10.0f), AndroidUtilities.dp(10.0f), AndroidUtilities.dp(10.0f), AndroidUtilities.dp(10.0f));
        textView3.setTextSize(1, 14.0f);
        textView3.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView3.setLines(1);
        linearLayout.addView(textView3, LayoutHelper.createLinear(0, 40, 1.0f, 4, 0, 4, 0));
        TextView textView4 = new TextView(context2);
        this.removeView = textView4;
        textView4.setGravity(1);
        SpannableStringBuilder spannableStringBuilder3 = new SpannableStringBuilder();
        spannableStringBuilder3.append("..").setSpan(new ColoredImageSpan(ContextCompat.getDrawable(context2, NUM)), 0, 1, 0);
        spannableStringBuilder3.setSpan(new DialogCell.FixedWidthSpan(AndroidUtilities.dp(8.0f)), 1, 2, 0);
        spannableStringBuilder3.append(LocaleController.getString("DeleteLink", NUM));
        spannableStringBuilder3.append(".").setSpan(new DialogCell.FixedWidthSpan(AndroidUtilities.dp(5.0f)), spannableStringBuilder3.length() - 1, spannableStringBuilder3.length(), 0);
        textView4.setText(spannableStringBuilder3);
        textView4.setPadding(AndroidUtilities.dp(10.0f), AndroidUtilities.dp(10.0f), AndroidUtilities.dp(10.0f), AndroidUtilities.dp(10.0f));
        textView4.setTextSize(1, 14.0f);
        textView4.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView4.setLines(1);
        linearLayout.addView(textView4, LayoutHelper.createLinear(0, -2, 1.0f, 4, 0, 4, 0));
        textView4.setVisibility(8);
        addView(linearLayout, LayoutHelper.createLinear(-1, -2, 0.0f, 20.0f, 0.0f, 0.0f));
        AvatarsContainer avatarsContainer2 = new AvatarsContainer(context2);
        this.avatarsContainer = avatarsContainer2;
        addView(avatarsContainer2, LayoutHelper.createLinear(-1, 44, 0.0f, 12.0f, 0.0f, 0.0f));
        textView2.setOnClickListener(new View.OnClickListener(bottomSheet, baseFragment2) {
            public final /* synthetic */ BottomSheet f$1;
            public final /* synthetic */ BaseFragment f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onClick(View view) {
                LinkActionView.this.lambda$new$0$LinkActionView(this.f$1, this.f$2, view);
            }
        });
        if (z) {
            avatarsContainer2.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    LinkActionView.this.lambda$new$1$LinkActionView(view);
                }
            });
        }
        textView3.setOnClickListener(new View.OnClickListener(baseFragment2) {
            public final /* synthetic */ BaseFragment f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                LinkActionView.this.lambda$new$2$LinkActionView(this.f$1, view);
            }
        });
        textView4.setOnClickListener(new View.OnClickListener(baseFragment2) {
            public final /* synthetic */ BaseFragment f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                LinkActionView.this.lambda$new$4$LinkActionView(this.f$1, view);
            }
        });
        this.optionsView.setOnClickListener(new View.OnClickListener(context, z, bottomSheet, baseFragment) {
            public final /* synthetic */ Context f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ BottomSheet f$3;
            public final /* synthetic */ BaseFragment f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void onClick(View view) {
                LinkActionView.this.lambda$new$8$LinkActionView(this.f$1, this.f$2, this.f$3, this.f$4, view);
            }
        });
        frameLayout2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LinkActionView.this.copyView.callOnClick();
            }
        });
        updateColors();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$LinkActionView(BottomSheet bottomSheet, BaseFragment baseFragment, View view) {
        try {
            if (this.link != null) {
                ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", this.link));
                if (bottomSheet == null || bottomSheet.getContainer() == null) {
                    BulletinFactory.createCopyLinkBulletin(baseFragment).show();
                } else {
                    BulletinFactory.createCopyLinkBulletin(bottomSheet.getContainer()).show();
                }
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$LinkActionView(View view) {
        this.delegate.showUsersForPermanentLink();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$LinkActionView(BaseFragment baseFragment, View view) {
        try {
            if (this.link != null) {
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("text/plain");
                intent.putExtra("android.intent.extra.TEXT", this.link);
                baseFragment.startActivityForResult(Intent.createChooser(intent, LocaleController.getString("InviteToGroupByLink", NUM)), 500);
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$4 */
    public /* synthetic */ void lambda$new$4$LinkActionView(BaseFragment baseFragment, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder((Context) baseFragment.getParentActivity());
        builder.setTitle(LocaleController.getString("DeleteLink", NUM));
        builder.setMessage(LocaleController.getString("DeleteLinkHelp", NUM));
        builder.setPositiveButton(LocaleController.getString("Delete", NUM), new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                LinkActionView.this.lambda$null$3$LinkActionView(dialogInterface, i);
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
        baseFragment.showDialog(builder.create());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$3 */
    public /* synthetic */ void lambda$null$3$LinkActionView(DialogInterface dialogInterface, int i) {
        Delegate delegate2 = this.delegate;
        if (delegate2 != null) {
            delegate2.removeLink();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$8 */
    public /* synthetic */ void lambda$new$8$LinkActionView(Context context, boolean z, BottomSheet bottomSheet, BaseFragment baseFragment, View view) {
        final FrameLayout frameLayout2;
        if (this.isPublic) {
            showQrCode();
        } else if (this.actionBarPopupWindow == null) {
            ActionBarPopupWindow.ActionBarPopupWindowLayout actionBarPopupWindowLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(context);
            if (!z) {
                ActionBarMenuSubItem actionBarMenuSubItem = new ActionBarMenuSubItem(context, true, false);
                actionBarMenuSubItem.setTextAndIcon(LocaleController.getString("Edit", NUM), NUM);
                actionBarPopupWindowLayout.addView(actionBarMenuSubItem, LayoutHelper.createLinear(-1, 48));
                actionBarMenuSubItem.setOnClickListener(new View.OnClickListener() {
                    public final void onClick(View view) {
                        LinkActionView.this.lambda$null$5$LinkActionView(view);
                    }
                });
            }
            ActionBarMenuSubItem actionBarMenuSubItem2 = new ActionBarMenuSubItem(context, false, true);
            actionBarMenuSubItem2.setTextAndIcon(LocaleController.getString("RevokeLink", NUM), NUM);
            actionBarMenuSubItem2.setColors(Theme.getColor("windowBackgroundWhiteRedText"), Theme.getColor("windowBackgroundWhiteRedText"));
            actionBarMenuSubItem2.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    LinkActionView.this.lambda$null$6$LinkActionView(view);
                }
            });
            actionBarPopupWindowLayout.addView(actionBarMenuSubItem2, LayoutHelper.createLinear(-1, 48));
            if (bottomSheet == null) {
                frameLayout2 = baseFragment.getParentLayout();
            } else {
                frameLayout2 = bottomSheet.getContainer();
            }
            if (frameLayout2 != null) {
                getPointOnScreen(this.frameLayout, frameLayout2, this.point);
                float f = this.point[1];
                final AnonymousClass1 r5 = new View(context) {
                    /* access modifiers changed from: protected */
                    public void onDraw(Canvas canvas) {
                        canvas.drawColor(NUM);
                        LinkActionView linkActionView = LinkActionView.this;
                        linkActionView.getPointOnScreen(linkActionView.frameLayout, frameLayout2, LinkActionView.this.point);
                        canvas.save();
                        float[] fArr = LinkActionView.this.point;
                        canvas.translate(fArr[0], fArr[1]);
                        LinkActionView.this.frameLayout.draw(canvas);
                        canvas.restore();
                    }
                };
                frameLayout2.addView(r5, LayoutHelper.createFrame(-1, -1.0f));
                float f2 = 0.0f;
                r5.setAlpha(0.0f);
                r5.animate().alpha(1.0f).setDuration(150);
                actionBarPopupWindowLayout.measure(View.MeasureSpec.makeMeasureSpec(frameLayout2.getMeasuredWidth(), 0), View.MeasureSpec.makeMeasureSpec(frameLayout2.getMeasuredHeight(), 0));
                final FrameLayout frameLayout3 = frameLayout2;
                AnonymousClass2 r0 = new ActionBarPopupWindow(actionBarPopupWindowLayout, -2, -2) {
                    public void dismiss() {
                        super.dismiss();
                        ActionBarPopupWindow unused = LinkActionView.this.actionBarPopupWindow = null;
                        r5.animate().cancel();
                        r5.animate().alpha(0.0f).setDuration(150).setListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animator) {
                                if (r5.getParent() != null) {
                                    AnonymousClass2 r2 = AnonymousClass2.this;
                                    frameLayout3.removeView(r5);
                                }
                            }
                        });
                    }
                };
                this.actionBarPopupWindow = r0;
                r0.setOutsideTouchable(true);
                this.actionBarPopupWindow.setClippingEnabled(true);
                this.actionBarPopupWindow.setAnimationStyle(NUM);
                this.actionBarPopupWindow.setFocusable(true);
                this.actionBarPopupWindow.setInputMethodMode(2);
                this.actionBarPopupWindow.setSoftInputMode(0);
                this.actionBarPopupWindow.getContentView().setFocusableInTouchMode(true);
                actionBarPopupWindowLayout.setDispatchKeyEventListener(new ActionBarPopupWindow.OnDispatchKeyEventListener() {
                    public final void onDispatchKeyEvent(KeyEvent keyEvent) {
                        LinkActionView.this.lambda$null$7$LinkActionView(keyEvent);
                    }
                });
                if (AndroidUtilities.isTablet()) {
                    f += (float) frameLayout2.getPaddingTop();
                    f2 = 0.0f - ((float) frameLayout2.getPaddingLeft());
                }
                this.actionBarPopupWindow.showAtLocation(frameLayout2, 0, (int) (((float) ((frameLayout2.getMeasuredWidth() - actionBarPopupWindowLayout.getMeasuredWidth()) - AndroidUtilities.dp(16.0f))) + frameLayout2.getX() + f2), (int) (f + ((float) this.frameLayout.getMeasuredHeight()) + frameLayout2.getY()));
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$5 */
    public /* synthetic */ void lambda$null$5$LinkActionView(View view) {
        this.delegate.editLink();
        ActionBarPopupWindow actionBarPopupWindow2 = this.actionBarPopupWindow;
        if (actionBarPopupWindow2 != null) {
            actionBarPopupWindow2.dismiss();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$6 */
    public /* synthetic */ void lambda$null$6$LinkActionView(View view) {
        revokeLink();
        ActionBarPopupWindow actionBarPopupWindow2 = this.actionBarPopupWindow;
        if (actionBarPopupWindow2 != null) {
            actionBarPopupWindow2.dismiss();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$7 */
    public /* synthetic */ void lambda$null$7$LinkActionView(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == 4 && keyEvent.getRepeatCount() == 0 && this.actionBarPopupWindow.isShowing()) {
            this.actionBarPopupWindow.dismiss(true);
        }
    }

    /* access modifiers changed from: private */
    public void getPointOnScreen(FrameLayout frameLayout2, FrameLayout frameLayout3, float[] fArr) {
        float f = 0.0f;
        float f2 = 0.0f;
        View view = frameLayout2;
        while (view != frameLayout3) {
            f2 += view.getY();
            f += view.getX();
            if (view instanceof ScrollView) {
                f2 -= (float) view.getScrollY();
            }
            View view2 = (View) view.getParent();
            boolean z = view2 instanceof ViewGroup;
            view = view2;
            if (!z) {
                return;
            }
        }
        fArr[0] = f - ((float) frameLayout3.getPaddingLeft());
        fArr[1] = f2 - ((float) frameLayout3.getPaddingTop());
    }

    private void showQrCode() {
        AnonymousClass4 r0 = new QRCodeBottomSheet(getContext(), this.link) {
            public void dismiss() {
                super.dismiss();
                QRCodeBottomSheet unused = LinkActionView.this.qrCodeBottomSheet = null;
            }
        };
        this.qrCodeBottomSheet = r0;
        r0.show();
        ActionBarPopupWindow actionBarPopupWindow2 = this.actionBarPopupWindow;
        if (actionBarPopupWindow2 != null) {
            actionBarPopupWindow2.dismiss();
        }
    }

    public void updateColors() {
        this.copyView.setTextColor(Theme.getColor("featuredStickers_buttonText"));
        this.shareView.setTextColor(Theme.getColor("featuredStickers_buttonText"));
        this.removeView.setTextColor(Theme.getColor("featuredStickers_buttonText"));
        this.copyView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6.0f), Theme.getColor("featuredStickers_addButton"), Theme.getColor("featuredStickers_addButtonPressed")));
        this.shareView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6.0f), Theme.getColor("featuredStickers_addButton"), Theme.getColor("featuredStickers_addButtonPressed")));
        this.removeView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6.0f), Theme.getColor("chat_attachAudioBackground"), Theme.getColor("featuredStickers_addButtonPressed")));
        this.frameLayout.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6.0f), Theme.getColor("graySection"), ColorUtils.setAlphaComponent(Theme.getColor("listSelectorSDK21"), 76)));
        this.linkView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.optionsView.setColorFilter(Theme.getColor("dialogTextGray3"));
        this.avatarsContainer.countTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlueText"));
        this.avatarsContainer.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6.0f), 0, ColorUtils.setAlphaComponent(Theme.getColor("windowBackgroundWhiteBlueText"), 76)));
        QRCodeBottomSheet qRCodeBottomSheet = this.qrCodeBottomSheet;
        if (qRCodeBottomSheet != null) {
            qRCodeBottomSheet.updateColors();
        }
    }

    public void setLink(String str) {
        this.link = str;
        if (str == null) {
            this.linkView.setText(LocaleController.getString("Loading", NUM));
        } else if (str.startsWith("https://")) {
            this.linkView.setText(str.substring(8));
        } else {
            this.linkView.setText(str);
        }
    }

    public void setRevoke(boolean z) {
        if (z) {
            this.optionsView.setVisibility(8);
            this.shareView.setVisibility(8);
            this.copyView.setVisibility(8);
            this.removeView.setVisibility(0);
            return;
        }
        this.optionsView.setVisibility(0);
        this.shareView.setVisibility(0);
        this.copyView.setVisibility(0);
        this.removeView.setVisibility(8);
    }

    public void showOptions(boolean z) {
        this.optionsView.setVisibility(z ? 0 : 8);
    }

    public void setPublic(boolean z) {
        if (this.isPublic != z) {
            this.isPublic = z;
            if (z) {
                this.optionsView.setVisibility(8);
                ImageView imageView = this.optionsView;
                imageView.setImageDrawable(ContextCompat.getDrawable(imageView.getContext(), NUM));
                return;
            }
            this.optionsView.setVisibility(0);
            ImageView imageView2 = this.optionsView;
            imageView2.setImageDrawable(ContextCompat.getDrawable(imageView2.getContext(), NUM));
        }
    }

    private class AvatarsContainer extends FrameLayout {
        AvatarsImageView avatarsImageView;
        TextView countTextView;

        public AvatarsContainer(Context context) {
            super(context);
            this.avatarsImageView = new AvatarsImageView(context, LinkActionView.this) {
                /* access modifiers changed from: protected */
                public void onMeasure(int i, int i2) {
                    int min = Math.min(3, LinkActionView.this.usersCount);
                    super.onMeasure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp((float) (min == 0 ? 0 : ((min - 1) * 20) + 24 + 8)), NUM), i2);
                }
            };
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(0);
            addView(linearLayout, LayoutHelper.createFrame(-2, -1, 1));
            TextView textView = new TextView(context);
            this.countTextView = textView;
            textView.setTextSize(1, 14.0f);
            this.countTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            linearLayout.addView(this.avatarsImageView, LayoutHelper.createLinear(-2, -1));
            linearLayout.addView(this.countTextView, LayoutHelper.createLinear(-2, -2, 16));
            setPadding(0, AndroidUtilities.dp(8.0f), 0, AndroidUtilities.dp(8.0f));
            this.avatarsImageView.commitTransition(false);
        }
    }

    private void revokeLink() {
        AlertDialog.Builder builder = new AlertDialog.Builder((Context) this.fragment.getParentActivity());
        builder.setMessage(LocaleController.getString("RevokeAlert", NUM));
        builder.setTitle(LocaleController.getString("RevokeLink", NUM));
        builder.setPositiveButton(LocaleController.getString("RevokeButton", NUM), new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                LinkActionView.this.lambda$revokeLink$9$LinkActionView(dialogInterface, i);
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
        this.fragment.showDialog(builder.create());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$revokeLink$9 */
    public /* synthetic */ void lambda$revokeLink$9$LinkActionView(DialogInterface dialogInterface, int i) {
        Delegate delegate2 = this.delegate;
        if (delegate2 != null) {
            delegate2.revokeLink();
        }
    }

    public void setDelegate(Delegate delegate2) {
        this.delegate = delegate2;
    }

    public void setUsers(int i, ArrayList<TLRPC$User> arrayList) {
        this.usersCount = i;
        if (i == 0) {
            this.avatarsContainer.setVisibility(8);
            setPadding(AndroidUtilities.dp(19.0f), AndroidUtilities.dp(18.0f), AndroidUtilities.dp(19.0f), AndroidUtilities.dp(18.0f));
        } else {
            this.avatarsContainer.setVisibility(0);
            setPadding(AndroidUtilities.dp(19.0f), AndroidUtilities.dp(18.0f), AndroidUtilities.dp(19.0f), AndroidUtilities.dp(10.0f));
            this.avatarsContainer.countTextView.setText(LocaleController.formatPluralString("PeopleJoined", i));
            this.avatarsContainer.requestLayout();
        }
        if (arrayList != null) {
            for (int i2 = 0; i2 < 3; i2++) {
                if (i2 < arrayList.size()) {
                    MessagesController.getInstance(UserConfig.selectedAccount).putUser(arrayList.get(i2), false);
                    this.avatarsContainer.avatarsImageView.setObject(i2, UserConfig.selectedAccount, arrayList.get(i2));
                } else {
                    this.avatarsContainer.avatarsImageView.setObject(i2, UserConfig.selectedAccount, (TLObject) null);
                }
                this.avatarsContainer.avatarsImageView.commitTransition(false);
            }
        }
    }

    public void loadUsers(TLRPC$TL_chatInviteExported tLRPC$TL_chatInviteExported, int i) {
        if (tLRPC$TL_chatInviteExported == null) {
            setUsers(0, (ArrayList<TLRPC$User>) null);
            return;
        }
        setUsers(tLRPC$TL_chatInviteExported.usage, tLRPC$TL_chatInviteExported.importers);
        if (tLRPC$TL_chatInviteExported.usage > 0 && tLRPC$TL_chatInviteExported.importers == null && !this.loadingImporters) {
            TLRPC$TL_messages_getChatInviteImporters tLRPC$TL_messages_getChatInviteImporters = new TLRPC$TL_messages_getChatInviteImporters();
            tLRPC$TL_messages_getChatInviteImporters.link = tLRPC$TL_chatInviteExported.link;
            tLRPC$TL_messages_getChatInviteImporters.peer = MessagesController.getInstance(UserConfig.selectedAccount).getInputPeer(-i);
            tLRPC$TL_messages_getChatInviteImporters.offset_user = new TLRPC$TL_inputUserEmpty();
            tLRPC$TL_messages_getChatInviteImporters.limit = Math.min(tLRPC$TL_chatInviteExported.usage, 3);
            this.loadingImporters = true;
            ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(tLRPC$TL_messages_getChatInviteImporters, new RequestDelegate(tLRPC$TL_chatInviteExported) {
                public final /* synthetic */ TLRPC$TL_chatInviteExported f$1;

                {
                    this.f$1 = r2;
                }

                public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                    LinkActionView.this.lambda$loadUsers$11$LinkActionView(this.f$1, tLObject, tLRPC$TL_error);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$loadUsers$11 */
    public /* synthetic */ void lambda$loadUsers$11$LinkActionView(TLRPC$TL_chatInviteExported tLRPC$TL_chatInviteExported, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_error, tLObject, tLRPC$TL_chatInviteExported) {
            public final /* synthetic */ TLRPC$TL_error f$1;
            public final /* synthetic */ TLObject f$2;
            public final /* synthetic */ TLRPC$TL_chatInviteExported f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                LinkActionView.this.lambda$null$10$LinkActionView(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$10 */
    public /* synthetic */ void lambda$null$10$LinkActionView(TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, TLRPC$TL_chatInviteExported tLRPC$TL_chatInviteExported) {
        this.loadingImporters = false;
        if (tLRPC$TL_error == null) {
            TLRPC$TL_messages_chatInviteImporters tLRPC$TL_messages_chatInviteImporters = (TLRPC$TL_messages_chatInviteImporters) tLObject;
            if (tLRPC$TL_chatInviteExported.importers == null) {
                tLRPC$TL_chatInviteExported.importers = new ArrayList<>(3);
            }
            tLRPC$TL_chatInviteExported.importers.clear();
            for (int i = 0; i < tLRPC$TL_messages_chatInviteImporters.users.size(); i++) {
                tLRPC$TL_chatInviteExported.importers.addAll(tLRPC$TL_messages_chatInviteImporters.users);
            }
            setUsers(tLRPC$TL_chatInviteExported.usage, tLRPC$TL_chatInviteExported.importers);
        }
    }
}
