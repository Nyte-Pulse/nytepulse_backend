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

    private static final String MUSIC_FOLDER = "music_tracks";

    public BunnyNetService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public BunnyNetUploadResult uploadMusicTrack(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        return uploadToBunnyStorage(file, fileName, MUSIC_FOLDER, Media.MediaType.AUDIO);
    }

    public String getMusicUrl(String fileName) {
        return bunnyNetConfig.getStorage().getCdnUrl() + "/" + MUSIC_FOLDER + "/" + fileName;
    }

    public BunnyNetUploadResult uploadMusicCover(MultipartFile file) throws IOException {
        String fileName = "cover_" + generateFileName(file.getOriginalFilename());
        return uploadToBunnyStorage(file, fileName, MUSIC_FOLDER, Media.MediaType.IMAGE);
    }

    private BunnyNetUploadResult uploadToBunnyStorage(MultipartFile file, String fileName, String folder, Media.MediaType mediaType) throws IOException {
        String uploadPath = "/" + folder + "/" + fileName;
        String uploadUrl = bunnyNetConfig.getStorage().getBaseUrl() +
                "/" + bunnyNetConfig.getStorage().getZoneName() + uploadPath;

        log.info("Uploading file to: {}", uploadUrl);

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
            log.info("Upload successful: {}", cdnUrl);

            return BunnyNetUploadResult.builder()
                    .fileName(fileName)
                    .cdnUrl(cdnUrl)
                    .fileSize(file.getSize())
                    .mediaType(mediaType)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload to BunnyNet: {}", e.getMessage());
            throw new IOException("Failed to upload file to " + folder, e);
        }
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

    public String getProfilePictureUrl(String fileName) {
        return bunnyNetConfig.getStorage().getCdnUrl() +
                "/" + PROFILE_PICTURE_FOLDER + "/" + fileName;
    }


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

    private String generateProfilePictureFileName(String userId, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return userId + "_" + System.currentTimeMillis() + extension;
    }

    public BunnyNetUploadResult updateProfilePicture(MultipartFile file, String userId, String oldFileName)
            throws IOException {

        // Delete old profile picture if exists
        if (oldFileName != null && !oldFileName.isEmpty()) {
            deleteProfilePicture(oldFileName);
        }

        // Upload new profile picture
        return uploadProfilePicture(file, userId);

    }

    public BunnyNetUploadResult uploadImage(MultipartFile file) throws IOException {
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

    public BunnyNetUploadResult uploadVideo(MultipartFile file, String title) throws IOException {
        try {
            // Step 1: Create video object
            String videoId = createVideoObject(title);

            // Step 2: Upload video file
            uploadVideoFile(videoId, file);

            // Generate CDN URL for video
            String cdnUrl = String.format("%s/%s/playlist.m3u8",
                    bunnyNetConfig.getStream().getPullZoneUrl(), videoId);

            String thumbnailUrl = String.format("%s/%s/thumbnail.jpg",
                    bunnyNetConfig.getStream().getPullZoneUrl(), videoId);

            return BunnyNetUploadResult.builder()
                    .fileName(file.getOriginalFilename())
                    .cdnUrl(cdnUrl)
                    .bunnyVideoId(videoId)
                    .fileSize(file.getSize())
                    .mediaType(Media.MediaType.VIDEO)
                    .thumbnailUrl(thumbnailUrl)
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
            // Step 1: Create video object with correct metadata
            String videoId = createVideoObjectInFolder(title, folderName);

            if (videoId == null) {
                throw new IOException("Failed to get video ID from BunnyNet");
            }

            // Step 2: Upload video file
            uploadVideoFile(videoId, file);

            // Step 3: Generate HLS URL (simple URL without token)
            String cdnUrl = String.format("%s/%s/playlist.m3u8",
                    bunnyNetConfig.getStream().getPullZoneUrl(), videoId);

            String generatedFileName = generateFileName(file.getOriginalFilename());

            return BunnyNetUploadResult.builder()
                    .fileName(generatedFileName)
                    .cdnUrl(cdnUrl)
                    .bunnyVideoId(videoId)
                    .fileSize(file.getSize())
                    .mediaType(Media.MediaType.VIDEO)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload video to folder: {}", e.getMessage(), e);
            throw new IOException("Failed to upload video", e);
        }
    }

    private String createVideoObjectInFolder(String title, String collectionId) throws IOException {
        String createVideoUrl = bunnyNetConfig.getStream().getBaseUrl() +
                "/library/" + bunnyNetConfig.getStream().getLibraryId() + "/videos";

        log.info("Creating video object with title: {}", title);
        log.info("API URL: {}", createVideoUrl);
        log.info("Library ID: {}", bunnyNetConfig.getStream().getLibraryId());

        // Create simple request body - only title is required
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", title);

        try {
            String response = webClient.post()
                    .uri(createVideoUrl)
                    .header("AccessKey", bunnyNetConfig.getStream().getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)  // Use bodyValue instead of body(BodyInserters...)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Video creation response: {}", response);

            JsonNode jsonNode = objectMapper.readTree(response);
            String videoId = jsonNode.get("guid").asText();

            log.info("Created video with ID: {}", videoId);
            return videoId;

        } catch (Exception e) {
            log.error("Failed to create video object: {}", e.getMessage(), e);
            throw new IOException("Failed to create video object: " + e.getMessage(), e);
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


    public BunnyNetUploadResult updateEventPoster(MultipartFile file, String event_posters, String oldFileName) {
        try {
            if (oldFileName != null && !oldFileName.isEmpty()) {
                deleteImageFromFolder(oldFileName, EVENT_POSTER_FOLDER);
            }
            
            return uploadEventPoster(file, event_posters);

        } catch (IOException e) {
            log.error("Failed to update event poster: {}", e.getMessage());
            return null;
        }
    }
}
