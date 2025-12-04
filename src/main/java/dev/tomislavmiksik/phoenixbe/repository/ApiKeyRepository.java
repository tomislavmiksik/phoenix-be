package dev.tomislavmiksik.phoenixbe.repository;

import dev.tomislavmiksik.phoenixbe.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The interface Api key repository.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    /**
     * Find api key by label optional.
     *
     * @param label the label
     * @return the optional value of API KEY if found
     */
    Optional<ApiKey> findApiKeyByLabel(String label);

    /**
     * Find by key hash and active true optional.
     *
     * @param KeyHash the key hash
     * @return the optional
     */
    Optional<ApiKey> findByKeyHashAndActiveTrue(String KeyHash);
}
