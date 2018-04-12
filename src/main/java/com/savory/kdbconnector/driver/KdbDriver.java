package com.savory.kdbconnector.driver;

import com.savory.kdbconnector.driver.c;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.StringUtils;

//2014.03.25 allow calling connection close() even if already closed, use jdk1.7 api
//           jdk1.7 specific parts are sections after //1.7
//2012.11.26 getRow(), use jdk6 api, return char[] as String to support Aqua Data Studio for lists of char vectors.
//           java.sql.timestamp now maps to kdb+timestamp; use latest http://kx.com/q/s.k
//           jdk1.6 specific parts are sections after //4
//           For compiling for earlier jdks, remove those sections
//2007.04.20 c.java sql.date/time/timestamp
//jar cf Driver.jar *.class   url(Driver:q:host:port) isql(new service resources Driver.jar)
//javac -Xbootclasspath:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/classes.jar -target 1.6 -source 1.6 KdbDriver.java
public class KdbDriver implements java.sql.Driver {
    static int V = 2, v = 0;

    static void O(String s) {
        System.out.println(s);
    }

    public int getMinorVersion() {
        return v;
    }

    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    public boolean acceptsURL(String s) {
        return s.startsWith("jdbc:q:");
    }

    public Connection connect(String s, Properties p) throws SQLException {
        return !acceptsURL(s) ? null : new co(s.substring(7), p != null ? p.get("user") : p, p != null ? p.get("password") : p);
    }

    public DriverPropertyInfo[] getPropertyInfo(String s, Properties p) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    static {
        try {
            DriverManager.registerDriver(new KdbDriver());
        }
        catch (Exception e) {
            O(e.getMessage());
        }
    }

    static int[] SQLTYPE = { 0, 16, 0, 0, -2, 5, 4, -5, 7, 8, 0, 12, 0, 0, 91, 93, 0, 0, 0, 92 };
    static String[] TYPE = { "", "boolean", "", "", "byte", "short", "int", "long", "real", "float", "char", "symbol", "", "month", "date", "timestamp", "", "minute", "second", "time" };

    static int find(String[] x, String s) {
        int i = 0;
        for (; i < x.length && !s.equals(x[i]); ) ++i;
        return i;
    }

    static int find(int[] x, int j) {
        int i = 0;
        for (; i < x.length && x[i] != j; ) ++i;
        return i;
    }

    static void q(String s) throws SQLException {
        throw new SQLException(s);
    }

    static void q() throws SQLException {
        throw new SQLFeatureNotSupportedException("nyi");
    }

    static void q(Exception e) throws SQLException {
        throw new SQLException(e.getMessage());
    }

    public class co implements Connection {
        private com.savory.kdbconnector.driver.c c;

        public co(String s, Object u, Object p) throws SQLException {
            int i = s.indexOf(":");
            try {
                c = new c(s.substring(0, i), Integer.parseInt(s.substring(i + 1)), u == null ? "" : (String) u + ":" + (String) p);
            }
            catch (Exception e) {
                q(e);
            }
        }

        public Object ex(String statement, Object[] parameters) throws SQLException {
            try {
                if(parameters.length < 0){
                    return c.k(statement);
                }
                for(int i = 0 ; i< parameters.length ; i++){
                    statement = StringUtils.replaceOnce(statement, "?", (String)parameters[i]);
                }
                return c.k(statement);
//                return 0 < c.n(p) ? c.k(s, p) : c.k(".o.ex", s.toCharArray());
            }
            catch (Exception e) {
                q(e);
                return null;
            }
        }

        public rs qx(String s) throws SQLException {
            try {
                return new rs(null, c.k(s));
            }
            catch (Exception e) {
                q(e);
                return null;
            }
        }

        public rs qx(String s, Object x) throws SQLException {
            try {
                return new rs(null, c.k(s, x));
            }
            catch (Exception e) {
                q(e);
                return null;
            }
        }

        private boolean a = true;

        public void setAutoCommit(boolean b) throws SQLException {
            a = b;
        }

        public boolean getAutoCommit() throws SQLException {
            return a;
        }

        public void rollback() throws SQLException {
        }

        public void commit() throws SQLException {
        }

        public boolean isClosed() throws SQLException {
            return c == null;
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return null;
        }

        public Statement createStatement() throws SQLException {
            return new st(this);
        }

        public PreparedStatement prepareStatement(String s) throws SQLException {
            return new ps(this, s);
        }

        public CallableStatement prepareCall(String s) throws SQLException {
            return new cs(this, s);
        }

        public String nativeSQL(String s) throws SQLException {
            return s;
        }

        private boolean b;
        private int i = TRANSACTION_SERIALIZABLE, h = rs.HOLD_CURSORS_OVER_COMMIT;

        public void setReadOnly(boolean x) throws SQLException {
            b = x;
        }

        public boolean isReadOnly() throws SQLException {
            return b;
        }

        public void setCatalog(String s) throws SQLException {
            q("cat");
        }

        public String getCatalog() throws SQLException {
            q("cat");
            return null;
        }

        public void setTransactionIsolation(int x) throws SQLException {
            i = x;
        }

        public int getTransactionIsolation() throws SQLException {
            return i;
        }

        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        public void clearWarnings() throws SQLException {
        }

        public void close() throws SQLException {
            if (isClosed()) {
                return;
            }
            try {
                c.close();
            }
            catch (IOException e) {
                q(e);
            }
            finally {
                c = null;
            }
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return new st(this);
        }

        public PreparedStatement prepareStatement(String s, int resultSetType, int resultSetConcurrency) throws SQLException {
            return new ps(this, s);
        }

