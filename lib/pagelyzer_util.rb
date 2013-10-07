require 'cgi'
require 'digest/md5'

class String
	def numeric?
		Float(self) != nil rescue false
	end
end

def load(bom,source_file,type=:file)
	if bom.type==:file
		if !File.exists? "#{bom.source_file}"
			puts "File '#{bom.source_file}' not found"
			exit
		else
			doc = Nokogiri::HTML(File.open(bom.source_file))
		end
	else
		doc = Nokogiri::HTML(bom.source_file)
	end
	
	unless doc.at("//comment()[1]").content.nil?
		metadata = doc.at("//comment()[1]").content.gsub("}","").gsub("{","").split(",").collect {|x| x.split(":")}
		bom.window.width = metadata[0][2].to_i
		bom.window.height = metadata[1][1].to_i
		bom.document.width = metadata[2][2].to_i
		bom.document.height = metadata[3][1].to_i
		metadata[4] = metadata[4].insert(3,":")
		bom.document.url = metadata[4][2..(metadata[4].size-1)].join.strip
		bom.document.date = metadata[5][1..(metadata[5].size-1)].join.strip
	else
		bom.window.width = 0
		bom.window.height = 0
		bom.document.width = 0
		bom.document.height = 0
		bom.document.url = ""
		bom.document.date = ""
		puts "warning: empty metadata in input file"
	end
	bom.document.title  = doc.at('//title').inner_text.strip unless doc.at('//title').nil?
	
	bom.document.width = doc.at("html")["elem_width"].to_i unless doc.at("html").nil?
	bom.document.height = doc.at("html")["elem_height"].to_i  unless doc.at("html").nil?
	bom.document_area = bom.document.width.to_f * bom.document.height.to_f
		
	#cleaning up whitespaces and useless tags
	doc.search("//text()").each do |t|
		begin
			if t.inner_text.strip == ""
				t.parent.children.delete(t)
			end
		rescue
			
		end
	end

	(doc/"script").remove
	(doc/"style").remove
	(doc/"meta").remove
	(doc/"iframe").remove
	(doc/"noscript").remove

	doc

end

def normalize(str)
	uri = URI.parse(str)
    uri = URI.parse("http://" + str) if !uri.scheme

    if(uri.scheme != 'http' && uri.scheme != 'https')
        # more complaining about bad input
    else
		return uri
    end
end

def rgb2hex(color)
	if color == "rgba(0, 0, 0, 0)" or color == "transparent"
		return "#FFFFFF"
	else
		color =~ /^rgb(a)?\(([0-9]+),\s*([0-9]+),\s*([0-9]+)(,\s*([0-9]+(\.[0-9]+)))?\)$/
		r,g,b = $2,$3,$4,$6
		if r.to_i == 0 and g.to_i == 0 and b.to_i == 0
			r = 255
			g = 255
			b = 255
		end
		return '#' + [r, g, b].map { |i| i.to_i.to_s(16).rjust(2, '0').upcase}.join
	end
end

def text?(node)
	#node.is_a? Hpricot::Text or node.is_a? String
	node.text?
end

def malformed?(node)
	#node.is_a? Hpricot::BogusETag
	false
end

def invalid?(node)
	ret = false
	unless malformed? node or text?(node)
		ret = node.comment? or node.doctype? or (node.attributes['width'].to_i == 0) or (node.attributes['height'].to_i == 0)
	end
	ret
end

def partial_valid?(node)
	['FORM','HEAD','TABLE','DD','DT','UL','LI','DL','BLOCKQUOTE','DIV','BODY','P'].include? node.name.upcase
end

def valid?(node)
	if text?(node)
		return true
	elsif malformed?(node)
		return false
	elsif undesirable_node?(node)
		return false
	elsif node.children.nil?
		return visible?(node)
	else
		vis = 0
		if visible?(node)
			vis=1
			node.children.each do |c|
				vis+=1 if visible?(c)
			end
		end
		return vis>0
	end
end

def visible?(node)
	ret = false
	unless malformed?(node)
		if !text?(node) and !node.is_a? Nokogiri::XML::DTD
			if node['display']=="none" or (node['visibility']!="visible" and !node['visibility'].nil?)
				ret = false
			else
				if  node['elem_width'].nil? or node['elem_height'].nil?
					ret = false
				else
					ret = (node['elem_width'].to_i > 0) or (node['elem_height'].to_i > 0)
				end
			end
		else
			ret = false
		end
	end
	#puts ret
	ret
end
def count_visible(group)
	group.reduce(0) {|count,element| visible?(element) ? (count+1) : 0}
end


