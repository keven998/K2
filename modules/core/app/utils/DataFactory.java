package utils;

import models.geo.Locality;
import models.misc.SimpleRef;
import models.plan.PlanItem;
import models.traffic.AbstractRoute;
import models.traffic.AirRoute;
import models.traffic.TrainRoute;

import java.util.*;

/**
 * Created by topy on 2014/9/19.
 */
public class DataFactory {

    public static List<?> asList(Iterator<?> it) {
        if (null == it) {
            return Collections.emptyList();
        }
        List l = new ArrayList();
        for (; it.hasNext(); ) {
            Object element = (Object) it.next();
            l.add(element);
        }
        return l;

    }

    public static PlanItem createLocality(Locality locality) {
        if (null == locality) {
            return null;
        }
        PlanItem nearCap = new PlanItem();
        SimpleRef ref = new SimpleRef();
        ref.id = locality.getId();
        ref.zhName = locality.getZhName();
        nearCap.item = ref;
        nearCap.type = "loc";
        SimpleRef loc = new SimpleRef();
        loc.id = locality.getId();
        loc.zhName = locality.getZhName();
        nearCap.loc = loc;
        return nearCap;
    }

    public static PlanItem createDepStop(AbstractRoute route) {
        String subType;
        if (route instanceof AirRoute)
            subType = "airport";
        else if (route instanceof TrainRoute)
            subType = "trainStation";
        else
            subType = "";

        PlanItem depItem = new PlanItem();
        depItem.item = route.depStop;
        depItem.loc = route.depLoc;
        depItem.ts = route.depTime;
        depItem.type = "traffic";
        depItem.subType = subType;

        return depItem;
    }

    public static PlanItem createArrStop(AbstractRoute route) {
        String subType;
        if (route instanceof AirRoute)
            subType = "airport";
        else if (route instanceof TrainRoute)
            subType = "trainStation";
        else
            subType = "";

        PlanItem arrItem = new PlanItem();
        arrItem.item = route.arrStop;
        arrItem.loc = route.arrLoc;
        arrItem.ts = route.arrTime;
        arrItem.type = "traffic";
        arrItem.subType = subType;

        return arrItem;
    }

    public static PlanItem createTrafficInfo(AbstractRoute route) {
        PlanItem trafficInfo = new PlanItem();
        SimpleRef ref = new SimpleRef();
        ref.id = route.getId();
        ref.zhName = route.code;
        trafficInfo.item = ref;
        trafficInfo.ts = route.depTime;
        trafficInfo.extra = route;
        trafficInfo.type = "traffic";
        if (route instanceof AirRoute)
            trafficInfo.subType = "airRoute";
        else if (route instanceof TrainRoute)
            trafficInfo.subType = "trainRoute";
        else
            trafficInfo.subType = "";

        return trafficInfo;
    }

    /**
     * 生成添加交通时的时间过滤
     */
    public static List<Calendar> createTrafficTimeFilter(Date firstDate, boolean epDep) {

        Calendar calLower, calUpper;
        if (epDep) {
            calLower = Calendar.getInstance();
            calLower.setTime(firstDate);
            // 允许的日期从前一天17:00到第二天12:00
            calLower.add(Calendar.DAY_OF_YEAR, -1);
            calLower.set(Calendar.HOUR_OF_DAY, 17);
            calLower.set(Calendar.MINUTE, 0);
            calLower.set(Calendar.SECOND, 0);
            calLower.set(Calendar.MILLISECOND, 0);
            calUpper = Calendar.getInstance();
            calUpper.setTime(firstDate);
            calUpper.set(Calendar.HOUR_OF_DAY, 12);
            calUpper.set(Calendar.MINUTE, 0);
            calUpper.set(Calendar.SECOND, 0);
            calUpper.set(Calendar.MILLISECOND, 0);
        } else {
            calLower = Calendar.getInstance();
            calLower.setTime(firstDate);
            // 允许的日期从前一天12:00到第二天12:00
            calLower.set(Calendar.HOUR_OF_DAY, 12);
            calLower.set(Calendar.MINUTE, 0);
            calLower.set(Calendar.SECOND, 0);
            calLower.set(Calendar.MILLISECOND, 0);
            calUpper = Calendar.getInstance();
            calUpper.setTime(firstDate);
            calUpper.add(Calendar.DAY_OF_YEAR, 1);
            calUpper.set(Calendar.HOUR_OF_DAY, 12);
            calUpper.set(Calendar.MINUTE, 0);
            calUpper.set(Calendar.SECOND, 0);
            calUpper.set(Calendar.MILLISECOND, 0);
        }

        return Arrays.asList(calLower, calUpper);
    }
}
