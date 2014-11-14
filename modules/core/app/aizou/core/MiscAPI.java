package aizou.core;

import exception.TravelPiException;
import models.MorphiaFactory;
import models.misc.TravelColumns;
import models.poi.Comment;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;
import java.util.Queue;

/**
 * Created by lxf on 14-11-12.
 */
public class MiscAPI {
    /**
     * 取得旅行专栏图片的url以及跳转链接的url
     *
     * @return
     * @throws TravelPiException
     */
    public static TravelColumns getColumns() throws TravelPiException {
        return MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC).
                createQuery(TravelColumns.class).get();
    }

    /**
     * 储存评论信息
     *
     * @param comment
     * @throws TravelPiException
     */
    public static void saveComment(Comment comment) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        ds.save(comment);
    }

    /**
     * 更新对景点的评论
     *
     * @param poiId
     * @param commentDetails
     * @throws TravelPiException
     */
    public static void updateComment(String poiId, String commentDetails) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        UpdateOperations<Comment> uo = ds.createUpdateOperations(Comment.class);
        uo.set("commentDetails", commentDetails);
        ds.update(ds.createQuery(Comment.class).field("poiId").equal(poiId), uo);
    }

    /**
     * 通过poiId取得评论
     *
     * @param poiId
     * @param page
     * @param pageSize
     * @return
     * @throws TravelPiException
     */
    public static List<Comment> displayCommentApi(String poiId, Boolean goodComment,
                                                  Boolean midComment, Boolean badComment, int page, int pageSize)
            throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Comment> query = ds.createQuery(Comment.class).field("poiId").equal(poiId);
        query = query.order("-commentTime");
        if (goodComment) {
            query = query.filter("score >=", 0.7).filter("score <", 1.0);
            return query.offset(page * pageSize).limit(pageSize).asList();
        }
        if (midComment) {
            query = query.filter("score >=", 0.3).filter("score <", 0.7);
            return query.offset(page * pageSize).limit(pageSize).asList();
        }
        if (midComment) {
            query = query.filter("score <", 0.3);
            return query.offset(page * pageSize).limit(pageSize).asList();
        }
        return query.offset(page * pageSize).limit(page).asList();
    }


}
