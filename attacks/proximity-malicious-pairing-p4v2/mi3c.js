// module imports
const noble = require("@abandonware/noble");
const scootercrypto = require("./scootercrypto.js");
const utils = require("./utils.js");
const HKDF = require("hkdf");
const crypto = require("crypto");
const { createCipheriv, createDecipheriv, randomBytes, createHmac, createHash } = require("crypto");
const fs = require("fs");

// characteristics
var UPNPCharacteristic;
var AVDTPCharacteristic;
var UARTRXCharacteristic;
var UARTTXCharacteristic;

// Pairing and unilateral Paired Authentication (EC+HKDF+AESCCM)
var scooterPublicKey = "04"; // specifies that an EC Key contains points X and Y
var ecdone = false;
var sc = new scootercrypto.ScooterCrypto("p256"); // if wrong curve, scooter will not accept our public key
var appKeyPair;
var scooterPublicKey;
var aesccmdone = false;
var challenge;
var pairchalldone = false;
var hexPairingKey;
var pairingkeypath = "./pairingkey.txt";
var pairdone = false;

// Session
var hexAppAuthChall;
var hexScooterAuthChall;
var authreqdone = false;
var hexAppToScooterSalt;
var hexScooterToAppSalt;
var hexScooterToAppSessionKey;
var hexAppToScooterSessionKey;
var hexScooterToAppIV;
var hexAppToScooterIV;
var hexFromScooterAuthChallSolution;

// IMPORTANT: remember to press the button just before activating the script, to set the scooter into pairing mode (advertisement changes to 305a from 3058)

if (process.argv[2] == "help") {
  console.log("Helpful list of possible commands:");
  console.log("sudo node mi3c.js pair");
  console.log("--> Pairs the machine with the e-scooter");
  console.log("sudo node mi3c.js sess lock");
  console.log("--> Starts a Session (pairing required) and locks the e-scooter");
  console.log("sudo node mi3c.js sess unlock");
  console.log("--> Starts a Session  (pairing required) and unlocks the e-scooter");
  console.log("sudo node mi3c.js sess setpass 000000");
  console.log("--> Starts a Session  (pairing required) and changes the e-scooter password");
  //console.log("sudo node mi3c.js sess reboot");
  //console.log("--> Starts a Session  (pair required) and reboots the e-scooter");
  //console.log("sudo node mi3c.js sess shutdown");
  //console.log("--> Starts a Session  (pair required) and shuts down the e-scooter");
  process.exit();
}

// start scanning when script starts
noble.on("stateChange", function(state) {
  if (state === "poweredOn") {
    noble.startScanning();
  } else {
    noble.stopScanning();
  }
});

// discover BLE devices and stop when Scooter is found
noble.on("discover", function(peripheral) {
  console.log("Found " + peripheral.address + " " + peripheral.addressType);  
  if (peripheral.advertisement.serviceData != "") {
    var servicedata = peripheral.advertisement.serviceData[0].data.toString("hex");
    console.log("Scooter Service Data: " + servicedata);
    if (servicedata.substr(0,3) == "305" && servicedata.substr(4,4) == "0e0f" && servicedata.substr(10,21) == "b255a7d9bffe08") { // this is a Xiaomi Scooter (Mi3 only?)
      if (process.argv[2] == "pair") {
        if (servicedata[3] == "8") { // Pairing mode not active, exit
          console.log("Found Xiaomi Scooter, but not in Pairing Mode");
          process.exit(0);
        } else if (servicedata[3] == "a") { // Pairing mode active, continue
          if (peripheral.advertisement.localName) { console.log(peripheral.advertisement.localName + " found in Pairing Mode"); }
          else { console.log("Scooter found in Pairing Mode"); }
          explore(peripheral);
        } else { // should never go here
          console.log("Irregular Scooter Service Data");
          process.exit(0);
        }
      } else if (process.argv[2] == "sess") {
        if (peripheral.advertisement.localName) { console.log(peripheral.advertisement.localName + " found"); }
        else { console.log("Scooter found"); }
        explore(peripheral);
      }
      noble.stopScanning();
    }
  }
});

