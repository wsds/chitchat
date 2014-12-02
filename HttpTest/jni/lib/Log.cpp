#include "Log.h"


void Log(const char * message){
	__android_log_print(ANDROID_LOG_ERROR, "OpenHttp",message);
}
