package org.telegram.ui;

import java.util.Comparator;
import org.telegram.tgnet.TLRPC.ChannelParticipant;

final /* synthetic */ class ChatUsersActivity$$Lambda$13 implements Comparator {
    private final ChatUsersActivity arg$1;

    ChatUsersActivity$$Lambda$13(ChatUsersActivity chatUsersActivity) {
        this.arg$1 = chatUsersActivity;
    }

    public int compare(Object obj, Object obj2) {
        return this.arg$1.lambda$null$20$ChatUsersActivity((ChannelParticipant) obj, (ChannelParticipant) obj2);
    }
}
