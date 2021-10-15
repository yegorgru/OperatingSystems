#include "ProcessWrapper.h"

class Manager
{
public:
	Manager();
public:
	void run();
private:
	enum class ConfirmationResult {
		Yes,
		No,
		Timeout
	};
private:
	void performSingleComputation(int x, uint32_t amountOfAttempts);
	bool reportResult();
	ConfirmationResult confirmation(const std::string& message, uint32_t seconds);
private:
	ProcessWrapper mF;
	ProcessWrapper mG;
};