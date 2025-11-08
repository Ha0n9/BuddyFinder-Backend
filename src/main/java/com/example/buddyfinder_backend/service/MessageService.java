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
    // COMMENT OUT for now to avoid dependency issues
    // private final NotificationService notificationService;

    public ChatMessage sendMessage(Long matchId, Long senderId, String content) {
        log.info("💬 MessageService.sendMessage called");
        log.info("   matchId: {}, senderId: {}, content: '{}'", matchId, senderId, content);

        try {
            log.info("🔍 Finding match...");
            Match match = matchRepository.findById(matchId)
                    .orElseThrow(() -> new RuntimeException("Match not found"));
            log.info("✅ Match found: {}", match.getMatchId());

            log.info("🔍 Finding sender...");
            User sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new RuntimeException("Sender not found"));
            log.info("✅ Sender found: {}", sender.getName());

            // Determine receiver
            User receiver = match.getUser1().getUserId().equals(senderId)
                    ? match.getUser2()
                    : match.getUser1();
            log.info("📮 Receiver: {}", receiver.getName());

            // Save message
            log.info("💾 Building message entity...");
            Message message = Message.builder()
                    .match(match)
                    .sender(sender)
                    .content(content)
                    .isRead(false)
                    .isDeleted(false)
                    .build();

            log.info("💾 Saving to database...");
            Message savedMessage = messageRepository.save(message);
            log.info("✅ Message saved with ID: {}", savedMessage.getMessageId());

            // ⚠️ TEMPORARILY DISABLED: Notification
            // This might be causing the exception!
            /*
            try {
                notificationService.notifyNewMessage(
                        receiver.getUserId(),
                        matchId,
                        sender.getName()
                );
                log.info("✅ Notification sent");
            } catch (Exception e) {
                log.error("⚠️ Failed to send notification (non-critical): {}", e.getMessage());
                // Don't throw - notification failure shouldn't break message sending
            }
            */

            log.info("🔄 Mapping to ChatMessage DTO...");
            ChatMessage chatMessage = mapToChatMessage(savedMessage);
            log.info("✅ ChatMessage created: {}", chatMessage);

            return chatMessage;

        } catch (Exception e) {
            log.error("❌ Error in MessageService.sendMessage:", e);
            throw e;
        }
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
        log.debug("🔄 Mapping Message ID {} to ChatMessage", message.getMessageId());

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

        log.debug("✅ Mapped to ChatMessage: {}", chatMessage);
        return chatMessage;
    }
}