package aizou.core;

import exception.TravelPiException;
import models.MorphiaFactory;
import models.guide.AbstractGuide;
import models.guide.Guide;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

/**
 * Created by topy on 2014/11/5.
 */
public class GuideAPI {

    /**
     * 根据ID取得攻略
     *
     * @param id
     * @return
     * @throws TravelPiException
     */
    public static Guide getGuideById(ObjectId id, List<String> fieldList) throws TravelPiException {
        Query<Guide> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE)
                .createQuery(Guide.class);
        query.field("_id").equal(id);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));

        return query.get();
    }

    /**
     * 根据ID删除攻略
     *
     * @param id
     * @throws TravelPiException
     */
    public static void deleteGuideById(ObjectId id) throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class);
        query.field("_id").equal(id);
        ds.delete(query);
    }

    /**
     * 根据用户ID取得攻略列表
     *
     * @param uid
     * @return
     * @throws TravelPiException
     */
    public static List<Guide> getGuideByUser(Integer uid, List<String> fieldList, int page, int pageSize) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class);
        query.field(Guide.fnUserId).equal(uid);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }

    /**
     * 更新行程单
     *
     * @param guideId
     * @param guide
     * @throws TravelPiException
     */
    public static void updateGuide(ObjectId guideId, Guide guide, String guidePart) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class).field("id").equal(guideId);
        UpdateOperations<Guide> update = ds.createUpdateOperations(Guide.class);
        if (guidePart.equals(AbstractGuide.fnItinerary))
            update.set(guidePart, guide.itinerary);
        else if (guidePart.equals(AbstractGuide.fnShopping))
            update.set(guidePart, guide.shopping);
        else if (guidePart.equals(AbstractGuide.fnDinning))
            update.set(guidePart, guide.dinning);
        ds.update(query, update);
    }

    /**
     * 通过id返回Guide详情
     *
     * @param id
     * @param list
     * @return
     * @throws TravelPiException
     */
    public static Guide getGuideInfo(ObjectId id, List<String> list) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class).field("_id").equal(id);
        if (list != null && !list.isEmpty()) {
            query.retrievedFields(true, list.toArray(new String[list.size()]));
        }
        return query.get();
    }

    /**
     * 保存攻略标题
     *
     * @param id
     * @param title
     * @throws TravelPiException
     */
    public static void saveGuideTitle(ObjectId id, String title) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> uo = ds.createUpdateOperations(Guide.class);
        uo.set("title", title);
        ds.update(ds.createQuery(Guide.class).field("_id").equal(id), uo);
    }

}
