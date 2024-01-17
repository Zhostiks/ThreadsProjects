package threads;

import java.util.*;
import java.util.concurrent.locks.*;

// the program that simulates a bank
// with accounts and uses locks
// to organise sequential access
// to account balances


public class Bank
{

    private Lock bankLock = new ReentrantLock();
    // ReentrantLock class object
    // which implements interface Lock

    private final double[] accounts;

    // Building a Bank object
    // @param n is Number of accounts
    // @param initialBalance is initial account balance

    private Condition sufficientFunds;

    public Bank(int n, double initialBalance)
    {
        accounts = new double[n];
        Arrays.fill(accounts, initialBalance);
        bankLock = new ReentrantLock();
        sufficientFunds = bankLock.newCondition();
    }

    // Money transfer from one account to another
    // @param from is an account from which money is transfered
    // @param to is an account which get transfered money
    // @param amount is a sum of transfer

    public void transfer(int from, int to, double amount)
            throws InterruptedException
    {
        bankLock.lock(); // securing the method
        try
        {
            while (accounts[from] < amount)
                sufficientFunds.await();
            System.out.print(Thread.currentThread());
            accounts[from] -= amount;
            System.out.printf(" %10.2f from %d to %d ", amount, from, to);
            accounts[to] += amount;
            System.out.printf(" Total Balance: %10.2f%n ", getTotalBalance());
            sufficientFunds.signalAll();
        }
        finally
        {
            bankLock.unlock(); // release the lock even if
                               // an exception is thrown
        }

    }

    // Get the amount of balance from accounts
    // @return return total balance

    public double getTotalBalance()
    {
        bankLock.lock();
        try {
            double sum = 0;

            for (double a : accounts)
                sum += a;

            return sum;
        }
        finally
        {
            bankLock.unlock();
        }
    }

    // Get the amount of bank accounts
    // @return return accounts amount

    public int size()
    {
        return accounts.length;
    }

}