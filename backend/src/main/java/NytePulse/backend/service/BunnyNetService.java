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
import java.util.UUID;

@Service
public class BunnyNetService {

    // Manual logger definition
    private static final Logger log = LoggerFactory.getLogger(BunnyNetService.class);

    @Autowired
    private BunnyNetConfig bunnyNetConfig;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public BunnyNetService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
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
        String uploadPath = "/images/" + fileName;

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
                    "/images/" + fileName;

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
}
