package com.kci.network;

public class ApiParameter {
    public static final String SERVER_ADDR                  =   "http://snsmecca.com/api/v1/sms/";
    public static final String GOOGLE_SERVER_ADDR           =   "https://maps.googleapis.com/maps/api/geocode/json";
    public static final int API_RESP_ERROR                  =   -2;

    public static final String API_REQ_PHONE_NUMBER         =   "number";
    public static final String API_REQ_SENT_DATE            =   "yyyymmdd";
    public static final String API_REQ_SENT_TIME            =   "hhmmss";
    public static final String API_REQ_SENT_NO              =   "no";
    public static final String API_REQ_SMS_CONTENT          =   "sms";

    public static final String API_RESP_STATUS              =   "status";

    // the code of activity or fragment corresponding to message
    public static final int CODE_LOGIN_MSG                  =   1;
    public static final int CODE_SMS_MSG                    =   2;
    public static final int CODE_AUTH_MSG                   =   3;
}
