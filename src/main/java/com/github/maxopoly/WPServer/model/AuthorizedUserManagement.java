package com.github.maxopoly.WPServer.model;

import com.github.maxopoly.WPCommon.model.permission.Permission;

import com.github.maxopoly.WPServer.database.AuthDAO;
import java.util.Map;

public class AuthorizedUserManagement {

	private AuthDAO db;
	private Map<String, Permission> userUUIDS;

	public AuthorizedUserManagement(AuthDAO dao) {
		this.db = dao;
		userUUIDS = db.loadAll();
	}

	public boolean isAuthorized(String uuidWithoutDash) {
		return userUUIDS.containsKey(uuidWithoutDash);
	}

	public Permission getPermission(String uuidWithoutDash) {
		return userUUIDS.get(uuidWithoutDash);
	}

}
