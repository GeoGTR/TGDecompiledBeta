package org.telegram.messenger;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.messenger.video.MediaCodecVideoConvertor;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$BotInlineResult;
import org.telegram.tgnet.TLRPC$Document;
import org.telegram.tgnet.TLRPC$DocumentAttribute;
import org.telegram.tgnet.TLRPC$EncryptedChat;
import org.telegram.tgnet.TLRPC$InputDocument;
import org.telegram.tgnet.TLRPC$Message;
import org.telegram.tgnet.TLRPC$MessageEntity;
import org.telegram.tgnet.TLRPC$Photo;
import org.telegram.tgnet.TLRPC$PhotoSize;
import org.telegram.tgnet.TLRPC$ReplyMarkup;
import org.telegram.tgnet.TLRPC$TL_document;
import org.telegram.tgnet.TLRPC$TL_documentAttributeAnimated;
import org.telegram.tgnet.TLRPC$TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC$TL_encryptedChat;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_messages_messages;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.tgnet.TLRPC$messages_Messages;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.FiltersView;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.EmbedBottomSheet;
import org.telegram.ui.Components.PhotoFilterView;
import org.telegram.ui.Components.PipRoundVideoView;
import org.telegram.ui.Components.Point;
import org.telegram.ui.Components.VideoPlayer;
import org.telegram.ui.PhotoViewer;

