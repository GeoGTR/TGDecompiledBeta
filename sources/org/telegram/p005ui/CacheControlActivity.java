package org.telegram.p005ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import java.io.File;
import java.util.ArrayList;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.CLASSNAMER;
import org.telegram.messenger.ClearCacheService;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.p005ui.ActionBar.AlertDialog;
import org.telegram.p005ui.ActionBar.BaseFragment;
import org.telegram.p005ui.ActionBar.BottomSheet.BottomSheetCell;
import org.telegram.p005ui.ActionBar.BottomSheet.Builder;
import org.telegram.p005ui.ActionBar.CLASSNAMEActionBar.ActionBarMenuOnItemClick;
import org.telegram.p005ui.ActionBar.Theme;
import org.telegram.p005ui.ActionBar.ThemeDescription;
import org.telegram.p005ui.Cells.CheckBoxCell;
import org.telegram.p005ui.Cells.TextInfoPrivacyCell;
import org.telegram.p005ui.Cells.TextSettingsCell;
import org.telegram.p005ui.Components.LayoutHelper;
import org.telegram.p005ui.Components.RecyclerListView;
import org.telegram.p005ui.Components.RecyclerListView.Holder;
import org.telegram.p005ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.p005ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLRPC.Message;

/* renamed from: org.telegram.ui.CacheControlActivity */
public class CacheControlActivity extends BaseFragment {
    private long audioSize = -1;
    private int cacheInfoRow;
    private int cacheRow;
    private long cacheSize = -1;
    private boolean calculating = true;
    private volatile boolean canceled = false;
    private boolean[] clear = new boolean[6];
    private int databaseInfoRow;
    private int databaseRow;
    private long databaseSize = -1;
    private long documentsSize = -1;
    private int keepMediaInfoRow;
    private int keepMediaRow;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private long musicSize = -1;
    private long photoSize = -1;
    private int rowCount;
    private long totalSize = -1;
    private long videoSize = -1;

    /* renamed from: org.telegram.ui.CacheControlActivity$1 */
    class CLASSNAME implements Runnable {

        /* renamed from: org.telegram.ui.CacheControlActivity$1$1 */
        class CLASSNAME implements Runnable {
            CLASSNAME() {
            }

            public void run() {
                CacheControlActivity.this.calculating = false;
                if (CacheControlActivity.this.listAdapter != null) {
                    CacheControlActivity.this.listAdapter.notifyDataSetChanged();
                }
            }
        }

        CLASSNAME() {
        }

