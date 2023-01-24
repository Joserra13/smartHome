package dte.masteriot.mdp.myiotapp2;

import static android.widget.Toast.LENGTH_SHORT;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 1000;

    int i = 0;

    boolean aux = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = findViewById(R.id.textView);
        nDetectTV = findViewById(R.id.textView3);
        gasTV = findViewById(R.id.textView14);
        humidityTV = findViewById(R.id.textView12);
        temperatureTV = findViewById(R.id.textView9);
        alarmSwitch = findViewById(R.id.switch1);
        windowSwitch = findViewById(R.id.switch2);

        instantiateWebSocket();

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

                if(!aux) {
                    webSocket.send(msg);
                    Toast.makeText(getApplicationContext(), "Msg sent!", LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                title.setText(String.valueOf(i));
                i++;

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

                    title.setText(text);

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
                        aux = false;
                    } else if(windowIsOpen == 1){
                        windowSwitch.setChecked(true);
                        aux = true;
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