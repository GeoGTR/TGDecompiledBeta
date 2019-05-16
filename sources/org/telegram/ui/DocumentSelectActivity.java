package org.telegram.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.SharedDocumentCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class DocumentSelectActivity extends BaseFragment {
    private static final int done = 3;
    private ArrayList<View> actionModeViews = new ArrayList();
    private boolean allowMusic;
    private boolean canSelectOnlyImageFiles;
    private File currentDir;
    private DocumentSelectActivityDelegate delegate;
    private EmptyTextProgressView emptyView;
    private ArrayList<HistoryEntry> history = new ArrayList();
    private ArrayList<ListItem> items = new ArrayList();
    private LinearLayoutManager layoutManager;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private int maxSelectedFiles = -1;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            -$$Lambda$DocumentSelectActivity$1$QUHI_wOoyDp1Dwx1fGWii_dVi9g -__lambda_documentselectactivity_1_quhi_wooydp1dwx1fgwii_dvi9g = new -$$Lambda$DocumentSelectActivity$1$QUHI_wOoyDp1Dwx1fGWii_dVi9g(this);
            if ("android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction())) {
                DocumentSelectActivity.this.listView.postDelayed(-__lambda_documentselectactivity_1_quhi_wooydp1dwx1fgwii_dvi9g, 1000);
            } else {
                -__lambda_documentselectactivity_1_quhi_wooydp1dwx1fgwii_dvi9g.run();
            }
        }

        public /* synthetic */ void lambda$onReceive$0$DocumentSelectActivity$1() {
            try {
                if (DocumentSelectActivity.this.currentDir == null) {
                    DocumentSelectActivity.this.listRoots();
                } else {
                    DocumentSelectActivity.this.listFiles(DocumentSelectActivity.this.currentDir);
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    };
    private boolean receiverRegistered = false;
    private ArrayList<ListItem> recentItems = new ArrayList();
    private boolean scrolling;
    private HashMap<String, ListItem> selectedFiles = new HashMap();
    private NumberTextView selectedMessagesCountTextView;
    private long sizeLimit = NUM;

    public interface DocumentSelectActivityDelegate {

        public final /* synthetic */ class -CC {
            public static void $default$startMusicSelectActivity(DocumentSelectActivityDelegate documentSelectActivityDelegate, BaseFragment baseFragment) {
            }
        }

        void didSelectFiles(DocumentSelectActivity documentSelectActivity, ArrayList<String> arrayList);

        void startDocumentSelectActivity();

        void startMusicSelectActivity(BaseFragment baseFragment);
    }

    private class HistoryEntry {
        File dir;
        int scrollItem;
        int scrollOffset;
        String title;

        private HistoryEntry() {
        }

        /* synthetic */ HistoryEntry(DocumentSelectActivity documentSelectActivity, AnonymousClass1 anonymousClass1) {
            this();
        }
    }

    private class ListItem {
        long date;
        String ext;
        File file;
        int icon;
        String subtitle;
        String thumb;
        String title;

        private ListItem() {
            String str = "";
            this.subtitle = str;
            this.ext = str;
        }

        /* synthetic */ ListItem(DocumentSelectActivity documentSelectActivity, AnonymousClass1 anonymousClass1) {
            this();
        }
    }

    private class ListAdapter extends SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean isEnabled(ViewHolder viewHolder) {
            return viewHolder.getItemViewType() != 0;
        }

        public int getItemCount() {
            int size = DocumentSelectActivity.this.items.size();
            return (!DocumentSelectActivity.this.history.isEmpty() || DocumentSelectActivity.this.recentItems.isEmpty()) ? size : size + (DocumentSelectActivity.this.recentItems.size() + 1);
        }

        public ListItem getItem(int i) {
            if (i < DocumentSelectActivity.this.items.size()) {
                return (ListItem) DocumentSelectActivity.this.items.get(i);
            }
            if (!(!DocumentSelectActivity.this.history.isEmpty() || DocumentSelectActivity.this.recentItems.isEmpty() || i == DocumentSelectActivity.this.items.size())) {
                i -= DocumentSelectActivity.this.items.size() + 1;
                if (i < DocumentSelectActivity.this.recentItems.size()) {
                    return (ListItem) DocumentSelectActivity.this.recentItems.get(i);
                }
            }
            return null;
        }

        public int getItemViewType(int i) {
            return getItem(i) != null ? 1 : 0;
        }

        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View sharedDocumentCell;
            if (i != 0) {
                sharedDocumentCell = new SharedDocumentCell(this.mContext);
            } else {
                sharedDocumentCell = new GraySectionCell(this.mContext);
                sharedDocumentCell.setText(LocaleController.getString("Recent", NUM));
            }
            return new Holder(sharedDocumentCell);
        }

        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            if (viewHolder.getItemViewType() == 1) {
                ListItem item = getItem(i);
                SharedDocumentCell sharedDocumentCell = (SharedDocumentCell) viewHolder.itemView;
                int i2 = item.icon;
                if (i2 != 0) {
                    sharedDocumentCell.setTextAndValueAndTypeAndThumb(item.title, item.subtitle, null, null, i2);
                } else {
                    SharedDocumentCell sharedDocumentCell2 = sharedDocumentCell;
                    sharedDocumentCell2.setTextAndValueAndTypeAndThumb(item.title, item.subtitle, item.ext.toUpperCase().substring(0, Math.min(item.ext.length(), 4)), item.thumb, 0);
                }
                if (item.file == null || !DocumentSelectActivity.this.actionBar.isActionModeShowed()) {
                    sharedDocumentCell.setChecked(false, DocumentSelectActivity.this.scrolling ^ 1);
                } else {
                    sharedDocumentCell.setChecked(DocumentSelectActivity.this.selectedFiles.containsKey(item.file.toString()), DocumentSelectActivity.this.scrolling ^ 1);
                }
            }
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:94:0x022a in {3, 6, 7, 8, 17, 20, 23, 42, 43, 48, 49, 51, 55, 58, 60, 62, 64, 70, 72, 78, 80, 83, 85, 86, 90, 92, 93} preds:[]
        	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:242)
        	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
        	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:42)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1257)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    @android.annotation.SuppressLint({"NewApi"})
    private void listRoots() {
        /*
        r11 = this;
        r0 = "Telegram";
        r1 = 0;
        r11.currentDir = r1;
        r2 = r11.items;
        r2.clear();
        r2 = new java.util.HashSet;
        r2.<init>();
        r3 = android.os.Environment.getExternalStorageDirectory();
        r3 = r3.getPath();
        android.os.Environment.isExternalStorageRemovable();
        r4 = android.os.Environment.getExternalStorageState();
        r5 = "mounted";
        r5 = r4.equals(r5);
        r6 = NUM; // 0x7var_ float:1.7945136E38 double:1.052935639E-314;
        r7 = NUM; // 0x7f0d088c float:1.8746553E38 double:1.0531308586E-314;
        r8 = "SdCard";
        if (r5 != 0) goto L_0x0036;
        r5 = "mounted_ro";
        r4 = r4.equals(r5);
        if (r4 == 0) goto L_0x006e;
        r4 = new org.telegram.ui.DocumentSelectActivity$ListItem;
        r4.<init>(r11, r1);
        r5 = android.os.Environment.isExternalStorageRemovable();
        if (r5 == 0) goto L_0x004a;
        r5 = org.telegram.messenger.LocaleController.getString(r8, r7);
        r4.title = r5;
        r4.icon = r6;
        goto L_0x005a;
        r5 = NUM; // 0x7f0d04c9 float:1.87446E38 double:1.053130383E-314;
        r9 = "InternalStorage";
        r5 = org.telegram.messenger.LocaleController.getString(r9, r5);
        r4.title = r5;
        r5 = NUM; // 0x7var_f float:1.7945193E38 double:1.0529356527E-314;
        r4.icon = r5;
        r5 = r11.getRootSubtitle(r3);
        r4.subtitle = r5;
        r5 = android.os.Environment.getExternalStorageDirectory();
        r4.file = r5;
        r5 = r11.items;
        r5.add(r4);
        r2.add(r3);
        r3 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x0160, all -> 0x015c }
        r4 = new java.io.FileReader;	 Catch:{ Exception -> 0x0160, all -> 0x015c }
        r5 = "/proc/mounts";	 Catch:{ Exception -> 0x0160, all -> 0x015c }
        r4.<init>(r5);	 Catch:{ Exception -> 0x0160, all -> 0x015c }
        r3.<init>(r4);	 Catch:{ Exception -> 0x0160, all -> 0x015c }
        r4 = r3.readLine();	 Catch:{ Exception -> 0x015a }
        if (r4 == 0) goto L_0x0156;	 Catch:{ Exception -> 0x015a }
        r5 = "vfat";	 Catch:{ Exception -> 0x015a }
        r5 = r4.contains(r5);	 Catch:{ Exception -> 0x015a }
        if (r5 != 0) goto L_0x0090;	 Catch:{ Exception -> 0x015a }
        r5 = "/mnt";	 Catch:{ Exception -> 0x015a }
        r5 = r4.contains(r5);	 Catch:{ Exception -> 0x015a }
        if (r5 == 0) goto L_0x007a;	 Catch:{ Exception -> 0x015a }
        r5 = org.telegram.messenger.BuildVars.LOGS_ENABLED;	 Catch:{ Exception -> 0x015a }
        if (r5 == 0) goto L_0x0097;	 Catch:{ Exception -> 0x015a }
        org.telegram.messenger.FileLog.d(r4);	 Catch:{ Exception -> 0x015a }
        r5 = new java.util.StringTokenizer;	 Catch:{ Exception -> 0x015a }
        r9 = " ";	 Catch:{ Exception -> 0x015a }
        r5.<init>(r4, r9);	 Catch:{ Exception -> 0x015a }
        r5.nextToken();	 Catch:{ Exception -> 0x015a }
        r5 = r5.nextToken();	 Catch:{ Exception -> 0x015a }
        r9 = r2.contains(r5);	 Catch:{ Exception -> 0x015a }
        if (r9 == 0) goto L_0x00ac;	 Catch:{ Exception -> 0x015a }
        goto L_0x007a;	 Catch:{ Exception -> 0x015a }
        r9 = "/dev/block/vold";	 Catch:{ Exception -> 0x015a }
        r9 = r4.contains(r9);	 Catch:{ Exception -> 0x015a }
        if (r9 == 0) goto L_0x007a;	 Catch:{ Exception -> 0x015a }
        r9 = "/mnt/secure";	 Catch:{ Exception -> 0x015a }
        r9 = r4.contains(r9);	 Catch:{ Exception -> 0x015a }
        if (r9 != 0) goto L_0x007a;	 Catch:{ Exception -> 0x015a }
        r9 = "/mnt/asec";	 Catch:{ Exception -> 0x015a }
        r9 = r4.contains(r9);	 Catch:{ Exception -> 0x015a }
        if (r9 != 0) goto L_0x007a;	 Catch:{ Exception -> 0x015a }
        r9 = "/mnt/obb";	 Catch:{ Exception -> 0x015a }
        r9 = r4.contains(r9);	 Catch:{ Exception -> 0x015a }
        if (r9 != 0) goto L_0x007a;	 Catch:{ Exception -> 0x015a }
        r9 = "/dev/mapper";	 Catch:{ Exception -> 0x015a }
        r9 = r4.contains(r9);	 Catch:{ Exception -> 0x015a }
        if (r9 != 0) goto L_0x007a;	 Catch:{ Exception -> 0x015a }
        r9 = "tmpfs";	 Catch:{ Exception -> 0x015a }
        r4 = r4.contains(r9);	 Catch:{ Exception -> 0x015a }
        if (r4 != 0) goto L_0x007a;	 Catch:{ Exception -> 0x015a }
        r4 = new java.io.File;	 Catch:{ Exception -> 0x015a }
        r4.<init>(r5);	 Catch:{ Exception -> 0x015a }
        r4 = r4.isDirectory();	 Catch:{ Exception -> 0x015a }
        if (r4 != 0) goto L_0x0113;	 Catch:{ Exception -> 0x015a }
        r4 = 47;	 Catch:{ Exception -> 0x015a }
        r4 = r5.lastIndexOf(r4);	 Catch:{ Exception -> 0x015a }
        r9 = -1;	 Catch:{ Exception -> 0x015a }
        if (r4 == r9) goto L_0x0113;	 Catch:{ Exception -> 0x015a }
        r9 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x015a }
        r9.<init>();	 Catch:{ Exception -> 0x015a }
        r10 = "/storage/";	 Catch:{ Exception -> 0x015a }
        r9.append(r10);	 Catch:{ Exception -> 0x015a }
        r4 = r4 + 1;	 Catch:{ Exception -> 0x015a }
        r4 = r5.substring(r4);	 Catch:{ Exception -> 0x015a }
        r9.append(r4);	 Catch:{ Exception -> 0x015a }
        r4 = r9.toString();	 Catch:{ Exception -> 0x015a }
        r9 = new java.io.File;	 Catch:{ Exception -> 0x015a }
        r9.<init>(r4);	 Catch:{ Exception -> 0x015a }
        r9 = r9.isDirectory();	 Catch:{ Exception -> 0x015a }
        if (r9 == 0) goto L_0x0113;	 Catch:{ Exception -> 0x015a }
        goto L_0x0114;	 Catch:{ Exception -> 0x015a }
        r4 = r5;	 Catch:{ Exception -> 0x015a }
        r2.add(r4);	 Catch:{ Exception -> 0x015a }
        r5 = new org.telegram.ui.DocumentSelectActivity$ListItem;	 Catch:{ Exception -> 0x0150 }
        r5.<init>(r11, r1);	 Catch:{ Exception -> 0x0150 }
        r9 = r4.toLowerCase();	 Catch:{ Exception -> 0x0150 }
        r10 = "sd";	 Catch:{ Exception -> 0x0150 }
        r9 = r9.contains(r10);	 Catch:{ Exception -> 0x0150 }
        if (r9 == 0) goto L_0x012f;	 Catch:{ Exception -> 0x0150 }
        r9 = org.telegram.messenger.LocaleController.getString(r8, r7);	 Catch:{ Exception -> 0x0150 }
        r5.title = r9;	 Catch:{ Exception -> 0x0150 }
        goto L_0x013a;	 Catch:{ Exception -> 0x0150 }
        r9 = "ExternalStorage";	 Catch:{ Exception -> 0x0150 }
        r10 = NUM; // 0x7f0d0407 float:1.8744206E38 double:1.053130287E-314;	 Catch:{ Exception -> 0x0150 }
        r9 = org.telegram.messenger.LocaleController.getString(r9, r10);	 Catch:{ Exception -> 0x0150 }
        r5.title = r9;	 Catch:{ Exception -> 0x0150 }
        r5.icon = r6;	 Catch:{ Exception -> 0x0150 }
        r9 = r11.getRootSubtitle(r4);	 Catch:{ Exception -> 0x0150 }
        r5.subtitle = r9;	 Catch:{ Exception -> 0x0150 }
        r9 = new java.io.File;	 Catch:{ Exception -> 0x0150 }
        r9.<init>(r4);	 Catch:{ Exception -> 0x0150 }
        r5.file = r9;	 Catch:{ Exception -> 0x0150 }
        r4 = r11.items;	 Catch:{ Exception -> 0x0150 }
        r4.add(r5);	 Catch:{ Exception -> 0x0150 }
        goto L_0x007a;
        r4 = move-exception;
        org.telegram.messenger.FileLog.e(r4);	 Catch:{ Exception -> 0x015a }
        goto L_0x007a;
        r3.close();	 Catch:{ Exception -> 0x016b }
        goto L_0x016f;
        r2 = move-exception;
        goto L_0x0162;
        r0 = move-exception;
        r3 = r1;
        goto L_0x021f;
        r2 = move-exception;
        r3 = r1;
        org.telegram.messenger.FileLog.e(r2);	 Catch:{ all -> 0x021e }
        if (r3 == 0) goto L_0x016f;
        r3.close();	 Catch:{ Exception -> 0x016b }
        goto L_0x016f;
        r2 = move-exception;
        org.telegram.messenger.FileLog.e(r2);
        r2 = new org.telegram.ui.DocumentSelectActivity$ListItem;
        r2.<init>(r11, r1);
        r3 = "/";
        r2.title = r3;
        r4 = NUM; // 0x7f0d0961 float:1.8746985E38 double:1.053130964E-314;
        r5 = "SystemRoot";
        r4 = org.telegram.messenger.LocaleController.getString(r5, r4);
        r2.subtitle = r4;
        r4 = NUM; // 0x7var_ float:1.7945132E38 double:1.052935638E-314;
        r2.icon = r4;
        r5 = new java.io.File;
        r5.<init>(r3);
        r2.file = r5;
        r3 = r11.items;
        r3.add(r2);
        r2 = new java.io.File;	 Catch:{ Exception -> 0x01ba }
        r3 = android.os.Environment.getExternalStorageDirectory();	 Catch:{ Exception -> 0x01ba }
        r2.<init>(r3, r0);	 Catch:{ Exception -> 0x01ba }
        r3 = r2.exists();	 Catch:{ Exception -> 0x01ba }
        if (r3 == 0) goto L_0x01be;	 Catch:{ Exception -> 0x01ba }
        r3 = new org.telegram.ui.DocumentSelectActivity$ListItem;	 Catch:{ Exception -> 0x01ba }
        r3.<init>(r11, r1);	 Catch:{ Exception -> 0x01ba }
        r3.title = r0;	 Catch:{ Exception -> 0x01ba }
        r0 = r2.toString();	 Catch:{ Exception -> 0x01ba }
        r3.subtitle = r0;	 Catch:{ Exception -> 0x01ba }
        r3.icon = r4;	 Catch:{ Exception -> 0x01ba }
        r3.file = r2;	 Catch:{ Exception -> 0x01ba }
        r0 = r11.items;	 Catch:{ Exception -> 0x01ba }
        r0.add(r3);	 Catch:{ Exception -> 0x01ba }
        goto L_0x01be;
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
        r0 = new org.telegram.ui.DocumentSelectActivity$ListItem;
        r0.<init>(r11, r1);
        r2 = NUM; // 0x7f0d0474 float:1.8744427E38 double:1.053130341E-314;
        r3 = "Gallery";
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r0.title = r2;
        r2 = NUM; // 0x7f0d0475 float:1.8744429E38 double:1.0531303413E-314;
        r3 = "GalleryInfo";
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r0.subtitle = r2;
        r2 = NUM; // 0x7var_ float:1.7945195E38 double:1.052935653E-314;
        r0.icon = r2;
        r0.file = r1;
        r2 = r11.items;
        r2.add(r0);
        r0 = r11.allowMusic;
        if (r0 == 0) goto L_0x0210;
        r0 = new org.telegram.ui.DocumentSelectActivity$ListItem;
        r0.<init>(r11, r1);
        r2 = NUM; // 0x7f0d0141 float:1.8742766E38 double:1.053129936E-314;
        r3 = "AttachMusic";
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r0.title = r2;
        r2 = NUM; // 0x7f0d059e float:1.8745031E38 double:1.053130488E-314;
        r3 = "MusicInfo";
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r0.subtitle = r2;
        r2 = NUM; // 0x7var_ float:1.7945197E38 double:1.0529356537E-314;
        r0.icon = r2;
        r0.file = r1;
        r1 = r11.items;
        r1.add(r0);
        r0 = r11.listView;
        org.telegram.messenger.AndroidUtilities.clearDrawableAnimation(r0);
        r0 = 1;
        r11.scrolling = r0;
        r0 = r11.listAdapter;
        r0.notifyDataSetChanged();
        return;
        r0 = move-exception;
        if (r3 == 0) goto L_0x0229;
        r3.close();	 Catch:{ Exception -> 0x0225 }
        goto L_0x0229;
        r1 = move-exception;
        org.telegram.messenger.FileLog.e(r1);
        throw r0;
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DocumentSelectActivity.listRoots():void");
    }

    public DocumentSelectActivity(boolean z) {
        this.allowMusic = z;
    }

    public boolean onFragmentCreate() {
        loadRecentFiles();
        return super.onFragmentCreate();
    }

    public void onFragmentDestroy() {
        try {
            if (this.receiverRegistered) {
                ApplicationLoader.applicationContext.unregisterReceiver(this.receiver);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        super.onFragmentDestroy();
    }

    public View createView(Context context) {
        if (!this.receiverRegistered) {
            this.receiverRegistered = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
            intentFilter.addAction("android.intent.action.MEDIA_CHECKING");
            intentFilter.addAction("android.intent.action.MEDIA_EJECT");
            intentFilter.addAction("android.intent.action.MEDIA_MOUNTED");
            intentFilter.addAction("android.intent.action.MEDIA_NOFS");
            intentFilter.addAction("android.intent.action.MEDIA_REMOVED");
            intentFilter.addAction("android.intent.action.MEDIA_SHARED");
            intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTABLE");
            intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
            intentFilter.addDataScheme("file");
            ApplicationLoader.applicationContext.registerReceiver(this.receiver, intentFilter);
        }
        this.actionBar.setBackButtonDrawable(new BackDrawable(false));
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("SelectFile", NUM));
        this.actionBar.setActionBarMenuOnItemClick(new ActionBarMenuOnItemClick() {
            public void onItemClick(int i) {
                if (i == -1) {
                    if (DocumentSelectActivity.this.actionBar.isActionModeShowed()) {
                        DocumentSelectActivity.this.selectedFiles.clear();
                        DocumentSelectActivity.this.actionBar.hideActionMode();
                        i = DocumentSelectActivity.this.listView.getChildCount();
                        for (int i2 = 0; i2 < i; i2++) {
                            View childAt = DocumentSelectActivity.this.listView.getChildAt(i2);
                            if (childAt instanceof SharedDocumentCell) {
                                ((SharedDocumentCell) childAt).setChecked(false, true);
                            }
                        }
                        return;
                    }
                    DocumentSelectActivity.this.finishFragment();
                } else if (i == 3 && DocumentSelectActivity.this.delegate != null) {
                    DocumentSelectActivity.this.delegate.didSelectFiles(DocumentSelectActivity.this, new ArrayList(DocumentSelectActivity.this.selectedFiles.keySet()));
                    for (ListItem listItem : DocumentSelectActivity.this.selectedFiles.values()) {
                        listItem.date = System.currentTimeMillis();
                    }
                }
            }
        });
        this.selectedFiles.clear();
        this.actionModeViews.clear();
        ActionBarMenu createActionMode = this.actionBar.createActionMode();
        this.selectedMessagesCountTextView = new NumberTextView(createActionMode.getContext());
        this.selectedMessagesCountTextView.setTextSize(18);
        this.selectedMessagesCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.selectedMessagesCountTextView.setTextColor(Theme.getColor("actionBarActionModeDefaultIcon"));
        this.selectedMessagesCountTextView.setOnTouchListener(-$$Lambda$DocumentSelectActivity$wOAW0__TUXtkjQ-Z0vg8d4FmxpE.INSTANCE);
        createActionMode.addView(this.selectedMessagesCountTextView, LayoutHelper.createLinear(0, -1, 1.0f, 65, 0, 0, 0));
        this.actionModeViews.add(createActionMode.addItemWithWidth(3, NUM, AndroidUtilities.dp(54.0f)));
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) this.fragmentView;
        this.emptyView = new EmptyTextProgressView(context);
        this.emptyView.showTextView();
        frameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0f));
        this.listView = new RecyclerListView(context);
        this.listView.setVerticalScrollBarEnabled(false);
        RecyclerListView recyclerListView = this.listView;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, 1, false);
        this.layoutManager = linearLayoutManager;
        recyclerListView.setLayoutManager(linearLayoutManager);
        this.listView.setEmptyView(this.emptyView);
        RecyclerListView recyclerListView2 = this.listView;
        ListAdapter listAdapter = new ListAdapter(context);
        this.listAdapter = listAdapter;
        recyclerListView2.setAdapter(listAdapter);
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0f));
        this.listView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                DocumentSelectActivity.this.scrolling = i != 0;
            }
        });
        this.listView.setOnItemLongClickListener(new -$$Lambda$DocumentSelectActivity$CyKwYdaVewVAaKBAnA3V_bH1l5E(this));
        this.listView.setOnItemClickListener(new -$$Lambda$DocumentSelectActivity$lBQknjeUipxjA7znufbDrCLASSNAMELRU(this));
        listRoots();
        return this.fragmentView;
    }

    public /* synthetic */ boolean lambda$createView$1$DocumentSelectActivity(View view, int i) {
        if (this.actionBar.isActionModeShowed()) {
            return false;
        }
        ListItem item = this.listAdapter.getItem(i);
        if (item == null) {
            return false;
        }
        File file = item.file;
        if (!(file == null || file.isDirectory())) {
            if (!file.canRead()) {
                showErrorBox(LocaleController.getString("AccessError", NUM));
                return false;
            } else if (this.canSelectOnlyImageFiles && item.thumb == null) {
                showErrorBox(LocaleController.formatString("PassportUploadNotImage", NUM, new Object[0]));
                return false;
            } else {
                if (this.sizeLimit != 0) {
                    if (file.length() > this.sizeLimit) {
                        showErrorBox(LocaleController.formatString("FileUploadLimit", NUM, AndroidUtilities.formatFileSize(this.sizeLimit)));
                        return false;
                    }
                }
                if (this.maxSelectedFiles >= 0 && this.selectedFiles.size() >= this.maxSelectedFiles) {
                    showErrorBox(LocaleController.formatString("PassportUploadMaxReached", NUM, LocaleController.formatPluralString("Files", this.maxSelectedFiles)));
                    return false;
                } else if (file.length() == 0) {
                    return false;
                } else {
                    this.selectedFiles.put(file.toString(), item);
                    this.selectedMessagesCountTextView.setNumber(1, false);
                    AnimatorSet animatorSet = new AnimatorSet();
                    ArrayList arrayList = new ArrayList();
                    for (int i2 = 0; i2 < this.actionModeViews.size(); i2++) {
                        View view2 = (View) this.actionModeViews.get(i2);
                        AndroidUtilities.clearDrawableAnimation(view2);
                        arrayList.add(ObjectAnimator.ofFloat(view2, "scaleY", new float[]{0.1f, 1.0f}));
                    }
                    animatorSet.playTogether(arrayList);
                    animatorSet.setDuration(250);
                    animatorSet.start();
                    this.scrolling = false;
                    if (view instanceof SharedDocumentCell) {
                        ((SharedDocumentCell) view).setChecked(true, true);
                    }
                    this.actionBar.showActionMode();
                }
            }
        }
        return true;
    }

    public /* synthetic */ void lambda$createView$2$DocumentSelectActivity(View view, int i) {
        ListItem item = this.listAdapter.getItem(i);
        if (item != null) {
            File file = item.file;
            ArrayList arrayList;
            HistoryEntry historyEntry;
            if (file == null) {
                int i2 = item.icon;
                DocumentSelectActivityDelegate documentSelectActivityDelegate;
                if (i2 == NUM) {
                    documentSelectActivityDelegate = this.delegate;
                    if (documentSelectActivityDelegate != null) {
                        documentSelectActivityDelegate.startDocumentSelectActivity();
                    }
                    finishFragment(false);
                } else if (i2 == NUM) {
                    documentSelectActivityDelegate = this.delegate;
                    if (documentSelectActivityDelegate != null) {
                        documentSelectActivityDelegate.startMusicSelectActivity(this);
                    }
                } else {
                    arrayList = this.history;
                    historyEntry = (HistoryEntry) arrayList.remove(arrayList.size() - 1);
                    this.actionBar.setTitle(historyEntry.title);
                    File file2 = historyEntry.dir;
                    if (file2 != null) {
                        listFiles(file2);
                    } else {
                        listRoots();
                    }
                    this.layoutManager.scrollToPositionWithOffset(historyEntry.scrollItem, historyEntry.scrollOffset);
                }
            } else if (file.isDirectory()) {
                historyEntry = new HistoryEntry(this, null);
                historyEntry.scrollItem = this.layoutManager.findLastVisibleItemPosition();
                View findViewByPosition = this.layoutManager.findViewByPosition(historyEntry.scrollItem);
                if (findViewByPosition != null) {
                    historyEntry.scrollOffset = findViewByPosition.getTop();
                }
                historyEntry.dir = this.currentDir;
                historyEntry.title = this.actionBar.getTitle();
                this.history.add(historyEntry);
                if (listFiles(file)) {
                    this.actionBar.setTitle(item.title);
                } else {
                    this.history.remove(historyEntry);
                }
            } else {
                if (!file.canRead()) {
                    showErrorBox(LocaleController.getString("AccessError", NUM));
                    file = new File("/mnt/sdcard");
                }
                if (this.canSelectOnlyImageFiles && item.thumb == null) {
                    showErrorBox(LocaleController.formatString("PassportUploadNotImage", NUM, new Object[0]));
                    return;
                }
                if (this.sizeLimit != 0) {
                    if (file.length() > this.sizeLimit) {
                        showErrorBox(LocaleController.formatString("FileUploadLimit", NUM, AndroidUtilities.formatFileSize(this.sizeLimit)));
                        return;
                    }
                }
                if (file.length() != 0) {
                    if (this.actionBar.isActionModeShowed()) {
                        if (this.selectedFiles.containsKey(file.toString())) {
                            this.selectedFiles.remove(file.toString());
                        } else if (this.maxSelectedFiles < 0 || this.selectedFiles.size() < this.maxSelectedFiles) {
                            this.selectedFiles.put(file.toString(), item);
                        } else {
                            showErrorBox(LocaleController.formatString("PassportUploadMaxReached", NUM, LocaleController.formatPluralString("Files", this.maxSelectedFiles)));
                            return;
                        }
                        if (this.selectedFiles.isEmpty()) {
                            this.actionBar.hideActionMode();
                        } else {
                            this.selectedMessagesCountTextView.setNumber(this.selectedFiles.size(), true);
                        }
                        this.scrolling = false;
                        if (view instanceof SharedDocumentCell) {
                            ((SharedDocumentCell) view).setChecked(this.selectedFiles.containsKey(item.file.toString()), true);
                        }
                    } else if (this.delegate != null) {
                        arrayList = new ArrayList();
                        arrayList.add(file.getAbsolutePath());
                        this.delegate.didSelectFiles(this, arrayList);
                    }
                }
            }
        }
    }

    public void setMaxSelectedFiles(int i) {
        this.maxSelectedFiles = i;
    }

    public void setCanSelectOnlyImageFiles(boolean z) {
        this.canSelectOnlyImageFiles = true;
    }

    public void loadRecentFiles() {
        try {
            File[] listFiles = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();
            for (File file : listFiles) {
                if (!file.isDirectory()) {
                    ListItem listItem = new ListItem(this, null);
                    listItem.title = file.getName();
                    listItem.file = file;
                    String name = file.getName();
                    String[] split = name.split("\\.");
                    listItem.ext = split.length > 1 ? split[split.length - 1] : "?";
                    listItem.subtitle = AndroidUtilities.formatFileSize(file.length());
                    name = name.toLowerCase();
                    if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".jpeg")) {
                        listItem.thumb = file.getAbsolutePath();
                    }
                    this.recentItems.add(listItem);
                }
            }
            Collections.sort(this.recentItems, -$$Lambda$DocumentSelectActivity$9fKm6qZG_Rnisjsmtz1XWQccmXA.INSTANCE);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    static /* synthetic */ int lambda$loadRecentFiles$3(ListItem listItem, ListItem listItem2) {
        long lastModified = listItem.file.lastModified();
        long lastModified2 = listItem2.file.lastModified();
        if (lastModified == lastModified2) {
            return 0;
        }
        return lastModified > lastModified2 ? -1 : 1;
    }

    public void onResume() {
        super.onResume();
        ListAdapter listAdapter = this.listAdapter;
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        fixLayoutInternal();
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        RecyclerListView recyclerListView = this.listView;
        if (recyclerListView != null) {
            recyclerListView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    DocumentSelectActivity.this.listView.getViewTreeObserver().removeOnPreDrawListener(this);
                    DocumentSelectActivity.this.fixLayoutInternal();
                    return true;
                }
            });
        }
    }

    private void fixLayoutInternal() {
        if (this.selectedMessagesCountTextView != null) {
            if (AndroidUtilities.isTablet() || ApplicationLoader.applicationContext.getResources().getConfiguration().orientation != 2) {
                this.selectedMessagesCountTextView.setTextSize(20);
            } else {
                this.selectedMessagesCountTextView.setTextSize(18);
            }
        }
    }

    public boolean onBackPressed() {
        if (this.history.size() <= 0) {
            return super.onBackPressed();
        }
        ArrayList arrayList = this.history;
        HistoryEntry historyEntry = (HistoryEntry) arrayList.remove(arrayList.size() - 1);
        this.actionBar.setTitle(historyEntry.title);
        File file = historyEntry.dir;
        if (file != null) {
            listFiles(file);
        } else {
            listRoots();
        }
        this.layoutManager.scrollToPositionWithOffset(historyEntry.scrollItem, historyEntry.scrollOffset);
        return false;
    }

    public void setDelegate(DocumentSelectActivityDelegate documentSelectActivityDelegate) {
        this.delegate = documentSelectActivityDelegate;
    }

    private boolean listFiles(File file) {
        if (file.canRead()) {
            try {
                File[] listFiles = file.listFiles();
                if (listFiles == null) {
                    showErrorBox(LocaleController.getString("UnknownError", NUM));
                    return false;
                }
                String str;
                this.currentDir = file;
                this.items.clear();
                Arrays.sort(listFiles, -$$Lambda$DocumentSelectActivity$KoBsEgVmPEJw-tYKCZKJ_jB7oso.INSTANCE);
                int i = 0;
                while (true) {
                    str = "Folder";
                    if (i >= listFiles.length) {
                        break;
                    }
                    File file2 = listFiles[i];
                    if (file2.getName().indexOf(46) != 0) {
                        ListItem listItem = new ListItem(this, null);
                        listItem.title = file2.getName();
                        listItem.file = file2;
                        if (file2.isDirectory()) {
                            listItem.icon = NUM;
                            listItem.subtitle = LocaleController.getString(str, NUM);
                        } else {
                            String name = file2.getName();
                            String[] split = name.split("\\.");
                            listItem.ext = split.length > 1 ? split[split.length - 1] : "?";
                            listItem.subtitle = AndroidUtilities.formatFileSize(file2.length());
                            name = name.toLowerCase();
                            if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".jpeg")) {
                                listItem.thumb = file2.getAbsolutePath();
                            }
                        }
                        this.items.add(listItem);
                    }
                    i++;
                }
                ListItem listItem2 = new ListItem(this, null);
                listItem2.title = "..";
                if (this.history.size() > 0) {
                    ArrayList arrayList = this.history;
                    File file3 = ((HistoryEntry) arrayList.get(arrayList.size() - 1)).dir;
                    if (file3 == null) {
                        listItem2.subtitle = LocaleController.getString(str, NUM);
                    } else {
                        listItem2.subtitle = file3.toString();
                    }
                } else {
                    listItem2.subtitle = LocaleController.getString(str, NUM);
                }
                listItem2.icon = NUM;
                listItem2.file = null;
                this.items.add(0, listItem2);
                AndroidUtilities.clearDrawableAnimation(this.listView);
                this.scrolling = true;
                this.listAdapter.notifyDataSetChanged();
                return true;
            } catch (Exception e) {
                showErrorBox(e.getLocalizedMessage());
                return false;
            }
        } else if ((!file.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().toString()) && !file.getAbsolutePath().startsWith("/sdcard") && !file.getAbsolutePath().startsWith("/mnt/sdcard")) || Environment.getExternalStorageState().equals("mounted") || Environment.getExternalStorageState().equals("mounted_ro")) {
            showErrorBox(LocaleController.getString("AccessError", NUM));
            return false;
        } else {
            this.currentDir = file;
            this.items.clear();
            if ("shared".equals(Environment.getExternalStorageState())) {
                this.emptyView.setText(LocaleController.getString("UsbActive", NUM));
            } else {
                this.emptyView.setText(LocaleController.getString("NotMounted", NUM));
            }
            AndroidUtilities.clearDrawableAnimation(this.listView);
            this.scrolling = true;
            this.listAdapter.notifyDataSetChanged();
            return true;
        }
    }

    static /* synthetic */ int lambda$listFiles$4(File file, File file2) {
        if (file.isDirectory() == file2.isDirectory()) {
            return file.getName().compareToIgnoreCase(file2.getName());
        }
        return file.isDirectory() ? -1 : 1;
    }

    private void showErrorBox(String str) {
        if (getParentActivity() != null) {
            new Builder(getParentActivity()).setTitle(LocaleController.getString("AppName", NUM)).setMessage(str).setPositiveButton(LocaleController.getString("OK", NUM), null).show();
        }
    }

    private String getRootSubtitle(String str) {
        try {
            StatFs statFs = new StatFs(str);
            long availableBlocks = ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
            if (((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize()) == 0) {
                return "";
            }
            return LocaleController.formatString("FreeOfTotal", NUM, AndroidUtilities.formatFileSize(availableBlocks), AndroidUtilities.formatFileSize(((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize())));
        } catch (Exception e) {
            FileLog.e(e);
            return str;
        }
    }

    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescription[] themeDescriptionArr = new ThemeDescription[22];
        themeDescriptionArr[0] = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite");
        themeDescriptionArr[1] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
        themeDescriptionArr[2] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
        themeDescriptionArr[3] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
        themeDescriptionArr[4] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
        themeDescriptionArr[5] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
        themeDescriptionArr[6] = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
        themeDescriptionArr[7] = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder");
        themeDescriptionArr[8] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_ITEMSCOLOR, null, null, null, null, "actionBarActionModeDefaultIcon");
        themeDescriptionArr[9] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_BACKGROUND, null, null, null, null, "actionBarActionModeDefault");
        themeDescriptionArr[10] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_TOPBACKGROUND, null, null, null, null, "actionBarActionModeDefaultTop");
        themeDescriptionArr[11] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_SELECTORCOLOR, null, null, null, null, "actionBarActionModeDefaultSelector");
        themeDescriptionArr[12] = new ThemeDescription(this.selectedMessagesCountTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "actionBarActionModeDefaultIcon");
        themeDescriptionArr[13] = new ThemeDescription(this.listView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, "key_graySectionText");
        themeDescriptionArr[14] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, "graySection");
        themeDescriptionArr[15] = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"nameTextView"}, null, null, null, "windowBackgroundWhiteBlackText");
        themeDescriptionArr[16] = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"dateTextView"}, null, null, null, "windowBackgroundWhiteGrayText3");
        View view = this.listView;
        int i = ThemeDescription.FLAG_CHECKBOX;
        Class[] clsArr = new Class[]{SharedDocumentCell.class};
        String[] strArr = new String[1];
        strArr[0] = "checkBox";
        themeDescriptionArr[17] = new ThemeDescription(view, i, clsArr, strArr, null, null, null, "checkbox");
        themeDescriptionArr[18] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{SharedDocumentCell.class}, new String[]{"checkBox"}, null, null, null, "checkboxCheck");
        view = this.listView;
        i = ThemeDescription.FLAG_IMAGECOLOR;
        clsArr = new Class[]{SharedDocumentCell.class};
        strArr = new String[1];
        strArr[0] = "thumbImageView";
        themeDescriptionArr[19] = new ThemeDescription(view, i, clsArr, strArr, null, null, null, "files_folderIcon");
        themeDescriptionArr[20] = new ThemeDescription(this.listView, ThemeDescription.FLAG_IMAGECOLOR | ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{SharedDocumentCell.class}, new String[]{"thumbImageView"}, null, null, null, "files_folderIconBackground");
        themeDescriptionArr[21] = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"extTextView"}, null, null, null, "files_iconText");
        return themeDescriptionArr;
    }
}
