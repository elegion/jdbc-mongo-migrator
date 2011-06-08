SQL (JDBC) to MongoDB migration tool
====================================

This is our internal run-once utility, which purpose is to import data from
relational database to MongoDB using JSON mapping file.

Note:
-----
As it was written as internal run-once tool, it contains a lot of dirty hacks and poorly-written code, sorry.
After all, we decided that it seem to be rather universal and useful to be opensourced.

Features:
---------
* Should work with virtually any SQL database (using JDBC). Tested with H2 and MySQL.
* Collections are mapped to SQL SELECTs, making possible to use JOINs and SQL functions e.g. LOWER().
* Nested structures creation using dot notation.
* Embedded arrays, counters, maps, using child SELECTs for each entity.
* Placeholders for entity fields in child selects (e.g. in "where" clause)
* Explicit conversion to int/long/string
* Cached column-to-objectId bindings, which allows to refer not-yet-inserted documents.
* Batch select/insert, may significally improve performance

Mapping format
--------------
Example of mapping file:
    { "collections": {
        //MongoDB collection name. There could be several mappings for single collection,
        //which result in consecutive inserts
        "users": {
          //FROM part, it is possible to use JOINs here
          "from": "users AS u"

          //columns-fields mapping
          "mapping": {
            // Generate mongodb objectId.
            // All subsequent usages of {"$oid" : { "collection" : "users", "key" : ... }} (or $oidString) with the same key
            // will generate the same objectId
            "_id" : { "$oid" : "u.id" }

            // Same as above, but converts objectId's to string
            "stringId" : {"$oidString" : "u.id"}

            // Simple column-to-field value binding
            "oldId" : "u.id"
            "username" : "u.username"

            // Explicitly convert rating column to integer type
            // (to prevent automatic conversion to long by JDBC driver)
            // Dot notation in field name will create nested "counters" structure
            "counters.rating" : { "$int" : "u.rating"}

            // Integer field, countaining count(*) of user's posts.
            // Note the reference to oldId field
            "counters.posts" : {
                "$count" : {
                    "from" :    "posts AS p"
                    "where" :   "p.user_id = ${oldId}"
                }
            }

            // generate short url for each user using link_id column
            // like "http://exmpl.me/u/fQsd"
            "shortUrl" : { "$surl" : "http://exmpl.me/u/${u.link_id}"}

            // Array of strings field, containing values of 'slug' columns from referenced groups
            "groups" : {
                "$array" : {
                    "from" : "groups AS g LEFT JOIN members AS m ON g.id = m.group_id"
                    "mapping" : "LOWER(g.slug)"
                    "where" : "banned = 0 AND m.user_id = ${oldId}"
                 }
             }

            // Map of counters key = group slug, value = posts count in group
            "groupPostCount" : {
                "$countMap" : {
                    "from" : "posts AS p LEFT JOIN groups AS g ON p.group_id = g.id"
                    "key" : "LOWER(g.slug)"
                    "where" : "g.slug IS NOT NULL AND p.user_id = ${oldId}"
                 }
             }

            // Array of structures
            "invites" : {
                "$array" : {
                    "from" : "group_invites AS i LEFT JOIN groups AS g ON i.group_id = g.id"
                    "mapping" : {
                        "group" : "LOWER(g.slug)",
                        "invitedBy" : {"$oidString" : "i.invited_by_id"}
                    }
                    "where" : "i.user_id = ${oldId}"
                }

            }

            // Column values converted to array. Resulting array will contain only non-null values.
            "roles": {
                "$colArray" : [
                    "CASE u.is_admin WHEN 1 THEN 'admin' ELSE NULL END",
                    "CASE u.is_staff WHEN 1 THEN 'staff' ELSE NULL END"
                ]
            }
          }
        }
      }
    }

Configuration
-------------
Migration tool takes config file as the first (and only) argument, `./jmm.conf` by default.
The tool uses [configgy](https://github.com/robey/configgy) for configuration and logging. Here is a config file example:

    mapping {
        # Mapping file (see above)
        file = "mapping.json"

        # Short url generation parameters
        short_url {
            alphabet = "fBMhDcvQd27l4ePtH0RspY3NFbXVzna6iG8oygkqULwWAmC5ZJEKjISuOTx91r"
            blockSize = 12
            minLength = 4
        }
    }

    # JDBC parameters
    jdbc {
        driver = "com.mysql.jdbc.Driver"
        uri = "jdbc:mysql://192.168.1.1:3306/prod?user=root&amp;password=mysecret&amp;characterEncoding=UTF8"

        # Batch size for selects
        limit = 5000
    }

    mongo {
        # Set to true for dry run (no actual inserts will be performed)
        dry-run = false
        host = "127.0.0.1"
        port= 27017
        database = "migrated"

        #drop database before import
        clean = true

    #    user=
    #    password=
    }

    #logging parameters, see configgy readme for details
    log {
        filename = "migration.log"
        level = "info"
        utc = false
        console = false
    }









