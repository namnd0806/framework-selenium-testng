package pages.saucedemo;

import core.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for SauceDemo Checkout Complete page.
 */
public class SauceCheckoutCompletePage extends BasePage {

    private static final By COMPLETE_HEADER  = By.cssSelector(".complete-header");
    private static final By COMPLETE_TEXT    = By.cssSelector(".complete-text");
    private static final By PONY_EXPRESS_IMG = By.cssSelector(".pony_express");
    private static final By BACK_HOME_BUTTON = By.id("back-to-products");

    public SauceCheckoutCompletePage(WebDriver driver) {
        super(driver);
    }

    public boolean isOrderCompleted() {
        try {
            wait.until(ExpectedConditions.urlContains("checkout-complete"));
            return isDisplayed(COMPLETE_HEADER);
        } catch (Exception e) {
            return false;
        }
    }

    public String getCompleteHeaderText() { return getText(COMPLETE_HEADER); }
    public String getCompleteText()       { return getText(COMPLETE_TEXT); }

    public boolean isPonyExpressImageDisplayed() {
        return isDisplayed(PONY_EXPRESS_IMG);
    }

    @Step("Quay về trang sản phẩm")
    public void clickBackToProducts() {
        click(BACK_HOME_BUTTON);
    }
}
