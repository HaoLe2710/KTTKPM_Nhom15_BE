import org.springframework.security.crypto.bcrypt.BCrypt;
public class CheckHash {
  public static void main(String[] args) {
    String hash = "$2a$10$8ONRaEf7/V3YnLm9ify.BOoRXTJFNp9L0Q8gx24HZ5zUv1748xvGK";
    String[] c = {"admin123", "admin", "123456", "12345678", "Admin@123", "admin@123", "password", "kttkpm123", "Kttkpm@123", "SystemAdmin@123"};
    for (String s : c) {
      System.out.println(s + " => " + BCrypt.checkpw(s, hash));
    }
  }
}
