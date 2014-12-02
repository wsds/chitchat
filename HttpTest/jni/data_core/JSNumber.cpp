#include "JSNumber.h"


JSNumber::JSNumber(){
	this->type = JSNUMBER;
}

JSNumber::JSNumber(int number){
	this->type = JSNUMBER;
	this->number = number;
}