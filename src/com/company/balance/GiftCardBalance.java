package com.company.balance;

import com.company.StaticConstants;

import java.util.Scanner;
import java.util.UUID;

public class GiftCardBalance extends Balance implements SendMoney {


    public GiftCardBalance(UUID customerId, Double balance) {
        super(customerId, balance);
    }

    @Override
    public Double addBalance(int selection) {
        if (selection==2){
            System.out.println("How much you would like to add?");
            double additionalAmount = new Scanner(System.in).nextDouble();
            double promotionAmount = additionalAmount * 10 / 100;
            setBalance(getBalance() + additionalAmount + promotionAmount);
            System.out.println("New giftCard balance: " + getBalance());
        }

        return getBalance();
    }

    @Override
    public Double sendMoney(UUID id, Double desiredAmount) {
        if (desiredAmount > 0) {
            double newGiftCardBalance = 0.0;
            for (Balance balance : StaticConstants.GIFT_CARD_BALANCE_LIST) {

                if (balance.getCustomerId().toString().equals(id.toString())) {
                    if (!(getBalance() >= desiredAmount)) {
                        System.err.println("Insufficient balance, Please try again!");
                        newGiftCardBalance = getBalance();

                    } else {
                        balance.setBalance(balance.getBalance() + desiredAmount);
//                      System.out.println(balance.getBalance());
                        setBalance(getBalance() - desiredAmount);
                        System.out.println("Your funds has been successfully transferred. Your new balance is: " + getBalance());
                    }
                }
            }

        } else {
            System.err.println("Invalid amount. Please enter valid amount : " + desiredAmount);
        }
        return getBalance();
    }

}
