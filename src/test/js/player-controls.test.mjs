import assert from "node:assert/strict";
import test from "node:test";
import { changePlaybackRate, trackProgress, zoomControl } from "../../main/resources/static/!resources/player-controls.mjs";

test("elapsed playback updates progress even after the video is fully buffered", () => {
    const video = new EventTarget();
    const bar = { style: {}, setAttribute(name, value) { this[name] = value; } };
    trackProgress(video, bar);
    video.duration = 40;
    video.currentTime = 10;
    video.dispatchEvent(new Event("timeupdate"));
    assert.equal(bar.style.width, "25%");
    assert.equal(bar["aria-valuenow"], 25);
    video.duration = NaN;
    video.dispatchEvent(new Event("emptied"));
    assert.equal(bar.style.width, "0%");
});

test("quarter-speed steps stay within the supported range", () => {
    const video = { playbackRate: 1 };
    changePlaybackRate(video, 1);
    assert.equal(video.playbackRate, 1.25);
    for (let i = 0; i < 10; i++) changePlaybackRate(video, -1);
    assert.equal(video.playbackRate, 0.25);
    for (let i = 0; i < 100; i++) changePlaybackRate(video, 1);
    assert.equal(video.playbackRate, 16);
});

test("zoom resumes at 110% after resetting a panned video", () => {
    const container = { style: { left: "-5px", top: "-10px" } };
    const zoom = zoomControl(container);
    zoom("+");
    zoom("+");
    zoom("=");
    assert.deepEqual(container.style, { left: "0px", top: "0px", width: "100%" });
    zoom("+");
    assert.equal(container.style.width, "110%");
    zoom("-");
    zoom("-");
    assert.equal(container.style.width, "100%");
});
