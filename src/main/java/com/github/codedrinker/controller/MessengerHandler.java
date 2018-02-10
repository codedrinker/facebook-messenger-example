package com.github.codedrinker.controller;

import com.alibaba.fastjson.JSON;
import com.github.codedrinker.fm.builder.*;
import com.github.codedrinker.fm.entity.FMReceiveMessage;
import com.github.codedrinker.fm.entity.FMReplyMessage;
import com.github.codedrinker.fm.handler.FMMessageHandler;
import com.github.codedrinker.fm.provider.FMProvider;
import org.apache.commons.lang3.StringUtils;


/**
 * Created by codedrinker on 10/02/2018.
 */
public class MessengerHandler extends FMMessageHandler {
    @Override
    public void handle(FMReceiveMessage.Messaging messaging) {
        FMReceiveMessage.Messaging.Message message = messaging.getMessage();
        if (message.getQuick_reply() != null && StringUtils.isNotBlank(message.getQuick_reply().getPayload())) {
            //因为快速回复和按钮不一样，不能产生Postback，所以需要在这里处理quick reply的事件
            long time = System.currentTimeMillis();
            System.out.println(JSON.toJSONString(message, true));
        } else {
            //如果不是 quick reply，通过文本判断内容
            String text = message.getText();
            if (text == null) return;
            switch (text) {
                case "1":
                    FMProvider.sendMessage(FMReplyMessageBuilder
                            .textBuilder(messaging.getSender().getId(), "Text Message.")
                            .build());
                    break;
                case "2":
                    FMProvider.sendMessage(FMReplyMessageBuilder
                            .defaultBuilder()
                            .withRecipient(messaging.getSender().getId())
                            .withMessage(FMMessageBuilder
                                    .defaultBuilder()
                                    .withAttachment(
                                            FMAttachmentBuilder
                                                    .defaultBuilder()
                                                    .withText("Text messagen with btns, pls Click Me")
                                                    .withType(FMReplyMessage.TemplateType.button)
                                                    .withButtons(FMButtonBuilder
                                                            .postbackBuilder()
                                                            .withPayload("CLICKME")
                                                            .withTitle("CLICK ME")
                                                            .build())
                                                    .build())
                                    .build())
                            .build());
                    break;
                case "3":
                    FMProvider.sendMessage(FMReplyMessageBuilder
                            .defaultBuilder()
                            .withRecipient(messaging.getSender().getId())
                            .withMessage(FMMessageBuilder
                                    .defaultBuilder()
                                    .withAttachment(FMAttachmentBuilder.defaultBuilder().withElements(FMElementBuilder
                                            .defaultBuilder()
                                            .withImageUrl("https://scontent-nrt1-1.xx.fbcdn.net/v/t1.0-1/p480x480/15355831_1416125901771040_9115209388184344039_n.jpg?oh=cb178c86ea238d85456dcf8278c805f4&oe=5B24DA25")
                                            .withTitle("Card Title")
                                            .withSubtitle("Card Subtitle")
                                            .withButtons(
                                                    FMButtonBuilder
                                                            .postbackBuilder()
                                                            .withPayload("Share")
                                                            .withTitle("Share")
                                                            .build())
                                            .build()).build())
                                    .build())
                            .build());
                    break;
                case "4":
                    FMProvider.sendMessage(FMReplyMessageBuilder
                            .textBuilder(messaging.getSender().getId(), "Quick Replies")
                            .withQuickReplies(
                                    FMQuickReplyBuilder
                                            .defaultBuilder()
                                            .withTitle("1")
                                            .withContentType(FMReplyMessage.QuickReplyType.text)
                                            .withPayload("1")
                                            .build(),
                                    FMQuickReplyBuilder
                                            .defaultBuilder()
                                            .withTitle("2")
                                            .withContentType(FMReplyMessage.QuickReplyType.text)
                                            .withPayload("2")
                                            .build())
                            .build());
                    break;
            }
        }
    }
}