public class MediaController implements AudioManager.OnAudioFocusChangeListener, NotificationCenter.NotificationCenterDelegate, SensorEventListener {
    private static final int AUDIO_FOCUSED = 2;
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    public static final String AUIDO_MIME_TYPE = "audio/mp4a-latm";
    private static volatile MediaController Instance = null;
    public static final int VIDEO_BITRATE_1080 = 6800000;
    public static final int VIDEO_BITRATE_360 = 750000;
    public static final int VIDEO_BITRATE_480 = 1000000;
    public static final int VIDEO_BITRATE_720 = 2621440;
    public static final String VIDEO_MIME_TYPE = "video/avc";
    private static final float VOLUME_DUCK = 0.2f;
    private static final float VOLUME_NORMAL = 1.0f;
    public static AlbumEntry allMediaAlbumEntry;
    public static ArrayList<AlbumEntry> allMediaAlbums = new ArrayList<>();
    public static ArrayList<AlbumEntry> allPhotoAlbums = new ArrayList<>();
    public static AlbumEntry allPhotosAlbumEntry;
    public static AlbumEntry allVideosAlbumEntry;
    private static Runnable broadcastPhotosRunnable;
    private static final String[] projectionPhotos;
    private static final String[] projectionVideo;
    /* access modifiers changed from: private */
    public static Runnable refreshGalleryRunnable;
    private Sensor accelerometerSensor;
    private boolean accelerometerVertical;
    private boolean allowStartRecord;
    private int audioFocus = 0;
    private AudioInfo audioInfo;
    /* access modifiers changed from: private */
    public VideoPlayer audioPlayer = null;
    AudioManager.OnAudioFocusChangeListener audioRecordFocusChangedListener = new AudioManager.OnAudioFocusChangeListener() {
        public final void onAudioFocusChange(int i) {
            MediaController.this.lambda$new$0$MediaController(i);
        }
    };
    /* access modifiers changed from: private */
    public AudioRecord audioRecorder;
    /* access modifiers changed from: private */
    public float audioVolume;
    private ValueAnimator audioVolumeAnimator;
    private final ValueAnimator.AnimatorUpdateListener audioVolumeUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float unused = MediaController.this.audioVolume = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            MediaController.this.setPlayerVolume();
        }
    };
    /* access modifiers changed from: private */
    public Activity baseActivity;
    /* access modifiers changed from: private */
    public boolean callInProgress;
    private int countLess;
    /* access modifiers changed from: private */
    public AspectRatioFrameLayout currentAspectRatioFrameLayout;
    /* access modifiers changed from: private */
    public float currentAspectRatioFrameLayoutRatio;
    private boolean currentAspectRatioFrameLayoutReady;
    /* access modifiers changed from: private */
    public int currentAspectRatioFrameLayoutRotation;
    private float currentMusicPlaybackSpeed = 1.0f;
    private float currentPlaybackSpeed = 1.0f;
    private int currentPlaylistNum;
    /* access modifiers changed from: private */
    public TextureView currentTextureView;
    /* access modifiers changed from: private */
    public FrameLayout currentTextureViewContainer;
    private boolean downloadingCurrentMessage;
    /* access modifiers changed from: private */
    public VideoPlayer emojiSoundPlayer = null;
    /* access modifiers changed from: private */
    public int emojiSoundPlayerNum = 0;
    /* access modifiers changed from: private */
    public ExternalObserver externalObserver;
    private View feedbackView;
    /* access modifiers changed from: private */
    public ByteBuffer fileBuffer;
    /* access modifiers changed from: private */
    public DispatchQueue fileEncodingQueue;
    private BaseFragment flagSecureFragment;
    private boolean forceLoopCurrentPlaylist;
    private HashMap<String, MessageObject> generatingWaveform = new HashMap<>();
    private MessageObject goingToShowMessageObject;
    private float[] gravity = new float[3];
    private float[] gravityFast = new float[3];
    private Sensor gravitySensor;
    private int hasAudioFocus;
    private boolean hasRecordAudioFocus;
    private boolean ignoreOnPause;
    private boolean ignoreProximity;
    private boolean inputFieldHasText;
    /* access modifiers changed from: private */
    public InternalObserver internalObserver;
    /* access modifiers changed from: private */
    public boolean isDrawingWasReady;
    /* access modifiers changed from: private */
    public boolean isPaused = false;
    private boolean isStreamingCurrentAudio;
    private int lastChatAccount;
    private long lastChatEnterTime;
    private long lastChatLeaveTime;
    private ArrayList<Long> lastChatVisibleMessages;
    private long lastMediaCheckTime;
    private int lastMessageId;
    /* access modifiers changed from: private */
    public long lastProgress = 0;
    private float lastProximityValue = -100.0f;
    /* access modifiers changed from: private */
    public long lastSaveTime;
    private TLRPC$EncryptedChat lastSecretChat;
    private long lastTimestamp = 0;
    private TLRPC$User lastUser;
    private float[] linearAcceleration = new float[3];
    private Sensor linearSensor;
    private boolean loadingPlaylist;
    private String[] mediaProjections;
    /* access modifiers changed from: private */
    public PipRoundVideoView pipRoundVideoView;
    /* access modifiers changed from: private */
    public int pipSwitchingState;
    private boolean playMusicAgain;
    /* access modifiers changed from: private */
    public int playerNum;
    private boolean playerWasReady;
    /* access modifiers changed from: private */
    public MessageObject playingMessageObject;
    /* access modifiers changed from: private */
    public ArrayList<MessageObject> playlist = new ArrayList<>();
    private int playlistClassGuid;
    private boolean[] playlistEndReached = {false, false};
    private PlaylistGlobalSearchParams playlistGlobalSearchParams;
    private HashMap<Integer, MessageObject> playlistMap = new HashMap<>();
    private int[] playlistMaxId = {Integer.MAX_VALUE, Integer.MAX_VALUE};
    private long playlistMergeDialogId;
    private float previousAccValue;
    private Timer progressTimer = null;
    private final Object progressTimerSync = new Object();
    private boolean proximityHasDifferentValues;
    private Sensor proximitySensor;
    private boolean proximityTouched;
    private PowerManager.WakeLock proximityWakeLock;
    private ChatActivity raiseChat;
    private boolean raiseToEarRecord;
    private int raisedToBack;
    private int raisedToTop;
    private int raisedToTopSign;
    public int recordBufferSize = 1280;
    /* access modifiers changed from: private */
    public ArrayList<ByteBuffer> recordBuffers = new ArrayList<>();
    private long recordDialogId;
    /* access modifiers changed from: private */
    public DispatchQueue recordQueue;
    private MessageObject recordReplyingMsg;
    private MessageObject recordReplyingTopMsg;
    /* access modifiers changed from: private */
    public Runnable recordRunnable = new Runnable() {
        /* JADX WARNING: Removed duplicated region for block: B:36:0x00d5 A[SYNTHETIC, Splitter:B:36:0x00d5] */
        /* JADX WARNING: Removed duplicated region for block: B:49:0x0115  */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x0118  */
        /* JADX WARNING: Removed duplicated region for block: B:59:0x00e9 A[SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r18 = this;
                r1 = r18
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this
                android.media.AudioRecord r0 = r0.audioRecorder
                if (r0 == 0) goto L_0x0165
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this
                java.util.ArrayList r0 = r0.recordBuffers
                boolean r0 = r0.isEmpty()
                r2 = 0
                if (r0 != 0) goto L_0x002d
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this
                java.util.ArrayList r0 = r0.recordBuffers
                java.lang.Object r0 = r0.get(r2)
                java.nio.ByteBuffer r0 = (java.nio.ByteBuffer) r0
                org.telegram.messenger.MediaController r3 = org.telegram.messenger.MediaController.this
                java.util.ArrayList r3 = r3.recordBuffers
                r3.remove(r2)
                goto L_0x003c
            L_0x002d:
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this
                int r0 = r0.recordBufferSize
                java.nio.ByteBuffer r0 = java.nio.ByteBuffer.allocateDirect(r0)
                java.nio.ByteOrder r3 = java.nio.ByteOrder.nativeOrder()
                r0.order(r3)
            L_0x003c:
                r3 = r0
                r3.rewind()
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this
                android.media.AudioRecord r0 = r0.audioRecorder
                int r4 = r3.capacity()
                int r4 = r0.read(r3, r4)
                if (r4 <= 0) goto L_0x013e
                r3.limit(r4)
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this     // Catch:{ Exception -> 0x00f6 }
                long r7 = r0.samplesCount     // Catch:{ Exception -> 0x00f6 }
                int r0 = r4 / 2
                long r9 = (long) r0     // Catch:{ Exception -> 0x00f6 }
                long r7 = r7 + r9
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this     // Catch:{ Exception -> 0x00f6 }
                long r9 = r0.samplesCount     // Catch:{ Exception -> 0x00f6 }
                double r9 = (double) r9
                double r11 = (double) r7
                java.lang.Double.isNaN(r9)
                java.lang.Double.isNaN(r11)
                double r9 = r9 / r11
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this     // Catch:{ Exception -> 0x00f6 }
                short[] r0 = r0.recordSamples     // Catch:{ Exception -> 0x00f6 }
                int r0 = r0.length     // Catch:{ Exception -> 0x00f6 }
                double r11 = (double) r0
                java.lang.Double.isNaN(r11)
                double r9 = r9 * r11
                int r0 = (int) r9
                org.telegram.messenger.MediaController r9 = org.telegram.messenger.MediaController.this     // Catch:{ Exception -> 0x00f6 }
                short[] r9 = r9.recordSamples     // Catch:{ Exception -> 0x00f6 }
                int r9 = r9.length     // Catch:{ Exception -> 0x00f6 }
                int r9 = r9 - r0
                r10 = 0
                if (r0 == 0) goto L_0x00a8
                org.telegram.messenger.MediaController r11 = org.telegram.messenger.MediaController.this     // Catch:{ Exception -> 0x00f6 }
                short[] r11 = r11.recordSamples     // Catch:{ Exception -> 0x00f6 }
                int r11 = r11.length     // Catch:{ Exception -> 0x00f6 }
                float r11 = (float) r11     // Catch:{ Exception -> 0x00f6 }
                float r12 = (float) r0     // Catch:{ Exception -> 0x00f6 }
                float r11 = r11 / r12
                r12 = 0
                r13 = 0
            L_0x0091:
                if (r12 >= r0) goto L_0x00a8
                org.telegram.messenger.MediaController r14 = org.telegram.messenger.MediaController.this     // Catch:{ Exception -> 0x00f6 }
                short[] r14 = r14.recordSamples     // Catch:{ Exception -> 0x00f6 }
                org.telegram.messenger.MediaController r15 = org.telegram.messenger.MediaController.this     // Catch:{ Exception -> 0x00f6 }
                short[] r15 = r15.recordSamples     // Catch:{ Exception -> 0x00f6 }
                int r5 = (int) r13     // Catch:{ Exception -> 0x00f6 }
                short r5 = r15[r5]     // Catch:{ Exception -> 0x00f6 }
                r14[r12] = r5     // Catch:{ Exception -> 0x00f6 }
                float r13 = r13 + r11
                int r12 = r12 + 1
                goto L_0x0091
            L_0x00a8:
                float r5 = (float) r4
                r6 = 1073741824(0x40000000, float:2.0)
                float r5 = r5 / r6
                float r6 = (float) r9
                float r5 = r5 / r6
                r6 = 0
                r16 = 0
            L_0x00b1:
                int r9 = r4 / 2
                if (r6 >= r9) goto L_0x00ec
                short r9 = r3.getShort()     // Catch:{ Exception -> 0x00f2 }
                int r11 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x00f2 }
                r12 = 21
                if (r11 >= r12) goto L_0x00ca
                r11 = 2500(0x9c4, float:3.503E-42)
                if (r9 <= r11) goto L_0x00d2
                int r11 = r9 * r9
                double r11 = (double) r11
                java.lang.Double.isNaN(r11)
                goto L_0x00d0
            L_0x00ca:
                int r11 = r9 * r9
                double r11 = (double) r11
                java.lang.Double.isNaN(r11)
            L_0x00d0:
                double r16 = r16 + r11
            L_0x00d2:
                int r11 = (int) r10
                if (r6 != r11) goto L_0x00e9
                org.telegram.messenger.MediaController r11 = org.telegram.messenger.MediaController.this     // Catch:{ Exception -> 0x00f2 }
                short[] r11 = r11.recordSamples     // Catch:{ Exception -> 0x00f2 }
                int r11 = r11.length     // Catch:{ Exception -> 0x00f2 }
                if (r0 >= r11) goto L_0x00e9
                org.telegram.messenger.MediaController r11 = org.telegram.messenger.MediaController.this     // Catch:{ Exception -> 0x00f2 }
                short[] r11 = r11.recordSamples     // Catch:{ Exception -> 0x00f2 }
                r11[r0] = r9     // Catch:{ Exception -> 0x00f2 }
                float r10 = r10 + r5
                int r0 = r0 + 1
            L_0x00e9:
                int r6 = r6 + 1
                goto L_0x00b1
            L_0x00ec:
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this     // Catch:{ Exception -> 0x00f2 }
                long unused = r0.samplesCount = r7     // Catch:{ Exception -> 0x00f2 }
                goto L_0x00fe
            L_0x00f2:
                r0 = move-exception
                r5 = r16
                goto L_0x00f9
            L_0x00f6:
                r0 = move-exception
                r5 = 0
            L_0x00f9:
                org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
                r16 = r5
            L_0x00fe:
                r3.position(r2)
                double r5 = (double) r4
                java.lang.Double.isNaN(r5)
                double r16 = r16 / r5
                r5 = 4611686018427387904(0xNUM, double:2.0)
                double r16 = r16 / r5
                double r5 = java.lang.Math.sqrt(r16)
                int r0 = r3.capacity()
                if (r4 == r0) goto L_0x0116
                r2 = 1
            L_0x0116:
                if (r4 == 0) goto L_0x0126
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this
                org.telegram.messenger.DispatchQueue r0 = r0.fileEncodingQueue
                org.telegram.messenger.-$$Lambda$MediaController$2$KFezl721_K6mLwIJkJIfLv3NCqc r4 = new org.telegram.messenger.-$$Lambda$MediaController$2$KFezl721_K6mLwIJkJIfLv3NCqc
                r4.<init>(r3, r2)
                r0.postRunnable(r4)
            L_0x0126:
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this
                org.telegram.messenger.DispatchQueue r0 = r0.recordQueue
                org.telegram.messenger.MediaController r2 = org.telegram.messenger.MediaController.this
                java.lang.Runnable r2 = r2.recordRunnable
                r0.postRunnable(r2)
                org.telegram.messenger.-$$Lambda$MediaController$2$ZmxoYb44RB2_mvL9ZAZBxtwyRAY r0 = new org.telegram.messenger.-$$Lambda$MediaController$2$ZmxoYb44RB2_mvL9ZAZBxtwyRAY
                r0.<init>(r5)
                org.telegram.messenger.AndroidUtilities.runOnUIThread(r0)
                goto L_0x0165
            L_0x013e:
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this
                java.util.ArrayList r0 = r0.recordBuffers
                r0.add(r3)
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this
                int r0 = r0.sendAfterDone
                r2 = 3
                if (r0 == r2) goto L_0x0165
                org.telegram.messenger.MediaController r0 = org.telegram.messenger.MediaController.this
                int r2 = r0.sendAfterDone
                org.telegram.messenger.MediaController r3 = org.telegram.messenger.MediaController.this
                boolean r3 = r3.sendAfterDoneNotify
                org.telegram.messenger.MediaController r4 = org.telegram.messenger.MediaController.this
                int r4 = r4.sendAfterDoneScheduleDate
                r0.stopRecordingInternal(r2, r3, r4)
            L_0x0165:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.AnonymousClass2.run():void");
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$run$1 */
        public /* synthetic */ void lambda$run$1$MediaController$2(ByteBuffer byteBuffer, boolean z) {
            int i;
            while (byteBuffer.hasRemaining()) {
                if (byteBuffer.remaining() > MediaController.this.fileBuffer.remaining()) {
                    i = byteBuffer.limit();
                    byteBuffer.limit(MediaController.this.fileBuffer.remaining() + byteBuffer.position());
                } else {
                    i = -1;
                }
                MediaController.this.fileBuffer.put(byteBuffer);
                if (MediaController.this.fileBuffer.position() == MediaController.this.fileBuffer.limit() || z) {
                    MediaController mediaController = MediaController.this;
                    if (mediaController.writeFrame(mediaController.fileBuffer, !z ? MediaController.this.fileBuffer.limit() : byteBuffer.position()) != 0) {
                        MediaController.this.fileBuffer.rewind();
                        MediaController mediaController2 = MediaController.this;
                        long unused = mediaController2.recordTimeCount = mediaController2.recordTimeCount + ((long) ((MediaController.this.fileBuffer.limit() / 2) / (MediaController.this.sampleRate / 1000)));
                    }
                }
                if (i != -1) {
                    byteBuffer.limit(i);
                }
            }
            MediaController.this.recordQueue.postRunnable(new Runnable(byteBuffer) {
                public final /* synthetic */ ByteBuffer f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MediaController.AnonymousClass2.this.lambda$null$0$MediaController$2(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$null$0 */
        public /* synthetic */ void lambda$null$0$MediaController$2(ByteBuffer byteBuffer) {
            MediaController.this.recordBuffers.add(byteBuffer);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$run$2 */
        public /* synthetic */ void lambda$run$2$MediaController$2(double d) {
            NotificationCenter.getInstance(MediaController.this.recordingCurrentAccount).postNotificationName(NotificationCenter.recordProgressChanged, Integer.valueOf(MediaController.this.recordingGuid), Double.valueOf(d));
        }
    };
    /* access modifiers changed from: private */
    public short[] recordSamples = new short[1024];
    /* access modifiers changed from: private */
    public Runnable recordStartRunnable;
    private long recordStartTime;
    /* access modifiers changed from: private */
    public long recordTimeCount;
    /* access modifiers changed from: private */
    public TLRPC$TL_document recordingAudio;
    private File recordingAudioFile;
    /* access modifiers changed from: private */
    public int recordingCurrentAccount;
    /* access modifiers changed from: private */
    public int recordingGuid = -1;
    private boolean resumeAudioOnFocusGain;
    public int sampleRate = 16000;
    /* access modifiers changed from: private */
    public long samplesCount;
    /* access modifiers changed from: private */
    public float seekToProgressPending;
    /* access modifiers changed from: private */
    public int sendAfterDone;
    /* access modifiers changed from: private */
    public boolean sendAfterDoneNotify;
    /* access modifiers changed from: private */
    public int sendAfterDoneScheduleDate;
    private SensorManager sensorManager;
    private boolean sensorsStarted;
    private Runnable setLoadingRunnable = new Runnable() {
        public void run() {
            if (MediaController.this.playingMessageObject != null) {
                FileLoader.getInstance(MediaController.this.playingMessageObject.currentAccount).setLoadingVideo(MediaController.this.playingMessageObject.getDocument(), true, false);
            }
        }
    };
    /* access modifiers changed from: private */
    public String shouldSavePositionForCurrentAudio;
    private ArrayList<MessageObject> shuffledPlaylist = new ArrayList<>();
    /* access modifiers changed from: private */
    public int startObserverToken;
    private StopMediaObserverRunnable stopMediaObserverRunnable;
    /* access modifiers changed from: private */
    public final Object sync = new Object();
    private long timeSinceRaise;
    private boolean useFrontSpeaker;
    private ArrayList<VideoConvertMessage> videoConvertQueue = new ArrayList<>();
    private final Object videoConvertSync = new Object();
    /* access modifiers changed from: private */
    public VideoPlayer videoPlayer;
    private final Object videoQueueSync = new Object();
    private ArrayList<MessageObject> voiceMessagesPlaylist;
    private SparseArray<MessageObject> voiceMessagesPlaylistMap;
    private boolean voiceMessagesPlaylistUnread;

    public static class AudioEntry {
        public String author;
        public int duration;
        public String genre;
        public long id;
        public MessageObject messageObject;
        public String path;
        public String title;
    }

    public static class CropState {
        public float cropPh = 1.0f;
        public float cropPw = 1.0f;
        public float cropPx;
        public float cropPy;
        public float cropRotate;
        public float cropScale = 1.0f;
        public boolean freeform;
        public int height;
        public boolean initied;
        public float lockedAspectRatio;
        public Matrix matrix;
        public boolean mirrored;
        public float scale;
        public float stateScale;
        public int transformHeight;
        public int transformRotation;
        public int transformWidth;
        public int width;
    }

    public static class SavedFilterState {
        public float blurAngle;
        public float blurExcludeBlurSize;
        public Point blurExcludePoint;
        public float blurExcludeSize;
        public int blurType;
        public float contrastValue;
        public PhotoFilterView.CurvesToolValue curvesToolValue = new PhotoFilterView.CurvesToolValue();
        public float enhanceValue;
        public float exposureValue;
        public float fadeValue;
        public float grainValue;
        public float highlightsValue;
        public float saturationValue;
        public float shadowsValue;
        public float sharpenValue;
        public float softenSkinValue;
        public int tintHighlightsColor;
        public int tintShadowsColor;
        public float vignetteValue;
        public float warmthValue;
    }

    public interface VideoConvertorListener {
        boolean checkConversionCanceled();

        void didWriteData(long j, float f);
    }

    private static int getVideoBitrateWithFactor(float f) {
        return (int) (f * 2000.0f * 1000.0f * 1.13f);
    }

    public static native int isOpusFile(String str);

    private static boolean isRecognizedFormat(int i) {
        if (i == 39 || i == NUM) {
            return true;
        }
        switch (i) {
            case 19:
            case 20:
            case 21:
                return true;
            default:
                return false;
        }
    }

    private native int startRecord(String str, int i);

    private native void stopRecord();

    /* access modifiers changed from: private */
    public native int writeFrame(ByteBuffer byteBuffer, int i);

    public native byte[] getWaveform(String str);

    public native byte[] getWaveform2(short[] sArr, int i);

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public boolean isBuffering() {
        VideoPlayer videoPlayer2 = this.audioPlayer;
        if (videoPlayer2 != null) {
            return videoPlayer2.isBuffering();
        }
        return false;
    }

    private static class AudioBuffer {
        ByteBuffer buffer;
        byte[] bufferBytes;
        int finished;
        long pcmOffset;
        int size;

        public AudioBuffer(int i) {
            this.buffer = ByteBuffer.allocateDirect(i);
            this.bufferBytes = new byte[i];
        }
    }

    static {
        String[] strArr = new String[9];
        strArr[0] = "_id";
        strArr[1] = "bucket_id";
        strArr[2] = "bucket_display_name";
        strArr[3] = "_data";
        int i = Build.VERSION.SDK_INT;
        String str = "date_modified";
        strArr[4] = i > 28 ? str : "datetaken";
        strArr[5] = "orientation";
        strArr[6] = "width";
        strArr[7] = "height";
        strArr[8] = "_size";
        projectionPhotos = strArr;
        String[] strArr2 = new String[9];
        strArr2[0] = "_id";
        strArr2[1] = "bucket_id";
        strArr2[2] = "bucket_display_name";
        strArr2[3] = "_data";
        if (i <= 28) {
            str = "datetaken";
        }
        strArr2[4] = str;
        strArr2[5] = "duration";
        strArr2[6] = "width";
        strArr2[7] = "height";
        strArr2[8] = "_size";
        projectionVideo = strArr2;
    }

    public static class AlbumEntry {
        public int bucketId;
        public String bucketName;
        public PhotoEntry coverPhoto;
        public ArrayList<PhotoEntry> photos = new ArrayList<>();
        public SparseArray<PhotoEntry> photosByIds = new SparseArray<>();
        public boolean videoOnly;

        public AlbumEntry(int i, String str, PhotoEntry photoEntry) {
            this.bucketId = i;
            this.bucketName = str;
            this.coverPhoto = photoEntry;
        }

        public void addPhoto(PhotoEntry photoEntry) {
            this.photos.add(photoEntry);
            this.photosByIds.put(photoEntry.imageId, photoEntry);
        }
    }

    public static class MediaEditState {
        public long averageDuration;
        public CharSequence caption;
        public CropState cropState;
        public ArrayList<VideoEditedInfo.MediaEntity> croppedMediaEntities;
        public String croppedPaintPath;
        public VideoEditedInfo editedInfo;
        public ArrayList<TLRPC$MessageEntity> entities;
        public String filterPath;
        public String fullPaintPath;
        public String imagePath;
        public boolean isCropped;
        public boolean isFiltered;
        public boolean isPainted;
        public ArrayList<VideoEditedInfo.MediaEntity> mediaEntities;
        public String paintPath;
        public SavedFilterState savedFilterState;
        public ArrayList<TLRPC$InputDocument> stickers;
        public String thumbPath;
        public int ttl;

        public String getPath() {
            return null;
        }

        public void reset() {
            this.caption = null;
            this.thumbPath = null;
            this.filterPath = null;
            this.imagePath = null;
            this.paintPath = null;
            this.croppedPaintPath = null;
            this.isFiltered = false;
            this.isPainted = false;
            this.isCropped = false;
            this.ttl = 0;
            this.mediaEntities = null;
            this.editedInfo = null;
            this.entities = null;
            this.savedFilterState = null;
            this.stickers = null;
            this.cropState = null;
        }

        public void copyFrom(MediaEditState mediaEditState) {
            this.caption = mediaEditState.caption;
            this.thumbPath = mediaEditState.thumbPath;
            this.imagePath = mediaEditState.imagePath;
            this.filterPath = mediaEditState.filterPath;
            this.paintPath = mediaEditState.paintPath;
            this.croppedPaintPath = mediaEditState.croppedPaintPath;
            this.fullPaintPath = mediaEditState.fullPaintPath;
            this.entities = mediaEditState.entities;
            this.savedFilterState = mediaEditState.savedFilterState;
            this.mediaEntities = mediaEditState.mediaEntities;
            this.croppedMediaEntities = mediaEditState.croppedMediaEntities;
            this.stickers = mediaEditState.stickers;
            this.editedInfo = mediaEditState.editedInfo;
            this.averageDuration = mediaEditState.averageDuration;
            this.isFiltered = mediaEditState.isFiltered;
            this.isPainted = mediaEditState.isPainted;
            this.isCropped = mediaEditState.isCropped;
            this.ttl = mediaEditState.ttl;
            this.cropState = mediaEditState.cropState;
        }
    }

    public static class PhotoEntry extends MediaEditState {
        public int bucketId;
        public boolean canDeleteAfter;
        public long dateTaken;
        public int duration;
        public int height;
        public int imageId;
        public boolean isMuted;
        public boolean isVideo;
        public int orientation;
        public String path;
        public long size;
        public int width;

        public PhotoEntry(int i, int i2, long j, String str, int i3, boolean z, int i4, int i5, long j2) {
            this.bucketId = i;
            this.imageId = i2;
            this.dateTaken = j;
            this.path = str;
            this.width = i4;
            this.height = i5;
            this.size = j2;
            if (z) {
                this.duration = i3;
            } else {
                this.orientation = i3;
            }
            this.isVideo = z;
        }

        public String getPath() {
            return this.path;
        }

        public void reset() {
            if (this.isVideo && this.filterPath != null) {
                new File(this.filterPath).delete();
                this.filterPath = null;
            }
            super.reset();
        }
    }

    public static class SearchImage extends MediaEditState {
        public CharSequence caption;
        public int date;
        public TLRPC$Document document;
        public int height;
        public String id;
        public String imageUrl;
        public TLRPC$BotInlineResult inlineResult;
        public HashMap<String, String> params;
        public TLRPC$Photo photo;
        public TLRPC$PhotoSize photoSize;
        public int size;
        public TLRPC$PhotoSize thumbPhotoSize;
        public String thumbUrl;
        public int type;
        public int width;

        public String getPath() {
            TLRPC$PhotoSize tLRPC$PhotoSize = this.photoSize;
            if (tLRPC$PhotoSize != null) {
                return FileLoader.getPathToAttach(tLRPC$PhotoSize, true).getAbsolutePath();
            }
            TLRPC$Document tLRPC$Document = this.document;
            if (tLRPC$Document != null) {
                return FileLoader.getPathToAttach(tLRPC$Document, true).getAbsolutePath();
            }
            return ImageLoader.getHttpFilePath(this.imageUrl, "jpg").getAbsolutePath();
        }

        public void reset() {
            super.reset();
        }

        public String getAttachName() {
            TLRPC$PhotoSize tLRPC$PhotoSize = this.photoSize;
            if (tLRPC$PhotoSize != null) {
                return FileLoader.getAttachFileName(tLRPC$PhotoSize);
            }
            TLRPC$Document tLRPC$Document = this.document;
            if (tLRPC$Document != null) {
                return FileLoader.getAttachFileName(tLRPC$Document);
            }
            return Utilities.MD5(this.imageUrl) + "." + ImageLoader.getHttpUrlExtension(this.imageUrl, "jpg");
        }

        public String getPathToAttach() {
            TLRPC$PhotoSize tLRPC$PhotoSize = this.photoSize;
            if (tLRPC$PhotoSize != null) {
                return FileLoader.getPathToAttach(tLRPC$PhotoSize, true).getAbsolutePath();
            }
            TLRPC$Document tLRPC$Document = this.document;
            if (tLRPC$Document != null) {
                return FileLoader.getPathToAttach(tLRPC$Document, true).getAbsolutePath();
            }
            return this.imageUrl;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$MediaController(int i) {
        if (i != 1) {
            this.hasRecordAudioFocus = false;
        }
    }

    private static class VideoConvertMessage {
        public int currentAccount;
        public MessageObject messageObject;
        public VideoEditedInfo videoEditedInfo;

        public VideoConvertMessage(MessageObject messageObject2, VideoEditedInfo videoEditedInfo2) {
            this.messageObject = messageObject2;
            this.currentAccount = messageObject2.currentAccount;
            this.videoEditedInfo = videoEditedInfo2;
        }
    }

    private class InternalObserver extends ContentObserver {
        public InternalObserver() {
            super((Handler) null);
        }

        public void onChange(boolean z) {
            super.onChange(z);
            MediaController.this.processMediaObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        }
    }

    private class ExternalObserver extends ContentObserver {
        public ExternalObserver() {
            super((Handler) null);
        }

        public void onChange(boolean z) {
            super.onChange(z);
            MediaController.this.processMediaObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
    }

    private static class GalleryObserverInternal extends ContentObserver {
        public GalleryObserverInternal() {
            super((Handler) null);
        }

        private void scheduleReloadRunnable() {
            AndroidUtilities.runOnUIThread(MediaController.refreshGalleryRunnable = new Runnable() {
                public final void run() {
                    MediaController.GalleryObserverInternal.this.lambda$scheduleReloadRunnable$0$MediaController$GalleryObserverInternal();
                }
            }, 2000);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$scheduleReloadRunnable$0 */
        public /* synthetic */ void lambda$scheduleReloadRunnable$0$MediaController$GalleryObserverInternal() {
            if (PhotoViewer.getInstance().isVisible()) {
                scheduleReloadRunnable();
                return;
            }
            Runnable unused = MediaController.refreshGalleryRunnable = null;
            MediaController.loadGalleryPhotosAlbums(0);
        }

        public void onChange(boolean z) {
            super.onChange(z);
            if (MediaController.refreshGalleryRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(MediaController.refreshGalleryRunnable);
            }
            scheduleReloadRunnable();
        }
    }

    private static class GalleryObserverExternal extends ContentObserver {
        public GalleryObserverExternal() {
            super((Handler) null);
        }

        public void onChange(boolean z) {
            super.onChange(z);
            if (MediaController.refreshGalleryRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(MediaController.refreshGalleryRunnable);
            }
            AndroidUtilities.runOnUIThread(MediaController.refreshGalleryRunnable = $$Lambda$MediaController$GalleryObserverExternal$mKteLd2S0yu6HY8w0ERnZ8DgWvE.INSTANCE, 2000);
        }

        static /* synthetic */ void lambda$onChange$0() {
            Runnable unused = MediaController.refreshGalleryRunnable = null;
            MediaController.loadGalleryPhotosAlbums(0);
        }
    }

    public static void checkGallery() {
        AlbumEntry albumEntry;
        if (Build.VERSION.SDK_INT >= 24 && (albumEntry = allPhotosAlbumEntry) != null) {
            Utilities.globalQueue.postRunnable(new Runnable(albumEntry.photos.size()) {
                public final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    MediaController.lambda$checkGallery$1(this.f$0);
                }
            }, 2000);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006d, code lost:
        if (r5 != null) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006f, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0077, code lost:
        if (r5 == null) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x007a, code lost:
        if (r13 == r6) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x007c, code lost:
        r13 = refreshGalleryRunnable;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x007e, code lost:
        if (r13 == null) goto L_0x0085;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0080, code lost:
        org.telegram.messenger.AndroidUtilities.cancelRunOnUIThread(r13);
        refreshGalleryRunnable = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0085, code lost:
        loadGalleryPhotosAlbums(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0036  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0041 A[DONT_GENERATE] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static /* synthetic */ void lambda$checkGallery$1(int r13) {
        /*
            java.lang.String r0 = "COUNT(_id)"
            java.lang.String r1 = "android.permission.READ_EXTERNAL_STORAGE"
            r2 = 1
            r3 = 0
            r4 = 0
            android.content.Context r5 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ all -> 0x003a }
            int r5 = r5.checkSelfPermission(r1)     // Catch:{ all -> 0x003a }
            if (r5 != 0) goto L_0x0032
            android.content.Context r5 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ all -> 0x003a }
            android.content.ContentResolver r6 = r5.getContentResolver()     // Catch:{ all -> 0x003a }
            android.net.Uri r7 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI     // Catch:{ all -> 0x003a }
            java.lang.String[] r8 = new java.lang.String[r2]     // Catch:{ all -> 0x003a }
            r8[r4] = r0     // Catch:{ all -> 0x003a }
            r9 = 0
            r10 = 0
            r11 = 0
            android.database.Cursor r5 = android.provider.MediaStore.Images.Media.query(r6, r7, r8, r9, r10, r11)     // Catch:{ all -> 0x003a }
            if (r5 == 0) goto L_0x0033
            boolean r6 = r5.moveToNext()     // Catch:{ all -> 0x0030 }
            if (r6 == 0) goto L_0x0033
            int r6 = r5.getInt(r4)     // Catch:{ all -> 0x0030 }
            int r6 = r6 + r4
            goto L_0x0034
        L_0x0030:
            r6 = move-exception
            goto L_0x003c
        L_0x0032:
            r5 = r3
        L_0x0033:
            r6 = 0
        L_0x0034:
            if (r5 == 0) goto L_0x0045
            r5.close()
            goto L_0x0045
        L_0x003a:
            r6 = move-exception
            r5 = r3
        L_0x003c:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r6)     // Catch:{ all -> 0x0090 }
            if (r5 == 0) goto L_0x0044
            r5.close()
        L_0x0044:
            r6 = 0
        L_0x0045:
            android.content.Context r7 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ all -> 0x0073 }
            int r1 = r7.checkSelfPermission(r1)     // Catch:{ all -> 0x0073 }
            if (r1 != 0) goto L_0x006d
            android.content.Context r1 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ all -> 0x0073 }
            android.content.ContentResolver r7 = r1.getContentResolver()     // Catch:{ all -> 0x0073 }
            android.net.Uri r8 = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI     // Catch:{ all -> 0x0073 }
            java.lang.String[] r9 = new java.lang.String[r2]     // Catch:{ all -> 0x0073 }
            r9[r4] = r0     // Catch:{ all -> 0x0073 }
            r10 = 0
            r11 = 0
            r12 = 0
            android.database.Cursor r5 = android.provider.MediaStore.Images.Media.query(r7, r8, r9, r10, r11, r12)     // Catch:{ all -> 0x0073 }
            if (r5 == 0) goto L_0x006d
            boolean r0 = r5.moveToNext()     // Catch:{ all -> 0x0073 }
            if (r0 == 0) goto L_0x006d
            int r0 = r5.getInt(r4)     // Catch:{ all -> 0x0073 }
            int r6 = r6 + r0
        L_0x006d:
            if (r5 == 0) goto L_0x007a
        L_0x006f:
            r5.close()
            goto L_0x007a
        L_0x0073:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)     // Catch:{ all -> 0x0089 }
            if (r5 == 0) goto L_0x007a
            goto L_0x006f
        L_0x007a:
            if (r13 == r6) goto L_0x0088
            java.lang.Runnable r13 = refreshGalleryRunnable
            if (r13 == 0) goto L_0x0085
            org.telegram.messenger.AndroidUtilities.cancelRunOnUIThread(r13)
            refreshGalleryRunnable = r3
        L_0x0085:
            loadGalleryPhotosAlbums(r4)
        L_0x0088:
            return
        L_0x0089:
            r13 = move-exception
            if (r5 == 0) goto L_0x008f
            r5.close()
        L_0x008f:
            throw r13
        L_0x0090:
            r13 = move-exception
            if (r5 == 0) goto L_0x0096
            r5.close()
        L_0x0096:
            goto L_0x0098
        L_0x0097:
            throw r13
        L_0x0098:
            goto L_0x0097
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.lambda$checkGallery$1(int):void");
    }

    private final class StopMediaObserverRunnable implements Runnable {
        public int currentObserverToken;

        private StopMediaObserverRunnable() {
            this.currentObserverToken = 0;
        }

        public void run() {
            if (this.currentObserverToken == MediaController.this.startObserverToken) {
                try {
                    if (MediaController.this.internalObserver != null) {
                        ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(MediaController.this.internalObserver);
                        InternalObserver unused = MediaController.this.internalObserver = null;
                    }
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
                try {
                    if (MediaController.this.externalObserver != null) {
                        ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(MediaController.this.externalObserver);
                        ExternalObserver unused2 = MediaController.this.externalObserver = null;
                    }
                } catch (Exception e2) {
                    FileLog.e((Throwable) e2);
                }
            }
        }
    }

    public static MediaController getInstance() {
        MediaController mediaController = Instance;
        if (mediaController == null) {
            synchronized (MediaController.class) {
                mediaController = Instance;
                if (mediaController == null) {
                    mediaController = new MediaController();
                    Instance = mediaController;
                }
            }
        }
        return mediaController;
    }

    public MediaController() {
        DispatchQueue dispatchQueue = new DispatchQueue("recordQueue");
        this.recordQueue = dispatchQueue;
        dispatchQueue.setPriority(10);
        DispatchQueue dispatchQueue2 = new DispatchQueue("fileEncodingQueue");
        this.fileEncodingQueue = dispatchQueue2;
        dispatchQueue2.setPriority(10);
        this.recordQueue.postRunnable(new Runnable() {
            public final void run() {
                MediaController.this.lambda$new$2$MediaController();
            }
        });
        Utilities.globalQueue.postRunnable(new Runnable() {
            public final void run() {
                MediaController.this.lambda$new$3$MediaController();
            }
        });
        this.fileBuffer = ByteBuffer.allocateDirect(1920);
        AndroidUtilities.runOnUIThread(new Runnable() {
            public final void run() {
                MediaController.this.lambda$new$4$MediaController();
            }
        });
        String[] strArr = new String[7];
        strArr[0] = "_data";
        strArr[1] = "_display_name";
        strArr[2] = "bucket_display_name";
        strArr[3] = Build.VERSION.SDK_INT > 28 ? "date_modified" : "datetaken";
        strArr[4] = "title";
        strArr[5] = "width";
        strArr[6] = "height";
        this.mediaProjections = strArr;
        ContentResolver contentResolver = ApplicationLoader.applicationContext.getContentResolver();
        try {
            contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, new GalleryObserverExternal());
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        try {
            contentResolver.registerContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, true, new GalleryObserverInternal());
        } catch (Exception e2) {
            FileLog.e((Throwable) e2);
        }
        try {
            contentResolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, new GalleryObserverExternal());
        } catch (Exception e3) {
            FileLog.e((Throwable) e3);
        }
        try {
            contentResolver.registerContentObserver(MediaStore.Video.Media.INTERNAL_CONTENT_URI, true, new GalleryObserverInternal());
        } catch (Exception e4) {
            FileLog.e((Throwable) e4);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$MediaController() {
        try {
            this.sampleRate = 16000;
            int minBufferSize = AudioRecord.getMinBufferSize(16000, 16, 2);
            if (minBufferSize <= 0) {
                minBufferSize = 1280;
            }
            this.recordBufferSize = minBufferSize;
            for (int i = 0; i < 5; i++) {
                ByteBuffer allocateDirect = ByteBuffer.allocateDirect(this.recordBufferSize);
                allocateDirect.order(ByteOrder.nativeOrder());
                this.recordBuffers.add(allocateDirect);
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$3 */
    public /* synthetic */ void lambda$new$3$MediaController() {
        try {
            this.currentPlaybackSpeed = MessagesController.getGlobalMainSettings().getFloat("playbackSpeed", 1.0f);
            this.currentMusicPlaybackSpeed = MessagesController.getGlobalMainSettings().getFloat("musicPlaybackSpeed", 1.0f);
            SensorManager sensorManager2 = (SensorManager) ApplicationLoader.applicationContext.getSystemService("sensor");
            this.sensorManager = sensorManager2;
            this.linearSensor = sensorManager2.getDefaultSensor(10);
            Sensor defaultSensor = this.sensorManager.getDefaultSensor(9);
            this.gravitySensor = defaultSensor;
            if (this.linearSensor == null || defaultSensor == null) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("gravity or linear sensor not found");
                }
                this.accelerometerSensor = this.sensorManager.getDefaultSensor(1);
                this.linearSensor = null;
                this.gravitySensor = null;
            }
            this.proximitySensor = this.sensorManager.getDefaultSensor(8);
            this.proximityWakeLock = ((PowerManager) ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(32, "proximity");
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        try {
            AnonymousClass4 r1 = new PhoneStateListener() {
                public void onCallStateChanged(int i, String str) {
                    AndroidUtilities.runOnUIThread(new Runnable(i) {
                        public final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            MediaController.AnonymousClass4.this.lambda$onCallStateChanged$0$MediaController$4(this.f$1);
                        }
                    });
                }

                /* access modifiers changed from: private */
                /* renamed from: lambda$onCallStateChanged$0 */
                public /* synthetic */ void lambda$onCallStateChanged$0$MediaController$4(int i) {
                    if (i == 1) {
                        MediaController mediaController = MediaController.this;
                        if (mediaController.isPlayingMessage(mediaController.playingMessageObject) && !MediaController.this.isMessagePaused()) {
                            MediaController mediaController2 = MediaController.this;
                            mediaController2.lambda$startAudioAgain$7(mediaController2.playingMessageObject);
                        } else if (!(MediaController.this.recordStartRunnable == null && MediaController.this.recordingAudio == null)) {
                            MediaController.this.stopRecording(2, false, 0);
                        }
                        EmbedBottomSheet instance = EmbedBottomSheet.getInstance();
                        if (instance != null) {
                            instance.pause();
                        }
                        boolean unused = MediaController.this.callInProgress = true;
                    } else if (i == 0) {
                        boolean unused2 = MediaController.this.callInProgress = false;
                    } else if (i == 2) {
                        EmbedBottomSheet instance2 = EmbedBottomSheet.getInstance();
                        if (instance2 != null) {
                            instance2.pause();
                        }
                        boolean unused3 = MediaController.this.callInProgress = true;
                    }
                }
            };
            TelephonyManager telephonyManager = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService("phone");
            if (telephonyManager != null) {
                telephonyManager.listen(r1, 32);
            }
        } catch (Exception e2) {
            FileLog.e((Throwable) e2);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$4 */
    public /* synthetic */ void lambda$new$4$MediaController() {
        for (int i = 0; i < 3; i++) {
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.fileDidLoad);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.httpFileDidLoad);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.didReceiveNewMessages);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.messagesDeleted);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.removeAllMessagesFromDialog);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.musicDidLoad);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.mediaDidLoad);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.playerDidStartPlaying);
        }
    }

    public void onAudioFocusChange(int i) {
        AndroidUtilities.runOnUIThread(new Runnable(i) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MediaController.this.lambda$onAudioFocusChange$5$MediaController(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onAudioFocusChange$5 */
    public /* synthetic */ void lambda$onAudioFocusChange$5$MediaController(int i) {
        if (i == -1) {
            if (isPlayingMessage(getPlayingMessageObject()) && !isMessagePaused()) {
                lambda$startAudioAgain$7(this.playingMessageObject);
            }
            this.hasAudioFocus = 0;
            this.audioFocus = 0;
        } else if (i == 1) {
            this.audioFocus = 2;
            if (this.resumeAudioOnFocusGain) {
                this.resumeAudioOnFocusGain = false;
                if (isPlayingMessage(getPlayingMessageObject()) && isMessagePaused()) {
                    playMessage(getPlayingMessageObject());
                }
            }
        } else if (i == -3) {
            this.audioFocus = 1;
        } else if (i == -2) {
            this.audioFocus = 0;
            if (isPlayingMessage(getPlayingMessageObject()) && !isMessagePaused()) {
                lambda$startAudioAgain$7(this.playingMessageObject);
                this.resumeAudioOnFocusGain = true;
            }
        }
        setPlayerVolume();
    }

    /* access modifiers changed from: private */
    public void setPlayerVolume() {
        try {
            float f = this.audioFocus != 1 ? 1.0f : 0.2f;
            VideoPlayer videoPlayer2 = this.audioPlayer;
            if (videoPlayer2 != null) {
                videoPlayer2.setVolume(f * this.audioVolume);
                return;
            }
            VideoPlayer videoPlayer3 = this.videoPlayer;
            if (videoPlayer3 != null) {
                videoPlayer3.setVolume(f);
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    private void startProgressTimer(final MessageObject messageObject) {
        synchronized (this.progressTimerSync) {
            Timer timer = this.progressTimer;
            if (timer != null) {
                try {
                    timer.cancel();
                    this.progressTimer = null;
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
            }
            messageObject.getFileName();
            Timer timer2 = new Timer();
            this.progressTimer = timer2;
            timer2.schedule(new TimerTask() {
                public void run() {
                    synchronized (MediaController.this.sync) {
                        AndroidUtilities.runOnUIThread(new Runnable(messageObject) {
                            public final /* synthetic */ MessageObject f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                MediaController.AnonymousClass5.this.lambda$run$1$MediaController$5(this.f$1);
                            }
                        });
                    }
                }

                /* access modifiers changed from: private */
                /* renamed from: lambda$run$1 */
                public /* synthetic */ void lambda$run$1$MediaController$5(MessageObject messageObject) {
                    long j;
                    long j2;
                    float f;
                    float f2;
                    if (messageObject == null) {
                        return;
                    }
                    if (!(MediaController.this.audioPlayer == null && MediaController.this.videoPlayer == null) && !MediaController.this.isPaused) {
                        try {
                            if (MediaController.this.videoPlayer != null) {
                                j2 = MediaController.this.videoPlayer.getDuration();
                                j = MediaController.this.videoPlayer.getCurrentPosition();
                                if (j < 0) {
                                    return;
                                }
                                if (j2 > 0) {
                                    float f3 = (float) j2;
                                    f2 = ((float) MediaController.this.videoPlayer.getBufferedPosition()) / f3;
                                    f = j2 >= 0 ? ((float) j) / f3 : 0.0f;
                                    if (f >= 1.0f) {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            } else {
                                j2 = MediaController.this.audioPlayer.getDuration();
                                j = MediaController.this.audioPlayer.getCurrentPosition();
                                float f4 = j2 >= 0 ? ((float) j) / ((float) j2) : 0.0f;
                                float bufferedPosition = ((float) MediaController.this.audioPlayer.getBufferedPosition()) / ((float) j2);
                                if (j2 != -9223372036854775807L && j >= 0) {
                                    if (MediaController.this.seekToProgressPending == 0.0f) {
                                        f = f4;
                                        f2 = bufferedPosition;
                                    } else {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            }
                            long unused = MediaController.this.lastProgress = j;
                            messageObject.audioPlayerDuration = (int) (j2 / 1000);
                            messageObject.audioProgress = f;
                            messageObject.audioProgressSec = (int) (MediaController.this.lastProgress / 1000);
                            messageObject.bufferedProgress = f2;
                            if (f >= 0.0f && MediaController.this.shouldSavePositionForCurrentAudio != null && SystemClock.elapsedRealtime() - MediaController.this.lastSaveTime >= 1000) {
                                String unused2 = MediaController.this.shouldSavePositionForCurrentAudio;
                                long unused3 = MediaController.this.lastSaveTime = SystemClock.elapsedRealtime();
                                Utilities.globalQueue.postRunnable(new Runnable(f) {
                                    public final /* synthetic */ float f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    public final void run() {
                                        MediaController.AnonymousClass5.this.lambda$null$0$MediaController$5(this.f$1);
                                    }
                                });
                            }
                            NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, Integer.valueOf(messageObject.getId()), Float.valueOf(f));
                        } catch (Exception e) {
                            FileLog.e((Throwable) e);
                        }
                    }
                }

                /* access modifiers changed from: private */
                /* renamed from: lambda$null$0 */
                public /* synthetic */ void lambda$null$0$MediaController$5(float f) {
                    ApplicationLoader.applicationContext.getSharedPreferences("media_saved_pos", 0).edit().putFloat(MediaController.this.shouldSavePositionForCurrentAudio, f).commit();
                }
            }, 0, 17);
        }
    }

    private void stopProgressTimer() {
        synchronized (this.progressTimerSync) {
            Timer timer = this.progressTimer;
            if (timer != null) {
                try {
                    timer.cancel();
                    this.progressTimer = null;
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
            }
        }
    }

    public void cleanup() {
        cleanupPlayer(true, true);
        this.audioInfo = null;
        this.playMusicAgain = false;
        for (int i = 0; i < 3; i++) {
            DownloadController.getInstance(i).cleanup();
        }
        this.videoConvertQueue.clear();
        this.generatingWaveform.clear();
        this.voiceMessagesPlaylist = null;
        this.voiceMessagesPlaylistMap = null;
        clearPlaylist();
        cancelVideoConvert((MessageObject) null);
    }

    private void clearPlaylist() {
        this.playlist.clear();
        this.playlistMap.clear();
        this.shuffledPlaylist.clear();
        this.playlistClassGuid = 0;
        boolean[] zArr = this.playlistEndReached;
        zArr[1] = false;
        zArr[0] = false;
        this.playlistMergeDialogId = 0;
        int[] iArr = this.playlistMaxId;
        iArr[1] = Integer.MAX_VALUE;
        iArr[0] = Integer.MAX_VALUE;
        this.loadingPlaylist = false;
        this.playlistGlobalSearchParams = null;
    }

    public void startMediaObserver() {
        ApplicationLoader.applicationHandler.removeCallbacks(this.stopMediaObserverRunnable);
        this.startObserverToken++;
        try {
            if (this.internalObserver == null) {
                ContentResolver contentResolver = ApplicationLoader.applicationContext.getContentResolver();
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ExternalObserver externalObserver2 = new ExternalObserver();
                this.externalObserver = externalObserver2;
                contentResolver.registerContentObserver(uri, false, externalObserver2);
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        try {
            if (this.externalObserver == null) {
                ContentResolver contentResolver2 = ApplicationLoader.applicationContext.getContentResolver();
                Uri uri2 = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                InternalObserver internalObserver2 = new InternalObserver();
                this.internalObserver = internalObserver2;
                contentResolver2.registerContentObserver(uri2, false, internalObserver2);
            }
        } catch (Exception e2) {
            FileLog.e((Throwable) e2);
        }
    }

    public void stopMediaObserver() {
        if (this.stopMediaObserverRunnable == null) {
            this.stopMediaObserverRunnable = new StopMediaObserverRunnable();
        }
        this.stopMediaObserverRunnable.currentObserverToken = this.startObserverToken;
        ApplicationLoader.applicationHandler.postDelayed(this.stopMediaObserverRunnable, 5000);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Can't wrap try/catch for region: R(3:35|36|62) */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0052, code lost:
        if (r2.toLowerCase().contains("screenshot") == false) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005e, code lost:
        if (r4.toLowerCase().contains("screenshot") != false) goto L_0x0078;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x006a, code lost:
        if (r5.toLowerCase().contains("screenshot") != false) goto L_0x0078;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r14.add(java.lang.Long.valueOf(r6));
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x00a5 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void processMediaObserver(android.net.Uri r14) {
        /*
            r13 = this;
            r0 = 0
            android.graphics.Point r1 = org.telegram.messenger.AndroidUtilities.getRealScreenSize()     // Catch:{ Exception -> 0x00c7 }
            android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x00c7 }
            android.content.ContentResolver r3 = r2.getContentResolver()     // Catch:{ Exception -> 0x00c7 }
            java.lang.String[] r5 = r13.mediaProjections     // Catch:{ Exception -> 0x00c7 }
            r6 = 0
            r7 = 0
            java.lang.String r8 = "date_added DESC LIMIT 1"
            r4 = r14
            android.database.Cursor r0 = r3.query(r4, r5, r6, r7, r8)     // Catch:{ Exception -> 0x00c7 }
            java.util.ArrayList r14 = new java.util.ArrayList     // Catch:{ Exception -> 0x00c7 }
            r14.<init>()     // Catch:{ Exception -> 0x00c7 }
            if (r0 == 0) goto L_0x00b1
        L_0x001d:
            boolean r2 = r0.moveToNext()     // Catch:{ Exception -> 0x00c7 }
            if (r2 == 0) goto L_0x00ae
            r2 = 0
            java.lang.String r2 = r0.getString(r2)     // Catch:{ Exception -> 0x00c7 }
            r3 = 1
            java.lang.String r4 = r0.getString(r3)     // Catch:{ Exception -> 0x00c7 }
            r5 = 2
            java.lang.String r5 = r0.getString(r5)     // Catch:{ Exception -> 0x00c7 }
            r6 = 3
            long r6 = r0.getLong(r6)     // Catch:{ Exception -> 0x00c7 }
            r8 = 4
            java.lang.String r8 = r0.getString(r8)     // Catch:{ Exception -> 0x00c7 }
            r9 = 5
            int r9 = r0.getInt(r9)     // Catch:{ Exception -> 0x00c7 }
            r10 = 6
            int r10 = r0.getInt(r10)     // Catch:{ Exception -> 0x00c7 }
            java.lang.String r11 = "screenshot"
            if (r2 == 0) goto L_0x0054
            java.lang.String r12 = r2.toLowerCase()     // Catch:{ Exception -> 0x00c7 }
            boolean r12 = r12.contains(r11)     // Catch:{ Exception -> 0x00c7 }
            if (r12 != 0) goto L_0x0078
        L_0x0054:
            if (r4 == 0) goto L_0x0060
            java.lang.String r4 = r4.toLowerCase()     // Catch:{ Exception -> 0x00c7 }
            boolean r4 = r4.contains(r11)     // Catch:{ Exception -> 0x00c7 }
            if (r4 != 0) goto L_0x0078
        L_0x0060:
            if (r5 == 0) goto L_0x006c
            java.lang.String r4 = r5.toLowerCase()     // Catch:{ Exception -> 0x00c7 }
            boolean r4 = r4.contains(r11)     // Catch:{ Exception -> 0x00c7 }
            if (r4 != 0) goto L_0x0078
        L_0x006c:
            if (r8 == 0) goto L_0x001d
            java.lang.String r4 = r8.toLowerCase()     // Catch:{ Exception -> 0x00c7 }
            boolean r4 = r4.contains(r11)     // Catch:{ Exception -> 0x00c7 }
            if (r4 == 0) goto L_0x001d
        L_0x0078:
            if (r9 == 0) goto L_0x007c
            if (r10 != 0) goto L_0x008a
        L_0x007c:
            android.graphics.BitmapFactory$Options r4 = new android.graphics.BitmapFactory$Options     // Catch:{ Exception -> 0x00a5 }
            r4.<init>()     // Catch:{ Exception -> 0x00a5 }
            r4.inJustDecodeBounds = r3     // Catch:{ Exception -> 0x00a5 }
            android.graphics.BitmapFactory.decodeFile(r2, r4)     // Catch:{ Exception -> 0x00a5 }
            int r9 = r4.outWidth     // Catch:{ Exception -> 0x00a5 }
            int r10 = r4.outHeight     // Catch:{ Exception -> 0x00a5 }
        L_0x008a:
            if (r9 <= 0) goto L_0x009c
            if (r10 <= 0) goto L_0x009c
            int r2 = r1.x     // Catch:{ Exception -> 0x00a5 }
            if (r9 != r2) goto L_0x0096
            int r3 = r1.y     // Catch:{ Exception -> 0x00a5 }
            if (r10 == r3) goto L_0x009c
        L_0x0096:
            if (r10 != r2) goto L_0x001d
            int r2 = r1.y     // Catch:{ Exception -> 0x00a5 }
            if (r9 != r2) goto L_0x001d
        L_0x009c:
            java.lang.Long r2 = java.lang.Long.valueOf(r6)     // Catch:{ Exception -> 0x00a5 }
            r14.add(r2)     // Catch:{ Exception -> 0x00a5 }
            goto L_0x001d
        L_0x00a5:
            java.lang.Long r2 = java.lang.Long.valueOf(r6)     // Catch:{ Exception -> 0x00c7 }
            r14.add(r2)     // Catch:{ Exception -> 0x00c7 }
            goto L_0x001d
        L_0x00ae:
            r0.close()     // Catch:{ Exception -> 0x00c7 }
        L_0x00b1:
            boolean r1 = r14.isEmpty()     // Catch:{ Exception -> 0x00c7 }
            if (r1 != 0) goto L_0x00bf
            org.telegram.messenger.-$$Lambda$MediaController$i3eL4EzmOfRtpHlDYIIymjVY0zY r1 = new org.telegram.messenger.-$$Lambda$MediaController$i3eL4EzmOfRtpHlDYIIymjVY0zY     // Catch:{ Exception -> 0x00c7 }
            r1.<init>(r14)     // Catch:{ Exception -> 0x00c7 }
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r1)     // Catch:{ Exception -> 0x00c7 }
        L_0x00bf:
            if (r0 == 0) goto L_0x00ce
        L_0x00c1:
            r0.close()     // Catch:{ Exception -> 0x00ce }
            goto L_0x00ce
        L_0x00c5:
            r14 = move-exception
            goto L_0x00cf
        L_0x00c7:
            r14 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r14)     // Catch:{ all -> 0x00c5 }
            if (r0 == 0) goto L_0x00ce
            goto L_0x00c1
        L_0x00ce:
            return
        L_0x00cf:
            if (r0 == 0) goto L_0x00d4
            r0.close()     // Catch:{ Exception -> 0x00d4 }
        L_0x00d4:
            goto L_0x00d6
        L_0x00d5:
            throw r14
        L_0x00d6:
            goto L_0x00d5
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.processMediaObserver(android.net.Uri):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$processMediaObserver$6 */
    public /* synthetic */ void lambda$processMediaObserver$6$MediaController(ArrayList arrayList) {
        NotificationCenter.getInstance(this.lastChatAccount).postNotificationName(NotificationCenter.screenshotTook, new Object[0]);
        checkScreenshots(arrayList);
    }

    private void checkScreenshots(ArrayList<Long> arrayList) {
        if (arrayList != null && !arrayList.isEmpty() && this.lastChatEnterTime != 0) {
            if (this.lastUser != null || (this.lastSecretChat instanceof TLRPC$TL_encryptedChat)) {
                boolean z = false;
                for (int i = 0; i < arrayList.size(); i++) {
                    Long l = arrayList.get(i);
                    if ((this.lastMediaCheckTime == 0 || l.longValue() > this.lastMediaCheckTime) && l.longValue() >= this.lastChatEnterTime && (this.lastChatLeaveTime == 0 || l.longValue() <= this.lastChatLeaveTime + 2000)) {
                        this.lastMediaCheckTime = Math.max(this.lastMediaCheckTime, l.longValue());
                        z = true;
                    }
                }
                if (!z) {
                    return;
                }
                if (this.lastSecretChat != null) {
                    SecretChatHelper.getInstance(this.lastChatAccount).sendScreenshotMessage(this.lastSecretChat, this.lastChatVisibleMessages, (TLRPC$Message) null);
                } else {
                    SendMessagesHelper.getInstance(this.lastChatAccount).sendScreenshotMessage(this.lastUser, this.lastMessageId, (TLRPC$Message) null);
                }
            }
        }
    }

    public void setLastVisibleMessageIds(int i, long j, long j2, TLRPC$User tLRPC$User, TLRPC$EncryptedChat tLRPC$EncryptedChat, ArrayList<Long> arrayList, int i2) {
        this.lastChatEnterTime = j;
        this.lastChatLeaveTime = j2;
        this.lastChatAccount = i;
        this.lastSecretChat = tLRPC$EncryptedChat;
        this.lastUser = tLRPC$User;
        this.lastMessageId = i2;
        this.lastChatVisibleMessages = arrayList;
    }

    public void didReceivedNotification(int i, int i2, Object... objArr) {
        ArrayList<MessageObject> arrayList;
        int indexOf;
        int indexOf2;
        int i3 = 0;
        if (i == NotificationCenter.fileDidLoad || i == NotificationCenter.httpFileDidLoad) {
            String str = objArr[0];
            MessageObject messageObject = this.playingMessageObject;
            if (messageObject != null && messageObject.currentAccount == i2 && FileLoader.getAttachFileName(messageObject.getDocument()).equals(str)) {
                if (this.downloadingCurrentMessage) {
                    this.playMusicAgain = true;
                    playMessage(this.playingMessageObject);
                } else if (this.audioInfo == null) {
                    try {
                        this.audioInfo = AudioInfo.getAudioInfo(FileLoader.getPathToMessage(this.playingMessageObject.messageOwner));
                    } catch (Exception e) {
                        FileLog.e((Throwable) e);
                    }
                }
            }
        } else if (i == NotificationCenter.messagesDeleted) {
            if (!objArr[2].booleanValue()) {
                int intValue = objArr[1].intValue();
                ArrayList arrayList2 = objArr[0];
                MessageObject messageObject2 = this.playingMessageObject;
                if (messageObject2 != null && intValue == messageObject2.messageOwner.peer_id.channel_id && arrayList2.contains(Integer.valueOf(messageObject2.getId()))) {
                    cleanupPlayer(true, true);
                }
                ArrayList<MessageObject> arrayList3 = this.voiceMessagesPlaylist;
                if (arrayList3 != null && !arrayList3.isEmpty() && intValue == this.voiceMessagesPlaylist.get(0).messageOwner.peer_id.channel_id) {
                    while (i3 < arrayList2.size()) {
                        Integer num = (Integer) arrayList2.get(i3);
                        MessageObject messageObject3 = this.voiceMessagesPlaylistMap.get(num.intValue());
                        this.voiceMessagesPlaylistMap.remove(num.intValue());
                        if (messageObject3 != null) {
                            this.voiceMessagesPlaylist.remove(messageObject3);
                        }
                        i3++;
                    }
                }
            }
        } else if (i == NotificationCenter.removeAllMessagesFromDialog) {
            long longValue = objArr[0].longValue();
            MessageObject messageObject4 = this.playingMessageObject;
            if (messageObject4 != null && messageObject4.getDialogId() == longValue) {
                cleanupPlayer(false, true);
            }
        } else if (i == NotificationCenter.musicDidLoad) {
            long longValue2 = objArr[0].longValue();
            MessageObject messageObject5 = this.playingMessageObject;
            if (messageObject5 != null && messageObject5.isMusic() && this.playingMessageObject.getDialogId() == longValue2 && !this.playingMessageObject.scheduled) {
                this.playlist.addAll(0, objArr[1]);
                this.playlist.addAll(objArr[2]);
                int size = this.playlist.size();
                for (int i4 = 0; i4 < size; i4++) {
                    MessageObject messageObject6 = this.playlist.get(i4);
                    this.playlistMap.put(Integer.valueOf(messageObject6.getId()), messageObject6);
                    int[] iArr = this.playlistMaxId;
                    iArr[0] = Math.min(iArr[0], messageObject6.getId());
                }
                sortPlaylist();
                if (SharedConfig.shuffleMusic) {
                    buildShuffledPlayList();
                } else {
                    MessageObject messageObject7 = this.playingMessageObject;
                    if (messageObject7 != null && (indexOf2 = this.playlist.indexOf(messageObject7)) >= 0) {
                        this.currentPlaylistNum = indexOf2;
                    }
                }
                this.playlistClassGuid = ConnectionsManager.generateClassGuid();
            }
        } else if (i == NotificationCenter.mediaDidLoad) {
            if (objArr[3].intValue() == this.playlistClassGuid && this.playingMessageObject != null) {
                long longValue3 = objArr[0].longValue();
                objArr[4].intValue();
                ArrayList arrayList4 = objArr[2];
                int i5 = (int) longValue3;
                char c = longValue3 == this.playlistMergeDialogId ? (char) 1 : 0;
                if (!arrayList4.isEmpty()) {
                    this.playlistEndReached[c] = objArr[5].booleanValue();
                }
                int i6 = 0;
                for (int i7 = 0; i7 < arrayList4.size(); i7++) {
                    MessageObject messageObject8 = (MessageObject) arrayList4.get(i7);
                    if (!this.playlistMap.containsKey(Integer.valueOf(messageObject8.getId()))) {
                        i6++;
                        this.playlist.add(0, messageObject8);
                        this.playlistMap.put(Integer.valueOf(messageObject8.getId()), messageObject8);
                        int[] iArr2 = this.playlistMaxId;
                        iArr2[c] = Math.min(iArr2[c], messageObject8.getId());
                    }
                }
                sortPlaylist();
                MessageObject messageObject9 = this.playingMessageObject;
                if (messageObject9 != null && (indexOf = this.playlist.indexOf(messageObject9)) >= 0) {
                    this.currentPlaylistNum = indexOf;
                }
                this.loadingPlaylist = false;
                if (SharedConfig.shuffleMusic) {
                    buildShuffledPlayList();
                }
                if (i6 != 0) {
                    NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.moreMusicDidLoad, Integer.valueOf(i6));
                }
            }
        } else if (i == NotificationCenter.didReceiveNewMessages) {
            if (!objArr[2].booleanValue() && (arrayList = this.voiceMessagesPlaylist) != null && !arrayList.isEmpty() && objArr[0].longValue() == this.voiceMessagesPlaylist.get(0).getDialogId()) {
                ArrayList arrayList5 = objArr[1];
                while (i3 < arrayList5.size()) {
                    MessageObject messageObject10 = (MessageObject) arrayList5.get(i3);
                    if ((messageObject10.isVoice() || messageObject10.isRoundVideo()) && (!this.voiceMessagesPlaylistUnread || (messageObject10.isContentUnread() && !messageObject10.isOut()))) {
                        this.voiceMessagesPlaylist.add(messageObject10);
                        this.voiceMessagesPlaylistMap.put(messageObject10.getId(), messageObject10);
                    }
                    i3++;
                }
            }
        } else if (i == NotificationCenter.playerDidStartPlaying) {
            if (!getInstance().isCurrentPlayer(objArr[0])) {
                getInstance().lambda$startAudioAgain$7(getInstance().getPlayingMessageObject());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRecordingAudio() {
        return (this.recordStartRunnable == null && this.recordingAudio == null) ? false : true;
    }

    private boolean isNearToSensor(float f) {
        return f < 5.0f && f != this.proximitySensor.getMaximumRange();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000a, code lost:
        r0 = r1.playingMessageObject;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isRecordingOrListeningByProximity() {
        /*
            r1 = this;
            boolean r0 = r1.proximityTouched
            if (r0 == 0) goto L_0x001e
            boolean r0 = r1.isRecordingAudio()
            if (r0 != 0) goto L_0x001c
            org.telegram.messenger.MessageObject r0 = r1.playingMessageObject
            if (r0 == 0) goto L_0x001e
            boolean r0 = r0.isVoice()
            if (r0 != 0) goto L_0x001c
            org.telegram.messenger.MessageObject r0 = r1.playingMessageObject
            boolean r0 = r0.isRoundVideo()
            if (r0 == 0) goto L_0x001e
        L_0x001c:
            r0 = 1
            goto L_0x001f
        L_0x001e:
            r0 = 0
        L_0x001f:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.isRecordingOrListeningByProximity():boolean");
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        PowerManager.WakeLock wakeLock;
        PowerManager.WakeLock wakeLock2;
        PowerManager.WakeLock wakeLock3;
        PowerManager.WakeLock wakeLock4;
        PowerManager.WakeLock wakeLock5;
        int i;
        boolean z;
        double d;
        SensorEvent sensorEvent2 = sensorEvent;
        if (this.sensorsStarted && VoIPService.getSharedInstance() == null) {
            Sensor sensor = sensorEvent2.sensor;
            if (sensor == this.proximitySensor) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("proximity changed to " + sensorEvent2.values[0] + " max value = " + this.proximitySensor.getMaximumRange());
                }
                float f = this.lastProximityValue;
                if (f == -100.0f) {
                    this.lastProximityValue = sensorEvent2.values[0];
                } else if (f != sensorEvent2.values[0]) {
                    this.proximityHasDifferentValues = true;
                }
                if (this.proximityHasDifferentValues) {
                    this.proximityTouched = isNearToSensor(sensorEvent2.values[0]);
                }
            } else if (sensor == this.accelerometerSensor) {
                long j = this.lastTimestamp;
                if (j == 0) {
                    d = 0.9800000190734863d;
                } else {
                    double d2 = (double) (sensorEvent2.timestamp - j);
                    Double.isNaN(d2);
                    d = 1.0d / ((d2 / 1.0E9d) + 1.0d);
                }
                this.lastTimestamp = sensorEvent2.timestamp;
                float[] fArr = this.gravity;
                double d3 = (double) fArr[0];
                Double.isNaN(d3);
                double d4 = 1.0d - d;
                float[] fArr2 = sensorEvent2.values;
                double d5 = (double) fArr2[0];
                Double.isNaN(d5);
                fArr[0] = (float) ((d3 * d) + (d5 * d4));
                double d6 = (double) fArr[1];
                Double.isNaN(d6);
                double d7 = (double) fArr2[1];
                Double.isNaN(d7);
                fArr[1] = (float) ((d6 * d) + (d7 * d4));
                double d8 = (double) fArr[2];
                Double.isNaN(d8);
                double d9 = d * d8;
                double d10 = (double) fArr2[2];
                Double.isNaN(d10);
                fArr[2] = (float) (d9 + (d4 * d10));
                float[] fArr3 = this.gravityFast;
                fArr3[0] = (fArr[0] * 0.8f) + (fArr2[0] * 0.19999999f);
                fArr3[1] = (fArr[1] * 0.8f) + (fArr2[1] * 0.19999999f);
                fArr3[2] = (fArr[2] * 0.8f) + (fArr2[2] * 0.19999999f);
                float[] fArr4 = this.linearAcceleration;
                fArr4[0] = fArr2[0] - fArr[0];
                fArr4[1] = fArr2[1] - fArr[1];
                fArr4[2] = fArr2[2] - fArr[2];
            } else if (sensor == this.linearSensor) {
                float[] fArr5 = this.linearAcceleration;
                float[] fArr6 = sensorEvent2.values;
                fArr5[0] = fArr6[0];
                fArr5[1] = fArr6[1];
                fArr5[2] = fArr6[2];
            } else if (sensor == this.gravitySensor) {
                float[] fArr7 = this.gravityFast;
                float[] fArr8 = this.gravity;
                float[] fArr9 = sensorEvent2.values;
                float f2 = fArr9[0];
                fArr8[0] = f2;
                fArr7[0] = f2;
                float f3 = fArr9[1];
                fArr8[1] = f3;
                fArr7[1] = f3;
                float f4 = fArr9[2];
                fArr8[2] = f4;
                fArr7[2] = f4;
            }
            Sensor sensor2 = sensorEvent2.sensor;
            if (sensor2 == this.linearSensor || sensor2 == this.gravitySensor || sensor2 == this.accelerometerSensor) {
                float[] fArr10 = this.gravity;
                float f5 = fArr10[0];
                float[] fArr11 = this.linearAcceleration;
                float f6 = (f5 * fArr11[0]) + (fArr10[1] * fArr11[1]) + (fArr10[2] * fArr11[2]);
                int i2 = this.raisedToBack;
                if (i2 != 6 && ((f6 > 0.0f && this.previousAccValue > 0.0f) || (f6 < 0.0f && this.previousAccValue < 0.0f))) {
                    if (f6 > 0.0f) {
                        z = f6 > 15.0f;
                        i = 1;
                    } else {
                        z = f6 < -15.0f;
                        i = 2;
                    }
                    int i3 = this.raisedToTopSign;
                    if (i3 != 0 && i3 != i) {
                        int i4 = this.raisedToTop;
                        if (i4 != 6 || !z) {
                            if (!z) {
                                this.countLess++;
                            }
                            if (!(this.countLess != 10 && i4 == 6 && i2 == 0)) {
                                this.raisedToTop = 0;
                                this.raisedToTopSign = 0;
                                this.raisedToBack = 0;
                                this.countLess = 0;
                            }
                        } else if (i2 < 6) {
                            int i5 = i2 + 1;
                            this.raisedToBack = i5;
                            if (i5 == 6) {
                                this.raisedToTop = 0;
                                this.raisedToTopSign = 0;
                                this.countLess = 0;
                                this.timeSinceRaise = System.currentTimeMillis();
                                if (BuildVars.LOGS_ENABLED && BuildVars.DEBUG_PRIVATE_VERSION) {
                                    FileLog.d("motion detected");
                                }
                            }
                        }
                    } else if (z && i2 == 0 && (i3 == 0 || i3 == i)) {
                        int i6 = this.raisedToTop;
                        if (i6 < 6 && !this.proximityTouched) {
                            this.raisedToTopSign = i;
                            int i7 = i6 + 1;
                            this.raisedToTop = i7;
                            if (i7 == 6) {
                                this.countLess = 0;
                            }
                        }
                    } else {
                        if (!z) {
                            this.countLess++;
                        }
                        if (!(i3 == i && this.countLess != 10 && this.raisedToTop == 6 && i2 == 0)) {
                            this.raisedToBack = 0;
                            this.raisedToTop = 0;
                            this.raisedToTopSign = 0;
                            this.countLess = 0;
                        }
                    }
                }
                this.previousAccValue = f6;
                float[] fArr12 = this.gravityFast;
                this.accelerometerVertical = fArr12[1] > 2.5f && Math.abs(fArr12[2]) < 4.0f && Math.abs(this.gravityFast[0]) > 1.5f;
            }
            if (this.raisedToBack != 6 || !this.accelerometerVertical || !this.proximityTouched || NotificationsController.audioManager.isWiredHeadsetOn()) {
                boolean z2 = this.proximityTouched;
                if (z2) {
                    if (this.playingMessageObject != null && !ApplicationLoader.mainInterfacePaused && ((this.playingMessageObject.isVoice() || this.playingMessageObject.isRoundVideo()) && !this.useFrontSpeaker && !NotificationsController.audioManager.isWiredHeadsetOn())) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("start listen by proximity only");
                        }
                        if (this.proximityHasDifferentValues && (wakeLock3 = this.proximityWakeLock) != null && !wakeLock3.isHeld()) {
                            this.proximityWakeLock.acquire();
                        }
                        setUseFrontSpeaker(true);
                        startAudioAgain(false);
                        this.ignoreOnPause = true;
                    }
                } else if (!z2) {
                    if (this.raiseToEarRecord) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("stop record");
                        }
                        stopRecording(2, false, 0);
                        this.raiseToEarRecord = false;
                        this.ignoreOnPause = false;
                        if (this.proximityHasDifferentValues && (wakeLock2 = this.proximityWakeLock) != null && wakeLock2.isHeld()) {
                            this.proximityWakeLock.release();
                        }
                    } else if (this.useFrontSpeaker) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("stop listen");
                        }
                        this.useFrontSpeaker = false;
                        startAudioAgain(true);
                        this.ignoreOnPause = false;
                        if (this.proximityHasDifferentValues && (wakeLock = this.proximityWakeLock) != null && wakeLock.isHeld()) {
                            this.proximityWakeLock.release();
                        }
                    }
                }
            } else {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("sensor values reached");
                }
                if (this.playingMessageObject != null || this.recordStartRunnable != null || this.recordingAudio != null || PhotoViewer.getInstance().isVisible() || !ApplicationLoader.isScreenOn || this.inputFieldHasText || !this.allowStartRecord || this.raiseChat == null || this.callInProgress) {
                    MessageObject messageObject = this.playingMessageObject;
                    if (messageObject != null && ((messageObject.isVoice() || this.playingMessageObject.isRoundVideo()) && !this.useFrontSpeaker)) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("start listen");
                        }
                        if (this.proximityHasDifferentValues && (wakeLock4 = this.proximityWakeLock) != null && !wakeLock4.isHeld()) {
                            this.proximityWakeLock.acquire();
                        }
                        setUseFrontSpeaker(true);
                        startAudioAgain(false);
                        this.ignoreOnPause = true;
                    }
                } else if (!this.raiseToEarRecord) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("start record");
                    }
                    this.useFrontSpeaker = true;
                    if (!this.raiseChat.playFirstUnreadVoiceMessage()) {
                        this.raiseToEarRecord = true;
                        this.useFrontSpeaker = false;
                        startRecording(this.raiseChat.getCurrentAccount(), this.raiseChat.getDialogId(), (MessageObject) null, this.raiseChat.getThreadMessage(), this.raiseChat.getClassGuid());
                    }
                    if (this.useFrontSpeaker) {
                        setUseFrontSpeaker(true);
                    }
                    this.ignoreOnPause = true;
                    if (this.proximityHasDifferentValues && (wakeLock5 = this.proximityWakeLock) != null && !wakeLock5.isHeld()) {
                        this.proximityWakeLock.acquire();
                    }
                }
                this.raisedToBack = 0;
                this.raisedToTop = 0;
                this.raisedToTopSign = 0;
                this.countLess = 0;
            }
            if (this.timeSinceRaise != 0 && this.raisedToBack == 6 && Math.abs(System.currentTimeMillis() - this.timeSinceRaise) > 1000) {
                this.raisedToBack = 0;
                this.raisedToTop = 0;
                this.raisedToTopSign = 0;
                this.countLess = 0;
                this.timeSinceRaise = 0;
            }
        }
    }

    private void setUseFrontSpeaker(boolean z) {
        this.useFrontSpeaker = z;
        AudioManager audioManager = NotificationsController.audioManager;
        if (z) {
            audioManager.setBluetoothScoOn(false);
            audioManager.setSpeakerphoneOn(false);
            return;
        }
        audioManager.setSpeakerphoneOn(true);
    }

    public void startRecordingIfFromSpeaker() {
        ChatActivity chatActivity;
        if (this.useFrontSpeaker && (chatActivity = this.raiseChat) != null && this.allowStartRecord && SharedConfig.raiseToSpeak) {
            this.raiseToEarRecord = true;
            startRecording(chatActivity.getCurrentAccount(), this.raiseChat.getDialogId(), (MessageObject) null, this.raiseChat.getThreadMessage(), this.raiseChat.getClassGuid());
            this.ignoreOnPause = true;
        }
    }

    private void startAudioAgain(boolean z) {
        MessageObject messageObject = this.playingMessageObject;
        if (messageObject != null) {
            int i = 0;
            NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.audioRouteChanged, Boolean.valueOf(this.useFrontSpeaker));
            VideoPlayer videoPlayer2 = this.videoPlayer;
            if (videoPlayer2 != null) {
                if (!this.useFrontSpeaker) {
                    i = 3;
                }
                videoPlayer2.setStreamType(i);
                if (!z) {
                    if (this.videoPlayer.getCurrentPosition() < 1000) {
                        this.videoPlayer.seekTo(0);
                    }
                    this.videoPlayer.play();
                    return;
                }
                lambda$startAudioAgain$7(this.playingMessageObject);
                return;
            }
            VideoPlayer videoPlayer3 = this.audioPlayer;
            boolean z2 = videoPlayer3 != null;
            MessageObject messageObject2 = this.playingMessageObject;
            float f = messageObject2.audioProgress;
            int i2 = messageObject2.audioPlayerDuration;
            if (z || videoPlayer3 == null || !videoPlayer3.isPlaying() || ((float) i2) * f > 1.0f) {
                messageObject2.audioProgress = f;
            } else {
                messageObject2.audioProgress = 0.0f;
            }
            cleanupPlayer(false, true);
            playMessage(messageObject2);
            if (!z) {
                return;
            }
            if (z2) {
                AndroidUtilities.runOnUIThread(new Runnable(messageObject2) {
                    public final /* synthetic */ MessageObject f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        MediaController.this.lambda$startAudioAgain$7$MediaController(this.f$1);
                    }
                }, 100);
            } else {
                lambda$startAudioAgain$7(messageObject2);
            }
        }
    }

    public void setInputFieldHasText(boolean z) {
        this.inputFieldHasText = z;
    }

    public void setAllowStartRecord(boolean z) {
        this.allowStartRecord = z;
    }

    public void startRaiseToEarSensors(ChatActivity chatActivity) {
        if (chatActivity == null) {
            return;
        }
        if ((this.accelerometerSensor != null || (this.gravitySensor != null && this.linearAcceleration != null)) && this.proximitySensor != null) {
            this.raiseChat = chatActivity;
            if (!SharedConfig.raiseToSpeak) {
                MessageObject messageObject = this.playingMessageObject;
                if (messageObject == null) {
                    return;
                }
                if (!messageObject.isVoice() && !this.playingMessageObject.isRoundVideo()) {
                    return;
                }
            }
            if (!this.sensorsStarted) {
                float[] fArr = this.gravity;
                fArr[2] = 0.0f;
                fArr[1] = 0.0f;
                fArr[0] = 0.0f;
                float[] fArr2 = this.linearAcceleration;
                fArr2[2] = 0.0f;
                fArr2[1] = 0.0f;
                fArr2[0] = 0.0f;
                float[] fArr3 = this.gravityFast;
                fArr3[2] = 0.0f;
                fArr3[1] = 0.0f;
                fArr3[0] = 0.0f;
                this.lastTimestamp = 0;
                this.previousAccValue = 0.0f;
                this.raisedToTop = 0;
                this.raisedToTopSign = 0;
                this.countLess = 0;
                this.raisedToBack = 0;
                Utilities.globalQueue.postRunnable(new Runnable() {
                    public final void run() {
                        MediaController.this.lambda$startRaiseToEarSensors$8$MediaController();
                    }
                });
                this.sensorsStarted = true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startRaiseToEarSensors$8 */
    public /* synthetic */ void lambda$startRaiseToEarSensors$8$MediaController() {
        Sensor sensor = this.gravitySensor;
        if (sensor != null) {
            this.sensorManager.registerListener(this, sensor, 30000);
        }
        Sensor sensor2 = this.linearSensor;
        if (sensor2 != null) {
            this.sensorManager.registerListener(this, sensor2, 30000);
        }
        Sensor sensor3 = this.accelerometerSensor;
        if (sensor3 != null) {
            this.sensorManager.registerListener(this, sensor3, 30000);
        }
        this.sensorManager.registerListener(this, this.proximitySensor, 3);
    }

    public void stopRaiseToEarSensors(ChatActivity chatActivity, boolean z) {
        PowerManager.WakeLock wakeLock;
        if (this.ignoreOnPause) {
            this.ignoreOnPause = false;
            return;
        }
        stopRecording(z ? 2 : 0, false, 0);
        if (this.sensorsStarted && !this.ignoreOnPause) {
            if ((this.accelerometerSensor != null || (this.gravitySensor != null && this.linearAcceleration != null)) && this.proximitySensor != null && this.raiseChat == chatActivity) {
                this.raiseChat = null;
                this.sensorsStarted = false;
                this.accelerometerVertical = false;
                this.proximityTouched = false;
                this.raiseToEarRecord = false;
                this.useFrontSpeaker = false;
                Utilities.globalQueue.postRunnable(new Runnable() {
                    public final void run() {
                        MediaController.this.lambda$stopRaiseToEarSensors$9$MediaController();
                    }
                });
                if (this.proximityHasDifferentValues && (wakeLock = this.proximityWakeLock) != null && wakeLock.isHeld()) {
                    this.proximityWakeLock.release();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$stopRaiseToEarSensors$9 */
    public /* synthetic */ void lambda$stopRaiseToEarSensors$9$MediaController() {
        Sensor sensor = this.linearSensor;
        if (sensor != null) {
            this.sensorManager.unregisterListener(this, sensor);
        }
        Sensor sensor2 = this.gravitySensor;
        if (sensor2 != null) {
            this.sensorManager.unregisterListener(this, sensor2);
        }
        Sensor sensor3 = this.accelerometerSensor;
        if (sensor3 != null) {
            this.sensorManager.unregisterListener(this, sensor3);
        }
        this.sensorManager.unregisterListener(this, this.proximitySensor);
    }

    public void cleanupPlayer(boolean z, boolean z2) {
        cleanupPlayer(z, z2, false, false);
    }

    public void cleanupPlayer(boolean z, boolean z2, boolean z3, boolean z4) {
        PipRoundVideoView pipRoundVideoView2;
        MessageObject messageObject;
        if (this.audioPlayer != null) {
            ValueAnimator valueAnimator = this.audioVolumeAnimator;
            if (valueAnimator != null) {
                valueAnimator.removeAllUpdateListeners();
                this.audioVolumeAnimator.cancel();
            }
            if (!this.audioPlayer.isPlaying() || (messageObject = this.playingMessageObject) == null || messageObject.isVoice()) {
                try {
                    this.audioPlayer.releasePlayer(true);
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
            } else {
                final VideoPlayer videoPlayer2 = this.audioPlayer;
                ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.audioVolume, 0.0f});
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(videoPlayer2) {
                    public final /* synthetic */ VideoPlayer f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        MediaController.this.lambda$cleanupPlayer$10$MediaController(this.f$1, valueAnimator);
                    }
                });
                ofFloat.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        try {
                            videoPlayer2.releasePlayer(true);
                        } catch (Exception e) {
                            FileLog.e((Throwable) e);
                        }
                    }
                });
                ofFloat.setDuration(300);
                ofFloat.start();
            }
            this.audioPlayer = null;
            Theme.unrefAudioVisualizeDrawable(this.playingMessageObject);
        } else {
            VideoPlayer videoPlayer3 = this.videoPlayer;
            if (videoPlayer3 != null) {
                this.currentAspectRatioFrameLayout = null;
                this.currentTextureViewContainer = null;
                this.currentAspectRatioFrameLayoutReady = false;
                this.isDrawingWasReady = false;
                this.currentTextureView = null;
                this.goingToShowMessageObject = null;
                if (z4) {
                    PhotoViewer.getInstance().injectVideoPlayer(this.videoPlayer);
                    MessageObject messageObject2 = this.playingMessageObject;
                    this.goingToShowMessageObject = messageObject2;
                    NotificationCenter.getInstance(messageObject2.currentAccount).postNotificationName(NotificationCenter.messagePlayingGoingToStop, this.playingMessageObject, Boolean.TRUE);
                } else {
                    long currentPosition = videoPlayer3.getCurrentPosition();
                    MessageObject messageObject3 = this.playingMessageObject;
                    if (messageObject3 != null && messageObject3.isVideo() && currentPosition > 0 && currentPosition != -9223372036854775807L) {
                        MessageObject messageObject4 = this.playingMessageObject;
                        messageObject4.audioProgressMs = (int) currentPosition;
                        NotificationCenter.getInstance(messageObject4.currentAccount).postNotificationName(NotificationCenter.messagePlayingGoingToStop, this.playingMessageObject, Boolean.FALSE);
                    }
                    this.videoPlayer.releasePlayer(true);
                    this.videoPlayer = null;
                }
                try {
                    this.baseActivity.getWindow().clearFlags(128);
                } catch (Exception e2) {
                    FileLog.e((Throwable) e2);
                }
                if (this.playingMessageObject != null && !z4) {
                    AndroidUtilities.cancelRunOnUIThread(this.setLoadingRunnable);
                    FileLoader.getInstance(this.playingMessageObject.currentAccount).removeLoadingVideo(this.playingMessageObject.getDocument(), true, false);
                }
            }
        }
        stopProgressTimer();
        this.lastProgress = 0;
        this.isPaused = false;
        if (!this.useFrontSpeaker && !SharedConfig.raiseToSpeak) {
            ChatActivity chatActivity = this.raiseChat;
            stopRaiseToEarSensors(chatActivity, false);
            this.raiseChat = chatActivity;
        }
        PowerManager.WakeLock wakeLock = this.proximityWakeLock;
        if (wakeLock != null && wakeLock.isHeld() && !this.proximityTouched) {
            this.proximityWakeLock.release();
        }
        MessageObject messageObject5 = this.playingMessageObject;
        if (messageObject5 != null) {
            if (this.downloadingCurrentMessage) {
                FileLoader.getInstance(messageObject5.currentAccount).cancelLoadFile(this.playingMessageObject.getDocument());
            }
            MessageObject messageObject6 = this.playingMessageObject;
            if (z) {
                messageObject6.resetPlayingProgress();
                NotificationCenter.getInstance(messageObject6.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, Integer.valueOf(this.playingMessageObject.getId()), 0);
            }
            this.playingMessageObject = null;
            this.downloadingCurrentMessage = false;
            if (z) {
                NotificationsController.audioManager.abandonAudioFocus(this);
                this.hasAudioFocus = 0;
                int i = -1;
                ArrayList<MessageObject> arrayList = this.voiceMessagesPlaylist;
                if (arrayList != null) {
                    if (!z3 || (i = arrayList.indexOf(messageObject6)) < 0) {
                        this.voiceMessagesPlaylist = null;
                        this.voiceMessagesPlaylistMap = null;
                    } else {
                        this.voiceMessagesPlaylist.remove(i);
                        this.voiceMessagesPlaylistMap.remove(messageObject6.getId());
                        if (this.voiceMessagesPlaylist.isEmpty()) {
                            this.voiceMessagesPlaylist = null;
                            this.voiceMessagesPlaylistMap = null;
                        }
                    }
                }
                ArrayList<MessageObject> arrayList2 = this.voiceMessagesPlaylist;
                if (arrayList2 == null || i >= arrayList2.size()) {
                    if ((messageObject6.isVoice() || messageObject6.isRoundVideo()) && messageObject6.getId() != 0) {
                        startRecordingIfFromSpeaker();
                    }
                    NotificationCenter.getInstance(messageObject6.currentAccount).postNotificationName(NotificationCenter.messagePlayingDidReset, Integer.valueOf(messageObject6.getId()), Boolean.valueOf(z2));
                    this.pipSwitchingState = 0;
                    PipRoundVideoView pipRoundVideoView3 = this.pipRoundVideoView;
                    if (pipRoundVideoView3 != null) {
                        pipRoundVideoView3.close(true);
                        this.pipRoundVideoView = null;
                    }
                } else {
                    MessageObject messageObject7 = this.voiceMessagesPlaylist.get(i);
                    playMessage(messageObject7);
                    if (!messageObject7.isRoundVideo() && (pipRoundVideoView2 = this.pipRoundVideoView) != null) {
                        pipRoundVideoView2.close(true);
                        this.pipRoundVideoView = null;
                    }
                }
            }
            if (z2) {
                ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$cleanupPlayer$10 */
    public /* synthetic */ void lambda$cleanupPlayer$10$MediaController(VideoPlayer videoPlayer2, ValueAnimator valueAnimator) {
        videoPlayer2.setVolume((this.audioFocus != 1 ? 1.0f : 0.2f) * ((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public boolean isGoingToShowMessageObject(MessageObject messageObject) {
        return this.goingToShowMessageObject == messageObject;
    }

    public void resetGoingToShowMessageObject() {
        this.goingToShowMessageObject = null;
    }

    private boolean isSamePlayingMessage(MessageObject messageObject) {
        MessageObject messageObject2 = this.playingMessageObject;
        if (messageObject2 != null && messageObject2.getDialogId() == messageObject.getDialogId() && this.playingMessageObject.getId() == messageObject.getId()) {
            if ((this.playingMessageObject.eventId == 0) == (messageObject.eventId == 0)) {
                return true;
            }
        }
        return false;
    }

    public boolean seekToProgress(MessageObject messageObject, float f) {
        if (!((this.audioPlayer == null && this.videoPlayer == null) || messageObject == null || this.playingMessageObject == null || !isSamePlayingMessage(messageObject))) {
            try {
                VideoPlayer videoPlayer2 = this.audioPlayer;
                if (videoPlayer2 != null) {
                    long duration = videoPlayer2.getDuration();
                    if (duration == -9223372036854775807L) {
                        this.seekToProgressPending = f;
                    } else {
                        this.playingMessageObject.audioProgress = f;
                        long j = (long) ((int) (((float) duration) * f));
                        this.audioPlayer.seekTo(j);
                        this.lastProgress = j;
                    }
                } else {
                    VideoPlayer videoPlayer3 = this.videoPlayer;
                    if (videoPlayer3 != null) {
                        videoPlayer3.seekTo((long) (((float) videoPlayer3.getDuration()) * f));
                    }
                }
                NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingDidSeek, Integer.valueOf(this.playingMessageObject.getId()), Float.valueOf(f));
                return true;
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }
        return false;
    }

    public long getDuration() {
        VideoPlayer videoPlayer2 = this.audioPlayer;
        if (videoPlayer2 == null) {
            return 0;
        }
        return videoPlayer2.getDuration();
    }

    public MessageObject getPlayingMessageObject() {
        return this.playingMessageObject;
    }

    public int getPlayingMessageObjectNum() {
        return this.currentPlaylistNum;
    }

    private void buildShuffledPlayList() {
        if (!this.playlist.isEmpty()) {
            ArrayList arrayList = new ArrayList(this.playlist);
            this.shuffledPlaylist.clear();
            MessageObject messageObject = this.playlist.get(this.currentPlaylistNum);
            arrayList.remove(this.currentPlaylistNum);
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                int nextInt = Utilities.random.nextInt(arrayList.size());
                this.shuffledPlaylist.add(arrayList.get(nextInt));
                arrayList.remove(nextInt);
            }
            this.shuffledPlaylist.add(messageObject);
            this.currentPlaylistNum = this.shuffledPlaylist.size() - 1;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: org.telegram.tgnet.TLRPC$TL_messages_searchGlobal} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: org.telegram.tgnet.TLRPC$TL_messages_search} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v6, resolved type: org.telegram.tgnet.TLRPC$TL_messages_searchGlobal} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v7, resolved type: org.telegram.tgnet.TLRPC$TL_messages_searchGlobal} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v8, resolved type: org.telegram.tgnet.TLRPC$TL_messages_searchGlobal} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v9, resolved type: org.telegram.tgnet.TLRPC$TL_messages_searchGlobal} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void loadMoreMusic() {
        /*
            r12 = this;
            boolean r0 = r12.loadingPlaylist
            if (r0 != 0) goto L_0x015c
            org.telegram.messenger.MessageObject r0 = r12.playingMessageObject
            if (r0 == 0) goto L_0x015c
            boolean r1 = r0.scheduled
            if (r1 != 0) goto L_0x015c
            long r0 = r0.getDialogId()
            int r1 = (int) r0
            if (r1 == 0) goto L_0x015c
            int r0 = r12.playlistClassGuid
            if (r0 != 0) goto L_0x0019
            goto L_0x015c
        L_0x0019:
            org.telegram.messenger.MediaController$PlaylistGlobalSearchParams r1 = r12.playlistGlobalSearchParams
            r2 = 0
            r3 = 0
            r5 = 1
            if (r1 == 0) goto L_0x010d
            boolean r1 = r1.endReached
            if (r1 != 0) goto L_0x010c
            java.util.ArrayList<org.telegram.messenger.MessageObject> r1 = r12.playlist
            boolean r1 = r1.isEmpty()
            if (r1 != 0) goto L_0x010c
            java.util.ArrayList<org.telegram.messenger.MessageObject> r1 = r12.playlist
            java.lang.Object r1 = r1.get(r2)
            org.telegram.messenger.MessageObject r1 = (org.telegram.messenger.MessageObject) r1
            int r1 = r1.currentAccount
            org.telegram.messenger.MediaController$PlaylistGlobalSearchParams r2 = r12.playlistGlobalSearchParams
            int r2 = r2.dialogId
            r6 = 20
            r7 = 1000(0x3e8, double:4.94E-321)
            if (r2 == 0) goto L_0x0098
            org.telegram.tgnet.TLRPC$TL_messages_search r2 = new org.telegram.tgnet.TLRPC$TL_messages_search
            r2.<init>()
            org.telegram.messenger.MediaController$PlaylistGlobalSearchParams r9 = r12.playlistGlobalSearchParams
            java.lang.String r10 = r9.query
            r2.q = r10
            r2.limit = r6
            org.telegram.ui.Adapters.FiltersView$MediaFilterData r6 = r9.filter
            if (r6 != 0) goto L_0x0058
            org.telegram.tgnet.TLRPC$TL_inputMessagesFilterEmpty r6 = new org.telegram.tgnet.TLRPC$TL_inputMessagesFilterEmpty
            r6.<init>()
            goto L_0x005a
        L_0x0058:
            org.telegram.tgnet.TLRPC$MessagesFilter r6 = r6.filter
        L_0x005a:
            r2.filter = r6
            org.telegram.messenger.AccountInstance r6 = org.telegram.messenger.AccountInstance.getInstance(r1)
            org.telegram.messenger.MessagesController r6 = r6.getMessagesController()
            org.telegram.messenger.MediaController$PlaylistGlobalSearchParams r9 = r12.playlistGlobalSearchParams
            int r9 = r9.dialogId
            org.telegram.tgnet.TLRPC$InputPeer r6 = r6.getInputPeer((int) r9)
            r2.peer = r6
            java.util.ArrayList<org.telegram.messenger.MessageObject> r6 = r12.playlist
            int r9 = r6.size()
            int r9 = r9 - r5
            java.lang.Object r6 = r6.get(r9)
            org.telegram.messenger.MessageObject r6 = (org.telegram.messenger.MessageObject) r6
            int r6 = r6.getId()
            r2.offset_id = r6
            org.telegram.messenger.MediaController$PlaylistGlobalSearchParams r6 = r12.playlistGlobalSearchParams
            long r9 = r6.minDate
            int r11 = (r9 > r3 ? 1 : (r9 == r3 ? 0 : -1))
            if (r11 <= 0) goto L_0x008d
            long r9 = r9 / r7
            int r10 = (int) r9
            r2.min_date = r10
        L_0x008d:
            long r9 = r6.maxDate
            int r6 = (r9 > r3 ? 1 : (r9 == r3 ? 0 : -1))
            if (r6 <= 0) goto L_0x00fe
            long r9 = r9 / r7
            int r3 = (int) r9
            r2.min_date = r3
            goto L_0x00fe
        L_0x0098:
            org.telegram.tgnet.TLRPC$TL_messages_searchGlobal r2 = new org.telegram.tgnet.TLRPC$TL_messages_searchGlobal
            r2.<init>()
            r2.limit = r6
            org.telegram.messenger.MediaController$PlaylistGlobalSearchParams r6 = r12.playlistGlobalSearchParams
            java.lang.String r9 = r6.query
            r2.q = r9
            org.telegram.ui.Adapters.FiltersView$MediaFilterData r6 = r6.filter
            org.telegram.tgnet.TLRPC$MessagesFilter r6 = r6.filter
            r2.filter = r6
            java.util.ArrayList<org.telegram.messenger.MessageObject> r6 = r12.playlist
            int r9 = r6.size()
            int r9 = r9 - r5
            java.lang.Object r6 = r6.get(r9)
            org.telegram.messenger.MessageObject r6 = (org.telegram.messenger.MessageObject) r6
            int r9 = r6.getId()
            r2.offset_id = r9
            org.telegram.messenger.MediaController$PlaylistGlobalSearchParams r9 = r12.playlistGlobalSearchParams
            int r10 = r9.nextSearchRate
            r2.offset_rate = r10
            int r10 = r2.flags
            r10 = r10 | r5
            r2.flags = r10
            int r9 = r9.folderId
            r2.folder_id = r9
            org.telegram.tgnet.TLRPC$Message r6 = r6.messageOwner
            org.telegram.tgnet.TLRPC$Peer r6 = r6.peer_id
            int r9 = r6.channel_id
            if (r9 == 0) goto L_0x00d7
        L_0x00d5:
            int r6 = -r9
            goto L_0x00de
        L_0x00d7:
            int r9 = r6.chat_id
            if (r9 == 0) goto L_0x00dc
            goto L_0x00d5
        L_0x00dc:
            int r6 = r6.user_id
        L_0x00de:
            org.telegram.messenger.MessagesController r9 = org.telegram.messenger.MessagesController.getInstance(r1)
            org.telegram.tgnet.TLRPC$InputPeer r6 = r9.getInputPeer((int) r6)
            r2.offset_peer = r6
            org.telegram.messenger.MediaController$PlaylistGlobalSearchParams r6 = r12.playlistGlobalSearchParams
            long r9 = r6.minDate
            int r11 = (r9 > r3 ? 1 : (r9 == r3 ? 0 : -1))
            if (r11 <= 0) goto L_0x00f4
            long r9 = r9 / r7
            int r10 = (int) r9
            r2.min_date = r10
        L_0x00f4:
            long r9 = r6.maxDate
            int r6 = (r9 > r3 ? 1 : (r9 == r3 ? 0 : -1))
            if (r6 <= 0) goto L_0x00fe
            long r9 = r9 / r7
            int r3 = (int) r9
            r2.min_date = r3
        L_0x00fe:
            r12.loadingPlaylist = r5
            org.telegram.tgnet.ConnectionsManager r3 = org.telegram.tgnet.ConnectionsManager.getInstance(r1)
            org.telegram.messenger.-$$Lambda$MediaController$tG6YqE5f_tkXdwEtFnTmspAf0XY r4 = new org.telegram.messenger.-$$Lambda$MediaController$tG6YqE5f_tkXdwEtFnTmspAf0XY
            r4.<init>(r0, r1)
            r3.sendRequest(r2, r4)
        L_0x010c:
            return
        L_0x010d:
            boolean[] r0 = r12.playlistEndReached
            boolean r1 = r0[r2]
            if (r1 != 0) goto L_0x0135
            r12.loadingPlaylist = r5
            org.telegram.messenger.MessageObject r0 = r12.playingMessageObject
            int r0 = r0.currentAccount
            org.telegram.messenger.AccountInstance r0 = org.telegram.messenger.AccountInstance.getInstance(r0)
            org.telegram.messenger.MediaDataController r3 = r0.getMediaDataController()
            org.telegram.messenger.MessageObject r0 = r12.playingMessageObject
            long r4 = r0.getDialogId()
            r6 = 50
            int[] r0 = r12.playlistMaxId
            r7 = r0[r2]
            r8 = 4
            r9 = 1
            int r10 = r12.playlistClassGuid
            r3.loadMedia(r4, r6, r7, r8, r9, r10)
            goto L_0x015c
        L_0x0135:
            long r6 = r12.playlistMergeDialogId
            int r1 = (r6 > r3 ? 1 : (r6 == r3 ? 0 : -1))
            if (r1 == 0) goto L_0x015c
            boolean r0 = r0[r5]
            if (r0 != 0) goto L_0x015c
            r12.loadingPlaylist = r5
            org.telegram.messenger.MessageObject r0 = r12.playingMessageObject
            int r0 = r0.currentAccount
            org.telegram.messenger.AccountInstance r0 = org.telegram.messenger.AccountInstance.getInstance(r0)
            org.telegram.messenger.MediaDataController r3 = r0.getMediaDataController()
            long r4 = r12.playlistMergeDialogId
            r6 = 50
            int[] r0 = r12.playlistMaxId
            r7 = r0[r2]
            r8 = 4
            r9 = 1
            int r10 = r12.playlistClassGuid
            r3.loadMedia(r4, r6, r7, r8, r9, r10)
        L_0x015c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.loadMoreMusic():void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$loadMoreMusic$12 */
    public /* synthetic */ void lambda$loadMoreMusic$12$MediaController(int i, int i2, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(i, tLRPC$TL_error, tLObject, i2) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ TLRPC$TL_error f$2;
            public final /* synthetic */ TLObject f$3;
            public final /* synthetic */ int f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                MediaController.this.lambda$null$11$MediaController(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$11 */
    public /* synthetic */ void lambda$null$11$MediaController(int i, TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, int i2) {
        PlaylistGlobalSearchParams playlistGlobalSearchParams2;
        if (this.playlistClassGuid == i && (playlistGlobalSearchParams2 = this.playlistGlobalSearchParams) != null && this.playingMessageObject != null && tLRPC$TL_error == null) {
            this.loadingPlaylist = false;
            TLRPC$messages_Messages tLRPC$messages_Messages = (TLRPC$messages_Messages) tLObject;
            playlistGlobalSearchParams2.nextSearchRate = tLRPC$messages_Messages.next_rate;
            MessagesStorage.getInstance(i2).putUsersAndChats(tLRPC$messages_Messages.users, tLRPC$messages_Messages.chats, true, true);
            MessagesController.getInstance(i2).putUsers(tLRPC$messages_Messages.users, false);
            MessagesController.getInstance(i2).putChats(tLRPC$messages_Messages.chats, false);
            int size = tLRPC$messages_Messages.messages.size();
            int i3 = 0;
            for (int i4 = 0; i4 < size; i4++) {
                MessageObject messageObject = new MessageObject(i2, tLRPC$messages_Messages.messages.get(i4), false, true);
                if (!this.playlistMap.containsKey(Integer.valueOf(messageObject.getId()))) {
                    this.playlist.add(0, messageObject);
                    this.playlistMap.put(Integer.valueOf(messageObject.getId()), messageObject);
                    i3++;
                }
            }
            sortPlaylist();
            this.loadingPlaylist = false;
            this.playlistGlobalSearchParams.endReached = this.playlist.size() == this.playlistGlobalSearchParams.totalCount;
            if (SharedConfig.shuffleMusic) {
                buildShuffledPlayList();
            }
            if (i3 != 0) {
                NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.moreMusicDidLoad, Integer.valueOf(i3));
            }
        }
    }

    public boolean setPlaylist(ArrayList<MessageObject> arrayList, MessageObject messageObject, long j, PlaylistGlobalSearchParams playlistGlobalSearchParams2) {
        return setPlaylist(arrayList, messageObject, j, true, playlistGlobalSearchParams2);
    }

    public boolean setPlaylist(ArrayList<MessageObject> arrayList, MessageObject messageObject, long j) {
        return setPlaylist(arrayList, messageObject, j, true, (PlaylistGlobalSearchParams) null);
    }

    public boolean setPlaylist(ArrayList<MessageObject> arrayList, MessageObject messageObject, long j, boolean z, PlaylistGlobalSearchParams playlistGlobalSearchParams2) {
        if (this.playingMessageObject == messageObject) {
            int indexOf = this.playlist.indexOf(messageObject);
            if (indexOf >= 0) {
                this.currentPlaylistNum = indexOf;
            }
            return playMessage(messageObject);
        }
        this.forceLoopCurrentPlaylist = !z;
        this.playlistMergeDialogId = j;
        this.playMusicAgain = !this.playlist.isEmpty();
        clearPlaylist();
        this.playlistGlobalSearchParams = playlistGlobalSearchParams2;
        boolean z2 = false;
        if (!arrayList.isEmpty() && ((int) arrayList.get(0).getDialogId()) == 0) {
            z2 = true;
        }
        long j2 = Long.MIN_VALUE;
        long j3 = Long.MAX_VALUE;
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            MessageObject messageObject2 = arrayList.get(size);
            if (messageObject2.isMusic()) {
                long idWithChannel = messageObject2.getIdWithChannel();
                if (idWithChannel > 0 || z2) {
                    j3 = Math.min(j3, idWithChannel);
                    j2 = Math.max(j2, idWithChannel);
                }
                this.playlist.add(messageObject2);
                this.playlistMap.put(Integer.valueOf(messageObject2.getId()), messageObject2);
            }
        }
        sortPlaylist();
        int indexOf2 = this.playlist.indexOf(messageObject);
        this.currentPlaylistNum = indexOf2;
        if (indexOf2 == -1) {
            clearPlaylist();
            this.currentPlaylistNum = this.playlist.size();
            this.playlist.add(messageObject);
            this.playlistMap.put(Integer.valueOf(messageObject.getId()), messageObject);
        }
        if (messageObject.isMusic() && !messageObject.scheduled) {
            if (SharedConfig.shuffleMusic) {
                buildShuffledPlayList();
            }
            if (z) {
                if (this.playlistGlobalSearchParams == null) {
                    MediaDataController.getInstance(messageObject.currentAccount).loadMusic(messageObject.getDialogId(), j3, j2);
                } else {
                    this.playlistClassGuid = ConnectionsManager.generateClassGuid();
                }
            }
        }
        return playMessage(messageObject);
    }

    private void sortPlaylist() {
        Collections.sort(this.playlist, $$Lambda$MediaController$6n54V3F7VLwRw6fcyvar_bEp1QY.INSTANCE);
    }

    static /* synthetic */ int lambda$sortPlaylist$13(MessageObject messageObject, MessageObject messageObject2) {
        int id = messageObject.getId();
        int id2 = messageObject2.getId();
        long j = messageObject.messageOwner.grouped_id;
        long j2 = messageObject2.messageOwner.grouped_id;
        if (id >= 0 || id2 >= 0) {
            if (j == 0 || j != j2) {
                return C$r8$backportedMethods$utility$Integer$2$compare.compare(id, id2);
            }
            return C$r8$backportedMethods$utility$Integer$2$compare.compare(id2, id);
        } else if (j == 0 || j != j2) {
            return C$r8$backportedMethods$utility$Integer$2$compare.compare(id2, id);
        } else {
            return C$r8$backportedMethods$utility$Integer$2$compare.compare(id, id2);
        }
    }

    public void playNextMessage() {
        playNextMessageWithoutOrder(false);
    }

    public boolean findMessageInPlaylistAndPlay(MessageObject messageObject) {
        int indexOf = this.playlist.indexOf(messageObject);
        if (indexOf == -1) {
            return playMessage(messageObject);
        }
        playMessageAtIndex(indexOf);
        return true;
    }

    public void playMessageAtIndex(int i) {
        int i2 = this.currentPlaylistNum;
        if (i2 >= 0 && i2 < this.playlist.size()) {
            this.currentPlaylistNum = i;
            this.playMusicAgain = true;
            MessageObject messageObject = this.playlist.get(i);
            if (this.playingMessageObject != null && !isSamePlayingMessage(messageObject)) {
                this.playingMessageObject.resetPlayingProgress();
            }
            playMessage(messageObject);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0108  */
    /* JADX WARNING: Removed duplicated region for block: B:63:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void playNextMessageWithoutOrder(boolean r8) {
        /*
            r7 = this;
            boolean r0 = org.telegram.messenger.SharedConfig.shuffleMusic
            if (r0 == 0) goto L_0x0007
            java.util.ArrayList<org.telegram.messenger.MessageObject> r0 = r7.shuffledPlaylist
            goto L_0x0009
        L_0x0007:
            java.util.ArrayList<org.telegram.messenger.MessageObject> r0 = r7.playlist
        L_0x0009:
            r1 = 0
            r2 = 2
            r3 = 0
            r4 = 1
            if (r8 == 0) goto L_0x0032
            int r5 = org.telegram.messenger.SharedConfig.repeatMode
            if (r5 == r2) goto L_0x001b
            if (r5 != r4) goto L_0x0032
            int r5 = r0.size()
            if (r5 != r4) goto L_0x0032
        L_0x001b:
            boolean r5 = r7.forceLoopCurrentPlaylist
            if (r5 != 0) goto L_0x0032
            r7.cleanupPlayer(r3, r3)
            int r8 = r7.currentPlaylistNum
            java.lang.Object r8 = r0.get(r8)
            org.telegram.messenger.MessageObject r8 = (org.telegram.messenger.MessageObject) r8
            r8.audioProgress = r1
            r8.audioProgressSec = r3
            r7.playMessage(r8)
            return
        L_0x0032:
            boolean r5 = org.telegram.messenger.SharedConfig.playOrderReversed
            if (r5 == 0) goto L_0x0044
            int r5 = r7.currentPlaylistNum
            int r5 = r5 + r4
            r7.currentPlaylistNum = r5
            int r6 = r0.size()
            if (r5 < r6) goto L_0x0054
            r7.currentPlaylistNum = r3
            goto L_0x0052
        L_0x0044:
            int r5 = r7.currentPlaylistNum
            int r5 = r5 - r4
            r7.currentPlaylistNum = r5
            if (r5 >= 0) goto L_0x0054
            int r5 = r0.size()
            int r5 = r5 - r4
            r7.currentPlaylistNum = r5
        L_0x0052:
            r5 = 1
            goto L_0x0055
        L_0x0054:
            r5 = 0
        L_0x0055:
            if (r5 == 0) goto L_0x00fd
            if (r8 == 0) goto L_0x00fd
            int r8 = org.telegram.messenger.SharedConfig.repeatMode
            if (r8 != 0) goto L_0x00fd
            boolean r8 = r7.forceLoopCurrentPlaylist
            if (r8 != 0) goto L_0x00fd
            org.telegram.ui.Components.VideoPlayer r8 = r7.audioPlayer
            if (r8 != 0) goto L_0x0069
            org.telegram.ui.Components.VideoPlayer r0 = r7.videoPlayer
            if (r0 == 0) goto L_0x00fc
        L_0x0069:
            r0 = 0
            if (r8 == 0) goto L_0x007c
            r8.releasePlayer(r4)     // Catch:{ Exception -> 0x0070 }
            goto L_0x0074
        L_0x0070:
            r8 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r8)
        L_0x0074:
            r7.audioPlayer = r0
            org.telegram.messenger.MessageObject r8 = r7.playingMessageObject
            org.telegram.ui.ActionBar.Theme.unrefAudioVisualizeDrawable(r8)
            goto L_0x00b3
        L_0x007c:
            org.telegram.ui.Components.VideoPlayer r8 = r7.videoPlayer
            if (r8 == 0) goto L_0x00b3
            r7.currentAspectRatioFrameLayout = r0
            r7.currentTextureViewContainer = r0
            r7.currentAspectRatioFrameLayoutReady = r3
            r7.currentTextureView = r0
            r8.releasePlayer(r4)
            r7.videoPlayer = r0
            android.app.Activity r8 = r7.baseActivity     // Catch:{ Exception -> 0x0099 }
            android.view.Window r8 = r8.getWindow()     // Catch:{ Exception -> 0x0099 }
            r0 = 128(0x80, float:1.794E-43)
            r8.clearFlags(r0)     // Catch:{ Exception -> 0x0099 }
            goto L_0x009d
        L_0x0099:
            r8 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r8)
        L_0x009d:
            java.lang.Runnable r8 = r7.setLoadingRunnable
            org.telegram.messenger.AndroidUtilities.cancelRunOnUIThread(r8)
            org.telegram.messenger.MessageObject r8 = r7.playingMessageObject
            int r8 = r8.currentAccount
            org.telegram.messenger.FileLoader r8 = org.telegram.messenger.FileLoader.getInstance(r8)
            org.telegram.messenger.MessageObject r0 = r7.playingMessageObject
            org.telegram.tgnet.TLRPC$Document r0 = r0.getDocument()
            r8.removeLoadingVideo(r0, r4, r3)
        L_0x00b3:
            r7.stopProgressTimer()
            r5 = 0
            r7.lastProgress = r5
            r7.isPaused = r4
            org.telegram.messenger.MessageObject r8 = r7.playingMessageObject
            r8.audioProgress = r1
            r8.audioProgressSec = r3
            int r8 = r8.currentAccount
            org.telegram.messenger.NotificationCenter r8 = org.telegram.messenger.NotificationCenter.getInstance(r8)
            int r0 = org.telegram.messenger.NotificationCenter.messagePlayingProgressDidChanged
            java.lang.Object[] r1 = new java.lang.Object[r2]
            org.telegram.messenger.MessageObject r2 = r7.playingMessageObject
            int r2 = r2.getId()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r1[r3] = r2
            java.lang.Integer r2 = java.lang.Integer.valueOf(r3)
            r1[r4] = r2
            r8.postNotificationName(r0, r1)
            org.telegram.messenger.MessageObject r8 = r7.playingMessageObject
            int r8 = r8.currentAccount
            org.telegram.messenger.NotificationCenter r8 = org.telegram.messenger.NotificationCenter.getInstance(r8)
            int r0 = org.telegram.messenger.NotificationCenter.messagePlayingPlayStateChanged
            java.lang.Object[] r1 = new java.lang.Object[r4]
            org.telegram.messenger.MessageObject r2 = r7.playingMessageObject
            int r2 = r2.getId()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r1[r3] = r2
            r8.postNotificationName(r0, r1)
        L_0x00fc:
            return
        L_0x00fd:
            int r8 = r7.currentPlaylistNum
            if (r8 < 0) goto L_0x011c
            int r1 = r0.size()
            if (r8 < r1) goto L_0x0108
            goto L_0x011c
        L_0x0108:
            org.telegram.messenger.MessageObject r8 = r7.playingMessageObject
            if (r8 == 0) goto L_0x010f
            r8.resetPlayingProgress()
        L_0x010f:
            r7.playMusicAgain = r4
            int r8 = r7.currentPlaylistNum
            java.lang.Object r8 = r0.get(r8)
            org.telegram.messenger.MessageObject r8 = (org.telegram.messenger.MessageObject) r8
            r7.playMessage(r8)
        L_0x011c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.playNextMessageWithoutOrder(boolean):void");
    }

    public void playPreviousMessage() {
        int i;
        ArrayList<MessageObject> arrayList = SharedConfig.shuffleMusic ? this.shuffledPlaylist : this.playlist;
        if (!arrayList.isEmpty() && (i = this.currentPlaylistNum) >= 0 && i < arrayList.size()) {
            MessageObject messageObject = arrayList.get(this.currentPlaylistNum);
            if (messageObject.audioProgressSec > 10) {
                seekToProgress(messageObject, 0.0f);
                return;
            }
            if (SharedConfig.playOrderReversed) {
                int i2 = this.currentPlaylistNum - 1;
                this.currentPlaylistNum = i2;
                if (i2 < 0) {
                    this.currentPlaylistNum = arrayList.size() - 1;
                }
            } else {
                int i3 = this.currentPlaylistNum + 1;
                this.currentPlaylistNum = i3;
                if (i3 >= arrayList.size()) {
                    this.currentPlaylistNum = 0;
                }
            }
            int i4 = this.currentPlaylistNum;
            if (i4 >= 0 && i4 < arrayList.size()) {
                this.playMusicAgain = true;
                playMessage(arrayList.get(this.currentPlaylistNum));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkIsNextMediaFileDownloaded() {
        MessageObject messageObject = this.playingMessageObject;
        if (messageObject != null && messageObject.isMusic()) {
            checkIsNextMusicFileDownloaded(this.playingMessageObject.currentAccount);
        }
    }

    private void checkIsNextVoiceFileDownloaded(int i) {
        File file;
        ArrayList<MessageObject> arrayList = this.voiceMessagesPlaylist;
        if (arrayList != null && arrayList.size() >= 2) {
            MessageObject messageObject = this.voiceMessagesPlaylist.get(1);
            String str = messageObject.messageOwner.attachPath;
            File file2 = null;
            if (str != null && str.length() > 0) {
                File file3 = new File(messageObject.messageOwner.attachPath);
                if (file3.exists()) {
                    file2 = file3;
                }
            }
            if (file2 != null) {
                file = file2;
            } else {
                file = FileLoader.getPathToMessage(messageObject.messageOwner);
            }
            if (file != null) {
                boolean exists = file.exists();
            }
            if (file != null && file != file2 && !file.exists()) {
                FileLoader.getInstance(i).loadFile(messageObject.getDocument(), messageObject, 0, 0);
            }
        }
    }

    private void checkIsNextMusicFileDownloaded(int i) {
        int i2;
        File file;
        if (DownloadController.getInstance(i).canDownloadNextTrack()) {
            ArrayList<MessageObject> arrayList = SharedConfig.shuffleMusic ? this.shuffledPlaylist : this.playlist;
            if (arrayList != null && arrayList.size() >= 2) {
                if (SharedConfig.playOrderReversed) {
                    i2 = this.currentPlaylistNum + 1;
                    if (i2 >= arrayList.size()) {
                        i2 = 0;
                    }
                } else {
                    i2 = this.currentPlaylistNum - 1;
                    if (i2 < 0) {
                        i2 = arrayList.size() - 1;
                    }
                }
                if (i2 >= 0 && i2 < arrayList.size()) {
                    MessageObject messageObject = arrayList.get(i2);
                    File file2 = null;
                    if (!TextUtils.isEmpty(messageObject.messageOwner.attachPath)) {
                        File file3 = new File(messageObject.messageOwner.attachPath);
                        if (file3.exists()) {
                            file2 = file3;
                        }
                    }
                    if (file2 != null) {
                        file = file2;
                    } else {
                        file = FileLoader.getPathToMessage(messageObject.messageOwner);
                    }
                    if (file != null) {
                        boolean exists = file.exists();
                    }
                    if (file != null && file != file2 && !file.exists() && messageObject.isMusic()) {
                        FileLoader.getInstance(i).loadFile(messageObject.getDocument(), messageObject, 0, 0);
                    }
                }
            }
        }
    }

    public void setVoiceMessagesPlaylist(ArrayList<MessageObject> arrayList, boolean z) {
        this.voiceMessagesPlaylist = arrayList;
        if (arrayList != null) {
            this.voiceMessagesPlaylistUnread = z;
            this.voiceMessagesPlaylistMap = new SparseArray<>();
            for (int i = 0; i < this.voiceMessagesPlaylist.size(); i++) {
                MessageObject messageObject = this.voiceMessagesPlaylist.get(i);
                this.voiceMessagesPlaylistMap.put(messageObject.getId(), messageObject);
            }
        }
    }

    private void checkAudioFocus(MessageObject messageObject) {
        int i;
        int i2;
        if (messageObject.isVoice() || messageObject.isRoundVideo()) {
            i = this.useFrontSpeaker ? 3 : 2;
        } else {
            i = 1;
        }
        if (this.hasAudioFocus != i) {
            this.hasAudioFocus = i;
            if (i == 3) {
                i2 = NotificationsController.audioManager.requestAudioFocus(this, 0, 1);
            } else {
                i2 = NotificationsController.audioManager.requestAudioFocus(this, 3, i == 2 ? 3 : 1);
            }
            if (i2 == 1) {
                this.audioFocus = 2;
            }
        }
    }

    public void setCurrentVideoVisible(boolean z) {
        AspectRatioFrameLayout aspectRatioFrameLayout = this.currentAspectRatioFrameLayout;
        if (aspectRatioFrameLayout != null) {
            if (z) {
                PipRoundVideoView pipRoundVideoView2 = this.pipRoundVideoView;
                if (pipRoundVideoView2 != null) {
                    this.pipSwitchingState = 2;
                    pipRoundVideoView2.close(true);
                    this.pipRoundVideoView = null;
                } else if (aspectRatioFrameLayout != null) {
                    if (aspectRatioFrameLayout.getParent() == null) {
                        this.currentTextureViewContainer.addView(this.currentAspectRatioFrameLayout);
                    }
                    this.videoPlayer.setTextureView(this.currentTextureView);
                }
            } else if (aspectRatioFrameLayout.getParent() != null) {
                this.pipSwitchingState = 1;
                this.currentTextureViewContainer.removeView(this.currentAspectRatioFrameLayout);
            } else {
                if (this.pipRoundVideoView == null) {
                    try {
                        PipRoundVideoView pipRoundVideoView3 = new PipRoundVideoView();
                        this.pipRoundVideoView = pipRoundVideoView3;
                        pipRoundVideoView3.show(this.baseActivity, new Runnable() {
                            public final void run() {
                                MediaController.this.lambda$setCurrentVideoVisible$14$MediaController();
                            }
                        });
                    } catch (Exception unused) {
                        this.pipRoundVideoView = null;
                    }
                }
                PipRoundVideoView pipRoundVideoView4 = this.pipRoundVideoView;
                if (pipRoundVideoView4 != null) {
                    this.videoPlayer.setTextureView(pipRoundVideoView4.getTextureView());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setCurrentVideoVisible$14 */
    public /* synthetic */ void lambda$setCurrentVideoVisible$14$MediaController() {
        cleanupPlayer(true, true);
    }

    public void setTextureView(TextureView textureView, AspectRatioFrameLayout aspectRatioFrameLayout, FrameLayout frameLayout, boolean z) {
        if (textureView != null) {
            boolean z2 = true;
            if (!z && this.currentTextureView == textureView) {
                this.pipSwitchingState = 1;
                this.currentTextureView = null;
                this.currentAspectRatioFrameLayout = null;
                this.currentTextureViewContainer = null;
            } else if (this.videoPlayer != null && textureView != this.currentTextureView) {
                if (aspectRatioFrameLayout == null || !aspectRatioFrameLayout.isDrawingReady()) {
                    z2 = false;
                }
                this.isDrawingWasReady = z2;
                this.currentTextureView = textureView;
                PipRoundVideoView pipRoundVideoView2 = this.pipRoundVideoView;
                if (pipRoundVideoView2 != null) {
                    this.videoPlayer.setTextureView(pipRoundVideoView2.getTextureView());
                } else {
                    this.videoPlayer.setTextureView(textureView);
                }
                this.currentAspectRatioFrameLayout = aspectRatioFrameLayout;
                this.currentTextureViewContainer = frameLayout;
                if (this.currentAspectRatioFrameLayoutReady && aspectRatioFrameLayout != null && aspectRatioFrameLayout != null) {
                    aspectRatioFrameLayout.setAspectRatio(this.currentAspectRatioFrameLayoutRatio, this.currentAspectRatioFrameLayoutRotation);
                }
            }
        }
    }

    public void setBaseActivity(Activity activity, boolean z) {
        if (z) {
            this.baseActivity = activity;
        } else if (this.baseActivity == activity) {
            this.baseActivity = null;
        }
    }

    public void setFeedbackView(View view, boolean z) {
        if (z) {
            this.feedbackView = view;
        } else if (this.feedbackView == view) {
            this.feedbackView = null;
        }
    }

    public void setPlaybackSpeed(boolean z, float f) {
        if (z) {
            if (this.currentMusicPlaybackSpeed >= 6.0f && f == 1.0f && this.playingMessageObject != null) {
                this.audioPlayer.pause();
                MessageObject messageObject = this.playingMessageObject;
                AndroidUtilities.runOnUIThread(new Runnable(messageObject, messageObject.audioProgress) {
                    public final /* synthetic */ MessageObject f$1;
                    public final /* synthetic */ float f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        MediaController.this.lambda$setPlaybackSpeed$15$MediaController(this.f$1, this.f$2);
                    }
                }, 50);
            }
            this.currentMusicPlaybackSpeed = f;
        } else {
            this.currentPlaybackSpeed = f;
        }
        VideoPlayer videoPlayer2 = this.audioPlayer;
        if (videoPlayer2 != null) {
            videoPlayer2.setPlaybackSpeed(f);
        } else {
            VideoPlayer videoPlayer3 = this.videoPlayer;
            if (videoPlayer3 != null) {
                videoPlayer3.setPlaybackSpeed(f);
            }
        }
        MessagesController.getGlobalMainSettings().edit().putFloat(z ? "musicPlaybackSpeed" : "playbackSpeed", f).commit();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.messagePlayingSpeedChanged, new Object[0]);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setPlaybackSpeed$15 */
    public /* synthetic */ void lambda$setPlaybackSpeed$15$MediaController(MessageObject messageObject, float f) {
        if (this.audioPlayer != null && this.playingMessageObject != null && !this.isPaused) {
            if (isSamePlayingMessage(messageObject)) {
                seekToProgress(this.playingMessageObject, f);
            }
            this.audioPlayer.play();
        }
    }

    public float getPlaybackSpeed(boolean z) {
        return z ? this.currentMusicPlaybackSpeed : this.currentPlaybackSpeed;
    }

    /* access modifiers changed from: private */
    public void updateVideoState(MessageObject messageObject, int[] iArr, boolean z, boolean z2, int i) {
        MessageObject messageObject2;
        if (this.videoPlayer != null) {
            if (i == 4 || i == 1) {
                try {
                    this.baseActivity.getWindow().clearFlags(128);
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
            } else {
                try {
                    this.baseActivity.getWindow().addFlags(128);
                } catch (Exception e2) {
                    FileLog.e((Throwable) e2);
                }
            }
            if (i == 3) {
                this.playerWasReady = true;
                MessageObject messageObject3 = this.playingMessageObject;
                if (messageObject3 != null && (messageObject3.isVideo() || this.playingMessageObject.isRoundVideo())) {
                    AndroidUtilities.cancelRunOnUIThread(this.setLoadingRunnable);
                    FileLoader.getInstance(messageObject.currentAccount).removeLoadingVideo(this.playingMessageObject.getDocument(), true, false);
                }
                this.currentAspectRatioFrameLayoutReady = true;
            } else if (i == 2) {
                if (z2 && (messageObject2 = this.playingMessageObject) != null) {
                    if (!messageObject2.isVideo() && !this.playingMessageObject.isRoundVideo()) {
                        return;
                    }
                    if (this.playerWasReady) {
                        this.setLoadingRunnable.run();
                    } else {
                        AndroidUtilities.runOnUIThread(this.setLoadingRunnable, 1000);
                    }
                }
            } else if (this.videoPlayer.isPlaying() && i == 4) {
                if (!this.playingMessageObject.isVideo() || z || (iArr != null && iArr[0] >= 4)) {
                    cleanupPlayer(true, true, true, false);
                    return;
                }
                this.videoPlayer.seekTo(0);
                if (iArr != null) {
                    iArr[0] = iArr[0] + 1;
                }
            }
        }
    }

    public void injectVideoPlayer(VideoPlayer videoPlayer2, MessageObject messageObject) {
        if (videoPlayer2 != null && messageObject != null) {
            FileLoader.getInstance(messageObject.currentAccount).setLoadingVideoForPlayer(messageObject.getDocument(), true);
            this.playerWasReady = false;
            clearPlaylist();
            this.videoPlayer = videoPlayer2;
            this.playingMessageObject = messageObject;
            final int i = this.playerNum + 1;
            this.playerNum = i;
            final MessageObject messageObject2 = messageObject;
            videoPlayer2.setDelegate(new VideoPlayer.VideoPlayerDelegate((int[]) null, true) {
                public /* bridge */ /* synthetic */ void onRenderedFirstFrame(AnalyticsListener.EventTime eventTime) {
                    VideoPlayer.VideoPlayerDelegate.CC.$default$onRenderedFirstFrame(this, eventTime);
                }

                public /* bridge */ /* synthetic */ void onSeekFinished(AnalyticsListener.EventTime eventTime) {
                    VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekFinished(this, eventTime);
                }

                public /* bridge */ /* synthetic */ void onSeekStarted(AnalyticsListener.EventTime eventTime) {
                    VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekStarted(this, eventTime);
                }

                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                }

                public void onStateChanged(boolean z, int i) {
                    if (i == MediaController.this.playerNum) {
                        MediaController.this.updateVideoState(messageObject2, null, true, z, i);
                    }
                }

                public void onError(VideoPlayer videoPlayer, Exception exc) {
                    FileLog.e((Throwable) exc);
                }

                public void onVideoSizeChanged(int i, int i2, int i3, float f) {
                    int unused = MediaController.this.currentAspectRatioFrameLayoutRotation = i3;
                    if (!(i3 == 90 || i3 == 270)) {
                        int i4 = i2;
                        i2 = i;
                        i = i4;
                    }
                    float unused2 = MediaController.this.currentAspectRatioFrameLayoutRatio = i == 0 ? 1.0f : (((float) i2) * f) / ((float) i);
                    if (MediaController.this.currentAspectRatioFrameLayout != null) {
                        MediaController.this.currentAspectRatioFrameLayout.setAspectRatio(MediaController.this.currentAspectRatioFrameLayoutRatio, MediaController.this.currentAspectRatioFrameLayoutRotation);
                    }
                }

                public void onRenderedFirstFrame() {
                    if (MediaController.this.currentAspectRatioFrameLayout != null && !MediaController.this.currentAspectRatioFrameLayout.isDrawingReady()) {
                        boolean unused = MediaController.this.isDrawingWasReady = true;
                        MediaController.this.currentAspectRatioFrameLayout.setDrawingReady(true);
                        MediaController.this.currentTextureViewContainer.setTag(1);
                    }
                }

                public boolean onSurfaceDestroyed(SurfaceTexture surfaceTexture) {
                    if (MediaController.this.videoPlayer == null) {
                        return false;
                    }
                    if (MediaController.this.pipSwitchingState == 2) {
                        if (MediaController.this.currentAspectRatioFrameLayout != null) {
                            if (MediaController.this.isDrawingWasReady) {
                                MediaController.this.currentAspectRatioFrameLayout.setDrawingReady(true);
                            }
                            if (MediaController.this.currentAspectRatioFrameLayout.getParent() == null) {
                                MediaController.this.currentTextureViewContainer.addView(MediaController.this.currentAspectRatioFrameLayout);
                            }
                            if (MediaController.this.currentTextureView.getSurfaceTexture() != surfaceTexture) {
                                MediaController.this.currentTextureView.setSurfaceTexture(surfaceTexture);
                            }
                            MediaController.this.videoPlayer.setTextureView(MediaController.this.currentTextureView);
                        }
                        int unused = MediaController.this.pipSwitchingState = 0;
                        return true;
                    } else if (MediaController.this.pipSwitchingState == 1) {
                        if (MediaController.this.baseActivity != null) {
                            if (MediaController.this.pipRoundVideoView == null) {
                                try {
                                    PipRoundVideoView unused2 = MediaController.this.pipRoundVideoView = new PipRoundVideoView();
                                    MediaController.this.pipRoundVideoView.show(MediaController.this.baseActivity, new Runnable() {
                                        public final void run() {
                                            MediaController.AnonymousClass7.this.lambda$onSurfaceDestroyed$0$MediaController$7();
                                        }
                                    });
                                } catch (Exception unused3) {
                                    PipRoundVideoView unused4 = MediaController.this.pipRoundVideoView = null;
                                }
                            }
                            if (MediaController.this.pipRoundVideoView != null) {
                                if (MediaController.this.pipRoundVideoView.getTextureView().getSurfaceTexture() != surfaceTexture) {
                                    MediaController.this.pipRoundVideoView.getTextureView().setSurfaceTexture(surfaceTexture);
                                }
                                MediaController.this.videoPlayer.setTextureView(MediaController.this.pipRoundVideoView.getTextureView());
                            }
                        }
                        int unused5 = MediaController.this.pipSwitchingState = 0;
                        return true;
                    } else if (!PhotoViewer.hasInstance() || !PhotoViewer.getInstance().isInjectingVideoPlayer()) {
                        return false;
                    } else {
                        PhotoViewer.getInstance().injectVideoPlayerSurface(surfaceTexture);
                        return true;
                    }
                }

                /* access modifiers changed from: private */
                /* renamed from: lambda$onSurfaceDestroyed$0 */
                public /* synthetic */ void lambda$onSurfaceDestroyed$0$MediaController$7() {
                    MediaController.this.cleanupPlayer(true, true);
                }
            });
            this.currentAspectRatioFrameLayoutReady = false;
            TextureView textureView = this.currentTextureView;
            if (textureView != null) {
                this.videoPlayer.setTextureView(textureView);
            }
            checkAudioFocus(messageObject);
            setPlayerVolume();
            this.isPaused = false;
            this.lastProgress = 0;
            this.playingMessageObject = messageObject;
            if (!SharedConfig.raiseToSpeak) {
                startRaiseToEarSensors(this.raiseChat);
            }
            startProgressTimer(this.playingMessageObject);
            NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingDidStart, messageObject);
        }
    }

    public void playEmojiSound(AccountInstance accountInstance, String str, MessagesController.EmojiSound emojiSound, boolean z) {
        if (emojiSound != null) {
            Utilities.stageQueue.postRunnable(new Runnable(emojiSound, accountInstance, z) {
                public final /* synthetic */ MessagesController.EmojiSound f$1;
                public final /* synthetic */ AccountInstance f$2;
                public final /* synthetic */ boolean f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    MediaController.this.lambda$playEmojiSound$18$MediaController(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$playEmojiSound$18 */
    public /* synthetic */ void lambda$playEmojiSound$18$MediaController(MessagesController.EmojiSound emojiSound, AccountInstance accountInstance, boolean z) {
        TLRPC$TL_document tLRPC$TL_document = new TLRPC$TL_document();
        tLRPC$TL_document.access_hash = emojiSound.accessHash;
        tLRPC$TL_document.id = emojiSound.id;
        tLRPC$TL_document.mime_type = "sound/ogg";
        tLRPC$TL_document.file_reference = emojiSound.fileReference;
        tLRPC$TL_document.dc_id = accountInstance.getConnectionsManager().getCurrentDatacenterId();
        File pathToAttach = FileLoader.getPathToAttach(tLRPC$TL_document, true);
        if (!pathToAttach.exists()) {
            AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_document) {
                public final /* synthetic */ TLRPC$Document f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    AccountInstance.this.getFileLoader().loadFile(this.f$1, (Object) null, 1, 1);
                }
            });
        } else if (!z) {
            AndroidUtilities.runOnUIThread(new Runnable(pathToAttach) {
                public final /* synthetic */ File f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MediaController.this.lambda$null$16$MediaController(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$16 */
    public /* synthetic */ void lambda$null$16$MediaController(File file) {
        try {
            final int i = this.emojiSoundPlayerNum + 1;
            this.emojiSoundPlayerNum = i;
            VideoPlayer videoPlayer2 = this.emojiSoundPlayer;
            if (videoPlayer2 != null) {
                videoPlayer2.releasePlayer(true);
            }
            VideoPlayer videoPlayer3 = new VideoPlayer(false);
            this.emojiSoundPlayer = videoPlayer3;
            videoPlayer3.setDelegate(new VideoPlayer.VideoPlayerDelegate() {
                public void onError(VideoPlayer videoPlayer, Exception exc) {
                }

                public void onRenderedFirstFrame() {
                }

                public /* bridge */ /* synthetic */ void onRenderedFirstFrame(AnalyticsListener.EventTime eventTime) {
                    VideoPlayer.VideoPlayerDelegate.CC.$default$onRenderedFirstFrame(this, eventTime);
                }

                public /* bridge */ /* synthetic */ void onSeekFinished(AnalyticsListener.EventTime eventTime) {
                    VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekFinished(this, eventTime);
                }

                public /* bridge */ /* synthetic */ void onSeekStarted(AnalyticsListener.EventTime eventTime) {
                    VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekStarted(this, eventTime);
                }

                public boolean onSurfaceDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }

                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                }

                public void onVideoSizeChanged(int i, int i2, int i3, float f) {
                }

                public void onStateChanged(boolean z, int i) {
                    AndroidUtilities.runOnUIThread(new Runnable(i, i) {
                        public final /* synthetic */ int f$1;
                        public final /* synthetic */ int f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            MediaController.AnonymousClass8.this.lambda$onStateChanged$0$MediaController$8(this.f$1, this.f$2);
                        }
                    });
                }

                /* access modifiers changed from: private */
                /* renamed from: lambda$onStateChanged$0 */
                public /* synthetic */ void lambda$onStateChanged$0$MediaController$8(int i, int i2) {
                    if (i == MediaController.this.emojiSoundPlayerNum && i2 == 4 && MediaController.this.emojiSoundPlayer != null) {
                        try {
                            MediaController.this.emojiSoundPlayer.releasePlayer(true);
                            VideoPlayer unused = MediaController.this.emojiSoundPlayer = null;
                        } catch (Exception e) {
                            FileLog.e((Throwable) e);
                        }
                    }
                }
            });
            this.emojiSoundPlayer.preparePlayer(Uri.fromFile(file), "other");
            this.emojiSoundPlayer.setStreamType(3);
            this.emojiSoundPlayer.play();
        } catch (Exception e) {
            FileLog.e((Throwable) e);
            VideoPlayer videoPlayer4 = this.emojiSoundPlayer;
            if (videoPlayer4 != null) {
                videoPlayer4.releasePlayer(true);
                this.emojiSoundPlayer = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$playMessage$19 */
    public /* synthetic */ void lambda$playMessage$19$MediaController() {
        cleanupPlayer(true, true);
    }

    public boolean playMessage(MessageObject messageObject) {
        boolean z;
        File file;
        File file2;
        PowerManager.WakeLock wakeLock;
        final MessageObject messageObject2 = messageObject;
        if (messageObject2 == null) {
            return false;
        }
        if (!(this.audioPlayer == null && this.videoPlayer == null) && isSamePlayingMessage(messageObject)) {
            if (this.isPaused) {
                resumeAudio(messageObject);
            }
            if (!SharedConfig.raiseToSpeak) {
                startRaiseToEarSensors(this.raiseChat);
            }
            return true;
        }
        if (!messageObject.isOut() && messageObject.isContentUnread()) {
            MessagesController.getInstance(messageObject2.currentAccount).markMessageContentAsRead(messageObject2);
        }
        boolean z2 = this.playMusicAgain;
        boolean z3 = !z2;
        MessageObject messageObject3 = this.playingMessageObject;
        if (messageObject3 != null) {
            if (!z2) {
                messageObject3.resetPlayingProgress();
            }
            z3 = false;
        }
        cleanupPlayer(z3, false);
        this.shouldSavePositionForCurrentAudio = null;
        this.lastSaveTime = 0;
        this.playMusicAgain = false;
        this.seekToProgressPending = 0.0f;
        String str = messageObject2.messageOwner.attachPath;
        if (str == null || str.length() <= 0) {
            file = null;
            z = false;
        } else {
            file = new File(messageObject2.messageOwner.attachPath);
            z = file.exists();
            if (!z) {
                file = null;
            }
        }
        if (file != null) {
            file2 = file;
        } else {
            file2 = FileLoader.getPathToMessage(messageObject2.messageOwner);
        }
        boolean z4 = SharedConfig.streamMedia && (messageObject.isMusic() || messageObject.isRoundVideo() || (messageObject.isVideo() && messageObject.canStreamVideo())) && ((int) messageObject.getDialogId()) != 0;
        if (file2 == null || file2 == file || (z = file2.exists()) || z4) {
            boolean z5 = z;
            this.downloadingCurrentMessage = false;
            if (messageObject.isMusic()) {
                checkIsNextMusicFileDownloaded(messageObject2.currentAccount);
            } else {
                checkIsNextVoiceFileDownloaded(messageObject2.currentAccount);
            }
            AspectRatioFrameLayout aspectRatioFrameLayout = this.currentAspectRatioFrameLayout;
            if (aspectRatioFrameLayout != null) {
                this.isDrawingWasReady = false;
                aspectRatioFrameLayout.setDrawingReady(false);
            }
            boolean isVideo = messageObject.isVideo();
            String str2 = "&reference=";
            String str3 = "&name=";
            if (messageObject.isRoundVideo() || isVideo) {
                String str4 = str3;
                String str5 = "&rid=";
                String str6 = str4;
                FileLoader.getInstance(messageObject2.currentAccount).setLoadingVideoForPlayer(messageObject.getDocument(), true);
                this.playerWasReady = false;
                boolean z6 = !isVideo || (messageObject2.messageOwner.peer_id.channel_id == 0 && messageObject2.audioProgress <= 0.1f);
                int[] iArr = (!isVideo || messageObject.getDuration() > 30) ? null : new int[]{1};
                clearPlaylist();
                VideoPlayer videoPlayer2 = new VideoPlayer();
                this.videoPlayer = videoPlayer2;
                final int i = this.playerNum + 1;
                this.playerNum = i;
                String str7 = "UTF-8";
                AnonymousClass9 r10 = r1;
                String str8 = str5;
                String str9 = "&mime=";
                String str10 = str6;
                VideoPlayer videoPlayer3 = videoPlayer2;
                final MessageObject messageObject4 = messageObject;
                String str11 = str2;
                String str12 = "&size=";
                String str13 = "other";
                final int[] iArr2 = iArr;
                String str14 = "&dc=";
                File file3 = file2;
                final boolean z7 = z6;
                AnonymousClass9 r1 = new VideoPlayer.VideoPlayerDelegate() {
                    public /* bridge */ /* synthetic */ void onRenderedFirstFrame(AnalyticsListener.EventTime eventTime) {
                        VideoPlayer.VideoPlayerDelegate.CC.$default$onRenderedFirstFrame(this, eventTime);
                    }

                    public /* bridge */ /* synthetic */ void onSeekFinished(AnalyticsListener.EventTime eventTime) {
                        VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekFinished(this, eventTime);
                    }

                    public /* bridge */ /* synthetic */ void onSeekStarted(AnalyticsListener.EventTime eventTime) {
                        VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekStarted(this, eventTime);
                    }

                    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                    }

                    public void onStateChanged(boolean z, int i) {
                        if (i == MediaController.this.playerNum) {
                            MediaController.this.updateVideoState(messageObject4, iArr2, z7, z, i);
                        }
                    }

                    public void onError(VideoPlayer videoPlayer, Exception exc) {
                        FileLog.e((Throwable) exc);
                    }

                    public void onVideoSizeChanged(int i, int i2, int i3, float f) {
                        int unused = MediaController.this.currentAspectRatioFrameLayoutRotation = i3;
                        if (!(i3 == 90 || i3 == 270)) {
                            int i4 = i2;
                            i2 = i;
                            i = i4;
                        }
                        float unused2 = MediaController.this.currentAspectRatioFrameLayoutRatio = i == 0 ? 1.0f : (((float) i2) * f) / ((float) i);
                        if (MediaController.this.currentAspectRatioFrameLayout != null) {
                            MediaController.this.currentAspectRatioFrameLayout.setAspectRatio(MediaController.this.currentAspectRatioFrameLayoutRatio, MediaController.this.currentAspectRatioFrameLayoutRotation);
                        }
                    }

                    public void onRenderedFirstFrame() {
                        if (MediaController.this.currentAspectRatioFrameLayout != null && !MediaController.this.currentAspectRatioFrameLayout.isDrawingReady()) {
                            boolean unused = MediaController.this.isDrawingWasReady = true;
                            MediaController.this.currentAspectRatioFrameLayout.setDrawingReady(true);
                            MediaController.this.currentTextureViewContainer.setTag(1);
                        }
                    }

                    public boolean onSurfaceDestroyed(SurfaceTexture surfaceTexture) {
                        if (MediaController.this.videoPlayer == null) {
                            return false;
                        }
                        if (MediaController.this.pipSwitchingState == 2) {
                            if (MediaController.this.currentAspectRatioFrameLayout != null) {
                                if (MediaController.this.isDrawingWasReady) {
                                    MediaController.this.currentAspectRatioFrameLayout.setDrawingReady(true);
                                }
                                if (MediaController.this.currentAspectRatioFrameLayout.getParent() == null) {
                                    MediaController.this.currentTextureViewContainer.addView(MediaController.this.currentAspectRatioFrameLayout);
                                }
                                if (MediaController.this.currentTextureView.getSurfaceTexture() != surfaceTexture) {
                                    MediaController.this.currentTextureView.setSurfaceTexture(surfaceTexture);
                                }
                                MediaController.this.videoPlayer.setTextureView(MediaController.this.currentTextureView);
                            }
                            int unused = MediaController.this.pipSwitchingState = 0;
                            return true;
                        } else if (MediaController.this.pipSwitchingState == 1) {
                            if (MediaController.this.baseActivity != null) {
                                if (MediaController.this.pipRoundVideoView == null) {
                                    try {
                                        PipRoundVideoView unused2 = MediaController.this.pipRoundVideoView = new PipRoundVideoView();
                                        MediaController.this.pipRoundVideoView.show(MediaController.this.baseActivity, new Runnable() {
                                            public final void run() {
                                                MediaController.AnonymousClass9.this.lambda$onSurfaceDestroyed$0$MediaController$9();
                                            }
                                        });
                                    } catch (Exception unused3) {
                                        PipRoundVideoView unused4 = MediaController.this.pipRoundVideoView = null;
                                    }
                                }
                                if (MediaController.this.pipRoundVideoView != null) {
                                    if (MediaController.this.pipRoundVideoView.getTextureView().getSurfaceTexture() != surfaceTexture) {
                                        MediaController.this.pipRoundVideoView.getTextureView().setSurfaceTexture(surfaceTexture);
                                    }
                                    MediaController.this.videoPlayer.setTextureView(MediaController.this.pipRoundVideoView.getTextureView());
                                }
                            }
                            int unused5 = MediaController.this.pipSwitchingState = 0;
                            return true;
                        } else if (!PhotoViewer.hasInstance() || !PhotoViewer.getInstance().isInjectingVideoPlayer()) {
                            return false;
                        } else {
                            PhotoViewer.getInstance().injectVideoPlayerSurface(surfaceTexture);
                            return true;
                        }
                    }

                    /* access modifiers changed from: private */
                    /* renamed from: lambda$onSurfaceDestroyed$0 */
                    public /* synthetic */ void lambda$onSurfaceDestroyed$0$MediaController$9() {
                        MediaController.this.cleanupPlayer(true, true);
                    }
                };
                videoPlayer3.setDelegate(r10);
                this.currentAspectRatioFrameLayoutReady = false;
                if (this.pipRoundVideoView != null || !MessagesController.getInstance(messageObject2.currentAccount).isDialogVisible(messageObject.getDialogId(), messageObject2.scheduled)) {
                    if (this.pipRoundVideoView == null) {
                        try {
                            PipRoundVideoView pipRoundVideoView2 = new PipRoundVideoView();
                            this.pipRoundVideoView = pipRoundVideoView2;
                            pipRoundVideoView2.show(this.baseActivity, new Runnable() {
                                public final void run() {
                                    MediaController.this.lambda$playMessage$19$MediaController();
                                }
                            });
                        } catch (Exception unused) {
                            this.pipRoundVideoView = null;
                        }
                    }
                    PipRoundVideoView pipRoundVideoView3 = this.pipRoundVideoView;
                    if (pipRoundVideoView3 != null) {
                        this.videoPlayer.setTextureView(pipRoundVideoView3.getTextureView());
                    }
                } else {
                    TextureView textureView = this.currentTextureView;
                    if (textureView != null) {
                        this.videoPlayer.setTextureView(textureView);
                    }
                }
                if (z5) {
                    if (!messageObject2.mediaExists && file3 != file) {
                        AndroidUtilities.runOnUIThread(new Runnable(file3) {
                            public final /* synthetic */ File f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                NotificationCenter.getInstance(MessageObject.this.currentAccount).postNotificationName(NotificationCenter.fileDidLoad, FileLoader.getAttachFileName(MessageObject.this.getDocument()), this.f$1);
                            }
                        });
                    }
                    this.videoPlayer.preparePlayer(Uri.fromFile(file3), str13);
                } else {
                    try {
                        int fileReference = FileLoader.getInstance(messageObject2.currentAccount).getFileReference(messageObject2);
                        TLRPC$Document document = messageObject.getDocument();
                        StringBuilder sb = new StringBuilder();
                        sb.append("?account=");
                        sb.append(messageObject2.currentAccount);
                        sb.append("&id=");
                        sb.append(document.id);
                        sb.append("&hash=");
                        sb.append(document.access_hash);
                        sb.append(str14);
                        sb.append(document.dc_id);
                        sb.append(str12);
                        sb.append(document.size);
                        sb.append(str9);
                        String str15 = str7;
                        sb.append(URLEncoder.encode(document.mime_type, str15));
                        sb.append(str8);
                        sb.append(fileReference);
                        sb.append(str10);
                        sb.append(URLEncoder.encode(FileLoader.getDocumentFileName(document), str15));
                        sb.append(str11);
                        byte[] bArr = document.file_reference;
                        if (bArr == null) {
                            bArr = new byte[0];
                        }
                        sb.append(Utilities.bytesToHex(bArr));
                        this.videoPlayer.preparePlayer(Uri.parse("tg://" + messageObject.getFileName() + sb.toString()), str13);
                    } catch (Exception e) {
                        FileLog.e((Throwable) e);
                    }
                }
                if (messageObject.isRoundVideo()) {
                    this.videoPlayer.setStreamType(this.useFrontSpeaker ? 0 : 3);
                    float f = this.currentPlaybackSpeed;
                    if (f > 1.0f) {
                        this.videoPlayer.setPlaybackSpeed(f);
                    }
                } else {
                    this.videoPlayer.setStreamType(3);
                }
            } else {
                PipRoundVideoView pipRoundVideoView4 = this.pipRoundVideoView;
                if (pipRoundVideoView4 != null) {
                    pipRoundVideoView4.close(true);
                    this.pipRoundVideoView = null;
                }
                try {
                    VideoPlayer videoPlayer4 = new VideoPlayer();
                    this.audioPlayer = videoPlayer4;
                    final int i2 = this.playerNum + 1;
                    this.playerNum = i2;
                    videoPlayer4.setDelegate(new VideoPlayer.VideoPlayerDelegate() {
                        public void onError(VideoPlayer videoPlayer, Exception exc) {
                        }

                        public void onRenderedFirstFrame() {
                        }

                        public /* bridge */ /* synthetic */ void onRenderedFirstFrame(AnalyticsListener.EventTime eventTime) {
                            VideoPlayer.VideoPlayerDelegate.CC.$default$onRenderedFirstFrame(this, eventTime);
                        }

                        public /* bridge */ /* synthetic */ void onSeekFinished(AnalyticsListener.EventTime eventTime) {
                            VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekFinished(this, eventTime);
                        }

                        public /* bridge */ /* synthetic */ void onSeekStarted(AnalyticsListener.EventTime eventTime) {
                            VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekStarted(this, eventTime);
                        }

                        public boolean onSurfaceDestroyed(SurfaceTexture surfaceTexture) {
                            return false;
                        }

                        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                        }

                        public void onVideoSizeChanged(int i, int i2, int i3, float f) {
                        }

                        public void onStateChanged(boolean z, int i) {
                            if (i2 == MediaController.this.playerNum) {
                                if (i == 4 || ((i == 1 || i == 2) && z && messageObject2.audioProgress >= 0.999f)) {
                                    if (MediaController.this.playlist.isEmpty() || (MediaController.this.playlist.size() <= 1 && messageObject2.isVoice())) {
                                        MediaController mediaController = MediaController.this;
                                        MessageObject messageObject = messageObject2;
                                        mediaController.cleanupPlayer(true, true, messageObject != null && messageObject.isVoice(), false);
                                        return;
                                    }
                                    MediaController.this.playNextMessageWithoutOrder(true);
                                } else if (MediaController.this.audioPlayer != null && MediaController.this.seekToProgressPending != 0.0f) {
                                    if (i == 3 || i == 1) {
                                        long duration = (long) ((int) (((float) MediaController.this.audioPlayer.getDuration()) * MediaController.this.seekToProgressPending));
                                        MediaController.this.audioPlayer.seekTo(duration);
                                        long unused = MediaController.this.lastProgress = duration;
                                        float unused2 = MediaController.this.seekToProgressPending = 0.0f;
                                    }
                                }
                            }
                        }
                    });
                    this.audioPlayer.setAudioVisualizerDelegate(new VideoPlayer.AudioVisualizerDelegate() {
                        public void onVisualizerUpdate(boolean z, boolean z2, float[] fArr) {
                            Theme.getCurrentAudiVisualizerDrawable().setWaveform(z, z2, fArr);
                        }

                        public boolean needUpdate() {
                            return Theme.getCurrentAudiVisualizerDrawable().getParentView() != null;
                        }
                    });
                    if (z5) {
                        if (!messageObject2.mediaExists && file2 != file) {
                            AndroidUtilities.runOnUIThread(new Runnable(file2) {
                                public final /* synthetic */ File f$1;

                                {
                                    this.f$1 = r2;
                                }

                                public final void run() {
                                    NotificationCenter.getInstance(MessageObject.this.currentAccount).postNotificationName(NotificationCenter.fileDidLoad, FileLoader.getAttachFileName(MessageObject.this.getDocument()), this.f$1);
                                }
                            });
                        }
                        this.audioPlayer.preparePlayer(Uri.fromFile(file2), "other");
                        this.isStreamingCurrentAudio = false;
                    } else {
                        int fileReference2 = FileLoader.getInstance(messageObject2.currentAccount).getFileReference(messageObject2);
                        TLRPC$Document document2 = messageObject.getDocument();
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("?account=");
                        sb2.append(messageObject2.currentAccount);
                        sb2.append("&id=");
                        sb2.append(document2.id);
                        sb2.append("&hash=");
                        sb2.append(document2.access_hash);
                        sb2.append("&dc=");
                        sb2.append(document2.dc_id);
                        sb2.append("&size=");
                        sb2.append(document2.size);
                        sb2.append("&mime=");
                        sb2.append(URLEncoder.encode(document2.mime_type, "UTF-8"));
                        sb2.append("&rid=");
                        sb2.append(fileReference2);
                        sb2.append(str3);
                        sb2.append(URLEncoder.encode(FileLoader.getDocumentFileName(document2), "UTF-8"));
                        sb2.append(str2);
                        byte[] bArr2 = document2.file_reference;
                        if (bArr2 == null) {
                            bArr2 = new byte[0];
                        }
                        sb2.append(Utilities.bytesToHex(bArr2));
                        this.audioPlayer.preparePlayer(Uri.parse("tg://" + messageObject.getFileName() + sb2.toString()), "other");
                        this.isStreamingCurrentAudio = true;
                    }
                    if (messageObject.isVoice()) {
                        float f2 = this.currentPlaybackSpeed;
                        if (f2 > 1.0f) {
                            this.audioPlayer.setPlaybackSpeed(f2);
                        }
                        this.audioInfo = null;
                        clearPlaylist();
                    } else {
                        try {
                            this.audioInfo = AudioInfo.getAudioInfo(file2);
                        } catch (Exception e2) {
                            FileLog.e((Throwable) e2);
                        }
                        String fileName = messageObject.getFileName();
                        if (!TextUtils.isEmpty(fileName) && messageObject.getDuration() >= 1200) {
                            float f3 = ApplicationLoader.applicationContext.getSharedPreferences("media_saved_pos", 0).getFloat(fileName, -1.0f);
                            if (f3 > 0.0f && f3 < 0.999f) {
                                this.seekToProgressPending = f3;
                                messageObject2.audioProgress = f3;
                            }
                            this.shouldSavePositionForCurrentAudio = fileName;
                            float f4 = this.currentMusicPlaybackSpeed;
                            if (f4 > 1.0f) {
                                this.audioPlayer.setPlaybackSpeed(f4);
                            }
                        }
                    }
                    float f5 = messageObject2.forceSeekTo;
                    if (f5 >= 0.0f) {
                        this.seekToProgressPending = f5;
                        messageObject2.audioProgress = f5;
                        messageObject2.forceSeekTo = -1.0f;
                    }
                    this.audioPlayer.setStreamType(this.useFrontSpeaker ? 0 : 3);
                    this.audioPlayer.play();
                    if (!messageObject.isVoice()) {
                        ValueAnimator valueAnimator = this.audioVolumeAnimator;
                        if (valueAnimator != null) {
                            valueAnimator.removeAllListeners();
                            this.audioVolumeAnimator.cancel();
                        }
                        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.audioVolume, 1.0f});
                        this.audioVolumeAnimator = ofFloat;
                        ofFloat.addUpdateListener(this.audioVolumeUpdateListener);
                        this.audioVolumeAnimator.setDuration(300);
                        this.audioVolumeAnimator.start();
                    } else {
                        this.audioVolume = 1.0f;
                        setPlayerVolume();
                    }
                } catch (Exception e3) {
                    FileLog.e((Throwable) e3);
                    NotificationCenter instance = NotificationCenter.getInstance(messageObject2.currentAccount);
                    int i3 = NotificationCenter.messagePlayingPlayStateChanged;
                    Object[] objArr = new Object[1];
                    MessageObject messageObject5 = this.playingMessageObject;
                    objArr[0] = Integer.valueOf(messageObject5 != null ? messageObject5.getId() : 0);
                    instance.postNotificationName(i3, objArr);
                    VideoPlayer videoPlayer5 = this.audioPlayer;
                    if (videoPlayer5 != null) {
                        videoPlayer5.releasePlayer(true);
                        this.audioPlayer = null;
                        Theme.unrefAudioVisualizeDrawable(this.playingMessageObject);
                        this.isPaused = false;
                        this.playingMessageObject = null;
                        this.downloadingCurrentMessage = false;
                    }
                    return false;
                }
            }
            checkAudioFocus(messageObject);
            setPlayerVolume();
            this.isPaused = false;
            this.lastProgress = 0;
            this.playingMessageObject = messageObject2;
            if (!SharedConfig.raiseToSpeak) {
                startRaiseToEarSensors(this.raiseChat);
            }
            if (!ApplicationLoader.mainInterfacePaused && (wakeLock = this.proximityWakeLock) != null && !wakeLock.isHeld() && (this.playingMessageObject.isVoice() || this.playingMessageObject.isRoundVideo())) {
                this.proximityWakeLock.acquire();
            }
            startProgressTimer(this.playingMessageObject);
            NotificationCenter.getInstance(messageObject2.currentAccount).postNotificationName(NotificationCenter.messagePlayingDidStart, messageObject2);
            VideoPlayer videoPlayer6 = this.videoPlayer;
            if (videoPlayer6 != null) {
                try {
                    if (this.playingMessageObject.audioProgress != 0.0f) {
                        long duration = videoPlayer6.getDuration();
                        if (duration == -9223372036854775807L) {
                            duration = ((long) this.playingMessageObject.getDuration()) * 1000;
                        }
                        MessageObject messageObject6 = this.playingMessageObject;
                        int i4 = (int) (((float) duration) * messageObject6.audioProgress);
                        int i5 = messageObject6.audioProgressMs;
                        if (i5 != 0) {
                            messageObject6.audioProgressMs = 0;
                            i4 = i5;
                        }
                        this.videoPlayer.seekTo((long) i4);
                    }
                } catch (Exception e4) {
                    MessageObject messageObject7 = this.playingMessageObject;
                    messageObject7.audioProgress = 0.0f;
                    messageObject7.audioProgressSec = 0;
                    NotificationCenter.getInstance(messageObject2.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, Integer.valueOf(this.playingMessageObject.getId()), null);
                    FileLog.e((Throwable) e4);
                }
                this.videoPlayer.play();
            } else {
                VideoPlayer videoPlayer7 = this.audioPlayer;
                if (videoPlayer7 != null) {
                    try {
                        if (this.playingMessageObject.audioProgress != 0.0f) {
                            long duration2 = videoPlayer7.getDuration();
                            if (duration2 == -9223372036854775807L) {
                                duration2 = ((long) this.playingMessageObject.getDuration()) * 1000;
                            }
                            this.audioPlayer.seekTo((long) ((int) (((float) duration2) * this.playingMessageObject.audioProgress)));
                        }
                    } catch (Exception e5) {
                        this.playingMessageObject.resetPlayingProgress();
                        NotificationCenter.getInstance(messageObject2.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, Integer.valueOf(this.playingMessageObject.getId()), null);
                        FileLog.e((Throwable) e5);
                    }
                }
            }
            MessageObject messageObject8 = this.playingMessageObject;
            if (messageObject8 == null || !messageObject8.isMusic()) {
                ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
                return true;
            }
            try {
                ApplicationLoader.applicationContext.startService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
                return true;
            } catch (Throwable th) {
                FileLog.e(th);
                return true;
            }
        } else {
            FileLoader.getInstance(messageObject2.currentAccount).loadFile(messageObject.getDocument(), messageObject2, 0, 0);
            this.downloadingCurrentMessage = true;
            this.isPaused = false;
            this.lastProgress = 0;
            this.audioInfo = null;
            this.playingMessageObject = messageObject2;
            if (messageObject.isMusic()) {
                try {
                    ApplicationLoader.applicationContext.startService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
                } catch (Throwable th2) {
                    FileLog.e(th2);
                }
            } else {
                ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
            }
            NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingPlayStateChanged, Integer.valueOf(this.playingMessageObject.getId()));
            return true;
        }
    }

    public AudioInfo getAudioInfo() {
        return this.audioInfo;
    }

    public void setPlaybackOrderType(int i) {
        boolean z = SharedConfig.shuffleMusic;
        SharedConfig.setPlaybackOrderType(i);
        boolean z2 = SharedConfig.shuffleMusic;
        if (z == z2) {
            return;
        }
        if (z2) {
            buildShuffledPlayList();
            return;
        }
        MessageObject messageObject = this.playingMessageObject;
        if (messageObject != null) {
            int indexOf = this.playlist.indexOf(messageObject);
            this.currentPlaylistNum = indexOf;
            if (indexOf == -1) {
                clearPlaylist();
                cleanupPlayer(true, true);
            }
        }
    }

    public boolean isStreamingCurrentAudio() {
        return this.isStreamingCurrentAudio;
    }

    public boolean isCurrentPlayer(VideoPlayer videoPlayer2) {
        return this.videoPlayer == videoPlayer2 || this.audioPlayer == videoPlayer2;
    }

    /* renamed from: pauseMessage */
    public boolean lambda$startAudioAgain$7(MessageObject messageObject) {
        if (!((this.audioPlayer == null && this.videoPlayer == null) || messageObject == null || this.playingMessageObject == null || !isSamePlayingMessage(messageObject))) {
            stopProgressTimer();
            try {
                if (this.audioPlayer == null) {
                    VideoPlayer videoPlayer2 = this.videoPlayer;
                    if (videoPlayer2 != null) {
                        videoPlayer2.pause();
                    }
                } else if (!this.playingMessageObject.isVoice()) {
                    ValueAnimator valueAnimator = this.audioVolumeAnimator;
                    if (valueAnimator != null) {
                        valueAnimator.removeAllUpdateListeners();
                        this.audioVolumeAnimator.cancel();
                    }
                    ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
                    this.audioVolumeAnimator = ofFloat;
                    ofFloat.addUpdateListener(this.audioVolumeUpdateListener);
                    this.audioVolumeAnimator.setDuration(300);
                    this.audioVolumeAnimator.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            if (MediaController.this.audioPlayer != null) {
                                MediaController.this.audioPlayer.pause();
                            }
                        }
                    });
                    this.audioVolumeAnimator.start();
                } else {
                    this.audioPlayer.pause();
                }
                this.isPaused = true;
                NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingPlayStateChanged, Integer.valueOf(this.playingMessageObject.getId()));
                return true;
            } catch (Exception e) {
                FileLog.e((Throwable) e);
                this.isPaused = false;
            }
        }
        return false;
    }

    private boolean resumeAudio(MessageObject messageObject) {
        if (!((this.audioPlayer == null && this.videoPlayer == null) || messageObject == null || this.playingMessageObject == null || !isSamePlayingMessage(messageObject))) {
            try {
                startProgressTimer(this.playingMessageObject);
                ValueAnimator valueAnimator = this.audioVolumeAnimator;
                if (valueAnimator != null) {
                    valueAnimator.removeAllListeners();
                    this.audioVolumeAnimator.cancel();
                }
                if (!messageObject.isVoice()) {
                    ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.audioVolume, 1.0f});
                    this.audioVolumeAnimator = ofFloat;
                    ofFloat.addUpdateListener(this.audioVolumeUpdateListener);
                    this.audioVolumeAnimator.setDuration(300);
                    this.audioVolumeAnimator.start();
                } else {
                    this.audioVolume = 1.0f;
                    setPlayerVolume();
                }
                VideoPlayer videoPlayer2 = this.audioPlayer;
                if (videoPlayer2 != null) {
                    videoPlayer2.play();
                } else {
                    VideoPlayer videoPlayer3 = this.videoPlayer;
                    if (videoPlayer3 != null) {
                        videoPlayer3.play();
                    }
                }
                checkAudioFocus(messageObject);
                this.isPaused = false;
                NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingPlayStateChanged, Integer.valueOf(this.playingMessageObject.getId()));
                return true;
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }
        return false;
    }

    public boolean isVideoDrawingReady() {
        AspectRatioFrameLayout aspectRatioFrameLayout = this.currentAspectRatioFrameLayout;
        return aspectRatioFrameLayout != null && aspectRatioFrameLayout.isDrawingReady();
    }

    public ArrayList<MessageObject> getPlaylist() {
        return this.playlist;
    }

    public boolean isPlayingMessage(MessageObject messageObject) {
        MessageObject messageObject2;
        if (!((this.audioPlayer == null && this.videoPlayer == null) || messageObject == null || (messageObject2 = this.playingMessageObject) == null)) {
            long j = messageObject2.eventId;
            if (j != 0 && j == messageObject.eventId) {
                return !this.downloadingCurrentMessage;
            }
            if (isSamePlayingMessage(messageObject)) {
                return !this.downloadingCurrentMessage;
            }
        }
        return false;
    }

    public boolean isPlayingMessageAndReadyToDraw(MessageObject messageObject) {
        return this.isDrawingWasReady && isPlayingMessage(messageObject);
    }

    public boolean isMessagePaused() {
        return this.isPaused || this.downloadingCurrentMessage;
    }

    public boolean isDownloadingCurrentMessage() {
        return this.downloadingCurrentMessage;
    }

    public void setReplyingMessage(MessageObject messageObject, MessageObject messageObject2) {
        this.recordReplyingMsg = messageObject;
        this.recordReplyingTopMsg = messageObject2;
    }

    public void requestAudioFocus(boolean z) {
        if (z) {
            if (!this.hasRecordAudioFocus && SharedConfig.pauseMusicOnRecord && NotificationsController.audioManager.requestAudioFocus(this.audioRecordFocusChangedListener, 3, 2) == 1) {
                this.hasRecordAudioFocus = true;
            }
        } else if (this.hasRecordAudioFocus) {
            NotificationsController.audioManager.abandonAudioFocus(this.audioRecordFocusChangedListener);
            this.hasRecordAudioFocus = false;
        }
    }

    public void startRecording(int i, long j, MessageObject messageObject, MessageObject messageObject2, int i2) {
        MessageObject messageObject3 = this.playingMessageObject;
        boolean z = messageObject3 != null && isPlayingMessage(messageObject3) && !isMessagePaused();
        requestAudioFocus(true);
        try {
            this.feedbackView.performHapticFeedback(3, 2);
        } catch (Exception unused) {
        }
        DispatchQueue dispatchQueue = this.recordQueue;
        $$Lambda$MediaController$ncmYURCWK__UhLS7XSpZk7BNV3I r0 = new Runnable(i, i2, j, messageObject, messageObject2) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ int f$2;
            public final /* synthetic */ long f$3;
            public final /* synthetic */ MessageObject f$4;
            public final /* synthetic */ MessageObject f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r6;
                this.f$5 = r7;
            }

            public final void run() {
                MediaController.this.lambda$startRecording$26$MediaController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        };
        this.recordStartRunnable = r0;
        dispatchQueue.postRunnable(r0, z ? 500 : 50);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startRecording$26 */
    public /* synthetic */ void lambda$startRecording$26$MediaController(int i, int i2, long j, MessageObject messageObject, MessageObject messageObject2) {
        if (this.audioRecorder != null) {
            AndroidUtilities.runOnUIThread(new Runnable(i, i2) {
                public final /* synthetic */ int f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    MediaController.this.lambda$null$22$MediaController(this.f$1, this.f$2);
                }
            });
            return;
        }
        this.sendAfterDone = 0;
        TLRPC$TL_document tLRPC$TL_document = new TLRPC$TL_document();
        this.recordingAudio = tLRPC$TL_document;
        this.recordingGuid = i2;
        tLRPC$TL_document.file_reference = new byte[0];
        tLRPC$TL_document.dc_id = Integer.MIN_VALUE;
        tLRPC$TL_document.id = (long) SharedConfig.getLastLocalId();
        this.recordingAudio.user_id = UserConfig.getInstance(i).getClientUserId();
        TLRPC$TL_document tLRPC$TL_document2 = this.recordingAudio;
        tLRPC$TL_document2.mime_type = "audio/ogg";
        tLRPC$TL_document2.file_reference = new byte[0];
        SharedConfig.saveConfig();
        File file = new File(FileLoader.getDirectory(4), FileLoader.getAttachFileName(this.recordingAudio));
        this.recordingAudioFile = file;
        try {
            if (startRecord(file.getAbsolutePath(), 16000) == 0) {
                AndroidUtilities.runOnUIThread(new Runnable(i, i2) {
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        MediaController.this.lambda$null$23$MediaController(this.f$1, this.f$2);
                    }
                });
                return;
            }
            this.audioRecorder = new AudioRecord(0, this.sampleRate, 16, 2, this.recordBufferSize);
            this.recordStartTime = System.currentTimeMillis();
            this.recordTimeCount = 0;
            this.samplesCount = 0;
            this.recordDialogId = j;
            this.recordingCurrentAccount = i;
            this.recordReplyingMsg = messageObject;
            this.recordReplyingTopMsg = messageObject2;
            this.fileBuffer.rewind();
            this.audioRecorder.startRecording();
            this.recordQueue.postRunnable(this.recordRunnable);
            AndroidUtilities.runOnUIThread(new Runnable(i, i2) {
                public final /* synthetic */ int f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    MediaController.this.lambda$null$25$MediaController(this.f$1, this.f$2);
                }
            });
        } catch (Exception e) {
            FileLog.e((Throwable) e);
            this.recordingAudio = null;
            stopRecord();
            this.recordingAudioFile.delete();
            this.recordingAudioFile = null;
            try {
                this.audioRecorder.release();
                this.audioRecorder = null;
            } catch (Exception e2) {
                FileLog.e((Throwable) e2);
            }
            AndroidUtilities.runOnUIThread(new Runnable(i, i2) {
                public final /* synthetic */ int f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    MediaController.this.lambda$null$24$MediaController(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$22 */
    public /* synthetic */ void lambda$null$22$MediaController(int i, int i2) {
        this.recordStartRunnable = null;
        NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.recordStartError, Integer.valueOf(i2));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$23 */
    public /* synthetic */ void lambda$null$23$MediaController(int i, int i2) {
        this.recordStartRunnable = null;
        NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.recordStartError, Integer.valueOf(i2));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$24 */
    public /* synthetic */ void lambda$null$24$MediaController(int i, int i2) {
        this.recordStartRunnable = null;
        NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.recordStartError, Integer.valueOf(i2));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$25 */
    public /* synthetic */ void lambda$null$25$MediaController(int i, int i2) {
        this.recordStartRunnable = null;
        NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.recordStarted, Integer.valueOf(i2), Boolean.TRUE);
    }

    public void generateWaveform(MessageObject messageObject) {
        String str = messageObject.getId() + "_" + messageObject.getDialogId();
        String absolutePath = FileLoader.getPathToMessage(messageObject.messageOwner).getAbsolutePath();
        if (!this.generatingWaveform.containsKey(str)) {
            this.generatingWaveform.put(str, messageObject);
            Utilities.globalQueue.postRunnable(new Runnable(absolutePath, str, messageObject) {
                public final /* synthetic */ String f$1;
                public final /* synthetic */ String f$2;
                public final /* synthetic */ MessageObject f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    MediaController.this.lambda$generateWaveform$28$MediaController(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$generateWaveform$28 */
    public /* synthetic */ void lambda$generateWaveform$28$MediaController(String str, String str2, MessageObject messageObject) {
        AndroidUtilities.runOnUIThread(new Runnable(str2, getWaveform(str), messageObject) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ byte[] f$2;
            public final /* synthetic */ MessageObject f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                MediaController.this.lambda$null$27$MediaController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$27 */
    public /* synthetic */ void lambda$null$27$MediaController(String str, byte[] bArr, MessageObject messageObject) {
        MessageObject remove = this.generatingWaveform.remove(str);
        if (remove != null && bArr != null && remove.getDocument() != null) {
            int i = 0;
            while (true) {
                if (i >= remove.getDocument().attributes.size()) {
                    break;
                }
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = remove.getDocument().attributes.get(i);
                if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAudio) {
                    tLRPC$DocumentAttribute.waveform = bArr;
                    tLRPC$DocumentAttribute.flags |= 4;
                    break;
                }
                i++;
            }
            TLRPC$TL_messages_messages tLRPC$TL_messages_messages = new TLRPC$TL_messages_messages();
            tLRPC$TL_messages_messages.messages.add(remove.messageOwner);
            MessagesStorage.getInstance(remove.currentAccount).putMessages((TLRPC$messages_Messages) tLRPC$TL_messages_messages, remove.getDialogId(), -1, 0, false, messageObject.scheduled);
            ArrayList arrayList = new ArrayList();
            arrayList.add(remove);
            NotificationCenter.getInstance(remove.currentAccount).postNotificationName(NotificationCenter.replaceMessagesObjects, Long.valueOf(remove.getDialogId()), arrayList);
        }
    }

    /* access modifiers changed from: private */
    public void stopRecordingInternal(int i, boolean z, int i2) {
        if (i != 0) {
            this.fileEncodingQueue.postRunnable(new Runnable(this.recordingAudio, this.recordingAudioFile, i, z, i2) {
                public final /* synthetic */ TLRPC$TL_document f$1;
                public final /* synthetic */ File f$2;
                public final /* synthetic */ int f$3;
                public final /* synthetic */ boolean f$4;
                public final /* synthetic */ int f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                public final void run() {
                    MediaController.this.lambda$stopRecordingInternal$30$MediaController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                }
            });
        } else {
            File file = this.recordingAudioFile;
            if (file != null) {
                file.delete();
            }
            requestAudioFocus(false);
        }
        try {
            AudioRecord audioRecord = this.audioRecorder;
            if (audioRecord != null) {
                audioRecord.release();
                this.audioRecorder = null;
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
        this.recordingAudio = null;
        this.recordingAudioFile = null;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$stopRecordingInternal$30 */
    public /* synthetic */ void lambda$stopRecordingInternal$30$MediaController(TLRPC$TL_document tLRPC$TL_document, File file, int i, boolean z, int i2) {
        stopRecord();
        AndroidUtilities.runOnUIThread(new Runnable(tLRPC$TL_document, file, i, z, i2) {
            public final /* synthetic */ TLRPC$TL_document f$1;
            public final /* synthetic */ File f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ boolean f$4;
            public final /* synthetic */ int f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void run() {
                MediaController.this.lambda$null$29$MediaController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$29 */
    public /* synthetic */ void lambda$null$29$MediaController(TLRPC$TL_document tLRPC$TL_document, File file, int i, boolean z, int i2) {
        boolean z2;
        char c;
        TLRPC$TL_document tLRPC$TL_document2 = tLRPC$TL_document;
        int i3 = i;
        tLRPC$TL_document2.date = ConnectionsManager.getInstance(this.recordingCurrentAccount).getCurrentTime();
        tLRPC$TL_document2.size = (int) file.length();
        TLRPC$TL_documentAttributeAudio tLRPC$TL_documentAttributeAudio = new TLRPC$TL_documentAttributeAudio();
        tLRPC$TL_documentAttributeAudio.voice = true;
        short[] sArr = this.recordSamples;
        byte[] waveform2 = getWaveform2(sArr, sArr.length);
        tLRPC$TL_documentAttributeAudio.waveform = waveform2;
        if (waveform2 != null) {
            tLRPC$TL_documentAttributeAudio.flags |= 4;
        }
        long j = this.recordTimeCount;
        tLRPC$TL_documentAttributeAudio.duration = (int) (j / 1000);
        tLRPC$TL_document2.attributes.add(tLRPC$TL_documentAttributeAudio);
        if (j > 700) {
            if (i3 == 1) {
                c = 1;
                SendMessagesHelper.getInstance(this.recordingCurrentAccount).sendMessage(tLRPC$TL_document, (VideoEditedInfo) null, file.getAbsolutePath(), this.recordDialogId, this.recordReplyingMsg, this.recordReplyingTopMsg, (String) null, (ArrayList<TLRPC$MessageEntity>) null, (TLRPC$ReplyMarkup) null, (HashMap<String, String>) null, z, i2, 0, (Object) null);
            } else {
                c = 1;
            }
            NotificationCenter instance = NotificationCenter.getInstance(this.recordingCurrentAccount);
            int i4 = NotificationCenter.audioDidSent;
            Object[] objArr = new Object[3];
            z2 = false;
            objArr[0] = Integer.valueOf(this.recordingGuid);
            String str = null;
            int i5 = i;
            objArr[c] = i5 == 2 ? tLRPC$TL_document : null;
            if (i5 == 2) {
                str = file.getAbsolutePath();
            }
            objArr[2] = str;
            instance.postNotificationName(i4, objArr);
        } else {
            z2 = false;
            NotificationCenter.getInstance(this.recordingCurrentAccount).postNotificationName(NotificationCenter.audioRecordTooShort, Integer.valueOf(this.recordingGuid), Boolean.FALSE, Integer.valueOf((int) j));
            file.delete();
        }
        requestAudioFocus(z2);
    }

    public void stopRecording(int i, boolean z, int i2) {
        Runnable runnable = this.recordStartRunnable;
        if (runnable != null) {
            this.recordQueue.cancelRunnable(runnable);
            this.recordStartRunnable = null;
        }
        this.recordQueue.postRunnable(new Runnable(i, z, i2) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                MediaController.this.lambda$stopRecording$32$MediaController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$stopRecording$32 */
    public /* synthetic */ void lambda$stopRecording$32$MediaController(int i, boolean z, int i2) {
        if (this.sendAfterDone == 3) {
            this.sendAfterDone = 0;
            stopRecordingInternal(i, z, i2);
            return;
        }
        AudioRecord audioRecord = this.audioRecorder;
        if (audioRecord != null) {
            try {
                this.sendAfterDone = i;
                this.sendAfterDoneNotify = z;
                this.sendAfterDoneScheduleDate = i2;
                audioRecord.stop();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
                File file = this.recordingAudioFile;
                if (file != null) {
                    file.delete();
                }
            }
            if (i == 0) {
                stopRecordingInternal(0, false, 0);
            }
            try {
                this.feedbackView.performHapticFeedback(3, 2);
            } catch (Exception unused) {
            }
            AndroidUtilities.runOnUIThread(new Runnable(i) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MediaController.this.lambda$null$31$MediaController(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$31 */
    public /* synthetic */ void lambda$null$31$MediaController(int i) {
        NotificationCenter instance = NotificationCenter.getInstance(this.recordingCurrentAccount);
        int i2 = NotificationCenter.recordStopped;
        Object[] objArr = new Object[2];
        int i3 = 0;
        objArr[0] = Integer.valueOf(this.recordingGuid);
        if (i == 2) {
            i3 = 1;
        }
        objArr[1] = Integer.valueOf(i3);
        instance.postNotificationName(i2, objArr);
    }

    private static class MediaLoader implements NotificationCenter.NotificationCenterDelegate {
        private boolean cancelled;
        private int copiedFiles;
        private AccountInstance currentAccount;
        private boolean finished;
        private float finishedProgress;
        private boolean isMusic;
        private HashMap<String, MessageObject> loadingMessageObjects = new HashMap<>();
        private ArrayList<MessageObject> messageObjects;
        private MessagesStorage.IntCallback onFinishRunnable;
        private AlertDialog progressDialog;
        private CountDownLatch waitingForFile;

        public MediaLoader(Context context, AccountInstance accountInstance, ArrayList<MessageObject> arrayList, MessagesStorage.IntCallback intCallback) {
            this.currentAccount = accountInstance;
            this.messageObjects = arrayList;
            this.onFinishRunnable = intCallback;
            this.isMusic = arrayList.get(0).isMusic();
            this.currentAccount.getNotificationCenter().addObserver(this, NotificationCenter.fileDidLoad);
            this.currentAccount.getNotificationCenter().addObserver(this, NotificationCenter.FileLoadProgressChanged);
            this.currentAccount.getNotificationCenter().addObserver(this, NotificationCenter.fileDidFailToLoad);
            AlertDialog alertDialog = new AlertDialog(context, 2);
            this.progressDialog = alertDialog;
            alertDialog.setMessage(LocaleController.getString("Loading", NUM));
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.setCancelable(true);
            this.progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public final void onCancel(DialogInterface dialogInterface) {
                    MediaController.MediaLoader.this.lambda$new$0$MediaController$MediaLoader(dialogInterface);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$0 */
        public /* synthetic */ void lambda$new$0$MediaController$MediaLoader(DialogInterface dialogInterface) {
            this.cancelled = true;
        }

        public void start() {
            AndroidUtilities.runOnUIThread(new Runnable() {
                public final void run() {
                    MediaController.MediaLoader.this.lambda$start$1$MediaController$MediaLoader();
                }
            }, 250);
            new Thread(new Runnable() {
                public final void run() {
                    MediaController.MediaLoader.this.lambda$start$2$MediaController$MediaLoader();
                }
            }).start();
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$start$1 */
        public /* synthetic */ void lambda$start$1$MediaController$MediaLoader() {
            if (!this.finished) {
                this.progressDialog.show();
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$start$2 */
        public /* synthetic */ void lambda$start$2$MediaController$MediaLoader() {
            File file;
            String str;
            try {
                if (this.isMusic) {
                    file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                } else {
                    file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                }
                file.mkdir();
                int size = this.messageObjects.size();
                for (int i = 0; i < size; i++) {
                    MessageObject messageObject = this.messageObjects.get(i);
                    String documentName = messageObject.getDocumentName();
                    File file2 = new File(file, documentName);
                    if (file2.exists()) {
                        int lastIndexOf = documentName.lastIndexOf(46);
                        int i2 = 0;
                        while (true) {
                            if (i2 >= 10) {
                                break;
                            }
                            if (lastIndexOf != -1) {
                                str = documentName.substring(0, lastIndexOf) + "(" + (i2 + 1) + ")" + documentName.substring(lastIndexOf);
                            } else {
                                str = documentName + "(" + (i2 + 1) + ")";
                            }
                            File file3 = new File(file, str);
                            if (!file3.exists()) {
                                file2 = file3;
                                break;
                            } else {
                                i2++;
                                file2 = file3;
                            }
                        }
                    }
                    if (!file2.exists()) {
                        file2.createNewFile();
                    }
                    String str2 = messageObject.messageOwner.attachPath;
                    if (str2 != null && str2.length() > 0 && !new File(str2).exists()) {
                        str2 = null;
                    }
                    if (str2 == null || str2.length() == 0) {
                        str2 = FileLoader.getPathToMessage(messageObject.messageOwner).toString();
                    }
                    File file4 = new File(str2);
                    if (!file4.exists()) {
                        this.waitingForFile = new CountDownLatch(1);
                        addMessageToLoad(messageObject);
                        this.waitingForFile.await();
                    }
                    copyFile(file4, file2, messageObject.getMimeType());
                }
                checkIfFinished();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }

        private void checkIfFinished() {
            if (this.loadingMessageObjects.isEmpty()) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public final void run() {
                        MediaController.MediaLoader.this.lambda$checkIfFinished$4$MediaController$MediaLoader();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$checkIfFinished$4 */
        public /* synthetic */ void lambda$checkIfFinished$4$MediaController$MediaLoader() {
            try {
                if (this.progressDialog.isShowing()) {
                    this.progressDialog.dismiss();
                } else {
                    this.finished = true;
                }
                if (this.onFinishRunnable != null) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public final void run() {
                            MediaController.MediaLoader.this.lambda$null$3$MediaController$MediaLoader();
                        }
                    });
                }
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
            this.currentAccount.getNotificationCenter().removeObserver(this, NotificationCenter.fileDidLoad);
            this.currentAccount.getNotificationCenter().removeObserver(this, NotificationCenter.FileLoadProgressChanged);
            this.currentAccount.getNotificationCenter().removeObserver(this, NotificationCenter.fileDidFailToLoad);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$null$3 */
        public /* synthetic */ void lambda$null$3$MediaController$MediaLoader() {
            this.onFinishRunnable.run(this.copiedFiles);
        }

        private void addMessageToLoad(MessageObject messageObject) {
            AndroidUtilities.runOnUIThread(new Runnable(messageObject) {
                public final /* synthetic */ MessageObject f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MediaController.MediaLoader.this.lambda$addMessageToLoad$5$MediaController$MediaLoader(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$addMessageToLoad$5 */
        public /* synthetic */ void lambda$addMessageToLoad$5$MediaController$MediaLoader(MessageObject messageObject) {
            TLRPC$Document document = messageObject.getDocument();
            if (document != null) {
                this.loadingMessageObjects.put(FileLoader.getAttachFileName(document), messageObject);
                this.currentAccount.getFileLoader().loadFile(document, messageObject, 1, 0);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:104:0x017b, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:105:0x017c, code lost:
            r2 = r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:106:0x017d, code lost:
            if (r10 != null) goto L_0x017f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:108:?, code lost:
            r10.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:110:?, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:111:0x0183, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:117:0x018b, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:118:0x018c, code lost:
            r2 = r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:120:?, code lost:
            r21.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:122:?, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:123:0x0191, code lost:
            r0 = e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:90:0x0169, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:91:0x016a, code lost:
            r2 = r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:92:0x016b, code lost:
            if (r12 != null) goto L_0x016d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:94:?, code lost:
            r12.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:96:?, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:97:0x0171, code lost:
            r0 = th;
         */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [B:28:0x0062, B:115:0x018a, B:121:0x0190] */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [B:69:0x0149, B:88:0x0168, B:95:0x0170] */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [B:72:0x014e, B:102:0x017a, B:109:0x0182] */
        /* JADX WARNING: Missing exception handler attribute for start block: B:109:0x0182 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:121:0x0190 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:95:0x0170 */
        /* JADX WARNING: Unknown top exception splitter block from list: {B:121:0x0190=Splitter:B:121:0x0190, B:74:0x0151=Splitter:B:74:0x0151, B:28:0x0062=Splitter:B:28:0x0062, B:83:0x015f=Splitter:B:83:0x015f} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean copyFile(java.io.File r32, java.io.File r33, java.lang.String r34) {
            /*
                r31 = this;
                r1 = r31
                android.net.Uri r0 = android.net.Uri.fromFile(r32)
                boolean r0 = org.telegram.messenger.AndroidUtilities.isInternalUri((android.net.Uri) r0)
                r2 = 0
                if (r0 == 0) goto L_0x000e
                return r2
            L_0x000e:
                java.io.FileInputStream r3 = new java.io.FileInputStream     // Catch:{ Exception -> 0x0193 }
                r0 = r32
                r3.<init>(r0)     // Catch:{ Exception -> 0x0193 }
                java.nio.channels.FileChannel r10 = r3.getChannel()     // Catch:{ all -> 0x0185 }
                java.io.FileOutputStream r0 = new java.io.FileOutputStream     // Catch:{ all -> 0x0175 }
                r11 = r33
                r0.<init>(r11)     // Catch:{ all -> 0x0173 }
                java.nio.channels.FileChannel r12 = r0.getChannel()     // Catch:{ all -> 0x0173 }
                long r13 = r10.size()     // Catch:{ all -> 0x0165 }
                java.lang.Class<java.io.FileDescriptor> r0 = java.io.FileDescriptor.class
                java.lang.String r4 = "getInt$"
                java.lang.Class[] r5 = new java.lang.Class[r2]     // Catch:{ all -> 0x0066 }
                java.lang.reflect.Method r0 = r0.getDeclaredMethod(r4, r5)     // Catch:{ all -> 0x0066 }
                java.io.FileDescriptor r4 = r3.getFD()     // Catch:{ all -> 0x0066 }
                java.lang.Object[] r5 = new java.lang.Object[r2]     // Catch:{ all -> 0x0066 }
                java.lang.Object r0 = r0.invoke(r4, r5)     // Catch:{ all -> 0x0066 }
                java.lang.Integer r0 = (java.lang.Integer) r0     // Catch:{ all -> 0x0066 }
                int r0 = r0.intValue()     // Catch:{ all -> 0x0066 }
                boolean r0 = org.telegram.messenger.AndroidUtilities.isInternalUri((int) r0)     // Catch:{ all -> 0x0066 }
                if (r0 == 0) goto L_0x006a
                org.telegram.ui.ActionBar.AlertDialog r0 = r1.progressDialog     // Catch:{ all -> 0x0066 }
                if (r0 == 0) goto L_0x0054
                org.telegram.messenger.-$$Lambda$MediaController$MediaLoader$AhWed4ydFVrtLT8l_TqVfs_-1l0 r0 = new org.telegram.messenger.-$$Lambda$MediaController$MediaLoader$AhWed4ydFVrtLT8l_TqVfs_-1l0     // Catch:{ all -> 0x0066 }
                r0.<init>()     // Catch:{ all -> 0x0066 }
                org.telegram.messenger.AndroidUtilities.runOnUIThread(r0)     // Catch:{ all -> 0x0066 }
            L_0x0054:
                if (r12 == 0) goto L_0x0059
                r12.close()     // Catch:{ all -> 0x0173 }
            L_0x0059:
                if (r10 == 0) goto L_0x0062
                r10.close()     // Catch:{ all -> 0x005f }
                goto L_0x0062
            L_0x005f:
                r0 = move-exception
                goto L_0x0188
            L_0x0062:
                r3.close()     // Catch:{ Exception -> 0x0191 }
                return r2
            L_0x0066:
                r0 = move-exception
                org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)     // Catch:{ all -> 0x0165 }
            L_0x006a:
                r4 = 0
                r8 = r4
                r15 = r8
            L_0x006e:
                r0 = 1120403456(0x42CLASSNAME, float:100.0)
                int r4 = (r8 > r13 ? 1 : (r8 == r13 ? 0 : -1))
                if (r4 >= 0) goto L_0x00c1
                boolean r4 = r1.cancelled     // Catch:{ all -> 0x0165 }
                if (r4 == 0) goto L_0x0079
                goto L_0x00c1
            L_0x0079:
                long r4 = r13 - r8
                r6 = 4096(0x1000, double:2.0237E-320)
                long r17 = java.lang.Math.min(r6, r4)     // Catch:{ all -> 0x0165 }
                r4 = r12
                r5 = r10
                r19 = r6
                r6 = r8
                r21 = r3
                r2 = r8
                r8 = r17
                r4.transferFrom(r5, r6, r8)     // Catch:{ all -> 0x0163 }
                long r8 = r2 + r19
                int r4 = (r8 > r13 ? 1 : (r8 == r13 ? 0 : -1))
                if (r4 >= 0) goto L_0x009f
                long r4 = android.os.SystemClock.elapsedRealtime()     // Catch:{ all -> 0x0163 }
                r6 = 500(0x1f4, double:2.47E-321)
                long r4 = r4 - r6
                int r6 = (r15 > r4 ? 1 : (r15 == r4 ? 0 : -1))
                if (r6 > 0) goto L_0x00bd
            L_0x009f:
                long r4 = android.os.SystemClock.elapsedRealtime()     // Catch:{ all -> 0x0163 }
                float r6 = r1.finishedProgress     // Catch:{ all -> 0x0163 }
                java.util.ArrayList<org.telegram.messenger.MessageObject> r7 = r1.messageObjects     // Catch:{ all -> 0x0163 }
                int r7 = r7.size()     // Catch:{ all -> 0x0163 }
                float r7 = (float) r7     // Catch:{ all -> 0x0163 }
                float r0 = r0 / r7
                float r2 = (float) r2     // Catch:{ all -> 0x0163 }
                float r0 = r0 * r2
                float r2 = (float) r13     // Catch:{ all -> 0x0163 }
                float r0 = r0 / r2
                float r6 = r6 + r0
                int r0 = (int) r6     // Catch:{ all -> 0x0163 }
                org.telegram.messenger.-$$Lambda$MediaController$MediaLoader$SnHE8B84nWdyFiAl03Ud-KrXKTc r2 = new org.telegram.messenger.-$$Lambda$MediaController$MediaLoader$SnHE8B84nWdyFiAl03Ud-KrXKTc     // Catch:{ all -> 0x0163 }
                r2.<init>(r0)     // Catch:{ all -> 0x0163 }
                org.telegram.messenger.AndroidUtilities.runOnUIThread(r2)     // Catch:{ all -> 0x0163 }
                r15 = r4
            L_0x00bd:
                r3 = r21
                r2 = 0
                goto L_0x006e
            L_0x00c1:
                r21 = r3
                boolean r2 = r1.cancelled     // Catch:{ all -> 0x0163 }
                if (r2 != 0) goto L_0x0155
                boolean r2 = r1.isMusic     // Catch:{ all -> 0x0163 }
                r3 = 1
                if (r2 == 0) goto L_0x00d4
                android.net.Uri r2 = android.net.Uri.fromFile(r33)     // Catch:{ all -> 0x0163 }
                org.telegram.messenger.AndroidUtilities.addMediaToGallery((android.net.Uri) r2)     // Catch:{ all -> 0x0163 }
                goto L_0x012c
            L_0x00d4:
                android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ all -> 0x0163 }
                java.lang.String r4 = "download"
                java.lang.Object r2 = r2.getSystemService(r4)     // Catch:{ all -> 0x0163 }
                r22 = r2
                android.app.DownloadManager r22 = (android.app.DownloadManager) r22     // Catch:{ all -> 0x0163 }
                boolean r2 = android.text.TextUtils.isEmpty(r34)     // Catch:{ all -> 0x0163 }
                java.lang.String r4 = "text/plain"
                if (r2 == 0) goto L_0x0113
                android.webkit.MimeTypeMap r2 = android.webkit.MimeTypeMap.getSingleton()     // Catch:{ all -> 0x0163 }
                java.lang.String r5 = r33.getName()     // Catch:{ all -> 0x0163 }
                r6 = 46
                int r6 = r5.lastIndexOf(r6)     // Catch:{ all -> 0x0163 }
                r7 = -1
                if (r6 == r7) goto L_0x0110
                int r6 = r6 + r3
                java.lang.String r5 = r5.substring(r6)     // Catch:{ all -> 0x0163 }
                java.lang.String r5 = r5.toLowerCase()     // Catch:{ all -> 0x0163 }
                java.lang.String r2 = r2.getMimeTypeFromExtension(r5)     // Catch:{ all -> 0x0163 }
                boolean r5 = android.text.TextUtils.isEmpty(r2)     // Catch:{ all -> 0x0163 }
                if (r5 == 0) goto L_0x010d
                r2 = r4
            L_0x010d:
                r26 = r2
                goto L_0x0115
            L_0x0110:
                r26 = r4
                goto L_0x0115
            L_0x0113:
                r26 = r34
            L_0x0115:
                java.lang.String r23 = r33.getName()     // Catch:{ all -> 0x0163 }
                java.lang.String r24 = r33.getName()     // Catch:{ all -> 0x0163 }
                r25 = 0
                java.lang.String r27 = r33.getAbsolutePath()     // Catch:{ all -> 0x0163 }
                long r28 = r33.length()     // Catch:{ all -> 0x0163 }
                r30 = 1
                r22.addCompletedDownload(r23, r24, r25, r26, r27, r28, r30)     // Catch:{ all -> 0x0163 }
            L_0x012c:
                float r2 = r1.finishedProgress     // Catch:{ all -> 0x0163 }
                java.util.ArrayList<org.telegram.messenger.MessageObject> r4 = r1.messageObjects     // Catch:{ all -> 0x0163 }
                int r4 = r4.size()     // Catch:{ all -> 0x0163 }
                float r4 = (float) r4     // Catch:{ all -> 0x0163 }
                float r0 = r0 / r4
                float r2 = r2 + r0
                r1.finishedProgress = r2     // Catch:{ all -> 0x0163 }
                int r0 = (int) r2     // Catch:{ all -> 0x0163 }
                org.telegram.messenger.-$$Lambda$MediaController$MediaLoader$rkTlsTzRKy1M7QGQqs1I4zaTL-k r2 = new org.telegram.messenger.-$$Lambda$MediaController$MediaLoader$rkTlsTzRKy1M7QGQqs1I4zaTL-k     // Catch:{ all -> 0x0163 }
                r2.<init>(r0)     // Catch:{ all -> 0x0163 }
                org.telegram.messenger.AndroidUtilities.runOnUIThread(r2)     // Catch:{ all -> 0x0163 }
                int r0 = r1.copiedFiles     // Catch:{ all -> 0x0163 }
                int r0 = r0 + r3
                r1.copiedFiles = r0     // Catch:{ all -> 0x0163 }
                if (r12 == 0) goto L_0x014c
                r12.close()     // Catch:{ all -> 0x0171 }
            L_0x014c:
                if (r10 == 0) goto L_0x0151
                r10.close()     // Catch:{ all -> 0x0183 }
            L_0x0151:
                r21.close()     // Catch:{ Exception -> 0x0191 }
                return r3
            L_0x0155:
                if (r12 == 0) goto L_0x015a
                r12.close()     // Catch:{ all -> 0x0171 }
            L_0x015a:
                if (r10 == 0) goto L_0x015f
                r10.close()     // Catch:{ all -> 0x0183 }
            L_0x015f:
                r21.close()     // Catch:{ Exception -> 0x0191 }
                goto L_0x0199
            L_0x0163:
                r0 = move-exception
                goto L_0x0168
            L_0x0165:
                r0 = move-exception
                r21 = r3
            L_0x0168:
                throw r0     // Catch:{ all -> 0x0169 }
            L_0x0169:
                r0 = move-exception
                r2 = r0
                if (r12 == 0) goto L_0x0170
                r12.close()     // Catch:{ all -> 0x0170 }
            L_0x0170:
                throw r2     // Catch:{ all -> 0x0171 }
            L_0x0171:
                r0 = move-exception
                goto L_0x017a
            L_0x0173:
                r0 = move-exception
                goto L_0x0178
            L_0x0175:
                r0 = move-exception
                r11 = r33
            L_0x0178:
                r21 = r3
            L_0x017a:
                throw r0     // Catch:{ all -> 0x017b }
            L_0x017b:
                r0 = move-exception
                r2 = r0
                if (r10 == 0) goto L_0x0182
                r10.close()     // Catch:{ all -> 0x0182 }
            L_0x0182:
                throw r2     // Catch:{ all -> 0x0183 }
            L_0x0183:
                r0 = move-exception
                goto L_0x018a
            L_0x0185:
                r0 = move-exception
                r11 = r33
            L_0x0188:
                r21 = r3
            L_0x018a:
                throw r0     // Catch:{ all -> 0x018b }
            L_0x018b:
                r0 = move-exception
                r2 = r0
                r21.close()     // Catch:{ all -> 0x0190 }
            L_0x0190:
                throw r2     // Catch:{ Exception -> 0x0191 }
            L_0x0191:
                r0 = move-exception
                goto L_0x0196
            L_0x0193:
                r0 = move-exception
                r11 = r33
            L_0x0196:
                org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            L_0x0199:
                r33.delete()
                r2 = 0
                return r2
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.MediaLoader.copyFile(java.io.File, java.io.File, java.lang.String):boolean");
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$copyFile$6 */
        public /* synthetic */ void lambda$copyFile$6$MediaController$MediaLoader() {
            try {
                this.progressDialog.dismiss();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$copyFile$7 */
        public /* synthetic */ void lambda$copyFile$7$MediaController$MediaLoader(int i) {
            try {
                this.progressDialog.setProgress(i);
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$copyFile$8 */
        public /* synthetic */ void lambda$copyFile$8$MediaController$MediaLoader(int i) {
            try {
                this.progressDialog.setProgress(i);
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }

        public void didReceivedNotification(int i, int i2, Object... objArr) {
            if (i == NotificationCenter.fileDidLoad || i == NotificationCenter.fileDidFailToLoad) {
                if (this.loadingMessageObjects.remove(objArr[0]) != null) {
                    this.waitingForFile.countDown();
                }
            } else if (i == NotificationCenter.FileLoadProgressChanged) {
                if (this.loadingMessageObjects.containsKey(objArr[0])) {
                    AndroidUtilities.runOnUIThread(new Runnable((int) (this.finishedProgress + (((((float) objArr[1].longValue()) / ((float) objArr[2].longValue())) / ((float) this.messageObjects.size())) * 100.0f))) {
                        public final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            MediaController.MediaLoader.this.lambda$didReceivedNotification$9$MediaController$MediaLoader(this.f$1);
                        }
                    });
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$didReceivedNotification$9 */
        public /* synthetic */ void lambda$didReceivedNotification$9$MediaController$MediaLoader(int i) {
            try {
                this.progressDialog.setProgress(i);
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }
    }

    public static void saveFilesFromMessages(Context context, AccountInstance accountInstance, ArrayList<MessageObject> arrayList, MessagesStorage.IntCallback intCallback) {
        if (arrayList != null && !arrayList.isEmpty()) {
            new MediaLoader(context, accountInstance, arrayList, intCallback).start();
        }
    }

    public static void saveFile(String str, Context context, int i, String str2, String str3) {
        saveFile(str, context, i, str2, str3, (Runnable) null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0027 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0028  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void saveFile(java.lang.String r13, android.content.Context r14, int r15, java.lang.String r16, java.lang.String r17, java.lang.Runnable r18) {
        /*
            r0 = r13
            r1 = r14
            if (r0 != 0) goto L_0x0005
            return
        L_0x0005:
            boolean r2 = android.text.TextUtils.isEmpty(r13)
            r3 = 0
            if (r2 != 0) goto L_0x0024
            java.io.File r2 = new java.io.File
            r2.<init>(r13)
            boolean r0 = r2.exists()
            if (r0 == 0) goto L_0x0024
            android.net.Uri r0 = android.net.Uri.fromFile(r2)
            boolean r0 = org.telegram.messenger.AndroidUtilities.isInternalUri((android.net.Uri) r0)
            if (r0 == 0) goto L_0x0022
            goto L_0x0024
        L_0x0022:
            r6 = r2
            goto L_0x0025
        L_0x0024:
            r6 = r3
        L_0x0025:
            if (r6 != 0) goto L_0x0028
            return
        L_0x0028:
            r0 = 1
            boolean[] r9 = new boolean[r0]
            r2 = 0
            r9[r2] = r2
            boolean r4 = r6.exists()
            if (r4 == 0) goto L_0x0080
            boolean[] r12 = new boolean[r0]
            if (r1 == 0) goto L_0x006a
            if (r15 == 0) goto L_0x006a
            org.telegram.ui.ActionBar.AlertDialog r4 = new org.telegram.ui.ActionBar.AlertDialog     // Catch:{ Exception -> 0x0066 }
            r5 = 2
            r4.<init>(r14, r5)     // Catch:{ Exception -> 0x0066 }
            java.lang.String r1 = "Loading"
            r5 = 2131625956(0x7f0e07e4, float:1.8879135E38)
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r1, r5)     // Catch:{ Exception -> 0x0066 }
            r4.setMessage(r1)     // Catch:{ Exception -> 0x0066 }
            r4.setCanceledOnTouchOutside(r2)     // Catch:{ Exception -> 0x0066 }
            r4.setCancelable(r0)     // Catch:{ Exception -> 0x0066 }
            org.telegram.messenger.-$$Lambda$MediaController$BRiDc_pzXRkNP_zjIe9VtWBqJzA r0 = new org.telegram.messenger.-$$Lambda$MediaController$BRiDc_pzXRkNP_zjIe9VtWBqJzA     // Catch:{ Exception -> 0x0066 }
            r0.<init>(r9)     // Catch:{ Exception -> 0x0066 }
            r4.setOnCancelListener(r0)     // Catch:{ Exception -> 0x0066 }
            org.telegram.messenger.-$$Lambda$MediaController$4ovvS8ZKx9LGHvVfu0zBSuzreiE r0 = new org.telegram.messenger.-$$Lambda$MediaController$4ovvS8ZKx9LGHvVfu0zBSuzreiE     // Catch:{ Exception -> 0x0066 }
            r0.<init>(r12, r4)     // Catch:{ Exception -> 0x0066 }
            r1 = 250(0xfa, double:1.235E-321)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r0, r1)     // Catch:{ Exception -> 0x0066 }
            r8 = r4
            goto L_0x006b
        L_0x0066:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x006a:
            r8 = r3
        L_0x006b:
            java.lang.Thread r0 = new java.lang.Thread
            org.telegram.messenger.-$$Lambda$MediaController$lLDbCcwxYwZD9cDkIz2oiM4dMLQ r1 = new org.telegram.messenger.-$$Lambda$MediaController$lLDbCcwxYwZD9cDkIz2oiM4dMLQ
            r4 = r1
            r5 = r15
            r7 = r16
            r10 = r17
            r11 = r18
            r4.<init>(r5, r6, r7, r8, r9, r10, r11, r12)
            r0.<init>(r1)
            r0.start()
        L_0x0080:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.saveFile(java.lang.String, android.content.Context, int, java.lang.String, java.lang.String, java.lang.Runnable):void");
    }

    static /* synthetic */ void lambda$saveFile$33(boolean[] zArr, DialogInterface dialogInterface) {
        zArr[0] = true;
    }

    static /* synthetic */ void lambda$saveFile$34(boolean[] zArr, AlertDialog alertDialog) {
        if (!zArr[0]) {
            alertDialog.show();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x016e, code lost:
        if (r18 != null) goto L_0x0170;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:?, code lost:
        r18.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:?, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0174, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x015e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x015f, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0160, code lost:
        if (r19 != null) goto L_0x0162;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:?, code lost:
        r19.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:?, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0166, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x016c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x016d, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:74:0x014c, B:84:0x015d, B:91:0x0165] */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:77:0x0151, B:96:0x016b, B:103:0x0173] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:103:0x0173 */
    /* JADX WARNING: Missing exception handler attribute for start block: B:114:0x017f */
    /* JADX WARNING: Missing exception handler attribute for start block: B:91:0x0165 */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x018a A[Catch:{ Exception -> 0x0014 }] */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x018f A[Catch:{ Exception -> 0x0014 }] */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x0192 A[Catch:{ Exception -> 0x0014 }] */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x01cb  */
    /* JADX WARNING: Removed duplicated region for block: B:141:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00a0 A[Catch:{ Exception -> 0x0014 }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00e2 A[Catch:{ all -> 0x00fa, all -> 0x015a }] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0109  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x014c A[SYNTHETIC, Splitter:B:74:0x014c] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0151 A[SYNTHETIC, Splitter:B:77:0x0151] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:79:0x0154=Splitter:B:79:0x0154, B:52:0x00f6=Splitter:B:52:0x00f6, B:114:0x017f=Splitter:B:114:0x017f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static /* synthetic */ void lambda$saveFile$38(int r20, java.io.File r21, java.lang.String r22, org.telegram.ui.ActionBar.AlertDialog r23, boolean[] r24, java.lang.String r25, java.lang.Runnable r26, boolean[] r27) {
        /*
            r1 = r20
            r0 = r22
            r2 = r23
            r3 = 2
            r4 = 1
            r5 = 0
            if (r1 != 0) goto L_0x0017
            java.lang.String r0 = org.telegram.messenger.FileLoader.getFileExtension(r21)     // Catch:{ Exception -> 0x0014 }
            java.io.File r0 = org.telegram.messenger.AndroidUtilities.generatePicturePath(r5, r0)     // Catch:{ Exception -> 0x0014 }
            goto L_0x001d
        L_0x0014:
            r0 = move-exception
            goto L_0x01c6
        L_0x0017:
            if (r1 != r4) goto L_0x0020
            java.io.File r0 = org.telegram.messenger.AndroidUtilities.generateVideoPath()     // Catch:{ Exception -> 0x0014 }
        L_0x001d:
            r7 = r0
            goto L_0x009a
        L_0x0020:
            if (r1 != r3) goto L_0x0029
            java.lang.String r6 = android.os.Environment.DIRECTORY_DOWNLOADS     // Catch:{ Exception -> 0x0014 }
            java.io.File r6 = android.os.Environment.getExternalStoragePublicDirectory(r6)     // Catch:{ Exception -> 0x0014 }
            goto L_0x002f
        L_0x0029:
            java.lang.String r6 = android.os.Environment.DIRECTORY_MUSIC     // Catch:{ Exception -> 0x0014 }
            java.io.File r6 = android.os.Environment.getExternalStoragePublicDirectory(r6)     // Catch:{ Exception -> 0x0014 }
        L_0x002f:
            r6.mkdir()     // Catch:{ Exception -> 0x0014 }
            java.io.File r7 = new java.io.File     // Catch:{ Exception -> 0x0014 }
            r7.<init>(r6, r0)     // Catch:{ Exception -> 0x0014 }
            boolean r8 = r7.exists()     // Catch:{ Exception -> 0x0014 }
            if (r8 == 0) goto L_0x009a
            r8 = 46
            int r8 = r0.lastIndexOf(r8)     // Catch:{ Exception -> 0x0014 }
            r9 = 0
        L_0x0044:
            r10 = 10
            if (r9 >= r10) goto L_0x009a
            r7 = -1
            java.lang.String r10 = ")"
            java.lang.String r11 = "("
            if (r8 == r7) goto L_0x0072
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0014 }
            r7.<init>()     // Catch:{ Exception -> 0x0014 }
            java.lang.String r12 = r0.substring(r5, r8)     // Catch:{ Exception -> 0x0014 }
            r7.append(r12)     // Catch:{ Exception -> 0x0014 }
            r7.append(r11)     // Catch:{ Exception -> 0x0014 }
            int r11 = r9 + 1
            r7.append(r11)     // Catch:{ Exception -> 0x0014 }
            r7.append(r10)     // Catch:{ Exception -> 0x0014 }
            java.lang.String r10 = r0.substring(r8)     // Catch:{ Exception -> 0x0014 }
            r7.append(r10)     // Catch:{ Exception -> 0x0014 }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x0014 }
            goto L_0x0089
        L_0x0072:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0014 }
            r7.<init>()     // Catch:{ Exception -> 0x0014 }
            r7.append(r0)     // Catch:{ Exception -> 0x0014 }
            r7.append(r11)     // Catch:{ Exception -> 0x0014 }
            int r11 = r9 + 1
            r7.append(r11)     // Catch:{ Exception -> 0x0014 }
            r7.append(r10)     // Catch:{ Exception -> 0x0014 }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x0014 }
        L_0x0089:
            java.io.File r10 = new java.io.File     // Catch:{ Exception -> 0x0014 }
            r10.<init>(r6, r7)     // Catch:{ Exception -> 0x0014 }
            boolean r7 = r10.exists()     // Catch:{ Exception -> 0x0014 }
            if (r7 != 0) goto L_0x0096
            r7 = r10
            goto L_0x009a
        L_0x0096:
            int r9 = r9 + 1
            r7 = r10
            goto L_0x0044
        L_0x009a:
            boolean r0 = r7.exists()     // Catch:{ Exception -> 0x0014 }
            if (r0 != 0) goto L_0x00a3
            r7.createNewFile()     // Catch:{ Exception -> 0x0014 }
        L_0x00a3:
            long r8 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x0014 }
            r10 = 500(0x1f4, double:2.47E-321)
            long r8 = r8 - r10
            java.io.FileInputStream r6 = new java.io.FileInputStream     // Catch:{ Exception -> 0x0180 }
            r0 = r21
            r6.<init>(r0)     // Catch:{ Exception -> 0x0180 }
            java.nio.channels.FileChannel r18 = r6.getChannel()     // Catch:{ all -> 0x0176 }
            java.io.FileOutputStream r0 = new java.io.FileOutputStream     // Catch:{ all -> 0x0168 }
            r0.<init>(r7)     // Catch:{ all -> 0x0168 }
            java.nio.channels.FileChannel r19 = r0.getChannel()     // Catch:{ all -> 0x0168 }
            long r14 = r18.size()     // Catch:{ all -> 0x015a }
            java.lang.Class<java.io.FileDescriptor> r0 = java.io.FileDescriptor.class
            java.lang.String r12 = "getInt$"
            java.lang.Class[] r13 = new java.lang.Class[r5]     // Catch:{ all -> 0x00fa }
            java.lang.reflect.Method r0 = r0.getDeclaredMethod(r12, r13)     // Catch:{ all -> 0x00fa }
            java.io.FileDescriptor r12 = r6.getFD()     // Catch:{ all -> 0x00fa }
            java.lang.Object[] r13 = new java.lang.Object[r5]     // Catch:{ all -> 0x00fa }
            java.lang.Object r0 = r0.invoke(r12, r13)     // Catch:{ all -> 0x00fa }
            java.lang.Integer r0 = (java.lang.Integer) r0     // Catch:{ all -> 0x00fa }
            int r0 = r0.intValue()     // Catch:{ all -> 0x00fa }
            boolean r0 = org.telegram.messenger.AndroidUtilities.isInternalUri((int) r0)     // Catch:{ all -> 0x00fa }
            if (r0 == 0) goto L_0x00fe
            if (r2 == 0) goto L_0x00ec
            org.telegram.messenger.-$$Lambda$MediaController$Nnzv67BfVXhlLWmZKv1aRMvdq_M r0 = new org.telegram.messenger.-$$Lambda$MediaController$Nnzv67BfVXhlLWmZKv1aRMvdq_M     // Catch:{ all -> 0x00fa }
            r0.<init>()     // Catch:{ all -> 0x00fa }
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r0)     // Catch:{ all -> 0x00fa }
        L_0x00ec:
            if (r19 == 0) goto L_0x00f1
            r19.close()     // Catch:{ all -> 0x0168 }
        L_0x00f1:
            if (r18 == 0) goto L_0x00f6
            r18.close()     // Catch:{ all -> 0x0176 }
        L_0x00f6:
            r6.close()     // Catch:{ Exception -> 0x0180 }
            return
        L_0x00fa:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)     // Catch:{ all -> 0x015a }
        L_0x00fe:
            r12 = 0
        L_0x0100:
            int r0 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1))
            if (r0 >= 0) goto L_0x0148
            boolean r0 = r24[r5]     // Catch:{ all -> 0x015a }
            if (r0 == 0) goto L_0x0109
            goto L_0x0148
        L_0x0109:
            long r3 = r14 - r12
            r22 = r6
            r5 = 4096(0x1000, double:2.0237E-320)
            long r16 = java.lang.Math.min(r5, r3)     // Catch:{ all -> 0x0146 }
            r3 = r12
            r12 = r19
            r13 = r18
            r5 = r14
            r14 = r3
            r12.transferFrom(r13, r14, r16)     // Catch:{ all -> 0x0146 }
            if (r2 == 0) goto L_0x013c
            long r12 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x0146 }
            long r12 = r12 - r10
            int r0 = (r8 > r12 ? 1 : (r8 == r12 ? 0 : -1))
            if (r0 > 0) goto L_0x013c
            long r8 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x0146 }
            float r0 = (float) r3     // Catch:{ all -> 0x0146 }
            float r12 = (float) r5     // Catch:{ all -> 0x0146 }
            float r0 = r0 / r12
            r12 = 1120403456(0x42CLASSNAME, float:100.0)
            float r0 = r0 * r12
            int r0 = (int) r0     // Catch:{ all -> 0x0146 }
            org.telegram.messenger.-$$Lambda$MediaController$ajb4ultJdUDtupAuVv042t0yNC8 r12 = new org.telegram.messenger.-$$Lambda$MediaController$ajb4ultJdUDtupAuVv042t0yNC8     // Catch:{ all -> 0x0146 }
            r12.<init>(r0)     // Catch:{ all -> 0x0146 }
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r12)     // Catch:{ all -> 0x0146 }
        L_0x013c:
            r12 = 4096(0x1000, double:2.0237E-320)
            long r12 = r12 + r3
            r14 = r5
            r3 = 2
            r4 = 1
            r5 = 0
            r6 = r22
            goto L_0x0100
        L_0x0146:
            r0 = move-exception
            goto L_0x015d
        L_0x0148:
            r22 = r6
            if (r19 == 0) goto L_0x014f
            r19.close()     // Catch:{ all -> 0x0166 }
        L_0x014f:
            if (r18 == 0) goto L_0x0154
            r18.close()     // Catch:{ all -> 0x0174 }
        L_0x0154:
            r22.close()     // Catch:{ Exception -> 0x0180 }
            r3 = 0
            r4 = 1
            goto L_0x0186
        L_0x015a:
            r0 = move-exception
            r22 = r6
        L_0x015d:
            throw r0     // Catch:{ all -> 0x015e }
        L_0x015e:
            r0 = move-exception
            r3 = r0
            if (r19 == 0) goto L_0x0165
            r19.close()     // Catch:{ all -> 0x0165 }
        L_0x0165:
            throw r3     // Catch:{ all -> 0x0166 }
        L_0x0166:
            r0 = move-exception
            goto L_0x016b
        L_0x0168:
            r0 = move-exception
            r22 = r6
        L_0x016b:
            throw r0     // Catch:{ all -> 0x016c }
        L_0x016c:
            r0 = move-exception
            r3 = r0
            if (r18 == 0) goto L_0x0173
            r18.close()     // Catch:{ all -> 0x0173 }
        L_0x0173:
            throw r3     // Catch:{ all -> 0x0174 }
        L_0x0174:
            r0 = move-exception
            goto L_0x0179
        L_0x0176:
            r0 = move-exception
            r22 = r6
        L_0x0179:
            throw r0     // Catch:{ all -> 0x017a }
        L_0x017a:
            r0 = move-exception
            r3 = r0
            r22.close()     // Catch:{ all -> 0x017f }
        L_0x017f:
            throw r3     // Catch:{ Exception -> 0x0180 }
        L_0x0180:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)     // Catch:{ Exception -> 0x0014 }
            r3 = 0
            r4 = 0
        L_0x0186:
            boolean r0 = r24[r3]     // Catch:{ Exception -> 0x0014 }
            if (r0 == 0) goto L_0x018f
            r7.delete()     // Catch:{ Exception -> 0x0014 }
            r5 = 0
            goto L_0x0190
        L_0x018f:
            r5 = r4
        L_0x0190:
            if (r5 == 0) goto L_0x01c9
            r3 = 2
            if (r1 != r3) goto L_0x01b9
            android.content.Context r0 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0014 }
            java.lang.String r1 = "download"
            java.lang.Object r0 = r0.getSystemService(r1)     // Catch:{ Exception -> 0x0014 }
            r8 = r0
            android.app.DownloadManager r8 = (android.app.DownloadManager) r8     // Catch:{ Exception -> 0x0014 }
            java.lang.String r9 = r7.getName()     // Catch:{ Exception -> 0x0014 }
            java.lang.String r10 = r7.getName()     // Catch:{ Exception -> 0x0014 }
            r11 = 0
            java.lang.String r13 = r7.getAbsolutePath()     // Catch:{ Exception -> 0x0014 }
            long r14 = r7.length()     // Catch:{ Exception -> 0x0014 }
            r16 = 1
            r12 = r25
            r8.addCompletedDownload(r9, r10, r11, r12, r13, r14, r16)     // Catch:{ Exception -> 0x0014 }
            goto L_0x01c0
        L_0x01b9:
            android.net.Uri r0 = android.net.Uri.fromFile(r7)     // Catch:{ Exception -> 0x0014 }
            org.telegram.messenger.AndroidUtilities.addMediaToGallery((android.net.Uri) r0)     // Catch:{ Exception -> 0x0014 }
        L_0x01c0:
            if (r26 == 0) goto L_0x01c9
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r26)     // Catch:{ Exception -> 0x0014 }
            goto L_0x01c9
        L_0x01c6:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x01c9:
            if (r2 == 0) goto L_0x01d5
            org.telegram.messenger.-$$Lambda$MediaController$XQyblpNQGfsp8JlZDOUs7YDcFcw r0 = new org.telegram.messenger.-$$Lambda$MediaController$XQyblpNQGfsp8JlZDOUs7YDcFcw
            r1 = r27
            r0.<init>(r1)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r0)
        L_0x01d5:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.lambda$saveFile$38(int, java.io.File, java.lang.String, org.telegram.ui.ActionBar.AlertDialog, boolean[], java.lang.String, java.lang.Runnable, boolean[]):void");
    }

    static /* synthetic */ void lambda$null$35(AlertDialog alertDialog) {
        try {
            alertDialog.dismiss();
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    static /* synthetic */ void lambda$null$36(AlertDialog alertDialog, int i) {
        try {
            alertDialog.setProgress(i);
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    static /* synthetic */ void lambda$null$37(AlertDialog alertDialog, boolean[] zArr) {
        try {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            } else {
                zArr[0] = true;
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    public static boolean isWebp(Uri uri) {
        InputStream inputStream = null;
        try {
            InputStream openInputStream = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri);
            byte[] bArr = new byte[12];
            if (openInputStream.read(bArr, 0, 12) == 12) {
                String lowerCase = new String(bArr).toLowerCase();
                if (lowerCase.startsWith("riff") && lowerCase.endsWith("webp")) {
                    if (openInputStream != null) {
                        try {
                            openInputStream.close();
                        } catch (Exception e) {
                            FileLog.e((Throwable) e);
                        }
                    }
                    return true;
                }
            }
            if (openInputStream != null) {
                try {
                    openInputStream.close();
                } catch (Exception e2) {
                    FileLog.e((Throwable) e2);
                }
            }
        } catch (Exception e3) {
            FileLog.e((Throwable) e3);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e4) {
                    FileLog.e((Throwable) e4);
                }
            }
            throw th;
        }
        return false;
    }

    public static boolean isGif(Uri uri) {
        InputStream inputStream = null;
        try {
            InputStream openInputStream = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri);
            byte[] bArr = new byte[3];
            if (openInputStream.read(bArr, 0, 3) != 3 || !new String(bArr).equalsIgnoreCase("gif")) {
                if (openInputStream != null) {
                    try {
                        openInputStream.close();
                    } catch (Exception e) {
                        FileLog.e((Throwable) e);
                    }
                }
                return false;
            }
            if (openInputStream != null) {
                try {
                    openInputStream.close();
                } catch (Exception e2) {
                    FileLog.e((Throwable) e2);
                }
            }
            return true;
        } catch (Exception e3) {
            FileLog.e((Throwable) e3);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e4) {
                    FileLog.e((Throwable) e4);
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0040, code lost:
        if (r3 != null) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0045 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getFileName(android.net.Uri r11) {
        /*
            java.lang.String r0 = "_display_name"
            java.lang.String r1 = ""
            if (r11 != 0) goto L_0x0007
            return r1
        L_0x0007:
            r2 = 0
            java.lang.String r3 = r11.getScheme()     // Catch:{ Exception -> 0x005f }
            java.lang.String r4 = "content"
            boolean r3 = r3.equals(r4)     // Catch:{ Exception -> 0x005f }
            r4 = 1
            if (r3 == 0) goto L_0x004a
            android.content.Context r3 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x0046 }
            android.content.ContentResolver r5 = r3.getContentResolver()     // Catch:{ Exception -> 0x0046 }
            java.lang.String[] r7 = new java.lang.String[r4]     // Catch:{ Exception -> 0x0046 }
            r3 = 0
            r7[r3] = r0     // Catch:{ Exception -> 0x0046 }
            r8 = 0
            r9 = 0
            r10 = 0
            r6 = r11
            android.database.Cursor r3 = r5.query(r6, r7, r8, r9, r10)     // Catch:{ Exception -> 0x0046 }
            boolean r5 = r3.moveToFirst()     // Catch:{ all -> 0x003d }
            if (r5 == 0) goto L_0x0037
            int r0 = r3.getColumnIndex(r0)     // Catch:{ all -> 0x003d }
            java.lang.String r0 = r3.getString(r0)     // Catch:{ all -> 0x003d }
            r2 = r0
        L_0x0037:
            if (r3 == 0) goto L_0x004a
            r3.close()     // Catch:{ Exception -> 0x0046 }
            goto L_0x004a
        L_0x003d:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x003f }
        L_0x003f:
            r0 = move-exception
            if (r3 == 0) goto L_0x0045
            r3.close()     // Catch:{ all -> 0x0045 }
        L_0x0045:
            throw r0     // Catch:{ Exception -> 0x0046 }
        L_0x0046:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)     // Catch:{ Exception -> 0x005f }
        L_0x004a:
            if (r2 != 0) goto L_0x005e
            java.lang.String r2 = r11.getPath()     // Catch:{ Exception -> 0x005f }
            r11 = 47
            int r11 = r2.lastIndexOf(r11)     // Catch:{ Exception -> 0x005f }
            r0 = -1
            if (r11 == r0) goto L_0x005e
            int r11 = r11 + r4
            java.lang.String r2 = r2.substring(r11)     // Catch:{ Exception -> 0x005f }
        L_0x005e:
            return r2
        L_0x005f:
            r11 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r11)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.getFileName(android.net.Uri):java.lang.String");
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x00cb A[SYNTHETIC, Splitter:B:59:0x00cb] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00d5 A[SYNTHETIC, Splitter:B:64:0x00d5] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x00e3 A[SYNTHETIC, Splitter:B:72:0x00e3] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00ed A[SYNTHETIC, Splitter:B:77:0x00ed] */
    @android.annotation.SuppressLint({"DiscouragedPrivateApi"})
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String copyFileToCache(android.net.Uri r7, java.lang.String r8) {
        /*
            r0 = 0
            java.lang.String r1 = getFileName(r7)     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            java.lang.String r1 = org.telegram.messenger.FileLoader.fixFileName(r1)     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            r2 = 0
            if (r1 != 0) goto L_0x0027
            int r1 = org.telegram.messenger.SharedConfig.getLastLocalId()     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            org.telegram.messenger.SharedConfig.saveConfig()     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            java.util.Locale r3 = java.util.Locale.US     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            java.lang.String r4 = "%d.%s"
            r5 = 2
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            r5[r2] = r1     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            r1 = 1
            r5[r1] = r8     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            java.lang.String r1 = java.lang.String.format(r3, r4, r5)     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
        L_0x0027:
            java.io.File r8 = org.telegram.messenger.AndroidUtilities.getSharingDirectory()     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            r8.mkdirs()     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            java.io.File r3 = new java.io.File     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            r3.<init>(r8, r1)     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            android.net.Uri r8 = android.net.Uri.fromFile(r3)     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            boolean r8 = org.telegram.messenger.AndroidUtilities.isInternalUri((android.net.Uri) r8)     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            if (r8 == 0) goto L_0x003e
            return r0
        L_0x003e:
            android.content.Context r8 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            android.content.ContentResolver r8 = r8.getContentResolver()     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            java.io.InputStream r7 = r8.openInputStream(r7)     // Catch:{ Exception -> 0x00c3, all -> 0x00c0 }
            boolean r8 = r7 instanceof java.io.FileInputStream     // Catch:{ Exception -> 0x00ba, all -> 0x00b4 }
            if (r8 == 0) goto L_0x007e
            r8 = r7
            java.io.FileInputStream r8 = (java.io.FileInputStream) r8     // Catch:{ Exception -> 0x00ba, all -> 0x00b4 }
            java.lang.Class<java.io.FileDescriptor> r1 = java.io.FileDescriptor.class
            java.lang.String r4 = "getInt$"
            java.lang.Class[] r5 = new java.lang.Class[r2]     // Catch:{ all -> 0x007a }
            java.lang.reflect.Method r1 = r1.getDeclaredMethod(r4, r5)     // Catch:{ all -> 0x007a }
            java.io.FileDescriptor r8 = r8.getFD()     // Catch:{ all -> 0x007a }
            java.lang.Object[] r4 = new java.lang.Object[r2]     // Catch:{ all -> 0x007a }
            java.lang.Object r8 = r1.invoke(r8, r4)     // Catch:{ all -> 0x007a }
            java.lang.Integer r8 = (java.lang.Integer) r8     // Catch:{ all -> 0x007a }
            int r8 = r8.intValue()     // Catch:{ all -> 0x007a }
            boolean r8 = org.telegram.messenger.AndroidUtilities.isInternalUri((int) r8)     // Catch:{ all -> 0x007a }
            if (r8 == 0) goto L_0x007e
            if (r7 == 0) goto L_0x0079
            r7.close()     // Catch:{ Exception -> 0x0075 }
            goto L_0x0079
        L_0x0075:
            r7 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r7)
        L_0x0079:
            return r0
        L_0x007a:
            r8 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r8)     // Catch:{ Exception -> 0x00ba, all -> 0x00b4 }
        L_0x007e:
            java.io.FileOutputStream r8 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x00ba, all -> 0x00b4 }
            r8.<init>(r3)     // Catch:{ Exception -> 0x00ba, all -> 0x00b4 }
            r1 = 20480(0x5000, float:2.8699E-41)
            byte[] r1 = new byte[r1]     // Catch:{ Exception -> 0x00ae, all -> 0x00a9 }
        L_0x0087:
            int r4 = r7.read(r1)     // Catch:{ Exception -> 0x00ae, all -> 0x00a9 }
            r5 = -1
            if (r4 == r5) goto L_0x0092
            r8.write(r1, r2, r4)     // Catch:{ Exception -> 0x00ae, all -> 0x00a9 }
            goto L_0x0087
        L_0x0092:
            java.lang.String r0 = r3.getAbsolutePath()     // Catch:{ Exception -> 0x00ae, all -> 0x00a9 }
            if (r7 == 0) goto L_0x00a0
            r7.close()     // Catch:{ Exception -> 0x009c }
            goto L_0x00a0
        L_0x009c:
            r7 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r7)
        L_0x00a0:
            r8.close()     // Catch:{ Exception -> 0x00a4 }
            goto L_0x00a8
        L_0x00a4:
            r7 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r7)
        L_0x00a8:
            return r0
        L_0x00a9:
            r0 = move-exception
            r6 = r0
            r0 = r7
            r7 = r6
            goto L_0x00e1
        L_0x00ae:
            r1 = move-exception
            r6 = r8
            r8 = r7
            r7 = r1
            r1 = r6
            goto L_0x00c6
        L_0x00b4:
            r8 = move-exception
            r6 = r0
            r0 = r7
            r7 = r8
            r8 = r6
            goto L_0x00e1
        L_0x00ba:
            r8 = move-exception
            r1 = r0
            r6 = r8
            r8 = r7
            r7 = r6
            goto L_0x00c6
        L_0x00c0:
            r7 = move-exception
            r8 = r0
            goto L_0x00e1
        L_0x00c3:
            r7 = move-exception
            r8 = r0
            r1 = r8
        L_0x00c6:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r7)     // Catch:{ all -> 0x00de }
            if (r8 == 0) goto L_0x00d3
            r8.close()     // Catch:{ Exception -> 0x00cf }
            goto L_0x00d3
        L_0x00cf:
            r7 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r7)
        L_0x00d3:
            if (r1 == 0) goto L_0x00dd
            r1.close()     // Catch:{ Exception -> 0x00d9 }
            goto L_0x00dd
        L_0x00d9:
            r7 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r7)
        L_0x00dd:
            return r0
        L_0x00de:
            r7 = move-exception
            r0 = r8
            r8 = r1
        L_0x00e1:
            if (r0 == 0) goto L_0x00eb
            r0.close()     // Catch:{ Exception -> 0x00e7 }
            goto L_0x00eb
        L_0x00e7:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x00eb:
            if (r8 == 0) goto L_0x00f5
            r8.close()     // Catch:{ Exception -> 0x00f1 }
            goto L_0x00f5
        L_0x00f1:
            r8 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r8)
        L_0x00f5:
            goto L_0x00f7
        L_0x00f6:
            throw r7
        L_0x00f7:
            goto L_0x00f6
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.copyFileToCache(android.net.Uri, java.lang.String):java.lang.String");
    }

    public static void loadGalleryPhotosAlbums(int i) {
        Thread thread = new Thread(new Runnable(i) {
            public final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                MediaController.lambda$loadGalleryPhotosAlbums$40(this.f$0);
            }
        });
        thread.setPriority(1);
        thread.start();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v0, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v1, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v8, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v9, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v10, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v11, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r32v13, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v12, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v13, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r32v15, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v14, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v15, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v16, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v14, resolved type: org.telegram.messenger.MediaController$AlbumEntry} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v17, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v18, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r32v20, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v19, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v23, resolved type: org.telegram.messenger.MediaController$AlbumEntry} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x0264  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x027a A[SYNTHETIC, Splitter:B:125:0x027a] */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x02b3 A[SYNTHETIC, Splitter:B:138:0x02b3] */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x02c4 A[ADDED_TO_REGION, Catch:{ all -> 0x0433 }] */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x02e9 A[Catch:{ all -> 0x0433 }] */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x02ec A[Catch:{ all -> 0x0433 }] */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x0300 A[Catch:{ all -> 0x0433 }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0062 A[Catch:{ all -> 0x0293 }] */
    /* JADX WARNING: Removed duplicated region for block: B:211:0x042c A[SYNTHETIC, Splitter:B:211:0x042c] */
    /* JADX WARNING: Removed duplicated region for block: B:220:0x043a A[SYNTHETIC, Splitter:B:220:0x043a] */
    /* JADX WARNING: Removed duplicated region for block: B:228:0x0450 A[LOOP:2: B:226:0x044a->B:228:0x0450, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00a2  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00b7 A[SYNTHETIC, Splitter:B:29:0x00b7] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static /* synthetic */ void lambda$loadGalleryPhotosAlbums$40(int r51) {
        /*
            java.lang.String r1 = "AllMedia"
            java.lang.String r2 = "_size"
            java.lang.String r3 = "height"
            java.lang.String r4 = "width"
            java.lang.String r5 = "_data"
            java.lang.String r6 = "bucket_display_name"
            java.lang.String r7 = "bucket_id"
            java.lang.String r8 = "_id"
            java.lang.String r9 = " DESC"
            java.lang.String r10 = "android.permission.READ_EXTERNAL_STORAGE"
            java.lang.String r11 = "date_modified"
            java.lang.String r12 = "datetaken"
            java.util.ArrayList r14 = new java.util.ArrayList
            r14.<init>()
            java.util.ArrayList r15 = new java.util.ArrayList
            r15.<init>()
            android.util.SparseArray r13 = new android.util.SparseArray
            r13.<init>()
            r16 = r11
            android.util.SparseArray r11 = new android.util.SparseArray
            r11.<init>()
            r17 = 0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0052 }
            r0.<init>()     // Catch:{ Exception -> 0x0052 }
            java.lang.String r18 = android.os.Environment.DIRECTORY_DCIM     // Catch:{ Exception -> 0x0052 }
            java.io.File r18 = android.os.Environment.getExternalStoragePublicDirectory(r18)     // Catch:{ Exception -> 0x0052 }
            r19 = r12
            java.lang.String r12 = r18.getAbsolutePath()     // Catch:{ Exception -> 0x0050 }
            r0.append(r12)     // Catch:{ Exception -> 0x0050 }
            java.lang.String r12 = "/Camera/"
            r0.append(r12)     // Catch:{ Exception -> 0x0050 }
            java.lang.String r0 = r0.toString()     // Catch:{ Exception -> 0x0050 }
            r12 = r0
            goto L_0x005a
        L_0x0050:
            r0 = move-exception
            goto L_0x0055
        L_0x0052:
            r0 = move-exception
            r19 = r12
        L_0x0055:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            r12 = r17
        L_0x005a:
            r18 = r11
            int r0 = android.os.Build.VERSION.SDK_INT     // Catch:{ all -> 0x0293 }
            r11 = 23
            if (r0 < r11) goto L_0x0089
            if (r0 < r11) goto L_0x006d
            android.content.Context r11 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ all -> 0x0293 }
            int r11 = r11.checkSelfPermission(r10)     // Catch:{ all -> 0x0293 }
            if (r11 != 0) goto L_0x006d
            goto L_0x0089
        L_0x006d:
            r35 = r2
            r30 = r3
            r28 = r4
            r26 = r5
            r25 = r6
            r24 = r7
            r23 = r8
            r27 = r9
            r29 = r10
            r10 = r17
            r31 = r10
        L_0x0083:
            r32 = r31
            r33 = r32
            goto L_0x0278
        L_0x0089:
            android.content.Context r11 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ all -> 0x0293 }
            android.content.ContentResolver r23 = r11.getContentResolver()     // Catch:{ all -> 0x0293 }
            android.net.Uri r24 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI     // Catch:{ all -> 0x0293 }
            java.lang.String[] r25 = projectionPhotos     // Catch:{ all -> 0x0293 }
            r26 = 0
            r27 = 0
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0293 }
            r11.<init>()     // Catch:{ all -> 0x0293 }
            r29 = r10
            r10 = 28
            if (r0 <= r10) goto L_0x00a5
            r10 = r16
            goto L_0x00a7
        L_0x00a5:
            r10 = r19
        L_0x00a7:
            r11.append(r10)     // Catch:{ all -> 0x0281 }
            r11.append(r9)     // Catch:{ all -> 0x0281 }
            java.lang.String r28 = r11.toString()     // Catch:{ all -> 0x0281 }
            android.database.Cursor r10 = android.provider.MediaStore.Images.Media.query(r23, r24, r25, r26, r27, r28)     // Catch:{ all -> 0x0281 }
            if (r10 == 0) goto L_0x0264
            int r11 = r10.getColumnIndex(r8)     // Catch:{ all -> 0x0250 }
            r23 = r8
            int r8 = r10.getColumnIndex(r7)     // Catch:{ all -> 0x0242 }
            r24 = r7
            int r7 = r10.getColumnIndex(r6)     // Catch:{ all -> 0x0236 }
            r25 = r6
            int r6 = r10.getColumnIndex(r5)     // Catch:{ all -> 0x022c }
            r26 = r5
            r5 = 28
            if (r0 <= r5) goto L_0x00d6
            r0 = r16
            goto L_0x00d8
        L_0x00d6:
            r0 = r19
        L_0x00d8:
            int r0 = r10.getColumnIndex(r0)     // Catch:{ all -> 0x0224 }
            java.lang.String r5 = "orientation"
            int r5 = r10.getColumnIndex(r5)     // Catch:{ all -> 0x0224 }
            r27 = r9
            int r9 = r10.getColumnIndex(r4)     // Catch:{ all -> 0x021c }
            r28 = r4
            int r4 = r10.getColumnIndex(r3)     // Catch:{ all -> 0x0216 }
            r30 = r3
            int r3 = r10.getColumnIndex(r2)     // Catch:{ all -> 0x0212 }
            r31 = r17
            r32 = r31
            r33 = r32
            r34 = r33
        L_0x00fc:
            boolean r35 = r10.moveToNext()     // Catch:{ all -> 0x020d }
            if (r35 == 0) goto L_0x0209
            r35 = r2
            java.lang.String r2 = r10.getString(r6)     // Catch:{ all -> 0x0206 }
            boolean r36 = android.text.TextUtils.isEmpty(r2)     // Catch:{ all -> 0x0206 }
            if (r36 == 0) goto L_0x0111
            r2 = r35
            goto L_0x00fc
        L_0x0111:
            int r38 = r10.getInt(r11)     // Catch:{ all -> 0x0206 }
            r48 = r6
            int r6 = r10.getInt(r8)     // Catch:{ all -> 0x0206 }
            r49 = r8
            java.lang.String r8 = r10.getString(r7)     // Catch:{ all -> 0x0206 }
            long r39 = r10.getLong(r0)     // Catch:{ all -> 0x0206 }
            int r42 = r10.getInt(r5)     // Catch:{ all -> 0x0206 }
            int r44 = r10.getInt(r9)     // Catch:{ all -> 0x0206 }
            int r45 = r10.getInt(r4)     // Catch:{ all -> 0x0206 }
            long r46 = r10.getLong(r3)     // Catch:{ all -> 0x0206 }
            r50 = r0
            org.telegram.messenger.MediaController$PhotoEntry r0 = new org.telegram.messenger.MediaController$PhotoEntry     // Catch:{ all -> 0x0206 }
            r43 = 0
            r36 = r0
            r37 = r6
            r41 = r2
            r36.<init>(r37, r38, r39, r41, r42, r43, r44, r45, r46)     // Catch:{ all -> 0x0206 }
            if (r31 != 0) goto L_0x015f
            r36 = r3
            org.telegram.messenger.MediaController$AlbumEntry r3 = new org.telegram.messenger.MediaController$AlbumEntry     // Catch:{ all -> 0x0206 }
            r37 = r4
            java.lang.String r4 = "AllPhotos"
            r38 = r5
            r5 = 2131624238(0x7f0e012e, float:1.887565E38)
            java.lang.String r4 = org.telegram.messenger.LocaleController.getString(r4, r5)     // Catch:{ all -> 0x0206 }
            r5 = 0
            r3.<init>(r5, r4, r0)     // Catch:{ all -> 0x0206 }
            r15.add(r5, r3)     // Catch:{ all -> 0x017c }
            goto L_0x0167
        L_0x015f:
            r36 = r3
            r37 = r4
            r38 = r5
            r3 = r31
        L_0x0167:
            if (r32 != 0) goto L_0x0181
            org.telegram.messenger.MediaController$AlbumEntry r4 = new org.telegram.messenger.MediaController$AlbumEntry     // Catch:{ all -> 0x017c }
            r39 = r7
            r5 = 2131624237(0x7f0e012d, float:1.8875648E38)
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r1, r5)     // Catch:{ all -> 0x017c }
            r5 = 0
            r4.<init>(r5, r7, r0)     // Catch:{ all -> 0x017c }
            r14.add(r5, r4)     // Catch:{ all -> 0x01ff }
            goto L_0x0185
        L_0x017c:
            r0 = move-exception
            r31 = r3
            goto L_0x02ae
        L_0x0181:
            r39 = r7
            r4 = r32
        L_0x0185:
            r3.addPhoto(r0)     // Catch:{ all -> 0x01ff }
            r4.addPhoto(r0)     // Catch:{ all -> 0x01ff }
            java.lang.Object r5 = r13.get(r6)     // Catch:{ all -> 0x01ff }
            org.telegram.messenger.MediaController$AlbumEntry r5 = (org.telegram.messenger.MediaController.AlbumEntry) r5     // Catch:{ all -> 0x01ff }
            if (r5 != 0) goto L_0x01b5
            org.telegram.messenger.MediaController$AlbumEntry r5 = new org.telegram.messenger.MediaController$AlbumEntry     // Catch:{ all -> 0x01ff }
            r5.<init>(r6, r8, r0)     // Catch:{ all -> 0x01ff }
            r13.put(r6, r5)     // Catch:{ all -> 0x01ff }
            if (r33 != 0) goto L_0x01b2
            if (r12 == 0) goto L_0x01b2
            if (r2 == 0) goto L_0x01b2
            boolean r7 = r2.startsWith(r12)     // Catch:{ all -> 0x01ff }
            if (r7 == 0) goto L_0x01b2
            r7 = 0
            r14.add(r7, r5)     // Catch:{ all -> 0x01ff }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x01ff }
            r33 = r7
            goto L_0x01b5
        L_0x01b2:
            r14.add(r5)     // Catch:{ all -> 0x01ff }
        L_0x01b5:
            r5.addPhoto(r0)     // Catch:{ all -> 0x01ff }
            r5 = r18
            java.lang.Object r7 = r5.get(r6)     // Catch:{ all -> 0x01ff }
            org.telegram.messenger.MediaController$AlbumEntry r7 = (org.telegram.messenger.MediaController.AlbumEntry) r7     // Catch:{ all -> 0x01ff }
            if (r7 != 0) goto L_0x01e4
            org.telegram.messenger.MediaController$AlbumEntry r7 = new org.telegram.messenger.MediaController$AlbumEntry     // Catch:{ all -> 0x01ff }
            r7.<init>(r6, r8, r0)     // Catch:{ all -> 0x01ff }
            r5.put(r6, r7)     // Catch:{ all -> 0x01ff }
            if (r34 != 0) goto L_0x01e1
            if (r12 == 0) goto L_0x01e1
            if (r2 == 0) goto L_0x01e1
            boolean r2 = r2.startsWith(r12)     // Catch:{ all -> 0x01ff }
            if (r2 == 0) goto L_0x01e1
            r2 = 0
            r15.add(r2, r7)     // Catch:{ all -> 0x01ff }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x01ff }
            r34 = r2
            goto L_0x01e4
        L_0x01e1:
            r15.add(r7)     // Catch:{ all -> 0x01ff }
        L_0x01e4:
            r7.addPhoto(r0)     // Catch:{ all -> 0x01ff }
            r31 = r3
            r32 = r4
            r18 = r5
            r2 = r35
            r3 = r36
            r4 = r37
            r5 = r38
            r7 = r39
            r6 = r48
            r8 = r49
            r0 = r50
            goto L_0x00fc
        L_0x01ff:
            r0 = move-exception
            r31 = r3
            r32 = r4
            goto L_0x02ae
        L_0x0206:
            r0 = move-exception
            goto L_0x02ae
        L_0x0209:
            r35 = r2
            goto L_0x0278
        L_0x020d:
            r0 = move-exception
            r35 = r2
            goto L_0x02ae
        L_0x0212:
            r0 = move-exception
            r35 = r2
            goto L_0x0261
        L_0x0216:
            r0 = move-exception
            r35 = r2
            r30 = r3
            goto L_0x0261
        L_0x021c:
            r0 = move-exception
            r35 = r2
            r30 = r3
            r28 = r4
            goto L_0x0261
        L_0x0224:
            r0 = move-exception
            r35 = r2
            r30 = r3
            r28 = r4
            goto L_0x025f
        L_0x022c:
            r0 = move-exception
            r35 = r2
            r30 = r3
            r28 = r4
            r26 = r5
            goto L_0x025f
        L_0x0236:
            r0 = move-exception
            r35 = r2
            r30 = r3
            r28 = r4
            r26 = r5
            r25 = r6
            goto L_0x025f
        L_0x0242:
            r0 = move-exception
            r35 = r2
            r30 = r3
            r28 = r4
            r26 = r5
            r25 = r6
            r24 = r7
            goto L_0x025f
        L_0x0250:
            r0 = move-exception
            r35 = r2
            r30 = r3
            r28 = r4
            r26 = r5
            r25 = r6
            r24 = r7
            r23 = r8
        L_0x025f:
            r27 = r9
        L_0x0261:
            r31 = r17
            goto L_0x02aa
        L_0x0264:
            r35 = r2
            r30 = r3
            r28 = r4
            r26 = r5
            r25 = r6
            r24 = r7
            r23 = r8
            r27 = r9
            r31 = r17
            goto L_0x0083
        L_0x0278:
            if (r10 == 0) goto L_0x02bc
            r10.close()     // Catch:{ Exception -> 0x027e }
            goto L_0x02bc
        L_0x027e:
            r0 = move-exception
            r2 = r0
            goto L_0x02b9
        L_0x0281:
            r0 = move-exception
            r35 = r2
            r30 = r3
            r28 = r4
            r26 = r5
            r25 = r6
            r24 = r7
            r23 = r8
            r27 = r9
            goto L_0x02a6
        L_0x0293:
            r0 = move-exception
            r35 = r2
            r30 = r3
            r28 = r4
            r26 = r5
            r25 = r6
            r24 = r7
            r23 = r8
            r27 = r9
            r29 = r10
        L_0x02a6:
            r10 = r17
            r31 = r10
        L_0x02aa:
            r32 = r31
            r33 = r32
        L_0x02ae:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)     // Catch:{ all -> 0x0476 }
            if (r10 == 0) goto L_0x02bc
            r10.close()     // Catch:{ Exception -> 0x02b7 }
            goto L_0x02bc
        L_0x02b7:
            r0 = move-exception
            r2 = r0
        L_0x02b9:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r2)
        L_0x02bc:
            r18 = r31
            int r0 = android.os.Build.VERSION.SDK_INT     // Catch:{ all -> 0x0433 }
            r2 = 23
            if (r0 < r2) goto L_0x02d4
            if (r0 < r2) goto L_0x02d1
            android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ all -> 0x0433 }
            r3 = r29
            int r2 = r2.checkSelfPermission(r3)     // Catch:{ all -> 0x0433 }
            if (r2 != 0) goto L_0x02d1
            goto L_0x02d4
        L_0x02d1:
            r3 = 0
            goto L_0x042a
        L_0x02d4:
            android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ all -> 0x0433 }
            android.content.ContentResolver r3 = r2.getContentResolver()     // Catch:{ all -> 0x0433 }
            android.net.Uri r4 = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI     // Catch:{ all -> 0x0433 }
            java.lang.String[] r5 = projectionVideo     // Catch:{ all -> 0x0433 }
            r6 = 0
            r7 = 0
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0433 }
            r2.<init>()     // Catch:{ all -> 0x0433 }
            r8 = 28
            if (r0 <= r8) goto L_0x02ec
            r8 = r16
            goto L_0x02ee
        L_0x02ec:
            r8 = r19
        L_0x02ee:
            r2.append(r8)     // Catch:{ all -> 0x0433 }
            r8 = r27
            r2.append(r8)     // Catch:{ all -> 0x0433 }
            java.lang.String r8 = r2.toString()     // Catch:{ all -> 0x0433 }
            android.database.Cursor r10 = android.provider.MediaStore.Images.Media.query(r3, r4, r5, r6, r7, r8)     // Catch:{ all -> 0x0433 }
            if (r10 == 0) goto L_0x02d1
            r2 = r23
            int r2 = r10.getColumnIndex(r2)     // Catch:{ all -> 0x0433 }
            r3 = r24
            int r3 = r10.getColumnIndex(r3)     // Catch:{ all -> 0x0433 }
            r4 = r25
            int r4 = r10.getColumnIndex(r4)     // Catch:{ all -> 0x0433 }
            r5 = r26
            int r5 = r10.getColumnIndex(r5)     // Catch:{ all -> 0x0433 }
            r6 = 28
            if (r0 <= r6) goto L_0x031f
            r11 = r16
            goto L_0x0321
        L_0x031f:
            r11 = r19
        L_0x0321:
            int r0 = r10.getColumnIndex(r11)     // Catch:{ all -> 0x0433 }
            java.lang.String r6 = "duration"
            int r6 = r10.getColumnIndex(r6)     // Catch:{ all -> 0x0433 }
            r7 = r28
            int r7 = r10.getColumnIndex(r7)     // Catch:{ all -> 0x0433 }
            r8 = r30
            int r8 = r10.getColumnIndex(r8)     // Catch:{ all -> 0x0433 }
            r9 = r35
            int r9 = r10.getColumnIndex(r9)     // Catch:{ all -> 0x0433 }
        L_0x033d:
            boolean r11 = r10.moveToNext()     // Catch:{ all -> 0x0433 }
            if (r11 == 0) goto L_0x02d1
            java.lang.String r11 = r10.getString(r5)     // Catch:{ all -> 0x0433 }
            boolean r16 = android.text.TextUtils.isEmpty(r11)     // Catch:{ all -> 0x0433 }
            if (r16 == 0) goto L_0x034e
            goto L_0x033d
        L_0x034e:
            int r36 = r10.getInt(r2)     // Catch:{ all -> 0x0433 }
            r16 = r2
            int r2 = r10.getInt(r3)     // Catch:{ all -> 0x0433 }
            r19 = r3
            java.lang.String r3 = r10.getString(r4)     // Catch:{ all -> 0x0433 }
            long r37 = r10.getLong(r0)     // Catch:{ all -> 0x0433 }
            long r21 = r10.getLong(r6)     // Catch:{ all -> 0x0433 }
            int r42 = r10.getInt(r7)     // Catch:{ all -> 0x0433 }
            int r43 = r10.getInt(r8)     // Catch:{ all -> 0x0433 }
            long r44 = r10.getLong(r9)     // Catch:{ all -> 0x0433 }
            r23 = r0
            org.telegram.messenger.MediaController$PhotoEntry r0 = new org.telegram.messenger.MediaController$PhotoEntry     // Catch:{ all -> 0x0433 }
            r24 = 1000(0x3e8, double:4.94E-321)
            r26 = r4
            r27 = r5
            long r4 = r21 / r24
            int r5 = (int) r4     // Catch:{ all -> 0x0433 }
            r41 = 1
            r34 = r0
            r35 = r2
            r39 = r11
            r40 = r5
            r34.<init>(r35, r36, r37, r39, r40, r41, r42, r43, r44)     // Catch:{ all -> 0x0433 }
            if (r17 != 0) goto L_0x03ae
            org.telegram.messenger.MediaController$AlbumEntry r4 = new org.telegram.messenger.MediaController$AlbumEntry     // Catch:{ all -> 0x0433 }
            java.lang.String r5 = "AllVideos"
            r21 = r6
            r6 = 2131624239(0x7f0e012f, float:1.8875652E38)
            java.lang.String r5 = org.telegram.messenger.LocaleController.getString(r5, r6)     // Catch:{ all -> 0x0433 }
            r6 = 0
            r4.<init>(r6, r5, r0)     // Catch:{ all -> 0x0433 }
            r5 = 1
            r4.videoOnly = r5     // Catch:{ all -> 0x03ce }
            if (r32 == 0) goto L_0x03a5
            goto L_0x03a6
        L_0x03a5:
            r5 = 0
        L_0x03a6:
            if (r18 == 0) goto L_0x03aa
            int r5 = r5 + 1
        L_0x03aa:
            r14.add(r5, r4)     // Catch:{ all -> 0x03ce }
            goto L_0x03b2
        L_0x03ae:
            r21 = r6
            r4 = r17
        L_0x03b2:
            if (r32 != 0) goto L_0x03d3
            org.telegram.messenger.MediaController$AlbumEntry r5 = new org.telegram.messenger.MediaController$AlbumEntry     // Catch:{ all -> 0x03ce }
            r20 = r7
            r6 = 2131624237(0x7f0e012d, float:1.8875648E38)
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r1, r6)     // Catch:{ all -> 0x03ce }
            r6 = 0
            r5.<init>(r6, r7, r0)     // Catch:{ all -> 0x03ce }
            r14.add(r6, r5)     // Catch:{ all -> 0x03c7 }
            goto L_0x03d7
        L_0x03c7:
            r0 = move-exception
            r17 = r4
            r32 = r5
            goto L_0x0434
        L_0x03ce:
            r0 = move-exception
            r17 = r4
            goto L_0x0434
        L_0x03d3:
            r20 = r7
            r5 = r32
        L_0x03d7:
            r4.addPhoto(r0)     // Catch:{ all -> 0x0423 }
            r5.addPhoto(r0)     // Catch:{ all -> 0x0423 }
            java.lang.Object r6 = r13.get(r2)     // Catch:{ all -> 0x0423 }
            org.telegram.messenger.MediaController$AlbumEntry r6 = (org.telegram.messenger.MediaController.AlbumEntry) r6     // Catch:{ all -> 0x0423 }
            if (r6 != 0) goto L_0x0409
            org.telegram.messenger.MediaController$AlbumEntry r6 = new org.telegram.messenger.MediaController$AlbumEntry     // Catch:{ all -> 0x0423 }
            r6.<init>(r2, r3, r0)     // Catch:{ all -> 0x0423 }
            r13.put(r2, r6)     // Catch:{ all -> 0x0423 }
            if (r33 != 0) goto L_0x0404
            if (r12 == 0) goto L_0x0404
            if (r11 == 0) goto L_0x0404
            boolean r3 = r11.startsWith(r12)     // Catch:{ all -> 0x0423 }
            if (r3 == 0) goto L_0x0404
            r3 = 0
            r14.add(r3, r6)     // Catch:{ all -> 0x0421 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x0421 }
            r33 = r2
            goto L_0x040a
        L_0x0404:
            r3 = 0
            r14.add(r6)     // Catch:{ all -> 0x0421 }
            goto L_0x040a
        L_0x0409:
            r3 = 0
        L_0x040a:
            r6.addPhoto(r0)     // Catch:{ all -> 0x0421 }
            r17 = r4
            r32 = r5
            r2 = r16
            r3 = r19
            r7 = r20
            r6 = r21
            r0 = r23
            r4 = r26
            r5 = r27
            goto L_0x033d
        L_0x0421:
            r0 = move-exception
            goto L_0x0425
        L_0x0423:
            r0 = move-exception
            r3 = 0
        L_0x0425:
            r17 = r4
            r32 = r5
            goto L_0x0435
        L_0x042a:
            if (r10 == 0) goto L_0x0443
            r10.close()     // Catch:{ Exception -> 0x0430 }
            goto L_0x0443
        L_0x0430:
            r0 = move-exception
            r1 = r0
            goto L_0x0440
        L_0x0433:
            r0 = move-exception
        L_0x0434:
            r3 = 0
        L_0x0435:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)     // Catch:{ all -> 0x0468 }
            if (r10 == 0) goto L_0x0443
            r10.close()     // Catch:{ Exception -> 0x043e }
            goto L_0x0443
        L_0x043e:
            r0 = move-exception
            r1 = r0
        L_0x0440:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r1)
        L_0x0443:
            r19 = r17
            r17 = r32
            r16 = r33
            r11 = 0
        L_0x044a:
            int r0 = r14.size()
            if (r11 >= r0) goto L_0x0460
            java.lang.Object r0 = r14.get(r11)
            org.telegram.messenger.MediaController$AlbumEntry r0 = (org.telegram.messenger.MediaController.AlbumEntry) r0
            java.util.ArrayList<org.telegram.messenger.MediaController$PhotoEntry> r0 = r0.photos
            org.telegram.messenger.-$$Lambda$MediaController$F6SXaphXXYjgd_pcwAjMPqQu_nI r1 = org.telegram.messenger.$$Lambda$MediaController$F6SXaphXXYjgd_pcwAjMPqQu_nI.INSTANCE
            java.util.Collections.sort(r0, r1)
            int r11 = r11 + 1
            goto L_0x044a
        L_0x0460:
            r20 = 0
            r13 = r51
            broadcastNewPhotos(r13, r14, r15, r16, r17, r18, r19, r20)
            return
        L_0x0468:
            r0 = move-exception
            r1 = r0
            if (r10 == 0) goto L_0x0475
            r10.close()     // Catch:{ Exception -> 0x0470 }
            goto L_0x0475
        L_0x0470:
            r0 = move-exception
            r2 = r0
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r2)
        L_0x0475:
            throw r1
        L_0x0476:
            r0 = move-exception
            r1 = r0
            if (r10 == 0) goto L_0x0483
            r10.close()     // Catch:{ Exception -> 0x047e }
            goto L_0x0483
        L_0x047e:
            r0 = move-exception
            r2 = r0
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r2)
        L_0x0483:
            goto L_0x0485
        L_0x0484:
            throw r1
        L_0x0485:
            goto L_0x0484
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.lambda$loadGalleryPhotosAlbums$40(int):void");
    }

    static /* synthetic */ int lambda$null$39(PhotoEntry photoEntry, PhotoEntry photoEntry2) {
        long j = photoEntry.dateTaken;
        long j2 = photoEntry2.dateTaken;
        if (j < j2) {
            return 1;
        }
        return j > j2 ? -1 : 0;
    }

    private static void broadcastNewPhotos(int i, ArrayList<AlbumEntry> arrayList, ArrayList<AlbumEntry> arrayList2, Integer num, AlbumEntry albumEntry, AlbumEntry albumEntry2, AlbumEntry albumEntry3, int i2) {
        Runnable runnable = broadcastPhotosRunnable;
        if (runnable != null) {
            AndroidUtilities.cancelRunOnUIThread(runnable);
        }
        $$Lambda$MediaController$0nqVsYZMhoaXGDnyopApSVoRSbY r1 = new Runnable(i, arrayList, arrayList2, num, albumEntry, albumEntry2, albumEntry3) {
            public final /* synthetic */ int f$0;
            public final /* synthetic */ ArrayList f$1;
            public final /* synthetic */ ArrayList f$2;
            public final /* synthetic */ Integer f$3;
            public final /* synthetic */ MediaController.AlbumEntry f$4;
            public final /* synthetic */ MediaController.AlbumEntry f$5;
            public final /* synthetic */ MediaController.AlbumEntry f$6;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
            }

            public final void run() {
                MediaController.lambda$broadcastNewPhotos$41(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
            }
        };
        broadcastPhotosRunnable = r1;
        AndroidUtilities.runOnUIThread(r1, (long) i2);
    }

    static /* synthetic */ void lambda$broadcastNewPhotos$41(int i, ArrayList arrayList, ArrayList arrayList2, Integer num, AlbumEntry albumEntry, AlbumEntry albumEntry2, AlbumEntry albumEntry3) {
        if (PhotoViewer.getInstance().isVisible()) {
            broadcastNewPhotos(i, arrayList, arrayList2, num, albumEntry, albumEntry2, albumEntry3, 1000);
            return;
        }
        allMediaAlbums = arrayList;
        allPhotoAlbums = arrayList2;
        broadcastPhotosRunnable = null;
        allPhotosAlbumEntry = albumEntry2;
        allMediaAlbumEntry = albumEntry;
        allVideosAlbumEntry = albumEntry3;
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.albumsDidLoad, Integer.valueOf(i), arrayList, arrayList2, num);
    }

    public void scheduleVideoConvert(MessageObject messageObject) {
        scheduleVideoConvert(messageObject, false);
    }

    public boolean scheduleVideoConvert(MessageObject messageObject, boolean z) {
        if (messageObject == null || messageObject.videoEditedInfo == null) {
            return false;
        }
        if (z && !this.videoConvertQueue.isEmpty()) {
            return false;
        }
        if (z) {
            new File(messageObject.messageOwner.attachPath).delete();
        }
        this.videoConvertQueue.add(new VideoConvertMessage(messageObject, messageObject.videoEditedInfo));
        if (this.videoConvertQueue.size() == 1) {
            startVideoConvertFromQueue();
        }
        return true;
    }

    public void cancelVideoConvert(MessageObject messageObject) {
        if (messageObject != null && !this.videoConvertQueue.isEmpty()) {
            int i = 0;
            while (i < this.videoConvertQueue.size()) {
                VideoConvertMessage videoConvertMessage = this.videoConvertQueue.get(i);
                MessageObject messageObject2 = videoConvertMessage.messageObject;
                if (!messageObject2.equals(messageObject) || messageObject2.currentAccount != messageObject.currentAccount) {
                    i++;
                } else if (i == 0) {
                    synchronized (this.videoConvertSync) {
                        videoConvertMessage.videoEditedInfo.canceled = true;
                    }
                    return;
                } else {
                    this.videoConvertQueue.remove(i);
                    return;
                }
            }
        }
    }

    private boolean startVideoConvertFromQueue() {
        int i = 0;
        if (this.videoConvertQueue.isEmpty()) {
            return false;
        }
        VideoConvertMessage videoConvertMessage = this.videoConvertQueue.get(0);
        MessageObject messageObject = videoConvertMessage.messageObject;
        VideoEditedInfo videoEditedInfo = videoConvertMessage.videoEditedInfo;
        synchronized (this.videoConvertSync) {
            if (videoEditedInfo != null) {
                videoEditedInfo.canceled = false;
            }
        }
        Intent intent = new Intent(ApplicationLoader.applicationContext, VideoEncodingService.class);
        intent.putExtra("path", messageObject.messageOwner.attachPath);
        intent.putExtra("currentAccount", messageObject.currentAccount);
        if (messageObject.messageOwner.media.document != null) {
            while (true) {
                if (i >= messageObject.messageOwner.media.document.attributes.size()) {
                    break;
                } else if (messageObject.messageOwner.media.document.attributes.get(i) instanceof TLRPC$TL_documentAttributeAnimated) {
                    intent.putExtra("gif", true);
                    break;
                } else {
                    i++;
                }
            }
        }
        if (messageObject.getId() != 0) {
            try {
                ApplicationLoader.applicationContext.startService(intent);
            } catch (Throwable th) {
                FileLog.e(th);
            }
        }
        VideoConvertRunnable.runConversion(videoConvertMessage);
        return true;
    }

    @SuppressLint({"NewApi"})
    public static MediaCodecInfo selectCodec(String str) {
        int codecCount = MediaCodecList.getCodecCount();
        MediaCodecInfo mediaCodecInfo = null;
        for (int i = 0; i < codecCount; i++) {
            MediaCodecInfo codecInfoAt = MediaCodecList.getCodecInfoAt(i);
            if (codecInfoAt.isEncoder()) {
                for (String equalsIgnoreCase : codecInfoAt.getSupportedTypes()) {
                    if (equalsIgnoreCase.equalsIgnoreCase(str)) {
                        String name = codecInfoAt.getName();
                        if (name != null && (!name.equals("OMX.SEC.avc.enc") || name.equals("OMX.SEC.AVC.Encoder"))) {
                            return codecInfoAt;
                        }
                        mediaCodecInfo = codecInfoAt;
                    }
                }
                continue;
            }
        }
        return mediaCodecInfo;
    }

    @SuppressLint({"NewApi"})
    public static int selectColorFormat(MediaCodecInfo mediaCodecInfo, String str) {
        int i;
        MediaCodecInfo.CodecCapabilities capabilitiesForType = mediaCodecInfo.getCapabilitiesForType(str);
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int[] iArr = capabilitiesForType.colorFormats;
            if (i2 >= iArr.length) {
                return i3;
            }
            i = iArr[i2];
            if (isRecognizedFormat(i)) {
                if (!mediaCodecInfo.getName().equals("OMX.SEC.AVC.Encoder") || i != 19) {
                    return i;
                }
                i3 = i;
            }
            i2++;
        }
        return i;
    }

    public static int findTrack(MediaExtractor mediaExtractor, boolean z) {
        int trackCount = mediaExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            String string = mediaExtractor.getTrackFormat(i).getString("mime");
            if (z) {
                if (string.startsWith("audio/")) {
                    return i;
                }
            } else if (string.startsWith("video/")) {
                return i;
            }
        }
        return -5;
    }

    /* access modifiers changed from: private */
    public void didWriteData(VideoConvertMessage videoConvertMessage, File file, boolean z, long j, long j2, boolean z2, float f) {
        VideoEditedInfo videoEditedInfo = videoConvertMessage.videoEditedInfo;
        boolean z3 = videoEditedInfo.videoConvertFirstWrite;
        if (z3) {
            videoEditedInfo.videoConvertFirstWrite = false;
        }
        AndroidUtilities.runOnUIThread(new Runnable(z2, z, videoConvertMessage, file, f, j, z3, j2) {
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ MediaController.VideoConvertMessage f$3;
            public final /* synthetic */ File f$4;
            public final /* synthetic */ float f$5;
            public final /* synthetic */ long f$6;
            public final /* synthetic */ boolean f$7;
            public final /* synthetic */ long f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r9;
                this.f$8 = r10;
            }

            public final void run() {
                MediaController.this.lambda$didWriteData$42$MediaController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$didWriteData$42 */
    public /* synthetic */ void lambda$didWriteData$42$MediaController(boolean z, boolean z2, VideoConvertMessage videoConvertMessage, File file, float f, long j, boolean z3, long j2) {
        VideoConvertMessage videoConvertMessage2 = videoConvertMessage;
        if (z || z2) {
            synchronized (this.videoConvertSync) {
                videoConvertMessage2.videoEditedInfo.canceled = false;
            }
            this.videoConvertQueue.remove(videoConvertMessage);
            startVideoConvertFromQueue();
        }
        if (z) {
            NotificationCenter.getInstance(videoConvertMessage2.currentAccount).postNotificationName(NotificationCenter.filePreparingFailed, videoConvertMessage2.messageObject, file.toString(), Float.valueOf(f), Long.valueOf(j));
            return;
        }
        if (z3) {
            NotificationCenter.getInstance(videoConvertMessage2.currentAccount).postNotificationName(NotificationCenter.filePreparingStarted, videoConvertMessage2.messageObject, file.toString(), Float.valueOf(f), Long.valueOf(j));
        }
        NotificationCenter instance = NotificationCenter.getInstance(videoConvertMessage2.currentAccount);
        int i = NotificationCenter.fileNewChunkAvailable;
        Object[] objArr = new Object[6];
        objArr[0] = videoConvertMessage2.messageObject;
        objArr[1] = file.toString();
        objArr[2] = Long.valueOf(j2);
        objArr[3] = Long.valueOf(z2 ? file.length() : 0);
        objArr[4] = Float.valueOf(f);
        objArr[5] = Long.valueOf(j);
        instance.postNotificationName(i, objArr);
    }

    public void pauseByRewind() {
        VideoPlayer videoPlayer2 = this.audioPlayer;
        if (videoPlayer2 != null) {
            videoPlayer2.pause();
        }
    }

    public void resumeByRewind() {
        VideoPlayer videoPlayer2 = this.audioPlayer;
        if (videoPlayer2 != null && this.playingMessageObject != null && !this.isPaused) {
            if (videoPlayer2.isBuffering()) {
                MessageObject messageObject = this.playingMessageObject;
                cleanupPlayer(false, false);
                playMessage(messageObject);
                return;
            }
            this.audioPlayer.play();
        }
    }

    private static class VideoConvertRunnable implements Runnable {
        private VideoConvertMessage convertMessage;

        private VideoConvertRunnable(VideoConvertMessage videoConvertMessage) {
            this.convertMessage = videoConvertMessage;
        }

        public void run() {
            boolean unused = MediaController.getInstance().convertVideo(this.convertMessage);
        }

        public static void runConversion(VideoConvertMessage videoConvertMessage) {
            new Thread(new Runnable() {
                public final void run() {
                    MediaController.VideoConvertRunnable.lambda$runConversion$0(MediaController.VideoConvertMessage.this);
                }
            }).start();
        }

        static /* synthetic */ void lambda$runConversion$0(VideoConvertMessage videoConvertMessage) {
            try {
                Thread thread = new Thread(new VideoConvertRunnable(videoConvertMessage), "VideoConvertRunnable");
                thread.start();
                thread.join();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean convertVideo(VideoConvertMessage videoConvertMessage) {
        boolean z;
        int i;
        int i2;
        long j;
        int i3;
        final VideoEditedInfo videoEditedInfo;
        int i4;
        int i5;
        long j2;
        VideoConvertMessage videoConvertMessage2 = videoConvertMessage;
        MessageObject messageObject = videoConvertMessage2.messageObject;
        VideoEditedInfo videoEditedInfo2 = videoConvertMessage2.videoEditedInfo;
        if (messageObject == null || videoEditedInfo2 == null) {
            return false;
        }
        String str = videoEditedInfo2.originalPath;
        long j3 = videoEditedInfo2.startTime;
        long j4 = videoEditedInfo2.avatarStartTime;
        long j5 = videoEditedInfo2.endTime;
        int i6 = videoEditedInfo2.resultWidth;
        int i7 = videoEditedInfo2.resultHeight;
        int i8 = videoEditedInfo2.rotationValue;
        int i9 = videoEditedInfo2.originalWidth;
        int i10 = videoEditedInfo2.originalHeight;
        int i11 = videoEditedInfo2.framerate;
        int i12 = videoEditedInfo2.bitrate;
        int i13 = videoEditedInfo2.originalBitrate;
        VideoEditedInfo videoEditedInfo3 = videoEditedInfo2;
        int i14 = i10;
        boolean z2 = ((int) messageObject.getDialogId()) == 0;
        final File file = new File(messageObject.messageOwner.attachPath);
        if (file.exists()) {
            file.delete();
        }
        if (BuildVars.LOGS_ENABLED) {
            StringBuilder sb = new StringBuilder();
            z = z2;
            sb.append("begin convert ");
            sb.append(str);
            sb.append(" startTime = ");
            sb.append(j3);
            sb.append(" avatarStartTime = ");
            sb.append(j4);
            sb.append(" endTime ");
            sb.append(j5);
            sb.append(" rWidth = ");
            sb.append(i6);
            sb.append(" rHeight = ");
            sb.append(i7);
            sb.append(" rotation = ");
            sb.append(i8);
            sb.append(" oWidth = ");
            sb.append(i9);
            sb.append(" oHeight = ");
            i2 = i14;
            sb.append(i2);
            i = i6;
            sb.append(" framerate = ");
            sb.append(i11);
            sb.append(" bitrate = ");
            sb.append(i12);
            sb.append(" originalBitrate = ");
            sb.append(i13);
            FileLog.d(sb.toString());
        } else {
            z = z2;
            i2 = i14;
            i = i6;
        }
        if (str == null) {
            str = "";
        }
        if (j3 > 0 && j5 > 0) {
            j = j5 - j3;
            videoEditedInfo = videoEditedInfo3;
            i3 = i7;
        } else if (j5 > 0) {
            j = j5;
            i3 = i7;
            videoEditedInfo = videoEditedInfo3;
        } else {
            if (j3 > 0) {
                i3 = i7;
                videoEditedInfo = videoEditedInfo3;
                j2 = videoEditedInfo.originalDuration - j3;
            } else {
                i3 = i7;
                videoEditedInfo = videoEditedInfo3;
                j2 = videoEditedInfo.originalDuration;
            }
            j = j2;
        }
        if (i11 == 0) {
            i11 = 25;
        }
        if (i8 == 90 || i8 == 270) {
            i5 = i;
            i4 = i3;
        } else {
            i4 = i;
            i5 = i3;
        }
        long j6 = j4;
        boolean z3 = (j4 == -1 && videoEditedInfo.cropState == null && videoEditedInfo.mediaEntities == null && videoEditedInfo.paintPath == null && videoEditedInfo.filterState == null && i4 == i9 && i5 == i2 && i8 == 0 && !videoEditedInfo.roundVideo && j3 == -1) ? false : true;
        AnonymousClass13 r36 = r14;
        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("videoconvert", 0);
        long currentTimeMillis = System.currentTimeMillis();
        final VideoConvertMessage videoConvertMessage3 = videoConvertMessage;
        AnonymousClass13 r14 = new VideoConvertorListener() {
            private long lastAvailableSize = 0;

            public boolean checkConversionCanceled() {
                return videoEditedInfo.canceled;
            }

            public void didWriteData(long j, float f) {
                if (!videoEditedInfo.canceled) {
                    if (j < 0) {
                        j = file.length();
                    }
                    long j2 = j;
                    if (videoEditedInfo.needUpdateProgress || this.lastAvailableSize != j2) {
                        this.lastAvailableSize = j2;
                        MediaController.this.didWriteData(videoConvertMessage3, file, false, 0, j2, false, f);
                    }
                }
            }
        };
        videoEditedInfo.videoConvertFirstWrite = true;
        int i15 = i5;
        MediaCodecVideoConvertor mediaCodecVideoConvertor = r41;
        MediaCodecVideoConvertor mediaCodecVideoConvertor2 = new MediaCodecVideoConvertor();
        boolean convertVideo = mediaCodecVideoConvertor.convertVideo(str, file, i8, z, i4, i15, i11, i12, i13, j3, j5, j6, z3, j, videoEditedInfo.filterState, videoEditedInfo.paintPath, videoEditedInfo.mediaEntities, videoEditedInfo.isPhoto, videoEditedInfo.cropState, r36);
        boolean z4 = videoEditedInfo.canceled;
        if (!z4) {
            synchronized (this.videoConvertSync) {
                z4 = videoEditedInfo.canceled;
            }
        }
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("time=" + (System.currentTimeMillis() - currentTimeMillis) + " canceled=" + z4);
        }
        sharedPreferences.edit().putBoolean("isPreviousOk", true).apply();
        didWriteData(videoConvertMessage, file, true, mediaCodecVideoConvertor2.getLastFrameTimestamp(), file.length(), convertVideo || z4, 1.0f);
        return true;
    }

    public static int getVideoBitrate(String str) {
        int i;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(str);
            i = Integer.parseInt(mediaMetadataRetriever.extractMetadata(20));
        } catch (Exception e) {
            FileLog.e((Throwable) e);
            i = 0;
        }
        mediaMetadataRetriever.release();
        return i;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0056 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0057  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int makeVideoBitrate(int r5, int r6, int r7, int r8, int r9) {
        /*
            int r0 = java.lang.Math.min(r8, r9)
            r1 = 1065353216(0x3var_, float:1.0)
            r2 = 1080(0x438, float:1.513E-42)
            if (r0 < r2) goto L_0x0010
            r0 = 6800000(0x67CLASSNAME, float:9.52883E-39)
        L_0x000d:
            r2 = 1065353216(0x3var_, float:1.0)
            goto L_0x0036
        L_0x0010:
            int r0 = java.lang.Math.min(r8, r9)
            r2 = 720(0x2d0, float:1.009E-42)
            if (r0 < r2) goto L_0x001b
            r0 = 2621440(0x280000, float:3.67342E-39)
            goto L_0x000d
        L_0x001b:
            int r0 = java.lang.Math.min(r8, r9)
            r1 = 480(0x1e0, float:6.73E-43)
            if (r0 < r1) goto L_0x002d
            r0 = 1000000(0xvar_, float:1.401298E-39)
            r1 = 1061997773(0x3f4ccccd, float:0.8)
            r2 = 1063675494(0x3var_, float:0.9)
            goto L_0x0036
        L_0x002d:
            r0 = 750000(0xb71b0, float:1.050974E-39)
            r1 = 1058642330(0x3var_a, float:0.6)
            r2 = 1060320051(0x3var_, float:0.7)
        L_0x0036:
            float r3 = (float) r7
            float r5 = (float) r5
            float r4 = (float) r8
            float r5 = r5 / r4
            float r6 = (float) r6
            float r4 = (float) r9
            float r6 = r6 / r4
            float r5 = java.lang.Math.min(r5, r6)
            float r3 = r3 / r5
            int r5 = (int) r3
            float r5 = (float) r5
            float r5 = r5 * r1
            int r5 = (int) r5
            int r6 = getVideoBitrateWithFactor(r2)
            float r6 = (float) r6
            r1 = 1231093760(0x49610000, float:921600.0)
            int r9 = r9 * r8
            float r8 = (float) r9
            float r1 = r1 / r8
            float r6 = r6 / r1
            int r6 = (int) r6
            if (r7 >= r6) goto L_0x0057
            return r5
        L_0x0057:
            if (r5 <= r0) goto L_0x005a
            return r0
        L_0x005a:
            int r5 = java.lang.Math.max(r5, r6)
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.makeVideoBitrate(int, int, int, int, int):int");
    }

    public static class PlaylistGlobalSearchParams {
        final int dialogId;
        public boolean endReached;
        final FiltersView.MediaFilterData filter;
        public int folderId;
        final long maxDate;
        final long minDate;
        public int nextSearchRate;
        final String query;
        public int totalCount;

        public PlaylistGlobalSearchParams(String str, int i, long j, long j2, FiltersView.MediaFilterData mediaFilterData) {
            this.filter = mediaFilterData;
            this.query = str;
            this.dialogId = i;
            this.minDate = j;
            this.maxDate = j2;
        }
    }

    public boolean currentPlaylistIsGlobalSearch() {
        return this.playlistGlobalSearchParams != null;
    }
}
