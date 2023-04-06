# Anti-Downgrade BLE Firmware Patching

In this folder, we release a script that fixes the unauthorized session downgrade command in the Xiaomi P4v1 Session protocol. Our script slightly alters the firmware and completely removes from the target firmware the capability of accepting the session downgrade command and any other P3 command, thus defeating the Session Downgrade attack.

## Prerequisites
* A machine with Python.
* The original BLE122 firmware (provided in this folder).

## Setup
* No setup required.

## Execution
* Run the following command from this folder <code>python3 antidowngrade-patcher.py</code>.
* Find the *BLE122-patched.bin* file as the output. 
* **Caution!** If you plan on flashing the patched firmware on your Xiaomi e-scooter, please read the following. Flashing firmware is a dangerous process, and we are **not** responsible for any damage. We tested this on our Xiaomi M365 e-scooter, and our Xiaomi Pro 1 clone BLE subsystem boards. We do not know if your e-scooter has incompatible hardware/software/formware specs, so use this patcher at your own risk. Feel free to email us, you will find our email addresses on the published paper.

## Detailed Description
Our script looks for a specific conditional statement and patches it to allow only p4 session. Hence, the patch introduces no overheads (e.g., memory, computation). Our scripts opens the binary firmware, finds the function responsible for ble packet analysis, and alters the conditional statement that accepts either p3 and p4 packets, causing it to only accept p4 packets. More specifically, it replaces the <code>cmp</code> instruction <code>5a2f</code> with <code>552f</code>. As a result, the attacker can neither downgrade p4v1 to p3, nor send any other insecure p3 command.

This folder contains the following files:
* The Python script *antidowngrade-patcher.py* that processes an original Xiaomi BLE122 firmware to fix the Session Downgrade attack.
* The original *BLE122.bin* firmware
* The original *BLE122.zip* in the archive format required to flash it with [M365DownG](https://play.google.com/store/apps/details?id=com.m365downgrade) and similar apps.
* The patched *BLE122-patched.bin* firmware, as the output of our script.

## Video Demonstration

Video demonstration of the Anti-Downgrade BLE Firmware Patching:

[![patch](http://img.youtube.com/vi/r_MYs0fc1Ak/0.jpg)](https://www.youtube.com/shorts/r_MYs0fc1Ak)
