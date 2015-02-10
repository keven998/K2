package formatter.taozi.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.ImageItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lxf on 14-11-1.
 */
public class DetailsEntryFormatterOld extends TaoziBaseFormatter {

    @Override
    public JsonNode format(AizouBaseEntity item) {

        item.fillNullMembers();

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("detailsEntryFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept(
                        "title",
                        "desc",
                        "images"));

        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializerOld(ImageItemSerializerOld.ImageSizeDesc.MEDIUM));

        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);

        return mapper.valueToTree(item);
    }
}