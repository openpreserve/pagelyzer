class Item
	
	attr_accessor :nlevel, :nsection,:node,:level,:section
	attr_accessor :prob,:parent, :entropy, :children, :distance, :text_size
	
	def initialize(node,level,section,parent=nil,load_from_node=false)
		@node = node
		@level = level
		@nlevel = 0.0
		@nsection = 0.0
		@section=section
		@prob = 0.0
		@entropy = 0.0
		@parent=parent
		unless @parent.nil?
			@parent.children.push self
		end
		@children = []
		@distance = 0 #_to_parent
		if node.text?
			@text_size = node.content.split(" ").size
		else
			@text_size = 0
		end
		#get info from node if parameters are nil
		if load_from_node
			@nlevel = node["nlevel"].to_f
			@nsection = node["nlevel"].to_f
			@prob = node["prob"].to_f
			@entropy = node["entropy"].to_f
			@distance = node["distance"].to_f
		end
	end
	def to_vector
		[@node.name,@prob]
	end
	def tag
		@node.name
	end
	def uid
		require 'digest'
		"#{tag}_#{Digest::MD5.hexdigest("#{tag}_#{@section}_#{@level}")}"
	end
	def path
		if @parent.nil?
			return [self]
		else
			return ([@parent.path]+[self]).flatten
		end
	end
	def to_xml
		s = " "*(@level*2)
		
		attr = []
		attr.push attrfmt("section",@section)
		attr.push attrfmt("level",@level)
		attr.push attrfmt("nsection",@nsection)
		attr.push attrfmt("nlevel",@nlevel)
		attr.push attrfmt("prob",@prob)
		attr.push attrfmt("entropy",@entropy)
					
	
		if @node.text?
				attr.push attrfmt("size",@node.content.size)
				return "<#{tag} #{attr.join(" ")}/>\n"
		else
			unless @children == []
				s += "<#{tag} #{attr.join(" ")}>\n"
				@children.each do |child|
					s += child.to_xml unless child.nil?
				end
			else
				return "<#{tag} #{attr.join(" ")}/>\n"
			end
		end
		
		s += " "*(@level*2)
		s += "</#{tag}>\n"
		return s
	end
	
	def to_csv
		s = ""
		s += "#{@section}, #{@level}, #{tag}, #{@entropy}, #{@distance};\n"
		unless @children == []
			@children.each do |child|
				s += child.to_csv unless child.nil?
			end
		end	
		s
	end
	
	def to_list
		s = ""
		s += "#{section}: #{@node.path}\n"
		unless @children == []
			@children.each do |child|
				s += child.to_list unless child.nil?
			end
		end	
		s
	end
	
	def to_dot
		s = ""
		if @parent.nil?
			s+="digraph \"webpage\" {\n"
			s+="#{uid} [label=\"#{@section}\\n#{tag}\\nE:#{format(@entropy)}\\nP:#{format(@prob)}\\nS,L:[#{format(@nsection)},#{format(@nlevel)}]\"]\n"
		else
			s+="#{uid} [label=\"#{@section}\\n#{tag}\\nE:#{format(@entropy)}\\nP:#{format(@prob)}\\nS,L:[#{format(@nsection)},#{format(@nlevel)}]\"]\n"
			s+="#{@parent.uid} -> #{uid}\n [label=\"#{format(@distance)}\"]"
		end
		unless @children == []
			@children.each do |child|
				s+=child.to_dot unless child.nil?
			end
		end
		s+="}\n" if @parent.nil?
		return s
	end
	
	def to_grayscale
		s = " "*(@level*2)
		
		attr = []
		attr.push attrfmt("section",@section)
		attr.push attrfmt("level",@level)
		attr.push attrfmt("nsection",@nsection)
		attr.push attrfmt("nlevel",@nlevel)
		attr.push attrfmt("prob",@prob)
		attr.push attrfmt("entropy",@entropy)
		attr.push @node.attributes.map.collect {|a| attrfmt(a[0],a[1])}
		attr.push attrfmt("style","background-color:black")
		attr.flatten!
	
		if @node.text?
				attr.push attrfmt("size",@node.content.size)
				return "<#{tag} #{attr.join(" ")}/>\n"
		else
			unless @children == []
				s += "<#{tag} #{attr.join(" ")}>\n"
				@children.each do |child|
					s += child.to_grayscale unless child.nil?
				end
			else
				return "<#{tag} #{attr.join(" ")}/>\n"
			end
		end
		
		s += " "*(@level*2)
		s += "</#{tag}>\n"
		return s
	end
	def to_s
		"#{tag} #{node['ref']}"
	end

	private
	
	def attrfmt(key,value)
		"#{key}=\"#{value}\""
	end
	def format(double)
		"%.2f" % double
	end
end
