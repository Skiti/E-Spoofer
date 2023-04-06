#
# Xiaomi scooters toolkit - utils.py
# 
#

from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives.hashes import Hash, SHA1


init_value = bytes.fromhex("97CFB802844143DE56002B3B34780A5D")

class Utils():

    def calc_sha1_key(b1, b2):
        data = bytearray(32)
        data[:16] = b1
        data[16:] = b2

        sha = Hash(SHA1())
        sha.update(data)
        h = sha.finalize()[:16]
        assert len(h) == 16
        return h


    def xor(d1, d2, size):
        result = bytearray(size)
        for i in range(size):
            result[i] = d1[i] ^ d2[i]
        return result


    def aes_ecb_encrypt(data, key):
        aes = Cipher(
            algorithms.AES(key),
            modes.ECB(),
        ).encryptor()
        ct = aes.update(data) + aes.finalize()
        return ct


    def gen_aes_data(it, ble_data):
            aes_data = bytearray(16)
            aes_data[0] = 1
            aes_data[1] = (it & 0xff000000) >> 24
            aes_data[2] = (it & 0x00ff0000) >> 16
            aes_data[3] = (it & 0x0000ff00) >> 8
            aes_data[4] = (it & 0x000000ff) >> 0
            aes_data[5:5 + 8] = ble_data[:8]
            aes_data[15] = 0
            return aes_data 


    def crc_next(data, sha1_key=None, aes_data=None):
        if sha1_key is None and aes_data is None:
            result = bytearray(2)
            crc = ~sum(data)
            result[0] = crc & 0xff
            result[1] = (crc >> 8) & 0xff
            return result

        aes_key = Utils.aes_ecb_encrypt(aes_data, sha1_key)

        xor_data1 = bytearray(16)
        xor_data1[:3] = data[:3]
        xor_data2 = bytearray(16)
        xor_data2[:] = aes_key[:]

        xor_data = Utils.xor(xor_data1, xor_data2, 16)
        aes_key = Utils.aes_ecb_encrypt(xor_data, sha1_key)
        xor_data2[:] = aes_key[:]

        pl_len = len(data) - 3
        byte_idx = 3
        while pl_len > 0:
            tmp_len = pl_len if pl_len <= 16 else 16

            xor_data1 = bytearray(16)
            xor_data1[:tmp_len] = data[byte_idx:byte_idx + tmp_len]

            # like in crypto_next, but first xor then aes
            xor_data = Utils.xor(xor_data1, xor_data2, 16)

            aes_key = Utils.aes_ecb_encrypt(xor_data, sha1_key)
            xor_data2[:] = aes_key[:]

            pl_len -= tmp_len
            byte_idx += tmp_len

        aes_data[0] = 1
        aes_data[15] = 0

        aes_key = Utils.aes_ecb_encrypt(aes_data, sha1_key)
        xor_data1[:4] = aes_key[:4]

        crc = Utils.xor(xor_data1, xor_data2, 4)

        return crc
        
    def crypto_next(inp_data, sha1_key, aes_data=None):
        result = bytearray(len(inp_data))

        byte_idx = 0
        pl_len = len(inp_data)

        while pl_len > 0:
            tmp_len = pl_len if pl_len <= 16 else 16
            xor_data1 = bytearray(16)
            xor_data1[:tmp_len] = inp_data[byte_idx:byte_idx + tmp_len]

            if aes_data is None:
                aes_key = Utils.aes_ecb_encrypt(init_value, sha1_key)
            else:
                aes_data[15] += 1
                aes_key = Utils.aes_ecb_encrypt(aes_data, sha1_key)

            xor_data2 = bytearray(16)
            xor_data2[:] = aes_key[:]

            xor_data = Utils.xor(xor_data1, xor_data2, 16)
            result[byte_idx:byte_idx + tmp_len] = xor_data[:tmp_len]

            pl_len -= tmp_len
            byte_idx += tmp_len

        return result
