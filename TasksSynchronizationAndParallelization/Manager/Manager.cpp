
#include <algorithm>

#include <vector>
#include <iterator>
#include <iostream>

#include <boost/process.hpp>

int main()
{
    namespace bp = boost::process;
    bp::opstream inf;
    bp::ipstream outf;
    bp::opstream ing;
    bp::ipstream outg;

    bp::child cf("f.exe", bp::std_out > outf, bp::std_in < inf);
    bp::child cg("g.exe", bp::std_out > outg, bp::std_in < ing);

    inf << 5 << std::endl;
    ing << 5 << std::endl;
    int valuef, valueg;
    outf >> valuef;
    std::cout << valuef;
    outg >> valueg;
    std::cout << valueg;

    cf.wait();
    cg.wait();
}