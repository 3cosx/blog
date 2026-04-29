package cn.cosx.blog.knowledge.common.snow;

/**
 * 雪花算法ID生成器
 * 结构：1位符号位 + 41位时间戳 + 10位机器ID + 12位序列号
 */
public class SnowflakeIdGenerator {

    /**
     * 起始时间戳：2024-01-01 00:00:00
     */
    private static final long EPOCH = 1704038400000L;

    /**
     * 机器ID占用的位数
     */
    private static final long WORKER_ID_BITS = 10L;

    /**
     * 序列号占用的位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 最大机器ID：2^10 - 1 = 1023
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 机器ID左移位数
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 序列号掩码：2^12 - 1 = 4095
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 工作机器ID
     */
    private final long workerId;

    /**
     * 序列号
     */
    private long sequence = 0L;

    /**
     * 上次生成ID时的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 单例实例
     */
    private static volatile SnowflakeIdGenerator instance;

    /**
     * 构造函数
     *
     * @param workerId 工作机器ID (0 ~ 1023)
     */
    public SnowflakeIdGenerator(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("Worker ID must be between 0 and " + MAX_WORKER_ID);
        }
        this.workerId = workerId;
    }

    /**
     * 获取单例实例
     *
     * @return SnowflakeIdGenerator实例
     */
    public static SnowflakeIdGenerator getInstance() {
        if (instance == null) {
            synchronized (SnowflakeIdGenerator.class) {
                if (instance == null) {
                    instance = new SnowflakeIdGenerator(1L);
                }
            }
        }
        return instance;
    }

    /**
     * 获取单例实例（指定workerId）
     *
     * @param workerId 工作机器ID
     * @return SnowflakeIdGenerator实例
     */
    public static SnowflakeIdGenerator getInstance(long workerId) {
        if (instance == null) {
            synchronized (SnowflakeIdGenerator.class) {
                if (instance == null) {
                    instance = new SnowflakeIdGenerator(workerId);
                }
            }
        }
        return instance;
    }

    /**
     * 生成下一个ID
     *
     * @return 雪花算法生成的Long类型ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上次生成ID的时间，说明系统时钟回退
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        // 如果是同一时间生成的，则序列号自增
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 序列号溢出（超过4095），则等待到下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组装ID：时间戳部分 + 机器ID部分 + 序列号部分
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 生成下一个String类型的ID
     *
     * @return 雪花算法生成的String类型ID
     */
    public String nextIdStr() {
        return String.valueOf(nextId());
    }

    /**
     * 获取带前缀的ID字符串
     *
     * @param prefix 前缀
     * @return 带前缀的ID字符串
     */
    public String nextIdWithPrefix(String prefix) {
        return prefix + nextId();
    }

    /**
     * 阻塞到下一毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID时的时间戳
     * @return 下一毫秒的时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间戳
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
}