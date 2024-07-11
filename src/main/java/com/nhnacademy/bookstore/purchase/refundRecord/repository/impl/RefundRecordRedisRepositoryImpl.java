package com.nhnacademy.bookstore.purchase.refundRecord.repository.impl;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.nhnacademy.bookstore.purchase.refundRecord.dto.response.ReadRefundRecordResponse;
import com.nhnacademy.bookstore.purchase.refundRecord.exception.NotExistsRefundRecordRedis;
import com.nhnacademy.bookstore.purchase.refundRecord.repository.RefundRecordRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefundRecordRedisRepositoryImpl implements RefundRecordRedisRepository {
	private final RedisTemplate<String, Object> redisTemplate;

	@Override
	public Long create(String hashName, Long id, ReadRefundRecordResponse readRefundRecordResponse) {
		redisTemplate.opsForHash().put(hashName + ":", id.toString(), readRefundRecordResponse);
		redisTemplate.expire(hashName + ":", 1, TimeUnit.HOURS);

		return id;
	}

	@Override
	public Long update(String hashName, Long id, int quantity) {
		Object responses = redisTemplate.opsForHash()
			.get(hashName + ":", id.toString());
		if (Objects.isNull(responses)) {
			throw new NotExistsRefundRecordRedis();
		}
		ReadRefundRecordResponse response = (ReadRefundRecordResponse) responses;
		ReadRefundRecordResponse updateResponse = ReadRefundRecordResponse.builder()
			.quantity(quantity)
			.price((response.price() / response.quantity()) * quantity)
			.readBookByPurchase(response.readBookByPurchase())
			.id(response.id())
			.build();

		redisTemplate.opsForHash().put(hashName + ":", id.toString(), updateResponse);
		redisTemplate.expire(hashName + ":", 1, TimeUnit.HOURS);
		return id;
	}

	@Override
	public Long delete(String hashName, Long id) {
		redisTemplate.opsForHash().delete(hashName + ":", id.toString());
		return id;
	}

	@Override
	public void deleteAll(String hashName) {
		redisTemplate.delete(hashName + ":");
	}

	@Override
	public List<ReadRefundRecordResponse> readAll(String hashName) {
		List<Object> values = redisTemplate.opsForHash().values(hashName + ":");
		return values.stream()
			.map(ReadRefundRecordResponse.class::cast)
			.toList();
	}

	@Override
	public boolean isHit(String hashName) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(hashName + ":"));
	}

	@Override
	public boolean detailIsHit(String hashName, Long id) {
		return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(hashName + ":", id.toString()));
	}

}
