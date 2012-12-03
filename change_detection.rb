#!/usr/bin/env ruby
#-*- mode: ruby; encoding: utf-8 -*-
# Andrés Sanoja
# UPMC - LIP6
#
#
#
# change_detection.rb
#
# Requires: Ruby 1.9.1+ (1.8.x versions won't work), rubygems 1.3.7+
#
# Copyright (C) 2011, 2012 Andrés Sanoja, Université Pierre et Marie Curie -
# Laboratoire d'informatique de Paris 6 (LIP6)
#
# Contributors: Stephane Gançarski - LIP6
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
# This script covers all the change detection process. It is integrated with
# the pageanalyzer, marcalizer and VIDIFF tools.
#
# Default Degree of Coherence (DOC=6)
#
# ISSUES:
# The js-files-url parameter should be a URL accesible for a browser. 

require 'open3'

def usage
	puts "USAGE: ruby change_detection.rb --url1=URL --url2=URL [--doc=(1..10)] [--output-folder=FOLDER] [--browser=BROWSER_CODE] [--verbose]"
end

def help
	usage
	puts "This tool aims integrates all the change detection and segmentation tools"
	puts "Help:"
	puts " type = hybrid | webshot\n"
	puts "Browsers code are the same as defined in selenium. For instance:"
	puts " - firefox"
	puts " - chrome"
	puts " - iexploreproxy"
	puts " - safariproxy"
	puts " - opera"
	puts
end

if ARGV==[]
	usage
	exit 
end

output_folder = "out"
url1 =""
url2=""
browser = "firefox"
js_files_url =""
thumb=false
type = 'hybrid'
doc=6
webshot1 = ""
webshot2 = ""
verbose = false

ARGV.each do |op|
	sop = op.strip.split("=")
	url1 			= sop[1] if sop[0] == "--url1"
	url2 			= sop[1] if sop[0] == "--url2"
	browser 		= sop[1] if sop[0] == "--browser"
	output_folder 	= sop[1] if sop[0] == "--output-folder"
	js_files_url 	= sop[1] if sop[0] == "--js-files-url"
	doc 			= sop[1] if sop[0] == "--doc"
	webshot1 		= sop[1] if sop[0] == "--webshot1"
	webshot2 		= sop[1] if sop[0] == "--webshot2"
	type 			= sop[1] if sop[0] == "--type"
	verbose 		= true if op.strip.split("=")[0] == "--verbose"
	thumb 			= true if op.strip.split("=")[0] == "--thumbnail"
	
	if op[0..6] == "--help"
		help
		exit
	end
	if op[0..9] == "--version"
		puts "SCAPE Change Detection. Version 0.9"
		puts "UPMC - LIP6"
		exit
	end
end

#No longer needed
#~ if js_files_url.nil? or js_files_url==""
	#~ puts "ERROR: parameter --js-files-url not included. Sorry, can't continue"
	#~ exit
#~ end

filename1 = url1.gsub('/','_').gsub('http://','')
filename2 = url2.gsub('/','_').gsub('http://','')

cmd = []

cmd.push "ruby capture.rb --url=#{url1} #{"--thumbnail" if thumb} --browser=#{browser} --output-folder=#{output_folder}"
cmd.push "ruby capture.rb --url=#{url2} #{"--thumbnail" if thumb} --browser=#{browser} --output-folder=#{output_folder}"

cmd.push "ruby pageanalyzer.rb --decorated-file=#{output_folder}/#{browser}_#{filename1}_decorated.html --pdoc=#{doc} --output-file=#{output_folder}/#{browser}_#{filename1}.xml"
cmd.push "ruby pageanalyzer.rb --decorated-file=#{output_folder}/#{browser}_#{filename2}_decorated.html --pdoc=#{doc} --output-file=#{output_folder}/#{browser}_#{filename2}.xml"

cmd.push "cp #{output_folder}/#{browser}_#{filename1}.xml vidiff/v1.xml"
cmd.push "cp #{output_folder}/#{browser}_#{filename2}.xml vidiff/v2.xml"

cmd.push "cd vidiff;java -jar DIFF.jar v1.xml v2.xml"
cmd.push "cp vidiff/Delta.xml marcalizer/in/xml/1/delta/delta.xml"
cmd.push "cp vidiff/Delta.xml #{output_folder}/delta.xml"

cmd.push "cp #{output_folder}/#{browser}_#{filename1}.png marcalizer/in/images/1/view1.png"
cmd.push "cp #{output_folder}/#{browser}_#{filename1}.xml marcalizer/in/xml/1/vips1.xml"

cmd.push "cp #{output_folder}/#{browser}_#{filename2}.png marcalizer/in/images/1/view2.png"
cmd.push "cp #{output_folder}/#{browser}_#{filename2}.xml marcalizer/in/xml/1/vips2.xml"

if type == 'hybrid'
	cmd.push "cd marcalizer; java -jar marcalizer.jar -snapshot1 view1.png -snapshot2 view2.png -vips1 in/xml/ -vips2 useless -vidiff useless"
else
	cmd.push "cd marcalizer; java -jar marcalizer.jar -snapshot1 view1.png -snapshot2 view2.png"
end

stdin=nil
stdout=nil
stderr=nil
cmd.each do |c|
	unless verbose
		stdin,stdout,stderr = Open3.popen3(c)
		#puts stderr.readlines
	else
		system(c)
	end
end
puts stdout.readlines.last unless verbose
