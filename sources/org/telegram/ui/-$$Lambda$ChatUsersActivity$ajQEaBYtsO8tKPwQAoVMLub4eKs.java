package org.telegram.ui;

import java.util.Comparator;
import org.telegram.tgnet.TLObject;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ChatUsersActivity$ajQEaBYtsO8tKPwQAoVMLub4eKs implements Comparator {
    private final /* synthetic */ ChatUsersActivity f$0;

    public /* synthetic */ -$$Lambda$ChatUsersActivity$ajQEaBYtsO8tKPwQAoVMLub4eKs(ChatUsersActivity chatUsersActivity) {
        this.f$0 = chatUsersActivity;
    }

    public final int compare(Object obj, Object obj2) {
        return this.f$0.lambda$onOwnerChaged$4$ChatUsersActivity((TLObject) obj, (TLObject) obj2);
    }
}
