package org.telegram.ui.Components.Paint;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Render {
    public static RectF RenderPath(Path path, RenderState renderState) {
        renderState.baseWeight = path.getBaseWeight();
        renderState.spacing = path.getBrush().getSpacing();
        renderState.alpha = path.getBrush().getAlpha();
        renderState.angle = path.getBrush().getAngle();
        renderState.scale = path.getBrush().getScale();
        int length = path.getLength();
        if (length == 0) {
            return null;
        }
        int i = 0;
        if (length == 1) {
            PaintStamp(path.getPoints()[0], renderState);
        } else {
            Point[] points = path.getPoints();
            renderState.prepare();
            while (i < points.length - 1) {
                Point point = points[i];
                i++;
                PaintSegment(point, points[i], renderState);
            }
        }
        path.remainder = renderState.remainder;
        return Draw(renderState);
    }

    private static void PaintSegment(Point point, Point point2, RenderState renderState) {
        boolean z;
        int i;
        float f;
        Point point3 = point;
        Point point4 = point2;
        RenderState renderState2 = renderState;
        double distanceTo = (double) point.getDistanceTo(point2);
        Point substract = point4.substract(point3);
        Point point5 = new Point(1.0d, 1.0d, 0.0d);
        float atan2 = Math.abs(renderState2.angle) > 0.0f ? renderState2.angle : (float) Math.atan2(substract.y, substract.x);
        float f2 = renderState2.baseWeight * renderState2.scale;
        double max = (double) Math.max(1.0f, renderState2.spacing * f2);
        if (distanceTo > 0.0d) {
            Double.isNaN(distanceTo);
            point5 = substract.multiplyByScalar(1.0d / distanceTo);
        }
        Point point6 = point5;
        float min = Math.min(1.0f, renderState2.alpha * 1.15f);
        boolean z2 = point3.edge;
        boolean z3 = point4.edge;
        double d = renderState2.remainder;
        Double.isNaN(distanceTo);
        Double.isNaN(max);
        int count = renderState.getCount();
        renderState2.appendValuesCount((int) Math.ceil((distanceTo - d) / max));
        renderState2.setPosition(count);
        Point add = point3.add(point6.multiplyByScalar(renderState2.remainder));
        double d2 = renderState2.remainder;
        boolean z4 = true;
        while (true) {
            if (d2 > distanceTo) {
                z = z3;
                i = 1;
                break;
            }
            if (z2) {
                f = min;
            } else {
                f = renderState2.alpha;
            }
            i = 1;
            float f3 = f;
            z = z3;
            z4 = renderState.addPoint(add.toPointF(), f2, atan2, f3, -1);
            if (!z4) {
                break;
            }
            add = add.add(point6.multiplyByScalar(max));
            z2 = false;
            Double.isNaN(max);
            d2 += max;
            Point point7 = point2;
            z3 = z;
        }
        if (z4 && z) {
            renderState2.appendValuesCount(i);
            renderState.addPoint(point2.toPointF(), f2, atan2, min, -1);
        }
        Double.isNaN(distanceTo);
        renderState2.remainder = d2 - distanceTo;
    }

    private static void PaintStamp(Point point, RenderState renderState) {
        float f = renderState.baseWeight * renderState.scale;
        PointF pointF = point.toPointF();
        float f2 = Math.abs(renderState.angle) > 0.0f ? renderState.angle : 0.0f;
        float f3 = renderState.alpha;
        renderState.prepare();
        renderState.appendValuesCount(1);
        renderState.addPoint(pointF, f, f2, f3, 0);
    }

    private static RectF Draw(RenderState renderState) {
        float f;
        int i;
        RectF rectF = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
        int count = renderState.getCount();
        if (count == 0) {
            return rectF;
        }
        int i2 = count - 1;
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(((count * 4) + (i2 * 2)) * 20);
        allocateDirect.order(ByteOrder.nativeOrder());
        FloatBuffer asFloatBuffer = allocateDirect.asFloatBuffer();
        char c = 0;
        asFloatBuffer.position(0);
        renderState.setPosition(0);
        int i3 = 0;
        int i4 = 0;
        while (i3 < count) {
            float read = renderState.read();
            float read2 = renderState.read();
            float read3 = renderState.read();
            float read4 = renderState.read();
            float read5 = renderState.read();
            RectF rectF2 = new RectF(read - read3, read2 - read3, read + read3, read2 + read3);
            float[] fArr = new float[8];
            float f2 = rectF2.left;
            fArr[c] = f2;
            float f3 = rectF2.top;
            fArr[1] = f3;
            float f4 = rectF2.right;
            fArr[2] = f4;
            fArr[3] = f3;
            fArr[4] = f2;
            float f5 = rectF2.bottom;
            fArr[5] = f5;
            fArr[6] = f4;
            fArr[7] = f5;
            float centerX = rectF2.centerX();
            float centerY = rectF2.centerY();
            Matrix matrix = new Matrix();
            matrix.setRotate((float) Math.toDegrees((double) read4), centerX, centerY);
            matrix.mapPoints(fArr);
            matrix.mapRect(rectF2);
            Utils.RectFIntegral(rectF2);
            rectF.union(rectF2);
            if (i4 != 0) {
                asFloatBuffer.put(fArr[0]);
                i = 1;
                asFloatBuffer.put(fArr[1]);
                f = 0.0f;
                asFloatBuffer.put(0.0f);
                asFloatBuffer.put(0.0f);
                asFloatBuffer.put(read5);
                i4++;
            } else {
                i = 1;
                f = 0.0f;
            }
            asFloatBuffer.put(fArr[0]);
            asFloatBuffer.put(fArr[i]);
            asFloatBuffer.put(f);
            asFloatBuffer.put(f);
            asFloatBuffer.put(read5);
            asFloatBuffer.put(fArr[2]);
            asFloatBuffer.put(fArr[3]);
            asFloatBuffer.put(1.0f);
            asFloatBuffer.put(f);
            asFloatBuffer.put(read5);
            asFloatBuffer.put(fArr[4]);
            asFloatBuffer.put(fArr[5]);
            asFloatBuffer.put(f);
            asFloatBuffer.put(1.0f);
            asFloatBuffer.put(read5);
            asFloatBuffer.put(fArr[6]);
            asFloatBuffer.put(fArr[7]);
            asFloatBuffer.put(1.0f);
            asFloatBuffer.put(1.0f);
            asFloatBuffer.put(read5);
            i4 = i4 + i + i + i + i;
            if (i3 != i2) {
                asFloatBuffer.put(fArr[6]);
                asFloatBuffer.put(fArr[7]);
                asFloatBuffer.put(1.0f);
                asFloatBuffer.put(1.0f);
                asFloatBuffer.put(read5);
                i4++;
            }
            i3++;
            RenderState renderState2 = renderState;
            c = 0;
        }
        asFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(0, 2, 5126, false, 20, asFloatBuffer.slice());
        GLES20.glEnableVertexAttribArray(0);
        asFloatBuffer.position(2);
        GLES20.glVertexAttribPointer(1, 2, 5126, true, 20, asFloatBuffer.slice());
        GLES20.glEnableVertexAttribArray(1);
        asFloatBuffer.position(4);
        GLES20.glVertexAttribPointer(2, 1, 5126, true, 20, asFloatBuffer.slice());
        GLES20.glEnableVertexAttribArray(2);
        GLES20.glDrawArrays(5, 0, i4);
        return rectF;
    }
}
