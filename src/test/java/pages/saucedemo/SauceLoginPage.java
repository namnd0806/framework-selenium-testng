package pages.saucedemo;

import core.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object for SauceDemo Login page.
 */
public class SauceLoginPage extends BasePage {

    private static final By USERNAME_INPUT = By.id("user-name");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON   = By.id("login-button");
    private static final By ERROR_MESSAGE  = By.cssSelector("[data-test='error']");

    public SauceLoginPage(WebDriver driver) {
        super(driver);
    }

    @Step("Mở trang Login")
    public SauceLoginPage open() {
        openByKey("login");
        return this;
    }

    @Step("Nhập username: {username}")
    public void enterUsername(String username) {
        sendKeys(USERNAME_INPUT, username);
    }

    @Step("Nhập password")
    public void enterPassword(String password) {
        sendKeys(PASSWORD_INPUT, password);
    }

    @Step("Click nút Login")
    public void clickLogin() {
        click(LOGIN_BUTTON);
    }

    @Step("Đăng nhập với username: {username}")
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public String getErrorMessage() {
        return isDisplayed(ERROR_MESSAGE) ? getText(ERROR_MESSAGE) : "";
    }

    public boolean isLoginPageDisplayed() {
        return isDisplayed(LOGIN_BUTTON);
    }
}
