import pyshark
import datetime
import binascii

from Utils.decrypt_pair_initial import *
from Utils.decrypt_pair_finalize import *
from Utils.decrypt_auth import *

from descriptor import *

from Utils.utils import Utils
from Utils.constants import *

from cryptography.hazmat.primitives.asymmetric import ed25519
from cryptography.hazmat.primitives.asymmetric.utils import encode_dss_signature


def get_pkts(pcap_path: str):
    assert type(pcap_path) == str

    df = DF_ATT['TX_UUID128']+' or '+DF_ATT['RX_UUID128']+' or '+DF_ATT['XIAOMI']
    pkts = pyshark.FileCapture(pcap_path,
            display_filter=df)

    encr = ''
    decr = ''
    encr_f = ''

    device_name = bytes(str(pkts[0].bthci_acl.src_name), 'utf-8')
    print("Device Name: " + str(device_name))

    ble_key = ''
    pair_key = ''
    security = PAIR_INIT

    packet_number = 1
    for p in pkts:

        if 'btgatt_nordic_uart_tx' in p.btatt.field_names or 'btgatt_nordic_uart_rx' in p.btatt.field_names:

            if p.btatt.uuid128 == RX_UUID128:
                value = p.btatt.btgatt_nordic_uart_rx.raw_value
                if (value[:4] == '5aa5') or (value[:4] == '55ab') or (value[:4] == '55aa'):
                    encr_f = encr
                    encr = value
                else:
                    encr += value
                        
            if p.btatt.uuid128 == TX_UUID128:
                value = p.btatt.btgatt_nordic_uart_tx.raw_value
                if (value[:4] == '5aa5') or (value[:4] == '55ab') or (value[:4] == '55aa'):
                    encr_f = encr
                    encr = value
                else:
                    encr += value


            if ((value[:4] == '55ab') or (value[:4] == '55aa')) and '' != encr_f:
                print("Packet number: " + str(packet_number))
                descriptor(encr_f)
                packet_number+=1


            if (value[:4] == '5aa5') and '' != encr_f:

                if security == PAIR_INIT:
                    decr = decrypt_pair_initial(encr_f, device_name)

                    if "5aa51e" in decr and "5b" in decr and len(decr) == 74:
                        ble_key = decr[14:46]
                        security = PAIR_FINALIZE

                elif security == PAIR_FINALIZE:
                    decr = decrypt_pair_finalize(encr_f, device_name, ble_key)

                    if "5aa510" in decr and "5c" in decr and len(decr) == 46:
                        pair_key = decr[14:]

                    if "5aa500" in decr and "5c01" in decr and len(decr) == 14:
                        security = PAIRED

                elif security == PAIRED:
                    decr = decrypt_auth(encr_f, device_name, ble_key, pair_key)
                print("Packet number: " + str(packet_number))
                descriptor(decr)
                packet_number+=1

        elif 'service_uuid16' in p.btatt.field_names:
            if p.btatt.handle == '0x0016' or p.btatt.handle == '0x0013' or p.btatt.handle == '0x0011':
                if p.btatt.opcode == '0x1b' or p.btatt.opcode == '0x12':

                    print("Packet number: " + str(packet_number))
                    P4pairingandauth(p.btatt.value)
                    packet_number+=1

    pkts.close()

    return pkts


def auth(pcap_path):
    pass


if __name__ == "__main__":
    pass