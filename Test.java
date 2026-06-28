import org.springframework.data.redis.serializer.RedisSerializer; public class Test { public static void main(String[] args) { RedisSerializer<Object> s = RedisSerializer.json(); } }
