package org.telegram.p005ui.Components.Paint.Views;

import android.view.MotionEvent;

/* renamed from: org.telegram.ui.Components.Paint.Views.RotationGestureDetector */
public class RotationGestureDetector {
    private float angle;
    /* renamed from: fX */
    private float var_fX;
    /* renamed from: fY */
    private float var_fY;
    private OnRotationGestureListener mListener;
    /* renamed from: sX */
    private float var_sX;
    /* renamed from: sY */
    private float var_sY;
    private float startAngle;

    /* renamed from: org.telegram.ui.Components.Paint.Views.RotationGestureDetector$OnRotationGestureListener */
    public interface OnRotationGestureListener {
        void onRotation(RotationGestureDetector rotationGestureDetector);

        void onRotationBegin(RotationGestureDetector rotationGestureDetector);

        void onRotationEnd(RotationGestureDetector rotationGestureDetector);
    }

    public float getAngle() {
        return this.angle;
    }

    public float getStartAngle() {
        return this.startAngle;
    }

    public RotationGestureDetector(OnRotationGestureListener listener) {
        this.mListener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() != 2) {
            return false;
        }
        switch (event.getActionMasked()) {
            case 0:
            case 5:
                this.var_sX = event.getX(0);
                this.var_sY = event.getY(0);
                this.var_fX = event.getX(1);
                this.var_fY = event.getY(1);
                break;
            case 1:
            case 3:
                this.startAngle = Float.NaN;
                break;
            case 2:
                float nsX = event.getX(0);
                float nsY = event.getY(0);
                this.angle = angleBetweenLines(this.var_fX, this.var_fY, this.var_sX, this.var_sY, event.getX(1), event.getY(1), nsX, nsY);
                if (this.mListener != null) {
                    if (!Float.isNaN(this.startAngle)) {
                        this.mListener.onRotation(this);
                        break;
                    }
                    this.startAngle = this.angle;
                    this.mListener.onRotationBegin(this);
                    break;
                }
                break;
            case 6:
                this.startAngle = Float.NaN;
                if (this.mListener != null) {
                    this.mListener.onRotationEnd(this);
                    break;
                }
                break;
        }
        return true;
    }

    private float angleBetweenLines(float fX, float fY, float sX, float sY, float nfX, float nfY, float nsX, float nsY) {
        float angle = ((float) Math.toDegrees((double) (((float) Math.atan2((double) (fY - sY), (double) (fX - sX))) - ((float) Math.atan2((double) (nfY - nsY), (double) (nfX - nsX)))))) % 360.0f;
        if (angle < -180.0f) {
            angle += 360.0f;
        }
        if (angle > 180.0f) {
            return angle - 360.0f;
        }
        return angle;
    }
}
