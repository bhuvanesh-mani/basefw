package fw.core;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import fw.enums.BrowserType;
import fw.enums.OperatingSystem;
import fw.utilities.ConfigHolder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DriverFactory {
    private WebDriver driver;
    private ConfigHolder config = ConfigHolder.getInstance();
    private OperatingSystem os;
    private String osShortName = null;
    private boolean isGridExecution = false;
    private String hub_url = null;


    /**
     * Set up values for DriverFactory
     */
    private void initialize() {
        String mode = config.getProperty("selenium.grid.execution");
        if (mode == null || mode.trim().isEmpty())
            mode = "local";

        if (mode.trim().equalsIgnoreCase("true")
                || mode.trim().equalsIgnoreCase("on")
                || mode.trim().equalsIgnoreCase("yes")
                || mode.trim().equalsIgnoreCase("remote")
                || mode.trim().equalsIgnoreCase("grid"))
            this.isGridExecution = true;

        if (isGridExecution) {
            String hubURL = config.getProperty("selenium.grid.hub.url");
            /**
                if (hubURL == null || hubURL.trim().isEmpty())
                    hubURL = System.getProperty("selenium.grid.hub.url"); //Implement this in Config Holder itself
             **/
            if (hubURL == null || hubURL.trim().isEmpty())
                throw new RuntimeException("If Grid mode is on, then HUB URL is required. " +
                        "Provide value for property 'selenium.grid.hub.url'.");
            this.hub_url = hubURL;

            String osName = config.getProperty("selenium.remote.os.name",false);
            if(osName == null || osName.trim().isEmpty())
                osName = config.getProperty("os.name",false);
            if(osName == null || osName.trim().isEmpty())
                this.os = OperatingSystem.Any;
            else
                this.os  = getMatchingOSName(osName);
        }else{
            this.os = detectLocalRunningOS();
        }

    }

    public WebDriver getDriver() {
        return getDriver(this.getBrowser());
    }

    /**
     * This method invokes the browser type as required
     *
     * @param type
     * @return
     */
    public WebDriver getDriver(BrowserType type) {
        this.initialize();

        if(type == null)
            type = this.getBrowser();

        switch (type) {
            case firefox:
                this.driver = getFirefoxDriver();
                break;
            case ie:
                this.driver = getInternetExplorerDriver();
                break;
            case chrome:
                this.driver = getChromeDriver();
                break;
            case safari:
                this.driver = getSafariDriver();
                break;
        }

        this.driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        this.driver.manage().window().maximize();
        return this.driver;
    }

    /**
     *
     */
    public void closeBrowser(){
        if(this.driver != null ){
            this.driver.close();
            this.driver =  null;
        }
    }

    /**
     * If driver externally initialized, then set it manually
     *
     * @param driver
     */
    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }


    /**
     * Initializes Internet explorer driver
     *
     * @return
     */
    private WebDriver getInternetExplorerDriver() {
        WebDriver driver = null;
        try {
            DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
            capabilities.setBrowserName("ie");
            capabilities.setPlatform(this.getPlatform());
            if (!isGridExecution) {
                String path = getPath(this.os,BrowserType.ie);
                System.setProperty("webdriver.ie.driver",path);
                driver = new ChromeDriver(capabilities);
            } else {
                driver = new RemoteWebDriver(new URL(this.hub_url), capabilities);
            }
        }catch(MalformedURLException e){
            throw new RuntimeException("Error occured while invoking remote web driver using the hub url'" + this.hub_url +"'.");
        }
        return driver;
    }

    /**
     * Initializes Chrome driver
     *
     * @return
     */
    private WebDriver getChromeDriver() {
        WebDriver driver = null;
        try {
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            capabilities.setBrowserName("ie");
            capabilities.setPlatform(this.getPlatform());

            //additional capabilties here
            ChromeOptions options = new ChromeOptions();
            options.addArguments("start-maximized");
            options.addArguments("disable-extensions");

            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.default_content_settings.popups", 0);

            options.merge(capabilities);
            options.setExperimentalOption("prefs", prefs);
            options.setExperimentalOption("useAutomationExtension",false);

            if (!isGridExecution) {
                String path = getPath(this.os,BrowserType.chrome);
                System.setProperty("webdriver.chrome.driver",path);
                driver = new ChromeDriver(options);
            } else {
                driver = new RemoteWebDriver(new URL(this.hub_url), options);
            }
        }catch(MalformedURLException e){
            throw new RuntimeException("Error occured while invoking remote web driver using the hub url'" + this.hub_url +"'.");
        }
        return driver;
    }


    /**
     * Initializes Firefox driver
     *
     * @return
     */
    private WebDriver getFirefoxDriver() {
        WebDriver driver = null;
        try {

            DesiredCapabilities capabilities = DesiredCapabilities.firefox();
            capabilities.setCapability(FirefoxDriver.PROFILE,new FirefoxProfile());

            FirefoxOptions options = new FirefoxOptions();
            options.merge(capabilities);

            if (!isGridExecution) {
                String path = getPath(this.os,BrowserType.safari);
                System.setProperty("webdriver.gecko.driver",path);
                driver = new FirefoxDriver(options);
            } else {
                driver = new RemoteWebDriver(new URL(this.hub_url),options);
            }
        }catch(MalformedURLException e){
            throw new RuntimeException("Error occured while invoking remote web driver using the hub url'" + this.hub_url +"'.");
        }
        return driver;
    }

    /**
     * Initializes Safari Driver
     *
     * @return
     */
    private WebDriver getSafariDriver() {
        WebDriver driver = null;
        try {
            DesiredCapabilities capabilities = DesiredCapabilities.safari();
            capabilities.setBrowserName("safari");
            capabilities.setPlatform(this.getPlatform());

            SafariOptions options = new SafariOptions();
            options.merge(capabilities);

            if (!isGridExecution) {
                String path = getPath(this.os,BrowserType.ie);
                System.setProperty("webdriver.safari.driver",path);
                driver = new SafariDriver(options);
            } else {
                driver = new RemoteWebDriver(new URL(this.hub_url),options);
            }
        }catch(MalformedURLException e){
            throw new RuntimeException("Error occured while invoking remote web driver using the hub url'" + this.hub_url +"'.");
        }
        return driver;
    }

    /**
     * Based on OS and Browser type, initializes the path for driver
     *
     * @param os
     * @return
     */
    private String getPath(OperatingSystem os, BrowserType browser) {
        String path = null;

        switch (browser) {
            case firefox:
                switch (os) {
                    case Windows:
                        path = config.getProperty("webdriver.win.gecko.driver");
                        break;
                    case Linux:
                        path = config.getProperty("webdriver.lin.gecko.driver");
                        break;
                    case Mac:
                        path = config.getProperty("webdriver.mac.gecko.driver");
                        break;
                    case Solaris:
                        path = config.getProperty("webdriver.sol.gecko.driver");
                        break;
                }
                if (path == null || path.trim().isEmpty())
                    path = config.getProperty("webdriver.gecko.driver");
                if (path == null || path.trim().isEmpty())
                    path = System.getProperty("webdriver.gecko.driver");
                if (path == null || path.trim().isEmpty())
                    throw new RuntimeException("Firefox gecko driver path is required. " +
                            "Provide value for property 'webdriver."+this.osShortName+".gecko.path");
                System.setProperty("webdriver.gecko.driver ", path);
                break;
            case ie:
                switch (os) {
                    case Windows:
                        path = config.getProperty("webdriver.win.ie.driver");
                        break;
                    case Linux:
                        throw new RuntimeException("Internet Explorer in Linux? Thats not supported yet!");
                    case Mac:
                        throw new RuntimeException("Internet Explorer in Mac? Thats not supported yet!");
                    case Solaris:
                        throw new RuntimeException("Internet Explorer in Solaris? Thats not supported yet!");
                }
                if (path == null || path.trim().isEmpty())
                    path = config.getProperty("webdriver.ie.driver");
                if (path == null || path.trim().isEmpty())
                    path = System.getProperty("webdriver.ie.driver");
                if (path == null || path.trim().isEmpty())
                    throw new RuntimeException("Internet Explorer driver path is required. " +
                            "Provide value for property 'webdriver."+this.osShortName+".path");
                System.setProperty("webdriver.ie.driver ", path);
                break;
            case chrome:
                switch (os) {
                    case Windows:
                        path = config.getProperty("webdriver.win.chrome.driver");
                        break;
                    case Linux:
                        path = config.getProperty("webdriver.lin.chrome.driver");
                        break;
                    case Mac:
                        path = config.getProperty("webdriver.mac.chrome.driver");
                        break;
                    case Solaris:
                        path = config.getProperty("webdriver.sol.chrome.driver");
                        break;
                }
                if (path == null || path.trim().isEmpty())
                    path = config.getProperty("webdriver.chrome.driver");
                if (path == null || path.trim().isEmpty())
                    path = System.getProperty("webdriver.chrome.driver");
                if (path == null || path.trim().isEmpty())
                    throw new RuntimeException("Firefox gecko driver path is required. " +
                            "Provide value for property 'webdriver."+this.osShortName+".path");
                System.setProperty("webdriver.chrome.driver ", path);
                break;
            case safari:
                switch (os) {
                    case Windows:
                        path = config.getProperty("webdriver.win.safari.driver");
                        break;
                    case Linux:
                        path = config.getProperty("webdriver.lin.gecko.driver");
                        break;
                    case Mac:
                        path = config.getProperty("webdriver.mac.gecko.driver");
                        break;
                    case Solaris:
                        path = config.getProperty("webdriver.sol.gecko.driver");
                        break;
                }
                if (path == null || path.trim().isEmpty())
                    path = config.getProperty("webdriver.gecko.driver");
                if (path == null || path.trim().isEmpty())
                    path = System.getProperty("webdriver.gecko.driver");
                if (path == null || path.trim().isEmpty())
                    throw new RuntimeException("Firefox gecko driver path is required. " +
                            "Provide value for property 'webdriver."+this.osShortName+".gecko.path");
                System.setProperty("webdriver.gecko.driver ", path);
                break;
        }


        return path;
    }


    /**
     * Detects the operating system of JVM running
     *
     * @return
     */
    private OperatingSystem getMatchingOSName(String osName) {
        if (osName == null)
            return null;

        osName = osName.toLowerCase();

        if (osName.contains("win")) {
            return OperatingSystem.Windows;
        } else if (osName.contains("nix")
                || osName.contains("nux")
                || osName.contains("aix")) {
            return OperatingSystem.Linux;
        } else if (osName.contains("mac")) {
            return OperatingSystem.Mac;
        } else if (osName.contains("sunos")) {
            return OperatingSystem.Solaris;
        } else
            return null;
    }


    private OperatingSystem detectLocalRunningOS() {
        String osName = System.getProperty("os.name");
        return this.getMatchingOSName(osName);
    }

    /**
     *
     * @return
     */
    private Platform getPlatform() {
        if (this.os == null)
            return Platform.ANY;

        switch (this.os) {
            case Windows:
                return Platform.WINDOWS;
            case Linux:
                return Platform.LINUX;
            case Mac:
                return Platform.MAC;
            case Solaris:
                return Platform.UNIX;
            case Any:
                return Platform.WINDOWS;
            default:
                return Platform.ANY;
        }
    }


    private BrowserType getBrowser(){
        String browser  = config.getProperty("selenium.browser.name");
        if(browser == null || browser.trim().isEmpty())
            browser  = config.getProperty("browser.name");
        if(browser == null || browser.trim().isEmpty())
            return BrowserType.chrome;

        browser = browser.toLowerCase().replace("[^a-z]","");

        if(browser.contains("chrome"))
            return BrowserType.chrome;
        else if(browser.contains("ie") || browser.contains("internetexplorer")
                || browser.contains("iexplorer"))
            return BrowserType.ie;
        else if(browser.contains("safari"))
            return BrowserType.safari;
        else if(browser.contains("ff") || browser.contains("firefox"))
            return BrowserType.firefox;
        else
            return BrowserType.chrome;
    }


}

