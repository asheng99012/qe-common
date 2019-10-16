package com.dankegongyu.app.common;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dankegongyu.app.common.httpRpc.PostJson;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Files;
import io.swagger.models.Swagger;
import org.apache.http.entity.StringEntity;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SwaggerUtils {
    public static void createYaml(String groupName, String groupId, String artifactId, String version) throws IOException {
        Documentation documentation = AppUtils.getBean(DocumentationCache.class).documentationByGroup(groupName);
        Swagger swagger = AppUtils.getBean(ServiceModelToSwagger2Mapper.class).mapDocumentation(documentation);
        swagger.setBasePath(null);
        swagger.setHost(null);
        String json = AppUtils.getBean(JsonSerializer.class).toJson(swagger).value();
        System.out.println(json);
        System.out.println("+++++++++++++++++++++++");
        String openApi = new PostJson() {
            @Override
            public String getDataId() {
                return null;
            }

            @Override
            public String getUrl() {
                return "http://converter.swagger.io/api/convert";
            }

            @Override
            public Object prepareData() {
                JSONObject jsono = JSON.parseObject(json);
                return jsono;
            }

            @Override
            public StringEntity getData() {
                StringEntity entity = new StringEntity(JsonUtils.toJson(json), "utf-8");
                entity.setContentType("application/json");
                return entity;
            }

            @Override
            public Object dealResult(String ret) {
                ret = ret.replaceAll("(?)Using[A-Z]+\\_\\d+", "");
                return ret;
            }

            @Override
            public Map<String, String> getHeaders() {
                return new HashMap<String, String>() {
                    {
                        this.put("Content-Type", "application/json");
                    }
                };
            }
        }.send("").toString();

        ObjectNode jsonNodeTree = (ObjectNode) new ObjectMapper().readTree(openApi);
        ObjectNode jsonNode = (ObjectNode) jsonNodeTree.get("info");
        jsonNode.put("groupId", groupId);
        jsonNode.put("artifactId", artifactId);
        jsonNode.put("version", version);
        jsonNodeTree.remove("servers");
        String jsonAsYaml = new YAMLMapper().writeValueAsString(jsonNodeTree);
        jsonAsYaml = jsonAsYaml.replaceAll("---\n", "");
        Files.write(jsonAsYaml.getBytes(), new File("src/main/resources/" + groupId + "-" + artifactId + "-spec.yaml"));
    }
}