// connect to Scooter, discover services and characteristics, subscribe to AVDTP and then start Pairing
function explore(peripheral) {
  peripheral.on("disconnect", function() {
    process.exit(0);
  });

  peripheral.connect(function(error) {
    discovery(peripheral);
    (async () => {
      await utils.sleep(500);
      subscribeManager();
    })();
    (async () => {
      await utils.sleep(1000);
      console.log("Argument passed: " + process.argv[2]);
      if (process.argv[2] == "pair") { pairing(); }
      else if (process.argv[2] == "sess") { session(); }
    })();
  });
}

// discover services and remember important characteristics
function discovery(peripheral) {
  peripheral.discoverServices(null, function(error, services) {
    console.log("\n**Discovery**");
    for (var i in services) {
      services[i].discoverCharacteristics(null, function(error, characteristics) {
        for (var j in characteristics) {
          console.log("Characteristic " + characteristics[j].uuid);
          if (characteristics[j].uuid == "10")
            UPNPCharacteristic = characteristics[j];
          else if (characteristics[j].uuid == "19")
            AVDTPCharacteristic = characteristics[j];
          else if (characteristics[j].uuid == "6e400003b5a3f393e0a9e50e24dcca9e")
            UARTRXCharacteristic = characteristics[j];
          else if (characteristics[j].uuid == "6e400002b5a3f393e0a9e50e24dcca9e")
            UARTTXCharacteristic = characteristics[j];
        }
      });
    }
  });
}

// perform Xiaomi Mi 3 Scooter Pairing and unilateral Paired Authentication using EC+HKDF+AESCCM, then save Pairing Key for later Authentication and Encrypted Communication during Session
function pairing() {
  console.log("\n**Pairing**");
  // start preliminary and skippable BLE traffic
  var opcodeBuf = Buffer.from("a4", "hex");
  UPNPCharacteristic.write(opcodeBuf, true, function(error) {
    console.log("Sent a4");
  });
  opcodeBuf = Buffer.from("a2000000", "hex");
  AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
    console.log("Sent Pairing Start a2000000");
  });
}

// perform Xiaomi Scooter Mi 3 mutual Authentication during Session 
function session() {
  console.log("\n**Session**");
  fs.readFile(pairingkeypath, "utf8", function (err, data) {
    if (err) { return console.log(err); }
    hexPairingKey = data;
    console.log("Retrieved Pairing Key: " + hexPairingKey);
  });
  ecdone = true;
  aesccmdone = true;
  pairchalldone = true;
  pairdone = true;
  (async () => {
    await utils.sleep(1000); // wait for readFile
    if (!hexPairingKey) { return console.log("Pairing Key file not found"); }
    opcodeBuf = Buffer.from("24000000", "hex");
    UPNPCharacteristic.write(opcodeBuf, true, function(error) {
      console.log("Sent 24000000");
    });
    opcodeBuf = Buffer.from("0000000b0100", "hex");
    AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
      console.log("Sent 0000000b0100");
    });
    UPNPCharacteristic.subscribe(function(error) {
      console.log("Enabled UPNP notifications");
    });
  })();
}

