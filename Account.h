#pragma once
#include <omp.h>

class Account
{
private:
	unsigned int balance;
	unsigned int accountNumber;
public:
	Account(const unsigned int accountNumber, const unsigned int startingBalance) 
	{
		this->accountNumber = accountNumber;
		this->balance = startingBalance;
	}
	Account(const unsigned int accountNumber)
	{
		this->accountNumber = 0;
		this->balance = 0;
	}

	unsigned int getBalance() const { return balance; }
	unsigned int getAccountNumber() const { return accountNumber; }
	void deposit(const unsigned int amount) 
	{ 
		#pragma omp critical(balanceChange)
		balance += amount; 
	}
	bool withdraw(const unsigned int amount)
	{
		if (amount > balance)
		{
			return false;
		}
		#pragma omp critical(balanceChange)
		balance -= amount;
		return true;
	}
	void transfer(Account& other, const unsigned int amount)
	{
		if (withdraw(amount))
		{
			other.deposit(amount);
		}
	}
};
