package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;
import java.io.File;
import java.util.List;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimatorListenerAdapterProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.exoplayer.C;
import org.telegram.messenger.exoplayer.DefaultLoadControl;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.volley.DefaultRetryPolicy;
import org.telegram.tgnet.TLRPC.BotInlineResult;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.MentionsAdapter;
import org.telegram.ui.Adapters.MentionsAdapter.MentionsAdapterDelegate;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PhotoViewerCaptionEnterView;
import org.telegram.ui.Components.PhotoViewerCaptionEnterView.PhotoViewerCaptionEnterViewDelegate;
import org.telegram.ui.Components.PickerBottomLayoutViewer;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.SizeNotifierFrameLayoutPhoto;
import org.telegram.ui.Components.VideoSeekBarView;
import org.telegram.ui.Components.VideoSeekBarView.SeekBarDelegate;
import org.telegram.ui.Components.VideoTimelineView;
import org.telegram.ui.Components.VideoTimelineView.VideoTimelineViewDelegate;

@TargetApi(16)
public class VideoEditorActivity extends BaseFragment implements NotificationCenterDelegate {
    private boolean allowMentions;
    private long audioFramesSize;
    private int bitrate;
    private ActionBarMenuItem captionDoneItem;
    private PhotoViewerCaptionEnterView captionEditText;
    private ImageView captionItem;
    private ImageView compressItem;
    private boolean created;
    private CharSequence currentCaption;
    private VideoEditorActivityDelegate delegate;
    private long endTime;
    private long esimatedDuration;
    private int estimatedSize;
    private float lastProgress;
    private LinearLayoutManager mentionLayoutManager;
    private AnimatorSet mentionListAnimation;
    private RecyclerListView mentionListView;
    private MentionsAdapter mentionsAdapter;
    private ImageView muteItem;
    private boolean muteVideo;
    private boolean needCompressVideo;
    private boolean needSeek;
    private String oldTitle;
    private int originalBitrate;
    private int originalHeight;
    private long originalSize;
    private int originalWidth;
    private ChatActivity parentChatActivity;
    private PickerBottomLayoutViewer pickerView;
    private ImageView playButton;
    private boolean playerPrepared;
    private Runnable progressRunnable = new Runnable() {
        public void run() {
            while (true) {
                synchronized (VideoEditorActivity.this.sync) {
                    try {
                        boolean playerCheck = VideoEditorActivity.this.videoPlayer != null && VideoEditorActivity.this.videoPlayer.isPlaying();
                    } catch (Throwable e) {
                        playerCheck = false;
                        FileLog.e("tmessages", e);
                    }
                }
                if (playerCheck) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (VideoEditorActivity.this.videoPlayer != null && VideoEditorActivity.this.videoPlayer.isPlaying()) {
                                float startTime = VideoEditorActivity.this.videoTimelineView.getLeftProgress() * VideoEditorActivity.this.videoDuration;
                                float endTime = VideoEditorActivity.this.videoTimelineView.getRightProgress() * VideoEditorActivity.this.videoDuration;
                                if (startTime == endTime) {
                                    startTime = endTime - 0.01f;
                                }
                                float lrdiff = VideoEditorActivity.this.videoTimelineView.getRightProgress() - VideoEditorActivity.this.videoTimelineView.getLeftProgress();
                                float progress = VideoEditorActivity.this.videoTimelineView.getLeftProgress() + (lrdiff * ((((float) VideoEditorActivity.this.videoPlayer.getCurrentPosition()) - startTime) / (endTime - startTime)));
                                if (progress > VideoEditorActivity.this.lastProgress) {
                                    VideoEditorActivity.this.videoSeekBarView.setProgress(progress);
                                    VideoEditorActivity.this.lastProgress = progress;
                                }
                                if (((float) VideoEditorActivity.this.videoPlayer.getCurrentPosition()) >= endTime) {
                                    try {
                                        VideoEditorActivity.this.videoPlayer.pause();
                                        VideoEditorActivity.this.onPlayComplete();
                                    } catch (Throwable e) {
                                        FileLog.e("tmessages", e);
                                    }
                                }
                            }
                        }
                    });
                    try {
                        Thread.sleep(50);
                    } catch (Throwable e2) {
                        FileLog.e("tmessages", e2);
                    }
                } else {
                    synchronized (VideoEditorActivity.this.sync) {
                        VideoEditorActivity.this.thread = null;
                    }
                    return;
                }
            }
        }
    };
    private int resultHeight;
    private int resultWidth;
    private int rotationValue;
    private long startTime;
    private final Object sync = new Object();
    private TextureView textureView;
    private Thread thread;
    private float videoDuration;
    private long videoFramesSize;
    private String videoPath;
    private MediaPlayer videoPlayer;
    private VideoSeekBarView videoSeekBarView;
    private VideoTimelineView videoTimelineView;

    public interface VideoEditorActivityDelegate {
        void didFinishEditVideo(String str, long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, long j3, long j4, String str2);
    }

    public VideoEditorActivity(Bundle args) {
        super(args);
        this.videoPath = args.getString("videoPath");
    }

    public boolean onFragmentCreate() {
        if (this.created) {
            return true;
        }
        if (this.videoPath == null || !processOpenVideo()) {
            return false;
        }
        this.videoPlayer = new MediaPlayer();
        this.videoPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        VideoEditorActivity.this.onPlayComplete();
                    }
                });
            }
        });
        this.videoPlayer.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                VideoEditorActivity.this.playerPrepared = true;
                if (VideoEditorActivity.this.videoTimelineView != null && VideoEditorActivity.this.videoPlayer != null) {
                    VideoEditorActivity.this.videoPlayer.seekTo((int) (VideoEditorActivity.this.videoTimelineView.getLeftProgress() * VideoEditorActivity.this.videoDuration));
                }
            }
        });
        try {
            this.videoPlayer.setDataSource(this.videoPath);
            this.videoPlayer.prepareAsync();
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
            this.created = true;
            return super.onFragmentCreate();
        } catch (Throwable e) {
            FileLog.e("tmessages", e);
            return false;
        }
    }

    public void onFragmentDestroy() {
        if (this.videoTimelineView != null) {
            this.videoTimelineView.destroy();
        }
        if (this.videoPlayer != null) {
            try {
                this.videoPlayer.stop();
                this.videoPlayer.release();
                this.videoPlayer = null;
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }
        }
        if (this.captionEditText != null) {
            this.captionEditText.onDestroy();
        }
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        super.onFragmentDestroy();
    }

    public View createView(Context context) {
        this.needCompressVideo = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("compress_video", true);
        this.actionBar.setBackgroundColor(-16777216);
        this.actionBar.setItemsBackgroundColor(Theme.ACTION_BAR_PICKER_SELECTOR_COLOR);
        this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        this.actionBar.setTitle(LocaleController.getString("AttachVideo", R.string.AttachVideo));
        this.actionBar.setSubtitleColor(-1);
        this.actionBar.setActionBarMenuOnItemClick(new ActionBarMenuOnItemClick() {
            public void onItemClick(int id) {
                if (id == -1) {
                    if (VideoEditorActivity.this.captionEditText.isPopupShowing() || VideoEditorActivity.this.captionEditText.isKeyboardVisible()) {
                        VideoEditorActivity.this.closeCaptionEnter(false);
                    } else {
                        VideoEditorActivity.this.finishFragment();
                    }
                } else if (id == 1) {
                    VideoEditorActivity.this.closeCaptionEnter(true);
                }
            }
        });
        this.captionDoneItem = this.actionBar.createMenu().addItemWithWidth(1, R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        this.captionDoneItem.setVisibility(8);
        this.fragmentView = new SizeNotifierFrameLayoutPhoto(context) {
            int lastWidth;

            public boolean dispatchKeyEventPreIme(KeyEvent event) {
                if (event == null || event.getKeyCode() != 4 || event.getAction() != 1 || (!VideoEditorActivity.this.captionEditText.isPopupShowing() && !VideoEditorActivity.this.captionEditText.isKeyboardVisible())) {
                    return super.dispatchKeyEventPreIme(event);
                }
                VideoEditorActivity.this.closeCaptionEnter(false);
                return false;
            }

            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);
                setMeasuredDimension(widthSize, heightSize);
                if (!AndroidUtilities.isTablet()) {
                    heightSize = AndroidUtilities.displaySize.y - ActionBar.getCurrentActionBarHeight();
                }
                measureChildWithMargins(VideoEditorActivity.this.captionEditText, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int inputFieldHeight = VideoEditorActivity.this.captionEditText.getMeasuredHeight();
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (!(child.getVisibility() == 8 || child == VideoEditorActivity.this.captionEditText)) {
                        if (VideoEditorActivity.this.captionEditText.isPopupView(child)) {
                            if (!AndroidUtilities.isInMultiwindow && !AndroidUtilities.isTablet()) {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, C.ENCODING_PCM_32BIT), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, C.ENCODING_PCM_32BIT));
                            } else if (AndroidUtilities.isTablet()) {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, C.ENCODING_PCM_32BIT), MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320.0f), (heightSize - inputFieldHeight) - AndroidUtilities.statusBarHeight), C.ENCODING_PCM_32BIT));
                            } else {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, C.ENCODING_PCM_32BIT), MeasureSpec.makeMeasureSpec((heightSize - inputFieldHeight) - AndroidUtilities.statusBarHeight, C.ENCODING_PCM_32BIT));
                            }
                        } else if (child == VideoEditorActivity.this.textureView) {
                            int width = widthSize;
                            int height = heightSize - AndroidUtilities.dp(166.0f);
                            int vwidth = (VideoEditorActivity.this.rotationValue == 90 || VideoEditorActivity.this.rotationValue == 270) ? VideoEditorActivity.this.originalHeight : VideoEditorActivity.this.originalWidth;
                            int vheight = (VideoEditorActivity.this.rotationValue == 90 || VideoEditorActivity.this.rotationValue == 270) ? VideoEditorActivity.this.originalWidth : VideoEditorActivity.this.originalHeight;
                            float ar = ((float) vwidth) / ((float) vheight);
                            if (((float) width) / ((float) vwidth) > ((float) height) / ((float) vheight)) {
                                width = (int) (((float) height) * ar);
                            } else {
                                height = (int) (((float) width) / ar);
                            }
                            child.measure(MeasureSpec.makeMeasureSpec(width, C.ENCODING_PCM_32BIT), MeasureSpec.makeMeasureSpec(height, C.ENCODING_PCM_32BIT));
                        } else {
                            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                        }
                    }
                }
                if (this.lastWidth != widthSize) {
                    VideoEditorActivity.this.videoTimelineView.clearFrames();
                    this.lastWidth = widthSize;
                }
            }

            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                int heightSize;
                int count = getChildCount();
                int paddingBottom = (getKeyboardHeight() > AndroidUtilities.dp(20.0f) || AndroidUtilities.isInMultiwindow || AndroidUtilities.isTablet()) ? 0 : VideoEditorActivity.this.captionEditText.getEmojiPadding();
                if (AndroidUtilities.isTablet()) {
                    heightSize = getMeasuredHeight();
                } else {
                    heightSize = AndroidUtilities.displaySize.y - ActionBar.getCurrentActionBarHeight();
                }
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
                                childTop = (((heightSize - height) / 2) + lp.topMargin) - lp.bottomMargin;
                                break;
                            case 48:
                                childTop = lp.topMargin;
                                break;
                            case 80:
                                childTop = (heightSize - height) - lp.bottomMargin;
                                break;
                            default:
                                childTop = lp.topMargin;
                                break;
                        }
                        if (child == VideoEditorActivity.this.mentionListView) {
                            childTop = (((b - paddingBottom) - t) - height) - lp.bottomMargin;
                            childTop = (VideoEditorActivity.this.captionEditText.isPopupShowing() || VideoEditorActivity.this.captionEditText.isKeyboardVisible() || VideoEditorActivity.this.captionEditText.getEmojiPadding() != 0) ? childTop - VideoEditorActivity.this.captionEditText.getMeasuredHeight() : childTop + AndroidUtilities.dp(400.0f);
                        } else if (child == VideoEditorActivity.this.captionEditText) {
                            childTop = (((b - paddingBottom) - t) - height) - lp.bottomMargin;
                            if (!(VideoEditorActivity.this.captionEditText.isPopupShowing() || VideoEditorActivity.this.captionEditText.isKeyboardVisible() || VideoEditorActivity.this.captionEditText.getEmojiPadding() != 0)) {
                                childTop += AndroidUtilities.dp(400.0f);
                            }
                        } else if (VideoEditorActivity.this.captionEditText.isPopupView(child)) {
                            childTop = (AndroidUtilities.isInMultiwindow || AndroidUtilities.isTablet()) ? (VideoEditorActivity.this.captionEditText.getTop() - child.getMeasuredHeight()) + AndroidUtilities.dp(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) : VideoEditorActivity.this.captionEditText.getBottom();
                        } else if (child == VideoEditorActivity.this.textureView) {
                            childLeft = ((r - l) - VideoEditorActivity.this.textureView.getMeasuredWidth()) / 2;
                            childTop = AndroidUtilities.dp(14.0f);
                        }
                        child.layout(childLeft, childTop, childLeft + width, childTop + height);
                    }
                }
                notifyHeightChanged();
            }
        };
        this.fragmentView.setBackgroundColor(-16777216);
        SizeNotifierFrameLayoutPhoto frameLayout = this.fragmentView;
        frameLayout.setWithoutWindow(true);
        this.pickerView = new PickerBottomLayoutViewer(context);
        this.pickerView.setBackgroundColor(0);
        this.pickerView.updateSelectedCount(0, false);
        frameLayout.addView(this.pickerView, LayoutHelper.createFrame(-1, 48, 83));
        this.pickerView.cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                VideoEditorActivity.this.finishFragment();
            }
        });
        this.pickerView.doneButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                synchronized (VideoEditorActivity.this.sync) {
                    if (VideoEditorActivity.this.videoPlayer != null) {
                        try {
                            VideoEditorActivity.this.videoPlayer.stop();
                            VideoEditorActivity.this.videoPlayer.release();
                            VideoEditorActivity.this.videoPlayer = null;
                        } catch (Throwable e) {
                            FileLog.e("tmessages", e);
                        }
                    }
                }
                if (VideoEditorActivity.this.delegate != null) {
                    if (VideoEditorActivity.this.compressItem.getVisibility() == 8 || (VideoEditorActivity.this.compressItem.getVisibility() == 0 && !VideoEditorActivity.this.needCompressVideo)) {
                        VideoEditorActivity.this.delegate.didFinishEditVideo(VideoEditorActivity.this.videoPath, VideoEditorActivity.this.startTime, VideoEditorActivity.this.endTime, VideoEditorActivity.this.originalWidth, VideoEditorActivity.this.originalHeight, VideoEditorActivity.this.rotationValue, VideoEditorActivity.this.originalWidth, VideoEditorActivity.this.originalHeight, VideoEditorActivity.this.muteVideo ? -1 : VideoEditorActivity.this.originalBitrate, (long) VideoEditorActivity.this.estimatedSize, VideoEditorActivity.this.esimatedDuration, VideoEditorActivity.this.currentCaption != null ? VideoEditorActivity.this.currentCaption.toString() : null);
                    } else {
                        VideoEditorActivity.this.delegate.didFinishEditVideo(VideoEditorActivity.this.videoPath, VideoEditorActivity.this.startTime, VideoEditorActivity.this.endTime, VideoEditorActivity.this.resultWidth, VideoEditorActivity.this.resultHeight, VideoEditorActivity.this.rotationValue, VideoEditorActivity.this.originalWidth, VideoEditorActivity.this.originalHeight, VideoEditorActivity.this.muteVideo ? -1 : VideoEditorActivity.this.bitrate, (long) VideoEditorActivity.this.estimatedSize, VideoEditorActivity.this.esimatedDuration, VideoEditorActivity.this.currentCaption != null ? VideoEditorActivity.this.currentCaption.toString() : null);
                    }
                }
                VideoEditorActivity.this.finishFragment();
            }
        });
        LinearLayout itemsLayout = new LinearLayout(context);
        itemsLayout.setOrientation(0);
        this.pickerView.addView(itemsLayout, LayoutHelper.createFrame(-2, 48, 49));
        this.captionItem = new ImageView(context);
        this.captionItem.setScaleType(ScaleType.CENTER);
        this.captionItem.setImageResource(TextUtils.isEmpty(this.currentCaption) ? R.drawable.photo_text : R.drawable.photo_text2);
        this.captionItem.setBackgroundDrawable(Theme.createBarSelectorDrawable(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR));
        itemsLayout.addView(this.captionItem, LayoutHelper.createLinear(56, 48));
        this.captionItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                VideoEditorActivity.this.captionEditText.setFieldText(VideoEditorActivity.this.currentCaption);
                VideoEditorActivity.this.captionDoneItem.setVisibility(0);
                VideoEditorActivity.this.pickerView.setVisibility(8);
                if (!AndroidUtilities.isTablet()) {
                    VideoEditorActivity.this.videoSeekBarView.setVisibility(8);
                    VideoEditorActivity.this.videoTimelineView.setVisibility(8);
                }
                LayoutParams layoutParams = (LayoutParams) VideoEditorActivity.this.captionEditText.getLayoutParams();
                layoutParams.bottomMargin = 0;
                VideoEditorActivity.this.captionEditText.setLayoutParams(layoutParams);
                layoutParams = (LayoutParams) VideoEditorActivity.this.mentionListView.getLayoutParams();
                layoutParams.bottomMargin = 0;
                VideoEditorActivity.this.mentionListView.setLayoutParams(layoutParams);
                VideoEditorActivity.this.captionEditText.openKeyboard();
                VideoEditorActivity.this.oldTitle = VideoEditorActivity.this.actionBar.getSubtitle();
                VideoEditorActivity.this.actionBar.setTitle(LocaleController.getString("VideoCaption", R.string.VideoCaption));
                VideoEditorActivity.this.actionBar.setSubtitle(null);
            }
        });
        this.compressItem = new ImageView(context);
        this.compressItem.setScaleType(ScaleType.CENTER);
        this.compressItem.setImageResource(this.needCompressVideo ? R.drawable.hd_off : R.drawable.hd_on);
        this.compressItem.setBackgroundDrawable(Theme.createBarSelectorDrawable(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR));
        ImageView imageView = this.compressItem;
        int i = (this.originalHeight == this.resultHeight && this.originalWidth == this.resultWidth) ? 8 : 0;
        imageView.setVisibility(i);
        itemsLayout.addView(this.compressItem, LayoutHelper.createLinear(56, 48));
        this.compressItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                boolean z;
                VideoEditorActivity videoEditorActivity = VideoEditorActivity.this;
                if (VideoEditorActivity.this.needCompressVideo) {
                    z = false;
                } else {
                    z = true;
                }
                videoEditorActivity.needCompressVideo = z;
                Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
                editor.putBoolean("compress_video", VideoEditorActivity.this.needCompressVideo);
                editor.commit();
                VideoEditorActivity.this.compressItem.setImageResource(VideoEditorActivity.this.needCompressVideo ? R.drawable.hd_off : R.drawable.hd_on);
                VideoEditorActivity.this.updateVideoInfo();
            }
        });
        if (VERSION.SDK_INT < 18) {
            try {
                MediaCodecInfo codecInfo = MediaController.selectCodec("video/avc");
                if (codecInfo == null) {
                    this.compressItem.setVisibility(8);
                } else {
                    String name = codecInfo.getName();
                    if (name.equals("OMX.google.h264.encoder") || name.equals("OMX.ST.VFM.H264Enc") || name.equals("OMX.Exynos.avc.enc") || name.equals("OMX.MARVELL.VIDEO.HW.CODA7542ENCODER") || name.equals("OMX.MARVELL.VIDEO.H264ENCODER") || name.equals("OMX.k3.video.encoder.avc") || name.equals("OMX.TI.DUCATI1.VIDEO.H264E")) {
                        this.compressItem.setVisibility(8);
                    } else if (MediaController.selectColorFormat(codecInfo, "video/avc") == 0) {
                        this.compressItem.setVisibility(8);
                    }
                }
            } catch (Throwable e) {
                this.compressItem.setVisibility(8);
                FileLog.e("tmessages", e);
            }
        }
        this.muteItem = new ImageView(context);
        this.muteItem.setScaleType(ScaleType.CENTER);
        this.muteItem.setBackgroundDrawable(Theme.createBarSelectorDrawable(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR));
        itemsLayout.addView(this.muteItem, LayoutHelper.createLinear(56, 48));
        this.muteItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                VideoEditorActivity.this.muteVideo = !VideoEditorActivity.this.muteVideo;
                VideoEditorActivity.this.updateMuteButton();
            }
        });
        this.videoTimelineView = new VideoTimelineView(context);
        this.videoTimelineView.setVideoPath(this.videoPath);
        this.videoTimelineView.setDelegate(new VideoTimelineViewDelegate() {
            public void onLeftProgressChanged(float progress) {
                if (VideoEditorActivity.this.videoPlayer != null && VideoEditorActivity.this.playerPrepared) {
                    try {
                        if (VideoEditorActivity.this.videoPlayer.isPlaying()) {
                            VideoEditorActivity.this.videoPlayer.pause();
                            VideoEditorActivity.this.playButton.setImageResource(R.drawable.video_edit_play);
                        }
                        VideoEditorActivity.this.videoPlayer.setOnSeekCompleteListener(null);
                        VideoEditorActivity.this.videoPlayer.seekTo((int) (VideoEditorActivity.this.videoDuration * progress));
                    } catch (Throwable e) {
                        FileLog.e("tmessages", e);
                    }
                    VideoEditorActivity.this.needSeek = true;
                    VideoEditorActivity.this.videoSeekBarView.setProgress(VideoEditorActivity.this.videoTimelineView.getLeftProgress());
                    VideoEditorActivity.this.updateVideoInfo();
                }
            }

            public void onRifhtProgressChanged(float progress) {
                if (VideoEditorActivity.this.videoPlayer != null && VideoEditorActivity.this.playerPrepared) {
                    try {
                        if (VideoEditorActivity.this.videoPlayer.isPlaying()) {
                            VideoEditorActivity.this.videoPlayer.pause();
                            VideoEditorActivity.this.playButton.setImageResource(R.drawable.video_edit_play);
                        }
                        VideoEditorActivity.this.videoPlayer.setOnSeekCompleteListener(null);
                        VideoEditorActivity.this.videoPlayer.seekTo((int) (VideoEditorActivity.this.videoDuration * progress));
                    } catch (Throwable e) {
                        FileLog.e("tmessages", e);
                    }
                    VideoEditorActivity.this.needSeek = true;
                    VideoEditorActivity.this.videoSeekBarView.setProgress(VideoEditorActivity.this.videoTimelineView.getLeftProgress());
                    VideoEditorActivity.this.updateVideoInfo();
                }
            }
        });
        frameLayout.addView(this.videoTimelineView, LayoutHelper.createFrame(-1, 44.0f, 83, 0.0f, 0.0f, 0.0f, 67.0f));
        this.videoSeekBarView = new VideoSeekBarView(context);
        this.videoSeekBarView.setDelegate(new SeekBarDelegate() {
            public void onSeekBarDrag(float progress) {
                if (progress < VideoEditorActivity.this.videoTimelineView.getLeftProgress()) {
                    progress = VideoEditorActivity.this.videoTimelineView.getLeftProgress();
                    VideoEditorActivity.this.videoSeekBarView.setProgress(progress);
                } else if (progress > VideoEditorActivity.this.videoTimelineView.getRightProgress()) {
                    progress = VideoEditorActivity.this.videoTimelineView.getRightProgress();
                    VideoEditorActivity.this.videoSeekBarView.setProgress(progress);
                }
                if (VideoEditorActivity.this.videoPlayer != null && VideoEditorActivity.this.playerPrepared) {
                    try {
                        VideoEditorActivity.this.videoPlayer.seekTo((int) (VideoEditorActivity.this.videoDuration * progress));
                        VideoEditorActivity.this.lastProgress = progress;
                    } catch (Throwable e) {
                        FileLog.e("tmessages", e);
                    }
                }
            }
        });
        frameLayout.addView(this.videoSeekBarView, LayoutHelper.createFrame(-1, 40.0f, 83, 11.0f, 0.0f, 11.0f, 112.0f));
        this.textureView = new TextureView(context);
        this.textureView.setSurfaceTextureListener(new SurfaceTextureListener() {
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (VideoEditorActivity.this.textureView != null && VideoEditorActivity.this.textureView.isAvailable() && VideoEditorActivity.this.videoPlayer != null) {
                    try {
                        VideoEditorActivity.this.videoPlayer.setSurface(new Surface(VideoEditorActivity.this.textureView.getSurfaceTexture()));
                        if (VideoEditorActivity.this.playerPrepared) {
                            VideoEditorActivity.this.videoPlayer.seekTo((int) (VideoEditorActivity.this.videoTimelineView.getLeftProgress() * VideoEditorActivity.this.videoDuration));
                        }
                    } catch (Throwable e) {
                        FileLog.e("tmessages", e);
                    }
                }
            }

            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (VideoEditorActivity.this.videoPlayer != null) {
                    VideoEditorActivity.this.videoPlayer.setDisplay(null);
                }
                return true;
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        frameLayout.addView(this.textureView, LayoutHelper.createFrame(-1, -1.0f, 51, 0.0f, 14.0f, 0.0f, 140.0f));
        this.playButton = new ImageView(context);
        this.playButton.setScaleType(ScaleType.CENTER);
        this.playButton.setImageResource(R.drawable.video_edit_play);
        this.playButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (VideoEditorActivity.this.videoPlayer != null && VideoEditorActivity.this.playerPrepared) {
                    if (VideoEditorActivity.this.videoPlayer.isPlaying()) {
                        VideoEditorActivity.this.videoPlayer.pause();
                        VideoEditorActivity.this.playButton.setImageResource(R.drawable.video_edit_play);
                        return;
                    }
                    try {
                        VideoEditorActivity.this.playButton.setImageDrawable(null);
                        VideoEditorActivity.this.lastProgress = 0.0f;
                        if (VideoEditorActivity.this.needSeek) {
                            VideoEditorActivity.this.videoPlayer.seekTo((int) (VideoEditorActivity.this.videoDuration * VideoEditorActivity.this.videoSeekBarView.getProgress()));
                            VideoEditorActivity.this.needSeek = false;
                        }
                        VideoEditorActivity.this.videoPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
                            public void onSeekComplete(MediaPlayer mp) {
                                float startTime = VideoEditorActivity.this.videoTimelineView.getLeftProgress() * VideoEditorActivity.this.videoDuration;
                                float endTime = VideoEditorActivity.this.videoTimelineView.getRightProgress() * VideoEditorActivity.this.videoDuration;
                                if (startTime == endTime) {
                                    startTime = endTime - 0.01f;
                                }
                                VideoEditorActivity.this.lastProgress = (((float) VideoEditorActivity.this.videoPlayer.getCurrentPosition()) - startTime) / (endTime - startTime);
                                VideoEditorActivity.this.lastProgress = VideoEditorActivity.this.videoTimelineView.getLeftProgress() + (VideoEditorActivity.this.lastProgress * (VideoEditorActivity.this.videoTimelineView.getRightProgress() - VideoEditorActivity.this.videoTimelineView.getLeftProgress()));
                                VideoEditorActivity.this.videoSeekBarView.setProgress(VideoEditorActivity.this.lastProgress);
                            }
                        });
                        VideoEditorActivity.this.videoPlayer.start();
                        synchronized (VideoEditorActivity.this.sync) {
                            if (VideoEditorActivity.this.thread == null) {
                                VideoEditorActivity.this.thread = new Thread(VideoEditorActivity.this.progressRunnable);
                                VideoEditorActivity.this.thread.start();
                            }
                        }
                    } catch (Throwable e) {
                        FileLog.e("tmessages", e);
                    }
                }
            }
        });
        frameLayout.addView(this.playButton, LayoutHelper.createFrame(100, 100.0f, 17, 0.0f, 0.0f, 0.0f, 70.0f));
        if (this.captionEditText != null) {
            this.captionEditText.onDestroy();
        }
        this.captionEditText = new PhotoViewerCaptionEnterView(context, frameLayout, null);
        this.captionEditText.setForceFloatingEmoji(AndroidUtilities.isTablet());
        this.captionEditText.setDelegate(new PhotoViewerCaptionEnterViewDelegate() {
            public void onCaptionEnter() {
                VideoEditorActivity.this.closeCaptionEnter(true);
            }

            public void onTextChanged(CharSequence text) {
                if (VideoEditorActivity.this.mentionsAdapter != null && VideoEditorActivity.this.captionEditText != null && VideoEditorActivity.this.parentChatActivity != null && text != null) {
                    VideoEditorActivity.this.mentionsAdapter.searchUsernameOrHashtag(text.toString(), VideoEditorActivity.this.captionEditText.getCursorPosition(), VideoEditorActivity.this.parentChatActivity.messages);
                }
            }

            public void onWindowSizeChanged(int size) {
                int i;
                int min = Math.min(3, VideoEditorActivity.this.mentionsAdapter.getItemCount()) * 36;
                if (VideoEditorActivity.this.mentionsAdapter.getItemCount() > 3) {
                    i = 18;
                } else {
                    i = 0;
                }
                if (size - (ActionBar.getCurrentActionBarHeight() * 2) < AndroidUtilities.dp((float) (i + min))) {
                    VideoEditorActivity.this.allowMentions = false;
                    if (VideoEditorActivity.this.mentionListView != null && VideoEditorActivity.this.mentionListView.getVisibility() == 0) {
                        VideoEditorActivity.this.mentionListView.setVisibility(4);
                    }
                } else {
                    VideoEditorActivity.this.allowMentions = true;
                    if (VideoEditorActivity.this.mentionListView != null && VideoEditorActivity.this.mentionListView.getVisibility() == 4) {
                        VideoEditorActivity.this.mentionListView.setVisibility(0);
                    }
                }
                VideoEditorActivity.this.fragmentView.requestLayout();
            }
        });
        frameLayout.addView(this.captionEditText, LayoutHelper.createFrame(-1, -2.0f, 83, 0.0f, 0.0f, 0.0f, -400.0f));
        this.captionEditText.onCreate();
        this.mentionListView = new RecyclerListView(context);
        this.mentionListView.setTag(Integer.valueOf(5));
        this.mentionLayoutManager = new LinearLayoutManager(context) {
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        this.mentionLayoutManager.setOrientation(1);
        this.mentionListView.setLayoutManager(this.mentionLayoutManager);
        this.mentionListView.setBackgroundColor(Theme.ACTION_BAR_PHOTO_VIEWER_COLOR);
        this.mentionListView.setVisibility(8);
        this.mentionListView.setClipToPadding(true);
        this.mentionListView.setOverScrollMode(2);
        frameLayout.addView(this.mentionListView, LayoutHelper.createFrame(-1, 110, 83));
        RecyclerListView recyclerListView = this.mentionListView;
        Adapter mentionsAdapter = new MentionsAdapter(context, true, 0, new MentionsAdapterDelegate() {
            public void needChangePanelVisibility(boolean show) {
                if (show) {
                    int i;
                    LayoutParams layoutParams3 = (LayoutParams) VideoEditorActivity.this.mentionListView.getLayoutParams();
                    int min = Math.min(3, VideoEditorActivity.this.mentionsAdapter.getItemCount()) * 36;
                    if (VideoEditorActivity.this.mentionsAdapter.getItemCount() > 3) {
                        i = 18;
                    } else {
                        i = 0;
                    }
                    int height = min + i;
                    layoutParams3.height = AndroidUtilities.dp((float) height);
                    layoutParams3.topMargin = -AndroidUtilities.dp((float) height);
                    VideoEditorActivity.this.mentionListView.setLayoutParams(layoutParams3);
                    if (VideoEditorActivity.this.mentionListAnimation != null) {
                        VideoEditorActivity.this.mentionListAnimation.cancel();
                        VideoEditorActivity.this.mentionListAnimation = null;
                    }
                    if (VideoEditorActivity.this.mentionListView.getVisibility() == 0) {
                        VideoEditorActivity.this.mentionListView.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                        return;
                    }
                    VideoEditorActivity.this.mentionLayoutManager.scrollToPositionWithOffset(0, 10000);
                    if (VideoEditorActivity.this.allowMentions) {
                        VideoEditorActivity.this.mentionListView.setVisibility(0);
                        VideoEditorActivity.this.mentionListAnimation = new AnimatorSet();
                        VideoEditorActivity.this.mentionListAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(VideoEditorActivity.this.mentionListView, "alpha", new float[]{0.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT})});
                        VideoEditorActivity.this.mentionListAnimation.addListener(new AnimatorListenerAdapterProxy() {
                            public void onAnimationEnd(Animator animation) {
                                if (VideoEditorActivity.this.mentionListAnimation != null && VideoEditorActivity.this.mentionListAnimation.equals(animation)) {
                                    VideoEditorActivity.this.mentionListAnimation = null;
                                }
                            }
                        });
                        VideoEditorActivity.this.mentionListAnimation.setDuration(200);
                        VideoEditorActivity.this.mentionListAnimation.start();
                        return;
                    }
                    VideoEditorActivity.this.mentionListView.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    VideoEditorActivity.this.mentionListView.setVisibility(4);
                    return;
                }
                if (VideoEditorActivity.this.mentionListAnimation != null) {
                    VideoEditorActivity.this.mentionListAnimation.cancel();
                    VideoEditorActivity.this.mentionListAnimation = null;
                }
                if (VideoEditorActivity.this.mentionListView.getVisibility() == 8) {
                    return;
                }
                if (VideoEditorActivity.this.allowMentions) {
                    VideoEditorActivity.this.mentionListAnimation = new AnimatorSet();
                    AnimatorSet access$4400 = VideoEditorActivity.this.mentionListAnimation;
                    Animator[] animatorArr = new Animator[1];
                    animatorArr[0] = ObjectAnimator.ofFloat(VideoEditorActivity.this.mentionListView, "alpha", new float[]{0.0f});
                    access$4400.playTogether(animatorArr);
                    VideoEditorActivity.this.mentionListAnimation.addListener(new AnimatorListenerAdapterProxy() {
                        public void onAnimationEnd(Animator animation) {
                            if (VideoEditorActivity.this.mentionListAnimation != null && VideoEditorActivity.this.mentionListAnimation.equals(animation)) {
                                VideoEditorActivity.this.mentionListView.setVisibility(8);
                                VideoEditorActivity.this.mentionListAnimation = null;
                            }
                        }
                    });
                    VideoEditorActivity.this.mentionListAnimation.setDuration(200);
                    VideoEditorActivity.this.mentionListAnimation.start();
                    return;
                }
                VideoEditorActivity.this.mentionListView.setVisibility(8);
            }

            public void onContextSearch(boolean searching) {
            }

            public void onContextClick(BotInlineResult result) {
            }
        });
        this.mentionsAdapter = mentionsAdapter;
        recyclerListView.setAdapter(mentionsAdapter);
        this.mentionsAdapter.setAllowNewMentions(false);
        this.mentionListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(View view, int position) {
                User object = VideoEditorActivity.this.mentionsAdapter.getItem(position);
                int start = VideoEditorActivity.this.mentionsAdapter.getResultStartPosition();
                int len = VideoEditorActivity.this.mentionsAdapter.getResultLength();
                if (object instanceof User) {
                    User user = object;
                    if (user != null) {
                        VideoEditorActivity.this.captionEditText.replaceWithText(start, len, "@" + user.username + " ");
                    }
                } else if (object instanceof String) {
                    VideoEditorActivity.this.captionEditText.replaceWithText(start, len, object + " ");
                }
            }
        });
        this.mentionListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemClick(View view, int position) {
                if (VideoEditorActivity.this.getParentActivity() == null || !(VideoEditorActivity.this.mentionsAdapter.getItem(position) instanceof String)) {
                    return false;
                }
                Builder builder = new Builder(VideoEditorActivity.this.getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        VideoEditorActivity.this.mentionsAdapter.clearRecentHashtags();
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                VideoEditorActivity.this.showDialog(builder.create());
                return true;
            }
        });
        updateVideoInfo();
        updateMuteButton();
        return this.fragmentView;
    }

    public void onPause() {
        super.onPause();
        if (this.captionDoneItem.getVisibility() != 8) {
            closeCaptionEnter(true);
        }
    }

    public void setParentChatActivity(ChatActivity chatActivity) {
        this.parentChatActivity = chatActivity;
    }

    private void closeCaptionEnter(boolean apply) {
        if (apply) {
            this.currentCaption = this.captionEditText.getFieldCharSequence();
        }
        this.actionBar.setSubtitle(this.oldTitle);
        this.captionDoneItem.setVisibility(8);
        this.pickerView.setVisibility(0);
        if (!AndroidUtilities.isTablet()) {
            this.videoSeekBarView.setVisibility(0);
            this.videoTimelineView.setVisibility(0);
        }
        LayoutParams layoutParams = (LayoutParams) this.captionEditText.getLayoutParams();
        layoutParams.bottomMargin = -AndroidUtilities.dp(400.0f);
        this.captionEditText.setLayoutParams(layoutParams);
        layoutParams = (LayoutParams) this.mentionListView.getLayoutParams();
        layoutParams.bottomMargin = -AndroidUtilities.dp(400.0f);
        this.mentionListView.setLayoutParams(layoutParams);
        this.actionBar.setTitle(this.muteVideo ? LocaleController.getString("AttachGif", R.string.AttachGif) : LocaleController.getString("AttachVideo", R.string.AttachVideo));
        this.captionItem.setImageResource(TextUtils.isEmpty(this.currentCaption) ? R.drawable.photo_text : R.drawable.photo_text2);
        if (this.captionEditText.isPopupShowing()) {
            this.captionEditText.hidePopup();
        }
        this.captionEditText.closeKeyboard();
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.closeChats) {
            removeSelfFromStack();
        }
    }

    public void updateMuteButton() {
        if (this.videoPlayer != null) {
            float volume = this.muteVideo ? 0.0f : DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
            if (this.videoPlayer != null) {
                this.videoPlayer.setVolume(volume, volume);
            }
        }
        if (this.muteVideo) {
            this.actionBar.setTitle(LocaleController.getString("AttachGif", R.string.AttachGif));
            this.muteItem.setImageResource(R.drawable.volume_off);
            if (this.captionItem.getVisibility() == 0) {
                this.needCompressVideo = true;
                this.compressItem.setImageResource(R.drawable.hd_off);
                this.compressItem.setClickable(false);
                this.compressItem.setAlpha(DefaultLoadControl.DEFAULT_HIGH_BUFFER_LOAD);
                this.compressItem.setEnabled(false);
                return;
            }
            return;
        }
        this.actionBar.setTitle(LocaleController.getString("AttachVideo", R.string.AttachVideo));
        this.muteItem.setImageResource(R.drawable.volume_on);
        if (this.captionItem.getVisibility() == 0) {
            this.compressItem.setClickable(true);
            this.compressItem.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            this.compressItem.setEnabled(true);
        }
    }

    private void onPlayComplete() {
        if (this.playButton != null) {
            this.playButton.setImageResource(R.drawable.video_edit_play);
        }
        if (!(this.videoSeekBarView == null || this.videoTimelineView == null)) {
            this.videoSeekBarView.setProgress(this.videoTimelineView.getLeftProgress());
        }
        try {
            if (this.videoPlayer != null && this.videoTimelineView != null) {
                this.videoPlayer.seekTo((int) (this.videoTimelineView.getLeftProgress() * this.videoDuration));
            }
        } catch (Throwable e) {
            FileLog.e("tmessages", e);
        }
    }

    private void updateVideoInfo() {
        if (this.actionBar != null) {
            int width;
            int height;
            this.esimatedDuration = (long) Math.ceil((double) ((this.videoTimelineView.getRightProgress() - this.videoTimelineView.getLeftProgress()) * this.videoDuration));
            if (this.compressItem.getVisibility() == 8 || (this.compressItem.getVisibility() == 0 && !this.needCompressVideo)) {
                width = (this.rotationValue == 90 || this.rotationValue == 270) ? this.originalHeight : this.originalWidth;
                height = (this.rotationValue == 90 || this.rotationValue == 270) ? this.originalWidth : this.originalHeight;
                this.estimatedSize = (int) (((float) this.originalSize) * (((float) this.esimatedDuration) / this.videoDuration));
            } else {
                width = (this.rotationValue == 90 || this.rotationValue == 270) ? this.resultHeight : this.resultWidth;
                height = (this.rotationValue == 90 || this.rotationValue == 270) ? this.resultWidth : this.resultHeight;
                this.estimatedSize = (int) (((float) (this.audioFramesSize + this.videoFramesSize)) * (((float) this.esimatedDuration) / this.videoDuration));
                this.estimatedSize += (this.estimatedSize / 32768) * 16;
            }
            if (this.videoTimelineView.getLeftProgress() == 0.0f) {
                this.startTime = -1;
            } else {
                this.startTime = ((long) (this.videoTimelineView.getLeftProgress() * this.videoDuration)) * 1000;
            }
            if (this.videoTimelineView.getRightProgress() == DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
                this.endTime = -1;
            } else {
                this.endTime = ((long) (this.videoTimelineView.getRightProgress() * this.videoDuration)) * 1000;
            }
            String videoDimension = String.format("%dx%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
            int seconds = ((int) Math.ceil((double) (this.esimatedDuration / 1000))) - (((int) ((this.esimatedDuration / 1000) / 60)) * 60);
            String videoTimeSize = String.format("%d:%02d, ~%s", new Object[]{Integer.valueOf((int) ((this.esimatedDuration / 1000) / 60)), Integer.valueOf(seconds), AndroidUtilities.formatFileSize((long) this.estimatedSize)});
            this.actionBar.setSubtitle(String.format("%s, %s", new Object[]{videoDimension, videoTimeSize}));
        }
    }

    public void setDelegate(VideoEditorActivityDelegate videoEditorActivityDelegate) {
        this.delegate = videoEditorActivityDelegate;
    }

    private boolean processOpenVideo() {
        try {
            this.originalSize = new File(this.videoPath).length();
            Container isoFile = new IsoFile(this.videoPath);
            List<Box> boxes = Path.getPaths(isoFile, "/moov/trak/");
            TrackHeaderBox trackHeaderBox = null;
            boolean isAvc = true;
            boolean isMp4A = true;
            if (Path.getPath(isoFile, "/moov/trak/mdia/minf/stbl/stsd/mp4a/") == null) {
                isMp4A = false;
            }
            if (!isMp4A) {
                return false;
            }
            int i;
            if (Path.getPath(isoFile, "/moov/trak/mdia/minf/stbl/stsd/avc1/") == null) {
                isAvc = false;
            }
            for (Box box : boxes) {
                TrackBox trackBox = (TrackBox) box;
                long sampleSizes = 0;
                long trackBitrate = 0;
                try {
                    MediaBox mediaBox = trackBox.getMediaBox();
                    MediaHeaderBox mediaHeaderBox = mediaBox.getMediaHeaderBox();
                    for (long size : mediaBox.getMediaInformationBox().getSampleTableBox().getSampleSizeBox().getSampleSizes()) {
                        sampleSizes += size;
                    }
                    this.videoDuration = ((float) mediaHeaderBox.getDuration()) / ((float) mediaHeaderBox.getTimescale());
                    trackBitrate = (long) ((int) (((float) (8 * sampleSizes)) / this.videoDuration));
                } catch (Throwable e) {
                    FileLog.e("tmessages", e);
                }
                TrackHeaderBox headerBox = trackBox.getTrackHeaderBox();
                if (headerBox.getWidth() == 0.0d || headerBox.getHeight() == 0.0d) {
                    this.audioFramesSize += sampleSizes;
                } else {
                    trackHeaderBox = headerBox;
                    i = (int) ((trackBitrate / 100000) * 100000);
                    this.bitrate = i;
                    this.originalBitrate = i;
                    if (this.bitrate > 900000) {
                        this.bitrate = 900000;
                    }
                    this.videoFramesSize += sampleSizes;
                }
            }
            if (trackHeaderBox == null) {
                return false;
            }
            Matrix matrix = trackHeaderBox.getMatrix();
            if (matrix.equals(Matrix.ROTATE_90)) {
                this.rotationValue = 90;
            } else if (matrix.equals(Matrix.ROTATE_180)) {
                this.rotationValue = 180;
            } else if (matrix.equals(Matrix.ROTATE_270)) {
                this.rotationValue = 270;
            }
            i = (int) trackHeaderBox.getWidth();
            this.originalWidth = i;
            this.resultWidth = i;
            i = (int) trackHeaderBox.getHeight();
            this.originalHeight = i;
            this.resultHeight = i;
            if (this.resultWidth > 640 || this.resultHeight > 640) {
                float scale;
                if (this.resultWidth > this.resultHeight) {
                    scale = 640.0f / ((float) this.resultWidth);
                } else {
                    scale = 640.0f / ((float) this.resultHeight);
                }
                this.resultWidth = (int) (((float) this.resultWidth) * scale);
                this.resultHeight = (int) (((float) this.resultHeight) * scale);
                if (this.bitrate != 0) {
                    this.bitrate = (int) (((float) this.bitrate) * Math.max(0.5f, scale));
                    this.videoFramesSize = (long) (((float) (this.bitrate / 8)) * this.videoDuration);
                }
            }
            if (!isAvc && (this.resultWidth == this.originalWidth || this.resultHeight == this.originalHeight)) {
                return false;
            }
            this.videoDuration *= 1000.0f;
            updateVideoInfo();
            return true;
        } catch (Throwable e2) {
            FileLog.e("tmessages", e2);
            return false;
        }
    }
}
