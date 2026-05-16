package pages.saucedemo;

import core.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page Object for SauceDemo Checkout Step Two (order summary) page.
 */
public class SauceCheckoutStepTwoPage extends BasePage {

    private static final By ITEM_NAMES     = By.cssSelector(".inventory_item_name");
    private static final By ITEM_PRICES    = By.cssSelector(".inventory_item_price");
    private static final By SUBTOTAL_LABEL = By.cssSelector(".summary_subtotal_label");
    private static final By TAX_LABEL      = By.cssSelector(".summary_tax_label");
    private static final By TOTAL_LABEL    = By.cssSelector(".summary_total_label");
    private static final By FINISH_BUTTON  = By.id("finish");
    private static final By CANCEL_BUTTON  = By.id("cancel");

    public SauceCheckoutStepTwoPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOrderSummaryDisplayed() {
        try {
            wait.until(ExpectedConditions.urlContains("checkout-step-two"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_BUTTON));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getOrderItemNames() {
        return findElements(ITEM_NAMES).stream()
                .map(WebElement::getText)
                .toList();
    }

    public String getSubtotal() { return getText(SUBTOTAL_LABEL); }
    public String getTax()      { return getText(TAX_LABEL); }
    public String getTotal()    { return getText(TOTAL_LABEL); }

    @Step("Click nút Finish để hoàn tất đơn hàng")
    public void clickFinish() {
        scrollToElement(FINISH_BUTTON);
        click(FINISH_BUTTON);
    }

    @Step("Click nút Cancel")
    public void clickCancel() {
        click(CANCEL_BUTTON);
    }
}
