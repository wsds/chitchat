#ifndef JSOBJECT_H
#define JSOBJECT_H


#ifndef NULL
#define NULL 0
#endif /* NULL */

#define JSBOOL 0
#define JSNUMBER 1
#define JSJSON 2
#define JSKEYVALUE 3
#define JSSTRING 4

#define JSINSTANCE 5
#define JSCLASS 6
#define JSFUNCTION 7

class JSObject
{
public:
	char type;
	int number;//for debug
	char* char_string;//for debug
};


#endif /* JSOBJECT_H */

