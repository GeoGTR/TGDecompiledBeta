package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaCodecInfo;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Layout.Alignment;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.method.LinkMovementMethod;
import android.util.Property;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Keep;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScrollerEnd;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.State;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Bitmaps;
import org.telegram.messenger.BringAppForegroundService;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.ImageReceiver.BitmapHolder;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.PhotoEntry;
import org.telegram.messenger.MediaController.SearchImage;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MediaDataController.KeywordResult;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SecureDocument;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.messenger.WebFile;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInlineResult;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.PageBlock;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_fileLocationToBeDeprecated;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageActionEmpty;
import org.telegram.tgnet.TLRPC.TL_messageActionUserUpdatedPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_webDocument;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebDocument;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.ActionBarPopupWindow.ActionBarPopupWindowLayout;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.MentionsAdapter;
import org.telegram.ui.Adapters.MentionsAdapter.MentionsAdapterDelegate;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.PhotoPickerPhotoCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatAttachAlert;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.ClippingImageView;
import org.telegram.ui.Components.GroupedPhotosListView;
import org.telegram.ui.Components.GroupedPhotosListView.GroupedPhotosListViewDelegate;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.Components.OtherDocumentPlaceholderDrawable;
import org.telegram.ui.Components.PhotoCropView;
import org.telegram.ui.Components.PhotoFilterView;
import org.telegram.ui.Components.PhotoPaintView;
import org.telegram.ui.Components.PhotoViewerCaptionEnterView;
import org.telegram.ui.Components.PhotoViewerCaptionEnterView.PhotoViewerCaptionEnterViewDelegate;
import org.telegram.ui.Components.PickerBottomLayoutViewer;
import org.telegram.ui.Components.PipVideoView;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.ui.Components.SeekBar;
import org.telegram.ui.Components.SeekBar.SeekBarDelegate;
import org.telegram.ui.Components.SizeNotifierFrameLayoutPhoto;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.Components.URLSpanUserMentionPhotoViewer;
import org.telegram.ui.Components.VideoForwardDrawable;
import org.telegram.ui.Components.VideoForwardDrawable.VideoForwardDrawableDelegate;
import org.telegram.ui.Components.VideoPlayer;
import org.telegram.ui.Components.VideoPlayer.VideoPlayerDelegate;
import org.telegram.ui.Components.VideoSeekPreviewImage;
import org.telegram.ui.Components.VideoTimelinePlayView;
import org.telegram.ui.Components.VideoTimelinePlayView.VideoTimelineViewDelegate;

public class PhotoViewer implements NotificationCenterDelegate, OnGestureListener, OnDoubleTapListener {
    @SuppressLint({"StaticFieldLeak"})
    private static volatile PhotoViewer Instance = null;
    private static volatile PhotoViewer PipInstance = null;
    public static final int SELECT_TYPE_AVATAR = 1;
    public static final int SELECT_TYPE_QR = 10;
    public static final int SELECT_TYPE_WALLPAPER = 3;
    private static DecelerateInterpolator decelerateInterpolator = null;
    private static final int gallery_menu_cancel_loading = 7;
    private static final int gallery_menu_delete = 6;
    private static final int gallery_menu_masks = 13;
    private static final int gallery_menu_openin = 11;
    private static final int gallery_menu_pip = 5;
    private static final int gallery_menu_save = 1;
    private static final int gallery_menu_savegif = 14;
    private static final int gallery_menu_send = 3;
    private static final int gallery_menu_share = 10;
    private static final int gallery_menu_showall = 2;
    private static final int gallery_menu_showinchat = 4;
    private static Drawable[] progressDrawables;
    private static Paint progressPaint;
    private ActionBar actionBar;
    private AnimatorSet actionBarAnimator;
    private Context actvityContext;
    private ActionBarMenuSubItem allMediaItem;
    private boolean allowMentions;
    private boolean allowOrder = true;
    private boolean allowShare;
    private float animateToScale;
    private float animateToX;
    private float animateToY;
    private ClippingImageView animatingImageView;
    private Runnable animationEndRunnable;
    private int animationInProgress;
    private long animationStartTime;
    private float animationValue;
    private float[][] animationValues = ((float[][]) Array.newInstance(float.class, new int[]{2, 10}));
    private boolean applying;
    private AspectRatioFrameLayout aspectRatioFrameLayout;
    private boolean attachedToWindow;
    private long audioFramesSize;
    private ArrayList<Photo> avatarsArr = new ArrayList();
    private int avatarsDialogId;
    private BackgroundDrawable backgroundDrawable = new BackgroundDrawable(-16777216);
    private int bitrate;
    private Paint blackPaint = new Paint();
    private FrameLayout bottomLayout;
    private boolean bottomTouchEnabled = true;
    private ImageView cameraItem;
    private boolean canDragDown = true;
    private boolean canZoom = true;
    private PhotoViewerCaptionEnterView captionEditText;
    private TextView captionTextView;
    private ImageReceiver centerImage = new ImageReceiver();
    private AnimatorSet changeModeAnimation;
    private TextureView changedTextureView;
    private boolean changingPage;
    private boolean changingTextureView;
    private CheckBox checkImageView;
    private int classGuid;
    private ImageView compressItem;
    private AnimatorSet compressItemAnimation;
    private int compressionsCount = -1;
    private FrameLayoutDrawer containerView;
    private ImageView cropItem;
    private int currentAccount;
    private AnimatedFileDrawable currentAnimation;
    private Bitmap currentBitmap;
    private BotInlineResult currentBotInlineResult;
    private AnimatorSet currentCaptionAnimation;
    private long currentDialogId;
    private int currentEditMode;
    private ImageLocation currentFileLocation;
    private String[] currentFileNames = new String[3];
    private int currentIndex;
    private AnimatorSet currentListViewAnimation;
    private Runnable currentLoadingVideoRunnable;
    private MessageObject currentMessageObject;
    private String currentPathObject;
    private PlaceProviderObject currentPlaceObject;
    private Uri currentPlayingVideoFile;
    private SecureDocument currentSecureDocument;
    private String currentSubtitle;
    private BitmapHolder currentThumb;
    private ImageLocation currentUserAvatarLocation = null;
    private boolean currentVideoFinishedLoading;
    private int dateOverride;
    private TextView dateTextView;
    private boolean disableShowCheck;
    private boolean discardTap;
    private boolean doneButtonPressed;
    private boolean dontResetZoomOnFirstLayout;
    private boolean doubleTap;
    private boolean doubleTapEnabled;
    private float dragY;
    private boolean draggingDown;
    private PickerBottomLayoutViewer editorDoneLayout;
    private boolean[] endReached = new boolean[]{false, true};
    private long endTime;
    private long estimatedDuration;
    private int estimatedSize;
    private boolean firstAnimationDelay;
    boolean fromCamera;
    private GestureDetector gestureDetector;
    private GroupedPhotosListView groupedPhotosListView;
    private PlaceProviderObject hideAfterAnimation;
    private boolean ignoreDidSetImage;
    private AnimatorSet imageMoveAnimation;
    private ArrayList<MessageObject> imagesArr = new ArrayList();
    private ArrayList<Object> imagesArrLocals = new ArrayList();
    private ArrayList<ImageLocation> imagesArrLocations = new ArrayList();
    private ArrayList<Integer> imagesArrLocationsSizes = new ArrayList();
    private ArrayList<MessageObject> imagesArrTemp = new ArrayList();
    private SparseArray<MessageObject>[] imagesByIds = new SparseArray[]{new SparseArray(), new SparseArray()};
    private SparseArray<MessageObject>[] imagesByIdsTemp = new SparseArray[]{new SparseArray(), new SparseArray()};
    private boolean inPreview;
    private VideoPlayer injectingVideoPlayer;
    private SurfaceTexture injectingVideoPlayerSurface;
    private DecelerateInterpolator interpolator = new DecelerateInterpolator(1.5f);
    private boolean invalidCoords;
    private boolean isActionBarVisible = true;
    private boolean isCurrentVideo;
    private boolean isEvent;
    private boolean isFirstLoading;
    private boolean isInline;
    private boolean isPhotosListViewVisible;
    private boolean isPlaying;
    private boolean isStreaming;
    private boolean isVisible;
    private boolean keepScreenOnFlagSet;
    private long lastBufferedPositionCheck;
    private Object lastInsets;
    private long lastSaveTime;
    private String lastTitle;
    private ImageReceiver leftImage = new ImageReceiver();
    private boolean loadInitialVideo;
    private boolean loadingMoreImages;
    private ActionBarMenuItem masksItem;
    private int maxSelectedPhotos = -1;
    private float maxX;
    private float maxY;
    private LinearLayoutManager mentionLayoutManager;
    private AnimatorSet mentionListAnimation;
    private RecyclerListView mentionListView;
    private MentionsAdapter mentionsAdapter;
    private ActionBarMenuItem menuItem;
    private long mergeDialogId;
    private float minX;
    private float minY;
    private AnimatorSet miniProgressAnimator;
    private Runnable miniProgressShowRunnable = new -$$Lambda$PhotoViewer$kxaoiG79AFKAwlWEJtsCNV1PPvc(this);
    private RadialProgressView miniProgressView;
    private float moveStartX;
    private float moveStartY;
    private boolean moving;
    private ImageView muteItem;
    private boolean muteVideo;
    private String nameOverride;
    private TextView nameTextView;
    private boolean needCaptionLayout;
    private boolean needSearchImageInArr;
    private boolean needShowOnReady;
    private boolean openedFullScreenVideo;
    private boolean opennedFromMedia;
    private int originalBitrate;
    private int originalHeight;
    private long originalSize;
    private int originalWidth;
    private boolean padImageForHorizontalInsets;
    private ImageView paintItem;
    private Activity parentActivity;
    private ChatAttachAlert parentAlert;
    private ChatActivity parentChatActivity;
    private PhotoCropView photoCropView;
    private PhotoFilterView photoFilterView;
    private PhotoPaintView photoPaintView;
    private PhotoProgressView[] photoProgressViews = new PhotoProgressView[3];
    private CounterView photosCounterView;
    private FrameLayout pickerView;
    private ImageView pickerViewSendButton;
    private float pinchCenterX;
    private float pinchCenterY;
    private float pinchStartDistance;
    private float pinchStartScale = 1.0f;
    private float pinchStartX;
    private float pinchStartY;
    private boolean pipAnimationInProgress;
    private boolean pipAvailable;
    private ActionBarMenuItem pipItem;
    private int[] pipPosition = new int[2];
    private PipVideoView pipVideoView;
    private PhotoViewerProvider placeProvider;
    private View playButtonAccessibilityOverlay;
    private boolean playerInjected;
    private boolean playerWasReady;
    private int previewViewEnd;
    private int previousCompression;
    private RadialProgressView progressView;
    private QualityChooseView qualityChooseView;
    private AnimatorSet qualityChooseViewAnimation;
    private PickerBottomLayoutViewer qualityPicker;
    private boolean requestingPreview;
    private TextView resetButton;
    private int resultHeight;
    private int resultWidth;
    private ImageReceiver rightImage = new ImageReceiver();
    private ImageView rotateItem;
    private int rotationValue;
    private float scale = 1.0f;
    private Scroller scroller;
    private ArrayList<SecureDocument> secureDocuments = new ArrayList();
    private float seekToProgressPending;
    private float seekToProgressPending2;
    private int selectedCompression;
    private ListAdapter selectedPhotosAdapter;
    private RecyclerListView selectedPhotosListView;
    private ActionBarMenuItem sendItem;
    private int sendPhotoType;
    private ActionBarPopupWindowLayout sendPopupLayout;
    private ActionBarPopupWindow sendPopupWindow;
    private Runnable setLoadingRunnable = new Runnable() {
        public void run() {
            if (PhotoViewer.this.currentMessageObject != null) {
                FileLoader.getInstance(PhotoViewer.this.currentMessageObject.currentAccount).setLoadingVideo(PhotoViewer.this.currentMessageObject.getDocument(), true, false);
            }
        }
    };
    private ImageView shareButton;
    private int sharedMediaType;
    private String shouldSavePositionForCurrentVideo;
    private PlaceProviderObject showAfterAnimation;
    private boolean skipFirstBufferingProgress;
    private int slideshowMessageId;
    private long startTime;
    private long startedPlayTime;
    private boolean streamingAlertShown;
    private SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (PhotoViewer.this.videoTextureView == null || !PhotoViewer.this.changingTextureView) {
                return true;
            }
            if (PhotoViewer.this.switchingInlineMode) {
                PhotoViewer.this.waitingForFirstTextureUpload = 2;
            }
            PhotoViewer.this.videoTextureView.setSurfaceTexture(surfaceTexture);
            PhotoViewer.this.videoTextureView.setVisibility(0);
            PhotoViewer.this.changingTextureView = false;
            PhotoViewer.this.containerView.invalidate();
            return false;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            if (PhotoViewer.this.waitingForFirstTextureUpload == 1) {
                PhotoViewer.this.changedTextureView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                    public boolean onPreDraw() {
                        PhotoViewer.this.changedTextureView.getViewTreeObserver().removeOnPreDrawListener(this);
                        if (PhotoViewer.this.textureImageView != null) {
                            PhotoViewer.this.textureImageView.setVisibility(4);
                            PhotoViewer.this.textureImageView.setImageDrawable(null);
                            if (PhotoViewer.this.currentBitmap != null) {
                                PhotoViewer.this.currentBitmap.recycle();
                                PhotoViewer.this.currentBitmap = null;
                            }
                        }
                        AndroidUtilities.runOnUIThread(new -$$Lambda$PhotoViewer$4$1$chreZZnt0YkItDSej6M4QHP4VU4(this));
                        PhotoViewer.this.waitingForFirstTextureUpload = 0;
                        return true;
                    }

                    public /* synthetic */ void lambda$onPreDraw$0$PhotoViewer$4$1() {
                        if (PhotoViewer.this.isInline) {
                            PhotoViewer.this.dismissInternal();
                        }
                    }
                });
                PhotoViewer.this.changedTextureView.invalidate();
            }
        }
    };
    private TextView switchCaptionTextView;
    private int switchImageAfterAnimation;
    private Runnable switchToInlineRunnable = new Runnable() {
        public void run() {
            PhotoViewer.this.switchingInlineMode = false;
            if (PhotoViewer.this.currentBitmap != null) {
                PhotoViewer.this.currentBitmap.recycle();
                PhotoViewer.this.currentBitmap = null;
            }
            PhotoViewer.this.changingTextureView = true;
            if (PhotoViewer.this.textureImageView != null) {
                try {
                    PhotoViewer.this.currentBitmap = Bitmaps.createBitmap(PhotoViewer.this.videoTextureView.getWidth(), PhotoViewer.this.videoTextureView.getHeight(), Config.ARGB_8888);
                    PhotoViewer.this.videoTextureView.getBitmap(PhotoViewer.this.currentBitmap);
                } catch (Throwable th) {
                    if (PhotoViewer.this.currentBitmap != null) {
                        PhotoViewer.this.currentBitmap.recycle();
                        PhotoViewer.this.currentBitmap = null;
                    }
                    FileLog.e(th);
                }
                if (PhotoViewer.this.currentBitmap != null) {
                    PhotoViewer.this.textureImageView.setVisibility(0);
                    PhotoViewer.this.textureImageView.setImageBitmap(PhotoViewer.this.currentBitmap);
                } else {
                    PhotoViewer.this.textureImageView.setImageDrawable(null);
                }
            }
            PhotoViewer.this.isInline = true;
            PhotoViewer.this.pipVideoView = new PipVideoView(false);
            PhotoViewer photoViewer = PhotoViewer.this;
            PipVideoView access$1500 = photoViewer.pipVideoView;
            Activity access$2800 = PhotoViewer.this.parentActivity;
            PhotoViewer photoViewer2 = PhotoViewer.this;
            photoViewer.changedTextureView = access$1500.show(access$2800, photoViewer2, photoViewer2.aspectRatioFrameLayout.getAspectRatio(), PhotoViewer.this.aspectRatioFrameLayout.getVideoRotation());
            PhotoViewer.this.changedTextureView.setVisibility(4);
            PhotoViewer.this.aspectRatioFrameLayout.removeView(PhotoViewer.this.videoTextureView);
        }
    };
    private boolean switchingInlineMode;
    private int switchingToIndex;
    private ImageView textureImageView;
    private boolean textureUploaded;
    private ImageView timeItem;
    private int totalImagesCount;
    private int totalImagesCountMerge;
    private long transitionAnimationStartTime;
    private float translationX;
    private float translationY;
    private boolean tryStartRequestPreviewOnFinish;
    private ImageView tuneItem;
    private Runnable updateProgressRunnable = new Runnable() {
        public void run() {
            if (PhotoViewer.this.videoPlayer != null) {
                float currentPosition;
                if (!PhotoViewer.this.isCurrentVideo) {
                    float f;
                    currentPosition = ((float) PhotoViewer.this.videoPlayer.getCurrentPosition()) / ((float) PhotoViewer.this.videoPlayer.getDuration());
                    if (PhotoViewer.this.currentVideoFinishedLoading) {
                        f = 1.0f;
                    } else {
                        long elapsedRealtime = SystemClock.elapsedRealtime();
                        if (Math.abs(elapsedRealtime - PhotoViewer.this.lastBufferedPositionCheck) >= 500) {
                            if (PhotoViewer.this.isStreaming) {
                                f = FileLoader.getInstance(PhotoViewer.this.currentAccount).getBufferedProgressFromPosition(PhotoViewer.this.seekToProgressPending != 0.0f ? PhotoViewer.this.seekToProgressPending : currentPosition, PhotoViewer.this.currentFileNames[0]);
                            } else {
                                f = 1.0f;
                            }
                            PhotoViewer.this.lastBufferedPositionCheck = elapsedRealtime;
                        } else {
                            f = -1.0f;
                        }
                    }
                    if (PhotoViewer.this.inPreview || PhotoViewer.this.videoTimelineView.getVisibility() != 0) {
                        if (PhotoViewer.this.seekToProgressPending == 0.0f) {
                            PhotoViewer.this.videoPlayerSeekbar.setProgress(currentPosition);
                        }
                        if (f != -1.0f) {
                            PhotoViewer.this.videoPlayerSeekbar.setBufferedProgress(f);
                            if (PhotoViewer.this.pipVideoView != null) {
                                PhotoViewer.this.pipVideoView.setBufferedProgress(f);
                            }
                        }
                    } else if (currentPosition >= PhotoViewer.this.videoTimelineView.getRightProgress()) {
                        PhotoViewer.this.videoPlayer.pause();
                        PhotoViewer.this.videoPlayerSeekbar.setProgress(0.0f);
                        PhotoViewer.this.videoPlayer.seekTo((long) ((int) (PhotoViewer.this.videoTimelineView.getLeftProgress() * ((float) PhotoViewer.this.videoPlayer.getDuration()))));
                        PhotoViewer.this.containerView.invalidate();
                    } else {
                        currentPosition -= PhotoViewer.this.videoTimelineView.getLeftProgress();
                        if (currentPosition < 0.0f) {
                            currentPosition = 0.0f;
                        }
                        currentPosition /= PhotoViewer.this.videoTimelineView.getRightProgress() - PhotoViewer.this.videoTimelineView.getLeftProgress();
                        if (currentPosition > 1.0f) {
                            currentPosition = 1.0f;
                        }
                        PhotoViewer.this.videoPlayerSeekbar.setProgress(currentPosition);
                    }
                    PhotoViewer.this.videoPlayerControlFrameLayout.invalidate();
                    if (PhotoViewer.this.shouldSavePositionForCurrentVideo != null && currentPosition >= 0.0f && PhotoViewer.this.shouldSavePositionForCurrentVideo != null && SystemClock.uptimeMillis() - PhotoViewer.this.lastSaveTime >= 1000) {
                        PhotoViewer.this.shouldSavePositionForCurrentVideo;
                        PhotoViewer.this.lastSaveTime = SystemClock.uptimeMillis();
                        Utilities.globalQueue.postRunnable(new -$$Lambda$PhotoViewer$2$ttTXp-z309aCIedHZEwyaNdn18I(this, currentPosition));
                    }
                    PhotoViewer.this.updateVideoPlayerTime();
                } else if (!PhotoViewer.this.videoTimelineView.isDragging()) {
                    currentPosition = ((float) PhotoViewer.this.videoPlayer.getCurrentPosition()) / ((float) PhotoViewer.this.videoPlayer.getDuration());
                    if (PhotoViewer.this.inPreview || PhotoViewer.this.videoTimelineView.getVisibility() != 0) {
                        PhotoViewer.this.videoTimelineView.setProgress(currentPosition);
                    } else if (currentPosition >= PhotoViewer.this.videoTimelineView.getRightProgress()) {
                        PhotoViewer.this.videoTimelineView.setProgress(0.0f);
                        PhotoViewer.this.videoPlayer.seekTo((long) ((int) (PhotoViewer.this.videoTimelineView.getLeftProgress() * ((float) PhotoViewer.this.videoPlayer.getDuration()))));
                        if (PhotoViewer.this.muteVideo) {
                            PhotoViewer.this.videoPlayer.play();
                        } else {
                            PhotoViewer.this.videoPlayer.pause();
                        }
                        PhotoViewer.this.containerView.invalidate();
                    } else {
                        currentPosition -= PhotoViewer.this.videoTimelineView.getLeftProgress();
                        if (currentPosition < 0.0f) {
                            currentPosition = 0.0f;
                        }
                        currentPosition /= PhotoViewer.this.videoTimelineView.getRightProgress() - PhotoViewer.this.videoTimelineView.getLeftProgress();
                        if (currentPosition > 1.0f) {
                            currentPosition = 1.0f;
                        }
                        PhotoViewer.this.videoTimelineView.setProgress(currentPosition);
                    }
                    PhotoViewer.this.updateVideoPlayerTime();
                }
            }
            if (PhotoViewer.this.isPlaying) {
                AndroidUtilities.runOnUIThread(PhotoViewer.this.updateProgressRunnable, 17);
            }
        }

        public /* synthetic */ void lambda$run$0$PhotoViewer$2(float f) {
            ApplicationLoader.applicationContext.getSharedPreferences("media_saved_pos", 0).edit().putFloat(PhotoViewer.this.shouldSavePositionForCurrentVideo, f).commit();
        }
    };
    private VelocityTracker velocityTracker;
    private ImageView videoBackwardButton;
    private float videoCrossfadeAlpha;
    private long videoCrossfadeAlphaLastTime;
    private boolean videoCrossfadeStarted;
    private float videoCutEnd;
    private float videoCutStart;
    private float videoDuration;
    private ImageView videoForwardButton;
    private VideoForwardDrawable videoForwardDrawable;
    private int videoFramerate;
    private long videoFramesSize;
    private boolean videoHasAudio;
    private ImageView videoPlayButton;
    private VideoPlayer videoPlayer;
    private FrameLayout videoPlayerControlFrameLayout;
    private SeekBar videoPlayerSeekbar;
    private SimpleTextView videoPlayerTime;
    private VideoSeekPreviewImage videoPreviewFrame;
    private AnimatorSet videoPreviewFrameAnimation;
    private MessageObject videoPreviewMessageObject;
    private TextureView videoTextureView;
    private VideoTimelinePlayView videoTimelineView;
    private AlertDialog visibleDialog;
    private int waitingForDraw;
    private int waitingForFirstTextureUpload;
    private boolean wasLayout;
    private LayoutParams windowLayoutParams;
    private FrameLayout windowView;
    private boolean zoomAnimation;
    private boolean zooming;

    private class BackgroundDrawable extends ColorDrawable {
        private boolean allowDrawContent;
        private Runnable drawRunnable;

        public BackgroundDrawable(int i) {
            super(i);
        }

        @Keep
        public void setAlpha(int i) {
            if (PhotoViewer.this.parentActivity instanceof LaunchActivity) {
                boolean z = (PhotoViewer.this.isVisible && i == 255) ? false : true;
                this.allowDrawContent = z;
                ((LaunchActivity) PhotoViewer.this.parentActivity).drawerLayoutContainer.setAllowDrawContent(this.allowDrawContent);
                if (PhotoViewer.this.parentAlert != null) {
                    if (!this.allowDrawContent) {
                        AndroidUtilities.runOnUIThread(new -$$Lambda$PhotoViewer$BackgroundDrawable$pDidiNsUGVGYtvuU76vLRyxH30U(this), 50);
                    } else if (PhotoViewer.this.parentAlert != null) {
                        PhotoViewer.this.parentAlert.setAllowDrawContent(this.allowDrawContent);
                    }
                }
            }
            super.setAlpha(i);
        }

        public /* synthetic */ void lambda$setAlpha$0$PhotoViewer$BackgroundDrawable() {
            if (PhotoViewer.this.parentAlert != null) {
                PhotoViewer.this.parentAlert.setAllowDrawContent(this.allowDrawContent);
            }
        }

        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (getAlpha() != 0) {
                Runnable runnable = this.drawRunnable;
                if (runnable != null) {
                    AndroidUtilities.runOnUIThread(runnable);
                    this.drawRunnable = null;
                }
            }
        }
    }

    private class CounterView extends View {
        private int currentCount = 0;
        private int height;
        private Paint paint;
        private RectF rect;
        private float rotation;
        private StaticLayout staticLayout;
        private TextPaint textPaint = new TextPaint(1);
        private int width;

        public CounterView(Context context) {
            super(context);
            this.textPaint.setTextSize((float) AndroidUtilities.dp(18.0f));
            this.textPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            this.textPaint.setColor(-1);
            this.paint = new Paint(1);
            this.paint.setColor(-1);
            this.paint.setStrokeWidth((float) AndroidUtilities.dp(2.0f));
            this.paint.setStyle(Style.STROKE);
            this.paint.setStrokeJoin(Join.ROUND);
            this.rect = new RectF();
            setCount(0);
        }

        @Keep
        public void setScaleX(float f) {
            super.setScaleX(f);
            invalidate();
        }

        @Keep
        public void setRotationX(float f) {
            this.rotation = f;
            invalidate();
        }

        public float getRotationX() {
            return this.rotation;
        }

        public void setCount(int i) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("");
            stringBuilder.append(Math.max(1, i));
            this.staticLayout = new StaticLayout(stringBuilder.toString(), this.textPaint, AndroidUtilities.dp(100.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            this.width = (int) Math.ceil((double) this.staticLayout.getLineWidth(0));
            this.height = this.staticLayout.getLineBottom(0);
            AnimatorSet animatorSet = new AnimatorSet();
            if (i == 0) {
                r3 = new Animator[4];
                r3[0] = ObjectAnimator.ofFloat(this, View.SCALE_X, new float[]{0.0f});
                r3[1] = ObjectAnimator.ofFloat(this, View.SCALE_Y, new float[]{0.0f});
                r3[2] = ObjectAnimator.ofInt(this.paint, AnimationProperties.PAINT_ALPHA, new int[]{0});
                r3[3] = ObjectAnimator.ofInt(this.textPaint, AnimationProperties.PAINT_ALPHA, new int[]{0});
                animatorSet.playTogether(r3);
                animatorSet.setInterpolator(new DecelerateInterpolator());
            } else {
                int i2 = this.currentCount;
                if (i2 == 0) {
                    animatorSet.playTogether(new Animator[]{ObjectAnimator.ofFloat(this, View.SCALE_X, new float[]{0.0f, 1.0f}), ObjectAnimator.ofFloat(this, View.SCALE_Y, new float[]{0.0f, 1.0f}), ObjectAnimator.ofInt(this.paint, AnimationProperties.PAINT_ALPHA, new int[]{0, 255}), ObjectAnimator.ofInt(this.textPaint, AnimationProperties.PAINT_ALPHA, new int[]{0, 255})});
                    animatorSet.setInterpolator(new DecelerateInterpolator());
                } else if (i < i2) {
                    animatorSet.playTogether(new Animator[]{ObjectAnimator.ofFloat(this, View.SCALE_X, new float[]{1.1f, 1.0f}), ObjectAnimator.ofFloat(this, View.SCALE_Y, new float[]{1.1f, 1.0f})});
                    animatorSet.setInterpolator(new OvershootInterpolator());
                } else {
                    animatorSet.playTogether(new Animator[]{ObjectAnimator.ofFloat(this, View.SCALE_X, new float[]{0.9f, 1.0f}), ObjectAnimator.ofFloat(this, View.SCALE_Y, new float[]{0.9f, 1.0f})});
                    animatorSet.setInterpolator(new OvershootInterpolator());
                }
            }
            animatorSet.setDuration(180);
            animatorSet.start();
            requestLayout();
            this.currentCount = i;
        }

        /* Access modifiers changed, original: protected */
        public void onMeasure(int i, int i2) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(Math.max(this.width + AndroidUtilities.dp(20.0f), AndroidUtilities.dp(30.0f)), NUM), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40.0f), NUM));
        }

        /* Access modifiers changed, original: protected */
        public void onDraw(Canvas canvas) {
            int measuredHeight = getMeasuredHeight() / 2;
            this.paint.setAlpha(255);
            this.rect.set((float) AndroidUtilities.dp(1.0f), (float) (measuredHeight - AndroidUtilities.dp(14.0f)), (float) (getMeasuredWidth() - AndroidUtilities.dp(1.0f)), (float) (measuredHeight + AndroidUtilities.dp(14.0f)));
            canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(15.0f), (float) AndroidUtilities.dp(15.0f), this.paint);
            if (this.staticLayout != null) {
                this.textPaint.setAlpha((int) ((1.0f - this.rotation) * 255.0f));
                canvas.save();
                canvas.translate((float) ((getMeasuredWidth() - this.width) / 2), (((float) ((getMeasuredHeight() - this.height) / 2)) + AndroidUtilities.dpf2(0.2f)) + (this.rotation * ((float) AndroidUtilities.dp(5.0f))));
                this.staticLayout.draw(canvas);
                canvas.restore();
                this.paint.setAlpha((int) (this.rotation * 255.0f));
                measuredHeight = (int) this.rect.centerX();
                int centerY = (int) (((float) ((int) this.rect.centerY())) - ((((float) AndroidUtilities.dp(5.0f)) * (1.0f - this.rotation)) + ((float) AndroidUtilities.dp(3.0f))));
                canvas.drawLine((float) (AndroidUtilities.dp(0.5f) + measuredHeight), (float) (centerY - AndroidUtilities.dp(0.5f)), (float) (measuredHeight - AndroidUtilities.dp(6.0f)), (float) (AndroidUtilities.dp(6.0f) + centerY), this.paint);
                canvas.drawLine((float) (measuredHeight - AndroidUtilities.dp(0.5f)), (float) (centerY - AndroidUtilities.dp(0.5f)), (float) (measuredHeight + AndroidUtilities.dp(6.0f)), (float) (centerY + AndroidUtilities.dp(6.0f)), this.paint);
            }
        }
    }

    private class LinkMovementMethodMy extends LinkMovementMethod {
        private LinkMovementMethodMy() {
        }

        /* synthetic */ LinkMovementMethodMy(PhotoViewer photoViewer, AnonymousClass1 anonymousClass1) {
            this();
        }

        public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent motionEvent) {
            try {
                boolean onTouchEvent = super.onTouchEvent(textView, spannable, motionEvent);
                if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                    URLSpanNoUnderline[] uRLSpanNoUnderlineArr = (URLSpanNoUnderline[]) spannable.getSpans(textView.getSelectionStart(), textView.getSelectionEnd(), URLSpanNoUnderline.class);
                    if (uRLSpanNoUnderlineArr != null && uRLSpanNoUnderlineArr.length > 0) {
                        String url = uRLSpanNoUnderlineArr[0].getURL();
                        if (!(!url.startsWith("video") || PhotoViewer.this.videoPlayer == null || PhotoViewer.this.currentMessageObject == null)) {
                            int intValue = Utilities.parseInt(url).intValue();
                            if (PhotoViewer.this.videoPlayer.getDuration() == -9223372036854775807L) {
                                PhotoViewer.this.seekToProgressPending = ((float) intValue) / ((float) PhotoViewer.this.currentMessageObject.getDuration());
                            } else {
                                PhotoViewer.this.videoPlayer.seekTo(((long) intValue) * 1000);
                            }
                        }
                    }
                    Selection.removeSelection(spannable);
                }
                return onTouchEvent;
            } catch (Exception e) {
                FileLog.e(e);
                return false;
            }
        }
    }

    private class PhotoProgressView {
        private float alpha = 1.0f;
        private float animatedAlphaValue = 1.0f;
        private float animatedProgressValue = 0.0f;
        private float animationProgressStart = 0.0f;
        private int backgroundState = -1;
        private float currentProgress = 0.0f;
        private long currentProgressTime = 0;
        private long lastUpdateTime = 0;
        private View parent;
        private int previousBackgroundState = -2;
        private RectF progressRect = new RectF();
        private float radOffset = 0.0f;
        private float scale = 1.0f;
        private int size = AndroidUtilities.dp(64.0f);

        public PhotoProgressView(Context context, View view) {
            if (PhotoViewer.decelerateInterpolator == null) {
                PhotoViewer.decelerateInterpolator = new DecelerateInterpolator(1.5f);
                PhotoViewer.progressPaint = new Paint(1);
                PhotoViewer.progressPaint.setStyle(Style.STROKE);
                PhotoViewer.progressPaint.setStrokeCap(Cap.ROUND);
                PhotoViewer.progressPaint.setStrokeWidth((float) AndroidUtilities.dp(3.0f));
                PhotoViewer.progressPaint.setColor(-1);
            }
            this.parent = view;
        }

        private void updateAnimation() {
            long currentTimeMillis = System.currentTimeMillis();
            long j = currentTimeMillis - this.lastUpdateTime;
            if (j > 18) {
                j = 18;
            }
            this.lastUpdateTime = currentTimeMillis;
            if (this.animatedProgressValue != 1.0f) {
                this.radOffset += ((float) (360 * j)) / 3000.0f;
                float f = this.currentProgress;
                float f2 = this.animationProgressStart;
                float f3 = f - f2;
                if (f3 > 0.0f) {
                    this.currentProgressTime += j;
                    if (this.currentProgressTime >= 300) {
                        this.animatedProgressValue = f;
                        this.animationProgressStart = f;
                        this.currentProgressTime = 0;
                    } else {
                        this.animatedProgressValue = f2 + (f3 * PhotoViewer.decelerateInterpolator.getInterpolation(((float) this.currentProgressTime) / 300.0f));
                    }
                }
                this.parent.invalidate();
            }
            if (this.animatedProgressValue >= 1.0f && this.previousBackgroundState != -2) {
                this.animatedAlphaValue -= ((float) j) / 200.0f;
                if (this.animatedAlphaValue <= 0.0f) {
                    this.animatedAlphaValue = 0.0f;
                    this.previousBackgroundState = -2;
                }
                this.parent.invalidate();
            }
        }

        public void setProgress(float f, boolean z) {
            if (z) {
                this.animationProgressStart = this.animatedProgressValue;
            } else {
                this.animatedProgressValue = f;
                this.animationProgressStart = f;
            }
            this.currentProgress = f;
            this.currentProgressTime = 0;
        }

        public void setBackgroundState(int i, boolean z) {
            if (this.backgroundState != i || !z) {
                this.lastUpdateTime = System.currentTimeMillis();
                if (z) {
                    int i2 = this.backgroundState;
                    if (i2 != i) {
                        this.previousBackgroundState = i2;
                        this.animatedAlphaValue = 1.0f;
                        this.backgroundState = i;
                        this.parent.invalidate();
                    }
                }
                this.previousBackgroundState = -2;
                this.backgroundState = i;
                this.parent.invalidate();
            }
        }

        public void setAlpha(float f) {
            this.alpha = f;
        }

        public void setScale(float f) {
            this.scale = f;
        }

        public void onDraw(Canvas canvas) {
            Drawable drawable;
            int i = (int) (((float) this.size) * this.scale);
            int access$3600 = (PhotoViewer.this.getContainerViewWidth() - i) / 2;
            int access$3700 = (PhotoViewer.this.getContainerViewHeight() - i) / 2;
            int i2 = this.previousBackgroundState;
            if (i2 >= 0 && i2 < 4) {
                drawable = PhotoViewer.progressDrawables[this.previousBackgroundState];
                if (drawable != null) {
                    drawable.setAlpha((int) ((this.animatedAlphaValue * 255.0f) * this.alpha));
                    drawable.setBounds(access$3600, access$3700, access$3600 + i, access$3700 + i);
                    drawable.draw(canvas);
                }
            }
            i2 = this.backgroundState;
            if (i2 >= 0 && i2 < 4) {
                drawable = PhotoViewer.progressDrawables[this.backgroundState];
                if (drawable != null) {
                    if (this.previousBackgroundState != -2) {
                        drawable.setAlpha((int) (((1.0f - this.animatedAlphaValue) * 255.0f) * this.alpha));
                    } else {
                        drawable.setAlpha((int) (this.alpha * 255.0f));
                    }
                    drawable.setBounds(access$3600, access$3700, access$3600 + i, access$3700 + i);
                    drawable.draw(canvas);
                }
            }
            i2 = this.backgroundState;
            if (!(i2 == 0 || i2 == 1)) {
                i2 = this.previousBackgroundState;
                if (!(i2 == 0 || i2 == 1)) {
                    return;
                }
            }
            int dp = AndroidUtilities.dp(4.0f);
            if (this.previousBackgroundState != -2) {
                PhotoViewer.progressPaint.setAlpha((int) ((this.animatedAlphaValue * 255.0f) * this.alpha));
            } else {
                PhotoViewer.progressPaint.setAlpha((int) (this.alpha * 255.0f));
            }
            this.progressRect.set((float) (access$3600 + dp), (float) (access$3700 + dp), (float) ((access$3600 + i) - dp), (float) ((access$3700 + i) - dp));
            canvas.drawArc(this.progressRect, this.radOffset - 0.049804688f, Math.max(4.0f, this.animatedProgressValue * 360.0f), false, PhotoViewer.progressPaint);
            updateAnimation();
        }
    }

    public interface PhotoViewerProvider {
        boolean allowCaption();

        boolean canCaptureMorePhotos();

        boolean canScrollAway();

        boolean cancelButtonPressed();

        void deleteImageAtIndex(int i);

        String getDeleteMessageString();

        int getPhotoIndex(int i);

        PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, FileLocation fileLocation, int i, boolean z);

        int getSelectedCount();

        HashMap<Object, Object> getSelectedPhotos();

        ArrayList<Object> getSelectedPhotosOrder();

        BitmapHolder getThumbForPhoto(MessageObject messageObject, FileLocation fileLocation, int i);

        boolean isPhotoChecked(int i);

        void needAddMorePhotos();

        boolean scaleToFill();

        void sendButtonPressed(int i, VideoEditedInfo videoEditedInfo, boolean z, int i2);

        int setPhotoChecked(int i, VideoEditedInfo videoEditedInfo);

        int setPhotoUnchecked(Object obj);

        void updatePhotoAtIndex(int i);

        void willHidePhotoViewer();

        void willSwitchFromPhoto(MessageObject messageObject, FileLocation fileLocation, int i);
    }

    public static class PlaceProviderObject {
        public int clipBottomAddition;
        public int clipTopAddition;
        public int dialogId;
        public ImageReceiver imageReceiver;
        public int index;
        public boolean isEvent;
        public View parentView;
        public int radius;
        public float scale = 1.0f;
        public int size;
        public BitmapHolder thumb;
        public int viewX;
        public int viewY;
    }

    private class QualityChooseView extends View {
        private int circleSize;
        private int gapSize;
        private int lineSize;
        private boolean moving;
        private Paint paint = new Paint(1);
        private int sideSide;
        private boolean startMoving;
        private int startMovingQuality;
        private float startX;
        private TextPaint textPaint = new TextPaint(1);

        public QualityChooseView(Context context) {
            super(context);
            this.textPaint.setTextSize((float) AndroidUtilities.dp(12.0f));
            this.textPaint.setColor(-3289651);
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            float x = motionEvent.getX();
            boolean z = false;
            int i;
            int i2;
            int i3;
            int i4;
            if (motionEvent.getAction() == 0) {
                getParent().requestDisallowInterceptTouchEvent(true);
                i = 0;
                while (i < PhotoViewer.this.compressionsCount) {
                    i2 = this.sideSide;
                    i3 = this.lineSize + (this.gapSize * 2);
                    i4 = this.circleSize;
                    i2 = (i2 + ((i3 + i4) * i)) + (i4 / 2);
                    if (x <= ((float) (i2 - AndroidUtilities.dp(15.0f))) || x >= ((float) (i2 + AndroidUtilities.dp(15.0f)))) {
                        i++;
                    } else {
                        if (i == PhotoViewer.this.selectedCompression) {
                            z = true;
                        }
                        this.startMoving = z;
                        this.startX = x;
                        this.startMovingQuality = PhotoViewer.this.selectedCompression;
                    }
                }
            } else if (motionEvent.getAction() == 2) {
                if (this.startMoving) {
                    if (Math.abs(this.startX - x) >= AndroidUtilities.getPixelsInCM(0.5f, true)) {
                        this.moving = true;
                        this.startMoving = false;
                    }
                } else if (this.moving) {
                    i = 0;
                    while (i < PhotoViewer.this.compressionsCount) {
                        i2 = this.sideSide;
                        int i5 = this.lineSize;
                        i3 = this.gapSize;
                        i4 = (i3 * 2) + i5;
                        int i6 = this.circleSize;
                        i2 = (i2 + ((i4 + i6) * i)) + (i6 / 2);
                        i5 = ((i5 / 2) + (i6 / 2)) + i3;
                        if (x <= ((float) (i2 - i5)) || x >= ((float) (i2 + i5))) {
                            i++;
                        } else if (PhotoViewer.this.selectedCompression != i) {
                            PhotoViewer.this.selectedCompression = i;
                            PhotoViewer.this.didChangedCompressionLevel(false);
                            invalidate();
                        }
                    }
                }
            } else if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                if (!this.moving) {
                    i = 0;
                    while (i < PhotoViewer.this.compressionsCount) {
                        i2 = this.sideSide;
                        i3 = this.lineSize + (this.gapSize * 2);
                        i4 = this.circleSize;
                        i2 = (i2 + ((i3 + i4) * i)) + (i4 / 2);
                        if (x <= ((float) (i2 - AndroidUtilities.dp(15.0f))) || x >= ((float) (i2 + AndroidUtilities.dp(15.0f)))) {
                            i++;
                        } else if (PhotoViewer.this.selectedCompression != i) {
                            PhotoViewer.this.selectedCompression = i;
                            PhotoViewer.this.didChangedCompressionLevel(true);
                            invalidate();
                        }
                    }
                } else if (PhotoViewer.this.selectedCompression != this.startMovingQuality) {
                    PhotoViewer.this.requestVideoPreview(1);
                }
                this.startMoving = false;
                this.moving = false;
            }
            return true;
        }

        /* Access modifiers changed, original: protected */
        public void onMeasure(int i, int i2) {
            super.onMeasure(i, i2);
            this.circleSize = AndroidUtilities.dp(12.0f);
            this.gapSize = AndroidUtilities.dp(2.0f);
            this.sideSide = AndroidUtilities.dp(18.0f);
        }

        /* Access modifiers changed, original: protected */
        public void onDraw(Canvas canvas) {
            if (PhotoViewer.this.compressionsCount != 1) {
                this.lineSize = (((getMeasuredWidth() - (this.circleSize * PhotoViewer.this.compressionsCount)) - (this.gapSize * 8)) - (this.sideSide * 2)) / (PhotoViewer.this.compressionsCount - 1);
            } else {
                this.lineSize = ((getMeasuredWidth() - (this.circleSize * PhotoViewer.this.compressionsCount)) - (this.gapSize * 8)) - (this.sideSide * 2);
            }
            int measuredHeight = (getMeasuredHeight() / 2) + AndroidUtilities.dp(6.0f);
            int i = 0;
            while (i < PhotoViewer.this.compressionsCount) {
                String stringBuilder;
                int i2 = this.sideSide;
                int i3 = this.lineSize + (this.gapSize * 2);
                int i4 = this.circleSize;
                i2 = (i2 + ((i3 + i4) * i)) + (i4 / 2);
                if (i <= PhotoViewer.this.selectedCompression) {
                    this.paint.setColor(-11292945);
                } else {
                    this.paint.setColor(NUM);
                }
                if (i == PhotoViewer.this.compressionsCount - 1) {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append(Math.min(PhotoViewer.this.originalWidth, PhotoViewer.this.originalHeight));
                    stringBuilder2.append("p");
                    stringBuilder = stringBuilder2.toString();
                } else {
                    stringBuilder = i == 0 ? "240p" : i == 1 ? "360p" : i == 2 ? "480p" : "720p";
                }
                float measureText = this.textPaint.measureText(stringBuilder);
                float f = (float) i2;
                canvas.drawCircle(f, (float) measuredHeight, (float) (i == PhotoViewer.this.selectedCompression ? AndroidUtilities.dp(8.0f) : this.circleSize / 2), this.paint);
                canvas.drawText(stringBuilder, f - (measureText / 2.0f), (float) (measuredHeight - AndroidUtilities.dp(16.0f)), this.textPaint);
                if (i != 0) {
                    i2 = ((i2 - (this.circleSize / 2)) - this.gapSize) - this.lineSize;
                    canvas.drawRect((float) i2, (float) (measuredHeight - AndroidUtilities.dp(1.0f)), (float) (i2 + this.lineSize), (float) (AndroidUtilities.dp(2.0f) + measuredHeight), this.paint);
                }
                i++;
            }
        }
    }

    public static class EmptyPhotoViewerProvider implements PhotoViewerProvider {
        public boolean allowCaption() {
            return true;
        }

        public boolean canCaptureMorePhotos() {
            return true;
        }

        public boolean canScrollAway() {
            return true;
        }

        public boolean cancelButtonPressed() {
            return true;
        }

        public void deleteImageAtIndex(int i) {
        }

        public String getDeleteMessageString() {
            return null;
        }

        public int getPhotoIndex(int i) {
            return -1;
        }

        public PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, FileLocation fileLocation, int i, boolean z) {
            return null;
        }

        public int getSelectedCount() {
            return 0;
        }

        public HashMap<Object, Object> getSelectedPhotos() {
            return null;
        }

        public ArrayList<Object> getSelectedPhotosOrder() {
            return null;
        }

        public BitmapHolder getThumbForPhoto(MessageObject messageObject, FileLocation fileLocation, int i) {
            return null;
        }

        public boolean isPhotoChecked(int i) {
            return false;
        }

        public void needAddMorePhotos() {
        }

        public boolean scaleToFill() {
            return false;
        }

        public void sendButtonPressed(int i, VideoEditedInfo videoEditedInfo, boolean z, int i2) {
        }

        public int setPhotoChecked(int i, VideoEditedInfo videoEditedInfo) {
            return -1;
        }

        public int setPhotoUnchecked(Object obj) {
            return -1;
        }

        public void updatePhotoAtIndex(int i) {
        }

        public void willHidePhotoViewer() {
        }

        public void willSwitchFromPhoto(MessageObject messageObject, FileLocation fileLocation, int i) {
        }
    }

    private class FrameLayoutDrawer extends SizeNotifierFrameLayoutPhoto {
        private boolean ignoreLayout;
        private Paint paint = new Paint();

        public FrameLayoutDrawer(Context context) {
            super(context);
            setWillNotDraw(false);
            this.paint.setColor(NUM);
        }

        /* Access modifiers changed, original: protected */
        public void onMeasure(int i, int i2) {
            int size = MeasureSpec.getSize(i);
            int size2 = MeasureSpec.getSize(i2);
            setMeasuredDimension(size, size2);
            this.ignoreLayout = true;
            TextView access$3900 = PhotoViewer.this.captionTextView;
            Point point = AndroidUtilities.displaySize;
            access$3900.setMaxLines(point.x > point.y ? 5 : 10);
            this.ignoreLayout = false;
            measureChildWithMargins(PhotoViewer.this.captionEditText, i, 0, i2, 0);
            int measuredHeight = PhotoViewer.this.captionEditText.getMeasuredHeight();
            size -= getPaddingRight() + getPaddingLeft();
            size2 -= getPaddingBottom();
            int childCount = getChildCount();
            for (int i3 = 0; i3 < childCount; i3++) {
                View childAt = getChildAt(i3);
                if (!(childAt.getVisibility() == 8 || childAt == PhotoViewer.this.captionEditText)) {
                    if (childAt == PhotoViewer.this.aspectRatioFrameLayout) {
                        childAt.measure(i, MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.y + (VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0), NUM));
                    } else if (!PhotoViewer.this.captionEditText.isPopupView(childAt)) {
                        measureChildWithMargins(childAt, i, 0, i2, 0);
                    } else if (!AndroidUtilities.isInMultiwindow) {
                        childAt.measure(MeasureSpec.makeMeasureSpec(size, NUM), MeasureSpec.makeMeasureSpec(childAt.getLayoutParams().height, NUM));
                    } else if (AndroidUtilities.isTablet()) {
                        childAt.measure(MeasureSpec.makeMeasureSpec(size, NUM), MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320.0f), (size2 - measuredHeight) - AndroidUtilities.statusBarHeight), NUM));
                    } else {
                        childAt.measure(MeasureSpec.makeMeasureSpec(size, NUM), MeasureSpec.makeMeasureSpec((size2 - measuredHeight) - AndroidUtilities.statusBarHeight, NUM));
                    }
                }
            }
        }

        /* Access modifiers changed, original: protected */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x00c0  */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x00b3  */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x009f  */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x008a  */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x00b3  */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x00c0  */
        public void onLayout(boolean r16, int r17, int r18, int r19, int r20) {
            /*
            r15 = this;
            r0 = r15;
            r1 = r15.getChildCount();
            r2 = r15.getKeyboardHeight();
            r3 = NUM; // 0x41a00000 float:20.0 double:5.439686476E-315;
            r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
            r4 = 0;
            if (r2 > r3) goto L_0x0021;
        L_0x0012:
            r2 = org.telegram.messenger.AndroidUtilities.isInMultiwindow;
            if (r2 != 0) goto L_0x0021;
        L_0x0016:
            r2 = org.telegram.ui.PhotoViewer.this;
            r2 = r2.captionEditText;
            r2 = r2.getEmojiPadding();
            goto L_0x0022;
        L_0x0021:
            r2 = 0;
        L_0x0022:
            if (r4 >= r1) goto L_0x01b3;
        L_0x0024:
            r3 = r15.getChildAt(r4);
            r5 = r3.getVisibility();
            r6 = 8;
            if (r5 != r6) goto L_0x0032;
        L_0x0030:
            goto L_0x01af;
        L_0x0032:
            r5 = org.telegram.ui.PhotoViewer.this;
            r5 = r5.aspectRatioFrameLayout;
            if (r3 != r5) goto L_0x0041;
        L_0x003a:
            r5 = r17;
            r6 = r19;
            r7 = r20;
            goto L_0x0053;
        L_0x0041:
            r5 = r15.getPaddingLeft();
            r5 = r17 + r5;
            r6 = r15.getPaddingRight();
            r6 = r19 - r6;
            r7 = r15.getPaddingBottom();
            r7 = r20 - r7;
        L_0x0053:
            r8 = r3.getLayoutParams();
            r8 = (android.widget.FrameLayout.LayoutParams) r8;
            r9 = r3.getMeasuredWidth();
            r10 = r3.getMeasuredHeight();
            r11 = r8.gravity;
            r12 = -1;
            if (r11 != r12) goto L_0x0068;
        L_0x0066:
            r11 = 51;
        L_0x0068:
            r12 = r11 & 7;
            r11 = r11 & 112;
            r12 = r12 & 7;
            r13 = 5;
            r14 = 1;
            if (r12 == r14) goto L_0x007c;
        L_0x0072:
            if (r12 == r13) goto L_0x0077;
        L_0x0074:
            r6 = r8.leftMargin;
            goto L_0x0086;
        L_0x0077:
            r6 = r6 - r5;
            r6 = r6 - r9;
            r12 = r8.rightMargin;
            goto L_0x0085;
        L_0x007c:
            r6 = r6 - r5;
            r6 = r6 - r9;
            r6 = r6 / 2;
            r12 = r8.leftMargin;
            r6 = r6 + r12;
            r12 = r8.rightMargin;
        L_0x0085:
            r6 = r6 - r12;
        L_0x0086:
            r12 = 16;
            if (r11 == r12) goto L_0x009f;
        L_0x008a:
            r12 = 48;
            if (r11 == r12) goto L_0x009c;
        L_0x008e:
            r12 = 80;
            if (r11 == r12) goto L_0x0095;
        L_0x0092:
            r7 = r8.topMargin;
            goto L_0x00ab;
        L_0x0095:
            r7 = r7 - r2;
            r7 = r7 - r18;
            r7 = r7 - r10;
            r8 = r8.bottomMargin;
            goto L_0x00aa;
        L_0x009c:
            r7 = r8.topMargin;
            goto L_0x00ab;
        L_0x009f:
            r7 = r7 - r2;
            r7 = r7 - r18;
            r7 = r7 - r10;
            r7 = r7 / 2;
            r11 = r8.topMargin;
            r7 = r7 + r11;
            r8 = r8.bottomMargin;
        L_0x00aa:
            r7 = r7 - r8;
        L_0x00ab:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.mentionListView;
            if (r3 != r8) goto L_0x00c0;
        L_0x00b3:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.captionEditText;
            r8 = r8.getMeasuredHeight();
        L_0x00bd:
            r7 = r7 - r8;
            goto L_0x01a7;
        L_0x00c0:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.captionEditText;
            r8 = r8.isPopupView(r3);
            if (r8 == 0) goto L_0x00f4;
        L_0x00cc:
            r7 = org.telegram.messenger.AndroidUtilities.isInMultiwindow;
            if (r7 == 0) goto L_0x00e8;
        L_0x00d0:
            r7 = org.telegram.ui.PhotoViewer.this;
            r7 = r7.captionEditText;
            r7 = r7.getTop();
            r8 = r3.getMeasuredHeight();
            r7 = r7 - r8;
            r8 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
            r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
            r7 = r7 + r8;
            goto L_0x01a7;
        L_0x00e8:
            r7 = org.telegram.ui.PhotoViewer.this;
            r7 = r7.captionEditText;
            r7 = r7.getBottom();
            goto L_0x01a7;
        L_0x00f4:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.selectedPhotosListView;
            if (r3 != r8) goto L_0x0108;
        L_0x00fc:
            r7 = org.telegram.ui.PhotoViewer.this;
            r7 = r7.actionBar;
            r7 = r7.getMeasuredHeight();
            goto L_0x01a7;
        L_0x0108:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.captionTextView;
            if (r3 == r8) goto L_0x018d;
        L_0x0110:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.switchCaptionTextView;
            if (r3 != r8) goto L_0x011a;
        L_0x0118:
            goto L_0x018d;
        L_0x011a:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.cameraItem;
            if (r3 != r8) goto L_0x0154;
        L_0x0122:
            r7 = org.telegram.ui.PhotoViewer.this;
            r7 = r7.pickerView;
            r7 = r7.getTop();
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.sendPhotoType;
            r11 = 4;
            if (r8 == r11) goto L_0x0141;
        L_0x0135:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.sendPhotoType;
            if (r8 != r13) goto L_0x013e;
        L_0x013d:
            goto L_0x0141;
        L_0x013e:
            r8 = NUM; // 0x41700000 float:15.0 double:5.424144515E-315;
            goto L_0x0143;
        L_0x0141:
            r8 = NUM; // 0x42200000 float:40.0 double:5.481131706E-315;
        L_0x0143:
            r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
            r7 = r7 - r8;
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.cameraItem;
            r8 = r8.getMeasuredHeight();
            goto L_0x00bd;
        L_0x0154:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.videoPreviewFrame;
            if (r3 != r8) goto L_0x01a7;
        L_0x015c:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.groupedPhotosListView;
            r8 = r8.currentPhotos;
            r8 = r8.isEmpty();
            if (r8 != 0) goto L_0x0175;
        L_0x016a:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.groupedPhotosListView;
            r8 = r8.getMeasuredHeight();
            r7 = r7 - r8;
        L_0x0175:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.captionTextView;
            r8 = r8.getVisibility();
            if (r8 != 0) goto L_0x01a7;
        L_0x0181:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.captionTextView;
            r8 = r8.getMeasuredHeight();
            goto L_0x00bd;
        L_0x018d:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.groupedPhotosListView;
            r8 = r8.currentPhotos;
            r8 = r8.isEmpty();
            if (r8 != 0) goto L_0x01a7;
        L_0x019b:
            r8 = org.telegram.ui.PhotoViewer.this;
            r8 = r8.groupedPhotosListView;
            r8 = r8.getMeasuredHeight();
            goto L_0x00bd;
        L_0x01a7:
            r8 = r6 + r5;
            r6 = r6 + r9;
            r6 = r6 + r5;
            r10 = r10 + r7;
            r3.layout(r8, r7, r6, r10);
        L_0x01af:
            r4 = r4 + 1;
            goto L_0x0022;
        L_0x01b3:
            r15.notifyHeightChanged();
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer$FrameLayoutDrawer.onLayout(boolean, int, int, int, int):void");
        }

        /* Access modifiers changed, original: protected */
        public void onDraw(Canvas canvas) {
            PhotoViewer.this.onDraw(canvas);
            if (VERSION.SDK_INT >= 21 && AndroidUtilities.statusBarHeight != 0 && PhotoViewer.this.actionBar != null) {
                this.paint.setAlpha((int) ((PhotoViewer.this.actionBar.getAlpha() * 255.0f) * 0.2f));
                canvas.drawRect(0.0f, 0.0f, (float) getMeasuredWidth(), (float) AndroidUtilities.statusBarHeight, this.paint);
                this.paint.setAlpha((int) ((PhotoViewer.this.actionBar.getAlpha() * 255.0f) * 0.498f));
                if (getPaddingRight() > 0) {
                    canvas.drawRect((float) (getMeasuredWidth() - getPaddingRight()), 0.0f, (float) getMeasuredWidth(), (float) getMeasuredHeight(), this.paint);
                }
                if (getPaddingLeft() > 0) {
                    canvas.drawRect(0.0f, 0.0f, (float) getPaddingLeft(), (float) getMeasuredHeight(), this.paint);
                }
            }
        }

        /* Access modifiers changed, original: protected */
        public boolean drawChild(Canvas canvas, View view, long j) {
            boolean z = true;
            if (view == PhotoViewer.this.mentionListView || view == PhotoViewer.this.captionEditText) {
                if (!PhotoViewer.this.captionEditText.isPopupShowing() && PhotoViewer.this.captionEditText.getEmojiPadding() == 0 && ((AndroidUtilities.usingHardwareInput && PhotoViewer.this.captionEditText.getTag() == null) || getKeyboardHeight() == 0)) {
                    return false;
                }
            } else if (view == PhotoViewer.this.cameraItem || view == PhotoViewer.this.pickerView || view == PhotoViewer.this.pickerViewSendButton || view == PhotoViewer.this.captionTextView || (PhotoViewer.this.muteItem.getVisibility() == 0 && view == PhotoViewer.this.bottomLayout)) {
                int emojiPadding = (getKeyboardHeight() > AndroidUtilities.dp(20.0f) || AndroidUtilities.isInMultiwindow) ? 0 : PhotoViewer.this.captionEditText.getEmojiPadding();
                if (PhotoViewer.this.captionEditText.isPopupShowing() || ((AndroidUtilities.usingHardwareInput && PhotoViewer.this.captionEditText.getTag() != null) || getKeyboardHeight() > AndroidUtilities.dp(80.0f) || emojiPadding != 0)) {
                    if (BuildVars.DEBUG_VERSION) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("keyboard height = ");
                        stringBuilder.append(getKeyboardHeight());
                        stringBuilder.append(" padding = ");
                        stringBuilder.append(emojiPadding);
                        FileLog.d(stringBuilder.toString());
                    }
                    PhotoViewer.this.bottomTouchEnabled = false;
                    return false;
                }
                PhotoViewer.this.bottomTouchEnabled = true;
            } else if (view == PhotoViewer.this.checkImageView || view == PhotoViewer.this.photosCounterView) {
                if (PhotoViewer.this.captionEditText.getTag() != null) {
                    PhotoViewer.this.bottomTouchEnabled = false;
                    return false;
                }
                PhotoViewer.this.bottomTouchEnabled = true;
            } else if (view == PhotoViewer.this.miniProgressView) {
                return false;
            }
            try {
                if (view == PhotoViewer.this.aspectRatioFrameLayout || !super.drawChild(canvas, view, j)) {
                    z = false;
                }
            } catch (Throwable unused) {
            }
            return z;
        }

        public void requestLayout() {
            if (!this.ignoreLayout) {
                super.requestLayout();
            }
        }
    }

    private class ListAdapter extends SelectionAdapter {
        private Context mContext;

        public int getItemViewType(int i) {
            return 0;
        }

        public boolean isEnabled(ViewHolder viewHolder) {
            return true;
        }

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public int getItemCount() {
            return (PhotoViewer.this.placeProvider == null || PhotoViewer.this.placeProvider.getSelectedPhotosOrder() == null) ? 0 : PhotoViewer.this.placeProvider.getSelectedPhotosOrder().size();
        }

        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            PhotoPickerPhotoCell photoPickerPhotoCell = new PhotoPickerPhotoCell(this.mContext, false);
            photoPickerPhotoCell.checkFrame.setOnClickListener(new -$$Lambda$PhotoViewer$ListAdapter$9zkDzdvMmAtv_zjywq_3chr7CXo(this));
            return new Holder(photoPickerPhotoCell);
        }

        public /* synthetic */ void lambda$onCreateViewHolder$0$PhotoViewer$ListAdapter(View view) {
            Object tag = ((View) view.getParent()).getTag();
            int indexOf = PhotoViewer.this.imagesArrLocals.indexOf(tag);
            int photoChecked;
            if (indexOf >= 0) {
                photoChecked = PhotoViewer.this.placeProvider.setPhotoChecked(indexOf, PhotoViewer.this.getCurrentVideoEditedInfo());
                PhotoViewer.this.placeProvider.isPhotoChecked(indexOf);
                if (indexOf == PhotoViewer.this.currentIndex) {
                    PhotoViewer.this.checkImageView.setChecked(-1, false, true);
                }
                if (photoChecked >= 0) {
                    PhotoViewer.this.selectedPhotosAdapter.notifyItemRemoved(photoChecked);
                }
                PhotoViewer.this.updateSelectedCount();
                return;
            }
            photoChecked = PhotoViewer.this.placeProvider.setPhotoUnchecked(tag);
            if (photoChecked >= 0) {
                PhotoViewer.this.selectedPhotosAdapter.notifyItemRemoved(photoChecked);
                PhotoViewer.this.updateSelectedCount();
            }
        }

        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            PhotoPickerPhotoCell photoPickerPhotoCell = (PhotoPickerPhotoCell) viewHolder.itemView;
            photoPickerPhotoCell.itemWidth = AndroidUtilities.dp(82.0f);
            BackupImageView backupImageView = photoPickerPhotoCell.imageView;
            backupImageView.setOrientation(0, true);
            Object obj = PhotoViewer.this.placeProvider.getSelectedPhotos().get(PhotoViewer.this.placeProvider.getSelectedPhotosOrder().get(i));
            if (obj instanceof PhotoEntry) {
                PhotoEntry photoEntry = (PhotoEntry) obj;
                photoPickerPhotoCell.setTag(photoEntry);
                photoPickerPhotoCell.videoInfoContainer.setVisibility(4);
                String str = photoEntry.thumbPath;
                if (str != null) {
                    backupImageView.setImage(str, null, this.mContext.getResources().getDrawable(NUM));
                } else if (photoEntry.path != null) {
                    backupImageView.setOrientation(photoEntry.orientation, true);
                    String str2 = ":";
                    StringBuilder stringBuilder;
                    if (photoEntry.isVideo) {
                        photoPickerPhotoCell.videoInfoContainer.setVisibility(0);
                        photoPickerPhotoCell.videoTextView.setText(AndroidUtilities.formatShortDuration(photoEntry.duration));
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("vthumb://");
                        stringBuilder.append(photoEntry.imageId);
                        stringBuilder.append(str2);
                        stringBuilder.append(photoEntry.path);
                        backupImageView.setImage(stringBuilder.toString(), null, this.mContext.getResources().getDrawable(NUM));
                    } else {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("thumb://");
                        stringBuilder.append(photoEntry.imageId);
                        stringBuilder.append(str2);
                        stringBuilder.append(photoEntry.path);
                        backupImageView.setImage(stringBuilder.toString(), null, this.mContext.getResources().getDrawable(NUM));
                    }
                } else {
                    backupImageView.setImageResource(NUM);
                }
                photoPickerPhotoCell.setChecked(-1, true, false);
                photoPickerPhotoCell.checkBox.setVisibility(0);
            } else if (obj instanceof SearchImage) {
                SearchImage searchImage = (SearchImage) obj;
                photoPickerPhotoCell.setTag(searchImage);
                photoPickerPhotoCell.setImage(searchImage);
                photoPickerPhotoCell.videoInfoContainer.setVisibility(4);
                photoPickerPhotoCell.setChecked(-1, true, false);
                photoPickerPhotoCell.checkBox.setVisibility(0);
            }
        }
    }

    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    public void onLongPress(MotionEvent motionEvent) {
    }

    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        return false;
    }

    public void onShowPress(MotionEvent motionEvent) {
    }

    public /* synthetic */ void lambda$new$0$PhotoViewer() {
        toggleMiniProgressInternal(true);
    }

    public static PhotoViewer getPipInstance() {
        return PipInstance;
    }

    public static PhotoViewer getInstance() {
        PhotoViewer photoViewer = Instance;
        if (photoViewer == null) {
            synchronized (PhotoViewer.class) {
                photoViewer = Instance;
                if (photoViewer == null) {
                    photoViewer = new PhotoViewer();
                    Instance = photoViewer;
                }
            }
        }
        return photoViewer;
    }

    public boolean isOpenedFullScreenVideo() {
        return this.openedFullScreenVideo;
    }

    public static boolean hasInstance() {
        return Instance != null;
    }

    public PhotoViewer() {
        this.blackPaint.setColor(-16777216);
    }

    /* JADX WARNING: Removed duplicated region for block: B:243:0x04f0  */
    /* JADX WARNING: Removed duplicated region for block: B:243:0x04f0  */
    /* JADX WARNING: Removed duplicated region for block: B:243:0x04f0  */
    /* JADX WARNING: Removed duplicated region for block: B:243:0x04f0  */
    /* JADX WARNING: Missing block: B:30:0x0079, code skipped:
            if (org.telegram.messenger.MessageObject.isVideoDocument(r0.currentBotInlineResult.document) != false) goto L_0x007b;
     */
    /* JADX WARNING: Missing block: B:224:0x04a2, code skipped:
            if (((org.telegram.messenger.MessageObject) r1.get(r1.size() - 1)).getDialogId() != r0.mergeDialogId) goto L_0x04e3;
     */
    /* JADX WARNING: Missing block: B:237:0x04e1, code skipped:
            if (((org.telegram.messenger.MessageObject) r0.imagesArrTemp.get(0)).getDialogId() != r0.mergeDialogId) goto L_0x04e3;
     */
    public void didReceivedNotification(int r21, int r22, java.lang.Object... r23) {
        /*
        r20 = this;
        r0 = r20;
        r1 = r21;
        r2 = org.telegram.messenger.NotificationCenter.fileDidFailToLoad;
        r3 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r4 = 3;
        r5 = 1;
        r6 = 0;
        if (r1 != r2) goto L_0x0030;
    L_0x000d:
        r1 = r23[r6];
        r1 = (java.lang.String) r1;
    L_0x0011:
        if (r6 >= r4) goto L_0x0606;
    L_0x0013:
        r2 = r0.currentFileNames;
        r7 = r2[r6];
        if (r7 == 0) goto L_0x002d;
    L_0x0019:
        r2 = r2[r6];
        r2 = r2.equals(r1);
        if (r2 == 0) goto L_0x002d;
    L_0x0021:
        r1 = r0.photoProgressViews;
        r1 = r1[r6];
        r1.setProgress(r3, r5);
        r0.checkProgress(r6, r5);
        goto L_0x0606;
    L_0x002d:
        r6 = r6 + 1;
        goto L_0x0011;
    L_0x0030:
        r2 = org.telegram.messenger.NotificationCenter.fileDidLoad;
        if (r1 != r2) goto L_0x008b;
    L_0x0034:
        r1 = r23[r6];
        r1 = (java.lang.String) r1;
        r2 = 0;
    L_0x0039:
        if (r2 >= r4) goto L_0x0606;
    L_0x003b:
        r7 = r0.currentFileNames;
        r8 = r7[r2];
        if (r8 == 0) goto L_0x0088;
    L_0x0041:
        r7 = r7[r2];
        r7 = r7.equals(r1);
        if (r7 == 0) goto L_0x0088;
    L_0x0049:
        r1 = r0.photoProgressViews;
        r1 = r1[r2];
        r1.setProgress(r3, r5);
        r0.checkProgress(r2, r5);
        r1 = r0.videoPlayer;
        if (r1 != 0) goto L_0x007e;
    L_0x0057:
        if (r2 != 0) goto L_0x007e;
    L_0x0059:
        r1 = r0.currentMessageObject;
        if (r1 == 0) goto L_0x0063;
    L_0x005d:
        r1 = r1.isVideo();
        if (r1 != 0) goto L_0x007b;
    L_0x0063:
        r1 = r0.currentBotInlineResult;
        if (r1 == 0) goto L_0x007e;
    L_0x0067:
        r1 = r1.type;
        r3 = "video";
        r1 = r1.equals(r3);
        if (r1 != 0) goto L_0x007b;
    L_0x0071:
        r1 = r0.currentBotInlineResult;
        r1 = r1.document;
        r1 = org.telegram.messenger.MessageObject.isVideoDocument(r1);
        if (r1 == 0) goto L_0x007e;
    L_0x007b:
        r0.onActionClick(r6);
    L_0x007e:
        if (r2 != 0) goto L_0x0606;
    L_0x0080:
        r1 = r0.videoPlayer;
        if (r1 == 0) goto L_0x0606;
    L_0x0084:
        r0.currentVideoFinishedLoading = r5;
        goto L_0x0606;
    L_0x0088:
        r2 = r2 + 1;
        goto L_0x0039;
    L_0x008b:
        r2 = org.telegram.messenger.NotificationCenter.FileLoadProgressChanged;
        r7 = 0;
        if (r1 != r2) goto L_0x013f;
    L_0x0091:
        r1 = r23[r6];
        r1 = (java.lang.String) r1;
        r2 = 0;
    L_0x0096:
        if (r2 >= r4) goto L_0x0606;
    L_0x0098:
        r9 = r0.currentFileNames;
        r10 = r9[r2];
        if (r10 == 0) goto L_0x0138;
    L_0x009e:
        r9 = r9[r2];
        r9 = r9.equals(r1);
        if (r9 == 0) goto L_0x0138;
    L_0x00a6:
        r9 = r23[r5];
        r9 = (java.lang.Float) r9;
        r10 = r0.photoProgressViews;
        r10 = r10[r2];
        r11 = r9.floatValue();
        r10.setProgress(r11, r5);
        if (r2 != 0) goto L_0x0138;
    L_0x00b7:
        r10 = r0.videoPlayer;
        if (r10 == 0) goto L_0x0138;
    L_0x00bb:
        r10 = r0.videoPlayerSeekbar;
        if (r10 == 0) goto L_0x0138;
    L_0x00bf:
        r10 = r0.currentVideoFinishedLoading;
        r11 = -NUM; // 0xffffffffbvar_ float:-1.0 double:NaN;
        if (r10 == 0) goto L_0x00c6;
    L_0x00c5:
        goto L_0x011c;
    L_0x00c6:
        r12 = android.os.SystemClock.elapsedRealtime();
        r14 = r0.lastBufferedPositionCheck;
        r14 = r12 - r14;
        r14 = java.lang.Math.abs(r14);
        r16 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        r10 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1));
        if (r10 < 0) goto L_0x011a;
    L_0x00d8:
        r10 = r0.seekToProgressPending;
        r14 = 0;
        r15 = (r10 > r14 ? 1 : (r10 == r14 ? 0 : -1));
        if (r15 != 0) goto L_0x0102;
    L_0x00df:
        r10 = r0.videoPlayer;
        r14 = r10.getDuration();
        r10 = r0.videoPlayer;
        r3 = r10.getCurrentPosition();
        r10 = (r14 > r7 ? 1 : (r14 == r7 ? 0 : -1));
        if (r10 < 0) goto L_0x0101;
    L_0x00ef:
        r17 = -NUM; // 0xNUM float:1.4E-45 double:-4.9E-324;
        r10 = (r14 > r17 ? 1 : (r14 == r17 ? 0 : -1));
        if (r10 == 0) goto L_0x0101;
    L_0x00f8:
        r10 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1));
        if (r10 < 0) goto L_0x0101;
    L_0x00fc:
        r3 = (float) r3;
        r4 = (float) r14;
        r3 = r3 / r4;
        r10 = r3;
        goto L_0x0102;
    L_0x0101:
        r10 = 0;
    L_0x0102:
        r3 = r0.isStreaming;
        if (r3 == 0) goto L_0x0115;
    L_0x0106:
        r3 = r0.currentAccount;
        r3 = org.telegram.messenger.FileLoader.getInstance(r3);
        r4 = r0.currentFileNames;
        r4 = r4[r6];
        r3 = r3.getBufferedProgressFromPosition(r10, r4);
        goto L_0x0117;
    L_0x0115:
        r3 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
    L_0x0117:
        r0.lastBufferedPositionCheck = r12;
        goto L_0x011c;
    L_0x011a:
        r3 = -NUM; // 0xffffffffbvar_ float:-1.0 double:NaN;
    L_0x011c:
        r4 = (r3 > r11 ? 1 : (r3 == r11 ? 0 : -1));
        if (r4 == 0) goto L_0x0131;
    L_0x0120:
        r4 = r0.videoPlayerSeekbar;
        r4.setBufferedProgress(r3);
        r4 = r0.pipVideoView;
        if (r4 == 0) goto L_0x012c;
    L_0x0129:
        r4.setBufferedProgress(r3);
    L_0x012c:
        r3 = r0.videoPlayerControlFrameLayout;
        r3.invalidate();
    L_0x0131:
        r3 = r9.floatValue();
        r0.checkBufferedProgress(r3);
    L_0x0138:
        r2 = r2 + 1;
        r3 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r4 = 3;
        goto L_0x0096;
    L_0x013f:
        r2 = org.telegram.messenger.NotificationCenter.dialogPhotosLoaded;
        r3 = 4;
        r4 = 2;
        r9 = -1;
        if (r1 != r2) goto L_0x028d;
    L_0x0146:
        r2 = 3;
        r1 = r23[r2];
        r1 = (java.lang.Integer) r1;
        r1 = r1.intValue();
        r2 = r23[r6];
        r2 = (java.lang.Integer) r2;
        r2 = r2.intValue();
        r7 = r0.avatarsDialogId;
        if (r7 != r2) goto L_0x0606;
    L_0x015b:
        r2 = r0.classGuid;
        if (r2 != r1) goto L_0x0606;
    L_0x015f:
        r1 = r23[r4];
        r1 = (java.lang.Boolean) r1;
        r1 = r1.booleanValue();
        r2 = r23[r3];
        r2 = (java.util.ArrayList) r2;
        r3 = r2.isEmpty();
        if (r3 == 0) goto L_0x0172;
    L_0x0171:
        return;
    L_0x0172:
        r3 = r0.imagesArrLocations;
        r3.clear();
        r3 = r0.imagesArrLocationsSizes;
        r3.clear();
        r3 = r0.avatarsArr;
        r3.clear();
        r3 = 0;
        r4 = -1;
    L_0x0183:
        r7 = r2.size();
        if (r3 >= r7) goto L_0x0201;
    L_0x0189:
        r7 = r2.get(r3);
        r7 = (org.telegram.tgnet.TLRPC.Photo) r7;
        if (r7 == 0) goto L_0x01fe;
    L_0x0191:
        r8 = r7 instanceof org.telegram.tgnet.TLRPC.TL_photoEmpty;
        if (r8 != 0) goto L_0x01fe;
    L_0x0195:
        r8 = r7.sizes;
        if (r8 != 0) goto L_0x019a;
    L_0x0199:
        goto L_0x01fe;
    L_0x019a:
        r10 = 640; // 0x280 float:8.97E-43 double:3.16E-321;
        r8 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r8, r10);
        if (r8 == 0) goto L_0x01fe;
    L_0x01a2:
        if (r4 != r9) goto L_0x01d7;
    L_0x01a4:
        r10 = r0.currentFileLocation;
        if (r10 == 0) goto L_0x01d7;
    L_0x01a8:
        r10 = 0;
    L_0x01a9:
        r11 = r7.sizes;
        r11 = r11.size();
        if (r10 >= r11) goto L_0x01d7;
    L_0x01b1:
        r11 = r7.sizes;
        r11 = r11.get(r10);
        r11 = (org.telegram.tgnet.TLRPC.PhotoSize) r11;
        r11 = r11.location;
        r12 = r11.local_id;
        r13 = r0.currentFileLocation;
        r13 = r13.location;
        r14 = r13.local_id;
        if (r12 != r14) goto L_0x01d4;
    L_0x01c5:
        r11 = r11.volume_id;
        r13 = r13.volume_id;
        r15 = (r11 > r13 ? 1 : (r11 == r13 ? 0 : -1));
        if (r15 != 0) goto L_0x01d4;
    L_0x01cd:
        r4 = r0.imagesArrLocations;
        r4 = r4.size();
        goto L_0x01d7;
    L_0x01d4:
        r10 = r10 + 1;
        goto L_0x01a9;
    L_0x01d7:
        r10 = r7.dc_id;
        if (r10 == 0) goto L_0x01e3;
    L_0x01db:
        r11 = r8.location;
        r11.dc_id = r10;
        r10 = r7.file_reference;
        r11.file_reference = r10;
    L_0x01e3:
        r10 = org.telegram.messenger.ImageLocation.getForPhoto(r8, r7);
        if (r10 == 0) goto L_0x01fe;
    L_0x01e9:
        r11 = r0.imagesArrLocations;
        r11.add(r10);
        r10 = r0.imagesArrLocationsSizes;
        r8 = r8.size;
        r8 = java.lang.Integer.valueOf(r8);
        r10.add(r8);
        r8 = r0.avatarsArr;
        r8.add(r7);
    L_0x01fe:
        r3 = r3 + 1;
        goto L_0x0183;
    L_0x0201:
        r2 = r0.avatarsArr;
        r2 = r2.isEmpty();
        r3 = 6;
        if (r2 != 0) goto L_0x0210;
    L_0x020a:
        r2 = r0.menuItem;
        r2.showSubItem(r3);
        goto L_0x0215;
    L_0x0210:
        r2 = r0.menuItem;
        r2.hideSubItem(r3);
    L_0x0215:
        r0.needSearchImageInArr = r6;
        r0.currentIndex = r9;
        if (r4 == r9) goto L_0x021f;
    L_0x021b:
        r0.setImageIndex(r4, r5);
        goto L_0x0277;
    L_0x021f:
        r2 = r0.avatarsDialogId;
        r3 = 0;
        if (r2 <= 0) goto L_0x0235;
    L_0x0224:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.MessagesController.getInstance(r2);
        r4 = r0.avatarsDialogId;
        r4 = java.lang.Integer.valueOf(r4);
        r2 = r2.getUser(r4);
        goto L_0x024b;
    L_0x0235:
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.MessagesController.getInstance(r2);
        r4 = r0.avatarsDialogId;
        r4 = -r4;
        r4 = java.lang.Integer.valueOf(r4);
        r2 = r2.getChat(r4);
        r19 = r3;
        r3 = r2;
        r2 = r19;
    L_0x024b:
        if (r2 != 0) goto L_0x024f;
    L_0x024d:
        if (r3 == 0) goto L_0x0277;
    L_0x024f:
        if (r2 == 0) goto L_0x0256;
    L_0x0251:
        r2 = org.telegram.messenger.ImageLocation.getForUser(r2, r5);
        goto L_0x025a;
    L_0x0256:
        r2 = org.telegram.messenger.ImageLocation.getForChat(r3, r5);
    L_0x025a:
        if (r2 == 0) goto L_0x0277;
    L_0x025c:
        r3 = r0.imagesArrLocations;
        r3.add(r6, r2);
        r2 = r0.avatarsArr;
        r3 = new org.telegram.tgnet.TLRPC$TL_photoEmpty;
        r3.<init>();
        r2.add(r6, r3);
        r2 = r0.imagesArrLocationsSizes;
        r3 = java.lang.Integer.valueOf(r6);
        r2.add(r6, r3);
        r0.setImageIndex(r6, r5);
    L_0x0277:
        if (r1 == 0) goto L_0x0606;
    L_0x0279:
        r1 = r0.currentAccount;
        r2 = org.telegram.messenger.MessagesController.getInstance(r1);
        r3 = r0.avatarsDialogId;
        r4 = 80;
        r5 = 0;
        r7 = 0;
        r8 = r0.classGuid;
        r2.loadDialogPhotos(r3, r4, r5, r7, r8);
        goto L_0x0606;
    L_0x028d:
        r2 = org.telegram.messenger.NotificationCenter.mediaCountDidLoad;
        if (r1 != r2) goto L_0x0347;
    L_0x0291:
        r1 = r23[r6];
        r1 = (java.lang.Long) r1;
        r1 = r1.longValue();
        r7 = r0.currentDialogId;
        r3 = (r1 > r7 ? 1 : (r1 == r7 ? 0 : -1));
        if (r3 == 0) goto L_0x02a5;
    L_0x029f:
        r7 = r0.mergeDialogId;
        r3 = (r1 > r7 ? 1 : (r1 == r7 ? 0 : -1));
        if (r3 != 0) goto L_0x0606;
    L_0x02a5:
        r7 = r0.currentDialogId;
        r3 = (r1 > r7 ? 1 : (r1 == r7 ? 0 : -1));
        if (r3 != 0) goto L_0x02b6;
    L_0x02ab:
        r1 = r23[r5];
        r1 = (java.lang.Integer) r1;
        r1 = r1.intValue();
        r0.totalImagesCount = r1;
        goto L_0x02c6;
    L_0x02b6:
        r7 = r0.mergeDialogId;
        r3 = (r1 > r7 ? 1 : (r1 == r7 ? 0 : -1));
        if (r3 != 0) goto L_0x02c6;
    L_0x02bc:
        r1 = r23[r5];
        r1 = (java.lang.Integer) r1;
        r1 = r1.intValue();
        r0.totalImagesCountMerge = r1;
    L_0x02c6:
        r1 = r0.needSearchImageInArr;
        if (r1 == 0) goto L_0x02e7;
    L_0x02ca:
        r1 = r0.isFirstLoading;
        if (r1 == 0) goto L_0x02e7;
    L_0x02ce:
        r0.isFirstLoading = r6;
        r0.loadingMoreImages = r5;
        r1 = r0.currentAccount;
        r2 = org.telegram.messenger.MediaDataController.getInstance(r1);
        r3 = r0.currentDialogId;
        r5 = 80;
        r6 = 0;
        r7 = r0.sharedMediaType;
        r8 = 1;
        r9 = r0.classGuid;
        r2.loadMedia(r3, r5, r6, r7, r8, r9);
        goto L_0x0606;
    L_0x02e7:
        r1 = r0.imagesArr;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x0606;
    L_0x02ef:
        r1 = r0.opennedFromMedia;
        r2 = NUM; // 0x7f0e0733 float:1.8878776E38 double:1.053163067E-314;
        r3 = "Of";
        if (r1 == 0) goto L_0x0319;
    L_0x02f8:
        r1 = r0.actionBar;
        r4 = new java.lang.Object[r4];
        r7 = r0.currentIndex;
        r7 = r7 + r5;
        r7 = java.lang.Integer.valueOf(r7);
        r4[r6] = r7;
        r6 = r0.totalImagesCount;
        r7 = r0.totalImagesCountMerge;
        r6 = r6 + r7;
        r6 = java.lang.Integer.valueOf(r6);
        r4[r5] = r6;
        r2 = org.telegram.messenger.LocaleController.formatString(r3, r2, r4);
        r1.setTitle(r2);
        goto L_0x0606;
    L_0x0319:
        r1 = r0.actionBar;
        r4 = new java.lang.Object[r4];
        r7 = r0.totalImagesCount;
        r8 = r0.totalImagesCountMerge;
        r7 = r7 + r8;
        r8 = r0.imagesArr;
        r8 = r8.size();
        r7 = r7 - r8;
        r8 = r0.currentIndex;
        r7 = r7 + r8;
        r7 = r7 + r5;
        r7 = java.lang.Integer.valueOf(r7);
        r4[r6] = r7;
        r6 = r0.totalImagesCount;
        r7 = r0.totalImagesCountMerge;
        r6 = r6 + r7;
        r6 = java.lang.Integer.valueOf(r6);
        r4[r5] = r6;
        r2 = org.telegram.messenger.LocaleController.formatString(r3, r2, r4);
        r1.setTitle(r2);
        goto L_0x0606;
    L_0x0347:
        r2 = org.telegram.messenger.NotificationCenter.mediaDidLoad;
        if (r1 != r2) goto L_0x0590;
    L_0x034b:
        r1 = r23[r6];
        r1 = (java.lang.Long) r1;
        r1 = r1.longValue();
        r3 = 3;
        r3 = r23[r3];
        r3 = (java.lang.Integer) r3;
        r3 = r3.intValue();
        r10 = r0.currentDialogId;
        r12 = (r1 > r10 ? 1 : (r1 == r10 ? 0 : -1));
        if (r12 == 0) goto L_0x0368;
    L_0x0362:
        r10 = r0.mergeDialogId;
        r12 = (r1 > r10 ? 1 : (r1 == r10 ? 0 : -1));
        if (r12 != 0) goto L_0x0606;
    L_0x0368:
        r10 = r0.classGuid;
        if (r3 != r10) goto L_0x0606;
    L_0x036c:
        r0.loadingMoreImages = r6;
        r10 = r0.currentDialogId;
        r3 = (r1 > r10 ? 1 : (r1 == r10 ? 0 : -1));
        if (r3 != 0) goto L_0x0376;
    L_0x0374:
        r1 = 0;
        goto L_0x0377;
    L_0x0376:
        r1 = 1;
    L_0x0377:
        r2 = r23[r4];
        r2 = (java.util.ArrayList) r2;
        r3 = r0.endReached;
        r10 = 5;
        r10 = r23[r10];
        r10 = (java.lang.Boolean) r10;
        r10 = r10.booleanValue();
        r3[r1] = r10;
        r3 = r0.needSearchImageInArr;
        if (r3 == 0) goto L_0x052a;
    L_0x038c:
        r3 = r2.isEmpty();
        if (r3 == 0) goto L_0x039d;
    L_0x0392:
        if (r1 != 0) goto L_0x039a;
    L_0x0394:
        r10 = r0.mergeDialogId;
        r3 = (r10 > r7 ? 1 : (r10 == r7 ? 0 : -1));
        if (r3 != 0) goto L_0x039d;
    L_0x039a:
        r0.needSearchImageInArr = r6;
        return;
    L_0x039d:
        r3 = r0.imagesArr;
        r10 = r0.currentIndex;
        r3 = r3.get(r10);
        r3 = (org.telegram.messenger.MessageObject) r3;
        r10 = 0;
        r11 = 0;
        r12 = -1;
    L_0x03aa:
        r13 = r2.size();
        if (r10 >= r13) goto L_0x03ff;
    L_0x03b0:
        r13 = r2.get(r10);
        r13 = (org.telegram.messenger.MessageObject) r13;
        r14 = r0.imagesByIdsTemp;
        r14 = r14[r1];
        r15 = r13.getId();
        r14 = r14.indexOfKey(r15);
        if (r14 >= 0) goto L_0x03fc;
    L_0x03c4:
        r14 = r0.imagesByIdsTemp;
        r14 = r14[r1];
        r15 = r13.getId();
        r14.put(r15, r13);
        r14 = r0.opennedFromMedia;
        if (r14 == 0) goto L_0x03e6;
    L_0x03d3:
        r14 = r0.imagesArrTemp;
        r14.add(r13);
        r13 = r13.getId();
        r14 = r3.getId();
        if (r13 != r14) goto L_0x03e3;
    L_0x03e2:
        r12 = r11;
    L_0x03e3:
        r11 = r11 + 1;
        goto L_0x03fc;
    L_0x03e6:
        r11 = r11 + 1;
        r14 = r0.imagesArrTemp;
        r14.add(r6, r13);
        r13 = r13.getId();
        r14 = r3.getId();
        if (r13 != r14) goto L_0x03fc;
    L_0x03f7:
        r12 = r2.size();
        r12 = r12 - r11;
    L_0x03fc:
        r10 = r10 + 1;
        goto L_0x03aa;
    L_0x03ff:
        if (r11 != 0) goto L_0x0413;
    L_0x0401:
        if (r1 != 0) goto L_0x0409;
    L_0x0403:
        r2 = r0.mergeDialogId;
        r10 = (r2 > r7 ? 1 : (r2 == r7 ? 0 : -1));
        if (r10 != 0) goto L_0x0413;
    L_0x0409:
        r2 = r0.imagesArr;
        r2 = r2.size();
        r0.totalImagesCount = r2;
        r0.totalImagesCountMerge = r6;
    L_0x0413:
        if (r12 == r9) goto L_0x0458;
    L_0x0415:
        r1 = r0.imagesArr;
        r1.clear();
        r1 = r0.imagesArr;
        r2 = r0.imagesArrTemp;
        r1.addAll(r2);
        r1 = 0;
    L_0x0422:
        if (r1 >= r4) goto L_0x043a;
    L_0x0424:
        r2 = r0.imagesByIds;
        r3 = r0.imagesByIdsTemp;
        r3 = r3[r1];
        r3 = r3.clone();
        r2[r1] = r3;
        r2 = r0.imagesByIdsTemp;
        r2 = r2[r1];
        r2.clear();
        r1 = r1 + 1;
        goto L_0x0422;
    L_0x043a:
        r1 = r0.imagesArrTemp;
        r1.clear();
        r0.needSearchImageInArr = r6;
        r0.currentIndex = r9;
        r1 = r0.imagesArr;
        r1 = r1.size();
        if (r12 < r1) goto L_0x0453;
    L_0x044b:
        r1 = r0.imagesArr;
        r1 = r1.size();
        r12 = r1 + -1;
    L_0x0453:
        r0.setImageIndex(r12, r5);
        goto L_0x0606;
    L_0x0458:
        r2 = r0.opennedFromMedia;
        if (r2 == 0) goto L_0x04a5;
    L_0x045c:
        r2 = r0.imagesArrTemp;
        r2 = r2.isEmpty();
        if (r2 == 0) goto L_0x0466;
    L_0x0464:
        r2 = 0;
        goto L_0x0477;
    L_0x0466:
        r2 = r0.imagesArrTemp;
        r3 = r2.size();
        r3 = r3 - r5;
        r2 = r2.get(r3);
        r2 = (org.telegram.messenger.MessageObject) r2;
        r2 = r2.getId();
    L_0x0477:
        if (r1 != 0) goto L_0x04e9;
    L_0x0479:
        r3 = r0.endReached;
        r3 = r3[r1];
        if (r3 == 0) goto L_0x04e9;
    L_0x047f:
        r3 = r0.mergeDialogId;
        r9 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1));
        if (r9 == 0) goto L_0x04e9;
    L_0x0485:
        r1 = r0.imagesArrTemp;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x04e6;
    L_0x048d:
        r1 = r0.imagesArrTemp;
        r3 = r1.size();
        r3 = r3 - r5;
        r1 = r1.get(r3);
        r1 = (org.telegram.messenger.MessageObject) r1;
        r3 = r1.getDialogId();
        r7 = r0.mergeDialogId;
        r1 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1));
        if (r1 == 0) goto L_0x04e6;
    L_0x04a4:
        goto L_0x04e3;
    L_0x04a5:
        r2 = r0.imagesArrTemp;
        r2 = r2.isEmpty();
        if (r2 == 0) goto L_0x04af;
    L_0x04ad:
        r2 = 0;
        goto L_0x04bb;
    L_0x04af:
        r2 = r0.imagesArrTemp;
        r2 = r2.get(r6);
        r2 = (org.telegram.messenger.MessageObject) r2;
        r2 = r2.getId();
    L_0x04bb:
        if (r1 != 0) goto L_0x04e9;
    L_0x04bd:
        r3 = r0.endReached;
        r3 = r3[r1];
        if (r3 == 0) goto L_0x04e9;
    L_0x04c3:
        r3 = r0.mergeDialogId;
        r9 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1));
        if (r9 == 0) goto L_0x04e9;
    L_0x04c9:
        r1 = r0.imagesArrTemp;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x04e6;
    L_0x04d1:
        r1 = r0.imagesArrTemp;
        r1 = r1.get(r6);
        r1 = (org.telegram.messenger.MessageObject) r1;
        r3 = r1.getDialogId();
        r7 = r0.mergeDialogId;
        r1 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1));
        if (r1 == 0) goto L_0x04e6;
    L_0x04e3:
        r1 = 1;
        r10 = 0;
        goto L_0x04ea;
    L_0x04e6:
        r10 = r2;
        r1 = 1;
        goto L_0x04ea;
    L_0x04e9:
        r10 = r2;
    L_0x04ea:
        r2 = r0.endReached;
        r2 = r2[r1];
        if (r2 != 0) goto L_0x0606;
    L_0x04f0:
        r0.loadingMoreImages = r5;
        r2 = r0.opennedFromMedia;
        if (r2 == 0) goto L_0x0510;
    L_0x04f6:
        r2 = r0.currentAccount;
        r6 = org.telegram.messenger.MediaDataController.getInstance(r2);
        if (r1 != 0) goto L_0x0501;
    L_0x04fe:
        r1 = r0.currentDialogId;
        goto L_0x0503;
    L_0x0501:
        r1 = r0.mergeDialogId;
    L_0x0503:
        r7 = r1;
        r9 = 80;
        r11 = r0.sharedMediaType;
        r12 = 1;
        r13 = r0.classGuid;
        r6.loadMedia(r7, r9, r10, r11, r12, r13);
        goto L_0x0606;
    L_0x0510:
        r2 = r0.currentAccount;
        r6 = org.telegram.messenger.MediaDataController.getInstance(r2);
        if (r1 != 0) goto L_0x051b;
    L_0x0518:
        r1 = r0.currentDialogId;
        goto L_0x051d;
    L_0x051b:
        r1 = r0.mergeDialogId;
    L_0x051d:
        r7 = r1;
        r9 = 80;
        r11 = r0.sharedMediaType;
        r12 = 1;
        r13 = r0.classGuid;
        r6.loadMedia(r7, r9, r10, r11, r12, r13);
        goto L_0x0606;
    L_0x052a:
        r2 = r2.iterator();
        r3 = 0;
    L_0x052f:
        r4 = r2.hasNext();
        if (r4 == 0) goto L_0x0566;
    L_0x0535:
        r4 = r2.next();
        r4 = (org.telegram.messenger.MessageObject) r4;
        r7 = r0.imagesByIds;
        r7 = r7[r1];
        r8 = r4.getId();
        r7 = r7.indexOfKey(r8);
        if (r7 >= 0) goto L_0x052f;
    L_0x0549:
        r3 = r3 + 1;
        r7 = r0.opennedFromMedia;
        if (r7 == 0) goto L_0x0555;
    L_0x054f:
        r7 = r0.imagesArr;
        r7.add(r4);
        goto L_0x055a;
    L_0x0555:
        r7 = r0.imagesArr;
        r7.add(r6, r4);
    L_0x055a:
        r7 = r0.imagesByIds;
        r7 = r7[r1];
        r8 = r4.getId();
        r7.put(r8, r4);
        goto L_0x052f;
    L_0x0566:
        r1 = r0.opennedFromMedia;
        if (r1 == 0) goto L_0x0578;
    L_0x056a:
        if (r3 != 0) goto L_0x0606;
    L_0x056c:
        r1 = r0.imagesArr;
        r1 = r1.size();
        r0.totalImagesCount = r1;
        r0.totalImagesCountMerge = r6;
        goto L_0x0606;
    L_0x0578:
        if (r3 == 0) goto L_0x0584;
    L_0x057a:
        r1 = r0.currentIndex;
        r0.currentIndex = r9;
        r1 = r1 + r3;
        r0.setImageIndex(r1, r5);
        goto L_0x0606;
    L_0x0584:
        r1 = r0.imagesArr;
        r1 = r1.size();
        r0.totalImagesCount = r1;
        r0.totalImagesCountMerge = r6;
        goto L_0x0606;
    L_0x0590:
        r2 = org.telegram.messenger.NotificationCenter.emojiDidLoad;
        if (r1 != r2) goto L_0x059c;
    L_0x0594:
        r1 = r0.captionTextView;
        if (r1 == 0) goto L_0x0606;
    L_0x0598:
        r1.invalidate();
        goto L_0x0606;
    L_0x059c:
        r2 = org.telegram.messenger.NotificationCenter.filePreparingFailed;
        if (r1 != r2) goto L_0x05d6;
    L_0x05a0:
        r1 = r23[r6];
        r1 = (org.telegram.messenger.MessageObject) r1;
        r2 = r0.loadInitialVideo;
        if (r2 == 0) goto L_0x05b5;
    L_0x05a8:
        r0.loadInitialVideo = r6;
        r1 = r0.progressView;
        r1.setVisibility(r3);
        r1 = r0.currentPlayingVideoFile;
        r0.preparePlayer(r1, r6, r6);
        goto L_0x0606;
    L_0x05b5:
        r2 = r0.tryStartRequestPreviewOnFinish;
        if (r2 == 0) goto L_0x05ca;
    L_0x05b9:
        r0.releasePlayer(r6);
        r1 = org.telegram.messenger.MediaController.getInstance();
        r2 = r0.videoPreviewMessageObject;
        r1 = r1.scheduleVideoConvert(r2, r5);
        r1 = r1 ^ r5;
        r0.tryStartRequestPreviewOnFinish = r1;
        goto L_0x0606;
    L_0x05ca:
        r2 = r0.videoPreviewMessageObject;
        if (r1 != r2) goto L_0x0606;
    L_0x05ce:
        r0.requestingPreview = r6;
        r1 = r0.progressView;
        r1.setVisibility(r3);
        goto L_0x0606;
    L_0x05d6:
        r2 = org.telegram.messenger.NotificationCenter.fileNewChunkAvailable;
        if (r1 != r2) goto L_0x0606;
    L_0x05da:
        r1 = r23[r6];
        r1 = (org.telegram.messenger.MessageObject) r1;
        r2 = r0.videoPreviewMessageObject;
        if (r1 != r2) goto L_0x0606;
    L_0x05e2:
        r1 = r23[r5];
        r1 = (java.lang.String) r1;
        r2 = 3;
        r2 = r23[r2];
        r2 = (java.lang.Long) r2;
        r9 = r2.longValue();
        r2 = (r9 > r7 ? 1 : (r9 == r7 ? 0 : -1));
        if (r2 == 0) goto L_0x0606;
    L_0x05f3:
        r0.requestingPreview = r6;
        r2 = r0.progressView;
        r2.setVisibility(r3);
        r2 = new java.io.File;
        r2.<init>(r1);
        r1 = android.net.Uri.fromFile(r2);
        r0.preparePlayer(r1, r6, r5);
    L_0x0606:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.didReceivedNotification(int, int, java.lang.Object[]):void");
    }

    private void showDownloadAlert() {
        Builder builder = new Builder(this.parentActivity);
        builder.setTitle(LocaleController.getString("AppName", NUM));
        builder.setPositiveButton(LocaleController.getString("OK", NUM), null);
        MessageObject messageObject = this.currentMessageObject;
        int i = 0;
        if (messageObject != null && messageObject.isVideo() && FileLoader.getInstance(this.currentMessageObject.currentAccount).isLoadingFile(this.currentFileNames[0])) {
            i = 1;
        }
        if (i != 0) {
            builder.setMessage(LocaleController.getString("PleaseStreamDownload", NUM));
        } else {
            builder.setMessage(LocaleController.getString("PleaseDownload", NUM));
        }
        showAlertDialog(builder);
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x0098 */
    /* JADX WARNING: Can't wrap try/catch for region: R(4:37|38|39|40) */
    private void onSharePressed() {
        /*
        r6 = this;
        r0 = r6.parentActivity;
        if (r0 == 0) goto L_0x00c4;
    L_0x0004:
        r0 = r6.allowShare;
        if (r0 != 0) goto L_0x000a;
    L_0x0008:
        goto L_0x00c4;
    L_0x000a:
        r0 = r6.currentMessageObject;	 Catch:{ Exception -> 0x00c0 }
        r1 = 1;
        r2 = 0;
        r3 = 0;
        if (r0 == 0) goto L_0x0041;
    L_0x0011:
        r0 = r6.currentMessageObject;	 Catch:{ Exception -> 0x00c0 }
        r2 = r0.isVideo();	 Catch:{ Exception -> 0x00c0 }
        r0 = r6.currentMessageObject;	 Catch:{ Exception -> 0x00c0 }
        r0 = r0.messageOwner;	 Catch:{ Exception -> 0x00c0 }
        r0 = r0.attachPath;	 Catch:{ Exception -> 0x00c0 }
        r0 = android.text.TextUtils.isEmpty(r0);	 Catch:{ Exception -> 0x00c0 }
        if (r0 != 0) goto L_0x0036;
    L_0x0023:
        r0 = new java.io.File;	 Catch:{ Exception -> 0x00c0 }
        r4 = r6.currentMessageObject;	 Catch:{ Exception -> 0x00c0 }
        r4 = r4.messageOwner;	 Catch:{ Exception -> 0x00c0 }
        r4 = r4.attachPath;	 Catch:{ Exception -> 0x00c0 }
        r0.<init>(r4);	 Catch:{ Exception -> 0x00c0 }
        r4 = r0.exists();	 Catch:{ Exception -> 0x00c0 }
        if (r4 != 0) goto L_0x0035;
    L_0x0034:
        goto L_0x0036;
    L_0x0035:
        r3 = r0;
    L_0x0036:
        if (r3 != 0) goto L_0x0059;
    L_0x0038:
        r0 = r6.currentMessageObject;	 Catch:{ Exception -> 0x00c0 }
        r0 = r0.messageOwner;	 Catch:{ Exception -> 0x00c0 }
        r3 = org.telegram.messenger.FileLoader.getPathToMessage(r0);	 Catch:{ Exception -> 0x00c0 }
        goto L_0x0059;
    L_0x0041:
        r0 = r6.currentFileLocation;	 Catch:{ Exception -> 0x00c0 }
        if (r0 == 0) goto L_0x0059;
    L_0x0045:
        r0 = r6.currentFileLocation;	 Catch:{ Exception -> 0x00c0 }
        r0 = r0.location;	 Catch:{ Exception -> 0x00c0 }
        r3 = r6.avatarsDialogId;	 Catch:{ Exception -> 0x00c0 }
        if (r3 != 0) goto L_0x0054;
    L_0x004d:
        r3 = r6.isEvent;	 Catch:{ Exception -> 0x00c0 }
        if (r3 == 0) goto L_0x0052;
    L_0x0051:
        goto L_0x0054;
    L_0x0052:
        r3 = 0;
        goto L_0x0055;
    L_0x0054:
        r3 = 1;
    L_0x0055:
        r3 = org.telegram.messenger.FileLoader.getPathToAttach(r0, r3);	 Catch:{ Exception -> 0x00c0 }
    L_0x0059:
        r0 = r3.exists();	 Catch:{ Exception -> 0x00c0 }
        if (r0 == 0) goto L_0x00bc;
    L_0x005f:
        r0 = new android.content.Intent;	 Catch:{ Exception -> 0x00c0 }
        r4 = "android.intent.action.SEND";
        r0.<init>(r4);	 Catch:{ Exception -> 0x00c0 }
        if (r2 == 0) goto L_0x006e;
    L_0x0068:
        r2 = "video/mp4";
        r0.setType(r2);	 Catch:{ Exception -> 0x00c0 }
        goto L_0x0081;
    L_0x006e:
        r2 = r6.currentMessageObject;	 Catch:{ Exception -> 0x00c0 }
        if (r2 == 0) goto L_0x007c;
    L_0x0072:
        r2 = r6.currentMessageObject;	 Catch:{ Exception -> 0x00c0 }
        r2 = r2.getMimeType();	 Catch:{ Exception -> 0x00c0 }
        r0.setType(r2);	 Catch:{ Exception -> 0x00c0 }
        goto L_0x0081;
    L_0x007c:
        r2 = "image/jpeg";
        r0.setType(r2);	 Catch:{ Exception -> 0x00c0 }
    L_0x0081:
        r2 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x00c0 }
        r4 = 24;
        r5 = "android.intent.extra.STREAM";
        if (r2 < r4) goto L_0x00a0;
    L_0x0089:
        r2 = r6.parentActivity;	 Catch:{ Exception -> 0x0098 }
        r4 = "org.telegram.messenger.provider";
        r2 = androidx.core.content.FileProvider.getUriForFile(r2, r4, r3);	 Catch:{ Exception -> 0x0098 }
        r0.putExtra(r5, r2);	 Catch:{ Exception -> 0x0098 }
        r0.setFlags(r1);	 Catch:{ Exception -> 0x0098 }
        goto L_0x00a7;
    L_0x0098:
        r1 = android.net.Uri.fromFile(r3);	 Catch:{ Exception -> 0x00c0 }
        r0.putExtra(r5, r1);	 Catch:{ Exception -> 0x00c0 }
        goto L_0x00a7;
    L_0x00a0:
        r1 = android.net.Uri.fromFile(r3);	 Catch:{ Exception -> 0x00c0 }
        r0.putExtra(r5, r1);	 Catch:{ Exception -> 0x00c0 }
    L_0x00a7:
        r1 = r6.parentActivity;	 Catch:{ Exception -> 0x00c0 }
        r2 = "ShareFile";
        r3 = NUM; // 0x7f0e09f9 float:1.8880216E38 double:1.053163418E-314;
        r2 = org.telegram.messenger.LocaleController.getString(r2, r3);	 Catch:{ Exception -> 0x00c0 }
        r0 = android.content.Intent.createChooser(r0, r2);	 Catch:{ Exception -> 0x00c0 }
        r2 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        r1.startActivityForResult(r0, r2);	 Catch:{ Exception -> 0x00c0 }
        goto L_0x00c4;
    L_0x00bc:
        r6.showDownloadAlert();	 Catch:{ Exception -> 0x00c0 }
        goto L_0x00c4;
    L_0x00c0:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x00c4:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.onSharePressed():void");
    }

    private void setScaleToFill() {
        float bitmapWidth = (float) this.centerImage.getBitmapWidth();
        float containerViewWidth = (float) getContainerViewWidth();
        float bitmapHeight = (float) this.centerImage.getBitmapHeight();
        float containerViewHeight = (float) getContainerViewHeight();
        float min = Math.min(containerViewHeight / bitmapHeight, containerViewWidth / bitmapWidth);
        this.scale = Math.max(containerViewWidth / ((float) ((int) (bitmapWidth * min))), containerViewHeight / ((float) ((int) (bitmapHeight * min))));
        updateMinMax(this.scale);
    }

    public void setParentAlert(ChatAttachAlert chatAttachAlert) {
        this.parentAlert = chatAttachAlert;
    }

    public void setParentActivity(Activity activity) {
        Activity activity2 = activity;
        this.currentAccount = UserConfig.selectedAccount;
        this.centerImage.setCurrentAccount(this.currentAccount);
        this.leftImage.setCurrentAccount(this.currentAccount);
        this.rightImage.setCurrentAccount(this.currentAccount);
        if (this.parentActivity != activity2 && activity2 != null) {
            int i;
            this.parentActivity = activity2;
            this.actvityContext = new ContextThemeWrapper(this.parentActivity, NUM);
            if (progressDrawables == null) {
                progressDrawables = new Drawable[4];
                progressDrawables[0] = this.parentActivity.getResources().getDrawable(NUM);
                progressDrawables[1] = this.parentActivity.getResources().getDrawable(NUM);
                progressDrawables[2] = this.parentActivity.getResources().getDrawable(NUM);
                progressDrawables[3] = this.parentActivity.getResources().getDrawable(NUM);
            }
            this.scroller = new Scroller(activity2);
            this.windowView = new FrameLayout(activity2) {
                private Runnable attachRunnable;

                public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
                    return PhotoViewer.this.isVisible && super.onInterceptTouchEvent(motionEvent);
                }

                public boolean onTouchEvent(MotionEvent motionEvent) {
                    return PhotoViewer.this.isVisible && PhotoViewer.this.onTouchEvent(motionEvent);
                }

                /* Access modifiers changed, original: protected */
                public boolean drawChild(Canvas canvas, View view, long j) {
                    boolean drawChild;
                    try {
                        drawChild = super.drawChild(canvas, view, j);
                    } catch (Throwable unused) {
                        drawChild = false;
                    }
                    if (VERSION.SDK_INT >= 21 && view == PhotoViewer.this.animatingImageView && PhotoViewer.this.lastInsets != null) {
                        Canvas canvas2 = canvas;
                        canvas2.drawRect(0.0f, (float) getMeasuredHeight(), (float) getMeasuredWidth(), (float) (getMeasuredHeight() + ((WindowInsets) PhotoViewer.this.lastInsets).getSystemWindowInsetBottom()), PhotoViewer.this.blackPaint);
                    }
                    return drawChild;
                }

                /* Access modifiers changed, original: protected */
                public void onMeasure(int i, int i2) {
                    i = MeasureSpec.getSize(i);
                    i2 = MeasureSpec.getSize(i2);
                    if (VERSION.SDK_INT < 21 || PhotoViewer.this.lastInsets == null) {
                        int i3 = AndroidUtilities.displaySize.y;
                        if (i2 > i3) {
                            i2 = i3;
                        }
                    } else {
                        WindowInsets windowInsets = (WindowInsets) PhotoViewer.this.lastInsets;
                        if (AndroidUtilities.incorrectDisplaySizeFix) {
                            int i4 = AndroidUtilities.displaySize.y;
                            if (i2 > i4) {
                                i2 = i4;
                            }
                            i2 += AndroidUtilities.statusBarHeight;
                        }
                        i2 -= windowInsets.getSystemWindowInsetBottom();
                    }
                    setMeasuredDimension(i, i2);
                    ViewGroup.LayoutParams layoutParams = PhotoViewer.this.animatingImageView.getLayoutParams();
                    PhotoViewer.this.animatingImageView.measure(MeasureSpec.makeMeasureSpec(layoutParams.width, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(layoutParams.height, Integer.MIN_VALUE));
                    PhotoViewer.this.containerView.measure(MeasureSpec.makeMeasureSpec(i, NUM), MeasureSpec.makeMeasureSpec(i2, NUM));
                }

                /* Access modifiers changed, original: protected */
                public void onLayout(boolean z, int i, int i2, int i3, int i4) {
                    if (VERSION.SDK_INT >= 21) {
                        PhotoViewer.this.lastInsets;
                    }
                    PhotoViewer.this.animatingImageView.layout(0, 0, PhotoViewer.this.animatingImageView.getMeasuredWidth() + 0, PhotoViewer.this.animatingImageView.getMeasuredHeight());
                    PhotoViewer.this.containerView.layout(0, 0, PhotoViewer.this.containerView.getMeasuredWidth() + 0, PhotoViewer.this.containerView.getMeasuredHeight());
                    PhotoViewer.this.wasLayout = true;
                    if (z) {
                        if (!PhotoViewer.this.dontResetZoomOnFirstLayout) {
                            PhotoViewer.this.scale = 1.0f;
                            PhotoViewer.this.translationX = 0.0f;
                            PhotoViewer.this.translationY = 0.0f;
                            PhotoViewer photoViewer = PhotoViewer.this;
                            photoViewer.updateMinMax(photoViewer.scale);
                        }
                        if (PhotoViewer.this.checkImageView != null) {
                            PhotoViewer.this.checkImageView.post(new -$$Lambda$PhotoViewer$5$gQgnGW1vt8iiuLxByIijlXWDJsU(this));
                        }
                    }
                    if (PhotoViewer.this.dontResetZoomOnFirstLayout) {
                        PhotoViewer.this.setScaleToFill();
                        PhotoViewer.this.dontResetZoomOnFirstLayout = false;
                    }
                }

                public /* synthetic */ void lambda$onLayout$0$PhotoViewer$5() {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) PhotoViewer.this.checkImageView.getLayoutParams();
                    ((WindowManager) ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
                    int i = 0;
                    layoutParams.topMargin = ((ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(40.0f)) / 2) + (VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
                    PhotoViewer.this.checkImageView.setLayoutParams(layoutParams);
                    layoutParams = (FrameLayout.LayoutParams) PhotoViewer.this.photosCounterView.getLayoutParams();
                    int currentActionBarHeight = (ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(40.0f)) / 2;
                    if (VERSION.SDK_INT >= 21) {
                        i = AndroidUtilities.statusBarHeight;
                    }
                    layoutParams.topMargin = currentActionBarHeight + i;
                    PhotoViewer.this.photosCounterView.setLayoutParams(layoutParams);
                }

                /* Access modifiers changed, original: protected */
                public void onAttachedToWindow() {
                    super.onAttachedToWindow();
                    PhotoViewer.this.attachedToWindow = true;
                }

                /* Access modifiers changed, original: protected */
                public void onDetachedFromWindow() {
                    super.onDetachedFromWindow();
                    PhotoViewer.this.attachedToWindow = false;
                    PhotoViewer.this.wasLayout = false;
                }

                public boolean dispatchKeyEventPreIme(KeyEvent keyEvent) {
                    if (keyEvent == null || keyEvent.getKeyCode() != 4 || keyEvent.getAction() != 1) {
                        return super.dispatchKeyEventPreIme(keyEvent);
                    }
                    if (PhotoViewer.this.captionEditText.isPopupShowing() || PhotoViewer.this.captionEditText.isKeyboardVisible()) {
                        PhotoViewer.this.closeCaptionEnter(false);
                        return false;
                    }
                    PhotoViewer.getInstance().closePhoto(true, false);
                    return true;
                }
            };
            this.windowView.setBackgroundDrawable(this.backgroundDrawable);
            this.windowView.setClipChildren(true);
            this.windowView.setFocusable(false);
            this.animatingImageView = new ClippingImageView(activity2);
            this.animatingImageView.setAnimationValues(this.animationValues);
            this.windowView.addView(this.animatingImageView, LayoutHelper.createFrame(40, 40.0f));
            this.containerView = new FrameLayoutDrawer(activity2);
            this.containerView.setFocusable(false);
            this.windowView.addView(this.containerView, LayoutHelper.createFrame(-1, -1, 51));
            if (VERSION.SDK_INT >= 21) {
                this.containerView.setFitsSystemWindows(true);
                this.containerView.setOnApplyWindowInsetsListener(new -$$Lambda$PhotoViewer$dljDNjeNI7WCE_iobU6HEh1zPNQ(this));
                this.containerView.setSystemUiVisibility(1792);
            }
            this.windowLayoutParams = new LayoutParams();
            LayoutParams layoutParams = this.windowLayoutParams;
            layoutParams.height = -1;
            layoutParams.format = -3;
            layoutParams.width = -1;
            layoutParams.gravity = 51;
            layoutParams.type = 99;
            if (VERSION.SDK_INT >= 28) {
                layoutParams.layoutInDisplayCutoutMode = 1;
            }
            if (VERSION.SDK_INT >= 21) {
                this.windowLayoutParams.flags = -NUM;
            } else {
                this.windowLayoutParams.flags = 131072;
            }
            this.actionBar = new ActionBar(activity2) {
                public void setAlpha(float f) {
                    super.setAlpha(f);
                    PhotoViewer.this.containerView.invalidate();
                }
            };
            this.actionBar.setTitleColor(-1);
            this.actionBar.setSubtitleColor(-1);
            this.actionBar.setBackgroundColor(NUM);
            this.actionBar.setOccupyStatusBar(VERSION.SDK_INT >= 21);
            this.actionBar.setItemsBackgroundColor(NUM, false);
            this.actionBar.setBackButtonImage(NUM);
            this.actionBar.setTitle(LocaleController.formatString("Of", NUM, Integer.valueOf(1), Integer.valueOf(1)));
            this.containerView.addView(this.actionBar, LayoutHelper.createFrame(-1, -2.0f));
            this.actionBar.setActionBarMenuOnItemClick(new ActionBarMenuOnItemClick() {
                public void onItemClick(int i) {
                    int i2 = i;
                    int i3 = 1;
                    Bundle bundle;
                    if (i2 == -1) {
                        if (PhotoViewer.this.needCaptionLayout && (PhotoViewer.this.captionEditText.isPopupShowing() || PhotoViewer.this.captionEditText.isKeyboardVisible())) {
                            PhotoViewer.this.closeCaptionEnter(false);
                            return;
                        }
                        PhotoViewer.this.closePhoto(true, false);
                    } else if (i2 == 1) {
                        File pathToAttach;
                        if (VERSION.SDK_INT >= 23) {
                            if (PhotoViewer.this.parentActivity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                                PhotoViewer.this.parentActivity.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 4);
                                return;
                            }
                        }
                        if (PhotoViewer.this.currentMessageObject != null) {
                            if ((PhotoViewer.this.currentMessageObject.messageOwner.media instanceof TL_messageMediaWebPage) && PhotoViewer.this.currentMessageObject.messageOwner.media.webpage != null && PhotoViewer.this.currentMessageObject.messageOwner.media.webpage.document == null) {
                                PhotoViewer photoViewer = PhotoViewer.this;
                                pathToAttach = FileLoader.getPathToAttach(photoViewer.getFileLocation(photoViewer.currentIndex, null), true);
                            } else {
                                pathToAttach = FileLoader.getPathToMessage(PhotoViewer.this.currentMessageObject.messageOwner);
                            }
                        } else if (PhotoViewer.this.currentFileLocation != null) {
                            TL_fileLocationToBeDeprecated tL_fileLocationToBeDeprecated = PhotoViewer.this.currentFileLocation.location;
                            boolean z = PhotoViewer.this.avatarsDialogId != 0 || PhotoViewer.this.isEvent;
                            pathToAttach = FileLoader.getPathToAttach(tL_fileLocationToBeDeprecated, z);
                        } else {
                            pathToAttach = null;
                        }
                        if (pathToAttach == null || !pathToAttach.exists()) {
                            PhotoViewer.this.showDownloadAlert();
                        } else {
                            String file = pathToAttach.toString();
                            Activity access$2800 = PhotoViewer.this.parentActivity;
                            if (PhotoViewer.this.currentMessageObject == null || !PhotoViewer.this.currentMessageObject.isVideo()) {
                                i3 = 0;
                            }
                            MediaController.saveFile(file, access$2800, i3, null, null);
                        }
                    } else if (i2 == 2) {
                        if (PhotoViewer.this.currentDialogId != 0) {
                            PhotoViewer.this.disableShowCheck = true;
                            bundle = new Bundle();
                            bundle.putLong("dialog_id", PhotoViewer.this.currentDialogId);
                            MediaActivity mediaActivity = new MediaActivity(bundle, new int[]{-1, -1, -1, -1, -1}, null, PhotoViewer.this.sharedMediaType);
                            if (PhotoViewer.this.parentChatActivity != null) {
                                mediaActivity.setChatInfo(PhotoViewer.this.parentChatActivity.getCurrentChatInfo());
                            }
                            PhotoViewer.this.closePhoto(false, false);
                            ((LaunchActivity) PhotoViewer.this.parentActivity).presentFragment(mediaActivity, false, true);
                        }
                    } else if (i2 == 4) {
                        if (PhotoViewer.this.currentMessageObject != null) {
                            bundle = new Bundle();
                            int access$7800 = (int) PhotoViewer.this.currentDialogId;
                            int access$78002 = (int) (PhotoViewer.this.currentDialogId >> 32);
                            if (access$7800 == 0) {
                                bundle.putInt("enc_id", access$78002);
                            } else if (access$7800 > 0) {
                                bundle.putInt("user_id", access$7800);
                            } else if (access$7800 < 0) {
                                Chat chat = MessagesController.getInstance(PhotoViewer.this.currentAccount).getChat(Integer.valueOf(-access$7800));
                                if (!(chat == null || chat.migrated_to == null)) {
                                    bundle.putInt("migrated_to", access$7800);
                                    access$7800 = -chat.migrated_to.channel_id;
                                }
                                bundle.putInt("chat_id", -access$7800);
                            }
                            bundle.putInt("message_id", PhotoViewer.this.currentMessageObject.getId());
                            NotificationCenter.getInstance(PhotoViewer.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                            LaunchActivity launchActivity = (LaunchActivity) PhotoViewer.this.parentActivity;
                            boolean z2 = launchActivity.getMainFragmentsCount() > 1 || AndroidUtilities.isTablet();
                            launchActivity.presentFragment(new ChatActivity(bundle), z2, true);
                            PhotoViewer.this.currentMessageObject = null;
                            PhotoViewer.this.closePhoto(false, false);
                        }
                    } else if (i2 == 3) {
                        if (PhotoViewer.this.currentMessageObject != null && PhotoViewer.this.parentActivity != null) {
                            ((LaunchActivity) PhotoViewer.this.parentActivity).switchToAccount(PhotoViewer.this.currentMessageObject.currentAccount, true);
                            bundle = new Bundle();
                            bundle.putBoolean("onlySelect", true);
                            bundle.putInt("dialogsType", 3);
                            DialogsActivity dialogsActivity = new DialogsActivity(bundle);
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(PhotoViewer.this.currentMessageObject);
                            dialogsActivity.setDelegate(new -$$Lambda$PhotoViewer$7$MBP2lcO9C4pgMJ4ntN8Td94R7bI(this, arrayList));
                            ((LaunchActivity) PhotoViewer.this.parentActivity).presentFragment(dialogsActivity, false, true);
                            PhotoViewer.this.closePhoto(false, false);
                        }
                    } else if (i2 == 6) {
                        if (PhotoViewer.this.parentActivity != null && PhotoViewer.this.placeProvider != null) {
                            Builder builder = new Builder(PhotoViewer.this.parentActivity);
                            String deleteMessageString = PhotoViewer.this.placeProvider.getDeleteMessageString();
                            String str = "AreYouSureDeletePhotoTitle";
                            if (deleteMessageString != null) {
                                builder.setTitle(LocaleController.getString(str, NUM));
                                builder.setMessage(deleteMessageString);
                            } else if (PhotoViewer.this.currentMessageObject != null && PhotoViewer.this.currentMessageObject.isVideo()) {
                                builder.setTitle(LocaleController.getString("AreYouSureDeleteVideoTitle", NUM));
                                builder.setMessage(LocaleController.formatString("AreYouSureDeleteVideo", NUM, new Object[0]));
                            } else if (PhotoViewer.this.currentMessageObject == null || !PhotoViewer.this.currentMessageObject.isGif()) {
                                builder.setTitle(LocaleController.getString(str, NUM));
                                builder.setMessage(LocaleController.formatString("AreYouSureDeletePhoto", NUM, new Object[0]));
                            } else {
                                builder.setTitle(LocaleController.getString("AreYouSureDeleteGIFTitle", NUM));
                                builder.setMessage(LocaleController.formatString("AreYouSureDeleteGIF", NUM, new Object[0]));
                            }
                            boolean[] zArr = new boolean[1];
                            if (!(PhotoViewer.this.currentMessageObject == null || PhotoViewer.this.currentMessageObject.scheduled)) {
                                int dialogId = (int) PhotoViewer.this.currentMessageObject.getDialogId();
                                if (dialogId != 0) {
                                    User user;
                                    Chat chat2;
                                    if (dialogId > 0) {
                                        user = MessagesController.getInstance(PhotoViewer.this.currentAccount).getUser(Integer.valueOf(dialogId));
                                        chat2 = null;
                                    } else {
                                        chat2 = MessagesController.getInstance(PhotoViewer.this.currentAccount).getChat(Integer.valueOf(-dialogId));
                                        user = null;
                                    }
                                    if (!(user == null && ChatObject.isChannel(chat2))) {
                                        int currentTime = ConnectionsManager.getInstance(PhotoViewer.this.currentAccount).getCurrentTime();
                                        int i4;
                                        if (user != null) {
                                            i4 = MessagesController.getInstance(PhotoViewer.this.currentAccount).revokeTimePmLimit;
                                        } else {
                                            i4 = MessagesController.getInstance(PhotoViewer.this.currentAccount).revokeTimeLimit;
                                        }
                                        if (!((user == null || user.id == UserConfig.getInstance(PhotoViewer.this.currentAccount).getClientUserId()) && chat2 == null) && ((PhotoViewer.this.currentMessageObject.messageOwner.action == null || (PhotoViewer.this.currentMessageObject.messageOwner.action instanceof TL_messageActionEmpty)) && PhotoViewer.this.currentMessageObject.isOut() && currentTime - PhotoViewer.this.currentMessageObject.messageOwner.date <= i4)) {
                                            FrameLayout frameLayout = new FrameLayout(PhotoViewer.this.parentActivity);
                                            CheckBoxCell checkBoxCell = new CheckBoxCell(PhotoViewer.this.parentActivity, 1);
                                            checkBoxCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
                                            String str2 = "";
                                            if (chat2 != null) {
                                                checkBoxCell.setText(LocaleController.getString("DeleteForAll", NUM), str2, false, false);
                                            } else {
                                                checkBoxCell.setText(LocaleController.formatString("DeleteForUser", NUM, UserObject.getFirstName(user)), str2, false, false);
                                            }
                                            checkBoxCell.setPadding(LocaleController.isRTL ? AndroidUtilities.dp(16.0f) : AndroidUtilities.dp(8.0f), 0, LocaleController.isRTL ? AndroidUtilities.dp(8.0f) : AndroidUtilities.dp(16.0f), 0);
                                            frameLayout.addView(checkBoxCell, LayoutHelper.createFrame(-1, 48.0f, 51, 0.0f, 0.0f, 0.0f, 0.0f));
                                            checkBoxCell.setOnClickListener(new -$$Lambda$PhotoViewer$7$PnoO3dhBjKU061H6V2Ybr5NYtAo(zArr));
                                            builder.setView(frameLayout);
                                        }
                                    }
                                }
                            }
                            builder.setPositiveButton(LocaleController.getString("Delete", NUM), new -$$Lambda$PhotoViewer$7$It5atnO7wprrf4dhdkFyC0mozx4(this, zArr));
                            builder.setNegativeButton(LocaleController.getString("Cancel", NUM), null);
                            AlertDialog create = builder.create();
                            PhotoViewer.this.showAlertDialog(builder);
                            TextView textView = (TextView) create.getButton(-1);
                            if (textView != null) {
                                textView.setTextColor(Theme.getColor("dialogTextRed2"));
                            }
                        }
                    } else if (i2 == 10) {
                        PhotoViewer.this.onSharePressed();
                    } else if (i2 == 11) {
                        try {
                            AndroidUtilities.openForView(PhotoViewer.this.currentMessageObject, PhotoViewer.this.parentActivity);
                            PhotoViewer.this.closePhoto(false, false);
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    } else if (i2 == 13) {
                        if (PhotoViewer.this.parentActivity != null && PhotoViewer.this.currentMessageObject != null && PhotoViewer.this.currentMessageObject.messageOwner.media != null && PhotoViewer.this.currentMessageObject.messageOwner.media.photo != null) {
                            new StickersAlert(PhotoViewer.this.parentActivity, PhotoViewer.this.currentMessageObject, PhotoViewer.this.currentMessageObject.messageOwner.media.photo).show();
                        }
                    } else if (i2 == 5) {
                        if (PhotoViewer.this.pipItem.getAlpha() == 1.0f) {
                            PhotoViewer.this.switchToPip();
                        }
                    } else if (i2 == 7) {
                        if (PhotoViewer.this.currentMessageObject != null) {
                            FileLoader.getInstance(PhotoViewer.this.currentAccount).cancelLoadFile(PhotoViewer.this.currentMessageObject.getDocument());
                            PhotoViewer.this.releasePlayer(false);
                            PhotoViewer.this.bottomLayout.setTag(Integer.valueOf(1));
                            PhotoViewer.this.bottomLayout.setVisibility(0);
                        }
                    } else if (i2 == 14 && PhotoViewer.this.currentMessageObject != null) {
                        MessagesController.getInstance(PhotoViewer.this.currentAccount).saveGif(PhotoViewer.this.currentMessageObject, PhotoViewer.this.currentMessageObject.getDocument());
                        if (PhotoViewer.this.parentChatActivity != null) {
                            PhotoViewer.this.parentChatActivity.getChatActivityEnterView();
                        }
                    }
                }

                public /* synthetic */ void lambda$onItemClick$0$PhotoViewer$7(ArrayList arrayList, DialogsActivity dialogsActivity, ArrayList arrayList2, CharSequence charSequence, boolean z) {
                    ArrayList arrayList3 = arrayList2;
                    int i = 0;
                    if (arrayList2.size() > 1 || ((Long) arrayList3.get(0)).longValue() == ((long) UserConfig.getInstance(PhotoViewer.this.currentAccount).getClientUserId()) || charSequence != null) {
                        ArrayList arrayList4 = arrayList;
                        while (i < arrayList2.size()) {
                            long longValue = ((Long) arrayList3.get(i)).longValue();
                            if (charSequence != null) {
                                SendMessagesHelper.getInstance(PhotoViewer.this.currentAccount).sendMessage(charSequence.toString(), longValue, null, null, true, null, null, null, true, 0);
                            }
                            SendMessagesHelper.getInstance(PhotoViewer.this.currentAccount).sendMessage(arrayList, longValue, true, 0);
                            i++;
                        }
                        dialogsActivity.finishFragment();
                        return;
                    }
                    long longValue2 = ((Long) arrayList3.get(0)).longValue();
                    int i2 = (int) longValue2;
                    int i3 = (int) (longValue2 >> 32);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("scrollToTopOnResume", true);
                    if (i2 == 0) {
                        bundle.putInt("enc_id", i3);
                    } else if (i2 > 0) {
                        bundle.putInt("user_id", i2);
                    } else if (i2 < 0) {
                        bundle.putInt("chat_id", -i2);
                    }
                    NotificationCenter.getInstance(PhotoViewer.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                    ChatActivity chatActivity = new ChatActivity(bundle);
                    if (((LaunchActivity) PhotoViewer.this.parentActivity).presentFragment(chatActivity, true, false)) {
                        chatActivity.showFieldPanelForForward(true, arrayList);
                    } else {
                        dialogsActivity.finishFragment();
                    }
                }

                static /* synthetic */ void lambda$onItemClick$1(boolean[] zArr, View view) {
                    CheckBoxCell checkBoxCell = (CheckBoxCell) view;
                    zArr[0] = zArr[0] ^ 1;
                    checkBoxCell.setChecked(zArr[0], true);
                }

                /* JADX WARNING: Removed duplicated region for block: B:47:0x0198  */
                /* JADX WARNING: Removed duplicated region for block: B:46:0x0184  */
                /* JADX WARNING: Missing block: B:42:0x017d, code skipped:
            if (r2.location.volume_id == org.telegram.ui.PhotoViewer.access$9200(r11.this$0).location.volume_id) goto L_0x017f;
     */
                public /* synthetic */ void lambda$onItemClick$2$PhotoViewer$7(boolean[] r12, android.content.DialogInterface r13, int r14) {
                    /*
                    r11 = this;
                    r13 = org.telegram.ui.PhotoViewer.this;
                    r13 = r13.imagesArr;
                    r13 = r13.isEmpty();
                    r14 = 0;
                    r0 = 0;
                    if (r13 != 0) goto L_0x00cb;
                L_0x000e:
                    r13 = org.telegram.ui.PhotoViewer.this;
                    r13 = r13.currentIndex;
                    if (r13 < 0) goto L_0x00ca;
                L_0x0016:
                    r13 = org.telegram.ui.PhotoViewer.this;
                    r13 = r13.currentIndex;
                    r1 = org.telegram.ui.PhotoViewer.this;
                    r1 = r1.imagesArr;
                    r1 = r1.size();
                    if (r13 < r1) goto L_0x002a;
                L_0x0028:
                    goto L_0x00ca;
                L_0x002a:
                    r13 = org.telegram.ui.PhotoViewer.this;
                    r13 = r13.imagesArr;
                    r1 = org.telegram.ui.PhotoViewer.this;
                    r1 = r1.currentIndex;
                    r13 = r13.get(r1);
                    r13 = (org.telegram.messenger.MessageObject) r13;
                    r1 = r13.isSent();
                    if (r1 == 0) goto L_0x02aa;
                L_0x0042:
                    r1 = org.telegram.ui.PhotoViewer.this;
                    r1.closePhoto(r0, r0);
                    r3 = new java.util.ArrayList;
                    r3.<init>();
                    r1 = org.telegram.ui.PhotoViewer.this;
                    r1 = r1.slideshowMessageId;
                    if (r1 == 0) goto L_0x0062;
                L_0x0054:
                    r1 = org.telegram.ui.PhotoViewer.this;
                    r1 = r1.slideshowMessageId;
                    r1 = java.lang.Integer.valueOf(r1);
                    r3.add(r1);
                    goto L_0x006d;
                L_0x0062:
                    r1 = r13.getId();
                    r1 = java.lang.Integer.valueOf(r1);
                    r3.add(r1);
                L_0x006d:
                    r1 = r13.getDialogId();
                    r2 = (int) r1;
                    if (r2 != 0) goto L_0x00ab;
                L_0x0074:
                    r1 = r13.messageOwner;
                    r1 = r1.random_id;
                    r4 = 0;
                    r6 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1));
                    if (r6 == 0) goto L_0x00ab;
                L_0x007e:
                    r14 = new java.util.ArrayList;
                    r14.<init>();
                    r1 = r13.messageOwner;
                    r1 = r1.random_id;
                    r1 = java.lang.Long.valueOf(r1);
                    r14.add(r1);
                    r1 = org.telegram.ui.PhotoViewer.this;
                    r1 = r1.currentAccount;
                    r1 = org.telegram.messenger.MessagesController.getInstance(r1);
                    r4 = r13.getDialogId();
                    r2 = 32;
                    r4 = r4 >> r2;
                    r2 = (int) r4;
                    r2 = java.lang.Integer.valueOf(r2);
                    r1 = r1.getEncryptedChat(r2);
                    r4 = r14;
                    r5 = r1;
                    goto L_0x00ad;
                L_0x00ab:
                    r4 = r14;
                    r5 = r4;
                L_0x00ad:
                    r14 = org.telegram.ui.PhotoViewer.this;
                    r14 = r14.currentAccount;
                    r2 = org.telegram.messenger.MessagesController.getInstance(r14);
                    r6 = r13.getDialogId();
                    r14 = r13.messageOwner;
                    r14 = r14.to_id;
                    r8 = r14.channel_id;
                    r9 = r12[r0];
                    r10 = r13.scheduled;
                    r2.deleteMessages(r3, r4, r5, r6, r8, r9, r10);
                    goto L_0x02aa;
                L_0x00ca:
                    return;
                L_0x00cb:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.avatarsArr;
                    r12 = r12.isEmpty();
                    r13 = -1;
                    r1 = 1;
                    if (r12 != 0) goto L_0x023e;
                L_0x00d9:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.currentIndex;
                    if (r12 < 0) goto L_0x023d;
                L_0x00e1:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.currentIndex;
                    r2 = org.telegram.ui.PhotoViewer.this;
                    r2 = r2.avatarsArr;
                    r2 = r2.size();
                    if (r12 < r2) goto L_0x00f5;
                L_0x00f3:
                    goto L_0x023d;
                L_0x00f5:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.avatarsArr;
                    r2 = org.telegram.ui.PhotoViewer.this;
                    r2 = r2.currentIndex;
                    r12 = r12.get(r2);
                    r12 = (org.telegram.tgnet.TLRPC.Photo) r12;
                    r2 = org.telegram.ui.PhotoViewer.this;
                    r2 = r2.imagesArrLocations;
                    r3 = org.telegram.ui.PhotoViewer.this;
                    r3 = r3.currentIndex;
                    r2 = r2.get(r3);
                    r2 = (org.telegram.messenger.ImageLocation) r2;
                    r3 = r12 instanceof org.telegram.tgnet.TLRPC.TL_photoEmpty;
                    if (r3 == 0) goto L_0x011e;
                L_0x011d:
                    r12 = r14;
                L_0x011e:
                    r3 = org.telegram.ui.PhotoViewer.this;
                    r3 = r3.currentUserAvatarLocation;
                    if (r3 == 0) goto L_0x0181;
                L_0x0126:
                    if (r12 == 0) goto L_0x015d;
                L_0x0128:
                    r2 = r12.sizes;
                    r2 = r2.iterator();
                L_0x012e:
                    r3 = r2.hasNext();
                    if (r3 == 0) goto L_0x0181;
                L_0x0134:
                    r3 = r2.next();
                    r3 = (org.telegram.tgnet.TLRPC.PhotoSize) r3;
                    r4 = r3.location;
                    r4 = r4.local_id;
                    r5 = org.telegram.ui.PhotoViewer.this;
                    r5 = r5.currentUserAvatarLocation;
                    r5 = r5.location;
                    r5 = r5.local_id;
                    if (r4 != r5) goto L_0x012e;
                L_0x014a:
                    r3 = r3.location;
                    r3 = r3.volume_id;
                    r5 = org.telegram.ui.PhotoViewer.this;
                    r5 = r5.currentUserAvatarLocation;
                    r5 = r5.location;
                    r5 = r5.volume_id;
                    r7 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1));
                    if (r7 != 0) goto L_0x012e;
                L_0x015c:
                    goto L_0x017f;
                L_0x015d:
                    r3 = r2.location;
                    r3 = r3.local_id;
                    r4 = org.telegram.ui.PhotoViewer.this;
                    r4 = r4.currentUserAvatarLocation;
                    r4 = r4.location;
                    r4 = r4.local_id;
                    if (r3 != r4) goto L_0x0181;
                L_0x016d:
                    r2 = r2.location;
                    r2 = r2.volume_id;
                    r4 = org.telegram.ui.PhotoViewer.this;
                    r4 = r4.currentUserAvatarLocation;
                    r4 = r4.location;
                    r4 = r4.volume_id;
                    r6 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
                    if (r6 != 0) goto L_0x0181;
                L_0x017f:
                    r2 = 1;
                    goto L_0x0182;
                L_0x0181:
                    r2 = 0;
                L_0x0182:
                    if (r2 == 0) goto L_0x0198;
                L_0x0184:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.currentAccount;
                    r12 = org.telegram.messenger.MessagesController.getInstance(r12);
                    r12.deleteUserPhoto(r14);
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12.closePhoto(r0, r0);
                    goto L_0x02aa;
                L_0x0198:
                    if (r12 == 0) goto L_0x02aa;
                L_0x019a:
                    r14 = new org.telegram.tgnet.TLRPC$TL_inputPhoto;
                    r14.<init>();
                    r2 = r12.id;
                    r14.id = r2;
                    r2 = r12.access_hash;
                    r14.access_hash = r2;
                    r2 = r12.file_reference;
                    r14.file_reference = r2;
                    r2 = r14.file_reference;
                    if (r2 != 0) goto L_0x01b3;
                L_0x01af:
                    r2 = new byte[r0];
                    r14.file_reference = r2;
                L_0x01b3:
                    r2 = org.telegram.ui.PhotoViewer.this;
                    r2 = r2.currentAccount;
                    r2 = org.telegram.messenger.MessagesController.getInstance(r2);
                    r2.deleteUserPhoto(r14);
                    r14 = org.telegram.ui.PhotoViewer.this;
                    r14 = r14.currentAccount;
                    r14 = org.telegram.messenger.MessagesStorage.getInstance(r14);
                    r2 = org.telegram.ui.PhotoViewer.this;
                    r2 = r2.avatarsDialogId;
                    r3 = r12.id;
                    r14.clearUserPhoto(r2, r3);
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.imagesArrLocations;
                    r14 = org.telegram.ui.PhotoViewer.this;
                    r14 = r14.currentIndex;
                    r12.remove(r14);
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.imagesArrLocationsSizes;
                    r14 = org.telegram.ui.PhotoViewer.this;
                    r14 = r14.currentIndex;
                    r12.remove(r14);
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.avatarsArr;
                    r14 = org.telegram.ui.PhotoViewer.this;
                    r14 = r14.currentIndex;
                    r12.remove(r14);
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.imagesArrLocations;
                    r12 = r12.isEmpty();
                    if (r12 == 0) goto L_0x0215;
                L_0x020e:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12.closePhoto(r0, r0);
                    goto L_0x02aa;
                L_0x0215:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.currentIndex;
                    r14 = org.telegram.ui.PhotoViewer.this;
                    r14 = r14.avatarsArr;
                    r14 = r14.size();
                    if (r12 < r14) goto L_0x0232;
                L_0x0227:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.avatarsArr;
                    r12 = r12.size();
                    r12 = r12 - r1;
                L_0x0232:
                    r14 = org.telegram.ui.PhotoViewer.this;
                    r14.currentIndex = r13;
                    r13 = org.telegram.ui.PhotoViewer.this;
                    r13.setImageIndex(r12, r1);
                    goto L_0x02aa;
                L_0x023d:
                    return;
                L_0x023e:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.secureDocuments;
                    r12 = r12.isEmpty();
                    if (r12 != 0) goto L_0x02aa;
                L_0x024a:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.placeProvider;
                    if (r12 != 0) goto L_0x0253;
                L_0x0252:
                    return;
                L_0x0253:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.secureDocuments;
                    r14 = org.telegram.ui.PhotoViewer.this;
                    r14 = r14.currentIndex;
                    r12.remove(r14);
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.placeProvider;
                    r14 = org.telegram.ui.PhotoViewer.this;
                    r14 = r14.currentIndex;
                    r12.deleteImageAtIndex(r14);
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.secureDocuments;
                    r12 = r12.isEmpty();
                    if (r12 == 0) goto L_0x0283;
                L_0x027d:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12.closePhoto(r0, r0);
                    goto L_0x02aa;
                L_0x0283:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.currentIndex;
                    r14 = org.telegram.ui.PhotoViewer.this;
                    r14 = r14.secureDocuments;
                    r14 = r14.size();
                    if (r12 < r14) goto L_0x02a0;
                L_0x0295:
                    r12 = org.telegram.ui.PhotoViewer.this;
                    r12 = r12.secureDocuments;
                    r12 = r12.size();
                    r12 = r12 - r1;
                L_0x02a0:
                    r14 = org.telegram.ui.PhotoViewer.this;
                    r14.currentIndex = r13;
                    r13 = org.telegram.ui.PhotoViewer.this;
                    r13.setImageIndex(r12, r1);
                L_0x02aa:
                    return;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer$AnonymousClass7.lambda$onItemClick$2$PhotoViewer$7(boolean[], android.content.DialogInterface, int):void");
                }

                public boolean canOpenMenu() {
                    if (PhotoViewer.this.currentMessageObject != null) {
                        return FileLoader.getPathToMessage(PhotoViewer.this.currentMessageObject.messageOwner).exists();
                    }
                    boolean z = false;
                    if (PhotoViewer.this.currentFileLocation == null) {
                        return false;
                    }
                    PhotoViewer photoViewer = PhotoViewer.this;
                    FileLocation access$8700 = photoViewer.getFileLocation(photoViewer.currentFileLocation);
                    if (PhotoViewer.this.avatarsDialogId != 0 || PhotoViewer.this.isEvent) {
                        z = true;
                    }
                    return FileLoader.getPathToAttach(access$8700, z).exists();
                }
            });
            ActionBarMenu createMenu = this.actionBar.createMenu();
            this.masksItem = createMenu.addItem(13, NUM);
            this.pipItem = createMenu.addItem(5, NUM);
            this.sendItem = createMenu.addItem(3, NUM);
            this.menuItem = createMenu.addItem(0, NUM);
            this.menuItem.addSubItem(11, NUM, LocaleController.getString("OpenInExternalApp", NUM)).setColors(-328966, -328966);
            this.menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", NUM));
            this.allMediaItem = this.menuItem.addSubItem(2, NUM, LocaleController.getString("ShowAllMedia", NUM));
            this.allMediaItem.setColors(-328966, -328966);
            this.menuItem.addSubItem(14, NUM, LocaleController.getString("SaveToGIFs", NUM)).setColors(-328966, -328966);
            this.menuItem.addSubItem(4, NUM, LocaleController.getString("ShowInChat", NUM)).setColors(-328966, -328966);
            this.menuItem.addSubItem(10, NUM, LocaleController.getString("ShareFile", NUM)).setColors(-328966, -328966);
            this.menuItem.addSubItem(1, NUM, LocaleController.getString("SaveToGallery", NUM)).setColors(-328966, -328966);
            this.menuItem.addSubItem(6, NUM, LocaleController.getString("Delete", NUM)).setColors(-328966, -328966);
            this.menuItem.addSubItem(7, NUM, LocaleController.getString("StopDownload", NUM)).setColors(-328966, -328966);
            this.menuItem.redrawPopup(-NUM);
            this.sendItem.setContentDescription(LocaleController.getString("Forward", NUM));
            this.bottomLayout = new FrameLayout(this.actvityContext);
            this.bottomLayout.setBackgroundColor(NUM);
            this.containerView.addView(this.bottomLayout, LayoutHelper.createFrame(-1, 48, 83));
            this.groupedPhotosListView = new GroupedPhotosListView(this.actvityContext);
            this.containerView.addView(this.groupedPhotosListView, LayoutHelper.createFrame(-1, 62.0f, 83, 0.0f, 0.0f, 0.0f, 48.0f));
            this.groupedPhotosListView.setDelegate(new GroupedPhotosListViewDelegate() {
                public ArrayList<PageBlock> getPageBlockArr() {
                    return null;
                }

                public Object getParentObject() {
                    return null;
                }

                public int getCurrentIndex() {
                    return PhotoViewer.this.currentIndex;
                }

                public int getCurrentAccount() {
                    return PhotoViewer.this.currentAccount;
                }

                public int getAvatarsDialogId() {
                    return PhotoViewer.this.avatarsDialogId;
                }

                public int getSlideshowMessageId() {
                    return PhotoViewer.this.slideshowMessageId;
                }

                public ArrayList<ImageLocation> getImagesArrLocations() {
                    return PhotoViewer.this.imagesArrLocations;
                }

                public ArrayList<MessageObject> getImagesArr() {
                    return PhotoViewer.this.imagesArr;
                }

                public void setCurrentIndex(int i) {
                    PhotoViewer.this.currentIndex = -1;
                    if (PhotoViewer.this.currentThumb != null) {
                        PhotoViewer.this.currentThumb.release();
                        PhotoViewer.this.currentThumb = null;
                    }
                    PhotoViewer.this.setImageIndex(i, true);
                }
            });
            this.captionTextView = createCaptionTextView();
            this.switchCaptionTextView = createCaptionTextView();
            for (i = 0; i < 3; i++) {
                this.photoProgressViews[i] = new PhotoProgressView(this.containerView.getContext(), this.containerView);
                this.photoProgressViews[i].setBackgroundState(0, false);
            }
            this.miniProgressView = new RadialProgressView(this.actvityContext) {
                public void setAlpha(float f) {
                    super.setAlpha(f);
                    if (PhotoViewer.this.containerView != null) {
                        PhotoViewer.this.containerView.invalidate();
                    }
                }

                public void invalidate() {
                    super.invalidate();
                    if (PhotoViewer.this.containerView != null) {
                        PhotoViewer.this.containerView.invalidate();
                    }
                }
            };
            this.miniProgressView.setUseSelfAlpha(true);
            this.miniProgressView.setProgressColor(-1);
            this.miniProgressView.setSize(AndroidUtilities.dp(54.0f));
            this.miniProgressView.setBackgroundResource(NUM);
            this.miniProgressView.setVisibility(4);
            this.miniProgressView.setAlpha(0.0f);
            this.containerView.addView(this.miniProgressView, LayoutHelper.createFrame(64, 64, 17));
            this.shareButton = new ImageView(this.containerView.getContext());
            this.shareButton.setImageResource(NUM);
            this.shareButton.setScaleType(ScaleType.CENTER);
            this.shareButton.setBackgroundDrawable(Theme.createSelectorDrawable(NUM));
            this.bottomLayout.addView(this.shareButton, LayoutHelper.createFrame(50, -1, 53));
            this.shareButton.setOnClickListener(new -$$Lambda$PhotoViewer$jnWuEkM1mcOIlgJQ2IKC_gla8zE(this));
            this.shareButton.setContentDescription(LocaleController.getString("ShareFile", NUM));
            this.nameTextView = new TextView(this.containerView.getContext());
            this.nameTextView.setTextSize(1, 14.0f);
            this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            this.nameTextView.setSingleLine(true);
            this.nameTextView.setMaxLines(1);
            this.nameTextView.setEllipsize(TruncateAt.END);
            this.nameTextView.setTextColor(-1);
            this.nameTextView.setGravity(3);
            this.bottomLayout.addView(this.nameTextView, LayoutHelper.createFrame(-1, -2.0f, 51, 16.0f, 5.0f, 60.0f, 0.0f));
            this.dateTextView = new TextView(this.containerView.getContext());
            this.dateTextView.setTextSize(1, 13.0f);
            this.dateTextView.setSingleLine(true);
            this.dateTextView.setMaxLines(1);
            this.dateTextView.setEllipsize(TruncateAt.END);
            this.dateTextView.setTextColor(-1);
            this.dateTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            this.dateTextView.setGravity(3);
            this.bottomLayout.addView(this.dateTextView, LayoutHelper.createFrame(-1, -2.0f, 51, 16.0f, 25.0f, 50.0f, 0.0f));
            createVideoControlsInterface();
            this.progressView = new RadialProgressView(this.parentActivity);
            this.progressView.setProgressColor(-1);
            this.progressView.setBackgroundResource(NUM);
            this.progressView.setVisibility(4);
            this.containerView.addView(this.progressView, LayoutHelper.createFrame(54, 54, 17));
            this.qualityPicker = new PickerBottomLayoutViewer(this.parentActivity);
            this.qualityPicker.setBackgroundColor(NUM);
            this.qualityPicker.updateSelectedCount(0, false);
            this.qualityPicker.setTranslationY((float) AndroidUtilities.dp(120.0f));
            this.qualityPicker.doneButton.setText(LocaleController.getString("Done", NUM).toUpperCase());
            this.containerView.addView(this.qualityPicker, LayoutHelper.createFrame(-1, 48, 83));
            this.qualityPicker.cancelButton.setOnClickListener(new -$$Lambda$PhotoViewer$Ix7Bv26ggFYqzRhGVPWK6_2sNXM(this));
            this.qualityPicker.doneButton.setOnClickListener(new -$$Lambda$PhotoViewer$39sOKx3t1IYK0_BSSSDOFzpR-Xk(this));
            this.videoForwardDrawable = new VideoForwardDrawable();
            this.videoForwardDrawable.setDelegate(new VideoForwardDrawableDelegate() {
                public void onAnimationEnd() {
                }

                public void invalidate() {
                    PhotoViewer.this.containerView.invalidate();
                }
            });
            this.qualityChooseView = new QualityChooseView(this.parentActivity);
            this.qualityChooseView.setTranslationY((float) AndroidUtilities.dp(120.0f));
            this.qualityChooseView.setVisibility(4);
            this.qualityChooseView.setBackgroundColor(NUM);
            this.containerView.addView(this.qualityChooseView, LayoutHelper.createFrame(-1, 70.0f, 83, 0.0f, 0.0f, 0.0f, 48.0f));
            this.pickerView = new FrameLayout(this.actvityContext) {
                public boolean dispatchTouchEvent(MotionEvent motionEvent) {
                    return PhotoViewer.this.bottomTouchEnabled && super.dispatchTouchEvent(motionEvent);
                }

                public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
                    return PhotoViewer.this.bottomTouchEnabled && super.onInterceptTouchEvent(motionEvent);
                }

                public boolean onTouchEvent(MotionEvent motionEvent) {
                    return PhotoViewer.this.bottomTouchEnabled && super.onTouchEvent(motionEvent);
                }
            };
            this.pickerView.setBackgroundColor(NUM);
            this.containerView.addView(this.pickerView, LayoutHelper.createFrame(-1, -2, 83));
            this.videoTimelineView = new VideoTimelinePlayView(this.parentActivity);
            this.videoTimelineView.setDelegate(new VideoTimelineViewDelegate() {
                public void didStartDragging() {
                }

                public void didStopDragging() {
                }

                public void onLeftProgressChanged(float f) {
                    if (PhotoViewer.this.videoPlayer != null) {
                        if (PhotoViewer.this.videoPlayer.isPlaying()) {
                            PhotoViewer.this.videoPlayer.pause();
                            PhotoViewer.this.containerView.invalidate();
                        }
                        PhotoViewer.this.videoPlayer.seekTo((long) ((int) (PhotoViewer.this.videoDuration * f)));
                        PhotoViewer.this.videoPlayerSeekbar.setProgress(0.0f);
                        PhotoViewer.this.videoTimelineView.setProgress(0.0f);
                        PhotoViewer.this.updateVideoInfo();
                    }
                }

                public void onRightProgressChanged(float f) {
                    if (PhotoViewer.this.videoPlayer != null) {
                        if (PhotoViewer.this.videoPlayer.isPlaying()) {
                            PhotoViewer.this.videoPlayer.pause();
                            PhotoViewer.this.containerView.invalidate();
                        }
                        PhotoViewer.this.videoPlayer.seekTo((long) ((int) (PhotoViewer.this.videoDuration * f)));
                        PhotoViewer.this.videoPlayerSeekbar.setProgress(0.0f);
                        PhotoViewer.this.videoTimelineView.setProgress(0.0f);
                        PhotoViewer.this.updateVideoInfo();
                    }
                }

                public void onPlayProgressChanged(float f) {
                    if (PhotoViewer.this.videoPlayer != null) {
                        PhotoViewer.this.videoPlayer.seekTo((long) ((int) (PhotoViewer.this.videoDuration * f)));
                    }
                }
            });
            this.pickerView.addView(this.videoTimelineView, LayoutHelper.createFrame(-1, 58.0f, 51, 0.0f, 8.0f, 0.0f, 88.0f));
            this.pickerViewSendButton = new ImageView(this.parentActivity);
            this.pickerViewSendButton.setScaleType(ScaleType.CENTER);
            this.pickerViewSendButton.setBackgroundDrawable(Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56.0f), -10043398, -10043398));
            this.pickerViewSendButton.setColorFilter(new PorterDuffColorFilter(-1, Mode.MULTIPLY));
            this.pickerViewSendButton.setImageResource(NUM);
            this.containerView.addView(this.pickerViewSendButton, LayoutHelper.createFrame(56, 56.0f, 85, 0.0f, 0.0f, 14.0f, 14.0f));
            this.pickerViewSendButton.setContentDescription(LocaleController.getString("Send", NUM));
            this.pickerViewSendButton.setOnClickListener(new -$$Lambda$PhotoViewer$Ygm2nkEIz52xt8vsvpsgSvg6BwY(this));
            this.pickerViewSendButton.setOnLongClickListener(new -$$Lambda$PhotoViewer$LuqbQKYg6TRVlCqtgDJnfBlmnYo(this));
            LinearLayout linearLayout = new LinearLayout(this.parentActivity);
            linearLayout.setOrientation(0);
            this.pickerView.addView(linearLayout, LayoutHelper.createFrame(-2, 48.0f, 81, 0.0f, 0.0f, 34.0f, 0.0f));
            this.cropItem = new ImageView(this.parentActivity);
            this.cropItem.setScaleType(ScaleType.CENTER);
            this.cropItem.setImageResource(NUM);
            this.cropItem.setBackgroundDrawable(Theme.createSelectorDrawable(NUM));
            linearLayout.addView(this.cropItem, LayoutHelper.createLinear(70, 48));
            this.cropItem.setOnClickListener(new -$$Lambda$PhotoViewer$ZNbglMdvMBz7fic2b_SNO94Evbk(this));
            this.cropItem.setContentDescription(LocaleController.getString("CropImage", NUM));
            this.rotateItem = new ImageView(this.parentActivity);
            this.rotateItem.setScaleType(ScaleType.CENTER);
            this.rotateItem.setImageResource(NUM);
            this.rotateItem.setBackgroundDrawable(Theme.createSelectorDrawable(NUM));
            linearLayout.addView(this.rotateItem, LayoutHelper.createLinear(70, 48));
            this.rotateItem.setOnClickListener(new -$$Lambda$PhotoViewer$MIXMx0jfsMSq1oOpVJ4spUPP5Ss(this));
            this.rotateItem.setContentDescription(LocaleController.getString("AccDescrRotate", NUM));
            this.paintItem = new ImageView(this.parentActivity);
            this.paintItem.setScaleType(ScaleType.CENTER);
            this.paintItem.setImageResource(NUM);
            this.paintItem.setBackgroundDrawable(Theme.createSelectorDrawable(NUM));
            linearLayout.addView(this.paintItem, LayoutHelper.createLinear(70, 48));
            this.paintItem.setOnClickListener(new -$$Lambda$PhotoViewer$FkVTKWzor1OYhcy-PdaAovVJgkQ(this));
            this.paintItem.setContentDescription(LocaleController.getString("AccDescrPhotoEditor", NUM));
            this.compressItem = new ImageView(this.parentActivity);
            this.compressItem.setTag(Integer.valueOf(1));
            this.compressItem.setScaleType(ScaleType.CENTER);
            this.compressItem.setBackgroundDrawable(Theme.createSelectorDrawable(NUM));
            this.selectedCompression = MessagesController.getGlobalMainSettings().getInt("compress_video2", 1);
            int i2 = this.selectedCompression;
            if (i2 <= 0) {
                this.compressItem.setImageResource(NUM);
            } else if (i2 == 1) {
                this.compressItem.setImageResource(NUM);
            } else if (i2 == 2) {
                this.compressItem.setImageResource(NUM);
            } else if (i2 == 3) {
                this.compressItem.setImageResource(NUM);
            } else if (i2 == 4) {
                this.compressItem.setImageResource(NUM);
            }
            linearLayout.addView(this.compressItem, LayoutHelper.createLinear(70, 48));
            this.compressItem.setOnClickListener(new -$$Lambda$PhotoViewer$BtjFUBdRHmVM1bQF4oFTkkfFVOY(this));
            String[] strArr = new String[]{"240", "360", "480", "720", "1080"};
            ImageView imageView = this.compressItem;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(LocaleController.getString("AccDescrVideoQuality", NUM));
            stringBuilder.append(", ");
            stringBuilder.append(strArr[Math.max(0, this.selectedCompression)]);
            imageView.setContentDescription(stringBuilder.toString());
            this.muteItem = new ImageView(this.parentActivity);
            this.muteItem.setScaleType(ScaleType.CENTER);
            this.muteItem.setBackgroundDrawable(Theme.createSelectorDrawable(NUM));
            linearLayout.addView(this.muteItem, LayoutHelper.createLinear(70, 48));
            this.muteItem.setOnClickListener(new -$$Lambda$PhotoViewer$S_daa09rFx1nnjIEAneeGg9ELbc(this));
            this.cameraItem = new ImageView(this.parentActivity);
            this.cameraItem.setScaleType(ScaleType.CENTER);
            this.cameraItem.setImageResource(NUM);
            this.cameraItem.setBackgroundDrawable(Theme.createSelectorDrawable(NUM));
            this.cameraItem.setContentDescription(LocaleController.getString("AccDescrTakeMorePics", NUM));
            this.containerView.addView(this.cameraItem, LayoutHelper.createFrame(48, 48.0f, 85, 0.0f, 0.0f, 16.0f, 0.0f));
            this.cameraItem.setOnClickListener(new -$$Lambda$PhotoViewer$8WYWZO9APiTa-g69GdZkhyHWH9E(this));
            this.tuneItem = new ImageView(this.parentActivity);
            this.tuneItem.setScaleType(ScaleType.CENTER);
            this.tuneItem.setImageResource(NUM);
            this.tuneItem.setBackgroundDrawable(Theme.createSelectorDrawable(NUM));
            linearLayout.addView(this.tuneItem, LayoutHelper.createLinear(70, 48));
            this.tuneItem.setOnClickListener(new -$$Lambda$PhotoViewer$sARPAyOZF_I_73Q4HYYBPf0SZ70(this));
            this.tuneItem.setContentDescription(LocaleController.getString("AccDescrPhotoAdjust", NUM));
            this.timeItem = new ImageView(this.parentActivity);
            this.timeItem.setScaleType(ScaleType.CENTER);
            this.timeItem.setImageResource(NUM);
            this.timeItem.setBackgroundDrawable(Theme.createSelectorDrawable(NUM));
            this.timeItem.setContentDescription(LocaleController.getString("SetTimer", NUM));
            linearLayout.addView(this.timeItem, LayoutHelper.createLinear(70, 48));
            this.timeItem.setOnClickListener(new -$$Lambda$PhotoViewer$jYKD2cxo5_0tGvpCTvsWOo0KirI(this));
            this.editorDoneLayout = new PickerBottomLayoutViewer(this.actvityContext);
            this.editorDoneLayout.setBackgroundColor(NUM);
            this.editorDoneLayout.updateSelectedCount(0, false);
            this.editorDoneLayout.setVisibility(8);
            this.containerView.addView(this.editorDoneLayout, LayoutHelper.createFrame(-1, 48, 83));
            this.editorDoneLayout.cancelButton.setOnClickListener(new -$$Lambda$PhotoViewer$Th3FzuWk5suxKezwG9EaMgQL5D8(this));
            this.editorDoneLayout.doneButton.setOnClickListener(new -$$Lambda$PhotoViewer$MNTsOUmDycPlm-15mLZjYpUg6Ec(this));
            this.resetButton = new TextView(this.actvityContext);
            this.resetButton.setVisibility(8);
            this.resetButton.setTextSize(1, 14.0f);
            this.resetButton.setTextColor(-1);
            this.resetButton.setGravity(17);
            this.resetButton.setBackgroundDrawable(Theme.createSelectorDrawable(-12763843, 0));
            this.resetButton.setPadding(AndroidUtilities.dp(20.0f), 0, AndroidUtilities.dp(20.0f), 0);
            this.resetButton.setText(LocaleController.getString("Reset", NUM).toUpperCase());
            this.resetButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            this.editorDoneLayout.addView(this.resetButton, LayoutHelper.createFrame(-2, -1, 49));
            this.resetButton.setOnClickListener(new -$$Lambda$PhotoViewer$v61rlDfXP7XMVPYKHGswQHaXc-E(this));
            this.gestureDetector = new GestureDetector(this.containerView.getContext(), this);
            setDoubleTapEnabled(true);
            -$$Lambda$PhotoViewer$PCLASSNAMEVrOt5LzTS-K9H7Q-NcqFyuY -__lambda_photoviewer_pCLASSNAMEvrot5lzts-k9h7q-ncqfyuy = new -$$Lambda$PhotoViewer$PCLASSNAMEVrOt5LzTS-K9H7Q-NcqFyuY(this);
            this.centerImage.setParentView(this.containerView);
            this.centerImage.setCrossfadeAlpha((byte) 2);
            this.centerImage.setInvalidateAll(true);
            this.centerImage.setDelegate(-__lambda_photoviewer_pCLASSNAMEvrot5lzts-k9h7q-ncqfyuy);
            this.leftImage.setParentView(this.containerView);
            this.leftImage.setCrossfadeAlpha((byte) 2);
            this.leftImage.setInvalidateAll(true);
            this.leftImage.setDelegate(-__lambda_photoviewer_pCLASSNAMEvrot5lzts-k9h7q-ncqfyuy);
            this.rightImage.setParentView(this.containerView);
            this.rightImage.setCrossfadeAlpha((byte) 2);
            this.rightImage.setInvalidateAll(true);
            this.rightImage.setDelegate(-__lambda_photoviewer_pCLASSNAMEvrot5lzts-k9h7q-ncqfyuy);
            i = ((WindowManager) ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
            this.checkImageView = new CheckBox(this.containerView.getContext(), NUM) {
                public boolean onTouchEvent(MotionEvent motionEvent) {
                    return PhotoViewer.this.bottomTouchEnabled && super.onTouchEvent(motionEvent);
                }
            };
            this.checkImageView.setDrawBackground(true);
            this.checkImageView.setHasBorder(true);
            this.checkImageView.setSize(40);
            this.checkImageView.setCheckOffset(AndroidUtilities.dp(1.0f));
            this.checkImageView.setColor(-10043398, -1);
            this.checkImageView.setVisibility(8);
            FrameLayoutDrawer frameLayoutDrawer = this.containerView;
            CheckBox checkBox = this.checkImageView;
            float f = (i == 3 || i == 1) ? 58.0f : 68.0f;
            frameLayoutDrawer.addView(checkBox, LayoutHelper.createFrame(40, 40.0f, 53, 0.0f, f, 10.0f, 0.0f));
            if (VERSION.SDK_INT >= 21) {
                FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.checkImageView.getLayoutParams();
                layoutParams2.topMargin += AndroidUtilities.statusBarHeight;
            }
            this.checkImageView.setOnClickListener(new -$$Lambda$PhotoViewer$yIZ3ONbqyCutefV5TGZ7fZK7kgA(this));
            this.photosCounterView = new CounterView(this.parentActivity);
            frameLayoutDrawer = this.containerView;
            CounterView counterView = this.photosCounterView;
            f = (i == 3 || i == 1) ? 58.0f : 68.0f;
            frameLayoutDrawer.addView(counterView, LayoutHelper.createFrame(40, 40.0f, 53, 0.0f, f, 66.0f, 0.0f));
            if (VERSION.SDK_INT >= 21) {
                FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) this.photosCounterView.getLayoutParams();
                layoutParams3.topMargin += AndroidUtilities.statusBarHeight;
            }
            this.photosCounterView.setOnClickListener(new -$$Lambda$PhotoViewer$rPb0RgpaZ3Eh8JF9MD-M8IVx8nE(this));
            this.selectedPhotosListView = new RecyclerListView(this.parentActivity);
            this.selectedPhotosListView.setVisibility(8);
            this.selectedPhotosListView.setAlpha(0.0f);
            this.selectedPhotosListView.setTranslationY((float) (-AndroidUtilities.dp(10.0f)));
            this.selectedPhotosListView.addItemDecoration(new ItemDecoration() {
                public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, State state) {
                    int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
                    if ((view instanceof PhotoPickerPhotoCell) && childAdapterPosition == 0) {
                        rect.left = AndroidUtilities.dp(3.0f);
                    } else {
                        rect.left = 0;
                    }
                    rect.right = AndroidUtilities.dp(3.0f);
                }
            });
            ((DefaultItemAnimator) this.selectedPhotosListView.getItemAnimator()).setDelayAnimations(false);
            this.selectedPhotosListView.setBackgroundColor(NUM);
            this.selectedPhotosListView.setPadding(0, AndroidUtilities.dp(3.0f), 0, AndroidUtilities.dp(3.0f));
            this.selectedPhotosListView.setLayoutManager(new LinearLayoutManager(this.parentActivity, 0, false) {
                public void smoothScrollToPosition(RecyclerView recyclerView, State state, int i) {
                    LinearSmoothScrollerEnd linearSmoothScrollerEnd = new LinearSmoothScrollerEnd(recyclerView.getContext());
                    linearSmoothScrollerEnd.setTargetPosition(i);
                    startSmoothScroll(linearSmoothScrollerEnd);
                }
            });
            RecyclerListView recyclerListView = this.selectedPhotosListView;
            ListAdapter listAdapter = new ListAdapter(this.parentActivity);
            this.selectedPhotosAdapter = listAdapter;
            recyclerListView.setAdapter(listAdapter);
            this.containerView.addView(this.selectedPhotosListView, LayoutHelper.createFrame(-1, 88, 51));
            this.selectedPhotosListView.setOnItemClickListener(new -$$Lambda$PhotoViewer$0AnEmMjG936mbZXkBzDYnmuApXg(this));
            this.captionEditText = new PhotoViewerCaptionEnterView(this.actvityContext, this.containerView, this.windowView) {
                public boolean dispatchTouchEvent(MotionEvent motionEvent) {
                    boolean z = false;
                    try {
                        if (!PhotoViewer.this.bottomTouchEnabled && super.dispatchTouchEvent(motionEvent)) {
                            z = true;
                        }
                        return z;
                    } catch (Exception e) {
                        FileLog.e(e);
                        return false;
                    }
                }

                public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
                    boolean z = false;
                    try {
                        if (!PhotoViewer.this.bottomTouchEnabled && super.onInterceptTouchEvent(motionEvent)) {
                            z = true;
                        }
                        return z;
                    } catch (Exception e) {
                        FileLog.e(e);
                        return false;
                    }
                }

                public boolean onTouchEvent(MotionEvent motionEvent) {
                    return !PhotoViewer.this.bottomTouchEnabled && super.onTouchEvent(motionEvent);
                }

                /* Access modifiers changed, original: protected */
                public void extendActionMode(ActionMode actionMode, Menu menu) {
                    if (PhotoViewer.this.parentChatActivity != null) {
                        PhotoViewer.this.parentChatActivity.extendActionMode(menu);
                    }
                }
            };
            this.captionEditText.setDelegate(new PhotoViewerCaptionEnterViewDelegate() {
                public void onCaptionEnter() {
                    PhotoViewer.this.closeCaptionEnter(true);
                }

                public void onTextChanged(CharSequence charSequence) {
                    if (PhotoViewer.this.mentionsAdapter != null && PhotoViewer.this.captionEditText != null && PhotoViewer.this.parentChatActivity != null && charSequence != null) {
                        PhotoViewer.this.mentionsAdapter.searchUsernameOrHashtag(charSequence.toString(), PhotoViewer.this.captionEditText.getCursorPosition(), PhotoViewer.this.parentChatActivity.messages, false);
                    }
                }

                public void onWindowSizeChanged(int i) {
                    if (i - (ActionBar.getCurrentActionBarHeight() * 2) < AndroidUtilities.dp((float) ((Math.min(3, PhotoViewer.this.mentionsAdapter.getItemCount()) * 36) + (PhotoViewer.this.mentionsAdapter.getItemCount() > 3 ? 18 : 0)))) {
                        PhotoViewer.this.allowMentions = false;
                        if (PhotoViewer.this.mentionListView != null && PhotoViewer.this.mentionListView.getVisibility() == 0) {
                            PhotoViewer.this.mentionListView.setVisibility(4);
                            return;
                        }
                        return;
                    }
                    PhotoViewer.this.allowMentions = true;
                    if (PhotoViewer.this.mentionListView != null && PhotoViewer.this.mentionListView.getVisibility() == 4) {
                        PhotoViewer.this.mentionListView.setVisibility(0);
                    }
                }
            });
            if (VERSION.SDK_INT >= 19) {
                this.captionEditText.setImportantForAccessibility(4);
            }
            this.containerView.addView(this.captionEditText, LayoutHelper.createFrame(-1, -2, 83));
            this.mentionListView = new RecyclerListView(this.actvityContext) {
                public boolean dispatchTouchEvent(MotionEvent motionEvent) {
                    return !PhotoViewer.this.bottomTouchEnabled && super.dispatchTouchEvent(motionEvent);
                }

                public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
                    return !PhotoViewer.this.bottomTouchEnabled && super.onInterceptTouchEvent(motionEvent);
                }

                public boolean onTouchEvent(MotionEvent motionEvent) {
                    return !PhotoViewer.this.bottomTouchEnabled && super.onTouchEvent(motionEvent);
                }
            };
            this.mentionListView.setTag(Integer.valueOf(5));
            this.mentionLayoutManager = new LinearLayoutManager(this.actvityContext) {
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            };
            this.mentionLayoutManager.setOrientation(1);
            this.mentionListView.setLayoutManager(this.mentionLayoutManager);
            this.mentionListView.setBackgroundColor(NUM);
            this.mentionListView.setVisibility(8);
            this.mentionListView.setClipToPadding(true);
            this.mentionListView.setOverScrollMode(2);
            this.containerView.addView(this.mentionListView, LayoutHelper.createFrame(-1, 110, 83));
            recyclerListView = this.mentionListView;
            MentionsAdapter mentionsAdapter = new MentionsAdapter(this.actvityContext, true, 0, new MentionsAdapterDelegate() {
                public void onContextClick(BotInlineResult botInlineResult) {
                }

                public void onContextSearch(boolean z) {
                }

                public void needChangePanelVisibility(boolean z) {
                    if (z) {
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) PhotoViewer.this.mentionListView.getLayoutParams();
                        float min = (float) ((Math.min(3, PhotoViewer.this.mentionsAdapter.getItemCount()) * 36) + (PhotoViewer.this.mentionsAdapter.getItemCount() > 3 ? 18 : 0));
                        layoutParams.height = AndroidUtilities.dp(min);
                        layoutParams.topMargin = -AndroidUtilities.dp(min);
                        PhotoViewer.this.mentionListView.setLayoutParams(layoutParams);
                        if (PhotoViewer.this.mentionListAnimation != null) {
                            PhotoViewer.this.mentionListAnimation.cancel();
                            PhotoViewer.this.mentionListAnimation = null;
                        }
                        if (PhotoViewer.this.mentionListView.getVisibility() == 0) {
                            PhotoViewer.this.mentionListView.setAlpha(1.0f);
                            return;
                        }
                        PhotoViewer.this.mentionLayoutManager.scrollToPositionWithOffset(0, 10000);
                        if (PhotoViewer.this.allowMentions) {
                            PhotoViewer.this.mentionListView.setVisibility(0);
                            PhotoViewer.this.mentionListAnimation = new AnimatorSet();
                            PhotoViewer.this.mentionListAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(PhotoViewer.this.mentionListView, View.ALPHA, new float[]{0.0f, 1.0f})});
                            PhotoViewer.this.mentionListAnimation.addListener(new AnimatorListenerAdapter() {
                                public void onAnimationEnd(Animator animator) {
                                    if (PhotoViewer.this.mentionListAnimation != null && PhotoViewer.this.mentionListAnimation.equals(animator)) {
                                        PhotoViewer.this.mentionListAnimation = null;
                                    }
                                }
                            });
                            PhotoViewer.this.mentionListAnimation.setDuration(200);
                            PhotoViewer.this.mentionListAnimation.start();
                        } else {
                            PhotoViewer.this.mentionListView.setAlpha(1.0f);
                            PhotoViewer.this.mentionListView.setVisibility(4);
                        }
                    } else {
                        if (PhotoViewer.this.mentionListAnimation != null) {
                            PhotoViewer.this.mentionListAnimation.cancel();
                            PhotoViewer.this.mentionListAnimation = null;
                        }
                        if (PhotoViewer.this.mentionListView.getVisibility() != 8) {
                            if (PhotoViewer.this.allowMentions) {
                                PhotoViewer.this.mentionListAnimation = new AnimatorSet();
                                AnimatorSet access$10100 = PhotoViewer.this.mentionListAnimation;
                                Animator[] animatorArr = new Animator[1];
                                animatorArr[0] = ObjectAnimator.ofFloat(PhotoViewer.this.mentionListView, View.ALPHA, new float[]{0.0f});
                                access$10100.playTogether(animatorArr);
                                PhotoViewer.this.mentionListAnimation.addListener(new AnimatorListenerAdapter() {
                                    public void onAnimationEnd(Animator animator) {
                                        if (PhotoViewer.this.mentionListAnimation != null && PhotoViewer.this.mentionListAnimation.equals(animator)) {
                                            PhotoViewer.this.mentionListView.setVisibility(8);
                                            PhotoViewer.this.mentionListAnimation = null;
                                        }
                                    }
                                });
                                PhotoViewer.this.mentionListAnimation.setDuration(200);
                                PhotoViewer.this.mentionListAnimation.start();
                            } else {
                                PhotoViewer.this.mentionListView.setVisibility(8);
                            }
                        }
                    }
                }
            });
            this.mentionsAdapter = mentionsAdapter;
            recyclerListView.setAdapter(mentionsAdapter);
            this.mentionListView.setOnItemClickListener(new -$$Lambda$PhotoViewer$bKgrSm6AofDvar__rtAGVFxdlER0(this));
            this.mentionListView.setOnItemLongClickListener(new -$$Lambda$PhotoViewer$XK2mp7qvOBWcZwrjv0iPj-jz1vM(this));
            if (((AccessibilityManager) this.actvityContext.getSystemService("accessibility")).isEnabled()) {
                this.playButtonAccessibilityOverlay = new View(this.actvityContext);
                this.playButtonAccessibilityOverlay.setContentDescription(LocaleController.getString("AccActionPlay", NUM));
                this.playButtonAccessibilityOverlay.setFocusable(true);
                this.containerView.addView(this.playButtonAccessibilityOverlay, LayoutHelper.createFrame(64, 64, 17));
            }
        }
    }

    public /* synthetic */ WindowInsets lambda$setParentActivity$1$PhotoViewer(View view, WindowInsets windowInsets) {
        WindowInsets windowInsets2 = (WindowInsets) this.lastInsets;
        this.lastInsets = windowInsets;
        if (windowInsets2 == null || !windowInsets2.toString().equals(windowInsets.toString())) {
            if (this.animationInProgress == 1) {
                ClippingImageView clippingImageView = this.animatingImageView;
                clippingImageView.setTranslationX(clippingImageView.getTranslationX() - ((float) getLeftInset()));
                this.animationValues[0][2] = this.animatingImageView.getTranslationX();
            }
            this.windowView.requestLayout();
        }
        this.containerView.setPadding(windowInsets.getSystemWindowInsetLeft(), 0, windowInsets.getSystemWindowInsetRight(), 0);
        return windowInsets.consumeSystemWindowInsets();
    }

    public /* synthetic */ void lambda$setParentActivity$2$PhotoViewer(View view) {
        onSharePressed();
    }

    public /* synthetic */ void lambda$setParentActivity$3$PhotoViewer(View view) {
        this.selectedCompression = this.previousCompression;
        didChangedCompressionLevel(false);
        showQualityView(false);
        requestVideoPreview(2);
    }

    public /* synthetic */ void lambda$setParentActivity$4$PhotoViewer(View view) {
        showQualityView(false);
        requestVideoPreview(2);
    }

    public /* synthetic */ void lambda$setParentActivity$5$PhotoViewer(View view) {
        ChatActivity chatActivity = this.parentChatActivity;
        if (chatActivity == null || !chatActivity.isInScheduleMode() || this.parentChatActivity.isEditingMessageMedia()) {
            sendPressed(true, 0);
        } else {
            AlertsCreator.createScheduleDatePickerDialog(this.parentActivity, UserObject.isUserSelf(this.parentChatActivity.getCurrentUser()), new -$$Lambda$PhotoViewer$U1zArzGnJvar_Mkld4B6r4KrJeFI(this));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x00e9  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0113  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0110  */
    public /* synthetic */ boolean lambda$setParentActivity$8$PhotoViewer(android.view.View r15) {
        /*
        r14 = this;
        r0 = r14.parentChatActivity;
        r1 = 0;
        if (r0 == 0) goto L_0x01b5;
    L_0x0005:
        r0 = r0.isInScheduleMode();
        if (r0 == 0) goto L_0x000d;
    L_0x000b:
        goto L_0x01b5;
    L_0x000d:
        r0 = r14.parentChatActivity;
        r0.getCurrentChat();
        r0 = r14.parentChatActivity;
        r0 = r0.getCurrentUser();
        r2 = r14.parentChatActivity;
        r2 = r2.getCurrentEncryptedChat();
        if (r2 == 0) goto L_0x0021;
    L_0x0020:
        return r1;
    L_0x0021:
        r2 = new org.telegram.ui.ActionBar.ActionBarPopupWindow$ActionBarPopupWindowLayout;
        r3 = r14.parentActivity;
        r2.<init>(r3);
        r14.sendPopupLayout = r2;
        r2 = r14.sendPopupLayout;
        r2.setAnimationEnabled(r1);
        r2 = r14.sendPopupLayout;
        r3 = new org.telegram.ui.PhotoViewer$13;
        r3.<init>();
        r2.setOnTouchListener(r3);
        r2 = r14.sendPopupLayout;
        r3 = new org.telegram.ui.-$$Lambda$PhotoViewer$aArwG34KoZhGPyugN6tEOQe8mEg;
        r3.<init>(r14);
        r2.setDispatchKeyEventListener(r3);
        r2 = r14.sendPopupLayout;
        r2.setShowedFromBotton(r1);
        r2 = r14.sendPopupLayout;
        r2 = r2.getBackgroundDrawable();
        r3 = new android.graphics.PorterDuffColorFilter;
        r4 = -NUM; // 0xffffffffvar_ float:-5.2615274E34 double:NaN;
        r5 = android.graphics.PorterDuff.Mode.MULTIPLY;
        r3.<init>(r4, r5);
        r2.setColorFilter(r3);
        r2 = 0;
        r3 = 0;
    L_0x005d:
        r4 = 3;
        r5 = 2;
        r6 = 1;
        if (r2 >= r5) goto L_0x012f;
    L_0x0062:
        if (r2 != 0) goto L_0x00a8;
    L_0x0064:
        r5 = r14.placeProvider;
        if (r5 == 0) goto L_0x00a8;
    L_0x0068:
        r5 = r5.getSelectedPhotos();
        if (r5 == 0) goto L_0x00a8;
    L_0x006e:
        r5 = r14.placeProvider;
        r5 = r5.getSelectedPhotos();
        r5 = r5.entrySet();
        r5 = r5.iterator();
    L_0x007c:
        r7 = r5.hasNext();
        if (r7 == 0) goto L_0x00a3;
    L_0x0082:
        r7 = r5.next();
        r7 = (java.util.Map.Entry) r7;
        r7 = r7.getValue();
        r8 = r7 instanceof org.telegram.messenger.MediaController.PhotoEntry;
        if (r8 == 0) goto L_0x0098;
    L_0x0090:
        r7 = (org.telegram.messenger.MediaController.PhotoEntry) r7;
        r7 = r7.ttl;
        if (r7 == 0) goto L_0x007c;
    L_0x0096:
        r5 = 1;
        goto L_0x00a4;
    L_0x0098:
        r8 = r7 instanceof org.telegram.messenger.MediaController.SearchImage;
        if (r8 == 0) goto L_0x007c;
    L_0x009c:
        r7 = (org.telegram.messenger.MediaController.SearchImage) r7;
        r7 = r7.ttl;
        if (r7 == 0) goto L_0x007c;
    L_0x00a2:
        goto L_0x0096;
    L_0x00a3:
        r5 = 0;
    L_0x00a4:
        if (r5 == 0) goto L_0x00b2;
    L_0x00a6:
        goto L_0x012b;
    L_0x00a8:
        if (r2 != r6) goto L_0x00b2;
    L_0x00aa:
        r5 = org.telegram.messenger.UserObject.isUserSelf(r0);
        if (r5 == 0) goto L_0x00b2;
    L_0x00b0:
        goto L_0x012b;
    L_0x00b2:
        r5 = new org.telegram.ui.ActionBar.ActionBarMenuSubItem;
        r7 = r14.parentActivity;
        r5.<init>(r7);
        r7 = NUM; // 0x24ffffff float:1.11022296E-16 double:3.066947037E-315;
        r8 = 7;
        r7 = org.telegram.ui.ActionBar.Theme.createSelectorDrawable(r7, r8);
        r5.setBackgroundDrawable(r7);
        if (r2 != 0) goto L_0x00e9;
    L_0x00c6:
        r6 = org.telegram.messenger.UserObject.isUserSelf(r0);
        r7 = NUM; // 0x7var_ec float:1.7945576E38 double:1.052935746E-314;
        if (r6 == 0) goto L_0x00dc;
    L_0x00cf:
        r6 = NUM; // 0x7f0e09e5 float:1.8880175E38 double:1.053163408E-314;
        r8 = "SetReminder";
        r6 = org.telegram.messenger.LocaleController.getString(r8, r6);
        r5.setTextAndIcon(r6, r7);
        goto L_0x00fa;
    L_0x00dc:
        r6 = NUM; // 0x7f0e0971 float:1.887994E38 double:1.053163351E-314;
        r8 = "ScheduleMessage";
        r6 = org.telegram.messenger.LocaleController.getString(r8, r6);
        r5.setTextAndIcon(r6, r7);
        goto L_0x00fa;
    L_0x00e9:
        if (r2 != r6) goto L_0x00fa;
    L_0x00eb:
        r6 = NUM; // 0x7f0e09c8 float:1.8880116E38 double:1.053163394E-314;
        r7 = "SendWithoutSound";
        r6 = org.telegram.messenger.LocaleController.getString(r7, r6);
        r7 = NUM; // 0x7var_ float:1.7945209E38 double:1.0529356567E-314;
        r5.setTextAndIcon(r6, r7);
    L_0x00fa:
        r6 = NUM; // 0x43440000 float:196.0 double:5.57567864E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r5.setMinimumWidth(r6);
        r6 = -1;
        r5.setColors(r6, r6);
        r6 = r14.sendPopupLayout;
        r7 = -1;
        r8 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r9 = org.telegram.messenger.LocaleController.isRTL;
        if (r9 == 0) goto L_0x0113;
    L_0x0110:
        r4 = 5;
        r9 = 5;
        goto L_0x0114;
    L_0x0113:
        r9 = 3;
    L_0x0114:
        r10 = 0;
        r4 = r3 * 48;
        r11 = (float) r4;
        r12 = 0;
        r13 = 0;
        r4 = org.telegram.ui.Components.LayoutHelper.createFrame(r7, r8, r9, r10, r11, r12, r13);
        r6.addView(r5, r4);
        r4 = new org.telegram.ui.-$$Lambda$PhotoViewer$YO7AvWy5kgomuVoNJ5CABQtycKg;
        r4.<init>(r14, r2, r0);
        r5.setOnClickListener(r4);
        r3 = r3 + 1;
    L_0x012b:
        r2 = r2 + 1;
        goto L_0x005d;
    L_0x012f:
        r0 = new org.telegram.ui.ActionBar.ActionBarPopupWindow;
        r2 = r14.sendPopupLayout;
        r3 = -2;
        r0.<init>(r2, r3, r3);
        r14.sendPopupWindow = r0;
        r0 = r14.sendPopupWindow;
        r0.setAnimationEnabled(r1);
        r0 = r14.sendPopupWindow;
        r2 = NUM; // 0x7f0var_ float:1.9007979E38 double:1.05319454E-314;
        r0.setAnimationStyle(r2);
        r0 = r14.sendPopupWindow;
        r0.setOutsideTouchable(r6);
        r0 = r14.sendPopupWindow;
        r0.setClippingEnabled(r6);
        r0 = r14.sendPopupWindow;
        r0.setInputMethodMode(r5);
        r0 = r14.sendPopupWindow;
        r0.setSoftInputMode(r1);
        r0 = r14.sendPopupWindow;
        r0 = r0.getContentView();
        r0.setFocusableInTouchMode(r6);
        r0 = r14.sendPopupLayout;
        r2 = NUM; // 0x447a0000 float:1000.0 double:5.676053805E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r7 = -NUM; // 0xfffffffvar_ float:-0.0 double:NaN;
        r3 = android.view.View.MeasureSpec.makeMeasureSpec(r3, r7);
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r7);
        r0.measure(r3, r2);
        r0 = r14.sendPopupWindow;
        r0.setFocusable(r6);
        r0 = new int[r5];
        r15.getLocationInWindow(r0);
        r2 = r14.sendPopupWindow;
        r3 = 51;
        r7 = r0[r1];
        r8 = r15.getMeasuredWidth();
        r7 = r7 + r8;
        r8 = r14.sendPopupLayout;
        r8 = r8.getMeasuredWidth();
        r7 = r7 - r8;
        r8 = NUM; // 0x41600000 float:14.0 double:5.41896386E-315;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r8);
        r7 = r7 + r8;
        r0 = r0[r6];
        r6 = r14.sendPopupLayout;
        r6 = r6.getMeasuredHeight();
        r0 = r0 - r6;
        r6 = NUM; // 0x41900000 float:18.0 double:5.43450582E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r0 = r0 - r6;
        r2.showAtLocation(r15, r3, r7, r0);
        r15.performHapticFeedback(r4, r5);
    L_0x01b5:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.lambda$setParentActivity$8$PhotoViewer(android.view.View):boolean");
    }

    public /* synthetic */ void lambda$null$6$PhotoViewer(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == 4 && keyEvent.getRepeatCount() == 0) {
            ActionBarPopupWindow actionBarPopupWindow = this.sendPopupWindow;
            if (actionBarPopupWindow != null && actionBarPopupWindow.isShowing()) {
                this.sendPopupWindow.dismiss();
            }
        }
    }

    public /* synthetic */ void lambda$null$7$PhotoViewer(int i, User user, View view) {
        ActionBarPopupWindow actionBarPopupWindow = this.sendPopupWindow;
        if (actionBarPopupWindow != null && actionBarPopupWindow.isShowing()) {
            this.sendPopupWindow.dismiss();
        }
        if (i == 0) {
            AlertsCreator.createScheduleDatePickerDialog(this.parentActivity, UserObject.isUserSelf(user), new -$$Lambda$PhotoViewer$U1zArzGnJvar_Mkld4B6r4KrJeFI(this));
        } else if (i == 1) {
            sendPressed(false, 0);
        }
    }

    public /* synthetic */ void lambda$setParentActivity$9$PhotoViewer(View view) {
        if (this.captionEditText.getTag() == null) {
            switchToEditMode(1);
        }
    }

    public /* synthetic */ void lambda$setParentActivity$10$PhotoViewer(View view) {
        PhotoCropView photoCropView = this.photoCropView;
        if (photoCropView != null) {
            photoCropView.rotate();
        }
    }

    public /* synthetic */ void lambda$setParentActivity$11$PhotoViewer(View view) {
        if (this.captionEditText.getTag() == null) {
            switchToEditMode(3);
        }
    }

    public /* synthetic */ void lambda$setParentActivity$12$PhotoViewer(View view) {
        if (this.captionEditText.getTag() == null) {
            showQualityView(true);
            requestVideoPreview(1);
        }
    }

    public /* synthetic */ void lambda$setParentActivity$13$PhotoViewer(View view) {
        if (this.captionEditText.getTag() == null) {
            this.muteVideo ^= 1;
            updateMuteButton();
            updateVideoInfo();
            if (!this.muteVideo || this.checkImageView.isChecked()) {
                Object obj = this.imagesArrLocals.get(this.currentIndex);
                if (obj instanceof PhotoEntry) {
                    ((PhotoEntry) obj).editedInfo = getCurrentVideoEditedInfo();
                }
            } else {
                this.checkImageView.callOnClick();
            }
        }
    }

    public /* synthetic */ void lambda$setParentActivity$14$PhotoViewer(View view) {
        if (this.placeProvider != null && this.captionEditText.getTag() == null) {
            this.placeProvider.needAddMorePhotos();
            closePhoto(true, false);
        }
    }

    public /* synthetic */ void lambda$setParentActivity$15$PhotoViewer(View view) {
        if (this.captionEditText.getTag() == null) {
            switchToEditMode(2);
        }
    }

    public /* synthetic */ void lambda$setParentActivity$21$PhotoViewer(View view) {
        if (this.parentActivity != null && this.captionEditText.getTag() == null) {
            int i;
            String str;
            BottomSheet.Builder builder = new BottomSheet.Builder(this.parentActivity);
            builder.setUseHardwareLayer(false);
            LinearLayout linearLayout = new LinearLayout(this.parentActivity);
            linearLayout.setOrientation(1);
            builder.setCustomView(linearLayout);
            TextView textView = new TextView(this.parentActivity);
            textView.setLines(1);
            textView.setSingleLine(true);
            textView.setText(LocaleController.getString("MessageLifetime", NUM));
            textView.setTextColor(-1);
            textView.setTextSize(1, 16.0f);
            textView.setEllipsize(TruncateAt.MIDDLE);
            textView.setPadding(AndroidUtilities.dp(21.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(21.0f), AndroidUtilities.dp(4.0f));
            textView.setGravity(16);
            linearLayout.addView(textView, LayoutHelper.createFrame(-1, -2.0f));
            textView.setOnTouchListener(-$$Lambda$PhotoViewer$dNgnxOfGozESSNmhIUiGrmXOvYo.INSTANCE);
            textView = new TextView(this.parentActivity);
            if (this.isCurrentVideo) {
                i = NUM;
                str = "MessageLifetimeVideo";
            } else {
                i = NUM;
                str = "MessageLifetimePhoto";
            }
            textView.setText(LocaleController.getString(str, i));
            textView.setTextColor(-8355712);
            textView.setTextSize(1, 14.0f);
            textView.setEllipsize(TruncateAt.MIDDLE);
            textView.setPadding(AndroidUtilities.dp(21.0f), 0, AndroidUtilities.dp(21.0f), AndroidUtilities.dp(8.0f));
            textView.setGravity(16);
            linearLayout.addView(textView, LayoutHelper.createFrame(-1, -2.0f));
            textView.setOnTouchListener(-$$Lambda$PhotoViewer$NS34BJTSE805Ka6Cky_LZyYatpM.INSTANCE);
            BottomSheet create = builder.create();
            NumberPicker numberPicker = new NumberPicker(this.parentActivity);
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(28);
            Object obj = this.imagesArrLocals.get(this.currentIndex);
            int i2 = obj instanceof PhotoEntry ? ((PhotoEntry) obj).ttl : obj instanceof SearchImage ? ((SearchImage) obj).ttl : 0;
            if (i2 == 0) {
                numberPicker.setValue(MessagesController.getGlobalMainSettings().getInt("self_destruct", 7));
            } else if (i2 < 0 || i2 >= 21) {
                numberPicker.setValue(((i2 / 5) + 21) - 5);
            } else {
                numberPicker.setValue(i2);
            }
            numberPicker.setTextColor(-1);
            numberPicker.setSelectorColor(-11711155);
            numberPicker.setFormatter(-$$Lambda$PhotoViewer$jBfQR6oRFkAIVuAnOk58WefFBr8.INSTANCE);
            linearLayout.addView(numberPicker, LayoutHelper.createLinear(-1, -2));
            AnonymousClass14 anonymousClass14 = new FrameLayout(this.parentActivity) {
                /* Access modifiers changed, original: protected */
                public void onLayout(boolean z, int i, int i2, int i3, int i4) {
                    int childCount = getChildCount();
                    i3 -= i;
                    View view = null;
                    for (i2 = 0; i2 < childCount; i2++) {
                        View childAt = getChildAt(i2);
                        if (((Integer) childAt.getTag()).intValue() == -1) {
                            childAt.layout((i3 - getPaddingRight()) - childAt.getMeasuredWidth(), getPaddingTop(), (i3 - getPaddingRight()) + childAt.getMeasuredWidth(), getPaddingTop() + childAt.getMeasuredHeight());
                            view = childAt;
                        } else if (((Integer) childAt.getTag()).intValue() == -2) {
                            int paddingRight = (i3 - getPaddingRight()) - childAt.getMeasuredWidth();
                            if (view != null) {
                                paddingRight -= view.getMeasuredWidth() + AndroidUtilities.dp(8.0f);
                            }
                            childAt.layout(paddingRight, getPaddingTop(), childAt.getMeasuredWidth() + paddingRight, getPaddingTop() + childAt.getMeasuredHeight());
                        } else {
                            childAt.layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + childAt.getMeasuredWidth(), getPaddingTop() + childAt.getMeasuredHeight());
                        }
                    }
                }
            };
            anonymousClass14.setPadding(AndroidUtilities.dp(8.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(8.0f));
            linearLayout.addView(anonymousClass14, LayoutHelper.createLinear(-1, 52));
            TextView textView2 = new TextView(this.parentActivity);
            textView2.setMinWidth(AndroidUtilities.dp(64.0f));
            textView2.setTag(Integer.valueOf(-1));
            textView2.setTextSize(1, 14.0f);
            textView2.setTextColor(-11944718);
            textView2.setGravity(17);
            textView2.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textView2.setText(LocaleController.getString("Done", NUM).toUpperCase());
            textView2.setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(-11944718));
            textView2.setPadding(AndroidUtilities.dp(10.0f), 0, AndroidUtilities.dp(10.0f), 0);
            anonymousClass14.addView(textView2, LayoutHelper.createFrame(-2, 36, 53));
            textView2.setOnClickListener(new -$$Lambda$PhotoViewer$YEbB9iIsfDsamtaFu8ijwr2rXTc(this, numberPicker, create));
            textView2 = new TextView(this.parentActivity);
            textView2.setMinWidth(AndroidUtilities.dp(64.0f));
            textView2.setTag(Integer.valueOf(-2));
            textView2.setTextSize(1, 14.0f);
            textView2.setTextColor(-11944718);
            textView2.setGravity(17);
            textView2.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textView2.setText(LocaleController.getString("Cancel", NUM).toUpperCase());
            textView2.setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(-11944718));
            textView2.setPadding(AndroidUtilities.dp(10.0f), 0, AndroidUtilities.dp(10.0f), 0);
            anonymousClass14.addView(textView2, LayoutHelper.createFrame(-2, 36, 53));
            textView2.setOnClickListener(new -$$Lambda$PhotoViewer$tJw-2WgyJEzNhE-B7DSLZqOa2Wc(create));
            create.show();
            create.setBackgroundColor(-16777216);
        }
    }

    static /* synthetic */ String lambda$null$18(int i) {
        if (i == 0) {
            return LocaleController.getString("ShortMessageLifetimeForever", NUM);
        }
        if (i < 1 || i >= 21) {
            return LocaleController.formatTTLString((i - 16) * 5);
        }
        return LocaleController.formatTTLString(i);
    }

    public /* synthetic */ void lambda$null$19$PhotoViewer(NumberPicker numberPicker, BottomSheet bottomSheet, View view) {
        int value = numberPicker.getValue();
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putInt("self_destruct", value);
        edit.commit();
        bottomSheet.dismiss();
        if (value < 0 || value >= 21) {
            value = (value - 16) * 5;
        }
        Object obj = this.imagesArrLocals.get(this.currentIndex);
        if (obj instanceof PhotoEntry) {
            ((PhotoEntry) obj).ttl = value;
        } else if (obj instanceof SearchImage) {
            ((SearchImage) obj).ttl = value;
        }
        this.timeItem.setColorFilter(value != 0 ? new PorterDuffColorFilter(-12734994, Mode.MULTIPLY) : null);
        if (!this.checkImageView.isChecked()) {
            this.checkImageView.callOnClick();
        }
    }

    public /* synthetic */ void lambda$setParentActivity$22$PhotoViewer(View view) {
        switchToEditMode(0);
    }

    public /* synthetic */ void lambda$setParentActivity$23$PhotoViewer(View view) {
        if (this.currentEditMode != 1 || this.photoCropView.isReady()) {
            applyCurrentEditMode();
            switchToEditMode(0);
        }
    }

    public /* synthetic */ void lambda$setParentActivity$24$PhotoViewer(View view) {
        this.photoCropView.reset();
    }

    public /* synthetic */ void lambda$setParentActivity$25$PhotoViewer(ImageReceiver imageReceiver, boolean z, boolean z2) {
        if (imageReceiver == this.centerImage && z && !z2 && ((this.currentEditMode == 1 || this.sendPhotoType == 1) && this.photoCropView != null)) {
            Bitmap bitmap = imageReceiver.getBitmap();
            if (bitmap != null) {
                this.photoCropView.setBitmap(bitmap, imageReceiver.getOrientation(), this.sendPhotoType != 1, true);
            }
        }
        if (imageReceiver == this.centerImage && z) {
            PhotoViewerProvider photoViewerProvider = this.placeProvider;
            if (photoViewerProvider != null && photoViewerProvider.scaleToFill() && !this.ignoreDidSetImage) {
                if (this.wasLayout) {
                    setScaleToFill();
                } else {
                    this.dontResetZoomOnFirstLayout = true;
                }
            }
        }
    }

    public /* synthetic */ void lambda$setParentActivity$26$PhotoViewer(View view) {
        if (this.captionEditText.getTag() == null) {
            setPhotoChecked();
        }
    }

    public /* synthetic */ void lambda$setParentActivity$27$PhotoViewer(View view) {
        if (this.captionEditText.getTag() == null) {
            PhotoViewerProvider photoViewerProvider = this.placeProvider;
            if (photoViewerProvider != null && photoViewerProvider.getSelectedPhotosOrder() != null && !this.placeProvider.getSelectedPhotosOrder().isEmpty()) {
                togglePhotosListView(this.isPhotosListViewVisible ^ 1, true);
            }
        }
    }

    public /* synthetic */ void lambda$setParentActivity$28$PhotoViewer(View view, int i) {
        this.ignoreDidSetImage = true;
        int indexOf = this.imagesArrLocals.indexOf(view.getTag());
        if (indexOf >= 0) {
            this.currentIndex = -1;
            setImageIndex(indexOf, true);
        }
        this.ignoreDidSetImage = false;
    }

    public /* synthetic */ void lambda$setParentActivity$29$PhotoViewer(View view, int i) {
        Object item = this.mentionsAdapter.getItem(i);
        i = this.mentionsAdapter.getResultStartPosition();
        int resultLength = this.mentionsAdapter.getResultLength();
        String str = " ";
        PhotoViewerCaptionEnterView photoViewerCaptionEnterView;
        StringBuilder stringBuilder;
        if (item instanceof User) {
            User user = (User) item;
            if (user.username != null) {
                photoViewerCaptionEnterView = this.captionEditText;
                stringBuilder = new StringBuilder();
                stringBuilder.append("@");
                stringBuilder.append(user.username);
                stringBuilder.append(str);
                photoViewerCaptionEnterView.replaceWithText(i, resultLength, stringBuilder.toString(), false);
                return;
            }
            String firstName = UserObject.getFirstName(user);
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(firstName);
            stringBuilder2.append(str);
            SpannableString spannableString = new SpannableString(stringBuilder2.toString());
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("");
            stringBuilder3.append(user.id);
            spannableString.setSpan(new URLSpanUserMentionPhotoViewer(stringBuilder3.toString(), true), 0, spannableString.length(), 33);
            this.captionEditText.replaceWithText(i, resultLength, spannableString, false);
        } else if (item instanceof String) {
            photoViewerCaptionEnterView = this.captionEditText;
            stringBuilder = new StringBuilder();
            stringBuilder.append(item);
            stringBuilder.append(str);
            photoViewerCaptionEnterView.replaceWithText(i, resultLength, stringBuilder.toString(), false);
        } else if (item instanceof KeywordResult) {
            String str2 = ((KeywordResult) item).emoji;
            this.captionEditText.addEmojiToRecent(str2);
            this.captionEditText.replaceWithText(i, resultLength, str2, true);
        }
    }

    public /* synthetic */ boolean lambda$setParentActivity$31$PhotoViewer(View view, int i) {
        if (!(this.mentionsAdapter.getItem(i) instanceof String)) {
            return false;
        }
        Builder builder = new Builder(this.parentActivity);
        builder.setTitle(LocaleController.getString("AppName", NUM));
        builder.setMessage(LocaleController.getString("ClearSearch", NUM));
        builder.setPositiveButton(LocaleController.getString("ClearButton", NUM).toUpperCase(), new -$$Lambda$PhotoViewer$tlN5IMX8sO3FJ1PWxMARBzKi2wc(this));
        builder.setNegativeButton(LocaleController.getString("Cancel", NUM), null);
        showAlertDialog(builder);
        return true;
    }

    public /* synthetic */ void lambda$null$30$PhotoViewer(DialogInterface dialogInterface, int i) {
        this.mentionsAdapter.clearRecentHashtags();
    }

    private void sendPressed(boolean z, int i) {
        if (this.captionEditText.getTag() == null) {
            if (this.sendPhotoType == 1) {
                applyCurrentEditMode();
            }
            if (!(this.placeProvider == null || this.doneButtonPressed)) {
                ChatActivity chatActivity = this.parentChatActivity;
                if (chatActivity != null) {
                    Chat currentChat = chatActivity.getCurrentChat();
                    if (this.parentChatActivity.getCurrentUser() != null || ((ChatObject.isChannel(currentChat) && currentChat.megagroup) || !ChatObject.isChannel(currentChat))) {
                        Editor edit = MessagesController.getNotificationsSettings(this.currentAccount).edit();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("silent_");
                        stringBuilder.append(this.parentChatActivity.getDialogId());
                        edit.putBoolean(stringBuilder.toString(), z ^ 1).commit();
                    }
                }
                this.placeProvider.sendButtonPressed(this.currentIndex, getCurrentVideoEditedInfo(), z, i);
                this.doneButtonPressed = true;
                closePhoto(false, false);
            }
        }
    }

    private boolean checkInlinePermissions() {
        Activity activity = this.parentActivity;
        if (activity == null) {
            return false;
        }
        if (VERSION.SDK_INT < 23 || Settings.canDrawOverlays(activity)) {
            return true;
        }
        new Builder(this.parentActivity).setTitle(LocaleController.getString("AppName", NUM)).setMessage(LocaleController.getString("PermissionDrawAboveOtherApps", NUM)).setPositiveButton(LocaleController.getString("PermissionOpenSettings", NUM), new -$$Lambda$PhotoViewer$_jaa_bD76RyBYqeandF7XoujxXI(this)).show();
        return false;
    }

    public /* synthetic */ void lambda$checkInlinePermissions$32$PhotoViewer(DialogInterface dialogInterface, int i) {
        Activity activity = this.parentActivity;
        if (activity != null) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("package:");
                stringBuilder.append(this.parentActivity.getPackageName());
                activity.startActivity(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse(stringBuilder.toString())));
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    private TextView createCaptionTextView() {
        AnonymousClass23 anonymousClass23 = new TextView(this.actvityContext) {
            public boolean onTouchEvent(MotionEvent motionEvent) {
                return PhotoViewer.this.bottomTouchEnabled && super.onTouchEvent(motionEvent);
            }
        };
        anonymousClass23.setMovementMethod(new LinkMovementMethodMy(this, null));
        anonymousClass23.setPadding(AndroidUtilities.dp(20.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(20.0f), AndroidUtilities.dp(8.0f));
        anonymousClass23.setLinkTextColor(-8994063);
        anonymousClass23.setTextColor(-1);
        anonymousClass23.setHighlightColor(NUM);
        anonymousClass23.setGravity((LocaleController.isRTL ? 5 : 3) | 16);
        anonymousClass23.setTextSize(1, 16.0f);
        anonymousClass23.setVisibility(4);
        anonymousClass23.setOnClickListener(new -$$Lambda$PhotoViewer$I9-uiAsgMo4nvqkxumKlnFoVA1E(this));
        return anonymousClass23;
    }

    public /* synthetic */ void lambda$createCaptionTextView$33$PhotoViewer(View view) {
        if (this.needCaptionLayout) {
            openCaptionEnter();
        }
    }

    private int getLeftInset() {
        Object obj = this.lastInsets;
        return (obj == null || VERSION.SDK_INT < 21) ? 0 : ((WindowInsets) obj).getSystemWindowInsetLeft();
    }

    private int getRightInset() {
        Object obj = this.lastInsets;
        return (obj == null || VERSION.SDK_INT < 21) ? 0 : ((WindowInsets) obj).getSystemWindowInsetRight();
    }

    private void dismissInternal() {
        try {
            if (this.windowView.getParent() != null) {
                ((LaunchActivity) this.parentActivity).drawerLayoutContainer.setAllowDrawContent(true);
                ((WindowManager) this.parentActivity.getSystemService("window")).removeView(this.windowView);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void switchToPip() {
        if (this.videoPlayer != null && this.textureUploaded && checkInlinePermissions() && !this.changingTextureView && !this.switchingInlineMode && !this.isInline) {
            if (PipInstance != null) {
                PipInstance.destroyPhotoViewer();
            }
            this.openedFullScreenVideo = false;
            PipInstance = Instance;
            Instance = null;
            this.switchingInlineMode = true;
            this.isVisible = false;
            PlaceProviderObject placeProviderObject = this.currentPlaceObject;
            if (placeProviderObject != null) {
                placeProviderObject.imageReceiver.setVisible(true, true);
                AnimatedFileDrawable animation = this.currentPlaceObject.imageReceiver.getAnimation();
                if (animation != null) {
                    Bitmap animatedBitmap = animation.getAnimatedBitmap();
                    if (animatedBitmap != null) {
                        try {
                            Bitmap bitmap = this.videoTextureView.getBitmap(animatedBitmap.getWidth(), animatedBitmap.getHeight());
                            new Canvas(animatedBitmap).drawBitmap(bitmap, 0.0f, 0.0f, null);
                            bitmap.recycle();
                        } catch (Throwable th) {
                            FileLog.e(th);
                        }
                    }
                    animation.seekTo(this.videoPlayer.getCurrentPosition(), true);
                    this.currentPlaceObject.imageReceiver.setAllowStartAnimation(true);
                    this.currentPlaceObject.imageReceiver.startAnimation();
                }
            }
            if (VERSION.SDK_INT >= 21) {
                this.pipAnimationInProgress = true;
                org.telegram.ui.Components.Rect pipRect = PipVideoView.getPipRect(this.aspectRatioFrameLayout.getAspectRatio());
                float width = pipRect.width / ((float) this.videoTextureView.getWidth());
                pipRect.y += (float) AndroidUtilities.statusBarHeight;
                AnimatorSet animatorSet = new AnimatorSet();
                Animator[] animatorArr = new Animator[13];
                animatorArr[0] = ObjectAnimator.ofFloat(this.textureImageView, View.SCALE_X, new float[]{width});
                animatorArr[1] = ObjectAnimator.ofFloat(this.textureImageView, View.SCALE_Y, new float[]{width});
                animatorArr[2] = ObjectAnimator.ofFloat(this.textureImageView, View.TRANSLATION_X, new float[]{pipRect.x});
                animatorArr[3] = ObjectAnimator.ofFloat(this.textureImageView, View.TRANSLATION_Y, new float[]{pipRect.y});
                animatorArr[4] = ObjectAnimator.ofFloat(this.videoTextureView, View.SCALE_X, new float[]{width});
                animatorArr[5] = ObjectAnimator.ofFloat(this.videoTextureView, View.SCALE_Y, new float[]{width});
                animatorArr[6] = ObjectAnimator.ofFloat(this.videoTextureView, View.TRANSLATION_X, new float[]{(pipRect.x - this.aspectRatioFrameLayout.getX()) + ((float) getLeftInset())});
                animatorArr[7] = ObjectAnimator.ofFloat(this.videoTextureView, View.TRANSLATION_Y, new float[]{pipRect.y - this.aspectRatioFrameLayout.getY()});
                animatorArr[8] = ObjectAnimator.ofInt(this.backgroundDrawable, AnimationProperties.COLOR_DRAWABLE_ALPHA, new int[]{0});
                animatorArr[9] = ObjectAnimator.ofFloat(this.actionBar, View.ALPHA, new float[]{0.0f});
                animatorArr[10] = ObjectAnimator.ofFloat(this.bottomLayout, View.ALPHA, new float[]{0.0f});
                animatorArr[11] = ObjectAnimator.ofFloat(this.captionTextView, View.ALPHA, new float[]{0.0f});
                animatorArr[12] = ObjectAnimator.ofFloat(this.groupedPhotosListView, View.ALPHA, new float[]{0.0f});
                animatorSet.playTogether(animatorArr);
                animatorSet.setInterpolator(new DecelerateInterpolator());
                animatorSet.setDuration(250);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        PhotoViewer.this.pipAnimationInProgress = false;
                        PhotoViewer.this.switchToInlineRunnable.run();
                    }
                });
                animatorSet.start();
                return;
            }
            this.switchToInlineRunnable.run();
            dismissInternal();
        }
    }

    public VideoPlayer getVideoPlayer() {
        return this.videoPlayer;
    }

    public void exitFromPip() {
        if (this.isInline) {
            if (Instance != null) {
                Instance.closePhoto(false, true);
            }
            Instance = PipInstance;
            PipInstance = null;
            this.switchingInlineMode = true;
            Bitmap bitmap = this.currentBitmap;
            if (bitmap != null) {
                bitmap.recycle();
                this.currentBitmap = null;
            }
            this.changingTextureView = true;
            this.isInline = false;
            this.videoTextureView.setVisibility(4);
            this.aspectRatioFrameLayout.addView(this.videoTextureView);
            if (ApplicationLoader.mainInterfacePaused) {
                try {
                    this.parentActivity.startService(new Intent(ApplicationLoader.applicationContext, BringAppForegroundService.class));
                } catch (Throwable th) {
                    FileLog.e(th);
                }
            }
            if (VERSION.SDK_INT >= 21) {
                this.pipAnimationInProgress = true;
                org.telegram.ui.Components.Rect pipRect = PipVideoView.getPipRect(this.aspectRatioFrameLayout.getAspectRatio());
                float f = pipRect.width / ((float) this.textureImageView.getLayoutParams().width);
                pipRect.y += (float) AndroidUtilities.statusBarHeight;
                this.textureImageView.setScaleX(f);
                this.textureImageView.setScaleY(f);
                this.textureImageView.setTranslationX(pipRect.x);
                this.textureImageView.setTranslationY(pipRect.y);
                this.videoTextureView.setScaleX(f);
                this.videoTextureView.setScaleY(f);
                this.videoTextureView.setTranslationX(pipRect.x - this.aspectRatioFrameLayout.getX());
                this.videoTextureView.setTranslationY(pipRect.y - this.aspectRatioFrameLayout.getY());
            } else {
                this.pipVideoView.close();
                this.pipVideoView = null;
            }
            try {
                this.isVisible = true;
                ((WindowManager) this.parentActivity.getSystemService("window")).addView(this.windowView, this.windowLayoutParams);
                if (this.currentPlaceObject != null) {
                    this.currentPlaceObject.imageReceiver.setVisible(false, false);
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (VERSION.SDK_INT >= 21) {
                this.waitingForDraw = 4;
            }
        }
    }

    private void updateVideoSeekPreviewPosition() {
        int thumbX = (this.videoPlayerSeekbar.getThumbX() + AndroidUtilities.dp(48.0f)) - (this.videoPreviewFrame.getMeasuredWidth() / 2);
        int dp = AndroidUtilities.dp(10.0f);
        int measuredWidth = (this.videoPlayerControlFrameLayout.getMeasuredWidth() - AndroidUtilities.dp(10.0f)) - (this.videoPreviewFrame.getMeasuredWidth() / 2);
        if (thumbX < dp) {
            thumbX = dp;
        } else if (thumbX >= measuredWidth) {
            thumbX = measuredWidth;
        }
        this.videoPreviewFrame.setTranslationX((float) thumbX);
    }

    private void showVideoSeekPreviewPosition(boolean z) {
        if ((z && this.videoPreviewFrame.getTag() != null) || (!z && this.videoPreviewFrame.getTag() == null)) {
            return;
        }
        if (!z || this.videoPreviewFrame.isReady()) {
            AnimatorSet animatorSet = this.videoPreviewFrameAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            this.videoPreviewFrame.setTag(z ? Integer.valueOf(1) : null);
            this.videoPreviewFrameAnimation = new AnimatorSet();
            animatorSet = this.videoPreviewFrameAnimation;
            Animator[] animatorArr = new Animator[1];
            VideoSeekPreviewImage videoSeekPreviewImage = this.videoPreviewFrame;
            Property property = View.ALPHA;
            float[] fArr = new float[1];
            fArr[0] = z ? 1.0f : 0.0f;
            animatorArr[0] = ObjectAnimator.ofFloat(videoSeekPreviewImage, property, fArr);
            animatorSet.playTogether(animatorArr);
            this.videoPreviewFrameAnimation.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    PhotoViewer.this.videoPreviewFrameAnimation = null;
                }
            });
            this.videoPreviewFrameAnimation.setDuration(180);
            this.videoPreviewFrameAnimation.start();
            return;
        }
        this.needShowOnReady = z;
    }

    private void createVideoControlsInterface() {
        this.videoPlayerSeekbar = new SeekBar(this.containerView.getContext());
        this.videoPlayerSeekbar.setLineHeight(AndroidUtilities.dp(4.0f));
        this.videoPlayerSeekbar.setColors(NUM, NUM, -2764585, -1, -1);
        this.videoPlayerSeekbar.setDelegate(new SeekBarDelegate() {
            public void onSeekBarDrag(float f) {
                if (PhotoViewer.this.videoPlayer != null) {
                    if (!PhotoViewer.this.inPreview && PhotoViewer.this.videoTimelineView.getVisibility() == 0) {
                        f = PhotoViewer.this.videoTimelineView.getLeftProgress() + ((PhotoViewer.this.videoTimelineView.getRightProgress() - PhotoViewer.this.videoTimelineView.getLeftProgress()) * f);
                    }
                    long duration = PhotoViewer.this.videoPlayer.getDuration();
                    if (duration == -9223372036854775807L) {
                        PhotoViewer.this.seekToProgressPending = f;
                    } else {
                        PhotoViewer.this.videoPlayer.seekTo((long) ((int) (f * ((float) duration))));
                    }
                    PhotoViewer.this.showVideoSeekPreviewPosition(false);
                    PhotoViewer.this.needShowOnReady = false;
                }
            }

            public void onSeekBarContinuousDrag(float f) {
                if (!(PhotoViewer.this.videoPlayer == null || PhotoViewer.this.videoPreviewFrame == null)) {
                    PhotoViewer.this.videoPreviewFrame.setProgress(f, PhotoViewer.this.videoPlayerSeekbar.getWidth());
                }
                PhotoViewer.this.showVideoSeekPreviewPosition(true);
                PhotoViewer.this.updateVideoSeekPreviewPosition();
            }
        });
        this.videoPlayerControlFrameLayout = new FrameLayout(this.containerView.getContext()) {
            public boolean onTouchEvent(MotionEvent motionEvent) {
                motionEvent.getX();
                motionEvent.getY();
                if (PhotoViewer.this.videoPlayerSeekbar.onTouch(motionEvent.getAction(), motionEvent.getX() - ((float) AndroidUtilities.dp(48.0f)), motionEvent.getY())) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                }
                return true;
            }

            /* Access modifiers changed, original: protected */
            public void onMeasure(int i, int i2) {
                super.onMeasure(i, i2);
                long j = 0;
                if (PhotoViewer.this.videoPlayer != null) {
                    long duration = PhotoViewer.this.videoPlayer.getDuration();
                    if (duration != -9223372036854775807L) {
                        j = duration;
                    }
                }
                j /= 1000;
                Paint paint = PhotoViewer.this.videoPlayerTime.getPaint();
                r10 = new Object[4];
                long j2 = j / 60;
                r10[0] = Long.valueOf(j2);
                j %= 60;
                r10[1] = Long.valueOf(j);
                r10[2] = Long.valueOf(j2);
                r10[3] = Long.valueOf(j);
                PhotoViewer.this.videoPlayerSeekbar.setSize((getMeasuredWidth() - AndroidUtilities.dp(64.0f)) - ((int) Math.ceil((double) paint.measureText(String.format("%02d:%02d / %02d:%02d", r10)))), getMeasuredHeight());
            }

            /* Access modifiers changed, original: protected */
            public void onLayout(boolean z, int i, int i2, int i3, int i4) {
                float currentPosition;
                super.onLayout(z, i, i2, i3, i4);
                if (PhotoViewer.this.videoPlayer != null) {
                    currentPosition = ((float) PhotoViewer.this.videoPlayer.getCurrentPosition()) / ((float) PhotoViewer.this.videoPlayer.getDuration());
                    if (!PhotoViewer.this.inPreview && PhotoViewer.this.videoTimelineView.getVisibility() == 0) {
                        currentPosition -= PhotoViewer.this.videoTimelineView.getLeftProgress();
                        if (currentPosition < 0.0f) {
                            currentPosition = 0.0f;
                        }
                        currentPosition /= PhotoViewer.this.videoTimelineView.getRightProgress() - PhotoViewer.this.videoTimelineView.getLeftProgress();
                        if (currentPosition > 1.0f) {
                            currentPosition = 1.0f;
                        }
                    }
                } else {
                    currentPosition = 0.0f;
                }
                PhotoViewer.this.videoPlayerSeekbar.setProgress(currentPosition);
                PhotoViewer.this.videoTimelineView.setProgress(currentPosition);
            }

            /* Access modifiers changed, original: protected */
            public void onDraw(Canvas canvas) {
                canvas.save();
                canvas.translate((float) AndroidUtilities.dp(48.0f), 0.0f);
                PhotoViewer.this.videoPlayerSeekbar.draw(canvas);
                canvas.restore();
            }
        };
        this.videoPlayerControlFrameLayout.setWillNotDraw(false);
        this.bottomLayout.addView(this.videoPlayerControlFrameLayout, LayoutHelper.createFrame(-1, -1, 51));
        this.videoPreviewFrame = new VideoSeekPreviewImage(this.containerView.getContext(), new -$$Lambda$PhotoViewer$om-yNq7QQUVRvzeIpHWwXj19jW8(this)) {
            /* Access modifiers changed, original: protected */
            public void onLayout(boolean z, int i, int i2, int i3, int i4) {
                super.onLayout(z, i, i2, i3, i4);
                PhotoViewer.this.updateVideoSeekPreviewPosition();
            }

            public void setVisibility(int i) {
                super.setVisibility(i);
                if (i == 0) {
                    PhotoViewer.this.updateVideoSeekPreviewPosition();
                }
            }
        };
        this.videoPreviewFrame.setAlpha(0.0f);
        this.containerView.addView(this.videoPreviewFrame, LayoutHelper.createFrame(-2, -2.0f, 83, 0.0f, 0.0f, 0.0f, 58.0f));
        this.videoPlayButton = new ImageView(this.containerView.getContext());
        this.videoPlayButton.setScaleType(ScaleType.CENTER);
        this.videoPlayerControlFrameLayout.addView(this.videoPlayButton, LayoutHelper.createFrame(48, 48.0f, 51, 4.0f, 0.0f, 0.0f, 0.0f));
        this.videoPlayButton.setFocusable(true);
        this.videoPlayButton.setContentDescription(LocaleController.getString("AccActionPlay", NUM));
        this.videoPlayButton.setOnClickListener(new -$$Lambda$PhotoViewer$XmFAQDBOZ_COTRNsUhQ5tVoPbTA(this));
        this.videoPlayerTime = new SimpleTextView(this.containerView.getContext());
        this.videoPlayerTime.setTextColor(-1);
        this.videoPlayerTime.setGravity(53);
        this.videoPlayerTime.setTextSize(13);
        this.videoPlayerControlFrameLayout.addView(this.videoPlayerTime, LayoutHelper.createFrame(-2, -1.0f, 53, 0.0f, 17.0f, 7.0f, 0.0f));
    }

    public /* synthetic */ void lambda$createVideoControlsInterface$34$PhotoViewer() {
        if (this.needShowOnReady) {
            showVideoSeekPreviewPosition(true);
        }
    }

    public /* synthetic */ void lambda$createVideoControlsInterface$35$PhotoViewer(View view) {
        VideoPlayer videoPlayer = this.videoPlayer;
        if (videoPlayer != null) {
            if (this.isPlaying) {
                videoPlayer.pause();
            } else {
                if (this.isCurrentVideo) {
                    if (Math.abs(this.videoTimelineView.getProgress() - 1.0f) < 0.01f || this.videoPlayer.getCurrentPosition() == this.videoPlayer.getDuration()) {
                        this.videoPlayer.seekTo(0);
                    }
                } else if (Math.abs(this.videoPlayerSeekbar.getProgress() - 1.0f) < 0.01f || this.videoPlayer.getCurrentPosition() == this.videoPlayer.getDuration()) {
                    this.videoPlayer.seekTo(0);
                }
                this.videoPlayer.play();
            }
            this.containerView.invalidate();
        }
    }

    private void openCaptionEnter() {
        if (this.imageMoveAnimation == null && this.changeModeAnimation == null && this.currentEditMode == 0) {
            int i = this.sendPhotoType;
            if (i != 1 && i != 3 && i != 10) {
                this.selectedPhotosListView.setVisibility(8);
                this.selectedPhotosListView.setEnabled(false);
                this.selectedPhotosListView.setAlpha(0.0f);
                this.selectedPhotosListView.setTranslationY((float) (-AndroidUtilities.dp(10.0f)));
                this.photosCounterView.setRotationX(0.0f);
                this.isPhotosListViewVisible = false;
                this.captionEditText.setTag(Integer.valueOf(1));
                this.captionEditText.openKeyboard();
                this.captionEditText.setImportantForAccessibility(0);
                this.lastTitle = this.actionBar.getTitle();
                if (this.isCurrentVideo) {
                    int i2;
                    String str;
                    ActionBar actionBar = this.actionBar;
                    if (this.muteVideo) {
                        i2 = NUM;
                        str = "GifCaption";
                    } else {
                        i2 = NUM;
                        str = "VideoCaption";
                    }
                    actionBar.setTitle(LocaleController.getString(str, i2));
                    this.actionBar.setSubtitle(null);
                    return;
                }
                this.actionBar.setTitle(LocaleController.getString("PhotoCaption", NUM));
            }
        }
    }

    private VideoEditedInfo getCurrentVideoEditedInfo() {
        if (!this.isCurrentVideo || this.currentPlayingVideoFile == null || this.compressionsCount == 0) {
            return null;
        }
        VideoEditedInfo videoEditedInfo = new VideoEditedInfo();
        videoEditedInfo.startTime = this.startTime;
        videoEditedInfo.endTime = this.endTime;
        videoEditedInfo.start = this.videoCutStart;
        videoEditedInfo.end = this.videoCutEnd;
        videoEditedInfo.rotationValue = this.rotationValue;
        videoEditedInfo.originalWidth = this.originalWidth;
        videoEditedInfo.originalHeight = this.originalHeight;
        videoEditedInfo.bitrate = this.bitrate;
        videoEditedInfo.originalPath = this.currentPlayingVideoFile.getPath();
        int i = this.estimatedSize;
        videoEditedInfo.estimatedSize = i != 0 ? (long) i : 1;
        videoEditedInfo.estimatedDuration = this.estimatedDuration;
        videoEditedInfo.framerate = this.videoFramerate;
        int i2 = -1;
        if (this.muteVideo || !(this.compressItem.getTag() == null || this.selectedCompression == this.compressionsCount - 1)) {
            if (this.muteVideo) {
                this.selectedCompression = 1;
                updateWidthHeightBitrateForCompression();
            }
            videoEditedInfo.resultWidth = this.resultWidth;
            videoEditedInfo.resultHeight = this.resultHeight;
            if (!this.muteVideo) {
                i2 = this.bitrate;
            }
            videoEditedInfo.bitrate = i2;
            videoEditedInfo.muted = this.muteVideo;
        } else {
            videoEditedInfo.resultWidth = this.originalWidth;
            videoEditedInfo.resultHeight = this.originalHeight;
            if (!this.muteVideo) {
                i2 = this.originalBitrate;
            }
            videoEditedInfo.bitrate = i2;
            videoEditedInfo.muted = this.muteVideo;
        }
        return videoEditedInfo;
    }

    private boolean supportsSendingNewEntities() {
        ChatActivity chatActivity = this.parentChatActivity;
        if (chatActivity != null) {
            EncryptedChat encryptedChat = chatActivity.currentEncryptedChat;
            if (encryptedChat == null || AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 101) {
                return true;
            }
        }
        return false;
    }

    private void closeCaptionEnter(boolean z) {
        int i = this.currentIndex;
        if (i >= 0 && i < this.imagesArrLocals.size()) {
            Object obj = this.imagesArrLocals.get(this.currentIndex);
            CharSequence charSequence = null;
            if (z) {
                CharSequence[] charSequenceArr = new CharSequence[]{this.captionEditText.getFieldCharSequence()};
                ArrayList entities = MediaDataController.getInstance(this.currentAccount).getEntities(charSequenceArr, supportsSendingNewEntities());
                if (obj instanceof PhotoEntry) {
                    PhotoEntry photoEntry = (PhotoEntry) obj;
                    photoEntry.caption = charSequenceArr[0];
                    photoEntry.entities = entities;
                } else if (obj instanceof SearchImage) {
                    SearchImage searchImage = (SearchImage) obj;
                    searchImage.caption = charSequenceArr[0];
                    searchImage.entities = entities;
                }
                if (!(this.captionEditText.getFieldCharSequence().length() == 0 || this.placeProvider.isPhotoChecked(this.currentIndex))) {
                    setPhotoChecked();
                }
                setCurrentCaption(null, charSequenceArr[0], false);
            }
            this.captionEditText.setTag(null);
            String str = this.lastTitle;
            if (str != null) {
                this.actionBar.setTitle(str);
                this.lastTitle = null;
            }
            if (this.isCurrentVideo) {
                ActionBar actionBar = this.actionBar;
                if (!this.muteVideo) {
                    charSequence = this.currentSubtitle;
                }
                actionBar.setSubtitle(charSequence);
            }
            updateCaptionTextForCurrentPhoto(obj);
            if (this.captionEditText.isPopupShowing()) {
                this.captionEditText.hidePopup();
            }
            this.captionEditText.closeKeyboard();
            if (VERSION.SDK_INT >= 19) {
                this.captionEditText.setImportantForAccessibility(4);
            }
        }
    }

    private void updateVideoPlayerTime() {
        CharSequence format;
        VideoPlayer videoPlayer = this.videoPlayer;
        String str = "%02d:%02d / %02d:%02d";
        Integer valueOf = Integer.valueOf(0);
        if (videoPlayer == null) {
            format = String.format(str, new Object[]{valueOf, valueOf, valueOf, valueOf});
        } else {
            long currentPosition = videoPlayer.getCurrentPosition();
            long j = 0;
            if (currentPosition < 0) {
                currentPosition = 0;
            }
            long duration = this.videoPlayer.getDuration();
            if (duration >= 0) {
                j = duration;
            }
            if (j == -9223372036854775807L || currentPosition == -9223372036854775807L) {
                format = String.format(str, new Object[]{valueOf, valueOf, valueOf, valueOf});
            } else {
                if (!this.inPreview && this.videoTimelineView.getVisibility() == 0) {
                    j = (long) (((float) j) * (this.videoTimelineView.getRightProgress() - this.videoTimelineView.getLeftProgress()));
                    currentPosition = (long) (((float) currentPosition) - (this.videoTimelineView.getLeftProgress() * ((float) j)));
                    if (currentPosition > j) {
                        currentPosition = j;
                    }
                }
                currentPosition /= 1000;
                j /= 1000;
                format = String.format(str, new Object[]{Long.valueOf(currentPosition / 60), Long.valueOf(currentPosition % 60), Long.valueOf(j / 60), Long.valueOf(j % 60)});
            }
        }
        this.videoPlayerTime.setText(format);
    }

    private void checkBufferedProgress(float f) {
        if (!(!this.isStreaming || this.parentActivity == null || this.streamingAlertShown || this.videoPlayer == null)) {
            MessageObject messageObject = this.currentMessageObject;
            if (messageObject != null) {
                Document document = messageObject.getDocument();
                if (document != null && this.currentMessageObject.getDuration() >= 20 && f < 0.9f) {
                    int i = document.size;
                    if ((((float) i) * f >= 5242880.0f || (f >= 0.5f && i >= 2097152)) && Math.abs(SystemClock.elapsedRealtime() - this.startedPlayTime) >= 2000) {
                        if (this.videoPlayer.getDuration() == -9223372036854775807L) {
                            Toast.makeText(this.parentActivity, LocaleController.getString("VideoDoesNotSupportStreaming", NUM), 1).show();
                        }
                        this.streamingAlertShown = true;
                    }
                }
            }
        }
    }

    public void injectVideoPlayer(VideoPlayer videoPlayer) {
        this.injectingVideoPlayer = videoPlayer;
    }

    public void injectVideoPlayerSurface(SurfaceTexture surfaceTexture) {
        this.injectingVideoPlayerSurface = surfaceTexture;
    }

    public boolean isInjectingVideoPlayer() {
        return this.injectingVideoPlayer != null;
    }

    private void updatePlayerState(boolean z, int i) {
        if (this.videoPlayer != null) {
            PipVideoView pipVideoView;
            if (this.isStreaming) {
                if (i != 2 || !this.skipFirstBufferingProgress) {
                    boolean z2 = this.seekToProgressPending != 0.0f || i == 2;
                    toggleMiniProgress(z2, true);
                } else if (z) {
                    this.skipFirstBufferingProgress = false;
                }
            }
            if (!z || i == 4 || i == 1) {
                try {
                    this.parentActivity.getWindow().clearFlags(128);
                    this.keepScreenOnFlagSet = false;
                } catch (Exception e) {
                    FileLog.e(e);
                }
            } else {
                try {
                    this.parentActivity.getWindow().addFlags(128);
                    this.keepScreenOnFlagSet = true;
                } catch (Exception e2) {
                    FileLog.e(e2);
                }
            }
            if (i == 3 || i == 1) {
                if (this.currentMessageObject != null) {
                    this.videoPreviewFrame.open(this.videoPlayer.getCurrentUri());
                }
                if (this.seekToProgressPending != 0.0f) {
                    this.videoPlayer.seekTo((long) ((int) (((float) this.videoPlayer.getDuration()) * this.seekToProgressPending)));
                    this.seekToProgressPending = 0.0f;
                    MessageObject messageObject = this.currentMessageObject;
                    if (!(messageObject == null || FileLoader.getInstance(messageObject.currentAccount).isLoadingVideoAny(this.currentMessageObject.getDocument()))) {
                        this.skipFirstBufferingProgress = true;
                    }
                }
            }
            MessageObject messageObject2;
            if (i == 3) {
                if (this.aspectRatioFrameLayout.getVisibility() != 0) {
                    this.aspectRatioFrameLayout.setVisibility(0);
                }
                if (!this.pipItem.isEnabled()) {
                    this.pipAvailable = true;
                    this.pipItem.setEnabled(true);
                    this.pipItem.setAlpha(1.0f);
                }
                this.playerWasReady = true;
                messageObject2 = this.currentMessageObject;
                if (messageObject2 != null && messageObject2.isVideo()) {
                    AndroidUtilities.cancelRunOnUIThread(this.setLoadingRunnable);
                    FileLoader.getInstance(this.currentMessageObject.currentAccount).removeLoadingVideo(this.currentMessageObject.getDocument(), true, false);
                }
            } else if (i == 2 && z) {
                messageObject2 = this.currentMessageObject;
                if (messageObject2 != null && messageObject2.isVideo()) {
                    if (this.playerWasReady) {
                        this.setLoadingRunnable.run();
                    } else {
                        AndroidUtilities.runOnUIThread(this.setLoadingRunnable, 1000);
                    }
                }
            }
            if (!this.videoPlayer.isPlaying() || i == 4) {
                if (this.isPlaying) {
                    this.isPlaying = false;
                    this.videoPlayButton.setImageResource(NUM);
                    AndroidUtilities.cancelRunOnUIThread(this.updateProgressRunnable);
                    if (i == 4) {
                        if (!this.isCurrentVideo) {
                            if (!this.isActionBarVisible) {
                                toggleActionBar(true, true);
                            }
                            this.videoPlayerSeekbar.setProgress(0.0f);
                            this.videoPlayerControlFrameLayout.invalidate();
                            if (this.inPreview || this.videoTimelineView.getVisibility() != 0) {
                                this.videoPlayer.seekTo(0);
                            } else {
                                this.videoPlayer.seekTo((long) ((int) (this.videoTimelineView.getLeftProgress() * ((float) this.videoPlayer.getDuration()))));
                            }
                            this.videoPlayer.pause();
                        } else if (!this.videoTimelineView.isDragging()) {
                            this.videoTimelineView.setProgress(0.0f);
                            if (this.inPreview || this.videoTimelineView.getVisibility() != 0) {
                                this.videoPlayer.seekTo(0);
                            } else {
                                this.videoPlayer.seekTo((long) ((int) (this.videoTimelineView.getLeftProgress() * ((float) this.videoPlayer.getDuration()))));
                            }
                            this.videoPlayer.pause();
                            this.containerView.invalidate();
                        }
                        pipVideoView = this.pipVideoView;
                        if (pipVideoView != null) {
                            pipVideoView.onVideoCompleted();
                        }
                    }
                }
            } else if (!this.isPlaying) {
                this.isPlaying = true;
                this.videoPlayButton.setImageResource(NUM);
                AndroidUtilities.runOnUIThread(this.updateProgressRunnable);
            }
            pipVideoView = this.pipVideoView;
            if (pipVideoView != null) {
                pipVideoView.updatePlayButton();
            }
            updateVideoPlayerTime();
        }
    }

    private void preparePlayer(Uri uri, boolean z, boolean z2) {
        if (!z2) {
            this.currentPlayingVideoFile = uri;
        }
        if (this.parentActivity != null) {
            MessageObject messageObject;
            float f;
            int i = 0;
            this.streamingAlertShown = false;
            this.startedPlayTime = SystemClock.elapsedRealtime();
            this.currentVideoFinishedLoading = false;
            this.lastBufferedPositionCheck = 0;
            boolean z3 = true;
            this.firstAnimationDelay = true;
            this.inPreview = z2;
            releasePlayer(false);
            if (this.videoTextureView == null) {
                this.aspectRatioFrameLayout = new AspectRatioFrameLayout(this.parentActivity) {
                    /* Access modifiers changed, original: protected */
                    public void onMeasure(int i, int i2) {
                        super.onMeasure(i, i2);
                        if (PhotoViewer.this.textureImageView != null) {
                            ViewGroup.LayoutParams layoutParams = PhotoViewer.this.textureImageView.getLayoutParams();
                            layoutParams.width = getMeasuredWidth();
                            layoutParams.height = getMeasuredHeight();
                        }
                    }
                };
                this.aspectRatioFrameLayout.setVisibility(4);
                this.containerView.addView(this.aspectRatioFrameLayout, 0, LayoutHelper.createFrame(-1, -1, 17));
                this.videoTextureView = new TextureView(this.parentActivity);
                SurfaceTexture surfaceTexture = this.injectingVideoPlayerSurface;
                if (surfaceTexture != null) {
                    this.videoTextureView.setSurfaceTexture(surfaceTexture);
                    this.textureUploaded = true;
                    this.injectingVideoPlayerSurface = null;
                }
                this.videoTextureView.setPivotX(0.0f);
                this.videoTextureView.setPivotY(0.0f);
                this.videoTextureView.setOpaque(false);
                this.aspectRatioFrameLayout.addView(this.videoTextureView, LayoutHelper.createFrame(-1, -1, 17));
            }
            if (VERSION.SDK_INT >= 21 && this.textureImageView == null) {
                this.textureImageView = new ImageView(this.parentActivity);
                this.textureImageView.setBackgroundColor(-65536);
                this.textureImageView.setPivotX(0.0f);
                this.textureImageView.setPivotY(0.0f);
                this.textureImageView.setVisibility(4);
                this.containerView.addView(this.textureImageView);
            }
            this.textureUploaded = false;
            this.videoCrossfadeStarted = false;
            TextureView textureView = this.videoTextureView;
            this.videoCrossfadeAlpha = 0.0f;
            textureView.setAlpha(0.0f);
            this.videoPlayButton.setImageResource(NUM);
            this.playerWasReady = false;
            if (this.videoPlayer == null) {
                VideoPlayer videoPlayer = this.injectingVideoPlayer;
                if (videoPlayer != null) {
                    this.videoPlayer = videoPlayer;
                    this.injectingVideoPlayer = null;
                    this.playerInjected = true;
                    updatePlayerState(this.videoPlayer.getPlayWhenReady(), this.videoPlayer.getPlaybackState());
                    z3 = false;
                } else {
                    this.videoPlayer = new VideoPlayer();
                }
                this.videoPlayer.setTextureView(this.videoTextureView);
                this.videoPlayer.setDelegate(new VideoPlayerDelegate() {
                    public void onStateChanged(boolean z, int i) {
                        PhotoViewer.this.updatePlayerState(z, i);
                    }

                    public void onError(Exception exception) {
                        if (PhotoViewer.this.videoPlayer != null) {
                            FileLog.e((Throwable) exception);
                            if (PhotoViewer.this.menuItem.isSubItemVisible(11)) {
                                Builder builder = new Builder(PhotoViewer.this.parentActivity);
                                builder.setTitle(LocaleController.getString("AppName", NUM));
                                builder.setMessage(LocaleController.getString("CantPlayVideo", NUM));
                                builder.setPositiveButton(LocaleController.getString("Open", NUM), new -$$Lambda$PhotoViewer$30$SQyb3EGBh8Egj0kfWjglParH1CY(this));
                                builder.setNegativeButton(LocaleController.getString("Cancel", NUM), null);
                                PhotoViewer.this.showAlertDialog(builder);
                            }
                        }
                    }

                    public /* synthetic */ void lambda$onError$0$PhotoViewer$30(DialogInterface dialogInterface, int i) {
                        try {
                            AndroidUtilities.openForView(PhotoViewer.this.currentMessageObject, PhotoViewer.this.parentActivity);
                            PhotoViewer.this.closePhoto(false, false);
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }

                    public void onVideoSizeChanged(int i, int i2, int i3, float f) {
                        if (PhotoViewer.this.aspectRatioFrameLayout != null) {
                            if (!(i3 == 90 || i3 == 270)) {
                                int i4 = i2;
                                i2 = i;
                                i = i4;
                            }
                            PhotoViewer.this.aspectRatioFrameLayout.setAspectRatio(i == 0 ? 1.0f : (((float) i2) * f) / ((float) i), i3);
                        }
                    }

                    public void onRenderedFirstFrame() {
                        if (!PhotoViewer.this.textureUploaded) {
                            PhotoViewer.this.textureUploaded = true;
                            PhotoViewer.this.containerView.invalidate();
                        }
                    }

                    public boolean onSurfaceDestroyed(SurfaceTexture surfaceTexture) {
                        if (PhotoViewer.this.changingTextureView) {
                            PhotoViewer.this.changingTextureView = false;
                            if (PhotoViewer.this.isInline) {
                                if (PhotoViewer.this.isInline) {
                                    PhotoViewer.this.waitingForFirstTextureUpload = 1;
                                }
                                PhotoViewer.this.changedTextureView.setSurfaceTexture(surfaceTexture);
                                PhotoViewer.this.changedTextureView.setSurfaceTextureListener(PhotoViewer.this.surfaceTextureListener);
                                PhotoViewer.this.changedTextureView.setVisibility(0);
                                return true;
                            }
                        }
                        return false;
                    }

                    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                        if (PhotoViewer.this.waitingForFirstTextureUpload == 2) {
                            if (PhotoViewer.this.textureImageView != null) {
                                PhotoViewer.this.textureImageView.setVisibility(4);
                                PhotoViewer.this.textureImageView.setImageDrawable(null);
                                if (PhotoViewer.this.currentBitmap != null) {
                                    PhotoViewer.this.currentBitmap.recycle();
                                    PhotoViewer.this.currentBitmap = null;
                                }
                            }
                            PhotoViewer.this.switchingInlineMode = false;
                            if (VERSION.SDK_INT >= 21) {
                                PhotoViewer.this.aspectRatioFrameLayout.getLocationInWindow(PhotoViewer.this.pipPosition);
                                int[] access$11500 = PhotoViewer.this.pipPosition;
                                access$11500[1] = (int) (((float) access$11500[1]) - PhotoViewer.this.containerView.getTranslationY());
                                PhotoViewer.this.textureImageView.setTranslationX(PhotoViewer.this.textureImageView.getTranslationX() + ((float) PhotoViewer.this.getLeftInset()));
                                PhotoViewer.this.videoTextureView.setTranslationX((PhotoViewer.this.videoTextureView.getTranslationX() + ((float) PhotoViewer.this.getLeftInset())) - PhotoViewer.this.aspectRatioFrameLayout.getX());
                                AnimatorSet animatorSet = new AnimatorSet();
                                Animator[] animatorArr = new Animator[13];
                                animatorArr[0] = ObjectAnimator.ofFloat(PhotoViewer.this.textureImageView, View.SCALE_X, new float[]{1.0f});
                                animatorArr[1] = ObjectAnimator.ofFloat(PhotoViewer.this.textureImageView, View.SCALE_Y, new float[]{1.0f});
                                animatorArr[2] = ObjectAnimator.ofFloat(PhotoViewer.this.textureImageView, View.TRANSLATION_X, new float[]{(float) PhotoViewer.this.pipPosition[0]});
                                animatorArr[3] = ObjectAnimator.ofFloat(PhotoViewer.this.textureImageView, View.TRANSLATION_Y, new float[]{(float) PhotoViewer.this.pipPosition[1]});
                                animatorArr[4] = ObjectAnimator.ofFloat(PhotoViewer.this.videoTextureView, View.SCALE_X, new float[]{1.0f});
                                animatorArr[5] = ObjectAnimator.ofFloat(PhotoViewer.this.videoTextureView, View.SCALE_Y, new float[]{1.0f});
                                animatorArr[6] = ObjectAnimator.ofFloat(PhotoViewer.this.videoTextureView, View.TRANSLATION_X, new float[]{((float) PhotoViewer.this.pipPosition[0]) - PhotoViewer.this.aspectRatioFrameLayout.getX()});
                                animatorArr[7] = ObjectAnimator.ofFloat(PhotoViewer.this.videoTextureView, View.TRANSLATION_Y, new float[]{((float) PhotoViewer.this.pipPosition[1]) - PhotoViewer.this.aspectRatioFrameLayout.getY()});
                                animatorArr[8] = ObjectAnimator.ofInt(PhotoViewer.this.backgroundDrawable, AnimationProperties.COLOR_DRAWABLE_ALPHA, new int[]{255});
                                animatorArr[9] = ObjectAnimator.ofFloat(PhotoViewer.this.actionBar, View.ALPHA, new float[]{1.0f});
                                animatorArr[10] = ObjectAnimator.ofFloat(PhotoViewer.this.bottomLayout, View.ALPHA, new float[]{1.0f});
                                animatorArr[11] = ObjectAnimator.ofFloat(PhotoViewer.this.captionTextView, View.ALPHA, new float[]{1.0f});
                                animatorArr[12] = ObjectAnimator.ofFloat(PhotoViewer.this.groupedPhotosListView, View.ALPHA, new float[]{1.0f});
                                animatorSet.playTogether(animatorArr);
                                animatorSet.setInterpolator(new DecelerateInterpolator());
                                animatorSet.setDuration(250);
                                animatorSet.addListener(new AnimatorListenerAdapter() {
                                    public void onAnimationEnd(Animator animator) {
                                        PhotoViewer.this.pipAnimationInProgress = false;
                                    }
                                });
                                animatorSet.start();
                            }
                            PhotoViewer.this.waitingForFirstTextureUpload = 0;
                        }
                    }
                });
            } else {
                z3 = false;
            }
            this.shouldSavePositionForCurrentVideo = null;
            this.lastSaveTime = 0;
            if (z3) {
                this.seekToProgressPending = this.seekToProgressPending2;
                this.videoPlayer.preparePlayer(uri, "other");
                this.videoPlayerSeekbar.setProgress(0.0f);
                this.videoTimelineView.setProgress(0.0f);
                this.videoPlayerSeekbar.setBufferedProgress(0.0f);
                this.videoPlayer.setPlayWhenReady(z);
                messageObject = this.currentMessageObject;
                if (messageObject != null) {
                    String fileName = messageObject.getFileName();
                    if (!TextUtils.isEmpty(fileName) && this.currentMessageObject.getDuration() >= 1200) {
                        f = ApplicationLoader.applicationContext.getSharedPreferences("media_saved_pos", 0).getFloat(fileName, -1.0f);
                        if (f > 0.0f && f < 0.999f) {
                            this.currentMessageObject.forceSeekTo = f;
                            this.videoPlayerSeekbar.setProgress(f);
                        }
                        this.shouldSavePositionForCurrentVideo = fileName;
                    }
                }
            }
            messageObject = this.currentMessageObject;
            if (messageObject != null) {
                f = messageObject.forceSeekTo;
                if (f >= 0.0f) {
                    this.seekToProgressPending = f;
                    messageObject.forceSeekTo = -1.0f;
                }
            }
            BotInlineResult botInlineResult = this.currentBotInlineResult;
            if (botInlineResult == null || !(botInlineResult.type.equals("video") || MessageObject.isVideoDocument(this.currentBotInlineResult.document))) {
                this.bottomLayout.setPadding(0, 0, 0, 0);
            } else {
                this.bottomLayout.setVisibility(0);
                this.bottomLayout.setPadding(0, 0, AndroidUtilities.dp(84.0f), 0);
                this.pickerView.setVisibility(8);
            }
            FrameLayout frameLayout = this.videoPlayerControlFrameLayout;
            if (this.isCurrentVideo) {
                i = 8;
            }
            frameLayout.setVisibility(i);
            this.dateTextView.setVisibility(8);
            this.nameTextView.setVisibility(8);
            if (this.allowShare) {
                this.shareButton.setVisibility(8);
                this.menuItem.showSubItem(10);
            }
            this.inPreview = z2;
            updateAccessibilityOverlayVisibility();
        }
    }

    private void releasePlayer(boolean z) {
        if (this.videoPlayer != null) {
            AndroidUtilities.cancelRunOnUIThread(this.setLoadingRunnable);
            this.videoPlayer.releasePlayer(true);
            this.videoPlayer = null;
            updateAccessibilityOverlayVisibility();
        }
        this.videoPreviewFrame.close();
        toggleMiniProgress(false, false);
        this.pipAvailable = false;
        this.playerInjected = false;
        if (this.pipItem.isEnabled()) {
            this.pipItem.setEnabled(false);
            this.pipItem.setAlpha(0.5f);
        }
        if (this.keepScreenOnFlagSet) {
            try {
                this.parentActivity.getWindow().clearFlags(128);
                this.keepScreenOnFlagSet = false;
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        AspectRatioFrameLayout aspectRatioFrameLayout = this.aspectRatioFrameLayout;
        if (aspectRatioFrameLayout != null) {
            try {
                this.containerView.removeView(aspectRatioFrameLayout);
            } catch (Throwable unused) {
            }
            this.aspectRatioFrameLayout = null;
        }
        if (this.videoTextureView != null) {
            this.videoTextureView = null;
        }
        if (this.isPlaying) {
            this.isPlaying = false;
            if (!z) {
                this.videoPlayButton.setImageResource(NUM);
            }
            AndroidUtilities.cancelRunOnUIThread(this.updateProgressRunnable);
        }
        if (!z && !this.inPreview && !this.requestingPreview) {
            this.videoPlayerControlFrameLayout.setVisibility(8);
            this.dateTextView.setVisibility(0);
            this.nameTextView.setVisibility(0);
            if (this.allowShare) {
                this.shareButton.setVisibility(0);
                this.menuItem.hideSubItem(10);
            }
        }
    }

    private void updateCaptionTextForCurrentPhoto(Object obj) {
        CharSequence charSequence = obj instanceof PhotoEntry ? ((PhotoEntry) obj).caption : (!(obj instanceof BotInlineResult) && (obj instanceof SearchImage)) ? ((SearchImage) obj).caption : null;
        if (TextUtils.isEmpty(charSequence)) {
            this.captionEditText.setFieldText("");
        } else {
            this.captionEditText.setFieldText(charSequence);
        }
        this.captionEditText.setAllowTextEntitiesIntersection(supportsSendingNewEntities());
    }

    public void showAlertDialog(Builder builder) {
        if (this.parentActivity != null) {
            try {
                if (this.visibleDialog != null) {
                    this.visibleDialog.dismiss();
                    this.visibleDialog = null;
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            try {
                this.visibleDialog = builder.show();
                this.visibleDialog.setCanceledOnTouchOutside(true);
                this.visibleDialog.setOnDismissListener(new -$$Lambda$PhotoViewer$lnLCiWQ1DH7lBk3sKhRBJozSzVo(this));
            } catch (Exception e2) {
                FileLog.e(e2);
            }
        }
    }

    public /* synthetic */ void lambda$showAlertDialog$36$PhotoViewer(DialogInterface dialogInterface) {
        this.visibleDialog = null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:72:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0045  */
    private void applyCurrentEditMode() {
        /*
        r19 = this;
        r0 = r19;
        r1 = r0.currentEditMode;
        r2 = 3;
        r4 = 2;
        r6 = 1;
        if (r1 == r6) goto L_0x0039;
    L_0x0009:
        if (r1 != 0) goto L_0x0010;
    L_0x000b:
        r1 = r0.sendPhotoType;
        if (r1 != r6) goto L_0x0010;
    L_0x000f:
        goto L_0x0039;
    L_0x0010:
        r1 = r0.currentEditMode;
        if (r1 != r4) goto L_0x0023;
    L_0x0014:
        r1 = r0.photoFilterView;
        r1 = r1.getBitmap();
        r7 = r0.photoFilterView;
        r7 = r7.getSavedFilterState();
        r15 = r7;
        r14 = 0;
        goto L_0x0036;
    L_0x0023:
        if (r1 != r2) goto L_0x0033;
    L_0x0025:
        r1 = r0.photoPaintView;
        r1 = r1.getBitmap();
        r7 = r0.photoPaintView;
        r7 = r7.getMasks();
        r14 = r7;
        goto L_0x0040;
    L_0x0033:
        r1 = 0;
        r14 = 0;
        r15 = 0;
    L_0x0036:
        r16 = 0;
        goto L_0x0043;
    L_0x0039:
        r1 = r0.photoCropView;
        r1 = r1.getBitmap();
        r14 = 0;
    L_0x0040:
        r15 = 0;
        r16 = 1;
    L_0x0043:
        if (r1 == 0) goto L_0x0205;
    L_0x0045:
        r7 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        r8 = (float) r7;
        r7 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        r9 = (float) r7;
        r10 = 80;
        r11 = 0;
        r12 = 101; // 0x65 float:1.42E-43 double:5.0E-322;
        r13 = 101; // 0x65 float:1.42E-43 double:5.0E-322;
        r7 = r1;
        r7 = org.telegram.messenger.ImageLoader.scaleAndSaveImage(r7, r8, r9, r10, r11, r12, r13);
        if (r7 == 0) goto L_0x0205;
    L_0x005d:
        r8 = r0.imagesArrLocals;
        r9 = r0.currentIndex;
        r8 = r8.get(r9);
        r9 = r8 instanceof org.telegram.messenger.MediaController.PhotoEntry;
        r10 = NUM; // 0x42var_ float:120.0 double:5.548480205E-315;
        r13 = -12734994; // 0xffffffffff3dadee float:-2.5212719E38 double:NaN;
        if (r9 == 0) goto L_0x00ed;
    L_0x006e:
        r12 = r8;
        r12 = (org.telegram.messenger.MediaController.PhotoEntry) r12;
        r7 = org.telegram.messenger.FileLoader.getPathToAttach(r7, r6);
        r7 = r7.toString();
        r12.imagePath = r7;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r8 = (float) r7;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r9 = (float) r7;
        r10 = 70;
        r11 = 0;
        r17 = 101; // 0x65 float:1.42E-43 double:5.0E-322;
        r18 = 101; // 0x65 float:1.42E-43 double:5.0E-322;
        r7 = r1;
        r3 = r12;
        r12 = r17;
        r5 = -12734994; // 0xffffffffff3dadee float:-2.5212719E38 double:NaN;
        r13 = r18;
        r7 = org.telegram.messenger.ImageLoader.scaleAndSaveImage(r7, r8, r9, r10, r11, r12, r13);
        if (r7 == 0) goto L_0x00a5;
    L_0x009b:
        r7 = org.telegram.messenger.FileLoader.getPathToAttach(r7, r6);
        r7 = r7.toString();
        r3.thumbPath = r7;
    L_0x00a5:
        if (r14 == 0) goto L_0x00ac;
    L_0x00a7:
        r7 = r3.stickers;
        r7.addAll(r14);
    L_0x00ac:
        r7 = r0.currentEditMode;
        if (r7 != r6) goto L_0x00bf;
    L_0x00b0:
        r2 = r0.cropItem;
        r7 = new android.graphics.PorterDuffColorFilter;
        r8 = android.graphics.PorterDuff.Mode.MULTIPLY;
        r7.<init>(r5, r8);
        r2.setColorFilter(r7);
        r3.isCropped = r6;
        goto L_0x00e0;
    L_0x00bf:
        if (r7 != r4) goto L_0x00d0;
    L_0x00c1:
        r2 = r0.tuneItem;
        r7 = new android.graphics.PorterDuffColorFilter;
        r8 = android.graphics.PorterDuff.Mode.MULTIPLY;
        r7.<init>(r5, r8);
        r2.setColorFilter(r7);
        r3.isFiltered = r6;
        goto L_0x00e0;
    L_0x00d0:
        if (r7 != r2) goto L_0x00e0;
    L_0x00d2:
        r2 = r0.paintItem;
        r7 = new android.graphics.PorterDuffColorFilter;
        r8 = android.graphics.PorterDuff.Mode.MULTIPLY;
        r7.<init>(r5, r8);
        r2.setColorFilter(r7);
        r3.isPainted = r6;
    L_0x00e0:
        if (r15 == 0) goto L_0x00e6;
    L_0x00e2:
        r3.savedFilterState = r15;
        goto L_0x0168;
    L_0x00e6:
        if (r16 == 0) goto L_0x0168;
    L_0x00e8:
        r2 = 0;
        r3.savedFilterState = r2;
        goto L_0x0168;
    L_0x00ed:
        r5 = -12734994; // 0xffffffffff3dadee float:-2.5212719E38 double:NaN;
        r3 = r8 instanceof org.telegram.messenger.MediaController.SearchImage;
        if (r3 == 0) goto L_0x0168;
    L_0x00f4:
        r3 = r8;
        r3 = (org.telegram.messenger.MediaController.SearchImage) r3;
        r7 = org.telegram.messenger.FileLoader.getPathToAttach(r7, r6);
        r7 = r7.toString();
        r3.imagePath = r7;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r8 = (float) r7;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r10);
        r9 = (float) r7;
        r10 = 70;
        r11 = 0;
        r12 = 101; // 0x65 float:1.42E-43 double:5.0E-322;
        r13 = 101; // 0x65 float:1.42E-43 double:5.0E-322;
        r7 = r1;
        r7 = org.telegram.messenger.ImageLoader.scaleAndSaveImage(r7, r8, r9, r10, r11, r12, r13);
        if (r7 == 0) goto L_0x0123;
    L_0x0119:
        r7 = org.telegram.messenger.FileLoader.getPathToAttach(r7, r6);
        r7 = r7.toString();
        r3.thumbPath = r7;
    L_0x0123:
        if (r14 == 0) goto L_0x012a;
    L_0x0125:
        r7 = r3.stickers;
        r7.addAll(r14);
    L_0x012a:
        r7 = r0.currentEditMode;
        if (r7 != r6) goto L_0x013d;
    L_0x012e:
        r2 = r0.cropItem;
        r7 = new android.graphics.PorterDuffColorFilter;
        r8 = android.graphics.PorterDuff.Mode.MULTIPLY;
        r7.<init>(r5, r8);
        r2.setColorFilter(r7);
        r3.isCropped = r6;
        goto L_0x015e;
    L_0x013d:
        if (r7 != r4) goto L_0x014e;
    L_0x013f:
        r2 = r0.tuneItem;
        r7 = new android.graphics.PorterDuffColorFilter;
        r8 = android.graphics.PorterDuff.Mode.MULTIPLY;
        r7.<init>(r5, r8);
        r2.setColorFilter(r7);
        r3.isFiltered = r6;
        goto L_0x015e;
    L_0x014e:
        if (r7 != r2) goto L_0x015e;
    L_0x0150:
        r2 = r0.paintItem;
        r7 = new android.graphics.PorterDuffColorFilter;
        r8 = android.graphics.PorterDuff.Mode.MULTIPLY;
        r7.<init>(r5, r8);
        r2.setColorFilter(r7);
        r3.isPainted = r6;
    L_0x015e:
        if (r15 == 0) goto L_0x0163;
    L_0x0160:
        r3.savedFilterState = r15;
        goto L_0x0168;
    L_0x0163:
        if (r16 == 0) goto L_0x0168;
    L_0x0165:
        r2 = 0;
        r3.savedFilterState = r2;
    L_0x0168:
        r2 = r0.sendPhotoType;
        if (r2 == 0) goto L_0x016f;
    L_0x016c:
        r3 = 4;
        if (r2 != r3) goto L_0x0185;
    L_0x016f:
        r2 = r0.placeProvider;
        if (r2 == 0) goto L_0x0185;
    L_0x0173:
        r3 = r0.currentIndex;
        r2.updatePhotoAtIndex(r3);
        r2 = r0.placeProvider;
        r3 = r0.currentIndex;
        r2 = r2.isPhotoChecked(r3);
        if (r2 != 0) goto L_0x0185;
    L_0x0182:
        r19.setPhotoChecked();
    L_0x0185:
        r2 = r0.currentEditMode;
        if (r2 != r6) goto L_0x01e2;
    L_0x0189:
        r2 = r0.photoCropView;
        r2 = r2.getRectSizeX();
        r3 = r19.getContainerViewWidth();
        r3 = (float) r3;
        r2 = r2 / r3;
        r3 = r0.photoCropView;
        r3 = r3.getRectSizeY();
        r5 = r19.getContainerViewHeight();
        r5 = (float) r5;
        r3 = r3 / r5;
        r5 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1));
        if (r5 <= 0) goto L_0x01a6;
    L_0x01a5:
        goto L_0x01a7;
    L_0x01a6:
        r2 = r3;
    L_0x01a7:
        r0.scale = r2;
        r2 = r0.photoCropView;
        r2 = r2.getRectX();
        r3 = r0.photoCropView;
        r3 = r3.getRectSizeX();
        r5 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r3 = r3 / r5;
        r2 = r2 + r3;
        r3 = r19.getContainerViewWidth();
        r3 = r3 / r4;
        r3 = (float) r3;
        r2 = r2 - r3;
        r0.translationX = r2;
        r2 = r0.photoCropView;
        r2 = r2.getRectY();
        r3 = r0.photoCropView;
        r3 = r3.getRectSizeY();
        r3 = r3 / r5;
        r2 = r2 + r3;
        r3 = r19.getContainerViewHeight();
        r3 = r3 / r4;
        r3 = (float) r3;
        r2 = r2 - r3;
        r0.translationY = r2;
        r0.zoomAnimation = r6;
        r0.applying = r6;
        r2 = r0.photoCropView;
        r2.onDisappear();
    L_0x01e2:
        r2 = r0.centerImage;
        r3 = 0;
        r2.setParentView(r3);
        r2 = r0.centerImage;
        r3 = 0;
        r2.setOrientation(r3, r6);
        r0.ignoreDidSetImage = r6;
        r2 = r0.centerImage;
        r2.setImageBitmap(r1);
        r0.ignoreDidSetImage = r3;
        r1 = r0.centerImage;
        r2 = r0.containerView;
        r1.setParentView(r2);
        r1 = r0.sendPhotoType;
        if (r1 != r6) goto L_0x0205;
    L_0x0202:
        r19.setCropBitmap();
    L_0x0205:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.applyCurrentEditMode():void");
    }

    private void setPhotoChecked() {
        PhotoViewerProvider photoViewerProvider = this.placeProvider;
        if (photoViewerProvider != null) {
            if (photoViewerProvider.getSelectedPhotos() == null || this.maxSelectedPhotos <= 0 || this.placeProvider.getSelectedPhotos().size() < this.maxSelectedPhotos || this.placeProvider.isPhotoChecked(this.currentIndex)) {
                int photoChecked = this.placeProvider.setPhotoChecked(this.currentIndex, getCurrentVideoEditedInfo());
                boolean isPhotoChecked = this.placeProvider.isPhotoChecked(this.currentIndex);
                this.checkImageView.setChecked(isPhotoChecked, true);
                if (photoChecked >= 0) {
                    if (isPhotoChecked) {
                        this.selectedPhotosAdapter.notifyItemInserted(photoChecked);
                        this.selectedPhotosListView.smoothScrollToPosition(photoChecked);
                    } else {
                        this.selectedPhotosAdapter.notifyItemRemoved(photoChecked);
                    }
                }
                updateSelectedCount();
            } else {
                if (this.allowOrder) {
                    ChatActivity chatActivity = this.parentChatActivity;
                    if (chatActivity != null) {
                        Chat currentChat = chatActivity.getCurrentChat();
                        if (!(currentChat == null || ChatObject.hasAdminRights(currentChat) || !currentChat.slowmode_enabled)) {
                            AlertsCreator.createSimpleAlert(this.parentActivity, LocaleController.getString("Slowmode", NUM), LocaleController.getString("SlowmodeSelectSendError", NUM)).show();
                        }
                    }
                }
            }
        }
    }

    private void createCropView() {
        if (this.photoCropView == null) {
            this.photoCropView = new PhotoCropView(this.actvityContext);
            this.photoCropView.setVisibility(8);
            this.containerView.addView(this.photoCropView, this.containerView.indexOfChild(this.pickerViewSendButton) - 1, LayoutHelper.createFrame(-1, -1.0f, 51, 0.0f, 0.0f, 0.0f, 48.0f));
            this.photoCropView.setDelegate(new -$$Lambda$PhotoViewer$b8lXPxl6X4UfdpKy0ueyazB_CW0(this));
        }
    }

    public /* synthetic */ void lambda$createCropView$37$PhotoViewer(boolean z) {
        this.resetButton.setVisibility(z ? 8 : 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:80:0x02f3  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x02e6  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x02e6  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x02f3  */
    private void switchToEditMode(int r18) {
        /*
        r17 = this;
        r0 = r17;
        r1 = r18;
        r2 = r0.currentEditMode;
        if (r2 == r1) goto L_0x0557;
    L_0x0008:
        r2 = r0.centerImage;
        r2 = r2.getBitmap();
        if (r2 == 0) goto L_0x0557;
    L_0x0010:
        r2 = r0.changeModeAnimation;
        if (r2 != 0) goto L_0x0557;
    L_0x0014:
        r2 = r0.imageMoveAnimation;
        if (r2 != 0) goto L_0x0557;
    L_0x0018:
        r2 = r0.photoProgressViews;
        r3 = 0;
        r2 = r2[r3];
        r2 = r2.backgroundState;
        r4 = -1;
        if (r2 != r4) goto L_0x0557;
    L_0x0024:
        r2 = r0.captionEditText;
        r2 = r2.getTag();
        if (r2 == 0) goto L_0x002e;
    L_0x002c:
        goto L_0x0557;
    L_0x002e:
        r2 = NUM; // 0x433a0000 float:186.0 double:5.57244073E-315;
        r5 = 3;
        r6 = NUM; // 0x42fCLASSNAME float:126.0 double:5.552365696E-315;
        r9 = 4;
        r10 = 0;
        r11 = 1;
        r12 = 2;
        if (r1 != 0) goto L_0x019f;
    L_0x0039:
        r4 = r0.centerImage;
        r4 = r4.getBitmap();
        if (r4 == 0) goto L_0x00c9;
    L_0x0041:
        r4 = r0.centerImage;
        r4 = r4.getBitmapWidth();
        r13 = r0.centerImage;
        r13 = r13.getBitmapHeight();
        r14 = r17.getContainerViewWidth();
        r14 = (float) r14;
        r4 = (float) r4;
        r14 = r14 / r4;
        r15 = r17.getContainerViewHeight();
        r15 = (float) r15;
        r13 = (float) r13;
        r15 = r15 / r13;
        r7 = r0.getContainerViewWidth(r3);
        r7 = (float) r7;
        r7 = r7 / r4;
        r4 = r0.getContainerViewHeight(r3);
        r4 = (float) r4;
        r4 = r4 / r13;
        r8 = (r14 > r15 ? 1 : (r14 == r15 ? 0 : -1));
        if (r8 <= 0) goto L_0x006c;
    L_0x006b:
        r14 = r15;
    L_0x006c:
        r8 = (r7 > r4 ? 1 : (r7 == r4 ? 0 : -1));
        if (r8 <= 0) goto L_0x0071;
    L_0x0070:
        goto L_0x0072;
    L_0x0071:
        r4 = r7;
    L_0x0072:
        r7 = r0.sendPhotoType;
        if (r7 != r11) goto L_0x007a;
    L_0x0076:
        r0.setCropTranslations(r11);
        goto L_0x00c9;
    L_0x007a:
        r4 = r4 / r14;
        r0.animateToScale = r4;
        r0.animateToX = r10;
        r4 = r17.getLeftInset();
        r4 = r4 / r12;
        r7 = r17.getRightInset();
        r7 = r7 / r12;
        r4 = r4 - r7;
        r4 = (float) r4;
        r0.translationX = r4;
        r4 = r0.currentEditMode;
        if (r4 != r11) goto L_0x009b;
    L_0x0091:
        r4 = NUM; // 0x42680000 float:58.0 double:5.50444465E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = (float) r4;
        r0.animateToY = r4;
        goto L_0x00b2;
    L_0x009b:
        if (r4 != r12) goto L_0x00a7;
    L_0x009d:
        r4 = NUM; // 0x42b80000 float:92.0 double:5.530347917E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = (float) r4;
        r0.animateToY = r4;
        goto L_0x00b2;
    L_0x00a7:
        if (r4 != r5) goto L_0x00b2;
    L_0x00a9:
        r4 = NUM; // 0x42300000 float:44.0 double:5.48631236E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r4 = (float) r4;
        r0.animateToY = r4;
    L_0x00b2:
        r4 = android.os.Build.VERSION.SDK_INT;
        r7 = 21;
        if (r4 < r7) goto L_0x00c1;
    L_0x00b8:
        r4 = r0.animateToY;
        r7 = org.telegram.messenger.AndroidUtilities.statusBarHeight;
        r7 = r7 / r12;
        r7 = (float) r7;
        r4 = r4 - r7;
        r0.animateToY = r4;
    L_0x00c1:
        r7 = java.lang.System.currentTimeMillis();
        r0.animationStartTime = r7;
        r0.zoomAnimation = r11;
    L_0x00c9:
        r0.padImageForHorizontalInsets = r3;
        r4 = new android.animation.AnimatorSet;
        r4.<init>();
        r0.imageMoveAnimation = r4;
        r4 = new java.util.ArrayList;
        r4.<init>(r9);
        r7 = r0.currentEditMode;
        if (r7 != r11) goto L_0x010f;
    L_0x00db:
        r2 = r0.editorDoneLayout;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r11];
        r7 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r7 = (float) r7;
        r6[r3] = r7;
        r2 = android.animation.ObjectAnimator.ofFloat(r2, r5, r6);
        r4.add(r2);
        r2 = org.telegram.ui.Components.AnimationProperties.PHOTO_VIEWER_ANIMATION_VALUE;
        r5 = new float[r12];
        r5 = {0, NUM};
        r2 = android.animation.ObjectAnimator.ofFloat(r0, r2, r5);
        r4.add(r2);
        r2 = r0.photoCropView;
        r5 = android.view.View.ALPHA;
        r6 = new float[r11];
        r6[r3] = r10;
        r2 = android.animation.ObjectAnimator.ofFloat(r2, r5, r6);
        r4.add(r2);
        goto L_0x0182;
    L_0x010f:
        if (r7 != r12) goto L_0x013d;
    L_0x0111:
        r5 = r0.photoFilterView;
        r5.shutdown();
        r5 = r0.photoFilterView;
        r5 = r5.getToolsView();
        r6 = android.view.View.TRANSLATION_Y;
        r7 = new float[r11];
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = (float) r2;
        r7[r3] = r2;
        r2 = android.animation.ObjectAnimator.ofFloat(r5, r6, r7);
        r4.add(r2);
        r2 = org.telegram.ui.Components.AnimationProperties.PHOTO_VIEWER_ANIMATION_VALUE;
        r3 = new float[r12];
        r3 = {0, NUM};
        r2 = android.animation.ObjectAnimator.ofFloat(r0, r2, r3);
        r4.add(r2);
        goto L_0x0182;
    L_0x013d:
        if (r7 != r5) goto L_0x0182;
    L_0x013f:
        r2 = r0.photoPaintView;
        r2.shutdown();
        r2 = r0.photoPaintView;
        r2 = r2.getToolsView();
        r5 = android.view.View.TRANSLATION_Y;
        r7 = new float[r11];
        r8 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r8 = (float) r8;
        r7[r3] = r8;
        r2 = android.animation.ObjectAnimator.ofFloat(r2, r5, r7);
        r4.add(r2);
        r2 = r0.photoPaintView;
        r2 = r2.getColorPicker();
        r5 = android.view.View.TRANSLATION_Y;
        r7 = new float[r11];
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r6 = (float) r6;
        r7[r3] = r6;
        r2 = android.animation.ObjectAnimator.ofFloat(r2, r5, r7);
        r4.add(r2);
        r2 = org.telegram.ui.Components.AnimationProperties.PHOTO_VIEWER_ANIMATION_VALUE;
        r3 = new float[r12];
        r3 = {0, NUM};
        r2 = android.animation.ObjectAnimator.ofFloat(r0, r2, r3);
        r4.add(r2);
    L_0x0182:
        r2 = r0.imageMoveAnimation;
        r2.playTogether(r4);
        r2 = r0.imageMoveAnimation;
        r3 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r2.setDuration(r3);
        r2 = r0.imageMoveAnimation;
        r3 = new org.telegram.ui.PhotoViewer$31;
        r3.<init>(r1);
        r2.addListener(r3);
        r1 = r0.imageMoveAnimation;
        r1.start();
        goto L_0x0557;
    L_0x019f:
        r7 = NUM; // 0x42CLASSNAME float:96.0 double:5.532938244E-315;
        if (r1 != r11) goto L_0x02a1;
    L_0x01a3:
        r17.createCropView();
        r2 = r0.photoCropView;
        r2.onAppear();
        r2 = r0.editorDoneLayout;
        r2 = r2.doneButton;
        r4 = NUM; // 0x7f0e032f float:1.887669E38 double:1.0531625593E-314;
        r5 = "Crop";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        r2.setText(r4);
        r2 = r0.editorDoneLayout;
        r2 = r2.doneButton;
        r4 = -11420173; // 0xfffffffffvar_bdf3 float:-2.7879492E38 double:NaN;
        r2.setTextColor(r4);
        r2 = new android.animation.AnimatorSet;
        r2.<init>();
        r0.changeModeAnimation = r2;
        r2 = new java.util.ArrayList;
        r2.<init>();
        r4 = r0.pickerView;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r12];
        r6[r3] = r10;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r8 = (float) r8;
        r6[r11] = r8;
        r4 = android.animation.ObjectAnimator.ofFloat(r4, r5, r6);
        r2.add(r4);
        r4 = r0.pickerViewSendButton;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r12];
        r6[r3] = r10;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r8 = (float) r8;
        r6[r11] = r8;
        r4 = android.animation.ObjectAnimator.ofFloat(r4, r5, r6);
        r2.add(r4);
        r4 = r0.actionBar;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r12];
        r6[r3] = r10;
        r8 = r4.getHeight();
        r8 = -r8;
        r8 = (float) r8;
        r6[r11] = r8;
        r4 = android.animation.ObjectAnimator.ofFloat(r4, r5, r6);
        r2.add(r4);
        r4 = r0.needCaptionLayout;
        if (r4 == 0) goto L_0x022e;
    L_0x0218:
        r4 = r0.captionTextView;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r12];
        r6[r3] = r10;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r3 = (float) r3;
        r6[r11] = r3;
        r3 = android.animation.ObjectAnimator.ofFloat(r4, r5, r6);
        r2.add(r3);
    L_0x022e:
        r3 = r0.sendPhotoType;
        if (r3 == 0) goto L_0x0234;
    L_0x0232:
        if (r3 != r9) goto L_0x0254;
    L_0x0234:
        r3 = r0.checkImageView;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
        r3 = r0.photosCounterView;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
    L_0x0254:
        r3 = r0.selectedPhotosListView;
        r3 = r3.getVisibility();
        if (r3 != 0) goto L_0x026c;
    L_0x025c:
        r3 = r0.selectedPhotosListView;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
    L_0x026c:
        r3 = r0.cameraItem;
        r3 = r3.getTag();
        if (r3 == 0) goto L_0x0284;
    L_0x0274:
        r3 = r0.cameraItem;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
    L_0x0284:
        r3 = r0.changeModeAnimation;
        r3.playTogether(r2);
        r2 = r0.changeModeAnimation;
        r3 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r2.setDuration(r3);
        r2 = r0.changeModeAnimation;
        r3 = new org.telegram.ui.PhotoViewer$32;
        r3.<init>(r1);
        r2.addListener(r3);
        r1 = r0.changeModeAnimation;
        r1.start();
        goto L_0x0557;
    L_0x02a1:
        r8 = -NUM; // 0xffffffffbvar_ float:-1.0 double:NaN;
        if (r1 != r12) goto L_0x040b;
    L_0x02a5:
        r5 = r0.photoFilterView;
        if (r5 != 0) goto L_0x0335;
    L_0x02a9:
        r5 = r0.imagesArrLocals;
        r5 = r5.isEmpty();
        r6 = 0;
        if (r5 != 0) goto L_0x02e2;
    L_0x02b2:
        r5 = r0.imagesArrLocals;
        r13 = r0.currentIndex;
        r5 = r5.get(r13);
        r13 = r5 instanceof org.telegram.messenger.MediaController.PhotoEntry;
        if (r13 == 0) goto L_0x02d7;
    L_0x02be:
        r5 = (org.telegram.messenger.MediaController.PhotoEntry) r5;
        r13 = r5.imagePath;
        if (r13 != 0) goto L_0x02ce;
    L_0x02c4:
        r6 = r5.path;
        r13 = r5.savedFilterState;
        r16 = r13;
        r13 = r6;
        r6 = r16;
        goto L_0x02cf;
    L_0x02ce:
        r13 = r6;
    L_0x02cf:
        r5 = r5.orientation;
        r16 = r13;
        r13 = r5;
        r5 = r16;
        goto L_0x02e4;
    L_0x02d7:
        r13 = r5 instanceof org.telegram.messenger.MediaController.SearchImage;
        if (r13 == 0) goto L_0x02e2;
    L_0x02db:
        r5 = (org.telegram.messenger.MediaController.SearchImage) r5;
        r6 = r5.savedFilterState;
        r5 = r5.imageUrl;
        goto L_0x02e3;
    L_0x02e2:
        r5 = r6;
    L_0x02e3:
        r13 = 0;
    L_0x02e4:
        if (r6 != 0) goto L_0x02f3;
    L_0x02e6:
        r5 = r0.centerImage;
        r5 = r5.getBitmap();
        r13 = r0.centerImage;
        r13 = r13.getOrientation();
        goto L_0x02f7;
    L_0x02f3:
        r5 = android.graphics.BitmapFactory.decodeFile(r5);
    L_0x02f7:
        r14 = new org.telegram.ui.Components.PhotoFilterView;
        r15 = r0.parentActivity;
        r14.<init>(r15, r5, r13, r6);
        r0.photoFilterView = r14;
        r5 = r0.containerView;
        r6 = r0.photoFilterView;
        r4 = org.telegram.ui.Components.LayoutHelper.createFrame(r4, r8);
        r5.addView(r6, r4);
        r4 = r0.photoFilterView;
        r4 = r4.getDoneTextView();
        r5 = new org.telegram.ui.-$$Lambda$PhotoViewer$iPzcqyK9klw8fE_zuZB7xfys4J8;
        r5.<init>(r0);
        r4.setOnClickListener(r5);
        r4 = r0.photoFilterView;
        r4 = r4.getCancelTextView();
        r5 = new org.telegram.ui.-$$Lambda$PhotoViewer$Ws0W6J-E4CCAikNDkwUaS7ufRkg;
        r5.<init>(r0);
        r4.setOnClickListener(r5);
        r4 = r0.photoFilterView;
        r4 = r4.getToolsView();
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r2 = (float) r2;
        r4.setTranslationY(r2);
    L_0x0335:
        r2 = new android.animation.AnimatorSet;
        r2.<init>();
        r0.changeModeAnimation = r2;
        r2 = new java.util.ArrayList;
        r2.<init>();
        r4 = r0.pickerView;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r12];
        r6[r3] = r10;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r8 = (float) r8;
        r6[r11] = r8;
        r4 = android.animation.ObjectAnimator.ofFloat(r4, r5, r6);
        r2.add(r4);
        r4 = r0.pickerViewSendButton;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r12];
        r6[r3] = r10;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r7 = (float) r7;
        r6[r11] = r7;
        r4 = android.animation.ObjectAnimator.ofFloat(r4, r5, r6);
        r2.add(r4);
        r4 = r0.actionBar;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r12];
        r6[r3] = r10;
        r3 = r4.getHeight();
        r3 = -r3;
        r3 = (float) r3;
        r6[r11] = r3;
        r3 = android.animation.ObjectAnimator.ofFloat(r4, r5, r6);
        r2.add(r3);
        r3 = r0.sendPhotoType;
        if (r3 == 0) goto L_0x039e;
    L_0x0388:
        if (r3 != r9) goto L_0x038b;
    L_0x038a:
        goto L_0x039e;
    L_0x038b:
        if (r3 != r11) goto L_0x03be;
    L_0x038d:
        r3 = r0.photoCropView;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
        goto L_0x03be;
    L_0x039e:
        r3 = r0.checkImageView;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
        r3 = r0.photosCounterView;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
    L_0x03be:
        r3 = r0.selectedPhotosListView;
        r3 = r3.getVisibility();
        if (r3 != 0) goto L_0x03d6;
    L_0x03c6:
        r3 = r0.selectedPhotosListView;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
    L_0x03d6:
        r3 = r0.cameraItem;
        r3 = r3.getTag();
        if (r3 == 0) goto L_0x03ee;
    L_0x03de:
        r3 = r0.cameraItem;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
    L_0x03ee:
        r3 = r0.changeModeAnimation;
        r3.playTogether(r2);
        r2 = r0.changeModeAnimation;
        r3 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r2.setDuration(r3);
        r2 = r0.changeModeAnimation;
        r3 = new org.telegram.ui.PhotoViewer$33;
        r3.<init>(r1);
        r2.addListener(r3);
        r1 = r0.changeModeAnimation;
        r1.start();
        goto L_0x0557;
    L_0x040b:
        if (r1 != r5) goto L_0x0557;
    L_0x040d:
        r2 = r0.photoPaintView;
        if (r2 != 0) goto L_0x0469;
    L_0x0411:
        r2 = new org.telegram.ui.Components.PhotoPaintView;
        r5 = r0.parentActivity;
        r13 = r0.centerImage;
        r13 = r13.getBitmap();
        r14 = r0.centerImage;
        r14 = r14.getOrientation();
        r2.<init>(r5, r13, r14);
        r0.photoPaintView = r2;
        r2 = r0.containerView;
        r5 = r0.photoPaintView;
        r4 = org.telegram.ui.Components.LayoutHelper.createFrame(r4, r8);
        r2.addView(r5, r4);
        r2 = r0.photoPaintView;
        r2 = r2.getDoneTextView();
        r4 = new org.telegram.ui.-$$Lambda$PhotoViewer$9pMvR6fP_n59bDPsHRYPAq6ryyo;
        r4.<init>(r0);
        r2.setOnClickListener(r4);
        r2 = r0.photoPaintView;
        r2 = r2.getCancelTextView();
        r4 = new org.telegram.ui.-$$Lambda$PhotoViewer$xHUEjTPCEWvnAkiqr8jK2QN-GCg;
        r4.<init>(r0);
        r2.setOnClickListener(r4);
        r2 = r0.photoPaintView;
        r2 = r2.getColorPicker();
        r4 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = (float) r4;
        r2.setTranslationY(r4);
        r2 = r0.photoPaintView;
        r2 = r2.getToolsView();
        r4 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r4 = (float) r4;
        r2.setTranslationY(r4);
    L_0x0469:
        r2 = new android.animation.AnimatorSet;
        r2.<init>();
        r0.changeModeAnimation = r2;
        r2 = new java.util.ArrayList;
        r2.<init>();
        r4 = r0.pickerView;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r12];
        r6[r3] = r10;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r8 = (float) r8;
        r6[r11] = r8;
        r4 = android.animation.ObjectAnimator.ofFloat(r4, r5, r6);
        r2.add(r4);
        r4 = r0.pickerViewSendButton;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r12];
        r6[r3] = r10;
        r8 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r8 = (float) r8;
        r6[r11] = r8;
        r4 = android.animation.ObjectAnimator.ofFloat(r4, r5, r6);
        r2.add(r4);
        r4 = r0.actionBar;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r12];
        r6[r3] = r10;
        r8 = r4.getHeight();
        r8 = -r8;
        r8 = (float) r8;
        r6[r11] = r8;
        r4 = android.animation.ObjectAnimator.ofFloat(r4, r5, r6);
        r2.add(r4);
        r4 = r0.needCaptionLayout;
        if (r4 == 0) goto L_0x04d2;
    L_0x04bc:
        r4 = r0.captionTextView;
        r5 = android.view.View.TRANSLATION_Y;
        r6 = new float[r12];
        r6[r3] = r10;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r3 = (float) r3;
        r6[r11] = r3;
        r3 = android.animation.ObjectAnimator.ofFloat(r4, r5, r6);
        r2.add(r3);
    L_0x04d2:
        r3 = r0.sendPhotoType;
        if (r3 == 0) goto L_0x04ec;
    L_0x04d6:
        if (r3 != r9) goto L_0x04d9;
    L_0x04d8:
        goto L_0x04ec;
    L_0x04d9:
        if (r3 != r11) goto L_0x050c;
    L_0x04db:
        r3 = r0.photoCropView;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
        goto L_0x050c;
    L_0x04ec:
        r3 = r0.checkImageView;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
        r3 = r0.photosCounterView;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
    L_0x050c:
        r3 = r0.selectedPhotosListView;
        r3 = r3.getVisibility();
        if (r3 != 0) goto L_0x0524;
    L_0x0514:
        r3 = r0.selectedPhotosListView;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
    L_0x0524:
        r3 = r0.cameraItem;
        r3 = r3.getTag();
        if (r3 == 0) goto L_0x053c;
    L_0x052c:
        r3 = r0.cameraItem;
        r4 = android.view.View.ALPHA;
        r5 = new float[r12];
        r5 = {NUM, 0};
        r3 = android.animation.ObjectAnimator.ofFloat(r3, r4, r5);
        r2.add(r3);
    L_0x053c:
        r3 = r0.changeModeAnimation;
        r3.playTogether(r2);
        r2 = r0.changeModeAnimation;
        r3 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r2.setDuration(r3);
        r2 = r0.changeModeAnimation;
        r3 = new org.telegram.ui.PhotoViewer$34;
        r3.<init>(r1);
        r2.addListener(r3);
        r1 = r0.changeModeAnimation;
        r1.start();
    L_0x0557:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.switchToEditMode(int):void");
    }

    public /* synthetic */ void lambda$switchToEditMode$38$PhotoViewer(View view) {
        applyCurrentEditMode();
        switchToEditMode(0);
    }

    public /* synthetic */ void lambda$switchToEditMode$40$PhotoViewer(View view) {
        if (this.photoFilterView.hasChanges()) {
            Context context = this.parentActivity;
            if (context != null) {
                Builder builder = new Builder(context);
                builder.setMessage(LocaleController.getString("DiscardChanges", NUM));
                builder.setTitle(LocaleController.getString("AppName", NUM));
                builder.setPositiveButton(LocaleController.getString("OK", NUM), new -$$Lambda$PhotoViewer$QEpK71JALiNMkiUImJBc4xxTNjw(this));
                builder.setNegativeButton(LocaleController.getString("Cancel", NUM), null);
                showAlertDialog(builder);
            } else {
                return;
            }
        }
        switchToEditMode(0);
    }

    public /* synthetic */ void lambda$null$39$PhotoViewer(DialogInterface dialogInterface, int i) {
        switchToEditMode(0);
    }

    public /* synthetic */ void lambda$switchToEditMode$41$PhotoViewer(View view) {
        applyCurrentEditMode();
        switchToEditMode(0);
    }

    public /* synthetic */ void lambda$null$42$PhotoViewer() {
        switchToEditMode(0);
    }

    public /* synthetic */ void lambda$switchToEditMode$43$PhotoViewer(View view) {
        this.photoPaintView.maybeShowDismissalAlert(this, this.parentActivity, new -$$Lambda$PhotoViewer$ihEX_rGeTSDXqm14d_kFpSR0yYM(this));
    }

    private void toggleCheckImageView(boolean z) {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList arrayList = new ArrayList();
        FrameLayout frameLayout = this.pickerView;
        Property property = View.ALPHA;
        float[] fArr = new float[1];
        float f = 1.0f;
        fArr[0] = z ? 1.0f : 0.0f;
        arrayList.add(ObjectAnimator.ofFloat(frameLayout, property, fArr));
        ImageView imageView = this.pickerViewSendButton;
        property = View.ALPHA;
        fArr = new float[1];
        fArr[0] = z ? 1.0f : 0.0f;
        arrayList.add(ObjectAnimator.ofFloat(imageView, property, fArr));
        if (this.needCaptionLayout) {
            TextView textView = this.captionTextView;
            property = View.ALPHA;
            fArr = new float[1];
            fArr[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(textView, property, fArr));
        }
        int i = this.sendPhotoType;
        if (i == 0 || i == 4) {
            CheckBox checkBox = this.checkImageView;
            property = View.ALPHA;
            fArr = new float[1];
            fArr[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(checkBox, property, fArr));
            CounterView counterView = this.photosCounterView;
            property = View.ALPHA;
            float[] fArr2 = new float[1];
            if (!z) {
                f = 0.0f;
            }
            fArr2[0] = f;
            arrayList.add(ObjectAnimator.ofFloat(counterView, property, fArr2));
        }
        animatorSet.playTogether(arrayList);
        animatorSet.setDuration(200);
        animatorSet.start();
    }

    private void toggleMiniProgressInternal(final boolean z) {
        if (z) {
            this.miniProgressView.setVisibility(0);
        }
        this.miniProgressAnimator = new AnimatorSet();
        AnimatorSet animatorSet = this.miniProgressAnimator;
        Animator[] animatorArr = new Animator[1];
        RadialProgressView radialProgressView = this.miniProgressView;
        Property property = View.ALPHA;
        float[] fArr = new float[1];
        fArr[0] = z ? 1.0f : 0.0f;
        animatorArr[0] = ObjectAnimator.ofFloat(radialProgressView, property, fArr);
        animatorSet.playTogether(animatorArr);
        this.miniProgressAnimator.setDuration(200);
        this.miniProgressAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (animator.equals(PhotoViewer.this.miniProgressAnimator)) {
                    if (!z) {
                        PhotoViewer.this.miniProgressView.setVisibility(4);
                    }
                    PhotoViewer.this.miniProgressAnimator = null;
                }
            }

            public void onAnimationCancel(Animator animator) {
                if (animator.equals(PhotoViewer.this.miniProgressAnimator)) {
                    PhotoViewer.this.miniProgressAnimator = null;
                }
            }
        });
        this.miniProgressAnimator.start();
    }

    private void toggleMiniProgress(boolean z, boolean z2) {
        int i = 0;
        if (z2) {
            toggleMiniProgressInternal(z);
            AnimatorSet animatorSet;
            if (z) {
                animatorSet = this.miniProgressAnimator;
                if (animatorSet != null) {
                    animatorSet.cancel();
                    this.miniProgressAnimator = null;
                }
                AndroidUtilities.cancelRunOnUIThread(this.miniProgressShowRunnable);
                if (this.firstAnimationDelay) {
                    this.firstAnimationDelay = false;
                    toggleMiniProgressInternal(true);
                    return;
                }
                AndroidUtilities.runOnUIThread(this.miniProgressShowRunnable, 500);
                return;
            }
            AndroidUtilities.cancelRunOnUIThread(this.miniProgressShowRunnable);
            animatorSet = this.miniProgressAnimator;
            if (animatorSet != null) {
                animatorSet.cancel();
                toggleMiniProgressInternal(false);
                return;
            }
            return;
        }
        AnimatorSet animatorSet2 = this.miniProgressAnimator;
        if (animatorSet2 != null) {
            animatorSet2.cancel();
            this.miniProgressAnimator = null;
        }
        this.miniProgressView.setAlpha(z ? 1.0f : 0.0f);
        RadialProgressView radialProgressView = this.miniProgressView;
        if (!z) {
            i = 4;
        }
        radialProgressView.setVisibility(i);
    }

    private void toggleActionBar(final boolean z, boolean z2) {
        AnimatorSet animatorSet = this.actionBarAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        if (z) {
            this.actionBar.setVisibility(0);
            if (this.bottomLayout.getTag() != null) {
                this.bottomLayout.setVisibility(0);
            }
            if (this.captionTextView.getTag() != null) {
                this.captionTextView.setVisibility(0);
                VideoSeekPreviewImage videoSeekPreviewImage = this.videoPreviewFrame;
                if (videoSeekPreviewImage != null) {
                    videoSeekPreviewImage.requestLayout();
                }
            }
        }
        this.isActionBarVisible = z;
        if (VERSION.SDK_INT >= 21 && this.sendPhotoType != 1) {
            int i = (this.containerView.getPaddingLeft() > 0 || this.containerView.getPaddingRight() > 0) ? 4098 : 0;
            int i2 = 4 | i;
            FrameLayoutDrawer frameLayoutDrawer;
            if (z) {
                frameLayoutDrawer = this.containerView;
                frameLayoutDrawer.setSystemUiVisibility((i2 ^ -1) & frameLayoutDrawer.getSystemUiVisibility());
            } else {
                frameLayoutDrawer = this.containerView;
                frameLayoutDrawer.setSystemUiVisibility(i2 | frameLayoutDrawer.getSystemUiVisibility());
            }
        }
        float f = 1.0f;
        if (z2) {
            ArrayList arrayList = new ArrayList();
            ActionBar actionBar = this.actionBar;
            Property property = View.ALPHA;
            float[] fArr = new float[1];
            fArr[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBar, property, fArr));
            FrameLayout frameLayout = this.bottomLayout;
            if (frameLayout != null) {
                property = View.ALPHA;
                fArr = new float[1];
                fArr[0] = z ? 1.0f : 0.0f;
                arrayList.add(ObjectAnimator.ofFloat(frameLayout, property, fArr));
            }
            GroupedPhotosListView groupedPhotosListView = this.groupedPhotosListView;
            property = View.ALPHA;
            fArr = new float[1];
            fArr[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(groupedPhotosListView, property, fArr));
            if (this.captionTextView.getTag() != null) {
                TextView textView = this.captionTextView;
                property = View.ALPHA;
                float[] fArr2 = new float[1];
                if (!z) {
                    f = 0.0f;
                }
                fArr2[0] = f;
                arrayList.add(ObjectAnimator.ofFloat(textView, property, fArr2));
            }
            this.actionBarAnimator = new AnimatorSet();
            this.actionBarAnimator.playTogether(arrayList);
            this.actionBarAnimator.setDuration(200);
            this.actionBarAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    if (animator.equals(PhotoViewer.this.actionBarAnimator)) {
                        if (!z) {
                            PhotoViewer.this.actionBar.setVisibility(4);
                            if (PhotoViewer.this.bottomLayout.getTag() != null) {
                                PhotoViewer.this.bottomLayout.setVisibility(4);
                            }
                            if (PhotoViewer.this.captionTextView.getTag() != null) {
                                PhotoViewer.this.captionTextView.setVisibility(4);
                            }
                        }
                        PhotoViewer.this.actionBarAnimator = null;
                    }
                }

                public void onAnimationCancel(Animator animator) {
                    if (animator.equals(PhotoViewer.this.actionBarAnimator)) {
                        PhotoViewer.this.actionBarAnimator = null;
                    }
                }
            });
            this.actionBarAnimator.start();
            return;
        }
        this.actionBar.setAlpha(z ? 1.0f : 0.0f);
        this.bottomLayout.setAlpha(z ? 1.0f : 0.0f);
        this.groupedPhotosListView.setAlpha(z ? 1.0f : 0.0f);
        TextView textView2 = this.captionTextView;
        if (!z) {
            f = 0.0f;
        }
        textView2.setAlpha(f);
    }

    private void togglePhotosListView(boolean z, boolean z2) {
        if (z != this.isPhotosListViewVisible) {
            if (z) {
                this.selectedPhotosListView.setVisibility(0);
            }
            this.isPhotosListViewVisible = z;
            this.selectedPhotosListView.setEnabled(z);
            float f = 1.0f;
            if (z2) {
                ArrayList arrayList = new ArrayList();
                RecyclerListView recyclerListView = this.selectedPhotosListView;
                Property property = View.ALPHA;
                float[] fArr = new float[1];
                fArr[0] = z ? 1.0f : 0.0f;
                arrayList.add(ObjectAnimator.ofFloat(recyclerListView, property, fArr));
                recyclerListView = this.selectedPhotosListView;
                property = View.TRANSLATION_Y;
                fArr = new float[1];
                fArr[0] = z ? 0.0f : (float) (-AndroidUtilities.dp(10.0f));
                arrayList.add(ObjectAnimator.ofFloat(recyclerListView, property, fArr));
                CounterView counterView = this.photosCounterView;
                Property property2 = View.ROTATION_X;
                float[] fArr2 = new float[1];
                if (!z) {
                    f = 0.0f;
                }
                fArr2[0] = f;
                arrayList.add(ObjectAnimator.ofFloat(counterView, property2, fArr2));
                this.currentListViewAnimation = new AnimatorSet();
                this.currentListViewAnimation.playTogether(arrayList);
                if (!z) {
                    this.currentListViewAnimation.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            if (PhotoViewer.this.currentListViewAnimation != null && PhotoViewer.this.currentListViewAnimation.equals(animator)) {
                                PhotoViewer.this.selectedPhotosListView.setVisibility(8);
                                PhotoViewer.this.currentListViewAnimation = null;
                            }
                        }
                    });
                }
                this.currentListViewAnimation.setDuration(200);
                this.currentListViewAnimation.start();
            } else {
                this.selectedPhotosListView.setAlpha(z ? 1.0f : 0.0f);
                this.selectedPhotosListView.setTranslationY(z ? 0.0f : (float) (-AndroidUtilities.dp(10.0f)));
                CounterView counterView2 = this.photosCounterView;
                if (!z) {
                    f = 0.0f;
                }
                counterView2.setRotationX(f);
                if (!z) {
                    this.selectedPhotosListView.setVisibility(8);
                }
            }
        }
    }

    private String getFileName(int i) {
        if (i < 0) {
            return null;
        }
        String str = ".jpg";
        String str2 = "_";
        StringBuilder stringBuilder;
        if (this.secureDocuments.isEmpty()) {
            if (this.imagesArrLocations.isEmpty() && this.imagesArr.isEmpty()) {
                if (this.imagesArrLocals.isEmpty() || i >= this.imagesArrLocals.size()) {
                    return null;
                }
                Object obj = this.imagesArrLocals.get(i);
                if (obj instanceof SearchImage) {
                    return ((SearchImage) obj).getAttachName();
                }
                if (obj instanceof BotInlineResult) {
                    BotInlineResult botInlineResult = (BotInlineResult) obj;
                    Document document = botInlineResult.document;
                    if (document != null) {
                        return FileLoader.getAttachFileName(document);
                    }
                    Photo photo = botInlineResult.photo;
                    if (photo != null) {
                        return FileLoader.getAttachFileName(FileLoader.getClosestPhotoSizeWithSize(photo.sizes, AndroidUtilities.getPhotoSize()));
                    }
                    if (botInlineResult.content instanceof TL_webDocument) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(Utilities.MD5(botInlineResult.content.url));
                        stringBuilder.append(".");
                        WebDocument webDocument = botInlineResult.content;
                        stringBuilder.append(ImageLoader.getHttpUrlExtension(webDocument.url, FileLoader.getMimeTypePart(webDocument.mime_type)));
                        return stringBuilder.toString();
                    }
                }
            } else if (this.imagesArrLocations.isEmpty()) {
                if (this.imagesArr.isEmpty() || i >= this.imagesArr.size()) {
                    return null;
                }
                return FileLoader.getMessageFileName(((MessageObject) this.imagesArr.get(i)).messageOwner);
            } else if (i >= this.imagesArrLocations.size()) {
                return null;
            } else {
                ImageLocation imageLocation = (ImageLocation) this.imagesArrLocations.get(i);
                if (imageLocation == null) {
                    return null;
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append(imageLocation.location.volume_id);
                stringBuilder.append(str2);
                stringBuilder.append(imageLocation.location.local_id);
                stringBuilder.append(str);
                return stringBuilder.toString();
            }
            return null;
        } else if (i >= this.secureDocuments.size()) {
            return null;
        } else {
            SecureDocument secureDocument = (SecureDocument) this.secureDocuments.get(i);
            stringBuilder = new StringBuilder();
            stringBuilder.append(secureDocument.secureFile.dc_id);
            stringBuilder.append(str2);
            stringBuilder.append(secureDocument.secureFile.id);
            stringBuilder.append(str);
            return stringBuilder.toString();
        }
    }

    private ImageLocation getImageLocation(int i, int[] iArr) {
        if (i < 0) {
            return null;
        }
        if (this.secureDocuments.isEmpty()) {
            if (this.imagesArrLocations.isEmpty()) {
                if (this.imagesArr.isEmpty() || i >= this.imagesArr.size()) {
                    return null;
                }
                MessageObject messageObject = (MessageObject) this.imagesArr.get(i);
                Message message = messageObject.messageOwner;
                PhotoSize closestPhotoSizeWithSize;
                if (!(message instanceof TL_messageService)) {
                    MessageMedia messageMedia = message.media;
                    if (!(messageMedia instanceof TL_messageMediaPhoto) || messageMedia.photo == null) {
                        messageMedia = messageObject.messageOwner.media;
                        if (!(messageMedia instanceof TL_messageMediaWebPage) || messageMedia.webpage == null) {
                            messageMedia = messageObject.messageOwner.media;
                            if (messageMedia instanceof TL_messageMediaInvoice) {
                                return ImageLocation.getForWebFile(WebFile.createWithWebDocument(((TL_messageMediaInvoice) messageMedia).photo));
                            }
                            if (messageObject.getDocument() != null && MessageObject.isDocumentHasThumb(messageObject.getDocument())) {
                                Document document = messageObject.getDocument();
                                PhotoSize closestPhotoSizeWithSize2 = FileLoader.getClosestPhotoSizeWithSize(document.thumbs, 90);
                                if (iArr != null) {
                                    iArr[0] = closestPhotoSizeWithSize2.size;
                                    if (iArr[0] == 0) {
                                        iArr[0] = -1;
                                    }
                                }
                                return ImageLocation.getForDocument(closestPhotoSizeWithSize2, document);
                            }
                        }
                    }
                    if (messageObject.isGif()) {
                        return ImageLocation.getForDocument(messageObject.getDocument());
                    }
                    closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, AndroidUtilities.getPhotoSize());
                    if (closestPhotoSizeWithSize != null) {
                        if (iArr != null) {
                            iArr[0] = closestPhotoSizeWithSize.size;
                            if (iArr[0] == 0) {
                                iArr[0] = -1;
                            }
                        }
                        return ImageLocation.getForObject(closestPhotoSizeWithSize, messageObject.photoThumbsObject);
                    } else if (iArr != null) {
                        iArr[0] = -1;
                    }
                } else if (message.action instanceof TL_messageActionUserUpdatedPhoto) {
                    return null;
                } else {
                    closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, AndroidUtilities.getPhotoSize());
                    if (closestPhotoSizeWithSize != null) {
                        if (iArr != null) {
                            iArr[0] = closestPhotoSizeWithSize.size;
                            if (iArr[0] == 0) {
                                iArr[0] = -1;
                            }
                        }
                        return ImageLocation.getForObject(closestPhotoSizeWithSize, messageObject.photoThumbsObject);
                    } else if (iArr != null) {
                        iArr[0] = -1;
                    }
                }
                return null;
            } else if (i >= this.imagesArrLocations.size()) {
                return null;
            } else {
                if (iArr != null) {
                    iArr[0] = ((Integer) this.imagesArrLocationsSizes.get(i)).intValue();
                }
                return (ImageLocation) this.imagesArrLocations.get(i);
            }
        } else if (i >= this.secureDocuments.size()) {
            return null;
        } else {
            if (iArr != null) {
                iArr[0] = ((SecureDocument) this.secureDocuments.get(i)).secureFile.size;
            }
            return ImageLocation.getForSecureDocument((SecureDocument) this.secureDocuments.get(i));
        }
    }

    private TLObject getFileLocation(int i, int[] iArr) {
        if (i < 0) {
            return null;
        }
        if (this.secureDocuments.isEmpty()) {
            if (this.imagesArrLocations.isEmpty()) {
                if (this.imagesArr.isEmpty() || i >= this.imagesArr.size()) {
                    return null;
                }
                MessageObject messageObject = (MessageObject) this.imagesArr.get(i);
                Message message = messageObject.messageOwner;
                PhotoSize closestPhotoSizeWithSize;
                if (message instanceof TL_messageService) {
                    MessageAction messageAction = message.action;
                    if (messageAction instanceof TL_messageActionUserUpdatedPhoto) {
                        return messageAction.newUserPhoto.photo_big;
                    }
                    closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, AndroidUtilities.getPhotoSize());
                    if (closestPhotoSizeWithSize != null) {
                        if (iArr != null) {
                            iArr[0] = closestPhotoSizeWithSize.size;
                            if (iArr[0] == 0) {
                                iArr[0] = -1;
                            }
                        }
                        return closestPhotoSizeWithSize;
                    } else if (iArr != null) {
                        iArr[0] = -1;
                    }
                } else {
                    MessageMedia messageMedia = message.media;
                    if (!(messageMedia instanceof TL_messageMediaPhoto) || messageMedia.photo == null) {
                        messageMedia = messageObject.messageOwner.media;
                        if (!(messageMedia instanceof TL_messageMediaWebPage) || messageMedia.webpage == null) {
                            messageMedia = messageObject.messageOwner.media;
                            if (messageMedia instanceof TL_messageMediaInvoice) {
                                return ((TL_messageMediaInvoice) messageMedia).photo;
                            }
                            if (messageObject.getDocument() != null && MessageObject.isDocumentHasThumb(messageObject.getDocument())) {
                                closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.getDocument().thumbs, 90);
                                if (iArr != null) {
                                    iArr[0] = closestPhotoSizeWithSize.size;
                                    if (iArr[0] == 0) {
                                        iArr[0] = -1;
                                    }
                                }
                                return closestPhotoSizeWithSize;
                            }
                        }
                    }
                    closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, AndroidUtilities.getPhotoSize());
                    if (closestPhotoSizeWithSize != null) {
                        if (iArr != null) {
                            iArr[0] = closestPhotoSizeWithSize.size;
                            if (iArr[0] == 0) {
                                iArr[0] = -1;
                            }
                        }
                        return closestPhotoSizeWithSize;
                    } else if (iArr != null) {
                        iArr[0] = -1;
                    }
                }
                return null;
            } else if (i >= this.imagesArrLocations.size()) {
                return null;
            } else {
                if (iArr != null) {
                    iArr[0] = ((Integer) this.imagesArrLocationsSizes.get(i)).intValue();
                }
                return ((ImageLocation) this.imagesArrLocations.get(i)).location;
            }
        } else if (i >= this.secureDocuments.size()) {
            return null;
        } else {
            if (iArr != null) {
                iArr[0] = ((SecureDocument) this.secureDocuments.get(i)).secureFile.size;
            }
            return (TLObject) this.secureDocuments.get(i);
        }
    }

    private void updateSelectedCount() {
        PhotoViewerProvider photoViewerProvider = this.placeProvider;
        if (photoViewerProvider != null) {
            int selectedCount = photoViewerProvider.getSelectedCount();
            this.photosCounterView.setCount(selectedCount);
            if (selectedCount == 0) {
                togglePhotosListView(false, true);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:201:0x051e  */
    /* JADX WARNING: Removed duplicated region for block: B:200:0x051c  */
    /* JADX WARNING: Removed duplicated region for block: B:204:0x0527  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x069c  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x04d8  */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x0567  */
    /* JADX WARNING: Removed duplicated region for block: B:207:0x055b  */
    /* JADX WARNING: Removed duplicated region for block: B:228:0x05cb  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0634  */
    /* JADX WARNING: Removed duplicated region for block: B:228:0x05cb  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0634  */
    /* JADX WARNING: Removed duplicated region for block: B:228:0x05cb  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0634  */
    /* JADX WARNING: Missing block: B:53:0x022b, code skipped:
            if ("telegram_album".equals(r2.type) != false) goto L_0x022d;
     */
    /* JADX WARNING: Missing block: B:178:0x04d1, code skipped:
            if (r0.cropItem.getVisibility() == 0) goto L_0x04a1;
     */
    private void onPhotoShow(org.telegram.messenger.MessageObject r27, org.telegram.tgnet.TLRPC.FileLocation r28, org.telegram.messenger.ImageLocation r29, java.util.ArrayList<org.telegram.messenger.MessageObject> r30, java.util.ArrayList<org.telegram.messenger.SecureDocument> r31, java.util.ArrayList<java.lang.Object> r32, int r33, org.telegram.ui.PhotoViewer.PlaceProviderObject r34) {
        /*
        r26 = this;
        r0 = r26;
        r1 = r27;
        r2 = r30;
        r3 = r31;
        r4 = r32;
        r5 = r33;
        r6 = r34;
        r7 = org.telegram.tgnet.ConnectionsManager.generateClassGuid();
        r0.classGuid = r7;
        r7 = 0;
        r0.currentMessageObject = r7;
        r0.currentFileLocation = r7;
        r0.currentSecureDocument = r7;
        r0.currentPathObject = r7;
        r8 = 0;
        r0.fromCamera = r8;
        r0.currentBotInlineResult = r7;
        r9 = -1;
        r0.currentIndex = r9;
        r10 = r0.currentFileNames;
        r10[r8] = r7;
        r11 = 1;
        r12 = java.lang.Integer.valueOf(r11);
        r10[r11] = r7;
        r13 = 2;
        r10[r13] = r7;
        r0.avatarsDialogId = r8;
        r0.totalImagesCount = r8;
        r0.totalImagesCountMerge = r8;
        r0.currentEditMode = r8;
        r0.isFirstLoading = r11;
        r0.needSearchImageInArr = r8;
        r0.loadingMoreImages = r8;
        r10 = r0.endReached;
        r10[r8] = r8;
        r14 = r0.mergeDialogId;
        r16 = 0;
        r18 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1));
        if (r18 != 0) goto L_0x004f;
    L_0x004d:
        r14 = 1;
        goto L_0x0050;
    L_0x004f:
        r14 = 0;
    L_0x0050:
        r10[r11] = r14;
        r0.opennedFromMedia = r8;
        r0.needCaptionLayout = r8;
        r10 = r0.containerView;
        r10.setTag(r12);
        r0.isCurrentVideo = r8;
        r10 = r0.imagesArr;
        r10.clear();
        r10 = r0.imagesArrLocations;
        r10.clear();
        r10 = r0.imagesArrLocationsSizes;
        r10.clear();
        r10 = r0.avatarsArr;
        r10.clear();
        r10 = r0.secureDocuments;
        r10.clear();
        r10 = r0.imagesArrLocals;
        r10.clear();
        r10 = 0;
    L_0x007c:
        if (r10 >= r13) goto L_0x008f;
    L_0x007e:
        r14 = r0.imagesByIds;
        r14 = r14[r10];
        r14.clear();
        r14 = r0.imagesByIdsTemp;
        r14 = r14[r10];
        r14.clear();
        r10 = r10 + 1;
        goto L_0x007c;
    L_0x008f:
        r10 = r0.imagesArrTemp;
        r10.clear();
        r0.currentUserAvatarLocation = r7;
        r10 = r0.containerView;
        r10.setPadding(r8, r8, r8, r8);
        r10 = r0.currentThumb;
        if (r10 == 0) goto L_0x00a2;
    L_0x009f:
        r10.release();
    L_0x00a2:
        if (r6 == 0) goto L_0x00a7;
    L_0x00a4:
        r10 = r6.thumb;
        goto L_0x00a8;
    L_0x00a7:
        r10 = r7;
    L_0x00a8:
        r0.currentThumb = r10;
        if (r6 == 0) goto L_0x00b2;
    L_0x00ac:
        r10 = r6.isEvent;
        if (r10 == 0) goto L_0x00b2;
    L_0x00b0:
        r10 = 1;
        goto L_0x00b3;
    L_0x00b2:
        r10 = 0;
    L_0x00b3:
        r0.isEvent = r10;
        r0.sharedMediaType = r8;
        r10 = r0.allMediaItem;
        r14 = NUM; // 0x7f0e0a19 float:1.888028E38 double:1.053163434E-314;
        r15 = "ShowAllMedia";
        r14 = org.telegram.messenger.LocaleController.getString(r15, r14);
        r10.setText(r14);
        r10 = r0.menuItem;
        r10.setVisibility(r8);
        r10 = r0.sendItem;
        r14 = 8;
        r10.setVisibility(r14);
        r10 = r0.pipItem;
        r10.setVisibility(r14);
        r10 = r0.cameraItem;
        r10.setVisibility(r14);
        r10 = r0.cameraItem;
        r10.setTag(r7);
        r10 = r0.bottomLayout;
        r10.setVisibility(r8);
        r10 = r0.bottomLayout;
        r10.setTag(r12);
        r10 = r0.bottomLayout;
        r15 = 0;
        r10.setTranslationY(r15);
        r10 = r0.captionTextView;
        r10.setTranslationY(r15);
        r10 = r0.shareButton;
        r10.setVisibility(r14);
        r10 = r0.qualityChooseView;
        r9 = 4;
        if (r10 == 0) goto L_0x010c;
    L_0x00ff:
        r10.setVisibility(r9);
        r10 = r0.qualityPicker;
        r10.setVisibility(r9);
        r10 = r0.qualityChooseView;
        r10.setTag(r7);
    L_0x010c:
        r10 = r0.qualityChooseViewAnimation;
        if (r10 == 0) goto L_0x0115;
    L_0x0110:
        r10.cancel();
        r0.qualityChooseViewAnimation = r7;
    L_0x0115:
        r0.setDoubleTapEnabled(r11);
        r0.allowShare = r8;
        r0.slideshowMessageId = r8;
        r0.nameOverride = r7;
        r0.dateOverride = r8;
        r10 = r0.menuItem;
        r10.hideSubItem(r13);
        r10 = r0.menuItem;
        r10.hideSubItem(r9);
        r10 = r0.menuItem;
        r13 = 10;
        r10.hideSubItem(r13);
        r10 = r0.menuItem;
        r13 = 11;
        r10.hideSubItem(r13);
        r10 = r0.menuItem;
        r13 = 14;
        r10.hideSubItem(r13);
        r10 = r0.actionBar;
        r10.setTranslationY(r15);
        r10 = r0.checkImageView;
        r13 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r10.setAlpha(r13);
        r10 = r0.checkImageView;
        r10.setVisibility(r14);
        r10 = r0.actionBar;
        r10.setTitleRightMargin(r8);
        r10 = r0.photosCounterView;
        r10.setAlpha(r13);
        r10 = r0.photosCounterView;
        r10.setVisibility(r14);
        r10 = r0.pickerView;
        r10.setVisibility(r14);
        r10 = r0.pickerViewSendButton;
        r10.setVisibility(r14);
        r10 = r0.pickerViewSendButton;
        r10.setTranslationY(r15);
        r10 = r0.pickerView;
        r10.setAlpha(r13);
        r10 = r0.pickerViewSendButton;
        r10.setAlpha(r13);
        r10 = r0.pickerView;
        r10.setTranslationY(r15);
        r10 = r0.paintItem;
        r10.setVisibility(r14);
        r10 = r0.cropItem;
        r10.setVisibility(r14);
        r10 = r0.tuneItem;
        r10.setVisibility(r14);
        r10 = r0.timeItem;
        r10.setVisibility(r14);
        r10 = r0.rotateItem;
        r10.setVisibility(r14);
        r10 = r0.videoTimelineView;
        r10.setVisibility(r14);
        r10 = r0.compressItem;
        r10.setVisibility(r14);
        r10 = r0.captionEditText;
        r10.setVisibility(r14);
        r10 = r0.mentionListView;
        r10.setVisibility(r14);
        r10 = r0.muteItem;
        r10.setVisibility(r14);
        r10 = r0.actionBar;
        r10.setSubtitle(r7);
        r10 = r0.masksItem;
        r10.setVisibility(r14);
        r0.muteVideo = r8;
        r10 = r0.muteItem;
        r13 = NUM; // 0x7var_e5 float:1.794608E38 double:1.052935869E-314;
        r10.setImageResource(r13);
        r10 = r0.editorDoneLayout;
        r10.setVisibility(r14);
        r10 = r0.captionTextView;
        r10.setTag(r7);
        r10 = r0.captionTextView;
        r10.setVisibility(r9);
        r10 = r0.photoCropView;
        if (r10 == 0) goto L_0x01d9;
    L_0x01d6:
        r10.setVisibility(r14);
    L_0x01d9:
        r10 = r0.photoFilterView;
        if (r10 == 0) goto L_0x01e0;
    L_0x01dd:
        r10.setVisibility(r14);
    L_0x01e0:
        r10 = 0;
    L_0x01e1:
        r13 = 3;
        if (r10 >= r13) goto L_0x01f6;
    L_0x01e4:
        r13 = r0.photoProgressViews;
        r22 = r13[r10];
        if (r22 == 0) goto L_0x01f1;
    L_0x01ea:
        r13 = r13[r10];
        r15 = -1;
        r13.setBackgroundState(r15, r8);
        goto L_0x01f2;
    L_0x01f1:
        r15 = -1;
    L_0x01f2:
        r10 = r10 + 1;
        r15 = 0;
        goto L_0x01e1;
    L_0x01f6:
        r10 = NUM; // 0x7f0e0a18 float:1.8880279E38 double:1.0531634333E-314;
        r13 = "ShowAllFiles";
        if (r1 == 0) goto L_0x02f0;
    L_0x01fd:
        if (r2 != 0) goto L_0x02f0;
    L_0x01ff:
        r2 = r1.messageOwner;
        r2 = r2.media;
        r3 = r2 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
        if (r3 == 0) goto L_0x028d;
    L_0x0207:
        r2 = r2.webpage;
        if (r2 == 0) goto L_0x028d;
    L_0x020b:
        r3 = r2.site_name;
        if (r3 == 0) goto L_0x028d;
    L_0x020f:
        r3 = r3.toLowerCase();
        r4 = "instagram";
        r4 = r3.equals(r4);
        if (r4 != 0) goto L_0x022d;
    L_0x021b:
        r4 = "twitter";
        r3 = r3.equals(r4);
        if (r3 != 0) goto L_0x022d;
    L_0x0223:
        r3 = r2.type;
        r4 = "telegram_album";
        r3 = r4.equals(r3);
        if (r3 == 0) goto L_0x028d;
    L_0x022d:
        r3 = r2.author;
        r3 = android.text.TextUtils.isEmpty(r3);
        if (r3 != 0) goto L_0x0239;
    L_0x0235:
        r3 = r2.author;
        r0.nameOverride = r3;
    L_0x0239:
        r3 = r2.cached_page;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_page;
        if (r3 == 0) goto L_0x0262;
    L_0x023f:
        r3 = 0;
    L_0x0240:
        r4 = r2.cached_page;
        r4 = r4.blocks;
        r4 = r4.size();
        if (r3 >= r4) goto L_0x0262;
    L_0x024a:
        r4 = r2.cached_page;
        r4 = r4.blocks;
        r4 = r4.get(r3);
        r4 = (org.telegram.tgnet.TLRPC.PageBlock) r4;
        r6 = r4 instanceof org.telegram.tgnet.TLRPC.TL_pageBlockAuthorDate;
        if (r6 == 0) goto L_0x025f;
    L_0x0258:
        r4 = (org.telegram.tgnet.TLRPC.TL_pageBlockAuthorDate) r4;
        r2 = r4.published_date;
        r0.dateOverride = r2;
        goto L_0x0262;
    L_0x025f:
        r3 = r3 + 1;
        goto L_0x0240;
    L_0x0262:
        r2 = r1.getWebPagePhotos(r7, r7);
        r3 = r2.isEmpty();
        if (r3 != 0) goto L_0x028d;
    L_0x026c:
        r3 = r27.getId();
        r0.slideshowMessageId = r3;
        r0.needSearchImageInArr = r8;
        r3 = r0.imagesArr;
        r3.addAll(r2);
        r2 = r0.imagesArr;
        r2 = r2.size();
        r0.totalImagesCount = r2;
        r2 = r0.imagesArr;
        r2 = r2.indexOf(r1);
        if (r2 >= 0) goto L_0x028a;
    L_0x0289:
        r2 = 0;
    L_0x028a:
        r0.setImageIndex(r2, r11);
    L_0x028d:
        r2 = r27.canPreviewDocument();
        if (r2 == 0) goto L_0x029e;
    L_0x0293:
        r0.sharedMediaType = r11;
        r2 = r0.allMediaItem;
        r3 = org.telegram.messenger.LocaleController.getString(r13, r10);
        r2.setText(r3);
    L_0x029e:
        r2 = r0.slideshowMessageId;
        if (r2 != 0) goto L_0x02fa;
    L_0x02a2:
        r2 = r0.imagesArr;
        r2.add(r1);
        r2 = r1.eventId;
        r4 = (r2 > r16 ? 1 : (r2 == r16 ? 0 : -1));
        if (r4 == 0) goto L_0x02b0;
    L_0x02ad:
        r0.needSearchImageInArr = r8;
        goto L_0x02ec;
    L_0x02b0:
        r2 = r0.currentAnimation;
        if (r2 == 0) goto L_0x02bc;
    L_0x02b4:
        r0.needSearchImageInArr = r8;
        r1 = r0.sendItem;
        r1.setVisibility(r8);
        goto L_0x02ec;
    L_0x02bc:
        r2 = r1.scheduled;
        if (r2 != 0) goto L_0x02ec;
    L_0x02c0:
        r2 = r1.messageOwner;
        r3 = r2.media;
        r4 = r3 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
        if (r4 != 0) goto L_0x02ec;
    L_0x02c8:
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
        if (r3 != 0) goto L_0x02ec;
    L_0x02cc:
        r2 = r2.action;
        if (r2 == 0) goto L_0x02d4;
    L_0x02d0:
        r2 = r2 instanceof org.telegram.tgnet.TLRPC.TL_messageActionEmpty;
        if (r2 == 0) goto L_0x02ec;
    L_0x02d4:
        r0.needSearchImageInArr = r11;
        r2 = r0.imagesByIds;
        r2 = r2[r8];
        r3 = r27.getId();
        r2.put(r3, r1);
        r1 = r0.menuItem;
        r2 = 2;
        r1.showSubItem(r2);
        r1 = r0.sendItem;
        r1.setVisibility(r8);
    L_0x02ec:
        r0.setImageIndex(r8, r11);
        goto L_0x02fa;
    L_0x02f0:
        if (r3 == 0) goto L_0x02fd;
    L_0x02f2:
        r1 = r0.secureDocuments;
        r1.addAll(r3);
        r0.setImageIndex(r5, r11);
    L_0x02fa:
        r3 = r7;
        goto L_0x0591;
    L_0x02fd:
        if (r28 == 0) goto L_0x038a;
    L_0x02ff:
        r1 = r6.dialogId;
        r0.avatarsDialogId = r1;
        if (r29 != 0) goto L_0x0334;
    L_0x0305:
        r1 = r0.avatarsDialogId;
        if (r1 <= 0) goto L_0x031e;
    L_0x0309:
        r1 = r0.currentAccount;
        r1 = org.telegram.messenger.MessagesController.getInstance(r1);
        r2 = r0.avatarsDialogId;
        r2 = java.lang.Integer.valueOf(r2);
        r1 = r1.getUser(r2);
        r1 = org.telegram.messenger.ImageLocation.getForUser(r1, r11);
        goto L_0x0336;
    L_0x031e:
        r1 = r0.currentAccount;
        r1 = org.telegram.messenger.MessagesController.getInstance(r1);
        r2 = r0.avatarsDialogId;
        r2 = -r2;
        r2 = java.lang.Integer.valueOf(r2);
        r1 = r1.getChat(r2);
        r1 = org.telegram.messenger.ImageLocation.getForChat(r1, r11);
        goto L_0x0336;
    L_0x0334:
        r1 = r29;
    L_0x0336:
        if (r1 != 0) goto L_0x033c;
    L_0x0338:
        r0.closePhoto(r8, r8);
        return;
    L_0x033c:
        r2 = r0.imagesArrLocations;
        r2.add(r1);
        r0.currentUserAvatarLocation = r1;
        r1 = r0.imagesArrLocationsSizes;
        r2 = r6.size;
        r2 = java.lang.Integer.valueOf(r2);
        r1.add(r2);
        r1 = r0.avatarsArr;
        r2 = new org.telegram.tgnet.TLRPC$TL_photoEmpty;
        r2.<init>();
        r1.add(r2);
        r1 = r0.shareButton;
        r2 = r0.videoPlayerControlFrameLayout;
        r2 = r2.getVisibility();
        if (r2 == 0) goto L_0x0363;
    L_0x0362:
        r14 = 0;
    L_0x0363:
        r1.setVisibility(r14);
        r0.allowShare = r11;
        r1 = r0.menuItem;
        r2 = 2;
        r1.hideSubItem(r2);
        r1 = r0.shareButton;
        r1 = r1.getVisibility();
        if (r1 != 0) goto L_0x037e;
    L_0x0376:
        r1 = r0.menuItem;
        r2 = 10;
        r1.hideSubItem(r2);
        goto L_0x0385;
    L_0x037e:
        r2 = 10;
        r1 = r0.menuItem;
        r1.showSubItem(r2);
    L_0x0385:
        r0.setImageIndex(r8, r11);
        goto L_0x02fa;
    L_0x038a:
        if (r2 == 0) goto L_0x03f7;
    L_0x038c:
        r1 = r0.imagesArr;
        r1.addAll(r2);
        r1 = 0;
    L_0x0392:
        r2 = r0.imagesArr;
        r2 = r2.size();
        if (r1 >= r2) goto L_0x03bf;
    L_0x039a:
        r2 = r0.imagesArr;
        r2 = r2.get(r1);
        r2 = (org.telegram.messenger.MessageObject) r2;
        r3 = r0.imagesByIds;
        r14 = r2.getDialogId();
        r7 = r0.currentDialogId;
        r4 = (r14 > r7 ? 1 : (r14 == r7 ? 0 : -1));
        if (r4 != 0) goto L_0x03b0;
    L_0x03ae:
        r4 = 0;
        goto L_0x03b1;
    L_0x03b0:
        r4 = 1;
    L_0x03b1:
        r3 = r3[r4];
        r4 = r2.getId();
        r3.put(r4, r2);
        r1 = r1 + 1;
        r7 = 0;
        r8 = 0;
        goto L_0x0392;
    L_0x03bf:
        r1 = r0.imagesArr;
        r1 = r1.get(r5);
        r1 = (org.telegram.messenger.MessageObject) r1;
        r2 = r1.scheduled;
        if (r2 != 0) goto L_0x03ea;
    L_0x03cb:
        r0.opennedFromMedia = r11;
        r2 = r0.menuItem;
        r2.showSubItem(r9);
        r2 = r0.sendItem;
        r3 = 0;
        r2.setVisibility(r3);
        r1 = r1.canPreviewDocument();
        if (r1 == 0) goto L_0x03f2;
    L_0x03de:
        r0.sharedMediaType = r11;
        r1 = r0.allMediaItem;
        r2 = org.telegram.messenger.LocaleController.getString(r13, r10);
        r1.setText(r2);
        goto L_0x03f2;
    L_0x03ea:
        r1 = r0.imagesArr;
        r1 = r1.size();
        r0.totalImagesCount = r1;
    L_0x03f2:
        r0.setImageIndex(r5, r11);
        goto L_0x0590;
    L_0x03f7:
        if (r4 == 0) goto L_0x0590;
    L_0x03f9:
        r1 = r0.sendPhotoType;
        r2 = 5;
        if (r1 == 0) goto L_0x040b;
    L_0x03fe:
        if (r1 == r9) goto L_0x040b;
    L_0x0400:
        r3 = 2;
        if (r1 == r3) goto L_0x0405;
    L_0x0403:
        if (r1 != r2) goto L_0x0421;
    L_0x0405:
        r1 = r32.size();
        if (r1 <= r11) goto L_0x0421;
    L_0x040b:
        r1 = r0.checkImageView;
        r3 = 0;
        r1.setVisibility(r3);
        r1 = r0.photosCounterView;
        r1.setVisibility(r3);
        r1 = r0.actionBar;
        r3 = NUM; // 0x42CLASSNAME float:100.0 double:5.53552857E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r1.setTitleRightMargin(r3);
    L_0x0421:
        r1 = r0.sendPhotoType;
        r3 = 2;
        if (r1 == r3) goto L_0x0428;
    L_0x0426:
        if (r1 != r2) goto L_0x043b;
    L_0x0428:
        r1 = r0.placeProvider;
        r1 = r1.canCaptureMorePhotos();
        if (r1 == 0) goto L_0x043b;
    L_0x0430:
        r1 = r0.cameraItem;
        r3 = 0;
        r1.setVisibility(r3);
        r1 = r0.cameraItem;
        r1.setTag(r12);
    L_0x043b:
        r1 = r0.menuItem;
        r1.setVisibility(r14);
        r1 = r0.imagesArrLocals;
        r1.addAll(r4);
        r1 = r0.imagesArrLocals;
        r1 = r1.get(r5);
        r3 = r1 instanceof org.telegram.messenger.MediaController.PhotoEntry;
        if (r3 == 0) goto L_0x04a3;
    L_0x044f:
        r3 = r0.sendPhotoType;
        r4 = 10;
        if (r3 != r4) goto L_0x0460;
    L_0x0455:
        r1 = r0.cropItem;
        r1.setVisibility(r14);
        r1 = r0.rotateItem;
        r1.setVisibility(r14);
        goto L_0x04a1;
    L_0x0460:
        r1 = (org.telegram.messenger.MediaController.PhotoEntry) r1;
        r1 = r1.isVideo;
        if (r1 == 0) goto L_0x0489;
    L_0x0466:
        r1 = r0.cropItem;
        r1.setVisibility(r14);
        r1 = r0.rotateItem;
        r1.setVisibility(r14);
        r1 = r0.bottomLayout;
        r3 = 0;
        r1.setVisibility(r3);
        r1 = r0.bottomLayout;
        r1.setTag(r12);
        r1 = r0.bottomLayout;
        r3 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = -r3;
        r3 = (float) r3;
        r1.setTranslationY(r3);
        goto L_0x04a1;
    L_0x0489:
        r1 = r0.cropItem;
        if (r3 == r11) goto L_0x048f;
    L_0x048d:
        r3 = 0;
        goto L_0x0491;
    L_0x048f:
        r3 = 8;
    L_0x0491:
        r1.setVisibility(r3);
        r1 = r0.rotateItem;
        r3 = r0.sendPhotoType;
        if (r3 == r11) goto L_0x049d;
    L_0x049a:
        r3 = 8;
        goto L_0x049e;
    L_0x049d:
        r3 = 0;
    L_0x049e:
        r1.setVisibility(r3);
    L_0x04a1:
        r8 = 1;
        goto L_0x04d4;
    L_0x04a3:
        r3 = r1 instanceof org.telegram.tgnet.TLRPC.BotInlineResult;
        if (r3 == 0) goto L_0x04b3;
    L_0x04a7:
        r1 = r0.cropItem;
        r1.setVisibility(r14);
        r1 = r0.rotateItem;
        r1.setVisibility(r14);
    L_0x04b1:
        r8 = 0;
        goto L_0x04d4;
    L_0x04b3:
        r3 = r0.cropItem;
        r4 = r1 instanceof org.telegram.messenger.MediaController.SearchImage;
        if (r4 == 0) goto L_0x04c1;
    L_0x04b9:
        r1 = (org.telegram.messenger.MediaController.SearchImage) r1;
        r1 = r1.type;
        if (r1 != 0) goto L_0x04c1;
    L_0x04bf:
        r1 = 0;
        goto L_0x04c3;
    L_0x04c1:
        r1 = 8;
    L_0x04c3:
        r3.setVisibility(r1);
        r1 = r0.rotateItem;
        r1.setVisibility(r14);
        r1 = r0.cropItem;
        r1 = r1.getVisibility();
        if (r1 != 0) goto L_0x04b1;
    L_0x04d3:
        goto L_0x04a1;
    L_0x04d4:
        r1 = r0.parentChatActivity;
        if (r1 == 0) goto L_0x052c;
    L_0x04d8:
        r1 = r1.currentEncryptedChat;
        if (r1 == 0) goto L_0x04e6;
    L_0x04dc:
        r1 = r1.layer;
        r1 = org.telegram.messenger.AndroidUtilities.getPeerLayerVersion(r1);
        r3 = 46;
        if (r1 < r3) goto L_0x052c;
    L_0x04e6:
        r1 = r0.mentionsAdapter;
        r3 = r0.parentChatActivity;
        r3 = r3.chatInfo;
        r1.setChatInfo(r3);
        r1 = r0.mentionsAdapter;
        r3 = r0.parentChatActivity;
        r3 = r3.currentChat;
        if (r3 == 0) goto L_0x04f9;
    L_0x04f7:
        r3 = 1;
        goto L_0x04fa;
    L_0x04f9:
        r3 = 0;
    L_0x04fa:
        r1.setNeedUsernames(r3);
        r1 = r0.mentionsAdapter;
        r3 = 0;
        r1.setNeedBotContext(r3);
        if (r8 == 0) goto L_0x0513;
    L_0x0505:
        r1 = r0.placeProvider;
        if (r1 == 0) goto L_0x0511;
    L_0x0509:
        if (r1 == 0) goto L_0x0513;
    L_0x050b:
        r1 = r1.allowCaption();
        if (r1 == 0) goto L_0x0513;
    L_0x0511:
        r1 = 1;
        goto L_0x0514;
    L_0x0513:
        r1 = 0;
    L_0x0514:
        r0.needCaptionLayout = r1;
        r1 = r0.captionEditText;
        r3 = r0.needCaptionLayout;
        if (r3 == 0) goto L_0x051e;
    L_0x051c:
        r3 = 0;
        goto L_0x0520;
    L_0x051e:
        r3 = 8;
    L_0x0520:
        r1.setVisibility(r3);
        r1 = r0.needCaptionLayout;
        if (r1 == 0) goto L_0x052c;
    L_0x0527:
        r1 = r0.captionEditText;
        r1.onCreate();
    L_0x052c:
        r1 = r0.pickerView;
        r3 = 0;
        r1.setVisibility(r3);
        r1 = r0.pickerViewSendButton;
        r1.setVisibility(r3);
        r1 = r0.pickerViewSendButton;
        r3 = 0;
        r1.setTranslationY(r3);
        r1 = r0.pickerViewSendButton;
        r3 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r1.setAlpha(r3);
        r1 = r0.bottomLayout;
        r1.setVisibility(r14);
        r1 = r0.bottomLayout;
        r3 = 0;
        r1.setTag(r3);
        r1 = r0.containerView;
        r1.setTag(r3);
        r0.setImageIndex(r5, r11);
        r1 = r0.sendPhotoType;
        if (r1 != r11) goto L_0x0567;
    L_0x055b:
        r1 = r0.paintItem;
        r2 = 0;
        r1.setVisibility(r2);
        r1 = r0.tuneItem;
        r1.setVisibility(r2);
        goto L_0x058c;
    L_0x0567:
        if (r1 == r9) goto L_0x0582;
    L_0x0569:
        if (r1 == r2) goto L_0x0582;
    L_0x056b:
        r1 = r0.paintItem;
        r2 = r0.cropItem;
        r2 = r2.getVisibility();
        r1.setVisibility(r2);
        r1 = r0.tuneItem;
        r2 = r0.cropItem;
        r2 = r2.getVisibility();
        r1.setVisibility(r2);
        goto L_0x058c;
    L_0x0582:
        r1 = r0.paintItem;
        r1.setVisibility(r14);
        r1 = r0.tuneItem;
        r1.setVisibility(r14);
    L_0x058c:
        r26.updateSelectedCount();
        goto L_0x0591;
    L_0x0590:
        r3 = 0;
    L_0x0591:
        r1 = r0.currentAnimation;
        if (r1 != 0) goto L_0x0601;
    L_0x0595:
        r1 = r0.isEvent;
        if (r1 != 0) goto L_0x0601;
    L_0x0599:
        r1 = r0.currentDialogId;
        r4 = (r1 > r16 ? 1 : (r1 == r16 ? 0 : -1));
        if (r4 == 0) goto L_0x05e6;
    L_0x059f:
        r1 = r0.totalImagesCount;
        if (r1 != 0) goto L_0x05e6;
    L_0x05a3:
        r1 = r0.currentMessageObject;
        if (r1 == 0) goto L_0x05e6;
    L_0x05a7:
        r1 = r1.scheduled;
        if (r1 != 0) goto L_0x05e6;
    L_0x05ab:
        r1 = r0.currentAccount;
        r1 = org.telegram.messenger.MediaDataController.getInstance(r1);
        r6 = r0.currentDialogId;
        r2 = r0.sharedMediaType;
        r4 = r0.classGuid;
        r8 = 1;
        r27 = r1;
        r28 = r6;
        r30 = r2;
        r31 = r4;
        r32 = r8;
        r27.getMediaCount(r28, r30, r31, r32);
        r1 = r0.mergeDialogId;
        r4 = (r1 > r16 ? 1 : (r1 == r16 ? 0 : -1));
        if (r4 == 0) goto L_0x0601;
    L_0x05cb:
        r1 = r0.currentAccount;
        r1 = org.telegram.messenger.MediaDataController.getInstance(r1);
        r6 = r0.mergeDialogId;
        r2 = r0.sharedMediaType;
        r4 = r0.classGuid;
        r8 = 1;
        r27 = r1;
        r28 = r6;
        r30 = r2;
        r31 = r4;
        r32 = r8;
        r27.getMediaCount(r28, r30, r31, r32);
        goto L_0x0601;
    L_0x05e6:
        r1 = r0.avatarsDialogId;
        if (r1 == 0) goto L_0x0601;
    L_0x05ea:
        r1 = r0.currentAccount;
        r19 = org.telegram.messenger.MessagesController.getInstance(r1);
        r1 = r0.avatarsDialogId;
        r21 = 80;
        r22 = 0;
        r24 = 1;
        r2 = r0.classGuid;
        r20 = r1;
        r25 = r2;
        r19.loadDialogPhotos(r20, r21, r22, r24, r25);
    L_0x0601:
        r1 = r0.currentMessageObject;
        if (r1 == 0) goto L_0x060e;
    L_0x0605:
        r1 = r1.isVideo();
        if (r1 != 0) goto L_0x060c;
    L_0x060b:
        goto L_0x060e;
    L_0x060c:
        r1 = 0;
        goto L_0x0627;
    L_0x060e:
        r1 = r0.currentBotInlineResult;
        if (r1 == 0) goto L_0x062c;
    L_0x0612:
        r1 = r1.type;
        r2 = "video";
        r1 = r1.equals(r2);
        if (r1 != 0) goto L_0x060c;
    L_0x061c:
        r1 = r0.currentBotInlineResult;
        r1 = r1.document;
        r1 = org.telegram.messenger.MessageObject.isVideoDocument(r1);
        if (r1 == 0) goto L_0x062c;
    L_0x0626:
        goto L_0x060c;
    L_0x0627:
        r0.onActionClick(r1);
        goto L_0x06a2;
    L_0x062c:
        r1 = r0.imagesArrLocals;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x06a2;
    L_0x0634:
        r1 = r0.imagesArrLocals;
        r1 = r1.get(r5);
        r2 = r0.parentChatActivity;
        if (r2 == 0) goto L_0x0643;
    L_0x063e:
        r7 = r2.getCurrentUser();
        goto L_0x0644;
    L_0x0643:
        r7 = r3;
    L_0x0644:
        r2 = r0.parentChatActivity;
        if (r2 == 0) goto L_0x066c;
    L_0x0648:
        r2 = r2.isSecretChat();
        if (r2 != 0) goto L_0x066c;
    L_0x064e:
        r2 = r0.parentChatActivity;
        r2 = r2.isInScheduleMode();
        if (r2 != 0) goto L_0x066c;
    L_0x0656:
        if (r7 == 0) goto L_0x066c;
    L_0x0658:
        r2 = r7.bot;
        if (r2 != 0) goto L_0x066c;
    L_0x065c:
        r2 = org.telegram.messenger.UserObject.isUserSelf(r7);
        if (r2 != 0) goto L_0x066c;
    L_0x0662:
        r2 = r0.parentChatActivity;
        r2 = r2.isEditingMessageMedia();
        if (r2 != 0) goto L_0x066c;
    L_0x066a:
        r8 = 1;
        goto L_0x066d;
    L_0x066c:
        r8 = 0;
    L_0x066d:
        r2 = r1 instanceof org.telegram.tgnet.TLRPC.BotInlineResult;
        if (r2 == 0) goto L_0x0673;
    L_0x0671:
        r8 = 0;
        goto L_0x069a;
    L_0x0673:
        r2 = r1 instanceof org.telegram.messenger.MediaController.PhotoEntry;
        if (r2 == 0) goto L_0x068d;
    L_0x0677:
        r1 = (org.telegram.messenger.MediaController.PhotoEntry) r1;
        r2 = r1.isVideo;
        if (r2 == 0) goto L_0x069a;
    L_0x067d:
        r2 = new java.io.File;
        r1 = r1.path;
        r2.<init>(r1);
        r1 = android.net.Uri.fromFile(r2);
        r2 = 0;
        r0.preparePlayer(r1, r2, r2);
        goto L_0x069a;
    L_0x068d:
        if (r8 == 0) goto L_0x069a;
    L_0x068f:
        r2 = r1 instanceof org.telegram.messenger.MediaController.SearchImage;
        if (r2 == 0) goto L_0x069a;
    L_0x0693:
        r1 = (org.telegram.messenger.MediaController.SearchImage) r1;
        r1 = r1.type;
        if (r1 != 0) goto L_0x0671;
    L_0x0699:
        r8 = 1;
    L_0x069a:
        if (r8 == 0) goto L_0x06a2;
    L_0x069c:
        r1 = r0.timeItem;
        r2 = 0;
        r1.setVisibility(r2);
    L_0x06a2:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.onPhotoShow(org.telegram.messenger.MessageObject, org.telegram.tgnet.TLRPC$FileLocation, org.telegram.messenger.ImageLocation, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, int, org.telegram.ui.PhotoViewer$PlaceProviderObject):void");
    }

    private void setDoubleTapEnabled(boolean z) {
        this.doubleTapEnabled = z;
        this.gestureDetector.setOnDoubleTapListener(z ? this : null);
    }

    public boolean isMuteVideo() {
        return this.muteVideo;
    }

    private void setImages() {
        if (this.animationInProgress == 0) {
            setIndexToImage(this.centerImage, this.currentIndex);
            setIndexToImage(this.rightImage, this.currentIndex + 1);
            setIndexToImage(this.leftImage, this.currentIndex - 1);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00ec  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00c9  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0109  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0102  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x017f  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x06bc  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x06e6  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x06ca  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x070e  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0766  */
    /* JADX WARNING: Removed duplicated region for block: B:307:0x0764  */
    /* JADX WARNING: Removed duplicated region for block: B:312:0x0770  */
    /* JADX WARNING: Removed duplicated region for block: B:311:0x076e  */
    /* JADX WARNING: Removed duplicated region for block: B:316:0x077a  */
    /* JADX WARNING: Removed duplicated region for block: B:315:0x0778  */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x0783  */
    private void setIsAboutToSwitchToIndex(int r27, boolean r28) {
        /*
        r26 = this;
        r0 = r26;
        r1 = r27;
        if (r28 != 0) goto L_0x000b;
    L_0x0006:
        r2 = r0.switchingToIndex;
        if (r2 != r1) goto L_0x000b;
    L_0x000a:
        return;
    L_0x000b:
        r0.switchingToIndex = r1;
        r2 = r26.getFileName(r27);
        r3 = r0.imagesArr;
        r3 = r3.isEmpty();
        r4 = "AttachVideo";
        r6 = "AttachPhoto";
        r8 = 6;
        r10 = "Of";
        r11 = "";
        r13 = 2;
        r14 = 0;
        r15 = 8;
        r9 = 0;
        if (r3 != 0) goto L_0x0471;
    L_0x0027:
        r1 = r0.switchingToIndex;
        if (r1 < 0) goto L_0x0470;
    L_0x002b:
        r3 = r0.imagesArr;
        r3 = r3.size();
        if (r1 < r3) goto L_0x0035;
    L_0x0033:
        goto L_0x0470;
    L_0x0035:
        r1 = r0.imagesArr;
        r3 = r0.switchingToIndex;
        r1 = r1.get(r3);
        r1 = (org.telegram.messenger.MessageObject) r1;
        r3 = r1.isVideo();
        r16 = r1.isInvoice();
        r7 = 11;
        if (r16 == 0) goto L_0x007e;
    L_0x004b:
        r2 = r0.masksItem;
        r2.setVisibility(r15);
        r2 = r0.menuItem;
        r2.hideSubItem(r8);
        r2 = r0.menuItem;
        r2.hideSubItem(r7);
        r2 = r1.messageOwner;
        r2 = r2.media;
        r2 = r2.description;
        r0.allowShare = r9;
        r3 = r0.bottomLayout;
        r7 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r7 = (float) r7;
        r3.setTranslationY(r7);
        r3 = r0.captionTextView;
        r7 = NUM; // 0x42400000 float:48.0 double:5.491493014E-315;
        r7 = org.telegram.messenger.AndroidUtilities.dp(r7);
        r7 = (float) r7;
        r3.setTranslationY(r7);
        r17 = r6;
        goto L_0x01df;
    L_0x007e:
        r12 = r0.masksItem;
        r17 = r1.hasPhotoStickers();
        if (r17 == 0) goto L_0x0091;
    L_0x0086:
        r17 = r6;
        r5 = r1.getDialogId();
        r6 = (int) r5;
        if (r6 == 0) goto L_0x0093;
    L_0x008f:
        r5 = 0;
        goto L_0x0095;
    L_0x0091:
        r17 = r6;
    L_0x0093:
        r5 = 8;
    L_0x0095:
        r12.setVisibility(r5);
        r5 = r1.isNewGif();
        if (r5 == 0) goto L_0x00a5;
    L_0x009e:
        r5 = r0.menuItem;
        r6 = 14;
        r5.showSubItem(r6);
    L_0x00a5:
        r5 = r0.parentChatActivity;
        if (r5 == 0) goto L_0x00b1;
    L_0x00a9:
        r5 = r5.isInScheduleMode();
        if (r5 == 0) goto L_0x00b1;
    L_0x00af:
        r5 = 1;
        goto L_0x00b2;
    L_0x00b1:
        r5 = 0;
    L_0x00b2:
        r5 = r1.canDeleteMessage(r5, r14);
        if (r5 == 0) goto L_0x00c2;
    L_0x00b8:
        r5 = r0.slideshowMessageId;
        if (r5 != 0) goto L_0x00c2;
    L_0x00bc:
        r5 = r0.menuItem;
        r5.showSubItem(r8);
        goto L_0x00c7;
    L_0x00c2:
        r5 = r0.menuItem;
        r5.hideSubItem(r8);
    L_0x00c7:
        if (r3 == 0) goto L_0x00ec;
    L_0x00c9:
        r5 = r0.menuItem;
        r5.showSubItem(r7);
        r5 = r0.pipItem;
        r5 = r5.getVisibility();
        if (r5 == 0) goto L_0x00db;
    L_0x00d6:
        r5 = r0.pipItem;
        r5.setVisibility(r9);
    L_0x00db:
        r5 = r0.pipAvailable;
        if (r5 != 0) goto L_0x00fe;
    L_0x00df:
        r5 = r0.pipItem;
        r5.setEnabled(r9);
        r5 = r0.pipItem;
        r6 = NUM; // 0x3var_ float:0.5 double:5.222099017E-315;
        r5.setAlpha(r6);
        goto L_0x00fe;
    L_0x00ec:
        r5 = r0.menuItem;
        r5.hideSubItem(r7);
        r5 = r0.pipItem;
        r5 = r5.getVisibility();
        if (r5 == r15) goto L_0x00fe;
    L_0x00f9:
        r5 = r0.pipItem;
        r5.setVisibility(r15);
    L_0x00fe:
        r5 = r0.nameOverride;
        if (r5 == 0) goto L_0x0109;
    L_0x0102:
        r6 = r0.nameTextView;
        r6.setText(r5);
        goto L_0x017a;
    L_0x0109:
        r5 = r1.isFromUser();
        if (r5 == 0) goto L_0x0133;
    L_0x010f:
        r5 = r0.currentAccount;
        r5 = org.telegram.messenger.MessagesController.getInstance(r5);
        r6 = r1.messageOwner;
        r6 = r6.from_id;
        r6 = java.lang.Integer.valueOf(r6);
        r5 = r5.getUser(r6);
        if (r5 == 0) goto L_0x012d;
    L_0x0123:
        r6 = r0.nameTextView;
        r5 = org.telegram.messenger.UserObject.getUserName(r5);
        r6.setText(r5);
        goto L_0x017a;
    L_0x012d:
        r5 = r0.nameTextView;
        r5.setText(r11);
        goto L_0x017a;
    L_0x0133:
        r5 = r0.currentAccount;
        r5 = org.telegram.messenger.MessagesController.getInstance(r5);
        r6 = r1.messageOwner;
        r6 = r6.to_id;
        r6 = r6.channel_id;
        r6 = java.lang.Integer.valueOf(r6);
        r5 = r5.getChat(r6);
        r6 = org.telegram.messenger.ChatObject.isChannel(r5);
        if (r6 == 0) goto L_0x016b;
    L_0x014d:
        r6 = r5.megagroup;
        if (r6 == 0) goto L_0x016b;
    L_0x0151:
        r6 = r1.isForwardedChannelPost();
        if (r6 == 0) goto L_0x016b;
    L_0x0157:
        r5 = r0.currentAccount;
        r5 = org.telegram.messenger.MessagesController.getInstance(r5);
        r6 = r1.messageOwner;
        r6 = r6.fwd_from;
        r6 = r6.channel_id;
        r6 = java.lang.Integer.valueOf(r6);
        r5 = r5.getChat(r6);
    L_0x016b:
        if (r5 == 0) goto L_0x0175;
    L_0x016d:
        r6 = r0.nameTextView;
        r5 = r5.title;
        r6.setText(r5);
        goto L_0x017a;
    L_0x0175:
        r5 = r0.nameTextView;
        r5.setText(r11);
    L_0x017a:
        r5 = r0.dateOverride;
        if (r5 == 0) goto L_0x017f;
    L_0x017e:
        goto L_0x0183;
    L_0x017f:
        r5 = r1.messageOwner;
        r5 = r5.date;
    L_0x0183:
        r5 = (long) r5;
        r7 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r5 = r5 * r7;
        r7 = NUM; // 0x7f0e0CLASSNAME float:1.8881573E38 double:1.0531637485E-314;
        r8 = new java.lang.Object[r13];
        r11 = org.telegram.messenger.LocaleController.getInstance();
        r11 = r11.formatterYear;
        r12 = new java.util.Date;
        r12.<init>(r5);
        r11 = r11.format(r12);
        r8[r9] = r11;
        r11 = org.telegram.messenger.LocaleController.getInstance();
        r11 = r11.formatterDay;
        r12 = new java.util.Date;
        r12.<init>(r5);
        r5 = r11.format(r12);
        r6 = 1;
        r8[r6] = r5;
        r5 = "formatDateAtTime";
        r5 = org.telegram.messenger.LocaleController.formatString(r5, r7, r8);
        if (r2 == 0) goto L_0x01d8;
    L_0x01b8:
        if (r3 == 0) goto L_0x01d8;
    L_0x01ba:
        r2 = r0.dateTextView;
        r3 = new java.lang.Object[r13];
        r3[r9] = r5;
        r5 = r1.getDocument();
        r5 = r5.size;
        r5 = (long) r5;
        r5 = org.telegram.messenger.AndroidUtilities.formatFileSize(r5);
        r6 = 1;
        r3[r6] = r5;
        r5 = "%s (%s)";
        r3 = java.lang.String.format(r5, r3);
        r2.setText(r3);
        goto L_0x01dd;
    L_0x01d8:
        r2 = r0.dateTextView;
        r2.setText(r5);
    L_0x01dd:
        r2 = r1.caption;
    L_0x01df:
        r3 = r0.currentAnimation;
        if (r3 == 0) goto L_0x0220;
    L_0x01e3:
        r3 = r0.menuItem;
        r4 = 1;
        r3.hideSubItem(r4);
        r3 = r0.menuItem;
        r4 = 10;
        r3.hideSubItem(r4);
        r3 = r0.parentChatActivity;
        if (r3 == 0) goto L_0x01fc;
    L_0x01f4:
        r3 = r3.isInScheduleMode();
        if (r3 == 0) goto L_0x01fc;
    L_0x01fa:
        r3 = 1;
        goto L_0x01fd;
    L_0x01fc:
        r3 = 0;
    L_0x01fd:
        r3 = r1.canDeleteMessage(r3, r14);
        if (r3 != 0) goto L_0x0208;
    L_0x0203:
        r3 = r0.menuItem;
        r3.setVisibility(r15);
    L_0x0208:
        r3 = 1;
        r0.allowShare = r3;
        r3 = r0.shareButton;
        r3.setVisibility(r9);
        r3 = r0.actionBar;
        r4 = NUM; // 0x7f0e014b float:1.8875709E38 double:1.05316232E-314;
        r5 = "AttachGif";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        r3.setTitle(r4);
        goto L_0x0468;
    L_0x0220:
        r3 = r0.totalImagesCount;
        r5 = r0.totalImagesCountMerge;
        r3 = r3 + r5;
        if (r3 == 0) goto L_0x039a;
    L_0x0227:
        r3 = r0.needSearchImageInArr;
        if (r3 != 0) goto L_0x039a;
    L_0x022b:
        r3 = r0.opennedFromMedia;
        if (r3 == 0) goto L_0x02e8;
    L_0x022f:
        r3 = r0.imagesArr;
        r3 = r3.size();
        r4 = r0.totalImagesCount;
        r5 = r0.totalImagesCountMerge;
        r4 = r4 + r5;
        if (r3 >= r4) goto L_0x02c3;
    L_0x023c:
        r3 = r0.loadingMoreImages;
        if (r3 != 0) goto L_0x02c3;
    L_0x0240:
        r3 = r0.switchingToIndex;
        r4 = r0.imagesArr;
        r4 = r4.size();
        r5 = 5;
        r4 = r4 - r5;
        if (r3 <= r4) goto L_0x02c3;
    L_0x024c:
        r3 = r0.imagesArr;
        r3 = r3.isEmpty();
        if (r3 == 0) goto L_0x0256;
    L_0x0254:
        r3 = 0;
        goto L_0x0268;
    L_0x0256:
        r3 = r0.imagesArr;
        r4 = r3.size();
        r5 = 1;
        r4 = r4 - r5;
        r3 = r3.get(r4);
        r3 = (org.telegram.messenger.MessageObject) r3;
        r3 = r3.getId();
    L_0x0268:
        r4 = r0.endReached;
        r4 = r4[r9];
        if (r4 == 0) goto L_0x029e;
    L_0x026e:
        r4 = r0.mergeDialogId;
        r6 = 0;
        r8 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r8 == 0) goto L_0x029e;
    L_0x0276:
        r4 = r0.imagesArr;
        r4 = r4.isEmpty();
        if (r4 != 0) goto L_0x029a;
    L_0x027e:
        r4 = r0.imagesArr;
        r5 = r4.size();
        r6 = 1;
        r5 = r5 - r6;
        r4 = r4.get(r5);
        r4 = (org.telegram.messenger.MessageObject) r4;
        r4 = r4.getDialogId();
        r6 = r0.mergeDialogId;
        r8 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r8 == 0) goto L_0x029a;
    L_0x0296:
        r3 = 1;
        r22 = 0;
        goto L_0x02a1;
    L_0x029a:
        r22 = r3;
        r3 = 1;
        goto L_0x02a1;
    L_0x029e:
        r22 = r3;
        r3 = 0;
    L_0x02a1:
        r4 = r0.currentAccount;
        r18 = org.telegram.messenger.MediaDataController.getInstance(r4);
        if (r3 != 0) goto L_0x02ac;
    L_0x02a9:
        r3 = r0.currentDialogId;
        goto L_0x02ae;
    L_0x02ac:
        r3 = r0.mergeDialogId;
    L_0x02ae:
        r19 = r3;
        r21 = 80;
        r3 = r0.sharedMediaType;
        r24 = 1;
        r4 = r0.classGuid;
        r23 = r3;
        r25 = r4;
        r18.loadMedia(r19, r21, r22, r23, r24, r25);
        r3 = 1;
        r0.loadingMoreImages = r3;
        goto L_0x02c4;
    L_0x02c3:
        r3 = 1;
    L_0x02c4:
        r4 = r0.actionBar;
        r5 = new java.lang.Object[r13];
        r6 = r0.switchingToIndex;
        r6 = r6 + r3;
        r6 = java.lang.Integer.valueOf(r6);
        r5[r9] = r6;
        r6 = r0.totalImagesCount;
        r7 = r0.totalImagesCountMerge;
        r6 = r6 + r7;
        r6 = java.lang.Integer.valueOf(r6);
        r5[r3] = r6;
        r3 = NUM; // 0x7f0e0733 float:1.8878776E38 double:1.053163067E-314;
        r3 = org.telegram.messenger.LocaleController.formatString(r10, r3, r5);
        r4.setTitle(r3);
        goto L_0x0412;
    L_0x02e8:
        r3 = r0.imagesArr;
        r3 = r3.size();
        r4 = r0.totalImagesCount;
        r5 = r0.totalImagesCountMerge;
        r4 = r4 + r5;
        if (r3 >= r4) goto L_0x0368;
    L_0x02f5:
        r3 = r0.loadingMoreImages;
        if (r3 != 0) goto L_0x0368;
    L_0x02f9:
        r3 = r0.switchingToIndex;
        r4 = 5;
        if (r3 >= r4) goto L_0x0368;
    L_0x02fe:
        r3 = r0.imagesArr;
        r3 = r3.isEmpty();
        if (r3 == 0) goto L_0x0308;
    L_0x0306:
        r3 = 0;
        goto L_0x0314;
    L_0x0308:
        r3 = r0.imagesArr;
        r3 = r3.get(r9);
        r3 = (org.telegram.messenger.MessageObject) r3;
        r3 = r3.getId();
    L_0x0314:
        r4 = r0.endReached;
        r4 = r4[r9];
        if (r4 == 0) goto L_0x0344;
    L_0x031a:
        r4 = r0.mergeDialogId;
        r6 = 0;
        r8 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r8 == 0) goto L_0x0344;
    L_0x0322:
        r4 = r0.imagesArr;
        r4 = r4.isEmpty();
        if (r4 != 0) goto L_0x0340;
    L_0x032a:
        r4 = r0.imagesArr;
        r4 = r4.get(r9);
        r4 = (org.telegram.messenger.MessageObject) r4;
        r4 = r4.getDialogId();
        r6 = r0.mergeDialogId;
        r8 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r8 == 0) goto L_0x0340;
    L_0x033c:
        r3 = 1;
        r22 = 0;
        goto L_0x0347;
    L_0x0340:
        r22 = r3;
        r3 = 1;
        goto L_0x0347;
    L_0x0344:
        r22 = r3;
        r3 = 0;
    L_0x0347:
        r4 = r0.currentAccount;
        r18 = org.telegram.messenger.MediaDataController.getInstance(r4);
        if (r3 != 0) goto L_0x0352;
    L_0x034f:
        r3 = r0.currentDialogId;
        goto L_0x0354;
    L_0x0352:
        r3 = r0.mergeDialogId;
    L_0x0354:
        r19 = r3;
        r21 = 80;
        r3 = r0.sharedMediaType;
        r24 = 1;
        r4 = r0.classGuid;
        r23 = r3;
        r25 = r4;
        r18.loadMedia(r19, r21, r22, r23, r24, r25);
        r3 = 1;
        r0.loadingMoreImages = r3;
    L_0x0368:
        r3 = r0.actionBar;
        r4 = new java.lang.Object[r13];
        r5 = r0.totalImagesCount;
        r6 = r0.totalImagesCountMerge;
        r5 = r5 + r6;
        r6 = r0.imagesArr;
        r6 = r6.size();
        r5 = r5 - r6;
        r6 = r0.switchingToIndex;
        r5 = r5 + r6;
        r6 = 1;
        r5 = r5 + r6;
        r5 = java.lang.Integer.valueOf(r5);
        r4[r9] = r5;
        r5 = r0.totalImagesCount;
        r7 = r0.totalImagesCountMerge;
        r5 = r5 + r7;
        r5 = java.lang.Integer.valueOf(r5);
        r4[r6] = r5;
        r5 = NUM; // 0x7f0e0733 float:1.8878776E38 double:1.053163067E-314;
        r4 = org.telegram.messenger.LocaleController.formatString(r10, r5, r4);
        r3.setTitle(r4);
        goto L_0x0412;
    L_0x039a:
        r3 = r0.slideshowMessageId;
        if (r3 != 0) goto L_0x03dd;
    L_0x039e:
        r3 = r1.messageOwner;
        r3 = r3.media;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
        if (r3 == 0) goto L_0x03dd;
    L_0x03a6:
        r3 = r1.canPreviewDocument();
        if (r3 == 0) goto L_0x03bb;
    L_0x03ac:
        r3 = r0.actionBar;
        r4 = NUM; // 0x7f0e0149 float:1.8875705E38 double:1.053162319E-314;
        r5 = "AttachDocument";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        r3.setTitle(r4);
        goto L_0x0412;
    L_0x03bb:
        r3 = r1.isVideo();
        if (r3 == 0) goto L_0x03ce;
    L_0x03c1:
        r3 = r0.actionBar;
        r5 = NUM; // 0x7f0e015c float:1.8875743E38 double:1.0531623286E-314;
        r4 = org.telegram.messenger.LocaleController.getString(r4, r5);
        r3.setTitle(r4);
        goto L_0x0412;
    L_0x03ce:
        r3 = r0.actionBar;
        r5 = r17;
        r4 = NUM; // 0x7f0e0156 float:1.8875731E38 double:1.0531623256E-314;
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        r3.setTitle(r4);
        goto L_0x0412;
    L_0x03dd:
        if (r16 == 0) goto L_0x03eb;
    L_0x03df:
        r3 = r0.actionBar;
        r4 = r1.messageOwner;
        r4 = r4.media;
        r4 = r4.title;
        r3.setTitle(r4);
        goto L_0x0412;
    L_0x03eb:
        r3 = r1.isVideo();
        if (r3 == 0) goto L_0x03fe;
    L_0x03f1:
        r3 = r0.actionBar;
        r5 = NUM; // 0x7f0e015c float:1.8875743E38 double:1.0531623286E-314;
        r4 = org.telegram.messenger.LocaleController.getString(r4, r5);
        r3.setTitle(r4);
        goto L_0x0412;
    L_0x03fe:
        r3 = r1.getDocument();
        if (r3 == 0) goto L_0x0412;
    L_0x0404:
        r3 = r0.actionBar;
        r4 = NUM; // 0x7f0e0149 float:1.8875705E38 double:1.053162319E-314;
        r5 = "AttachDocument";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        r3.setTitle(r4);
    L_0x0412:
        r3 = r0.currentDialogId;
        r4 = (int) r3;
        if (r4 != 0) goto L_0x041c;
    L_0x0417:
        r3 = r0.sendItem;
        r3.setVisibility(r15);
    L_0x041c:
        r3 = r1.messageOwner;
        r3 = r3.ttl;
        if (r3 == 0) goto L_0x043b;
    L_0x0422:
        r4 = 3600; // 0xe10 float:5.045E-42 double:1.7786E-320;
        if (r3 >= r4) goto L_0x043b;
    L_0x0426:
        r0.allowShare = r9;
        r3 = r0.menuItem;
        r4 = 1;
        r3.hideSubItem(r4);
        r3 = r0.shareButton;
        r3.setVisibility(r15);
        r3 = r0.menuItem;
        r5 = 10;
        r3.hideSubItem(r5);
        goto L_0x0468;
    L_0x043b:
        r4 = 1;
        r0.allowShare = r4;
        r3 = r0.menuItem;
        r3.showSubItem(r4);
        r3 = r0.shareButton;
        r4 = r0.videoPlayerControlFrameLayout;
        r4 = r4.getVisibility();
        if (r4 == 0) goto L_0x044e;
    L_0x044d:
        r15 = 0;
    L_0x044e:
        r3.setVisibility(r15);
        r3 = r0.shareButton;
        r3 = r3.getVisibility();
        if (r3 != 0) goto L_0x0461;
    L_0x0459:
        r3 = r0.menuItem;
        r4 = 10;
        r3.hideSubItem(r4);
        goto L_0x0468;
    L_0x0461:
        r4 = 10;
        r3 = r0.menuItem;
        r3.showSubItem(r4);
    L_0x0468:
        r3 = r0.groupedPhotosListView;
        r3.fillList();
        r14 = r1;
        goto L_0x078b;
    L_0x0470:
        return;
    L_0x0471:
        r5 = r6;
        r2 = r0.secureDocuments;
        r2 = r2.isEmpty();
        if (r2 != 0) goto L_0x04b1;
    L_0x047a:
        r0.allowShare = r9;
        r1 = r0.menuItem;
        r2 = 1;
        r1.hideSubItem(r2);
        r1 = r0.nameTextView;
        r1.setText(r11);
        r1 = r0.dateTextView;
        r1.setText(r11);
        r1 = r0.actionBar;
        r3 = new java.lang.Object[r13];
        r4 = r0.switchingToIndex;
        r4 = r4 + r2;
        r4 = java.lang.Integer.valueOf(r4);
        r3[r9] = r4;
        r4 = r0.secureDocuments;
        r4 = r4.size();
        r4 = java.lang.Integer.valueOf(r4);
        r3[r2] = r4;
        r2 = NUM; // 0x7f0e0733 float:1.8878776E38 double:1.053163067E-314;
        r2 = org.telegram.messenger.LocaleController.formatString(r10, r2, r3);
        r1.setTitle(r2);
        goto L_0x078a;
    L_0x04b1:
        r2 = r0.imagesArrLocations;
        r2 = r2.isEmpty();
        if (r2 != 0) goto L_0x055a;
    L_0x04b9:
        if (r1 < 0) goto L_0x0559;
    L_0x04bb:
        r2 = r0.imagesArrLocations;
        r2 = r2.size();
        if (r1 < r2) goto L_0x04c5;
    L_0x04c3:
        goto L_0x0559;
    L_0x04c5:
        r1 = r0.nameTextView;
        r1.setText(r11);
        r1 = r0.dateTextView;
        r1.setText(r11);
        r1 = r0.avatarsDialogId;
        r2 = r0.currentAccount;
        r2 = org.telegram.messenger.UserConfig.getInstance(r2);
        r2 = r2.getClientUserId();
        if (r1 != r2) goto L_0x04eb;
    L_0x04dd:
        r1 = r0.avatarsArr;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x04eb;
    L_0x04e5:
        r1 = r0.menuItem;
        r1.showSubItem(r8);
        goto L_0x04f0;
    L_0x04eb:
        r1 = r0.menuItem;
        r1.hideSubItem(r8);
    L_0x04f0:
        r1 = r0.isEvent;
        if (r1 == 0) goto L_0x0502;
    L_0x04f4:
        r1 = r0.actionBar;
        r2 = NUM; // 0x7f0e0156 float:1.8875731E38 double:1.0531623256E-314;
        r2 = org.telegram.messenger.LocaleController.getString(r5, r2);
        r1.setTitle(r2);
        r4 = 1;
        goto L_0x0526;
    L_0x0502:
        r1 = r0.actionBar;
        r2 = new java.lang.Object[r13];
        r3 = r0.switchingToIndex;
        r4 = 1;
        r3 = r3 + r4;
        r3 = java.lang.Integer.valueOf(r3);
        r2[r9] = r3;
        r3 = r0.imagesArrLocations;
        r3 = r3.size();
        r3 = java.lang.Integer.valueOf(r3);
        r2[r4] = r3;
        r3 = NUM; // 0x7f0e0733 float:1.8878776E38 double:1.053163067E-314;
        r2 = org.telegram.messenger.LocaleController.formatString(r10, r3, r2);
        r1.setTitle(r2);
    L_0x0526:
        r1 = r0.menuItem;
        r1.showSubItem(r4);
        r0.allowShare = r4;
        r1 = r0.shareButton;
        r2 = r0.videoPlayerControlFrameLayout;
        r2 = r2.getVisibility();
        if (r2 == 0) goto L_0x0538;
    L_0x0537:
        r15 = 0;
    L_0x0538:
        r1.setVisibility(r15);
        r1 = r0.shareButton;
        r1 = r1.getVisibility();
        if (r1 != 0) goto L_0x054b;
    L_0x0543:
        r1 = r0.menuItem;
        r2 = 10;
        r1.hideSubItem(r2);
        goto L_0x0552;
    L_0x054b:
        r2 = 10;
        r1 = r0.menuItem;
        r1.showSubItem(r2);
    L_0x0552:
        r1 = r0.groupedPhotosListView;
        r1.fillList();
        goto L_0x078a;
    L_0x0559:
        return;
    L_0x055a:
        r2 = r0.imagesArrLocals;
        r2 = r2.isEmpty();
        if (r2 != 0) goto L_0x078a;
    L_0x0562:
        if (r1 < 0) goto L_0x0789;
    L_0x0564:
        r2 = r0.imagesArrLocals;
        r2 = r2.size();
        if (r1 < r2) goto L_0x056e;
    L_0x056c:
        goto L_0x0789;
    L_0x056e:
        r2 = r0.imagesArrLocals;
        r1 = r2.get(r1);
        r2 = r1 instanceof org.telegram.tgnet.TLRPC.BotInlineResult;
        if (r2 == 0) goto L_0x059e;
    L_0x0578:
        r2 = r1;
        r2 = (org.telegram.tgnet.TLRPC.BotInlineResult) r2;
        r0.currentBotInlineResult = r2;
        r3 = r2.document;
        if (r3 == 0) goto L_0x0586;
    L_0x0581:
        r2 = org.telegram.messenger.MessageObject.isVideoDocument(r3);
        goto L_0x0596;
    L_0x0586:
        r3 = r2.content;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_webDocument;
        if (r3 == 0) goto L_0x0595;
    L_0x058c:
        r2 = r2.type;
        r3 = "video";
        r2 = r2.equals(r3);
        goto L_0x0596;
    L_0x0595:
        r2 = 0;
    L_0x0596:
        r3 = r2;
    L_0x0597:
        r6 = r14;
        r2 = 0;
        r7 = 0;
        r8 = 0;
        r11 = 0;
        goto L_0x06b4;
    L_0x059e:
        r2 = r1 instanceof org.telegram.messenger.MediaController.PhotoEntry;
        if (r2 == 0) goto L_0x05ad;
    L_0x05a2:
        r3 = r1;
        r3 = (org.telegram.messenger.MediaController.PhotoEntry) r3;
        r6 = r3.path;
        r3 = r3.isVideo;
        r8 = r6;
        r6 = 0;
        r7 = 1;
        goto L_0x05c5;
    L_0x05ad:
        r3 = r1 instanceof org.telegram.messenger.MediaController.SearchImage;
        if (r3 == 0) goto L_0x05c1;
    L_0x05b1:
        r3 = r1;
        r3 = (org.telegram.messenger.MediaController.SearchImage) r3;
        r6 = r3.getPathToAttach();
        r3 = r3.type;
        r7 = 1;
        r8 = r6;
        if (r3 != r7) goto L_0x05c3;
    L_0x05be:
        r3 = 0;
        r6 = 1;
        goto L_0x05c5;
    L_0x05c1:
        r7 = 1;
        r8 = r14;
    L_0x05c3:
        r3 = 0;
        r6 = 0;
    L_0x05c5:
        if (r3 == 0) goto L_0x060b;
    L_0x05c7:
        r6 = r0.muteItem;
        r6.setVisibility(r9);
        r6 = r0.compressItem;
        r6.setVisibility(r9);
        r0.isCurrentVideo = r7;
        r26.updateAccessibilityOverlayVisibility();
        r6 = 0;
        r7 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        if (r2 == 0) goto L_0x05e9;
    L_0x05db:
        r11 = r1;
        r11 = (org.telegram.messenger.MediaController.PhotoEntry) r11;
        r11 = r11.editedInfo;
        if (r11 == 0) goto L_0x05e9;
    L_0x05e2:
        r6 = r11.muted;
        r7 = r11.start;
        r11 = r11.end;
        goto L_0x05ed;
    L_0x05e9:
        r6 = 0;
        r7 = 0;
        r11 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
    L_0x05ed:
        r0.processOpenVideo(r8, r6, r7, r11);
        r6 = r0.videoTimelineView;
        r6.setVisibility(r9);
        r6 = r0.paintItem;
        r6.setVisibility(r15);
        r6 = r0.cropItem;
        r6.setVisibility(r15);
        r6 = r0.tuneItem;
        r6.setVisibility(r15);
        r6 = r0.rotateItem;
        r6.setVisibility(r15);
        goto L_0x0679;
    L_0x060b:
        r7 = r0.videoTimelineView;
        r7.setVisibility(r15);
        r7 = r0.muteItem;
        r7.setVisibility(r15);
        r0.isCurrentVideo = r9;
        r26.updateAccessibilityOverlayVisibility();
        r7 = r0.compressItem;
        r7.setVisibility(r15);
        if (r6 != 0) goto L_0x0660;
    L_0x0621:
        r6 = r0.sendPhotoType;
        r7 = 10;
        if (r6 != r7) goto L_0x0628;
    L_0x0627:
        goto L_0x0660;
    L_0x0628:
        r7 = 4;
        if (r6 == r7) goto L_0x063a;
    L_0x062b:
        r7 = 5;
        if (r6 != r7) goto L_0x062f;
    L_0x062e:
        goto L_0x063a;
    L_0x062f:
        r6 = r0.paintItem;
        r6.setVisibility(r9);
        r6 = r0.tuneItem;
        r6.setVisibility(r9);
        goto L_0x0644;
    L_0x063a:
        r6 = r0.paintItem;
        r6.setVisibility(r15);
        r6 = r0.tuneItem;
        r6.setVisibility(r15);
    L_0x0644:
        r6 = r0.cropItem;
        r7 = r0.sendPhotoType;
        r8 = 1;
        if (r7 == r8) goto L_0x064d;
    L_0x064b:
        r7 = 0;
        goto L_0x064f;
    L_0x064d:
        r7 = 8;
    L_0x064f:
        r6.setVisibility(r7);
        r6 = r0.rotateItem;
        r7 = r0.sendPhotoType;
        if (r7 == r8) goto L_0x065b;
    L_0x0658:
        r7 = 8;
        goto L_0x065c;
    L_0x065b:
        r7 = 0;
    L_0x065c:
        r6.setVisibility(r7);
        goto L_0x0674;
    L_0x0660:
        r6 = r0.paintItem;
        r6.setVisibility(r15);
        r6 = r0.cropItem;
        r6.setVisibility(r15);
        r6 = r0.rotateItem;
        r6.setVisibility(r15);
        r6 = r0.tuneItem;
        r6.setVisibility(r15);
    L_0x0674:
        r6 = r0.actionBar;
        r6.setSubtitle(r14);
    L_0x0679:
        if (r2 == 0) goto L_0x06a3;
    L_0x067b:
        r2 = r1;
        r2 = (org.telegram.messenger.MediaController.PhotoEntry) r2;
        r6 = r2.bucketId;
        if (r6 != 0) goto L_0x0695;
    L_0x0682:
        r6 = r2.dateTaken;
        r11 = 0;
        r8 = (r6 > r11 ? 1 : (r6 == r11 ? 0 : -1));
        if (r8 != 0) goto L_0x0695;
    L_0x068a:
        r6 = r0.imagesArrLocals;
        r6 = r6.size();
        r7 = 1;
        if (r6 != r7) goto L_0x0695;
    L_0x0693:
        r6 = 1;
        goto L_0x0696;
    L_0x0695:
        r6 = 0;
    L_0x0696:
        r0.fromCamera = r6;
        r6 = r2.caption;
        r7 = r2.ttl;
        r8 = r2.isFiltered;
        r11 = r2.isPainted;
        r2 = r2.isCropped;
        goto L_0x06b4;
    L_0x06a3:
        r2 = r1 instanceof org.telegram.messenger.MediaController.SearchImage;
        if (r2 == 0) goto L_0x0597;
    L_0x06a7:
        r2 = r1;
        r2 = (org.telegram.messenger.MediaController.SearchImage) r2;
        r6 = r2.caption;
        r7 = r2.ttl;
        r8 = r2.isFiltered;
        r11 = r2.isPainted;
        r2 = r2.isCropped;
    L_0x06b4:
        r12 = r0.bottomLayout;
        r12 = r12.getVisibility();
        if (r12 == r15) goto L_0x06c1;
    L_0x06bc:
        r12 = r0.bottomLayout;
        r12.setVisibility(r15);
    L_0x06c1:
        r12 = r0.bottomLayout;
        r12.setTag(r14);
        r12 = r0.fromCamera;
        if (r12 == 0) goto L_0x06e6;
    L_0x06ca:
        if (r3 == 0) goto L_0x06d9;
    L_0x06cc:
        r3 = r0.actionBar;
        r5 = NUM; // 0x7f0e015c float:1.8875743E38 double:1.0531623286E-314;
        r4 = org.telegram.messenger.LocaleController.getString(r4, r5);
        r3.setTitle(r4);
        goto L_0x070a;
    L_0x06d9:
        r3 = r0.actionBar;
        r4 = NUM; // 0x7f0e0156 float:1.8875731E38 double:1.0531623256E-314;
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        r3.setTitle(r4);
        goto L_0x070a;
    L_0x06e6:
        r3 = r0.actionBar;
        r4 = new java.lang.Object[r13];
        r5 = r0.switchingToIndex;
        r12 = 1;
        r5 = r5 + r12;
        r5 = java.lang.Integer.valueOf(r5);
        r4[r9] = r5;
        r5 = r0.imagesArrLocals;
        r5 = r5.size();
        r5 = java.lang.Integer.valueOf(r5);
        r4[r12] = r5;
        r5 = NUM; // 0x7f0e0733 float:1.8878776E38 double:1.053163067E-314;
        r4 = org.telegram.messenger.LocaleController.formatString(r10, r5, r4);
        r3.setTitle(r4);
    L_0x070a:
        r3 = r0.parentChatActivity;
        if (r3 == 0) goto L_0x0731;
    L_0x070e:
        r3 = r3.getCurrentChat();
        if (r3 == 0) goto L_0x071c;
    L_0x0714:
        r4 = r0.actionBar;
        r3 = r3.title;
        r4.setTitle(r3);
        goto L_0x0731;
    L_0x071c:
        r3 = r0.parentChatActivity;
        r3 = r3.getCurrentUser();
        if (r3 == 0) goto L_0x0731;
    L_0x0724:
        r4 = r0.actionBar;
        r5 = r3.first_name;
        r3 = r3.last_name;
        r3 = org.telegram.messenger.ContactsController.formatName(r5, r3);
        r4.setTitle(r3);
    L_0x0731:
        r3 = r0.sendPhotoType;
        if (r3 == 0) goto L_0x0746;
    L_0x0735:
        r4 = 4;
        if (r3 == r4) goto L_0x0746;
    L_0x0738:
        if (r3 == r13) goto L_0x073d;
    L_0x073a:
        r4 = 5;
        if (r3 != r4) goto L_0x0753;
    L_0x073d:
        r3 = r0.imagesArrLocals;
        r3 = r3.size();
        r4 = 1;
        if (r3 <= r4) goto L_0x0753;
    L_0x0746:
        r3 = r0.checkImageView;
        r4 = r0.placeProvider;
        r5 = r0.switchingToIndex;
        r4 = r4.isPhotoChecked(r5);
        r3.setChecked(r4, r9);
    L_0x0753:
        r0.updateCaptionTextForCurrentPhoto(r1);
        r1 = new android.graphics.PorterDuffColorFilter;
        r3 = -12734994; // 0xffffffffff3dadee float:-2.5212719E38 double:NaN;
        r4 = android.graphics.PorterDuff.Mode.MULTIPLY;
        r1.<init>(r3, r4);
        r3 = r0.timeItem;
        if (r7 == 0) goto L_0x0766;
    L_0x0764:
        r4 = r1;
        goto L_0x0767;
    L_0x0766:
        r4 = r14;
    L_0x0767:
        r3.setColorFilter(r4);
        r3 = r0.paintItem;
        if (r11 == 0) goto L_0x0770;
    L_0x076e:
        r4 = r1;
        goto L_0x0771;
    L_0x0770:
        r4 = r14;
    L_0x0771:
        r3.setColorFilter(r4);
        r3 = r0.cropItem;
        if (r2 == 0) goto L_0x077a;
    L_0x0778:
        r2 = r1;
        goto L_0x077b;
    L_0x077a:
        r2 = r14;
    L_0x077b:
        r3.setColorFilter(r2);
        r2 = r0.tuneItem;
        if (r8 == 0) goto L_0x0783;
    L_0x0782:
        goto L_0x0784;
    L_0x0783:
        r1 = r14;
    L_0x0784:
        r2.setColorFilter(r1);
        r2 = r6;
        goto L_0x078b;
    L_0x0789:
        return;
    L_0x078a:
        r2 = r14;
    L_0x078b:
        r1 = 1;
        r1 = r28 ^ 1;
        r0.setCurrentCaption(r14, r2, r1);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.setIsAboutToSwitchToIndex(int, boolean):void");
    }

    private FileLocation getFileLocation(ImageLocation imageLocation) {
        return imageLocation == null ? null : imageLocation.location;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0089  */
    private void setImageIndex(int r12, boolean r13) {
        /*
        r11 = this;
        r0 = r11.currentIndex;
        if (r0 == r12) goto L_0x02b5;
    L_0x0004:
        r0 = r11.placeProvider;
        if (r0 != 0) goto L_0x000a;
    L_0x0008:
        goto L_0x02b5;
    L_0x000a:
        r0 = 0;
        if (r13 != 0) goto L_0x0016;
    L_0x000d:
        r1 = r11.currentThumb;
        if (r1 == 0) goto L_0x0016;
    L_0x0011:
        r1.release();
        r11.currentThumb = r0;
    L_0x0016:
        r1 = r11.currentFileNames;
        r2 = r11.getFileName(r12);
        r3 = 0;
        r1[r3] = r2;
        r1 = r11.currentFileNames;
        r2 = r12 + 1;
        r2 = r11.getFileName(r2);
        r4 = 1;
        r1[r4] = r2;
        r1 = r11.currentFileNames;
        r2 = r12 + -1;
        r2 = r11.getFileName(r2);
        r5 = 2;
        r1[r5] = r2;
        r1 = r11.placeProvider;
        r2 = r11.currentMessageObject;
        r6 = r11.currentFileLocation;
        r6 = r11.getFileLocation(r6);
        r7 = r11.currentIndex;
        r1.willSwitchFromPhoto(r2, r6, r7);
        r1 = r11.currentIndex;
        r11.currentIndex = r12;
        r2 = r11.currentIndex;
        r11.setIsAboutToSwitchToIndex(r2, r13);
        r2 = r11.imagesArr;
        r2 = r2.isEmpty();
        if (r2 != 0) goto L_0x00a9;
    L_0x0055:
        r12 = r11.currentIndex;
        if (r12 < 0) goto L_0x00a5;
    L_0x0059:
        r2 = r11.imagesArr;
        r2 = r2.size();
        if (r12 < r2) goto L_0x0062;
    L_0x0061:
        goto L_0x00a5;
    L_0x0062:
        r12 = r11.imagesArr;
        r2 = r11.currentIndex;
        r12 = r12.get(r2);
        r12 = (org.telegram.messenger.MessageObject) r12;
        if (r13 == 0) goto L_0x007e;
    L_0x006e:
        r13 = r11.currentMessageObject;
        if (r13 == 0) goto L_0x007e;
    L_0x0072:
        r13 = r13.getId();
        r2 = r12.getId();
        if (r13 != r2) goto L_0x007e;
    L_0x007c:
        r13 = 1;
        goto L_0x007f;
    L_0x007e:
        r13 = 0;
    L_0x007f:
        r11.currentMessageObject = r12;
        r2 = r12.isVideo();
        r6 = r11.sharedMediaType;
        if (r6 != r4) goto L_0x00a2;
    L_0x0089:
        r12 = r12.canPreviewDocument();
        r11.canZoom = r12;
        if (r12 == 0) goto L_0x009a;
    L_0x0091:
        r12 = r11.menuItem;
        r12.showSubItem(r4);
        r11.setDoubleTapEnabled(r4);
        goto L_0x00a2;
    L_0x009a:
        r12 = r11.menuItem;
        r12.hideSubItem(r4);
        r11.setDoubleTapEnabled(r3);
    L_0x00a2:
        r12 = r0;
        goto L_0x01b1;
    L_0x00a5:
        r11.closePhoto(r3, r3);
        return;
    L_0x00a9:
        r2 = r11.secureDocuments;
        r2 = r2.isEmpty();
        if (r2 != 0) goto L_0x00cc;
    L_0x00b1:
        if (r12 < 0) goto L_0x00c8;
    L_0x00b3:
        r13 = r11.secureDocuments;
        r13 = r13.size();
        if (r12 < r13) goto L_0x00bc;
    L_0x00bb:
        goto L_0x00c8;
    L_0x00bc:
        r13 = r11.secureDocuments;
        r12 = r13.get(r12);
        r12 = (org.telegram.messenger.SecureDocument) r12;
        r11.currentSecureDocument = r12;
        goto L_0x01ae;
    L_0x00c8:
        r11.closePhoto(r3, r3);
        return;
    L_0x00cc:
        r2 = r11.imagesArrLocations;
        r2 = r2.isEmpty();
        if (r2 != 0) goto L_0x0115;
    L_0x00d4:
        if (r12 < 0) goto L_0x0111;
    L_0x00d6:
        r2 = r11.imagesArrLocations;
        r2 = r2.size();
        if (r12 < r2) goto L_0x00df;
    L_0x00de:
        goto L_0x0111;
    L_0x00df:
        r2 = r11.currentFileLocation;
        r6 = r11.imagesArrLocations;
        r6 = r6.get(r12);
        r6 = (org.telegram.messenger.ImageLocation) r6;
        if (r13 == 0) goto L_0x0103;
    L_0x00eb:
        if (r2 == 0) goto L_0x0103;
    L_0x00ed:
        if (r6 == 0) goto L_0x0103;
    L_0x00ef:
        r13 = r2.location;
        r2 = r13.local_id;
        r6 = r6.location;
        r7 = r6.local_id;
        if (r2 != r7) goto L_0x0103;
    L_0x00f9:
        r7 = r13.volume_id;
        r9 = r6.volume_id;
        r13 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1));
        if (r13 != 0) goto L_0x0103;
    L_0x0101:
        r13 = 1;
        goto L_0x0104;
    L_0x0103:
        r13 = 0;
    L_0x0104:
        r2 = r11.imagesArrLocations;
        r12 = r2.get(r12);
        r12 = (org.telegram.messenger.ImageLocation) r12;
        r11.currentFileLocation = r12;
        r12 = r0;
        goto L_0x01b0;
    L_0x0111:
        r11.closePhoto(r3, r3);
        return;
    L_0x0115:
        r13 = r11.imagesArrLocals;
        r13 = r13.isEmpty();
        if (r13 != 0) goto L_0x01ae;
    L_0x011d:
        if (r12 < 0) goto L_0x01aa;
    L_0x011f:
        r13 = r11.imagesArrLocals;
        r13 = r13.size();
        if (r12 < r13) goto L_0x0129;
    L_0x0127:
        goto L_0x01aa;
    L_0x0129:
        r13 = r11.imagesArrLocals;
        r12 = r13.get(r12);
        r13 = r12 instanceof org.telegram.tgnet.TLRPC.BotInlineResult;
        if (r13 == 0) goto L_0x017c;
    L_0x0133:
        r12 = (org.telegram.tgnet.TLRPC.BotInlineResult) r12;
        r11.currentBotInlineResult = r12;
        r13 = r12.document;
        if (r13 == 0) goto L_0x014c;
    L_0x013b:
        r13 = org.telegram.messenger.FileLoader.getPathToAttach(r13);
        r13 = r13.getAbsolutePath();
        r11.currentPathObject = r13;
        r12 = r12.document;
        r12 = org.telegram.messenger.MessageObject.isVideoDocument(r12);
        goto L_0x0179;
    L_0x014c:
        r13 = r12.photo;
        if (r13 == 0) goto L_0x0165;
    L_0x0150:
        r12 = r13.sizes;
        r13 = org.telegram.messenger.AndroidUtilities.getPhotoSize();
        r12 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r12, r13);
        r12 = org.telegram.messenger.FileLoader.getPathToAttach(r12);
        r12 = r12.getAbsolutePath();
        r11.currentPathObject = r12;
        goto L_0x0178;
    L_0x0165:
        r13 = r12.content;
        r2 = r13 instanceof org.telegram.tgnet.TLRPC.TL_webDocument;
        if (r2 == 0) goto L_0x0178;
    L_0x016b:
        r13 = r13.url;
        r11.currentPathObject = r13;
        r12 = r12.type;
        r13 = "video";
        r12 = r12.equals(r13);
        goto L_0x0179;
    L_0x0178:
        r12 = 0;
    L_0x0179:
        r2 = r12;
        r12 = r0;
        goto L_0x019b;
    L_0x017c:
        r13 = r12 instanceof org.telegram.messenger.MediaController.PhotoEntry;
        if (r13 == 0) goto L_0x019d;
    L_0x0180:
        r12 = (org.telegram.messenger.MediaController.PhotoEntry) r12;
        r13 = r12.path;
        r11.currentPathObject = r13;
        r2 = r11.currentPathObject;
        if (r2 != 0) goto L_0x018e;
    L_0x018a:
        r11.closePhoto(r3, r3);
        return;
    L_0x018e:
        r12 = r12.isVideo;
        r2 = new java.io.File;
        r2.<init>(r13);
        r13 = android.net.Uri.fromFile(r2);
        r2 = r12;
        r12 = r13;
    L_0x019b:
        r13 = 0;
        goto L_0x01b1;
    L_0x019d:
        r13 = r12 instanceof org.telegram.messenger.MediaController.SearchImage;
        if (r13 == 0) goto L_0x01ae;
    L_0x01a1:
        r12 = (org.telegram.messenger.MediaController.SearchImage) r12;
        r12 = r12.getPathToAttach();
        r11.currentPathObject = r12;
        goto L_0x01ae;
    L_0x01aa:
        r11.closePhoto(r3, r3);
        return;
    L_0x01ae:
        r12 = r0;
        r13 = 0;
    L_0x01b0:
        r2 = 0;
    L_0x01b1:
        r6 = r11.currentPlaceObject;
        if (r6 == 0) goto L_0x01c1;
    L_0x01b5:
        r7 = r11.animationInProgress;
        if (r7 != 0) goto L_0x01bf;
    L_0x01b9:
        r6 = r6.imageReceiver;
        r6.setVisible(r4, r4);
        goto L_0x01c1;
    L_0x01bf:
        r11.showAfterAnimation = r6;
    L_0x01c1:
        r6 = r11.placeProvider;
        r7 = r11.currentMessageObject;
        r8 = r11.currentFileLocation;
        r8 = r11.getFileLocation(r8);
        r9 = r11.currentIndex;
        r6 = r6.getPlaceForPhoto(r7, r8, r9, r3);
        r11.currentPlaceObject = r6;
        r6 = r11.currentPlaceObject;
        if (r6 == 0) goto L_0x01e3;
    L_0x01d7:
        r7 = r11.animationInProgress;
        if (r7 != 0) goto L_0x01e1;
    L_0x01db:
        r6 = r6.imageReceiver;
        r6.setVisible(r3, r4);
        goto L_0x01e3;
    L_0x01e1:
        r11.hideAfterAnimation = r6;
    L_0x01e3:
        if (r13 != 0) goto L_0x024e;
    L_0x01e5:
        r11.draggingDown = r3;
        r13 = 0;
        r11.translationX = r13;
        r11.translationY = r13;
        r6 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r11.scale = r6;
        r11.animateToX = r13;
        r11.animateToY = r13;
        r11.animateToScale = r6;
        r7 = 0;
        r11.animationStartTime = r7;
        r11.imageMoveAnimation = r0;
        r11.changeModeAnimation = r0;
        r0 = r11.aspectRatioFrameLayout;
        if (r0 == 0) goto L_0x0206;
    L_0x0202:
        r7 = 4;
        r0.setVisibility(r7);
    L_0x0206:
        r11.pinchStartDistance = r13;
        r11.pinchStartScale = r6;
        r11.pinchCenterX = r13;
        r11.pinchCenterY = r13;
        r11.pinchStartX = r13;
        r11.pinchStartY = r13;
        r11.moveStartX = r13;
        r11.moveStartY = r13;
        r11.zooming = r3;
        r11.moving = r3;
        r11.doubleTap = r3;
        r11.invalidCoords = r3;
        r11.canDragDown = r4;
        r11.changingPage = r3;
        r11.switchImageAfterAnimation = r3;
        r13 = r11.sharedMediaType;
        if (r13 == r4) goto L_0x0246;
    L_0x0228:
        r13 = r11.imagesArrLocals;
        r13 = r13.isEmpty();
        if (r13 == 0) goto L_0x0243;
    L_0x0230:
        r13 = r11.currentFileNames;
        r13 = r13[r3];
        if (r13 == 0) goto L_0x0241;
    L_0x0236:
        r13 = r11.photoProgressViews;
        r13 = r13[r3];
        r13 = r13.backgroundState;
        if (r13 == 0) goto L_0x0241;
    L_0x0240:
        goto L_0x0243;
    L_0x0241:
        r13 = 0;
        goto L_0x0244;
    L_0x0243:
        r13 = 1;
    L_0x0244:
        r11.canZoom = r13;
    L_0x0246:
        r13 = r11.scale;
        r11.updateMinMax(r13);
        r11.releasePlayer(r3);
    L_0x024e:
        if (r2 == 0) goto L_0x0257;
    L_0x0250:
        if (r12 == 0) goto L_0x0257;
    L_0x0252:
        r11.isStreaming = r3;
        r11.preparePlayer(r12, r3, r3);
    L_0x0257:
        r12 = -1;
        if (r1 != r12) goto L_0x0267;
    L_0x025a:
        r11.setImages();
        r12 = 0;
    L_0x025e:
        r13 = 3;
        if (r12 >= r13) goto L_0x02b5;
    L_0x0261:
        r11.checkProgress(r12, r3);
        r12 = r12 + 1;
        goto L_0x025e;
    L_0x0267:
        r11.checkProgress(r3, r3);
        r12 = r11.currentIndex;
        if (r1 <= r12) goto L_0x0291;
    L_0x026e:
        r13 = r11.rightImage;
        r0 = r11.centerImage;
        r11.rightImage = r0;
        r0 = r11.leftImage;
        r11.centerImage = r0;
        r11.leftImage = r13;
        r13 = r11.photoProgressViews;
        r0 = r13[r3];
        r1 = r13[r5];
        r13[r3] = r1;
        r13[r5] = r0;
        r13 = r11.leftImage;
        r12 = r12 - r4;
        r11.setIndexToImage(r13, r12);
        r11.checkProgress(r4, r3);
        r11.checkProgress(r5, r3);
        goto L_0x02b5;
    L_0x0291:
        if (r1 >= r12) goto L_0x02b5;
    L_0x0293:
        r13 = r11.leftImage;
        r0 = r11.centerImage;
        r11.leftImage = r0;
        r0 = r11.rightImage;
        r11.centerImage = r0;
        r11.rightImage = r13;
        r13 = r11.photoProgressViews;
        r0 = r13[r3];
        r1 = r13[r4];
        r13[r3] = r1;
        r13[r4] = r0;
        r13 = r11.rightImage;
        r12 = r12 + r4;
        r11.setIndexToImage(r13, r12);
        r11.checkProgress(r4, r3);
        r11.checkProgress(r5, r3);
    L_0x02b5:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.setImageIndex(int, boolean):void");
    }

    private void setCurrentCaption(MessageObject messageObject, CharSequence charSequence, boolean z) {
        MessageObject messageObject2 = messageObject;
        if (this.needCaptionLayout) {
            if (this.captionTextView.getParent() != this.pickerView) {
                this.captionTextView.setBackgroundDrawable(null);
                this.containerView.removeView(this.captionTextView);
                this.pickerView.addView(this.captionTextView, LayoutHelper.createFrame(-1, -2.0f, 83, 0.0f, 0.0f, 76.0f, 48.0f));
            }
        } else if (this.captionTextView.getParent() != this.containerView) {
            this.captionTextView.setBackgroundColor(NUM);
            this.pickerView.removeView(this.captionTextView);
            this.containerView.addView(this.captionTextView, LayoutHelper.createFrame(-1, -2.0f, 83, 0.0f, 0.0f, 0.0f, 48.0f));
        }
        if (this.isCurrentVideo) {
            this.captionTextView.setMaxLines(1);
            this.captionTextView.setSingleLine(true);
        } else {
            this.captionTextView.setSingleLine(false);
            TextView textView = this.captionTextView;
            Point point = AndroidUtilities.displaySize;
            textView.setMaxLines(point.x > point.y ? 5 : 10);
        }
        Object obj = this.captionTextView.getTag() != null ? 1 : null;
        AnimatorSet animatorSet;
        Animator[] animatorArr;
        if (!TextUtils.isEmpty(charSequence)) {
            Object replaceEmoji;
            Theme.createChatResources(null, true);
            if (messageObject2 == null || messageObject2.messageOwner.entities.isEmpty()) {
                replaceEmoji = Emoji.replaceEmoji(new SpannableStringBuilder(charSequence), this.captionTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
            } else {
                SpannableString valueOf = SpannableString.valueOf(charSequence.toString());
                messageObject2.addEntitiesToText(valueOf, true, false);
                replaceEmoji = Emoji.replaceEmoji(valueOf, this.captionTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
            }
            this.captionTextView.setTag(replaceEmoji);
            AnimatorSet animatorSet2 = this.currentCaptionAnimation;
            if (animatorSet2 != null) {
                animatorSet2.cancel();
                this.currentCaptionAnimation = null;
            }
            try {
                this.captionTextView.setText(replaceEmoji);
            } catch (Exception e) {
                FileLog.e(e);
            }
            this.captionTextView.setScrollY(0);
            this.captionTextView.setTextColor(-1);
            replaceEmoji = (this.isActionBarVisible && (this.bottomLayout.getVisibility() == 0 || this.pickerView.getVisibility() == 0)) ? 1 : null;
            if (replaceEmoji != null) {
                this.captionTextView.setVisibility(0);
                if (z && obj == null) {
                    this.currentCaptionAnimation = new AnimatorSet();
                    this.currentCaptionAnimation.setDuration(200);
                    this.currentCaptionAnimation.setInterpolator(decelerateInterpolator);
                    this.currentCaptionAnimation.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            if (animator.equals(PhotoViewer.this.currentCaptionAnimation)) {
                                PhotoViewer.this.currentCaptionAnimation = null;
                            }
                        }
                    });
                    animatorSet = this.currentCaptionAnimation;
                    animatorArr = new Animator[2];
                    animatorArr[0] = ObjectAnimator.ofFloat(this.captionTextView, View.ALPHA, new float[]{0.0f, 1.0f});
                    animatorArr[1] = ObjectAnimator.ofFloat(this.captionTextView, View.TRANSLATION_Y, new float[]{(float) AndroidUtilities.dp(5.0f), 0.0f});
                    animatorSet.playTogether(animatorArr);
                    this.currentCaptionAnimation.start();
                    return;
                }
                this.captionTextView.setAlpha(1.0f);
            } else if (this.captionTextView.getVisibility() == 0) {
                this.captionTextView.setVisibility(4);
                this.captionTextView.setAlpha(0.0f);
            }
        } else if (this.needCaptionLayout) {
            this.captionTextView.setText(LocaleController.getString("AddCaption", NUM));
            this.captionTextView.setTag("empty");
            this.captionTextView.setVisibility(0);
            this.captionTextView.setTextColor(-NUM);
        } else {
            this.captionTextView.setTextColor(-1);
            this.captionTextView.setTag(null);
            if (!z || obj == null) {
                this.captionTextView.setVisibility(4);
                return;
            }
            this.currentCaptionAnimation = new AnimatorSet();
            this.currentCaptionAnimation.setDuration(200);
            this.currentCaptionAnimation.setInterpolator(decelerateInterpolator);
            this.currentCaptionAnimation.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    if (animator.equals(PhotoViewer.this.currentCaptionAnimation)) {
                        PhotoViewer.this.captionTextView.setVisibility(4);
                        PhotoViewer.this.currentCaptionAnimation = null;
                    }
                }

                public void onAnimationCancel(Animator animator) {
                    if (animator.equals(PhotoViewer.this.currentCaptionAnimation)) {
                        PhotoViewer.this.currentCaptionAnimation = null;
                    }
                }
            });
            animatorSet = this.currentCaptionAnimation;
            animatorArr = new Animator[2];
            animatorArr[0] = ObjectAnimator.ofFloat(this.captionTextView, View.ALPHA, new float[]{0.0f});
            animatorArr[1] = ObjectAnimator.ofFloat(this.captionTextView, View.TRANSLATION_Y, new float[]{(float) AndroidUtilities.dp(5.0f)});
            animatorSet.playTogether(animatorArr);
            this.currentCaptionAnimation.start();
        }
    }

    /* JADX WARNING: Missing block: B:21:0x0058, code skipped:
            if (r8.exists() == false) goto L_0x005a;
     */
    private void checkProgress(int r12, boolean r13) {
        /*
        r11 = this;
        r0 = r11.currentIndex;
        r1 = 2;
        r2 = 1;
        if (r12 != r2) goto L_0x0009;
    L_0x0006:
        r0 = r0 + 1;
        goto L_0x000d;
    L_0x0009:
        if (r12 != r1) goto L_0x000d;
    L_0x000b:
        r0 = r0 + -1;
    L_0x000d:
        r3 = r11.currentFileNames;
        r3 = r3[r12];
        r4 = 3;
        r5 = -1;
        r6 = 0;
        if (r3 == 0) goto L_0x0284;
    L_0x0016:
        r3 = r11.currentMessageObject;
        r7 = 0;
        if (r3 == 0) goto L_0x00a7;
    L_0x001b:
        if (r0 < 0) goto L_0x009f;
    L_0x001d:
        r3 = r11.imagesArr;
        r3 = r3.size();
        if (r0 < r3) goto L_0x0027;
    L_0x0025:
        goto L_0x009f;
    L_0x0027:
        r3 = r11.imagesArr;
        r3 = r3.get(r0);
        r3 = (org.telegram.messenger.MessageObject) r3;
        r8 = r11.sharedMediaType;
        if (r8 != r2) goto L_0x0041;
    L_0x0033:
        r8 = r3.canPreviewDocument();
        if (r8 != 0) goto L_0x0041;
    L_0x0039:
        r0 = r11.photoProgressViews;
        r12 = r0[r12];
        r12.setBackgroundState(r5, r13);
        return;
    L_0x0041:
        r8 = r3.messageOwner;
        r8 = r8.attachPath;
        r8 = android.text.TextUtils.isEmpty(r8);
        if (r8 != 0) goto L_0x005a;
    L_0x004b:
        r8 = new java.io.File;
        r9 = r3.messageOwner;
        r9 = r9.attachPath;
        r8.<init>(r9);
        r9 = r8.exists();
        if (r9 != 0) goto L_0x005b;
    L_0x005a:
        r8 = r7;
    L_0x005b:
        if (r8 != 0) goto L_0x007e;
    L_0x005d:
        r8 = r3.messageOwner;
        r8 = r8.media;
        r9 = r8 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
        if (r9 == 0) goto L_0x0076;
    L_0x0065:
        r8 = r8.webpage;
        if (r8 == 0) goto L_0x0076;
    L_0x0069:
        r8 = r8.document;
        if (r8 != 0) goto L_0x0076;
    L_0x006d:
        r0 = r11.getFileLocation(r0, r7);
        r0 = org.telegram.messenger.FileLoader.getPathToAttach(r0, r2);
        goto L_0x007c;
    L_0x0076:
        r0 = r3.messageOwner;
        r0 = org.telegram.messenger.FileLoader.getPathToMessage(r0);
    L_0x007c:
        r7 = r0;
        goto L_0x007f;
    L_0x007e:
        r7 = r8;
    L_0x007f:
        r0 = org.telegram.messenger.SharedConfig.streamMedia;
        if (r0 == 0) goto L_0x0098;
    L_0x0083:
        r0 = r3.isVideo();
        if (r0 == 0) goto L_0x0098;
    L_0x0089:
        r0 = r3.canStreamVideo();
        if (r0 == 0) goto L_0x0098;
    L_0x008f:
        r8 = r3.getDialogId();
        r0 = (int) r8;
        if (r0 == 0) goto L_0x0098;
    L_0x0096:
        r0 = 1;
        goto L_0x0099;
    L_0x0098:
        r0 = 0;
    L_0x0099:
        r3 = r3.isVideo();
        goto L_0x01d5;
    L_0x009f:
        r0 = r11.photoProgressViews;
        r12 = r0[r12];
        r12.setBackgroundState(r5, r13);
        return;
    L_0x00a7:
        r3 = r11.currentBotInlineResult;
        r8 = 4;
        if (r3 == 0) goto L_0x0159;
    L_0x00ac:
        if (r0 < 0) goto L_0x0151;
    L_0x00ae:
        r3 = r11.imagesArrLocals;
        r3 = r3.size();
        if (r0 < r3) goto L_0x00b8;
    L_0x00b6:
        goto L_0x0151;
    L_0x00b8:
        r3 = r11.imagesArrLocals;
        r0 = r3.get(r0);
        r0 = (org.telegram.tgnet.TLRPC.BotInlineResult) r0;
        r3 = r0.type;
        r9 = "video";
        r3 = r3.equals(r9);
        if (r3 != 0) goto L_0x00f8;
    L_0x00ca:
        r3 = r0.document;
        r3 = org.telegram.messenger.MessageObject.isVideoDocument(r3);
        if (r3 == 0) goto L_0x00d3;
    L_0x00d2:
        goto L_0x00f8;
    L_0x00d3:
        r3 = r0.document;
        if (r3 == 0) goto L_0x00e5;
    L_0x00d7:
        r7 = new java.io.File;
        r0 = org.telegram.messenger.FileLoader.getDirectory(r4);
        r3 = r11.currentFileNames;
        r3 = r3[r12];
        r7.<init>(r0, r3);
        goto L_0x00f6;
    L_0x00e5:
        r0 = r0.photo;
        if (r0 == 0) goto L_0x00f6;
    L_0x00e9:
        r7 = new java.io.File;
        r0 = org.telegram.messenger.FileLoader.getDirectory(r6);
        r3 = r11.currentFileNames;
        r3 = r3[r12];
        r7.<init>(r0, r3);
    L_0x00f6:
        r0 = 0;
        goto L_0x0137;
    L_0x00f8:
        r3 = r0.document;
        if (r3 == 0) goto L_0x0101;
    L_0x00fc:
        r7 = org.telegram.messenger.FileLoader.getPathToAttach(r3);
        goto L_0x0136;
    L_0x0101:
        r3 = r0.content;
        r3 = r3 instanceof org.telegram.tgnet.TLRPC.TL_webDocument;
        if (r3 == 0) goto L_0x0136;
    L_0x0107:
        r7 = new java.io.File;
        r3 = org.telegram.messenger.FileLoader.getDirectory(r8);
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = r0.content;
        r10 = r10.url;
        r10 = org.telegram.messenger.Utilities.MD5(r10);
        r9.append(r10);
        r10 = ".";
        r9.append(r10);
        r0 = r0.content;
        r0 = r0.url;
        r10 = "mp4";
        r0 = org.telegram.messenger.ImageLoader.getHttpUrlExtension(r0, r10);
        r9.append(r0);
        r0 = r9.toString();
        r7.<init>(r3, r0);
    L_0x0136:
        r0 = 1;
    L_0x0137:
        if (r7 == 0) goto L_0x013f;
    L_0x0139:
        r3 = r7.exists();
        if (r3 != 0) goto L_0x014d;
    L_0x013f:
        r3 = new java.io.File;
        r7 = org.telegram.messenger.FileLoader.getDirectory(r8);
        r8 = r11.currentFileNames;
        r8 = r8[r12];
        r3.<init>(r7, r8);
        r7 = r3;
    L_0x014d:
        r3 = r0;
        r0 = 0;
        goto L_0x01d5;
    L_0x0151:
        r0 = r11.photoProgressViews;
        r12 = r0[r12];
        r12.setBackgroundState(r5, r13);
        return;
    L_0x0159:
        r3 = r11.currentFileLocation;
        if (r3 == 0) goto L_0x018b;
    L_0x015d:
        if (r0 < 0) goto L_0x0183;
    L_0x015f:
        r3 = r11.imagesArrLocations;
        r3 = r3.size();
        if (r0 < r3) goto L_0x0168;
    L_0x0167:
        goto L_0x0183;
    L_0x0168:
        r3 = r11.imagesArrLocations;
        r0 = r3.get(r0);
        r0 = (org.telegram.messenger.ImageLocation) r0;
        r0 = r0.location;
        r3 = r11.avatarsDialogId;
        if (r3 != 0) goto L_0x017d;
    L_0x0176:
        r3 = r11.isEvent;
        if (r3 == 0) goto L_0x017b;
    L_0x017a:
        goto L_0x017d;
    L_0x017b:
        r3 = 0;
        goto L_0x017e;
    L_0x017d:
        r3 = 1;
    L_0x017e:
        r7 = org.telegram.messenger.FileLoader.getPathToAttach(r0, r3);
        goto L_0x01d3;
    L_0x0183:
        r0 = r11.photoProgressViews;
        r12 = r0[r12];
        r12.setBackgroundState(r5, r13);
        return;
    L_0x018b:
        r3 = r11.currentSecureDocument;
        if (r3 == 0) goto L_0x01af;
    L_0x018f:
        if (r0 < 0) goto L_0x01a7;
    L_0x0191:
        r3 = r11.secureDocuments;
        r3 = r3.size();
        if (r0 < r3) goto L_0x019a;
    L_0x0199:
        goto L_0x01a7;
    L_0x019a:
        r3 = r11.secureDocuments;
        r0 = r3.get(r0);
        r0 = (org.telegram.messenger.SecureDocument) r0;
        r7 = org.telegram.messenger.FileLoader.getPathToAttach(r0, r2);
        goto L_0x01d3;
    L_0x01a7:
        r0 = r11.photoProgressViews;
        r12 = r0[r12];
        r12.setBackgroundState(r5, r13);
        return;
    L_0x01af:
        r0 = r11.currentPathObject;
        if (r0 == 0) goto L_0x01d3;
    L_0x01b3:
        r7 = new java.io.File;
        r0 = org.telegram.messenger.FileLoader.getDirectory(r4);
        r3 = r11.currentFileNames;
        r3 = r3[r12];
        r7.<init>(r0, r3);
        r0 = r7.exists();
        if (r0 != 0) goto L_0x01d3;
    L_0x01c6:
        r7 = new java.io.File;
        r0 = org.telegram.messenger.FileLoader.getDirectory(r8);
        r3 = r11.currentFileNames;
        r3 = r3[r12];
        r7.<init>(r0, r3);
    L_0x01d3:
        r0 = 0;
        r3 = 0;
    L_0x01d5:
        if (r7 == 0) goto L_0x01df;
    L_0x01d7:
        r8 = r7.exists();
        if (r8 == 0) goto L_0x01df;
    L_0x01dd:
        r8 = 1;
        goto L_0x01e0;
    L_0x01df:
        r8 = 0;
    L_0x01e0:
        if (r7 == 0) goto L_0x021e;
    L_0x01e2:
        if (r8 != 0) goto L_0x01e6;
    L_0x01e4:
        if (r0 == 0) goto L_0x021e;
    L_0x01e6:
        if (r3 == 0) goto L_0x01f0;
    L_0x01e8:
        r0 = r11.photoProgressViews;
        r0 = r0[r12];
        r0.setBackgroundState(r4, r13);
        goto L_0x01f7;
    L_0x01f0:
        r0 = r11.photoProgressViews;
        r0 = r0[r12];
        r0.setBackgroundState(r5, r13);
    L_0x01f7:
        if (r12 != 0) goto L_0x0265;
    L_0x01f9:
        r13 = 7;
        if (r8 != 0) goto L_0x0218;
    L_0x01fc:
        r0 = r11.currentAccount;
        r0 = org.telegram.messenger.FileLoader.getInstance(r0);
        r1 = r11.currentFileNames;
        r1 = r1[r12];
        r0 = r0.isLoadingFile(r1);
        if (r0 != 0) goto L_0x0212;
    L_0x020c:
        r0 = r11.menuItem;
        r0.hideSubItem(r13);
        goto L_0x0265;
    L_0x0212:
        r0 = r11.menuItem;
        r0.showSubItem(r13);
        goto L_0x0265;
    L_0x0218:
        r0 = r11.menuItem;
        r0.hideSubItem(r13);
        goto L_0x0265;
    L_0x021e:
        if (r3 == 0) goto L_0x0240;
    L_0x0220:
        r13 = r11.currentAccount;
        r13 = org.telegram.messenger.FileLoader.getInstance(r13);
        r0 = r11.currentFileNames;
        r0 = r0[r12];
        r13 = r13.isLoadingFile(r0);
        if (r13 != 0) goto L_0x0238;
    L_0x0230:
        r13 = r11.photoProgressViews;
        r13 = r13[r12];
        r13.setBackgroundState(r1, r6);
        goto L_0x0247;
    L_0x0238:
        r13 = r11.photoProgressViews;
        r13 = r13[r12];
        r13.setBackgroundState(r2, r6);
        goto L_0x0247;
    L_0x0240:
        r0 = r11.photoProgressViews;
        r0 = r0[r12];
        r0.setBackgroundState(r6, r13);
    L_0x0247:
        r13 = org.telegram.messenger.ImageLoader.getInstance();
        r0 = r11.currentFileNames;
        r0 = r0[r12];
        r13 = r13.getFileProgress(r0);
        if (r13 != 0) goto L_0x025a;
    L_0x0255:
        r13 = 0;
        r13 = java.lang.Float.valueOf(r13);
    L_0x025a:
        r0 = r11.photoProgressViews;
        r0 = r0[r12];
        r13 = r13.floatValue();
        r0.setProgress(r13, r6);
    L_0x0265:
        if (r12 != 0) goto L_0x02b5;
    L_0x0267:
        r12 = r11.imagesArrLocals;
        r12 = r12.isEmpty();
        if (r12 == 0) goto L_0x0281;
    L_0x026f:
        r12 = r11.currentFileNames;
        r12 = r12[r6];
        if (r12 == 0) goto L_0x0280;
    L_0x0275:
        r12 = r11.photoProgressViews;
        r12 = r12[r6];
        r12 = r12.backgroundState;
        if (r12 == 0) goto L_0x0280;
    L_0x027f:
        goto L_0x0281;
    L_0x0280:
        r2 = 0;
    L_0x0281:
        r11.canZoom = r2;
        goto L_0x02b5;
    L_0x0284:
        r1 = r11.imagesArrLocals;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x02a4;
    L_0x028c:
        if (r0 < 0) goto L_0x02a4;
    L_0x028e:
        r1 = r11.imagesArrLocals;
        r1 = r1.size();
        if (r0 >= r1) goto L_0x02a4;
    L_0x0296:
        r1 = r11.imagesArrLocals;
        r0 = r1.get(r0);
        r1 = r0 instanceof org.telegram.messenger.MediaController.PhotoEntry;
        if (r1 == 0) goto L_0x02a4;
    L_0x02a0:
        r0 = (org.telegram.messenger.MediaController.PhotoEntry) r0;
        r6 = r0.isVideo;
    L_0x02a4:
        if (r6 == 0) goto L_0x02ae;
    L_0x02a6:
        r0 = r11.photoProgressViews;
        r12 = r0[r12];
        r12.setBackgroundState(r4, r13);
        goto L_0x02b5;
    L_0x02ae:
        r0 = r11.photoProgressViews;
        r12 = r0[r12];
        r12.setBackgroundState(r5, r13);
    L_0x02b5:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.checkProgress(int, boolean):void");
    }

    public int getSelectiongLength() {
        PhotoViewerCaptionEnterView photoViewerCaptionEnterView = this.captionEditText;
        return photoViewerCaptionEnterView != null ? photoViewerCaptionEnterView.getSelectionLength() : 0;
    }

    private void setIndexToImage(ImageReceiver imageReceiver, int i) {
        ImageReceiver imageReceiver2 = imageReceiver;
        int i2 = i;
        imageReceiver2.setOrientation(0, false);
        MessageObject messageObject = null;
        if (this.secureDocuments.isEmpty()) {
            String str = "%d_%d";
            Object obj;
            TLObject fileLocation;
            if (this.imagesArrLocals.isEmpty()) {
                BitmapHolder bitmapHolder;
                PhotoSize closestPhotoSizeWithSize;
                if (this.imagesArr.isEmpty() || i2 < 0 || i2 >= this.imagesArr.size()) {
                    obj = null;
                } else {
                    MessageObject messageObject2 = (MessageObject) this.imagesArr.get(i2);
                    imageReceiver2.setShouldGenerateQualityThumb(true);
                    obj = messageObject2;
                }
                if (obj != null) {
                    if (obj.isVideo()) {
                        imageReceiver2.setNeedsQualityThumb(true);
                        ArrayList arrayList = obj.photoThumbs;
                        if (arrayList == null || arrayList.isEmpty()) {
                            imageReceiver2.setImageBitmap(this.parentActivity.getResources().getDrawable(NUM));
                        } else {
                            BitmapHolder bitmapHolder2 = this.currentThumb;
                            if (bitmapHolder2 == null || imageReceiver2 != this.centerImage) {
                                bitmapHolder2 = null;
                            }
                            imageReceiver.setImage(null, null, bitmapHolder2 == null ? ImageLocation.getForObject(FileLoader.getClosestPhotoSizeWithSize(obj.photoThumbs, 100), obj.photoThumbsObject) : null, "b", bitmapHolder2 != null ? new BitmapDrawable(bitmapHolder2.bitmap) : null, 0, null, obj, 1);
                        }
                        return;
                    }
                    Drawable drawable = this.currentAnimation;
                    if (drawable != null) {
                        imageReceiver2.setImageBitmap(drawable);
                        this.currentAnimation.setSecondParentView(this.containerView);
                        return;
                    } else if (this.sharedMediaType == 1) {
                        if (obj.canPreviewDocument()) {
                            Document document = obj.getDocument();
                            imageReceiver2.setNeedsQualityThumb(true);
                            bitmapHolder = this.currentThumb;
                            if (bitmapHolder == null || imageReceiver2 != this.centerImage) {
                                bitmapHolder = null;
                            }
                            closestPhotoSizeWithSize = obj != null ? FileLoader.getClosestPhotoSizeWithSize(obj.photoThumbs, 100) : null;
                            int i3 = (int) (2048.0f / AndroidUtilities.density);
                            ImageLocation forDocument = ImageLocation.getForDocument(document);
                            String format = String.format(Locale.US, str, new Object[]{Integer.valueOf(i3), Integer.valueOf(i3)});
                            ImageLocation forDocument2 = bitmapHolder == null ? ImageLocation.getForDocument(closestPhotoSizeWithSize, document) : null;
                            if (bitmapHolder != null) {
                                messageObject = new BitmapDrawable(bitmapHolder.bitmap);
                            }
                            imageReceiver.setImage(forDocument, format, forDocument2, "b", messageObject, document.size, null, obj, 0);
                        } else {
                            imageReceiver2.setImageBitmap(new OtherDocumentPlaceholderDrawable(this.parentActivity, this.containerView, obj));
                        }
                        return;
                    }
                }
                int[] iArr = new int[1];
                ImageLocation imageLocation = getImageLocation(i2, iArr);
                fileLocation = getFileLocation(i2, iArr);
                if (imageLocation != null) {
                    TLObject tLObject;
                    imageReceiver2.setNeedsQualityThumb(true);
                    bitmapHolder = this.currentThumb;
                    if (bitmapHolder == null || imageReceiver2 != this.centerImage) {
                        bitmapHolder = null;
                    }
                    if (iArr[0] == 0) {
                        iArr[0] = -1;
                    }
                    if (obj != null) {
                        closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(obj.photoThumbs, 100);
                        tLObject = obj.photoThumbsObject;
                    } else {
                        closestPhotoSizeWithSize = null;
                        tLObject = closestPhotoSizeWithSize;
                    }
                    if (closestPhotoSizeWithSize != null && closestPhotoSizeWithSize == fileLocation) {
                        closestPhotoSizeWithSize = null;
                    }
                    int i4 = ((obj == null || !obj.isWebpage()) && this.avatarsDialogId == 0 && !this.isEvent) ? 0 : 1;
                    if (obj == null) {
                        i2 = this.avatarsDialogId;
                        if (i2 != 0) {
                            User user;
                            if (i2 > 0) {
                                user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.avatarsDialogId));
                            } else {
                                user = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-this.avatarsDialogId));
                            }
                            obj = user;
                        } else {
                            obj = null;
                        }
                    }
                    imageReceiver.setImage(imageLocation, null, bitmapHolder == null ? ImageLocation.getForObject(closestPhotoSizeWithSize, tLObject) : null, "b", bitmapHolder != null ? new BitmapDrawable(bitmapHolder.bitmap) : null, iArr[0], null, obj, i4);
                } else {
                    imageReceiver2.setNeedsQualityThumb(true);
                    if (iArr[0] == 0) {
                        imageReceiver2.setImageBitmap(null);
                    } else {
                        imageReceiver2.setImageBitmap(this.parentActivity.getResources().getDrawable(NUM));
                    }
                }
            } else if (i2 < 0 || i2 >= this.imagesArrLocals.size()) {
                imageReceiver2.setImageBitmap(null);
            } else {
                String str2;
                PhotoSize photoSize;
                TLObject tLObject2;
                boolean z;
                String str3;
                int i5;
                int i6;
                WebFile webFile;
                Document document2;
                obj = this.imagesArrLocals.get(i2);
                int photoSize2 = (int) (((float) AndroidUtilities.getPhotoSize()) / AndroidUtilities.density);
                BitmapHolder bitmapHolder3 = this.currentThumb;
                if (bitmapHolder3 == null || imageReceiver2 != this.centerImage) {
                    bitmapHolder3 = null;
                }
                if (bitmapHolder3 == null) {
                    bitmapHolder3 = this.placeProvider.getThumbForPhoto(null, null, i2);
                }
                String str4 = "d";
                String str5;
                int i7;
                if (obj instanceof PhotoEntry) {
                    String str6;
                    PhotoEntry photoEntry = (PhotoEntry) obj;
                    boolean z2 = photoEntry.isVideo;
                    if (z2) {
                        str5 = photoEntry.thumbPath;
                        if (str5 == null) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("vthumb://");
                            stringBuilder.append(photoEntry.imageId);
                            stringBuilder.append(":");
                            stringBuilder.append(photoEntry.path);
                            str5 = stringBuilder.toString();
                        }
                        str6 = null;
                    } else {
                        str5 = photoEntry.imagePath;
                        if (str5 == null) {
                            imageReceiver2.setOrientation(photoEntry.orientation, false);
                            str5 = photoEntry.path;
                        }
                        str6 = String.format(Locale.US, str, new Object[]{Integer.valueOf(photoSize2), Integer.valueOf(photoSize2)});
                    }
                    str2 = str6;
                    photoSize = null;
                    tLObject2 = photoSize;
                    z = z2;
                    str3 = str5;
                    i5 = 0;
                    i6 = 0;
                    webFile = tLObject2;
                    document2 = webFile;
                } else if (obj instanceof BotInlineResult) {
                    PhotoSize closestPhotoSizeWithSize2;
                    WebFile webFile2;
                    PhotoSize photoSize3;
                    BotInlineResult botInlineResult = (BotInlineResult) obj;
                    if (botInlineResult.type.equals("video") || MessageObject.isVideoDocument(botInlineResult.document)) {
                        Document document3 = botInlineResult.document;
                        if (document3 != null) {
                            closestPhotoSizeWithSize2 = FileLoader.getClosestPhotoSizeWithSize(document3.thumbs, 90);
                            tLObject2 = botInlineResult.document;
                            webFile2 = null;
                            document2 = webFile2;
                            str2 = document2;
                            i7 = 0;
                            i5 = i7;
                            z = false;
                            i6 = 1;
                            str3 = null;
                            photoSize3 = closestPhotoSizeWithSize2;
                            webFile = webFile2;
                            photoSize = photoSize3;
                        } else {
                            WebDocument webDocument = botInlineResult.thumb;
                            if (webDocument instanceof TL_webDocument) {
                                webFile2 = WebFile.createWithWebDocument(webDocument);
                                closestPhotoSizeWithSize2 = null;
                                document2 = closestPhotoSizeWithSize2;
                                tLObject2 = document2;
                                str2 = tLObject2;
                                i7 = 0;
                                i5 = i7;
                                z = false;
                                i6 = 1;
                                str3 = null;
                                photoSize3 = closestPhotoSizeWithSize2;
                                webFile = webFile2;
                                photoSize = photoSize3;
                            }
                        }
                    } else {
                        String str7 = "gif";
                        if (botInlineResult.type.equals(str7)) {
                            document2 = botInlineResult.document;
                            if (document2 != null) {
                                i7 = document2.size;
                                webFile2 = null;
                                tLObject2 = webFile2;
                                str2 = str4;
                                closestPhotoSizeWithSize2 = tLObject2;
                                i5 = i7;
                                z = false;
                                i6 = 1;
                                str3 = null;
                                photoSize3 = closestPhotoSizeWithSize2;
                                webFile = webFile2;
                                photoSize = photoSize3;
                            }
                        }
                        Photo photo = botInlineResult.photo;
                        if (photo != null) {
                            closestPhotoSizeWithSize2 = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, AndroidUtilities.getPhotoSize());
                            fileLocation = botInlineResult.photo;
                            i7 = closestPhotoSizeWithSize2.size;
                            str2 = String.format(Locale.US, str, new Object[]{Integer.valueOf(photoSize2), Integer.valueOf(photoSize2)});
                            tLObject2 = fileLocation;
                            webFile2 = null;
                            document2 = webFile2;
                            i5 = i7;
                            z = false;
                            i6 = 1;
                            str3 = null;
                            photoSize3 = closestPhotoSizeWithSize2;
                            webFile = webFile2;
                            photoSize = photoSize3;
                        } else if (botInlineResult.content instanceof TL_webDocument) {
                            if (!botInlineResult.type.equals(str7)) {
                                str4 = String.format(Locale.US, str, new Object[]{Integer.valueOf(photoSize2), Integer.valueOf(photoSize2)});
                            }
                            webFile2 = WebFile.createWithWebDocument(botInlineResult.content);
                            TLObject tLObject3 = null;
                            tLObject2 = tLObject3;
                            str2 = str4;
                            i7 = 0;
                            closestPhotoSizeWithSize2 = tLObject2;
                            i5 = i7;
                            z = false;
                            i6 = 1;
                            str3 = null;
                            photoSize3 = closestPhotoSizeWithSize2;
                            webFile = webFile2;
                            photoSize = photoSize3;
                        }
                    }
                    webFile2 = null;
                    closestPhotoSizeWithSize2 = webFile2;
                    document2 = closestPhotoSizeWithSize2;
                    tLObject2 = document2;
                    str2 = tLObject2;
                    i7 = 0;
                    i5 = i7;
                    z = false;
                    i6 = 1;
                    str3 = null;
                    photoSize3 = closestPhotoSizeWithSize2;
                    webFile = webFile2;
                    photoSize = photoSize3;
                } else if (obj instanceof SearchImage) {
                    MessageObject messageObject3;
                    SearchImage searchImage = (SearchImage) obj;
                    PhotoSize photoSize4 = searchImage.photoSize;
                    if (photoSize4 != null) {
                        fileLocation = searchImage.photo;
                        messageObject3 = null;
                        i7 = photoSize4.size;
                        tLObject2 = fileLocation;
                        photoSize = photoSize4;
                        document2 = messageObject3;
                    } else {
                        str5 = searchImage.imagePath;
                        if (str5 != null) {
                            photoSize = null;
                            tLObject2 = photoSize;
                            messageObject3 = str5;
                            i7 = 0;
                        } else {
                            document2 = searchImage.document;
                            if (document2 != null) {
                                i7 = document2.size;
                                photoSize = null;
                                tLObject2 = photoSize;
                                messageObject3 = tLObject2;
                            } else {
                                str5 = searchImage.imageUrl;
                                i7 = searchImage.size;
                                fileLocation = null;
                                tLObject2 = fileLocation;
                                Object obj2 = str5;
                            }
                        }
                        document2 = tLObject2;
                    }
                    i5 = i7;
                    z = false;
                    i6 = 1;
                    str3 = messageObject3;
                    str2 = str4;
                    webFile = null;
                } else {
                    photoSize = null;
                    webFile = photoSize;
                    document2 = webFile;
                    tLObject2 = document2;
                    str2 = tLObject2;
                    str3 = str2;
                    z = false;
                    i5 = 0;
                    i6 = 0;
                }
                Drawable bitmapDrawable;
                Drawable drawable2;
                if (document2 != null) {
                    imageReceiver.setImage(ImageLocation.getForDocument(document2), "d", bitmapHolder3 == null ? ImageLocation.getForDocument(FileLoader.getClosestPhotoSizeWithSize(document2.thumbs, 90), document2) : null, String.format(Locale.US, str, new Object[]{Integer.valueOf(photoSize2), Integer.valueOf(photoSize2)}), bitmapHolder3 != null ? new BitmapDrawable(bitmapHolder3.bitmap) : null, i5, null, obj, i6);
                } else if (photoSize != null) {
                    imageReceiver.setImage(ImageLocation.getForObject(photoSize, tLObject2), str2, bitmapHolder3 != null ? new BitmapDrawable(bitmapHolder3.bitmap) : null, i5, null, obj, i6);
                } else if (webFile != null) {
                    ImageLocation forWebFile = ImageLocation.getForWebFile(webFile);
                    if (bitmapHolder3 != null) {
                        bitmapDrawable = new BitmapDrawable(bitmapHolder3.bitmap);
                    } else {
                        if (z) {
                            Activity activity = this.parentActivity;
                            if (activity != null) {
                                bitmapDrawable = activity.getResources().getDrawable(NUM);
                            }
                        }
                        drawable2 = null;
                        imageReceiver.setImage(forWebFile, str2, drawable2, null, obj, i6);
                    }
                    drawable2 = bitmapDrawable;
                    imageReceiver.setImage(forWebFile, str2, drawable2, null, obj, i6);
                } else {
                    if (bitmapHolder3 != null) {
                        bitmapDrawable = new BitmapDrawable(bitmapHolder3.bitmap);
                    } else {
                        if (z) {
                            Activity activity2 = this.parentActivity;
                            if (activity2 != null) {
                                bitmapDrawable = activity2.getResources().getDrawable(NUM);
                            }
                        }
                        drawable2 = null;
                        imageReceiver.setImage(str3, str2, drawable2, null, i5);
                    }
                    drawable2 = bitmapDrawable;
                    imageReceiver.setImage(str3, str2, drawable2, null, i5);
                }
            }
        } else if (i2 >= 0 && i2 < this.secureDocuments.size()) {
            this.secureDocuments.get(i2);
            AndroidUtilities.getPhotoSize();
            float f = AndroidUtilities.density;
            BitmapHolder bitmapHolder4 = this.currentThumb;
            if (bitmapHolder4 == null || imageReceiver2 != this.centerImage) {
                bitmapHolder4 = null;
            }
            if (bitmapHolder4 == null) {
                bitmapHolder4 = this.placeProvider.getThumbForPhoto(null, null, i2);
            }
            SecureDocument secureDocument = (SecureDocument) this.secureDocuments.get(i2);
            int i8 = secureDocument.secureFile.size;
            ImageLocation forSecureDocument = ImageLocation.getForSecureDocument(secureDocument);
            if (bitmapHolder4 != null) {
                messageObject = new BitmapDrawable(bitmapHolder4.bitmap);
            }
            imageReceiver.setImage(forSecureDocument, "d", null, null, messageObject, i8, null, null, 0);
        }
    }

    public static boolean isShowingImage(MessageObject messageObject) {
        boolean z = (Instance == null || Instance.pipAnimationInProgress || !Instance.isVisible || Instance.disableShowCheck || messageObject == null || Instance.currentMessageObject == null || Instance.currentMessageObject.getId() != messageObject.getId() || Instance.currentMessageObject.getDialogId() != messageObject.getDialogId()) ? false : true;
        if (z || PipInstance == null) {
            return z;
        }
        return PipInstance.isVisible && !PipInstance.disableShowCheck && messageObject != null && PipInstance.currentMessageObject != null && PipInstance.currentMessageObject.getId() == messageObject.getId() && PipInstance.currentMessageObject.getDialogId() == messageObject.getDialogId();
    }

    public static boolean isPlayingMessageInPip(MessageObject messageObject) {
        return (PipInstance == null || messageObject == null || PipInstance.currentMessageObject == null || PipInstance.currentMessageObject.getId() != messageObject.getId() || PipInstance.currentMessageObject.getDialogId() != messageObject.getDialogId()) ? false : true;
    }

    public static boolean isPlayingMessage(MessageObject messageObject) {
        return (Instance == null || Instance.pipAnimationInProgress || !Instance.isVisible || messageObject == null || Instance.currentMessageObject == null || Instance.currentMessageObject.getId() != messageObject.getId() || Instance.currentMessageObject.getDialogId() != messageObject.getDialogId()) ? false : true;
    }

    public static boolean isShowingImage(FileLocation fileLocation) {
        if (Instance == null || !Instance.isVisible || Instance.disableShowCheck || fileLocation == null || Instance.currentFileLocation == null || fileLocation.local_id != Instance.currentFileLocation.location.local_id || fileLocation.volume_id != Instance.currentFileLocation.location.volume_id || fileLocation.dc_id != Instance.currentFileLocation.dc_id) {
            return false;
        }
        return true;
    }

    public static boolean isShowingImage(BotInlineResult botInlineResult) {
        if (Instance == null || !Instance.isVisible || Instance.disableShowCheck || botInlineResult == null || Instance.currentBotInlineResult == null || botInlineResult.id != Instance.currentBotInlineResult.id) {
            return false;
        }
        return true;
    }

    public static boolean isShowingImage(String str) {
        if (Instance == null || !Instance.isVisible || Instance.disableShowCheck || str == null || !str.equals(Instance.currentPathObject)) {
            return false;
        }
        return true;
    }

    public void setParentChatActivity(ChatActivity chatActivity) {
        this.parentChatActivity = chatActivity;
    }

    public void setMaxSelectedPhotos(int i, boolean z) {
        this.maxSelectedPhotos = i;
        this.allowOrder = z;
    }

    public boolean openPhoto(MessageObject messageObject, long j, long j2, PhotoViewerProvider photoViewerProvider) {
        return openPhoto(messageObject, null, null, null, null, null, 0, photoViewerProvider, null, j, j2, true);
    }

    public boolean openPhoto(MessageObject messageObject, long j, long j2, PhotoViewerProvider photoViewerProvider, boolean z) {
        return openPhoto(messageObject, null, null, null, null, null, 0, photoViewerProvider, null, j, j2, z);
    }

    public boolean openPhoto(FileLocation fileLocation, PhotoViewerProvider photoViewerProvider) {
        return openPhoto(null, fileLocation, null, null, null, null, 0, photoViewerProvider, null, 0, 0, true);
    }

    public boolean openPhoto(FileLocation fileLocation, ImageLocation imageLocation, PhotoViewerProvider photoViewerProvider) {
        return openPhoto(null, fileLocation, imageLocation, null, null, null, 0, photoViewerProvider, null, 0, 0, true);
    }

    public boolean openPhoto(ArrayList<MessageObject> arrayList, int i, long j, long j2, PhotoViewerProvider photoViewerProvider) {
        return openPhoto((MessageObject) arrayList.get(i), null, null, arrayList, null, null, i, photoViewerProvider, null, j, j2, true);
    }

    public boolean openPhoto(ArrayList<SecureDocument> arrayList, int i, PhotoViewerProvider photoViewerProvider) {
        return openPhoto(null, null, null, null, arrayList, null, i, photoViewerProvider, null, 0, 0, true);
    }

    public boolean openPhotoForSelect(ArrayList<Object> arrayList, int i, int i2, PhotoViewerProvider photoViewerProvider, ChatActivity chatActivity) {
        this.sendPhotoType = i2;
        ImageView imageView = this.pickerViewSendButton;
        if (imageView != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageView.getLayoutParams();
            int i3 = this.sendPhotoType;
            if (i3 == 4 || i3 == 5) {
                this.pickerViewSendButton.setImageResource(NUM);
                layoutParams.bottomMargin = AndroidUtilities.dp(19.0f);
            } else if (i3 == 1 || i3 == 3 || i3 == 10) {
                this.pickerViewSendButton.setImageResource(NUM);
                this.pickerViewSendButton.setPadding(0, AndroidUtilities.dp(1.0f), 0, 0);
                layoutParams.bottomMargin = AndroidUtilities.dp(19.0f);
            } else {
                this.pickerViewSendButton.setImageResource(NUM);
                layoutParams.bottomMargin = AndroidUtilities.dp(14.0f);
            }
            this.pickerViewSendButton.setLayoutParams(layoutParams);
        }
        return openPhoto(null, null, null, null, null, arrayList, i, photoViewerProvider, chatActivity, 0, 0, true);
    }

    private boolean checkAnimation() {
        if (this.animationInProgress != 0 && Math.abs(this.transitionAnimationStartTime - System.currentTimeMillis()) >= 500) {
            Runnable runnable = this.animationEndRunnable;
            if (runnable != null) {
                runnable.run();
                this.animationEndRunnable = null;
            }
            this.animationInProgress = 0;
        }
        if (this.animationInProgress != 0) {
            return true;
        }
        return false;
    }

    private void setCropTranslations(boolean z) {
        if (this.sendPhotoType == 1) {
            int bitmapWidth = this.centerImage.getBitmapWidth();
            int bitmapHeight = this.centerImage.getBitmapHeight();
            if (!(bitmapWidth == 0 || bitmapHeight == 0)) {
                float f = (float) bitmapWidth;
                float containerViewWidth = ((float) getContainerViewWidth()) / f;
                float f2 = (float) bitmapHeight;
                float containerViewHeight = ((float) getContainerViewHeight()) / f2;
                if (containerViewWidth > containerViewHeight) {
                    containerViewWidth = containerViewHeight;
                }
                containerViewHeight = (float) Math.min(getContainerViewWidth(1), getContainerViewHeight(1));
                f = containerViewHeight / f;
                f2 = containerViewHeight / f2;
                if (f <= f2) {
                    f = f2;
                }
                if (z) {
                    this.animationStartTime = System.currentTimeMillis();
                    this.animateToX = (float) ((getLeftInset() / 2) - (getRightInset() / 2));
                    int i = this.currentEditMode;
                    if (i == 2) {
                        this.animateToY = (float) (AndroidUtilities.dp(92.0f) - AndroidUtilities.dp(56.0f));
                    } else if (i == 3) {
                        this.animateToY = (float) (AndroidUtilities.dp(44.0f) - AndroidUtilities.dp(56.0f));
                    }
                    this.animateToScale = f / containerViewWidth;
                    this.zoomAnimation = true;
                } else {
                    this.animationStartTime = 0;
                    this.translationX = (float) ((getLeftInset() / 2) - (getRightInset() / 2));
                    this.translationY = (float) ((-AndroidUtilities.dp(56.0f)) + (VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight / 2 : 0));
                    this.scale = f / containerViewWidth;
                    updateMinMax(this.scale);
                }
            }
        }
    }

    private void setCropBitmap() {
        if (this.sendPhotoType == 1) {
            Bitmap bitmap = this.centerImage.getBitmap();
            int orientation = this.centerImage.getOrientation();
            if (bitmap == null) {
                bitmap = this.animatingImageView.getBitmap();
                orientation = this.animatingImageView.getOrientation();
            }
            if (bitmap != null) {
                this.photoCropView.setBitmap(bitmap, orientation, false, false);
                if (this.currentEditMode == 0) {
                    setCropTranslations(false);
                }
            }
        }
    }

    private void initCropView() {
        if (this.sendPhotoType == 1) {
            this.photoCropView.setBitmap(null, 0, false, false);
            this.photoCropView.onAppear();
            this.photoCropView.setVisibility(0);
            this.photoCropView.setAlpha(1.0f);
            this.photoCropView.onAppeared();
            this.padImageForHorizontalInsets = true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x005c A:{Catch:{ Exception -> 0x027c }} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0054 A:{Catch:{ Exception -> 0x027c }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0127  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x013e  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x013a  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x01fb A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0155  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x025f  */
    /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0048 */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Can't wrap try/catch for region: R(16:13|(2:15|16)|17|18|(1:20)(1:21)|(1:26)(1:25)|27|28|(1:30)|31|(1:33)(2:34|(1:36)(1:37))|38|(5:40|(2:42|(2:44|(5:46|(1:48)|49|(1:58)(1:57)|59)(2:60|(1:62))))|63|(1:65)|66)(2:(3:70|(1:72)(1:73)|74)|75)|76|(1:78)|79) */
    /* JADX WARNING: Missing block: B:80:0x027c, code skipped:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:81:0x027d, code skipped:
            org.telegram.messenger.FileLog.e(r0);
     */
    public boolean openPhoto(org.telegram.messenger.MessageObject r18, org.telegram.tgnet.TLRPC.FileLocation r19, org.telegram.messenger.ImageLocation r20, java.util.ArrayList<org.telegram.messenger.MessageObject> r21, java.util.ArrayList<org.telegram.messenger.SecureDocument> r22, java.util.ArrayList<java.lang.Object> r23, int r24, org.telegram.ui.PhotoViewer.PhotoViewerProvider r25, org.telegram.ui.ChatActivity r26, long r27, long r29, boolean r31) {
        /*
        r17 = this;
        r10 = r17;
        r0 = r18;
        r3 = r19;
        r11 = r23;
        r1 = r25;
        r2 = r26;
        r4 = r10.parentActivity;
        r12 = 0;
        if (r4 == 0) goto L_0x0280;
    L_0x0011:
        r4 = r10.isVisible;
        if (r4 != 0) goto L_0x0280;
    L_0x0015:
        if (r1 != 0) goto L_0x001d;
    L_0x0017:
        r4 = r17.checkAnimation();
        if (r4 != 0) goto L_0x0280;
    L_0x001d:
        if (r0 != 0) goto L_0x002b;
    L_0x001f:
        if (r3 != 0) goto L_0x002b;
    L_0x0021:
        if (r21 != 0) goto L_0x002b;
    L_0x0023:
        if (r11 != 0) goto L_0x002b;
    L_0x0025:
        if (r22 != 0) goto L_0x002b;
    L_0x0027:
        if (r20 != 0) goto L_0x002b;
    L_0x0029:
        goto L_0x0280;
    L_0x002b:
        r13 = 1;
        r8 = r24;
        r14 = r1.getPlaceForPhoto(r0, r3, r8, r13);
        r4 = 0;
        r10.lastInsets = r4;
        r5 = r10.parentActivity;
        r6 = "window";
        r5 = r5.getSystemService(r6);
        r5 = (android.view.WindowManager) r5;
        r6 = r10.attachedToWindow;
        if (r6 == 0) goto L_0x0048;
    L_0x0043:
        r6 = r10.windowView;	 Catch:{ Exception -> 0x0048 }
        r5.removeView(r6);	 Catch:{ Exception -> 0x0048 }
    L_0x0048:
        r6 = r10.windowLayoutParams;	 Catch:{ Exception -> 0x027c }
        r7 = 99;
        r6.type = r7;	 Catch:{ Exception -> 0x027c }
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x027c }
        r7 = 21;
        if (r6 < r7) goto L_0x005c;
    L_0x0054:
        r6 = r10.windowLayoutParams;	 Catch:{ Exception -> 0x027c }
        r9 = -NUM; // 0xfffffffvar_ float:-2.75865E-40 double:NaN;
        r6.flags = r9;	 Catch:{ Exception -> 0x027c }
        goto L_0x0062;
    L_0x005c:
        r6 = r10.windowLayoutParams;	 Catch:{ Exception -> 0x027c }
        r9 = 131072; // 0x20000 float:1.83671E-40 double:6.47582E-319;
        r6.flags = r9;	 Catch:{ Exception -> 0x027c }
    L_0x0062:
        if (r2 == 0) goto L_0x0073;
    L_0x0064:
        r6 = r26.getCurrentEncryptedChat();	 Catch:{ Exception -> 0x027c }
        if (r6 == 0) goto L_0x0073;
    L_0x006a:
        r6 = r10.windowLayoutParams;	 Catch:{ Exception -> 0x027c }
        r9 = r6.flags;	 Catch:{ Exception -> 0x027c }
        r9 = r9 | 8192;
        r6.flags = r9;	 Catch:{ Exception -> 0x027c }
        goto L_0x007b;
    L_0x0073:
        r6 = r10.windowLayoutParams;	 Catch:{ Exception -> 0x027c }
        r9 = r6.flags;	 Catch:{ Exception -> 0x027c }
        r9 = r9 & -8193;
        r6.flags = r9;	 Catch:{ Exception -> 0x027c }
    L_0x007b:
        r6 = r10.windowLayoutParams;	 Catch:{ Exception -> 0x027c }
        r9 = 272; // 0x110 float:3.81E-43 double:1.344E-321;
        r6.softInputMode = r9;	 Catch:{ Exception -> 0x027c }
        r6 = r10.windowView;	 Catch:{ Exception -> 0x027c }
        r6.setFocusable(r12);	 Catch:{ Exception -> 0x027c }
        r6 = r10.containerView;	 Catch:{ Exception -> 0x027c }
        r6.setFocusable(r12);	 Catch:{ Exception -> 0x027c }
        r6 = r10.windowView;	 Catch:{ Exception -> 0x027c }
        r15 = r10.windowLayoutParams;	 Catch:{ Exception -> 0x027c }
        r5.addView(r6, r15);	 Catch:{ Exception -> 0x027c }
        r10.doneButtonPressed = r12;
        r10.parentChatActivity = r2;
        r2 = r10.actionBar;
        r6 = NUM; // 0x7f0e0733 float:1.8878776E38 double:1.053163067E-314;
        r15 = 2;
        r15 = new java.lang.Object[r15];
        r16 = java.lang.Integer.valueOf(r13);
        r15[r12] = r16;
        r16 = java.lang.Integer.valueOf(r13);
        r15[r13] = r16;
        r9 = "Of";
        r6 = org.telegram.messenger.LocaleController.formatString(r9, r6, r15);
        r2.setTitle(r6);
        r2 = r10.currentAccount;
        r2 = org.telegram.messenger.NotificationCenter.getInstance(r2);
        r6 = org.telegram.messenger.NotificationCenter.fileDidFailToLoad;
        r2.addObserver(r10, r6);
        r2 = r10.currentAccount;
        r2 = org.telegram.messenger.NotificationCenter.getInstance(r2);
        r6 = org.telegram.messenger.NotificationCenter.fileDidLoad;
        r2.addObserver(r10, r6);
        r2 = r10.currentAccount;
        r2 = org.telegram.messenger.NotificationCenter.getInstance(r2);
        r6 = org.telegram.messenger.NotificationCenter.FileLoadProgressChanged;
        r2.addObserver(r10, r6);
        r2 = r10.currentAccount;
        r2 = org.telegram.messenger.NotificationCenter.getInstance(r2);
        r6 = org.telegram.messenger.NotificationCenter.mediaCountDidLoad;
        r2.addObserver(r10, r6);
        r2 = r10.currentAccount;
        r2 = org.telegram.messenger.NotificationCenter.getInstance(r2);
        r6 = org.telegram.messenger.NotificationCenter.mediaDidLoad;
        r2.addObserver(r10, r6);
        r2 = r10.currentAccount;
        r2 = org.telegram.messenger.NotificationCenter.getInstance(r2);
        r6 = org.telegram.messenger.NotificationCenter.dialogPhotosLoaded;
        r2.addObserver(r10, r6);
        r2 = org.telegram.messenger.NotificationCenter.getGlobalInstance();
        r6 = org.telegram.messenger.NotificationCenter.emojiDidLoad;
        r2.addObserver(r10, r6);
        r2 = r10.currentAccount;
        r2 = org.telegram.messenger.NotificationCenter.getInstance(r2);
        r6 = org.telegram.messenger.NotificationCenter.filePreparingFailed;
        r2.addObserver(r10, r6);
        r2 = r10.currentAccount;
        r2 = org.telegram.messenger.NotificationCenter.getInstance(r2);
        r6 = org.telegram.messenger.NotificationCenter.fileNewChunkAvailable;
        r2.addObserver(r10, r6);
        r10.placeProvider = r1;
        r1 = r29;
        r10.mergeDialogId = r1;
        r1 = r27;
        r10.currentDialogId = r1;
        r1 = r10.selectedPhotosAdapter;
        r1.notifyDataSetChanged();
        r1 = r10.velocityTracker;
        if (r1 != 0) goto L_0x012d;
    L_0x0127:
        r1 = android.view.VelocityTracker.obtain();
        r10.velocityTracker = r1;
    L_0x012d:
        r10.isVisible = r13;
        r10.togglePhotosListView(r12, r12);
        r1 = r31 ^ 1;
        r10.openedFullScreenVideo = r1;
        r1 = r10.openedFullScreenVideo;
        if (r1 == 0) goto L_0x013e;
    L_0x013a:
        r10.toggleActionBar(r12, r12);
        goto L_0x014c;
    L_0x013e:
        r1 = r10.sendPhotoType;
        if (r1 != r13) goto L_0x0149;
    L_0x0142:
        r17.createCropView();
        r10.toggleActionBar(r12, r12);
        goto L_0x014c;
    L_0x0149:
        r10.toggleActionBar(r13, r12);
    L_0x014c:
        r15 = 0;
        r10.seekToProgressPending2 = r15;
        r10.skipFirstBufferingProgress = r12;
        r10.playerInjected = r12;
        if (r14 == 0) goto L_0x01fb;
    L_0x0155:
        r10.disableShowCheck = r13;
        r10.animationInProgress = r13;
        if (r0 == 0) goto L_0x01c5;
    L_0x015b:
        r1 = r14.imageReceiver;
        r1 = r1.getAnimation();
        r10.currentAnimation = r1;
        r1 = r10.currentAnimation;
        if (r1 == 0) goto L_0x01c5;
    L_0x0167:
        r1 = r18.isVideo();
        if (r1 == 0) goto L_0x01b9;
    L_0x016d:
        r1 = r14.imageReceiver;
        r1.setAllowStartAnimation(r12);
        r1 = r14.imageReceiver;
        r1.stopAnimation();
        r1 = org.telegram.messenger.MediaController.getInstance();
        r1 = r1.isPlayingMessage(r0);
        if (r1 == 0) goto L_0x0185;
    L_0x0181:
        r1 = r0.audioProgress;
        r10.seekToProgressPending2 = r1;
    L_0x0185:
        r1 = r10.injectingVideoPlayer;
        if (r1 != 0) goto L_0x01b3;
    L_0x0189:
        r1 = r0.currentAccount;
        r1 = org.telegram.messenger.FileLoader.getInstance(r1);
        r2 = r18.getDocument();
        r1 = r1.isLoadingVideo(r2, r13);
        if (r1 != 0) goto L_0x01b3;
    L_0x0199:
        r1 = r10.currentAnimation;
        r1 = r1.hasBitmap();
        if (r1 != 0) goto L_0x01b1;
    L_0x01a1:
        r1 = r0.currentAccount;
        r1 = org.telegram.messenger.FileLoader.getInstance(r1);
        r2 = r18.getDocument();
        r1 = r1.isLoadingVideo(r2, r12);
        if (r1 != 0) goto L_0x01b3;
    L_0x01b1:
        r1 = 1;
        goto L_0x01b4;
    L_0x01b3:
        r1 = 0;
    L_0x01b4:
        r10.skipFirstBufferingProgress = r1;
        r10.currentAnimation = r4;
        goto L_0x01c5;
    L_0x01b9:
        r1 = r0.getWebPagePhotos(r4, r4);
        r1 = r1.size();
        if (r1 <= r13) goto L_0x01c5;
    L_0x01c3:
        r10.currentAnimation = r4;
    L_0x01c5:
        r1 = r17;
        r2 = r18;
        r3 = r19;
        r4 = r20;
        r5 = r21;
        r6 = r22;
        r7 = r23;
        r8 = r24;
        r9 = r14;
        r1.onPhotoShow(r2, r3, r4, r5, r6, r7, r8, r9);
        r0 = r10.sendPhotoType;
        if (r0 != r13) goto L_0x01ec;
    L_0x01dd:
        r0 = r10.photoCropView;
        r0.setVisibility(r12);
        r0 = r10.photoCropView;
        r0.setAlpha(r15);
        r0 = r10.photoCropView;
        r0.setFreeform(r12);
    L_0x01ec:
        r0 = r10.windowView;
        r0 = r0.getViewTreeObserver();
        r1 = new org.telegram.ui.PhotoViewer$40;
        r1.<init>(r14, r11);
        r0.addOnPreDrawListener(r1);
        goto L_0x024f;
    L_0x01fb:
        if (r11 == 0) goto L_0x0227;
    L_0x01fd:
        r1 = r10.sendPhotoType;
        r2 = 3;
        if (r1 == r2) goto L_0x0227;
    L_0x0202:
        r1 = android.os.Build.VERSION.SDK_INT;
        if (r1 < r7) goto L_0x020e;
    L_0x0206:
        r1 = r10.windowLayoutParams;
        r2 = -NUM; // 0xfffffffvar_ float:-9.2194E-41 double:NaN;
        r1.flags = r2;
        goto L_0x0212;
    L_0x020e:
        r1 = r10.windowLayoutParams;
        r1.flags = r12;
    L_0x0212:
        r1 = r10.windowLayoutParams;
        r2 = 272; // 0x110 float:3.81E-43 double:1.344E-321;
        r1.softInputMode = r2;
        r2 = r10.windowView;
        r5.updateViewLayout(r2, r1);
        r1 = r10.windowView;
        r1.setFocusable(r13);
        r1 = r10.containerView;
        r1.setFocusable(r13);
    L_0x0227:
        r1 = r10.backgroundDrawable;
        r2 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r1.setAlpha(r2);
        r1 = r10.containerView;
        r2 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r1.setAlpha(r2);
        r1 = r17;
        r2 = r18;
        r3 = r19;
        r4 = r20;
        r5 = r21;
        r6 = r22;
        r7 = r23;
        r8 = r24;
        r9 = r14;
        r1.onPhotoShow(r2, r3, r4, r5, r6, r7, r8, r9);
        r17.initCropView();
        r17.setCropBitmap();
    L_0x024f:
        r0 = r10.parentActivity;
        r1 = "accessibility";
        r0 = r0.getSystemService(r1);
        r0 = (android.view.accessibility.AccessibilityManager) r0;
        r1 = r0.isTouchExplorationEnabled();
        if (r1 == 0) goto L_0x027b;
    L_0x025f:
        r1 = android.view.accessibility.AccessibilityEvent.obtain();
        r2 = 16384; // 0x4000 float:2.2959E-41 double:8.0948E-320;
        r1.setEventType(r2);
        r2 = r1.getText();
        r3 = NUM; // 0x7f0e003a float:1.8875155E38 double:1.0531621853E-314;
        r4 = "AccDescrPhotoViewer";
        r3 = org.telegram.messenger.LocaleController.getString(r4, r3);
        r2.add(r3);
        r0.sendAccessibilityEvent(r1);
    L_0x027b:
        return r13;
    L_0x027c:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x0280:
        return r12;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.openPhoto(org.telegram.messenger.MessageObject, org.telegram.tgnet.TLRPC$FileLocation, org.telegram.messenger.ImageLocation, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, int, org.telegram.ui.PhotoViewer$PhotoViewerProvider, org.telegram.ui.ChatActivity, long, long, boolean):boolean");
    }

    public void injectVideoPlayerToMediaController() {
        if (this.videoPlayer.isPlaying()) {
            MediaController.getInstance().injectVideoPlayer(this.videoPlayer, this.currentMessageObject);
            this.videoPlayer = null;
            updateAccessibilityOverlayVisibility();
        }
    }

    public void closePhoto(boolean z, boolean z2) {
        int i;
        int i2 = 3;
        if (!z2) {
            i = this.currentEditMode;
            if (i != 0) {
                if (i == 3) {
                    PhotoPaintView photoPaintView = this.photoPaintView;
                    if (photoPaintView != null) {
                        photoPaintView.maybeShowDismissalAlert(this, this.parentActivity, new -$$Lambda$PhotoViewer$bfU4s37je-nMbLzGkZwQ700TwGs(this));
                        return;
                    }
                }
                switchToEditMode(0);
                return;
            }
        }
        QualityChooseView qualityChooseView = this.qualityChooseView;
        if (qualityChooseView == null || qualityChooseView.getTag() == null) {
            this.openedFullScreenVideo = false;
            try {
                if (this.visibleDialog != null) {
                    this.visibleDialog.dismiss();
                    this.visibleDialog = null;
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (VERSION.SDK_INT >= 21 && this.actionBar != null) {
                i = this.containerView.getSystemUiVisibility() & 4102;
                if (i != 0) {
                    FrameLayoutDrawer frameLayoutDrawer = this.containerView;
                    frameLayoutDrawer.setSystemUiVisibility((i ^ -1) & frameLayoutDrawer.getSystemUiVisibility());
                }
            }
            i = this.currentEditMode;
            if (i != 0) {
                if (i == 2) {
                    this.photoFilterView.shutdown();
                    this.containerView.removeView(this.photoFilterView);
                    this.photoFilterView = null;
                } else if (i == 1) {
                    this.editorDoneLayout.setVisibility(8);
                    this.photoCropView.setVisibility(8);
                } else if (i == 3) {
                    this.photoPaintView.shutdown();
                    this.containerView.removeView(this.photoPaintView);
                    this.photoPaintView = null;
                }
                this.currentEditMode = 0;
            } else if (this.sendPhotoType == 1) {
                this.photoCropView.setVisibility(8);
            }
            if (this.parentActivity != null && ((this.isInline || this.isVisible) && !checkAnimation() && this.placeProvider != null && (!this.captionEditText.hideActionMode() || z2))) {
                PlaceProviderObject placeForPhoto = this.placeProvider.getPlaceForPhoto(this.currentMessageObject, getFileLocation(this.currentFileLocation), this.currentIndex, true);
                if (!(this.videoPlayer == null || placeForPhoto == null)) {
                    AnimatedFileDrawable animation = placeForPhoto.imageReceiver.getAnimation();
                    if (animation != null) {
                        if (this.textureUploaded) {
                            Bitmap animatedBitmap = animation.getAnimatedBitmap();
                            if (animatedBitmap != null) {
                                try {
                                    Bitmap bitmap = this.videoTextureView.getBitmap(animatedBitmap.getWidth(), animatedBitmap.getHeight());
                                    new Canvas(animatedBitmap).drawBitmap(bitmap, 0.0f, 0.0f, null);
                                    bitmap.recycle();
                                } catch (Throwable e2) {
                                    FileLog.e(e2);
                                }
                            }
                        }
                        animation.seekTo(this.videoPlayer.getCurrentPosition(), FileLoader.getInstance(this.currentMessageObject.currentAccount).isLoadingVideo(this.currentMessageObject.getDocument(), true) ^ 1);
                        placeForPhoto.imageReceiver.setAllowStartAnimation(true);
                        placeForPhoto.imageReceiver.startAnimation();
                    }
                }
                releasePlayer(true);
                this.captionEditText.onDestroy();
                this.parentChatActivity = null;
                removeObservers();
                this.isActionBarVisible = false;
                VelocityTracker velocityTracker = this.velocityTracker;
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    this.velocityTracker = null;
                }
                if (this.isInline) {
                    this.isInline = false;
                    this.animationInProgress = 0;
                    onPhotoClosed(placeForPhoto);
                    this.containerView.setScaleX(1.0f);
                    this.containerView.setScaleY(1.0f);
                } else {
                    Drawable drawable;
                    Animator[] animatorArr;
                    if (z) {
                        RectF drawRegion;
                        int animatedOrientation;
                        this.animationInProgress = 1;
                        this.animatingImageView.setVisibility(0);
                        this.containerView.invalidate();
                        AnimatorSet animatorSet = new AnimatorSet();
                        ViewGroup.LayoutParams layoutParams = this.animatingImageView.getLayoutParams();
                        if (placeForPhoto != null) {
                            this.animatingImageView.setNeedRadius(placeForPhoto.radius != 0);
                            drawRegion = placeForPhoto.imageReceiver.getDrawRegion();
                            layoutParams.width = (int) drawRegion.width();
                            layoutParams.height = (int) drawRegion.height();
                            i = placeForPhoto.imageReceiver.getOrientation();
                            animatedOrientation = placeForPhoto.imageReceiver.getAnimatedOrientation();
                            if (animatedOrientation != 0) {
                                i = animatedOrientation;
                            }
                            this.animatingImageView.setOrientation(i);
                            this.animatingImageView.setImageBitmap(placeForPhoto.thumb);
                        } else {
                            this.animatingImageView.setNeedRadius(false);
                            layoutParams.width = this.centerImage.getImageWidth();
                            layoutParams.height = this.centerImage.getImageHeight();
                            this.animatingImageView.setOrientation(this.centerImage.getOrientation());
                            this.animatingImageView.setImageBitmap(this.centerImage.getBitmapSafe());
                            drawRegion = null;
                        }
                        if (layoutParams.width == 0) {
                            layoutParams.width = 1;
                        }
                        if (layoutParams.height == 0) {
                            layoutParams.height = 1;
                        }
                        this.animatingImageView.setLayoutParams(layoutParams);
                        float measuredWidth = ((float) this.windowView.getMeasuredWidth()) / ((float) layoutParams.width);
                        float f = ((float) (AndroidUtilities.displaySize.y + (VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0))) / ((float) layoutParams.height);
                        if (measuredWidth > f) {
                            measuredWidth = f;
                        }
                        f = (float) layoutParams.width;
                        float f2 = this.scale;
                        float f3 = (((float) (AndroidUtilities.displaySize.y + (VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0))) - ((((float) layoutParams.height) * f2) * measuredWidth)) / 2.0f;
                        this.animatingImageView.setTranslationX(((((float) this.windowView.getMeasuredWidth()) - ((f * f2) * measuredWidth)) / 2.0f) + this.translationX);
                        this.animatingImageView.setTranslationY(f3 + this.translationY);
                        this.animatingImageView.setScaleX(this.scale * measuredWidth);
                        this.animatingImageView.setScaleY(this.scale * measuredWidth);
                        if (placeForPhoto != null) {
                            placeForPhoto.imageReceiver.setVisible(false, true);
                            i = (int) Math.abs(drawRegion.left - ((float) placeForPhoto.imageReceiver.getImageX()));
                            int abs = (int) Math.abs(drawRegion.top - ((float) placeForPhoto.imageReceiver.getImageY()));
                            int[] iArr = new int[2];
                            placeForPhoto.parentView.getLocationInWindow(iArr);
                            animatedOrientation = (int) ((((float) (iArr[1] - (VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight))) - (((float) placeForPhoto.viewY) + drawRegion.top)) + ((float) placeForPhoto.clipTopAddition));
                            if (animatedOrientation < 0) {
                                animatedOrientation = 0;
                            }
                            f2 = (float) placeForPhoto.viewY;
                            float f4 = drawRegion.top;
                            int height = (int) ((((f2 + f4) + (drawRegion.bottom - f4)) - ((float) ((iArr[1] + placeForPhoto.parentView.getHeight()) - (VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight)))) + ((float) placeForPhoto.clipBottomAddition));
                            if (height < 0) {
                                height = 0;
                            }
                            int max = Math.max(animatedOrientation, abs);
                            height = Math.max(height, abs);
                            this.animationValues[0][0] = this.animatingImageView.getScaleX();
                            this.animationValues[0][1] = this.animatingImageView.getScaleY();
                            this.animationValues[0][2] = this.animatingImageView.getTranslationX();
                            this.animationValues[0][3] = this.animatingImageView.getTranslationY();
                            float[][] fArr = this.animationValues;
                            fArr[0][4] = 0.0f;
                            fArr[0][5] = 0.0f;
                            fArr[0][6] = 0.0f;
                            fArr[0][7] = 0.0f;
                            fArr[0][8] = 0.0f;
                            fArr[0][9] = 0.0f;
                            float[] fArr2 = fArr[1];
                            f4 = placeForPhoto.scale;
                            fArr2[0] = f4;
                            fArr[1][1] = f4;
                            fArr[1][2] = ((float) placeForPhoto.viewX) + (drawRegion.left * f4);
                            fArr[1][3] = ((float) placeForPhoto.viewY) + (drawRegion.top * f4);
                            measuredWidth = (float) i;
                            fArr[1][4] = measuredWidth * f4;
                            fArr[1][5] = ((float) max) * f4;
                            fArr[1][6] = ((float) height) * f4;
                            fArr[1][7] = (float) placeForPhoto.radius;
                            fArr[1][8] = ((float) abs) * f4;
                            fArr[1][9] = measuredWidth * f4;
                            if (this.sendPhotoType == 1) {
                                i2 = 4;
                            }
                            ArrayList arrayList = new ArrayList(i2);
                            arrayList.add(ObjectAnimator.ofFloat(this.animatingImageView, AnimationProperties.CLIPPING_IMAGE_VIEW_PROGRESS, new float[]{0.0f, 1.0f}));
                            arrayList.add(ObjectAnimator.ofInt(this.backgroundDrawable, AnimationProperties.COLOR_DRAWABLE_ALPHA, new int[]{0}));
                            arrayList.add(ObjectAnimator.ofFloat(this.containerView, View.ALPHA, new float[]{0.0f}));
                            if (this.sendPhotoType == 1) {
                                arrayList.add(ObjectAnimator.ofFloat(this.photoCropView, View.ALPHA, new float[]{0.0f}));
                            }
                            animatorSet.playTogether(arrayList);
                        } else {
                            i = AndroidUtilities.displaySize.y + (VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
                            animatorArr = new Animator[4];
                            animatorArr[0] = ObjectAnimator.ofInt(this.backgroundDrawable, AnimationProperties.COLOR_DRAWABLE_ALPHA, new int[]{0});
                            animatorArr[1] = ObjectAnimator.ofFloat(this.animatingImageView, View.ALPHA, new float[]{0.0f});
                            ClippingImageView clippingImageView = this.animatingImageView;
                            Property property = View.TRANSLATION_Y;
                            float[] fArr3 = new float[1];
                            if (this.translationY < 0.0f) {
                                i = -i;
                            }
                            fArr3[0] = (float) i;
                            animatorArr[2] = ObjectAnimator.ofFloat(clippingImageView, property, fArr3);
                            animatorArr[3] = ObjectAnimator.ofFloat(this.containerView, View.ALPHA, new float[]{0.0f});
                            animatorSet.playTogether(animatorArr);
                        }
                        this.animationEndRunnable = new -$$Lambda$PhotoViewer$mGM60iC_P4fh9CnbEk9TbbVIpVQ(this, placeForPhoto);
                        animatorSet.setDuration(200);
                        animatorSet.addListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animator) {
                                AndroidUtilities.runOnUIThread(new -$$Lambda$PhotoViewer$41$zVPp9kWNm7HNZtSbgwtWUuDr3b8(this));
                            }

                            public /* synthetic */ void lambda$onAnimationEnd$0$PhotoViewer$41() {
                                if (PhotoViewer.this.animationEndRunnable != null) {
                                    PhotoViewer.this.animationEndRunnable.run();
                                    PhotoViewer.this.animationEndRunnable = null;
                                }
                            }
                        });
                        this.transitionAnimationStartTime = System.currentTimeMillis();
                        if (VERSION.SDK_INT >= 18) {
                            this.containerView.setLayerType(2, null);
                        }
                        animatorSet.start();
                        drawable = null;
                    } else {
                        AnimatorSet animatorSet2 = new AnimatorSet();
                        animatorArr = new Animator[4];
                        animatorArr[0] = ObjectAnimator.ofFloat(this.containerView, View.SCALE_X, new float[]{0.9f});
                        animatorArr[1] = ObjectAnimator.ofFloat(this.containerView, View.SCALE_Y, new float[]{0.9f});
                        animatorArr[2] = ObjectAnimator.ofInt(this.backgroundDrawable, AnimationProperties.COLOR_DRAWABLE_ALPHA, new int[]{0});
                        animatorArr[3] = ObjectAnimator.ofFloat(this.containerView, View.ALPHA, new float[]{0.0f});
                        animatorSet2.playTogether(animatorArr);
                        this.animationInProgress = 2;
                        this.animationEndRunnable = new -$$Lambda$PhotoViewer$iGASJJZ8pX_oKwIrDoX93gGyCFM(this, placeForPhoto);
                        animatorSet2.setDuration(200);
                        animatorSet2.addListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animator) {
                                if (PhotoViewer.this.animationEndRunnable != null) {
                                    PhotoViewer.this.animationEndRunnable.run();
                                    PhotoViewer.this.animationEndRunnable = null;
                                }
                            }
                        });
                        this.transitionAnimationStartTime = System.currentTimeMillis();
                        if (VERSION.SDK_INT >= 18) {
                            drawable = null;
                            this.containerView.setLayerType(2, null);
                        } else {
                            drawable = null;
                        }
                        animatorSet2.start();
                    }
                    AnimatedFileDrawable animatedFileDrawable = this.currentAnimation;
                    if (animatedFileDrawable != null) {
                        animatedFileDrawable.setSecondParentView(drawable);
                        this.currentAnimation = drawable;
                        this.centerImage.setImageBitmap(drawable);
                    }
                    PhotoViewerProvider photoViewerProvider = this.placeProvider;
                    if (!(photoViewerProvider == null || photoViewerProvider.canScrollAway())) {
                        this.placeProvider.cancelButtonPressed();
                    }
                }
                return;
            }
            return;
        }
        this.qualityPicker.cancelButton.callOnClick();
    }

    public /* synthetic */ void lambda$closePhoto$44$PhotoViewer() {
        switchToEditMode(0);
    }

    public /* synthetic */ void lambda$closePhoto$45$PhotoViewer(PlaceProviderObject placeProviderObject) {
        if (VERSION.SDK_INT >= 18) {
            this.containerView.setLayerType(0, null);
        }
        this.animationInProgress = 0;
        onPhotoClosed(placeProviderObject);
    }

    public /* synthetic */ void lambda$closePhoto$46$PhotoViewer(PlaceProviderObject placeProviderObject) {
        FrameLayoutDrawer frameLayoutDrawer = this.containerView;
        if (frameLayoutDrawer != null) {
            if (VERSION.SDK_INT >= 18) {
                frameLayoutDrawer.setLayerType(0, null);
            }
            this.animationInProgress = 0;
            onPhotoClosed(placeProviderObject);
            this.containerView.setScaleX(1.0f);
            this.containerView.setScaleY(1.0f);
        }
    }

    private void removeObservers() {
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.fileDidFailToLoad);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.fileDidLoad);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileLoadProgressChanged);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mediaCountDidLoad);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mediaDidLoad);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.dialogPhotosLoaded);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoad);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.filePreparingFailed);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.fileNewChunkAvailable);
        ConnectionsManager.getInstance(this.currentAccount).cancelRequestsForGuid(this.classGuid);
    }

    public void destroyPhotoViewer() {
        if (this.parentActivity != null && this.windowView != null) {
            PipVideoView pipVideoView = this.pipVideoView;
            if (pipVideoView != null) {
                pipVideoView.close();
                this.pipVideoView = null;
            }
            removeObservers();
            releasePlayer(false);
            try {
                if (this.windowView.getParent() != null) {
                    ((WindowManager) this.parentActivity.getSystemService("window")).removeViewImmediate(this.windowView);
                }
                this.windowView = null;
            } catch (Exception e) {
                FileLog.e(e);
            }
            BitmapHolder bitmapHolder = this.currentThumb;
            if (bitmapHolder != null) {
                bitmapHolder.release();
                this.currentThumb = null;
            }
            this.animatingImageView.setImageBitmap(null);
            PhotoViewerCaptionEnterView photoViewerCaptionEnterView = this.captionEditText;
            if (photoViewerCaptionEnterView != null) {
                photoViewerCaptionEnterView.onDestroy();
            }
            if (this == PipInstance) {
                PipInstance = null;
            } else {
                Instance = null;
            }
        }
    }

    private void onPhotoClosed(PlaceProviderObject placeProviderObject) {
        this.isVisible = false;
        this.disableShowCheck = true;
        this.currentMessageObject = null;
        this.currentBotInlineResult = null;
        this.currentFileLocation = null;
        this.currentSecureDocument = null;
        this.currentPathObject = null;
        FrameLayout frameLayout = this.videoPlayerControlFrameLayout;
        if (frameLayout != null) {
            frameLayout.setVisibility(8);
            this.dateTextView.setVisibility(0);
            this.nameTextView.setVisibility(0);
        }
        this.sendPhotoType = 0;
        BitmapHolder bitmapHolder = this.currentThumb;
        if (bitmapHolder != null) {
            bitmapHolder.release();
            this.currentThumb = null;
        }
        this.parentAlert = null;
        AnimatedFileDrawable animatedFileDrawable = this.currentAnimation;
        if (animatedFileDrawable != null) {
            animatedFileDrawable.setSecondParentView(null);
            this.currentAnimation = null;
        }
        for (int i = 0; i < 3; i++) {
            PhotoProgressView[] photoProgressViewArr = this.photoProgressViews;
            if (photoProgressViewArr[i] != null) {
                photoProgressViewArr[i].setBackgroundState(-1, false);
            }
        }
        requestVideoPreview(0);
        VideoTimelinePlayView videoTimelinePlayView = this.videoTimelineView;
        if (videoTimelinePlayView != null) {
            videoTimelinePlayView.destroy();
        }
        this.centerImage.setImageBitmap(null);
        this.leftImage.setImageBitmap(null);
        this.rightImage.setImageBitmap(null);
        this.containerView.post(new -$$Lambda$PhotoViewer$wFOeSTXQOhJr0lZX9F9aD_JNaTs(this));
        PhotoViewerProvider photoViewerProvider = this.placeProvider;
        if (photoViewerProvider != null) {
            photoViewerProvider.willHidePhotoViewer();
        }
        this.groupedPhotosListView.clear();
        this.placeProvider = null;
        this.selectedPhotosAdapter.notifyDataSetChanged();
        this.disableShowCheck = false;
        if (placeProviderObject != null) {
            placeProviderObject.imageReceiver.setVisible(true, true);
        }
    }

    public /* synthetic */ void lambda$onPhotoClosed$47$PhotoViewer() {
        this.animatingImageView.setImageBitmap(null);
        try {
            if (this.windowView.getParent() != null) {
                ((WindowManager) this.parentActivity.getSystemService("window")).removeView(this.windowView);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void redraw(int i) {
        if (i < 6) {
            FrameLayoutDrawer frameLayoutDrawer = this.containerView;
            if (frameLayoutDrawer != null) {
                frameLayoutDrawer.invalidate();
                AndroidUtilities.runOnUIThread(new -$$Lambda$PhotoViewer$MrwxbvU2NEMCLASSNAMEPniIifKaiVaZc(this, i), 100);
            }
        }
    }

    public /* synthetic */ void lambda$redraw$48$PhotoViewer(int i) {
        redraw(i + 1);
    }

    public void onResume() {
        redraw(0);
        VideoPlayer videoPlayer = this.videoPlayer;
        if (videoPlayer != null) {
            videoPlayer.seekTo(videoPlayer.getCurrentPosition() + 1);
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        PipVideoView pipVideoView = this.pipVideoView;
        if (pipVideoView != null) {
            pipVideoView.onConfigurationChanged();
        }
    }

    public void onPause() {
        if (this.currentAnimation != null) {
            closePhoto(false, false);
            return;
        }
        if (this.lastTitle != null) {
            closeCaptionEnter(true);
        }
    }

    public boolean isVisible() {
        return this.isVisible && this.placeProvider != null;
    }

    private void updateMinMax(float f) {
        int imageWidth = ((int) ((((float) this.centerImage.getImageWidth()) * f) - ((float) getContainerViewWidth()))) / 2;
        int imageHeight = ((int) ((((float) this.centerImage.getImageHeight()) * f) - ((float) getContainerViewHeight()))) / 2;
        if (imageWidth > 0) {
            this.minX = (float) (-imageWidth);
            this.maxX = (float) imageWidth;
        } else {
            this.maxX = 0.0f;
            this.minX = 0.0f;
        }
        if (imageHeight > 0) {
            this.minY = (float) (-imageHeight);
            this.maxY = (float) imageHeight;
            return;
        }
        this.maxY = 0.0f;
        this.minY = 0.0f;
    }

    private int getAdditionX() {
        int i = this.currentEditMode;
        return (i == 0 || i == 3) ? 0 : AndroidUtilities.dp(14.0f);
    }

    private int getAdditionY() {
        int i = this.currentEditMode;
        int i2 = 0;
        if (i == 3) {
            i = AndroidUtilities.dp(8.0f);
            if (VERSION.SDK_INT >= 21) {
                i2 = AndroidUtilities.statusBarHeight;
            }
            return i + i2;
        } else if (i == 0) {
            return 0;
        } else {
            i = AndroidUtilities.dp(14.0f);
            if (VERSION.SDK_INT >= 21) {
                i2 = AndroidUtilities.statusBarHeight;
            }
            return i + i2;
        }
    }

    private int getContainerViewWidth() {
        return getContainerViewWidth(this.currentEditMode);
    }

    private int getContainerViewWidth(int i) {
        int width = this.containerView.getWidth();
        return (i == 0 || i == 3) ? width : width - AndroidUtilities.dp(28.0f);
    }

    private int getContainerViewHeight() {
        return getContainerViewHeight(this.currentEditMode);
    }

    private int getContainerViewHeight(int i) {
        int i2 = AndroidUtilities.displaySize.y;
        if (i == 0 && VERSION.SDK_INT >= 21) {
            i2 += AndroidUtilities.statusBarHeight;
        }
        if (i == 1) {
            i = AndroidUtilities.dp(144.0f);
        } else if (i == 2) {
            i = AndroidUtilities.dp(214.0f);
        } else if (i != 3) {
            return i2;
        } else {
            i = AndroidUtilities.dp(48.0f) + ActionBar.getCurrentActionBarHeight();
        }
        return i2 - i;
    }

    private boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.animationInProgress != 0 || this.animationStartTime != 0) {
            return false;
        }
        int i = this.currentEditMode;
        if (i == 2) {
            this.photoFilterView.onTouch(motionEvent);
            return true;
        }
        if (!(i == 1 || this.sendPhotoType == 1)) {
            if (this.captionEditText.isPopupShowing() || this.captionEditText.isKeyboardVisible()) {
                if (motionEvent.getAction() == 1) {
                    closeCaptionEnter(true);
                }
            } else if (this.currentEditMode == 0 && this.sendPhotoType != 1 && motionEvent.getPointerCount() == 1 && this.gestureDetector.onTouchEvent(motionEvent) && this.doubleTap) {
                this.doubleTap = false;
                this.moving = false;
                this.zooming = false;
                checkMinMax(false);
                return true;
            } else {
                float y;
                if (motionEvent.getActionMasked() == 0 || motionEvent.getActionMasked() == 5) {
                    this.discardTap = false;
                    if (!this.scroller.isFinished()) {
                        this.scroller.abortAnimation();
                    }
                    if (!(this.draggingDown || this.changingPage)) {
                        VelocityTracker velocityTracker;
                        if (this.canZoom && motionEvent.getPointerCount() == 2) {
                            this.pinchStartDistance = (float) Math.hypot((double) (motionEvent.getX(1) - motionEvent.getX(0)), (double) (motionEvent.getY(1) - motionEvent.getY(0)));
                            this.pinchStartScale = this.scale;
                            this.pinchCenterX = (motionEvent.getX(0) + motionEvent.getX(1)) / 2.0f;
                            this.pinchCenterY = (motionEvent.getY(0) + motionEvent.getY(1)) / 2.0f;
                            this.pinchStartX = this.translationX;
                            this.pinchStartY = this.translationY;
                            this.zooming = true;
                            this.moving = false;
                            velocityTracker = this.velocityTracker;
                            if (velocityTracker != null) {
                                velocityTracker.clear();
                            }
                        } else if (motionEvent.getPointerCount() == 1) {
                            this.moveStartX = motionEvent.getX();
                            y = motionEvent.getY();
                            this.moveStartY = y;
                            this.dragY = y;
                            this.draggingDown = false;
                            this.canDragDown = true;
                            velocityTracker = this.velocityTracker;
                            if (velocityTracker != null) {
                                velocityTracker.clear();
                            }
                        }
                    }
                } else {
                    float f = 0.0f;
                    float containerViewHeight;
                    float f2;
                    float f3;
                    if (motionEvent.getActionMasked() == 2) {
                        if (this.canZoom && motionEvent.getPointerCount() == 2 && !this.draggingDown && this.zooming && !this.changingPage) {
                            this.discardTap = true;
                            this.scale = (((float) Math.hypot((double) (motionEvent.getX(1) - motionEvent.getX(0)), (double) (motionEvent.getY(1) - motionEvent.getY(0)))) / this.pinchStartDistance) * this.pinchStartScale;
                            this.translationX = (this.pinchCenterX - ((float) (getContainerViewWidth() / 2))) - (((this.pinchCenterX - ((float) (getContainerViewWidth() / 2))) - this.pinchStartX) * (this.scale / this.pinchStartScale));
                            y = this.pinchCenterY - ((float) (getContainerViewHeight() / 2));
                            containerViewHeight = (this.pinchCenterY - ((float) (getContainerViewHeight() / 2))) - this.pinchStartY;
                            f2 = this.scale;
                            this.translationY = y - (containerViewHeight * (f2 / this.pinchStartScale));
                            updateMinMax(f2);
                            this.containerView.invalidate();
                        } else if (motionEvent.getPointerCount() == 1) {
                            VelocityTracker velocityTracker2 = this.velocityTracker;
                            if (velocityTracker2 != null) {
                                velocityTracker2.addMovement(motionEvent);
                            }
                            containerViewHeight = Math.abs(motionEvent.getX() - this.moveStartX);
                            f2 = Math.abs(motionEvent.getY() - this.dragY);
                            if (containerViewHeight > ((float) AndroidUtilities.dp(3.0f)) || f2 > ((float) AndroidUtilities.dp(3.0f))) {
                                this.discardTap = true;
                                QualityChooseView qualityChooseView = this.qualityChooseView;
                                if (qualityChooseView != null && qualityChooseView.getVisibility() == 0) {
                                    return true;
                                }
                            }
                            if (this.placeProvider.canScrollAway() && this.currentEditMode == 0 && this.sendPhotoType != 1 && this.canDragDown && !this.draggingDown && this.scale == 1.0f && f2 >= ((float) AndroidUtilities.dp(30.0f)) && f2 / 2.0f > containerViewHeight) {
                                this.draggingDown = true;
                                this.moving = false;
                                this.dragY = motionEvent.getY();
                                if (this.isActionBarVisible && this.containerView.getTag() != null) {
                                    toggleActionBar(false, true);
                                } else if (this.pickerView.getVisibility() == 0) {
                                    toggleActionBar(false, true);
                                    togglePhotosListView(false, true);
                                    toggleCheckImageView(false);
                                }
                                return true;
                            } else if (this.draggingDown) {
                                this.translationY = motionEvent.getY() - this.dragY;
                                this.containerView.invalidate();
                            } else if (this.invalidCoords || this.animationStartTime != 0) {
                                this.invalidCoords = false;
                                this.moveStartX = motionEvent.getX();
                                this.moveStartY = motionEvent.getY();
                            } else {
                                containerViewHeight = this.moveStartX - motionEvent.getX();
                                f2 = this.moveStartY - motionEvent.getY();
                                if (this.moving || this.currentEditMode != 0 || ((this.scale == 1.0f && Math.abs(f2) + ((float) AndroidUtilities.dp(12.0f)) < Math.abs(containerViewHeight)) || this.scale != 1.0f)) {
                                    if (!this.moving) {
                                        this.moving = true;
                                        this.canDragDown = false;
                                        containerViewHeight = 0.0f;
                                        f2 = 0.0f;
                                    }
                                    this.moveStartX = motionEvent.getX();
                                    this.moveStartY = motionEvent.getY();
                                    updateMinMax(this.scale);
                                    if ((this.translationX < this.minX && !(this.currentEditMode == 0 && this.rightImage.hasImageSet())) || (this.translationX > this.maxX && !(this.currentEditMode == 0 && this.leftImage.hasImageSet()))) {
                                        containerViewHeight /= 3.0f;
                                    }
                                    y = this.maxY;
                                    if (y == 0.0f) {
                                        float f4 = this.minY;
                                        if (f4 == 0.0f && this.currentEditMode == 0 && this.sendPhotoType != 1) {
                                            f3 = this.translationY;
                                            if (f3 - f2 < f4) {
                                                this.translationY = f4;
                                            } else {
                                                if (f3 - f2 > y) {
                                                    this.translationY = y;
                                                }
                                                f = f2;
                                            }
                                            this.translationX -= containerViewHeight;
                                            if (!(this.scale == 1.0f && this.currentEditMode == 0)) {
                                                this.translationY -= f;
                                            }
                                            this.containerView.invalidate();
                                        }
                                    }
                                    y = this.translationY;
                                    if (y < this.minY || y > this.maxY) {
                                        f = f2 / 3.0f;
                                        this.translationX -= containerViewHeight;
                                        this.translationY -= f;
                                        this.containerView.invalidate();
                                    }
                                    f = f2;
                                    this.translationX -= containerViewHeight;
                                    this.translationY -= f;
                                    this.containerView.invalidate();
                                }
                            }
                        }
                    } else if (motionEvent.getActionMasked() == 3 || motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 6) {
                        if (this.zooming) {
                            this.invalidCoords = true;
                            y = this.scale;
                            if (y < 1.0f) {
                                updateMinMax(1.0f);
                                animateTo(1.0f, 0.0f, 0.0f, true);
                            } else if (y > 3.0f) {
                                y = (this.pinchCenterX - ((float) (getContainerViewWidth() / 2))) - (((this.pinchCenterX - ((float) (getContainerViewWidth() / 2))) - this.pinchStartX) * (3.0f / this.pinchStartScale));
                                containerViewHeight = (this.pinchCenterY - ((float) (getContainerViewHeight() / 2))) - (((this.pinchCenterY - ((float) (getContainerViewHeight() / 2))) - this.pinchStartY) * (3.0f / this.pinchStartScale));
                                updateMinMax(3.0f);
                                f2 = this.minX;
                                if (y >= f2) {
                                    f2 = this.maxX;
                                    if (y <= f2) {
                                        f2 = y;
                                    }
                                }
                                y = this.minY;
                                if (containerViewHeight >= y) {
                                    y = this.maxY;
                                    if (containerViewHeight <= y) {
                                        y = containerViewHeight;
                                    }
                                }
                                animateTo(3.0f, f2, y, true);
                            } else {
                                checkMinMax(true);
                            }
                            this.zooming = false;
                        } else if (this.draggingDown) {
                            if (Math.abs(this.dragY - motionEvent.getY()) > ((float) getContainerViewHeight()) / 6.0f) {
                                closePhoto(true, false);
                            } else {
                                if (this.pickerView.getVisibility() == 0) {
                                    toggleActionBar(true, true);
                                    toggleCheckImageView(true);
                                }
                                animateTo(1.0f, 0.0f, 0.0f, false);
                            }
                            this.draggingDown = false;
                        } else if (this.moving) {
                            y = this.translationX;
                            containerViewHeight = this.translationY;
                            updateMinMax(this.scale);
                            this.moving = false;
                            this.canDragDown = true;
                            VelocityTracker velocityTracker3 = this.velocityTracker;
                            if (velocityTracker3 != null && this.scale == 1.0f) {
                                velocityTracker3.computeCurrentVelocity(1000);
                                f = this.velocityTracker.getXVelocity();
                            }
                            if (this.currentEditMode == 0 && this.sendPhotoType != 1) {
                                if ((this.translationX < this.minX - ((float) (getContainerViewWidth() / 3)) || f < ((float) (-AndroidUtilities.dp(650.0f)))) && this.rightImage.hasImageSet()) {
                                    goToNext();
                                    return true;
                                } else if ((this.translationX > this.maxX + ((float) (getContainerViewWidth() / 3)) || f > ((float) AndroidUtilities.dp(650.0f))) && this.leftImage.hasImageSet()) {
                                    goToPrev();
                                    return true;
                                }
                            }
                            f2 = this.translationX;
                            f3 = this.minX;
                            if (f2 >= f3) {
                                f3 = this.maxX;
                                if (f2 <= f3) {
                                    f3 = y;
                                }
                            }
                            y = this.translationY;
                            f2 = this.minY;
                            if (y >= f2) {
                                f2 = this.maxY;
                                if (y <= f2) {
                                    f2 = containerViewHeight;
                                }
                            }
                            animateTo(this.scale, f3, f2, false);
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }

    private void checkMinMax(boolean z) {
        float f = this.translationX;
        float f2 = this.translationY;
        updateMinMax(this.scale);
        float f3 = this.translationX;
        float f4 = this.minX;
        if (f3 >= f4) {
            f4 = this.maxX;
            if (f3 <= f4) {
                f4 = f;
            }
        }
        f = this.translationY;
        f3 = this.minY;
        if (f >= f3) {
            f3 = this.maxY;
            if (f <= f3) {
                f3 = f2;
            }
        }
        animateTo(this.scale, f4, f3, z);
    }

    private void goToNext() {
        float containerViewWidth = this.scale != 1.0f ? ((float) ((getContainerViewWidth() - this.centerImage.getImageWidth()) / 2)) * this.scale : 0.0f;
        this.switchImageAfterAnimation = 1;
        animateTo(this.scale, ((this.minX - ((float) getContainerViewWidth())) - containerViewWidth) - ((float) (AndroidUtilities.dp(30.0f) / 2)), this.translationY, false);
    }

    private void goToPrev() {
        float containerViewWidth = this.scale != 1.0f ? ((float) ((getContainerViewWidth() - this.centerImage.getImageWidth()) / 2)) * this.scale : 0.0f;
        this.switchImageAfterAnimation = 2;
        animateTo(this.scale, ((this.maxX + ((float) getContainerViewWidth())) + containerViewWidth) + ((float) (AndroidUtilities.dp(30.0f) / 2)), this.translationY, false);
    }

    private void animateTo(float f, float f2, float f3, boolean z) {
        animateTo(f, f2, f3, z, 250);
    }

    private void animateTo(float f, float f2, float f3, boolean z, int i) {
        if (this.scale != f || this.translationX != f2 || this.translationY != f3) {
            this.zoomAnimation = z;
            this.animateToScale = f;
            this.animateToX = f2;
            this.animateToY = f3;
            this.animationStartTime = System.currentTimeMillis();
            this.imageMoveAnimation = new AnimatorSet();
            this.imageMoveAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(this, AnimationProperties.PHOTO_VIEWER_ANIMATION_VALUE, new float[]{0.0f, 1.0f})});
            this.imageMoveAnimation.setInterpolator(this.interpolator);
            this.imageMoveAnimation.setDuration((long) i);
            this.imageMoveAnimation.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    PhotoViewer.this.imageMoveAnimation = null;
                    PhotoViewer.this.containerView.invalidate();
                }
            });
            this.imageMoveAnimation.start();
        }
    }

    @Keep
    public void setAnimationValue(float f) {
        this.animationValue = f;
        this.containerView.invalidate();
    }

    @Keep
    public float getAnimationValue() {
        return this.animationValue;
    }

    /* JADX WARNING: Removed duplicated region for block: B:100:0x01d0  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x01ce  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0210  */
    /* JADX WARNING: Removed duplicated region for block: B:201:0x042f  */
    /* JADX WARNING: Removed duplicated region for block: B:211:0x0488  */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x0541  */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x05a2  */
    /* JADX WARNING: Removed duplicated region for block: B:201:0x042f  */
    /* JADX WARNING: Removed duplicated region for block: B:211:0x0488  */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x0541  */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x05a2  */
    /* JADX WARNING: Removed duplicated region for block: B:249:? A:{SYNTHETIC, RETURN, SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:244:0x05a9  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x034d  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0383  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0413  */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x03ff  */
    /* JADX WARNING: Removed duplicated region for block: B:201:0x042f  */
    /* JADX WARNING: Removed duplicated region for block: B:211:0x0488  */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x0541  */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x05a2  */
    /* JADX WARNING: Removed duplicated region for block: B:244:0x05a9  */
    /* JADX WARNING: Removed duplicated region for block: B:249:? A:{SYNTHETIC, RETURN, SKIP} */
    /* JADX WARNING: Missing block: B:198:0x042a, code skipped:
            if (r5.isLoadingStream() == false) goto L_0x042c;
     */
    @android.annotation.SuppressLint({"NewApi", "DrawAllocation"})
    private void onDraw(android.graphics.Canvas r21) {
        /*
        r20 = this;
        r1 = r20;
        r2 = r21;
        r0 = r1.animationInProgress;
        r3 = 1;
        if (r0 == r3) goto L_0x05ea;
    L_0x0009:
        r4 = r1.isVisible;
        r5 = 2;
        if (r4 != 0) goto L_0x0016;
    L_0x000e:
        if (r0 == r5) goto L_0x0016;
    L_0x0010:
        r0 = r1.pipAnimationInProgress;
        if (r0 != 0) goto L_0x0016;
    L_0x0014:
        goto L_0x05ea;
    L_0x0016:
        r0 = r1.padImageForHorizontalInsets;
        r4 = 0;
        if (r0 == 0) goto L_0x002d;
    L_0x001b:
        r21.save();
        r0 = r20.getLeftInset();
        r0 = r0 / r5;
        r6 = r20.getRightInset();
        r6 = r6 / r5;
        r0 = r0 - r6;
        r0 = (float) r0;
        r2.translate(r0, r4);
    L_0x002d:
        r0 = r1.imageMoveAnimation;
        r6 = -NUM; // 0xffffffffbvar_ float:-1.0 double:NaN;
        r7 = 0;
        r8 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        if (r0 == 0) goto L_0x0076;
    L_0x0036:
        r0 = r1.scroller;
        r0 = r0.isFinished();
        if (r0 != 0) goto L_0x0043;
    L_0x003e:
        r0 = r1.scroller;
        r0.abortAnimation();
    L_0x0043:
        r0 = r1.scale;
        r9 = r1.animateToScale;
        r10 = r9 - r0;
        r11 = r1.animationValue;
        r10 = r10 * r11;
        r10 = r10 + r0;
        r12 = r1.translationX;
        r13 = r1.animateToX;
        r13 = r13 - r12;
        r13 = r13 * r11;
        r13 = r13 + r12;
        r14 = r1.translationY;
        r15 = r1.animateToY;
        r15 = r15 - r14;
        r15 = r15 * r11;
        r11 = r14 + r15;
        r9 = (r9 > r8 ? 1 : (r9 == r8 ? 0 : -1));
        if (r9 != 0) goto L_0x006d;
    L_0x0063:
        r0 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
        if (r0 != 0) goto L_0x006d;
    L_0x0067:
        r0 = (r12 > r4 ? 1 : (r12 == r4 ? 0 : -1));
        if (r0 != 0) goto L_0x006d;
    L_0x006b:
        r0 = r11;
        goto L_0x006f;
    L_0x006d:
        r0 = -NUM; // 0xffffffffbvar_ float:-1.0 double:NaN;
    L_0x006f:
        r9 = r1.containerView;
        r9.invalidate();
        goto L_0x0143;
    L_0x0076:
        r9 = r1.animationStartTime;
        r11 = 0;
        r0 = (r9 > r11 ? 1 : (r9 == r11 ? 0 : -1));
        if (r0 == 0) goto L_0x0093;
    L_0x007e:
        r0 = r1.animateToX;
        r1.translationX = r0;
        r0 = r1.animateToY;
        r1.translationY = r0;
        r0 = r1.animateToScale;
        r1.scale = r0;
        r1.animationStartTime = r11;
        r0 = r1.scale;
        r1.updateMinMax(r0);
        r1.zoomAnimation = r7;
    L_0x0093:
        r0 = r1.scroller;
        r0 = r0.isFinished();
        if (r0 != 0) goto L_0x00ee;
    L_0x009b:
        r0 = r1.scroller;
        r0 = r0.computeScrollOffset();
        if (r0 == 0) goto L_0x00ee;
    L_0x00a3:
        r0 = r1.scroller;
        r0 = r0.getStartX();
        r0 = (float) r0;
        r9 = r1.maxX;
        r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1));
        if (r0 >= 0) goto L_0x00c6;
    L_0x00b0:
        r0 = r1.scroller;
        r0 = r0.getStartX();
        r0 = (float) r0;
        r9 = r1.minX;
        r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1));
        if (r0 <= 0) goto L_0x00c6;
    L_0x00bd:
        r0 = r1.scroller;
        r0 = r0.getCurrX();
        r0 = (float) r0;
        r1.translationX = r0;
    L_0x00c6:
        r0 = r1.scroller;
        r0 = r0.getStartY();
        r0 = (float) r0;
        r9 = r1.maxY;
        r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1));
        if (r0 >= 0) goto L_0x00e9;
    L_0x00d3:
        r0 = r1.scroller;
        r0 = r0.getStartY();
        r0 = (float) r0;
        r9 = r1.minY;
        r0 = (r0 > r9 ? 1 : (r0 == r9 ? 0 : -1));
        if (r0 <= 0) goto L_0x00e9;
    L_0x00e0:
        r0 = r1.scroller;
        r0 = r0.getCurrY();
        r0 = (float) r0;
        r1.translationY = r0;
    L_0x00e9:
        r0 = r1.containerView;
        r0.invalidate();
    L_0x00ee:
        r0 = r1.switchImageAfterAnimation;
        if (r0 == 0) goto L_0x0135;
    L_0x00f2:
        r1.openedFullScreenVideo = r7;
        r0 = r1.imagesArrLocals;
        r0 = r0.isEmpty();
        if (r0 != 0) goto L_0x011c;
    L_0x00fc:
        r0 = r1.currentIndex;
        if (r0 < 0) goto L_0x011c;
    L_0x0100:
        r9 = r1.imagesArrLocals;
        r9 = r9.size();
        if (r0 >= r9) goto L_0x011c;
    L_0x0108:
        r0 = r1.imagesArrLocals;
        r9 = r1.currentIndex;
        r0 = r0.get(r9);
        r9 = r0 instanceof org.telegram.messenger.MediaController.PhotoEntry;
        if (r9 == 0) goto L_0x011c;
    L_0x0114:
        r0 = (org.telegram.messenger.MediaController.PhotoEntry) r0;
        r9 = r20.getCurrentVideoEditedInfo();
        r0.editedInfo = r9;
    L_0x011c:
        r0 = r1.switchImageAfterAnimation;
        if (r0 != r3) goto L_0x0129;
    L_0x0120:
        r0 = new org.telegram.ui.-$$Lambda$PhotoViewer$PgSXr-DoWLQ-H0Db5AsSFGN7Cu8;
        r0.<init>(r1);
        org.telegram.messenger.AndroidUtilities.runOnUIThread(r0);
        goto L_0x0133;
    L_0x0129:
        if (r0 != r5) goto L_0x0133;
    L_0x012b:
        r0 = new org.telegram.ui.-$$Lambda$PhotoViewer$JNDcI2bubuBaN5sKdJr5MiZBjBU;
        r0.<init>(r1);
        org.telegram.messenger.AndroidUtilities.runOnUIThread(r0);
    L_0x0133:
        r1.switchImageAfterAnimation = r7;
    L_0x0135:
        r10 = r1.scale;
        r0 = r1.translationY;
        r13 = r1.translationX;
        r9 = r1.moving;
        r11 = r0;
        if (r9 != 0) goto L_0x0141;
    L_0x0140:
        goto L_0x0143;
    L_0x0141:
        r0 = -NUM; // 0xffffffffbvar_ float:-1.0 double:NaN;
    L_0x0143:
        r9 = r1.animationInProgress;
        if (r9 == r5) goto L_0x0190;
    L_0x0147:
        r9 = r1.pipAnimationInProgress;
        if (r9 != 0) goto L_0x0190;
    L_0x014b:
        r9 = r1.isInline;
        if (r9 != 0) goto L_0x0190;
    L_0x014f:
        r9 = r1.currentEditMode;
        if (r9 != 0) goto L_0x0189;
    L_0x0153:
        r9 = r1.sendPhotoType;
        if (r9 == r3) goto L_0x0189;
    L_0x0157:
        r9 = r1.scale;
        r9 = (r9 > r8 ? 1 : (r9 == r8 ? 0 : -1));
        if (r9 != 0) goto L_0x0189;
    L_0x015d:
        r6 = (r0 > r6 ? 1 : (r0 == r6 ? 0 : -1));
        if (r6 == 0) goto L_0x0189;
    L_0x0161:
        r6 = r1.zoomAnimation;
        if (r6 != 0) goto L_0x0189;
    L_0x0165:
        r6 = r20.getContainerViewHeight();
        r6 = (float) r6;
        r9 = NUM; // 0x40800000 float:4.0 double:5.34643471E-315;
        r6 = r6 / r9;
        r9 = r1.backgroundDrawable;
        r12 = NUM; // 0x42fe0000 float:127.0 double:5.553013277E-315;
        r14 = NUM; // 0x437var_ float:255.0 double:5.5947823E-315;
        r0 = java.lang.Math.abs(r0);
        r0 = java.lang.Math.min(r0, r6);
        r0 = r0 / r6;
        r0 = r8 - r0;
        r0 = r0 * r14;
        r0 = java.lang.Math.max(r12, r0);
        r0 = (int) r0;
        r9.setAlpha(r0);
        goto L_0x0190;
    L_0x0189:
        r0 = r1.backgroundDrawable;
        r6 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r0.setAlpha(r6);
    L_0x0190:
        r0 = r1.currentEditMode;
        if (r0 != 0) goto L_0x01d4;
    L_0x0194:
        r0 = r1.sendPhotoType;
        if (r0 == r3) goto L_0x01d4;
    L_0x0198:
        r0 = r1.scale;
        r0 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
        if (r0 < 0) goto L_0x01cb;
    L_0x019e:
        r0 = r1.zoomAnimation;
        if (r0 != 0) goto L_0x01cb;
    L_0x01a2:
        r0 = r1.zooming;
        if (r0 != 0) goto L_0x01cb;
    L_0x01a6:
        r0 = r1.maxX;
        r9 = NUM; // 0x40a00000 float:5.0 double:5.356796015E-315;
        r12 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r12 = (float) r12;
        r0 = r0 + r12;
        r0 = (r13 > r0 ? 1 : (r13 == r0 ? 0 : -1));
        if (r0 <= 0) goto L_0x01b7;
    L_0x01b4:
        r0 = r1.leftImage;
        goto L_0x01cc;
    L_0x01b7:
        r0 = r1.minX;
        r9 = org.telegram.messenger.AndroidUtilities.dp(r9);
        r9 = (float) r9;
        r0 = r0 - r9;
        r0 = (r13 > r0 ? 1 : (r13 == r0 ? 0 : -1));
        if (r0 >= 0) goto L_0x01c6;
    L_0x01c3:
        r0 = r1.rightImage;
        goto L_0x01cc;
    L_0x01c6:
        r0 = r1.groupedPhotosListView;
        r0.setMoveProgress(r4);
    L_0x01cb:
        r0 = 0;
    L_0x01cc:
        if (r0 == 0) goto L_0x01d0;
    L_0x01ce:
        r9 = 1;
        goto L_0x01d1;
    L_0x01d0:
        r9 = 0;
    L_0x01d1:
        r1.changingPage = r9;
        goto L_0x01d5;
    L_0x01d4:
        r0 = 0;
    L_0x01d5:
        r9 = r1.rightImage;
        r12 = NUM; // 0x3e99999a float:0.3 double:5.188942835E-315;
        r15 = NUM; // 0x41var_ float:30.0 double:5.465589745E-315;
        if (r0 != r9) goto L_0x02ab;
    L_0x01de:
        r9 = r1.zoomAnimation;
        if (r9 != 0) goto L_0x0206;
    L_0x01e2:
        r9 = r1.minX;
        r16 = (r13 > r9 ? 1 : (r13 == r9 ? 0 : -1));
        if (r16 >= 0) goto L_0x0206;
    L_0x01e8:
        r9 = r9 - r13;
        r6 = r20.getContainerViewWidth();
        r6 = (float) r6;
        r9 = r9 / r6;
        r6 = java.lang.Math.min(r8, r9);
        r9 = r8 - r6;
        r9 = r9 * r12;
        r7 = r20.getContainerViewWidth();
        r7 = -r7;
        r17 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r17 = r17 / 2;
        r7 = r7 - r17;
        r7 = (float) r7;
        goto L_0x020a;
    L_0x0206:
        r7 = r13;
        r6 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r9 = 0;
    L_0x020a:
        r17 = r0.hasBitmapImage();
        if (r17 == 0) goto L_0x026a;
    L_0x0210:
        r21.save();
        r17 = r20.getContainerViewWidth();
        r12 = r17 / 2;
        r12 = (float) r12;
        r17 = r20.getContainerViewHeight();
        r3 = r17 / 2;
        r3 = (float) r3;
        r2.translate(r12, r3);
        r3 = r20.getContainerViewWidth();
        r12 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r12 = r12 / r5;
        r3 = r3 + r12;
        r3 = (float) r3;
        r3 = r3 + r7;
        r2.translate(r3, r4);
        r3 = r8 - r9;
        r2.scale(r3, r3);
        r3 = r0.getBitmapWidth();
        r12 = r0.getBitmapHeight();
        r4 = r20.getContainerViewWidth();
        r4 = (float) r4;
        r3 = (float) r3;
        r4 = r4 / r3;
        r14 = r20.getContainerViewHeight();
        r14 = (float) r14;
        r12 = (float) r12;
        r14 = r14 / r12;
        r18 = (r4 > r14 ? 1 : (r4 == r14 ? 0 : -1));
        if (r18 <= 0) goto L_0x0253;
    L_0x0252:
        goto L_0x0254;
    L_0x0253:
        r14 = r4;
    L_0x0254:
        r3 = r3 * r14;
        r3 = (int) r3;
        r12 = r12 * r14;
        r4 = (int) r12;
        r0.setAlpha(r6);
        r12 = -r3;
        r12 = r12 / r5;
        r14 = -r4;
        r14 = r14 / r5;
        r0.setImageCoords(r12, r14, r3, r4);
        r0.draw(r2);
        r21.restore();
    L_0x026a:
        r3 = r1.groupedPhotosListView;
        r4 = -r6;
        r3.setMoveProgress(r4);
        r21.save();
        r3 = r11 / r10;
        r2.translate(r7, r3);
        r3 = r20.getContainerViewWidth();
        r3 = (float) r3;
        r4 = r1.scale;
        r4 = r4 + r8;
        r3 = r3 * r4;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r15);
        r4 = (float) r4;
        r3 = r3 + r4;
        r4 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r3 = r3 / r4;
        r4 = -r11;
        r4 = r4 / r10;
        r2.translate(r3, r4);
        r3 = r1.photoProgressViews;
        r4 = 1;
        r3 = r3[r4];
        r7 = r8 - r9;
        r3.setScale(r7);
        r3 = r1.photoProgressViews;
        r3 = r3[r4];
        r3.setAlpha(r6);
        r3 = r1.photoProgressViews;
        r3 = r3[r4];
        r3.onDraw(r2);
        r21.restore();
    L_0x02ab:
        r3 = r1.zoomAnimation;
        if (r3 != 0) goto L_0x02d4;
    L_0x02af:
        r3 = r1.maxX;
        r4 = (r13 > r3 ? 1 : (r13 == r3 ? 0 : -1));
        if (r4 <= 0) goto L_0x02d4;
    L_0x02b5:
        r4 = r1.currentEditMode;
        if (r4 != 0) goto L_0x02d4;
    L_0x02b9:
        r4 = r1.sendPhotoType;
        r6 = 1;
        if (r4 == r6) goto L_0x02d4;
    L_0x02be:
        r3 = r13 - r3;
        r4 = r20.getContainerViewWidth();
        r4 = (float) r4;
        r3 = r3 / r4;
        r3 = java.lang.Math.min(r8, r3);
        r4 = NUM; // 0x3e99999a float:0.3 double:5.188942835E-315;
        r4 = r4 * r3;
        r3 = r8 - r3;
        r6 = r1.maxX;
        goto L_0x02d8;
    L_0x02d4:
        r6 = r13;
        r3 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r4 = 0;
    L_0x02d8:
        r7 = r1.aspectRatioFrameLayout;
        if (r7 == 0) goto L_0x02e4;
    L_0x02dc:
        r7 = r7.getVisibility();
        if (r7 != 0) goto L_0x02e4;
    L_0x02e2:
        r7 = 1;
        goto L_0x02e5;
    L_0x02e4:
        r7 = 0;
    L_0x02e5:
        r9 = r1.centerImage;
        r9 = r9.hasBitmapImage();
        if (r9 != 0) goto L_0x02f8;
    L_0x02ed:
        if (r7 == 0) goto L_0x02f4;
    L_0x02ef:
        r9 = r1.textureUploaded;
        if (r9 == 0) goto L_0x02f4;
    L_0x02f3:
        goto L_0x02f8;
    L_0x02f4:
        r19 = r13;
        goto L_0x03fb;
    L_0x02f8:
        r21.save();
        r9 = r20.getContainerViewWidth();
        r9 = r9 / r5;
        r12 = r20.getAdditionX();
        r9 = r9 + r12;
        r9 = (float) r9;
        r12 = r20.getContainerViewHeight();
        r12 = r12 / r5;
        r14 = r20.getAdditionY();
        r12 = r12 + r14;
        r12 = (float) r12;
        r2.translate(r9, r12);
        r2.translate(r6, r11);
        r9 = r10 - r4;
        r2.scale(r9, r9);
        if (r7 == 0) goto L_0x032f;
    L_0x031e:
        r9 = r1.textureUploaded;
        if (r9 == 0) goto L_0x032f;
    L_0x0322:
        r9 = r1.videoTextureView;
        r9 = r9.getMeasuredWidth();
        r12 = r1.videoTextureView;
        r12 = r12.getMeasuredHeight();
        goto L_0x033b;
    L_0x032f:
        r9 = r1.centerImage;
        r9 = r9.getBitmapWidth();
        r12 = r1.centerImage;
        r12 = r12.getBitmapHeight();
    L_0x033b:
        r14 = r20.getContainerViewWidth();
        r14 = (float) r14;
        r9 = (float) r9;
        r14 = r14 / r9;
        r15 = r20.getContainerViewHeight();
        r15 = (float) r15;
        r12 = (float) r12;
        r15 = r15 / r12;
        r18 = (r14 > r15 ? 1 : (r14 == r15 ? 0 : -1));
        if (r18 <= 0) goto L_0x034e;
    L_0x034d:
        r14 = r15;
    L_0x034e:
        r15 = r9 * r14;
        r15 = (int) r15;
        r14 = r14 * r12;
        r14 = (int) r14;
        if (r7 == 0) goto L_0x0368;
    L_0x0356:
        r5 = r1.textureUploaded;
        if (r5 == 0) goto L_0x0368;
    L_0x035a:
        r5 = r1.videoCrossfadeStarted;
        if (r5 == 0) goto L_0x0368;
    L_0x035e:
        r5 = r1.videoCrossfadeAlpha;
        r5 = (r5 > r8 ? 1 : (r5 == r8 ? 0 : -1));
        if (r5 == 0) goto L_0x0365;
    L_0x0364:
        goto L_0x0368;
    L_0x0365:
        r19 = r13;
        goto L_0x0381;
    L_0x0368:
        r5 = r1.centerImage;
        r5.setAlpha(r3);
        r5 = r1.centerImage;
        r8 = -r15;
        r18 = 2;
        r8 = r8 / 2;
        r19 = r13;
        r13 = -r14;
        r13 = r13 / 2;
        r5.setImageCoords(r8, r13, r15, r14);
        r5 = r1.centerImage;
        r5.draw(r2);
    L_0x0381:
        if (r7 == 0) goto L_0x03f8;
    L_0x0383:
        r5 = r21.getWidth();
        r5 = (float) r5;
        r5 = r5 / r9;
        r8 = r21.getHeight();
        r8 = (float) r8;
        r8 = r8 / r12;
        r9 = (r5 > r8 ? 1 : (r5 == r8 ? 0 : -1));
        if (r9 <= 0) goto L_0x0394;
    L_0x0393:
        r5 = r8;
    L_0x0394:
        r12 = r12 * r5;
        r5 = (int) r12;
        r8 = r1.videoCrossfadeStarted;
        if (r8 != 0) goto L_0x03ab;
    L_0x039b:
        r8 = r1.textureUploaded;
        if (r8 == 0) goto L_0x03ab;
    L_0x039f:
        r8 = 1;
        r1.videoCrossfadeStarted = r8;
        r8 = 0;
        r1.videoCrossfadeAlpha = r8;
        r8 = java.lang.System.currentTimeMillis();
        r1.videoCrossfadeAlphaLastTime = r8;
    L_0x03ab:
        r8 = -r15;
        r9 = 2;
        r8 = r8 / r9;
        r8 = (float) r8;
        r5 = -r5;
        r5 = r5 / r9;
        r5 = (float) r5;
        r2.translate(r8, r5);
        r5 = r1.videoTextureView;
        r8 = r1.videoCrossfadeAlpha;
        r8 = r8 * r3;
        r5.setAlpha(r8);
        r5 = r1.aspectRatioFrameLayout;
        r5.draw(r2);
        r5 = r1.videoCrossfadeStarted;
        if (r5 == 0) goto L_0x03f8;
    L_0x03c7:
        r5 = r1.videoCrossfadeAlpha;
        r8 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r5 = (r5 > r8 ? 1 : (r5 == r8 ? 0 : -1));
        if (r5 >= 0) goto L_0x03f8;
    L_0x03cf:
        r8 = java.lang.System.currentTimeMillis();
        r12 = r1.videoCrossfadeAlphaLastTime;
        r12 = r8 - r12;
        r1.videoCrossfadeAlphaLastTime = r8;
        r5 = r1.videoCrossfadeAlpha;
        r8 = (float) r12;
        r9 = r1.playerInjected;
        if (r9 == 0) goto L_0x03e3;
    L_0x03e0:
        r9 = NUM; // 0x42CLASSNAME float:100.0 double:5.53552857E-315;
        goto L_0x03e5;
    L_0x03e3:
        r9 = NUM; // 0x43480000 float:200.0 double:5.5769738E-315;
    L_0x03e5:
        r8 = r8 / r9;
        r5 = r5 + r8;
        r1.videoCrossfadeAlpha = r5;
        r5 = r1.containerView;
        r5.invalidate();
        r5 = r1.videoCrossfadeAlpha;
        r8 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r5 = (r5 > r8 ? 1 : (r5 == r8 ? 0 : -1));
        if (r5 <= 0) goto L_0x03f8;
    L_0x03f6:
        r1.videoCrossfadeAlpha = r8;
    L_0x03f8:
        r21.restore();
    L_0x03fb:
        r5 = r1.isCurrentVideo;
        if (r5 == 0) goto L_0x0413;
    L_0x03ff:
        r5 = r1.progressView;
        r5 = r5.getVisibility();
        if (r5 == 0) goto L_0x042c;
    L_0x0407:
        r5 = r1.videoPlayer;
        if (r5 == 0) goto L_0x0411;
    L_0x040b:
        r5 = r5.isPlaying();
        if (r5 != 0) goto L_0x042c;
    L_0x0411:
        r7 = 1;
        goto L_0x042d;
    L_0x0413:
        if (r7 != 0) goto L_0x041f;
    L_0x0415:
        r5 = r1.videoPlayerControlFrameLayout;
        r5 = r5.getVisibility();
        if (r5 == 0) goto L_0x041f;
    L_0x041d:
        r7 = 1;
        goto L_0x0420;
    L_0x041f:
        r7 = 0;
    L_0x0420:
        if (r7 == 0) goto L_0x042d;
    L_0x0422:
        r5 = r1.currentAnimation;
        if (r5 == 0) goto L_0x042d;
    L_0x0426:
        r5 = r5.isLoadingStream();
        if (r5 != 0) goto L_0x042d;
    L_0x042c:
        r7 = 0;
    L_0x042d:
        if (r7 == 0) goto L_0x0454;
    L_0x042f:
        r21.save();
        r5 = r11 / r10;
        r2.translate(r6, r5);
        r5 = r1.photoProgressViews;
        r7 = 0;
        r5 = r5[r7];
        r8 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r4 = r8 - r4;
        r5.setScale(r4);
        r4 = r1.photoProgressViews;
        r4 = r4[r7];
        r4.setAlpha(r3);
        r4 = r1.photoProgressViews;
        r4 = r4[r7];
        r4.onDraw(r2);
        r21.restore();
    L_0x0454:
        r4 = r1.pipAnimationInProgress;
        if (r4 != 0) goto L_0x0484;
    L_0x0458:
        r4 = r1.miniProgressView;
        r4 = r4.getVisibility();
        if (r4 == 0) goto L_0x0464;
    L_0x0460:
        r4 = r1.miniProgressAnimator;
        if (r4 == 0) goto L_0x0484;
    L_0x0464:
        r21.save();
        r4 = r1.miniProgressView;
        r4 = r4.getLeft();
        r4 = (float) r4;
        r4 = r4 + r6;
        r5 = r1.miniProgressView;
        r5 = r5.getTop();
        r5 = (float) r5;
        r6 = r11 / r10;
        r5 = r5 + r6;
        r2.translate(r4, r5);
        r4 = r1.miniProgressView;
        r4.draw(r2);
        r21.restore();
    L_0x0484:
        r4 = r1.leftImage;
        if (r0 != r4) goto L_0x053d;
    L_0x0488:
        r4 = r0.hasBitmapImage();
        if (r4 == 0) goto L_0x04f4;
    L_0x048e:
        r21.save();
        r4 = r20.getContainerViewWidth();
        r5 = 2;
        r4 = r4 / r5;
        r4 = (float) r4;
        r6 = r20.getContainerViewHeight();
        r6 = r6 / r5;
        r5 = (float) r6;
        r2.translate(r4, r5);
        r4 = r20.getContainerViewWidth();
        r4 = (float) r4;
        r5 = r1.scale;
        r6 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r5 = r5 + r6;
        r4 = r4 * r5;
        r5 = NUM; // 0x41var_ float:30.0 double:5.465589745E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r5);
        r5 = (float) r6;
        r4 = r4 + r5;
        r4 = -r4;
        r5 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r4 = r4 / r5;
        r4 = r4 + r19;
        r5 = 0;
        r2.translate(r4, r5);
        r4 = r0.getBitmapWidth();
        r5 = r0.getBitmapHeight();
        r6 = r20.getContainerViewWidth();
        r6 = (float) r6;
        r4 = (float) r4;
        r6 = r6 / r4;
        r7 = r20.getContainerViewHeight();
        r7 = (float) r7;
        r5 = (float) r5;
        r7 = r7 / r5;
        r8 = (r6 > r7 ? 1 : (r6 == r7 ? 0 : -1));
        if (r8 <= 0) goto L_0x04da;
    L_0x04d9:
        r6 = r7;
    L_0x04da:
        r4 = r4 * r6;
        r4 = (int) r4;
        r5 = r5 * r6;
        r5 = (int) r5;
        r6 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r0.setAlpha(r6);
        r7 = -r4;
        r8 = 2;
        r7 = r7 / r8;
        r9 = -r5;
        r9 = r9 / r8;
        r0.setImageCoords(r7, r9, r4, r5);
        r0.draw(r2);
        r21.restore();
        goto L_0x04f6;
    L_0x04f4:
        r6 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
    L_0x04f6:
        r0 = r1.groupedPhotosListView;
        r8 = r6 - r3;
        r0.setMoveProgress(r8);
        r21.save();
        r0 = r11 / r10;
        r13 = r19;
        r2.translate(r13, r0);
        r0 = r20.getContainerViewWidth();
        r0 = (float) r0;
        r3 = r1.scale;
        r3 = r3 + r6;
        r0 = r0 * r3;
        r3 = NUM; // 0x41var_ float:30.0 double:5.465589745E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = (float) r3;
        r0 = r0 + r3;
        r0 = -r0;
        r3 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
        r0 = r0 / r3;
        r3 = -r11;
        r3 = r3 / r10;
        r2.translate(r0, r3);
        r0 = r1.photoProgressViews;
        r3 = 2;
        r0 = r0[r3];
        r4 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r0.setScale(r4);
        r0 = r1.photoProgressViews;
        r0 = r0[r3];
        r0.setAlpha(r4);
        r0 = r1.photoProgressViews;
        r0 = r0[r3];
        r0.onDraw(r2);
        r21.restore();
    L_0x053d:
        r0 = r1.waitingForDraw;
        if (r0 == 0) goto L_0x059e;
    L_0x0541:
        r3 = 1;
        r0 = r0 - r3;
        r1.waitingForDraw = r0;
        r0 = r1.waitingForDraw;
        if (r0 != 0) goto L_0x0599;
    L_0x0549:
        r0 = r1.textureImageView;
        if (r0 == 0) goto L_0x0590;
    L_0x054d:
        r0 = r1.videoTextureView;	 Catch:{ all -> 0x0569 }
        r0 = r0.getWidth();	 Catch:{ all -> 0x0569 }
        r3 = r1.videoTextureView;	 Catch:{ all -> 0x0569 }
        r3 = r3.getHeight();	 Catch:{ all -> 0x0569 }
        r4 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ all -> 0x0569 }
        r0 = org.telegram.messenger.Bitmaps.createBitmap(r0, r3, r4);	 Catch:{ all -> 0x0569 }
        r1.currentBitmap = r0;	 Catch:{ all -> 0x0569 }
        r0 = r1.changedTextureView;	 Catch:{ all -> 0x0569 }
        r3 = r1.currentBitmap;	 Catch:{ all -> 0x0569 }
        r0.getBitmap(r3);	 Catch:{ all -> 0x0569 }
        goto L_0x0577;
    L_0x0569:
        r0 = move-exception;
        r3 = r1.currentBitmap;
        if (r3 == 0) goto L_0x0574;
    L_0x056e:
        r3.recycle();
        r3 = 0;
        r1.currentBitmap = r3;
    L_0x0574:
        org.telegram.messenger.FileLog.e(r0);
    L_0x0577:
        r0 = r1.currentBitmap;
        if (r0 == 0) goto L_0x0589;
    L_0x057b:
        r0 = r1.textureImageView;
        r3 = 0;
        r0.setVisibility(r3);
        r0 = r1.textureImageView;
        r3 = r1.currentBitmap;
        r0.setImageBitmap(r3);
        goto L_0x0590;
    L_0x0589:
        r0 = r1.textureImageView;
        r3 = 0;
        r0.setImageDrawable(r3);
        goto L_0x0591;
    L_0x0590:
        r3 = 0;
    L_0x0591:
        r0 = r1.pipVideoView;
        r0.close();
        r1.pipVideoView = r3;
        goto L_0x059e;
    L_0x0599:
        r0 = r1.containerView;
        r0.invalidate();
    L_0x059e:
        r0 = r1.padImageForHorizontalInsets;
        if (r0 == 0) goto L_0x05a5;
    L_0x05a2:
        r21.restore();
    L_0x05a5:
        r0 = r1.aspectRatioFrameLayout;
        if (r0 == 0) goto L_0x05ea;
    L_0x05a9:
        r0 = r1.videoForwardDrawable;
        r0 = r0.isAnimating();
        if (r0 == 0) goto L_0x05ea;
    L_0x05b1:
        r0 = r1.aspectRatioFrameLayout;
        r0 = r0.getMeasuredHeight();
        r0 = (float) r0;
        r3 = r1.scale;
        r4 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r3 = r3 - r4;
        r0 = r0 * r3;
        r0 = (int) r0;
        r3 = 2;
        r0 = r0 / r3;
        r3 = r1.videoForwardDrawable;
        r4 = r1.aspectRatioFrameLayout;
        r4 = r4.getLeft();
        r5 = r1.aspectRatioFrameLayout;
        r5 = r5.getTop();
        r5 = r5 - r0;
        r11 = r11 / r10;
        r6 = (int) r11;
        r5 = r5 + r6;
        r7 = r1.aspectRatioFrameLayout;
        r7 = r7.getRight();
        r8 = r1.aspectRatioFrameLayout;
        r8 = r8.getBottom();
        r8 = r8 + r0;
        r8 = r8 + r6;
        r3.setBounds(r4, r5, r7, r8);
        r0 = r1.videoForwardDrawable;
        r0.draw(r2);
    L_0x05ea:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.onDraw(android.graphics.Canvas):void");
    }

    public /* synthetic */ void lambda$onDraw$49$PhotoViewer() {
        setImageIndex(this.currentIndex + 1, false);
    }

    public /* synthetic */ void lambda$onDraw$50$PhotoViewer() {
        setImageIndex(this.currentIndex - 1, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:80:0x023e  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x018c  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x018c  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x023e  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x023e  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x018c  */
    /* JADX WARNING: Missing block: B:14:0x0037, code skipped:
            if (r1.exists() == false) goto L_0x0039;
     */
    /* JADX WARNING: Missing block: B:46:0x013f, code skipped:
            if (r0.exists() == false) goto L_0x0180;
     */
    /* JADX WARNING: Missing block: B:50:0x017e, code skipped:
            if (r0.exists() == false) goto L_0x0180;
     */
    private void onActionClick(boolean r11) {
        /*
        r10 = this;
        r0 = "UTF-8";
        r1 = r10.currentMessageObject;
        if (r1 != 0) goto L_0x000a;
    L_0x0006:
        r1 = r10.currentBotInlineResult;
        if (r1 == 0) goto L_0x0011;
    L_0x000a:
        r1 = r10.currentFileNames;
        r2 = 0;
        r1 = r1[r2];
        if (r1 != 0) goto L_0x0012;
    L_0x0011:
        return;
    L_0x0012:
        r10.isStreaming = r2;
        r1 = r10.currentMessageObject;
        r3 = "mp4";
        r4 = 1;
        r5 = 0;
        if (r1 == 0) goto L_0x012f;
    L_0x001c:
        r1 = r1.messageOwner;
        r1 = r1.attachPath;
        if (r1 == 0) goto L_0x0039;
    L_0x0022:
        r1 = r1.length();
        if (r1 == 0) goto L_0x0039;
    L_0x0028:
        r1 = new java.io.File;
        r6 = r10.currentMessageObject;
        r6 = r6.messageOwner;
        r6 = r6.attachPath;
        r1.<init>(r6);
        r6 = r1.exists();
        if (r6 != 0) goto L_0x003a;
    L_0x0039:
        r1 = r5;
    L_0x003a:
        if (r1 != 0) goto L_0x012c;
    L_0x003c:
        r1 = r10.currentMessageObject;
        r1 = r1.messageOwner;
        r1 = org.telegram.messenger.FileLoader.getPathToMessage(r1);
        r6 = r1.exists();
        if (r6 != 0) goto L_0x012c;
    L_0x004a:
        r1 = org.telegram.messenger.SharedConfig.streamMedia;
        if (r1 == 0) goto L_0x0180;
    L_0x004e:
        r1 = r10.currentMessageObject;
        r6 = r1.getDialogId();
        r1 = (int) r6;
        if (r1 == 0) goto L_0x0180;
    L_0x0057:
        r1 = r10.currentMessageObject;
        r1 = r1.isVideo();
        if (r1 == 0) goto L_0x0180;
    L_0x005f:
        r1 = r10.currentMessageObject;
        r1 = r1.canStreamVideo();
        if (r1 == 0) goto L_0x0180;
    L_0x0067:
        r1 = r10.currentMessageObject;	 Catch:{ Exception -> 0x0128 }
        r1 = r1.currentAccount;	 Catch:{ Exception -> 0x0128 }
        r1 = org.telegram.messenger.FileLoader.getInstance(r1);	 Catch:{ Exception -> 0x0128 }
        r6 = r10.currentMessageObject;	 Catch:{ Exception -> 0x0128 }
        r1 = r1.getFileReference(r6);	 Catch:{ Exception -> 0x0128 }
        r6 = r10.currentAccount;	 Catch:{ Exception -> 0x0128 }
        r6 = org.telegram.messenger.FileLoader.getInstance(r6);	 Catch:{ Exception -> 0x0128 }
        r7 = r10.currentMessageObject;	 Catch:{ Exception -> 0x0128 }
        r7 = r7.getDocument();	 Catch:{ Exception -> 0x0128 }
        r8 = r10.currentMessageObject;	 Catch:{ Exception -> 0x0128 }
        r6.loadFile(r7, r8, r4, r2);	 Catch:{ Exception -> 0x0128 }
        r6 = r10.currentMessageObject;	 Catch:{ Exception -> 0x0128 }
        r6 = r6.getDocument();	 Catch:{ Exception -> 0x0128 }
        r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0128 }
        r7.<init>();	 Catch:{ Exception -> 0x0128 }
        r8 = "?account=";
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = r10.currentMessageObject;	 Catch:{ Exception -> 0x0128 }
        r8 = r8.currentAccount;	 Catch:{ Exception -> 0x0128 }
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = "&id=";
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = r6.id;	 Catch:{ Exception -> 0x0128 }
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = "&hash=";
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = r6.access_hash;	 Catch:{ Exception -> 0x0128 }
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = "&dc=";
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = r6.dc_id;	 Catch:{ Exception -> 0x0128 }
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = "&size=";
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = r6.size;	 Catch:{ Exception -> 0x0128 }
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = "&mime=";
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = r6.mime_type;	 Catch:{ Exception -> 0x0128 }
        r8 = java.net.URLEncoder.encode(r8, r0);	 Catch:{ Exception -> 0x0128 }
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r8 = "&rid=";
        r7.append(r8);	 Catch:{ Exception -> 0x0128 }
        r7.append(r1);	 Catch:{ Exception -> 0x0128 }
        r1 = "&name=";
        r7.append(r1);	 Catch:{ Exception -> 0x0128 }
        r1 = org.telegram.messenger.FileLoader.getDocumentFileName(r6);	 Catch:{ Exception -> 0x0128 }
        r0 = java.net.URLEncoder.encode(r1, r0);	 Catch:{ Exception -> 0x0128 }
        r7.append(r0);	 Catch:{ Exception -> 0x0128 }
        r0 = "&reference=";
        r7.append(r0);	 Catch:{ Exception -> 0x0128 }
        r0 = r6.file_reference;	 Catch:{ Exception -> 0x0128 }
        if (r0 == 0) goto L_0x00f7;
    L_0x00f4:
        r0 = r6.file_reference;	 Catch:{ Exception -> 0x0128 }
        goto L_0x00f9;
    L_0x00f7:
        r0 = new byte[r2];	 Catch:{ Exception -> 0x0128 }
    L_0x00f9:
        r0 = org.telegram.messenger.Utilities.bytesToHex(r0);	 Catch:{ Exception -> 0x0128 }
        r7.append(r0);	 Catch:{ Exception -> 0x0128 }
        r0 = r7.toString();	 Catch:{ Exception -> 0x0128 }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0128 }
        r1.<init>();	 Catch:{ Exception -> 0x0128 }
        r6 = "tg://";
        r1.append(r6);	 Catch:{ Exception -> 0x0128 }
        r6 = r10.currentMessageObject;	 Catch:{ Exception -> 0x0128 }
        r6 = r6.getFileName();	 Catch:{ Exception -> 0x0128 }
        r1.append(r6);	 Catch:{ Exception -> 0x0128 }
        r1.append(r0);	 Catch:{ Exception -> 0x0128 }
        r0 = r1.toString();	 Catch:{ Exception -> 0x0128 }
        r0 = android.net.Uri.parse(r0);	 Catch:{ Exception -> 0x0128 }
        r10.isStreaming = r4;	 Catch:{ Exception -> 0x0129 }
        r10.checkProgress(r2, r2);	 Catch:{ Exception -> 0x0129 }
        goto L_0x0129;
    L_0x0128:
        r0 = r5;
    L_0x0129:
        r1 = r0;
        r0 = r5;
        goto L_0x0182;
    L_0x012c:
        r0 = r1;
    L_0x012d:
        r1 = r5;
        goto L_0x0182;
    L_0x012f:
        r0 = r10.currentBotInlineResult;
        if (r0 == 0) goto L_0x0180;
    L_0x0133:
        r1 = r0.document;
        if (r1 == 0) goto L_0x0142;
    L_0x0137:
        r0 = org.telegram.messenger.FileLoader.getPathToAttach(r1);
        r1 = r0.exists();
        if (r1 != 0) goto L_0x012d;
    L_0x0141:
        goto L_0x0180;
    L_0x0142:
        r0 = r0.content;
        r0 = r0 instanceof org.telegram.tgnet.TLRPC.TL_webDocument;
        if (r0 == 0) goto L_0x0180;
    L_0x0148:
        r0 = new java.io.File;
        r1 = 4;
        r1 = org.telegram.messenger.FileLoader.getDirectory(r1);
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = r10.currentBotInlineResult;
        r7 = r7.content;
        r7 = r7.url;
        r7 = org.telegram.messenger.Utilities.MD5(r7);
        r6.append(r7);
        r7 = ".";
        r6.append(r7);
        r7 = r10.currentBotInlineResult;
        r7 = r7.content;
        r7 = r7.url;
        r7 = org.telegram.messenger.ImageLoader.getHttpUrlExtension(r7, r3);
        r6.append(r7);
        r6 = r6.toString();
        r0.<init>(r1, r6);
        r1 = r0.exists();
        if (r1 != 0) goto L_0x012d;
    L_0x0180:
        r0 = r5;
        r1 = r0;
    L_0x0182:
        if (r0 == 0) goto L_0x018a;
    L_0x0184:
        if (r1 != 0) goto L_0x018a;
    L_0x0186:
        r1 = android.net.Uri.fromFile(r0);
    L_0x018a:
        if (r1 != 0) goto L_0x023e;
    L_0x018c:
        if (r11 == 0) goto L_0x0255;
    L_0x018e:
        r11 = r10.currentMessageObject;
        if (r11 == 0) goto L_0x01c5;
    L_0x0192:
        r11 = r10.currentAccount;
        r11 = org.telegram.messenger.FileLoader.getInstance(r11);
        r0 = r10.currentFileNames;
        r0 = r0[r2];
        r11 = r11.isLoadingFile(r0);
        if (r11 != 0) goto L_0x01b5;
    L_0x01a2:
        r11 = r10.currentAccount;
        r11 = org.telegram.messenger.FileLoader.getInstance(r11);
        r0 = r10.currentMessageObject;
        r0 = r0.getDocument();
        r1 = r10.currentMessageObject;
        r11.loadFile(r0, r1, r4, r2);
        goto L_0x022e;
    L_0x01b5:
        r11 = r10.currentAccount;
        r11 = org.telegram.messenger.FileLoader.getInstance(r11);
        r0 = r10.currentMessageObject;
        r0 = r0.getDocument();
        r11.cancelLoadFile(r0);
        goto L_0x022e;
    L_0x01c5:
        r11 = r10.currentBotInlineResult;
        if (r11 == 0) goto L_0x022e;
    L_0x01c9:
        r0 = r11.document;
        if (r0 == 0) goto L_0x01fb;
    L_0x01cd:
        r11 = r10.currentAccount;
        r11 = org.telegram.messenger.FileLoader.getInstance(r11);
        r0 = r10.currentFileNames;
        r0 = r0[r2];
        r11 = r11.isLoadingFile(r0);
        if (r11 != 0) goto L_0x01ed;
    L_0x01dd:
        r11 = r10.currentAccount;
        r11 = org.telegram.messenger.FileLoader.getInstance(r11);
        r0 = r10.currentBotInlineResult;
        r0 = r0.document;
        r1 = r10.currentMessageObject;
        r11.loadFile(r0, r1, r4, r2);
        goto L_0x022e;
    L_0x01ed:
        r11 = r10.currentAccount;
        r11 = org.telegram.messenger.FileLoader.getInstance(r11);
        r0 = r10.currentBotInlineResult;
        r0 = r0.document;
        r11.cancelLoadFile(r0);
        goto L_0x022e;
    L_0x01fb:
        r11 = r11.content;
        r11 = r11 instanceof org.telegram.tgnet.TLRPC.TL_webDocument;
        if (r11 == 0) goto L_0x022e;
    L_0x0201:
        r11 = org.telegram.messenger.ImageLoader.getInstance();
        r0 = r10.currentBotInlineResult;
        r0 = r0.content;
        r0 = r0.url;
        r11 = r11.isLoadingHttpFile(r0);
        if (r11 != 0) goto L_0x0221;
    L_0x0211:
        r11 = org.telegram.messenger.ImageLoader.getInstance();
        r0 = r10.currentBotInlineResult;
        r0 = r0.content;
        r0 = r0.url;
        r1 = r10.currentAccount;
        r11.loadHttpFile(r0, r3, r1);
        goto L_0x022e;
    L_0x0221:
        r11 = org.telegram.messenger.ImageLoader.getInstance();
        r0 = r10.currentBotInlineResult;
        r0 = r0.content;
        r0 = r0.url;
        r11.cancelLoadHttpFile(r0);
    L_0x022e:
        r11 = r10.centerImage;
        r11 = r11.getStaticThumb();
        r0 = r11 instanceof org.telegram.ui.Components.OtherDocumentPlaceholderDrawable;
        if (r0 == 0) goto L_0x0255;
    L_0x0238:
        r11 = (org.telegram.ui.Components.OtherDocumentPlaceholderDrawable) r11;
        r11.checkFileExist();
        goto L_0x0255;
    L_0x023e:
        r11 = r10.sharedMediaType;
        if (r11 != r4) goto L_0x0252;
    L_0x0242:
        r11 = r10.currentMessageObject;
        r11 = r11.canPreviewDocument();
        if (r11 != 0) goto L_0x0252;
    L_0x024a:
        r11 = r10.currentMessageObject;
        r0 = r10.parentActivity;
        org.telegram.messenger.AndroidUtilities.openDocument(r11, r0, r5);
        return;
    L_0x0252:
        r10.preparePlayer(r1, r4, r2);
    L_0x0255:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PhotoViewer.onActionClick(boolean):void");
    }

    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return (this.canZoom || this.doubleTapEnabled) ? false : onSingleTapConfirmed(motionEvent);
    }

    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        if (this.scale != 1.0f) {
            this.scroller.abortAnimation();
            this.scroller.fling(Math.round(this.translationX), Math.round(this.translationY), Math.round(f), Math.round(f2), (int) this.minX, (int) this.maxX, (int) this.minY, (int) this.maxY);
            this.containerView.postInvalidate();
        }
        return false;
    }

    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        if (this.discardTap) {
            return false;
        }
        float y;
        float containerViewHeight;
        int access$11800;
        if (this.containerView.getTag() != null) {
            AspectRatioFrameLayout aspectRatioFrameLayout = this.aspectRatioFrameLayout;
            Object obj = (aspectRatioFrameLayout == null || aspectRatioFrameLayout.getVisibility() != 0) ? null : 1;
            float x = motionEvent.getX();
            y = motionEvent.getY();
            if (this.sharedMediaType == 1) {
                MessageObject messageObject = this.currentMessageObject;
                if (messageObject != null) {
                    if (!messageObject.canPreviewDocument()) {
                        containerViewHeight = ((float) (getContainerViewHeight() - AndroidUtilities.dp(360.0f))) / 2.0f;
                        if (y >= containerViewHeight && y <= containerViewHeight + ((float) AndroidUtilities.dp(360.0f))) {
                            onActionClick(true);
                            return true;
                        }
                    }
                    toggleActionBar(this.isActionBarVisible ^ 1, true);
                }
            }
            PhotoProgressView[] photoProgressViewArr = this.photoProgressViews;
            if (!(photoProgressViewArr[0] == null || this.containerView == null || obj != null)) {
                access$11800 = photoProgressViewArr[0].backgroundState;
                if (access$11800 > 0 && access$11800 <= 3 && x >= ((float) (getContainerViewWidth() - AndroidUtilities.dp(100.0f))) / 2.0f && x <= ((float) (getContainerViewWidth() + AndroidUtilities.dp(100.0f))) / 2.0f && y >= ((float) (getContainerViewHeight() - AndroidUtilities.dp(100.0f))) / 2.0f && y <= ((float) (getContainerViewHeight() + AndroidUtilities.dp(100.0f))) / 2.0f) {
                    onActionClick(true);
                    checkProgress(0, true);
                    return true;
                }
            }
            toggleActionBar(this.isActionBarVisible ^ 1, true);
        } else {
            access$11800 = this.sendPhotoType;
            if (access$11800 != 0 && access$11800 != 4) {
                BotInlineResult botInlineResult = this.currentBotInlineResult;
                if (botInlineResult != null && (botInlineResult.type.equals("video") || MessageObject.isVideoDocument(this.currentBotInlineResult.document))) {
                    access$11800 = this.photoProgressViews[0].backgroundState;
                    if (access$11800 > 0 && access$11800 <= 3) {
                        containerViewHeight = motionEvent.getX();
                        y = motionEvent.getY();
                        if (containerViewHeight >= ((float) (getContainerViewWidth() - AndroidUtilities.dp(100.0f))) / 2.0f && containerViewHeight <= ((float) (getContainerViewWidth() + AndroidUtilities.dp(100.0f))) / 2.0f && y >= ((float) (getContainerViewHeight() - AndroidUtilities.dp(100.0f))) / 2.0f && y <= ((float) (getContainerViewHeight() + AndroidUtilities.dp(100.0f))) / 2.0f) {
                            onActionClick(true);
                            checkProgress(0, true);
                            return true;
                        }
                    }
                } else if (this.sendPhotoType == 2 && this.isCurrentVideo) {
                    this.videoPlayButton.callOnClick();
                }
            } else if (this.isCurrentVideo) {
                this.videoPlayButton.callOnClick();
            } else {
                this.checkImageView.performClick();
            }
        }
        return true;
    }

    public boolean onDoubleTap(MotionEvent motionEvent) {
        boolean z = false;
        long j = 0;
        if (this.videoPlayer != null && this.videoPlayerControlFrameLayout.getVisibility() == 0) {
            long currentPosition = this.videoPlayer.getCurrentPosition();
            long duration = this.videoPlayer.getDuration();
            if (duration >= 0 && currentPosition >= 0 && duration != -9223372036854775807L && currentPosition != -9223372036854775807L) {
                int containerViewWidth = getContainerViewWidth();
                float x = motionEvent.getX();
                containerViewWidth /= 3;
                long j2 = x >= ((float) (containerViewWidth * 2)) ? 10000 + currentPosition : x < ((float) containerViewWidth) ? currentPosition - 10000 : currentPosition;
                if (currentPosition != j2) {
                    if (j2 > duration) {
                        j = duration;
                    } else if (j2 >= 0) {
                        j = j2;
                    }
                    VideoForwardDrawable videoForwardDrawable = this.videoForwardDrawable;
                    if (x < ((float) containerViewWidth)) {
                        z = true;
                    }
                    videoForwardDrawable.setLeftSide(z);
                    this.videoPlayer.seekTo(j);
                    this.containerView.invalidate();
                    this.videoPlayerSeekbar.setProgress(((float) j) / ((float) duration));
                    this.videoPlayerControlFrameLayout.invalidate();
                    return true;
                }
            }
        }
        if (!this.canZoom || ((this.scale == 1.0f && (this.translationY != 0.0f || this.translationX != 0.0f)) || this.animationStartTime != 0 || this.animationInProgress != 0)) {
            return false;
        }
        if (this.scale == 1.0f) {
            float x2 = (motionEvent.getX() - ((float) (getContainerViewWidth() / 2))) - (((motionEvent.getX() - ((float) (getContainerViewWidth() / 2))) - this.translationX) * (3.0f / this.scale));
            float y = (motionEvent.getY() - ((float) (getContainerViewHeight() / 2))) - (((motionEvent.getY() - ((float) (getContainerViewHeight() / 2))) - this.translationY) * (3.0f / this.scale));
            updateMinMax(3.0f);
            float f = this.minX;
            if (x2 >= f) {
                f = this.maxX;
                if (x2 <= f) {
                    f = x2;
                }
            }
            x2 = this.minY;
            if (y >= x2) {
                x2 = this.maxY;
                if (y <= x2) {
                    x2 = y;
                }
            }
            animateTo(3.0f, f, x2, true);
        } else {
            animateTo(1.0f, 0.0f, 0.0f, true);
        }
        this.doubleTap = true;
        return true;
    }

    public void updateMuteButton() {
        VideoPlayer videoPlayer = this.videoPlayer;
        if (videoPlayer != null) {
            videoPlayer.setMute(this.muteVideo);
        }
        if (this.videoHasAudio) {
            this.muteItem.setEnabled(true);
            this.muteItem.setClickable(true);
            this.muteItem.setAlpha(1.0f);
            if (this.muteVideo) {
                this.actionBar.setSubtitle(null);
                this.muteItem.setImageResource(NUM);
                this.muteItem.setColorFilter(new PorterDuffColorFilter(-12734994, Mode.MULTIPLY));
                if (this.compressItem.getTag() != null) {
                    this.compressItem.setClickable(false);
                    this.compressItem.setAlpha(0.5f);
                    this.compressItem.setEnabled(false);
                }
                this.videoTimelineView.setMaxProgressDiff(30000.0f / this.videoDuration);
                this.muteItem.setContentDescription(LocaleController.getString("NoSound", NUM));
                return;
            }
            this.muteItem.setColorFilter(null);
            this.actionBar.setSubtitle(this.currentSubtitle);
            this.muteItem.setImageResource(NUM);
            this.muteItem.setContentDescription(LocaleController.getString("Sound", NUM));
            if (this.compressItem.getTag() != null) {
                this.compressItem.setClickable(true);
                this.compressItem.setAlpha(1.0f);
                this.compressItem.setEnabled(true);
            }
            this.videoTimelineView.setMaxProgressDiff(1.0f);
            return;
        }
        this.muteItem.setEnabled(false);
        this.muteItem.setClickable(false);
        this.muteItem.setAlpha(0.5f);
    }

    private void didChangedCompressionLevel(boolean z) {
        Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putInt("compress_video2", this.selectedCompression);
        edit.commit();
        updateWidthHeightBitrateForCompression();
        updateVideoInfo();
        if (z) {
            requestVideoPreview(1);
        }
    }

    private void updateVideoInfo() {
        ActionBar actionBar = this.actionBar;
        if (actionBar != null) {
            CharSequence charSequence = null;
            if (this.compressionsCount == 0) {
                actionBar.setSubtitle(null);
                return;
            }
            int i;
            int i2 = this.selectedCompression;
            if (i2 == 0) {
                this.compressItem.setImageResource(NUM);
            } else if (i2 == 1) {
                this.compressItem.setImageResource(NUM);
            } else if (i2 == 2) {
                this.compressItem.setImageResource(NUM);
            } else if (i2 == 3) {
                this.compressItem.setImageResource(NUM);
            } else if (i2 == 4) {
                this.compressItem.setImageResource(NUM);
            }
            String[] strArr = new String[]{"240", "360", "480", "720", "1080"};
            ImageView imageView = this.compressItem;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(LocaleController.getString("AccDescrVideoQuality", NUM));
            stringBuilder.append(", ");
            stringBuilder.append(strArr[Math.max(0, this.selectedCompression)]);
            imageView.setContentDescription(stringBuilder.toString());
            this.estimatedDuration = (long) Math.ceil((double) ((this.videoTimelineView.getRightProgress() - this.videoTimelineView.getLeftProgress()) * this.videoDuration));
            int i3;
            if (this.compressItem.getTag() == null || this.selectedCompression == this.compressionsCount - 1) {
                i2 = this.rotationValue;
                i2 = (i2 == 90 || i2 == 270) ? this.originalHeight : this.originalWidth;
                i3 = this.rotationValue;
                i = (i3 == 90 || i3 == 270) ? this.originalWidth : this.originalHeight;
                this.estimatedSize = (int) (((float) this.originalSize) * (((float) this.estimatedDuration) / this.videoDuration));
            } else {
                i2 = this.rotationValue;
                i2 = (i2 == 90 || i2 == 270) ? this.resultHeight : this.resultWidth;
                i3 = this.rotationValue;
                i = (i3 == 90 || i3 == 270) ? this.resultWidth : this.resultHeight;
                this.estimatedSize = (int) (((float) (this.audioFramesSize + this.videoFramesSize)) * (((float) this.estimatedDuration) / this.videoDuration));
                int i4 = this.estimatedSize;
                this.estimatedSize = i4 + ((i4 / 32768) * 16);
            }
            this.videoCutStart = this.videoTimelineView.getLeftProgress();
            this.videoCutEnd = this.videoTimelineView.getRightProgress();
            float f = this.videoCutStart;
            if (f == 0.0f) {
                this.startTime = -1;
            } else {
                this.startTime = ((long) (f * this.videoDuration)) * 1000;
            }
            f = this.videoCutEnd;
            if (f == 1.0f) {
                this.endTime = -1;
            } else {
                this.endTime = ((long) (f * this.videoDuration)) * 1000;
            }
            String format = String.format("%dx%d", new Object[]{Integer.valueOf(i2), Integer.valueOf(i)});
            String format2 = String.format("%s, ~%s", new Object[]{AndroidUtilities.formatShortDuration((int) (this.estimatedDuration / 1000)), AndroidUtilities.formatFileSize((long) this.estimatedSize)});
            this.currentSubtitle = String.format("%s, %s", new Object[]{format, format2});
            actionBar = this.actionBar;
            if (!this.muteVideo) {
                charSequence = this.currentSubtitle;
            }
            actionBar.setSubtitle(charSequence);
        }
    }

    private void requestVideoPreview(int i) {
        if (this.videoPreviewMessageObject != null) {
            MediaController.getInstance().cancelVideoConvert(this.videoPreviewMessageObject);
        }
        Object obj = (!this.requestingPreview || this.tryStartRequestPreviewOnFinish) ? null : 1;
        this.requestingPreview = false;
        this.loadInitialVideo = false;
        this.progressView.setVisibility(4);
        if (i != 1) {
            this.tryStartRequestPreviewOnFinish = false;
            if (i == 2) {
                preparePlayer(this.currentPlayingVideoFile, false, false);
            }
        } else if (this.selectedCompression == this.compressionsCount - 1) {
            this.tryStartRequestPreviewOnFinish = false;
            if (obj == null) {
                preparePlayer(this.currentPlayingVideoFile, false, false);
            } else {
                this.progressView.setVisibility(0);
                this.loadInitialVideo = true;
            }
        } else {
            VideoEditedInfo videoEditedInfo;
            this.requestingPreview = true;
            releasePlayer(false);
            if (this.videoPreviewMessageObject == null) {
                TL_message tL_message = new TL_message();
                tL_message.id = 0;
                tL_message.message = "";
                tL_message.media = new TL_messageMediaEmpty();
                tL_message.action = new TL_messageActionEmpty();
                this.videoPreviewMessageObject = new MessageObject(UserConfig.selectedAccount, tL_message, false);
                this.videoPreviewMessageObject.messageOwner.attachPath = new File(FileLoader.getDirectory(4), "video_preview.mp4").getAbsolutePath();
                this.videoPreviewMessageObject.videoEditedInfo = new VideoEditedInfo();
                videoEditedInfo = this.videoPreviewMessageObject.videoEditedInfo;
                videoEditedInfo.rotationValue = this.rotationValue;
                videoEditedInfo.originalWidth = this.originalWidth;
                videoEditedInfo.originalHeight = this.originalHeight;
                videoEditedInfo.framerate = this.videoFramerate;
                videoEditedInfo.originalPath = this.currentPlayingVideoFile.getPath();
            }
            videoEditedInfo = this.videoPreviewMessageObject.videoEditedInfo;
            long j = this.startTime;
            videoEditedInfo.startTime = j;
            long j2 = this.endTime;
            videoEditedInfo.endTime = j2;
            if (j == -1) {
                j = 0;
            }
            if (j2 == -1) {
                j2 = (long) (this.videoDuration * 1000.0f);
            }
            if (j2 - j > 5000000) {
                this.videoPreviewMessageObject.videoEditedInfo.endTime = j + 5000000;
            }
            videoEditedInfo = this.videoPreviewMessageObject.videoEditedInfo;
            videoEditedInfo.bitrate = this.bitrate;
            videoEditedInfo.resultWidth = this.resultWidth;
            videoEditedInfo.resultHeight = this.resultHeight;
            if (!MediaController.getInstance().scheduleVideoConvert(this.videoPreviewMessageObject, true)) {
                this.tryStartRequestPreviewOnFinish = true;
            }
            this.requestingPreview = true;
            this.progressView.setVisibility(0);
        }
        this.containerView.invalidate();
    }

    private void updateWidthHeightBitrateForCompression() {
        int i = this.compressionsCount;
        if (i > 0) {
            if (this.selectedCompression >= i) {
                this.selectedCompression = i - 1;
            }
            i = this.selectedCompression;
            if (i != this.compressionsCount - 1) {
                float f;
                if (i == 0) {
                    f = 426.0f;
                    i = 400000;
                } else if (i == 1) {
                    f = 640.0f;
                    i = 900000;
                } else if (i != 2) {
                    i = 2621440;
                    f = 1280.0f;
                } else {
                    f = 854.0f;
                    i = 1100000;
                }
                int i2 = this.originalWidth;
                int i3 = this.originalHeight;
                f /= i2 > i3 ? (float) i2 : (float) i3;
                this.resultWidth = Math.round((((float) this.originalWidth) * f) / 2.0f) * 2;
                this.resultHeight = Math.round((((float) this.originalHeight) * f) / 2.0f) * 2;
                if (this.bitrate != 0) {
                    this.bitrate = Math.min(i, (int) (((float) this.originalBitrate) / f));
                    this.videoFramesSize = (long) ((((float) (this.bitrate / 8)) * this.videoDuration) / 1000.0f);
                }
            }
        }
    }

    private void showQualityView(final boolean z) {
        if (z) {
            this.previousCompression = this.selectedCompression;
        }
        AnimatorSet animatorSet = this.qualityChooseViewAnimation;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        this.qualityChooseViewAnimation = new AnimatorSet();
        AnimatorSet animatorSet2;
        Animator[] animatorArr;
        if (z) {
            this.qualityChooseView.setTag(Integer.valueOf(1));
            animatorSet2 = this.qualityChooseViewAnimation;
            animatorArr = new Animator[3];
            animatorArr[0] = ObjectAnimator.ofFloat(this.pickerView, View.TRANSLATION_Y, new float[]{0.0f, (float) AndroidUtilities.dp(152.0f)});
            animatorArr[1] = ObjectAnimator.ofFloat(this.pickerViewSendButton, View.TRANSLATION_Y, new float[]{0.0f, (float) AndroidUtilities.dp(152.0f)});
            animatorArr[2] = ObjectAnimator.ofFloat(this.bottomLayout, View.TRANSLATION_Y, new float[]{(float) (-AndroidUtilities.dp(48.0f)), (float) AndroidUtilities.dp(104.0f)});
            animatorSet2.playTogether(animatorArr);
        } else {
            this.qualityChooseView.setTag(null);
            animatorSet2 = this.qualityChooseViewAnimation;
            animatorArr = new Animator[3];
            animatorArr[0] = ObjectAnimator.ofFloat(this.qualityChooseView, View.TRANSLATION_Y, new float[]{0.0f, (float) AndroidUtilities.dp(166.0f)});
            animatorArr[1] = ObjectAnimator.ofFloat(this.qualityPicker, View.TRANSLATION_Y, new float[]{0.0f, (float) AndroidUtilities.dp(166.0f)});
            animatorArr[2] = ObjectAnimator.ofFloat(this.bottomLayout, View.TRANSLATION_Y, new float[]{(float) (-AndroidUtilities.dp(48.0f)), (float) AndroidUtilities.dp(118.0f)});
            animatorSet2.playTogether(animatorArr);
        }
        this.qualityChooseViewAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (animator.equals(PhotoViewer.this.qualityChooseViewAnimation)) {
                    PhotoViewer.this.qualityChooseViewAnimation = new AnimatorSet();
                    AnimatorSet access$16300;
                    Animator[] animatorArr;
                    if (z) {
                        PhotoViewer.this.qualityChooseView.setVisibility(0);
                        PhotoViewer.this.qualityPicker.setVisibility(0);
                        access$16300 = PhotoViewer.this.qualityChooseViewAnimation;
                        animatorArr = new Animator[3];
                        animatorArr[0] = ObjectAnimator.ofFloat(PhotoViewer.this.qualityChooseView, View.TRANSLATION_Y, new float[]{0.0f});
                        animatorArr[1] = ObjectAnimator.ofFloat(PhotoViewer.this.qualityPicker, View.TRANSLATION_Y, new float[]{0.0f});
                        animatorArr[2] = ObjectAnimator.ofFloat(PhotoViewer.this.bottomLayout, View.TRANSLATION_Y, new float[]{(float) (-AndroidUtilities.dp(48.0f))});
                        access$16300.playTogether(animatorArr);
                    } else {
                        PhotoViewer.this.qualityChooseView.setVisibility(4);
                        PhotoViewer.this.qualityPicker.setVisibility(4);
                        access$16300 = PhotoViewer.this.qualityChooseViewAnimation;
                        animatorArr = new Animator[3];
                        animatorArr[0] = ObjectAnimator.ofFloat(PhotoViewer.this.pickerView, View.TRANSLATION_Y, new float[]{0.0f});
                        animatorArr[1] = ObjectAnimator.ofFloat(PhotoViewer.this.pickerViewSendButton, View.TRANSLATION_Y, new float[]{0.0f});
                        animatorArr[2] = ObjectAnimator.ofFloat(PhotoViewer.this.bottomLayout, View.TRANSLATION_Y, new float[]{(float) (-AndroidUtilities.dp(48.0f))});
                        access$16300.playTogether(animatorArr);
                    }
                    PhotoViewer.this.qualityChooseViewAnimation.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            if (animator.equals(PhotoViewer.this.qualityChooseViewAnimation)) {
                                PhotoViewer.this.qualityChooseViewAnimation = null;
                            }
                        }
                    });
                    PhotoViewer.this.qualityChooseViewAnimation.setDuration(200);
                    PhotoViewer.this.qualityChooseViewAnimation.setInterpolator(new AccelerateInterpolator());
                    PhotoViewer.this.qualityChooseViewAnimation.start();
                }
            }

            public void onAnimationCancel(Animator animator) {
                PhotoViewer.this.qualityChooseViewAnimation = null;
            }
        });
        this.qualityChooseViewAnimation.setDuration(200);
        this.qualityChooseViewAnimation.setInterpolator(new DecelerateInterpolator());
        this.qualityChooseViewAnimation.start();
    }

    private ByteArrayInputStream cleanBuffer(byte[] bArr) {
        byte[] bArr2 = new byte[bArr.length];
        int i = 0;
        int i2 = 0;
        while (i < bArr.length) {
            if (bArr[i] == (byte) 0 && bArr[i + 1] == (byte) 0 && bArr[i + 2] == (byte) 3) {
                bArr2[i2] = (byte) 0;
                bArr2[i2 + 1] = (byte) 0;
                i += 3;
                i2 += 2;
            } else {
                bArr2[i2] = bArr[i];
                i++;
                i2++;
            }
        }
        return new ByteArrayInputStream(bArr2, 0, i2);
    }

    private void processOpenVideo(final String str, boolean z, float f, float f2) {
        if (this.currentLoadingVideoRunnable != null) {
            Utilities.globalQueue.cancelRunnable(this.currentLoadingVideoRunnable);
            this.currentLoadingVideoRunnable = null;
        }
        this.videoTimelineView.setVideoPath(str, f, f2);
        this.videoPreviewMessageObject = null;
        setCompressItemEnabled(false, true);
        this.muteVideo = z;
        Object obj = this.imagesArrLocals.get(this.currentIndex);
        if (obj instanceof PhotoEntry) {
            ((PhotoEntry) obj).editedInfo = getCurrentVideoEditedInfo();
        }
        this.compressionsCount = -1;
        this.rotationValue = 0;
        this.videoFramerate = 25;
        this.originalSize = new File(str).length();
        DispatchQueue dispatchQueue = Utilities.globalQueue;
        AnonymousClass45 anonymousClass45 = new Runnable() {
            public void run() {
                if (PhotoViewer.this.currentLoadingVideoRunnable == this) {
                    int[] iArr = new int[9];
                    AnimatedFileDrawable.getVideoInfo(str, iArr);
                    if (PhotoViewer.this.currentLoadingVideoRunnable == this) {
                        PhotoViewer.this.currentLoadingVideoRunnable = null;
                        AndroidUtilities.runOnUIThread(new -$$Lambda$PhotoViewer$45$vnjlJkGknh6sv6GFMM7cRd1oepc(this, iArr));
                    }
                }
            }

            public /* synthetic */ void lambda$run$0$PhotoViewer$45(int[] iArr) {
                String str = "video/avc";
                if (PhotoViewer.this.parentActivity != null) {
                    PhotoViewer.this.videoHasAudio = iArr[0] != 0;
                    PhotoViewer.this.audioFramesSize = (long) iArr[5];
                    PhotoViewer.this.videoFramesSize = (long) iArr[6];
                    PhotoViewer.this.videoDuration = (float) iArr[4];
                    PhotoViewer photoViewer = PhotoViewer.this;
                    photoViewer.originalBitrate = photoViewer.bitrate = iArr[3];
                    PhotoViewer.this.videoFramerate = iArr[7];
                    if (PhotoViewer.this.bitrate > 900000) {
                        PhotoViewer.this.bitrate = 900000;
                    }
                    if (PhotoViewer.this.videoHasAudio) {
                        StringBuilder stringBuilder;
                        PhotoViewer.this.rotationValue = iArr[8];
                        photoViewer = PhotoViewer.this;
                        photoViewer.resultWidth = photoViewer.originalWidth = iArr[1];
                        photoViewer = PhotoViewer.this;
                        photoViewer.resultHeight = photoViewer.originalHeight = iArr[2];
                        PhotoViewer.this.selectedCompression = MessagesController.getGlobalMainSettings().getInt("compress_video2", 1);
                        if (PhotoViewer.this.originalWidth > 1280 || PhotoViewer.this.originalHeight > 1280) {
                            PhotoViewer.this.compressionsCount = 5;
                        } else if (PhotoViewer.this.originalWidth > 854 || PhotoViewer.this.originalHeight > 854) {
                            PhotoViewer.this.compressionsCount = 4;
                        } else if (PhotoViewer.this.originalWidth > 640 || PhotoViewer.this.originalHeight > 640) {
                            PhotoViewer.this.compressionsCount = 3;
                        } else if (PhotoViewer.this.originalWidth > 480 || PhotoViewer.this.originalHeight > 480) {
                            PhotoViewer.this.compressionsCount = 2;
                        } else {
                            PhotoViewer.this.compressionsCount = 1;
                        }
                        PhotoViewer.this.updateWidthHeightBitrateForCompression();
                        PhotoViewer photoViewer2 = PhotoViewer.this;
                        photoViewer2.setCompressItemEnabled(photoViewer2.compressionsCount > 1, true);
                        if (BuildVars.LOGS_ENABLED) {
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("compressionsCount = ");
                            stringBuilder.append(PhotoViewer.this.compressionsCount);
                            stringBuilder.append(" w = ");
                            stringBuilder.append(PhotoViewer.this.originalWidth);
                            stringBuilder.append(" h = ");
                            stringBuilder.append(PhotoViewer.this.originalHeight);
                            FileLog.d(stringBuilder.toString());
                        }
                        if (VERSION.SDK_INT < 18 && PhotoViewer.this.compressItem.getTag() != null) {
                            try {
                                MediaCodecInfo selectCodec = MediaController.selectCodec(str);
                                if (selectCodec == null) {
                                    if (BuildVars.LOGS_ENABLED) {
                                        FileLog.d("no codec info for video/avc");
                                    }
                                    PhotoViewer.this.setCompressItemEnabled(false, true);
                                } else {
                                    String name = selectCodec.getName();
                                    if (!(name.equals("OMX.google.h264.encoder") || name.equals("OMX.ST.VFM.H264Enc") || name.equals("OMX.Exynos.avc.enc") || name.equals("OMX.MARVELL.VIDEO.HW.CODA7542ENCODER") || name.equals("OMX.MARVELL.VIDEO.H264ENCODER") || name.equals("OMX.k3.video.encoder.avc"))) {
                                        if (!name.equals("OMX.TI.DUCATI1.VIDEO.H264E")) {
                                            if (MediaController.selectColorFormat(selectCodec, str) == 0) {
                                                if (BuildVars.LOGS_ENABLED) {
                                                    FileLog.d("no color format for video/avc");
                                                }
                                                PhotoViewer.this.setCompressItemEnabled(false, true);
                                            }
                                        }
                                    }
                                    if (BuildVars.LOGS_ENABLED) {
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append("unsupported encoder = ");
                                        stringBuilder.append(name);
                                        FileLog.d(stringBuilder.toString());
                                    }
                                    PhotoViewer.this.setCompressItemEnabled(false, true);
                                }
                            } catch (Exception e) {
                                PhotoViewer.this.setCompressItemEnabled(false, true);
                                FileLog.e(e);
                            }
                        }
                        PhotoViewer.this.qualityChooseView.invalidate();
                    } else {
                        PhotoViewer.this.compressionsCount = 0;
                    }
                    PhotoViewer.this.updateVideoInfo();
                    PhotoViewer.this.updateMuteButton();
                }
            }
        };
        this.currentLoadingVideoRunnable = anonymousClass45;
        dispatchQueue.postRunnable(anonymousClass45);
    }

    private void setCompressItemEnabled(boolean z, boolean z2) {
        ImageView imageView = this.compressItem;
        if (imageView != null) {
            if ((!z || imageView.getTag() == null) && (z || this.compressItem.getTag() != null)) {
                this.compressItem.setTag(z ? Integer.valueOf(1) : null);
                this.compressItem.setEnabled(z);
                this.compressItem.setClickable(z);
                AnimatorSet animatorSet = this.compressItemAnimation;
                if (animatorSet != null) {
                    animatorSet.cancel();
                    this.compressItemAnimation = null;
                }
                float f = 1.0f;
                if (z2) {
                    this.compressItemAnimation = new AnimatorSet();
                    AnimatorSet animatorSet2 = this.compressItemAnimation;
                    Animator[] animatorArr = new Animator[1];
                    ImageView imageView2 = this.compressItem;
                    Property property = View.ALPHA;
                    float[] fArr = new float[1];
                    if (!z) {
                        f = 0.5f;
                    }
                    fArr[0] = f;
                    animatorArr[0] = ObjectAnimator.ofFloat(imageView2, property, fArr);
                    animatorSet2.playTogether(animatorArr);
                    this.compressItemAnimation.setDuration(180);
                    this.compressItemAnimation.setInterpolator(decelerateInterpolator);
                    this.compressItemAnimation.start();
                } else {
                    ImageView imageView3 = this.compressItem;
                    if (!z) {
                        f = 0.5f;
                    }
                    imageView3.setAlpha(f);
                }
            }
        }
    }

    private void updateAccessibilityOverlayVisibility() {
        if (this.playButtonAccessibilityOverlay != null) {
            if (this.isCurrentVideo) {
                VideoPlayer videoPlayer = this.videoPlayer;
                if (videoPlayer == null || !videoPlayer.isPlaying()) {
                    this.playButtonAccessibilityOverlay.setVisibility(0);
                }
            }
            this.playButtonAccessibilityOverlay.setVisibility(4);
        }
    }
}
