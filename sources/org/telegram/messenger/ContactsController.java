package org.telegram.messenger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.PrivacyRule;
import org.telegram.tgnet.TLRPC.TL_accountDaysTTL;
import org.telegram.tgnet.TLRPC.TL_account_getAccountTTL;
import org.telegram.tgnet.TLRPC.TL_account_getPrivacy;
import org.telegram.tgnet.TLRPC.TL_account_privacyRules;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_contact;
import org.telegram.tgnet.TLRPC.TL_contactStatus;
import org.telegram.tgnet.TLRPC.TL_contacts_contactsNotModified;
import org.telegram.tgnet.TLRPC.TL_contacts_deleteContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_getContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_getStatuses;
import org.telegram.tgnet.TLRPC.TL_contacts_importContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_importedContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_resetSaved;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_help_getInviteText;
import org.telegram.tgnet.TLRPC.TL_help_inviteText;
import org.telegram.tgnet.TLRPC.TL_importedContact;
import org.telegram.tgnet.TLRPC.TL_inputPhoneContact;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyChatInvite;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyForwards;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyPhoneCall;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyPhoneP2P;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyProfilePhoto;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyStatusTimestamp;
import org.telegram.tgnet.TLRPC.TL_popularContact;
import org.telegram.tgnet.TLRPC.TL_user;
import org.telegram.tgnet.TLRPC.TL_userStatusLastMonth;
import org.telegram.tgnet.TLRPC.TL_userStatusLastWeek;
import org.telegram.tgnet.TLRPC.TL_userStatusRecently;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserStatus;
import org.telegram.tgnet.TLRPC.Vector;
import org.telegram.tgnet.TLRPC.contacts_Contacts;

public class ContactsController {
    private static volatile ContactsController[] Instance = new ContactsController[3];
    private ArrayList<PrivacyRule> callPrivacyRules;
    private int completedRequestsCount;
    public ArrayList<TL_contact> contacts;
    public HashMap<String, Contact> contactsBook;
    private boolean contactsBookLoaded;
    public HashMap<String, Contact> contactsBookSPhones;
    public HashMap<String, TL_contact> contactsByPhone;
    public HashMap<String, TL_contact> contactsByShortPhone;
    public ConcurrentHashMap<Integer, TL_contact> contactsDict;
    public boolean contactsLoaded;
    private boolean contactsSyncInProgress;
    private int currentAccount;
    private ArrayList<Integer> delayedContactsUpdate = new ArrayList();
    private int deleteAccountTTL;
    private ArrayList<PrivacyRule> forwardsPrivacyRules;
    private ArrayList<PrivacyRule> groupPrivacyRules;
    private boolean ignoreChanges;
    private String inviteLink;
    private String lastContactsVersions = "";
    private final Object loadContactsSync = new Object();
    private boolean loadingContacts;
    private int loadingDeleteInfo;
    private int[] loadingPrivacyInfo = new int[6];
    private boolean migratingContacts;
    private final Object observerLock = new Object();
    private ArrayList<PrivacyRule> p2pPrivacyRules;
    public ArrayList<Contact> phoneBookContacts;
    public ArrayList<String> phoneBookSectionsArray;
    public HashMap<String, ArrayList<Object>> phoneBookSectionsDict;
    private ArrayList<PrivacyRule> privacyRules;
    private ArrayList<PrivacyRule> profilePhotoPrivacyRules;
    private String[] projectionNames;
    private String[] projectionPhones;
    private HashMap<String, String> sectionsToReplace = new HashMap();
    public ArrayList<String> sortedUsersMutualSectionsArray;
    public ArrayList<String> sortedUsersSectionsArray;
    private Account systemAccount;
    private boolean updatingInviteLink;
    public HashMap<String, ArrayList<TL_contact>> usersMutualSectionsDict;
    public HashMap<String, ArrayList<TL_contact>> usersSectionsDict;

    public static class Contact {
        public int contact_id;
        public String first_name;
        public int imported;
        public boolean isGoodProvider;
        public String key;
        public String last_name;
        public boolean namesFilled;
        public ArrayList<Integer> phoneDeleted = new ArrayList(4);
        public ArrayList<String> phoneTypes = new ArrayList(4);
        public ArrayList<String> phones = new ArrayList(4);
        public String provider;
        public ArrayList<String> shortPhones = new ArrayList(4);
        public User user;

        public String getLetter() {
            return getLetter(this.first_name, this.last_name);
        }

        public static String getLetter(String str, String str2) {
            if (TextUtils.isEmpty(str)) {
                return !TextUtils.isEmpty(str2) ? str2.substring(0, 1) : "#";
            } else {
                return str.substring(0, 1);
            }
        }
    }

    private class MyContentObserver extends ContentObserver {
        private Runnable checkRunnable = -$$Lambda$ContactsController$MyContentObserver$VmhFqLMqh0tD4jEQWkPIR_W56Bc.INSTANCE;

        public boolean deliverSelfNotifications() {
            return false;
        }

        static /* synthetic */ void lambda$new$0() {
            for (int i = 0; i < 3; i++) {
                if (UserConfig.getInstance(i).isClientActivated()) {
                    ConnectionsManager.getInstance(i).resumeNetworkMaybe();
                    ContactsController.getInstance(i).checkContacts();
                }
            }
        }

        public MyContentObserver() {
            super(null);
        }

        public void onChange(boolean z) {
            super.onChange(z);
            synchronized (ContactsController.this.observerLock) {
                if (ContactsController.this.ignoreChanges) {
                    return;
                }
                Utilities.globalQueue.cancelRunnable(this.checkRunnable);
                Utilities.globalQueue.postRunnable(this.checkRunnable, 500);
            }
        }
    }

