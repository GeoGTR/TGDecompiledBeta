package org.telegram.messenger.camera;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import java.util.ArrayList;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

public class CameraSession {
    public static final int ORIENTATION_HYSTERESIS = 5;
    private AutoFocusCallback autoFocusCallback = -$$Lambda$CameraSession$XMt9_OOlnCLbx3LVWHZ1eqpLkD4.INSTANCE;
    protected CameraInfo cameraInfo;
    private String currentFlashMode;
    private int currentOrientation;
    private float currentZoom;
    private int diffOrientation;
    private boolean flipFront = true;
    private boolean initied;
    private boolean isVideo;
    private int jpegOrientation;
    private int lastDisplayOrientation = -1;
    private int lastOrientation = -1;
    private int maxZoom;
    private boolean meteringAreaSupported;
    private boolean optimizeForBarcode;
    private OrientationEventListener orientationEventListener;
    private final int pictureFormat;
    private final Size pictureSize;
    private final Size previewSize;
    private boolean sameTakePictureOrientation;

    static /* synthetic */ void lambda$new$0(boolean z, Camera camera) {
    }

    public CameraSession(CameraInfo cameraInfo, Size size, Size size2, int i) {
        this.previewSize = size;
        this.pictureSize = size2;
        this.pictureFormat = i;
        this.cameraInfo = cameraInfo;
        this.currentFlashMode = ApplicationLoader.applicationContext.getSharedPreferences("camera", 0).getString(this.cameraInfo.frontCamera != 0 ? "flashMode_front" : "flashMode", "off");
        this.orientationEventListener = new OrientationEventListener(ApplicationLoader.applicationContext) {
            public void onOrientationChanged(int i) {
                if (CameraSession.this.orientationEventListener != null && CameraSession.this.initied && i != -1) {
                    CameraSession cameraSession = CameraSession.this;
                    cameraSession.jpegOrientation = cameraSession.roundOrientation(i, cameraSession.jpegOrientation);
                    i = ((WindowManager) ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
                    if (CameraSession.this.lastOrientation != CameraSession.this.jpegOrientation || i != CameraSession.this.lastDisplayOrientation) {
                        if (!CameraSession.this.isVideo) {
                            CameraSession.this.configurePhotoCamera();
                        }
                        CameraSession.this.lastDisplayOrientation = i;
                        CameraSession cameraSession2 = CameraSession.this;
                        cameraSession2.lastOrientation = cameraSession2.jpegOrientation;
                    }
                }
            }
        };
        if (this.orientationEventListener.canDetectOrientation()) {
            this.orientationEventListener.enable();
            return;
        }
        this.orientationEventListener.disable();
        this.orientationEventListener = null;
    }

    private int roundOrientation(int i, int i2) {
        Object obj = 1;
        if (i2 != -1) {
            int abs = Math.abs(i - i2);
            if (Math.min(abs, 360 - abs) < 50) {
                obj = null;
            }
        }
        return obj != null ? (((i + 45) / 90) * 90) % 360 : i2;
    }

    public void setOptimizeForBarcode(boolean z) {
        this.optimizeForBarcode = z;
        configurePhotoCamera();
    }

    public void checkFlashMode(String str) {
        if (!CameraController.getInstance().availableFlashModes.contains(this.currentFlashMode)) {
            this.currentFlashMode = str;
            configurePhotoCamera();
            ApplicationLoader.applicationContext.getSharedPreferences("camera", 0).edit().putString(this.cameraInfo.frontCamera != 0 ? "flashMode_front" : "flashMode", str).commit();
        }
    }

    public void setCurrentFlashMode(String str) {
        this.currentFlashMode = str;
        configurePhotoCamera();
        ApplicationLoader.applicationContext.getSharedPreferences("camera", 0).edit().putString(this.cameraInfo.frontCamera != 0 ? "flashMode_front" : "flashMode", str).commit();
    }

    public void setTorchEnabled(boolean z) {
        String str;
        if (z) {
            try {
                str = "torch";
            } catch (Exception e) {
                FileLog.e(e);
                return;
            }
        }
        str = "off";
        this.currentFlashMode = str;
        configurePhotoCamera();
    }

    public String getCurrentFlashMode() {
        return this.currentFlashMode;
    }

    public String getNextFlashMode() {
        ArrayList arrayList = CameraController.getInstance().availableFlashModes;
        int i = 0;
        while (i < arrayList.size()) {
            if (!((String) arrayList.get(i)).equals(this.currentFlashMode)) {
                i++;
            } else if (i < arrayList.size() - 1) {
                return (String) arrayList.get(i + 1);
            } else {
                return (String) arrayList.get(0);
            }
        }
        return this.currentFlashMode;
    }

    public void setInitied() {
        this.initied = true;
    }

    public boolean isInitied() {
        return this.initied;
    }

    public int getCurrentOrientation() {
        return this.currentOrientation;
    }

    public boolean isFlipFront() {
        return this.flipFront;
    }

    public void setFlipFront(boolean z) {
        this.flipFront = z;
    }

    public int getWorldAngle() {
        return this.diffOrientation;
    }

    public boolean isSameTakePictureOrientation() {
        return this.sameTakePictureOrientation;
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0057 A:{Catch:{ Exception -> 0x0014, all -> 0x0158 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0067 A:{Catch:{ Exception -> 0x0014, all -> 0x0158 }} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x005d A:{Catch:{ Exception -> 0x0014, all -> 0x0158 }} */
    /* JADX WARNING: Removed duplicated region for block: B:80:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0155 A:{Catch:{ Exception -> 0x0014, all -> 0x0158 }} */
    /* JADX WARNING: Missing exception handler attribute for start block: B:71:0x014f */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0155 A:{Catch:{ Exception -> 0x0014, all -> 0x0158 }} */
    /* JADX WARNING: Removed duplicated region for block: B:80:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Missing exception handler attribute for start block: B:67:0x0147 */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* JADX WARNING: Can't wrap try/catch for region: R(19:36|37|(2:39|40)|41|(1:43)|44|(1:46)(2:47|(1:49))|50|(2:52|(1:54)(1:55))(1:56)|57|58|(3:60|(1:62)|63)(2:(1:65)|66)|67|68|69|70|71|72|(2:74|81)(1:80)) */
    public void configureRoundCamera() {
        /*
        r9 = this;
        r0 = 1;
        r9.isVideo = r0;	 Catch:{ all -> 0x0158 }
        r1 = r9.cameraInfo;	 Catch:{ all -> 0x0158 }
        r1 = r1.camera;	 Catch:{ all -> 0x0158 }
        if (r1 == 0) goto L_0x015c;
    L_0x0009:
        r2 = new android.hardware.Camera$CameraInfo;	 Catch:{ all -> 0x0158 }
        r2.<init>();	 Catch:{ all -> 0x0158 }
        r3 = 0;
        r3 = r1.getParameters();	 Catch:{ Exception -> 0x0014 }
        goto L_0x0018;
    L_0x0014:
        r4 = move-exception;
        org.telegram.messenger.FileLog.e(r4);	 Catch:{ all -> 0x0158 }
    L_0x0018:
        r4 = r9.cameraInfo;	 Catch:{ all -> 0x0158 }
        r4 = r4.getCameraId();	 Catch:{ all -> 0x0158 }
        android.hardware.Camera.getCameraInfo(r4, r2);	 Catch:{ all -> 0x0158 }
        r4 = r9.getDisplayOrientation(r2, r0);	 Catch:{ all -> 0x0158 }
        r5 = "samsung";
        r6 = android.os.Build.MANUFACTURER;	 Catch:{ all -> 0x0158 }
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0158 }
        r6 = 0;
        if (r5 == 0) goto L_0x003c;
    L_0x0030:
        r5 = "sf2wifixx";
        r7 = android.os.Build.PRODUCT;	 Catch:{ all -> 0x0158 }
        r5 = r5.equals(r7);	 Catch:{ all -> 0x0158 }
        if (r5 == 0) goto L_0x003c;
    L_0x003a:
        r5 = 0;
        goto L_0x006e;
    L_0x003c:
        r5 = 90;
        if (r4 == 0) goto L_0x0048;
    L_0x0040:
        if (r4 == r0) goto L_0x0050;
    L_0x0042:
        r7 = 2;
        if (r4 == r7) goto L_0x004d;
    L_0x0045:
        r7 = 3;
        if (r4 == r7) goto L_0x004a;
    L_0x0048:
        r7 = 0;
        goto L_0x0052;
    L_0x004a:
        r7 = 270; // 0x10e float:3.78E-43 double:1.334E-321;
        goto L_0x0052;
    L_0x004d:
        r7 = 180; // 0xb4 float:2.52E-43 double:8.9E-322;
        goto L_0x0052;
    L_0x0050:
        r7 = 90;
    L_0x0052:
        r8 = r2.orientation;	 Catch:{ all -> 0x0158 }
        r8 = r8 % r5;
        if (r8 == 0) goto L_0x0059;
    L_0x0057:
        r2.orientation = r6;	 Catch:{ all -> 0x0158 }
    L_0x0059:
        r5 = r2.facing;	 Catch:{ all -> 0x0158 }
        if (r5 != r0) goto L_0x0067;
    L_0x005d:
        r5 = r2.orientation;	 Catch:{ all -> 0x0158 }
        r5 = r5 + r7;
        r5 = r5 % 360;
        r5 = 360 - r5;
        r5 = r5 % 360;
        goto L_0x006e;
    L_0x0067:
        r5 = r2.orientation;	 Catch:{ all -> 0x0158 }
        r5 = r5 - r7;
        r5 = r5 + 360;
        r5 = r5 % 360;
    L_0x006e:
        r9.currentOrientation = r5;	 Catch:{ all -> 0x0158 }
        r1.setDisplayOrientation(r5);	 Catch:{ all -> 0x0158 }
        r5 = r9.currentOrientation;	 Catch:{ all -> 0x0158 }
        r5 = r5 - r4;
        r9.diffOrientation = r5;	 Catch:{ all -> 0x0158 }
        if (r3 == 0) goto L_0x015c;
    L_0x007a:
        r5 = org.telegram.messenger.BuildVars.LOGS_ENABLED;	 Catch:{ all -> 0x0158 }
        r7 = " ";
        if (r5 == 0) goto L_0x00a6;
    L_0x0080:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0158 }
        r5.<init>();	 Catch:{ all -> 0x0158 }
        r8 = "set preview size = ";
        r5.append(r8);	 Catch:{ all -> 0x0158 }
        r8 = r9.previewSize;	 Catch:{ all -> 0x0158 }
        r8 = r8.getWidth();	 Catch:{ all -> 0x0158 }
        r5.append(r8);	 Catch:{ all -> 0x0158 }
        r5.append(r7);	 Catch:{ all -> 0x0158 }
        r8 = r9.previewSize;	 Catch:{ all -> 0x0158 }
        r8 = r8.getHeight();	 Catch:{ all -> 0x0158 }
        r5.append(r8);	 Catch:{ all -> 0x0158 }
        r5 = r5.toString();	 Catch:{ all -> 0x0158 }
        org.telegram.messenger.FileLog.d(r5);	 Catch:{ all -> 0x0158 }
    L_0x00a6:
        r5 = r9.previewSize;	 Catch:{ all -> 0x0158 }
        r5 = r5.getWidth();	 Catch:{ all -> 0x0158 }
        r8 = r9.previewSize;	 Catch:{ all -> 0x0158 }
        r8 = r8.getHeight();	 Catch:{ all -> 0x0158 }
        r3.setPreviewSize(r5, r8);	 Catch:{ all -> 0x0158 }
        r5 = org.telegram.messenger.BuildVars.LOGS_ENABLED;	 Catch:{ all -> 0x0158 }
        if (r5 == 0) goto L_0x00df;
    L_0x00b9:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0158 }
        r5.<init>();	 Catch:{ all -> 0x0158 }
        r8 = "set picture size = ";
        r5.append(r8);	 Catch:{ all -> 0x0158 }
        r8 = r9.pictureSize;	 Catch:{ all -> 0x0158 }
        r8 = r8.getWidth();	 Catch:{ all -> 0x0158 }
        r5.append(r8);	 Catch:{ all -> 0x0158 }
        r5.append(r7);	 Catch:{ all -> 0x0158 }
        r7 = r9.pictureSize;	 Catch:{ all -> 0x0158 }
        r7 = r7.getHeight();	 Catch:{ all -> 0x0158 }
        r5.append(r7);	 Catch:{ all -> 0x0158 }
        r5 = r5.toString();	 Catch:{ all -> 0x0158 }
        org.telegram.messenger.FileLog.d(r5);	 Catch:{ all -> 0x0158 }
    L_0x00df:
        r5 = r9.pictureSize;	 Catch:{ all -> 0x0158 }
        r5 = r5.getWidth();	 Catch:{ all -> 0x0158 }
        r7 = r9.pictureSize;	 Catch:{ all -> 0x0158 }
        r7 = r7.getHeight();	 Catch:{ all -> 0x0158 }
        r3.setPictureSize(r5, r7);	 Catch:{ all -> 0x0158 }
        r5 = r9.pictureFormat;	 Catch:{ all -> 0x0158 }
        r3.setPictureFormat(r5);	 Catch:{ all -> 0x0158 }
        r3.setRecordingHint(r0);	 Catch:{ all -> 0x0158 }
        r5 = "continuous-video";
        r7 = r3.getSupportedFocusModes();	 Catch:{ all -> 0x0158 }
        r7 = r7.contains(r5);	 Catch:{ all -> 0x0158 }
        if (r7 == 0) goto L_0x0106;
    L_0x0102:
        r3.setFocusMode(r5);	 Catch:{ all -> 0x0158 }
        goto L_0x0115;
    L_0x0106:
        r5 = "auto";
        r7 = r3.getSupportedFocusModes();	 Catch:{ all -> 0x0158 }
        r7 = r7.contains(r5);	 Catch:{ all -> 0x0158 }
        if (r7 == 0) goto L_0x0115;
    L_0x0112:
        r3.setFocusMode(r5);	 Catch:{ all -> 0x0158 }
    L_0x0115:
        r5 = r9.jpegOrientation;	 Catch:{ all -> 0x0158 }
        r7 = -1;
        if (r5 == r7) goto L_0x0130;
    L_0x011a:
        r5 = r2.facing;	 Catch:{ all -> 0x0158 }
        if (r5 != r0) goto L_0x0128;
    L_0x011e:
        r5 = r2.orientation;	 Catch:{ all -> 0x0158 }
        r7 = r9.jpegOrientation;	 Catch:{ all -> 0x0158 }
        r5 = r5 - r7;
        r5 = r5 + 360;
        r5 = r5 % 360;
        goto L_0x0131;
    L_0x0128:
        r5 = r2.orientation;	 Catch:{ all -> 0x0158 }
        r7 = r9.jpegOrientation;	 Catch:{ all -> 0x0158 }
        r5 = r5 + r7;
        r5 = r5 % 360;
        goto L_0x0131;
    L_0x0130:
        r5 = 0;
    L_0x0131:
        r3.setRotation(r5);	 Catch:{ Exception -> 0x0147 }
        r2 = r2.facing;	 Catch:{ Exception -> 0x0147 }
        if (r2 != r0) goto L_0x0142;
    L_0x0138:
        r2 = 360 - r4;
        r2 = r2 % 360;
        if (r2 != r5) goto L_0x013f;
    L_0x013e:
        r6 = 1;
    L_0x013f:
        r9.sameTakePictureOrientation = r6;	 Catch:{ Exception -> 0x0147 }
        goto L_0x0147;
    L_0x0142:
        if (r4 != r5) goto L_0x0145;
    L_0x0144:
        r6 = 1;
    L_0x0145:
        r9.sameTakePictureOrientation = r6;	 Catch:{ Exception -> 0x0147 }
    L_0x0147:
        r2 = "off";
        r3.setFlashMode(r2);	 Catch:{ all -> 0x0158 }
        r1.setParameters(r3);	 Catch:{ Exception -> 0x014f }
    L_0x014f:
        r1 = r3.getMaxNumMeteringAreas();	 Catch:{ all -> 0x0158 }
        if (r1 <= 0) goto L_0x015c;
    L_0x0155:
        r9.meteringAreaSupported = r0;	 Catch:{ all -> 0x0158 }
        goto L_0x015c;
    L_0x0158:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x015c:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.camera.CameraSession.configureRoundCamera():void");
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0057 A:{Catch:{ Exception -> 0x0013, all -> 0x0124 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0067 A:{Catch:{ Exception -> 0x0013, all -> 0x0124 }} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x005d A:{Catch:{ Exception -> 0x0013, all -> 0x0124 }} */
    /* JADX WARNING: Removed duplicated region for block: B:77:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0121 A:{Catch:{ Exception -> 0x0013, all -> 0x0124 }} */
    /* JADX WARNING: Missing exception handler attribute for start block: B:68:0x011b */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0121 A:{Catch:{ Exception -> 0x0013, all -> 0x0124 }} */
    /* JADX WARNING: Removed duplicated region for block: B:77:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Missing exception handler attribute for start block: B:64:0x0113 */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* JADX WARNING: Can't wrap try/catch for region: R(14:36|(4:38|(1:40)|41|(1:43))(2:44|(1:46))|47|(2:49|(1:51)(1:52))(1:53)|54|55|(3:57|(1:59)|60)(2:(1:62)|63)|64|65|66|67|68|69|(2:71|78)(1:77)) */
    public void configurePhotoCamera() {
        /*
        r10 = this;
        r0 = "barcode";
        r1 = r10.cameraInfo;	 Catch:{ all -> 0x0124 }
        r1 = r1.camera;	 Catch:{ all -> 0x0124 }
        if (r1 == 0) goto L_0x0128;
    L_0x0008:
        r2 = new android.hardware.Camera$CameraInfo;	 Catch:{ all -> 0x0124 }
        r2.<init>();	 Catch:{ all -> 0x0124 }
        r3 = 0;
        r3 = r1.getParameters();	 Catch:{ Exception -> 0x0013 }
        goto L_0x0017;
    L_0x0013:
        r4 = move-exception;
        org.telegram.messenger.FileLog.e(r4);	 Catch:{ all -> 0x0124 }
    L_0x0017:
        r4 = r10.cameraInfo;	 Catch:{ all -> 0x0124 }
        r4 = r4.getCameraId();	 Catch:{ all -> 0x0124 }
        android.hardware.Camera.getCameraInfo(r4, r2);	 Catch:{ all -> 0x0124 }
        r4 = 1;
        r5 = r10.getDisplayOrientation(r2, r4);	 Catch:{ all -> 0x0124 }
        r6 = "samsung";
        r7 = android.os.Build.MANUFACTURER;	 Catch:{ all -> 0x0124 }
        r6 = r6.equals(r7);	 Catch:{ all -> 0x0124 }
        r7 = 0;
        if (r6 == 0) goto L_0x003c;
    L_0x0030:
        r6 = "sf2wifixx";
        r8 = android.os.Build.PRODUCT;	 Catch:{ all -> 0x0124 }
        r6 = r6.equals(r8);	 Catch:{ all -> 0x0124 }
        if (r6 == 0) goto L_0x003c;
    L_0x003a:
        r6 = 0;
        goto L_0x006e;
    L_0x003c:
        r6 = 90;
        if (r5 == 0) goto L_0x0048;
    L_0x0040:
        if (r5 == r4) goto L_0x0050;
    L_0x0042:
        r8 = 2;
        if (r5 == r8) goto L_0x004d;
    L_0x0045:
        r8 = 3;
        if (r5 == r8) goto L_0x004a;
    L_0x0048:
        r8 = 0;
        goto L_0x0052;
    L_0x004a:
        r8 = 270; // 0x10e float:3.78E-43 double:1.334E-321;
        goto L_0x0052;
    L_0x004d:
        r8 = 180; // 0xb4 float:2.52E-43 double:8.9E-322;
        goto L_0x0052;
    L_0x0050:
        r8 = 90;
    L_0x0052:
        r9 = r2.orientation;	 Catch:{ all -> 0x0124 }
        r9 = r9 % r6;
        if (r9 == 0) goto L_0x0059;
    L_0x0057:
        r2.orientation = r7;	 Catch:{ all -> 0x0124 }
    L_0x0059:
        r6 = r2.facing;	 Catch:{ all -> 0x0124 }
        if (r6 != r4) goto L_0x0067;
    L_0x005d:
        r6 = r2.orientation;	 Catch:{ all -> 0x0124 }
        r6 = r6 + r8;
        r6 = r6 % 360;
        r6 = 360 - r6;
        r6 = r6 % 360;
        goto L_0x006e;
    L_0x0067:
        r6 = r2.orientation;	 Catch:{ all -> 0x0124 }
        r6 = r6 - r8;
        r6 = r6 + 360;
        r6 = r6 % 360;
    L_0x006e:
        r10.currentOrientation = r6;	 Catch:{ all -> 0x0124 }
        r1.setDisplayOrientation(r6);	 Catch:{ all -> 0x0124 }
        if (r3 == 0) goto L_0x0128;
    L_0x0075:
        r6 = r10.previewSize;	 Catch:{ all -> 0x0124 }
        r6 = r6.getWidth();	 Catch:{ all -> 0x0124 }
        r8 = r10.previewSize;	 Catch:{ all -> 0x0124 }
        r8 = r8.getHeight();	 Catch:{ all -> 0x0124 }
        r3.setPreviewSize(r6, r8);	 Catch:{ all -> 0x0124 }
        r6 = r10.pictureSize;	 Catch:{ all -> 0x0124 }
        r6 = r6.getWidth();	 Catch:{ all -> 0x0124 }
        r8 = r10.pictureSize;	 Catch:{ all -> 0x0124 }
        r8 = r8.getHeight();	 Catch:{ all -> 0x0124 }
        r3.setPictureSize(r6, r8);	 Catch:{ all -> 0x0124 }
        r6 = r10.pictureFormat;	 Catch:{ all -> 0x0124 }
        r3.setPictureFormat(r6);	 Catch:{ all -> 0x0124 }
        r6 = 100;
        r3.setJpegQuality(r6);	 Catch:{ all -> 0x0124 }
        r3.setJpegThumbnailQuality(r6);	 Catch:{ all -> 0x0124 }
        r6 = r3.getMaxZoom();	 Catch:{ all -> 0x0124 }
        r10.maxZoom = r6;	 Catch:{ all -> 0x0124 }
        r6 = r10.currentZoom;	 Catch:{ all -> 0x0124 }
        r8 = r10.maxZoom;	 Catch:{ all -> 0x0124 }
        r8 = (float) r8;	 Catch:{ all -> 0x0124 }
        r6 = r6 * r8;
        r6 = (int) r6;	 Catch:{ all -> 0x0124 }
        r3.setZoom(r6);	 Catch:{ all -> 0x0124 }
        r6 = r10.optimizeForBarcode;	 Catch:{ all -> 0x0124 }
        if (r6 == 0) goto L_0x00d2;
    L_0x00b5:
        r6 = r3.getSupportedSceneModes();	 Catch:{ all -> 0x0124 }
        r6 = r6.contains(r0);	 Catch:{ all -> 0x0124 }
        if (r6 == 0) goto L_0x00c2;
    L_0x00bf:
        r3.setSceneMode(r0);	 Catch:{ all -> 0x0124 }
    L_0x00c2:
        r0 = "continuous-video";
        r6 = r3.getSupportedFocusModes();	 Catch:{ all -> 0x0124 }
        r6 = r6.contains(r0);	 Catch:{ all -> 0x0124 }
        if (r6 == 0) goto L_0x00e1;
    L_0x00ce:
        r3.setFocusMode(r0);	 Catch:{ all -> 0x0124 }
        goto L_0x00e1;
    L_0x00d2:
        r0 = "continuous-picture";
        r6 = r3.getSupportedFocusModes();	 Catch:{ all -> 0x0124 }
        r6 = r6.contains(r0);	 Catch:{ all -> 0x0124 }
        if (r6 == 0) goto L_0x00e1;
    L_0x00de:
        r3.setFocusMode(r0);	 Catch:{ all -> 0x0124 }
    L_0x00e1:
        r0 = r10.jpegOrientation;	 Catch:{ all -> 0x0124 }
        r6 = -1;
        if (r0 == r6) goto L_0x00fc;
    L_0x00e6:
        r0 = r2.facing;	 Catch:{ all -> 0x0124 }
        if (r0 != r4) goto L_0x00f4;
    L_0x00ea:
        r0 = r2.orientation;	 Catch:{ all -> 0x0124 }
        r6 = r10.jpegOrientation;	 Catch:{ all -> 0x0124 }
        r0 = r0 - r6;
        r0 = r0 + 360;
        r0 = r0 % 360;
        goto L_0x00fd;
    L_0x00f4:
        r0 = r2.orientation;	 Catch:{ all -> 0x0124 }
        r6 = r10.jpegOrientation;	 Catch:{ all -> 0x0124 }
        r0 = r0 + r6;
        r0 = r0 % 360;
        goto L_0x00fd;
    L_0x00fc:
        r0 = 0;
    L_0x00fd:
        r3.setRotation(r0);	 Catch:{ Exception -> 0x0113 }
        r2 = r2.facing;	 Catch:{ Exception -> 0x0113 }
        if (r2 != r4) goto L_0x010e;
    L_0x0104:
        r2 = 360 - r5;
        r2 = r2 % 360;
        if (r2 != r0) goto L_0x010b;
    L_0x010a:
        r7 = 1;
    L_0x010b:
        r10.sameTakePictureOrientation = r7;	 Catch:{ Exception -> 0x0113 }
        goto L_0x0113;
    L_0x010e:
        if (r5 != r0) goto L_0x0111;
    L_0x0110:
        r7 = 1;
    L_0x0111:
        r10.sameTakePictureOrientation = r7;	 Catch:{ Exception -> 0x0113 }
    L_0x0113:
        r0 = r10.currentFlashMode;	 Catch:{ all -> 0x0124 }
        r3.setFlashMode(r0);	 Catch:{ all -> 0x0124 }
        r1.setParameters(r3);	 Catch:{ Exception -> 0x011b }
    L_0x011b:
        r0 = r3.getMaxNumMeteringAreas();	 Catch:{ all -> 0x0124 }
        if (r0 <= 0) goto L_0x0128;
    L_0x0121:
        r10.meteringAreaSupported = r4;	 Catch:{ all -> 0x0124 }
        goto L_0x0128;
    L_0x0124:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x0128:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.camera.CameraSession.configurePhotoCamera():void");
    }

    /* Access modifiers changed, original: protected */
    public void focusToRect(Rect rect, Rect rect2) {
        try {
            Camera camera = this.cameraInfo.camera;
            if (camera != null) {
                camera.cancelAutoFocus();
                Parameters parameters = null;
                try {
                    parameters = camera.getParameters();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                if (parameters != null) {
                    parameters.setFocusMode("auto");
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(new Area(rect, 1000));
                    parameters.setFocusAreas(arrayList);
                    if (this.meteringAreaSupported) {
                        ArrayList arrayList2 = new ArrayList();
                        arrayList2.add(new Area(rect2, 1000));
                        parameters.setMeteringAreas(arrayList2);
                    }
                    try {
                        camera.setParameters(parameters);
                        camera.autoFocus(this.autoFocusCallback);
                    } catch (Exception e2) {
                        FileLog.e(e2);
                    }
                }
            }
        } catch (Exception e22) {
            FileLog.e(e22);
        }
    }

    /* Access modifiers changed, original: protected */
    public int getMaxZoom() {
        return this.maxZoom;
    }

    /* Access modifiers changed, original: protected */
    public void setZoom(float f) {
        this.currentZoom = f;
        configurePhotoCamera();
    }

    /* Access modifiers changed, original: protected */
    public void configureRecorder(int i, MediaRecorder mediaRecorder) {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(this.cameraInfo.cameraId, cameraInfo);
        getDisplayOrientation(cameraInfo, false);
        int i2 = this.jpegOrientation;
        int i3 = i2 != -1 ? cameraInfo.facing == 1 ? ((cameraInfo.orientation - i2) + 360) % 360 : (cameraInfo.orientation + i2) % 360 : 0;
        mediaRecorder.setOrientationHint(i3);
        i3 = getHigh();
        boolean hasProfile = CamcorderProfile.hasProfile(this.cameraInfo.cameraId, i3);
        boolean hasProfile2 = CamcorderProfile.hasProfile(this.cameraInfo.cameraId, 0);
        if (hasProfile && (i == 1 || !hasProfile2)) {
            mediaRecorder.setProfile(CamcorderProfile.get(this.cameraInfo.cameraId, i3));
        } else if (hasProfile2) {
            mediaRecorder.setProfile(CamcorderProfile.get(this.cameraInfo.cameraId, 0));
        } else {
            throw new IllegalStateException("cannot find valid CamcorderProfile");
        }
        this.isVideo = true;
    }

    /* Access modifiers changed, original: protected */
    public void stopVideoRecording() {
        this.isVideo = false;
        configurePhotoCamera();
    }

    private int getHigh() {
        if ("LGE".equals(Build.MANUFACTURER)) {
            if ("g3_tmo_us".equals(Build.PRODUCT)) {
                return 4;
            }
        }
        return 1;
    }

    private int getDisplayOrientation(CameraInfo cameraInfo, boolean z) {
        int rotation = ((WindowManager) ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
        int i = 0;
        if (rotation != 0) {
            if (rotation == 1) {
                i = 90;
            } else if (rotation == 2) {
                i = 180;
            } else if (rotation == 3) {
                i = 270;
            }
        }
        if (cameraInfo.facing != 1) {
            return ((cameraInfo.orientation - i) + 360) % 360;
        }
        int i2 = (360 - ((cameraInfo.orientation + i) % 360)) % 360;
        if (!z && i2 == 90) {
            i2 = 270;
        }
        if (!z) {
            if ("Huawei".equals(Build.MANUFACTURER)) {
                if ("angler".equals(Build.PRODUCT) && i2 == 270) {
                    return 90;
                }
            }
        }
        return i2;
    }

    public int getDisplayOrientation() {
        try {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(this.cameraInfo.getCameraId(), cameraInfo);
            return getDisplayOrientation(cameraInfo, true);
        } catch (Exception e) {
            FileLog.e(e);
            return 0;
        }
    }

    public void setPreviewCallback(PreviewCallback previewCallback) {
        this.cameraInfo.camera.setPreviewCallback(previewCallback);
    }

    public void setOneShotPreviewCallback(PreviewCallback previewCallback) {
        CameraInfo cameraInfo = this.cameraInfo;
        if (cameraInfo != null) {
            Camera camera = cameraInfo.camera;
            if (camera != null) {
                try {
                    camera.setOneShotPreviewCallback(previewCallback);
                } catch (Exception unused) {
                }
            }
        }
    }

    public void destroy() {
        this.initied = false;
        OrientationEventListener orientationEventListener = this.orientationEventListener;
        if (orientationEventListener != null) {
            orientationEventListener.disable();
            this.orientationEventListener = null;
        }
    }
}
