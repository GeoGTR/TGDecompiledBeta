package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.volley.DefaultRetryPolicy;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_contacts_importContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_importedContacts;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPhoneContact;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ContextProgressView;
import org.telegram.ui.Components.HintEditText;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.CountrySelectActivity.CountrySelectActivityDelegate;

public class NewContactActivity extends BaseFragment implements OnItemSelectedListener {
    private static final int done_button = 1;
    private AvatarDrawable avatarDrawable;
    private BackupImageView avatarImage;
    private EditText codeField;
    private HashMap<String, String> codesMap = new HashMap();
    private ArrayList<String> countriesArray = new ArrayList();
    private HashMap<String, String> countriesMap = new HashMap();
    private TextView countryButton;
    private int countryState;
    private boolean donePressed;
    private ActionBarMenuItem editDoneItem;
    private AnimatorSet editDoneItemAnimation;
    private ContextProgressView editDoneItemProgress;
    private EditText firstNameField;
    private boolean ignoreOnPhoneChange;
    private boolean ignoreOnTextChange;
    private boolean ignoreSelection;
    private EditText lastNameField;
    private HintEditText phoneField;
    private HashMap<String, String> phoneFormatMap = new HashMap();

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("AddContactTitle", R.string.AddContactTitle));
        this.actionBar.setActionBarMenuOnItemClick(new ActionBarMenuOnItemClick() {
            public void onItemClick(int id) {
                if (id == -1) {
                    NewContactActivity.this.finishFragment();
                } else if (id == 1 && !NewContactActivity.this.donePressed) {
                    Vibrator v;
                    if (NewContactActivity.this.firstNameField.length() == 0) {
                        v = (Vibrator) NewContactActivity.this.getParentActivity().getSystemService("vibrator");
                        if (v != null) {
                            v.vibrate(200);
                        }
                        AndroidUtilities.shakeView(NewContactActivity.this.firstNameField, 2.0f, 0);
                    } else if (NewContactActivity.this.codeField.length() == 0) {
                        v = (Vibrator) NewContactActivity.this.getParentActivity().getSystemService("vibrator");
                        if (v != null) {
                            v.vibrate(200);
                        }
                        AndroidUtilities.shakeView(NewContactActivity.this.codeField, 2.0f, 0);
                    } else if (NewContactActivity.this.phoneField.length() == 0) {
                        v = (Vibrator) NewContactActivity.this.getParentActivity().getSystemService("vibrator");
                        if (v != null) {
                            v.vibrate(200);
                        }
                        AndroidUtilities.shakeView(NewContactActivity.this.phoneField, 2.0f, 0);
                    } else {
                        NewContactActivity.this.donePressed = true;
                        NewContactActivity.this.showEditDoneProgress(true, true);
                        TL_contacts_importContacts req = new TL_contacts_importContacts();
                        final TL_inputPhoneContact inputPhoneContact = new TL_inputPhoneContact();
                        inputPhoneContact.first_name = NewContactActivity.this.firstNameField.getText().toString();
                        inputPhoneContact.last_name = NewContactActivity.this.lastNameField.getText().toString();
                        inputPhoneContact.phone = "+" + NewContactActivity.this.codeField.getText().toString() + NewContactActivity.this.phoneField.getText().toString();
                        req.contacts.add(inputPhoneContact);
                        ConnectionsManager.getInstance().bindRequestToGuid(ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                            public void run(TLObject response, final TL_error error) {
                                final TL_contacts_importedContacts res = (TL_contacts_importedContacts) response;
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        NewContactActivity.this.donePressed = false;
                                        if (res == null) {
                                            NewContactActivity.this.showEditDoneProgress(false, true);
                                            if (error == null || error.text.startsWith("FLOOD_WAIT")) {
                                                NewContactActivity.this.needShowAlert(LocaleController.getString("AppName", R.string.AppName), LocaleController.getString("FloodWait", R.string.FloodWait));
                                            } else {
                                                NewContactActivity.this.needShowAlert(LocaleController.getString("AppName", R.string.AppName), LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred) + "\n" + error.text);
                                            }
                                        } else if (!res.users.isEmpty()) {
                                            MessagesController.getInstance().putUsers(res.users, false);
                                            MessagesController.openChatOrProfileWith((User) res.users.get(0), null, NewContactActivity.this, 1, true);
                                        } else if (NewContactActivity.this.getParentActivity() != null) {
                                            NewContactActivity.this.showEditDoneProgress(false, true);
                                            Builder builder = new Builder(NewContactActivity.this.getParentActivity());
                                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                            builder.setMessage(LocaleController.formatString("ContactNotRegistered", R.string.ContactNotRegistered, ContactsController.formatName(inputPhoneContact.first_name, inputPhoneContact.last_name)));
                                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                            builder.setPositiveButton(LocaleController.getString("Invite", R.string.Invite), new OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        Intent intent = new Intent("android.intent.action.VIEW", Uri.fromParts("sms", inputPhoneContact.phone, null));
                                                        intent.putExtra("sms_body", LocaleController.getString("InviteText", R.string.InviteText));
                                                        NewContactActivity.this.getParentActivity().startActivityForResult(intent, 500);
                                                    } catch (Throwable e) {
                                                        FileLog.e("tmessages", e);
                                                    }
                                                }
                                            });
                                            NewContactActivity.this.showDialog(builder.create());
                                        }
                                    }
                                });
                            }
                        }, 2), NewContactActivity.this.classGuid);
                    }
                }
            }
        });
        this.avatarDrawable = new AvatarDrawable();
        this.avatarDrawable.setInfo(5, "", "", false);
        this.editDoneItem = this.actionBar.createMenu().addItemWithWidth(1, R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        this.editDoneItemProgress = new ContextProgressView(context, 1);
        this.editDoneItem.addView(this.editDoneItemProgress, LayoutHelper.createFrame(-1, -1.0f));
        this.editDoneItemProgress.setVisibility(4);
        this.fragmentView = new ScrollView(context);
        View linearLayout = new LinearLayout(context);
        linearLayout.setPadding(AndroidUtilities.dp(24.0f), 0, AndroidUtilities.dp(24.0f), 0);
        linearLayout.setOrientation(1);
        ((ScrollView) this.fragmentView).addView(linearLayout, LayoutHelper.createScroll(-1, -2, 51));
        linearLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        FrameLayout frameLayout = new FrameLayout(context);
        linearLayout.addView(frameLayout, LayoutHelper.createLinear(-1, -2, 0.0f, 24.0f, 0.0f, 0.0f));
        this.avatarImage = new BackupImageView(context);
        this.avatarImage.setImageDrawable(this.avatarDrawable);
        frameLayout.addView(this.avatarImage, LayoutHelper.createFrame(60, BitmapDescriptorFactory.HUE_YELLOW, 51, 0.0f, 9.0f, 0.0f, 0.0f));
        this.firstNameField = new EditText(context);
        this.firstNameField.setTextSize(1, 18.0f);
        this.firstNameField.setHintTextColor(Theme.SHARE_SHEET_EDIT_PLACEHOLDER_TEXT_COLOR);
        this.firstNameField.setTextColor(-14606047);
        this.firstNameField.setMaxLines(1);
        this.firstNameField.setLines(1);
        this.firstNameField.setSingleLine(true);
        this.firstNameField.setGravity(3);
        this.firstNameField.setInputType(49152);
        this.firstNameField.setImeOptions(5);
        this.firstNameField.setHint(LocaleController.getString("FirstName", R.string.FirstName));
        AndroidUtilities.clearCursorDrawable(this.firstNameField);
        frameLayout.addView(this.firstNameField, LayoutHelper.createFrame(-1, 34.0f, 51, 84.0f, 0.0f, 0.0f, 0.0f));
        this.firstNameField.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i != 5) {
                    return false;
                }
                NewContactActivity.this.lastNameField.requestFocus();
                NewContactActivity.this.lastNameField.setSelection(NewContactActivity.this.lastNameField.length());
                return true;
            }
        });
        this.firstNameField.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                NewContactActivity.this.avatarDrawable.setInfo(5, NewContactActivity.this.firstNameField.getText().toString(), NewContactActivity.this.lastNameField.getText().toString(), false);
                NewContactActivity.this.avatarImage.invalidate();
            }
        });
        this.lastNameField = new EditText(context);
        this.lastNameField.setTextSize(1, 18.0f);
        this.lastNameField.setHintTextColor(Theme.SHARE_SHEET_EDIT_PLACEHOLDER_TEXT_COLOR);
        this.lastNameField.setTextColor(-14606047);
        this.lastNameField.setMaxLines(1);
        this.lastNameField.setLines(1);
        this.lastNameField.setSingleLine(true);
        this.lastNameField.setGravity(3);
        this.lastNameField.setInputType(49152);
        this.lastNameField.setImeOptions(5);
        this.lastNameField.setHint(LocaleController.getString("LastName", R.string.LastName));
        AndroidUtilities.clearCursorDrawable(this.lastNameField);
        frameLayout.addView(this.lastNameField, LayoutHelper.createFrame(-1, 34.0f, 51, 84.0f, 44.0f, 0.0f, 0.0f));
        this.lastNameField.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i != 5) {
                    return false;
                }
                NewContactActivity.this.phoneField.requestFocus();
                NewContactActivity.this.phoneField.setSelection(NewContactActivity.this.phoneField.length());
                return true;
            }
        });
        this.lastNameField.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                NewContactActivity.this.avatarDrawable.setInfo(5, NewContactActivity.this.firstNameField.getText().toString(), NewContactActivity.this.lastNameField.getText().toString(), false);
                NewContactActivity.this.avatarImage.invalidate();
            }
        });
        this.countryButton = new TextView(context);
        this.countryButton.setTextSize(1, 18.0f);
        this.countryButton.setPadding(AndroidUtilities.dp(6.0f), AndroidUtilities.dp(10.0f), AndroidUtilities.dp(6.0f), 0);
        this.countryButton.setTextColor(-14606047);
        this.countryButton.setMaxLines(1);
        this.countryButton.setSingleLine(true);
        this.countryButton.setEllipsize(TruncateAt.END);
        this.countryButton.setGravity(3);
        this.countryButton.setBackgroundResource(R.drawable.spinner_states);
        linearLayout.addView(this.countryButton, LayoutHelper.createLinear(-1, 36, 0.0f, 24.0f, 0.0f, 14.0f));
        this.countryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                CountrySelectActivity fragment = new CountrySelectActivity();
                fragment.setCountrySelectActivityDelegate(new CountrySelectActivityDelegate() {
                    public void didSelectCountry(String name) {
                        NewContactActivity.this.selectCountry(name);
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                AndroidUtilities.showKeyboard(NewContactActivity.this.phoneField);
                            }
                        }, 300);
                        NewContactActivity.this.phoneField.requestFocus();
                        NewContactActivity.this.phoneField.setSelection(NewContactActivity.this.phoneField.length());
                    }
                });
                NewContactActivity.this.presentFragment(fragment);
            }
        });
        linearLayout = new View(context);
        linearLayout.setPadding(AndroidUtilities.dp(8.0f), 0, AndroidUtilities.dp(8.0f), 0);
        linearLayout.setBackgroundColor(-2368549);
        linearLayout.addView(linearLayout, LayoutHelper.createLinear(-1, 1, 0.0f, -17.5f, 0.0f, 0.0f));
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        linearLayout.addView(linearLayout, LayoutHelper.createLinear(-1, -2, 0.0f, 20.0f, 0.0f, 0.0f));
        linearLayout = new TextView(context);
        linearLayout.setText("+");
        linearLayout.setTextColor(-14606047);
        linearLayout.setTextSize(1, 18.0f);
        linearLayout.addView(linearLayout, LayoutHelper.createLinear(-2, -2));
        this.codeField = new EditText(context);
        this.codeField.setInputType(3);
        this.codeField.setTextColor(-14606047);
        AndroidUtilities.clearCursorDrawable(this.codeField);
        this.codeField.setPadding(AndroidUtilities.dp(10.0f), 0, 0, 0);
        this.codeField.setTextSize(1, 18.0f);
        this.codeField.setMaxLines(1);
        this.codeField.setGravity(19);
        this.codeField.setImeOptions(268435461);
        this.codeField.setFilters(new InputFilter[]{new LengthFilter(5)});
        linearLayout.addView(this.codeField, LayoutHelper.createLinear(55, 36, -9.0f, 0.0f, 16.0f, 0.0f));
        this.codeField.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {
                if (!NewContactActivity.this.ignoreOnTextChange) {
                    NewContactActivity.this.ignoreOnTextChange = true;
                    String text = PhoneFormat.stripExceptNumbers(NewContactActivity.this.codeField.getText().toString());
                    NewContactActivity.this.codeField.setText(text);
                    if (text.length() == 0) {
                        NewContactActivity.this.countryButton.setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
                        NewContactActivity.this.phoneField.setHintText(null);
                        NewContactActivity.this.countryState = 1;
                    } else {
                        boolean ok = false;
                        String textToSet = null;
                        if (text.length() > 4) {
                            NewContactActivity.this.ignoreOnTextChange = true;
                            for (int a = 4; a >= 1; a--) {
                                String sub = text.substring(0, a);
                                if (((String) NewContactActivity.this.codesMap.get(sub)) != null) {
                                    ok = true;
                                    textToSet = text.substring(a, text.length()) + NewContactActivity.this.phoneField.getText().toString();
                                    text = sub;
                                    NewContactActivity.this.codeField.setText(sub);
                                    break;
                                }
                            }
                            if (!ok) {
                                NewContactActivity.this.ignoreOnTextChange = true;
                                textToSet = text.substring(1, text.length()) + NewContactActivity.this.phoneField.getText().toString();
                                EditText access$200 = NewContactActivity.this.codeField;
                                text = text.substring(0, 1);
                                access$200.setText(text);
                            }
                        }
                        String country = (String) NewContactActivity.this.codesMap.get(text);
                        if (country != null) {
                            int index = NewContactActivity.this.countriesArray.indexOf(country);
                            if (index != -1) {
                                NewContactActivity.this.ignoreSelection = true;
                                NewContactActivity.this.countryButton.setText((CharSequence) NewContactActivity.this.countriesArray.get(index));
                                String hint = (String) NewContactActivity.this.phoneFormatMap.get(text);
                                NewContactActivity.this.phoneField.setHintText(hint != null ? hint.replace('X', '–') : null);
                                NewContactActivity.this.countryState = 0;
                            } else {
                                NewContactActivity.this.countryButton.setText(LocaleController.getString("WrongCountry", R.string.WrongCountry));
                                NewContactActivity.this.phoneField.setHintText(null);
                                NewContactActivity.this.countryState = 2;
                            }
                        } else {
                            NewContactActivity.this.countryButton.setText(LocaleController.getString("WrongCountry", R.string.WrongCountry));
                            NewContactActivity.this.phoneField.setHintText(null);
                            NewContactActivity.this.countryState = 2;
                        }
                        if (!ok) {
                            NewContactActivity.this.codeField.setSelection(NewContactActivity.this.codeField.getText().length());
                        }
                        if (textToSet != null) {
                            NewContactActivity.this.phoneField.requestFocus();
                            NewContactActivity.this.phoneField.setText(textToSet);
                            NewContactActivity.this.phoneField.setSelection(NewContactActivity.this.phoneField.length());
                        }
                    }
                    NewContactActivity.this.ignoreOnTextChange = false;
                }
            }
        });
        this.codeField.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i != 5) {
                    return false;
                }
                NewContactActivity.this.phoneField.requestFocus();
                NewContactActivity.this.phoneField.setSelection(NewContactActivity.this.phoneField.length());
                return true;
            }
        });
        this.phoneField = new HintEditText(context);
        this.phoneField.setInputType(3);
        this.phoneField.setTextColor(-14606047);
        this.phoneField.setHintTextColor(Theme.SHARE_SHEET_EDIT_PLACEHOLDER_TEXT_COLOR);
        this.phoneField.setPadding(0, 0, 0, 0);
        AndroidUtilities.clearCursorDrawable(this.phoneField);
        this.phoneField.setTextSize(1, 18.0f);
        this.phoneField.setMaxLines(1);
        this.phoneField.setGravity(19);
        this.phoneField.setImeOptions(268435462);
        linearLayout.addView(this.phoneField, LayoutHelper.createFrame(-1, 36.0f));
        this.phoneField.addTextChangedListener(new TextWatcher() {
            private int actionPosition;
            private int characterAction = -1;

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count == 0 && after == 1) {
                    this.characterAction = 1;
                } else if (count != 1 || after != 0) {
                    this.characterAction = -1;
                } else if (s.charAt(start) != ' ' || start <= 0) {
                    this.characterAction = 2;
                } else {
                    this.characterAction = 3;
                    this.actionPosition = start - 1;
                }
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                if (!NewContactActivity.this.ignoreOnPhoneChange) {
                    int a;
                    int start = NewContactActivity.this.phoneField.getSelectionStart();
                    String phoneChars = "0123456789";
                    String str = NewContactActivity.this.phoneField.getText().toString();
                    if (this.characterAction == 3) {
                        str = str.substring(0, this.actionPosition) + str.substring(this.actionPosition + 1, str.length());
                        start--;
                    }
                    StringBuilder builder = new StringBuilder(str.length());
                    for (a = 0; a < str.length(); a++) {
                        String ch = str.substring(a, a + 1);
                        if (phoneChars.contains(ch)) {
                            builder.append(ch);
                        }
                    }
                    NewContactActivity.this.ignoreOnPhoneChange = true;
                    String hint = NewContactActivity.this.phoneField.getHintText();
                    if (hint != null) {
                        a = 0;
                        while (a < builder.length()) {
                            if (a < hint.length()) {
                                if (hint.charAt(a) == ' ') {
                                    builder.insert(a, ' ');
                                    a++;
                                    if (!(start != a || this.characterAction == 2 || this.characterAction == 3)) {
                                        start++;
                                    }
                                }
                                a++;
                            } else {
                                builder.insert(a, ' ');
                                if (!(start != a + 1 || this.characterAction == 2 || this.characterAction == 3)) {
                                    start++;
                                }
                            }
                        }
                    }
                    NewContactActivity.this.phoneField.setText(builder);
                    if (start >= 0) {
                        HintEditText access$300 = NewContactActivity.this.phoneField;
                        if (start > NewContactActivity.this.phoneField.length()) {
                            start = NewContactActivity.this.phoneField.length();
                        }
                        access$300.setSelection(start);
                    }
                    NewContactActivity.this.phoneField.onTextChange();
                    NewContactActivity.this.ignoreOnPhoneChange = false;
                }
            }
        });
        this.phoneField.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i != 6) {
                    return false;
                }
                NewContactActivity.this.editDoneItem.performClick();
                return true;
            }
        });
        HashMap<String, String> languageMap = new HashMap();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open("countries.txt")));
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                String[] args = line.split(";");
                this.countriesArray.add(0, args[2]);
                this.countriesMap.put(args[2], args[0]);
                this.codesMap.put(args[0], args[2]);
                if (args.length > 3) {
                    this.phoneFormatMap.put(args[0], args[3]);
                }
                languageMap.put(args[1], args[2]);
            }
            bufferedReader.close();
        } catch (Throwable e) {
            FileLog.e("tmessages", e);
        }
        Collections.sort(this.countriesArray, new Comparator<String>() {
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });
        String country = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService("phone");
            if (telephonyManager != null) {
                country = telephonyManager.getSimCountryIso().toUpperCase();
            }
        } catch (Throwable e2) {
            FileLog.e("tmessages", e2);
        }
        if (country != null) {
            String countryName = (String) languageMap.get(country);
            if (!(countryName == null || this.countriesArray.indexOf(countryName) == -1)) {
                this.codeField.setText((CharSequence) this.countriesMap.get(countryName));
                this.countryState = 0;
            }
        }
        if (this.codeField.length() == 0) {
            this.countryButton.setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
            this.phoneField.setHintText(null);
            this.countryState = 1;
        }
        return this.fragmentView;
    }

    public void onResume() {
        super.onResume();
        if (!ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("view_animations", true)) {
            this.firstNameField.requestFocus();
            AndroidUtilities.showKeyboard(this.firstNameField);
        }
    }

    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            this.firstNameField.requestFocus();
            AndroidUtilities.showKeyboard(this.firstNameField);
        }
    }

    public void selectCountry(String name) {
        if (this.countriesArray.indexOf(name) != -1) {
            this.ignoreOnTextChange = true;
            String code = (String) this.countriesMap.get(name);
            this.codeField.setText(code);
            this.countryButton.setText(name);
            String hint = (String) this.phoneFormatMap.get(code);
            this.phoneField.setHintText(hint != null ? hint.replace('X', '–') : null);
            this.countryState = 0;
            this.ignoreOnTextChange = false;
        }
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (this.ignoreSelection) {
            this.ignoreSelection = false;
            return;
        }
        this.ignoreOnTextChange = true;
        this.codeField.setText((CharSequence) this.countriesMap.get((String) this.countriesArray.get(i)));
        this.ignoreOnTextChange = false;
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void showEditDoneProgress(final boolean show, boolean animated) {
        if (this.editDoneItemAnimation != null) {
            this.editDoneItemAnimation.cancel();
        }
        if (animated) {
            this.editDoneItemAnimation = new AnimatorSet();
            AnimatorSet animatorSet;
            Animator[] animatorArr;
            if (show) {
                this.editDoneItemProgress.setVisibility(0);
                this.editDoneItem.setEnabled(false);
                animatorSet = this.editDoneItemAnimation;
                animatorArr = new Animator[6];
                animatorArr[0] = ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "scaleX", new float[]{0.1f});
                animatorArr[1] = ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "scaleY", new float[]{0.1f});
                animatorArr[2] = ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "alpha", new float[]{0.0f});
                animatorArr[3] = ObjectAnimator.ofFloat(this.editDoneItemProgress, "scaleX", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorArr[4] = ObjectAnimator.ofFloat(this.editDoneItemProgress, "scaleY", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorArr[5] = ObjectAnimator.ofFloat(this.editDoneItemProgress, "alpha", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorSet.playTogether(animatorArr);
            } else {
                this.editDoneItem.getImageView().setVisibility(0);
                this.editDoneItem.setEnabled(true);
                animatorSet = this.editDoneItemAnimation;
                animatorArr = new Animator[6];
                animatorArr[0] = ObjectAnimator.ofFloat(this.editDoneItemProgress, "scaleX", new float[]{0.1f});
                animatorArr[1] = ObjectAnimator.ofFloat(this.editDoneItemProgress, "scaleY", new float[]{0.1f});
                animatorArr[2] = ObjectAnimator.ofFloat(this.editDoneItemProgress, "alpha", new float[]{0.0f});
                animatorArr[3] = ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "scaleX", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorArr[4] = ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "scaleY", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorArr[5] = ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "alpha", new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT});
                animatorSet.playTogether(animatorArr);
            }
            this.editDoneItemAnimation.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (NewContactActivity.this.editDoneItemAnimation != null && NewContactActivity.this.editDoneItemAnimation.equals(animation)) {
                        if (show) {
                            NewContactActivity.this.editDoneItem.getImageView().setVisibility(4);
                        } else {
                            NewContactActivity.this.editDoneItemProgress.setVisibility(4);
                        }
                    }
                }

                public void onAnimationCancel(Animator animation) {
                    if (NewContactActivity.this.editDoneItemAnimation != null && NewContactActivity.this.editDoneItemAnimation.equals(animation)) {
                        NewContactActivity.this.editDoneItemAnimation = null;
                    }
                }
            });
            this.editDoneItemAnimation.setDuration(150);
            this.editDoneItemAnimation.start();
        } else if (show) {
            this.editDoneItem.getImageView().setScaleX(0.1f);
            this.editDoneItem.getImageView().setScaleY(0.1f);
            this.editDoneItem.getImageView().setAlpha(0.0f);
            this.editDoneItemProgress.setScaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            this.editDoneItemProgress.setScaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            this.editDoneItemProgress.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            this.editDoneItem.getImageView().setVisibility(4);
            this.editDoneItemProgress.setVisibility(0);
            this.editDoneItem.setEnabled(false);
        } else {
            this.editDoneItemProgress.setScaleX(0.1f);
            this.editDoneItemProgress.setScaleY(0.1f);
            this.editDoneItemProgress.setAlpha(0.0f);
            this.editDoneItem.getImageView().setScaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            this.editDoneItem.getImageView().setScaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            this.editDoneItem.getImageView().setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            this.editDoneItem.getImageView().setVisibility(0);
            this.editDoneItemProgress.setVisibility(4);
            this.editDoneItem.setEnabled(true);
        }
    }

    private void needShowAlert(String title, String text) {
        if (text != null && getParentActivity() != null) {
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(title);
            builder.setMessage(text);
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            showDialog(builder.create());
        }
    }
}
