from tabulate import tabulate

def descriptor(packet: str):

    encrypted = False

    header = packet[:4]
    length = packet[4:6]
    prot = ''
    prot = protocol(header)

    source_dev = ''
    source = packet[6:8]
    source_dev = device(source)

    table = []
    headers = []

    if source_dev == "ENCRYPTED":
        encrypted = True

    if encrypted:

        iteration = packet[6:10]
        encr_payload = packet[10:-4]
        checksum = packet[-4:]

        table = [[header, length, iteration, encr_payload, checksum], [ prot, "", "pkt number", "", ""]]
        headers = ["header", "length", "iteration", "encrypted", "checksum"]

    elif header == '5aa5':

        target = packet[8:10]
        opcode = packet[10:14]
        payload = packet[14:]

        target_dev = ''
        target_dev = device(target)
        comm_name = command(opcode)

        table = [[header, length, source, target, opcode, payload], [ prot, "", source_dev, target_dev, comm_name, ""]]
        headers = ["header", "length", "source", "target", "opcode", "payload"]

    elif header == '55aa':

        opcode = packet[8:12]
        payload = packet[12:-4]
        checksum = packet[-4:]

        comm_name = command(opcode)

        table = [[header, length, source, opcode, payload, checksum], [ prot, "", source_dev, comm_name, "", ""]]
        headers = ["header", "length", "source", "opcode", "payload", "checksum"]

    print(tabulate(table, headers, tablefmt="mixed_grid"))



def P4pairingandauth(packet: str):

    table = [[packet]]
    headers = ["Pair/Auth packet"]

    print(tabulate(table, headers, tablefmt="mixed_grid"))




def protocol(header: str):

    if header == "5aa5":
        return "P3"
    if header == "55aa":
        return "P1"
    if header == "55ab":
        return "P2-P4"



def device(dev: str):

    if dev == "20":
        return "ESC IN"
    if dev == "23":
        return "ESC OUT"
    if dev == "21":
        return "BLE IN"
    if dev == "24":
        return "BLE OUT"
    if dev == "22":
        return "BMS IN"
    if dev == "25":
        return "BMS OUT"
    if dev == "3d":
        return "CAN/PC"
    if dev == "3e":
        return "BLE PHONE"
    else:
        return "ENCRYPTED"


def command(opcode: str):

    if "5b" == opcode[:2]:
        return "PAIR INIT"
    if "5c" == opcode[:2]:
        return "PAIR FIN"
    if "5d" == opcode[:2]:
        return "AUTH"
    if "01" == opcode[:2]:
        return "READ"
    if "02" == opcode[:2]:
        return "WRITE"
    if "03" == opcode[:2]:
        return "WRITE NO RESP"
    if "04" == opcode[:2]:
        return "RESPONSE"
    if "05" == opcode[:2]:
        return "RESPONSE"
    if "07" == opcode[:2]:
        return "UPDATE REQ"
    if "08" == opcode[:2]:
        return "UPDATE DATA"
    if "09" == opcode[:2]:
        return "UPDATE CHECK"
    if "0a" == opcode[:2]:
        return "RESET"
    if "0b" == opcode[:2]:
        return "UPDATE RESP"
    if "50" == opcode[:2]:
        return "CHANGE NAME"






