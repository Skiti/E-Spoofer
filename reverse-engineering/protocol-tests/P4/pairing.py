 
#
# Xiaomi scooters toolkit - pairing.py
# 
#

from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.ciphers.aead import AESCCM
from cryptography.hazmat.primitives.hmac import HMAC
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.backends import default_backend

def calc_did(priv_key,cb,data):
    
    cb = ec.EllipticCurvePublicKey.from_encoded_point(ec.SECP256R1(), b'\x04' + cb)
    priv_key = ec.derive_private_key(priv_key, ec.SECP256R1())
    
    e_share_key = priv_key.exchange(ec.ECDH(), cb)
    info = b"mible-setup-info"

    derived_key = HKDF(
        algorithm=hashes.SHA256(),
        length=64,
        salt=None,
        info=info,
        backend=default_backend()
    ).derive(e_share_key)

    token = derived_key[0:12]
    bind_key = derived_key[12:28]
    a = derived_key[28:44]

    aes_ccm = AESCCM(a, tag_length=4)
    nonce = bytes([16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27])
    aad = b"devID"
    did_ct = aes_ccm.encrypt(nonce, data[4:], aad)

    #print("eShareKey:", e_share_key.hex())
    #print("HKDF result: ", derived_key.hex())
    #print("token:", token.hex())
    #print("bind_key:", bind_key.hex())
    #print("A:", a.hex())
    #print("AES did CT: ", did_ct.hex())

    return did_ct, token



def pairing(priv_key: int, cb: bytearray, data: bytes) -> bytearray:
    """Obfusc a packet

    """
    assert type(priv_key) == int
    assert type(data) == bytes
    assert type(cb) == bytearray 
    
    return calc_did(priv_key,cb,data)


if __name__ == "__main__":

    pass
