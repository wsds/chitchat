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

	private short[] mRecordData;
	private short[] mPlayData;
	private byte[] mRecordProcessedData;
	private byte[] mPlayProcessedData;
	private boolean isRecording = false;
	private boolean isReady = false;
	private boolean isPlaying = false;
	private File audioFolder;
	private File raw;
	private AudioRecord mAudioRecord;
	private AudioTrack mAudioTrack;
	private PlayAudioThread mPlayAudioThread;
	private AudioListener mAudioListener;
	private int mPrimePlaySize = 0, mRecordReadSize = 38;

	private int mRecorderMinBufferSize = 0;

	private int mSpeexEncodeFrameSize = 0, mSpeexDecodeFrameSize = 0;
	private String fileName = "";

	private Speex speex;

	public static AudioHandlers handlers;

	public AudioHandlers() {
		speex = new Speex();
		mSpeexEncodeFrameSize = speex.getEncodeFrameSize();
		mSpeexDecodeFrameSize = speex.getDecodeFrameSize();
		Log.e(tag, "mSpeexEncodeFrameSize:" + mSpeexEncodeFrameSize + "          mSpeexDecodeFrameSize:" + mSpeexDecodeFrameSize);
		audioFolder = new File(Environment.getExternalStorageDirectory(), "audioTest");
		if (!audioFolder.exists()) {
			audioFolder.mkdir();
		}
	}

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
		if (mAudioRecord != null) {
			mAudioRecord.stop();
			isRecording = false;
			this.closeRecord();
		}
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

	public void startPlay(String filePath) {
		File file = new File(filePath);
		isPlaying = true;

		initAudioTrack();
		startThread(file);
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
		mRecordData = new short[mRecorderMinBufferSize];
		mRecordProcessedData = new byte[mSpeexEncodeFrameSize];

	}

	private void initAudioTrack() {
		mPrimePlaySize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, mPrimePlaySize * 2, AudioTrack.MODE_STREAM);
		isReady = true;
		mPlayData = new short[mPrimePlaySize];
		mPlayProcessedData = new byte[mSpeexDecodeFrameSize];
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

		byte[] data = null;
		if (inStream != null) {
			data = new byte[(int) (file.length() / 2)];
			try {
				int i = 0;
				while (inStream.available() > 0) {
					data[i] = inStream.readByte();
					i++;
				}
			} catch (IOException e) {
				e.printStackTrace();
				isReady = false;
				return;
			}

		}
		mPlayProcessedData = data;
		initAudioTrack();
	}

	private void getFilePath() {
		try {
			String fileName = String.valueOf(System.currentTimeMillis());
			raw = new File(audioFolder, fileName + ".aac");
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

	private void startThread(File file) {
		DataInputStream inStream = null;
		try {
			inStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			isReady = false;
			return;
		}
		if (inStream != null) {
			mPlayAudioThread = new PlayAudioThread(inStream);
			mPlayAudioThread.start();
		}
	}

	private void stopThread() {
		if (mPlayAudioThread != null) {
			mPlayAudioThread.interrupt();
			mPlayAudioThread = null;
		}
	}

	class PlayAudioThread extends Thread {
		private DataInputStream inStream;

		public PlayAudioThread(DataInputStream inStream) {
			this.inStream = inStream;

			try {
				Log.i(tag, "available:" + inStream.available());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			mAudioTrack.play();
			int total = 0;
			while (isPlaying) {
				try {
					int readSize = inStream.read(mPlayProcessedData, 0, mRecordReadSize);
					int decodeSize = speex.decode(mPlayProcessedData, mPlayData, readSize);
					int size = mAudioTrack.write(mPlayData, 0, decodeSize);
					Log.i(tag, "readSize:" + readSize + "    decodeSize:" + decodeSize + "   size:" + size + "    total:" + total);
					if (inStream.available() <= 0) {
						if (mAudioListener != null) {
							mAudioListener.onPlayComplete();
						}
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (mAudioListener != null) {
						mAudioListener.onPlayFail();
					}
					break;
				}
			}
			isPlaying = false;
			mAudioTrack.stop();
		}

		@Override
		public void interrupt() {
			isPlaying = false;
			mAudioTrack.stop();
			super.interrupt();
		}
	}

	String tag = "audio";

	class RecordAudioThread extends Thread {

		@Override
		public void run() {

			new EncodeAudioThread().start();
			while (isRecording) {
				int readSize = mAudioRecord.read(mRecordData, 0, mRecorderMinBufferSize);
				Log.v(tag, "pushEncodeData: " + readSize);
				speex.pushEncodeData(mRecordData, readSize);

				Log.i(tag, "readSize:" + readSize + "    mRecorderMinBufferSize:" + mRecorderMinBufferSize);
				if (mRecordReadSize != readSize) {
					mRecordReadSize = readSize;
					Log.i(tag, "readSize:" + readSize + "    mRecordReadSize:" + mRecordReadSize);
				}
				int volume = 0;
				if (mAudioListener != null) {
					mAudioListener.onRecording((int) Math.abs(volume / (float) readSize) / 10000 >> 1);
				}
			}

		}
	}

	class EncodeAudioThread extends Thread {
		@Override
		public void run() {
			DataOutputStream output = null;
			try {
				output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(raw)));
				int encodeSize = 0;
				while (isRecording) {
					Log.v(tag, "encodeFrame");
					encodeSize = speex.encodeFrame(mRecordProcessedData);
					if (encodeSize > 0) {
						output.write(mRecordProcessedData, 0, encodeSize);
						Log.d(tag, "encodeSize:" + encodeSize + "    mSpeexFrameSize:" + mSpeexEncodeFrameSize);
					} else {
						if (!isRecording) {
							break;
						} else {
							sleep(50);
						}
					}

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
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
