Pagelyzer (Maven)
===================================================================

### How to run it

	$ mvn compile
	
	$ mvn exec:java -Dexec.args="-get score -url1 http://www.lip6.fr -url2 http://www.upmc.fr -config src/main/resources/ext/config.xml"

* The configuration file can be anywhere else
* The values in the configuration are taken as defaults
* These defaults can be overriden by parameters

For example, asuming that the value in the configuration file set as:

	pagelyzer.run.default.parameter.get = "score"
	
then

	$ mvn exec:java -Dexec.args="-url1 http://www.lip6.fr -url2 http://www.upmc.fr -config src/main/resources/ext/config.xml"

it is similar to the first above

The only mandatory parameters are:
* the *URLS* and 
* the *CONFIGURATION* file.
	


	
