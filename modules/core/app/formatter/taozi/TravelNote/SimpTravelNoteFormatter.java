package formatter.taozi.TravelNote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.misc.TravelNote;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lxf on 14-11-1.
 */
public class SimpTravelNoteFormatter extends TaoziBaseFormatter {

    @Override
    public JsonNode format(AizouBaseEntity item) {

        item.fillNullMembers();

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("travelNoteFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept(
                        TravelNote.fnId,
                        TravelNote.fnAuthorAvatar,
                        TravelNote.fnAuthorName,
                        TravelNote.fnCover,
                        TravelNote.fnTitle,
                        TravelNote.fnPublishDate,
                        TravelNote.fnSource,
                        TravelNote.fnSummary
                ));

        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));

        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);

        return mapper.valueToTree(item);
    }
}