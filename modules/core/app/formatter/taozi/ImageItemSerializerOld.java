package formatter.taozi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import models.misc.ImageItem;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zephyre on 12/6/14.
 */
public class ImageItemSerializerOld extends JsonSerializer<ImageItem> {
    private ImageSizeDesc sizeDesc;
    private int width;

    public ImageItemSerializerOld() {
        this(ImageSizeDesc.MEDIUM);
    }

    public ImageItemSerializerOld(ImageSizeDesc sz) {
        this.sizeDesc = sz;
        this.width = 0;
    }

    public ImageItemSerializerOld(int width) {
        this.width = width;
    }

    public enum ImageSizeDesc {
        SMALL,
        MEDIUM,
        LARGE,
        FULL
    }

    @Override
    public void serialize(ImageItem imageItem, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();

        int maxWidth;
        String url;

        if (width == 0) {
            if (sizeDesc == null)
                maxWidth = 960;
            else {
                switch (sizeDesc) {
                    case SMALL:
                        maxWidth = 400;
                        break;
                    case MEDIUM:
                        maxWidth = 640;
                        break;
                    case LARGE:
                    default:
                        maxWidth = 960;
                        break;
                }
            }
        } else
            maxWidth = width;

        // 如果只给url，则是原链接，图片没上传，无法裁剪缩放
        if (imageItem.getKey() == null && imageItem.getUrl() != null) {
            jsonGenerator.writeStringField("url", imageItem.getUrl());
            jsonGenerator.writeNumberField("width", null);
            jsonGenerator.writeNumberField("height", null);
            jsonGenerator.writeEndObject();
            return;
        }

        String fullUrl = imageItem.getFullUrl();
        Integer width = imageItem.getW();
        Integer height = imageItem.getH();

        if (width != null && height != null) {
            String imgUrl;
            Map<String, Integer> cropHint = imageItem.getCropHint();
            if (sizeDesc == ImageSizeDesc.FULL)
                imgUrl = fullUrl;
            else {
                if (cropHint == null) {
                    imgUrl = String.format("%s?imageView2/2/w/%d", fullUrl, maxWidth);
                    double r = (double) height / width;
                    width = maxWidth;
                    height = (int) (width * r);
                } else {
                    int top = cropHint.get("top");
                    int right = cropHint.get("right");
                    int bottom = cropHint.get("bottom");
                    int left = cropHint.get("left");

                    width = right - left;
                    height = bottom - top;

                    imgUrl = String.format("%s?imageMogr2/auto-orient/strip/gravity/NorthWest/crop/!%dx%da%da%d/thumbnail/%d",
                            fullUrl, width, height, left, top, maxWidth);
                }
            }
            jsonGenerator.writeStringField("url", imgUrl);
            jsonGenerator.writeNumberField("width", width);
            jsonGenerator.writeNumberField("height", height);
        } else
            jsonGenerator.writeStringField("url", String.format("%s?imageView2/2/w/%d", fullUrl, maxWidth));

        jsonGenerator.writeEndObject();
    }
}
