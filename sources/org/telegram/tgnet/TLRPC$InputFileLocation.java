package org.telegram.tgnet;

public abstract class TLRPC$InputFileLocation extends TLObject {
    public long access_hash;
    public boolean big;
    public byte[] file_reference;
    public int flags;
    public long id;
    public int local_id;
    public TLRPC$InputPeer peer;
    public long secret;
    public TLRPC$InputStickerSet stickerset;
    public String thumb_size;
    public long volume_id;
}
