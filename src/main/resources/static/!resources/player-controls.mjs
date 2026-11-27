export function trackProgress(video, bar) {
    function update() {
        const percent = Number.isFinite(video.duration) && video.duration > 0
            ? 100 * video.currentTime / video.duration : 0;
        bar.style.width = percent + "%";
        bar.setAttribute("aria-valuenow", Math.round(percent));
    }
    video.addEventListener("timeupdate", update);
    video.addEventListener("durationchange", update);
    video.addEventListener("emptied", update);
}

export function changePlaybackRate(video, direction) {
    video.playbackRate = Math.min(16, Math.max(0.25, video.playbackRate + direction * 0.25));
}

export function zoomControl(container) {
    let width = 100;
    return function (key) {
        if (key === "=") {
            width = 100;
            container.style.left = "0px";
            container.style.top = "0px";
        } else {
            width = Math.max(100, width + (key === "+" ? 10 : -10));
        }
        container.style.width = width + "%";
    };
}
