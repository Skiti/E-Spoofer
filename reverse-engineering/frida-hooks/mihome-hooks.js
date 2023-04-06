/*
frida -U -n com.xiaomi.smarthome -l mihome-hooks.js --no-pause
objection --gadget "com.xiaomi.smarthome" explore
*/

function barrToHex(barr) {
  return Array.from(barr, function(byte) {
    return ('0' + (byte & 0xFF).toString(16)).slice(-2);
  }).join('');
}

function printStackTrace() {
	var JavaThread = Java.use("java.lang.Thread");
  var th = Java.cast( JavaThread.currentThread(), JavaThread);
  var stack = th.getStackTrace(), e=null;

  for(var i=0; i<stack.length; i++){
    console.log("\t"+stack[i].getClassName()+"."+stack[i].getMethodName()+"("+stack[i].getFileName()+")");
  }
}

Java.perform(function () {

	// Pair

	var OpenSSLECPublicKey = Java.use("com.android.org.conscrypt.OpenSSLECPublicKey");
	var OpenSSLECPrivateKey = Java.use("com.android.org.conscrypt.OpenSSLECPrivateKey");
	var Key = Java.use("java.security.Key");

	var Fyp = Java.use("_m_j.fyp");

	Fyp.O000000o.overload("java.security.PublicKey").implementation = function(pubkey) {
		console.warn("\nInside Fyp.generateXYCoordinates");
		var KeyCasted = Java.cast(pubkey, OpenSSLECPublicKey);
		var EncodedKeyCasted = KeyCasted.getEncoded();
		console.log("Fyp.generateXYCoordinates App Full EC Public Key: " + barrToHex(EncodedKeyCasted));
		var retval = this.O000000o(pubkey);
		console.log("Fyp.generateXYCoordinates EC Public Key (getXY without 04): " + barrToHex(retval));
		return retval;
	};

	Fyp.O000000o.overload("java.security.PublicKey", "java.security.PrivateKey").implementation = function(pubkey, privkey) {
		console.warn("\nInside Fyp.generateSecret");
		var KeyCasted = Java.cast(pubkey, OpenSSLECPublicKey);
		var EncodedKeyCasted = KeyCasted.getEncoded();
		console.log("Fyp.generateSecret Scooter Full EC Public Key: " + barrToHex(EncodedKeyCasted));
		var KeyCasted2 = Java.cast(privkey, OpenSSLECPrivateKey);
		var EncodedKeyCasted2 = KeyCasted2.getEncoded();
		console.log("Fyp.generateSecret App EC Private Key: " + barrToHex(EncodedKeyCasted2));
		var retval = this.O000000o(pubkey, privkey);
		var KeyCasted3 = Java.cast(privkey, OpenSSLECPrivateKey);
		var EncodedKeyCasted3 = KeyCasted3.getEncoded();
		console.log("Fyp.generateSecret Returned App EC Private Key: " + barrToHex(EncodedKeyCasted3));
		return retval;
	};

	Fyp.O000000o.overload("javax.crypto.SecretKey", "boolean", "[B", "[B").implementation = function(aeskey, bool, nonce, aad) {
		console.warn("\nInside Fyp.containAndCheckAESCCM");
		var KeyCasted = Java.cast(aeskey, Key);
		var EncodedKeyCasted = KeyCasted.getEncoded();
		console.log("Fyp.containAndCheckAESCCM bool: " + bool);
		console.log("Fyp.containAndCheckAESCCM AES Key: " + barrToHex(EncodedKeyCasted));
		console.log("Fyp.containAndCheckAESCCM Nonce: " + barrToHex(nonce));
		console.log("Fyp.containAndCheckAESCCM Aad: " + aad);
		var retval = this.O000000o(aeskey, bool, nonce, aad);

		return retval;
	}

	Fyp.O000000o.overload("[B", "[B", "[B").implementation = function(aeskey, nonce, plaintext) {
		console.warn("\nInside Fys.preManageNoAadAESCCM");
		console.log("Fyp.preManageNoAadAESCCM aeskey: " + barrToHex(aeskey));
		console.log("Fyp.preManageNoAadAESCCM nonce: " + barrToHex(nonce));
		console.log("Fyp.preManageNoAadAESCCM plaintext: " + barrToHex(plaintext));
		var retval = this.O000000o(aeskey, nonce, plaintext);
		console.log("Fyp.preManageNoAadAESCCM Return Ciphertext+Authtag: " + barrToHex(retval));
		return retval;
	}

	var Fyl = Java.use("_m_j.fyl");

	Fyp.O000000o.overload("javax.crypto.SecretKey", "[B", "[B", "[B").implementation = function(aeskey, nonce, plaintext, aad) {
		console.warn("\nInside Fyp.manageAESCCM");
		var KeyCasted = Java.cast(aeskey, Key);
		var EncodedKeyCasted = KeyCasted.getEncoded();
		console.log("Fyp.manageAESCCM AES Key: " + barrToHex(EncodedKeyCasted));
		console.log("Fyp.manageAESCCM Nonce: " + barrToHex(nonce));
		console.log("Fyp.manageAESCCM Plaintext: " + barrToHex(plaintext)); // challenge (during pairing / authentication) or message (during communication)
		console.log("Fyp.manageAESCCM Aad: " + aad);
		var retval = this.O000000o(aeskey, nonce, plaintext, aad);
		var FylCasted = Java.cast(retval, Fyl);
		console.log("Fyp.manageAESCCM Returned Ciphertext: " + barrToHex(FylCasted._O000000o.value));
		console.log("Fyp.manageAESCCM Returned AuthTag: " + barrToHex(FylCasted.O00000Oo.value));
		return retval;
	};

	Fyl.O000000o.implementation = function() {
		console.warn("\nInside Fyl.mergeEncryptedCmd");
		console.log("Fyl.mergeEncryptedCmd Ciphertext: " + barrToHex(this._O000000o.value));
		console.log("Fyl.mergeEncryptedCmd AuthTag: " + barrToHex(this.O00000Oo.value));
		var retval = this.O000000o();
		console.log("Fyl.mergeEncryptedCmd Returned EncryptedCommand: " + barrToHex(retval));
		return retval;
	};

	// Auth

	var Fys = Java.use("_m_j.fys");

	Fys.O000000o.overload("[B", "[B").implementation = function(pairkey, challengeconcat) {
		console.warn("\nInside Fys.doAuthMibleLoginInfoHMACSHA256");
		console.log("Fys.doAuthMibleLoginInfoHMACSHA256 PairingKey: " + barrToHex(pairkey));
		console.log("Fys.doAuthMibleLoginInfoHMACSHA256 AppToScooterSalt: " + barrToHex(challengeconcat)); // appchall + scooterchall
		var retval = this.O000000o(pairkey, challengeconcat);
		console.log("Fys.doAuthMibleLoginInfoHMACSHA256 Return: " + barrToHex(retval));
		return retval;
	}

	var Hgd = Java.use("_m_j.hgd");

	Hgd.onCharacteristicChanged.overload("android.bluetooth.BluetoothGattCharacteristic", "[B").implementation = function(bgattcharacteristic, notification) {
		console.warn("\nInside Hgd.onCharacteristicChanged");
		console.log("Hgd.onCharacteristicChanged notification: " + barrToHex(notification));
		var retval = this.onCharacteristicChanged(bgattcharacteristic, notification);
		return retval;
	}
	
});