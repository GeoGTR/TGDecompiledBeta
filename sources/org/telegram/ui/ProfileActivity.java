package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Property;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Keep;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import j$.util.Comparator;
import j$.util.function.Function;
import j$.util.function.ToDoubleFunction;
import j$.util.function.ToIntFunction;
import j$.util.function.ToLongFunction;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$BotInfo;
import org.telegram.tgnet.TLRPC$ChannelLocation;
import org.telegram.tgnet.TLRPC$ChannelParticipant;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$ChatFull;
import org.telegram.tgnet.TLRPC$ChatParticipant;
import org.telegram.tgnet.TLRPC$ChatPhoto;
import org.telegram.tgnet.TLRPC$Dialog;
import org.telegram.tgnet.TLRPC$Document;
import org.telegram.tgnet.TLRPC$EncryptedChat;
import org.telegram.tgnet.TLRPC$FileLocation;
import org.telegram.tgnet.TLRPC$InputFile;
import org.telegram.tgnet.TLRPC$InputPhoto;
import org.telegram.tgnet.TLRPC$Page;
import org.telegram.tgnet.TLRPC$PageBlock;
import org.telegram.tgnet.TLRPC$PageListItem;
import org.telegram.tgnet.TLRPC$PhotoSize;
import org.telegram.tgnet.TLRPC$ReplyMarkup;
import org.telegram.tgnet.TLRPC$TL_channelFull;
import org.telegram.tgnet.TLRPC$TL_channelLocation;
import org.telegram.tgnet.TLRPC$TL_channelParticipantAdmin;
import org.telegram.tgnet.TLRPC$TL_channelParticipantCreator;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsRecent;
import org.telegram.tgnet.TLRPC$TL_channels_channelParticipant;
import org.telegram.tgnet.TLRPC$TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC$TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC$TL_chatAdminRights;
import org.telegram.tgnet.TLRPC$TL_chatBannedRights;
import org.telegram.tgnet.TLRPC$TL_chatChannelParticipant;
import org.telegram.tgnet.TLRPC$TL_chatFull;
import org.telegram.tgnet.TLRPC$TL_chatParticipant;
import org.telegram.tgnet.TLRPC$TL_chatParticipantAdmin;
import org.telegram.tgnet.TLRPC$TL_chatParticipants;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_messages_getStickers;
import org.telegram.tgnet.TLRPC$TL_messages_getWebPage;
import org.telegram.tgnet.TLRPC$TL_messages_stickers;
import org.telegram.tgnet.TLRPC$TL_pageBlockAnchor;
import org.telegram.tgnet.TLRPC$TL_pageBlockList;
import org.telegram.tgnet.TLRPC$TL_pageBlockParagraph;
import org.telegram.tgnet.TLRPC$TL_pageListItemText;
import org.telegram.tgnet.TLRPC$TL_peerNotifySettings;
import org.telegram.tgnet.TLRPC$TL_photos_photo;
import org.telegram.tgnet.TLRPC$TL_photos_uploadProfilePhoto;
import org.telegram.tgnet.TLRPC$TL_userEmpty;
import org.telegram.tgnet.TLRPC$TL_userProfilePhoto;
import org.telegram.tgnet.TLRPC$TL_userProfilePhotoEmpty;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.tgnet.TLRPC$UserFull;
import org.telegram.tgnet.TLRPC$UserProfilePhoto;
import org.telegram.tgnet.TLRPC$UserStatus;
import org.telegram.tgnet.TLRPC$VideoSize;
import org.telegram.tgnet.TLRPC$WebPage;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.SettingsSearchCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChatRightsEditActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatGreetingsView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CrossfadeDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ScamDrawable;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.GroupCreateActivity;
import org.telegram.ui.NotificationsSettingsActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.ProfileActivity;

public class ProfileActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, DialogsActivity.DialogsActivityDelegate, SharedMediaLayout.SharedMediaPreloaderDelegate, ImageUpdater.ImageUpdaterDelegate {
    private Property<ActionBar, Float> ACTIONBAR_HEADER_PROGRESS;
    /* access modifiers changed from: private */
    public final Property<ProfileActivity, Float> HEADER_SHADOW;
    /* access modifiers changed from: private */
    public int addMemberRow;
    /* access modifiers changed from: private */
    public int administratorsRow;
    private boolean allowProfileAnimation;
    /* access modifiers changed from: private */
    public boolean allowPullingDown;
    /* access modifiers changed from: private */
    public ActionBarMenuItem animatingItem;
    private float animationProgress;
    private TLRPC$FileLocation avatar;
    /* access modifiers changed from: private */
    public AnimatorSet avatarAnimation;
    /* access modifiers changed from: private */
    public TLRPC$FileLocation avatarBig;
    private int avatarColor;
    /* access modifiers changed from: private */
    public FrameLayout avatarContainer;
    private AvatarDrawable avatarDrawable;
    /* access modifiers changed from: private */
    public AvatarImageView avatarImage;
    /* access modifiers changed from: private */
    public RadialProgressView avatarProgressView;
    private float avatarScale;
    private float avatarX;
    private float avatarY;
    /* access modifiers changed from: private */
    public ProfileGalleryView avatarsViewPager;
    /* access modifiers changed from: private */
    public PagerIndicatorView avatarsViewPagerIndicatorView;
    /* access modifiers changed from: private */
    public int banFromGroup;
    /* access modifiers changed from: private */
    public int bioRow;
    /* access modifiers changed from: private */
    public int blockedUsersRow;
    /* access modifiers changed from: private */
    public TLRPC$BotInfo botInfo;
    /* access modifiers changed from: private */
    public int bottomPaddingRow;
    /* access modifiers changed from: private */
    public ActionBarMenuItem callItem;
    /* access modifiers changed from: private */
    public boolean callItemVisible;
    private boolean canSearchMembers;
    /* access modifiers changed from: private */
    public int channelInfoRow;
    /* access modifiers changed from: private */
    public TLRPC$ChatFull chatInfo;
    /* access modifiers changed from: private */
    public int chatRow;
    /* access modifiers changed from: private */
    public int chat_id;
    /* access modifiers changed from: private */
    public int clearLogsRow;
    /* access modifiers changed from: private */
    public boolean creatingChat;
    /* access modifiers changed from: private */
    public String currentBio;
    private TLRPC$ChannelParticipant currentChannelParticipant;
    /* access modifiers changed from: private */
    public TLRPC$Chat currentChat;
    /* access modifiers changed from: private */
    public TLRPC$EncryptedChat currentEncryptedChat;
    /* access modifiers changed from: private */
    public float currentExpanAnimatorFracture;
    /* access modifiers changed from: private */
    public int dataRow;
    /* access modifiers changed from: private */
    public int debugHeaderRow;
    /* access modifiers changed from: private */
    public int devicesRow;
    /* access modifiers changed from: private */
    public int devicesSectionRow;
    /* access modifiers changed from: private */
    public long dialog_id;
    /* access modifiers changed from: private */
    public boolean doNotSetForeground;
    /* access modifiers changed from: private */
    public ActionBarMenuItem editItem;
    /* access modifiers changed from: private */
    public boolean editItemVisible;
    /* access modifiers changed from: private */
    public int emptyRow;
    /* access modifiers changed from: private */
    public EmptyTextProgressView emptyView;
    /* access modifiers changed from: private */
    public ValueAnimator expandAnimator;
    private float[] expandAnimatorValues;
    /* access modifiers changed from: private */
    public boolean expandPhoto;
    private float expandProgress;
    /* access modifiers changed from: private */
    public float extraHeight;
    /* access modifiers changed from: private */
    public int faqRow;
    /* access modifiers changed from: private */
    public int filtersRow;
    /* access modifiers changed from: private */
    public boolean fragmentOpened;
    private boolean hasVoiceChatItem;
    /* access modifiers changed from: private */
    public AnimatorSet headerAnimatorSet;
    protected float headerShadowAlpha;
    /* access modifiers changed from: private */
    public AnimatorSet headerShadowAnimatorSet;
    /* access modifiers changed from: private */
    public int helpHeaderRow;
    /* access modifiers changed from: private */
    public int helpSectionCell;
    /* access modifiers changed from: private */
    public ImageUpdater imageUpdater;
    /* access modifiers changed from: private */
    public int infoHeaderRow;
    /* access modifiers changed from: private */
    public int infoSectionRow;
    /* access modifiers changed from: private */
    public float initialAnimationExtraHeight;
    /* access modifiers changed from: private */
    public boolean isBot;
    public boolean isFragmentOpened;
    /* access modifiers changed from: private */
    public boolean isInLandscapeMode;
    private boolean[] isOnline;
    /* access modifiers changed from: private */
    public boolean isPulledDown;
    /* access modifiers changed from: private */
    public int joinRow;
    /* access modifiers changed from: private */
    public int languageRow;
    /* access modifiers changed from: private */
    public int lastMeasuredContentHeight;
    /* access modifiers changed from: private */
    public int lastMeasuredContentWidth;
    /* access modifiers changed from: private */
    public int lastSectionRow;
    /* access modifiers changed from: private */
    public LinearLayoutManager layoutManager;
    /* access modifiers changed from: private */
    public ListAdapter listAdapter;
    /* access modifiers changed from: private */
    public int listContentHeight;
    /* access modifiers changed from: private */
    public RecyclerListView listView;
    /* access modifiers changed from: private */
    public float listViewVelocityY;
    private boolean loadingUsers;
    /* access modifiers changed from: private */
    public int locationRow;
    /* access modifiers changed from: private */
    public Drawable lockIconDrawable;
    /* access modifiers changed from: private */
    public AudioPlayerAlert.ClippingTextViewSwitcher mediaCounterTextView;
    /* access modifiers changed from: private */
    public float mediaHeaderAnimationProgress;
    /* access modifiers changed from: private */
    public boolean mediaHeaderVisible;
    /* access modifiers changed from: private */
    public int membersEndRow;
    /* access modifiers changed from: private */
    public int membersHeaderRow;
    /* access modifiers changed from: private */
    public int membersSectionRow;
    /* access modifiers changed from: private */
    public int membersStartRow;
    private long mergeDialogId;
    /* access modifiers changed from: private */
    public SimpleTextView[] nameTextView;
    private float nameX;
    private float nameY;
    private boolean needSendMessage;
    /* access modifiers changed from: private */
    public int notificationRow;
    /* access modifiers changed from: private */
    public int notificationsDividerRow;
    /* access modifiers changed from: private */
    public int notificationsRow;
    /* access modifiers changed from: private */
    public int numberRow;
    /* access modifiers changed from: private */
    public int numberSectionRow;
    private int onlineCount;
    /* access modifiers changed from: private */
    public SimpleTextView[] onlineTextView;
    private float onlineX;
    private float onlineY;
    /* access modifiers changed from: private */
    public boolean openAnimationInProgress;
    /* access modifiers changed from: private */
    public boolean openingAvatar;
    /* access modifiers changed from: private */
    public ActionBarMenuItem otherItem;
    /* access modifiers changed from: private */
    public int overlayCountVisible;
    /* access modifiers changed from: private */
    public OverlaysView overlaysView;
    /* access modifiers changed from: private */
    public SparseArray<TLRPC$ChatParticipant> participantsMap;
    /* access modifiers changed from: private */
    public int phoneRow;
    /* access modifiers changed from: private */
    public int playProfileAnimation;
    /* access modifiers changed from: private */
    public int policyRow;
    /* access modifiers changed from: private */
    public HashMap<Integer, Integer> positionToOffset;
    private TLRPC$Document preloadedSticker;
    /* access modifiers changed from: private */
    public int privacyRow;
    boolean profileTransitionInProgress;
    private PhotoViewer.PhotoViewerProvider provider;
    /* access modifiers changed from: private */
    public int questionRow;
    private boolean recreateMenuAfterAnimation;
    /* access modifiers changed from: private */
    public Rect rect;
    /* access modifiers changed from: private */
    public int reportRow;
    /* access modifiers changed from: private */
    public boolean reportSpam;
    /* access modifiers changed from: private */
    public int rowCount;
    /* access modifiers changed from: private */
    public ScamDrawable scamDrawable;
    /* access modifiers changed from: private */
    public boolean scrolling;
    /* access modifiers changed from: private */
    public SearchAdapter searchAdapter;
    /* access modifiers changed from: private */
    public ActionBarMenuItem searchItem;
    /* access modifiers changed from: private */
    public RecyclerListView searchListView;
    /* access modifiers changed from: private */
    public boolean searchMode;
    /* access modifiers changed from: private */
    public int searchTransitionOffset;
    private float searchTransitionProgress;
    /* access modifiers changed from: private */
    public Animator searchViewTransition;
    /* access modifiers changed from: private */
    public int secretSettingsSectionRow;
    private int selectedUser;
    /* access modifiers changed from: private */
    public int sendLogsRow;
    /* access modifiers changed from: private */
    public int sendMessageRow;
    /* access modifiers changed from: private */
    public int setAvatarRow;
    /* access modifiers changed from: private */
    public int setAvatarSectionRow;
    /* access modifiers changed from: private */
    public int setUsernameRow;
    /* access modifiers changed from: private */
    public int settingsKeyRow;
    /* access modifiers changed from: private */
    public int settingsSectionRow;
    /* access modifiers changed from: private */
    public int settingsSectionRow2;
    /* access modifiers changed from: private */
    public int settingsTimerRow;
    /* access modifiers changed from: private */
    public SharedMediaLayout sharedMediaLayout;
    /* access modifiers changed from: private */
    public boolean sharedMediaLayoutAttached;
    private SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader;
    /* access modifiers changed from: private */
    public int sharedMediaRow;
    /* access modifiers changed from: private */
    public ArrayList<Integer> sortedUsers;
    /* access modifiers changed from: private */
    public int subscribersRow;
    /* access modifiers changed from: private */
    public int switchBackendRow;
    /* access modifiers changed from: private */
    public TopView topView;
    /* access modifiers changed from: private */
    public boolean transitionAnimationInProress;
    private int transitionIndex;
    /* access modifiers changed from: private */
    public int unblockRow;
    /* access modifiers changed from: private */
    public UndoView undoView;
    /* access modifiers changed from: private */
    public boolean userBlocked;
    /* access modifiers changed from: private */
    public TLRPC$UserFull userInfo;
    /* access modifiers changed from: private */
    public int userInfoRow;
    /* access modifiers changed from: private */
    public int user_id;
    /* access modifiers changed from: private */
    public int usernameRow;
    /* access modifiers changed from: private */
    public boolean usersEndReached;
    /* access modifiers changed from: private */
    public Drawable verifiedCheckDrawable;
    private CrossfadeDrawable verifiedCrossfadeDrawable;
    /* access modifiers changed from: private */
    public Drawable verifiedDrawable;
    /* access modifiers changed from: private */
    public int versionRow;
    /* access modifiers changed from: private */
    public ActionBarMenuItem videoCallItem;
    /* access modifiers changed from: private */
    public boolean videoCallItemVisible;
    /* access modifiers changed from: private */
    public Paint whitePaint;
    private ImageView writeButton;
    /* access modifiers changed from: private */
    public AnimatorSet writeButtonAnimation;

    public /* synthetic */ String getInitialSearchString() {
        return ImageUpdater.ImageUpdaterDelegate.CC.$default$getInitialSearchString(this);
    }

    public class AvatarImageView extends BackupImageView {
        private ImageReceiver.BitmapHolder drawableHolder;
        private float foregroundAlpha;
        private ImageReceiver foregroundImageReceiver = new ImageReceiver(this);
        private final Paint placeholderPaint;
        private final RectF rect = new RectF();

        public AvatarImageView(Context context) {
            super(context);
            Paint paint = new Paint(1);
            this.placeholderPaint = paint;
            paint.setColor(-16777216);
        }

        public void setForegroundImage(ImageLocation imageLocation, String str, Drawable drawable) {
            this.foregroundImageReceiver.setImage(imageLocation, str, drawable, 0, (String) null, (Object) null, 0);
            ImageReceiver.BitmapHolder bitmapHolder = this.drawableHolder;
            if (bitmapHolder != null) {
                bitmapHolder.release();
                this.drawableHolder = null;
            }
        }

        public void setForegroundImageDrawable(ImageReceiver.BitmapHolder bitmapHolder) {
            if (bitmapHolder != null) {
                this.foregroundImageReceiver.setImageBitmap(bitmapHolder.drawable);
            }
            ImageReceiver.BitmapHolder bitmapHolder2 = this.drawableHolder;
            if (bitmapHolder2 != null) {
                bitmapHolder2.release();
                this.drawableHolder = null;
            }
            this.drawableHolder = bitmapHolder;
        }

        public float getForegroundAlpha() {
            return this.foregroundAlpha;
        }

        public void setForegroundAlpha(float f) {
            this.foregroundAlpha = f;
            invalidate();
        }

        public void clearForeground() {
            AnimatedFileDrawable animation = this.foregroundImageReceiver.getAnimation();
            if (animation != null) {
                animation.removeSecondParentView(ProfileActivity.this.avatarImage);
            }
            this.foregroundImageReceiver.clearImage();
            ImageReceiver.BitmapHolder bitmapHolder = this.drawableHolder;
            if (bitmapHolder != null) {
                bitmapHolder.release();
                this.drawableHolder = null;
            }
            this.foregroundAlpha = 0.0f;
            invalidate();
        }

        /* access modifiers changed from: protected */
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.foregroundImageReceiver.onDetachedFromWindow();
            ImageReceiver.BitmapHolder bitmapHolder = this.drawableHolder;
            if (bitmapHolder != null) {
                bitmapHolder.release();
                this.drawableHolder = null;
            }
        }

        /* access modifiers changed from: protected */
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            this.foregroundImageReceiver.onAttachedToWindow();
        }

        public void setRoundRadius(int i) {
            super.setRoundRadius(i);
            this.foregroundImageReceiver.setRoundRadius(i);
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            if (this.foregroundAlpha < 1.0f) {
                this.imageReceiver.setImageCoords(0.0f, 0.0f, (float) getMeasuredWidth(), (float) getMeasuredHeight());
                this.imageReceiver.draw(canvas);
            }
            if (this.foregroundAlpha <= 0.0f) {
                return;
            }
            if (this.foregroundImageReceiver.getDrawable() != null) {
                this.foregroundImageReceiver.setImageCoords(0.0f, 0.0f, (float) getMeasuredWidth(), (float) getMeasuredHeight());
                this.foregroundImageReceiver.setAlpha(this.foregroundAlpha);
                this.foregroundImageReceiver.draw(canvas);
                return;
            }
            this.rect.set(0.0f, 0.0f, (float) getMeasuredWidth(), (float) getMeasuredHeight());
            this.placeholderPaint.setAlpha((int) (this.foregroundAlpha * 255.0f));
            float f = (float) this.foregroundImageReceiver.getRoundRadius()[0];
            canvas.drawRoundRect(this.rect, f, f, this.placeholderPaint);
        }

        public void invalidate() {
            super.invalidate();
            if (ProfileActivity.this.avatarsViewPager != null) {
                ProfileActivity.this.avatarsViewPager.getCurrentItemView();
                ProfileActivity.this.avatarsViewPager.invalidate();
            }
        }
    }

    private class TopView extends View {
        private int currentColor;
        private Paint paint = new Paint();

        public TopView(Context context) {
            super(context);
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int i, int i2) {
            setMeasuredDimension(View.MeasureSpec.getSize(i), View.MeasureSpec.getSize(i) + AndroidUtilities.dp(3.0f));
        }

        public void setBackgroundColor(int i) {
            if (i != this.currentColor) {
                this.currentColor = i;
                this.paint.setColor(i);
                invalidate();
            }
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            float access$800 = ProfileActivity.this.extraHeight + ((float) (ActionBar.getCurrentActionBarHeight() + (ProfileActivity.this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0))) + ((float) ProfileActivity.this.searchTransitionOffset);
            int access$1000 = (int) ((1.0f - ProfileActivity.this.mediaHeaderAnimationProgress) * access$800);
            if (access$1000 != 0) {
                this.paint.setColor(this.currentColor);
                canvas.drawRect(0.0f, 0.0f, (float) getMeasuredWidth(), (float) access$1000, this.paint);
            }
            float f = (float) access$1000;
            if (f != access$800) {
                this.paint.setColor(Theme.getColor("windowBackgroundWhite"));
                canvas.drawRect(0.0f, f, (float) getMeasuredWidth(), access$800, this.paint);
            }
            if (ProfileActivity.this.parentLayout != null) {
                ProfileActivity.this.parentLayout.drawHeaderShadow(canvas, (int) (ProfileActivity.this.headerShadowAlpha * 255.0f), (int) access$800);
            }
        }
    }

    private class OverlaysView extends View implements ProfileGalleryView.Callback {
        private float alpha;
        private float[] alphas;
        /* access modifiers changed from: private */
        public final ValueAnimator animator;
        private final float[] animatorValues;
        private final Paint backgroundPaint;
        private final Paint barPaint;
        private final GradientDrawable bottomOverlayGradient;
        private final Rect bottomOverlayRect;
        private float currentAnimationValue;
        private int currentLoadingAnimationDirection;
        private float currentLoadingAnimationProgress;
        private float currentProgress;
        /* access modifiers changed from: private */
        public boolean isOverlaysVisible;
        private long lastTime;
        private final float[] pressedOverlayAlpha;
        private final GradientDrawable[] pressedOverlayGradient;
        private final boolean[] pressedOverlayVisible;
        private int previousSelectedPotision;
        private float previousSelectedProgress;
        private final RectF rect;
        private final Paint selectedBarPaint;
        private int selectedPosition;
        private final int statusBarHeight;
        private final GradientDrawable topOverlayGradient;
        private final Rect topOverlayRect;

        public OverlaysView(Context context) {
            super(context);
            this.statusBarHeight = (!ProfileActivity.this.actionBar.getOccupyStatusBar() || ProfileActivity.this.inBubbleMode) ? 0 : AndroidUtilities.statusBarHeight;
            this.topOverlayRect = new Rect();
            this.bottomOverlayRect = new Rect();
            this.rect = new RectF();
            this.animatorValues = new float[]{0.0f, 1.0f};
            this.pressedOverlayGradient = new GradientDrawable[2];
            this.pressedOverlayVisible = new boolean[2];
            this.pressedOverlayAlpha = new float[2];
            this.alpha = 0.0f;
            this.alphas = null;
            this.previousSelectedPotision = -1;
            this.currentLoadingAnimationDirection = 1;
            setVisibility(8);
            Paint paint = new Paint(1);
            this.barPaint = paint;
            paint.setColor(NUM);
            Paint paint2 = new Paint(1);
            this.selectedBarPaint = paint2;
            paint2.setColor(-1);
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{NUM, 0});
            this.topOverlayGradient = gradientDrawable;
            gradientDrawable.setShape(0);
            GradientDrawable gradientDrawable2 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{NUM, 0});
            this.bottomOverlayGradient = gradientDrawable2;
            gradientDrawable2.setShape(0);
            int i = 0;
            while (i < 2) {
                this.pressedOverlayGradient[i] = new GradientDrawable(i == 0 ? GradientDrawable.Orientation.LEFT_RIGHT : GradientDrawable.Orientation.RIGHT_LEFT, new int[]{NUM, 0});
                this.pressedOverlayGradient[i].setShape(0);
                i++;
            }
            Paint paint3 = new Paint(1);
            this.backgroundPaint = paint3;
            paint3.setColor(-16777216);
            paint3.setAlpha(66);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.animator = ofFloat;
            ofFloat.setDuration(250);
            ofFloat.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ProfileActivity.OverlaysView.this.lambda$new$0$ProfileActivity$OverlaysView(valueAnimator);
                }
            });
            ofFloat.addListener(new AnimatorListenerAdapter(ProfileActivity.this) {
                public void onAnimationEnd(Animator animator) {
                    if (!OverlaysView.this.isOverlaysVisible) {
                        OverlaysView.this.setVisibility(8);
                    }
                }

                public void onAnimationStart(Animator animator) {
                    OverlaysView.this.setVisibility(0);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$0 */
        public /* synthetic */ void lambda$new$0$ProfileActivity$OverlaysView(ValueAnimator valueAnimator) {
            float[] fArr = this.animatorValues;
            float animatedFraction = valueAnimator.getAnimatedFraction();
            this.currentAnimationValue = animatedFraction;
            setAlphaValue(AndroidUtilities.lerp(fArr, animatedFraction), true);
        }

        public void saveCurrentPageProgress() {
            this.previousSelectedProgress = this.currentProgress;
            this.previousSelectedPotision = this.selectedPosition;
            this.currentLoadingAnimationProgress = 0.0f;
            this.currentLoadingAnimationDirection = 1;
        }

        public void setAlphaValue(float f, boolean z) {
            if (Build.VERSION.SDK_INT > 18) {
                int i = (int) (255.0f * f);
                this.topOverlayGradient.setAlpha(i);
                this.bottomOverlayGradient.setAlpha(i);
                this.backgroundPaint.setAlpha((int) (66.0f * f));
                this.barPaint.setAlpha((int) (85.0f * f));
                this.selectedBarPaint.setAlpha(i);
                this.alpha = f;
            } else {
                setAlpha(f);
            }
            if (!z) {
                this.currentAnimationValue = f;
            }
            invalidate();
        }

        public boolean isOverlaysVisible() {
            return this.isOverlaysVisible;
        }

        public void setOverlaysVisible() {
            this.isOverlaysVisible = true;
            setVisibility(0);
        }

        public void setOverlaysVisible(boolean z, float f) {
            if (z != this.isOverlaysVisible) {
                this.isOverlaysVisible = z;
                this.animator.cancel();
                float lerp = AndroidUtilities.lerp(this.animatorValues, this.currentAnimationValue);
                float f2 = 1.0f;
                if (z) {
                    this.animator.setDuration((long) (((1.0f - lerp) * 250.0f) / f));
                } else {
                    this.animator.setDuration((long) ((250.0f * lerp) / f));
                }
                float[] fArr = this.animatorValues;
                fArr[0] = lerp;
                if (!z) {
                    f2 = 0.0f;
                }
                fArr[1] = f2;
                this.animator.start();
            }
        }

        /* access modifiers changed from: protected */
        public void onSizeChanged(int i, int i2, int i3, int i4) {
            int currentActionBarHeight = this.statusBarHeight + ActionBar.getCurrentActionBarHeight();
            this.topOverlayRect.set(0, 0, i, (int) (((float) currentActionBarHeight) * 0.5f));
            this.bottomOverlayRect.set(0, (int) (((float) i2) - (((float) AndroidUtilities.dp(72.0f)) * 0.5f)), i, i2);
            this.topOverlayGradient.setBounds(0, this.topOverlayRect.bottom, i, currentActionBarHeight + AndroidUtilities.dp(16.0f));
            this.bottomOverlayGradient.setBounds(0, (i2 - AndroidUtilities.dp(72.0f)) - AndroidUtilities.dp(24.0f), i, this.bottomOverlayRect.top);
            int i5 = i / 5;
            this.pressedOverlayGradient[0].setBounds(0, 0, i5, i2);
            this.pressedOverlayGradient[1].setBounds(i - i5, 0, i, i2);
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:68:0x0222  */
        /* JADX WARNING: Removed duplicated region for block: B:72:0x024a  */
        /* JADX WARNING: Removed duplicated region for block: B:75:0x0264  */
        /* JADX WARNING: Removed duplicated region for block: B:76:0x0267  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onDraw(android.graphics.Canvas r22) {
            /*
                r21 = this;
                r0 = r21
                r1 = r22
                r3 = 0
            L_0x0005:
                r4 = 1132396544(0x437var_, float:255.0)
                r5 = 2
                r6 = 0
                if (r3 >= r5) goto L_0x0029
                float[] r5 = r0.pressedOverlayAlpha
                r7 = r5[r3]
                int r6 = (r7 > r6 ? 1 : (r7 == r6 ? 0 : -1))
                if (r6 <= 0) goto L_0x0026
                android.graphics.drawable.GradientDrawable[] r6 = r0.pressedOverlayGradient
                r6 = r6[r3]
                r5 = r5[r3]
                float r5 = r5 * r4
                int r4 = (int) r5
                r6.setAlpha(r4)
                android.graphics.drawable.GradientDrawable[] r4 = r0.pressedOverlayGradient
                r4 = r4[r3]
                r4.draw(r1)
            L_0x0026:
                int r3 = r3 + 1
                goto L_0x0005
            L_0x0029:
                android.graphics.drawable.GradientDrawable r3 = r0.topOverlayGradient
                r3.draw(r1)
                android.graphics.drawable.GradientDrawable r3 = r0.bottomOverlayGradient
                r3.draw(r1)
                android.graphics.Rect r3 = r0.topOverlayRect
                android.graphics.Paint r7 = r0.backgroundPaint
                r1.drawRect(r3, r7)
                android.graphics.Rect r3 = r0.bottomOverlayRect
                android.graphics.Paint r7 = r0.backgroundPaint
                r1.drawRect(r3, r7)
                org.telegram.ui.ProfileActivity r3 = org.telegram.ui.ProfileActivity.this
                org.telegram.ui.Components.ProfileGalleryView r3 = r3.avatarsViewPager
                int r3 = r3.getRealCount()
                org.telegram.ui.ProfileActivity r7 = org.telegram.ui.ProfileActivity.this
                org.telegram.ui.Components.ProfileGalleryView r7 = r7.avatarsViewPager
                int r7 = r7.getRealPosition()
                r0.selectedPosition = r7
                float[] r7 = r0.alphas
                if (r7 == 0) goto L_0x005e
                int r7 = r7.length
                if (r7 == r3) goto L_0x0065
            L_0x005e:
                float[] r7 = new float[r3]
                r0.alphas = r7
                java.util.Arrays.fill(r7, r6)
            L_0x0065:
                long r7 = android.os.SystemClock.elapsedRealtime()
                long r9 = r0.lastTime
                long r9 = r7 - r9
                r11 = 0
                int r13 = (r9 > r11 ? 1 : (r9 == r11 ? 0 : -1))
                if (r13 < 0) goto L_0x0079
                r11 = 20
                int r13 = (r9 > r11 ? 1 : (r9 == r11 ? 0 : -1))
                if (r13 <= 0) goto L_0x007b
            L_0x0079:
                r9 = 17
            L_0x007b:
                r0.lastTime = r7
                r8 = 1
                r11 = 1065353216(0x3var_, float:1.0)
                if (r3 <= r8) goto L_0x02dd
                r12 = 20
                if (r3 > r12) goto L_0x02dd
                org.telegram.ui.ProfileActivity r12 = org.telegram.ui.ProfileActivity.this
                int r12 = r12.overlayCountVisible
                r13 = 3
                if (r12 != 0) goto L_0x0097
                r0.alpha = r6
                org.telegram.ui.ProfileActivity r12 = org.telegram.ui.ProfileActivity.this
                int unused = r12.overlayCountVisible = r13
                goto L_0x00a6
            L_0x0097:
                org.telegram.ui.ProfileActivity r12 = org.telegram.ui.ProfileActivity.this
                int r12 = r12.overlayCountVisible
                if (r12 != r8) goto L_0x00a6
                r0.alpha = r6
                org.telegram.ui.ProfileActivity r12 = org.telegram.ui.ProfileActivity.this
                int unused = r12.overlayCountVisible = r5
            L_0x00a6:
                org.telegram.ui.ProfileActivity r12 = org.telegram.ui.ProfileActivity.this
                int r12 = r12.overlayCountVisible
                r14 = 1118437376(0x42aa0000, float:85.0)
                if (r12 != r5) goto L_0x00c4
                android.graphics.Paint r12 = r0.barPaint
                float r15 = r0.alpha
                float r15 = r15 * r14
                int r15 = (int) r15
                r12.setAlpha(r15)
                android.graphics.Paint r12 = r0.selectedBarPaint
                float r15 = r0.alpha
                float r15 = r15 * r4
                int r15 = (int) r15
                r12.setAlpha(r15)
            L_0x00c4:
                int r12 = r21.getMeasuredWidth()
                r15 = 1092616192(0x41200000, float:10.0)
                int r15 = org.telegram.messenger.AndroidUtilities.dp(r15)
                int r12 = r12 - r15
                int r15 = r3 + -1
                int r15 = r15 * 2
                float r15 = (float) r15
                int r15 = org.telegram.messenger.AndroidUtilities.dp(r15)
                int r12 = r12 - r15
                int r12 = r12 / r3
                r15 = 1082130432(0x40800000, float:4.0)
                int r15 = org.telegram.messenger.AndroidUtilities.dp(r15)
                int r2 = android.os.Build.VERSION.SDK_INT
                r8 = 21
                if (r2 < r8) goto L_0x00f1
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                boolean r2 = r2.inBubbleMode
                if (r2 != 0) goto L_0x00f1
                int r2 = org.telegram.messenger.AndroidUtilities.statusBarHeight
                goto L_0x00f2
            L_0x00f1:
                r2 = 0
            L_0x00f2:
                int r15 = r15 + r2
                r2 = 0
                r8 = 0
            L_0x00f5:
                r16 = 1140457472(0x43fa0000, float:500.0)
                if (r2 >= r3) goto L_0x027a
                int r17 = r2 * 2
                int r7 = r17 + 5
                float r7 = (float) r7
                int r7 = org.telegram.messenger.AndroidUtilities.dp(r7)
                int r17 = r12 * r2
                int r7 = r7 + r17
                r17 = 85
                int r5 = r0.previousSelectedPotision
                r18 = 80
                r19 = 1073741824(0x40000000, float:2.0)
                if (r2 != r5) goto L_0x0169
                float r5 = r0.previousSelectedProgress
                float r5 = r5 - r11
                float r5 = java.lang.Math.abs(r5)
                r20 = 953267991(0x38d1b717, float:1.0E-4)
                int r5 = (r5 > r20 ? 1 : (r5 == r20 ? 0 : -1))
                if (r5 <= 0) goto L_0x0169
                float r5 = r0.previousSelectedProgress
                r22.save()
                float r8 = (float) r7
                float r4 = (float) r12
                float r4 = r4 * r5
                float r4 = r4 + r8
                float r13 = (float) r15
                int r6 = r7 + r12
                float r6 = (float) r6
                int r16 = org.telegram.messenger.AndroidUtilities.dp(r19)
                int r11 = r15 + r16
                float r11 = (float) r11
                r1.clipRect(r4, r13, r6, r11)
                android.graphics.RectF r4 = r0.rect
                int r11 = org.telegram.messenger.AndroidUtilities.dp(r19)
                int r11 = r11 + r15
                float r11 = (float) r11
                r4.set(r8, r13, r6, r11)
                android.graphics.Paint r4 = r0.barPaint
                float r6 = r0.alpha
                float r6 = r6 * r14
                int r6 = (int) r6
                r4.setAlpha(r6)
                android.graphics.RectF r4 = r0.rect
                r6 = 1065353216(0x3var_, float:1.0)
                int r8 = org.telegram.messenger.AndroidUtilities.dp(r6)
                float r8 = (float) r8
                int r11 = org.telegram.messenger.AndroidUtilities.dp(r6)
                float r6 = (float) r11
                android.graphics.Paint r11 = r0.barPaint
                r1.drawRoundRect(r4, r8, r6, r11)
                r22.restore()
                r13 = r15
                r4 = 80
                r6 = 1118437376(0x42aa0000, float:85.0)
            L_0x0166:
                r8 = 1
                goto L_0x020d
            L_0x0169:
                int r4 = r0.selectedPosition
                if (r2 != r4) goto L_0x0206
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                org.telegram.ui.Components.ProfileGalleryView r4 = r4.avatarsViewPager
                boolean r4 = r4.isCurrentItemVideo()
                if (r4 == 0) goto L_0x01fe
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                org.telegram.ui.Components.ProfileGalleryView r4 = r4.avatarsViewPager
                float r5 = r4.getCurrentItemProgress()
                r0.currentProgress = r5
                r4 = 0
                int r6 = (r5 > r4 ? 1 : (r5 == r4 ? 0 : -1))
                if (r6 > 0) goto L_0x0196
                org.telegram.ui.ProfileActivity r6 = org.telegram.ui.ProfileActivity.this
                org.telegram.ui.Components.ProfileGalleryView r6 = r6.avatarsViewPager
                boolean r6 = r6.isLoadingCurrentVideo()
                if (r6 != 0) goto L_0x019c
            L_0x0196:
                float r6 = r0.currentLoadingAnimationProgress
                int r6 = (r6 > r4 ? 1 : (r6 == r4 ? 0 : -1))
                if (r6 <= 0) goto L_0x01c3
            L_0x019c:
                float r4 = r0.currentLoadingAnimationProgress
                int r6 = r0.currentLoadingAnimationDirection
                r13 = r15
                long r14 = (long) r6
                long r14 = r14 * r9
                float r8 = (float) r14
                float r8 = r8 / r16
                float r4 = r4 + r8
                r0.currentLoadingAnimationProgress = r4
                r8 = 1065353216(0x3var_, float:1.0)
                int r14 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1))
                if (r14 <= 0) goto L_0x01b7
                r0.currentLoadingAnimationProgress = r8
                int r6 = r6 * -1
                r0.currentLoadingAnimationDirection = r6
                goto L_0x01c4
            L_0x01b7:
                r8 = 0
                int r4 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1))
                if (r4 > 0) goto L_0x01c4
                r0.currentLoadingAnimationProgress = r8
                int r6 = r6 * -1
                r0.currentLoadingAnimationDirection = r6
                goto L_0x01c4
            L_0x01c3:
                r13 = r15
            L_0x01c4:
                android.graphics.RectF r4 = r0.rect
                float r6 = (float) r7
                float r8 = (float) r13
                int r14 = r7 + r12
                float r14 = (float) r14
                int r15 = org.telegram.messenger.AndroidUtilities.dp(r19)
                int r15 = r15 + r13
                float r15 = (float) r15
                r4.set(r6, r8, r14, r15)
                android.graphics.Paint r4 = r0.barPaint
                r6 = 1111490560(0x42400000, float:48.0)
                float r8 = r0.currentLoadingAnimationProgress
                float r8 = r8 * r6
                r6 = 1118437376(0x42aa0000, float:85.0)
                float r8 = r8 + r6
                float r11 = r0.alpha
                float r8 = r8 * r11
                int r8 = (int) r8
                r4.setAlpha(r8)
                android.graphics.RectF r4 = r0.rect
                r11 = 1065353216(0x3var_, float:1.0)
                int r8 = org.telegram.messenger.AndroidUtilities.dp(r11)
                float r8 = (float) r8
                int r14 = org.telegram.messenger.AndroidUtilities.dp(r11)
                float r14 = (float) r14
                android.graphics.Paint r15 = r0.barPaint
                r1.drawRoundRect(r4, r8, r14, r15)
                r4 = 80
                goto L_0x0166
            L_0x01fe:
                r13 = r15
                r6 = 1118437376(0x42aa0000, float:85.0)
                r11 = 1065353216(0x3var_, float:1.0)
                r0.currentProgress = r11
                goto L_0x0209
            L_0x0206:
                r13 = r15
                r6 = 1118437376(0x42aa0000, float:85.0)
            L_0x0209:
                r4 = 85
                r5 = 1065353216(0x3var_, float:1.0)
            L_0x020d:
                android.graphics.RectF r11 = r0.rect
                float r7 = (float) r7
                float r14 = (float) r13
                float r15 = (float) r12
                float r15 = r15 * r5
                float r15 = r15 + r7
                int r5 = org.telegram.messenger.AndroidUtilities.dp(r19)
                int r5 = r5 + r13
                float r5 = (float) r5
                r11.set(r7, r14, r15, r5)
                int r5 = r0.selectedPosition
                if (r2 == r5) goto L_0x024a
                org.telegram.ui.ProfileActivity r5 = org.telegram.ui.ProfileActivity.this
                int r5 = r5.overlayCountVisible
                r7 = 3
                if (r5 != r7) goto L_0x0247
                android.graphics.Paint r5 = r0.barPaint
                float r4 = (float) r4
                org.telegram.ui.Components.CubicBezierInterpolator r7 = org.telegram.ui.Components.CubicBezierInterpolator.EASE_BOTH
                float[] r11 = r0.alphas
                r11 = r11[r2]
                float r7 = r7.getInterpolation(r11)
                r11 = 1132396544(0x437var_, float:255.0)
                float r4 = org.telegram.messenger.AndroidUtilities.lerp(r4, r11, r7)
                float r7 = r0.alpha
                float r4 = r4 * r7
                int r4 = (int) r4
                r5.setAlpha(r4)
                goto L_0x0252
            L_0x0247:
                r11 = 1132396544(0x437var_, float:255.0)
                goto L_0x0252
            L_0x024a:
                r11 = 1132396544(0x437var_, float:255.0)
                float[] r4 = r0.alphas
                r5 = 1061158912(0x3var_, float:0.75)
                r4[r2] = r5
            L_0x0252:
                android.graphics.RectF r4 = r0.rect
                r5 = 1065353216(0x3var_, float:1.0)
                int r7 = org.telegram.messenger.AndroidUtilities.dp(r5)
                float r7 = (float) r7
                int r14 = org.telegram.messenger.AndroidUtilities.dp(r5)
                float r5 = (float) r14
                int r14 = r0.selectedPosition
                if (r2 != r14) goto L_0x0267
                android.graphics.Paint r14 = r0.selectedBarPaint
                goto L_0x0269
            L_0x0267:
                android.graphics.Paint r14 = r0.barPaint
            L_0x0269:
                r1.drawRoundRect(r4, r7, r5, r14)
                int r2 = r2 + 1
                r15 = r13
                r4 = 1132396544(0x437var_, float:255.0)
                r5 = 2
                r6 = 0
                r11 = 1065353216(0x3var_, float:1.0)
                r13 = 3
                r14 = 1118437376(0x42aa0000, float:85.0)
                goto L_0x00f5
            L_0x027a:
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                int r1 = r1.overlayCountVisible
                r2 = 2
                if (r1 != r2) goto L_0x02a1
                float r1 = r0.alpha
                r2 = 1065353216(0x3var_, float:1.0)
                int r3 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
                if (r3 >= 0) goto L_0x029a
                float r3 = (float) r9
                r4 = 1127481344(0x43340000, float:180.0)
                float r3 = r3 / r4
                float r1 = r1 + r3
                r0.alpha = r1
                int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
                if (r1 <= 0) goto L_0x0298
                r0.alpha = r2
            L_0x0298:
                r8 = 1
                goto L_0x02de
            L_0x029a:
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                r2 = 3
                int unused = r1.overlayCountVisible = r2
                goto L_0x02de
            L_0x02a1:
                r2 = 3
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                int r1 = r1.overlayCountVisible
                if (r1 != r2) goto L_0x02de
                r1 = 0
            L_0x02ab:
                float[] r2 = r0.alphas
                int r3 = r2.length
                if (r1 >= r3) goto L_0x02de
                int r3 = r0.selectedPosition
                r4 = -1
                if (r1 == r3) goto L_0x02d4
                r3 = r2[r1]
                r5 = 0
                int r3 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
                if (r3 <= 0) goto L_0x02d4
                r3 = r2[r1]
                float r6 = (float) r9
                float r6 = r6 / r16
                float r3 = r3 - r6
                r2[r1] = r3
                r3 = r2[r1]
                int r3 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
                if (r3 > 0) goto L_0x02d2
                r2[r1] = r5
                int r2 = r0.previousSelectedPotision
                if (r1 != r2) goto L_0x02d2
                r0.previousSelectedPotision = r4
            L_0x02d2:
                r8 = 1
                goto L_0x02da
            L_0x02d4:
                int r2 = r0.previousSelectedPotision
                if (r1 != r2) goto L_0x02da
                r0.previousSelectedPotision = r4
            L_0x02da:
                int r1 = r1 + 1
                goto L_0x02ab
            L_0x02dd:
                r8 = 0
            L_0x02de:
                r1 = 2
                r2 = 0
            L_0x02e0:
                if (r2 >= r1) goto L_0x032a
                boolean[] r3 = r0.pressedOverlayVisible
                boolean r3 = r3[r2]
                if (r3 == 0) goto L_0x030a
                float[] r3 = r0.pressedOverlayAlpha
                r4 = r3[r2]
                r5 = 1065353216(0x3var_, float:1.0)
                int r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
                if (r4 >= 0) goto L_0x0308
                r4 = r3[r2]
                float r6 = (float) r9
                r7 = 1127481344(0x43340000, float:180.0)
                float r6 = r6 / r7
                float r4 = r4 + r6
                r3[r2] = r4
                r4 = r3[r2]
                int r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
                if (r4 <= 0) goto L_0x0303
                r3[r2] = r5
            L_0x0303:
                r6 = 0
                r8 = 1
            L_0x0305:
                r11 = 1127481344(0x43340000, float:180.0)
                goto L_0x0327
            L_0x0308:
                r6 = 0
                goto L_0x0305
            L_0x030a:
                r5 = 1065353216(0x3var_, float:1.0)
                float[] r3 = r0.pressedOverlayAlpha
                r4 = r3[r2]
                r6 = 0
                int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
                if (r4 <= 0) goto L_0x0305
                r4 = r3[r2]
                float r7 = (float) r9
                r11 = 1127481344(0x43340000, float:180.0)
                float r7 = r7 / r11
                float r4 = r4 - r7
                r3[r2] = r4
                r4 = r3[r2]
                int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
                if (r4 >= 0) goto L_0x0326
                r3[r2] = r6
            L_0x0326:
                r8 = 1
            L_0x0327:
                int r2 = r2 + 1
                goto L_0x02e0
            L_0x032a:
                if (r8 == 0) goto L_0x032f
                r21.postInvalidateOnAnimation()
            L_0x032f:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.OverlaysView.onDraw(android.graphics.Canvas):void");
        }

        public void onDown(boolean z) {
            this.pressedOverlayVisible[!z] = true;
            postInvalidateOnAnimation();
        }

        public void onRelease() {
            Arrays.fill(this.pressedOverlayVisible, false);
            postInvalidateOnAnimation();
        }

        public void onPhotosLoaded() {
            ProfileActivity.this.updateProfileData();
        }

        public void onVideoSet() {
            invalidate();
        }
    }

    private class NestedFrameLayout extends FrameLayout implements NestedScrollingParent3 {
        private NestedScrollingParentHelper nestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        public void onNestedScroll(View view, int i, int i2, int i3, int i4, int i5) {
        }

        public void onStopNestedScroll(View view) {
        }

        public NestedFrameLayout(Context context) {
            super(context);
        }

        public void onNestedScroll(View view, int i, int i2, int i3, int i4, int i5, int[] iArr) {
            if (view == ProfileActivity.this.listView && ProfileActivity.this.sharedMediaLayoutAttached) {
                RecyclerListView currentListView = ProfileActivity.this.sharedMediaLayout.getCurrentListView();
                if (ProfileActivity.this.sharedMediaLayout.getTop() == 0) {
                    iArr[1] = i4;
                    currentListView.scrollBy(0, i4);
                }
            }
        }

        public boolean onNestedPreFling(View view, float f, float f2) {
            return super.onNestedPreFling(view, f, f2);
        }

        public void onNestedPreScroll(View view, int i, int i2, int[] iArr, int i3) {
            int i4;
            if (view == ProfileActivity.this.listView) {
                int i5 = -1;
                if (ProfileActivity.this.sharedMediaRow != -1 && ProfileActivity.this.sharedMediaLayoutAttached) {
                    boolean isSearchFieldVisible = ProfileActivity.this.actionBar.isSearchFieldVisible();
                    int top = ProfileActivity.this.sharedMediaLayout.getTop();
                    boolean z = false;
                    if (i2 < 0) {
                        if (top <= 0) {
                            RecyclerListView currentListView = ProfileActivity.this.sharedMediaLayout.getCurrentListView();
                            int findFirstVisibleItemPosition = ((LinearLayoutManager) currentListView.getLayoutManager()).findFirstVisibleItemPosition();
                            if (findFirstVisibleItemPosition != -1) {
                                RecyclerView.ViewHolder findViewHolderForAdapterPosition = currentListView.findViewHolderForAdapterPosition(findFirstVisibleItemPosition);
                                if (findViewHolderForAdapterPosition != null) {
                                    i5 = findViewHolderForAdapterPosition.itemView.getTop();
                                }
                                int paddingTop = currentListView.getPaddingTop();
                                if (!(i5 == paddingTop && findFirstVisibleItemPosition == 0)) {
                                    if (findFirstVisibleItemPosition != 0) {
                                        i4 = i2;
                                    } else {
                                        i4 = Math.max(i2, i5 - paddingTop);
                                    }
                                    iArr[1] = i4;
                                    currentListView.scrollBy(0, i2);
                                    z = true;
                                }
                            }
                        }
                        if (!isSearchFieldVisible) {
                            return;
                        }
                        if (z || top >= 0) {
                            iArr[1] = i2;
                        } else {
                            iArr[1] = i2 - Math.max(top, i2);
                        }
                    } else if (isSearchFieldVisible) {
                        RecyclerListView currentListView2 = ProfileActivity.this.sharedMediaLayout.getCurrentListView();
                        iArr[1] = i2;
                        if (top > 0) {
                            iArr[1] = iArr[1] - Math.min(iArr[1], i2);
                        }
                        if (iArr[1] > 0) {
                            currentListView2.scrollBy(0, iArr[1]);
                        }
                    }
                }
            }
        }

        public boolean onStartNestedScroll(View view, View view2, int i, int i2) {
            return ProfileActivity.this.sharedMediaRow != -1 && i == 2;
        }

        public void onNestedScrollAccepted(View view, View view2, int i, int i2) {
            this.nestedScrollingParentHelper.onNestedScrollAccepted(view, view2, i);
        }

        public void onStopNestedScroll(View view, int i) {
            this.nestedScrollingParentHelper.onStopNestedScroll(view);
        }
    }

    private class PagerIndicatorView extends View {
        private final PagerAdapter adapter;
        private final ValueAnimator animator;
        private final float[] animatorValues = {0.0f, 1.0f};
        private final Paint backgroundPaint;
        private final RectF indicatorRect = new RectF();
        /* access modifiers changed from: private */
        public boolean isIndicatorVisible;
        private final TextPaint textPaint;

        public PagerIndicatorView(Context context) {
            super(context);
            PagerAdapter adapter2 = ProfileActivity.this.avatarsViewPager.getAdapter();
            this.adapter = adapter2;
            setVisibility(8);
            TextPaint textPaint2 = new TextPaint(1);
            this.textPaint = textPaint2;
            textPaint2.setColor(-1);
            textPaint2.setTypeface(Typeface.SANS_SERIF);
            textPaint2.setTextAlign(Paint.Align.CENTER);
            textPaint2.setTextSize(AndroidUtilities.dpf2(15.0f));
            Paint paint = new Paint(1);
            this.backgroundPaint = paint;
            paint.setColor(NUM);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.animator = ofFloat;
            ofFloat.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ProfileActivity.PagerIndicatorView.this.lambda$new$0$ProfileActivity$PagerIndicatorView(valueAnimator);
                }
            });
            ofFloat.addListener(new AnimatorListenerAdapter(ProfileActivity.this, ProfileActivity.this.expandPhoto) {
                final /* synthetic */ boolean val$expanded;

                {
                    this.val$expanded = r3;
                }

                public void onAnimationEnd(Animator animator) {
                    if (PagerIndicatorView.this.isIndicatorVisible) {
                        ActionBarMenuItem access$2600 = PagerIndicatorView.this.getSecondaryMenuItem();
                        if (access$2600 != null) {
                            access$2600.setVisibility(8);
                        }
                        if (ProfileActivity.this.videoCallItemVisible) {
                            ProfileActivity.this.videoCallItem.setVisibility(8);
                            return;
                        }
                        return;
                    }
                    PagerIndicatorView.this.setVisibility(8);
                }

                public void onAnimationStart(Animator animator) {
                    ActionBarMenuItem access$2600 = PagerIndicatorView.this.getSecondaryMenuItem();
                    if (access$2600 != null && (access$2600 != ProfileActivity.this.searchItem || !this.val$expanded)) {
                        access$2600.setVisibility(0);
                    }
                    if (ProfileActivity.this.videoCallItemVisible) {
                        ProfileActivity.this.videoCallItem.setVisibility(0);
                    }
                    PagerIndicatorView.this.setVisibility(0);
                }
            });
            ProfileActivity.this.avatarsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(ProfileActivity.this) {
                private int prevPage;

                public void onPageScrollStateChanged(int i) {
                }

                public void onPageScrolled(int i, float f, int i2) {
                }

                public void onPageSelected(int i) {
                    int realPosition = ProfileActivity.this.avatarsViewPager.getRealPosition(i);
                    PagerIndicatorView.this.invalidateIndicatorRect(this.prevPage != realPosition);
                    this.prevPage = realPosition;
                    PagerIndicatorView.this.updateAvatarItems();
                }
            });
            adapter2.registerDataSetObserver(new DataSetObserver(ProfileActivity.this) {
                public void onChanged() {
                    int realCount = ProfileActivity.this.avatarsViewPager.getRealCount();
                    if (ProfileActivity.this.overlayCountVisible == 0 && realCount > 1 && realCount <= 20 && ProfileActivity.this.overlaysView.isOverlaysVisible()) {
                        int unused = ProfileActivity.this.overlayCountVisible = 1;
                    }
                    PagerIndicatorView.this.invalidateIndicatorRect(false);
                    PagerIndicatorView.this.refreshVisibility(1.0f);
                    PagerIndicatorView.this.updateAvatarItems();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$0 */
        public /* synthetic */ void lambda$new$0$ProfileActivity$PagerIndicatorView(ValueAnimator valueAnimator) {
            float lerp = AndroidUtilities.lerp(this.animatorValues, valueAnimator.getAnimatedFraction());
            ActionBarMenuItem secondaryMenuItem = getSecondaryMenuItem();
            if (secondaryMenuItem != null) {
                float f = 1.0f - lerp;
                secondaryMenuItem.setScaleX(f);
                secondaryMenuItem.setScaleY(f);
                secondaryMenuItem.setAlpha(f);
            }
            if (ProfileActivity.this.videoCallItemVisible) {
                float f2 = 1.0f - lerp;
                ProfileActivity.this.videoCallItem.setScaleX(f2);
                ProfileActivity.this.videoCallItem.setScaleY(f2);
                ProfileActivity.this.videoCallItem.setAlpha(f2);
            }
            setScaleX(lerp);
            setScaleY(lerp);
            setAlpha(lerp);
        }

        /* access modifiers changed from: private */
        public void updateAvatarItemsInternal() {
            if (ProfileActivity.this.otherItem != null && ProfileActivity.this.avatarsViewPager != null && ProfileActivity.this.isPulledDown) {
                if (ProfileActivity.this.avatarsViewPager.getRealPosition() == 0) {
                    ProfileActivity.this.otherItem.hideSubItem(33);
                    ProfileActivity.this.otherItem.showSubItem(36);
                    return;
                }
                ProfileActivity.this.otherItem.showSubItem(33);
                ProfileActivity.this.otherItem.hideSubItem(36);
            }
        }

        /* access modifiers changed from: private */
        public void updateAvatarItems() {
            if (ProfileActivity.this.imageUpdater != null) {
                if (ProfileActivity.this.otherItem.isSubMenuShowing()) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public final void run() {
                            ProfileActivity.PagerIndicatorView.this.updateAvatarItemsInternal();
                        }
                    }, 500);
                } else {
                    updateAvatarItemsInternal();
                }
            }
        }

        public boolean isIndicatorFullyVisible() {
            return this.isIndicatorVisible && !this.animator.isRunning();
        }

        public void setIndicatorVisible(boolean z, float f) {
            if (z != this.isIndicatorVisible) {
                this.isIndicatorVisible = z;
                this.animator.cancel();
                float lerp = AndroidUtilities.lerp(this.animatorValues, this.animator.getAnimatedFraction());
                float f2 = 1.0f;
                if (f <= 0.0f) {
                    this.animator.setDuration(0);
                } else if (z) {
                    this.animator.setDuration((long) (((1.0f - lerp) * 250.0f) / f));
                } else {
                    this.animator.setDuration((long) ((250.0f * lerp) / f));
                }
                float[] fArr = this.animatorValues;
                fArr[0] = lerp;
                if (!z) {
                    f2 = 0.0f;
                }
                fArr[1] = f2;
                this.animator.start();
            }
        }

        public void refreshVisibility(float f) {
            setIndicatorVisible(ProfileActivity.this.isPulledDown && ProfileActivity.this.avatarsViewPager.getRealCount() > 20, f);
        }

        /* access modifiers changed from: protected */
        public void onSizeChanged(int i, int i2, int i3, int i4) {
            invalidateIndicatorRect(false);
        }

        /* access modifiers changed from: private */
        public void invalidateIndicatorRect(boolean z) {
            if (z) {
                ProfileActivity.this.overlaysView.saveCurrentPageProgress();
            }
            ProfileActivity.this.overlaysView.invalidate();
            float measureText = this.textPaint.measureText(getCurrentTitle());
            this.indicatorRect.right = (float) (getMeasuredWidth() - AndroidUtilities.dp(54.0f));
            RectF rectF = this.indicatorRect;
            rectF.left = rectF.right - (measureText + AndroidUtilities.dpf2(16.0f));
            this.indicatorRect.top = (float) ((ProfileActivity.this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.dp(15.0f));
            RectF rectF2 = this.indicatorRect;
            rectF2.bottom = rectF2.top + ((float) AndroidUtilities.dp(26.0f));
            setPivotX(this.indicatorRect.centerX());
            setPivotY(this.indicatorRect.centerY());
            invalidate();
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            float dpf2 = AndroidUtilities.dpf2(12.0f);
            canvas.drawRoundRect(this.indicatorRect, dpf2, dpf2, this.backgroundPaint);
            canvas.drawText(getCurrentTitle(), this.indicatorRect.centerX(), this.indicatorRect.top + AndroidUtilities.dpf2(18.5f), this.textPaint);
        }

        private String getCurrentTitle() {
            return this.adapter.getPageTitle(ProfileActivity.this.avatarsViewPager.getCurrentItem()).toString();
        }

        /* access modifiers changed from: private */
        public ActionBarMenuItem getSecondaryMenuItem() {
            if (ProfileActivity.this.callItemVisible) {
                return ProfileActivity.this.callItem;
            }
            if (ProfileActivity.this.editItemVisible) {
                return ProfileActivity.this.editItem;
            }
            if (ProfileActivity.this.searchItem != null) {
                return ProfileActivity.this.searchItem;
            }
            return null;
        }
    }

    public ProfileActivity(Bundle bundle) {
        this(bundle, (SharedMediaLayout.SharedMediaPreloader) null);
    }

    public ProfileActivity(Bundle bundle, SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader2) {
        super(bundle);
        this.nameTextView = new SimpleTextView[2];
        this.onlineTextView = new SimpleTextView[2];
        this.isOnline = new boolean[1];
        this.headerShadowAlpha = 1.0f;
        this.participantsMap = new SparseArray<>();
        this.allowProfileAnimation = true;
        this.positionToOffset = new HashMap<>();
        this.expandAnimatorValues = new float[]{0.0f, 1.0f};
        this.whitePaint = new Paint();
        this.onlineCount = -1;
        this.rect = new Rect();
        this.HEADER_SHADOW = new AnimationProperties.FloatProperty<ProfileActivity>("headerShadow") {
            public void setValue(ProfileActivity profileActivity, float f) {
                ProfileActivity profileActivity2 = ProfileActivity.this;
                profileActivity2.headerShadowAlpha = f;
                profileActivity2.topView.invalidate();
            }

            public Float get(ProfileActivity profileActivity) {
                return Float.valueOf(ProfileActivity.this.headerShadowAlpha);
            }
        };
        this.provider = new PhotoViewer.EmptyPhotoViewerProvider() {
            public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC$FileLocation tLRPC$FileLocation, int i, boolean z) {
                TLRPC$FileLocation tLRPC$FileLocation2;
                TLRPC$Chat chat;
                TLRPC$ChatPhoto tLRPC$ChatPhoto;
                TLRPC$User user;
                TLRPC$UserProfilePhoto tLRPC$UserProfilePhoto;
                if (tLRPC$FileLocation == null) {
                    return null;
                }
                if (ProfileActivity.this.user_id == 0 ? ProfileActivity.this.chat_id == 0 || (chat = ProfileActivity.this.getMessagesController().getChat(Integer.valueOf(ProfileActivity.this.chat_id))) == null || (tLRPC$ChatPhoto = chat.photo) == null || (tLRPC$FileLocation2 = tLRPC$ChatPhoto.photo_big) == null : (user = ProfileActivity.this.getMessagesController().getUser(Integer.valueOf(ProfileActivity.this.user_id))) == null || (tLRPC$UserProfilePhoto = user.photo) == null || (tLRPC$FileLocation2 = tLRPC$UserProfilePhoto.photo_big) == null) {
                    tLRPC$FileLocation2 = null;
                }
                if (tLRPC$FileLocation2 == null || tLRPC$FileLocation2.local_id != tLRPC$FileLocation.local_id || tLRPC$FileLocation2.volume_id != tLRPC$FileLocation.volume_id || tLRPC$FileLocation2.dc_id != tLRPC$FileLocation.dc_id) {
                    return null;
                }
                int[] iArr = new int[2];
                ProfileActivity.this.avatarImage.getLocationInWindow(iArr);
                PhotoViewer.PlaceProviderObject placeProviderObject = new PhotoViewer.PlaceProviderObject();
                boolean z2 = false;
                placeProviderObject.viewX = iArr[0];
                placeProviderObject.viewY = iArr[1] - (Build.VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight);
                placeProviderObject.parentView = ProfileActivity.this.avatarImage;
                placeProviderObject.imageReceiver = ProfileActivity.this.avatarImage.getImageReceiver();
                if (ProfileActivity.this.user_id != 0) {
                    placeProviderObject.dialogId = ProfileActivity.this.user_id;
                } else if (ProfileActivity.this.chat_id != 0) {
                    placeProviderObject.dialogId = -ProfileActivity.this.chat_id;
                }
                placeProviderObject.thumb = placeProviderObject.imageReceiver.getBitmapSafe();
                placeProviderObject.size = -1;
                placeProviderObject.radius = ProfileActivity.this.avatarImage.getImageReceiver().getRoundRadius();
                placeProviderObject.scale = ProfileActivity.this.avatarContainer.getScaleX();
                if (ProfileActivity.this.user_id == ProfileActivity.this.getUserConfig().clientUserId) {
                    z2 = true;
                }
                placeProviderObject.canEdit = z2;
                return placeProviderObject;
            }

            public void willHidePhotoViewer() {
                ProfileActivity.this.avatarImage.getImageReceiver().setVisible(true, true);
            }

            public void openPhotoForEdit(String str, String str2, boolean z) {
                ProfileActivity.this.imageUpdater.openPhotoForEdit(str, str2, 0, z);
            }
        };
        this.ACTIONBAR_HEADER_PROGRESS = new AnimationProperties.FloatProperty<ActionBar>("animationProgress") {
            public void setValue(ActionBar actionBar, float f) {
                float unused = ProfileActivity.this.mediaHeaderAnimationProgress = f;
                ProfileActivity.this.topView.invalidate();
                int color = Theme.getColor("profile_title");
                int color2 = Theme.getColor("player_actionBarTitle");
                int offsetColor = AndroidUtilities.getOffsetColor(color, color2, f, 1.0f);
                ProfileActivity.this.nameTextView[1].setTextColor(offsetColor);
                if (ProfileActivity.this.lockIconDrawable != null) {
                    ProfileActivity.this.lockIconDrawable.setColorFilter(offsetColor, PorterDuff.Mode.MULTIPLY);
                }
                if (ProfileActivity.this.scamDrawable != null) {
                    ProfileActivity.this.scamDrawable.setColor(AndroidUtilities.getOffsetColor(Theme.getColor("avatar_subtitleInProfileBlue"), color2, f, 1.0f));
                }
                ProfileActivity.this.actionBar.setItemsColor(AndroidUtilities.getOffsetColor(Theme.getColor("actionBarDefaultIcon"), Theme.getColor("windowBackgroundWhiteGrayText2"), f, 1.0f), false);
                ProfileActivity.this.actionBar.setItemsBackgroundColor(AndroidUtilities.getOffsetColor(Theme.getColor("avatar_actionBarSelectorBlue"), Theme.getColor("actionBarActionModeDefaultSelector"), f, 1.0f), false);
                ProfileActivity.this.topView.invalidate();
                ProfileActivity.this.otherItem.setIconColor(Theme.getColor("actionBarDefaultIcon"));
                ProfileActivity.this.callItem.setIconColor(Theme.getColor("actionBarDefaultIcon"));
                ProfileActivity.this.videoCallItem.setIconColor(Theme.getColor("actionBarDefaultIcon"));
                ProfileActivity.this.editItem.setIconColor(Theme.getColor("actionBarDefaultIcon"));
                if (ProfileActivity.this.verifiedDrawable != null) {
                    ProfileActivity.this.verifiedDrawable.setColorFilter(AndroidUtilities.getOffsetColor(Theme.getColor("profile_verifiedBackground"), Theme.getColor("player_actionBarTitle"), f, 1.0f), PorterDuff.Mode.MULTIPLY);
                }
                if (ProfileActivity.this.verifiedCheckDrawable != null) {
                    ProfileActivity.this.verifiedCheckDrawable.setColorFilter(AndroidUtilities.getOffsetColor(Theme.getColor("profile_verifiedCheck"), Theme.getColor("windowBackgroundWhite"), f, 1.0f), PorterDuff.Mode.MULTIPLY);
                }
                if (ProfileActivity.this.avatarsViewPagerIndicatorView.getSecondaryMenuItem() != null && ProfileActivity.this.videoCallItemVisible) {
                    ProfileActivity profileActivity = ProfileActivity.this;
                    profileActivity.needLayoutText(Math.min(1.0f, profileActivity.extraHeight / ((float) AndroidUtilities.dp(88.0f))));
                }
            }

            public Float get(ActionBar actionBar) {
                return Float.valueOf(ProfileActivity.this.mediaHeaderAnimationProgress);
            }
        };
        this.sharedMediaPreloader = sharedMediaPreloader2;
    }

    public boolean onFragmentCreate() {
        boolean z = false;
        this.user_id = this.arguments.getInt("user_id", 0);
        this.chat_id = this.arguments.getInt("chat_id", 0);
        this.banFromGroup = this.arguments.getInt("ban_chat_id", 0);
        this.reportSpam = this.arguments.getBoolean("reportSpam", false);
        if (!this.expandPhoto) {
            boolean z2 = this.arguments.getBoolean("expandPhoto", false);
            this.expandPhoto = z2;
            if (z2) {
                this.needSendMessage = true;
            }
        }
        if (this.user_id != 0) {
            long j = this.arguments.getLong("dialog_id", 0);
            this.dialog_id = j;
            if (j != 0) {
                this.currentEncryptedChat = getMessagesController().getEncryptedChat(Integer.valueOf((int) (this.dialog_id >> 32)));
            }
            TLRPC$User user = getMessagesController().getUser(Integer.valueOf(this.user_id));
            if (user == null) {
                return false;
            }
            getNotificationCenter().addObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatCreated);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.blockedUsersDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.botInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.userInfoDidLoad);
            if (getMessagesController().blockePeers.indexOfKey(this.user_id) >= 0) {
                z = true;
            }
            this.userBlocked = z;
            if (user.bot) {
                this.isBot = true;
                getMediaDataController().loadBotInfo(user.id, true, this.classGuid);
            }
            this.userInfo = getMessagesController().getUserFull(this.user_id);
            getMessagesController().loadFullUser(getMessagesController().getUser(Integer.valueOf(this.user_id)), this.classGuid, true);
            this.participantsMap = null;
            if (UserObject.isUserSelf(user)) {
                ImageUpdater imageUpdater2 = new ImageUpdater(true);
                this.imageUpdater = imageUpdater2;
                imageUpdater2.setOpenWithFrontfaceCamera(true);
                ImageUpdater imageUpdater3 = this.imageUpdater;
                imageUpdater3.parentFragment = this;
                imageUpdater3.setDelegate(this);
                getMediaDataController().checkFeaturedStickers();
                getMessagesController().loadSuggestedFilters();
                getMessagesController().loadUserInfo(getUserConfig().getCurrentUser(), true, this.classGuid);
            }
        } else if (this.chat_id == 0) {
            return false;
        } else {
            TLRPC$Chat chat = getMessagesController().getChat(Integer.valueOf(this.chat_id));
            this.currentChat = chat;
            if (chat == null) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                getMessagesStorage().getStorageQueue().postRunnable(new Runnable(countDownLatch) {
                    public final /* synthetic */ CountDownLatch f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ProfileActivity.this.lambda$onFragmentCreate$0$ProfileActivity(this.f$1);
                    }
                });
                try {
                    countDownLatch.await();
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
                if (this.currentChat == null) {
                    return false;
                }
                getMessagesController().putChat(this.currentChat, true);
            }
            if (this.currentChat.megagroup) {
                getChannelParticipants(true);
            } else {
                this.participantsMap = null;
            }
            getNotificationCenter().addObserver(this, NotificationCenter.chatInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.chatOnlineCountDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.groupCallUpdated);
            this.sortedUsers = new ArrayList<>();
            updateOnlineCount();
            if (this.chatInfo == null) {
                this.chatInfo = getMessagesController().getChatFull(this.chat_id);
            }
            if (ChatObject.isChannel(this.currentChat)) {
                getMessagesController().loadFullChat(this.chat_id, this.classGuid, true);
            } else if (this.chatInfo == null) {
                this.chatInfo = getMessagesStorage().loadChatInfo(this.chat_id, false, (CountDownLatch) null, false, false);
            }
        }
        if (this.sharedMediaPreloader == null) {
            this.sharedMediaPreloader = new SharedMediaLayout.SharedMediaPreloader(this);
        }
        this.sharedMediaPreloader.addDelegate(this);
        getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().addObserver(this, NotificationCenter.didReceiveNewMessages);
        getNotificationCenter().addObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoad);
        updateRowsIds();
        if (this.arguments.containsKey("nearby_distance")) {
            preloadGreetingsSticker();
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFragmentCreate$0 */
    public /* synthetic */ void lambda$onFragmentCreate$0$ProfileActivity(CountDownLatch countDownLatch) {
        this.currentChat = getMessagesStorage().getChat(this.chat_id);
        countDownLatch.countDown();
    }

    private void preloadGreetingsSticker() {
        TLRPC$TL_messages_getStickers tLRPC$TL_messages_getStickers = new TLRPC$TL_messages_getStickers();
        tLRPC$TL_messages_getStickers.emoticon = "👋" + Emoji.fixEmoji("⭐");
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(tLRPC$TL_messages_getStickers, new RequestDelegate() {
            public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                ProfileActivity.this.lambda$preloadGreetingsSticker$2$ProfileActivity(tLObject, tLRPC$TL_error);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$preloadGreetingsSticker$2 */
    public /* synthetic */ void lambda$preloadGreetingsSticker$2$ProfileActivity(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        if (tLObject instanceof TLRPC$TL_messages_stickers) {
            ArrayList<TLRPC$Document> arrayList = ((TLRPC$TL_messages_stickers) tLObject).stickers;
            if (!arrayList.isEmpty()) {
                TLRPC$Document tLRPC$Document = arrayList.get(Math.abs(new Random().nextInt() % arrayList.size()));
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public final void run() {
                        new ImageReceiver().setImage(ImageLocation.getForDocument(TLRPC$Document.this), ChatGreetingsView.createFilter(TLRPC$Document.this), (Drawable) null, (String) null, TLRPC$Document.this, 0);
                    }
                });
                this.preloadedSticker = tLRPC$Document;
            }
        }
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        SharedMediaLayout sharedMediaLayout2 = this.sharedMediaLayout;
        if (sharedMediaLayout2 != null) {
            sharedMediaLayout2.onDestroy();
        }
        SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader2 = this.sharedMediaPreloader;
        if (sharedMediaPreloader2 != null) {
            sharedMediaPreloader2.onDestroy(this);
        }
        SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader3 = this.sharedMediaPreloader;
        if (sharedMediaPreloader3 != null) {
            sharedMediaPreloader3.removeDelegate(this);
        }
        getNotificationCenter().removeObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().removeObserver(this, NotificationCenter.didReceiveNewMessages);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoad);
        ProfileGalleryView profileGalleryView = this.avatarsViewPager;
        if (profileGalleryView != null) {
            profileGalleryView.onDestroy();
        }
        if (this.user_id != 0) {
            getNotificationCenter().removeObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.encryptedChatCreated);
            getNotificationCenter().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.blockedUsersDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.botInfoDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.userInfoDidLoad);
            getMessagesController().cancelLoadFullUser(this.user_id);
        } else if (this.chat_id != 0) {
            getNotificationCenter().removeObserver(this, NotificationCenter.chatInfoDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.chatOnlineCountDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.groupCallUpdated);
        }
        AvatarImageView avatarImageView = this.avatarImage;
        if (avatarImageView != null) {
            avatarImageView.setImageDrawable((Drawable) null);
        }
        ImageUpdater imageUpdater2 = this.imageUpdater;
        if (imageUpdater2 != null) {
            imageUpdater2.clear();
        }
    }

    /* access modifiers changed from: protected */
    public ActionBar createActionBar(Context context) {
        AnonymousClass3 r0 = new ActionBar(context) {
            public boolean onTouchEvent(MotionEvent motionEvent) {
                ProfileActivity.this.avatarContainer.getHitRect(ProfileActivity.this.rect);
                if (ProfileActivity.this.rect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                    return false;
                }
                return super.onTouchEvent(motionEvent);
            }
        };
        boolean z = false;
        r0.setBackgroundColor(0);
        r0.setItemsBackgroundColor(AvatarDrawable.getButtonColorForId((this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) ? 5 : this.chat_id), false);
        r0.setItemsColor(Theme.getColor("actionBarDefaultIcon"), false);
        r0.setBackButtonDrawable(new BackDrawable(false));
        r0.setCastShadows(false);
        r0.setAddToContainer(false);
        r0.setClipContent(true);
        if (Build.VERSION.SDK_INT >= 21 && !AndroidUtilities.isTablet() && !this.inBubbleMode) {
            z = true;
        }
        r0.setOccupyStatusBar(z);
        return r0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0046, code lost:
        r0 = r0.participants;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.view.View createView(android.content.Context r36) {
        /*
            r35 = this;
            r11 = r35
            r12 = r36
            org.telegram.ui.ActionBar.Theme.createProfileResources(r36)
            r13 = 0
            r11.searchTransitionOffset = r13
            r14 = 1065353216(0x3var_, float:1.0)
            r11.searchTransitionProgress = r14
            r11.searchMode = r13
            r15 = 1
            r11.hasOwnBackground = r15
            r16 = 1118830592(0x42b00000, float:88.0)
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r16)
            float r0 = (float) r0
            r11.extraHeight = r0
            org.telegram.ui.ActionBar.ActionBar r0 = r11.actionBar
            org.telegram.ui.ProfileActivity$4 r1 = new org.telegram.ui.ProfileActivity$4
            r1.<init>()
            r0.setActionBarMenuOnItemClick(r1)
            org.telegram.ui.Components.SharedMediaLayout r0 = r11.sharedMediaLayout
            if (r0 == 0) goto L_0x002d
            r0.onDestroy()
        L_0x002d:
            long r0 = r11.dialog_id
            r2 = 0
            int r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r4 == 0) goto L_0x0037
        L_0x0035:
            r9 = r0
            goto L_0x0041
        L_0x0037:
            int r0 = r11.user_id
            if (r0 == 0) goto L_0x003c
            goto L_0x003f
        L_0x003c:
            int r0 = r11.chat_id
            int r0 = -r0
        L_0x003f:
            long r0 = (long) r0
            goto L_0x0035
        L_0x0041:
            org.telegram.tgnet.TLRPC$ChatFull r0 = r11.chatInfo
            r8 = 0
            if (r0 == 0) goto L_0x0056
            org.telegram.tgnet.TLRPC$ChatParticipants r0 = r0.participants
            if (r0 == 0) goto L_0x0056
            java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r0 = r0.participants
            int r0 = r0.size()
            r1 = 5
            if (r0 <= r1) goto L_0x0056
            java.util.ArrayList<java.lang.Integer> r0 = r11.sortedUsers
            goto L_0x0057
        L_0x0056:
            r0 = r8
        L_0x0057:
            org.telegram.ui.ProfileActivity$5 r7 = new org.telegram.ui.ProfileActivity$5
            org.telegram.ui.Components.SharedMediaLayout$SharedMediaPreloader r5 = r11.sharedMediaPreloader
            org.telegram.tgnet.TLRPC$UserFull r1 = r11.userInfo
            if (r1 == 0) goto L_0x0063
            int r1 = r1.common_chats_count
            r6 = r1
            goto L_0x0064
        L_0x0063:
            r6 = 0
        L_0x0064:
            java.util.ArrayList<java.lang.Integer> r3 = r11.sortedUsers
            org.telegram.tgnet.TLRPC$ChatFull r4 = r11.chatInfo
            if (r0 == 0) goto L_0x006d
            r17 = 1
            goto L_0x006f
        L_0x006d:
            r17 = 0
        L_0x006f:
            r0 = r7
            r1 = r35
            r2 = r36
            r18 = r3
            r19 = r4
            r3 = r9
            r14 = r7
            r7 = r18
            r8 = r19
            r21 = r9
            r9 = r17
            r10 = r35
            r0.<init>(r2, r3, r5, r6, r7, r8, r9, r10)
            r11.sharedMediaLayout = r14
            androidx.recyclerview.widget.RecyclerView$LayoutParams r0 = new androidx.recyclerview.widget.RecyclerView$LayoutParams
            r9 = -1
            r0.<init>((int) r9, (int) r9)
            r14.setLayoutParams(r0)
            org.telegram.ui.ActionBar.ActionBar r0 = r11.actionBar
            org.telegram.ui.ActionBar.ActionBarMenu r0 = r0.createMenu()
            org.telegram.ui.Components.ImageUpdater r1 = r11.imageUpdater
            r2 = 8
            if (r1 == 0) goto L_0x00db
            r1 = 32
            r3 = 2131165476(0x7var_, float:1.794517E38)
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.addItem((int) r1, (int) r3)
            r1.setIsSearchField(r15)
            org.telegram.ui.ProfileActivity$6 r3 = new org.telegram.ui.ProfileActivity$6
            r3.<init>()
            r1.setActionBarMenuItemSearchListener(r3)
            r11.searchItem = r1
            java.lang.String r3 = "SearchInSettings"
            r4 = 2131627031(0x7f0e0CLASSNAME, float:1.8881315E38)
            java.lang.String r5 = org.telegram.messenger.LocaleController.getString(r3, r4)
            r1.setContentDescription(r5)
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r11.searchItem
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r3, r4)
            r1.setSearchFieldHint(r3)
            org.telegram.ui.Components.SharedMediaLayout r1 = r11.sharedMediaLayout
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r1.getSearchItem()
            r1.setVisibility(r2)
            boolean r1 = r11.expandPhoto
            if (r1 == 0) goto L_0x00db
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r11.searchItem
            r1.setVisibility(r2)
        L_0x00db:
            r1 = 16
            r3 = 2131165937(0x7var_f1, float:1.7946105E38)
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.addItem((int) r1, (int) r3)
            r11.videoCallItem = r1
            r3 = 2131627630(0x7f0e0e6e, float:1.888253E38)
            java.lang.String r4 = "VideoCall"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
            r1.setContentDescription(r3)
            r1 = 15
            r3 = 2131165486(0x7var_e, float:1.794519E38)
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.addItem((int) r1, (int) r3)
            r11.callItem = r1
            r3 = 2131624556(0x7f0e026c, float:1.8876295E38)
            java.lang.String r4 = "Call"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
            r1.setContentDescription(r3)
            r1 = 12
            r3 = 2131165456(0x7var_, float:1.794513E38)
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.addItem((int) r1, (int) r3)
            r11.editItem = r1
            r3 = 2131625128(0x7f0e04a8, float:1.8877455E38)
            java.lang.String r4 = "Edit"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
            r1.setContentDescription(r3)
            r1 = 10
            r3 = 2131165473(0x7var_, float:1.7945164E38)
            org.telegram.ui.ActionBar.ActionBarMenuItem r0 = r0.addItem((int) r1, (int) r3)
            r11.otherItem = r0
            r1 = 2131623985(0x7f0e0031, float:1.8875137E38)
            java.lang.String r3 = "AccDescrMoreOptions"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r3, r1)
            r0.setContentDescription(r1)
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            if (r0 == 0) goto L_0x0160
            org.telegram.ui.Components.ImageUpdater r0 = r11.imageUpdater
            if (r0 == 0) goto L_0x0160
            androidx.recyclerview.widget.LinearLayoutManager r0 = r11.layoutManager
            int r0 = r0.findFirstVisibleItemPosition()
            androidx.recyclerview.widget.LinearLayoutManager r1 = r11.layoutManager
            android.view.View r1 = r1.findViewByPosition(r0)
            if (r1 == 0) goto L_0x0156
            r1.getTop()
            org.telegram.ui.Components.RecyclerListView r1 = r11.listView
            r1.getPaddingTop()
            goto L_0x0157
        L_0x0156:
            r0 = -1
        L_0x0157:
            android.widget.ImageView r1 = r11.writeButton
            java.lang.Object r8 = r1.getTag()
            r10 = r0
            r14 = r8
            goto L_0x0162
        L_0x0160:
            r10 = -1
            r14 = 0
        L_0x0162:
            r11.createActionBarMenu(r13)
            org.telegram.ui.ProfileActivity$ListAdapter r0 = new org.telegram.ui.ProfileActivity$ListAdapter
            r0.<init>(r12)
            r11.listAdapter = r0
            org.telegram.ui.ProfileActivity$SearchAdapter r0 = new org.telegram.ui.ProfileActivity$SearchAdapter
            r0.<init>(r11, r12)
            r11.searchAdapter = r0
            org.telegram.ui.Components.AvatarDrawable r0 = new org.telegram.ui.Components.AvatarDrawable
            r0.<init>()
            r11.avatarDrawable = r0
            r0.setProfile(r15)
            org.telegram.ui.ProfileActivity$7 r0 = new org.telegram.ui.ProfileActivity$7
            r0.<init>(r12)
            r11.fragmentView = r0
            r0.setWillNotDraw(r13)
            android.view.View r0 = r11.fragmentView
            r8 = r0
            android.widget.FrameLayout r8 = (android.widget.FrameLayout) r8
            org.telegram.ui.ProfileActivity$8 r0 = new org.telegram.ui.ProfileActivity$8
            r0.<init>(r12)
            r11.listView = r0
            r0.setVerticalScrollBarEnabled(r13)
            org.telegram.ui.ProfileActivity$9 r0 = new org.telegram.ui.ProfileActivity$9
            r0.<init>()
            org.telegram.ui.Components.RecyclerListView r1 = r11.listView
            r1.setItemAnimator(r0)
            r0.setSupportsChangeAnimations(r13)
            r0.setDelayAnimations(r13)
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            r0.setClipToPadding(r13)
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            r0.setHideIfEmpty(r13)
            org.telegram.ui.ProfileActivity$10 r0 = new org.telegram.ui.ProfileActivity$10
            r0.<init>(r12)
            r11.layoutManager = r0
            r0.setOrientation(r15)
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            androidx.recyclerview.widget.LinearLayoutManager r1 = r11.layoutManager
            r0.setLayoutManager(r1)
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            r0.setGlowColor(r13)
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            org.telegram.ui.ProfileActivity$ListAdapter r1 = r11.listAdapter
            r0.setAdapter(r1)
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            r1 = 51
            android.widget.FrameLayout$LayoutParams r3 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r9, r1)
            r8.addView(r0, r3)
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            org.telegram.ui.-$$Lambda$ProfileActivity$S0pZhsKOFjeAvMTNaIiQDS-rffU r3 = new org.telegram.ui.-$$Lambda$ProfileActivity$S0pZhsKOFjeAvMTNaIiQDS-rffU
            r4 = r21
            r3.<init>(r4)
            r0.setOnItemClickListener((org.telegram.ui.Components.RecyclerListView.OnItemClickListenerExtended) r3)
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            org.telegram.ui.ProfileActivity$11 r3 = new org.telegram.ui.ProfileActivity$11
            r3.<init>()
            r0.setOnItemLongClickListener((org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener) r3)
            org.telegram.ui.ActionBar.ActionBarMenuItem r0 = r11.searchItem
            r7 = 18
            java.lang.String r3 = "avatar_backgroundActionBarBlue"
            r6 = -1082130432(0xffffffffbvar_, float:-1.0)
            if (r0 == 0) goto L_0x029a
            org.telegram.ui.Components.RecyclerListView r0 = new org.telegram.ui.Components.RecyclerListView
            r0.<init>(r12)
            r11.searchListView = r0
            r0.setVerticalScrollBarEnabled(r13)
            org.telegram.ui.Components.RecyclerListView r0 = r11.searchListView
            androidx.recyclerview.widget.LinearLayoutManager r4 = new androidx.recyclerview.widget.LinearLayoutManager
            r4.<init>(r12, r15, r13)
            r0.setLayoutManager(r4)
            org.telegram.ui.Components.RecyclerListView r0 = r11.searchListView
            int r4 = org.telegram.ui.ActionBar.Theme.getColor(r3)
            r0.setGlowColor(r4)
            org.telegram.ui.Components.RecyclerListView r0 = r11.searchListView
            org.telegram.ui.ProfileActivity$SearchAdapter r4 = r11.searchAdapter
            r0.setAdapter(r4)
            org.telegram.ui.Components.RecyclerListView r0 = r11.searchListView
            r4 = 0
            r0.setItemAnimator(r4)
            org.telegram.ui.Components.RecyclerListView r0 = r11.searchListView
            r0.setVisibility(r2)
            org.telegram.ui.Components.RecyclerListView r0 = r11.searchListView
            r0.setLayoutAnimation(r4)
            org.telegram.ui.Components.RecyclerListView r0 = r11.searchListView
            java.lang.String r4 = "windowBackgroundWhite"
            int r5 = org.telegram.ui.ActionBar.Theme.getColor(r4)
            r0.setBackgroundColor(r5)
            org.telegram.ui.Components.RecyclerListView r0 = r11.searchListView
            android.widget.FrameLayout$LayoutParams r5 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r9, r1)
            r8.addView(r0, r5)
            org.telegram.ui.Components.RecyclerListView r0 = r11.searchListView
            org.telegram.ui.-$$Lambda$ProfileActivity$GtTO1zKa9nsXqVAXN05ruTo0jGc r5 = new org.telegram.ui.-$$Lambda$ProfileActivity$GtTO1zKa9nsXqVAXN05ruTo0jGc
            r5.<init>()
            r0.setOnItemClickListener((org.telegram.ui.Components.RecyclerListView.OnItemClickListener) r5)
            org.telegram.ui.Components.RecyclerListView r0 = r11.searchListView
            org.telegram.ui.-$$Lambda$ProfileActivity$fe-gxWZQHbZJR4UbtWgXkFDrX2A r5 = new org.telegram.ui.-$$Lambda$ProfileActivity$fe-gxWZQHbZJR4UbtWgXkFDrX2A
            r5.<init>()
            r0.setOnItemLongClickListener((org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener) r5)
            org.telegram.ui.Components.RecyclerListView r0 = r11.searchListView
            org.telegram.ui.ProfileActivity$12 r5 = new org.telegram.ui.ProfileActivity$12
            r5.<init>()
            r0.setOnScrollListener(r5)
            org.telegram.ui.Components.EmptyTextProgressView r0 = new org.telegram.ui.Components.EmptyTextProgressView
            r0.<init>(r12)
            r11.emptyView = r0
            r0.showTextView()
            org.telegram.ui.Components.EmptyTextProgressView r0 = r11.emptyView
            r0.setTextSize(r7)
            org.telegram.ui.Components.EmptyTextProgressView r0 = r11.emptyView
            r0.setVisibility(r2)
            org.telegram.ui.Components.EmptyTextProgressView r0 = r11.emptyView
            r0.setShowAtCenter(r15)
            org.telegram.ui.Components.EmptyTextProgressView r0 = r11.emptyView
            r2 = 1112014848(0x42480000, float:50.0)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r0.setPadding(r13, r2, r13, r13)
            org.telegram.ui.Components.EmptyTextProgressView r0 = r11.emptyView
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r4)
            r0.setBackgroundColor(r2)
            org.telegram.ui.Components.EmptyTextProgressView r0 = r11.emptyView
            android.widget.FrameLayout$LayoutParams r2 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r6)
            r8.addView(r0, r2)
            org.telegram.ui.ProfileActivity$SearchAdapter r0 = r11.searchAdapter
            r0.loadFaqWebPage()
        L_0x029a:
            int r0 = r11.banFromGroup
            java.lang.String r17 = "fonts/rmedium.ttf"
            r18 = 1111490560(0x42400000, float:48.0)
            if (r0 == 0) goto L_0x0348
            org.telegram.messenger.MessagesController r0 = r35.getMessagesController()
            int r2 = r11.banFromGroup
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$Chat r0 = r0.getChat(r2)
            org.telegram.tgnet.TLRPC$ChannelParticipant r2 = r11.currentChannelParticipant
            if (r2 != 0) goto L_0x02d7
            org.telegram.tgnet.TLRPC$TL_channels_getParticipant r2 = new org.telegram.tgnet.TLRPC$TL_channels_getParticipant
            r2.<init>()
            org.telegram.tgnet.TLRPC$InputChannel r4 = org.telegram.messenger.MessagesController.getInputChannel((org.telegram.tgnet.TLRPC$Chat) r0)
            r2.channel = r4
            org.telegram.messenger.MessagesController r4 = r35.getMessagesController()
            int r5 = r11.user_id
            org.telegram.tgnet.TLRPC$InputUser r4 = r4.getInputUser((int) r5)
            r2.user_id = r4
            org.telegram.tgnet.ConnectionsManager r4 = r35.getConnectionsManager()
            org.telegram.ui.-$$Lambda$ProfileActivity$zTzjokKzyCLASSNAMEgdLxqDPPzIi0cww r5 = new org.telegram.ui.-$$Lambda$ProfileActivity$zTzjokKzyCLASSNAMEgdLxqDPPzIi0cww
            r5.<init>()
            r4.sendRequest(r2, r5)
        L_0x02d7:
            org.telegram.ui.ProfileActivity$13 r2 = new org.telegram.ui.ProfileActivity$13
            r2.<init>(r11, r12)
            r2.setWillNotDraw(r13)
            r4 = 83
            android.widget.FrameLayout$LayoutParams r1 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r1, r4)
            r8.addView(r2, r1)
            org.telegram.ui.-$$Lambda$ProfileActivity$w48DAXRzunGFMNRBtXufx5MH1b4 r1 = new org.telegram.ui.-$$Lambda$ProfileActivity$w48DAXRzunGFMNRBtXufx5MH1b4
            r1.<init>(r0)
            r2.setOnClickListener(r1)
            android.widget.TextView r0 = new android.widget.TextView
            r0.<init>(r12)
            java.lang.String r1 = "windowBackgroundWhiteRedText"
            int r1 = org.telegram.ui.ActionBar.Theme.getColor(r1)
            r0.setTextColor(r1)
            r1 = 1097859072(0x41700000, float:15.0)
            r0.setTextSize(r15, r1)
            r1 = 17
            r0.setGravity(r1)
            android.graphics.Typeface r1 = org.telegram.messenger.AndroidUtilities.getTypeface(r17)
            r0.setTypeface(r1)
            r1 = 2131624493(0x7f0e022d, float:1.8876167E38)
            java.lang.String r4 = "BanFromTheGroup"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r4, r1)
            r0.setText(r1)
            r20 = -2
            r21 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
            r22 = 17
            r23 = 0
            r24 = 1065353216(0x3var_, float:1.0)
            r25 = 0
            r26 = 0
            android.widget.FrameLayout$LayoutParams r1 = org.telegram.ui.Components.LayoutHelper.createFrame(r20, r21, r22, r23, r24, r25, r26)
            r2.addView(r0, r1)
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            int r1 = org.telegram.messenger.AndroidUtilities.dp(r16)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r18)
            r0.setPadding(r13, r1, r13, r2)
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            int r1 = org.telegram.messenger.AndroidUtilities.dp(r18)
            r0.setBottomGlowOffset(r1)
            goto L_0x0351
        L_0x0348:
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            int r1 = org.telegram.messenger.AndroidUtilities.dp(r16)
            r0.setPadding(r13, r1, r13, r13)
        L_0x0351:
            org.telegram.ui.ProfileActivity$TopView r0 = new org.telegram.ui.ProfileActivity$TopView
            r0.<init>(r12)
            r11.topView = r0
            int r1 = org.telegram.ui.ActionBar.Theme.getColor(r3)
            r0.setBackgroundColor(r1)
            org.telegram.ui.ProfileActivity$TopView r0 = r11.topView
            r8.addView(r0)
            android.widget.FrameLayout r0 = new android.widget.FrameLayout
            r0.<init>(r12)
            r11.avatarContainer = r0
            r5 = 0
            r0.setPivotX(r5)
            android.widget.FrameLayout r0 = r11.avatarContainer
            r0.setPivotY(r5)
            android.widget.FrameLayout r0 = r11.avatarContainer
            r20 = 42
            r21 = 1109917696(0x42280000, float:42.0)
            r22 = 51
            r23 = 1115684864(0x42800000, float:64.0)
            r24 = 0
            r25 = 0
            r26 = 0
            android.widget.FrameLayout$LayoutParams r1 = org.telegram.ui.Components.LayoutHelper.createFrame(r20, r21, r22, r23, r24, r25, r26)
            r8.addView(r0, r1)
            org.telegram.ui.ProfileActivity$AvatarImageView r0 = new org.telegram.ui.ProfileActivity$AvatarImageView
            r0.<init>(r12)
            r11.avatarImage = r0
            r1 = 1101529088(0x41a80000, float:21.0)
            int r1 = org.telegram.messenger.AndroidUtilities.dp(r1)
            r0.setRoundRadius(r1)
            org.telegram.ui.ProfileActivity$AvatarImageView r0 = r11.avatarImage
            r0.setPivotX(r5)
            org.telegram.ui.ProfileActivity$AvatarImageView r0 = r11.avatarImage
            r0.setPivotY(r5)
            android.widget.FrameLayout r0 = r11.avatarContainer
            org.telegram.ui.ProfileActivity$AvatarImageView r1 = r11.avatarImage
            android.widget.FrameLayout$LayoutParams r2 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r6)
            r0.addView(r1, r2)
            org.telegram.ui.ProfileActivity$AvatarImageView r0 = r11.avatarImage
            org.telegram.ui.-$$Lambda$ProfileActivity$UJPIQLJnlPvpuaHK6bxzezN2-bI r1 = new org.telegram.ui.-$$Lambda$ProfileActivity$UJPIQLJnlPvpuaHK6bxzezN2-bI
            r1.<init>()
            r0.setOnClickListener(r1)
            org.telegram.ui.ProfileActivity$AvatarImageView r0 = r11.avatarImage
            org.telegram.ui.-$$Lambda$ProfileActivity$Trd7Hs-A8T0q97Wm-MnqrmFIfwA r1 = new org.telegram.ui.-$$Lambda$ProfileActivity$Trd7Hs-A8T0q97Wm-MnqrmFIfwA
            r1.<init>()
            r0.setOnLongClickListener(r1)
            org.telegram.ui.ProfileActivity$AvatarImageView r0 = r11.avatarImage
            r1 = 2131624012(0x7f0e004c, float:1.8875192E38)
            java.lang.String r2 = "AccDescrProfilePicture"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            r0.setContentDescription(r1)
            org.telegram.ui.ProfileActivity$15 r0 = new org.telegram.ui.ProfileActivity$15
            r0.<init>(r12)
            r11.avatarProgressView = r0
            r1 = 1104150528(0x41d00000, float:26.0)
            int r1 = org.telegram.messenger.AndroidUtilities.dp(r1)
            r0.setSize(r1)
            org.telegram.ui.Components.RadialProgressView r0 = r11.avatarProgressView
            r0.setProgressColor(r9)
            org.telegram.ui.Components.RadialProgressView r0 = r11.avatarProgressView
            r0.setNoProgress(r13)
            android.widget.FrameLayout r0 = r11.avatarContainer
            org.telegram.ui.Components.RadialProgressView r1 = r11.avatarProgressView
            android.widget.FrameLayout$LayoutParams r2 = org.telegram.ui.Components.LayoutHelper.createFrame(r9, r6)
            r0.addView(r1, r2)
            r11.showAvatarProgress(r13, r13)
            org.telegram.ui.Components.ProfileGalleryView r0 = r11.avatarsViewPager
            if (r0 == 0) goto L_0x0401
            r0.onDestroy()
        L_0x0401:
            org.telegram.ui.ProfileActivity$OverlaysView r0 = new org.telegram.ui.ProfileActivity$OverlaysView
            r0.<init>(r12)
            r11.overlaysView = r0
            org.telegram.ui.Components.ProfileGalleryView r4 = new org.telegram.ui.Components.ProfileGalleryView
            int r0 = r11.user_id
            if (r0 == 0) goto L_0x040f
            goto L_0x0412
        L_0x040f:
            int r0 = r11.chat_id
            int r0 = -r0
        L_0x0412:
            long r0 = (long) r0
            r2 = r0
            org.telegram.ui.ActionBar.ActionBar r1 = r11.actionBar
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            org.telegram.ui.ProfileActivity$AvatarImageView r6 = r11.avatarImage
            int r19 = r35.getClassGuid()
            org.telegram.ui.ProfileActivity$OverlaysView r13 = r11.overlaysView
            r21 = r0
            r0 = r4
            r22 = r1
            r1 = r36
            r15 = r4
            r4 = r22
            r5 = r21
            r9 = -1082130432(0xffffffffbvar_, float:-1.0)
            r7 = r19
            r9 = r8
            r8 = r13
            r0.<init>(r1, r2, r4, r5, r6, r7, r8)
            r11.avatarsViewPager = r15
            org.telegram.tgnet.TLRPC$ChatFull r0 = r11.chatInfo
            r15.setChatInfo(r0)
            org.telegram.ui.Components.ProfileGalleryView r0 = r11.avatarsViewPager
            r9.addView(r0)
            org.telegram.ui.ProfileActivity$OverlaysView r0 = r11.overlaysView
            r9.addView(r0)
            org.telegram.ui.ProfileActivity$PagerIndicatorView r0 = new org.telegram.ui.ProfileActivity$PagerIndicatorView
            r0.<init>(r12)
            r11.avatarsViewPagerIndicatorView = r0
            r1 = -1082130432(0xffffffffbvar_, float:-1.0)
            r2 = -1
            android.widget.FrameLayout$LayoutParams r1 = org.telegram.ui.Components.LayoutHelper.createFrame(r2, r1)
            r9.addView(r0, r1)
            org.telegram.ui.ActionBar.ActionBar r0 = r11.actionBar
            r9.addView(r0)
            r0 = 0
        L_0x045d:
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            int r2 = r1.length
            r3 = 3
            r4 = 2
            if (r0 >= r2) goto L_0x050d
            int r2 = r11.playProfileAnimation
            if (r2 != 0) goto L_0x046f
            if (r0 != 0) goto L_0x046f
            r2 = 18
            r5 = 0
            goto L_0x0509
        L_0x046f:
            org.telegram.ui.ActionBar.SimpleTextView r2 = new org.telegram.ui.ActionBar.SimpleTextView
            r2.<init>(r12)
            r1[r0] = r2
            r1 = 1
            if (r0 != r1) goto L_0x0487
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            r1 = r1[r0]
            java.lang.String r2 = "profile_title"
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
            r1.setTextColor(r2)
            goto L_0x0494
        L_0x0487:
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            r1 = r1[r0]
            java.lang.String r2 = "actionBarDefaultTitle"
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
            r1.setTextColor(r2)
        L_0x0494:
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            r1 = r1[r0]
            r2 = 18
            r1.setTextSize(r2)
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            r1 = r1[r0]
            r1.setGravity(r3)
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            r1 = r1[r0]
            android.graphics.Typeface r3 = org.telegram.messenger.AndroidUtilities.getTypeface(r17)
            r1.setTypeface(r3)
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            r1 = r1[r0]
            r3 = 1067869798(0x3fa66666, float:1.3)
            int r3 = org.telegram.messenger.AndroidUtilities.dp(r3)
            int r3 = -r3
            r1.setLeftDrawableTopPadding(r3)
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            r1 = r1[r0]
            r5 = 0
            r1.setPivotX(r5)
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            r1 = r1[r0]
            r1.setPivotY(r5)
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            r1 = r1[r0]
            if (r0 != 0) goto L_0x04d5
            r3 = 0
            goto L_0x04d7
        L_0x04d5:
            r3 = 1065353216(0x3var_, float:1.0)
        L_0x04d7:
            r1.setAlpha(r3)
            r1 = 1
            if (r0 != r1) goto L_0x04eb
            org.telegram.ui.ActionBar.SimpleTextView[] r3 = r11.nameTextView
            r3 = r3[r0]
            r3.setScrollNonFitText(r1)
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            r1 = r1[r0]
            r1.setImportantForAccessibility(r4)
        L_0x04eb:
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.nameTextView
            r1 = r1[r0]
            r24 = -2
            r25 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
            r26 = 51
            r27 = 1122762752(0x42eCLASSNAME, float:118.0)
            r28 = 0
            if (r0 != 0) goto L_0x04fe
            r29 = 1111490560(0x42400000, float:48.0)
            goto L_0x0500
        L_0x04fe:
            r29 = 0
        L_0x0500:
            r30 = 0
            android.widget.FrameLayout$LayoutParams r3 = org.telegram.ui.Components.LayoutHelper.createFrame(r24, r25, r26, r27, r28, r29, r30)
            r9.addView(r1, r3)
        L_0x0509:
            int r0 = r0 + 1
            goto L_0x045d
        L_0x050d:
            r5 = 0
            r0 = 0
        L_0x050f:
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.onlineTextView
            int r2 = r1.length
            if (r0 >= r2) goto L_0x0574
            org.telegram.ui.ActionBar.SimpleTextView r2 = new org.telegram.ui.ActionBar.SimpleTextView
            r2.<init>(r12)
            r1[r0] = r2
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.onlineTextView
            r1 = r1[r0]
            java.lang.String r2 = "avatar_subtitleInProfileBlue"
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
            r1.setTextColor(r2)
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.onlineTextView
            r1 = r1[r0]
            r2 = 14
            r1.setTextSize(r2)
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.onlineTextView
            r1 = r1[r0]
            r1.setGravity(r3)
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.onlineTextView
            r1 = r1[r0]
            if (r0 == 0) goto L_0x0544
            if (r0 != r4) goto L_0x0541
            goto L_0x0544
        L_0x0541:
            r2 = 1065353216(0x3var_, float:1.0)
            goto L_0x0545
        L_0x0544:
            r2 = 0
        L_0x0545:
            r1.setAlpha(r2)
            if (r0 <= 0) goto L_0x0551
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.onlineTextView
            r1 = r1[r0]
            r1.setImportantForAccessibility(r4)
        L_0x0551:
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r11.onlineTextView
            r1 = r1[r0]
            r24 = -2
            r25 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
            r26 = 51
            r27 = 1122762752(0x42eCLASSNAME, float:118.0)
            r28 = 0
            if (r0 != 0) goto L_0x0564
            r29 = 1111490560(0x42400000, float:48.0)
            goto L_0x0568
        L_0x0564:
            r2 = 1090519040(0x41000000, float:8.0)
            r29 = 1090519040(0x41000000, float:8.0)
        L_0x0568:
            r30 = 0
            android.widget.FrameLayout$LayoutParams r2 = org.telegram.ui.Components.LayoutHelper.createFrame(r24, r25, r26, r27, r28, r29, r30)
            r9.addView(r1, r2)
            int r0 = r0 + 1
            goto L_0x050f
        L_0x0574:
            org.telegram.ui.ProfileActivity$16 r0 = new org.telegram.ui.ProfileActivity$16
            r0.<init>(r11, r12, r12)
            r11.mediaCounterTextView = r0
            r0.setAlpha(r5)
            org.telegram.ui.Components.AudioPlayerAlert$ClippingTextViewSwitcher r0 = r11.mediaCounterTextView
            r24 = -2
            r25 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
            r26 = 51
            r27 = 1122762752(0x42eCLASSNAME, float:118.0)
            r28 = 0
            r29 = 1090519040(0x41000000, float:8.0)
            r30 = 0
            android.widget.FrameLayout$LayoutParams r1 = org.telegram.ui.Components.LayoutHelper.createFrame(r24, r25, r26, r27, r28, r29, r30)
            r9.addView(r0, r1)
            r35.updateProfileData()
            android.widget.ImageView r0 = new android.widget.ImageView
            r0.<init>(r12)
            r11.writeButton = r0
            r0 = 1113587712(0x42600000, float:56.0)
            int r1 = org.telegram.messenger.AndroidUtilities.dp(r0)
            java.lang.String r2 = "profile_actionBackground"
            int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
            java.lang.String r3 = "profile_actionPressedBackground"
            int r3 = org.telegram.ui.ActionBar.Theme.getColor(r3)
            android.graphics.drawable.Drawable r1 = org.telegram.ui.ActionBar.Theme.createSimpleSelectorCircleDrawable(r1, r2, r3)
            int r2 = android.os.Build.VERSION.SDK_INT
            r3 = 21
            if (r2 >= r3) goto L_0x05e8
            android.content.res.Resources r6 = r36.getResources()
            r7 = 2131165416(0x7var_e8, float:1.7945048E38)
            android.graphics.drawable.Drawable r6 = r6.getDrawable(r7)
            android.graphics.drawable.Drawable r6 = r6.mutate()
            android.graphics.PorterDuffColorFilter r7 = new android.graphics.PorterDuffColorFilter
            r8 = -16777216(0xfffffffffvar_, float:-1.7014118E38)
            android.graphics.PorterDuff$Mode r13 = android.graphics.PorterDuff.Mode.MULTIPLY
            r7.<init>(r8, r13)
            r6.setColorFilter(r7)
            org.telegram.ui.Components.CombinedDrawable r7 = new org.telegram.ui.Components.CombinedDrawable
            r8 = 0
            r7.<init>(r6, r1, r8, r8)
            int r1 = org.telegram.messenger.AndroidUtilities.dp(r0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r0)
            r7.setIconSize(r1, r6)
            r1 = r7
        L_0x05e8:
            android.widget.ImageView r6 = r11.writeButton
            r6.setBackgroundDrawable(r1)
            int r1 = r11.user_id
            r6 = 1073741824(0x40000000, float:2.0)
            if (r1 == 0) goto L_0x0633
            org.telegram.ui.Components.ImageUpdater r1 = r11.imageUpdater
            if (r1 == 0) goto L_0x061c
            android.widget.ImageView r1 = r11.writeButton
            r7 = 2131165634(0x7var_c2, float:1.794549E38)
            r1.setImageResource(r7)
            android.widget.ImageView r1 = r11.writeButton
            r7 = 2131623963(0x7f0e001b, float:1.8875092E38)
            java.lang.String r8 = "AccDescrChangeProfilePicture"
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r8, r7)
            r1.setContentDescription(r7)
            android.widget.ImageView r1 = r11.writeButton
            int r7 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r8 = org.telegram.messenger.AndroidUtilities.dp(r6)
            r13 = 0
            r1.setPadding(r7, r8, r13, r13)
            goto L_0x0649
        L_0x061c:
            android.widget.ImageView r1 = r11.writeButton
            r7 = 2131165934(0x7var_ee, float:1.79461E38)
            r1.setImageResource(r7)
            android.widget.ImageView r1 = r11.writeButton
            r7 = 2131623999(0x7f0e003f, float:1.8875165E38)
            java.lang.String r8 = "AccDescrOpenChat"
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r8, r7)
            r1.setContentDescription(r7)
            goto L_0x0649
        L_0x0633:
            android.widget.ImageView r1 = r11.writeButton
            r7 = 2131165930(0x7var_ea, float:1.794609E38)
            r1.setImageResource(r7)
            android.widget.ImageView r1 = r11.writeButton
            r7 = 2131627660(0x7f0e0e8c, float:1.888259E38)
            java.lang.String r8 = "ViewDiscussion"
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r8, r7)
            r1.setContentDescription(r7)
        L_0x0649:
            android.widget.ImageView r1 = r11.writeButton
            android.graphics.PorterDuffColorFilter r7 = new android.graphics.PorterDuffColorFilter
            java.lang.String r8 = "profile_actionIcon"
            int r8 = org.telegram.ui.ActionBar.Theme.getColor(r8)
            android.graphics.PorterDuff$Mode r13 = android.graphics.PorterDuff.Mode.MULTIPLY
            r7.<init>(r8, r13)
            r1.setColorFilter(r7)
            android.widget.ImageView r1 = r11.writeButton
            android.widget.ImageView$ScaleType r7 = android.widget.ImageView.ScaleType.CENTER
            r1.setScaleType(r7)
            if (r2 < r3) goto L_0x06cc
            android.animation.StateListAnimator r1 = new android.animation.StateListAnimator
            r1.<init>()
            r7 = 1
            int[] r8 = new int[r7]
            r7 = 16842919(0x10100a7, float:2.3694026E-38)
            r13 = 0
            r8[r13] = r7
            android.widget.ImageView r7 = r11.writeButton
            android.util.Property r15 = android.view.View.TRANSLATION_Z
            float[] r0 = new float[r4]
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r6)
            float r5 = (float) r5
            r0[r13] = r5
            r5 = 1082130432(0x40800000, float:4.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            float r5 = (float) r5
            r17 = 1
            r0[r17] = r5
            android.animation.ObjectAnimator r0 = android.animation.ObjectAnimator.ofFloat(r7, r15, r0)
            r6 = 200(0xc8, double:9.9E-322)
            android.animation.ObjectAnimator r0 = r0.setDuration(r6)
            r1.addState(r8, r0)
            int[] r0 = new int[r13]
            android.widget.ImageView r6 = r11.writeButton
            float[] r7 = new float[r4]
            r8 = 1082130432(0x40800000, float:4.0)
            int r8 = org.telegram.messenger.AndroidUtilities.dp(r8)
            float r8 = (float) r8
            r7[r13] = r8
            r5 = 1073741824(0x40000000, float:2.0)
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            float r5 = (float) r5
            r8 = 1
            r7[r8] = r5
            android.animation.ObjectAnimator r5 = android.animation.ObjectAnimator.ofFloat(r6, r15, r7)
            r6 = 200(0xc8, double:9.9E-322)
            android.animation.ObjectAnimator r5 = r5.setDuration(r6)
            r1.addState(r0, r5)
            android.widget.ImageView r0 = r11.writeButton
            r0.setStateListAnimator(r1)
            android.widget.ImageView r0 = r11.writeButton
            org.telegram.ui.ProfileActivity$17 r1 = new org.telegram.ui.ProfileActivity$17
            r1.<init>(r11)
            r0.setOutlineProvider(r1)
        L_0x06cc:
            android.widget.ImageView r0 = r11.writeButton
            if (r2 < r3) goto L_0x06d5
            r1 = 56
            r28 = 56
            goto L_0x06d9
        L_0x06d5:
            r1 = 60
            r28 = 60
        L_0x06d9:
            if (r2 < r3) goto L_0x06de
            r29 = 1113587712(0x42600000, float:56.0)
            goto L_0x06e2
        L_0x06de:
            r1 = 1114636288(0x42700000, float:60.0)
            r29 = 1114636288(0x42700000, float:60.0)
        L_0x06e2:
            r30 = 53
            r31 = 0
            r32 = 0
            r33 = 1098907648(0x41800000, float:16.0)
            r34 = 0
            android.widget.FrameLayout$LayoutParams r1 = org.telegram.ui.Components.LayoutHelper.createFrame(r28, r29, r30, r31, r32, r33, r34)
            r9.addView(r0, r1)
            android.widget.ImageView r0 = r11.writeButton
            org.telegram.ui.-$$Lambda$ProfileActivity$h0jCvZ-O8pRXtGrdiKARn9h60dc r1 = new org.telegram.ui.-$$Lambda$ProfileActivity$h0jCvZ-O8pRXtGrdiKARn9h60dc
            r1.<init>()
            r0.setOnClickListener(r1)
            r0 = 0
            r11.needLayout(r0)
            r1 = -1
            if (r10 == r1) goto L_0x0722
            if (r14 == 0) goto L_0x0722
            android.widget.ImageView r1 = r11.writeButton
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            r1.setTag(r0)
            android.widget.ImageView r0 = r11.writeButton
            r1 = 1045220557(0x3e4ccccd, float:0.2)
            r0.setScaleX(r1)
            android.widget.ImageView r0 = r11.writeButton
            r0.setScaleY(r1)
            android.widget.ImageView r0 = r11.writeButton
            r1 = 0
            r0.setAlpha(r1)
        L_0x0722:
            org.telegram.ui.Components.RecyclerListView r0 = r11.listView
            org.telegram.ui.ProfileActivity$18 r1 = new org.telegram.ui.ProfileActivity$18
            r1.<init>()
            r0.setOnScrollListener(r1)
            org.telegram.ui.Components.UndoView r0 = new org.telegram.ui.Components.UndoView
            r0.<init>(r12)
            r11.undoView = r0
            r12 = -1
            r13 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
            r14 = 83
            r15 = 1090519040(0x41000000, float:8.0)
            r16 = 0
            r17 = 1090519040(0x41000000, float:8.0)
            r18 = 1090519040(0x41000000, float:8.0)
            android.widget.FrameLayout$LayoutParams r1 = org.telegram.ui.Components.LayoutHelper.createFrame(r12, r13, r14, r15, r16, r17, r18)
            r9.addView(r0, r1)
            float[] r0 = new float[r4]
            r0 = {0, NUM} // fill-array
            android.animation.ValueAnimator r0 = android.animation.ValueAnimator.ofFloat(r0)
            r11.expandAnimator = r0
            org.telegram.ui.-$$Lambda$ProfileActivity$Usa5qL0j8FVFtRj5sFo0mvey0is r1 = new org.telegram.ui.-$$Lambda$ProfileActivity$Usa5qL0j8FVFtRj5sFo0mvey0is
            r1.<init>()
            r0.addUpdateListener(r1)
            android.animation.ValueAnimator r0 = r11.expandAnimator
            org.telegram.ui.Components.CubicBezierInterpolator r1 = org.telegram.ui.Components.CubicBezierInterpolator.EASE_BOTH
            r0.setInterpolator(r1)
            android.animation.ValueAnimator r0 = r11.expandAnimator
            org.telegram.ui.ProfileActivity$19 r1 = new org.telegram.ui.ProfileActivity$19
            r1.<init>()
            r0.addListener(r1)
            r35.updateSelectedMediaTabText()
            android.view.View r0 = r11.fragmentView
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.createView(android.content.Context):android.view.View");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createView$5 */
    public /* synthetic */ void lambda$createView$5$ProfileActivity(long j, View view, int i, float f, float f2) {
        TLRPC$ChatParticipant tLRPC$ChatParticipant;
        long j2 = j;
        int i2 = i;
        if (getParentActivity() != null) {
            if (i2 == this.settingsKeyRow) {
                Bundle bundle = new Bundle();
                bundle.putInt("chat_id", (int) (this.dialog_id >> 32));
                presentFragment(new IdenticonActivity(bundle));
            } else if (i2 == this.settingsTimerRow) {
                showDialog(AlertsCreator.createTTLAlert(getParentActivity(), this.currentEncryptedChat).create());
            } else if (i2 == this.notificationsRow) {
                if ((!LocaleController.isRTL || f > ((float) AndroidUtilities.dp(76.0f))) && (LocaleController.isRTL || f < ((float) (view.getMeasuredWidth() - AndroidUtilities.dp(76.0f))))) {
                    AlertsCreator.showCustomNotificationsDialog(this, j, -1, (ArrayList<NotificationsSettingsActivity.NotificationException>) null, this.currentAccount, new MessagesStorage.IntCallback() {
                        public final void run(int i) {
                            ProfileActivity.this.lambda$null$3$ProfileActivity(i);
                        }
                    });
                    return;
                }
                NotificationsCheckCell notificationsCheckCell = (NotificationsCheckCell) view;
                boolean z = !notificationsCheckCell.isChecked();
                boolean isGlobalNotificationsEnabled = getNotificationsController().isGlobalNotificationsEnabled(j);
                long j3 = 0;
                if (z) {
                    SharedPreferences.Editor edit = MessagesController.getNotificationsSettings(this.currentAccount).edit();
                    if (isGlobalNotificationsEnabled) {
                        edit.remove("notify2_" + j);
                    } else {
                        edit.putInt("notify2_" + j, 0);
                    }
                    getMessagesStorage().setDialogFlags(j, 0);
                    edit.commit();
                    TLRPC$Dialog tLRPC$Dialog = getMessagesController().dialogs_dict.get(j);
                    if (tLRPC$Dialog != null) {
                        tLRPC$Dialog.notify_settings = new TLRPC$TL_peerNotifySettings();
                    }
                } else {
                    SharedPreferences.Editor edit2 = MessagesController.getNotificationsSettings(this.currentAccount).edit();
                    if (!isGlobalNotificationsEnabled) {
                        edit2.remove("notify2_" + j);
                    } else {
                        edit2.putInt("notify2_" + j, 2);
                        j3 = 1;
                    }
                    getNotificationsController().removeNotificationsForDialog(j);
                    getMessagesStorage().setDialogFlags(j, j3);
                    edit2.commit();
                    TLRPC$Dialog tLRPC$Dialog2 = getMessagesController().dialogs_dict.get(j);
                    if (tLRPC$Dialog2 != null) {
                        TLRPC$TL_peerNotifySettings tLRPC$TL_peerNotifySettings = new TLRPC$TL_peerNotifySettings();
                        tLRPC$Dialog2.notify_settings = tLRPC$TL_peerNotifySettings;
                        if (isGlobalNotificationsEnabled) {
                            tLRPC$TL_peerNotifySettings.mute_until = Integer.MAX_VALUE;
                        }
                    }
                }
                getNotificationsController().updateServerNotificationsSettings(j);
                notificationsCheckCell.setChecked(z);
                RecyclerListView.Holder holder = (RecyclerListView.Holder) this.listView.findViewHolderForPosition(this.notificationsRow);
                if (holder != null) {
                    this.listAdapter.onBindViewHolder(holder, this.notificationsRow);
                }
            } else if (i2 == this.unblockRow) {
                getMessagesController().unblockPeer(this.user_id);
                if (BulletinFactory.canShowBulletin(this)) {
                    BulletinFactory.createBanBulletin(this, false).show();
                }
            } else if (i2 == this.sendMessageRow) {
                onWriteButtonClick();
            } else if (i2 == this.reportRow) {
                AlertsCreator.createReportAlert(getParentActivity(), getDialogId(), 0, this);
            } else if (i2 >= this.membersStartRow && i2 < this.membersEndRow) {
                if (!this.sortedUsers.isEmpty()) {
                    tLRPC$ChatParticipant = this.chatInfo.participants.participants.get(this.sortedUsers.get(i2 - this.membersStartRow).intValue());
                } else {
                    tLRPC$ChatParticipant = this.chatInfo.participants.participants.get(i2 - this.membersStartRow);
                }
                onMemberClick(tLRPC$ChatParticipant, false);
            } else if (i2 == this.addMemberRow) {
                openAddMember();
            } else if (i2 == this.usernameRow) {
                if (this.currentChat != null) {
                    try {
                        Intent intent = new Intent("android.intent.action.SEND");
                        intent.setType("text/plain");
                        if (!TextUtils.isEmpty(this.chatInfo.about)) {
                            intent.putExtra("android.intent.extra.TEXT", this.currentChat.title + "\n" + this.chatInfo.about + "\nhttps://" + getMessagesController().linkPrefix + "/" + this.currentChat.username);
                        } else {
                            intent.putExtra("android.intent.extra.TEXT", this.currentChat.title + "\nhttps://" + getMessagesController().linkPrefix + "/" + this.currentChat.username);
                        }
                        getParentActivity().startActivityForResult(Intent.createChooser(intent, LocaleController.getString("BotShare", NUM)), 500);
                    } catch (Exception e) {
                        FileLog.e((Throwable) e);
                    }
                }
            } else if (i2 == this.locationRow) {
                if (this.chatInfo.location instanceof TLRPC$TL_channelLocation) {
                    LocationActivity locationActivity = new LocationActivity(5);
                    locationActivity.setChatLocation(this.chat_id, (TLRPC$TL_channelLocation) this.chatInfo.location);
                    presentFragment(locationActivity);
                }
            } else if (i2 == this.joinRow) {
                getMessagesController().addUserToChat(this.currentChat.id, getUserConfig().getCurrentUser(), 0, (String) null, this, (Runnable) null);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeSearchByActiveAction, new Object[0]);
            } else if (i2 == this.subscribersRow) {
                Bundle bundle2 = new Bundle();
                bundle2.putInt("chat_id", this.chat_id);
                bundle2.putInt("type", 2);
                ChatUsersActivity chatUsersActivity = new ChatUsersActivity(bundle2);
                chatUsersActivity.setInfo(this.chatInfo);
                presentFragment(chatUsersActivity);
            } else if (i2 == this.administratorsRow) {
                Bundle bundle3 = new Bundle();
                bundle3.putInt("chat_id", this.chat_id);
                bundle3.putInt("type", 1);
                ChatUsersActivity chatUsersActivity2 = new ChatUsersActivity(bundle3);
                chatUsersActivity2.setInfo(this.chatInfo);
                presentFragment(chatUsersActivity2);
            } else if (i2 == this.blockedUsersRow) {
                Bundle bundle4 = new Bundle();
                bundle4.putInt("chat_id", this.chat_id);
                bundle4.putInt("type", 0);
                ChatUsersActivity chatUsersActivity3 = new ChatUsersActivity(bundle4);
                chatUsersActivity3.setInfo(this.chatInfo);
                presentFragment(chatUsersActivity3);
            } else if (i2 == this.notificationRow) {
                presentFragment(new NotificationsSettingsActivity());
            } else if (i2 == this.privacyRow) {
                presentFragment(new PrivacySettingsActivity());
            } else if (i2 == this.dataRow) {
                presentFragment(new DataSettingsActivity());
            } else if (i2 == this.chatRow) {
                presentFragment(new ThemeActivity(0));
            } else if (i2 == this.filtersRow) {
                presentFragment(new FiltersSetupActivity());
            } else if (i2 == this.devicesRow) {
                presentFragment(new SessionsActivity(0));
            } else if (i2 == this.questionRow) {
                showDialog(AlertsCreator.createSupportAlert(this));
            } else if (i2 == this.faqRow) {
                Browser.openUrl((Context) getParentActivity(), LocaleController.getString("TelegramFaqUrl", NUM));
            } else if (i2 == this.policyRow) {
                Browser.openUrl((Context) getParentActivity(), LocaleController.getString("PrivacyPolicyUrl", NUM));
            } else if (i2 == this.sendLogsRow) {
                sendLogs();
            } else if (i2 == this.clearLogsRow) {
                FileLog.cleanupLogs();
            } else if (i2 == this.switchBackendRow) {
                if (getParentActivity() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder((Context) getParentActivity());
                    builder.setMessage(LocaleController.getString("AreYouSure", NUM));
                    builder.setTitle(LocaleController.getString("AppName", NUM));
                    builder.setPositiveButton(LocaleController.getString("OK", NUM), new DialogInterface.OnClickListener() {
                        public final void onClick(DialogInterface dialogInterface, int i) {
                            ProfileActivity.this.lambda$null$4$ProfileActivity(dialogInterface, i);
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
                    showDialog(builder.create());
                }
            } else if (i2 == this.languageRow) {
                presentFragment(new LanguageSelectActivity());
            } else if (i2 == this.setUsernameRow) {
                presentFragment(new ChangeUsernameActivity());
            } else if (i2 == this.bioRow) {
                if (this.userInfo != null) {
                    presentFragment(new ChangeBioActivity());
                }
            } else if (i2 == this.numberRow) {
                presentFragment(new ActionIntroActivity(3));
            } else if (i2 == this.setAvatarRow) {
                onWriteButtonClick();
            } else {
                processOnClickOrPress(i2);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$3 */
    public /* synthetic */ void lambda$null$3$ProfileActivity(int i) {
        this.listAdapter.notifyItemChanged(this.notificationsRow);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$4 */
    public /* synthetic */ void lambda$null$4$ProfileActivity(DialogInterface dialogInterface, int i) {
        SharedConfig.pushAuthKey = null;
        SharedConfig.pushAuthKeyId = null;
        SharedConfig.saveConfig();
        getConnectionsManager().switchBackend();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00a2  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00a9  */
    /* renamed from: lambda$createView$6 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$createView$6$ProfileActivity(android.view.View r8, int r9) {
        /*
            r7 = this;
            if (r9 >= 0) goto L_0x0003
            return
        L_0x0003:
            int r8 = r7.numberRow
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            org.telegram.ui.ProfileActivity$SearchAdapter r0 = r7.searchAdapter
            boolean r0 = r0.searchWas
            r1 = 0
            r2 = 1
            if (r0 == 0) goto L_0x0050
            org.telegram.ui.ProfileActivity$SearchAdapter r0 = r7.searchAdapter
            java.util.ArrayList r0 = r0.searchResults
            int r0 = r0.size()
            if (r9 >= r0) goto L_0x002b
            org.telegram.ui.ProfileActivity$SearchAdapter r8 = r7.searchAdapter
            java.util.ArrayList r8 = r8.searchResults
            java.lang.Object r8 = r8.get(r9)
            goto L_0x009d
        L_0x002b:
            org.telegram.ui.ProfileActivity$SearchAdapter r0 = r7.searchAdapter
            java.util.ArrayList r0 = r0.searchResults
            int r0 = r0.size()
            int r0 = r0 + r2
            int r9 = r9 - r0
            if (r9 < 0) goto L_0x009d
            org.telegram.ui.ProfileActivity$SearchAdapter r0 = r7.searchAdapter
            java.util.ArrayList r0 = r0.faqSearchResults
            int r0 = r0.size()
            if (r9 >= r0) goto L_0x009d
            org.telegram.ui.ProfileActivity$SearchAdapter r8 = r7.searchAdapter
            java.util.ArrayList r8 = r8.faqSearchResults
            java.lang.Object r8 = r8.get(r9)
            goto L_0x009d
        L_0x0050:
            org.telegram.ui.ProfileActivity$SearchAdapter r0 = r7.searchAdapter
            java.util.ArrayList r0 = r0.recentSearches
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x005e
            int r9 = r9 + -1
        L_0x005e:
            if (r9 < 0) goto L_0x0077
            org.telegram.ui.ProfileActivity$SearchAdapter r0 = r7.searchAdapter
            java.util.ArrayList r0 = r0.recentSearches
            int r0 = r0.size()
            if (r9 >= r0) goto L_0x0077
            org.telegram.ui.ProfileActivity$SearchAdapter r8 = r7.searchAdapter
            java.util.ArrayList r8 = r8.recentSearches
            java.lang.Object r8 = r8.get(r9)
            goto L_0x009d
        L_0x0077:
            org.telegram.ui.ProfileActivity$SearchAdapter r0 = r7.searchAdapter
            java.util.ArrayList r0 = r0.recentSearches
            int r0 = r0.size()
            int r0 = r0 + r2
            int r9 = r9 - r0
            if (r9 < 0) goto L_0x009d
            org.telegram.ui.ProfileActivity$SearchAdapter r0 = r7.searchAdapter
            java.util.ArrayList r0 = r0.faqSearchArray
            int r0 = r0.size()
            if (r9 >= r0) goto L_0x009d
            org.telegram.ui.ProfileActivity$SearchAdapter r8 = r7.searchAdapter
            java.util.ArrayList r8 = r8.faqSearchArray
            java.lang.Object r8 = r8.get(r9)
            r9 = 0
            goto L_0x009e
        L_0x009d:
            r9 = 1
        L_0x009e:
            boolean r0 = r8 instanceof org.telegram.ui.ProfileActivity.SearchAdapter.SearchResult
            if (r0 == 0) goto L_0x00a9
            r0 = r8
            org.telegram.ui.ProfileActivity$SearchAdapter$SearchResult r0 = (org.telegram.ui.ProfileActivity.SearchAdapter.SearchResult) r0
            r0.open()
            goto L_0x00ca
        L_0x00a9:
            boolean r0 = r8 instanceof org.telegram.messenger.MessagesController.FaqSearchResult
            if (r0 == 0) goto L_0x00ca
            r0 = r8
            org.telegram.messenger.MessagesController$FaqSearchResult r0 = (org.telegram.messenger.MessagesController.FaqSearchResult) r0
            int r3 = r7.currentAccount
            org.telegram.messenger.NotificationCenter r3 = org.telegram.messenger.NotificationCenter.getInstance(r3)
            int r4 = org.telegram.messenger.NotificationCenter.openArticle
            r5 = 2
            java.lang.Object[] r5 = new java.lang.Object[r5]
            org.telegram.ui.ProfileActivity$SearchAdapter r6 = r7.searchAdapter
            org.telegram.tgnet.TLRPC$WebPage r6 = r6.faqWebPage
            r5[r1] = r6
            java.lang.String r0 = r0.url
            r5[r2] = r0
            r3.postNotificationName(r4, r5)
        L_0x00ca:
            if (r9 == 0) goto L_0x00d3
            if (r8 == 0) goto L_0x00d3
            org.telegram.ui.ProfileActivity$SearchAdapter r9 = r7.searchAdapter
            r9.addRecent(r8)
        L_0x00d3:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.lambda$createView$6$ProfileActivity(android.view.View, int):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createView$8 */
    public /* synthetic */ boolean lambda$createView$8$ProfileActivity(View view, int i) {
        if (this.searchAdapter.isSearchWas() || this.searchAdapter.recentSearches.isEmpty()) {
            return false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder((Context) getParentActivity());
        builder.setTitle(LocaleController.getString("AppName", NUM));
        builder.setMessage(LocaleController.getString("ClearSearch", NUM));
        builder.setPositiveButton(LocaleController.getString("ClearButton", NUM).toUpperCase(), new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                ProfileActivity.this.lambda$null$7$ProfileActivity(dialogInterface, i);
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
        showDialog(builder.create());
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$7 */
    public /* synthetic */ void lambda$null$7$ProfileActivity(DialogInterface dialogInterface, int i) {
        this.searchAdapter.clearRecent();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createView$10 */
    public /* synthetic */ void lambda$createView$10$ProfileActivity(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        if (tLObject != null) {
            AndroidUtilities.runOnUIThread(new Runnable(tLObject) {
                public final /* synthetic */ TLObject f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ProfileActivity.this.lambda$null$9$ProfileActivity(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$9 */
    public /* synthetic */ void lambda$null$9$ProfileActivity(TLObject tLObject) {
        this.currentChannelParticipant = ((TLRPC$TL_channels_channelParticipant) tLObject).participant;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createView$11 */
    public /* synthetic */ void lambda$createView$11$ProfileActivity(TLRPC$Chat tLRPC$Chat, View view) {
        int i = this.user_id;
        int i2 = this.banFromGroup;
        TLRPC$TL_chatBannedRights tLRPC$TL_chatBannedRights = tLRPC$Chat.default_banned_rights;
        TLRPC$ChannelParticipant tLRPC$ChannelParticipant = this.currentChannelParticipant;
        ChatRightsEditActivity chatRightsEditActivity = new ChatRightsEditActivity(i, i2, (TLRPC$TL_chatAdminRights) null, tLRPC$TL_chatBannedRights, tLRPC$ChannelParticipant != null ? tLRPC$ChannelParticipant.banned_rights : null, "", 1, true, false);
        chatRightsEditActivity.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
            public void didSetRights(int i, TLRPC$TL_chatAdminRights tLRPC$TL_chatAdminRights, TLRPC$TL_chatBannedRights tLRPC$TL_chatBannedRights, String str) {
                ProfileActivity.this.removeSelfFromStack();
            }

            public void didChangeOwner(TLRPC$User tLRPC$User) {
                ProfileActivity.this.undoView.showWithAction((long) (-ProfileActivity.this.chat_id), ProfileActivity.this.currentChat.megagroup ? 10 : 9, (Object) tLRPC$User);
            }
        });
        presentFragment(chatRightsEditActivity);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createView$12 */
    public /* synthetic */ void lambda$createView$12$ProfileActivity(View view) {
        RecyclerView.ViewHolder findContainingViewHolder;
        Integer num;
        if (this.avatarBig == null) {
            if (!AndroidUtilities.isTablet() && !this.isInLandscapeMode && this.avatarImage.getImageReceiver().hasNotThumb()) {
                this.openingAvatar = true;
                this.allowPullingDown = true;
                View view2 = null;
                int i = 0;
                while (true) {
                    if (i >= this.listView.getChildCount()) {
                        break;
                    }
                    RecyclerListView recyclerListView = this.listView;
                    if (recyclerListView.getChildAdapterPosition(recyclerListView.getChildAt(i)) == 0) {
                        view2 = this.listView.getChildAt(i);
                        break;
                    }
                    i++;
                }
                if (!(view2 == null || (findContainingViewHolder = this.listView.findContainingViewHolder(view2)) == null || (num = this.positionToOffset.get(Integer.valueOf(findContainingViewHolder.getAdapterPosition()))) == null)) {
                    this.listView.smoothScrollBy(0, -(num.intValue() + ((this.listView.getPaddingTop() - view2.getTop()) - this.actionBar.getMeasuredHeight())), CubicBezierInterpolator.EASE_OUT_QUINT);
                    return;
                }
            }
            openAvatar();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createView$13 */
    public /* synthetic */ boolean lambda$createView$13$ProfileActivity(View view) {
        if (this.avatarBig != null) {
            return false;
        }
        openAvatar();
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createView$14 */
    public /* synthetic */ void lambda$createView$14$ProfileActivity(View view) {
        if (this.writeButton.getTag() == null) {
            onWriteButtonClick();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createView$15 */
    public /* synthetic */ void lambda$createView$15$ProfileActivity(ValueAnimator valueAnimator) {
        int i;
        int currentActionBarHeight = ActionBar.getCurrentActionBarHeight() + (this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
        float[] fArr = this.expandAnimatorValues;
        float animatedFraction = valueAnimator.getAnimatedFraction();
        this.currentExpanAnimatorFracture = animatedFraction;
        float lerp = AndroidUtilities.lerp(fArr, animatedFraction);
        this.avatarContainer.setScaleX(this.avatarScale);
        this.avatarContainer.setScaleY(this.avatarScale);
        this.avatarContainer.setTranslationX(AndroidUtilities.lerp(this.avatarX, 0.0f, lerp));
        this.avatarContainer.setTranslationY(AndroidUtilities.lerp((float) Math.ceil((double) this.avatarY), 0.0f, lerp));
        this.avatarImage.setRoundRadius((int) AndroidUtilities.lerp(AndroidUtilities.dpf2(21.0f), 0.0f, lerp));
        ActionBarMenuItem actionBarMenuItem = this.searchItem;
        if (actionBarMenuItem != null) {
            actionBarMenuItem.setAlpha(1.0f - lerp);
        }
        if (this.extraHeight > ((float) AndroidUtilities.dp(88.0f)) && this.expandProgress < 0.33f) {
            refreshNameAndOnlineXY();
        }
        ScamDrawable scamDrawable2 = this.scamDrawable;
        if (scamDrawable2 != null) {
            scamDrawable2.setColor(ColorUtils.blendARGB(Theme.getColor("avatar_subtitleInProfileBlue"), Color.argb(179, 255, 255, 255), lerp));
        }
        Drawable drawable = this.lockIconDrawable;
        if (drawable != null) {
            drawable.setColorFilter(ColorUtils.blendARGB(Theme.getColor("chat_lockIcon"), -1, lerp), PorterDuff.Mode.MULTIPLY);
        }
        CrossfadeDrawable crossfadeDrawable = this.verifiedCrossfadeDrawable;
        if (crossfadeDrawable != null) {
            crossfadeDrawable.setProgress(lerp);
        }
        float dpf2 = AndroidUtilities.dpf2(8.0f);
        float dpvar_ = AndroidUtilities.dpf2(16.0f) - ((float) this.nameTextView[1].getLeft());
        float f = (float) currentActionBarHeight;
        float dpvar_ = ((this.extraHeight + f) - AndroidUtilities.dpf2(38.0f)) - ((float) this.nameTextView[1].getBottom());
        float f2 = this.nameX;
        float f3 = this.nameY;
        float f4 = 1.0f - lerp;
        float f5 = f4 * f4;
        float f6 = f4 * 2.0f * lerp;
        float f7 = lerp * lerp;
        float f8 = (f2 * f5) + ((dpf2 + f2 + ((dpvar_ - f2) / 2.0f)) * f6) + (dpvar_ * f7);
        float f9 = (f3 * f5) + ((dpf2 + f3 + ((dpvar_ - f3) / 2.0f)) * f6) + (dpvar_ * f7);
        float dpvar_ = AndroidUtilities.dpf2(16.0f) - ((float) this.onlineTextView[1].getLeft());
        float dpvar_ = ((this.extraHeight + f) - AndroidUtilities.dpf2(18.0f)) - ((float) this.onlineTextView[1].getBottom());
        float var_ = this.onlineX;
        float var_ = this.onlineY;
        float var_ = (var_ * f5) + ((dpf2 + var_ + ((dpvar_ - var_) / 2.0f)) * f6) + (dpvar_ * f7);
        float var_ = (f5 * var_) + (f6 * (dpf2 + var_ + ((dpvar_ - var_) / 2.0f))) + (f7 * dpvar_);
        this.nameTextView[1].setTranslationX(f8);
        this.nameTextView[1].setTranslationY(f9);
        this.onlineTextView[1].setTranslationX(var_);
        this.onlineTextView[1].setTranslationY(var_);
        this.mediaCounterTextView.setTranslationX(var_);
        this.mediaCounterTextView.setTranslationY(var_);
        Object tag = this.onlineTextView[1].getTag();
        if (tag instanceof String) {
            i = Theme.getColor((String) tag);
        } else {
            i = Theme.getColor("avatar_subtitleInProfileBlue");
        }
        this.onlineTextView[1].setTextColor(ColorUtils.blendARGB(i, Color.argb(179, 255, 255, 255), lerp));
        if (this.extraHeight > ((float) AndroidUtilities.dp(88.0f))) {
            SimpleTextView[] simpleTextViewArr = this.nameTextView;
            simpleTextViewArr[1].setPivotY(AndroidUtilities.lerp(0.0f, (float) simpleTextViewArr[1].getMeasuredHeight(), lerp));
            this.nameTextView[1].setScaleX(AndroidUtilities.lerp(1.12f, 1.67f, lerp));
            this.nameTextView[1].setScaleY(AndroidUtilities.lerp(1.12f, 1.67f, lerp));
        }
        needLayoutText(Math.min(1.0f, this.extraHeight / ((float) AndroidUtilities.dp(88.0f))));
        this.nameTextView[1].setTextColor(ColorUtils.blendARGB(Theme.getColor("profile_title"), -1, lerp));
        this.actionBar.setItemsColor(ColorUtils.blendARGB(Theme.getColor("actionBarDefaultIcon"), -1, lerp), false);
        this.avatarImage.setForegroundAlpha(lerp);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.avatarContainer.getLayoutParams();
        layoutParams.width = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(42.0f), ((float) this.listView.getMeasuredWidth()) / this.avatarScale, lerp);
        layoutParams.height = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(42.0f), (this.extraHeight + f) / this.avatarScale, lerp);
        layoutParams.leftMargin = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(64.0f), 0.0f, lerp);
        this.avatarContainer.requestLayout();
    }

    public long getDialogId() {
        long j = this.dialog_id;
        if (j != 0) {
            return j;
        }
        int i = this.user_id;
        if (i != 0) {
            return (long) i;
        }
        return (long) (-this.chat_id);
    }

    public TLRPC$Chat getCurrentChat() {
        return this.currentChat;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004b, code lost:
        r0 = getMessagesController().getChat(java.lang.Integer.valueOf(r4.chat_id));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void openAvatar() {
        /*
            r4 = this;
            org.telegram.ui.Components.RecyclerListView r0 = r4.listView
            int r0 = r0.getScrollState()
            r1 = 1
            if (r0 != r1) goto L_0x000a
            return
        L_0x000a:
            int r0 = r4.user_id
            if (r0 == 0) goto L_0x0047
            org.telegram.messenger.MessagesController r0 = r4.getMessagesController()
            int r1 = r4.user_id
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            org.telegram.tgnet.TLRPC$User r0 = r0.getUser(r1)
            org.telegram.tgnet.TLRPC$UserProfilePhoto r1 = r0.photo
            if (r1 == 0) goto L_0x00ac
            org.telegram.tgnet.TLRPC$FileLocation r1 = r1.photo_big
            if (r1 == 0) goto L_0x00ac
            org.telegram.ui.PhotoViewer r1 = org.telegram.ui.PhotoViewer.getInstance()
            android.app.Activity r2 = r4.getParentActivity()
            r1.setParentActivity(r2)
            org.telegram.tgnet.TLRPC$UserProfilePhoto r1 = r0.photo
            int r2 = r1.dc_id
            if (r2 == 0) goto L_0x0039
            org.telegram.tgnet.TLRPC$FileLocation r1 = r1.photo_big
            r1.dc_id = r2
        L_0x0039:
            org.telegram.ui.PhotoViewer r1 = org.telegram.ui.PhotoViewer.getInstance()
            org.telegram.tgnet.TLRPC$UserProfilePhoto r0 = r0.photo
            org.telegram.tgnet.TLRPC$FileLocation r0 = r0.photo_big
            org.telegram.ui.PhotoViewer$PhotoViewerProvider r2 = r4.provider
            r1.openPhoto(r0, r2)
            goto L_0x00ac
        L_0x0047:
            int r0 = r4.chat_id
            if (r0 == 0) goto L_0x00ac
            org.telegram.messenger.MessagesController r0 = r4.getMessagesController()
            int r1 = r4.chat_id
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            org.telegram.tgnet.TLRPC$Chat r0 = r0.getChat(r1)
            org.telegram.tgnet.TLRPC$ChatPhoto r1 = r0.photo
            if (r1 == 0) goto L_0x00ac
            org.telegram.tgnet.TLRPC$FileLocation r1 = r1.photo_big
            if (r1 == 0) goto L_0x00ac
            org.telegram.ui.PhotoViewer r1 = org.telegram.ui.PhotoViewer.getInstance()
            android.app.Activity r2 = r4.getParentActivity()
            r1.setParentActivity(r2)
            org.telegram.tgnet.TLRPC$ChatPhoto r1 = r0.photo
            int r2 = r1.dc_id
            if (r2 == 0) goto L_0x0076
            org.telegram.tgnet.TLRPC$FileLocation r1 = r1.photo_big
            r1.dc_id = r2
        L_0x0076:
            org.telegram.tgnet.TLRPC$ChatFull r1 = r4.chatInfo
            if (r1 == 0) goto L_0x009e
            org.telegram.tgnet.TLRPC$Photo r1 = r1.chat_photo
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_photo
            if (r2 == 0) goto L_0x009e
            java.util.ArrayList<org.telegram.tgnet.TLRPC$VideoSize> r1 = r1.video_sizes
            boolean r1 = r1.isEmpty()
            if (r1 != 0) goto L_0x009e
            org.telegram.tgnet.TLRPC$ChatFull r1 = r4.chatInfo
            org.telegram.tgnet.TLRPC$Photo r1 = r1.chat_photo
            java.util.ArrayList<org.telegram.tgnet.TLRPC$VideoSize> r1 = r1.video_sizes
            r2 = 0
            java.lang.Object r1 = r1.get(r2)
            org.telegram.tgnet.TLRPC$VideoSize r1 = (org.telegram.tgnet.TLRPC$VideoSize) r1
            org.telegram.tgnet.TLRPC$ChatFull r2 = r4.chatInfo
            org.telegram.tgnet.TLRPC$Photo r2 = r2.chat_photo
            org.telegram.messenger.ImageLocation r1 = org.telegram.messenger.ImageLocation.getForPhoto((org.telegram.tgnet.TLRPC$VideoSize) r1, (org.telegram.tgnet.TLRPC$Photo) r2)
            goto L_0x009f
        L_0x009e:
            r1 = 0
        L_0x009f:
            org.telegram.ui.PhotoViewer r2 = org.telegram.ui.PhotoViewer.getInstance()
            org.telegram.tgnet.TLRPC$ChatPhoto r0 = r0.photo
            org.telegram.tgnet.TLRPC$FileLocation r0 = r0.photo_big
            org.telegram.ui.PhotoViewer$PhotoViewerProvider r3 = r4.provider
            r2.openPhotoWithVideo(r0, r1, r3)
        L_0x00ac:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.openAvatar():void");
    }

    /* access modifiers changed from: private */
    public void onWriteButtonClick() {
        if (this.user_id != 0) {
            boolean z = true;
            if (this.imageUpdater != null) {
                TLRPC$User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(this.currentAccount).getClientUserId()));
                if (user == null) {
                    user = UserConfig.getInstance(this.currentAccount).getCurrentUser();
                }
                if (user != null) {
                    ImageUpdater imageUpdater2 = this.imageUpdater;
                    TLRPC$UserProfilePhoto tLRPC$UserProfilePhoto = user.photo;
                    if (tLRPC$UserProfilePhoto == null || tLRPC$UserProfilePhoto.photo_big == null || (tLRPC$UserProfilePhoto instanceof TLRPC$TL_userProfilePhotoEmpty)) {
                        z = false;
                    }
                    imageUpdater2.openMenu(z, new Runnable() {
                        public final void run() {
                            ProfileActivity.this.lambda$onWriteButtonClick$16$ProfileActivity();
                        }
                    });
                    return;
                }
                return;
            }
            if (this.playProfileAnimation != 0) {
                ArrayList<BaseFragment> arrayList = this.parentLayout.fragmentsStack;
                if (arrayList.get(arrayList.size() - 2) instanceof ChatActivity) {
                    finishFragment();
                    return;
                }
            }
            TLRPC$User user2 = getMessagesController().getUser(Integer.valueOf(this.user_id));
            if (user2 != null && !(user2 instanceof TLRPC$TL_userEmpty)) {
                Bundle bundle = new Bundle();
                bundle.putInt("user_id", this.user_id);
                if (getMessagesController().checkCanOpenChat(bundle, this)) {
                    if (!AndroidUtilities.isTablet()) {
                        NotificationCenter notificationCenter = getNotificationCenter();
                        int i = NotificationCenter.closeChats;
                        notificationCenter.removeObserver(this, i);
                        getNotificationCenter().postNotificationName(i, new Object[0]);
                    }
                    int i2 = getArguments().getInt("nearby_distance", -1);
                    if (i2 >= 0) {
                        bundle.putInt("nearby_distance", i2);
                    }
                    ChatActivity chatActivity = new ChatActivity(bundle);
                    chatActivity.setPreloadedSticker(this.preloadedSticker);
                    presentFragment(chatActivity, true);
                    if (AndroidUtilities.isTablet()) {
                        finishFragment();
                        return;
                    }
                    return;
                }
                return;
            }
            return;
        }
        openDiscussion();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onWriteButtonClick$16 */
    public /* synthetic */ void lambda$onWriteButtonClick$16$ProfileActivity() {
        MessagesController.getInstance(this.currentAccount).deleteUserPhoto((TLRPC$InputPhoto) null);
    }

    /* access modifiers changed from: private */
    public void openDiscussion() {
        TLRPC$ChatFull tLRPC$ChatFull = this.chatInfo;
        if (tLRPC$ChatFull != null && tLRPC$ChatFull.linked_chat_id != 0) {
            Bundle bundle = new Bundle();
            bundle.putInt("chat_id", this.chatInfo.linked_chat_id);
            if (getMessagesController().checkCanOpenChat(bundle, this)) {
                presentFragment(new ChatActivity(bundle));
            }
        }
    }

    public boolean onMemberClick(TLRPC$ChatParticipant tLRPC$ChatParticipant, boolean z) {
        return onMemberClick(tLRPC$ChatParticipant, z, false);
    }

    public boolean onMemberClick(TLRPC$ChatParticipant tLRPC$ChatParticipant, boolean z, boolean z2) {
        boolean z3;
        boolean z4;
        TLRPC$ChannelParticipant tLRPC$ChannelParticipant;
        boolean z5;
        boolean z6;
        ArrayList arrayList;
        ArrayList arrayList2;
        boolean z7;
        String str;
        int i;
        TLRPC$ChatParticipant tLRPC$ChatParticipant2 = tLRPC$ChatParticipant;
        if (getParentActivity() == null) {
            return false;
        }
        if (z) {
            TLRPC$User user = getMessagesController().getUser(Integer.valueOf(tLRPC$ChatParticipant2.user_id));
            if (user == null || tLRPC$ChatParticipant2.user_id == getUserConfig().getClientUserId()) {
                return false;
            }
            this.selectedUser = tLRPC$ChatParticipant2.user_id;
            ArrayList arrayList3 = null;
            if (ChatObject.isChannel(this.currentChat)) {
                TLRPC$ChannelParticipant tLRPC$ChannelParticipant2 = ((TLRPC$TL_chatChannelParticipant) tLRPC$ChatParticipant2).channelParticipant;
                getMessagesController().getUser(Integer.valueOf(tLRPC$ChatParticipant2.user_id));
                z5 = ChatObject.canAddAdmins(this.currentChat);
                if (z5 && ((tLRPC$ChannelParticipant2 instanceof TLRPC$TL_channelParticipantCreator) || ((tLRPC$ChannelParticipant2 instanceof TLRPC$TL_channelParticipantAdmin) && !tLRPC$ChannelParticipant2.can_edit))) {
                    z5 = false;
                }
                z4 = tLRPC$ChannelParticipant2 instanceof TLRPC$TL_channelParticipantAdmin;
                z3 = ChatObject.canBlockUsers(this.currentChat) && ((!(tLRPC$ChannelParticipant2 instanceof TLRPC$TL_channelParticipantAdmin) && !(tLRPC$ChannelParticipant2 instanceof TLRPC$TL_channelParticipantCreator)) || tLRPC$ChannelParticipant2.can_edit);
                tLRPC$ChannelParticipant = tLRPC$ChannelParticipant2;
                z6 = z3;
            } else {
                TLRPC$Chat tLRPC$Chat = this.currentChat;
                boolean z8 = tLRPC$Chat.creator || ((tLRPC$ChatParticipant2 instanceof TLRPC$TL_chatParticipant) && (ChatObject.canBlockUsers(tLRPC$Chat) || tLRPC$ChatParticipant2.inviter_id == getUserConfig().getClientUserId()));
                z5 = this.currentChat.creator;
                z4 = tLRPC$ChatParticipant2 instanceof TLRPC$TL_chatParticipantAdmin;
                z3 = z5;
                z6 = z8;
                tLRPC$ChannelParticipant = null;
            }
            if (z2) {
                arrayList = null;
            } else {
                arrayList = new ArrayList();
            }
            if (z2) {
                arrayList2 = null;
            } else {
                arrayList2 = new ArrayList();
            }
            if (!z2) {
                arrayList3 = new ArrayList();
            }
            if (z5) {
                if (z2) {
                    return true;
                }
                if (z4) {
                    i = NUM;
                    str = "EditAdminRights";
                } else {
                    i = NUM;
                    str = "SetAsAdmin";
                }
                arrayList.add(LocaleController.getString(str, i));
                arrayList2.add(NUM);
                arrayList3.add(0);
            }
            if (z3) {
                if (z2) {
                    return true;
                }
                arrayList.add(LocaleController.getString("ChangePermissions", NUM));
                arrayList2.add(NUM);
                arrayList3.add(1);
            }
            if (!z6) {
                z7 = false;
            } else if (z2) {
                return true;
            } else {
                arrayList.add(LocaleController.getString("KickFromGroup", NUM));
                arrayList2.add(NUM);
                arrayList3.add(2);
                z7 = true;
            }
            if (z2 || arrayList.isEmpty()) {
                return false;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder((Context) getParentActivity());
            builder.setItems((CharSequence[]) arrayList.toArray(new CharSequence[0]), AndroidUtilities.toIntArray(arrayList2), new DialogInterface.OnClickListener(arrayList3, tLRPC$ChannelParticipant, tLRPC$ChatParticipant, user, z4) {
                public final /* synthetic */ ArrayList f$1;
                public final /* synthetic */ TLRPC$ChannelParticipant f$2;
                public final /* synthetic */ TLRPC$ChatParticipant f$3;
                public final /* synthetic */ TLRPC$User f$4;
                public final /* synthetic */ boolean f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    ProfileActivity.this.lambda$onMemberClick$18$ProfileActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, dialogInterface, i);
                }
            });
            AlertDialog create = builder.create();
            showDialog(create);
            if (z7) {
                create.setItemColor(arrayList.size() - 1, Theme.getColor("dialogTextRed2"), Theme.getColor("dialogRedIcon"));
            }
        } else if (tLRPC$ChatParticipant2.user_id == getUserConfig().getClientUserId()) {
            return false;
        } else {
            Bundle bundle = new Bundle();
            bundle.putInt("user_id", tLRPC$ChatParticipant2.user_id);
            presentFragment(new ProfileActivity(bundle));
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onMemberClick$18 */
    public /* synthetic */ void lambda$onMemberClick$18$ProfileActivity(ArrayList arrayList, TLRPC$ChannelParticipant tLRPC$ChannelParticipant, TLRPC$ChatParticipant tLRPC$ChatParticipant, TLRPC$User tLRPC$User, boolean z, DialogInterface dialogInterface, int i) {
        if (((Integer) arrayList.get(i)).intValue() == 2) {
            kickUser(this.selectedUser);
            return;
        }
        int intValue = ((Integer) arrayList.get(i)).intValue();
        if (intValue == 1 && ((tLRPC$ChannelParticipant instanceof TLRPC$TL_channelParticipantAdmin) || (tLRPC$ChatParticipant instanceof TLRPC$TL_chatParticipantAdmin))) {
            AlertDialog.Builder builder = new AlertDialog.Builder((Context) getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", NUM));
            builder.setMessage(LocaleController.formatString("AdminWillBeRemoved", NUM, ContactsController.formatName(tLRPC$User.first_name, tLRPC$User.last_name)));
            builder.setPositiveButton(LocaleController.getString("OK", NUM), new DialogInterface.OnClickListener(tLRPC$ChannelParticipant, intValue, tLRPC$User, tLRPC$ChatParticipant, z) {
                public final /* synthetic */ TLRPC$ChannelParticipant f$1;
                public final /* synthetic */ int f$2;
                public final /* synthetic */ TLRPC$User f$3;
                public final /* synthetic */ TLRPC$ChatParticipant f$4;
                public final /* synthetic */ boolean f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    ProfileActivity.this.lambda$null$17$ProfileActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, dialogInterface, i);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
            showDialog(builder.create());
        } else if (tLRPC$ChannelParticipant != null) {
            openRightsEdit(intValue, tLRPC$User, tLRPC$ChatParticipant, tLRPC$ChannelParticipant.admin_rights, tLRPC$ChannelParticipant.banned_rights, tLRPC$ChannelParticipant.rank, z);
        } else {
            openRightsEdit(intValue, tLRPC$User, tLRPC$ChatParticipant, (TLRPC$TL_chatAdminRights) null, (TLRPC$TL_chatBannedRights) null, "", z);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$17 */
    public /* synthetic */ void lambda$null$17$ProfileActivity(TLRPC$ChannelParticipant tLRPC$ChannelParticipant, int i, TLRPC$User tLRPC$User, TLRPC$ChatParticipant tLRPC$ChatParticipant, boolean z, DialogInterface dialogInterface, int i2) {
        TLRPC$ChannelParticipant tLRPC$ChannelParticipant2 = tLRPC$ChannelParticipant;
        if (tLRPC$ChannelParticipant2 != null) {
            openRightsEdit(i, tLRPC$User, tLRPC$ChatParticipant, tLRPC$ChannelParticipant2.admin_rights, tLRPC$ChannelParticipant2.banned_rights, tLRPC$ChannelParticipant2.rank, z);
            return;
        }
        openRightsEdit(i, tLRPC$User, tLRPC$ChatParticipant, (TLRPC$TL_chatAdminRights) null, (TLRPC$TL_chatBannedRights) null, "", z);
    }

    private void openRightsEdit(int i, TLRPC$User tLRPC$User, TLRPC$ChatParticipant tLRPC$ChatParticipant, TLRPC$TL_chatAdminRights tLRPC$TL_chatAdminRights, TLRPC$TL_chatBannedRights tLRPC$TL_chatBannedRights, String str, boolean z) {
        boolean[] zArr = new boolean[1];
        final TLRPC$User tLRPC$User2 = tLRPC$User;
        final boolean[] zArr2 = zArr;
        AnonymousClass20 r0 = new ChatRightsEditActivity(this, tLRPC$User2.id, this.chat_id, tLRPC$TL_chatAdminRights, this.currentChat.default_banned_rights, tLRPC$TL_chatBannedRights, str, i, true, false) {
            final /* synthetic */ ProfileActivity this$0;

            {
                this.this$0 = r12;
            }

            /* access modifiers changed from: protected */
            public void onTransitionAnimationEnd(boolean z, boolean z2) {
                if (!z && z2 && zArr2[0] && BulletinFactory.canShowBulletin(this.this$0)) {
                    BulletinFactory.createPromoteToAdminBulletin(this.this$0, tLRPC$User2.first_name).show();
                }
            }
        };
        final int i2 = i;
        final TLRPC$ChatParticipant tLRPC$ChatParticipant2 = tLRPC$ChatParticipant;
        final boolean z2 = z;
        final boolean[] zArr3 = zArr;
        r0.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
            /* JADX WARNING: Removed duplicated region for block: B:48:0x0145  */
            /* JADX WARNING: Removed duplicated region for block: B:62:? A[RETURN, SYNTHETIC] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void didSetRights(int r6, org.telegram.tgnet.TLRPC$TL_chatAdminRights r7, org.telegram.tgnet.TLRPC$TL_chatBannedRights r8, java.lang.String r9) {
                /*
                    r5 = this;
                    int r0 = r2
                    r1 = 0
                    r2 = 1
                    if (r0 != 0) goto L_0x008e
                    org.telegram.tgnet.TLRPC$ChatParticipant r0 = r3
                    boolean r3 = r0 instanceof org.telegram.tgnet.TLRPC$TL_chatChannelParticipant
                    if (r3 == 0) goto L_0x0046
                    org.telegram.tgnet.TLRPC$TL_chatChannelParticipant r0 = (org.telegram.tgnet.TLRPC$TL_chatChannelParticipant) r0
                    if (r6 != r2) goto L_0x001e
                    org.telegram.tgnet.TLRPC$TL_channelParticipantAdmin r3 = new org.telegram.tgnet.TLRPC$TL_channelParticipantAdmin
                    r3.<init>()
                    r0.channelParticipant = r3
                    int r4 = r3.flags
                    r4 = r4 | 4
                    r3.flags = r4
                    goto L_0x0025
                L_0x001e:
                    org.telegram.tgnet.TLRPC$TL_channelParticipant r3 = new org.telegram.tgnet.TLRPC$TL_channelParticipant
                    r3.<init>()
                    r0.channelParticipant = r3
                L_0x0025:
                    org.telegram.tgnet.TLRPC$ChannelParticipant r3 = r0.channelParticipant
                    org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                    org.telegram.messenger.UserConfig r4 = r4.getUserConfig()
                    int r4 = r4.getClientUserId()
                    r3.inviter_id = r4
                    org.telegram.tgnet.TLRPC$ChannelParticipant r0 = r0.channelParticipant
                    org.telegram.tgnet.TLRPC$ChatParticipant r3 = r3
                    int r4 = r3.user_id
                    r0.user_id = r4
                    int r3 = r3.date
                    r0.date = r3
                    r0.banned_rights = r8
                    r0.admin_rights = r7
                    r0.rank = r9
                    goto L_0x0082
                L_0x0046:
                    if (r0 == 0) goto L_0x0082
                    if (r6 != r2) goto L_0x0050
                    org.telegram.tgnet.TLRPC$TL_chatParticipantAdmin r7 = new org.telegram.tgnet.TLRPC$TL_chatParticipantAdmin
                    r7.<init>()
                    goto L_0x0055
                L_0x0050:
                    org.telegram.tgnet.TLRPC$TL_chatParticipant r7 = new org.telegram.tgnet.TLRPC$TL_chatParticipant
                    r7.<init>()
                L_0x0055:
                    org.telegram.tgnet.TLRPC$ChatParticipant r8 = r3
                    int r9 = r8.user_id
                    r7.user_id = r9
                    int r9 = r8.date
                    r7.date = r9
                    int r8 = r8.inviter_id
                    r7.inviter_id = r8
                    org.telegram.ui.ProfileActivity r8 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r8 = r8.chatInfo
                    org.telegram.tgnet.TLRPC$ChatParticipants r8 = r8.participants
                    java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r8 = r8.participants
                    org.telegram.tgnet.TLRPC$ChatParticipant r9 = r3
                    int r8 = r8.indexOf(r9)
                    if (r8 < 0) goto L_0x0082
                    org.telegram.ui.ProfileActivity r9 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r9 = r9.chatInfo
                    org.telegram.tgnet.TLRPC$ChatParticipants r9 = r9.participants
                    java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r9 = r9.participants
                    r9.set(r8, r7)
                L_0x0082:
                    if (r6 != r2) goto L_0x0158
                    boolean r6 = r4
                    if (r6 != 0) goto L_0x0158
                    boolean[] r6 = r5
                    r6[r1] = r2
                    goto L_0x0158
                L_0x008e:
                    if (r0 != r2) goto L_0x0158
                    if (r6 != 0) goto L_0x0158
                    org.telegram.ui.ProfileActivity r6 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$Chat r6 = r6.currentChat
                    boolean r6 = r6.megagroup
                    if (r6 == 0) goto L_0x0158
                    org.telegram.ui.ProfileActivity r6 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r6 = r6.chatInfo
                    if (r6 == 0) goto L_0x0158
                    org.telegram.ui.ProfileActivity r6 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r6 = r6.chatInfo
                    org.telegram.tgnet.TLRPC$ChatParticipants r6 = r6.participants
                    if (r6 == 0) goto L_0x0158
                    r6 = 0
                L_0x00af:
                    org.telegram.ui.ProfileActivity r7 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r7 = r7.chatInfo
                    org.telegram.tgnet.TLRPC$ChatParticipants r7 = r7.participants
                    java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r7 = r7.participants
                    int r7 = r7.size()
                    if (r6 >= r7) goto L_0x00f6
                    org.telegram.ui.ProfileActivity r7 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r7 = r7.chatInfo
                    org.telegram.tgnet.TLRPC$ChatParticipants r7 = r7.participants
                    java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r7 = r7.participants
                    java.lang.Object r7 = r7.get(r6)
                    org.telegram.tgnet.TLRPC$TL_chatChannelParticipant r7 = (org.telegram.tgnet.TLRPC$TL_chatChannelParticipant) r7
                    org.telegram.tgnet.TLRPC$ChannelParticipant r7 = r7.channelParticipant
                    int r7 = r7.user_id
                    org.telegram.tgnet.TLRPC$ChatParticipant r8 = r3
                    int r8 = r8.user_id
                    if (r7 != r8) goto L_0x00f3
                    org.telegram.ui.ProfileActivity r7 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r7 = r7.chatInfo
                    int r8 = r7.participants_count
                    int r8 = r8 - r2
                    r7.participants_count = r8
                    org.telegram.ui.ProfileActivity r7 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r7 = r7.chatInfo
                    org.telegram.tgnet.TLRPC$ChatParticipants r7 = r7.participants
                    java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r7 = r7.participants
                    r7.remove(r6)
                    r6 = 1
                    goto L_0x00f7
                L_0x00f3:
                    int r6 = r6 + 1
                    goto L_0x00af
                L_0x00f6:
                    r6 = 0
                L_0x00f7:
                    org.telegram.ui.ProfileActivity r7 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r7 = r7.chatInfo
                    if (r7 == 0) goto L_0x0142
                    org.telegram.ui.ProfileActivity r7 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r7 = r7.chatInfo
                    org.telegram.tgnet.TLRPC$ChatParticipants r7 = r7.participants
                    if (r7 == 0) goto L_0x0142
                L_0x0109:
                    org.telegram.ui.ProfileActivity r7 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r7 = r7.chatInfo
                    org.telegram.tgnet.TLRPC$ChatParticipants r7 = r7.participants
                    java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r7 = r7.participants
                    int r7 = r7.size()
                    if (r1 >= r7) goto L_0x0142
                    org.telegram.ui.ProfileActivity r7 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r7 = r7.chatInfo
                    org.telegram.tgnet.TLRPC$ChatParticipants r7 = r7.participants
                    java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r7 = r7.participants
                    java.lang.Object r7 = r7.get(r1)
                    org.telegram.tgnet.TLRPC$ChatParticipant r7 = (org.telegram.tgnet.TLRPC$ChatParticipant) r7
                    int r7 = r7.user_id
                    org.telegram.tgnet.TLRPC$ChatParticipant r8 = r3
                    int r8 = r8.user_id
                    if (r7 != r8) goto L_0x013f
                    org.telegram.ui.ProfileActivity r6 = org.telegram.ui.ProfileActivity.this
                    org.telegram.tgnet.TLRPC$ChatFull r6 = r6.chatInfo
                    org.telegram.tgnet.TLRPC$ChatParticipants r6 = r6.participants
                    java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r6 = r6.participants
                    r6.remove(r1)
                    goto L_0x0143
                L_0x013f:
                    int r1 = r1 + 1
                    goto L_0x0109
                L_0x0142:
                    r2 = r6
                L_0x0143:
                    if (r2 == 0) goto L_0x0158
                    org.telegram.ui.ProfileActivity r6 = org.telegram.ui.ProfileActivity.this
                    r6.updateOnlineCount()
                    org.telegram.ui.ProfileActivity r6 = org.telegram.ui.ProfileActivity.this
                    r6.updateRowsIds()
                    org.telegram.ui.ProfileActivity r6 = org.telegram.ui.ProfileActivity.this
                    org.telegram.ui.ProfileActivity$ListAdapter r6 = r6.listAdapter
                    r6.notifyDataSetChanged()
                L_0x0158:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.AnonymousClass21.didSetRights(int, org.telegram.tgnet.TLRPC$TL_chatAdminRights, org.telegram.tgnet.TLRPC$TL_chatBannedRights, java.lang.String):void");
            }

            public void didChangeOwner(TLRPC$User tLRPC$User) {
                ProfileActivity.this.undoView.showWithAction((long) (-ProfileActivity.this.chat_id), ProfileActivity.this.currentChat.megagroup ? 10 : 9, (Object) tLRPC$User);
            }
        });
        presentFragment(r0);
    }

    /* access modifiers changed from: private */
    public boolean processOnClickOrPress(int i) {
        String str;
        TLRPC$Chat chat;
        String str2;
        TLRPC$UserFull tLRPC$UserFull;
        if (i == this.usernameRow || i == this.setUsernameRow) {
            if (this.user_id != 0) {
                TLRPC$User user = getMessagesController().getUser(Integer.valueOf(this.user_id));
                if (user == null || (str = user.username) == null) {
                    return false;
                }
            } else if (this.chat_id == 0 || (chat = getMessagesController().getChat(Integer.valueOf(this.chat_id))) == null || (str = chat.username) == null) {
                return false;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder((Context) getParentActivity());
            builder.setItems(new CharSequence[]{LocaleController.getString("Copy", NUM)}, new DialogInterface.OnClickListener(str) {
                public final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    ProfileActivity.this.lambda$processOnClickOrPress$19$ProfileActivity(this.f$1, dialogInterface, i);
                }
            });
            showDialog(builder.create());
            return true;
        } else if (i == this.phoneRow || i == this.numberRow) {
            TLRPC$User user2 = getMessagesController().getUser(Integer.valueOf(this.user_id));
            if (user2 == null || (str2 = user2.phone) == null || str2.length() == 0 || getParentActivity() == null) {
                return false;
            }
            AlertDialog.Builder builder2 = new AlertDialog.Builder((Context) getParentActivity());
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            if (i == this.phoneRow) {
                TLRPC$UserFull tLRPC$UserFull2 = this.userInfo;
                if (tLRPC$UserFull2 != null && tLRPC$UserFull2.phone_calls_available) {
                    arrayList.add(LocaleController.getString("CallViaTelegram", NUM));
                    arrayList2.add(2);
                    if (Build.VERSION.SDK_INT >= 18 && this.userInfo.video_calls_available) {
                        arrayList.add(LocaleController.getString("VideoCallViaTelegram", NUM));
                        arrayList2.add(3);
                    }
                }
                arrayList.add(LocaleController.getString("Call", NUM));
                arrayList2.add(0);
            }
            arrayList.add(LocaleController.getString("Copy", NUM));
            arrayList2.add(1);
            builder2.setItems((CharSequence[]) arrayList.toArray(new CharSequence[0]), new DialogInterface.OnClickListener(arrayList2, user2) {
                public final /* synthetic */ ArrayList f$1;
                public final /* synthetic */ TLRPC$User f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    ProfileActivity.this.lambda$processOnClickOrPress$20$ProfileActivity(this.f$1, this.f$2, dialogInterface, i);
                }
            });
            showDialog(builder2.create());
            return true;
        } else if (i != this.channelInfoRow && i != this.userInfoRow && i != this.locationRow && i != this.bioRow) {
            return false;
        } else {
            if (i == this.bioRow && ((tLRPC$UserFull = this.userInfo) == null || TextUtils.isEmpty(tLRPC$UserFull.about))) {
                return false;
            }
            AlertDialog.Builder builder3 = new AlertDialog.Builder((Context) getParentActivity());
            builder3.setItems(new CharSequence[]{LocaleController.getString("Copy", NUM)}, new DialogInterface.OnClickListener(i) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    ProfileActivity.this.lambda$processOnClickOrPress$21$ProfileActivity(this.f$1, dialogInterface, i);
                }
            });
            showDialog(builder3.create());
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$processOnClickOrPress$19 */
    public /* synthetic */ void lambda$processOnClickOrPress$19$ProfileActivity(String str, DialogInterface dialogInterface, int i) {
        if (i == 0) {
            try {
                ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", "@" + str));
                Toast.makeText(getParentActivity(), LocaleController.getString("TextCopied", NUM), 0).show();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$processOnClickOrPress$20 */
    public /* synthetic */ void lambda$processOnClickOrPress$20$ProfileActivity(ArrayList arrayList, TLRPC$User tLRPC$User, DialogInterface dialogInterface, int i) {
        int intValue = ((Integer) arrayList.get(i)).intValue();
        if (intValue == 0) {
            try {
                Intent intent = new Intent("android.intent.action.DIAL", Uri.parse("tel:+" + tLRPC$User.phone));
                intent.addFlags(NUM);
                getParentActivity().startActivityForResult(intent, 500);
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        } else {
            boolean z = false;
            if (intValue == 1) {
                try {
                    ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", "+" + tLRPC$User.phone));
                    Toast.makeText(getParentActivity(), LocaleController.getString("PhoneCopied", NUM), 0).show();
                } catch (Exception e2) {
                    FileLog.e((Throwable) e2);
                }
            } else if (intValue == 2 || intValue == 3) {
                boolean z2 = intValue == 3;
                TLRPC$UserFull tLRPC$UserFull = this.userInfo;
                if (tLRPC$UserFull != null && tLRPC$UserFull.video_calls_available) {
                    z = true;
                }
                VoIPHelper.startCall(tLRPC$User, z2, z, getParentActivity(), this.userInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$processOnClickOrPress$21 */
    public /* synthetic */ void lambda$processOnClickOrPress$21$ProfileActivity(int i, DialogInterface dialogInterface, int i2) {
        try {
            String str = null;
            if (i == this.locationRow) {
                TLRPC$ChatFull tLRPC$ChatFull = this.chatInfo;
                if (tLRPC$ChatFull != null) {
                    TLRPC$ChannelLocation tLRPC$ChannelLocation = tLRPC$ChatFull.location;
                    if (tLRPC$ChannelLocation instanceof TLRPC$TL_channelLocation) {
                        str = ((TLRPC$TL_channelLocation) tLRPC$ChannelLocation).address;
                    }
                }
            } else if (i == this.channelInfoRow) {
                TLRPC$ChatFull tLRPC$ChatFull2 = this.chatInfo;
                if (tLRPC$ChatFull2 != null) {
                    str = tLRPC$ChatFull2.about;
                }
            } else {
                TLRPC$UserFull tLRPC$UserFull = this.userInfo;
                if (tLRPC$UserFull != null) {
                    str = tLRPC$UserFull.about;
                }
            }
            if (!TextUtils.isEmpty(str)) {
                AndroidUtilities.addToClipboard(str);
                Toast.makeText(getParentActivity(), LocaleController.getString("TextCopied", NUM), 0).show();
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    /* access modifiers changed from: private */
    public void leaveChatPressed() {
        AlertsCreator.createClearOrDeleteDialogAlert(this, false, this.currentChat, (TLRPC$User) null, false, new MessagesStorage.BooleanCallback() {
            public final void run(boolean z) {
                ProfileActivity.this.lambda$leaveChatPressed$22$ProfileActivity(z);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$leaveChatPressed$22 */
    public /* synthetic */ void lambda$leaveChatPressed$22$ProfileActivity(boolean z) {
        this.playProfileAnimation = 0;
        NotificationCenter notificationCenter = getNotificationCenter();
        int i = NotificationCenter.closeChats;
        notificationCenter.removeObserver(this, i);
        getNotificationCenter().postNotificationName(i, new Object[0]);
        finishFragment();
        getNotificationCenter().postNotificationName(NotificationCenter.needDeleteDialog, Long.valueOf((long) (-this.currentChat.id)), null, this.currentChat, Boolean.valueOf(z));
    }

    /* access modifiers changed from: private */
    public void getChannelParticipants(boolean z) {
        SparseArray<TLRPC$ChatParticipant> sparseArray;
        if (!this.loadingUsers && (sparseArray = this.participantsMap) != null && this.chatInfo != null) {
            this.loadingUsers = true;
            int i = 0;
            int i2 = (sparseArray.size() == 0 || !z) ? 0 : 300;
            TLRPC$TL_channels_getParticipants tLRPC$TL_channels_getParticipants = new TLRPC$TL_channels_getParticipants();
            tLRPC$TL_channels_getParticipants.channel = getMessagesController().getInputChannel(this.chat_id);
            tLRPC$TL_channels_getParticipants.filter = new TLRPC$TL_channelParticipantsRecent();
            if (!z) {
                i = this.participantsMap.size();
            }
            tLRPC$TL_channels_getParticipants.offset = i;
            tLRPC$TL_channels_getParticipants.limit = 200;
            getConnectionsManager().bindRequestToGuid(getConnectionsManager().sendRequest(tLRPC$TL_channels_getParticipants, new RequestDelegate(tLRPC$TL_channels_getParticipants, i2) {
                public final /* synthetic */ TLRPC$TL_channels_getParticipants f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                    ProfileActivity.this.lambda$getChannelParticipants$24$ProfileActivity(this.f$1, this.f$2, tLObject, tLRPC$TL_error);
                }
            }), this.classGuid);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getChannelParticipants$24 */
    public /* synthetic */ void lambda$getChannelParticipants$24$ProfileActivity(TLRPC$TL_channels_getParticipants tLRPC$TL_channels_getParticipants, int i, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_error, tLObject, tLRPC$TL_channels_getParticipants) {
            public final /* synthetic */ TLRPC$TL_error f$1;
            public final /* synthetic */ TLObject f$2;
            public final /* synthetic */ TLRPC$TL_channels_getParticipants f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                ProfileActivity.this.lambda$null$23$ProfileActivity(this.f$1, this.f$2, this.f$3);
            }
        }, (long) i);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$23 */
    public /* synthetic */ void lambda$null$23$ProfileActivity(TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, TLRPC$TL_channels_getParticipants tLRPC$TL_channels_getParticipants) {
        if (tLRPC$TL_error == null) {
            TLRPC$TL_channels_channelParticipants tLRPC$TL_channels_channelParticipants = (TLRPC$TL_channels_channelParticipants) tLObject;
            getMessagesController().putUsers(tLRPC$TL_channels_channelParticipants.users, false);
            if (tLRPC$TL_channels_channelParticipants.users.size() < 200) {
                this.usersEndReached = true;
            }
            if (tLRPC$TL_channels_getParticipants.offset == 0) {
                this.participantsMap.clear();
                this.chatInfo.participants = new TLRPC$TL_chatParticipants();
                getMessagesStorage().putUsersAndChats(tLRPC$TL_channels_channelParticipants.users, (ArrayList<TLRPC$Chat>) null, true, true);
                getMessagesStorage().updateChannelUsers(this.chat_id, tLRPC$TL_channels_channelParticipants.participants);
            }
            for (int i = 0; i < tLRPC$TL_channels_channelParticipants.participants.size(); i++) {
                TLRPC$TL_chatChannelParticipant tLRPC$TL_chatChannelParticipant = new TLRPC$TL_chatChannelParticipant();
                TLRPC$ChannelParticipant tLRPC$ChannelParticipant = tLRPC$TL_channels_channelParticipants.participants.get(i);
                tLRPC$TL_chatChannelParticipant.channelParticipant = tLRPC$ChannelParticipant;
                tLRPC$TL_chatChannelParticipant.inviter_id = tLRPC$ChannelParticipant.inviter_id;
                int i2 = tLRPC$ChannelParticipant.user_id;
                tLRPC$TL_chatChannelParticipant.user_id = i2;
                tLRPC$TL_chatChannelParticipant.date = tLRPC$ChannelParticipant.date;
                if (this.participantsMap.indexOfKey(i2) < 0) {
                    TLRPC$ChatFull tLRPC$ChatFull = this.chatInfo;
                    if (tLRPC$ChatFull.participants == null) {
                        tLRPC$ChatFull.participants = new TLRPC$TL_chatParticipants();
                    }
                    this.chatInfo.participants.participants.add(tLRPC$TL_chatChannelParticipant);
                    this.participantsMap.put(tLRPC$TL_chatChannelParticipant.user_id, tLRPC$TL_chatChannelParticipant);
                }
            }
        }
        updateOnlineCount();
        this.loadingUsers = false;
        updateRowsIds();
        ListAdapter listAdapter2 = this.listAdapter;
        if (listAdapter2 != null) {
            listAdapter2.notifyDataSetChanged();
        }
    }

    private void setMediaHeaderVisible(boolean z) {
        if (this.mediaHeaderVisible != z) {
            this.mediaHeaderVisible = z;
            AnimatorSet animatorSet = this.headerAnimatorSet;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            AnimatorSet animatorSet2 = this.headerShadowAnimatorSet;
            if (animatorSet2 != null) {
                animatorSet2.cancel();
            }
            final ActionBarMenuItem searchItem2 = this.sharedMediaLayout.getSearchItem();
            if (!this.mediaHeaderVisible) {
                if (this.callItemVisible) {
                    this.callItem.setVisibility(0);
                }
                if (this.videoCallItemVisible) {
                    this.videoCallItem.setVisibility(0);
                }
                if (this.editItemVisible) {
                    this.editItem.setVisibility(0);
                }
                this.otherItem.setVisibility(0);
            } else if (this.sharedMediaLayout.isSearchItemVisible()) {
                searchItem2.setVisibility(0);
            }
            ArrayList arrayList = new ArrayList();
            ActionBarMenuItem actionBarMenuItem = this.callItem;
            Property property = View.ALPHA;
            float[] fArr = new float[1];
            float f = 1.0f;
            fArr[0] = z ? 0.0f : 1.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem, property, fArr));
            ActionBarMenuItem actionBarMenuItem2 = this.videoCallItem;
            Property property2 = View.ALPHA;
            float[] fArr2 = new float[1];
            fArr2[0] = z ? 0.0f : 1.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem2, property2, fArr2));
            ActionBarMenuItem actionBarMenuItem3 = this.otherItem;
            Property property3 = View.ALPHA;
            float[] fArr3 = new float[1];
            fArr3[0] = z ? 0.0f : 1.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem3, property3, fArr3));
            ActionBarMenuItem actionBarMenuItem4 = this.editItem;
            Property property4 = View.ALPHA;
            float[] fArr4 = new float[1];
            fArr4[0] = z ? 0.0f : 1.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem4, property4, fArr4));
            ActionBarMenuItem actionBarMenuItem5 = this.callItem;
            Property property5 = View.TRANSLATION_Y;
            float[] fArr5 = new float[1];
            fArr5[0] = z ? (float) (-AndroidUtilities.dp(10.0f)) : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem5, property5, fArr5));
            ActionBarMenuItem actionBarMenuItem6 = this.videoCallItem;
            Property property6 = View.TRANSLATION_Y;
            float[] fArr6 = new float[1];
            fArr6[0] = z ? (float) (-AndroidUtilities.dp(10.0f)) : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem6, property6, fArr6));
            ActionBarMenuItem actionBarMenuItem7 = this.otherItem;
            Property property7 = View.TRANSLATION_Y;
            float[] fArr7 = new float[1];
            fArr7[0] = z ? (float) (-AndroidUtilities.dp(10.0f)) : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem7, property7, fArr7));
            ActionBarMenuItem actionBarMenuItem8 = this.editItem;
            Property property8 = View.TRANSLATION_Y;
            float[] fArr8 = new float[1];
            fArr8[0] = z ? (float) (-AndroidUtilities.dp(10.0f)) : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem8, property8, fArr8));
            Property property9 = View.ALPHA;
            float[] fArr9 = new float[1];
            fArr9[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(searchItem2, property9, fArr9));
            Property property10 = View.TRANSLATION_Y;
            float[] fArr10 = new float[1];
            fArr10[0] = z ? 0.0f : (float) AndroidUtilities.dp(10.0f);
            arrayList.add(ObjectAnimator.ofFloat(searchItem2, property10, fArr10));
            ActionBar actionBar = this.actionBar;
            Property<ActionBar, Float> property11 = this.ACTIONBAR_HEADER_PROGRESS;
            float[] fArr11 = new float[1];
            fArr11[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBar, property11, fArr11));
            SimpleTextView simpleTextView = this.onlineTextView[1];
            Property property12 = View.ALPHA;
            float[] fArr12 = new float[1];
            fArr12[0] = z ? 0.0f : 1.0f;
            arrayList.add(ObjectAnimator.ofFloat(simpleTextView, property12, fArr12));
            AudioPlayerAlert.ClippingTextViewSwitcher clippingTextViewSwitcher = this.mediaCounterTextView;
            Property property13 = View.ALPHA;
            float[] fArr13 = new float[1];
            if (!z) {
                f = 0.0f;
            }
            fArr13[0] = f;
            arrayList.add(ObjectAnimator.ofFloat(clippingTextViewSwitcher, property13, fArr13));
            if (z) {
                arrayList.add(ObjectAnimator.ofFloat(this, this.HEADER_SHADOW, new float[]{0.0f}));
            }
            AnimatorSet animatorSet3 = new AnimatorSet();
            this.headerAnimatorSet = animatorSet3;
            animatorSet3.playTogether(arrayList);
            this.headerAnimatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
            this.headerAnimatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    if (ProfileActivity.this.headerAnimatorSet != null) {
                        if (ProfileActivity.this.mediaHeaderVisible) {
                            if (ProfileActivity.this.callItemVisible) {
                                ProfileActivity.this.callItem.setVisibility(4);
                            }
                            if (ProfileActivity.this.videoCallItemVisible) {
                                ProfileActivity.this.videoCallItem.setVisibility(4);
                            }
                            if (ProfileActivity.this.editItemVisible) {
                                ProfileActivity.this.editItem.setVisibility(4);
                            }
                            ProfileActivity.this.otherItem.setVisibility(4);
                        } else {
                            if (ProfileActivity.this.sharedMediaLayout.isSearchItemVisible()) {
                                searchItem2.setVisibility(0);
                            }
                            AnimatorSet unused = ProfileActivity.this.headerShadowAnimatorSet = new AnimatorSet();
                            AnimatorSet access$12800 = ProfileActivity.this.headerShadowAnimatorSet;
                            ProfileActivity profileActivity = ProfileActivity.this;
                            access$12800.playTogether(new Animator[]{ObjectAnimator.ofFloat(profileActivity, profileActivity.HEADER_SHADOW, new float[]{1.0f})});
                            ProfileActivity.this.headerShadowAnimatorSet.setDuration(100);
                            ProfileActivity.this.headerShadowAnimatorSet.addListener(new AnimatorListenerAdapter() {
                                public void onAnimationEnd(Animator animator) {
                                    AnimatorSet unused = ProfileActivity.this.headerShadowAnimatorSet = null;
                                }
                            });
                            ProfileActivity.this.headerShadowAnimatorSet.start();
                        }
                    }
                    AnimatorSet unused2 = ProfileActivity.this.headerAnimatorSet = null;
                }

                public void onAnimationCancel(Animator animator) {
                    AnimatorSet unused = ProfileActivity.this.headerAnimatorSet = null;
                }
            });
            this.headerAnimatorSet.setDuration(150);
            this.headerAnimatorSet.start();
        }
    }

    /* access modifiers changed from: private */
    public void openAddMember() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("addToGroup", true);
        bundle.putInt("chatId", this.currentChat.id);
        GroupCreateActivity groupCreateActivity = new GroupCreateActivity(bundle);
        groupCreateActivity.setInfo(this.chatInfo);
        TLRPC$ChatFull tLRPC$ChatFull = this.chatInfo;
        if (!(tLRPC$ChatFull == null || tLRPC$ChatFull.participants == null)) {
            SparseArray sparseArray = new SparseArray();
            for (int i = 0; i < this.chatInfo.participants.participants.size(); i++) {
                sparseArray.put(this.chatInfo.participants.participants.get(i).user_id, (Object) null);
            }
            groupCreateActivity.setIgnoreUsers(sparseArray);
        }
        groupCreateActivity.setDelegate((GroupCreateActivity.ContactsAddActivityDelegate) new GroupCreateActivity.ContactsAddActivityDelegate() {
            public final void didSelectUsers(ArrayList arrayList, int i) {
                ProfileActivity.this.lambda$openAddMember$25$ProfileActivity(arrayList, i);
            }

            public /* synthetic */ void needAddBot(TLRPC$User tLRPC$User) {
                GroupCreateActivity.ContactsAddActivityDelegate.CC.$default$needAddBot(this, tLRPC$User);
            }
        });
        presentFragment(groupCreateActivity);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$openAddMember$25 */
    public /* synthetic */ void lambda$openAddMember$25$ProfileActivity(ArrayList arrayList, int i) {
        int size = arrayList.size();
        for (int i2 = 0; i2 < size; i2++) {
            getMessagesController().addUserToChat(this.chat_id, (TLRPC$User) arrayList.get(i2), i, (String) null, this, (Runnable) null);
        }
    }

    /* JADX WARNING: type inference failed for: r2v12, types: [androidx.recyclerview.widget.RecyclerView$ViewHolder] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void checkListViewScroll() {
        /*
            r6 = this;
            org.telegram.ui.Components.RecyclerListView r0 = r6.listView
            int r0 = r0.getVisibility()
            if (r0 == 0) goto L_0x0009
            return
        L_0x0009:
            boolean r0 = r6.sharedMediaLayoutAttached
            if (r0 == 0) goto L_0x001f
            org.telegram.ui.Components.SharedMediaLayout r0 = r6.sharedMediaLayout
            org.telegram.ui.Components.RecyclerListView r1 = r6.listView
            int r1 = r1.getMeasuredHeight()
            org.telegram.ui.Components.SharedMediaLayout r2 = r6.sharedMediaLayout
            int r2 = r2.getTop()
            int r1 = r1 - r2
            r0.setVisibleHeight(r1)
        L_0x001f:
            org.telegram.ui.Components.RecyclerListView r0 = r6.listView
            int r0 = r0.getChildCount()
            if (r0 <= 0) goto L_0x00bf
            boolean r0 = r6.openAnimationInProgress
            if (r0 == 0) goto L_0x002d
            goto L_0x00bf
        L_0x002d:
            r0 = 0
            r1 = 0
        L_0x002f:
            org.telegram.ui.Components.RecyclerListView r2 = r6.listView
            int r2 = r2.getChildCount()
            r3 = 0
            if (r1 >= r2) goto L_0x004e
            org.telegram.ui.Components.RecyclerListView r2 = r6.listView
            android.view.View r4 = r2.getChildAt(r1)
            int r2 = r2.getChildAdapterPosition(r4)
            if (r2 != 0) goto L_0x004b
            org.telegram.ui.Components.RecyclerListView r2 = r6.listView
            android.view.View r1 = r2.getChildAt(r1)
            goto L_0x004f
        L_0x004b:
            int r1 = r1 + 1
            goto L_0x002f
        L_0x004e:
            r1 = r3
        L_0x004f:
            if (r1 != 0) goto L_0x0052
            goto L_0x005b
        L_0x0052:
            org.telegram.ui.Components.RecyclerListView r2 = r6.listView
            androidx.recyclerview.widget.RecyclerView$ViewHolder r2 = r2.findContainingViewHolder(r1)
            r3 = r2
            org.telegram.ui.Components.RecyclerListView$Holder r3 = (org.telegram.ui.Components.RecyclerListView.Holder) r3
        L_0x005b:
            if (r1 != 0) goto L_0x005f
            r1 = 0
            goto L_0x0063
        L_0x005f:
            int r1 = r1.getTop()
        L_0x0063:
            r2 = -1
            if (r3 == 0) goto L_0x006b
            int r3 = r3.getAdapterPosition()
            goto L_0x006c
        L_0x006b:
            r3 = -1
        L_0x006c:
            if (r1 < 0) goto L_0x0071
            if (r3 != 0) goto L_0x0071
            goto L_0x0072
        L_0x0071:
            r1 = 0
        L_0x0072:
            org.telegram.ui.Components.ImageUpdater r3 = r6.imageUpdater
            r4 = 1
            if (r3 != 0) goto L_0x0081
            org.telegram.ui.ActionBar.ActionBar r3 = r6.actionBar
            boolean r3 = r3.isSearchFieldVisible()
            if (r3 == 0) goto L_0x0081
            r3 = 1
            goto L_0x0082
        L_0x0081:
            r3 = 0
        L_0x0082:
            int r5 = r6.sharedMediaRow
            if (r5 == r2) goto L_0x009d
            if (r3 != 0) goto L_0x009d
            org.telegram.ui.Components.RecyclerListView r2 = r6.listView
            androidx.recyclerview.widget.RecyclerView$ViewHolder r2 = r2.findViewHolderForAdapterPosition(r5)
            org.telegram.ui.Components.RecyclerListView$Holder r2 = (org.telegram.ui.Components.RecyclerListView.Holder) r2
            if (r2 == 0) goto L_0x009c
            android.view.View r2 = r2.itemView
            int r2 = r2.getTop()
            if (r2 > 0) goto L_0x009c
            r3 = 1
            goto L_0x009d
        L_0x009c:
            r3 = 0
        L_0x009d:
            r6.setMediaHeaderVisible(r3)
            float r2 = r6.extraHeight
            float r1 = (float) r1
            int r2 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1))
            if (r2 == 0) goto L_0x00bf
            r6.extraHeight = r1
            org.telegram.ui.ProfileActivity$TopView r1 = r6.topView
            r1.invalidate()
            int r1 = r6.playProfileAnimation
            if (r1 == 0) goto L_0x00bc
            float r1 = r6.extraHeight
            r2 = 0
            int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
            if (r1 == 0) goto L_0x00ba
            r0 = 1
        L_0x00ba:
            r6.allowProfileAnimation = r0
        L_0x00bc:
            r6.needLayout(r4)
        L_0x00bf:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.checkListViewScroll():void");
    }

    /* access modifiers changed from: private */
    public void updateSelectedMediaTabText() {
        SharedMediaLayout sharedMediaLayout2 = this.sharedMediaLayout;
        if (sharedMediaLayout2 != null && this.mediaCounterTextView != null) {
            int closestTab = sharedMediaLayout2.getClosestTab();
            int[] lastMediaCount = this.sharedMediaPreloader.getLastMediaCount();
            if (closestTab == 0) {
                this.mediaCounterTextView.setText(LocaleController.formatPluralString("Media", lastMediaCount[0]));
            } else if (closestTab == 1) {
                this.mediaCounterTextView.setText(LocaleController.formatPluralString("Files", lastMediaCount[1]));
            } else if (closestTab == 2) {
                this.mediaCounterTextView.setText(LocaleController.formatPluralString("Voice", lastMediaCount[2]));
            } else if (closestTab == 3) {
                this.mediaCounterTextView.setText(LocaleController.formatPluralString("Links", lastMediaCount[3]));
            } else if (closestTab == 4) {
                this.mediaCounterTextView.setText(LocaleController.formatPluralString("MusicFiles", lastMediaCount[4]));
            } else if (closestTab == 5) {
                this.mediaCounterTextView.setText(LocaleController.formatPluralString("GIFs", lastMediaCount[5]));
            } else if (closestTab == 6) {
                this.mediaCounterTextView.setText(LocaleController.formatPluralString("CommonGroups", this.userInfo.common_chats_count));
            } else if (closestTab == 7) {
                this.mediaCounterTextView.setText(this.onlineTextView[1].getText());
            }
        }
    }

    /* access modifiers changed from: private */
    public void needLayout(boolean z) {
        ValueAnimator valueAnimator;
        BackupImageView currentItemView;
        TLRPC$ChatFull tLRPC$ChatFull;
        int i = 0;
        int currentActionBarHeight = (this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
        RecyclerListView recyclerListView = this.listView;
        if (recyclerListView != null && !this.openAnimationInProgress) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) recyclerListView.getLayoutParams();
            if (layoutParams.topMargin != currentActionBarHeight) {
                layoutParams.topMargin = currentActionBarHeight;
                this.listView.setLayoutParams(layoutParams);
            }
        }
        if (this.avatarContainer != null) {
            float min = Math.min(1.0f, this.extraHeight / ((float) AndroidUtilities.dp(88.0f)));
            this.listView.setTopGlowOffset((int) this.extraHeight);
            this.listView.setOverScrollMode((this.extraHeight <= ((float) AndroidUtilities.dp(88.0f)) || this.extraHeight >= ((float) (this.listView.getMeasuredWidth() - currentActionBarHeight))) ? 0 : 2);
            ImageView imageView = this.writeButton;
            if (imageView != null) {
                imageView.setTranslationY(((((float) ((this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight())) + this.extraHeight) + ((float) this.searchTransitionOffset)) - ((float) AndroidUtilities.dp(29.5f)));
                if (!this.openAnimationInProgress) {
                    float f = 0.2f;
                    boolean z2 = min > 0.2f && !this.searchMode && (this.imageUpdater == null || this.setAvatarRow == -1);
                    if (z2 && this.chat_id != 0) {
                        z2 = (!ChatObject.isChannel(this.currentChat) || this.currentChat.megagroup || (tLRPC$ChatFull = this.chatInfo) == null || tLRPC$ChatFull.linked_chat_id == 0 || this.infoHeaderRow == -1) ? false : true;
                    }
                    if (z2 != (this.writeButton.getTag() == null)) {
                        if (z2) {
                            this.writeButton.setTag((Object) null);
                        } else {
                            this.writeButton.setTag(0);
                        }
                        AnimatorSet animatorSet = this.writeButtonAnimation;
                        if (animatorSet != null) {
                            this.writeButtonAnimation = null;
                            animatorSet.cancel();
                        }
                        if (z) {
                            AnimatorSet animatorSet2 = new AnimatorSet();
                            this.writeButtonAnimation = animatorSet2;
                            if (z2) {
                                animatorSet2.setInterpolator(new DecelerateInterpolator());
                                this.writeButtonAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(this.writeButton, View.SCALE_X, new float[]{1.0f}), ObjectAnimator.ofFloat(this.writeButton, View.SCALE_Y, new float[]{1.0f}), ObjectAnimator.ofFloat(this.writeButton, View.ALPHA, new float[]{1.0f})});
                            } else {
                                animatorSet2.setInterpolator(new AccelerateInterpolator());
                                this.writeButtonAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(this.writeButton, View.SCALE_X, new float[]{0.2f}), ObjectAnimator.ofFloat(this.writeButton, View.SCALE_Y, new float[]{0.2f}), ObjectAnimator.ofFloat(this.writeButton, View.ALPHA, new float[]{0.0f})});
                            }
                            this.writeButtonAnimation.setDuration(150);
                            this.writeButtonAnimation.addListener(new AnimatorListenerAdapter() {
                                public void onAnimationEnd(Animator animator) {
                                    if (ProfileActivity.this.writeButtonAnimation != null && ProfileActivity.this.writeButtonAnimation.equals(animator)) {
                                        AnimatorSet unused = ProfileActivity.this.writeButtonAnimation = null;
                                    }
                                }
                            });
                            this.writeButtonAnimation.start();
                        } else {
                            this.writeButton.setScaleX(z2 ? 1.0f : 0.2f);
                            ImageView imageView2 = this.writeButton;
                            if (z2) {
                                f = 1.0f;
                            }
                            imageView2.setScaleY(f);
                            this.writeButton.setAlpha(z2 ? 1.0f : 0.0f);
                        }
                    }
                }
            }
            this.avatarX = (-AndroidUtilities.dpf2(47.0f)) * min;
            float currentActionBarHeight2 = ((float) (this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0)) + ((((float) ActionBar.getCurrentActionBarHeight()) / 2.0f) * (min + 1.0f));
            float f2 = AndroidUtilities.density;
            this.avatarY = (currentActionBarHeight2 - (f2 * 21.0f)) + (f2 * 27.0f * min) + this.actionBar.getTranslationY();
            float f3 = this.openAnimationInProgress ? this.initialAnimationExtraHeight : this.extraHeight;
            if (f3 > ((float) AndroidUtilities.dp(88.0f)) || this.isPulledDown) {
                float max = Math.max(0.0f, Math.min(1.0f, (f3 - ((float) AndroidUtilities.dp(88.0f))) / ((float) ((this.listView.getMeasuredWidth() - currentActionBarHeight) - AndroidUtilities.dp(88.0f)))));
                this.expandProgress = max;
                this.avatarScale = AndroidUtilities.lerp(1.4285715f, 2.4285715f, Math.min(1.0f, max * 3.0f));
                float min2 = Math.min(AndroidUtilities.dpf2(2000.0f), Math.max(AndroidUtilities.dpf2(1100.0f), Math.abs(this.listViewVelocityY))) / AndroidUtilities.dpf2(1100.0f);
                if (!this.allowPullingDown || (!this.openingAvatar && this.expandProgress < 0.33f)) {
                    if (this.isPulledDown) {
                        this.isPulledDown = false;
                        ActionBarMenuItem actionBarMenuItem = this.otherItem;
                        if (actionBarMenuItem != null) {
                            actionBarMenuItem.hideSubItem(21);
                            if (this.imageUpdater != null) {
                                this.otherItem.hideSubItem(33);
                                this.otherItem.hideSubItem(34);
                                this.otherItem.hideSubItem(35);
                                this.otherItem.hideSubItem(36);
                                this.otherItem.showSubItem(31);
                                this.otherItem.showSubItem(30);
                            }
                        }
                        ActionBarMenuItem actionBarMenuItem2 = this.searchItem;
                        if (actionBarMenuItem2 != null) {
                            actionBarMenuItem2.setEnabled(!this.scrolling);
                        }
                        this.overlaysView.setOverlaysVisible(false, min2);
                        this.avatarsViewPagerIndicatorView.refreshVisibility(min2);
                        this.expandAnimator.cancel();
                        this.avatarImage.getImageReceiver().setAllowStartAnimation(true);
                        this.avatarImage.getImageReceiver().startAnimation();
                        float lerp = AndroidUtilities.lerp(this.expandAnimatorValues, this.currentExpanAnimatorFracture);
                        float[] fArr = this.expandAnimatorValues;
                        fArr[0] = lerp;
                        fArr[1] = 0.0f;
                        if (!this.isInLandscapeMode) {
                            this.expandAnimator.setDuration((long) ((lerp * 250.0f) / min2));
                        } else {
                            this.expandAnimator.setDuration(0);
                        }
                        this.topView.setBackgroundColor(Theme.getColor("avatar_backgroundActionBarBlue"));
                        if (!this.doNotSetForeground && (currentItemView = this.avatarsViewPager.getCurrentItemView()) != null) {
                            this.avatarImage.setForegroundImageDrawable(currentItemView.getImageReceiver().getDrawableSafe());
                        }
                        this.avatarImage.setForegroundAlpha(1.0f);
                        this.avatarContainer.setVisibility(0);
                        this.avatarsViewPager.setVisibility(8);
                        this.expandAnimator.start();
                    }
                    this.avatarContainer.setScaleX(this.avatarScale);
                    this.avatarContainer.setScaleY(this.avatarScale);
                    ValueAnimator valueAnimator2 = this.expandAnimator;
                    if (valueAnimator2 == null || !valueAnimator2.isRunning()) {
                        refreshNameAndOnlineXY();
                        this.nameTextView[1].setTranslationX(this.nameX);
                        this.nameTextView[1].setTranslationY(this.nameY);
                        this.onlineTextView[1].setTranslationX(this.onlineX);
                        this.onlineTextView[1].setTranslationY(this.onlineY);
                        this.mediaCounterTextView.setTranslationX(this.onlineX);
                        this.mediaCounterTextView.setTranslationY(this.onlineY);
                    }
                } else {
                    if (!this.isPulledDown) {
                        ActionBarMenuItem actionBarMenuItem3 = this.otherItem;
                        if (actionBarMenuItem3 != null) {
                            actionBarMenuItem3.showSubItem(21);
                            if (this.imageUpdater != null) {
                                this.otherItem.showSubItem(36);
                                this.otherItem.showSubItem(34);
                                this.otherItem.showSubItem(35);
                                this.otherItem.hideSubItem(33);
                                this.otherItem.hideSubItem(31);
                                this.otherItem.hideSubItem(30);
                            }
                        }
                        ActionBarMenuItem actionBarMenuItem4 = this.searchItem;
                        if (actionBarMenuItem4 != null) {
                            actionBarMenuItem4.setEnabled(false);
                        }
                        this.isPulledDown = true;
                        this.overlaysView.setOverlaysVisible(true, min2);
                        this.avatarsViewPagerIndicatorView.refreshVisibility(min2);
                        this.expandAnimator.cancel();
                        float lerp2 = AndroidUtilities.lerp(this.expandAnimatorValues, this.currentExpanAnimatorFracture);
                        float[] fArr2 = this.expandAnimatorValues;
                        fArr2[0] = lerp2;
                        fArr2[1] = 1.0f;
                        this.expandAnimator.setDuration((long) (((1.0f - lerp2) * 250.0f) / min2));
                        this.expandAnimator.addListener(new AnimatorListenerAdapter() {
                            public void onAnimationStart(Animator animator) {
                                ProfileActivity.this.setForegroundImage(false);
                                ProfileActivity.this.avatarsViewPager.setAnimatedFileMaybe(ProfileActivity.this.avatarImage.getImageReceiver().getAnimation());
                                ProfileActivity.this.avatarsViewPager.resetCurrentItem();
                            }

                            public void onAnimationEnd(Animator animator) {
                                ProfileActivity.this.expandAnimator.removeListener(this);
                                ProfileActivity.this.topView.setBackgroundColor(-16777216);
                                ProfileActivity.this.avatarContainer.setVisibility(8);
                                ProfileActivity.this.avatarsViewPager.setVisibility(0);
                            }
                        });
                        this.expandAnimator.start();
                    }
                    ViewGroup.LayoutParams layoutParams2 = this.avatarsViewPager.getLayoutParams();
                    layoutParams2.width = this.listView.getMeasuredWidth();
                    float f4 = f3 + ((float) currentActionBarHeight);
                    layoutParams2.height = (int) f4;
                    this.avatarsViewPager.requestLayout();
                    if (!this.expandAnimator.isRunning()) {
                        float dp = (!this.openAnimationInProgress || this.playProfileAnimation != 2) ? 0.0f : (-(1.0f - this.animationProgress)) * ((float) AndroidUtilities.dp(50.0f));
                        this.nameTextView[1].setTranslationX(AndroidUtilities.dpf2(16.0f) - ((float) this.nameTextView[1].getLeft()));
                        this.nameTextView[1].setTranslationY(((f4 - AndroidUtilities.dpf2(38.0f)) - ((float) this.nameTextView[1].getBottom())) + dp);
                        this.onlineTextView[1].setTranslationX(AndroidUtilities.dpf2(16.0f) - ((float) this.onlineTextView[1].getLeft()));
                        this.onlineTextView[1].setTranslationY(((f4 - AndroidUtilities.dpf2(18.0f)) - ((float) this.onlineTextView[1].getBottom())) + dp);
                        this.mediaCounterTextView.setTranslationX(this.onlineTextView[1].getTranslationX());
                        this.mediaCounterTextView.setTranslationY(this.onlineTextView[1].getTranslationY());
                    }
                }
            }
            if (this.openAnimationInProgress && this.playProfileAnimation == 2) {
                float currentActionBarHeight3 = ((((float) (this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0)) + (((float) ActionBar.getCurrentActionBarHeight()) / 2.0f)) - (AndroidUtilities.density * 21.0f)) + this.actionBar.getTranslationY();
                this.nameTextView[0].setTranslationX(0.0f);
                double d = (double) currentActionBarHeight3;
                this.nameTextView[0].setTranslationY(((float) Math.floor(d)) + ((float) AndroidUtilities.dp(1.3f)));
                this.onlineTextView[0].setTranslationX(0.0f);
                this.onlineTextView[0].setTranslationY(((float) Math.floor(d)) + ((float) AndroidUtilities.dp(24.0f)));
                this.nameTextView[0].setScaleX(1.0f);
                this.nameTextView[0].setScaleY(1.0f);
                SimpleTextView[] simpleTextViewArr = this.nameTextView;
                simpleTextViewArr[1].setPivotY((float) simpleTextViewArr[1].getMeasuredHeight());
                this.nameTextView[1].setScaleX(1.67f);
                this.nameTextView[1].setScaleY(1.67f);
                this.avatarScale = AndroidUtilities.lerp(1.0f, 2.4285715f, this.animationProgress);
                this.avatarImage.setRoundRadius((int) AndroidUtilities.lerp(AndroidUtilities.dpf2(21.0f), 0.0f, this.animationProgress));
                this.avatarContainer.setTranslationX(AndroidUtilities.lerp(0.0f, 0.0f, this.animationProgress));
                this.avatarContainer.setTranslationY(AndroidUtilities.lerp((float) Math.ceil(d), 0.0f, this.animationProgress));
                this.avatarContainer.setScaleX(this.avatarScale);
                this.avatarContainer.setScaleY(this.avatarScale);
                this.overlaysView.setAlphaValue(this.animationProgress, false);
                this.actionBar.setItemsColor(ColorUtils.blendARGB(Theme.getColor("actionBarDefaultIcon"), -1, this.animationProgress), false);
                ScamDrawable scamDrawable2 = this.scamDrawable;
                if (scamDrawable2 != null) {
                    scamDrawable2.setColor(ColorUtils.blendARGB(Theme.getColor("avatar_subtitleInProfileBlue"), Color.argb(179, 255, 255, 255), this.animationProgress));
                }
                Drawable drawable = this.lockIconDrawable;
                if (drawable != null) {
                    drawable.setColorFilter(ColorUtils.blendARGB(Theme.getColor("chat_lockIcon"), -1, this.animationProgress), PorterDuff.Mode.MULTIPLY);
                }
                CrossfadeDrawable crossfadeDrawable = this.verifiedCrossfadeDrawable;
                if (crossfadeDrawable != null) {
                    crossfadeDrawable.setProgress(this.animationProgress);
                    this.nameTextView[1].invalidate();
                }
                FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) this.avatarContainer.getLayoutParams();
                int lerp3 = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(42.0f), (this.extraHeight + ((float) currentActionBarHeight)) / this.avatarScale, this.animationProgress);
                layoutParams3.height = lerp3;
                layoutParams3.width = lerp3;
                layoutParams3.leftMargin = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(64.0f), 0.0f, this.animationProgress);
                this.avatarContainer.requestLayout();
            } else if (this.extraHeight <= ((float) AndroidUtilities.dp(88.0f))) {
                this.avatarScale = ((min * 18.0f) + 42.0f) / 42.0f;
                float f5 = (0.12f * min) + 1.0f;
                ValueAnimator valueAnimator3 = this.expandAnimator;
                if (valueAnimator3 == null || !valueAnimator3.isRunning()) {
                    this.avatarContainer.setScaleX(this.avatarScale);
                    this.avatarContainer.setScaleY(this.avatarScale);
                    this.avatarContainer.setTranslationX(this.avatarX);
                    this.avatarContainer.setTranslationY((float) Math.ceil((double) this.avatarY));
                }
                this.nameX = AndroidUtilities.density * -21.0f * min;
                this.nameY = ((float) Math.floor((double) this.avatarY)) + ((float) AndroidUtilities.dp(1.3f)) + (((float) AndroidUtilities.dp(7.0f)) * min);
                this.onlineX = AndroidUtilities.density * -21.0f * min;
                this.onlineY = ((float) Math.floor((double) this.avatarY)) + ((float) AndroidUtilities.dp(24.0f)) + (((float) Math.floor((double) (AndroidUtilities.density * 11.0f))) * min);
                while (true) {
                    SimpleTextView[] simpleTextViewArr2 = this.nameTextView;
                    if (i >= simpleTextViewArr2.length) {
                        break;
                    }
                    if (simpleTextViewArr2[i] != null) {
                        ValueAnimator valueAnimator4 = this.expandAnimator;
                        if (valueAnimator4 == null || !valueAnimator4.isRunning()) {
                            this.nameTextView[i].setTranslationX(this.nameX);
                            this.nameTextView[i].setTranslationY(this.nameY);
                            this.onlineTextView[i].setTranslationX(this.onlineX);
                            this.onlineTextView[i].setTranslationY(this.onlineY);
                            if (i == 1) {
                                this.mediaCounterTextView.setTranslationX(this.onlineX);
                                this.mediaCounterTextView.setTranslationY(this.onlineY);
                            }
                        }
                        this.nameTextView[i].setScaleX(f5);
                        this.nameTextView[i].setScaleY(f5);
                    }
                    i++;
                }
            }
            if (!this.openAnimationInProgress && ((valueAnimator = this.expandAnimator) == null || !valueAnimator.isRunning())) {
                needLayoutText(min);
            }
        }
        if (this.isPulledDown || (this.overlaysView.animator != null && this.overlaysView.animator.isRunning())) {
            ViewGroup.LayoutParams layoutParams4 = this.overlaysView.getLayoutParams();
            layoutParams4.width = this.listView.getMeasuredWidth();
            layoutParams4.height = (int) (this.extraHeight + ((float) currentActionBarHeight));
            this.overlaysView.requestLayout();
        }
    }

    /* access modifiers changed from: private */
    public void setForegroundImage(boolean z) {
        Drawable drawable = this.avatarImage.getImageReceiver().getDrawable();
        String str = null;
        if (drawable instanceof AnimatedFileDrawable) {
            AnimatedFileDrawable animatedFileDrawable = (AnimatedFileDrawable) drawable;
            this.avatarImage.setForegroundImage((ImageLocation) null, (String) null, animatedFileDrawable);
            if (z) {
                animatedFileDrawable.addSecondParentView(this.avatarImage);
                return;
            }
            return;
        }
        ImageLocation imageLocation = this.avatarsViewPager.getImageLocation(0);
        if (imageLocation != null && imageLocation.imageType == 2) {
            str = "g";
        }
        this.avatarImage.setForegroundImage(imageLocation, str, drawable);
    }

    private void refreshNameAndOnlineXY() {
        this.nameX = ((float) AndroidUtilities.dp(-21.0f)) + (((float) this.avatarContainer.getMeasuredWidth()) * (this.avatarScale - 1.4285715f));
        this.nameY = ((float) Math.floor((double) this.avatarY)) + ((float) AndroidUtilities.dp(1.3f)) + ((float) AndroidUtilities.dp(7.0f)) + ((((float) this.avatarContainer.getMeasuredHeight()) * (this.avatarScale - 1.4285715f)) / 2.0f);
        this.onlineX = ((float) AndroidUtilities.dp(-21.0f)) + (((float) this.avatarContainer.getMeasuredWidth()) * (this.avatarScale - 1.4285715f));
        this.onlineY = ((float) Math.floor((double) this.avatarY)) + ((float) AndroidUtilities.dp(24.0f)) + ((float) Math.floor((double) (AndroidUtilities.density * 11.0f))) + ((((float) this.avatarContainer.getMeasuredHeight()) * (this.avatarScale - 1.4285715f)) / 2.0f);
    }

    public RecyclerListView getListView() {
        return this.listView;
    }

    /* access modifiers changed from: private */
    public void needLayoutText(float f) {
        float scaleX = this.nameTextView[1].getScaleX();
        float f2 = this.extraHeight > ((float) AndroidUtilities.dp(88.0f)) ? 1.67f : 1.12f;
        if (this.extraHeight <= ((float) AndroidUtilities.dp(88.0f)) || scaleX == f2) {
            int dp = AndroidUtilities.isTablet() ? AndroidUtilities.dp(490.0f) : AndroidUtilities.displaySize.x;
            int dp2 = AndroidUtilities.dp((this.avatarsViewPagerIndicatorView.getSecondaryMenuItem() != null ? (1.0f - this.mediaHeaderAnimationProgress) * 48.0f : 0.0f) + 40.0f + (this.videoCallItemVisible ? (1.0f - this.mediaHeaderAnimationProgress) * 48.0f : 0.0f) + 126.0f);
            int i = dp - dp2;
            float f3 = (float) dp;
            int max = (int) ((f3 - (((float) dp2) * Math.max(0.0f, 1.0f - (f != 1.0f ? (0.15f * f) / (1.0f - f) : 1.0f)))) - this.nameTextView[1].getTranslationX());
            float measureText = (this.nameTextView[1].getPaint().measureText(this.nameTextView[1].getText().toString()) * scaleX) + ((float) this.nameTextView[1].getSideDrawablesSize());
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.nameTextView[1].getLayoutParams();
            int i2 = layoutParams.width;
            float f4 = (float) max;
            if (f4 < measureText) {
                layoutParams.width = Math.max(i, (int) Math.ceil((double) (((float) (max - AndroidUtilities.dp(24.0f))) / (((f2 - scaleX) * 7.0f) + scaleX))));
            } else {
                layoutParams.width = (int) Math.ceil((double) measureText);
            }
            int min = (int) Math.min(((f3 - this.nameTextView[1].getX()) / scaleX) - ((float) AndroidUtilities.dp(8.0f)), (float) layoutParams.width);
            layoutParams.width = min;
            if (min != i2) {
                this.nameTextView[1].requestLayout();
            }
            float measureText2 = this.onlineTextView[1].getPaint().measureText(this.onlineTextView[1].getText().toString());
            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.onlineTextView[1].getLayoutParams();
            FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) this.mediaCounterTextView.getLayoutParams();
            int i3 = layoutParams2.width;
            int ceil = (int) Math.ceil((double) (this.onlineTextView[1].getTranslationX() + ((float) AndroidUtilities.dp(8.0f)) + (((float) AndroidUtilities.dp(40.0f)) * (1.0f - f))));
            layoutParams2.rightMargin = ceil;
            layoutParams3.rightMargin = ceil;
            if (f4 < measureText2) {
                int ceil2 = (int) Math.ceil((double) max);
                layoutParams2.width = ceil2;
                layoutParams3.width = ceil2;
            } else {
                layoutParams2.width = -2;
                layoutParams3.width = -2;
            }
            if (i3 != layoutParams2.width) {
                this.onlineTextView[1].requestLayout();
                this.mediaCounterTextView.requestLayout();
            }
        }
    }

    private void fixLayout() {
        View view = this.fragmentView;
        if (view != null) {
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    if (ProfileActivity.this.fragmentView != null) {
                        ProfileActivity.this.checkListViewScroll();
                        ProfileActivity.this.needLayout(true);
                        ProfileActivity.this.fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    return true;
                }
            });
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        View findViewByPosition;
        super.onConfigurationChanged(configuration);
        SharedMediaLayout sharedMediaLayout2 = this.sharedMediaLayout;
        if (sharedMediaLayout2 != null) {
            sharedMediaLayout2.onConfigurationChanged(configuration);
        }
        invalidateIsInLandscapeMode();
        if (this.isInLandscapeMode && this.isPulledDown && (findViewByPosition = this.layoutManager.findViewByPosition(0)) != null) {
            this.listView.scrollBy(0, findViewByPosition.getTop() - AndroidUtilities.dp(88.0f));
        }
        fixLayout();
    }

    private void invalidateIsInLandscapeMode() {
        Point point = new Point();
        getParentActivity().getWindowManager().getDefaultDisplay().getSize(point);
        this.isInLandscapeMode = point.x > point.y;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: boolean} */
    /* JADX WARNING: type inference failed for: r0v0 */
    /* JADX WARNING: type inference failed for: r0v1, types: [int] */
    /* JADX WARNING: type inference failed for: r0v3 */
    /* JADX WARNING: type inference failed for: r0v4 */
    /* JADX WARNING: type inference failed for: r0v6 */
    /* JADX WARNING: type inference failed for: r0v7, types: [int] */
    /* JADX WARNING: type inference failed for: r0v9 */
    /* JADX WARNING: type inference failed for: r0v10 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void didReceivedNotification(int r11, int r12, java.lang.Object... r13) {
        /*
            r10 = this;
            int r12 = org.telegram.messenger.NotificationCenter.updateInterfaces
            r0 = 0
            r1 = 1
            if (r11 != r12) goto L_0x0081
            r11 = r13[r0]
            java.lang.Integer r11 = (java.lang.Integer) r11
            int r11 = r11.intValue()
            r12 = r11 & 2
            if (r12 != 0) goto L_0x001c
            r12 = r11 & 1
            if (r12 != 0) goto L_0x001c
            r12 = r11 & 4
            if (r12 == 0) goto L_0x001b
            goto L_0x001c
        L_0x001b:
            r1 = 0
        L_0x001c:
            int r12 = r10.user_id
            if (r12 == 0) goto L_0x0040
            if (r1 == 0) goto L_0x0025
            r10.updateProfileData()
        L_0x0025:
            r11 = r11 & 1024(0x400, float:1.435E-42)
            if (r11 == 0) goto L_0x029c
            org.telegram.ui.Components.RecyclerListView r11 = r10.listView
            if (r11 == 0) goto L_0x029c
            int r12 = r10.phoneRow
            androidx.recyclerview.widget.RecyclerView$ViewHolder r11 = r11.findViewHolderForPosition(r12)
            org.telegram.ui.Components.RecyclerListView$Holder r11 = (org.telegram.ui.Components.RecyclerListView.Holder) r11
            if (r11 == 0) goto L_0x029c
            org.telegram.ui.ProfileActivity$ListAdapter r12 = r10.listAdapter
            int r13 = r10.phoneRow
            r12.onBindViewHolder(r11, r13)
            goto L_0x029c
        L_0x0040:
            int r12 = r10.chat_id
            if (r12 == 0) goto L_0x029c
            r12 = r11 & 8192(0x2000, float:1.14794E-41)
            if (r12 != 0) goto L_0x0058
            r13 = r11 & 8
            if (r13 != 0) goto L_0x0058
            r13 = r11 & 16
            if (r13 != 0) goto L_0x0058
            r13 = r11 & 32
            if (r13 != 0) goto L_0x0058
            r13 = r11 & 4
            if (r13 == 0) goto L_0x005e
        L_0x0058:
            r10.updateOnlineCount()
            r10.updateProfileData()
        L_0x005e:
            if (r12 == 0) goto L_0x0063
            r10.updateListAnimated()
        L_0x0063:
            if (r1 == 0) goto L_0x029c
            org.telegram.ui.Components.RecyclerListView r12 = r10.listView
            if (r12 == 0) goto L_0x029c
            int r12 = r12.getChildCount()
        L_0x006d:
            if (r0 >= r12) goto L_0x029c
            org.telegram.ui.Components.RecyclerListView r13 = r10.listView
            android.view.View r13 = r13.getChildAt(r0)
            boolean r1 = r13 instanceof org.telegram.ui.Cells.UserCell
            if (r1 == 0) goto L_0x007e
            org.telegram.ui.Cells.UserCell r13 = (org.telegram.ui.Cells.UserCell) r13
            r13.update(r11)
        L_0x007e:
            int r0 = r0 + 1
            goto L_0x006d
        L_0x0081:
            int r12 = org.telegram.messenger.NotificationCenter.chatOnlineCountDidLoad
            if (r11 != r12) goto L_0x00af
            r11 = r13[r0]
            java.lang.Integer r11 = (java.lang.Integer) r11
            org.telegram.tgnet.TLRPC$ChatFull r12 = r10.chatInfo
            if (r12 == 0) goto L_0x00ae
            org.telegram.tgnet.TLRPC$Chat r12 = r10.currentChat
            if (r12 == 0) goto L_0x00ae
            int r12 = r12.id
            int r11 = r11.intValue()
            if (r12 == r11) goto L_0x009a
            goto L_0x00ae
        L_0x009a:
            org.telegram.tgnet.TLRPC$ChatFull r11 = r10.chatInfo
            r12 = r13[r1]
            java.lang.Integer r12 = (java.lang.Integer) r12
            int r12 = r12.intValue()
            r11.online_count = r12
            r10.updateOnlineCount()
            r10.updateProfileData()
            goto L_0x029c
        L_0x00ae:
            return
        L_0x00af:
            int r12 = org.telegram.messenger.NotificationCenter.contactsDidLoad
            if (r11 != r12) goto L_0x00b8
            r10.createActionBarMenu(r1)
            goto L_0x029c
        L_0x00b8:
            int r12 = org.telegram.messenger.NotificationCenter.encryptedChatCreated
            if (r11 != r12) goto L_0x00ca
            boolean r11 = r10.creatingChat
            if (r11 == 0) goto L_0x029c
            org.telegram.ui.-$$Lambda$ProfileActivity$f6qhfvIRavat7GmkhHIfMUo2wQU r11 = new org.telegram.ui.-$$Lambda$ProfileActivity$f6qhfvIRavat7GmkhHIfMUo2wQU
            r11.<init>(r13)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r11)
            goto L_0x029c
        L_0x00ca:
            int r12 = org.telegram.messenger.NotificationCenter.encryptedChatUpdated
            if (r11 != r12) goto L_0x00e3
            r11 = r13[r0]
            org.telegram.tgnet.TLRPC$EncryptedChat r11 = (org.telegram.tgnet.TLRPC$EncryptedChat) r11
            org.telegram.tgnet.TLRPC$EncryptedChat r12 = r10.currentEncryptedChat
            if (r12 == 0) goto L_0x029c
            int r13 = r11.id
            int r12 = r12.id
            if (r13 != r12) goto L_0x029c
            r10.currentEncryptedChat = r11
            r10.updateListAnimated()
            goto L_0x029c
        L_0x00e3:
            int r12 = org.telegram.messenger.NotificationCenter.blockedUsersDidLoad
            if (r11 != r12) goto L_0x0104
            boolean r11 = r10.userBlocked
            org.telegram.messenger.MessagesController r12 = r10.getMessagesController()
            android.util.SparseIntArray r12 = r12.blockePeers
            int r13 = r10.user_id
            int r12 = r12.indexOfKey(r13)
            if (r12 < 0) goto L_0x00f8
            r0 = 1
        L_0x00f8:
            r10.userBlocked = r0
            if (r11 == r0) goto L_0x029c
            r10.createActionBarMenu(r1)
            r10.updateListAnimated()
            goto L_0x029c
        L_0x0104:
            int r12 = org.telegram.messenger.NotificationCenter.groupCallUpdated
            if (r11 != r12) goto L_0x0149
            r11 = r13[r0]
            java.lang.Integer r11 = (java.lang.Integer) r11
            org.telegram.tgnet.TLRPC$Chat r12 = r10.currentChat
            if (r12 == 0) goto L_0x029c
            int r12 = r11.intValue()
            org.telegram.tgnet.TLRPC$Chat r13 = r10.currentChat
            int r1 = r13.id
            if (r12 != r1) goto L_0x029c
            boolean r12 = org.telegram.messenger.ChatObject.canManageCalls(r13)
            if (r12 == 0) goto L_0x029c
            int r12 = r10.currentAccount
            org.telegram.messenger.MessagesController r12 = org.telegram.messenger.MessagesController.getInstance(r12)
            int r11 = r11.intValue()
            org.telegram.tgnet.TLRPC$ChatFull r11 = r12.getChatFull(r11)
            if (r11 == 0) goto L_0x0132
            r10.chatInfo = r11
        L_0x0132:
            org.telegram.tgnet.TLRPC$ChatFull r11 = r10.chatInfo
            if (r11 == 0) goto L_0x029c
            org.telegram.tgnet.TLRPC$TL_inputGroupCall r11 = r11.call
            if (r11 != 0) goto L_0x013e
            boolean r12 = r10.hasVoiceChatItem
            if (r12 == 0) goto L_0x0144
        L_0x013e:
            if (r11 == 0) goto L_0x029c
            boolean r11 = r10.hasVoiceChatItem
            if (r11 == 0) goto L_0x029c
        L_0x0144:
            r10.createActionBarMenu(r0)
            goto L_0x029c
        L_0x0149:
            int r12 = org.telegram.messenger.NotificationCenter.chatInfoDidLoad
            r2 = 2
            if (r11 != r12) goto L_0x01ca
            r11 = r13[r0]
            org.telegram.tgnet.TLRPC$ChatFull r11 = (org.telegram.tgnet.TLRPC$ChatFull) r11
            int r12 = r11.id
            int r3 = r10.chat_id
            if (r12 != r3) goto L_0x029c
            r12 = r13[r2]
            java.lang.Boolean r12 = (java.lang.Boolean) r12
            boolean r12 = r12.booleanValue()
            org.telegram.tgnet.TLRPC$ChatFull r13 = r10.chatInfo
            boolean r2 = r13 instanceof org.telegram.tgnet.TLRPC$TL_channelFull
            if (r2 == 0) goto L_0x016e
            org.telegram.tgnet.TLRPC$ChatParticipants r2 = r11.participants
            if (r2 != 0) goto L_0x016e
            org.telegram.tgnet.TLRPC$ChatParticipants r2 = r13.participants
            r11.participants = r2
        L_0x016e:
            if (r13 != 0) goto L_0x0175
            boolean r13 = r11 instanceof org.telegram.tgnet.TLRPC$TL_channelFull
            if (r13 == 0) goto L_0x0175
            r0 = 1
        L_0x0175:
            r10.chatInfo = r11
            long r2 = r10.mergeDialogId
            r4 = 0
            int r13 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r13 != 0) goto L_0x0194
            int r11 = r11.migrated_from_chat_id
            if (r11 == 0) goto L_0x0194
            int r11 = -r11
            long r2 = (long) r11
            r10.mergeDialogId = r2
            org.telegram.messenger.MediaDataController r4 = r10.getMediaDataController()
            long r5 = r10.mergeDialogId
            r7 = 0
            int r8 = r10.classGuid
            r9 = 1
            r4.getMediaCount(r5, r7, r8, r9)
        L_0x0194:
            r10.fetchUsersFromChannelInfo()
            r10.updateOnlineCount()
            org.telegram.ui.Components.ProfileGalleryView r11 = r10.avatarsViewPager
            if (r11 == 0) goto L_0x01a3
            org.telegram.tgnet.TLRPC$ChatFull r13 = r10.chatInfo
            r11.setChatInfo(r13)
        L_0x01a3:
            r10.updateListAnimated()
            org.telegram.messenger.MessagesController r11 = r10.getMessagesController()
            int r13 = r10.chat_id
            java.lang.Integer r13 = java.lang.Integer.valueOf(r13)
            org.telegram.tgnet.TLRPC$Chat r11 = r11.getChat(r13)
            if (r11 == 0) goto L_0x01bb
            r10.currentChat = r11
            r10.createActionBarMenu(r1)
        L_0x01bb:
            org.telegram.tgnet.TLRPC$Chat r11 = r10.currentChat
            boolean r11 = r11.megagroup
            if (r11 == 0) goto L_0x029c
            if (r0 != 0) goto L_0x01c5
            if (r12 != 0) goto L_0x029c
        L_0x01c5:
            r10.getChannelParticipants(r1)
            goto L_0x029c
        L_0x01ca:
            int r12 = org.telegram.messenger.NotificationCenter.closeChats
            if (r11 != r12) goto L_0x01d3
            r10.removeSelfFromStack()
            goto L_0x029c
        L_0x01d3:
            int r12 = org.telegram.messenger.NotificationCenter.botInfoDidLoad
            if (r11 != r12) goto L_0x01e8
            r11 = r13[r0]
            org.telegram.tgnet.TLRPC$BotInfo r11 = (org.telegram.tgnet.TLRPC$BotInfo) r11
            int r12 = r11.user_id
            int r13 = r10.user_id
            if (r12 != r13) goto L_0x029c
            r10.botInfo = r11
            r10.updateListAnimated()
            goto L_0x029c
        L_0x01e8:
            int r12 = org.telegram.messenger.NotificationCenter.userInfoDidLoad
            if (r11 != r12) goto L_0x0243
            r11 = r13[r0]
            java.lang.Integer r11 = (java.lang.Integer) r11
            int r11 = r11.intValue()
            int r12 = r10.user_id
            if (r11 != r12) goto L_0x029c
            r11 = r13[r1]
            org.telegram.tgnet.TLRPC$UserFull r11 = (org.telegram.tgnet.TLRPC$UserFull) r11
            r10.userInfo = r11
            org.telegram.ui.Components.ImageUpdater r12 = r10.imageUpdater
            if (r12 == 0) goto L_0x0215
            java.lang.String r11 = r11.about
            java.lang.String r12 = r10.currentBio
            boolean r11 = android.text.TextUtils.equals(r11, r12)
            if (r11 != 0) goto L_0x029c
            org.telegram.ui.ProfileActivity$ListAdapter r11 = r10.listAdapter
            int r12 = r10.bioRow
            r11.notifyItemChanged(r12)
            goto L_0x029c
        L_0x0215:
            boolean r11 = r10.openAnimationInProgress
            if (r11 != 0) goto L_0x0221
            boolean r11 = r10.callItemVisible
            if (r11 != 0) goto L_0x0221
            r10.createActionBarMenu(r1)
            goto L_0x0223
        L_0x0221:
            r10.recreateMenuAfterAnimation = r1
        L_0x0223:
            r10.updateListAnimated()
            org.telegram.ui.Components.SharedMediaLayout r11 = r10.sharedMediaLayout
            org.telegram.tgnet.TLRPC$UserFull r12 = r10.userInfo
            int r12 = r12.common_chats_count
            r11.setCommonGroupsCount(r12)
            r10.updateSelectedMediaTabText()
            org.telegram.ui.Components.SharedMediaLayout$SharedMediaPreloader r11 = r10.sharedMediaPreloader
            if (r11 == 0) goto L_0x023c
            boolean r11 = r11.isMediaWasLoaded()
            if (r11 == 0) goto L_0x029c
        L_0x023c:
            r10.resumeDelayedFragmentAnimation()
            r10.needLayout(r1)
            goto L_0x029c
        L_0x0243:
            int r12 = org.telegram.messenger.NotificationCenter.didReceiveNewMessages
            if (r11 != r12) goto L_0x0291
            r11 = r13[r2]
            java.lang.Boolean r11 = (java.lang.Boolean) r11
            boolean r11 = r11.booleanValue()
            if (r11 == 0) goto L_0x0252
            return
        L_0x0252:
            long r11 = r10.getDialogId()
            r2 = r13[r0]
            java.lang.Long r2 = (java.lang.Long) r2
            long r2 = r2.longValue()
            int r4 = (r11 > r2 ? 1 : (r11 == r2 ? 0 : -1))
            if (r4 != 0) goto L_0x029c
            int r12 = (int) r11
            r11 = r13[r1]
            java.util.ArrayList r11 = (java.util.ArrayList) r11
        L_0x0267:
            int r12 = r11.size()
            if (r0 >= r12) goto L_0x029c
            java.lang.Object r12 = r11.get(r0)
            org.telegram.messenger.MessageObject r12 = (org.telegram.messenger.MessageObject) r12
            org.telegram.tgnet.TLRPC$EncryptedChat r13 = r10.currentEncryptedChat
            if (r13 == 0) goto L_0x028e
            org.telegram.tgnet.TLRPC$Message r12 = r12.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r12 = r12.action
            boolean r13 = r12 instanceof org.telegram.tgnet.TLRPC$TL_messageEncryptedAction
            if (r13 == 0) goto L_0x028e
            org.telegram.tgnet.TLRPC$DecryptedMessageAction r12 = r12.encryptedAction
            boolean r13 = r12 instanceof org.telegram.tgnet.TLRPC$TL_decryptedMessageActionSetMessageTTL
            if (r13 == 0) goto L_0x028e
            org.telegram.tgnet.TLRPC$TL_decryptedMessageActionSetMessageTTL r12 = (org.telegram.tgnet.TLRPC$TL_decryptedMessageActionSetMessageTTL) r12
            org.telegram.ui.ProfileActivity$ListAdapter r12 = r10.listAdapter
            if (r12 == 0) goto L_0x028e
            r12.notifyDataSetChanged()
        L_0x028e:
            int r0 = r0 + 1
            goto L_0x0267
        L_0x0291:
            int r12 = org.telegram.messenger.NotificationCenter.emojiDidLoad
            if (r11 != r12) goto L_0x029c
            org.telegram.ui.Components.RecyclerListView r11 = r10.listView
            if (r11 == 0) goto L_0x029c
            r11.invalidateViews()
        L_0x029c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.didReceivedNotification(int, int, java.lang.Object[]):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$didReceivedNotification$26 */
    public /* synthetic */ void lambda$didReceivedNotification$26$ProfileActivity(Object[] objArr) {
        NotificationCenter notificationCenter = getNotificationCenter();
        int i = NotificationCenter.closeChats;
        notificationCenter.removeObserver(this, i);
        getNotificationCenter().postNotificationName(i, new Object[0]);
        Bundle bundle = new Bundle();
        bundle.putInt("enc_id", objArr[0].id);
        presentFragment(new ChatActivity(bundle), true);
    }

    public boolean needDelayOpenAnimation() {
        return this.playProfileAnimation == 0;
    }

    public void mediaCountUpdated() {
        SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader2;
        SharedMediaLayout sharedMediaLayout2 = this.sharedMediaLayout;
        if (!(sharedMediaLayout2 == null || (sharedMediaPreloader2 = this.sharedMediaPreloader) == null)) {
            sharedMediaLayout2.setNewMediaCounts(sharedMediaPreloader2.getLastMediaCount());
        }
        updateSharedMediaRows();
        updateSelectedMediaTabText();
        if (this.userInfo != null) {
            resumeDelayedFragmentAnimation();
        }
    }

    public void onResume() {
        super.onResume();
        SharedMediaLayout sharedMediaLayout2 = this.sharedMediaLayout;
        if (sharedMediaLayout2 != null) {
            sharedMediaLayout2.onResume();
        }
        invalidateIsInLandscapeMode();
        ListAdapter listAdapter2 = this.listAdapter;
        if (listAdapter2 != null) {
            listAdapter2.notifyDataSetChanged();
        }
        ImageUpdater imageUpdater2 = this.imageUpdater;
        if (imageUpdater2 != null) {
            imageUpdater2.onResume();
            setParentActivityTitle(LocaleController.getString("Settings", NUM));
        }
        updateProfileData();
        fixLayout();
        SimpleTextView[] simpleTextViewArr = this.nameTextView;
        if (simpleTextViewArr[1] != null) {
            setParentActivityTitle(simpleTextViewArr[1].getText());
        }
    }

    public void onPause() {
        super.onPause();
        UndoView undoView2 = this.undoView;
        if (undoView2 != null) {
            undoView2.hide(true, 0);
        }
        ImageUpdater imageUpdater2 = this.imageUpdater;
        if (imageUpdater2 != null) {
            imageUpdater2.onPause();
        }
    }

    public boolean isSwipeBackEnabled(MotionEvent motionEvent) {
        SharedMediaLayout sharedMediaLayout2;
        if (this.sharedMediaRow == -1 || (sharedMediaLayout2 = this.sharedMediaLayout) == null) {
            return true;
        }
        sharedMediaLayout2.getHitRect(this.rect);
        if (!this.rect.contains((int) motionEvent.getX(), ((int) motionEvent.getY()) - this.actionBar.getMeasuredHeight())) {
            return true;
        }
        return this.sharedMediaLayout.isCurrentTabFirst();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000d, code lost:
        r0 = r2.sharedMediaLayout;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onBackPressed() {
        /*
            r2 = this;
            org.telegram.ui.ActionBar.ActionBar r0 = r2.actionBar
            boolean r0 = r0.isEnabled()
            if (r0 == 0) goto L_0x0019
            int r0 = r2.sharedMediaRow
            r1 = -1
            if (r0 == r1) goto L_0x0017
            org.telegram.ui.Components.SharedMediaLayout r0 = r2.sharedMediaLayout
            if (r0 == 0) goto L_0x0017
            boolean r0 = r0.closeActionMode()
            if (r0 != 0) goto L_0x0019
        L_0x0017:
            r0 = 1
            goto L_0x001a
        L_0x0019:
            r0 = 0
        L_0x001a:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.onBackPressed():boolean");
    }

    public boolean isSettings() {
        return this.imageUpdater != null;
    }

    /* access modifiers changed from: protected */
    public void onBecomeFullyHidden() {
        UndoView undoView2 = this.undoView;
        if (undoView2 != null) {
            undoView2.hide(true, 0);
        }
    }

    public void setPlayProfileAnimation(int i) {
        SharedPreferences globalMainSettings = MessagesController.getGlobalMainSettings();
        if (AndroidUtilities.isTablet()) {
            return;
        }
        if (globalMainSettings.getBoolean("view_animations", true)) {
            this.playProfileAnimation = i;
        } else if (i == 2) {
            this.expandPhoto = true;
        }
    }

    private void updateSharedMediaRows() {
        if (this.listAdapter != null) {
            updateListAnimated();
        }
    }

    /* access modifiers changed from: protected */
    public void onTransitionAnimationStart(boolean z, boolean z2) {
        this.isFragmentOpened = z;
        if (((!z && z2) || (z && !z2)) && this.playProfileAnimation != 0 && this.allowProfileAnimation && !this.isPulledDown) {
            this.openAnimationInProgress = true;
        }
        if (z) {
            if (this.imageUpdater != null) {
                this.transitionIndex = getNotificationCenter().setAnimationInProgress(this.transitionIndex, new int[]{NotificationCenter.dialogsNeedReload, NotificationCenter.closeChats, NotificationCenter.mediaCountDidLoad, NotificationCenter.mediaCountsDidLoad, NotificationCenter.userInfoDidLoad});
            } else {
                this.transitionIndex = getNotificationCenter().setAnimationInProgress(this.transitionIndex, new int[]{NotificationCenter.dialogsNeedReload, NotificationCenter.closeChats, NotificationCenter.mediaCountDidLoad, NotificationCenter.mediaCountsDidLoad});
            }
        }
        this.transitionAnimationInProress = true;
    }

    /* access modifiers changed from: protected */
    public void onTransitionAnimationEnd(boolean z, boolean z2) {
        if (z) {
            if (!z2) {
                if (this.playProfileAnimation != 0 && this.allowProfileAnimation) {
                    this.openAnimationInProgress = false;
                    checkListViewScroll();
                    if (this.recreateMenuAfterAnimation) {
                        createActionBarMenu(true);
                    }
                }
                if (!this.fragmentOpened) {
                    this.fragmentOpened = true;
                    this.fragmentView.requestLayout();
                }
            }
            getNotificationCenter().onAnimationFinish(this.transitionIndex);
        }
        this.transitionAnimationInProress = false;
    }

    @Keep
    public float getAnimationProgress() {
        return this.animationProgress;
    }

    @Keep
    public void setAnimationProgress(float f) {
        int i;
        int i2;
        int i3;
        float f2 = f;
        this.animationProgress = f2;
        this.listView.setAlpha(f2);
        this.listView.setTranslationX(((float) AndroidUtilities.dp(48.0f)) - (((float) AndroidUtilities.dp(48.0f)) * f2));
        int i4 = 2;
        if (this.playProfileAnimation != 2 || (i = this.avatarColor) == 0) {
            i = AvatarDrawable.getProfileBackColorForId((this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) ? 5 : this.chat_id);
        }
        int color = Theme.getColor("actionBarDefault");
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        this.topView.setBackgroundColor(Color.rgb(red + ((int) (((float) (Color.red(i) - red)) * f2)), green + ((int) (((float) (Color.green(i) - green)) * f2)), blue + ((int) (((float) (Color.blue(i) - blue)) * f2))));
        int iconColorForId = AvatarDrawable.getIconColorForId((this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) ? 5 : this.chat_id);
        int color2 = Theme.getColor("actionBarDefaultIcon");
        int red2 = Color.red(color2);
        int green2 = Color.green(color2);
        int blue2 = Color.blue(color2);
        this.actionBar.setItemsColor(Color.rgb(red2 + ((int) (((float) (Color.red(iconColorForId) - red2)) * f2)), green2 + ((int) (((float) (Color.green(iconColorForId) - green2)) * f2)), blue2 + ((int) (((float) (Color.blue(iconColorForId) - blue2)) * f2))), false);
        int color3 = Theme.getColor("profile_title");
        int color4 = Theme.getColor("actionBarDefaultTitle");
        int red3 = Color.red(color4);
        int green3 = Color.green(color4);
        int blue3 = Color.blue(color4);
        int alpha = Color.alpha(color4);
        int red4 = (int) (((float) (Color.red(color3) - red3)) * f2);
        int green4 = (int) (((float) (Color.green(color3) - green3)) * f2);
        int blue4 = (int) (((float) (Color.blue(color3) - blue3)) * f2);
        int alpha2 = (int) (((float) (Color.alpha(color3) - alpha)) * f2);
        int i5 = 0;
        while (true) {
            i2 = 1;
            if (i5 >= i4) {
                break;
            }
            SimpleTextView[] simpleTextViewArr = this.nameTextView;
            if (!(simpleTextViewArr[i5] == null || (i5 == 1 && this.playProfileAnimation == i4))) {
                simpleTextViewArr[i5].setTextColor(Color.argb(alpha + alpha2, red3 + red4, green3 + green4, blue3 + blue4));
            }
            i5++;
            i4 = 2;
        }
        if (this.isOnline[0]) {
            i3 = Theme.getColor("profile_status");
        } else {
            i3 = AvatarDrawable.getProfileTextColorForId((this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) ? 5 : this.chat_id);
        }
        int i6 = 0;
        int color5 = Theme.getColor(this.isOnline[0] ? "chat_status" : "actionBarDefaultSubtitle");
        int red5 = Color.red(color5);
        int green5 = Color.green(color5);
        int blue5 = Color.blue(color5);
        int alpha3 = Color.alpha(color5);
        int red6 = (int) (((float) (Color.red(i3) - red5)) * f2);
        int green6 = (int) (((float) (Color.green(i3) - green5)) * f2);
        int blue6 = (int) (((float) (Color.blue(i3) - blue5)) * f2);
        int alpha4 = (int) (((float) (Color.alpha(i3) - alpha3)) * f2);
        int i7 = 2;
        while (i6 < i7) {
            SimpleTextView[] simpleTextViewArr2 = this.onlineTextView;
            if (!(simpleTextViewArr2[i6] == null || (i6 == i2 && this.playProfileAnimation == i7))) {
                simpleTextViewArr2[i6].setTextColor(Color.argb(alpha3 + alpha4, red5 + red6, green5 + green6, blue5 + blue6));
            }
            i6++;
            i7 = 2;
            i2 = 1;
        }
        this.extraHeight = this.initialAnimationExtraHeight * f2;
        int i8 = this.user_id;
        if (i8 == 0) {
            i8 = this.chat_id;
        }
        int profileColorForId = AvatarDrawable.getProfileColorForId(i8);
        int i9 = this.user_id;
        if (i9 == 0) {
            i9 = this.chat_id;
        }
        int colorForId = AvatarDrawable.getColorForId(i9);
        if (profileColorForId != colorForId) {
            this.avatarDrawable.setColor(Color.rgb(Color.red(colorForId) + ((int) (((float) (Color.red(profileColorForId) - Color.red(colorForId))) * f2)), Color.green(colorForId) + ((int) (((float) (Color.green(profileColorForId) - Color.green(colorForId))) * f2)), Color.blue(colorForId) + ((int) (((float) (Color.blue(profileColorForId) - Color.blue(colorForId))) * f2))));
            this.avatarImage.invalidate();
        }
        this.topView.invalidate();
        needLayout(true);
        this.fragmentView.invalidate();
    }

    /* access modifiers changed from: protected */
    public AnimatorSet onCustomTransitionAnimation(boolean z, final Runnable runnable) {
        if (this.playProfileAnimation == 0 || !this.allowProfileAnimation || this.isPulledDown) {
            return null;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(this.playProfileAnimation == 2 ? 250 : 180);
        this.listView.setLayerType(2, (Paint) null);
        ActionBarMenu createMenu = this.actionBar.createMenu();
        if (createMenu.getItem(10) == null && this.animatingItem == null) {
            this.animatingItem = createMenu.addItem(10, NUM);
        }
        if (z) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.onlineTextView[1].getLayoutParams();
            layoutParams.rightMargin = (int) ((AndroidUtilities.density * -21.0f) + ((float) AndroidUtilities.dp(8.0f)));
            this.onlineTextView[1].setLayoutParams(layoutParams);
            if (this.playProfileAnimation != 2) {
                int ceil = (int) Math.ceil((double) (((float) (AndroidUtilities.displaySize.x - AndroidUtilities.dp(126.0f))) + (AndroidUtilities.density * 21.0f)));
                float measureText = (this.nameTextView[1].getPaint().measureText(this.nameTextView[1].getText().toString()) * 1.12f) + ((float) this.nameTextView[1].getSideDrawablesSize());
                FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.nameTextView[1].getLayoutParams();
                float f = (float) ceil;
                if (f < measureText) {
                    layoutParams2.width = (int) Math.ceil((double) (f / 1.12f));
                } else {
                    layoutParams2.width = -2;
                }
                this.nameTextView[1].setLayoutParams(layoutParams2);
                this.initialAnimationExtraHeight = (float) AndroidUtilities.dp(88.0f);
            } else {
                FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) this.nameTextView[1].getLayoutParams();
                layoutParams3.width = (int) (((float) (AndroidUtilities.displaySize.x - AndroidUtilities.dp(32.0f))) / 1.67f);
                this.nameTextView[1].setLayoutParams(layoutParams3);
            }
            this.fragmentView.setBackgroundColor(0);
            setAnimationProgress(0.0f);
            ArrayList arrayList = new ArrayList();
            arrayList.add(ObjectAnimator.ofFloat(this, "animationProgress", new float[]{0.0f, 1.0f}));
            ImageView imageView = this.writeButton;
            if (imageView != null && imageView.getTag() == null) {
                this.writeButton.setScaleX(0.2f);
                this.writeButton.setScaleY(0.2f);
                this.writeButton.setAlpha(0.0f);
                arrayList.add(ObjectAnimator.ofFloat(this.writeButton, View.SCALE_X, new float[]{1.0f}));
                arrayList.add(ObjectAnimator.ofFloat(this.writeButton, View.SCALE_Y, new float[]{1.0f}));
                arrayList.add(ObjectAnimator.ofFloat(this.writeButton, View.ALPHA, new float[]{1.0f}));
            }
            if (this.playProfileAnimation == 2) {
                this.avatarColor = AndroidUtilities.calcBitmapColor(this.avatarImage.getImageReceiver().getBitmap());
                this.nameTextView[1].setTextColor(-1);
                this.onlineTextView[1].setTextColor(Color.argb(179, 255, 255, 255));
                this.actionBar.setItemsBackgroundColor(NUM, false);
                this.overlaysView.setOverlaysVisible();
            }
            int i = 0;
            while (i < 2) {
                this.onlineTextView[i].setAlpha(i == 0 ? 1.0f : 0.0f);
                this.nameTextView[i].setAlpha(i == 0 ? 1.0f : 0.0f);
                SimpleTextView simpleTextView = this.onlineTextView[i];
                Property property = View.ALPHA;
                float[] fArr = new float[1];
                fArr[0] = i == 0 ? 0.0f : 1.0f;
                arrayList.add(ObjectAnimator.ofFloat(simpleTextView, property, fArr));
                SimpleTextView simpleTextView2 = this.nameTextView[i];
                Property property2 = View.ALPHA;
                float[] fArr2 = new float[1];
                fArr2[0] = i == 0 ? 0.0f : 1.0f;
                arrayList.add(ObjectAnimator.ofFloat(simpleTextView2, property2, fArr2));
                i++;
            }
            ActionBarMenuItem actionBarMenuItem = this.animatingItem;
            if (actionBarMenuItem != null) {
                actionBarMenuItem.setAlpha(1.0f);
                arrayList.add(ObjectAnimator.ofFloat(this.animatingItem, View.ALPHA, new float[]{0.0f}));
            }
            if (this.callItemVisible) {
                this.callItem.setAlpha(0.0f);
                arrayList.add(ObjectAnimator.ofFloat(this.callItem, View.ALPHA, new float[]{1.0f}));
            }
            if (this.videoCallItemVisible) {
                this.videoCallItem.setAlpha(0.0f);
                arrayList.add(ObjectAnimator.ofFloat(this.videoCallItem, View.ALPHA, new float[]{1.0f}));
            }
            if (this.editItemVisible) {
                this.editItem.setAlpha(0.0f);
                arrayList.add(ObjectAnimator.ofFloat(this.editItem, View.ALPHA, new float[]{1.0f}));
            }
            animatorSet.playTogether(arrayList);
        } else {
            this.initialAnimationExtraHeight = this.extraHeight;
            ArrayList arrayList2 = new ArrayList();
            arrayList2.add(ObjectAnimator.ofFloat(this, "animationProgress", new float[]{1.0f, 0.0f}));
            ImageView imageView2 = this.writeButton;
            if (imageView2 != null) {
                arrayList2.add(ObjectAnimator.ofFloat(imageView2, View.SCALE_X, new float[]{0.2f}));
                arrayList2.add(ObjectAnimator.ofFloat(this.writeButton, View.SCALE_Y, new float[]{0.2f}));
                arrayList2.add(ObjectAnimator.ofFloat(this.writeButton, View.ALPHA, new float[]{0.0f}));
            }
            int i2 = 0;
            while (i2 < 2) {
                SimpleTextView simpleTextView3 = this.onlineTextView[i2];
                Property property3 = View.ALPHA;
                float[] fArr3 = new float[1];
                fArr3[0] = i2 == 0 ? 1.0f : 0.0f;
                arrayList2.add(ObjectAnimator.ofFloat(simpleTextView3, property3, fArr3));
                SimpleTextView simpleTextView4 = this.nameTextView[i2];
                Property property4 = View.ALPHA;
                float[] fArr4 = new float[1];
                fArr4[0] = i2 == 0 ? 1.0f : 0.0f;
                arrayList2.add(ObjectAnimator.ofFloat(simpleTextView4, property4, fArr4));
                i2++;
            }
            ActionBarMenuItem actionBarMenuItem2 = this.animatingItem;
            if (actionBarMenuItem2 != null) {
                actionBarMenuItem2.setAlpha(0.0f);
                arrayList2.add(ObjectAnimator.ofFloat(this.animatingItem, View.ALPHA, new float[]{1.0f}));
            }
            if (this.callItemVisible) {
                this.callItem.setAlpha(1.0f);
                arrayList2.add(ObjectAnimator.ofFloat(this.callItem, View.ALPHA, new float[]{0.0f}));
            }
            if (this.videoCallItemVisible) {
                this.videoCallItem.setAlpha(1.0f);
                arrayList2.add(ObjectAnimator.ofFloat(this.videoCallItem, View.ALPHA, new float[]{0.0f}));
            }
            if (this.editItemVisible) {
                this.editItem.setAlpha(1.0f);
                arrayList2.add(ObjectAnimator.ofFloat(this.editItem, View.ALPHA, new float[]{0.0f}));
            }
            animatorSet.playTogether(arrayList2);
        }
        this.profileTransitionInProgress = true;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                ProfileActivity.this.lambda$onCustomTransitionAnimation$27$ProfileActivity(valueAnimator);
            }
        });
        animatorSet.playTogether(new Animator[]{ofFloat});
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                ProfileActivity.this.listView.setLayerType(0, (Paint) null);
                if (ProfileActivity.this.animatingItem != null) {
                    ProfileActivity.this.actionBar.createMenu().clearItems();
                    ActionBarMenuItem unused = ProfileActivity.this.animatingItem = null;
                }
                runnable.run();
                if (ProfileActivity.this.playProfileAnimation == 2) {
                    int unused2 = ProfileActivity.this.playProfileAnimation = 1;
                    ProfileActivity.this.avatarImage.setForegroundAlpha(1.0f);
                    ProfileActivity.this.avatarContainer.setVisibility(8);
                    ProfileActivity.this.avatarsViewPager.resetCurrentItem();
                    ProfileActivity.this.avatarsViewPager.setVisibility(0);
                }
                ProfileActivity profileActivity = ProfileActivity.this;
                profileActivity.profileTransitionInProgress = false;
                profileActivity.fragmentView.invalidate();
            }
        });
        animatorSet.setInterpolator(this.playProfileAnimation == 2 ? CubicBezierInterpolator.DEFAULT : new DecelerateInterpolator());
        AndroidUtilities.runOnUIThread(new Runnable(animatorSet) {
            public final /* synthetic */ AnimatorSet f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                this.f$0.start();
            }
        }, 50);
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCustomTransitionAnimation$27 */
    public /* synthetic */ void lambda$onCustomTransitionAnimation$27$ProfileActivity(ValueAnimator valueAnimator) {
        this.fragmentView.invalidate();
    }

    /* access modifiers changed from: private */
    public void updateOnlineCount() {
        int i;
        TLRPC$UserStatus tLRPC$UserStatus;
        this.onlineCount = 0;
        int currentTime = getConnectionsManager().getCurrentTime();
        this.sortedUsers.clear();
        TLRPC$ChatFull tLRPC$ChatFull = this.chatInfo;
        if ((tLRPC$ChatFull instanceof TLRPC$TL_chatFull) || ((tLRPC$ChatFull instanceof TLRPC$TL_channelFull) && tLRPC$ChatFull.participants_count <= 200 && tLRPC$ChatFull.participants != null)) {
            for (int i2 = 0; i2 < this.chatInfo.participants.participants.size(); i2++) {
                TLRPC$User user = getMessagesController().getUser(Integer.valueOf(this.chatInfo.participants.participants.get(i2).user_id));
                if (!(user == null || (tLRPC$UserStatus = user.status) == null || ((tLRPC$UserStatus.expires <= currentTime && user.id != getUserConfig().getClientUserId()) || user.status.expires <= 10000))) {
                    this.onlineCount++;
                }
                this.sortedUsers.add(Integer.valueOf(i2));
            }
            try {
                Collections.sort(this.sortedUsers, new Object(currentTime) {
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final int compare(Object obj, Object obj2) {
                        return ProfileActivity.this.lambda$updateOnlineCount$28$ProfileActivity(this.f$1, (Integer) obj, (Integer) obj2);
                    }

                    public /* synthetic */ Comparator reversed() {
                        return Comparator.CC.$default$reversed(this);
                    }

                    public /* synthetic */ java.util.Comparator thenComparing(Function function) {
                        return Comparator.CC.$default$thenComparing((java.util.Comparator) this, function);
                    }

                    public /* synthetic */ java.util.Comparator thenComparing(Function function, java.util.Comparator comparator) {
                        return Comparator.CC.$default$thenComparing(this, function, comparator);
                    }

                    public /* synthetic */ java.util.Comparator thenComparing(java.util.Comparator comparator) {
                        return Comparator.CC.$default$thenComparing((java.util.Comparator) this, comparator);
                    }

                    public /* synthetic */ java.util.Comparator thenComparingDouble(ToDoubleFunction toDoubleFunction) {
                        return Comparator.CC.$default$thenComparingDouble(this, toDoubleFunction);
                    }

                    public /* synthetic */ java.util.Comparator thenComparingInt(ToIntFunction toIntFunction) {
                        return Comparator.CC.$default$thenComparingInt(this, toIntFunction);
                    }

                    public /* synthetic */ java.util.Comparator thenComparingLong(ToLongFunction toLongFunction) {
                        return Comparator.CC.$default$thenComparingLong(this, toLongFunction);
                    }
                });
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
            ListAdapter listAdapter2 = this.listAdapter;
            if (listAdapter2 != null && (i = this.membersStartRow) > 0) {
                listAdapter2.notifyItemRangeChanged(i, this.sortedUsers.size());
            }
            if (this.sharedMediaLayout != null && this.sharedMediaRow != -1 && this.sortedUsers.size() > 5) {
                this.sharedMediaLayout.setChatUsers(this.sortedUsers, this.chatInfo);
            }
        } else if ((tLRPC$ChatFull instanceof TLRPC$TL_channelFull) && tLRPC$ChatFull.participants_count > 200) {
            this.onlineCount = tLRPC$ChatFull.online_count;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x005c  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0074 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x007f A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x008a A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0093 A[ADDED_TO_REGION] */
    /* renamed from: lambda$updateOnlineCount$28 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ int lambda$updateOnlineCount$28$ProfileActivity(int r5, java.lang.Integer r6, java.lang.Integer r7) {
        /*
            r4 = this;
            org.telegram.messenger.MessagesController r0 = r4.getMessagesController()
            org.telegram.tgnet.TLRPC$ChatFull r1 = r4.chatInfo
            org.telegram.tgnet.TLRPC$ChatParticipants r1 = r1.participants
            java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r1 = r1.participants
            int r7 = r7.intValue()
            java.lang.Object r7 = r1.get(r7)
            org.telegram.tgnet.TLRPC$ChatParticipant r7 = (org.telegram.tgnet.TLRPC$ChatParticipant) r7
            int r7 = r7.user_id
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            org.telegram.tgnet.TLRPC$User r7 = r0.getUser(r7)
            org.telegram.messenger.MessagesController r0 = r4.getMessagesController()
            org.telegram.tgnet.TLRPC$ChatFull r1 = r4.chatInfo
            org.telegram.tgnet.TLRPC$ChatParticipants r1 = r1.participants
            java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r1 = r1.participants
            int r6 = r6.intValue()
            java.lang.Object r6 = r1.get(r6)
            org.telegram.tgnet.TLRPC$ChatParticipant r6 = (org.telegram.tgnet.TLRPC$ChatParticipant) r6
            int r6 = r6.user_id
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            org.telegram.tgnet.TLRPC$User r6 = r0.getUser(r6)
            r0 = 50000(0xCLASSNAME, float:7.0065E-41)
            r1 = -110(0xfffffffffffffvar_, float:NaN)
            r2 = 0
            if (r7 == 0) goto L_0x0059
            boolean r3 = r7.bot
            if (r3 == 0) goto L_0x004b
            r7 = -110(0xfffffffffffffvar_, float:NaN)
            goto L_0x005a
        L_0x004b:
            boolean r3 = r7.self
            if (r3 == 0) goto L_0x0052
            int r7 = r5 + r0
            goto L_0x005a
        L_0x0052:
            org.telegram.tgnet.TLRPC$UserStatus r7 = r7.status
            if (r7 == 0) goto L_0x0059
            int r7 = r7.expires
            goto L_0x005a
        L_0x0059:
            r7 = 0
        L_0x005a:
            if (r6 == 0) goto L_0x006f
            boolean r3 = r6.bot
            if (r3 == 0) goto L_0x0061
            goto L_0x0070
        L_0x0061:
            boolean r1 = r6.self
            if (r1 == 0) goto L_0x0068
            int r1 = r5 + r0
            goto L_0x0070
        L_0x0068:
            org.telegram.tgnet.TLRPC$UserStatus r5 = r6.status
            if (r5 == 0) goto L_0x006f
            int r1 = r5.expires
            goto L_0x0070
        L_0x006f:
            r1 = 0
        L_0x0070:
            r5 = -1
            r6 = 1
            if (r7 <= 0) goto L_0x007d
            if (r1 <= 0) goto L_0x007d
            if (r7 <= r1) goto L_0x0079
            return r6
        L_0x0079:
            if (r7 >= r1) goto L_0x007c
            return r5
        L_0x007c:
            return r2
        L_0x007d:
            if (r7 >= 0) goto L_0x0088
            if (r1 >= 0) goto L_0x0088
            if (r7 <= r1) goto L_0x0084
            return r6
        L_0x0084:
            if (r7 >= r1) goto L_0x0087
            return r5
        L_0x0087:
            return r2
        L_0x0088:
            if (r7 >= 0) goto L_0x008c
            if (r1 > 0) goto L_0x0090
        L_0x008c:
            if (r7 != 0) goto L_0x0091
            if (r1 == 0) goto L_0x0091
        L_0x0090:
            return r5
        L_0x0091:
            if (r1 >= 0) goto L_0x0095
            if (r7 > 0) goto L_0x0099
        L_0x0095:
            if (r1 != 0) goto L_0x009a
            if (r7 == 0) goto L_0x009a
        L_0x0099:
            return r6
        L_0x009a:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.lambda$updateOnlineCount$28$ProfileActivity(int, java.lang.Integer, java.lang.Integer):int");
    }

    public void setChatInfo(TLRPC$ChatFull tLRPC$ChatFull) {
        int i;
        this.chatInfo = tLRPC$ChatFull;
        if (!(tLRPC$ChatFull == null || (i = tLRPC$ChatFull.migrated_from_chat_id) == 0 || this.mergeDialogId != 0)) {
            this.mergeDialogId = (long) (-i);
            getMediaDataController().getMediaCounts(this.mergeDialogId, this.classGuid);
        }
        SharedMediaLayout sharedMediaLayout2 = this.sharedMediaLayout;
        if (sharedMediaLayout2 != null) {
            sharedMediaLayout2.setChatInfo(this.chatInfo);
        }
        ProfileGalleryView profileGalleryView = this.avatarsViewPager;
        if (profileGalleryView != null) {
            profileGalleryView.setChatInfo(this.chatInfo);
        }
        fetchUsersFromChannelInfo();
    }

    public void setUserInfo(TLRPC$UserFull tLRPC$UserFull) {
        this.userInfo = tLRPC$UserFull;
    }

    public boolean canSearchMembers() {
        return this.canSearchMembers;
    }

    private void fetchUsersFromChannelInfo() {
        TLRPC$Chat tLRPC$Chat = this.currentChat;
        if (tLRPC$Chat != null && tLRPC$Chat.megagroup) {
            TLRPC$ChatFull tLRPC$ChatFull = this.chatInfo;
            if ((tLRPC$ChatFull instanceof TLRPC$TL_channelFull) && tLRPC$ChatFull.participants != null) {
                for (int i = 0; i < this.chatInfo.participants.participants.size(); i++) {
                    TLRPC$ChatParticipant tLRPC$ChatParticipant = this.chatInfo.participants.participants.get(i);
                    this.participantsMap.put(tLRPC$ChatParticipant.user_id, tLRPC$ChatParticipant);
                }
            }
        }
    }

    private void kickUser(int i) {
        if (i != 0) {
            TLRPC$User user = getMessagesController().getUser(Integer.valueOf(i));
            getMessagesController().deleteUserFromChat(this.chat_id, user, this.chatInfo);
            if (this.currentChat != null && user != null && BulletinFactory.canShowBulletin(this)) {
                BulletinFactory.createRemoveFromChatBulletin(this, user.first_name, this.currentChat.title).show();
                return;
            }
            return;
        }
        NotificationCenter notificationCenter = getNotificationCenter();
        int i2 = NotificationCenter.closeChats;
        notificationCenter.removeObserver(this, i2);
        if (AndroidUtilities.isTablet()) {
            getNotificationCenter().postNotificationName(i2, Long.valueOf(-((long) this.chat_id)));
        } else {
            getNotificationCenter().postNotificationName(i2, new Object[0]);
        }
        getMessagesController().deleteUserFromChat(this.chat_id, getMessagesController().getUser(Integer.valueOf(getUserConfig().getClientUserId())), this.chatInfo);
        this.playProfileAnimation = 0;
        finishFragment();
    }

    public boolean isChat() {
        return this.chat_id != 0;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00bd, code lost:
        if ((r3 instanceof org.telegram.tgnet.TLRPC$TL_fileLocationToBeDeprecated) == false) goto L_0x00bf;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateRowsIds() {
        /*
            r9 = this;
            int r0 = r9.rowCount
            r1 = 0
            r9.rowCount = r1
            r2 = -1
            r9.setAvatarRow = r2
            r9.setAvatarSectionRow = r2
            r9.numberSectionRow = r2
            r9.numberRow = r2
            r9.setUsernameRow = r2
            r9.bioRow = r2
            r9.settingsSectionRow = r2
            r9.settingsSectionRow2 = r2
            r9.notificationRow = r2
            r9.languageRow = r2
            r9.privacyRow = r2
            r9.dataRow = r2
            r9.chatRow = r2
            r9.filtersRow = r2
            r9.devicesRow = r2
            r9.devicesSectionRow = r2
            r9.helpHeaderRow = r2
            r9.questionRow = r2
            r9.faqRow = r2
            r9.policyRow = r2
            r9.helpSectionCell = r2
            r9.debugHeaderRow = r2
            r9.sendLogsRow = r2
            r9.clearLogsRow = r2
            r9.switchBackendRow = r2
            r9.versionRow = r2
            r9.sendMessageRow = r2
            r9.reportRow = r2
            r9.emptyRow = r2
            r9.infoHeaderRow = r2
            r9.phoneRow = r2
            r9.userInfoRow = r2
            r9.locationRow = r2
            r9.channelInfoRow = r2
            r9.usernameRow = r2
            r9.settingsTimerRow = r2
            r9.settingsKeyRow = r2
            r9.notificationsDividerRow = r2
            r9.notificationsRow = r2
            r9.infoSectionRow = r2
            r9.secretSettingsSectionRow = r2
            r9.bottomPaddingRow = r2
            r9.membersHeaderRow = r2
            r9.membersStartRow = r2
            r9.membersEndRow = r2
            r9.addMemberRow = r2
            r9.subscribersRow = r2
            r9.administratorsRow = r2
            r9.blockedUsersRow = r2
            r9.membersSectionRow = r2
            r9.sharedMediaRow = r2
            r9.unblockRow = r2
            r9.joinRow = r2
            r9.lastSectionRow = r2
            org.telegram.ui.Components.SharedMediaLayout$SharedMediaPreloader r3 = r9.sharedMediaPreloader
            r4 = 1
            if (r3 == 0) goto L_0x0088
            int[] r3 = r3.getLastMediaCount()
            r5 = 0
        L_0x007c:
            int r6 = r3.length
            if (r5 >= r6) goto L_0x0088
            r6 = r3[r5]
            if (r6 <= 0) goto L_0x0085
            r3 = 1
            goto L_0x0089
        L_0x0085:
            int r5 = r5 + 1
            goto L_0x007c
        L_0x0088:
            r3 = 0
        L_0x0089:
            int r5 = r9.user_id
            if (r5 == 0) goto L_0x029b
            boolean r5 = org.telegram.messenger.LocaleController.isRTL
            if (r5 == 0) goto L_0x0099
            int r5 = r9.rowCount
            int r6 = r5 + 1
            r9.rowCount = r6
            r9.emptyRow = r5
        L_0x0099:
            org.telegram.messenger.MessagesController r5 = r9.getMessagesController()
            int r6 = r9.user_id
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            org.telegram.tgnet.TLRPC$User r5 = r5.getUser(r6)
            boolean r6 = org.telegram.messenger.UserObject.isUserSelf(r5)
            if (r6 == 0) goto L_0x0199
            org.telegram.tgnet.TLRPC$FileLocation r3 = r9.avatarBig
            if (r3 != 0) goto L_0x00d7
            org.telegram.tgnet.TLRPC$UserProfilePhoto r3 = r5.photo
            if (r3 == 0) goto L_0x00bf
            org.telegram.tgnet.TLRPC$FileLocation r3 = r3.photo_big
            boolean r4 = r3 instanceof org.telegram.tgnet.TLRPC$TL_fileLocation_layer97
            if (r4 != 0) goto L_0x00d7
            boolean r3 = r3 instanceof org.telegram.tgnet.TLRPC$TL_fileLocationToBeDeprecated
            if (r3 != 0) goto L_0x00d7
        L_0x00bf:
            org.telegram.ui.Components.ProfileGalleryView r3 = r9.avatarsViewPager
            if (r3 == 0) goto L_0x00c9
            int r3 = r3.getRealCount()
            if (r3 != 0) goto L_0x00d7
        L_0x00c9:
            int r3 = r9.rowCount
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.setAvatarRow = r3
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.setAvatarSectionRow = r4
        L_0x00d7:
            int r3 = r9.rowCount
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.numberSectionRow = r3
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.numberRow = r4
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.setUsernameRow = r3
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.bioRow = r4
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.settingsSectionRow = r3
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.settingsSectionRow2 = r4
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.notificationRow = r3
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.privacyRow = r4
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.dataRow = r3
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.chatRow = r4
            org.telegram.messenger.MessagesController r3 = r9.getMessagesController()
            boolean r3 = r3.filtersEnabled
            if (r3 != 0) goto L_0x0129
            org.telegram.messenger.MessagesController r3 = r9.getMessagesController()
            java.util.ArrayList<org.telegram.messenger.MessagesController$DialogFilter> r3 = r3.dialogFilters
            boolean r3 = r3.isEmpty()
            if (r3 != 0) goto L_0x0131
        L_0x0129:
            int r3 = r9.rowCount
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.filtersRow = r3
        L_0x0131:
            int r3 = r9.rowCount
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.devicesRow = r3
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.languageRow = r4
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.devicesSectionRow = r3
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.helpHeaderRow = r4
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.questionRow = r3
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.faqRow = r4
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.policyRow = r3
            boolean r3 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r3 != 0) goto L_0x0165
            boolean r3 = org.telegram.messenger.BuildVars.DEBUG_PRIVATE_VERSION
            if (r3 == 0) goto L_0x0171
        L_0x0165:
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.helpSectionCell = r4
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.debugHeaderRow = r3
        L_0x0171:
            boolean r3 = org.telegram.messenger.BuildVars.LOGS_ENABLED
            if (r3 == 0) goto L_0x0183
            int r3 = r9.rowCount
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.sendLogsRow = r3
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.clearLogsRow = r4
        L_0x0183:
            boolean r3 = org.telegram.messenger.BuildVars.DEBUG_PRIVATE_VERSION
            if (r3 == 0) goto L_0x018f
            int r3 = r9.rowCount
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.switchBackendRow = r3
        L_0x018f:
            int r3 = r9.rowCount
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.versionRow = r3
            goto L_0x04b1
        L_0x0199:
            org.telegram.tgnet.TLRPC$UserFull r6 = r9.userInfo
            if (r6 == 0) goto L_0x01a5
            java.lang.String r6 = r6.about
            boolean r6 = android.text.TextUtils.isEmpty(r6)
            if (r6 == 0) goto L_0x01af
        L_0x01a5:
            if (r5 == 0) goto L_0x01b1
            java.lang.String r6 = r5.username
            boolean r6 = android.text.TextUtils.isEmpty(r6)
            if (r6 != 0) goto L_0x01b1
        L_0x01af:
            r6 = 1
            goto L_0x01b2
        L_0x01b1:
            r6 = 0
        L_0x01b2:
            if (r5 == 0) goto L_0x01bd
            java.lang.String r7 = r5.phone
            boolean r7 = android.text.TextUtils.isEmpty(r7)
            if (r7 != 0) goto L_0x01bd
            goto L_0x01be
        L_0x01bd:
            r4 = 0
        L_0x01be:
            int r7 = r9.rowCount
            int r8 = r7 + 1
            r9.rowCount = r8
            r9.infoHeaderRow = r7
            boolean r7 = r9.isBot
            if (r7 != 0) goto L_0x01d4
            if (r4 != 0) goto L_0x01ce
            if (r6 != 0) goto L_0x01d4
        L_0x01ce:
            int r4 = r8 + 1
            r9.rowCount = r4
            r9.phoneRow = r8
        L_0x01d4:
            org.telegram.tgnet.TLRPC$UserFull r4 = r9.userInfo
            if (r4 == 0) goto L_0x01e8
            java.lang.String r4 = r4.about
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x01e8
            int r4 = r9.rowCount
            int r6 = r4 + 1
            r9.rowCount = r6
            r9.userInfoRow = r4
        L_0x01e8:
            if (r5 == 0) goto L_0x01fa
            java.lang.String r4 = r5.username
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x01fa
            int r4 = r9.rowCount
            int r6 = r4 + 1
            r9.rowCount = r6
            r9.usernameRow = r4
        L_0x01fa:
            int r4 = r9.phoneRow
            if (r4 != r2) goto L_0x0206
            int r4 = r9.userInfoRow
            if (r4 != r2) goto L_0x0206
            int r4 = r9.usernameRow
            if (r4 == r2) goto L_0x020e
        L_0x0206:
            int r4 = r9.rowCount
            int r6 = r4 + 1
            r9.rowCount = r6
            r9.notificationsDividerRow = r4
        L_0x020e:
            int r4 = r9.user_id
            org.telegram.messenger.UserConfig r6 = r9.getUserConfig()
            int r6 = r6.getClientUserId()
            if (r4 == r6) goto L_0x0222
            int r4 = r9.rowCount
            int r6 = r4 + 1
            r9.rowCount = r6
            r9.notificationsRow = r4
        L_0x0222:
            int r4 = r9.rowCount
            int r6 = r4 + 1
            r9.rowCount = r6
            r9.infoSectionRow = r4
            org.telegram.tgnet.TLRPC$EncryptedChat r4 = r9.currentEncryptedChat
            boolean r7 = r4 instanceof org.telegram.tgnet.TLRPC$TL_encryptedChat
            if (r7 == 0) goto L_0x0242
            int r7 = r6 + 1
            r9.rowCount = r7
            r9.settingsTimerRow = r6
            int r6 = r7 + 1
            r9.rowCount = r6
            r9.settingsKeyRow = r7
            int r7 = r6 + 1
            r9.rowCount = r7
            r9.secretSettingsSectionRow = r6
        L_0x0242:
            if (r5 == 0) goto L_0x0268
            boolean r6 = r9.isBot
            if (r6 != 0) goto L_0x0268
            if (r4 != 0) goto L_0x0268
            int r4 = r5.id
            org.telegram.messenger.UserConfig r5 = r9.getUserConfig()
            int r5 = r5.getClientUserId()
            if (r4 == r5) goto L_0x0268
            boolean r4 = r9.userBlocked
            if (r4 == 0) goto L_0x0268
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.unblockRow = r4
            int r4 = r5 + 1
            r9.rowCount = r4
            r9.lastSectionRow = r5
        L_0x0268:
            if (r3 != 0) goto L_0x0291
            org.telegram.tgnet.TLRPC$UserFull r3 = r9.userInfo
            if (r3 == 0) goto L_0x0273
            int r3 = r3.common_chats_count
            if (r3 == 0) goto L_0x0273
            goto L_0x0291
        L_0x0273:
            int r3 = r9.lastSectionRow
            if (r3 != r2) goto L_0x04b1
            boolean r3 = r9.needSendMessage
            if (r3 == 0) goto L_0x04b1
            int r3 = r9.rowCount
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.sendMessageRow = r3
            int r3 = r4 + 1
            r9.rowCount = r3
            r9.reportRow = r4
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.lastSectionRow = r3
            goto L_0x04b1
        L_0x0291:
            int r3 = r9.rowCount
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.sharedMediaRow = r3
            goto L_0x04b1
        L_0x029b:
            int r4 = r9.chat_id
            if (r4 == 0) goto L_0x04b1
            org.telegram.tgnet.TLRPC$ChatFull r4 = r9.chatInfo
            if (r4 == 0) goto L_0x02b3
            java.lang.String r4 = r4.about
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 == 0) goto L_0x02bd
            org.telegram.tgnet.TLRPC$ChatFull r4 = r9.chatInfo
            org.telegram.tgnet.TLRPC$ChannelLocation r4 = r4.location
            boolean r4 = r4 instanceof org.telegram.tgnet.TLRPC$TL_channelLocation
            if (r4 != 0) goto L_0x02bd
        L_0x02b3:
            org.telegram.tgnet.TLRPC$Chat r4 = r9.currentChat
            java.lang.String r4 = r4.username
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x031d
        L_0x02bd:
            boolean r4 = org.telegram.messenger.LocaleController.isRTL
            if (r4 == 0) goto L_0x02df
            org.telegram.tgnet.TLRPC$Chat r4 = r9.currentChat
            boolean r4 = org.telegram.messenger.ChatObject.isChannel(r4)
            if (r4 == 0) goto L_0x02df
            org.telegram.tgnet.TLRPC$ChatFull r4 = r9.chatInfo
            if (r4 == 0) goto L_0x02df
            org.telegram.tgnet.TLRPC$Chat r5 = r9.currentChat
            boolean r5 = r5.megagroup
            if (r5 != 0) goto L_0x02df
            int r4 = r4.linked_chat_id
            if (r4 == 0) goto L_0x02df
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.emptyRow = r4
        L_0x02df:
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.infoHeaderRow = r4
            org.telegram.tgnet.TLRPC$ChatFull r4 = r9.chatInfo
            if (r4 == 0) goto L_0x030b
            java.lang.String r4 = r4.about
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x02fb
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.channelInfoRow = r4
        L_0x02fb:
            org.telegram.tgnet.TLRPC$ChatFull r4 = r9.chatInfo
            org.telegram.tgnet.TLRPC$ChannelLocation r4 = r4.location
            boolean r4 = r4 instanceof org.telegram.tgnet.TLRPC$TL_channelLocation
            if (r4 == 0) goto L_0x030b
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.locationRow = r4
        L_0x030b:
            org.telegram.tgnet.TLRPC$Chat r4 = r9.currentChat
            java.lang.String r4 = r4.username
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x031d
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.usernameRow = r4
        L_0x031d:
            int r4 = r9.infoHeaderRow
            if (r4 == r2) goto L_0x0329
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.notificationsDividerRow = r4
        L_0x0329:
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.notificationsRow = r4
            int r4 = r5 + 1
            r9.rowCount = r4
            r9.infoSectionRow = r5
            org.telegram.tgnet.TLRPC$Chat r4 = r9.currentChat
            boolean r4 = org.telegram.messenger.ChatObject.isChannel(r4)
            if (r4 == 0) goto L_0x037b
            org.telegram.tgnet.TLRPC$Chat r4 = r9.currentChat
            boolean r5 = r4.megagroup
            if (r5 != 0) goto L_0x037b
            org.telegram.tgnet.TLRPC$ChatFull r5 = r9.chatInfo
            if (r5 == 0) goto L_0x037b
            boolean r4 = r4.creator
            if (r4 != 0) goto L_0x0351
            boolean r4 = r5.can_view_participants
            if (r4 == 0) goto L_0x037b
        L_0x0351:
            int r4 = r9.rowCount
            int r6 = r4 + 1
            r9.rowCount = r6
            r9.membersHeaderRow = r4
            int r4 = r6 + 1
            r9.rowCount = r4
            r9.subscribersRow = r6
            int r6 = r4 + 1
            r9.rowCount = r6
            r9.administratorsRow = r4
            int r4 = r5.banned_count
            if (r4 != 0) goto L_0x036d
            int r4 = r5.kicked_count
            if (r4 == 0) goto L_0x0373
        L_0x036d:
            int r4 = r6 + 1
            r9.rowCount = r4
            r9.blockedUsersRow = r6
        L_0x0373:
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.membersSectionRow = r4
        L_0x037b:
            org.telegram.tgnet.TLRPC$Chat r4 = r9.currentChat
            boolean r4 = org.telegram.messenger.ChatObject.isChannel(r4)
            r5 = 5
            r6 = 0
            if (r4 == 0) goto L_0x0430
            org.telegram.tgnet.TLRPC$ChatFull r4 = r9.chatInfo
            if (r4 == 0) goto L_0x0412
            org.telegram.tgnet.TLRPC$Chat r7 = r9.currentChat
            boolean r7 = r7.megagroup
            if (r7 == 0) goto L_0x0412
            org.telegram.tgnet.TLRPC$ChatParticipants r4 = r4.participants
            if (r4 == 0) goto L_0x0412
            java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r4 = r4.participants
            boolean r4 = r4.isEmpty()
            if (r4 != 0) goto L_0x0412
            org.telegram.tgnet.TLRPC$Chat r4 = r9.currentChat
            boolean r4 = org.telegram.messenger.ChatObject.isNotInChat(r4)
            if (r4 != 0) goto L_0x03bf
            org.telegram.tgnet.TLRPC$Chat r4 = r9.currentChat
            boolean r4 = org.telegram.messenger.ChatObject.canAddUsers(r4)
            if (r4 == 0) goto L_0x03bf
            org.telegram.tgnet.TLRPC$ChatFull r4 = r9.chatInfo
            int r4 = r4.participants_count
            org.telegram.messenger.MessagesController r7 = r9.getMessagesController()
            int r7 = r7.maxMegagroupCount
            if (r4 >= r7) goto L_0x03bf
            int r4 = r9.rowCount
            int r7 = r4 + 1
            r9.rowCount = r7
            r9.addMemberRow = r4
        L_0x03bf:
            org.telegram.tgnet.TLRPC$ChatFull r4 = r9.chatInfo
            org.telegram.tgnet.TLRPC$ChatParticipants r4 = r4.participants
            java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r4 = r4.participants
            int r4 = r4.size()
            if (r4 <= r5) goto L_0x03e6
            if (r3 != 0) goto L_0x03ce
            goto L_0x03e6
        L_0x03ce:
            int r4 = r9.addMemberRow
            if (r4 == r2) goto L_0x03da
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.membersSectionRow = r4
        L_0x03da:
            org.telegram.ui.Components.SharedMediaLayout r4 = r9.sharedMediaLayout
            if (r4 == 0) goto L_0x0412
            java.util.ArrayList<java.lang.Integer> r5 = r9.sortedUsers
            org.telegram.tgnet.TLRPC$ChatFull r6 = r9.chatInfo
            r4.setChatUsers(r5, r6)
            goto L_0x0412
        L_0x03e6:
            int r4 = r9.addMemberRow
            if (r4 != r2) goto L_0x03f2
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.membersHeaderRow = r4
        L_0x03f2:
            int r4 = r9.rowCount
            r9.membersStartRow = r4
            org.telegram.tgnet.TLRPC$ChatFull r5 = r9.chatInfo
            org.telegram.tgnet.TLRPC$ChatParticipants r5 = r5.participants
            java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r5 = r5.participants
            int r5 = r5.size()
            int r4 = r4 + r5
            r9.rowCount = r4
            r9.membersEndRow = r4
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.membersSectionRow = r4
            org.telegram.ui.Components.SharedMediaLayout r4 = r9.sharedMediaLayout
            if (r4 == 0) goto L_0x0412
            r4.setChatUsers(r6, r6)
        L_0x0412:
            int r4 = r9.lastSectionRow
            if (r4 != r2) goto L_0x04a7
            org.telegram.tgnet.TLRPC$Chat r4 = r9.currentChat
            boolean r5 = r4.left
            if (r5 == 0) goto L_0x04a7
            boolean r4 = r4.kicked
            if (r4 != 0) goto L_0x04a7
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.joinRow = r4
            int r4 = r5 + 1
            r9.rowCount = r4
            r9.lastSectionRow = r5
            goto L_0x04a7
        L_0x0430:
            org.telegram.tgnet.TLRPC$ChatFull r4 = r9.chatInfo
            if (r4 == 0) goto L_0x04a7
            org.telegram.tgnet.TLRPC$ChatParticipants r4 = r4.participants
            boolean r4 = r4 instanceof org.telegram.tgnet.TLRPC$TL_chatParticipantsForbidden
            if (r4 != 0) goto L_0x04a7
            org.telegram.tgnet.TLRPC$Chat r4 = r9.currentChat
            boolean r4 = org.telegram.messenger.ChatObject.canAddUsers(r4)
            if (r4 != 0) goto L_0x044c
            org.telegram.tgnet.TLRPC$Chat r4 = r9.currentChat
            org.telegram.tgnet.TLRPC$TL_chatBannedRights r4 = r4.default_banned_rights
            if (r4 == 0) goto L_0x044c
            boolean r4 = r4.invite_users
            if (r4 != 0) goto L_0x0454
        L_0x044c:
            int r4 = r9.rowCount
            int r7 = r4 + 1
            r9.rowCount = r7
            r9.addMemberRow = r4
        L_0x0454:
            org.telegram.tgnet.TLRPC$ChatFull r4 = r9.chatInfo
            org.telegram.tgnet.TLRPC$ChatParticipants r4 = r4.participants
            java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r4 = r4.participants
            int r4 = r4.size()
            if (r4 <= r5) goto L_0x047b
            if (r3 != 0) goto L_0x0463
            goto L_0x047b
        L_0x0463:
            int r4 = r9.addMemberRow
            if (r4 == r2) goto L_0x046f
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.membersSectionRow = r4
        L_0x046f:
            org.telegram.ui.Components.SharedMediaLayout r4 = r9.sharedMediaLayout
            if (r4 == 0) goto L_0x04a7
            java.util.ArrayList<java.lang.Integer> r5 = r9.sortedUsers
            org.telegram.tgnet.TLRPC$ChatFull r6 = r9.chatInfo
            r4.setChatUsers(r5, r6)
            goto L_0x04a7
        L_0x047b:
            int r4 = r9.addMemberRow
            if (r4 != r2) goto L_0x0487
            int r4 = r9.rowCount
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.membersHeaderRow = r4
        L_0x0487:
            int r4 = r9.rowCount
            r9.membersStartRow = r4
            org.telegram.tgnet.TLRPC$ChatFull r5 = r9.chatInfo
            org.telegram.tgnet.TLRPC$ChatParticipants r5 = r5.participants
            java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r5 = r5.participants
            int r5 = r5.size()
            int r4 = r4 + r5
            r9.rowCount = r4
            r9.membersEndRow = r4
            int r5 = r4 + 1
            r9.rowCount = r5
            r9.membersSectionRow = r4
            org.telegram.ui.Components.SharedMediaLayout r4 = r9.sharedMediaLayout
            if (r4 == 0) goto L_0x04a7
            r4.setChatUsers(r6, r6)
        L_0x04a7:
            if (r3 == 0) goto L_0x04b1
            int r3 = r9.rowCount
            int r4 = r3 + 1
            r9.rowCount = r4
            r9.sharedMediaRow = r3
        L_0x04b1:
            int r3 = r9.sharedMediaRow
            if (r3 != r2) goto L_0x04bd
            int r2 = r9.rowCount
            int r3 = r2 + 1
            r9.rowCount = r3
            r9.bottomPaddingRow = r2
        L_0x04bd:
            org.telegram.ui.ActionBar.ActionBar r2 = r9.actionBar
            if (r2 == 0) goto L_0x04d3
            int r2 = org.telegram.ui.ActionBar.ActionBar.getCurrentActionBarHeight()
            org.telegram.ui.ActionBar.ActionBar r3 = r9.actionBar
            boolean r3 = r3.getOccupyStatusBar()
            if (r3 == 0) goto L_0x04d0
            int r3 = org.telegram.messenger.AndroidUtilities.statusBarHeight
            goto L_0x04d1
        L_0x04d0:
            r3 = 0
        L_0x04d1:
            int r2 = r2 + r3
            goto L_0x04d4
        L_0x04d3:
            r2 = 0
        L_0x04d4:
            org.telegram.ui.Components.RecyclerListView r3 = r9.listView
            if (r3 == 0) goto L_0x04f0
            int r3 = r9.rowCount
            if (r0 > r3) goto L_0x04f0
            int r0 = r9.listContentHeight
            if (r0 == 0) goto L_0x04f2
            int r0 = r0 + r2
            r2 = 1118830592(0x42b00000, float:88.0)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            int r0 = r0 + r2
            org.telegram.ui.Components.RecyclerListView r2 = r9.listView
            int r2 = r2.getMeasuredHeight()
            if (r0 >= r2) goto L_0x04f2
        L_0x04f0:
            r9.lastMeasuredContentWidth = r1
        L_0x04f2:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.updateRowsIds():void");
    }

    private Drawable getScamDrawable() {
        if (this.scamDrawable == null) {
            ScamDrawable scamDrawable2 = new ScamDrawable(11);
            this.scamDrawable = scamDrawable2;
            scamDrawable2.setColor(Theme.getColor("avatar_subtitleInProfileBlue"));
        }
        return this.scamDrawable;
    }

    private Drawable getLockIconDrawable() {
        if (this.lockIconDrawable == null) {
            this.lockIconDrawable = Theme.chat_lockIconDrawable.getConstantState().newDrawable().mutate();
        }
        return this.lockIconDrawable;
    }

    private Drawable getVerifiedCrossfadeDrawable() {
        if (this.verifiedCrossfadeDrawable == null) {
            this.verifiedDrawable = Theme.profile_verifiedDrawable.getConstantState().newDrawable().mutate();
            this.verifiedCheckDrawable = Theme.profile_verifiedCheckDrawable.getConstantState().newDrawable().mutate();
            this.verifiedCrossfadeDrawable = new CrossfadeDrawable(new CombinedDrawable(this.verifiedDrawable, this.verifiedCheckDrawable), ContextCompat.getDrawable(getParentActivity(), NUM));
        }
        return this.verifiedCrossfadeDrawable;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0209  */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x03df  */
    /* JADX WARNING: Removed duplicated region for block: B:219:0x047e  */
    /* JADX WARNING: Removed duplicated region for block: B:224:0x048c A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0589  */
    /* JADX WARNING: Removed duplicated region for block: B:260:0x0590  */
    /* JADX WARNING: Removed duplicated region for block: B:261:0x0593  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x05d8  */
    /* JADX WARNING: Removed duplicated region for block: B:274:0x05db  */
    /* JADX WARNING: Removed duplicated region for block: B:277:0x05e0  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x01df  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01e4  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x01e7  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateProfileData() {
        /*
            r24 = this;
            r0 = r24
            android.widget.FrameLayout r1 = r0.avatarContainer
            if (r1 == 0) goto L_0x0606
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r0.nameTextView
            if (r1 != 0) goto L_0x000c
            goto L_0x0606
        L_0x000c:
            org.telegram.tgnet.ConnectionsManager r1 = r24.getConnectionsManager()
            int r1 = r1.getConnectionState()
            r2 = 2
            r4 = 1
            if (r1 != r2) goto L_0x0022
            r1 = 2131627808(0x7f0e0var_, float:1.888289E38)
            java.lang.String r5 = "WaitingForNetwork"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r5, r1)
            goto L_0x0049
        L_0x0022:
            if (r1 != r4) goto L_0x002e
            r1 = 2131624906(0x7f0e03ca, float:1.8877005E38)
            java.lang.String r5 = "Connecting"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r5, r1)
            goto L_0x0049
        L_0x002e:
            r5 = 5
            if (r1 != r5) goto L_0x003b
            r1 = 2131627530(0x7f0e0e0a, float:1.8882327E38)
            java.lang.String r5 = "Updating"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r5, r1)
            goto L_0x0049
        L_0x003b:
            r5 = 4
            if (r1 != r5) goto L_0x0048
            r1 = 2131624908(0x7f0e03cc, float:1.887701E38)
            java.lang.String r5 = "ConnectingToProxy"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r5, r1)
            goto L_0x0049
        L_0x0048:
            r1 = 0
        L_0x0049:
            int r5 = r0.user_id
            java.lang.String r6 = "g"
            r7 = 0
            if (r5 == 0) goto L_0x023e
            org.telegram.messenger.MessagesController r5 = r24.getMessagesController()
            int r8 = r0.user_id
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            org.telegram.tgnet.TLRPC$User r5 = r5.getUser(r8)
            org.telegram.tgnet.TLRPC$UserProfilePhoto r8 = r5.photo
            if (r8 == 0) goto L_0x0065
            org.telegram.tgnet.TLRPC$FileLocation r8 = r8.photo_big
            goto L_0x0066
        L_0x0065:
            r8 = 0
        L_0x0066:
            org.telegram.ui.Components.AvatarDrawable r9 = r0.avatarDrawable
            r9.setInfo((org.telegram.tgnet.TLRPC$User) r5)
            org.telegram.messenger.ImageLocation r15 = org.telegram.messenger.ImageLocation.getForUser(r5, r4)
            org.telegram.messenger.ImageLocation r14 = org.telegram.messenger.ImageLocation.getForUser(r5, r7)
            org.telegram.ui.Components.ProfileGalleryView r9 = r0.avatarsViewPager
            org.telegram.messenger.ImageLocation r10 = r9.getCurrentVideoLocation(r14, r15)
            org.telegram.ui.Components.ProfileGalleryView r9 = r0.avatarsViewPager
            r9.initIfEmpty(r15, r14)
            if (r10 == 0) goto L_0x0086
            int r9 = r10.imageType
            if (r9 != r2) goto L_0x0086
            r11 = r6
            goto L_0x0087
        L_0x0086:
            r11 = 0
        L_0x0087:
            org.telegram.tgnet.TLRPC$FileLocation r6 = r0.avatarBig
            if (r6 != 0) goto L_0x009b
            org.telegram.ui.ProfileActivity$AvatarImageView r9 = r0.avatarImage
            org.telegram.ui.Components.AvatarDrawable r6 = r0.avatarDrawable
            java.lang.String r13 = "50_50"
            r12 = r14
            r16 = r14
            r14 = r6
            r6 = r15
            r15 = r5
            r9.setImage((org.telegram.messenger.ImageLocation) r10, (java.lang.String) r11, (org.telegram.messenger.ImageLocation) r12, (java.lang.String) r13, (android.graphics.drawable.Drawable) r14, (java.lang.Object) r15)
            goto L_0x009e
        L_0x009b:
            r16 = r14
            r6 = r15
        L_0x009e:
            r9 = -1
            if (r16 == 0) goto L_0x00a5
            int r10 = r0.setAvatarRow
            if (r10 != r9) goto L_0x00ab
        L_0x00a5:
            if (r16 != 0) goto L_0x00b1
            int r10 = r0.setAvatarRow
            if (r10 != r9) goto L_0x00b1
        L_0x00ab:
            r24.updateListAnimated()
            r0.needLayout(r4)
        L_0x00b1:
            org.telegram.messenger.FileLoader r9 = r24.getFileLoader()
            r12 = 0
            r13 = 0
            r14 = 1
            r10 = r6
            r11 = r5
            r9.loadFile(r10, r11, r12, r13, r14)
            java.lang.String r6 = org.telegram.messenger.UserObject.getUserName(r5)
            int r9 = r5.id
            org.telegram.messenger.UserConfig r10 = r24.getUserConfig()
            int r10 = r10.getClientUserId()
            if (r9 != r10) goto L_0x00d8
            r9 = 2131626307(0x7f0e0943, float:1.8879846E38)
            java.lang.String r10 = "Online"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            goto L_0x0145
        L_0x00d8:
            int r9 = r5.id
            r10 = 333000(0x514c8, float:4.66632E-40)
            if (r9 == r10) goto L_0x013c
            r10 = 777000(0xbdb28, float:1.088809E-39)
            if (r9 == r10) goto L_0x013c
            r10 = 42777(0xa719, float:5.9943E-41)
            if (r9 != r10) goto L_0x00ea
            goto L_0x013c
        L_0x00ea:
            boolean r9 = org.telegram.messenger.MessagesController.isSupportUser(r5)
            if (r9 == 0) goto L_0x00fa
            r9 = 2131627337(0x7f0e0d49, float:1.8881936E38)
            java.lang.String r10 = "SupportStatus"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            goto L_0x0145
        L_0x00fa:
            boolean r9 = r0.isBot
            if (r9 == 0) goto L_0x0108
            r9 = 2131624529(0x7f0e0251, float:1.887624E38)
            java.lang.String r10 = "Bot"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            goto L_0x0145
        L_0x0108:
            boolean[] r9 = r0.isOnline
            r9[r7] = r7
            int r10 = r0.currentAccount
            java.lang.String r9 = org.telegram.messenger.LocaleController.formatUserStatus(r10, r5, r9)
            org.telegram.ui.ActionBar.SimpleTextView[] r10 = r0.onlineTextView
            r11 = r10[r4]
            if (r11 == 0) goto L_0x0145
            boolean r11 = r0.mediaHeaderVisible
            if (r11 != 0) goto L_0x0145
            boolean[] r11 = r0.isOnline
            boolean r11 = r11[r7]
            if (r11 == 0) goto L_0x0125
            java.lang.String r11 = "profile_status"
            goto L_0x0127
        L_0x0125:
            java.lang.String r11 = "avatar_subtitleInProfileBlue"
        L_0x0127:
            r10 = r10[r4]
            r10.setTag(r11)
            boolean r10 = r0.isPulledDown
            if (r10 != 0) goto L_0x0145
            org.telegram.ui.ActionBar.SimpleTextView[] r10 = r0.onlineTextView
            r10 = r10[r4]
            int r11 = org.telegram.ui.ActionBar.Theme.getColor(r11)
            r10.setTextColor(r11)
            goto L_0x0145
        L_0x013c:
            r9 = 2131627125(0x7f0e0CLASSNAME, float:1.8881506E38)
            java.lang.String r10 = "ServiceNotifications"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
        L_0x0145:
            r10 = 0
        L_0x0146:
            if (r10 >= r2) goto L_0x022e
            org.telegram.ui.ActionBar.SimpleTextView[] r11 = r0.nameTextView
            r11 = r11[r10]
            if (r11 != 0) goto L_0x0150
            goto L_0x022a
        L_0x0150:
            if (r10 != 0) goto L_0x01c1
            int r11 = r5.id
            org.telegram.messenger.UserConfig r12 = r24.getUserConfig()
            int r12 = r12.getClientUserId()
            if (r11 == r12) goto L_0x01c1
            int r11 = r5.id
            int r12 = r11 / 1000
            r13 = 777(0x309, float:1.089E-42)
            if (r12 == r13) goto L_0x01c1
            int r11 = r11 / 1000
            r12 = 333(0x14d, float:4.67E-43)
            if (r11 == r12) goto L_0x01c1
            java.lang.String r11 = r5.phone
            if (r11 == 0) goto L_0x01c1
            int r11 = r11.length()
            if (r11 == 0) goto L_0x01c1
            org.telegram.messenger.ContactsController r11 = r24.getContactsController()
            j$.util.concurrent.ConcurrentHashMap<java.lang.Integer, org.telegram.tgnet.TLRPC$TL_contact> r11 = r11.contactsDict
            int r12 = r5.id
            java.lang.Integer r12 = java.lang.Integer.valueOf(r12)
            java.lang.Object r11 = r11.get(r12)
            if (r11 != 0) goto L_0x01c1
            org.telegram.messenger.ContactsController r11 = r24.getContactsController()
            j$.util.concurrent.ConcurrentHashMap<java.lang.Integer, org.telegram.tgnet.TLRPC$TL_contact> r11 = r11.contactsDict
            int r11 = r11.size()
            if (r11 != 0) goto L_0x019e
            org.telegram.messenger.ContactsController r11 = r24.getContactsController()
            boolean r11 = r11.isLoadingContacts()
            if (r11 != 0) goto L_0x01c1
        L_0x019e:
            org.telegram.PhoneFormat.PhoneFormat r11 = org.telegram.PhoneFormat.PhoneFormat.getInstance()
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = "+"
            r12.append(r13)
            java.lang.String r13 = r5.phone
            r12.append(r13)
            java.lang.String r12 = r12.toString()
            java.lang.String r11 = r11.format(r12)
            org.telegram.ui.ActionBar.SimpleTextView[] r12 = r0.nameTextView
            r12 = r12[r10]
            r12.setText(r11)
            goto L_0x01c8
        L_0x01c1:
            org.telegram.ui.ActionBar.SimpleTextView[] r11 = r0.nameTextView
            r11 = r11[r10]
            r11.setText(r6)
        L_0x01c8:
            if (r10 != 0) goto L_0x01d4
            if (r1 == 0) goto L_0x01d4
            org.telegram.ui.ActionBar.SimpleTextView[] r11 = r0.onlineTextView
            r11 = r11[r10]
            r11.setText(r1)
            goto L_0x01db
        L_0x01d4:
            org.telegram.ui.ActionBar.SimpleTextView[] r11 = r0.onlineTextView
            r11 = r11[r10]
            r11.setText(r9)
        L_0x01db:
            org.telegram.tgnet.TLRPC$EncryptedChat r11 = r0.currentEncryptedChat
            if (r11 == 0) goto L_0x01e4
            android.graphics.drawable.Drawable r11 = r24.getLockIconDrawable()
            goto L_0x01e5
        L_0x01e4:
            r11 = 0
        L_0x01e5:
            if (r10 != 0) goto L_0x0209
            boolean r12 = r5.scam
            if (r12 == 0) goto L_0x01f0
            android.graphics.drawable.Drawable r12 = r24.getScamDrawable()
            goto L_0x021c
        L_0x01f0:
            org.telegram.messenger.MessagesController r12 = r24.getMessagesController()
            long r13 = r0.dialog_id
            r15 = 0
            int r17 = (r13 > r15 ? 1 : (r13 == r15 ? 0 : -1))
            if (r17 == 0) goto L_0x01fd
            goto L_0x0200
        L_0x01fd:
            int r13 = r0.user_id
            long r13 = (long) r13
        L_0x0200:
            boolean r12 = r12.isDialogMuted(r13)
            if (r12 == 0) goto L_0x021b
            android.graphics.drawable.Drawable r12 = org.telegram.ui.ActionBar.Theme.chat_muteIconDrawable
            goto L_0x021c
        L_0x0209:
            boolean r12 = r5.scam
            if (r12 == 0) goto L_0x0212
            android.graphics.drawable.Drawable r12 = r24.getScamDrawable()
            goto L_0x021c
        L_0x0212:
            boolean r12 = r5.verified
            if (r12 == 0) goto L_0x021b
            android.graphics.drawable.Drawable r12 = r24.getVerifiedCrossfadeDrawable()
            goto L_0x021c
        L_0x021b:
            r12 = 0
        L_0x021c:
            org.telegram.ui.ActionBar.SimpleTextView[] r13 = r0.nameTextView
            r13 = r13[r10]
            r13.setLeftDrawable((android.graphics.drawable.Drawable) r11)
            org.telegram.ui.ActionBar.SimpleTextView[] r11 = r0.nameTextView
            r11 = r11[r10]
            r11.setRightDrawable((android.graphics.drawable.Drawable) r12)
        L_0x022a:
            int r10 = r10 + 1
            goto L_0x0146
        L_0x022e:
            org.telegram.ui.ProfileActivity$AvatarImageView r1 = r0.avatarImage
            org.telegram.messenger.ImageReceiver r1 = r1.getImageReceiver()
            boolean r2 = org.telegram.ui.PhotoViewer.isShowingImage((org.telegram.tgnet.TLRPC$FileLocation) r8)
            r2 = r2 ^ r4
            r1.setVisible(r2, r7)
            goto L_0x0606
        L_0x023e:
            int r5 = r0.chat_id
            if (r5 == 0) goto L_0x0606
            org.telegram.messenger.MessagesController r5 = r24.getMessagesController()
            int r8 = r0.chat_id
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            org.telegram.tgnet.TLRPC$Chat r5 = r5.getChat(r8)
            if (r5 == 0) goto L_0x0255
            r0.currentChat = r5
            goto L_0x0257
        L_0x0255:
            org.telegram.tgnet.TLRPC$Chat r5 = r0.currentChat
        L_0x0257:
            boolean r8 = org.telegram.messenger.ChatObject.isChannel(r5)
            java.lang.String r10 = "MegaPublic"
            java.lang.String r12 = "MegaPrivate"
            java.lang.String r14 = "MegaLocation"
            java.lang.String r15 = "OnlineCount"
            java.lang.String r3 = "%s, %s"
            java.lang.String r11 = "Subscribers"
            java.lang.String r9 = "Members"
            if (r8 == 0) goto L_0x0389
            org.telegram.tgnet.TLRPC$ChatFull r8 = r0.chatInfo
            if (r8 == 0) goto L_0x0353
            org.telegram.tgnet.TLRPC$Chat r13 = r0.currentChat
            boolean r7 = r13.megagroup
            if (r7 != 0) goto L_0x0287
            int r7 = r8.participants_count
            if (r7 == 0) goto L_0x0353
            boolean r7 = org.telegram.messenger.ChatObject.hasAdminRights(r13)
            if (r7 != 0) goto L_0x0353
            org.telegram.tgnet.TLRPC$ChatFull r7 = r0.chatInfo
            boolean r7 = r7.can_view_participants
            if (r7 == 0) goto L_0x0287
            goto L_0x0353
        L_0x0287:
            org.telegram.tgnet.TLRPC$Chat r7 = r0.currentChat
            boolean r7 = r7.megagroup
            if (r7 == 0) goto L_0x031f
            int r7 = r0.onlineCount
            if (r7 <= r4) goto L_0x02d8
            org.telegram.tgnet.TLRPC$ChatFull r7 = r0.chatInfo
            int r7 = r7.participants_count
            if (r7 == 0) goto L_0x02d8
            java.lang.Object[] r8 = new java.lang.Object[r2]
            java.lang.String r7 = org.telegram.messenger.LocaleController.formatPluralString(r9, r7)
            r13 = 0
            r8[r13] = r7
            int r7 = r0.onlineCount
            org.telegram.tgnet.TLRPC$ChatFull r13 = r0.chatInfo
            int r13 = r13.participants_count
            int r7 = java.lang.Math.min(r7, r13)
            java.lang.String r7 = org.telegram.messenger.LocaleController.formatPluralString(r15, r7)
            r8[r4] = r7
            java.lang.String r7 = java.lang.String.format(r3, r8)
            java.lang.Object[] r8 = new java.lang.Object[r2]
            org.telegram.tgnet.TLRPC$ChatFull r13 = r0.chatInfo
            int r13 = r13.participants_count
            java.lang.String r13 = org.telegram.messenger.LocaleController.formatPluralStringComma(r9, r13)
            r19 = 0
            r8[r19] = r13
            int r13 = r0.onlineCount
            org.telegram.tgnet.TLRPC$ChatFull r2 = r0.chatInfo
            int r2 = r2.participants_count
            int r2 = java.lang.Math.min(r13, r2)
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralStringComma(r15, r2)
            r8[r4] = r2
            java.lang.String r2 = java.lang.String.format(r3, r8)
            goto L_0x03da
        L_0x02d8:
            org.telegram.tgnet.TLRPC$ChatFull r2 = r0.chatInfo
            int r2 = r2.participants_count
            if (r2 != 0) goto L_0x0311
            boolean r2 = r5.has_geo
            if (r2 == 0) goto L_0x02ef
            r2 = 2131625894(0x7f0e07a6, float:1.8879009E38)
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r14, r2)
            java.lang.String r7 = r3.toLowerCase()
            goto L_0x0398
        L_0x02ef:
            java.lang.String r2 = r5.username
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 != 0) goto L_0x0304
            r2 = 2131625898(0x7f0e07aa, float:1.8879017E38)
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r10, r2)
            java.lang.String r7 = r3.toLowerCase()
            goto L_0x0398
        L_0x0304:
            r2 = 2131625895(0x7f0e07a7, float:1.887901E38)
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r12, r2)
            java.lang.String r7 = r3.toLowerCase()
            goto L_0x0398
        L_0x0311:
            java.lang.String r7 = org.telegram.messenger.LocaleController.formatPluralString(r9, r2)
            org.telegram.tgnet.TLRPC$ChatFull r2 = r0.chatInfo
            int r2 = r2.participants_count
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralStringComma(r9, r2)
            goto L_0x03da
        L_0x031f:
            int[] r2 = new int[r4]
            org.telegram.tgnet.TLRPC$ChatFull r3 = r0.chatInfo
            int r3 = r3.participants_count
            org.telegram.messenger.LocaleController.formatShortNumber(r3, r2)
            org.telegram.tgnet.TLRPC$Chat r2 = r0.currentChat
            boolean r2 = r2.megagroup
            if (r2 == 0) goto L_0x033f
            org.telegram.tgnet.TLRPC$ChatFull r2 = r0.chatInfo
            int r2 = r2.participants_count
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r9, r2)
            org.telegram.tgnet.TLRPC$ChatFull r3 = r0.chatInfo
            int r3 = r3.participants_count
            java.lang.String r3 = org.telegram.messenger.LocaleController.formatPluralStringComma(r9, r3)
            goto L_0x034f
        L_0x033f:
            org.telegram.tgnet.TLRPC$ChatFull r2 = r0.chatInfo
            int r2 = r2.participants_count
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r11, r2)
            org.telegram.tgnet.TLRPC$ChatFull r3 = r0.chatInfo
            int r3 = r3.participants_count
            java.lang.String r3 = org.telegram.messenger.LocaleController.formatPluralStringComma(r11, r3)
        L_0x034f:
            r7 = r2
            r2 = r3
            goto L_0x03da
        L_0x0353:
            org.telegram.tgnet.TLRPC$Chat r2 = r0.currentChat
            boolean r2 = r2.megagroup
            if (r2 == 0) goto L_0x0367
            r2 = 2131625794(0x7f0e0742, float:1.8878806E38)
            java.lang.String r3 = "Loading"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.String r7 = r2.toLowerCase()
            goto L_0x0398
        L_0x0367:
            int r2 = r5.flags
            r2 = r2 & 64
            if (r2 == 0) goto L_0x037b
            r2 = 2131624695(0x7f0e02f7, float:1.8876577E38)
            java.lang.String r3 = "ChannelPublic"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.String r7 = r2.toLowerCase()
            goto L_0x0398
        L_0x037b:
            r2 = 2131624692(0x7f0e02f4, float:1.887657E38)
            java.lang.String r3 = "ChannelPrivate"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.String r7 = r2.toLowerCase()
            goto L_0x0398
        L_0x0389:
            boolean r2 = org.telegram.messenger.ChatObject.isKickedFromChat(r5)
            if (r2 == 0) goto L_0x039a
            r2 = 2131627847(0x7f0e0var_, float:1.888297E38)
            java.lang.String r3 = "YouWereKicked"
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r3, r2)
        L_0x0398:
            r2 = r7
            goto L_0x03da
        L_0x039a:
            boolean r2 = org.telegram.messenger.ChatObject.isLeftFromChat(r5)
            if (r2 == 0) goto L_0x03aa
            r2 = 2131627846(0x7f0e0var_, float:1.8882968E38)
            java.lang.String r3 = "YouLeft"
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r3, r2)
            goto L_0x0398
        L_0x03aa:
            int r2 = r5.participants_count
            org.telegram.tgnet.TLRPC$ChatFull r7 = r0.chatInfo
            if (r7 == 0) goto L_0x03b8
            org.telegram.tgnet.TLRPC$ChatParticipants r2 = r7.participants
            java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r2 = r2.participants
            int r2 = r2.size()
        L_0x03b8:
            if (r2 == 0) goto L_0x03d5
            int r7 = r0.onlineCount
            if (r7 <= r4) goto L_0x03d5
            r7 = 2
            java.lang.Object[] r8 = new java.lang.Object[r7]
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r9, r2)
            r7 = 0
            r8[r7] = r2
            int r2 = r0.onlineCount
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r15, r2)
            r8[r4] = r2
            java.lang.String r7 = java.lang.String.format(r3, r8)
            goto L_0x0398
        L_0x03d5:
            java.lang.String r7 = org.telegram.messenger.LocaleController.formatPluralString(r9, r2)
            goto L_0x0398
        L_0x03da:
            r3 = 0
            r8 = 0
        L_0x03dc:
            r13 = 2
            if (r3 >= r13) goto L_0x0584
            org.telegram.ui.ActionBar.SimpleTextView[] r13 = r0.nameTextView
            r15 = r13[r3]
            if (r15 != 0) goto L_0x03f2
            r22 = r1
            r23 = r2
            r20 = r6
            r21 = r7
        L_0x03ed:
            r17 = 2131625895(0x7f0e07a7, float:1.887901E38)
            goto L_0x0577
        L_0x03f2:
            java.lang.String r15 = r5.title
            if (r15 == 0) goto L_0x03ff
            r13 = r13[r3]
            boolean r13 = r13.setText(r15)
            if (r13 == 0) goto L_0x03ff
            r8 = 1
        L_0x03ff:
            org.telegram.ui.ActionBar.SimpleTextView[] r13 = r0.nameTextView
            r13 = r13[r3]
            r15 = 0
            r13.setLeftDrawable((android.graphics.drawable.Drawable) r15)
            if (r3 == 0) goto L_0x0432
            boolean r13 = r5.scam
            if (r13 == 0) goto L_0x0419
            org.telegram.ui.ActionBar.SimpleTextView[] r13 = r0.nameTextView
            r13 = r13[r3]
            android.graphics.drawable.Drawable r15 = r24.getScamDrawable()
            r13.setRightDrawable((android.graphics.drawable.Drawable) r15)
            goto L_0x0442
        L_0x0419:
            boolean r13 = r5.verified
            if (r13 == 0) goto L_0x0429
            org.telegram.ui.ActionBar.SimpleTextView[] r13 = r0.nameTextView
            r13 = r13[r3]
            android.graphics.drawable.Drawable r15 = r24.getVerifiedCrossfadeDrawable()
            r13.setRightDrawable((android.graphics.drawable.Drawable) r15)
            goto L_0x0442
        L_0x0429:
            org.telegram.ui.ActionBar.SimpleTextView[] r13 = r0.nameTextView
            r13 = r13[r3]
            r15 = 0
            r13.setRightDrawable((android.graphics.drawable.Drawable) r15)
            goto L_0x0442
        L_0x0432:
            r15 = 0
            boolean r13 = r5.scam
            if (r13 == 0) goto L_0x0447
            org.telegram.ui.ActionBar.SimpleTextView[] r13 = r0.nameTextView
            r13 = r13[r3]
            android.graphics.drawable.Drawable r15 = r24.getScamDrawable()
            r13.setRightDrawable((android.graphics.drawable.Drawable) r15)
        L_0x0442:
            r20 = r6
            r21 = r7
            goto L_0x0464
        L_0x0447:
            org.telegram.ui.ActionBar.SimpleTextView[] r13 = r0.nameTextView
            r13 = r13[r3]
            org.telegram.messenger.MessagesController r15 = r24.getMessagesController()
            int r4 = r0.chat_id
            int r4 = -r4
            r20 = r6
            r21 = r7
            long r6 = (long) r4
            boolean r4 = r15.isDialogMuted(r6)
            if (r4 == 0) goto L_0x0460
            android.graphics.drawable.Drawable r15 = org.telegram.ui.ActionBar.Theme.chat_muteIconDrawable
            goto L_0x0461
        L_0x0460:
            r15 = 0
        L_0x0461:
            r13.setRightDrawable((android.graphics.drawable.Drawable) r15)
        L_0x0464:
            if (r3 != 0) goto L_0x0470
            if (r1 == 0) goto L_0x0470
            org.telegram.ui.ActionBar.SimpleTextView[] r4 = r0.onlineTextView
            r4 = r4[r3]
            r4.setText(r1)
            goto L_0x04d1
        L_0x0470:
            org.telegram.tgnet.TLRPC$Chat r4 = r0.currentChat
            boolean r6 = r4.megagroup
            if (r6 == 0) goto L_0x048c
            org.telegram.tgnet.TLRPC$ChatFull r6 = r0.chatInfo
            if (r6 == 0) goto L_0x048c
            int r6 = r0.onlineCount
            if (r6 <= 0) goto L_0x048c
            org.telegram.ui.ActionBar.SimpleTextView[] r4 = r0.onlineTextView
            r4 = r4[r3]
            if (r3 != 0) goto L_0x0487
            r6 = r21
            goto L_0x0488
        L_0x0487:
            r6 = r2
        L_0x0488:
            r4.setText(r6)
            goto L_0x04d1
        L_0x048c:
            if (r3 != 0) goto L_0x0562
            boolean r4 = org.telegram.messenger.ChatObject.isChannel(r4)
            if (r4 == 0) goto L_0x0562
            org.telegram.tgnet.TLRPC$ChatFull r4 = r0.chatInfo
            if (r4 == 0) goto L_0x0562
            int r4 = r4.participants_count
            if (r4 == 0) goto L_0x0562
            org.telegram.tgnet.TLRPC$Chat r6 = r0.currentChat
            boolean r7 = r6.megagroup
            if (r7 != 0) goto L_0x04a6
            boolean r6 = r6.broadcast
            if (r6 == 0) goto L_0x0562
        L_0x04a6:
            r6 = 1
            int[] r7 = new int[r6]
            java.lang.String r4 = org.telegram.messenger.LocaleController.formatShortNumber(r4, r7)
            org.telegram.tgnet.TLRPC$Chat r6 = r0.currentChat
            boolean r6 = r6.megagroup
            java.lang.String r13 = "%d"
            if (r6 == 0) goto L_0x0538
            org.telegram.tgnet.TLRPC$ChatFull r6 = r0.chatInfo
            int r6 = r6.participants_count
            if (r6 != 0) goto L_0x050b
            boolean r4 = r5.has_geo
            if (r4 == 0) goto L_0x04d7
            org.telegram.ui.ActionBar.SimpleTextView[] r4 = r0.onlineTextView
            r4 = r4[r3]
            r6 = 2131625894(0x7f0e07a6, float:1.8879009E38)
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r14, r6)
            java.lang.String r7 = r7.toLowerCase()
            r4.setText(r7)
        L_0x04d1:
            r22 = r1
            r23 = r2
            goto L_0x03ed
        L_0x04d7:
            r6 = 2131625894(0x7f0e07a6, float:1.8879009E38)
            java.lang.String r4 = r5.username
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x04f5
            org.telegram.ui.ActionBar.SimpleTextView[] r4 = r0.onlineTextView
            r4 = r4[r3]
            r15 = 2131625898(0x7f0e07aa, float:1.8879017E38)
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r10, r15)
            java.lang.String r7 = r7.toLowerCase()
            r4.setText(r7)
            goto L_0x04d1
        L_0x04f5:
            r15 = 2131625898(0x7f0e07aa, float:1.8879017E38)
            org.telegram.ui.ActionBar.SimpleTextView[] r4 = r0.onlineTextView
            r4 = r4[r3]
            r7 = 2131625895(0x7f0e07a7, float:1.887901E38)
            java.lang.String r13 = org.telegram.messenger.LocaleController.getString(r12, r7)
            java.lang.String r13 = r13.toLowerCase()
            r4.setText(r13)
            goto L_0x04d1
        L_0x050b:
            r15 = 2131625898(0x7f0e07aa, float:1.8879017E38)
            r17 = 2131625895(0x7f0e07a7, float:1.887901E38)
            org.telegram.ui.ActionBar.SimpleTextView[] r6 = r0.onlineTextView
            r6 = r6[r3]
            r18 = 0
            r15 = r7[r18]
            java.lang.String r15 = org.telegram.messenger.LocaleController.formatPluralString(r9, r15)
            r22 = r1
            r23 = r2
            r1 = 1
            java.lang.Object[] r2 = new java.lang.Object[r1]
            r1 = r7[r18]
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r2[r18] = r1
            java.lang.String r1 = java.lang.String.format(r13, r2)
            java.lang.String r1 = r15.replace(r1, r4)
            r6.setText(r1)
            goto L_0x0577
        L_0x0538:
            r22 = r1
            r23 = r2
            r17 = 2131625895(0x7f0e07a7, float:1.887901E38)
            r18 = 0
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r0.onlineTextView
            r1 = r1[r3]
            r2 = r7[r18]
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r11, r2)
            r6 = 1
            java.lang.Object[] r15 = new java.lang.Object[r6]
            r6 = r7[r18]
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            r15[r18] = r6
            java.lang.String r6 = java.lang.String.format(r13, r15)
            java.lang.String r2 = r2.replace(r6, r4)
            r1.setText(r2)
            goto L_0x0577
        L_0x0562:
            r22 = r1
            r23 = r2
            r17 = 2131625895(0x7f0e07a7, float:1.887901E38)
            org.telegram.ui.ActionBar.SimpleTextView[] r1 = r0.onlineTextView
            r1 = r1[r3]
            if (r3 != 0) goto L_0x0572
            r2 = r21
            goto L_0x0574
        L_0x0572:
            r2 = r23
        L_0x0574:
            r1.setText(r2)
        L_0x0577:
            int r3 = r3 + 1
            r6 = r20
            r7 = r21
            r1 = r22
            r2 = r23
            r4 = 1
            goto L_0x03dc
        L_0x0584:
            r20 = r6
            r1 = 1
            if (r8 == 0) goto L_0x058c
            r0.needLayout(r1)
        L_0x058c:
            org.telegram.tgnet.TLRPC$ChatPhoto r2 = r5.photo
            if (r2 == 0) goto L_0x0593
            org.telegram.tgnet.TLRPC$FileLocation r15 = r2.photo_big
            goto L_0x0594
        L_0x0593:
            r15 = 0
        L_0x0594:
            org.telegram.ui.Components.AvatarDrawable r2 = r0.avatarDrawable
            r2.setInfo((org.telegram.tgnet.TLRPC$Chat) r5)
            org.telegram.messenger.ImageLocation r2 = org.telegram.messenger.ImageLocation.getForChat(r5, r1)
            r1 = 0
            org.telegram.messenger.ImageLocation r11 = org.telegram.messenger.ImageLocation.getForChat(r5, r1)
            org.telegram.ui.Components.ProfileGalleryView r1 = r0.avatarsViewPager
            org.telegram.messenger.ImageLocation r9 = r1.getCurrentVideoLocation(r11, r2)
            org.telegram.ui.Components.ProfileGalleryView r1 = r0.avatarsViewPager
            boolean r1 = r1.initIfEmpty(r2, r11)
            if (r2 == 0) goto L_0x05b2
            if (r1 == 0) goto L_0x05d1
        L_0x05b2:
            boolean r1 = r0.isPulledDown
            if (r1 == 0) goto L_0x05d1
            androidx.recyclerview.widget.LinearLayoutManager r1 = r0.layoutManager
            r3 = 0
            android.view.View r1 = r1.findViewByPosition(r3)
            if (r1 == 0) goto L_0x05d1
            org.telegram.ui.Components.RecyclerListView r4 = r0.listView
            int r1 = r1.getTop()
            r6 = 1118830592(0x42b00000, float:88.0)
            int r6 = org.telegram.messenger.AndroidUtilities.dp(r6)
            int r1 = r1 - r6
            org.telegram.ui.Components.CubicBezierInterpolator r6 = org.telegram.ui.Components.CubicBezierInterpolator.EASE_OUT_QUINT
            r4.smoothScrollBy(r3, r1, r6)
        L_0x05d1:
            if (r9 == 0) goto L_0x05db
            int r1 = r9.imageType
            r3 = 2
            if (r1 != r3) goto L_0x05db
            r10 = r20
            goto L_0x05dc
        L_0x05db:
            r10 = 0
        L_0x05dc:
            org.telegram.tgnet.TLRPC$FileLocation r1 = r0.avatarBig
            if (r1 != 0) goto L_0x05ea
            org.telegram.ui.ProfileActivity$AvatarImageView r8 = r0.avatarImage
            org.telegram.ui.Components.AvatarDrawable r13 = r0.avatarDrawable
            java.lang.String r12 = "50_50"
            r14 = r5
            r8.setImage((org.telegram.messenger.ImageLocation) r9, (java.lang.String) r10, (org.telegram.messenger.ImageLocation) r11, (java.lang.String) r12, (android.graphics.drawable.Drawable) r13, (java.lang.Object) r14)
        L_0x05ea:
            org.telegram.messenger.FileLoader r8 = r24.getFileLoader()
            r11 = 0
            r12 = 0
            r13 = 1
            r9 = r2
            r10 = r5
            r8.loadFile(r9, r10, r11, r12, r13)
            org.telegram.ui.ProfileActivity$AvatarImageView r1 = r0.avatarImage
            org.telegram.messenger.ImageReceiver r1 = r1.getImageReceiver()
            boolean r2 = org.telegram.ui.PhotoViewer.isShowingImage((org.telegram.tgnet.TLRPC$FileLocation) r15)
            r3 = 1
            r2 = r2 ^ r3
            r3 = 0
            r1.setVisible(r2, r3)
        L_0x0606:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.updateProfileData():void");
    }

    private void createActionBarMenu(boolean z) {
        int i;
        String str;
        int i2;
        ActionBar actionBar = this.actionBar;
        if (actionBar != null && this.otherItem != null) {
            actionBar.createMenu();
            this.otherItem.removeAllSubItems();
            this.animatingItem = null;
            this.editItemVisible = false;
            this.callItemVisible = false;
            this.videoCallItemVisible = false;
            this.canSearchMembers = false;
            if (this.user_id != 0) {
                TLRPC$User user = getMessagesController().getUser(Integer.valueOf(this.user_id));
                if (user != null) {
                    if (UserObject.isUserSelf(user)) {
                        this.otherItem.addSubItem(30, NUM, LocaleController.getString("EditName", NUM));
                        this.otherItem.addSubItem(31, NUM, LocaleController.getString("LogOut", NUM));
                    } else {
                        TLRPC$UserFull tLRPC$UserFull = this.userInfo;
                        if (tLRPC$UserFull != null && tLRPC$UserFull.phone_calls_available) {
                            this.callItemVisible = true;
                            this.videoCallItemVisible = Build.VERSION.SDK_INT >= 18 && tLRPC$UserFull.video_calls_available;
                        }
                        int i3 = NUM;
                        if (!this.isBot && getContactsController().contactsDict.get(Integer.valueOf(this.user_id)) != null) {
                            if (!TextUtils.isEmpty(user.phone)) {
                                this.otherItem.addSubItem(3, NUM, LocaleController.getString("ShareContact", NUM));
                            }
                            this.otherItem.addSubItem(2, NUM, !this.userBlocked ? LocaleController.getString("BlockContact", NUM) : LocaleController.getString("Unblock", NUM));
                            this.otherItem.addSubItem(4, NUM, LocaleController.getString("EditContact", NUM));
                            this.otherItem.addSubItem(5, NUM, LocaleController.getString("DeleteContact", NUM));
                        } else if (!MessagesController.isSupportUser(user)) {
                            if (this.isBot) {
                                if (!user.bot_nochats) {
                                    this.otherItem.addSubItem(9, NUM, LocaleController.getString("BotInvite", NUM));
                                }
                                this.otherItem.addSubItem(10, NUM, LocaleController.getString("BotShare", NUM));
                            } else {
                                this.otherItem.addSubItem(1, NUM, LocaleController.getString("AddContact", NUM));
                            }
                            if (!TextUtils.isEmpty(user.phone)) {
                                this.otherItem.addSubItem(3, NUM, LocaleController.getString("ShareContact", NUM));
                            }
                            if (this.isBot) {
                                ActionBarMenuItem actionBarMenuItem = this.otherItem;
                                boolean z2 = this.userBlocked;
                                if (z2) {
                                    i3 = NUM;
                                }
                                if (!z2) {
                                    i2 = NUM;
                                    str = "BotStop";
                                } else {
                                    i2 = NUM;
                                    str = "BotRestart";
                                }
                                actionBarMenuItem.addSubItem(2, i3, LocaleController.getString(str, i2));
                            } else {
                                this.otherItem.addSubItem(2, NUM, !this.userBlocked ? LocaleController.getString("BlockContact", NUM) : LocaleController.getString("Unblock", NUM));
                            }
                        } else if (this.userBlocked) {
                            this.otherItem.addSubItem(2, NUM, LocaleController.getString("Unblock", NUM));
                        }
                        if (!UserObject.isDeleted(user) && !this.isBot && this.currentEncryptedChat == null && !this.userBlocked && (i = this.user_id) != 333000 && i != 777000 && i != 42777) {
                            this.otherItem.addSubItem(20, NUM, LocaleController.getString("StartEncryptedChat", NUM));
                        }
                        this.otherItem.addSubItem(14, NUM, LocaleController.getString("AddShortcut", NUM));
                    }
                } else {
                    return;
                }
            } else if (this.chat_id != 0) {
                TLRPC$Chat chat = getMessagesController().getChat(Integer.valueOf(this.chat_id));
                this.hasVoiceChatItem = false;
                if (ChatObject.isChannel(chat)) {
                    if (ChatObject.hasAdminRights(chat) || (chat.megagroup && ChatObject.canChangeChatInfo(chat))) {
                        this.editItemVisible = true;
                    }
                    if (this.chatInfo != null) {
                        if (chat.megagroup && ChatObject.canManageCalls(chat) && this.chatInfo.call == null) {
                            this.otherItem.addSubItem(15, NUM, LocaleController.getString("StartVoipChat", NUM));
                            this.hasVoiceChatItem = true;
                        }
                        if (this.chatInfo.can_view_stats) {
                            this.otherItem.addSubItem(19, NUM, LocaleController.getString("Statistics", NUM));
                        }
                    }
                    if (chat.megagroup) {
                        this.canSearchMembers = true;
                        this.otherItem.addSubItem(17, NUM, LocaleController.getString("SearchMembers", NUM));
                        if (!chat.creator && !chat.left && !chat.kicked) {
                            this.otherItem.addSubItem(7, NUM, LocaleController.getString("LeaveMegaMenu", NUM));
                        }
                    } else {
                        if (!TextUtils.isEmpty(chat.username)) {
                            this.otherItem.addSubItem(10, NUM, LocaleController.getString("BotShare", NUM));
                        }
                        TLRPC$ChatFull tLRPC$ChatFull = this.chatInfo;
                        if (!(tLRPC$ChatFull == null || tLRPC$ChatFull.linked_chat_id == 0)) {
                            this.otherItem.addSubItem(22, NUM, LocaleController.getString("ViewDiscussion", NUM));
                        }
                        TLRPC$Chat tLRPC$Chat = this.currentChat;
                        if (!tLRPC$Chat.creator && !tLRPC$Chat.left && !tLRPC$Chat.kicked) {
                            this.otherItem.addSubItem(7, NUM, LocaleController.getString("LeaveChannelMenu", NUM));
                        }
                    }
                } else {
                    if (this.chatInfo != null && ChatObject.canManageCalls(chat) && this.chatInfo.call == null) {
                        this.otherItem.addSubItem(15, NUM, LocaleController.getString("StartVoipChat", NUM));
                        this.hasVoiceChatItem = true;
                    }
                    if (ChatObject.canChangeChatInfo(chat)) {
                        this.editItemVisible = true;
                    }
                    if (!ChatObject.isKickedFromChat(chat) && !ChatObject.isLeftFromChat(chat)) {
                        this.canSearchMembers = true;
                        this.otherItem.addSubItem(17, NUM, LocaleController.getString("SearchMembers", NUM));
                    }
                    this.otherItem.addSubItem(7, NUM, LocaleController.getString("DeleteAndExit", NUM));
                }
                this.otherItem.addSubItem(14, NUM, LocaleController.getString("AddShortcut", NUM));
            }
            if (this.imageUpdater != null) {
                this.otherItem.addSubItem(36, NUM, LocaleController.getString("AddPhoto", NUM));
                this.otherItem.addSubItem(33, NUM, LocaleController.getString("SetAsMain", NUM));
                this.otherItem.addSubItem(21, NUM, LocaleController.getString("SaveToGallery", NUM));
                this.otherItem.addSubItem(35, NUM, LocaleController.getString("Delete", NUM));
            } else {
                this.otherItem.addSubItem(21, NUM, LocaleController.getString("SaveToGallery", NUM));
            }
            if (!this.isPulledDown) {
                this.otherItem.hideSubItem(21);
                this.otherItem.hideSubItem(33);
                this.otherItem.hideSubItem(36);
                this.otherItem.hideSubItem(34);
                this.otherItem.hideSubItem(35);
            }
            if (this.callItemVisible) {
                if (this.callItem.getVisibility() != 0) {
                    this.callItem.setVisibility(0);
                    if (z) {
                        this.callItem.setAlpha(0.0f);
                        this.callItem.animate().alpha(1.0f).setDuration(150).start();
                    }
                }
            } else if (this.callItem.getVisibility() != 8) {
                this.callItem.setVisibility(8);
            }
            if (this.videoCallItemVisible) {
                if (this.videoCallItem.getVisibility() != 0) {
                    this.videoCallItem.setVisibility(0);
                    if (z) {
                        this.videoCallItem.setAlpha(0.0f);
                        this.videoCallItem.animate().alpha(1.0f).setDuration(150).start();
                    }
                }
            } else if (this.videoCallItem.getVisibility() != 8) {
                this.videoCallItem.setVisibility(8);
            }
            if (this.editItemVisible) {
                if (this.editItem.getVisibility() != 0) {
                    this.editItem.setVisibility(0);
                    if (z) {
                        this.editItem.setAlpha(0.0f);
                        this.editItem.animate().alpha(1.0f).setDuration(150).start();
                    }
                }
            } else if (this.editItem.getVisibility() != 8) {
                this.editItem.setVisibility(8);
            }
            PagerIndicatorView pagerIndicatorView = this.avatarsViewPagerIndicatorView;
            if (pagerIndicatorView != null && pagerIndicatorView.isIndicatorFullyVisible()) {
                if (this.editItemVisible) {
                    this.editItem.setVisibility(8);
                    this.editItem.animate().cancel();
                    this.editItem.setAlpha(1.0f);
                }
                if (this.callItemVisible) {
                    this.callItem.setVisibility(8);
                    this.callItem.animate().cancel();
                    this.callItem.setAlpha(1.0f);
                }
                if (this.videoCallItemVisible) {
                    this.videoCallItem.setVisibility(8);
                    this.videoCallItem.animate().cancel();
                    this.videoCallItem.setAlpha(1.0f);
                }
            }
            SharedMediaLayout sharedMediaLayout2 = this.sharedMediaLayout;
            if (sharedMediaLayout2 != null) {
                sharedMediaLayout2.getSearchItem().requestLayout();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onDialogDismiss(Dialog dialog) {
        RecyclerListView recyclerListView = this.listView;
        if (recyclerListView != null) {
            recyclerListView.invalidateViews();
        }
    }

    public void didSelectDialogs(DialogsActivity dialogsActivity, ArrayList<Long> arrayList, CharSequence charSequence, boolean z) {
        long longValue = arrayList.get(0).longValue();
        Bundle bundle = new Bundle();
        bundle.putBoolean("scrollToTopOnResume", true);
        int i = (int) longValue;
        if (i == 0) {
            bundle.putInt("enc_id", (int) (longValue >> 32));
        } else if (i > 0) {
            bundle.putInt("user_id", i);
        } else {
            bundle.putInt("chat_id", -i);
        }
        if (getMessagesController().checkCanOpenChat(bundle, dialogsActivity)) {
            NotificationCenter notificationCenter = getNotificationCenter();
            int i2 = NotificationCenter.closeChats;
            notificationCenter.removeObserver(this, i2);
            getNotificationCenter().postNotificationName(i2, new Object[0]);
            presentFragment(new ChatActivity(bundle), true);
            removeSelfFromStack();
            getSendMessagesHelper().sendMessage(getMessagesController().getUser(Integer.valueOf(this.user_id)), longValue, (MessageObject) null, (MessageObject) null, (TLRPC$ReplyMarkup) null, (HashMap<String, String>) null, true, 0);
        }
    }

    public void onRequestPermissionsResultFragment(int i, String[] strArr, int[] iArr) {
        boolean z;
        ImageUpdater imageUpdater2 = this.imageUpdater;
        if (imageUpdater2 != null) {
            imageUpdater2.onRequestPermissionsResultFragment(i, strArr, iArr);
        }
        boolean z2 = false;
        if (i == 101 || i == 102) {
            TLRPC$User user = getMessagesController().getUser(Integer.valueOf(this.user_id));
            if (user != null) {
                int i2 = 0;
                while (true) {
                    if (i2 >= iArr.length) {
                        z = true;
                        break;
                    } else if (iArr[i2] != 0) {
                        z = false;
                        break;
                    } else {
                        i2++;
                    }
                }
                if (iArr.length <= 0 || !z) {
                    VoIPHelper.permissionDenied(getParentActivity(), (Runnable) null, i);
                    return;
                }
                boolean z3 = i == 102;
                TLRPC$UserFull tLRPC$UserFull = this.userInfo;
                if (tLRPC$UserFull != null && tLRPC$UserFull.video_calls_available) {
                    z2 = true;
                }
                VoIPHelper.startCall(user, z3, z2, getParentActivity(), this.userInfo);
            }
        } else if (i == 103 && this.currentChat != null) {
            int i3 = 0;
            while (true) {
                if (i3 >= iArr.length) {
                    z2 = true;
                    break;
                } else if (iArr[i3] != 0) {
                    break;
                } else {
                    i3++;
                }
            }
            if (iArr.length <= 0 || !z2) {
                VoIPHelper.permissionDenied(getParentActivity(), (Runnable) null, i);
            } else {
                VoIPHelper.startCall(this.currentChat, true, getParentActivity());
            }
        }
    }

    public void dismissCurrentDialog() {
        ImageUpdater imageUpdater2 = this.imageUpdater;
        if (imageUpdater2 == null || !imageUpdater2.dismissCurrentDialog(this.visibleDialog)) {
            super.dismissCurrentDialog();
        }
    }

    public boolean dismissDialogOnPause(Dialog dialog) {
        ImageUpdater imageUpdater2 = this.imageUpdater;
        return (imageUpdater2 == null || imageUpdater2.dismissDialogOnPause(dialog)) && super.dismissDialogOnPause(dialog);
    }

    /* access modifiers changed from: private */
    public Animator searchExpandTransition(final boolean z) {
        if (z) {
            AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
            AndroidUtilities.setAdjustResizeToNothing(getParentActivity(), this.classGuid);
        }
        Animator animator = this.searchViewTransition;
        if (animator != null) {
            animator.removeAllListeners();
            this.searchViewTransition.cancel();
        }
        float[] fArr = new float[2];
        fArr[0] = this.searchTransitionProgress;
        boolean z2 = true;
        fArr[1] = z ? 0.0f : 1.0f;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(fArr);
        float f = this.extraHeight;
        this.searchListView.setTranslationY(f);
        this.searchListView.setVisibility(0);
        this.searchItem.setVisibility(0);
        this.listView.setVisibility(0);
        needLayout(true);
        this.avatarContainer.setVisibility(0);
        this.nameTextView[1].setVisibility(0);
        this.onlineTextView[1].setVisibility(0);
        ActionBar actionBar = this.actionBar;
        if (this.searchTransitionProgress <= 0.5f) {
            z2 = false;
        }
        actionBar.onSearchFieldVisibilityChanged(z2);
        ActionBarMenuItem actionBarMenuItem = this.otherItem;
        int i = 8;
        if (actionBarMenuItem != null) {
            actionBarMenuItem.setVisibility(this.searchTransitionProgress > 0.5f ? 0 : 8);
        }
        this.searchItem.setVisibility(this.searchTransitionProgress > 0.5f ? 0 : 8);
        FrameLayout searchContainer = this.searchItem.getSearchContainer();
        if (this.searchTransitionProgress <= 0.5f) {
            i = 0;
        }
        searchContainer.setVisibility(i);
        this.searchListView.setEmptyView(this.emptyView);
        this.avatarContainer.setClickable(false);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(ofFloat, f, z) {
            public final /* synthetic */ ValueAnimator f$1;
            public final /* synthetic */ float f$2;
            public final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                ProfileActivity.this.lambda$searchExpandTransition$29$ProfileActivity(this.f$1, this.f$2, this.f$3, valueAnimator);
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                ProfileActivity.this.updateSearchViewState(z);
                ProfileActivity.this.avatarContainer.setClickable(true);
                if (z) {
                    ProfileActivity.this.searchItem.requestFocusOnSearchView();
                }
                ProfileActivity.this.needLayout(true);
                Animator unused = ProfileActivity.this.searchViewTransition = null;
                ProfileActivity.this.fragmentView.invalidate();
            }
        });
        ofFloat.setDuration(220);
        ofFloat.setInterpolator(CubicBezierInterpolator.DEFAULT);
        this.searchViewTransition = ofFloat;
        return ofFloat;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$searchExpandTransition$29 */
    public /* synthetic */ void lambda$searchExpandTransition$29$ProfileActivity(ValueAnimator valueAnimator, float f, boolean z, ValueAnimator valueAnimator2) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.searchTransitionProgress = floatValue;
        float f2 = (floatValue - 0.5f) / 0.5f;
        float f3 = (0.5f - floatValue) / 0.5f;
        if (f2 < 0.0f) {
            f2 = 0.0f;
        }
        if (f3 < 0.0f) {
            f3 = 0.0f;
        }
        float f4 = -f;
        this.searchTransitionOffset = (int) ((1.0f - floatValue) * f4);
        this.searchListView.setTranslationY(floatValue * f);
        this.emptyView.setTranslationY(f * this.searchTransitionProgress);
        this.listView.setTranslationY(f4 * (1.0f - this.searchTransitionProgress));
        this.listView.setScaleX(1.0f - ((1.0f - this.searchTransitionProgress) * 0.01f));
        this.listView.setScaleY(1.0f - ((1.0f - this.searchTransitionProgress) * 0.01f));
        this.listView.setAlpha(this.searchTransitionProgress);
        boolean z2 = true;
        needLayout(true);
        this.listView.setAlpha(f2);
        this.searchListView.setAlpha(1.0f - this.searchTransitionProgress);
        this.searchListView.setScaleX((this.searchTransitionProgress * 0.05f) + 1.0f);
        this.searchListView.setScaleY((this.searchTransitionProgress * 0.05f) + 1.0f);
        this.emptyView.setAlpha(1.0f - f2);
        this.avatarContainer.setAlpha(f2);
        this.nameTextView[1].setAlpha(f2);
        this.onlineTextView[1].setAlpha(f2);
        this.searchItem.getSearchField().setAlpha(f3);
        if (z && this.searchTransitionProgress < 0.7f) {
            this.searchItem.requestFocusOnSearchView();
        }
        int i = 8;
        this.searchItem.getSearchContainer().setVisibility(this.searchTransitionProgress < 0.5f ? 0 : 8);
        ActionBarMenuItem actionBarMenuItem = this.otherItem;
        if (actionBarMenuItem != null) {
            actionBarMenuItem.setVisibility(this.searchTransitionProgress > 0.5f ? 0 : 8);
        }
        ActionBarMenuItem actionBarMenuItem2 = this.searchItem;
        if (this.searchTransitionProgress > 0.5f) {
            i = 0;
        }
        actionBarMenuItem2.setVisibility(i);
        ActionBar actionBar = this.actionBar;
        if (this.searchTransitionProgress >= 0.5f) {
            z2 = false;
        }
        actionBar.onSearchFieldVisibilityChanged(z2);
        ActionBarMenuItem actionBarMenuItem3 = this.otherItem;
        if (actionBarMenuItem3 != null) {
            actionBarMenuItem3.setAlpha(f2);
        }
        this.searchItem.setAlpha(f2);
        this.topView.invalidate();
        this.fragmentView.invalidate();
    }

    /* access modifiers changed from: private */
    public void updateSearchViewState(boolean z) {
        int i = 0;
        int i2 = z ? 8 : 0;
        this.listView.setVisibility(i2);
        this.searchListView.setVisibility(z ? 0 : 8);
        FrameLayout searchContainer = this.searchItem.getSearchContainer();
        if (!z) {
            i = 8;
        }
        searchContainer.setVisibility(i);
        this.actionBar.onSearchFieldVisibilityChanged(z);
        this.avatarContainer.setVisibility(i2);
        this.nameTextView[1].setVisibility(i2);
        this.onlineTextView[1].setVisibility(i2);
        ActionBarMenuItem actionBarMenuItem = this.otherItem;
        if (actionBarMenuItem != null) {
            actionBarMenuItem.setAlpha(1.0f);
            this.otherItem.setVisibility(i2);
        }
        this.searchItem.setVisibility(i2);
        this.avatarContainer.setAlpha(1.0f);
        this.nameTextView[1].setAlpha(1.0f);
        this.onlineTextView[1].setAlpha(1.0f);
        this.searchItem.setAlpha(1.0f);
        this.listView.setAlpha(1.0f);
        this.searchListView.setAlpha(1.0f);
        this.emptyView.setAlpha(1.0f);
        if (z) {
            this.searchListView.setEmptyView(this.emptyView);
        } else {
            this.emptyView.setVisibility(8);
        }
    }

    public void onUploadProgressChanged(float f) {
        RadialProgressView radialProgressView = this.avatarProgressView;
        if (radialProgressView != null) {
            radialProgressView.setProgress(f);
        }
    }

    public void didStartUpload(boolean z) {
        RadialProgressView radialProgressView = this.avatarProgressView;
        if (radialProgressView != null) {
            radialProgressView.setProgress(0.0f);
        }
    }

    public void didUploadPhoto(TLRPC$InputFile tLRPC$InputFile, TLRPC$InputFile tLRPC$InputFile2, double d, String str, TLRPC$PhotoSize tLRPC$PhotoSize, TLRPC$PhotoSize tLRPC$PhotoSize2) {
        AndroidUtilities.runOnUIThread(new Runnable(tLRPC$InputFile, tLRPC$InputFile2, d, str, tLRPC$PhotoSize2, tLRPC$PhotoSize) {
            public final /* synthetic */ TLRPC$InputFile f$1;
            public final /* synthetic */ TLRPC$InputFile f$2;
            public final /* synthetic */ double f$3;
            public final /* synthetic */ String f$4;
            public final /* synthetic */ TLRPC$PhotoSize f$5;
            public final /* synthetic */ TLRPC$PhotoSize f$6;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r6;
                this.f$5 = r7;
                this.f$6 = r8;
            }

            public final void run() {
                ProfileActivity.this.lambda$didUploadPhoto$32$ProfileActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$31 */
    public /* synthetic */ void lambda$null$31$ProfileActivity(String str, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_error, tLObject, str) {
            public final /* synthetic */ TLRPC$TL_error f$1;
            public final /* synthetic */ TLObject f$2;
            public final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                ProfileActivity.this.lambda$null$30$ProfileActivity(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$30 */
    public /* synthetic */ void lambda$null$30$ProfileActivity(TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, String str) {
        if (tLRPC$TL_error == null) {
            TLRPC$User user = getMessagesController().getUser(Integer.valueOf(getUserConfig().getClientUserId()));
            if (user == null) {
                user = getUserConfig().getCurrentUser();
                if (user != null) {
                    getMessagesController().putUser(user, false);
                } else {
                    return;
                }
            } else {
                getUserConfig().setCurrentUser(user);
            }
            TLRPC$TL_photos_photo tLRPC$TL_photos_photo = (TLRPC$TL_photos_photo) tLObject;
            ArrayList<TLRPC$PhotoSize> arrayList = tLRPC$TL_photos_photo.photo.sizes;
            TLRPC$PhotoSize closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(arrayList, 150);
            TLRPC$PhotoSize closestPhotoSizeWithSize2 = FileLoader.getClosestPhotoSizeWithSize(arrayList, 800);
            TLRPC$VideoSize tLRPC$VideoSize = tLRPC$TL_photos_photo.photo.video_sizes.isEmpty() ? null : tLRPC$TL_photos_photo.photo.video_sizes.get(0);
            TLRPC$TL_userProfilePhoto tLRPC$TL_userProfilePhoto = new TLRPC$TL_userProfilePhoto();
            user.photo = tLRPC$TL_userProfilePhoto;
            tLRPC$TL_userProfilePhoto.photo_id = tLRPC$TL_photos_photo.photo.id;
            if (closestPhotoSizeWithSize != null) {
                tLRPC$TL_userProfilePhoto.photo_small = closestPhotoSizeWithSize.location;
            }
            if (closestPhotoSizeWithSize2 != null) {
                tLRPC$TL_userProfilePhoto.photo_big = closestPhotoSizeWithSize2.location;
            }
            if (!(closestPhotoSizeWithSize == null || this.avatar == null)) {
                FileLoader.getPathToAttach(this.avatar, true).renameTo(FileLoader.getPathToAttach(closestPhotoSizeWithSize, true));
                ImageLoader.getInstance().replaceImageInCache(this.avatar.volume_id + "_" + this.avatar.local_id + "@50_50", closestPhotoSizeWithSize.location.volume_id + "_" + closestPhotoSizeWithSize.location.local_id + "@50_50", ImageLocation.getForUser(user, false), true);
            }
            if (!(closestPhotoSizeWithSize2 == null || this.avatarBig == null)) {
                FileLoader.getPathToAttach(this.avatarBig, true).renameTo(FileLoader.getPathToAttach(closestPhotoSizeWithSize2, true));
            }
            if (!(tLRPC$VideoSize == null || str == null)) {
                new File(str).renameTo(FileLoader.getPathToAttach(tLRPC$VideoSize, "mp4", true));
            }
            getMessagesStorage().clearUserPhotos(user.id);
            ArrayList arrayList2 = new ArrayList();
            arrayList2.add(user);
            getMessagesStorage().putUsersAndChats(arrayList2, (ArrayList<TLRPC$Chat>) null, false, true);
        }
        this.allowPullingDown = !AndroidUtilities.isTablet() && !this.isInLandscapeMode && this.avatarImage.getImageReceiver().hasNotThumb();
        this.avatar = null;
        this.avatarBig = null;
        updateProfileData();
        showAvatarProgress(false, true);
        getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, 1535);
        getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged, new Object[0]);
        getUserConfig().saveConfig(true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$didUploadPhoto$32 */
    public /* synthetic */ void lambda$didUploadPhoto$32$ProfileActivity(TLRPC$InputFile tLRPC$InputFile, TLRPC$InputFile tLRPC$InputFile2, double d, String str, TLRPC$PhotoSize tLRPC$PhotoSize, TLRPC$PhotoSize tLRPC$PhotoSize2) {
        if (tLRPC$InputFile == null && tLRPC$InputFile2 == null) {
            this.allowPullingDown = false;
            TLRPC$FileLocation tLRPC$FileLocation = tLRPC$PhotoSize.location;
            this.avatar = tLRPC$FileLocation;
            this.avatarBig = tLRPC$PhotoSize2.location;
            this.avatarImage.setImage(ImageLocation.getForLocal(tLRPC$FileLocation), "50_50", (Drawable) this.avatarDrawable, (Object) null);
            if (this.setAvatarRow != -1) {
                updateRowsIds();
                ListAdapter listAdapter2 = this.listAdapter;
                if (listAdapter2 != null) {
                    listAdapter2.notifyDataSetChanged();
                }
                needLayout(true);
            }
            showAvatarProgress(true, false);
            View findViewByPosition = this.layoutManager.findViewByPosition(0);
            if (findViewByPosition != null && this.isPulledDown) {
                this.listView.smoothScrollBy(0, findViewByPosition.getTop() - AndroidUtilities.dp(88.0f), CubicBezierInterpolator.EASE_OUT_QUINT);
                return;
            }
            return;
        }
        TLRPC$TL_photos_uploadProfilePhoto tLRPC$TL_photos_uploadProfilePhoto = new TLRPC$TL_photos_uploadProfilePhoto();
        if (tLRPC$InputFile != null) {
            tLRPC$TL_photos_uploadProfilePhoto.file = tLRPC$InputFile;
            tLRPC$TL_photos_uploadProfilePhoto.flags |= 1;
        }
        if (tLRPC$InputFile2 != null) {
            tLRPC$TL_photos_uploadProfilePhoto.video = tLRPC$InputFile2;
            int i = tLRPC$TL_photos_uploadProfilePhoto.flags | 2;
            tLRPC$TL_photos_uploadProfilePhoto.flags = i;
            tLRPC$TL_photos_uploadProfilePhoto.video_start_ts = d;
            tLRPC$TL_photos_uploadProfilePhoto.flags = i | 4;
        }
        getConnectionsManager().sendRequest(tLRPC$TL_photos_uploadProfilePhoto, new RequestDelegate(str) {
            public final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                ProfileActivity.this.lambda$null$31$ProfileActivity(this.f$1, tLObject, tLRPC$TL_error);
            }
        });
    }

    private void showAvatarProgress(final boolean z, boolean z2) {
        if (this.avatarProgressView != null) {
            AnimatorSet animatorSet = this.avatarAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
                this.avatarAnimation = null;
            }
            if (z2) {
                AnimatorSet animatorSet2 = new AnimatorSet();
                this.avatarAnimation = animatorSet2;
                if (z) {
                    this.avatarProgressView.setVisibility(0);
                    this.avatarAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(this.avatarProgressView, View.ALPHA, new float[]{1.0f})});
                } else {
                    animatorSet2.playTogether(new Animator[]{ObjectAnimator.ofFloat(this.avatarProgressView, View.ALPHA, new float[]{0.0f})});
                }
                this.avatarAnimation.setDuration(180);
                this.avatarAnimation.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        if (ProfileActivity.this.avatarAnimation != null && ProfileActivity.this.avatarProgressView != null) {
                            if (!z) {
                                ProfileActivity.this.avatarProgressView.setVisibility(4);
                            }
                            AnimatorSet unused = ProfileActivity.this.avatarAnimation = null;
                        }
                    }

                    public void onAnimationCancel(Animator animator) {
                        AnimatorSet unused = ProfileActivity.this.avatarAnimation = null;
                    }
                });
                this.avatarAnimation.start();
            } else if (z) {
                this.avatarProgressView.setAlpha(1.0f);
                this.avatarProgressView.setVisibility(0);
            } else {
                this.avatarProgressView.setAlpha(0.0f);
                this.avatarProgressView.setVisibility(4);
            }
        }
    }

    public void onActivityResultFragment(int i, int i2, Intent intent) {
        ImageUpdater imageUpdater2 = this.imageUpdater;
        if (imageUpdater2 != null) {
            imageUpdater2.onActivityResult(i, i2, intent);
        }
    }

    public void saveSelfArgs(Bundle bundle) {
        String str;
        ImageUpdater imageUpdater2 = this.imageUpdater;
        if (imageUpdater2 != null && (str = imageUpdater2.currentPicturePath) != null) {
            bundle.putString("path", str);
        }
    }

    public void restoreSelfArgs(Bundle bundle) {
        ImageUpdater imageUpdater2 = this.imageUpdater;
        if (imageUpdater2 != null) {
            imageUpdater2.currentPicturePath = bundle.getString("path");
        }
    }

    private void sendLogs() {
        if (getParentActivity() != null) {
            AlertDialog alertDialog = new AlertDialog(getParentActivity(), 3);
            alertDialog.setCanCacnel(false);
            alertDialog.show();
            Utilities.globalQueue.postRunnable(new Runnable(alertDialog) {
                public final /* synthetic */ AlertDialog f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ProfileActivity.this.lambda$sendLogs$34$ProfileActivity(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0094 A[SYNTHETIC, Splitter:B:36:0x0094] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0099 A[Catch:{ Exception -> 0x00b0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a7 A[Catch:{ Exception -> 0x00b0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00ac A[Catch:{ Exception -> 0x00b0 }] */
    /* renamed from: lambda$sendLogs$34 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$sendLogs$34$ProfileActivity(org.telegram.ui.ActionBar.AlertDialog r14) {
        /*
            r13 = this;
            android.content.Context r0 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x00b0 }
            r1 = 0
            java.io.File r0 = r0.getExternalFilesDir(r1)     // Catch:{ Exception -> 0x00b0 }
            java.io.File r2 = new java.io.File     // Catch:{ Exception -> 0x00b0 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00b0 }
            r3.<init>()     // Catch:{ Exception -> 0x00b0 }
            java.lang.String r0 = r0.getAbsolutePath()     // Catch:{ Exception -> 0x00b0 }
            r3.append(r0)     // Catch:{ Exception -> 0x00b0 }
            java.lang.String r0 = "/logs"
            r3.append(r0)     // Catch:{ Exception -> 0x00b0 }
            java.lang.String r0 = r3.toString()     // Catch:{ Exception -> 0x00b0 }
            r2.<init>(r0)     // Catch:{ Exception -> 0x00b0 }
            java.io.File r0 = new java.io.File     // Catch:{ Exception -> 0x00b0 }
            java.lang.String r3 = "logs.zip"
            r0.<init>(r2, r3)     // Catch:{ Exception -> 0x00b0 }
            boolean r3 = r0.exists()     // Catch:{ Exception -> 0x00b0 }
            if (r3 == 0) goto L_0x0031
            r0.delete()     // Catch:{ Exception -> 0x00b0 }
        L_0x0031:
            java.io.File[] r2 = r2.listFiles()     // Catch:{ Exception -> 0x00b0 }
            r3 = 1
            boolean[] r4 = new boolean[r3]     // Catch:{ Exception -> 0x00b0 }
            java.io.FileOutputStream r5 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            r5.<init>(r0)     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            java.util.zip.ZipOutputStream r6 = new java.util.zip.ZipOutputStream     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            java.io.BufferedOutputStream r7 = new java.io.BufferedOutputStream     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            r7.<init>(r5)     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            r6.<init>(r7)     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            r5 = 65536(0x10000, float:9.18355E-41)
            byte[] r7 = new byte[r5]     // Catch:{ Exception -> 0x0085, all -> 0x0083 }
            r8 = 0
            r9 = 0
        L_0x004d:
            int r10 = r2.length     // Catch:{ Exception -> 0x0085, all -> 0x0083 }
            if (r9 >= r10) goto L_0x007d
            java.io.FileInputStream r10 = new java.io.FileInputStream     // Catch:{ Exception -> 0x0085, all -> 0x0083 }
            r11 = r2[r9]     // Catch:{ Exception -> 0x0085, all -> 0x0083 }
            r10.<init>(r11)     // Catch:{ Exception -> 0x0085, all -> 0x0083 }
            java.io.BufferedInputStream r11 = new java.io.BufferedInputStream     // Catch:{ Exception -> 0x0085, all -> 0x0083 }
            r11.<init>(r10, r5)     // Catch:{ Exception -> 0x0085, all -> 0x0083 }
            java.util.zip.ZipEntry r10 = new java.util.zip.ZipEntry     // Catch:{ Exception -> 0x007b }
            r12 = r2[r9]     // Catch:{ Exception -> 0x007b }
            java.lang.String r12 = r12.getName()     // Catch:{ Exception -> 0x007b }
            r10.<init>(r12)     // Catch:{ Exception -> 0x007b }
            r6.putNextEntry(r10)     // Catch:{ Exception -> 0x007b }
        L_0x006a:
            int r10 = r11.read(r7, r8, r5)     // Catch:{ Exception -> 0x007b }
            r12 = -1
            if (r10 == r12) goto L_0x0075
            r6.write(r7, r8, r10)     // Catch:{ Exception -> 0x007b }
            goto L_0x006a
        L_0x0075:
            r11.close()     // Catch:{ Exception -> 0x007b }
            int r9 = r9 + 1
            goto L_0x004d
        L_0x007b:
            r1 = move-exception
            goto L_0x008f
        L_0x007d:
            r4[r8] = r3     // Catch:{ Exception -> 0x0085, all -> 0x0083 }
        L_0x007f:
            r6.close()     // Catch:{ Exception -> 0x00b0 }
            goto L_0x009a
        L_0x0083:
            r14 = move-exception
            goto L_0x00a5
        L_0x0085:
            r2 = move-exception
            r11 = r1
            goto L_0x008e
        L_0x0088:
            r14 = move-exception
            r6 = r1
            goto L_0x00a5
        L_0x008b:
            r2 = move-exception
            r6 = r1
            r11 = r6
        L_0x008e:
            r1 = r2
        L_0x008f:
            r1.printStackTrace()     // Catch:{ all -> 0x00a3 }
            if (r11 == 0) goto L_0x0097
            r11.close()     // Catch:{ Exception -> 0x00b0 }
        L_0x0097:
            if (r6 == 0) goto L_0x009a
            goto L_0x007f
        L_0x009a:
            org.telegram.ui.-$$Lambda$ProfileActivity$jSITH1vOhBBvar_dqexb1HWDzxAI r1 = new org.telegram.ui.-$$Lambda$ProfileActivity$jSITH1vOhBBvar_dqexb1HWDzxAI     // Catch:{ Exception -> 0x00b0 }
            r1.<init>(r14, r4, r0)     // Catch:{ Exception -> 0x00b0 }
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r1)     // Catch:{ Exception -> 0x00b0 }
            goto L_0x00b4
        L_0x00a3:
            r14 = move-exception
            r1 = r11
        L_0x00a5:
            if (r1 == 0) goto L_0x00aa
            r1.close()     // Catch:{ Exception -> 0x00b0 }
        L_0x00aa:
            if (r6 == 0) goto L_0x00af
            r6.close()     // Catch:{ Exception -> 0x00b0 }
        L_0x00af:
            throw r14     // Catch:{ Exception -> 0x00b0 }
        L_0x00b0:
            r14 = move-exception
            r14.printStackTrace()
        L_0x00b4:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.lambda$sendLogs$34$ProfileActivity(org.telegram.ui.ActionBar.AlertDialog):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$33 */
    public /* synthetic */ void lambda$null$33$ProfileActivity(AlertDialog alertDialog, boolean[] zArr, File file) {
        Uri uri;
        try {
            alertDialog.dismiss();
        } catch (Exception unused) {
        }
        if (zArr[0]) {
            int i = Build.VERSION.SDK_INT;
            if (i >= 24) {
                uri = FileProvider.getUriForFile(getParentActivity(), "org.telegram.messenger.beta.provider", file);
            } else {
                uri = Uri.fromFile(file);
            }
            Intent intent = new Intent("android.intent.action.SEND");
            if (i >= 24) {
                intent.addFlags(1);
            }
            intent.setType("message/rfCLASSNAME");
            intent.putExtra("android.intent.extra.EMAIL", "");
            intent.putExtra("android.intent.extra.SUBJECT", "Logs from " + LocaleController.getInstance().formatterStats.format(System.currentTimeMillis()));
            intent.putExtra("android.intent.extra.STREAM", uri);
            if (getParentActivity() != null) {
                try {
                    getParentActivity().startActivityForResult(Intent.createChooser(intent, "Select email application."), 500);
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
            }
        } else {
            Toast.makeText(getParentActivity(), LocaleController.getString("ErrorOccurred", NUM), 0).show();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v0, resolved type: org.telegram.ui.Components.SharedMediaLayout} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: org.telegram.ui.Components.SharedMediaLayout} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: org.telegram.ui.Cells.TextDetailCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v21, resolved type: org.telegram.ui.Cells.TextInfoPrivacyCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v18, resolved type: org.telegram.ui.Components.SharedMediaLayout} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v28, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v29, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v30, resolved type: org.telegram.ui.ProfileActivity$ListAdapter$1} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v31, resolved type: org.telegram.ui.Cells.TextCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v32, resolved type: org.telegram.ui.Cells.DividerCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v33, resolved type: org.telegram.ui.Cells.NotificationsCheckCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v34, resolved type: org.telegram.ui.Cells.ShadowSectionCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v35, resolved type: org.telegram.ui.Cells.UserCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v36, resolved type: org.telegram.ui.ProfileActivity$ListAdapter$2} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v37, resolved type: org.telegram.ui.ProfileActivity$ListAdapter$3} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v38, resolved type: org.telegram.ui.Components.SharedMediaLayout} */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup r13, int r14) {
            /*
                r12 = this;
                r13 = -1
                r0 = 23
                r1 = 0
                r2 = 1
                r3 = 0
                switch(r14) {
                    case 1: goto L_0x015a;
                    case 2: goto L_0x014f;
                    case 3: goto L_0x0145;
                    case 4: goto L_0x013d;
                    case 5: goto L_0x0126;
                    case 6: goto L_0x011c;
                    case 7: goto L_0x0114;
                    case 8: goto L_0x0100;
                    case 9: goto L_0x0009;
                    case 10: goto L_0x0009;
                    case 11: goto L_0x00f8;
                    case 12: goto L_0x00f0;
                    case 13: goto L_0x00c7;
                    case 14: goto L_0x000b;
                    default: goto L_0x0009;
                }
            L_0x0009:
                goto L_0x0161
            L_0x000b:
                org.telegram.ui.Cells.TextInfoPrivacyCell r0 = new org.telegram.ui.Cells.TextInfoPrivacyCell
                android.content.Context r4 = r12.mContext
                r5 = 10
                r0.<init>(r4, r5)
                android.widget.TextView r4 = r0.getTextView()
                r4.setGravity(r2)
                android.widget.TextView r4 = r0.getTextView()
                java.lang.String r6 = "windowBackgroundWhiteGrayText3"
                int r6 = org.telegram.ui.ActionBar.Theme.getColor(r6)
                r4.setTextColor(r6)
                android.widget.TextView r4 = r0.getTextView()
                r4.setMovementMethod(r1)
                android.content.Context r1 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x00a0 }
                android.content.pm.PackageManager r1 = r1.getPackageManager()     // Catch:{ Exception -> 0x00a0 }
                android.content.Context r4 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x00a0 }
                java.lang.String r4 = r4.getPackageName()     // Catch:{ Exception -> 0x00a0 }
                android.content.pm.PackageInfo r1 = r1.getPackageInfo(r4, r3)     // Catch:{ Exception -> 0x00a0 }
                int r4 = r1.versionCode     // Catch:{ Exception -> 0x00a0 }
                int r6 = r4 / 10
                java.lang.String r7 = ""
                int r4 = r4 % r5
                switch(r4) {
                    case 0: goto L_0x0059;
                    case 1: goto L_0x0056;
                    case 2: goto L_0x0052;
                    case 3: goto L_0x0056;
                    case 4: goto L_0x0052;
                    case 5: goto L_0x004f;
                    case 6: goto L_0x004b;
                    case 7: goto L_0x004f;
                    case 8: goto L_0x004b;
                    case 9: goto L_0x0059;
                    default: goto L_0x004a;
                }     // Catch:{ Exception -> 0x00a0 }
            L_0x004a:
                goto L_0x0076
            L_0x004b:
                java.lang.String r7 = "x86_64"
                goto L_0x0076
            L_0x004f:
                java.lang.String r7 = "arm64-v8a"
                goto L_0x0076
            L_0x0052:
                java.lang.String r7 = "x86"
                goto L_0x0076
            L_0x0056:
                java.lang.String r7 = "arm-v7a"
                goto L_0x0076
            L_0x0059:
                java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00a0 }
                r4.<init>()     // Catch:{ Exception -> 0x00a0 }
                java.lang.String r5 = "universal "
                r4.append(r5)     // Catch:{ Exception -> 0x00a0 }
                java.lang.String r5 = android.os.Build.CPU_ABI     // Catch:{ Exception -> 0x00a0 }
                r4.append(r5)     // Catch:{ Exception -> 0x00a0 }
                java.lang.String r5 = " "
                r4.append(r5)     // Catch:{ Exception -> 0x00a0 }
                java.lang.String r5 = android.os.Build.CPU_ABI2     // Catch:{ Exception -> 0x00a0 }
                r4.append(r5)     // Catch:{ Exception -> 0x00a0 }
                java.lang.String r7 = r4.toString()     // Catch:{ Exception -> 0x00a0 }
            L_0x0076:
                java.lang.String r4 = "TelegramVersion"
                r5 = 2131627373(0x7f0e0d6d, float:1.8882009E38)
                java.lang.Object[] r8 = new java.lang.Object[r2]     // Catch:{ Exception -> 0x00a0 }
                java.util.Locale r9 = java.util.Locale.US     // Catch:{ Exception -> 0x00a0 }
                java.lang.String r10 = "v%s (%d) %s"
                r11 = 3
                java.lang.Object[] r11 = new java.lang.Object[r11]     // Catch:{ Exception -> 0x00a0 }
                java.lang.String r1 = r1.versionName     // Catch:{ Exception -> 0x00a0 }
                r11[r3] = r1     // Catch:{ Exception -> 0x00a0 }
                java.lang.Integer r1 = java.lang.Integer.valueOf(r6)     // Catch:{ Exception -> 0x00a0 }
                r11[r2] = r1     // Catch:{ Exception -> 0x00a0 }
                r1 = 2
                r11[r1] = r7     // Catch:{ Exception -> 0x00a0 }
                java.lang.String r1 = java.lang.String.format(r9, r10, r11)     // Catch:{ Exception -> 0x00a0 }
                r8[r3] = r1     // Catch:{ Exception -> 0x00a0 }
                java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r4, r5, r8)     // Catch:{ Exception -> 0x00a0 }
                r0.setText(r1)     // Catch:{ Exception -> 0x00a0 }
                goto L_0x00a4
            L_0x00a0:
                r1 = move-exception
                org.telegram.messenger.FileLog.e((java.lang.Throwable) r1)
            L_0x00a4:
                android.widget.TextView r1 = r0.getTextView()
                r2 = 1096810496(0x41600000, float:14.0)
                int r4 = org.telegram.messenger.AndroidUtilities.dp(r2)
                int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
                r1.setPadding(r3, r4, r3, r2)
                android.content.Context r1 = r12.mContext
                r2 = 2131165447(0x7var_, float:1.7945111E38)
                java.lang.String r3 = "windowBackgroundGrayShadow"
                android.graphics.drawable.Drawable r1 = org.telegram.ui.ActionBar.Theme.getThemedDrawable((android.content.Context) r1, (int) r2, (java.lang.String) r3)
                r0.setBackgroundDrawable(r1)
                r1 = r0
                goto L_0x0161
            L_0x00c7:
                org.telegram.ui.ProfileActivity r0 = org.telegram.ui.ProfileActivity.this
                org.telegram.ui.Components.SharedMediaLayout r0 = r0.sharedMediaLayout
                android.view.ViewParent r0 = r0.getParent()
                if (r0 == 0) goto L_0x00e8
                org.telegram.ui.ProfileActivity r0 = org.telegram.ui.ProfileActivity.this
                org.telegram.ui.Components.SharedMediaLayout r0 = r0.sharedMediaLayout
                android.view.ViewParent r0 = r0.getParent()
                android.view.ViewGroup r0 = (android.view.ViewGroup) r0
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.ui.Components.SharedMediaLayout r1 = r1.sharedMediaLayout
                r0.removeView(r1)
            L_0x00e8:
                org.telegram.ui.ProfileActivity r0 = org.telegram.ui.ProfileActivity.this
                org.telegram.ui.Components.SharedMediaLayout r1 = r0.sharedMediaLayout
                goto L_0x0161
            L_0x00f0:
                org.telegram.ui.ProfileActivity$ListAdapter$3 r1 = new org.telegram.ui.ProfileActivity$ListAdapter$3
                android.content.Context r0 = r12.mContext
                r1.<init>(r0)
                goto L_0x0161
            L_0x00f8:
                org.telegram.ui.ProfileActivity$ListAdapter$2 r1 = new org.telegram.ui.ProfileActivity$ListAdapter$2
                android.content.Context r0 = r12.mContext
                r1.<init>(r12, r0)
                goto L_0x0161
            L_0x0100:
                org.telegram.ui.Cells.UserCell r1 = new org.telegram.ui.Cells.UserCell
                android.content.Context r0 = r12.mContext
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                int r4 = r4.addMemberRow
                if (r4 != r13) goto L_0x010f
                r4 = 9
                goto L_0x0110
            L_0x010f:
                r4 = 6
            L_0x0110:
                r1.<init>(r0, r4, r3, r2)
                goto L_0x0161
            L_0x0114:
                org.telegram.ui.Cells.ShadowSectionCell r1 = new org.telegram.ui.Cells.ShadowSectionCell
                android.content.Context r0 = r12.mContext
                r1.<init>(r0)
                goto L_0x0161
            L_0x011c:
                org.telegram.ui.Cells.NotificationsCheckCell r1 = new org.telegram.ui.Cells.NotificationsCheckCell
                android.content.Context r2 = r12.mContext
                r4 = 70
                r1.<init>(r2, r0, r4, r3)
                goto L_0x0161
            L_0x0126:
                org.telegram.ui.Cells.DividerCell r1 = new org.telegram.ui.Cells.DividerCell
                android.content.Context r0 = r12.mContext
                r1.<init>(r0)
                r0 = 1101004800(0x41a00000, float:20.0)
                int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
                r2 = 1082130432(0x40800000, float:4.0)
                int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
                r1.setPadding(r0, r2, r3, r3)
                goto L_0x0161
            L_0x013d:
                org.telegram.ui.Cells.TextCell r1 = new org.telegram.ui.Cells.TextCell
                android.content.Context r0 = r12.mContext
                r1.<init>(r0)
                goto L_0x0161
            L_0x0145:
                org.telegram.ui.ProfileActivity$ListAdapter$1 r1 = new org.telegram.ui.ProfileActivity$ListAdapter$1
                android.content.Context r0 = r12.mContext
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                r1.<init>(r0, r2)
                goto L_0x0161
            L_0x014f:
                org.telegram.ui.Cells.TextDetailCell r1 = new org.telegram.ui.Cells.TextDetailCell
                android.content.Context r0 = r12.mContext
                r1.<init>(r0)
                r1.setContentDescriptionValueFirst(r2)
                goto L_0x0161
            L_0x015a:
                org.telegram.ui.Cells.HeaderCell r1 = new org.telegram.ui.Cells.HeaderCell
                android.content.Context r2 = r12.mContext
                r1.<init>(r2, r0)
            L_0x0161:
                r0 = 13
                if (r14 == r0) goto L_0x016e
                androidx.recyclerview.widget.RecyclerView$LayoutParams r14 = new androidx.recyclerview.widget.RecyclerView$LayoutParams
                r0 = -2
                r14.<init>((int) r13, (int) r0)
                r1.setLayoutParams(r14)
            L_0x016e:
                org.telegram.ui.Components.RecyclerListView$Holder r13 = new org.telegram.ui.Components.RecyclerListView$Holder
                r13.<init>(r1)
                return r13
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.ListAdapter.onCreateViewHolder(android.view.ViewGroup, int):androidx.recyclerview.widget.RecyclerView$ViewHolder");
        }

        public void onViewAttachedToWindow(RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.itemView == ProfileActivity.this.sharedMediaLayout) {
                boolean unused = ProfileActivity.this.sharedMediaLayoutAttached = true;
            }
        }

        public void onViewDetachedFromWindow(RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.itemView == ProfileActivity.this.sharedMediaLayout) {
                boolean unused = ProfileActivity.this.sharedMediaLayoutAttached = false;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:95:0x02a8  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder r13, int r14) {
            /*
                r12 = this;
                int r2 = r13.getItemViewType()
                r3 = 2131624663(0x7f0e02d7, float:1.8876512E38)
                java.lang.String r4 = "ChannelMembers"
                r5 = 2131627557(0x7f0e0e25, float:1.8882382E38)
                java.lang.String r6 = "UserBio"
                r7 = 0
                r8 = -1
                r9 = 0
                r10 = 1
                switch(r2) {
                    case 1: goto L_0x0966;
                    case 2: goto L_0x072a;
                    case 3: goto L_0x06cc;
                    case 4: goto L_0x02bf;
                    case 5: goto L_0x0015;
                    case 6: goto L_0x0157;
                    case 7: goto L_0x00de;
                    case 8: goto L_0x0017;
                    default: goto L_0x0015;
                }
            L_0x0015:
                goto L_0x0a0e
            L_0x0017:
                android.view.View r0 = r13.itemView
                org.telegram.ui.Cells.UserCell r0 = (org.telegram.ui.Cells.UserCell) r0
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                java.util.ArrayList r2 = r2.sortedUsers
                boolean r2 = r2.isEmpty()
                if (r2 != 0) goto L_0x0050
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r2 = r2.chatInfo
                org.telegram.tgnet.TLRPC$ChatParticipants r2 = r2.participants
                java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r2 = r2.participants
                org.telegram.ui.ProfileActivity r3 = org.telegram.ui.ProfileActivity.this
                java.util.ArrayList r3 = r3.sortedUsers
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                int r4 = r4.membersStartRow
                int r4 = r14 - r4
                java.lang.Object r3 = r3.get(r4)
                java.lang.Integer r3 = (java.lang.Integer) r3
                int r3 = r3.intValue()
                java.lang.Object r2 = r2.get(r3)
                org.telegram.tgnet.TLRPC$ChatParticipant r2 = (org.telegram.tgnet.TLRPC$ChatParticipant) r2
                goto L_0x0068
            L_0x0050:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r2 = r2.chatInfo
                org.telegram.tgnet.TLRPC$ChatParticipants r2 = r2.participants
                java.util.ArrayList<org.telegram.tgnet.TLRPC$ChatParticipant> r2 = r2.participants
                org.telegram.ui.ProfileActivity r3 = org.telegram.ui.ProfileActivity.this
                int r3 = r3.membersStartRow
                int r3 = r14 - r3
                java.lang.Object r2 = r2.get(r3)
                org.telegram.tgnet.TLRPC$ChatParticipant r2 = (org.telegram.tgnet.TLRPC$ChatParticipant) r2
            L_0x0068:
                if (r2 == 0) goto L_0x0a0e
                boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_chatChannelParticipant
                if (r3 == 0) goto L_0x009b
                r3 = r2
                org.telegram.tgnet.TLRPC$TL_chatChannelParticipant r3 = (org.telegram.tgnet.TLRPC$TL_chatChannelParticipant) r3
                org.telegram.tgnet.TLRPC$ChannelParticipant r3 = r3.channelParticipant
                java.lang.String r4 = r3.rank
                boolean r4 = android.text.TextUtils.isEmpty(r4)
                if (r4 != 0) goto L_0x007f
                java.lang.String r3 = r3.rank
            L_0x007d:
                r7 = r3
                goto L_0x00b6
            L_0x007f:
                boolean r4 = r3 instanceof org.telegram.tgnet.TLRPC$TL_channelParticipantCreator
                if (r4 == 0) goto L_0x008d
                r3 = 2131624643(0x7f0e02c3, float:1.8876472E38)
                java.lang.String r4 = "ChannelCreator"
                java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
                goto L_0x007d
            L_0x008d:
                boolean r3 = r3 instanceof org.telegram.tgnet.TLRPC$TL_channelParticipantAdmin
                if (r3 == 0) goto L_0x00b6
                r3 = 2131624625(0x7f0e02b1, float:1.8876435E38)
                java.lang.String r4 = "ChannelAdmin"
                java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
                goto L_0x007d
            L_0x009b:
                boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_chatParticipantCreator
                if (r3 == 0) goto L_0x00a9
                r3 = 2131624643(0x7f0e02c3, float:1.8876472E38)
                java.lang.String r4 = "ChannelCreator"
                java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r4, r3)
                goto L_0x00b6
            L_0x00a9:
                boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_chatParticipantAdmin
                if (r3 == 0) goto L_0x00b6
                r3 = 2131624625(0x7f0e02b1, float:1.8876435E38)
                java.lang.String r4 = "ChannelAdmin"
                java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r4, r3)
            L_0x00b6:
                r0.setAdminRole(r7)
                org.telegram.ui.ProfileActivity r3 = org.telegram.ui.ProfileActivity.this
                org.telegram.messenger.MessagesController r3 = r3.getMessagesController()
                int r2 = r2.user_id
                java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
                org.telegram.tgnet.TLRPC$User r2 = r3.getUser(r2)
                r3 = 0
                r4 = 0
                r5 = 0
                org.telegram.ui.ProfileActivity r6 = org.telegram.ui.ProfileActivity.this
                int r6 = r6.membersEndRow
                int r6 = r6 - r10
                if (r14 == r6) goto L_0x00d7
                r6 = 1
                goto L_0x00d8
            L_0x00d7:
                r6 = 0
            L_0x00d8:
                r1 = r0
                r1.setData(r2, r3, r4, r5, r6)
                goto L_0x0a0e
            L_0x00de:
                android.view.View r0 = r13.itemView
                java.lang.Integer r2 = java.lang.Integer.valueOf(r14)
                r0.setTag(r2)
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.infoSectionRow
                java.lang.String r3 = "windowBackgroundGrayShadow"
                if (r14 != r2) goto L_0x0112
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.lastSectionRow
                if (r2 != r8) goto L_0x0112
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.secretSettingsSectionRow
                if (r2 != r8) goto L_0x0112
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.sharedMediaRow
                if (r2 != r8) goto L_0x0112
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.membersSectionRow
                if (r2 == r8) goto L_0x0149
            L_0x0112:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.secretSettingsSectionRow
                if (r14 == r2) goto L_0x0149
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.lastSectionRow
                if (r14 == r2) goto L_0x0149
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.membersSectionRow
                if (r14 != r2) goto L_0x013b
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                int r1 = r1.lastSectionRow
                if (r1 != r8) goto L_0x013b
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                int r1 = r1.sharedMediaRow
                if (r1 != r8) goto L_0x013b
                goto L_0x0149
            L_0x013b:
                android.content.Context r1 = r12.mContext
                r2 = 2131165446(0x7var_, float:1.794511E38)
                android.graphics.drawable.Drawable r1 = org.telegram.ui.ActionBar.Theme.getThemedDrawable((android.content.Context) r1, (int) r2, (java.lang.String) r3)
                r0.setBackgroundDrawable(r1)
                goto L_0x0a0e
            L_0x0149:
                android.content.Context r1 = r12.mContext
                r2 = 2131165447(0x7var_, float:1.7945111E38)
                android.graphics.drawable.Drawable r1 = org.telegram.ui.ActionBar.Theme.getThemedDrawable((android.content.Context) r1, (int) r2, (java.lang.String) r3)
                r0.setBackgroundDrawable(r1)
                goto L_0x0a0e
            L_0x0157:
                android.view.View r0 = r13.itemView
                org.telegram.ui.Cells.NotificationsCheckCell r0 = (org.telegram.ui.Cells.NotificationsCheckCell) r0
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.notificationsRow
                if (r14 != r2) goto L_0x0a0e
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                int r1 = r1.currentAccount
                android.content.SharedPreferences r1 = org.telegram.messenger.MessagesController.getNotificationsSettings(r1)
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                long r2 = r2.dialog_id
                r4 = 0
                int r6 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
                if (r6 == 0) goto L_0x0180
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                long r2 = r2.dialog_id
                goto L_0x0197
            L_0x0180:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.user_id
                if (r2 == 0) goto L_0x018f
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.user_id
                goto L_0x0196
            L_0x018f:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.chat_id
                int r2 = -r2
            L_0x0196:
                long r2 = (long) r2
            L_0x0197:
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                java.lang.String r5 = "custom_"
                r4.append(r5)
                r4.append(r2)
                java.lang.String r4 = r4.toString()
                boolean r4 = r1.getBoolean(r4, r9)
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                r5.<init>()
                java.lang.String r6 = "notify2_"
                r5.append(r6)
                r5.append(r2)
                java.lang.String r5 = r5.toString()
                boolean r5 = r1.contains(r5)
                java.lang.StringBuilder r8 = new java.lang.StringBuilder
                r8.<init>()
                r8.append(r6)
                r8.append(r2)
                java.lang.String r6 = r8.toString()
                int r6 = r1.getInt(r6, r9)
                java.lang.StringBuilder r8 = new java.lang.StringBuilder
                r8.<init>()
                java.lang.String r11 = "notifyuntil_"
                r8.append(r11)
                r8.append(r2)
                java.lang.String r8 = r8.toString()
                int r1 = r1.getInt(r8, r9)
                r8 = 3
                if (r6 != r8) goto L_0x0271
                r8 = 2147483647(0x7fffffff, float:NaN)
                if (r1 == r8) goto L_0x0271
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.ConnectionsManager r2 = r2.getConnectionsManager()
                int r2 = r2.getCurrentTime()
                int r1 = r1 - r2
                if (r1 > 0) goto L_0x0216
                if (r4 == 0) goto L_0x020a
                r1 = 2131626255(0x7f0e090f, float:1.8879741E38)
                java.lang.String r2 = "NotificationsCustom"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                goto L_0x0213
            L_0x020a:
                r1 = 2131626283(0x7f0e092b, float:1.8879798E38)
                java.lang.String r2 = "NotificationsOn"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            L_0x0213:
                r7 = r1
                goto L_0x02a6
            L_0x0216:
                r2 = 3600(0xe10, float:5.045E-42)
                r3 = 2131627828(0x7f0e0var_, float:1.8882931E38)
                java.lang.String r4 = "WillUnmuteIn"
                if (r1 >= r2) goto L_0x0232
                java.lang.Object[] r2 = new java.lang.Object[r10]
                int r1 = r1 / 60
                java.lang.String r5 = "Minutes"
                java.lang.String r1 = org.telegram.messenger.LocaleController.formatPluralString(r5, r1)
                r2[r9] = r1
                java.lang.String r7 = org.telegram.messenger.LocaleController.formatString(r4, r3, r2)
            L_0x022f:
                r10 = 0
                goto L_0x02a6
            L_0x0232:
                r2 = 86400(0x15180, float:1.21072E-40)
                r5 = 1114636288(0x42700000, float:60.0)
                if (r1 >= r2) goto L_0x0251
                java.lang.Object[] r2 = new java.lang.Object[r10]
                float r1 = (float) r1
                float r1 = r1 / r5
                float r1 = r1 / r5
                double r5 = (double) r1
                double r5 = java.lang.Math.ceil(r5)
                int r1 = (int) r5
                java.lang.String r5 = "Hours"
                java.lang.String r1 = org.telegram.messenger.LocaleController.formatPluralString(r5, r1)
                r2[r9] = r1
                java.lang.String r7 = org.telegram.messenger.LocaleController.formatString(r4, r3, r2)
                goto L_0x022f
            L_0x0251:
                r2 = 31536000(0x1e13380, float:8.2725845E-38)
                if (r1 >= r2) goto L_0x022f
                java.lang.Object[] r2 = new java.lang.Object[r10]
                float r1 = (float) r1
                float r1 = r1 / r5
                float r1 = r1 / r5
                r5 = 1103101952(0x41CLASSNAME, float:24.0)
                float r1 = r1 / r5
                double r5 = (double) r1
                double r5 = java.lang.Math.ceil(r5)
                int r1 = (int) r5
                java.lang.String r5 = "Days"
                java.lang.String r1 = org.telegram.messenger.LocaleController.formatPluralString(r5, r1)
                r2[r9] = r1
                java.lang.String r7 = org.telegram.messenger.LocaleController.formatString(r4, r3, r2)
                goto L_0x022f
            L_0x0271:
                if (r6 != 0) goto L_0x0281
                if (r5 == 0) goto L_0x0276
                goto L_0x0285
            L_0x0276:
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.messenger.NotificationsController r1 = r1.getNotificationsController()
                boolean r10 = r1.isGlobalNotificationsEnabled((long) r2)
                goto L_0x0285
            L_0x0281:
                if (r6 != r10) goto L_0x0284
                goto L_0x0285
            L_0x0284:
                r10 = 0
            L_0x0285:
                if (r10 == 0) goto L_0x0293
                if (r4 == 0) goto L_0x0293
                r1 = 2131626255(0x7f0e090f, float:1.8879741E38)
                java.lang.String r2 = "NotificationsCustom"
                java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r2, r1)
                goto L_0x02a6
            L_0x0293:
                if (r10 == 0) goto L_0x029b
                r1 = 2131626283(0x7f0e092b, float:1.8879798E38)
                java.lang.String r2 = "NotificationsOn"
                goto L_0x02a0
            L_0x029b:
                r1 = 2131626281(0x7f0e0929, float:1.8879794E38)
                java.lang.String r2 = "NotificationsOff"
            L_0x02a0:
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                goto L_0x0213
            L_0x02a6:
                if (r7 != 0) goto L_0x02b1
                r1 = 2131626281(0x7f0e0929, float:1.8879794E38)
                java.lang.String r2 = "NotificationsOff"
                java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r2, r1)
            L_0x02b1:
                r1 = 2131626249(0x7f0e0909, float:1.8879729E38)
                java.lang.String r2 = "Notifications"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setTextAndValueAndCheck(r1, r7, r10, r9)
                goto L_0x0a0e
            L_0x02bf:
                android.view.View r0 = r13.itemView
                org.telegram.ui.Cells.TextCell r0 = (org.telegram.ui.Cells.TextCell) r0
                java.lang.String r2 = "windowBackgroundWhiteGrayIcon"
                java.lang.String r5 = "windowBackgroundWhiteBlackText"
                r0.setColors(r2, r5)
                r0.setTag(r5)
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.settingsTimerRow
                r5 = 32
                if (r14 != r2) goto L_0x030f
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.messenger.MessagesController r1 = r1.getMessagesController()
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                long r2 = r2.dialog_id
                long r2 = r2 >> r5
                int r3 = (int) r2
                java.lang.Integer r2 = java.lang.Integer.valueOf(r3)
                org.telegram.tgnet.TLRPC$EncryptedChat r1 = r1.getEncryptedChat(r2)
                int r1 = r1.ttl
                if (r1 != 0) goto L_0x02fd
                r1 = 2131627213(0x7f0e0ccd, float:1.8881684E38)
                java.lang.String r2 = "ShortMessageLifetimeForever"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                goto L_0x0301
            L_0x02fd:
                java.lang.String r1 = org.telegram.messenger.LocaleController.formatTTLString(r1)
            L_0x0301:
                r2 = 2131625925(0x7f0e07c5, float:1.8879072E38)
                java.lang.String r3 = "MessageLifetime"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r0.setTextAndValue(r2, r1, r9)
                goto L_0x0a0e
            L_0x030f:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.unblockRow
                java.lang.String r6 = "windowBackgroundWhiteRedText5"
                if (r14 != r2) goto L_0x032b
                r1 = 2131627498(0x7f0e0dea, float:1.8882262E38)
                java.lang.String r2 = "Unblock"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1, r9)
                r0.setColors(r7, r6)
                goto L_0x0a0e
            L_0x032b:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.settingsKeyRow
                if (r14 != r2) goto L_0x035f
                org.telegram.ui.Components.IdenticonDrawable r1 = new org.telegram.ui.Components.IdenticonDrawable
                r1.<init>()
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.messenger.MessagesController r2 = r2.getMessagesController()
                org.telegram.ui.ProfileActivity r3 = org.telegram.ui.ProfileActivity.this
                long r3 = r3.dialog_id
                long r3 = r3 >> r5
                int r4 = (int) r3
                java.lang.Integer r3 = java.lang.Integer.valueOf(r4)
                org.telegram.tgnet.TLRPC$EncryptedChat r2 = r2.getEncryptedChat(r3)
                r1.setEncryptedChat(r2)
                r2 = 2131625214(0x7f0e04fe, float:1.887763E38)
                java.lang.String r3 = "EncryptionKey"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r0.setTextAndValueDrawable(r2, r1, r9)
                goto L_0x0a0e
            L_0x035f:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.joinRow
                if (r14 != r2) goto L_0x0393
                java.lang.String r1 = "windowBackgroundWhiteBlueText2"
                r0.setColors(r7, r1)
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$Chat r1 = r1.currentChat
                boolean r1 = r1.megagroup
                if (r1 == 0) goto L_0x0385
                r1 = 2131626802(0x7f0e0b32, float:1.888085E38)
                java.lang.String r2 = "ProfileJoinGroup"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1, r9)
                goto L_0x0a0e
            L_0x0385:
                r1 = 2131626801(0x7f0e0b31, float:1.8880848E38)
                java.lang.String r2 = "ProfileJoinChannel"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1, r9)
                goto L_0x0a0e
            L_0x0393:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.subscribersRow
                java.lang.String r5 = "%d"
                if (r14 != r2) goto L_0x0452
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r2 = r2.chatInfo
                r6 = 2131165256(0x7var_, float:1.7944724E38)
                if (r2 == 0) goto L_0x0411
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$Chat r2 = r2.currentChat
                boolean r2 = org.telegram.messenger.ChatObject.isChannel(r2)
                if (r2 == 0) goto L_0x03ea
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$Chat r2 = r2.currentChat
                boolean r2 = r2.megagroup
                if (r2 != 0) goto L_0x03ea
                r2 = 2131624709(0x7f0e0305, float:1.8876605E38)
                java.lang.String r3 = "ChannelSubscribers"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                java.lang.Object[] r3 = new java.lang.Object[r10]
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r4 = r4.chatInfo
                int r4 = r4.participants_count
                java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
                r3[r9] = r4
                java.lang.String r3 = java.lang.String.format(r5, r3)
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                int r4 = r4.membersSectionRow
                int r4 = r4 - r10
                if (r14 == r4) goto L_0x03e5
                r9 = 1
            L_0x03e5:
                r0.setTextAndValueAndIcon(r2, r3, r6, r9)
                goto L_0x0a0e
            L_0x03ea:
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r4, r3)
                java.lang.Object[] r3 = new java.lang.Object[r10]
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r4 = r4.chatInfo
                int r4 = r4.participants_count
                java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
                r3[r9] = r4
                java.lang.String r3 = java.lang.String.format(r5, r3)
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                int r4 = r4.membersSectionRow
                int r4 = r4 - r10
                if (r14 == r4) goto L_0x040c
                r9 = 1
            L_0x040c:
                r0.setTextAndValueAndIcon(r2, r3, r6, r9)
                goto L_0x0a0e
            L_0x0411:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$Chat r2 = r2.currentChat
                boolean r2 = org.telegram.messenger.ChatObject.isChannel(r2)
                if (r2 == 0) goto L_0x043f
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$Chat r2 = r2.currentChat
                boolean r2 = r2.megagroup
                if (r2 != 0) goto L_0x043f
                r2 = 2131624709(0x7f0e0305, float:1.8876605E38)
                java.lang.String r3 = "ChannelSubscribers"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                org.telegram.ui.ProfileActivity r3 = org.telegram.ui.ProfileActivity.this
                int r3 = r3.membersSectionRow
                int r3 = r3 - r10
                if (r14 == r3) goto L_0x043a
                r9 = 1
            L_0x043a:
                r0.setTextAndIcon((java.lang.String) r2, (int) r6, (boolean) r9)
                goto L_0x0a0e
            L_0x043f:
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r4, r3)
                org.telegram.ui.ProfileActivity r3 = org.telegram.ui.ProfileActivity.this
                int r3 = r3.membersSectionRow
                int r3 = r3 - r10
                if (r14 == r3) goto L_0x044d
                r9 = 1
            L_0x044d:
                r0.setTextAndIcon((java.lang.String) r2, (int) r6, (boolean) r9)
                goto L_0x0a0e
            L_0x0452:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.administratorsRow
                if (r14 != r2) goto L_0x04ac
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r2 = r2.chatInfo
                if (r2 == 0) goto L_0x0491
                r2 = 2131624627(0x7f0e02b3, float:1.887644E38)
                java.lang.String r3 = "ChannelAdministrators"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                java.lang.Object[] r3 = new java.lang.Object[r10]
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r4 = r4.chatInfo
                int r4 = r4.admins_count
                java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
                r3[r9] = r4
                java.lang.String r3 = java.lang.String.format(r5, r3)
                r4 = 2131165247(0x7var_f, float:1.7944706E38)
                org.telegram.ui.ProfileActivity r5 = org.telegram.ui.ProfileActivity.this
                int r5 = r5.membersSectionRow
                int r5 = r5 - r10
                if (r14 == r5) goto L_0x048c
                r9 = 1
            L_0x048c:
                r0.setTextAndValueAndIcon(r2, r3, r4, r9)
                goto L_0x0a0e
            L_0x0491:
                r2 = 2131624627(0x7f0e02b3, float:1.887644E38)
                java.lang.String r3 = "ChannelAdministrators"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r3 = 2131165247(0x7var_f, float:1.7944706E38)
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                int r4 = r4.membersSectionRow
                int r4 = r4 - r10
                if (r14 == r4) goto L_0x04a7
                r9 = 1
            L_0x04a7:
                r0.setTextAndIcon((java.lang.String) r2, (int) r3, (boolean) r9)
                goto L_0x0a0e
            L_0x04ac:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.blockedUsersRow
                if (r14 != r2) goto L_0x0512
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r2 = r2.chatInfo
                if (r2 == 0) goto L_0x04f7
                r2 = 2131624632(0x7f0e02b8, float:1.887645E38)
                java.lang.String r3 = "ChannelBlacklist"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                java.lang.Object[] r3 = new java.lang.Object[r10]
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r4 = r4.chatInfo
                int r4 = r4.banned_count
                org.telegram.ui.ProfileActivity r6 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r6 = r6.chatInfo
                int r6 = r6.kicked_count
                int r4 = java.lang.Math.max(r4, r6)
                java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
                r3[r9] = r4
                java.lang.String r3 = java.lang.String.format(r5, r3)
                r4 = 2131165254(0x7var_, float:1.794472E38)
                org.telegram.ui.ProfileActivity r5 = org.telegram.ui.ProfileActivity.this
                int r5 = r5.membersSectionRow
                int r5 = r5 - r10
                if (r14 == r5) goto L_0x04f2
                r9 = 1
            L_0x04f2:
                r0.setTextAndValueAndIcon(r2, r3, r4, r9)
                goto L_0x0a0e
            L_0x04f7:
                r2 = 2131624632(0x7f0e02b8, float:1.887645E38)
                java.lang.String r3 = "ChannelBlacklist"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r3 = 2131165254(0x7var_, float:1.794472E38)
                org.telegram.ui.ProfileActivity r4 = org.telegram.ui.ProfileActivity.this
                int r4 = r4.membersSectionRow
                int r4 = r4 - r10
                if (r14 == r4) goto L_0x050d
                r9 = 1
            L_0x050d:
                r0.setTextAndIcon((java.lang.String) r2, (int) r3, (boolean) r9)
                goto L_0x0a0e
            L_0x0512:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.addMemberRow
                if (r14 != r2) goto L_0x053d
                java.lang.String r1 = "windowBackgroundWhiteBlueIcon"
                java.lang.String r2 = "windowBackgroundWhiteBlueButton"
                r0.setColors(r1, r2)
                r1 = 2131624182(0x7f0e00f6, float:1.8875536E38)
                java.lang.String r2 = "AddMember"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165249(0x7var_, float:1.794471E38)
                org.telegram.ui.ProfileActivity r3 = org.telegram.ui.ProfileActivity.this
                int r3 = r3.membersSectionRow
                if (r3 != r8) goto L_0x0538
                r9 = 1
            L_0x0538:
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r9)
                goto L_0x0a0e
            L_0x053d:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.sendMessageRow
                if (r14 != r2) goto L_0x0553
                r1 = 2131627096(0x7f0e0CLASSNAME, float:1.8881447E38)
                java.lang.String r2 = "SendMessageLocation"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1, r10)
                goto L_0x0a0e
            L_0x0553:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.reportRow
                if (r14 != r2) goto L_0x056c
                r1 = 2131626932(0x7f0e0bb4, float:1.8881114E38)
                java.lang.String r2 = "ReportUserLocation"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1, r9)
                r0.setColors(r7, r6)
                goto L_0x0a0e
            L_0x056c:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.languageRow
                if (r14 != r2) goto L_0x0585
                r1 = 2131625727(0x7f0e06ff, float:1.887867E38)
                java.lang.String r2 = "Language"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165662(0x7var_de, float:1.7945547E38)
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r9)
                goto L_0x0a0e
            L_0x0585:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.notificationRow
                if (r14 != r2) goto L_0x059e
                r1 = 2131626251(0x7f0e090b, float:1.8879733E38)
                java.lang.String r2 = "NotificationsAndSounds"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165670(0x7var_e6, float:1.7945564E38)
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r10)
                goto L_0x0a0e
            L_0x059e:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.privacyRow
                if (r14 != r2) goto L_0x05b7
                r1 = 2131626797(0x7f0e0b2d, float:1.888084E38)
                java.lang.String r2 = "PrivacySettings"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165680(0x7var_f0, float:1.7945584E38)
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r10)
                goto L_0x0a0e
            L_0x05b7:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.dataRow
                if (r14 != r2) goto L_0x05d0
                r1 = 2131624977(0x7f0e0411, float:1.8877149E38)
                java.lang.String r2 = "DataSettings"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165645(0x7var_cd, float:1.7945513E38)
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r10)
                goto L_0x0a0e
            L_0x05d0:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.chatRow
                if (r14 != r2) goto L_0x05e9
                r1 = 2131624766(0x7f0e033e, float:1.887672E38)
                java.lang.String r2 = "ChatSettings"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165637(0x7var_c5, float:1.7945497E38)
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r10)
                goto L_0x0a0e
            L_0x05e9:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.filtersRow
                if (r14 != r2) goto L_0x0602
                r1 = 2131625453(0x7f0e05ed, float:1.8878114E38)
                java.lang.String r2 = "Filters"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165649(0x7var_d1, float:1.7945521E38)
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r10)
                goto L_0x0a0e
            L_0x0602:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.questionRow
                if (r14 != r2) goto L_0x061b
                r1 = 2131624346(0x7f0e019a, float:1.887587E38)
                java.lang.String r2 = "AskAQuestion"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165692(0x7var_fc, float:1.7945608E38)
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r10)
                goto L_0x0a0e
            L_0x061b:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.faqRow
                if (r14 != r2) goto L_0x0634
                r1 = 2131627364(0x7f0e0d64, float:1.888199E38)
                java.lang.String r2 = "TelegramFAQ"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165654(0x7var_d6, float:1.7945531E38)
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r10)
                goto L_0x0a0e
            L_0x0634:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.policyRow
                if (r14 != r2) goto L_0x064d
                r1 = 2131626790(0x7f0e0b26, float:1.8880826E38)
                java.lang.String r2 = "PrivacyPolicy"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165674(0x7var_ea, float:1.7945572E38)
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r9)
                goto L_0x0a0e
            L_0x064d:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.sendLogsRow
                if (r14 != r2) goto L_0x0663
                r1 = 2131625009(0x7f0e0431, float:1.8877214E38)
                java.lang.String r2 = "DebugSendLogs"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1, r10)
                goto L_0x0a0e
            L_0x0663:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.clearLogsRow
                if (r14 != r2) goto L_0x0682
                r1 = 2131624992(0x7f0e0420, float:1.887718E38)
                java.lang.String r2 = "DebugClearLogs"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.switchBackendRow
                if (r2 == r8) goto L_0x067d
                r9 = 1
            L_0x067d:
                r0.setText(r1, r9)
                goto L_0x0a0e
            L_0x0682:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.switchBackendRow
                if (r14 != r2) goto L_0x0691
                java.lang.String r1 = "Switch Backend"
                r0.setText(r1, r9)
                goto L_0x0a0e
            L_0x0691:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.devicesRow
                if (r14 != r2) goto L_0x06aa
                r1 = 2131625079(0x7f0e0477, float:1.8877356E38)
                java.lang.String r2 = "Devices"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165647(0x7var_cf, float:1.7945517E38)
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r10)
                goto L_0x0a0e
            L_0x06aa:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.setAvatarRow
                if (r14 != r2) goto L_0x0a0e
                java.lang.String r1 = "windowBackgroundWhiteBlueIcon"
                java.lang.String r2 = "windowBackgroundWhiteBlueButton"
                r0.setColors(r1, r2)
                r1 = 2131627138(0x7f0e0CLASSNAME, float:1.8881532E38)
                java.lang.String r2 = "SetProfilePhoto"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r2 = 2131165710(0x7var_e, float:1.7945645E38)
                r0.setTextAndIcon((java.lang.String) r1, (int) r2, (boolean) r9)
                goto L_0x0a0e
            L_0x06cc:
                android.view.View r0 = r13.itemView
                org.telegram.ui.Cells.AboutLinkCell r0 = (org.telegram.ui.Cells.AboutLinkCell) r0
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.userInfoRow
                if (r14 != r2) goto L_0x06ef
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$UserFull r1 = r1.userInfo
                java.lang.String r1 = r1.about
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r6, r5)
                org.telegram.ui.ProfileActivity r3 = org.telegram.ui.ProfileActivity.this
                boolean r3 = r3.isBot
                r0.setTextAndValue(r1, r2, r3)
                goto L_0x0a0e
            L_0x06ef:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.channelInfoRow
                if (r14 != r2) goto L_0x0a0e
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r1 = r1.chatInfo
                java.lang.String r1 = r1.about
            L_0x06ff:
                java.lang.String r2 = "\n\n\n"
                boolean r3 = r1.contains(r2)
                if (r3 == 0) goto L_0x070e
                java.lang.String r3 = "\n\n"
                java.lang.String r1 = r1.replace(r2, r3)
                goto L_0x06ff
            L_0x070e:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$Chat r2 = r2.currentChat
                boolean r2 = org.telegram.messenger.ChatObject.isChannel(r2)
                if (r2 == 0) goto L_0x0725
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$Chat r2 = r2.currentChat
                boolean r2 = r2.megagroup
                if (r2 != 0) goto L_0x0725
                r9 = 1
            L_0x0725:
                r0.setText(r1, r9)
                goto L_0x0a0e
            L_0x072a:
                android.view.View r0 = r13.itemView
                org.telegram.ui.Cells.TextDetailCell r0 = (org.telegram.ui.Cells.TextDetailCell) r0
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.phoneRow
                java.lang.String r3 = "+"
                if (r14 != r2) goto L_0x0785
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.messenger.MessagesController r1 = r1.getMessagesController()
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.user_id
                java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
                org.telegram.tgnet.TLRPC$User r1 = r1.getUser(r2)
                java.lang.String r2 = r1.phone
                boolean r2 = android.text.TextUtils.isEmpty(r2)
                if (r2 != 0) goto L_0x076e
                org.telegram.PhoneFormat.PhoneFormat r2 = org.telegram.PhoneFormat.PhoneFormat.getInstance()
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                r4.append(r3)
                java.lang.String r1 = r1.phone
                r4.append(r1)
                java.lang.String r1 = r4.toString()
                java.lang.String r1 = r2.format(r1)
                goto L_0x0777
            L_0x076e:
                r1 = 2131626664(0x7f0e0aa8, float:1.888057E38)
                java.lang.String r2 = "PhoneHidden"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            L_0x0777:
                r2 = 2131626667(0x7f0e0aab, float:1.8880577E38)
                java.lang.String r3 = "PhoneMobile"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r0.setTextAndValue(r1, r2, r9)
                goto L_0x0a0e
            L_0x0785:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.usernameRow
                r4 = 2131627595(0x7f0e0e4b, float:1.8882459E38)
                java.lang.String r8 = "Username"
                if (r14 != r2) goto L_0x081f
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                int r1 = r1.user_id
                if (r1 == 0) goto L_0x07d7
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.messenger.MessagesController r1 = r1.getMessagesController()
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.user_id
                java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
                org.telegram.tgnet.TLRPC$User r1 = r1.getUser(r2)
                if (r1 == 0) goto L_0x07cc
                java.lang.String r2 = r1.username
                boolean r2 = android.text.TextUtils.isEmpty(r2)
                if (r2 != 0) goto L_0x07cc
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "@"
                r2.append(r3)
                java.lang.String r1 = r1.username
                r2.append(r1)
                java.lang.String r1 = r2.toString()
                goto L_0x07ce
            L_0x07cc:
                java.lang.String r1 = "-"
            L_0x07ce:
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r8, r4)
                r0.setTextAndValue(r1, r2, r9)
                goto L_0x0a0e
            L_0x07d7:
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$Chat r1 = r1.currentChat
                if (r1 == 0) goto L_0x0a0e
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.messenger.MessagesController r1 = r1.getMessagesController()
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.chat_id
                java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
                org.telegram.tgnet.TLRPC$Chat r1 = r1.getChat(r2)
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                org.telegram.ui.ProfileActivity r3 = org.telegram.ui.ProfileActivity.this
                org.telegram.messenger.MessagesController r3 = r3.getMessagesController()
                java.lang.String r3 = r3.linkPrefix
                r2.append(r3)
                java.lang.String r3 = "/"
                r2.append(r3)
                java.lang.String r1 = r1.username
                r2.append(r1)
                java.lang.String r1 = r2.toString()
                r2 = 2131625682(0x7f0e06d2, float:1.8878579E38)
                java.lang.String r3 = "InviteLink"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r0.setTextAndValue(r1, r2, r9)
                goto L_0x0a0e
            L_0x081f:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.locationRow
                if (r14 != r2) goto L_0x0855
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r1 = r1.chatInfo
                if (r1 == 0) goto L_0x0a0e
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r1 = r1.chatInfo
                org.telegram.tgnet.TLRPC$ChannelLocation r1 = r1.location
                boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_channelLocation
                if (r1 == 0) goto L_0x0a0e
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$ChatFull r1 = r1.chatInfo
                org.telegram.tgnet.TLRPC$ChannelLocation r1 = r1.location
                org.telegram.tgnet.TLRPC$TL_channelLocation r1 = (org.telegram.tgnet.TLRPC$TL_channelLocation) r1
                java.lang.String r1 = r1.address
                r2 = 2131624365(0x7f0e01ad, float:1.8875908E38)
                java.lang.String r3 = "AttachLocation"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r0.setTextAndValue(r1, r2, r9)
                goto L_0x0a0e
            L_0x0855:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.numberRow
                if (r14 != r2) goto L_0x08ab
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                int r1 = r1.currentAccount
                org.telegram.messenger.UserConfig r1 = org.telegram.messenger.UserConfig.getInstance(r1)
                org.telegram.tgnet.TLRPC$User r1 = r1.getCurrentUser()
                if (r1 == 0) goto L_0x0891
                java.lang.String r2 = r1.phone
                if (r2 == 0) goto L_0x0891
                int r2 = r2.length()
                if (r2 == 0) goto L_0x0891
                org.telegram.PhoneFormat.PhoneFormat r2 = org.telegram.PhoneFormat.PhoneFormat.getInstance()
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                r4.append(r3)
                java.lang.String r1 = r1.phone
                r4.append(r1)
                java.lang.String r1 = r4.toString()
                java.lang.String r1 = r2.format(r1)
                goto L_0x089a
            L_0x0891:
                r1 = 2131626302(0x7f0e093e, float:1.8879836E38)
                java.lang.String r2 = "NumberUnknown"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            L_0x089a:
                r2 = 2131627351(0x7f0e0d57, float:1.8881964E38)
                java.lang.String r3 = "TapToChangePhone"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r0.setTextAndValue(r1, r2, r10)
                r0.setContentDescriptionValueFirst(r9)
                goto L_0x0a0e
            L_0x08ab:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.setUsernameRow
                if (r14 != r2) goto L_0x08f4
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                int r1 = r1.currentAccount
                org.telegram.messenger.UserConfig r1 = org.telegram.messenger.UserConfig.getInstance(r1)
                org.telegram.tgnet.TLRPC$User r1 = r1.getCurrentUser()
                if (r1 == 0) goto L_0x08df
                java.lang.String r2 = r1.username
                boolean r2 = android.text.TextUtils.isEmpty(r2)
                if (r2 != 0) goto L_0x08df
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "@"
                r2.append(r3)
                java.lang.String r1 = r1.username
                r2.append(r1)
                java.lang.String r1 = r2.toString()
                goto L_0x08e8
            L_0x08df:
                r1 = 2131627598(0x7f0e0e4e, float:1.8882465E38)
                java.lang.String r2 = "UsernameEmpty"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            L_0x08e8:
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r8, r4)
                r0.setTextAndValue(r1, r2, r10)
                r0.setContentDescriptionValueFirst(r10)
                goto L_0x0a0e
            L_0x08f4:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.bioRow
                if (r14 != r2) goto L_0x0a0e
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$UserFull r1 = r1.userInfo
                if (r1 == 0) goto L_0x092d
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$UserFull r1 = r1.userInfo
                java.lang.String r1 = r1.about
                boolean r1 = android.text.TextUtils.isEmpty(r1)
                if (r1 != 0) goto L_0x0913
                goto L_0x092d
            L_0x0913:
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r6, r5)
                r2 = 2131627558(0x7f0e0e26, float:1.8882384E38)
                java.lang.String r3 = "UserBioDetail"
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                r0.setTextAndValue(r1, r2, r9)
                r0.setContentDescriptionValueFirst(r9)
                org.telegram.ui.ProfileActivity r0 = org.telegram.ui.ProfileActivity.this
                java.lang.String unused = r0.currentBio = r7
                goto L_0x0a0e
            L_0x092d:
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$UserFull r1 = r1.userInfo
                if (r1 != 0) goto L_0x093f
                r1 = 2131625794(0x7f0e0742, float:1.8878806E38)
                java.lang.String r2 = "Loading"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                goto L_0x0947
            L_0x093f:
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$UserFull r1 = r1.userInfo
                java.lang.String r1 = r1.about
            L_0x0947:
                java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r6, r5)
                r0.setTextWithEmojiAndValue(r1, r2, r9)
                r0.setContentDescriptionValueFirst(r10)
                org.telegram.ui.ProfileActivity r0 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$UserFull r1 = r0.userInfo
                if (r1 == 0) goto L_0x0961
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$UserFull r1 = r1.userInfo
                java.lang.String r7 = r1.about
            L_0x0961:
                java.lang.String unused = r0.currentBio = r7
                goto L_0x0a0e
            L_0x0966:
                android.view.View r0 = r13.itemView
                org.telegram.ui.Cells.HeaderCell r0 = (org.telegram.ui.Cells.HeaderCell) r0
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.infoHeaderRow
                if (r14 != r2) goto L_0x09ab
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$Chat r1 = r1.currentChat
                boolean r1 = org.telegram.messenger.ChatObject.isChannel(r1)
                if (r1 == 0) goto L_0x099e
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                org.telegram.tgnet.TLRPC$Chat r1 = r1.currentChat
                boolean r1 = r1.megagroup
                if (r1 != 0) goto L_0x099e
                org.telegram.ui.ProfileActivity r1 = org.telegram.ui.ProfileActivity.this
                int r1 = r1.channelInfoRow
                if (r1 == r8) goto L_0x099e
                r1 = 2131626915(0x7f0e0ba3, float:1.888108E38)
                java.lang.String r2 = "ReportChatDescription"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1)
                goto L_0x0a0e
            L_0x099e:
                r1 = 2131625660(0x7f0e06bc, float:1.8878534E38)
                java.lang.String r2 = "Info"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1)
                goto L_0x0a0e
            L_0x09ab:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.membersHeaderRow
                if (r14 != r2) goto L_0x09bb
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r4, r3)
                r0.setText(r1)
                goto L_0x0a0e
            L_0x09bb:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.settingsSectionRow2
                if (r14 != r2) goto L_0x09d0
                r1 = 2131626985(0x7f0e0be9, float:1.8881222E38)
                java.lang.String r2 = "SETTINGS"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1)
                goto L_0x0a0e
            L_0x09d0:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.numberSectionRow
                if (r14 != r2) goto L_0x09e5
                r1 = 2131624073(0x7f0e0089, float:1.8875315E38)
                java.lang.String r2 = "Account"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1)
                goto L_0x0a0e
            L_0x09e5:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.helpHeaderRow
                if (r14 != r2) goto L_0x09fa
                r1 = 2131627153(0x7f0e0CLASSNAME, float:1.8881562E38)
                java.lang.String r2 = "SettingsHelp"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1)
                goto L_0x0a0e
            L_0x09fa:
                org.telegram.ui.ProfileActivity r2 = org.telegram.ui.ProfileActivity.this
                int r2 = r2.debugHeaderRow
                if (r14 != r2) goto L_0x0a0e
                r1 = 2131627151(0x7f0e0c8f, float:1.8881558E38)
                java.lang.String r2 = "SettingsDebug"
                java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
                r0.setText(r1)
            L_0x0a0e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ProfileActivity.ListAdapter.onBindViewHolder(androidx.recyclerview.widget.RecyclerView$ViewHolder, int):void");
        }

        public boolean isEnabled(RecyclerView.ViewHolder viewHolder) {
            if (ProfileActivity.this.notificationRow != -1) {
                int adapterPosition = viewHolder.getAdapterPosition();
                if (adapterPosition == ProfileActivity.this.notificationRow || adapterPosition == ProfileActivity.this.numberRow || adapterPosition == ProfileActivity.this.privacyRow || adapterPosition == ProfileActivity.this.languageRow || adapterPosition == ProfileActivity.this.setUsernameRow || adapterPosition == ProfileActivity.this.bioRow || adapterPosition == ProfileActivity.this.versionRow || adapterPosition == ProfileActivity.this.dataRow || adapterPosition == ProfileActivity.this.chatRow || adapterPosition == ProfileActivity.this.questionRow || adapterPosition == ProfileActivity.this.devicesRow || adapterPosition == ProfileActivity.this.filtersRow || adapterPosition == ProfileActivity.this.faqRow || adapterPosition == ProfileActivity.this.policyRow || adapterPosition == ProfileActivity.this.sendLogsRow || adapterPosition == ProfileActivity.this.clearLogsRow || adapterPosition == ProfileActivity.this.switchBackendRow || adapterPosition == ProfileActivity.this.setAvatarRow) {
                    return true;
                }
                return false;
            }
            int itemViewType = viewHolder.getItemViewType();
            if (itemViewType == 1 || itemViewType == 5 || itemViewType == 7 || itemViewType == 9 || itemViewType == 10 || itemViewType == 11 || itemViewType == 12 || itemViewType == 13) {
                return false;
            }
            return true;
        }

        public int getItemCount() {
            return ProfileActivity.this.rowCount;
        }

        public int getItemViewType(int i) {
            if (i == ProfileActivity.this.infoHeaderRow || i == ProfileActivity.this.membersHeaderRow || i == ProfileActivity.this.settingsSectionRow2 || i == ProfileActivity.this.numberSectionRow || i == ProfileActivity.this.helpHeaderRow || i == ProfileActivity.this.debugHeaderRow) {
                return 1;
            }
            if (i == ProfileActivity.this.phoneRow || i == ProfileActivity.this.usernameRow || i == ProfileActivity.this.locationRow || i == ProfileActivity.this.numberRow || i == ProfileActivity.this.setUsernameRow || i == ProfileActivity.this.bioRow) {
                return 2;
            }
            if (i == ProfileActivity.this.userInfoRow || i == ProfileActivity.this.channelInfoRow) {
                return 3;
            }
            if (i == ProfileActivity.this.settingsTimerRow || i == ProfileActivity.this.settingsKeyRow || i == ProfileActivity.this.reportRow || i == ProfileActivity.this.subscribersRow || i == ProfileActivity.this.administratorsRow || i == ProfileActivity.this.blockedUsersRow || i == ProfileActivity.this.addMemberRow || i == ProfileActivity.this.joinRow || i == ProfileActivity.this.unblockRow || i == ProfileActivity.this.sendMessageRow || i == ProfileActivity.this.notificationRow || i == ProfileActivity.this.privacyRow || i == ProfileActivity.this.languageRow || i == ProfileActivity.this.dataRow || i == ProfileActivity.this.chatRow || i == ProfileActivity.this.questionRow || i == ProfileActivity.this.devicesRow || i == ProfileActivity.this.filtersRow || i == ProfileActivity.this.faqRow || i == ProfileActivity.this.policyRow || i == ProfileActivity.this.sendLogsRow || i == ProfileActivity.this.clearLogsRow || i == ProfileActivity.this.switchBackendRow || i == ProfileActivity.this.setAvatarRow) {
                return 4;
            }
            if (i == ProfileActivity.this.notificationsDividerRow) {
                return 5;
            }
            if (i == ProfileActivity.this.notificationsRow) {
                return 6;
            }
            if (i == ProfileActivity.this.infoSectionRow || i == ProfileActivity.this.lastSectionRow || i == ProfileActivity.this.membersSectionRow || i == ProfileActivity.this.secretSettingsSectionRow || i == ProfileActivity.this.settingsSectionRow || i == ProfileActivity.this.devicesSectionRow || i == ProfileActivity.this.helpSectionCell || i == ProfileActivity.this.setAvatarSectionRow) {
                return 7;
            }
            if (i >= ProfileActivity.this.membersStartRow && i < ProfileActivity.this.membersEndRow) {
                return 8;
            }
            if (i == ProfileActivity.this.emptyRow) {
                return 11;
            }
            if (i == ProfileActivity.this.bottomPaddingRow) {
                return 12;
            }
            if (i == ProfileActivity.this.sharedMediaRow) {
                return 13;
            }
            return i == ProfileActivity.this.versionRow ? 14 : 0;
        }
    }

    private class SearchAdapter extends RecyclerListView.SelectionAdapter {
        /* access modifiers changed from: private */
        public ArrayList<MessagesController.FaqSearchResult> faqSearchArray;
        /* access modifiers changed from: private */
        public ArrayList<MessagesController.FaqSearchResult> faqSearchResults;
        /* access modifiers changed from: private */
        public TLRPC$WebPage faqWebPage;
        private String lastSearchString;
        private boolean loadingFaqPage;
        private Context mContext;
        /* access modifiers changed from: private */
        public ArrayList<Object> recentSearches;
        private ArrayList<CharSequence> resultNames;
        private SearchResult[] searchArray;
        /* access modifiers changed from: private */
        public ArrayList<SearchResult> searchResults;
        private Runnable searchRunnable;
        /* access modifiers changed from: private */
        public boolean searchWas;
        final /* synthetic */ ProfileActivity this$0;

        private class SearchResult {
            /* access modifiers changed from: private */
            public int guid;
            /* access modifiers changed from: private */
            public int iconResId;
            /* access modifiers changed from: private */
            public int num;
            private Runnable openRunnable;
            /* access modifiers changed from: private */
            public String[] path;
            private String rowName;
            /* access modifiers changed from: private */
            public String searchTitle;

            public SearchResult(SearchAdapter searchAdapter, int i, String str, int i2, Runnable runnable) {
                this(i, str, (String) null, (String) null, (String) null, i2, runnable);
            }

            public SearchResult(SearchAdapter searchAdapter, int i, String str, String str2, int i2, Runnable runnable) {
                this(i, str, (String) null, str2, (String) null, i2, runnable);
            }

            public SearchResult(SearchAdapter searchAdapter, int i, String str, String str2, String str3, int i2, Runnable runnable) {
                this(i, str, str2, str3, (String) null, i2, runnable);
            }

            public SearchResult(int i, String str, String str2, String str3, String str4, int i2, Runnable runnable) {
                this.guid = i;
                this.searchTitle = str;
                this.rowName = str2;
                this.openRunnable = runnable;
                this.iconResId = i2;
                if (str3 != null && str4 != null) {
                    this.path = new String[]{str3, str4};
                } else if (str3 != null) {
                    this.path = new String[]{str3};
                }
            }

            public boolean equals(Object obj) {
                if ((obj instanceof SearchResult) && this.guid == ((SearchResult) obj).guid) {
                    return true;
                }
                return false;
            }

            public String toString() {
                SerializedData serializedData = new SerializedData();
                serializedData.writeInt32(this.num);
                serializedData.writeInt32(1);
                serializedData.writeInt32(this.guid);
                return Utilities.bytesToHex(serializedData.toByteArray());
            }

            /* access modifiers changed from: private */
            public void open() {
                this.openRunnable.run();
                AndroidUtilities.scrollToFragmentRow(SearchAdapter.this.this$0.parentLayout, this.rowName);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$0 */
        public /* synthetic */ void lambda$new$0$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ChangeNameActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$1 */
        public /* synthetic */ void lambda$new$1$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ActionIntroActivity(3));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$2 */
        public /* synthetic */ void lambda$new$2$ProfileActivity$SearchAdapter() {
            int i = 0;
            while (true) {
                if (i >= 3) {
                    i = -1;
                    break;
                } else if (!UserConfig.getInstance(i).isClientActivated()) {
                    break;
                } else {
                    i++;
                }
            }
            if (i >= 0) {
                this.this$0.presentFragment(new LoginActivity(i));
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$3 */
        public /* synthetic */ void lambda$new$3$ProfileActivity$SearchAdapter() {
            if (this.this$0.userInfo != null) {
                this.this$0.presentFragment(new ChangeBioActivity());
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$4 */
        public /* synthetic */ void lambda$new$4$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new NotificationsSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$5 */
        public /* synthetic */ void lambda$new$5$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new NotificationsCustomSettingsActivity(1, new ArrayList(), true));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$6 */
        public /* synthetic */ void lambda$new$6$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new NotificationsCustomSettingsActivity(0, new ArrayList(), true));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$7 */
        public /* synthetic */ void lambda$new$7$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new NotificationsCustomSettingsActivity(2, new ArrayList(), true));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$8 */
        public /* synthetic */ void lambda$new$8$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new NotificationsSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$9 */
        public /* synthetic */ void lambda$new$9$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new NotificationsSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$10 */
        public /* synthetic */ void lambda$new$10$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new NotificationsSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$11 */
        public /* synthetic */ void lambda$new$11$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new NotificationsSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$12 */
        public /* synthetic */ void lambda$new$12$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new NotificationsSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$13 */
        public /* synthetic */ void lambda$new$13$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new NotificationsSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$14 */
        public /* synthetic */ void lambda$new$14$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacySettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$15 */
        public /* synthetic */ void lambda$new$15$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacyUsersActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$16 */
        public /* synthetic */ void lambda$new$16$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacyControlActivity(6, true));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$17 */
        public /* synthetic */ void lambda$new$17$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacyControlActivity(0, true));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$18 */
        public /* synthetic */ void lambda$new$18$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacyControlActivity(4, true));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$19 */
        public /* synthetic */ void lambda$new$19$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacyControlActivity(5, true));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$20 */
        public /* synthetic */ void lambda$new$20$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacyControlActivity(3, true));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$21 */
        public /* synthetic */ void lambda$new$21$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacyControlActivity(2, true));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$22 */
        public /* synthetic */ void lambda$new$22$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacyControlActivity(1, true));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$23 */
        public /* synthetic */ void lambda$new$23$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PasscodeActivity(SharedConfig.passcodeHash.length() > 0 ? 2 : 0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$24 */
        public /* synthetic */ void lambda$new$24$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new TwoStepVerificationActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$25 */
        public /* synthetic */ void lambda$new$25$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new SessionsActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$26 */
        public /* synthetic */ void lambda$new$26$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacySettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$27 */
        public /* synthetic */ void lambda$new$27$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacySettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$28 */
        public /* synthetic */ void lambda$new$28$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacySettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$29 */
        public /* synthetic */ void lambda$new$29$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new SessionsActivity(1));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$30 */
        public /* synthetic */ void lambda$new$30$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacySettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$31 */
        public /* synthetic */ void lambda$new$31$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacySettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$32 */
        public /* synthetic */ void lambda$new$32$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacySettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$33 */
        public /* synthetic */ void lambda$new$33$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacySettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$34 */
        public /* synthetic */ void lambda$new$34$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new PrivacySettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$35 */
        public /* synthetic */ void lambda$new$35$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new SessionsActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$36 */
        public /* synthetic */ void lambda$new$36$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$37 */
        public /* synthetic */ void lambda$new$37$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$38 */
        public /* synthetic */ void lambda$new$38$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new CacheControlActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$39 */
        public /* synthetic */ void lambda$new$39$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new CacheControlActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$40 */
        public /* synthetic */ void lambda$new$40$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new CacheControlActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$41 */
        public /* synthetic */ void lambda$new$41$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new CacheControlActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$42 */
        public /* synthetic */ void lambda$new$42$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataUsageActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$43 */
        public /* synthetic */ void lambda$new$43$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$44 */
        public /* synthetic */ void lambda$new$44$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataAutoDownloadActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$45 */
        public /* synthetic */ void lambda$new$45$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataAutoDownloadActivity(1));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$46 */
        public /* synthetic */ void lambda$new$46$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataAutoDownloadActivity(2));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$47 */
        public /* synthetic */ void lambda$new$47$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$48 */
        public /* synthetic */ void lambda$new$48$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$49 */
        public /* synthetic */ void lambda$new$49$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$50 */
        public /* synthetic */ void lambda$new$50$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$51 */
        public /* synthetic */ void lambda$new$51$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$52 */
        public /* synthetic */ void lambda$new$52$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$53 */
        public /* synthetic */ void lambda$new$53$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$54 */
        public /* synthetic */ void lambda$new$54$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$55 */
        public /* synthetic */ void lambda$new$55$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$56 */
        public /* synthetic */ void lambda$new$56$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ProxyListActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$57 */
        public /* synthetic */ void lambda$new$57$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ProxyListActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$58 */
        public /* synthetic */ void lambda$new$58$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new DataSettingsActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$59 */
        public /* synthetic */ void lambda$new$59$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ThemeActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$60 */
        public /* synthetic */ void lambda$new$60$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ThemeActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$61 */
        public /* synthetic */ void lambda$new$61$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new WallpapersListActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$62 */
        public /* synthetic */ void lambda$new$62$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new WallpapersListActivity(1));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$63 */
        public /* synthetic */ void lambda$new$63$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new WallpapersListActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$64 */
        public /* synthetic */ void lambda$new$64$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ThemeActivity(1));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$65 */
        public /* synthetic */ void lambda$new$65$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ThemeActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$66 */
        public /* synthetic */ void lambda$new$66$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ThemeActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$67 */
        public /* synthetic */ void lambda$new$67$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ThemeActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$68 */
        public /* synthetic */ void lambda$new$68$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ThemeActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$69 */
        public /* synthetic */ void lambda$new$69$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ThemeActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$70 */
        public /* synthetic */ void lambda$new$70$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ThemeActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$71 */
        public /* synthetic */ void lambda$new$71$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ThemeActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$72 */
        public /* synthetic */ void lambda$new$72$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ThemeActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$73 */
        public /* synthetic */ void lambda$new$73$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new StickersActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$74 */
        public /* synthetic */ void lambda$new$74$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new StickersActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$75 */
        public /* synthetic */ void lambda$new$75$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new FeaturedStickersActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$76 */
        public /* synthetic */ void lambda$new$76$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new StickersActivity(1));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$77 */
        public /* synthetic */ void lambda$new$77$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ArchivedStickersActivity(0));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$78 */
        public /* synthetic */ void lambda$new$78$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new ArchivedStickersActivity(1));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$79 */
        public /* synthetic */ void lambda$new$79$ProfileActivity$SearchAdapter() {
            this.this$0.presentFragment(new LanguageSelectActivity());
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$80 */
        public /* synthetic */ void lambda$new$80$ProfileActivity$SearchAdapter() {
            ProfileActivity profileActivity = this.this$0;
            profileActivity.showDialog(AlertsCreator.createSupportAlert(profileActivity));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$81 */
        public /* synthetic */ void lambda$new$81$ProfileActivity$SearchAdapter() {
            Browser.openUrl((Context) this.this$0.getParentActivity(), LocaleController.getString("TelegramFaqUrl", NUM));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$82 */
        public /* synthetic */ void lambda$new$82$ProfileActivity$SearchAdapter() {
            Browser.openUrl((Context) this.this$0.getParentActivity(), LocaleController.getString("PrivacyPolicyUrl", NUM));
        }

        public SearchAdapter(ProfileActivity profileActivity, Context context) {
            SearchResult searchResult;
            String[] strArr;
            this.this$0 = profileActivity;
            SearchResult[] searchResultArr = new SearchResult[83];
            searchResultArr[0] = new SearchResult(this, 500, LocaleController.getString("EditName", NUM), 0, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$0$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[1] = new SearchResult(this, 501, LocaleController.getString("ChangePhoneNumber", NUM), 0, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$1$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[2] = new SearchResult(this, 502, LocaleController.getString("AddAnotherAccount", NUM), 0, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$2$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[3] = new SearchResult(this, 503, LocaleController.getString("UserBio", NUM), 0, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$3$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[4] = new SearchResult(this, 1, LocaleController.getString("NotificationsAndSounds", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$4$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[5] = new SearchResult(this, 2, LocaleController.getString("NotificationsPrivateChats", NUM), LocaleController.getString("NotificationsAndSounds", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$5$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[6] = new SearchResult(this, 3, LocaleController.getString("NotificationsGroups", NUM), LocaleController.getString("NotificationsAndSounds", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$6$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[7] = new SearchResult(this, 4, LocaleController.getString("NotificationsChannels", NUM), LocaleController.getString("NotificationsAndSounds", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$7$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[8] = new SearchResult(this, 5, LocaleController.getString("VoipNotificationSettings", NUM), "callsSectionRow", LocaleController.getString("NotificationsAndSounds", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$8$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[9] = new SearchResult(this, 6, LocaleController.getString("BadgeNumber", NUM), "badgeNumberSection", LocaleController.getString("NotificationsAndSounds", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$9$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[10] = new SearchResult(this, 7, LocaleController.getString("InAppNotifications", NUM), "inappSectionRow", LocaleController.getString("NotificationsAndSounds", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$10$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[11] = new SearchResult(this, 8, LocaleController.getString("ContactJoined", NUM), "contactJoinedRow", LocaleController.getString("NotificationsAndSounds", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$11$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[12] = new SearchResult(this, 9, LocaleController.getString("PinnedMessages", NUM), "pinnedMessageRow", LocaleController.getString("NotificationsAndSounds", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$12$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[13] = new SearchResult(this, 10, LocaleController.getString("ResetAllNotifications", NUM), "resetNotificationsRow", LocaleController.getString("NotificationsAndSounds", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$13$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[14] = new SearchResult(this, 100, LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$14$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[15] = new SearchResult(this, 101, LocaleController.getString("BlockedUsers", NUM), LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$15$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[16] = new SearchResult(this, 105, LocaleController.getString("PrivacyPhone", NUM), LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$16$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[17] = new SearchResult(this, 102, LocaleController.getString("PrivacyLastSeen", NUM), LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$17$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[18] = new SearchResult(this, 103, LocaleController.getString("PrivacyProfilePhoto", NUM), LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$18$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[19] = new SearchResult(this, 104, LocaleController.getString("PrivacyForwards", NUM), LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$19$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[20] = new SearchResult(this, 105, LocaleController.getString("PrivacyP2P", NUM), LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$20$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[21] = new SearchResult(this, 106, LocaleController.getString("Calls", NUM), LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$21$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[22] = new SearchResult(this, 107, LocaleController.getString("GroupsAndChannels", NUM), LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$22$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[23] = new SearchResult(this, 108, LocaleController.getString("Passcode", NUM), LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$23$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[24] = new SearchResult(this, 109, LocaleController.getString("TwoStepVerification", NUM), LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$24$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[25] = new SearchResult(this, 110, LocaleController.getString("SessionsTitle", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$25$ProfileActivity$SearchAdapter();
                }
            });
            if (profileActivity.getMessagesController().autoarchiveAvailable) {
                searchResult = new SearchResult(this, 121, LocaleController.getString("ArchiveAndMute", NUM), "newChatsRow", LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                    public final void run() {
                        ProfileActivity.SearchAdapter.this.lambda$new$26$ProfileActivity$SearchAdapter();
                    }
                });
            } else {
                searchResult = null;
            }
            searchResultArr[26] = searchResult;
            searchResultArr[27] = new SearchResult(this, 112, LocaleController.getString("DeleteAccountIfAwayFor2", NUM), "deleteAccountRow", LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$27$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[28] = new SearchResult(this, 113, LocaleController.getString("PrivacyPaymentsClear", NUM), "paymentsClearRow", LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$28$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[29] = new SearchResult(this, 114, LocaleController.getString("WebSessionsTitle", NUM), LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$29$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[30] = new SearchResult(this, 115, LocaleController.getString("SyncContactsDelete", NUM), "contactsDeleteRow", LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$30$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[31] = new SearchResult(this, 116, LocaleController.getString("SyncContacts", NUM), "contactsSyncRow", LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$31$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[32] = new SearchResult(this, 117, LocaleController.getString("SuggestContacts", NUM), "contactsSuggestRow", LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$32$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[33] = new SearchResult(this, 118, LocaleController.getString("MapPreviewProvider", NUM), "secretMapRow", LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$33$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[34] = new SearchResult(this, 119, LocaleController.getString("SecretWebPage", NUM), "secretWebpageRow", LocaleController.getString("PrivacySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$34$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[35] = new SearchResult(this, 120, LocaleController.getString("Devices", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$35$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[36] = new SearchResult(this, 200, LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$36$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[37] = new SearchResult(this, 201, LocaleController.getString("DataUsage", NUM), "usageSectionRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$37$ProfileActivity$SearchAdapter();
                }
            });
            String str = "StorageUsage";
            searchResultArr[38] = new SearchResult(this, 202, LocaleController.getString("StorageUsage", NUM), LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$38$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[39] = new SearchResult(203, LocaleController.getString("KeepMedia", NUM), "keepMediaRow", LocaleController.getString("DataSettings", NUM), LocaleController.getString(str, NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$39$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[40] = new SearchResult(204, LocaleController.getString("ClearMediaCache", NUM), "cacheRow", LocaleController.getString("DataSettings", NUM), LocaleController.getString(str, NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$40$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[41] = new SearchResult(205, LocaleController.getString("LocalDatabase", NUM), "databaseRow", LocaleController.getString("DataSettings", NUM), LocaleController.getString(str, NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$41$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[42] = new SearchResult(this, 206, LocaleController.getString("NetworkUsage", NUM), LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$42$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[43] = new SearchResult(this, 207, LocaleController.getString("AutomaticMediaDownload", NUM), "mediaDownloadSectionRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$43$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[44] = new SearchResult(this, 208, LocaleController.getString("WhenUsingMobileData", NUM), LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$44$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[45] = new SearchResult(this, 209, LocaleController.getString("WhenConnectedOnWiFi", NUM), LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$45$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[46] = new SearchResult(this, 210, LocaleController.getString("WhenRoaming", NUM), LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$46$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[47] = new SearchResult(this, 211, LocaleController.getString("ResetAutomaticMediaDownload", NUM), "resetDownloadRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$47$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[48] = new SearchResult(this, 212, LocaleController.getString("AutoplayMedia", NUM), "autoplayHeaderRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$48$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[49] = new SearchResult(this, 213, LocaleController.getString("AutoplayGIF", NUM), "autoplayGifsRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$49$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[50] = new SearchResult(this, 214, LocaleController.getString("AutoplayVideo", NUM), "autoplayVideoRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$50$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[51] = new SearchResult(this, 215, LocaleController.getString("Streaming", NUM), "streamSectionRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$51$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[52] = new SearchResult(this, 216, LocaleController.getString("EnableStreaming", NUM), "enableStreamRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$52$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[53] = new SearchResult(this, 217, LocaleController.getString("Calls", NUM), "callsSectionRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$53$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[54] = new SearchResult(this, 218, LocaleController.getString("VoipUseLessData", NUM), "useLessDataForCallsRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$54$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[55] = new SearchResult(this, 219, LocaleController.getString("VoipQuickReplies", NUM), "quickRepliesRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$55$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[56] = new SearchResult(this, 220, LocaleController.getString("ProxySettings", NUM), LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$56$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[57] = new SearchResult(221, LocaleController.getString("UseProxyForCalls", NUM), "callsRow", LocaleController.getString("DataSettings", NUM), LocaleController.getString("ProxySettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$57$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[58] = new SearchResult(this, 111, LocaleController.getString("PrivacyDeleteCloudDrafts", NUM), "clearDraftsRow", LocaleController.getString("DataSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$58$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[59] = new SearchResult(this, 300, LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$59$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[60] = new SearchResult(this, 301, LocaleController.getString("TextSizeHeader", NUM), "textSizeHeaderRow", LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$60$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[61] = new SearchResult(this, 302, LocaleController.getString("ChatBackground", NUM), LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$61$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[62] = new SearchResult(303, LocaleController.getString("SetColor", NUM), (String) null, LocaleController.getString("ChatSettings", NUM), LocaleController.getString("ChatBackground", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$62$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[63] = new SearchResult(304, LocaleController.getString("ResetChatBackgrounds", NUM), "resetRow", LocaleController.getString("ChatSettings", NUM), LocaleController.getString("ChatBackground", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$63$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[64] = new SearchResult(this, 305, LocaleController.getString("AutoNightTheme", NUM), LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$64$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[65] = new SearchResult(this, 306, LocaleController.getString("ColorTheme", NUM), "themeHeaderRow", LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$65$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[66] = new SearchResult(this, 307, LocaleController.getString("ChromeCustomTabs", NUM), "customTabsRow", LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$66$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[67] = new SearchResult(this, 308, LocaleController.getString("DirectShare", NUM), "directShareRow", LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$67$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[68] = new SearchResult(this, 309, LocaleController.getString("EnableAnimations", NUM), "enableAnimationsRow", LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$68$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[69] = new SearchResult(this, 310, LocaleController.getString("RaiseToSpeak", NUM), "raiseToSpeakRow", LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$69$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[70] = new SearchResult(this, 311, LocaleController.getString("SendByEnter", NUM), "sendByEnterRow", LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$70$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[71] = new SearchResult(this, 312, LocaleController.getString("SaveToGallerySettings", NUM), "saveToGalleryRow", LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$71$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[72] = new SearchResult(this, 312, LocaleController.getString("DistanceUnits", NUM), "distanceRow", LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$72$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[73] = new SearchResult(this, 313, LocaleController.getString("StickersAndMasks", NUM), LocaleController.getString("ChatSettings", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$73$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[74] = new SearchResult(314, LocaleController.getString("SuggestStickers", NUM), "suggestRow", LocaleController.getString("ChatSettings", NUM), LocaleController.getString("StickersAndMasks", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$74$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[75] = new SearchResult(315, LocaleController.getString("FeaturedStickers", NUM), (String) null, LocaleController.getString("ChatSettings", NUM), LocaleController.getString("StickersAndMasks", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$75$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[76] = new SearchResult(316, LocaleController.getString("Masks", NUM), (String) null, LocaleController.getString("ChatSettings", NUM), LocaleController.getString("StickersAndMasks", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$76$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[77] = new SearchResult(317, LocaleController.getString("ArchivedStickers", NUM), (String) null, LocaleController.getString("ChatSettings", NUM), LocaleController.getString("StickersAndMasks", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$77$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[78] = new SearchResult(317, LocaleController.getString("ArchivedMasks", NUM), (String) null, LocaleController.getString("ChatSettings", NUM), LocaleController.getString("StickersAndMasks", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$78$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[79] = new SearchResult(this, 400, LocaleController.getString("Language", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$79$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[80] = new SearchResult(this, 402, LocaleController.getString("AskAQuestion", NUM), LocaleController.getString("SettingsHelp", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$80$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[81] = new SearchResult(this, 403, LocaleController.getString("TelegramFAQ", NUM), LocaleController.getString("SettingsHelp", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$81$ProfileActivity$SearchAdapter();
                }
            });
            searchResultArr[82] = new SearchResult(this, 404, LocaleController.getString("PrivacyPolicy", NUM), LocaleController.getString("SettingsHelp", NUM), NUM, new Runnable() {
                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$new$82$ProfileActivity$SearchAdapter();
                }
            });
            this.searchArray = searchResultArr;
            this.faqSearchArray = new ArrayList<>();
            this.resultNames = new ArrayList<>();
            this.searchResults = new ArrayList<>();
            this.faqSearchResults = new ArrayList<>();
            this.recentSearches = new ArrayList<>();
            this.mContext = context;
            HashMap hashMap = new HashMap();
            int i = 0;
            while (true) {
                SearchResult[] searchResultArr2 = this.searchArray;
                if (i >= searchResultArr2.length) {
                    break;
                }
                if (searchResultArr2[i] != null) {
                    hashMap.put(Integer.valueOf(searchResultArr2[i].guid), this.searchArray[i]);
                }
                i++;
            }
            Set<String> stringSet = MessagesController.getGlobalMainSettings().getStringSet("settingsSearchRecent2", (Set) null);
            if (stringSet != null) {
                for (String hexToBytes : stringSet) {
                    try {
                        SerializedData serializedData = new SerializedData(Utilities.hexToBytes(hexToBytes));
                        boolean z = false;
                        int readInt32 = serializedData.readInt32(false);
                        int readInt322 = serializedData.readInt32(false);
                        if (readInt322 == 0) {
                            String readString = serializedData.readString(false);
                            int readInt323 = serializedData.readInt32(false);
                            if (readInt323 > 0) {
                                strArr = new String[readInt323];
                                int i2 = 0;
                                while (i2 < readInt323) {
                                    strArr[i2] = serializedData.readString(z);
                                    i2++;
                                    z = false;
                                }
                            } else {
                                strArr = null;
                            }
                            MessagesController.FaqSearchResult faqSearchResult = new MessagesController.FaqSearchResult(readString, strArr, serializedData.readString(z));
                            faqSearchResult.num = readInt32;
                            this.recentSearches.add(faqSearchResult);
                        } else if (readInt322 == 1) {
                            try {
                                SearchResult searchResult2 = (SearchResult) hashMap.get(Integer.valueOf(serializedData.readInt32(false)));
                                if (searchResult2 != null) {
                                    int unused = searchResult2.num = readInt32;
                                    this.recentSearches.add(searchResult2);
                                }
                            } catch (Exception unused2) {
                            }
                        }
                    } catch (Exception unused3) {
                    }
                }
            }
            Collections.sort(this.recentSearches, new Object() {
                public final int compare(Object obj, Object obj2) {
                    return ProfileActivity.SearchAdapter.this.lambda$new$83$ProfileActivity$SearchAdapter(obj, obj2);
                }

                public /* synthetic */ java.util.Comparator reversed() {
                    return Comparator.CC.$default$reversed(this);
                }

                public /* synthetic */ java.util.Comparator thenComparing(Function function) {
                    return Comparator.CC.$default$thenComparing((java.util.Comparator) this, function);
                }

                public /* synthetic */ java.util.Comparator thenComparing(Function function, java.util.Comparator comparator) {
                    return Comparator.CC.$default$thenComparing(this, function, comparator);
                }

                public /* synthetic */ java.util.Comparator thenComparing(java.util.Comparator comparator) {
                    return Comparator.CC.$default$thenComparing((java.util.Comparator) this, comparator);
                }

                public /* synthetic */ java.util.Comparator thenComparingDouble(ToDoubleFunction toDoubleFunction) {
                    return Comparator.CC.$default$thenComparingDouble(this, toDoubleFunction);
                }

                public /* synthetic */ java.util.Comparator thenComparingInt(ToIntFunction toIntFunction) {
                    return Comparator.CC.$default$thenComparingInt(this, toIntFunction);
                }

                public /* synthetic */ java.util.Comparator thenComparingLong(ToLongFunction toLongFunction) {
                    return Comparator.CC.$default$thenComparingLong(this, toLongFunction);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$83 */
        public /* synthetic */ int lambda$new$83$ProfileActivity$SearchAdapter(Object obj, Object obj2) {
            int num = getNum(obj);
            int num2 = getNum(obj2);
            if (num < num2) {
                return -1;
            }
            return num > num2 ? 1 : 0;
        }

        /* access modifiers changed from: private */
        public void loadFaqWebPage() {
            TLRPC$WebPage tLRPC$WebPage = this.this$0.getMessagesController().faqWebPage;
            this.faqWebPage = tLRPC$WebPage;
            if (tLRPC$WebPage != null) {
                this.faqSearchArray.addAll(this.this$0.getMessagesController().faqSearchArray);
            }
            if (this.faqWebPage == null && !this.loadingFaqPage) {
                this.loadingFaqPage = true;
                TLRPC$TL_messages_getWebPage tLRPC$TL_messages_getWebPage = new TLRPC$TL_messages_getWebPage();
                tLRPC$TL_messages_getWebPage.url = LocaleController.getString("TelegramFaqUrl", NUM);
                tLRPC$TL_messages_getWebPage.hash = 0;
                this.this$0.getConnectionsManager().sendRequest(tLRPC$TL_messages_getWebPage, new RequestDelegate() {
                    public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                        ProfileActivity.SearchAdapter.this.lambda$loadFaqWebPage$85$ProfileActivity$SearchAdapter(tLObject, tLRPC$TL_error);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$loadFaqWebPage$85 */
        public /* synthetic */ void lambda$loadFaqWebPage$85$ProfileActivity$SearchAdapter(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
            if (tLObject instanceof TLRPC$WebPage) {
                ArrayList arrayList = new ArrayList();
                TLRPC$WebPage tLRPC$WebPage = (TLRPC$WebPage) tLObject;
                TLRPC$Page tLRPC$Page = tLRPC$WebPage.cached_page;
                if (tLRPC$Page != null) {
                    int size = tLRPC$Page.blocks.size();
                    for (int i = 0; i < size; i++) {
                        TLRPC$PageBlock tLRPC$PageBlock = tLRPC$WebPage.cached_page.blocks.get(i);
                        if (tLRPC$PageBlock instanceof TLRPC$TL_pageBlockList) {
                            String str = null;
                            if (i != 0) {
                                TLRPC$PageBlock tLRPC$PageBlock2 = tLRPC$WebPage.cached_page.blocks.get(i - 1);
                                if (tLRPC$PageBlock2 instanceof TLRPC$TL_pageBlockParagraph) {
                                    str = ArticleViewer.getPlainText(((TLRPC$TL_pageBlockParagraph) tLRPC$PageBlock2).text).toString();
                                }
                            }
                            TLRPC$TL_pageBlockList tLRPC$TL_pageBlockList = (TLRPC$TL_pageBlockList) tLRPC$PageBlock;
                            int size2 = tLRPC$TL_pageBlockList.items.size();
                            for (int i2 = 0; i2 < size2; i2++) {
                                TLRPC$PageListItem tLRPC$PageListItem = tLRPC$TL_pageBlockList.items.get(i2);
                                if (tLRPC$PageListItem instanceof TLRPC$TL_pageListItemText) {
                                    TLRPC$TL_pageListItemText tLRPC$TL_pageListItemText = (TLRPC$TL_pageListItemText) tLRPC$PageListItem;
                                    String url = ArticleViewer.getUrl(tLRPC$TL_pageListItemText.text);
                                    String charSequence = ArticleViewer.getPlainText(tLRPC$TL_pageListItemText.text).toString();
                                    if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(charSequence)) {
                                        arrayList.add(new MessagesController.FaqSearchResult(charSequence, str != null ? new String[]{LocaleController.getString("SettingsSearchFaq", NUM), str} : new String[]{LocaleController.getString("SettingsSearchFaq", NUM)}, url));
                                    }
                                }
                            }
                        } else if (tLRPC$PageBlock instanceof TLRPC$TL_pageBlockAnchor) {
                            break;
                        }
                    }
                    this.faqWebPage = tLRPC$WebPage;
                }
                AndroidUtilities.runOnUIThread(new Runnable(arrayList) {
                    public final /* synthetic */ ArrayList f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ProfileActivity.SearchAdapter.this.lambda$null$84$ProfileActivity$SearchAdapter(this.f$1);
                    }
                });
            }
            this.loadingFaqPage = false;
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$null$84 */
        public /* synthetic */ void lambda$null$84$ProfileActivity$SearchAdapter(ArrayList arrayList) {
            this.faqSearchArray.addAll(arrayList);
            this.this$0.getMessagesController().faqSearchArray = arrayList;
            this.this$0.getMessagesController().faqWebPage = this.faqWebPage;
            if (!this.searchWas) {
                notifyDataSetChanged();
            }
        }

        public int getItemCount() {
            int i = 0;
            if (this.searchWas) {
                int size = this.searchResults.size();
                if (!this.faqSearchResults.isEmpty()) {
                    i = this.faqSearchResults.size() + 1;
                }
                return size + i;
            }
            int size2 = this.recentSearches.isEmpty() ? 0 : this.recentSearches.size() + 1;
            if (!this.faqSearchArray.isEmpty()) {
                i = this.faqSearchArray.size() + 1;
            }
            return size2 + i;
        }

        public boolean isEnabled(RecyclerView.ViewHolder viewHolder) {
            return viewHolder.getItemViewType() == 0;
        }

        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            int i2;
            int itemViewType = viewHolder.getItemViewType();
            boolean z = true;
            if (itemViewType == 0) {
                SettingsSearchCell settingsSearchCell = (SettingsSearchCell) viewHolder.itemView;
                boolean z2 = false;
                if (!this.searchWas) {
                    if (!this.recentSearches.isEmpty()) {
                        i--;
                    }
                    if (i < this.recentSearches.size()) {
                        Object obj = this.recentSearches.get(i);
                        if (obj instanceof SearchResult) {
                            SearchResult searchResult = (SearchResult) obj;
                            String access$20900 = searchResult.searchTitle;
                            String[] access$20800 = searchResult.path;
                            if (i >= this.recentSearches.size() - 1) {
                                z = false;
                            }
                            settingsSearchCell.setTextAndValue(access$20900, access$20800, false, z);
                        } else if (obj instanceof MessagesController.FaqSearchResult) {
                            MessagesController.FaqSearchResult faqSearchResult = (MessagesController.FaqSearchResult) obj;
                            String str = faqSearchResult.title;
                            String[] strArr = faqSearchResult.path;
                            if (i < this.recentSearches.size() - 1) {
                                z2 = true;
                            }
                            settingsSearchCell.setTextAndValue(str, strArr, true, z2);
                        }
                    } else {
                        int size = i - (this.recentSearches.size() + 1);
                        MessagesController.FaqSearchResult faqSearchResult2 = this.faqSearchArray.get(size);
                        String str2 = faqSearchResult2.title;
                        String[] strArr2 = faqSearchResult2.path;
                        if (size < this.recentSearches.size() - 1) {
                            z2 = true;
                        }
                        settingsSearchCell.setTextAndValue(str2, strArr2, true, z2);
                    }
                } else if (i < this.searchResults.size()) {
                    SearchResult searchResult2 = this.searchResults.get(i);
                    SearchResult searchResult3 = i > 0 ? this.searchResults.get(i - 1) : null;
                    if (searchResult3 == null || searchResult3.iconResId != searchResult2.iconResId) {
                        i2 = searchResult2.iconResId;
                    } else {
                        i2 = 0;
                    }
                    CharSequence charSequence = this.resultNames.get(i);
                    String[] access$208002 = searchResult2.path;
                    if (i >= this.searchResults.size() - 1) {
                        z = false;
                    }
                    settingsSearchCell.setTextAndValueAndIcon(charSequence, access$208002, i2, z);
                } else {
                    int size2 = i - (this.searchResults.size() + 1);
                    CharSequence charSequence2 = this.resultNames.get(this.searchResults.size() + size2);
                    String[] strArr3 = this.faqSearchResults.get(size2).path;
                    if (size2 < this.searchResults.size() - 1) {
                        z2 = true;
                    }
                    settingsSearchCell.setTextAndValue(charSequence2, strArr3, true, z2);
                }
            } else if (itemViewType == 1) {
                ((GraySectionCell) viewHolder.itemView).setText(LocaleController.getString("SettingsFaqSearchTitle", NUM));
            } else if (itemViewType == 2) {
                ((HeaderCell) viewHolder.itemView).setText(LocaleController.getString("SettingsRecent", NUM));
            }
        }

        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view;
            if (i == 0) {
                view = new SettingsSearchCell(this.mContext);
            } else if (i != 1) {
                view = new HeaderCell(this.mContext, 16);
            } else {
                view = new GraySectionCell(this.mContext);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
            return new RecyclerListView.Holder(view);
        }

        public int getItemViewType(int i) {
            if (this.searchWas) {
                if (i >= this.searchResults.size() && i == this.searchResults.size()) {
                    return 1;
                }
            } else if (i != 0) {
                return (this.recentSearches.isEmpty() || i != this.recentSearches.size() + 1) ? 0 : 1;
            } else {
                if (!this.recentSearches.isEmpty()) {
                    return 2;
                }
                return 1;
            }
        }

        public void addRecent(Object obj) {
            int indexOf = this.recentSearches.indexOf(obj);
            if (indexOf >= 0) {
                this.recentSearches.remove(indexOf);
            }
            this.recentSearches.add(0, obj);
            if (!this.searchWas) {
                notifyDataSetChanged();
            }
            if (this.recentSearches.size() > 20) {
                ArrayList<Object> arrayList = this.recentSearches;
                arrayList.remove(arrayList.size() - 1);
            }
            LinkedHashSet linkedHashSet = new LinkedHashSet();
            int size = this.recentSearches.size();
            for (int i = 0; i < size; i++) {
                Object obj2 = this.recentSearches.get(i);
                if (obj2 instanceof SearchResult) {
                    int unused = ((SearchResult) obj2).num = i;
                } else if (obj2 instanceof MessagesController.FaqSearchResult) {
                    ((MessagesController.FaqSearchResult) obj2).num = i;
                }
                linkedHashSet.add(obj2.toString());
            }
            MessagesController.getGlobalMainSettings().edit().putStringSet("settingsSearchRecent2", linkedHashSet).commit();
        }

        public void clearRecent() {
            this.recentSearches.clear();
            MessagesController.getGlobalMainSettings().edit().remove("settingsSearchRecent2").commit();
            notifyDataSetChanged();
        }

        private int getNum(Object obj) {
            if (obj instanceof SearchResult) {
                return ((SearchResult) obj).num;
            }
            if (obj instanceof MessagesController.FaqSearchResult) {
                return ((MessagesController.FaqSearchResult) obj).num;
            }
            return 0;
        }

        public void search(String str) {
            this.lastSearchString = str;
            if (this.searchRunnable != null) {
                Utilities.searchQueue.cancelRunnable(this.searchRunnable);
                this.searchRunnable = null;
            }
            if (TextUtils.isEmpty(str)) {
                this.searchWas = false;
                this.searchResults.clear();
                this.faqSearchResults.clear();
                this.resultNames.clear();
                this.this$0.emptyView.setTopImage(0);
                this.this$0.emptyView.setText(LocaleController.getString("SettingsNoRecent", NUM));
                notifyDataSetChanged();
                return;
            }
            DispatchQueue dispatchQueue = Utilities.searchQueue;
            $$Lambda$ProfileActivity$SearchAdapter$GljfJTfxfMnyTtz5iII2fCyNwVU r1 = new Runnable(str) {
                public final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$search$87$ProfileActivity$SearchAdapter(this.f$1);
                }
            };
            this.searchRunnable = r1;
            dispatchQueue.postRunnable(r1, 300);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$search$87 */
        public /* synthetic */ void lambda$search$87$ProfileActivity$SearchAdapter(String str) {
            SpannableStringBuilder spannableStringBuilder;
            int i;
            String str2;
            String str3;
            SpannableStringBuilder spannableStringBuilder2;
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            ArrayList arrayList3 = new ArrayList();
            String str4 = " ";
            String[] split = str.split(str4);
            String[] strArr = new String[split.length];
            int i2 = 0;
            while (true) {
                spannableStringBuilder = null;
                if (i2 >= split.length) {
                    break;
                }
                strArr[i2] = LocaleController.getInstance().getTranslitString(split[i2]);
                if (strArr[i2].equals(split[i2])) {
                    strArr[i2] = null;
                }
                i2++;
            }
            int i3 = 0;
            while (true) {
                SearchResult[] searchResultArr = this.searchArray;
                if (i3 >= searchResultArr.length) {
                    break;
                }
                SearchResult searchResult = searchResultArr[i3];
                if (searchResult != null) {
                    String str5 = str4 + searchResult.searchTitle.toLowerCase();
                    SpannableStringBuilder spannableStringBuilder3 = spannableStringBuilder;
                    int i4 = 0;
                    while (i4 < split.length) {
                        if (split[i4].length() != 0) {
                            String str6 = split[i4];
                            int indexOf = str5.indexOf(str4 + str6);
                            if (indexOf < 0 && strArr[i4] != null) {
                                str6 = strArr[i4];
                                indexOf = str5.indexOf(str4 + str6);
                            }
                            if (indexOf < 0) {
                                break;
                            }
                            spannableStringBuilder2 = spannableStringBuilder3 == null ? new SpannableStringBuilder(searchResult.searchTitle) : spannableStringBuilder3;
                            str3 = str5;
                            spannableStringBuilder2.setSpan(new ForegroundColorSpan(Theme.getColor("windowBackgroundWhiteBlueText4")), indexOf, str6.length() + indexOf, 33);
                        } else {
                            str3 = str5;
                            spannableStringBuilder2 = spannableStringBuilder3;
                        }
                        if (spannableStringBuilder2 != null && i4 == split.length - 1) {
                            if (searchResult.guid == 502) {
                                int i5 = -1;
                                int i6 = 0;
                                while (true) {
                                    if (i6 >= 3) {
                                        break;
                                    } else if (!UserConfig.getInstance(i3).isClientActivated()) {
                                        i5 = i6;
                                        break;
                                    } else {
                                        i6++;
                                    }
                                }
                                if (i5 < 0) {
                                }
                            }
                            arrayList.add(searchResult);
                            arrayList3.add(spannableStringBuilder2);
                        }
                        i4++;
                        String str7 = str;
                        spannableStringBuilder3 = spannableStringBuilder2;
                        str5 = str3;
                    }
                }
                i3++;
                String str8 = str;
                spannableStringBuilder = null;
            }
            if (this.faqWebPage != null) {
                int size = this.faqSearchArray.size();
                int i7 = 0;
                while (i7 < size) {
                    MessagesController.FaqSearchResult faqSearchResult = this.faqSearchArray.get(i7);
                    String str9 = str4 + faqSearchResult.title.toLowerCase();
                    int i8 = 0;
                    SpannableStringBuilder spannableStringBuilder4 = null;
                    while (i8 < split.length) {
                        if (split[i8].length() != 0) {
                            String str10 = split[i8];
                            int indexOf2 = str9.indexOf(str4 + str10);
                            if (indexOf2 < 0 && strArr[i8] != null) {
                                str10 = strArr[i8];
                                indexOf2 = str9.indexOf(str4 + str10);
                            }
                            if (indexOf2 < 0) {
                                break;
                            }
                            if (spannableStringBuilder4 == null) {
                                str2 = str4;
                                spannableStringBuilder4 = new SpannableStringBuilder(faqSearchResult.title);
                            } else {
                                str2 = str4;
                            }
                            i = size;
                            spannableStringBuilder4.setSpan(new ForegroundColorSpan(Theme.getColor("windowBackgroundWhiteBlueText4")), indexOf2, str10.length() + indexOf2, 33);
                        } else {
                            str2 = str4;
                            i = size;
                        }
                        if (spannableStringBuilder4 != null && i8 == split.length - 1) {
                            arrayList2.add(faqSearchResult);
                            arrayList3.add(spannableStringBuilder4);
                        }
                        i8++;
                        str4 = str2;
                        size = i;
                    }
                    i7++;
                    str4 = str4;
                    size = size;
                }
            }
            AndroidUtilities.runOnUIThread(new Runnable(str, arrayList, arrayList2, arrayList3) {
                public final /* synthetic */ String f$1;
                public final /* synthetic */ ArrayList f$2;
                public final /* synthetic */ ArrayList f$3;
                public final /* synthetic */ ArrayList f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run() {
                    ProfileActivity.SearchAdapter.this.lambda$null$86$ProfileActivity$SearchAdapter(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$null$86 */
        public /* synthetic */ void lambda$null$86$ProfileActivity$SearchAdapter(String str, ArrayList arrayList, ArrayList arrayList2, ArrayList arrayList3) {
            if (str.equals(this.lastSearchString)) {
                if (!this.searchWas) {
                    this.this$0.emptyView.setTopImage(NUM);
                    this.this$0.emptyView.setText(LocaleController.getString("SettingsNoResults", NUM));
                }
                this.searchWas = true;
                this.searchResults = arrayList;
                this.faqSearchResults = arrayList2;
                this.resultNames = arrayList3;
                notifyDataSetChanged();
            }
        }

        public boolean isSearchWas() {
            return this.searchWas;
        }
    }

    public ArrayList<ThemeDescription> getThemeDescriptions() {
        $$Lambda$ProfileActivity$EEvRECrfgeGCLASSNAMEPiR9FioMXdtzU r10 = new ThemeDescription.ThemeDescriptionDelegate() {
            public final void didSetColor() {
                ProfileActivity.this.lambda$getThemeDescriptions$35$ProfileActivity();
            }
        };
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        SharedMediaLayout sharedMediaLayout2 = this.sharedMediaLayout;
        if (sharedMediaLayout2 != null) {
            arrayList.addAll(sharedMediaLayout2.getThemeDescriptions());
        }
        arrayList.add(new ThemeDescription(this.listView, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
        arrayList.add(new ThemeDescription(this.searchListView, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
        arrayList.add(new ThemeDescription(this.listView, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundGray"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSubmenuBackground"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSubmenuItem"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM | ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSubmenuItemIcon"));
        $$Lambda$ProfileActivity$EEvRECrfgeGCLASSNAMEPiR9FioMXdtzU r7 = r10;
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "actionBarDefaultIcon"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "avatar_actionBarSelectorBlue"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "chat_lockIcon"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "avatar_subtitleInProfileBlue"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "avatar_backgroundActionBarBlue"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "profile_title"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "profile_status"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "avatar_subtitleInProfileBlue"));
        arrayList.add(new ThemeDescription(this.mediaCounterTextView.getTextView(), ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "player_actionBarSubtitle"));
        arrayList.add(new ThemeDescription(this.mediaCounterTextView.getNextTextView(), ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "player_actionBarSubtitle"));
        arrayList.add(new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "avatar_backgroundActionBarBlue"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "listSelectorSDK21"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "divider"));
        arrayList.add(new ThemeDescription(this.avatarImage, 0, (Class[]) null, (Paint) null, Theme.avatarDrawables, (ThemeDescription.ThemeDescriptionDelegate) null, "avatar_text"));
        arrayList.add(new ThemeDescription(this.avatarImage, 0, (Class[]) null, (Paint) null, new Drawable[]{this.avatarDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "avatar_backgroundInProfileBlue"));
        arrayList.add(new ThemeDescription(this.writeButton, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "profile_actionIcon"));
        arrayList.add(new ThemeDescription(this.writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "profile_actionBackground"));
        arrayList.add(new ThemeDescription(this.writeButton, ThemeDescription.FLAG_DRAWABLESELECTEDSTATE | ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "profile_actionPressedBackground"));
        arrayList.add(new ThemeDescription((View) this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
        arrayList.add(new ThemeDescription((View) this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGreenText2"));
        arrayList.add(new ThemeDescription((View) this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteRedText5"));
        arrayList.add(new ThemeDescription((View) this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueText2"));
        arrayList.add(new ThemeDescription((View) this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueButton"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{TextCell.class}, new String[]{"valueTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteValueText"));
        arrayList.add(new ThemeDescription((View) this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"imageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayIcon"));
        arrayList.add(new ThemeDescription((View) this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"imageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueIcon"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{TextDetailCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{TextDetailCell.class}, new String[]{"valueTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText2"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueHeader"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText2"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "switchTrack"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "switchTrackChecked"));
        arrayList.add(new ThemeDescription((View) this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{UserCell.class}, new String[]{"adminTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "profile_creatorIcon"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{UserCell.class}, new String[]{"imageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayIcon"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{UserCell.class}, new String[]{"nameTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
        $$Lambda$ProfileActivity$EEvRECrfgeGCLASSNAMEPiR9FioMXdtzU r8 = r10;
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{UserCell.class}, new String[]{"statusColor"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) r8, "windowBackgroundWhiteGrayText"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{UserCell.class}, new String[]{"statusOnlineColor"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) r8, "windowBackgroundWhiteBlueText"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, (Paint) null, Theme.avatarDrawables, (ThemeDescription.ThemeDescriptionDelegate) null, "avatar_text"));
        $$Lambda$ProfileActivity$EEvRECrfgeGCLASSNAMEPiR9FioMXdtzU r72 = r10;
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundRed"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundOrange"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundViolet"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundGreen"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundCyan"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundBlue"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundPink"));
        arrayList.add(new ThemeDescription(this.undoView, ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_background"));
        arrayList.add(new ThemeDescription((View) this.undoView, 0, new Class[]{UndoView.class}, new String[]{"undoImageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_cancelColor"));
        arrayList.add(new ThemeDescription((View) this.undoView, 0, new Class[]{UndoView.class}, new String[]{"undoTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_cancelColor"));
        arrayList.add(new ThemeDescription((View) this.undoView, 0, new Class[]{UndoView.class}, new String[]{"infoTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_infoColor"));
        arrayList.add(new ThemeDescription((View) this.undoView, 0, new Class[]{UndoView.class}, new String[]{"textPaint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_infoColor"));
        arrayList.add(new ThemeDescription((View) this.undoView, 0, new Class[]{UndoView.class}, new String[]{"progressPaint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_infoColor"));
        arrayList.add(new ThemeDescription((View) this.undoView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{UndoView.class}, new String[]{"leftImageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_infoColor"));
        RecyclerListView recyclerListView = this.listView;
        RecyclerListView recyclerListView2 = recyclerListView;
        arrayList.add(new ThemeDescription(recyclerListView2, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{AboutLinkCell.class}, Theme.profile_aboutTextPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
        RecyclerListView recyclerListView3 = this.listView;
        RecyclerListView recyclerListView4 = recyclerListView3;
        arrayList.add(new ThemeDescription(recyclerListView4, ThemeDescription.FLAG_LINKCOLOR, new Class[]{AboutLinkCell.class}, Theme.profile_aboutTextPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteLinkText"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{AboutLinkCell.class}, Theme.linkSelectionPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteLinkSelection"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundGrayShadow"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundGrayShadow"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText4"));
        arrayList.add(new ThemeDescription((View) this.searchListView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueHeader"));
        arrayList.add(new ThemeDescription((View) this.searchListView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "key_graySectionText"));
        arrayList.add(new ThemeDescription(this.searchListView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "graySection"));
        arrayList.add(new ThemeDescription((View) this.searchListView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
        arrayList.add(new ThemeDescription((View) this.searchListView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"valueTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText2"));
        arrayList.add(new ThemeDescription((View) this.searchListView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"imageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayIcon"));
        if (this.mediaHeaderVisible) {
            arrayList.add(new ThemeDescription(this.nameTextView[1], 0, (Class[]) null, (Paint) null, new Drawable[]{this.verifiedCheckDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarTitle"));
            arrayList.add(new ThemeDescription(this.nameTextView[1], 0, (Class[]) null, (Paint) null, new Drawable[]{this.verifiedDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
        } else {
            arrayList.add(new ThemeDescription(this.nameTextView[1], 0, (Class[]) null, (Paint) null, new Drawable[]{this.verifiedCheckDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "profile_verifiedCheck"));
            arrayList.add(new ThemeDescription(this.nameTextView[1], 0, (Class[]) null, (Paint) null, new Drawable[]{this.verifiedDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "profile_verifiedBackground"));
        }
        return arrayList;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getThemeDescriptions$35 */
    public /* synthetic */ void lambda$getThemeDescriptions$35$ProfileActivity() {
        RecyclerListView recyclerListView = this.listView;
        if (recyclerListView != null) {
            int childCount = recyclerListView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = this.listView.getChildAt(i);
                if (childAt instanceof UserCell) {
                    ((UserCell) childAt).update(0);
                }
            }
        }
        if (!this.isPulledDown) {
            SimpleTextView[] simpleTextViewArr = this.onlineTextView;
            if (simpleTextViewArr[1] != null) {
                Object tag = simpleTextViewArr[1].getTag();
                if (tag instanceof String) {
                    this.onlineTextView[1].setTextColor(Theme.getColor((String) tag));
                }
            }
            Drawable drawable = this.lockIconDrawable;
            if (drawable != null) {
                drawable.setColorFilter(Theme.getColor("chat_lockIcon"), PorterDuff.Mode.MULTIPLY);
            }
            ScamDrawable scamDrawable2 = this.scamDrawable;
            if (scamDrawable2 != null) {
                scamDrawable2.setColor(Theme.getColor("avatar_subtitleInProfileBlue"));
            }
            this.nameTextView[1].setBackgroundColor(Theme.getColor("avatar_backgroundActionBarBlue"));
            this.nameTextView[1].setTextColor(Theme.getColor("profile_title"));
            this.actionBar.setItemsColor(Theme.getColor("actionBarDefaultIcon"), false);
            this.actionBar.setItemsBackgroundColor(Theme.getColor("avatar_actionBarSelectorBlue"), false);
        }
    }

    public void updateListAnimated() {
        if (this.listAdapter == null) {
            updateRowsIds();
            return;
        }
        View view = null;
        DiffCallback diffCallback = new DiffCallback();
        diffCallback.oldRowCount = this.rowCount;
        diffCallback.fillPositions(diffCallback.oldPositionToItem);
        updateRowsIds();
        diffCallback.fillPositions(diffCallback.newPositionToItem);
        DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo((RecyclerView.Adapter) this.listAdapter);
        RecyclerListView recyclerListView = this.listView;
        if (recyclerListView != null && this.layoutManager != null && recyclerListView.getChildCount() > 0) {
            int i = 0;
            int i2 = -1;
            while (true) {
                if (i >= this.listView.getChildCount()) {
                    break;
                }
                RecyclerListView recyclerListView2 = this.listView;
                i2 = recyclerListView2.getChildAdapterPosition(recyclerListView2.getChildAt(i));
                if (i2 != -1) {
                    view = this.listView.getChildAt(i);
                    break;
                }
                i++;
            }
            if (view != null) {
                this.layoutManager.scrollToPositionWithOffset(i2, view.getTop() - this.listView.getPaddingTop());
            }
        }
    }

    private class DiffCallback extends DiffUtil.Callback {
        SparseIntArray newPositionToItem;
        SparseIntArray oldPositionToItem;
        int oldRowCount;

        private DiffCallback() {
            this.oldPositionToItem = new SparseIntArray();
            this.newPositionToItem = new SparseIntArray();
        }

        public int getOldListSize() {
            return this.oldRowCount;
        }

        public int getNewListSize() {
            return ProfileActivity.this.rowCount;
        }

        public boolean areItemsTheSame(int i, int i2) {
            return this.oldPositionToItem.get(i) == this.newPositionToItem.get(i2);
        }

        public boolean areContentsTheSame(int i, int i2) {
            return this.oldPositionToItem.get(i) == this.newPositionToItem.get(i2);
        }

        public void fillPositions(SparseIntArray sparseIntArray) {
            sparseIntArray.clear();
            put(1, ProfileActivity.this.setAvatarRow, sparseIntArray);
            put(2, ProfileActivity.this.setAvatarSectionRow, sparseIntArray);
            put(3, ProfileActivity.this.numberSectionRow, sparseIntArray);
            put(4, ProfileActivity.this.numberRow, sparseIntArray);
            put(5, ProfileActivity.this.setUsernameRow, sparseIntArray);
            put(6, ProfileActivity.this.bioRow, sparseIntArray);
            put(7, ProfileActivity.this.settingsSectionRow, sparseIntArray);
            put(8, ProfileActivity.this.settingsSectionRow2, sparseIntArray);
            put(9, ProfileActivity.this.notificationRow, sparseIntArray);
            put(10, ProfileActivity.this.languageRow, sparseIntArray);
            put(11, ProfileActivity.this.privacyRow, sparseIntArray);
            put(12, ProfileActivity.this.dataRow, sparseIntArray);
            put(13, ProfileActivity.this.chatRow, sparseIntArray);
            put(14, ProfileActivity.this.filtersRow, sparseIntArray);
            put(15, ProfileActivity.this.devicesRow, sparseIntArray);
            put(16, ProfileActivity.this.devicesSectionRow, sparseIntArray);
            put(17, ProfileActivity.this.helpHeaderRow, sparseIntArray);
            put(18, ProfileActivity.this.questionRow, sparseIntArray);
            put(19, ProfileActivity.this.faqRow, sparseIntArray);
            put(20, ProfileActivity.this.policyRow, sparseIntArray);
            put(21, ProfileActivity.this.helpSectionCell, sparseIntArray);
            put(22, ProfileActivity.this.debugHeaderRow, sparseIntArray);
            put(23, ProfileActivity.this.sendLogsRow, sparseIntArray);
            put(24, ProfileActivity.this.clearLogsRow, sparseIntArray);
            put(25, ProfileActivity.this.switchBackendRow, sparseIntArray);
            put(26, ProfileActivity.this.versionRow, sparseIntArray);
            put(27, ProfileActivity.this.emptyRow, sparseIntArray);
            put(28, ProfileActivity.this.bottomPaddingRow, sparseIntArray);
            put(29, ProfileActivity.this.infoHeaderRow, sparseIntArray);
            put(30, ProfileActivity.this.phoneRow, sparseIntArray);
            put(31, ProfileActivity.this.locationRow, sparseIntArray);
            put(32, ProfileActivity.this.userInfoRow, sparseIntArray);
            put(33, ProfileActivity.this.channelInfoRow, sparseIntArray);
            put(34, ProfileActivity.this.usernameRow, sparseIntArray);
            put(35, ProfileActivity.this.notificationsDividerRow, sparseIntArray);
            put(36, ProfileActivity.this.notificationsRow, sparseIntArray);
            put(37, ProfileActivity.this.infoSectionRow, sparseIntArray);
            put(38, ProfileActivity.this.sendMessageRow, sparseIntArray);
            put(39, ProfileActivity.this.reportRow, sparseIntArray);
            put(40, ProfileActivity.this.settingsTimerRow, sparseIntArray);
            put(41, ProfileActivity.this.settingsKeyRow, sparseIntArray);
            put(42, ProfileActivity.this.secretSettingsSectionRow, sparseIntArray);
            put(43, ProfileActivity.this.membersHeaderRow, sparseIntArray);
            put(44, ProfileActivity.this.addMemberRow, sparseIntArray);
            put(45, ProfileActivity.this.subscribersRow, sparseIntArray);
            put(46, ProfileActivity.this.administratorsRow, sparseIntArray);
            put(47, ProfileActivity.this.blockedUsersRow, sparseIntArray);
            put(48, ProfileActivity.this.membersSectionRow, sparseIntArray);
            put(49, ProfileActivity.this.sharedMediaRow, sparseIntArray);
            put(50, ProfileActivity.this.unblockRow, sparseIntArray);
            put(51, ProfileActivity.this.joinRow, sparseIntArray);
            put(52, ProfileActivity.this.lastSectionRow, sparseIntArray);
        }

        private void put(int i, int i2, SparseIntArray sparseIntArray) {
            if (i2 >= 0) {
                sparseIntArray.put(i2, i);
            }
        }
    }
}
