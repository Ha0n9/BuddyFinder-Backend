package com.example.buddyfinder_backend.service;

import com.example.buddyfinder_backend.dto.ChatMessage;
import com.example.buddyfinder_backend.entity.Match;
import com.example.buddyfinder_backend.entity.Message;
import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.MatchRepository;
import com.example.buddyfinder_backend.repository.MessageRepository;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    public ChatMessage sendMessage(Long matchId, Long senderId, String content) {
        log.info("💬 Sending message - Match: {}, Sender: {}, Content: {}", matchId, senderId, content);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify sender is part of the match
        if (!match.getUser1().getUserId().equals(senderId) &&
                !match.getUser2().getUserId().equals(senderId)) {
            throw new RuntimeException("Unauthorized to send message to this match");
        }

        // Create and save message
        Message message = Message.builder()
                .match(match)
                .sender(sender)
                .content(content)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .isDeleted(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        log.info("✅ Message saved with ID: {}", savedMessage.getMessageId());

        // Update last message time in match
        match.setLastMessageAt(LocalDateTime.now());
        matchRepository.save(match);

        // ✅ FIX: Return complete ChatMessage DTO
        ChatMessage chatMessage = mapToChatMessage(savedMessage);
        log.info("📤 Returning ChatMessage: {}", chatMessage);

        return chatMessage;
    }

    public List<ChatMessage> getMessagesByMatch(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        // Verify user is part of the match
        if (!match.getUser1().getUserId().equals(userId) &&
                !match.getUser2().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to view messages");
        }

        List<Message> messages = messageRepository.findByMatch_MatchIdOrderByTimestampAsc(matchId);
        log.info("📬 Retrieved {} messages for match {}", messages.size(), matchId);

        // Mark messages as read for this user
        messages.stream()
                .filter(msg -> !msg.getSender().getUserId().equals(userId))
                .filter(msg -> !msg.getIsRead())
                .forEach(msg -> {
                    msg.setIsRead(true);
                    msg.setReadAt(LocalDateTime.now());
                    messageRepository.save(msg);
                });

        return messages.stream()
                .map(this::mapToChatMessage)
                .collect(Collectors.toList());
    }

    public Integer getUnreadCount(Long matchId, Long userId) {
        return messageRepository.countByMatch_MatchIdAndIsReadFalseAndSender_UserIdNot(matchId, userId);
    }

    private ChatMessage mapToChatMessage(Message message) {
        // ✅ FIX: Ensure all fields are populated
        ChatMessage chatMessage = ChatMessage.builder()
                .messageId(message.getMessageId())
                .matchId(message.getMatch().getMatchId())
                .senderId(message.getSender().getUserId())
                .senderName(message.getSender().getName())
                .content(message.getContent())
                .mediaUrl(message.getMediaUrl())
                .mediaType(message.getMediaType())
                .timestamp(message.getTimestamp())
                .isRead(message.getIsRead())
                .build();

        log.debug("🔄 Mapped Message {} to ChatMessage", message.getMessageId());
        return chatMessage;
    }
}