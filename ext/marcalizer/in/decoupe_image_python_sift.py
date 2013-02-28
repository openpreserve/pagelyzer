import os
import time
import sys
import Image
import _imaging
#print _imaging.__file__

image_filepath = sys.argv[1]
tmp_filename = "tmp.png"
max_height = 1000
new_path = sys.argv[2]

for f in os.listdir(image_filepath):
    s1 = "%s/%s" % (image_filepath, f)
    s2 = "%s/%s" % (new_path, f)
#    os.mkdir(s2)
    for f2 in os.listdir(s1):
        im = Image.open("%s/%s"%(s1,f2))
        im.convert("L")
        (width, height) = im.size
        nf = new_path+"/"+f+"/"+f2[:-4]+".png"
        im.crop((0,0,width, min(height, max_height))).save(nf)
        os.system("convert "+nf+" -flatten +matte "+nf)