function subscribeManager() {
  // subsribe to AVDTP notifications
  AVDTPCharacteristic.subscribe(function(error) {
    console.log("Enabled AVDTP notifications");
  });
  // manage AVDTP characteristic
  AVDTPCharacteristic.on("data", function(data, isNotification) {
    var opcodeBuf;
    var hexdata = data.toString("hex");
    console.log("Received AVDTP notification: " + hexdata);
    if (hexdata == "000004000612") { // can be skipped
      opcodeBuf = Buffer.from("000005000612", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 000005000612");
      });
    } else if (hexdata == "0000040112121212121212121212121212121212") {
      opcodeBuf = Buffer.from("0000050112121212121212121212121212121212", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 0000050112121212121212121212121212121212");
      });
      
      UPNPCharacteristic.subscribe(function(error) {
        console.log("Enabled UPNP notifications");
      });
      // end preliminary and skippable BLE traffic
      // start Pairing
      opcodeBuf = Buffer.from("a2000000", "hex");
      UPNPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent a2000000");
      });
    } else if (hexdata == "000000000200") {
      opcodeBuf = Buffer.from("00000101", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 00000101");
      });
    // receive Challenge to send later an encrypted Challenge Solution (unilateral Paired Authentication, only App authenticates to Scooter)
    } else if (hexdata.slice(0, 4) == "0100" && !pairchalldone) {
      // remember Challenge Part1
      challenge = hexdata.slice(12);
    } else if (hexdata.slice(0, 4) == "0200" && !pairchalldone) {
      // remember Challenge Part2
      challenge += hexdata.slice(4);
      console.log("Received Challenge: " + challenge);
      pairchalldone = true;
      opcodeBuf = Buffer.from("00000100", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 00000100");
      });
      opcodeBuf = Buffer.from("15000000", "hex");
      UPNPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 15000000");
      });
      opcodeBuf = Buffer.from("000000030400", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 000000030400");
      });
    } else if (hexdata == "000000030400") {
      opcodeBuf = Buffer.from("00000101", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 00000101");
      });
        // start EC, generate KeyPair, remember App Private Key, and send App Public Key to Scooter
    } else if (hexdata == "00000101" && !ecdone) {
      appKeyPair = sc.generateKeyPair();
      var privkey = appKeyPair.getPrivate("hex").toString();
      var pubkey = appKeyPair.getPublic("hex").toString();
      console.log("App EC Private Key: " + privkey);
      console.log("App EC Public Key: " + pubkey + " length: " + pubkey.length);
      var pubkeyp1 = "0100" + pubkey.slice(2, 38);
      var pubkeyp2 = "0200" + pubkey.slice(38, 74);
      var pubkeyp3 = "0300" + pubkey.slice(74, 110);
      var pubkeyp4 = "0400" + pubkey.slice(110, 130);
      // send EC App Public Key in four parts
      opcodeBuf = Buffer.from(pubkeyp1, "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent " + pubkeyp1);
      });
      opcodeBuf = Buffer.from(pubkeyp2, "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent " + pubkeyp2);
      });
      opcodeBuf = Buffer.from(pubkeyp3, "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent " + pubkeyp3);
      });
      opcodeBuf = Buffer.from(pubkeyp4, "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent " + pubkeyp4);
      });
      console.log("Sent App EC Public Key: " + pubkey + "\n");
    } else if (hexdata.slice(0, 4) == "0100" && pairchalldone && !pairdone) { // receive EC Scooter Public Key in four parts
      scooterPublicKey = scooterPublicKey + hexdata.slice(4, 40);
    } else if (hexdata.slice(0, 4) == "0200" && pairchalldone && !pairdone) { // receive EC Scooter Public Key in four parts
      scooterPublicKey = scooterPublicKey + hexdata.slice(4, 40)
    } else if (hexdata.slice(0, 4) == "0300" && pairchalldone && !pairdone) { // receive EC Scooter Public Key in four parts
      scooterPublicKey = scooterPublicKey + hexdata.slice(4, 40);
    } else if (hexdata.slice(0, 4) == "0400" && pairchalldone && !pairdone) { // receive EC Scooter Public Key in four parts
      scooterPublicKey = scooterPublicKey + hexdata.slice(4, 24);
      console.log("Received Scooter EC Public Key: " + scooterPublicKey + "\n");
      opcodeBuf = Buffer.from("00000100", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 00000100");
      });
      opcodeBuf = Buffer.from("000000000200", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 000000000200");
      });
      ecdone = true;
    // start HKDF+AESCCM for unilateral Paired Authentication, by sending to the Scooter the encrypted Challenge Solution aka HKDF+AESCCM Ciphertext (received earlier from Scooter)
    } else if (hexdata == "00000101" && ecdone && !pairdone) {
      console.log("**Pairing HKDF**");
      var restoredScooterPublicKey = sc.restoreKeyFromPublic(scooterPublicKey, "hex");
      var sharedSecret = appKeyPair.derive(restoredScooterPublicKey.getPublic());
      var hexSharedSecret = sharedSecret.toString("hex");
      if (hexSharedSecret.length < 64) { hexSharedSecret = "0" + hexSharedSecret; } // careful, sometimes you need to add 0 to the Shared Secret?
      console.log("Shared Secret: " + hexSharedSecret);
      var salt = Buffer.from("", "hex"); // empty Salt
      var data = Buffer.from(hexSharedSecret, "hex"); // Data is the EC Shared Secret
      var hkdf = new HKDF("sha256", salt, data);
      var info = Buffer.from("mible-setup-info", "utf8"); // static Info value = mible-setup-info
      var length = 64; // fixed 64 bytes length
      hkdf.derive(info, length, function(derivedKey) {
        var hexDerivedKey = utils.bytesToHex(derivedKey); // HKDF output, important Pairing Key and AES Key
        hexPairingKey = hexDerivedKey.slice(0, 24);
        console.log("PAIRING KEY: " + hexPairingKey);
        console.log("**Pairing AES-CCM**");
        const nonce = Buffer.from("101112131415161718191a1b", "hex"); // static Nonce value, potential security issue?
        const aad = Buffer.from("devID", "utf8"); // static Aad value
        const dataBuf = Buffer.from(challenge, "hex");
        var aeskey = Buffer.from(hexDerivedKey.slice(56, 88), "hex"); // Key is the AES Key retrieved from a part of the HKDF output
        const cipher = createCipheriv("aes-128-ccm", aeskey, nonce, {
          authTagLength: 4
        });
        cipher.setAAD(aad, {
          plaintextLength: Buffer.byteLength(dataBuf)
        });
        const ciphertext = cipher.update(dataBuf);
        cipher.final();
        const tag = cipher.getAuthTag();
        var hexCiphertext = utils.bytesToHex(ciphertext);
        hexCiphertext += utils.bytesToHex(tag); // append Tag
        console.log("Ciphertext: " + hexCiphertext);
        // send encrypted Challenge Solution (Ciphertext)
        var cryptop1 = "0100" + hexCiphertext.slice(0, 36);
        var cryptop2 = "0200" + hexCiphertext.slice(36, 48);
        opcodeBuf = Buffer.from(cryptop1, "hex");
        AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
          console.log("Sent " + cryptop1);
        });
        opcodeBuf = Buffer.from(cryptop2, "hex");
        AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
          console.log("Sent " + cryptop2);
        });
        aesccmdone = true;
        console.log("Sent Encrypted AES-CCM Challenge Solution: " + hexCiphertext.slice(0, 40) + "\nwith Tag: " + utils.bytesToHex(tag));
      });
    } else if (hexdata == "00000100" && aesccmdone && !pairdone) {
      opcodeBuf = Buffer.from("13000000", "hex");
      UPNPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 13000000");
      });
    // end unilateral Paired Authentication 
    // start mutual Authentication and Session Establishment
    } else if (hexdata == "00000101" && ecdone && pairdone && !authreqdone) {
      hexAppAuthChall = "aabbccddaabbccddaabbccddaabbccdd";
      console.log("Sent App Auth Challenge: " + hexAppAuthChall); // send App Auth Challenge
      opcodeBuf = Buffer.from("0100" + hexAppAuthChall, "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 0100" + hexAppAuthChall);
      });
    } else if (hexdata == "0000000d0100") {
      opcodeBuf = Buffer.from("00000101", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 00000101"); // request Scooter Auth Challenge
      });
    } else if (hexdata.slice(0, 4) == "0100" && pairchalldone && pairdone && !authreqdone) {
      hexScooterAuthChall = hexdata.slice(4);
      console.log("Received Scooter Auth Challenge: " + hexScooterAuthChall); // receive Scooter Auth Challenge
      opcodeBuf = Buffer.from("00000100", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 00000100");
      });
      authreqdone = true;
    } else if (hexdata == "0000000c0200") {
      opcodeBuf = Buffer.from("00000101", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 00000101");
      });
    } else if (hexdata.slice(0, 4) == "0100" && pairchalldone && pairdone && authreqdone) {
      hexFromScooterAuthChallSolution = hexdata.slice(4);
      opcodeBuf = Buffer.from("00000100", "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent 00000100");
      });
    } else if (hexdata.slice(0, 4) == "0200" && pairchalldone && pairdone && authreqdone) {
      hexFromScooterAuthChallSolution += hexdata.slice(4);
      console.log("Received (Scooter) Auth Challenge Solution: " + hexFromScooterAuthChallSolution);
      console.log("**Authentication and Session Establishment HKDF**");
      hexAppToScooterSalt = hexAppAuthChall + hexScooterAuthChall; // AppToScooter Salt is AppChall.concat(ScooterChall)
      hexScooterToAppSalt = hexScooterAuthChall + hexAppAuthChall; // ScooterToApp Salt is ScooterChall.concat(AppChall)
      console.log("hexAppToScooterSalt: " + hexAppToScooterSalt);
      console.log("hexScooterToAppSalt: " + hexScooterToAppSalt);
      var data = Buffer.from(hexPairingKey, "hex"); // Data is the Pairing Key generated by EC+HKDF+AESCCM Paired Authentication
      var salt = Buffer.from(hexAppToScooterSalt, "hex"); // Salt is the AppToScooter Salt
      var hkdf = new HKDF("sha256", salt, data);
      var info = Buffer.from("mible-login-info", "utf8"); // static Info value = mible-login-info
      var length = 64; // fixed 64 bytes length
      hkdf.derive(info, length, function(derivedKey) {
        var hexDerivedKey = utils.bytesToHex(derivedKey);
        hexScooterToAppSessionKey = hexDerivedKey.slice(0, 32); // ScooterToApp Session Key
        hexAppToScooterSessionKey = hexDerivedKey.slice(32, 64); // AppToScooter Session Key
        hexScooterToAppIV = hexDerivedKey.slice(64, 72); // ScooterToApp IV
        hexAppToScooterIV = hexDerivedKey.slice(72, 80); // AppToScooter IV
        console.log("HKDF Output: " + hexDerivedKey);
        console.log("HKDF ScooterToApp Session Key: " + hexScooterToAppSessionKey);
        console.log("HKDF ScooterToApp IV: " + hexScooterToAppIV);
        console.log("HKDF AppToScooter Session Key: " + hexAppToScooterSessionKey);
        console.log("HKDF AppToScooter IV: " + hexAppToScooterIV);
        console.log("**Authentication ScooterToApp HMACSHA256**");
        var hmackey = Buffer.from(hexScooterToAppSessionKey, "hex"); // Key is the ScooterToApp Session Key 
        const Hmac = createHmac("sha256", hmackey);
        var data = Buffer.from(hexScooterToAppSalt, "hex"); // Data is the ScooterToApp Salt
        Hmac.update(data);
        var expectedFromScooterAuthChallSolution = Hmac.digest("hex");
        console.log("Expected (Scooter) Auth Challenge Solution: " + expectedFromScooterAuthChallSolution);
        console.log("Received (Scooter) Auth Challenge Solution: " + hexFromScooterAuthChallSolution);
        if (hexFromScooterAuthChallSolution == expectedFromScooterAuthChallSolution) {
          console.log("Scooter is now Authenticated to the App");
          opcodeBuf = Buffer.from("00000100", "hex");
          AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
            console.log("Sent 00000100");
          });
          opcodeBuf = Buffer.from("0000000a0200", "hex");
          AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
            console.log("Sent 0000000a0200");
          });
        } else {
          console.log("Scooter to App Authentication failed because of Solution mismatch");
        }
      });
    } else if (hexdata == "00000101" && ecdone && pairdone && authreqdone) {
      console.log("**Authentication AppToScooter HMACSHA256**");
      
      var hmackey = Buffer.from(hexAppToScooterSessionKey, "hex"); // Key is the AppToScooter Session Key 
      const Hmac = createHmac("sha256", hmackey); // Key is the ScooterToApp Session Key 
      var data = Buffer.from(hexAppToScooterSalt, "hex"); // Data is the AppToScooter Salt
      Hmac.update(data);
      var hexAppAuthChallSolution = Hmac.digest("hex");
      var solutionp1 = "0100" + hexAppAuthChallSolution.slice(0, 36);
      var solutionp2 = "0200" + hexAppAuthChallSolution.slice(36, 64);
      opcodeBuf = Buffer.from(solutionp1, "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent " + solutionp1);
      });
      opcodeBuf = Buffer.from(solutionp2, "hex");
      AVDTPCharacteristic.write(opcodeBuf, true, function(error) {
        console.log("Sent " + solutionp2);
      });
      authchalldone = true;
      console.log("Sent (App) Auth Challenge Solution: " + hexAppAuthChallSolution); // send App Auth Challenge Solution
    } else if (hexdata == "00000100" && ecdone && pairdone && authreqdone && authchalldone) {
      console.log("App should be now Authenticated to the Scooter");
    }
    // end mutual Authentication and Session Establishment
  });
  // manage UPNP characteristic
  UPNPCharacteristic.on("data", function(data, isNotification) {
    var hexdata = data.toString("hex");
    console.log("Received UPNP notification " + hexdata);
    // confirm successful Pairing and unilateral Paired Authentication
    if (hexdata == "11000000") {
      console.log("Pairing and Paired Authentication Success");
      fs.writeFile(pairingkeypath, hexPairingKey, function (err) {
        if (err) { return console.log(err); }
        console.log("Pairing Key saved in " + pairingkeypath);
        pairdone = true;
        process.exit();
      });
      //authentication();
    // confirm successful mutual Authentication and Session Establishment
    } else if (hexdata == "21000000") {
      console.log("Authentication Success");
      console.log("You can now send encrypted commands to the Scooter");
      if (process.argv[3] == "lock" || process.argv[3] == "unlock" || process.argv[3] == "setpass" || process.argv[3] == "reboot" || process.argv[3] == "shutdown") {
        communication();
      }
    } else { 
      console.log("Authentication Failed");
    }
  });
}

