rule found_inspect_packets
{
    strings:
        $a = { 55 28 01 d0 5a 28 02 d1 63 78 00 2b 04 d0 }
    condition:
        $a
}

rule found_advertising
{
    strings:
        $a = { 02 aa 00 21 30 46 04 f0 b6 fd }
    condition:
        $a
}

rule found_refuse_55aa
{
    strings:
        $a = { 49 79 55 28 02 d0 aa 28 04 d0 08 e0 00 29 06 d1 6a 71 70 bd 00 29 02 d0 }
    condition:
        $a
}

rule found_manage_incoming_traffic
{
    strings:
        $a = { 00 f0 f6 fb 15 b0 f0 bd a2 78 72 4f 50 2a 74 d0 13 dc 08 2a 09 d0 04 dc 01 2a 60 d0 07 2a f1 d1 }
    condition:
        $a
}

rule found_disable_5aa5
{
    strings:
        $a = { 02 7f 1f 48 3e 2a 11 d0 01 22 8a 76 82 78 f7 23 1a 40 }
    condition:
        $a
}

rule found_init_miservice
{
    strings:
        $a = { 00 28 09 d0 01 28 39 d0 02 28 03 d1 1f a1 13 20 06 f0 f9 f9 03 b0 f0 bd 60 68 26 49 05 68 28 89 88 42 f7 d1 25 a1 13 20 ee 69 06 f0 ec f9 20 78 00 28 1b d1 2c 4c 68 88 a0 80 f2 8b 30 46 e2 80 40 30 c3 89 23 81 c0 8f 60 81 e7 89 a1 89 01 91 00 90 02 97 25 49 13 20 06 f0 e5 fa 14 20 00 90 e1 88 a0 88 22 4b 00 22 05 f0 f5 f9 30 46 00 f0 b8 fe 28 46 00 f0 b5 fe 03 b0 f0 bd }
    condition:
        $a
}