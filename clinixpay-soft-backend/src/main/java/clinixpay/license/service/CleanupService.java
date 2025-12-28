package clinixpay.license.service;



import clinixpay.license.model.KeyStatus;
import clinixpay.license.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleanupService {

    @Autowired
    private UserRepository userRepository;

    // Define the threshold for abandonment (e.g., 6 hours)
    private static final long ABANDONMENT_HOURS = 6;

    /**
     * Scheduled task to check and delete user records that have been
     * stuck in PENDING_PAYMENT status for longer than the abandonment threshold.
     * Runs at 0 minutes past every hour (e.g., 10:00:00, 11:00:00, etc.).
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupAbandonedPayments() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(ABANDONMENT_HOURS);

        // Find all users who are PENDING_PAYMENT and were registered before the cutoff time
        List<clinixpay.license.model.User> abandonedUsers = userRepository.findByKeyStatusAndRegistrationTimeBefore(
                KeyStatus.PENDING_PAYMENT,
                cutoffTime
        );

        if (!abandonedUsers.isEmpty()) {
            System.out.println("SCHEDULER: Found " + abandonedUsers.size() + " abandoned PENDING_PAYMENT records to delete.");
            userRepository.deleteAll(abandonedUsers);
        }
    }
}