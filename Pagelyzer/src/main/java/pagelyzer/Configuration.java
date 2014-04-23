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
import java.util.HashMap;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * Represent the configuration of the application. The configuration has all the default values 
 * that comes from the config.xml file.
 * Implements a HashMap with those parameters from the command line.
 * @author sanojaa
 */
public class Configuration extends XMLConfiguration {
    /**
     * values that come from as commandline arguments
     **/
    private HashMap<String,String> values;
    /**
     * Constant local
     **/
    public static final String LOCAL = "local";
    /**
     * Constant remote
     */
    public static final String REMOTE = "remote";
    /**
     * Constant score
     */
    public static final String SCORE = "score";
    /**
     * Constant screenshot
     */
    public static final String SCREENSHOT = "screenshot";
    /**
     * Constant source
     */
    public static final String SOURCE = "source";
    /**
     * Constant segmentation
     */
    public static final String SEGMENTATION = "segmentation";
    
    /**
     * Constructor
     * @param configFilePath path to the configuration file
     * @throws ConfigurationException
     */
    public Configuration(String configFilePath) throws ConfigurationException {
        super(configFilePath);
        this.values = new HashMap<String,String>();
    }

    /**
     * Get a value from the configuration. 
     * @param key the key for the value
     * @return the value from the HashMap if present, else return the value from the config file
     */
    public String get(String key) {
        String ret="";
        if (this.values.containsKey(key)) {
            ret = this.values.get(key);
        } else {
            try {
                ret = this.getString(key);
            } catch(Exception ex) {
               return(null);
            }
        } 
        return(ret);
    }

    /**
     * Get a value from the configuration as boolean. 
     * @param key the key for the value
     * @return the value from the HashMap if present, else return the value from the config file
     */
    public boolean getLogic(String key) {
        boolean ret;
        if (this.values.containsKey(key)) {
            ret = this.values.get(key)=="true";
        } else {
            try {
                ret = this.getBoolean(key);
            } catch(Exception ex) {
               return(false);
            }
        } 
        return(ret);
    }

    /**
     * Get a value from the configuration as integer. 
     * @param key the key for the value
     * @return the value from the HashMap if present, else return the value from the config file
     */
    public int getNumber(String key) {
        int ret;
        if (this.values.containsKey(key)) {
            ret = Integer.parseInt(this.values.get(key));
        } else {
            try {
                ret = this.getInt(key);
            } catch(Exception ex) {
               return(-999999);
            }
        } 
        return(ret);
    }

    /**
     * Set a new value in the HashMap values
     * @param key the key for the new value
     * @param value the new value
     */
    public void set(String key,String value) {
        this.values.put(key, value);
    }
}
