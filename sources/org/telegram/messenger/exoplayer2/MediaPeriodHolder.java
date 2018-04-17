package org.telegram.messenger.exoplayer2;

import android.util.Log;
import org.telegram.messenger.exoplayer2.source.ClippingMediaPeriod;
import org.telegram.messenger.exoplayer2.source.EmptySampleStream;
import org.telegram.messenger.exoplayer2.source.MediaPeriod;
import org.telegram.messenger.exoplayer2.source.MediaSource;
import org.telegram.messenger.exoplayer2.source.SampleStream;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelection;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelectionArray;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelector;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelectorResult;
import org.telegram.messenger.exoplayer2.upstream.Allocator;
import org.telegram.messenger.exoplayer2.util.Assertions;

final class MediaPeriodHolder {
    private static final String TAG = "MediaPeriodHolder";
    public boolean hasEnabledTracks;
    public MediaPeriodInfo info;
    public final boolean[] mayRetainStreamFlags;
    public final MediaPeriod mediaPeriod;
    private final MediaSource mediaSource;
    public MediaPeriodHolder next;
    private TrackSelectorResult periodTrackSelectorResult;
    public boolean prepared;
    private final RendererCapabilities[] rendererCapabilities;
    public long rendererPositionOffsetUs;
    public final SampleStream[] sampleStreams;
    private final TrackSelector trackSelector;
    public TrackSelectorResult trackSelectorResult;
    public final Object uid;

    public MediaPeriodHolder(RendererCapabilities[] rendererCapabilities, long rendererPositionOffsetUs, TrackSelector trackSelector, Allocator allocator, MediaSource mediaSource, Object uid, MediaPeriodInfo info) {
        this.rendererCapabilities = rendererCapabilities;
        this.rendererPositionOffsetUs = rendererPositionOffsetUs - info.startPositionUs;
        this.trackSelector = trackSelector;
        this.mediaSource = mediaSource;
        this.uid = Assertions.checkNotNull(uid);
        this.info = info;
        this.sampleStreams = new SampleStream[rendererCapabilities.length];
        this.mayRetainStreamFlags = new boolean[rendererCapabilities.length];
        MediaPeriod mediaPeriod = mediaSource.createPeriod(info.id, allocator);
        if (info.endPositionUs != Long.MIN_VALUE) {
            ClippingMediaPeriod clippingMediaPeriod = new ClippingMediaPeriod(mediaPeriod, true);
            clippingMediaPeriod.setClipping(0, info.endPositionUs);
            mediaPeriod = clippingMediaPeriod;
        }
        this.mediaPeriod = mediaPeriod;
    }

    public long toRendererTime(long periodTimeUs) {
        return periodTimeUs + getRendererOffset();
    }

    public long toPeriodTime(long rendererTimeUs) {
        return rendererTimeUs - getRendererOffset();
    }

    public long getRendererOffset() {
        return this.rendererPositionOffsetUs;
    }

    public boolean isFullyBuffered() {
        return this.prepared && (!this.hasEnabledTracks || this.mediaPeriod.getBufferedPositionUs() == Long.MIN_VALUE);
    }

    public long getDurationUs() {
        return this.info.durationUs;
    }

    public long getBufferedPositionUs(boolean convertEosToDuration) {
        if (!this.prepared) {
            return this.info.startPositionUs;
        }
        long bufferedPositionUs = this.mediaPeriod.getBufferedPositionUs();
        long j = (bufferedPositionUs == Long.MIN_VALUE && convertEosToDuration) ? this.info.durationUs : bufferedPositionUs;
        return j;
    }

    public long getNextLoadPositionUs() {
        return !this.prepared ? 0 : this.mediaPeriod.getNextLoadPositionUs();
    }

    public TrackSelectorResult handlePrepared(float playbackSpeed) throws ExoPlaybackException {
        this.prepared = true;
        selectTracks(playbackSpeed);
        long newStartPositionUs = applyTrackSelection(this.info.startPositionUs, false);
        this.rendererPositionOffsetUs += this.info.startPositionUs - newStartPositionUs;
        this.info = this.info.copyWithStartPositionUs(newStartPositionUs);
        return this.trackSelectorResult;
    }

    public void reevaluateBuffer(long rendererPositionUs) {
        if (this.prepared) {
            this.mediaPeriod.reevaluateBuffer(toPeriodTime(rendererPositionUs));
        }
    }

    public void continueLoading(long rendererPositionUs) {
        this.mediaPeriod.continueLoading(toPeriodTime(rendererPositionUs));
    }

