require 'cgi'
require 'digest/md5'

def load(source_file,job_id)
	if !File.exists? "#{source_file}"
		puts "File '#{source_file}' not found"
		exit
	else
		doc = open("#{source_file}") { |f| Hpricot(f, :fixup_tags => true) }
	end
	data = YAML.load(doc.at("//comment()[1]").content)
	$window.width = data['window']['width'].to_i
	$window.height = data['window']['height'].to_i
	
	data = YAML.load(doc.at("//comment()[2]").content) 
	$document.width = data['document']['width'].to_i
	$document.height = data['document']['height'].to_i
	$document_area = doc.at('//body')['width'].to_f * doc.at('//body')['height'].to_f
	
	data = YAML.load(doc.at("//comment()[3]").content) 
	$document.url = data['page']['url'].to_s
	$document.date = data['page']['date'].to_s
	$document.title = doc.at('//title').inner_text.strip

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
	node.is_a? Hpricot::Text or node.is_a? String
end

def malformed?(node)
	node.is_a? Hpricot::BogusETag
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
		#puts "#{node.name} #{node.children.size} #{count_reduce(node.children,"visible?")}"
		vis = 0
		if visible?(node)
			vis=0
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
		unless text?(node)
			#puts "#{node.name} #{node.attributes['visibility']} #{node.attributes['display']}"
			if node.attributes['display']=="none" or node.attributes['visibility']!="visible"
				ret = false
			else
				ret = (node.attributes['width'].to_i > 0) or (node.attributes['height'].to_i > 0)
			end
		else
			ret = true
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
		return ['DIV','TABLE','TR','BODY','IFRAME'].include? node.name.upcase
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
			b = node['width'].to_f - node['left'].to_f
			h = node['height'].to_f - node['top'].to_f
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

def fix_children_dimension(element)
	unless element.children.nil? or text?(element) or malformed?(element)
		element.children.each do |c|
			unless text?(c) or malformed?(c) or !valid?(c)
				fix_children_dimension(c)
				#puts "verify element #{c.name} (#{c['id']})"
				if c['left'].to_f<element['left'].to_f or c['left'].to_f<0
					#puts "#{c.name}(#{c['id']}):#{c['left']} is far at left than #{element.name}(#{element['uid']}):#{element['left']}"
					element['left'] = c['left']
				end
				if c['top'].to_f<element['top'].to_f or c['top'].to_f<0
					#puts "#{c.name}(#{c['id']}):#{c['top']} is far at right than #{element.name}(#{element['uid']}):#{element['top']}"
					element['top'] = c['top']
				end
				if c['width'].to_f>element['width'].to_f
					#puts "#{c.name}(#{c['id']}):#{c['width']} is wider than #{element.name}(#{element['uid']}):#{element['width']}"
					if c['width'].to_i > $document.width
						c['width'] = element['width']
					else
						element['width'] = c['width'] 
					end
				end
				if c['height'].to_f>element['height'].to_f or c['height'].to_f<0
					#puts "#{c.name}(#{c['id']}):#{c['height']} is higher than #{element.name}(#{element['uid']}):#{element['height']}"
					element['height'] = c['height']
				end
			end
		end
	end
end

#example: <div style='background-image: aaa.jpg'> becomes <div><img src='aaa.jpg'></div>
def fix_no_explicit_nodes(element)
	unless text?(element) or malformed?(element)
		#puts "verificando #{element.name} #{element['style']}"
		unless element['style'].nil?
			if element['style'].strip[0..16]=='background-image:'
				iu = element['style'].strip[17..element['style'].strip.size].gsub('url','').gsub('(','').gsub(')','')
				element.inner_html = "<IMG src='#{iu}' uid='#{element['uid']}_img' id='#{element['id']}_img' left='#{element['left']}' top='#{element['top']}' width='#{element['width']}' height='#{element['height']}' alt='Background Image'/>" + element.inner_html
			end
		end
		unless element.children.nil?
			element.children.each do |child|
				fix_no_explicit_nodes(child)
			end
		end
	end
