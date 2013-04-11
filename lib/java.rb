# =======================================
# jrequire - using java libraries in ruby
# =======================================
#
# Project Home: http://github.com/sklemm/jrequire
#
# Original Work
# Copyright (C) 2006 Richard L. Apodaca
# Modifications
# Copyright (C) 2009 Sebastian Klemm
#
# This file is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License version 2.1 as published by the Free Software
# Foundation.
#
# This file is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this file; if not, write to the Free
# Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor
# Boston, MA 02111-1301, USA.

require 'rubygems'
gem 'rjb'
require 'rjb'

# Loads the JVM with the given <tt>classpath</tt> and arguments to the jre.
# All needed .jars should be included in <tt>classpath</tt>.
def load_jvm(classpath, jargs)
  Rjb::load(classpath, jargs)
end

module Kernel

  # Maps the packages and class name specified by <tt>qualified_class_name</tt>
  # to a nested set of Ruby modules. The first letter of each module name is
  # capitalized. For example, <tt>java.util.HashMap</tt> would become <tt>
  # Java::Util::HashMap</tt>.
  def jrequire(qualified_class_name)
    java_class = Rjb::import(qualified_class_name)
    package_names = qualified_class_name.to_s.split('.')
    java_class_name = package_names.delete(package_names.last)
    new_module = self.class

    package_names.each do |package_name|
      module_name = package_name.capitalize

      if !new_module.const_defined?(module_name)
        new_module = new_module.const_set(module_name, Module.new)
      else
        new_module = new_module.const_get(module_name)
      end
    end

    return false if new_module.const_defined?(java_class_name)

    new_module.const_set(java_class_name, java_class)

    return true
  end
  
end

