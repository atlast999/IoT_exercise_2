package com.example.mqttclient

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import com.google.gson.Gson

class MainViewModel: ViewModel() {
    lateinit var scene: Scene

    val brokerUrl = MutableLiveData("tcp://broker.emqx.io:1883")
    val publishTopic = MutableLiveData<String>()
    val subscribeTopic = MutableLiveData<String>()

    val connectStatus = MutableLiveData("Chưa kết nối")
    val subscribeStatus = MutableLiveData("Chưa đăng ký")

    val temperature = MutableLiveData<String>()
    val humidity = MutableLiveData<String>()
    val light = MutableLiveData<String>()
    val sound = MutableLiveData<String>()

    val receivedMessage = MutableLiveData("")

    val gson = Gson()
    lateinit var client: MqttAndroidClient

    fun connect(){
        val clientId = MqttClient.generateClientId()
        client = MqttAndroidClient(scene.getContext(), brokerUrl.value, clientId)
        client.connect(null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                connectStatus.value = "Done kết nối"
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                connectStatus.value = "Chưa kết nối"
            }
        })
    }

    fun subscribe(){
        client.subscribe(subscribeTopic.value, 2, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                subscribeStatus.value = "Done đăng ký"
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                subscribeStatus.value = "Chưa đăng ký"
            }
        })
        client.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable?) {
                connectStatus.value = "Chưa kết nối"
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Toast.makeText(scene.getContext(), "Message received", Toast.LENGTH_LONG).show()
                Log.d("TAG", "messageArrived: ${message.toString()}")
                message?.let {
                    receivedMessage.value += it.toString()
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                connectStatus.value = "Done kết nối"
            }
        })
    }

    fun publish(){
        val deviceInfo = Device(temperature.value, humidity.value, light.value, sound.value)
        client.publish(publishTopic.value, gson.toJson(deviceInfo).toByteArray(Charsets.UTF_8), 2, false)
        Log.d("TAG", "publish: ${gson.toJson(deviceInfo)}")
    }

    override fun onCleared() {
        super.onCleared()
        client.unsubscribe(subscribeTopic.value)
        client.disconnect()
    }

}


interface Scene{
    fun getContext(): Context
}