#ifndef JSSTRING_H
#define JSSTRING_H


#include "base/JSObject.h"
#include "base/List.h"
#include "base/HashTable.h"

#ifndef NULL
#define NULL 0
#endif /* NULL */

class JSString : public JSObject
{
public:
	//char* char_string; 
	int length;

	JSString();
	JSString(char* char_string);
	~JSString();

};


#endif /* JSSTRING_H */

