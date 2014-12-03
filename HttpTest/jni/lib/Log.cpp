#include "Log.h"


void Log(char * message){
	__android_log_print(ANDROID_LOG_ERROR, "OpenHttp",(const char *)message);
}

void Log(int number){
	char target[15] = "";
	parseNubmerToString(number, target);
	Log(( char*) target);
}
