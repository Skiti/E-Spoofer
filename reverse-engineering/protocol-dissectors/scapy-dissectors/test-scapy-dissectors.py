"""
dissect.py

scooter (sco) is the BLE peripheral and application (app) is the BLE central.

"""

import os
from scapy.all import *
from binascii import unhexlify

# P1 55AA

class P1Write(Packet):
    name = "P1Write"
    fields_desc = [
        XNBytesField("header", None, 2),
        BitFieldLenField("len", None, size=8, length_of="payload"),
        XByteField("target", None),
        XByteField("operation", None),
        XByteField("command", None),
        StrLenField("payload", None, length_from=lambda pkt:pkt.len -2),
        XNBytesField("checksum", None, 2),
    ]

class P1Notify(Packet):
    name = "P1Notify"
    fields_desc = [
        XNBytesField("header", None, 2),
        BitFieldLenField("len", None, size=8, length_of="payload"),
        XByteField("target", None),
        XByteField("operation", None),
        XByteField("command", None),
        StrLenField("payload", None, length_from=lambda pkt:pkt.len -2),
        XNBytesField("checksum", None, 2),
    ]


# P2 55AB

class P2Write(Packet):
    name = "P2Write"
    fields_desc = [
        XNBytesField("header", None, 2),
        XByteField("len", None),
        XNBytesField("encrypted", None, 8),
        XNBytesField("checksum", None, 2),
    ]

class P2Notify(Packet):
    name = "P2Notify"
    fields_desc = [
        XNBytesField("header", None, 2),
        XByteField("len", None),
        XNBytesField("encrypted", None, 9),
        XNBytesField("checksum", None, 2),
    ]

# P3 5AA5

class P3Decrypted(Packet):
    name = "P3Decrypted"
    fields_desc = [
        XNBytesField("header", None, 2),
        XByteField("len", None),
        XByteField("source", None),
        XByteField("target", None),
        XNBytesField("iteration", None, 2),
    ]


# P4 55AB

class P4Encrypted(Packet):
    name = "P4Encrypted"
    fields_desc = [
        XNBytesField("header", None, 2),
        XByteField("len", None),
        XNBytesField("iteration", None, 2),
        XNBytesField("ciphertext", None, 12),
        XNBytesField("crc", None, 2),
    ]

class P4PairChalPart1(Packet):
    name = "P4PairChalPart1"
    fields_desc = [
        XNBytesField("header", None, 2),
        XNBytesField("other", None, 4),
        XNBytesField("chalpart1", None, 14),
    ]

class P4PairChalPart2(Packet):
    name = "P4PairChalPart2"
    fields_desc = [
        XNBytesField("header", None, 2),
        XNBytesField("chalpart2", None, 14),
    ]

class P4PairPubkey(Packet):
    name = "P4PairPubkey"
    fields_desc = [
        XNBytesField("part", None, 2),
        XNBytesField("keypart", None, 18),
    ]

class P4PairPubkeyPart4(Packet):
    name = "P4PairPubkeyPart4"
    fields_desc = [
        XNBytesField("part", None, 2),
        XNBytesField("keypart", None, 10),
    ]

class P4SessChalPart1(Packet):
    name = "P4SessChalPart1"
    fields_desc = [
        XNBytesField("part", None, 2),
        XNBytesField("chalpart", None, 18),
    ]  

class P4SessChalPart2(Packet):
    name = "P4SessChalPart2"
    fields_desc = [
        XNBytesField("part", None, 2),
        XNBytesField("chalpart", None, 14),
    ]  

# Advertisement

class Advertisement(Packet):
    name = "P4SessChalPart2"
    fields_desc = [
        XNBytesField("header", None, 11),
        XByteField("pairingmode", None),
        XNBytesField("unknown1", None, 9),
        XNBytesField("scootername", None, 13),
        XNBytesField("unknown2", None, 2),
        XNBytesField("manufacturer", None, 2),
        XByteField("scootermodel", None),
        XByteField("securitylevel", None),
        XNBytesField("unknown3", None, 4),
    ]

if __name__ == "__main__":

    # P1 55AA
    p1write = P1Write(unhexlify("55aa0320012502b4ff"))
    p1notify = P1Notify(unhexlify("55aa04230125040ba3ff"))

    # P2 55AB
    p2write = P2Write(unhexlify("55ab03718862adb8666afc70fb"))
    p2notify = P2Notify(unhexlify("55ab047288c0a3c04f17df2970fb"))

    # P3 5AA5
    p3pairreqdec = P3Decrypted(unhexlify("5aa5003d215b00"))
    p3paircontdec = P3Decrypted(unhexlify("5aa51e213d5b01"))
    p3pairblekeydec = P3Decrypted(unhexlify("5aa5103d215c00"))
    p3pairenddec = P3Decrypted(unhexlify("5aa500213d5c01"))

    p3sessreqdec = P3Decrypted(unhexlify("5aa50e3d215d00"))
    p3sessenddec = P3Decrypted(unhexlify("5aa500213d5d01"))

    # P4 55AB
    p4encrypted = P4Encrypted(unhexlify("55ab0300004ac3826f713080f7668a352899fa"))

    # P4 Pairing
    p4pairchalpart1 = P4PairChalPart1(unhexlify("010034ebb3ee54aa572f6a12f40d686af5f517f5"))
    p4pairchalpart2 = P4PairChalPart2(unhexlify("0200c801b78d5d470632d18071745e22"))

    p4pairpubkeypart1 = P4PairPubkey(unhexlify("0100377bbb56efbac98168669b1635c4cd48b562"))
    p4pairpubkeypart2 = P4PairPubkey(unhexlify("0200f10ea92c2cda9e2d476c627dd5a99058cf04"))
    p4pairpubkeypart3 = P4PairPubkey(unhexlify("0300f3e8cceb08a1530242a5fe34437956bea1d9"))
    p4pairpubkeypart4 = P4PairPubkeyPart4(unhexlify("040087a9e309f8100c8ba9ad"))

    # P4 Session Establishment
    p4sesschalpart1 = P4SessChalPart1(unhexlify("0100b144b534b2e4dbd618dc51811e5a676cf280"))
    p4sesschalpart2 = P4SessChalPart2(unhexlify("020069cebe9e129cca4181c7c584613b"))

    # Advertisement
    advertisementpairoff = Advertisement(unhexlify("0201060f1695fe30580e0f01b255a7d9bffe080e094d4953636f6f7465723737323309ff4e422e02000000cf"))
    advertisementpairon = Advertisement(unhexlify("0201060f1695fe305a0e0f02b255a7d9bffe080e094d4953636f6f7465723737323309ff4e422e02000000cf"))

    interact(mydict=globals(), mybanner="Analyze Xiaomi e-scooter BLE packets")
