package org.telegram.messenger;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.view.View;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentEncrypted;
import org.telegram.tgnet.TLRPC.TL_fileEncryptedLocation;
import org.telegram.tgnet.TLRPC.TL_fileLocation;
import org.telegram.tgnet.TLRPC.TL_photoStrippedSize;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.RecyclableDrawable;

public class ImageReceiver implements NotificationCenterDelegate {
    private static PorterDuffColorFilter selectedColorFilter = new PorterDuffColorFilter(-2236963, Mode.MULTIPLY);
    private static PorterDuffColorFilter selectedGroupColorFilter = new PorterDuffColorFilter(-4473925, Mode.MULTIPLY);
    private boolean allowDecodeSingleFrame;
    private boolean allowStartAnimation;
    private RectF bitmapRect;
    private BitmapShader bitmapShader;
    private BitmapShader bitmapShaderThumb;
    private boolean canceledLoading;
    private boolean centerRotation;
    private ColorFilter colorFilter;
    private byte crossfadeAlpha;
    private Drawable crossfadeImage;
    private String crossfadeKey;
    private BitmapShader crossfadeShader;
    private boolean crossfadeWithOldImage;
    private boolean crossfadeWithThumb;
    private boolean crossfadingWithThumb;
    private int currentAccount;
    private float currentAlpha;
    private int currentCacheType;
    private String currentExt;
    private String currentFilter;
    private String currentHttpUrl;
    private Drawable currentImage;
    private TLObject currentImageLocation;
    private String currentKey;
    private boolean currentKeyQuality;
    private Object currentParentObject;
    private int currentSize;
    private Drawable currentThumb;
    private String currentThumbFilter;
    private String currentThumbKey;
    private TLObject currentThumbLocation;
    private ImageReceiverDelegate delegate;
    private Rect drawRegion;
    private boolean forceCrossfade;
    private boolean forceLoding;
    private boolean forcePreview;
    private int imageH;
    private int imageW;
    private int imageX;
    private int imageY;
    private boolean invalidateAll;
    private boolean isAspectFit;
    private int isPressed;
    private boolean isVisible;
    private long lastUpdateAlphaTime;
    private boolean manualAlphaAnimator;
    private boolean needsQualityThumb;
    private int orientation;
    private float overrideAlpha;
    private int param;
    private View parentView;
    private Paint roundPaint;
    private int roundRadius;
    private RectF roundRect;
    private SetImageBackup setImageBackup;
    private Matrix shaderMatrix;
    private boolean shouldGenerateQualityThumb;
    private Drawable staticThumb;
    private int tag;
    private int thumbOrientation;
    private int thumbTag;

    public static class BitmapHolder {
        public Bitmap bitmap;
        private String key;

        public BitmapHolder(Bitmap b, String k) {
            this.bitmap = b;
            this.key = k;
            if (this.key != null) {
                ImageLoader.getInstance().incrementUseCount(this.key);
            }
        }

        public int getWidth() {
            return this.bitmap != null ? this.bitmap.getWidth() : 0;
        }

        public int getHeight() {
            return this.bitmap != null ? this.bitmap.getHeight() : 0;
        }

        public boolean isRecycled() {
            return this.bitmap == null || this.bitmap.isRecycled();
        }

        public void release() {
            if (this.key == null) {
                this.bitmap = null;
                return;
            }
            boolean canDelete = ImageLoader.getInstance().decrementUseCount(this.key);
            if (!ImageLoader.getInstance().isInCache(this.key) && canDelete) {
                this.bitmap.recycle();
            }
            this.key = null;
            this.bitmap = null;
        }
    }

    public interface ImageReceiverDelegate {
        void didSetImage(ImageReceiver imageReceiver, boolean z, boolean z2);
    }

    private class SetImageBackup {
        public int cacheType;
        public String ext;
        public TLObject fileLocation;
        public String filter;
        public String httpUrl;
        public Object parentObject;
        public int size;
        public Drawable thumb;
        public String thumbFilter;
        public TLObject thumbLocation;

        private SetImageBackup() {
        }
    }

    public ImageReceiver() {
        this(null);
    }

    public ImageReceiver(View view) {
        this.allowStartAnimation = true;
        this.drawRegion = new Rect();
        this.isVisible = true;
        this.roundRect = new RectF();
        this.bitmapRect = new RectF();
        this.shaderMatrix = new Matrix();
        this.overrideAlpha = 1.0f;
        this.crossfadeAlpha = (byte) 1;
        this.parentView = view;
        this.roundPaint = new Paint(3);
        this.currentAccount = UserConfig.selectedAccount;
    }

    public void cancelLoadImage() {
        this.forceLoding = false;
        ImageLoader.getInstance().cancelLoadingForImageReceiver(this, 0);
        this.canceledLoading = true;
    }

    public void setForceLoading(boolean value) {
        this.forceLoding = value;
    }

    public boolean isForceLoding() {
        return this.forceLoding;
    }

    public void setImage(TLObject path, String filter, Drawable thumb, String ext, Object parentObject, int cacheType) {
        setImage(path, null, filter, thumb, null, null, 0, ext, parentObject, cacheType);
    }

    public void setImage(TLObject path, String filter, Drawable thumb, int size, String ext, Object parentObject, int cacheType) {
        setImage(path, null, filter, thumb, null, null, size, ext, parentObject, cacheType);
    }

    public void setImage(String httpUrl, String filter, Drawable thumb, String ext, int size) {
        setImage(null, httpUrl, filter, thumb, null, null, size, ext, null, 1);
    }

    public void setImage(TLObject fileLocation, String filter, TLObject thumbLocation, String thumbFilter, String ext, Object parentObject, int cacheType) {
        setImage(fileLocation, null, filter, null, thumbLocation, thumbFilter, 0, ext, parentObject, cacheType);
    }

    public void setImage(TLObject fileLocation, String filter, TLObject thumbLocation, String thumbFilter, int size, String ext, Object parentObject, int cacheType) {
        setImage(fileLocation, null, filter, null, thumbLocation, thumbFilter, size, ext, parentObject, cacheType);
    }

