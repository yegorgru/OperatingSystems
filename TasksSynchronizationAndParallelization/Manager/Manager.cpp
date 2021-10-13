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

    int valuef, valueg;
    outf >> valuef;
    std::cout << valuef;
    outg >> valueg;
    std::cout << valueg;
    
    cf.wait();
    cg.wait();
}