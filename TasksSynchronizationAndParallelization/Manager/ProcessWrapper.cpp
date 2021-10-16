#include "ProcessWrapper.h"

#include <stdexcept>

namespace OS::Lab1
{

ProcessWrapper::ProcessWrapper(const std::string& command)
	: mCommand(command)
{

}

void ProcessWrapper::start()
{
	if (mProcess.running()) {
		mProcess.terminate();
	}
	mInStream = InStream();
	mOutStream = OutStream();
	mProcess = Process(mCommand, boost::process::std_out > mOutStream, boost::process::std_in < mInStream);
}

bool ProcessWrapper::running()
{
	return mProcess.running();
}

void ProcessWrapper::terminate()
{
	if (!mProcess.running()) {
		throw std::runtime_error("child process is not running");
	}
	mProcess.terminate();
}

}