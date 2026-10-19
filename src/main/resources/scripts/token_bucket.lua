-- KEYS[1] = bucket key
-- ARGV[1] = capacity
-- ARGV[2] = refill tokens
-- ARGV[3] = refill period in milliseconds

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refillTokens = tonumber(ARGV[2])
local refillPeriodMs = tonumber(ARGV[3])

if not capacity or not refillTokens or not refillPeriodMs
    or capacity < 1 or refillTokens < 1 or refillPeriodMs < 1 then
    return redis.error_reply("Invalid rate limit policy")
end

-- Use Redis server time (milliseconds).
local time = redis.call('TIME')
local nowMs = tonumber(time[1]) * 1000
    + math.floor(tonumber(time[2]) / 1000)

-- Read bucket state.
local tokens = tonumber(redis.call('HGET', key, 'tokens'))
local lastRefillMs = tonumber(redis.call('HGET', key, 'lastRefillMs'))

-- New buckets start full.
if not tokens or not lastRefillMs then
    tokens = capacity
    lastRefillMs = nowMs
end

-- Refill tokens according to elapsed time.
local elapsedMs = math.max(0, nowMs - lastRefillMs)
local refillRate = refillTokens / refillPeriodMs

tokens = math.min(capacity, tokens + elapsedMs * refillRate)
lastRefillMs = nowMs

local allowed = 0
local retryAfterSeconds = 0

-- Consume one token if available.
if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
else
    local waitMs = (1 - tokens) / refillRate
    retryAfterSeconds = math.max(1, math.ceil(waitMs / 1000))
end

-- Save the updated bucket.
redis.call('HSET', key,
    'tokens', tostring(tokens),
    'lastRefillMs', tostring(lastRefillMs)
)

-- Remove inactive buckets after enough time to refill fully.
local ttlMs = math.ceil(capacity / refillRate)
redis.call('PEXPIRE', key, math.max(1, ttlMs))

-- Return: allowed, whole tokens remaining, retry seconds.
return {
    allowed,
    math.floor(tokens),
    retryAfterSeconds
}