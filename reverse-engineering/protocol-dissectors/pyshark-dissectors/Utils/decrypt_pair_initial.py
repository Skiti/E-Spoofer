#
# This script decrypts packets implementing protocol 5AA5 in the pairing phase
#
# ea2c71f4b38d808eb798396c1e52958d

from Utils.utils import Utils
import argparse
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives.hashes import Hash, SHA1


def decrypt_pair_initial(packet: str, device_name: bytes):

    #data = bytes.fromhex(args.hex)
    data = bytes.fromhex(packet)
    result = bytearray(len(data) - 6)
    result[:3] = data[:3]

    pl_len = len(data) - 9
    pl = bytearray(pl_len)
    pl[:] = data[3:3 + pl_len]

    #device_name = b'83629197'
    FW_DATA = bytes.fromhex("97CFB802844143DE56002B3B34780A5D")
    sha1_key = Utils.calc_sha1_key(device_name, FW_DATA)

    decrypting = bytearray(len(pl))
    byte_idx = 0

    while pl_len > 0:
        tmp_len = pl_len if pl_len <= 16 else 16
        xor_data1 = bytearray(16)
        xor_data1[:tmp_len] = pl[byte_idx:byte_idx + tmp_len]

        aes_key = Utils.aes_ecb_encrypt(FW_DATA, sha1_key)

        xor_data2 = bytearray(16)
        xor_data2[:] = aes_key[:]

        xor_data = Utils.xor(xor_data1, xor_data2, 16)
        decrypting[byte_idx:byte_idx + tmp_len] = xor_data[:tmp_len]

        pl_len -= tmp_len
        byte_idx += tmp_len

    result[3:] = decrypting[:]

    return result.hex()

       
