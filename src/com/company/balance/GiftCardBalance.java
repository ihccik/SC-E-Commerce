package com.company.balance;

import com.company.StaticConstants;

import java.util.UUID;

public class GiftCardBalance extends Balance implements SendMoney {


    public GiftCardBalance(UUID customerId, Double balance) {
        super(customerId, balance);
    }

    @Override
    public Double addBalance(Double additionalBalance) {
        double promotionAmount = additionalBalance * 10 / 100;
        setBalance(getBalance() + additionalBalance + promotionAmount);
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
//                        System.out.println(balance.getBalance());
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
