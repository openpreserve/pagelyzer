class Driver
attr_reader :name,:browser,:host,:port
def initialize(name,browser,host,port)
@name = name
@browser = browser
@port = port
@host = host
end
def filename
	"#{@name.gsub(' ','_')}.png"
end
def browser_driver
	"*#{@browser}"
end
def browser_symbol
	@browser.to_sym
end
end
