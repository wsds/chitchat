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

	private static final int SAMPLE_RATE = 8000;// 8000 11025 32000 44100
	private short[] mBuffer;
	private short[] mData;
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
	private int mPrimePlaySize = 0;
	private int mPlayOffset = 0;
	private int mRecorderMinBufferSize = 0;
	private int mSpeexFrameSize = 0;
	private String fileName = "";

	private Speex speex;

	public static AudioHandlers handlers;

	public AudioHandlers() {
		speex = new Speex();
		mSpeexFrameSize = speex.getFrameSize();
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

	public void startPlay(String filePath) {
		File file = new File(filePath);
		isPlaying = true;
		mPlayOffset = 0;
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
		mBuffer = new short[mRecorderMinBufferSize];
		mRecordProcessedData = new byte[mSpeexFrameSize];
	}

	private void initAudioTrack() {
		mPrimePlaySize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, mPrimePlaySize * 2, AudioTrack.MODE_STREAM);
		isReady = true;
		mData = new short[mPrimePlaySize * 10];
		mPlayProcessedData = new byte[mSpeexFrameSize];
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
					int readSize = inStream.read(mPlayProcessedData, 0, 38);
					int decodeSize = speex.decode(mPlayProcessedData, mData, readSize);
					int size = mAudioTrack.write(mData, 0, decodeSize);
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
			mAudioTrack.stop();
			isPlaying = false;
			super.interrupt();
		}
	}

	String tag = "audio";

	class RecordAudioThread extends Thread {

		@Override
		public void run() {
			DataOutputStream output = null;
			try {
				output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(raw)));
				while (isRecording) {
					int readSize = mAudioRecord.read(mBuffer, 0, mRecorderMinBufferSize);
					int volume = 0, encodeSize = 0, total = 0;
					Log.i(tag, "readSize:" + readSize + "    mRecorderMinBufferSize:" + mRecorderMinBufferSize);
					while (true) {
						if ((total + mSpeexFrameSize) >= readSize) {
							encodeSize = speex.encode(mBuffer, total, mRecordProcessedData, readSize - total);

						} else {
							encodeSize = speex.encode(mBuffer, total, mRecordProcessedData, mSpeexFrameSize);
						}
						Log.d(tag, "encodeSize:" + encodeSize + "    mSpeexFrameSize:" + mSpeexFrameSize + "total" + total);
						output.write(mRecordProcessedData, 0, encodeSize);
						total += mSpeexFrameSize;
						if (total >= readSize) {
							break;
						}
						// for (int j = 0; j < encodeSize; j++) {
						// output.writeByte(processedData[j]);
						// // volume += processedData[i] * processedData[i];
						// }
					}
					// if (mAudioListener != null) {
					// mAudioListener.onRecording((int) Math.abs(volume / (float) encodeSize) / 10000 >> 1);
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
