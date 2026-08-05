package com.ytclass.ytclassifier.controller;
import com.ytclass.ytclassifier.model.Video;
import com.ytclass.ytclassifier.model.User;
import com.ytclass.ytclassifier.model.Review;
import com.ytclass.ytclassifier.repository.VideoRepository;
import com.ytclass.ytclassifier.repository.UserRepository;
import com.ytclass.ytclassifier.repository.ReviewRepository;
import com.ytclass.ytclassifier.service.VideoService;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.servlet.ModelAndView;
@RestController
@RequestMapping("/")
public class VideoController {
    private final VideoRepository videoRepository;
    private final VideoService videoService;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    public VideoController(VideoRepository videoRepository, VideoService videoService,
                           UserRepository userRepository, ReviewRepository reviewRepository) {
        this.videoRepository = videoRepository;
        this.videoService = videoService;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }
    private String generateRandomYid() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
    private String generateUniqueYid() {
        String generated;
        do {
            generated = generateRandomYid();
        } while (userRepository.findByYid(generated).isPresent());
        return generated;
    }
    private void registerOrUpdateUser(OAuth2User oauth2User) {
        if (oauth2User != null) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                String name = oauth2User.getAttribute("name");
                String picture = oauth2User.getAttribute("picture");
                String googleId = oauth2User.getAttribute("sub");
                User user = userRepository.findById(email).orElse(new User());
                boolean isNewUser = (user.getEmail() == null);
                user.setEmail(email);
                user.setName(name);
                user.setGoogleId(googleId);
                user.setPictureUrl(picture);
                user.setProvider("google");
                user.setLastLogin(LocalDateTime.now());
                if (isNewUser) {
                    user.setCreatedAt(LocalDateTime.now());
                }
                if (user.getYid() == null || user.getYid().isEmpty()) {
                    user.setYid(generateUniqueYid());
                }
                userRepository.save(user);
            }
        }
    }
    private void addSharedAttributes(ModelAndView mav, Authentication authentication) {
        mav.addObject("userCount", userRepository.count());
        boolean loggedIn = authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
        mav.addObject("isLoggedIn", loggedIn);
        if (loggedIn && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            registerOrUpdateUser(oauth2User);
            mav.addObject("userName", oauth2User.getAttribute("name"));
            mav.addObject("userPicture", oauth2User.getAttribute("picture"));
            mav.addObject("userEmail", oauth2User.getAttribute("email"));
        }
    }
    @GetMapping("/")
    public ModelAndView landing(Authentication authentication) {
        ModelAndView mav = new ModelAndView("landing");
        addSharedAttributes(mav, authentication);
        return mav;
    }
    @GetMapping("/index")
    public ModelAndView index(Authentication authentication) {
        ModelAndView mav = new ModelAndView("index");
        addSharedAttributes(mav, authentication);
        for (User u : userRepository.findAll()) {
            if (u.getYid() == null || u.getYid().isEmpty()) {
                u.setYid(generateUniqueYid());
                userRepository.save(u);
            }
        }
        User activeUser = null;
        if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                activeUser = userRepository.findById(email).orElse(null);
            }
        }
        if (activeUser != null) {
            mav.addObject("videos", videoRepository.findByUser(activeUser));
        } else {
            mav.addObject("videos", java.util.Collections.emptyList());
        }
        return mav;
    }
    @GetMapping("/review")
    public ModelAndView review(Authentication authentication) {
        ModelAndView mav = new ModelAndView("review");
        addSharedAttributes(mav, authentication);
        mav.addObject("reviews", reviewRepository.findAllByOrderByCreatedAtDesc());
        return mav;
    }
    @GetMapping("/how-it-works")
    public ModelAndView howItWorks(Authentication authentication) {
        ModelAndView mav = new ModelAndView("how-it-works");
        addSharedAttributes(mav, authentication);
        return mav;
    }
    @GetMapping("/privacy")
    public ModelAndView privacy(Authentication authentication) {
        ModelAndView mav = new ModelAndView("privacy");
        addSharedAttributes(mav, authentication);
        return mav;
    }
    @GetMapping("/terms")
    public ModelAndView terms(Authentication authentication) {
        ModelAndView mav = new ModelAndView("terms");
        addSharedAttributes(mav, authentication);
        return mav;
    }
    @PostMapping("/review/add")
    public ModelAndView addReview(
            @RequestParam("comment") String comment,
            @RequestParam("rating") int rating,
            Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String picture = oauth2User.getAttribute("picture");
            if (rating < 1 || rating > 5) {
                rating = 5;
            }
            if (comment == null || comment.trim().isEmpty()) {
                return new ModelAndView("redirect:/review");
            }
            comment = comment.trim();
            Review review = new Review();
            review.setUserEmail(email);
            review.setUserName(name);
            review.setUserPicture(picture);
            review.setComment(comment);
            review.setRating(rating);
            review.setCreatedAt(LocalDateTime.now());
            reviewRepository.save(review);
        }
        return new ModelAndView("redirect:/review");
    }
    @GetMapping("/api/video/{videoId}")
    public org.springframework.http.ResponseEntity<?> getVideo(
            @PathVariable String videoId,
            @RequestHeader(value = "X-User-Yid", required = false) String xUserYid,
            Authentication authentication) {
        if (videoId == null || !videoId.matches("^[a-zA-Z0-9_-]{11}$")) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "Invalid YouTube video ID."));
        }
        User currentUser = null;
        if (xUserYid != null && !xUserYid.isBlank() && !xUserYid.equalsIgnoreCase("ANONYMOUS")) {
            currentUser = userRepository.findByYid(xUserYid.trim()).orElse(null);
        }
        if (currentUser == null && authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                currentUser = userRepository.findById(email).orElse(null);
            }
        }
        if (currentUser == null) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("error", "Unauthorized. Please log in to the dashboard first."));
        }
        Video video = videoService.processVideo(videoId, currentUser);
        return org.springframework.http.ResponseEntity.ok(video);
    }
    @GetMapping(value = "/ytclassifier-extension.zip")
    public org.springframework.http.ResponseEntity<byte[]> downloadExtension(
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request) {
        String yid = "ANONYMOUS";
        String name = "Anonymous";
        if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                User user = userRepository.findById(email).orElse(null);
                if (user != null) {
                    yid = user.getYid();
                    name = user.getName();
                }
            }
            if (yid == null || yid.isEmpty()) yid = "ANONYMOUS";
            if (name == null || name.isEmpty()) name = "Anonymous";
        }
        String escapedYid = yid.replace("\\", "\\\\").replace("\"", "\\\"");
        String escapedName = name.replace("\\", "\\\\").replace("\"", "\\\"");
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isBlank()) {
            scheme = request.getScheme();
        }
        String host = request.getHeader("X-Forwarded-Host");
        String apiBaseUrl;
        if (host != null && !host.isBlank()) {
            apiBaseUrl = scheme + "://" + host;
        } else {
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            apiBaseUrl = scheme + "://" + serverName;
            if (("http".equals(scheme) && serverPort != 80) || ("https".equals(scheme) && serverPort != 443)) {
                apiBaseUrl += ":" + serverPort;
            }
        }
        String configJsContent = """
        const EXTENSION_CONFIG = {
            userYid: "%s",
            userName: "%s",
            apiBaseUrl: "%s"
        };
        """.formatted(escapedYid, escapedName, apiBaseUrl);
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
             java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            String[] files = {"manifest.json", "popup.html", "popup.js", "content.js", "preview.html", "test.jpg"};
            for (String fileName : files) {
                try (java.io.InputStream is = getClass().getResourceAsStream("/extension/" + fileName)) {
                    if (is != null) {
                        zos.putNextEntry(new java.util.zip.ZipEntry(fileName));
                        is.transferTo(zos);
                        zos.closeEntry();
                    }
                }
            }
            zos.putNextEntry(new java.util.zip.ZipEntry("config.js"));
            zos.write(configJsContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.finish();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/zip"));
            headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("attachment")
                    .filename("ytclassifier-extension.zip").build());
            return new org.springframework.http.ResponseEntity<>(baos.toByteArray(), headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new org.springframework.http.ResponseEntity<>(new byte[0], org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}