#! /usr/bin/ruby1.9.1
#-*- mode: ruby; encoding: utf-8 -*-
# Andrés Sanoja
# UPMC - LIP6
#
#
#
# pagelyzer_analyzer
#
# Requires: Ruby 1.9.1+ (1.8.x versions won't work), rubygems 1.3.7+ and Hpricot gem v=0.8.6
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
# ISSUES:
# 1. For commandline parameters is better to escape them, e.g:
#
# pagelyzer_analyzer --decorated-file=/my/path with/spaces -- only processes /my/path !
# pagelyzer_analyzer --decorated-file=/my/path\ with/spaces -- results in correct behaviour
#

Encoding.default_external = Encoding::UTF_8

require 'nokogiri'
require_relative '../lib/pagelyzer_util.rb'
require_relative '../lib/pagelyzer_dimension.rb'
require_relative '../lib/pagelyzer_url_utils.rb'
require "sanitize"


class BlockOMatic

	attr_accessor :type,:document,:window,:source_file,:document_area, :error
	attr_accessor :doc_rel,:doc_proportion, :pdoc,:target_path,:next_block_id,:debug
	
	def initialize
		@window = Dimension.new
		@document = Dimension.new
		@screenshot = Dimension.new
		@max_weight = 0
		@heuristics = []
		@next_block_id = 10000
		@gid = 1
		@block_count = 0
		@job_id = 0
		@browser_id = 0
		@source_file = nil
		@output_file = nil
		@document_area = 0.0
		@debug = false
		@granularity = 0.4
		@doc_proportion = 1
		@target_path = "./"
		@type = :file
		@blocks = []
		@type = :file
		@error = false
		@segmented_page = nil
		@verbose = true
	end

def set_source_content(dhtml) 
	@source_file = dhtml
	@type = :content
end
def set_source_file(f) 
	@source_file = f
	@type=:file
end
def set_output_file(f) 
	@output_file = f
	@type=:file
end
def set_granularity(n) 
	@granularity = n/10
end

def mark(node,d)
	elem = d.at(node.path)
	unless elem.nil?
		d.xpath(node.path).first['style'] = "border: solid 2px red"
		d.xpath(node.path).first['title'] = "#{node.path} - #{node["id"]}"
	end
end

def area(node)
cx = node["elem_width"].to_f / 2
cy = node["elem_height"].to_f / 2

r = Math.sqrt(cx**2 + cy**2)

angle = Math.asin(cy/r)

return (4*(r**2)*Math.sin(2*Math::PI/4))/2


end

def relative_area(node1,node2)
	a= area(node1)/area(node2)
	a = 1 if a>1
	return a
end

def evalnode(elem,gratio,body,tr,ta,kl,kr)
	newblock = true

	rdocarea = relative_area(elem,body)
	parent_area = relative_area(elem,elem.parent)
	elem_area = area(elem)
	

	
	lclose = false
	
	if ((elem.parent['elem_left'].to_f - elem['elem_left'].to_f).abs < kl) and 
	   ((elem.parent['elem_width'].to_f - elem['elem_width'].to_f).abs < kr) 
		lclose = true
	end
	
	lelem = elem.path.split("/").size
	telem = elem.inner_text.squeeze(" ").gsub("\n","").strip.size
		
	puts "Element: '#{elem['id']}' at #{elem.path} (#{elem["elem_left"]} #{elem["elem_top"]} #{elem["elem_width"]} #{elem["elem_height"]}), E.area: #{elem_area}, P.area: #{area(elem.parent)}, Rel.area: #{parent_area}, RelDoc.area: #{rdocarea}" if @verbose
		
	if ((parent_area>=tr or elem_area>=ta) and lclose) 
		if lclose
			puts "      NB1: it is at parent limits" if @verbose
		else
			puts "      NB1: it is relevant into parent area or whole length element" if @verbose
		end
	else
		puts "      NB1: not relevant into parent area or whole length element" if @verbose
		newblock=false
	end
	if (rdocarea>0.1 or lclose) 
		if lclose
			puts "      NB2: it is at parent limits" if @verbose
		else
			puts "      NB2: it is a significat block respect page: #{rdocarea}" if @verbose
		end
	else
		puts "      NB2: not significat block respect page" if @verbose
		newblock=false
	end
	if lelem <= gratio 
		puts "      NB3: it is inside the granularity" if @verbose
	else
		puts "      NB3: not into the granularity for" if @verbose
		newblock=false
	end
	if telem>0 
		puts "      NB4: it has siginificant text size" if @verbose
	else
		puts "      NB4: has not siginificant text size" if @verbose
		newblock=false
	end
	if visible?(elem)
		puts "      NB5: it is visible" if @verbose
	else
		puts "      NB5: not visible" if @verbose
		newblock=false
	end
	newblock
