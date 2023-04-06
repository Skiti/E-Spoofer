// converts a hex string to a buffer
function hexToBuffer(hex) {
  for (var bytes = [], c = 0; c < hex.length; c += 2)
    bytes.push(parseInt(hex.substr(c, 2), 16));
  return bytes;
}

// converts a hex string to a byte array
function hexToBytes(hex) {
    for (var bytes = [], c = 0; c < hex.length; c += 2)
        bytes.push(parseInt(hex.substr(c, 2), 16));
    return bytes;
}

// converts a byte array to a hex string
function bytesToHex(bytes) {
    for (var hex = [], i = 0; i < bytes.length; i++) {
        var current = bytes[i] < 0 ? bytes[i] + 256 : bytes[i];
        hex.push((current >>> 4).toString(16));
        hex.push((current & 0xF).toString(16));
    }
    return hex.join("");
}

// converts a string into hex
function strToHex(txt){
    const encoder = new TextEncoder();
    return Array
        .from(encoder.encode(txt))
        .map(b => b.toString(16).padStart(2, '0'))
        .join('')
}

function decToHex(str){ // .toString(16) only works up to 2^53
    var dec = str.toString().split(''), sum = [], hex = [], i, s
    while(dec.length){
        s = 1 * dec.shift()
        for(i = 0; s || i < sum.length; i++){
            s += (sum[i] || 0) * 10
            sum[i] = s % 16
            s = (s - sum[i]) / 16
        }
    }
    while(sum.length){
        hex.push(sum.pop().toString(16))
    }
    return hex.join('')
}

// timed sleep
const sleep = ms => new Promise(res => setTimeout(res, ms));

function generateChecksum(hexdata) {
  var sum = 0;
  for ( let i=0; i<hexdata.length; i=i+2) {
    var d = parseInt(hexdata[i] + hexdata[i+1], 16);
    sum += d;
  }
  sum = ~sum; // bitwise NOT operator
  return decimalHexTwosComplement(sum);
}

function decimalHexTwosComplement(decimal) {
  var size = 4;
  if (decimal >= 0) {
    var hexadecimal = decimal.toString(16);
    while ((hexadecimal.length % size) != 0) {
      hexadecimal = "" + 0 + hexadecimal;
    }
    return hexadecimal;
  } else {
    var hexadecimal = Math.abs(decimal).toString(16);
    while ((hexadecimal.length % size) != 0) {
      hexadecimal = "" + 0 + hexadecimal;
    }
    var output = '';
    for (i = 0; i < hexadecimal.length; i++) {
      output += (0x0F - parseInt(hexadecimal[i], 16)).toString(16);
    }
    output = (0x01 + parseInt(output, 16)).toString(16);
    return output;
  }
}


module.exports = { hexToBuffer, hexToBytes, bytesToHex, strToHex, decToHex, sleep, generateChecksum };