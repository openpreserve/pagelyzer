/*
# Andrés Sanoja
# UPMC - LIP6
# pagelyzer 
#
# Copyright (C) 2011, 2012, 2013, 2014 Andrés Sanoja, Université Pierre et Marie Curie -
# Laboratoire d'informatique de Paris 6 (LIP6)
#
# Authors
# Andrés Sanoja andres.sanoja@lip6.fr
# Alexis Lechervy alexis.lechervy@lip6.fr
# Zeynep Pehlivan zeynep.pehlivan@lip6.fr
# Myriam Ben Saad myriam.ben-saad@lip6.fr
# Marc Law marc.law@lip6.fr
# Carlos Sureda carlos.sureda@lip6.fr
# Jordi Creus jordi.creus@lip6.fr
# LIP6 / Université Pierre et Marie Curie

# Responsables WP
# Matthieu CORD/UPMC
# Stéphane GANÇARSKI/UPMC
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#
# Some parts of this package are adapted from the BrowserShot proyect developed by IM, France.
# https://github.com/sbarton/browser-shot-tool-mapred
 */

package pagelyzer;
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
 * Class encapsulating the browser representation for the job
 * @author sanojaa
 * This class is a adaptation from the BrowserRep class from BrowserShot_mapred proyect
*/
       public class BrowserRep {
           /**
            * The webdriver instance
            */
            WebDriver driver;
            /**
             * The browser caparilities
             */
            DesiredCapabilities capabilities;
            /**
             * A description of the browser
             */
            String desc;
            /**
             * Reference to the Browser's javascript interpreter
             */
            JavascriptExecutor js;
            /**
             * Max time out
             */
            private static final long MAX_WAIT_S = 45;
            
            /**
             * Constructor
             */
            public BrowserRep() {
                this.driver = null;
                this.capabilities = null;
                this.desc = "";
                this.js = null;
            }
            
            /**
             * Constructor
             * @param driver the selenium webdriver
             * @param capabilities the capabilities of the browser
             * @param desc the description of the browser
             */
            public BrowserRep(WebDriver driver, DesiredCapabilities capabilities, String desc) {
                this.driver = driver;
                this.capabilities = capabilities;
                this.desc = desc;
                if (driver instanceof JavascriptExecutor) {
                    this.js = (JavascriptExecutor)driver;
                }
            }   
            
            /**
             * Sets the javascript interpreter if present to the driver
             */
            private void setJSDriver() {
                if (this.driver instanceof JavascriptExecutor) {
                    this.js = (JavascriptExecutor)this.driver;
                }
            }
            
            /**
             * Set the selenium driver as local (webdriver instance)
             */
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
            
            /**
             * Set the selenium driver as remote (selenium grid or hub)
             * @param url the web page to process
             * @throws MalformedURLException 
             */
            public void setRemoteDriver(String url) throws MalformedURLException {
                this.driver = new RemoteWebDriver(new URL(url),capabilities);
                setJSDriver();
                this.driver = new Augmenter().augment(this.driver);
                this.driver.manage().timeouts().pageLoadTimeout(MAX_WAIT_S, TimeUnit.SECONDS);
                this.driver.manage().timeouts().implicitlyWait(MAX_WAIT_S, TimeUnit.SECONDS);
            }
            
            /**
             * Close the current selenium instance
             */
            public void close() {
                this.driver.close();
                this.driver = null;
                this.js = null;
            }
            
           
        }
