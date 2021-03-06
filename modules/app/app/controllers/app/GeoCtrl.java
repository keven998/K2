package controllers.app;

import aizou.core.GeoAPI;
import aizou.core.LocalityAPI;
import aizou.core.PoiAPI;
import aizou.core.UserUgcAPI;
import aspectj.Key;
import aspectj.UsingOcsCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.geo.*;
import models.geo.*;
import org.bson.types.ObjectId;
import play.Configuration;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.Utils;
import utils.results.TaoziResBuilder;

import java.text.ParseException;
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
     * @param id 城市ID
     * @return
     */
    //@UsingOcsCache(key = "getLocality({id})", expireTime = 3600)
    public static Result getLocality(@Key(tag = "id") String id) throws AizouException {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        Long userId;
        if (request().hasHeader("UserId"))
            userId = Long.parseLong(request().getHeader("UserId"));
        else
            userId = null;
        Locality locality = GeoAPI.locDetails(id);
        if (locality == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Locality not exist.");
        //是否被收藏
//        MiscAPI.isFavorite(locality, userId);

        LocalityFormatter localityFormatter = FormatterFactory.getInstance(LocalityFormatter.class, imgWidth);
        ObjectNode response = (ObjectNode) localityFormatter.formatNode(locality);
        // 显示图集的数量
//        response.put("imageCnt", MiscAPI.getLocalityAlbumCount(locality.getId()));
        response.put("playGuide", "http://h5.taozilvxing.com/city/items.php?tid=" + id);
//        response.put("diningTitles", contentsTitles(locality.getCuisines()));
        response.put("shoppingTitles", contentsTitles(locality.getCommodities()));
        response.put("isVote", locality.getVotes().contains(userId));
        response.put("voteCnt", locality.getVotes().size());
        response.put("traveled", UserUgcAPI.hasTraveled(userId, locality.getId()));
        return Utils.createResponse(ErrorCode.NORMAL, response);
    }

    /**
     * 获得城市图集列表
     *
     * @param id
     * @param page
     * @param pageSize
     * @return
     */
    @UsingOcsCache(key = "getLocalityAlbums({id},{page},{pageSize})", expireTime = 3600)
    public static Result getLocalityAlbums(@Key(tag = "id") String id,
                                           @Key(tag = "page") int page,
                                           @Key(tag = "pageSize") int pageSize) throws AizouException {
        return MiscCtrl.getAlbums(id, page, pageSize);
    }

    public static Result getLocalityComments(@Key(tag = "type") String commentType,
                                             @Key(tag = "id") String id,
                                             @Key(tag = "page") int page,
                                             @Key(tag = "pageSize") int pageSize) throws AizouException {
        return MiscCtrl.getLocalityComments(commentType, id, page, pageSize);
    }

    /**
     * 获得国内国外目的地
     *
     * @param abroad
     * @param page
     * @param pageSize
     * @return
     */
    @UsingOcsCache(key = "destinations|{abroad}|{page}|{pageSize}", expireTime = 3600)
//    @UsingLocalCache(callback = "getLMD", args = "{abroad}|{page}")
    public static Result exploreDestinations(@Key(tag = "abroad") boolean abroad,
                                             @Key(tag = "groupBy") boolean groupBy,
                                             @Key(tag = "page") int page,
                                             @Key(tag = "pageSize") int pageSize) throws AizouException {
        Http.Request req = request();
        Http.Response rsp = response();
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        Configuration config = Configuration.root();
        Map destnations = (Map) config.getObject("destinations");
        //TODO 禁用了这里的304机制，统一由ModifiedHandler处理
        String lastModify = destnations.get("lastModify").toString();
        //添加缓存用的相应头
        try {
            Utils.addCacheResponseHeader(rsp, lastModify);
        } catch (ParseException e) {
        }
        if (Utils.useCache(req, lastModify))
            return status(304, "Content not modified, dude.");
        return exportDestinationImpl(imgWidth, destnations, abroad, groupBy, page, pageSize);
    }

    public static Result exportDestinationImpl(int imgWidth, Map destnations, boolean abroad, boolean groupBy, int page, int pageSize) throws AizouException {

        // 国外目的地
        if (abroad) {
            String countrysStr = destnations.get("country").toString();
            List<String> countryNames = Arrays.asList(countrysStr.split(Constants.SYMBOL_SLASH));
            List<Country> countryList = GeoAPI.searchCountryByName(countryNames, Constants.ZERO_COUNT,
                    Constants.MAX_COUNT);

            SimpleCountryFormatter formatter = new SimpleCountryFormatter();
            if (imgWidth > 0)
                formatter.setImageWidth(imgWidth);
            JsonNode destResult = formatter.formatNode(countryList);

            for (Iterator<JsonNode> itr = destResult.elements(); itr.hasNext(); ) {
                ObjectNode cNode = (ObjectNode) itr.next();
                JsonNode localities = getDestinationsNodeByCountry(imgWidth, new ObjectId(cNode.get("id").asText()), page, pageSize);
                cNode.put("destinations", localities);
            }

            return new TaoziResBuilder().setBody(destResult).build();
        } else {
            //国内目的地
            Map<String, Object> mapConf = Configuration.root().getConfig("domestic").asMap();
            Map<String, Object> pinyinConf = Configuration.root().getConfig("pinyin").asMap();
            Map<String, Object> provinceConf = Configuration.root().getConfig("province").asMap();
            Map<String, Object> sortConf = Configuration.root().getConfig("sort").asMap();
            String k;
            int sort = 0;
            Object v, pinyinObj, provinceObj, sortObj;
            String zhName = null;
            String pinyin = null;
            String province = null;

            RmdLocality locality;
            RmdProvince rmdProvince;
            Map<String, List<RmdLocality>> rmdProvinceMap = new HashMap<>();
            List<RmdLocality> localityList;
            List<RmdProvince> rmdProvinceList = new ArrayList<>();

            Map<String, Object> provincePinyinConf = Configuration.root().getConfig("provincePinyin").asMap();

            List<ObjectId> oid = new ArrayList<>();
            for (String str : mapConf.keySet())
                oid.add(new ObjectId(str));

            Map<String, Locality> locationMap = LocalityAPI.getLocalityMap(oid, Arrays.asList(Locality.FD_ID, Locality.fnLocation, Locality.fnImages), Constants.ZERO_COUNT,
                    Constants.MAX_COUNT);

            //取出配置文件中的数据,并转换为Entity
            for (Map.Entry<String, Object> entry : mapConf.entrySet()) {
                k = entry.getKey();
                v = entry.getValue();
                if (v != null)
                    zhName = v.toString();
                pinyinObj = pinyinConf.get(k);
                if (pinyinObj != null)
                    pinyin = pinyinObj.toString();
                provinceObj = provinceConf.get(k);
                if (provinceObj != null)
                    province = provinceObj.toString();

                sortObj = sortConf.get(k);
                if (sortObj != null)
                    sort = Integer.valueOf(sortObj.toString());
                locality = new RmdLocality();
                locality.setId(new ObjectId(k));
                locality.setZhName(zhName);
                locality.setEnName(pinyin);
                Locality localityLocation = locationMap.get(locality.getId().toString());
                if (localityLocation != null) {
                    locality.setLocation(localityLocation.getLocation());
                    locality.setImages(localityLocation.getImages());
                }
                locality.setPinyin(pinyin);
                locality.setProvince(province);
                locality.setSort(sort);

                localityList = rmdProvinceMap.get(province);
                if (localityList == null)
                    localityList = new ArrayList<>();
                localityList.add(locality);
                rmdProvinceMap.put(province, localityList);
            }
            String proZhName;
            String proPinyin = null;
            // 把目的地按照省份分组
            for (Map.Entry<String, List<RmdLocality>> entry : rmdProvinceMap.entrySet()) {
                rmdProvince = new RmdProvince();
                proZhName = entry.getKey().toString();
                if (provincePinyinConf.get(proZhName) != null)
                    proPinyin = provincePinyinConf.get(proZhName).toString();
                rmdProvince.setPinyin(proPinyin);
                rmdProvince.setId(new ObjectId());
                rmdProvince.setZhName(entry.getKey());
                rmdProvince.setEnName("");
                rmdProvince.setDestinations(entry.getValue());
                rmdProvinceList.add(rmdProvince);
            }
            // 排序
            sortByPinyin(rmdProvinceList);
            sortLocalityByPinyin(rmdProvinceList);
            RmdProvinceFormatter formatter = new RmdProvinceFormatter(imgWidth);

            JsonNode jsonNode = formatter.formatNode(rmdProvinceList);
            return new TaoziResBuilder().setBody(jsonNode).build();
        }
    }

    public static Result getCountyById(String id) throws AizouException {
        Country country = GeoAPI.countryDetails(id);
        if (country == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Country not exist.");

        ObjectNode response = new ObjectMapper().createObjectNode();
        response.put("zhName", country.getZhName());
        response.put("commoditiesCnt", 0);
        return Utils.createResponse(ErrorCode.NORMAL, response);
    }

    /**
     * 推荐目的地的省份按拼音排序
     *
     * @param rmdProvinceList
     */
    public static void sortByPinyin(List<RmdProvince> rmdProvinceList) {

        Collections.sort(rmdProvinceList, new Comparator<RmdProvince>() {
            public int compare(RmdProvince arg0, RmdProvince arg1) {
                return arg0.getPinyin().compareTo(arg1.getPinyin());
            }
        });
    }

    /**
     * 推荐目的地按拼音排序
     *
     * @param rmdProvinceList
     */
    public static void sortLocalityByPinyin(List<RmdProvince> rmdProvinceList) {
        for (RmdProvince rmdProvince : rmdProvinceList) {
            List<RmdLocality> destinations = rmdProvince.getDestinations();
            Collections.sort(destinations, new Comparator<RmdLocality>() {
                public int compare(RmdLocality arg0, RmdLocality arg1) {
                    if (arg0.getSort() <= 0 || arg1.getSort() <= 0)
                        return 0;
                    return arg0.getSort() - arg1.getSort() > 0 ? 1 : -1;
                }
            });
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
    private static JsonNode getDestinationsNodeByCountry(int imgWidth, ObjectId id, int page, int pageSize)
            throws AizouException {
        List<Locality> localities = GeoAPI.getDestinationsByCountry(id, page, pageSize);

        SimpleLocalityWithLocationFormatter formatter = FormatterFactory.getInstance(SimpleLocalityWithLocationFormatter.class, imgWidth);
        return formatter.formatNode(localities);
    }

    /**
     * 游玩攻略-H5
     *
     * @param locId
     * @param field
     * @return
     */
    public static Result getTravelGuide(String locId, String field) throws AizouException {
        if (field == null || field.isEmpty())
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);

        List<String> fieldList = new ArrayList<>();
        switch (field) {
            case "remoteTraffic":
                fieldList.add(Locality.fnRemoteTraffic);
                break;
            case "localTraffic":
                fieldList.add(Locality.fnLocalTraffic);
                break;
            case "activities":
                Collections.addAll(fieldList, Locality.fnActivityIntro, Locality.fnActivities);
                break;
            case "tips":
                fieldList.add(Locality.fnTips);
                break;
            case "specials":
                fieldList.add(Locality.fnSpecials);
                break;
            case "geoHistory":
                fieldList.add(Locality.fnGeoHistory);
                break;
            case "dining":
                Collections.addAll(fieldList, Locality.fnDinningIntro, Locality.fnCuisines);
                break;
            case "shopping":
                Collections.addAll(fieldList, Locality.fnShoppingIntro, Locality.fnCommodities);
                break;
            case "desc":
                fieldList.add(Locality.fnDesc);
                break;
            case "diningTitles":
                Collections.addAll(fieldList, Locality.fnCuisines);
                break;
            case "shoppingTitles":
                Collections.addAll(fieldList, Locality.fnCommodities);
                break;
            default:
                throw new AizouException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
        Locality locality = PoiAPI.getLocalityByField(new ObjectId(locId), fieldList);
        if (locality == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Locality is not exist.ID:" + locId);
        ObjectNode result = Json.newObject();

        DetailsEntryFormatter detailsEntryFormatter = FormatterFactory.getInstance(DetailsEntryFormatter.class, imgWidth);
        switch (field) {
            case "remoteTraffic":
                result.put("desc", "");
                result.put("contents", detailsEntryFormatter.formatNode(locality.getRemoteTraffic()));
                break;
            case "localTraffic":
                result.put("desc", "");
                result.put("contents", detailsEntryFormatter.formatNode(locality.getLocalTraffic()));
                break;
            case "activities":
                result.put("desc", locality.getActivityIntro());
                result.put("contents", detailsEntryFormatter.formatNode(locality.getActivities()));
                break;
            case "tips":
                result.put("desc", "");
                result.put("contents", detailsEntryFormatter.formatNode(locality.getTips()));
                break;
            case "geoHistory":
                result.put("desc", "");
                result.put("contents", detailsEntryFormatter.formatNode(locality.getGeoHistory()));
                break;
            case "specials":
                result.put("desc", "");
                result.put("contents", detailsEntryFormatter.formatNode(locality.getSpecials()));
                break;
            case "desc":
                result.put("desc", locality.getDesc());
                result.put("contents", Json.toJson(new ArrayList<>()));
                break;
            case "dining":
                result.put("desc", locality.getDiningIntro());
                result.put("contents", detailsEntryFormatter.formatNode(locality.getCuisines()));
                break;
            case "shopping":
                result.put("desc", locality.getShoppingIntro());
                result.put("contents", detailsEntryFormatter.formatNode(locality.getCommodities()));
                break;
            case "diningTitles":
                result.put("contentsTitles", contentsTitles(locality.getCuisines()));
                break;
            case "shoppingTitles":
                result.put("contentsTitles", contentsTitles(locality.getCommodities()));
                break;
        }
        return Utils.createResponse(ErrorCode.NORMAL, result);
    }

    public static String contentsTitles(List<DetailsEntry> entries) {
        if (entries == null)
            return "";

        StringBuilder sb = new StringBuilder();
        for (DetailsEntry entry : entries) {
            sb.append(entry.getTitle());
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * 游玩攻略概览-H5
     *
     * @param locId
     * @return
     */
    public static Result getTravelGuideOutLine(String locId) throws AizouException {
        Locality loc = null;
        loc = GeoAPI.locDetails(new ObjectId(locId), Arrays.asList("zhName"));

        ObjectNode node;
        List<ObjectNode> result = new ArrayList<>();
        node = Json.newObject();
        node.put("title", "到达" + (loc == null ? "" : loc.getZhName()));
        node.put("fields", Json.toJson(Arrays.asList("remoteTraffic")));
        result.add(node);

        node = Json.newObject();
        node.put("title", "当地交通");
        node.put("fields", Json.toJson(Arrays.asList("localTraffic")));
        result.add(node);

        node = Json.newObject();
        node.put("title", "不可错过的游玩体验");
        node.put("fields", Json.toJson(Arrays.asList("specials")));
        result.add(node);

        node = Json.newObject();
        node.put("title", "旅行小贴士");
        node.put("fields", Json.toJson(Arrays.asList("tips")));
        result.add(node);

        node = Json.newObject();
        node.put("title", "节庆与民俗活动");
        node.put("fields", Json.toJson(Arrays.asList("activities")));
        result.add(node);

        node = Json.newObject();
        node.put("title", "宗教、文化与历史");
        node.put("fields", Json.toJson(Arrays.asList("geoHistory")));
        result.add(node);
//        node = Json.newObject();
//        node.put("title", "描述");
//        node.put("fields", Json.toJson(Arrays.asList("desc")));
//        result.add(node);

//        node = Json.newObject();
//        node.put("title", "地道美食");
//        node.put("fields", Json.toJson(Arrays.asList("dining")));
//        result.add(node);
//
//        node = Json.newObject();
//        node.put("title", "购物指南");
//        node.put("fields", Json.toJson(Arrays.asList("shopping")));
//        result.add(node);

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));

    }

}
