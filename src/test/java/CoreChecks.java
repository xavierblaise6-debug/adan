import com.ontime.mediconnect.model.Visit;
import com.ontime.mediconnect.util.PasswordHasher;
import com.ontime.mediconnect.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Plain-Java checks (no database, no JavaFX needed). Run with:
 *   javac -d out $(find src/main/java -name "*.java" | grep -v /ui/) src/test/java/CoreChecks.java
 *   java -cp out CoreChecks
 * Used as the evidence for SWD Form 10 (test data) for the validation and security rules.
 */
public class CoreChecks {
    private static int failures = 0;

    public static void main(String[] args) {
        // Password hashing
        String h = PasswordHasher.hash("Admin@123");
        check("hash verifies correct password", PasswordHasher.verify("Admin@123", h));
        check("hash rejects wrong password", !PasswordHasher.verify("admin@123", h));
        check("hash is salted (two hashes differ)", !h.equals(PasswordHasher.hash("Admin@123")));
        check("malformed stored hash rejected", !PasswordHasher.verify("x", "garbage"));

        // Names
        check("name ok", Validator.name("Name", "Dr. Anne-Marie O'Neil") == null);
        check("name blank rejected", Validator.name("Name", "  ") != null);
        check("name digits rejected", Validator.name("Name", "John3") != null);
        check("name too long rejected", Validator.name("Name", "A".repeat(51)) != null);

        // Phone / email
        check("phone ok", Validator.phone("+12155550123") == null);
        check("phone letters rejected", Validator.phone("21555abc") != null);
        check("phone too short rejected", Validator.phone("12345") != null);
        check("email ok", Validator.email("a.b@pmc.org") == null);
        check("email no @ rejected", Validator.email("ab.pmc.org") != null);
        check("email > 30 chars rejected", Validator.email("a".repeat(25) + "@pmc.org") != null);

        // Patient fields
        check("age 0 ok", Validator.age("0") == null);
        check("age 120 ok", Validator.age("120") == null);
        check("age 121 rejected", Validator.age("121") != null);
        check("age text rejected", Validator.age("abc") != null);
        check("blood O+ ok", Validator.bloodGroup("O+") == null);
        check("blood C+ rejected", Validator.bloodGroup("C+") != null);
        check("gender F ok", Validator.gender("F") == null);
        check("gender null rejected", Validator.gender(null) != null);
        check("weight 70.5 ok", Validator.positiveNumber("Weight", "70.5") == null);
        check("weight 0 rejected", Validator.positiveNumber("Weight", "0") != null);
        check("weight negative rejected", Validator.positiveNumber("Weight", "-3") != null);

        // Money / staff id
        check("money 150.75 ok", Validator.money("Fee", "150.75") == null);
        check("money 3 decimals rejected", Validator.money("Fee", "1.234") != null);
        check("money negative rejected", Validator.money("Fee", "-1") != null);
        check("staff id S001 ok", Validator.staffId("S001") == null);
        check("staff id 1234 rejected", Validator.staffId("1234") != null);

        // Helper
        check("firstError returns first message", "b".equals(Validator.firstError(null, "b", "c")));
        check("firstError all valid -> null", Validator.firstError(null, null) == null);

        // Visit total
        Visit v = new Visit("V001", "D001", "P001", "Flu", null, 1, LocalDate.of(2026, 11, 10),
                new BigDecimal("120.00"), new BigDecimal("35.50"));
        check("visit total = cons + other", new BigDecimal("155.50").compareTo(v.total()) == 0);

        System.out.println(failures == 0 ? "ALL CHECKS PASSED" : failures + " CHECK(S) FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failures++;
    }
}
