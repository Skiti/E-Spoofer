from session_auth import *

def test_sess_auth():
    
    token = bytes.fromhex('FAE06D113992E3B58D1B19D1')
    rand_challenge = bytes.fromhex('1952712bbacec53be78df9b94aedabd9')
    remote_rand_challenge = bytes.fromhex('eba0a72095d80439e6aa7ce9bb32a698')
    remote_info = bytes.fromhex('19e2918d6a8a5b297e193774ed4de04f14d3c0db5e3386a3910a87f74980da09')

    print("\nToken: " + token.hex())
    print("Random Session Challenge: " + rand_challenge.hex())
    print("Random Remote Session Challenge: " + remote_rand_challenge.hex())
    print("Remote Info: " + remote_info.hex())

    info, expected_info, derived_keys = auth(token, rand_challenge, remote_rand_challenge, remote_info)

    info_v = bytes.fromhex('aececd656a602400f78f41f2bbb3125aa8167dcf58a48a1d19e9b79c4232587e')
    expected_info_v = bytes.fromhex('19e2918d6a8a5b297e193774ed4de04f14d3c0db5e3386a3910a87f74980da09')
    derived_keys_v = bytes.fromhex('442134d3db1ea70a1a9e3c8a9d38e97d255bd024ed489c43aff395ef32e324ca8faf2c0c32b0e5bc1830e54476a658e04ad36811bf8aa1ea1a99e087ecd222cb')
    
    print("\nInfo: " + info.hex())
    print("Info_v: " + info_v.hex())
    assert info == info_v
    print("Correct! The two values match.")

    print("\nExp_Info: " + expected_info.hex())
    print("Exp_Info_v: " + expected_info_v.hex())
    assert expected_info == expected_info_v
    print("Correct! The two values match.")

    print("\nDerived_keys: " + derived_keys.hex())
    print("Derived_keys_v: " + derived_keys_v.hex())
    assert derived_keys == derived_keys_v
    print("Correct! The two values match.\n")

if __name__ == "__main__":

    test_sess_auth()
 