    static /* synthetic */ void lambda$resetImportedContacts$9(TLObject tLObject, TL_error tL_error) {
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:39:0x00a3 in {4, 11, 19, 20, 21, 23, 25, 27, 29, 30, 33, 34, 35, 37, 38} preds:[]
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
    private void performWriteContactsToPhoneBookInternal(java.util.ArrayList<org.telegram.tgnet.TLRPC.TL_contact> r11) {
        /*
        r10 = this;
        r0 = 0;
        r1 = r10.hasContactsPermission();	 Catch:{ Exception -> 0x0093 }
        if (r1 != 0) goto L_0x0008;	 Catch:{ Exception -> 0x0093 }
        return;	 Catch:{ Exception -> 0x0093 }
        r1 = android.provider.ContactsContract.RawContacts.CONTENT_URI;	 Catch:{ Exception -> 0x0093 }
        r1 = r1.buildUpon();	 Catch:{ Exception -> 0x0093 }
        r2 = "account_name";	 Catch:{ Exception -> 0x0093 }
        r3 = r10.systemAccount;	 Catch:{ Exception -> 0x0093 }
        r3 = r3.name;	 Catch:{ Exception -> 0x0093 }
        r1 = r1.appendQueryParameter(r2, r3);	 Catch:{ Exception -> 0x0093 }
        r2 = "account_type";	 Catch:{ Exception -> 0x0093 }
        r3 = r10.systemAccount;	 Catch:{ Exception -> 0x0093 }
        r3 = r3.type;	 Catch:{ Exception -> 0x0093 }
        r1 = r1.appendQueryParameter(r2, r3);	 Catch:{ Exception -> 0x0093 }
        r3 = r1.build();	 Catch:{ Exception -> 0x0093 }
        r1 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Exception -> 0x0093 }
        r2 = r1.getContentResolver();	 Catch:{ Exception -> 0x0093 }
        r1 = 2;	 Catch:{ Exception -> 0x0093 }
        r4 = new java.lang.String[r1];	 Catch:{ Exception -> 0x0093 }
        r1 = "_id";	 Catch:{ Exception -> 0x0093 }
        r8 = 0;	 Catch:{ Exception -> 0x0093 }
        r4[r8] = r1;	 Catch:{ Exception -> 0x0093 }
        r1 = "sync2";	 Catch:{ Exception -> 0x0093 }
        r9 = 1;	 Catch:{ Exception -> 0x0093 }
        r4[r9] = r1;	 Catch:{ Exception -> 0x0093 }
        r5 = 0;	 Catch:{ Exception -> 0x0093 }
        r6 = 0;	 Catch:{ Exception -> 0x0093 }
        r7 = 0;	 Catch:{ Exception -> 0x0093 }
        r1 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x0093 }
        r2 = new org.telegram.messenger.support.SparseLongArray;	 Catch:{ Exception -> 0x008e, all -> 0x008b }
        r2.<init>();	 Catch:{ Exception -> 0x008e, all -> 0x008b }
        if (r1 == 0) goto L_0x0087;	 Catch:{ Exception -> 0x008e, all -> 0x008b }
        r3 = r1.moveToNext();	 Catch:{ Exception -> 0x008e, all -> 0x008b }
        if (r3 == 0) goto L_0x0059;	 Catch:{ Exception -> 0x008e, all -> 0x008b }
        r3 = r1.getInt(r9);	 Catch:{ Exception -> 0x008e, all -> 0x008b }
        r4 = r1.getLong(r8);	 Catch:{ Exception -> 0x008e, all -> 0x008b }
        r2.put(r3, r4);	 Catch:{ Exception -> 0x008e, all -> 0x008b }
        goto L_0x0047;	 Catch:{ Exception -> 0x008e, all -> 0x008b }
        r1.close();	 Catch:{ Exception -> 0x008e, all -> 0x008b }
        r1 = 0;
        r3 = r11.size();	 Catch:{ Exception -> 0x0093 }
        if (r1 >= r3) goto L_0x0088;	 Catch:{ Exception -> 0x0093 }
        r3 = r11.get(r1);	 Catch:{ Exception -> 0x0093 }
        r3 = (org.telegram.tgnet.TLRPC.TL_contact) r3;	 Catch:{ Exception -> 0x0093 }
        r4 = r3.user_id;	 Catch:{ Exception -> 0x0093 }
        r4 = r2.indexOfKey(r4);	 Catch:{ Exception -> 0x0093 }
        if (r4 >= 0) goto L_0x0084;	 Catch:{ Exception -> 0x0093 }
        r4 = r10.currentAccount;	 Catch:{ Exception -> 0x0093 }
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);	 Catch:{ Exception -> 0x0093 }
        r3 = r3.user_id;	 Catch:{ Exception -> 0x0093 }
        r3 = java.lang.Integer.valueOf(r3);	 Catch:{ Exception -> 0x0093 }
        r3 = r4.getUser(r3);	 Catch:{ Exception -> 0x0093 }
        r10.addContactToPhoneBook(r3, r8);	 Catch:{ Exception -> 0x0093 }
        r1 = r1 + 1;
        goto L_0x005d;
        r0 = r1;
        if (r0 == 0) goto L_0x009c;
        goto L_0x0099;
        r11 = move-exception;
        r0 = r1;
        goto L_0x009d;
        r11 = move-exception;
        r0 = r1;
        goto L_0x0094;
        r11 = move-exception;
        goto L_0x009d;
        r11 = move-exception;
        org.telegram.messenger.FileLog.e(r11);	 Catch:{ all -> 0x0091 }
        if (r0 == 0) goto L_0x009c;
        r0.close();
        return;
        if (r0 == 0) goto L_0x00a2;
        r0.close();
        throw r11;
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ContactsController.performWriteContactsToPhoneBookInternal(java.util.ArrayList):void");
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:228:0x036d in {4, 6, 11, 13, 28, 31, 32, 35, 38, 43, 45, 47, 48, 57, 58, 63, 70, 71, 76, 81, 82, 83, 84, 92, 93, 94, 97, 100, 103, 106, 107, 109, 110, 111, 114, 116, 117, 118, 119, 121, 122, 124, 125, 144, 145, 147, 148, 153, 154, 160, 166, 168, 169, 174, 175, 177, 178, 180, 182, 183, 184, 186, 187, 191, 193, 195, 197, 199, 200, 202, 203, 205, 207, 211, 214, 216, 217, 218, 220, 224, 226, 227} preds:[]
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
    private java.util.HashMap<java.lang.String, org.telegram.messenger.ContactsController.Contact> readContactsFromPhoneBook() {
        /*
        r21 = this;
        r1 = r21;
        r0 = r1.currentAccount;
        r0 = org.telegram.messenger.UserConfig.getInstance(r0);
        r0 = r0.syncContacts;
        if (r0 != 0) goto L_0x001b;
        r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED;
        if (r0 == 0) goto L_0x0015;
        r0 = "contacts sync disabled";
        org.telegram.messenger.FileLog.d(r0);
        r0 = new java.util.HashMap;
        r0.<init>();
        return r0;
        r0 = r21.hasContactsPermission();
        if (r0 != 0) goto L_0x0030;
        r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED;
        if (r0 == 0) goto L_0x002a;
        r0 = "app has no contacts permissions";
        org.telegram.messenger.FileLog.d(r0);
        r0 = new java.util.HashMap;
        r0.<init>();
        return r0;
        r0 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r0.<init>();	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r3 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r3 = r3.getContentResolver();	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r10 = new java.util.HashMap;	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r10.<init>();	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r11 = new java.util.ArrayList;	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r11.<init>();	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r5 = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI;	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r6 = r1.projectionPhones;	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r7 = 0;	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r8 = 0;	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r9 = 0;	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r4 = r3;	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r4 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ Throwable -> 0x0344, all -> 0x033f }
        r14 = 0;
        r15 = "";
        r9 = 1;
        if (r4 == 0) goto L_0x020e;
        r5 = r4.getCount();	 Catch:{ Throwable -> 0x0207, all -> 0x0201 }
        if (r5 <= 0) goto L_0x01ef;	 Catch:{ Throwable -> 0x0207, all -> 0x0201 }
        r6 = new java.util.HashMap;	 Catch:{ Throwable -> 0x0207, all -> 0x0201 }
        r6.<init>(r5);	 Catch:{ Throwable -> 0x0207, all -> 0x0201 }
        r5 = 1;
        r7 = r4.moveToNext();	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r7 == 0) goto L_0x01eb;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r7 = r4.getString(r9);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r8 = 5;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r8 = r4.getString(r8);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r8 != 0) goto L_0x0075;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r8 = r15;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r2 = ".sim";	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r2 = r8.indexOf(r2);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r2 == 0) goto L_0x007f;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r2 = 1;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x0080;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r2 = 0;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r17 = android.text.TextUtils.isEmpty(r7);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r17 == 0) goto L_0x0087;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x00e5;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r7 = org.telegram.PhoneFormat.PhoneFormat.stripExceptNumbers(r7, r9);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r17 = android.text.TextUtils.isEmpty(r7);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r17 == 0) goto L_0x0092;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x00e5;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r12 = "+";	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r12 = r7.startsWith(r12);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r12 == 0) goto L_0x00a7;
        r12 = r7.substring(r9);	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        goto L_0x00a8;
        r0 = move-exception;
        r10 = r1;
        goto L_0x0204;
        r0 = move-exception;
        r10 = r1;
        goto L_0x01fa;
        r12 = r7;
        r9 = r4.getString(r14);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0.setLength(r14);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        android.database.DatabaseUtils.appendEscapedSQLString(r0, r9);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13 = r0.toString();	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r18 = r10.get(r12);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r14 = r18;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r14 = (org.telegram.messenger.ContactsController.Contact) r14;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r14 == 0) goto L_0x00e9;
        r7 = r14.isGoodProvider;	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        if (r7 != 0) goto L_0x00e5;	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r7 = r14.provider;	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r7 = r8.equals(r7);	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        if (r7 != 0) goto L_0x00e5;	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r7 = 0;	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r0.setLength(r7);	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r7 = r14.key;	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        android.database.DatabaseUtils.appendEscapedSQLString(r0, r7);	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r7 = r0.toString();	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r11.remove(r7);	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r11.add(r13);	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r14.key = r9;	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r14.isGoodProvider = r2;	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r14.provider = r8;	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r9 = 1;
        r14 = 0;
        goto L_0x0063;
        r14 = r11.contains(r13);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r14 != 0) goto L_0x00f2;
        r11.add(r13);	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r13 = 2;
        r14 = r4.getInt(r13);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13 = r6.get(r9);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13 = (org.telegram.messenger.ContactsController.Contact) r13;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r13 != 0) goto L_0x015a;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13 = new org.telegram.messenger.ContactsController$Contact;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13.<init>();	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r18 = r0;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = 4;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r4.getString(r0);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r0 != 0) goto L_0x010f;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r15;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x0113;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r0.trim();	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r19 = r1.isNotValidNameString(r0);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r19 == 0) goto L_0x0120;
        r13.first_name = r0;	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r13.last_name = r15;	 Catch:{ Throwable -> 0x00a3, all -> 0x009f }
        r19 = r3;
        goto L_0x014b;
        r19 = r3;
        r3 = 32;
        r3 = r0.lastIndexOf(r3);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = -1;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r3 == r1) goto L_0x0147;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = 0;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r20 = r0.substring(r1, r3);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = r20.trim();	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13.first_name = r1;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r3 = r3 + 1;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = r0.length();	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r0.substring(r3, r1);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r0.trim();	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13.last_name = r0;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x014b;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13.first_name = r0;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13.last_name = r15;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13.provider = r8;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13.isGoodProvider = r2;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13.key = r9;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r5 + 1;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r13.contact_id = r5;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r6.put(r9, r13);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r5 = r0;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x015e;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r18 = r0;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r19 = r3;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r13.shortPhones;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0.add(r12);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r13.phones;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0.add(r7);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r13.phoneDeleted;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = 0;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r2 = java.lang.Integer.valueOf(r1);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0.add(r2);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = NUM; // 0x7f0d07ba float:1.8746127E38 double:1.053130755E-314;
        r1 = "PhoneMobile";
        if (r14 != 0) goto L_0x018c;
        r2 = 3;
        r3 = r4.getString(r2);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r2 = r13.phoneTypes;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r3 == 0) goto L_0x0183;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x0187;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r3 = org.telegram.messenger.LocaleController.getString(r1, r0);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r2.add(r3);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r2 = 1;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x01de;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r2 = 1;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r14 != r2) goto L_0x019e;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r13.phoneTypes;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = "PhoneHome";	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r3 = NUM; // 0x7f0d07b8 float:1.8746123E38 double:1.053130754E-314;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = org.telegram.messenger.LocaleController.getString(r1, r3);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0.add(r1);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x01de;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r3 = 2;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r14 != r3) goto L_0x01ab;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r3 = r13.phoneTypes;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = org.telegram.messenger.LocaleController.getString(r1, r0);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r3.add(r0);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x01de;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = 3;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r14 != r0) goto L_0x01bd;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r13.phoneTypes;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = "PhoneWork";	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r3 = NUM; // 0x7f0d07c0 float:1.8746139E38 double:1.053130758E-314;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = org.telegram.messenger.LocaleController.getString(r1, r3);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0.add(r1);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x01de;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = 12;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        if (r14 != r0) goto L_0x01d0;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r13.phoneTypes;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = "PhoneMain";	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r3 = NUM; // 0x7f0d07b9 float:1.8746125E38 double:1.0531307543E-314;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = org.telegram.messenger.LocaleController.getString(r1, r3);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0.add(r1);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        goto L_0x01de;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r13.phoneTypes;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = "PhoneOther";	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r3 = NUM; // 0x7f0d07bf float:1.8746137E38 double:1.0531307573E-314;	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r1 = org.telegram.messenger.LocaleController.getString(r1, r3);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0.add(r1);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r10.put(r12, r13);	 Catch:{ Throwable -> 0x01f7, all -> 0x0201 }
        r0 = r18;
        r3 = r19;
        r9 = 1;
        r14 = 0;
        r1 = r21;
        goto L_0x0063;
        r19 = r3;
        r2 = 1;
        goto L_0x01f3;
        r19 = r3;
        r2 = 1;
        r6 = 0;
        r4.close();	 Catch:{ Exception -> 0x01fe }
        goto L_0x01fe;
        r0 = move-exception;
        r10 = r21;
        r2 = r4;
        r1 = r6;
        goto L_0x0348;
        r1 = r6;
        r3 = 0;
        goto L_0x0213;
        r0 = move-exception;
        r10 = r21;
        r1 = r0;
        goto L_0x0361;
        r0 = move-exception;
        r1 = 0;
        r10 = r21;
        r2 = r4;
        goto L_0x0348;
        r19 = r3;
        r2 = 1;
        r3 = r4;
        r1 = 0;
        r0 = ",";	 Catch:{ Throwable -> 0x033a, all -> 0x0334 }
        r0 = android.text.TextUtils.join(r0, r11);	 Catch:{ Throwable -> 0x033a, all -> 0x0334 }
        r5 = android.provider.ContactsContract.Data.CONTENT_URI;	 Catch:{ Throwable -> 0x033a, all -> 0x0334 }
        r10 = r21;
        r6 = r10.projectionNames;	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r4 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r4.<init>();	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r7 = "lookup IN (";	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r4.append(r7);	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r4.append(r0);	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r0 = ") AND ";	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r4.append(r0);	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r0 = "mimetype";	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r4.append(r0);	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r0 = " = '";	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r4.append(r0);	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r0 = "vnd.android.cursor.item/name";	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r4.append(r0);	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r0 = "'";	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r4.append(r0);	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r7 = r4.toString();	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r8 = 0;	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r9 = 0;	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r4 = r19;	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r0 = 1;	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        r2 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ Throwable -> 0x0332, all -> 0x0330 }
        if (r2 == 0) goto L_0x0322;
        r3 = r2.moveToNext();	 Catch:{ Throwable -> 0x0320 }
        if (r3 == 0) goto L_0x031a;	 Catch:{ Throwable -> 0x0320 }
        r3 = 0;	 Catch:{ Throwable -> 0x0320 }
        r4 = r2.getString(r3);	 Catch:{ Throwable -> 0x0320 }
        r5 = r2.getString(r0);	 Catch:{ Throwable -> 0x0320 }
        r6 = 2;	 Catch:{ Throwable -> 0x0320 }
        r7 = r2.getString(r6);	 Catch:{ Throwable -> 0x0320 }
        r8 = 3;	 Catch:{ Throwable -> 0x0320 }
        r9 = r2.getString(r8);	 Catch:{ Throwable -> 0x0320 }
        r4 = r1.get(r4);	 Catch:{ Throwable -> 0x0320 }
        r4 = (org.telegram.messenger.ContactsController.Contact) r4;	 Catch:{ Throwable -> 0x0320 }
        if (r4 == 0) goto L_0x0254;	 Catch:{ Throwable -> 0x0320 }
        r11 = r4.namesFilled;	 Catch:{ Throwable -> 0x0320 }
        if (r11 != 0) goto L_0x0254;	 Catch:{ Throwable -> 0x0320 }
        r11 = r4.isGoodProvider;	 Catch:{ Throwable -> 0x0320 }
        r12 = " ";
        if (r11 == 0) goto L_0x02b5;
        if (r5 == 0) goto L_0x0284;
        r4.first_name = r5;	 Catch:{ Throwable -> 0x0320 }
        goto L_0x0286;	 Catch:{ Throwable -> 0x0320 }
        r4.first_name = r15;	 Catch:{ Throwable -> 0x0320 }
        if (r7 == 0) goto L_0x028b;	 Catch:{ Throwable -> 0x0320 }
        r4.last_name = r7;	 Catch:{ Throwable -> 0x0320 }
        goto L_0x028d;	 Catch:{ Throwable -> 0x0320 }
        r4.last_name = r15;	 Catch:{ Throwable -> 0x0320 }
        r5 = android.text.TextUtils.isEmpty(r9);	 Catch:{ Throwable -> 0x0320 }
        if (r5 != 0) goto L_0x0316;	 Catch:{ Throwable -> 0x0320 }
        r5 = r4.first_name;	 Catch:{ Throwable -> 0x0320 }
        r5 = android.text.TextUtils.isEmpty(r5);	 Catch:{ Throwable -> 0x0320 }
        if (r5 != 0) goto L_0x02b2;	 Catch:{ Throwable -> 0x0320 }
        r5 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0320 }
        r5.<init>();	 Catch:{ Throwable -> 0x0320 }
        r7 = r4.first_name;	 Catch:{ Throwable -> 0x0320 }
        r5.append(r7);	 Catch:{ Throwable -> 0x0320 }
        r5.append(r12);	 Catch:{ Throwable -> 0x0320 }
        r5.append(r9);	 Catch:{ Throwable -> 0x0320 }
        r5 = r5.toString();	 Catch:{ Throwable -> 0x0320 }
        r4.first_name = r5;	 Catch:{ Throwable -> 0x0320 }
        goto L_0x0316;	 Catch:{ Throwable -> 0x0320 }
        r4.first_name = r9;	 Catch:{ Throwable -> 0x0320 }
        goto L_0x0316;	 Catch:{ Throwable -> 0x0320 }
        r11 = r10.isNotValidNameString(r5);	 Catch:{ Throwable -> 0x0320 }
        if (r11 != 0) goto L_0x02cb;	 Catch:{ Throwable -> 0x0320 }
        r11 = r4.first_name;	 Catch:{ Throwable -> 0x0320 }
        r11 = r11.contains(r5);	 Catch:{ Throwable -> 0x0320 }
        if (r11 != 0) goto L_0x02e1;	 Catch:{ Throwable -> 0x0320 }
        r11 = r4.first_name;	 Catch:{ Throwable -> 0x0320 }
        r11 = r5.contains(r11);	 Catch:{ Throwable -> 0x0320 }
        if (r11 != 0) goto L_0x02e1;	 Catch:{ Throwable -> 0x0320 }
        r11 = r10.isNotValidNameString(r7);	 Catch:{ Throwable -> 0x0320 }
        if (r11 != 0) goto L_0x0316;	 Catch:{ Throwable -> 0x0320 }
        r11 = r4.last_name;	 Catch:{ Throwable -> 0x0320 }
        r11 = r11.contains(r7);	 Catch:{ Throwable -> 0x0320 }
        if (r11 != 0) goto L_0x02e1;	 Catch:{ Throwable -> 0x0320 }
        r11 = r4.last_name;	 Catch:{ Throwable -> 0x0320 }
        r11 = r5.contains(r11);	 Catch:{ Throwable -> 0x0320 }
        if (r11 == 0) goto L_0x0316;	 Catch:{ Throwable -> 0x0320 }
        if (r5 == 0) goto L_0x02e6;	 Catch:{ Throwable -> 0x0320 }
        r4.first_name = r5;	 Catch:{ Throwable -> 0x0320 }
        goto L_0x02e8;	 Catch:{ Throwable -> 0x0320 }
        r4.first_name = r15;	 Catch:{ Throwable -> 0x0320 }
        r5 = android.text.TextUtils.isEmpty(r9);	 Catch:{ Throwable -> 0x0320 }
        if (r5 != 0) goto L_0x030f;	 Catch:{ Throwable -> 0x0320 }
        r5 = r4.first_name;	 Catch:{ Throwable -> 0x0320 }
        r5 = android.text.TextUtils.isEmpty(r5);	 Catch:{ Throwable -> 0x0320 }
        if (r5 != 0) goto L_0x030d;	 Catch:{ Throwable -> 0x0320 }
        r5 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0320 }
        r5.<init>();	 Catch:{ Throwable -> 0x0320 }
        r11 = r4.first_name;	 Catch:{ Throwable -> 0x0320 }
        r5.append(r11);	 Catch:{ Throwable -> 0x0320 }
        r5.append(r12);	 Catch:{ Throwable -> 0x0320 }
        r5.append(r9);	 Catch:{ Throwable -> 0x0320 }
        r5 = r5.toString();	 Catch:{ Throwable -> 0x0320 }
        r4.first_name = r5;	 Catch:{ Throwable -> 0x0320 }
        goto L_0x030f;	 Catch:{ Throwable -> 0x0320 }
        r4.first_name = r9;	 Catch:{ Throwable -> 0x0320 }
        if (r7 == 0) goto L_0x0314;	 Catch:{ Throwable -> 0x0320 }
        r4.last_name = r7;	 Catch:{ Throwable -> 0x0320 }
        goto L_0x0316;	 Catch:{ Throwable -> 0x0320 }
        r4.last_name = r15;	 Catch:{ Throwable -> 0x0320 }
        r4.namesFilled = r0;	 Catch:{ Throwable -> 0x0320 }
        goto L_0x0254;
        r2.close();	 Catch:{ Exception -> 0x031d }
        r16 = 0;
        goto L_0x0324;
        r0 = move-exception;
        goto L_0x0348;
        r16 = r2;
        if (r16 == 0) goto L_0x0355;
        r16.close();	 Catch:{ Exception -> 0x032a }
        goto L_0x0355;
        r0 = move-exception;
        r2 = r0;
        org.telegram.messenger.FileLog.e(r2);
        goto L_0x0355;
        r0 = move-exception;
        goto L_0x0337;
        r0 = move-exception;
        goto L_0x033d;
        r0 = move-exception;
        r10 = r21;
        r1 = r0;
        r4 = r3;
        goto L_0x0361;
        r0 = move-exception;
        r10 = r21;
        r2 = r3;
        goto L_0x0348;
        r0 = move-exception;
        r10 = r1;
        r1 = r0;
        r4 = 0;
        goto L_0x0361;
        r0 = move-exception;
        r10 = r1;
        r1 = 0;
        r2 = 0;
        org.telegram.messenger.FileLog.e(r0);	 Catch:{ all -> 0x035e }
        if (r1 == 0) goto L_0x0350;	 Catch:{ all -> 0x035e }
        r1.clear();	 Catch:{ all -> 0x035e }
        if (r2 == 0) goto L_0x0355;
        r2.close();	 Catch:{ Exception -> 0x032a }
        if (r1 == 0) goto L_0x0358;
        goto L_0x035d;
        r1 = new java.util.HashMap;
        r1.<init>();
        return r1;
        r0 = move-exception;
        r1 = r0;
        r4 = r2;
        if (r4 == 0) goto L_0x036c;
        r4.close();	 Catch:{ Exception -> 0x0367 }
        goto L_0x036c;
        r0 = move-exception;
        r2 = r0;
        org.telegram.messenger.FileLog.e(r2);
        throw r1;
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ContactsController.readContactsFromPhoneBook():java.util.HashMap");
    }

    public static ContactsController getInstance(int i) {
        ContactsController contactsController = Instance[i];
        if (contactsController == null) {
            synchronized (ContactsController.class) {
                contactsController = Instance[i];
                if (contactsController == null) {
                    ContactsController[] contactsControllerArr = Instance;
                    ContactsController contactsController2 = new ContactsController(i);
                    contactsControllerArr[i] = contactsController2;
                    contactsController = contactsController2;
                }
            }
        }
        return contactsController;
    }

    public ContactsController(int i) {
        String[] strArr = new String[6];
        strArr[0] = "lookup";
        strArr[1] = "data1";
        strArr[2] = "data2";
        strArr[3] = "data3";
        strArr[4] = "display_name";
        strArr[5] = "account_type";
        this.projectionPhones = strArr;
        this.projectionNames = new String[]{"lookup", "data2", "data3", "data5"};
        this.contactsBook = new HashMap();
        this.contactsBookSPhones = new HashMap();
        this.phoneBookContacts = new ArrayList();
        this.phoneBookSectionsDict = new HashMap();
        this.phoneBookSectionsArray = new ArrayList();
        this.contacts = new ArrayList();
        this.contactsDict = new ConcurrentHashMap(20, 1.0f, 2);
        this.usersSectionsDict = new HashMap();
        this.sortedUsersSectionsArray = new ArrayList();
        this.usersMutualSectionsDict = new HashMap();
        this.sortedUsersMutualSectionsArray = new ArrayList();
        this.contactsByPhone = new HashMap();
        this.contactsByShortPhone = new HashMap();
        this.currentAccount = i;
        if (MessagesController.getMainSettings(this.currentAccount).getBoolean("needGetStatuses", false)) {
            reloadContactsStatuses();
        }
        String str = "A";
        this.sectionsToReplace.put("À", str);
        this.sectionsToReplace.put("Á", str);
        this.sectionsToReplace.put("Ä", str);
        str = "U";
        this.sectionsToReplace.put("Ù", str);
        this.sectionsToReplace.put("Ú", str);
        this.sectionsToReplace.put("Ü", str);
        str = "I";
        this.sectionsToReplace.put("Ì", str);
        this.sectionsToReplace.put("Í", str);
        this.sectionsToReplace.put("Ï", str);
        str = "E";
        this.sectionsToReplace.put("È", str);
        this.sectionsToReplace.put("É", str);
        this.sectionsToReplace.put("Ê", str);
        this.sectionsToReplace.put("Ë", str);
        str = "O";
        this.sectionsToReplace.put("Ò", str);
        this.sectionsToReplace.put("Ó", str);
        this.sectionsToReplace.put("Ö", str);
        this.sectionsToReplace.put("Ç", "C");
        this.sectionsToReplace.put("Ñ", "N");
        str = "Y";
        this.sectionsToReplace.put("Ÿ", str);
        this.sectionsToReplace.put("Ý", str);
        this.sectionsToReplace.put("Ţ", str);
        if (i == 0) {
            Utilities.globalQueue.postRunnable(new -$$Lambda$ContactsController$IIQVqTpQCvnIY8-p91gfnj3he0s(this));
        }
    }

    public /* synthetic */ void lambda$new$0$ContactsController() {
        try {
            if (hasContactsPermission()) {
                ApplicationLoader.applicationContext.getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, new MyContentObserver());
            }
        } catch (Throwable unused) {
        }
    }

    public void cleanup() {
        this.contactsBook.clear();
        this.contactsBookSPhones.clear();
        this.phoneBookContacts.clear();
        this.contacts.clear();
        this.contactsDict.clear();
        this.usersSectionsDict.clear();
        this.usersMutualSectionsDict.clear();
        this.sortedUsersSectionsArray.clear();
        this.sortedUsersMutualSectionsArray.clear();
        this.delayedContactsUpdate.clear();
        this.contactsByPhone.clear();
        this.contactsByShortPhone.clear();
        this.phoneBookSectionsDict.clear();
        this.phoneBookSectionsArray.clear();
        this.loadingContacts = false;
        this.contactsSyncInProgress = false;
        this.contactsLoaded = false;
        this.contactsBookLoaded = false;
        this.lastContactsVersions = "";
        this.loadingDeleteInfo = 0;
        this.deleteAccountTTL = 0;
        int i = 0;
        while (true) {
            int[] iArr = this.loadingPrivacyInfo;
            if (i < iArr.length) {
                iArr[i] = 0;
                i++;
            } else {
                this.privacyRules = null;
                this.groupPrivacyRules = null;
                this.callPrivacyRules = null;
                this.p2pPrivacyRules = null;
                this.profilePhotoPrivacyRules = null;
                this.forwardsPrivacyRules = null;
                Utilities.globalQueue.postRunnable(new -$$Lambda$ContactsController$6JQLrbPbpNJhoOI_5BGOSFa9lbo(this));
                return;
            }
        }
    }

    public /* synthetic */ void lambda$cleanup$1$ContactsController() {
        this.migratingContacts = false;
        this.completedRequestsCount = 0;
    }

    public void checkInviteText() {
        SharedPreferences mainSettings = MessagesController.getMainSettings(this.currentAccount);
        this.inviteLink = mainSettings.getString("invitelink", null);
        int i = mainSettings.getInt("invitelinktime", 0);
        if (!this.updatingInviteLink) {
            if (this.inviteLink == null || Math.abs((System.currentTimeMillis() / 1000) - ((long) i)) >= 86400) {
                this.updatingInviteLink = true;
                ConnectionsManager.getInstance(this.currentAccount).sendRequest(new TL_help_getInviteText(), new -$$Lambda$ContactsController$kcKNthDEzlD6nPSHQklW5Vw14V8(this), 2);
            }
        }
    }

    public /* synthetic */ void lambda$checkInviteText$3$ContactsController(TLObject tLObject, TL_error tL_error) {
        if (tLObject != null) {
            TL_help_inviteText tL_help_inviteText = (TL_help_inviteText) tLObject;
            if (tL_help_inviteText.message.length() != 0) {
                AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$A4-cT-_oXrEc9OYs6FogIzF6avE(this, tL_help_inviteText));
            }
        }
    }

    public /* synthetic */ void lambda$null$2$ContactsController(TL_help_inviteText tL_help_inviteText) {
        this.updatingInviteLink = false;
        Editor edit = MessagesController.getMainSettings(this.currentAccount).edit();
        String str = tL_help_inviteText.message;
        this.inviteLink = str;
        edit.putString("invitelink", str);
        edit.putInt("invitelinktime", (int) (System.currentTimeMillis() / 1000));
        edit.commit();
    }

    public String getInviteText(int i) {
        String str = this.inviteLink;
        if (str == null) {
            str = "https://telegram.org/dl";
        }
        String str2 = "InviteText2";
        if (i <= 1) {
            return LocaleController.formatString(str2, NUM, str);
        }
        try {
            return String.format(LocaleController.getPluralString("InviteTextNum", i), new Object[]{Integer.valueOf(i), str});
        } catch (Exception unused) {
            return LocaleController.formatString(str2, NUM, str);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0052 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x004d A:{SYNTHETIC, Splitter:B:19:0x004d} */
    public void checkAppAccount() {
        /*
        r12 = this;
        r0 = "org.telegram.messenger";
        r1 = "";
        r2 = org.telegram.messenger.ApplicationLoader.applicationContext;
        r2 = android.accounts.AccountManager.get(r2);
        r3 = 0;
        r4 = r2.getAccountsByType(r0);	 Catch:{ Throwable -> 0x0055 }
        r12.systemAccount = r3;	 Catch:{ Throwable -> 0x0055 }
        r5 = 0;
        r6 = 0;
    L_0x0013:
        r7 = r4.length;	 Catch:{ Throwable -> 0x0055 }
        if (r6 >= r7) goto L_0x0055;
    L_0x0016:
        r7 = r4[r6];	 Catch:{ Throwable -> 0x0055 }
        r8 = 0;
    L_0x0019:
        r9 = 3;
        if (r8 >= r9) goto L_0x004a;
    L_0x001c:
        r9 = org.telegram.messenger.UserConfig.getInstance(r8);	 Catch:{ Throwable -> 0x0055 }
        r9 = r9.getCurrentUser();	 Catch:{ Throwable -> 0x0055 }
        if (r9 == 0) goto L_0x0047;
    L_0x0026:
        r10 = r7.name;	 Catch:{ Throwable -> 0x0055 }
        r11 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0055 }
        r11.<init>();	 Catch:{ Throwable -> 0x0055 }
        r11.append(r1);	 Catch:{ Throwable -> 0x0055 }
        r9 = r9.id;	 Catch:{ Throwable -> 0x0055 }
        r11.append(r9);	 Catch:{ Throwable -> 0x0055 }
        r9 = r11.toString();	 Catch:{ Throwable -> 0x0055 }
        r9 = r10.equals(r9);	 Catch:{ Throwable -> 0x0055 }
        if (r9 == 0) goto L_0x0047;
    L_0x003f:
        r9 = r12.currentAccount;	 Catch:{ Throwable -> 0x0055 }
        if (r8 != r9) goto L_0x0045;
    L_0x0043:
        r12.systemAccount = r7;	 Catch:{ Throwable -> 0x0055 }
    L_0x0045:
        r7 = 1;
        goto L_0x004b;
    L_0x0047:
        r8 = r8 + 1;
        goto L_0x0019;
    L_0x004a:
        r7 = 0;
    L_0x004b:
        if (r7 != 0) goto L_0x0052;
    L_0x004d:
        r7 = r4[r6];	 Catch:{ Exception -> 0x0052 }
        r2.removeAccount(r7, r3, r3);	 Catch:{ Exception -> 0x0052 }
    L_0x0052:
        r6 = r6 + 1;
        goto L_0x0013;
    L_0x0055:
        r4 = r12.currentAccount;
        r4 = org.telegram.messenger.UserConfig.getInstance(r4);
        r4 = r4.isClientActivated();
        if (r4 == 0) goto L_0x008d;
    L_0x0061:
        r12.readContacts();
        r4 = r12.systemAccount;
        if (r4 != 0) goto L_0x008d;
    L_0x0068:
        r4 = new android.accounts.Account;	 Catch:{ Exception -> 0x008d }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x008d }
        r5.<init>();	 Catch:{ Exception -> 0x008d }
        r5.append(r1);	 Catch:{ Exception -> 0x008d }
        r6 = r12.currentAccount;	 Catch:{ Exception -> 0x008d }
        r6 = org.telegram.messenger.UserConfig.getInstance(r6);	 Catch:{ Exception -> 0x008d }
        r6 = r6.getClientUserId();	 Catch:{ Exception -> 0x008d }
        r5.append(r6);	 Catch:{ Exception -> 0x008d }
        r5 = r5.toString();	 Catch:{ Exception -> 0x008d }
        r4.<init>(r5, r0);	 Catch:{ Exception -> 0x008d }
        r12.systemAccount = r4;	 Catch:{ Exception -> 0x008d }
        r0 = r12.systemAccount;	 Catch:{ Exception -> 0x008d }
        r2.addAccountExplicitly(r0, r1, r3);	 Catch:{ Exception -> 0x008d }
    L_0x008d:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ContactsController.checkAppAccount():void");
    }

    public void deleteUnknownAppAccounts() {
        try {
            this.systemAccount = null;
            AccountManager accountManager = AccountManager.get(ApplicationLoader.applicationContext);
            Account[] accountsByType = accountManager.getAccountsByType("org.telegram.messenger");
            for (int i = 0; i < accountsByType.length; i++) {
                Object obj;
                Account account = accountsByType[i];
                for (int i2 = 0; i2 < 3; i2++) {
                    User currentUser = UserConfig.getInstance(i2).getCurrentUser();
                    if (currentUser != null) {
                        String str = account.name;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("");
                        stringBuilder.append(currentUser.id);
                        if (str.equals(stringBuilder.toString())) {
                            obj = 1;
                            break;
                        }
                    }
                }
                obj = null;
                if (obj == null) {
                    try {
                        accountManager.removeAccount(accountsByType[i], null, null);
                    } catch (Exception unused) {
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkContacts() {
        Utilities.globalQueue.postRunnable(new -$$Lambda$ContactsController$gch7bRXld1l_k0g1GAfFCoMhjIs(this));
    }

    public /* synthetic */ void lambda$checkContacts$4$ContactsController() {
        if (checkContactsInternal()) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("detected contacts change");
            }
            performSyncPhoneBook(getContactsCopy(this.contactsBook), true, false, true, false, true, false);
        }
    }

    public void forceImportContacts() {
        Utilities.globalQueue.postRunnable(new -$$Lambda$ContactsController$PHwuGsXHnbafxq3b4_SbXpvFUkE(this));
    }

    public /* synthetic */ void lambda$forceImportContacts$5$ContactsController() {
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("force import contacts");
        }
        performSyncPhoneBook(new HashMap(), true, true, true, true, false, false);
    }