end

def change_relative_url(element)
	unless text?(element) or malformed?(element)
		unless element[:href].nil?
			if relative? element[:href]
				element[:href] = make_absolute($document.url,element[:href])
			end
		end
		unless element[:src].nil?
			if relative? element[:src]
				element[:src] = make_absolute($document.url,element[:src])
			end
		end
		unless element.children.nil?
			element.children.each do |child|
				change_relative_url(child)
			end
		end
	end
end

def normalize_DOM(element)
	element.search("/").each do |e|
		if e.is_a? Hpricot::Elem
			fix_children_dimension(e)
			
			$document.width = element.at('//body')['width'].to_f - element.at('//body')['left'].to_f
			$document.height = element.at('//body')['height'].to_f - element.at('//body')['top'].to_f
			$document_area = $document.width * $document.height
			
			fix_no_explicit_nodes(e)
			change_relative_url(e)
		end
	end
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
		puts "cleanup child_id:#{child.id} parent_id:#{child.parent.id} current_block_id:#{new_parent.id}"  if $debug
		if child.parent.id != new_parent.id
			to_del.push child
		end
	end
	to_del.each {|b| children.delete(b)}
end


def footer
svg = ""
svg += "<text x='0' y='#{$document.height+20}'>URL: #{$document.url}</text>"
svg += "<text x='0' y='#{$document.height+50}'>COMMENTS:</text>"
svg += "<line x1='#{0}' y1='#{$document.height+60}' x2='#{$document.width}' y2='#{$document.height+60}' style='stroke:rgb(0,0,0)'/>"
svg += "<line x1='#{0}' y1='#{$document.height+80}' x2='#{$document.width}' y2='#{$document.height+80}' style='stroke:rgb(0,0,0)'/>"
svg += "<line x1='#{0}' y1='#{$document.height+100}' x2='#{$document.width}' y2='#{$document.height+100}' style='stroke:rgb(0,0,0)'/>"


svg += "<text x='0' y='#{$document.height+120}'>RATING:</text>"

svg += "<text x='200' y='#{$document.height+130}' style='font-size:24'>1</text>"
svg += "<circle cx='230' cy='#{$document.height+120}' r='10' style='fill:white;stroke:black'/>"

svg += "<text x='250' y='#{$document.height+130}' style='font-size:24'>2</text>"
svg += "<circle cx='280' cy='#{$document.height+120}' r='10' style='fill:white;stroke:black'/>"

svg += "<text x='300' y='#{$document.height+130}' style='font-size:24'>3</text>"
svg += "<circle cx='330' cy='#{$document.height+120}' r='10' style='fill:white;stroke:black'/>"

svg += "<text x='350' y='#{$document.height+130}' style='font-size:24'>4</text>"
svg += "<circle cx='380' cy='#{$document.height+120}' r='10' style='fill:white;stroke:black'/>"

svg += "<text x='400' y='#{$document.height+130}' style='font-size:24'>5</text>"
svg += "<circle cx='430' cy='#{$document.height+120}' r='10' style='fill:white;stroke:black'/>"

svg += "<text x='0' y='#{$document.height+150}' style='font-size:24'>PDoC = #{$pdoc}</text>"

svg += "<text x='0' y='#{$document.height+180}' style='font-size:24'>Internal Reference: #{$target_path.gsub('/var/www/vseg/pages/','').gsub('/',' ')} </text>"

svg
end

def get_new_composite_block(parent)
	nb = CompositeBlock.new
	$next_block_id+=1
	nb.id = $next_block_id
	nb.parent = parent
	if $debug
		puts "create new block #{nb.id}"
	end
end

def relative_area(node)
return (area(node)/$document_area)
end

def tag_based_weight(node)
	w = 0
	w = 4 if ['BODY'].include? node.name.upcase
	w = 6 if ['DL','UL','DIV','TABLE'].include? node.name.upcase
	w = 9 if ['P','H1','LI'].include? node.name.upcase
	w
end
