Pagelyzer 
====================================================================

Installation and Configuration Manual - Java (Standalone Version)
====================================================================

## Installing Dependencies

    $ sudo apt-get install openjdk-7-jdk
    $ sudo apt-get install xvfb (optional)

Using Pagelyzer will popup a window to render the web pages (to get a screenshot and also to do the segmentation). You can use Xvfb to make it headless. Xvfb is used incase if the Graphical User Interface (GUI) is not available in your system but also not the open pop-up window.  You should first describe a display and then execute the jars on this display:

    $ Xvfb:1 -screen 0 1024x768x24 &
    $ DISPLAY=:1 java -jar ....


This ReadMe will explain the usage of different jar files obtained from this code source: https://github.com/openplanets/pagelyzer
Different executable jars generated from source code can be downloaded from http://scape.lip6.fr/Pagelyzer_all-jars.zip

## Command-line Parameters

Example_configFiles contains different configuration examples and its ReadMe explains each tag. 
SettingsFiles folder is a "must" folder. After downloading it, you can change its name but you should not change its subfolders names. js and ext folder should be always in the same folder. 
Then you update the subdir tag in the config file.


### Training

If you want to define "what is similar" and "what is dissimilar" according to your needs, you can first train the system:


| parameter 	| description 													|
| ------------ | :----------------------------------------: | ------------------------------------------------------------- |  
| config.xml | This is the path to the configuration file. Different examples can be found here: https://github.com/openplanets/pagelyzer/tree/master/Example_configFiles 
| annotations.txt | The path to a file that contains the annotated dataset to train the system.This is the file where you describe which pair of urls are simmilar/dissimilar. The file should have the following structure: URL1 \t URL2 \t ANNOTATION (0 dissimilar 1 similar).

    $ java -cp Pagelyzer-libs.jar:PagelyzerTrain.jar  pagelyzer.Train config.xml   annotations.txt
    $ java -jar java  -jar ./target/PagelyzerTrain-0.0.1-SNAPSHOT-jar-with-dependencies.jar config.xml   annotations.txt

This generates an output file and save it based on the settings on configuration file by "subdir" tag (config.xml). This file contains the information related to decision boundary and SVM and is used for comparison.


### Comparison

We can compare the web pages as follows:


| parameter 	| arguments 							  	| description 													|
| ------------ | :----------------------------------------: | ------------------------------------------------------------- |  
| url1 | URI | First URL |
| url2 | URI | Second URL |
| config | path | path to the configuration file (config.xml)	  |


    $ java -jar Pagelyzer-0.0.1-SNAPSHOT-jar-with-dependencies.jar -url1 http://www.lip6.fr -url2 http://www.lip6.fr  -config  config.xml
    $ java -cp Pagelyzer-libs.jar:Pagelyzer.jar  pagelyzer.JPagelyzer -url1 http://www.lip6.fr -url2  http://www.lip6.fr  -config config.xml

This will give a score between -1 and 1. All the scores negatives mean that the pages are dissimilar and all scores positive mean that the pages are similar. 
The values are also ranked which means that two pages with a score 0.9 is more similar than two pages with a score 0.2. However, the score 0 means that the system is 
not able to decide if the pages are similar or not. It means that the training dataset is small or do not contain the diverse examples. 
The suggestion in that case is to train the system again with a bigger dataset. 


### Test

This section will show you how to make tests with a bunch of url pairs at the same time.


| parameter 	| description 													|
| ------------ | :----------------------------------------: | ------------------------------------------------------------- |  
| test.txt | The path to a file that contains a list of urls that you would like to test URL1 \t URL2
| config.xml  | The path to the configuration file (config.xml)
| result.txt | The path to the file where you would like to save the results URL1 \t URL2 \t Score

    $java  -cp Pagelyzer-libs.jar:Pagelyzer-0.0.1-SNAPSHOT-tests.jar Test test.txt  config.xml results.txt



