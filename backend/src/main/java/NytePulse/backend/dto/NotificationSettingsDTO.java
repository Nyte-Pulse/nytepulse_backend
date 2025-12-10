package NytePulse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsDTO {
    private Boolean notifyNewFollower;
    private Boolean notifyLikePost;
    private Boolean notifyLikeComment;
    private Boolean notifyCommentPost;
    private Boolean notifyCommentStory;
    private Boolean notifyMention;
    private Boolean notifyTag;
    private Boolean notifyShare;
    private Boolean notifyFollowRequest;
    private Boolean notifyFollowRequestAccepted;
}
