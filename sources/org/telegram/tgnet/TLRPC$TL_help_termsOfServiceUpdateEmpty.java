package org.telegram.tgnet;

public class TLRPC$TL_help_termsOfServiceUpdateEmpty extends TLRPC$help_TermsOfServiceUpdate {
    public static int constructor = -NUM;
    public int expires;

    public void readParams(AbstractSerializedData abstractSerializedData, boolean z) {
        this.expires = abstractSerializedData.readInt32(z);
    }

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        abstractSerializedData.writeInt32(this.expires);
    }
}