    public void syncPhoneBookByAlert(HashMap<String, Contact> hashMap, boolean z, boolean z2, boolean z3) {
        Utilities.globalQueue.postRunnable(new -$$Lambda$ContactsController$zqe6opQgPyDWpCXT4MV8TEtpNRg(this, hashMap, z, z2, z3));
    }

    public /* synthetic */ void lambda$syncPhoneBookByAlert$6$ContactsController(HashMap hashMap, boolean z, boolean z2, boolean z3) {
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("sync contacts by alert");
        }
        performSyncPhoneBook(hashMap, true, z, z2, false, false, z3);
    }

    public void deleteAllContacts(Runnable runnable) {
        resetImportedContacts();
        TL_contacts_deleteContacts tL_contacts_deleteContacts = new TL_contacts_deleteContacts();
        int size = this.contacts.size();
        for (int i = 0; i < size; i++) {
            tL_contacts_deleteContacts.id.add(MessagesController.getInstance(this.currentAccount).getInputUser(((TL_contact) this.contacts.get(i)).user_id));
        }
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(tL_contacts_deleteContacts, new -$$Lambda$ContactsController$Ok5Vywi0AIsSfLPeC7ZAX0Eg0KM(this, runnable));
    }

    public /* synthetic */ void lambda$deleteAllContacts$8$ContactsController(Runnable runnable, TLObject tLObject, TL_error tL_error) {
        if (tLObject instanceof TL_boolTrue) {
            this.contactsBookSPhones.clear();
            this.contactsBook.clear();
            this.completedRequestsCount = 0;
            this.migratingContacts = false;
            this.contactsSyncInProgress = false;
            this.contactsLoaded = false;
            this.loadingContacts = false;
            this.contactsBookLoaded = false;
            this.lastContactsVersions = "";
            AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$Oyly5yQ4u-Oj_ZJTW79_45tDILE(this, runnable));
        }
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0049 */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|(5:5|(4:8|(2:10|(3:22|12|21)(1:24))(1:23)|13|6)|20|14|3)|15|16|17|19) */
    public /* synthetic */ void lambda$null$7$ContactsController(java.lang.Runnable r13) {
        /*
        r12 = this;
        r0 = "org.telegram.messenger";
        r1 = "";
        r2 = org.telegram.messenger.ApplicationLoader.applicationContext;
        r2 = android.accounts.AccountManager.get(r2);
        r3 = 0;
        r4 = 0;
        r5 = r2.getAccountsByType(r0);	 Catch:{ Throwable -> 0x0049 }
        r12.systemAccount = r3;	 Catch:{ Throwable -> 0x0049 }
        r6 = 0;
    L_0x0013:
        r7 = r5.length;	 Catch:{ Throwable -> 0x0049 }
        if (r6 >= r7) goto L_0x0049;
    L_0x0016:
        r7 = r5[r6];	 Catch:{ Throwable -> 0x0049 }
        r8 = 0;
    L_0x0019:
        r9 = 3;
        if (r8 >= r9) goto L_0x0046;
    L_0x001c:
        r9 = org.telegram.messenger.UserConfig.getInstance(r8);	 Catch:{ Throwable -> 0x0049 }
        r9 = r9.getCurrentUser();	 Catch:{ Throwable -> 0x0049 }
        if (r9 == 0) goto L_0x0043;
    L_0x0026:
        r10 = r7.name;	 Catch:{ Throwable -> 0x0049 }
        r11 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0049 }
        r11.<init>();	 Catch:{ Throwable -> 0x0049 }
        r11.append(r1);	 Catch:{ Throwable -> 0x0049 }
        r9 = r9.id;	 Catch:{ Throwable -> 0x0049 }
        r11.append(r9);	 Catch:{ Throwable -> 0x0049 }
        r9 = r11.toString();	 Catch:{ Throwable -> 0x0049 }
        r9 = r10.equals(r9);	 Catch:{ Throwable -> 0x0049 }
        if (r9 == 0) goto L_0x0043;
    L_0x003f:
        r2.removeAccount(r7, r3, r3);	 Catch:{ Throwable -> 0x0049 }
        goto L_0x0046;
    L_0x0043:
        r8 = r8 + 1;
        goto L_0x0019;
    L_0x0046:
        r6 = r6 + 1;
        goto L_0x0013;
    L_0x0049:
        r5 = new android.accounts.Account;	 Catch:{ Exception -> 0x006e }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x006e }
        r6.<init>();	 Catch:{ Exception -> 0x006e }
        r6.append(r1);	 Catch:{ Exception -> 0x006e }
        r7 = r12.currentAccount;	 Catch:{ Exception -> 0x006e }
        r7 = org.telegram.messenger.UserConfig.getInstance(r7);	 Catch:{ Exception -> 0x006e }
        r7 = r7.getClientUserId();	 Catch:{ Exception -> 0x006e }
        r6.append(r7);	 Catch:{ Exception -> 0x006e }
        r6 = r6.toString();	 Catch:{ Exception -> 0x006e }
        r5.<init>(r6, r0);	 Catch:{ Exception -> 0x006e }
        r12.systemAccount = r5;	 Catch:{ Exception -> 0x006e }
        r0 = r12.systemAccount;	 Catch:{ Exception -> 0x006e }
        r2.addAccountExplicitly(r0, r1, r3);	 Catch:{ Exception -> 0x006e }
    L_0x006e:
        r0 = r12.currentAccount;
        r0 = org.telegram.messenger.MessagesStorage.getInstance(r0);
        r1 = new java.util.HashMap;
        r1.<init>();
        r2 = 1;
        r0.putCachedPhoneBook(r1, r4, r2);
        r0 = r12.currentAccount;
        r0 = org.telegram.messenger.MessagesStorage.getInstance(r0);
        r1 = new java.util.ArrayList;
        r1.<init>();
        r0.putContacts(r1, r2);
        r0 = r12.phoneBookContacts;
        r0.clear();
        r0 = r12.contacts;
        r0.clear();
        r0 = r12.contactsDict;
        r0.clear();
        r0 = r12.usersSectionsDict;
        r0.clear();
        r0 = r12.usersMutualSectionsDict;
        r0.clear();
        r0 = r12.sortedUsersSectionsArray;
        r0.clear();
        r0 = r12.phoneBookSectionsDict;
        r0.clear();
        r0 = r12.phoneBookSectionsArray;
        r0.clear();
        r0 = r12.delayedContactsUpdate;
        r0.clear();
        r0 = r12.sortedUsersMutualSectionsArray;
        r0.clear();
        r0 = r12.contactsByPhone;
        r0.clear();
        r0 = r12.contactsByShortPhone;
        r0.clear();
        r0 = r12.currentAccount;
        r0 = org.telegram.messenger.NotificationCenter.getInstance(r0);
        r1 = org.telegram.messenger.NotificationCenter.contactsDidLoad;
        r2 = new java.lang.Object[r4];
        r0.postNotificationName(r1, r2);
        r12.loadContacts(r4, r4);
        r13.run();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ContactsController.lambda$null$7$ContactsController(java.lang.Runnable):void");
    }

    public void resetImportedContacts() {
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(new TL_contacts_resetSaved(), -$$Lambda$ContactsController$rEtEE7WzBCVDghotOSTBjLdLnsM.INSTANCE);
    }

    /* JADX WARNING: Missing block: B:20:0x004f, code skipped:
            if (r2 != null) goto L_0x0051;
     */
    /* JADX WARNING: Missing block: B:22:?, code skipped:
            r2.close();
     */
    /* JADX WARNING: Missing block: B:27:0x005b, code skipped:
            if (r2 == null) goto L_0x0068;
     */
    private boolean checkContactsInternal() {
        /*
        r10 = this;
        r0 = "version";
        r1 = 0;
        r2 = r10.hasContactsPermission();	 Catch:{ Exception -> 0x0064 }
        if (r2 != 0) goto L_0x000a;
    L_0x0009:
        return r1;
    L_0x000a:
        r2 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Exception -> 0x0064 }
        r3 = r2.getContentResolver();	 Catch:{ Exception -> 0x0064 }
        r2 = 0;
        r4 = android.provider.ContactsContract.RawContacts.CONTENT_URI;	 Catch:{ Exception -> 0x0057 }
        r9 = 1;
        r5 = new java.lang.String[r9];	 Catch:{ Exception -> 0x0057 }
        r5[r1] = r0;	 Catch:{ Exception -> 0x0057 }
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r2 = r3.query(r4, r5, r6, r7, r8);	 Catch:{ Exception -> 0x0057 }
        if (r2 == 0) goto L_0x004f;
    L_0x0021:
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0057 }
        r3.<init>();	 Catch:{ Exception -> 0x0057 }
    L_0x0026:
        r4 = r2.moveToNext();	 Catch:{ Exception -> 0x0057 }
        if (r4 == 0) goto L_0x0038;
    L_0x002c:
        r4 = r2.getColumnIndex(r0);	 Catch:{ Exception -> 0x0057 }
        r4 = r2.getString(r4);	 Catch:{ Exception -> 0x0057 }
        r3.append(r4);	 Catch:{ Exception -> 0x0057 }
        goto L_0x0026;
    L_0x0038:
        r0 = r3.toString();	 Catch:{ Exception -> 0x0057 }
        r3 = r10.lastContactsVersions;	 Catch:{ Exception -> 0x0057 }
        r3 = r3.length();	 Catch:{ Exception -> 0x0057 }
        if (r3 == 0) goto L_0x004d;
    L_0x0044:
        r3 = r10.lastContactsVersions;	 Catch:{ Exception -> 0x0057 }
        r3 = r3.equals(r0);	 Catch:{ Exception -> 0x0057 }
        if (r3 != 0) goto L_0x004d;
    L_0x004c:
        r1 = 1;
    L_0x004d:
        r10.lastContactsVersions = r0;	 Catch:{ Exception -> 0x0057 }
    L_0x004f:
        if (r2 == 0) goto L_0x0068;
    L_0x0051:
        r2.close();	 Catch:{ Exception -> 0x0064 }
        goto L_0x0068;
    L_0x0055:
        r0 = move-exception;
        goto L_0x005e;
    L_0x0057:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);	 Catch:{ all -> 0x0055 }
        if (r2 == 0) goto L_0x0068;
    L_0x005d:
        goto L_0x0051;
    L_0x005e:
        if (r2 == 0) goto L_0x0063;
    L_0x0060:
        r2.close();	 Catch:{ Exception -> 0x0064 }
    L_0x0063:
        throw r0;	 Catch:{ Exception -> 0x0064 }
    L_0x0064:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x0068:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ContactsController.checkContactsInternal():boolean");
    }

    public void readContacts() {
        synchronized (this.loadContactsSync) {
            if (this.loadingContacts) {
                return;
            }
            this.loadingContacts = true;
            Utilities.stageQueue.postRunnable(new -$$Lambda$ContactsController$DgnS7Gvt4et5oNJCSlLnfIbm4Ag(this));
        }
    }

    public /* synthetic */ void lambda$readContacts$10$ContactsController() {
        if (!this.contacts.isEmpty() || this.contactsLoaded) {
            synchronized (this.loadContactsSync) {
                this.loadingContacts = false;
            }
            return;
        }
        loadContacts(true, 0);
    }

    private boolean isNotValidNameString(String str) {
        boolean z = true;
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        int length = str.length();
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            char charAt = str.charAt(i2);
            if (charAt >= '0' && charAt <= '9') {
                i++;
            }
        }
        if (i <= 3) {
            z = false;
        }
        return z;
    }

    public HashMap<String, Contact> getContactsCopy(HashMap<String, Contact> hashMap) {
        HashMap hashMap2 = new HashMap();
        for (Entry entry : hashMap.entrySet()) {
            Contact contact = new Contact();
            Contact contact2 = (Contact) entry.getValue();
            contact.phoneDeleted.addAll(contact2.phoneDeleted);
            contact.phones.addAll(contact2.phones);
            contact.phoneTypes.addAll(contact2.phoneTypes);
            contact.shortPhones.addAll(contact2.shortPhones);
            contact.first_name = contact2.first_name;
            contact.last_name = contact2.last_name;
            contact.contact_id = contact2.contact_id;
            contact.key = contact2.key;
            hashMap2.put(contact.key, contact);
        }
        return hashMap2;
    }

    /* Access modifiers changed, original: protected */
    public void migratePhoneBookToV7(SparseArray<Contact> sparseArray) {
        Utilities.globalQueue.postRunnable(new -$$Lambda$ContactsController$BUiF7Jzba7DA47bmyOp4BvMiTjQ(this, sparseArray));
    }

    public /* synthetic */ void lambda$migratePhoneBookToV7$11$ContactsController(SparseArray sparseArray) {
        if (!this.migratingContacts) {
            Contact contact;
            this.migratingContacts = true;
            HashMap hashMap = new HashMap();
            HashMap readContactsFromPhoneBook = readContactsFromPhoneBook();
            HashMap hashMap2 = new HashMap();
            Iterator it = readContactsFromPhoneBook.entrySet().iterator();
            while (true) {
                int i = 0;
                if (!it.hasNext()) {
                    break;
                }
                contact = (Contact) ((Entry) it.next()).getValue();
                while (i < contact.shortPhones.size()) {
                    hashMap2.put(contact.shortPhones.get(i), contact.key);
                    i++;
                }
            }
            for (int i2 = 0; i2 < sparseArray.size(); i2++) {
                contact = (Contact) sparseArray.valueAt(i2);
                for (int i3 = 0; i3 < contact.shortPhones.size(); i3++) {
                    String str = (String) hashMap2.get((String) contact.shortPhones.get(i3));
                    if (str != null) {
                        contact.key = str;
                        hashMap.put(str, contact);
                        break;
                    }
                }
            }
            if (BuildVars.LOGS_ENABLED) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("migrated contacts ");
                stringBuilder.append(hashMap.size());
                stringBuilder.append(" of ");
                stringBuilder.append(sparseArray.size());
                FileLog.d(stringBuilder.toString());
            }
            MessagesStorage.getInstance(this.currentAccount).putCachedPhoneBook(hashMap, true, false);
        }
    }

    /* Access modifiers changed, original: protected */
    public void performSyncPhoneBook(HashMap<String, Contact> hashMap, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6) {
        if (z2) {
        } else if (!this.contactsBookLoaded) {
            return;
        }
        Utilities.globalQueue.postRunnable(new -$$Lambda$ContactsController$bP_Wp_ENz0cZTKPV19hn5HbOllw(this, hashMap, z3, z, z2, z4, z5, z6));
    }

    /* JADX WARNING: Removed duplicated region for block: B:105:0x026d  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01e3  */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x04be  */
    /* JADX WARNING: Removed duplicated region for block: B:219:0x04fc  */
    /* JADX WARNING: Removed duplicated region for block: B:217:0x04ea  */
    /* JADX WARNING: Missing block: B:44:0x0134, code skipped:
            if (r2.first_name.equals(r4.first_name) != false) goto L_0x0139;
     */
    /* JADX WARNING: Missing block: B:49:0x0149, code skipped:
            if (r2.last_name.equals(r4.last_name) == false) goto L_0x014b;
     */
    /* JADX WARNING: Missing block: B:50:0x014b, code skipped:
            r0 = 1;
     */
    /* JADX WARNING: Missing block: B:119:0x02f5, code skipped:
            if (r11.intValue() == 1) goto L_0x0307;
     */
    public /* synthetic */ void lambda$performSyncPhoneBook$24$ContactsController(java.util.HashMap r27, boolean r28, boolean r29, boolean r30, boolean r31, boolean r32, boolean r33) {
        /*
        r26 = this;
        r13 = r26;
        r3 = r27;
        r0 = new java.util.HashMap;
        r0.<init>();
        r1 = r27.entrySet();
        r1 = r1.iterator();
    L_0x0011:
        r2 = r1.hasNext();
        r8 = 0;
        if (r2 == 0) goto L_0x0038;
    L_0x0018:
        r2 = r1.next();
        r2 = (java.util.Map.Entry) r2;
        r2 = r2.getValue();
        r2 = (org.telegram.messenger.ContactsController.Contact) r2;
    L_0x0024:
        r4 = r2.shortPhones;
        r4 = r4.size();
        if (r8 >= r4) goto L_0x0011;
    L_0x002c:
        r4 = r2.shortPhones;
        r4 = r4.get(r8);
        r0.put(r4, r2);
        r8 = r8 + 1;
        goto L_0x0024;
    L_0x0038:
        r1 = org.telegram.messenger.BuildVars.LOGS_ENABLED;
        if (r1 == 0) goto L_0x0041;
    L_0x003c:
        r1 = "start read contacts from phone";
        org.telegram.messenger.FileLog.d(r1);
    L_0x0041:
        if (r28 != 0) goto L_0x0046;
    L_0x0043:
        r26.checkContactsInternal();
    L_0x0046:
        r14 = r26.readContactsFromPhoneBook();
        r15 = new java.util.HashMap;
        r15.<init>();
        r12 = new java.util.HashMap;
        r12.<init>();
        r11 = new java.util.ArrayList;
        r11.<init>();
        r1 = r14.entrySet();
        r1 = r1.iterator();
    L_0x0061:
        r2 = r1.hasNext();
        if (r2 == 0) goto L_0x00b3;
    L_0x0067:
        r2 = r1.next();
        r2 = (java.util.Map.Entry) r2;
        r2 = r2.getValue();
        r2 = (org.telegram.messenger.ContactsController.Contact) r2;
        r4 = r2.shortPhones;
        r4 = r4.size();
        r5 = 0;
    L_0x007a:
        if (r5 >= r4) goto L_0x0098;
    L_0x007c:
        r6 = r2.shortPhones;
        r6 = r6.get(r5);
        r6 = (java.lang.String) r6;
        r7 = r6.length();
        r7 = r7 + -7;
        r7 = java.lang.Math.max(r8, r7);
        r6 = r6.substring(r7);
        r12.put(r6, r2);
        r5 = r5 + 1;
        goto L_0x007a;
    L_0x0098:
        r4 = r2.getLetter();
        r5 = r15.get(r4);
        r5 = (java.util.ArrayList) r5;
        if (r5 != 0) goto L_0x00af;
    L_0x00a4:
        r5 = new java.util.ArrayList;
        r5.<init>();
        r15.put(r4, r5);
        r11.add(r4);
    L_0x00af:
        r5.add(r2);
        goto L_0x0061;
    L_0x00b3:
        r10 = new java.util.HashMap;
        r10.<init>();
        r1 = r27.size();
        r9 = new java.util.ArrayList;
        r9.<init>();
        r2 = r27.isEmpty();
        r5 = "";
        if (r2 != 0) goto L_0x03a5;
    L_0x00c9:
        r2 = r14.entrySet();
        r2 = r2.iterator();
        r7 = 0;
        r16 = 0;
    L_0x00d4:
        r17 = r2.hasNext();
        if (r17 == 0) goto L_0x0357;
    L_0x00da:
        r17 = r2.next();
        r17 = (java.util.Map.Entry) r17;
        r18 = r17.getKey();
        r6 = r18;
        r6 = (java.lang.String) r6;
        r17 = r17.getValue();
        r4 = r17;
        r4 = (org.telegram.messenger.ContactsController.Contact) r4;
        r17 = r3.get(r6);
        r17 = (org.telegram.messenger.ContactsController.Contact) r17;
        if (r17 != 0) goto L_0x0116;
    L_0x00f8:
        r31 = r2;
    L_0x00fa:
        r2 = r4.shortPhones;
        r2 = r2.size();
        if (r8 >= r2) goto L_0x0118;
    L_0x0102:
        r2 = r4.shortPhones;
        r2 = r2.get(r8);
        r2 = r0.get(r2);
        r2 = (org.telegram.messenger.ContactsController.Contact) r2;
        if (r2 == 0) goto L_0x0113;
    L_0x0110:
        r6 = r2.key;
        goto L_0x011a;
    L_0x0113:
        r8 = r8 + 1;
        goto L_0x00fa;
    L_0x0116:
        r31 = r2;
    L_0x0118:
        r2 = r17;
    L_0x011a:
        if (r2 == 0) goto L_0x0120;
    L_0x011c:
        r8 = r2.imported;
        r4.imported = r8;
    L_0x0120:
        if (r2 == 0) goto L_0x014d;
    L_0x0122:
        r8 = r4.first_name;
        r8 = android.text.TextUtils.isEmpty(r8);
        if (r8 != 0) goto L_0x0137;
    L_0x012a:
        r8 = r2.first_name;
        r17 = r0;
        r0 = r4.first_name;
        r0 = r8.equals(r0);
        if (r0 == 0) goto L_0x014b;
    L_0x0136:
        goto L_0x0139;
    L_0x0137:
        r17 = r0;
    L_0x0139:
        r0 = r4.last_name;
        r0 = android.text.TextUtils.isEmpty(r0);
        if (r0 != 0) goto L_0x014f;
    L_0x0141:
        r0 = r2.last_name;
        r8 = r4.last_name;
        r0 = r0.equals(r8);
        if (r0 != 0) goto L_0x014f;
    L_0x014b:
        r0 = 1;
        goto L_0x0150;
    L_0x014d:
        r17 = r0;
    L_0x014f:
        r0 = 0;
    L_0x0150:
        if (r2 == 0) goto L_0x02ae;
    L_0x0152:
        if (r0 == 0) goto L_0x0156;
    L_0x0154:
        goto L_0x02ae;
    L_0x0156:
        r0 = 0;
    L_0x0157:
        r8 = r4.phones;
        r8 = r8.size();
        if (r0 >= r8) goto L_0x0298;
    L_0x015f:
        r8 = r4.shortPhones;
        r8 = r8.get(r0);
        r8 = (java.lang.String) r8;
        r22 = r8.length();
        r23 = r5;
        r5 = r22 + -7;
        r22 = r12;
        r12 = 0;
        r5 = java.lang.Math.max(r12, r5);
        r5 = r8.substring(r5);
        r10.put(r8, r4);
        r12 = r2.shortPhones;
        r12 = r12.indexOf(r8);
        if (r29 == 0) goto L_0x01d7;
    L_0x0185:
        r24 = r12;
        r12 = r13.contactsByPhone;
        r12 = r12.get(r8);
        r12 = (org.telegram.tgnet.TLRPC.TL_contact) r12;
        if (r12 == 0) goto L_0x01ca;
    L_0x0191:
        r25 = r11;
        r11 = r13.currentAccount;
        r11 = org.telegram.messenger.MessagesController.getInstance(r11);
        r12 = r12.user_id;
        r12 = java.lang.Integer.valueOf(r12);
        r11 = r11.getUser(r12);
        if (r11 == 0) goto L_0x01db;
    L_0x01a5:
        r16 = r16 + 1;
        r12 = r11.first_name;
        r12 = android.text.TextUtils.isEmpty(r12);
        if (r12 == 0) goto L_0x01db;
    L_0x01af:
        r11 = r11.last_name;
        r11 = android.text.TextUtils.isEmpty(r11);
        if (r11 == 0) goto L_0x01db;
    L_0x01b7:
        r11 = r4.first_name;
        r11 = android.text.TextUtils.isEmpty(r11);
        if (r11 == 0) goto L_0x01c7;
    L_0x01bf:
        r11 = r4.last_name;
        r11 = android.text.TextUtils.isEmpty(r11);
        if (r11 != 0) goto L_0x01db;
    L_0x01c7:
        r11 = 1;
        r12 = -1;
        goto L_0x01de;
    L_0x01ca:
        r25 = r11;
        r11 = r13.contactsByShortPhone;
        r11 = r11.containsKey(r5);
        if (r11 == 0) goto L_0x01db;
    L_0x01d4:
        r16 = r16 + 1;
        goto L_0x01db;
    L_0x01d7:
        r25 = r11;
        r24 = r12;
    L_0x01db:
        r12 = r24;
        r11 = 0;
    L_0x01de:
        r24 = r15;
        r15 = -1;
        if (r12 != r15) goto L_0x026d;
    L_0x01e3:
        if (r29 == 0) goto L_0x028c;
    L_0x01e5:
        if (r11 != 0) goto L_0x0241;
    L_0x01e7:
        r11 = r13.contactsByPhone;
        r8 = r11.get(r8);
        r8 = (org.telegram.tgnet.TLRPC.TL_contact) r8;
        if (r8 == 0) goto L_0x0237;
    L_0x01f1:
        r5 = r13.currentAccount;
        r5 = org.telegram.messenger.MessagesController.getInstance(r5);
        r8 = r8.user_id;
        r8 = java.lang.Integer.valueOf(r8);
        r5 = r5.getUser(r8);
        if (r5 == 0) goto L_0x0234;
    L_0x0203:
        r16 = r16 + 1;
        r8 = r5.first_name;
        if (r8 == 0) goto L_0x020a;
    L_0x0209:
        goto L_0x020c;
    L_0x020a:
        r8 = r23;
    L_0x020c:
        r5 = r5.last_name;
        if (r5 == 0) goto L_0x0211;
    L_0x0210:
        goto L_0x0213;
    L_0x0211:
        r5 = r23;
    L_0x0213:
        r11 = r4.first_name;
        r8 = r8.equals(r11);
        if (r8 == 0) goto L_0x0223;
    L_0x021b:
        r8 = r4.last_name;
        r5 = r5.equals(r8);
        if (r5 != 0) goto L_0x028c;
    L_0x0223:
        r5 = r4.first_name;
        r5 = android.text.TextUtils.isEmpty(r5);
        if (r5 == 0) goto L_0x0241;
    L_0x022b:
        r5 = r4.last_name;
        r5 = android.text.TextUtils.isEmpty(r5);
        if (r5 == 0) goto L_0x0241;
    L_0x0233:
        goto L_0x028c;
    L_0x0234:
        r7 = r7 + 1;
        goto L_0x0241;
    L_0x0237:
        r8 = r13.contactsByShortPhone;
        r5 = r8.containsKey(r5);
        if (r5 == 0) goto L_0x0241;
    L_0x023f:
        r16 = r16 + 1;
    L_0x0241:
        r5 = new org.telegram.tgnet.TLRPC$TL_inputPhoneContact;
        r5.<init>();
        r8 = r4.contact_id;
        r11 = (long) r8;
        r5.client_id = r11;
        r11 = r5.client_id;
        r15 = r7;
        r7 = (long) r0;
        r18 = 32;
        r7 = r7 << r18;
        r7 = r7 | r11;
        r5.client_id = r7;
        r7 = r4.first_name;
        r5.first_name = r7;
        r7 = r4.last_name;
        r5.last_name = r7;
        r7 = r4.phones;
        r7 = r7.get(r0);
        r7 = (java.lang.String) r7;
        r5.phone = r7;
        r9.add(r5);
        r7 = r15;
        goto L_0x028c;
    L_0x026d:
        r5 = r4.phoneDeleted;
        r8 = r2.phoneDeleted;
        r8 = r8.get(r12);
        r5.set(r0, r8);
        r5 = r2.phones;
        r5.remove(r12);
        r5 = r2.shortPhones;
        r5.remove(r12);
        r5 = r2.phoneDeleted;
        r5.remove(r12);
        r5 = r2.phoneTypes;
        r5.remove(r12);
    L_0x028c:
        r0 = r0 + 1;
        r12 = r22;
        r5 = r23;
        r15 = r24;
        r11 = r25;
        goto L_0x0157;
    L_0x0298:
        r23 = r5;
        r25 = r11;
        r22 = r12;
        r24 = r15;
        r0 = r2.phones;
        r0 = r0.isEmpty();
        if (r0 == 0) goto L_0x02ab;
    L_0x02a8:
        r3.remove(r6);
    L_0x02ab:
        r13 = r1;
        goto L_0x0345;
    L_0x02ae:
        r23 = r5;
        r25 = r11;
        r22 = r12;
        r24 = r15;
        r5 = 0;
    L_0x02b7:
        r8 = r4.phones;
        r8 = r8.size();
        if (r5 >= r8) goto L_0x033f;
    L_0x02bf:
        r8 = r4.shortPhones;
        r8 = r8.get(r5);
        r8 = (java.lang.String) r8;
        r11 = r8.length();
        r11 = r11 + -7;
        r12 = 0;
        r11 = java.lang.Math.max(r12, r11);
        r8.substring(r11);
        r10.put(r8, r4);
        if (r2 == 0) goto L_0x02f8;
    L_0x02da:
        r11 = r2.shortPhones;
        r11 = r11.indexOf(r8);
        r12 = -1;
        if (r11 == r12) goto L_0x02f9;
    L_0x02e3:
        r15 = r2.phoneDeleted;
        r11 = r15.get(r11);
        r11 = (java.lang.Integer) r11;
        r15 = r4.phoneDeleted;
        r15.set(r5, r11);
        r11 = r11.intValue();
        r15 = 1;
        if (r11 != r15) goto L_0x02f9;
    L_0x02f7:
        goto L_0x0307;
    L_0x02f8:
        r12 = -1;
    L_0x02f9:
        if (r29 == 0) goto L_0x0307;
    L_0x02fb:
        if (r0 != 0) goto L_0x030c;
    L_0x02fd:
        r11 = r13.contactsByPhone;
        r8 = r11.containsKey(r8);
        if (r8 == 0) goto L_0x030a;
    L_0x0305:
        r16 = r16 + 1;
    L_0x0307:
        r15 = r0;
        r13 = r1;
        goto L_0x0337;
    L_0x030a:
        r7 = r7 + 1;
    L_0x030c:
        r8 = new org.telegram.tgnet.TLRPC$TL_inputPhoneContact;
        r8.<init>();
        r11 = r4.contact_id;
        r12 = (long) r11;
        r8.client_id = r12;
        r11 = r8.client_id;
        r15 = r0;
        r13 = r1;
        r0 = (long) r5;
        r18 = 32;
        r0 = r0 << r18;
        r0 = r0 | r11;
        r8.client_id = r0;
        r0 = r4.first_name;
        r8.first_name = r0;
        r0 = r4.last_name;
        r8.last_name = r0;
        r0 = r4.phones;
        r0 = r0.get(r5);
        r0 = (java.lang.String) r0;
        r8.phone = r0;
        r9.add(r8);
    L_0x0337:
        r5 = r5 + 1;
        r1 = r13;
        r0 = r15;
        r13 = r26;
        goto L_0x02b7;
    L_0x033f:
        r13 = r1;
        if (r2 == 0) goto L_0x0345;
    L_0x0342:
        r3.remove(r6);
    L_0x0345:
        r2 = r31;
        r1 = r13;
        r0 = r17;
        r12 = r22;
        r5 = r23;
        r15 = r24;
        r11 = r25;
        r8 = 0;
        r13 = r26;
        goto L_0x00d4;
    L_0x0357:
        r13 = r1;
        r25 = r11;
        r22 = r12;
        r24 = r15;
        if (r30 != 0) goto L_0x037d;
    L_0x0360:
        r0 = r27.isEmpty();
        if (r0 == 0) goto L_0x037d;
    L_0x0366:
        r0 = r9.isEmpty();
        if (r0 == 0) goto L_0x037d;
    L_0x036c:
        r0 = r14.size();
        r1 = r13;
        if (r1 != r0) goto L_0x037e;
    L_0x0373:
        r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED;
        if (r0 == 0) goto L_0x037c;
    L_0x0377:
        r0 = "contacts not changed!";
        org.telegram.messenger.FileLog.d(r0);
    L_0x037c:
        return;
    L_0x037d:
        r1 = r13;
    L_0x037e:
        if (r29 == 0) goto L_0x039f;
    L_0x0380:
        r0 = r27.isEmpty();
        if (r0 != 0) goto L_0x039f;
    L_0x0386:
        r0 = r14.isEmpty();
        if (r0 != 0) goto L_0x039f;
    L_0x038c:
        r0 = r9.isEmpty();
        if (r0 == 0) goto L_0x039f;
    L_0x0392:
        r13 = r26;
        r0 = r13.currentAccount;
        r0 = org.telegram.messenger.MessagesStorage.getInstance(r0);
        r2 = 0;
        r0.putCachedPhoneBook(r14, r2, r2);
        goto L_0x03a1;
    L_0x039f:
        r13 = r26;
    L_0x03a1:
        r8 = r16;
        goto L_0x047b;
    L_0x03a5:
        r23 = r5;
        r25 = r11;
        r22 = r12;
        r24 = r15;
        if (r29 == 0) goto L_0x0479;
    L_0x03af:
        r0 = r14.entrySet();
        r0 = r0.iterator();
        r8 = 0;
    L_0x03b8:
        r2 = r0.hasNext();
        if (r2 == 0) goto L_0x0477;
    L_0x03be:
        r2 = r0.next();
        r2 = (java.util.Map.Entry) r2;
        r4 = r2.getValue();
        r4 = (org.telegram.messenger.ContactsController.Contact) r4;
        r2 = r2.getKey();
        r2 = (java.lang.String) r2;
        r2 = 0;
    L_0x03d1:
        r5 = r4.phones;
        r5 = r5.size();
        if (r2 >= r5) goto L_0x03b8;
    L_0x03d9:
        if (r31 != 0) goto L_0x044b;
    L_0x03db:
        r5 = r4.shortPhones;
        r5 = r5.get(r2);
        r5 = (java.lang.String) r5;
        r6 = r5.length();
        r6 = r6 + -7;
        r7 = 0;
        r6 = java.lang.Math.max(r7, r6);
        r6 = r5.substring(r6);
        r7 = r13.contactsByPhone;
        r5 = r7.get(r5);
        r5 = (org.telegram.tgnet.TLRPC.TL_contact) r5;
        if (r5 == 0) goto L_0x0441;
    L_0x03fc:
        r6 = r13.currentAccount;
        r6 = org.telegram.messenger.MessagesController.getInstance(r6);
        r5 = r5.user_id;
        r5 = java.lang.Integer.valueOf(r5);
        r5 = r6.getUser(r5);
        if (r5 == 0) goto L_0x044b;
    L_0x040e:
        r8 = r8 + 1;
        r6 = r5.first_name;
        if (r6 == 0) goto L_0x0415;
    L_0x0414:
        goto L_0x0417;
    L_0x0415:
        r6 = r23;
    L_0x0417:
        r5 = r5.last_name;
        if (r5 == 0) goto L_0x041c;
    L_0x041b:
        goto L_0x041e;
    L_0x041c:
        r5 = r23;
    L_0x041e:
        r7 = r4.first_name;
        r6 = r6.equals(r7);
        if (r6 == 0) goto L_0x042e;
    L_0x0426:
        r6 = r4.last_name;
        r5 = r5.equals(r6);
        if (r5 != 0) goto L_0x043e;
    L_0x042e:
        r5 = r4.first_name;
        r5 = android.text.TextUtils.isEmpty(r5);
        if (r5 == 0) goto L_0x044b;
    L_0x0436:
        r5 = r4.last_name;
        r5 = android.text.TextUtils.isEmpty(r5);
        if (r5 == 0) goto L_0x044b;
    L_0x043e:
        r15 = 32;
        goto L_0x0473;
    L_0x0441:
        r5 = r13.contactsByShortPhone;
        r5 = r5.containsKey(r6);
        if (r5 == 0) goto L_0x044b;
    L_0x0449:
        r8 = r8 + 1;
    L_0x044b:
        r5 = new org.telegram.tgnet.TLRPC$TL_inputPhoneContact;
        r5.<init>();
        r6 = r4.contact_id;
        r6 = (long) r6;
        r5.client_id = r6;
        r6 = r5.client_id;
        r11 = (long) r2;
        r15 = 32;
        r11 = r11 << r15;
        r6 = r6 | r11;
        r5.client_id = r6;
        r6 = r4.first_name;
        r5.first_name = r6;
        r6 = r4.last_name;
        r5.last_name = r6;
        r6 = r4.phones;
        r6 = r6.get(r2);
        r6 = (java.lang.String) r6;
        r5.phone = r6;
        r9.add(r5);
    L_0x0473:
        r2 = r2 + 1;
        goto L_0x03d1;
    L_0x0477:
        r7 = 0;
        goto L_0x047b;
    L_0x0479:
        r7 = 0;
        r8 = 0;
    L_0x047b:
        r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED;
        if (r0 == 0) goto L_0x0484;
    L_0x047f:
        r0 = "done processing contacts";
        org.telegram.messenger.FileLog.d(r0);
    L_0x0484:
        if (r29 == 0) goto L_0x05e3;
    L_0x0486:
        r0 = r9.isEmpty();
        if (r0 != 0) goto L_0x05c6;
    L_0x048c:
        r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED;
        if (r0 == 0) goto L_0x0495;
    L_0x0490:
        r0 = "start import contacts";
        org.telegram.messenger.FileLog.e(r0);
    L_0x0495:
        r0 = 2;
        if (r32 == 0) goto L_0x04b9;
    L_0x0498:
        if (r7 == 0) goto L_0x04b9;
    L_0x049a:
        r2 = 30;
        if (r7 < r2) goto L_0x04a0;
    L_0x049e:
        r2 = 1;
        goto L_0x04ba;
    L_0x04a0:
        if (r30 == 0) goto L_0x04b9;
    L_0x04a2:
        if (r1 != 0) goto L_0x04b9;
    L_0x04a4:
        r1 = r13.contactsByPhone;
        r1 = r1.size();
        r1 = r1 - r8;
        r2 = r13.contactsByPhone;
        r2 = r2.size();
        r2 = r2 / 3;
        r2 = r2 * 2;
        if (r1 <= r2) goto L_0x04b9;
    L_0x04b7:
        r2 = 2;
        goto L_0x04ba;
    L_0x04b9:
        r2 = 0;
    L_0x04ba:
        r0 = org.telegram.messenger.BuildVars.LOGS_ENABLED;
        if (r0 == 0) goto L_0x04e8;
    L_0x04be:
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r1 = "new phone book contacts ";
        r0.append(r1);
        r0.append(r7);
        r1 = " serverContactsInPhonebook ";
        r0.append(r1);
        r0.append(r8);
        r1 = " totalContacts ";
        r0.append(r1);
        r1 = r13.contactsByPhone;
        r1 = r1.size();
        r0.append(r1);
        r0 = r0.toString();
        org.telegram.messenger.FileLog.d(r0);
    L_0x04e8:
        if (r2 == 0) goto L_0x04fc;
    L_0x04ea:
        r6 = new org.telegram.messenger.-$$Lambda$ContactsController$1bnNRpKN4G2RMftCjRi6zZdnM-I;
        r0 = r6;
        r1 = r26;
        r3 = r27;
        r4 = r30;
        r5 = r28;
        r0.<init>(r1, r2, r3, r4, r5);
        org.telegram.messenger.AndroidUtilities.runOnUIThread(r6);
        return;
    L_0x04fc:
        if (r33 == 0) goto L_0x0516;
    L_0x04fe:
        r8 = org.telegram.messenger.Utilities.stageQueue;
        r9 = new org.telegram.messenger.-$$Lambda$ContactsController$GNntUmuXLL6W1VYySyoHwi7w0_8;
        r0 = r9;
        r1 = r26;
        r2 = r10;
        r3 = r14;
        r4 = r30;
        r5 = r24;
        r6 = r25;
        r7 = r22;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7);
        r8.postRunnable(r9);
        return;
    L_0x0516:
        r0 = 1;
        r15 = new boolean[r0];
        r0 = 0;
        r15[r0] = r0;
        r12 = new java.util.HashMap;
        r12.<init>(r14);
        r11 = new android.util.SparseArray;
        r11.<init>();
        r0 = r12.entrySet();
        r0 = r0.iterator();
    L_0x052e:
        r1 = r0.hasNext();
        if (r1 == 0) goto L_0x0548;
    L_0x0534:
        r1 = r0.next();
        r1 = (java.util.Map.Entry) r1;
        r1 = r1.getValue();
        r1 = (org.telegram.messenger.ContactsController.Contact) r1;
        r2 = r1.contact_id;
        r1 = r1.key;
        r11.put(r2, r1);
        goto L_0x052e;
    L_0x0548:
        r1 = 0;
        r13.completedRequestsCount = r1;
        r0 = r9.size();
        r0 = (double) r0;
        r2 = NUM; // 0x407fNUM float:0.0 double:500.0;
        java.lang.Double.isNaN(r0);
        r0 = r0 / r2;
        r0 = java.lang.Math.ceil(r0);
        r8 = (int) r0;
        r7 = 0;
    L_0x055f:
        if (r7 >= r8) goto L_0x0612;
    L_0x0561:
        r6 = new org.telegram.tgnet.TLRPC$TL_contacts_importContacts;
        r6.<init>();
        r0 = r7 * 500;
        r1 = r0 + 500;
        r2 = r9.size();
        r1 = java.lang.Math.min(r1, r2);
        r2 = new java.util.ArrayList;
        r0 = r9.subList(r0, r1);
        r2.<init>(r0);
        r6.contacts = r2;
        r0 = r13.currentAccount;
        r5 = org.telegram.tgnet.ConnectionsManager.getInstance(r0);
        r4 = new org.telegram.messenger.-$$Lambda$ContactsController$nCB3Aueb5LjSTAeo-ZnP4JYneR4;
        r0 = r4;
        r1 = r26;
        r2 = r12;
        r3 = r11;
        r13 = r4;
        r4 = r15;
        r16 = r15;
        r15 = r5;
        r5 = r14;
        r27 = r6;
        r20 = r7;
        r7 = r8;
        r17 = r8;
        r8 = r10;
        r18 = r9;
        r9 = r30;
        r19 = r10;
        r10 = r24;
        r23 = r11;
        r21 = r25;
        r11 = r21;
        r25 = r12;
        r12 = r22;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12);
        r0 = 6;
        r1 = r27;
        r15.sendRequest(r1, r13, r0);
        r7 = r20 + 1;
        r13 = r26;
        r15 = r16;
        r8 = r17;
        r9 = r18;
        r10 = r19;
        r11 = r23;
        r12 = r25;
        r25 = r21;
        goto L_0x055f;
    L_0x05c6:
        r19 = r10;
        r21 = r25;
        r8 = org.telegram.messenger.Utilities.stageQueue;
        r9 = new org.telegram.messenger.-$$Lambda$ContactsController$-jMNdCLASSNAMENspFJ8mldMYkB_XJE88;
        r0 = r9;
        r1 = r26;
        r2 = r19;
        r3 = r14;
        r4 = r30;
        r5 = r24;
        r6 = r21;
        r7 = r22;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7);
        r8.postRunnable(r9);
        goto L_0x0612;
    L_0x05e3:
        r19 = r10;
        r21 = r25;
        r8 = org.telegram.messenger.Utilities.stageQueue;
        r9 = new org.telegram.messenger.-$$Lambda$ContactsController$VZyJqVQUXu54Ctl7i8Dn-JZ0oQE;
        r0 = r9;
        r1 = r26;
        r2 = r19;
        r3 = r14;
        r4 = r30;
        r5 = r24;
        r6 = r21;
        r7 = r22;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7);
        r8.postRunnable(r9);
        r0 = r14.isEmpty();
        if (r0 != 0) goto L_0x0612;
    L_0x0605:
        r0 = r26;
        r1 = r0.currentAccount;
        r1 = org.telegram.messenger.MessagesStorage.getInstance(r1);
        r2 = 0;
        r1.putCachedPhoneBook(r14, r2, r2);
        goto L_0x0614;
    L_0x0612:
        r0 = r26;
    L_0x0614:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ContactsController.lambda$performSyncPhoneBook$24$ContactsController(java.util.HashMap, boolean, boolean, boolean, boolean, boolean, boolean):void");
    }

    public /* synthetic */ void lambda$null$13$ContactsController(int i, HashMap hashMap, boolean z, boolean z2) {
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.hasNewContactsToImport, Integer.valueOf(i), hashMap, Boolean.valueOf(z), Boolean.valueOf(z2));
    }

    public /* synthetic */ void lambda$null$15$ContactsController(HashMap hashMap, HashMap hashMap2, boolean z, HashMap hashMap3, ArrayList arrayList, HashMap hashMap4) {
        this.contactsBookSPhones = hashMap;
        this.contactsBook = hashMap2;
        this.contactsSyncInProgress = false;
        this.contactsBookLoaded = true;
        if (z) {
            this.contactsLoaded = true;
        }
        if (!this.delayedContactsUpdate.isEmpty() && this.contactsLoaded) {
            applyContactsUpdates(this.delayedContactsUpdate, null, null, null);
            this.delayedContactsUpdate.clear();
        }
        MessagesStorage.getInstance(this.currentAccount).putCachedPhoneBook(hashMap2, false, false);
        AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$HLpbAIiF9PYZwFaig6jCvfwgV6A(this, hashMap3, arrayList, hashMap4));
    }

    public /* synthetic */ void lambda$null$14$ContactsController(HashMap hashMap, ArrayList arrayList, HashMap hashMap2) {
        mergePhonebookAndTelegramContacts(hashMap, arrayList, hashMap2);
        updateUnregisteredContacts();
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoad, new Object[0]);
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.contactsImported, new Object[0]);
    }

    public /* synthetic */ void lambda$null$19$ContactsController(HashMap hashMap, SparseArray sparseArray, boolean[] zArr, HashMap hashMap2, TL_contacts_importContacts tL_contacts_importContacts, int i, HashMap hashMap3, boolean z, HashMap hashMap4, ArrayList arrayList, HashMap hashMap5, TLObject tLObject, TL_error tL_error) {
        HashMap hashMap6 = hashMap;
        SparseArray sparseArray2 = sparseArray;
        TL_contacts_importContacts tL_contacts_importContacts2 = tL_contacts_importContacts;
        TL_error tL_error2 = tL_error;
        this.completedRequestsCount++;
        HashMap hashMap7;
        if (tL_error2 == null) {
            int i2;
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("contacts imported");
            }
            TL_contacts_importedContacts tL_contacts_importedContacts = (TL_contacts_importedContacts) tLObject;
            if (!tL_contacts_importedContacts.retry_contacts.isEmpty()) {
                for (i2 = 0; i2 < tL_contacts_importedContacts.retry_contacts.size(); i2++) {
                    hashMap.remove(sparseArray.get((int) ((Long) tL_contacts_importedContacts.retry_contacts.get(i2)).longValue()));
                }
                zArr[0] = true;
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("result has retry contacts");
                }
            }
            for (i2 = 0; i2 < tL_contacts_importedContacts.popular_invites.size(); i2++) {
                TL_popularContact tL_popularContact = (TL_popularContact) tL_contacts_importedContacts.popular_invites.get(i2);
                Contact contact = (Contact) hashMap2.get(sparseArray.get((int) tL_popularContact.client_id));
                if (contact != null) {
                    contact.imported = tL_popularContact.importers;
                }
            }
            hashMap7 = hashMap2;
            MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(tL_contacts_importedContacts.users, null, true, true);
            ArrayList arrayList2 = new ArrayList();
            for (i2 = 0; i2 < tL_contacts_importedContacts.imported.size(); i2++) {
                TL_contact tL_contact = new TL_contact();
                tL_contact.user_id = ((TL_importedContact) tL_contacts_importedContacts.imported.get(i2)).user_id;
                arrayList2.add(tL_contact);
            }
            processLoadedContacts(arrayList2, tL_contacts_importedContacts.users, 2);
        } else {
            hashMap7 = hashMap2;
            for (int i3 = 0; i3 < tL_contacts_importContacts2.contacts.size(); i3++) {
                hashMap.remove(sparseArray.get((int) ((TL_inputPhoneContact) tL_contacts_importContacts2.contacts.get(i3)).client_id));
            }
            zArr[0] = true;
            if (BuildVars.LOGS_ENABLED) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("import contacts error ");
                stringBuilder.append(tL_error2.text);
                FileLog.d(stringBuilder.toString());
            }
        }
        if (this.completedRequestsCount == i) {
            if (!hashMap.isEmpty()) {
                MessagesStorage.getInstance(this.currentAccount).putCachedPhoneBook(hashMap, false, false);
            }
            Utilities.stageQueue.postRunnable(new -$$Lambda$ContactsController$5_qynEMvCO-Vcwp4tn_AnSTIGhc(this, hashMap3, hashMap2, z, hashMap4, arrayList, hashMap5, zArr));
        }
    }

    public /* synthetic */ void lambda$null$18$ContactsController(HashMap hashMap, HashMap hashMap2, boolean z, HashMap hashMap3, ArrayList arrayList, HashMap hashMap4, boolean[] zArr) {
        this.contactsBookSPhones = hashMap;
        this.contactsBook = hashMap2;
        this.contactsSyncInProgress = false;
        this.contactsBookLoaded = true;
        if (z) {
            this.contactsLoaded = true;
        }
        if (!this.delayedContactsUpdate.isEmpty() && this.contactsLoaded) {
            applyContactsUpdates(this.delayedContactsUpdate, null, null, null);
            this.delayedContactsUpdate.clear();
        }
        AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$QCeCLASSNAMEnQJOhdVmTlR71H9gJSWro(this, hashMap3, arrayList, hashMap4));
        if (zArr[0]) {
            Utilities.globalQueue.postRunnable(new -$$Lambda$ContactsController$jDhNdq7M1sl26gLErTQhOHpc1l4(this), 300000);
        }
    }

    public /* synthetic */ void lambda$null$16$ContactsController(HashMap hashMap, ArrayList arrayList, HashMap hashMap2) {
        mergePhonebookAndTelegramContacts(hashMap, arrayList, hashMap2);
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.contactsImported, new Object[0]);
    }

    public /* synthetic */ void lambda$null$17$ContactsController() {
        MessagesStorage.getInstance(this.currentAccount).getCachedPhoneBook(true);
    }

    public /* synthetic */ void lambda$null$21$ContactsController(HashMap hashMap, HashMap hashMap2, boolean z, HashMap hashMap3, ArrayList arrayList, HashMap hashMap4) {
        this.contactsBookSPhones = hashMap;
        this.contactsBook = hashMap2;
        this.contactsSyncInProgress = false;
        this.contactsBookLoaded = true;
        if (z) {
            this.contactsLoaded = true;
        }
        if (!this.delayedContactsUpdate.isEmpty() && this.contactsLoaded) {
            applyContactsUpdates(this.delayedContactsUpdate, null, null, null);
            this.delayedContactsUpdate.clear();
        }
        AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$P0Dtr3FaFqbEIj7kQUrR_yvUd8o(this, hashMap3, arrayList, hashMap4));
    }

    public /* synthetic */ void lambda$null$20$ContactsController(HashMap hashMap, ArrayList arrayList, HashMap hashMap2) {
        mergePhonebookAndTelegramContacts(hashMap, arrayList, hashMap2);
        updateUnregisteredContacts();
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoad, new Object[0]);
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.contactsImported, new Object[0]);
    }

    public /* synthetic */ void lambda$null$23$ContactsController(HashMap hashMap, HashMap hashMap2, boolean z, HashMap hashMap3, ArrayList arrayList, HashMap hashMap4) {
        this.contactsBookSPhones = hashMap;
        this.contactsBook = hashMap2;
        this.contactsSyncInProgress = false;
        this.contactsBookLoaded = true;
        if (z) {
            this.contactsLoaded = true;
        }
        if (!this.delayedContactsUpdate.isEmpty() && this.contactsLoaded && this.contactsBookLoaded) {
            applyContactsUpdates(this.delayedContactsUpdate, null, null, null);
            this.delayedContactsUpdate.clear();
        }
        AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$3JHpI-WMQtsFtzkjJ3kfvTUf7ro(this, hashMap3, arrayList, hashMap4));
    }

    public /* synthetic */ void lambda$null$22$ContactsController(HashMap hashMap, ArrayList arrayList, HashMap hashMap2) {
        mergePhonebookAndTelegramContacts(hashMap, arrayList, hashMap2);
    }

    public boolean isLoadingContacts() {
        boolean z;
        synchronized (this.loadContactsSync) {
            z = this.loadingContacts;
        }
        return z;
    }

    private int getContactsHash(ArrayList<TL_contact> arrayList) {
        ArrayList arrayList2 = new ArrayList(arrayList);
        Collections.sort(arrayList2, -$$Lambda$ContactsController$TrjXA3zXxBZ5H4pd0ZldkUjIm4Y.INSTANCE);
        int size = arrayList2.size();
        long j = 0;
        for (int i = -1; i < size; i++) {
            int i2;
            if (i == -1) {
                j = (j * 20261) + 2147483648L;
                i2 = UserConfig.getInstance(this.currentAccount).contactsSavedCount;
            } else {
                j = (j * 20261) + 2147483648L;
                i2 = ((TL_contact) arrayList2.get(i)).user_id;
            }
            j = (j + ((long) i2)) % 2147483648L;
        }
        return (int) j;
    }

    static /* synthetic */ int lambda$getContactsHash$25(TL_contact tL_contact, TL_contact tL_contact2) {
        int i = tL_contact.user_id;
        int i2 = tL_contact2.user_id;
        if (i > i2) {
            return 1;
        }
        return i < i2 ? -1 : 0;
    }

    public void loadContacts(boolean z, int i) {
        synchronized (this.loadContactsSync) {
            this.loadingContacts = true;
        }
        if (z) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("load contacts from cache");
            }
            MessagesStorage.getInstance(this.currentAccount).getContacts();
            return;
        }
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("load contacts from server");
        }
        TL_contacts_getContacts tL_contacts_getContacts = new TL_contacts_getContacts();
        tL_contacts_getContacts.hash = i;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(tL_contacts_getContacts, new -$$Lambda$ContactsController$LgaPvpXbmtv6_BmnTvc8qVOudt4(this, i));
    }

    public /* synthetic */ void lambda$loadContacts$27$ContactsController(int i, TLObject tLObject, TL_error tL_error) {
        if (tL_error == null) {
            contacts_Contacts contacts_contacts = (contacts_Contacts) tLObject;
            if (i == 0 || !(contacts_contacts instanceof TL_contacts_contactsNotModified)) {
                UserConfig.getInstance(this.currentAccount).contactsSavedCount = contacts_contacts.saved_count;
                UserConfig.getInstance(this.currentAccount).saveConfig(false);
                processLoadedContacts(contacts_contacts.contacts, contacts_contacts.users, 0);
            } else {
                this.contactsLoaded = true;
                if (!this.delayedContactsUpdate.isEmpty() && this.contactsBookLoaded) {
                    applyContactsUpdates(this.delayedContactsUpdate, null, null, null);
                    this.delayedContactsUpdate.clear();
                }
                UserConfig.getInstance(this.currentAccount).lastContactsSyncTime = (int) (System.currentTimeMillis() / 1000);
                UserConfig.getInstance(this.currentAccount).saveConfig(false);
                AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$gXCq3FitJ9fiYzIr_FTcMY_P3T0(this));
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("load contacts don't change");
                }
            }
        }
    }

    public /* synthetic */ void lambda$null$26$ContactsController() {
        synchronized (this.loadContactsSync) {
            this.loadingContacts = false;
        }
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoad, new Object[0]);
    }

    public void processLoadedContacts(ArrayList<TL_contact> arrayList, ArrayList<User> arrayList2, int i) {
        AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$EZvJWephnINCisVyT9aioUlUdEQ(this, arrayList2, i, arrayList));
    }

    public /* synthetic */ void lambda$processLoadedContacts$35$ContactsController(ArrayList arrayList, int i, ArrayList arrayList2) {
        int i2 = 0;
        MessagesController.getInstance(this.currentAccount).putUsers(arrayList, i == 1);
        SparseArray sparseArray = new SparseArray();
        boolean isEmpty = arrayList2.isEmpty();
        if (!this.contacts.isEmpty()) {
            int i3 = 0;
            while (i3 < arrayList2.size()) {
                if (this.contactsDict.get(Integer.valueOf(((TL_contact) arrayList2.get(i3)).user_id)) != null) {
                    arrayList2.remove(i3);
                    i3--;
                }
                i3++;
            }
            arrayList2.addAll(this.contacts);
        }
        while (i2 < arrayList2.size()) {
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TL_contact) arrayList2.get(i2)).user_id));
            if (user != null) {
                sparseArray.put(user.id, user);
            }
            i2++;
        }
        Utilities.stageQueue.postRunnable(new -$$Lambda$ContactsController$FhL_9umLAIuYNOniVSTFnjIHfVA(this, i, arrayList2, sparseArray, arrayList, isEmpty));
    }

    public /* synthetic */ void lambda$null$34$ContactsController(int i, ArrayList arrayList, SparseArray sparseArray, ArrayList arrayList2, boolean z) {
        HashMap hashMap;
        HashMap hashMap2;
        int i2 = i;
        ArrayList arrayList3 = arrayList;
        SparseArray sparseArray2 = sparseArray;
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("done loading contacts");
        }
        if (i2 == 1 && (arrayList.isEmpty() || Math.abs((System.currentTimeMillis() / 1000) - ((long) UserConfig.getInstance(this.currentAccount).lastContactsSyncTime)) >= 86400)) {
            loadContacts(false, getContactsHash(arrayList3));
            if (arrayList.isEmpty()) {
                return;
            }
        }
        if (i2 == 0) {
            UserConfig.getInstance(this.currentAccount).lastContactsSyncTime = (int) (System.currentTimeMillis() / 1000);
            UserConfig.getInstance(this.currentAccount).saveConfig(false);
        }
        int i3 = 0;
        while (i3 < arrayList.size()) {
            TL_contact tL_contact = (TL_contact) arrayList3.get(i3);
            if (sparseArray2.get(tL_contact.user_id) != null || tL_contact.user_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                i3++;
            } else {
                loadContacts(false, 0);
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("contacts are broken, load from server");
                }
                return;
            }
        }
        if (i2 != 1) {
            MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(arrayList2, null, true, true);
            MessagesStorage.getInstance(this.currentAccount).putContacts(arrayList3, i2 != 2);
        }
        Collections.sort(arrayList3, new -$$Lambda$ContactsController$hjm8gMZUsX8pFMsciXhWnMP28rs(sparseArray2));
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap(20, 1.0f, 2);
        HashMap hashMap3 = new HashMap();
        HashMap hashMap4 = new HashMap();
        ArrayList arrayList4 = new ArrayList();
        ArrayList arrayList5 = new ArrayList();
        if (this.contactsBookLoaded) {
            hashMap = null;
            hashMap2 = hashMap;
        } else {
            HashMap hashMap5 = new HashMap();
            hashMap2 = new HashMap();
            hashMap = hashMap5;
        }
        i3 = 0;
        while (i3 < arrayList.size()) {
            TL_contact tL_contact2 = (TL_contact) arrayList3.get(i3);
            User user = (User) sparseArray2.get(tL_contact2.user_id);
            if (user != null) {
                Object obj;
                concurrentHashMap.put(Integer.valueOf(tL_contact2.user_id), tL_contact2);
                if (!(hashMap == null || TextUtils.isEmpty(user.phone))) {
                    hashMap.put(user.phone, tL_contact2);
                    String str = user.phone;
                    hashMap2.put(str.substring(Math.max(0, str.length() - 7)), tL_contact2);
                }
                String firstName = UserObject.getFirstName(user);
                if (firstName.length() > 1) {
                    firstName = firstName.substring(0, 1);
                }
                if (firstName.length() == 0) {
                    obj = "#";
                } else {
                    obj = firstName.toUpperCase();
                }
                String str2 = (String) this.sectionsToReplace.get(obj);
                if (str2 != null) {
                    obj = str2;
                }
                arrayList3 = (ArrayList) hashMap3.get(obj);
                if (arrayList3 == null) {
                    arrayList3 = new ArrayList();
                    hashMap3.put(obj, arrayList3);
                    arrayList4.add(obj);
                }
                arrayList3.add(tL_contact2);
                if (user.mutual_contact) {
                    arrayList3 = (ArrayList) hashMap4.get(obj);
                    if (arrayList3 == null) {
                        arrayList3 = new ArrayList();
                        hashMap4.put(obj, arrayList3);
                        arrayList5.add(obj);
                    }
                    arrayList3.add(tL_contact2);
                }
            }
            i3++;
            arrayList3 = arrayList;
            sparseArray2 = sparseArray;
        }
        Collections.sort(arrayList4, -$$Lambda$ContactsController$Io20DZyV84DSau97gyyKYypoUA8.INSTANCE);
        Collections.sort(arrayList5, -$$Lambda$ContactsController$DcIoZeDGqSMeB8gKr5g3ew0Ok2Y.INSTANCE);
        AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$1CJ23H1PXk1UitBXEZ5w9tuujVY(this, arrayList, concurrentHashMap, hashMap3, hashMap4, arrayList4, arrayList5, i, z));
        if (!this.delayedContactsUpdate.isEmpty() && this.contactsLoaded && this.contactsBookLoaded) {
            applyContactsUpdates(this.delayedContactsUpdate, null, null, null);
            this.delayedContactsUpdate.clear();
        }
        if (hashMap != null) {
            AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$VBtQ6OXrlCuEUdHYI1TREe5kYV4(this, hashMap, hashMap2));
        } else {
            this.contactsLoaded = true;
        }
    }

    static /* synthetic */ int lambda$null$29(String str, String str2) {
        char charAt = str.charAt(0);
        char charAt2 = str2.charAt(0);
        if (charAt == '#') {
            return 1;
        }
        if (charAt2 == '#') {
            return -1;
        }
        return str.compareTo(str2);
    }

    static /* synthetic */ int lambda$null$30(String str, String str2) {
        char charAt = str.charAt(0);
        char charAt2 = str2.charAt(0);
        if (charAt == '#') {
            return 1;
        }
        if (charAt2 == '#') {
            return -1;
        }
        return str.compareTo(str2);
    }

    public /* synthetic */ void lambda$null$31$ContactsController(ArrayList arrayList, ConcurrentHashMap concurrentHashMap, HashMap hashMap, HashMap hashMap2, ArrayList arrayList2, ArrayList arrayList3, int i, boolean z) {
        this.contacts = arrayList;
        this.contactsDict = concurrentHashMap;
        this.usersSectionsDict = hashMap;
        this.usersMutualSectionsDict = hashMap2;
        this.sortedUsersSectionsArray = arrayList2;
        this.sortedUsersMutualSectionsArray = arrayList3;
        if (i != 2) {
            synchronized (this.loadContactsSync) {
                this.loadingContacts = false;
            }
        }
        performWriteContactsToPhoneBook();
        updateUnregisteredContacts();
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoad, new Object[0]);
        if (i == 1 || z) {
            reloadContactsStatusesMaybe();
        } else {
            saveContactsLoadTime();
        }
    }

    public /* synthetic */ void lambda$null$33$ContactsController(HashMap hashMap, HashMap hashMap2) {
        Utilities.globalQueue.postRunnable(new -$$Lambda$ContactsController$EHQlX5PriiljkxmdW3HZOsijhuY(this, hashMap, hashMap2));
        if (!this.contactsSyncInProgress) {
            this.contactsSyncInProgress = true;
            MessagesStorage.getInstance(this.currentAccount).getCachedPhoneBook(false);
        }
    }

    public /* synthetic */ void lambda$null$32$ContactsController(HashMap hashMap, HashMap hashMap2) {
        this.contactsByPhone = hashMap;
        this.contactsByShortPhone = hashMap2;
    }

    public boolean isContact(int i) {
        return this.contactsDict.get(Integer.valueOf(i)) != null;
    }

    private void reloadContactsStatusesMaybe() {
        try {
            if (MessagesController.getMainSettings(this.currentAccount).getLong("lastReloadStatusTime", 0) < System.currentTimeMillis() - 86400000) {
                reloadContactsStatuses();
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void saveContactsLoadTime() {
        try {
            MessagesController.getMainSettings(this.currentAccount).edit().putLong("lastReloadStatusTime", System.currentTimeMillis()).commit();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void mergePhonebookAndTelegramContacts(HashMap<String, ArrayList<Object>> hashMap, ArrayList<String> arrayList, HashMap<String, Contact> hashMap2) {
        Utilities.globalQueue.postRunnable(new -$$Lambda$ContactsController$gajOA1_r5XMonBIZ6Lvar_P0bD9I(this, new ArrayList(this.contacts), hashMap2, hashMap, arrayList));
    }

    public /* synthetic */ void lambda$mergePhonebookAndTelegramContacts$39$ContactsController(ArrayList arrayList, HashMap hashMap, HashMap hashMap2, ArrayList arrayList2) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TL_contact) arrayList.get(i)).user_id));
            if (!(user == null || TextUtils.isEmpty(user.phone))) {
                String str = user.phone;
                Contact contact = (Contact) hashMap.get(str.substring(Math.max(0, str.length() - 7)));
                if (contact == null) {
                    str = Contact.getLetter(user.first_name, user.last_name);
                    ArrayList arrayList3 = (ArrayList) hashMap2.get(str);
                    if (arrayList3 == null) {
                        arrayList3 = new ArrayList();
                        hashMap2.put(str, arrayList3);
                        arrayList2.add(str);
                    }
                    arrayList3.add(user);
                } else if (contact.user == null) {
                    contact.user = user;
                }
            }
        }
        for (ArrayList sort : hashMap2.values()) {
            Collections.sort(sort, -$$Lambda$ContactsController$433Sx76_-fODvK-PjR-GANTzejU.INSTANCE);
        }
        Collections.sort(arrayList2, -$$Lambda$ContactsController$8txvpYjs0528CTiynLTYRW8aKog.INSTANCE);
        AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$EJa18fQNfNOetnYr7I8VZ_MtAgo(this, arrayList2, hashMap2));
    }

    static /* synthetic */ int lambda$null$36(Object obj, Object obj2) {
        String formatName;
        User user;
        String str = "";
        if (obj instanceof User) {
            User user2 = (User) obj;
            formatName = formatName(user2.first_name, user2.last_name);
        } else if (obj instanceof Contact) {
            Contact contact = (Contact) obj;
            user = contact.user;
            formatName = user != null ? formatName(user.first_name, user.last_name) : formatName(contact.first_name, contact.last_name);
        } else {
            formatName = str;
        }
        if (obj2 instanceof User) {
            User user3 = (User) obj2;
            str = formatName(user3.first_name, user3.last_name);
        } else if (obj2 instanceof Contact) {
            String formatName2;
            Contact contact2 = (Contact) obj2;
            user = contact2.user;
            if (user != null) {
                formatName2 = formatName(user.first_name, user.last_name);
            } else {
                formatName2 = formatName(contact2.first_name, contact2.last_name);
            }
            str = formatName2;
        }
        return formatName.compareTo(str);
    }

    static /* synthetic */ int lambda$null$37(String str, String str2) {
        char charAt = str.charAt(0);
        char charAt2 = str2.charAt(0);
        if (charAt == '#') {
            return 1;
        }
        if (charAt2 == '#') {
            return -1;
        }
        return str.compareTo(str2);
    }

    public /* synthetic */ void lambda$null$38$ContactsController(ArrayList arrayList, HashMap hashMap) {
        this.phoneBookSectionsArray = arrayList;
        this.phoneBookSectionsDict = hashMap;
    }

    private void updateUnregisteredContacts() {
        HashMap hashMap = new HashMap();
        int size = this.contacts.size();
        for (int i = 0; i < size; i++) {
            TL_contact tL_contact = (TL_contact) this.contacts.get(i);
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(tL_contact.user_id));
            if (!(user == null || TextUtils.isEmpty(user.phone))) {
                hashMap.put(user.phone, tL_contact);
            }
        }
        ArrayList arrayList = new ArrayList();
        for (Entry value : this.contactsBook.entrySet()) {
            Object obj;
            Contact contact = (Contact) value.getValue();
            int i2 = 0;
            while (true) {
                obj = 1;
                if (i2 < contact.phones.size()) {
                    if (hashMap.containsKey((String) contact.shortPhones.get(i2)) || ((Integer) contact.phoneDeleted.get(i2)).intValue() == 1) {
                        break;
                    }
                    i2++;
                } else {
                    obj = null;
                    break;
                }
            }
            if (obj == null) {
                arrayList.add(contact);
            }
        }
        Collections.sort(arrayList, -$$Lambda$ContactsController$mHBJOOEPuO6QrX7ZbJrnWuc_NBQ.INSTANCE);
        this.phoneBookContacts = arrayList;
    }

    static /* synthetic */ int lambda$updateUnregisteredContacts$40(Contact contact, Contact contact2) {
        String str = contact.first_name;
        if (str.length() == 0) {
            str = contact.last_name;
        }
        String str2 = contact2.first_name;
        if (str2.length() == 0) {
            str2 = contact2.last_name;
        }
        return str.compareTo(str2);
    }

    private void buildContactsSectionsArrays(boolean z) {
        if (z) {
            Collections.sort(this.contacts, new -$$Lambda$ContactsController$l3XEyiXk02DazId-mQdpRpST3Co(this));
        }
        HashMap hashMap = new HashMap();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < this.contacts.size(); i++) {
            TL_contact tL_contact = (TL_contact) this.contacts.get(i);
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(tL_contact.user_id));
            if (user != null) {
                Object obj;
                String firstName = UserObject.getFirstName(user);
                if (firstName.length() > 1) {
                    firstName = firstName.substring(0, 1);
                }
                if (firstName.length() == 0) {
                    obj = "#";
                } else {
                    obj = firstName.toUpperCase();
                }
                String str = (String) this.sectionsToReplace.get(obj);
                if (str != null) {
                    obj = str;
                }
                ArrayList arrayList2 = (ArrayList) hashMap.get(obj);
                if (arrayList2 == null) {
                    arrayList2 = new ArrayList();
                    hashMap.put(obj, arrayList2);
                    arrayList.add(obj);
                }
                arrayList2.add(tL_contact);
            }
        }
        Collections.sort(arrayList, -$$Lambda$ContactsController$4fSnP4tj8Rx2FPjE7t10auDnEhg.INSTANCE);
        this.usersSectionsDict = hashMap;
        this.sortedUsersSectionsArray = arrayList;
    }

    public /* synthetic */ int lambda$buildContactsSectionsArrays$41$ContactsController(TL_contact tL_contact, TL_contact tL_contact2) {
        return UserObject.getFirstName(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(tL_contact.user_id))).compareTo(UserObject.getFirstName(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(tL_contact2.user_id))));
    }

    static /* synthetic */ int lambda$buildContactsSectionsArrays$42(String str, String str2) {
        char charAt = str.charAt(0);
        char charAt2 = str2.charAt(0);
        if (charAt == '#') {
            return 1;
        }
        if (charAt2 == '#') {
            return -1;
        }
        return str.compareTo(str2);
    }

    private boolean hasContactsPermission() {
        boolean z = true;
        if (VERSION.SDK_INT >= 23) {
            if (ApplicationLoader.applicationContext.checkSelfPermission("android.permission.READ_CONTACTS") != 0) {
                z = false;
            }
            return z;
        }
        Cursor cursor = null;
        try {
            cursor = ApplicationLoader.applicationContext.getContentResolver().query(Phone.CONTENT_URI, this.projectionPhones, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                return false;
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e2) {
                    FileLog.e(e2);
                }
            }
            return true;
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e22) {
                    FileLog.e(e22);
                }
            }
        }
    }

    private void performWriteContactsToPhoneBook() {
        Utilities.phoneBookQueue.postRunnable(new -$$Lambda$ContactsController$E0GDxDm4XOysG7SHcvpj5BVOSkQ(this, new ArrayList(this.contacts)));
    }

    public /* synthetic */ void lambda$performWriteContactsToPhoneBook$43$ContactsController(ArrayList arrayList) {
        performWriteContactsToPhoneBookInternal(arrayList);
    }

    private void applyContactsUpdates(ArrayList<Integer> arrayList, ConcurrentHashMap<Integer, User> concurrentHashMap, ArrayList<TL_contact> arrayList2, ArrayList<Integer> arrayList3) {
        ArrayList arrayList22;
        ArrayList arrayList32;
        Integer num;
        StringBuilder stringBuilder;
        String str;
        int i = 0;
        if (arrayList22 == null || arrayList32 == null) {
            arrayList22 = new ArrayList();
            arrayList32 = new ArrayList();
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                num = (Integer) arrayList.get(i2);
                if (num.intValue() > 0) {
                    TL_contact tL_contact = new TL_contact();
                    tL_contact.user_id = num.intValue();
                    arrayList22.add(tL_contact);
                } else if (num.intValue() < 0) {
                    arrayList32.add(Integer.valueOf(-num.intValue()));
                }
            }
        }
        if (BuildVars.LOGS_ENABLED) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("process update - contacts add = ");
            stringBuilder.append(arrayList22.size());
            stringBuilder.append(" delete = ");
            stringBuilder.append(arrayList32.size());
            FileLog.d(stringBuilder.toString());
        }
        stringBuilder = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        int i3 = 0;
        Object obj = null;
        while (true) {
            str = ",";
            User user = null;
            if (i3 >= arrayList22.size()) {
                break;
            }
            TL_contact tL_contact2 = (TL_contact) arrayList22.get(i3);
            if (concurrentHashMap != null) {
                user = (User) concurrentHashMap.get(Integer.valueOf(tL_contact2.user_id));
            }
            if (user == null) {
                user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(tL_contact2.user_id));
            } else {
                MessagesController.getInstance(this.currentAccount).putUser(user, true);
            }
            if (user == null || TextUtils.isEmpty(user.phone)) {
                obj = 1;
            } else {
                Contact contact = (Contact) this.contactsBookSPhones.get(user.phone);
                if (contact != null) {
                    int indexOf = contact.shortPhones.indexOf(user.phone);
                    if (indexOf != -1) {
                        contact.phoneDeleted.set(indexOf, Integer.valueOf(0));
                    }
                }
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(str);
                }
                stringBuilder.append(user.phone);
            }
            i3++;
        }
        while (i < arrayList32.size()) {
            num = (Integer) arrayList32.get(i);
            Utilities.phoneBookQueue.postRunnable(new -$$Lambda$ContactsController$HjkvfPeYk7Ow3aL58FFYm929xXw(this, num));
            User user2 = concurrentHashMap != null ? (User) concurrentHashMap.get(num) : null;
            if (user2 == null) {
                user2 = MessagesController.getInstance(this.currentAccount).getUser(num);
            } else {
                MessagesController.getInstance(this.currentAccount).putUser(user2, true);
            }
            if (user2 == null) {
                obj = 1;
            } else if (!TextUtils.isEmpty(user2.phone)) {
                Contact contact2 = (Contact) this.contactsBookSPhones.get(user2.phone);
                if (contact2 != null) {
                    int indexOf2 = contact2.shortPhones.indexOf(user2.phone);
                    if (indexOf2 != -1) {
                        contact2.phoneDeleted.set(indexOf2, Integer.valueOf(1));
                    }
                }
                if (stringBuilder2.length() != 0) {
                    stringBuilder2.append(str);
                }
                stringBuilder2.append(user2.phone);
            }
            i++;
        }
        if (!(stringBuilder.length() == 0 && stringBuilder2.length() == 0)) {
            MessagesStorage.getInstance(this.currentAccount).applyPhoneBookUpdates(stringBuilder.toString(), stringBuilder2.toString());
        }
        if (obj != null) {
            Utilities.stageQueue.postRunnable(new -$$Lambda$ContactsController$zT0R4MDQT-YLqu-ka0J-Safa99M(this));
        } else {
            AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$8jHuChSQV9WMksUcSKrM56MxPqE(this, arrayList22, arrayList32));
        }
    }

    public /* synthetic */ void lambda$applyContactsUpdates$44$ContactsController(Integer num) {
        deleteContactFromPhoneBook(num.intValue());
    }

    public /* synthetic */ void lambda$applyContactsUpdates$45$ContactsController() {
        loadContacts(false, 0);
    }

    public /* synthetic */ void lambda$applyContactsUpdates$46$ContactsController(ArrayList arrayList, ArrayList arrayList2) {
        int i;
        for (i = 0; i < arrayList.size(); i++) {
            TL_contact tL_contact = (TL_contact) arrayList.get(i);
            if (this.contactsDict.get(Integer.valueOf(tL_contact.user_id)) == null) {
                this.contacts.add(tL_contact);
                this.contactsDict.put(Integer.valueOf(tL_contact.user_id), tL_contact);
            }
        }
        for (i = 0; i < arrayList2.size(); i++) {
            Integer num = (Integer) arrayList2.get(i);
            TL_contact tL_contact2 = (TL_contact) this.contactsDict.get(num);
            if (tL_contact2 != null) {
                this.contacts.remove(tL_contact2);
                this.contactsDict.remove(num);
            }
        }
        if (!arrayList.isEmpty()) {
            updateUnregisteredContacts();
            performWriteContactsToPhoneBook();
        }
        performSyncPhoneBook(getContactsCopy(this.contactsBook), false, false, false, false, true, false);
        buildContactsSectionsArrays(arrayList.isEmpty() ^ 1);
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoad, new Object[0]);
    }

    public void processContactsUpdates(ArrayList<Integer> arrayList, ConcurrentHashMap<Integer, User> concurrentHashMap) {
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            Integer num = (Integer) it.next();
            int indexOf;
            if (num.intValue() > 0) {
                TL_contact tL_contact = new TL_contact();
                tL_contact.user_id = num.intValue();
                arrayList2.add(tL_contact);
                if (!this.delayedContactsUpdate.isEmpty()) {
                    indexOf = this.delayedContactsUpdate.indexOf(Integer.valueOf(-num.intValue()));
                    if (indexOf != -1) {
                        this.delayedContactsUpdate.remove(indexOf);
                    }
                }
            } else if (num.intValue() < 0) {
                arrayList3.add(Integer.valueOf(-num.intValue()));
                if (!this.delayedContactsUpdate.isEmpty()) {
                    indexOf = this.delayedContactsUpdate.indexOf(Integer.valueOf(-num.intValue()));
                    if (indexOf != -1) {
                        this.delayedContactsUpdate.remove(indexOf);
                    }
                }
            }
        }
        if (!arrayList3.isEmpty()) {
            MessagesStorage.getInstance(this.currentAccount).deleteContacts(arrayList3);
        }
        if (!arrayList2.isEmpty()) {
            MessagesStorage.getInstance(this.currentAccount).putContacts(arrayList2, false);
        }
        if (this.contactsLoaded && this.contactsBookLoaded) {
            applyContactsUpdates(arrayList, concurrentHashMap, arrayList2, arrayList3);
            return;
        }
        this.delayedContactsUpdate.addAll(arrayList);
        if (BuildVars.LOGS_ENABLED) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("delay update - contacts add = ");
            stringBuilder.append(arrayList2.size());
            stringBuilder.append(" delete = ");
            stringBuilder.append(arrayList3.size());
            FileLog.d(stringBuilder.toString());
        }
    }

    /* JADX WARNING: Missing block: B:43:0x013c, code skipped:
            return -1;
     */
    public long addContactToPhoneBook(org.telegram.tgnet.TLRPC.User r8, boolean r9) {
        /*
        r7 = this;
        r0 = r7.systemAccount;
        r1 = -1;
        if (r0 == 0) goto L_0x013c;
    L_0x0006:
        if (r8 == 0) goto L_0x013c;
    L_0x0008:
        r0 = r8.phone;
        r0 = android.text.TextUtils.isEmpty(r0);
        if (r0 == 0) goto L_0x0012;
    L_0x0010:
        goto L_0x013c;
    L_0x0012:
        r0 = r7.hasContactsPermission();
        if (r0 != 0) goto L_0x0019;
    L_0x0018:
        return r1;
    L_0x0019:
        r0 = r7.observerLock;
        monitor-enter(r0);
        r3 = 1;
        r7.ignoreChanges = r3;	 Catch:{ all -> 0x0139 }
        monitor-exit(r0);	 Catch:{ all -> 0x0139 }
        r0 = org.telegram.messenger.ApplicationLoader.applicationContext;
        r0 = r0.getContentResolver();
        if (r9 == 0) goto L_0x0065;
    L_0x0028:
        r9 = android.provider.ContactsContract.RawContacts.CONTENT_URI;	 Catch:{ Exception -> 0x0065 }
        r9 = r9.buildUpon();	 Catch:{ Exception -> 0x0065 }
        r3 = "caller_is_syncadapter";
        r4 = "true";
        r9 = r9.appendQueryParameter(r3, r4);	 Catch:{ Exception -> 0x0065 }
        r3 = "account_name";
        r4 = r7.systemAccount;	 Catch:{ Exception -> 0x0065 }
        r4 = r4.name;	 Catch:{ Exception -> 0x0065 }
        r9 = r9.appendQueryParameter(r3, r4);	 Catch:{ Exception -> 0x0065 }
        r3 = "account_type";
        r4 = r7.systemAccount;	 Catch:{ Exception -> 0x0065 }
        r4 = r4.type;	 Catch:{ Exception -> 0x0065 }
        r9 = r9.appendQueryParameter(r3, r4);	 Catch:{ Exception -> 0x0065 }
        r9 = r9.build();	 Catch:{ Exception -> 0x0065 }
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0065 }
        r3.<init>();	 Catch:{ Exception -> 0x0065 }
        r4 = "sync2 = ";
        r3.append(r4);	 Catch:{ Exception -> 0x0065 }
        r4 = r8.id;	 Catch:{ Exception -> 0x0065 }
        r3.append(r4);	 Catch:{ Exception -> 0x0065 }
        r3 = r3.toString();	 Catch:{ Exception -> 0x0065 }
        r4 = 0;
        r0.delete(r9, r3, r4);	 Catch:{ Exception -> 0x0065 }
    L_0x0065:
        r9 = new java.util.ArrayList;
        r9.<init>();
        r3 = android.provider.ContactsContract.RawContacts.CONTENT_URI;
        r3 = android.content.ContentProviderOperation.newInsert(r3);
        r4 = r7.systemAccount;
        r4 = r4.name;
        r5 = "account_name";
        r3.withValue(r5, r4);
        r4 = r7.systemAccount;
        r4 = r4.type;
        r5 = "account_type";
        r3.withValue(r5, r4);
        r4 = r8.phone;
        r5 = "sync1";
        r3.withValue(r5, r4);
        r4 = r8.id;
        r4 = java.lang.Integer.valueOf(r4);
        r5 = "sync2";
        r3.withValue(r5, r4);
        r3 = r3.build();
        r9.add(r3);
        r3 = android.provider.ContactsContract.Data.CONTENT_URI;
        r3 = android.content.ContentProviderOperation.newInsert(r3);
        r4 = 0;
        r5 = "raw_contact_id";
        r3.withValueBackReference(r5, r4);
        r5 = "mimetype";
        r6 = "vnd.android.cursor.item/name";
        r3.withValue(r5, r6);
        r5 = r8.first_name;
        r6 = "data2";
        r3.withValue(r6, r5);
        r5 = r8.last_name;
        r6 = "data3";
        r3.withValue(r6, r5);
        r3 = r3.build();
        r9.add(r3);
        r3 = android.provider.ContactsContract.Data.CONTENT_URI;
        r3 = android.content.ContentProviderOperation.newInsert(r3);
        r5 = "raw_contact_id";
        r3.withValueBackReference(r5, r4);
        r5 = "mimetype";
        r6 = "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile";
        r3.withValue(r5, r6);
        r5 = r8.id;
        r5 = java.lang.Integer.valueOf(r5);
        r6 = "data1";
        r3.withValue(r6, r5);
        r5 = "data2";
        r6 = "Telegram Profile";
        r3.withValue(r5, r6);
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "+";
        r5.append(r6);
        r6 = r8.phone;
        r5.append(r6);
        r5 = r5.toString();
        r6 = "data3";
        r3.withValue(r6, r5);
        r8 = r8.id;
        r8 = java.lang.Integer.valueOf(r8);
        r5 = "data4";
        r3.withValue(r5, r8);
        r8 = r3.build();
        r9.add(r8);
        r8 = "com.android.contacts";
        r8 = r0.applyBatch(r8, r9);	 Catch:{ Exception -> 0x012f }
        if (r8 == 0) goto L_0x012f;
    L_0x0119:
        r9 = r8.length;	 Catch:{ Exception -> 0x012f }
        if (r9 <= 0) goto L_0x012f;
    L_0x011c:
        r9 = r8[r4];	 Catch:{ Exception -> 0x012f }
        r9 = r9.uri;	 Catch:{ Exception -> 0x012f }
        if (r9 == 0) goto L_0x012f;
    L_0x0122:
        r8 = r8[r4];	 Catch:{ Exception -> 0x012f }
        r8 = r8.uri;	 Catch:{ Exception -> 0x012f }
        r8 = r8.getLastPathSegment();	 Catch:{ Exception -> 0x012f }
        r8 = java.lang.Long.parseLong(r8);	 Catch:{ Exception -> 0x012f }
        r1 = r8;
    L_0x012f:
        r8 = r7.observerLock;
        monitor-enter(r8);
        r7.ignoreChanges = r4;	 Catch:{ all -> 0x0136 }
        monitor-exit(r8);	 Catch:{ all -> 0x0136 }
        return r1;
    L_0x0136:
        r9 = move-exception;
        monitor-exit(r8);	 Catch:{ all -> 0x0136 }
        throw r9;
    L_0x0139:
        r8 = move-exception;
        monitor-exit(r0);	 Catch:{ all -> 0x0139 }
        throw r8;
    L_0x013c:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ContactsController.addContactToPhoneBook(org.telegram.tgnet.TLRPC$User, boolean):long");
    }

    private void deleteContactFromPhoneBook(int i) {
        if (hasContactsPermission()) {
            synchronized (this.observerLock) {
                this.ignoreChanges = true;
            }
            try {
                ContentResolver contentResolver = ApplicationLoader.applicationContext.getContentResolver();
                Uri build = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").appendQueryParameter("account_name", this.systemAccount.name).appendQueryParameter("account_type", this.systemAccount.type).build();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("sync2 = ");
                stringBuilder.append(i);
                contentResolver.delete(build, stringBuilder.toString(), null);
            } catch (Exception e) {
                FileLog.e(e);
            }
            synchronized (this.observerLock) {
                this.ignoreChanges = false;
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void markAsContacted(String str) {
        if (str != null) {
            Utilities.phoneBookQueue.postRunnable(new -$$Lambda$ContactsController$ZSZ9C_4-dtPH1zqggEDwmuwv69A(str));
        }
    }

    static /* synthetic */ void lambda$markAsContacted$47(String str) {
        Uri parse = Uri.parse(str);
        ContentValues contentValues = new ContentValues();
        contentValues.put("last_time_contacted", Long.valueOf(System.currentTimeMillis()));
        ApplicationLoader.applicationContext.getContentResolver().update(parse, contentValues, null, null);
    }

    public void addContact(User user) {
        if (user != null && !TextUtils.isEmpty(user.phone)) {
            TL_contacts_importContacts tL_contacts_importContacts = new TL_contacts_importContacts();
            ArrayList arrayList = new ArrayList();
            TL_inputPhoneContact tL_inputPhoneContact = new TL_inputPhoneContact();
            tL_inputPhoneContact.phone = user.phone;
            String str = "+";
            if (!tL_inputPhoneContact.phone.startsWith(str)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(str);
                stringBuilder.append(tL_inputPhoneContact.phone);
                tL_inputPhoneContact.phone = stringBuilder.toString();
            }
            tL_inputPhoneContact.first_name = user.first_name;
            tL_inputPhoneContact.last_name = user.last_name;
            tL_inputPhoneContact.client_id = 0;
            arrayList.add(tL_inputPhoneContact);
            tL_contacts_importContacts.contacts = arrayList;
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(tL_contacts_importContacts, new -$$Lambda$ContactsController$-Vi-J8-93cHW3trChoxTwI17XeA(this), 6);
        }
    }

    public /* synthetic */ void lambda$addContact$50$ContactsController(TLObject tLObject, TL_error tL_error) {
        if (tL_error == null) {
            TL_contacts_importedContacts tL_contacts_importedContacts = (TL_contacts_importedContacts) tLObject;
            MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(tL_contacts_importedContacts.users, null, true, true);
            for (int i = 0; i < tL_contacts_importedContacts.users.size(); i++) {
                User user = (User) tL_contacts_importedContacts.users.get(i);
                Utilities.phoneBookQueue.postRunnable(new -$$Lambda$ContactsController$tvZs_rDgKy4-ldDTgpQkKllXL0E(this, user));
                TL_contact tL_contact = new TL_contact();
                tL_contact.user_id = user.id;
                ArrayList arrayList = new ArrayList();
                arrayList.add(tL_contact);
                MessagesStorage.getInstance(this.currentAccount).putContacts(arrayList, false);
                if (!TextUtils.isEmpty(user.phone)) {
                    formatName(user.first_name, user.last_name);
                    MessagesStorage.getInstance(this.currentAccount).applyPhoneBookUpdates(user.phone, "");
                    Contact contact = (Contact) this.contactsBookSPhones.get(user.phone);
                    if (contact != null) {
                        int indexOf = contact.shortPhones.indexOf(user.phone);
                        if (indexOf != -1) {
                            contact.phoneDeleted.set(indexOf, Integer.valueOf(0));
                        }
                    }
                }
            }
            AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$3Lppzk_aX70-Q6C3adV0V2TBG9k(this, tL_contacts_importedContacts));
        }
    }

    public /* synthetic */ void lambda$null$48$ContactsController(User user) {
        addContactToPhoneBook(user, true);
    }

    public /* synthetic */ void lambda$null$49$ContactsController(TL_contacts_importedContacts tL_contacts_importedContacts) {
        Iterator it = tL_contacts_importedContacts.users.iterator();
        while (it.hasNext()) {
            User user = (User) it.next();
            MessagesController.getInstance(this.currentAccount).putUser(user, false);
            if (this.contactsDict.get(Integer.valueOf(user.id)) == null) {
                TL_contact tL_contact = new TL_contact();
                tL_contact.user_id = user.id;
                this.contacts.add(tL_contact);
                this.contactsDict.put(Integer.valueOf(tL_contact.user_id), tL_contact);
            }
        }
        buildContactsSectionsArrays(true);
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoad, new Object[0]);
    }

    public void deleteContact(ArrayList<User> arrayList) {
        if (arrayList != null && !arrayList.isEmpty()) {
            TL_contacts_deleteContacts tL_contacts_deleteContacts = new TL_contacts_deleteContacts();
            ArrayList arrayList2 = new ArrayList();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                User user = (User) it.next();
                InputUser inputUser = MessagesController.getInstance(this.currentAccount).getInputUser(user);
                if (inputUser != null) {
                    arrayList2.add(Integer.valueOf(user.id));
                    tL_contacts_deleteContacts.id.add(inputUser);
                }
            }
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(tL_contacts_deleteContacts, new -$$Lambda$ContactsController$AcnaGQSIUUZcaRV7Rm4-aExKG2o(this, arrayList2, arrayList));
        }
    }

    public /* synthetic */ void lambda$deleteContact$53$ContactsController(ArrayList arrayList, ArrayList arrayList2, TLObject tLObject, TL_error tL_error) {
        if (tL_error == null) {
            MessagesStorage.getInstance(this.currentAccount).deleteContacts(arrayList);
            Utilities.phoneBookQueue.postRunnable(new -$$Lambda$ContactsController$4aPOKb9vaAxgXYIv3var_-1I3WZs(this, arrayList2));
            for (int i = 0; i < arrayList2.size(); i++) {
                User user = (User) arrayList2.get(i);
                if (!TextUtils.isEmpty(user.phone)) {
                    UserObject.getUserName(user);
                    MessagesStorage.getInstance(this.currentAccount).applyPhoneBookUpdates(user.phone, "");
                    Contact contact = (Contact) this.contactsBookSPhones.get(user.phone);
                    if (contact != null) {
                        int indexOf = contact.shortPhones.indexOf(user.phone);
                        if (indexOf != -1) {
                            contact.phoneDeleted.set(indexOf, Integer.valueOf(1));
                        }
                    }
                }
            }
            AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$c7nr2gq-mBuBJyCFVFUiq4lubio(this, arrayList2));
        }
    }

    public /* synthetic */ void lambda$null$51$ContactsController(ArrayList arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            deleteContactFromPhoneBook(((User) it.next()).id);
        }
    }

    public /* synthetic */ void lambda$null$52$ContactsController(ArrayList arrayList) {
        Iterator it = arrayList.iterator();
        Object obj = null;
        while (it.hasNext()) {
            User user = (User) it.next();
            TL_contact tL_contact = (TL_contact) this.contactsDict.get(Integer.valueOf(user.id));
            if (tL_contact != null) {
                this.contacts.remove(tL_contact);
                this.contactsDict.remove(Integer.valueOf(user.id));
                obj = 1;
            }
        }
        if (obj != null) {
            buildContactsSectionsArrays(false);
        }
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(1));
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoad, new Object[0]);
    }

    public void reloadContactsStatuses() {
        saveContactsLoadTime();
        MessagesController.getInstance(this.currentAccount).clearFullUsers();
        Editor edit = MessagesController.getMainSettings(this.currentAccount).edit();
        edit.putBoolean("needGetStatuses", true).commit();
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(new TL_contacts_getStatuses(), new -$$Lambda$ContactsController$h5vCC-HpjKgEFkNX9o79KFdTfVk(this, edit));
    }

    public /* synthetic */ void lambda$reloadContactsStatuses$55$ContactsController(Editor editor, TLObject tLObject, TL_error tL_error) {
        if (tL_error == null) {
            AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$gWEMQfmNPp3nFPxuDjbs9cZtdo4(this, editor, tLObject));
        }
    }

    public /* synthetic */ void lambda$null$54$ContactsController(Editor editor, TLObject tLObject) {
        editor.remove("needGetStatuses").commit();
        Vector vector = (Vector) tLObject;
        if (!vector.objects.isEmpty()) {
            ArrayList arrayList = new ArrayList();
            Iterator it = vector.objects.iterator();
            while (it.hasNext()) {
                Object next = it.next();
                TL_user tL_user = new TL_user();
                TL_contactStatus tL_contactStatus = (TL_contactStatus) next;
                if (tL_contactStatus != null) {
                    UserStatus userStatus = tL_contactStatus.status;
                    if (userStatus instanceof TL_userStatusRecently) {
                        userStatus.expires = -100;
                    } else if (userStatus instanceof TL_userStatusLastWeek) {
                        userStatus.expires = -101;
                    } else if (userStatus instanceof TL_userStatusLastMonth) {
                        userStatus.expires = -102;
                    }
                    User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(tL_contactStatus.user_id));
                    if (user != null) {
                        user.status = tL_contactStatus.status;
                    }
                    tL_user.status = tL_contactStatus.status;
                    arrayList.add(tL_user);
                }
            }
            MessagesStorage.getInstance(this.currentAccount).updateUsers(arrayList, true, true, true);
        }
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(4));
    }

    public void loadPrivacySettings() {
        if (this.loadingDeleteInfo == 0) {
            this.loadingDeleteInfo = 1;
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(new TL_account_getAccountTTL(), new -$$Lambda$ContactsController$UpZcB_L92bZrGJkH0CSwUF9nhJQ(this));
        }
        int i = 0;
        while (true) {
            int[] iArr = this.loadingPrivacyInfo;
            if (i < iArr.length) {
                if (iArr[i] == 0) {
                    iArr[i] = 1;
                    TL_account_getPrivacy tL_account_getPrivacy = new TL_account_getPrivacy();
                    if (i == 0) {
                        tL_account_getPrivacy.key = new TL_inputPrivacyKeyStatusTimestamp();
                    } else if (i == 1) {
                        tL_account_getPrivacy.key = new TL_inputPrivacyKeyChatInvite();
                    } else if (i == 2) {
                        tL_account_getPrivacy.key = new TL_inputPrivacyKeyPhoneCall();
                    } else if (i == 3) {
                        tL_account_getPrivacy.key = new TL_inputPrivacyKeyPhoneP2P();
                    } else if (i != 4) {
                        tL_account_getPrivacy.key = new TL_inputPrivacyKeyForwards();
                    } else {
                        tL_account_getPrivacy.key = new TL_inputPrivacyKeyProfilePhoto();
                    }
                    ConnectionsManager.getInstance(this.currentAccount).sendRequest(tL_account_getPrivacy, new -$$Lambda$ContactsController$AbTZFLT5AeZ8Xev0g7z_25zx9YM(this, i));
                }
                i++;
            } else {
                NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
                return;
            }
        }
    }

    public /* synthetic */ void lambda$loadPrivacySettings$57$ContactsController(TLObject tLObject, TL_error tL_error) {
        AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$3k8Ydvk8Rxka6E1rBHMnjl6kFrM(this, tL_error, tLObject));
    }

    public /* synthetic */ void lambda$null$56$ContactsController(TL_error tL_error, TLObject tLObject) {
        if (tL_error == null) {
            this.deleteAccountTTL = ((TL_accountDaysTTL) tLObject).days;
            this.loadingDeleteInfo = 2;
        } else {
            this.loadingDeleteInfo = 0;
        }
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
    }

    public /* synthetic */ void lambda$loadPrivacySettings$59$ContactsController(int i, TLObject tLObject, TL_error tL_error) {
        AndroidUtilities.runOnUIThread(new -$$Lambda$ContactsController$EuAKug4zrNU4o4RK27r3kMVoC7I(this, tL_error, tLObject, i));
    }

    public /* synthetic */ void lambda$null$58$ContactsController(TL_error tL_error, TLObject tLObject, int i) {
        if (tL_error == null) {
            TL_account_privacyRules tL_account_privacyRules = (TL_account_privacyRules) tLObject;
            MessagesController.getInstance(this.currentAccount).putUsers(tL_account_privacyRules.users, false);
            if (i == 0) {
                this.privacyRules = tL_account_privacyRules.rules;
            } else if (i == 1) {
                this.groupPrivacyRules = tL_account_privacyRules.rules;
            } else if (i == 2) {
                this.callPrivacyRules = tL_account_privacyRules.rules;
            } else if (i == 3) {
                this.p2pPrivacyRules = tL_account_privacyRules.rules;
            } else if (i != 4) {
                this.forwardsPrivacyRules = tL_account_privacyRules.rules;
            } else {
                this.profilePhotoPrivacyRules = tL_account_privacyRules.rules;
            }
            this.loadingPrivacyInfo[i] = 2;
        } else {
            this.loadingPrivacyInfo[i] = 0;
        }
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
    }

    public void setDeleteAccountTTL(int i) {
        this.deleteAccountTTL = i;
    }

    public int getDeleteAccountTTL() {
        return this.deleteAccountTTL;
    }

    public boolean getLoadingDeleteInfo() {
        return this.loadingDeleteInfo != 2;
    }

    public boolean getLoadingPrivicyInfo(int i) {
        return this.loadingPrivacyInfo[i] != 2;
    }

    public ArrayList<PrivacyRule> getPrivacyRules(int i) {
        if (i == 5) {
            return this.forwardsPrivacyRules;
        }
        if (i == 4) {
            return this.profilePhotoPrivacyRules;
        }
        if (i == 3) {
            return this.p2pPrivacyRules;
        }
        if (i == 2) {
            return this.callPrivacyRules;
        }
        if (i == 1) {
            return this.groupPrivacyRules;
        }
        return this.privacyRules;
    }

    public void setPrivacyRules(ArrayList<PrivacyRule> arrayList, int i) {
        if (i == 5) {
            this.forwardsPrivacyRules = arrayList;
        } else if (i == 4) {
            this.profilePhotoPrivacyRules = arrayList;
        } else if (i == 3) {
            this.p2pPrivacyRules = arrayList;
        } else if (i == 2) {
            this.callPrivacyRules = arrayList;
        } else if (i == 1) {
            this.groupPrivacyRules = arrayList;
        } else {
            this.privacyRules = arrayList;
        }
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
        reloadContactsStatuses();
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0264 A:{Catch:{ Exception -> 0x026f }} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x00ca A:{Catch:{ Exception -> 0x026f }} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0107 A:{SYNTHETIC, Splitter:B:17:0x0107} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0264 A:{Catch:{ Exception -> 0x026f }} */
    public void createOrUpdateConnectionServiceContact(int r23, java.lang.String r24, java.lang.String r25) {
        /*
        r22 = this;
        r1 = r22;
        r0 = r23;
        r2 = r24;
        r3 = r25;
        r4 = "raw_contact_id=? AND mimetype=?";
        r5 = "vnd.android.cursor.item/group_membership";
        r6 = "TelegramConnectionService";
        r7 = "true";
        r8 = "caller_is_syncadapter";
        r9 = "mimetype";
        r10 = "";
        r11 = "raw_contact_id";
        r12 = r22.hasContactsPermission();
        if (r12 != 0) goto L_0x001f;
    L_0x001e:
        return;
    L_0x001f:
        r12 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Exception -> 0x026f }
        r12 = r12.getContentResolver();	 Catch:{ Exception -> 0x026f }
        r15 = new java.util.ArrayList;	 Catch:{ Exception -> 0x026f }
        r15.<init>();	 Catch:{ Exception -> 0x026f }
        r13 = android.provider.ContactsContract.Groups.CONTENT_URI;	 Catch:{ Exception -> 0x026f }
        r13 = r13.buildUpon();	 Catch:{ Exception -> 0x026f }
        r13 = r13.appendQueryParameter(r8, r7);	 Catch:{ Exception -> 0x026f }
        r14 = r13.build();	 Catch:{ Exception -> 0x026f }
        r13 = android.provider.ContactsContract.RawContacts.CONTENT_URI;	 Catch:{ Exception -> 0x026f }
        r13 = r13.buildUpon();	 Catch:{ Exception -> 0x026f }
        r7 = r13.appendQueryParameter(r8, r7);	 Catch:{ Exception -> 0x026f }
        r7 = r7.build();	 Catch:{ Exception -> 0x026f }
        r8 = 1;
        r13 = new java.lang.String[r8];	 Catch:{ Exception -> 0x026f }
        r16 = "_id";
        r8 = 0;
        r13[r8] = r16;	 Catch:{ Exception -> 0x026f }
        r16 = "title=? AND account_type=? AND account_name=?";
        r8 = 3;
        r19 = r9;
        r9 = new java.lang.String[r8];	 Catch:{ Exception -> 0x026f }
        r17 = 0;
        r9[r17] = r6;	 Catch:{ Exception -> 0x026f }
        r8 = r1.systemAccount;	 Catch:{ Exception -> 0x026f }
        r8 = r8.type;	 Catch:{ Exception -> 0x026f }
        r17 = 1;
        r9[r17] = r8;	 Catch:{ Exception -> 0x026f }
        r8 = r1.systemAccount;	 Catch:{ Exception -> 0x026f }
        r8 = r8.name;	 Catch:{ Exception -> 0x026f }
        r3 = 2;
        r9[r3] = r8;	 Catch:{ Exception -> 0x026f }
        r18 = 0;
        r8 = r13;
        r13 = r12;
        r20 = r14;
        r21 = r15;
        r15 = r8;
        r17 = r9;
        r8 = r13.query(r14, r15, r16, r17, r18);	 Catch:{ Exception -> 0x026f }
        r9 = "account_name";
        r15 = "account_type";
        if (r8 == 0) goto L_0x008b;
    L_0x007d:
        r13 = r8.moveToFirst();	 Catch:{ Exception -> 0x026f }
        if (r13 == 0) goto L_0x008b;
    L_0x0083:
        r13 = 0;
        r6 = r8.getInt(r13);	 Catch:{ Exception -> 0x026f }
        r16 = r15;
        goto L_0x00c8;
    L_0x008b:
        r13 = new android.content.ContentValues;	 Catch:{ Exception -> 0x026f }
        r13.<init>();	 Catch:{ Exception -> 0x026f }
        r14 = r1.systemAccount;	 Catch:{ Exception -> 0x026f }
        r14 = r14.type;	 Catch:{ Exception -> 0x026f }
        r13.put(r15, r14);	 Catch:{ Exception -> 0x026f }
        r14 = r1.systemAccount;	 Catch:{ Exception -> 0x026f }
        r14 = r14.name;	 Catch:{ Exception -> 0x026f }
        r13.put(r9, r14);	 Catch:{ Exception -> 0x026f }
        r14 = "group_visible";
        r16 = 0;
        r3 = java.lang.Integer.valueOf(r16);	 Catch:{ Exception -> 0x026f }
        r13.put(r14, r3);	 Catch:{ Exception -> 0x026f }
        r3 = "group_is_read_only";
        r16 = r15;
        r14 = 1;
        r15 = java.lang.Integer.valueOf(r14);	 Catch:{ Exception -> 0x026f }
        r13.put(r3, r15);	 Catch:{ Exception -> 0x026f }
        r3 = "title";
        r13.put(r3, r6);	 Catch:{ Exception -> 0x026f }
        r3 = r20;
        r3 = r12.insert(r3, r13);	 Catch:{ Exception -> 0x026f }
        r3 = r3.getLastPathSegment();	 Catch:{ Exception -> 0x026f }
        r6 = java.lang.Integer.parseInt(r3);	 Catch:{ Exception -> 0x026f }
    L_0x00c8:
        if (r8 == 0) goto L_0x00cd;
    L_0x00ca:
        r8.close();	 Catch:{ Exception -> 0x026f }
    L_0x00cd:
        r14 = android.provider.ContactsContract.Data.CONTENT_URI;	 Catch:{ Exception -> 0x026f }
        r3 = 1;
        r15 = new java.lang.String[r3];	 Catch:{ Exception -> 0x026f }
        r3 = 0;
        r15[r3] = r11;	 Catch:{ Exception -> 0x026f }
        r8 = "mimetype=? AND data1=?";
        r13 = 2;
        r3 = new java.lang.String[r13];	 Catch:{ Exception -> 0x026f }
        r13 = 0;
        r3[r13] = r5;	 Catch:{ Exception -> 0x026f }
        r13 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x026f }
        r13.<init>();	 Catch:{ Exception -> 0x026f }
        r13.append(r6);	 Catch:{ Exception -> 0x026f }
        r13.append(r10);	 Catch:{ Exception -> 0x026f }
        r13 = r13.toString();	 Catch:{ Exception -> 0x026f }
        r17 = 1;
        r3[r17] = r13;	 Catch:{ Exception -> 0x026f }
        r18 = 0;
        r13 = r12;
        r20 = r12;
        r12 = r16;
        r16 = r8;
        r17 = r3;
        r3 = r13.query(r14, r15, r16, r17, r18);	 Catch:{ Exception -> 0x026f }
        r8 = r21.size();	 Catch:{ Exception -> 0x026f }
        r13 = "data1";
        if (r3 == 0) goto L_0x01bf;
    L_0x0107:
        r14 = r3.moveToFirst();	 Catch:{ Exception -> 0x026f }
        if (r14 == 0) goto L_0x01bf;
    L_0x010d:
        r14 = 0;
        r5 = r3.getInt(r14);	 Catch:{ Exception -> 0x026f }
        r6 = android.content.ContentProviderOperation.newUpdate(r7);	 Catch:{ Exception -> 0x026f }
        r7 = "_id=?";
        r8 = 1;
        r9 = new java.lang.String[r8];	 Catch:{ Exception -> 0x026f }
        r8 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x026f }
        r8.<init>();	 Catch:{ Exception -> 0x026f }
        r8.append(r5);	 Catch:{ Exception -> 0x026f }
        r8.append(r10);	 Catch:{ Exception -> 0x026f }
        r8 = r8.toString();	 Catch:{ Exception -> 0x026f }
        r11 = 0;
        r9[r11] = r8;	 Catch:{ Exception -> 0x026f }
        r6 = r6.withSelection(r7, r9);	 Catch:{ Exception -> 0x026f }
        r7 = "deleted";
        r8 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x026f }
        r6 = r6.withValue(r7, r8);	 Catch:{ Exception -> 0x026f }
        r6 = r6.build();	 Catch:{ Exception -> 0x026f }
        r14 = r21;
        r14.add(r6);	 Catch:{ Exception -> 0x026f }
        r6 = android.provider.ContactsContract.Data.CONTENT_URI;	 Catch:{ Exception -> 0x026f }
        r6 = android.content.ContentProviderOperation.newUpdate(r6);	 Catch:{ Exception -> 0x026f }
        r7 = 2;
        r8 = new java.lang.String[r7];	 Catch:{ Exception -> 0x026f }
        r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x026f }
        r7.<init>();	 Catch:{ Exception -> 0x026f }
        r7.append(r5);	 Catch:{ Exception -> 0x026f }
        r7.append(r10);	 Catch:{ Exception -> 0x026f }
        r7 = r7.toString();	 Catch:{ Exception -> 0x026f }
        r9 = 0;
        r8[r9] = r7;	 Catch:{ Exception -> 0x026f }
        r7 = "vnd.android.cursor.item/phone_v2";
        r9 = 1;
        r8[r9] = r7;	 Catch:{ Exception -> 0x026f }
        r6 = r6.withSelection(r4, r8);	 Catch:{ Exception -> 0x026f }
        r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x026f }
        r7.<init>();	 Catch:{ Exception -> 0x026f }
        r8 = "+99084";
        r7.append(r8);	 Catch:{ Exception -> 0x026f }
        r7.append(r0);	 Catch:{ Exception -> 0x026f }
        r0 = r7.toString();	 Catch:{ Exception -> 0x026f }
        r0 = r6.withValue(r13, r0);	 Catch:{ Exception -> 0x026f }
        r0 = r0.build();	 Catch:{ Exception -> 0x026f }
        r14.add(r0);	 Catch:{ Exception -> 0x026f }
        r0 = android.provider.ContactsContract.Data.CONTENT_URI;	 Catch:{ Exception -> 0x026f }
        r0 = android.content.ContentProviderOperation.newUpdate(r0);	 Catch:{ Exception -> 0x026f }
        r6 = 2;
        r6 = new java.lang.String[r6];	 Catch:{ Exception -> 0x026f }
        r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x026f }
        r7.<init>();	 Catch:{ Exception -> 0x026f }
        r7.append(r5);	 Catch:{ Exception -> 0x026f }
        r7.append(r10);	 Catch:{ Exception -> 0x026f }
        r5 = r7.toString();	 Catch:{ Exception -> 0x026f }
        r7 = 0;
        r6[r7] = r5;	 Catch:{ Exception -> 0x026f }
        r5 = "vnd.android.cursor.item/name";
        r7 = 1;
        r6[r7] = r5;	 Catch:{ Exception -> 0x026f }
        r0 = r0.withSelection(r4, r6);	 Catch:{ Exception -> 0x026f }
        r4 = "data2";
        r0 = r0.withValue(r4, r2);	 Catch:{ Exception -> 0x026f }
        r2 = "data3";
        r4 = r25;
        r0 = r0.withValue(r2, r4);	 Catch:{ Exception -> 0x026f }
        r0 = r0.build();	 Catch:{ Exception -> 0x026f }
        r14.add(r0);	 Catch:{ Exception -> 0x026f }
        goto L_0x0262;
    L_0x01bf:
        r4 = r25;
        r14 = r21;
        r7 = android.content.ContentProviderOperation.newInsert(r7);	 Catch:{ Exception -> 0x026f }
        r10 = r1.systemAccount;	 Catch:{ Exception -> 0x026f }
        r10 = r10.type;	 Catch:{ Exception -> 0x026f }
        r7 = r7.withValue(r12, r10);	 Catch:{ Exception -> 0x026f }
        r10 = r1.systemAccount;	 Catch:{ Exception -> 0x026f }
        r10 = r10.name;	 Catch:{ Exception -> 0x026f }
        r7 = r7.withValue(r9, r10);	 Catch:{ Exception -> 0x026f }
        r9 = "raw_contact_is_read_only";
        r10 = 1;
        r10 = java.lang.Integer.valueOf(r10);	 Catch:{ Exception -> 0x026f }
        r7 = r7.withValue(r9, r10);	 Catch:{ Exception -> 0x026f }
        r9 = "aggregation_mode";
        r10 = 3;
        r10 = java.lang.Integer.valueOf(r10);	 Catch:{ Exception -> 0x026f }
        r7 = r7.withValue(r9, r10);	 Catch:{ Exception -> 0x026f }
        r7 = r7.build();	 Catch:{ Exception -> 0x026f }
        r14.add(r7);	 Catch:{ Exception -> 0x026f }
        r7 = android.provider.ContactsContract.Data.CONTENT_URI;	 Catch:{ Exception -> 0x026f }
        r7 = android.content.ContentProviderOperation.newInsert(r7);	 Catch:{ Exception -> 0x026f }
        r7 = r7.withValueBackReference(r11, r8);	 Catch:{ Exception -> 0x026f }
        r9 = "vnd.android.cursor.item/name";
        r10 = r19;
        r7 = r7.withValue(r10, r9);	 Catch:{ Exception -> 0x026f }
        r9 = "data2";
        r2 = r7.withValue(r9, r2);	 Catch:{ Exception -> 0x026f }
        r7 = "data3";
        r2 = r2.withValue(r7, r4);	 Catch:{ Exception -> 0x026f }
        r2 = r2.build();	 Catch:{ Exception -> 0x026f }
        r14.add(r2);	 Catch:{ Exception -> 0x026f }
        r2 = android.provider.ContactsContract.Data.CONTENT_URI;	 Catch:{ Exception -> 0x026f }
        r2 = android.content.ContentProviderOperation.newInsert(r2);	 Catch:{ Exception -> 0x026f }
        r2 = r2.withValueBackReference(r11, r8);	 Catch:{ Exception -> 0x026f }
        r4 = "vnd.android.cursor.item/phone_v2";
        r2 = r2.withValue(r10, r4);	 Catch:{ Exception -> 0x026f }
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x026f }
        r4.<init>();	 Catch:{ Exception -> 0x026f }
        r7 = "+99084";
        r4.append(r7);	 Catch:{ Exception -> 0x026f }
        r4.append(r0);	 Catch:{ Exception -> 0x026f }
        r0 = r4.toString();	 Catch:{ Exception -> 0x026f }
        r0 = r2.withValue(r13, r0);	 Catch:{ Exception -> 0x026f }
        r0 = r0.build();	 Catch:{ Exception -> 0x026f }
        r14.add(r0);	 Catch:{ Exception -> 0x026f }
        r0 = android.provider.ContactsContract.Data.CONTENT_URI;	 Catch:{ Exception -> 0x026f }
        r0 = android.content.ContentProviderOperation.newInsert(r0);	 Catch:{ Exception -> 0x026f }
        r0 = r0.withValueBackReference(r11, r8);	 Catch:{ Exception -> 0x026f }
        r0 = r0.withValue(r10, r5);	 Catch:{ Exception -> 0x026f }
        r2 = java.lang.Integer.valueOf(r6);	 Catch:{ Exception -> 0x026f }
        r0 = r0.withValue(r13, r2);	 Catch:{ Exception -> 0x026f }
        r0 = r0.build();	 Catch:{ Exception -> 0x026f }
        r14.add(r0);	 Catch:{ Exception -> 0x026f }
    L_0x0262:
        if (r3 == 0) goto L_0x0267;
    L_0x0264:
        r3.close();	 Catch:{ Exception -> 0x026f }
    L_0x0267:
        r0 = "com.android.contacts";
        r2 = r20;
        r2.applyBatch(r0, r14);	 Catch:{ Exception -> 0x026f }
        goto L_0x0273;
    L_0x026f:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x0273:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ContactsController.createOrUpdateConnectionServiceContact(int, java.lang.String, java.lang.String):void");
    }

    public void deleteConnectionServiceContact() {
        String str = "";
        if (hasContactsPermission()) {
            try {
                ContentResolver contentResolver = ApplicationLoader.applicationContext.getContentResolver();
                ContentResolver contentResolver2 = contentResolver;
                Cursor query = contentResolver2.query(Groups.CONTENT_URI, new String[]{"_id"}, "title=? AND account_type=? AND account_name=?", new String[]{"TelegramConnectionService", this.systemAccount.type, this.systemAccount.name}, null);
                if (query == null || !query.moveToFirst()) {
                    if (query != null) {
                        query.close();
                    }
                    return;
                }
                int i = query.getInt(0);
                query.close();
                Uri uri = Data.CONTENT_URI;
                String[] strArr = new String[]{"raw_contact_id"};
                String[] strArr2 = new String[2];
                strArr2[0] = "vnd.android.cursor.item/group_membership";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(i);
                stringBuilder.append(str);
                strArr2[1] = stringBuilder.toString();
                query = contentResolver.query(uri, strArr, "mimetype=? AND data1=?", strArr2, null);
                if (query == null || !query.moveToFirst()) {
                    if (query != null) {
                        query.close();
                    }
                    return;
                }
                i = query.getInt(0);
                query.close();
                strArr = new String[1];
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(i);
                stringBuilder2.append(str);
                strArr[0] = stringBuilder2.toString();
                contentResolver.delete(RawContacts.CONTENT_URI, "_id=?", strArr);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static String formatName(String str, String str2) {
        if (str != null) {
            str = str.trim();
        }
        if (str2 != null) {
            str2 = str2.trim();
        }
        int i = 0;
        int length = str != null ? str.length() : 0;
        if (str2 != null) {
            i = str2.length();
        }
        StringBuilder stringBuilder = new StringBuilder((length + i) + 1);
        String str3 = " ";
        if (LocaleController.nameDisplayOrder == 1) {
            if (str != null && str.length() > 0) {
                stringBuilder.append(str);
                if (str2 != null && str2.length() > 0) {
                    stringBuilder.append(str3);
                    stringBuilder.append(str2);
                }
            } else if (str2 != null && str2.length() > 0) {
                stringBuilder.append(str2);
            }
        } else if (str2 != null && str2.length() > 0) {
            stringBuilder.append(str2);
            if (str != null && str.length() > 0) {
                stringBuilder.append(str3);
                stringBuilder.append(str);
            }
        } else if (str != null && str.length() > 0) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }
}
