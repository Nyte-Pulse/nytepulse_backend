package NytePulse.backend.entity;

import NytePulse.backend.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_visibility")
    private PostVisibility postVisibility = PostVisibility.FOLLOWERS;

    @Enumerated(EnumType.STRING)
    @Column(name = "story_visibility")
    private StoryVisibility storyVisibility = StoryVisibility.FOLLOWERS;

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_visibility")
    private CommentVisibility commentVisibility = CommentVisibility.FOLLOWERS;

    @Enumerated(EnumType.STRING)
    @Column(name = "story_comment_visibility")
    private StoryCommentVisibility storyCommentVisibility = StoryCommentVisibility.FOLLOWERS;

    @Enumerated(EnumType.STRING)
    @Column(name = "mention_visibility")
    private MentionVisibility mentionVisibility = MentionVisibility.FOLLOWERS;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_visibility")
    private TagVisibility tagVisibility = TagVisibility.FOLLOWERS;

    @Column(name = "allow_direct_messages")
    private Boolean allowDirectMessages = true;

    @Column(name = "allow_mentions")
    private Boolean allowMentions = true;

    @Column(name = "allow_tags")
    private Boolean allowTags = true;

    @Column(name = "allow_stories_mentions")
    private Boolean allowStoriesMentions = true;

    @Column(name = "notify_new_follower")
    private Boolean notifyNewFollower = true;

    @Column(name = "notify_like_post")
    private Boolean notifyLikePost = true;

    @Column(name = "notify_like_comment")
    private Boolean notifyLikeComment = true;

    @Column(name = "notify_comment_post")
    private Boolean notifyCommentPost = true;

    @Column(name = "notify_comment_story")
    private Boolean notifyCommentStory = true;

    @Column(name = "notify_mention")
    private Boolean notifyMention = true;

    @Column(name = "notify_tag")
    private Boolean notifyTag = true;

    @Column(name = "notify_share")
    private Boolean notifyShare = true;

    @Column(name = "notify_follow_request")
    private Boolean notifyFollowRequest = true;

    @Column(name = "notify_follow_request_accepted")
    private Boolean notifyFollowRequestAccepted = true;

    @Column(name = "notify_new_message")
    private Boolean notifyNewMessage = true;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
