# Selenium Interactions

## 1. Basic Interactions

```java
WebElement element = driver.findElement(By.id("username"));

// Input
element.sendKeys("john@test.com");   // Gõ text
element.clear();                      // Xóa text
element.sendKeys(Keys.CONTROL + "a"); // Ctrl+A
element.sendKeys(Keys.BACK_SPACE);    // Backspace

// Click
element.click();
element.submit(); // Submit form (dùng cho form element)

// Get info
String text      = element.getText();                    // Text hiển thị
String value     = element.getAttribute("value");        // Attribute value
String href      = element.getAttribute("href");
String cssClass  = element.getAttribute("class");
String color     = element.getCssValue("color");         // CSS property

// State checks
boolean displayed = element.isDisplayed(); // Có hiển thị không
boolean enabled   = element.isEnabled();   // Có enabled không (button, input)
boolean selected  = element.isSelected();  // Có được chọn không (checkbox, radio)
```

---

## 2. Select - Dropdown

```java
import org.openqa.selenium.support.ui.Select;

WebElement dropdownEl = driver.findElement(By.id("country"));
Select select = new Select(dropdownEl);

// Chọn option
select.selectByVisibleText("Vietnam");   // Theo text hiển thị
select.selectByValue("VN");              // Theo value attribute
select.selectByIndex(0);                 // Theo index (0-based)

// Deselect (chỉ dùng cho multi-select)
select.deselectByVisibleText("Vietnam");
select.deselectAll();

// Get options
List<WebElement> allOptions     = select.getOptions();
WebElement firstSelected        = select.getFirstSelectedOption();
List<WebElement> allSelected    = select.getAllSelectedOptions();
boolean isMultiple              = select.isMultiple();

// Ví dụ thực tế
public void selectCountry(String countryName) {
    Select countryDropdown = new Select(driver.findElement(By.id("country")));
    countryDropdown.selectByVisibleText(countryName);
    // Verify selection
    Assert.assertEquals(countryDropdown.getFirstSelectedOption().getText(), countryName);
}
```

---

## 3. Actions Class - Advanced Interactions

```java
import org.openqa.selenium.interactions.Actions;

Actions actions = new Actions(driver);

// Hover (mouse over)
WebElement menu = driver.findElement(By.id("nav-menu"));
actions.moveToElement(menu).perform();
// Sau đó click submenu
driver.findElement(By.linkText("Products")).click();

// Double click
actions.doubleClick(element).perform();

// Right click (context menu)
actions.contextClick(element).perform();

// Click and hold
actions.clickAndHold(element).perform();
actions.release().perform();

// Drag and Drop
WebElement source = driver.findElement(By.id("draggable"));
WebElement target = driver.findElement(By.id("droppable"));
actions.dragAndDrop(source, target).perform();
// Hoặc
actions.clickAndHold(source)
       .moveToElement(target)
       .release()
       .perform();

// Key combinations
actions.keyDown(Keys.CONTROL)
       .sendKeys("a")  // Ctrl+A
       .keyUp(Keys.CONTROL)
       .perform();

// Chain nhiều actions
actions.moveToElement(menu)
       .pause(Duration.ofMillis(500))
       .click()
       .perform();
```

---

## 4. JavaScript Executor

```java
JavascriptExecutor js = (JavascriptExecutor) driver;

// Scroll
js.executeScript("window.scrollTo(0, document.body.scrollHeight)"); // Scroll to bottom
js.executeScript("window.scrollTo(0, 0)");                          // Scroll to top
js.executeScript("arguments[0].scrollIntoView(true);", element);    // Scroll to element

// Click via JS (khi element bị che khuất)
js.executeScript("arguments[0].click();", element);

// Get value
String value = (String) js.executeScript("return arguments[0].value;", inputElement);
String innerText = (String) js.executeScript("return arguments[0].innerText;", element);

// Set value (khi sendKeys không hoạt động)
js.executeScript("arguments[0].value = arguments[1];", inputElement, "test@email.com");

// Highlight element (debug)
js.executeScript("arguments[0].style.border='3px solid red'", element);

// Page info
String title = (String) js.executeScript("return document.title;");
Long scrollY = (Long) js.executeScript("return window.scrollY;");

// Ví dụ thực tế: click button bị overlay
public void clickWithJS(WebElement element) {
    try {
        element.click();
    } catch (ElementClickInterceptedException e) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", element);
    }
}
```

---

## 5. Handle Alert

