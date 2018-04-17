package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.text.SpannableStringBuilder;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.LocationController.SharingLocationInfo;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.SharingLocationsAlert.SharingLocationsAlertDelegate;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LocationActivity;
import org.telegram.ui.LocationActivity.LocationActivityDelegate;
import org.telegram.ui.VoIPActivity;

public class FragmentContextView extends FrameLayout implements NotificationCenterDelegate {
    private FragmentContextView additionalContextView;
    private AnimatorSet animatorSet;
    private Runnable checkLocationRunnable = new C11581();
    private ImageView closeButton;
    private int currentStyle = -1;
    private boolean firstLocationsLoaded;
    private BaseFragment fragment;
    private FrameLayout frameLayout;
    private boolean isLocation;
    private int lastLocationSharingCount = -1;
    private MessageObject lastMessageObject;
    private String lastString;
    private boolean loadingSharingCount;
    private ImageView playButton;
    private TextView titleTextView;
    private float topPadding;
    private boolean visible;
    private float yPosition;

    /* renamed from: org.telegram.ui.Components.FragmentContextView$1 */
    class C11581 implements Runnable {
        C11581() {
        }

        public void run() {
            FragmentContextView.this.checkLocationString();
            AndroidUtilities.runOnUIThread(FragmentContextView.this.checkLocationRunnable, 1000);
        }
    }

    /* renamed from: org.telegram.ui.Components.FragmentContextView$2 */
    class C11592 implements OnClickListener {
        C11592() {
        }

