/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.lip6.jpagelyzer;

import com.opera.core.systems.OperaDriver;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 *
 * @author sanojaa
 * Class encapsulating the browser representation for the job
*/
       public class BrowserRep {
            WebDriver driver;
            DesiredCapabilities capabilities;
            String desc;
            JavascriptExecutor js;
            private static final long MAX_WAIT_S = 45;
            
            public BrowserRep() {
                this.driver = null;
                this.capabilities = null;
                this.desc = "";
                this.js = null;
            }
            
            public BrowserRep(WebDriver driver, DesiredCapabilities capabilities, String desc) {
                this.driver = driver;
                this.capabilities = capabilities;
                this.desc = desc;
                if (driver instanceof JavascriptExecutor) {
                    this.js = (JavascriptExecutor)driver;
                }
            }   
            
            private void setJSDriver() {
                if (this.driver instanceof JavascriptExecutor) {
                    this.js = (JavascriptExecutor)this.driver;
                }
            }
            
            public void setLocalDriver() {
                switch (this.desc) {
                    case "firefox"  : this.driver = new FirefoxDriver();break;
                    case "iexplorer": this.driver = new InternetExplorerDriver();break;
                    case "chrome"   : this.driver = new ChromeDriver();break;
                    case "opera"    : this.driver = new OperaDriver();break;
                    case "htmlunit" : this.driver = new HtmlUnitDriver();break;
                }
                setJSDriver();
                this.driver.manage().timeouts().pageLoadTimeout(MAX_WAIT_S, TimeUnit.SECONDS);
                this.driver.manage().timeouts().implicitlyWait(MAX_WAIT_S, TimeUnit.SECONDS);
            }
            
            public void setRemoteDriver() throws MalformedURLException {
                this.driver = new RemoteWebDriver(new URL(JPagelyzer.seleniumUrl),capabilities);
                setJSDriver();
                this.driver = new Augmenter().augment(this.driver);
                this.driver.manage().timeouts().pageLoadTimeout(MAX_WAIT_S, TimeUnit.SECONDS);
                this.driver.manage().timeouts().implicitlyWait(MAX_WAIT_S, TimeUnit.SECONDS);
            }
            
            public void close() {
                this.driver.close();
                this.driver = null;
                this.js = null;
            }
            
           
        }
