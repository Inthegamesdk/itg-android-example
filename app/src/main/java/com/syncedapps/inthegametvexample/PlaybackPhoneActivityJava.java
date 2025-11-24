package com.syncedapps.inthegametvexample;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.syncedapps.inthegametv.integration.ITGExoPlayerAdapter;
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent;
import com.syncedapps.inthegametv.network.ITGEnvironment;
import com.syncedapps.inthegametvexample.databinding.ActivityPhonePlaybackBinding;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlaybackPhoneActivityJava extends FragmentActivity {

    private ExoPlayer player;
    private Long playbackPosition = 0L;
    private Boolean playWhenReady = true;
    private StyledPlayerView videoView = null;

    private ITGPlaybackComponent mITGComponent = null;
    private ITGExoPlayerAdapter mITGPlayerAdapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restorePlaybackStateIfAny(savedInstanceState);

        com.syncedapps.inthegametvexample.databinding.ActivityPhonePlaybackBinding binding = ActivityPhonePlaybackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupFullscreenMode();

        // Replace 'your_account_id' and 'your_channel_slug' with actual values
        String  accountId = "68650da0324217d506bcc2d4";
        String channelSlug = "samplechannel";

        //add video view
        videoView = buildVideoView();

        // Initialize ITGPlaybackComponent
        mITGComponent = new ITGPlaybackComponent(this);


        // Set up the ITGExoPlayerAdapter with your player view
        ITGExoPlayerAdapter adapter = new ITGExoPlayerAdapter(videoView, new WeakReference<>(null));
        mITGPlayerAdapter = adapter;


        Map<String, String> vars = new HashMap<>();
        vars.put("varName", "varValue");

        // Initialize the ITG component with necessary parameters
        mITGComponent.init(
               this, //mandatory: fragment activity instance
               adapter, //mandatory: adapter between the player and SDK

                accountId, //mandatory: your ITG accountId
                channelSlug, //mandatory: your channelId on our admin panel
                ITGEnvironment.Companion.getV2_3(), //mandatory: env
                "foreignId", //optional: user foreign id, provide null if no need
                Arrays.asList(
                        "channel1",
                        "channel2",
                        "channel3",
                        "channel4",
                        "channel5"
                ),  //optional: virtual channels, provide null if needed
                vars, //optional: runtime variables, provide new HashMap<>() if needed
                savedInstanceState,  //mandatory: saved state of the component
                false
        );


        // Add the ITG component to your view hierarchy
        binding.outerContainer.addView(mITGComponent, 0);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {

            @Override
            public void handleOnBackPressed() {
                if (mITGComponent == null || !mITGComponent.handleBackPressIfNeeded()) {
                    // Implement your own back press action here
                    finish();
                }
            }
        });
    }
    private void restorePlaybackStateIfAny(Bundle  savedInstanceState) {
        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getLong("playbackPosition", 0L);
            playWhenReady = savedInstanceState.getBoolean("playWhenReady");
        }
    }

    private void setupFullscreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Objects.requireNonNull(getWindow().getInsetsController()).hide(WindowInsets.Type.statusBars());
        } else {
            Objects.requireNonNull(getWindow()).setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
    }

    private void startVideo() {
        prepareMediaForPlaying(Uri.parse(ConstJava.videoURL));
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(0, playbackPosition);
        player.prepare();
    }

    private void prepareMediaForPlaying(Uri mediaSourceUri) {
        DefaultHttpDataSource.Factory upstreamDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true);

        DefaultDataSource.Factory defaultDataSourceFactory =
                new DefaultDataSource.Factory(this, upstreamDataSourceFactory);

        MediaSource mediaSource = null;
        if (Objects.requireNonNull(mediaSourceUri.getLastPathSegment()).endsWith(".m3u8")) {
            mediaSource = new HlsMediaSource.Factory(defaultDataSourceFactory)
                    .createMediaSource(
                            MediaItem.fromUri(mediaSourceUri)
                    );
        } else {
            mediaSource = new ProgressiveMediaSource.Factory(defaultDataSourceFactory)
                    .createMediaSource(
                            MediaItem.fromUri(mediaSourceUri)
                    );
        }
        player.setMediaSource(mediaSource);
    }

    private StyledPlayerView buildVideoView() {
        @SuppressLint("InflateParams") StyledPlayerView videoView = (StyledPlayerView) getLayoutInflater().inflate(R.layout.styled_player_view, null, false);
        videoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return videoView;
    }

    private void initializePlayer() {
        long SEEK_INCREMENT = 10_000L;
        ExoPlayer player = new ExoPlayer.Builder(this)
                .setSeekBackIncrementMs(SEEK_INCREMENT)
                .setSeekForwardIncrementMs(SEEK_INCREMENT)
                .build();

        // Notify the ITGPlayerAdapter that the player is ready
        mITGPlayerAdapter.onPlayerReady(player);

        videoView.setPlayer(player);
        this.player = player;
    }

    private void releasePlayer() {
        ExoPlayer exoPlayer = player;
        if (exoPlayer != null) {
            playbackPosition = exoPlayer.getCurrentPosition();
            playWhenReady = exoPlayer.getPlayWhenReady();
            videoView.setPlayer(null);

            // Notify the ITGPlayerAdapter that the player has been released
            mITGPlayerAdapter.onPlayerReleased();

            exoPlayer.release();
        }
        player = null;
    }

    public void onResume() {
        super.onResume();
        if ((Build.VERSION.SDK_INT <= 23 || player == null)) {
            initializePlayer();
            startVideo();
        }
    }

    public void onStop() {
        if (Build.VERSION.SDK_INT > 23) {
            releasePlayer();
        }
        super.onStop();
    }

    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("playbackPosition", playbackPosition);
        outState.putBoolean("playWhenReady", playWhenReady);

        // Saving the state of the SDK
        mITGComponent.onSaveInstanceState(outState);
    }

    @SuppressLint("RestrictedApi")
    public boolean dispatchKeyEvent(KeyEvent event) {
        ITGPlaybackComponent itgPlaybackComponent = mITGComponent;
        if (itgPlaybackComponent != null
                && itgPlaybackComponent.getItgOverlayView() != null
                && itgPlaybackComponent.getItgOverlayView().isKeyEventConsumable(event)) {
            return super.dispatchKeyEvent(event);
        }
        // ... rest of your dispatchKeyEvent code
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        ITGPlaybackComponent itgPlaybackComponent = mITGComponent;
        if (itgPlaybackComponent != null
                && itgPlaybackComponent.getItgOverlayView() != null
                && itgPlaybackComponent.getItgOverlayView().isKeyEventConsumable(event)) {
            return super.onKeyUp(keyCode, event);
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        ITGPlaybackComponent itgPlaybackComponent = mITGComponent;
        if (itgPlaybackComponent != null
                && itgPlaybackComponent.getItgOverlayView() != null
                && itgPlaybackComponent.getItgOverlayView().isKeyEventConsumable(event)) {
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

}
