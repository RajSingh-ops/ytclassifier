# YouTube AI Credibility Classifier (YT-Classifier)

An AI-powered tool designed to analyze the credibility of YouTube videos directly within your web browser. Using advanced Large Language Models (via Google Gemini/OpenRouter), the application scans video transcripts for factual accuracy, bias, logical consistency, and source referencing, then provides a detailed credibility score and explanation.

---

## 🌟 Features

### 1. Spring Boot 3+ / 4.0 Backend
- **RESTful API**: Serves endpoints like `/api/video/{videoId}` to process and retrieve credibility analysis.
- **Spring Security & Google OAuth2**: Secures the user dashboard with a seamless Google Sign-in flow.
- **Database Persistence**: Stores analysis logs and video scores securely (using H2/SQLite).
- **CORS Configured**: Fully open to communication from Google Chrome extensions.

### 2. Chrome Extension
- **Native Sidebar Integration**: Injects a premium **"Credibility Check"** button directly into YouTube's watch page sidebar.
- **In-Page Glassmorphic UI**: Displays analysis results (SVG circular score progress ring, count-up animation, status badges, and AI breakdown text) natively in the sidebar without popping up new windows.
- **Toolbar Control Panel**: Includes an active ON/OFF toggle switch in the browser toolbar popup to quickly enable or disable the extension.

---

## 🛠️ Technology Stack

- **Backend**: Java 25, Spring Boot, Spring Security (OAuth2), Spring Data JPA, Maven.
- **Database**: H2 (In-memory) / SQLite (Persistent).
- **Frontend (Web Dashboard)**: Thymeleaf templates, CSS Grid/Flexbox, dynamic JS.
- **Extension**: Vanilla JS, Chrome Extensions Manifest V3, SVG animations, CSS Glassmorphism.

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java Development Kit (JDK)**: Version 25 or higher.
- **Google Client ID & Secret**: For OAuth2 login (configured on Google Cloud Console).
- **AI API Keys**: Gemini API Key or OpenRouter API Key.

### 2. Configure Environment Variables
Create a `.env` file in the root directory and add the following keys (see `.env.example`):
```properties
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GEMINI_API_KEY=your-gemini-api-key
```

### 3. Run the Backend Server
Start the Spring Boot server using the Maven wrapper:
```bash
# On Windows
./mvnw.cmd spring-boot:run

# On Linux/macOS
./mvnw spring-boot:run
```
The server will start on `http://localhost:8080` (or `https://ytclassifier.xyz` in production).

### 4. Install the Chrome Extension
1. Open Google Chrome and navigate to `chrome://extensions/`.
2. Enable **Developer mode** (toggle in the top-right corner).
3. Click **Load unpacked** in the top-left corner.
4. Select the `extension/` directory of this project.

Alternatively, visit the web dashboard at `http://localhost:8080` or `https://ytclassifier.xyz` (after logging in) and click **Download Extension** to download a compiled `.zip` file of the extension.

---

## 📖 How to Use

1. Ensure the Spring Boot backend is running.
2. Navigate to any informational or news-related video on **YouTube**.
3. Locate the red **"Credibility Check"** button at the top of the recommended video sidebar.
4. Click the button to initiate scanning. The extension will fetch the video transcript, analyze it, and display the credibility score and breakdown directly on the page.
5. Dismiss the card at any time using the close (`×`) button in the corner to restore the check button.
