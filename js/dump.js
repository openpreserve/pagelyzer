/*
Andrés Sanoja
 UPMC - LIP6



 dump.js

 Requires: Selenium Web Driver

 Copyright (C) 2011, 2012 Andrés Sanoja, Université Pierre et Marie Curie -
 Laboratoire d'informatique de Paris 6 (LIP6)

 Contributors: Stephane Gançarski - LIP6

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

var dump_loaded = true;
var dump_text = "";
var cont=1;

function puts(s) {
	dump_text += s;
}

function textNode(pNode) {
	if (pNode.prop("tagName")) //asuming this as text node condition
		return false
	else
		return true;
}

function rgb2hex(rgbString) {
	if (rgbString != "rgba(0, 0, 0, 0)") {
		var parts = rgbString.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
		delete (parts[0]);
		for (var i = 1; i <= 3; ++i) {
			parts[i] = parseInt(parts[i]).toString(16);
			if (parts[i].length == 1) parts[i] = '0' + parts[i];
		}
		return "#"+parts.join('');
	} else {
		return "#ffffff"
	}
}

function walk(pNode,nLevel) {
	var tab="";
	var src = "";
	var aux="";
	for(var k=0;k<nLevel;k++) {tab+=" ";}
	var attr = " uid='"+cont+"'";
	if (pNode.attr('id')) {
		attr += " id='"+(pNode.attr('id'))+"'";
	} else {
		attr += " id='element-"+(cont)+"'";
	}
	attr += " left='"+pNode.offset().left+"'";
	attr += " top='"+pNode.offset().top+"'";
	attr += " width='"+(pNode.offset().left+pNode.outerWidth())+"'";
	attr += " height='"+(pNode.offset().top+pNode.outerHeight())+"'";
	attr += " margin_left='"+pNode.css('marginLeft')+"'";
	attr += " background_color='"+pNode.css("background-color")+"'";
	attr += " font_size='"+pNode.css('font-size')+"'";
	attr += " font_weight='"+pNode.css('font-weight')+"'";
	attr += " display='"+pNode.css('display')+"'";
	attr += " visibility='"+pNode.css('visibility')+"'";
	if (pNode.attr('style')) {
		attr += " style='"+pNode.attr('style')+"'";
	}
	cont+=1;
	
	if (pNode.prop("tagName") == 'A') {
		if (pNode.prop("href"))
			attr += " href='"+pNode.prop("href")+"'";
	}
	
	if ((pNode.prop("tagName")!='TBODY') && (pNode.prop("tagName")!='IMG') && (pNode.prop("tagName")!="CANVAS") && (pNode.attr('id')!='fxdriver-screenshot-canvas'))
		src += tab + "<"+pNode.prop("tagName")+" "+ attr +">\n";
	if (pNode.prop("tagName")=='IMG')
		src += tab + "<"+pNode.prop("tagName")+" "+ attr +" src='"+pNode.attr('src')+"' alt='"+pNode.attr('alt')+"'/>\n";
	//if (pNode.attr('style').trim().substring(0,16)=='background-image:') 
		//src += "<IMG src='"+pNode.attr('style').trim().substring(17)+"'/>\n";
	
	pNode.contents().each(function() {
			if (textNode($(this))) {
				if ($(this).text().trim() != "")
					aux=$("<phantom/>").text($(this).text().trim()).html();
					src += (tab+"   ") + aux+"\n"; //TODO: is a good idea to hide text and use TEXT token instead
					aux="";
			} else {
				
				src += walk($(this),nLevel+1);
			}
	});
	if ((pNode.prop("tagName")!='TBODY') && (pNode.prop("tagName")!='IMG') && (pNode.prop("tagName")!="CANVAS") && (pNode.attr('id')!='fxdriver-screenshot-canvas'))
		src += tab + "</"+pNode.prop("tagName")+">\n";
	return(src);
}

function dump_start() {
	var src = ""
	$('window').width(1024);
	$('window').height(768);
	var now = new Date();
	var then = now.getFullYear()+'-'+now.getMonth()+'-'+now.getDay()+' '+now.getHours()+':'+now.getMinutes()+":"+now.getSeconds();
	src += "<!-- window: {width : "+ $(window).width() + ", height: " + $(window).height()+"} -->\n";
	src += "<!-- document: {width: "+ $(document).width() + ", height: " + $(document).height()+"} -->\n";
	src += "<!-- page: {url: "+  $(location).attr('href') + ", date: "+ then +"} -->\n";
	src += walk($('html'),0);
	return(src);
}