def anchor?(node)
	unless malformed?(node) or text?(node)
		return (node.name.downcase == 'a' and node['href'].nil?)
	else
		return false
	end
end

def has_children?(node)
	unless text?(node) or malformed?(node)
		unless node.children.nil?
			return !node.children.empty?
		else
			return false
		end
	else
		return false
	end
end

def content_container?(node)
	['DL','UL','P','H1','H2','TD','FORM'].include? node.name.upcase
end

def container?(node) #the tag
	if text?(node)
		return false
	elsif malformed?(node)
		return false
	else
		return ['DIV','TABLE','TR','BODY','IFRAME','FRAMESET','FRAME','SECTION','HEADER','FOOTER'].include? node.name.upcase
	end
end

def image?(node)
	['IMG'].include? node.name.upcase
end

def inline?(node)
	['SUP','LI','B','I','BIG','EM','FONT','STRONG','U','IMG','A','INPUT','CODE','EM','CODE','SPAN','BUTTON','DD'].include? node.name.upcase
end

def line_break?(node)
	!inline?(node) and !text?(node)
end

def undesirable_node?(node)
	['SCRIPT','STYLE','MAP','LABEL'].include? node.name.upcase
end

def virtual_text?(node)
#puts "evaluate virtual text on #{node.name}"
	unless malformed?(node) 
		if inline?(node)
			unless node.children.nil?
				text = count_reduce(node.children,"text?")
				vnodes = node.children.collect {|c| virtual_text?(c)}.uniq
				ret = vnodes.size==1 and vnodes[0]==true #with uniq there should be only one true element for be true the expr. or a false. Two values makes true & false = false
				#puts "VT(#{node.name}) #{vnodes} #{ret}"
				return true
			else
				return visible?(node)
			end
		else
			#puts "not inline #{node.name}"
			return text?(node)
		end
	else
		#puts "#{node.name} #{malformed?(node)} #{text?(node)}"
		return false
	end
end


def hyperlink?(node)
node.name.upcase == 'A'
end

def image?(node)
node.name.upcase == 'IMG'
end

def cformat(number)
	"%05.4f"%number.to_f
end
def sum_weights(a,b)
	ret=0
	if a.is_a? Hpricot::Elem and b.is_a? Hpricot::Elem
		ret = a['weight'].to_f + b['weight'].to_f
	end
	ret
end
def count_reduce(collection,function_name)
	unless collection.nil?
		r=0
		#r = collection.reduce(0) {|count,element| send(function_name,element) ? (count+1) : 0}
		collection.each do |element|
			r+=1 if send(function_name,element)
		end
		#puts "counting #{function_name} for #{collection.collect{|x|x.name}} = #{r}"
		return r
	else
		return 0
	end
end
def area(node)
	unless malformed? node
		unless text? node
			b = node['elem_width'].to_f - node['elem_left'].to_f
			h = node['elem_height'].to_f - node['elem_top'].to_f
			#puts "compute de area of #{node.xpath} (#{node['left']},#{node['top']},#{node['width']},#{node['height']}) b=#{b}, h=#{h} b*h=#{b*h}px^2" if ['93','94'].include? node['uid']
			return(b * h)
		else
			return(area(node.parent))
		end
	else
		return(0)
	end
end

def f(node)
na = area(node)
pa = area(node.parent)
w = node['weight'].to_f
level = node['level'].to_i
da = 0
da = na / pa unless pa==0
#(da+w) / (2*(level+1))
w
end

def mark_node(node,doc,rule)
node.set_attribute "candidate","#{doc}"
node.set_attribute "rule","#{rule}"
end

def mark_block(node,doc)
node.set_attribute "block","#{doc}"
end

def classify(node)
	if text? node
		"TEXT"
	elsif ['h1','h2','h3','h4'].include? node.name.downcase
		'TITLE'
	elsif node.name.downcase == 'img'
		'IMG'
	else
		"COMBINED"
	end
end

def fix_children_dimension(bom,element)
	unless element.children.nil? or text?(element) or malformed?(element)
		element.children.each do |c|
			unless text?(c) or malformed?(c) or !valid?(c)
				fix_children_dimension(bom,c)
				if c['elem_left'].to_f<element['elem_left'].to_f or c['elem_left'].to_f<0
					c['elem_left'] = element['elem_left']
				end
				if c['elem_top'].to_f<element['elem_top'].to_f or c['elem_top'].to_f<0
					c['elem_top'] = element['elem_top']
				end
				if c['elem_width'].to_f>element['elem_width'].to_f
					if element['elem_width'].to_i > bom.document.width
						c['elem_width'] = bom.document.width.to_s
					else
						unless element['elem_width'].nil?
							c['elem_width'] = element['elem_width'] 
						end
					end
				end
				if c['elem_height'].to_f>element['elem_height'].to_f or c['elem_height'].to_f<0
					unless element['elem_height'].nil?
						c['elem_height'] = element['elem_height']
					end
				end
			end
		end
	end
