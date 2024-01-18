#pragma once
#include <stdlib.h>
using namespace std;

namespace Utils 
{
	bool randomBool() {
		return (rand() % 2) == 0;
	}
	int randomInt(int min, int max) {
		return rand() % (max - min + 1) + min;
	}
};