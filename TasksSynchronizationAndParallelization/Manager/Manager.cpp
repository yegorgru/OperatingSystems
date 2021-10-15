#include "Manager.h"

#include <iostream>
#include <sstream>

#include <boost/chrono.hpp>

#define WIN32_LEAN_AND_MEAN
#include <windows.h>

namespace {
    void resetKeyStates() {
        std::vector<char> keys{ 78, 89, 27, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57 };
        for (auto key : keys) {
            GetAsyncKeyState(key) & 0x0001;
        }
    }

    template <typename T>
    T readNumberInput()
    {
        resetKeyStates();
        std::stringstream ss;
        while (!(GetAsyncKeyState(13) & 0x0001)) {
            for (int i = 48; i <= 57; i++) {
                if (GetAsyncKeyState(i) & 0x0001) {
                    char cur = static_cast<char>(i);
                    ss << cur;
                    std::cout << cur;
                }
            }
            if ((GetAsyncKeyState(8) & 0x0001) && ss.str().size() != 0) {
                std::cout << "\b \b";
            }
        }
        std::cout << std::endl;
        T result = 0;
        ss >> result;
        return result;
    }
}

Manager::Manager()
    : mF("f.exe")
    , mG("g.exe")
    , mFComputed(false)
    , mGComputed(false)
{

}

void Manager::run()
{
    while (true) {
        std::cout << "Enter x: " << std::endl;
        auto x = readNumberInput<int>();

        std::cout << "Enter amount of attempts in case of soft fail: " << std::endl;
        auto amountOfAttempts = readNumberInput<uint32_t>();
        amountOfAttempts = amountOfAttempts == 0 ? 1 : amountOfAttempts;

        performSingleComputation(x, amountOfAttempts);

        switch (confirmation("Please confirm performing of next computation y(es, perform)/n(o, exit) [n]", 10)) {
        case ConfirmationResult::Yes: {
            continue;
        }
        case ConfirmationResult::No: {
            return;
        }
        case ConfirmationResult::Timeout: {
            std::cout << "Next computation is not confirmed within 10s, closing" << std::endl;
            return;
        }
        }
    }
}

void Manager::performSingleComputation(int x, uint32_t amountOfAttempts)
{
    mFComputed = false;
    mGComputed = false;
    for (uint32_t i = 0; i < amountOfAttempts; i++) {
        std::cout << "Computations have started. Attempt #" << i + 1 << std::endl;
        if (!mFComputed) {
            mF.start();
            mF.write<int>(x);
        }
        if (!mGComputed) {
            mG.start();
            mG.write<int>(x);
        }
        resetKeyStates();
        while (mF.running() || mG.running()) {
            if (GetAsyncKeyState(27) & 0x0001) {
                switch (confirmation("Please confirm that computation should be stopped y(es, stop)/n(ot yet) [n]", 5)) {
                case ConfirmationResult::Yes: {
                    if (!mF.running() && !mG.running()) {
                        std::cout << "overridden by system" << std::endl;
                    }
                    else {
                        std::cout << "Computation was canceled." << std::endl;
                    }
                    if (mF.running()) {
                        std::cout << "f did not finish. " << std::endl;
                        mF.terminate();
                    }
                    if (mG.running()) {
                        std::cout << "g did not finish. " << std::endl;
                        mG.terminate();
                    }
                    break;
                }
                case ConfirmationResult::No: {
                    std::cout << "Action was not confirmed" << std::endl;
                    break;
                }
                case ConfirmationResult::Timeout: {
                    std::cout << "Action is not confirmed within 5s proceeding..." << std::endl;
                    break;
                }
                }
            }
        }

        if (processResults()) {
            return;
        }
    }
}

bool Manager::processResults() {
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
    mFComputed = fCode != 1;
    mGComputed = gCode != 1;
    return fCode != 1 && gCode != 1;
}

Manager::ConfirmationResult Manager::confirmation(const std::string& message, uint32_t seconds)
{
    resetKeyStates();
    std::cout << message << std::endl;
    auto start = boost::chrono::steady_clock::now();
    while (true) {
        if (GetAsyncKeyState(89) & 0x0001) {
            return ConfirmationResult::Yes;
        }
        else if (GetAsyncKeyState(78) & 0x0001) {
            return ConfirmationResult::No;
        }
        else {
            auto now = boost::chrono::steady_clock::now();
            if (boost::chrono::duration_cast<boost::chrono::seconds>(now - start).count() > seconds) {
                return ConfirmationResult::Timeout;
            }
        }
    }
}