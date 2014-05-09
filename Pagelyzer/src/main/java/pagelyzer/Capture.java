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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.XMLConfiguration;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.*;

/**
 * Class that encapsulates selenium web driver (local and remote)
 * @author sanojaa
 */
public class Capture {
    public BrowserRep browser = new BrowserRep();
    public CaptureResult result = new CaptureResult();
    private XMLConfiguration config;
    private int ATTEMPTMAX=10;
    /**
     * Constructor
     * @param config2 the current configuration
     */
    public Capture(XMLConfiguration config2) {
        this.config = config2;
    }
        /**
         * This function is a adaptation from the initWebDriver() method from BrowserShot_mapred proyect
         * Initialize the webdriver given the capability object
         * @param capability
         * @return 
         */
        public void initWebDriver() {
            String msg = "";
            boolean done = false;
            int attemptNo = 0;
            while (!done && attemptNo<ATTEMPTMAX) {// to limit attempts to 10 ZP
                attemptNo++;
                try {
                    System.out.println("Attempt = "+attemptNo);
                    if (config.getString("selenium.run.mode").equals(JPagelyzer.LOCAL)) {
                        this.browser.setLocalDriver();
                    } else {
                        this.browser.setRemoteDriver(config.getString("selenium.server.url"));
                    }
                    done = true;
                } catch (MalformedURLException e) {
                        throw new RuntimeException("Invalid Selenium driver URL", e);
                } catch (IOException e) {
                    if (config.getBoolean("pagelyzer.run.verbose")) {msg = e.toString();}
                    System.out.println("Attempt failed sleeping for 10s."+msg);
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Capture.class.getName()).log(Level.SEVERE, null, ex);
                    }                
                } catch (WebDriverException e) {
                    if (config.getBoolean("pagelyzer.run.verbose")) {msg = e.toString();}
                    System.out.println("Attempt failed sleeping for 10s."+msg);
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Capture.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (Exception e) {
                    if (config.getBoolean("pagelyzer.run.verbose")) {msg = e.toString();}
                    System.out.println("Some proble arrived :("+msg);
                }
            }
            
            
        }
    
        /**
         * Prepare a new instance of webdriver for browser
        * This function is a adaptation from the setup() method from BrowserShot_mapred proyect
        * @param browser the current browser to setup
        **/
        public void setup(String browser) {     
            System.out.println("Setting up browser: "+browser);
            DesiredCapabilities capability = null;
            if  (browser.equals("firefox")) {
                capability = DesiredCapabilities.firefox();
            }
            else if (browser.equals("opera")) {
                capability = DesiredCapabilities.opera();
            }
            else if (browser.equals("chrome")) {
                capability = DesiredCapabilities.chrome();
                capability.setCapability("chrome.switches", Arrays.asList("--disable-logging"));
            }else {
                throw new RuntimeException("Browser "+browser+ " not recognized.");
            }

            capability.setPlatform(Platform.LINUX);                
            this.browser.desc = browser;
            this.browser.capabilities = capability;
            initWebDriver();
        }
        
        /**
         * Close the current webdriver instance
        * This function is a adaptation from the cleanup() method from BrowserShot_mapred proyect
        * @throws IOException
        * @throws InterruptedException
        **/
        public void cleanup() throws IOException, InterruptedException {     
               this.browser.driver.close();
        }
        
        
