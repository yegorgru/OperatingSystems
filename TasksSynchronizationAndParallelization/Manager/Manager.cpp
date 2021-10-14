#include "Manager.h"

#include <iostream>

#include <boost/chrono.hpp>

#define WIN32_LEAN_AND_MEAN
#include <windows.h>

Manager::Manager()
    : mF("f.exe")
    , mG("g.exe")
{

}

void Manager::run()
{
    int x;
    std::cout << "Enter x: " << std::endl;
    std::cin >> x;

    mF.start();
    mG.start();
    mF.write<int>(x);
    mG.write<int>(x);

    //todo : reset keyboard state before listening
    while (mF.running() || mG.running()) {
        if (GetAsyncKeyState(27) & 0x0001) {
            std::cout << "Please confirm that computation should be stopped y(es, stop)/n(ot yet) [n]" << std::endl;
            auto start = boost::chrono::steady_clock::now();
            while (true) {
                if (GetAsyncKeyState(89) & 0x0001) {
                    if (!mF.running() && !mG.running()) {
                        std::cout << "overridden by system" << std::endl;
                        reportResult();
                        exit(0);
                    }
                    std::cout << "Computation was cancled." << std::endl;
                    if (mF.running()) {
                        std::cout << "f did not finish. " << std::endl;
                        mF.terminate();
                    }
                    if (mG.running()) {
                        std::cout << "g did not finish. " << std::endl;
                        mG.terminate();
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

    reportResult();
}

void Manager::reportResult() {
    auto fCode = mF.read<int>();
    auto gCode = mG.read<int>();
    if (fCode == 0 && gCode == 0) {
        auto fResult = mF.read<int>();
        auto gResult = mG.read<int>();
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