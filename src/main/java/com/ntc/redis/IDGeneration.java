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

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;

/**
 *
 * @author nghiatc
 * @since Sep 10, 2015
 */
public class IDGeneration {
	private final RedissonClient redisson;

	public static IDGeneration Instance = new IDGeneration();

	private IDGeneration() {
		redisson = RedisClient.getInstance("autoGeneration").getConnect();
		if(redisson == null) {
			System.out.println("Don't create connect to redis");
		}
	}

	public long generateId(String key) {
		RAtomicLong idGen = redisson.getAtomicLong(genKey(key));
		return idGen.incrementAndGet();
	}

	public long setStartId(String key, int val) {
		RAtomicLong idGen = redisson.getAtomicLong(genKey(key));
		return idGen.addAndGet(val);
	}

	public String genKey(String key) {
		return "ntc." + key;
	}
}
