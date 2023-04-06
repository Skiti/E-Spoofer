const scootercrypto = require("./scootercrypto.js");
const utils = require("./utils.js");
const HKDF = require("hkdf");
const crypto = require("crypto");
const { createCipheriv, createDecipheriv, randomBytes } = require("crypto");
/*
var sc = new scootercrypto.ScooterCrypto("p256");

var privkey = Buffer.from("00e0777fb06c8670cf87bdcfc9133307984685637b4f181024df6018c73ac3c86b", "hex"); // insert here
var appKeyPair = sc.restoreKeyFromPrivate(privkey, "hex");

var appPublicKey = appKeyPair.getPrivate("hex").toString();
var appPrivateKey = appKeyPair.getPublic("hex").toString();
console.log("App EC Public Key: " + appPublicKey);
console.log("App EC Private Key: " + appPrivateKey);

var scooterPublicKey = "045360fd8fd842baf52751b341de2d454894a773b716ccf84fda96e4beeaa77df07b7522c3481fbf672534d61d4a7a7f7fa723cffde5778ad11bde465d755e3cde"; // insert here
var restoredScooterPublicKey = sc.restoreKeyFromPublic(scooterPublicKey, "hex");
var sharedSecret = appKeyPair.derive(restoredScooterPublicKey.getPublic());

var hexSharedSecret = "0" + sharedSecret.toString("hex");
console.log("Hex Shared Secret: " + hexSharedSecret);
var salt = Buffer.from("", "hex");
var ss = Buffer.from(hexSharedSecret, "hex");
var info = Buffer.from("mible-setup-info", "utf8");

var hkdf = new HKDF("sha256", "", ss);
hkdf.derive(info, 64, function(derivedKey) {
  var hexDerivedKey = utils.bytesToHex(derivedKey);
  var token = hexDerivedKey.slice(0, 24);
  var bindkey = hexDerivedKey.slice(24, 56);
  var aeskey = hexDerivedKey.slice(56, 88);
  console.log("Hex HKDF Derived Key: " + hexDerivedKey);
  
  //console.log("Token: " + token);
  //console.log("Bind Key: " + bindkey);
  console.log("AES Key: " + aeskey);

  const nonce = Buffer.from("101112131415161718191a1b", "hex");
  const aad = Buffer.from("devID", "utf8");
  const plaintext = Buffer.from("00626c742e342e316230646c726a3638676b3030", "hex");

  console.log("nonce: " + utils.bytesToHex(nonce));
  console.log("aad: " + utils.bytesToHex(aad));
  console.log("plaintext: " + plaintext);
  console.log("plaintext: " + utils.bytesToHex(plaintext));

  const cipher = createCipheriv("aes-128-ccm", Buffer.from(aeskey, "hex"), nonce, {
    authTagLength: 4
  });
  cipher.setAAD(aad, {
    plaintextLength: Buffer.byteLength(plaintext)
  });
  var ciphertext = cipher.update(plaintext);
  cipher.final();
  const tag = cipher.getAuthTag();

  var hexCiphertext = utils.bytesToHex(ciphertext);
  var hexTag = utils.bytesToHex(tag);

  console.log("Ciphertext: " + hexCiphertext);
  console.log("Tag: " + hexTag);
});
*/

console.log("Checksum: " + utils.generateChecksum("062700ec53e143e8b1ae63ae000041b3f56a"));