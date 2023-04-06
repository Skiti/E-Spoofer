from obfuscation import *

def test_obfuscation():

    clear  =  bytearray.fromhex('5aa5003e215b00')
    cipher  =  bytearray.fromhex('5aa500a1616a44000045ff0000')
    ComputedCipher   =  obfuscation(clear)
    print("Cleartext: " + clear.hex())
    print("Expected Ciphertext: " + cipher.hex())
    print("Output Ciphertext: " + ComputedCipher.hex())
    assert ComputedCipher == cipher
    print("\nCorrect! The two values match.\n")


    clear  =  bytearray.fromhex('5aa5013e2001102c')
    cipher  =  bytearray.fromhex('5aa501a16030548b000064ff0000')
    ComputedCipher   =  obfuscation(clear)
    print("Cleartext: " + clear.hex())
    print("Expected Ciphertext: " + cipher.hex())
    print("Output Ciphertext: " + ComputedCipher.hex())
    assert ComputedCipher == cipher
    print("\nCorrect! The two values match.\n")


if __name__ == "__main__":

    test_obfuscation()