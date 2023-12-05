package com.fss.fsswebrtc_01

//import org.webrtc.PeerConnectionFactory;
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.webrtc_fssxiot.utils.AppUtils
import com.fss.fsswebrtc_01.databinding.ActivityMainBinding
import com.fss.fsswebrtc_01.utils.PermissionUtils
import com.fss.fsswebrtc_01.webRTCModules.CallbackSdpObserver
import com.fss.fsswebrtc_01.webRTCModules.PeerConnectionObserver
import com.fss.fsswebrtc_01.webRTCModules.PeerConnectionUtils
import com.fss.fsswebrtc_01.webRTCModules.SignalingCommand
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack
import java.nio.ByteBuffer
import java.nio.CharBuffer


var mId = AppUtils.getRandomString(10)
private var SIGNALING_URL_HIEU = "ws://1.52.246.108:8000/" + mId
private var SIGNALING_URL_BINH = "ws://42.116.138.38:8089/" + mId
private var SIGNALING_URL_BINH_LOCAL = "ws://172.24.5.134:8089/" + mId

class MainActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private val ENV = Env.BINH
    private lateinit var mSignalingWebsocket: WebSocket
    val eglBaseContext = EglBase.create().eglBaseContext
    lateinit private var peerConnectionUtils: PeerConnectionUtils
    var peerConnection: PeerConnection? = null
    var mOffer = ""
    var mediaStream: MediaStream? = null
    lateinit var datachannel: DataChannel
    lateinit private var peerConnectionFactory: PeerConnectionFactory
    private val sessionManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mediaConstraints = MediaConstraints().apply {
        /*    mandatory.add(
                MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
            )*/
        /*        mandatory.add(
                  MediaConstraints.KeyValuePair(
                      "OfferToReceiveAudio", "true")
              )*/

        optional.add(
            MediaConstraints.KeyValuePair(
                "audio", "true"
            )
        )
        optional.add(
            MediaConstraints.KeyValuePair(
                "video", "true"
            )
        )
    }

    private var _binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding?.root)
        if (!PermissionUtils.permissionCheck(this, PermissionUtils.PERMISSIONS_MICROPHONE)) {
            //TODO
        } else {
            PermissionUtils.permissionCheck(this, PermissionUtils.PERMISSIONS_MICROPHONE, 10001)
        }

        peerConnectionUtils = PeerConnectionUtils(ENV, this, eglBaseContext)
        peerConnectionFactory = peerConnectionUtils.peerConnectionFactory

        initializeSignalingWebSocket()

        _binding?.btnStart?.setOnClickListener {
            sendAnswer()
        }

        with(_binding?.remoteView) {
            this?.init(eglBaseContext, null)
            this?.setEnableHardwareScaler(true)
        }
    }

    fun initializeSignalingWebSocket() {
        val client = OkHttpClient()
        val request = Request
            .Builder()
            .url(if (ENV == Env.HIEU) SIGNALING_URL_HIEU else if (ENV == Env.BINH_LOCAL) SIGNALING_URL_BINH_LOCAL else SIGNALING_URL_BINH)
            .build()
        mSignalingWebsocket = client.newWebSocket(request, signalingWebsocketListener)
    }

    fun sendStreamRequest() {
        var a = ""
        if (ENV == Env.HIEU) {
            a = "{\n" +
                    "  \"id\": \"server\",\n" +
                    "  \"type\": \"streamRequest\",\n" +
                    "  \"receiver\": \"${mId}\"\n" +
                    "}"
        } else {
            Log.d(TAG, ">> BINH")
            a = "{\n" +
                    "  \"id\": \"server\",\n" +
                    "  \"type\": \"request\"\n" +
                    "}"
        }
        mSignalingWebsocket.send(a)
    }

    private fun sendAnswerSignaling(text: String) {
        var a = "{\n" +
                "  \"id\": \"server\",\n" +
                "  \"type\": \"answer\",\n" +
                "  \"sdp\": ${text} \n" +
                "}"
        Log.d(TAG, ">> send ---- ${a}")
        mSignalingWebsocket.send(a)
    }

    val signalingWebsocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            sendStreamRequest()
            Log.d(TAG, ">>onOpen")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            val jsonObject = JSONObject(text)
            val type = jsonObject.getString("type")
            if (type.startsWith(SignalingCommand.OFFER.toString(), true)) {
                handleOffer(text)
            }
            Log.d(TAG, ">>onMessage ${text}")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.d(TAG, ">>onClosed")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            Log.d(TAG, ">>onClosing ${code} ${reason}")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            Log.d(TAG, ">>onFailure ${t} ${Gson().toJson(response)}")
        }
    }

    fun handleOffer(offer: String) {
        val jsonObject = JSONObject(offer)
        val mes = jsonObject.getString("sdp")
        mOffer = mes
        runOnUiThread {
            _binding?.btnStart?.isEnabled = true
        }
    }

    private fun sendAnswer() {
        peerConnection = createNewPeerConnection()
        val sessionDescription = SessionDescription(SessionDescription.Type.OFFER, mOffer)
        peerConnection?.setRemoteDescription(
            CallbackSdpObserver(
                onSuccess = {
                    Log.d(TAG, ">> setRemoteDescription >> onSuccess ")
                },
                onFailure = {
                    Log.d(TAG, ">> setRemoteDescription >> failure")
                }
            ),
            sessionDescription
        )
        peerConnection?.createAnswer(
            CallbackSdpObserver(
                onCreate = { answer ->
                    peerConnection?.setLocalDescription(
                        CallbackSdpObserver(
                            onSuccess = {
                            },
                            onFailure = {

                            }
                        ),
                        answer
                    )
                },
                onFailure = {

                }
            ), mediaConstraints
        )
    }

    private fun createNewPeerConnection(): PeerConnection? {
        var pc = peerConnectionFactory.createPeerConnection(
            peerConnectionUtils.rtcConfig,
            PeerConnectionObserver(
                onIceCandidateCallback = { iceCandidate ->
                },
                onIceGatheringChangeCallback = {
                    if (it == PeerConnection.IceGatheringState.COMPLETE) {
                        val gson = GsonBuilder().disableHtmlEscaping().create()
                        val answer = gson.toJson(peerConnection?.localDescription?.description)
                        sendAnswerSignaling(answer)
                    }
                },
                onTrackCallback = { rtpTransceiver ->

                },

                onAddStreamCallback = {
                    mediaStream = it
                    mediaStream?.videoTracks?.get(0)?.addSink(_binding?.remoteView)

                },
                onDataChannelCallBack = {
                    datachannel = it!!
                    datachannel?.registerObserver(object : DataChannel.Observer {

                        override fun onBufferedAmountChange(p0: Long) {
                            //Log.d(TAG,">>onBufferedAmountChange ${p0}")
                        }

                        override fun onStateChange() {
                            Log.d(TAG, ">>onStateChange ${datachannel.state()}")
                        }

                        override fun onMessage(p0: DataChannel.Buffer?) {
                            val data: ByteBuffer = p0?.data!!
                            val bytes = ByteArray(data.capacity())
                            data[bytes]

                            Handler(Looper.getMainLooper()).postDelayed({
                                val mes = "Pong " + System.currentTimeMillis()
                                val buf: DataChannel.Buffer =
                                    DataChannel.Buffer(stringIntoByteBuffer(mes), false)
                                //Log.d(TAG,">> ${mes}")
                                datachannel.send(buf)
                            }, 2000)

                        }
                    })
                }
            )
        )

        return pc
    }

    fun stringIntoByteBuffer(mes: String): ByteBuffer {
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(50)
        val charBuffer: CharBuffer = byteBuffer.asCharBuffer()
        charBuffer.put(mes)
        return byteBuffer
    }

    override fun onStop() {
        super.onStop()
        peerConnection?.close()
        datachannel.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaStream?.videoTracks?.get(0)?.removeSink(_binding?.remoteView)
        peerConnection?.close()
        datachannel.close()
    }
}

enum class Env {
    BINH,
    BINH_LOCAL,
    HIEU
}