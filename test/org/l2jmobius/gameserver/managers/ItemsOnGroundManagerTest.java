package org.l2jmobius.gameserver.managers;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.ItemType;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;

public class ItemsOnGroundManagerTest
{
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Starting ItemsOnGroundManagerTest...");

            // 1. Initialize ThreadPool (required by Item constructor)
            ThreadPool.init();

            // 2. Prepare Mock Connection
            MockConnection con = new MockConnection();

            // 3. Prepare Items
            List<Item> items = new ArrayList<>();
            for (int i = 0; i < 2500; i++)
            {
                items.add(new DummyItem(i + 1));
            }

            // 4. Run storeItems
            System.out.println("Calling storeItems with " + items.size() + " items...");
            long start = System.nanoTime();
            ItemsOnGroundManager.storeItems(con, items);
            long end = System.nanoTime();

            System.out.println("storeItems completed in " + (end - start) / 1000000.0 + " ms.");

            // 5. Verify results
            MockPreparedStatement ps = con.lastPs;
            if (ps == null)
            {
                System.out.println("FAIL: PreparedStatement was not created.");
                return;
            }

            System.out.println("Verifying addBatch calls...");
            if (ps.addBatchCount != 2500)
            {
                System.out.println("FAIL: addBatch called " + ps.addBatchCount + " times, expected 2500.");
            }
            else
            {
                System.out.println("PASS: addBatch called correct number of times.");
            }

            System.out.println("Verifying executeBatch calls...");
            // 2500 items. Batch size 1000.
            // 1000 -> execute
            // 2000 -> execute
            // 2500 -> end of loop -> execute
            // Total 3 executions.
            if (ps.executeBatchCount != 3)
            {
                System.out.println("FAIL: executeBatch called " + ps.executeBatchCount + " times, expected 3.");
            }
            else
            {
                System.out.println("PASS: executeBatch called correct number of times.");
            }

