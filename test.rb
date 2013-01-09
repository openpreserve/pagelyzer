def cd(url1,url2,b1,b2,t)
	if b1==b2
		return ["ruby change_detection.rb --url1=#{url1} --url2=#{url2} --browser=#{b1} --type=#{t}",url1,url2,b1,b2,t]
	else
		return ["ruby change_detection.rb --url1=#{url1} --url2=#{url2} --browser1=#{b1} --browser2=#{b2} --type=#{t}",url1,url2,b1,b2,t]
	end
end

tset = ["http://www.lip6.fr","http://www.upmc.fr","http://www.cnn.com","http://stackoverflow.com","http://web.archive.org/web/20110101121609/http://www.bl.uk/","http://web.archive.org/web/20110724060948/http://www.bl.uk/"]
bset = ["firefox","chrome"]
kset = ["hybrid","visual"]
cmd = []

File.open("t.csv","w")

0.upto(tset.size-1) do |i|
	0.upto(tset.size-1) do |j|
		0.upto(bset.size-1) do |b1|
			0.upto(bset.size-1) do |b2|
				0.upto(kset.size-1) do |t|
					cmd.push cd(tset[i],tset[j],bset[b1],bset[b2],kset[t])
				end
				break
			end
			break
		end
		break
	end
end

cmd.each do |c|
	puts c[0]
	r = `#{c[0]}`
	#system(c[0])
	File.open("t.csv","a") {|f| f.puts "#{c[5]};#{c[3]};#{c[1]};#{c[4]};#{c[2]};#{r.gsub("\n","")}"}
end

