package net.hogelab.android.MediaPlayer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.SeekBar;


public class MediaPlayerActivity extends Activity
		implements	Callback,
					OnPreparedListener,
					OnVideoSizeChangedListener,
					OnCompletionListener,
					OnErrorListener,
					OnInfoListener,
					OnBufferingUpdateListener,
					OnSeekCompleteListener {

	private static final String	TAG = MediaPlayerActivity.class.getSimpleName();

	private MediaPlayer			mMediaPlayer = null;
	private String				mUrl = null;

	private Handler				mHandler = null;
	private Timer				mSeekBarUpdateTimer = null;

	private SurfaceView			mPlayerSurface = null;
	private SurfaceHolder		mHolder = null;

	private View				mProgressContainer = null;
	private ProgressBar			mProgressBar = null;

	private View				mControllerContainer = null;
	private SeekBar				mPositionSeekBar = null;

	private int					mVideoWidth = 0;
	private int					mVideoHeight = 0;
	private int					mVideoDurationSeconds = 0;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_mediaplayer);

		Bundle extras = getIntent().getExtras();
		mUrl = extras.getString("mediaUrl");

		mHandler = new Handler(this.getMainLooper());

		mPlayerSurface = (SurfaceView)findViewById(R.id.surfacePlayer);
		SurfaceHolder holder = mPlayerSurface.getHolder();
		//holder.setKeepScreenOn(true);
		holder.addCallback(this);

		mProgressContainer = findViewById(R.id.progressContainer);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
		hideProgress();

		mControllerContainer = findViewById(R.id.controllerContainer);
		mPositionSeekBar = (SeekBar)findViewById(R.id.seekBar);
		//hideController();
	}


	//--------------------------------------------------
	// SurfaceHolder callbacks

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");

		final SurfaceHolder fh = holder;
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				createPlayer(fh);				
			}
		});
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");

		final SurfaceHolder fh = holder;
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				destroyPlayer(fh);
			}
		});
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged format:" + format + ", width:" + width + ", height:" + height);
	}


	//--------------------------------------------------
	// MediaPlayer callbacks

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "onPrepared");

		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mMediaPlayer.start();

				initializeController();
			}
		});
	}


	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.d(TAG, "onVideoSizeChanged width:" + width + ", height:" + height);

		final int fw = width;
		final int fh = height;
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				setPlayerSize(fw, fh);
			}
		});
	}


	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "onCompletion");
	}


	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d(TAG, "onError what:" + what + ", extra:" + extra);

		switch (what) {

		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			break;

		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			break;
		}

		switch (extra) {

		case MediaPlayer.MEDIA_ERROR_IO:
			break;

		case MediaPlayer.MEDIA_ERROR_MALFORMED:
			break;

		case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
			break;

		case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
			break;
		}

		return false;
	}


	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.d(TAG, "onInfo what:" + what + ", extra:" + extra);

		switch (what) {

		case MediaPlayer.MEDIA_INFO_UNKNOWN:
			Log.d(TAG, "onInfo - MEDIA_INFO_UNKNOWN");
			break;

		case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
			Log.d(TAG, "onInfo - MEDIA_INFO_VIDEO_TRACK_LAGGING");
			break;

		case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
			Log.d(TAG, "onInfo - MEDIA_INFO_VIDEO_RENDERING_START");
			break;

		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			Log.d(TAG, "onInfo - MEDIA_INFO_BUFFERING_START");
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					showProgress();
				}
			});
			break;

		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			Log.d(TAG, "onInfo - MEDIA_INFO_BUFFERING_END");
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					hideProgress();
				}
			});
			break;

		case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
			Log.d(TAG, "onInfo - MEDIA_INFO_BAD_INTERLEAVING");
			break;

		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			Log.d(TAG, "onInfo - MEDIA_INFO_NOT_SEEKABLE");
			break;

		case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
			Log.d(TAG, "onInfo - MEDIA_INFO_METADATA_UPDATE");
			break;
		}

		return false;
	}


	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.d(TAG, "onBufferingUpdate percent:" + percent);

		final int fp = percent;
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				updateProgress(fp);
			}
		});
	}


	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.d(TAG, "onSeekComplete");
	}


	public void onClickRewind(View v) {
		int position = getPositionSeconds() - 30;
		if (position < 0) {
			position = 0;
		}

		final int fp = position;
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mMediaPlayer.seekTo(fp * 1000);
			}
		});
	}


	public void onClickPause(View v) {
	}


	public void onClickFastForward(View v) {
		int position = getPositionSeconds() + 30;
		int duration = getDurationSeconds();
		if (position > duration) {
			position = duration;
		}

		final int fp = position;
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mMediaPlayer.seekTo(fp * 1000);
			}
		});
	}


	//--------------------------------------------------
	// private functions

	private int getDurationSeconds() {
		if (mMediaPlayer != null) {
			return mMediaPlayer.getDuration() / 1000;
		}

		return 0;
	}

	private int getPositionSeconds() {
		if (mMediaPlayer != null) {
			return mMediaPlayer.getCurrentPosition() / 1000;
		}

		return 0;
	}


	private void createPlayer(SurfaceHolder holder) {
		if (mMediaPlayer != null) {
			destroyPlayer(holder);
		}

		mMediaPlayer = new MediaPlayer();
		mHolder = holder;

		mVideoWidth = 0;
		mVideoHeight = 0;
		mVideoDurationSeconds = 0;

		try {
			mMediaPlayer.setDisplay(mHolder);
			mMediaPlayer.setDataSource(mUrl);

			mMediaPlayer.setScreenOnWhilePlaying(true);

			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnErrorListener(this);
			mMediaPlayer.setOnInfoListener(this);
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnSeekCompleteListener(this);

			mMediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void destroyPlayer(SurfaceHolder holder) {
		stopTimer();

		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}

			mMediaPlayer.setDisplay(null);
			mMediaPlayer.release();
			mMediaPlayer = null;
		}

		mHolder = null;
	}


	private void initializeController() {
		mPositionSeekBar.setMax(getDurationSeconds());
		mPositionSeekBar.setProgress(0);

		startTimer();
	}


	private void setPlayerSize(int videoWidth, int videoHeight) {
		if (videoWidth == mVideoWidth && videoHeight == mVideoHeight) {
			return;
		}

		mVideoWidth = videoWidth;
		mVideoHeight = videoHeight;

		Point screenSize = new Point(0, 0);
	    getWindowManager().getDefaultDisplay().getSize(screenSize);
		float widthRatio = (float)screenSize.x / mVideoWidth;
		float heightRatio = (float)screenSize.y / mVideoHeight;

		int width = 0;
		int height = 0;
//		if (widthRatio > heightRatio) {
		if (widthRatio < heightRatio) {
			width = screenSize.x;
			height = (int)(mVideoHeight * widthRatio);
		} else {
			width = (int)(mVideoWidth * heightRatio);
			height = screenSize.y;
		}

		ViewGroup.LayoutParams slp = mPlayerSurface.getLayoutParams();
	    slp.width = width;
	    slp.height = height;
	    mPlayerSurface.setLayoutParams(slp);

	    Log.d(TAG, "setPlayerSize - Video Width:" + mVideoWidth + ", Height:" + mVideoHeight +
	    		" - Adjusted Width:" + width + ", Height:" + height);
	}


	private void startTimer() {
		stopTimer();

		mSeekBarUpdateTimer = new Timer();
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				onTimer();
			}			
		};
		mSeekBarUpdateTimer.schedule(timerTask, 1000, 1000);
	}

	private void stopTimer() {
		if (mSeekBarUpdateTimer != null) {
			mSeekBarUpdateTimer.cancel();
			mSeekBarUpdateTimer = null;
		}
	}

	private void onTimer() {
		if (mMediaPlayer != null) {
			int durationSeconds = getDurationSeconds();
			if (durationSeconds != mVideoDurationSeconds) {
				mVideoDurationSeconds = durationSeconds;
				mPositionSeekBar.setMax(mVideoDurationSeconds);
			}

			mPositionSeekBar.setProgress(getPositionSeconds());
		}
	}


	private void showProgress() {
		mProgressBar.setProgress(0);
		mProgressContainer.setVisibility(View.VISIBLE);
	}

	private void hideProgress() {
		mProgressContainer.setVisibility(View.INVISIBLE);
	}

	private void updateProgress(int percent) {
		if (percent <= 0) {
			mProgressBar.setProgress(0);
		} else if (percent <= 100) {
			mProgressBar.setProgress(percent);
		} else {
			mProgressBar.setProgress(100);
		}
	}


	private void showController() {
		if (!mControllerContainer.isShown()) {
			mControllerContainer.setVisibility(View.VISIBLE);
		}
	}

	private void hideController() {
		if (mControllerContainer.isShown()) {
			mControllerContainer.setVisibility(View.INVISIBLE);
		}
	}
}
