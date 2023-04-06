# Firmware Reversing

This folder contains the following files:
* The original BLE152 firmware, running the Xiaomi P4v1 protocol that supports the unauthenticated session downgrade command.
* Our Ghidra reverse-engineering project on BLE152 firmware, with improved naming of functions and variables, memory mapping and additional comments.
* Our Yara rules that detect interesting functions related to Xiaomi e-scooters.
* The [YaraSearch.py](https://github.com/0x6d696368/ghidra_scripts/blob/master/YaraSearch.py) script for Ghidra that edits the code accordingly to our Yara rules.
