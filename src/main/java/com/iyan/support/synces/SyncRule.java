package com.iyan.support.synces;

public class SyncRule {

	private String esIndex;
	private String esType;
	private String esField;
	
	public SyncRule(String index, String type,String field) {
		this.esIndex = index;
		this.esType = type;
		this.esField = field;
	}
	
	public String getType() {
		return esType;
	}
	public void setType(String type) {
		this.esType = type;
	}
	public String getIndex() {
		return esIndex;
	}
	public void setIndex(String index) {
		this.esIndex = index;
	}

	public String getField() {
		return esField;
	}

	public void setField(String esField) {
		this.esField = esField;
	}
	
}
