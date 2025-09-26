// BunnyNetUploadResult.java
package NytePulse.backend.dto;

import NytePulse.backend.entity.Media;

public class BunnyNetUploadResult {
    private String fileName;
    private String cdnUrl;
    private String bunnyVideoId;
    private Long fileSize;
    private Media.MediaType mediaType;

    // Constructors
    public BunnyNetUploadResult() {}

    public BunnyNetUploadResult(String fileName, String cdnUrl, String bunnyVideoId,
                                Long fileSize, Media.MediaType mediaType) {
        this.fileName = fileName;
        this.cdnUrl = cdnUrl;
        this.bunnyVideoId = bunnyVideoId;
        this.fileSize = fileSize;
        this.mediaType = mediaType;
    }

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCdnUrl() {
        return cdnUrl;
    }

    public void setCdnUrl(String cdnUrl) {
        this.cdnUrl = cdnUrl;
    }

    public String getBunnyVideoId() {
        return bunnyVideoId;
    }

    public void setBunnyVideoId(String bunnyVideoId) {
        this.bunnyVideoId = bunnyVideoId;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Media.MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(Media.MediaType mediaType) {
        this.mediaType = mediaType;
    }

    // Static builder method
    public static BunnyNetUploadResultBuilder builder() {
        return new BunnyNetUploadResultBuilder();
    }

    // Builder class
    public static class BunnyNetUploadResultBuilder {
        private String fileName;
        private String cdnUrl;
        private String bunnyVideoId;
        private Long fileSize;
        private Media.MediaType mediaType;

        public BunnyNetUploadResultBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public BunnyNetUploadResultBuilder cdnUrl(String cdnUrl) {
            this.cdnUrl = cdnUrl;
            return this;
        }

        public BunnyNetUploadResultBuilder bunnyVideoId(String bunnyVideoId) {
            this.bunnyVideoId = bunnyVideoId;
            return this;
        }

        public BunnyNetUploadResultBuilder fileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public BunnyNetUploadResultBuilder mediaType(Media.MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public BunnyNetUploadResult build() {
            return new BunnyNetUploadResult(fileName, cdnUrl, bunnyVideoId, fileSize, mediaType);
        }
    }
}
