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
                }
            }
        }
    }

    private double toDegree(float value) {
        return value * 9.183673469;
    }

    private void connectToServer() {
        try {
            mSocket = IO.socket("http://localhost");
        } catch(URISyntaxException urise) {
            Log.e(LOG_TAG, "wrong uri", urise);
        }
        if (mSocket == null) {
            return;
        }
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                mSocket.emit("foo", "hi");
                mSocket.disconnect();
            }

        }).on("event", new Emitter.Listener() {

            @Override
            public void call(Object... args) {}

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {}

        });
        mSocket.connect();
    }
}
