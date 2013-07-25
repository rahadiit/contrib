package pt.utl.ist.repox.accessPoint.database;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.log4j.Logger;
import pt.utl.ist.repox.RepoxConfiguration;
import pt.utl.ist.util.sql.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

public class DatabaseAccessDerby implements DatabaseAccess {
	private static final Logger log = Logger.getLogger(DatabaseAccessDerby.class);

	protected RepoxConfiguration configuration;
	protected BasicDataSource repoxDataSource;
	protected String dbUrl;

	public DatabaseAccessDerby(RepoxConfiguration configuration) {
		super();

		try {
			this.configuration = configuration;

			Properties dbConnectionProperties = new Properties();
			String url = configuration.getDatabaseUrl() + configuration.getDatabasePath();
			if(configuration.isDatabaseCreate()) {
				url += ";create=true";
			}
			dbConnectionProperties.put("url", url);
			dbConnectionProperties.put("driverClassName", configuration.getDatabaseDriverClassName());

			log.info("Database URL connection: " + url);
			repoxDataSource = (BasicDataSource) BasicDataSourceFactory.createDataSource(dbConnectionProperties);

			Class.forName(configuration.getDatabaseDriverClassName()).newInstance();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public String getVarType(Class classOfValue) {
		String valueType = "varchar(255)";

		if(classOfValue.equals(Date.class)) {
			valueType = "date";
		} else if(classOfValue.equals(Integer.class)) {
			valueType = "int";
		} else if(classOfValue.equals(Long.class)) {
			valueType = "bigint";
		} else if(classOfValue.equals(byte[].class)) {
			valueType = "blob(16M)";
		}

		return valueType;
	}


	public Connection openDbConnection() {
		try {
			return repoxDataSource.getConnection();
		}
		catch (SQLException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public void createTableIndexes(Connection con, String idType, String table, String valueType, boolean indexValue) {
		String createTableQuery = "CREATE TABLE " + table + " (id int NOT NULL GENERATED BY DEFAULT AS IDENTITY, " + "nc "
		+ idType + " NOT NULL, " + "value " + valueType + ", deleted SMALLINT, PRIMARY KEY(id))";
		log.info(createTableQuery);
		SqlUtil.runUpdate(createTableQuery, con);

		String iSystemIndexQuery = "CREATE INDEX " + table + "_i_nc ON " + table + "(nc)";
		SqlUtil.runUpdate(iSystemIndexQuery, con);

		if(indexValue) {
			String valueIndexQuery = "CREATE INDEX " + table + "_i_val ON " + table + "(value)";
			SqlUtil.runUpdate(valueIndexQuery, con);
		}
	}

	public void deleteTable(Connection con, String table) throws SQLException {
		PreparedStatement statement = con.prepareStatement("drop table " + table);
		SqlUtil.runUpdate(statement);
	}

	public String renameTableString(String oldTableName, String newTableName) {
		return "RENAME TABLE " + oldTableName + " TO " + newTableName;
	}

	public String renameIndexString(String oldIndexName, String newIndexName) {
		return "RENAME INDEX " + oldIndexName + " TO " + newIndexName;
	}
}