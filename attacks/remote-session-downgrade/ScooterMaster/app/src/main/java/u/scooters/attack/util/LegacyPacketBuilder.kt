/** */ //	Function: M365 BLE message builder
//	Author:   Salvador Mart�n
//	Date:    12/02/2018
//
//	This library is free software; you can redistribute it and/or
//	modify it under the terms of the GNU Lesser General Public
//	License as published by the Free Software Foundation; either
//	version 2.1 of the License, or (at your option) any later version.
//
//	This library is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//	Lesser General Public License for more details.
//
//	I am not responsible of any damage caused by the misuse of this library.
//	Use at your own risk.
//
//	If you modify or use this, please don't delete my name and give me credits.
/** */
package u.scooters.attack.util

import android.util.Log
import java.util.ArrayList

class LegacyPacketBuilder {

    companion object {
        @JvmStatic var ninebot: Boolean = false
        var encryptionNinebot: NinebotProtocolEncryption = NinebotProtocolEncryption("")
    }

    private var msg: MutableList<Int>? = null
    private var direction = 0
    private var source = Commands.SMARTPHONE
    private var rw = 0
    private var position = 0
    private var payload: MutableList<Int>? = null
    private var checksum = 0

    fun setDirection(drct: Int): LegacyPacketBuilder {
        direction = drct
        checksum += direction
        return this
    }

    fun setSource(): LegacyPacketBuilder {
        source = Commands.SMARTPHONE
        checksum += source
        return this
    }

    fun setRW(readOrWrite: Int): LegacyPacketBuilder { // read or write
        rw = readOrWrite
        checksum += rw
        return this
    }

    fun setPosition(pos: Int): LegacyPacketBuilder {
        position = pos
        checksum += position
        return this
    }

    fun setPayload(bytesToSend: ByteArray): LegacyPacketBuilder {
        payload = ArrayList()
        checksum += bytesToSend.size + 2
        for (b in bytesToSend) {
            payload!!.add(b.toInt())
            checksum += b
        }
        return this
    }

    fun setPayload(bytesToSend: MutableList<Int>?): LegacyPacketBuilder {
        payload = bytesToSend
        checksum += payload!!.size + 2
        for (i in payload!!) {
            checksum += i
        }
        return this
    }

    fun setPayload(singleByteToSend: Int): LegacyPacketBuilder {
        payload = ArrayList()
        payload!!.add(singleByteToSend)
        checksum += 3
        checksum += singleByteToSend
        return this
    }

    fun build(): String {
        setupHeaders()
        setupBody()
        if (!ninebot)
            calculateChecksum()
        return construct()
    }

    private fun setupHeaders() {
        msg = ArrayList(0)
        if (!ninebot) {
            msg!!.add(0x55)
            msg!!.add(0xAA)
        }else {
            msg!!.add(0x5A)
            msg!!.add(0xA5)
        }
    }

    private fun setupBody() {
        if (!ninebot)
            msg!!.add(payload!!.size + 2)
        else
            msg!!.add(payload!!.size)
        if (ninebot)
            msg!!.add(source)
        msg!!.add(direction)
        msg!!.add(rw)
        msg!!.add(position)
        for (i in payload!!) {
            msg!!.add(i)
        }
    }

    private fun calculateChecksum() {
        checksum = checksum xor 0xffff
        msg!!.add(checksum and 0xff)
        msg!!.add(checksum shr 8)
    }

    private fun construct(): String {
        var result = ""
        for (i in msg!!) {
            result += if (i >= 0 && i <= 15) "0" + Integer.toHexString(i) else Integer.toHexString(i)
        }
        Log.e("LegacyPacketBuilder",result);
        if(ninebot)
            return HexString.bytesToHex(encryptionNinebot.encrypt(HexString.hexToBytes(result)))
        else
            return result
    }
}