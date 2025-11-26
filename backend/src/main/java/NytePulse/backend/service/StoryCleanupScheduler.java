package NytePulse.backend.service;

import NytePulse.backend.entity.Media;
import NytePulse.backend.entity.Story;
import NytePulse.backend.repository.StoryRepository;
import NytePulse.backend.service.BunnyNetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StoryCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(StoryCleanupScheduler.class);

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private BunnyNetService bunnyNetService;

    // Run every hour
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteExpiredStories() {
        log.info("Starting expired stories cleanup task");

        LocalDateTime now = LocalDateTime.now();
        List<Story> expiredStories = storyRepository.findByExpiresAtBefore(now);

        log.info("Found {} expired stories to delete", expiredStories.size());

        for (Story story : expiredStories) {
            try {
                // Delete media files from BunnyNet
                if (story.getMedia() != null) {
                    for (Media media : story.getMedia()) {
                        deleteMediaFromBunnyNet(media);
                    }
                }

                // Delete story from database (cascade will delete media records)
                storyRepository.delete(story);
                log.info("Deleted expired story ID: {}", story.getId());

            } catch (Exception e) {
                log.error("Failed to delete story ID {}: {}", story.getId(), e.getMessage());
            }
        }

        log.info("Completed expired stories cleanup task");
    }

    private void deleteMediaFromBunnyNet(Media media) {
        try {
            if (media.getMediaType() == Media.MediaType.IMAGE) {
                // Delete image from BunnyNet Storage
                bunnyNetService.deleteImageFromFolder(media.getFileName(), "stories");
            } else if (media.getMediaType() == Media.MediaType.VIDEO) {
                // Delete video from BunnyNet Stream
                bunnyNetService.deleteVideo(media.getBunnyVideoId());
            }
            log.info("Deleted media file: {}", media.getFileName());
        } catch (Exception e) {
            log.error("Failed to delete media file {}: {}", media.getFileName(), e.getMessage());
        }
    }
}