        public CallableStatement prepareCall(String s, int resultSetType, int resultSetConcurrency) throws SQLException {
            return new cs(this, s);
        }

        public Map getTypeMap() throws SQLException {
            return null;
        }

        public void setTypeMap(Map map) throws SQLException {
        }

        //3
        public void setHoldability(int holdability) throws SQLException {
            h = holdability;
        }

        public int getHoldability() throws SQLException {
            return h;
        }

        public Savepoint setSavepoint() throws SQLException {
            q("sav");
            return null;
        }

        public Savepoint setSavepoint(String name) throws SQLException {
            q("sav");
            return null;
        }

        public void rollback(Savepoint savepoint) throws SQLException {
        }

        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return new st(this);
        }

        public PreparedStatement prepareStatement(String s, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return new ps(this, s);
        }

        public CallableStatement prepareCall(String s, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return new cs(this, s);
        }

        public PreparedStatement prepareStatement(String s, int autoGeneratedKeys) throws SQLException {
            return new ps(this, s);
        }

        public PreparedStatement prepareStatement(String s, int[] columnIndexes) throws SQLException {
            return new ps(this, s);
        }

        public PreparedStatement prepareStatement(String s, String[] columnNames) throws SQLException {
            return new ps(this, s);
        }

        //4
        private Properties clientInfo = new Properties();

        public Clob createClob() throws SQLException {
            q();
            return null;
        }

        public Blob createBlob() throws SQLException {
            q();
            return null;
        }

        public NClob createNClob() throws SQLException {
            q();
            return null;
        }

        public SQLXML createSQLXML() throws SQLException {
            q();
            return null;
        }

        public boolean isValid(int i) throws SQLException {
            if (i < 0) {
                q();
            }
            return c != null;
        }

        public void setClientInfo(String k, String v) throws SQLClientInfoException {
            clientInfo.setProperty(k, v);
        }

        public void setClientInfo(Properties p) throws SQLClientInfoException {
            clientInfo = p;
        }

        public String getClientInfo(String k) throws SQLException {
            return (String) clientInfo.get(k);
        }

        public Properties getClientInfo() throws SQLException {
            return clientInfo;
        }

        public Array createArrayOf(String string, Object[] os) throws SQLException {
            q();
            return null;
        }

        public Struct createStruct(String string, Object[] os) throws SQLException {
            q();
            return null;
        }

        public <T> T unwrap(Class<T> type) throws SQLException {
            q();
            return null;
        }

        public boolean isWrapperFor(Class<?> type) throws SQLException {
            q();
            return false;
        }

