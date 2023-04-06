import argparse

def xor(d1, d2, size):
    result = bytearray(size)
    for i in range(size):
        result[i] = d1[i] ^ d2[i]
    return result 

parser = argparse.ArgumentParser()
parser.add_argument("hex", help="hex string to obfuscate/deobfuscate")
parser.add_argument("mask", help="XOR mask")
args = parser.parse_args()

if args.hex == None: #check hex string
    __exit__

if args.mask == None: #check mask
    __exit__
    

data = bytes.fromhex(args.hex)
mask = bytes.fromhex(args.mask)


result = bytearray()
result[:3] = data[:3]

pl_len = len(data) - 3
pl = bytearray(pl_len)
pl[:] = data[3:3 + pl_len]


result[4:] = xor(mask, pl, len(pl))


print(result.hex())

if(result.hex()[6:8] == '22' or result.hex()[6:8] == '20'):
    result[7] = result[11]
    result[8] = result[12]
    result = result[:9]
    

print(result.hex())