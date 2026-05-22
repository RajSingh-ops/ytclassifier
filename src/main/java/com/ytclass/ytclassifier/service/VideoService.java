package com.ytclass.ytclassifier.service;

import com.ytclass.ytclassifier.model.Video;
import com.ytclass.ytclassifier.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.*;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final com.ytclass.ytclassifier.repository.UserRepository userRepository;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    public VideoService(VideoRepository videoRepository, com.ytclass.ytclassifier.repository.UserRepository userRepository) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // 🔥 MAIN ENTRY
    // =========================

    public Video processVideo(String videoId) {
        return processVideo(videoId, getFirstUser());
    }

    public Video processVideo(String videoId, com.ytclass.ytclassifier.model.User user) {
        java.util.Optional<Video> existing = videoRepository.findById(videoId);
        if (existing.isPresent()) {
            Video video = existing.get();
            if (user != null && !video.getUsers().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
                video.getUsers().add(user);
                return videoRepository.save(video);
            }
            return video;
        }
        return processNewVideo(videoId, user);
    }

    private Video processNewVideo(String videoId, com.ytclass.ytclassifier.model.User user) {
        // 🔥 Step 1: Metadata
        VideoMeta meta = getVideoMeta(videoId);

        String type = classifyType(meta.categoryId, meta.title, meta.description);

        Video newVideo = new Video();
        newVideo.setVideoId(videoId);
        newVideo.setTitle(meta.title);
        newVideo.setChannelName(meta.channelTitle);
        if (user != null) {
            newVideo.getUsers().add(user);
        }

        // 🔥 Step 2: Skip non-news BUT SAVE
        if (!type.equals("news")) {
            String msg = "This is a " + type + " video, not a news video";
            newVideo.setCredibilityScore(-1);
            newVideo.setExplanation(msg);
            return videoRepository.save(newVideo);
        }

        // 🔥 Step 3: Direct Prompt (Video URL + Metadata)
        String videoUrl = "https://www.youtube.com/watch?v=" + videoId;
        String prompt = """
Return EXACTLY:

Score: <number>/100
Explanation: <one short sentence>

Please evaluate the credibility based on the video link and its metadata (Title and Description). Consider opinions and commentary, but focus heavily on verifying how truthful the stated facts are.

Video Link: %s
Title: %s
Description: %s
""".formatted(videoUrl, meta.title, meta.description);

        // 🔥 Step 4: AI
        String aiResponse = callAI(prompt);

        int score = extractScore(aiResponse);
        String explanation = extractExplanation(aiResponse);

        // 🔥 Safety fallback
        if (score == -1) {
            score = 0;
            explanation = "Could not evaluate credibility";
        }

        newVideo.setCredibilityScore(score);
        newVideo.setExplanation(explanation);
        return videoRepository.save(newVideo);
    }

    private com.ytclass.ytclassifier.model.User getFirstUser() {
        return userRepository.findFirstByOrderByCreatedAtAsc().orElse(null);
    }

    // =========================
    // 🔥 YOUTUBE META
    // =========================

    private VideoMeta getVideoMeta(String videoId) {
        try {
            String url = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id="
                    + videoId + "&key=" + youtubeApiKey;

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();

            org.json.JSONObject json = new org.json.JSONObject(body);
            org.json.JSONArray items = json.optJSONArray("items");
            
            if (items != null && items.length() > 0) {
                org.json.JSONObject snippet = items.getJSONObject(0).getJSONObject("snippet");
                String categoryId = snippet.optString("categoryId", "0");
                String title = snippet.optString("title", "");
                String description = snippet.optString("description", "");
                String channelTitle = snippet.optString("channelTitle", "");
                
                return new VideoMeta(categoryId, title, description, channelTitle);
            }

            return new VideoMeta("0", "", "", "");

        } catch (Exception e) {
            return new VideoMeta("0", "", "", "");
        }
    }

    private String extract(String text, String regex) {
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : "";
    }

    // =========================
    // 🔥 TYPE CLASSIFIER
    // =========================

    private String classifyType(String categoryId, String title, String description) {

        String text = (title + " " + description).toLowerCase();

        if (categoryId.equals("10")) return "music";
        if (categoryId.equals("24")) return "entertainment";
        if (categoryId.equals("17")) return "sports";
        if (categoryId.equals("23")) return "comedy";

        if (categoryId.equals("25") || text.contains("news")) return "news";
        if (categoryId.equals("27") || text.contains("tutorial") || text.contains("learn")) return "education";
        if (categoryId.equals("28") || text.contains("ai") || text.contains("programming")) return "technology";

        return "other";
    }



    // =========================
    // 🔥 AI CALL (OpenRouter)
    // =========================

    private String callAI(String prompt) {
        try {

            String requestBody = """
            {
              "model": "openai/gpt-oss-120b:free",
              "messages": [
                {"role": "user", "content": "%s"}
              ]
            }
            """.formatted(prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", ""));

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();

            System.out.println("RAW AI RESPONSE: " + body);

            org.json.JSONObject json = new org.json.JSONObject(body);

            // 🔥 handle API error properly
            if (json.has("error")) {
                String openRouterError = json.getJSONObject("error").optString("message", "Unknown OpenRouter Error");
                return "Score: 0/100\nExplanation: " + openRouterError;
            }

            Object contentObj = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .get("content");

            String content;

            if (contentObj instanceof String) {
                content = (String) contentObj;
            } else if (contentObj instanceof org.json.JSONArray arr) {
                content = arr.getJSONObject(0).getString("text");
            } else {
                return "Score: 0/100\nExplanation: Invalid AI response";
            }

            return content;

        } catch (Exception e) {
            return "Score: 0/100\nExplanation: AI failed";
        }
    }

    // =========================
    // 🔥 PARSING
    // =========================

    private int extractScore(String response) {
        try {
            Pattern pattern = Pattern.compile("Score:\\s*(\\d{1,3})");
            Matcher matcher = pattern.matcher(response);

            if (matcher.find()) {
                return Math.min(Integer.parseInt(matcher.group(1)), 100);
            }

            return -1;

        } catch (Exception e) {
            return -1;
        }
    }

    private String extractExplanation(String response) {
        try {
            String[] parts = response.split("Explanation:");

            if (parts.length > 1) {
                return parts[1].trim();
            }

            return "No explanation";

        } catch (Exception e) {
            return "Error extracting explanation";
        }
    }

    // =========================
    // 🔥 HELPER
    // =========================

    private static class VideoMeta {
        String categoryId;
        String title;
        String description;
        String channelTitle;

        public VideoMeta(String categoryId, String title, String description, String channelTitle) {
            this.categoryId = categoryId;
            this.title = title;
            this.description = description;
            this.channelTitle = channelTitle;
        }
    }
}