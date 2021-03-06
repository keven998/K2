package models.group;

import models.AizouBaseEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Transient;

import java.util.Arrays;
import java.util.List;

/**
 * Created by topy on 2015/6/15.
 */
public class ChatGroup extends AizouBaseEntity {

    @Transient
    public static String FD_GROUPID = "groupId";

    @Transient
    public static String FD_NAME = "name";

    @Transient
    public static String FD_DESC = "desc";

    @Transient
    public static String FD_TYPE = "type";

    @Transient
    public static String FD_TYPE_COMMON = "common";

    @Transient
    public static String FD_AVATAR = "avatar";

    @Transient
    public static String FD_TAGS = "tags";

    @Transient
    public static String FD_CREATOR = "creator";

    @Transient
    public static String FD_MAXUSERS = "maxUsers";

    @Transient
    public static String FD_VISIBLE = "visible";

    @Transient
    public static String FD_PARTICIPANTS = "participants";

    @Transient
    public static String FD_PARTICIPANTCNT = "participantCnt";

    @Indexed(unique = true)
    private Long groupId;

    private String name;

    private String desc;

    private String type;

    private String avatar;

    private List<String> tags;

    private Long creator;

    private List<Long> admin;


    private List<Long> participants;

    private Integer participantCnt;

    private Long msgCounter;

    private Integer maxUsers;

    private Long createTime;

    private Long updateTime;

    private Boolean visible;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public List<Long> getAdmin() {
        return admin;
    }

    public void setAdmin(List<Long> admin) {
        this.admin = admin;
    }

    public List<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Long> participants) {
        this.participants = participants;
    }

    public Long getMsgCounter() {
        return msgCounter;
    }

    public void setMsgCounter(Long msgCounter) {
        this.msgCounter = msgCounter;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Integer getParticipantCnt() {
        if (participants == null)
            return 0;
        else
            return participants.size();
    }

    public void setParticipantCnt(Integer participantCnt) {
        this.participantCnt = participantCnt;
    }
}
