package org.telegram.ui;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ChatActivity$cAumT2eyR1Hd6g6TboDLH1ovRbk implements OnTouchListener {
    private final /* synthetic */ ChatActivity f$0;
    private final /* synthetic */ View f$1;
    private final /* synthetic */ Rect f$2;

    public /* synthetic */ -$$Lambda$ChatActivity$cAumT2eyR1Hd6g6TboDLH1ovRbk(ChatActivity chatActivity, View view, Rect rect) {
        this.f$0 = chatActivity;
        this.f$1 = view;
        this.f$2 = rect;
    }

    public final boolean onTouch(View view, MotionEvent motionEvent) {
        return this.f$0.lambda$createMenu$62$ChatActivity(this.f$1, this.f$2, view, motionEvent);
    }
}
