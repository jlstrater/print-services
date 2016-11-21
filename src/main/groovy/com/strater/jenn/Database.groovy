package com.strater.jenn

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j

@Slf4j
@SuppressWarnings('UnnecessaryObjectReferences')
class Database {
    static Sql db = Sql.newInstance('jdbc:h2:~/users', 'sa', 'sa', 'org.h2.Driver')

    static void setupTestUsers() {
        //setup user database

        setupUsers()

        setupACL()

        setupRBAC()
    }

    static private setupUsers() {
        //reset table on each server restart
        db.execute('drop table if exists users')

        db.execute('create table users (id int primary key, username varchar , password varchar, sessionId varchar, ' +
                'expiration timestamp, role varchar NOT NULL )')
        db.withBatch {
            String insert = 'insert into users (id, username, password, role) values(:id, :username, :password, :role)'
            db.execute(insert, [id: 1, username: 'unitTest', password: Crypto.encrypt('unitTest', 'test123'),
                                 role: 'ADMIN'])
            db.execute(insert, [id: 2, username: 'bobTheBuilder', password:
                    Crypto.encrypt('bobTheBuilder', 'nightCrewRulez'), role: 'TECHNICIAN'])
            db.execute(insert, [id: 3, username: 'oridinaryJoe', password:
                    Crypto.encrypt('oridinaryJoe', 'password'), role: 'USER'])
            db.execute(insert, [id: 4, username: 'powerUser1', password: Crypto.encrypt('powerUser1', 'supersecret123'),
                                 role: 'POWER_USER'])
            db.execute(insert, [id: 5, username: 'admin', password: Crypto.encrypt('admin', 'supersecret456'),
                                role: 'ADMIN'])
        }

        List<GroovyRowResult> result = db.rows ('select username from users')
        log.info 'created users table with users: ' + result*.get('username').toString()
    }

    static private setupACL() {
        db.execute('drop table if EXISTS access_control_list')
        db.execute('create table access_control_list(id int primary key, role varchar , permission varchar)')
        db.withBatch {
            String insert = 'insert into access_control_list(id, role, permission) values(:id, :role, :permission)'
            db.execute(insert, [id: 1, role: 'USER', permission: 'print'])
            db.execute(insert, [id: 2, role: 'USER', permission: 'queue'])
            db.execute(insert, [id: 3, role: 'POWER_USER', permission: 'print'])
            db.execute(insert, [id: 4, role: 'POWER_USER', permission: 'queue'])
            db.execute(insert, [id: 5, role: 'POWER_USER', permission: 'topQueue'])
            db.execute(insert, [id: 6, role: 'POWER_USER', permission: 'restart'])
            db.execute(insert, [id: 7, role: 'TECHNICIAN', permission: 'setConfig'])
            db.execute(insert, [id: 8, role: 'TECHNICIAN', permission: 'readConfig'])
            db.execute(insert, [id: 9, role: 'TECHNICIAN', permission: 'status'])
            db.execute(insert, [id: 10, role: 'TECHNICIAN', permission: 'stop'])
            db.execute(insert, [id: 11, role: 'TECHNICIAN', permission: 'start'])
            db.execute(insert, [id: 12, role: 'TECHNICIAN', permission: 'restart'])
            db.execute(insert, [id: 13, role: 'ADMIN', permission: 'print'])
            db.execute(insert, [id: 14, role: 'ADMIN', permission: 'queue'])
            db.execute(insert, [id: 15, role: 'ADMIN', permission: 'topQueue'])
            db.execute(insert, [id: 16, role: 'ADMIN', permission: 'restart'])
            db.execute(insert, [id: 17, role: 'ADMIN', permission: 'setConfig'])
            db.execute(insert, [id: 18, role: 'ADMIN', permission: 'readConfig'])
            db.execute(insert, [id: 19, role: 'ADMIN', permission: 'status'])
            db.execute(insert, [id: 20, role: 'ADMIN', permission: 'stop'])
            db.execute(insert, [id: 21, role: 'ADMIN', permission: 'start'])
        }
    }

    static private setupRBAC() {
        db.execute('drop table if exists permissions')
        db.execute('create table permissions (id int primary key, role varchar, permission varchar)')
        db.withBatch {
            String insert = 'insert into permissions(id, role, permission) values(:id, :role, :permission)'
            db.execute(insert, [id: 1, role: 'USER', permission: 'print'])
            db.execute(insert, [id: 2, role: 'USER', permission: 'queue'])
            db.execute(insert, [id: 3, role: 'POWER_USER', permission: 'topQueue'])
            db.execute(insert, [id: 4, role: 'POWER_USER', permission: 'restart'])
            db.execute(insert, [id: 5, role: 'TECHNICIAN', permission: 'setConfig'])
            db.execute(insert, [id: 6, role: 'TECHNICIAN', permission: 'readConfig'])
            db.execute(insert, [id: 7, role: 'TECHNICIAN', permission: 'status'])
            db.execute(insert, [id: 8, role: 'TECHNICIAN', permission: 'stop'])
            db.execute(insert, [id: 9, role: 'TECHNICIAN', permission: 'start'])
            db.execute(insert, [id: 10, role: 'TECHNICIAN', permission: 'restart'])
        }

        db.execute('drop table if exists role_inheritance')
        db.execute('create table role_inheritance (id int primary key, role varchar, inheritsFrom varchar not null)')
        db.withBatch {
            String insert = 'insert into role_inheritance(id, role, inheritsFrom) values(:id, :role, :inheritsFrom)'
            db.execute(insert, [id: 1, role: 'POWER_USER', inheritsFrom: 'USER'])
            db.execute(insert, [id: 2, role: 'ADMIN', inheritsFrom: 'POWER_USER'])
            db.execute(insert, [id: 3, role: 'ADMIN', inheritsFrom: 'TECHNICIAN'])
        }
    }
}
