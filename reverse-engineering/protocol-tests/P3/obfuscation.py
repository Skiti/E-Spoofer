from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives.hashes import Hash, SHA1
from utils import Utils 

init_value = bytes.fromhex("97CFB802844143DE56002B3B34780A5D")
device_name = b'MIScooter5787'


def obfuscation(data: bytearray) -> bytearray:
    """Obfusc a packet

    """
    assert type(data) == bytearray
    
    result = bytearray(152)
    result[:3] = data[:3]

    pl_len = len(data) - 3
    pl = bytearray(pl_len)
    pl[:] = data[3:3 + pl_len]
    
    sha1_key = Utils.calc_sha1_key(device_name, init_value)
    
    crc = Utils.crc_next(pl)
    enc = Utils.crypto_next(pl, sha1_key)
    
    result[3:3 + pl_len] = enc[:]
    result[pl_len + 3] = 0
    result[pl_len + 4] = 0
    result[pl_len + 5] = crc[0]
    result[pl_len + 6] = crc[1]
    result[pl_len + 7] = 0
    result[pl_len + 8] = 0
    result = result[:pl_len + 9]
    
    assert type(result) == bytearray
    return result


if __name__ == "__main__":

    pass