end

def start
	begin
		doc = load(self,@source_file,:content)
	rescue
		puts "ERROR: There was a problem loading the page #{$!}"
		@error=true
		return self
	end
	#doc = normalize_DOM(self,doc)
	lpath = 0
	
	doc.search("*").each do |elem|
		lpath=elem.path.split("/").size if elem.path.split("/").size > lpath
	end

	gratio = 2 + lpath * @granularity

	puts "using #{gratio} gr" if @verbose

	seg = Nokogiri::HTML(File.open(@source_file.gsub(".dhtml",".html")))

	blocks = []
	
	tr = @granularity
	ta = 133000
	kl = 1
	kr = 1

	body = doc.at('body')
	#doc_area = area(body)
	doc.search("//*[count(child::*) <= 1]").each do |elem|
		if ["head","meta","link","script"].include? elem.name
			puts "SKIPPED TAG #{elem['id']} #{elem.path} (#{elem["elem_left"]} #{elem["elem_top"]} #{elem["elem_width"]} #{elem["elem_height"]})" if @verbose
			next 
		end
		
		puts "="*80 if @verbose
		
		cur = elem
		good_partition = false
		good_element = nil
		good_weight = 0
		candidates = []
		rooted = false
		
		
		while !rooted and !good_partition
			siblings = cur.xpath('../*')
			if siblings.size == 1
				if evalnode(cur,gratio,body,tr,ta,kl,kr)
					good_partition = true
					good_element = cur
					good_weight = 1
				else
					cur = cur.parent
				end
			else
				if evalnode(cur,gratio,body,tr,ta,kl,kr)
					#puts "NEW BLOCK #{elem['id']} #{elem.path} (#{elem["elem_left"]} #{elem["elem_top"]} #{elem["elem_width"]} #{elem["elem_height"]})" if @verbose
					
					invalids = 0
					signodes = 0
					weight = 0
					siblings.each do |si|
						if evalnode(si,gratio,body,tr,ta,kl,kr)
							signodes+=1 
						end
						unless visible?(si)
							invalids+=1
						end
						if (siblings.size-invalids)==0
							weight = 0
						else
							weight = signodes.to_f / (siblings.size-invalids)
						end
						
						if weight>=0.9
							puts "NEW BLOCK #{cur.path}"
							good_partition=true
							good_element=cur
							good_weight=weight
						else
							candidates.push [cur,weight]
							puts candidates.collect {|x| "#{x[0].path} #{x[1]}"}
							cur = cur.parent
						end
					end
				else
					if ['html'].include?(cur.parent.name)
						rooted = true
					end
					cur = cur.parent
				end
			end		
		end
		
		next if candidates==[]
		
		if !good_partition or !rooted
			good_weight = 0
			candidates.each do |c|
				if c[1]>good_weight
					good_element = c[0]
					good_weight  = c[1]
				end
			end

			good_element.xpath('../*').each do |si|
				if visible?(si) 
					@blocks.push [si,good_weight] 
				end
			end
		end
	end

	puts "====  BLOCKS ====" if @verbose
	@blocks = @blocks.uniq
	i=1
	@blocks.each do |b,w|
		puts "Block#{i} #{b.path} #{b["id"]} #{b["class"]} (#{b["elem_left"]} #{b["elem_top"]} #{b["elem_width"]} #{b["elem_height"]}) W:#{w}" if @verbose
		mark(b,seg)
		i+=1
	end

	@segmented_page = seg
	return self
end

