# Reverse-Engineering Tools

## Overview

### Firmware Reversing

The [Firmware Reversing](https://github.com/Skiti/Espoofer/tree/main/reverse-engineering/firmware-reversing) folder contains tools and material useful for reversing Xiaomi BLE firmware, including the original BLE152 (running P4v1), our Ghidra reversing project on BLE152 firmware, and some Yara rules.

### Frida Hooks

The [Frida Hooks](https://github.com/Skiti/Espoofer/tree/main/reverse-engineering/frida-hooks) folder contains the Frida hooks targeting Mi Home. Our hooks detect and print cryptography-related values during both Pairing and Session.

### Protocol Dissectors

The [Protocol Dissectors](https://github.com/Skiti/Espoofer/tree/main/reverse-engineering/protocol-dissectors) folder contains the automated Pyshark dissectors, as well as the Scapy dissectors, to analyze Xiaomi BLE packets. Our dissectors can analyze any Xiaomi protocol version, and we provide sample BLE packets to test.

### Protocol Tests

The [Protocol Tests](https://github.com/Skiti/Espoofer/tree/main/reverse-engineering/protocol-tests) folder contains test scripts for the syntax of Xiaomi protocols (P2, P3, P4v1 and P4v2). Those tests involve Xiaomi Pairing and Session, and include operations regarding Authentication, Communication and Encryption.
