const EC = require("elliptic").ec;
const elliptic = require("elliptic");

class ScooterCrypto {

	constructor(curve) {
		this.curve = new EC(curve);
	}

	generateKeyPair() {
		this.keyPair = this.curve.genKeyPair();
		return this.keyPair;
	}

	restoreKeyFromPrivate(key, format) {
		return this.curve.keyFromPrivate(key, format);
	}

	restoreKeyFromPublic(key, format) {
		return this.curve.keyFromPublic(key, format);
	}

}

module.exports = { ScooterCrypto };