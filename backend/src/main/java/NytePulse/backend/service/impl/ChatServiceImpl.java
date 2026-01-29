package NytePulse.backend.service.impl;

import NytePulse.backend.dto.*;
import NytePulse.backend.entity.*;
import NytePulse.backend.enums.NotificationType;
import NytePulse.backend.repository.*;
import NytePulse.backend.service.NotificationService;
import NytePulse.backend.service.centralServices.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private ConversationParticipantRepository participantRepository;
    @Autowired
    private ChatMessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;
    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MessageStatusRepository messageStatusRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private static final Logger logger = LoggerFactory.getLogger(ClubServiceImpl.class);

    @Override
    @Transactional
    public ResponseEntity<?> createOrGetPrivateConversation(Long userId1, Long userId2) {
        try {
            // Check if conversation already exists
            var existingConversation = conversationRepository
                    .findPrivateConversationBetweenUsers(userId1, userId2);

            if (existingConversation.isPresent()) {
                return ResponseEntity.ok(mapToConversationDTO(existingConversation.get(), userId1));
            }

            // Create new conversation
            Conversation conversation = new Conversation();
            conversation.setType(Conversation.ConversationType.PRIVATE);
            conversation = conversationRepository.save(conversation);

            // Add participants
            User user1 = userRepository.findById(userId1)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            User user2 = userRepository.findById(userId2)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ConversationParticipant participant1 = new ConversationParticipant();
            participant1.setConversation(conversation);
            participant1.setUser(user1);
            participantRepository.save(participant1);

            ConversationParticipant participant2 = new ConversationParticipant();
            participant2.setConversation(conversation);
            participant2.setUser(user2);
            participantRepository.save(participant2);

            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Successfully created user conversations");
            response.put("conversation", mapToConversationDTO(conversation, userId1));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create Or Get Private Conversation");
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> createGroupConversation(Long creatorId, List<Long> participantIds, String groupName) {
        try{
        Conversation conversation = new Conversation();
        conversation.setType(Conversation.ConversationType.GROUP);
        conversation.setName(groupName);
        conversation = conversationRepository.save(conversation);

        // Add creator
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        ConversationParticipant creatorParticipant = new ConversationParticipant();
        creatorParticipant.setConversation(conversation);
        creatorParticipant.setUser(creator);
        participantRepository.save(creatorParticipant);

        // Add other participants
        for (Long participantId : participantIds) {
            if (!participantId.equals(creatorId)) {
                User user = userRepository.findById(participantId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                ConversationParticipant participant = new ConversationParticipant();
                participant.setConversation(conversation);
                participant.setUser(user);
                participantRepository.save(participant);
            }
        }

        return ResponseEntity.ok(mapToConversationDTO(conversation, creatorId));
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Failed to create Group Conversation");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUserConversations(Long userId) {

        try {
            List<Conversation> conversations = conversationRepository.findByUserId(userId);


            List<ConversationDTO> conversationDTOs = new ArrayList<>();
            for (Conversation conversation : conversations) {
                conversationDTOs.add(mapToConversationDTO(conversation, userId));
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Successfully fetched user conversations");
            response.put("conversations", conversationDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get User Conversations");
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    @Transactional
    public ChatMessageDTO sendMessage(Long conversationId, Long senderId, String content,
                                      String messageType, String fileUrl) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setSender(sender);
        message.setContent(content);
        message.setMessageType(ChatMessage.MessageType.valueOf(messageType));
        message.setFileUrl(fileUrl);

        message = messageRepository.save(message);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        List<ConversationParticipant> participants = participantRepository
                .findByConversationId(conversationId);

        for (ConversationParticipant participant : participants) {

            Long recipientId = participant.getUser().getId();
            if (!participant.getUser().getId().equals(senderId)) {
                MessageStatus status = new MessageStatus();
                status.setMessageId(message.getId());
                status.setUserId(participant.getUser().getId());
                status.setStatus(MessageStatus.Status.SENT);
                messageStatusRepository.save(status);

                UserDetails userDetails = userDetailsRepository.findByUsername(sender.getUsername());

                String notificationMessage = userDetails.getName() + " sent you a message";
                if (content != null && !content.isEmpty()) {
                    // Truncate long messages
                    String preview = content.length() > 50 ? content.substring(0, 50) + "..." : content;
                    notificationMessage = userDetails.getName() + " sent you a message : " + preview + " profilePicture :" + userDetails.getProfilePicture() ;
                }

                notificationService.createNotification(
                        recipientId,              // ✅ Recipient (the other person)
                        senderId,                 // ✅ Actor (the sender)
                        NotificationType.NEW_MESSAGE,
                        notificationMessage,
                        message.getId(),          // ✅ Reference to message
                        "MESSAGE"                 // ✅ Reference type
                );
            }
        }

        return mapToChatMessageDTO(message);
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getConversationMessages(Long conversationId, Long userId, int page, int size) {
        try{
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = messageRepository
                .findByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(conversationId, pageable);

        List<ChatMessageDTO> messageDTOs = messages.stream()
                .map(this::mapToChatMessageDTO)
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("messages", messageDTOs);
        response.put("currentPage", messages.getNumber());
        response.put("totalItems", messages.getTotalElements());
        response.put("status", HttpStatus.OK.value());
        response.put("totalPages", messages.getTotalPages());
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Failed to get Conversation Messages");
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    }

    @Override
    @Transactional
//    public ResponseEntity<?> markConversationAsRead(Long conversationId, Long userId) {
//        ConversationParticipant participant = participantRepository
//                .findByConversationIdAndUserId(conversationId, userId)
//                .orElseThrow(() -> new RuntimeException("Participant not found"));
//
//        participant.setLastReadAt(LocalDateTime.now());
//        participantRepository.save(participant);
//         HashMap<String,Object> res= new HashMap<>();
//        res.put("success", HttpStatus.OK.value());
//        res.put("message", "Conversation marked as read");
//        return ResponseEntity.ok(res);
//    }

    public ResponseEntity<?> markConversationAsRead(Long conversationId, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        // A. Update Participant "Last Read" Timestamp
        ConversationParticipant participant = participantRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setLastReadAt(now);
        participantRepository.save(participant);

        // B. Update Message Statuses (DB)
        // This query runs efficiently in the DB without fetching all message objects
        messageStatusRepository.markAllAsRead(userId, conversationId, now);

        // C. REAL-TIME NOTIFICATION (WebSockets)
        // Notify the OTHER participants in this conversation that this user has read the messages.

        // Payload to send via Socket
        Map<String, Object> readReceipt = new HashMap<>();
        readReceipt.put("type", "READ_RECEIPT");
        readReceipt.put("conversationId", conversationId);
        readReceipt.put("userId", userId); // Who read the messages
        readReceipt.put("readAt", now);

        // Send to a topic specific to this conversation
        // Clients subscribed to "/topic/conversation/{id}" will receive this
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, readReceipt);

        HashMap<String,Object> res= new HashMap<>();
        res.put("success", HttpStatus.OK.value());
        res.put("message", "Conversation marked as read");
        return ResponseEntity.ok(res);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUnreadConversations(Long userId) {

        List<Conversation> unreadConversations = conversationRepository
                .findConversationsWithUnreadMessages(userId);

        List<ConversationDTO> conversationDTOs = new ArrayList<>();
        for (Conversation conversation : unreadConversations) {
            conversationDTOs.add(mapToConversationDTO(conversation, userId));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Successfully fetched unread conversations");
        response.put("conversations", conversationDTOs);

        return ResponseEntity.ok(response);
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUnreadCount(Long conversationId, Long userId) {
        Long count = messageRepository.countUnreadMessages(conversationId, userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteMessage(Long messageId, Long userId) {
        try{
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this message");
        }

        message.setIsDeleted(true);
        messageRepository.save(message);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message deleted successfully"
        ));
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Failed to delete Messages");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    }

    // Helper methods
    private ConversationDTO mapToConversationDTO(Conversation conversation, Long currentUserId) {

        Long messageCount=chatMessageRepository.countByConversationId(conversation.getId());

        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setMessageCount(messageCount);
        dto.setType(conversation.getType().name());
        dto.setUpdatedAt(conversation.getUpdatedAt());

        Long unreadCount = messageStatusRepository.countUnreadMessages(conversation.getId(), currentUserId);
        dto.setUnreadCount(unreadCount != null ? unreadCount.intValue() : 0);

        System.out.println("Unread count for conversation " + conversation.getId() + ": " + unreadCount);

        // Get participants
        List<ConversationParticipant> participants = participantRepository
                .findByConversationId(conversation.getId());

        List<UserBasicDTO> participantDTOs = participants.stream()
                .map(p -> {
                    UserBasicDTO userDTO = new UserBasicDTO();
                    userDTO.setId(p.getUser().getId());
                    userDTO.setUsername(p.getUser().getUsername());

                    try {
                        UserDetails userDetails = userDetailsRepository.findByUsername(p.getUser().getUsername());
                        if (userDetails != null) {
                            userDTO.setName(userDetails.getName());
                            userDTO.setProfilePicture(userDetails.getProfilePicture());
                            userDTO.setUserId(userDetails.getUserId());
                        }
                    } catch (Exception e) {

                        logger.error("Error fetching user details for user: " + p.getUser().getUsername(), e);
                    }

                    return userDTO;
                })
                .collect(Collectors.toList());

        dto.setParticipants(participantDTOs);

        // For private chat, set name as other participant's name
        if (conversation.getType() == Conversation.ConversationType.PRIVATE) {
            UserBasicDTO otherUser = participantDTOs.stream()
                    .filter(u -> !u.getId().equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            if (otherUser != null) {
                dto.setName(otherUser.getName() != null ? otherUser.getName() : otherUser.getUsername());
                dto.setImage(otherUser.getProfilePicture());
            }
        } else {
            dto.setName(conversation.getName());
            dto.setImage(conversation.getGroupImage());
        }

        // Get last message
        var lastMessage = messageRepository.findLastMessageByConversationId(conversation.getId());
        lastMessage.ifPresent(msg -> dto.setLastMessage(mapToChatMessageDTO(msg)));
        
        return dto;
    }

    private ChatMessageDTO mapToChatMessageDTO(ChatMessage message) {
   Optional<MessageStatus> messageStatus= messageStatusRepository.findByMessageId(message.getId());
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getUsername());
//        dto.setSenderAvatar(message.getSender().getProfilePicture());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType().name());
        dto.setFileUrl(message.getFileUrl());
        dto.setTimestamp(message.getCreatedAt());
        dto.setIsEdited(message.getEditedAt() != null);
        dto.setStatus(messageStatus.get().getStatus().toString());

        return dto;
    }
}
