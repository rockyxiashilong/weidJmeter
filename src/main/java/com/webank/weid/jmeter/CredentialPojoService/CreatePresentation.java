package com.webank.weid.jmeter.CredentialPojoService;

import com.webank.weid.jmeter.Common;
import com.webank.weid.protocol.base.*;
import com.webank.weid.protocol.request.CptMapArgs;
import com.webank.weid.protocol.request.CreateCredentialPojoArgs;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.CptService;
import com.webank.weid.rpc.CredentialPojoService;
import com.webank.weid.rpc.WeIdService;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.service.impl.CredentialPojoServiceImpl;
import com.webank.weid.service.impl.WeIdServiceImpl;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreatePresentation extends AbstractJavaSamplerClient {
    private static volatile WeIdService weIdService = new WeIdServiceImpl();
    private static volatile CptService cptService = new CptServiceImpl();
    private static volatile CredentialPojoService credentialPojoService= new CredentialPojoServiceImpl();
    private static volatile WeIdAuthentication weIdAuthentication;
    private static volatile CptBaseInfo cptBaseInfo;
    private static volatile CreateCredentialPojoArgs createCredentialPojoArgs;
    private static volatile CredentialPojo credentialPojo;
    private static volatile List<CredentialPojo> credentialList = new ArrayList<>();
    private static volatile PresentationPolicyE presentationPolicyE;
    private static volatile Challenge challenge;
    private static volatile CreateWeIdDataResult weIdDataResult;
    static {
        if(weIdDataResult == null){
            weIdDataResult = weIdService.createWeId().getResult();
        }

        if (weIdAuthentication == null){
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
        if (credentialPojo ==null){
            credentialPojo = credentialPojoService.createCredential(createCredentialPojoArgs).getResult();
            credentialList.add(credentialPojo);
        }
        if(challenge == null){
            challenge =Challenge.create(credentialPojo.getIssuer(),String.valueOf(System.currentTimeMillis()));
        }
        if (presentationPolicyE == null){
            String policyJson = "{\"extra\" : {\"extra1\" : \"\",\"extra2\" : \"\"},\"id\" : 123456,\"version\" : 1,\"orgId\" : \"webank\",\"weId\" : \"did:weid:0x0231765e19955fc65133ec8591d73e9136306cd0\",\"policy\" : {\"1017\" : {\"fieldsToBeDisclosed\" : {\"gender\" : 0,\"name\" : 1,\"age\" : 0}}}}";
            presentationPolicyE = PresentationPolicyE.fromJson(policyJson);
            presentationPolicyE.setPolicyPublisherWeId(credentialPojo.getIssuer());
            Map<Integer, ClaimPolicy> policyMap = presentationPolicyE.getPolicy();
            ClaimPolicy cliamPolicy = policyMap.get(1017);
            policyMap.remove(1017);
            policyMap.put(createCredentialPojoArgs.getCptId(), cliamPolicy);
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Arguments params = new Arguments();
        JavaSamplerContext arg0 = new JavaSamplerContext(params);
        CreatePresentation test = new CreatePresentation();
        test.setupTest(arg0);
        test.runTest(arg0);
        test.teardownTest(arg0);
    }
    // Run every time the pressure thread starts
    @Override
    public void setupTest(JavaSamplerContext context) {
        getNewLogger().info("--------------------->this is CreatePresentation setupTest");
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
        result.setSampleLabel("CreatePresentation");
        result.sampleStart();
        ResponseData<PresentationE>  rr = credentialPojoService.createPresentation(
                credentialList,
                presentationPolicyE,
                challenge,
                weIdAuthentication);
        if (rr.getResult() == null) {
            try {
                throw new Exception("CreatePresentation:" +
                        rr.getErrorCode() + ", " +
                        rr.getErrorMessage());
            } catch (Exception e) {
                e.printStackTrace();
                getNewLogger().error("------------->CreatePresentation fail:", e);
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
            getNewLogger().info("----------------->CreatePresentation"+rr.toString());
            result.sampleEnd();
        }
        return result;
    }
    @Override
    protected Logger getNewLogger() {
        return super.getNewLogger();
    }
}
