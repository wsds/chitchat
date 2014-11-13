package com.open.chitchat.model;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.open.chitchat.listener.PlayCompleteListener;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class AudioHandlers {
	private FileHandlers fileHandlers = FileHandlers.getInstance();
	private Data data = Data.getInstance();

	private static final int SAMPLE_RATE = 8000;
	private short[] mBuffer;
	private byte[] mData;
	private boolean isRecording = false;
	private boolean isReady = false;
	private File raw;
	private AudioRecord mAudioRecord;
	private AudioTrack mAudioTrack;
	private PlayAudioThread mPlayAudioThread;
	private PlayCompleteListener mPlayCompleteListener;
	private int mPrimePlaySize = 0;
	private int mPlayOffset = 0;

	public static AudioHandlers handlers;

	public static AudioHandlers getInstance() {
		if (handlers == null) {
			handlers = new AudioHandlers();
		}
		return handlers;
	}

	public void setPlayCompleteListener(PlayCompleteListener mPlayCompleteListener) {
		this.mPlayCompleteListener = mPlayCompleteListener;
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
		startBufferedWrite(raw);

		isRecording = true;
	}

	public String stopRecording() {
		if (isRecording) {
			return "";
		}
		mAudioRecord.stop();
		isRecording = false;
		this.closeRecord();
		return raw.getAbsolutePath();
	}

	public boolean isRecording() {
		return isRecording;
	}

	public void prepare(String fileName) {
		File file = new File(fileHandlers.sdcardVoiceFolder, fileName);
		if (file.exists()) {
			getData(file);
		} else {
			fileHandlers.downloadVoiceFile(file, fileName);
		}
	}

	public void play() {
		if (isReady) {
			mPlayOffset = 0;
			startThread();
		}
	}

	public void release() {
		stop();
		releaseAudioTrack();
	}

	public void stop() {
		isReady = false;
		stopThread();
	}

	private void initRecorder() {
		int minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		mBuffer = new short[minBufSize];
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufSize);
	}

	private void closeRecord() {
		if (mAudioRecord != null)
			mAudioRecord.release();
		mAudioRecord = null;
	}

	@SuppressWarnings("resource")
	private void getData(File file) {
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			isReady = false;
			return;
		}

		byte[] data = null;
		if (inStream != null) {
			long size = file.length();
			data = new byte[(int) size];
			try {
				inStream.read(data);
			} catch (IOException e) {
				e.printStackTrace();
				isReady = false;
				return;
			}

		}
		mData = data;
		initAudioTrack();
	}

	private void initAudioTrack() {
		int minBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		mPrimePlaySize = minBufSize * 2;
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, minBufSize, AudioTrack.MODE_STREAM);
		// STREAM_ALARM��������
		// STREAM_MUSCI��������������music��
		// STREAM_RING������
		// STREAM_SYSTEM��ϵͳ����
		// STREAM_VOCIE_CALL���绰����
		// AudioTrack����MODE_STATIC��MODE_STREAM���ַ��ࡣ
		// STREAM����˼�����û���Ӧ�ó���ͨ��write��ʽ������һ��һ�ε�д��audiotrack�С�
		// �����������socket�з�������һ����Ӧ�ò��ĳ���ط���ȡ���ݣ�����ͨ�������õ�PCM���ݣ�Ȼ��write��audiotrack��
		// ���ַ�ʽ�Ļ�������������JAVA���Native�㽻����Ч����ʧ�ϴ�
		// ��STATIC����˼��һ��ʼ������ʱ�򣬾Ͱ���Ƶ���ݷŵ�һ���̶���buffer��Ȼ��ֱ�Ӵ���audiotrack��
		// �����Ͳ���һ�δε�write�ˡ�AudioTrack���Լ��������buffer�е����ݡ�
		// ���ַ��������������ڴ�ռ�ý�С����ʱҪ��ϸߵ�������˵�����á�
		isReady = true;
	}

	private void getFilePath() {
		try {
			String fileName = String.valueOf(System.currentTimeMillis()) + data.userInformation.currentUser.phone;
			raw = new File(fileHandlers.sdcardVoiceFolder, fileName + ".osa");
			raw.createNewFile();
			// runCommand("chmod 777 " + rawPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startBufferedWrite(final File file) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				DataOutputStream output = null;
				try {
					output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
					while (isRecording) {
						int readSize = mAudioRecord.read(mBuffer, 0, mBuffer.length);
						for (int i = 0; i < readSize; i++) {
							output.writeShort(mBuffer[i]);
						}
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
		}).start();
	}

	private void releaseAudioTrack() {
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack.release();
			mAudioTrack = null;
		}

	}

	private void startThread() {
		if (mPlayAudioThread == null) {
			mPlayAudioThread = new PlayAudioThread();
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

		@Override
		public void run() {
			mAudioTrack.play();
			while (true) {
				try {
					int size = mAudioTrack.write(mData, mPlayOffset, mPrimePlaySize);
					mPlayOffset += mPrimePlaySize;
				} catch (Exception e) {
					e.printStackTrace();
					if (mPlayCompleteListener != null) {
						mPlayCompleteListener.onPlayFail();
					}
					break;
				}
				if (mPlayOffset >= mData.length) {
					if (mPlayCompleteListener != null) {
						mPlayCompleteListener.onPlayComplete();
					}
					break;
				}
			}
			mAudioTrack.stop();
		}
	}

}
