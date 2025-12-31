package clinixpay.license.service;

import clinixpay.license.dto.LicensePurchaseRequest;
import clinixpay.license.model.KeyStatus;
import clinixpay.license.model.User;
import clinixpay.license.repository.UserRepository;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Service
public class LicenseService {

    @Autowired private UserRepository userRepository;
    @Autowired private KeyGeneratorService keyGeneratorService;
    @Autowired private PaymentService paymentService;
    @Autowired private EmailService emailService;

    public User initiateLicensePurchase(LicensePurchaseRequest request) throws RazorpayException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("User with email " + request.getEmail() + " is already registered.");
        }

        Long planAmountPaise = paymentService.getPlanAmountPaise(request.getPlanId());
        String planName = paymentService.getPlanName(request.getPlanId());
        boolean isFreePlan = planAmountPaise == 0L;

        String plainLicensekey = keyGeneratorService.generateUnique12DigitKey();
        String hashedLicensekey = keyGeneratorService.hashKey(plainLicensekey);

        User newUser = new User();
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setMobileNumber(request.getMobileNumber());
        newUser.setPlanId(request.getPlanId());
        newUser.setSelectedPlan(planName);
        newUser.setPlanAmountPaise(planAmountPaise);
        newUser.setLicensekey(hashedLicensekey);

        if (isFreePlan) {
            Map.Entry<Long, ChronoUnit> validity = paymentService.getPlanValidityDuration(request.getPlanId());
            newUser.setKeyStatus(KeyStatus.ACTIVE);
            newUser.setKeyGenerationTime(LocalDateTime.now());
            newUser.setKeyExpiryTime(LocalDateTime.now().plus(validity.getKey(), validity.getValue()));

            User savedUser = userRepository.save(newUser);

            emailService.sendPaymentSuccessEmail(
                    savedUser.getEmail(),
                    savedUser.getFullName(),
                    plainLicensekey,
                    savedUser.getSelectedPlan(),
                    savedUser.getPlanAmountPaise(),
                    validity.getKey()
            );
            return savedUser;
        } else {
            newUser.setKeyStatus(KeyStatus.PENDING_PAYMENT);
            newUser.setTempPlainLoginKey(plainLicensekey);
            User savedUser = userRepository.save(newUser);
            String razorpayOrderId = paymentService.createRazorpayOrder(planAmountPaise, savedUser.getId());
            savedUser.setRazorpayOrderId(razorpayOrderId);
            return userRepository.save(savedUser);
        }
    }

    public User completeLicenseActivation(String userId, String paymentId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));

        Integer planId = user.getPlanId();
        if (planId == null) planId = 1;

        Map.Entry<Long, ChronoUnit> validity = paymentService.getPlanValidityDuration(planId);

        String plainLoginKey = user.getTempPlainLoginKey();
        user.setKeyStatus(KeyStatus.ACTIVE);
        user.setKeyGenerationTime(LocalDateTime.now());
        user.setKeyExpiryTime(LocalDateTime.now().plus(validity.getKey(), validity.getValue()));
        user.setRazorpayPaymentId(paymentId);
        user.setTempPlainLoginKey(null);

        User completedUser = userRepository.save(user);

        emailService.sendPaymentSuccessEmail(
                completedUser.getEmail(),
                completedUser.getFullName(),
                plainLoginKey,
                completedUser.getSelectedPlan(),
                completedUser.getPlanAmountPaise(),
                validity.getKey()
        );
        return completedUser;
    }

    /**
     * FIX FOR THE "CANNOT FIND SYMBOL" ERROR:
     * Added String return type and logic for ALREADY_ACTIVE status.
     */
    public String deletePendingUser(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.getKeyStatus() == KeyStatus.ACTIVE) {
                return "ALREADY_ACTIVE";
            }

            if (user.getKeyStatus() == KeyStatus.PENDING_PAYMENT) {
                userRepository.delete(user);
                return "DELETED";
            }
        }
        return "NOT_FOUND";
    }

    public User validateUserLicense(String email, String plainLicenseKey) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found"));
        if (user.getKeyStatus() != KeyStatus.ACTIVE) throw new IllegalStateException("License inactive");
        if (!keyGeneratorService.checkKey(plainLicenseKey, user.getLicensekey())) throw new IllegalStateException("Invalid key");
        return user;
    }
}