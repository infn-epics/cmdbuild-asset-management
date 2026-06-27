/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.command;


import org.cmdbuild.chat.ChatMessage;
import org.cmdbuild.chat.ChatService;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.service.rest.v4.endpoint.ChatMessageWs;
import org.springframework.stereotype.Component;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.cmdbuild.chat.ChatMessage.*;
import static org.cmdbuild.chat.ChatMessageStatus.CMS_ARCHIVED;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

/**
 * @author ldare
 */
@Component
public class ChatMessageWsCommand {

    private final ChatService chatService;

    public ChatMessageWsCommand(ChatService chatService) {
        this.chatService = checkNotNull(chatService);
    }

    public PagedElements<ChatMessage> doGetMessages(WsQueryOptions options) {
        return chatService.getMessagesForCurrentUser(options.getQuery().mapAttrNames(map(
                "sourceType", CHAT_MESSAGE_ATTR_SOURCE_TYPE,
                "type", CHAT_MESSAGE_ATTR_TYPE,
                "status", CHAT_MESSAGE_ATTR_STATUS,
                "timestamp", CHAT_MESSAGE_ATTR_TIMESTAMP,
                "sourceName", CHAT_MESSAGE_ATTR_SOURCE_NAME,
                "target", CHAT_MESSAGE_ATTR_TARGET,
                "thread", CHAT_MESSAGE_ATTR_THREAD
        )));
    }

    public void doUpdateMessage(Long recordId, ChatMessageWs.WsMessageData message) {
        checkArgument(equal(message.getStatus(), CMS_ARCHIVED));
        chatService.archiveMessagesForCurrentUser(list(recordId));
    }

    public ChatMessage doSendMessage(ChatMessageWs.WsMessageData message) {
        return chatService.sendMessage(message.toChatMessageData());
    }

    public void doDeleteMessage(Long recordId) {
        chatService.deleteMessagesForCurrentUser(list(recordId));
    }
}
