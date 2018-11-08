package org.telegram.messenger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.SparseArray;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.PhoneFormat.C0216PhoneFormat;
import org.telegram.messenger.support.SparseLongArray;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.PrivacyRule;
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
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyPhoneCall;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyStatusTimestamp;
import org.telegram.tgnet.TLRPC.TL_popularContact;
import org.telegram.tgnet.TLRPC.TL_user;
import org.telegram.tgnet.TLRPC.TL_userStatusLastMonth;
import org.telegram.tgnet.TLRPC.TL_userStatusLastWeek;
import org.telegram.tgnet.TLRPC.TL_userStatusRecently;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.Vector;
import org.telegram.tgnet.TLRPC.contacts_Contacts;

public class ContactsController {
    private static volatile ContactsController[] Instance = new ContactsController[3];
    private ArrayList<PrivacyRule> callPrivacyRules;
    private int completedRequestsCount;
    public ArrayList<TL_contact> contacts = new ArrayList();
    public HashMap<String, Contact> contactsBook = new HashMap();
    private boolean contactsBookLoaded;
    public HashMap<String, Contact> contactsBookSPhones = new HashMap();
    public HashMap<String, TL_contact> contactsByPhone = new HashMap();
    public HashMap<String, TL_contact> contactsByShortPhone = new HashMap();
    public ConcurrentHashMap<Integer, TL_contact> contactsDict = new ConcurrentHashMap(20, 1.0f, 2);
    public boolean contactsLoaded;
    private boolean contactsSyncInProgress;
    private int currentAccount;
    private ArrayList<Integer> delayedContactsUpdate = new ArrayList();
    private int deleteAccountTTL;
    private ArrayList<PrivacyRule> groupPrivacyRules;
    private boolean ignoreChanges;
    private String inviteLink;
    private String lastContactsVersions = TtmlNode.ANONYMOUS_REGION_ID;
    private final Object loadContactsSync = new Object();
    private int loadingCallsInfo;
    private boolean loadingContacts;
    private int loadingDeleteInfo;
    private int loadingGroupInfo;
    private int loadingLastSeenInfo;
    private boolean migratingContacts;
    private final Object observerLock = new Object();
    public ArrayList<Contact> phoneBookContacts = new ArrayList();
    public ArrayList<String> phoneBookSectionsArray = new ArrayList();
    public HashMap<String, ArrayList<Object>> phoneBookSectionsDict = new HashMap();
    private ArrayList<PrivacyRule> privacyRules;
    private String[] projectionNames = new String[]{"lookup", "data2", "data3", "data5"};
    private String[] projectionPhones = new String[]{"lookup", "data1", "data2", "data3", "display_name", "account_type"};
    private HashMap<String, String> sectionsToReplace = new HashMap();
    public ArrayList<String> sortedUsersMutualSectionsArray = new ArrayList();
    public ArrayList<String> sortedUsersSectionsArray = new ArrayList();
    private Account systemAccount;
    private boolean updatingInviteLink;
    public HashMap<String, ArrayList<TL_contact>> usersMutualSectionsDict = new HashMap();
    public HashMap<String, ArrayList<TL_contact>> usersSectionsDict = new HashMap();

    /* renamed from: org.telegram.messenger.ContactsController$12 */
    class C024312 implements Comparator<TL_contact> {
        C024312() {
        }

        public int compare(TL_contact tl_contact, TL_contact tl_contact2) {
            if (tl_contact.user_id > tl_contact2.user_id) {
                return 1;
            }
            if (tl_contact.user_id < tl_contact2.user_id) {
                return -1;
            }
            return 0;
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$16 */
    class C025816 implements Comparator<Contact> {
        C025816() {
        }

        public int compare(Contact contact, Contact contact2) {
            String toComapre1 = contact.first_name;
            if (toComapre1.length() == 0) {
                toComapre1 = contact.last_name;
            }
            String toComapre2 = contact2.first_name;
            if (toComapre2.length() == 0) {
                toComapre2 = contact2.last_name;
            }
            return toComapre1.compareTo(toComapre2);
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$17 */
    class C025917 implements Comparator<TL_contact> {
        C025917() {
        }

        public int compare(TL_contact tl_contact, TL_contact tl_contact2) {
            return UserObject.getFirstName(MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(tl_contact.user_id))).compareTo(UserObject.getFirstName(MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(tl_contact2.user_id))));
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$18 */
    class C026018 implements Comparator<String> {
        C026018() {
        }

        public int compare(String s, String s2) {
            char cv1 = s.charAt(0);
            char cv2 = s2.charAt(0);
            if (cv1 == '#') {
                return 1;
            }
            if (cv2 == '#') {
                return -1;
            }
            return s.compareTo(s2);
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$1 */
    class C02621 implements Runnable {
        C02621() {
        }

        public void run() {
            try {
                if (ContactsController.this.hasContactsPermission()) {
                    ApplicationLoader.applicationContext.getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, new MyContentObserver());
                }
            } catch (Throwable th) {
            }
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$21 */
    class C026421 implements Runnable {
        C026421() {
        }

        public void run() {
            ContactsController.this.loadContacts(false, 0);
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$24 */
    class C026924 implements RequestDelegate {
        C026924() {
        }

        public void run(TLObject response, TL_error error) {
            if (error == null) {
                final TL_contacts_importedContacts res = (TL_contacts_importedContacts) response;
                MessagesStorage.getInstance(ContactsController.this.currentAccount).putUsersAndChats(res.users, null, true, true);
                for (int a = 0; a < res.users.size(); a++) {
                    final User u = (User) res.users.get(a);
                    Utilities.phoneBookQueue.postRunnable(new Runnable() {
                        public void run() {
                            ContactsController.this.addContactToPhoneBook(u, true);
                        }
                    });
                    TL_contact newContact = new TL_contact();
                    newContact.user_id = u.f228id;
                    ArrayList<TL_contact> arrayList = new ArrayList();
                    arrayList.add(newContact);
                    MessagesStorage.getInstance(ContactsController.this.currentAccount).putContacts(arrayList, false);
                    if (!TextUtils.isEmpty(u.phone)) {
                        CharSequence name = ContactsController.formatName(u.first_name, u.last_name);
                        MessagesStorage.getInstance(ContactsController.this.currentAccount).applyPhoneBookUpdates(u.phone, TtmlNode.ANONYMOUS_REGION_ID);
                        Contact contact = (Contact) ContactsController.this.contactsBookSPhones.get(u.phone);
                        if (contact != null) {
                            int index = contact.shortPhones.indexOf(u.phone);
                            if (index != -1) {
                                contact.phoneDeleted.set(index, Integer.valueOf(0));
                            }
                        }
                    }
                }
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        Iterator it = res.users.iterator();
                        while (it.hasNext()) {
                            User u = (User) it.next();
                            MessagesController.getInstance(ContactsController.this.currentAccount).putUser(u, false);
                            if (ContactsController.this.contactsDict.get(Integer.valueOf(u.f228id)) == null) {
                                TL_contact newContact = new TL_contact();
                                newContact.user_id = u.f228id;
                                ContactsController.this.contacts.add(newContact);
                                ContactsController.this.contactsDict.put(Integer.valueOf(newContact.user_id), newContact);
                            }
                        }
                        ContactsController.this.buildContactsSectionsArrays(true);
                        NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                    }
                });
            }
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$27 */
    class C027627 implements RequestDelegate {
        C027627() {
        }

        public void run(final TLObject response, final TL_error error) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    if (error == null) {
                        ContactsController.this.deleteAccountTTL = response.days;
                        ContactsController.this.loadingDeleteInfo = 2;
                    } else {
                        ContactsController.this.loadingDeleteInfo = 0;
                    }
                    NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
                }
            });
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$28 */
    class C027828 implements RequestDelegate {
        C027828() {
        }

        public void run(final TLObject response, final TL_error error) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    if (error == null) {
                        TL_account_privacyRules rules = response;
                        MessagesController.getInstance(ContactsController.this.currentAccount).putUsers(rules.users, false);
                        ContactsController.this.privacyRules = rules.rules;
                        ContactsController.this.loadingLastSeenInfo = 2;
                    } else {
                        ContactsController.this.loadingLastSeenInfo = 0;
                    }
                    NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
                }
            });
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$29 */
    class C028029 implements RequestDelegate {
        C028029() {
        }

        public void run(final TLObject response, final TL_error error) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    if (error == null) {
                        TL_account_privacyRules rules = response;
                        MessagesController.getInstance(ContactsController.this.currentAccount).putUsers(rules.users, false);
                        ContactsController.this.callPrivacyRules = rules.rules;
                        ContactsController.this.loadingCallsInfo = 2;
                    } else {
                        ContactsController.this.loadingCallsInfo = 0;
                    }
                    NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
                }
            });
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$2 */
    class C02812 implements Runnable {
        C02812() {
        }

        public void run() {
            ContactsController.this.migratingContacts = false;
            ContactsController.this.completedRequestsCount = 0;
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$30 */
    class C028430 implements RequestDelegate {
        C028430() {
        }

        public void run(final TLObject response, final TL_error error) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    if (error == null) {
                        TL_account_privacyRules rules = response;
                        MessagesController.getInstance(ContactsController.this.currentAccount).putUsers(rules.users, false);
                        ContactsController.this.groupPrivacyRules = rules.rules;
                        ContactsController.this.loadingGroupInfo = 2;
                    } else {
                        ContactsController.this.loadingGroupInfo = 0;
                    }
                    NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
                }
            });
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$3 */
    class C02853 implements RequestDelegate {
        C02853() {
        }

        public void run(TLObject response, TL_error error) {
            if (response != null) {
                final TL_help_inviteText res = (TL_help_inviteText) response;
                if (res.message.length() != 0) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            ContactsController.this.updatingInviteLink = false;
                            Editor editor = MessagesController.getMainSettings(ContactsController.this.currentAccount).edit();
                            editor.putString("invitelink", ContactsController.this.inviteLink = res.message);
                            editor.putInt("invitelinktime", (int) (System.currentTimeMillis() / 1000));
                            editor.commit();
                        }
                    });
                }
            }
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$4 */
    class C02864 implements Runnable {
        C02864() {
        }

        public void run() {
            if (ContactsController.this.checkContactsInternal()) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.m11d("detected contacts change");
                }
                ContactsController.this.performSyncPhoneBook(ContactsController.this.getContactsCopy(ContactsController.this.contactsBook), true, false, true, false, true, false);
            }
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$5 */
    class C02875 implements Runnable {
        C02875() {
        }

        public void run() {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.m11d("force import contacts");
            }
            ContactsController.this.performSyncPhoneBook(new HashMap(), true, true, true, true, false, false);
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$8 */
    class C02918 implements RequestDelegate {
        C02918() {
        }

        public void run(TLObject response, TL_error error) {
        }
    }

    /* renamed from: org.telegram.messenger.ContactsController$9 */
    class C02929 implements Runnable {
        C02929() {
        }

        public void run() {
            if (!ContactsController.this.contacts.isEmpty() || ContactsController.this.contactsLoaded) {
                synchronized (ContactsController.this.loadContactsSync) {
                    ContactsController.this.loadingContacts = false;
                }
                return;
            }
            ContactsController.this.loadContacts(true, 0);
        }
    }

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

        public static String getLetter(String first_name, String last_name) {
            if (!TextUtils.isEmpty(first_name)) {
                return first_name.substring(0, 1);
            }
            if (TextUtils.isEmpty(last_name)) {
                return "#";
            }
            return last_name.substring(0, 1);
        }
    }

    private class MyContentObserver extends ContentObserver {
        private Runnable checkRunnable = new C02931();

        /* renamed from: org.telegram.messenger.ContactsController$MyContentObserver$1 */
        class C02931 implements Runnable {
            C02931() {
            }

            public void run() {
                for (int a = 0; a < 3; a++) {
                    if (UserConfig.getInstance(a).isClientActivated()) {
                        ConnectionsManager.getInstance(a).resumeNetworkMaybe();
                        ContactsController.getInstance(a).checkContacts();
                    }
                }
            }
        }

