package NytePulse.backend.service.impl;

import NytePulse.backend.dto.*;
import NytePulse.backend.entity.*;
import NytePulse.backend.repository.*;
import NytePulse.backend.service.centralServices.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    @Autowired
    private  ConversationRepository conversationRepository;
    @Autowired
    private  ConversationParticipantRepository participantRepository;
    @Autowired
    private  ChatMessageRepository messageRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  UserDetailsRepository userDetailsRepository;

    @Autowired
    private MessageStatusRepository messageStatusRepository;

    @Override
    @Transactional
    public ConversationDTO createOrGetPrivateConversation(Long userId1, Long userId2) {
        // Check if conversation already exists
        var existingConversation = conversationRepository
                .findPrivateConversationBetweenUsers(userId1, userId2);

        if (existingConversation.isPresent()) {
            return mapToConversationDTO(existingConversation.get(), userId1);
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

        return mapToConversationDTO(conversation, userId1);
    }

    @Override
    @Transactional
    public ConversationDTO createGroupConversation(Long creatorId, List<Long> participantIds, String groupName) {
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

        return mapToConversationDTO(conversation, creatorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUserId(userId);

        return conversations.stream()
                .map(conv -> mapToConversationDTO(conv, userId))
                .collect(Collectors.toList());
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

        // Update conversation timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        List<ConversationParticipant> participants = participantRepository
                .findByConversationId(conversationId);

        for (ConversationParticipant participant : participants) {
            // Don't create status for sender (only for recipients)
            if (!participant.getUser().getId().equals(senderId)) {
                MessageStatus status = new MessageStatus();
                status.setMessageId(message.getId());
                status.setUserId(participant.getUser().getId());
                status.setStatus(MessageStatus.Status.SENT);
                messageStatusRepository.save(status);

                System.out.println("✓ Created message status for user: " + participant.getUser().getId());
            }
        }

        return mapToChatMessageDTO(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getConversationMessages(Long conversationId, Long userId,
                                                        int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = messageRepository
                .findByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(conversationId, pageable);

        return messages.map(this::mapToChatMessageDTO);
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long conversationId, Long userId) {
        ConversationParticipant participant = participantRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setLastReadAt(LocalDateTime.now());
        participantRepository.save(participant);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUnreadCount(Long conversationId, Long userId) {
        Long count = messageRepository.countUnreadMessages(conversationId, userId);
        return count != null ? count.intValue() : 0;
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this message");
        }

        message.setIsDeleted(true);
        messageRepository.save(message);
    }

    // Helper methods
    private ConversationDTO mapToConversationDTO(Conversation conversation, Long currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setType(conversation.getType().name());
        dto.setUpdatedAt(conversation.getUpdatedAt());

        // Get participants
        List<ConversationParticipant> participants = participantRepository
                .findByConversationId(conversation.getId());

        List<UserBasicDTO> participantDTOs = participants.stream()
                .map(p -> {
                    UserBasicDTO userDTO = new UserBasicDTO();
                    userDTO.setId(p.getUser().getId());
                    userDTO.setUsername(p.getUser().getUsername());
//                    userDTO.setProfilePicture(p.getUser().getProfilePicture());

                    try {
                        var userDetails = userDetailsRepository.findByUsername(p.getUser().getUsername());
                        if (userDetails != null) {
                            userDTO.setName(userDetails.getName());
                        }
                    } catch (Exception e) {
                        // Ignore
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

        // Get unread count
        dto.setUnreadCount(getUnreadCount(conversation.getId(), currentUserId));

        return dto;
    }

    private ChatMessageDTO mapToChatMessageDTO(ChatMessage message) {
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
        dto.setStatus("SENT");

        return dto;
    }
}
