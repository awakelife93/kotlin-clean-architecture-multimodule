package com.example.demo.user.mapper

import com.example.demo.extension.toFlexibleLocalDateTime
import com.example.demo.user.model.UserDeleteItem
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.SQLException

class UserDeleteItemRowMapper : RowMapper<UserDeleteItem> {
	@Throws(SQLException::class)
	override fun mapRow(
		resultSet: ResultSet,
		rowNumber: Int
	): UserDeleteItem {
		val user =
			UserDeleteItem.of(
				resultSet.getLong("id"),
				resultSet.getString("email"),
				resultSet.getString("name"),
				resultSet.getString("role"),
				resultSet.getString("deleted_dt").toFlexibleLocalDateTime()
			)

		return user
	}
}
