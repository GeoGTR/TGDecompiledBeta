package org.telegram.tgnet;

public class TLRPC$TL_messages_editChatPhoto extends TLObject {
    public static int constructor = -NUM;
    public int chat_id;
    public TLRPC$InputChatPhoto photo;

    public TLObject deserializeResponse(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        return TLRPC$Updates.TLdeserialize(abstractSerializedData, i, z);
    }

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        abstractSerializedData.writeInt32(this.chat_id);
        this.photo.serializeToStream(abstractSerializedData);
    }
}
