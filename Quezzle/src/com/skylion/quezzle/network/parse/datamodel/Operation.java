package com.skylion.quezzle.network.parse.datamodel;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 27.03.14
 * Time: 22:59
 * To change this template use File | Settings | File Templates.
 */
public class Operation {
    private String method;
    private String path;
    private Object body;

    public Operation(String method, String path, Object body) {
        this.method = method;
        this.path = path;
        this.body = body;
    }
}
