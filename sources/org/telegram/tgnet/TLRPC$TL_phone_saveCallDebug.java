package org.telegram.tgnet;

public class TLRPC$TL_phone_saveCallDebug extends TLObject {
    public static int constructor = NUM;
    public TLRPC$TL_dataJSON debug;
    public TLRPC$TL_inputPhoneCall peer;

    public TLObject deserializeResponse(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        return TLRPC$Bool.TLdeserialize(abstractSerializedData, i, z);
    }

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        this.peer.serializeToStream(abstractSerializedData);
        this.debug.serializeToStream(abstractSerializedData);
    }
}
