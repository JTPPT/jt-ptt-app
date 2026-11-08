package pe.net.jtelecomperu.jtptt;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import androidx.core.content.ContextCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

@CapacitorPlugin(name = "JTAudio", permissions = {
    @Permission(alias = "mic", strings = { Manifest.permission.RECORD_AUDIO }),
    @Permission(alias = "notif", strings = { "android.permission.POST_NOTIFICATIONS" })
})
public class JTAudioPlugin extends Plugin {

    private AudioManager am() {
        return (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    /** Altavoz forzado, modo comunicación (como un radio). */
    @PluginMethod
    public void speaker(PluginCall call) {
        boolean on = call.getBoolean("on", true);
        AudioManager a = am();
        if (on) {
            a.setMode(AudioManager.MODE_IN_COMMUNICATION);
            a.setSpeakerphoneOn(true);
            int max = a.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            a.setStreamVolume(AudioManager.STREAM_VOICE_CALL, max, 0);
        } else {
            a.setSpeakerphoneOn(false);
            a.setMode(AudioManager.MODE_NORMAL);
        }
        JSObject r = new JSObject();
        r.put("speaker", a.isSpeakerphoneOn());
        call.resolve(r);
    }

    /** Servicio en primer plano: mantiene la app viva en segundo plano. */
    @PluginMethod
    public void start(PluginCall call) {
        if (getPermissionState("mic") != com.getcapacitor.PermissionState.GRANTED
            || (Build.VERSION.SDK_INT >= 33 && getPermissionState("notif") != com.getcapacitor.PermissionState.GRANTED)) {
            requestAllPermissions(call, "afterPerms");
            return;
        }
        doStart(call);
    }

    @com.getcapacitor.annotation.PermissionCallback
    private void afterPerms(PluginCall call) {
        doStart(call);
    }

    private void doStart(PluginCall call) {
        try {
            Intent i = new Intent(getContext(), PttService.class);
            ContextCompat.startForegroundService(getContext(), i);
            call.resolve();
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void stop(PluginCall call) {
        getContext().stopService(new Intent(getContext(), PttService.class));
        call.resolve();
    }
}
