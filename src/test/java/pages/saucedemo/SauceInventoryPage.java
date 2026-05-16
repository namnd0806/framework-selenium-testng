package pages.saucedemo;

import core.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page Object for SauceDemo Inventory (Products) page.
 */
public class SauceInventoryPage extends BasePage {

    private static final By PAGE_TITLE       = By.cssSelector(".title");
    private static final By PRODUCT_NAMES    = By.cssSelector(".inventory_item_name");
    private static final By ADD_TO_CART_BTNS = By.cssSelector(".btn_inventory");
    private static final By CART_BADGE       = By.cssSelector(".shopping_cart_badge");
    private static final By CART_ICON        = By.cssSelector(".shopping_cart_link");

    public SauceInventoryPage(WebDriver driver) {
        super(driver);
    }

    public boolean isInventoryPageDisplayed() {
        try {
            wait.until(ExpectedConditions.urlContains("inventory"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_TITLE));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Thêm {count} sản phẩm đầu tiên vào giỏ hàng")
    public void addFirstNProductsToCart(int count) {
        List<WebElement> addButtons = findElements(ADD_TO_CART_BTNS);
        int limit = Math.min(count, addButtons.size());
        for (int i = 0; i < limit; i++) {
            addButtons.get(i).click();
        }
    }

    @Step("Thêm sản phẩm '{productName}' vào giỏ hàng")
    public void addProductToCartByName(String productName) {
        By addButton = By.xpath(
            "//div[text()='" + productName + "']/ancestor::div[@class='inventory_item']"
            + "//button[contains(@class,'btn_inventory')]"
        );
        click(addButton);
    }

    public int getCartBadgeCount() {
        if (!isDisplayed(CART_BADGE)) return 0;
        return Integer.parseInt(getText(CART_BADGE));
    }

    @Step("Mở giỏ hàng")
    public void goToCart() {
        click(CART_ICON);
    }

    public List<String> getProductNames() {
        return findElements(PRODUCT_NAMES).stream()
                .map(WebElement::getText)
                .toList();
    }
}
