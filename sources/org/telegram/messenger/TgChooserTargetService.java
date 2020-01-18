package org.telegram.messenger;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Icon;
import android.service.chooser.ChooserTarget;
import android.service.chooser.ChooserTargetService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.LaunchActivity;

@TargetApi(23)
public class TgChooserTargetService extends ChooserTargetService {
    private RectF bitmapRect;
    private Paint roundPaint;

    public List<ChooserTarget> onGetChooserTargets(ComponentName componentName, IntentFilter intentFilter) {
        int i = UserConfig.selectedAccount;
        ArrayList arrayList = new ArrayList();
        if (!UserConfig.getInstance(i).isClientActivated() || !MessagesController.getGlobalMainSettings().getBoolean("direct_share", true)) {
            return arrayList;
        }
        ImageLoader.getInstance();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        MessagesStorage.getInstance(i).getStorageQueue().postRunnable(new -$$Lambda$TgChooserTargetService$Tk7TkprF_pFKgMncgdcbNzQeTqs(this, i, arrayList, new ComponentName(getPackageName(), LaunchActivity.class.getCanonicalName()), countDownLatch));
        try {
            countDownLatch.await();
        } catch (Exception e) {
            FileLog.e(e);
        }
        return arrayList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:72:0x01b5  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0199  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0199  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x01b5  */
    public /* synthetic */ void lambda$onGetChooserTargets$0$TgChooserTargetService(int r18, java.util.List r19, android.content.ComponentName r20, java.util.concurrent.CountDownLatch r21) {
        /*
        r17 = this;
        r1 = r17;
        r2 = new java.util.ArrayList;
        r2.<init>();
        r3 = new java.util.ArrayList;
        r3.<init>();
        r4 = new java.util.ArrayList;
        r4.<init>();
        r5 = 1;
        r6 = 0;
        r0 = new java.util.ArrayList;	 Catch:{ Exception -> 0x00bd }
        r0.<init>();	 Catch:{ Exception -> 0x00bd }
        r7 = org.telegram.messenger.UserConfig.getInstance(r18);	 Catch:{ Exception -> 0x00bd }
        r7 = r7.getClientUserId();	 Catch:{ Exception -> 0x00bd }
        r7 = java.lang.Integer.valueOf(r7);	 Catch:{ Exception -> 0x00bd }
        r0.add(r7);	 Catch:{ Exception -> 0x00bd }
        r7 = new java.util.ArrayList;	 Catch:{ Exception -> 0x00bd }
        r7.<init>();	 Catch:{ Exception -> 0x00bd }
        r8 = org.telegram.messenger.MessagesStorage.getInstance(r18);	 Catch:{ Exception -> 0x00bd }
        r8 = r8.getDatabase();	 Catch:{ Exception -> 0x00bd }
        r9 = java.util.Locale.US;	 Catch:{ Exception -> 0x00bd }
        r10 = "SELECT did FROM dialogs ORDER BY date DESC LIMIT %d,%d";
        r11 = 2;
        r11 = new java.lang.Object[r11];	 Catch:{ Exception -> 0x00bd }
        r12 = java.lang.Integer.valueOf(r6);	 Catch:{ Exception -> 0x00bd }
        r11[r6] = r12;	 Catch:{ Exception -> 0x00bd }
        r12 = 30;
        r12 = java.lang.Integer.valueOf(r12);	 Catch:{ Exception -> 0x00bd }
        r11[r5] = r12;	 Catch:{ Exception -> 0x00bd }
        r9 = java.lang.String.format(r9, r10, r11);	 Catch:{ Exception -> 0x00bd }
        r10 = new java.lang.Object[r6];	 Catch:{ Exception -> 0x00bd }
        r8 = r8.queryFinalized(r9, r10);	 Catch:{ Exception -> 0x00bd }
    L_0x0053:
        r9 = r8.next();	 Catch:{ Exception -> 0x00bd }
        if (r9 == 0) goto L_0x0095;
    L_0x0059:
        r9 = r8.longValue(r6);	 Catch:{ Exception -> 0x00bd }
        r10 = (int) r9;	 Catch:{ Exception -> 0x00bd }
        if (r10 == 0) goto L_0x0053;
    L_0x0060:
        if (r10 <= 0) goto L_0x0074;
    L_0x0062:
        r9 = java.lang.Integer.valueOf(r10);	 Catch:{ Exception -> 0x00bd }
        r9 = r0.contains(r9);	 Catch:{ Exception -> 0x00bd }
        if (r9 != 0) goto L_0x0086;
    L_0x006c:
        r9 = java.lang.Integer.valueOf(r10);	 Catch:{ Exception -> 0x00bd }
        r0.add(r9);	 Catch:{ Exception -> 0x00bd }
        goto L_0x0086;
    L_0x0074:
        r9 = -r10;
        r11 = java.lang.Integer.valueOf(r9);	 Catch:{ Exception -> 0x00bd }
        r11 = r7.contains(r11);	 Catch:{ Exception -> 0x00bd }
        if (r11 != 0) goto L_0x0086;
    L_0x007f:
        r9 = java.lang.Integer.valueOf(r9);	 Catch:{ Exception -> 0x00bd }
        r7.add(r9);	 Catch:{ Exception -> 0x00bd }
    L_0x0086:
        r9 = java.lang.Integer.valueOf(r10);	 Catch:{ Exception -> 0x00bd }
        r2.add(r9);	 Catch:{ Exception -> 0x00bd }
        r9 = r2.size();	 Catch:{ Exception -> 0x00bd }
        r10 = 8;
        if (r9 != r10) goto L_0x0053;
    L_0x0095:
        r8.dispose();	 Catch:{ Exception -> 0x00bd }
        r8 = r7.isEmpty();	 Catch:{ Exception -> 0x00bd }
        r9 = ",";
        if (r8 != 0) goto L_0x00ab;
    L_0x00a0:
        r8 = org.telegram.messenger.MessagesStorage.getInstance(r18);	 Catch:{ Exception -> 0x00bd }
        r7 = android.text.TextUtils.join(r9, r7);	 Catch:{ Exception -> 0x00bd }
        r8.getChatsInternal(r7, r3);	 Catch:{ Exception -> 0x00bd }
    L_0x00ab:
        r7 = r0.isEmpty();	 Catch:{ Exception -> 0x00bd }
        if (r7 != 0) goto L_0x00c1;
    L_0x00b1:
        r7 = org.telegram.messenger.MessagesStorage.getInstance(r18);	 Catch:{ Exception -> 0x00bd }
        r0 = android.text.TextUtils.join(r9, r0);	 Catch:{ Exception -> 0x00bd }
        r7.getUsersInternal(r0, r4);	 Catch:{ Exception -> 0x00bd }
        goto L_0x00c1;
    L_0x00bd:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x00c1:
        r0 = org.telegram.messenger.Utilities.random;
        r7 = r0.nextLong();
        org.telegram.messenger.SharedConfig.directShareHash = r7;
        r0 = org.telegram.messenger.ApplicationLoader.applicationContext;
        r7 = "mainconfig";
        r0 = r0.getSharedPreferences(r7, r6);
        r0 = r0.edit();
        r7 = org.telegram.messenger.SharedConfig.directShareHash;
        r9 = "directShareHash";
        r0 = r0.putLong(r9, r7);
        r0.commit();
        r0 = 0;
    L_0x00e1:
        r7 = r2.size();
        if (r0 >= r7) goto L_0x01bb;
    L_0x00e7:
        r13 = new android.os.Bundle;
        r13.<init>();
        r7 = r2.get(r0);
        r7 = (java.lang.Integer) r7;
        r7 = r7.intValue();
        r8 = "hash";
        r9 = "dialogId";
        r10 = 0;
        if (r7 <= 0) goto L_0x0154;
    L_0x00fd:
        r11 = 0;
    L_0x00fe:
        r12 = r4.size();
        if (r11 >= r12) goto L_0x0151;
    L_0x0104:
        r12 = r4.get(r11);
        r12 = (org.telegram.tgnet.TLRPC.User) r12;
        r14 = r12.id;
        if (r14 != r7) goto L_0x014e;
    L_0x010e:
        r11 = r12.bot;
        if (r11 != 0) goto L_0x0151;
    L_0x0112:
        r14 = (long) r7;
        r13.putLong(r9, r14);
        r14 = org.telegram.messenger.SharedConfig.directShareHash;
        r13.putLong(r8, r14);
        r7 = org.telegram.messenger.UserObject.isUserSelf(r12);
        if (r7 == 0) goto L_0x0134;
    L_0x0121:
        r7 = NUM; // 0x7f0e09bd float:1.8880094E38 double:1.0531633883E-314;
        r8 = "SavedMessages";
        r10 = org.telegram.messenger.LocaleController.getString(r8, r7);
        r7 = r17.createSavedMessagesIcon();
        r16 = r10;
        r10 = r7;
        r7 = r16;
        goto L_0x0152;
    L_0x0134:
        r7 = r12.first_name;
        r8 = r12.last_name;
        r7 = org.telegram.messenger.ContactsController.formatName(r7, r8);
        r8 = r12.photo;
        if (r8 == 0) goto L_0x0152;
    L_0x0140:
        r8 = r8.photo_small;
        if (r8 == 0) goto L_0x0152;
    L_0x0144:
        r8 = org.telegram.messenger.FileLoader.getPathToAttach(r8, r5);
        r8 = r1.createRoundBitmap(r8);
        r10 = r8;
        goto L_0x0152;
    L_0x014e:
        r11 = r11 + 1;
        goto L_0x00fe;
    L_0x0151:
        r7 = r10;
    L_0x0152:
        r9 = r7;
        goto L_0x0197;
    L_0x0154:
        r11 = 0;
    L_0x0155:
        r12 = r3.size();
        if (r11 >= r12) goto L_0x0196;
    L_0x015b:
        r12 = r3.get(r11);
        r12 = (org.telegram.tgnet.TLRPC.Chat) r12;
        r14 = r12.id;
        r15 = -r7;
        if (r14 != r15) goto L_0x0193;
    L_0x0166:
        r11 = org.telegram.messenger.ChatObject.isNotInChat(r12);
        if (r11 != 0) goto L_0x0196;
    L_0x016c:
        r11 = org.telegram.messenger.ChatObject.isChannel(r12);
        if (r11 == 0) goto L_0x0176;
    L_0x0172:
        r11 = r12.megagroup;
        if (r11 == 0) goto L_0x0196;
    L_0x0176:
        r14 = (long) r7;
        r13.putLong(r9, r14);
        r14 = org.telegram.messenger.SharedConfig.directShareHash;
        r13.putLong(r8, r14);
        r7 = r12.photo;
        if (r7 == 0) goto L_0x0190;
    L_0x0183:
        r7 = r7.photo_small;
        if (r7 == 0) goto L_0x0190;
    L_0x0187:
        r7 = org.telegram.messenger.FileLoader.getPathToAttach(r7, r5);
        r7 = r1.createRoundBitmap(r7);
        r10 = r7;
    L_0x0190:
        r7 = r12.title;
        goto L_0x0152;
    L_0x0193:
        r11 = r11 + 1;
        goto L_0x0155;
    L_0x0196:
        r9 = r10;
    L_0x0197:
        if (r9 == 0) goto L_0x01b5;
    L_0x0199:
        if (r10 != 0) goto L_0x01a5;
    L_0x019b:
        r7 = org.telegram.messenger.ApplicationLoader.applicationContext;
        r8 = NUM; // 0x7var_b float:1.7945347E38 double:1.0529356903E-314;
        r7 = android.graphics.drawable.Icon.createWithResource(r7, r8);
        r10 = r7;
    L_0x01a5:
        r7 = new android.service.chooser.ChooserTarget;
        r11 = NUM; // 0x3var_ float:1.0 double:5.263544247E-315;
        r8 = r7;
        r12 = r20;
        r8.<init>(r9, r10, r11, r12, r13);
        r8 = r19;
        r8.add(r7);
        goto L_0x01b7;
    L_0x01b5:
        r8 = r19;
    L_0x01b7:
        r0 = r0 + 1;
        goto L_0x00e1;
    L_0x01bb:
        r21.countDown();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.TgChooserTargetService.lambda$onGetChooserTargets$0$TgChooserTargetService(int, java.util.List, android.content.ComponentName, java.util.concurrent.CountDownLatch):void");
    }

    private Icon createRoundBitmap(File file) {
        try {
            Bitmap decodeFile = BitmapFactory.decodeFile(file.toString());
            if (decodeFile != null) {
                Bitmap createBitmap = Bitmap.createBitmap(decodeFile.getWidth(), decodeFile.getHeight(), Config.ARGB_8888);
                createBitmap.eraseColor(0);
                Canvas canvas = new Canvas(createBitmap);
                BitmapShader bitmapShader = new BitmapShader(decodeFile, TileMode.CLAMP, TileMode.CLAMP);
                if (this.roundPaint == null) {
                    this.roundPaint = new Paint(1);
                    this.bitmapRect = new RectF();
                }
                this.roundPaint.setShader(bitmapShader);
                this.bitmapRect.set(0.0f, 0.0f, (float) decodeFile.getWidth(), (float) decodeFile.getHeight());
                canvas.drawRoundRect(this.bitmapRect, (float) decodeFile.getWidth(), (float) decodeFile.getHeight(), this.roundPaint);
                return Icon.createWithBitmap(createBitmap);
            }
        } catch (Throwable th) {
            FileLog.e(th);
        }
        return null;
    }

    private Icon createSavedMessagesIcon() {
        try {
            Bitmap createBitmap = Bitmap.createBitmap(AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f), Config.ARGB_8888);
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setAvatarType(1);
            avatarDrawable.setBounds(0, 0, createBitmap.getWidth(), createBitmap.getHeight());
            avatarDrawable.draw(new Canvas(createBitmap));
            return Icon.createWithBitmap(createBitmap);
        } catch (Throwable th) {
            FileLog.e(th);
            return null;
        }
    }
}
