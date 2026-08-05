document.addEventListener('DOMContentLoaded', () => {
    const card = document.getElementById('tilt-card');
    const scoreH = document.getElementById('live-score');
    const badgeEl = document.getElementById('live-badge');
    const summaryEl = document.getElementById('live-summary');
    const factualMetric = document.getElementById('live-metric-factual');
    const factualFill = document.getElementById('live-fill-factual');
    const sourcesMetric = document.getElementById('live-metric-sources');
    const sourcesFill = document.getElementById('live-fill-sources');
    const fallaciesMetric = document.getElementById('live-metric-fallacies');
    const fallaciesFill = document.getElementById('live-fill-fallacies');
    if (card && scoreH && badgeEl && summaryEl) {
        const wrapper = card.parentElement;
        let currentScore = 85;
        let scoreInterval = null;
        const animateToScore = (target) => {
            clearInterval(scoreInterval);
            scoreInterval = setInterval(() => {
                if (currentScore < target) currentScore++;
                else if (currentScore > target) currentScore--;
                else { clearInterval(scoreInterval); return; }
                scoreH.innerText = currentScore;
            }, 8);
        };
        wrapper.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left - rect.width / 2;
            const y = e.clientY - rect.top - rect.height / 2;
            if (x > 0 && y > 0) {
                if (!card.classList.contains('south-east-danger')) {
                    card.classList.add('south-east-danger');
                    badgeEl.innerText = "Low Credibility";
                    badgeEl.className = "trust-badge low-badge";
                    if (factualMetric) factualMetric.innerText = "42%";
                    if (factualFill) {
                        factualFill.style.width = "42%";
                        factualFill.style.backgroundColor = "var(--status-low)";
                    }
                    if (sourcesMetric) sourcesMetric.innerText = "50%";
                    if (sourcesFill) {
                        sourcesFill.style.width = "50%";
                        sourcesFill.style.backgroundColor = "var(--status-low)";
                    }
                    if (fallaciesMetric) fallaciesMetric.innerText = "Multiple Flagged";
                    if (fallaciesFill) {
                        fallaciesFill.style.width = "35%";
                        fallaciesFill.style.backgroundColor = "var(--status-low)";
                    }
                    summaryEl.innerText = "The video metadata displays multiple conflicting metrics, unverifiable claims, and logical fallacies.";
                    animateToScore(35);
                }
            } else if (card.classList.contains('south-east-danger')) {
                card.classList.remove('south-east-danger');
                badgeEl.innerText = "Highly Credible";
                badgeEl.className = "trust-badge credible-badge";
                if (factualMetric) factualMetric.innerText = "92%";
                if (factualFill) {
                    factualFill.style.width = "92%";
                    factualFill.style.backgroundColor = "var(--accent)";
                }
                if (sourcesMetric) sourcesMetric.innerText = "88%";
                if (sourcesFill) {
                    sourcesFill.style.width = "88%";
                    sourcesFill.style.backgroundColor = "var(--accent)";
                }
                if (fallaciesMetric) fallaciesMetric.innerText = "None Flagged";
                if (fallaciesFill) {
                    fallaciesFill.style.width = "100%";
                    fallaciesFill.style.backgroundColor = "var(--status-high)";
                }
                summaryEl.innerText = "The video presents well-sourced factual claims with consistent logical reasoning and deep analytical backing.";
                animateToScore(85);
            }
        });
        wrapper.addEventListener('mouseleave', () => {
            if (card.classList.contains('south-east-danger')) {
                card.classList.remove('south-east-danger');
                badgeEl.innerText = "Highly Credible";
                badgeEl.className = "trust-badge credible-badge";
                if (factualMetric) factualMetric.innerText = "92%";
                if (factualFill) {
                    factualFill.style.width = "92%";
                    factualFill.style.backgroundColor = "var(--accent)";
                }
                if (sourcesMetric) sourcesMetric.innerText = "88%";
                if (sourcesFill) {
                    sourcesFill.style.width = "88%";
                    sourcesFill.style.backgroundColor = "var(--accent)";
                }
                if (fallaciesMetric) fallaciesMetric.innerText = "None Flagged";
                if (fallaciesFill) {
                    fallaciesFill.style.width = "100%";
                    fallaciesFill.style.backgroundColor = "var(--status-high)";
                }
                summaryEl.innerText = "The video presents well-sourced factual claims with consistent logical reasoning and deep analytical backing.";
            }
            animateToScore(85);
        });
    }
});
const videoCache = {};
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.history-item').forEach(item => {
        const vid = item.getAttribute('data-id');
        const scoreStr = item.getAttribute('data-score');
        const explanation = item.getAttribute('data-explanation') || '';
        if (vid && scoreStr) {
            const score = parseInt(scoreStr, 10);
            videoCache[vid] = { credibilityScore: score, explanation };
        }
    });
});
function toggleAccordion(el) {
    const parent = el.parentElement;
    document.querySelectorAll('.accordion-row').forEach(r => {
        if (r !== parent) r.classList.remove('active');
    });
    parent.classList.toggle('active');
}
function loadHistoryItemFromData(el) {
    const id = el.getAttribute('data-id');
    const score = parseInt(el.getAttribute('data-score'), 10);
    const explanation = el.getAttribute('data-explanation');
    loadHistoryItem(id, score, explanation);
}
function extractVideoId(urlOrId) {
    if (!urlOrId) return "";
    urlOrId = urlOrId.trim();
    if (urlOrId.length === 11) return urlOrId;
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
    const match = urlOrId.match(regExp);
    return (match && match[2].length === 11) ? match[2] : "";
}
function animateScore(targetScore) {
    const ring = document.getElementById('score-ring');
    const text = document.getElementById('score-text');
    if (!ring || !text) return;
    const circumference = 402;
    ring.style.strokeDashoffset = circumference - (targetScore / 100) * circumference;
    let current = 0;
    const step = targetScore / 100;
    const counter = setInterval(() => {
        current += step;
        if (current >= targetScore) {
            text.innerText = Math.round(targetScore);
            clearInterval(counter);
        } else {
            text.innerText = Math.round(current);
        }
    }, 10);
}
function updateResultUI(score, explanation) {
    const resultPanel = document.getElementById('result-panel');
    const badge = document.getElementById('status-badge');
    const exp = document.getElementById('explanation');
    const ring = document.getElementById('score-ring');
    if (!resultPanel || !badge || !exp || !ring) return;
    resultPanel.style.display = 'block';
    exp.innerText = explanation;
    if (score >= 70) {
        badge.innerText = "Highly Credible";
        badge.className = "badge-status status-credible";
        ring.style.stroke = "var(--status-high)";
    } else if (score >= 40) {
        badge.innerText = "Questionable / Mixed";
        badge.className = "badge-status status-neutral";
        ring.style.stroke = "var(--status-mid)";
    } else if (score >= 0) {
        badge.innerText = "Low Credibility";
        badge.className = "badge-status status-low";
        ring.style.stroke = "var(--status-low)";
    } else {
        badge.innerText = "Non-News Video ℹ️";
        badge.className = "badge-status status-neutral";
        ring.style.stroke = "var(--text-muted)";
    }
    animateScore(score >= 0 ? score : 0);
}
function loadHistoryItem(id, score, explanation) {
    const input = document.getElementById('video-url-input');
    if (input) input.value = id;
    videoCache[id] = { credibilityScore: score, explanation };
    updateResultUI(score, explanation);
}
function analyzeVideo() {
    const input = document.getElementById('video-url-input');
    if (!input) return;
    const videoId = extractVideoId(input.value);
    if (!videoId) {
        alert("Please enter a valid YouTube Video URL or 11-character ID.");
        return;
    }
    if (videoCache[videoId]) {
        updateResultUI(videoCache[videoId].credibilityScore, videoCache[videoId].explanation);
        return;
    }
    const loading = document.getElementById('loading-state');
    const resultPanel = document.getElementById('result-panel');
    if (loading) loading.style.display = 'flex';
    if (resultPanel) resultPanel.style.display = 'none';
    fetch(`/api/video/${videoId}`)
        .then(r => { if (!r.ok) throw new Error("Server error"); return r.json(); })
        .then(data => {
            if (loading) loading.style.display = 'none';
            videoCache[videoId] = { credibilityScore: data.credibilityScore, explanation: data.explanation };
            updateResultUI(data.credibilityScore, data.explanation);
        })
        .catch(() => {
            if (loading) loading.style.display = 'none';
            alert("Failed to analyze video. Please try again later.");
        });
}
document.addEventListener('DOMContentLoaded', () => {
    const chartCanvas = document.getElementById('credibilityChart');
    if (!chartCanvas) return;
    const videoLabels = [];
    const videoScores = [];
    const pointColors = [];
    document.querySelectorAll('.history-item').forEach(item => {
        const vid = item.getAttribute('data-id');
        const scoreStr = item.getAttribute('data-score');
        if (vid && scoreStr) {
            const score = parseInt(scoreStr, 10);
            if (score >= 0) {
                videoLabels.push(vid);
                videoScores.push(score);
                pointColors.push(score > 50 ? '#10b981' : '#ef4444');
            }
        }
    });
    if (videoLabels.length > 0 && typeof Chart !== 'undefined') {
        const ctx = chartCanvas.getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: videoLabels,
                datasets: [{
                    label: 'Credibility Score',
                    data: videoScores,
                    borderColor: 'rgba(37, 99, 235, 0.25)',
                    segment: {
                        borderColor: ctx => ctx.p1.parsed.y > 50 ? '#10b981' : '#ef4444'
                    },
                    pointBackgroundColor: pointColors,
                    pointBorderColor: '#09090b',
                    pointRadius: 5,
                    pointHoverRadius: 7,
                    borderWidth: 2,
                    tension: 0.2
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        grid: { color: '#27272a' },
                        ticks: { color: '#a1a1aa' }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { color: '#a1a1aa' }
                    }
                },
                plugins: {
                    legend: { display: false }
                }
            }
        });
    } else {
        const parent = chartCanvas.parentElement;
        if (parent) parent.style.display = 'none';
    }
});
document.addEventListener('DOMContentLoaded', () => {
    const stars = document.querySelectorAll('.star-btn');
    const ratingInput = document.getElementById('rating-input');
    if (stars.length > 0 && ratingInput) {
        const setRating = (val) => {
            ratingInput.value = val;
            stars.forEach(star => {
                const starVal = parseInt(star.getAttribute('data-value'), 10);
                if (starVal <= val) {
                    star.classList.add('active');
                } else {
                    star.classList.remove('active');
                }
            });
        };
        setRating(5);
        stars.forEach(star => {
            star.addEventListener('click', () => {
                const val = parseInt(star.getAttribute('data-value'), 10);
                setRating(val);
            });
        });
    }
});
