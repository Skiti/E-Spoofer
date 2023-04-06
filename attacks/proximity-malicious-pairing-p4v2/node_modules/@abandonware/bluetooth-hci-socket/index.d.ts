// SPDX-Licence-Indentifier: MIT
// By: Yusuf Can INCE <ycanince@gmail.com>

/// <reference types="node" />

interface Device {
    devId: number | null;
    devUp: boolean | null;
    /** USB-IF vendor ID. */
    idVendor: number | null;
    /** USB-IF product ID. */
    idProduct: number | null;
    /** Integer USB device number */
    busNumber: number | null;
    /** Integer USB device address */
    deviceAddress: number | null;
}

declare class BluetoothHciSocket extends NodeJS.EventEmitter {
    getDeviceList(): Device[];
    isDevUp(): boolean;

    start(): void;
    stop(): void;
    reset(): void;

    bindRaw(devId: number): number;
    bindUser(devId: number): number;
    bindControl(): number;

    setFilter(filter: Buffer): void;
    write(data: Buffer): void;

    on(event: "data", cb: (data: Buffer) => void): this;
    on(event: "error", cb: (error: NodeJS.ErrnoException) => void): this;
}

export = BluetoothHciSocket;