        public void run() {
            CacheControlActivity.this.cacheSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(4), 0);
            if (!CacheControlActivity.this.canceled) {
                CacheControlActivity.this.photoSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(0), 0);
                if (!CacheControlActivity.this.canceled) {
                    CacheControlActivity.this.videoSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(2), 0);
                    if (!CacheControlActivity.this.canceled) {
                        CacheControlActivity.this.documentsSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(3), 1);
                        if (!CacheControlActivity.this.canceled) {
                            CacheControlActivity.this.musicSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(3), 2);
                            if (!CacheControlActivity.this.canceled) {
                                CacheControlActivity.this.audioSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(1), 0);
                                CacheControlActivity.this.totalSize = ((((CacheControlActivity.this.cacheSize + CacheControlActivity.this.videoSize) + CacheControlActivity.this.audioSize) + CacheControlActivity.this.photoSize) + CacheControlActivity.this.documentsSize) + CacheControlActivity.this.musicSize;
                                AndroidUtilities.runOnUIThread(new CLASSNAME());
                            }
                        }
                    }
                }
            }
        }
    }

    /* renamed from: org.telegram.ui.CacheControlActivity$3 */
    class CLASSNAME extends ActionBarMenuOnItemClick {
        CLASSNAME() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                CacheControlActivity.this.finishFragment();
            }
        }
    }

    /* renamed from: org.telegram.ui.CacheControlActivity$4 */
    class CLASSNAME implements OnItemClickListener {

        /* renamed from: org.telegram.ui.CacheControlActivity$4$1 */
        class CLASSNAME implements OnClickListener {
            CLASSNAME() {
            }

            public void onClick(DialogInterface dialog, int which) {
                Editor editor = MessagesController.getGlobalMainSettings().edit();
                if (which == 0) {
                    editor.putInt("keep_media", 3).commit();
                } else if (which == 1) {
                    editor.putInt("keep_media", 0).commit();
                } else if (which == 2) {
                    editor.putInt("keep_media", 1).commit();
                } else if (which == 3) {
                    editor.putInt("keep_media", 2).commit();
                }
                if (CacheControlActivity.this.listAdapter != null) {
                    CacheControlActivity.this.listAdapter.notifyDataSetChanged();
                }
                PendingIntent pintent = PendingIntent.getService(ApplicationLoader.applicationContext, 1, new Intent(ApplicationLoader.applicationContext, ClearCacheService.class), 0);
                AlarmManager alarmManager = (AlarmManager) ApplicationLoader.applicationContext.getSystemService("alarm");
                if (which == 2) {
                    alarmManager.cancel(pintent);
                } else {
                    alarmManager.setInexactRepeating(2, 86400000, 86400000, pintent);
                }
            }
        }

        /* renamed from: org.telegram.ui.CacheControlActivity$4$2 */
        class CLASSNAME implements OnClickListener {
            CLASSNAME() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                final AlertDialog progressDialog = new AlertDialog(CacheControlActivity.this.getParentActivity(), 1);
                progressDialog.setMessage(LocaleController.getString("Loading", CLASSNAMER.string.Loading));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                progressDialog.show();
                MessagesStorage.getInstance(CacheControlActivity.this.currentAccount).getStorageQueue().postRunnable(new Runnable() {

                    /* renamed from: org.telegram.ui.CacheControlActivity$4$2$1$1 */
                    class CLASSNAME implements Runnable {
                        CLASSNAME() {
                        }

                        public void run() {
                            try {
                                progressDialog.dismiss();
                            } catch (Throwable e) {
                                FileLog.m14e(e);
                            }
                            if (CacheControlActivity.this.listAdapter != null) {
                                CacheControlActivity.this.databaseSize = MessagesStorage.getInstance(CacheControlActivity.this.currentAccount).getDatabaseSize();
                                CacheControlActivity.this.listAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    public void run() {
                        try {
                            SQLiteDatabase database = MessagesStorage.getInstance(CacheControlActivity.this.currentAccount).getDatabase();
                            ArrayList<Long> dialogsToCleanup = new ArrayList();
                            SQLiteCursor cursor = database.queryFinalized("SELECT did FROM dialogs WHERE 1", new Object[0]);
                            StringBuilder ids = new StringBuilder();
                            while (cursor.next()) {
                                long did = cursor.longValue(0);
                                int high_id = (int) (did >> 32);
                                if (!(((int) did) == 0 || high_id == 1)) {
                                    dialogsToCleanup.add(Long.valueOf(did));
                                }
                            }
                            cursor.dispose();
                            SQLitePreparedStatement state5 = database.executeFast("REPLACE INTO messages_holes VALUES(?, ?, ?)");
                            SQLitePreparedStatement state6 = database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
                            database.beginTransaction();
                            for (int a = 0; a < dialogsToCleanup.size(); a++) {
                                Long did2 = (Long) dialogsToCleanup.get(a);
                                int messagesCount = 0;
                                cursor = database.queryFinalized("SELECT COUNT(mid) FROM messages WHERE uid = " + did2, new Object[0]);
                                if (cursor.next()) {
                                    messagesCount = cursor.intValue(0);
                                }
                                cursor.dispose();
                                if (messagesCount > 2) {
                                    cursor = database.queryFinalized("SELECT last_mid_i, last_mid FROM dialogs WHERE did = " + did2, new Object[0]);
                                    int messageId = -1;
                                    if (cursor.next()) {
                                        long last_mid_i = cursor.longValue(0);
                                        long last_mid = cursor.longValue(1);
                                        SQLiteCursor cursor2 = database.queryFinalized("SELECT data FROM messages WHERE uid = " + did2 + " AND mid IN (" + last_mid_i + "," + last_mid + ")", new Object[0]);
                                        while (cursor2.next()) {
                                            try {
                                                NativeByteBuffer data = cursor2.byteBufferValue(0);
                                                if (data != null) {
                                                    Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                                                    message.readAttachPath(data, UserConfig.getInstance(CacheControlActivity.this.currentAccount).clientUserId);
                                                    data.reuse();
                                                    if (message != null) {
                                                        messageId = message.var_id;
                                                    }
                                                }
                                            } catch (Throwable e) {
                                                FileLog.m14e(e);
                                            }
                                        }
                                        cursor2.dispose();
                                        database.executeFast("DELETE FROM messages WHERE uid = " + did2 + " AND mid != " + last_mid_i + " AND mid != " + last_mid).stepThis().dispose();
                                        database.executeFast("DELETE FROM messages_holes WHERE uid = " + did2).stepThis().dispose();
                                        database.executeFast("DELETE FROM bot_keyboard WHERE uid = " + did2).stepThis().dispose();
                                        database.executeFast("DELETE FROM media_counts_v2 WHERE uid = " + did2).stepThis().dispose();
                                        database.executeFast("DELETE FROM media_v2 WHERE uid = " + did2).stepThis().dispose();
                                        database.executeFast("DELETE FROM media_holes_v2 WHERE uid = " + did2).stepThis().dispose();
                                        DataQuery.getInstance(CacheControlActivity.this.currentAccount).clearBotKeyboard(did2.longValue(), null);
                                        if (messageId != -1) {
                                            MessagesStorage.createFirstHoles(did2.longValue(), state5, state6, messageId);
                                        }
                                    }
                                    cursor.dispose();
                                }
                            }
                            database.executeFast("DELETE FROM messages WHERE uid = " + -NUM).stepThis().dispose();
                            database.executeFast("DELETE FROM messages_holes WHERE uid = " + -NUM).stepThis().dispose();
                            database.executeFast("DELETE FROM bot_keyboard WHERE uid = " + -NUM).stepThis().dispose();
                            database.executeFast("DELETE FROM media_counts_v2 WHERE uid = " + -NUM).stepThis().dispose();
                            database.executeFast("DELETE FROM media_v2 WHERE uid = " + -NUM).stepThis().dispose();
                            database.executeFast("DELETE FROM media_holes_v2 WHERE uid = " + -NUM).stepThis().dispose();
                            state5.dispose();
                            state6.dispose();
                            database.commitTransaction();
                            database.executeFast("PRAGMA journal_size_limit = 0").stepThis().dispose();
                            database.executeFast("VACUUM").stepThis().dispose();
                            database.executeFast("PRAGMA journal_size_limit = -1").stepThis().dispose();
                        } catch (Throwable e2) {
                            FileLog.m14e(e2);
                        } finally {
                            AndroidUtilities.runOnUIThread(new CLASSNAME());
                        }
                    }
                });
            }
        }

        /* renamed from: org.telegram.ui.CacheControlActivity$4$3 */
        class CLASSNAME implements View.OnClickListener {
            CLASSNAME() {
            }

            public void onClick(View v) {
                CheckBoxCell cell = (CheckBoxCell) v;
                int num = ((Integer) cell.getTag()).intValue();
                CacheControlActivity.this.clear[num] = !CacheControlActivity.this.clear[num];
                cell.setChecked(CacheControlActivity.this.clear[num], true);
            }
        }

        /* renamed from: org.telegram.ui.CacheControlActivity$4$4 */
        class CLASSNAME implements View.OnClickListener {
            CLASSNAME() {
            }

            public void onClick(View v) {
                try {
                    if (CacheControlActivity.this.visibleDialog != null) {
                        CacheControlActivity.this.visibleDialog.dismiss();
                    }
                } catch (Throwable e) {
                    FileLog.m14e(e);
                }
                CacheControlActivity.this.cleanupFolders();
            }
        }

        CLASSNAME() {
        }

        public void onItemClick(View view, int position) {
            if (CacheControlActivity.this.getParentActivity() != null) {
                Builder builder;
                if (position == CacheControlActivity.this.keepMediaRow) {
                    builder = new Builder(CacheControlActivity.this.getParentActivity());
                    builder.setItems(new CharSequence[]{LocaleController.formatPluralString("Days", 3), LocaleController.formatPluralString("Weeks", 1), LocaleController.formatPluralString("Months", 1), LocaleController.getString("KeepMediaForever", CLASSNAMER.string.KeepMediaForever)}, new CLASSNAME());
                    CacheControlActivity.this.showDialog(builder.create());
                } else if (position == CacheControlActivity.this.databaseRow) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(CacheControlActivity.this.getParentActivity());
                    builder2.setTitle(LocaleController.getString("AppName", CLASSNAMER.string.AppName));
                    builder2.setNegativeButton(LocaleController.getString("Cancel", CLASSNAMER.string.Cancel), null);
                    builder2.setMessage(LocaleController.getString("LocalDatabaseClear", CLASSNAMER.string.LocalDatabaseClear));
                    builder2.setPositiveButton(LocaleController.getString("CacheClear", CLASSNAMER.string.CacheClear), new CLASSNAME());
                    CacheControlActivity.this.showDialog(builder2.create());
                } else if (position == CacheControlActivity.this.cacheRow && CacheControlActivity.this.totalSize > 0 && CacheControlActivity.this.getParentActivity() != null) {
                    builder = new Builder(CacheControlActivity.this.getParentActivity());
                    builder.setApplyTopPadding(false);
                    builder.setApplyBottomPadding(false);
                    LinearLayout linearLayout = new LinearLayout(CacheControlActivity.this.getParentActivity());
                    linearLayout.setOrientation(1);
                    for (int a = 0; a < 6; a++) {
                        long size = 0;
                        String name = null;
                        if (a == 0) {
                            size = CacheControlActivity.this.photoSize;
                            name = LocaleController.getString("LocalPhotoCache", CLASSNAMER.string.LocalPhotoCache);
                        } else if (a == 1) {
                            size = CacheControlActivity.this.videoSize;
                            name = LocaleController.getString("LocalVideoCache", CLASSNAMER.string.LocalVideoCache);
                        } else if (a == 2) {
                            size = CacheControlActivity.this.documentsSize;
                            name = LocaleController.getString("LocalDocumentCache", CLASSNAMER.string.LocalDocumentCache);
                        } else if (a == 3) {
                            size = CacheControlActivity.this.musicSize;
                            name = LocaleController.getString("LocalMusicCache", CLASSNAMER.string.LocalMusicCache);
                        } else if (a == 4) {
                            size = CacheControlActivity.this.audioSize;
                            name = LocaleController.getString("LocalAudioCache", CLASSNAMER.string.LocalAudioCache);
                        } else if (a == 5) {
                            size = CacheControlActivity.this.cacheSize;
                            name = LocaleController.getString("LocalCache", CLASSNAMER.string.LocalCache);
                        }
                        if (size > 0) {
                            CacheControlActivity.this.clear[a] = true;
                            CheckBoxCell checkBoxCell = new CheckBoxCell(CacheControlActivity.this.getParentActivity(), 1);
                            checkBoxCell.setTag(Integer.valueOf(a));
                            checkBoxCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
                            linearLayout.addView(checkBoxCell, LayoutHelper.createLinear(-1, 48));
                            checkBoxCell.setText(name, AndroidUtilities.formatFileSize(size), true, true);
                            checkBoxCell.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                            checkBoxCell.setOnClickListener(new CLASSNAME());
                        } else {
                            CacheControlActivity.this.clear[a] = false;
                        }
                    }
                    BottomSheetCell cell = new BottomSheetCell(CacheControlActivity.this.getParentActivity(), 1);
                    cell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
                    cell.setTextAndIcon(LocaleController.getString("ClearMediaCache", CLASSNAMER.string.ClearMediaCache).toUpperCase(), 0);
                    cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText));
                    cell.setOnClickListener(new CLASSNAME());
                    linearLayout.addView(cell, LayoutHelper.createLinear(-1, 48));
                    builder.setCustomView(linearLayout);
                    CacheControlActivity.this.showDialog(builder.create());
                }
            }
        }
    }

    /* renamed from: org.telegram.ui.CacheControlActivity$ListAdapter */
    private class ListAdapter extends SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean isEnabled(ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == CacheControlActivity.this.databaseRow || ((position == CacheControlActivity.this.cacheRow && CacheControlActivity.this.totalSize > 0) || position == CacheControlActivity.this.keepMediaRow);
        }

        public int getItemCount() {
            return CacheControlActivity.this.rowCount;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new TextSettingsCell(this.mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    view = new TextInfoPrivacyCell(this.mContext);
                    break;
            }
            return new Holder(view);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    TextSettingsCell textCell = holder.itemView;
                    if (position == CacheControlActivity.this.databaseRow) {
                        textCell.setTextAndValue(LocaleController.getString("LocalDatabase", CLASSNAMER.string.LocalDatabase), AndroidUtilities.formatFileSize(CacheControlActivity.this.databaseSize), false);
                        return;
                    } else if (position == CacheControlActivity.this.cacheRow) {
                        if (CacheControlActivity.this.calculating) {
                            textCell.setTextAndValue(LocaleController.getString("ClearMediaCache", CLASSNAMER.string.ClearMediaCache), LocaleController.getString("CalculatingSize", CLASSNAMER.string.CalculatingSize), false);
                            return;
                        } else {
                            textCell.setTextAndValue(LocaleController.getString("ClearMediaCache", CLASSNAMER.string.ClearMediaCache), CacheControlActivity.this.totalSize == 0 ? LocaleController.getString("CacheEmpty", CLASSNAMER.string.CacheEmpty) : AndroidUtilities.formatFileSize(CacheControlActivity.this.totalSize), false);
                            return;
                        }
                    } else if (position == CacheControlActivity.this.keepMediaRow) {
                        String value;
                        int keepMedia = MessagesController.getGlobalMainSettings().getInt("keep_media", 2);
                        if (keepMedia == 0) {
                            value = LocaleController.formatPluralString("Weeks", 1);
                        } else if (keepMedia == 1) {
                            value = LocaleController.formatPluralString("Months", 1);
                        } else if (keepMedia == 3) {
                            value = LocaleController.formatPluralString("Days", 3);
                        } else {
                            value = LocaleController.getString("KeepMediaForever", CLASSNAMER.string.KeepMediaForever);
                        }
                        textCell.setTextAndValue(LocaleController.getString("KeepMedia", CLASSNAMER.string.KeepMedia), value, false);
                        return;
                    } else {
                        return;
                    }
                case 1:
                    TextInfoPrivacyCell privacyCell = holder.itemView;
                    if (position == CacheControlActivity.this.databaseInfoRow) {
                        privacyCell.setText(LocaleController.getString("LocalDatabaseInfo", CLASSNAMER.string.LocalDatabaseInfo));
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, CLASSNAMER.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                        return;
                    } else if (position == CacheControlActivity.this.cacheInfoRow) {
                        privacyCell.setText(TtmlNode.ANONYMOUS_REGION_ID);
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, CLASSNAMER.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                        return;
                    } else if (position == CacheControlActivity.this.keepMediaInfoRow) {
                        privacyCell.setText(AndroidUtilities.replaceTags(LocaleController.getString("KeepMediaInfo", CLASSNAMER.string.KeepMediaInfo)));
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, CLASSNAMER.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }

        public int getItemViewType(int i) {
            if (i == CacheControlActivity.this.databaseInfoRow || i == CacheControlActivity.this.cacheInfoRow || i == CacheControlActivity.this.keepMediaInfoRow) {
                return 1;
            }
            return 0;
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        this.rowCount = 0;
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.keepMediaRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.keepMediaInfoRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.cacheRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.cacheInfoRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.databaseRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.databaseInfoRow = i;
        this.databaseSize = MessagesStorage.getInstance(this.currentAccount).getDatabaseSize();
        Utilities.globalQueue.postRunnable(new CLASSNAME());
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        this.canceled = true;
    }

    private long getDirectorySize(File dir, int documentsMusicType) {
        if (dir == null || this.canceled) {
            return 0;
        }
        if (dir.isDirectory()) {
            return Utilities.getDirSize(dir.getAbsolutePath(), documentsMusicType);
        }
        if (dir.isFile()) {
            return 0 + dir.length();
        }
        return 0;
    }

    private void cleanupFolders() {
        final AlertDialog progressDialog = new AlertDialog(getParentActivity(), 1);
        progressDialog.setMessage(LocaleController.getString("Loading", CLASSNAMER.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        Utilities.globalQueue.postRunnable(new Runnable() {
            public void run() {
                boolean imagesCleared = false;
                for (int a = 0; a < 6; a++) {
                    if (CacheControlActivity.this.clear[a]) {
                        int type = -1;
                        int documentsMusicType = 0;
                        if (a == 0) {
                            type = 0;
                        } else if (a == 1) {
                            type = 2;
                        } else if (a == 2) {
                            type = 3;
                            documentsMusicType = 1;
                        } else if (a == 3) {
                            type = 3;
                            documentsMusicType = 2;
                        } else if (a == 4) {
                            type = 1;
                        } else if (a == 5) {
                            type = 4;
                        }
                        if (type != -1) {
                            File file = FileLoader.checkDirectory(type);
                            if (file != null) {
                                Utilities.clearDir(file.getAbsolutePath(), documentsMusicType, Long.MAX_VALUE);
                            }
                            if (type == 4) {
                                CacheControlActivity.this.cacheSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(4), documentsMusicType);
                                imagesCleared = true;
                            } else if (type == 1) {
                                CacheControlActivity.this.audioSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(1), documentsMusicType);
                            } else if (type == 3) {
                                if (documentsMusicType == 1) {
                                    CacheControlActivity.this.documentsSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(3), documentsMusicType);
                                } else {
                                    CacheControlActivity.this.musicSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(3), documentsMusicType);
                                }
                            } else if (type == 0) {
                                imagesCleared = true;
                                CacheControlActivity.this.photoSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(0), documentsMusicType);
                            } else if (type == 2) {
                                CacheControlActivity.this.videoSize = CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(2), documentsMusicType);
                            }
                        }
                    }
                }
                final boolean imagesClearedFinal = imagesCleared;
                CacheControlActivity.this.totalSize = ((((CacheControlActivity.this.cacheSize + CacheControlActivity.this.videoSize) + CacheControlActivity.this.audioSize) + CacheControlActivity.this.photoSize) + CacheControlActivity.this.documentsSize) + CacheControlActivity.this.musicSize;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        if (imagesClearedFinal) {
                            ImageLoader.getInstance().clearMemory();
                        }
                        if (CacheControlActivity.this.listAdapter != null) {
                            CacheControlActivity.this.listAdapter.notifyDataSetChanged();
                        }
                        try {
                            progressDialog.dismiss();
                        } catch (Throwable e) {
                            FileLog.m14e(e);
                        }
                    }
                });
            }
        });
    }

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(CLASSNAMER.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("StorageUsage", CLASSNAMER.string.StorageUsage));
        this.actionBar.setActionBarMenuOnItemClick(new CLASSNAME());
        this.listAdapter = new ListAdapter(context);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        this.listView = new RecyclerListView(context);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setLayoutManager(new LinearLayoutManager(context, 1, false));
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0f));
        this.listView.setAdapter(this.listAdapter);
        this.listView.setOnItemClickListener(new CLASSNAME());
        return this.fragmentView;
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    public ThemeDescription[] getThemeDescriptions() {
        r9 = new ThemeDescription[12];
        r9[0] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextSettingsCell.class}, null, null, null, Theme.key_windowBackgroundWhite);
        r9[1] = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray);
        r9[2] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault);
        r9[3] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault);
        r9[4] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon);
        r9[5] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle);
        r9[6] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector);
        r9[7] = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector);
        r9[8] = new ThemeDescription(this.listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        r9[9] = new ThemeDescription(this.listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText);
        r9[10] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow);
        r9[11] = new ThemeDescription(this.listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4);
        return r9;
    }
}
