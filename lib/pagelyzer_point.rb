#!/usr/bin/env ruby
#-*- mode: ruby; encoding: utf-8 -*-
# Andrés Sanoja
# UPMC - LIP6
#
# point.rb
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
# Class for (x,y) point representation
# 
#
class Point

attr_accessor :x,:y

def initialize(x=0,y=0)
	@x = x.to_f
	@y = y.to_f
end
def to_s
	"(#{"%6.2f" % @x},#{"%6.2f" % @y})"
end
def distance_to?(point)
	#euclidian distance between two points
	Math.sqrt( (point.x - @x) ** 2 + (point.y - @y) ** 2 )
end

def into?(poly,maxy)
	#define if a point is into a polygon using ray tracing technique
	crossings = 0
	desp = 0
	polaux = poly.collect{|p| [p.x,p.y]}
	while (@y+desp)<maxy
		if polaux.include? [@x,@y+desp]
			crossings+=1
		end
		desp+=1
	end
	puts "#{crossings}" unless crossings==0
	crossings % 2 != 0
end
end
