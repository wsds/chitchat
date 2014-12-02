#ifndef OPENHTTP_H
#define OPENHTTP_H


#include "JSObject.h"
#include "MemoryManagement.h"

#ifndef NULL
#define NULL 0
#endif /* NULL */

class OpenHttp
{
public:


    bool is_initialized = false;

    bool initialize();

    bool free();

};


#endif /* OPENHTTP_H */

