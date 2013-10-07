#! /usr/bin/ruby1.9.1
#-*- mode: ruby; encoding: utf-8 -*-
# Andrés Sanoja
# UPMC - LIP6
#
#
#
# pagelyzer_capture
#
# Requires: Ruby 1.9.1+ (1.8.x versions won't work), rubygems 1.3.7+ and ImageMagick 6.6.0-4+
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
# REMARKS:
# ImageMagick is not mandatory but it is used for processing thumbnails of webshots
# this thumbns area usefull for integrating with other tools and for future optimization
# of change detection process ignoring parameter --thumb should do the trick
#

require 'selenium-webdriver'
#require 'selenium/client'
require 'base64'
require 'uri'
require 'fileutils'

class Capture

def initialize
	@browser_instances = {}
end

def self.parse_filename(url)
	url.gsub('http://','').gsub('/','_').gsub('.','_').gsub("?","_").gsub("=","_")
end

def save_image(fn,content)
	f = open(fn,'wb')
	f.write(Base64.decode64(content))
	f.close
end

def self.usage
	puts "USAGE: pagelyzer_capture --url=URL [--output-folder=FOLDER] [--browser=BROWSER_CODE] [--no-screenshot] [--thumbnail] [--help] "
end

def self.help
	usage
	puts "This tool aims to have a HTML document with the visual cues integrated, called Decorated HTML. This allows to save the state of a browser at the moment of capture"
	puts "Browsers code are the same as defined in selenium. For instance:"
	puts " - firefox"
	puts " - chrome"
	puts " - iexploreproxy"
	puts " - safariproxy"
	puts " - opera"
	puts ""
	puts "Note: the browser should be installed in your machine to work with selenium webdriver"
 	puts ""
	puts "The output is sent to 'out' folder. If it doesn't exists it will be created"
end

def config(key)
	conf = File.open('etc/pagelyzer.conf','r')
	value = ""
	conf.each_line do |line|
		pair = line.split(":")
		if pair[0].strip == key
			value = pair[1].strip
			break
		end
	end
	return value
end

def verify_data_folder(path)
	state = true
	state = File.exists? path+"/js/decorate_mini.js"
	state
end

def remove_slash(path)
	path.sub(/(\/)+$/,'')
end

def open(browsers)
	browsers.uniq.each do |browser|
		begin
			@browser_instances[browser] = Selenium::WebDriver.for browser.to_sym
		rescue
			puts "Connection not possible with #{browser.to_sym}"
			puts "WARNING: Is #{browser} installed in your system?"
			puts "Try with the --browser=your_installed_browser"
			puts $!
			Capture.help
			next
		end
	end
end

def close
	puts "Closing browsers"
	@browser_instances.each do |browser,instance|
		begin
			instance.close
			instance.quit
			puts "Browser #{browser} closed"
		rescue
			puts "Browser #{browser} rest open"
		end
		@browser_instances[browser=nil]
	end
	@browser_instances = {}
end

def reset
	puts "Reseting browsers"
	browsers = @browser_instances.keys
	browsers.each {|b| system "killall #{b}"}
	close
	open(browsers)
end


def start(url,browser,output_folder,no_screenshot,thumb,current_folder,command,timeout)
	
	if URI.parse(url).scheme.nil?
		url = "http://#{url}"
	end
	
	if !(url =~ URI::regexp)
		puts "ERROR: invalid URL"
		return nil
	end

	puts "Timeout: #{timeout}secs"
	
	base64 = nil
	src = ""
	
	dump = File.open("#{current_folder}/data/js/decorate_mini.js").read

load_dump = <<FIN
function func_dump() {
	var script = document.createElement('script');
	script.type = "text/javascript"
	script.setAttribute("id","pagelyzerinject");
	script.appendChild(document.createTextNode("#{dump}"));
	document.getElementsByTagName('head')[0].appendChild(script);
}

var callback = arguments[arguments.length - 1];
callback(func_dump());
FIN

		driver = @browser_instances[browser]
		begin
			driver.manage.timeouts.implicit_wait = "60"
		rescue
			puts "WARNING: browser is not accepting time out"
		end
		
		begin
			Timeout.timeout(timeout) do 
				puts "Loading #{url} in #{browser}"
				driver.navigate.to url
				puts "Page loaded"
			end
		rescue
			puts "ERROR: Page load timeout #{$!}"
			reset
			raise "ERROR: Page load timeout #{$!}"
			return nil #unless command
		end
		src =""
		status="OK"
		driver.manage.window.resize_to(970,728)
		
		filename = Capture.parse_filename(url)
		
		if command
			File.open("#{output_folder}/#{browser}_#{filename}.html",'w') {|f| f.write driver.page_source}
		end
		
		unless no_screenshot
			puts "Getting screenshot"
			if command
				driver.save_screenshot("#{output_folder}/#{browser}_#{filename}.png")
			else
				base64 = driver.screenshot_as(:base64)
				#File.open("test",'wb') {|f| f.write base64}
			end
			if thumb
				begin
					system("cp \"#{output_folder}/#{browser}_#{filename}.png\" \"#{output_folder}/#{browser}_#{filename}_thumb.png\"")
					p1 = system("convert \"#{output_folder}/#{browser}_#{filename}_thumb.png\" -crop 1024x768+0+0 \"#{output_folder}/#{browser}_#{filename}_thumb.png\"")
					p2 = system("convert \"#{output_folder}/#{browser}_#{filename}_thumb.png\" -filter Lanczos 300x225 \"#{output_folder}/#{browser}_#{filename}_thumb.png\"")
					if p1 and p2
						puts "Thumbnail problem. Is ImageMagick 6+ installed in your system?"
					end
				rescue Exception=>e
					puts "ImageMagick error. Thumbnail not generated present?"
					puts e.backtrace
				end
			end
			#converting image from RGBA to RGB
			#marcalizer complains on RGBA format
			#disabled this functionality
			puts "thumbnail not functional yet" if thumb
			#system "convert \"#{output_folder}/#{browser}_#{filename}.png\" -flatten +matte \"#{output_folder}/#{browser}_#{filename}.png\""
		end
		
		begin
			puts "Getting rendered DOM"
			driver.execute_async_script(load_dump)
			#sleep 120000
			loaded = false
			k=0
			while !loaded and k<10
				begin
					r = driver.execute_script("return dump_loaded!=undefined;")
					loaded = (r==true);
					puts "Waiting page to finish loading..."
					sleep(0.5)
				 rescue
					 puts "Something maybe is wrong, but still waiting page to finish loading... (attempt #{k}/10)"
					 sleep(2)
				 end
				k+=1
			end
			src = driver.execute_script("return dump_start();")
			puts "done"
		rescue Exception=>e
			puts "#{browser} failed!"
			puts "The JavaScript could not be injected into page"
			raise "The JavaScript could not be injected into page"
			status="FAIL"
			#puts e.backtrace
			#driver.close
		end
		
		ret = [driver.page_source,src,base64]
		
		if command
			File.open("#{output_folder}/#{browser}_#{filename}.dhtml",'w') {|f| f.write src}
		end
		puts "capture done."
		return ret #unless command
	end
end

