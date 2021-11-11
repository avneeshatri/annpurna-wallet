package com.annpurna.wallet.contract;

import com.annpurna.chaincode.model.Wallet;

public interface IERC20 {

	void totalSupply();

	Long balanceOf(Wallet account);

	void transfer(Wallet recipient, long amount);

	Long allowance(Wallet owner, Wallet spender);

	boolean approve(Wallet spender,String recipent, long amount);

	void transferFrom(Wallet sender, Wallet recipient, long amount);

}