    public void setImage(TLObject fileLocation, String httpUrl, String filter, Drawable thumb, TLObject thumbLocation, String thumbFilter, int size, String ext, Object parentObject, int cacheType) {
        if (this.setImageBackup != null) {
            this.setImageBackup.fileLocation = null;
            this.setImageBackup.httpUrl = null;
            this.setImageBackup.thumbLocation = null;
            this.setImageBackup.thumb = null;
        }
        ImageReceiverDelegate imageReceiverDelegate;
        boolean z;
        boolean z2;
        if (!(fileLocation == null && httpUrl == null && thumbLocation == null) && (fileLocation == null || (fileLocation instanceof TL_fileLocation) || (fileLocation instanceof TL_fileEncryptedLocation) || (fileLocation instanceof TL_document) || (fileLocation instanceof WebFile) || (fileLocation instanceof TL_documentEncrypted) || (fileLocation instanceof PhotoSize) || (fileLocation instanceof SecureDocument))) {
            FileLocation location;
            TL_photoStrippedSize location2;
            PhotoSize photoSize;
            if (!((thumbLocation instanceof PhotoSize) || (thumbLocation instanceof TL_fileLocation) || (thumbLocation instanceof TL_fileEncryptedLocation))) {
                thumbLocation = null;
            }
            String key = null;
            if (fileLocation != null) {
                if (fileLocation instanceof SecureDocument) {
                    SecureDocument document = (SecureDocument) fileLocation;
                    key = document.secureFile.dc_id + "_" + document.secureFile.id;
                } else if (fileLocation instanceof FileLocation) {
                    location = (FileLocation) fileLocation;
                    key = location.volume_id + "_" + location.local_id;
                } else if (fileLocation instanceof TL_photoStrippedSize) {
                    location2 = (TL_photoStrippedSize) fileLocation;
                    key = "stripped" + FileRefController.getKeyForParentObject(parentObject);
                } else if (fileLocation instanceof PhotoSize) {
                    photoSize = (PhotoSize) fileLocation;
                    key = photoSize.location.volume_id + "_" + photoSize.location.local_id;
                } else if (fileLocation instanceof WebFile) {
                    key = Utilities.MD5(((WebFile) fileLocation).url);
                } else {
                    Document location3 = (Document) fileLocation;
                    if (location3.dc_id != 0) {
                        key = location3.dc_id + "_" + location3.id;
                    } else {
                        fileLocation = null;
                    }
                }
            } else if (httpUrl != null) {
                key = Utilities.MD5(httpUrl);
            }
            this.currentKeyQuality = false;
            if (key == null && this.needsQualityThumb && (parentObject instanceof MessageObject)) {
                Document document2 = ((MessageObject) parentObject).getDocument();
                if (document2 != null) {
                    key = "q_" + document2.dc_id + "_" + document2.id;
                    this.currentKeyQuality = true;
                }
            }
            if (!(key == null || filter == null)) {
                key = key + "@" + filter;
            }
            if (this.currentKey != null && this.currentKey.equals(key)) {
                if (this.delegate != null) {
                    imageReceiverDelegate = this.delegate;
                    z = (this.currentImage == null && this.currentThumb == null && this.staticThumb == null) ? false : true;
                    if (this.currentImage == null) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    imageReceiverDelegate.didSetImage(this, z, z2);
                }
                if (!(this.canceledLoading || this.forcePreview)) {
                    return;
                }
            }
            String thumbKey = null;
            if (thumbLocation instanceof FileLocation) {
                location = (FileLocation) thumbLocation;
                thumbKey = location.volume_id + "_" + location.local_id;
            } else if (thumbLocation instanceof TL_photoStrippedSize) {
                location2 = (TL_photoStrippedSize) thumbLocation;
                thumbKey = "stripped" + FileRefController.getKeyForParentObject(parentObject);
            } else if (thumbLocation instanceof PhotoSize) {
                photoSize = (PhotoSize) thumbLocation;
                thumbKey = photoSize.location.volume_id + "_" + photoSize.location.local_id;
            }
            if (!(thumbKey == null || thumbFilter == null)) {
                thumbKey = thumbKey + "@" + thumbFilter;
            }
            if (!this.crossfadeWithOldImage) {
                recycleBitmap(key, 0);
                recycleBitmap(thumbKey, 1);
                recycleBitmap(null, 2);
                this.crossfadeShader = null;
            } else if (this.currentImage != null) {
                recycleBitmap(thumbKey, 1);
                recycleBitmap(null, 2);
                this.crossfadeShader = this.bitmapShader;
                this.crossfadeImage = this.currentImage;
                this.crossfadeKey = this.currentKey;
                this.crossfadingWithThumb = false;
                this.currentImage = null;
                this.currentKey = null;
            } else if (this.currentThumb != null) {
                recycleBitmap(key, 0);
                recycleBitmap(null, 2);
                this.crossfadeShader = this.bitmapShaderThumb;
                this.crossfadeImage = this.currentThumb;
                this.crossfadeKey = this.currentThumbKey;
                this.crossfadingWithThumb = false;
                this.currentThumb = null;
                this.currentThumbKey = null;
            } else if (this.staticThumb != null) {
                recycleBitmap(key, 0);
                recycleBitmap(thumbKey, 1);
                recycleBitmap(null, 2);
                this.crossfadeShader = this.bitmapShaderThumb;
                this.crossfadeImage = this.staticThumb;
                this.crossfadingWithThumb = false;
                this.crossfadeKey = null;
                this.currentThumb = null;
                this.currentThumbKey = null;
            } else {
                recycleBitmap(key, 0);
                recycleBitmap(thumbKey, 1);
                recycleBitmap(null, 2);
                this.crossfadeShader = null;
            }
            this.currentParentObject = parentObject;
            this.currentThumbKey = thumbKey;
            this.currentKey = key;
            this.currentExt = ext;
            this.currentImageLocation = fileLocation;
            this.currentHttpUrl = httpUrl;
            this.currentFilter = filter;
            this.currentThumbFilter = thumbFilter;
            this.currentSize = size;
            this.currentCacheType = cacheType;
            this.currentThumbLocation = thumbLocation;
            this.staticThumb = thumb;
            this.bitmapShader = null;
            this.bitmapShaderThumb = null;
            this.currentAlpha = 1.0f;
            if (this.delegate != null) {
                imageReceiverDelegate = this.delegate;
                z = (this.currentImage == null && this.currentThumb == null && this.staticThumb == null) ? false : true;
                imageReceiverDelegate.didSetImage(this, z, this.currentImage == null);
            }
            ImageLoader.getInstance().loadImageForImageReceiver(this);
            if (this.parentView == null) {
                return;
            }
            if (this.invalidateAll) {
                this.parentView.invalidate();
                return;
            } else {
                this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                return;
            }
        }
        for (int a = 0; a < 3; a++) {
            recycleBitmap(null, a);
        }
        this.currentKey = null;
        this.currentExt = ext;
        this.currentThumbKey = null;
        this.currentThumbFilter = null;
        this.currentImageLocation = null;
        this.currentHttpUrl = null;
        this.currentFilter = null;
        this.currentParentObject = null;
        this.currentCacheType = 0;
        this.staticThumb = thumb;
        this.currentAlpha = 1.0f;
        this.currentThumbLocation = null;
        this.currentSize = 0;
        this.currentImage = null;
        this.bitmapShader = null;
        this.bitmapShaderThumb = null;
        this.crossfadeShader = null;
        ImageLoader.getInstance().cancelLoadingForImageReceiver(this, 0);
        if (this.parentView != null) {
            if (this.invalidateAll) {
                this.parentView.invalidate();
            } else {
                this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
            }
        }
        if (this.delegate != null) {
            imageReceiverDelegate = this.delegate;
            if (this.currentImage == null && this.currentThumb == null && this.staticThumb == null) {
                z = false;
            } else {
                z = true;
            }
            if (this.currentImage == null) {
                z2 = true;
            } else {
                z2 = false;
            }
            imageReceiverDelegate.didSetImage(this, z, z2);
        }
    }

