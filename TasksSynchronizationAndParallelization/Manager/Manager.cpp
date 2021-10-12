
#include <algorithm>

#include <vector>
#include <iterator>
#include <iostream>

#include <boost/process.hpp>

//using namespace boost::process;

int main()
{
    boost::process::child c1("f.exe");
    boost::process::child c2("g.exe");
    c1.wait();
    c2.wait();
}