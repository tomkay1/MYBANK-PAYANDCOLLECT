package com.mybank.pc.collection.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseUnionpayBatchCollectionQuery<M extends BaseUnionpayBatchCollectionQuery<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setTxnType(java.lang.String txnType) {
		set("txnType", txnType);
	}
	
	public java.lang.String getTxnType() {
		return getStr("txnType");
	}

	public void setTxnSubType(java.lang.String txnSubType) {
		set("txnSubType", txnSubType);
	}
	
	public java.lang.String getTxnSubType() {
		return getStr("txnSubType");
	}

	public void setBizType(java.lang.String bizType) {
		set("bizType", bizType);
	}
	
	public java.lang.String getBizType() {
		return getStr("bizType");
	}

	public void setChannelType(java.lang.String channelType) {
		set("channelType", channelType);
	}
	
	public java.lang.String getChannelType() {
		return getStr("channelType");
	}

	public void setAccessType(java.lang.String accessType) {
		set("accessType", accessType);
	}
	
	public java.lang.String getAccessType() {
		return getStr("accessType");
	}

	public void setMerId(java.lang.String merId) {
		set("merId", merId);
	}
	
	public java.lang.String getMerId() {
		return getStr("merId");
	}

	public void setBatchNo(java.lang.String batchNo) {
		set("batchNo", batchNo);
	}
	
	public java.lang.String getBatchNo() {
		return getStr("batchNo");
	}

	public void setTxnTime(java.lang.String txnTime) {
		set("txnTime", txnTime);
	}
	
	public java.lang.String getTxnTime() {
		return getStr("txnTime");
	}

	public void setReq(java.lang.String req) {
		set("req", req);
	}
	
	public java.lang.String getReq() {
		return getStr("req");
	}

	public void setRespCode(java.lang.String respCode) {
		set("respCode", respCode);
	}
	
	public java.lang.String getRespCode() {
		return getStr("respCode");
	}

	public void setRespMsg(java.lang.String respMsg) {
		set("respMsg", respMsg);
	}
	
	public java.lang.String getRespMsg() {
		return getStr("respMsg");
	}

	public void setResp(java.lang.String resp) {
		set("resp", resp);
	}
	
	public java.lang.String getResp() {
		return getStr("resp");
	}

	public void setExceInfo(java.lang.String exceInfo) {
		set("exceInfo", exceInfo);
	}
	
	public java.lang.String getExceInfo() {
		return getStr("exceInfo");
	}

	public void setCat(java.util.Date cat) {
		set("cat", cat);
	}
	
	public java.util.Date getCat() {
		return get("cat");
	}

	public void setMat(java.util.Date mat) {
		set("mat", mat);
	}
	
	public java.util.Date getMat() {
		return get("mat");
	}

	public void setOperID(java.lang.String operID) {
		set("operID", operID);
	}
	
	public java.lang.String getOperID() {
		return getStr("operID");
	}

}
