package org.webrtc;

import java.util.List;
import java.util.Map;
import org.webrtc.MediaStreamTrack;

public class RtpParameters {
    public final List<Codec> codecs;
    public DegradationPreference degradationPreference;
    public final List<Encoding> encodings;
    private final List<HeaderExtension> headerExtensions;
    private final Rtcp rtcp;
    public final String transactionId;

    public enum DegradationPreference {
        DISABLED,
        MAINTAIN_FRAMERATE,
        MAINTAIN_RESOLUTION,
        BALANCED;

        @CalledByNative("DegradationPreference")
        static DegradationPreference fromNativeIndex(int i) {
            return values()[i];
        }
    }

    public static class Encoding {
        public boolean active = true;
        public double bitratePriority = 1.0d;
        public Integer maxBitrateBps;
        public Integer maxFramerate;
        public Integer minBitrateBps;
        public int networkPriority = 0;
        public Integer numTemporalLayers;
        public String rid;
        public Double scaleResolutionDownBy;
        public Long ssrc;

        public Encoding(String str, boolean z, Double d) {
            this.rid = str;
            this.active = z;
            this.scaleResolutionDownBy = d;
        }

        @CalledByNative("Encoding")
        Encoding(String str, boolean z, double d, int i, Integer num, Integer num2, Integer num3, Integer num4, Double d2, Long l) {
            this.rid = str;
            this.active = z;
            this.bitratePriority = d;
            this.networkPriority = i;
            this.maxBitrateBps = num;
            this.minBitrateBps = num2;
            this.maxFramerate = num3;
            this.numTemporalLayers = num4;
            this.scaleResolutionDownBy = d2;
            this.ssrc = l;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Encoding")
        public String getRid() {
            return this.rid;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Encoding")
        public boolean getActive() {
            return this.active;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Encoding")
        public double getBitratePriority() {
            return this.bitratePriority;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Encoding")
        public int getNetworkPriority() {
            return this.networkPriority;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Encoding")
        public Integer getMaxBitrateBps() {
            return this.maxBitrateBps;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Encoding")
        public Integer getMinBitrateBps() {
            return this.minBitrateBps;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Encoding")
        public Integer getMaxFramerate() {
            return this.maxFramerate;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Encoding")
        public Integer getNumTemporalLayers() {
            return this.numTemporalLayers;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Encoding")
        public Double getScaleResolutionDownBy() {
            return this.scaleResolutionDownBy;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Encoding")
        public Long getSsrc() {
            return this.ssrc;
        }
    }

    public static class Codec {
        public Integer clockRate;
        MediaStreamTrack.MediaType kind;
        public String name;
        public Integer numChannels;
        public Map<String, String> parameters;
        public int payloadType;

        @CalledByNative("Codec")
        Codec(int i, String str, MediaStreamTrack.MediaType mediaType, Integer num, Integer num2, Map<String, String> map) {
            this.payloadType = i;
            this.name = str;
            this.kind = mediaType;
            this.clockRate = num;
            this.numChannels = num2;
            this.parameters = map;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Codec")
        public int getPayloadType() {
            return this.payloadType;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Codec")
        public String getName() {
            return this.name;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Codec")
        public MediaStreamTrack.MediaType getKind() {
            return this.kind;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Codec")
        public Integer getClockRate() {
            return this.clockRate;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Codec")
        public Integer getNumChannels() {
            return this.numChannels;
        }

        /* access modifiers changed from: package-private */
        @CalledByNative("Codec")
        public Map getParameters() {
            return this.parameters;
        }
    }

    public static class Rtcp {
        private final String cname;
        private final boolean reducedSize;

        @CalledByNative("Rtcp")
        Rtcp(String str, boolean z) {
            this.cname = str;
            this.reducedSize = z;
        }

        @CalledByNative("Rtcp")
        public String getCname() {
            return this.cname;
        }

        @CalledByNative("Rtcp")
        public boolean getReducedSize() {
            return this.reducedSize;
        }
    }

    public static class HeaderExtension {
        private final boolean encrypted;
        private final int id;
        private final String uri;

        @CalledByNative("HeaderExtension")
        HeaderExtension(String str, int i, boolean z) {
            this.uri = str;
            this.id = i;
            this.encrypted = z;
        }

        @CalledByNative("HeaderExtension")
        public String getUri() {
            return this.uri;
        }

        @CalledByNative("HeaderExtension")
        public int getId() {
            return this.id;
        }

        @CalledByNative("HeaderExtension")
        public boolean getEncrypted() {
            return this.encrypted;
        }
    }

    @CalledByNative
    RtpParameters(String str, DegradationPreference degradationPreference2, Rtcp rtcp2, List<HeaderExtension> list, List<Encoding> list2, List<Codec> list3) {
        this.transactionId = str;
        this.degradationPreference = degradationPreference2;
        this.rtcp = rtcp2;
        this.headerExtensions = list;
        this.encodings = list2;
        this.codecs = list3;
    }

    /* access modifiers changed from: package-private */
    @CalledByNative
    public String getTransactionId() {
        return this.transactionId;
    }

    /* access modifiers changed from: package-private */
    @CalledByNative
    public DegradationPreference getDegradationPreference() {
        return this.degradationPreference;
    }

    @CalledByNative
    public Rtcp getRtcp() {
        return this.rtcp;
    }

    @CalledByNative
    public List<HeaderExtension> getHeaderExtensions() {
        return this.headerExtensions;
    }

    /* access modifiers changed from: package-private */
    @CalledByNative
    public List<Encoding> getEncodings() {
        return this.encodings;
    }

    /* access modifiers changed from: package-private */
    @CalledByNative
    public List<Codec> getCodecs() {
        return this.codecs;
    }
}
