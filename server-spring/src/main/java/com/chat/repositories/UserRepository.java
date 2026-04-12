package com.chat.repositories;

import com.chat.models.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Data access interface for {@link User} documents.
 *
 */
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email is already registered.
     *
     * @param email the email address to check
     * @return {@code true} if the email is already in use
     */
    boolean existsByEmail(String email);

    /**
     * Returns all users ordered by the supplied sort criteria.
     * Typically called with {@code Sort.by(DESC, "online")} to surface
     * online users first in the contact list.
     *
     * @param sort the sort specification
     * @return list of all users in the requested order
     */
    List<User> findAll(Sort sort);
}
