package utils;

import com.github.javafaker.Faker;

import java.util.Locale;
import java.util.Random;

/**
 * Utility class for generating random test data using Java Faker with Vietnamese locale.
 * All methods are static — no instantiation required.
 */
public class DataGenerator {

    private static final Faker faker = new Faker(new Locale("vi"));
    private static final Random random = new Random();

    // Private constructor — utility class, not meant to be instantiated
    private DataGenerator() {}

    /**
     * Generates a random full name (Vietnamese locale).
     *
     * @return a random full name string
     */
    public static String generateFullName() {
        return faker.name().fullName();
    }

    /**
     * Generates a random email address.
     * Format: {username}@{domain}
     *
     * @return a random email string
     */
    public static String generateEmail() {
        return faker.internet().emailAddress();
    }

    /**
     * Generates a random Vietnamese-style phone number.
     * Format: 10-digit number starting with 0 (e.g. 0912345678).
     *
     * @return a random phone number string
     */
    public static String generatePhoneNumber() {
        // Vietnamese mobile prefixes: 03x, 05x, 07x, 08x, 09x
        String[] prefixes = {"032", "033", "034", "035", "036", "037", "038", "039",
                             "056", "058", "070", "076", "077", "078", "079",
                             "081", "082", "083", "084", "085", "086", "089",
                             "090", "091", "092", "093", "094", "095", "096", "097", "098", "099"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        // Remaining 7 digits
        String suffix = String.format("%07d", random.nextInt(10_000_000));
        return prefix + suffix;
    }

    /**
     * Generates a random password of the specified length.
     * The password contains a mix of uppercase letters, lowercase letters, digits,
     * and special characters to satisfy common password policies.
     *
     * @param length the desired password length (must be &gt;= 4)
     * @return a random password string of the given length
     * @throws IllegalArgumentException if length is less than 4
     */
    public static String generatePassword(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("Password length must be at least 4, got: " + length);
        }
        return faker.internet().password(length, length, true, true, true);
    }

    /**
     * Generates a random username.
     * Format: lowercase alphanumeric string (e.g. "john_doe42").
     *
     * @return a random username string
     */
    public static String generateUsername() {
        return faker.name().username();
    }

    /**
     * Generates a random integer between {@code min} (inclusive) and {@code max} (inclusive).
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer in [min, max]
     * @throws IllegalArgumentException if min &gt; max
     */
    public static int generateRandomInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException(
                    "min must be <= max, got min=" + min + ", max=" + max);
        }
        return faker.number().numberBetween(min, max + 1);
    }

    /**
     * Generates a random address (Vietnamese locale).
     *
     * @return a random address string
     */
    public static String generateAddress() {
        return faker.address().fullAddress();
    }
}
