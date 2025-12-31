package clinixpay.license.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${smtp.email.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a professional HTML email with dynamic plan details and validity days.
     */
    public void sendPaymentSuccessEmail(String toEmail, String fullName, String licensekey, String planName, Long amountPaise, Long validityDays) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String amountRupees = String.format("%.2f", amountPaise / 100.0);
            String subject = "Payment Successful & Welcome to Clinixpay!";

            // Generate HTML body using the placeholder template
            String emailHtmlBody = createHtmlEmailBody(fullName, licensekey, planName, amountRupees, validityDays);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(emailHtmlBody, true);

            mailSender.send(message);
            System.out.println("License key email (" + validityDays + " days) sent successfully to: " + toEmail);

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to send success email to " + toEmail);
            throw new RuntimeException("Email delivery failed: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method using Java Text Blocks and Placeholder Replacement.
     * This avoids the "illegal text block open delimiter" compilation error.
     */
    private String createHtmlEmailBody(String fullName, String licensekey, String planName, String amountRupees, Long validityDays) {
        String template = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px;">
                <header style="background-color: #007BFF; color: white; padding: 15px; text-align: center; border-radius: 5px 5px 0 0;">
                    <h2 style="margin: 0;">CLINIXPAY - OFFICIAL CONFIRMATION</h2>
                </header>
                
                <section style="padding: 20px 0;">
                    <p>Dear <strong>{{fullName}}</strong>,</p>
                    
                    <p>We are delighted to confirm your registration for the <strong>{{planName}}</strong> plan.</p>
                    <p style="font-size: 1.1em; color: #28A745; font-weight: bold;">Amount Paid: ₹{{amountRupees}}</p>
                    <p>Your account status: <span style="color: #28A745; font-weight: bold;">ACTIVE</span></p>
                    
                    <div style="border: 2px solid #FFC107; padding: 15px; text-align: center; margin: 20px 0; background-color: #FFFBEA; border-radius: 8px;">
                        <h3 style="margin-top: 0; color: #333;">Your Secure Login Key</h3>
                        <p style="font-size: 1.5em; font-weight: bold; color: #D39E00; margin: 5px 0; letter-spacing: 2px;">{{licensekey}}</p>
                    </div>
                    
                    <p>Please use this key to log in. This key is valid for <strong>{{validityDays}} days</strong> from the moment of activation.</p>
                </section>
                
                <section style="border-top: 1px solid #ddd; padding-top: 20px;">
                    <h4 style="color: #6C757D; margin-bottom: 10px;">Terms and Conditions Summary</h4>
                    <ul style="color: #555; line-height: 1.6; padding-left: 20px;">
                        <li><strong>Key Validity:</strong> Valid for {{validityDays}} days.</li>
                        <li><strong>Refunds:</strong> All sales are final once the service is activated.</li>
                        <li><strong>Security:</strong> You are responsible for maintaining the confidentiality of your login key.</li>
                    </ul>
                </section>
                
                <footer style="margin-top: 30px; text-align: center; font-size: 0.8em; color: #6C757D; border-top: 1px solid #eee; padding-top: 15px;">
                    <p>Thank you for choosing Clinixpay.</p>
                    <p><strong>CLINIXPAY SUPPORT TEAM</strong></p>
                </footer>
            </div>
            """;

        // Replace placeholders with dynamic data
        return template
                .replace("{{fullName}}", fullName)
                .replace("{{planName}}", planName)
                .replace("{{amountRupees}}", amountRupees)
                .replace("{{licensekey}}", licensekey)
                .replace("{{validityDays}}", String.valueOf(validityDays));
    }
}