        //1.7
        public int getNetworkTimeout() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("nyi");
        }

        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("nyi");
        }

        public void abort(Executor executor) throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("nyi");
        }

        public void setSchema(String s) {
        }

        public String getSchema() {
            return null;
        }
    }

    public class st implements Statement {
        private co co;
        private Object r;
        private int R, T;
        protected Object[] p = {};

        public st(co x) {
            co = x;
        }

        public int executeUpdate(String s) throws SQLException {
            co.ex(s, p);
            return -1;
        }

        public ResultSet executeQuery(String s) throws SQLException {
            return new rs(this, co.ex(s, p));
        }

        public boolean execute(String s) throws SQLException {
            return null != (r = co.ex(s, p));
        }

        public ResultSet getResultSet() throws SQLException {
            return new rs(this, r);
        }

        public int getUpdateCount() {
            return -1;
        }

        public int getMaxRows() throws SQLException {
            return R;
        }

        public void setMaxRows(int i) throws SQLException {
            R = i;
        }

        public int getQueryTimeout() throws SQLException {
            return T;
        }

        public void setQueryTimeout(int i) throws SQLException {
            T = i;
        }

        // truncate excess BINARY,VARBINARY,LONGVARBINARY,CHAR,VARCHAR,and LONGVARCHAR fields
        public int getMaxFieldSize() throws SQLException {
            return 0;
        }

        public void setMaxFieldSize(int i) throws SQLException {
        }

        public void setEscapeProcessing(boolean b) throws SQLException {
        }

        public void cancel() throws SQLException {
        }

        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        public void clearWarnings() throws SQLException {
        }

        // positioned update? different statement?
        public void setCursorName(String name) throws SQLException {
            q("cur");
        }

        public boolean getMoreResults() throws SQLException {
            return false;
        }

        public void close() throws SQLException {
            co = null;
        }

        public void setFetchDirection(int direction) throws SQLException {
            q("fd");
        }

        public int getFetchDirection() throws SQLException {
            return 0;
        }

        public void setFetchSize(int rows) throws SQLException {
        }

        public int getFetchSize() throws SQLException {
            return 0;
        }

        public int getResultSetConcurrency() throws SQLException {
            return rs.CONCUR_READ_ONLY;
        }

        public int getResultSetType() throws SQLException {
            return rs.TYPE_SCROLL_INSENSITIVE;
        }

        public void addBatch(String sql) throws SQLException {
            q("bat");
        }

        public void clearBatch() throws SQLException {
        }

        public int[] executeBatch() throws SQLException {
            return new int[0];
        }

        public Connection getConnection() throws SQLException {
            return co;
        }

        //3
        public boolean getMoreResults(int current) throws SQLException {
            return false;
        }

        public ResultSet getGeneratedKeys() throws SQLException {
            return null;
        }

        public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
            q("a");
            return 0;
        }

        public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
            q("a");
            return 0;
        }

        public int executeUpdate(String sql, String[] columnNames) throws SQLException {
            q("a");
            return 0;
        }

        public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
            q("a");
            return false;
        }

        public boolean execute(String sql, int[] columnIndexes) throws SQLException {
            q("a");
            return false;
        }

        public boolean execute(String sql, String[] columnNames) throws SQLException {
            q("a");
            return false;
        }

        public int getResultSetHoldability() throws SQLException {
            return rs.HOLD_CURSORS_OVER_COMMIT;
        }

        //4
        boolean poolable = false;

        public boolean isClosed() throws SQLException {
            return co == null || co.isClosed();
        }

        public void setPoolable(boolean b) throws SQLException {
            if (isClosed()) {
                throw new SQLException("Closed");
            }
            poolable = b;
        }

        public boolean isPoolable() throws SQLException {
            if (isClosed()) {
                throw new SQLException("Closed");
            }
            return poolable;
        }

        public <T> T unwrap(Class<T> type) throws SQLException {
            q();
            return null;
        }

        public boolean isWrapperFor(Class<?> type) throws SQLException {
            q();
            return false;
        }

        //1.7
        boolean _closeOnCompletion = false;

        public void closeOnCompletion() {
            _closeOnCompletion = true;
        }

        public boolean isCloseOnCompletion() {
            return _closeOnCompletion;
        }
    }

    public class ps extends st implements PreparedStatement {
        private String s;

        public ps(co co, String x) {
            super(co);
            s = x;
        }

        public ResultSet executeQuery() throws SQLException {
            System.out.println("heck");
            return executeQuery(s);
        }

        public int executeUpdate() throws SQLException {
            return executeUpdate(s);
        }

        public boolean execute() throws SQLException {
            return execute(s);
        }

        public void clearParameters() throws SQLException {
            try {
                for (int i = 0; i < c.n(p); ) p[i++] = null;
            }
            catch (UnsupportedEncodingException ex) {
                throw new SQLException(ex);
            }
        }

        public void setObject(int i, Object x) throws SQLException {
            int n;
            try {
                n = c.n(p);
            }
            catch (UnsupportedEncodingException ex) {
                throw new SQLException(ex);
            }
            if (i > n) {
                Object[] r = new Object[i];
                System.arraycopy(p, 0, r, 0, n);
                p = r;
                for (; n < i; ) p[n++] = null;
            }
            p[i - 1] = x;
        }

        public void setObject(int i, Object x, int targetSqlType) throws SQLException {
            setObject(i, x);
        }

        public void setObject(int i, Object x, int targetSqlType, int scale) throws SQLException {
            setObject(i, x);
        }

        public void setNull(int i, int t) throws SQLException {
            setObject(i, c.NULL[find(SQLTYPE, t)]);
        }

        public void setBoolean(int i, boolean x) throws SQLException {
            setObject(i, new Boolean(x));
        }

        public void setByte(int i, byte x) throws SQLException {
            setObject(i, new Byte(x));
        }

        public void setShort(int i, short x) throws SQLException {
            setObject(i, new Short(x));
        }

        public void setInt(int i, int x) throws SQLException {
            setObject(i, new Integer(x));
        }

        public void setLong(int i, long x) throws SQLException {
            setObject(i, new Long(x));
        }

        public void setFloat(int i, float x) throws SQLException {
            setObject(i, new Float(x));
        }

        public void setDouble(int i, double x) throws SQLException {
            setObject(i, new Double(x));
        }

        public void setString(int i, String x) throws SQLException {
            setObject(i, x);
        }

        public void setDate(int i, Date x) throws SQLException {
            setObject(i, x);
        }

        public void setTime(int i, Time x) throws SQLException {
            setObject(i, x);
        }

        public void setTimestamp(int i, Timestamp x) throws SQLException {
            setObject(i, x);
        }

        public void setBytes(int i, byte x[]) throws SQLException {
            setObject(i, new String(x));
        } // FIXME with proper kdb type (be wary of encoding)

        public void setBigDecimal(int i, BigDecimal x) throws SQLException {
            q();
        }

        public void setAsciiStream(int i, InputStream x, int length) throws SQLException {
            q();
        }

        public void setUnicodeStream(int i, InputStream x, int length) throws SQLException {
            q();
        }

        public void setBinaryStream(int i, InputStream x, int length) throws SQLException {
            q();
        }

        public void addBatch() throws SQLException {
        }

        public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
            q();
        }

        public void setRef(int i, Ref x) throws SQLException {
            q();
        }

        public void setBlob(int i, Blob x) throws SQLException {
            q();
        }

        public void setClob(int i, Clob x) throws SQLException {
            q();
        }

        public void setArray(int i, Array x) throws SQLException {
            q();
        }

        public ResultSetMetaData getMetaData() throws SQLException {
            q("m");
            return null;
        }

        public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
            q();
        }

        public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
            q();
        }

        public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
            q();
        }

        public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
            q();
        }

        //3
        public void setURL(int parameterIndex, URL x) throws SQLException {
            q();
        }

        public ParameterMetaData getParameterMetaData() throws SQLException {
            q("m");
            return null;
        }

        //4
        public void setRowId(int i, RowId rowid) throws SQLException {
            q();
        }

        public void setNString(int i, String string) throws SQLException {
            q();
        }

        public void setNCharacterStream(int i, Reader reader, long l) throws SQLException {
            q();
        }

        public void setNClob(int i, NClob nclob) throws SQLException {
            q();
        }

        public void setClob(int i, Reader reader, long l) throws SQLException {
            q();
        }

        public void setBlob(int i, InputStream in, long l) throws SQLException {
            q();
        }

        public void setNClob(int i, Reader reader, long l) throws SQLException {
            q();
        }

        public void setSQLXML(int i, SQLXML sqlxml) throws SQLException {
            q();
        }

        public void setAsciiStream(int i, InputStream in, long l) throws SQLException {
            q();
        }

        public void setBinaryStream(int i, InputStream in, long l) throws SQLException {
            q();
        }

        public void setCharacterStream(int i, Reader reader, long l) throws SQLException {
            q();
        }

        public void setAsciiStream(int i, InputStream in) throws SQLException {
            q();
        }

        public void setBinaryStream(int i, InputStream in) throws SQLException {
            q();
        }

        public void setCharacterStream(int i, Reader reader) throws SQLException {
            q();
        }

        public void setNCharacterStream(int i, Reader reader) throws SQLException {
            q();
        }

        public void setClob(int i, Reader reader) throws SQLException {
            q();
        }

        public void setBlob(int i, InputStream in) throws SQLException {
            q();
        }

        public void setNClob(int i, Reader reader) throws SQLException {
            q();
        }
    }

    public class cs extends ps implements CallableStatement {
        public cs(co c, String s) {
            super(c, s);
        }

        public void registerOutParameter(int i, int sqlType) throws SQLException {
        }

        public void registerOutParameter(int i, int sqlType, int scale) throws SQLException {
        }

        public boolean wasNull() throws SQLException {
            return false;
        }

        public String getString(int i) throws SQLException {
            return null;
        }

        public boolean getBoolean(int i) throws SQLException {
            return false;
        }

        public byte getByte(int i) throws SQLException {
            return 0;
        }

        public short getShort(int i) throws SQLException {
            return 0;
        }

        public int getInt(int i) throws SQLException {
            return 0;
        }

        public long getLong(int i) throws SQLException {
            return 0;
        }

        public float getFloat(int i) throws SQLException {
            return (float) 0.0;
        }

        public double getDouble(int i) throws SQLException {
            return 0.0;
        }

        public BigDecimal getBigDecimal(int i, int scale) throws SQLException {
            return null;
        }

        public Date getDate(int i) throws SQLException {
            return null;
        }

        public Time getTime(int i) throws SQLException {
            return null;
        }

        public Timestamp getTimestamp(int i) throws SQLException {
            return null;
        }

        public byte[] getBytes(int i) throws SQLException {
            return null;
        }

        public Object getObject(int i) throws SQLException {
            return null;
        }

        public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
            q();
            return null;
        }

        public Object getObject(int i, Map map) throws SQLException {
            q();
            return null;
        }

        public Ref getRef(int i) throws SQLException {
            q();
            return null;
        }

        public Blob getBlob(int i) throws SQLException {
            q();
            return null;
        }

        public Clob getClob(int i) throws SQLException {
            q();
            return null;
        }

        public Array getArray(int i) throws SQLException {
            q();
            return null;
        }

        public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
            q();
            return null;
        }

        public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
            q();
            return null;
        }

        public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
            q();
            return null;
        }

        public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException {
            q();
        }

        //3
        public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
            q();
        }

        public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
            q();
        }

        public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
            q();
        }

        public URL getURL(int parameterIndex) throws SQLException {
            q();
            return null;
        }

        public void setURL(String parameterName, URL val) throws SQLException {
            q();
        }

        public void setNull(String parameterName, int sqlType) throws SQLException {
            q();
        }

        public void setBoolean(String parameterName, boolean x) throws SQLException {
            q();
        }

        public void setByte(String parameterName, byte x) throws SQLException {
            q();
        }

        public void setShort(String parameterName, short x) throws SQLException {
            q();
        }

        public void setInt(String parameterName, int x) throws SQLException {
            q();
        }

        public void setLong(String parameterName, long x) throws SQLException {
            q();
        }

        public void setFloat(String parameterName, float x) throws SQLException {
            q();
        }

        public void setDouble(String parameterName, double x) throws SQLException {
            q();
        }

        public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
            q();
        }

        public void setString(String parameterName, String x) throws SQLException {
            q();
        }

        public void setBytes(String parameterName, byte[] x) throws SQLException {
            q();
        }

        public void setDate(String parameterName, Date x) throws SQLException {
            q();
        }

        public void setTime(String parameterName, Time x) throws SQLException {
            q();
        }

        public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
            q();
        }

        public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
            q();
        }

        public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
            q();
        }

        public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
            q();
        }

        public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
            q();
        }

        public void setObject(String parameterName, Object x) throws SQLException {
            q();
        }

        public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
            q();
        }

        public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
            q();
        }

        public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
            q();
        }

        public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
            q();
        }

        public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
            q();
        }

        public String getString(String parameterName) throws SQLException {
            return null;
        }

        public boolean getBoolean(String parameterName) throws SQLException {
            return false;
        }

        public byte getByte(String parameterName) throws SQLException {
            return 0;
        }

        public short getShort(String parameterName) throws SQLException {
            return 0;
        }

        public int getInt(String parameterName) throws SQLException {
            return 0;
        }

        public long getLong(String parameterName) throws SQLException {
            return 0;
        }

        public float getFloat(String parameterName) throws SQLException {
            return 0;
        }

        public double getDouble(String parameterName) throws SQLException {
            return 0;
        }

        public byte[] getBytes(String parameterName) throws SQLException {
            return null;
        }

        public Date getDate(String parameterName) throws SQLException {
            return null;
        }

        public Time getTime(String parameterName) throws SQLException {
            return null;
        }

        public Timestamp getTimestamp(String parameterName) throws SQLException {
            return null;
        }

        public Object getObject(String parameterName) throws SQLException {
            return null;
        }

        public BigDecimal getBigDecimal(String parameterName) throws SQLException {
            return null;
        }

        public Object getObject(String parameterName, Map map) throws SQLException {
            return null;
        }

        public Ref getRef(String parameterName) throws SQLException {
            return null;
        }

        public Blob getBlob(String parameterName) throws SQLException {
            return null;
        }

        public Clob getClob(String parameterName) throws SQLException {
            return null;
        }

        public Array getArray(String parameterName) throws SQLException {
            return null;
        }

        public Date getDate(String parameterName, Calendar cal) throws SQLException {
            return null;
        }

        public Time getTime(String parameterName, Calendar cal) throws SQLException {
            return null;
        }

        public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
            return null;
        }

        public URL getURL(String parameterName) throws SQLException {
            return null;
        }

        //4
        public RowId getRowId(int i) throws SQLException {
            q();
            return null;
        }

        public RowId getRowId(String string) throws SQLException {
            q();
            return null;
        }

        public void setRowId(String string, RowId rowid) throws SQLException {
            q();
        }

        public void setNString(String string, String string1) throws SQLException {
            q();
        }

        public void setNCharacterStream(String string, Reader reader, long l) throws SQLException {
            q();
        }

        public void setNClob(String string, NClob nclob) throws SQLException {
            q();
        }

        public void setClob(String string, Reader reader, long l) throws SQLException {
            q();
        }

        public void setBlob(String string, InputStream in, long l) throws SQLException {
            q();
        }

        public void setNClob(String string, Reader reader, long l) throws SQLException {
            q();
        }

        public NClob getNClob(int i) throws SQLException {
            q();
            return null;
        }

        public NClob getNClob(String string) throws SQLException {
            q();
            return null;
        }

        public void setSQLXML(String string, SQLXML sqlxml) throws SQLException {
            q();
        }

        public SQLXML getSQLXML(int i) throws SQLException {
            q();
            return null;
        }

        public SQLXML getSQLXML(String string) throws SQLException {
            q();
            return null;
        }

        public String getNString(int i) throws SQLException {
            q();
            return null;
        }

        public String getNString(String string) throws SQLException {
            q();
            return null;
        }

        public Reader getNCharacterStream(int i) throws SQLException {
            q();
            return null;
        }

        public Reader getNCharacterStream(String string) throws SQLException {
            q();
            return null;
        }

        public Reader getCharacterStream(int i) throws SQLException {
            q();
            return null;
        }

        public Reader getCharacterStream(String string) throws SQLException {
            q();
            return null;
        }

        public void setBlob(String string, Blob blob) throws SQLException {
            q();
        }

        public void setClob(String string, Clob clob) throws SQLException {
            q();
        }

        public void setAsciiStream(String string, InputStream in, long l) throws SQLException {
            q();
        }

        public void setBinaryStream(String string, InputStream in, long l) throws SQLException {
            q();
        }

        public void setCharacterStream(String string, Reader reader, long l) throws SQLException {
            q();
        }

        public void setAsciiStream(String string, InputStream in) throws SQLException {
            q();
        }

        public void setBinaryStream(String string, InputStream in) throws SQLException {
            q();
        }

        public void setCharacterStream(String string, Reader reader) throws SQLException {
            q();
        }

        public void setNCharacterStream(String string, Reader reader) throws SQLException {
            q();
        }

        public void setClob(String string, Reader reader) throws SQLException {
            q();
        }

        public void setBlob(String string, InputStream in) throws SQLException {
            q();
        }

        public void setNClob(String string, Reader reader) throws SQLException {
            q();
        }

        //1.7
        public <T> T getObject(String s, Class<T> t) throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("nyi");
        }

        public <T> T getObject(int parameterIndex, Class<T> t) throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("nyi");
        }
    }

    public class rs implements ResultSet {
        private st st;
        private String[] f;
        private Object o, d[];
        private int r, n;

        public rs(st s, Object x) throws SQLException {
            st = s;
            c.Flip a;
            try {
                a = c.td(x);
                f = a.x;
                d = a.y;
                n = c.n(d[0]);
                r = -1;
            }
            catch (UnsupportedEncodingException ex) {
                throw new SQLException(ex);
            }
        }

        public ResultSetMetaData getMetaData() throws SQLException {
            return new rm(f, d);
        }

        public int findColumn(String s) throws SQLException {
            return 1 + find(f, s);
        }

        public boolean next() throws SQLException {
            return ++r < n;
        }

        public boolean wasNull() throws SQLException {
            return o == null;
        }

        public Object getObject(int i) throws SQLException {
            o = c.at(d[i - 1], r);
            return o instanceof char[] ? new String((char[]) o) : o;
        }

        public boolean getBoolean(int i) throws SQLException {
            return ((Boolean) getObject(i)).booleanValue();
        }

        public byte getByte(int i) throws SQLException {
            return ((Byte) getObject(i)).byteValue();
        }

        public short getShort(int i) throws SQLException {
            Object x = getObject(i);
            return x == null ? 0 : ((Short) x).shortValue();
        }

        public int getInt(int i) throws SQLException {
            Object x = getObject(i);
            return x == null ? 0 : ((Long) x).intValue();
        }

        public long getLong(int i) throws SQLException {
            Object x = getObject(i);
            return x == null ? 0 : ((Long) x).longValue();
        }

        public float getFloat(int i) throws SQLException {
            Object x = getObject(i);
            return x == null ? 0 : ((Float) x).floatValue();
        }

        public double getDouble(int i) throws SQLException {
            Object x = getObject(i);
            return x == null ? 0 : ((Double) x).doubleValue();
        }

        public String getString(int i) throws SQLException {
            Object x = getObject(i);
            return x == null ? null : x.toString();
        }

        public Date getDate(int i) throws SQLException {
            return (Date) getObject(i);
        }

        public Time getTime(int i) throws SQLException {
            return (Time) getObject(i);
        }

        public Timestamp getTimestamp(int i) throws SQLException {
            return (Timestamp) getObject(i);
        }

        public byte[] getBytes(int i) throws SQLException {
            q();
            return null;
        }

        public BigDecimal getBigDecimal(int i, int scale) throws SQLException {
            q();
            return null;
        }

        public InputStream getAsciiStream(int i) throws SQLException {
            q();
            return null;
        }

        public InputStream getUnicodeStream(int i) throws SQLException {
            q();
            return null;
        }

        public InputStream getBinaryStream(int i) throws SQLException {
            q();
            return null;
        }

        public Object getObject(String s) throws SQLException {
            return getObject(findColumn(s));
        }

        public boolean getBoolean(String s) throws SQLException {
            return getBoolean(findColumn(s));
        }

        public byte getByte(String s) throws SQLException {
            return getByte(findColumn(s));
        }

        public short getShort(String s) throws SQLException {
            return getShort(findColumn(s));
        }

        public int getInt(String s) throws SQLException {
            return getInt(findColumn(s));
        }

        public long getLong(String s) throws SQLException {
            return getLong(findColumn(s));
        }

        public float getFloat(String s) throws SQLException {
            return getFloat(findColumn(s));
        }

        public double getDouble(String s) throws SQLException {
            return getDouble(findColumn(s));
        }

        public String getString(String s) throws SQLException {
            return getString(findColumn(s));
        }

        public Date getDate(String s) throws SQLException {
            return getDate(findColumn(s));
        }

        public Time getTime(String s) throws SQLException {
            return getTime(findColumn(s));
        }

        public Timestamp getTimestamp(String s) throws SQLException {
            return getTimestamp(findColumn(s));
        }

        public byte[] getBytes(String s) throws SQLException {
            return getBytes(findColumn(s));
        }

        public BigDecimal getBigDecimal(String s, int scale) throws SQLException {
            return getBigDecimal(findColumn(s), scale);
        }

        public InputStream getAsciiStream(String s) throws SQLException {
            return getAsciiStream(findColumn(s));
        }

        public InputStream getUnicodeStream(String s) throws SQLException {
            return getUnicodeStream(findColumn(s));
        }

        public InputStream getBinaryStream(String s) throws SQLException {
            return getBinaryStream(findColumn(s));
        }

        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        public void clearWarnings() throws SQLException {
        }

        public String getCursorName() throws SQLException {
            q("cur");
            return "";
        }

        public void close() throws SQLException {
            d = null;
        }

        public Reader getCharacterStream(int columnIndex) throws SQLException {
            q();
            return null;
        }

        public Reader getCharacterStream(String columnName) throws SQLException {
            q();
            return null;
        }

        public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
            q();
            return null;
        }

        public BigDecimal getBigDecimal(String columnName) throws SQLException {
            q();
            return null;
        }

        public boolean isBeforeFirst() throws SQLException {
            return r < 0;
        }

        public boolean isAfterLast() throws SQLException {
            return r >= n;
        }

        public boolean isFirst() throws SQLException {
            return r == 0;
        }

        public boolean isLast() throws SQLException {
            return r == n - 1;
        }

        public void beforeFirst() throws SQLException {
            r = -1;
        }

        public void afterLast() throws SQLException {
            r = n;
        }

        public boolean first() throws SQLException {
            r = 0;
            return n > 0;
        }

        public boolean last() throws SQLException {
            r = n - 1;
            return n > 0;
        }

        public int getRow() throws SQLException {
            return r + 1;
        }

        public boolean absolute(int row) throws SQLException {
            r = row - 1;
            return r < n;
        }

        public boolean relative(int rows) throws SQLException {
            r += rows;
            return r >= 0 && r < n;
        }

        public boolean previous() throws SQLException {
            --r;
            return r >= 0;
        }

        public void setFetchDirection(int direction) throws SQLException {
            q("fd");
        }

        public int getFetchDirection() throws SQLException {
            return FETCH_FORWARD;
        }

        public void setFetchSize(int rows) throws SQLException {
        }

        public int getFetchSize() throws SQLException {
            return 0;
        }

        public int getType() throws SQLException {
            return TYPE_SCROLL_SENSITIVE;
        }

        public int getConcurrency() throws SQLException {
            return CONCUR_READ_ONLY;
        }

        public boolean rowUpdated() throws SQLException {
            q();
            return false;
        }

        public boolean rowInserted() throws SQLException {
            q();
            return false;
        }

        public boolean rowDeleted() throws SQLException {
            q();
            return false;
        }

        public void updateNull(int columnIndex) throws SQLException {
            q();
        }

        public void updateBoolean(int columnIndex, boolean x) throws SQLException {
            q();
        }

        public void updateByte(int columnIndex, byte x) throws SQLException {
            q();
        }

        public void updateShort(int columnIndex, short x) throws SQLException {
            q();
        }

        public void updateInt(int columnIndex, int x) throws SQLException {
            q();
        }

        public void updateLong(int columnIndex, long x) throws SQLException {
            q();
        }

        public void updateFloat(int columnIndex, float x) throws SQLException {
            q();
        }

        public void updateDouble(int columnIndex, double x) throws SQLException {
            q();
        }

        public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
            q();
        }

        public void updateString(int columnIndex, String x) throws SQLException {
            q();
        }

        public void updateBytes(int columnIndex, byte[] x) throws SQLException {
            q();
        }

        public void updateDate(int columnIndex, Date x) throws SQLException {
            q();
        }

        public void updateTime(int columnIndex, Time x) throws SQLException {
            q();
        }

        public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
            q();
        }

        public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
            q();
        }

        public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
            q();
        }

        public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
            q();
        }

        public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
            q();
        }

        public void updateObject(int columnIndex, Object x) throws SQLException {
            q();
        }

        public void updateNull(String columnName) throws SQLException {
            q();
        }

        public void updateBoolean(String columnName, boolean x) throws SQLException {
            q();
        }

        public void updateByte(String columnName, byte x) throws SQLException {
            q();
        }

        public void updateShort(String columnName, short x) throws SQLException {
            q();
        }

        public void updateInt(String columnName, int x) throws SQLException {
            q();
        }

        public void updateLong(String columnName, long x) throws SQLException {
            q();
        }

        public void updateFloat(String columnName, float x) throws SQLException {
            q();
        }

        public void updateDouble(String columnName, double x) throws SQLException {
            q();
        }

        public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
            q();
        }

        public void updateString(String columnName, String x) throws SQLException {
            q();
        }

        public void updateBytes(String columnName, byte[] x) throws SQLException {
            q();
        }

        public void updateDate(String columnName, Date x) throws SQLException {
            q();
        }

        public void updateTime(String columnName, Time x) throws SQLException {
            q();
        }

        public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
            q();
        }

        public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
            q();
        }

        public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
            q();
        }

        public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
            q();
        }

        public void updateObject(String columnName, Object x, int scale) throws SQLException {
            q();
        }

        public void updateObject(String columnName, Object x) throws SQLException {
            q();
        }

        public void insertRow() throws SQLException {
            q();
        }

        public void updateRow() throws SQLException {
            q();
        }

        public void deleteRow() throws SQLException {
            q();
        }

        public void refreshRow() throws SQLException {
            q();
        }

        public void cancelRowUpdates() throws SQLException {
            q();
        }

        public void moveToInsertRow() throws SQLException {
            q();
        }

        public void moveToCurrentRow() throws SQLException {
            q();
        }

        public Statement getStatement() throws SQLException {
            return st;
        }

        public Object getObject(int i, Map map) throws SQLException {
            q();
            return null;
        }

        public Ref getRef(int i) throws SQLException {
            q();
            return null;
        }

        public Blob getBlob(int i) throws SQLException {
            q();
            return null;
        }

        public Clob getClob(int i) throws SQLException {
            q();
            return null;
        }

        public Array getArray(int i) throws SQLException {
            q();
            return null;
        }

        public Object getObject(String colName, Map map) throws SQLException {
            q();
            return null;
        }

        public Ref getRef(String colName) throws SQLException {
            q();
            return null;
        }

        public Blob getBlob(String colName) throws SQLException {
            q();
            return null;
        }

        public Clob getClob(String colName) throws SQLException {
            q();
            return null;
        }

        public Array getArray(String colName) throws SQLException {
            q();
            return null;
        }

        public Date getDate(int columnIndex, Calendar cal) throws SQLException {
            q();
            return null;
        }

        public Date getDate(String columnName, Calendar cal) throws SQLException {
            q();
            return null;
        }

        public Time getTime(int columnIndex, Calendar cal) throws SQLException {
            q();
            return null;
        }

        public Time getTime(String columnName, Calendar cal) throws SQLException {
            q();
            return null;
        }

        public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
            q();
            return null;
        }

        public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
            q();
            return null;
        }

        //3
        public URL getURL(int columnIndex) throws SQLException {
            q();
            return null;
        }

        public URL getURL(String columnName) throws SQLException {
            q();
            return null;
        }

        public void updateRef(int columnIndex, Ref x) throws SQLException {
            q();
        }

        public void updateRef(String columnName, Ref x) throws SQLException {
            q();
        }

        public void updateBlob(int columnIndex, Blob x) throws SQLException {
            q();
        }

        public void updateBlob(String columnName, Blob x) throws SQLException {
            q();
        }

        public void updateClob(int columnIndex, Clob x) throws SQLException {
            q();
        }

        public void updateClob(String columnName, Clob x) throws SQLException {
            q();
        }

        public void updateArray(int columnIndex, Array x) throws SQLException {
            q();
        }

        public void updateArray(String columnName, Array x) throws SQLException {
            q();
        }

        //4
        public RowId getRowId(int i) throws SQLException {
            q();
            return null;
        }

        public RowId getRowId(String string) throws SQLException {
            q();
            return null;
        }

        public void updateRowId(int i, RowId rowid) throws SQLException {
            q();
        }

        public void updateRowId(String string, RowId rowid) throws SQLException {
            q();
        }

        public int getHoldability() throws SQLException {
            q();
            return 0;
        }

        public boolean isClosed() throws SQLException {
            return d == null;
        }

        public void updateNString(int i, String string) throws SQLException {
            q();
        }

        public void updateNString(String string, String string1) throws SQLException {
            q();
        }

        public void updateNClob(int i, NClob nclob) throws SQLException {
            q();
        }

        public void updateNClob(String string, NClob nclob) throws SQLException {
            q();
        }

        public NClob getNClob(int i) throws SQLException {
            q();
            return null;
        }

        public NClob getNClob(String string) throws SQLException {
            q();
            return null;
        }

        public SQLXML getSQLXML(int i) throws SQLException {
            q();
            return null;
        }

        public SQLXML getSQLXML(String string) throws SQLException {
            q();
            return null;
        }

        public void updateSQLXML(int i, SQLXML sqlxml) throws SQLException {
            q();
        }

        public void updateSQLXML(String string, SQLXML sqlxml) throws SQLException {
            q();
        }

        public String getNString(int i) throws SQLException {
            q();
            return null;
        }

        public String getNString(String string) throws SQLException {
            q();
            return null;
        }

        public Reader getNCharacterStream(int i) throws SQLException {
            q();
            return null;
        }

        public Reader getNCharacterStream(String string) throws SQLException {
            q();
            return null;
        }

        public void updateNCharacterStream(int i, Reader reader, long l) throws SQLException {
            q();
        }

        public void updateNCharacterStream(String string, Reader reader, long l) throws SQLException {
            q();
        }

        public void updateAsciiStream(int i, InputStream in, long l) throws SQLException {
            q();
        }

        public void updateBinaryStream(int i, InputStream in, long l) throws SQLException {
            q();
        }

        public void updateCharacterStream(int i, Reader reader, long l) throws SQLException {
            q();
        }

        public void updateAsciiStream(String string, InputStream in, long l) throws SQLException {
            q();
        }

        public void updateBinaryStream(String string, InputStream in, long l) throws SQLException {
            q();
        }

        public void updateCharacterStream(String string, Reader reader, long l) throws SQLException {
            q();
        }

        public void updateBlob(int i, InputStream in, long l) throws SQLException {
            q();
        }

        public void updateBlob(String string, InputStream in, long l) throws SQLException {
            q();
        }

        public void updateClob(int i, Reader reader, long l) throws SQLException {
            q();
        }

        public void updateClob(String string, Reader reader, long l) throws SQLException {
            q();
        }

        public void updateNClob(int i, Reader reader, long l) throws SQLException {
            q();
        }

        public void updateNClob(String string, Reader reader, long l) throws SQLException {
            q();
        }

        public void updateNCharacterStream(int i, Reader reader) throws SQLException {
            q();
        }

        public void updateNCharacterStream(String string, Reader reader) throws SQLException {
            q();
        }

        public void updateAsciiStream(int i, InputStream in) throws SQLException {
            q();
        }

        public void updateBinaryStream(int i, InputStream in) throws SQLException {
            q();
        }

        public void updateCharacterStream(int i, Reader reader) throws SQLException {
            q();
        }

        public void updateAsciiStream(String string, InputStream in) throws SQLException {
            q();
        }

        public void updateBinaryStream(String string, InputStream in) throws SQLException {
            q();
        }

        public void updateCharacterStream(String string, Reader reader) throws SQLException {
            q();
        }

        public void updateBlob(int i, InputStream in) throws SQLException {
            q();
        }

        public void updateBlob(String string, InputStream in) throws SQLException {
            q();
        }

        public void updateClob(int i, Reader reader) throws SQLException {
            q();
        }

        public void updateClob(String string, Reader reader) throws SQLException {
            q();
        }

        public void updateNClob(int i, Reader reader) throws SQLException {
            q();
        }

        public void updateNClob(String string, Reader reader) throws SQLException {
            q();
        }

        public <T> T unwrap(Class<T> type) throws SQLException {
            q();
            return null;
        }

        public boolean isWrapperFor(Class<?> type) throws SQLException {
            q();
            return false;
        }

        //1.7
        public <T> T getObject(String parameterName, Class<T> t) throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("nyi");
        }

        public <T> T getObject(int columnIndex, Class<T> t) throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("nyi");
        }
    }

    public class rm implements ResultSetMetaData {
        private String[] f;
        private Object[] d;

        public rm(String[] x, Object[] y) {
            f = x;
            d = y;
        }

        public int getColumnCount() throws SQLException {
            return f.length;
        }

        public String getColumnName(int i) throws SQLException {
            return f[i - 1];
        }

        public String getColumnTypeName(int i) throws SQLException {
            return TYPE[c.t(d[i - 1])];
        }

        public int getColumnDisplaySize(int i) throws SQLException {
            return 11;
        }

        public int getScale(int i) throws SQLException {
            return 2;
        }

        public int isNullable(int i) throws SQLException {
            return 1;
        }

        public String getColumnLabel(int i) throws SQLException {
            return getColumnName(i);
        }

        public int getColumnType(int i) throws SQLException {
            return SQLTYPE[c.t(d[i - 1])];
        }

        public int getPrecision(int i) throws SQLException {
            return 11;
        } //SQLPREC[c.t(d[i-1])];}

        public boolean isSigned(int i) throws SQLException {
            return true;
        }

        public String getTableName(int i) throws SQLException {
            return "";
        }

        public String getSchemaName(int i) throws SQLException {
            return "";
        }

        public String getCatalogName(int i) throws SQLException {
            return "";
        }

        public boolean isReadOnly(int i) throws SQLException {
            return false;
        }

        public boolean isWritable(int i) throws SQLException {
            return false;
        }

        public boolean isDefinitelyWritable(int i) throws SQLException {
            return false;
        }

        public boolean isAutoIncrement(int i) throws SQLException {
            return false;
        }

        public boolean isCaseSensitive(int i) throws SQLException {
            return true;
        }

        public boolean isSearchable(int i) throws SQLException {
            return true;
        }

        public boolean isCurrency(int i) throws SQLException {
            return false;
        }

        public String getColumnClassName(int column) throws SQLException {
            q("col");
            return null;
        }

        //4
        public <T> T unwrap(Class<T> type) throws SQLException {
            q();
            return null;
        }

        public boolean isWrapperFor(Class<?> type) throws SQLException {
            q();
            return false;
        }
    }
}
