package com.webank.weid.jmeter.EvidenceService;

import com.webank.weid.jmeter.Common;
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
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.service.impl.engine.EngineFactory;
import com.webank.weid.service.impl.engine.EvidenceServiceEngine;
import com.webank.weid.util.CredentialUtils;
import com.webank.weid.util.DataToolUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BatchCreateEvidence extends AbstractJavaSamplerClient {
    private static volatile WeIdService weIdService = new WeIdServiceImpl();
    private static volatile CptService cptService = new CptServiceImpl();
    private static volatile CredentialPojoService credentialPojoService= new CredentialPojoServiceImpl();
    private static volatile CreateWeIdDataResult weIdDataResult;
    private static volatile WeIdAuthentication weIdAuthentication;
    private static volatile CptBaseInfo cptBaseInfo;
    private static volatile String privateKey = Common.readPrivateKeyFromFile("ecdsa_key");
    private static volatile CredentialPojo credentialPojo;
    private static volatile EvidenceServiceEngine engine = EngineFactory.createEvidenceServiceEngine();
    List<String> hashValues = new ArrayList<>();
    List<String> signatures = new ArrayList<>();
    List<Long> timestamps = new ArrayList<>();
    List<String> signers = new ArrayList<>();
    List<String> logs = new ArrayList<>();
    List<String> customKeys = new ArrayList<>();
    private String batchSize;
    private String logLength;
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
        if (credentialPojo == null){
            CreateCredentialPojoArgs<Map<String, Object>> createCredentialPojoArgs =
                    Common.buildCreateCredentialPojoArgs(weIdDataResult);
            createCredentialPojoArgs.setCptId(cptBaseInfo.getCptId());
            credentialPojo=
                    credentialPojoService.createCredential(createCredentialPojoArgs).getResult();
            System.out.println("credentialPojo------------->"+credentialPojo.toJson());
        }

    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Arguments params = new Arguments();
        params.addArgument("batchSize","100");
        params.addArgument("logLength","1000");
        JavaSamplerContext arg0 = new JavaSamplerContext(params);
        BatchCreateEvidence test = new BatchCreateEvidence();
        test.setupTest(arg0);
        test.runTest(arg0);
        test.teardownTest(arg0);
    }
    // Run every time the pressure thread starts
    @Override
    public void setupTest(JavaSamplerContext context) {
        getNewLogger().info("--------------------->this is BatchCreateEvidence setupTest");
        super.setupTest(context);
        this.batchSize = context.getParameter("batchSize") == null ? this.batchSize : context.getParameter("batchSize");
        this.logLength = context.getParameter("logLength") == null ? this.logLength : context.getParameter("logLength");
        int batchSize = Integer.parseInt(this.batchSize);
        int logLength = Integer.parseInt(this.logLength);
        getNewLogger().info("batchSize-------->"+batchSize);
        getNewLogger().info("logLength-------->"+logLength);
        for (int i = 0; i < batchSize; i++) {
            CredentialPojo copyCredentialPojo = CredentialUtils.copyCredential(credentialPojo);
            copyCredentialPojo.setId(UUID.randomUUID().toString());
            String hash = copyCredentialPojo.getHash();
            hashValues.add(copyCredentialPojo.getHash());
            signatures.add(new String(DataToolUtils.base64Encode(DataToolUtils
                    .simpleSignatureSerialization(DataToolUtils.signMessage(hash, privateKey))),
                    StandardCharsets.UTF_8));
            timestamps.add(System.currentTimeMillis());
            signers.add(DataToolUtils.convertPrivateKeyToDefaultWeId(privateKey));
            StringBuffer buffer = new StringBuffer();
            for (int j = 0; j < logLength; j++) {
                buffer.append("a");
            }
            logs.add("test log" + i + buffer.toString());
            if (i % 2 == 1) {
                customKeys.add(String.valueOf(System.currentTimeMillis()));
            } else {
                customKeys.add(StringUtils.EMPTY);
            }
        }
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
        arguments.addArgument("batchSize", this.batchSize);
        arguments.addArgument("logLength", this.logLength);
//        arguments.addArgument("name", this.name);
        return arguments;
    }

    // Jmeter runs once runTest to calculate a thing, it will run repeatedly
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.setSampleLabel("BatchCreateEvidence");
        result.sampleStart();
        ResponseData<List<Boolean>> rr = engine
                .batchCreateEvidence(hashValues, signatures, logs, timestamps, signers, privateKey);
        if (rr.getResult() == null || rr.getResult().size()!=Integer.parseInt(this.batchSize)) {
            try {
                throw new Exception("BatchCreateEvidence:" +
                        rr.getErrorCode() + ", " +
                        rr.getErrorMessage());
            } catch (Exception e) {
                e.printStackTrace();
                getNewLogger().error("------------->BatchCreateEvidence fail:", e);
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
            getNewLogger().info("----------------->BatchCreateEvidence"+rr.toString());
            result.sampleEnd();
        }
        return result;
    }
    @Override
    protected Logger getNewLogger() {
        return super.getNewLogger();
    }
}
