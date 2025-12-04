package dev.tomislavmiksik.phoenixbe.service;


import dev.tomislavmiksik.phoenixbe.dto.keygen.ApiKeyResponse;
import dev.tomislavmiksik.phoenixbe.entity.ApiKey;

/**
 * The interface of Admin service.
 */
public interface AdminService {

    /**
     * Create api key api key.
     *
     * @param label the label
     * @return the api key
     */
     ApiKeyResponse createApiKey(String label);
}
