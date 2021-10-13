#include <algorithm>
#include <iostream>

#define WIN32_LEAN_AND_MEAN
#include <windows.h>

#include <boost/process.hpp>

#include "ProcessWrapper.h"

int main()
{
    namespace bp = boost::process;

    int x;
    std::cout << "Enter x: " << std::endl;
    std::cin >> x;

    ProcessWrapper f("f.exe");
    ProcessWrapper g("g.exe");

    f.start();
    g.start();
    f.writeToInStream<int>(x);
    g.writeToInStream<int>(x);

    while (f.running() || g.running()) {
        if (GetAsyncKeyState(27) & 0x0001) {
            std::cout << ".";
        }
    }

    auto fCode = f.readFromOutStream<int>();
    auto gCode = g.readFromOutStream<int>();
    if (fCode == 0 && gCode == 0) {
        auto fResult = f.readFromOutStream<int>();
        auto gResult = g.readFromOutStream<int>();
        std::cout << "Result: " << fResult + gResult;
    }
    if(fCode == 1){
        std::cout << "f function failed, soft" << std::endl;
    }
    if (gCode == 1) {
        std::cout << "g function failed, soft" << std::endl;
    }
    if (fCode == 2) {
        std::cout << "f function failed, hard" << std::endl;
    }
    if (gCode == 2) {
        std::cout << "g function failed, hard" << std::endl;
    }
}