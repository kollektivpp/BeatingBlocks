package beatingblocks.android.neoklosch.beatingblocks;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, MessageApi.MessageListener,
        NodeApi.NodeListener {
    private static final String LOG_TAG = "MainActivity";

    private static final int SENSOR_TYPE_HEARTRATE = 65562;

    private static final String HEART_BEAT_PATH = "/heartbeat";
    private static final String GYRO_PATH = "/gyro";
    private static final String BEAT_KEY = "beat";
    private static final String AXIS_X_KEY = "axisx";
    private static final String AXIS_Y_KEY = "axisy";
    private static final String AXIS_Z_KEY = "axisz";


    private TextView mHeartRate;
    private TextView mAccuracy;
    private TextView mSensorInformation;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private Sensor mGyroscope;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mHeartRate = (TextView) stub.findViewById(R.id.MainActivityHeartRate);
                mAccuracy = (TextView) stub.findViewById(R.id.MainActivityAccuracy);
                mSensorInformation = (TextView) stub.findViewById(R.id.MainActivitySensorInformation);
            }
        });

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE_HEARTRATE);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mHeartRateSensor == null) {
            Log.e(LOG_TAG, "Watch does not support heart rate measurement");
        }
        if (mGyroscope == null) {
            Log.e(LOG_TAG, "Watch does not support gyroscope measurement");
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        if (mSensorManager != null) {
            mSensorManager.registerListener(this, this.mHeartRateSensor, 3);
            mSensorManager.registerListener(this, this.mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        PutDataRequest request = null;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(GYRO_PATH);
            putDataMapRequest.getDataMap().putFloat(AXIS_X_KEY, sensorEvent.values[0]);
            putDataMapRequest.getDataMap().putFloat(AXIS_Y_KEY, sensorEvent.values[1]);
            putDataMapRequest.getDataMap().putFloat(AXIS_Z_KEY, sensorEvent.values[2]);
            request = putDataMapRequest.asPutDataRequest();
        } else {
            mHeartRate.setText(String.valueOf(sensorEvent.values[0]));
            mAccuracy.setText("Accuracy: " + sensorEvent.accuracy);
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(HEART_BEAT_PATH);
            putDataMapRequest.getDataMap().putString(BEAT_KEY, String.valueOf(sensorEvent.values[0]));
            request = putDataMapRequest.asPutDataRequest();
        }

        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        if (request != null) {
            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            if (dataItemResult.getStatus().isSuccess()) {
                                Log.v(LOG_TAG, "successfully send data");
                            } else {
                                Log.v(LOG_TAG, "data not send");
                            }
                        }
                    });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(LOG_TAG, "accuracy changed: " + accuracy);
        mAccuracy.setText("Accuracy: " + Integer.toString(accuracy));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Nothing to do here
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        // Nothing to do here
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // Nothing to do here
    }

    @Override
    public void onPeerConnected(Node node) {
        // Nothing to do here
    }

    @Override
    public void onPeerDisconnected(Node node) {
        // Nothing to do here
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Nothing to do here
    }
}
