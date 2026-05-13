package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity;

public class ClaimImageResponse {

    private Long imageId;
    private String fileName;
    private String contentType;
    private String imageUrl;

    public ClaimImageResponse(Long claimId, ClaimImage claimImage) {
        this.imageId = claimImage.getImageId();
        this.fileName = claimImage.getFileName();
        this.contentType = claimImage.getContentType();
        this.imageUrl = "/claims/" + claimId + "/images/" + claimImage.getImageId();
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
