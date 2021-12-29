package com.annpurna.chaincode.model;

import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public class Wallet {

	@Property()
	private String id ;
	@Property()
	private long createdOn ;
	@Property()
	private String createdBy ;
	@Property()
	private String owner ;
	@Property()
	private Long value = 0L;
	/*@Property
	private Map<String,Long> allowed ; */
	@Property
	private String secret ;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Long getValue() {
		return this.value ;
	}
	
	public void setValue(Long value) {
		this.value = value;
	}
	/*
	public void addAllowed(String walletId, Long balance) {
		if(this.allowed == null) {
			this.allowed = new HashMap<String,Long> () ;
		}
		if(this.allowed.get(walletId) != null) {
			this.allowed.put(walletId, this.allowed.get(walletId) + balance) ;
		} else {
			this.allowed.put(walletId, balance);
		}
	}
	
	public Map<String, Long> getAllowed() {
		return allowed;
	}

	public void setAllowed(Map<String, Long> allowed) {
		this.allowed = allowed;
	}
*/
	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	@Override
	public String toString() {
		return "Wallet [id=" + id + ", createdOn=" + createdOn + ", createdBy=" + createdBy + ","
				+"]";
	}
	
}