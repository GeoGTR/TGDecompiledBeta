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
        SensorManager sensorManager2 = (SensorManager) context.getSystemService("sensor");
        this.sensorManager = sensorManager2;
        this.accelerometer = sensorManager2.getDefaultSensor(1);
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

    public void setCallback(Callback callback2) {
        this.callback = callback2;
    }

    public float getScale(int i, int i2) {
        float f = (float) i;
        float dp = (float) (AndroidUtilities.dp(16.0f) * 2);
        float f2 = (f + dp) / f;
        float f3 = (float) i2;
        return Math.max(f2, (dp + f3) / f3);
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x00b1  */
    /* JADX WARNING: Removed duplicated region for block: B:24:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onSensorChanged(android.hardware.SensorEvent r17) {
        /*
            r16 = this;
            r0 = r16
            android.view.WindowManager r1 = r0.wm
            android.view.Display r1 = r1.getDefaultDisplay()
            int r1 = r1.getRotation()
            r2 = r17
            float[] r2 = r2.values
            r3 = 0
            r4 = r2[r3]
            r5 = 1092413450(0x411ce80a, float:9.80665)
            float r4 = r4 / r5
            r6 = 1
            r7 = r2[r6]
            float r7 = r7 / r5
            r8 = 2
            r2 = r2[r8]
            float r2 = r2 / r5
            double r9 = (double) r4
            float r5 = r7 * r7
            float r2 = r2 * r2
            float r5 = r5 + r2
            double r11 = (double) r5
            double r11 = java.lang.Math.sqrt(r11)
            double r9 = java.lang.Math.atan2(r9, r11)
            r11 = 4614256656552045848(0x400921fb54442d18, double:3.NUM)
            double r9 = r9 / r11
            r13 = 4611686018427387904(0xNUM, double:2.0)
            double r9 = r9 * r13
            float r5 = (float) r9
            double r9 = (double) r7
            float r4 = r4 * r4
            float r4 = r4 + r2
            double r3 = (double) r4
            double r2 = java.lang.Math.sqrt(r3)
            double r2 = java.lang.Math.atan2(r9, r2)
            double r2 = r2 / r11
            double r2 = r2 * r13
            float r2 = (float) r2
            if (r1 == r6) goto L_0x005b
            if (r1 == r8) goto L_0x0057
            r3 = 3
            if (r1 == r3) goto L_0x0055
            r15 = r5
            r5 = r2
            r2 = r15
            goto L_0x005b
        L_0x0055:
            float r5 = -r5
            goto L_0x005b
        L_0x0057:
            float r1 = -r2
            float r5 = -r5
            r2 = r5
            r5 = r1
        L_0x005b:
            float[] r1 = r0.rollBuffer
            int r3 = r0.bufferOffset
            r1[r3] = r5
            float[] r4 = r0.pitchBuffer
            r4[r3] = r2
            int r3 = r3 + r6
            int r1 = r1.length
            int r3 = r3 % r1
            r0.bufferOffset = r3
            r1 = 0
            r2 = 0
            r3 = 0
        L_0x006d:
            float[] r4 = r0.rollBuffer
            int r5 = r4.length
            if (r3 >= r5) goto L_0x007d
            r4 = r4[r3]
            float r1 = r1 + r4
            float[] r4 = r0.pitchBuffer
            r4 = r4[r3]
            float r2 = r2 + r4
            int r3 = r3 + 1
            goto L_0x006d
        L_0x007d:
            int r3 = r4.length
            float r3 = (float) r3
            float r1 = r1 / r3
            int r3 = r4.length
            float r3 = (float) r3
            float r2 = r2 / r3
            r3 = 1065353216(0x3var_, float:1.0)
            int r3 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r3 <= 0) goto L_0x008e
            r3 = 1073741824(0x40000000, float:2.0)
        L_0x008b:
            float r1 = r3 - r1
            goto L_0x0097
        L_0x008e:
            r3 = -1082130432(0xffffffffbvar_, float:-1.0)
            int r3 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r3 >= 0) goto L_0x0097
            r3 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
            goto L_0x008b
        L_0x0097:
            r3 = 1098907648(0x41800000, float:16.0)
            float r4 = org.telegram.messenger.AndroidUtilities.dpf2(r3)
            float r2 = r2 * r4
            int r2 = java.lang.Math.round(r2)
            float r3 = org.telegram.messenger.AndroidUtilities.dpf2(r3)
            float r1 = r1 * r3
            int r1 = java.lang.Math.round(r1)
            org.telegram.ui.Components.WallpaperParallaxEffect$Callback r3 = r0.callback
            if (r3 == 0) goto L_0x00b4
            r3.onOffsetsChanged(r2, r1)
        L_0x00b4:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.WallpaperParallaxEffect.onSensorChanged(android.hardware.SensorEvent):void");
    }
}
