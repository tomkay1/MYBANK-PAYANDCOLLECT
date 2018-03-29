package com.mybank.pc.collection.entrust;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.mybank.pc.collection.model.CollectionEntrust;
import com.mybank.pc.collection.model.UnionpayEntrust;
import com.mybank.pc.exception.EntrustRuntimeException;
import com.mybank.pc.interceptors.EntrustExceptionInterceptor;
import com.mybank.pc.kits.unionpay.acp.AcpService;
import com.mybank.pc.kits.unionpay.acp.SDK;
import com.mybank.pc.kits.unionpay.acp.SDKConfig;

public class CEntrustSrv {

	@Before({ EntrustExceptionInterceptor.class, Tx.class })
	public void establish(Kv kv, String userId) {
		UnionpayEntrust unionpayEntrust = new UnionpayEntrust();
		try {
			String merCode = kv.getStr("merCode");

			String accNo = kv.getStr("accNo");
			String certifTp = kv.getStr("certifTp");
			String certifId = kv.getStr("certifId");
			String customerNm = kv.getStr("customerNm");
			String phoneNo = kv.getStr("phoneNo");
			String cvn2 = kv.getStr("cvn2");
			String expired = kv.getStr("expired");

			Date now = new Date();
			String orderId = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now) + accNo;
			if(orderId.length()>40){
				orderId=orderId.substring(0, 40);
			}
			
			String txnTime = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

			unionpayEntrust.setCustomerNm(customerNm);
			unionpayEntrust.setCertifTp(certifTp);
			unionpayEntrust.setCertifId(certifId);
			unionpayEntrust.setAccNo(accNo);
			unionpayEntrust.setPhoneNo(phoneNo);
			unionpayEntrust.setCvn2(cvn2);
			unionpayEntrust.setExpired(expired);
			unionpayEntrust.setTradeNo(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now) + certifId);
			unionpayEntrust.setOrderId(orderId);
			unionpayEntrust.setTxnTime(txnTime);
			unionpayEntrust.setCat(now);
			unionpayEntrust.setMat(now);
			unionpayEntrust.setOperID(userId);

			SDK sdk = null;
			if (merCode.equals("0")) {
				sdk = SDK.REALTIME_SDK;
			} else if (merCode.equals("1")) {
				sdk = SDK.BATCH_SDK;
			}
			SDKConfig sdkConfig = sdk.getSdkConfig();
			AcpService acpService = sdk.getAcpService();
			String encoding = "UTF-8";
			String merId = sdk.getMerId();

			unionpayEntrust.setMerId(merId);

			Map<String, String> contentData = new HashMap<String, String>();

			/*** 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改 ***/
			contentData.put("version", sdkConfig.getVersion()); // 版本号
			contentData.put("encoding", encoding); // 字符集编码 可以使用UTF-8,GBK两种方式
			contentData.put("signMethod", sdkConfig.getSignMethod()); // 签名方法
																		// 目前只支持01-RSA方式证书加密
			contentData.put("txnType", "72"); // 交易类型
			contentData.put("txnSubType", "11"); // 交易子类型
			contentData.put("bizType", "000501"); // 业务类型
			contentData.put("channelType", "07"); // 渠道类型

			/*** 商户接入参数 ***/
			contentData.put("merId", merId); // 商户号码（商户号码777290058110097仅做为测试调通交易使用，该商户号配置了需要对敏感信息加密）测试时请改成自己申请的商户号，【自己注册的测试777开头的商户号不支持代收产品】
			contentData.put("accessType", "0"); // 接入类型，商户接入固定填0，不需修改
			contentData.put("orderId", orderId); // 商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
			contentData.put("txnTime", txnTime); // 订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
			contentData.put("accType", "01"); // 账号类型

