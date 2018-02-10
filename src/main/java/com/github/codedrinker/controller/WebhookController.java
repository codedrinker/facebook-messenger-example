package com.github.codedrinker.controller;

import com.alibaba.fastjson.JSON;
import com.github.codedrinker.fm.FMClient;
import com.github.codedrinker.fm.aspect.FMResultAspect;
import com.github.codedrinker.fm.builder.FMCallToActionBuilder;
import com.github.codedrinker.fm.builder.FMSettingMessageBuilder;
import com.github.codedrinker.fm.entity.FMReceiveMessage;
import com.github.codedrinker.fm.entity.FMResult;
import com.github.codedrinker.fm.entity.FMSettingMessage;
import com.github.codedrinker.fm.handler.FMDefaultMessageDeliveryHandler;
import com.github.codedrinker.fm.handler.FMDefaultMessageReadHandler;
import com.github.codedrinker.fm.handler.FMMessagePostBackHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by codedrinker on 10/02/2018.
 */
@Controller
public class WebhookController {
    private static boolean isFirstHook = true;

    @RequestMapping(value = "/check", method = RequestMethod.GET)
    @ResponseBody
    public String check() {
        return "ok";
    }

    @RequestMapping(value = "/webhook", method = RequestMethod.GET)
    @ResponseBody
    public void getWebhook(HttpServletRequest request, HttpServletResponse response) {
        String mode = request.getParameter("hub.mode");
        String token = request.getParameter("hub.verify_token");
        if (StringUtils.equals("subscribe", mode) && StringUtils.equals("EAACTECZA84coBALuEyN7NmDWvgvkR0P6H7IYA8T0jd0s3tb4OZBLN7aAT9WOfneKnrMhX6nBQKLKXcREP0dgsbwJkXHTRe2xiXscXlX1tDZAEkp47ACZA3ZACOOewA7ZCERZAvu4S6DwZA2imjf23kX1yb7HD74TjsJ41GV6wrZAtzQZDZD", token)) {
            String parameter = request.getParameter("hub.challenge");
            try {
                response.getWriter().println(parameter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("failed");
            response.setStatus(403);
        }
    }

    @RequestMapping(value = "/webhook", method = RequestMethod.POST)
    @ResponseBody
    public void postWebhook(HttpServletRequest request, HttpServletResponse response) {
        try {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String payload = buffer.toString();

            String xHubSignature = request.getHeader("X-Hub-Signature");
            FMClient fmClient = FMClient.getInstance();
            fmClient.withAccessToken("EAACTECZA84coBALuEyN7NmDWvgvkR0P6H7IYA8T0jd0s3tb4OZBLN7aAT9WOfneKnrMhX6nBQKLKXcREP0dgsbwJkXHTRe2xiXscXlX1tDZAEkp47ACZA3ZACOOewA7ZCERZAvu4S6DwZA2imjf23kX1yb7HD74TjsJ41GV6wrZAtzQZDZD")
                    .withAccessSecret("d15872fbd9ec0c3f6c32be4be818c7a5")
                    .withFmMessagePostBackHandler(new FMMessagePostBackHandler() {
                        @Override
                        public void handle(FMReceiveMessage.Messaging messaging) {
                            System.out.println(JSON.toJSONString(messaging, true));
                        }
                    })
                    .withFmMessageHandler(new MessengerHandler())
                    .withFmMessageReadHandler(new FMDefaultMessageReadHandler())
                    .withFmMessageDeliveryHandler(new FMDefaultMessageDeliveryHandler())
                    .withFmResultAspect(new FMResultAspect() {
                        @Override
                        public void handle(FMResult fmResult) {
                            System.out.println(JSON.toJSONString(fmClient, true));
                        }
                    });
            boolean signature = fmClient.signature(payload, xHubSignature);
            if (signature) {
                if (isFirstHook) {
                    fmClient.sendSetting(FMSettingMessageBuilder
                            .defaultBuilder()
                            .withGreeting("Welcome to our messenger, you can buy anything.")
                            .withSettingType(FMSettingMessage.SettingType.greeting)
                            .build());
                    fmClient.sendSetting(FMSettingMessageBuilder
                            .defaultBuilder()
                            .withSettingType(FMSettingMessage.SettingType.call_to_actions)
                            .withThreadState(FMSettingMessage.ThreadState.new_thread)
                            .withCallToAction(FMCallToActionBuilder
                                    .defaultBuilder()
                                    .withPayload("GETSTARTED")
                                    .withType(FMSettingMessage.CallActionType.postback)
                                    .build())
                            .build());
                    fmClient.sendSetting(FMSettingMessageBuilder.defaultBuilder()
                            .withSettingType(FMSettingMessage.SettingType.call_to_actions)
                            .withThreadState(FMSettingMessage.ThreadState.existing_thread)
                            .withCallToAction(FMCallToActionBuilder
                                            .defaultBuilder()
                                            .withPayload("DAILYFEATURED")
                                            .withTitle("Daily Featured")
                                            .withType(FMSettingMessage.CallActionType.postback)
                                            .build(),
                                    FMCallToActionBuilder
                                            .defaultBuilder()
                                            .withPayload("POPULARTOPICS")
                                            .withTitle("Popular Topics")
                                            .withType(FMSettingMessage.CallActionType.postback)
                                            .build())
                            .build());
                    isFirstHook = false;
                }
                fmClient.dispatch(payload);
            } else {
                response.setStatus(403);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
