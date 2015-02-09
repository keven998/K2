package controllers.web;

import aizou.core.LocalityAPI;
import aizou.core.PoiAPI;
import aizou.core.UserAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import exception.AizouException;
import exception.ErrorCode;
import formatter.travelpi.geo.LocalityFormatter;
import formatter.travelpi.geo.SimpleLocalityFormatter;
import formatter.web.misc.ArticleFormatter;
import formatter.web.misc.ArticleSimpleFormatter;
import formatter.web.misc.RecommendationFormatter;
import models.MorphiaFactory;
import models.geo.Locality;
import models.misc.Article;
import models.misc.Recommendation;
import models.poi.AbstractPOI;
import models.poi.ViewSpot;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 其它
 *
 * @author Zephyre
 */
public class MiscCtrl extends Controller {

    /**
     * 获得更新信息
     *
     * @return
     */
    public static Result getUpdates() {
        Http.Request req = request();
        String platform = req.getQueryString("platform");
        if (platform == null)
            // 在1.0版本中，
            platform = req.getQueryString("platformVer");

        String ver = req.getQueryString("v");

        if (ver == null || ver.isEmpty() || platform == null || platform.isEmpty())
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID ARGUMENT");

        platform = platform.toLowerCase();
        ver = ver.toLowerCase();

        double oldVerN = 0;
        String[] oldVerP = ver.split("\\.");
        for (int i = 0; i < oldVerP.length; i++)
            oldVerN += Math.pow(10, -3 * i) * Double.parseDouble(oldVerP[i]);

        Matcher m = Pattern.compile("^(android|ios)").matcher(platform);
        if (!m.find() || !m.group(1).equals("android"))
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("INVALID PLATFORM: %s", platform));

        BasicDBObject ret;
        try {
            DBCollection col = Utils.getMongoClient().getDB("misc").getCollection("MiscInfo");
            ret = (BasicDBObject) col.findOne();
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }

        String newVerS = ret.getString("androidUpdates");
        String[] newVerP = newVerS.split("\\.");
        double newVerN = 0;
        for (int i = 0; i < newVerP.length; i++)
            newVerN += Math.pow(10, -3 * i) * Double.parseDouble(newVerP[i]);

