package org.telegram.tgnet;

public class TLRPC$TL_inputMessageEntityMentionName extends TLRPC$MessageEntity {
    public static int constructor = NUM;
    public TLRPC$InputUser user_id;

    public void readParams(AbstractSerializedData abstractSerializedData, boolean z) {
        this.offset = abstractSerializedData.readInt32(z);
        this.length = abstractSerializedData.readInt32(z);
        this.user_id = TLRPC$InputUser.TLdeserialize(abstractSerializedData, abstractSerializedData.readInt32(z), z);
    }

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        abstractSerializedData.writeInt32(this.offset);
        abstractSerializedData.writeInt32(this.length);
        this.user_id.serializeToStream(abstractSerializedData);
    }
}
