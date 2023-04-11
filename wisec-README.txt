Readme for reproducibility submission of paper ID [#23]

A) Source code info
Repository: https://github.com/Skiti/Espoofer
List of Programming Languages: Python, Kotlin, Javascript, NodeJS
Compiler Info: N/A
Packages/Libraries Needed: Python (cryptography, scapy, pyshark, tshark, ghidra), Kotlin (RxAndroidBle), NodeJS (@abandonware/noble, elliptic, hkdf, crypto)

B) Datasets info
Repository: N/A
Data generators: N/A

C) Hardware Info
Our setup includes a laptop, four smartphones, and three Xiaomi electric scooters using five BLE subsystem boards.
The laptop is a Dell Inspiron 15 3000 (11th Gen Intel Core i3-1115G4, 6 MB cache, 2 cores, 4 threads, up to 4.10 GHz Turbo).
The four smartphones are a rooted Pixel 2 (Android 11), a rooted Oneplus 3 (Android 9), a Realme GT (Android 12), and an iPhone 7 (iOS v15.7).
The three Xiaomi e-scooters are an M365, an Essential, and a Mi 3.
Three BLE subsystem boards are original from an M3675, an Essential and a Mi 3. Two BLE subsystem boards are a Pro 1 and Pro 2 clone bought online.

D) Experimentation Info
Please refer to the specific README.md file included in each subfolder of this repository.

E) Software License
MIT License.

F) Additional Information
Please refer to the README.md file for additional information.

Our claims:
1 - We release a script for the Malicious Pairing attack (also shown in a video demonstration, in case reviewers do not possess a Xiaomi e-scooter)
2 - We release an Android app for the Session Downgrade attack (also shown in a video demonstration, in case reviewers do not possess a Xiaomi e-scooter running P4v1)
3 - We release the Anti-Downgrade BLE patching script to fix Session Downgrade (also shown in a video demonstration, in case reviewers do not possess a Xiaomi e-scooter running P4v1)
4 - We release our reverse-engineering tools (Ghidra+Yara tools, Frida hooks, Scapy/Pyshark dissectors, testers for Xiaomi protocols), that can be tested as described in their related README.md file
5 - The Artifact is available, functional and the claims in our paper can be reproduced using our code
