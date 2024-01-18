#include <iostream>
#include "RandomNumberChange.h"
#include "BankingSystem.h"

int main()
{
	srand((unsigned int)time(NULL)); // set seed for random number generator
	RandomNumberChange rnc;
	int result = rnc.run(1, 2);
	std::cout << "RandomNumberChange result: " << result << std::endl;
	BankingSystem bs(5, 1000);
	std::vector<Account> resultBank = bs.run(1);
	//print number and blance of each account:
	for (int i = 0; i < resultBank.size(); i++)
	{
		std::cout << "Account " << resultBank[i].getAccountNumber() << " has balance " << resultBank[i].getBalance() << std::endl;
	}

    
}
