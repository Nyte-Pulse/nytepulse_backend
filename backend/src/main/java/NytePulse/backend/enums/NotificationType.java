package NytePulse.backend.enums;

public enum NotificationType {
    NEW_FOLLOWER,           // Someone followed you
    LIKE_POST,              // Someone liked your post
    LIKE_COMMENT,           // Someone liked your comment
    COMMENT_POST,           // Someone commented on your post
    COMMENT_STORY,          // Someone commented on your story
    MENTION_POST,           // Someone mentioned you in a post
    MENTION_COMMENT,        // Someone mentioned you in a comment
    TAG_POST,               // Someone tagged you in a post
    SHARE_POST,             // Someone shared your post
    STORY_VIEW,             // Someone viewed your story
    FOLLOW_REQUEST,         // Someone requested to follow you (private account)
    FOLLOW_REQUEST_ACCEPTED // Your follow request was accepted
}
