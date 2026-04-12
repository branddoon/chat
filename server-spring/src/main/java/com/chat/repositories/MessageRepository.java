package com.chat.repositories;

import com.chat.models.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Data access interface for {@link Message} documents.
 *
 */
public interface MessageRepository extends MongoRepository<Message, String> {

    /**
     * Retrieves messages exchanged between two users, regardless of direction.
     *
     * @param uid1     ID of the first participant
     * @param uid2     ID of the second participant
     * @param pageable pagination and sort — typically limit 30, ascending by {@code createdAt}
     * @return list of messages in the conversation
     */
    @Query("{ '$or': [ { 'from': ?0, 'to': ?1 }, { 'from': ?1, 'to': ?0 } ] }")
    List<Message> findConversation(String uid1, String uid2, Pageable pageable);
}
