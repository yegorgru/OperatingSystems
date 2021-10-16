#ifndef _OS_LAB1_MANAGER_H_
#define _OS_LAB1_MANAGER_H_

#include "ProcessWrapper.h"

#include <utility>

namespace OS::Lab1
{

class Manager
{
public:
	Manager();
	Manager(const Manager&) = delete;
	Manager(Manager&&) = delete;
	~Manager() = default;
	Manager& operator= (const Manager&) = delete;
	Manager& operator= (Manager&&) = delete;
public:
	void run();
private:
	enum class Code {
		Undefined = -1,
		Value,
		SoftFail,
		HardFail
	};
	using Value = int;
	using Results = std::pair<Code, Value>;
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

} //OS::Lab1

#endif //_OS_LAB1_MANAGER_H_