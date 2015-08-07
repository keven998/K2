package formatter.taozi.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.geo.LocalitySerializer;
import models.geo.Locality;
import models.misc.ImageItem;
import models.user.Favorite;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import utils.Constants;

import java.io.IOException;
import java.util.List;

/**
 * 返回用户的收藏信息（即：查看自己的收藏信息时使用）
 * <p>
 * Created by topy on 10/28/14.
 */
public class FavoriteFormatter extends AizouFormatter<Favorite> {

    public FavoriteFormatter(Integer imgWidth) {
        registerSerializer(ImageItem.class, new ImageItemSerializer(imgWidth));
        registerSerializer(Locality.class, new LocalitySerializer());
        registerSerializer(Favorite.class, new FavoriteSerializer());
        initObjectMapper(null);
//        filteredFields = new HashSet<>();
//        Collections.addAll(filteredFields,
//                Favorite.fnZhName,
//                Favorite.fnEnName,
//                Favorite.fnItemId,
//                Favorite.fnImage,
//                Favorite.fnType,
//                Favorite.fnUserId,
//                Favorite.fnCreateTime,
//                Favorite.fnId,
//                Favorite.fnDesc,
//                Favorite.fnLocality
//        );
//
//        if (type.equals("vs") || type.equals("locality")) {
//            Collections.addAll(filteredFields, Favorite.fnTimeCostDesc);
//        } else if (type.equals("restaurant") || type.equals("hotel")) {
//            Collections.addAll(filteredFields, Favorite.fnTimeCostDesc, Favorite.fnRating,
//                    Favorite.fnAddress, Favorite.fnPriceDesc, Favorite.fnTelephone);
//        } else if (type.equals("shopping")) {
//            Collections.addAll(filteredFields, Favorite.fnRating);
//        }

    }

    class FavoriteSerializer extends AizouSerializer<Favorite> {
        @Override
        public void serialize(Favorite favorite, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();

            writeObjectId(favorite, jgen, serializerProvider);

            ObjectId itemId = favorite.itemId;
            if (itemId != null) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ObjectId.class, null);
                jgen.writeFieldName(Favorite.fnItemId);
                ret.serialize(itemId, jgen, serializerProvider);
            } else {
                jgen.writeStringField(Favorite.fnItemId, "");
            }

            jgen.writeStringField(Favorite.fnZhName, getString(favorite.zhName));
            jgen.writeStringField(Favorite.fnEnName, getString(favorite.enName));
            jgen.writeStringField(Favorite.fnType, getString(favorite.type));
            jgen.writeStringField(Favorite.fnDesc, getString(StringUtils.abbreviate(favorite.desc, Constants.ABBREVIATE_LEN)));
            jgen.writeNumberField(Favorite.fnCreateTime, getValue(favorite.createTime.getTime()));
            jgen.writeNumberField(Favorite.fnUserId, getValue(favorite.userId));

            // Images
            jgen.writeFieldName("images");
            List<ImageItem> images = favorite.images;
            jgen.writeStartArray();
            if (images != null && !images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jgen, serializerProvider);
            }
            jgen.writeEndArray();

            // Locality
            jgen.writeFieldName(Favorite.fnLocality);
            Locality locality = favorite.locality;
            if (locality != null) {
                JsonSerializer<Object> retLocality = serializerProvider.findValueSerializer(Locality.class, null);
                retLocality.serialize(locality, jgen, serializerProvider);
            } else {
                jgen.writeStartObject();
                jgen.writeEndObject();
            }

            String type = favorite.type;
            if (type.equals("vs") || type.equals("locality")) {
                jgen.writeStringField(Favorite.fnTimeCostDesc, getString(favorite.timeCostDesc));
            } else if (type.equals("restaurant") || type.equals("hotel")) {
                jgen.writeStringField(Favorite.fnTimeCostDesc, getString(favorite.timeCostDesc));
                jgen.writeStringField(Favorite.fnAddress, getString(favorite.address));
                jgen.writeStringField(Favorite.fnPriceDesc, getString(favorite.priceDesc));
                jgen.writeStringField(Favorite.fnTelephone, getString(favorite.telephone));
                jgen.writeNumberField(Favorite.fnRating, getValue(favorite.rating));
            } else if (type.equals("shopping")) {
                jgen.writeNumberField(Favorite.fnRating, getValue(favorite.rating));
            } else if (type.equals("travelNote"))
                jgen.writeStringField("detailUrl", "http://api.lvxingpai.com/app/travelnotesScala/" + getString(favorite.itemId.toString()));
//            jgen.writeStringField("detailUrl", "http://h5.taozilvxing.com/dayDetail.php?id=" + getString(favorite.itemId.toString()));

            jgen.writeEndObject();
        }
    }

}

