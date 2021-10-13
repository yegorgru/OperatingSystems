#include <algorithm>
#include <iostream>

#define WIN32_LEAN_AND_MEAN
#include <windows.h>

#include <boost/process.hpp>

int main()
{
    namespace bp = boost::process;

    int x;
    std::cout << "Enter x: " << std::endl;
    std::cin >> x;

    bp::opstream inf;
    bp::ipstream outf;
    bp::opstream ing;
    bp::ipstream outg;

    bp::child cf("f.exe", bp::std_out > outf, bp::std_in < inf);
    bp::child cg("g.exe", bp::std_out > outg, bp::std_in < ing);
    inf << x << std::endl;
    ing << x << std::endl;

    while (cf.running() || cg.running()) {
        if (GetAsyncKeyState(27) & 0x0001) {
            std::cout << ".";
        }
    }

    int fCode, gCode;
    outf >> fCode;
    outg >> gCode;
    if (fCode == 0 && gCode == 0) {
        int fResult, gResult;
        outf >> fResult;
        outg >> gResult;
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
    cf.wait();
    cg.wait();
}