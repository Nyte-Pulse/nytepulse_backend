// BunnyNetService.java
package NytePulse.backend.service;

import NytePulse.backend.config.BunnyNetConfig;
import NytePulse.backend.dto.BunnyNetUploadResult;
import NytePulse.backend.entity.Media;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class BunnyNetService {

    private static final Logger log = LoggerFactory.getLogger(BunnyNetService.class);

    @Autowired
    private BunnyNetConfig bunnyNetConfig;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final String PROFILE_PICTURE_FOLDER = "profile_picture";
    private static final String EVENT_POSTER_FOLDER = "event_poster";

    public BunnyNetService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public BunnyNetUploadResult uploadEventPoster(MultipartFile file, String userId) throws IOException {
        log.info("Uploading event poster for user: {}", userId);

        String fileName = generateEventPosterFileName(userId, file.getOriginalFilename());
        String uploadPath = "/" + EVENT_POSTER_FOLDER + "/" + fileName;

        String uploadUrl = bunnyNetConfig.getStorage().getBaseUrl() +
                "/" + bunnyNetConfig.getStorage().getZoneName() + uploadPath;

        log.info("Profile picture upload URL: {}", uploadUrl);

        try {
            webClient.put()
                    .uri(uploadUrl)
                    .header("AccessKey", bunnyNetConfig.getStorage().getAccessKey())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(BodyInserters.fromResource(file.getResource()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String cdnUrl = bunnyNetConfig.getStorage().getCdnUrl() + uploadPath;

            log.info("Event poster uploaded successfully: {}", cdnUrl);

            return BunnyNetUploadResult.builder()
                    .fileName(fileName)
                    .cdnUrl(cdnUrl)
                    .fileSize(file.getSize())
                    .mediaType(Media.MediaType.IMAGE)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload event poster: {}", e.getMessage());
            throw new IOException("Failed to upload event poster", e);
        }
    }

    private String generateEventPosterFileName(String userId, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return userId + "_" + System.currentTimeMillis() + extension;
    }

    /**
     * Upload profile picture to specific folder
     */
    public BunnyNetUploadResult uploadProfilePicture(MultipartFile file, String userId) throws IOException {
        log.info("Uploading profile picture for user: {}", userId);

        String fileName = generateProfilePictureFileName(userId, file.getOriginalFilename());
        String uploadPath = "/" + PROFILE_PICTURE_FOLDER + "/" + fileName;

        String uploadUrl = bunnyNetConfig.getStorage().getBaseUrl() +
                "/" + bunnyNetConfig.getStorage().getZoneName() + uploadPath;

        log.info("Profile picture upload URL: {}", uploadUrl);

        try {
            webClient.put()
                    .uri(uploadUrl)
                    .header("AccessKey", bunnyNetConfig.getStorage().getAccessKey())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(BodyInserters.fromResource(file.getResource()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String cdnUrl = bunnyNetConfig.getStorage().getCdnUrl() + uploadPath;

            log.info("Profile picture uploaded successfully: {}", cdnUrl);

            return BunnyNetUploadResult.builder()
                    .fileName(fileName)
                    .cdnUrl(cdnUrl)
                    .fileSize(file.getSize())
                    .mediaType(Media.MediaType.IMAGE)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload profile picture: {}", e.getMessage());
            throw new IOException("Failed to upload profile picture", e);
        }
    }

    /**
     * Get profile picture URL
     */
    public String getProfilePictureUrl(String fileName) {
        return bunnyNetConfig.getStorage().getCdnUrl() +
                "/" + PROFILE_PICTURE_FOLDER + "/" + fileName;
    }


    /**
     * Download profile picture as byte array
     */
    public byte[] downloadProfilePicture(String fileName) throws IOException {
        String downloadUrl = bunnyNetConfig.getStorage().getBaseUrl() +
                "/" + bunnyNetConfig.getStorage().getZoneName() +
                "/" + PROFILE_PICTURE_FOLDER + "/" + fileName;

        log.info("Downloading profile picture from: {}", downloadUrl);

        try {
            return webClient.get()
                    .uri(downloadUrl)
                    .header("AccessKey", bunnyNetConfig.getStorage().getAccessKey())
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

        } catch (Exception e) {
            log.error("Failed to download profile picture: {}", e.getMessage());
            throw new IOException("Failed to download profile picture", e);
        }
    }

    /**
     * Delete profile picture from storage
     */
    public boolean deleteProfilePicture(String fileName) {
        try {
            String deleteUrl = bunnyNetConfig.getStorage().getBaseUrl() +
                    "/" + bunnyNetConfig.getStorage().getZoneName() +
                    "/" + PROFILE_PICTURE_FOLDER + "/" + fileName;

            webClient.method(HttpMethod.DELETE)
                    .uri(deleteUrl)
                    .header("AccessKey", bunnyNetConfig.getStorage().getAccessKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully deleted profile picture: {}", fileName);
            return true;

        } catch (Exception e) {
            log.error("Failed to delete profile picture: {} - Error: {}", fileName, e.getMessage());
            return false;
        }
    }

    /**
     * Generate filename for profile pictures with userId
     */
    private String generateProfilePictureFileName(String userId, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return userId + "_" + System.currentTimeMillis() + extension;
    }

    /**
     * Update user profile picture (delete old and upload new)
     */
    public BunnyNetUploadResult updateProfilePicture(MultipartFile file, String userId, String oldFileName)
            throws IOException {

        // Delete old profile picture if exists
        if (oldFileName != null && !oldFileName.isEmpty()) {
            deleteProfilePicture(oldFileName);
        }

        // Upload new profile picture
        return uploadProfilePicture(file, userId);

    }

    /**
     * Upload image to BunnyNet Storage
     */
    public BunnyNetUploadResult uploadImage(MultipartFile file) throws IOException {
        // DEBUG: Print the configuration values
        log.info("DEBUG - Storage Access Key: {}", bunnyNetConfig.getStorage().getAccessKey());
        log.info("DEBUG - Storage Zone Name: {}", bunnyNetConfig.getStorage().getZoneName());
        log.info("DEBUG - Storage Base URL: {}", bunnyNetConfig.getStorage().getBaseUrl());
        String fileName = generateFileName(file.getOriginalFilename());
        String uploadPath = "/post_image/" + fileName;

        // Upload to BunnyNet Storage
        String uploadUrl = bunnyNetConfig.getStorage().getBaseUrl() +
                "/" + bunnyNetConfig.getStorage().getZoneName() + uploadPath;

        log.info("DEBUG - Upload URL: {}", uploadUrl);

        try {
            webClient.put()
                    .uri(uploadUrl)
                    .header("AccessKey", bunnyNetConfig.getStorage().getAccessKey())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(BodyInserters.fromResource(file.getResource()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String cdnUrl = bunnyNetConfig.getStorage().getCdnUrl() + uploadPath;

            return BunnyNetUploadResult.builder()
                    .fileName(fileName)
                    .cdnUrl(cdnUrl)
                    .fileSize(file.getSize())
                    .mediaType(Media.MediaType.IMAGE)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload image to BunnyNet: {}", e.getMessage());
            throw new IOException("Failed to upload image", e);
        }
    }

    /**
     * Upload video to BunnyNet Stream
     */
    public BunnyNetUploadResult uploadVideo(MultipartFile file, String title) throws IOException {
        try {
            // Step 1: Create video object
            String videoId = createVideoObject(title);

            // Step 2: Upload video file
            uploadVideoFile(videoId, file);

            // Generate CDN URL for video
            String cdnUrl = String.format("https://iframe.mediadelivery.net/embed/%s/%s",
                    bunnyNetConfig.getStream().getLibraryId(), videoId);

            return BunnyNetUploadResult.builder()
                    .fileName(file.getOriginalFilename())
                    .cdnUrl(cdnUrl)
                    .bunnyVideoId(videoId)
                    .fileSize(file.getSize())
                    .mediaType(Media.MediaType.VIDEO)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload video to BunnyNet Stream: {}", e.getMessage());
            throw new IOException("Failed to upload video", e);
        }
    }

    private String createVideoObject(String title) throws IOException {
        String createVideoUrl = bunnyNetConfig.getStream().getBaseUrl() +
                "/library/" + bunnyNetConfig.getStream().getLibraryId() + "/videos";

        String requestBody = String.format("{\"title\":\"%s\"}", title);

        try {
            String response = webClient.post()
                    .uri(createVideoUrl)
                    .header("AccessKey", bunnyNetConfig.getStream().getApiKey())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("guid").asText();

        } catch (Exception e) {
            log.error("Failed to create video object: {}", e.getMessage());
            throw new IOException("Failed to create video object", e);
        }
    }

    private void uploadVideoFile(String videoId, MultipartFile file) throws IOException {
        String uploadUrl = bunnyNetConfig.getStream().getBaseUrl() +
                "/library/" + bunnyNetConfig.getStream().getLibraryId() +
                "/videos/" + videoId;

        try {
            webClient.put()
                    .uri(uploadUrl)
                    .header("AccessKey", bunnyNetConfig.getStream().getApiKey())
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(BodyInserters.fromResource(file.getResource()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            log.error("Failed to upload video file: {}", e.getMessage());
            throw new IOException("Failed to upload video file", e);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    public boolean deleteImageFromStorage(String fileName) {
        try {
            String deleteUrl = bunnyNetConfig.getStorage().getBaseUrl() +
                    "/" + bunnyNetConfig.getStorage().getZoneName() +
                    "/post_image/" + fileName;

            webClient.method(HttpMethod.DELETE)
                    .uri(deleteUrl)
                    .header("AccessKey", bunnyNetConfig.getStorage().getAccessKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully deleted image from BunnyNet: {}", fileName);
            return true;

        } catch (Exception e) {
            log.error("Failed to delete image from BunnyNet: {} - Error: {}", fileName, e.getMessage());
            return false;
        }
    }

    public boolean deleteVideoFromStream(String videoId) {
        try {
            String deleteUrl = bunnyNetConfig.getStream().getBaseUrl() +
                    "/library/" + bunnyNetConfig.getStream().getLibraryId() +
                    "/videos/" + videoId;

            webClient.method(HttpMethod.DELETE)
                    .uri(deleteUrl)
                    .header("AccessKey", bunnyNetConfig.getStream().getApiKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully deleted video from BunnyNet Stream: {}", videoId);
            return true;

        } catch (Exception e) {
            log.error("Failed to delete video from BunnyNet Stream: {} - Error: {}", videoId, e.getMessage());
            return false;
        }
    }

    public boolean deleteMedia(String fileName, String bunnyVideoId, Media.MediaType mediaType) {
        if (mediaType == Media.MediaType.IMAGE) {
            return deleteImageFromStorage(fileName);
        } else if (mediaType == Media.MediaType.VIDEO && bunnyVideoId != null) {
            return deleteVideoFromStream(bunnyVideoId);
        }
        return false;
    }

    public BunnyNetUploadResult uploadImageToFolder(MultipartFile file, String folderName) throws IOException {
        log.info("DEBUG - Storage Access Key: {}", bunnyNetConfig.getStorage().getAccessKey());
        log.info("DEBUG - Storage Zone Name: {}", bunnyNetConfig.getStorage().getZoneName());
        log.info("DEBUG - Storage Base URL: {}", bunnyNetConfig.getStorage().getBaseUrl());

        String fileName = generateFileName(file.getOriginalFilename());
        String uploadPath = "/" + folderName + "/" + fileName;

        String uploadUrl = bunnyNetConfig.getStorage().getBaseUrl() +
                "/" + bunnyNetConfig.getStorage().getZoneName() + uploadPath;

        log.info("DEBUG - Upload URL: {}", uploadUrl);

        try {
            webClient.put()
                    .uri(uploadUrl)
                    .header("AccessKey", bunnyNetConfig.getStorage().getAccessKey())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(BodyInserters.fromResource(file.getResource()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String cdnUrl = bunnyNetConfig.getStorage().getCdnUrl() + uploadPath;

            return BunnyNetUploadResult.builder()
                    .fileName(fileName)
                    .cdnUrl(cdnUrl)
                    .fileSize(file.getSize())
                    .mediaType(Media.MediaType.IMAGE)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload image to BunnyNet: {}", e.getMessage());
            throw new IOException("Failed to upload image", e);
        }
    }

    public BunnyNetUploadResult uploadVideoToFolder(MultipartFile file, String title, String folderName) throws IOException {
        try {
            // Create video metadata with folder info
            String createVideoUrl = bunnyNetConfig.getStream().getBaseUrl() +
                    "/library/" + bunnyNetConfig.getStream().getLibraryId() + "/videos";

            String generatedFileName = generateFileName(file.getOriginalFilename());
            Map<String, Object> videoMetadata = new HashMap<>();
            videoMetadata.put("title", title);
            videoMetadata.put("folder", folderName);
            videoMetadata.put("fileName", generatedFileName);

            // Create video resource and get videoId
            String response = webClient.post()
                    .uri(createVideoUrl)
                    .header("AccessKey", bunnyNetConfig.getStream().getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(videoMetadata)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String videoId = parseVideoIdFromResponse(response);

            if (videoId == null) {
                throw new IOException("Failed to get video ID from BunnyNet");
            }

            // Upload video binary file (same as your existing function)
            String uploadUrl = bunnyNetConfig.getStream().getBaseUrl() +
                    "/library/" + bunnyNetConfig.getStream().getLibraryId() +
                    "/videos/" + videoId;

            webClient.put()
                    .uri(uploadUrl)
                    .header("AccessKey", bunnyNetConfig.getStream().getApiKey())
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(BodyInserters.fromResource(file.getResource()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Construct CDN URL for BunnyNet Stream playback
            String cdnUrl = "https://iframe.mediadelivery.net/embed/" +
                    bunnyNetConfig.getStream().getLibraryId() + "/" + videoId;

            // Return result
            return BunnyNetUploadResult.builder()
                    .fileName(generatedFileName)
                    .cdnUrl(cdnUrl)
                    .bunnyVideoId(videoId)
                    .fileSize(file.getSize())
                    .mediaType(Media.MediaType.VIDEO)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload video to folder: {}", e.getMessage());
            throw new IOException("Failed to upload video", e);
        }
    }



    private String parseVideoIdFromResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

            // Extract the video ID (adjust field name based on actual API response)
            String videoId = jsonNode.get("guid").asText();

            log.info("Parsed video ID: {}", videoId);
            return videoId;

        } catch (Exception e) {
            log.error("Failed to parse video ID from response: {}", e.getMessage());
            return null;
        }
    }

    public void deleteImageFromFolder(String fileName, String folderName) throws IOException {
        String deletePath = "/" + folderName + "/" + fileName;
        String deleteUrl = bunnyNetConfig.getStorage().getBaseUrl() +
                "/" + bunnyNetConfig.getStorage().getZoneName() + deletePath;

        try {
            webClient.delete()
                    .uri(deleteUrl)
                    .header("AccessKey", bunnyNetConfig.getStorage().getAccessKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Deleted image from BunnyNet: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to delete image from BunnyNet: {}", e.getMessage());
            throw new IOException("Failed to delete image", e);
        }
    }

    public void deleteVideo(String videoId) throws IOException {
        if (videoId == null) return;

        String deleteUrl = bunnyNetConfig.getStream().getBaseUrl() +
                "/library/" + bunnyNetConfig.getStream().getLibraryId() +
                "/videos/" + videoId;

        try {
            webClient.delete()
                    .uri(deleteUrl)
                    .header("AccessKey", bunnyNetConfig.getStream().getApiKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Deleted video from BunnyNet: {}", videoId);
        } catch (Exception e) {
            log.error("Failed to delete video from BunnyNet: {}", e.getMessage());
            throw new IOException("Failed to delete video", e);
        }
    }





}
