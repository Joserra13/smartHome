package dte.masteriot.mdp.myiotapp2;

import static android.widget.Toast.LENGTH_SHORT;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    private WebSocket webSocket;

    TextView title;
    TextView nDetectTV;
    TextView gasTV;
    TextView temperatureTV;
    TextView humidityTV;
    Switch alarmSwitch;
    Switch windowSwitch;
    ImageView imOrganic;
    ImageView imPaper;
    ImageView imGlass;
    ImageView imPlastic;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 1000;

    final private String SUB_TOPIC = "smarthome/kitchen/bins/#";

    /* Mqtt parameters */
    final String host = "tcp://192.168.1.92:1883";
    private String clientId = "MQTTCLient1";

    MqttAndroidClient mqttAndroidClient;
    public MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nDetectTV = findViewById(R.id.textView3);
        gasTV = findViewById(R.id.textView14);
        humidityTV = findViewById(R.id.textView12);
        temperatureTV = findViewById(R.id.textView9);
        alarmSwitch = findViewById(R.id.switch1);
        windowSwitch = findViewById(R.id.switch2);
        imOrganic = findViewById(R.id.imageViewOrganic);
        imGlass = findViewById(R.id.imageViewGlass);
        imPaper = findViewById(R.id.imageViewPaper);
        imPlastic = findViewById(R.id.imageViewPlastic);

        instantiateWebSocket();
        mqttConnection();

        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                String msg = "A " + String.valueOf(isChecked);

                webSocket.send(msg);
                Toast.makeText(getApplicationContext(), "Msg sent!", LENGTH_SHORT).show();
            }
        });

        windowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                String msg = "W " + String.valueOf(isChecked);

                    webSocket.send(msg);
                    Toast.makeText(getApplicationContext(), "Msg sent!", LENGTH_SHORT).show();

            }
        });

    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);

                String msg = "R";
                webSocket.send(msg);
                //Toast.makeText(MainActivity.this, "This method is run every 10 seconds", Toast.LENGTH_SHORT).show();
            }
        }, delay);
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
    }

    private void instantiateWebSocket() {

        OkHttpClient client = new OkHttpClient();

        //replace x.x.x.x with your machine's IP Address
        Request request = new Request.Builder().url("ws://192.168.1.92:8080").build();
        SocketListener socketListener = new SocketListener(this);
        webSocket = client.newWebSocket(request, socketListener);
    }

    public void mqttConnection(){

        /* clientId, username, password */
        AiotMqttOption aiotMqttOption = new AiotMqttOption();
        if (aiotMqttOption == null) {
            Log.e("MQTTConn", "device info error");
        } else {
            clientId = aiotMqttOption.getClientId();
        }

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

        /* MqttAndroidClient */
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i("MQTTConn", "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i("MQTTConn", "topic: " + topic + ", msg: " + new String(message.getPayload()));
                //title.setText(new String(message.getPayload()));

                JSONObject json_obj = null;
                String type = "";
                int capacity = 0;

                try {

                    json_obj = new JSONObject(String.valueOf(message));
                    type = json_obj.getString("type");
                    capacity = json_obj.getInt("capacity");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                switch (type){
                    case "organic":
                        if(capacity == 0){
                            imOrganic.setImageResource(R.drawable.c0);
                        } else if(capacity == 25){
                            imOrganic.setImageResource(R.drawable.c25);
                        } else if(capacity == 50){
                            imOrganic.setImageResource(R.drawable.c50);
                        } else if(capacity == 75){
                            imOrganic.setImageResource(R.drawable.c75);
                        } else if(capacity == 100){
                            imOrganic.setImageResource(R.drawable.c100);
                        } else {
                            imOrganic.setImageResource(R.drawable.c0);
                        }
                        break;
                    case "plastic":
                        if(capacity == 0){
                            imPlastic.setImageResource(R.drawable.c0);
                        } else if(capacity == 25){
                            imPlastic.setImageResource(R.drawable.c25);
                        } else if(capacity == 50){
                            imPlastic.setImageResource(R.drawable.c50);
                        } else if(capacity == 75){
                            imPlastic.setImageResource(R.drawable.c75);
                        } else if(capacity == 100){
                            imPlastic.setImageResource(R.drawable.c100);
                        } else {
                            imPlastic.setImageResource(R.drawable.c0);
                        }
                        break;
                    case "paper":
                        if(capacity == 0){
                            imPaper.setImageResource(R.drawable.c0);
                        } else if(capacity == 25){
                            imPaper.setImageResource(R.drawable.c25);
                        } else if(capacity == 50){
                            imPaper.setImageResource(R.drawable.c50);
                        } else if(capacity == 75){
                            imPaper.setImageResource(R.drawable.c75);
                        } else if(capacity == 100){
                            imPaper.setImageResource(R.drawable.c100);
                        } else {
                            imPaper.setImageResource(R.drawable.c0);
                        }
                        break;
                    case "glass":
                        if(capacity == 0){
                            imGlass.setImageResource(R.drawable.c0);
                        } else if(capacity == 25){
                            imGlass.setImageResource(R.drawable.c25);
                        } else if(capacity == 50){
                            imGlass.setImageResource(R.drawable.c50);
                        } else if(capacity == 75){
                            imGlass.setImageResource(R.drawable.c75);
                        } else if(capacity == 100){
                            imGlass.setImageResource(R.drawable.c100);
                        } else {
                            imGlass.setImageResource(R.drawable.c0);
                        }
                        break;
                    default:
                        Toast.makeText(activity, "Unknown msg", LENGTH_SHORT).show();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i("MQTTConn", "msg delivered");
            }
        });

        /* Mqtt connection */
        try {
            mqttAndroidClient.connect(mqttConnectOptions,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("MQTTConn", "connect succeed");

                    subscribeTopic(SUB_TOPIC);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i("MQTTConn", "connect failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param topic
     */
    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("MQTTConn", "subscribed succeed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i("MQTTConn", "subscribed failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public class SocketListener extends WebSocketListener {

        public MainActivity activity;
        public SocketListener(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {

            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(activity, "Connection Established!", LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onMessage(WebSocket webSocket, final String text) {

            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    //Toast.makeText(activity, "Message received!", LENGTH_SHORT).show();

                    //title.setText(text);

                    JSONObject json_obj = null;
                    int stateAlarm = 0;
                    int nDetections = 32202;
                    int gasValue = 32202;
                    int windowIsOpen = 0;
                    double temp = 3220.2;
                    double hum = 3220.2;

                    try {
                        json_obj = new JSONObject(text);

                        stateAlarm = json_obj.getInt("stateAlarm");
                        nDetections = json_obj.getInt("nDetections");
                        gasValue = json_obj.getInt("gasValue");
                        windowIsOpen = json_obj.getInt("windowIsOpen");
                        temp = json_obj.getDouble("temp");
                        hum = json_obj.getDouble("hum");


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(stateAlarm == 0){
                        alarmSwitch.setChecked(false);
                    } else if(stateAlarm == 1){
                        alarmSwitch.setChecked(true);
                    }

                    nDetectTV.setText(String.valueOf(nDetections));

                    gasTV.setText(String.valueOf(gasValue).concat("ppm"));

                    if(windowIsOpen == 0){
                        windowSwitch.setChecked(false);

                    } else if(windowIsOpen == 1){
                        windowSwitch.setChecked(true);
                    }

                    temperatureTV.setText(String.valueOf(temp).concat("ºC"));
                    humidityTV.setText(String.valueOf(hum).concat("%"));

                }
            });
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);

            Toast.makeText(activity, "Connection Closed!", LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(WebSocket webSocket, final Throwable t, @Nullable final Response response) {
            super.onFailure(webSocket, t, response);
        }
    }


}