			// 姓名，证件类型+证件号码至少二选一必送，手机号可选，贷记卡的cvn2,expired可选。
			Map<String, String> customerInfoMap = new HashMap<String, String>();
			customerInfoMap.put("certifTp", certifTp); // 证件类型
			customerInfoMap.put("certifId", certifId); // 证件号码
			customerInfoMap.put("customerNm", customerNm); // 姓名

			customerInfoMap.put("phoneNo", phoneNo); // 手机号
			// 当卡号为贷记卡的时候cvn2,expired可选上送
			customerInfoMap.put("cvn2", cvn2); // 卡背面的cvn2三位数字
			customerInfoMap.put("expired", expired); // 有效期 年在前月在后

			// 如果商户号开通了【商户对敏感信息加密】的权限那么需要对
			// accNo，pin和phoneNo，cvn2，expired加密（如果这些上送的话），对敏感信息加密使用：
			String accNoEnc = acpService.encryptData(accNo, encoding); // 这里测试的时候使用的是测试卡号，正式环境请使用真实卡号
			contentData.put("accNo", accNoEnc);
			contentData.put("encryptCertId", acpService.getEncryptCertId()); // 加密证书的certId，配置在acp_sdk.properties文件
																				// acpsdk.encryptCert.path属性下
			String customerInfoStr = acpService.getCustomerInfoWithEncrypt(customerInfoMap, null, encoding);

			// 如果商户号未开通【商户对敏感信息加密】权限那么不需对敏感信息加密使用：
			// contentData.put("accNo", "6216261000000000018");
			// //这里测试的时候使用的是测试卡号，正式环境请使用真实卡号
			// String customerInfoStr =
			// DemoBase.getCustomerInfo(customerInfoMap,null);

			contentData.put("customerInfo", customerInfoStr);
			unionpayEntrust.setReq(JsonKit.toJson(contentData));

