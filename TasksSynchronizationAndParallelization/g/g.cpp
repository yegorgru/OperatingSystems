#include <iostream>
#include "../lab1_cpp/trialfuncs.hpp"

int main()
{
    static_assert(std::is_same<int, os::lab1::compfuncs::op_group_traits<os::lab1::compfuncs::INT_SUM>::value_type>(), "wrong typing for INT_SUM");
    int x;
    std::cin >> x;
    auto result = os::lab1::compfuncs::trial_g<os::lab1::compfuncs::INT_SUM>(x);
    if (std::holds_alternative<int>(result)) {
        std::cout << 0 << std::endl << std::get<int>(result) << std::endl;
    }
    else if (std::holds_alternative<os::lab1::compfuncs::soft_fail>(result)) {
        std::cout << 1;
    }
    else {
        std::cout << 2;
    }
    return 0;
}