# Protocol Tests - P2

## Overview

The P2 protocol exchanges obfuscated application-layer packets. The packets are obfuscated by performing a XOR using a XOR mask, and deobfuscated by performing the same XOR with the same XOR mask (since XOR is associative). The XOR mask is easily obtainable, as it is sent in cleartext at the start of every BLE connection over the Hardcopy Data Channel characteristic. 

### Prerequisites

* A machine with Python.
* Some sample obfuscated P2 packet to deobfuscate (or some cleartext P2 packets to obfuscate).
* The correct XOR mask, obtainable from the capture file.

## Setup
* No setup required.

### Execution
* The syntax is the following <code>python3 obfuscation-test-P2.py [packet] [mask]</code>.
* Run the following command <code>python3 obfuscation-test-P2.py 55ab03562ff7b700000000c9fd 762ea7a24e25c2685181bd57</code>.
* Observe the obfuscated/deobfuscated packet, which should be <code>55ab0320015015987c</code>.
