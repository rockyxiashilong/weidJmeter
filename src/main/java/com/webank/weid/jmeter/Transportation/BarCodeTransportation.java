package com.webank.weid.jmeter.Transportation;

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
import com.webank.weid.suite.api.transportation.TransportationFactory;
import com.webank.weid.suite.api.transportation.params.EncodeType;
import com.webank.weid.suite.api.transportation.params.ProtocolProperty;
import com.webank.weid.suite.api.transportation.params.TransportationType;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class

BarCodeTransportation extends AbstractJavaSamplerClient {
    private static volatile WeIdService weIdService = new WeIdServiceImpl();
    private static volatile CptService cptService = new CptServiceImpl();
    private static volatile CredentialPojoService credentialPojoService= new CredentialPojoServiceImpl();
    private static volatile WeIdAuthentication weIdAuthentication;
    private static volatile CptBaseInfo cptBaseInfo;
    private static volatile CreateCredentialPojoArgs createCredentialPojoArgs;
    private static volatile CredentialPojo credentialPojo;
    private static volatile CredentialPojoList list = new CredentialPojoList();
    private static volatile List<String> verifierWeIdList = new ArrayList<String>();
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
            list.add(credentialPojo);
        }
        if(verifierWeIdList.size()==0){
            verifierWeIdList.add(weIdDataResult.getWeId());
            System.out.println(verifierWeIdList);
        }

    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Arguments params = new Arguments();
        JavaSamplerContext arg0 = new JavaSamplerContext(params);
        BarCodeTransportation test = new BarCodeTransportation();
        test.setupTest(arg0);
        test.runTest(arg0);
        test.teardownTest(arg0);
    }
    // Run every time the pressure thread starts
    @Override
    public void setupTest(JavaSamplerContext context) {
        getNewLogger().info("--------------------->this is BarCodeTransportation setupTest");
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
        result.setSampleLabel("BarCodeTransportation");
        result.sampleStart();
        getNewLogger().info("verifierWeIdList-------->"+verifierWeIdList);
        ResponseData<String> serialize =
                TransportationFactory.build(TransportationType.BAR_CODE)
                        .specify(verifierWeIdList)
                        .serialize(weIdAuthentication, list, new ProtocolProperty(EncodeType.CIPHER));
        if (serialize.getResult() == null) {
            try {
                throw new Exception("BarCodeTransportation:" +
                        serialize.getErrorCode() + ", " +
                        serialize.getErrorMessage());
            } catch (Exception e) {
                e.printStackTrace();
                getNewLogger().error("BarCodeTransportation fail:-------------->", e);
                result.sampleEnd();
                result.setSuccessful(false);
                result.setResponseHeaders("false");
                result.setResponseMessage(e.getMessage());
                return result;
            }
        }
        getNewLogger().info("Bar code serialize--------------->"+serialize.getResult());
        ResponseData<CredentialPojoList> deserialize =
                TransportationFactory.build(TransportationType.BAR_CODE)
                        .deserialize(weIdAuthentication, serialize.getResult(), CredentialPojoList.class);
        if (deserialize.getResult() == null) {
            try {
                throw new Exception("BarCodeTransportation:" +
                        deserialize.getErrorCode() + ", " +
                        deserialize.getErrorMessage());
            } catch (Exception e) {
                e.printStackTrace();
                getNewLogger().error("------------->BarCodeTransportation fail:", e);
                result.sampleEnd();
                result.setSuccessful(false);
                result.setResponseHeaders("false");
                result.setResponseMessage(e.getMessage());
            }
        }else {
            result.setSuccessful(true);
            result.setResponseMessage(deserialize.getErrorMessage());
            result.setResponseData(deserialize.getResult().toString());
            result.setResponseHeaders("true");
            getNewLogger().info("----------------->BarCodeTransportation"+deserialize.toString());
            result.sampleEnd();
        }
        return result;
    }
    @Override
    protected Logger getNewLogger() {
        return super.getNewLogger();
    }
}
