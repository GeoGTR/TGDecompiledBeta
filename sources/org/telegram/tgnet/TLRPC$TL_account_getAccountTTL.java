package org.telegram.tgnet;

public class TLRPC$TL_account_getAccountTTL extends TLObject {
    public static int constructor = NUM;

    public TLObject deserializeResponse(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        return TLRPC$TL_accountDaysTTL.TLdeserialize(abstractSerializedData, i, z);
    }

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
    }
}
