package org.telegram.tgnet;

public class TLRPC$TL_inputUserSelf extends TLRPC$InputUser {
    public static int constructor = -NUM;

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
    }
}
