#pragma once

#include <algorithm>
#include <string>

#include <boost/process.hpp>

class ProcessWrapper
{
public:
	using ChildProcess = boost::process::child;
	using InStream = boost::process::opstream;
	using OutStream = boost::process::ipstream;
public:
	ProcessWrapper(const std::string& command);
public:
	void start();
	bool running();
public:
	template<typename T>
	void writeToInStream(const T& value)
	{
		mInStream << value << std::endl;
	}

	template<typename T>
	T readFromOutStream()
	{
		T value;
		mOutStream >> value;
		return value;
	}
private:
	ChildProcess mProcess;
	std::string mCommand;
	InStream mInStream;
	OutStream mOutStream;
};

