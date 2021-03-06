package org.telegram.tgnet;

public class TLRPC$TL_messages_editChatAdmin extends TLObject {
    public static int constructor = -NUM;
    public int chat_id;
    public boolean is_admin;
    public TLRPC$InputUser user_id;

    public TLObject deserializeResponse(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        return TLRPC$Bool.TLdeserialize(abstractSerializedData, i, z);
    }

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        abstractSerializedData.writeInt32(this.chat_id);
        this.user_id.serializeToStream(abstractSerializedData);
        abstractSerializedData.writeBool(this.is_admin);
    }
}
