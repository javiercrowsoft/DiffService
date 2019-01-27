#!/usr/bin/python

import sys

from base64 import b64encode
from json import dumps

if len(sys.argv) < 3:
  print "Usage inputFile outputFile"
  sys.exit(2)

ENCODING = 'utf-8'
IMAGE_NAME = sys.argv[1]
JSON_NAME = sys.argv[2]

# first: reading the binary stuff
# note the 'rb' flag
# result: bytes
with open(IMAGE_NAME, 'rb') as open_file:
    byte_content = open_file.read()

# second: base64 encode read data
# result: bytes (again)
base64_bytes = b64encode(byte_content)

# third: decode these bytes to text
# result: string (in utf-8)
base64_string = base64_bytes.decode(ENCODING)

# optional: doing stuff with the data
# result here: some dict
raw_data = {"name":IMAGE_NAME, "data": base64_string}

# now: encoding the data to json
# result: string
json_data = dumps(raw_data, indent=2)

# finally: writing the json string to disk
# note the 'w' flag, no 'b' needed as we deal with text here
with open(JSON_NAME, 'w') as another_open_file:
    another_open_file.write(json_data)