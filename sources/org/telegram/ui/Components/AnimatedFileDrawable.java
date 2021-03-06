package org.telegram.ui.Components;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimatedFileDrawableStream;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.tgnet.TLRPC$Document;

public class AnimatedFileDrawable extends BitmapDrawable implements Animatable {
    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2, new ThreadPoolExecutor.DiscardPolicy());
    private static float[] radii = new float[8];
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());
    private RectF actualDrawRect;
    private boolean applyTransformation;
    /* access modifiers changed from: private */
    public Bitmap backgroundBitmap;
    /* access modifiers changed from: private */
    public int backgroundBitmapTime;
    /* access modifiers changed from: private */
    public BitmapShader backgroundShader;
    /* access modifiers changed from: private */
    public int currentAccount;
    /* access modifiers changed from: private */
    public DispatchQueue decodeQueue;
    private boolean decodeSingleFrame;
    /* access modifiers changed from: private */
    public boolean decoderCreated;
    /* access modifiers changed from: private */
    public boolean destroyWhenDone;
    private final Rect dstRect;
    /* access modifiers changed from: private */
    public float endTime;
    /* access modifiers changed from: private */
    public boolean forceDecodeAfterNextFrame;
    /* access modifiers changed from: private */
    public int invalidateAfter;
    /* access modifiers changed from: private */
    public boolean invalidateParentViewWithSecond;
    private boolean invalidatePath;
    /* access modifiers changed from: private */
    public volatile boolean isRecycled;
    private volatile boolean isRunning;
    /* access modifiers changed from: private */
    public long lastFrameDecodeTime;
    private long lastFrameTime;
    /* access modifiers changed from: private */
    public int lastTimeStamp;
    private Runnable loadFrameRunnable;
    /* access modifiers changed from: private */
    public Runnable loadFrameTask;
    protected final Runnable mInvalidateTask;
    private final Runnable mStartTask;
    /* access modifiers changed from: private */
    public final int[] metaData;
    public volatile long nativePtr;
    /* access modifiers changed from: private */
    public Bitmap nextRenderingBitmap;
    /* access modifiers changed from: private */
    public int nextRenderingBitmapTime;
    /* access modifiers changed from: private */
    public BitmapShader nextRenderingShader;
    /* access modifiers changed from: private */
    public View parentView;
    /* access modifiers changed from: private */
    public File path;
    /* access modifiers changed from: private */
    public boolean pendingRemoveLoading;
    /* access modifiers changed from: private */
    public int pendingRemoveLoadingFramesReset;
    /* access modifiers changed from: private */
    public volatile long pendingSeekTo;
    /* access modifiers changed from: private */
    public volatile long pendingSeekToUI;
    private boolean recycleWithSecond;
    /* access modifiers changed from: private */
    public Bitmap renderingBitmap;
    private int renderingBitmapTime;
    /* access modifiers changed from: private */
    public int renderingHeight;
    private BitmapShader renderingShader;
    /* access modifiers changed from: private */
    public int renderingWidth;
    private Path roundPath;
    private int[] roundRadius;
    private int[] roundRadiusBackup;
    /* access modifiers changed from: private */
    public float scaleFactor;
    private float scaleX;
    private float scaleY;
    /* access modifiers changed from: private */
    public ArrayList<View> secondParentViews;
    private Matrix shaderMatrix;
    /* access modifiers changed from: private */
    public boolean singleFrameDecoded;
    /* access modifiers changed from: private */
    public float startTime;
    /* access modifiers changed from: private */
    public AnimatedFileDrawableStream stream;
    /* access modifiers changed from: private */
    public long streamFileSize;
    /* access modifiers changed from: private */
    public final Object sync;
    /* access modifiers changed from: private */
    public Runnable uiRunnable;
    /* access modifiers changed from: private */
    public Runnable uiRunnableNoFrame;
    private boolean useSharedQueue;

    /* access modifiers changed from: private */
    public static native long createDecoder(String str, int[] iArr, int i, long j, Object obj, boolean z);

    /* access modifiers changed from: private */
    public static native void destroyDecoder(long j);

    private static native int getFrameAtTime(long j, long j2, Bitmap bitmap, int[] iArr, int i);

    /* access modifiers changed from: private */
    public static native int getVideoFrame(long j, Bitmap bitmap, int[] iArr, int i, boolean z, float f, float f2);

    private static native void getVideoInfo(int i, String str, int[] iArr);

    private static native void prepareToSeek(long j);

    /* access modifiers changed from: private */
    public static native void seekToMs(long j, long j2, boolean z);

    private static native void stopDecoder(long j);

    public int getOpacity() {
        return -2;
    }

    static /* synthetic */ int access$1010(AnimatedFileDrawable animatedFileDrawable) {
        int i = animatedFileDrawable.pendingRemoveLoadingFramesReset;
        animatedFileDrawable.pendingRemoveLoadingFramesReset = i - 1;
        return i;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$AnimatedFileDrawable() {
        View view;
        if (!this.secondParentViews.isEmpty()) {
            int size = this.secondParentViews.size();
            for (int i = 0; i < size; i++) {
                this.secondParentViews.get(i).invalidate();
            }
        }
        if ((this.secondParentViews.isEmpty() || this.invalidateParentViewWithSecond) && (view = this.parentView) != null) {
            view.invalidate();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$AnimatedFileDrawable() {
        View view;
        if (!this.secondParentViews.isEmpty()) {
            int size = this.secondParentViews.size();
            for (int i = 0; i < size; i++) {
                this.secondParentViews.get(i).invalidate();
            }
        }
        if ((this.secondParentViews.isEmpty() || this.invalidateParentViewWithSecond) && (view = this.parentView) != null) {
            view.invalidate();
        }
    }

    public AnimatedFileDrawable(File file, boolean z, long j, TLRPC$Document tLRPC$Document, ImageLocation imageLocation, Object obj, long j2, int i, boolean z2) {
        this(file, z, j, tLRPC$Document, imageLocation, obj, j2, i, z2, 0, 0);
    }

    public AnimatedFileDrawable(File file, boolean z, long j, TLRPC$Document tLRPC$Document, ImageLocation imageLocation, Object obj, long j2, int i, boolean z2, int i2, int i3) {
        long j3;
        int i4;
        long j4 = j;
        long j5 = j2;
        this.invalidateAfter = 50;
        int[] iArr = new int[5];
        this.metaData = iArr;
        this.pendingSeekTo = -1;
        this.pendingSeekToUI = -1;
        this.sync = new Object();
        this.actualDrawRect = new RectF();
        this.roundRadius = new int[4];
        this.shaderMatrix = new Matrix();
        this.roundPath = new Path();
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.dstRect = new Rect();
        this.scaleFactor = 1.0f;
        this.secondParentViews = new ArrayList<>();
        this.invalidatePath = true;
        this.mInvalidateTask = new Runnable() {
            public final void run() {
                AnimatedFileDrawable.this.lambda$new$0$AnimatedFileDrawable();
            }
        };
        this.uiRunnableNoFrame = new Runnable() {
            public void run() {
                if (AnimatedFileDrawable.this.destroyWhenDone && AnimatedFileDrawable.this.nativePtr != 0) {
                    AnimatedFileDrawable.destroyDecoder(AnimatedFileDrawable.this.nativePtr);
                    AnimatedFileDrawable.this.nativePtr = 0;
                }
                if (AnimatedFileDrawable.this.nativePtr == 0) {
                    if (AnimatedFileDrawable.this.renderingBitmap != null) {
                        AnimatedFileDrawable.this.renderingBitmap.recycle();
                        Bitmap unused = AnimatedFileDrawable.this.renderingBitmap = null;
                    }
                    if (AnimatedFileDrawable.this.backgroundBitmap != null) {
                        AnimatedFileDrawable.this.backgroundBitmap.recycle();
                        Bitmap unused2 = AnimatedFileDrawable.this.backgroundBitmap = null;
                    }
                    if (AnimatedFileDrawable.this.decodeQueue != null) {
                        AnimatedFileDrawable.this.decodeQueue.recycle();
                        DispatchQueue unused3 = AnimatedFileDrawable.this.decodeQueue = null;
                        return;
                    }
                    return;
                }
                Runnable unused4 = AnimatedFileDrawable.this.loadFrameTask = null;
                AnimatedFileDrawable.this.scheduleNextGetFrame();
            }
        };
        this.uiRunnable = new Runnable() {
            public void run() {
                if (AnimatedFileDrawable.this.destroyWhenDone && AnimatedFileDrawable.this.nativePtr != 0) {
                    AnimatedFileDrawable.destroyDecoder(AnimatedFileDrawable.this.nativePtr);
                    AnimatedFileDrawable.this.nativePtr = 0;
                }
                if (AnimatedFileDrawable.this.nativePtr == 0) {
                    if (AnimatedFileDrawable.this.renderingBitmap != null) {
                        AnimatedFileDrawable.this.renderingBitmap.recycle();
                        Bitmap unused = AnimatedFileDrawable.this.renderingBitmap = null;
                    }
                    if (AnimatedFileDrawable.this.backgroundBitmap != null) {
                        AnimatedFileDrawable.this.backgroundBitmap.recycle();
                        Bitmap unused2 = AnimatedFileDrawable.this.backgroundBitmap = null;
                    }
                    if (AnimatedFileDrawable.this.decodeQueue != null) {
                        AnimatedFileDrawable.this.decodeQueue.recycle();
                        DispatchQueue unused3 = AnimatedFileDrawable.this.decodeQueue = null;
                        return;
                    }
                    return;
                }
                if (AnimatedFileDrawable.this.stream != null && AnimatedFileDrawable.this.pendingRemoveLoading) {
                    FileLoader.getInstance(AnimatedFileDrawable.this.currentAccount).removeLoadingVideo(AnimatedFileDrawable.this.stream.getDocument(), false, false);
                }
                if (AnimatedFileDrawable.this.pendingRemoveLoadingFramesReset <= 0) {
                    boolean unused4 = AnimatedFileDrawable.this.pendingRemoveLoading = true;
                } else {
                    AnimatedFileDrawable.access$1010(AnimatedFileDrawable.this);
                }
                if (!AnimatedFileDrawable.this.forceDecodeAfterNextFrame) {
                    boolean unused5 = AnimatedFileDrawable.this.singleFrameDecoded = true;
                } else {
                    boolean unused6 = AnimatedFileDrawable.this.forceDecodeAfterNextFrame = false;
                }
                Runnable unused7 = AnimatedFileDrawable.this.loadFrameTask = null;
                AnimatedFileDrawable animatedFileDrawable = AnimatedFileDrawable.this;
                Bitmap unused8 = animatedFileDrawable.nextRenderingBitmap = animatedFileDrawable.backgroundBitmap;
                AnimatedFileDrawable animatedFileDrawable2 = AnimatedFileDrawable.this;
                int unused9 = animatedFileDrawable2.nextRenderingBitmapTime = animatedFileDrawable2.backgroundBitmapTime;
                AnimatedFileDrawable animatedFileDrawable3 = AnimatedFileDrawable.this;
                BitmapShader unused10 = animatedFileDrawable3.nextRenderingShader = animatedFileDrawable3.backgroundShader;
                if (AnimatedFileDrawable.this.metaData[3] < AnimatedFileDrawable.this.lastTimeStamp) {
                    AnimatedFileDrawable animatedFileDrawable4 = AnimatedFileDrawable.this;
                    int unused11 = animatedFileDrawable4.lastTimeStamp = animatedFileDrawable4.startTime > 0.0f ? (int) (AnimatedFileDrawable.this.startTime * 1000.0f) : 0;
                }
                if (AnimatedFileDrawable.this.metaData[3] - AnimatedFileDrawable.this.lastTimeStamp != 0) {
                    AnimatedFileDrawable animatedFileDrawable5 = AnimatedFileDrawable.this;
                    int unused12 = animatedFileDrawable5.invalidateAfter = animatedFileDrawable5.metaData[3] - AnimatedFileDrawable.this.lastTimeStamp;
                }
                if (AnimatedFileDrawable.this.pendingSeekToUI >= 0 && AnimatedFileDrawable.this.pendingSeekTo == -1) {
                    long unused13 = AnimatedFileDrawable.this.pendingSeekToUI = -1;
                    int unused14 = AnimatedFileDrawable.this.invalidateAfter = 0;
                }
                AnimatedFileDrawable animatedFileDrawable6 = AnimatedFileDrawable.this;
                int unused15 = animatedFileDrawable6.lastTimeStamp = animatedFileDrawable6.metaData[3];
                if (!AnimatedFileDrawable.this.secondParentViews.isEmpty()) {
                    int size = AnimatedFileDrawable.this.secondParentViews.size();
                    for (int i = 0; i < size; i++) {
                        ((View) AnimatedFileDrawable.this.secondParentViews.get(i)).invalidate();
                    }
                }
                if ((AnimatedFileDrawable.this.secondParentViews.isEmpty() || AnimatedFileDrawable.this.invalidateParentViewWithSecond) && AnimatedFileDrawable.this.parentView != null) {
                    AnimatedFileDrawable.this.parentView.invalidate();
                }
                AnimatedFileDrawable.this.scheduleNextGetFrame();
            }
        };
        this.loadFrameRunnable = new Runnable() {
            public void run() {
                if (!AnimatedFileDrawable.this.isRecycled) {
                    boolean z = false;
                    if (!AnimatedFileDrawable.this.decoderCreated && AnimatedFileDrawable.this.nativePtr == 0) {
                        AnimatedFileDrawable animatedFileDrawable = AnimatedFileDrawable.this;
                        animatedFileDrawable.nativePtr = AnimatedFileDrawable.createDecoder(animatedFileDrawable.path.getAbsolutePath(), AnimatedFileDrawable.this.metaData, AnimatedFileDrawable.this.currentAccount, AnimatedFileDrawable.this.streamFileSize, AnimatedFileDrawable.this.stream, false);
                        if (AnimatedFileDrawable.this.nativePtr != 0 && (AnimatedFileDrawable.this.metaData[0] > 3840 || AnimatedFileDrawable.this.metaData[1] > 3840)) {
                            AnimatedFileDrawable.destroyDecoder(AnimatedFileDrawable.this.nativePtr);
                            AnimatedFileDrawable.this.nativePtr = 0;
                        }
                        if (AnimatedFileDrawable.this.renderingHeight <= 0 || AnimatedFileDrawable.this.renderingWidth <= 0 || AnimatedFileDrawable.this.metaData[0] <= 0 || AnimatedFileDrawable.this.metaData[1] <= 0) {
                            float unused = AnimatedFileDrawable.this.scaleFactor = 1.0f;
                        } else {
                            AnimatedFileDrawable animatedFileDrawable2 = AnimatedFileDrawable.this;
                            float unused2 = animatedFileDrawable2.scaleFactor = Math.max(((float) animatedFileDrawable2.renderingWidth) / ((float) AnimatedFileDrawable.this.metaData[0]), ((float) AnimatedFileDrawable.this.renderingHeight) / ((float) AnimatedFileDrawable.this.metaData[1]));
                            if (AnimatedFileDrawable.this.scaleFactor <= 0.0f || ((double) AnimatedFileDrawable.this.scaleFactor) > 0.7d) {
                                float unused3 = AnimatedFileDrawable.this.scaleFactor = 1.0f;
                            }
                        }
                        boolean unused4 = AnimatedFileDrawable.this.decoderCreated = true;
                    }
                    try {
                        if (AnimatedFileDrawable.this.nativePtr == 0 && AnimatedFileDrawable.this.metaData[0] != 0) {
                            if (AnimatedFileDrawable.this.metaData[1] != 0) {
                                AndroidUtilities.runOnUIThread(AnimatedFileDrawable.this.uiRunnableNoFrame);
                                return;
                            }
                        }
                        if (AnimatedFileDrawable.this.backgroundBitmap == null && AnimatedFileDrawable.this.metaData[0] > 0 && AnimatedFileDrawable.this.metaData[1] > 0) {
                            AnimatedFileDrawable animatedFileDrawable3 = AnimatedFileDrawable.this;
                            Bitmap unused5 = animatedFileDrawable3.backgroundBitmap = Bitmap.createBitmap((int) (((float) animatedFileDrawable3.metaData[0]) * AnimatedFileDrawable.this.scaleFactor), (int) (((float) AnimatedFileDrawable.this.metaData[1]) * AnimatedFileDrawable.this.scaleFactor), Bitmap.Config.ARGB_8888);
                            if (AnimatedFileDrawable.this.backgroundShader == null && AnimatedFileDrawable.this.backgroundBitmap != null && AnimatedFileDrawable.this.hasRoundRadius()) {
                                AnimatedFileDrawable animatedFileDrawable4 = AnimatedFileDrawable.this;
                                Bitmap access$300 = AnimatedFileDrawable.this.backgroundBitmap;
                                Shader.TileMode tileMode = Shader.TileMode.CLAMP;
                                BitmapShader unused6 = animatedFileDrawable4.backgroundShader = new BitmapShader(access$300, tileMode, tileMode);
                            }
                        }
                    } catch (Throwable th) {
                        FileLog.e(th);
                    }
                    if (AnimatedFileDrawable.this.pendingSeekTo >= 0) {
                        AnimatedFileDrawable.this.metaData[3] = (int) AnimatedFileDrawable.this.pendingSeekTo;
                        long access$2300 = AnimatedFileDrawable.this.pendingSeekTo;
                        synchronized (AnimatedFileDrawable.this.sync) {
                            long unused7 = AnimatedFileDrawable.this.pendingSeekTo = -1;
                        }
                        if (AnimatedFileDrawable.this.stream != null) {
                            AnimatedFileDrawable.this.stream.reset();
                        }
                        AnimatedFileDrawable.seekToMs(AnimatedFileDrawable.this.nativePtr, access$2300, true);
                        z = true;
                    }
                    if (AnimatedFileDrawable.this.backgroundBitmap != null) {
                        long unused8 = AnimatedFileDrawable.this.lastFrameDecodeTime = System.currentTimeMillis();
                        if (AnimatedFileDrawable.getVideoFrame(AnimatedFileDrawable.this.nativePtr, AnimatedFileDrawable.this.backgroundBitmap, AnimatedFileDrawable.this.metaData, AnimatedFileDrawable.this.backgroundBitmap.getRowBytes(), false, AnimatedFileDrawable.this.startTime, AnimatedFileDrawable.this.endTime) == 0) {
                            AndroidUtilities.runOnUIThread(AnimatedFileDrawable.this.uiRunnableNoFrame);
                            return;
                        }
                        if (z) {
                            AnimatedFileDrawable animatedFileDrawable5 = AnimatedFileDrawable.this;
                            int unused9 = animatedFileDrawable5.lastTimeStamp = animatedFileDrawable5.metaData[3];
                        }
                        AnimatedFileDrawable animatedFileDrawable6 = AnimatedFileDrawable.this;
                        int unused10 = animatedFileDrawable6.backgroundBitmapTime = animatedFileDrawable6.metaData[3];
                    }
                }
                AndroidUtilities.runOnUIThread(AnimatedFileDrawable.this.uiRunnable);
            }
        };
        this.mStartTask = new Runnable() {
            public final void run() {
                AnimatedFileDrawable.this.lambda$new$1$AnimatedFileDrawable();
            }
        };
        this.path = file;
        this.streamFileSize = j4;
        this.currentAccount = i;
        this.renderingHeight = i3;
        this.renderingWidth = i2;
        getPaint().setFlags(3);
        if (j4 == 0 || (tLRPC$Document == null && imageLocation == null)) {
            j3 = 0;
        } else {
            j3 = 0;
            this.stream = new AnimatedFileDrawableStream(tLRPC$Document, imageLocation, obj, i, z2);
        }
        if (z) {
            this.nativePtr = createDecoder(file.getAbsolutePath(), iArr, this.currentAccount, this.streamFileSize, this.stream, z2);
            if (this.nativePtr != j3 && (iArr[0] > 3840 || iArr[1] > 3840)) {
                destroyDecoder(this.nativePtr);
                this.nativePtr = j3;
            }
            int i5 = this.renderingHeight;
            if (i5 <= 0 || (i4 = this.renderingWidth) <= 0 || iArr[0] <= 0 || iArr[1] <= 0) {
                this.scaleFactor = 1.0f;
            } else {
                float max = Math.max(((float) i4) / ((float) iArr[0]), ((float) i5) / ((float) iArr[1]));
                this.scaleFactor = max;
                if (max <= 0.0f || ((double) max) > 0.7d) {
                    this.scaleFactor = 1.0f;
                }
            }
            this.decoderCreated = true;
        }
        if (j5 != j3) {
            seekTo(j5, false);
        }
    }

    public Bitmap getFrameAtTime(long j) {
        return getFrameAtTime(j, false);
    }

    public Bitmap getFrameAtTime(long j, boolean z) {
        int i;
        if (!this.decoderCreated || this.nativePtr == 0) {
            return null;
        }
        AnimatedFileDrawableStream animatedFileDrawableStream = this.stream;
        if (animatedFileDrawableStream != null) {
            animatedFileDrawableStream.cancel(false);
            this.stream.reset();
        }
        if (!z) {
            seekToMs(this.nativePtr, j, z);
        }
        if (this.backgroundBitmap == null) {
            int[] iArr = this.metaData;
            float f = this.scaleFactor;
            this.backgroundBitmap = Bitmap.createBitmap((int) (((float) iArr[0]) * f), (int) (((float) iArr[1]) * f), Bitmap.Config.ARGB_8888);
        }
        if (z) {
            long j2 = this.nativePtr;
            Bitmap bitmap = this.backgroundBitmap;
            i = getFrameAtTime(j2, j, bitmap, this.metaData, bitmap.getRowBytes());
        } else {
            long j3 = this.nativePtr;
            Bitmap bitmap2 = this.backgroundBitmap;
            i = getVideoFrame(j3, bitmap2, this.metaData, bitmap2.getRowBytes(), true, 0.0f, 0.0f);
        }
        if (i != 0) {
            return this.backgroundBitmap;
        }
        return null;
    }

    public void setParentView(View view) {
        if (this.parentView == null) {
            this.parentView = view;
        }
    }

    public void setInvalidateParentViewWithSecond(boolean z) {
        this.invalidateParentViewWithSecond = z;
    }

    public void addSecondParentView(View view) {
        if (view != null && !this.secondParentViews.contains(view)) {
            this.secondParentViews.add(view);
        }
    }

    public void removeSecondParentView(View view) {
        this.secondParentViews.remove(view);
        if (!this.secondParentViews.isEmpty()) {
            return;
        }
        if (this.recycleWithSecond) {
            recycle();
            return;
        }
        int[] iArr = this.roundRadiusBackup;
        if (iArr != null) {
            setRoundRadius(iArr);
        }
    }

    public void setAllowDecodeSingleFrame(boolean z) {
        this.decodeSingleFrame = z;
        if (z) {
            scheduleNextGetFrame();
        }
    }

    public void seekTo(long j, boolean z) {
        seekTo(j, z, false);
    }

    public void seekTo(long j, boolean z, boolean z2) {
        AnimatedFileDrawableStream animatedFileDrawableStream;
        synchronized (this.sync) {
            this.pendingSeekTo = j;
            this.pendingSeekToUI = j;
            if (this.nativePtr != 0) {
                prepareToSeek(this.nativePtr);
            }
            if (this.decoderCreated && (animatedFileDrawableStream = this.stream) != null) {
                animatedFileDrawableStream.cancel(z);
                this.pendingRemoveLoading = z;
                this.pendingRemoveLoadingFramesReset = z ? 0 : 10;
            }
            if (z2 && this.decodeSingleFrame) {
                this.singleFrameDecoded = false;
                if (this.loadFrameTask == null) {
                    scheduleNextGetFrame();
                } else {
                    this.forceDecodeAfterNextFrame = true;
                }
            }
        }
    }

    public void recycle() {
        if (!this.secondParentViews.isEmpty()) {
            this.recycleWithSecond = true;
            return;
        }
        this.isRunning = false;
        this.isRecycled = true;
        if (this.loadFrameTask == null) {
            if (this.nativePtr != 0) {
                destroyDecoder(this.nativePtr);
                this.nativePtr = 0;
            }
            Bitmap bitmap = this.renderingBitmap;
            if (bitmap != null) {
                bitmap.recycle();
                this.renderingBitmap = null;
            }
            Bitmap bitmap2 = this.nextRenderingBitmap;
            if (bitmap2 != null) {
                bitmap2.recycle();
                this.nextRenderingBitmap = null;
            }
            DispatchQueue dispatchQueue = this.decodeQueue;
            if (dispatchQueue != null) {
                dispatchQueue.recycle();
                this.decodeQueue = null;
            }
        } else {
            this.destroyWhenDone = true;
        }
        AnimatedFileDrawableStream animatedFileDrawableStream = this.stream;
        if (animatedFileDrawableStream != null) {
            animatedFileDrawableStream.cancel(true);
        }
    }

    public void resetStream(boolean z) {
        AnimatedFileDrawableStream animatedFileDrawableStream = this.stream;
        if (animatedFileDrawableStream != null) {
            animatedFileDrawableStream.cancel(true);
        }
        if (this.nativePtr == 0) {
            return;
        }
        if (z) {
            stopDecoder(this.nativePtr);
        } else {
            prepareToSeek(this.nativePtr);
        }
    }

    protected static void runOnUiThread(Runnable runnable) {
        Looper myLooper = Looper.myLooper();
        Handler handler = uiHandler;
        if (myLooper == handler.getLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    public void setUseSharedQueue(boolean z) {
        this.useSharedQueue = z;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            recycle();
        } finally {
            super.finalize();
        }
    }

    public void start() {
        if (!this.isRunning) {
            this.isRunning = true;
            scheduleNextGetFrame();
            runOnUiThread(this.mStartTask);
        }
    }

    public float getCurrentProgress() {
        if (this.metaData[4] == 0) {
            return 0.0f;
        }
        if (this.pendingSeekToUI >= 0) {
            return ((float) this.pendingSeekToUI) / ((float) this.metaData[4]);
        }
        int[] iArr = this.metaData;
        return ((float) iArr[3]) / ((float) iArr[4]);
    }

    public int getCurrentProgressMs() {
        if (this.pendingSeekToUI >= 0) {
            return (int) this.pendingSeekToUI;
        }
        int i = this.nextRenderingBitmapTime;
        return i != 0 ? i : this.renderingBitmapTime;
    }

    public int getDurationMs() {
        return this.metaData[4];
    }

    /* access modifiers changed from: private */
    public void scheduleNextGetFrame() {
        if (this.loadFrameTask == null) {
            long j = 0;
            if ((this.nativePtr != 0 || !this.decoderCreated) && !this.destroyWhenDone) {
                if (!this.isRunning) {
                    boolean z = this.decodeSingleFrame;
                    if (!z) {
                        return;
                    }
                    if (z && this.singleFrameDecoded) {
                        return;
                    }
                }
                if (this.lastFrameDecodeTime != 0) {
                    int i = this.invalidateAfter;
                    j = Math.min((long) i, Math.max(0, ((long) i) - (System.currentTimeMillis() - this.lastFrameDecodeTime)));
                }
                if (this.useSharedQueue) {
                    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = executor;
                    Runnable runnable = this.loadFrameRunnable;
                    this.loadFrameTask = runnable;
                    scheduledThreadPoolExecutor.schedule(runnable, j, TimeUnit.MILLISECONDS);
                    return;
                }
                if (this.decodeQueue == null) {
                    this.decodeQueue = new DispatchQueue("decodeQueue" + this);
                }
                DispatchQueue dispatchQueue = this.decodeQueue;
                Runnable runnable2 = this.loadFrameRunnable;
                this.loadFrameTask = runnable2;
                dispatchQueue.postRunnable(runnable2, j);
            }
        }
    }

    public void stop() {
        this.isRunning = false;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public int getIntrinsicHeight() {
        int i = 0;
        if (this.decoderCreated) {
            int[] iArr = this.metaData;
            i = (iArr[2] == 90 || iArr[2] == 270) ? iArr[0] : iArr[1];
        }
        if (i == 0) {
            return AndroidUtilities.dp(100.0f);
        }
        return (int) (((float) i) * this.scaleFactor);
    }

    public int getIntrinsicWidth() {
        int i = 0;
        if (this.decoderCreated) {
            int[] iArr = this.metaData;
            i = (iArr[2] == 90 || iArr[2] == 270) ? iArr[1] : iArr[0];
        }
        if (i == 0) {
            return AndroidUtilities.dp(100.0f);
        }
        return (int) (((float) i) * this.scaleFactor);
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        this.applyTransformation = true;
    }

    public void draw(Canvas canvas) {
        Bitmap bitmap;
        Canvas canvas2 = canvas;
        if ((this.nativePtr != 0 || !this.decoderCreated) && !this.destroyWhenDone) {
            long currentTimeMillis = System.currentTimeMillis();
            if (this.isRunning) {
                Bitmap bitmap2 = this.renderingBitmap;
                if (bitmap2 == null && this.nextRenderingBitmap == null) {
                    scheduleNextGetFrame();
                } else if (this.nextRenderingBitmap != null && (bitmap2 == null || Math.abs(currentTimeMillis - this.lastFrameTime) >= ((long) this.invalidateAfter))) {
                    this.renderingBitmap = this.nextRenderingBitmap;
                    this.renderingBitmapTime = this.nextRenderingBitmapTime;
                    this.renderingShader = this.nextRenderingShader;
                    this.nextRenderingBitmap = null;
                    this.nextRenderingBitmapTime = 0;
                    this.nextRenderingShader = null;
                    this.lastFrameTime = currentTimeMillis;
                }
            } else if (!this.isRunning && this.decodeSingleFrame && Math.abs(currentTimeMillis - this.lastFrameTime) >= ((long) this.invalidateAfter) && (bitmap = this.nextRenderingBitmap) != null) {
                this.renderingBitmap = bitmap;
                this.renderingBitmapTime = this.nextRenderingBitmapTime;
                this.renderingShader = this.nextRenderingShader;
                this.nextRenderingBitmap = null;
                this.nextRenderingBitmapTime = 0;
                this.nextRenderingShader = null;
                this.lastFrameTime = currentTimeMillis;
            }
            Bitmap bitmap3 = this.renderingBitmap;
            if (bitmap3 != null) {
                if (this.applyTransformation) {
                    int width = bitmap3.getWidth();
                    int height = this.renderingBitmap.getHeight();
                    int[] iArr = this.metaData;
                    if (iArr[2] == 90 || iArr[2] == 270) {
                        int i = height;
                        height = width;
                        width = i;
                    }
                    this.dstRect.set(getBounds());
                    this.scaleX = ((float) this.dstRect.width()) / ((float) width);
                    this.scaleY = ((float) this.dstRect.height()) / ((float) height);
                    this.applyTransformation = false;
                }
                if (hasRoundRadius()) {
                    Math.max(this.scaleX, this.scaleY);
                    if (this.renderingShader == null) {
                        Bitmap bitmap4 = this.backgroundBitmap;
                        Shader.TileMode tileMode = Shader.TileMode.CLAMP;
                        this.renderingShader = new BitmapShader(bitmap4, tileMode, tileMode);
                    }
                    Paint paint = getPaint();
                    paint.setShader(this.renderingShader);
                    this.shaderMatrix.reset();
                    Matrix matrix = this.shaderMatrix;
                    Rect rect = this.dstRect;
                    matrix.setTranslate((float) rect.left, (float) rect.top);
                    int[] iArr2 = this.metaData;
                    if (iArr2[2] == 90) {
                        this.shaderMatrix.preRotate(90.0f);
                        this.shaderMatrix.preTranslate(0.0f, (float) (-this.dstRect.width()));
                    } else if (iArr2[2] == 180) {
                        this.shaderMatrix.preRotate(180.0f);
                        this.shaderMatrix.preTranslate((float) (-this.dstRect.width()), (float) (-this.dstRect.height()));
                    } else if (iArr2[2] == 270) {
                        this.shaderMatrix.preRotate(270.0f);
                        this.shaderMatrix.preTranslate((float) (-this.dstRect.height()), 0.0f);
                    }
                    this.shaderMatrix.preScale(this.scaleX, this.scaleY);
                    this.renderingShader.setLocalMatrix(this.shaderMatrix);
                    if (this.invalidatePath) {
                        this.invalidatePath = false;
                        int i2 = 0;
                        while (true) {
                            int[] iArr3 = this.roundRadius;
                            if (i2 >= iArr3.length) {
                                break;
                            }
                            float[] fArr = radii;
                            int i3 = i2 * 2;
                            fArr[i3] = (float) iArr3[i2];
                            fArr[i3 + 1] = (float) iArr3[i2];
                            i2++;
                        }
                        this.roundPath.reset();
                        this.roundPath.addRoundRect(this.actualDrawRect, radii, Path.Direction.CW);
                        this.roundPath.close();
                    }
                    canvas2.drawPath(this.roundPath, paint);
                } else {
                    Rect rect2 = this.dstRect;
                    canvas2.translate((float) rect2.left, (float) rect2.top);
                    int[] iArr4 = this.metaData;
                    if (iArr4[2] == 90) {
                        canvas2.rotate(90.0f);
                        canvas2.translate(0.0f, (float) (-this.dstRect.width()));
                    } else if (iArr4[2] == 180) {
                        canvas2.rotate(180.0f);
                        canvas2.translate((float) (-this.dstRect.width()), (float) (-this.dstRect.height()));
                    } else if (iArr4[2] == 270) {
                        canvas2.rotate(270.0f);
                        canvas2.translate((float) (-this.dstRect.height()), 0.0f);
                    }
                    canvas2.scale(this.scaleX, this.scaleY);
                    canvas2.drawBitmap(this.renderingBitmap, 0.0f, 0.0f, getPaint());
                }
                if (this.isRunning) {
                    long max = Math.max(1, (((long) this.invalidateAfter) - (currentTimeMillis - this.lastFrameTime)) - 17);
                    Handler handler = uiHandler;
                    handler.removeCallbacks(this.mInvalidateTask);
                    handler.postDelayed(this.mInvalidateTask, Math.min(max, (long) this.invalidateAfter));
                }
            }
        }
    }

    public int getMinimumHeight() {
        int i = 0;
        if (this.decoderCreated) {
            int[] iArr = this.metaData;
            i = (iArr[2] == 90 || iArr[2] == 270) ? iArr[0] : iArr[1];
        }
        return i == 0 ? AndroidUtilities.dp(100.0f) : i;
    }

    public int getMinimumWidth() {
        int i = 0;
        if (this.decoderCreated) {
            int[] iArr = this.metaData;
            i = (iArr[2] == 90 || iArr[2] == 270) ? iArr[1] : iArr[0];
        }
        return i == 0 ? AndroidUtilities.dp(100.0f) : i;
    }

    public Bitmap getAnimatedBitmap() {
        Bitmap bitmap = this.renderingBitmap;
        if (bitmap != null) {
            return bitmap;
        }
        Bitmap bitmap2 = this.nextRenderingBitmap;
        if (bitmap2 != null) {
            return bitmap2;
        }
        return null;
    }

    public void setActualDrawRect(float f, float f2, float f3, float f4) {
        float f5 = f4 + f2;
        float f6 = f3 + f;
        RectF rectF = this.actualDrawRect;
        if (rectF.left != f || rectF.top != f2 || rectF.right != f6 || rectF.bottom != f5) {
            rectF.set(f, f2, f6, f5);
            this.invalidatePath = true;
        }
    }

    public void setRoundRadius(int[] iArr) {
        if (!this.secondParentViews.isEmpty()) {
            if (this.roundRadiusBackup == null) {
                this.roundRadiusBackup = new int[4];
            }
            int[] iArr2 = this.roundRadius;
            int[] iArr3 = this.roundRadiusBackup;
            System.arraycopy(iArr2, 0, iArr3, 0, iArr3.length);
        }
        for (int i = 0; i < 4; i++) {
            if (!this.invalidatePath && iArr[i] != this.roundRadius[i]) {
                this.invalidatePath = true;
            }
            this.roundRadius[i] = iArr[i];
        }
    }

    /* access modifiers changed from: private */
    public boolean hasRoundRadius() {
        int i = 0;
        while (true) {
            int[] iArr = this.roundRadius;
            if (i >= iArr.length) {
                return false;
            }
            if (iArr[i] != 0) {
                return true;
            }
            i++;
        }
    }

    public boolean hasBitmap() {
        return (this.nativePtr == 0 || (this.renderingBitmap == null && this.nextRenderingBitmap == null)) ? false : true;
    }

    public int getOrientation() {
        return this.metaData[2];
    }

    public AnimatedFileDrawable makeCopy() {
        AnimatedFileDrawable animatedFileDrawable;
        AnimatedFileDrawableStream animatedFileDrawableStream = this.stream;
        if (animatedFileDrawableStream != null) {
            File file = this.path;
            long j = this.streamFileSize;
            TLRPC$Document document = animatedFileDrawableStream.getDocument();
            ImageLocation location = this.stream.getLocation();
            Object parentObject = this.stream.getParentObject();
            long j2 = this.pendingSeekToUI;
            int i = this.currentAccount;
            AnimatedFileDrawableStream animatedFileDrawableStream2 = this.stream;
            animatedFileDrawable = new AnimatedFileDrawable(file, false, j, document, location, parentObject, j2, i, animatedFileDrawableStream2 != null && animatedFileDrawableStream2.isPreview());
        } else {
            File file2 = this.path;
            long j3 = this.streamFileSize;
            long j4 = this.pendingSeekToUI;
            int i2 = this.currentAccount;
            AnimatedFileDrawableStream animatedFileDrawableStream3 = this.stream;
            animatedFileDrawable = new AnimatedFileDrawable(file2, false, j3, (TLRPC$Document) null, (ImageLocation) null, (Object) null, j4, i2, animatedFileDrawableStream3 != null && animatedFileDrawableStream3.isPreview());
        }
        AnimatedFileDrawable animatedFileDrawable2 = animatedFileDrawable;
        int[] iArr = animatedFileDrawable2.metaData;
        int[] iArr2 = this.metaData;
        iArr[0] = iArr2[0];
        iArr[1] = iArr2[1];
        return animatedFileDrawable2;
    }

    public static void getVideoInfo(String str, int[] iArr) {
        getVideoInfo(Build.VERSION.SDK_INT, str, iArr);
    }

    public void setStartEndTime(long j, long j2) {
        this.startTime = ((float) j) / 1000.0f;
        this.endTime = ((float) j2) / 1000.0f;
        if (((long) getCurrentProgressMs()) < j) {
            seekTo(j, true);
        }
    }

    public long getStartTime() {
        return (long) (this.startTime * 1000.0f);
    }
}
