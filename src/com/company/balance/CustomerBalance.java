package com.company.balance;

import java.util.Scanner;
import java.util.UUID;

public class CustomerBalance extends Balance{

    public CustomerBalance(UUID customerId, Double balance) {
        super(customerId, balance);
    }

    @Override
    public Double addBalance(int selection) {
        if (selection==1){
            System.out.println("How much you would like to add?");
            double additionalAmount = new Scanner(System.in).nextDouble();
            setBalance(getBalance() + additionalAmount);
            System.out.println("New customer balance: " + getBalance());
        }
        return getBalance();
    }
}
