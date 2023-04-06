from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.ciphers.aead import AESCCM
from cryptography.hazmat.primitives.hmac import HMAC
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.backends import default_backend


def calc_auth(token, rand_challenge,remote_rand_challenge,info):
        
    salt = rand_challenge + remote_rand_challenge
    salt_inv = remote_rand_challenge + rand_challenge
        
    info = b"mible-login-info"
    derived_keys = HKDF(
        algorithm=hashes.SHA256(),
        length=64,
        salt=salt,
        info=info,
        backend=default_backend()
    ).derive(token)
    
    keys = {
        'dev_key': derived_keys[:16],
        'app_key': derived_keys[16:32],
        'dev_iv': derived_keys[32:36],
        'app_iv': derived_keys[36:40],
    }
    
    hmac = HMAC(keys['app_key'], algorithm=hashes.SHA256())
    hmac.update(salt)
    info = hmac.finalize()
    
    hmac = HMAC(keys['dev_key'], algorithm=hashes.SHA256())
    hmac.update(salt_inv)
    expected_remote_info = hmac.finalize()
        
    return info, expected_remote_info, derived_keys


def auth(token: bytes, rand_challenge: bytes, remote_rand_challenge: bytes, remote_info: bytes) -> bytearray:

    assert type(token) == bytes
    assert type(rand_challenge) == bytes
    assert type(remote_rand_challenge) == bytes
    assert type(remote_info) == bytes
    
    return calc_auth(token, rand_challenge, remote_rand_challenge, remote_info)


if __name__ == "__main__":

    pass