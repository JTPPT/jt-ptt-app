package pe.net.jtelecomperu.jtptt;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(JTAudioPlugin.class);
        super.onCreate(savedInstanceState);
        pedirPermisos();
    }

    private void pedirPermisos() {
        java.util.ArrayList<String> faltan = new java.util.ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            faltan.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT >= 33
            && ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED)
            faltan.add("android.permission.POST_NOTIFICATIONS");
        if (faltan.isEmpty()) arrancarAudio();
        else ActivityCompat.requestPermissions(this, faltan.toArray(new String[0]), 4711);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == 4711) arrancarAudio();
    }

    /** Altavoz forzado + servicio en primer plano (escucha en segundo plano). */
    private void arrancarAudio() {
        AudioManager a = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        a.setMode(AudioManager.MODE_IN_COMMUNICATION);
        a.setSpeakerphoneOn(true);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            try { ContextCompat.startForegroundService(this, new Intent(this, PttService.class)); } catch (Exception e) { }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AudioManager a = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (a.getMode() == AudioManager.MODE_IN_COMMUNICATION) a.setSpeakerphoneOn(true);
    }
}
