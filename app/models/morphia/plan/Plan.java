package models.morphia.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import models.morphia.misc.CheckinRatings;
import models.morphia.misc.Description;
import models.morphia.misc.ImageItem;
import models.morphia.misc.SimpleRef;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import play.libs.Json;

import java.util.*;

/**
 * 用户路线规划。
 *
 * @author Zephyre
 */
@Entity
public class Plan extends TravelPiBaseItem implements ITravelPiFormatter {

    @Embedded
    public List<SimpleRef> targets;

    public List<String> tags;

    public String title;

    public Integer planId;

    /**
     * 行程天数
     */
    public Integer days;

    /**
     * 景点个数
     */
    public Integer vsCnt;

    public String desc;

    @Embedded
    public Description description;

    public List<String> imageList;

    public List<ImageItem> images;

    public List<Integer> travelMonth;

    public Integer totalCost;

    public List<Integer> budget;

    public Integer stayBudget;

    public Integer trafficBudget;

    public Integer viewBudget;

    /**
     * 人工标记的路线优先级
     */
    public Integer manualPriority;

    /**
     * 注意事项
     */
    public String tips;

    /**
     * 路线速览
     */
    public List<String> summary;

    /**
     * 人工编辑的路线标签：最省钱……
     */
    public List<String> lxpTag;

    @Embedded
    public CheckinRatings ratings;

    public List<PlanDayEntry> details;


    @Override
    public JsonNode toJson() {
        return toJson(true);
    }

    public JsonNode toJson(boolean showDetails) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("_id", id.toString());


        List<JsonNode> targetList = new ArrayList<>();
        if (null != targets) {
            for (SimpleRef t : targets)
                targetList.add(t.toJson());

            if (targetList.size() > 0) {
                builder.add("target", targetList.get(0));
                builder.add("targets", Json.toJson(targetList));
            }
        }
        builder.add("tags", (tags != null && !tags.isEmpty()) ? Json.toJson(tags) : new ArrayList<>());
        builder.add("title", (title != null && !title.isEmpty()) ? title : "");
        if (days != null)
            builder.add("days", days);
        if (null != description && null != description.desc) {
            builder.add("desc", description.desc);
        }
        builder.add("ratings", ratings != null ? ratings.toJson() : Json.newObject());
        builder.add("imageList", (imageList != null && !imageList.isEmpty()) ? Json.toJson(imageList) : new ArrayList<>());

        // 如果存在更高阶的images字段，则使用之
        if (images != null && !images.isEmpty()) {
            List<ImageItem> imgList = new ArrayList<>();
            for (ImageItem img : images) {
                if (img.enabled != null && !img.enabled)
                    continue;
                imgList.add(img);
            }

            Collections.sort(imgList, new Comparator<ImageItem>() {
                @Override
                public int compare(ImageItem o1, ImageItem o2) {
                    if (o1.fSize != null && o2.fSize != null)
                        return o2.fSize - o1.fSize;
                    else if (o1.w != null && o2.w != null)
                        return o2.w - o1.w;
                    else if (o1.h != null && o2.h != null)
                        return o2.h - o1.h;
                    else
                        return 0;
                }
            });

            List<String> ret = new ArrayList<String>();
            for (ImageItem img : imgList) {
                if (img.url != null)
                    ret.add(img.url);
            }

            builder.add("imageList", ret);
        }

//        builder.add("budget", Arrays.asList(2000, 3000));

        Integer tempStayBudget = stayBudget == null ? 0 : stayBudget;
        Integer tempTrafficBudget = trafficBudget == null ? 0 : trafficBudget;
        Integer tempViewBudget = viewBudget == null ? 0 : viewBudget;
        builder.add("stayBudget", tempStayBudget);
        builder.add("trafficBudget", tempTrafficBudget);
        builder.add("viewBudget", tempViewBudget);
        Integer total = tempStayBudget + tempTrafficBudget + tempViewBudget;
        Integer addTotal = total * 2;
        total = (int) (Math.round(total / 100.0) * 100);
        addTotal = (int) (Math.round(addTotal / 80.0) * 100);
        builder.add("budget", Arrays.asList(total, addTotal));

        List<String> ret = new ArrayList<>();
        if (null != lxpTag)
            for (String tag : lxpTag) {
                ret.add(tag);
            }
        builder.add("lxpTag", ret);

        if (showDetails) {
            List<JsonNode> detailsNodes = new ArrayList<>();
            if (details != null) {
                for (PlanDayEntry entry : details) {
                    detailsNodes.add(entry.toJson());
                }
            }
            builder.add("details", !detailsNodes.isEmpty() ? Json.toJson(detailsNodes) : new ArrayList<>());
        }


        this.buildSummary(details);
        builder.add("summary", summary);
        builder.add("vsCnt", vsCnt);


        return Json.toJson(builder.get());
    }

    private void buildSummary(List<PlanDayEntry> details) {

        List<String> summaryList = new ArrayList<String>(10);
        StringBuffer result = new StringBuffer(10);
        String tempString = null;
        int day = 1;
        int tempVsCount = 0;
        List<PlanItem> tempAct = null;
        for (PlanDayEntry planDayEntry : details) {
            tempAct = planDayEntry.actv;
            for (PlanItem planItem : tempAct) {

                if (null != planItem.type && (planItem.type.equals("traffic"))) {
                    continue;
                }
                if (planItem.type.equals("vs"))
                    tempVsCount++;
                result.append(planItem.item.zhName);
                result.append("-");
            }
            tempString = result.toString();
            if (!tempString.equals(""))
                summaryList.add(tempString.substring(0, tempString.length() - 1));
            day++;
            result.setLength(0);
        }
        summary = summaryList;
        vsCnt = tempVsCount;

    }
}