```java
// Chờ alert xuất hiện
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
Alert alert = wait.until(ExpectedConditions.alertIsPresent());

// Lấy text
String alertText = alert.getText();
System.out.println("Alert: " + alertText);

// Accept (OK)
alert.accept();

// Dismiss (Cancel)
alert.dismiss();

// Prompt - nhập text
alert.sendKeys("My input text");
alert.accept();

// Ví dụ thực tế
public void handleDeleteConfirmation() {
    driver.findElement(By.id("delete-btn")).click();
    Alert alert = new WebDriverWait(driver, Duration.ofSeconds(5))
        .until(ExpectedConditions.alertIsPresent());
    Assert.assertEquals(alert.getText(), "Are you sure you want to delete?");
    alert.accept(); // Confirm delete
}
```

---

## 6. Handle iFrame

```java
// Switch vào iframe
driver.switchTo().frame("iframe-name");           // Theo name/id
driver.switchTo().frame(0);                        // Theo index
driver.switchTo().frame(driver.findElement(By.tagName("iframe"))); // Theo element

// Thao tác trong iframe
driver.findElement(By.id("element-in-iframe")).click();

// Quay về main content
driver.switchTo().defaultContent();

// Nested iframe
driver.switchTo().frame("outer-frame");
driver.switchTo().frame("inner-frame");
// ... thao tác
driver.switchTo().defaultContent(); // Về main content luôn

// Ví dụ thực tế: Rich text editor (TinyMCE, CKEditor)
public void typeInRichTextEditor(String text) {
    driver.switchTo().frame(driver.findElement(By.cssSelector("iframe.tox-edit-area__iframe")));
    driver.findElement(By.tagName("body")).sendKeys(text);
    driver.switchTo().defaultContent();
}
```

---

## 7. Handle Multiple Windows/Tabs

```java
// Lấy handle của window hiện tại
String mainWindow = driver.getWindowHandle();

// Click link mở tab mới
driver.findElement(By.linkText("Open in new tab")).click();

// Lấy tất cả window handles
Set<String> allWindows = driver.getWindowHandles();

// Switch sang tab mới
for (String window : allWindows) {
    if (!window.equals(mainWindow)) {
        driver.switchTo().window(window);
        break;
    }
}

// Thao tác trên tab mới
System.out.println("New tab title: " + driver.getTitle());

// Đóng tab hiện tại và quay về main
driver.close();
driver.switchTo().window(mainWindow);

// Mở tab mới bằng JS
js.executeScript("window.open('https://example.com', '_blank');");
```

---

## 8. File Upload

```java
// Cách 1: sendKeys với đường dẫn file (phổ biến nhất)
WebElement fileInput = driver.findElement(By.cssSelector("input[type='file']"));
String filePath = System.getProperty("user.dir") + "/src/test/resources/testfiles/document.pdf";
fileInput.sendKeys(filePath);

// Cách 2: Nếu input bị hidden, dùng JS để unhide trước
js.executeScript("arguments[0].style.display='block';", fileInput);
fileInput.sendKeys(filePath);

// Verify file được chọn
String selectedFile = fileInput.getAttribute("value");
Assert.assertTrue(selectedFile.contains("document.pdf"));
```

---

## 9. Câu hỏi phỏng vấn

**Q1: Khi nào dùng JavaScript Executor thay vì click() thông thường?**
> **Trả lời:** Khi element bị che khuất bởi element khác (ElementClickInterceptedException), khi element nằm ngoài viewport, hoặc khi cần scroll. Tuy nhiên nên ưu tiên dùng Actions.moveToElement() trước.
>
> **Gợi nhớ:** JS click = giải pháp cuối cùng khi click thường thất bại

**Q2: Sự khác nhau giữa getText() và getAttribute("value")?**
> **Trả lời:** getText() lấy text hiển thị của element (innerText). getAttribute("value") lấy giá trị của attribute "value" trong HTML — dùng cho input fields, select options.
>
> **Gợi nhớ:** getText = text nhìn thấy, getAttribute("value") = giá trị trong HTML

**Q3: Làm thế nào để xử lý dropdown không phải thẻ `<select>`?**
> **Trả lời:** Không dùng Select class được. Phải click vào dropdown để mở, sau đó findElements để lấy danh sách options, rồi click vào option cần chọn.
>
> **Gợi nhớ:** Select class chỉ dùng cho `<select>` HTML, custom dropdown thì click thủ công

**Q4: Tại sao phải switchTo().defaultContent() sau khi xử lý iframe?**
> **Trả lời:** Sau khi switch vào iframe, driver chỉ tìm element trong iframe đó. Nếu không switch về, các findElement tiếp theo sẽ fail vì tìm trong iframe thay vì main page.
>
> **Gợi nhớ:** Vào iframe = vào phòng khác, phải ra cửa (defaultContent) mới về nhà chính

---

[Tiếp theo: 03-waits-and-synchronization.md](./03-waits-and-synchronization.md) | [Quay lại README](./README.md)
