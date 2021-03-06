package org.telegram.tgnet;

public class TLRPC$TL_users_getFullUser extends TLObject {
    public static int constructor = -NUM;
    public TLRPC$InputUser id;

    public TLObject deserializeResponse(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        return TLRPC$UserFull.TLdeserialize(abstractSerializedData, i, z);
    }

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        this.id.serializeToStream(abstractSerializedData);
    }
}
