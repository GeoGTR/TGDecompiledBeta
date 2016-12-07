package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.volley.DefaultRetryPolicy;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.User;

public class GroupCreateSpan extends View {
    private static Paint backPaint = new Paint(1);
    private static TextPaint textPaint = new TextPaint(1);
    private AvatarDrawable avatarDrawable;
    private int[] colors = new int[6];
    private Drawable deleteDrawable = getResources().getDrawable(R.drawable.delete);
    private boolean deleting;
    private ImageReceiver imageReceiver;
    private long lastUpdateTime;
    private StaticLayout nameLayout;
    private float progress;
    private RectF rect = new RectF();
    private int textWidth;
    private float textX;
    private int uid;

    public GroupCreateSpan(Context context, User user) {
        int maxNameWidth;
        super(context);
        textPaint.setTextSize((float) AndroidUtilities.dp(14.0f));
        textPaint.setColor(-14606047);
        backPaint.setColor(-855310);
        this.avatarDrawable = new AvatarDrawable();
        this.avatarDrawable.setTextSize(AndroidUtilities.dp(12.0f));
        this.avatarDrawable.setInfo(user);
        this.avatarDrawable.setColor(AvatarDrawable.getColorForId(5));
        this.imageReceiver = new ImageReceiver();
        this.imageReceiver.setRoundRadius(AndroidUtilities.dp(16.0f));
        this.imageReceiver.setParentView(this);
        this.imageReceiver.setImageCoords(0, 0, AndroidUtilities.dp(32.0f), AndroidUtilities.dp(32.0f));
        this.uid = user.id;
        if (AndroidUtilities.isTablet()) {
            maxNameWidth = AndroidUtilities.dp(366.0f) / 2;
        } else {
            maxNameWidth = (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(164.0f)) / 2;
        }
        this.nameLayout = new StaticLayout(TextUtils.ellipsize(UserObject.getFirstName(user).replace('\n', ' '), textPaint, (float) maxNameWidth, TruncateAt.END), textPaint, 1000, Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
        if (this.nameLayout.getLineCount() > 0) {
            this.textWidth = (int) Math.ceil((double) this.nameLayout.getLineWidth(0));
            this.textX = -this.nameLayout.getLineLeft(0);
        }
        FileLocation photo = null;
        if (user.photo != null) {
            photo = user.photo.photo_small;
        }
        this.imageReceiver.setImage(photo, null, "50_50", this.avatarDrawable, null, null, 0, null, true);
        int color = AvatarDrawable.getGroupCreateColor(5);
        this.colors[0] = Color.red(-855310);
        this.colors[1] = Color.red(color) - 20;
        this.colors[2] = Color.green(-855310);
        this.colors[3] = Color.green(color) - 20;
        this.colors[4] = Color.blue(-855310);
        this.colors[5] = Color.blue(color) - 20;
    }

    public boolean isDeleting() {
        return this.deleting;
    }

    public void startDeleteAnimation() {
        if (!this.deleting) {
            this.deleting = true;
            this.lastUpdateTime = System.currentTimeMillis();
            invalidate();
        }
    }

    public void cancelDeleteAnimation() {
        if (this.deleting) {
            this.deleting = false;
            this.lastUpdateTime = System.currentTimeMillis();
            invalidate();
        }
    }

    public int getUid() {
        return this.uid;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(AndroidUtilities.dp(57.0f) + this.textWidth, AndroidUtilities.dp(32.0f));
    }

    protected void onDraw(Canvas canvas) {
        if ((this.deleting && this.progress != DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) || !(this.deleting || this.progress == 0.0f)) {
            long dt = System.currentTimeMillis() - this.lastUpdateTime;
            if (dt < 0 || dt > 17) {
                dt = 17;
            }
            if (this.deleting) {
                this.progress += ((float) dt) / BitmapDescriptorFactory.HUE_GREEN;
                if (this.progress >= DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
                    this.progress = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
                }
            } else {
                this.progress -= ((float) dt) / BitmapDescriptorFactory.HUE_GREEN;
                if (this.progress < 0.0f) {
                    this.progress = 0.0f;
                }
            }
            invalidate();
        }
        canvas.save();
        this.rect.set(0.0f, 0.0f, (float) getMeasuredWidth(), (float) AndroidUtilities.dp(32.0f));
        backPaint.setColor(Color.argb(255, this.colors[0] + ((int) (((float) (this.colors[1] - this.colors[0])) * this.progress)), this.colors[2] + ((int) (((float) (this.colors[3] - this.colors[2])) * this.progress)), this.colors[4] + ((int) (((float) (this.colors[5] - this.colors[4])) * this.progress))));
        canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(16.0f), (float) AndroidUtilities.dp(16.0f), backPaint);
        this.imageReceiver.draw(canvas);
        if (this.progress != 0.0f) {
            backPaint.setColor(this.avatarDrawable.getColor());
            backPaint.setAlpha((int) (255.0f * this.progress));
            canvas.drawCircle((float) AndroidUtilities.dp(16.0f), (float) AndroidUtilities.dp(16.0f), (float) AndroidUtilities.dp(16.0f), backPaint);
            canvas.save();
            canvas.rotate(45.0f * (DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - this.progress), (float) AndroidUtilities.dp(16.0f), (float) AndroidUtilities.dp(16.0f));
            this.deleteDrawable.setBounds(AndroidUtilities.dp(11.0f), AndroidUtilities.dp(11.0f), AndroidUtilities.dp(21.0f), AndroidUtilities.dp(21.0f));
            this.deleteDrawable.setAlpha((int) (255.0f * this.progress));
            this.deleteDrawable.draw(canvas);
            canvas.restore();
        }
        canvas.translate(this.textX + ((float) AndroidUtilities.dp(41.0f)), (float) AndroidUtilities.dp(8.0f));
        this.nameLayout.draw(canvas);
        canvas.restore();
    }
}