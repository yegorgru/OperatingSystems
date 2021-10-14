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
	ChildProcess mProcess;
	std::string mCommand;
	InStream mInStream;
	OutStream mOutStream;
};

