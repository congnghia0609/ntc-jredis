/*
 * Copyright 2015 nghiatc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ntc.redis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.redisson.api.RAtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nghiatc
 * @since Sep 10, 2015
 */
public class RIDGen {
    private static final Logger logger = LoggerFactory.getLogger(RIDGen.class);
	private RedisClient rediscli;
    private static Map<String, RIDGen> mapRIDGens = new ConcurrentHashMap<>();
    private static Lock lock = new ReentrantLock();

	public static RIDGen getInstance() {
		return getInstance("ridgen");
	}
    
    public static RIDGen getInstance(String configName) {
        if(configName == null || configName.isEmpty()){
            return null;
        }
        RIDGen instance = mapRIDGens.containsKey(configName) ? mapRIDGens.get(configName) : null;
		if(instance == null) {
			lock.lock();
			try {
                instance = mapRIDGens.containsKey(configName) ? mapRIDGens.get(configName) : null;
				if(instance == null) {
					instance = new RIDGen(configName);
                    mapRIDGens.put(configName, instance);
				} else {
                    if (!instance.isOpen()) {
                        instance.close();
                        instance = new RIDGen(configName);
                        mapRIDGens.put(configName, instance);
                    }
                }
			} finally {
				lock.unlock();
			}
		} else {
            if (!instance.isOpen()) {
                lock.lock();
                try {
                    instance.close();
                    instance = new RIDGen(configName);
                    mapRIDGens.put(configName, instance);
                } finally {
                    lock.unlock();
                }
            }
        }
		return instance;
	}

	private RIDGen() {
		rediscli = RedisClient.getInstance("ridgen");
		if(rediscli == null) {
			System.out.println("Don't create connect to redis");
		}
	}
    
    private RIDGen(String configName) {
        if (configName == null || configName.isEmpty()) {
            throw new ExceptionInInitializerError("configName much not empty...");
        }
		rediscli = RedisClient.getInstance(configName);
		if(rediscli == null) {
			System.out.println("Don't create connect to redis");
		}
	}
    
    public boolean isOpen() {
        return !rediscli.isShuttingDown();
    }
    
    public void close() {
        rediscli.shutdown();
        rediscli = null;
    }

	public long generateId(String key) {
		RAtomicLong idGen = rediscli.getConnect().getAtomicLong(genKey(key));
		return idGen.incrementAndGet();
	}

	public long setStartId(String key, int val) {
		RAtomicLong idGen = rediscli.getConnect().getAtomicLong(genKey(key));
		return idGen.addAndGet(val);
	}

	public String genKey(String key) {
		return "ntc." + key;
	}
}
