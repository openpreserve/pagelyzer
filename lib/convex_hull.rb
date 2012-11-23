#!/usr/bin/env ruby
#-*- mode: ruby; encoding: utf-8 -*-
# Andrés Sanoja
# UPMC - LIP6
#
# convexhull.rb
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
# class for determine the convexhull of the union of two polygon
# 

class ConvexHull

	def initialize(points)
		@points = points
	end

	def calculate
		lop = @points.sort_by { |p| p.x }
		left = lop.shift
		right = lop.pop
		lower, upper = [left], [left]
		lower_hull, upper_hull = [], []
		det_func = determinant_function(left, right)
		until lop.empty?
			p = lop.shift
			( det_func.call(p) < 0 ? lower : upper ) << p
		end
		lower << right
		until lower.empty?
			lower_hull << lower.shift
			while (lower_hull.size >= 3) &&
				!convex?(lower_hull.last(3), true)
				last = lower_hull.pop
				lower_hull.pop
				lower_hull << last
			end
	   end
		upper << right
		until upper.empty?
			upper_hull << upper.shift
			while (upper_hull.size >= 3) &&
				!convex?(upper_hull.last(3), false)
				last = upper_hull.pop
				upper_hull.pop
				upper_hull << last
			end
		end
		upper_hull.shift
		upper_hull.pop
		lower_hull + upper_hull.reverse
	end
	 
	private

	def determinant_function(p0, p1)
		proc { |p| ((p0.x-p1.x)*(p.y-p1.y))-((p.x-p1.x)*(p0.y-p1.y)) }
	end

	def convex?(list_of_three, lower)
		p0, p1, p2 = list_of_three
		(determinant_function(p0, p2).call(p1) > 0) ^ lower
	end
	 
end
