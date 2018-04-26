package com.mybank.pc.collection.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.SqlPara;
import com.mybank.pc.collection.model.base.BaseUnionpayCollectionQuery;
import com.mybank.pc.kits.unionpay.acp.AcpService;
import com.mybank.pc.kits.unionpay.acp.SDK;
import com.mybank.pc.kits.unionpay.acp.SDKConfig;
import com.mybank.pc.kits.unionpay.acp.SDKConstants;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class UnionpayCollectionQuery extends BaseUnionpayCollectionQuery<UnionpayCollectionQuery> {
	public static final UnionpayCollectionQuery dao = new UnionpayCollectionQuery().dao();

	private Map<String, String> queryReqData = null;
	private Map<String, String> queryRspData = null;

	public Map<String, String> assemblyQueryRequest() {
		Map<String, String> contentData = new HashMap<String, String>();

		SDK sdk = SDK.getByMerId(getMerId());
		SDKConfig sdkConfig = sdk.getSdkConfig();
		AcpService acpService = sdk.getAcpService();

		// 版本号
		contentData.put("version", sdkConfig.getVersion());
		// 字符集编码 可以使用UTF-8,GBK两种方式
		contentData.put("encoding", SDKConstants.UTF_8_ENCODING);
		// 签名方法 目前只支持01-RSA方式证书加密
		contentData.put("signMethod", sdkConfig.getSignMethod());
		// 交易类型 22 批量查询
		contentData.put("txnType", getTxnType());
		// 交易子类 02代收
		contentData.put("txnSubType", getTxnSubType());
		// 代收 000501
		contentData.put("bizType", getBizType());

		/*** 商户接入参数 ***/
		// 接入类型，商户接入填0，不需修改（0：直连商户2：平台商户）
		contentData.put("accessType", getAccessType());
		// 商户号码，请改成自己申请的商户号，【测试777开通的商户号不支持代收产品】
		contentData.put("merId", getMerId());

		/** 与批量查询相关的参数 **/
		// 被查询批量交易批次号
		contentData.put("orderId", getOrderId());
		// 原批量代收请求的交易时间
		contentData.put("txnTime", getTxnTime());

		setReq(JsonKit.toJson(contentData));
		// 报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可
		queryReqData = acpService.sign(contentData, SDKConstants.UTF_8_ENCODING);
		return queryReqData;
	}

	/**
	 * 对请求参数进行签名并发送http post请求，接收同步应答报文
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> queryResult() throws Exception {
		SDK sdk = SDK.getByMerId(getMerId());
		SDKConfig sdkConfig = sdk.getSdkConfig();
		AcpService acpService = sdk.getAcpService();

		// 交易请求url从配置文件读取对应属性文件acp_sdk.properties中的acpsdk.batchTransUrl
		String requestQueryUrl = sdkConfig.getSingleQueryUrl();
		// 发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
		if (queryReqData == null) {
			assemblyQueryRequest();
		}
		queryRspData = acpService.post(queryReqData, requestQueryUrl, SDKConstants.UTF_8_ENCODING);
		setResp(JsonKit.toJson(queryRspData));
		return queryRspData;
	}

	public void setFieldFromQueryResp() {
		if (this.queryRspData != null) {
			String respCode = queryRspData.get("respCode");
			String respMsg = queryRspData.get("respMsg");
			String acqInsCode = queryRspData.get("acqInsCode");
			String queryId = queryRspData.get("queryId");
			String traceNo = queryRspData.get("traceNo");
			String traceTime = queryRspData.get("traceTime");
			String settleAmt = queryRspData.get("settleAmt");
			String settleCurrencyCode = queryRspData.get("settleCurrencyCode");
			String settleDate = queryRspData.get("settleDate");
			String exchangeRate = queryRspData.get("exchangeRate");
			String exchangeDate = queryRspData.get("exchangeDate");
			String currencyCode = queryRspData.get("currencyCode");
			String txnAmt = queryRspData.get("txnAmt");
			String origRespCode = queryRspData.get("origRespCode");
			String origRespMsg = queryRspData.get("origRespMsg");
			String accNo = queryRspData.get("accNo");
			String payCardType = queryRspData.get("payCardType");
			String payType = queryRspData.get("payType");
			String payCardNo = queryRspData.get("payCardNo");
			String payCardIssueName = queryRspData.get("payCardIssueName");
			String cardTransData = queryRspData.get("cardTransData");
			String issuerIdentifyMode = queryRspData.get("issuerIdentifyMode");

			setRespCode(respCode);
			setRespMsg(respMsg);
			setAcqInsCode(acqInsCode);
			setQueryId(queryId);
			setTraceNo(traceNo);
			setTraceTime(traceTime);
			setSettleAmt(settleAmt);
			setSettleCurrencyCode(settleCurrencyCode);
			setSettleDate(settleDate);
			setExchangeRate(exchangeRate);
			setExchangeDate(exchangeDate);
			setCurrencyCode(currencyCode);
			setTxnAmt(txnAmt);
			setOrigRespCode(origRespCode);
			setOrigRespMsg(origRespMsg);
			setAccNo(accNo);
			setPayCardType(payCardType);
			setPayType(payType);
			setPayCardNo(payCardNo);
			setPayCardIssueName(payCardIssueName);
			setCardTransData(cardTransData);
			setIssuerIdentifyMode(issuerIdentifyMode);
		}
	}

	public boolean validateQueryResp() {
		return SDK.validateResp(queryRspData, getMerId(), SDKConstants.UTF_8_ENCODING);
	}

	public List<UnionpayCollectionQuery> findUnionpayCollectionQuery() {
		return findUnionpayCollectionQuery(this);
	}

	public static List<UnionpayCollectionQuery> findUnionpayCollectionQuery(UnionpayCollectionQuery param) {
		SqlPara sqlPara = Db.getSqlPara("collection_trade.findUnionpayCollectionQuery", param);
		return UnionpayCollectionQuery.dao.find(sqlPara);
	}

	public Map<String, String> getQueryReqData() {
		return queryReqData;
	}

	public void setQueryReqData(Map<String, String> queryReqData) {
		this.queryReqData = queryReqData;
	}

	public Map<String, String> getQueryRspData() {
		return queryRspData;
	}

	public void setQueryRspData(Map<String, String> queryRspData) {
		this.queryRspData = queryRspData;
	}
}
