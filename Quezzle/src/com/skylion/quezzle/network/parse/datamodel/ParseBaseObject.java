package com.skylion.quezzle.network.parse.datamodel;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 20.03.14
 * Time: 23:14
 * To change this template use File | Settings | File Templates.
 */
public class ParseBaseObject {
    public String objectId;
    private String createdAt;
    private String updatedAt;

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    //TODO implements getters for createdAt and updatedAt
}