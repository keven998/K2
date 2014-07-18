package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系信息。
 *
 * @author Zephyre
 */
@Embedded
public class Contact {
    public List<String> phoneList;
    public String fax;
    public String email;

    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        for (String k : new String[]{"fax", "email"}) {
            try {
                Object val = Ratings.class.getField(k).get(this);
                builder.add(k, val);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
        }
        builder.add("phoneList", (phoneList != null ? phoneList : new ArrayList<>()));
        return Json.toJson(builder.get());
    }
}
