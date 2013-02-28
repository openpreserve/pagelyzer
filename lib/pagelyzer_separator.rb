#!/usr/bin/env ruby
#-*- mode: ruby; encoding: utf-8 -*-
# Andrés Sanoja
# UPMC - LIP6
#
# separator.rb
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
# class for detect gaps between blocks and determine the kind of separation 
# (horizontal and vertical)
#

class Separator
attr_accessor :sp,:ep, :bright_side,:dark_side, :weight
	def initialize(sp,ep)
		@sp = sp.to_f
		@ep = ep.to_f
		@bright_side = []
		@dark_side = []
		@weight=0
	end
	def polygon(block,mode)
		#get the polygon representation of the separator
		poly = []
		if mode == :horizontal
			poly.push [block.min_x,@sp]
			poly.push [block.max_x,@sp]
			poly.push [block.max_x,@ep]
			poly.push [block.min_x,@ep]
		else
			poly.push [@sp,block.min_y]
			poly.push [@sp,block.max_y]
			poly.push [@ep,block.max_y]
			poly.push [@ep,block.min_y]
		end
		poly
	end
	
	def points(block,mode)
		#get the list of points in the separator polygon
		polygon(block,mode).flatten
	end
	
	def to_s
		#string representation
		sa = []
		sb = []
		
		sa = @bright_side.collect{|x| x.id}.join(',') unless @bright_side.nil?
		sb = @dark_side.collect{|x| x.id}.join(',') unless @dark_side.nil?
		
		"{(#{cformat(@sp)},#{cformat(@ep)}) W:#{@weight} A:[#{sa}] B:[#{sb}]} "
	end
	
	def <=>(obj)
		#define comparison type
		if self.weight < obj.weight
			-1
		elsif self.weight > obj.weight
			1
		else
			0
		end
	end
	
	def contains?(block,mode)
		# permits to detect if a block is inside a separator
		if mode == :horizontal
			return ((@sp <= block.min_y) and (block.max_y <= @ep))
		else
			return ((@sp <= block.min_x) and (block.max_x <= @ep))
		end
	end
	
	def covered_by?(block,mode)
		# permits to detect if a separator is covered by a block
		if mode==:horizontal
			return (block.min_y <= @sp and @ep <= block.max_y)
		else
			return (block.min_x <= @sp and @ep <= block.max_x)
		end
	end
	
	def top_crossed_by?(block,mode)
		# permits to detect if a block is pass over a separator
		if mode==:horizontal
			return (block.min_y < @sp and block.max_y < @ep and block.max_y > @sp )
		else
			return (block.min_x < @sp and block.max_x < @ep and block.max_x > @sp ) 
		end
	end
	
	def bottom_crossed_by?(block,mode)
		# permits to detect if a block is pass below a separator
		if mode==:horizontal
			return (block.min_y > @sp and block.min_y < @ep and block.max_y < @ep )
		else
			return (block.min_x > @sp and block.min_x < @ep and block.max_x < @ep )
		end
	end
	
	def process_weight(mode)
		#* assign a weight to a separator
		#* greater distance greater weight
		#* different types, font, background of content in both sides of separator higher weight
		max_set = []
		@bright_side.each do |b|
			max_dist = 0
			@dark_side.each do |d|
				if mode==:horizontal
					dist = Point.new(0,b.max_y).distance_to? Point.new(0,d.min_y)
				else
					dist = Point.new(b.max_x,0).distance_to? Point.new(d.min_x,0)
				end
				if dist > max_dist
					max_dist = dist
				end
			end
			max_set.push max_dist
		end
		@weight = max_set.max unless max_set.empty?
		
		# backgrounds differents
		bg = @bright_side.collect {|x| x.candidates_background_color_list}.flatten.uniq & @dark_side.collect {|x| x.candidates_background_color_list}.flatten.uniq
		@weight += 10 if bg==[]
		
		#structures of blocks of two sides are are very similar (text, images, etc) decrease weight
		a=@bright_side.collect {|x| x.candidates_structure}.flatten.uniq.first
		b=@dark_side.collect {|x| x.candidates_structure}.flatten.uniq.first
		@weight -=10 if a=='TITLE' and b!='TITLE'
		@weight -=10 if a==b and a!='COMBINED'
		
		#fontsizes if different increase weight
		a=@bright_side.collect {|x| x.candidates_font_size_list}.flatten.max
		b=@dark_side.collect {|x| x.candidates_font_size_list}.flatten.min
		a=0 if a.nil?
		b=0 if b.nil?
		@weight +=10 if a < b
	end
end
