#!/usr/bin/env ruby
#-*- mode: ruby; encoding: utf-8 -*-
# Andrés Sanoja
# UPMC - LIP6
#
# url_utils.rb
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
# methods for relative -> absolute URL processing
# 
#

  def relative?(url)
    if url.match(/^http/) or url.match(/^file/)
      return false
    end
    return true
  end

  def make_absolute(potential_base, relative_url)
    absolute_url = nil;
    if relative_url.match(/^\//)
      absolute_url = create_absolute_url_from_base(potential_base, relative_url)
    else
      absolute_url = create_absolute_url_from_context(potential_base, relative_url)
    end
    return absolute_url
  end

  def urls_on_same_domain?(url1, url2)
    return get_domain(url1) == get_domain(url2)
  end

  def get_domain(url)
    return remove_extra_paths(url)
  end

  def create_absolute_url_from_base(potential_base, relative_url)
    naked_base = remove_extra_paths(potential_base)
    return naked_base + relative_url
  end

  def remove_extra_paths(potential_base)
    index_to_start_slash_search = potential_base.index('://')+3
    index_of_first_relevant_slash = potential_base.index('/', index_to_start_slash_search)
    if index_of_first_relevant_slash != nil
      return potential_base[0, index_of_first_relevant_slash]
    end
    return potential_base
  end

  def create_absolute_url_from_context(potential_base, relative_url)
    absolute_url = nil;
    if potential_base.match(/\/$/)
      absolute_url = potential_base+relative_url
    else
      last_index_of_slash = potential_base.rindex('/')
      if potential_base[last_index_of_slash-2, 2] == ':/'
        absolute_url = potential_base+'/'+relative_url
      else
        last_index_of_dot = potential_base.rindex('.')
        if last_index_of_dot < last_index_of_slash
          absolute_url = potential_base+'/'+relative_url
        else
          absolute_url = potential_base[0, last_index_of_slash+1] + relative_url
        end
      end
    end
    return absolute_url
  end
