var containerList 	= ["BODY","DIV","UL","DL","P","TABLE","TD","SECTION","HEADER","FOOTER","ASIDE","NAV","ARTICLE","OBJECT","IFRAME","INS"];
var contentList 	= ["SPAN","A","LI","DT","DD","H1","H2","H3","H4","H5","IMG","INS","INPUT","P"];
var excludeList 	= ["SCRIPT","STYLE","AREA","HEAD","META","FRAME","FRAMESET","BR","HR","NOSCRIPT"];
var ignoreList 	 	= ["HTML","TBODY","TR","PARAM","LINK"];

var ac = 5; //0 small blocks <--> 10 big blocks
var dc = 50; //50px max separation distance between blocks for merging process
var tc = 1; //minimum characters to be consider a valid text element not WS or valid innertext string

var dx = dc; //max distance between blocks and page boundaries 
var dy = dc; // used to determine labels to logic objects. Using DC parameter as default.

var blocks = []; //list of all logic objects (blocks)
var geoList = []; //list if all geometric objects (content)

var bomversion = "1.1";

var colors = {PAGE:'#FFFF00',CONTAINER:'#34FF00',CONTENT_CONTAINER:'#6FBEE5',CONTENT:'blue',DEFAULT:'magenta'};

var page; //page logical object

var displayBlocks = true; //for debugging. Write DIVS representing blocks in live web page.

/* ViXML generation functions */

