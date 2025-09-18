package testCasesCode;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import base_Classes.Base_Page;

public class Multi_Server_Login_Page extends Base_Page {

    private String baseUrlAfterLogin = "";  // Base URL captured after login
    private Properties config = p; // config.properties loaded from Base_Page

    public Multi_Server_Login_Page() {
        super();
        initializeElements(this);
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(20));
    }

    // ✅ Check if server is up
    public boolean isServerUp(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            System.out.println("🔁 Server HTTP Response Code: " + responseCode);
            int code = connection.getResponseCode();
            System.out.println("🔁 Server HTTP Response Code: " + code);
            return (code >= 200 && code < 400);

        } catch (Exception e) {
            System.out.println("❌ Server check failed: " + e.getMessage());
            return false;
        }
    }

    // ✅ Web elements
    @FindBy(xpath = "//input[@id='txtUserName']")
    WebElement usernameInput;

    @FindBy(xpath = "//input[@id='btnlogin']")
    WebElement nextBtn;

    @FindBy(xpath = "//input[@id='txtPassword']")
    WebElement passwordInput;

    @FindBy(xpath = "//li[@id='logg_main']")
    WebElement userMenu;

    // ✅ Login Method
    public String performLogin(String url, String username, String password) {
        if (!isServerUp(url)) {
            return "❌ Server is down or unreachable";
        }

        try {
            getDriver().get(url);
            getDriver().manage().deleteAllCookies();
            getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
            getDriver().manage().window().maximize();

            wait.until(ExpectedConditions.visibilityOf(usernameInput)).clear();
            usernameInput.sendKeys(username);

            wait.until(ExpectedConditions.elementToBeClickable(nextBtn)).click();

            wait.until(ExpectedConditions.visibilityOf(passwordInput)).clear();
            passwordInput.sendKeys(password);

            wait.until(ExpectedConditions.elementToBeClickable(nextBtn)).click();

            boolean isSuccess = isHomePageDisplayed();
            if (isSuccess && userMenu.isDisplayed()) {
                // ✅ After successful login, extract base URL
                String currentUrl = getDriver().getCurrentUrl(); // e.g., https://www2.domain.com/NU/Dashboard
                baseUrlAfterLogin = extractBaseUrl(currentUrl);
                return "✅ Login Success";
            } else {
                return "❌ Login Failed - Homepage not visible";
            }

        } catch (Exception e) {
            System.out.println("❌ Exception during login: " + e.getMessage());
            return "❌ Login Failed with Exception";
        }
    }

    // ✅ Homepage load check
    public boolean isHomePageDisplayed() throws InterruptedException {
        String expected = "Home/Dashboard";
        for (int retry = 1; retry <= 4; retry++) {
            if (getDriver().getCurrentUrl().contains(expected)) {
                System.out.println("✅ Homepage loaded after retry: " + retry);
                return true;
            } else {
                getDriver().navigate().refresh();
                Thread.sleep(3000);
            }
        }
        return false;
    }

 // ✅ Logout using config key and dynamic base URL
    public void performLogout() {
        try {
            String signOutPath = config.getProperty("signouturl");
            if (signOutPath == null || signOutPath.trim().isEmpty()) {
                System.out.println("❌ Logout path not found in config.");
                return;
            }

            if (baseUrlAfterLogin == null || baseUrlAfterLogin.isEmpty()) {
                baseUrlAfterLogin = extractBaseUrl(getDriver().getCurrentUrl());
            }

            String logoutUrl = baseUrlAfterLogin + signOutPath;
            getDriver().get(logoutUrl);
            System.out.println("🔒 Logged out successfully: " + logoutUrl);
        } catch (Exception e) {
            System.out.println("⚠️ Logout failed: " + e.getMessage());
        }
    }

    // ✅ Extract base URL (e.g., https://www2.domain.com)
    public String extractBaseUrl(String fullUrl) {
        try {
            URL urlObj = new URL(fullUrl);
            return urlObj.getProtocol() + "://" + urlObj.getHost();  // https://www2.domain.com
        } catch (Exception e) {
            System.out.println("❌ Failed to extract base URL: " + e.getMessage());
            return "";
        }
    }
    

 // ✅ Generic method to navigate to any page using config key
    public void navigateToPage(String configKey) {
        try {
            String path = config.getProperty(configKey);
            if (path == null || path.trim().isEmpty()) {
                System.out.println("❌ Path not found in config for key: " + configKey);
                return;
            }

            if (baseUrlAfterLogin.isEmpty()) {
                baseUrlAfterLogin = extractBaseUrl(getDriver().getCurrentUrl());
            }

            String fullUrl = baseUrlAfterLogin + path;
            System.out.println("🔗 Navigating to: " + fullUrl);
            getDriver().get(fullUrl);
        } catch (Exception e) {
            System.out.println("❌ Failed to navigate using key " + configKey + ": " + e.getMessage());
        }
    }
}
