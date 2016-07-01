package com.eje_c.vrmultiview.gearvr;

import android.media.MediaPlayer;
import android.support.annotation.StringRes;
import android.widget.TextView;

import com.eje_c.meganekko.Material;
import com.eje_c.meganekko.MeganekkoApp;
import com.eje_c.meganekko.Scene;
import com.eje_c.meganekko.SceneObject;

import java.io.IOException;

public class PlayerScene extends Scene {
    private SceneObject text, waiting, player;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String path;

    @Override
    protected void initialize(MeganekkoApp app) {
        super.initialize(app);
        text = findObjectById(R.id.text);
        waiting = findObjectById(R.id.waiting);
        player = findObjectById(R.id.player);
        player.material(Material.from(mediaPlayer));
    }

    public void start() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();

            getApp().runOnGlThread(() -> {
                waiting.setVisible(false);
                player.setVisible(true);
            });
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            getApp().runOnGlThread(() -> {
                waiting.setVisible(true);
                player.setVisible(false);
            });
        }
    }

    public void load(String path) throws IOException {

        // 二度同じパスは読み込まない
        if (this.path != null && this.path.equals(path)) return;
        this.path = path;

        mediaPlayer.reset();
        mediaPlayer.setDataSource(path);
        mediaPlayer.prepare();

        // 正方形サイズだったらステレオ
        Material.StereoMode stereoMode = mediaPlayer.getVideoWidth() == mediaPlayer.getVideoHeight()
                ? Material.StereoMode.TOP_BOTTOM
                : Material.StereoMode.NORMAL;
        getApp().runOnGlThread(() -> player.material().setStereoMode(stereoMode));
    }

    public void seekTo(int msec) {
        if (path == null) return;

        try {
            mediaPlayer.seekTo(msec);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void setVolume(float volume) {
        mediaPlayer.setVolume(volume, volume);
    }

    public void setText(@StringRes int textRes) {
        TextView view = (TextView) text.view().findViewById(R.id.text);
        view.setText(textRes);
        text.updateViewLayout(true);
    }
}
