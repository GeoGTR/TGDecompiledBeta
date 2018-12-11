package org.telegram.p005ui.Components;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.CLASSNAMER;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.MessagesStorage.IntCallback;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.SecretChatHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.p005ui.ActionBar.AlertDialog;
import org.telegram.p005ui.ActionBar.AlertDialog.Builder;
import org.telegram.p005ui.ActionBar.BaseFragment;
import org.telegram.p005ui.ActionBar.BottomSheet;
import org.telegram.p005ui.ActionBar.Theme;
import org.telegram.p005ui.Cells.AccountSelectCell;
import org.telegram.p005ui.Cells.RadioColorCell;
import org.telegram.p005ui.Cells.TextColorCell;
import org.telegram.p005ui.LaunchActivity;
import org.telegram.p005ui.ProfileNotificationsActivity;
import org.telegram.p005ui.ReportOtherActivity;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.InputPeer;
import org.telegram.tgnet.TLRPC.TL_account_changePhone;
import org.telegram.tgnet.TLRPC.TL_account_confirmPhone;
import org.telegram.tgnet.TLRPC.TL_account_getAuthorizationForm;
import org.telegram.tgnet.TLRPC.TL_account_getPassword;
import org.telegram.tgnet.TLRPC.TL_account_getTmpPassword;
import org.telegram.tgnet.TLRPC.TL_account_reportPeer;
import org.telegram.tgnet.TLRPC.TL_account_saveSecureValue;
import org.telegram.tgnet.TLRPC.TL_account_sendChangePhoneCode;
import org.telegram.tgnet.TLRPC.TL_account_sendConfirmPhoneCode;
import org.telegram.tgnet.TLRPC.TL_account_verifyEmail;
import org.telegram.tgnet.TLRPC.TL_account_verifyPhone;
import org.telegram.tgnet.TLRPC.TL_auth_resendCode;
import org.telegram.tgnet.TLRPC.TL_channels_createChannel;
import org.telegram.tgnet.TLRPC.TL_channels_editAdmin;
import org.telegram.tgnet.TLRPC.TL_channels_editBanned;
import org.telegram.tgnet.TLRPC.TL_channels_inviteToChannel;
import org.telegram.tgnet.TLRPC.TL_channels_joinChannel;
import org.telegram.tgnet.TLRPC.TL_contacts_importContacts;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputReportReasonPornography;
import org.telegram.tgnet.TLRPC.TL_inputReportReasonSpam;
import org.telegram.tgnet.TLRPC.TL_inputReportReasonViolence;
import org.telegram.tgnet.TLRPC.TL_messages_addChatUser;
import org.telegram.tgnet.TLRPC.TL_messages_createChat;
import org.telegram.tgnet.TLRPC.TL_messages_editMessage;
import org.telegram.tgnet.TLRPC.TL_messages_forwardMessages;
import org.telegram.tgnet.TLRPC.TL_messages_getAttachedStickers;
import org.telegram.tgnet.TLRPC.TL_messages_importChatInvite;
import org.telegram.tgnet.TLRPC.TL_messages_report;
import org.telegram.tgnet.TLRPC.TL_messages_sendBroadcast;
import org.telegram.tgnet.TLRPC.TL_messages_sendInlineBotResult;
import org.telegram.tgnet.TLRPC.TL_messages_sendMedia;
import org.telegram.tgnet.TLRPC.TL_messages_sendMessage;
import org.telegram.tgnet.TLRPC.TL_messages_startBot;
import org.telegram.tgnet.TLRPC.TL_payments_sendPaymentForm;
import org.telegram.tgnet.TLRPC.TL_payments_validateRequestedInfo;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettings;
import org.telegram.tgnet.TLRPC.TL_updateUserName;
import org.telegram.tgnet.TLRPC.User;

/* renamed from: org.telegram.ui.Components.AlertsCreator */
public class AlertsCreator {

    /* renamed from: org.telegram.ui.Components.AlertsCreator$AccountSelectDelegate */
    public interface AccountSelectDelegate {
        void didSelectAccount(int i);
    }

    /* renamed from: org.telegram.ui.Components.AlertsCreator$DatePickerDelegate */
    public interface DatePickerDelegate {
        void didSelectDate(int i, int i2, int i3);
    }

    /* renamed from: org.telegram.ui.Components.AlertsCreator$PaymentAlertDelegate */
    public interface PaymentAlertDelegate {
        void didPressedNewCard();
    }

