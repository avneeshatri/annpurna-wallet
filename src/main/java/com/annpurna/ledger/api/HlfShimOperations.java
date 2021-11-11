package com.annpurna.ledger.api;

import java.util.Map;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.impl.StateBasedEndorsementFactory;

import com.annpurna.wallet.exception.AnnpurnaWalletException;
import com.annpurna.wallet.utils.CommonUtils;
import com.annpurna.wallet.utils.JsonParser;

public interface HlfShimOperations {
	

    /**
     * Checks the existence of an state.
     *
     * @param ctx the transaction context
     * @param stateId the id of the asset
     * @return boolean indicating the existence of the asset
     */
    default boolean stateExists(final Context ctx, final String stateId) {
        ChaincodeStub stub = ctx.getStub();
        String stateJson = stub.getStringState(stateId);
        return !CommonUtils.isBlank(stateJson);
    }
    
    default boolean updateStateInLeger (Context ctx, String stateId, Object state) {
        ChaincodeStub stub = ctx.getStub();
        String stateJson = JsonParser.serialize(state);
        stub.putStringState(stateId, stateJson);
        return true;
    }
    
    default boolean updatePrivateData (Context ctx, String clientPrivateCollectionName,
    		String stateId, Object privateData) {
        String stateJson = JsonParser.serialize(privateData);
        ctx.getStub().putPrivateData(clientPrivateCollectionName, stateId, stateJson);
        return true;
    }
    
    /**
     * Retrieves the client's OrgId (MSPID)
     *
     * @param ctx the transaction context
     * @return String value of the Org MSPID
     */
    default String getClientOrgId(final Context ctx) {
        return ctx.getClientIdentity().getMSPID();
    }
    
    default String getTxInitatorUserID(Context ctx){
    	return new String(ctx.getStub().getCreator());
    }
    
    default void setPrivateStateBasedEndorsement(final Context ctx, final String stateId, final String[] ownerOrgs, String collectionName) {
        StateBasedEndorsement stateBasedEndorsement = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(null);
        stateBasedEndorsement.addOrgs(StateBasedEndorsement.RoleType.RoleTypeMember, ownerOrgs);
        ctx.getStub().setPrivateDataValidationParameter(collectionName, stateId, stateBasedEndorsement.policy());
    } 
    
    

    /**
     * Sets an endorsement policy to the assetId Key.
     * Enforces that the owner Org must endorse future update transactions for the specified assetId Key.
     *
     * @param ctx the transaction context
     * @param assetId the id of the asset
     * @param ownerOrgs the list of Owner Org MSPID's
     */
    default void setStateBasedEndorsement(final Context ctx, final String assetId, final String[] ownerOrgs) {
        StateBasedEndorsement stateBasedEndorsement = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(null);
        stateBasedEndorsement.addOrgs(StateBasedEndorsement.RoleType.RoleTypeMember, ownerOrgs);
        ctx.getStub().setStateValidationParameter(assetId, stateBasedEndorsement.policy());
    }
    
    
    default byte[]  getTxTransitentProperties(Context ctx , String privateDataPropertiesKey, boolean privateDataCheck){
    	Map<String, byte[]> transientMap = ctx.getStub().getTransient();
        if (privateDataCheck && !transientMap.containsKey(privateDataPropertiesKey)) {
            String errorMessage = String.format("Call must specify s% in Transient map input",privateDataPropertiesKey);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AnnpurnaWalletException.FailureCause.INCOMPLETE_INPUT.toString());
        }
        return transientMap.get(privateDataPropertiesKey);
    }
    
    default byte[] readState(final Context ctx, final String stateId) {
        ChaincodeStub stub = ctx.getStub();
        return stub.getState(stateId);
    }
    
    default String readStateAsString(final Context ctx, final String stateId) {
        ChaincodeStub stub = ctx.getStub();
        return stub.getStringState(stateId);
    }
}