end

#example: <div style='background-image: aaa.jpg'> becomes <div><img src='aaa.jpg'></div>
def fix_no_explicit_nodes(element)
	unless text?(element) or malformed?(element)
		unless element['style'].nil?
			if element['style'].strip[0..16]=='background-image:'
				iu = element['style'].strip[17..element['style'].strip.size].gsub('url','').gsub('(','').gsub(')','')
				element.inner_html = "<IMG src='#{iu}' uid='#{element['uid']}_img' id='#{element['id']}_img' elem_left='#{element['elem_left']}' elem_top='#{element['elem_top']}' elem_width='#{element['elem_width']}' height='#{element['elem_height']}' alt='Background Image'/>" + element.inner_html
			end
		end
		unless element.children.nil?
			element.children.each do |child|
				fix_no_explicit_nodes(child)
			end
		end
	end
end

def change_relative_url(bom,element)
	unless text?(element) or malformed?(element)
		unless element['href'].nil?
			if relative? element['href']
				element['href'] = make_absolute(bom.document.url,element['href'])
			end
		end
		unless element['src'].nil?
			if relative? element['src']
				element['src'] = make_absolute(bom.document.url,element['src'])
			end
		end
		unless element.children.nil?
			element.children.each do |child|
				change_relative_url(bom,child)
			end
		end
	end
end

def normalize_DOM(bom,element)
	element.search("//body").each do |e|
		
		if e.elem? or e.is_a? Nokogiri::HTML::Document
			fix_children_dimension(bom,e)
			
			bom.document.width = element.at('//body')['elem_width'].to_f - element.at('//body')['elem_left'].to_f
			bom.document.height = element.at('//body')['elem_height'].to_f - element.at('//body')['elem_top'].to_f
			bom.document_area = bom.document.width * bom.document.height
			
			fix_no_explicit_nodes(e)
			change_relative_url(bom,e)
		end
	end
	element
end

def escape_html(src='')
if src.nil?
	return ""
else
	return CGI::escapeHTML(src) 
end
end

def crypt(txt)
Digest::MD5.hexdigest(txt)
end

def contain_image?(node,level=0)
	ic = []
	unless text?(node) or malformed?(node)
		unless node.children.nil?
			node.children.each do |child|
				if is_image?(child)
					ic.push level
				end
				ic.push contain_image?(child,level+1)
			end
		end
	end
	ic.flatten
end

def is_image?(node)
	node.name.downcase == 'img'
end

def contain_p?(node,level=0)
	pc = []
	unless text?(node) or malformed?(node)
		unless node.children.nil?
			node.children.each do |child|
				if child.name.downcase == 'p'
					pc.push level
				end
				pc.push contain_p?(child,level+1)
			end
		end
	end
	pc.flatten
end

def text_len(node)
	tl = 0
	unless text?(node) or malformed?(node)
		unless node.children.nil?
			node.children.each do |child|
				if text?(child)
					tl+=child.strip.size
				else
					tl+=text_len(child)
				end
			end
		end
	end
	tl
end

def link_text_len(node)
	tl = 0
	unless text?(node) or malformed?(node)
		unless node.children.nil?
			node.children.each do |child|
				if child.name.lowercase == 'a'
					unless child['alt'].nil?
						tl+=child['alt'].size
					end
				else
					tl+=link_text_len(child)
				end
			end
		end
	end
	tl
end

def parent_children_cleanup(children,new_parent)
	to_del = []
	children.each do |child|
		parent_children_cleanup(child.children,child) unless child.children==[]
		if child.parent.id != new_parent.id
			to_del.push child
		end
	end
	to_del.each {|b| children.delete(b)}
end


def get_new_composite_block(bom,parent)
	nb = CompositeBlock.new
	bom.next_block_id+=1
	nb.id = bom.next_block_id
	nb.parent = parent
	if bom.debug
		puts "create new block #{nb.id}"
	end
end

def relative_area(bom,node)
return (area(node)/bom.document_area)
end

def tag_based_weight(node)
	w = 0
	w = 4 if ['BODY'].include? node.name.upcase
	w = 6 if ['DL','UL','DIV','TABLE'].include? node.name.upcase
	w = 9 if ['P','H1','LI'].include? node.name.upcase
	w
end
