package com.chat.controllers;

import com.chat.dto.MessageDto;
import com.chat.models.Message;
import com.chat.repositories.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for message history endpoints.
 *
 * <ul>
 *   <li>{@code GET /api/messages/:uid} – returns the last 30 messages between
 *       the authenticated user and the target user, sorted oldest-first</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/messages")
public class MessagesController {

    private final MessageRepository messageRepository;

    /**
     * @param messageRepository data access for {@link Message} documents
     */
    public MessagesController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Returns the last 30 messages in the conversation between the
     * authenticated user and the specified target user, ordered oldest-first.
     *
     * @param uid            the target user's MongoDB ID (path variable)
     * @param authentication Spring Security context populated by
     *                       {@link com.chat.middleware.JwtFilter}
     * @return {@code 200} with {@code { ok: true, messages: [...] }}
     */
    @GetMapping("/{uid}")
    public ResponseEntity<?> getChat(@PathVariable String uid, Authentication authentication) {
        String myId = (String) authentication.getPrincipal();

        List<Message> last30 = messageRepository.findConversation(
                myId, uid,
                PageRequest.of(0, 30, Sort.by(Sort.Direction.ASC, "createdAt")));

        List<MessageDto> messages = last30.stream()
                .map(m -> new MessageDto(
                        m.getId(), m.getFrom(), m.getTo(),
                        m.getMessage(), m.getCreatedAt().toString(),
                        m.getUpdatedAt().toString()))
                .toList();

        return ResponseEntity.ok(Map.of("ok", true, "messages", messages));
    }
}
