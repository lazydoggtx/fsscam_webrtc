package com.fss.fsswebrtc_01.webRTCModules

import android.content.Context
import android.util.Log
import com.fss.fsswebrtc_01.Env
import org.webrtc.BuiltinAudioDecoderFactoryFactory
import org.webrtc.BuiltinAudioEncoderFactoryFactory
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.JavaAudioDeviceModule

class PeerConnectionUtils(
    env : Env,
    context: Context,
    eglBaseContext: EglBase.Context
) {
    private val TAG = javaClass.simpleName
    init {
        PeerConnectionFactory.InitializationOptions
            .builder(context)
            .createInitializationOptions().also { initializationOptions ->
                PeerConnectionFactory.initialize(initializationOptions)
            }
    }

    private val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(eglBaseContext, false, true)
    private val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
    private val defaultAudioEncoderFactoryFactory = BuiltinAudioEncoderFactoryFactory()
    private val defaultAudioDecoderFactoryFactory = BuiltinAudioDecoderFactoryFactory()


    val peerConnectionFactory: PeerConnectionFactory = PeerConnectionFactory
        .builder()
        .setOptions(PeerConnectionFactory.Options().apply {
            //disableEncryption = true
            //disableNetworkMonitor = true
        })
        .setVideoDecoderFactory(defaultVideoDecoderFactory)
        .setVideoEncoderFactory(defaultVideoEncoderFactory)
        .setAudioDecoderFactoryFactory(defaultAudioDecoderFactoryFactory)
        .setAudioEncoderFactoryFactory(defaultAudioEncoderFactoryFactory)
        .setAudioDeviceModule(
            JavaAudioDeviceModule.builder(context)
                .setAudioTrackErrorCallback(object  : JavaAudioDeviceModule.AudioTrackErrorCallback{
                    override fun onWebRtcAudioTrackInitError(p0: String?) {
                        Log.d(TAG,"onWebRtcAudioTrackInitError ${p0}")
                    }

                    override fun onWebRtcAudioTrackStartError(
                        p0: JavaAudioDeviceModule.AudioTrackStartErrorCode?,
                        p1: String?
                    ) {
                        Log.d(TAG,">>onWebRtcAudioTrackStartError ${p0} ${p1}")
                    }

                    override fun onWebRtcAudioTrackError(p0: String?) {
                        Log.d(TAG,"onWebRtcAudioTrackError ${p0}")
                    }

                })
                .setAudioTrackStateCallback(object : JavaAudioDeviceModule.AudioTrackStateCallback{
                    override fun onWebRtcAudioTrackStart() {
                        Log.d(TAG,">>onWebRtcAudioTrackStart")
                    }

                    override fun onWebRtcAudioTrackStop() {
                        Log.d(TAG,">>onWebRtcAudioTrackStop")
                    }

                })
                .createAudioDeviceModule().also {
                //it.setMicrophoneMute(false)
                //it.setSpeakerMute(false)
            }

        )
        .createPeerConnectionFactory()

    val rtcConfig = PeerConnection.RTCConfiguration(
        if (env == Env.HIEU)
            arrayListOf(
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
            )
        else if (env == Env.BINH_LOCAL) {
            arrayListOf(
                PeerConnection.IceServer.builder("stun:172.24.5.134:5349").createIceServer(),
                PeerConnection.IceServer.builder("turn:172.24.5.134:5349")
                    .setUsername("wuser")
                    .setPassword("wpsk")
                    .createIceServer()
            )
        }
        else{
            arrayListOf(
                PeerConnection.IceServer.builder("stun:42.116.138.35:3478").createIceServer(),
                PeerConnection.IceServer.builder("turn:42.116.138.35:3478")
                    .setUsername("turnuser")
                    .setPassword("turn456")
                    .createIceServer()
            )
    }

    ).apply {
        // it's very important to use new unified sdp semantics PLAN_B is deprecated
        if (env == Env.HIEU){
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        }
        if (env == Env.BINH){
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        }
    }
}