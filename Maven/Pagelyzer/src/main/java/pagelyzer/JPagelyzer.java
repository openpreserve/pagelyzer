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

import Scape.MarcAlizer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;

/**
 * Class to calculate de change detection between two web pages
 * @author sanojaa
 */
public class JPagelyzer {

     private Configuration config;

    /**
     * get the current configuration
     * @return the current configuration
     */
    public Configuration getConfig() {
        return(this.config);
    }

    /**
     *
     * @param configParam the current configuration
     * @throws ConfigurationException
     */
    public void init(String configParam) throws ConfigurationException {
        this.config = new Configuration(configParam);
    }
    
    /**
    * Method to detect the changement on two web pages versions. It prints the score
    * @param url1 the first web page
    * @param url2 the second web page
    **/
    public void changeDetection(String url1,String url2) {
    	String cfile = config.get("pagelyzer.run.default.comparison.path")+"/"+config.get("pagelyzer.run.default.comparison.file");

        MarcAlizer marcalizer = new MarcAlizer();
      
        try {
        marcalizer.init(new File(cfile), config.get("pagelyzer.run.default.comparison.path"));
        } catch (Exception ex) {
            System.err.println("Marcalize could not be initialized");
            System.exit(0);
        }
        
        Capture capture1 = new Capture(this.config);
        Capture capture2 = new Capture(this.config);
        
        capture1.setup(config.get("pagelyzer.run.default.parameter.browser1"));
        capture2.setup(config.get("pagelyzer.run.default.parameter.browser2"));
        
        
        switch (config.get("pagelyzer.run.default.comparison.mode")) {
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
        
        if (config.getLogic("debug.screenshots.active")) {
            String apath="";
            if (config.get("debug.screenshots.path").endsWith("/")) {
                apath = config.get("debug.screenshots.path");
                apath = apath.substring(0, apath.length() - 1);
            }
            capture1.result.saveDebugFile(apath + "/" + config.get("pagelyzer.debug.screenshots.filepattern").replace("#{n}", "1"));
            capture2.result.saveDebugFile(apath + "/" + config.get("pagelyzer.debug.screenshots.filepattern").replace("#{n}", "2"));
        }
        
        
        try {        
            capture1.cleanup();
            capture2.cleanup();
        } catch (IOException | InterruptedException ex) {
                Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Method to call functionalities
     * @param target extra functionality. It can be: screenshot, segmentation, source
     * @param url the web page to process
     */
    public void get(String target, String url) {
        Capture capture = new Capture(config);
        capture.setup(this.config.get("pagelyzer.run.default.parameter.browser"));
        switch(target) {
            case Configuration.SCREENSHOT   : capture.run(url,true,false);break;
            case Configuration.SEGMENTATION : capture.run(url,false,true);break;
            case Configuration.SOURCE       : capture.run(url,false,false);break;
        }
        CaptureResult result = capture.result;

        OutputStream out=null;
        try {
            String ext="";
            
            switch(target) {
                case Configuration.SCREENSHOT   :ext="png";break;
                case Configuration.SEGMENTATION :ext="xml";break;
                case Configuration.SOURCE       : ext="html";break;
            }
             out = new BufferedOutputStream(new FileOutputStream(this.config.get("pagelyzer.run.default.parameter.outputfile").replace("#{ext}", ext)));
             switch(target) {
                case Configuration.SCREENSHOT   : out.write(result.image);break;
                case Configuration.SEGMENTATION : out.write(result.viXML.getBytes("UTF-8"));break;
                case Configuration.SOURCE       : out.write(result.srcHTML.getBytes("UTF-8"));break;
            }
            out.close();
            capture.cleanup();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
         } catch ( IOException | InterruptedException ex) {
             Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
    /**
     * print the help usage of this application
    * @param options the command line arguments
    **/
    private static void usage(Options options){
    // Use the inbuilt formatter class
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "jPagelyzer help", options );
    }
    
    /**
    * @param args the command line arguments
     * @throws URISyntaxException 
    **/
    public static void main(String[] args) throws URISyntaxException {
        String url1="";
        String url2="";
        String url="";
        
        JPagelyzer pagelyzer = new JPagelyzer();
        
        Options options = new Options();
        options.addOption("get",true,"Funcionality to run");
        options.addOption("url1",true,"First URL");
        options.addOption("url",true,"web page URL");
        options.addOption("url2",true,"Second URL");
        options.addOption("browser1",true,"Browser for first URL");
        options.addOption("browser2",true,"Browser for second URL");
        options.addOption("browser",true,"Browser for rendering");
        options.addOption("cpath",true,"Parameters configuration path");
        options.addOption("config",true,"Global configuration file");
        options.addOption("cmode",true,"Comparation mode");
        options.addOption("granularity",true,"Segmentation granularity");
        options.addOption("ofile",true,"Output file");
        options.addOption("verbose",false,"Verbose output");
        
        CommandLineParser parser = new BasicParser();
        CommandLine cmd;
        
        /* Parsing comandline parameters*/
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException pe) { 
            usage(options); return; 
        }
        
        /* verifying tha configuration file has been passed as parameter. Die if it is not */
        
        if (!cmd.hasOption("config")) {usage(options);System.exit(0);}
        
        /* Loading and parsing configuration XML file. Override defaults from config if that is the case */
        try {
             pagelyzer.init(cmd.getOptionValue("config"));
             if (cmd.hasOption("local"))        pagelyzer.getConfig().set("selenium.run.mode", Configuration.LOCAL);
             if (cmd.hasOption("debugshot"))    pagelyzer.getConfig().set("pagelyzer.debug.screenshots.active", "true");
             if (cmd.hasOption("debugpath"))    pagelyzer.getConfig().set("pagelyzer.debug.screenshots.path",cmd.getOptionValue("debugpath"));
             if (cmd.hasOption("verbose"))      pagelyzer.getConfig().set("pagelyzer.run.verbose","true");
             if (cmd.hasOption("get"))          pagelyzer.getConfig().set("pagelyzer.run.default.parameter.get",cmd.getOptionValue("get"));
             if (cmd.hasOption("hub"))          pagelyzer.getConfig().set("selenium.server.url", cmd.getOptionValue("hub"));
             if (cmd.hasOption("browser1"))     pagelyzer.getConfig().set("pagelyzer.run.default.browser1",cmd.getOptionValue("browser1"));
             if (cmd.hasOption("browser2"))     pagelyzer.getConfig().set("pagelyzer.run.default.browser2",cmd.getOptionValue("browser2"));
             if (cmd.hasOption("ofile"))        pagelyzer.getConfig().set("pagelyzer.run.default.outputfile",cmd.getOptionValue("ofile"));
             if (cmd.hasOption("cmode"))
             {
                                                pagelyzer.getConfig().set("pagelyzer.run.default.comparison.mode",cmd.getOptionValue("cmode"));
                                                pagelyzer.getConfig().set("pagelyzer.run.default.comparison.file","ex_"+cmd.getOptionValue("cmode")+".xml");
             }
             if (cmd.hasOption("cpath"))        pagelyzer.getConfig().set("pagelyzer.run.default.comparison.path",Utils.checkLastSlash(cmd.getOptionValue("cpath")));
             if (cmd.hasOption("granularity"))  pagelyzer.getConfig().set("bom.granularity",cmd.getOptionValue("granularity"));
         } catch (ConfigurationException ex) {
             Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
         }
        
        
        /* Validate program intrinsic input parameters and configuration */
        if (pagelyzer.getConfig().get("pagelyzer.run.default.parameter.get")==null) {usage(options);System.exit(0);}
        if (pagelyzer.getConfig().get("selenium.run.mode").equals(Configuration.LOCAL)) {
           System.out.println("Selenium: local WebDriver");
        } else {
           System.out.println("Selenium: remote " + pagelyzer.getConfig().get("selenium.server.url"));
        }
        if (( pagelyzer.getConfig().getLogic("pagelyzer.debug.screenshots.active")) && (pagelyzer.getConfig().get("pagelyzer.debug.screenshots.path") == null)) {
            System.out.println("Debug was activated, but no path is specified to put the files. Use -debugpath path or change the configuration file");
            System.exit(0);
        }
        
        if (pagelyzer.getConfig().get("pagelyzer.run.default.parameter.get").equals(Configuration.SCORE) ) {
            if (!cmd.hasOption("url1")) {
                System.out.println("URL1 parameter missing");
                System.exit(0);
            } else url1 = cmd.getOptionValue("url1");
            if (!cmd.hasOption("url2")) {
                System.out.println("URL2 parameter missing");
                System.exit(0);
            } else url2 = cmd.getOptionValue("url2");
            
            /* 
            * assure that if the comparison configuration is not passed neither as parameter in the commandline
            * nor in the configuration file (commented), use the one included as resource with the jar file
            */
            if (pagelyzer.getConfig().get("pagelyzer.run.default.comparison.path")==null) {
                pagelyzer.getConfig().set("pagelyzer.run.default.comparison.path",pagelyzer.getClass().getResource(pagelyzer.getConfig().get("pagelyzer.run.default.comparison.subdir")).getPath());
            }
        } else {
            if (!cmd.hasOption("url")) {
                System.out.println("URL parameter missing");
                System.exit(0);
            } else url = cmd.getOptionValue("url");
        }

        /*
        * All is validated and fine, we can proceed to call functionalities
        */
        switch(pagelyzer.getConfig().get("pagelyzer.run.default.parameter.get")) {
            case Configuration.SCORE:
                String cfile=pagelyzer.getConfig().get("pagelyzer.run.default.comparison.path") + "/"+pagelyzer.getConfig().get("pagelyzer.run.default.comparison.file");
                System.out.println("Using parameters found in " + cfile);
                System.out.println("Change detection. Mode: "+pagelyzer.getConfig().get("pagelyzer.run.default.comparison.mode")+". Port:" + pagelyzer.getConfig().get("pagelyzer.run.internal.server.local.port"));
                pagelyzer.changeDetection(url1,url2); 
                break;
            case Configuration.SCREENSHOT:
                pagelyzer.get(Configuration.SCREENSHOT,url);
                break;
            case Configuration.SOURCE:
                pagelyzer.get(Configuration.SOURCE,url);
                break;
            case Configuration.SEGMENTATION:
                pagelyzer.get(Configuration.SEGMENTATION,url);
                break;
        }
        
    }
    
}
