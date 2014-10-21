package controllers;


import com.fasterxml.jackson.databind.node.ObjectNode;
import core.WeatherAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.morphia.misc.Weather;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

/**
 * @author lxf
 */
public class WeatherCtrl extends Controller {

    public static Result getWeatherDetail(String cityId) throws TravelPiException {
        ObjectNode response = Json.newObject();
        Weather weather=WeatherAPI.weatherDetails(new ObjectId(cityId));
        if (weather==null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid locality id: %d", cityId));
        response.put("weather",weather.toJson());
        return Utils.createResponse(ErrorCode.NORMAL, response);
    }
}
