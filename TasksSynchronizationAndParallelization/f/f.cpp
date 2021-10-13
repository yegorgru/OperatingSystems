#include <iostream>
#include <string>
#include <thread>
#include <chrono>

int main()
{
    int x;
    std::cin >> x;
    std::this_thread::sleep_for(std::chrono::milliseconds(1000));
    std::cout << x;
    return 0;
}
