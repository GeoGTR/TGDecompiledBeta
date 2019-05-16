package org.telegram.ui.Components;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;
import org.telegram.messenger.AndroidUtilities;

public class WallpaperParallaxEffect implements SensorEventListener {
    private Sensor accelerometer;
    private int bufferOffset;
    private Callback callback;
    private boolean enabled;
    private float[] pitchBuffer = new float[3];
    private float[] rollBuffer = new float[3];
    private SensorManager sensorManager;
    private WindowManager wm;

    public interface Callback {
        void onOffsetsChanged(int i, int i2);
    }

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public WallpaperParallaxEffect(Context context) {
        this.wm = (WindowManager) context.getSystemService("window");
        this.sensorManager = (SensorManager) context.getSystemService("sensor");
        this.accelerometer = this.sensorManager.getDefaultSensor(1);
    }

    public void setEnabled(boolean z) {
        if (this.enabled != z) {
            this.enabled = z;
            Sensor sensor = this.accelerometer;
            if (sensor != null) {
                if (z) {
                    this.sensorManager.registerListener(this, sensor, 1);
                } else {
                    this.sensorManager.unregisterListener(this);
                }
            }
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public float getScale(int i, int i2) {
        float f = (float) i;
        float dp = (float) (AndroidUtilities.dp(16.0f) * 2);
        float f2 = (f + dp) / f;
        f = (float) i2;
        return Math.max(f2, (dp + f) / f);
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0081 A:{SYNTHETIC, EDGE_INSN: B:23:0x0081->B:13:0x0081 ?: BREAK  , EDGE_INSN: B:23:0x0081->B:13:0x0081 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0076 A:{LOOP_END, LOOP:0: B:10:0x0071->B:12:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:25:? A:{SYNTHETIC, RETURN} */
    public void onSensorChanged(android.hardware.SensorEvent r17) {
        /*
        r16 = this;
        r0 = r16;
        r1 = r0.wm;
        r1 = r1.getDefaultDisplay();
        r1 = r1.getRotation();
        r2 = r17;
        r2 = r2.values;
        r3 = 0;
        r4 = r2[r3];
        r5 = NUM; // 0x411ce80a float:9.80665 double:5.397239567E-315;
        r4 = r4 / r5;
        r6 = 1;
        r7 = r2[r6];
        r7 = r7 / r5;
        r8 = 2;
        r2 = r2[r8];
        r2 = r2 / r5;
        r9 = (double) r4;
        r5 = r7 * r7;
        r2 = r2 * r2;
        r5 = r5 + r2;
        r11 = (double) r5;
        r11 = java.lang.Math.sqrt(r11);
        r9 = java.lang.Math.atan2(r9, r11);
        r11 = NUM; // 0x400921fb54442d18 float:3.37028055E12 double:3.NUM;
        r9 = r9 / r11;
        r13 = NUM; // 0xNUM float:0.0 double:2.0;
        r9 = r9 * r13;
        r5 = (float) r9;
        r9 = (double) r7;
        r4 = r4 * r4;
        r4 = r4 + r2;
        r3 = (double) r4;
        r2 = java.lang.Math.sqrt(r3);
        r2 = java.lang.Math.atan2(r9, r2);
        r2 = r2 / r11;
        r2 = r2 * r13;
        r2 = (float) r2;
        if (r1 == 0) goto L_0x005b;
    L_0x004c:
        if (r1 == r6) goto L_0x005e;
    L_0x004e:
        if (r1 == r8) goto L_0x0056;
    L_0x0050:
        r3 = 3;
        if (r1 == r3) goto L_0x0054;
    L_0x0053:
        goto L_0x005b;
    L_0x0054:
        r5 = -r5;
        goto L_0x005e;
    L_0x0056:
        r1 = -r2;
        r5 = -r5;
        r2 = r5;
        r5 = r1;
        goto L_0x005e;
    L_0x005b:
        r15 = r5;
        r5 = r2;
        r2 = r15;
    L_0x005e:
        r1 = r0.rollBuffer;
        r3 = r0.bufferOffset;
        r1[r3] = r5;
        r4 = r0.pitchBuffer;
        r4[r3] = r2;
        r3 = r3 + r6;
        r1 = r1.length;
        r3 = r3 % r1;
        r0.bufferOffset = r3;
        r1 = 0;
        r1 = 0;
        r2 = 0;
        r3 = 0;
    L_0x0071:
        r4 = r0.rollBuffer;
        r5 = r4.length;
        if (r1 >= r5) goto L_0x0081;
    L_0x0076:
        r4 = r4[r1];
        r2 = r2 + r4;
        r4 = r0.pitchBuffer;
        r4 = r4[r1];
        r3 = r3 + r4;
        r1 = r1 + 1;
        goto L_0x0071;
    L_0x0081:
        r1 = r4.length;
        r1 = (float) r1;
        r2 = r2 / r1;
        r1 = r4.length;
        r1 = (float) r1;
        r3 = r3 / r1;
        r1 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r1 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1));
        if (r1 <= 0) goto L_0x0092;
    L_0x008d:
        r1 = NUM; // 0x40000000 float:2.0 double:5.304989477E-315;
    L_0x008f:
        r2 = r1 - r2;
        goto L_0x009b;
    L_0x0092:
        r1 = -NUM; // 0xffffffffbvar_ float:-1.0 double:NaN;
        r1 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1));
        if (r1 >= 0) goto L_0x009b;
    L_0x0098:
        r1 = -NUM; // 0xffffffffCLASSNAME float:-2.0 double:NaN;
        goto L_0x008f;
    L_0x009b:
        r1 = NUM; // 0x41800000 float:16.0 double:5.42932517E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dpf2(r1);
        r3 = r3 * r4;
        r3 = java.lang.Math.round(r3);
        r1 = org.telegram.messenger.AndroidUtilities.dpf2(r1);
        r2 = r2 * r1;
        r1 = java.lang.Math.round(r2);
        r2 = r0.callback;
        if (r2 == 0) goto L_0x00b8;
    L_0x00b5:
        r2.onOffsetsChanged(r3, r1);
    L_0x00b8:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.WallpaperParallaxEffect.onSensorChanged(android.hardware.SensorEvent):void");
    }
}
