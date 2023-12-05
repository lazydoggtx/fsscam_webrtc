package com.fss.fsswebrtc_01.webRTCModules

import android.util.Log
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver

/**
 * [PeerConnection.Observer] implementation with default callbacks and ability to override them
 * NOTE: This class is not mandatory but simplifies work with WebRTC.
 */
class PeerConnectionObserver(
    private val onIceCandidateCallback: (IceCandidate) -> Unit = {},
    private val onIceGatheringChangeCallback: (PeerConnection.IceGatheringState?) -> Unit = {},
    private val onTrackCallback: (RtpTransceiver?) -> Unit = {},
    private val onAddStreamCallback: (MediaStream?) -> Unit = {},
    private val onDataChannelCallBack: (DataChannel?) -> Unit = {}

) : PeerConnection.Observer {
    private val TAG = javaClass.simpleName
    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.d(TAG,">>onSignalingChange ${p0}")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.d(TAG,">>onIceConnectionChange ${p0}")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        onIceGatheringChangeCallback(p0)
    }

    // called when LocalIceCandidate received
    override fun onIceCandidate(iceCandidate: IceCandidate?) {
        iceCandidate ?: return
        onIceCandidateCallback(iceCandidate)
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
    }

    override fun onAddStream(mediaStream: MediaStream?) {
        onAddStreamCallback(mediaStream)
    }

    override fun onRemoveStream(p0: MediaStream?) {
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.d(TAG,">>onDataChannel ${p0?.state()}")
        onDataChannelCallBack(p0)
    }

    override fun onRenegotiationNeeded() {
        Log.d(TAG,">>onRenegotiationNeeded")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.d(TAG,">>onAddTrack")
    }

    // called when the remote track received
    override fun onTrack(transceiver: RtpTransceiver?) {
        super.onTrack(transceiver)
        onTrackCallback(transceiver)
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        super.onConnectionChange(newState)
        Log.d(TAG,">>onConnectionChange ${newState}")
    }
}