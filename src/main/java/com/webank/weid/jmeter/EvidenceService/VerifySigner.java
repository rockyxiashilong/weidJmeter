package com.webank.weid.jmeter.EvidenceService;

import com.webank.weid.jmeter.Common;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.CredentialPojo;
import com.webank.weid.protocol.base.EvidenceInfo;
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

import java.util.Map;

public class VerifySigner extends AbstractJavaSamplerClient {
    private static volatile WeIdService weIdService = new WeIdServiceImpl();
    private static volatile CptService cptService = new CptServiceImpl();
    private static volatile CredentialPojoService credentialPojoService= new CredentialPojoServiceImpl();
    private static volatile EvidenceService evidenceService = new EvidenceServiceImpl();
    private static volatile WeIdAuthentication weIdAuthentication;
    private static volatile CptBaseInfo cptBaseInfo;
    private static volatile CredentialPojo credentialPojo;
    private static volatile String hash;
    private static volatile EvidenceInfo evidenceInfo;
    private static volatile CreateWeIdDataResult createWeIdDataResult;
    static {
        if(createWeIdDataResult == null){
            createWeIdDataResult = weIdService.createWeId().getResult();
        }
        if(weIdAuthentication == null){
            weIdAuthentication = Common.buildWeIdAuthentication(createWeIdDataResult);
        }
        if (cptBaseInfo==null){
            CptMapArgs cptMapArgs = Common.buildCptArgs(createWeIdDataResult);
            ResponseData<CptBaseInfo> response = cptService.registerCpt(cptMapArgs);
            cptBaseInfo = response.getResult();
        }
        if (credentialPojo == null){
            CreateCredentialPojoArgs<Map<String, Object>> createCredentialPojoArgs =
                    Common.buildCreateCredentialPojoArgs(createWeIdDataResult);
            createCredentialPojoArgs.setCptId(cptBaseInfo.getCptId());
            credentialPojo=
                    credentialPojoService.createCredential(createCredentialPojoArgs).getResult();
            System.out.println("credentialPojo------------->"+credentialPojo.toJson());
            hash =evidenceService.createEvidence(credentialPojo,weIdAuthentication.getWeIdPrivateKey()).getResult();
        }
        if(evidenceInfo == null){
            evidenceInfo = evidenceService.getEvidence(hash).getResult();
            System.out.println("evidenceInfo-------->"+evidenceInfo);
        }

    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Arguments params = new Arguments();
        JavaSamplerContext arg0 = new JavaSamplerContext(params);
        VerifySigner test = new VerifySigner();
        test.setupTest(arg0);
        test.runTest(arg0);
        test.teardownTest(arg0);
    }
    // Run every time the pressure thread starts
    @Override
    public void setupTest(JavaSamplerContext context) {
        getNewLogger().info("--------------------->this is VerifySigner setupTest");
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
        result.setSampleLabel("VerifySigner");
        result.sampleStart();
        ResponseData<Boolean> rr = evidenceService.verifySigner(evidenceInfo,weIdAuthentication.getWeId());
        if (rr.getResult() == null || rr.getResult()==false) {
            try {
                throw new Exception("VerifySigner:" +
                        rr.getErrorCode() + ", " +
                        rr.getErrorMessage());
            } catch (Exception e) {
                e.printStackTrace();
                getNewLogger().error("------------->VerifySigner fail:", e);
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
            getNewLogger().info("----------------->VerifySigner"+rr.toString());
            result.sampleEnd();
        }
        return result;
    }
    @Override
    protected Logger getNewLogger() {
        return super.getNewLogger();
    }
}
