package NytePulse.backend.service.impl;

import NytePulse.backend.dto.LiveStreamResponseDTO;
import NytePulse.backend.dto.StartStreamRequestDTO;
import NytePulse.backend.dto.StreamAccessResponseDTO;
import NytePulse.backend.dto.StreamFeedItemDTO;
import NytePulse.backend.entity.LiveStream;
import NytePulse.backend.entity.User;
import NytePulse.backend.entity.UserDetails;
import NytePulse.backend.enums.StreamVisibility;
import NytePulse.backend.repository.LiveStreamRepository;
import NytePulse.backend.repository.UserDetailsRepository;
import NytePulse.backend.repository.UserRelationshipRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.centralServices.LiveStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiveStreamServiceImpl implements LiveStreamService {

    private final LiveStreamRepository liveStreamRepository;
    private final UserRepository userRepository;
    private final UserRelationshipRepository userRelationshipRepository;

    private final UserDetailsRepository userDetailsRepository;

    private final CloseFriendServiceImpl closeFriendServiceImpl;

    @Value("${ome.ingest.ip}")
    private String OME_INGEST_IP;

    @Value("${bunny.cdn.domain}")
    private String CDN_DOMAIN;

    @Override
    @Transactional
    public ResponseEntity<?> startStream(String userId, StartStreamRequestDTO request) {
        User user = userRepository.findByUserId(userId);

        liveStreamRepository.findByUser_Id(user.getId())
                .ifPresent(liveStreamRepository::delete);

        String streamKey = UUID.randomUUID().toString();

        String ingestUrl = "rtmp://" + OME_INGEST_IP + "/live/" + streamKey;
        String playbackUrl = CDN_DOMAIN + "/live/" + streamKey + ".flv";

        // Default to FOLLOWERS if visibility is null
        StreamVisibility visibility = request.getVisibility() != null
                ? request.getVisibility()
                : StreamVisibility.FOLLOWERS;

        LiveStream stream = LiveStream.builder()
                .user(user)
                .streamKey(streamKey)
                .ingestUrl(ingestUrl)
                .playbackUrl(playbackUrl)
                .visibility(visibility)
                .build();

        liveStreamRepository.save(stream);

        LiveStreamResponseDTO response = LiveStreamResponseDTO.builder()
                .streamKey(streamKey)
                .ingestUrl(ingestUrl)
                .playbackUrl(playbackUrl)
                .visibility(visibility.name())
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    @Transactional
    public void stopStream(String userId, String streamKey) {
        LiveStream stream = liveStreamRepository.findByStreamKey(streamKey)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        if (!stream.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to stop this stream");
        }

        liveStreamRepository.delete(stream);
    }


    @Override
    public ResponseEntity<?> checkStreamAccess(String viewerId) {
        List<LiveStream> allStreams = liveStreamRepository.findAll();

        User viewer = userRepository.findByUserId(viewerId);
        if (viewer == null) {
            throw new RuntimeException("Viewer not found");
        }

        List<StreamFeedItemDTO> accessibleStreams = allStreams.stream()
                .filter(stream -> isAllowedToWatch(viewer, stream))
                .map(stream -> {
                    User broadcaster = stream.getUser();
                    UserDetails broadcasterDetails = userDetailsRepository.findByUserId(broadcaster.getUserId());
                    return StreamFeedItemDTO.builder()
                            .streamKey(stream.getStreamKey())
                            .playbackUrl(stream.getPlaybackUrl())
                            .broadcasterId(broadcaster.getUserId())
                            .broadcasterName(broadcasterDetails.getName())
                            .broadcasterUsername(broadcaster.getUsername())
                            .broadcasterProfileUrl(broadcasterDetails.getProfilePicture())
                            .visibility(stream.getVisibility().name())
                            .build();
                })
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("count", accessibleStreams.size());
            response.put("streams", accessibleStreams);

        return ResponseEntity.ok(response);
    }


    private boolean isAllowedToWatch(User viewer, LiveStream stream) {
        User broadcaster = stream.getUser();

        if (broadcaster.getUserId().equals(viewer.getUserId())) {
            return true;
        }

        switch (stream.getVisibility()) {
            case EVERYONE:
                return true;

            case FOLLOWERS:
                return userRelationshipRepository
                        .existsByFollower_IdAndFollowing_Id(viewer.getId(), broadcaster.getId());

            case CLOSE_FRIENDS:
                return closeFriendServiceImpl.isCloseFriend(
                        broadcaster.getUserId(),
                        viewer.getUserId()
                );

            default:
                return false;
        }
    }
}