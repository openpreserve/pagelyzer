FAQ
==============

1 - What does the Pagelyzer score mean? 

Pagelyzer score is normalized between -1 and 1. Negative scores mean that two pages you compare are dissimilar, positif scores 
mean that they are similar. Between two scores, for example URL1-URL2 with score 0.5 and URL1-URL3 with score 0.9, we can say that 
 URL1-URL3 is more similar than  URL1-URL2. 

2 - What does 0 score mean?

Pagelyzer is based on a supervised binary classifier with hyperplane at 0. Thus, if you have 0 as score, it means that the system is not able to decide 
if two urls are similar or not. You can try to train your system with more data to avoid this.

3 - I have an image (text) collection that I would like to compare different pairs. Can I use Pagelyzer? 

No and yes. You can not use pagelyzer directly with the existing svm files because these files contain information learned for specific annotation and just to compare web pages. 
But the idea behind Pagelyzer can be used. For this, you need to use Pagelyzer-Train function and update it to read images.

4 - Testing giteclipse


