package com.annpurna.wallet.exception;

public class AnnpurnaWalletException extends RuntimeException {

	private FailureCause failureCause ;
	public AnnpurnaWalletException(String errorMessage, FailureCause failureCause) {
		super(errorMessage);
		this.failureCause = failureCause ;
	}

	public AnnpurnaWalletException(String errorMessage, FailureCause failureCause,Exception e) {
		super(errorMessage,e);
		this.failureCause = failureCause ;
	}
	
	public FailureCause getFailureCause() {
		return failureCause;
	}


	public void setFailureCause(FailureCause failureCause) {
		this.failureCause = failureCause;
	}


	public enum FailureCause {
		AUTH_FAILURE,
		ASSET_ALREADY_EXIST,
		INCOMPLETE_INPUT,
		INVALID_INPUT,
		OPERATION_NOT_PERMITTED,
		ILLEGAL_ASSET,
		NOT_FOUND,
		UNEXPECTED_EXCEPTION;
	}
}
