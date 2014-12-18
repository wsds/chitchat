#ifndef LOG_H
#define LOG_H
#include <android/log.h>
#include <stdlib.h>
#include "../data_core/base/HashTable.h"

void Log(char * message);
void Log(int number);
void Log(long number);
void Log(char * message1, char * message2);
void Log(char * message1, int number);
#endif /* LOG_H */

