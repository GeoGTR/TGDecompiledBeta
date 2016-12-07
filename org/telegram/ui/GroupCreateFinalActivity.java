package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimatorListenerAdapterProxy;
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
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.messenger.volley.DefaultRetryPolicy;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.GroupCreateSectionCell;
import org.telegram.ui.Cells.GroupCreateUserCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.AvatarUpdater.AvatarUpdaterDelegate;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ContextProgressView;
import org.telegram.ui.Components.GroupCreateDividerItemDecoration;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public class GroupCreateFinalActivity extends BaseFragment implements NotificationCenterDelegate, AvatarUpdaterDelegate {
    private static final int done_button = 1;
    private GroupCreateAdapter adapter;
    private FileLocation avatar;
    private AvatarDrawable avatarDrawable;
    private BackupImageView avatarImage;
    private AvatarUpdater avatarUpdater = new AvatarUpdater();
    private int chatType = 0;
    private boolean createAfterUpload;
    private ActionBarMenuItem doneItem;
    private AnimatorSet doneItemAnimation;
    private boolean donePressed;
    private EditText editText;
    private FrameLayout editTextContainer;
    private RecyclerView listView;
    private String nameToSet;
    private ContextProgressView progressView;
    private int reqId;
    private ArrayList<Integer> selectedContacts;
    private InputFile uploadedAvatar;

    public class GroupCreateAdapter extends Adapter {
        private Context context;

        public class Holder extends ViewHolder {
            public Holder(View itemView) {
                super(itemView);
            }
        }

        public GroupCreateAdapter(Context ctx) {
            this.context = ctx;
        }

        public int getItemCount() {
            return GroupCreateFinalActivity.this.selectedContacts.size() + 1;
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
                    holder.itemView.setUser(MessagesController.getInstance().getUser((Integer) GroupCreateFinalActivity.this.selectedContacts.get(position - 1)), null, null);
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
        this.avatarDrawable = new AvatarDrawable();
    }

    public boolean onFragmentCreate() {
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatDidCreated);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatDidFailCreate);
        this.avatarUpdater.parentFragment = this;
        this.avatarUpdater.delegate = this;
        this.selectedContacts = getArguments().getIntegerArrayList("result");
        final ArrayList<Integer> usersToLoad = new ArrayList();
        for (int a = 0; a < this.selectedContacts.size(); a++) {
            Integer uid = (Integer) this.selectedContacts.get(a);
            if (MessagesController.getInstance().getUser(uid) == null) {
                usersToLoad.add(uid);
            }
        }
        if (!usersToLoad.isEmpty()) {
            final Semaphore semaphore = new Semaphore(0);
            final ArrayList<User> users = new ArrayList();
            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
                public void run() {
                    users.addAll(MessagesStorage.getInstance().getUsers(usersToLoad));
                    semaphore.release();
                }
            });
            try {
                semaphore.acquire();
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }
            if (usersToLoad.size() != users.size() || users.isEmpty()) {
                return false;
            }
            Iterator it = users.iterator();
            while (it.hasNext()) {
                MessagesController.getInstance().putUser((User) it.next(), true);
            }
        }
        return super.onFragmentCreate();
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatDidCreated);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatDidFailCreate);
        this.avatarUpdater.clear();
        if (this.reqId != 0) {
            ConnectionsManager.getInstance().cancelRequest(this.reqId, true);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.adapter != null) {
            this.adapter.notifyDataSetChanged();
        }
    }

    public View createView(Context context) {
        float f;
        float f2;
        this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("NewGroup", R.string.NewGroup));
        this.actionBar.setActionBarMenuOnItemClick(new ActionBarMenuOnItemClick() {
            public void onItemClick(int id) {
                if (id == -1) {
                    GroupCreateFinalActivity.this.finishFragment();
                } else if (id == 1 && !GroupCreateFinalActivity.this.donePressed) {
                    if (GroupCreateFinalActivity.this.editText.length() == 0) {
                        Vibrator v = (Vibrator) GroupCreateFinalActivity.this.getParentActivity().getSystemService("vibrator");
                        if (v != null) {
                            v.vibrate(200);
                        }
                        AndroidUtilities.shakeView(GroupCreateFinalActivity.this.editText, 2.0f, 0);
                        return;
                    }
                    GroupCreateFinalActivity.this.donePressed = true;
                    AndroidUtilities.hideKeyboard(GroupCreateFinalActivity.this.editText);
                    GroupCreateFinalActivity.this.editText.setEnabled(false);
                    if (GroupCreateFinalActivity.this.avatarUpdater.uploadingAvatar != null) {
                        GroupCreateFinalActivity.this.createAfterUpload = true;
                        return;
                    }
                    GroupCreateFinalActivity.this.showEditDoneProgress(true);
                    GroupCreateFinalActivity.this.reqId = MessagesController.getInstance().createChat(GroupCreateFinalActivity.this.editText.getText().toString(), GroupCreateFinalActivity.this.selectedContacts, null, GroupCreateFinalActivity.this.chatType, GroupCreateFinalActivity.this);
                }
            }
        });
        this.doneItem = this.actionBar.createMenu().addItemWithWidth(1, R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        this.progressView = new ContextProgressView(context, 1);
        this.doneItem.addView(this.progressView, LayoutHelper.createFrame(-1, -1.0f));
        this.progressView.setVisibility(4);
        this.fragmentView = new LinearLayout(context) {
            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                boolean result = super.drawChild(canvas, child, drawingTime);
                if (child == GroupCreateFinalActivity.this.listView) {
                    GroupCreateFinalActivity.this.parentLayout.drawHeaderShadow(canvas, GroupCreateFinalActivity.this.editTextContainer.getMeasuredHeight());
                }
                return result;
            }
        };
        LinearLayout linearLayout = this.fragmentView;
        linearLayout.setOrientation(1);
        this.editTextContainer = new FrameLayout(context);
        linearLayout.addView(this.editTextContainer, LayoutHelper.createLinear(-1, -2));
        this.avatarImage = new BackupImageView(context);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(32.0f));
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
        this.avatarDrawable.setDrawPhoto(true);
        this.avatarImage.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (GroupCreateFinalActivity.this.getParentActivity() != null) {
                    Builder builder = new Builder(GroupCreateFinalActivity.this.getParentActivity());
                    builder.setItems(GroupCreateFinalActivity.this.avatar != null ? new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley), LocaleController.getString("DeletePhoto", R.string.DeletePhoto)} : new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley)}, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                GroupCreateFinalActivity.this.avatarUpdater.openCamera();
                            } else if (i == 1) {
                                GroupCreateFinalActivity.this.avatarUpdater.openGallery();
                            } else if (i == 2) {
                                GroupCreateFinalActivity.this.avatar = null;
                                GroupCreateFinalActivity.this.uploadedAvatar = null;
                                GroupCreateFinalActivity.this.avatarImage.setImage(GroupCreateFinalActivity.this.avatar, "50_50", GroupCreateFinalActivity.this.avatarDrawable);
                            }
                        }
                    });
                    GroupCreateFinalActivity.this.showDialog(builder.create());
                }
            }
        });
        this.editText = new EditText(context);
        this.editText.setHint(this.chatType == 0 ? LocaleController.getString("EnterGroupNamePlaceholder", R.string.EnterGroupNamePlaceholder) : LocaleController.getString("EnterListName", R.string.EnterListName));
        if (this.nameToSet != null) {
            this.editText.setText(this.nameToSet);
            this.nameToSet = null;
        }
        this.editText.setMaxLines(4);
        this.editText.setGravity((LocaleController.isRTL ? 5 : 3) | 16);
        this.editText.setTextSize(1, 18.0f);
        this.editText.setHintTextColor(Theme.SHARE_SHEET_EDIT_PLACEHOLDER_TEXT_COLOR);
        this.editText.setImeOptions(268435456);
        this.editText.setInputType(16384);
        this.editText.setPadding(0, 0, 0, AndroidUtilities.dp(8.0f));
        this.editText.setFilters(new InputFilter[]{new LengthFilter(100)});
        AndroidUtilities.clearCursorDrawable(this.editText);
        this.editText.setTextColor(-14606047);
        frameLayout = this.editTextContainer;
        view = this.editText;
        f = LocaleController.isRTL ? 16.0f : 96.0f;
        if (LocaleController.isRTL) {
            f2 = 96.0f;
        } else {
            f2 = 16.0f;
        }
        frameLayout.addView(view, LayoutHelper.createFrame(-1, -2.0f, 16, f, 0.0f, f2, 0.0f));
        this.editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                String obj;
                AvatarDrawable access$1300 = GroupCreateFinalActivity.this.avatarDrawable;
                if (GroupCreateFinalActivity.this.editText.length() > 0) {
                    obj = GroupCreateFinalActivity.this.editText.getText().toString();
                } else {
                    obj = null;
                }
                access$1300.setInfo(5, obj, null, false);
                GroupCreateFinalActivity.this.avatarImage.invalidate();
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, 1, false);
        this.listView = new RecyclerListView(context);
        RecyclerView recyclerView = this.listView;
        Adapter groupCreateAdapter = new GroupCreateAdapter(context);
        this.adapter = groupCreateAdapter;
        recyclerView.setAdapter(groupCreateAdapter);
        this.listView.setLayoutManager(linearLayoutManager);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setVerticalScrollbarPosition(LocaleController.isRTL ? 1 : 2);
        this.listView.addItemDecoration(new GroupCreateDividerItemDecoration());
        linearLayout.addView(this.listView, LayoutHelper.createLinear(-1, -1));
        this.listView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == 1) {
                    AndroidUtilities.hideKeyboard(GroupCreateFinalActivity.this.editText);
                }
            }
        });
        return this.fragmentView;
    }

    public void didUploadedPhoto(final InputFile file, final PhotoSize small, PhotoSize big) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                GroupCreateFinalActivity.this.uploadedAvatar = file;
                GroupCreateFinalActivity.this.avatar = small.location;
                GroupCreateFinalActivity.this.avatarImage.setImage(GroupCreateFinalActivity.this.avatar, "50_50", GroupCreateFinalActivity.this.avatarDrawable);
                if (GroupCreateFinalActivity.this.createAfterUpload) {
                    MessagesController.getInstance().createChat(GroupCreateFinalActivity.this.editText.getText().toString(), GroupCreateFinalActivity.this.selectedContacts, null, GroupCreateFinalActivity.this.chatType, GroupCreateFinalActivity.this);
                }
            }
        });
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        this.avatarUpdater.onActivityResult(requestCode, resultCode, data);
    }

    public void saveSelfArgs(Bundle args) {
        if (!(this.avatarUpdater == null || this.avatarUpdater.currentPicturePath == null)) {
            args.putString("path", this.avatarUpdater.currentPicturePath);
        }
        if (this.editText != null) {
            String text = this.editText.getText().toString();
            if (text != null && text.length() != 0) {
                args.putString("nameTextView", text);
            }
        }
    }

    public void restoreSelfArgs(Bundle args) {
        if (this.avatarUpdater != null) {
            this.avatarUpdater.currentPicturePath = args.getString("path");
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
            this.editText.requestFocus();
            AndroidUtilities.showKeyboard(this.editText);
        }
    }

    public void didReceivedNotification(int id, Object... args) {
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
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
            Bundle args2 = new Bundle();
            args2.putInt("chat_id", chat_id);
            presentFragment(new ChatActivity(args2), true);
            if (this.uploadedAvatar != null) {
                MessagesController.getInstance().changeChatAvatar(chat_id, this.uploadedAvatar);
            }
        }
    }

    private void showEditDoneProgress(final boolean show) {
        if (this.doneItem != null) {
            if (this.doneItemAnimation != null) {
                this.doneItemAnimation.cancel();
            }
            this.doneItemAnimation = new AnimatorSet();
            AnimatorSet animatorSet;
            Animator[] animatorArr;
            if (show) {
                this.progressView.setVisibility(0);
                this.doneItem.setEnabled(false);
                animatorSet = this.doneItemAnimation;
                animatorArr = new Animator[6];
                animatorArr[0] = ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleX", new float[]{0.1f});
                animatorArr[1] = ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleY", new float[]{0.1f});
                animatorArr[2] = ObjectAnimator.ofFloat(this.doneItem.getImageView(), "alpha", new float[]{0.0f});
                animatorArr[3] = ObjectAnimator.ofFloat(this.progressView, "scaleX", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorArr[4] = ObjectAnimator.ofFloat(this.progressView, "scaleY", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorArr[5] = ObjectAnimator.ofFloat(this.progressView, "alpha", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorSet.playTogether(animatorArr);
            } else {
                this.doneItem.getImageView().setVisibility(0);
                this.doneItem.setEnabled(true);
                animatorSet = this.doneItemAnimation;
                animatorArr = new Animator[6];
                animatorArr[0] = ObjectAnimator.ofFloat(this.progressView, "scaleX", new float[]{0.1f});
                animatorArr[1] = ObjectAnimator.ofFloat(this.progressView, "scaleY", new float[]{0.1f});
                animatorArr[2] = ObjectAnimator.ofFloat(this.progressView, "alpha", new float[]{0.0f});
                animatorArr[3] = ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleX", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorArr[4] = ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleY", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorArr[5] = ObjectAnimator.ofFloat(this.doneItem.getImageView(), "alpha", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorSet.playTogether(animatorArr);
            }
            this.doneItemAnimation.addListener(new AnimatorListenerAdapterProxy() {
                public void onAnimationEnd(Animator animation) {
                    if (GroupCreateFinalActivity.this.doneItemAnimation != null && GroupCreateFinalActivity.this.doneItemAnimation.equals(animation)) {
                        if (show) {
                            GroupCreateFinalActivity.this.doneItem.getImageView().setVisibility(4);
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
    }
}
