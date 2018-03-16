package com.mybank.pc.admin.model.base;

import com.jfinal.plugin.activerecord.IBean;
import com.mybank.pc.core.CoreModel;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseUserRole<M extends BaseUserRole<M>> extends CoreModel<M> implements IBean {

	public void setId(java.lang.Long id) {
		set("id", id);
	}

	public java.lang.Long getId() {
		return getLong("id");
	}

	public void setUid(java.lang.Long uid) {
		set("uid", uid);
	}

	public java.lang.Long getUid() {
		return getLong("uid");
	}

	public void setRid(java.lang.Integer rid) {
		set("rid", rid);
	}

	public java.lang.Integer getRid() {
		return getInt("rid");
	}

}
