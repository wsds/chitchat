#ifndef HASHTABLE_H
#define HASHTABLE_H

#include "JSObject.h"
#include "MemoryManagement.h"

#include "../JSNumber.h"

#ifndef NULL
#define NULL 0
#endif /* NULL */

class HashEntry {
public:
	char* key;
	JSObject* value;
	unsigned int hash;

	int level;

	HashEntry* next;
};

class HashTable {
public:

	HashEntry** elements; //~~~~~~~~~~~~~~~Memory Management~~~~~~~~~~~~~~~~~
	//HashEntry* elements[50];//~~~~~~~~~~~~~~~Memory Management~~~~~~~~~~~~~~~~~//for debug

	int length;

	int max_size;
	int threshold;

	bool is_initialized = false;

	//get O(1)
	JSObject* get(char* key);
	JSObject* get(int key);
	JSObject* get(char* key, int keyLength);

	//set O(1)
	JSObject* set(char* key, JSObject* value);
	JSObject* set(int key, JSObject* value);
	JSObject* set(char* key, int keyLength, JSObject* value);

	//del O(1)
	JSObject* del(char* key);
	JSObject* del(int key);
	JSObject* del(char* key, int keyLength);

	//resize O(n)
	bool resize();

	//initialize default size=8;
	bool initialize();

	bool free();
};

unsigned int dictGenHashFunction(const void *key, int len);

int strlen(char *str);
int strcmp(char *src, char *dst);
int strcopy(char *source, char *target);
int strcopy(char *source, char *target, int length);
int strappend(char *target, char *source);
void strclear(char *str);

static char NUMBERCHARSTART = '0';
static char NUMBERCHAREND = '9';

bool isNumber(char *string);
int parseStringToNubmer(char* string, int length);
int parseNubmerToString(int number, char * target);

#endif /* HASHTABLE_H */

