#pragma once
# include <omp.h>
#include "Utils.h"
#include <time.h>

class RandomNumberChange
{
private:
	int number;

public:
	int run(int seconds, int threadCount) 
	{
		if (threadCount < 1 || seconds < 1) {
			return 0;
		}
		omp_set_dynamic(0);					// Ensure use of user specified thread count
		omp_set_num_threads(threadCount);	// Set number of threads to use as specified
		time_t start = time(0);
		time_t endTime = start + seconds;
		while (time(0) < endTime)
		{
			#pragma omp parallel for
			for (int i = 0; i < threadCount; i++)
			{
				if (Utils::randomBool())
				{
					#pragma omp atomic
					number++;
				}
				else
				{
					#pragma omp atomic
					number--;
				}
			}
		}

		return number;
	}

	RandomNumberChange()
	{
		number = 0;
	}
};