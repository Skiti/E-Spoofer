# Proximity Malicious Pairing Attack

We implement the proximity Malicious Pairing attack as a NodeJS script. Our script utilizes the Noble library to create a fake BLE central that impersonates Mi Home, and reimplements Xiaomi application-layer Pairing protocol. Our script can lock any Xiaomi e-scooter (apart from the Mi4, which is currently untested), and change its password to prevent access to the e-scooter from Mi Home, or it can unlock an e-scooter allowing anyone nearby to physically steal it. The Malicious Pairing attack requires the e-scooter to be in Pairing Mode, in order to actually pair. The Pairing Mode can be inadvertently enabled by the owner, as a side effect of turning on/off the headlights, or by the attacker (that quickly passes by and presses the button).

This folder contains the following files:
* The *mi3c.js* script, that runs the Malicious Pairing attack and reimplements Xiaomi Pairing and Session protocols.
* The *scootercrypto.js* script, that provides cryptography.
* The *utils.js* script, that provides utilities.

## Prerequisites
* Xiaomi e-scooter running P4v1 or P4v2 (e.g., Mi 3 running the BLE157).
	- See Table 2 in our paper.
* Attacking machine with administrative privileges and Bluetooth capabilities.
	- Our instructions refer to Ubuntu distributions, but Windows and iOS should work as they are compatible with Noble.
* Installation of NodeJS on the attacking machine.

## Setup
* The legitimate owner pairs the e-scooter with the Mi Home app, then disconnects from BLE.

## Attack Execution
* (Optional) Run the following command from this folder <code>sudo node mi3c.js help</code> to see the commands available in our script.
* Press the headlights button on the Xiaomi e-scooter to enable Pairing Mode for the next ~17 seconds.
	- Run <code>sudo node mi3c.js pair</code>, to pair and generate a new Pairing Key (this is required for establishing a new Session), while the e-scooter is in Pairing Mode.
	- The attacking machine is legitimately paired, and the following commands can be issued anytime, in no particular order.
* Run <code>sudo node mi3c.js sess lock</code> to lock the e-scooter.
	- Press Ctrl-C to reset the prompt after issuing any command.
* Run <code>sudo node mi3c.js sess unlock</code> to unlock the e-scooter.
* Run <code>sudo node mi3c.js sess setpass 111111</code> to change the e-scooter password.
	- (Optional) Pair with Mi Home and verify that the password changed.

## Video demonstration

Video demonstration of the Malicious Pairing attack on a Xiaomi Mi3 e-scooter:

[![malpair](http://img.youtube.com/vi/aQbrjr5YyKk/0.jpg)](https://www.youtube.com/watch?v=aQbrjr5YyKk)
