# Attacks

## Overview

In our work, we perform Proximity and Remote Malicious Pairing, and Proximity and Remote Session Downgrade.
We currently automate and open-source Proximity Malicious Pairing, and Remote Session Downgrade.
We did not release yet the other two attacks (i.e., Remote Malicious Pairing, and Proximity Session Downgrade) because we have to clean our code. 
Nonetheless, we tested them and we confirmed that they work as advertised in the Evaluation section of our paper.

We implement Malicious Pairing as a NodeJS script that replicates Xiaomi Pairing P4 protocol. The attacking device must be within BLE range of the victim e-scooter.
The target e-scooter must be in Pairing Mode (i.e., someone pressed the headlight button up to 17 seconds ago). You can find our script in the [proximity-malicious-pairing-p4v2](https://github.com/Skiti/Espoofer/tree/main/attacks/proximity-malicious-pairing-p4v2) folder.

We implement Session Downgrade as an Android app written in Kotlin, using the RxJava library. We target Xiaomi P4v2 Session protocol. The app must be installed on the victim's Android smartphone, and granted BLE-related permissions. The app must run while the target e-scooter is within BLE range of the smartphone. You can find our script in the [remote-session-downgrade](https://github.com/Skiti/Espoofer/tree/main/attacks/remote-session-downgrade) folder.