    public boolean canInvertBitmap() {
        return (this.currentImage instanceof ExtendedBitmapDrawable) || (this.currentThumb instanceof ExtendedBitmapDrawable) || (this.staticThumb instanceof ExtendedBitmapDrawable);
    }

    public void setColorFilter(ColorFilter filter) {
        this.colorFilter = filter;
    }

    public void setDelegate(ImageReceiverDelegate delegate) {
        this.delegate = delegate;
    }

    public void setPressed(int value) {
        this.isPressed = value;
    }

    public boolean getPressed() {
        return this.isPressed != 0;
    }

    public void setOrientation(int angle, boolean center) {
        while (angle < 0) {
            angle += 360;
        }
        while (angle > 360) {
            angle -= 360;
        }
        this.thumbOrientation = angle;
        this.orientation = angle;
        this.centerRotation = center;
    }

    public void setInvalidateAll(boolean value) {
        this.invalidateAll = value;
    }

    public Drawable getStaticThumb() {
        return this.staticThumb;
    }

    public int getAnimatedOrientation() {
        if (this.currentImage instanceof AnimatedFileDrawable) {
            return ((AnimatedFileDrawable) this.currentImage).getOrientation();
        }
        if (this.staticThumb instanceof AnimatedFileDrawable) {
            return ((AnimatedFileDrawable) this.staticThumb).getOrientation();
        }
        if (this.currentImage instanceof ExtendedBitmapDrawable) {
            return ((ExtendedBitmapDrawable) this.currentImage).getOrientation();
        }
        return 0;
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void setImageBitmap(Bitmap bitmap) {
        Drawable bitmapDrawable;
        if (bitmap != null) {
            bitmapDrawable = new BitmapDrawable(null, bitmap);
        } else {
            bitmapDrawable = null;
        }
        setImageBitmap(bitmapDrawable);
    }

    public void setImageBitmap(Drawable bitmap) {
        boolean z = false;
        ImageLoader.getInstance().cancelLoadingForImageReceiver(this, 0);
        int a;
        if (!this.crossfadeWithOldImage) {
            for (a = 0; a < 3; a++) {
                recycleBitmap(null, a);
            }
        } else if (this.currentImage != null) {
            recycleBitmap(null, 1);
            recycleBitmap(null, 2);
            this.crossfadeShader = this.bitmapShader;
            this.crossfadeImage = this.currentImage;
            this.crossfadeKey = this.currentKey;
            this.crossfadingWithThumb = true;
        } else if (this.currentThumb != null) {
            recycleBitmap(null, 0);
            recycleBitmap(null, 2);
            this.crossfadeShader = this.bitmapShaderThumb;
            this.crossfadeImage = this.currentThumb;
            this.crossfadeKey = this.currentThumbKey;
            this.crossfadingWithThumb = true;
        } else if (this.staticThumb != null) {
            recycleBitmap(null, 0);
            recycleBitmap(null, 1);
            recycleBitmap(null, 2);
            this.crossfadeShader = this.bitmapShaderThumb;
            this.crossfadeImage = this.staticThumb;
            this.crossfadingWithThumb = true;
            this.crossfadeKey = null;
        } else {
            for (a = 0; a < 3; a++) {
                recycleBitmap(null, a);
            }
            this.crossfadeShader = null;
        }
        if (this.staticThumb instanceof RecyclableDrawable) {
            this.staticThumb.recycle();
        }
        this.staticThumb = bitmap;
        if (this.roundRadius == 0 || !(bitmap instanceof BitmapDrawable)) {
            this.bitmapShaderThumb = null;
        } else {
            this.bitmapShaderThumb = new BitmapShader(((BitmapDrawable) bitmap).getBitmap(), TileMode.CLAMP, TileMode.CLAMP);
        }
        this.currentThumbLocation = null;
        this.currentKey = null;
        this.currentExt = null;
        this.currentThumbKey = null;
        this.currentKeyQuality = false;
        this.currentImage = null;
        this.currentThumbFilter = null;
        this.currentImageLocation = null;
        this.currentHttpUrl = null;
        this.currentFilter = null;
        this.currentSize = 0;
        this.currentCacheType = 0;
        this.bitmapShader = null;
        if (this.setImageBackup != null) {
            this.setImageBackup.fileLocation = null;
            this.setImageBackup.httpUrl = null;
            this.setImageBackup.thumbLocation = null;
            this.setImageBackup.thumb = null;
        }
        this.currentAlpha = 1.0f;
        if (this.delegate != null) {
            boolean z2;
            ImageReceiverDelegate imageReceiverDelegate = this.delegate;
            if (this.currentThumb == null && this.staticThumb == null) {
                z2 = false;
            } else {
                z2 = true;
            }
            imageReceiverDelegate.didSetImage(this, z2, true);
        }
        if (this.parentView != null) {
            if (this.invalidateAll) {
                this.parentView.invalidate();
            } else {
                this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
            }
        }
        if (this.forceCrossfade && this.crossfadeWithOldImage && this.crossfadeImage != null) {
            this.currentAlpha = 0.0f;
            this.lastUpdateAlphaTime = System.currentTimeMillis();
            if (!(this.currentThumb == null && this.staticThumb == null)) {
                z = true;
            }
            this.crossfadeWithThumb = z;
        }
    }

    public void clearImage() {
        for (int a = 0; a < 3; a++) {
            recycleBitmap(null, a);
        }
        ImageLoader.getInstance().cancelLoadingForImageReceiver(this, 0);
    }

    public void onDetachedFromWindow() {
        if (!(this.currentImageLocation == null && this.currentHttpUrl == null && this.currentThumbLocation == null && this.staticThumb == null)) {
            if (this.setImageBackup == null) {
                this.setImageBackup = new SetImageBackup();
            }
            this.setImageBackup.fileLocation = this.currentImageLocation;
            this.setImageBackup.httpUrl = this.currentHttpUrl;
            this.setImageBackup.filter = this.currentFilter;
            this.setImageBackup.thumb = this.staticThumb;
            this.setImageBackup.thumbLocation = this.currentThumbLocation;
            this.setImageBackup.thumbFilter = this.currentThumbFilter;
            this.setImageBackup.size = this.currentSize;
            this.setImageBackup.ext = this.currentExt;
            this.setImageBackup.cacheType = this.currentCacheType;
            this.setImageBackup.parentObject = this.currentParentObject;
        }
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReplacedPhotoInMemCache);
        clearImage();
    }

