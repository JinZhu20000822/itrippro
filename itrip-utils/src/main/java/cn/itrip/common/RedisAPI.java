package cn.itrip.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * RedisAPI
 * @author bdqn_hl
 * @date 2014-3-15
 */
public class RedisAPI {
	
	public JedisPool jedisPool;
	
	public JedisPool getJedisPool() {
		return jedisPool;
	}
	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}
	/**
	 * set key and value to redis
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean set(String key,String value){
		try{	System.out.println("key键"+key+"值"+value);
			Jedis jedis = jedisPool.getResource();
			jedis.set(key, value);
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * set key and value to redis
	 * @param key
	 * @param seconds 有效期
	 * @param value
	 * @return
	 */
	public boolean set(String key,int seconds,String value){

		try{
			Jedis jedis = jedisPool.getResource();
			jedis.setex(key, seconds, value);
			System.out.println("值"+value+"/n键"+key+"有效期"+seconds);
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 判断某个key是否存在
	 * @param key
	 * @return
	 */
	public boolean exist(String key){

		try{
			Jedis jedis = jedisPool.getResource();
			System.out.println("判断键"+key+"布尔"+ jedis.exists(key));
			return jedis.exists(key);
		}catch(Exception e){
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * 返还到连接池
	 * @param pool
	 * @param redis
	 */
	public static void returnResource(JedisPool pool,Jedis redis){
		if(redis != null){
			pool.returnResource(redis);
		}
	}
	
	/**
	 * 获取数据
	 * @param key
	 * @return
	 */
	public String get(String key){
		String value = null;
		Jedis jedis = null;
		try{
			jedis = jedisPool.getResource();
			value = jedis.get(key);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			//返还到连接池
			returnResource(jedisPool, jedis);
		}
		System.out.println("值啊"+value);
		return value;
	}
	
	/**
	 * 查询key的有效期,当 key 不存在时，返回 -2 。 当 key 存在但没有设置剩余生存时间时，返回 -1 。 否则，以秒为单位，返回 key 的剩余生存时间。
	 * 注意：在 Redis 2.8 以前，当 key 不存在，或者 key 没有设置剩余生存时间时，命令都返回 -1 。
	 * @param key
	 * @return 剩余多少秒
	 */
	public Long ttl(String key){
		try{
			Jedis jedis = jedisPool.getResource();
			return jedis.ttl(key);
		}catch(Exception e){
			e.printStackTrace();
		}
		return (long) -2;
	}
	
	/**
	 * 删除
	 * @param key
	 */
	public void delete(String key){
		try{
			Jedis jedis = jedisPool.getResource();
			jedis.del(key);
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	public static void main(String[] args){
		//连接到本地的Redis服务
		Jedis jedis = new Jedis("localhost");
		//查看服务是否在运行
		System.out.println("Server is running:"+jedis.ping());
		jedis.set("foo","bar");
		String value = jedis.get("foo");

//		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
//		RedisAPI api= (RedisAPI)ctx.getBean("redisAPI");
//		api.set("id","100");
//		boolean bool = api.exist("id");
//        String v = api.get("id");
//        long sec = api.ttl("id");
//            System.out.println("bool:"+bool+",v:"+v+",sec:"+sec);

	}

}