function communication() {
  console.log("\n**Communication**");
  UARTRXCharacteristic.subscribe(function(error) {
    console.log("Enabled UART RX notifications");
  });
  (async () => {
    await utils.sleep(1000);
    // old scooter ECDH protocol
    //var lockCommand = "20037001";
    //var setpassCommand = "20038011";
    //var size = "03"; // size of the command, for lock is 03 and for setpass is 01
    if (process.argv[3] == "lock") {
      sendEncryptedCommand(generateEncryptedCommand("2002700100", "04")); // Mi3 Lock Command Prefix
    } else if (process.argv[3] == "unlock") {
      sendEncryptedCommand(generateEncryptedCommand("2002710100", "04")); // Mi3 Unlock Command Prefix
    } else if (process.argv[3] == "setpass") {
      var setpassprefix = "200380"; // Mi3 Setpass Command Prefix
      var password = process.argv[4];
      if (password.length == 0 || password == null) {
        console.log("Blank or null password");
        process.exit(0);
      } else if (password.length != 6) {
        console.log("Warning: you are setting a non-6 digits password");
      }
      var hashedpassword = createHash("sha256").update(password).digest("hex");
      var command = generateEncryptedCommand(setpassprefix + hashedpassword, "22"); // must send the same command 5 times to work (same in the real app)
      sendEncryptedCommand(command.slice(0,40));
      sendEncryptedCommand(command.slice(40,80));
      sendEncryptedCommand(command.slice(80,100));
      sendEncryptedCommand(command.slice(0,40));
      sendEncryptedCommand(command.slice(40,80));
      sendEncryptedCommand(command.slice(80,100));
      sendEncryptedCommand(command.slice(0,40));
      sendEncryptedCommand(command.slice(40,80));
      sendEncryptedCommand(command.slice(80,100));
      sendEncryptedCommand(command.slice(0,40));
      sendEncryptedCommand(command.slice(40,80));
      sendEncryptedCommand(command.slice(80,100));
      sendEncryptedCommand(command.slice(0,40));
      sendEncryptedCommand(command.slice(40,80));
      sendEncryptedCommand(command.slice(80,100));
    } else if (process.argv[3] == "reboot") { // not working yet TODO
      sendEncryptedCommand(generateEncryptedCommand("3E20027801", "04")); // Mi3 Reboot Command Prefix
    } else if (process.argv[3] == "shutdown") { // not working yet TODO
      sendEncryptedCommand(generateEncryptedCommand("3E20027901", "04")); // Mi3 Shutdown Command Prefix
    } else {
      console.log("Weird Input, will not work");
      process.exit();
    }
  })();
}

