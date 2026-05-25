package com.clmcat.qianyu.core.geo;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 地理位置类型
 */
public class PointTypeHandler extends BaseTypeHandler<Point> {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final WKTReader WKT_READER = new WKTReader(GEOMETRY_FACTORY);
    private static final WKTWriter WKT_WRITER = new WKTWriter();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Point parameter, JdbcType jdbcType) throws SQLException {
        String wkt = WKT_WRITER.write(parameter);
        ps.setString(i, wkt);
    }

    @Override
    public Point getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String wkt = rs.getString(columnName);
        return parseWkt(wkt);
    }

    @Override
    public Point getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String wkt = rs.getString(columnIndex);
        return parseWkt(wkt);
    }

    @Override
    public Point getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String wkt = cs.getString(columnIndex);
        return parseWkt(wkt);
    }

    private Point parseWkt(String wkt) {
        if (wkt == null || wkt.trim().isEmpty()) {
            return null;
        }
        try {
            return (Point) WKT_READER.read(wkt);
        } catch (Exception e) {
            throw new RuntimeException("解析 WKT 失败: " + wkt, e);
        }
    }
}