package utils.formatter.taozi.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import models.TravelPiBaseItem;
import models.user.UserInfo;
import utils.formatter.JsonFormatter;

import java.util.HashSet;
import java.util.Set;

/**
 * 返回用户的详细信息（即：查看自己的用户信息时使用）
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class SelfUserFormatter implements JsonFormatter {
    @Override
    public JsonNode format(TravelPiBaseItem item) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        PropertyFilter theFilter = new SimpleBeanPropertyFilter() {
            @Override
            public void serializeAsField
                    (Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
                if (include(writer)) {
                    writer.serializeAsField(pojo, jgen, provider);
                } else if (!jgen.canOmitFields()) { // since 2.3
                    writer.serializeAsOmittedField(pojo, jgen, provider);
                }
            }

            private boolean includeImpl(PropertyWriter writer) {
                Set<String> includedFields = new HashSet<>();
                includedFields.add(UserInfo.fnNickName);
                includedFields.add(UserInfo.fnAvatar);
                includedFields.add(UserInfo.fnUserId);
                includedFields.add(UserInfo.fnGender);
                includedFields.add(UserInfo.fnSignature);
                includedFields.add(UserInfo.fnTel);
                includedFields.add(UserInfo.fnDialCode);
                includedFields.add(UserInfo.fnEmail);
                includedFields.add(UserInfo.fnEasemobUser);

                return (includedFields.contains(writer.getName()));
            }

            @Override
            protected boolean include(BeanPropertyWriter beanPropertyWriter) {
                return includeImpl(beanPropertyWriter);
            }

            @Override
            protected boolean include(PropertyWriter writer) {
                return includeImpl(writer);
            }
        };

        FilterProvider filters = new SimpleFilterProvider().addFilter("userInfoFilter", theFilter);
        mapper.setFilters(filters);

        return mapper.valueToTree(item);
    }
}