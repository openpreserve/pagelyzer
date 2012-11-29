fin=File.open(ARGV[0]).read
fout = fin.gsub /(?<!\n)\n(?!\n)/,' '
fout = fout.gsub /^$\n/,''
fout = fout.gsub(/\s+/,' ').strip
File.open(ARGV[1],'w') {|f| f.write(fout)}
