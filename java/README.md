Pagelyzer Installation and Configuration Manual (Standalone Version)
====================================================================

Suite of tools for detecting changes in web pages and their rendering
---------------------------------------------------------------------

Authors
-------
* Andrés Sanoja <andres.sanoja@lip6.fr>
* Alexis Lechervy <alexis.lechervy@lip6.fr> 
* Zeynep Pehlivan <zeynep.pehlivan@lip6.fr>
* Myriam Ben Saad <myriam.ben-saad@lip6.fr>
* Marc Law <marc.law@lip6.fr>
* Carlos Sureda <carlos.sureda@lip6.fr>
* Jordi Creus <jordi.creus@lip6.fr>

LIP6 / Université Pierre et Marie Curie

Responsables WP
---------------

* Matthieu CORD/UPMC
* Stéphane GANÇARSKI/UPMC

This work was partially supported by the SCAPE Project. The SCAPE project is co-funded
by the European Union under FP7 ICT-2009.4.1 (Grant Agreement number 270137).

Important Note:
----------------
Some parts of this package are adapted from the BrowserShot proyect developed by IM, France. https://github.com/sbarton/browser-shot-tool-mapred


## Installing Dependencies

    $ sudo apt-get install openjdk-7-jdk
    $ sudo apt-get install xvfb (optional)

Note1: Installing the selenium-webdriver may cause some warnings in text encoding that should be
fine, in almost all the cases.

## Prerequisites

JPagelyzer can be used either with a selenium server or using the Webdriver class.

Download standalone server in [3]

and run it:

    $ java -jar selenium-server-standalone-2.24.1.jar -port 8015

It may be other port if needed.

## Command-line Parameters


Usage: java -jar JPagelyzer -get arg [options]

General Parameters
---------------- 
 
| parameter 	| arguments 							  	| description 													|  
| ------------ | :----------------------------------------: | ------------------------------------------------------------- |  
| get 			| score, source, screenshot, segmentation 	| Funcionality to run: score, source, screenshot, segmentation 	|  
| hub			| URI										| Selenium Server hub full address http://host:port/wd/hub. Default: http://127.0.0.1:8015/wd/hub |  
| local        | none | Use local selenium WebDriver class instead of server |  
| ofile 		| path |     Output file  |  
| port			| number |      Internal jPagelyzer internal server port. Default: 8016 (in general this doesn't need to be changed only if this port is used by another application)  |  
  
Score functionality (change detection)
---------------------------------------
 
| parameter 	| arguments 							  	| description 													|
| ------------ | :----------------------------------------: | ------------------------------------------------------------- |  
| url1 | URI | First URL |
| url2 | URI | Second URL |
| browser1 | browsercode |	Browser for first URL  |
| browser2 | browsercode |	Browser for second URL  |
| cmode | comparation mode | Comparation mode: images (default), structure and hybrid |
| cpath | path | Parameters configuration path  |
| granularity | number | Segmentation granularity from 1-10 range (3 default)  |
 
Screenshot, Segmentation and Source functionalities
-----------------------------------------------------  
 
| parameter 	| arguments 							  	| description 													|
| ------------ | :----------------------------------------: | ------------------------------------------------------------- |  
| url | URI | web page URL | 
| browser |  browser code | Browser for render URL (default: firefox )   | 

Debuging
------------------ 
| parameter 	| arguments 							  	| description 													|
| ------------ | :----------------------------------------: | ------------------------------------------------------------- |  
| debugpath |  path | path for storing debug image files of after-rendering  | 
| debugshot | none | get image files of after-rendering. Only used when -get score parameter is used  | 

Browsers code are the same as defined in selenium. For instance:  
* firefox (default)
* chrome
* iexploreproxy
* safariproxy
* opera
* for more detail consult [1]

# Examples

1. Capture a web page screenshot with default parameters:  
$ java -jar JPagelyzer.jar -get screenshot 
       -url=http://www.google.fr 
       -ofile image.png
2. Change detection on two pages with default parameters  
    $ java -jar JPagelyzer.jar -get score -url1=http://www.host.com/page1.html -url2=http://www.host.com/page2.html
3. Change detection on two pages with hybrid method  
    $ java -jar JPagelyzer.jar -get score -url1=http://www.host.com/page1.html -url2=http://www.host.com/page2.html -cmode hybrid
4. Change detection with different browsers  
    $ java -jar JPagelyzer.jar -get score -url1=http://www.host.com/page1.html -url2=http://www.host.com/page2.html -cmode hybrid -browser1 firefox -browser2 chrome
4. Change detection without connecting to Selenium server  
    $ java -jar JPagelyzer.jar -get score  -url1=http://www.host.com/page1.html -url2=http://www.host.com/page2.html -local
5. Change detection using selenium server in different myhost and myport  
    $ java -jar JPagelyzer.jar -get score  -url1=http://www.host.com/page1.html -url2=http://www.host.com/page2.html -hub http://myhost:myport/wd/hub  
6. Using custom parameters configuration file  
    $ java -jar JPagelyzer.jar -get score  -url1=http://www.host.com/page1.html -url2=http://www.host.com/page2.html -cpath /my/path/ext/ex_myparams.xml  

## Remarks:
* Firefox driver is the default to selenium. For installing other browsers can reference to [2],
e.g. to run pagelyzer on your chrome/chromium instance, you should install the ChromeDriver before:
* Download the appropriate version from http://code.google.com/p/chromedriver/downloads/list 
* Unzip it and copy it to a visible folder, e.g:
  `$ sudo cp chromedriver /usr/bin/`

* If no granularity parameter is given, a default of 3 will be chosen.
* The URL's should include the http schema  
`--url=http://www.host.com ---it is ok!` 
`--url=host.com ---won't work!`  

* IMPORTANT: remember to set the paths in configuration files located in ext/ folder.

# External References:

[1] http://docs.seleniumhq.org/about/platforms.jsp#browsers  

[2] http://code.google.com/p/selenium/wiki/FrequentlyAskedQuestions#Q:_Which_browsers_does_WebDriver_support?  

[3] https://code.google.com/p/selenium/downloads/list  
