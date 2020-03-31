package com.webank.weid.jmeter;

import com.webank.weid.constant.JsonSchemaConstant;
import com.webank.weid.jmeter.AuthorityIssuerService.IsAuthorityIssuer;
import com.webank.weid.protocol.base.AuthorityIssuer;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.request.*;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Common {
    private static final Logger logger = LoggerFactory.getLogger(Common.class);

    public static WeIdAuthentication buildWeIdAuthentication(CreateWeIdDataResult weIdData) {
        WeIdAuthentication weIdAuthentication = new WeIdAuthentication();
        weIdAuthentication.setWeId(weIdData.getWeId());
        weIdAuthentication.setWeIdPublicKeyId(weIdData.getWeId() + "#keys-0");
        weIdAuthentication.setWeIdPrivateKey(new WeIdPrivateKey());
        weIdAuthentication.getWeIdPrivateKey().setPrivateKey(
                weIdData.getUserWeIdPrivateKey().getPrivateKey());
        return weIdAuthentication;
    }

    public static CptMapArgs buildCptArgs(CreateWeIdDataResult createWeId) {

        CptMapArgs cptMapArgs = new CptMapArgs();
        cptMapArgs.setCptJsonSchema(buildCptJsonSchema());
        cptMapArgs.setWeIdAuthentication(buildWeIdAuthentication(createWeId));

        return cptMapArgs;
    }
    public static CreateCredentialPojoArgs<Map<String, Object>> buildCreateCredentialPojoArgs(
            CreateWeIdDataResult createWeId) {

        CreateCredentialPojoArgs<Map<String, Object>> createCredentialPojoArgs =
                new CreateCredentialPojoArgs<Map<String, Object>>();

        createCredentialPojoArgs.setIssuer(createWeId.getWeId());
        createCredentialPojoArgs.setExpirationDate(
                System.currentTimeMillis() + (1000 * 60 * 60 * 24));
        createCredentialPojoArgs.setWeIdAuthentication(buildWeIdAuthentication(createWeId));

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("id", createWeId.getWeId());
        claimMap.put("name","rocky");
        claimMap.put("age",20);
        claimMap.put("gender","F");
        createCredentialPojoArgs.setClaim(claimMap);

        return createCredentialPojoArgs;
    }
    public static SetPublicKeyArgs buildSetPublicKeyArgs(CreateWeIdDataResult createWeId) {

        SetPublicKeyArgs setPublicKeyArgs = new SetPublicKeyArgs();
        setPublicKeyArgs.setWeId(createWeId.getWeId());
        setPublicKeyArgs.setPublicKey(createWeId.getUserWeIdPublicKey().getPublicKey());
        setPublicKeyArgs.setUserWeIdPrivateKey(new WeIdPrivateKey());
        setPublicKeyArgs.getUserWeIdPrivateKey()
                .setPrivateKey(createWeId.getUserWeIdPrivateKey().getPrivateKey());

        return setPublicKeyArgs;
    }

    public static SetAuthenticationArgs buildSetAuthenticationArgs(
            CreateWeIdDataResult createWeId) {

        SetAuthenticationArgs setAuthenticationArgs = new SetAuthenticationArgs();
        setAuthenticationArgs.setWeId(createWeId.getWeId());
        setAuthenticationArgs.setPublicKey(createWeId.getUserWeIdPublicKey().getPublicKey());
        setAuthenticationArgs.setUserWeIdPrivateKey(new WeIdPrivateKey());
        setAuthenticationArgs.getUserWeIdPrivateKey()
                .setPrivateKey(createWeId.getUserWeIdPrivateKey().getPrivateKey());

        return setAuthenticationArgs;
    }

    /**
     * build default RegisterAuthorityIssuerArgs.
     *
     * @return RegisterAuthorityIssuerArgs
     */
    public static RegisterAuthorityIssuerArgs buildRegisterAuthorityIssuerArgs(
            CreateWeIdDataResult createWeId,
            String privateKey) {

        AuthorityIssuer authorityIssuer = new AuthorityIssuer();
        authorityIssuer.setWeId(createWeId.getWeId());
        authorityIssuer.setName("rocky");
        authorityIssuer.setAccValue("0");

        RegisterAuthorityIssuerArgs registerAuthorityIssuerArgs = new RegisterAuthorityIssuerArgs();
        registerAuthorityIssuerArgs.setAuthorityIssuer(authorityIssuer);
        registerAuthorityIssuerArgs.setWeIdPrivateKey(new WeIdPrivateKey());
        registerAuthorityIssuerArgs.getWeIdPrivateKey().setPrivateKey(privateKey);

        return registerAuthorityIssuerArgs;
    }

    /**
     * buildSetPublicKeyArgs.
     *
     * @param createWeId WeId
     * @return SetServiceArgs
     */
    public static SetServiceArgs buildSetServiceArgs(CreateWeIdDataResult createWeId) {

        SetServiceArgs setServiceArgs = new SetServiceArgs();
        setServiceArgs.setWeId(createWeId.getWeId());
        setServiceArgs.setType("driveCardService");
        setServiceArgs.setServiceEndpoint("https://weidentity.webank.com/endpoint/xxxxx");
        setServiceArgs.setUserWeIdPrivateKey(new WeIdPrivateKey());
        setServiceArgs.getUserWeIdPrivateKey()
                .setPrivateKey(createWeId.getUserWeIdPrivateKey().getPrivateKey());

        return setServiceArgs;
    }

    /**
     * build cpt json schema.
     *
     * @return HashMap
     */
    public static HashMap<String, Object> buildCptJsonSchema() {

        HashMap<String, Object> cptJsonSchemaNew = new HashMap<String, Object>(3);
        cptJsonSchemaNew.put(JsonSchemaConstant.TITLE_KEY, "Digital Identity");
        cptJsonSchemaNew.put(JsonSchemaConstant.DESCRIPTION_KEY, "this is a cpt template");

        HashMap<String, Object> propertitesMap1 = new HashMap<String, Object>(2);
        propertitesMap1.put(JsonSchemaConstant.TYPE_KEY, JsonSchemaConstant.DATA_TYPE_STRING);
        propertitesMap1.put(JsonSchemaConstant.DESCRIPTION_KEY, "this is name");

        String[] genderEnum = {"F", "M"};
        HashMap<String, Object> propertitesMap2 = new HashMap<String, Object>(2);
        propertitesMap2.put(JsonSchemaConstant.TYPE_KEY, JsonSchemaConstant.DATA_TYPE_STRING);
        propertitesMap2.put(JsonSchemaConstant.DATA_TYPE_ENUM, genderEnum);

        HashMap<String, Object> propertitesMap3 = new HashMap<String, Object>(2);
        propertitesMap3.put(JsonSchemaConstant.TYPE_KEY, JsonSchemaConstant.DATA_TYPE_NUMBER);
        propertitesMap3.put(JsonSchemaConstant.DESCRIPTION_KEY, "this is age");

        HashMap<String, Object> propertitesMap4 = new HashMap<String, Object>(2);
        propertitesMap4.put(JsonSchemaConstant.TYPE_KEY, JsonSchemaConstant.DATA_TYPE_STRING);
        propertitesMap4.put(JsonSchemaConstant.DESCRIPTION_KEY, "this is weid");

        HashMap<String, Object> cptJsonSchema = new HashMap<String, Object>(3);
        cptJsonSchema.put("name", propertitesMap1);
        cptJsonSchema.put("gender", propertitesMap2);
        cptJsonSchema.put("age", propertitesMap3);
        cptJsonSchema.put("id", propertitesMap4);
        cptJsonSchemaNew.put(JsonSchemaConstant.PROPERTIES_KEY, cptJsonSchema);

        String[] genderRequired = {"id", "name", "gender"};
        cptJsonSchemaNew.put(JsonSchemaConstant.REQUIRED_KEY, genderRequired);

        return cptJsonSchemaNew;
    }

    public static String readPrivateKeyFromFile(String fileName) {

        BufferedReader br = null;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        StringBuffer privateKey = new StringBuffer();

        URL fileUrl = IsAuthorityIssuer.class.getClassLoader().getResource(fileName);
        if (fileUrl == null) {
            return privateKey.toString();
        }

        String filePath = fileUrl.getFile();
        if (filePath == null) {
            return privateKey.toString();
        }

        try {
            fis = new FileInputStream(fileUrl.getFile());
            isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            br = new BufferedReader(isr);

            String line = null;
            while ((line = br.readLine()) != null) {
                privateKey.append(line);
            }
        } catch (Exception e) {
            logger.error("read privateKey from {} failed, error:{}", fileName, e);
        } finally {
            closeStream(br, fis, isr);
        }

        return privateKey.toString();
    }

    private static void closeStream(BufferedReader br, FileInputStream fis, InputStreamReader isr) {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                logger.error("BufferedReader close error:", e);
            }
        }
        if (isr != null) {
            try {
                isr.close();
            } catch (IOException e) {
                logger.error("InputStreamReader close error:", e);
            }
        }
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                logger.error("FileInputStream close error:", e);
            }
        }
    }
}
