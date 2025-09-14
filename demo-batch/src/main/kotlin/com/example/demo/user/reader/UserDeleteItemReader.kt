package com.example.demo.user.reader

import com.example.demo.user.mapper.UserDeleteItemRowMapper
import com.example.demo.user.model.UserDeleteItem
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.PagingQueryProvider
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import javax.sql.DataSource

@Component
class UserDeleteItemReader(
	private val dataSource: DataSource
) {
	fun reader(
		chunkSize: Int,
		now: LocalDateTime
	): JdbcPagingItemReader<UserDeleteItem> =
		JdbcPagingItemReaderBuilder<UserDeleteItem>()
			.name("DeletedUsersYearAgoReader")
			.dataSource(dataSource)
			.pageSize(chunkSize)
			.fetchSize(chunkSize)
			.queryProvider(pagingQueryProvider())
			.parameterValues(mapOf("oneYearBeforeNow" to now.minusYears(1) as Any))
			.rowMapper(UserDeleteItemRowMapper())
			.build()

	private fun pagingQueryProvider(): PagingQueryProvider {
		val queryProvider = SqlPagingQueryProviderFactoryBean()
		queryProvider.setDataSource(dataSource)
		queryProvider.setSelectClause("SELECT *")
		queryProvider.setFromClause("FROM \"user\"")
		queryProvider.setWhereClause("WHERE deleted_dt <= :oneYearBeforeNow")
		queryProvider.setSortKeys(mapOf("id" to Order.ASCENDING))
		return queryProvider.`object`
	}
}