            if (ps.executeCount != 0)
            {
                System.out.println("FAIL: execute() called " + ps.executeCount + " times, expected 0.");
            }
             else
            {
                System.out.println("PASS: execute() called 0 times.");
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            System.out.println("Shutting down ThreadPool...");
            ThreadPool.shutdown();
        }
    }

    // --- Mocks ---

    static class DummyItemTemplate extends ItemTemplate
    {
        public DummyItemTemplate(StatSet set)
        {
            super(set);
        }

        @Override
        public ItemType getItemType()
        {
            return EtcItemType.NONE;
        }

        @Override
        public int getItemMask()
        {
            return 0;
        }
    }

    static class DummyItem extends Item
    {
        public DummyItem(int objectId)
        {
            super(objectId, createTemplate());
            // Need to set minimal fields used by storeItems
            setCount(1);
            setEnchantLevel(0);
            setXYZ(100, 200, 300);
            setDropTime(System.currentTimeMillis());
            setProtected(false);
        }

        private static DummyItemTemplate createTemplate()
        {
            StatSet set = new StatSet();
            set.set("item_id", 100);
            set.set("name", "Dummy Item");
            return new DummyItemTemplate(set);
        }
    }

    static class MockConnection implements Connection
    {
        MockPreparedStatement lastPs;

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException
        {
            lastPs = new MockPreparedStatement(sql);
            return lastPs;
        }

        // Implement other methods as no-op or throw unsupported
        public void close() throws SQLException {}
        public java.sql.Statement createStatement() throws SQLException { return null; }
        public void setAutoCommit(boolean autoCommit) throws SQLException {}
        public boolean getAutoCommit() throws SQLException { return true; }
        public void commit() throws SQLException {}
        public void rollback() throws SQLException {}
        public boolean isClosed() throws SQLException { return false; }
        public java.sql.DatabaseMetaData getMetaData() throws SQLException { return null; }
        public void setReadOnly(boolean readOnly) throws SQLException {}
        public boolean isReadOnly() throws SQLException { return false; }
        public void setCatalog(String catalog) throws SQLException {}
        public String getCatalog() throws SQLException { return null; }
        public void setTransactionIsolation(int level) throws SQLException {}
        public int getTransactionIsolation() throws SQLException { return 0; }
        public java.sql.SQLWarning getWarnings() throws SQLException { return null; }
        public void clearWarnings() throws SQLException {}
        public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException { return null; }
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return null; }
        public java.util.Map<String, Class<?>> getTypeMap() throws SQLException { return null; }
        public void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException {}
        public void setHoldability(int holdability) throws SQLException {}
        public int getHoldability() throws SQLException { return 0; }
        public java.sql.Savepoint setSavepoint() throws SQLException { return null; }
        public java.sql.Savepoint setSavepoint(String name) throws SQLException { return null; }
        public void rollback(java.sql.Savepoint savepoint) throws SQLException {}
        public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException {}
        public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return null; }
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return null; }
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException { return null; }
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException { return null; }
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException { return null; }
        public java.sql.Clob createClob() throws SQLException { return null; }
        public java.sql.Blob createBlob() throws SQLException { return null; }
        public java.sql.NClob createNClob() throws SQLException { return null; }
        public java.sql.SQLXML createSQLXML() throws SQLException { return null; }
        public boolean isValid(int timeout) throws SQLException { return true; }
        public void setClientInfo(String name, String value) throws java.sql.SQLClientInfoException {}
        public void setClientInfo(Properties properties) throws java.sql.SQLClientInfoException {}
        public String getClientInfo(String name) throws SQLException { return null; }
        public Properties getClientInfo() throws SQLException { return null; }
        public java.sql.Array createArrayOf(String typeName, Object[] elements) throws SQLException { return null; }
        public java.sql.Struct createStruct(String typeName, Object[] attributes) throws SQLException { return null; }
        public void setSchema(String schema) throws SQLException {}
        public String getSchema() throws SQLException { return null; }
        public void abort(Executor executor) throws SQLException {}
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {}
        public int getNetworkTimeout() throws SQLException { return 0; }
        public <T> T unwrap(Class<T> iface) throws SQLException { return null; }
        public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
        public CallableStatement prepareCall(String sql) throws SQLException { return null; }
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return null; }
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return null; }
        public String nativeSQL(String sql) throws SQLException { return sql; }
    }

    static class MockPreparedStatement implements PreparedStatement
    {
        String sql;
        int addBatchCount = 0;
        int executeBatchCount = 0;
        int executeCount = 0;

        public MockPreparedStatement(String sql)
        {
            this.sql = sql;
        }

        @Override
        public void addBatch() throws SQLException
        {
            addBatchCount++;
        }

        @Override
        public int[] executeBatch() throws SQLException
        {
            executeBatchCount++;
            return new int[0];
        }

        @Override
        public boolean execute() throws SQLException
        {
            executeCount++;
            return true;
        }

        // Setters - no-op
        public void setInt(int parameterIndex, int x) throws SQLException {}
        public void setLong(int parameterIndex, long x) throws SQLException {}

        public void close() throws SQLException {}

        // Other required methods
        public ResultSet executeQuery() throws SQLException { return null; }
        public int executeUpdate() throws SQLException { return 0; }
        public void setNull(int parameterIndex, int sqlType) throws SQLException {}
        public void setBoolean(int parameterIndex, boolean x) throws SQLException {}
        public void setByte(int parameterIndex, byte x) throws SQLException {}
        public void setShort(int parameterIndex, short x) throws SQLException {}
        public void setFloat(int parameterIndex, float x) throws SQLException {}
        public void setDouble(int parameterIndex, double x) throws SQLException {}
        public void setBigDecimal(int parameterIndex, java.math.BigDecimal x) throws SQLException {}
        public void setString(int parameterIndex, String x) throws SQLException {}
        public void setBytes(int parameterIndex, byte[] x) throws SQLException {}
        public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {}
        public void setTime(int parameterIndex, java.sql.Time x) throws SQLException {}
        public void setTimestamp(int parameterIndex, java.sql.Timestamp x) throws SQLException {}
        public void setAsciiStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {}
        public void setUnicodeStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {}
        public void setBinaryStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {}
        public void clearParameters() throws SQLException {}
        public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {}
        public void setObject(int parameterIndex, Object x) throws SQLException {}
        public void addBatch(String sql) throws SQLException {}
        public void setCharacterStream(int parameterIndex, java.io.Reader reader, int length) throws SQLException {}
        public void setRef(int parameterIndex, java.sql.Ref x) throws SQLException {}
        public void setBlob(int parameterIndex, java.sql.Blob x) throws SQLException {}
        public void setClob(int parameterIndex, java.sql.Clob x) throws SQLException {}
        public void setArray(int parameterIndex, java.sql.Array x) throws SQLException {}
        public java.sql.ResultSetMetaData getMetaData() throws SQLException { return null; }
        public void setDate(int parameterIndex, java.sql.Date x, java.util.Calendar cal) throws SQLException {}
        public void setTime(int parameterIndex, java.sql.Time x, java.util.Calendar cal) throws SQLException {}
        public void setTimestamp(int parameterIndex, java.sql.Timestamp x, java.util.Calendar cal) throws SQLException {}
        public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {}
        public void setURL(int parameterIndex, java.net.URL x) throws SQLException {}
        public java.sql.ParameterMetaData getParameterMetaData() throws SQLException { return null; }
        public void setRowId(int parameterIndex, java.sql.RowId x) throws SQLException {}
        public void setNString(int parameterIndex, String value) throws SQLException {}
        public void setNCharacterStream(int parameterIndex, java.io.Reader value, long length) throws SQLException {}
        public void setNClob(int parameterIndex, java.sql.NClob value) throws SQLException {}
        public void setClob(int parameterIndex, java.io.Reader reader, long length) throws SQLException {}
        public void setBlob(int parameterIndex, java.io.InputStream inputStream, long length) throws SQLException {}
        public void setNClob(int parameterIndex, java.io.Reader reader, long length) throws SQLException {}
        public void setSQLXML(int parameterIndex, java.sql.SQLXML xmlObject) throws SQLException {}
        public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {}
        public void setAsciiStream(int parameterIndex, java.io.InputStream x, long length) throws SQLException {}
        public void setBinaryStream(int parameterIndex, java.io.InputStream x, long length) throws SQLException {}
        public void setCharacterStream(int parameterIndex, java.io.Reader reader, long length) throws SQLException {}
        public void setAsciiStream(int parameterIndex, java.io.InputStream x) throws SQLException {}
        public void setBinaryStream(int parameterIndex, java.io.InputStream x) throws SQLException {}
        public void setCharacterStream(int parameterIndex, java.io.Reader reader) throws SQLException {}
        public void setNCharacterStream(int parameterIndex, java.io.Reader value) throws SQLException {}
        public void setClob(int parameterIndex, java.io.Reader reader) throws SQLException {}
        public void setBlob(int parameterIndex, java.io.InputStream inputStream) throws SQLException {}
        public void setNClob(int parameterIndex, java.io.Reader reader) throws SQLException {}

        // Statement methods
        public int executeUpdate(String sql) throws SQLException { return 0; }
        public void closeOnCompletion() throws SQLException {}
        public boolean isCloseOnCompletion() throws SQLException { return false; }
        public ResultSet executeQuery(String sql) throws SQLException { return null; }
        public boolean execute(String sql) throws SQLException { return false; }
        public int getMaxFieldSize() throws SQLException { return 0; }
        public void setMaxFieldSize(int max) throws SQLException {}
        public int getMaxRows() throws SQLException { return 0; }
        public void setMaxRows(int max) throws SQLException {}
        public void setEscapeProcessing(boolean enable) throws SQLException {}
        public int getQueryTimeout() throws SQLException { return 0; }
        public void setQueryTimeout(int seconds) throws SQLException {}
        public void cancel() throws SQLException {}
        public java.sql.SQLWarning getWarnings() throws SQLException { return null; }
        public void clearWarnings() throws SQLException {}
        public void setCursorName(String name) throws SQLException {}
        public boolean getMoreResults() throws SQLException { return false; }
        public void setFetchDirection(int direction) throws SQLException {}
        public int getFetchDirection() throws SQLException { return 0; }
        public void setFetchSize(int rows) throws SQLException {}
        public int getFetchSize() throws SQLException { return 0; }
        public int getResultSetConcurrency() throws SQLException { return 0; }
        public int getResultSetType() throws SQLException { return 0; }
        public void clearBatch() throws SQLException {}
        public Connection getConnection() throws SQLException { return null; }
        public boolean getMoreResults(int current) throws SQLException { return false; }
        public ResultSet getGeneratedKeys() throws SQLException { return null; }
        public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException { return 0; }
        public int executeUpdate(String sql, int[] columnIndexes) throws SQLException { return 0; }
        public int executeUpdate(String sql, String[] columnNames) throws SQLException { return 0; }
        public boolean execute(String sql, int autoGeneratedKeys) throws SQLException { return false; }
        public boolean execute(String sql, int[] columnIndexes) throws SQLException { return false; }
        public boolean execute(String sql, String[] columnNames) throws SQLException { return false; }
        public int getResultSetHoldability() throws SQLException { return 0; }
        public boolean isClosed() throws SQLException { return false; }
        public void setPoolable(boolean poolable) throws SQLException {}
        public boolean isPoolable() throws SQLException { return false; }
        public ResultSet getResultSet() throws SQLException { return null; }
        public int getUpdateCount() throws SQLException { return 0; }
        public <T> T unwrap(Class<T> iface) throws SQLException { return null; }
        public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
    }
}