			/** 对请求参数进行签名并发送http post请求，接收同步应答报文 **/
			Map<String, String> reqData = acpService.sign(contentData, encoding); // 报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
			String requestBackUrl = sdkConfig.getBackRequestUrl(); // 交易请求url从配置文件读取对应属性文件acp_sdk.properties中的
																	// acpsdk.backTransUrl
			Map<String, String> rspData = acpService.post(reqData, requestBackUrl, encoding); // 发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
			unionpayEntrust.setResp(JsonKit.toJson(rspData));
			handlingEstablishResult(rspData, acpService, encoding, unionpayEntrust);
		} catch (EntrustRuntimeException e) {
			throw e;
		} catch (Exception e) {
			EntrustRuntimeException xe = new EntrustRuntimeException(e);
			xe.setContext(unionpayEntrust);
			throw xe;
		}
	}

	/**
	 * 应答码规范参考open.unionpay.com帮助中心 下载 产品接口规范 《平台接入接口规范-第5部分-附录》
	 * 
	 * @param rspData
	 * @param acpService
	 * @param encoding
	 */
	private void handlingEstablishResult(Map<String, String> rspData, AcpService acpService, String encoding,
			UnionpayEntrust unionpayEntrust) {
		try {
			CollectionEntrust query = new CollectionEntrust();
			query.setCustomerNm(unionpayEntrust.getCustomerNm());
			query.setCertifId(unionpayEntrust.getCertifId());
			query.setAccNo(unionpayEntrust.getAccNo());
			query.setMerId(unionpayEntrust.getMerId());

			CollectionEntrust collectionEntrust = query.findOne();
			boolean needSave = collectionEntrust == null;
			if (needSave) {
				collectionEntrust = new CollectionEntrust();
				collectionEntrust.setCustomerNm(unionpayEntrust.getCustomerNm());
				collectionEntrust.setCertifTp(unionpayEntrust.getCertifTp());
				collectionEntrust.setCertifId(unionpayEntrust.getCertifId());
				collectionEntrust.setAccNo(unionpayEntrust.getAccNo());
				collectionEntrust.setPhoneNo(unionpayEntrust.getPhoneNo());
				collectionEntrust.setCvn2(unionpayEntrust.getCvn2());
				collectionEntrust.setExpired(unionpayEntrust.getExpired());
				collectionEntrust.setMerId(unionpayEntrust.getMerId());
				collectionEntrust.setCat(unionpayEntrust.getCat());
			}
			collectionEntrust.setMat(unionpayEntrust.getCat());
			collectionEntrust.setOperID(unionpayEntrust.getOperID());

			boolean isEmpty = rspData.isEmpty();
			boolean isValidate = acpService.validate(rspData, encoding);

			if (isEmpty) {// 未返回正确的http状态
				LogKit.error("未获取到返回报文或返回http状态码非200");
				throw new RuntimeException("未获取到返回报文或返回http状态码非200");
			}
			if (isValidate) {
				LogKit.info("验证签名成功");
			} else {
				LogKit.error("验证签名失败");
				throw new RuntimeException("验证签名失败");
			}

			String respCode = rspData.get("respCode");
			String respMsg = rspData.get("respMsg");

			unionpayEntrust.setRespCode(respCode);
			unionpayEntrust.setRespMsg(respMsg);
			if (("00").equals(respCode)) {// 成功
				collectionEntrust.setStatus("0");
				unionpayEntrust.setFinalCode("0");
			} else {
				collectionEntrust.setStatus("1");
				unionpayEntrust.setFinalCode("2");
			}

			if (needSave) {
				collectionEntrust.save();
			} else {
				collectionEntrust.update();
			}
			unionpayEntrust.save();
		} catch (Exception e) {
			EntrustRuntimeException xe = new EntrustRuntimeException(e);
			xe.setContext(unionpayEntrust);
			throw xe;
		}
	}

	public void handlingException(Invocation invocation, EntrustRuntimeException e) {
		String actionKey = invocation.getActionKey();
		if (actionKey.equals("/coll/entrust/establish")) {
			UnionpayEntrust unionpayEntrust = (UnionpayEntrust) e.getContext();
			unionpayEntrust.setExpired(JsonKit.toJson(e.getExceptionInfo()));
			unionpayEntrust.setFinalCode("2");
			unionpayEntrust.save();

			CollectionEntrust query = new CollectionEntrust();
			String customerNm = unionpayEntrust.getCustomerNm();
			query.setCustomerNm(customerNm == null ? "" : customerNm);
			String certifId = unionpayEntrust.getCertifId();
			query.setCertifId(certifId == null ? "" : certifId);
			String accNo = unionpayEntrust.getAccNo();
			query.setAccNo(accNo == null ? "" : accNo);
			String merId = unionpayEntrust.getMerId();
			query.setMerId(merId == null ? "" : merId);

			CollectionEntrust collectionEntrust = query.findOne();
			boolean needSave = collectionEntrust == null;
			if (needSave) {
				collectionEntrust = new CollectionEntrust();
				collectionEntrust.setCustomerNm(unionpayEntrust.getCustomerNm());
				collectionEntrust.setCertifTp(unionpayEntrust.getCertifTp());
				collectionEntrust.setCertifId(unionpayEntrust.getCertifId());
				collectionEntrust.setAccNo(unionpayEntrust.getAccNo());
				collectionEntrust.setPhoneNo(unionpayEntrust.getPhoneNo());
				collectionEntrust.setCvn2(unionpayEntrust.getCvn2());
				collectionEntrust.setExpired(unionpayEntrust.getExpired());
				collectionEntrust.setMerId(unionpayEntrust.getMerId());
				collectionEntrust.setCat(unionpayEntrust.getCat());
			}
			collectionEntrust.setMat(unionpayEntrust.getCat());
			collectionEntrust.setStatus("1");
			collectionEntrust.setOperID(unionpayEntrust.getOperID());

			if (needSave) {
				collectionEntrust.save();
			} else {
				collectionEntrust.update();
			}
		}

	}

}