function extractVideoId() {
    const url = window.location.href;
    const match = url.match(/[?&]v=([^&]+)/);

    if (match && match[1]) {
        const videoId = match[1];

        console.log("Found videoId:", videoId);

        chrome.storage.local.set({ videoId });
        
        // Remove existing UI if navigating to a new video
        removeUI();
        injectButton(videoId);
    } else {
        console.log("No video ID found");
        removeUI();
        removeButton();
    }
}

function injectButton(videoId) {
    // Prevent multiple buttons, just update videoId and show it
    const existingBtn = document.getElementById('yt-classifier-btn');
    if (existingBtn) {
        existingBtn.onclick = () => {
            injectUI(videoId);
            existingBtn.style.display = 'none';
        };
        existingBtn.style.display = 'flex';
        return;
    }

    const container = document.querySelector('#secondary-inner') || document.querySelector('#secondary');
    
    if (!container) {
        setTimeout(() => injectButton(videoId), 1000);
        return;
    }

    const btn = document.createElement('button');
    btn.id = 'yt-classifier-btn';
    btn.innerHTML = 'AI Check';
    btn.style.width = '100%';
    btn.style.padding = '14px';
    btn.style.marginBottom = '16px';
    btn.style.backgroundColor = 'rgba(239, 68, 68, 0.15)'; 
    btn.style.border = '1px solid rgba(239, 68, 68, 0.3)';
    btn.style.color = '#fca5a5';
    btn.style.borderRadius = '12px';
    btn.style.fontSize = '15px';
    btn.style.fontWeight = '600';
    btn.style.cursor = 'pointer';
    btn.style.display = 'flex';
    btn.style.alignItems = 'center';
    btn.style.justifyContent = 'center';
    btn.style.transition = 'all 0.2s ease';
    btn.style.backdropFilter = 'blur(8px)';
    btn.style.fontFamily = 'Roboto, Arial, sans-serif'; 
    
    btn.onmouseover = () => {
        btn.style.backgroundColor = 'rgba(239, 68, 68, 0.25)';
        btn.style.transform = 'translateY(-1px)';
        btn.style.boxShadow = '0 4px 12px rgba(239, 68, 68, 0.2)';
    };
    btn.onmouseout = () => {
        btn.style.backgroundColor = 'rgba(239, 68, 68, 0.15)';
        btn.style.transform = 'translateY(0)';
        btn.style.boxShadow = 'none';
    };

    btn.onclick = () => {
        injectUI(videoId);
        btn.style.display = 'none'; // hide button after click
    };

    container.insertBefore(btn, container.firstChild);
}

function removeButton() {
    const existing = document.getElementById('yt-classifier-btn');
    if (existing) existing.remove();
}

function injectUI(videoId) {
    // Prevent multiple injections, just update src if it exists
    const existing = document.getElementById('yt-classifier-iframe');
    if (existing) {
        existing.src = chrome.runtime.getURL(`popup.html?videoId=${videoId}`);
        return;
    }

    // Try to find the YouTube right sidebar to inject into
    const container = document.querySelector('#secondary-inner') || document.querySelector('#secondary');
    
    if (!container) {
        // YouTube loads dynamically, retry after a delay
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

    // Insert iframe right after the hidden button (or at top)
    const btn = document.getElementById('yt-classifier-btn');
    if (btn && btn.nextSibling) {
        container.insertBefore(iframe, btn.nextSibling);
    } else {
        container.insertBefore(iframe, container.firstChild);
    }
}

function removeUI() {
    const existing = document.getElementById('yt-classifier-iframe');
    if (existing) {
        existing.remove();
    }
}

// ...existing code...
extractVideoId();

// ...existing code...
document.addEventListener('yt-navigate-finish', extractVideoId);
document.addEventListener('yt-page-data-updated', extractVideoId);

// ...existing code...
setInterval(() => {
    const url = window.location.href;
    const match = url.match(/[?&]v=([^&]+)/);
    if (match && match[1]) {
        const videoId = match[1];
        const btn = document.getElementById('yt-classifier-btn');
        const iframe = document.getElementById('yt-classifier-iframe');
        const container = document.querySelector('#secondary-inner') || document.querySelector('#secondary');
        
        // If on a video page, container exists, but our UI got wiped out, re-inject
        if (container && !btn && !iframe) {
            injectButton(videoId);
        }
    }
}, 2000);