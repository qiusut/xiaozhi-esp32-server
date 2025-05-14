package xiaozhi.modules.recipe.handlers;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@MappedTypes({List.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ParamTypeHandler2<T> extends BaseTypeHandler<List<T>> {

    private final Class<List<T>> type;

    public ParamTypeHandler2(Class<List<T>> type) {
        if (log.isTraceEnabled()) {
            log.trace("ParamTypeHandler(" + type + ")");
        }
        Assert.notNull(type, "Type argument cannot be null");
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, toJson(parameter));
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String json = rs.getString(columnName);
        return StringUtils.isBlank(json) ? null : parse(json);
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final String json = rs.getString(columnIndex);
        return StringUtils.isBlank(json) ? null : parse(json);
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final String json = cs.getString(columnIndex);
        return StringUtils.isBlank(json) ? null : parse(json);
    }

    protected List<T> parse(String json){
        List<T> object = JSONUtil.parse(json).toBean(type);
        return object;
    }

    protected String toJson(List<T> obj){
        return JSONUtil.toJsonStr(obj);
    }
}
