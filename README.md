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

# Enviroment Verification and Configuration
The tools pagelyzer: analyzer, changedetection and capture are written in Ruby 1.9.1. In the other
hand for the change detection process others tools are used that are written in Java, therefore this
should be taken into account in the enviroment verification process. The development enviroment
was Linux Ubuntu, the package description is done following its repositories, but in theory
should be compatible with Debian repos.

## Ruby Installation
We need to be carefull with this step because the software won't work on the 1.8.x versions of Ruby.
sudo apt-get install ruby1.9.1-full
After that we should check that both, ruby and rubygems, are been properly installed.

`$ ruby -v`
`1.9.2p290 (2011-07-09 revision 32533) [i686-linux]`

It is enough to match the version number. Any doubts there are several tutorials to do this [1]. Now
we check the rubygems package manager:

`$ gem -v`
`1.3.7`

# Instalation of Pagelyzer 0.9

Pagelyzer is a set of components that can be used (most of them) independently, but in the case of
change detection they are all used as a chain for simplicity of integration.
The software can be downloaded from:
http://www-poleia.lip6.fr/~sanojaa/pagelyzer-ruby-0.9.1-standalone.zip
Now we un-compress the zip file in the desired destination
The folder structure should be like the following:

* pagelyzer
* Gemfile
* bin/
* bin/pagelyzer_analyzer
* bin/pagelyzer_capture
* bin/pagelyzer_changedetection
* data/
* js/
* doc/
* ext/
* ext/marcalizer
* lib/
* out 

Note: out folder is intended to be an output folder, but it is optional. Can be overridden with
parameters.

## Installing Dependencies
After the language and the package manager are properly configured and installed, we may proceed
to install the dependencies:
`$ sudo apt-get install libxslt-dev libxml2-dev`
`$ sudo apt-get install openjdk-7-jdk`
`$ sudo apt-get install imagemagick`
`$ sudo apt-get install xvfb`

Note1: Installing the selenium-webdriver may cause some warnings in text encoding that should be
fine, in almost all the cases.
Note 2: The java installation is a reference to remember that it should be present.
Note 3: ImageMagick 6 is optional, it is needed for thumbnailing. This thumbs area is useful for integrating with 
other tools and for future optimization of change detection process .

We need to install also some ruby libraries needed by the software. This step can be done simple
using Bundler gem. To install it:

`$ sudo gem install bundler`

Get into the project folder and type:

`$ bundle` 

When finished we will have all dependencies installed.

## Command-line Parameters

pagelyzer:

`USAGE: ./pagelyzer [--help|--version] [<command> <command_options>]`

The available commands are:
* capture
* analyzer
* changedetection
* train

Capture:

`USAGE: ./pagelyzer capture --url=URL [--output-folder=FOLDER] [-browser=BROWSER_CODE] [--thumbnail] [--headless] [--help] [--no-screenshot]`

Analyzer:

`USAGE: ./pagelyzer analyzer --decorated-file=FILE [--output-file=FILE] [-pdoc=(0..10)] [--version] [--help]`

Changedetection:

`USAGE: ./pagelyzer_changedetection [--url1=URL --url2=URL | urls=FILE] [conf=CONF_FILE] [--doc=(1..10)] [--browser=BROWSER_CODE | --browser1=BROWSER_CODE --browser2=BROWSER_CODE] [--verbose] --type=[images|structure|hybrid] [--headless]`

Browsers code are the same as defined in selenium. For instance:
* firefox (default)
* chrome
* iexploreproxy
* safariproxy
* opera

For the input URL file it expects the following syntax of each line:
 - URL1 URL2


# Examples

1. Capture a web page with default parameters:

`$ ./pagelyzer capture --url=http://www.google.fr`

It will copy to $HOME_FOLDER/pagelyzer the outcome. If the folder does not exist it will be created. It will create three files: 

* firefox_www_google_fr.html (rendered version of the web page)
* firefox_www_google_fr.dhtml (rendered version with visual cues included for segmentation algorithm)
* firefox_www_google_fr.png (webshot of the page)

2. Change detection on two pages with default parameters

`$ ./pagelyzer changedetection --url1=http://www.host.com/page1.html --url2=http://www.host.com/page2.html`

3. Change detection on two pages with hybrid method

`$ ./pagelyzer changedetection --url1=http://www.host.com/page1.html --url2=http://www.host.com/page2.html --type=hybrid`

will create the same files as above for each url and also the ViXMLs and delta file.

4. Change detection with different browsers

`$ ./pagelyzer changedetection --url1=http://www.host.com/page1.html --url2=http://www.host.com/page2.html --browser1=firefox --browser2=chrome`

url1 will be evaluated with browser1 and url2 with browser2

4. Change detection with same browser (the most common case)

`$ ./pagelyzer changedetection --url1=http://www.host.com/page1.html --url2=http://www.host.com/page2.html --browser=firefox`

same browser for both urls

## Remarks:
* Firefox driver is the default to selenium. For installing other browsers can reference to [2],
e.g. to run pagelyzer on your chrome/chromium instance, you should install the ChromeDriver before:
* Download the appropriate version from http://code.google.com/p/chromedriver/downloads/list 
* Unzip it and copy it to a visible folder, e.g:
  `$ sudo cp chromedriver /usr/bin/`

For command-line parameters is better to escape them, e.g:

./pagelyzer analyzer --decorated-file=/my/path with/spaces -- only processes /my/path !
./Pagelyzer analyzer --decorated-file=/my/path\ with/spaces -- results in correct behaviour

* If no Degree of Coherence is given, a default of doc=6 will be chosen.
* The URL's should include the http schema
`--url=http://www.host.com ---it is ok!
 --url=host.com ---won't work!`

# External References:
[1] http://answers.oreilly.com/topic/2845-installing-ruby-1-9-on-a-debian-or-ubuntu-system/

[2] http://code.google.com/p/selenium/wiki/FrequentlyAskedQuestions#Q:_Which_browsers_does_WebDriver_support?
