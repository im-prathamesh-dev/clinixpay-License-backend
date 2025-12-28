package clinixpay.license.repository;




import clinixpay.license.model.KeyStatus;
import clinixpay.license.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    // Finds a user by email (for checking if the user is already registered)
    Optional<User> findByEmail(String email);

    // CRITICAL: Used by KeyGeneratorService to ensure the HASH of the generated key is unique.

    Optional<User> findByLicensekey(String licenseKey);
    List<User> findByKeyStatusAndRegistrationTimeBefore(KeyStatus keyStatus, LocalDateTime registrationTime);
}