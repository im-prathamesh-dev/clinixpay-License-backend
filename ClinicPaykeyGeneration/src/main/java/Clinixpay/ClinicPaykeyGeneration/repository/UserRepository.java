package Clinixpay.ClinicPaykeyGeneration.repository;

import Clinixpay.ClinicPaykeyGeneration.model.KeyStatus; // <--- NEW IMPORT
import Clinixpay.ClinicPaykeyGeneration.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime; // <--- NEW IMPORT
import java.util.List; // <--- NEW IMPORT
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    // Finds a user by email (for checking if the user is already registered)
    Optional<User> findByEmail(String email);

    // CRITICAL: Used by KeyGeneratorService to ensure the HASH of the generated key is unique.
    Optional<User> findByLicensekey(String licenseKey);

    /**
     * Finds users that are stuck in PENDING_PAYMENT and were registered before a specific time.
     * Used by the scheduled cleanup service.
     */
    List<User> findByKeyStatusAndRegistrationTimeBefore(KeyStatus keyStatus, LocalDateTime registrationTime);
}