package org.telegram.tgnet;

public abstract class TLRPC$GroupCall extends TLObject {
    public long access_hash;
    public boolean can_change_join_muted;
    public int duration;
    public int flags;
    public long id;
    public boolean join_muted;
    public TLRPC$TL_dataJSON params;
    public int participants_count;
    public int version;

    public static TLRPC$GroupCall TLdeserialize(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        TLRPC$GroupCall tLRPC$GroupCall;
        if (i != NUM) {
            tLRPC$GroupCall = i != NUM ? null : new TLRPC$TL_groupCallDiscarded();
        } else {
            tLRPC$GroupCall = new TLRPC$TL_groupCall();
        }
        if (tLRPC$GroupCall != null || !z) {
            if (tLRPC$GroupCall != null) {
                tLRPC$GroupCall.readParams(abstractSerializedData, z);
            }
            return tLRPC$GroupCall;
        }
        throw new RuntimeException(String.format("can't parse magic %x in GroupCall", new Object[]{Integer.valueOf(i)}));
    }
}
