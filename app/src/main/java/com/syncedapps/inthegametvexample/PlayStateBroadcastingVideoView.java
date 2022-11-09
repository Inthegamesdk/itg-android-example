package com.syncedapps.inthegametvexample;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class PlayStateBroadcastingVideoView extends VideoView {
    private PlayPauseListener mListener;

    public PlayStateBroadcastingVideoView(Context context) {
        super(context);
    }

    public PlayStateBroadcastingVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayStateBroadcastingVideoView(Context context, AttributeSet attrs, int theme) {
        super(context, attrs, theme);
    }

    @Override
    public void pause() {
        super.pause();
        if (mListener != null) {
            mListener.onPauseVideo();
        }
    }

    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            mListener.onPlayVideo();
        }
    }

    public void setPlayPauseListener(PlayPauseListener listener) {
        mListener = listener;
    }

    public interface PlayPauseListener {
        void onPlayVideo();

        void onPauseVideo();
    }

}