package controllers.taozi;

import aizou.core.MiscAPI;
import aizou.core.PoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.AizouException;
import exception.ErrorCode;
import formatter.taozi.geo.LocalityGuideFormatter;
import formatter.taozi.misc.CommentFormatter;
import formatter.taozi.poi.DetailedPOIFormatter;
import formatter.taozi.poi.POIRmdFormatter;
import formatter.taozi.poi.SimplePOIFormatter;
import models.geo.Locality;
import models.poi.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.DataFilter;
import utils.TaoziDataFilter;
import utils.Utils;

import java.util.*;

/**
 * Created by topy on 2014/11/1.
 */
public class POICtrl extends Controller {

    public static JsonNode viewPOIInfoImpl(Class<? extends AbstractPOI> poiClass, String spotId,
                                           int commentPage, int commentPageSize, Long userId,
                                           int rmdPage, int rmdPageSize, int imgWidth) throws AizouException {
        DetailedPOIFormatter<? extends AbstractPOI> poiFormatter = new DetailedPOIFormatter<>(poiClass).setImageWidth(imgWidth);
        AbstractPOI poiInfo = PoiAPI.getPOIInfo(new ObjectId(spotId), poiClass, poiFormatter.getFilteredFields());
        if (poiInfo == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI ID: %s.", spotId));


        //是否被收藏
        MiscAPI.isFavorite(poiInfo, userId);
        JsonNode info = poiFormatter.format(poiInfo);

        //取得推荐
        List<POIRmd> rmdEntities = PoiAPI.getPOIRmd(spotId, rmdPage, rmdPageSize);
        List<JsonNode> recommends = new ArrayList<>();
        for (POIRmd temp : rmdEntities) {
            recommends.add(new POIRmdFormatter().format(temp));
        }

        // 取得评论
        List<Comment> commentsEntities = PoiAPI.getPOIComment(spotId, commentPage, commentPageSize);
        List<JsonNode> comments = new ArrayList<>();
        for (Comment temp : commentsEntities) {
            comments.add(new CommentFormatter().format(temp));
        }
        ObjectNode ret = (ObjectNode) info;
        ret.put("recommends", Json.toJson(recommends));

        ret.put("comments", Json.toJson(comments));
        // 添加H5接口
        if (poiClass == Shopping.class || poiClass == Restaurant.class)
            ret.put("moreCommentsUrl", "http://h5.taozilvxing.com/morecomment.php?pid=" + spotId);
        return ret;
    }

    /**
     * 获得POI的详细信息。
     *
     * @param poiDesc POI的类型说明:
     *                vs: 景点
     *                hotel: 酒店
     *                restaurant: 餐饮
     *                shopping:购物
     *                entertainment:美食
     * @param spotId  POI的ID。
     */
    public static Result viewPOIInfo(String poiDesc, String spotId, int commentPage, int commentPageSize,
                                     int rmdPage, int rmdPageSize) {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);

        Class<? extends AbstractPOI> poiClass;
        switch (poiDesc) {
            case "vs":
                poiClass = ViewSpot.class;
                break;
            case "hotel":
                poiClass = Hotel.class;
                break;
            case "restaurant":
                poiClass = Restaurant.class;
                break;
            case "shopping":
                poiClass = Shopping.class;
                break;
            default:
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc));
        }
        try {
            Long userId;
            if (request().hasHeader("UserId"))
                userId = Long.parseLong(request().getHeader("UserId"));
            else
                userId = null;
            JsonNode ret = viewPOIInfoImpl(poiClass, spotId, commentPage, commentPageSize, userId, rmdPage, rmdPageSize, imgWidth);
            return Utils.createResponse(ErrorCode.NORMAL, ret);
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }

    /**
     * 获得景点周边的周边POI按照
     *
     * @param gainType
     * @param lat
     * @param lgn
     * @param page
     * @param pageSize
     * @return
     *//*
    public static Result getPOINearBy(String gainType, String lat, String lgn, int page, int pageSize) {

        try {
            Double latD = Double.valueOf(lat);
            Double lngD = Double.valueOf(lat);
            List<String> fieldsLimit = Arrays.asList(AbstractPOI.simpID, AbstractPOI.FD_NAME, AbstractPOI.FD_DESC, AbstractPOI.FD_IMAGES);
            List<AbstractPOI> poiInfos = (List<AbstractPOI>) PoiAPI.getPOINearBy(gainType, latD, lngD, fieldsLimit, page, pageSize);

            List<JsonNode> nodeList = new ArrayList();
            for (AbstractPOI temp : poiInfos) {
                nodeList.add(new SimplePOIFormatter().format(temp));
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(nodeList));
        } catch (NumberFormatException | TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }*/

    /**
     * 根据关键词搜索POI信息
     *
     * @param poiType
     * @param tag
     * @param keyword
     * @param page
     * @param pageSize
     * @param sortField
     * @param sortType
     * @param hotelTypeStr
     * @return
     */
    public static Result poiSearch(String poiType, String tag, String keyword, int page, int pageSize, String sortField, String sortType, String hotelTypeStr) {
        //酒店的类型
        int hotelType = 0;
        if (!hotelTypeStr.equals("")) {
            try {
                hotelType = Integer.parseInt(hotelTypeStr);
            } catch (ClassCastException e) {
                hotelType = 0;
            }

        }
        //判断搜索的关键词是否为空
        if (keyword.equals("")) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "key word can not be null");
        }
        PoiAPI.POIType type = null;
        switch (poiType) {
            case "vs":
                type = PoiAPI.POIType.VIEW_SPOT;
                break;
            case "hotel":
                type = PoiAPI.POIType.HOTEL;
                break;
            case "restaurant":
                type = PoiAPI.POIType.RESTAURANT;
                break;
            case "shopping":
                type = PoiAPI.POIType.SHOPPING;
                break;
        }
        if (type == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiType));

        //处理排序
        boolean sort = false;
        if (sortType != null && sortType.equals("asc"))
            sort = true;

        PoiAPI.SortField sf;
        switch (sortField) {
            case "price":
                sf = PoiAPI.SortField.PRICE;
                break;
            case "score":
                sf = PoiAPI.SortField.SCORE;
                break;
            default:
                sf = null;
        }

        List<JsonNode> results = new ArrayList<>();
        Iterator<? extends AbstractPOI> it = null;
        try {
            it = PoiAPI.poiSearch(type, tag, keyword, sf, sort, page, pageSize, true, hotelType);
            while (it.hasNext())
                results.add(new SimplePOIFormatter().format(it.next()));
            return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(Json.toJson(results), request(), Constants.BIG_PIC));
        } catch (AizouException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }

    /**
     * 查看特定地区的poi
     *
     * @param poiType
     * @param locId
     * @param tagFilter
     * @param sortField
     * @param page
     * @param pageSize
     * @return
     */
    public static Result viewPoiList(String poiType, String locId, String tagFilter, String sortField, String sortType, int page, int pageSize, int commentPage, int commentPageSize) {
        PoiAPI.POIType type = null;
        switch (poiType) {
            case "vs":
                type = PoiAPI.POIType.VIEW_SPOT;
                break;
            case "hotel":
                type = PoiAPI.POIType.HOTEL;
                break;
            case "restaurant":
                type = PoiAPI.POIType.RESTAURANT;
                break;
            case "shopping":
                type = PoiAPI.POIType.SHOPPING;
                break;
        }
        if (type == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiType));
        //处理排序
        boolean sort = false;
        if (sortType != null && sortType.equals("asc"))
            sort = true;
        PoiAPI.SortField sf;
        switch (sortField) {
            case "rating":
                sf = PoiAPI.SortField.RATING;
                break;
            case "hotness":
                sf = PoiAPI.SortField.HOTNESS;
                break;
            default:
                sf = null;
        }

        List<JsonNode> results = new ArrayList<>();
        List<? extends AbstractPOI> it;
        List<Comment> commentsEntities;
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        try {
            it = PoiAPI.viewPoiList(type, new ObjectId(locId), sf, sort, page, pageSize);
            for (AbstractPOI temp : it) {
                temp.images = TaoziDataFilter.getOneImage(temp.images);
                temp.priceDesc = TaoziDataFilter.getPriceDesc(temp);
                temp.desc = StringUtils.abbreviate(temp.desc, Constants.ABBREVIATE_LEN);
                ObjectNode ret = (ObjectNode) new SimplePOIFormatter().setImageWidth(imgWidth).format(temp);
                if (poiType.equals("restaurant") || poiType.equals("shopping") ||
                        poiType.equals("hotel")) {
                    commentsEntities = PoiAPI.getPOIComment(temp.getId().toString(), commentPage, commentPageSize);
                    int commCnt = (int) PoiAPI.getPOICommentCount(temp.getId().toString());
                    List<JsonNode> comments = new ArrayList<>();
                    for (Comment cmt : commentsEntities) {
                        comments.add(new CommentFormatter().format(cmt));
                    }
                    ret.put("comments", Json.toJson(comments));
                    ret.put("commentCnt", commCnt);
                }
                results.add(ret);
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
        } catch (AizouException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }

    /**
     * 发现特定景点周边的poi
     *
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getPoiNear(double lng, double lat, double maxDist, boolean spot, boolean hotel,
                                    boolean restaurant, boolean shopping, int page, int pageSize, int commentPage, int commentPageSize) {
        try {
            // 获取图片宽度
            String imgWidthStr = request().getQueryString("imgWidth");
            int imgWidth = 0;
            if (imgWidthStr != null)
                imgWidth = Integer.valueOf(imgWidthStr);
            ObjectNode results = getPoiNearImpl(lng, lat, maxDist, spot, hotel, restaurant, shopping, page, pageSize, commentPage, commentPageSize, imgWidth);
            return Utils.createResponse(ErrorCode.NORMAL, results);
        } catch (AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    private static ObjectNode getPoiNearImpl(double lng, double lat, double maxDist, boolean spot, boolean hotel,
                                             boolean restaurant, boolean shopping, int page, int pageSize, int commentPage, int commentPageSize, int imgWidth) throws AizouException {
        ObjectNode results = Json.newObject();

        //发现poi
        List<PoiAPI.POIType> poiKeyList = new ArrayList<>();
        HashMap<PoiAPI.POIType, String> poiMap = new HashMap<>();
        if (spot) {
            poiKeyList.add(PoiAPI.POIType.VIEW_SPOT);
            poiMap.put(PoiAPI.POIType.VIEW_SPOT, "vs");
        }

        if (hotel) {
            poiKeyList.add(PoiAPI.POIType.HOTEL);
            poiMap.put(PoiAPI.POIType.HOTEL, "hotel");
        }

        if (restaurant) {
            poiKeyList.add(PoiAPI.POIType.RESTAURANT);
            poiMap.put(PoiAPI.POIType.RESTAURANT, "restaurant");
        }

        if (shopping) {
            poiKeyList.add(PoiAPI.POIType.SHOPPING);
            poiMap.put(PoiAPI.POIType.SHOPPING, "shopping");
        }

        for (PoiAPI.POIType poiType : poiKeyList) {
            List<JsonNode> retPoiList = new ArrayList<>();
            Iterator<? extends AbstractPOI> iterator = PoiAPI.getPOINearBy(poiType, lng, lat, maxDist,
                    page, pageSize);
            ObjectNode ret;
            AbstractPOI poi;
            List<Comment> commentsEntities;
            if (iterator != null) {
                for (; iterator.hasNext(); ) {
                    poi = iterator.next();
                    ret = (ObjectNode) new SimplePOIFormatter().setImageWidth(imgWidth).format(poi);
                    if (poiType.equals(PoiAPI.POIType.RESTAURANT) || poiType.equals(PoiAPI.POIType.SHOPPING) ||
                            poiType.equals(PoiAPI.POIType.HOTEL)) {
                        commentsEntities = PoiAPI.getPOIComment(poi.getId().toString(), commentPage, commentPageSize);
                        int commCnt = (int) PoiAPI.getPOICommentCount(poi.getId().toString());
                        List<JsonNode> comments = new ArrayList<>();
                        for (Comment cmt : commentsEntities) {
                            comments.add(new CommentFormatter().format(cmt));
                        }
                        ret.put("comments", Json.toJson(comments));
                        ret.put("commentCnt", commCnt);
                    }
                    retPoiList.add(ret);
                }
                results.put(poiMap.get(poiType), Json.toJson(retPoiList));
            }

        }
        return results;
    }

    /**
     * 获取乘车指南/景点简介
     *
     * @param id
     * @param desc
     * @param traffic
     * @return
     */
    public static Result getLocDetail(String id, boolean desc, boolean traffic) {
        try {
            ObjectNode results = Json.newObject();
            ObjectId oid = new ObjectId(id);
            ViewSpot viewSpot = PoiAPI.getVsDetail(oid, Arrays.asList(ViewSpot.detDesc));
            if (desc) {
                results.put("desc", viewSpot.description.desc);
            }

            if (traffic) {
                results.put("traffic", viewSpot.description.traffic);
            }
            return Utils.createResponse(ErrorCode.NORMAL, results);
        } catch (AizouException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    /**
     * 特定地点美食、购物发现
     *
     * @param id
     * @param dinning
     * @param shopping
     * @param page
     * @param pageSize
     * @return
     */
//    public static Result getDinShop(String id, boolean dinning, boolean shopping,
//                                    int page, int pageSize) {
//        //TODO 缺少店铺推荐数据
//        try {
//            ObjectNode results = Json.newObject();
//            List<PoiAPI.DestinationType> destKeyList = new ArrayList<>();
//            HashMap<PoiAPI.DestinationType, String> poiMap = new HashMap<>();
//            if (dinning) {
//                destKeyList.add(PoiAPI.DestinationType.DINNING);
//                poiMap.put(PoiAPI.DestinationType.DINNING, "dinning");
//            }
//
//            if (shopping) {
//                destKeyList.add(PoiAPI.DestinationType.SHOPPING);
//                poiMap.put(PoiAPI.DestinationType.SHOPPING, "shopping");
//            }
//            ObjectId oid = new ObjectId(id);
//            for (PoiAPI.DestinationType type : destKeyList) {
//
//                Locality locality = PoiAPI.getTravelGuideApi(oid, type, page, pageSize);
//                String kind = poiMap.get(type);
//                //results.put(kind, new DestinationGuideFormatter().format(destination,kind));
//                results.put(kind, new LocalityGuideFormatter().format(locality));
//            }
//            return Utils.createResponse(ErrorCode.NORMAL, results);
//        } catch (AizouException | NullPointerException e) {
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
//        }
//    }

    /**
     * 游玩攻略
     *
     * @param locId
     * @param field
     * @return
     */
    public static Result getTravelGuide(String locId, String field, String poiDesc) {
        try {
            List<String> destKeyList = new ArrayList<>();

            Class<? extends AbstractPOI> poiClass;
            switch (poiDesc) {
                case "vs":
                    poiClass = ViewSpot.class;
                    break;
                case "hotel":
                    poiClass = Hotel.class;
                    break;
                case "restaurant":
                    poiClass = Restaurant.class;
                    break;
                case "shopping":
                    poiClass = Shopping.class;
                    break;
                default:
                    return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", poiDesc));
            }

//            for (String f : fields.split("\\s*,\\s*")) {
//            }
            switch (field) {
                case "tips":
                    destKeyList.add(AbstractPOI.FD_TIPS);
                    break;
                case "trafficInfo":
                    destKeyList.add(AbstractPOI.FD_TRAFFICINFO);
                    break;
                case "visitGuide":
                    destKeyList.add(AbstractPOI.FD_VISITGUIDE);
                    break;
            }

            AbstractPOI poiInfo = PoiAPI.getPOIInfo(new ObjectId(locId), poiClass, destKeyList);
            ObjectNode result = Json.newObject();
            if (field.equals("tips")) {
                result.put("desc", "");
                result.put("contents", Json.toJson(GeoCtrl.contentsToList(poiInfo.getTips())));
            } else if (field.equals("trafficInfo")) {
                result.put("contents", Json.toJson(poiInfo.getTrafficInfo()));
            } else if (field.equals("visitGuide")) {
                result.put("contents", Json.toJson(poiInfo.getVisitGuide()));
            }

            return Utils.createResponse(ErrorCode.NORMAL, result);
        } catch (AizouException | NullPointerException | NumberFormatException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }
}
