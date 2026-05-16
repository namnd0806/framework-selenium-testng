package tests.saucedemo;

import base.UIBaseTest;
import core.utils.AssertionUtils;
import io.qameta.allure.*;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.saucedemo.*;
import core.report.AllureListener;

/**
 * End-to-end purchase flow test on SauceDemo (https://www.saucedemo.com).
 */
@Listeners(AllureListener.class)
@Epic("E-Commerce")
@Feature("Purchase Flow")
public class SaucePurchaseTest extends UIBaseTest {

    private static final String USERNAME      = "standard_user";
    private static final String PASSWORD      = "secret_sauce";
    private static final String FIRST_NAME    = "Nguyen";
    private static final String LAST_NAME     = "Van A";
    private static final String ZIP_CODE      = "70000";
    private static final int    PRODUCTS_TO_ADD = 2;

    private SauceLoginPage            loginPage;
    private SauceInventoryPage        inventoryPage;
    private SauceCartPage             cartPage;
    private SauceCheckoutStepOnePage  checkoutStepOne;
    private SauceCheckoutStepTwoPage  checkoutStepTwo;
    private SauceCheckoutCompletePage checkoutComplete;

    @Override
    protected void beforeTest() {
        loginPage        = new SauceLoginPage(driver);
        inventoryPage    = new SauceInventoryPage(driver);
        cartPage         = new SauceCartPage(driver);
        checkoutStepOne  = new SauceCheckoutStepOnePage(driver);
        checkoutStepTwo  = new SauceCheckoutStepTwoPage(driver);
        checkoutComplete = new SauceCheckoutCompletePage(driver);
        loginPage.open();
    }

    // -------------------------------------------------------------------------

    @Test(description = "Đăng nhập thành công với tài khoản hợp lệ")
    @Story("Login")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginSuccess() {
        loginPage.login(USERNAME, PASSWORD);

        AssertionUtils.assertTrue(
            inventoryPage.isInventoryPageDisplayed(),
            "Trang sản phẩm phải hiển thị sau khi đăng nhập thành công"
        );
    }

    @Test(description = "Đăng nhập thất bại với tài khoản không hợp lệ")
    @Story("Login")
    @Severity(SeverityLevel.NORMAL)
    public void testLoginFailure() {
        loginPage.login("invalid_user", "wrong_password");

        String errorMsg = loginPage.getErrorMessage();
        AssertionUtils.assertTrue(
            !errorMsg.isEmpty(),
            "Phải hiển thị thông báo lỗi khi đăng nhập sai"
        );
        AssertionUtils.assertTrue(
            errorMsg.contains("do not match") || errorMsg.contains("Username and password"),
            "Thông báo lỗi phải đề cập đến sai thông tin. Actual: " + errorMsg
        );
    }

    @Test(description = "Luồng mua hàng đầy đủ: đăng nhập → thêm sản phẩm → checkout → hoàn tất")
    @Story("Purchase")
    @Severity(SeverityLevel.CRITICAL)
    public void testFullPurchaseFlow() {
        loginPage.login(USERNAME, PASSWORD);
        AssertionUtils.assertTrue(
            inventoryPage.isInventoryPageDisplayed(),
            "Phải ở trang sản phẩm sau khi đăng nhập"
        );

        inventoryPage.addFirstNProductsToCart(PRODUCTS_TO_ADD);
        AssertionUtils.assertEquals(
            inventoryPage.getCartBadgeCount(), PRODUCTS_TO_ADD,
            "Giỏ hàng phải hiển thị " + PRODUCTS_TO_ADD + " sản phẩm"
        );

        inventoryPage.goToCart();
        AssertionUtils.assertTrue(cartPage.isCartPageDisplayed(), "Phải ở trang giỏ hàng");
        AssertionUtils.assertEquals(
            cartPage.getCartItemCount(), PRODUCTS_TO_ADD,
            "Giỏ hàng phải chứa " + PRODUCTS_TO_ADD + " sản phẩm"
        );

        cartPage.clickCheckout();
        AssertionUtils.assertTrue(
            checkoutStepOne.isCheckoutStepOneDisplayed(),
            "Phải ở trang nhập thông tin khách hàng"
        );

        checkoutStepOne.fillCustomerInfo(FIRST_NAME, LAST_NAME, ZIP_CODE);
        checkoutStepOne.clickContinue();
        AssertionUtils.assertTrue(
            checkoutStepTwo.isOrderSummaryDisplayed(),
            "Phải ở trang xác nhận đơn hàng"
        );

        AssertionUtils.assertEquals(
            checkoutStepTwo.getOrderItemNames().size(), PRODUCTS_TO_ADD,
            "Tóm tắt đơn hàng phải liệt kê " + PRODUCTS_TO_ADD + " sản phẩm"
        );

        checkoutStepTwo.clickFinish();
        AssertionUtils.assertTrue(
            checkoutComplete.isOrderCompleted(),
            "Phải ở trang xác nhận hoàn tất đơn hàng"
        );

        String header = checkoutComplete.getCompleteHeaderText();
        AssertionUtils.assertTrue(
            header.contains("Thank you"),
            "Header phải chứa 'Thank you'. Actual: " + header
        );
    }

    @Test(description = "Checkout thất bại khi thiếu thông tin khách hàng")
    @Story("Purchase")
    @Severity(SeverityLevel.NORMAL)
    public void testCheckoutWithMissingInfo() {
        loginPage.login(USERNAME, PASSWORD);
        AssertionUtils.assertTrue(inventoryPage.isInventoryPageDisplayed(), "Phải ở trang sản phẩm");

        inventoryPage.addFirstNProductsToCart(1);
        inventoryPage.goToCart();
        AssertionUtils.assertTrue(cartPage.isCartPageDisplayed(), "Phải ở trang giỏ hàng");

        cartPage.clickCheckout();
        AssertionUtils.assertTrue(
            checkoutStepOne.isCheckoutStepOneDisplayed(),
            "Phải ở trang nhập thông tin khách hàng"
        );

        checkoutStepOne.clickContinue();

        String errorMsg = checkoutStepOne.getErrorMessage();
        AssertionUtils.assertTrue(
            !errorMsg.isEmpty(),
            "Phải hiển thị lỗi khi thiếu thông tin bắt buộc"
        );
        AssertionUtils.assertTrue(
            errorMsg.contains("First Name is required"),
            "Lỗi phải đề cập First Name is required. Actual: " + errorMsg
        );
    }
}
