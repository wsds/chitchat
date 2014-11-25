#include <jni.h>

#include <string.h>
#include <stdlib.h>
#include <unistd.h>

#include <speex/speex.h>

#include <speex/speex_preprocess.h>    
#include <speex/speex_echo.h>     

static int codec_open = 0;

static int dec_frame_size;
static int enc_frame_size;

static SpeexBits * ebits;
static SpeexBits * dbits;

static short * encode_data_buffer;
static int encode_data_index;
static int encoded_frame_index;

static short * encode_data_temp;
static jbyte * output_frame_buffer;

void *enc_state;
void *dec_state;

SpeexPreprocessState * preprocessState;

static JavaVM *gJavaVM;
//com.android.gl2jni
extern "C"
JNIEXPORT jint JNICALL Java_com_android_gl2jni_Speex_open(JNIEnv *env, jobject obj, jint compression) {
	int tmp;

	if (codec_open++ != 0)
	return (jint) 0;

	ebits= new SpeexBits();
	dbits= new SpeexBits();

	speex_bits_init(ebits);
	speex_bits_init(dbits);

	preprocessState=speex_preprocess_state_init(160, 8000);

	enc_state = speex_encoder_init(&speex_nb_mode);
	dec_state = speex_decoder_init(&speex_nb_mode);
	tmp = compression;
	speex_encoder_ctl(enc_state, SPEEX_SET_QUALITY, &tmp);
	speex_encoder_ctl(enc_state, SPEEX_GET_FRAME_SIZE, &enc_frame_size);
	speex_decoder_ctl(dec_state, SPEEX_GET_FRAME_SIZE, &dec_frame_size);

	int denoise = 1;
	int noiseSuppress = -25;
	speex_preprocess_ctl(preprocessState, SPEEX_PREPROCESS_SET_DENOISE, &denoise);
	speex_preprocess_ctl(preprocessState, SPEEX_PREPROCESS_SET_NOISE_SUPPRESS, &noiseSuppress);

	int agc = 1;
	float q=24000;
	speex_preprocess_ctl(preprocessState, SPEEX_PREPROCESS_SET_AGC, &agc);
	speex_preprocess_ctl(preprocessState, SPEEX_PREPROCESS_SET_AGC_LEVEL,&q);
	int vad = 1;
	int vadProbStart = 80;
	int vadProbContinue = 65;
	speex_preprocess_ctl(preprocessState, SPEEX_PREPROCESS_SET_VAD, &vad);
	speex_preprocess_ctl(preprocessState, SPEEX_PREPROCESS_SET_PROB_START , &vadProbStart);
	speex_preprocess_ctl(preprocessState, SPEEX_PREPROCESS_SET_PROB_CONTINUE, &vadProbContinue);

	encode_data_buffer= (jshort*) malloc(enc_frame_size*10 *sizeof(jshort));

	encode_data_temp = (jshort*) malloc(enc_frame_size*10 *sizeof(jshort));

	output_frame_buffer=(jbyte*) malloc(enc_frame_size *sizeof(jbyte));

	encode_data_index = 0;
	encoded_frame_index = 0;
	return (jint) 0;
}

extern "C"
JNIEXPORT jint Java_com_android_gl2jni_Speex_encode(JNIEnv *env,
		jobject obj, jshortArray lin, jint offset, jbyteArray encoded,
		jint size) {

	jshort buffer[enc_frame_size];
	jbyte output_buffer[enc_frame_size];
	int nsamples = (size - 1) / enc_frame_size + 1;
	int i, tot_bytes = 0;

	if (!codec_open)
	return 0;

	speex_bits_reset(ebits);

	for (i = 0; i < nsamples; i++) {
		env->GetShortArrayRegion(lin, offset + i * enc_frame_size,
				enc_frame_size, buffer);
		speex_encode_int(enc_state, buffer, ebits);
	}
	//env->GetShortArrayRegion(lin, offset, enc_frame_size, buffer);
	//speex_encode_int(enc_state, buffer, &ebits);

	tot_bytes = speex_bits_write(ebits, (char *) output_buffer,
			enc_frame_size);
	env->SetByteArrayRegion(encoded, 0, tot_bytes, output_buffer);

	return (jint) tot_bytes;
}

