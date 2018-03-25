package com.entity;

import javax.persistence.*;

@Entity
public class Picture {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Integer idPicture;
    private String description;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private String filename;

    @Column(columnDefinition = "TEXT")
    private String image;

    private String contentType;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Picture() {
    }

    public Integer getIdPicture() {
        return idPicture;
    }

    public void setIdPicture(Integer idPicture) {
        this.idPicture = idPicture;
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
}
