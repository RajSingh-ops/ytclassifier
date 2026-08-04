# YT-Classifier

Live at [ytclassifer.xyz](https://ytclassifer.xyz)

YT-Classifier is a deployed web app for checking the credibility of YouTube videos. It analyzes video context with AI, scores the content for reliability, and gives users a clear breakdown of factual confidence, bias signals, logical consistency, and source quality.

The goal is simple: help viewers make a faster, better-informed judgment before trusting or sharing a video.

## What It Does

- Checks YouTube videos for credibility using AI-assisted analysis.
- Produces a readable score with an explanation instead of a vague label.
- Highlights signals such as bias, factual risk, missing sourcing, and consistency.
- Lets signed-in users access the dashboard with Google login.
- Provides a Chrome extension experience that brings credibility checks directly onto YouTube.
- Keeps reviewed videos and scores available through the app dashboard.

## Use The App

Visit [ytclassifer.xyz](https://ytclassifer.xyz), sign in with Google, and use the dashboard to access the extension and review analyzed videos.

Once the extension is connected, open a YouTube video and run a credibility check from the page. The result appears alongside the video so the analysis stays in context while you watch.

## Product Flow

1. Sign in to the YT-Classifier dashboard.
2. Add the browser extension from the dashboard.
3. Open a YouTube video.
4. Run a credibility check.
5. Review the score, reasoning, and saved analysis from the app.

## Built With

- Java and Spring Boot
- Spring Security with Google OAuth2
- Spring Data JPA
- SQLite persistence in production
- Thymeleaf dashboard views
- Chrome Extension Manifest V3
- OpenRouter AI and YouTube Data API integrations

