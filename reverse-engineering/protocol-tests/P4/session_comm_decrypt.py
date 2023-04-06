 
#
# Xiaomi scooters toolkit - comm_decrypt.py
# 
#
import secrets
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.ciphers.aead import AESCCM
from cryptography.hazmat.primitives.hmac import HMAC
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.backends import default_backend


def decrypt_uart(key, iv, msg):
        header = msg[:2]
        if header != b'\x55\xab':
            raise Exception("Invalid header.")

        it = msg[3:5]
        ct = msg[5:-2]

        nonce = iv + bytes([0] * 4) + it + bytes([0] * 2)
    
        aes_ccm = AESCCM(key, tag_length=4)
        return aes_ccm.decrypt(nonce, ct, None)[:-4]


def comm_decrypt(dev_key: bytes, dev_iv: bytes, pkt: bytes) -> bytearray:
    """Encrypt a packet"""
    
    assert type(dev_key) == bytes
    assert type(dev_iv) == bytes
    assert type(pkt) == bytes
    
    return decrypt_uart(dev_key, dev_iv,pkt)
    

if __name__ == "__main__":

    pass
