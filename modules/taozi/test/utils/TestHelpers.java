package utils;

import com.fasterxml.jackson.databind.JsonNode;
import play.api.mvc.HandlerRef;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeRequest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;

/**
 * Created by zephyre on 2/9/15.
 */
public class TestHelpers {

    public static JsonNode getResultNode(HandlerRef<?> handler, FakeRequest req) {
        Result result;
        if (req != null)
            result = callAction(handler, req);
        else
            result = callAction(handler);
        JsonNode node = Json.parse(contentAsString(result));

        assertThat(node.get("code").asInt()).isEqualTo(0);

        return node.get("result");
    }

    public static JsonNode getResultNode(HandlerRef<?> handler) {
        return getResultNode(handler, null);
    }

    /**
     * 检查item的字段名称是否正确
     *
     * @param item
     * @param fields
     */
    public static void assertFields(JsonNode item, String... fields) {
        Set<String> standands = new HashSet<>();
        Collections.addAll(standands, fields);

        Set<String> nodeFields = new HashSet<>();
        for (Iterator<String> itr = item.fieldNames(); itr.hasNext(); )
            nodeFields.add(itr.next());

        assertThat(nodeFields).isEqualTo(standands);
    }

    public static void assertText(JsonNode node, String field, boolean allowEmpty) {
        assertText(node, new String[]{field}, allowEmpty);
    }

    public static void assertText(JsonNode node, String[] fields, boolean allowEmpty) {
        for (String key : fields) {
            JsonNode txtNode = node.get(key);
            if (txtNode == null)
                assertThat(false).isTrue();
            else {
                assertThat(txtNode.isTextual()).isTrue();
                if (!allowEmpty)
                    assertThat(txtNode.asText().trim().isEmpty()).isFalse();
            }
        }
    }

    public static void assertInt(JsonNode node, String field, boolean nullable) {
        assertInt(node, new String[]{field}, nullable);
    }

    public static void assertInt(JsonNode node, String[] fields, boolean nullable) {
        for (String key : fields) {
            JsonNode valNode = node.get(key);

            if (!nullable && (valNode == null || valNode.isNull()))
                assertThat(false).isTrue();

            if (valNode != null && !valNode.isNull()) {
                assertThat(valNode.isInt()).isTrue();
            }
        }
    }

    public static void assertFloat(JsonNode node, String field, boolean nullable) {
        assertFloat(node, new String[]{field}, nullable);
    }

    public static void assertFloat(JsonNode node, String[] fields, boolean nullable) {
        for (String key : fields) {
            JsonNode valNode = node.get(key);

            if (!nullable && (valNode == null || valNode.isNull()))
                assertThat(false).isTrue();

            if (valNode != null && !valNode.isNull()) {
                assertThat(valNode.isDouble()).isTrue();
            }
        }
    }

    public static void assertCoords(double lng, double lat) {
        assertThat(Math.abs(lng)).isGreaterThan(0);
        assertThat(Math.abs(lng)).isLessThan(180);
        assertThat(Math.abs(lat)).isGreaterThan(0);
        assertThat(Math.abs(lat)).isLessThan(90);
    }

    public static void assertCoords(JsonNode item) {
        double lng = item.get("coordinates").get(0).asDouble();
        double lat = item.get("coordinates").get(1).asDouble();
        assertThat(lng > -180 && lng < 180).isTrue();
        assertThat(lat > -90 && lat < 90).isTrue();
        assertThat(lat != 0 && lng != 0).isTrue();
    }

    public static void assertImages(JsonNode images, boolean allowEmpty) {
        assertThat(images.isArray()).isTrue();
        if (!allowEmpty)
            assertThat(images.size()).isPositive();

        for (JsonNode img : images)
            assertText(img, "url", false);
    }
}