    /* JADX WARNING: Missing block: B:183:0x049d, code:
            if (r3.equals("USERNAME_INVALID") != false) goto L_0x0487;
     */
    /* JADX WARNING: Missing block: B:212:0x0551, code:
            if (r3.equals("BOT_PRECHECKOUT_FAILED") != false) goto L_0x0540;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Dialog processError(int currentAccount, TL_error error, BaseFragment fragment, TLObject request, Object... args) {
        boolean z = false;
        if (error.code == 406 || error.text == null) {
            return null;
        }
        if (!(request instanceof TL_account_saveSecureValue) && !(request instanceof TL_account_getAuthorizationForm)) {
            if (!(request instanceof TL_channels_joinChannel) && !(request instanceof TL_channels_editAdmin) && !(request instanceof TL_channels_inviteToChannel) && !(request instanceof TL_messages_addChatUser) && !(request instanceof TL_messages_startBot) && !(request instanceof TL_channels_editBanned)) {
                if (!(request instanceof TL_messages_createChat)) {
                    if (!(request instanceof TL_channels_createChannel)) {
                        if (!(request instanceof TL_messages_editMessage)) {
                            if (!(request instanceof TL_messages_sendMessage) && !(request instanceof TL_messages_sendMedia) && !(request instanceof TL_messages_sendBroadcast) && !(request instanceof TL_messages_sendInlineBotResult) && !(request instanceof TL_messages_forwardMessages)) {
                                if (!(request instanceof TL_messages_importChatInvite)) {
                                    if (!(request instanceof TL_messages_getAttachedStickers)) {
                                        if (!(request instanceof TL_account_confirmPhone) && !(request instanceof TL_account_verifyPhone) && !(request instanceof TL_account_verifyEmail)) {
                                            if (!(request instanceof TL_auth_resendCode)) {
                                                if (!(request instanceof TL_account_sendConfirmPhoneCode)) {
                                                    if (!(request instanceof TL_account_changePhone)) {
                                                        if (!(request instanceof TL_account_sendChangePhoneCode)) {
                                                            String str;
                                                            if (!(request instanceof TL_updateUserName)) {
                                                                if (!(request instanceof TL_contacts_importContacts)) {
                                                                    if (!(request instanceof TL_account_getPassword) && !(request instanceof TL_account_getTmpPassword)) {
                                                                        if (!(request instanceof TL_payments_sendPaymentForm)) {
                                                                            if (request instanceof TL_payments_validateRequestedInfo) {
                                                                                String str2 = error.text;
                                                                                boolean z2 = true;
                                                                                switch (str2.hashCode()) {
                                                                                    case 1758025548:
                                                                                        if (str2.equals("SHIPPING_NOT_AVAILABLE")) {
                                                                                            z2 = false;
                                                                                            break;
                                                                                        }
                                                                                        break;
                                                                                }
                                                                                switch (z2) {
                                                                                    case false:
                                                                                        AlertsCreator.showSimpleToast(fragment, LocaleController.getString("PaymentNoShippingMethod", CLASSNAMER.string.PaymentNoShippingMethod));
                                                                                        break;
                                                                                    default:
                                                                                        AlertsCreator.showSimpleToast(fragment, error.text);
                                                                                        break;
                                                                                }
                                                                            }
                                                                        }
                                                                        str = error.text;
                                                                        switch (str.hashCode()) {
                                                                            case -1144062453:
                                                                                break;
                                                                            case -784238410:
                                                                                if (str.equals("PAYMENT_FAILED")) {
                                                                                    z = true;
                                                                                    break;
                                                                                }
                                                                            default:
                                                                                z = true;
                                                                                break;
                                                                        }
                                                                        switch (z) {
                                                                            case false:
                                                                                AlertsCreator.showSimpleToast(fragment, LocaleController.getString("PaymentPrecheckoutFailed", CLASSNAMER.string.PaymentPrecheckoutFailed));
                                                                                break;
                                                                            case true:
                                                                                AlertsCreator.showSimpleToast(fragment, LocaleController.getString("PaymentFailed", CLASSNAMER.string.PaymentFailed));
                                                                                break;
                                                                            default:
                                                                                AlertsCreator.showSimpleToast(fragment, error.text);
                                                                                break;
                                                                        }
                                                                    } else if (error.text.startsWith("FLOOD_WAIT")) {
                                                                        AlertsCreator.showSimpleToast(fragment, AlertsCreator.getFloodWaitString(error.text));
                                                                    } else {
                                                                        AlertsCreator.showSimpleToast(fragment, error.text);
                                                                    }
                                                                } else if (error == null || error.text.startsWith("FLOOD_WAIT")) {
                                                                    AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("FloodWait", CLASSNAMER.string.FloodWait));
                                                                } else {
                                                                    AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("ErrorOccurred", CLASSNAMER.string.ErrorOccurred) + "\n" + error.text);
                                                                }
                                                            } else {
                                                                str = error.text;
                                                                switch (str.hashCode()) {
                                                                    case 288843630:
                                                                        break;
                                                                    case 533175271:
                                                                        if (str.equals("USERNAME_OCCUPIED")) {
                                                                            z = true;
                                                                            break;
                                                                        }
                                                                    default:
                                                                        z = true;
                                                                        break;
                                                                }
                                                                switch (z) {
                                                                    case false:
                                                                        AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("UsernameInvalid", CLASSNAMER.string.UsernameInvalid));
                                                                        break;
                                                                    case true:
                                                                        AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("UsernameInUse", CLASSNAMER.string.UsernameInUse));
                                                                        break;
                                                                    default:
                                                                        AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("ErrorOccurred", CLASSNAMER.string.ErrorOccurred));
                                                                        break;
                                                                }
                                                            }
                                                        } else if (error.text.contains("PHONE_NUMBER_INVALID")) {
                                                            AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("InvalidPhoneNumber", CLASSNAMER.string.InvalidPhoneNumber));
                                                        } else if (error.text.contains("PHONE_CODE_EMPTY") || error.text.contains("PHONE_CODE_INVALID")) {
                                                            AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("InvalidCode", CLASSNAMER.string.InvalidCode));
                                                        } else if (error.text.contains("PHONE_CODE_EXPIRED")) {
                                                            AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("CodeExpired", CLASSNAMER.string.CodeExpired));
                                                        } else if (error.text.startsWith("FLOOD_WAIT")) {
                                                            AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("FloodWait", CLASSNAMER.string.FloodWait));
                                                        } else if (error.text.startsWith("PHONE_NUMBER_OCCUPIED")) {
                                                            AlertsCreator.showSimpleAlert(fragment, LocaleController.formatString("ChangePhoneNumberOccupied", CLASSNAMER.string.ChangePhoneNumberOccupied, (String) args[0]));
                                                        } else {
                                                            AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("ErrorOccurred", CLASSNAMER.string.ErrorOccurred));
                                                        }
                                                    } else if (error.text.contains("PHONE_NUMBER_INVALID")) {
                                                        AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("InvalidPhoneNumber", CLASSNAMER.string.InvalidPhoneNumber));
                                                    } else if (error.text.contains("PHONE_CODE_EMPTY") || error.text.contains("PHONE_CODE_INVALID")) {
                                                        AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("InvalidCode", CLASSNAMER.string.InvalidCode));
                                                    } else if (error.text.contains("PHONE_CODE_EXPIRED")) {
                                                        AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("CodeExpired", CLASSNAMER.string.CodeExpired));
                                                    } else if (error.text.startsWith("FLOOD_WAIT")) {
                                                        AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("FloodWait", CLASSNAMER.string.FloodWait));
                                                    } else {
                                                        AlertsCreator.showSimpleAlert(fragment, error.text);
                                                    }
                                                } else if (error.code == 400) {
                                                    return AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("CancelLinkExpired", CLASSNAMER.string.CancelLinkExpired));
                                                } else {
                                                    if (error.text != null) {
                                                        if (error.text.startsWith("FLOOD_WAIT")) {
                                                            return AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("FloodWait", CLASSNAMER.string.FloodWait));
                                                        }
                                                        return AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("ErrorOccurred", CLASSNAMER.string.ErrorOccurred));
                                                    }
                                                }
                                            } else if (error.text.contains("PHONE_NUMBER_INVALID")) {
                                                return AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("InvalidPhoneNumber", CLASSNAMER.string.InvalidPhoneNumber));
                                            } else {
                                                if (error.text.contains("PHONE_CODE_EMPTY") || error.text.contains("PHONE_CODE_INVALID")) {
                                                    return AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("InvalidCode", CLASSNAMER.string.InvalidCode));
                                                }
                                                if (error.text.contains("PHONE_CODE_EXPIRED")) {
                                                    return AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("CodeExpired", CLASSNAMER.string.CodeExpired));
                                                }
                                                if (error.text.startsWith("FLOOD_WAIT")) {
                                                    return AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("FloodWait", CLASSNAMER.string.FloodWait));
                                                }
                                                if (error.code != -1000) {
                                                    return AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("ErrorOccurred", CLASSNAMER.string.ErrorOccurred) + "\n" + error.text);
                                                }
                                            }
                                        } else if (error.text.contains("PHONE_CODE_EMPTY") || error.text.contains("PHONE_CODE_INVALID") || error.text.contains("CODE_INVALID") || error.text.contains("CODE_EMPTY")) {
                                            return AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("InvalidCode", CLASSNAMER.string.InvalidCode));
                                        } else {
                                            if (error.text.contains("PHONE_CODE_EXPIRED") || error.text.contains("EMAIL_VERIFY_EXPIRED")) {
                                                return AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("CodeExpired", CLASSNAMER.string.CodeExpired));
                                            }
                                            if (error.text.startsWith("FLOOD_WAIT")) {
                                                return AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("FloodWait", CLASSNAMER.string.FloodWait));
                                            }
                                            return AlertsCreator.showSimpleAlert(fragment, error.text);
                                        }
                                    } else if (!(fragment == null || fragment.getParentActivity() == null)) {
                                        Toast.makeText(fragment.getParentActivity(), LocaleController.getString("ErrorOccurred", CLASSNAMER.string.ErrorOccurred) + "\n" + error.text, 0).show();
                                    }
                                } else if (error.text.startsWith("FLOOD_WAIT")) {
                                    AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("FloodWait", CLASSNAMER.string.FloodWait));
                                } else if (error.text.equals("USERS_TOO_MUCH")) {
                                    AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("JoinToGroupErrorFull", CLASSNAMER.string.JoinToGroupErrorFull));
                                } else {
                                    AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("JoinToGroupErrorNotExist", CLASSNAMER.string.JoinToGroupErrorNotExist));
                                }
                            } else if (error.text.equals("PEER_FLOOD")) {
                                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.needShowAlert, Integer.valueOf(0));
                            }
                        } else if (!error.text.equals("MESSAGE_NOT_MODIFIED")) {
                            if (fragment != null) {
                                AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("EditMessageError", CLASSNAMER.string.EditMessageError));
                            } else {
                                AlertsCreator.showSimpleToast(fragment, LocaleController.getString("EditMessageError", CLASSNAMER.string.EditMessageError));
                            }
                        }
                    } else if (error.text.startsWith("FLOOD_WAIT")) {
                        AlertsCreator.showFloodWaitAlert(error.text, fragment);
                    } else {
                        AlertsCreator.showAddUserAlert(error.text, fragment, false);
                    }
                } else if (error.text.startsWith("FLOOD_WAIT")) {
                    AlertsCreator.showFloodWaitAlert(error.text, fragment);
                } else {
                    AlertsCreator.showAddUserAlert(error.text, fragment, false);
                }
            } else if (fragment != null) {
                AlertsCreator.showAddUserAlert(error.text, fragment, ((Boolean) args[0]).booleanValue());
            } else if (error.text.equals("PEER_FLOOD")) {
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.needShowAlert, Integer.valueOf(1));
            }
        } else if (error.text.contains("PHONE_NUMBER_INVALID")) {
            AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("InvalidPhoneNumber", CLASSNAMER.string.InvalidPhoneNumber));
        } else if (error.text.startsWith("FLOOD_WAIT")) {
            AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("FloodWait", CLASSNAMER.string.FloodWait));
        } else if ("APP_VERSION_OUTDATED".equals(error.text)) {
            AlertsCreator.showUpdateAppAlert(fragment.getParentActivity(), LocaleController.getString("UpdateAppAlert", CLASSNAMER.string.UpdateAppAlert), true);
        } else {
            AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("ErrorOccurred", CLASSNAMER.string.ErrorOccurred) + "\n" + error.text);
        }
        return null;
    }

    public static Toast showSimpleToast(BaseFragment baseFragment, String text) {
        if (text == null) {
            return null;
        }
        Context context;
        if (baseFragment == null || baseFragment.getParentActivity() == null) {
            context = ApplicationLoader.applicationContext;
        } else {
            context = baseFragment.getParentActivity();
        }
        Toast toast = Toast.makeText(context, text, 1);
        toast.show();
        return toast;
    }

    public static AlertDialog showUpdateAppAlert(Context context, String text, boolean updateApp) {
        if (context == null || text == null) {
            return null;
        }
        Builder builder = new Builder(context);
        builder.setTitle(LocaleController.getString("AppName", CLASSNAMER.string.AppName));
        builder.setMessage(text);
        builder.setPositiveButton(LocaleController.getString("OK", CLASSNAMER.string.OK), null);
        if (updateApp) {
            builder.setNegativeButton(LocaleController.getString("UpdateApp", CLASSNAMER.string.UpdateApp), new AlertsCreator$$Lambda$0(context));
        }
        return builder.show();
    }

    public static Builder createSimpleAlert(Context context, String text) {
        if (text == null) {
            return null;
        }
        Builder builder = new Builder(context);
        builder.setTitle(LocaleController.getString("AppName", CLASSNAMER.string.AppName));
        builder.setMessage(text);
        builder.setPositiveButton(LocaleController.getString("OK", CLASSNAMER.string.OK), null);
        return builder;
    }

    public static Dialog showSimpleAlert(BaseFragment baseFragment, String text) {
        if (text == null || baseFragment == null || baseFragment.getParentActivity() == null) {
            return null;
        }
        Dialog dialog = AlertsCreator.createSimpleAlert(baseFragment.getParentActivity(), text).create();
        baseFragment.showDialog(dialog);
        return dialog;
    }

    public static void showCustomNotificationsDialog(BaseFragment parentFragment, long did, int currentAccount, IntCallback callback) {
        if (parentFragment != null && parentFragment.getParentActivity() != null) {
            boolean defaultEnabled;
            if (((int) did) < 0) {
                defaultEnabled = MessagesController.getNotificationsSettings(currentAccount).getBoolean("EnableGroup", true);
            } else {
                defaultEnabled = MessagesController.getNotificationsSettings(currentAccount).getBoolean("EnableAll", true);
            }
            String[] descriptions = new String[6];
            descriptions[0] = defaultEnabled ? LocaleController.getString("NotificationsDefaultOn", CLASSNAMER.string.NotificationsDefaultOn) : LocaleController.getString("NotificationsDefaultOff", CLASSNAMER.string.NotificationsDefaultOff);
            descriptions[1] = LocaleController.getString("NotificationsTurnOn", CLASSNAMER.string.NotificationsTurnOn);
            descriptions[2] = LocaleController.formatString("MuteFor", CLASSNAMER.string.MuteFor, LocaleController.formatPluralString("Hours", 1));
            descriptions[3] = LocaleController.formatString("MuteFor", CLASSNAMER.string.MuteFor, LocaleController.formatPluralString("Days", 2));
            descriptions[4] = LocaleController.getString("NotificationsCustomize", CLASSNAMER.string.NotificationsCustomize);
            descriptions[5] = LocaleController.getString("NotificationsTurnOff", CLASSNAMER.string.NotificationsTurnOff);
            int[] icons = new int[6];
            icons[0] = defaultEnabled ? CLASSNAMER.drawable.notifications_s_on : CLASSNAMER.drawable.notifications_s_off;
            icons[1] = CLASSNAMER.drawable.notifications_s_on;
            icons[2] = CLASSNAMER.drawable.notifications_s_1h;
            icons[3] = CLASSNAMER.drawable.notifications_s_2d;
            icons[4] = CLASSNAMER.drawable.notifications_s_custom;
            icons[5] = CLASSNAMER.drawable.notifications_s_off;
            LinearLayout linearLayout = new LinearLayout(parentFragment.getParentActivity());
            linearLayout.setOrientation(1);
            for (int a = 0; a < descriptions.length; a++) {
                TextView textView = new TextView(parentFragment.getParentActivity());
                textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                textView.setTextSize(1, 16.0f);
                textView.setLines(1);
                textView.setMaxLines(1);
                Drawable drawable = parentFragment.getParentActivity().getResources().getDrawable(icons[a]);
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogIcon), Mode.MULTIPLY));
                textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                textView.setTag(Integer.valueOf(a));
                textView.setBackgroundDrawable(Theme.getSelectorDrawable(false));
                textView.setPadding(AndroidUtilities.m10dp(24.0f), 0, AndroidUtilities.m10dp(24.0f), 0);
                textView.setSingleLine(true);
                textView.setGravity(19);
                textView.setCompoundDrawablePadding(AndroidUtilities.m10dp(26.0f));
                textView.setText(descriptions[a]);
                linearLayout.addView(textView, LayoutHelper.createLinear(-1, 48, 51));
                textView.setOnClickListener(new AlertsCreator$$Lambda$1(currentAccount, did, parentFragment, callback));
            }
            Builder builder = new Builder(parentFragment.getParentActivity());
            builder.setTitle(LocaleController.getString("Notifications", CLASSNAMER.string.Notifications));
            builder.setView(linearLayout);
            parentFragment.showDialog(builder.create());
        }
    }

    static final /* synthetic */ void lambda$showCustomNotificationsDialog$1$AlertsCreator(int currentAccount, long did, BaseFragment parentFragment, IntCallback callback, View v) {
        int i = ((Integer) v.getTag()).intValue();
        Editor editor;
        TL_dialog dialog;
        if (i == 0 || i == 1) {
            editor = MessagesController.getNotificationsSettings(currentAccount).edit();
            if (i == 0) {
                editor.remove("notify2_" + did);
            } else {
                editor.putInt("notify2_" + did, 0);
            }
            MessagesStorage.getInstance(currentAccount).setDialogFlags(did, 0);
            editor.commit();
            dialog = (TL_dialog) MessagesController.getInstance(currentAccount).dialogs_dict.get(did);
            if (dialog != null) {
                dialog.notify_settings = new TL_peerNotifySettings();
            }
            NotificationsController.getInstance(currentAccount).updateServerNotificationsSettings(did);
        } else if (i == 4) {
            Bundle args = new Bundle();
            args.putLong("dialog_id", did);
            parentFragment.presentFragment(new ProfileNotificationsActivity(args));
        } else {
            long flags;
            int untilTime = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
            if (i == 2) {
                untilTime += 3600;
            } else if (i == 3) {
                untilTime += 172800;
            } else if (i == 5) {
                untilTime = ConnectionsManager.DEFAULT_DATACENTER_ID;
            }
            editor = MessagesController.getNotificationsSettings(currentAccount).edit();
            if (i == 5) {
                editor.putInt("notify2_" + did, 2);
                flags = 1;
            } else {
                editor.putInt("notify2_" + did, 3);
                editor.putInt("notifyuntil_" + did, untilTime);
                flags = (((long) untilTime) << 32) | 1;
            }
            NotificationsController.getInstance(currentAccount).removeNotificationsForDialog(did);
            MessagesStorage.getInstance(currentAccount).setDialogFlags(did, flags);
            editor.commit();
            dialog = (TL_dialog) MessagesController.getInstance(currentAccount).dialogs_dict.get(did);
            if (dialog != null) {
                dialog.notify_settings = new TL_peerNotifySettings();
                dialog.notify_settings.mute_until = untilTime;
            }
            NotificationsController.getInstance(currentAccount).updateServerNotificationsSettings(did);
        }
        if (callback != null) {
            callback.lambda$null$79$MessagesStorage(i);
        }
        parentFragment.dismissCurrentDialig();
    }

