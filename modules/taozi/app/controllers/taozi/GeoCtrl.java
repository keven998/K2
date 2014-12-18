package controllers.taozi;

import aizou.core.GeoAPI;
import aizou.core.MiscAPI;
import aizou.core.PoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.AizouException;
import exception.ErrorCode;
import formatter.taozi.geo.DetailedLocalityFormatter;
import formatter.taozi.geo.SimpleCountryFormatter;
import formatter.taozi.geo.SimpleLocalityFormatter;
import formatter.taozi.poi.SimplePOIFormatter;
import models.geo.Country;
import models.geo.Locality;
import models.poi.AbstractPOI;
import org.bson.types.ObjectId;
import play.Configuration;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.Utils;

import java.util.*;

/**
 * 地理相关
 * <p>
 * Created by zephyre on 14-6-20.
 */
public class GeoCtrl extends Controller {
    /**
     * 根据id查看城市详情
     *
     * @param id      城市ID
     * @param noteCnt 游记个数
     * @return
     */
    public static Result getLocality(String id, int noteCnt) {
        try {
            Long userId;
            if (request().hasHeader("UserId"))
                userId = Long.parseLong(request().getHeader("UserId"));
            else
                userId = null;
            Locality locality = GeoAPI.locDetails(id);
            //是否被收藏
            MiscAPI.isFavorite(locality, userId);
            if (locality == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Locality not exist.");
            ObjectNode response = (ObjectNode) new DetailedLocalityFormatter().format(locality);
            //List<TravelNote> tras = TravelNoteAPI.searchNoteByLoc(Arrays.asList(locality.getZhName()), null, 0, noteCnt);
            //List<ObjectNode> objs = new ArrayList<>();
            //for (TravelNote tra : tras) {
            //    objs.add((ObjectNode) new TravelNoteFormatter().format(tra));
            //}
            int imageCnt = locality.getImages() == null ? 0 : locality.getImages().size();
            response.put("imageCnt", imageCnt);
            return Utils.createResponse(ErrorCode.NORMAL, response);
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }

    /**
     * 特定地点美食、景点、购物发现
     *
     * @param locId
     * @param vs
     * @param dinning
     * @param shopping
     * @param page
     * @param pageSize
     * @return
     */
    public static Result exploreDinShop(String locId, boolean vs, boolean dinning, boolean shopping, boolean hotel, boolean restaurant,
                                        int page, int pageSize) {
        //TODO 没有美食/购物的数据
        try {
            ObjectNode results = Json.newObject();
            HashMap<PoiAPI.POIType, String> poiMap = new HashMap<>();
            if (vs)
                poiMap.put(PoiAPI.POIType.VIEW_SPOT, "vs");
            if (dinning)
                poiMap.put(PoiAPI.POIType.DINNING, "dinning");

            if (shopping)
                poiMap.put(PoiAPI.POIType.SHOPPING, "shopping");
            if (hotel)
                poiMap.put(PoiAPI.POIType.HOTEL, "hotel");

            if (restaurant)
                poiMap.put(PoiAPI.POIType.RESTAURANT, "restaurant");

            for (Map.Entry<PoiAPI.POIType, String> entry : poiMap.entrySet()) {
                List<JsonNode> retPoiList = new ArrayList<>();
                PoiAPI.POIType poiType = entry.getKey();
                String poiTypeName = entry.getValue();

                // TODO 暂时返回国内数据
                for (Iterator<? extends AbstractPOI> it = PoiAPI.explore(poiType, null, false, page, pageSize);
                     it.hasNext(); )
                    retPoiList.add(new SimplePOIFormatter().format(it.next()));
                results.put(poiTypeName, Json.toJson(retPoiList));
            }
            return Utils.createResponse(ErrorCode.NORMAL, results);
        } catch (AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    /**
     * 获得国内国外目的地
     *
     * @param abroad
     * @param page
     * @param pageSize
     * @return
     */
    public static Result exploreDestinations(boolean abroad, int page, int pageSize) {
        try {
            List<ObjectNode> objs = new ArrayList<>();
            List<ObjectNode> dests;
            if (abroad) {
                // TODO 桃子上线的国家暂时写到配置文件中
                Configuration config = Configuration.root();
                Map destnations = (Map) config.getObject("destinations");
                String countrysStr = destnations.get("country").toString();
                List<String> countryNames = Arrays.asList(countrysStr.split(Constants.SYMBOL_SLASH));
                List<Country> countryList = GeoAPI.searchCountryByName(countryNames, Constants.ZERO_COUNT, Constants.MAX_COUNT);
                for (Country c : countryList) {
                    ObjectNode node = (ObjectNode) new SimpleCountryFormatter().format(c);
                    dests = getDestinationsNodeByCountry(c.getId(), page, pageSize);
                    node.put("destinations", Json.toJson(dests));
                    objs.add(node);
                }
            } else {
                // TODO 全部取出，不要分页，暂时取100个
                List<Locality> destinations = GeoAPI.getDestinations(abroad, page, Constants.MAX_COUNT);
                for (Locality des : destinations) {
                    objs.add((ObjectNode) new SimpleLocalityFormatter().format(des));
                }
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(objs));
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }

    /**
     * 根据国家取得目的地
     *
     * @param id
     * @param page
     * @param pageSize
     * @return
     * @throws exception.AizouException
     */
    private static List<ObjectNode> getDestinationsNodeByCountry(ObjectId id, int page, int pageSize) throws AizouException {
        List<ObjectNode> result = new ArrayList<>(pageSize);
        List<Locality> localities = GeoAPI.getDestinationsByCountry(id, page, pageSize);
        for (Locality des : localities) {
            result.add((ObjectNode) new SimpleLocalityFormatter().format(des));
        }
        return result;
    }
}
