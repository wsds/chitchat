#include "Log.h"

void Log(char * message) {
	__android_log_print(ANDROID_LOG_ERROR, "OpenHttp", (const char *) message);
}

char bigMessage[300] = "";
void Log(char * message1, char * message2) {
	int message1Len = strlen(message1);
	int message2Len = strlen(message2);
	for (int i = 0; i < message1Len; i++) {
		*(bigMessage + i) = *(message1 + i);
	}
	for (int i = 0; i < message2Len; i++) {
		*(bigMessage + message1Len + i) = *(message2 + i);
	}
	*(bigMessage + message1Len + message2Len) = 0;
	__android_log_print(ANDROID_LOG_ERROR, "OpenHttp", (const char *) bigMessage);
}

void Log(char * message1, int number) {
	int message1Len = strlen(message1);
	for (int i = 0; i < message1Len; i++) {
		*(bigMessage + i) = *(message1 + i);
	}
	char message2[15] = "";
	parseNubmerToString(number, message2);
	for (int i = 0; i < 15; i++) {
		*(bigMessage + message1Len + i) = *(message2 + i);
	}
	*(bigMessage + message1Len + 15) = 0;
	__android_log_print(ANDROID_LOG_ERROR, "OpenHttp", (const char *) bigMessage);
}


void Log(int number) {
	char target[15] = "";
	parseNubmerToString(number, target);
	Log((char*) target);
}


void Log(long number) {
	char target[32] = "";
	parseNubmerToString(number, target);
	Log((char*) target);
}
