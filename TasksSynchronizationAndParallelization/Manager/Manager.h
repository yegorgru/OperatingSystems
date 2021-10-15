#include "ProcessWrapper.h"

class Manager
{
public:
	Manager();
public:
	void run();
private:
	void performSingleComputation(int x, uint32_t amountOfAttempts);
	bool processResults();
private:
	ProcessWrapper mF;
	ProcessWrapper mG;
	bool mFComputed;
	bool mGComputed;
};