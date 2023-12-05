package com.fss.fsswebrtc_01

fun ByteArray.toHexString() : String {
    return this.joinToString("") {
        java.lang.String.format("%02x", it)
    }
}