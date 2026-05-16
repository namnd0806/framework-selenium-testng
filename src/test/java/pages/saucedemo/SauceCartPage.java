package pages.saucedemo;

import core.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page Object for SauceDemo Cart page.
 */
public class SauceCartPage extends BasePage {

    private static final By CART_ITEMS      = By.cssSelector(".cart_item");
    private static final By ITEM_NAMES      = By.cssSelector(".inventory_item_name");
    private static final By CHECKOUT_BUTTON = By.id("checkout");
    private static final By CONTINUE_BUTTON = By.id("continue-shopping");

    public SauceCartPage(WebDriver driver) {
        super(driver);
    }

    public boolean isCartPageDisplayed() {
        try {
            wait.until(ExpectedConditions.urlContains("cart"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(CHECKOUT_BUTTON));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int getCartItemCount() {
        return findElements(CART_ITEMS).size();
    }

    public List<String> getCartItemNames() {
        return findElements(ITEM_NAMES).stream()
                .map(WebElement::getText)
                .toList();
    }

    @Step("Click nút Checkout")
    public void clickCheckout() {
        executeScript("document.getElementById('checkout').click();");
    }

    @Step("Tiếp tục mua hàng")
    public void continueShopping() {
        click(CONTINUE_BUTTON);
    }
}
