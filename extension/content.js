function extractVideoId() {
    const url = window.location.href;
    const match = url.match(/[?&]v=([^&]+)/);

    if (match && match[1]) {
        const videoId = match[1];

        console.log("Found videoId:", videoId);

        chrome.storage.local.set({ videoId });
        removeUI();
        injectUI(videoId);
    } else {
        console.log("No video ID found");
        removeUI();
    }
}

function injectUI(videoId) {
    const existing = document.getElementById('yt-classifier-iframe');
    if (existing) {
        existing.src = chrome.runtime.getURL(`popup.html?videoId=${videoId}`);
        return;
    }

    const container = document.querySelector('#secondary-inner') || document.querySelector('#secondary');
    
    if (!container) {
        setTimeout(() => injectUI(videoId), 1000);
        return;
    }

    const iframe = document.createElement('iframe');
    iframe.id = 'yt-classifier-iframe';
    iframe.src = chrome.runtime.getURL(`popup.html?videoId=${videoId}`);
    iframe.style.width = '100%';
    iframe.style.height = '440px'; 
    iframe.style.border = '1px solid rgba(255, 255, 255, 0.08)';
    iframe.style.borderRadius = '14px';
    iframe.style.marginBottom = '24px';
    iframe.style.backgroundColor = '#0d0404';
    iframe.style.boxShadow = '0 8px 32px rgba(0, 0, 0, 0.4)';
    iframe.style.zIndex = '9999';

    container.insertBefore(iframe, container.firstChild);
}

// removeUI removes the injected iframe from the page.
function removeUI() {
    const existing = document.getElementById('yt-classifier-iframe');
    if (existing) {
        existing.remove();
    }
}

extractVideoId();

document.addEventListener('yt-navigate-finish', extractVideoId);
document.addEventListener('yt-page-data-updated', extractVideoId);

setInterval(() => {
    const url = window.location.href;
    const match = url.match(/[?&]v=([^&]+)/);
    if (match && match[1]) {
        const videoId = match[1];
        const iframe = document.getElementById('yt-classifier-iframe');
        const container = document.querySelector('#secondary-inner') || document.querySelector('#secondary');
        
        if (container && !iframe) {
            injectUI(videoId);
        }
    }
}, 2000);