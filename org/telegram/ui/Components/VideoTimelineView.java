package org.telegram.ui.Components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.volley.DefaultRetryPolicy;
import org.telegram.ui.ActionBar.Theme;

@TargetApi(10)
public class VideoTimelineView extends View {
    private static final Object sync = new Object();
    private AsyncTask<Integer, Integer, Bitmap> currentTask;
    private VideoTimelineViewDelegate delegate;
    private int frameHeight;
    private long frameTimeOffset;
    private int frameWidth;
    private ArrayList<Bitmap> frames = new ArrayList();
    private int framesToLoad;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private Paint paint = new Paint(1);
    private Paint paint2;
    private float pressDx;
    private boolean pressedLeft;
    private boolean pressedRight;
    private float progressLeft;
    private float progressRight = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
    private long videoLength;

    public interface VideoTimelineViewDelegate {
        void onLeftProgressChanged(float f);

        void onRifhtProgressChanged(float f);
    }

    public VideoTimelineView(Context context) {
        super(context);
        this.paint.setColor(-1);
        this.paint2 = new Paint();
        this.paint2.setColor(Theme.ACTION_BAR_PHOTO_VIEWER_COLOR);
    }

    public float getLeftProgress() {
        return this.progressLeft;
    }

    public float getRightProgress() {
        return this.progressRight;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        int width = getMeasuredWidth() - AndroidUtilities.dp(32.0f);
        int startX = ((int) (((float) width) * this.progressLeft)) + AndroidUtilities.dp(16.0f);
        int endX = ((int) (((float) width) * this.progressRight)) + AndroidUtilities.dp(16.0f);
        if (event.getAction() == 0) {
            int additionWidth = AndroidUtilities.dp(12.0f);
            if (((float) (startX - additionWidth)) <= x && x <= ((float) (startX + additionWidth)) && y >= 0.0f && y <= ((float) getMeasuredHeight())) {
                this.pressedLeft = true;
                this.pressDx = (float) ((int) (x - ((float) startX)));
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            } else if (((float) (endX - additionWidth)) > x || x > ((float) (endX + additionWidth)) || y < 0.0f || y > ((float) getMeasuredHeight())) {
                return false;
            } else {
                this.pressedRight = true;
                this.pressDx = (float) ((int) (x - ((float) endX)));
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            }
        } else if (event.getAction() == 1 || event.getAction() == 3) {
            if (this.pressedLeft) {
                this.pressedLeft = false;
                return true;
            } else if (!this.pressedRight) {
                return false;
            } else {
                this.pressedRight = false;
                return true;
            }
        } else if (event.getAction() != 2) {
            return false;
        } else {
            if (this.pressedLeft) {
                startX = (int) (x - this.pressDx);
                if (startX < AndroidUtilities.dp(16.0f)) {
                    startX = AndroidUtilities.dp(16.0f);
                } else if (startX > endX) {
                    startX = endX;
                }
                this.progressLeft = ((float) (startX - AndroidUtilities.dp(16.0f))) / ((float) width);
                if (this.delegate != null) {
                    this.delegate.onLeftProgressChanged(this.progressLeft);
                }
                invalidate();
                return true;
            } else if (!this.pressedRight) {
                return false;
            } else {
                endX = (int) (x - this.pressDx);
                if (endX < startX) {
                    endX = startX;
                } else if (endX > AndroidUtilities.dp(16.0f) + width) {
                    endX = width + AndroidUtilities.dp(16.0f);
                }
                this.progressRight = ((float) (endX - AndroidUtilities.dp(16.0f))) / ((float) width);
                if (this.delegate != null) {
                    this.delegate.onRifhtProgressChanged(this.progressRight);
                }
                invalidate();
                return true;
            }
        }
    }

