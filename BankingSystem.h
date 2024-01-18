#pragma once
#include <vector>
#include <omp.h>
#include <time.h>
#include "Utils.h"
#include "Account.h"

const unsigned int maxTransferAmount = 100;
const unsigned int maxDepositAmount = 10;

class BankingSystem
{
public:
	std::vector<Account> accounts;
	BankingSystem(const unsigned int accountCount, const unsigned int startingBalance)
	{
		accounts = std::vector<Account>();
		for (unsigned int i = 0; i < accountCount; i++)
		{
			accounts.push_back(Account(i, startingBalance));
		}
	}
	std::vector<Account> run(unsigned int seconds) 
	{
		time_t start = time(0);
		time_t endTime = start + seconds;

		while (time(0) < endTime)
		{
			#pragma omp parallel for
			for (int i = 0; i < accounts.size(); i++)
			{
				Account& account = accounts[i];
				if (Utils::randomBool()) // either deposit/withdraw or make a transfer
				{
					if (Utils::randomBool()) // either deposit or withdraw
					{
						account.deposit(Utils::randomInt(1, maxDepositAmount));
					}
					else
					{
						account.withdraw(Utils::randomInt(1, maxDepositAmount));
					}
				}
				else
				{
					//transfer money to some other account (can be to myself, then nothing happens - this is by design)
					Account& other = accounts[Utils::randomInt(0, (int)accounts.size() - 1)];
					account.transfer(other, Utils::randomInt(1, maxTransferAmount));

				}
			}
		}
		return accounts;
	}
};
