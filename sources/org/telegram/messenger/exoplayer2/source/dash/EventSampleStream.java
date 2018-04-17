package org.telegram.messenger.exoplayer2.source.dash;

import java.io.IOException;
import org.telegram.messenger.exoplayer2.C0539C;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.FormatHolder;
import org.telegram.messenger.exoplayer2.decoder.DecoderInputBuffer;
import org.telegram.messenger.exoplayer2.metadata.emsg.EventMessageEncoder;
import org.telegram.messenger.exoplayer2.source.SampleStream;
import org.telegram.messenger.exoplayer2.source.dash.manifest.EventStream;
import org.telegram.messenger.exoplayer2.util.Util;

final class EventSampleStream implements SampleStream {
    private int currentIndex;
    private final EventMessageEncoder eventMessageEncoder = new EventMessageEncoder();
    private EventStream eventStream;
    private boolean eventStreamUpdatable;
    private long[] eventTimesUs;
    private boolean isFormatSentDownstream;
    private long pendingSeekPositionUs = C0539C.TIME_UNSET;
    private final Format upstreamFormat;

    EventSampleStream(EventStream eventStream, Format upstreamFormat, boolean eventStreamUpdatable) {
        this.upstreamFormat = upstreamFormat;
        updateEventStream(eventStream, eventStreamUpdatable);
    }

    void updateEventStream(EventStream eventStream, boolean eventStreamUpdatable) {
        long lastReadPositionUs = this.currentIndex == 0 ? C0539C.TIME_UNSET : this.eventTimesUs[this.currentIndex - 1];
        this.eventStreamUpdatable = eventStreamUpdatable;
        this.eventStream = eventStream;
        this.eventTimesUs = eventStream.presentationTimesUs;
        if (this.pendingSeekPositionUs != C0539C.TIME_UNSET) {
            seekToUs(this.pendingSeekPositionUs);
        } else if (lastReadPositionUs != C0539C.TIME_UNSET) {
            this.currentIndex = Util.binarySearchCeil(this.eventTimesUs, lastReadPositionUs, false, false);
        }
    }

    String eventStreamId() {
        return this.eventStream.id();
    }

    public boolean isReady() {
        return true;
    }

    public void maybeThrowError() throws IOException {
    }

    public int readData(FormatHolder formatHolder, DecoderInputBuffer buffer, boolean formatRequired) {
        if (!formatRequired) {
            if (this.isFormatSentDownstream) {
                if (this.currentIndex != this.eventTimesUs.length) {
                    int sampleIndex = this.currentIndex;
                    this.currentIndex = sampleIndex + 1;
                    byte[] serializedEvent = this.eventMessageEncoder.encode(this.eventStream.events[sampleIndex], this.eventStream.timescale);
                    if (serializedEvent == null) {
                        return -3;
                    }
                    buffer.ensureSpaceForWrite(serializedEvent.length);
                    buffer.setFlags(1);
                    buffer.data.put(serializedEvent);
                    buffer.timeUs = this.eventTimesUs[sampleIndex];
                    return -4;
                } else if (this.eventStreamUpdatable) {
                    return -3;
                } else {
                    buffer.setFlags(4);
                    return -4;
                }
            }
        }
        formatHolder.format = this.upstreamFormat;
        this.isFormatSentDownstream = true;
        return -5;
    }

    public int skipData(long positionUs) {
        int newIndex = Math.max(this.currentIndex, Util.binarySearchCeil(this.eventTimesUs, positionUs, true, false));
        int skipped = newIndex - this.currentIndex;
        this.currentIndex = newIndex;
        return skipped;
    }

    public void seekToUs(long positionUs) {
        boolean z = false;
        this.currentIndex = Util.binarySearchCeil(this.eventTimesUs, positionUs, true, false);
        if (this.eventStreamUpdatable && this.currentIndex == this.eventTimesUs.length) {
            z = true;
        }
        this.pendingSeekPositionUs = z ? positionUs : C0539C.TIME_UNSET;
    }
}
