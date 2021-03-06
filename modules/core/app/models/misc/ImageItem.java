package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

import java.util.Map;

/**
 * 表示一张图像。
 * <p>
 * Created by zephyre on 8/14/14.
 */
@JsonFilter("imageItemFilter")
@Embedded
public class ImageItem extends AizouBaseItem {

    @Transient
    public static final String FD_URL = "url";

    @Transient
    public static final String FD_CROP_HINT = "cropHint";

    @Transient
    public static final String FD_WIDTH = "w";

    @Transient
    public static final String FD_HEIGHT = "h";
    @Transient
    public static final String FD_CAPTION = "caption";

    private String caption;

    private Map<String, Integer> cropHint;

    private String key;

    private String bucket;

    private String url;

    private ObjectId id;
    /**
     * 图像宽度
     */
    private Integer w;

    /**
     * 图像高度
     */
    private Integer h;

    /**
     * 图像格式
     */
    private String fmt;

    /**
     * 图像色彩模型。
     */
    private String cm;

    /**
     * 图像MD5哈希校验
     */
    private String hash;

    /**
     * 图像文件的大小
     */
    private Integer size;

    public ImageItem() {
        if (key == null && url != null) {
            key = url.substring(39, url.length() - 1);
        }
    }

    /**
     * 根据bucket和key，生成完整的图像链接
     */
    public String getFullUrl() {
        if (key != null)
            //return String.format("http://%s.qiniudn.com/%s", bucket != null ? bucket : "taozi-uploads", key);
            return bucket != null ? String.format("http://%s.qiniudn.com/%s", bucket, key) : String.format("http://images.taozilvxing.com/%s", key);
        else
            return null;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public Integer getW() {
        return w;
    }

    public void setW(Integer w) {
        this.w = w;
    }

    public Integer getH() {
        return h;
    }

    public void setH(Integer h) {
        this.h = h;
    }

    public String getFmt() {
        return fmt;
    }

    public void setFmt(String fmt) {
        this.fmt = fmt;
    }

    public String getCm() {
        return cm;
    }

    public void setCm(String cm) {
        this.cm = cm;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Map<String, Integer> getCropHint() {
        return cropHint;
    }

    public void setCropHint(Map<String, Integer> cropHint) {
        this.cropHint = cropHint;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
