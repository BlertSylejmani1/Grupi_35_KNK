import com.smartinventory.service.EmailService;
public class EmailSmokeTest {
  public static void main(String[] args) {
    EmailService email = new EmailService();
    boolean sent = email.send("Smart Inventory Java SMTP test", "This is a direct Java SMTP test from Smart Inventory.");
    System.out.println(sent ? "EMAIL_TEST_OK" : "EMAIL_TEST_FAILED: " + EmailService.lastError());
  }
}
