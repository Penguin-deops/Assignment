package com.waracle.cakemgr;



import java.io.Serializable;
import javax.persistence.*;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
@Table(name = "CakeEntity", uniqueConstraints = {@UniqueConstraint(columnNames = "ID"), @UniqueConstraint(columnNames = "TITLE")})
public class CakeEntity implements Serializable {

    private static final long serialVersionUID = -1798070786993154676L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Integer id;

    @Column(name = "TITLE", unique = true, nullable = false, length = 100)
    private String title;

    @Column(name = "DESCRIPTION", unique = false, nullable = false, length = 100)
    private String description;

    @Column(name = "IMAGE", unique = false, nullable = false, length = 300)
    private String image;

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("CaleEntity [");
        stringBuilder.append("id=").append( getId() );
        stringBuilder.append(", title=").append( getTitle() );
        stringBuilder.append(", description=").append( getDescription() );
        stringBuilder.append("] ");
        
        return stringBuilder.toString();
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}