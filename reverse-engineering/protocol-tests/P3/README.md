# Protocol Tests - P3

## Overview

The P3 protocol exchanges obfuscated application-layer packets. The packets are obfuscated by XORing the cleartext packet with a deterministic Pairing Key (obtained from the e-scooter name and an hard-coded constant). The deofuscation process is similar. This is true for P3 Pairing and Session.

This folder contains the following files:
* The *obfuscation.py* script, that obfuscates/deobfuscates P3 packets.
* The *obfuscation-test-P3.py* script, that run the test. 
* The *utils.py* script, that performs cryptography.
* The *sample-5AA5.pcap* file, a sample BLE capture for testing purposes.

### Prerequisites
* A machine with Python.
* Some sample Xiaomi P3 packets (already provided inside the *obfuscation-test-P3.py* script).

## Setup
* Run the following command from this folder <code>pip3 install -r requirements.txt</code>.

### Execution
* Run the following command <code>python3 obfuscation-test-P3.py</code>.
* Observe the outcome of the test, which should pass.