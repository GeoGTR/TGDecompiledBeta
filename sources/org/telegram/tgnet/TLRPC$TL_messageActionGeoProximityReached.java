package org.telegram.tgnet;

public class TLRPC$TL_messageActionGeoProximityReached extends TLRPC$MessageAction {
    public static int constructor = -NUM;
    public int distance;
    public TLRPC$Peer from_id;
    public TLRPC$Peer to_id;

    public void readParams(AbstractSerializedData abstractSerializedData, boolean z) {
        this.from_id = TLRPC$Peer.TLdeserialize(abstractSerializedData, abstractSerializedData.readInt32(z), z);
        this.to_id = TLRPC$Peer.TLdeserialize(abstractSerializedData, abstractSerializedData.readInt32(z), z);
        this.distance = abstractSerializedData.readInt32(z);
    }

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        this.from_id.serializeToStream(abstractSerializedData);
        this.to_id.serializeToStream(abstractSerializedData);
        abstractSerializedData.writeInt32(this.distance);
    }
}
