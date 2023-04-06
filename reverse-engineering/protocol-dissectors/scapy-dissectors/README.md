# Scapy Dissectors

## Prerequisites
* A machine with Python.
* Some sample Xiaomi packets to dissect (already provided inside the *test-scapy-dissectors.py* file).

## Setup
* Run the following command from this folder <code>pip3 install -r requirements.txt</code>.

## Execution
* Run the script using the following command <code>python3 test-scapy-dissectors.py</code>.
* The Scapy command line will open.
* Input one of the following commands to dissect the corresponding Xiaomi packet:
	- P1: <code>p1write</code>, <code>p1notify</code>.
	- P2: <code>p2write</code>, <code>p2notify</code>.
	- P3: <code>p3pairreqdec</code>, <code>p3paircontdec</code>, <code>p3pairblekeydec</code>, <code>p3pairenddec</code>, <code>p3sessreqdec</code>, <code>p3sessenddec</code>.
	- P4: <code>p4encrypted</code>, <code>p4pairchalpart1</code>, <code>p4pairchalpart2</code>, <code>p4pairpubkeypart1</code>, <code>p4pairpubkeypart2</code>, <code>p4pairpubkeypart3</code>, <code>p4pairpubkeypart4</code>, <code>p4sesschalpart1</code>, <code>p4sesschalpart2</code>.
	- Advertisement: <code>advertisementpairoff</code>, <code>advertisementpairon</code>.
* Observe the output, showing the fields of the dissected Xiaomi packet.
* Write <code>exit()</code>, or press *Ctrl-D* to close the command line.