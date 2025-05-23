package com.type_it_backend;

import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;

public class RedisDatabaseManager {
    private final String USERNAME = "default";
    private final String PASSWORD = "77KqMNKSYyIYFQFebmAwwMbFEQxeCMJs";
    private final String HOST = "redis-14561.c92.us-east-1-3.ec2.redns.redis-cloud.com";
    private final int PORT = 14561;

    private final long EXPIRATION_TIME = (3600)*3; // 3 hours data expiration time

    private UnifiedJedis jedis;


    public RedisDatabaseManager() {
        // Setup the connection to the Redis database
        JedisClientConfig config = DefaultJedisClientConfig.builder()
                .user(USERNAME)
                .password(PASSWORD)
                .build();

        // Connect to the Redis database
        UnifiedJedis jedis = new UnifiedJedis(
            new HostAndPort(HOST, PORT),
            config
        );

        this.jedis = jedis;
    }


    public Boolean saveData(String key, String value) {
        try{
            // Save data to the Redis database
            this.jedis.setex(key, EXPIRATION_TIME ,value);
            return true;
        }
        catch (JedisException e){
            System.out.println("Error saving data to Redis: " + e.getMessage());
            
        }

        return false; // Return false if an error occurred
    }

    public String getData(String key) {
        String value = null;

        // Get data from the Redis database
        if (this.jedis.exists(key)){
            try{
                value = this.jedis.get(key);
            }
            catch (Exception e){
                System.out.println("Error getting data from Redis: " + e.getMessage());
            }
        }
        return value;
    }



    public Boolean deleteData(String key) {
        try {
            if (this.jedis.exists(key)) {
                // Delete the key from Redis
                this.jedis.del(key);
                return true;
            }
        } catch (JedisException e) {
            System.out.println("Error removing data from Redis: " + e.getMessage());
        }
        return false; // Return false if the key doesn't exist or an error occurred
    }

    public Boolean isKeyExist(String key) {
        try {
            // Check if the key exists in Redis
            return this.jedis.exists(key);
        } catch (JedisException e) {
            System.out.println("Error checking key existence in Redis: " + e.getMessage());
        }
        return false; // Return false if an error occurred
    }


    public UnifiedJedis getJedis() {
        return this.jedis; // Return the Jedis instance
    }

    public void close() {
        this.jedis.close(); // Close the Redis connection
    }

    
    
}