//        private static String getStringFromInputStream(InputStream is) {
//
//		BufferedReader br = null;
//		StringBuilder sb = new StringBuilder();
// 
//		String line;
//		try {
// 
//			br = new BufferedReader(new InputStreamReader(is));
//			while ((line = br.readLine()) != null) {
//				sb.append(line);
//			}
// 
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (br != null) {
//				try {
//					br.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
// 
//		return sb.toString();
// 
//	}
        
        /**
         * Execute a capture from a current browser instance.
        * This function is a adaptation from the getScreenShotWithTimeout() method from BrowserShot_mapred proyect
     * @param url the web page to capture
     * @param screenshot indicates if a page screenshot should be taken
     * @param segmentation indicates if the segmentation should be done
     * @return CaptureResult the result of the capture
        **/
        public CaptureResult process(String url,boolean screenshot,boolean segmentation) {
            String serverlink;
            boolean local = true;
            ServerLyzer server = null;
            String srcJS = "";
            String jqueryJS = "";
            String polyKJS = "";
            String cryptoJS = "";
                    
            System.out.println("getting data using driver: "+this.browser.desc);
            
            if (segmentation) {
                if ((config.getString("pagelyzer.run.internal.server.remote.url")==null)) {
                    serverlink = "http://"+config.getString("pagelyzer.run.internal.server.local.ip")+":"+config.getString("pagelyzer.run.internal.server.local.port");
                    local=true;
                } else {
                    serverlink = config.getString("pagelyzer.run.internal.server.remote.url");
                    local=false;
                }
                srcJS = serverlink +"/bomlib.js";
                jqueryJS = serverlink + "/jquery-min.js";
                polyKJS = serverlink + "/polyk.js";
                cryptoJS = serverlink + "/md5.js";
            }
            
            try {
                this.browser.driver.get(url);
                String title = this.browser.driver.getTitle();
                result.srcHTML = this.browser.driver.getPageSource();
                
                System.out.println("title: "+title);
                if (screenshot) {
                    result.image =  ((TakesScreenshot)this.browser.driver).getScreenshotAs(OutputType.BYTES);
                }
                if (segmentation) {
                    if (local) {
                        server = new ServerLyzer(config);
                        int port = config.getInt("pagelyzer.run.internal.server.local.port");
                        server.start(port,config.getString("pagelyzer.run.internal.server.local.wwwroot"));
                    }
                    
                    this.browser.js.executeScript("var s=window.document.createElement('script');s.setAttribute('id','bominject');s.setAttribute('src','"+srcJS+"');window.document.head.appendChild(s)");
                    this.browser.js.executeScript("var j=window.document.createElement('script');j.setAttribute('id','bomjquery');j.setAttribute('src','"+jqueryJS+"');window.document.head.appendChild(j)");
                    this.browser.js.executeScript("var k=window.document.createElement('script');k.setAttribute('id','bompolyk');k.setAttribute('src','"+polyKJS+"');window.document.head.appendChild(k)");
                    this.browser.js.executeScript("var q=window.document.createElement('script');q.setAttribute('id','bompolyk');q.setAttribute('src','"+cryptoJS+"');window.document.head.appendChild(q)");
                    
                    WebDriverWait wait = new WebDriverWait(this.browser.driver,120);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bominject")));
                   
                    String bomversion = (String) this.browser.js.executeScript("return bomversion");
                   
                    System.out.println("Using BoM algorithm v"+bomversion + " pAC=" + config.getString("bom.granularity"));
                    result.viXML = (String) this.browser.js.executeScript("return startSegmentation(window," + config.getString("bom.granularity") + "," + config.getString("bom.separation")+ ",false)");


                    //result.viXML = (String) this.browser.js.executeScript("return startSegmentation(window," + Capture.granularity + ",50,false);");
                    //if (config.getLogic("pagelyzer.debug.screenshots.active")) {
                     //   result.debug =  ((TakesScreenshot)this.browser.driver).getScreenshotAs(OutputType.BYTES);
                    //}
                    // WHy not to use just image? ZP
                    if (local) {
                        server.stop();
                    }
                }
            } catch(WebDriverException e) {
                System.out.println("ERROR: Could not load " + url);
                if (config.getString("selenium.run.mode").equals(JPagelyzer.REMOTE)) {
                    System.out.println("Can not connect to server "+config.getString("selenium.server.url"));
                }
                System.out.println("Trying to reinitialize browser");
                if (server != null) server.stop();
                System.out.println(e);
                try {
                    this.browser.driver.close();
                } catch (WebDriverException ex) {
                    System.out.println("ERROR: cannot close browser ");
                }
                    
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                } catch (InterruptedException e2) {
                }

                initWebDriver();
                return null;
                
            } catch (Throwable e) {
                if (server != null) server.stop();
                if (config.getString("selenium.run.mode").equals(JPagelyzer.REMOTE)) {
                    System.out.println("Can not connect to server "+config.getString("selenium.server.url"));
                }
                System.out.println("ERROR: Could not load " + url);
                System.out.println(e);
                return null;
            }
            
            return(result);
        }   

    /**
     * Invoke the capture process
     * @param url the web page to capture
     * @param screenshot indicates if the screenshot should be taken
     * @param segmentation indicates if the segmentation should be done
     */
    public void run(String url,boolean screenshot,boolean segmentation) {
            this.result = process(url,screenshot,segmentation);
        }
}
