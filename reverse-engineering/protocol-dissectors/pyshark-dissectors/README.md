# Pyshark Dissectors

## Prerequisites
* A machine with Python and Wireshark.
* The [sample.pcap](https://github.com/Skiti/Espoofer/tree/main/reverse-engineering/protocol-dissectors/pyshark-dissectors/sample.pcap) file, or any other Xiaomi e-scooter BLE capture.

## Setup
* Run the following command from this folder <code>pip3 install -r requirements.txt</code>.
* (Optional) If you want to test a different BLE capture file, simply copy it in this folder and our script will automatically detect it and analyze it.

## Execution
* Run the script using the following command <code>python3 test-pyshark-dissectors.py > out.txt</code>.
* Open the output file.
* Observe the output, showing the the dissected Xiaomi packet fields automatically extracted and analyzed from all BLE capture files.

## Detailed Description

This folder contains the following files:
* The *descriptor.py* script, that defines the Xiaomi protocol structure and naming convention.
* The *dissectors.py* script, that automatically detects Xiaomi packets from a BLE capture.
* The *test-pyshark-dissectors.py* script, that analyzes all BLE captures in this folder by running the aforementioned scripts.
* The *sample.pcap* capture file, that contains a sample BLE capture from a Xiaomi M365 e-scooter useful for testing our dissectors.
* The *out.txt* file, that contains a sample output of our dissectors.
* The *Utils* folder, that contains utility libraries needed to run our dissectors.