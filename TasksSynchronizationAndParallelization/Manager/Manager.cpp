#include <algorithm>
#include <iostream>

#define WIN32_LEAN_AND_MEAN
#include <windows.h>

#include <boost/process.hpp>
#include <boost/thread.hpp>

#include "ProcessWrapper.h"

void reportResult(ProcessWrapper& f, ProcessWrapper& g) {
    auto fCode = f.read<int>();
    auto gCode = g.read<int>();
    if (fCode == 0 && gCode == 0) {
        auto fResult = f.read<int>();
        auto gResult = g.read<int>();
        std::cout << "Result: " << fResult + gResult << std::endl;
    }
    if (fCode == 1) {
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
    f.write<int>(x);
    g.write<int>(x);

    //todo : reset keyboard state before listening
    while (f.running() || g.running()) {
        if (GetAsyncKeyState(27) & 0x0001) {
            std::cout << "Please confirm that computation should be stopped y(es, stop)/n(ot yet) [n]" << std::endl;
            auto start = boost::chrono::steady_clock::now();
            while (true) {
                if (GetAsyncKeyState(89) & 0x0001) {
                    if (!f.running() && !g.running()) {
                        std::cout << "overridden by system" << std::endl;
                        reportResult(f, g);
                        exit(0);
                    }
                    std::cout << "Computation was cancled." << std::endl;
                    if (f.running()) {
                        std::cout << "f did not finish. " << std::endl;
                        f.terminate();
                    }
                    if(g.running()) {
                        std::cout << "g did not finish. " << std::endl;
                        g.terminate();
                    }
                    exit(0);
                }
                else if (GetAsyncKeyState(78) & 0x0001) {
                    std::cout << "Cancellation was not confirmed" << std::endl;
                    break;
                }
                auto now = boost::chrono::steady_clock::now();
                if (boost::chrono::duration_cast<boost::chrono::seconds>(now - start).count() > 5) {
                    std::cout << "Action is not confirmed within 5s proceeding..." << std::endl;
                    break;
                }
            }
        }
    }

    reportResult(f, g);
}