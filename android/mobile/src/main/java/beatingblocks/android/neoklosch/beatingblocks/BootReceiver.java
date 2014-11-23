package beatingblocks.android.neoklosch.beatingblocks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by neoklosch on 23.11.14.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(BeatService.class.getName());
        context.startService(serviceIntent);
    }
}