    public void setVideoPath(String path) {
        this.mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            this.mediaMetadataRetriever.setDataSource(path);
            this.videoLength = Long.parseLong(this.mediaMetadataRetriever.extractMetadata(9));
        } catch (Throwable e) {
            FileLog.e("tmessages", e);
        }
    }

    public void setDelegate(VideoTimelineViewDelegate delegate) {
        this.delegate = delegate;
    }

    private void reloadFrames(int frameNum) {
        if (this.mediaMetadataRetriever != null) {
            if (frameNum == 0) {
                this.frameHeight = AndroidUtilities.dp(40.0f);
                this.framesToLoad = (getMeasuredWidth() - AndroidUtilities.dp(16.0f)) / this.frameHeight;
                this.frameWidth = (int) Math.ceil((double) (((float) (getMeasuredWidth() - AndroidUtilities.dp(16.0f))) / ((float) this.framesToLoad)));
                this.frameTimeOffset = this.videoLength / ((long) this.framesToLoad);
            }
            this.currentTask = new AsyncTask<Integer, Integer, Bitmap>() {
                private int frameNum = 0;

                protected Bitmap doInBackground(Integer... objects) {
                    this.frameNum = objects[0].intValue();
                    Bitmap bitmap = null;
                    if (isCancelled()) {
                        return null;
                    }
                    try {
                        bitmap = VideoTimelineView.this.mediaMetadataRetriever.getFrameAtTime((VideoTimelineView.this.frameTimeOffset * ((long) this.frameNum)) * 1000);
                        if (isCancelled()) {
                            return null;
                        }
                        if (bitmap != null) {
                            float scale;
                            Bitmap result = Bitmap.createBitmap(VideoTimelineView.this.frameWidth, VideoTimelineView.this.frameHeight, bitmap.getConfig());
                            Canvas canvas = new Canvas(result);
                            float scaleX = ((float) VideoTimelineView.this.frameWidth) / ((float) bitmap.getWidth());
                            float scaleY = ((float) VideoTimelineView.this.frameHeight) / ((float) bitmap.getHeight());
                            if (scaleX > scaleY) {
                                scale = scaleX;
                            } else {
                                scale = scaleY;
                            }
                            int w = (int) (((float) bitmap.getWidth()) * scale);
                            int h = (int) (((float) bitmap.getHeight()) * scale);
                            canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect((VideoTimelineView.this.frameWidth - w) / 2, (VideoTimelineView.this.frameHeight - h) / 2, w, h), null);
                            bitmap.recycle();
                            bitmap = result;
                        }
                        return bitmap;
                    } catch (Throwable e) {
                        FileLog.e("tmessages", e);
                    }
                }

                protected void onPostExecute(Bitmap bitmap) {
                    if (!isCancelled()) {
                        VideoTimelineView.this.frames.add(bitmap);
                        VideoTimelineView.this.invalidate();
                        if (this.frameNum < VideoTimelineView.this.framesToLoad) {
                            VideoTimelineView.this.reloadFrames(this.frameNum + 1);
                        }
                    }
                }
            };
            this.currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Integer[]{Integer.valueOf(frameNum), null, null});
        }
    }

    public void destroy() {
        synchronized (sync) {
            try {
                if (this.mediaMetadataRetriever != null) {
                    this.mediaMetadataRetriever.release();
                    this.mediaMetadataRetriever = null;
                }
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }
        }
        Iterator it = this.frames.iterator();
        while (it.hasNext()) {
            Bitmap bitmap = (Bitmap) it.next();
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        this.frames.clear();
        if (this.currentTask != null) {
            this.currentTask.cancel(true);
            this.currentTask = null;
        }
    }

    public void clearFrames() {
        Iterator it = this.frames.iterator();
        while (it.hasNext()) {
            Bitmap bitmap = (Bitmap) it.next();
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        this.frames.clear();
        if (this.currentTask != null) {
            this.currentTask.cancel(true);
            this.currentTask = null;
        }
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth() - AndroidUtilities.dp(36.0f);
        int startX = ((int) (((float) width) * this.progressLeft)) + AndroidUtilities.dp(16.0f);
        int endX = ((int) (((float) width) * this.progressRight)) + AndroidUtilities.dp(16.0f);
        canvas.save();
        canvas.clipRect(AndroidUtilities.dp(16.0f), 0, AndroidUtilities.dp(20.0f) + width, AndroidUtilities.dp(44.0f));
        if (this.frames.isEmpty() && this.currentTask == null) {
            reloadFrames(0);
        } else {
            int offset = 0;
            for (int a = 0; a < this.frames.size(); a++) {
                Bitmap bitmap = (Bitmap) this.frames.get(a);
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, (float) (AndroidUtilities.dp(16.0f) + (this.frameWidth * offset)), (float) AndroidUtilities.dp(2.0f), null);
                }
                offset++;
            }
        }
        canvas.drawRect((float) AndroidUtilities.dp(16.0f), (float) AndroidUtilities.dp(2.0f), (float) startX, (float) AndroidUtilities.dp(42.0f), this.paint2);
        canvas.drawRect((float) (AndroidUtilities.dp(4.0f) + endX), (float) AndroidUtilities.dp(2.0f), (float) ((AndroidUtilities.dp(16.0f) + width) + AndroidUtilities.dp(4.0f)), (float) AndroidUtilities.dp(42.0f), this.paint2);
        canvas.drawRect((float) startX, 0.0f, (float) (AndroidUtilities.dp(2.0f) + startX), (float) AndroidUtilities.dp(44.0f), this.paint);
        canvas.drawRect((float) (AndroidUtilities.dp(2.0f) + endX), 0.0f, (float) (AndroidUtilities.dp(4.0f) + endX), (float) AndroidUtilities.dp(44.0f), this.paint);
        canvas.drawRect((float) (AndroidUtilities.dp(2.0f) + startX), 0.0f, (float) (AndroidUtilities.dp(4.0f) + endX), (float) AndroidUtilities.dp(2.0f), this.paint);
        canvas.drawRect((float) (AndroidUtilities.dp(2.0f) + startX), (float) AndroidUtilities.dp(42.0f), (float) (AndroidUtilities.dp(4.0f) + endX), (float) AndroidUtilities.dp(44.0f), this.paint);
        canvas.restore();
        canvas.drawCircle((float) startX, (float) (getMeasuredHeight() / 2), (float) AndroidUtilities.dp(7.0f), this.paint);
        canvas.drawCircle((float) (AndroidUtilities.dp(4.0f) + endX), (float) (getMeasuredHeight() / 2), (float) AndroidUtilities.dp(7.0f), this.paint);
    }
}