def get_preq(node)
	links = []
	text = []
	images = []
	node.xpath("text()").each do |tt|
		text.push Sanitize.clean(tt.inner_text)
	end
	node.xpath("img").each do |img|
		images.push img
	end
	node.xpath("a").each do |link|
		links.push link
	end
	
		#~ if ['a','img'].include? tag.name.downcase
			#~ if tag.name.downcase == 'a'
				#~ links.push tag 
			#~ end
			#~ if tag.name.downcase == 'img'
				#~ images.push tag 
			#~ end
			#~ text.push Sanitize.clean(tag.inner_text)
		#~ else
			#~ unless undesirable_node?(tag)
				#~ text.push Sanitize.clean(tag.inner_text)
			#~ end
		#~ end
	#end
	[links.uniq,images.uniq,text.uniq]
end

	def parse_xml(blockpack,sid)
		src = ""
		i=1
		block = blockpack[0]
		weight = blockpack[1]
		l = block["elem_left"].to_i
		t = block["elem_top"].to_i
		w = block["elem_width"].to_i - l
		h = block["elem_height"].to_i - t
		
		src+= "<Block Ref=\"Block#{sid}\" internal_id='#{@id}' ID=\"$BLOCK_ID$\" Pos=\"WindowWidth||PageRectLeft:#{l} WindowHeight||PageRectTop:#{t} ObjectRectWidth:#{w} ObjectRectHeight:#{h}\" Doc=\"#{@granularity}\">\n"
			src += "<weight>\n"
			src += "#{weight}\n"
			src += "</weight>\n"
			src += "<Paths>\n"
			#src += @candidates.collect {|c| "<path>#{c.path},#{c["elem_left"]},#{c["elem_top"]},#{c["elem_width"]},#{c["elem_height"]},#{c["id"]},#{c["uid"]}</path>\n"}.join("")
			
			src += "<path>#{block.path},#{l},#{t},#{w},#{h},#{block["id"]},#{block["uid"]}</path>\n"
			src += "</Paths>\n"
			
				src += "<Links ID=\"$LINKS_ID$\" IDList=\"$ID_LIST_LINKS$\">\n"
				lid = []
				sl = ""
				@links,@images,@text = get_preq(block)
				
				@links.each do |link|
					unless malformed?(link)
						iid = crypt(escape_html(link.inner_text.strip) + escape_html(link[:href]))
						lid.push iid
						sl += "<link ID=\"#{iid}\" Name=\"#{escape_html(link.inner_text.strip)}\" Adr=\"#{escape_html(link[:href])}\"/>"
					end
				end
				src.gsub!('$ID_LIST_LINKS$',lid.join(','))
				src.gsub!('$LINKS_ID$',crypt(sl))
				src += sl
				src += "</Links>\n"
				
				src += "<Imgs ID=\"$IMGS_ID$\" IDList=\"$ID_LIST_IMAGES$\">\n"
				lim = []
				si = ""
				@images.each do |image|
					unless malformed?(image)
						iid = crypt(escape_html(image['alt'])+escape_html(image['src']))
						lim.push iid
						si += "<img ID=\"#{iid}\" Name=\"#{escape_html(image[:alt])}\" Src=\"#{escape_html(image[:src])}\"/>"
					end
				end
				src.gsub!('$ID_LIST_IMAGES$',lim.join(','))
				src.gsub!('$IMGS_ID$',crypt(si))
				src += si
				src += "</Imgs>\n"
				
				@text.delete(nil)
				@text.delete('')
				@text.collect! {|t| 
					t.gsub(/(?<!\n)\n(?!\n)/,' ').gsub(/^$\n/,'').gsub(/\s+/,' ').strip
				}
				txt = escape_html(@text.join(","))
				src += "<Txts ID=\"#{crypt(txt)}\" Txt=\"#{txt}\"/>\n"
			#~ unless @children.empty?
				#~ @children.each do |child|
					#~ src += child.to_xml
				#~ end
			#~ end
		src += "</Block>\n"
		src.gsub!('$BLOCK_ID$',crypt(src))
		src
	end

	def to_xml
	src = ""
	src += "<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\" ?>\n"
	src += "<XML>\n"
		src += "<Document url=\"#{escape_html(@document.url.gsub('"',''))}\" Title=\"#{escape_html(@document.title)}\" Version=\"#{@version}\" Pos=\"WindowWidth||PageRectLeft:#{@document.width} WindowHeight||PageRectTop:#{@document.height} ObjectRectWith:0 ObjectRectHeight:0\">\n"
			i = 1
			@blocks.each do |b|
				src += parse_xml(b,i)
				i+=1
			end
		src += "</Document>\n"
	src += "</XML>\n"
	src 
	end

end
