package com.webank.weid.jmeter.AuthorityIssuerService;

import com.webank.weid.constant.ErrorCode;
import com.webank.weid.jmeter.Common;
import com.webank.weid.protocol.base.AuthorityIssuer;
import com.webank.weid.protocol.request.RegisterAuthorityIssuerArgs;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.AuthorityIssuerService;
import com.webank.weid.rpc.WeIdService;
import com.webank.weid.service.impl.AuthorityIssuerServiceImpl;
import com.webank.weid.service.impl.WeIdServiceImpl;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Assert;
import org.slf4j.Logger;

public class QueryAuthorityIssuerInfo extends AbstractJavaSamplerClient {
    private static volatile Common common = new Common();
    private static volatile WeIdService weIdService = new WeIdServiceImpl();
    private static volatile AuthorityIssuerService authorityIssuerService = new AuthorityIssuerServiceImpl();
    private static volatile String privateKey = common.readPrivateKeyFromFile("ecdsa_key");
    private static volatile CreateWeIdDataResult weid = null;
    static {
        if (weid == null){
            weid = weIdService.createWeId().getResult();
            registerAuthorityIssuer(weid);
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Arguments params = new Arguments();
        JavaSamplerContext arg0 = new JavaSamplerContext(params);
        QueryAuthorityIssuerInfo test = new QueryAuthorityIssuerInfo();
        test.setupTest(arg0);
        test.runTest(arg0);
        test.teardownTest(arg0);
    }
    // Run every time the pressure thread starts
    @Override
    public void setupTest(JavaSamplerContext context) {
        getNewLogger().info("--------------------->this is QueryAuthorityIssuerInfo setupTest");
        super.setupTest(context);
    }

    // Execute every runTest run
    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
    }

    // JMeter GUI parameters
    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
//        arguments.addArgument("idNumber", this.idNumber);
//        arguments.addArgument("name", this.name);
        return arguments;
    }

    // Jmeter runs once runTest to calculate a thing, it will run repeatedly
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.setSampleLabel("QueryAuthorityIssuerInfo");
        result.sampleStart();
        ResponseData<AuthorityIssuer> rr = authorityIssuerService.queryAuthorityIssuerInfo(weid.getWeId());
        if (rr.getResult() == null) {
            try {
                throw new Exception("QueryAuthorityIssuerInfo:" +
                        rr.getErrorCode() + ", " +
                        rr.getErrorMessage());
            } catch (Exception e) {
                e.printStackTrace();
                getNewLogger().error("------------->QueryAuthorityIssuerInfo fail:", e);
                result.sampleEnd();
                result.setSuccessful(false);
                result.setResponseHeaders("false");
                result.setResponseMessage(e.getMessage());
            }
        }else {
            result.setSuccessful(true);
            result.setResponseMessage(rr.getErrorMessage());
            result.setResponseData(rr.getResult().toString());
            result.setResponseHeaders("true");
            getNewLogger().info("----------------->QueryAuthorityIssuerInfo"+rr.toString());
            result.sampleEnd();
        }
        return result;
    }
    @Override
    protected Logger getNewLogger() {
        return super.getNewLogger();
    }

   private static void registerAuthorityIssuer(CreateWeIdDataResult createWeId) {

        RegisterAuthorityIssuerArgs registerAuthorityIssuerArgs =
                Common.buildRegisterAuthorityIssuerArgs(createWeId, privateKey);

        ResponseData<Boolean> response = new ResponseData<>(false,
                ErrorCode.AUTHORITY_ISSUER_CONTRACT_ERROR_NAME_ALREADY_EXISTS);

        while (response.getErrorCode()
                == ErrorCode.AUTHORITY_ISSUER_CONTRACT_ERROR_NAME_ALREADY_EXISTS.getCode()) {
            String name = registerAuthorityIssuerArgs.getAuthorityIssuer().getName();
            registerAuthorityIssuerArgs.getAuthorityIssuer().setName(name + Math.random());
            response = authorityIssuerService.registerAuthorityIssuer(registerAuthorityIssuerArgs);
        }

        Assert.assertEquals(ErrorCode.SUCCESS.getCode(), response.getErrorCode().intValue());
        Assert.assertEquals(true, response.getResult());
    }
}
