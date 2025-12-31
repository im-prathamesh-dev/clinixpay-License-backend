package clinixpay.license.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String mobileNumber;

    /**
     * NEW FIELD: Stores the ID of the plan selected (0, 1, or 2).
     * This ensures the system knows exactly which validity period
     * to apply after the payment is verified.
     */
    private Integer planId;

    // --- Key Generation Fields ---
    @Indexed(unique = true)
    private String licensekey;           // Stores the HASHED key for verification

    /**
     * Stores the plain key temporarily until payment is verified and email is sent.
     * Cleared (set to null) immediately after successful activation for security.
     */
    private String tempPlainLoginKey;

    private KeyStatus keyStatus = KeyStatus.PENDING_PAYMENT;
    private LocalDateTime keyGenerationTime;
    private LocalDateTime keyExpiryTime;

    // --- Payment Fields ---
    private String selectedPlan;      // Descriptive name (e.g., "Clinixpay-Gold")
    private Long planAmountPaise;     // Amount in Paise
    private String razorpayOrderId;   // ID for the transaction session
    private String razorpayPaymentId; // Final ID after successful payment

    private LocalDateTime registrationTime = LocalDateTime.now();
}