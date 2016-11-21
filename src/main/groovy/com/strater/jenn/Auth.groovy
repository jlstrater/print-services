package com.strater.jenn

import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import java.sql.Timestamp

class Auth {
    Sql db = Database.db

    Boolean authenticateAndAuthorizeWithACL(String username, String password, String action) {
        if (!authenticate(username, password)) {
            return false
        } else if (!authorizeWithAcl(username, action)) {
            return false
        }
        return true
    }

    String authenticateAndAuthorizeWithACL(String sessionId, String action) {
        String username = authenticate(sessionId)
        if (!username) {
            return ''
        } else if (!authorizeWithAcl(username, action)) {
            return ''
        }
        return username
    }

    Boolean authenticateAndAuthorizeWithRbac(String username, String password, String action) {
        if (!authenticate(username, password)) {
            return false
        } else if (!authorizeWithRbac(username, action)) {
            return false
        }
        return true
    }

    String authenticateAndAuthorizeWithRbac(String sessionId, String action) {
        String username = authenticate(sessionId)
        if (!username) {
            return ''
        } else if (!authorizeWithRbac(username, action)) {
            return ''
        }
        return username
    }

    private Boolean authenticate(String username, String password) {
        GroovyRowResult row = db.firstRow('select password from users where username=:username', [username: username])

        return Crypto.compare(username, password, row?.get('password')?.toString())
    }

    private String authenticate(String sessionId) {
        GroovyRowResult row = db.firstRow('select username from users where sessionId=:sessionId and ' +
                'expiration >= :now',
                [sessionId: sessionId, now: new Timestamp(System.currentTimeMillis())])
        return row?.get('username')
    }

    private Boolean authorizeWithAcl(String username, String action) {
        GroovyRowResult result = db.firstRow('select role from users where username=:username', [username: username])
        String role = result.get('role')
        List<String> permissions = getAclPermissions(role)
        return (action in permissions)
    }

    private List<String> getAclPermissions(String role) {
        List<GroovyRowResult> permissions = db.rows('select permission from access_control_list where role=:role',
                [role: role])
        return permissions*.get('permission')
    }

    private Boolean authorizeWithRbac(String username, String action) {
        GroovyRowResult result = db.firstRow('select role from users where username=:username', [username: username])
        String role = result.get('role')
        List<String> permissions = getRbacPerimssions(role, [])
        return (action in permissions)
    }

    private List<String> getRbacPerimssions(String role, List<String> allPermissions) {
        if (!role) {
            return allPermissions
        }
        List<GroovyRowResult> permissions = db.rows('select permission from permissions where role=:role',
                [role: role])
        allPermissions.addAll(permissions*.get('permission'))
        List<GroovyRowResult> hierarchy = db.rows('select inheritsFrom from role_inheritance where role=:role',
                [role: role])
        if (hierarchy) {
            hierarchy.each {
                allPermissions.addAll(getRbacPerimssions(it.get('inheritsFrom'), allPermissions))
            }
        }
        return allPermissions
    }
}
