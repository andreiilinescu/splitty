/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import client.services.GsonInstantTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.moandjiezana.toml.Toml;
import commons.*;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.springframework.messaging.converter.GsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class ServerUtils {

    private final String serverAddress;
    private final AppConfig appConfig;
    private final MailConfig mailConfig;

    public MailConfig getMailConfig() {
        return mailConfig;
    }

    private ExecutorService exec = Executors.newSingleThreadExecutor();
    private StompSession session;

    public ServerUtils() {
        URL resource = getClass().getClassLoader().getResource("client/server_config.toml");

        File config;

        if (resource == null) {
            throw new IllegalArgumentException("File not found!");
        } else {
            try {
                config = new File(resource.toURI());
            } catch (Exception e) {
                throw new IllegalArgumentException("URI not parsable");
            }
        }

        Toml toml;

        try {
            toml = new Toml().read(config);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse toml file: " + e.getMessage());
        }

        String address = toml.getString("address");
        long port = toml.getLong("port");
        appConfig = toml.to(AppConfig.class);
        mailConfig = appConfig.getMailConfig();

        serverAddress = address + ":" + port + "/";
        session = connect(toml.getString("websocket")+":"+port+"/websocket");
        System.out.println(serverAddress);
    }

    public List<Quote> getQuotes() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/quotes")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Quote>>() {
                });
    }

    public Participant addParticipant(UUID eventId, Participant participant) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + eventId + "/participants")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(participant, APPLICATION_JSON), Participant.class);
    }

    public Participant getParticipant(UUID eventId, UUID participantId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + eventId + "/participants/" + participantId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<Participant>(){});
    }

    public Participant updateParticipant(Event event, Participant participant) {
        participant.setEventPartOf(event);
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + event.getId() + "/participants/" + participant.getId())
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(participant, APPLICATION_JSON), Participant.class);
    }

    public Event addEvent(Event event) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(event, APPLICATION_JSON), Event.class);
    }

    public void removeParticipant(Event event, Participant participant) {
        ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + event.getId() + "/participants/" + participant.getId())
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .delete();
    }

    public Event updateEvent(Event event) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + event.getId())
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(event, APPLICATION_JSON), Event.class);
    }

    public List<Event> getEvents() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(serverAddress).path("api/events") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<List<Event>>() {
                });
    }

    public Event getEvent(UUID id) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(serverAddress).path("api/events/" + id) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<Event>() {
                });
    }

    public Event joinEvent(String inviteCode) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(serverAddress).path("api/events/join/" + inviteCode) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<Event>() {
                });
    }

    public Event removeEvent(UUID id) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(serverAddress).path("api/events/" + id) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .delete(new GenericType<Event>() {
                });
    }

    public Expense addExpense(UUID eventId, Expense exp) {
        System.out.println("ServerUtils: " + exp.getPaidBy());
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("/api/events/" + eventId + "/expenses")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(exp, APPLICATION_JSON), Expense.class);
    }

    public void updateExpense(UUID eventId, UUID expenseId, Expense exp){
        System.out.println(eventId);
        ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("/api/events/" + eventId + "/expenses/" + expenseId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(exp, APPLICATION_JSON), Expense.class);
    }

    public List<Expense> getExpensesByEvent(UUID eventId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("/api/events/" + eventId + "/expenses")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Expense>>() {
                });
    }

    /**
     * Listen for new events
     */
    public void listenEvents(Consumer<EventLongPollingWrapper> eventConsumer) {
        exec.submit(() -> {
            System.out.println("Listening for updates");
            while (!Thread.currentThread().isInterrupted()) {
                var res = ClientBuilder.newClient(new ClientConfig())
                        .target(serverAddress).path("/api/events/subscribe")
                        .request(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .get(Response.class);
                System.out.println("Got response");
                System.out.println(res.toString());
                if (res.getStatus() == 204) {
                    System.out.println("No content");
                    continue;
                }
                if (res.getStatus() != 200) {
                    System.out.println("Error: " + res.getStatus());
                    continue;
                }
                EventLongPollingWrapper wrapper = null;
                try {
                    wrapper = res.readEntity(EventLongPollingWrapper.class);
                }
                catch (ProcessingException e){
                    e.printStackTrace();
                }
                if (wrapper == null) {
                    System.out.println("No event");
                    continue;
                }
                eventConsumer.accept(wrapper);
            }
            System.out.println("Stopped listening for updates");
        });
        System.out.println("Stopped listening for updates 2");
    }

    public void stopThread() {
        exec.shutdownNow();
    }


    public String checkPassword(String password) {
        return ClientBuilder.newClient(new ClientConfig())//
                .target(serverAddress).path("admin/login")//
                .request(APPLICATION_JSON)//
                .accept(APPLICATION_JSON)//
                .post(Entity.entity(password, APPLICATION_JSON), String.class);
    }

    public String getAdmin() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(serverAddress).path("admin/") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<String>() {
                });
    }

    public void sendEmailInvitation(String toEmail, String inviteCode, String creator) {
        JsonObject body = new JsonObject();
        body.addProperty("senderEmail", mailConfig.getUsername());
        body.addProperty("toEmail", toEmail);
        body.addProperty("inviteCode", inviteCode);
        body.addProperty("creator", creator);
        body.addProperty("password", mailConfig.getPassword());
        body.addProperty("host", mailConfig.getHost());
        body.addProperty("port", mailConfig.getPort());
        body.addProperty("smtpAuth", mailConfig.isSmtpAuth());
        body.addProperty("startTls", mailConfig.isStartTls());

        System.out.println(body);
        ClientBuilder.newClient(new ClientConfig())//
                .target(serverAddress).path("api/mail")//
                .request(APPLICATION_JSON)//
                .accept(APPLICATION_JSON)//
                .post(Entity.entity(body.toString(), APPLICATION_JSON), String.class);
    }

    public void sendEmail(String toEmail, String subject, String content) {
        JsonObject body = new JsonObject();
        body.addProperty("senderEmail", mailConfig.getUsername());
        body.addProperty("toEmail", toEmail);
        body.addProperty("password", mailConfig.getPassword());
        body.addProperty("host", mailConfig.getHost());
        body.addProperty("port", mailConfig.getPort());
        body.addProperty("smtpAuth", mailConfig.isSmtpAuth());
        body.addProperty("startTls", mailConfig.isStartTls());
        body.addProperty("body", content);
        body.addProperty("subject", subject);

        ClientBuilder.newClient(new ClientConfig())//
                .target(serverAddress).path("api/mail/custom")//
                .request(APPLICATION_JSON)//
                .accept(APPLICATION_JSON)//
                .post(Entity.entity(body.toString(), APPLICATION_JSON), String.class);
    }

    public double convert(double amount, String from, String to, Instant when) {
        JsonObject body = new JsonObject();
        body.addProperty("amount", amount);
        body.addProperty("from", from);
        body.addProperty("to", to);
        body.addProperty("when", when.toString());
        System.out.println(body);
        return ClientBuilder.newClient(new ClientConfig())//
                .target(serverAddress).path("api/convert")//
                .request(APPLICATION_JSON)//
                .accept(APPLICATION_JSON)//
                .post(Entity.entity(body.toString(), APPLICATION_JSON), Double.class);
    }



    public List<Tag> getTagsFromEvent(UUID eventId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + eventId + "/tags")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Tag>>(){});
    }

    public Tag getTag(UUID eventId, UUID id) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + eventId + "/tags/" + id)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<Tag>(){});
    }

    public Tag addTag(UUID eventId, Tag t) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + eventId + "/tags/")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(t, APPLICATION_JSON), Tag.class);
    }

    public void removeTag(UUID eventId, UUID id) {
        ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + eventId + "/tags/" + id)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .delete();
    }

    public Tag updateTag(UUID eventId, UUID id, Tag t) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + eventId + "/tags/" + id)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(t, APPLICATION_JSON), Tag.class);
    }

    public void addExpenseTag(UUID eventId, UUID id, UUID expenseId) {
        ClientBuilder.newClient(new ClientConfig())
                .target(serverAddress).path("api/events/" + eventId + "/tags/" + id + "/expenses")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(expenseId, APPLICATION_JSON), Tag.class);
    }

    private StompSession connect(String url){
        var client = new StandardWebSocketClient();
        var stomp = new WebSocketStompClient(client);
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Instant.class, new GsonInstantTypeAdapter())
                .create();
        stomp.setMessageConverter(new GsonMessageConverter(gson));
        try {
            return stomp.connect(url, new StompSessionHandlerAdapter() {}).get();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        } catch (ExecutionException e){
            throw new RuntimeException();
        }
        throw new IllegalStateException();
    }

    public void registerForMessages(String dest, Consumer<Event> consumer){
        session.subscribe(dest, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Event.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                consumer.accept((Event) payload);
            }
        });
    }

    public void send(String dest, Event e){
        System.out.println("I send message to " + dest + " for " + e.getTitle() + " with " + e.getParticipants().size() + " participants");
        session.send(dest, e);
    }
}