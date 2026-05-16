package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object for the Login page.
 *
 * <p>Encapsulates all locators and interaction methods for the login form.
 * Contains no assertion logic — assertions belong in the test layer.</p>
 *
 * <p>Satisfies Requirements: 1.2, 1.4, 1.5, 4.5</p>
 */
public class LoginPage extends BasePage {

    // -------------------------------------------------------------------------
    // Locators
    // -------------------------------------------------------------------------

    /** Username input field. */
    private static final By USERNAME_INPUT = By.id("username");

    /** Password input field. */
    private static final By PASSWORD_INPUT = By.id("password");

    /** Login / submit button. */
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");

    /** Error message element displayed on failed login. */
    private static final By ERROR_MESSAGE = By.cssSelector(".error-message, #error-message, [data-testid='error']");

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a new {@code LoginPage} with the given WebDriver instance.
     *
     * @param driver the WebDriver instance for this page (must not be {@code null})
     */
    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // -------------------------------------------------------------------------
    // Interaction methods
    // -------------------------------------------------------------------------

    /**
     * Types the given username into the username input field.
     *
     * @param username the username to enter
     */
    public void enterUsername(String username) {
        sendKeys(USERNAME_INPUT, username);
    }

    /**
     * Types the given password into the password input field.
     *
     * @param password the password to enter
     */
    public void enterPassword(String password) {
        sendKeys(PASSWORD_INPUT, password);
    }

    /**
     * Clicks the login / submit button.
     */
    public void clickLogin() {
        click(SUBMIT_BUTTON);
    }

    /**
     * Returns the visible text of the error message element.
     *
     * @return the error message text, or an empty string if the element is not visible
     */
    public String getErrorMessage() {
        if (isDisplayed(ERROR_MESSAGE)) {
            return getText(ERROR_MESSAGE);
        }
        return "";
    }

    /**
     * Convenience method that enters the username and password, then clicks login.
     *
     * @param username the username to enter
     * @param password the password to enter
     */
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }
}
