#
# This script decrypts packets implementing protocol 5AA5 in the pairing phase
#

from Utils.utils import Utils
import argparse
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives.hashes import Hash, SHA1



def decrypt_auth(packet: str, device_name: bytes, ble_key: str, pair_key: str):

    data = bytes.fromhex(packet)
    ble_data = bytes.fromhex(ble_key)
    app_data = bytes.fromhex(pair_key)

    result = bytearray(len(data) - 6)
    result[:3] = data[:3]

    pl_len = len(data) - 9
    pl = bytearray(pl_len)
    pl[:] = data[3:3 + pl_len]

    it = (data[-2] << 8) + data[-1]

    sha1_key = Utils.calc_sha1_key(app_data, ble_data)

    aes_data = Utils.gen_aes_data(it, ble_data)

    decrypting = bytearray(len(pl))
    byte_idx = 0

    while pl_len > 0:
        tmp_len = pl_len if pl_len <= 16 else 16
        xor_data1 = bytearray(16)
        xor_data1[:tmp_len] = pl[byte_idx:byte_idx + tmp_len]

        aes_data[15] += 1
        aes_key = Utils.aes_ecb_encrypt(aes_data, sha1_key)

        xor_data2 = bytearray(16)
        xor_data2[:] = aes_key[:]

        xor_data = Utils.xor(xor_data1, xor_data2, 16)
        decrypting[byte_idx:byte_idx + tmp_len] = xor_data[:tmp_len]

        pl_len -= tmp_len
        byte_idx += tmp_len

    result[3:] = decrypting[:]

    return result.hex()


       
