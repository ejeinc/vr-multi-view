package com.eje_c.vrmultiview.gearvr;

import android.media.MediaPlayer;
import android.support.annotation.StringRes;
import android.widget.TextView;

import org.meganekkovr.Entity;
import org.meganekkovr.Scene;
import org.meganekkovr.SurfaceRendererComponent;

import java.io.IOException;

public class PlayerScene extends Scene {
    private Entity text, waiting, player;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String path;

    @Override
    public void init() {
        super.init();
        text = findById(R.id.text);
        waiting = findById(R.id.waiting);
        player = findById(R.id.player);

        // Prepare for video rendering
        SurfaceRendererComponent surfaceRenderer = new SurfaceRendererComponent();
        surfaceRenderer.setContinuousUpdate(true);
        mediaPlayer.setSurface(surfaceRenderer.getSurface());
        player.add(surfaceRenderer);
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
        SurfaceRendererComponent.StereoMode stereoMode = mediaPlayer.getVideoWidth() == mediaPlayer.getVideoHeight()
                ? SurfaceRendererComponent.StereoMode.TOP_BOTTOM
                : SurfaceRendererComponent.StereoMode.NORMAL;
        getApp().runOnGlThread(() -> player.getComponent(SurfaceRendererComponent.class).setStereoMode(stereoMode));
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
    }

    public void setText(String text) {
        TextView view = (TextView) this.text.view().findViewById(R.id.text);
        view.setText(text);
    }
}
