package org.telegram.ui;

import android.util.SparseArray;
import java.util.ArrayList;
import java.util.HashMap;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.Adapters.SearchAdapterHelper.HashtagObject;
import org.telegram.ui.Adapters.SearchAdapterHelper.SearchAdapterHelperDelegate;
import org.telegram.ui.Adapters.SearchAdapterHelper.SearchAdapterHelperDelegate.-CC;
import org.telegram.ui.GroupCreateActivity.GroupCreateAdapter;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$GroupCreateActivity$GroupCreateAdapter$E5cKkbpq2FJnJZ0QVy8Qf2zhCrM implements SearchAdapterHelperDelegate {
    private final /* synthetic */ GroupCreateAdapter f$0;

    public /* synthetic */ -$$Lambda$GroupCreateActivity$GroupCreateAdapter$E5cKkbpq2FJnJZ0QVy8Qf2zhCrM(GroupCreateAdapter groupCreateAdapter) {
        this.f$0 = groupCreateAdapter;
    }

    public /* synthetic */ SparseArray<User> getExcludeUsers() {
        return -CC.$default$getExcludeUsers(this);
    }

    public final void onDataSetChanged() {
        this.f$0.lambda$new$0$GroupCreateActivity$GroupCreateAdapter();
    }

    public /* synthetic */ void onSetHashtags(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap) {
        -CC.$default$onSetHashtags(this, arrayList, hashMap);
    }
}
