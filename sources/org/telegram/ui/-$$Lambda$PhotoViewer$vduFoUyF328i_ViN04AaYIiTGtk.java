package org.telegram.ui;

import android.view.View;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$PhotoViewer$vduFoUyvar_i_ViN04AaYIiTGtk implements OnItemLongClickListener {
    private final /* synthetic */ PhotoViewer f$0;

    public /* synthetic */ -$$Lambda$PhotoViewer$vduFoUyvar_i_ViN04AaYIiTGtk(PhotoViewer photoViewer) {
        this.f$0 = photoViewer;
    }

    public final boolean onItemClick(View view, int i) {
        return this.f$0.lambda$setParentActivity$28$PhotoViewer(view, i);
    }
}