    public static AlertDialog showSecretLocationAlert(Context context, int currentAccount, Runnable onSelectRunnable, boolean inChat) {
        ArrayList<String> arrayList = new ArrayList();
        int providers = MessagesController.getInstance(currentAccount).availableMapProviders;
        if ((providers & 1) != 0) {
            arrayList.add(LocaleController.getString("MapPreviewProviderTelegram", CLASSNAMER.string.MapPreviewProviderTelegram));
        }
        if ((providers & 2) != 0) {
            arrayList.add(LocaleController.getString("MapPreviewProviderGoogle", CLASSNAMER.string.MapPreviewProviderGoogle));
        }
        if ((providers & 4) != 0) {
            arrayList.add(LocaleController.getString("MapPreviewProviderYandex", CLASSNAMER.string.MapPreviewProviderYandex));
        }
        arrayList.add(LocaleController.getString("MapPreviewProviderNobody", CLASSNAMER.string.MapPreviewProviderNobody));
        Builder builder = new Builder(context).setTitle(LocaleController.getString("ChooseMapPreviewProvider", CLASSNAMER.string.ChooseMapPreviewProvider)).setItems((CharSequence[]) arrayList.toArray(new String[arrayList.size()]), new AlertsCreator$$Lambda$2(onSelectRunnable));
        if (!inChat) {
            builder.setNegativeButton(LocaleController.getString("Cancel", CLASSNAMER.string.Cancel), null);
        }
        AlertDialog dialog = builder.show();
        if (inChat) {
            dialog.setCanceledOnTouchOutside(false);
        }
        return dialog;
    }

    static final /* synthetic */ void lambda$showSecretLocationAlert$2$AlertsCreator(Runnable onSelectRunnable, DialogInterface dialog, int which) {
        SharedConfig.setSecretMapPreviewType(which);
        if (onSelectRunnable != null) {
            onSelectRunnable.run();
        }
    }

