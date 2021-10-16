#include "ProcessWrapper.h"

#include <utility>

class Manager
{
public:
	Manager();
public:
	void run();
private:
	using Results = std::pair<int, int>;
private:
	void performSingleComputation(int x, uint32_t amountOfAttempts);
	void reportResults();
	void getResults(bool f);
private:
	ProcessWrapper mF;
	ProcessWrapper mG;
	Results mFResults;
	Results mGResults;
};