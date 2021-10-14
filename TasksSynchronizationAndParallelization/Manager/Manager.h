#include "ProcessWrapper.h"

class Manager
{
public:
	Manager();
public:
	void run();
private:
	void reportResult();
private:
	ProcessWrapper mF;
	ProcessWrapper mG;
};