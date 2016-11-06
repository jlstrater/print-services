package com.strater.jenn

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j

@Slf4j
class Database {
    static Sql db = Sql.newInstance('jdbc:h2:~/users', 'sa', 'sa', 'org.h2.Driver')

    static void setupTestUsers() {
        //setup user database
        Sql sql = new Database().db

        //reset table on each server restart
        sql.execute('drop table if exists users')

        sql.execute('create table users (id int, username varchar , password varchar, sessionId varchar, ' +
                'ttl timestamp )')
        sql.execute('insert into users (id, username, password) values(:id, :username, :password)',
                [id: 1, username: 'jenn', password: new Crypto().encrypt('jenn', 'supersecret123')])
        sql.execute('insert into users (id, username, password) values(:id, :username, :password)',
                [id: 1, username: 'admin', password: new Crypto().encrypt('admin', 'petsname5')])
        sql.execute('insert into users (id, username, password) values(:id, :username, :password)',
                [id: 1, username: 'test', password: new Crypto().encrypt('test', 'test123')])

        List<GroovyRowResult> result = sql.rows ('select username from users')
        log.info 'created users table with users: ' + result*.get('username').toString()
    }
}