    private static void updateDayPicker(NumberPicker dayPicker, NumberPicker monthPicker, NumberPicker yearPicker) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2, monthPicker.getValue());
        calendar.set(1, yearPicker.getValue());
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(calendar.getActualMaximum(5));
    }

    private static void checkPickerDate(NumberPicker dayPicker, NumberPicker monthPicker, NumberPicker yearPicker) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentYear = calendar.get(1);
        int currentMonth = calendar.get(2);
        int currentDay = calendar.get(5);
        if (currentYear > yearPicker.getValue()) {
            yearPicker.setValue(currentYear);
        }
        if (yearPicker.getValue() == currentYear) {
            if (currentMonth > monthPicker.getValue()) {
                monthPicker.setValue(currentMonth);
            }
            if (currentMonth == monthPicker.getValue() && currentDay > dayPicker.getValue()) {
                dayPicker.setValue(currentDay);
            }
        }
    }

    public static Builder createDatePickerDialog(Context context, int minYear, int maxYear, int currentYearDiff, int selectedDay, int selectedMonth, int selectedYear, String title, boolean checkMinDate, DatePickerDelegate datePickerDelegate) {
        if (context == null) {
            return null;
        }
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        linearLayout.setWeightSum(1.0f);
        NumberPicker monthPicker = new NumberPicker(context);
        NumberPicker dayPicker = new NumberPicker(context);
        NumberPicker yearPicker = new NumberPicker(context);
        linearLayout.addView(dayPicker, LayoutHelper.createLinear(0, -2, 0.3f));
        dayPicker.setOnScrollListener(new AlertsCreator$$Lambda$3(checkMinDate, dayPicker, monthPicker, yearPicker));
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        linearLayout.addView(monthPicker, LayoutHelper.createLinear(0, -2, 0.3f));
        monthPicker.setFormatter(AlertsCreator$$Lambda$4.$instance);
        monthPicker.setOnValueChangedListener(new AlertsCreator$$Lambda$5(dayPicker, monthPicker, yearPicker));
        monthPicker.setOnScrollListener(new AlertsCreator$$Lambda$6(checkMinDate, dayPicker, monthPicker, yearPicker));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentYear = calendar.get(1);
        yearPicker.setMinValue(currentYear + minYear);
        yearPicker.setMaxValue(currentYear + maxYear);
        yearPicker.setValue(currentYear + currentYearDiff);
        linearLayout.addView(yearPicker, LayoutHelper.createLinear(0, -2, 0.4f));
        yearPicker.setOnValueChangedListener(new AlertsCreator$$Lambda$7(dayPicker, monthPicker, yearPicker));
        yearPicker.setOnScrollListener(new AlertsCreator$$Lambda$8(checkMinDate, dayPicker, monthPicker, yearPicker));
        AlertsCreator.updateDayPicker(dayPicker, monthPicker, yearPicker);
        if (checkMinDate) {
            AlertsCreator.checkPickerDate(dayPicker, monthPicker, yearPicker);
        }
        if (selectedDay != -1) {
            dayPicker.setValue(selectedDay);
            monthPicker.setValue(selectedMonth);
            yearPicker.setValue(selectedYear);
        }
        Builder builder = new Builder(context);
        builder.setTitle(title);
        builder.setView(linearLayout);
        builder.setPositiveButton(LocaleController.getString("Set", CLASSNAMER.string.Set), new AlertsCreator$$Lambda$9(checkMinDate, dayPicker, monthPicker, yearPicker, datePickerDelegate));
        builder.setNegativeButton(LocaleController.getString("Cancel", CLASSNAMER.string.Cancel), null);
        return builder;
    }

    static final /* synthetic */ void lambda$createDatePickerDialog$3$AlertsCreator(boolean checkMinDate, NumberPicker dayPicker, NumberPicker monthPicker, NumberPicker yearPicker, NumberPicker view, int scrollState) {
        if (checkMinDate && scrollState == 0) {
            AlertsCreator.checkPickerDate(dayPicker, monthPicker, yearPicker);
        }
    }

    static final /* synthetic */ String lambda$createDatePickerDialog$4$AlertsCreator(int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(5, 1);
        calendar.set(2, value);
        return calendar.getDisplayName(2, 1, Locale.getDefault());
    }

    static final /* synthetic */ void lambda$createDatePickerDialog$6$AlertsCreator(boolean checkMinDate, NumberPicker dayPicker, NumberPicker monthPicker, NumberPicker yearPicker, NumberPicker view, int scrollState) {
        if (checkMinDate && scrollState == 0) {
            AlertsCreator.checkPickerDate(dayPicker, monthPicker, yearPicker);
        }
    }

    static final /* synthetic */ void lambda$createDatePickerDialog$8$AlertsCreator(boolean checkMinDate, NumberPicker dayPicker, NumberPicker monthPicker, NumberPicker yearPicker, NumberPicker view, int scrollState) {
        if (checkMinDate && scrollState == 0) {
            AlertsCreator.checkPickerDate(dayPicker, monthPicker, yearPicker);
        }
    }

    static final /* synthetic */ void lambda$createDatePickerDialog$9$AlertsCreator(boolean checkMinDate, NumberPicker dayPicker, NumberPicker monthPicker, NumberPicker yearPicker, DatePickerDelegate datePickerDelegate, DialogInterface dialog, int which) {
        if (checkMinDate) {
            AlertsCreator.checkPickerDate(dayPicker, monthPicker, yearPicker);
        }
        datePickerDelegate.didSelectDate(yearPicker.getValue(), monthPicker.getValue(), dayPicker.getValue());
    }

    public static Dialog createMuteAlert(Context context, long dialog_id) {
        if (context == null) {
            return null;
        }
        BottomSheet.Builder builder = new BottomSheet.Builder(context);
        builder.setTitle(LocaleController.getString("Notifications", CLASSNAMER.string.Notifications));
        CharSequence[] items = new CharSequence[4];
        items[0] = LocaleController.formatString("MuteFor", CLASSNAMER.string.MuteFor, LocaleController.formatPluralString("Hours", 1));
        items[1] = LocaleController.formatString("MuteFor", CLASSNAMER.string.MuteFor, LocaleController.formatPluralString("Hours", 8));
        items[2] = LocaleController.formatString("MuteFor", CLASSNAMER.string.MuteFor, LocaleController.formatPluralString("Days", 2));
        items[3] = LocaleController.getString("MuteDisable", CLASSNAMER.string.MuteDisable);
        builder.setItems(items, new AlertsCreator$$Lambda$10(dialog_id));
        return builder.create();
    }

    static final /* synthetic */ void lambda$createMuteAlert$10$AlertsCreator(long dialog_id, DialogInterface dialogInterface, int i) {
        long flags;
        int untilTime = ConnectionsManager.getInstance(UserConfig.selectedAccount).getCurrentTime();
        if (i == 0) {
            untilTime += 3600;
        } else if (i == 1) {
            untilTime += 28800;
        } else if (i == 2) {
            untilTime += 172800;
        } else if (i == 3) {
            untilTime = ConnectionsManager.DEFAULT_DATACENTER_ID;
        }
        Editor editor = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
        if (i == 3) {
            editor.putInt("notify2_" + dialog_id, 2);
            flags = 1;
        } else {
            editor.putInt("notify2_" + dialog_id, 3);
            editor.putInt("notifyuntil_" + dialog_id, untilTime);
            flags = (((long) untilTime) << 32) | 1;
        }
        NotificationsController.getInstance(UserConfig.selectedAccount).removeNotificationsForDialog(dialog_id);
        MessagesStorage.getInstance(UserConfig.selectedAccount).setDialogFlags(dialog_id, flags);
        editor.commit();
        TL_dialog dialog = (TL_dialog) MessagesController.getInstance(UserConfig.selectedAccount).dialogs_dict.get(dialog_id);
        if (dialog != null) {
            dialog.notify_settings = new TL_peerNotifySettings();
            dialog.notify_settings.mute_until = untilTime;
        }
        NotificationsController.getInstance(UserConfig.selectedAccount).updateServerNotificationsSettings(dialog_id);
    }

    public static Dialog createReportAlert(Context context, long dialog_id, int messageId, BaseFragment parentFragment) {
        if (context == null || parentFragment == null) {
            return null;
        }
        BottomSheet.Builder builder = new BottomSheet.Builder(context);
        builder.setTitle(LocaleController.getString("ReportChat", CLASSNAMER.string.ReportChat));
        builder.setItems(new CharSequence[]{LocaleController.getString("ReportChatSpam", CLASSNAMER.string.ReportChatSpam), LocaleController.getString("ReportChatViolence", CLASSNAMER.string.ReportChatViolence), LocaleController.getString("ReportChatPornography", CLASSNAMER.string.ReportChatPornography), LocaleController.getString("ReportChatOther", CLASSNAMER.string.ReportChatOther)}, new AlertsCreator$$Lambda$11(dialog_id, messageId, parentFragment, context));
        return builder.create();
    }

    static final /* synthetic */ void lambda$createReportAlert$12$AlertsCreator(long dialog_id, int messageId, BaseFragment parentFragment, Context context, DialogInterface dialogInterface, int i) {
        if (i == 3) {
            Bundle args = new Bundle();
            args.putLong("dialog_id", dialog_id);
            args.putLong("message_id", (long) messageId);
            parentFragment.presentFragment(new ReportOtherActivity(args));
            return;
        }
        TLObject req;
        InputPeer peer = MessagesController.getInstance(UserConfig.selectedAccount).getInputPeer((int) dialog_id);
        TLObject request;
        if (messageId != 0) {
            request = new TL_messages_report();
            request.peer = peer;
            request.var_id.add(Integer.valueOf(messageId));
            if (i == 0) {
                request.reason = new TL_inputReportReasonSpam();
            } else if (i == 1) {
                request.reason = new TL_inputReportReasonViolence();
            } else if (i == 2) {
                request.reason = new TL_inputReportReasonPornography();
            }
            req = request;
        } else {
            request = new TL_account_reportPeer();
            request.peer = peer;
            if (i == 0) {
                request.reason = new TL_inputReportReasonSpam();
            } else if (i == 1) {
                request.reason = new TL_inputReportReasonViolence();
            } else if (i == 2) {
                request.reason = new TL_inputReportReasonPornography();
            }
            req = request;
        }
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, AlertsCreator$$Lambda$31.$instance);
        Toast.makeText(context, LocaleController.getString("ReportChatSent", CLASSNAMER.string.ReportChatSent), 0).show();
    }

    static final /* synthetic */ void lambda$null$11$AlertsCreator(TLObject response, TL_error error) {
    }

    private static String getFloodWaitString(String error) {
        String timeString;
        int time = Utilities.parseInt(error).intValue();
        if (time < 60) {
            timeString = LocaleController.formatPluralString("Seconds", time);
        } else {
            timeString = LocaleController.formatPluralString("Minutes", time / 60);
        }
        return LocaleController.formatString("FloodWaitTime", CLASSNAMER.string.FloodWaitTime, timeString);
    }

    public static void showFloodWaitAlert(String error, BaseFragment fragment) {
        if (error != null && error.startsWith("FLOOD_WAIT") && fragment != null && fragment.getParentActivity() != null) {
            String timeString;
            int time = Utilities.parseInt(error).intValue();
            if (time < 60) {
                timeString = LocaleController.formatPluralString("Seconds", time);
            } else {
                timeString = LocaleController.formatPluralString("Minutes", time / 60);
            }
            Builder builder = new Builder(fragment.getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", CLASSNAMER.string.AppName));
            builder.setMessage(LocaleController.formatString("FloodWaitTime", CLASSNAMER.string.FloodWaitTime, timeString));
            builder.setPositiveButton(LocaleController.getString("OK", CLASSNAMER.string.OK), null);
            fragment.showDialog(builder.create(), true, null);
        }
    }

    public static void showSendMediaAlert(int result, BaseFragment fragment) {
        if (result != 0) {
            Builder builder = new Builder(fragment.getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", CLASSNAMER.string.AppName));
            if (result == 1) {
                builder.setMessage(LocaleController.getString("ErrorSendRestrictedStickers", CLASSNAMER.string.ErrorSendRestrictedStickers));
            } else if (result == 2) {
                builder.setMessage(LocaleController.getString("ErrorSendRestrictedMedia", CLASSNAMER.string.ErrorSendRestrictedMedia));
            }
            builder.setPositiveButton(LocaleController.getString("OK", CLASSNAMER.string.OK), null);
            fragment.showDialog(builder.create(), true, null);
        }
    }

    public static void showAddUserAlert(String error, BaseFragment fragment, boolean isChannel) {
        if (error != null && fragment != null && fragment.getParentActivity() != null) {
            Builder builder = new Builder(fragment.getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", CLASSNAMER.string.AppName));
            boolean z = true;
            switch (error.hashCode()) {
                case -1763467626:
                    if (error.equals("USERS_TOO_FEW")) {
                        z = true;
                        break;
                    }
                    break;
                case -538116776:
                    if (error.equals("USER_BLOCKED")) {
                        z = true;
                        break;
                    }
                    break;
                case -512775857:
                    if (error.equals("USER_RESTRICTED")) {
                        z = true;
                        break;
                    }
                    break;
                case -454039871:
                    if (error.equals("PEER_FLOOD")) {
                        z = false;
                        break;
                    }
                    break;
                case -420079733:
                    if (error.equals("BOTS_TOO_MUCH")) {
                        z = true;
                        break;
                    }
                    break;
                case 98635865:
                    if (error.equals("USER_KICKED")) {
                        z = true;
                        break;
                    }
                    break;
                case 517420851:
                    if (error.equals("USER_BOT")) {
                        z = true;
                        break;
                    }
                    break;
                case 845559454:
                    if (error.equals("YOU_BLOCKED_USER")) {
                        z = true;
                        break;
                    }
                    break;
                case 916342611:
                    if (error.equals("USER_ADMIN_INVALID")) {
                        z = true;
                        break;
                    }
                    break;
                case 1047173446:
                    if (error.equals("CHAT_ADMIN_BAN_REQUIRED")) {
                        z = true;
                        break;
                    }
                    break;
                case 1167301807:
                    if (error.equals("USERS_TOO_MUCH")) {
                        z = true;
                        break;
                    }
                    break;
                case 1227003815:
                    if (error.equals("USER_ID_INVALID")) {
                        z = true;
                        break;
                    }
                    break;
                case 1253103379:
                    if (error.equals("ADMINS_TOO_MUCH")) {
                        z = true;
                        break;
                    }
                    break;
                case 1623167701:
                    if (error.equals("USER_NOT_MUTUAL_CONTACT")) {
                        z = true;
                        break;
                    }
                    break;
                case 1754587486:
                    if (error.equals("CHAT_ADMIN_INVITE_REQUIRED")) {
                        z = true;
                        break;
                    }
                    break;
                case 1916725894:
                    if (error.equals("USER_PRIVACY_RESTRICTED")) {
                        z = true;
                        break;
                    }
                    break;
            }
            switch (z) {
                case false:
                    builder.setMessage(LocaleController.getString("NobodyLikesSpam2", CLASSNAMER.string.NobodyLikesSpam2));
                    builder.setNegativeButton(LocaleController.getString("MoreInfo", CLASSNAMER.string.MoreInfo), new AlertsCreator$$Lambda$12(fragment));
                    break;
                case true:
                case true:
                case true:
                    if (!isChannel) {
                        builder.setMessage(LocaleController.getString("GroupUserCantAdd", CLASSNAMER.string.GroupUserCantAdd));
                        break;
                    } else {
                        builder.setMessage(LocaleController.getString("ChannelUserCantAdd", CLASSNAMER.string.ChannelUserCantAdd));
                        break;
                    }
                case true:
                    if (!isChannel) {
                        builder.setMessage(LocaleController.getString("GroupUserAddLimit", CLASSNAMER.string.GroupUserAddLimit));
                        break;
                    } else {
                        builder.setMessage(LocaleController.getString("ChannelUserAddLimit", CLASSNAMER.string.ChannelUserAddLimit));
                        break;
                    }
                case true:
                    if (!isChannel) {
                        builder.setMessage(LocaleController.getString("GroupUserLeftError", CLASSNAMER.string.GroupUserLeftError));
                        break;
                    } else {
                        builder.setMessage(LocaleController.getString("ChannelUserLeftError", CLASSNAMER.string.ChannelUserLeftError));
                        break;
                    }
                case true:
                    if (!isChannel) {
                        builder.setMessage(LocaleController.getString("GroupUserCantAdmin", CLASSNAMER.string.GroupUserCantAdmin));
                        break;
                    } else {
                        builder.setMessage(LocaleController.getString("ChannelUserCantAdmin", CLASSNAMER.string.ChannelUserCantAdmin));
                        break;
                    }
                case true:
                    if (!isChannel) {
                        builder.setMessage(LocaleController.getString("GroupUserCantBot", CLASSNAMER.string.GroupUserCantBot));
                        break;
                    } else {
                        builder.setMessage(LocaleController.getString("ChannelUserCantBot", CLASSNAMER.string.ChannelUserCantBot));
                        break;
                    }
                case true:
                    if (!isChannel) {
                        builder.setMessage(LocaleController.getString("InviteToGroupError", CLASSNAMER.string.InviteToGroupError));
                        break;
                    } else {
                        builder.setMessage(LocaleController.getString("InviteToChannelError", CLASSNAMER.string.InviteToChannelError));
                        break;
                    }
                case true:
                    builder.setMessage(LocaleController.getString("CreateGroupError", CLASSNAMER.string.CreateGroupError));
                    break;
                case true:
                    builder.setMessage(LocaleController.getString("UserRestricted", CLASSNAMER.string.UserRestricted));
                    break;
                case true:
                    builder.setMessage(LocaleController.getString("YouBlockedUser", CLASSNAMER.string.YouBlockedUser));
                    break;
                case true:
                case true:
                    builder.setMessage(LocaleController.getString("AddAdminErrorBlacklisted", CLASSNAMER.string.AddAdminErrorBlacklisted));
                    break;
                case true:
                    builder.setMessage(LocaleController.getString("AddAdminErrorNotAMember", CLASSNAMER.string.AddAdminErrorNotAMember));
                    break;
                case true:
                    builder.setMessage(LocaleController.getString("AddBannedErrorAdmin", CLASSNAMER.string.AddBannedErrorAdmin));
                    break;
                default:
                    builder.setMessage(LocaleController.getString("ErrorOccurred", CLASSNAMER.string.ErrorOccurred) + "\n" + error);
                    break;
            }
            builder.setPositiveButton(LocaleController.getString("OK", CLASSNAMER.string.OK), null);
            fragment.showDialog(builder.create(), true, null);
        }
    }

    public static Dialog createColorSelectDialog(Activity parentActivity, long dialog_id, boolean globalGroup, boolean globalAll, Runnable onSelect) {
        int currentColor;
        SharedPreferences preferences = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
        if (globalGroup) {
            currentColor = preferences.getInt("GroupLed", -16776961);
        } else if (globalAll) {
            currentColor = preferences.getInt("MessagesLed", -16776961);
        } else {
            if (preferences.contains("color_" + dialog_id)) {
                currentColor = preferences.getInt("color_" + dialog_id, -16776961);
            } else if (((int) dialog_id) < 0) {
                currentColor = preferences.getInt("GroupLed", -16776961);
            } else {
                currentColor = preferences.getInt("MessagesLed", -16776961);
            }
        }
        View linearLayout = new LinearLayout(parentActivity);
        linearLayout.setOrientation(1);
        String[] descriptions = new String[]{LocaleController.getString("ColorRed", CLASSNAMER.string.ColorRed), LocaleController.getString("ColorOrange", CLASSNAMER.string.ColorOrange), LocaleController.getString("ColorYellow", CLASSNAMER.string.ColorYellow), LocaleController.getString("ColorGreen", CLASSNAMER.string.ColorGreen), LocaleController.getString("ColorCyan", CLASSNAMER.string.ColorCyan), LocaleController.getString("ColorBlue", CLASSNAMER.string.ColorBlue), LocaleController.getString("ColorViolet", CLASSNAMER.string.ColorViolet), LocaleController.getString("ColorPink", CLASSNAMER.string.ColorPink), LocaleController.getString("ColorWhite", CLASSNAMER.string.ColorWhite)};
        int[] selectedColor = new int[]{currentColor};
        for (int a = 0; a < 9; a++) {
            RadioColorCell cell = new RadioColorCell(parentActivity);
            cell.setPadding(AndroidUtilities.m10dp(4.0f), 0, AndroidUtilities.m10dp(4.0f), 0);
            cell.setTag(Integer.valueOf(a));
            cell.setCheckColor(TextColorCell.colors[a], TextColorCell.colors[a]);
            cell.setTextAndValue(descriptions[a], currentColor == TextColorCell.colorsToSave[a]);
            linearLayout.addView(cell);
            cell.setOnClickListener(new AlertsCreator$$Lambda$13(linearLayout, selectedColor));
        }
        Builder builder = new Builder((Context) parentActivity);
        builder.setTitle(LocaleController.getString("LedColor", CLASSNAMER.string.LedColor));
        builder.setView(linearLayout);
        builder.setPositiveButton(LocaleController.getString("Set", CLASSNAMER.string.Set), new AlertsCreator$$Lambda$14(globalAll, selectedColor, globalGroup, dialog_id, onSelect));
        builder.setNeutralButton(LocaleController.getString("LedDisabled", CLASSNAMER.string.LedDisabled), new AlertsCreator$$Lambda$15(globalAll, globalGroup, dialog_id, onSelect));
        if (!(globalAll || globalGroup)) {
            builder.setNegativeButton(LocaleController.getString("Default", CLASSNAMER.string.Default), new AlertsCreator$$Lambda$16(dialog_id, onSelect));
        }
        return builder.create();
    }

    static final /* synthetic */ void lambda$createColorSelectDialog$14$AlertsCreator(LinearLayout linearLayout, int[] selectedColor, View v) {
        int count = linearLayout.getChildCount();
        for (int a1 = 0; a1 < count; a1++) {
            boolean z;
            View cell1 = (RadioColorCell) linearLayout.getChildAt(a1);
            if (cell1 == v) {
                z = true;
            } else {
                z = false;
            }
            cell1.setChecked(z, true);
        }
        selectedColor[0] = TextColorCell.colorsToSave[((Integer) v.getTag()).intValue()];
    }

    static final /* synthetic */ void lambda$createColorSelectDialog$15$AlertsCreator(boolean globalAll, int[] selectedColor, boolean globalGroup, long dialog_id, Runnable onSelect, DialogInterface dialogInterface, int which) {
        Editor editor = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
        if (globalAll) {
            editor.putInt("MessagesLed", selectedColor[0]);
        } else if (globalGroup) {
            editor.putInt("GroupLed", selectedColor[0]);
        } else {
            editor.putInt("color_" + dialog_id, selectedColor[0]);
        }
        editor.commit();
        if (onSelect != null) {
            onSelect.run();
        }
    }

    static final /* synthetic */ void lambda$createColorSelectDialog$16$AlertsCreator(boolean globalAll, boolean globalGroup, long dialog_id, Runnable onSelect, DialogInterface dialog, int which) {
        Editor editor = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
        if (globalAll) {
            editor.putInt("MessagesLed", 0);
        } else if (globalGroup) {
            editor.putInt("GroupLed", 0);
        } else {
            editor.putInt("color_" + dialog_id, 0);
        }
        editor.commit();
        if (onSelect != null) {
            onSelect.run();
        }
    }

    static final /* synthetic */ void lambda$createColorSelectDialog$17$AlertsCreator(long dialog_id, Runnable onSelect, DialogInterface dialog, int which) {
        Editor editor = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
        editor.remove("color_" + dialog_id);
        editor.commit();
        if (onSelect != null) {
            onSelect.run();
        }
    }

    public static Dialog createVibrationSelectDialog(Activity parentActivity, BaseFragment parentFragment, long dialog_id, boolean globalGroup, boolean globalAll, Runnable onSelect) {
        String prefix = dialog_id != 0 ? "vibrate_" : globalGroup ? "vibrate_group" : "vibrate_messages";
        return AlertsCreator.createVibrationSelectDialog(parentActivity, parentFragment, dialog_id, prefix, onSelect);
    }

    public static Dialog createVibrationSelectDialog(Activity parentActivity, BaseFragment parentFragment, long dialog_id, String prefKeyPrefix, Runnable onSelect) {
        String[] descriptions;
        SharedPreferences preferences = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
        int[] selected = new int[1];
        if (dialog_id != 0) {
            selected[0] = preferences.getInt(prefKeyPrefix + dialog_id, 0);
            if (selected[0] == 3) {
                selected[0] = 2;
            } else if (selected[0] == 2) {
                selected[0] = 3;
            }
            descriptions = new String[]{LocaleController.getString("VibrationDefault", CLASSNAMER.string.VibrationDefault), LocaleController.getString("Short", CLASSNAMER.string.Short), LocaleController.getString("Long", CLASSNAMER.string.Long), LocaleController.getString("VibrationDisabled", CLASSNAMER.string.VibrationDisabled)};
        } else {
            selected[0] = preferences.getInt(prefKeyPrefix, 0);
            if (selected[0] == 0) {
                selected[0] = 1;
            } else if (selected[0] == 1) {
                selected[0] = 2;
            } else if (selected[0] == 2) {
                selected[0] = 0;
            }
            descriptions = new String[]{LocaleController.getString("VibrationDisabled", CLASSNAMER.string.VibrationDisabled), LocaleController.getString("VibrationDefault", CLASSNAMER.string.VibrationDefault), LocaleController.getString("Short", CLASSNAMER.string.Short), LocaleController.getString("Long", CLASSNAMER.string.Long), LocaleController.getString("OnlyIfSilent", CLASSNAMER.string.OnlyIfSilent)};
        }
        LinearLayout linearLayout = new LinearLayout(parentActivity);
        linearLayout.setOrientation(1);
        int a = 0;
        while (a < descriptions.length) {
            RadioColorCell cell = new RadioColorCell(parentActivity);
            cell.setPadding(AndroidUtilities.m10dp(4.0f), 0, AndroidUtilities.m10dp(4.0f), 0);
            cell.setTag(Integer.valueOf(a));
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
            cell.setTextAndValue(descriptions[a], selected[0] == a);
            linearLayout.addView(cell);
            cell.setOnClickListener(new AlertsCreator$$Lambda$17(selected, dialog_id, prefKeyPrefix, parentFragment, onSelect));
            a++;
        }
        Builder builder = new Builder((Context) parentActivity);
        builder.setTitle(LocaleController.getString("Vibrate", CLASSNAMER.string.Vibrate));
        builder.setView(linearLayout);
        builder.setPositiveButton(LocaleController.getString("Cancel", CLASSNAMER.string.Cancel), null);
        return builder.create();
    }

    static final /* synthetic */ void lambda$createVibrationSelectDialog$18$AlertsCreator(int[] selected, long dialog_id, String prefKeyPrefix, BaseFragment parentFragment, Runnable onSelect, View v) {
        selected[0] = ((Integer) v.getTag()).intValue();
        Editor editor = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
        if (dialog_id != 0) {
            if (selected[0] == 0) {
                editor.putInt(prefKeyPrefix + dialog_id, 0);
            } else if (selected[0] == 1) {
                editor.putInt(prefKeyPrefix + dialog_id, 1);
            } else if (selected[0] == 2) {
                editor.putInt(prefKeyPrefix + dialog_id, 3);
            } else if (selected[0] == 3) {
                editor.putInt(prefKeyPrefix + dialog_id, 2);
            }
        } else if (selected[0] == 0) {
            editor.putInt(prefKeyPrefix, 2);
        } else if (selected[0] == 1) {
            editor.putInt(prefKeyPrefix, 0);
        } else if (selected[0] == 2) {
            editor.putInt(prefKeyPrefix, 1);
        } else if (selected[0] == 3) {
            editor.putInt(prefKeyPrefix, 3);
        } else if (selected[0] == 4) {
            editor.putInt(prefKeyPrefix, 4);
        }
        editor.commit();
        if (parentFragment != null) {
            parentFragment.dismissCurrentDialig();
        }
        if (onSelect != null) {
            onSelect.run();
        }
    }

    public static Dialog createLocationUpdateDialog(Activity parentActivity, User user, IntCallback callback) {
        int[] selected = new int[1];
        String[] descriptions = new String[]{LocaleController.getString("SendLiveLocationFor15m", CLASSNAMER.string.SendLiveLocationFor15m), LocaleController.getString("SendLiveLocationFor1h", CLASSNAMER.string.SendLiveLocationFor1h), LocaleController.getString("SendLiveLocationFor8h", CLASSNAMER.string.SendLiveLocationFor8h)};
        LinearLayout linearLayout = new LinearLayout(parentActivity);
        linearLayout.setOrientation(1);
        TextView titleTextView = new TextView(parentActivity);
        if (user != null) {
            titleTextView.setText(LocaleController.formatString("LiveLocationAlertPrivate", CLASSNAMER.string.LiveLocationAlertPrivate, UserObject.getFirstName(user)));
        } else {
            titleTextView.setText(LocaleController.getString("LiveLocationAlertGroup", CLASSNAMER.string.LiveLocationAlertGroup));
        }
        titleTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleTextView.setTextSize(1, 16.0f);
        titleTextView.setGravity((LocaleController.isRTL ? 5 : 3) | 48);
        linearLayout.addView(titleTextView, LayoutHelper.createLinear(-2, -2, (LocaleController.isRTL ? 5 : 3) | 48, 24, 0, 24, 8));
        int a = 0;
        while (a < descriptions.length) {
            RadioColorCell cell = new RadioColorCell(parentActivity);
            cell.setPadding(AndroidUtilities.m10dp(4.0f), 0, AndroidUtilities.m10dp(4.0f), 0);
            cell.setTag(Integer.valueOf(a));
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
            cell.setTextAndValue(descriptions[a], selected[0] == a);
            linearLayout.addView(cell);
            cell.setOnClickListener(new AlertsCreator$$Lambda$18(selected, linearLayout));
            a++;
        }
        Builder builder = new Builder((Context) parentActivity);
        builder.setTopImage(new ShareLocationDrawable(parentActivity, false), Theme.getColor(Theme.key_dialogTopBackground));
        builder.setView(linearLayout);
        builder.setPositiveButton(LocaleController.getString("ShareFile", CLASSNAMER.string.ShareFile), new AlertsCreator$$Lambda$19(selected, callback));
        builder.setNeutralButton(LocaleController.getString("Cancel", CLASSNAMER.string.Cancel), null);
        return builder.create();
    }

    static final /* synthetic */ void lambda$createLocationUpdateDialog$19$AlertsCreator(int[] selected, LinearLayout linearLayout, View v) {
        selected[0] = ((Integer) v.getTag()).intValue();
        int count = linearLayout.getChildCount();
        for (int a1 = 0; a1 < count; a1++) {
            View child = linearLayout.getChildAt(a1);
            if (child instanceof RadioColorCell) {
                boolean z;
                RadioColorCell radioColorCell = (RadioColorCell) child;
                if (child == v) {
                    z = true;
                } else {
                    z = false;
                }
                radioColorCell.setChecked(z, true);
            }
        }
    }

    static final /* synthetic */ void lambda$createLocationUpdateDialog$20$AlertsCreator(int[] selected, IntCallback callback, DialogInterface dialog, int which) {
        int time;
        if (selected[0] == 0) {
            time = 900;
        } else if (selected[0] == 1) {
            time = 3600;
        } else {
            time = 28800;
        }
        callback.lambda$null$79$MessagesStorage(time);
    }

    public static Builder createContactsPermissionDialog(Activity parentActivity, IntCallback callback) {
        Builder builder = new Builder((Context) parentActivity);
        builder.setTopImage((int) CLASSNAMER.drawable.permissions_contacts, Theme.getColor(Theme.key_dialogTopBackground));
        builder.setMessage(AndroidUtilities.replaceTags(LocaleController.getString("ContactsPermissionAlert", CLASSNAMER.string.ContactsPermissionAlert)));
        builder.setPositiveButton(LocaleController.getString("ContactsPermissionAlertContinue", CLASSNAMER.string.ContactsPermissionAlertContinue), new AlertsCreator$$Lambda$20(callback));
        builder.setNegativeButton(LocaleController.getString("ContactsPermissionAlertNotNow", CLASSNAMER.string.ContactsPermissionAlertNotNow), new AlertsCreator$$Lambda$21(callback));
        return builder;
    }

    public static Dialog createFreeSpaceDialog(LaunchActivity parentActivity) {
        int[] selected = new int[1];
        int keepMedia = MessagesController.getGlobalMainSettings().getInt("keep_media", 2);
        if (keepMedia == 2) {
            selected[0] = 3;
        } else if (keepMedia == 0) {
            selected[0] = 1;
        } else if (keepMedia == 1) {
            selected[0] = 2;
        } else if (keepMedia == 3) {
            selected[0] = 0;
        }
        String[] descriptions = new String[]{LocaleController.formatPluralString("Days", 3), LocaleController.formatPluralString("Weeks", 1), LocaleController.formatPluralString("Months", 1), LocaleController.getString("LowDiskSpaceNeverRemove", CLASSNAMER.string.LowDiskSpaceNeverRemove)};
        LinearLayout linearLayout = new LinearLayout(parentActivity);
        linearLayout.setOrientation(1);
        View titleTextView = new TextView(parentActivity);
        titleTextView.setText(LocaleController.getString("LowDiskSpaceTitle2", CLASSNAMER.string.LowDiskSpaceTitle2));
        titleTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleTextView.setTextSize(1, 16.0f);
        titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        titleTextView.setGravity((LocaleController.isRTL ? 5 : 3) | 48);
        linearLayout.addView(titleTextView, LayoutHelper.createLinear(-2, -2, (LocaleController.isRTL ? 5 : 3) | 48, 24, 0, 24, 8));
        int a = 0;
        while (a < descriptions.length) {
            RadioColorCell cell = new RadioColorCell(parentActivity);
            cell.setPadding(AndroidUtilities.m10dp(4.0f), 0, AndroidUtilities.m10dp(4.0f), 0);
            cell.setTag(Integer.valueOf(a));
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
            cell.setTextAndValue(descriptions[a], selected[0] == a);
            linearLayout.addView(cell);
            cell.setOnClickListener(new AlertsCreator$$Lambda$22(selected, linearLayout));
            a++;
        }
        Builder builder = new Builder((Context) parentActivity);
        builder.setTitle(LocaleController.getString("LowDiskSpaceTitle", CLASSNAMER.string.LowDiskSpaceTitle));
        builder.setMessage(LocaleController.getString("LowDiskSpaceMessage", CLASSNAMER.string.LowDiskSpaceMessage));
        builder.setView(linearLayout);
        builder.setPositiveButton(LocaleController.getString("OK", CLASSNAMER.string.OK), new AlertsCreator$$Lambda$23(selected));
        builder.setNeutralButton(LocaleController.getString("ClearMediaCache", CLASSNAMER.string.ClearMediaCache), new AlertsCreator$$Lambda$24(parentActivity));
        return builder.create();
    }

    static final /* synthetic */ void lambda$createFreeSpaceDialog$23$AlertsCreator(int[] selected, LinearLayout linearLayout, View v) {
        int num = ((Integer) v.getTag()).intValue();
        if (num == 0) {
            selected[0] = 3;
        } else if (num == 1) {
            selected[0] = 0;
        } else if (num == 2) {
            selected[0] = 1;
        } else if (num == 3) {
            selected[0] = 2;
        }
        int count = linearLayout.getChildCount();
        for (int a1 = 0; a1 < count; a1++) {
            View child = linearLayout.getChildAt(a1);
            if (child instanceof RadioColorCell) {
                boolean z;
                RadioColorCell radioColorCell = (RadioColorCell) child;
                if (child == v) {
                    z = true;
                } else {
                    z = false;
                }
                radioColorCell.setChecked(z, true);
            }
        }
    }

    public static Dialog createPrioritySelectDialog(Activity parentActivity, BaseFragment parentFragment, long dialog_id, boolean globalGroup, boolean globalAll, Runnable onSelect) {
        String[] descriptions;
        SharedPreferences preferences = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
        int[] selected = new int[1];
        if (dialog_id != 0) {
            selected[0] = preferences.getInt("priority_" + dialog_id, 3);
            if (selected[0] == 3) {
                selected[0] = 0;
            } else if (selected[0] == 4) {
                selected[0] = 1;
            } else if (selected[0] == 5) {
                selected[0] = 2;
            } else if (selected[0] == 0) {
                selected[0] = 3;
            } else {
                selected[0] = 4;
            }
            descriptions = new String[]{LocaleController.getString("NotificationsPrioritySettings", CLASSNAMER.string.NotificationsPrioritySettings), LocaleController.getString("NotificationsPriorityLow", CLASSNAMER.string.NotificationsPriorityLow), LocaleController.getString("NotificationsPriorityMedium", CLASSNAMER.string.NotificationsPriorityMedium), LocaleController.getString("NotificationsPriorityHigh", CLASSNAMER.string.NotificationsPriorityHigh), LocaleController.getString("NotificationsPriorityUrgent", CLASSNAMER.string.NotificationsPriorityUrgent)};
        } else {
            if (globalAll) {
                selected[0] = preferences.getInt("priority_messages", 1);
            } else if (globalGroup) {
                selected[0] = preferences.getInt("priority_group", 1);
            }
            if (selected[0] == 4) {
                selected[0] = 0;
            } else if (selected[0] == 5) {
                selected[0] = 1;
            } else if (selected[0] == 0) {
                selected[0] = 2;
            } else {
                selected[0] = 3;
            }
            descriptions = new String[]{LocaleController.getString("NotificationsPriorityLow", CLASSNAMER.string.NotificationsPriorityLow), LocaleController.getString("NotificationsPriorityMedium", CLASSNAMER.string.NotificationsPriorityMedium), LocaleController.getString("NotificationsPriorityHigh", CLASSNAMER.string.NotificationsPriorityHigh), LocaleController.getString("NotificationsPriorityUrgent", CLASSNAMER.string.NotificationsPriorityUrgent)};
        }
        LinearLayout linearLayout = new LinearLayout(parentActivity);
        linearLayout.setOrientation(1);
        int a = 0;
        while (a < descriptions.length) {
            RadioColorCell cell = new RadioColorCell(parentActivity);
            cell.setPadding(AndroidUtilities.m10dp(4.0f), 0, AndroidUtilities.m10dp(4.0f), 0);
            cell.setTag(Integer.valueOf(a));
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
            cell.setTextAndValue(descriptions[a], selected[0] == a);
            linearLayout.addView(cell);
            cell.setOnClickListener(new AlertsCreator$$Lambda$25(selected, dialog_id, globalGroup, parentFragment, onSelect));
            a++;
        }
        Builder builder = new Builder((Context) parentActivity);
        builder.setTitle(LocaleController.getString("NotificationsImportance", CLASSNAMER.string.NotificationsImportance));
        builder.setView(linearLayout);
        builder.setPositiveButton(LocaleController.getString("Cancel", CLASSNAMER.string.Cancel), null);
        return builder.create();
    }

    static final /* synthetic */ void lambda$createPrioritySelectDialog$26$AlertsCreator(int[] selected, long dialog_id, boolean globalGroup, BaseFragment parentFragment, Runnable onSelect, View v) {
        selected[0] = ((Integer) v.getTag()).intValue();
        Editor editor = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
        int option;
        if (dialog_id != 0) {
            if (selected[0] == 0) {
                option = 3;
            } else if (selected[0] == 1) {
                option = 4;
            } else if (selected[0] == 2) {
                option = 5;
            } else if (selected[0] == 3) {
                option = 0;
            } else {
                option = 1;
            }
            editor.putInt("priority_" + dialog_id, option);
        } else {
            String str;
            if (selected[0] == 0) {
                option = 4;
            } else if (selected[0] == 1) {
                option = 5;
            } else if (selected[0] == 2) {
                option = 0;
            } else {
                option = 1;
            }
            if (globalGroup) {
                str = "priority_group";
            } else {
                str = "priority_messages";
            }
            editor.putInt(str, option);
        }
        editor.commit();
        if (parentFragment != null) {
            parentFragment.dismissCurrentDialig();
        }
        if (onSelect != null) {
            onSelect.run();
        }
    }

    public static Dialog createPopupSelectDialog(Activity parentActivity, BaseFragment parentFragment, boolean globalGroup, boolean globalAll, Runnable onSelect) {
        SharedPreferences preferences = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
        int[] selected = new int[1];
        if (globalAll) {
            selected[0] = preferences.getInt("popupAll", 0);
        } else if (globalGroup) {
            selected[0] = preferences.getInt("popupGroup", 0);
        }
        String[] descriptions = new String[]{LocaleController.getString("NoPopup", CLASSNAMER.string.NoPopup), LocaleController.getString("OnlyWhenScreenOn", CLASSNAMER.string.OnlyWhenScreenOn), LocaleController.getString("OnlyWhenScreenOff", CLASSNAMER.string.OnlyWhenScreenOff), LocaleController.getString("AlwaysShowPopup", CLASSNAMER.string.AlwaysShowPopup)};
        LinearLayout linearLayout = new LinearLayout(parentActivity);
        linearLayout.setOrientation(1);
        int a = 0;
        while (a < descriptions.length) {
            RadioColorCell cell = new RadioColorCell(parentActivity);
            cell.setTag(Integer.valueOf(a));
            cell.setPadding(AndroidUtilities.m10dp(4.0f), 0, AndroidUtilities.m10dp(4.0f), 0);
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
            cell.setTextAndValue(descriptions[a], selected[0] == a);
            linearLayout.addView(cell);
            cell.setOnClickListener(new AlertsCreator$$Lambda$26(selected, globalGroup, parentFragment, onSelect));
            a++;
        }
        Builder builder = new Builder((Context) parentActivity);
        builder.setTitle(LocaleController.getString("PopupNotification", CLASSNAMER.string.PopupNotification));
        builder.setView(linearLayout);
        builder.setPositiveButton(LocaleController.getString("Cancel", CLASSNAMER.string.Cancel), null);
        return builder.create();
    }

    static final /* synthetic */ void lambda$createPopupSelectDialog$27$AlertsCreator(int[] selected, boolean globalGroup, BaseFragment parentFragment, Runnable onSelect, View v) {
        selected[0] = ((Integer) v.getTag()).intValue();
        Editor editor = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
        editor.putInt(globalGroup ? "popupGroup" : "popupAll", selected[0]);
        editor.commit();
        if (parentFragment != null) {
            parentFragment.dismissCurrentDialig();
        }
        if (onSelect != null) {
            onSelect.run();
        }
    }

    public static Dialog createSingleChoiceDialog(Activity parentActivity, BaseFragment parentFragment, String[] options, String title, int selected, OnClickListener listener) {
        LinearLayout linearLayout = new LinearLayout(parentActivity);
        linearLayout.setOrientation(1);
        Builder builder = new Builder((Context) parentActivity);
        for (int a = 0; a < options.length; a++) {
            boolean z;
            RadioColorCell cell = new RadioColorCell(parentActivity);
            cell.setPadding(AndroidUtilities.m10dp(4.0f), 0, AndroidUtilities.m10dp(4.0f), 0);
            cell.setTag(Integer.valueOf(a));
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
            String str = options[a];
            if (selected == a) {
                z = true;
            } else {
                z = false;
            }
            cell.setTextAndValue(str, z);
            linearLayout.addView(cell);
            cell.setOnClickListener(new AlertsCreator$$Lambda$27(parentFragment, listener));
        }
        builder.setTitle(title);
        builder.setView(linearLayout);
        builder.setPositiveButton(LocaleController.getString("Cancel", CLASSNAMER.string.Cancel), null);
        return builder.create();
    }

    static final /* synthetic */ void lambda$createSingleChoiceDialog$28$AlertsCreator(BaseFragment parentFragment, OnClickListener listener, View v) {
        int sel = ((Integer) v.getTag()).intValue();
        if (parentFragment != null) {
            parentFragment.dismissCurrentDialig();
        }
        listener.onClick(null, sel);
    }

    public static Builder createTTLAlert(Context context, EncryptedChat encryptedChat) {
        Builder builder = new Builder(context);
        builder.setTitle(LocaleController.getString("MessageLifetime", CLASSNAMER.string.MessageLifetime));
        NumberPicker numberPicker = new NumberPicker(context);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(20);
        if (encryptedChat.ttl > 0 && encryptedChat.ttl < 16) {
            numberPicker.setValue(encryptedChat.ttl);
        } else if (encryptedChat.ttl == 30) {
            numberPicker.setValue(16);
        } else if (encryptedChat.ttl == 60) {
            numberPicker.setValue(17);
        } else if (encryptedChat.ttl == 3600) {
            numberPicker.setValue(18);
        } else if (encryptedChat.ttl == 86400) {
            numberPicker.setValue(19);
        } else if (encryptedChat.ttl == 604800) {
            numberPicker.setValue(20);
        } else if (encryptedChat.ttl == 0) {
            numberPicker.setValue(0);
        }
        numberPicker.setFormatter(AlertsCreator$$Lambda$28.$instance);
        builder.setView(numberPicker);
        builder.setNegativeButton(LocaleController.getString("Done", CLASSNAMER.string.Done), new AlertsCreator$$Lambda$29(encryptedChat, numberPicker));
        return builder;
    }

    static final /* synthetic */ String lambda$createTTLAlert$29$AlertsCreator(int value) {
        if (value == 0) {
            return LocaleController.getString("ShortMessageLifetimeForever", CLASSNAMER.string.ShortMessageLifetimeForever);
        }
        if (value >= 1 && value < 16) {
            return LocaleController.formatTTLString(value);
        }
        if (value == 16) {
            return LocaleController.formatTTLString(30);
        }
        if (value == 17) {
            return LocaleController.formatTTLString(60);
        }
        if (value == 18) {
            return LocaleController.formatTTLString(3600);
        }
        if (value == 19) {
            return LocaleController.formatTTLString(86400);
        }
        if (value == 20) {
            return LocaleController.formatTTLString(604800);
        }
        return TtmlNode.ANONYMOUS_REGION_ID;
    }

    static final /* synthetic */ void lambda$createTTLAlert$30$AlertsCreator(EncryptedChat encryptedChat, NumberPicker numberPicker, DialogInterface dialog, int which) {
        int oldValue = encryptedChat.ttl;
        which = numberPicker.getValue();
        if (which >= 0 && which < 16) {
            encryptedChat.ttl = which;
        } else if (which == 16) {
            encryptedChat.ttl = 30;
        } else if (which == 17) {
            encryptedChat.ttl = 60;
        } else if (which == 18) {
            encryptedChat.ttl = 3600;
        } else if (which == 19) {
            encryptedChat.ttl = 86400;
        } else if (which == 20) {
            encryptedChat.ttl = 604800;
        }
        if (oldValue != encryptedChat.ttl) {
            SecretChatHelper.getInstance(UserConfig.selectedAccount).sendTTLMessage(encryptedChat, null);
            MessagesStorage.getInstance(UserConfig.selectedAccount).updateEncryptedChatTTL(encryptedChat);
        }
    }

    public static AlertDialog createAccountSelectDialog(Activity parentActivity, AccountSelectDelegate delegate) {
        if (UserConfig.getActivatedAccountsCount() < 2) {
            return null;
        }
        Builder builder = new Builder((Context) parentActivity);
        Runnable dismissRunnable = builder.getDismissRunnable();
        AlertDialog[] alertDialog = new AlertDialog[1];
        LinearLayout linearLayout = new LinearLayout(parentActivity);
        linearLayout.setOrientation(1);
        for (int a = 0; a < 3; a++) {
            if (UserConfig.getInstance(a).getCurrentUser() != null) {
                AccountSelectCell cell = new AccountSelectCell(parentActivity);
                cell.setAccount(a, false);
                cell.setPadding(AndroidUtilities.m10dp(14.0f), 0, AndroidUtilities.m10dp(14.0f), 0);
                cell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
                linearLayout.addView(cell, LayoutHelper.createLinear(-1, 48));
                cell.setOnClickListener(new AlertsCreator$$Lambda$30(alertDialog, dismissRunnable, delegate));
            }
        }
        builder.setTitle(LocaleController.getString("SelectAccount", CLASSNAMER.string.SelectAccount));
        builder.setView(linearLayout);
        builder.setPositiveButton(LocaleController.getString("Cancel", CLASSNAMER.string.Cancel), null);
        AlertDialog create = builder.create();
        alertDialog[0] = create;
        return create;
    }

    static final /* synthetic */ void lambda$createAccountSelectDialog$31$AlertsCreator(AlertDialog[] alertDialog, Runnable dismissRunnable, AccountSelectDelegate delegate, View v) {
        if (alertDialog[0] != null) {
            alertDialog[0].setOnDismissListener(null);
        }
        dismissRunnable.run();
        delegate.didSelectAccount(((AccountSelectCell) v).getAccountNumber());
    }
}
