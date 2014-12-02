#ifndef JSON_H
#define JSON_H


#include "base/JSObject.h"
#include "base/List.h"
#include "base/HashTable.h"

#include "JSKeyValue.h"
//#include "JSParsingObject.h"


#include "JSString.h"
#include "JSNumber.h"

#ifndef NULL
#define NULL 0
#endif /* NULL */

static char LEFTBRACKET = '[';
static char RIGHTBRACKET = ']';
static char SINGLEQUOTE = '\'';
static char DOUBLEQUOTES = '"';
static char EQUALITY = '=';
static char BLANK = ' ';

static char SLASH = '/';

static char COMMA = ',';
static char SEMICOLON = ';';
static char COLON = ':';

static char LEFTSMALLBRACKET = '(';
static char RIGHTSMALLBRACKET = ')';

static char LEFTBIGBRACKET = '{';
static char RIGHTBIGBRACKET = '}';

static char BR = '\n';
static char ENTER = '\r';

static char STREND = '\0';

static char TAB = '\t';

class JSONIndicator
{
public:
	int head;
	int tail;
	int quotes_count;//[0,1,3]

	JSObject* json;
};

class JSON : public JSObject
{
public:
	JSON();
	~JSON();

	LIST *list;
	HashTable *hashTable;

	JSONIndicator* json_indicator;

	int length;
	bool is_initialized = false;

	bool initialize();
	bool free();


	//API
	//list
	JSObject* pop();
	bool push(JSObject* object);
	bool insert(JSObject* object, int index);
	bool replace(JSObject* object, int index);
	JSObject*  del(int index);
	JSObject* find(int index);

	//hash
	JSObject* get(char* key);
	bool set(char* key, JSObject* value);
	JSObject*  del(char* key);

	//serialize
	char* stringify();
	bool parse(char* string);
};

void testJSONParse();
JSON* parseJSON(char* string);
JSObject*  parseObject(char* string, JSONIndicator* object_indicator, bool isJSKeyValue);

char* stringifyJSON(JSON* json);

JSON* cloneJSON(JSON* json);
void cloneJSON(JSON * json, JSON * clone);

#endif /* JSON_H */

