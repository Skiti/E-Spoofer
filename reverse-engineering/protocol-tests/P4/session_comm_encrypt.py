 
#
# Xiaomi scooters toolkit - comm_encrypt.py
# 
#
import secrets
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.ciphers.aead import AESCCM
from cryptography.hazmat.primitives.hmac import HMAC
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.backends import default_backend

def int_to_bytes(i, size=4):
    res = bytearray(size)
    for j in range(size):
        res[j] = (i >> (j * 8)) & 0xff
    return res


def crc16(arr):
    n = ~sum(arr)
    return int_to_bytes(n, 2)


def encrypt_uart(key, iv, msg, it=0, rand=secrets.token_bytes(4)):
    msg = msg[2:]  # ditch header

    size = msg[:1]
    data = msg[1:]
    data += rand  # add four random bytes to data

    it = int_to_bytes(it)  # encode iterator to four bytes
    nonce = iv + bytes([0] * 4) + it

    aes_ccm = AESCCM(key, tag_length=4)
    ct = aes_ccm.encrypt(nonce, data, None)

    header = b'\x55\xab'  # new header
    data = size + it[:2] + ct  # new data
    crc = crc16(data)  # new checksum

    return header + data + crc


def comm_encrypt(app_key: bytes, app_iv: bytes, pkt: bytes, it: int) -> bytearray:
    """Encrypt a packet"""
    
    assert type(app_key) == bytes
    assert type(app_iv) == bytes
    assert type(pkt) == bytes
    assert type(it) == int
    
    return encrypt_uart(app_key, app_iv,pkt, it)
    

if __name__ == "__main__":

    pass
