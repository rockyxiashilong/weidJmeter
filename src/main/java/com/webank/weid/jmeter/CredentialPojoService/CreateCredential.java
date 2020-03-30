package com.webank.weid.jmeter.CredentialPojoService;

import com.webank.weid.jmeter.Common;
import com.webank.weid.jmeter.EvidenceService.CreateEvidence;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.CredentialPojo;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.request.CptMapArgs;
import com.webank.weid.protocol.request.CreateCredentialPojoArgs;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.CptService;
import com.webank.weid.rpc.CredentialPojoService;
import com.webank.weid.rpc.EvidenceService;
import com.webank.weid.rpc.WeIdService;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.service.impl.CredentialPojoServiceImpl;
import com.webank.weid.service.impl.EvidenceServiceImpl;
import com.webank.weid.service.impl.WeIdServiceImpl;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;

public class CreateCredential extends AbstractJavaSamplerClient {
    private static volatile WeIdService weIdService = new WeIdServiceImpl();
    private static volatile CptService cptService = new CptServiceImpl();
    private static volatile CredentialPojoService credentialPojoService= new CredentialPojoServiceImpl();
    private static volatile CreateWeIdDataResult weIdDataResult;
    private static volatile WeIdAuthentication weIdAuthentication;
    private static volatile CptBaseInfo cptBaseInfo;
    private static volatile CreateCredentialPojoArgs createCredentialPojoArgs;
    static {
        if(weIdDataResult ==null){
            weIdDataResult = weIdService.createWeId().getResult();
            weIdAuthentication = Common.buildWeIdAuthentication(weIdDataResult);
        }

        if (cptBaseInfo==null){
            CptMapArgs cptMapArgs = Common.buildCptArgs(weIdDataResult);
            ResponseData<CptBaseInfo> response = cptService.registerCpt(cptMapArgs);
            cptBaseInfo = response.getResult();
        }
        if (createCredentialPojoArgs == null){
            createCredentialPojoArgs =
                    Common.buildCreateCredentialPojoArgs(weIdDataResult);
            createCredentialPojoArgs.setCptId(cptBaseInfo.getCptId());
        }
    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Arguments params = new Arguments();
        JavaSamplerContext arg0 = new JavaSamplerContext(params);
        CreateCredential test = new CreateCredential();
        test.setupTest(arg0);
        test.runTest(arg0);
        test.teardownTest(arg0);
    }
    // Run every time the pressure thread starts
    @Override
    public void setupTest(JavaSamplerContext context) {
        getNewLogger().info("--------------------->this is CreateCredential setupTest");
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
        result.setSampleLabel("CreateCredential");
        result.sampleStart();
        ResponseData<CredentialPojo> rr =
                credentialPojoService.createCredential(createCredentialPojoArgs);
        if (rr.getResult() == null) {
            try {
                throw new Exception("CreateCredential:" +
                        rr.getErrorCode() + ", " +
                        rr.getErrorMessage());
            } catch (Exception e) {
                e.printStackTrace();
                getNewLogger().error("------------->CreateCredential fail:", e);
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
            getNewLogger().info("----------------->CreateCredential"+rr.toString());
            result.sampleEnd();
        }
        return result;
    }
    @Override
    protected Logger getNewLogger() {
        return super.getNewLogger();
    }
}
