#ifndef LIST_H
#define LIST_H


#include "JSObject.h"
#include "MemoryManagement.h"

#ifndef NULL
#define NULL 0
#endif /* NULL */

class LIST
{
public:
	//store the orginal type of data directly should be supported. or supported by another class. 
    JSObject** elements;//~~~~~~~~~~~~~~~Memory Management~~~~~~~~~~~~~~~~~
	//JSObject* elements[50];//~~~~~~~~~~~~~~~Memory Management~~~~~~~~~~~~~~~~~//for debug

    int length;

    int max_size;
    int threshold;


    bool is_initialized = false;

    //API
    //find O(1)
    JSObject* find(int index);
    //find O(n)
    int findValue(JSObject* object);

    //insert O(n)
    bool insert(JSObject* object, int index);


    //replace O(1)
    bool replace(JSObject* object, int index);

    //del O(n)
	JSObject*  del(int index);

    //pop O(1)
    JSObject* pop();

    //push O(1)
    bool push(JSObject* object);

    //resize O(n)
    bool resize();

    //initialize default size=8;
    bool initialize();

    bool free();

};


#endif /* LIST_H */