        public MyContentObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (ContactsController.this.observerLock) {
                if (ContactsController.this.ignoreChanges) {
                    return;
                }
                Utilities.globalQueue.cancelRunnable(this.checkRunnable);
                Utilities.globalQueue.postRunnable(this.checkRunnable, 500);
            }
        }

        public boolean deliverSelfNotifications() {
            return false;
        }
    }

    public static ContactsController getInstance(int num) {
        Throwable th;
        ContactsController localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (ContactsController.class) {
                try {
                    localInstance = Instance[num];
                    if (localInstance == null) {
                        ContactsController[] contactsControllerArr = Instance;
                        ContactsController localInstance2 = new ContactsController(num);
                        try {
                            contactsControllerArr[num] = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th2) {
                            th = th2;
                            localInstance = localInstance2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
        return localInstance;
    }

    public ContactsController(int instance) {
        this.currentAccount = instance;
        if (MessagesController.getMainSettings(this.currentAccount).getBoolean("needGetStatuses", false)) {
            reloadContactsStatuses();
        }
        this.sectionsToReplace.put("\u00c0", "A");
        this.sectionsToReplace.put("\u00c1", "A");
        this.sectionsToReplace.put("\u00c4", "A");
        this.sectionsToReplace.put("\u00d9", "U");
        this.sectionsToReplace.put("\u00da", "U");
        this.sectionsToReplace.put("\u00dc", "U");
        this.sectionsToReplace.put("\u00cc", "I");
        this.sectionsToReplace.put("\u00cd", "I");
        this.sectionsToReplace.put("\u00cf", "I");
        this.sectionsToReplace.put("\u00c8", "E");
        this.sectionsToReplace.put("\u00c9", "E");
        this.sectionsToReplace.put("\u00ca", "E");
        this.sectionsToReplace.put("\u00cb", "E");
        this.sectionsToReplace.put("\u00d2", "O");
        this.sectionsToReplace.put("\u00d3", "O");
        this.sectionsToReplace.put("\u00d6", "O");
        this.sectionsToReplace.put("\u00c7", "C");
        this.sectionsToReplace.put("\u00d1", "N");
        this.sectionsToReplace.put("\u0178", "Y");
        this.sectionsToReplace.put("\u00dd", "Y");
        this.sectionsToReplace.put("\u0162", "Y");
        if (instance == 0) {
            Utilities.globalQueue.postRunnable(new C02621());
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
        this.lastContactsVersions = TtmlNode.ANONYMOUS_REGION_ID;
        this.loadingDeleteInfo = 0;
        this.deleteAccountTTL = 0;
        this.loadingLastSeenInfo = 0;
        this.loadingGroupInfo = 0;
        this.loadingCallsInfo = 0;
        Utilities.globalQueue.postRunnable(new C02812());
        this.privacyRules = null;
    }

    public void checkInviteText() {
        SharedPreferences preferences = MessagesController.getMainSettings(this.currentAccount);
        this.inviteLink = preferences.getString("invitelink", null);
        int time = preferences.getInt("invitelinktime", 0);
        if (!this.updatingInviteLink) {
            if (this.inviteLink == null || Math.abs((System.currentTimeMillis() / 1000) - ((long) time)) >= 86400) {
                this.updatingInviteLink = true;
                ConnectionsManager.getInstance(this.currentAccount).sendRequest(new TL_help_getInviteText(), new C02853(), 2);
            }
        }
    }

    public String getInviteText(int contacts) {
        String link = this.inviteLink == null ? "https://telegram.org/dl" : this.inviteLink;
        if (contacts <= 1) {
            return LocaleController.formatString("InviteText2", C0541R.string.InviteText2, link);
        }
        try {
            return String.format(LocaleController.getPluralString("InviteTextNum", contacts), new Object[]{Integer.valueOf(contacts), link});
        } catch (Exception e) {
            return LocaleController.formatString("InviteText2", C0541R.string.InviteText2, link);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0053 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x004c A:{SYNTHETIC, Splitter: B:17:0x004c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void checkAppAccount() {
        AccountManager am = AccountManager.get(ApplicationLoader.applicationContext);
        try {
            Account[] accounts = am.getAccountsByType(BuildConfig.APPLICATION_ID);
            this.systemAccount = null;
            for (int a = 0; a < accounts.length; a++) {
                Account acc = accounts[a];
                boolean found = false;
                int b = 0;
                while (b < 3) {
                    User user = UserConfig.getInstance(b).getCurrentUser();
                    if (user == null || !acc.name.equals(TtmlNode.ANONYMOUS_REGION_ID + user.f228id)) {
                        b++;
                    } else {
                        if (b == this.currentAccount) {
                            this.systemAccount = acc;
                        }
                        found = true;
                        if (found) {
                            try {
                                am.removeAccount(accounts[a], null, null);
                            } catch (Exception e) {
                            }
                        }
                    }
                }
                if (found) {
                }
            }
        } catch (Throwable th) {
        }
        if (UserConfig.getInstance(this.currentAccount).isClientActivated()) {
            readContacts();
            if (this.systemAccount == null) {
                try {
                    this.systemAccount = new Account(TtmlNode.ANONYMOUS_REGION_ID + UserConfig.getInstance(this.currentAccount).getClientUserId(), BuildConfig.APPLICATION_ID);
                    am.addAccountExplicitly(this.systemAccount, TtmlNode.ANONYMOUS_REGION_ID, null);
                } catch (Exception e2) {
                }
            }
        }
    }

    public void deleteUnknownAppAccounts() {
        try {
            this.systemAccount = null;
            AccountManager am = AccountManager.get(ApplicationLoader.applicationContext);
            Account[] accounts = am.getAccountsByType(BuildConfig.APPLICATION_ID);
            for (int a = 0; a < accounts.length; a++) {
                Account acc = accounts[a];
                boolean found = false;
                for (int b = 0; b < 3; b++) {
                    User user = UserConfig.getInstance(b).getCurrentUser();
                    if (user != null && acc.name.equals(TtmlNode.ANONYMOUS_REGION_ID + user.f228id)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    try {
                        am.removeAccount(accounts[a], null, null);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e2) {
            ThrowableExtension.printStackTrace(e2);
        }
    }

    public void checkContacts() {
        Utilities.globalQueue.postRunnable(new C02864());
    }

    public void forceImportContacts() {
        Utilities.globalQueue.postRunnable(new C02875());
    }

    public void syncPhoneBookByAlert(HashMap<String, Contact> contacts, boolean first, boolean schedule, boolean cancel) {
        final HashMap<String, Contact> hashMap = contacts;
        final boolean z = first;
        final boolean z2 = schedule;
        final boolean z3 = cancel;
        Utilities.globalQueue.postRunnable(new Runnable() {
            public void run() {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.m11d("sync contacts by alert");
                }
                ContactsController.this.performSyncPhoneBook(hashMap, true, z, z2, false, false, z3);
            }
        });
    }

    public void deleteAllContacts(final Runnable runnable) {
        resetImportedContacts();
        TL_contacts_deleteContacts req = new TL_contacts_deleteContacts();
        int size = this.contacts.size();
        for (int a = 0; a < size; a++) {
            req.f161id.add(MessagesController.getInstance(this.currentAccount).getInputUser(((TL_contact) this.contacts.get(a)).user_id));
        }
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new RequestDelegate() {

            /* renamed from: org.telegram.messenger.ContactsController$7$1 */
            class C02891 implements Runnable {
                C02891() {
                }

                public void run() {
                    AccountManager am = AccountManager.get(ApplicationLoader.applicationContext);
                    try {
                        Account[] accounts = am.getAccountsByType(BuildConfig.APPLICATION_ID);
                        ContactsController.this.systemAccount = null;
                        for (Account acc : accounts) {
                            for (int b = 0; b < 3; b++) {
                                User user = UserConfig.getInstance(b).getCurrentUser();
                                if (user != null && acc.name.equals(TtmlNode.ANONYMOUS_REGION_ID + user.f228id)) {
                                    am.removeAccount(acc, null, null);
                                    break;
                                }
                            }
                        }
                    } catch (Throwable th) {
                    }
                    try {
                        ContactsController.this.systemAccount = new Account(TtmlNode.ANONYMOUS_REGION_ID + UserConfig.getInstance(ContactsController.this.currentAccount).getClientUserId(), BuildConfig.APPLICATION_ID);
                        am.addAccountExplicitly(ContactsController.this.systemAccount, TtmlNode.ANONYMOUS_REGION_ID, null);
                    } catch (Exception e) {
                    }
                    MessagesStorage.getInstance(ContactsController.this.currentAccount).putCachedPhoneBook(new HashMap(), false, true);
                    MessagesStorage.getInstance(ContactsController.this.currentAccount).putContacts(new ArrayList(), true);
                    ContactsController.this.phoneBookContacts.clear();
                    ContactsController.this.contacts.clear();
                    ContactsController.this.contactsDict.clear();
                    ContactsController.this.usersSectionsDict.clear();
                    ContactsController.this.usersMutualSectionsDict.clear();
                    ContactsController.this.sortedUsersSectionsArray.clear();
                    ContactsController.this.phoneBookSectionsDict.clear();
                    ContactsController.this.phoneBookSectionsArray.clear();
                    ContactsController.this.delayedContactsUpdate.clear();
                    ContactsController.this.sortedUsersMutualSectionsArray.clear();
                    ContactsController.this.contactsByPhone.clear();
                    ContactsController.this.contactsByShortPhone.clear();
                    NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                    ContactsController.this.loadContacts(false, 0);
                    runnable.run();
                }
            }

            public void run(TLObject response, TL_error error) {
                if (response instanceof TL_boolTrue) {
                    ContactsController.this.contactsBookSPhones.clear();
                    ContactsController.this.contactsBook.clear();
                    ContactsController.this.completedRequestsCount = 0;
                    ContactsController.this.migratingContacts = false;
                    ContactsController.this.contactsSyncInProgress = false;
                    ContactsController.this.contactsLoaded = false;
                    ContactsController.this.loadingContacts = false;
                    ContactsController.this.contactsBookLoaded = false;
                    ContactsController.this.lastContactsVersions = TtmlNode.ANONYMOUS_REGION_ID;
                    AndroidUtilities.runOnUIThread(new C02891());
                }
            }
        });
    }

    public void resetImportedContacts() {
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(new TL_contacts_resetSaved(), new C02918());
    }

    private boolean checkContactsInternal() {
        boolean reload = false;
        try {
            boolean z;
            if (hasContactsPermission()) {
                ContentResolver cr = ApplicationLoader.applicationContext.getContentResolver();
                Cursor pCur = null;
                try {
                    pCur = cr.query(RawContacts.CONTENT_URI, new String[]{"version"}, null, null, null);
                    if (pCur != null) {
                        StringBuilder currentVersion = new StringBuilder();
                        while (pCur.moveToNext()) {
                            currentVersion.append(pCur.getString(pCur.getColumnIndex("version")));
                        }
                        String newContactsVersion = currentVersion.toString();
                        if (!(this.lastContactsVersions.length() == 0 || this.lastContactsVersions.equals(newContactsVersion))) {
                            reload = true;
                        }
                        this.lastContactsVersions = newContactsVersion;
                    }
                    if (pCur != null) {
                        pCur.close();
                    }
                } catch (Throwable e) {
                    FileLog.m14e(e);
                    if (pCur != null) {
                        pCur.close();
                    }
                } catch (Throwable th) {
                    if (pCur != null) {
                        pCur.close();
                    }
                }
                z = reload;
                return reload;
            }
            z = false;
            return false;
        } catch (Throwable e2) {
            FileLog.m14e(e2);
        }
    }

    public void readContacts() {
        synchronized (this.loadContactsSync) {
            if (this.loadingContacts) {
                return;
            }
            this.loadingContacts = true;
            Utilities.stageQueue.postRunnable(new C02929());
        }
    }

    private boolean isNotValidNameString(String src) {
        if (TextUtils.isEmpty(src)) {
            return true;
        }
        int count = 0;
        int len = src.length();
        for (int a = 0; a < len; a++) {
            char c = src.charAt(a);
            if (c >= '0' && c <= '9') {
                count++;
            }
        }
        if (count <= 3) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:147:0x03bb A:{Catch:{ Throwable -> 0x0117, all -> 0x01ea }} */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x0383 A:{Catch:{ Throwable -> 0x0117, all -> 0x01ea }} */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x038d A:{Catch:{ Throwable -> 0x0117, all -> 0x01ea }} */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x03c6 A:{Catch:{ Throwable -> 0x0117, all -> 0x01ea }} */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x03b5 A:{Catch:{ Throwable -> 0x0117, all -> 0x01ea }} */
    /* JADX WARNING: Missing block: B:131:0x0361, code:
            if (r18.contains(r9.first_name) == false) goto L_0x0363;
     */
    /* JADX WARNING: Missing block: B:137:0x037f, code:
            if (r18.contains(r9.last_name) != false) goto L_0x0381;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private HashMap<String, Contact> readContactsFromPhoneBook() {
        if (!UserConfig.getInstance(this.currentAccount).syncContacts) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.m11d("contacts sync disabled");
            }
            return new HashMap();
        } else if (hasContactsPermission()) {
            Cursor pCur = null;
            HashMap<String, Contact> contactsMap = null;
            try {
                String lookup_key;
                Contact contact;
                StringBuilder escaper = new StringBuilder();
                ContentResolver cr = ApplicationLoader.applicationContext.getContentResolver();
                HashMap<String, Contact> shortContacts = new HashMap();
                ArrayList<String> idsArr = new ArrayList();
                pCur = cr.query(Phone.CONTENT_URI, this.projectionPhones, null, null, null);
                if (pCur != null) {
                    int count = pCur.getCount();
                    if (count > 0) {
                        int lastContactId;
                        if (null == null) {
                            lastContactId = 1;
                            contactsMap = new HashMap(count);
                        } else {
                            lastContactId = 1;
                        }
                        while (pCur.moveToNext()) {
                            String number = pCur.getString(1);
                            String accountType = pCur.getString(5);
                            if (accountType == null) {
                                accountType = TtmlNode.ANONYMOUS_REGION_ID;
                            }
                            boolean isGoodAccountType = accountType.indexOf(".sim") != 0;
                            if (!TextUtils.isEmpty(number)) {
                                number = C0216PhoneFormat.stripExceptNumbers(number, true);
                                if (!TextUtils.isEmpty(number)) {
                                    String shortNumber = number;
                                    if (number.startsWith("+")) {
                                        shortNumber = number.substring(1);
                                    }
                                    lookup_key = pCur.getString(0);
                                    escaper.setLength(0);
                                    DatabaseUtils.appendEscapedSQLString(escaper, lookup_key);
                                    String key = escaper.toString();
                                    Contact existingContact = (Contact) shortContacts.get(shortNumber);
                                    if (existingContact == null) {
                                        int lastContactId2;
                                        if (!idsArr.contains(key)) {
                                            idsArr.add(key);
                                        }
                                        int type = pCur.getInt(2);
                                        contact = (Contact) contactsMap.get(lookup_key);
                                        if (contact == null) {
                                            contact = new Contact();
                                            String displayName = pCur.getString(4);
                                            if (displayName == null) {
                                                displayName = TtmlNode.ANONYMOUS_REGION_ID;
                                            } else {
                                                displayName = displayName.trim();
                                            }
                                            if (isNotValidNameString(displayName)) {
                                                contact.first_name = displayName;
                                                contact.last_name = TtmlNode.ANONYMOUS_REGION_ID;
                                            } else {
                                                int spaceIndex = displayName.lastIndexOf(32);
                                                if (spaceIndex != -1) {
                                                    contact.first_name = displayName.substring(0, spaceIndex).trim();
                                                    contact.last_name = displayName.substring(spaceIndex + 1, displayName.length()).trim();
                                                } else {
                                                    contact.first_name = displayName;
                                                    contact.last_name = TtmlNode.ANONYMOUS_REGION_ID;
                                                }
                                            }
                                            contact.provider = accountType;
                                            contact.isGoodProvider = isGoodAccountType;
                                            contact.key = lookup_key;
                                            lastContactId2 = lastContactId + 1;
                                            contact.contact_id = lastContactId;
                                            contactsMap.put(lookup_key, contact);
                                        } else {
                                            lastContactId2 = lastContactId;
                                        }
                                        contact.shortPhones.add(shortNumber);
                                        contact.phones.add(number);
                                        contact.phoneDeleted.add(Integer.valueOf(0));
                                        if (type == 0) {
                                            String custom = pCur.getString(3);
                                            ArrayList arrayList = contact.phoneTypes;
                                            if (custom == null) {
                                                custom = LocaleController.getString("PhoneMobile", C0541R.string.PhoneMobile);
                                            }
                                            arrayList.add(custom);
                                        } else if (type == 1) {
                                            contact.phoneTypes.add(LocaleController.getString("PhoneHome", C0541R.string.PhoneHome));
                                        } else if (type == 2) {
                                            contact.phoneTypes.add(LocaleController.getString("PhoneMobile", C0541R.string.PhoneMobile));
                                        } else if (type == 3) {
                                            contact.phoneTypes.add(LocaleController.getString("PhoneWork", C0541R.string.PhoneWork));
                                        } else if (type == 12) {
                                            contact.phoneTypes.add(LocaleController.getString("PhoneMain", C0541R.string.PhoneMain));
                                        } else {
                                            contact.phoneTypes.add(LocaleController.getString("PhoneOther", C0541R.string.PhoneOther));
                                        }
                                        shortContacts.put(shortNumber, contact);
                                        lastContactId = lastContactId2;
                                    } else if (!(existingContact.isGoodProvider || accountType.equals(existingContact.provider))) {
                                        escaper.setLength(0);
                                        DatabaseUtils.appendEscapedSQLString(escaper, existingContact.key);
                                        idsArr.remove(escaper.toString());
                                        idsArr.add(key);
                                        existingContact.key = lookup_key;
                                        existingContact.isGoodProvider = isGoodAccountType;
                                        existingContact.provider = accountType;
                                    }
                                }
                            }
                        }
                    }
                    try {
                        pCur.close();
                    } catch (Exception e) {
                    }
                    pCur = null;
                }
                pCur = cr.query(Data.CONTENT_URI, this.projectionNames, "lookup IN (" + TextUtils.join(",", idsArr) + ") AND " + "mimetype" + " = '" + "vnd.android.cursor.item/name" + "'", null, null);
                if (pCur != null) {
                    while (pCur.moveToNext()) {
                        lookup_key = pCur.getString(0);
                        String fname = pCur.getString(1);
                        String sname = pCur.getString(2);
                        String mname = pCur.getString(3);
                        contact = (Contact) contactsMap.get(lookup_key);
                        if (!(contact == null || contact.namesFilled)) {
                            if (contact.isGoodProvider) {
                                if (fname != null) {
                                    contact.first_name = fname;
                                } else {
                                    contact.first_name = TtmlNode.ANONYMOUS_REGION_ID;
                                }
                                if (sname != null) {
                                    contact.last_name = sname;
                                } else {
                                    contact.last_name = TtmlNode.ANONYMOUS_REGION_ID;
                                }
                                if (!TextUtils.isEmpty(mname)) {
                                    if (TextUtils.isEmpty(contact.first_name)) {
                                        contact.first_name = mname;
                                    } else {
                                        contact.first_name += " " + mname;
                                    }
                                }
                            } else {
                                if (!isNotValidNameString(fname)) {
                                    if (!contact.first_name.contains(fname)) {
                                    }
                                    if (fname == null) {
                                        contact.first_name = fname;
                                    } else {
                                        contact.first_name = TtmlNode.ANONYMOUS_REGION_ID;
                                    }
                                    if (!TextUtils.isEmpty(mname)) {
                                        if (TextUtils.isEmpty(contact.first_name)) {
                                            contact.first_name = mname;
                                        } else {
                                            contact.first_name += " " + mname;
                                        }
                                    }
                                    if (sname == null) {
                                        contact.last_name = sname;
                                    } else {
                                        contact.last_name = TtmlNode.ANONYMOUS_REGION_ID;
                                    }
                                }
                                if (!isNotValidNameString(sname)) {
                                    if (!contact.last_name.contains(sname)) {
                                    }
                                    if (fname == null) {
                                    }
                                    if (TextUtils.isEmpty(mname)) {
                                    }
                                    if (sname == null) {
                                    }
                                }
                            }
                            contact.namesFilled = true;
                        }
                    }
                    try {
                        pCur.close();
                    } catch (Exception e2) {
                    }
                    pCur = null;
                }
                if (pCur != null) {
                    try {
                        pCur.close();
                    } catch (Throwable e3) {
                        FileLog.m14e(e3);
                    }
                }
            } catch (Throwable th) {
                if (pCur != null) {
                    try {
                        pCur.close();
                    } catch (Throwable e32) {
                        FileLog.m14e(e32);
                    }
                }
            }
            return contactsMap == null ? new HashMap() : contactsMap;
        } else {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.m11d("app has no contacts permissions");
            }
            return new HashMap();
        }
    }

    public HashMap<String, Contact> getContactsCopy(HashMap<String, Contact> original) {
        HashMap<String, Contact> ret = new HashMap();
        for (Entry<String, Contact> entry : original.entrySet()) {
            Contact copyContact = new Contact();
            Contact originalContact = (Contact) entry.getValue();
            copyContact.phoneDeleted.addAll(originalContact.phoneDeleted);
            copyContact.phones.addAll(originalContact.phones);
            copyContact.phoneTypes.addAll(originalContact.phoneTypes);
            copyContact.shortPhones.addAll(originalContact.shortPhones);
            copyContact.first_name = originalContact.first_name;
            copyContact.last_name = originalContact.last_name;
            copyContact.contact_id = originalContact.contact_id;
            copyContact.key = originalContact.key;
            ret.put(copyContact.key, copyContact);
        }
        return ret;
    }

    protected void migratePhoneBookToV7(final SparseArray<Contact> contactHashMap) {
        Utilities.globalQueue.postRunnable(new Runnable() {
            public void run() {
                if (!ContactsController.this.migratingContacts) {
                    Contact value;
                    int a;
                    ContactsController.this.migratingContacts = true;
                    HashMap<String, Contact> migratedMap = new HashMap();
                    HashMap<String, Contact> contactsMap = ContactsController.this.readContactsFromPhoneBook();
                    HashMap<String, String> contactsBookShort = new HashMap();
                    for (Entry<String, Contact> entry : contactsMap.entrySet()) {
                        value = (Contact) entry.getValue();
                        for (a = 0; a < value.shortPhones.size(); a++) {
                            contactsBookShort.put(value.shortPhones.get(a), value.key);
                        }
                    }
                    for (int b = 0; b < contactHashMap.size(); b++) {
                        value = (Contact) contactHashMap.valueAt(b);
                        for (a = 0; a < value.shortPhones.size(); a++) {
                            String key = (String) contactsBookShort.get((String) value.shortPhones.get(a));
                            if (key != null) {
                                value.key = key;
                                migratedMap.put(key, value);
                                break;
                            }
                        }
                    }
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.m11d("migrated contacts " + migratedMap.size() + " of " + contactHashMap.size());
                    }
                    MessagesStorage.getInstance(ContactsController.this.currentAccount).putCachedPhoneBook(migratedMap, true, false);
                }
            }
        });
    }

    protected void performSyncPhoneBook(HashMap<String, Contact> contactHashMap, boolean request, boolean first, boolean schedule, boolean force, boolean checkCount, boolean canceled) {
        if (first || this.contactsBookLoaded) {
            final HashMap<String, Contact> hashMap = contactHashMap;
            final boolean z = schedule;
            final boolean z2 = request;
            final boolean z3 = first;
            final boolean z4 = force;
            final boolean z5 = checkCount;
            final boolean z6 = canceled;
            Utilities.globalQueue.postRunnable(new Runnable() {

                /* renamed from: org.telegram.messenger.ContactsController$11$1 */
                class C02301 implements Runnable {
                    C02301() {
                    }

                    public void run() {
                        ArrayList<User> toDelete = new ArrayList();
                        if (!(hashMap == null || hashMap.isEmpty())) {
                            try {
                                int a;
                                User user;
                                HashMap<String, User> contactsPhonesShort = new HashMap();
                                for (a = 0; a < ContactsController.this.contacts.size(); a++) {
                                    user = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(((TL_contact) ContactsController.this.contacts.get(a)).user_id));
                                    if (!(user == null || TextUtils.isEmpty(user.phone))) {
                                        contactsPhonesShort.put(user.phone, user);
                                    }
                                }
                                int removed = 0;
                                for (Entry<String, Contact> entry : hashMap.entrySet()) {
                                    Contact contact = (Contact) entry.getValue();
                                    boolean was = false;
                                    a = 0;
                                    while (a < contact.shortPhones.size()) {
                                        user = (User) contactsPhonesShort.get((String) contact.shortPhones.get(a));
                                        if (user != null) {
                                            was = true;
                                            toDelete.add(user);
                                            contact.shortPhones.remove(a);
                                            a--;
                                        }
                                        a++;
                                    }
                                    if (!was || contact.shortPhones.size() == 0) {
                                        removed++;
                                    }
                                }
                            } catch (Throwable e) {
                                FileLog.m14e(e);
                            }
                        }
                        if (!toDelete.isEmpty()) {
                            ContactsController.this.deleteContact(toDelete);
                        }
                    }
                }

                public void run() {
                    Contact c;
                    int a;
                    String key;
                    Contact value;
                    int newPhonebookContacts = 0;
                    int serverContactsInPhonebook = 0;
                    HashMap<String, Contact> contactShortHashMap = new HashMap();
                    for (Entry<String, Contact> entry : hashMap.entrySet()) {
                        c = (Contact) entry.getValue();
                        for (a = 0; a < c.shortPhones.size(); a++) {
                            contactShortHashMap.put(c.shortPhones.get(a), c);
                        }
                    }
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.m11d("start read contacts from phone");
                    }
                    if (!z) {
                        ContactsController.this.checkContactsInternal();
                    }
                    final HashMap<String, Contact> contactsMap = ContactsController.this.readContactsFromPhoneBook();
                    final HashMap<String, ArrayList<Object>> phoneBookSectionsDictFinal = new HashMap();
                    final HashMap<String, Contact> phoneBookByShortPhonesFinal = new HashMap();
                    final ArrayList<String> phoneBookSectionsArrayFinal = new ArrayList();
                    for (Entry<String, Contact> entry2 : contactsMap.entrySet()) {
                        Contact contact = (Contact) entry2.getValue();
                        int size = contact.shortPhones.size();
                        for (a = 0; a < size; a++) {
                            String phone = (String) contact.shortPhones.get(a);
                            phoneBookByShortPhonesFinal.put(phone.substring(Math.max(0, phone.length() - 7)), contact);
                        }
                        key = contact.getLetter();
                        ArrayList<Object> arrayList = (ArrayList) phoneBookSectionsDictFinal.get(key);
                        if (arrayList == null) {
                            arrayList = new ArrayList();
                            phoneBookSectionsDictFinal.put(key, arrayList);
                            phoneBookSectionsArrayFinal.add(key);
                        }
                        arrayList.add(contact);
                    }
                    final HashMap<String, Contact> contactsBookShort = new HashMap();
                    int alreadyImportedContacts = hashMap.size();
                    ArrayList<TL_inputPhoneContact> toImport = new ArrayList();
                    String sphone;
                    String sphone9;
                    TL_inputPhoneContact imp;
                    TL_contact contact2;
                    User user;
                    String firstName;
                    String lastName;
                    if (!hashMap.isEmpty()) {
                        for (Entry<String, Contact> pair : contactsMap.entrySet()) {
                            String id = (String) pair.getKey();
                            value = (Contact) pair.getValue();
                            Contact existing = (Contact) hashMap.get(id);
                            if (existing == null) {
                                for (a = 0; a < value.shortPhones.size(); a++) {
                                    c = (Contact) contactShortHashMap.get(value.shortPhones.get(a));
                                    if (c != null) {
                                        existing = c;
                                        id = existing.key;
                                        break;
                                    }
                                }
                            }
                            if (existing != null) {
                                value.imported = existing.imported;
                            }
                            boolean nameChanged = (existing == null || ((TextUtils.isEmpty(value.first_name) || existing.first_name.equals(value.first_name)) && (TextUtils.isEmpty(value.last_name) || existing.last_name.equals(value.last_name)))) ? false : true;
                            int index;
                            if (existing == null || nameChanged) {
                                for (a = 0; a < value.phones.size(); a++) {
                                    sphone = (String) value.shortPhones.get(a);
                                    sphone9 = sphone.substring(Math.max(0, sphone.length() - 7));
                                    contactsBookShort.put(sphone, value);
                                    if (existing != null) {
                                        index = existing.shortPhones.indexOf(sphone);
                                        if (index != -1) {
                                            Integer deleted = (Integer) existing.phoneDeleted.get(index);
                                            value.phoneDeleted.set(a, deleted);
                                            if (deleted.intValue() == 1) {
                                            }
                                        }
                                    }
                                    if (z2) {
                                        if (!nameChanged) {
                                            if (ContactsController.this.contactsByPhone.containsKey(sphone)) {
                                                serverContactsInPhonebook++;
                                            } else {
                                                newPhonebookContacts++;
                                            }
                                        }
                                        imp = new TL_inputPhoneContact();
                                        imp.client_id = (long) value.contact_id;
                                        imp.client_id |= ((long) a) << 32;
                                        imp.first_name = value.first_name;
                                        imp.last_name = value.last_name;
                                        imp.phone = (String) value.phones.get(a);
                                        toImport.add(imp);
                                    }
                                }
                                if (existing != null) {
                                    hashMap.remove(id);
                                }
                            } else {
                                for (a = 0; a < value.phones.size(); a++) {
                                    sphone = (String) value.shortPhones.get(a);
                                    sphone9 = sphone.substring(Math.max(0, sphone.length() - 7));
                                    contactsBookShort.put(sphone, value);
                                    index = existing.shortPhones.indexOf(sphone);
                                    boolean emptyNameReimport = false;
                                    if (z2) {
                                        contact2 = (TL_contact) ContactsController.this.contactsByPhone.get(sphone);
                                        if (contact2 != null) {
                                            user = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(contact2.user_id));
                                            if (user != null) {
                                                serverContactsInPhonebook++;
                                                if (TextUtils.isEmpty(user.first_name) && TextUtils.isEmpty(user.last_name) && !(TextUtils.isEmpty(value.first_name) && TextUtils.isEmpty(value.last_name))) {
                                                    index = -1;
                                                    emptyNameReimport = true;
                                                }
                                            }
                                        } else if (ContactsController.this.contactsByShortPhone.containsKey(sphone9)) {
                                            serverContactsInPhonebook++;
                                        }
                                    }
                                    if (index != -1) {
                                        value.phoneDeleted.set(a, existing.phoneDeleted.get(index));
                                        existing.phones.remove(index);
                                        existing.shortPhones.remove(index);
                                        existing.phoneDeleted.remove(index);
                                        existing.phoneTypes.remove(index);
                                    } else if (z2) {
                                        if (!emptyNameReimport) {
                                            contact2 = (TL_contact) ContactsController.this.contactsByPhone.get(sphone);
                                            if (contact2 != null) {
                                                user = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(contact2.user_id));
                                                if (user != null) {
                                                    serverContactsInPhonebook++;
                                                    firstName = user.first_name != null ? user.first_name : TtmlNode.ANONYMOUS_REGION_ID;
                                                    lastName = user.last_name != null ? user.last_name : TtmlNode.ANONYMOUS_REGION_ID;
                                                    if (firstName.equals(value.first_name)) {
                                                        if (lastName.equals(value.last_name)) {
                                                        }
                                                    }
                                                    if (TextUtils.isEmpty(value.first_name) && TextUtils.isEmpty(value.last_name)) {
                                                    }
                                                } else {
                                                    newPhonebookContacts++;
                                                }
                                            } else if (ContactsController.this.contactsByShortPhone.containsKey(sphone9)) {
                                                serverContactsInPhonebook++;
                                            }
                                        }
                                        imp = new TL_inputPhoneContact();
                                        imp.client_id = (long) value.contact_id;
                                        imp.client_id |= ((long) a) << 32;
                                        imp.first_name = value.first_name;
                                        imp.last_name = value.last_name;
                                        imp.phone = (String) value.phones.get(a);
                                        toImport.add(imp);
                                    }
                                }
                                if (existing.phones.isEmpty()) {
                                    hashMap.remove(id);
                                }
                            }
                        }
                        if (!z3 && hashMap.isEmpty() && toImport.isEmpty() && alreadyImportedContacts == contactsMap.size()) {
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.m11d("contacts not changed!");
                                return;
                            }
                            return;
                        } else if (!(!z2 || hashMap.isEmpty() || contactsMap.isEmpty())) {
                            if (toImport.isEmpty()) {
                                MessagesStorage.getInstance(ContactsController.this.currentAccount).putCachedPhoneBook(contactsMap, false, false);
                            }
                            if (!(true || hashMap.isEmpty())) {
                                AndroidUtilities.runOnUIThread(new C02301());
                            }
                        }
                    } else if (z2) {
                        for (Entry<String, Contact> pair2 : contactsMap.entrySet()) {
                            value = (Contact) pair2.getValue();
                            key = (String) pair2.getKey();
                            for (a = 0; a < value.phones.size(); a++) {
                                if (!z4) {
                                    sphone = (String) value.shortPhones.get(a);
                                    sphone9 = sphone.substring(Math.max(0, sphone.length() - 7));
                                    contact2 = (TL_contact) ContactsController.this.contactsByPhone.get(sphone);
                                    if (contact2 != null) {
                                        user = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(contact2.user_id));
                                        if (user != null) {
                                            serverContactsInPhonebook++;
                                            firstName = user.first_name != null ? user.first_name : TtmlNode.ANONYMOUS_REGION_ID;
                                            lastName = user.last_name != null ? user.last_name : TtmlNode.ANONYMOUS_REGION_ID;
                                            if (firstName.equals(value.first_name)) {
                                                if (lastName.equals(value.last_name)) {
                                                }
                                            }
                                            if (TextUtils.isEmpty(value.first_name) && TextUtils.isEmpty(value.last_name)) {
                                            }
                                        }
                                    } else if (ContactsController.this.contactsByShortPhone.containsKey(sphone9)) {
                                        serverContactsInPhonebook++;
                                    }
                                }
                                imp = new TL_inputPhoneContact();
                                imp.client_id = (long) value.contact_id;
                                imp.client_id |= ((long) a) << 32;
                                imp.first_name = value.first_name;
                                imp.last_name = value.last_name;
                                imp.phone = (String) value.phones.get(a);
                                toImport.add(imp);
                            }
                        }
                    }
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.m11d("done processing contacts");
                    }
                    if (!z2) {
                        Utilities.stageQueue.postRunnable(new Runnable() {

                            /* renamed from: org.telegram.messenger.ContactsController$11$6$1 */
                            class C02401 implements Runnable {
                                C02401() {
                                }

                                public void run() {
                                    ContactsController.this.mergePhonebookAndTelegramContacts(phoneBookSectionsDictFinal, phoneBookSectionsArrayFinal, phoneBookByShortPhonesFinal);
                                }
                            }

                            public void run() {
                                ContactsController.this.contactsBookSPhones = contactsBookShort;
                                ContactsController.this.contactsBook = contactsMap;
                                ContactsController.this.contactsSyncInProgress = false;
                                ContactsController.this.contactsBookLoaded = true;
                                if (z3) {
                                    ContactsController.this.contactsLoaded = true;
                                }
                                if (!ContactsController.this.delayedContactsUpdate.isEmpty() && ContactsController.this.contactsLoaded && ContactsController.this.contactsBookLoaded) {
                                    ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                                    ContactsController.this.delayedContactsUpdate.clear();
                                }
                                AndroidUtilities.runOnUIThread(new C02401());
                            }
                        });
                        if (!contactsMap.isEmpty()) {
                            MessagesStorage.getInstance(ContactsController.this.currentAccount).putCachedPhoneBook(contactsMap, false, false);
                        }
                    } else if (toImport.isEmpty()) {
                        Utilities.stageQueue.postRunnable(new Runnable() {

                            /* renamed from: org.telegram.messenger.ContactsController$11$5$1 */
                            class C02381 implements Runnable {
                                C02381() {
                                }

                                public void run() {
                                    ContactsController.this.mergePhonebookAndTelegramContacts(phoneBookSectionsDictFinal, phoneBookSectionsArrayFinal, phoneBookByShortPhonesFinal);
                                    ContactsController.this.updateUnregisteredContacts();
                                    NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                                    NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsImported, new Object[0]);
                                }
                            }

                            public void run() {
                                ContactsController.this.contactsBookSPhones = contactsBookShort;
                                ContactsController.this.contactsBook = contactsMap;
                                ContactsController.this.contactsSyncInProgress = false;
                                ContactsController.this.contactsBookLoaded = true;
                                if (z3) {
                                    ContactsController.this.contactsLoaded = true;
                                }
                                if (!ContactsController.this.delayedContactsUpdate.isEmpty() && ContactsController.this.contactsLoaded) {
                                    ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                                    ContactsController.this.delayedContactsUpdate.clear();
                                }
                                AndroidUtilities.runOnUIThread(new C02381());
                            }
                        });
                    } else {
                        int checkType;
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.m12e("start import contacts");
                        }
                        if (!z5 || newPhonebookContacts == 0) {
                            checkType = 0;
                        } else if (newPhonebookContacts >= 30) {
                            checkType = 1;
                        } else if (z3 && alreadyImportedContacts == 0 && ContactsController.this.contactsByPhone.size() - serverContactsInPhonebook > (ContactsController.this.contactsByPhone.size() / 3) * 2) {
                            checkType = 2;
                        } else {
                            checkType = 0;
                        }
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.m11d("new phone book contacts " + newPhonebookContacts + " serverContactsInPhonebook " + serverContactsInPhonebook + " totalContacts " + ContactsController.this.contactsByPhone.size());
                        }
                        if (checkType != 0) {
                            final int i = checkType;
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.hasNewContactsToImport, Integer.valueOf(i), hashMap, Boolean.valueOf(z3), Boolean.valueOf(z));
                                }
                            });
                        } else if (z6) {
                            Utilities.stageQueue.postRunnable(new Runnable() {

                                /* renamed from: org.telegram.messenger.ContactsController$11$3$1 */
                                class C02321 implements Runnable {
                                    C02321() {
                                    }

                                    public void run() {
                                        ContactsController.this.mergePhonebookAndTelegramContacts(phoneBookSectionsDictFinal, phoneBookSectionsArrayFinal, phoneBookByShortPhonesFinal);
                                        ContactsController.this.updateUnregisteredContacts();
                                        NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                                        NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsImported, new Object[0]);
                                    }
                                }

                                public void run() {
                                    ContactsController.this.contactsBookSPhones = contactsBookShort;
                                    ContactsController.this.contactsBook = contactsMap;
                                    ContactsController.this.contactsSyncInProgress = false;
                                    ContactsController.this.contactsBookLoaded = true;
                                    if (z3) {
                                        ContactsController.this.contactsLoaded = true;
                                    }
                                    if (!ContactsController.this.delayedContactsUpdate.isEmpty() && ContactsController.this.contactsLoaded) {
                                        ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                                        ContactsController.this.delayedContactsUpdate.clear();
                                    }
                                    MessagesStorage.getInstance(ContactsController.this.currentAccount).putCachedPhoneBook(contactsMap, false, false);
                                    AndroidUtilities.runOnUIThread(new C02321());
                                }
                            });
                        } else {
                            final boolean[] hasErrors = new boolean[]{false};
                            final HashMap<String, Contact> contactsMapToSave = new HashMap(contactsMap);
                            final SparseArray<String> contactIdToKey = new SparseArray();
                            for (Entry<String, Contact> entry22 : contactsMapToSave.entrySet()) {
                                value = (Contact) entry22.getValue();
                                contactIdToKey.put(value.contact_id, value.key);
                            }
                            ContactsController.this.completedRequestsCount = 0;
                            final int count = (int) Math.ceil(((double) toImport.size()) / 500.0d);
                            for (a = 0; a < count; a++) {
                                final TLObject req = new TL_contacts_importContacts();
                                int start = a * 500;
                                req.contacts = new ArrayList(toImport.subList(start, Math.min(start + 500, toImport.size())));
                                final HashMap<String, Contact> hashMap = contactsMap;
                                final HashMap<String, Contact> hashMap2 = contactsBookShort;
                                final HashMap<String, ArrayList<Object>> hashMap3 = phoneBookSectionsDictFinal;
                                final ArrayList<String> arrayList2 = phoneBookSectionsArrayFinal;
                                final HashMap<String, Contact> hashMap4 = phoneBookByShortPhonesFinal;
                                ConnectionsManager.getInstance(ContactsController.this.currentAccount).sendRequest(req, new RequestDelegate() {

                                    /* renamed from: org.telegram.messenger.ContactsController$11$4$1 */
                                    class C02361 implements Runnable {

                                        /* renamed from: org.telegram.messenger.ContactsController$11$4$1$1 */
                                        class C02341 implements Runnable {
                                            C02341() {
                                            }

                                            public void run() {
                                                ContactsController.this.mergePhonebookAndTelegramContacts(hashMap3, arrayList2, hashMap4);
                                                NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsImported, new Object[0]);
                                            }
                                        }

                                        /* renamed from: org.telegram.messenger.ContactsController$11$4$1$2 */
                                        class C02352 implements Runnable {
                                            C02352() {
                                            }

                                            public void run() {
                                                MessagesStorage.getInstance(ContactsController.this.currentAccount).getCachedPhoneBook(true);
                                            }
                                        }

                                        C02361() {
                                        }

                                        public void run() {
                                            ContactsController.this.contactsBookSPhones = hashMap2;
                                            ContactsController.this.contactsBook = hashMap;
                                            ContactsController.this.contactsSyncInProgress = false;
                                            ContactsController.this.contactsBookLoaded = true;
                                            if (z3) {
                                                ContactsController.this.contactsLoaded = true;
                                            }
                                            if (!ContactsController.this.delayedContactsUpdate.isEmpty() && ContactsController.this.contactsLoaded) {
                                                ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                                                ContactsController.this.delayedContactsUpdate.clear();
                                            }
                                            AndroidUtilities.runOnUIThread(new C02341());
                                            if (hasErrors[0]) {
                                                Utilities.globalQueue.postRunnable(new C02352(), 1800000);
                                            }
                                        }
                                    }

                                    public void run(TLObject response, TL_error error) {
                                        ContactsController.this.completedRequestsCount = ContactsController.this.completedRequestsCount + 1;
                                        int a;
                                        if (error == null) {
                                            if (BuildVars.LOGS_ENABLED) {
                                                FileLog.m11d("contacts imported");
                                            }
                                            TL_contacts_importedContacts res = (TL_contacts_importedContacts) response;
                                            if (!res.retry_contacts.isEmpty()) {
                                                for (a = 0; a < res.retry_contacts.size(); a++) {
                                                    contactsMapToSave.remove(contactIdToKey.get((int) ((Long) res.retry_contacts.get(a)).longValue()));
                                                }
                                                hasErrors[0] = true;
                                                if (BuildVars.LOGS_ENABLED) {
                                                    FileLog.m11d("result has retry contacts");
                                                }
                                            }
                                            for (a = 0; a < res.popular_invites.size(); a++) {
                                                TL_popularContact popularContact = (TL_popularContact) res.popular_invites.get(a);
                                                Contact contact = (Contact) hashMap.get(contactIdToKey.get((int) popularContact.client_id));
                                                if (contact != null) {
                                                    contact.imported = popularContact.importers;
                                                }
                                            }
                                            MessagesStorage.getInstance(ContactsController.this.currentAccount).putUsersAndChats(res.users, null, true, true);
                                            ArrayList<TL_contact> cArr = new ArrayList();
                                            for (a = 0; a < res.imported.size(); a++) {
                                                TL_contact contact2 = new TL_contact();
                                                contact2.user_id = ((TL_importedContact) res.imported.get(a)).user_id;
                                                cArr.add(contact2);
                                            }
                                            ContactsController.this.processLoadedContacts(cArr, res.users, 2);
                                        } else {
                                            for (a = 0; a < req.contacts.size(); a++) {
                                                contactsMapToSave.remove(contactIdToKey.get((int) ((TL_inputPhoneContact) req.contacts.get(a)).client_id));
                                            }
                                            hasErrors[0] = true;
                                            if (BuildVars.LOGS_ENABLED) {
                                                FileLog.m11d("import contacts error " + error.text);
                                            }
                                        }
                                        if (ContactsController.this.completedRequestsCount == count) {
                                            if (!contactsMapToSave.isEmpty()) {
                                                MessagesStorage.getInstance(ContactsController.this.currentAccount).putCachedPhoneBook(contactsMapToSave, false, false);
                                            }
                                            Utilities.stageQueue.postRunnable(new C02361());
                                        }
                                    }
                                }, 6);
                            }
                        }
                    }
                }
            });
        }
    }

    public boolean isLoadingContacts() {
        boolean z;
        synchronized (this.loadContactsSync) {
            z = this.loadingContacts;
        }
        return z;
    }

    private int getContactsHash(ArrayList<TL_contact> contacts) {
        long acc = 0;
        ArrayList<TL_contact> contacts2 = new ArrayList(contacts);
        Collections.sort(contacts2, new C024312());
        int count = contacts2.size();
        for (int a = -1; a < count; a++) {
            long j;
            int i;
            if (a == -1) {
                j = (acc * 20261) + 2147483648L;
                i = UserConfig.getInstance(this.currentAccount).contactsSavedCount;
            } else {
                j = (acc * 20261) + 2147483648L;
                i = ((TL_contact) contacts2.get(a)).user_id;
            }
            acc = (j + ((long) i)) % 2147483648L;
        }
        return (int) acc;
    }

    public void loadContacts(boolean fromCache, final int hash) {
        synchronized (this.loadContactsSync) {
            this.loadingContacts = true;
        }
        if (fromCache) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.m11d("load contacts from cache");
            }
            MessagesStorage.getInstance(this.currentAccount).getContacts();
            return;
        }
        if (BuildVars.LOGS_ENABLED) {
            FileLog.m11d("load contacts from server");
        }
        TL_contacts_getContacts req = new TL_contacts_getContacts();
        req.hash = hash;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new RequestDelegate() {

            /* renamed from: org.telegram.messenger.ContactsController$13$1 */
            class C02441 implements Runnable {
                C02441() {
                }

                public void run() {
                    synchronized (ContactsController.this.loadContactsSync) {
                        ContactsController.this.loadingContacts = false;
                    }
                    NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                }
            }

            public void run(TLObject response, TL_error error) {
                if (error == null) {
                    contacts_Contacts res = (contacts_Contacts) response;
                    if (hash == 0 || !(res instanceof TL_contacts_contactsNotModified)) {
                        UserConfig.getInstance(ContactsController.this.currentAccount).contactsSavedCount = res.saved_count;
                        UserConfig.getInstance(ContactsController.this.currentAccount).saveConfig(false);
                        ContactsController.this.processLoadedContacts(res.contacts, res.users, 0);
                        return;
                    }
                    ContactsController.this.contactsLoaded = true;
                    if (!ContactsController.this.delayedContactsUpdate.isEmpty() && ContactsController.this.contactsBookLoaded) {
                        ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                        ContactsController.this.delayedContactsUpdate.clear();
                    }
                    UserConfig.getInstance(ContactsController.this.currentAccount).lastContactsSyncTime = (int) (System.currentTimeMillis() / 1000);
                    UserConfig.getInstance(ContactsController.this.currentAccount).saveConfig(false);
                    AndroidUtilities.runOnUIThread(new C02441());
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.m11d("load contacts don't change");
                    }
                }
            }
        });
    }

    public void processLoadedContacts(final ArrayList<TL_contact> contactsArr, final ArrayList<User> usersArr, final int from) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                int a;
                boolean z = true;
                MessagesController instance = MessagesController.getInstance(ContactsController.this.currentAccount);
                ArrayList arrayList = usersArr;
                if (from != 1) {
                    z = false;
                }
                instance.putUsers(arrayList, z);
                final SparseArray<User> usersDict = new SparseArray();
                final boolean isEmpty = contactsArr.isEmpty();
                if (!ContactsController.this.contacts.isEmpty()) {
                    a = 0;
                    while (a < contactsArr.size()) {
                        if (ContactsController.this.contactsDict.get(Integer.valueOf(((TL_contact) contactsArr.get(a)).user_id)) != null) {
                            contactsArr.remove(a);
                            a--;
                        }
                        a++;
                    }
                    contactsArr.addAll(ContactsController.this.contacts);
                }
                for (a = 0; a < contactsArr.size(); a++) {
                    User user = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(((TL_contact) contactsArr.get(a)).user_id));
                    if (user != null) {
                        usersDict.put(user.f228id, user);
                    }
                }
                Utilities.stageQueue.postRunnable(new Runnable() {

                    /* renamed from: org.telegram.messenger.ContactsController$14$1$1 */
                    class C02461 implements Comparator<TL_contact> {
                        C02461() {
                        }

                        public int compare(TL_contact tl_contact, TL_contact tl_contact2) {
                            return UserObject.getFirstName((User) usersDict.get(tl_contact.user_id)).compareTo(UserObject.getFirstName((User) usersDict.get(tl_contact2.user_id)));
                        }
                    }

                    /* renamed from: org.telegram.messenger.ContactsController$14$1$2 */
                    class C02472 implements Comparator<String> {
                        C02472() {
                        }

                        public int compare(String s, String s2) {
                            char cv1 = s.charAt(0);
                            char cv2 = s2.charAt(0);
                            if (cv1 == '#') {
                                return 1;
                            }
                            if (cv2 == '#') {
                                return -1;
                            }
                            return s.compareTo(s2);
                        }
                    }

                    /* renamed from: org.telegram.messenger.ContactsController$14$1$3 */
                    class C02483 implements Comparator<String> {
                        C02483() {
                        }

                        public int compare(String s, String s2) {
                            char cv1 = s.charAt(0);
                            char cv2 = s2.charAt(0);
                            if (cv1 == '#') {
                                return 1;
                            }
                            if (cv2 == '#') {
                                return -1;
                            }
                            return s.compareTo(s2);
                        }
                    }

                    public void run() {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.m11d("done loading contacts");
                        }
                        if (from == 1 && (contactsArr.isEmpty() || Math.abs((System.currentTimeMillis() / 1000) - ((long) UserConfig.getInstance(ContactsController.this.currentAccount).lastContactsSyncTime)) >= 86400)) {
                            ContactsController.this.loadContacts(false, ContactsController.this.getContactsHash(contactsArr));
                            if (contactsArr.isEmpty()) {
                                return;
                            }
                        }
                        if (from == 0) {
                            UserConfig.getInstance(ContactsController.this.currentAccount).lastContactsSyncTime = (int) (System.currentTimeMillis() / 1000);
                            UserConfig.getInstance(ContactsController.this.currentAccount).saveConfig(false);
                        }
                        int a = 0;
                        while (a < contactsArr.size()) {
                            TL_contact contact = (TL_contact) contactsArr.get(a);
                            if (usersDict.get(contact.user_id) != null || contact.user_id == UserConfig.getInstance(ContactsController.this.currentAccount).getClientUserId()) {
                                a++;
                            } else {
                                ContactsController.this.loadContacts(false, 0);
                                if (BuildVars.LOGS_ENABLED) {
                                    FileLog.m11d("contacts are broken, load from server");
                                    return;
                                }
                                return;
                            }
                        }
                        if (from != 1) {
                            MessagesStorage.getInstance(ContactsController.this.currentAccount).putUsersAndChats(usersArr, null, true, true);
                            MessagesStorage.getInstance(ContactsController.this.currentAccount).putContacts(contactsArr, from != 2);
                        }
                        Collections.sort(contactsArr, new C02461());
                        final ConcurrentHashMap<Integer, TL_contact> contactsDictionary = new ConcurrentHashMap(20, 1.0f, 2);
                        final HashMap<String, ArrayList<TL_contact>> sectionsDict = new HashMap();
                        final HashMap<String, ArrayList<TL_contact>> sectionsDictMutual = new HashMap();
                        final ArrayList<String> sortedSectionsArray = new ArrayList();
                        final ArrayList<String> sortedSectionsArrayMutual = new ArrayList();
                        HashMap<String, TL_contact> contactsByPhonesDict = null;
                        HashMap<String, TL_contact> contactsByPhonesShortDict = null;
                        if (!ContactsController.this.contactsBookLoaded) {
                            contactsByPhonesDict = new HashMap();
                            contactsByPhonesShortDict = new HashMap();
                        }
                        final HashMap<String, TL_contact> contactsByPhonesDictFinal = contactsByPhonesDict;
                        HashMap<String, TL_contact> contactsByPhonesShortDictFinal = contactsByPhonesShortDict;
                        for (a = 0; a < contactsArr.size(); a++) {
                            TL_contact value = (TL_contact) contactsArr.get(a);
                            User user = (User) usersDict.get(value.user_id);
                            if (user != null) {
                                contactsDictionary.put(Integer.valueOf(value.user_id), value);
                                if (!(contactsByPhonesDict == null || TextUtils.isEmpty(user.phone))) {
                                    contactsByPhonesDict.put(user.phone, value);
                                    contactsByPhonesShortDict.put(user.phone.substring(Math.max(0, user.phone.length() - 7)), value);
                                }
                                String key = UserObject.getFirstName(user);
                                if (key.length() > 1) {
                                    key = key.substring(0, 1);
                                }
                                if (key.length() == 0) {
                                    key = "#";
                                } else {
                                    key = key.toUpperCase();
                                }
                                String replace = (String) ContactsController.this.sectionsToReplace.get(key);
                                if (replace != null) {
                                    key = replace;
                                }
                                ArrayList<TL_contact> arr = (ArrayList) sectionsDict.get(key);
                                if (arr == null) {
                                    arr = new ArrayList();
                                    sectionsDict.put(key, arr);
                                    sortedSectionsArray.add(key);
                                }
                                arr.add(value);
                                if (user.mutual_contact) {
                                    arr = (ArrayList) sectionsDictMutual.get(key);
                                    if (arr == null) {
                                        arr = new ArrayList();
                                        sectionsDictMutual.put(key, arr);
                                        sortedSectionsArrayMutual.add(key);
                                    }
                                    arr.add(value);
                                }
                            }
                        }
                        Collections.sort(sortedSectionsArray, new C02472());
                        Collections.sort(sortedSectionsArrayMutual, new C02483());
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                ContactsController.this.contacts = contactsArr;
                                ContactsController.this.contactsDict = contactsDictionary;
                                ContactsController.this.usersSectionsDict = sectionsDict;
                                ContactsController.this.usersMutualSectionsDict = sectionsDictMutual;
                                ContactsController.this.sortedUsersSectionsArray = sortedSectionsArray;
                                ContactsController.this.sortedUsersMutualSectionsArray = sortedSectionsArrayMutual;
                                if (from != 2) {
                                    synchronized (ContactsController.this.loadContactsSync) {
                                        ContactsController.this.loadingContacts = false;
                                    }
                                }
                                ContactsController.this.performWriteContactsToPhoneBook();
                                ContactsController.this.updateUnregisteredContacts();
                                NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                                if (from == 1 || isEmpty) {
                                    ContactsController.this.reloadContactsStatusesMaybe();
                                } else {
                                    ContactsController.this.saveContactsLoadTime();
                                }
                            }
                        });
                        if (!ContactsController.this.delayedContactsUpdate.isEmpty() && ContactsController.this.contactsLoaded && ContactsController.this.contactsBookLoaded) {
                            ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                            ContactsController.this.delayedContactsUpdate.clear();
                        }
                        if (contactsByPhonesDictFinal != null) {
                            final HashMap<String, TL_contact> hashMap = contactsByPhonesShortDictFinal;
                            AndroidUtilities.runOnUIThread(new Runnable() {

                                /* renamed from: org.telegram.messenger.ContactsController$14$1$5$1 */
                                class C02501 implements Runnable {
                                    C02501() {
                                    }

                                    public void run() {
                                        ContactsController.this.contactsByPhone = contactsByPhonesDictFinal;
                                        ContactsController.this.contactsByShortPhone = hashMap;
                                    }
                                }

                                public void run() {
                                    Utilities.globalQueue.postRunnable(new C02501());
                                    if (!ContactsController.this.contactsSyncInProgress) {
                                        ContactsController.this.contactsSyncInProgress = true;
                                        MessagesStorage.getInstance(ContactsController.this.currentAccount).getCachedPhoneBook(false);
                                    }
                                }
                            });
                            return;
                        }
                        ContactsController.this.contactsLoaded = true;
                    }
                });
            }
        });
    }

    private void reloadContactsStatusesMaybe() {
        try {
            if (MessagesController.getMainSettings(this.currentAccount).getLong("lastReloadStatusTime", 0) < System.currentTimeMillis() - 86400000) {
                reloadContactsStatuses();
            }
        } catch (Throwable e) {
            FileLog.m14e(e);
        }
    }

    private void saveContactsLoadTime() {
        try {
            MessagesController.getMainSettings(this.currentAccount).edit().putLong("lastReloadStatusTime", System.currentTimeMillis()).commit();
        } catch (Throwable e) {
            FileLog.m14e(e);
        }
    }

    private void mergePhonebookAndTelegramContacts(HashMap<String, ArrayList<Object>> phoneBookSectionsDictFinal, ArrayList<String> phoneBookSectionsArrayFinal, HashMap<String, Contact> phoneBookByShortPhonesFinal) {
        final ArrayList<TL_contact> contactsCopy = new ArrayList(this.contacts);
        final HashMap<String, Contact> hashMap = phoneBookByShortPhonesFinal;
        final HashMap<String, ArrayList<Object>> hashMap2 = phoneBookSectionsDictFinal;
        final ArrayList<String> arrayList = phoneBookSectionsArrayFinal;
        Utilities.globalQueue.postRunnable(new Runnable() {

            /* renamed from: org.telegram.messenger.ContactsController$15$1 */
            class C02541 implements Comparator<Object> {
                C02541() {
                }

                public int compare(Object o1, Object o2) {
                    User user;
                    String name1;
                    Contact contact;
                    String name2;
                    if (o1 instanceof User) {
                        user = (User) o1;
                        name1 = ContactsController.formatName(user.first_name, user.last_name);
                    } else if (o1 instanceof Contact) {
                        contact = (Contact) o1;
                        if (contact.user != null) {
                            name1 = ContactsController.formatName(contact.user.first_name, contact.user.last_name);
                        } else {
                            name1 = ContactsController.formatName(contact.first_name, contact.last_name);
                        }
                    } else {
                        name1 = TtmlNode.ANONYMOUS_REGION_ID;
                    }
                    if (o2 instanceof User) {
                        user = (User) o2;
                        name2 = ContactsController.formatName(user.first_name, user.last_name);
                    } else if (o2 instanceof Contact) {
                        contact = (Contact) o2;
                        if (contact.user != null) {
                            name2 = ContactsController.formatName(contact.user.first_name, contact.user.last_name);
                        } else {
                            name2 = ContactsController.formatName(contact.first_name, contact.last_name);
                        }
                    } else {
                        name2 = TtmlNode.ANONYMOUS_REGION_ID;
                    }
                    return name1.compareTo(name2);
                }
            }

            /* renamed from: org.telegram.messenger.ContactsController$15$2 */
            class C02552 implements Comparator<String> {
                C02552() {
                }

                public int compare(String s, String s2) {
                    char cv1 = s.charAt(0);
                    char cv2 = s2.charAt(0);
                    if (cv1 == '#') {
                        return 1;
                    }
                    if (cv2 == '#') {
                        return -1;
                    }
                    return s.compareTo(s2);
                }
            }

            /* renamed from: org.telegram.messenger.ContactsController$15$3 */
            class C02563 implements Runnable {
                C02563() {
                }

                public void run() {
                    ContactsController.this.phoneBookSectionsArray = arrayList;
                    ContactsController.this.phoneBookSectionsDict = hashMap2;
                }
            }

            public void run() {
                ArrayList<Object> arrayList;
                int size = contactsCopy.size();
                for (int a = 0; a < size; a++) {
                    User user = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(((TL_contact) contactsCopy.get(a)).user_id));
                    if (!(user == null || TextUtils.isEmpty(user.phone))) {
                        Contact contact = (Contact) hashMap.get(user.phone.substring(Math.max(0, user.phone.length() - 7)));
                        if (contact == null) {
                            String key = Contact.getLetter(user.first_name, user.last_name);
                            arrayList = (ArrayList) hashMap2.get(key);
                            if (arrayList == null) {
                                arrayList = new ArrayList();
                                hashMap2.put(key, arrayList);
                                arrayList.add(key);
                            }
                            arrayList.add(user);
                        } else if (contact.user == null) {
                            contact.user = user;
                        }
                    }
                }
                for (ArrayList<Object> arrayList2 : hashMap2.values()) {
                    Collections.sort(arrayList2, new C02541());
                }
                Collections.sort(arrayList, new C02552());
                AndroidUtilities.runOnUIThread(new C02563());
            }
        });
    }

    private void updateUnregisteredContacts() {
        int a;
        HashMap<String, TL_contact> contactsPhonesShort = new HashMap();
        int size = this.contacts.size();
        for (a = 0; a < size; a++) {
            TL_contact value = (TL_contact) this.contacts.get(a);
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(value.user_id));
            if (!(user == null || TextUtils.isEmpty(user.phone))) {
                contactsPhonesShort.put(user.phone, value);
            }
        }
        ArrayList<Contact> sortedPhoneBookContacts = new ArrayList();
        for (Entry<String, Contact> pair : this.contactsBook.entrySet()) {
            Contact value2 = (Contact) pair.getValue();
            boolean skip = false;
            a = 0;
            while (a < value2.phones.size()) {
                if (contactsPhonesShort.containsKey((String) value2.shortPhones.get(a)) || ((Integer) value2.phoneDeleted.get(a)).intValue() == 1) {
                    skip = true;
                    break;
                }
                a++;
            }
            if (!skip) {
                sortedPhoneBookContacts.add(value2);
            }
        }
        Collections.sort(sortedPhoneBookContacts, new C025816());
        this.phoneBookContacts = sortedPhoneBookContacts;
    }

    private void buildContactsSectionsArrays(boolean sort) {
        if (sort) {
            Collections.sort(this.contacts, new C025917());
        }
        HashMap<String, ArrayList<TL_contact>> sectionsDict = new HashMap();
        ArrayList<String> sortedSectionsArray = new ArrayList();
        for (int a = 0; a < this.contacts.size(); a++) {
            TL_contact value = (TL_contact) this.contacts.get(a);
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(value.user_id));
            if (user != null) {
                String key = UserObject.getFirstName(user);
                if (key.length() > 1) {
                    key = key.substring(0, 1);
                }
                if (key.length() == 0) {
                    key = "#";
                } else {
                    key = key.toUpperCase();
                }
                String replace = (String) this.sectionsToReplace.get(key);
                if (replace != null) {
                    key = replace;
                }
                ArrayList<TL_contact> arr = (ArrayList) sectionsDict.get(key);
                if (arr == null) {
                    arr = new ArrayList();
                    sectionsDict.put(key, arr);
                    sortedSectionsArray.add(key);
                }
                arr.add(value);
            }
        }
        Collections.sort(sortedSectionsArray, new C026018());
        this.usersSectionsDict = sectionsDict;
        this.sortedUsersSectionsArray = sortedSectionsArray;
    }

    private boolean hasContactsPermission() {
        if (VERSION.SDK_INT < 23) {
            Cursor cursor = null;
            try {
                cursor = ApplicationLoader.applicationContext.getContentResolver().query(Phone.CONTENT_URI, this.projectionPhones, null, null, null);
                if (cursor == null || cursor.getCount() == 0) {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable e) {
                            FileLog.m14e(e);
                        }
                    }
                    return false;
                }
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable e2) {
                        FileLog.m14e(e2);
                    }
                }
                return true;
            } catch (Throwable th) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable e22) {
                        FileLog.m14e(e22);
                    }
                }
            }
        } else if (ApplicationLoader.applicationContext.checkSelfPermission("android.permission.READ_CONTACTS") == 0) {
            return true;
        } else {
            return false;
        }
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void performWriteContactsToPhoneBookInternal(ArrayList<TL_contact> contactsArray) {
        Cursor cursor = null;
        try {
            if (hasContactsPermission()) {
                Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("account_name", this.systemAccount.name).appendQueryParameter("account_type", this.systemAccount.type).build();
                cursor = ApplicationLoader.applicationContext.getContentResolver().query(rawContactUri, new String[]{"_id", "sync2"}, null, null, null);
                SparseLongArray bookContacts = new SparseLongArray();
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        bookContacts.put(cursor.getInt(1), cursor.getLong(0));
                    }
                    cursor.close();
                    cursor = null;
                    for (int a = 0; a < contactsArray.size(); a++) {
                        TL_contact u = (TL_contact) contactsArray.get(a);
                        if (bookContacts.indexOfKey(u.user_id) < 0) {
                            addContactToPhoneBook(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(u.user_id)), false);
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } else if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable e) {
            FileLog.m14e(e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private void performWriteContactsToPhoneBook() {
        final ArrayList<TL_contact> contactsArray = new ArrayList(this.contacts);
        Utilities.phoneBookQueue.postRunnable(new Runnable() {
            public void run() {
                ContactsController.this.performWriteContactsToPhoneBookInternal(contactsArray);
            }
        });
    }

    private void applyContactsUpdates(ArrayList<Integer> ids, ConcurrentHashMap<Integer, User> userDict, ArrayList<TL_contact> newC, ArrayList<Integer> contactsTD) {
        int a;
        Integer uid;
        User user;
        Contact contact;
        int index;
        if (newC == null || contactsTD == null) {
            newC = new ArrayList();
            contactsTD = new ArrayList();
            for (a = 0; a < ids.size(); a++) {
                uid = (Integer) ids.get(a);
                if (uid.intValue() > 0) {
                    TL_contact contact2 = new TL_contact();
                    contact2.user_id = uid.intValue();
                    newC.add(contact2);
                } else if (uid.intValue() < 0) {
                    contactsTD.add(Integer.valueOf(-uid.intValue()));
                }
            }
        }
        if (BuildVars.LOGS_ENABLED) {
            FileLog.m11d("process update - contacts add = " + newC.size() + " delete = " + contactsTD.size());
        }
        StringBuilder toAdd = new StringBuilder();
        StringBuilder toDelete = new StringBuilder();
        boolean reloadContacts = false;
        for (a = 0; a < newC.size(); a++) {
            TL_contact newContact = (TL_contact) newC.get(a);
            user = null;
            if (userDict != null) {
                user = (User) userDict.get(Integer.valueOf(newContact.user_id));
            }
            if (user == null) {
                user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(newContact.user_id));
            } else {
                MessagesController.getInstance(this.currentAccount).putUser(user, true);
            }
            if (user == null || TextUtils.isEmpty(user.phone)) {
                reloadContacts = true;
            } else {
                contact = (Contact) this.contactsBookSPhones.get(user.phone);
                if (contact != null) {
                    index = contact.shortPhones.indexOf(user.phone);
                    if (index != -1) {
                        contact.phoneDeleted.set(index, Integer.valueOf(0));
                    }
                }
                if (toAdd.length() != 0) {
                    toAdd.append(",");
                }
                toAdd.append(user.phone);
            }
        }
        for (a = 0; a < contactsTD.size(); a++) {
            uid = (Integer) contactsTD.get(a);
            Utilities.phoneBookQueue.postRunnable(new Runnable() {
                public void run() {
                    ContactsController.this.deleteContactFromPhoneBook(uid.intValue());
                }
            });
            user = null;
            if (userDict != null) {
                user = (User) userDict.get(uid);
            }
            if (user == null) {
                user = MessagesController.getInstance(this.currentAccount).getUser(uid);
            } else {
                MessagesController.getInstance(this.currentAccount).putUser(user, true);
            }
            if (user == null) {
                reloadContacts = true;
            } else if (!TextUtils.isEmpty(user.phone)) {
                contact = (Contact) this.contactsBookSPhones.get(user.phone);
                if (contact != null) {
                    index = contact.shortPhones.indexOf(user.phone);
                    if (index != -1) {
                        contact.phoneDeleted.set(index, Integer.valueOf(1));
                    }
                }
                if (toDelete.length() != 0) {
                    toDelete.append(",");
                }
                toDelete.append(user.phone);
            }
        }
        if (!(toAdd.length() == 0 && toDelete.length() == 0)) {
            MessagesStorage.getInstance(this.currentAccount).applyPhoneBookUpdates(toAdd.toString(), toDelete.toString());
        }
        if (reloadContacts) {
            Utilities.stageQueue.postRunnable(new C026421());
            return;
        }
        final ArrayList<TL_contact> newContacts = newC;
        final ArrayList<Integer> contactsToDelete = contactsTD;
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                int a;
                TL_contact contact;
                boolean z = true;
                for (a = 0; a < newContacts.size(); a++) {
                    contact = (TL_contact) newContacts.get(a);
                    if (ContactsController.this.contactsDict.get(Integer.valueOf(contact.user_id)) == null) {
                        ContactsController.this.contacts.add(contact);
                        ContactsController.this.contactsDict.put(Integer.valueOf(contact.user_id), contact);
                    }
                }
                for (a = 0; a < contactsToDelete.size(); a++) {
                    Integer uid = (Integer) contactsToDelete.get(a);
                    contact = (TL_contact) ContactsController.this.contactsDict.get(uid);
                    if (contact != null) {
                        ContactsController.this.contacts.remove(contact);
                        ContactsController.this.contactsDict.remove(uid);
                    }
                }
                if (!newContacts.isEmpty()) {
                    ContactsController.this.updateUnregisteredContacts();
                    ContactsController.this.performWriteContactsToPhoneBook();
                }
                ContactsController.this.performSyncPhoneBook(ContactsController.this.getContactsCopy(ContactsController.this.contactsBook), false, false, false, false, true, false);
                ContactsController contactsController = ContactsController.this;
                if (newContacts.isEmpty()) {
                    z = false;
                }
                contactsController.buildContactsSectionsArrays(z);
                NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
            }
        });
    }

    public void processContactsUpdates(ArrayList<Integer> ids, ConcurrentHashMap<Integer, User> userDict) {
        ArrayList<TL_contact> newContacts = new ArrayList();
        ArrayList<Integer> contactsToDelete = new ArrayList();
        Iterator it = ids.iterator();
        while (it.hasNext()) {
            Integer uid = (Integer) it.next();
            int idx;
            if (uid.intValue() > 0) {
                TL_contact contact = new TL_contact();
                contact.user_id = uid.intValue();
                newContacts.add(contact);
                if (!this.delayedContactsUpdate.isEmpty()) {
                    idx = this.delayedContactsUpdate.indexOf(Integer.valueOf(-uid.intValue()));
                    if (idx != -1) {
                        this.delayedContactsUpdate.remove(idx);
                    }
                }
            } else if (uid.intValue() < 0) {
                contactsToDelete.add(Integer.valueOf(-uid.intValue()));
                if (!this.delayedContactsUpdate.isEmpty()) {
                    idx = this.delayedContactsUpdate.indexOf(Integer.valueOf(-uid.intValue()));
                    if (idx != -1) {
                        this.delayedContactsUpdate.remove(idx);
                    }
                }
            }
        }
        if (!contactsToDelete.isEmpty()) {
            MessagesStorage.getInstance(this.currentAccount).deleteContacts(contactsToDelete);
        }
        if (!newContacts.isEmpty()) {
            MessagesStorage.getInstance(this.currentAccount).putContacts(newContacts, false);
        }
        if (this.contactsLoaded && this.contactsBookLoaded) {
            applyContactsUpdates(ids, userDict, newContacts, contactsToDelete);
            return;
        }
        this.delayedContactsUpdate.addAll(ids);
        if (BuildVars.LOGS_ENABLED) {
            FileLog.m11d("delay update - contacts add = " + newContacts.size() + " delete = " + contactsToDelete.size());
        }
    }

    public long addContactToPhoneBook(User user, boolean check) {
        long res = -1;
        if (!(this.systemAccount == null || user == null || TextUtils.isEmpty(user.phone) || !hasContactsPermission())) {
            res = -1;
            synchronized (this.observerLock) {
                this.ignoreChanges = true;
            }
            ContentResolver contentResolver = ApplicationLoader.applicationContext.getContentResolver();
            if (check) {
                try {
                    contentResolver.delete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").appendQueryParameter("account_name", this.systemAccount.name).appendQueryParameter("account_type", this.systemAccount.type).build(), "sync2 = " + user.f228id, null);
                } catch (Throwable e) {
                    FileLog.m14e(e);
                }
            }
            ArrayList<ContentProviderOperation> query = new ArrayList();
            Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
            builder.withValue("account_name", this.systemAccount.name);
            builder.withValue("account_type", this.systemAccount.type);
            builder.withValue("sync1", user.phone);
            builder.withValue("sync2", Integer.valueOf(user.f228id));
            query.add(builder.build());
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", 0);
            builder.withValue("mimetype", "vnd.android.cursor.item/name");
            builder.withValue("data2", user.first_name);
            builder.withValue("data3", user.last_name);
            query.add(builder.build());
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", 0);
            builder.withValue("mimetype", "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile");
            builder.withValue("data1", Integer.valueOf(user.f228id));
            builder.withValue("data2", "Telegram Profile");
            builder.withValue("data3", "+" + user.phone);
            builder.withValue("data4", Integer.valueOf(user.f228id));
            query.add(builder.build());
            try {
                ContentProviderResult[] result = contentResolver.applyBatch("com.android.contacts", query);
                if (!(result == null || result.length <= 0 || result[0].uri == null)) {
                    res = Long.parseLong(result[0].uri.getLastPathSegment());
                }
            } catch (Throwable e2) {
                FileLog.m14e(e2);
            }
            synchronized (this.observerLock) {
                this.ignoreChanges = false;
            }
        }
        return res;
    }

    private void deleteContactFromPhoneBook(int uid) {
        if (hasContactsPermission()) {
            synchronized (this.observerLock) {
                this.ignoreChanges = true;
            }
            try {
                ApplicationLoader.applicationContext.getContentResolver().delete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").appendQueryParameter("account_name", this.systemAccount.name).appendQueryParameter("account_type", this.systemAccount.type).build(), "sync2 = " + uid, null);
            } catch (Throwable e) {
                FileLog.m14e(e);
            }
            synchronized (this.observerLock) {
                this.ignoreChanges = false;
            }
        }
    }

    protected void markAsContacted(final String contactId) {
        if (contactId != null) {
            Utilities.phoneBookQueue.postRunnable(new Runnable() {
                public void run() {
                    Uri uri = Uri.parse(contactId);
                    ContentValues values = new ContentValues();
                    values.put("last_time_contacted", Long.valueOf(System.currentTimeMillis()));
                    ApplicationLoader.applicationContext.getContentResolver().update(uri, values, null, null);
                }
            });
        }
    }

    public void addContact(User user) {
        if (user != null && !TextUtils.isEmpty(user.phone)) {
            TL_contacts_importContacts req = new TL_contacts_importContacts();
            ArrayList<TL_inputPhoneContact> contactsParams = new ArrayList();
            TL_inputPhoneContact c = new TL_inputPhoneContact();
            c.phone = user.phone;
            if (!c.phone.startsWith("+")) {
                c.phone = "+" + c.phone;
            }
            c.first_name = user.first_name;
            c.last_name = user.last_name;
            c.client_id = 0;
            contactsParams.add(c);
            req.contacts = contactsParams;
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new C026924(), 6);
        }
    }

    public void deleteContact(final ArrayList<User> users) {
        if (users != null && !users.isEmpty()) {
            TL_contacts_deleteContacts req = new TL_contacts_deleteContacts();
            final ArrayList<Integer> uids = new ArrayList();
            Iterator it = users.iterator();
            while (it.hasNext()) {
                User user = (User) it.next();
                InputUser inputUser = MessagesController.getInstance(this.currentAccount).getInputUser(user);
                if (inputUser != null) {
                    uids.add(Integer.valueOf(user.f228id));
                    req.f161id.add(inputUser);
                }
            }
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new RequestDelegate() {

                /* renamed from: org.telegram.messenger.ContactsController$25$1 */
                class C02701 implements Runnable {
                    C02701() {
                    }

                    public void run() {
                        Iterator it = users.iterator();
                        while (it.hasNext()) {
                            ContactsController.this.deleteContactFromPhoneBook(((User) it.next()).f228id);
                        }
                    }
                }

                /* renamed from: org.telegram.messenger.ContactsController$25$2 */
                class C02712 implements Runnable {
                    C02712() {
                    }

                    public void run() {
                        boolean remove = false;
                        Iterator it = users.iterator();
                        while (it.hasNext()) {
                            User user = (User) it.next();
                            TL_contact contact = (TL_contact) ContactsController.this.contactsDict.get(Integer.valueOf(user.f228id));
                            if (contact != null) {
                                remove = true;
                                ContactsController.this.contacts.remove(contact);
                                ContactsController.this.contactsDict.remove(Integer.valueOf(user.f228id));
                            }
                        }
                        if (remove) {
                            ContactsController.this.buildContactsSectionsArrays(false);
                        }
                        NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(1));
                        NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                    }
                }

                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        MessagesStorage.getInstance(ContactsController.this.currentAccount).deleteContacts(uids);
                        Utilities.phoneBookQueue.postRunnable(new C02701());
                        for (int a = 0; a < users.size(); a++) {
                            User user = (User) users.get(a);
                            if (!TextUtils.isEmpty(user.phone)) {
                                CharSequence name = UserObject.getUserName(user);
                                MessagesStorage.getInstance(ContactsController.this.currentAccount).applyPhoneBookUpdates(user.phone, TtmlNode.ANONYMOUS_REGION_ID);
                                Contact contact = (Contact) ContactsController.this.contactsBookSPhones.get(user.phone);
                                if (contact != null) {
                                    int index = contact.shortPhones.indexOf(user.phone);
                                    if (index != -1) {
                                        contact.phoneDeleted.set(index, Integer.valueOf(1));
                                    }
                                }
                            }
                        }
                        AndroidUtilities.runOnUIThread(new C02712());
                    }
                }
            });
        }
    }

    public void reloadContactsStatuses() {
        saveContactsLoadTime();
        MessagesController.getInstance(this.currentAccount).clearFullUsers();
        final Editor editor = MessagesController.getMainSettings(this.currentAccount).edit();
        editor.putBoolean("needGetStatuses", true).commit();
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(new TL_contacts_getStatuses(), new RequestDelegate() {
            public void run(final TLObject response, TL_error error) {
                if (error == null) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            editor.remove("needGetStatuses").commit();
                            Vector vector = response;
                            if (!vector.objects.isEmpty()) {
                                ArrayList<User> dbUsersStatus = new ArrayList();
                                Iterator it = vector.objects.iterator();
                                while (it.hasNext()) {
                                    TL_contactStatus object = it.next();
                                    User toDbUser = new TL_user();
                                    TL_contactStatus status = object;
                                    if (status != null) {
                                        if (status.status instanceof TL_userStatusRecently) {
                                            status.status.expires = -100;
                                        } else if (status.status instanceof TL_userStatusLastWeek) {
                                            status.status.expires = -101;
                                        } else if (status.status instanceof TL_userStatusLastMonth) {
                                            status.status.expires = -102;
                                        }
                                        User user = MessagesController.getInstance(ContactsController.this.currentAccount).getUser(Integer.valueOf(status.user_id));
                                        if (user != null) {
                                            user.status = status.status;
                                        }
                                        toDbUser.status = status.status;
                                        dbUsersStatus.add(toDbUser);
                                    }
                                }
                                MessagesStorage.getInstance(ContactsController.this.currentAccount).updateUsers(dbUsersStatus, true, true, true);
                            }
                            NotificationCenter.getInstance(ContactsController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(4));
                        }
                    });
                }
            }
        });
    }

    public void loadPrivacySettings() {
        TL_account_getPrivacy req;
        if (this.loadingDeleteInfo == 0) {
            this.loadingDeleteInfo = 1;
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(new TL_account_getAccountTTL(), new C027627());
        }
        if (this.loadingLastSeenInfo == 0) {
            this.loadingLastSeenInfo = 1;
            req = new TL_account_getPrivacy();
            req.key = new TL_inputPrivacyKeyStatusTimestamp();
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new C027828());
        }
        if (this.loadingCallsInfo == 0) {
            this.loadingCallsInfo = 1;
            req = new TL_account_getPrivacy();
            req.key = new TL_inputPrivacyKeyPhoneCall();
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new C028029());
        }
        if (this.loadingGroupInfo == 0) {
            this.loadingGroupInfo = 1;
            req = new TL_account_getPrivacy();
            req.key = new TL_inputPrivacyKeyChatInvite();
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new C028430());
        }
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
    }

    public void setDeleteAccountTTL(int ttl) {
        this.deleteAccountTTL = ttl;
    }

    public int getDeleteAccountTTL() {
        return this.deleteAccountTTL;
    }

    public boolean getLoadingDeleteInfo() {
        return this.loadingDeleteInfo != 2;
    }

    public boolean getLoadingLastSeenInfo() {
        return this.loadingLastSeenInfo != 2;
    }

    public boolean getLoadingCallsInfo() {
        return this.loadingCallsInfo != 2;
    }

    public boolean getLoadingGroupInfo() {
        return this.loadingGroupInfo != 2;
    }

    public ArrayList<PrivacyRule> getPrivacyRules(int type) {
        if (type == 2) {
            return this.callPrivacyRules;
        }
        if (type == 1) {
            return this.groupPrivacyRules;
        }
        return this.privacyRules;
    }

    public void setPrivacyRules(ArrayList<PrivacyRule> rules, int type) {
        if (type == 2) {
            this.callPrivacyRules = rules;
        } else if (type == 1) {
            this.groupPrivacyRules = rules;
        } else {
            this.privacyRules = rules;
        }
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
        reloadContactsStatuses();
    }

    public static String formatName(String firstName, String lastName) {
        int length;
        int i = 0;
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }
        if (firstName != null) {
            length = firstName.length();
        } else {
            length = 0;
        }
        if (lastName != null) {
            i = lastName.length();
        }
        StringBuilder result = new StringBuilder((i + length) + 1);
        if (LocaleController.nameDisplayOrder == 1) {
            if (firstName != null && firstName.length() > 0) {
                result.append(firstName);
                if (lastName != null && lastName.length() > 0) {
                    result.append(" ");
                    result.append(lastName);
                }
            } else if (lastName != null && lastName.length() > 0) {
                result.append(lastName);
            }
        } else if (lastName != null && lastName.length() > 0) {
            result.append(lastName);
            if (firstName != null && firstName.length() > 0) {
                result.append(" ");
                result.append(firstName);
            }
        } else if (firstName != null && firstName.length() > 0) {
            result.append(firstName);
        }
        return result.toString();
    }
}
