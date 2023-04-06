from pairing import *

def test_pairing():

    priv_key = 105254998248991456785192364562040725539418728932325801395197577422824712686581
    data  =  bytes.fromhex('0100000000626c742e342e313871766a6664386767673030')
    cb  =  bytearray.fromhex('7f26e625c39567724ceea3e6610acbd0083427688f77efd02d0ffd831eb2a6db0994c7a35c75301b9da2031303f66bbe7ead9586f50bf9704ac2868c5af45930')

    print("\nPrivate Key: " + str(priv_key))
    print("Data: " + data.hex())
    print("Cb: " + cb.hex())
    
    did_ct, token =  pairing(priv_key, cb, data)
    
    did_ct_v = bytes.fromhex('47c7555ed23dc5aebe8e65d9e2dd37b8e5ea203c1cb568df')
    token_v = bytes.fromhex('FAE06D113992E3B58D1B19D1')
    
    print("\nDid_ct: " + did_ct.hex())
    print("Did_ct_v: " + did_ct_v.hex())
    assert did_ct == did_ct_v
    print("Correct! The two values match.")

    print("\nToken: " + token.hex())
    print("Token_v: " + token_v.hex())
    assert token == token_v
    print("Correct! The two values match.\n")

if __name__ == "__main__":

    test_pairing()