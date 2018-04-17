package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.TL_channel;
import org.telegram.tgnet.TLRPC.TL_channelForbidden;
import org.telegram.tgnet.TLRPC.TL_chatEmpty;
import org.telegram.tgnet.TLRPC.TL_chatForbidden;

public class ChatObject {
    public static final int CHAT_TYPE_BROADCAST = 1;
    public static final int CHAT_TYPE_CHANNEL = 2;
    public static final int CHAT_TYPE_CHAT = 0;
    public static final int CHAT_TYPE_MEGAGROUP = 4;
    public static final int CHAT_TYPE_USER = 3;

    public static boolean isLeftFromChat(Chat chat) {
        if (!(chat == null || (chat instanceof TL_chatEmpty) || (chat instanceof TL_chatForbidden) || (chat instanceof TL_channelForbidden) || chat.left)) {
            if (!chat.deactivated) {
                return false;
            }
        }
        return true;
    }

    public static boolean isKickedFromChat(Chat chat) {
        if (!(chat == null || (chat instanceof TL_chatEmpty) || (chat instanceof TL_chatForbidden) || (chat instanceof TL_channelForbidden) || chat.kicked || chat.deactivated)) {
            if (chat.banned_rights == null || !chat.banned_rights.view_messages) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotInChat(Chat chat) {
        if (!(chat == null || (chat instanceof TL_chatEmpty) || (chat instanceof TL_chatForbidden) || (chat instanceof TL_channelForbidden) || chat.left || chat.kicked)) {
            if (!chat.deactivated) {
                return false;
            }
        }
        return true;
    }

    public static boolean isChannel(Chat chat) {
        if (!(chat instanceof TL_channel)) {
            if (!(chat instanceof TL_channelForbidden)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isMegagroup(Chat chat) {
        return ((chat instanceof TL_channel) || (chat instanceof TL_channelForbidden)) && chat.megagroup;
    }

    public static boolean hasAdminRights(Chat chat) {
        return chat != null && (chat.creator || !(chat.admin_rights == null || chat.admin_rights.flags == 0));
    }

    public static boolean canChangeChatInfo(Chat chat) {
        return chat != null && (chat.creator || (chat.admin_rights != null && chat.admin_rights.change_info));
    }

    public static boolean canAddAdmins(Chat chat) {
        return chat != null && (chat.creator || (chat.admin_rights != null && chat.admin_rights.add_admins));
    }

    public static boolean canBlockUsers(Chat chat) {
        return chat != null && (chat.creator || (chat.admin_rights != null && chat.admin_rights.ban_users));
    }

    public static boolean canSendStickers(Chat chat) {
        if (chat != null) {
            if (chat != null) {
                if (chat.banned_rights != null) {
                    if (!(chat.banned_rights.send_media || chat.banned_rights.send_stickers)) {
                    }
                }
            }
            return false;
        }
        return true;
    }

    public static boolean canSendEmbed(Chat chat) {
        if (chat != null) {
            if (chat != null) {
                if (chat.banned_rights != null) {
                    if (!(chat.banned_rights.send_media || chat.banned_rights.embed_links)) {
                    }
                }
            }
            return false;
        }
        return true;
    }

    public static boolean canSendMessages(Chat chat) {
        if (chat != null) {
            if (chat != null) {
                if (chat.banned_rights != null) {
                    if (!chat.banned_rights.send_messages) {
                    }
                }
            }
            return false;
        }
        return true;
    }

    public static boolean canPost(Chat chat) {
        return chat != null && (chat.creator || (chat.admin_rights != null && chat.admin_rights.post_messages));
    }

    public static boolean canAddViaLink(Chat chat) {
        return chat != null && (chat.creator || (chat.admin_rights != null && chat.admin_rights.invite_link));
    }

    public static boolean canAddUsers(Chat chat) {
        return chat != null && (chat.creator || (chat.admin_rights != null && chat.admin_rights.invite_users));
    }

    public static boolean canEditInfo(Chat chat) {
        return chat != null && (chat.creator || (chat.admin_rights != null && chat.admin_rights.change_info));
    }

    public static boolean isChannel(int chatId, int currentAccount) {
        Chat chat = MessagesController.getInstance(currentAccount).getChat(Integer.valueOf(chatId));
        if (!(chat instanceof TL_channel)) {
            if (!(chat instanceof TL_channelForbidden)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCanWriteToChannel(int chatId, int currentAccount) {
        Chat chat = MessagesController.getInstance(currentAccount).getChat(Integer.valueOf(chatId));
        return chat != null && (chat.creator || ((chat.admin_rights != null && chat.admin_rights.post_messages) || chat.megagroup));
    }

    public static boolean canWriteToChat(Chat chat) {
        if (isChannel(chat) && !chat.creator && (chat.admin_rights == null || !chat.admin_rights.post_messages)) {
            if (chat.broadcast) {
                return false;
            }
        }
        return true;
    }

    public static Chat getChatByDialog(long did, int currentAccount) {
        int lower_id = (int) did;
        int high_id = (int) (did >> 32);
        if (lower_id < 0) {
            return MessagesController.getInstance(currentAccount).getChat(Integer.valueOf(-lower_id));
        }
        return null;
    }
}
