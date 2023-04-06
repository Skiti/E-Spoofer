from dissectors import *

import glob

def test_dissectors():

    extensions = ('./*.pcap', './*.pcapng')
    files_list = []
    for ext in extensions:
        files_list.extend(glob.glob(ext))
    print(files_list)

    for ble_capture in files_list:
        print("============PCAP DESCRIPTOR============")
        pkts = get_pkts(ble_capture)
        print("============END PCAP DESCRIPTOR============")

if __name__ == "__main__":

    test_dissectors()