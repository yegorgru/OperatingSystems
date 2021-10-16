#ifndef _OS_LAB1_PROCESSWRAPPER_H_
#define _OS_LAB1_PROCESSWRAPPER_H_

#include <algorithm>		//include to prevent problems with namespace std inside boost
#include <string>

#include <boost/process.hpp>

namespace OS::Lab1
{

class ProcessWrapper
{
public:
	ProcessWrapper(const std::string& command);
	ProcessWrapper(const ProcessWrapper&) = delete;
	ProcessWrapper(ProcessWrapper&&) = delete;
	~ProcessWrapper() = default;
	ProcessWrapper& operator= (const ProcessWrapper&) = delete;
	ProcessWrapper& operator= (ProcessWrapper&&) = delete;
public:
	void start();
	bool running();
	void terminate();
public:
	template<typename T>
	void write(const T& value)
	{
		mInStream << value << std::endl;
	}
	template<typename T>
	T read()
	{
		T value;
		mOutStream >> value;
		return value;
	}
private:
	using Process = boost::process::child;
	using InStream = boost::process::opstream;
	using OutStream = boost::process::ipstream;
private:
	Process mProcess;
	std::string mCommand;
	InStream mInStream;
	OutStream mOutStream;
};

} //OS::Lab1

#endif //_OS_LAB1_PROCESSWRAPPER_H_
