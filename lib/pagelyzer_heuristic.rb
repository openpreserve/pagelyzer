#!/usr/bin/env ruby
#-*- mode: ruby; encoding: utf-8 -*-
# Andrés Sanoja
# UPMC - LIP6
#
# heuristic.rb
#
# Requires: Ruby 1.9.1+ (1.8.x versions won't work) and rubygems 1.3.7+
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
# classes for determine if a HTML node should be considered as a coherent segment of 
# a web page (block)
# This file includes the following classes:
# - Heuristic: intended as a abstract class for heuristics
# - Action: action to take if a heuristic is met
# - Body: Body tag detection and actions
# - Invalids: Detect invalid nodes (no visible in rendering, divs with no children, etc)
# - OneChild: Detect tags that serve only for organizing but not relevant to page layout
# - LayoutContainer: Detect big nodes that helps to describe the page layout
# - IndirectContainer: Node which one of his children is far bigger than the rest, for preserving block partitioning
# - Container: Common nodes for organizing content (divs, table, etc)
# - ContentContainer: Tags that holds content directly (p, h1, td, etc)
# - Content: Content itself with inline tags (a, b, span, em, etc)
# - Image: detect a tag image
# - DefaultDivide: If any of the above rules are met evaluate if the node should be divided based on the relative area it covers
# - DefaultExtract: Same as dividing, but for extract a node as block

class Action
	attr_accessor :rec,:doc

	def initialize(r='divide',d=nil)
		@rec = r
		@doc = d
	end
end

class Heuristic

	attr_accessor :weight,:action

	def initialize(bom,weight,internal_debug=true)
		@weight = weight
		@action = Action.new
		@internal_debug=internal_debug
		@bom = bom
	end

	def debug(node)
	end

	def parse(node)
	end

	def name
		self.class.to_s
	end
end

class Body < Heuristic

	def parse(node)
		used = false
		debug(node)
		if node.name.downcase == 'body'
			used = true
			@action = Action.new('extract',@weight)
		end
		used
	end
end

class Invalids < Heuristic
	def parse(node)
		debug(node)
		used = false
		if has_children?(node)
			vt = 0
			node.children.each do |c|
				vt+=1 if valid?(c)
			end
			if (!text?(node) and vt == 0) or (line_break?(node) and node.children.size==0)
				used = true
				@action = Action.new('skip')
			end
		end
		used
	end
end

class OneChild < Heuristic
	def parse(node)
		debug(node)
		used = false
		if has_children?(node)
			varr = []
			node.children.each do |child|
				if valid?(child) and !text?(child) and line_break?(child)
					varr.push child
				end
			end
			if varr.size == 1
				ar = area(varr.first) / area(node).to_f
				if ar > 0.9
					used = true
					@action = Action.new('divide')
				elsif ar < 0.1
					used = true
					@action = Action.new('extract',8)
				end
			end
		end
		used
	end
end

class LayoutContainer < Heuristic
	def parse(node)
		debug(node)
		used = false
		unless text?(node) or !has_children?(node) or !line_break?(node)
			cont = 0
			valid = 0
			sub_tree = 0
			excluded = 0
			node.children.each do |c| 
				cont+=1 if container?(c)
				valid += 1 if valid?(c) and !anchor?(c)
			end
			rd = cont.to_f/(valid).to_f
			if rd > 0.6
				if area(node) >= (@bom.doc_rel * @bom.doc_proportion)
					@action = Action.new('extract',4)
					used = true
				end
			end
		end
		used
	end
end

class IndirectContainer < Heuristic
	def parse(node)
		debug(node)
		used=false
		if has_children?(node) and line_break?(node) and !text?(node)
			max_child = nil
			amax = 0
			node.children.each do |child|
				a = area(child)/area(node)
				if amax < a
					max_child = child
					amax = a
				end
			end
			if amax > 0.95 and (container?(max_child) or line_break?(max_child))
				@action = Action.new('extract',8)
				used = true
			end
		end
		used
	end
end

class Container < Heuristic
	def parse(node)
		debug(node)
		used = false
		unless text?(node) or !has_children?(node) or !line_break?(node)
			cont = 0
			invalids = 0
			sub_tree = 0
			excluded = 0
			hcont=0
			avg = 0
			node.children.each do |c| 
				cont+=1 if container?(c)
				invalids += 1 if !valid?(c)
				sub_tree += 1 if has_children?(c) and valid?(c) and container?(c)
				excluded += 1 if anchor?(c)
				avg += area(c)
			end
			avg = avg.to_f 
			rd = cont.to_f/(node.children.size-excluded-invalids).to_f
			rs = sub_tree.to_f/(node.children.size-excluded-invalids).to_f
			ca = avg / area(node)
			if (rd > 0.6 and rs > 0.6) or node.name.downcase == 'table'
				if area(node) < (@bom.doc_rel * @bom.doc_proportion)
					if relative_area(@bom,node)*100 > 0
						if ca<0.55
							@action = Action.new('extract',9)
						else
							@action = Action.new('extract',tag_based_weight(node))
						end
						used = true
					end
				end
			end
		end
		used
	end
end

class ContentContainer < Heuristic
	def parse(node)
		debug(node)
		used = false
		if has_children?(node) and line_break?(node)
			c = 0
			div = 0
			inv = 0
			node.children.each do |child|
				c += 1 if content_container?(child) or virtual_text?(child)
				div += 1 if child.name.downcase == 'div'
				inv += 1 if !valid?(child)
			end
			rel = div.to_f/(node.children.size.to_f-inv) unless node.children.size.to_f-inv==0
			if c==(node.children.size-inv-div) and rel<=0.5 
				used = true
				if contain_p?(node) or contain_image?(node) 
					if relative_area(@bom,node) > 0.45 
						if @bom.pdoc > 6
							@action = Action.new('extract',tag_based_weight(node))
						else
							@action = Action.new('extract',8)
						end
					else
						@action = Action.new('extract',8)
					end
				else
					@action = Action.new('extract',tag_based_weight(node))
				end
				
			end
		end
	end
end

class Content < Heuristic
	def parse(node)
		debug(node)
		used=false
		if content_container?(node)
			if virtual_text?(node)
				used = true
				@action = Action.new('extract',tag_based_weight(node))
			end
		end
		used
	end
end

class Image < Heuristic
	def parse(node)
		debug(node)
		used=false
		if is_image?(node)
			used = true
			@action = Action.new('extract',@weight)
		end
		used
	end
end

class DefaultDivide < Heuristic
	def parse(node)
	debug(node)
	used=false
		if line_break?(node) and node.name.downcase != 'body' and relative_area(@bom,node) >= 0.10 and has_children?(node)
			@action = Action.new('divide',@weight)
			used = true
		end
	used
	end
end

class DefaultExtract < Heuristic
	def parse(node)
	debug(node)
	used=false
		if line_break?(node) and relative_area(@bom,node) > 0.1 and valid?(node)
			@action = Action.new('extract',tag_based_weight(node)) 
			used = true
		end
	used
	end
end