extern "C"
JNIEXPORT jint Java_com_android_gl2jni_Speex_encode1(JNIEnv *env, jobject obj, jshortArray lin, jint offset, jbyteArray encoded, jint size) {

	jshort buffer[enc_frame_size];
	jbyte output_buffer[enc_frame_size];
	int i, tot_bytes = 0;

	if (!codec_open)
	return 0;

	speex_bits_reset(ebits);

	env->GetShortArrayRegion(lin, offset, enc_frame_size, buffer);
	speex_encode_int(enc_state, buffer, ebits);
	//env->GetShortArrayRegion(lin, offset, enc_frame_size, buffer);
	//speex_encode_int(enc_state, buffer, &ebits);

	tot_bytes = speex_bits_write(ebits, (char *) output_buffer, enc_frame_size);
	env->SetByteArrayRegion(encoded, 0, tot_bytes, output_buffer);

	return (jint) tot_bytes;
}

extern "C"
JNIEXPORT jint Java_com_android_gl2jni_Speex_pushEncodeData(JNIEnv *env, jobject obj, jshortArray encodeData, jint size) {

	env->GetShortArrayRegion(encodeData, 0, size, encode_data_temp);

	int index=0;
	int encode_data_buffer_size=enc_frame_size*10;
	for (int i = 0; i < size; i++) {
		index=(encode_data_index + i) % encode_data_buffer_size;
		encode_data_buffer[index] = encode_data_temp[i];
	}
	encode_data_index = encode_data_index + size;

	return (jint) 1;
}

extern "C" int encodeFrame(spx_int16_t *in) {

	int encoded_size_bytes = -1;

	speex_bits_reset(ebits);

	speex_encode_int(enc_state, in, ebits);

	encoded_size_bytes = speex_bits_write(ebits, (char *) output_frame_buffer, enc_frame_size);
	return encoded_size_bytes;
}

extern "C"
JNIEXPORT jint Java_com_android_gl2jni_Speex_encodeFrame(JNIEnv *env, jobject obj, jbyteArray encodedFrameData) {
	int encoded_size_bytes=-1;

	if (!codec_open) {
		return (jint) encoded_size_bytes;
	}
	;
	if (encoded_frame_index <= (encode_data_index - enc_frame_size)) {
		int index=encoded_frame_index%(enc_frame_size*10);
		encoded_size_bytes=encodeFrame(encode_data_buffer+index);
		encoded_frame_index=encoded_frame_index+enc_frame_size;
		env->SetByteArrayRegion(encodedFrameData, 0, encoded_size_bytes, output_frame_buffer);
	} else {
		encoded_size_bytes=-1;
	}

	return (jint) encoded_size_bytes;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_android_gl2jni_Speex_decode(JNIEnv *env,
		jobject obj, jbyteArray encoded, jshortArray lin, jint size) {

	jbyte buffer[dec_frame_size];
	jshort output_buffer[dec_frame_size];
	jsize encoded_length = size;

	if (!codec_open)
	return 0;

	env->GetByteArrayRegion(encoded, 0, encoded_length, buffer);
	speex_bits_read_from(dbits, (char *) buffer, encoded_length);
	speex_decode_int(dec_state, dbits, output_buffer);
	env->SetShortArrayRegion(lin, 0, dec_frame_size, output_buffer);

	return (jint) dec_frame_size;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_android_gl2jni_Speex_getEncodeFrameSize(
		JNIEnv *env, jobject obj) {

	if (!codec_open)
	return 0;
	return (jint) enc_frame_size;

}

extern "C"
JNIEXPORT jint JNICALL Java_com_android_gl2jni_Speex_getDecodeFrameSize(
		JNIEnv *env, jobject obj) {

	if (!codec_open)
	return 0;
	return (jint) dec_frame_size;

}

extern "C"
JNIEXPORT void JNICALL Java_com_android_gl2jni_Speex_close(JNIEnv *env,
		jobject obj) {

	if (--codec_open != 0)
	return;

	free(encode_data_buffer);
	free(encode_data_temp);
	free(output_frame_buffer);

	speex_bits_destroy(ebits);
	speex_bits_destroy(dbits);
	speex_decoder_destroy(dec_state);
	speex_encoder_destroy(enc_state);
}
