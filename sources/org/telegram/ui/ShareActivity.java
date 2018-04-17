package org.telegram.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.beta.R;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.ui.Components.ShareAlert;

public class ShareActivity extends Activity {
    private Dialog visibleDialog;

    /* renamed from: org.telegram.ui.ShareActivity$1 */
    class C16951 implements OnDismissListener {
        C16951() {
        }

        public void onDismiss(DialogInterface dialog) {
            if (!ShareActivity.this.isFinishing()) {
                ShareActivity.this.finish();
            }
            ShareActivity.this.visibleDialog = null;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        ApplicationLoader.postInitApplication();
        AndroidUtilities.checkDisplaySize(this, getResources().getConfiguration());
        requestWindowFeature(1);
        setTheme(R.style.Theme.TMessages.Transparent);
        super.onCreate(savedInstanceState);
        setContentView(new View(this), new LayoutParams(-1, -1));
        Intent intent = getIntent();
        if (intent != null && "android.intent.action.VIEW".equals(intent.getAction())) {
            if (intent.getData() != null) {
                Uri data = intent.getData();
                String scheme = data.getScheme();
                String url = data.toString();
                String hash = data.getQueryParameter("hash");
                if ("tgb".equals(scheme) && url.toLowerCase().startsWith("tgb://share_game_score")) {
                    if (!TextUtils.isEmpty(hash)) {
                        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("botshare", 0);
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(hash);
                        stringBuilder.append("_m");
                        String message = sharedPreferences.getString(stringBuilder.toString(), null);
                        if (TextUtils.isEmpty(message)) {
                            finish();
                            return;
                        }
                        SerializedData serializedData = new SerializedData(Utilities.hexToBytes(message));
                        Message mess = Message.TLdeserialize(serializedData, serializedData.readInt32(false), false);
                        mess.readAttachPath(serializedData, 0);
                        if (mess == null) {
                            finish();
                            return;
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(hash);
                        stringBuilder.append("_link");
                        String link = sharedPreferences.getString(stringBuilder.toString(), null);
                        MessageObject messageObject = new MessageObject(UserConfig.selectedAccount, mess, false);
                        messageObject.messageOwner.with_my_score = true;
                        try {
                            r7.visibleDialog = ShareAlert.createShareAlert(r7, messageObject, null, false, link, null);
                            r7.visibleDialog.setCanceledOnTouchOutside(true);
                            r7.visibleDialog.setOnDismissListener(new C16951());
                            r7.visibleDialog.show();
                        } catch (Throwable e) {
                            FileLog.m3e(e);
                            finish();
                        }
                        return;
                    }
                }
                finish();
                return;
            }
        }
        finish();
    }

    public void onPause() {
        super.onPause();
        try {
            if (this.visibleDialog != null && this.visibleDialog.isShowing()) {
                this.visibleDialog.dismiss();
                this.visibleDialog = null;
            }
        } catch (Throwable e) {
            FileLog.m3e(e);
        }
    }
}
