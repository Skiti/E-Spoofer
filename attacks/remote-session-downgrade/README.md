# Remote Session Downgrade Attack

## ScooterMaster App Overview

We implement the remote Session Downgrade attack as an Android app, called *ScooterMaster*. Our app can lock a vulnerable Xiaomi e-scooter, and change its password to prevent access to the e-scooter from Mi Home, or it can unlock an e-scooter allowing anyone nearby to physically steal it. The *ScooterMaster* app takes ~2 seconds to perform the remote Session Downgrade attack. The app detects vulnerable Xiaomi e-scooters by analyzing their advertisement (reading the e-scooter model and the protocol version). Then, the app sends the unauthenticated session downgrade command, and establishes a new Session speaking the insecure P3 Session protocol, instead of the more secure P4.

This folder contains the following files:
* The [ScooterMaster](https://github.com/Skiti/Espoofer/tree/main/attacks/remote-session-downgrade/ScooterMaster) contains the Kotlin source code for the ScooterMaster app (only avaialble for Android).
* The [scootermaster.apk](https://github.com/Skiti/Espoofer/blob/main/attacks/remote-session-downgrade/scootermaster.apk) file is the latest release of the ScooterMaster app, directly compiled from the source code provided here.

## Prerequisites
* A Xiaomi e-scooter running a vulnerable BLE firmware running P4v1 (e.g., BLE122, BLE129).
* Any smartphone that will be paired with the target Xiaomi e-scooter, posing as the legitimate owner of the e-scooter.
* An attacking Android smartphone, running the *ScooterMaster* app.

## Setup
* Pair the Xiaomi e-scooter with the smartphone belonging to the legitimate owner.
* Install the *ScooterMaster* app on the attacking Android smartphone.
  - Grant BLUETOOTH\_SCAN and BLUETOOTH\_CONNECT permissions in newer Android versions.
* Power up the target Xiaomi e-scooter (running P4v1).
* Open *ScooterMaster* app while within BLE range of the e-scooter.

## Attack Execution 

### Automatic Mode (**Recommended**)
* Press "Start Service" from the drop down menu to start the remote Session Downgrade attacks that locks the e-scooter.
* Observe that the e-scooter was locked, without performing Pairing nor Session, and no prior knowledge.

### Manual Mode
* Scan for nearby e-scooters (Scan button in MainActivity to start scanning). 
  - (Optional) Read the e-scooters' names and their security level (0 = P1, 1 = P2, 2 = P3 or P4).
* Connect to the target e-scooter by pressing on the scooter name.
* Press "Start Handler" to syncronize scooter information.
* Press "Launch Total Lock Attack" to start the remote Session Downgrade attacks that locks the e-scooter.
* Observe that the e-scooter was locked, without performing Pairing nor Session, and no prior knowledge.

## Video Demonstration

Video demonstration of the Session Downgrade attack on a Xiaomi M365 e-scooter:

[![sessdown](http://img.youtube.com/vi/pLcg4fTy9Kw/0.jpg)](https://www.youtube.com/shorts/pLcg4fTy9Kw)


