const loadingState = document.getElementById("loading-state");
const resultState = document.getElementById("result-state");
const infoState = document.getElementById("info-state");

const scoreRing = document.getElementById("score-ring");
const scoreNumber = document.getElementById("score-number");
const statusBadge = document.getElementById("status-badge");
const statusText = document.getElementById("status-text");
const explanationText = document.getElementById("explanation-text");

const infoIcon = document.getElementById("info-icon");
const infoTitle = document.getElementById("info-title");
const infoDesc = document.getElementById("info-desc");

function showState(state) {
    loadingState.classList.add("hidden");
    resultState.classList.add("hidden");
    infoState.classList.add("hidden");

    if (state === "loading") loadingState.classList.remove("hidden");
    if (state === "result") {
        resultState.classList.remove("hidden");
        resultState.classList.add("fade-in");
    }
    if (state === "info") {
        infoState.classList.remove("hidden");
        infoState.classList.add("fade-in");
    }
}

function animateRing(score) {
    const circumference = 2 * Math.PI * 65;
    const offset = circumference - (score / 100) * circumference;

    let color, glowColor;
    if (score > 70) {
        color = "#ffffff";
        glowColor = "rgba(255, 255, 255, 0.5)";
    } else if (score > 40) {
        color = "#fca5a5";
        glowColor = "rgba(252, 165, 165, 0.5)";
    } else {
        color = "#ef4444";
        glowColor = "rgba(239, 68, 68, 0.6)";
    }

    scoreRing.style.stroke = color;
    scoreRing.style.setProperty("--ring-color", glowColor);

    requestAnimationFrame(() => {
        scoreRing.style.strokeDashoffset = offset;
    });
}

function animateCounter(target) {
    let current = 0;
    const duration = 1200;
    const start = performance.now();

    function step(timestamp) {
        const elapsed = timestamp - start;
        const progress = Math.min(elapsed / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);

        current = Math.round(eased * target);
        scoreNumber.textContent = current;

        if (progress < 1) {
            requestAnimationFrame(step);
        }
    }

    requestAnimationFrame(step);
}

function setStatus(score) {
    statusBadge.classList.remove("high", "medium", "low", "neutral");

    if (score > 70) {
        statusBadge.classList.add("high");
        statusText.textContent = "Highly Credible";
    } else if (score > 40) {
        statusBadge.classList.add("medium");
        statusText.textContent = "Moderate Credibility";
    } else {
        statusBadge.classList.add("low");
        statusText.textContent = "Low Credibility";
    }
}

function showInfo(title, desc) {
    infoTitle.textContent = title;
    infoDesc.textContent = desc;
    showState("info");
}

async function run() {
    try {
        if (typeof EXTENSION_CONFIG !== 'undefined' && EXTENSION_CONFIG.userYid && EXTENSION_CONFIG.userYid !== "ANONYMOUS") {
            const userDisplay = document.getElementById("user-info-display");
            if (userDisplay) {
                userDisplay.textContent = `User: ${EXTENSION_CONFIG.userName || EXTENSION_CONFIG.userYid}`;
            }
        }

        showState("loading");

        const urlParams = new URLSearchParams(window.location.search);
        let videoId = urlParams.get('videoId');

        if (!videoId) {
            const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
            
            if (!tab || !tab.url || !tab.url.includes("youtube.com/watch")) {
                showInfo("Not a Video Page", "Navigate to a YouTube video to analyze its credibility.");
                return;
            }

            const url = new URL(tab.url);
            videoId = url.searchParams.get("v");
        }

        if (!videoId) {
            showInfo("No Video Found", "Could not extract the video ID from this URL.");
            return;
        }

        const headers = {};
        if (typeof EXTENSION_CONFIG !== 'undefined' && EXTENSION_CONFIG.userYid) {
            headers['X-User-Yid'] = EXTENSION_CONFIG.userYid;
        }

        let baseUrl = "http://localhost:8080";
        if (typeof EXTENSION_CONFIG !== 'undefined' && EXTENSION_CONFIG.apiBaseUrl) {
            baseUrl = EXTENSION_CONFIG.apiBaseUrl;
        }

        const res = await fetch(`${baseUrl}/api/video/${videoId}`, { 
            headers: headers,
            credentials: 'include' 
        });
        const data = await res.json();

        console.log("API RESPONSE:", data);

        if (!data || data.error) {
            showInfo("API Error", data?.error || "Something went wrong while contacting the server.");
            return;
        }

        if (typeof data.credibilityScore === "undefined") {
            showInfo("Invalid Response", "The backend returned an unexpected format.");
            return;
        }

        if (data.credibilityScore === -1) {
            showInfo("Not Informational", data.explanation || "This video is not an informational video.");
            return;
        }

        if (data.credibilityScore === 0) {
            showInfo("Unavailable", data.explanation || "Could not analyze this video.");
            return;
        }

        showState("result");
        animateRing(data.credibilityScore);
        animateCounter(data.credibilityScore);
        setStatus(data.credibilityScore);
        explanationText.textContent = data.explanation || "No explanation provided.";

    } catch (err) {
        console.error(err);
        showInfo("Connection Error", "Could not reach the backend. Make sure the Spring Boot server is running on localhost:8080 or https://ytclassifier.xyz.");
    }
}

run();