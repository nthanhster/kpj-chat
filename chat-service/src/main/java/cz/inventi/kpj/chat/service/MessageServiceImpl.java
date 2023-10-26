package cz.inventi.kpj.chat.service;

import cz.inventi.kpj.chat.mapper.MessageMapper;
import cz.inventi.kpj.chat.messaging.MessageBroker;
import cz.inventi.kpj.chat.messaging.MessageListener;
import cz.inventi.kpj.chat.model.MessageEvent;
import cz.inventi.kpj.chat.model.MessageType;
import cz.inventi.kpj.openapi.model.Message;
import cz.inventi.kpj.openapi.model.MessageRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
public class MessageServiceImpl implements MessageService, MessageListener {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private MessageBroker messageBroker;

    @PostConstruct
    private void init() {
        messageBroker.registerListener(this);
    }

    private final List<Message> messages = new ArrayList<>();

    @Override
    public List<Message> listMessages() {
        return messages;
    }

    @Override
    public UUID createMessage(MessageRequest messageRequest) {

        // TODO: map messageRequest to messageEvent; set type, id and created; publish messageEvent via messageBroker and return its id
        MessageEvent messageEvent = messageMapper.requestToEvent(messageRequest);
        messageEvent.setType(MessageType.MESSAGE); // Assuming MessageType.MESSAGE represents a regular message.

        // Set the remaining fields
        messageEvent.setId(UUID.randomUUID());
        messageEvent.setCreated(OffsetDateTime.now());

        // Publish the messageEvent via messageBroker
        messageBroker.publish(messageEvent);
        log.info(messageEvent.getId());
        return messageEvent.getId();
    }

    @Override
    public void onMessage(MessageEvent messageEvent) {
        // TODO: switch over messageEvent type; if it is a message, map it to Message and add it to messages list; if it is a presence check, just log it; if it is an unknown type, log a warning

        switch (messageEvent.getType()) {
            case MESSAGE:
                messages.add(messageMapper.eventToDTO(messageEvent));
                break;
            case PRESENCE:
                log.info("Received a presence check.");
                break;
            default:
                log.warn("Received an unknown message type: " + messageEvent.getType());
                break;
            }
        }


        // TODO: execute every 10 seconds (*/10 * * * * *)
    @Scheduled(cron = "*/10 * * * * *")
    public void sendPresence() {
        // TODO: create a new messageEvent with type PRESENCE, id, created and name; publish it via messageBroker and log an info message
        MessageEvent presenceEvent = new MessageEvent();
        presenceEvent.setType(MessageType.PRESENCE);
        presenceEvent.setId(UUID.randomUUID());
        presenceEvent.setCreated(OffsetDateTime.now());
        presenceEvent.setName("Thanh Here");
        log.info(presenceEvent.getId());
        messageBroker.publish(presenceEvent);
        log.info("Sent a presence check.");





    }
}
