#ifndef CLOX_COMPILER_h
#define CLOX_COMPILER_h

#include "object.h"
#include "vm.h"

ObjFunction *compile(const char *source);
void markCompilerRoots();

#endif
