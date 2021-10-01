package com.revature.get_sets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.revature.documents.Set;
import com.revature.documents.SetDto;
import com.revature.documents.User;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GetHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Gson mapper = new GsonBuilder().setPrettyPrinting().create();
    private final SetRepo setRepo;
    private final UserRepo userRepo;

    public GetHandler(){

        setRepo = new SetRepo();
        userRepo = new UserRepo();
    }
    public GetHandler(SetRepo setRepo, UserRepo userRepo){

        this.setRepo = setRepo;
        this.userRepo = userRepo;
    }

    /**
     *  This if else statement will check if the apiGatewayProxyRequestEvent.getQueryStringParameters().keySet().toString()
     *   is one of the parameters to look for sets. If none of these match then it will return all Sets.
     * @param apiGatewayProxyRequestEvent
     * @param context
     * @return APIGatewayProxyResponseEvent
     * @author Jose Tejada
     */

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {

        LambdaLogger logger = context.getLogger();
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        logger.log("RECEIVED EVENT: " + apiGatewayProxyRequestEvent.getQueryStringParameters().keySet());

        //Just casting to list to avoid using Set which collides with our Set name...
        List<String> queryValues = apiGatewayProxyRequestEvent.getQueryStringParameters().keySet().stream().collect(Collectors.toList());
        List<String> pathValues = apiGatewayProxyRequestEvent.getPathParameters().keySet().stream().collect(Collectors.toList());

        System.out.println(queryValues);
        System.out.println(pathValues);

        if (pathValues.contains("id")) {


            System.out.println("will return list of sets that match the tags");
            String id = apiGatewayProxyRequestEvent.getPathParameters().get("id");

            try {
                Set sets = setRepo.getSetById(id);
                responseEvent.setBody(mapper.toJson(sets));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                logger.log(e.getMessage());
            }



        } else if (queryValues.contains("tags")) {
            //TODO finish implementing getSet by tags.

//           String tags = apiGatewayProxyRequestEvent.getQueryStringParameters().get("tags");
//            List<Set> respBody = new ArrayList<>();
//
//            try {
//
//                logger.log("Tags" + tags);
//                PageIterable<Set> sets = setRepo.getAllSets();
//                responseEvent.setBody(mapper.toJson(respBody));
//
//
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//                logger.log(e.getMessage());
//            }





        } else if (queryValues.contains("user_id")) {


            String user_id = null;
            try {
                user_id = apiGatewayProxyRequestEvent.getQueryStringParameters().get("user_id");
                User users = userRepo.getSetById(user_id);
                responseEvent.setBody(mapper.toJson(users.getCreatedSets()));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                logger.log(e.getMessage());
            }




        } else {
            System.out.println("get all sets in the DB");
            List<SetDto> respBody = new ArrayList<>();

            try {
                PageIterable<Set> sets = setRepo.getAllSets();
                sets.stream().forEach(page -> page.items().forEach(set -> {

                    //this turns the tag object into a list of strings
                    List<String> tags = new ArrayList<>();

                    logger.log("Set:" + set + "\n");
                    respBody.add(SetDto.builder()
                            .setName(set.getSetName())
                            .tags(set.getTags())
                            .cards(set.getCards())
                            .author(set.getAuthor())
                            .isPublic(set.isPublic())
                            .views(set.getViews())
                            .plays(set.getPlays())
                            .studies(set.getStudies())
                            .favorites(set.getFavorites()).build());

                }));
                responseEvent.setBody(mapper.toJson(respBody));


            } catch (Exception e) {
                System.out.println(e.getMessage());
                logger.log(e.getMessage());
            }}


            System.out.println(responseEvent);
            return responseEvent;


        }
    }