        ObjectNode result = Json.newObject();
        if (newVerN > oldVerN) {
            result.put("update", true);
            result.put("version", newVerS);
            result.put("desc", "");
            result.put("downloadUrl", ret.getString("androidUrl"));
        } else
            result.put("update", false);
        return Utils.createResponse(ErrorCode.NORMAL, result);
    }

    /**
     * 联想提示
     *
     * @param word
     * @param pageSize
     * @return
     */
    public static JsonNode getSuggestionsImpl(String word, boolean loc, boolean vs, boolean hotel, boolean restaurant,
                                              int pageSize) throws AizouException {
        ObjectNode ret = Json.newObject();
        ObjectNode temp;
        List<JsonNode> locList = new ArrayList<>();
        if (loc) {
            Locality item;
            for (Iterator<Locality> it = LocalityAPI.getSuggestion(word, pageSize); it.hasNext(); ) {
                item = it.next();
                temp = Json.newObject();
                temp.put("_id", item.getId().toString());
                temp.put("zhName", item.getZhName());
                locList.add(temp);
            }
        }
        ret.put("loc", Json.toJson(locList));

        List<JsonNode> vsList = new ArrayList<>();
        AbstractPOI item;
        if (vs) {
            for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.VIEW_SPOT, word, pageSize);
                 it.hasNext(); ) {
                item = it.next();
                temp = Json.newObject();
                temp.put("_id", item.getId().toString());
                temp.put("zhName", item.zhName);
                vsList.add(temp);
            }
        }
        ret.put("vs", Json.toJson(vsList));

        List<JsonNode> hotelList = new ArrayList<>();
        if (hotel) {
            for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.HOTEL, word, pageSize);
                 it.hasNext(); ) {
                item = it.next();
                temp = Json.newObject();
                temp.put("_id", item.getId().toString());
                temp.put("zhName", item.zhName);
                hotelList.add(temp);
            }
        }
        ret.put("hotel", Json.toJson(hotelList));

        List<JsonNode> dinningList = new ArrayList<>();
        if (restaurant) {
            for (Iterator<? extends AbstractPOI> it = PoiAPI.getSuggestions(PoiAPI.POIType.RESTAURANT, word, pageSize);
                 it.hasNext(); ) {
                item = it.next();
                temp = Json.newObject();
                temp.put("_id", item.getId().toString());
                temp.put("zhName", item.zhName);
                dinningList.add(temp);
            }

        }
        ret.put("restaurant", Json.toJson(dinningList));

        return ret;
    }


    /**
     * 联想提示
     *
     * @param word
     * @param pageSize
     * @return
     */
    public static Result getSuggestions(String word, boolean loc, boolean vs, boolean hotel, boolean restaurant,
                                        int pageSize) {
        try {
            return Utils.createResponse(ErrorCode.NORMAL, getSuggestionsImpl(word, loc, vs, hotel, restaurant,
                    pageSize));
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }


    }

    /**
     * 广义的发现接口（通过一系列开关来控制）
     *
     * @param loc
     * @param vs
     * @param hotel
     * @param restaurant
     * @param page
     * @param pageSize
     * @return
     */
    public static Result explore(boolean details, boolean loc, boolean vs, boolean hotel, boolean restaurant,
                                 boolean abroad, int page, int pageSize) {
        try {
            return Utils.createResponse(ErrorCode.NORMAL,
                    exploreImpl(details, loc, vs, hotel, restaurant, abroad, page, pageSize));
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }

    public static JsonNode exploreImpl(boolean details, boolean loc, boolean vs, boolean hotel, boolean restaurant,
                                       boolean abroad, int page, int pageSize) throws AizouException {
        ObjectNode results = Json.newObject();

        // 发现城市
        if (loc) {
            List<JsonNode> retLocList = new ArrayList<>();
            // TODO 获得城市信息
            for (Locality locality : LocalityAPI.explore(details, abroad, page, pageSize))
                retLocList.add(LocalityFormatter.getInstance().format(locality));
//                retLocList.add(locality.toJson(2));
            results.put("loc", Json.toJson(retLocList));
        }

        List<PoiAPI.POIType> poiKeyList = new ArrayList<>();
        if (vs)
            poiKeyList.add(PoiAPI.POIType.VIEW_SPOT);
        if (hotel)
            poiKeyList.add(PoiAPI.POIType.HOTEL);
        if (restaurant)
            poiKeyList.add(PoiAPI.POIType.RESTAURANT);

        HashMap<PoiAPI.POIType, String> poiMap = new HashMap<PoiAPI.POIType, String>() {
            {
                put(PoiAPI.POIType.VIEW_SPOT, "vs");
                put(PoiAPI.POIType.HOTEL, "hotel");
                put(PoiAPI.POIType.RESTAURANT, "restaurant");
            }
        };

        for (PoiAPI.POIType poiType : poiKeyList) {
            if (poiType == PoiAPI.POIType.VIEW_SPOT) {
                // TODO 暂时不返回景点推荐数据
                results.put(poiMap.get(poiType), Json.toJson(new ArrayList<>()));
            } else {
                // 发现POI
                List<JsonNode> retPoiList = new ArrayList<>();
                for (Iterator<? extends AbstractPOI> it = PoiAPI.explore(poiType, (ObjectId) null, abroad, page, pageSize);
                     it.hasNext(); )
                    retPoiList.add(it.next().toJson(2));
                results.put(poiMap.get(poiType), Json.toJson(retPoiList));
            }
        }
        return results;
    }

    /**
     * 推荐
     *
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
    public static Result recommend(String type, int page, int pageSize) {
        List<JsonNode> results = new ArrayList<>();

        Datastore ds;
        try {
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            Query<Recommendation> query = ds.createQuery(Recommendation.class);

            query.field("enabled").equal(Boolean.TRUE).field(type).greaterThan(0);
            query.order(type).offset(page * pageSize).limit(pageSize);

            JsonNode node;
            for (Iterator<Recommendation> it = query.iterator(); it.hasNext(); ) {
                if (type.equals("hotvs") || type.equals("hotcity"))
                    node = new RecommendationFormatter().format(it.next());
                else
                    node = it.next().toJson();
                results.add(node);
            }
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }

    /**
     * 获取文章详情
     *
     * @param id
     * @return
     */
    public static Result getArticleById(String id) {
        JsonNode result;

        Datastore ds;
        try {
            ObjectId oid = new ObjectId(id);
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            Query<Article> query = ds.createQuery(Article.class);

            query.field("enabled").equal(Boolean.TRUE).field("_id").equal(oid);
            Article article = query.get();

            if (article == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("INVALID ARTICLE ID: %s", id));
            result = new ArticleFormatter().format(article);
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, result);
    }

    /**
     * 获取文章列表
     *
     * @param
     * @return
     */
    public static Result getArticles(String removeId, int page, int pageSize) {
        List<JsonNode> result = new ArrayList();

        Datastore ds;
        try {
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            Query<Article> query = ds.createQuery(Article.class);
            query.field("enabled").equal(Boolean.TRUE);
            query.order("-publishTime").offset(page * pageSize).limit(pageSize);
            query.retrievedFields(false, "content");
            JsonNode node;
            Article article;
            for (Iterator<Article> it = query.iterator(); it.hasNext(); ) {
                article = it.next();
                // 除ID为removeId之外的其他文章列表
                if (article.getId().equals(removeId))
                    continue;
                node = new ArticleSimpleFormatter().format(article);
                result.add(node);
            }
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
    }

    public static Result checkValidation() {
        JsonNode data = request().body().asJson();

        JsonNode tmp = data.get("tel");
        String tel = null;
        if (tmp != null)
            tel = Utils.telParser(tmp.asText());

        tmp = data.get("actionCode");
        Integer actionCode = null;
        try {
            if (tmp != null)
                actionCode = Integer.parseInt(tmp.asText());
        } catch (IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid arguments.");
        }

        Integer countryCode = 86;
        Integer userId = Integer.valueOf(data.get("userId").asText());
        try {
            tmp = data.get("code");
            if (tmp != null)
                countryCode = Integer.parseInt(tmp.asText());
        } catch (IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid arguments.");
        }

        String v = null;
        tmp = data.get("validationCode");
        if (tmp != null)
            v = tmp.asText();

        if (tel == null || actionCode == null || v == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid arguments.");

        try {
            if (actionCode != 1)
                throw new AizouException(ErrorCode.SMS_INVALID_ACTION, String.format("Invalid SMS action code: %d.", actionCode));

            boolean valid = UserAPI.checkValidation(countryCode, tel, actionCode, v, userId);

            ObjectNode result = Json.newObject();
            result.put("isValid", valid);
            return Utils.createResponse(ErrorCode.NORMAL, result);
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }

}
