package models.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import org.mongodb.morphia.annotations.Entity;

/**
 * 美食评论
 * <p>
 * Created by topy on 14-11-12.
 */
@JsonFilter("shoppingCommentFilter")
@Entity
public class ShoppingComment extends Comment {


}
