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

import com.ntc.configer.NConfig;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nghiatc
 * @since Sep 10, 2015
 */
public class RedisClient {
    private final Logger logger = LoggerFactory.getLogger(RedisClient.class);
	private static final ConcurrentHashMap<String, RedisClient> instanceMap = new ConcurrentHashMap<String, RedisClient>(16, 0.9f, 16);
	private static Lock lock = new ReentrantLock();

	private final RedissonClient redisson;

	public static RedisClient getInstance(String configName) {
		String identify = NConfig.getConfig().getString(NConfig.genKey(configName, "host"));
		RedisClient instance = instanceMap.get(identify);
		if(instance == null) {
			lock.lock();
			try {
				instance = instanceMap.get(identify);
				if(instance == null) {
					instance = new RedisClient(configName);
					instanceMap.put(identify, instance);
				}
			} finally {
				lock.unlock();
			}
		}

		return instance;
	}

	public RedisClient(String configName) {
		Config config = new Config();
		config.useSingleServer().setAddress(NConfig.getConfig().getString(NConfig.genKey(configName, "host"), "redis://127.0.0.1:6379"));
        config.useSingleServer().setClientName(configName);
        
		redisson = Redisson.create(config);
		if (redisson == null) {
			System.out.println("don't create redis session");
		}
	}

	public RedissonClient getConnect() {
		return redisson;
	}

	public void shutdown() {
		redisson.shutdown();
	}
}
