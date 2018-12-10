package org.telegram.p005ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.p005ui.ActionBar.BaseFragment;
import org.telegram.p005ui.ActionBar.CLASSNAMEActionBar.ActionBarMenuOnItemClick;
import org.telegram.p005ui.ActionBar.Theme;
import org.telegram.p005ui.ActionBar.ThemeDescription;
import org.telegram.p005ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.p005ui.Cells.GroupCreateSectionCell;
import org.telegram.p005ui.Cells.GroupCreateUserCell;
import org.telegram.p005ui.Components.AvatarDrawable;
import org.telegram.p005ui.Components.BackupImageView;
import org.telegram.p005ui.Components.CombinedDrawable;
import org.telegram.p005ui.Components.ContextProgressView;
import org.telegram.p005ui.Components.EditTextEmoji;
import org.telegram.p005ui.Components.GroupCreateDividerItemDecoration;
import org.telegram.p005ui.Components.ImageUpdater;
import org.telegram.p005ui.Components.ImageUpdater.ImageUpdaterDelegate;
import org.telegram.p005ui.Components.LayoutHelper;
import org.telegram.p005ui.Components.RecyclerListView;
import org.telegram.p005ui.Components.RecyclerListView.Holder;
import org.telegram.p005ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.p005ui.Components.SizeNotifierFrameLayout;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_secureFile;
import org.telegram.tgnet.TLRPC.User;

/* renamed from: org.telegram.ui.GroupCreateFinalActivity */
public class GroupCreateFinalActivity extends BaseFragment implements NotificationCenterDelegate, ImageUpdaterDelegate {
    private static final int done_button = 1;
    private GroupCreateAdapter adapter;
    private FileLocation avatar;
    private AvatarDrawable avatarDrawable = new AvatarDrawable();
    private BackupImageView avatarImage;
    private int chatType;
    private boolean createAfterUpload;
    private AnimatorSet doneItemAnimation;
    private boolean donePressed;
    private EditTextEmoji editText;
    private FrameLayout editTextContainer;
    private FrameLayout floatingButtonContainer;
    private ImageView floatingButtonIcon;
    private ImageUpdater imageUpdater;
    private RecyclerView listView;
    private String nameToSet;
    private ContextProgressView progressView;
    private int reqId;
    private ArrayList<Integer> selectedContacts;
    private InputFile uploadedAvatar;

    /* renamed from: org.telegram.ui.GroupCreateFinalActivity$6 */
    class CLASSNAME extends ViewOutlineProvider {
        CLASSNAME() {
        }

        @SuppressLint({"NewApi"})
        public void getOutline(View view, Outline outline) {
            outline.setOval(0, 0, AndroidUtilities.m9dp(56.0f), AndroidUtilities.m9dp(56.0f));
        }
    }

