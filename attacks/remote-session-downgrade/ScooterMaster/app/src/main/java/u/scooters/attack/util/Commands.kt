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

object Commands{
    const val MASTER_TO_M365 = 0x20
    const val MASTER_TO_BATTERY = 0x22
    const val MASTER = 0x20
    const val BLUETOOTH = 0x21
    const val BATTERY = 0x22
    const val SMARTPHONE = 0x3E
    const val COMPUTER = 0x3D

    @JvmField
    var READ = 0x01
    @JvmField
    var WRITE_WITH_RESPONSE = 0x02
    @JvmField
    var WRITE = 0x03

}
