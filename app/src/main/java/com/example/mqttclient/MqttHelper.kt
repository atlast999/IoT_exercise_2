package com.example.mqttclient

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MqttHelper(context: Context?, topic: String) {
    private val TAG = "MqttHelper"
    var mqttAndroidClient: MqttAndroidClient?
//    val serverUri = "tcp://broker.emqx.io:1883"
    private val serverUri = "tcp://10.1.17.45:1883"
    private val clientId: String = MqttClient.generateClientId()
    var topic: String? = null
    private val username = "mqttuser"
    private val password = "960c3dac4fa81b420477942876b9c4fb1a255667a9dbe389d"

    init {
        mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
        connect(topic)
    }

    private fun connect(topic: String) {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false
//        mqttConnectOptions.userName = username
//        mqttConnectOptions.password = password.toCharArray()
        try {
            mqttAndroidClient!!.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient!!.setBufferOpts(disconnectedBufferOptions)
                    subscribeToTopic(topic)
                    Log.d(TAG, "onSuccess: connection")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.d(TAG, "onFailure: connection")
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    private fun subscribeToTopic(topic: String?) {
        try {
            this.topic = topic
            mqttAndroidClient!!.subscribe(topic, 2, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
//                    Timber.tag("Mqtt").w("Subscribed with topic %s", topic)
                    Log.d(TAG, "onSuccess: subscription")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
//                    Timber.tag("Mqtt").w("Subscribed fail with topic %s", topic)
                    Log.d(TAG, "onFailure: subscription")
                }
            })
        } catch (ex: MqttException) {
            System.err.println("Exceptionst subscribing")
            ex.printStackTrace()
        }
    }

    fun setCallback(callback: MqttCallbackExtended?) {
        mqttAndroidClient!!.setCallback(callback)
    }
    fun publish(topic: String, message: String){
        mqttAndroidClient?.publish(topic, MqttMessage(message.toByteArray()))
    }
    fun disconnect() {
        if (mqttAndroidClient != null && mqttAndroidClient!!.isConnected) {
            try {
                if (!topic.isNullOrEmpty()) {
                    mqttAndroidClient!!.unsubscribe(topic)
                }
                mqttAndroidClient!!.disconnect()
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

}