    /* renamed from: org.telegram.ui.GroupCreateFinalActivity$1 */
    class CLASSNAME extends ActionBarMenuOnItemClick {
        CLASSNAME() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                GroupCreateFinalActivity.this.lambda$checkDiscard$70$PassportActivity();
            }
        }
    }

    /* renamed from: org.telegram.ui.GroupCreateFinalActivity$5 */
    class CLASSNAME extends OnScrollListener {
        CLASSNAME() {
        }

        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == 1) {
                AndroidUtilities.hideKeyboard(GroupCreateFinalActivity.this.editText);
            }
        }
    }

    /* renamed from: org.telegram.ui.GroupCreateFinalActivity$GroupCreateAdapter */
    public class GroupCreateAdapter extends SelectionAdapter {
        private Context context;

        public GroupCreateAdapter(Context ctx) {
            this.context = ctx;
        }

        public int getItemCount() {
            return GroupCreateFinalActivity.this.selectedContacts.size() + 1;
        }

        public boolean isEnabled(ViewHolder holder) {
            return false;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new GroupCreateSectionCell(this.context);
                    break;
                default:
                    view = new GroupCreateUserCell(this.context, false);
                    break;
            }
            return new Holder(view);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    holder.itemView.setText(LocaleController.formatPluralString("Members", GroupCreateFinalActivity.this.selectedContacts.size()));
                    return;
                default:
                    holder.itemView.setUser(MessagesController.getInstance(GroupCreateFinalActivity.this.currentAccount).getUser((Integer) GroupCreateFinalActivity.this.selectedContacts.get(position - 1)), null, null);
                    return;
            }
        }

        public int getItemViewType(int position) {
            switch (position) {
                case 0:
                    return 0;
                default:
                    return 1;
            }
        }

        public void onViewRecycled(ViewHolder holder) {
            if (holder.getItemViewType() == 1) {
                ((GroupCreateUserCell) holder.itemView).recycle();
            }
        }
    }

    public GroupCreateFinalActivity(Bundle args) {
        super(args);
        this.chatType = args.getInt("chatType", 0);
    }

    public boolean onFragmentCreate() {
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatDidCreated);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatDidFailCreate);
        this.imageUpdater = new ImageUpdater();
        this.imageUpdater.parentFragment = this;
        this.imageUpdater.delegate = this;
        this.selectedContacts = getArguments().getIntegerArrayList("result");
        ArrayList<Integer> usersToLoad = new ArrayList();
        for (int a = 0; a < this.selectedContacts.size(); a++) {
            Integer uid = (Integer) this.selectedContacts.get(a);
            if (MessagesController.getInstance(this.currentAccount).getUser(uid) == null) {
                usersToLoad.add(uid);
            }
        }
        if (!usersToLoad.isEmpty()) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            ArrayList<User> users = new ArrayList();
            MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new GroupCreateFinalActivity$$Lambda$0(this, users, usersToLoad, countDownLatch));
            try {
                countDownLatch.await();
            } catch (Throwable e) {
                FileLog.m13e(e);
            }
            if (usersToLoad.size() != users.size() || users.isEmpty()) {
                return false;
            }
            Iterator it = users.iterator();
            while (it.hasNext()) {
                MessagesController.getInstance(this.currentAccount).putUser((User) it.next(), true);
            }
        }
        return super.onFragmentCreate();
    }

    final /* synthetic */ void lambda$onFragmentCreate$0$GroupCreateFinalActivity(ArrayList users, ArrayList usersToLoad, CountDownLatch countDownLatch) {
        users.addAll(MessagesStorage.getInstance(this.currentAccount).getUsers(usersToLoad));
        countDownLatch.countDown();
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatDidCreated);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatDidFailCreate);
        this.imageUpdater.clear();
        if (this.reqId != 0) {
            ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.reqId, true);
        }
        if (this.editText != null) {
            this.editText.onDestroy();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.editText != null) {
            this.editText.onResume();
        }
        if (this.adapter != null) {
            this.adapter.notifyDataSetChanged();
        }
    }

    public void onPause() {
        super.onPause();
        if (this.editText != null) {
            this.editText.onPause();
        }
    }

    public boolean onBackPressed() {
        if (this.editText == null || !this.editText.isPopupShowing()) {
            return true;
        }
        this.editText.hidePopup(true);
        return false;
    }

    public View createView(Context context) {
        float f;
        float f2;
        float f3;
        if (this.editText != null) {
            this.editText.onDestroy();
        }
        this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("NewGroup", R.string.NewGroup));
        this.actionBar.setActionBarMenuOnItemClick(new CLASSNAME());
        View CLASSNAME = new SizeNotifierFrameLayout(context) {
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);
                setMeasuredDimension(widthSize, heightSize);
                heightSize -= getPaddingTop();
                measureChildWithMargins(GroupCreateFinalActivity.this.actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int keyboardSize = getKeyboardHeight();
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (!(child == null || child.getVisibility() == 8 || child == GroupCreateFinalActivity.this.actionBar)) {
                        if (GroupCreateFinalActivity.this.editText == null || !GroupCreateFinalActivity.this.editText.isPopupView(child)) {
                            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                        } else if (!AndroidUtilities.isInMultiwindow && !AndroidUtilities.isTablet()) {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, NUM), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, NUM));
                        } else if (AndroidUtilities.isTablet()) {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, NUM), MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.m9dp(AndroidUtilities.isTablet() ? 200.0f : 320.0f), (heightSize - AndroidUtilities.statusBarHeight) + getPaddingTop()), NUM));
                        } else {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, NUM), MeasureSpec.makeMeasureSpec((heightSize - AndroidUtilities.statusBarHeight) + getPaddingTop(), NUM));
                        }
                    }
                }
            }

            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                int count = getChildCount();
                int paddingBottom = (getKeyboardHeight() > AndroidUtilities.m9dp(20.0f) || AndroidUtilities.isInMultiwindow || AndroidUtilities.isTablet()) ? 0 : GroupCreateFinalActivity.this.editText.getEmojiPadding();
                setBottomClip(paddingBottom);
                for (int i = 0; i < count; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() != 8) {
                        int childLeft;
                        int childTop;
                        LayoutParams lp = (LayoutParams) child.getLayoutParams();
                        int width = child.getMeasuredWidth();
                        int height = child.getMeasuredHeight();
                        int gravity = lp.gravity;
                        if (gravity == -1) {
                            gravity = 51;
                        }
                        int verticalGravity = gravity & 112;
                        switch ((gravity & 7) & 7) {
                            case 1:
                                childLeft = ((((r - l) - width) / 2) + lp.leftMargin) - lp.rightMargin;
                                break;
                            case 5:
                                childLeft = (r - width) - lp.rightMargin;
                                break;
                            default:
                                childLeft = lp.leftMargin;
                                break;
                        }
                        switch (verticalGravity) {
                            case 16:
                                childTop = (((((b - paddingBottom) - t) - height) / 2) + lp.topMargin) - lp.bottomMargin;
                                break;
                            case 48:
                                childTop = lp.topMargin + getPaddingTop();
                                break;
                            case 80:
                                childTop = (((b - paddingBottom) - t) - height) - lp.bottomMargin;
                                break;
                            default:
                                childTop = lp.topMargin;
                                break;
                        }
                        if (GroupCreateFinalActivity.this.editText != null && GroupCreateFinalActivity.this.editText.isPopupView(child)) {
                            if (AndroidUtilities.isTablet()) {
                                childTop = getMeasuredHeight() - child.getMeasuredHeight();
                            } else {
                                childTop = (getMeasuredHeight() + getKeyboardHeight()) - child.getMeasuredHeight();
                            }
                        }
                        child.layout(childLeft, childTop, childLeft + width, childTop + height);
                    }
                }
                notifyHeightChanged();
            }
        };
        this.fragmentView = CLASSNAME;
        this.fragmentView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        this.fragmentView.setOnTouchListener(GroupCreateFinalActivity$$Lambda$1.$instance);
        CLASSNAME = new LinearLayout(context) {
            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                boolean result = super.drawChild(canvas, child, drawingTime);
                if (child == GroupCreateFinalActivity.this.listView) {
                    GroupCreateFinalActivity.this.parentLayout.drawHeaderShadow(canvas, GroupCreateFinalActivity.this.editTextContainer.getMeasuredHeight());
                }
                return result;
            }
        };
        CLASSNAME.setOrientation(1);
        CLASSNAME.addView(CLASSNAME, LayoutHelper.createFrame(-1, -1.0f));
        this.editTextContainer = new FrameLayout(context);
        CLASSNAME.addView(this.editTextContainer, LayoutHelper.createLinear(-1, -2));
        this.avatarImage = new BackupImageView(context);
        this.avatarImage.setRoundRadius(AndroidUtilities.m9dp(32.0f));
        this.avatarDrawable.setInfo(5, null, null, this.chatType == 1);
        this.avatarImage.setImageDrawable(this.avatarDrawable);
        FrameLayout frameLayout = this.editTextContainer;
        View view = this.avatarImage;
        int i = (LocaleController.isRTL ? 5 : 3) | 48;
        if (LocaleController.isRTL) {
            f = 0.0f;
        } else {
            f = 16.0f;
        }
        if (LocaleController.isRTL) {
            f2 = 16.0f;
        } else {
            f2 = 0.0f;
        }
        frameLayout.addView(view, LayoutHelper.createFrame(64, 64.0f, i, f, 16.0f, f2, 16.0f));
        Paint paint = new Paint(1);
        paint.setColor(NUM);
        final Paint paint2 = paint;
        ImageView avatarEditor = new ImageView(context) {
            protected void onDraw(Canvas canvas) {
                if (GroupCreateFinalActivity.this.avatarImage.getImageReceiver().hasNotThumb()) {
                    paint2.setAlpha((int) (85.0f * GroupCreateFinalActivity.this.avatarImage.getImageReceiver().getCurrentAlpha()));
                    canvas.drawCircle((float) (getMeasuredWidth() / 2), (float) (getMeasuredHeight() / 2), (float) AndroidUtilities.m9dp(32.0f), paint2);
                }
                super.onDraw(canvas);
            }
        };
        avatarEditor.setImageResource(R.drawable.menu_camera_av);
        avatarEditor.setScaleType(ScaleType.CENTER);
        this.editTextContainer.addView(avatarEditor, LayoutHelper.createFrame(64, 64.0f, (LocaleController.isRTL ? 5 : 3) | 48, LocaleController.isRTL ? 0.0f : 16.0f, 16.0f, LocaleController.isRTL ? 16.0f : 0.0f, 16.0f));
        avatarEditor.setOnClickListener(new GroupCreateFinalActivity$$Lambda$2(this));
        this.editText = new EditTextEmoji((Activity) context, CLASSNAME, this);
        this.editText.setHint(this.chatType == 0 ? LocaleController.getString("EnterGroupNamePlaceholder", R.string.EnterGroupNamePlaceholder) : LocaleController.getString("EnterListName", R.string.EnterListName));
        if (this.nameToSet != null) {
            this.editText.setText(this.nameToSet);
            this.nameToSet = null;
        }
        this.editText.setFilters(new InputFilter[]{new LengthFilter(100)});
        this.editTextContainer.addView(this.editText, LayoutHelper.createFrame(-1, -2.0f, 16, LocaleController.isRTL ? 5.0f : 96.0f, 0.0f, LocaleController.isRTL ? 96.0f : 5.0f, 0.0f));
        LayoutManager linearLayoutManager = new LinearLayoutManager(context, 1, false);
        this.listView = new RecyclerListView(context);
        RecyclerView recyclerView = this.listView;
        Adapter groupCreateAdapter = new GroupCreateAdapter(context);
        this.adapter = groupCreateAdapter;
        recyclerView.setAdapter(groupCreateAdapter);
        this.listView.setLayoutManager(linearLayoutManager);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setVerticalScrollbarPosition(LocaleController.isRTL ? 1 : 2);
        this.listView.addItemDecoration(new GroupCreateDividerItemDecoration());
        CLASSNAME.addView(this.listView, LayoutHelper.createLinear(-1, -1));
        this.listView.setOnScrollListener(new CLASSNAME());
        this.floatingButtonContainer = new FrameLayout(context);
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.m9dp(56.0f), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
        if (VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.ACTION_BAR_VIDEO_EDIT_COLOR, Mode.MULTIPLY));
            Drawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.m9dp(56.0f), AndroidUtilities.m9dp(56.0f));
            drawable = combinedDrawable;
        }
        this.floatingButtonContainer.setBackgroundDrawable(drawable);
        if (VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{16842919}, ObjectAnimator.ofFloat(this.floatingButtonIcon, "translationZ", new float[]{(float) AndroidUtilities.m9dp(2.0f), (float) AndroidUtilities.m9dp(4.0f)}).setDuration(200));
            animator.addState(new int[0], ObjectAnimator.ofFloat(this.floatingButtonIcon, "translationZ", new float[]{(float) AndroidUtilities.m9dp(4.0f), (float) AndroidUtilities.m9dp(2.0f)}).setDuration(200));
            this.floatingButtonContainer.setStateListAnimator(animator);
            this.floatingButtonContainer.setOutlineProvider(new CLASSNAME());
        }
        View view2 = this.floatingButtonContainer;
        int i2 = VERSION.SDK_INT >= 21 ? 56 : 60;
        if (VERSION.SDK_INT >= 21) {
            f3 = 56.0f;
        } else {
            f3 = 60.0f;
        }
        if (LocaleController.isRTL) {
            i = 3;
        } else {
            i = 5;
        }
        i |= 80;
        if (LocaleController.isRTL) {
            f = 14.0f;
        } else {
            f = 0.0f;
        }
        if (LocaleController.isRTL) {
            f2 = 0.0f;
        } else {
            f2 = 14.0f;
        }
        CLASSNAME.addView(view2, LayoutHelper.createFrame(i2, f3, i, f, 0.0f, f2, 14.0f));
        this.floatingButtonContainer.setOnClickListener(new GroupCreateFinalActivity$$Lambda$3(this));
        this.floatingButtonIcon = new ImageView(context);
        this.floatingButtonIcon.setScaleType(ScaleType.CENTER);
        this.floatingButtonIcon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), Mode.MULTIPLY));
        this.floatingButtonIcon.setImageResource(R.drawable.checkbig);
        this.floatingButtonContainer.addView(this.floatingButtonIcon, LayoutHelper.createFrame(VERSION.SDK_INT >= 21 ? 56 : 60, VERSION.SDK_INT >= 21 ? 56.0f : 60.0f));
        this.progressView = new ContextProgressView(context, 1);
        this.progressView.setVisibility(4);
        this.floatingButtonContainer.addView(this.progressView, LayoutHelper.createFrame(-1, -1.0f));
        return this.fragmentView;
    }

    final /* synthetic */ void lambda$createView$3$GroupCreateFinalActivity(View view) {
        this.imageUpdater.openMenu(this.avatar != null, new GroupCreateFinalActivity$$Lambda$6(this));
    }

    final /* synthetic */ void lambda$null$2$GroupCreateFinalActivity() {
        this.avatar = null;
        this.uploadedAvatar = null;
        this.avatarImage.setImage(this.avatar, "50_50", this.avatarDrawable, null);
    }

    final /* synthetic */ void lambda$createView$4$GroupCreateFinalActivity(View view) {
        if (!this.donePressed) {
            if (this.editText.length() == 0) {
                Vibrator v = (Vibrator) getParentActivity().getSystemService("vibrator");
                if (v != null) {
                    v.vibrate(200);
                }
                AndroidUtilities.shakeView(this.editText, 2.0f, 0);
                return;
            }
            this.donePressed = true;
            AndroidUtilities.hideKeyboard(this.editText);
            this.editText.setEnabled(false);
            if (this.imageUpdater.uploadingImage != null) {
                this.createAfterUpload = true;
                return;
            }
            showEditDoneProgress(true);
            this.reqId = MessagesController.getInstance(this.currentAccount).createChat(this.editText.getText().toString(), this.selectedContacts, null, this.chatType, this);
        }
    }

    public void didUploadedPhoto(InputFile file, PhotoSize photoSize, TL_secureFile secureFile) {
        AndroidUtilities.runOnUIThread(new GroupCreateFinalActivity$$Lambda$4(this, file, photoSize));
    }

    final /* synthetic */ void lambda$didUploadedPhoto$5$GroupCreateFinalActivity(InputFile file, PhotoSize photoSize) {
        this.uploadedAvatar = file;
        this.avatar = photoSize.location;
        this.avatarImage.setImage(this.avatar, "50_50", this.avatarDrawable, null);
        if (this.createAfterUpload) {
            MessagesController.getInstance(this.currentAccount).createChat(this.editText.getText().toString(), this.selectedContacts, null, this.chatType, this);
        }
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        this.imageUpdater.onActivityResult(requestCode, resultCode, data);
    }

    public void saveSelfArgs(Bundle args) {
        if (!(this.imageUpdater == null || this.imageUpdater.currentPicturePath == null)) {
            args.putString("path", this.imageUpdater.currentPicturePath);
        }
        if (this.editText != null) {
            String text = this.editText.getText().toString();
            if (text != null && text.length() != 0) {
                args.putString("nameTextView", text);
            }
        }
    }

    public void restoreSelfArgs(Bundle args) {
        if (this.imageUpdater != null) {
            this.imageUpdater.currentPicturePath = args.getString("path");
        }
        String text = args.getString("nameTextView");
        if (text == null) {
            return;
        }
        if (this.editText != null) {
            this.editText.setText(text);
        } else {
            this.nameToSet = text;
        }
    }

    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            this.editText.openKeyboard();
        }
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            if (this.listView != null) {
                int mask = ((Integer) args[0]).intValue();
                if ((mask & 2) != 0 || (mask & 1) != 0 || (mask & 4) != 0) {
                    int count = this.listView.getChildCount();
                    for (int a = 0; a < count; a++) {
                        View child = this.listView.getChildAt(a);
                        if (child instanceof GroupCreateUserCell) {
                            ((GroupCreateUserCell) child).update(mask);
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.chatDidFailCreate) {
            this.reqId = 0;
            this.donePressed = false;
            showEditDoneProgress(false);
            if (this.editText != null) {
                this.editText.setEnabled(true);
            }
        } else if (id == NotificationCenter.chatDidCreated) {
            this.reqId = 0;
            int chat_id = ((Integer) args[0]).intValue();
            NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
            Bundle args2 = new Bundle();
            args2.putInt("chat_id", chat_id);
            presentFragment(new ChatActivity(args2), true);
            if (this.uploadedAvatar != null) {
                MessagesController.getInstance(this.currentAccount).changeChatAvatar(chat_id, this.uploadedAvatar);
            }
        }
    }

    private void showEditDoneProgress(final boolean show) {
        if (this.doneItemAnimation != null) {
            this.doneItemAnimation.cancel();
        }
        this.doneItemAnimation = new AnimatorSet();
        AnimatorSet animatorSet;
        Animator[] animatorArr;
        if (show) {
            this.progressView.setVisibility(0);
            this.floatingButtonContainer.setEnabled(false);
            animatorSet = this.doneItemAnimation;
            animatorArr = new Animator[6];
            animatorArr[0] = ObjectAnimator.ofFloat(this.floatingButtonIcon, "scaleX", new float[]{0.1f});
            animatorArr[1] = ObjectAnimator.ofFloat(this.floatingButtonIcon, "scaleY", new float[]{0.1f});
            animatorArr[2] = ObjectAnimator.ofFloat(this.floatingButtonIcon, "alpha", new float[]{0.0f});
            animatorArr[3] = ObjectAnimator.ofFloat(this.progressView, "scaleX", new float[]{1.0f});
            animatorArr[4] = ObjectAnimator.ofFloat(this.progressView, "scaleY", new float[]{1.0f});
            animatorArr[5] = ObjectAnimator.ofFloat(this.progressView, "alpha", new float[]{1.0f});
            animatorSet.playTogether(animatorArr);
        } else {
            this.floatingButtonIcon.setVisibility(0);
            this.floatingButtonContainer.setEnabled(true);
            animatorSet = this.doneItemAnimation;
            animatorArr = new Animator[6];
            animatorArr[0] = ObjectAnimator.ofFloat(this.progressView, "scaleX", new float[]{0.1f});
            animatorArr[1] = ObjectAnimator.ofFloat(this.progressView, "scaleY", new float[]{0.1f});
            animatorArr[2] = ObjectAnimator.ofFloat(this.progressView, "alpha", new float[]{0.0f});
            animatorArr[3] = ObjectAnimator.ofFloat(this.floatingButtonIcon, "scaleX", new float[]{1.0f});
            animatorArr[4] = ObjectAnimator.ofFloat(this.floatingButtonIcon, "scaleY", new float[]{1.0f});
            animatorArr[5] = ObjectAnimator.ofFloat(this.floatingButtonIcon, "alpha", new float[]{1.0f});
            animatorSet.playTogether(animatorArr);
        }
        this.doneItemAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (GroupCreateFinalActivity.this.doneItemAnimation != null && GroupCreateFinalActivity.this.doneItemAnimation.equals(animation)) {
                    if (show) {
                        GroupCreateFinalActivity.this.floatingButtonIcon.setVisibility(4);
                    } else {
                        GroupCreateFinalActivity.this.progressView.setVisibility(4);
                    }
                }
            }

            public void onAnimationCancel(Animator animation) {
                if (GroupCreateFinalActivity.this.doneItemAnimation != null && GroupCreateFinalActivity.this.doneItemAnimation.equals(animation)) {
                    GroupCreateFinalActivity.this.doneItemAnimation = null;
                }
            }
        });
        this.doneItemAnimation.setDuration(150);
        this.doneItemAnimation.start();
    }

    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescriptionDelegate cellDelegate = new GroupCreateFinalActivity$$Lambda$5(this);
        r10 = new ThemeDescription[34];
        r10[10] = new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider);
        r10[11] = new ThemeDescription(this.editText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        r10[12] = new ThemeDescription(this.editText, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_groupcreate_hintText);
        r10[13] = new ThemeDescription(this.editText, ThemeDescription.FLAG_CURSORCOLOR, null, null, null, null, Theme.key_groupcreate_cursor);
        r10[14] = new ThemeDescription(this.editText, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField);
        r10[15] = new ThemeDescription(this.editText, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated);
        r10[16] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GroupCreateSectionCell.class}, null, null, null, Theme.key_graySection);
        r10[17] = new ThemeDescription(this.listView, 0, new Class[]{GroupCreateSectionCell.class}, new String[]{"drawable"}, null, null, null, Theme.key_groupcreate_sectionShadow);
        r10[18] = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateSectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_groupcreate_sectionText);
        r10[19] = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateUserCell.class}, new String[]{"textView"}, null, null, null, Theme.key_groupcreate_sectionText);
        r10[20] = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{GroupCreateUserCell.class}, new String[]{"statusTextView"}, null, null, null, Theme.key_groupcreate_onlineText);
        r10[21] = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{GroupCreateUserCell.class}, new String[]{"statusTextView"}, null, null, null, Theme.key_groupcreate_offlineText);
        r10[22] = new ThemeDescription(this.listView, 0, new Class[]{GroupCreateUserCell.class}, null, new Drawable[]{Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, cellDelegate, Theme.key_avatar_text);
        r10[23] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed);
        r10[24] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange);
        r10[25] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet);
        r10[26] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen);
        r10[27] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan);
        r10[28] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue);
        r10[29] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink);
        r10[30] = new ThemeDescription(this.progressView, 0, null, null, null, null, Theme.key_contextProgressInner2);
        r10[31] = new ThemeDescription(this.progressView, 0, null, null, null, null, Theme.key_contextProgressOuter2);
        r10[32] = new ThemeDescription(this.editText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        r10[33] = new ThemeDescription(this.editText, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText);
        return r10;
    }

    final /* synthetic */ void lambda$getThemeDescriptions$6$GroupCreateFinalActivity() {
        if (this.listView != null) {
            int count = this.listView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = this.listView.getChildAt(a);
                if (child instanceof GroupCreateUserCell) {
                    ((GroupCreateUserCell) child).update(0);
                }
            }
        }
    }
}
