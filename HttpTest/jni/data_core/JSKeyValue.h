#ifndef JSKEYVALUE_H
#define JSKEYVALUE_H


#include "base/JSObject.h"

#ifndef NULL
#define NULL 0
#endif /* NULL */

class JSKeyValue : public JSObject
{
public:
	JSKeyValue();
	char* key;
	JSObject* value;

	void free();
};


#endif /* JSKEYVALUE_H */

