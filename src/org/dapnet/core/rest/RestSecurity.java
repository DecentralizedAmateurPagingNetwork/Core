/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2015
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institut für Hochfrequenztechnik
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.HashUtil;
import org.dapnet.core.model.User;

import javax.ws.rs.core.HttpHeaders;
import java.util.Base64;
import java.util.StringTokenizer;

public class RestSecurity {
    public enum SecurityLevel {
        ADMIN_ONLY, OWNER_ONLY, USER_ONLY, EVERYBODY
    }

    public enum SecurityStatus {
        UNAUTHORIZED, FORBIDDEN, ADMIN, OWNER, USER, ANYBODY, INTERNAL_ERROR
    }

    private static final Logger logger = LogManager.getLogger(RestSecurity.class.getName());
    private RestListener restListener;

    public RestSecurity(RestListener restListener) {
        this.restListener = restListener;
    }

    public SecurityStatus getStatus(HttpHeaders httpHeaders, SecurityLevel minSecurityLevel) {
        return getStatus(httpHeaders, minSecurityLevel, null);
    }

    public SecurityStatus getStatus(HttpHeaders httpHeaders, SecurityLevel minSecurityLevel,
                                    RestAuthorizable restAuthorizable) {
        //Get LoginData
        LoginData loginData;
        try {
            loginData = getLoginData(httpHeaders);
        } catch (Exception e) {
            //No Authorization Data in Http Header
            logger.info("No Authorization Data in HttpHeader");
            return checkAuthorization(minSecurityLevel, SecurityStatus.ANYBODY);
        }
        //Get User
        User user = restListener.getState().getUsers().findByName(loginData.getUsername());
        if (user == null) {
            logger.info("No User with such name");
            return SecurityStatus.UNAUTHORIZED;
        }
        //ValidatePassword
        boolean authenticated = false;
        try {
            authenticated = HashUtil.validatePassword(loginData.getPassword(), user.getHash());
        } catch (Exception e) {
            logger.error("Error while validating password");
            e.printStackTrace();
            return SecurityStatus.INTERNAL_ERROR;
        }
        if (!authenticated) {
            logger.info("Wrong Password");
            return SecurityStatus.UNAUTHORIZED;
        }

        //Check if admin
        if (user.isAdmin()) {
            return checkAuthorization(minSecurityLevel, SecurityStatus.ADMIN);
        }

        //Check if owner
        if (isOwner(user.getName(), restAuthorizable)) {
            return checkAuthorization(minSecurityLevel, SecurityStatus.OWNER);
        }

        //Is User
        return checkAuthorization(minSecurityLevel, SecurityStatus.USER);
    }

    private boolean isOwner(String name, RestAuthorizable restAuthorizable) {
        if (restAuthorizable == null) {
            return false;
        }
        for(String ownerName : restAuthorizable.getOwnerNames()){
            if(name.equals(ownerName)){
                return true;
            }
        }
        return false;
    }

    //Check SecurityStatus against minSecurityLevel
    private SecurityStatus checkAuthorization(SecurityLevel minSecurityLevel, SecurityStatus givenSecurityStatus) {
        switch (givenSecurityStatus) {
            case ADMIN:
                return SecurityStatus.ADMIN;
            case OWNER:
                switch (minSecurityLevel) {
                    case ADMIN_ONLY:
                        return SecurityStatus.FORBIDDEN;
                    default:
                        return SecurityStatus.OWNER;
                }
            case USER:
                switch (minSecurityLevel) {
                    case ADMIN_ONLY:
                        return SecurityStatus.FORBIDDEN;
                    case OWNER_ONLY:
                        return SecurityStatus.FORBIDDEN;
                    default:
                        return SecurityStatus.USER;
                }
            case ANYBODY:
                switch (minSecurityLevel) {
                    case ADMIN_ONLY:
                        return SecurityStatus.FORBIDDEN;
                    case OWNER_ONLY:
                        return SecurityStatus.FORBIDDEN;
                    case USER_ONLY:
                        return SecurityStatus.FORBIDDEN;
                    default:
                        return SecurityStatus.ANYBODY;
                }
            case INTERNAL_ERROR:
                return SecurityStatus.INTERNAL_ERROR;
        }
        return SecurityStatus.INTERNAL_ERROR;
    }

    //Create LoginData from httpHeaders
    public class LoginData {
        private String username;
        private String password;

        public LoginData(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public LoginData getLoginData(HttpHeaders httpHeaders) throws Exception {
        String authorizationToken = httpHeaders.getRequestHeader("Authorization").get(0);
        String encodedUserPassword = authorizationToken.replaceFirst("Basic ", "");
        String usernameAndPassword = null;
        byte[] decodedBytes = Base64.getDecoder().decode(
                encodedUserPassword);
        usernameAndPassword = new String(decodedBytes, "UTF-8");

        StringTokenizer tokenizer = new StringTokenizer(
                usernameAndPassword, ":");
        String username = tokenizer.nextToken();
        String password = tokenizer.nextToken();

        return new LoginData(username, password);
    }


}
