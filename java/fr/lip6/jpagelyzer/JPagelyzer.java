/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.lip6.jpagelyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import Taverna.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author sanojaa
 */
public class JPagelyzer {

     public static String seleniumUrl = "http://127.0.0.1:8015/wd/hub";
     public static int port = 8016;
     public static boolean local = false;
     public static String seleniumStat = "";
     public static boolean debugshot = false;
     public static String debugPath = null;

    /**
     * @param url1
     * @param url2
     * @param browser1
     * @param browser2
     * @param mode
     * @param granularity
     */
    
    public void changeDetection(String url1,String url2,String browser1,String browser2,String mode,String cFile) {
        
        Capture capture1 = new Capture();
        Capture capture2 = new Capture();
        
        capture1.setup(browser1);
        capture2.setup(browser2);
        
        Taverna.ScapeTest marcalizer = new Taverna.ScapeTest();
        
        marcalizer.init(new File(cFile));
        
        switch (mode) {
            case "images"    : capture1.run(url1, true, false); 
                               capture2.run(url2, true, false); 
                               marcalizer.run(capture1.result.getBufferedImage(), capture2.result.getBufferedImage());
                               break;
            case "structure" : 
                               capture1.run(url1, false, true); 
                               capture2.run(url2, false, true); 
                               marcalizer.run(capture1.result.viXML,capture2.result.viXML);
                               break;
            case "hybrid"    : 
                               capture1.run(url1, true, true);
                               capture2.run(url2, true, true);
                               marcalizer.run(capture1.result.viXML,capture2.result.viXML,capture1.result.getBufferedImage(), capture2.result.getBufferedImage());
                               break;
        }
        
        if (JPagelyzer.debugshot) {
            if (JPagelyzer.debugPath.endsWith("/")) {
                JPagelyzer.debugPath = JPagelyzer.debugPath.substring(0, JPagelyzer.debugPath.length() - 1);
            }
            capture1.result.saveDebugFile(JPagelyzer.debugPath + "/page1.png");
            capture2.result.saveDebugFile(JPagelyzer.debugPath + "/page2.png");
        }
        
        
        try {        
            capture1.cleanup();
            capture2.cleanup();
        } catch (IOException | InterruptedException ex) {
                Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void get(String target, String url,String browser,String outputfile) {
        Capture capture = new Capture();
        capture.setup(browser);
        switch(target) {
            case "screenshot"   : capture.run(url,true,false);break;
            case "segmentation" : capture.run(url,false,true);break;
            case "source"       : capture.run(url,false,false);break;
        }
        CaptureResult result = capture.result;
        
        OutputStream out=null;
        try {
             out = new BufferedOutputStream(new FileOutputStream(outputfile));
             switch(target) {
                case "screenshot"   : out.write(result.image);break;
                case "segmentation" : out.write(result.viXML.getBytes("UTF-8"));break;
                case "source"       : out.write(result.srcHTML.getBytes("UTF-8"));break;
            }
             out.close();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
         }
        try {        
            capture.cleanup();
        } catch (IOException | InterruptedException ex) {
                Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void usage(Options options){
    // Use the inbuilt formatter class
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "jPagelyzer help", options );
    }
    
    /**
    * @param args the command line arguments
    **/
    public static void main(String[] args) {
        String url1="";
        String url2="";
        String url="";
        String ofile="";
        String browser="firefox";
        String browser1 = "firefox";
        String browser2 = "firefox";
        String mode = "images";
        String cpath = "/home/sanojaa/src/JPagelyzer/src/ext/";
        String cfile = cpath + "/ex_"+mode+".xml";
        
        Options options = new Options();
        options.addOption("get",true,"Funcionality to run");
        options.addOption("url1",true,"First URL");
        options.addOption("url",true,"web page URL");
        options.addOption("url2",true,"Second URL");
        options.addOption("browser1",true,"Browser for first URL");
        options.addOption("browser2",true,"Browser for second URL");
        options.addOption("browser",true,"Browser for rendering");
        options.addOption("cpath",true,"Parameters configuration path");
        options.addOption("cmode",true,"Comparation mode");
        options.addOption("granularity",true,"Segmentation granularity");
        options.addOption("hub",true,"Selenium Server hub full address http://<host>:<port>/wd/hub. Default: http://127.0.0.1:8015/wd/hub");
        options.addOption("port",true,"Internal jPagelyzer internal server port. Default: 8016");
        options.addOption("ofile",true,"Output file");
        options.addOption("local",false,"Use local selenium WebDriver instead of server");
        options.addOption("debugshot",false,"get image files of after-rendering. Only used when -get score parameter is used");
        options.addOption("debugpath",true,"path for storing debug image files of after-rendering");
        
        if (JPagelyzer.debugshot) {
            System.out.println("Debug mode. Path "+JPagelyzer.debugPath);
        }
        
        CommandLineParser parser = new BasicParser();
        CommandLine cmd;
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException pe) { 
            usage(options); return; 
        }
        if (!cmd.hasOption("get")) {usage(options);System.exit(0);}
        
        JPagelyzer pagelyzer = new JPagelyzer();
        
        if (cmd.hasOption("local")) {JPagelyzer.local = true;}
        
        if (JPagelyzer.local) {
            JPagelyzer.seleniumStat = "Selenium: local WebDriver";
        } else {
            JPagelyzer.seleniumStat = "Selenium: remote "+JPagelyzer.seleniumUrl;
        }
        
        switch(cmd.getOptionValue("get")) {
            case "score":
                if (cmd.hasOption("url1")) {url1 = cmd.getOptionValue("url1");} else {System.out.println("URL1 parameter missing");System.exit(0);}
                if (cmd.hasOption("url2")) {url2 = cmd.getOptionValue("url2");} else {System.out.println("URL2 parameter missing");System.exit(0);}
                if (cmd.hasOption("granularity")) {
                    int x = Integer.parseInt(cmd.getOptionValue("granularity"));
                    int y = 10 - x;
                    Capture.granularity = (new Integer(y).doubleValue()) / (new Integer(10).doubleValue());
                }
                if (cmd.hasOption("browser1")) browser1 = cmd.getOptionValue("browser1");
                if (cmd.hasOption("browser2")) browser2 = cmd.getOptionValue("browser2");
                if (cmd.hasOption("cmode")) mode = cmd.getOptionValue("cmode");
                if (cmd.hasOption("hub")) {JPagelyzer.seleniumUrl = cmd.getOptionValue("hub");}
                if (cmd.hasOption("debugshot")) {JPagelyzer.debugshot = true;}
                if (cmd.hasOption("debugpath")) {JPagelyzer.debugPath = cmd.getOptionValue("debugpath");}
                if ((JPagelyzer.debugshot) && (JPagelyzer.debugPath == null)) {
                    System.out.println("Debug was activated, but no path is specified to put the files. Use -debugpath path");
                    System.exit(0);
                }
                if (cmd.hasOption("cpath")) {
                    cpath = cmd.getOptionValue("cpath");
                } else {
                    cpath = System.getProperty("user.dir") + "/ext/";
                }
                if (cpath.endsWith("/")) {
                    cpath = cpath.substring(0, cpath.length() - 1);
                }
                if (cmd.hasOption("port")) {JPagelyzer.port = Integer.parseInt(cmd.getOptionValue("port"));}
                cfile = cpath + "/ex_"+mode+".xml";
                System.out.println("Using parameters found in " + cfile);
                System.out.println("Change detection. Mode: "+mode+". Port:" + JPagelyzer.port+". "+JPagelyzer.seleniumStat);
                pagelyzer.changeDetection(url1,url2,browser1,browser2,mode,cfile);
                break;
            case "screenshot":
                ofile ="image.png";
                if (cmd.hasOption("url")) {url = cmd.getOptionValue("url");} else {System.out.println("URL parameter missing");System.exit(0);}
                if (cmd.hasOption("browser")) browser = cmd.getOptionValue("browser");
                if (cmd.hasOption("ofile")) ofile = cmd.getOptionValue("ofile");
                System.out.println("Screenshot. "+JPagelyzer.seleniumStat);
                pagelyzer.get("screenshot",url, browser, ofile);
                break;
            case "source":
                ofile ="page.html";
                if (cmd.hasOption("url")) {url = cmd.getOptionValue("url");} else {System.out.println("URL parameter missing");System.exit(0);}
                if (cmd.hasOption("browser")) browser = cmd.getOptionValue("browser");
                if (cmd.hasOption("ofile")) ofile = cmd.getOptionValue("ofile");
                System.out.println("Source. "+JPagelyzer.seleniumStat);
                pagelyzer.get("source",url, browser, ofile);
                break;
            case "segmentation":
                ofile ="blocks.xml";
                if (cmd.hasOption("url")) {url = cmd.getOptionValue("url");} else {System.out.println("URL parameter missing");System.exit(0);}
                if (cmd.hasOption("browser")) browser = cmd.getOptionValue("browser");
                if (cmd.hasOption("ofile")) ofile = cmd.getOptionValue("ofile");
                if (cmd.hasOption("port")) {JPagelyzer.port = Integer.parseInt(cmd.getOptionValue("port"));}
                if (cmd.hasOption("granularity")){Capture.granularity = (10.0-Integer.parseInt(cmd.getOptionValue("granularity"))) / 10.0;}
                System.out.println("Segmentation. Selenium: "+JPagelyzer.seleniumUrl);
                pagelyzer.get("segmentation",url, browser, ofile);
                break;
        }
    }
    
}