function generateEncryptedCommand(command, size) {
  var header = "55ab"; // protocol header
  var hexCounterUART = "00000000"; // always the first UART TX command we send
  console.log("**Encrypting Command using AESCCM**");
  const nonce = Buffer.from(hexAppToScooterIV + "00000000" + hexCounterUART, "hex");
  console.log("nonce: " + utils.bytesToHex(nonce));
  const aad = Buffer.from("", "utf8"); // empty Aad value
  const dataBuf = Buffer.from(command + "aaaabbbb", "hex"); // cut off prefix 55AA + size, and concat 8 random bytes to Lock command
  console.log("data: " + command + "aaaabbbb");
  var aeskey = Buffer.from(hexAppToScooterSessionKey, "hex"); // Key is the AppToScooter Session Key
  const cipher = createCipheriv("aes-128-ccm", aeskey, nonce, {
    authTagLength: 4
  });
  cipher.setAAD(aad, {
    plaintextLength: Buffer.byteLength(dataBuf)
  });
  const ciphertext = cipher.update(dataBuf);
  cipher.final();
  const tag = cipher.getAuthTag();
  var hexEncryptedCommand = utils.bytesToHex(ciphertext);
  hexEncryptedCommand += utils.bytesToHex(tag); // append Tag
  console.log("hexEncryptedCommand: " + hexEncryptedCommand);
  var hexPayload = size + hexCounterUART.slice(0, 4) + hexEncryptedCommand;
  var checksum = utils.generateChecksum(hexPayload);
  checksum = checksum.slice(2, 4) + checksum.slice(0, 2); // reverse endianess
  console.log("hexAppToScooterSessionKey: " + hexAppToScooterSessionKey);
  console.log("hexAppToScooterIV: " + hexAppToScooterIV);
  console.log("hexPayload: " + hexPayload);
  console.log("checksum: " + checksum);
  var encryptedCommand = header + hexPayload + checksum;
  console.log("Created Encrypted Command: " + encryptedCommand);
  return encryptedCommand;
}

function sendEncryptedCommand(encryptedcmd) {
  var opcodeBuf = Buffer.from(encryptedcmd, "hex");
  UARTTXCharacteristic.write(opcodeBuf, true, function(error) {
    console.log("Sent " + encryptedcmd);
  });
}

// Wireshark Filter: Scooter Advertisement (Unpairable || Pairable) and BTATT
// (bthci_evt.bd_addr == fe:bf:d9:a7:55:b2 && (btcommon.eir_ad.entry.service_data[0:2] == 30:58 || btcommon.eir_ad.entry.service_data[0:2] == 30:5a)) || btatt