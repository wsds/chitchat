package com.android.gl2jni;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class AudioHandlers {
	// private FileHandlers fileHandlers = FileHandlers.getInstance();
	// private Data data = Data.getInstance();

	private static final int SAMPLE_RATE = 11025;// 8000 11025 32000 44100
	private short[] mBuffer;
	private short[] mData;
	private boolean isRecording = false;
	private boolean isReady = false;
	private boolean isPlaying = false;
	private File raw;
	private AudioRecord mAudioRecord;
	private AudioTrack mAudioTrack;
	private PlayAudioThread mPlayAudioThread;
	private AudioListener mAudioListener;
	private int mPrimePlaySize = 0;
	private int mPlayOffset = 0;
	private int mRecorderMinBufferSize = 0;
	private String fileName = "";

	public static AudioHandlers handlers;

	public static AudioHandlers getInstance() {
		if (handlers == null) {
			handlers = new AudioHandlers();
		}
		return handlers;
	}

	public void setAudioListener(AudioListener mAudioListener) {
		this.mAudioListener = mAudioListener;
	}

	public void startRecording() {
		if (isRecording) {
			return;
		}
		if (mAudioRecord == null) {
			initRecorder();
		}

		getFilePath();
		mAudioRecord.startRecording();
		new RecordAudioThread().start();
		isRecording = true;
	}

	public String stopRecording() {
		mAudioRecord.stop();
		isRecording = false;
		this.closeRecord();
		return raw.getAbsolutePath();
	}

	public void cancelRecording() {
		mAudioRecord.stop();
		isRecording = false;
		this.closeRecord();
		raw.delete();
	}

	private void closeRecord() {
		if (mAudioRecord != null)
			mAudioRecord.release();
		mAudioRecord = null;
	}

	public void preparePlay(String fileName) {
		if (!this.fileName.equals(fileName)) {
			this.fileName = fileName;
			isReady = false;
			File file = new File(fileName);
			if (file.exists()) {
				getData(file);
			} else {
				Log.e("Speex", "FileNotFound");
				// fileHandlers.downloadVoiceFile(file, fileName);
			}
		} else {
			isReady = true;
			if (isPlaying) {
				stopPlay();
			}
			if (mAudioListener != null) {
				mAudioListener.onPrepared();
			}
		}
	}

	public void startPlay() {
		if (isReady) {
			isPlaying = true;
			mPlayOffset = 0;
			startThread();
		}
	}

	public void stopPlay() {
		isPlaying = false;
		stopThread();
	}

	public void releasePlyer() {
		this.stopPlay();
		releaseAudioTrack();
	}

	private void initRecorder() {
		mRecorderMinBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mRecorderMinBufferSize);
		mBuffer = new short[mRecorderMinBufferSize];
	}

	private void initAudioTrack() {
		mPrimePlaySize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, mPrimePlaySize * 2, AudioTrack.MODE_STREAM);
		isReady = true;
		if (mAudioListener != null) {
			mAudioListener.onPrepared();
		}
	}

	@SuppressWarnings("resource")
	private void getData(File file) {
		DataInputStream inStream = null;
		try {
			inStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			isReady = false;
			return;
		}

		short[] data = null;
		if (inStream != null) {
			data = new short[(int) (file.length() / 2)];
			try {
				int i = 0;
				while (inStream.available() > 0) {
					data[i] = inStream.readShort();
					i++;
				}
			} catch (IOException e) {
				e.printStackTrace();
				isReady = false;
				return;
			}

		}
		mData = data;
		initAudioTrack();
	}

	private void getFilePath() {
		try {
			String fileName = String.valueOf(System.currentTimeMillis());
			raw = new File(Environment.getExternalStorageDirectory(), "/audioTest/" + fileName + ".osa");
			raw.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isRecording() {
		return isRecording;
	}

	public boolean isReady() {
		return isReady;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	private void releaseAudioTrack() {
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack.release();
			mAudioTrack = null;
		}

	}

	private void startThread() {
		mPlayAudioThread = new PlayAudioThread();
		mPlayAudioThread.start();
	}

	private void stopThread() {
		if (mPlayAudioThread != null) {
			mPlayAudioThread.interrupt();
			mPlayAudioThread = null;
		}
	}

	class PlayAudioThread extends Thread {

		@Override
		public void run() {
			mAudioTrack.play();
			while (isPlaying) {
				try {
					int size = mAudioTrack.write(mData, mPlayOffset, mPrimePlaySize);
					mPlayOffset += size;
				} catch (Exception e) {
					e.printStackTrace();
					if (mAudioListener != null) {
						mAudioListener.onPlayFail();
					}
					break;
				}
				if (mPlayOffset >= mData.length) {
					if (mAudioListener != null) {
						mAudioListener.onPlayComplete();
					}
					break;
				}
			}
			isPlaying = false;
			mAudioTrack.stop();
		}

		@Override
		public void interrupt() {
			mAudioTrack.stop();
			isPlaying = false;
			super.interrupt();
		}
	}

	class RecordAudioThread extends Thread {
		@Override
		public void run() {
			DataOutputStream output = null;
			try {
				output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(raw)));
				while (isRecording) {
					int readSize = mAudioRecord.read(mBuffer, 0, mRecorderMinBufferSize);
					int volume = 0;
					for (int i = 0; i < readSize; i++) {
						output.writeShort(mBuffer[i]);
						volume += mBuffer[i] * mBuffer[i];
					}
					if (mAudioListener != null) {
						mAudioListener.onRecording((int) Math.abs(volume / (float) readSize) / 10000 >> 1);
					}
					// for (int i = 0; i < mBuffer.length; i++) {
					//
					// }
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (output != null) {
					try {
						output.flush();
					} catch (IOException e) {
						e.printStackTrace();

					} finally {
						try {
							output.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
