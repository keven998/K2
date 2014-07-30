package models.morphia.traffic;

import com.mongodb.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.morphia.misc.SimpleRef;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import play.data.validation.Constraints;
import models.morphia.traffic.TrainEntry;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import play.libs.Json;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * 列车路线。
 *
 * @author Zephyre
 */
@Entity
public class TrainRoute extends AbstractRoute {
    public Map<String, Double> price;
    /**
     * 列车车次的类型。包括但不限于：
     * T：特快
     * D：动车
     * G：高铁
     */
    public String type;

    public List<TrainEntry> details;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("_id", id.toString()).add("code", code);

        for (Map.Entry<String,String> entry: new HashMap<String,String>(){
            {
                put("arrStop", "arrTrainStation");
                put("depStop", "depTrainStation");
            }
        }.entrySet()){
            String k = entry.getKey();
            String v = entry.getValue();
            SimpleRef val = null;
            try {
                val = (SimpleRef) TrainRoute.class.getField(k).get(this);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
            //PC_Chen:return {} instead of ""
            builder.add(v, val != null ? val.toJson() : new HashMap<>());
        }

        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        for (String k : new String[]{"depTime", "arrTime"}) {
            Date val = null;
            try {
                val = (Date) AirRoute.class.getField(k).get(this);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            builder.add(k, val != null ? fmt.format(val) : "");
        }

        super.addColumn(builder,TrainRoute.class,"timeCost","totalDist","distance","type");

        // 价格列表
        BasicDBObjectBuilder priceBuilder = BasicDBObjectBuilder.start();
        for (Map.Entry<String,Double> entry:price.entrySet()){
            String k = entry.getKey();
            Double v = entry.getValue();

            //PC_Chen:return {} instead of ""
            if (v!=null)
            priceBuilder.add(k, v);
        }
        builder.add("priceList", priceBuilder.get());

        //最低价格
         DBObject priceList = priceBuilder.get();
            // 最低票价
         if (null!= priceList ){
            builder.add("price", Collections.min(priceList.toMap().values()));
         }

        //no:selfChk meal noStop jetName jetFullName

        return Json.toJson(builder.get());
    }




}
