package org.telegram.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.android.search.verification.client.SearchActionVerificationClientService;
import org.telegram.tgnet.TLRPC.User;

public class GoogleVoiceClientService extends SearchActionVerificationClientService {
    public void performAction(Intent intent, boolean z, Bundle bundle) {
        if (z) {
            AndroidUtilities.runOnUIThread(new -$$Lambda$GoogleVoiceClientService$VV-KGUoAfPyufdZvgocPLqrA5Dk(intent));
        }
    }

    static /* synthetic */ void lambda$performAction$0(Intent intent) {
        try {
            int i = UserConfig.selectedAccount;
            ApplicationLoader.postInitApplication();
            if (!AndroidUtilities.needShowPasscode(false)) {
                if (!SharedConfig.isWaitingForPasscodeEnter) {
                    String stringExtra = intent.getStringExtra("android.intent.extra.TEXT");
                    if (!TextUtils.isEmpty(stringExtra)) {
                        String stringExtra2 = intent.getStringExtra("com.google.android.voicesearch.extra.RECIPIENT_CONTACT_URI");
                        int parseInt = Integer.parseInt(intent.getStringExtra("com.google.android.voicesearch.extra.RECIPIENT_CONTACT_CHAT_ID"));
                        User user = MessagesController.getInstance(i).getUser(Integer.valueOf(parseInt));
                        if (user == null) {
                            user = MessagesStorage.getInstance(i).getUserSync(parseInt);
                            if (user != null) {
                                MessagesController.getInstance(i).putUser(user, true);
                            }
                        }
                        if (user != null) {
                            ContactsController.getInstance(i).markAsContacted(stringExtra2);
                            SendMessagesHelper.getInstance(i).sendMessage(stringExtra, (long) user.id, null, null, true, null, null, null, true, 0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }
}
