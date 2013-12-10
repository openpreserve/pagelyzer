#!/usr/bin/ruby1.9.1
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

require 'open3'
require 'rubygems'
require 'fileutils'
#require 'hpricot'
require 'nokogiri'
require 'uri'
require 'yaml'
require 'open-uri'
require 'sanitize'
require_relative '../lib/pagelyzer_url_utils.rb'
require_relative '../lib/pagelyzer_dimension.rb'
require_relative '../lib/pagelyzer_block.rb'
require_relative '../lib/pagelyzer_util.rb'
require_relative '../lib/pagelyzer_point.rb'
require_relative '../lib/pagelyzer_separator.rb'
#~ require_relative '../lib/pagelyzer_convex_hull.rb'
require_relative '../lib/pagelyzer_heuristic.rb'


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
		@pdoc = 6
		@doc_proportion = 1
		@target_path = "./"
		@type = :file
		@color = ['#1E90FF','#90EE90','#0000FF','#FF0000','#FFA500','#8B6914','#EE1CA2','#77AF4F','#A020F0','#82B5D8','','','']
		@root = nil
		@type = :file
		@error = false
		@doc = nil
	end
	
	def process_heuristics1(node)
	ret=nil
		@heuristics.each do |h|
			if h.parse(node)
				ret= h
				break 
			end
		end
	return ret
	end
	def process_heuristics2(node)
		ind=@heuristics.collect{|a| a.name}.index(node['rule']) 
		@heuristics[ind].action = Action.new('extract',@heuristics[ind].weight)
		return @heuristics[ind]
	end
	
	def process_node(node)
		rule_used = nil
		if node['candidate'].nil?
			unless malformed?(node) or text?(node) or !valid?(node)
				rule_used = process_heuristics1(node)
			end
			
		else
			rule_used = process_heuristics2(node)
		end
		
		rule_used 
	end

	def process_current_node(node,heuristic)
		current_block = Block.new 
		current_block.id = @block_count
		@block_count += 1
		current_block.add_candidate [node],heuristic
		current_block.process_path
		return current_block
	end
	
	def process_current_block(node,current_block)
		sub_block_list = []
		unless node.children.nil? 
			node.children.each do |e|
				sub_block_list.push detect_blocks(e)
			end
			sub_block_list.flatten!
			sub_block_list.delete(nil)
			sub_block_list.each {|b| current_block.add_child b}
		end
	end
	
	def divide_current_block(node)
		sub_block_list = []
		unless node.children.nil? 
				node.children.each do |e|
					sub_block_list.push detect_blocks(e)
				end
				sub_block_list.flatten!
				sub_block_list.delete(nil)
		end
		return sub_block_list
	end
	
	def detect_blocks(node)
		current_block = nil
		current_block = []
		if node.elem? and !undesirable_node?(node) and visible?(node)
			heuristic = process_node(node)
			if !heuristic.nil? and heuristic.action.rec == 'extract'
				current_block = process_current_node(node,heuristic)
				#TODO: verify if there are nodes that do not corresponds to a sub-block IMPLICIT BLOCKS
				if current_block.doc < @pdoc	
					process_current_block(node,current_block)
				end
			elsif !heuristic.nil? and heuristic.action.rec == 'divide'
				#divide
				current_block = divide_current_block(node)
			end
		end
	current_block
	end

	def detect_separators(block)
		unless block.children.nil?
			unless block.children.size==0
				block.children.each_with_index do |child,i|
					block.children[i] = detect_separators(child)
				end
			end
		end
		block = find_separators(block,:horizontal)
		block = find_separators(block,:vertical)
	block
	end


	def to_xml
	src = ""
	src += "<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\" ?>\n"
	src += "<XML>\n"
		src += "<Document url=\"#{escape_html(@document.url.gsub('"',''))}\" Title=\"#{escape_html(@document.title)}\" Version=\"#{@version}\" Pos=\"WindowWidth||PageRectLeft:#{@document.width} WindowHeight||PageRectTop:#{@document.height} ObjectRectWith:0 ObjectRectHeight:0\">\n"
			src += @root.to_xml
		src += "</Document>\n"
	src += "</XML>\n"
	src 
	end

	def help
		usage
		puts "This tool is oriented to separate web pages into segments called blocks, based on the structural and visual properties"
	end

	def self.usage
		puts "USAGE: pagelyzer_analyzer --decorated-file=FILE [--output-file=FILE] [--pdoc=(0..10)] [--version] [--help]"
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
	@pdoc = n
end

def initialize_heuristics
	@heuristics = []
	@heuristics.push Body.new(self,4)	#only for body
	@heuristics.push Invalids.new(self,3)	#skip
	@heuristics.push OneChild.new(self,3)	#divide
	@heuristics.push LayoutContainer.new(self,4)	#might extract
	@heuristics.push IndirectContainer.new(self,6)	#might extract
	@heuristics.push Container.new(self,4)	#might extract
	@heuristics.push ContentContainer.new(self,9)	#might extract
	@heuristics.push Content.new(self,9)	#might extract
	@heuristics.push Image.new(self,9)	#extract
	@heuristics.push DefaultDivide.new(self,8) #might extract
	@heuristics.push DefaultExtract.new(self,10) #might extract
end

def initialize_granularity
	doc = normalize_DOM(self,doc) 
	@doc_proportion = @document_area / 10.0
	@doc_rel = 10-@pdoc+1
end

def load_document
	begin
		@doc = load(self,@source_file,:content)
	rescue
		puts "ERROR: There was a problem loading the page"
		@error=true
		return self
	end
end

def get_first_node
	#get first node to start validation
	firstnode = nil
	head = @doc.at('head')
	if head.nil?
		body = @doc.at('body')
		firstnode = body unless body.nil?
	else
		firstnode = head.next_sibling
	end
	firstnode = @doc.root if firstnode.nil?
	return firstnode
end

def get_real_root
	if @root.nil?
		@error = true
	else
		if @root.is_a? Array
			if @root == []
				@error = true
			elsif @root.size==1
				@root=@root.first
			else
				sub = @root
				@root = Block.new
				@root.add_candidate doc.root
				@root.children = sub
			end
		end
	end
end

def start
	
	load_document

	initialize_heuristics

	@root = detect_blocks(get_first_node)
	
	get_real_root
	
	@root.process_path
	
	return self
	
	end
end