    public boolean onAttachedToWindow() {
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didReplacedPhotoInMemCache);
        if (this.setImageBackup == null || (this.setImageBackup.fileLocation == null && this.setImageBackup.httpUrl == null && this.setImageBackup.thumbLocation == null && this.setImageBackup.thumb == null)) {
            return false;
        }
        setImage(this.setImageBackup.fileLocation, this.setImageBackup.httpUrl, this.setImageBackup.filter, this.setImageBackup.thumb, this.setImageBackup.thumbLocation, this.setImageBackup.thumbFilter, this.setImageBackup.size, this.setImageBackup.ext, this.setImageBackup.parentObject, this.setImageBackup.cacheType);
        return true;
    }

    private void drawDrawable(Canvas canvas, Drawable drawable, int alpha, BitmapShader shader, boolean thumb) {
        if (drawable instanceof BitmapDrawable) {
            Paint paint;
            int bitmapW;
            int bitmapH;
            Drawable bitmapDrawable = (BitmapDrawable) drawable;
            int o = thumb ? this.thumbOrientation : this.orientation;
            if (shader != null) {
                paint = this.roundPaint;
            } else {
                paint = bitmapDrawable.getPaint();
            }
            boolean hasFilter = (paint == null || paint.getColorFilter() == null) ? false : true;
            if (hasFilter && this.isPressed == 0) {
                if (shader != null) {
                    this.roundPaint.setColorFilter(null);
                } else if (this.staticThumb != drawable) {
                    bitmapDrawable.setColorFilter(null);
                }
            } else if (!(hasFilter || this.isPressed == 0)) {
                if (this.isPressed == 1) {
                    if (shader != null) {
                        this.roundPaint.setColorFilter(selectedColorFilter);
                    } else {
                        bitmapDrawable.setColorFilter(selectedColorFilter);
                    }
                } else if (shader != null) {
                    this.roundPaint.setColorFilter(selectedGroupColorFilter);
                } else {
                    bitmapDrawable.setColorFilter(selectedGroupColorFilter);
                }
            }
            if (this.colorFilter != null) {
                if (shader != null) {
                    this.roundPaint.setColorFilter(this.colorFilter);
                } else {
                    bitmapDrawable.setColorFilter(this.colorFilter);
                }
            }
            if (bitmapDrawable instanceof AnimatedFileDrawable) {
                if (o % 360 == 90 || o % 360 == 270) {
                    bitmapW = bitmapDrawable.getIntrinsicHeight();
                    bitmapH = bitmapDrawable.getIntrinsicWidth();
                } else {
                    bitmapW = bitmapDrawable.getIntrinsicWidth();
                    bitmapH = bitmapDrawable.getIntrinsicHeight();
                }
            } else if (o % 360 == 90 || o % 360 == 270) {
                bitmapW = bitmapDrawable.getBitmap().getHeight();
                bitmapH = bitmapDrawable.getBitmap().getWidth();
            } else {
                bitmapW = bitmapDrawable.getBitmap().getWidth();
                bitmapH = bitmapDrawable.getBitmap().getHeight();
            }
            float scaleW = ((float) bitmapW) / ((float) this.imageW);
            float scaleH = ((float) bitmapH) / ((float) this.imageH);
            float scale;
            int width;
            int height;
            int centerX;
            int centerY;
            if (shader != null) {
                this.roundPaint.setShader(shader);
                scale = Math.min(scaleW, scaleH);
                this.roundRect.set((float) this.imageX, (float) this.imageY, (float) (this.imageX + this.imageW), (float) (this.imageY + this.imageH));
                this.shaderMatrix.reset();
                if (Math.abs(scaleW - scaleH) <= 1.0E-5f) {
                    this.drawRegion.set(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                } else if (((float) bitmapW) / scaleH > ((float) this.imageW)) {
                    this.drawRegion.set(this.imageX - ((((int) (((float) bitmapW) / scaleH)) - this.imageW) / 2), this.imageY, this.imageX + ((((int) (((float) bitmapW) / scaleH)) + this.imageW) / 2), this.imageY + this.imageH);
                } else {
                    this.drawRegion.set(this.imageX, this.imageY - ((((int) (((float) bitmapH) / scaleW)) - this.imageH) / 2), this.imageX + this.imageW, this.imageY + ((((int) (((float) bitmapH) / scaleW)) + this.imageH) / 2));
                }
                if (this.isVisible) {
                    if (Math.abs(scaleW - scaleH) > 1.0E-5f) {
                        int w = (int) Math.floor((double) (((float) this.imageW) * scale));
                        int h = (int) Math.floor((double) (((float) this.imageH) * scale));
                        this.bitmapRect.set((float) ((bitmapW - w) / 2), (float) ((bitmapH - h) / 2), (float) ((bitmapW + w) / 2), (float) ((bitmapH + h) / 2));
                        this.shaderMatrix.setRectToRect(this.bitmapRect, this.roundRect, ScaleToFit.START);
                    } else {
                        this.bitmapRect.set(0.0f, 0.0f, (float) bitmapW, (float) bitmapH);
                        this.shaderMatrix.setRectToRect(this.bitmapRect, this.roundRect, ScaleToFit.FILL);
                    }
                    shader.setLocalMatrix(this.shaderMatrix);
                    this.roundPaint.setAlpha(alpha);
                    canvas.drawRoundRect(this.roundRect, (float) this.roundRadius, (float) this.roundRadius, this.roundPaint);
                    return;
                }
                return;
            } else if (this.isAspectFit) {
                scale = Math.max(scaleW, scaleH);
                canvas.save();
                bitmapW = (int) (((float) bitmapW) / scale);
                bitmapH = (int) (((float) bitmapH) / scale);
                this.drawRegion.set(this.imageX + ((this.imageW - bitmapW) / 2), this.imageY + ((this.imageH - bitmapH) / 2), this.imageX + ((this.imageW + bitmapW) / 2), this.imageY + ((this.imageH + bitmapH) / 2));
                bitmapDrawable.setBounds(this.drawRegion);
                try {
                    bitmapDrawable.setAlpha(alpha);
                    bitmapDrawable.draw(canvas);
                } catch (Throwable e) {
                    if (bitmapDrawable == this.currentImage && this.currentKey != null) {
                        ImageLoader.getInstance().removeImage(this.currentKey);
                        this.currentKey = null;
                    } else if (bitmapDrawable == this.currentThumb && this.currentThumbKey != null) {
                        ImageLoader.getInstance().removeImage(this.currentThumbKey);
                        this.currentThumbKey = null;
                    }
                    setImage(this.currentImageLocation, this.currentHttpUrl, this.currentFilter, this.currentThumb, this.currentThumbLocation, this.currentThumbFilter, this.currentSize, this.currentExt, this.currentParentObject, this.currentCacheType);
                    FileLog.e(e);
                }
                canvas.restore();
                return;
            } else if (Math.abs(scaleW - scaleH) > 1.0E-5f) {
                canvas.save();
                canvas.clipRect(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                if (o % 360 != 0) {
                    if (this.centerRotation) {
                        canvas.rotate((float) o, (float) (this.imageW / 2), (float) (this.imageH / 2));
                    } else {
                        canvas.rotate((float) o, 0.0f, 0.0f);
                    }
                }
                if (((float) bitmapW) / scaleH > ((float) this.imageW)) {
                    bitmapW = (int) (((float) bitmapW) / scaleH);
                    this.drawRegion.set(this.imageX - ((bitmapW - this.imageW) / 2), this.imageY, this.imageX + ((this.imageW + bitmapW) / 2), this.imageY + this.imageH);
                } else {
                    bitmapH = (int) (((float) bitmapH) / scaleW);
                    this.drawRegion.set(this.imageX, this.imageY - ((bitmapH - this.imageH) / 2), this.imageX + this.imageW, this.imageY + ((this.imageH + bitmapH) / 2));
                }
                if (bitmapDrawable instanceof AnimatedFileDrawable) {
                    ((AnimatedFileDrawable) bitmapDrawable).setActualDrawRect(this.imageX, this.imageY, this.imageW, this.imageH);
                }
                if (o % 360 == 90 || o % 360 == 270) {
                    width = (this.drawRegion.right - this.drawRegion.left) / 2;
                    height = (this.drawRegion.bottom - this.drawRegion.top) / 2;
                    centerX = (this.drawRegion.right + this.drawRegion.left) / 2;
                    centerY = (this.drawRegion.top + this.drawRegion.bottom) / 2;
                    bitmapDrawable.setBounds(centerX - height, centerY - width, centerX + height, centerY + width);
                } else {
                    bitmapDrawable.setBounds(this.drawRegion);
                }
                if (this.isVisible) {
                    try {
                        bitmapDrawable.setAlpha(alpha);
                        bitmapDrawable.draw(canvas);
                    } catch (Throwable e2) {
                        if (bitmapDrawable == this.currentImage && this.currentKey != null) {
                            ImageLoader.getInstance().removeImage(this.currentKey);
                            this.currentKey = null;
                        } else if (bitmapDrawable == this.currentThumb && this.currentThumbKey != null) {
                            ImageLoader.getInstance().removeImage(this.currentThumbKey);
                            this.currentThumbKey = null;
                        }
                        setImage(this.currentImageLocation, this.currentHttpUrl, this.currentFilter, this.currentThumb, this.currentThumbLocation, this.currentThumbFilter, this.currentSize, this.currentExt, this.currentParentObject, this.currentCacheType);
                        FileLog.e(e2);
                    }
                }
                canvas.restore();
                return;
            } else {
                canvas.save();
                if (o % 360 != 0) {
                    if (this.centerRotation) {
                        canvas.rotate((float) o, (float) (this.imageW / 2), (float) (this.imageH / 2));
                    } else {
                        canvas.rotate((float) o, 0.0f, 0.0f);
                    }
                }
                this.drawRegion.set(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                if (bitmapDrawable instanceof AnimatedFileDrawable) {
                    ((AnimatedFileDrawable) bitmapDrawable).setActualDrawRect(this.imageX, this.imageY, this.imageW, this.imageH);
                }
                if (o % 360 == 90 || o % 360 == 270) {
                    width = (this.drawRegion.right - this.drawRegion.left) / 2;
                    height = (this.drawRegion.bottom - this.drawRegion.top) / 2;
                    centerX = (this.drawRegion.right + this.drawRegion.left) / 2;
                    centerY = (this.drawRegion.top + this.drawRegion.bottom) / 2;
                    bitmapDrawable.setBounds(centerX - height, centerY - width, centerX + height, centerY + width);
                } else {
                    bitmapDrawable.setBounds(this.drawRegion);
                }
                if (this.isVisible) {
                    try {
                        bitmapDrawable.setAlpha(alpha);
                        bitmapDrawable.draw(canvas);
                    } catch (Throwable e22) {
                        if (bitmapDrawable == this.currentImage && this.currentKey != null) {
                            ImageLoader.getInstance().removeImage(this.currentKey);
                            this.currentKey = null;
                        } else if (bitmapDrawable == this.currentThumb && this.currentThumbKey != null) {
                            ImageLoader.getInstance().removeImage(this.currentThumbKey);
                            this.currentThumbKey = null;
                        }
                        setImage(this.currentImageLocation, this.currentHttpUrl, this.currentFilter, this.currentThumb, this.currentThumbLocation, this.currentThumbFilter, this.currentSize, this.currentExt, this.currentParentObject, this.currentCacheType);
                        FileLog.e(e22);
                    }
                }
                canvas.restore();
                return;
            }
        }
        this.drawRegion.set(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
        drawable.setBounds(this.drawRegion);
        if (this.isVisible) {
            try {
                drawable.setAlpha(alpha);
                drawable.draw(canvas);
            } catch (Throwable e222) {
                FileLog.e(e222);
            }
        }
    }

    private void checkAlphaAnimation(boolean skip) {
        if (!this.manualAlphaAnimator && this.currentAlpha != 1.0f) {
            if (!skip) {
                long dt = System.currentTimeMillis() - this.lastUpdateAlphaTime;
                if (dt > 18) {
                    dt = 18;
                }
                this.currentAlpha += ((float) dt) / 150.0f;
                if (this.currentAlpha > 1.0f) {
                    this.currentAlpha = 1.0f;
                    if (this.crossfadeImage != null) {
                        recycleBitmap(null, 2);
                        this.crossfadeShader = null;
                    }
                }
            }
            this.lastUpdateAlphaTime = System.currentTimeMillis();
            if (this.parentView == null) {
                return;
            }
            if (this.invalidateAll) {
                this.parentView.invalidate();
            } else {
                this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
            }
        }
    }

    public boolean draw(Canvas canvas) {
        Drawable drawable = null;
        try {
            boolean animationNotReady = (this.currentImage instanceof AnimatedFileDrawable) && !((AnimatedFileDrawable) this.currentImage).hasBitmap();
            boolean isThumb = false;
            BitmapShader customShader = null;
            if (!this.forcePreview && this.currentImage != null && !animationNotReady) {
                drawable = this.currentImage;
            } else if (this.crossfadeImage != null && !this.crossfadingWithThumb) {
                drawable = this.crossfadeImage;
                customShader = this.crossfadeShader;
            } else if (this.staticThumb instanceof BitmapDrawable) {
                drawable = this.staticThumb;
                isThumb = true;
            } else if (this.currentThumb != null) {
                drawable = this.currentThumb;
                isThumb = true;
            }
            if (drawable != null) {
                int i;
                BitmapShader bitmapShader;
                if (this.crossfadeAlpha == (byte) 0) {
                    i = (int) (this.overrideAlpha * 255.0f);
                    bitmapShader = customShader != null ? customShader : isThumb ? this.bitmapShaderThumb : this.bitmapShader;
                    drawDrawable(canvas, drawable, i, bitmapShader, isThumb);
                } else if (this.crossfadeWithThumb && animationNotReady) {
                    drawDrawable(canvas, drawable, (int) (this.overrideAlpha * 255.0f), this.bitmapShaderThumb, isThumb);
                } else {
                    if (this.crossfadeWithThumb && this.currentAlpha != 1.0f) {
                        Drawable thumbDrawable = null;
                        BitmapShader customThumbShader = null;
                        if (drawable == this.currentImage) {
                            if (this.crossfadeImage != null) {
                                thumbDrawable = this.crossfadeImage;
                                customThumbShader = this.crossfadeShader;
                            } else if (this.staticThumb != null) {
                                thumbDrawable = this.staticThumb;
                            } else if (this.currentThumb != null) {
                                thumbDrawable = this.currentThumb;
                            }
                        } else if (drawable == this.currentThumb || drawable == this.crossfadeImage) {
                            if (this.staticThumb != null) {
                                thumbDrawable = this.staticThumb;
                            }
                        } else if (drawable == this.staticThumb && this.crossfadeImage != null) {
                            thumbDrawable = this.crossfadeImage;
                            customThumbShader = this.crossfadeShader;
                        }
                        if (thumbDrawable != null) {
                            drawDrawable(canvas, thumbDrawable, (int) (this.overrideAlpha * 255.0f), customThumbShader != null ? customThumbShader : this.bitmapShaderThumb, true);
                        }
                    }
                    i = (int) ((this.overrideAlpha * this.currentAlpha) * 255.0f);
                    bitmapShader = customShader != null ? customShader : isThumb ? this.bitmapShaderThumb : this.bitmapShader;
                    drawDrawable(canvas, drawable, i, bitmapShader, isThumb);
                }
                boolean z = animationNotReady && this.crossfadeWithThumb;
                checkAlphaAnimation(z);
                return true;
            } else if (this.staticThumb != null) {
                drawDrawable(canvas, this.staticThumb, (int) (this.overrideAlpha * 255.0f), null, true);
                checkAlphaAnimation(animationNotReady);
                return true;
            } else {
                checkAlphaAnimation(animationNotReady);
                return false;
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public void setManualAlphaAnimator(boolean value) {
        this.manualAlphaAnimator = value;
    }

    public float getCurrentAlpha() {
        return this.currentAlpha;
    }

    @Keep
    public void setCurrentAlpha(float value) {
        this.currentAlpha = value;
    }

    public Drawable getDrawable() {
        if (this.currentImage != null) {
            return this.currentImage;
        }
        if (this.currentThumb != null) {
            return this.currentThumb;
        }
        if (this.staticThumb != null) {
            return this.staticThumb;
        }
        return null;
    }

    public Bitmap getBitmap() {
        if (this.currentImage instanceof AnimatedFileDrawable) {
            return ((AnimatedFileDrawable) this.currentImage).getAnimatedBitmap();
        }
        if (this.staticThumb instanceof AnimatedFileDrawable) {
            return ((AnimatedFileDrawable) this.staticThumb).getAnimatedBitmap();
        }
        if (this.currentImage instanceof BitmapDrawable) {
            return ((BitmapDrawable) this.currentImage).getBitmap();
        }
        if (this.currentThumb instanceof BitmapDrawable) {
            return ((BitmapDrawable) this.currentThumb).getBitmap();
        }
        if (this.staticThumb instanceof BitmapDrawable) {
            return ((BitmapDrawable) this.staticThumb).getBitmap();
        }
        return null;
    }

    public BitmapHolder getBitmapSafe() {
        Bitmap bitmap = null;
        String key = null;
        if (this.currentImage instanceof AnimatedFileDrawable) {
            bitmap = ((AnimatedFileDrawable) this.currentImage).getAnimatedBitmap();
        } else if (this.staticThumb instanceof AnimatedFileDrawable) {
            bitmap = ((AnimatedFileDrawable) this.staticThumb).getAnimatedBitmap();
        } else if (this.currentImage instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) this.currentImage).getBitmap();
            key = this.currentKey;
        } else if (this.currentThumb instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) this.currentThumb).getBitmap();
            key = this.currentThumbKey;
        } else if (this.staticThumb instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) this.staticThumb).getBitmap();
        }
        if (bitmap != null) {
            return new BitmapHolder(bitmap, key);
        }
        return null;
    }

    public Bitmap getThumbBitmap() {
        if (this.currentThumb instanceof BitmapDrawable) {
            return ((BitmapDrawable) this.currentThumb).getBitmap();
        }
        if (this.staticThumb instanceof BitmapDrawable) {
            return ((BitmapDrawable) this.staticThumb).getBitmap();
        }
        return null;
    }

    public BitmapHolder getThumbBitmapSafe() {
        Bitmap bitmap = null;
        String key = null;
        if (this.currentThumb instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) this.currentThumb).getBitmap();
            key = this.currentThumbKey;
        } else if (this.staticThumb instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) this.staticThumb).getBitmap();
        }
        if (bitmap != null) {
            return new BitmapHolder(bitmap, key);
        }
        return null;
    }

    public int getBitmapWidth() {
        if (this.currentImage instanceof AnimatedFileDrawable) {
            if (this.orientation % 360 == 0 || this.orientation % 360 == 180) {
                return this.currentImage.getIntrinsicWidth();
            }
            return this.currentImage.getIntrinsicHeight();
        } else if (this.staticThumb instanceof AnimatedFileDrawable) {
            return (this.orientation % 360 == 0 || this.orientation % 360 == 180) ? this.staticThumb.getIntrinsicWidth() : this.staticThumb.getIntrinsicHeight();
        } else {
            Bitmap bitmap = getBitmap();
            if (bitmap != null) {
                return (this.orientation % 360 == 0 || this.orientation % 360 == 180) ? bitmap.getWidth() : bitmap.getHeight();
            } else {
                if (this.staticThumb != null) {
                    return this.staticThumb.getIntrinsicWidth();
                }
                return 1;
            }
        }
    }

    public int getBitmapHeight() {
        if (this.currentImage instanceof AnimatedFileDrawable) {
            if (this.orientation % 360 == 0 || this.orientation % 360 == 180) {
                return this.currentImage.getIntrinsicHeight();
            }
            return this.currentImage.getIntrinsicWidth();
        } else if (this.staticThumb instanceof AnimatedFileDrawable) {
            return (this.orientation % 360 == 0 || this.orientation % 360 == 180) ? this.staticThumb.getIntrinsicHeight() : this.staticThumb.getIntrinsicWidth();
        } else {
            Bitmap bitmap = getBitmap();
            if (bitmap != null) {
                return (this.orientation % 360 == 0 || this.orientation % 360 == 180) ? bitmap.getHeight() : bitmap.getWidth();
            } else {
                if (this.staticThumb != null) {
                    return this.staticThumb.getIntrinsicHeight();
                }
                return 1;
            }
        }
    }

    public void setVisible(boolean value, boolean invalidate) {
        if (this.isVisible != value) {
            this.isVisible = value;
            if (invalidate && this.parentView != null) {
                if (this.invalidateAll) {
                    this.parentView.invalidate();
                } else {
                    this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                }
            }
        }
    }

    public boolean getVisible() {
        return this.isVisible;
    }

    public void setAlpha(float value) {
        this.overrideAlpha = value;
    }

    public void setCrossfadeAlpha(byte value) {
        this.crossfadeAlpha = value;
    }

    public boolean hasImage() {
        return (this.currentImage == null && this.currentThumb == null && this.currentKey == null && this.currentHttpUrl == null && this.staticThumb == null) ? false : true;
    }

    public boolean hasBitmapImage() {
        return (this.currentImage == null && this.currentThumb == null && this.staticThumb == null) ? false : true;
    }

    public boolean hasNotThumb() {
        return this.currentImage != null;
    }

    public void setAspectFit(boolean value) {
        this.isAspectFit = value;
    }

    public void setParentView(View view) {
        this.parentView = view;
        if (this.currentImage instanceof AnimatedFileDrawable) {
            this.currentImage.setParentView(this.parentView);
        }
    }

    public void setImageX(int x) {
        this.imageX = x;
    }

    public void setImageY(int y) {
        this.imageY = y;
    }

    public void setImageWidth(int width) {
        this.imageW = width;
    }

    public void setImageCoords(int x, int y, int width, int height) {
        this.imageX = x;
        this.imageY = y;
        this.imageW = width;
        this.imageH = height;
    }

    public float getCenterX() {
        return ((float) this.imageX) + (((float) this.imageW) / 2.0f);
    }

    public float getCenterY() {
        return ((float) this.imageY) + (((float) this.imageH) / 2.0f);
    }

    public int getImageX() {
        return this.imageX;
    }

    public int getImageX2() {
        return this.imageX + this.imageW;
    }

    public int getImageY() {
        return this.imageY;
    }

    public int getImageY2() {
        return this.imageY + this.imageH;
    }

    public int getImageWidth() {
        return this.imageW;
    }

    public int getImageHeight() {
        return this.imageH;
    }

    public float getImageAspectRatio() {
        return this.orientation % 180 != 0 ? ((float) this.drawRegion.height()) / ((float) this.drawRegion.width()) : ((float) this.drawRegion.width()) / ((float) this.drawRegion.height());
    }

    public String getExt() {
        return this.currentExt;
    }

    public boolean isInsideImage(float x, float y) {
        return x >= ((float) this.imageX) && x <= ((float) (this.imageX + this.imageW)) && y >= ((float) this.imageY) && y <= ((float) (this.imageY + this.imageH));
    }

    public Rect getDrawRegion() {
        return this.drawRegion;
    }

    public String getFilter() {
        return this.currentFilter;
    }

    public String getThumbFilter() {
        return this.currentThumbFilter;
    }

    public String getKey() {
        return this.currentKey;
    }

    public String getThumbKey() {
        return this.currentThumbKey;
    }

    public int getSize() {
        return this.currentSize;
    }

    public TLObject getImageLocation() {
        return this.currentImageLocation;
    }

    public TLObject getThumbLocation() {
        return this.currentThumbLocation;
    }

    public String getHttpImageLocation() {
        return this.currentHttpUrl;
    }

    public int getCacheType() {
        return this.currentCacheType;
    }

    public void setForcePreview(boolean value) {
        this.forcePreview = value;
    }

    public void setForceCrossfade(boolean value) {
        this.forceCrossfade = value;
    }

    public boolean isForcePreview() {
        return this.forcePreview;
    }

    public void setRoundRadius(int value) {
        this.roundRadius = value;
    }

    public void setCurrentAccount(int value) {
        this.currentAccount = value;
    }

    public int getRoundRadius() {
        return this.roundRadius;
    }

    public Object getParentObject() {
        return this.currentParentObject;
    }

    public void setNeedsQualityThumb(boolean value) {
        this.needsQualityThumb = value;
    }

    public void setCrossfadeWithOldImage(boolean value) {
        this.crossfadeWithOldImage = value;
    }

    public boolean isNeedsQualityThumb() {
        return this.needsQualityThumb;
    }

    public boolean isCurrentKeyQuality() {
        return this.currentKeyQuality;
    }

    public int getcurrentAccount() {
        return this.currentAccount;
    }

    public void setShouldGenerateQualityThumb(boolean value) {
        this.shouldGenerateQualityThumb = value;
    }

    public boolean isShouldGenerateQualityThumb() {
        return this.shouldGenerateQualityThumb;
    }

    public void setAllowStartAnimation(boolean value) {
        this.allowStartAnimation = value;
    }

    public void setAllowDecodeSingleFrame(boolean value) {
        this.allowDecodeSingleFrame = value;
    }

    public boolean isAllowStartAnimation() {
        return this.allowStartAnimation;
    }

    public void startAnimation() {
        if (this.currentImage instanceof AnimatedFileDrawable) {
            ((AnimatedFileDrawable) this.currentImage).start();
        }
    }

    public void stopAnimation() {
        if (this.currentImage instanceof AnimatedFileDrawable) {
            ((AnimatedFileDrawable) this.currentImage).stop();
        }
    }

    public boolean isAnimationRunning() {
        return (this.currentImage instanceof AnimatedFileDrawable) && ((AnimatedFileDrawable) this.currentImage).isRunning();
    }

    public AnimatedFileDrawable getAnimation() {
        return this.currentImage instanceof AnimatedFileDrawable ? (AnimatedFileDrawable) this.currentImage : null;
    }

    protected int getTag(boolean thumb) {
        if (thumb) {
            return this.thumbTag;
        }
        return this.tag;
    }

    protected void setTag(int value, boolean thumb) {
        if (thumb) {
            this.thumbTag = value;
        } else {
            this.tag = value;
        }
    }

    public void setParam(int value) {
        this.param = value;
    }

    public int getParam() {
        return this.param;
    }

    protected boolean setImageBitmapByKey(BitmapDrawable bitmap, String key, boolean thumb, boolean memCache) {
        boolean z = false;
        if (bitmap == null || key == null) {
            return false;
        }
        boolean z2;
        if (thumb) {
            if (this.currentThumb == null && (this.currentImage == null || (((this.currentImage instanceof AnimatedFileDrawable) && !((AnimatedFileDrawable) this.currentImage).hasBitmap()) || this.forcePreview))) {
                if (!key.equals(this.currentThumbKey)) {
                    return false;
                }
                ImageLoader.getInstance().incrementUseCount(this.currentThumbKey);
                this.currentThumb = bitmap;
                if (bitmap instanceof ExtendedBitmapDrawable) {
                    this.thumbOrientation = ((ExtendedBitmapDrawable) bitmap).getOrientation();
                }
                if (this.roundRadius == 0 || !(bitmap instanceof BitmapDrawable)) {
                    this.bitmapShaderThumb = null;
                } else if (bitmap instanceof AnimatedFileDrawable) {
                    ((AnimatedFileDrawable) bitmap).setRoundRadius(this.roundRadius);
                } else {
                    this.bitmapShaderThumb = new BitmapShader(bitmap.getBitmap(), TileMode.CLAMP, TileMode.CLAMP);
                }
                if (memCache || this.crossfadeAlpha == (byte) 2) {
                    this.currentAlpha = 1.0f;
                } else if ((this.currentParentObject instanceof MessageObject) && ((MessageObject) this.currentParentObject).isRoundVideo() && ((MessageObject) this.currentParentObject).isSending()) {
                    this.currentAlpha = 1.0f;
                } else {
                    this.currentAlpha = 0.0f;
                    this.lastUpdateAlphaTime = System.currentTimeMillis();
                    z2 = this.staticThumb != null && this.currentKey == null;
                    this.crossfadeWithThumb = z2;
                }
                if (!((this.staticThumb instanceof BitmapDrawable) || this.parentView == null)) {
                    if (this.invalidateAll) {
                        this.parentView.invalidate();
                    } else {
                        this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                    }
                }
            }
        } else if (!key.equals(this.currentKey)) {
            return false;
        } else {
            if (!(bitmap instanceof AnimatedFileDrawable)) {
                ImageLoader.getInstance().incrementUseCount(this.currentKey);
            }
            this.currentImage = bitmap;
            if (bitmap instanceof ExtendedBitmapDrawable) {
                this.orientation = ((ExtendedBitmapDrawable) bitmap).getOrientation();
            }
            if (this.roundRadius == 0 || !(bitmap instanceof BitmapDrawable)) {
                this.bitmapShader = null;
            } else if (bitmap instanceof AnimatedFileDrawable) {
                ((AnimatedFileDrawable) bitmap).setRoundRadius(this.roundRadius);
            } else {
                this.bitmapShader = new BitmapShader(bitmap.getBitmap(), TileMode.CLAMP, TileMode.CLAMP);
            }
            if ((memCache || this.forcePreview) && !this.forceCrossfade) {
                this.currentAlpha = 1.0f;
            } else if ((this.currentThumb == null && this.staticThumb == null) || this.currentAlpha == 1.0f || this.forceCrossfade) {
                this.currentAlpha = 0.0f;
                this.lastUpdateAlphaTime = System.currentTimeMillis();
                if (this.crossfadeImage == null && this.currentThumb == null && this.staticThumb == null) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                this.crossfadeWithThumb = z2;
            }
            if (bitmap instanceof AnimatedFileDrawable) {
                AnimatedFileDrawable fileDrawable = (AnimatedFileDrawable) bitmap;
                fileDrawable.setParentView(this.parentView);
                if (this.allowStartAnimation) {
                    fileDrawable.start();
                } else {
                    fileDrawable.setAllowDecodeSingleFrame(this.allowDecodeSingleFrame);
                }
            }
            if (this.parentView != null) {
                if (this.invalidateAll) {
                    this.parentView.invalidate();
                } else {
                    this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                }
            }
        }
        if (this.delegate != null) {
            ImageReceiverDelegate imageReceiverDelegate = this.delegate;
            if (this.currentImage == null && this.currentThumb == null && this.staticThumb == null) {
                z2 = false;
            } else {
                z2 = true;
            }
            if (this.currentImage == null) {
                z = true;
            }
            imageReceiverDelegate.didSetImage(this, z2, z);
        }
        return true;
    }

    private void recycleBitmap(String newKey, int type) {
        String key;
        Drawable image;
        String replacedKey;
        if (type == 2) {
            key = this.crossfadeKey;
            image = this.crossfadeImage;
        } else if (type == 1) {
            key = this.currentThumbKey;
            image = this.currentThumb;
        } else {
            key = this.currentKey;
            image = this.currentImage;
        }
        if (key != null && key.startsWith("-")) {
            replacedKey = ImageLoader.getInstance().getReplacedKey(key);
            if (replacedKey != null) {
                key = replacedKey;
            }
        }
        replacedKey = ImageLoader.getInstance().getReplacedKey(key);
        if (key != null && ((newKey == null || !newKey.equals(key)) && image != null)) {
            if (image instanceof AnimatedFileDrawable) {
                ((AnimatedFileDrawable) image).recycle();
            } else if (image instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
                boolean canDelete = ImageLoader.getInstance().decrementUseCount(key);
                if (!ImageLoader.getInstance().isInCache(key) && canDelete) {
                    bitmap.recycle();
                }
            }
        }
        if (type == 2) {
            this.crossfadeKey = null;
            this.crossfadeImage = null;
        } else if (type == 1) {
            this.currentThumb = null;
            this.currentThumbKey = null;
        } else {
            this.currentImage = null;
            this.currentKey = null;
        }
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.didReplacedPhotoInMemCache) {
            String oldKey = args[0];
            if (this.currentKey != null && this.currentKey.equals(oldKey)) {
                this.currentKey = (String) args[1];
                this.currentImageLocation = (TLObject) args[2];
            }
            if (this.currentThumbKey != null && this.currentThumbKey.equals(oldKey)) {
                this.currentThumbKey = (String) args[1];
                this.currentThumbLocation = (TLObject) args[2];
            }
            if (this.setImageBackup != null) {
                if (this.currentKey != null && this.currentKey.equals(oldKey)) {
                    this.currentKey = (String) args[1];
                    this.currentImageLocation = (TLObject) args[2];
                }
                if (this.currentThumbKey != null && this.currentThumbKey.equals(oldKey)) {
                    this.currentThumbKey = (String) args[1];
                    this.currentThumbLocation = (TLObject) args[2];
                }
            }
        }
    }
}