        public void onClick(View v) {
            if (FragmentContextView.this.currentStyle != 0) {
                return;
            }
            if (MediaController.getInstance().isMessagePaused()) {
                MediaController.getInstance().playMessage(MediaController.getInstance().getPlayingMessageObject());
            } else {
                MediaController.getInstance().pauseMessage(MediaController.getInstance().getPlayingMessageObject());
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.FragmentContextView$3 */
    class C11613 implements OnClickListener {

        /* renamed from: org.telegram.ui.Components.FragmentContextView$3$1 */
        class C11601 implements DialogInterface.OnClickListener {
            C11601() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                if (FragmentContextView.this.fragment instanceof DialogsActivity) {
                    for (int a = 0; a < 3; a++) {
                        LocationController.getInstance(a).removeAllLocationSharings();
                    }
                    return;
                }
                LocationController.getInstance(FragmentContextView.this.fragment.getCurrentAccount()).removeSharingLocation(((ChatActivity) FragmentContextView.this.fragment).getDialogId());
            }
        }

        C11613() {
        }

        public void onClick(View v) {
            if (FragmentContextView.this.currentStyle == 2) {
                Builder builder = new Builder(FragmentContextView.this.fragment.getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                if (FragmentContextView.this.fragment instanceof DialogsActivity) {
                    builder.setMessage(LocaleController.getString("StopLiveLocationAlertAll", R.string.StopLiveLocationAlertAll));
                } else {
                    ChatActivity activity = (ChatActivity) FragmentContextView.this.fragment;
                    Chat chat = activity.getCurrentChat();
                    User user = activity.getCurrentUser();
                    if (chat != null) {
                        builder.setMessage(LocaleController.formatString("StopLiveLocationAlertToGroup", R.string.StopLiveLocationAlertToGroup, chat.title));
                    } else if (user != null) {
                        builder.setMessage(LocaleController.formatString("StopLiveLocationAlertToUser", R.string.StopLiveLocationAlertToUser, UserObject.getFirstName(user)));
                    } else {
                        builder.setMessage(LocaleController.getString("AreYouSure", R.string.AreYouSure));
                    }
                }
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new C11601());
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                builder.show();
                return;
            }
            MediaController.getInstance().cleanupPlayer(true, true);
        }
    }

    /* renamed from: org.telegram.ui.Components.FragmentContextView$4 */
    class C11624 implements OnClickListener {

        /* renamed from: org.telegram.ui.Components.FragmentContextView$4$1 */
        class C20551 implements SharingLocationsAlertDelegate {
            C20551() {
            }

            public void didSelectLocation(SharingLocationInfo info) {
                FragmentContextView.this.openSharingLocation(info);
            }
        }

        C11624() {
        }

        public void onClick(View v) {
            long dialog_id;
            if (FragmentContextView.this.currentStyle == 0) {
                MessageObject messageObject = MediaController.getInstance().getPlayingMessageObject();
                if (!(FragmentContextView.this.fragment == null || messageObject == null)) {
                    if (messageObject.isMusic()) {
                        FragmentContextView.this.fragment.showDialog(new AudioPlayerAlert(FragmentContextView.this.getContext()));
                    } else {
                        dialog_id = 0;
                        if (FragmentContextView.this.fragment instanceof ChatActivity) {
                            dialog_id = ((ChatActivity) FragmentContextView.this.fragment).getDialogId();
                        }
                        if (messageObject.getDialogId() == dialog_id) {
                            ((ChatActivity) FragmentContextView.this.fragment).scrollToMessageId(messageObject.getId(), 0, false, 0, true);
                        } else {
                            dialog_id = messageObject.getDialogId();
                            Bundle args = new Bundle();
                            int lower_part = (int) dialog_id;
                            int high_id = (int) (dialog_id >> 32);
                            if (lower_part == 0) {
                                args.putInt("enc_id", high_id);
                            } else if (high_id == 1) {
                                args.putInt("chat_id", lower_part);
                            } else if (lower_part > 0) {
                                args.putInt("user_id", lower_part);
                            } else if (lower_part < 0) {
                                args.putInt("chat_id", -lower_part);
                            }
                            args.putInt("message_id", messageObject.getId());
                            FragmentContextView.this.fragment.presentFragment(new ChatActivity(args), FragmentContextView.this.fragment instanceof ChatActivity);
                        }
                    }
                }
            } else if (FragmentContextView.this.currentStyle == 1) {
                Intent intent = new Intent(FragmentContextView.this.getContext(), VoIPActivity.class);
                intent.addFlags(805306368);
                FragmentContextView.this.getContext().startActivity(intent);
            } else if (FragmentContextView.this.currentStyle == 2) {
                long did;
                dialog_id = 0;
                int account = UserConfig.selectedAccount;
                if (FragmentContextView.this.fragment instanceof ChatActivity) {
                    did = ((ChatActivity) FragmentContextView.this.fragment).getDialogId();
                    account = FragmentContextView.this.fragment.getCurrentAccount();
                } else if (LocationController.getLocationsCount() == 1) {
                    for (int a = 0; a < 3; a++) {
                        if (!LocationController.getInstance(a).sharingLocationsUI.isEmpty()) {
                            SharingLocationInfo info = (SharingLocationInfo) LocationController.getInstance(a).sharingLocationsUI.get(0);
                            dialog_id = info.did;
                            account = info.messageObject.currentAccount;
                            break;
                        }
                    }
                    did = dialog_id;
                } else {
                    did = 0;
                }
                if (did != 0) {
                    FragmentContextView.this.openSharingLocation(LocationController.getInstance(account).getSharingLocationInfo(did));
                } else {
                    FragmentContextView.this.fragment.showDialog(new SharingLocationsAlert(FragmentContextView.this.getContext(), new C20551()));
                }
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.FragmentContextView$6 */
    class C11636 extends AnimatorListenerAdapter {
        C11636() {
        }

        public void onAnimationEnd(Animator animation) {
            if (FragmentContextView.this.animatorSet != null && FragmentContextView.this.animatorSet.equals(animation)) {
                FragmentContextView.this.setVisibility(8);
                FragmentContextView.this.animatorSet = null;
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.FragmentContextView$7 */
    class C11647 extends AnimatorListenerAdapter {
        C11647() {
        }

        public void onAnimationEnd(Animator animation) {
            if (FragmentContextView.this.animatorSet != null && FragmentContextView.this.animatorSet.equals(animation)) {
                FragmentContextView.this.animatorSet = null;
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.FragmentContextView$8 */
    class C11658 extends AnimatorListenerAdapter {
        C11658() {
        }

        public void onAnimationEnd(Animator animation) {
            if (FragmentContextView.this.animatorSet != null && FragmentContextView.this.animatorSet.equals(animation)) {
                FragmentContextView.this.setVisibility(8);
                FragmentContextView.this.animatorSet = null;
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.FragmentContextView$9 */
    class C11669 extends AnimatorListenerAdapter {
        C11669() {
        }

        public void onAnimationEnd(Animator animation) {
            if (FragmentContextView.this.animatorSet != null && FragmentContextView.this.animatorSet.equals(animation)) {
                FragmentContextView.this.animatorSet = null;
            }
        }
    }

    public FragmentContextView(Context context, BaseFragment parentFragment, boolean location) {
        super(context);
        this.fragment = parentFragment;
        this.visible = true;
        this.isLocation = location;
        ((ViewGroup) this.fragment.getFragmentView()).setClipToPadding(false);
        setTag(Integer.valueOf(1));
        this.frameLayout = new FrameLayout(context);
        this.frameLayout.setWillNotDraw(false);
        addView(this.frameLayout, LayoutHelper.createFrame(-1, 36.0f, 51, 0.0f, 0.0f, 0.0f, 0.0f));
        View shadow = new View(context);
        shadow.setBackgroundResource(R.drawable.header_shadow);
        addView(shadow, LayoutHelper.createFrame(-1, 3.0f, 51, 0.0f, 36.0f, 0.0f, 0.0f));
        this.playButton = new ImageView(context);
        this.playButton.setScaleType(ScaleType.CENTER);
        this.playButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_inappPlayerPlayPause), Mode.MULTIPLY));
        addView(this.playButton, LayoutHelper.createFrame(36, 36.0f, 51, 0.0f, 0.0f, 0.0f, 0.0f));
        this.playButton.setOnClickListener(new C11592());
        this.titleTextView = new TextView(context);
        this.titleTextView.setMaxLines(1);
        this.titleTextView.setLines(1);
        this.titleTextView.setSingleLine(true);
        this.titleTextView.setEllipsize(TruncateAt.END);
        this.titleTextView.setTextSize(1, 15.0f);
        this.titleTextView.setGravity(19);
        addView(this.titleTextView, LayoutHelper.createFrame(-1, 36.0f, 51, 35.0f, 0.0f, 36.0f, 0.0f));
        this.closeButton = new ImageView(context);
        this.closeButton.setImageResource(R.drawable.miniplayer_close);
        this.closeButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_inappPlayerClose), Mode.MULTIPLY));
        this.closeButton.setScaleType(ScaleType.CENTER);
        addView(this.closeButton, LayoutHelper.createFrame(36, 36, 53));
        this.closeButton.setOnClickListener(new C11613());
        setOnClickListener(new C11624());
    }

    public void setAdditionalContextView(FragmentContextView contextView) {
        this.additionalContextView = contextView;
    }

    private void openSharingLocation(final SharingLocationInfo info) {
        if (info != null) {
            if (this.fragment.getParentActivity() != null) {
                LaunchActivity launchActivity = (LaunchActivity) this.fragment.getParentActivity();
                launchActivity.switchToAccount(info.messageObject.currentAccount, true);
                LocationActivity locationActivity = new LocationActivity(2);
                locationActivity.setMessageObject(info.messageObject);
                final long dialog_id = info.messageObject.getDialogId();
                locationActivity.setDelegate(new LocationActivityDelegate() {
                    public void didSelectLocation(MessageMedia location, int live) {
                        SendMessagesHelper.getInstance(info.messageObject.currentAccount).sendMessage(location, dialog_id, null, null, null);
                    }
                });
                launchActivity.presentFragment(locationActivity);
            }
        }
    }

    public float getTopPadding() {
        return this.topPadding;
    }

    private void checkVisibility() {
        boolean show = false;
        int i = 0;
        if (this.isLocation) {
            if (this.fragment instanceof DialogsActivity) {
                show = LocationController.getLocationsCount() != 0;
            } else {
                show = LocationController.getInstance(this.fragment.getCurrentAccount()).isSharingLocation(((ChatActivity) this.fragment).getDialogId());
            }
        } else if (VoIPService.getSharedInstance() == null || VoIPService.getSharedInstance().getCallState() == 15) {
            MessageObject messageObject = MediaController.getInstance().getPlayingMessageObject();
            if (!(messageObject == null || messageObject.getId() == 0)) {
                show = true;
            }
        } else {
            show = true;
        }
        if (!show) {
            i = 8;
        }
        setVisibility(i);
    }

    @Keep
    public void setTopPadding(float value) {
        this.topPadding = value;
        if (this.fragment != null) {
            View view = this.fragment.getFragmentView();
            int additionalPadding = 0;
            if (this.additionalContextView != null && this.additionalContextView.getVisibility() == 0) {
                additionalPadding = AndroidUtilities.dp(36.0f);
            }
            if (view != null) {
                view.setPadding(0, ((int) this.topPadding) + additionalPadding, 0, 0);
            }
            if (this.isLocation && this.additionalContextView != null) {
                ((LayoutParams) this.additionalContextView.getLayoutParams()).topMargin = (-AndroidUtilities.dp(36.0f)) - ((int) this.topPadding);
            }
        }
    }

    private void updateStyle(int style) {
        if (this.currentStyle != style) {
            this.currentStyle = style;
            if (style != 0) {
                if (style != 2) {
                    if (style == 1) {
                        this.titleTextView.setText(LocaleController.getString("ReturnToCall", R.string.ReturnToCall));
                        this.frameLayout.setBackgroundColor(Theme.getColor(Theme.key_returnToCallBackground));
                        this.frameLayout.setTag(Theme.key_returnToCallBackground);
                        this.titleTextView.setTextColor(Theme.getColor(Theme.key_returnToCallText));
                        this.titleTextView.setTag(Theme.key_returnToCallText);
                        this.closeButton.setVisibility(8);
                        this.playButton.setVisibility(8);
                        this.titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                        this.titleTextView.setTextSize(1, 14.0f);
                        this.titleTextView.setLayoutParams(LayoutHelper.createFrame(-2, -2.0f, 17, 0.0f, 0.0f, 0.0f, 2.0f));
                    }
                }
            }
            this.frameLayout.setBackgroundColor(Theme.getColor(Theme.key_inappPlayerBackground));
            this.frameLayout.setTag(Theme.key_inappPlayerBackground);
            this.titleTextView.setTextColor(Theme.getColor(Theme.key_inappPlayerTitle));
            this.titleTextView.setTag(Theme.key_inappPlayerTitle);
            this.closeButton.setVisibility(0);
            this.playButton.setVisibility(0);
            this.titleTextView.setTypeface(Typeface.DEFAULT);
            this.titleTextView.setTextSize(1, 15.0f);
            this.titleTextView.setLayoutParams(LayoutHelper.createFrame(-1, 36.0f, 51, 35.0f, 0.0f, 36.0f, 0.0f));
            if (style == 0) {
                this.playButton.setLayoutParams(LayoutHelper.createFrame(36, 36.0f, 51, 0.0f, 0.0f, 0.0f, 0.0f));
                this.titleTextView.setLayoutParams(LayoutHelper.createFrame(-1, 36.0f, 51, 35.0f, 0.0f, 36.0f, 0.0f));
            } else if (style == 2) {
                this.playButton.setLayoutParams(LayoutHelper.createFrame(36, 36.0f, 51, 8.0f, 0.0f, 0.0f, 0.0f));
                this.titleTextView.setLayoutParams(LayoutHelper.createFrame(-1, 36.0f, 51, 51.0f, 0.0f, 36.0f, 0.0f));
            }
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.topPadding = 0.0f;
        if (this.isLocation) {
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.liveLocationsChanged);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.liveLocationsCacheChanged);
            return;
        }
        for (int a = 0; a < 3; a++) {
            NotificationCenter.getInstance(a).removeObserver(this, NotificationCenter.messagePlayingDidReset);
            NotificationCenter.getInstance(a).removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
            NotificationCenter.getInstance(a).removeObserver(this, NotificationCenter.messagePlayingDidStarted);
        }
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didStartedCall);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didEndedCall);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.isLocation) {
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.liveLocationsChanged);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.liveLocationsCacheChanged);
            if (this.additionalContextView != null) {
                this.additionalContextView.checkVisibility();
            }
            checkLiveLocation(true);
            return;
        }
        for (int a = 0; a < 3; a++) {
            NotificationCenter.getInstance(a).addObserver(this, NotificationCenter.messagePlayingDidReset);
            NotificationCenter.getInstance(a).addObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
            NotificationCenter.getInstance(a).addObserver(this, NotificationCenter.messagePlayingDidStarted);
        }
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didStartedCall);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didEndedCall);
        if (this.additionalContextView != null) {
            this.additionalContextView.checkVisibility();
        }
        if (VoIPService.getSharedInstance() == null || VoIPService.getSharedInstance().getCallState() == 15) {
            checkPlayer(true);
        } else {
            checkCall(true);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, AndroidUtilities.dp2(39.0f));
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.liveLocationsChanged) {
            checkLiveLocation(false);
        } else if (id != NotificationCenter.liveLocationsCacheChanged) {
            if (!(id == NotificationCenter.messagePlayingDidStarted || id == NotificationCenter.messagePlayingPlayStateChanged || id == NotificationCenter.messagePlayingDidReset)) {
                if (id != NotificationCenter.didEndedCall) {
                    if (id == NotificationCenter.didStartedCall) {
                        checkCall(false);
                        return;
                    } else {
                        checkPlayer(false);
                        return;
                    }
                }
            }
            checkPlayer(false);
        } else if (this.fragment instanceof ChatActivity) {
            if (((ChatActivity) this.fragment).getDialogId() == ((Long) args[0]).longValue()) {
                checkLocationString();
            }
        }
    }

    private void checkLiveLocation(boolean create) {
        boolean show;
        View fragmentView = this.fragment.getFragmentView();
        if (!(create || fragmentView == null || (fragmentView.getParent() != null && ((View) fragmentView.getParent()).getVisibility() == 0))) {
            create = true;
        }
        if (this.fragment instanceof DialogsActivity) {
            show = LocationController.getLocationsCount() != 0;
        } else {
            show = LocationController.getInstance(this.fragment.getCurrentAccount()).isSharingLocation(((ChatActivity) this.fragment).getDialogId());
        }
        if (show) {
            updateStyle(2);
            this.playButton.setImageDrawable(new ShareLocationDrawable(getContext(), true));
            if (create && this.topPadding == 0.0f) {
                setTopPadding((float) AndroidUtilities.dp2(36.0f));
                setTranslationY(0.0f);
                this.yPosition = 0.0f;
            }
            if (!this.visible) {
                if (!create) {
                    if (this.animatorSet != null) {
                        this.animatorSet.cancel();
                        this.animatorSet = null;
                    }
                    this.animatorSet = new AnimatorSet();
                    AnimatorSet animatorSet = this.animatorSet;
                    r10 = new Animator[2];
                    r10[0] = ObjectAnimator.ofFloat(this, "translationY", new float[]{(float) (-AndroidUtilities.dp2(36.0f)), 0.0f});
                    r10[1] = ObjectAnimator.ofFloat(this, "topPadding", new float[]{(float) AndroidUtilities.dp2(36.0f)});
                    animatorSet.playTogether(r10);
                    this.animatorSet.setDuration(200);
                    this.animatorSet.addListener(new C11647());
                    this.animatorSet.start();
                }
                this.visible = true;
                setVisibility(0);
            }
            if (this.fragment instanceof DialogsActivity) {
                int lower_id;
                String param;
                String liveLocation = LocaleController.getString("AttachLiveLocation", R.string.AttachLiveLocation);
                ArrayList<SharingLocationInfo> infos = new ArrayList();
                for (int a = 0; a < 3; a++) {
                    infos.addAll(LocationController.getInstance(a).sharingLocationsUI);
                }
                if (infos.size() == 1) {
                    String param2;
                    SharingLocationInfo info = (SharingLocationInfo) infos.get(0);
                    lower_id = (int) info.messageObject.getDialogId();
                    if (lower_id > 0) {
                        param2 = UserObject.getFirstName(MessagesController.getInstance(info.messageObject.currentAccount).getUser(Integer.valueOf(lower_id)));
                    } else {
                        Chat chat = MessagesController.getInstance(info.messageObject.currentAccount).getChat(Integer.valueOf(-lower_id));
                        if (chat != null) {
                            param2 = chat.title;
                        } else {
                            param2 = TtmlNode.ANONYMOUS_REGION_ID;
                        }
                    }
                    param = param2;
                } else {
                    param = LocaleController.formatPluralString("Chats", infos.size());
                }
                String fullString = String.format(LocaleController.getString("AttachLiveLocationIsSharing", R.string.AttachLiveLocationIsSharing), new Object[]{liveLocation, param});
                lower_id = fullString.indexOf(liveLocation);
                SpannableStringBuilder stringBuilder = new SpannableStringBuilder(fullString);
                this.titleTextView.setEllipsize(TruncateAt.END);
                stringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor(Theme.key_inappPlayerPerformer)), lower_id, liveLocation.length() + lower_id, 18);
                this.titleTextView.setText(stringBuilder);
                return;
            }
            this.checkLocationRunnable.run();
            checkLocationString();
            return;
        }
        this.lastLocationSharingCount = -1;
        AndroidUtilities.cancelRunOnUIThread(this.checkLocationRunnable);
        if (this.visible) {
            this.visible = false;
            if (create) {
                if (getVisibility() != 8) {
                    setVisibility(8);
                }
                setTopPadding(0.0f);
                return;
            }
            if (this.animatorSet != null) {
                this.animatorSet.cancel();
                this.animatorSet = null;
            }
            this.animatorSet = new AnimatorSet();
            animatorSet = this.animatorSet;
            r8 = new Animator[2];
            r8[0] = ObjectAnimator.ofFloat(this, "translationY", new float[]{(float) (-AndroidUtilities.dp2(36.0f))});
            r8[1] = ObjectAnimator.ofFloat(this, "topPadding", new float[]{0.0f});
            animatorSet.playTogether(r8);
            this.animatorSet.setDuration(200);
            this.animatorSet.addListener(new C11636());
            this.animatorSet.start();
        }
    }

    private void checkLocationString() {
        if (this.fragment instanceof ChatActivity) {
            if (r0.titleTextView != null) {
                int date;
                ChatActivity chatActivity = r0.fragment;
                long dialogId = chatActivity.getDialogId();
                int currentAccount = chatActivity.getCurrentAccount();
                ArrayList<Message> messages = (ArrayList) LocationController.getInstance(currentAccount).locationsCache.get(dialogId);
                if (!r0.firstLocationsLoaded) {
                    LocationController.getInstance(currentAccount).loadLiveLocations(dialogId);
                    r0.firstLocationsLoaded = true;
                }
                int locationSharingCount = 0;
                User notYouUser = null;
                if (messages != null) {
                    int currentUserId = UserConfig.getInstance(currentAccount).getClientUserId();
                    date = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
                    User notYouUser2 = null;
                    int locationSharingCount2 = 0;
                    for (locationSharingCount = 0; locationSharingCount < messages.size(); locationSharingCount++) {
                        Message message = (Message) messages.get(locationSharingCount);
                        if (message.media != null) {
                            if (message.date + message.media.period > date) {
                                if (notYouUser2 == null && message.from_id != currentUserId) {
                                    notYouUser2 = MessagesController.getInstance(currentAccount).getUser(Integer.valueOf(message.from_id));
                                }
                                locationSharingCount2++;
                            }
                        }
                    }
                    locationSharingCount = locationSharingCount2;
                    notYouUser = notYouUser2;
                }
                if (r0.lastLocationSharingCount != locationSharingCount) {
                    String fullString;
                    int start;
                    SpannableStringBuilder stringBuilder;
                    r0.lastLocationSharingCount = locationSharingCount;
                    String liveLocation = LocaleController.getString("AttachLiveLocation", R.string.AttachLiveLocation);
                    if (locationSharingCount == 0) {
                        fullString = liveLocation;
                    } else {
                        date = locationSharingCount - 1;
                        if (!LocationController.getInstance(currentAccount).isSharingLocation(dialogId)) {
                            int i = 0;
                            if (date != 0) {
                                fullString = String.format("%1$s - %2$s %3$s", new Object[]{liveLocation, UserObject.getFirstName(notYouUser), LocaleController.formatPluralString("AndOther", date)});
                            } else {
                                fullString = String.format("%1$s - %2$s", new Object[]{liveLocation, UserObject.getFirstName(notYouUser)});
                                if (r0.lastString != null || !fullString.equals(r0.lastString)) {
                                    r0.lastString = fullString;
                                    start = fullString.indexOf(liveLocation);
                                    stringBuilder = new SpannableStringBuilder(fullString);
                                    r0.titleTextView.setEllipsize(TruncateAt.END);
                                    if (start >= 0) {
                                        stringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor(Theme.key_inappPlayerPerformer)), start, liveLocation.length() + start, 18);
                                    }
                                    r0.titleTextView.setText(stringBuilder);
                                }
                                return;
                            }
                        } else if (date == 0) {
                            fullString = String.format("%1$s - %2$s", new Object[]{liveLocation, LocaleController.getString("ChatYourSelfName", R.string.ChatYourSelfName)});
                        } else if (date != 1 || notYouUser == null) {
                            fullString = String.format("%1$s - %2$s %3$s", new Object[]{liveLocation, LocaleController.getString("ChatYourSelfName", R.string.ChatYourSelfName), LocaleController.formatPluralString("AndOther", date)});
                        } else {
                            Object[] objArr = new Object[2];
                            objArr[0] = liveLocation;
                            objArr[1] = LocaleController.formatString("SharingYouAndOtherName", R.string.SharingYouAndOtherName, UserObject.getFirstName(notYouUser));
                            fullString = String.format("%1$s - %2$s", objArr);
                        }
                    }
                    if (r0.lastString != null) {
                    }
                    r0.lastString = fullString;
                    start = fullString.indexOf(liveLocation);
                    stringBuilder = new SpannableStringBuilder(fullString);
                    r0.titleTextView.setEllipsize(TruncateAt.END);
                    if (start >= 0) {
                        stringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor(Theme.key_inappPlayerPerformer)), start, liveLocation.length() + start, 18);
                    }
                    r0.titleTextView.setText(stringBuilder);
                }
            }
        }
    }

    private void checkPlayer(boolean create) {
        boolean create2;
        MessageObject messageObject = MediaController.getInstance().getPlayingMessageObject();
        View fragmentView = this.fragment.getFragmentView();
        if (create || fragmentView == null || (fragmentView.getParent() != null && ((View) fragmentView.getParent()).getVisibility() == 0)) {
            create2 = create;
        } else {
            create2 = true;
        }
        if (messageObject != null) {
            if (messageObject.getId() != 0) {
                int prevStyle = r0.currentStyle;
                updateStyle(0);
                if (create2 && r0.topPadding == 0.0f) {
                    setTopPadding((float) AndroidUtilities.dp2(36.0f));
                    if (r0.additionalContextView == null || r0.additionalContextView.getVisibility() != 0) {
                        ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(36.0f);
                    } else {
                        ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(72.0f);
                    }
                    setTranslationY(0.0f);
                    r0.yPosition = 0.0f;
                }
                if (!r0.visible) {
                    if (!create2) {
                        if (r0.animatorSet != null) {
                            r0.animatorSet.cancel();
                            r0.animatorSet = null;
                        }
                        r0.animatorSet = new AnimatorSet();
                        if (r0.additionalContextView == null || r0.additionalContextView.getVisibility() != 0) {
                            ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(36.0f);
                        } else {
                            ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(72.0f);
                        }
                        AnimatorSet animatorSet = r0.animatorSet;
                        r13 = new Animator[2];
                        r13[0] = ObjectAnimator.ofFloat(r0, "translationY", new float[]{(float) (-AndroidUtilities.dp2(36.0f)), 0.0f});
                        r13[1] = ObjectAnimator.ofFloat(r0, "topPadding", new float[]{(float) AndroidUtilities.dp2(36.0f)});
                        animatorSet.playTogether(r13);
                        r0.animatorSet.setDuration(200);
                        r0.animatorSet.addListener(new C11669());
                        r0.animatorSet.start();
                    }
                    r0.visible = true;
                    setVisibility(0);
                }
                if (MediaController.getInstance().isMessagePaused()) {
                    r0.playButton.setImageResource(R.drawable.miniplayer_play);
                } else {
                    r0.playButton.setImageResource(R.drawable.miniplayer_pause);
                }
                if (!(r0.lastMessageObject == messageObject && prevStyle == 0)) {
                    SpannableStringBuilder stringBuilder;
                    r0.lastMessageObject = messageObject;
                    if (!r0.lastMessageObject.isVoice()) {
                        if (!r0.lastMessageObject.isRoundVideo()) {
                            stringBuilder = new SpannableStringBuilder(String.format("%s - %s", new Object[]{messageObject.getMusicAuthor(), messageObject.getMusicTitle()}));
                            r0.titleTextView.setEllipsize(TruncateAt.END);
                            stringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor(Theme.key_inappPlayerPerformer)), 0, messageObject.getMusicAuthor().length(), 18);
                            r0.titleTextView.setText(stringBuilder);
                        }
                    }
                    stringBuilder = new SpannableStringBuilder(String.format("%s %s", new Object[]{messageObject.getMusicAuthor(), messageObject.getMusicTitle()}));
                    r0.titleTextView.setEllipsize(TruncateAt.MIDDLE);
                    stringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor(Theme.key_inappPlayerPerformer)), 0, messageObject.getMusicAuthor().length(), 18);
                    r0.titleTextView.setText(stringBuilder);
                }
            }
        }
        r0.lastMessageObject = null;
        boolean callAvailable = (VoIPService.getSharedInstance() == null || VoIPService.getSharedInstance().getCallState() == 15) ? false : true;
        if (callAvailable) {
            checkCall(false);
            return;
        }
        if (r0.visible) {
            r0.visible = false;
            if (create2) {
                if (getVisibility() != 8) {
                    setVisibility(8);
                }
                setTopPadding(0.0f);
            } else {
                if (r0.animatorSet != null) {
                    r0.animatorSet.cancel();
                    r0.animatorSet = null;
                }
                r0.animatorSet = new AnimatorSet();
                animatorSet = r0.animatorSet;
                r12 = new Animator[2];
                r12[0] = ObjectAnimator.ofFloat(r0, "translationY", new float[]{(float) (-AndroidUtilities.dp2(36.0f))});
                r12[1] = ObjectAnimator.ofFloat(r0, "topPadding", new float[]{0.0f});
                animatorSet.playTogether(r12);
                r0.animatorSet.setDuration(200);
                r0.animatorSet.addListener(new C11658());
                r0.animatorSet.start();
            }
        }
    }

    private void checkCall(boolean create) {
        View fragmentView = this.fragment.getFragmentView();
        if (!(create || fragmentView == null || (fragmentView.getParent() != null && ((View) fragmentView.getParent()).getVisibility() == 0))) {
            create = true;
        }
        boolean callAvailable = (VoIPService.getSharedInstance() == null || VoIPService.getSharedInstance().getCallState() == 15) ? false : true;
        AnimatorSet animatorSet;
        if (callAvailable) {
            updateStyle(1);
            if (create && this.topPadding == 0.0f) {
                setTopPadding((float) AndroidUtilities.dp2(36.0f));
                if (this.additionalContextView == null || this.additionalContextView.getVisibility() != 0) {
                    ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(36.0f);
                } else {
                    ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(72.0f);
                }
                setTranslationY(0.0f);
                this.yPosition = 0.0f;
            }
            if (!this.visible) {
                if (!create) {
                    if (this.animatorSet != null) {
                        this.animatorSet.cancel();
                        this.animatorSet = null;
                    }
                    this.animatorSet = new AnimatorSet();
                    if (this.additionalContextView == null || this.additionalContextView.getVisibility() != 0) {
                        ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(36.0f);
                    } else {
                        ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(72.0f);
                    }
                    animatorSet = this.animatorSet;
                    r10 = new Animator[2];
                    r10[0] = ObjectAnimator.ofFloat(this, "translationY", new float[]{(float) (-AndroidUtilities.dp2(36.0f)), 0.0f});
                    r10[1] = ObjectAnimator.ofFloat(this, "topPadding", new float[]{(float) AndroidUtilities.dp2(36.0f)});
                    animatorSet.playTogether(r10);
                    this.animatorSet.setDuration(200);
                    this.animatorSet.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            if (FragmentContextView.this.animatorSet != null && FragmentContextView.this.animatorSet.equals(animation)) {
                                FragmentContextView.this.animatorSet = null;
                            }
                        }
                    });
                    this.animatorSet.start();
                }
                this.visible = true;
                setVisibility(0);
            }
        } else if (this.visible) {
            this.visible = false;
            if (create) {
                if (getVisibility() != 8) {
                    setVisibility(8);
                }
                setTopPadding(0.0f);
                return;
            }
            if (this.animatorSet != null) {
                this.animatorSet.cancel();
                this.animatorSet = null;
            }
            this.animatorSet = new AnimatorSet();
            animatorSet = this.animatorSet;
            r7 = new Animator[2];
            r7[0] = ObjectAnimator.ofFloat(this, "translationY", new float[]{(float) (-AndroidUtilities.dp2(36.0f))});
            r7[1] = ObjectAnimator.ofFloat(this, "topPadding", new float[]{0.0f});
            animatorSet.playTogether(r7);
            this.animatorSet.setDuration(200);
            this.animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (FragmentContextView.this.animatorSet != null && FragmentContextView.this.animatorSet.equals(animation)) {
                        FragmentContextView.this.setVisibility(8);
                        FragmentContextView.this.animatorSet = null;
                    }
                }
            });
            this.animatorSet.start();
        }
    }

    @Keep
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        this.yPosition = translationY;
        invalidate();
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        int restoreToCount = canvas.save();
        if (this.yPosition < 0.0f) {
            canvas.clipRect(0, (int) (-this.yPosition), child.getMeasuredWidth(), AndroidUtilities.dp2(39.0f));
        }
        boolean result = super.drawChild(canvas, child, drawingTime);
        canvas.restoreToCount(restoreToCount);
        return result;
    }
}
