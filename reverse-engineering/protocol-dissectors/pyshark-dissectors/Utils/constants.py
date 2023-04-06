 #
# Xiaomi scooters toolkit - constants.py
# 
# 

# NOTE: ATT
XIAOMI = '0xfe95'
TX_UUID128 = '6e:40:00:02:b5:a3:f3:93:e0:a9:e5:0e:24:dc:ca:9e'
RX_UUID128 = '6e:40:00:03:b5:a3:f3:93:e0:a9:e5:0e:24:dc:ca:9e'

# NOTE: display filters
# (btatt.uuid128 == 6e:40:00:02:b5:a3:f3:93:e0:a9:e5:0e:24:dc:ca:9e) 
# or (btatt.uuid128 == 6e:40:00:03:b5:a3:f3:93:e0:a9:e5:0e:24:dc:ca:9e)
DF_ATT = {
    'TX_UUID128': f'btatt.uuid128 == {TX_UUID128}',
    'RX_UUID128': f'btatt.uuid128 == {RX_UUID128}',
    'XIAOMI': f'btatt.service_uuid16 == {XIAOMI}',
} 

# NOTE: SECURITY
PAIR_INIT = 0
PAIR_FINALIZE = 1
PAIRED = 2
