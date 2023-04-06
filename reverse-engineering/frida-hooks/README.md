# Frida Hooks

This folder contains the [Frida](https://frida.re/) hooks targeting Mi Home. We recommend Mi Home v7.11.704 (on Android), as our hooks might not work on later releases.

## Prerequisites
* A rooted phone running [Frida server](https://frida.re/docs/android/).
* A machine running [Frida client](https://frida.re/docs/installation/).
* Our hooks.
* A Xiaomi e-scooter.

## Setup
* Connect the rooted phone to your machine.
* Setup your enviroment as described in the Frida documentation.
* Install Mi Home.
* Power up the Xiaomi e-scooter.

## Execution
* Run the following command from this folder <code>frida -U -n com.xiaomi.smarthome -l mihome-hooks.js --no-pause</code>.
* Utilize Mi Home with the Xiaomi e-scooter (Pairing, Session).
* Observe the output on the console.

## Hooks Detailed Description
* *Fyp.O000000o* generates the ECDH XY coordinates.
* *Fyp.O000000o* generates the ECDH shared secret.
* *Fyp.O000000o*, *Fyp.O000000o*, and most importantly *Fyp.O000000o* all involve AES-CCM encryption, receiving (a mix of) the Key, the Nonce, the AAD, the Plaintext, and returning the Ciphertext and the AuthTag.
* *Fyl.O000000o* creates an encrypted command from the Ciphertext and the AuthTag.
* *Fys.O000000o* solves the challenge during Session by performing HMAC-SHA256 with the Pairing Key and the AppToScooterSalt.
* *Hgd.onCharacteristicChanged* monitors all BLE notifications received from the Xiaomi e-scooter.
