package beatingblocks.android.neoklosch.beatingblocks;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by neoklosch on 23.11.14.
 */
public class BeatService extends WearableListenerService {
    private static final String LOG_TAG = "BeatService";

    private static final String HEART_BEAT_PATH = "/heartbeat";
    private static final String GYRO_PATH = "/gyro";
    private static final String BEAT_KEY = "beat";
    private static final String AXIS_X_KEY = "axisx";
    private static final String AXIS_Y_KEY = "axisy";
    private static final String AXIS_Z_KEY = "axisz";

    private Socket mSocket;

    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        connectToServer();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        if(!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(LOG_TAG, "DataLayerListenerService failed to connect to GoogleApiClient.");
                return;
            }
        }

        for (DataEvent event : events) {
            String path = event.getDataItem().getUri().getPath();
            if (HEART_BEAT_PATH.equals(path)) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    String beat = dataMap.getString(BEAT_KEY);
                    Log.v(LOG_TAG, "beat: " + beat);
                    if (mSocket != null) {
                        mSocket.emit("heartbeat", "hi");
                    }

                }
            } else if (GYRO_PATH.equals(path)) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    float axisX = dataMap.getFloat(AXIS_X_KEY);
                    float axisY = dataMap.getFloat(AXIS_Y_KEY);
                    float axisZ = dataMap.getFloat(AXIS_Z_KEY);
                    Log.v(LOG_TAG, "axisX: " + toDegree(axisX));
                    Log.v(LOG_TAG, "axisY: " + toDegree(axisY));
                    Log.v(LOG_TAG, "axisZ: " + toDegree(axisZ));
                    JSONObject coordinates = new JSONObject();
                    try {
                        coordinates.put(AXIS_X_KEY, axisX);
                        coordinates.put(AXIS_Y_KEY, axisY);
                        coordinates.put(AXIS_Z_KEY, axisZ);

                    } catch (JSONException jsone) {
                        Log.e(LOG_TAG, jsone.getMessage(), jsone);
                    }
                    if (mSocket != null) {
                        mSocket.emit("deviceorientation", coordinates.toString());
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        mSocket.disconnect();
        super.onDestroy();
    }

    private double toDegree(float value) {
        return value * 9.183673469;
    }

    private void connectToServer() {
        try {
            Log.v(LOG_TAG, "trying to connect to nodejs server");
            mSocket = IO.socket("http://192.168.43.57:8888/controller");
        } catch(URISyntaxException urise) {
            Log.e(LOG_TAG, "wrong uri", urise);
        }
        if (mSocket == null) {
            return;
        }
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.v(LOG_TAG, "successfully connected to node js server");
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.v(LOG_TAG, "successfully disconnected from node js server");
            }

        });
        mSocket.connect();
    }
}
