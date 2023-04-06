# Protocol Tests - P4

## Overview

The P4 protocol perform Elliptic-Curve Diffie-Hellman key exchange to agree on a shared secret Pairing Key, used to encrypt application-layer packets. 

This folder contains the following files:
* The *pairing_test.py* script, that tests P4 Pairing.
* The *pairing.py* script, that implements P4 Pairing.
* The *session_auth_test.py* script, that tests Authentication during P4 Session.
* The *session_auth.py* script, that implements Authentication during P4 Session.
* The *session_comm_decrypt_test.py* script, that decrypts Xiaomi Encrypted Communication packets during Session.
* The *session_comm_decrypt.py* script, that implements the decryption of Xiaomi Encrypted Communication during Session.
* The *session_comm_encrypt_test.py* script, that encrypts Xiaomi Encrypted Communication packets exchanged during Session.
* The *session_comm_encrypt.py* script, that implements the encryption of Xiaomi Encrypted Communication during Session.

### Prerequisites
* A machine with Python.
* Some sample Xiaomi P4 packets (already provided inside the scripts when necessary).
* The correct secrets related to the P4 packets to test (already provided inside the scripts when necessary).

## Setup
* Run the following command from this folder <code>pip3 install -r requirements.txt</code>.

### Execution
* Run the following command <code>make</code>.
* All tests will be run sequentially.
* Observe the outcome of the tests, which should all pass.