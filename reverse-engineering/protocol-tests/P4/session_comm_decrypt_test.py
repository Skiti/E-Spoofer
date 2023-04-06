from session_comm_decrypt import *

def test_comm_decrypt():
    
    dev_key = bytes.fromhex("fdccd8204d3fe10e558834ef523acb09")
    dev_iv = bytes.fromhex("dff222e6")
    pkt = bytes.fromhex("55ab100100eb48b60b1b7cee5d8b6fdb8b6ba7dd5223ebc380a78436c70df1f2")

    print("\nDev_key: " + dev_key.hex())
    print("Dev_iv: " + dev_iv.hex())
    print("Pkt: " + pkt.hex())
    
    decrypted = comm_decrypt(dev_key, dev_iv, pkt)
    decrypted_v = bytes.fromhex("23011031363133332f3031313635373837")
    
    print("\nDecrypted: " + decrypted.hex())
    print("Decrypted_v: " + decrypted_v.hex())
    assert decrypted == decrypted_v
    print("Correct! The two values match.\n")
    

if __name__ == "__main__":

    test_comm_decrypt()