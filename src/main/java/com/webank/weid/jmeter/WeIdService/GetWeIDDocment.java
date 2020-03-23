package com.webank.weid.jmeter.WeIdService;

import com.webank.weid.protocol.base.WeIdDocument;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.WeIdService;
import com.webank.weid.service.impl.WeIdServiceImpl;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;

public class GetWeIDDocment extends AbstractJavaSamplerClient {
    private static WeIdService weIdService = new WeIdServiceImpl();
    private static String weid = weIdService.createWeId().toString();

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Arguments params = new Arguments();
        JavaSamplerContext arg0 = new JavaSamplerContext(params);
        CreateWeId test = new CreateWeId();
        test.setupTest(arg0);
        test.runTest(arg0);
        test.teardownTest(arg0);
    }
    // Run every time the pressure thread starts
    @Override
    public void setupTest(JavaSamplerContext context) {
        getNewLogger().info("--------------------->this is get weid document setupTest");
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
        result.setSampleLabel("get weid document");
        result.sampleStart();
        ResponseData<WeIdDocument> rr = this.weIdService.getWeIdDocument(weid);
        if (rr.getResult() == null) {
            try {
                throw new Exception("get weid doc fail:" +
                        rr.getErrorCode() + ", " +
                        rr.getErrorMessage());
            } catch (Exception e) {
                e.printStackTrace();
                getNewLogger().error("------------->get weid doc fail:", e);
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
            getNewLogger().info("----------------->"+rr.toString());
            result.sampleEnd();
        }
        return result;
    }
    @Override
    protected Logger getNewLogger() {
        return super.getNewLogger();
    }
}
