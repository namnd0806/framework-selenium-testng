package pages.saucedemo;

import core.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for SauceDemo Checkout Step One (customer info) page.
 */
public class SauceCheckoutStepOnePage extends BasePage {

    private static final By FIRST_NAME_INPUT = By.id("first-name");
    private static final By LAST_NAME_INPUT  = By.id("last-name");
    private static final By ZIP_CODE_INPUT   = By.id("postal-code");
    private static final By CONTINUE_BUTTON  = By.id("continue");
    private static final By CANCEL_BUTTON    = By.id("cancel");
    private static final By ERROR_MESSAGE    = By.cssSelector("[data-test='error']");

    public SauceCheckoutStepOnePage(WebDriver driver) {
        super(driver);
    }

    public boolean isCheckoutStepOneDisplayed() {
        try {
            wait.until(ExpectedConditions.urlContains("checkout-step-one"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(CONTINUE_BUTTON));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Nhập First Name: {firstName}")
    public void enterFirstName(String firstName) {
        sendKeys(FIRST_NAME_INPUT, firstName);
    }

    @Step("Nhập Last Name: {lastName}")
    public void enterLastName(String lastName) {
        sendKeys(LAST_NAME_INPUT, lastName);
    }

    @Step("Nhập Zip Code: {zipCode}")
    public void enterZipCode(String zipCode) {
        sendKeys(ZIP_CODE_INPUT, zipCode);
    }

    @Step("Điền thông tin khách hàng: {firstName} {lastName}, {zipCode}")
    public void fillCustomerInfo(String firstName, String lastName, String zipCode) {
        enterFirstName(firstName);
        enterLastName(lastName);
        enterZipCode(zipCode);
    }

    @Step("Click nút Continue")
    public void clickContinue() {
        click(CONTINUE_BUTTON);
    }

    @Step("Click nút Cancel")
    public void clickCancel() {
        click(CANCEL_BUTTON);
    }

    public String getErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
            return getText(ERROR_MESSAGE);
        } catch (Exception e) {
            return "";
        }
    }
}
