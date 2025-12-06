package NytePulse.backend.dto;

import NytePulse.backend.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDTO {
    private String userId;
    private PostVisibility postVisibility;
    private StoryVisibility storyVisibility;
    private CommentVisibility commentVisibility;
    private StoryCommentVisibility storyCommentVisibility;
    private MentionVisibility mentionVisibility;
    private TagVisibility tagVisibility;
    private Boolean allowDirectMessages;
    private Boolean allowMentions;
    private Boolean allowTags;
    private Boolean allowStoriesMentions;
}