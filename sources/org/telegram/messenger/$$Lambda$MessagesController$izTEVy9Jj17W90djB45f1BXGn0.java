package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

/* renamed from: org.telegram.messenger.-$$Lambda$MessagesController$izTEVy9Jj17W90djB45-f1BXGn0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MessagesController$izTEVy9Jj17W90djB45f1BXGn0 implements RequestDelegate {
    public static final /* synthetic */ $$Lambda$MessagesController$izTEVy9Jj17W90djB45f1BXGn0 INSTANCE = new $$Lambda$MessagesController$izTEVy9Jj17W90djB45f1BXGn0();

    private /* synthetic */ $$Lambda$MessagesController$izTEVy9Jj17W90djB45f1BXGn0() {
    }

    public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        MessagesController.lambda$markMessageContentAsRead$178(tLObject, tLRPC$TL_error);
    }
}