/* escape special characters from link title */
function code(s) {
    var res="";
    if (!s) return "";
    res = s.replace(/&/g,"");
    res = res.replace(/>/g,"");
    res = res.replace(/</g,"");
    res = res.replace(/&quot;/g,"");
    res = res.replace(/(['"])/g, "");
    res = res.replace(/<(?:.|\n)*?>/gm, '');
	res = res.replace(/<[^>]+>/ig,"");
    	res = res.replace(/\s+/g, ' ');
    	res = res.replace(/&/g,"");

    return res;
}

/* get ViXML document */
function getViXML() {
    var cnt = "<XML>\n";
    cnt+="<Document url='"+(document.URL)+"' Title='"+code(document.title)+"' Version='"+bomversion+"' Pos='WindowWidth||PageRectLeft:"+documentDim().w+" WindowHeight||PageRectTop:"+documentDim().h+" ObjectRectWith:0 ObjectRectHeight:0'>\n";
    cnt+=getViXMLObject(page,0,1);
    cnt+="</Document>\n";
    cnt+="</XML>";
    return(cnt);
}

/* get the image list of a block */
function getImageList(obj) {
    var t="<Imgs ID='#ID#' IDList='$ID_LIST_IMAGES$'>\n";
    var a = "";
    var list = [];
    var col = [];

    for (var m=0;m<obj.geometricObjects.length;m++) {
        var geo = obj.geometricObjects[m];

        axc = obj.geometricObjects[m].element.getElementsByTagName("img");
	//alert("img " +axc.length);
        for (p=0;p<axc.length;p++) {
            col.push(axc[p]);
        }

	axc = obj.geometricObjects[m].element.getElementsByTagName("input");
	//alert("input " +axc.length);
	 for (p=0;p<axc.length;p++) {
	  if (axc[p].type == "image") {
            	col.push(axc[p]);
            }
	  }
    }

    col = col.filter(function(elem, pos) {return col.indexOf(elem) == pos;});
    
    for (var i=0;i<col.length;i++) {
        var img = col[i];
        var pt=img.getAttribute("alt");
        a = "<img ID='#ID#' Name=\""+code(pt)+"\" Src='"+code(img.getAttribute('src'))+"'/>"
        var c = CryptoJS.MD5(a);
        list.push(c);
        t += a.replace('#ID#',c)+"\n";
    }
    t+="</Imgs>\n";
    t = t.replace('$ID_LIST_IMAGES$',list.join(","));
    t = t.replace("#ID#",CryptoJS.MD5(t));
    return(t);
}

/* get the link list of a block */
function getLinksList(obj) {
    var t="<Links ID='#ID#' IDList='$ID_LIST_LINKS$'>\n";
    var a = "";
    var list = [];
    var col = [];
    for (var m=0;m<obj.geometricObjects.length;m++) {
        var geo = obj.geometricObjects[m];
        axc = obj.geometricObjects[m].element.getElementsByTagName("a");

        for (p=0;p<axc.length;p++) {
            col.push(axc[p]);
        }
    }
    col = col.filter(function(elem, pos) {return col.indexOf(elem) == pos;});
    
    for (var i=0;i<col.length;i++) {
        var link = col[i];
        var pt=$(link).text();
	all = link.getAttribute('href');
	all = code(all);
 
        a = "<link ID='#ID#' Name=\""+code(pt)+"\" Adr='"+all+"'/>"
        var c = CryptoJS.MD5(a);
        list.push(c);
        t += a.replace('#ID#',c)+"\n";
    }
    t+="</Links>\n";
    t = t.replace('$ID_LIST_LINKS$',list.join(","));
    t = t.replace("#ID#",CryptoJS.MD5(t));
    return(t);
}

/* get text nodes recursively in a block */
function collectTextNodes(element, texts) {
    if (element.tagName.toLowerCase() == "script") return;
    
    for (var child= element.firstChild; child!==null; child= child.nextSibling) {
        if (child.nodeType===3)
            texts.push(child);
        else if (child.nodeType===1)
            collectTextNodes(child, texts);
    }
}

/* get text nodes recursively in a block */

function getTextWithSpaces(element) {
    if (element.tagName.toLowerCase() == "script") return "";
    var texts= [];
    collectTextNodes(element, texts);
    for (var i= texts.length; i-->0;)
        texts[i]= texts[i].data.replace("'","").replace('"',"").replace("&","&amp;");
    return texts.join(' ');
}

function getText(obj) {
    var all="";
    for (var i=0;i<obj.geometricObjects.length;i++) {
        var geo = obj.geometricObjects[i];
        if (geo) {
            if (!isExcluded(geo.element)) {
		$(geo.element).find('script').remove();
		all += getTextWithSpaces(geo.element)
		}
            
        }
    }


    all = all.replace(/<[^>]+>/ig,"");
    all = all.replace(/\s+/g, ' ');
    all = all.replace(/&/,"");
    all = code(all);
    var txt = "<Txts ID=\""+CryptoJS.MD5(all)+"\" Txt=\""+all+"\"/>";
    return(txt);
}



/* get ViXML for a block */
function getViXMLObject(obj,level,pid) {
	if (!obj) return;
	var xml = "";
        var block="";
        var internal = "";
        
	var spc="";
	
	for (var j=0;j<level;j++) spc+=" ";
        
        
        if (obj.terminal()) {
            internal= getLinksList(obj);
            internal+=spc+ getImageList(obj);
            internal+=spc+getText(obj);
            block+=spc+"<Block Ref='Block"+pid+"' leafblock='1' internal_id='"+obj.label+"' ID='"+CryptoJS.MD5(internal)+"' Pos='WindowWidth||PageRectLeft:" +obj.dim.x+ " WindowHeight||PageRectTop:"+obj.dim.y+" ObjectRectWidth:"+obj.dim.w+" ObjectRectHeight:"+obj.dim.h+"' Doc=''>\n"
            block+=internal;
        } else {
            var tt=spc+"<Block Ref='Block"+pid+"' internal_id='"+obj.label+"' ID='#ID#' Pos='WindowWidth||PageRectLeft:" +obj.dim.x+ " WindowHeight||PageRectTop:"+obj.dim.y+" ObjectRectWidth:"+obj.dim.w+" ObjectRectHeight:"+obj.dim.h+"' Doc=''>\n"
            block += tt.replace("#ID#","");
        }
        xml+=block;
        for (var j=0;j<obj.children.length;j++) {
                block = obj.children[j];
                if (block) {
                    xml+=getViXMLObject(block,level+1,pid+"."+j);
                }
        }
        
	xml+= spc+"</Block>\n"
	return(xml);
}

/*
 * Get document geometry width and height;
 * */
function documentDim(win,doc) {
	var w,h;
	if (win) w=win; else win=window;
	if (doc) d=doc; else doc=document;
	return {w:$(doc).width(),h:$(doc).height()};
}

/* count the children of an element */
function elementCount(element) {
	if (!element) return;
	if (isWS(element)) return;
	if (isComment(element)) return;
	if (isText(element)) return;
	
	var count=0;
	for (var i=0;i<obj.childNodes.length;i++) {
		var child = obj.childNodes[i];
		if (child && !isWS(child) && !isComment(child) && !isText(child))
			count++;
	}
	return(count)
}

/* 
 * count the children of a block
 * onlyLeaves=true count only leaves or terminal blocks
 */
function blockCount(obj,onlyLeaves) {
	if (!obj) return;
	if (!obj.block)  return;
	var count;
	if (onlyLeaves && countChildren(obj)>0) 
		count=0;
	else 
		count=1;
	
	for (var i=0;i<obj.children.length;i++) {
		if (obj && obj.block) {
			dat = blockCount(obj.children[i],onlyLeaves);
			if (dat) {
				count+=dat
			}
		}
	}
	return(count);
}

/*
 * build logic objects in base of geometric objects go's
 */
function prepareLogicStructure(go,parent) {
	if (!go) return;
	var log,gchild,lchild;
	log=parent;
	if (go.children.length == 1) {
		log = prepareLogicStructure(go.children[0],parent);
	} else {
		if ( (included(go.type,["CONTAINER","CONTENT_CONTAINER","CONTENT","DEFAULT"])) && (go.geometry) )
		{
			log = createNewLogicalObject(go,parent);
		}
		for (var i=0;i<go.children.length;i++) {
			gchild = go.children[i];
			prepareLogicStructure(gchild,log);
		}
	}
	return(log);
}

/*
 * used for debuging in local javascript console 
 */
function debug(s) {
    console.log(s);
} 

/*
 * main function call to perform segmentation
 * win: the browser window object, frame, etc.
 * pac: parameter of granularity
 * pdc: distance parameter of separation between blocks (used for merging)
 * proclog: process logic structure. True for automatic segmentation
 */ 
function startSegmentation(win,pac,pdc,proclog) {	
		contentWindow = win;
		contentDocument = contentWindow.document;
		ac = pac;
		dc = pdc;
		debug("Starting with AC:"+ac+", DC:"+dc+"px, proclog:"+proclog);
		var root = contentDocument.getElementsByTagName('BODY')[0];
		debug("Processing Content Structure");
		processContentStructure(root,0);
		debug("Processing Geometric Structure");
		var georoot = processGeometricStructure(root,undefined);
		debug("Pre-processing Logic Structure");
		page = prepareLogicStructure(georoot);
		debug("Processing Logic Structure");
		//console.log(root,page)
		processLogicStructure(page,0,1,undefined);
                return(getViXML());
}

/*
 * used to send segmentation results to a remote server
*/
function post_to_url(path, params, method) {
    method = method || "post"; // Set method to post by default if not specified.
    var form = document.createElement("form");
    form.setAttribute("method", method);
    form.setAttribute("action", path);

    for(var key in params) {
        if(params.hasOwnProperty(key)) {
            var hiddenField = document.createElement("input");
            hiddenField.setAttribute("type", "hidden");
            hiddenField.setAttribute("name", key);
            hiddenField.setAttribute("value", params[key]);
            form.appendChild(hiddenField);
         }
    }
    outputDocument.body.appendChild(form);
    form.submit();
}

/* build the xpath of an element */
function getXPath(elt) {
     var path = "";
     for (; elt && elt.nodeType == 1; elt = elt.parentNode)
     {
   	idx = getElementIdx(elt);
	xname = elt.tagName;
	if (idx > 1) xname += "[" + idx + "]";
	path = "/" + xname + path;
     }
     return path.toLowerCase();	
}

/* auxiliar function of xpath one*/
function getElementIdx(elt)
{
    var count = 1;
    for (var sib = elt.previousSibling; sib ; sib = sib.previousSibling)
    {
        if(sib.nodeType == 1 && sib.tagName == elt.tagName)	count++
    }
    
    return count;
}

/*
 * get the absolute position of an element.
 * */

function getRect(obj) {
	if (obj.tagName.toUpperCase() == "BODY") {
		return {x:0, y:0, w:documentDim().w, h:documentDim().h}
	} else {
		//~ r=obj.getBoundingClientRect();
		return {x:$(obj).offset().left, y:$(obj).offset().top, w:$(obj).width(), h:$(obj).height()};
	}
	
}

/*is an element a text node? */
function isText(element) {
	if (element) {
		if (element.nodeName=="#text") {
			return(element.data.trim()!="");
		} else
			return(false);
	} else {
		return(false);
	}
}

/*is an element a whitespace? */
function isWS(element) {
	if (element) {
		if (element.nodeType == 3) {
			if (element.data) {
				return(element.data.trim()=="");
			} else {
				return false
			}
		} else {
			return false;
		}
	} else {
		return(false);
	}
}

/*is an element a comment node? */
function isComment(element) {
	if (element) {
		if (element.nodeType == 8) {
			return true;
		} else {
			return false;
		}
	} else {
		return(false);
	}
}

/*is an element the root element? */
function isRoot(element) {
	if (element) {
		if (element.tagName.toUpperCase() == "BODY") {
			return true;
		} else {
			return false;
		}
	} else {
		return(false);
	}
}

/*belongs an element to the content-container category ? */
function isContentContainer(element) {
	if (!element) return;
	if (isContainer(element)) {
		var etc=0;
		var child;
		var n=element.childNodes.length;
		for (var i=0; i<element.childNodes.length; i++) {
			child = element.childNodes[i];
			if (isContent(child)) {
				etc++;
			}
			if (isWS(child) || isComment(child) || !visible(child) || isExcluded(child) || isIgnored(child)) {
				n--;
			}
		}
		return(etc == n);
	} else {
		return(false);
	}
}

/*belongs an element to the default category ? */
function isDefault(element) {
	if (element.childNodes.length==0) {
		return(true)
	} 
	var ws = 0;
	for (var i=0; i<element.childNodes.length; i++) {
		if (isWS(element.childNodes[i])) {
			ws++;
		}
	}
	return(element.childNodes.length == ws);
}

/*belongs an element to the content category ? */
function isContent(element) {
	if (!element) return;
	if (!element.tagName) return;
	if (isWS(element)) return(false);
	if (isComment(element)) return(false);
	if (isText(element)) return(true);
	var itis = false;
	for (var i=0;i<contentList.length;i++) {
		
		if (contentList[i].toUpperCase() == element.tagName.toUpperCase()) {
			itis = true;
			break;
		}
	}
	return(itis);
}

/*belongs an element to the container category ? */
function isContainer(element) {
	if (!element) return(false);
	if (isWS(element)) return(false);
	if (isComment(element)) return(false);
	if (isText(element)) return(false);
	
	var itis = false;
	for (var i=0;i<containerList.length;i++) {
		if (containerList[i].toUpperCase() == element.tagName.toUpperCase()) {
			itis = true;
			break;
		}
	}
	return(itis);
}

/*is the element excluded of processing? */
function isExcluded(element) {
	if (!element) return(false);
	if (isWS(element)) return(false);
	if (isComment(element)) return(false);
	if (isText(element)) return(false);
	if (!element) return(false);
	
	var itis = false;
	for (var i=0;i<excludeList.length;i++) {
		if (excludeList[i] && element.tagName) {
			if (excludeList[i].toUpperCase() == element.tagName.toUpperCase()) {
				itis = true;
				break;
			}
		}
	}
	return(itis);
}

/*is the element ignored in processing? */
function isIgnored(element) {
	if (!element) return(false);
	if (isWS(element)) return(false);
	if (isComment(element)) return(false);
	if (isText(element)) return(false);
	if (!element) return(false);
	
	var itis = false;
	for (var i=0;i<ignoreList.length;i++) {
		if (ignoreList[i].toUpperCase() == element.tagName.toUpperCase()) {
			itis = true;
			break;
		}
	}
	return(itis);
}

/*which category the element belongs */
function BOMType(element) {
        if (!element) return(null);
        if (isWS(element)) return(null);
	if (isComment(element)) return(null);
	if (isText(element)) return(null);
	if (isExcluded(element)) return(null);
	if (!visible(element)) return(null);
	if (!isContent(element)) {
            var txn = $(element).text();
            if (txn) {
                if ((element.children.length==0) && (txn.trim()=="")) {
                        return(false);
                }
            }
	}
	//this bother on chrome evaluation with this pluging
	if (element.getAttribute("id")) {
		if (element.getAttribute("id").toLowerCase() == "window-resizer-tooltip") 
			return false;
	}
	
	if (isDefault(element)) 			return "DEFAULT";
	if (isContent(element)) 			return "CONTENT";
	if (isContentContainer(element)) 	return "CONTENT_CONTAINER";
	if (isContainer(element)) 			return "CONTAINER";
	if (isRoot(element)) 				return "PAGE";
	return(null);
}

/*build the content structure DOM -> geometric objects */
function processContentStructure(element,level) {
	if (!element) return;
	if (isWS(element)) return(false);
	if (isComment(element)) return(false);
	if (isText(element)) {
		var span = contentDocument.createElement("span");
		span.setAttribute("class","bomwrapper");
		var par = element.parentNode;
		span.appendChild(element);
		par.appendChild(span);
		element = span;
		element.setAttribute("bomtype","CONTENT");
		return;
	}

	if (element.getAttribute("bomtype")) return(false);
	if (isExcluded(element)) return(false);
	if (!visible(element)) return(false);
	if (!valid(element)) return(false);
	if (element.getAttribute('id')=="window-resizer-tooltip") return(false);
	if (isIgnored(element)) {
		for (var i=0; i<element.childNodes.length; i++) {
			processContentStructure(element.childNodes[i],level+1);
		}
		return;
	}
	
	var tn = element.tagName;
	var bt = BOMType(element);
	element.setAttribute("bomtype",bt);
	for (var i=0; i<element.childNodes.length; i++) {
		processContentStructure(element.childNodes[i],level+1);
	}
}

/*refName -> name class#id */
function refName(element) {
	if (!element) return "";
	if (isWS(element)) return("");
	if (isComment(element)) return("");
	if (isText(element)) return("");
	if (isExcluded(element)) return("");
	var name = "(nodef)";
	if (element.tagName) name = element.tagName;
	if (element.getAttribute("id")) name = name + "." + element.getAttribute("id");
	if (element.className) name = name + " " + element.className
	return(name);
}

/*process a geometric object*/
function processGeometricObject(element) {
	var bt = BOMType(element);
	var geo;
	if (bt) {
		element.setAttribute("bomtype",bt);
		var dim = getRect(element);
		var r = dim.x+" "+dim.y+" "+dim.w+" "+dim.h;
		var a =  relativeArea(element);
		element.setAttribute("bomgeometry",r);
		element.setAttribute("bomarea",a);
		element.setAttribute("bomid","C"+(1000+Math.random(1000)));
		geo = createNewGeometricObject(element,element.parent);
	}
	return(geo);
}

/* process the geometric structure */
function processGeometricStructure(element,parent) {
	if (!element) return;
	if (isWS(element)) return(false);
	if (isComment(element)) return(false);
	if (isText(element)) return(false);
	if (isExcluded(element)) return(false);
	
	var dim = getRect(element);
	if ((dim.w<10) || (dim.h<10)) {
		//SKIP IT DO NOT CREATE GEOMETRIC OBJECT
		return;
	}
	var geo = createNewGeometricObject(element,parent);
	
	var k = 0;
	for (var i=0; i<element.childNodes.length; i++) {
		child = element.childNodes[i];
		if (!isWS(child) && !isComment(child) && !isText(child) && !isExcluded(child)) {
			k++;
			processGeometricStructure(element.childNodes[i],geo);
		} 
	}
	return geo;
}

/* is visual cues present: bold, font sizes, background colors, etc.*/
function visuallyDifferent(element) {
	if (element) {
		if (!included(element.style.backgroundColor.toLowerCase(),["","transparent","rgba(0,0,0,0)"])) {
			return(true);
		}
	}
	return(false);
}

/* get set of points of block geometry*/
function getPolygonPoints(dim) {
	var res = [];
	res = res.concat([dim.x,dim.y]);
	res = res.concat([dim.x,dim.y+dim.h]);
	res = res.concat([dim.x+dim.w,dim.y+dim.h]);
	res = res.concat([dim.x+dim.w,dim.y]);
	return(res);
}

/* get set of X-axis of block geometry*/
function getX(points) {
	var x=[];
	for (var i=0;i<points.length;i+=2) {
		x.push(points[i]);
	}
	return(x);
}

/* get set of Y-axis of block geometry*/
function getY(points) {
	var y=[];
	for (var i=1;i<points.length;i+=2) {
		y.push(points[i]);
	}
	return(y);
}

/* is an element already visited */
function visited(element) {
	if (!element) return(true);
	var res;
	if (res = element.getAttribute("visited")) {
		if (res == "true") {
			return(true);
		} else {
			return(false);
		}
	} else {
		return(false);
	}
}


/*is an element visible?*/
function visible(obj) {
	if (isWS(obj)) return(false);
	if (isComment(obj)) return(false);
	if (isText(obj)) return(true);
	if (isExcluded(obj)) return(false);
	
	var xvis=true;
	var xarea=parseFloat((Math.abs(getRect(obj).w-getRect(obj).x))*(Math.abs(getRect(obj).h-getRect(obj).y)));
	if (obj.style) {
		xvis = (included(obj.style.visibility.toUpperCase(),["","VISIBLE","INHERIT"])) && (!included(obj.style.display.toUpperCase(),["NONE"]));
	}
	return(xvis && (xarea>0));
}

/*is an element valid?*/
function valid(obj) {
	var val = false;
	var c=0;
	var ws=0;
	var vok=0;
	var child;
	
	if (isContent(obj)) vok++; 
	
	for (var i=0;i<obj.childNodes.length;i++) {
		child = obj.childNodes[i];
		if (isWS(child)) {
			ws++;
		} else { 
			if (isText(child)) {
				if (child.data.trim().length>=tc) { //mejorar el conteo evitar ws internos
					vok++;
				}
			} else {
				if (isContent(child)) { //i.e.: <img/>, <br/> tags
					vok++;
				} else {
					if (!isExcluded(child) && !isComment(child) ) {
						if (valid(child)) 
							vok++;
					} 
				}
			}
		}
	}
	return(vok>0);
}

/* calcul the distance between two blocks */
function distance(log1,log2) {
	
	var closest = {obj:undefined, value:9999999999};
	
	ileft = 0
	itop = 1
	iright = 2
	ibottom = 3
	
	poly1 = getPolygonPoints(log1.dim);
	
	dleft = PolyK.ClosestEdge	(poly1,log2.dim.x,log2.dim.y);
	dtop = PolyK.ClosestEdge	(poly1,log2.dim.x,log2.dim.y+log2.dim.h);
	dright = PolyK.	ClosestEdge	(poly1,log2.dim.x+log2.dim.w,log2.dim.y+log2.dim.h);
	dbottom = PolyK.ClosestEdge	(poly1,log2.dim.x+log2.dim.w,log2.dim.y);
		
	if (dleft.dist < closest.value) {
		closest.obj = blocks[i];
		closest.value = dleft.dist;
	}
	if (dtop.dist < closest.value) {
		closest.obj = blocks[i];
		closest.value = dtop.dist;
	}
	if (dright.dist < closest.value) {
		closest.obj = blocks[i];
		closest.value = dright.dist;
	}
	if (dbottom.dist < closest.value) {
		closest.obj = blocks[i];
		closest.value = dbottom.dist;
	}
	
	return(closest);
}

/*create a new logic object based on a geometric object geo*/
function createNewLogicalObject(geo,parent) {
	var log = new logicalObject();
	log.parent = parent;
	log.addGeometricObject(geo);
	log.setLabel();
	if (parent)	parent.children.push(log);
	blocks.push(log);
	return(log);
}

/* create a new geometric object based on an element*/
function createNewGeometricObject(element,parent) {
	var geo = new geometricObject();
	geo.addContentElement(element);
	geo.parent = parent;
	if (parent) parent.children.push(geo);
	geoList.push(geo);
	return(geo);
}

/*are they? and which aligment exists between log1 and log2: horizontal or vertical?*/
function getAligment(log1,log2) {
	if ( (Math.abs(log1.dim.x-log2.dim.x)<dc) && (Math.abs(log1.dim.w-log2.dim.w)<dc)) {
		return("V");
	} else { 
		if ( (Math.abs(log1.dim.y-log2.dim.y)<dc) && (Math.abs(log1.dim.h-log2.dim.h)<dc)) {
			return("H");
		} else {
			return(null);
		}
	}
}

/*remove subblock*/
function removeLogicObject(log) {
	if (!log) return;
	//console.log("delete "+log.id+" "+log.label)
	if (log.parent) {
		for(var i=0;i<log.children.length;i++) {
			var child = log.children[i];
			if (child) {
				child.parent = log.parent;
				log.parent.children.push(child);
				log.children[i] = undefined;
			}
		}
		var ind=log.parent.children.indexOf(log);
		log.parent.children[ind]=undefined
	}
	blocks.splice(blocks.indexOf(log),1);
	log.deleteBlock();
	log = undefined

}

/*evaluates a logical object (block)*/
function processLogicalObject(log) {
	if (!log) return;
	if (log.visited) return;
	var dep = false;
	var i,j;
	for (i=0;i<log.children.length;i++) {
		if (log.children[i]) {
			if (log.children[i].relativeArea()>=ac) {
				processLogicStructure(log.children[i])
			} else {
				if (log.children[i].visualCuesPresent) {
					log.children[i].clearChildrenBlocks();
				} else {
					var touch = 0;
					var stop=false;
					if (i<log.children.length-1)
						j=i+1
					else
						j=i
					while (!stop) {
						if (log.children[j]) {
							if (log.children[j] && log.children[j].relativeArea()<ac) {
								if (log.children[j] && (i!=j) && (distance(log.children[i],log.children[j]).value<dc) && (included(getAligment(log.children[i],log.children[j]),["V","H"])) ) {
									log.children[i].mergeWith(log.children[j]);
									
									touch++;
								} 
							} 
						}
						j++;
						stop = (j>=log.children.length);
					}
				}
				if (touch==0) {
					log.children[i].clearChildrenBlocks();
				}
			} 
		}
	}
	log.updateBlock();
}


/* evaluate the logic structure to conform final blocks */
function processLogicStructure(log,level,pid,parent) {
	if (!log) return;
	if (log.visited) return;
	
	if (log.type=="PAGE") {
		processLogicalObject(log);
	} else if (log.type=="CONTAINER"){
		if (log.children.length==1) {
			child = log.children[0];
			removeLogicObject(log);
			processLogicStructure(child,level+1,pid,log);
		} else {
			processLogicalObject(log);
		}
	} else if (log.type=="CONTENT_CONTAINER") {
		if (log.children.length==1) {
			child = log.children[0];
			removeLogicObject(log);
			processLogicStructure(child,level+1,pid,log);
		} else {
			processLogicalObject(log);
		}
	} else if (log.type=="CONTENT") {
		if (log.children.length==1) {
			child = log.children[0];
			removeLogicObject(log);
			processLogicStructure(child,level+1,pid,log);
		} else {
			processLogicalObject(log);
		}
	} else if (log.type=="DEFAULT") {
		processLogicalObject(log);
	} else {
		console.log("WARNING!!!! Element SKIPPED")
	}
	log.visited = true;
}

/* included obj in arr?*/
function included(obj,arr) {
	for (var i=0;i<arr.length;i++) {
		if (arr[i]==obj) {
			return(true);
		}
	}
	return(false);
}

/* ==============================================================*/
/*  LOGIC OBJECT */
/* ==============================================================*/

function logicalObject(obj) {
	this.block = undefined;
	this.id = "L"+blocks.length;
	this.dim = undefined;
	this.parent;
	this.visited = false;
	this.children = [];
	this.type = "";
	this.geometricObjects = [];
	this.label = "STANDARD";
	this.visualCuesPresent = false;

	this.setLabel = function() {
		if (this.dim.x < dx) {
			this.label = "LEFT";
		} else {
			if ((documentDim().w - this.dim.w) < dx) {
				this.label = "RIGHT";
			} else {
				if (this.dim.y < dy) {
					this.label = "TOP";
				} else {
					if ((documentDim().h - this.dim.h) < dy) {
						this.label = "BOTTOM";
					} else {
						this.label = "STANDARD";
					}
				}
			}
		}
	}
	
	this.nextSibling = function() {
		if (this.parent) {
			ind = this.parent.children.indexOf(this);
			if (ind<this.parent.children.length) {
				return(this.parent.children[ind+1]);
			} else {
				return(null);
			}
		} else {
			return(null);
		}
	}
	
	this.previousSibling = function() {
		if (this.parent) {
			ind = this.parent.children.indexOf(this);
			if (ind>0) {
				return(this.parent.children[ind-1]);
			} else {
				return(null);
			}
		} else {
			return(null);
		}
	}
	
	this.clearChildrenBlocks = function() {
		this.visited = true
		for (var i=0;i<this.children.length;i++) {
			if (this.children[i]) {
				this.children[i].clearChildrenBlocks();
				this.children[i].deleteBlock();
				this.children[i] = undefined;
				blocks.splice(blocks.indexOf(this.children[i]),1);
			}
		}
	}
	
	this.mergeWith = function(log) {
		if (!log) return;
		if (this == log) return;
		if (log.geometricObjects) {
			for (var i=0;i<log.geometricObjects.length;i++) {
				this.addGeometricObject(log.geometricObjects[i]);
			}
		}
		if (log.parent) {
			var ind=log.parent.children.indexOf(log);
			log.parent.children[ind]=undefined
		}

		log.deleteBlock();
		
		for (var i=0;i<log.children.length;i++) {
			child = log.children[i];
			if (child) {
				child.parent = this;
				child.visited = true;
				this.children.push(child);
			}
		}
		
		if (this.type!="CONTAINER") {
			if (log.type!="DEFAULT") {
				if (this.type != log.type) this.type = log.type;
			}
		}
		this.clearChildrenBlocks();
		blocks.splice(blocks.indexOf(log),1);
		log = undefined
		this.updateBlock();
	}
	
	this.normW = function() {
		return 100*this.dim.w/documentDim().w;
	}
	this.normH = function() {
		return 100*this.dim.h/documentDim().h;
	}

	this.normarea = function() {
		var cont = 0;
		for (var i=0;i<100;i+=10) {
			if (this.normW()>i) cont++;
		}
		var dw = cont;
		cont = 0;
		for (var i=0;i<100;i+=10) {
			if (this.normH()>i) cont++;
		}
		var dh = cont;
		
		var d = (dw*dh)*10/100;
		return(d);
	}
	this.relativeArea = function() {
		if (page) {
			return(this.normarea());
		} else {
			return(10);
		}
	}
	
	this.bomgeometry = function() {
		return [this.dim.x,this.dim.y,this.dim.w,this.dim.h];
	}
	
	this.makeid = function()
	{
		var text = "";
		var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

		for( var i=0; i < 25; i++ )
			text += possible.charAt(Math.floor(Math.random() * possible.length));

		return text;
	}
	
	this.addChild = function(child) {
		child.parent = this;
		this.children.push(child);
	}
	
	this.addGeometricObject = function(geo) {
		if (!geo) return;
		if (!geo.element) return;
		this.geometricObjects.push(geo);
		
		var nr;
		var r;
		if (!this.dim) {
			if (geo.element.tagName.toUpperCase()=="BODY") {
				this.type='PAGE';this.id="PAGE";
				this.dim = {x:0,y:0,w:documentDim(parent.contentWindow,parent.contentDocument).w,h:documentDim(parent.contentWindow,parent.contentDocument).h}
			} else  {
				this.dim = {x:0,y:0,w:0,h:0}
				this.dim = geo.geometry;
				this.type = geo.type;
			}
			r = getPolygonPoints(this.dim);
		} else {
			r = getPolygonPoints(this.dim).concat(getPolygonPoints(geo.geometry));
			if (included(geo.type,["CONTAINER","CONTENT_CONTAINER"])) {
				this.type = geo.type;
			}
		}
		
		xs = getX(r);
		ys = getY(r);
		
		this.dim.x = Math.min.apply(null, xs);
		this.dim.y = Math.min.apply(null, ys);
		this.dim.w = Math.max.apply(null, xs)-this.dim.x;
		this.dim.h = Math.max.apply(null, ys)-this.dim.y;
		
		if (!this.block) {
			this.block = this.insertBlock();
		}
		var g = this.dim.x+" "+this.dim.y+" "+this.dim.w+" "+this.dim.h;
		this.block.setAttribute("bomgeometry",g);
		this.block.setAttribute("bomtype",this.type);
		color=colors[this.type]; 
		
		if (displayBlocks) {
                    this.block.setAttribute("style","border : 2px solid black;z-index: 10000;position:absolute;background-color:transparent;border-color:"+color+";color:black;font-weight:bold;opacity:1"); 
                    this.block.style.left = this.dim.x+"px";
                    this.block.style.top = this.dim.y+"px";
                    this.block.style.width = this.dim.w+"px";
                    this.block.style.height = this.dim.h+"px";
                }
		geo.visited = true;
		var sfc;
		var bgc;
		bgc = contentWindow.getComputedStyle(geo.element,null).getPropertyValue("background-color");
		fc = parseInt(contentWindow.getComputedStyle(geo.element,null).getPropertyValue("font-size"));
		if (bgc!="rgba(0, 0, 0, 0)") {
			this.visualCuesPresent = true;
		}

		if (geo.element.parentNode) {
			t=0
			for (var k=0;k<geo.element.parentNode.children.length;k++) {
				if ( (geo.element.parentNode.children[k]!=geo) ) {
					var sfc = parseInt(contentWindow.getComputedStyle(geo.element.parentNode.children[k],null).getPropertyValue("font-size")); 
					if (2*sfc < fc) 
						t++;
				}
			}
			if (t>0) 
				this.visualCuesPresent = true;
		}
		this.updateBlock();
	}
	
	this.insertBlock = function() {
            var vc="";
            var block = document.createElement('div');
            if (this.visualCuesPresent) vc="VC";
            block.innerHTML = "<span visited='true' class='bomauxtext' style='opacity:1;color:black;font-size:12pt'>"+this.id+" - "+this.relativeArea().toFixed(4) +"<br>"+vc+"</span>";
            block.setAttribute("class","block");
            block.setAttribute("visited","true");
            block.setAttribute("id",this.makeid());
            contentDocument.body.appendChild(block);
            return(block);
	}
	
	this.updateBlock = function() {
		var aaa=this.relativeArea().toFixed(4);
		if (this.countChildren()==0) {
                    this.setOn();
                } else {
                    this.hide();
                }
		this.block.innerHTML = "<span visited='true' class='bomauxtext' style='opacity:1;color:black;font-size:12pt'>"+this.id+" - "+aaa +"</span>";
	}
	
	this.setOn = function() {
		if (!this.block) return;
		var c = colors[this.type]
		if (!c) c="black";
		this.block.style.backgroundColor = c;
		this.block.style.opacity = "0.5";
		this.block.style.border = "2px dotted black";
		this.block.style.color = "white";
	}
	
	this.setOff = function() {
		if (!this.block) return;
		this.block.style.backgroundColor = "transparent";
		this.block.style.color = "black";
		this.block.style.opacity = "1";
		this.block.style.border = "2px solid "+colors[this.type];
	}
	this.hide = function() {
		if (!this.block) return;
		this.block.style.backgroundColor = "transparent";
		this.block.style.opacity = "1";
		this.block.style.border = "0px solid transparent";
	}
	this.deleteBlock = function() {
		if (this.block) contentDocument.body.removeChild(this.block);
		this.block=undefined;
	}
	this.countCover = function() {
		var cont=0;
		for (var i=0;i<this.geometricObjects.length;i++) {
			cont+=this.geometricObjects[i].countCover();
		}
		return(cont);
	}
	this.countChildren = function() {
		var cont=0;
		for (var i=0;i<this.children.length;i++) {
			if (this.children[i]) {
				cont++;
			}
		}
		return(cont);
	}
        this.terminal = function() {
            return(this.countChildren()==0);
        }
}

function relativeArea(element) {
		var r,p;
		if (element.parentNode) {
			r = getPolygonPoints(getRect(element));
			p = getPolygonPoints(getRect(element.parentNode))
			ra = Math.abs(PolyK.GetArea(r));
			rp = Math.abs(PolyK.GetArea(p));
			res = ra / rp;
			if (res>1) res=1;
			return(res);
		} else {
			return(1);
		}
	}

/* ==============================================================*/
/*  GEOMETRIC OBJECT */
/* ==============================================================*/

function geometricObject() {
	this.children = [];
	this.parent = undefined;
	this.element = undefined;
	this.type = undefined;
	this.geometry = undefined;
	this.area = undefined;
	this.id = undefined;
	this.visited = false;
	
	this.addContentElement = function(element) {
		this.element = element;
		this.bt = BOMType(this.element);
		if (this.bt) {
			this.type = this.bt;
			this.geometry=getRect(this.element);
			this.area = relativeArea(this.element);
			this.id = "C"+(1000+Math.random(1000));
		}
	}
	this.getGeometry = function() {
		return(this.dim.x+" "+this.dim.y+" "+this.dim.w+" "+this.dim.h);
	}
	this.countCover = function() {
		return($(this.element).find('*').length)
	}
}