    public boolean selectTracks(float playbackSpeed) throws ExoPlaybackException {
        TrackSelectorResult selectorResult = this.trackSelector.selectTracks(this.rendererCapabilities, this.mediaPeriod.getTrackGroups());
        int i = 0;
        if (selectorResult.isEquivalent(this.periodTrackSelectorResult)) {
            return false;
        }
        this.trackSelectorResult = selectorResult;
        TrackSelection[] all = this.trackSelectorResult.selections.getAll();
        int length = all.length;
        while (i < length) {
            TrackSelection trackSelection = all[i];
            if (trackSelection != null) {
                trackSelection.onPlaybackSpeed(playbackSpeed);
            }
            i++;
        }
        return true;
    }

    public long applyTrackSelection(long positionUs, boolean forceRecreateStreams) {
        return applyTrackSelection(positionUs, forceRecreateStreams, new boolean[this.rendererCapabilities.length]);
    }

    public long applyTrackSelection(long positionUs, boolean forceRecreateStreams, boolean[] streamResetFlags) {
        TrackSelectionArray trackSelections = this.trackSelectorResult.selections;
        int i = 0;
        while (true) {
            boolean z = true;
            if (i >= trackSelections.length) {
                break;
            }
            boolean[] zArr = r0.mayRetainStreamFlags;
            if (forceRecreateStreams || !r0.trackSelectorResult.isEquivalent(r0.periodTrackSelectorResult, i)) {
                z = false;
            }
            zArr[i] = z;
            i++;
        }
        disassociateNoSampleRenderersWithEmptySampleStream(r0.sampleStreams);
        updatePeriodTrackSelectorResult(r0.trackSelectorResult);
        long positionUs2 = r0.mediaPeriod.selectTracks(trackSelections.getAll(), r0.mayRetainStreamFlags, r0.sampleStreams, streamResetFlags, positionUs);
        associateNoSampleRenderersWithEmptySampleStream(r0.sampleStreams);
        r0.hasEnabledTracks = false;
        for (int i2 = 0; i2 < r0.sampleStreams.length; i2++) {
            if (r0.sampleStreams[i2] != null) {
                Assertions.checkState(r0.trackSelectorResult.renderersEnabled[i2]);
                if (r0.rendererCapabilities[i2].getTrackType() != 5) {
                    r0.hasEnabledTracks = true;
                }
            } else {
                Assertions.checkState(trackSelections.get(i2) == null);
            }
        }
        return positionUs2;
    }

    public void release() {
        updatePeriodTrackSelectorResult(null);
        try {
            if (this.info.endPositionUs != Long.MIN_VALUE) {
                this.mediaSource.releasePeriod(((ClippingMediaPeriod) this.mediaPeriod).mediaPeriod);
            } else {
                this.mediaSource.releasePeriod(this.mediaPeriod);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Period release failed.", e);
        }
    }

    private void updatePeriodTrackSelectorResult(TrackSelectorResult trackSelectorResult) {
        if (this.periodTrackSelectorResult != null) {
            disableTrackSelectionsInResult(this.periodTrackSelectorResult);
        }
        this.periodTrackSelectorResult = trackSelectorResult;
        if (this.periodTrackSelectorResult != null) {
            enableTrackSelectionsInResult(this.periodTrackSelectorResult);
        }
    }

    private void enableTrackSelectionsInResult(TrackSelectorResult trackSelectorResult) {
        for (int i = 0; i < trackSelectorResult.renderersEnabled.length; i++) {
            boolean rendererEnabled = trackSelectorResult.renderersEnabled[i];
            TrackSelection trackSelection = trackSelectorResult.selections.get(i);
            if (rendererEnabled && trackSelection != null) {
                trackSelection.enable();
            }
        }
    }

    private void disableTrackSelectionsInResult(TrackSelectorResult trackSelectorResult) {
        for (int i = 0; i < trackSelectorResult.renderersEnabled.length; i++) {
            boolean rendererEnabled = trackSelectorResult.renderersEnabled[i];
            TrackSelection trackSelection = trackSelectorResult.selections.get(i);
            if (rendererEnabled && trackSelection != null) {
                trackSelection.disable();
            }
        }
    }

    private void disassociateNoSampleRenderersWithEmptySampleStream(SampleStream[] sampleStreams) {
        for (int i = 0; i < this.rendererCapabilities.length; i++) {
            if (this.rendererCapabilities[i].getTrackType() == 5) {
                sampleStreams[i] = null;
            }
        }
    }

    private void associateNoSampleRenderersWithEmptySampleStream(SampleStream[] sampleStreams) {
        int i = 0;
        while (i < this.rendererCapabilities.length) {
            if (this.rendererCapabilities[i].getTrackType() == 5 && this.trackSelectorResult.renderersEnabled[i]) {
                sampleStreams[i] = new EmptySampleStream();
            }
            i++;
        }
    }
}
