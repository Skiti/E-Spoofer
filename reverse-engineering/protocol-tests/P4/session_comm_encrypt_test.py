from session_comm_encrypt import *

def test_comm_encrypt():
    
    app_key = bytes.fromhex("987009144c8118eff7fd598cdb49d302")
    app_iv = bytes.fromhex("633c38e8")
    pkt = bytes.fromhex("55aa032001100e")
    it = 0

    print("\nApp_key: " + app_key.hex())
    print("App_iv: " + app_iv.hex())
    print("Pkt: " + pkt.hex())
    
    encrypted = comm_encrypt(app_key, app_iv, pkt, it)
                               
    encrypted_v = bytes.fromhex("55ab0300004ac3826f713080f7668a352899fa")
    
    print("\nEncrypted[:9]: " + encrypted[:9].hex())
    print("Encrypted_v[:9]: " + encrypted_v[:9].hex())
    assert encrypted[:9] == encrypted_v[:9]
    print("Correct! The two values match.\n")
    

if __name__ == "__main__":

    test_comm_encrypt()