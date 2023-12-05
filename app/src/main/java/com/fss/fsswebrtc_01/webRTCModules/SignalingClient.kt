package com.fss.fsswebrtc_01.webRTCModules

import android.util.Log
import com.example.webrtc_fssxiot.utils.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject

//private const val SIGNALING_URL = "ws://192.168.14.20:8080/rtc" // use local ip for devices in local network
//private const val SIGNALING_URL = "ws://10.0.2.2:8080/rtc" // for emulator
//private const val SIGNALING_URL = "ws://1.52.246.108:8080/rtc"
//private const val SIGNALING_URL = "ws://192.168.1.216:8080/rtc"
//private var SIGNALING_URL = "ws://1.52.246.108:8000/"+getRandomString(10) //aws


class SignalingClient {
    companion object{
        val mId = AppUtils.getRandomString(10)
        //private var SIGNALING_URL = "ws://sig.espitek.com:8089/"+ mId
        private var SIGNALING_URL = "ws://1.52.246.108:8000/"+mId
    }
    private val TAG = javaClass.simpleName
    private val signalingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val client = OkHttpClient()
    private val request = Request
        .Builder()
        .url(SIGNALING_URL)
        .build()

    private var mSignalingClientCallback: SignalingClientCallback? = null
    private var mOffer = ""

    // opening web socket with signaling server
    private val ws = client.newWebSocket(request, SignalingWebSocketListener())


    private inner class SignalingWebSocketListener : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG,">>onMessage ${text}")
            val jsonObject = JSONObject(text)
            val type = jsonObject.getString("type")
            when{
                type.startsWith(SignalingCommand.STATE.toString(), true) ->
                    handleStateMessage(text)
                type.startsWith(SignalingCommand.OFFER.toString(), true) ->
                    handleSignalingCommand(SignalingCommand.OFFER, text)
                type.startsWith(SignalingCommand.ANSWER.toString(), true) ->
                    handleSignalingCommand(SignalingCommand.ANSWER, text)
                type.startsWith(SignalingCommand.ICE.toString(), true) ->
                    handleSignalingCommand(SignalingCommand.ICE, text)
                else->{
                    Log.d(TAG,">>onMessage else ${text}")
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            Log.d(TAG,">>onFailure $")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            Log.d(TAG,">>onClosing ${code} -- ${reason}")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.d(TAG,">>onClosed ${code} -- ${reason}")
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            Log.d(TAG,">>onOpen ${SIGNALING_URL}")
            sendStreamRequest(webSocket)
        }
    }

    fun sendStreamRequest(webSocket: WebSocket){
        var a = "{\n" +
                "  \"id\": \"server\",\n" +
                "  \"type\": \"streamRequest\",\n" +
                "  \"receiver\": \"${mId}\"\n" +
                "}"
        /*var a = "{\n" +
                "  \"id\": \"server\",\n" +
                "  \"type\": \"request\"\n" +
                "}"*/
        var res = ws.send(a)
        Log.d(TAG,">>sendStreamRequest ${a} ${res}")
    }

    fun sendAnswerSignailing(value: String){
        //var anh = "aaaa"
        var a = "{\n" +
                "  \"id\": \"server\",\n" +
                "  \"type\": \"answer\",\n" +
                "  \"sdp\": \"${value}\"\n" +
                "}"

        var res  = ws.send(a.toString())
    }

    private fun handleStateMessage(message: String) {
        Log.d(TAG,">>handleStateMessage ")
        val state = getSeparatedMessage(message)
    }

    private fun handleSignalingCommand(command: SignalingCommand, text: String) {
        //val value = getSeparatedMessage(text)
        val jsonObject = JSONObject(text)
        val type = jsonObject.getString("type")
        var mes = ""
        if (type.equals("offer")){
            mes = jsonObject.getString("sdp")
            Log.d(TAG,">>onOffer before")

            mOffer = mes
            Log.d(TAG,">> ${mes}")
        }
    }

    private fun getSeparatedMessage(text: String) = text.substringAfter(' ')

    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun setListener(signalingClientCallback:SignalingClientCallback){
        mSignalingClientCallback = signalingClientCallback
    }
}
interface SignalingClientCallback{
    fun onOffer(ans: String)
    fun onAnswer(ans: String)
}

enum class WebRTCSessionState {
    Active, // Offer and Answer messages has been sent
    Creating, // Creating session, offer has been sent
    Ready, // Both clients available and ready to initiate session
    Impossible, // We have less than two clients connected to the server
    Offline, // unable to connect signaling server
    Open
}

enum class SignalingCommand {
    REQUEST,
    STATE, // Command for WebRTCSessionState
    OFFER, // to send or receive offer
    ANSWER, // to send or receive answer
    ICE // to send and receive ice candidates
}