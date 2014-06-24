package controllers;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.traffic.plane.AirRoute;
import models.traffic.plane.FlightPrice;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;


/**
 * 交通相关
 *
 * @author Zephyre
 */
public class TrafficCtrl extends Controller {

    /**
     * 按照航班号获得航班信息。
     *
     * @param flightCode 航班号。
     * @return 航班信息。
     */
    public static Result getAirRouteByCode(String flightCode) {
        AirRoute route = AirRoute.finder.byId(flightCode);
        if (route == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT,
                    String.format("Invalid flight code: %s", flightCode));
        return Utils.createResponse(ErrorCode.NORMAL, getAirRouteNode(route));
    }

    /**
     * 获得航班信息
     *
     * @param departure  出发地id
     * @param arrival    到达地id
     * @param sort       排序方式
     * @param timeFilter 出发时间过滤
     * @param page       分页偏移量
     * @param pageSize   页面大小
     * @return 航班列表
     */
    public static Result getAirRoutes(long departure, long arrival, String sortType, String sort,
                                      String timeFilterType, int timeFilter, int page, int pageSize) {
        ExpressionList<AirRoute> expList = AirRoute.finder.fetch("airline").fetch("priceList")
                .fetch("departure").fetch("departure.locality")
                .fetch("arrival").fetch("arrival.locality")
                .where().eq("departure.locality.id", departure).eq("arrival.locality.id", arrival);

        String filterField = (timeFilterType.equals("arr") ? "arrivalTime" : "departureTime");
        if (timeFilter == 1)
            expList = expList.ge(filterField, Time.valueOf("06:00:00")).le(filterField, Time.valueOf("12:00:00"));
        else if (timeFilter == 2)
            expList = expList.ge(filterField, Time.valueOf("12:00:00")).le(filterField, Time.valueOf("18:00:00"));
        else if (timeFilter == 3)
            expList = expList.disjunction().add(Expr.ge(filterField, Time.valueOf("18:00:00")))
                    .add(Expr.le(filterField, Time.valueOf("06:00:00")));

        Query<AirRoute> query;
        String field;
        switch (sortType) {
            case "dep":
                field = "departureTime";
                break;
            case "arr":
                field = "arrivalTime";
                break;
            case "price":
            default:
                field = "price";
                break;
        }

        if (sort.equals("dsc"))
            query = expList.order().desc(field);
        else
            query = expList.order().asc(field);

        List<AirRoute> routeList = query.setFirstRow(page * pageSize).setMaxRows(pageSize).findList();

        List<JsonNode> result = new ArrayList<>();
        for (AirRoute route : routeList)
            result.add(getAirRouteNode(route));

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
    }

    private static ObjectNode getAirRouteNode(AirRoute route) {
        ObjectNode routeJson = Json.newObject();
        routeJson.put("flightCode", route.flightCode);
        routeJson.put("airlineName", route.airline.airlineFullName);
        routeJson.put("departureAirport", route.departure.name);
        routeJson.put("arrivalAirport", route.arrival.name);
        String terminalStr = route.departureTerminal;
        if (terminalStr != null && !terminalStr.isEmpty())
            routeJson.put("departureTerminal", terminalStr);
        terminalStr = route.arrivalTerminal;
        if (terminalStr != null && !terminalStr.isEmpty())
            routeJson.put("arrivalTerminal", terminalStr);
        routeJson.put("departureLocality", route.departure.locality.localLocalityName);
        routeJson.put("arrivalLocality", route.arrival.locality.localLocalityName);
        routeJson.put("distance", route.distance);

        routeJson.put("airlineCode", route.airline.airlineCode);
        routeJson.put("departureTime", route.departureTime.toString());
        routeJson.put("arrivalTime", route.arrivalTime.toString());
        routeJson.put("dayLag", route.dayLag);
        routeJson.put("duration", route.duration);
        Float onTime = route.onTimeStat;
        if (onTime > 0)
            routeJson.put("onTimeRate", (int) (onTime * 100) / 100.0);
        if (!route.priceList.isEmpty()) {
            FlightPrice price = route.priceList.get(0);
            routeJson.put("price", price.ticketPrice / 100.0);
            routeJson.put("discount", (int) (price.discount * 100) / 100.0);
            routeJson.put("tax", price.tax / 100.0);
            routeJson.put("fuelSurcharge", price.fuelSurcharge / 100.0);
        }
        return routeJson;
    }
}