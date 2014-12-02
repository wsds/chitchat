#ifndef JSNUMBER_H
#define JSNUMBER_H


#include "base/JSObject.h"

#ifndef NULL
#define NULL 0
#endif /* NULL */

class JSNumber : public JSObject
{

public:
	JSNumber();
	JSNumber(int number);

    //int number;
    void test()
    {

//		int i = 1 + 1;
    }

};


#endif /* JSNUMBER_H */

