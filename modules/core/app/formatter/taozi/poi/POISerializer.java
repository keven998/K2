package formatter.taozi.poi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.AizouBaseEntity;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.*;
import org.bson.types.ObjectId;
import utils.TaoziDataFilter;

import java.io.IOException;
import java.util.List;

/**
 * Created by zephyre on 12/6/14.
 */
public class POISerializer extends AizouSerializer<AbstractPOI> {

    private Level level;

    public POISerializer() {
        this(Level.SIMPLE);
    }

    public POISerializer(Level level) {
        this.level = level;
    }

    public enum Level {
        SIMPLE,
        DETAILED
    }

    @Override
    public void serialize(AbstractPOI abstractPOI, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();

        writeObjectId(abstractPOI, jsonGenerator, serializerProvider);
        jsonGenerator.writeStringField(AbstractPOI.FD_ZH_NAME, getString(abstractPOI.zhName));
        jsonGenerator.writeStringField(AbstractPOI.FD_EN_NAME, getString(abstractPOI.enName));
        jsonGenerator.writeNumberField(AbstractPOI.FD_RATING, getValue(abstractPOI.rating));
        jsonGenerator.writeStringField(AbstractPOI.FD_ADDRESS, getString(abstractPOI.address));

        jsonGenerator.writeFieldName("images");
        List<ImageItem> images = abstractPOI.getImages();
        jsonGenerator.writeStartArray();
        if (images != null && !images.isEmpty()) {
            JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
            for (ImageItem image : images)
                ret.serialize(image, jsonGenerator, serializerProvider);
        }
        jsonGenerator.writeEndArray();

        // Diff POI
        if (abstractPOI instanceof ViewSpot) {
            // Type use for serialize
            jsonGenerator.writeStringField("type", "vs");
            // TimeCost
            jsonGenerator.writeStringField(AbstractPOI.FD_TIMECOSTDESC, getString(abstractPOI.timeCostDesc));
            // PriceDesc
            jsonGenerator.writeStringField(AbstractPOI.FD_PRICE_DESC, getString(abstractPOI.priceDesc));
        } else if (abstractPOI instanceof Restaurant) {
            // Type use for serialize
            jsonGenerator.writeStringField("type", "restaurant");

            // PriceDesc
            jsonGenerator.writeStringField(AbstractPOI.FD_PRICE_DESC, getString(abstractPOI.priceDesc));
            // Tel
            List<String> tels = abstractPOI.tel;
            jsonGenerator.writeFieldName(AbstractPOI.FD_TELEPHONE);
            jsonGenerator.writeStartArray();
            if (tels != null && (!tels.isEmpty())) {
                for (String tel : tels)
                    jsonGenerator.writeString(getString(tel));
            }
            jsonGenerator.writeEndArray();
        } else if (abstractPOI instanceof Shopping) {
            // Type use for serialize
            jsonGenerator.writeStringField("type", "shopping");
            // Tel
            List<String> tels = abstractPOI.tel;
            jsonGenerator.writeFieldName(AbstractPOI.FD_TELEPHONE);
            jsonGenerator.writeStartArray();
            if (tels != null && (!tels.isEmpty())) {
                for (String tel : tels)
                    jsonGenerator.writeString(getString(tel));
            }
            jsonGenerator.writeEndArray();

        } else if (abstractPOI instanceof Hotel) {
            // Type use for serialize
            jsonGenerator.writeStringField("type", "hotel");
            // Tel
            List<String> tels = abstractPOI.tel;
            jsonGenerator.writeFieldName(AbstractPOI.FD_TELEPHONE);
            jsonGenerator.writeStartArray();
            if (tels != null && (!tels.isEmpty())) {
                for (String tel : tels)
                    jsonGenerator.writeString(getString(tel));
            }
            jsonGenerator.writeEndArray();

        }
        // Rank
        Integer rank = abstractPOI.getRank();
        jsonGenerator.writeObjectField(AbstractPOI.FD_RANK, rank != null ? rank : null);

        // Targets
        jsonGenerator.writeFieldName(AbstractPOI.detTargets);
        List<ObjectId> targets = abstractPOI.targets;
        JsonSerializer<Object> retObjectId;
        if (targets != null && !targets.isEmpty()) {
            jsonGenerator.writeStartArray();
            retObjectId = serializerProvider.findValueSerializer(ObjectId.class, null);
            for (ObjectId id : targets)
                retObjectId.serialize(id, jsonGenerator, serializerProvider);
            jsonGenerator.writeEndArray();
        } else {
            retObjectId = serializerProvider.findNullValueSerializer(null);
            retObjectId.serialize(targets, jsonGenerator, serializerProvider);
        }

        // Locality
        jsonGenerator.writeFieldName(AbstractPOI.FD_LOCALITY);
        Locality localities = abstractPOI.getLocality();
        JsonSerializer<Object> retLocality;
        if (localities != null) {
            retLocality = serializerProvider.findValueSerializer(Locality.class, null);
            retLocality.serialize(localities, jsonGenerator, serializerProvider);
        } else {
            retLocality = serializerProvider.findNullValueSerializer(null);
            retLocality.serialize(localities, jsonGenerator, serializerProvider);
        }

        // Location
        jsonGenerator.writeFieldName(AbstractPOI.FD_LOCATION);
        GeoJsonPoint geoJsonPoint = abstractPOI.getLocation();
        JsonSerializer<Object> retLocalition;
        if (geoJsonPoint != null) {
            retLocalition = serializerProvider.findValueSerializer(GeoJsonPoint.class, null);
            retLocalition.serialize(geoJsonPoint, jsonGenerator, serializerProvider);
        } else {
            retLocalition = serializerProvider.findNullValueSerializer(null);
            retLocalition.serialize(geoJsonPoint, jsonGenerator, serializerProvider);
        }

        if (level.equals(Level.DETAILED)) {
            String id = abstractPOI.getId().toString();
            jsonGenerator.writeBooleanField(AizouBaseEntity.FD_IS_FAVORITE, getValue(abstractPOI.getIsFavorite()));
            jsonGenerator.writeStringField(AbstractPOI.FD_PRICE_DESC, getString(TaoziDataFilter.getPriceDesc(abstractPOI)));

            if (abstractPOI.getTrafficInfo() == null || abstractPOI.getTrafficInfo().equals(""))
                jsonGenerator.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "");
            else
                jsonGenerator.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "http://h5.taozilvxing.com/poi_traffic.php?tid=" + id);

            if (abstractPOI.getVisitGuide() == null || abstractPOI.getVisitGuide().equals(""))
                jsonGenerator.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "");
            else
                jsonGenerator.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "http://h5.taozilvxing.com/poi_play.php?tid=" + id);

            if (abstractPOI.getTips() == null || abstractPOI.getTips().equals(""))
                jsonGenerator.writeStringField(AbstractPOI.FD_TIPS_URL, "");
            else
                jsonGenerator.writeStringField(AbstractPOI.FD_TIPS_URL, "http://h5.taozilvxing.com/poi_tips.php?tid=" + id);
        }
        jsonGenerator.writeEndObject();
    }
}
