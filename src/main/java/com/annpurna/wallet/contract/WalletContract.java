package com.annpurna.wallet.contract;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.annpurna.chaincode.model.Wallet;
import com.annpurna.ledger.api.HlfShimOperations;
import com.annpurna.wallet.crypto.CryptoUtil;
import com.annpurna.wallet.exception.AnnpurnaWalletException;
import com.annpurna.wallet.utils.CommonUtils;
import com.annpurna.wallet.utils.JsonParser;


@Contract(
        name = "WalletContract",
        info = @Info(
                title = "Wallet Contract",
                description = "Wallet Contract for secure coin transfer."
                		+ "Create/Updates user wallet",
                version = "0.0.1",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
@Default
public class WalletContract implements ContractInterface , IERC20 , HlfShimOperations{
	
	private static final String ZUDEXO_MSP = "ZudexoMSP" ;
	
	private static final String CREATE_WALLET_EVENT = "CREATE_WALLET" ;
	private static final String ADD_FUND_EVENT = "ADD_FUND";

	/**
	 * Create Wallet - this is a account of user on blockchain
	 * This should be restricted to zudexo or zudexo and fci both
	 * @param ctx
	 * @param assetJson
	 * @return
	 */
	@Transaction(intent = Transaction.TYPE.SUBMIT, name="CreateWallet")
    public String createWallet(final Context ctx , String walletJson , String signature) {
         try {
        	System.out.println("Initiator: " + getTxInitatorUserID(ctx));
         	Wallet wallet = JsonParser.deserialize(walletJson, Wallet.class) ;
         	assertAsset(wallet);
         
		 	if (stateExists(ctx, wallet.getId())) {
	            String errorMessage = String.format("Wallet %s already exists", wallet.getId());
	            System.out.println(errorMessage);
	            throw new AnnpurnaWalletException(errorMessage, AnnpurnaWalletException.FailureCause.ASSET_ALREADY_EXIST);
	        }
		 	assertSignature(wallet,signature);
		 	
		 	wallet.setValue(0L);
		 	
	    	final String ownerOrg = getClientOrgId(ctx);
	        System.out.println("client org:"+ownerOrg);
	        wallet.setCreatedBy(ownerOrg);

	        byte[] walletBytes = JsonParser.serialize(wallet).getBytes();
	        
	        System.out.println("Wallet Bytes size:"+ walletBytes.length);
	        System.out.println("Wallet Json:"+ new String(walletBytes));
	        ctx.getStub().setEvent(CREATE_WALLET_EVENT,walletBytes );
	        
			Wallet respWallet = updateAssetInLeger(ctx,wallet) ;
	     //   wallet.setSecret(null);
	        //wallet.setOwner(null);
			String responseStr = JsonParser.serialize(wallet);
			System.out.println("Response:"+responseStr);
			return responseStr;
	        
         } catch (AnnpurnaWalletException e){
        	 System.out.println("Error: "+e.getMessage());
        	 throw new ChaincodeException(e.getMessage(),e.getFailureCause().toString()) ;
         } catch (Exception e){
        	 throw new ChaincodeException("Unexpected Exception Occured",e) ;
         }
	} 
	
	@Transaction(intent = Transaction.TYPE.SUBMIT, name="CreatePartnerWallet")
    public String createPartnerWallet(final Context ctx , String orgMspId) {
		try {
			String clientOrgMspId = getClientOrgId(ctx);
			if(!ZUDEXO_MSP.equals(clientOrgMspId)) {
				String errorMessage = String.format("Only %s is allowed to create Parter Wallet not %s",ZUDEXO_MSP,orgMspId);
			     System.out.println(errorMessage);
			     throw new AnnpurnaWalletException(errorMessage, AnnpurnaWalletException.FailureCause.AUTH_FAILURE);
			}
			Wallet wallet = new Wallet() ;
			wallet.setId(orgMspId);
			wallet.setOwner(orgMspId);
			wallet.setValue(0L);
			wallet.setCreatedBy(clientOrgMspId);
			wallet.setCreatedOn(0L);
	        byte[] walletBytes = JsonParser.serialize(wallet).getBytes();
	        
	        System.out.println("Wallet Bytes size:"+ walletBytes.length);
			ctx.getStub().setEvent(CREATE_WALLET_EVENT, walletBytes);
			
			Wallet respWallet = updateAssetInLeger(ctx,wallet) ;
			String responseStr = JsonParser.serialize(respWallet);
			System.out.println("Response:"+responseStr);
			return responseStr;
		} catch(AnnpurnaWalletException e) {
			 System.out.println("Error: "+e.getMessage());
        	 throw new ChaincodeException(e.getMessage(),e.getFailureCause().toString()) ;
		}
	}
	
	@Transaction(intent = Transaction.TYPE.SUBMIT, name="AddFunds")
    public String addFunds(final Context ctx , Long value) {
		try {
			System.out.println("Adding funds to wallet");
			if(value == null || value < 0) {
				 throw new AnnpurnaWalletException("Invalid value argument:" + value,
						 AnnpurnaWalletException.FailureCause.INVALID_INPUT);
			}
			String orgMspId = getClientOrgId(ctx);
			if(!ZUDEXO_MSP.equals(orgMspId)) {
				String errorMessage = String.format("Only %s is allowed to create Parter Wallet not %s",
						ZUDEXO_MSP,orgMspId);
			     throw new AnnpurnaWalletException(errorMessage, 
			    		 AnnpurnaWalletException.FailureCause.AUTH_FAILURE);
			}
			Wallet wallet = readWallet(ctx,orgMspId);
			wallet.setValue(wallet.getValue() + value );
			
			ctx.getStub().setEvent(ADD_FUND_EVENT, ("Added Fund").getBytes());
			System.out.println("Funds added to wallet");
			Wallet respWallet =  updateAssetInLeger(ctx,wallet) ;
			String responseStr = JsonParser.serialize(respWallet);
			System.out.println("Response:"+responseStr);
			return responseStr;
		} catch(AnnpurnaWalletException e) {
			 System.out.println("Error: "+e.getMessage());
        	 throw new ChaincodeException(e.getMessage(),e.getFailureCause().toString()) ;
		}

		
	}
	
	/**
	 * Deprecated for the moment
	 * @param ctx
	 * @param assetJson
	 * @param signatue
	 * @return
	 *
	@Transaction(intent = Transaction.TYPE.SUBMIT, name="UpdateWallet")
    public Wallet updateWallet(final Context ctx , String assetJson, String signatue) {
         try {
        	System.out.println("Initiator: "+getTxInitatorUserID(ctx));
         	Wallet wallet = JsonParser.deserialize(assetJson, Wallet.class) ;
         	assertAsset(wallet);
         	assertSignature(wallet, signatue);
         	
		 	return updateWallet(ctx,wallet) ;
	        
         } catch (AnnpurnaWalletException e){
        	 System.out.println("Error: "+e.getMessage());
        	 throw new ChaincodeException(e.getMessage(),e.getFailureCause().toString()) ;
         } catch (Exception e){
        	 throw new ChaincodeException("Unexpected Exception Occured",e) ;
         }
	} 
	*/
	private Wallet updateWallet(Context ctx, Wallet wallet) {
	 	if (!stateExists(ctx, wallet.getId())) {
            String errorMessage = String.format("Wallet %s does not exists", wallet.getId());
            System.out.println(errorMessage);
            throw new AnnpurnaWalletException(errorMessage, AnnpurnaWalletException.FailureCause.NOT_FOUND);
        }
		return updateAssetInLeger(ctx,wallet) ;
	}
	
	/**
     * Retrieves an wallet with the given walletid.
     *
     * @param ctx the transaction context
     * @param assetId the id of the asset
     * @return the asset found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE,name="ReadWallet")
    public Wallet readWallet(final Context ctx, final String walletId,String signature) {
    	try {
    		System.out.println("Reading Wallet");
    		Wallet wallet = readWallet(ctx,walletId);
    		/* Signature validation is only performed for non network member (partner organizations) users */
    		System.out.println("Wallet Json:"+ new String(JsonParser.serialize(wallet).getBytes()));
    		if(!getClientOrgId(ctx).equals(walletId)) {
    	        assertSignature(wallet,signature);
    		}
	        
	        return wallet;
    	} catch (AnnpurnaWalletException e){
       	 System.out.println("Error Occured: "+e);
       	 e.printStackTrace();
       	 throw new ChaincodeException(e.getMessage(),e.getFailureCause().toString()) ;
        } catch (Exception e){
       	 throw new ChaincodeException("Unexpected Exception Occured",e) ;
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE,name="GetWalletHistory")
    public Wallet[] readWalletHistory(final Context ctx, final String walletId,String signature) {
    	System.out.println("Invoking get wallet history for :"+walletId);
		Wallet wallet = readWallet(ctx,walletId);
		/* Signature validation is only performed for non network member (partner organizations) users */
		if(!getClientOrgId(ctx).equals(walletId)) {
	        assertSignature(wallet,signature);
		}
		List<Wallet>  history = new ArrayList<Wallet> ();
		
		QueryResultsIterator<KeyModification> iter = ctx.getStub().getHistoryForKey(wallet.getId());
        
		Iterator<KeyModification> historyIter = iter.iterator() ;
		
		while (historyIter.hasNext()) {
			KeyModification item = historyIter.next() ;
			Wallet walletUpdate = JsonParser.deserialize(item.getStringValue(), Wallet.class) ;
			history.add(walletUpdate);
		}
		
		try {
			iter.close();
		} catch (Exception e) {
			String errorMessage = String.format("Unexpected exception while signature verification for wallet %s", wallet.getId());
			 System.out.println(errorMessage);
		     throw new AnnpurnaWalletException(errorMessage, AnnpurnaWalletException.FailureCause.UNEXPECTED_EXCEPTION,e);
		}
		
		System.out.println("Found History Txs :"+history.size());
		
		Wallet[] walletArrHistory = new Wallet[history.size()];
		
		for(int i =0 ;i < history.size() ; i++) {
			walletArrHistory[i] = history.get(i) ;
		}
		System.out.println("History parsed to array from list") ;
		return walletArrHistory;
    }
    
   private Wallet readWallet(final Context ctx, final String walletId) {
       String walletJSON = readStateAsString(ctx,walletId);
   	
       if (CommonUtils.isBlank(walletJSON)) {
           String errorMessage = String.format("Wallet %s does not exist", walletId);
           System.out.println(errorMessage);
           throw new ChaincodeException(errorMessage, AnnpurnaWalletException.FailureCause.NOT_FOUND.toString());
       }
       
       return JsonParser.deserialize(walletJSON,Wallet.class);
   }
    

    /**
     * Checks if asset provided is a valid asset
     * @throws AnnpurnaWalletException 
     */
    private void assertAsset (Wallet wallet) throws AnnpurnaWalletException {
    	if(CommonUtils.isBlank(wallet) || CommonUtils.isBlank(wallet.getId()) 
    			|| CommonUtils.isBlank(wallet.getOwner()) ){
    		throw new AnnpurnaWalletException("Invalid Asset definition - mandatory attributes missing",AnnpurnaWalletException.FailureCause.ILLEGAL_ASSET) ;
    	}
   	}
    
   

    
    private Wallet updateAssetInLeger (Context ctx, Wallet asset) {
        updateStateInLeger(ctx,asset.getId(), asset);
        return asset ;
    }
    


	@Override
	public void totalSupply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Long balanceOf(Wallet wallet) {
		return wallet.getValue() ;
	}

	
	@Transaction(intent = Transaction.TYPE.EVALUATE, name="BalanceOf")
	public Long balanceOf(Context ctx ,String walletId, String signature) {
		Wallet wallet = readWallet(ctx,walletId,signature);
		return balanceOf(wallet);
	}
	
	/**
	 * Transaction can be invoked by User. Tokens are transfered from user to recipient.
	 * This can be invoked when user buys product or service from partner
	 * @param ctx
	 * @param signature
	 * @param senderWalletId
	 * @param recipientWalletId
	 * @param amount
	 */
	@Transaction(intent = Transaction.TYPE.SUBMIT, name="Transfer")
	public void transfer(Context ctx ,String signature ,String senderWalletId, String recipientWalletId, long amount ) {
		Wallet walletSender = readWallet(ctx,senderWalletId);
		
		assertSignature(walletSender, signature);
	    
	    Wallet walleRecipent = readWallet(ctx,recipientWalletId);
	    transfer(walleRecipent,amount);
	    walletSender.setValue(walletSender.getValue() - amount);
	    
	    updateWallet(ctx, walleRecipent) ;
	    updateWallet(ctx, walletSender) ;
		
	}
	
	/**
	 * Transaction can be invoked by network member. Tokens are transfered from Parter to recipient.
	 * This can be used for recharging user wallet ,refunding token to user, sending tokens between partners.
	 * @param ctx
	 * @param recipientWalletId
	 * @param amount
	 */
	@Transaction(intent = Transaction.TYPE.SUBMIT, name="TransferTo")
	public void transferTo(Context ctx , String recipientWalletId, long amount ) {
		Wallet walletSender = readWallet(ctx,getClientOrgId(ctx));
	    
	    Wallet walleRecipent = readWallet(ctx,recipientWalletId);
	    transfer(walleRecipent,amount);
	    walletSender.setValue(walletSender.getValue() - amount);
	    
	    updateWallet(ctx, walleRecipent) ;
	    updateWallet(ctx, walletSender) ;
		
	}
	
	@Override
	public void transfer(Wallet recipient, long amount) {
		recipient.setValue(recipient.getValue() + amount);
	}

	@Transaction(intent = Transaction.TYPE.EVALUATE, name="Allowance")
	public Long allowance(Context ctx ,String walletIdOwner, String walletIdSpender, String signature) {
		Wallet walletSender = readWallet(ctx,walletIdOwner);
		Wallet walletRecipient = readWallet(ctx,walletIdSpender);
		
		return allowance(walletSender,walletRecipient);
	}
	
	@Override
	public Long allowance(Wallet walletSender, Wallet walletRecipient) {
		return null;//walletSender.getAllowed().get(walletRecipient.getId());
	}

	@Override
	public boolean approve(Wallet spender,String recipient, long amount) {
		//spender.addAllowed(recipient, amount);
		return true;
	}

	@Transaction(intent = Transaction.TYPE.SUBMIT, name="Approve")
	public Boolean approve(Context ctx ,String signature ,String ownerWalletId, String spenderWalletId, long amount ) {
		Wallet walletSender = readWallet(ctx,ownerWalletId);
		assertSignature(walletSender, signature);
		boolean result = approve(walletSender, spenderWalletId, amount);
		updateWallet(ctx, walletSender) ;
		return result;
	}
	
	@Override
	public void transferFrom(Wallet sender, Wallet recipient, long amount) {
		long senderBalance = sender.getValue() ;
		
		if(amount >= 0 && senderBalance > 0 && senderBalance >= amount) {
			recipient.setValue(recipient.getValue() + amount);
			sender.setValue(sender.getValue() - amount);
		}
	}
	
	@Transaction(intent = Transaction.TYPE.SUBMIT, name="TransferFrom")
	public void transferFrom(Context ctx ,String signature ,String sender, String recipient, long amount ) {
		Wallet walletSender = readWallet(ctx,sender);
		Wallet walletRecipient = readWallet(ctx,recipient);
		
		transferFrom(walletSender,walletRecipient,amount);
		
		updateWallet(ctx, walletSender) ;
		updateWallet(ctx, walletRecipient) ;
	}
	
	private static void assertSignature(Wallet wallet,String signature){
	 	try {
			if(!CryptoUtil.verifySinature(CryptoUtil.base64Decoded(signature),
					wallet.getId().getBytes(),CryptoUtil.generateX509EncodedPublicKey( CryptoUtil.base64Decoded(wallet.getOwner())))) {
				 String errorMessage = String.format("Signature verification failed", wallet.getId());
			     System.out.println(errorMessage);
			     throw new AnnpurnaWalletException(errorMessage, AnnpurnaWalletException.FailureCause.AUTH_FAILURE);
			}
		} catch (Exception e) {
			 String errorMessage = String.format("Unexpected exception while signature verification for wallet %s", wallet.getId());
			 System.out.println(errorMessage);
		     throw new AnnpurnaWalletException(errorMessage, AnnpurnaWalletException.FailureCause.UNEXPECTED_EXCEPTION,e);
		}
	}
}

