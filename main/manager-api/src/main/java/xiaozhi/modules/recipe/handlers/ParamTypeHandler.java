
package xiaozhi.modules.recipe.handlers;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;

@Slf4j
@MappedTypes({List.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ParamTypeHandler<T> extends AbstractJsonTypeHandler<List<T>> {
    private final Class<List<T>> type;

    public ParamTypeHandler(Class<List<T>> type) {
        if (log.isTraceEnabled()) {
            log.trace("ParamTypeHandler(" + type + ")");
        }
        Assert.notNull(type, "Type argument cannot be null");
        this.type = type;
    }

    @Override
    protected List<T> parse(String json) {
        System.out.println(json);
        List<T> object = JSONUtil.parse(json).toBean(type);
        return object;
    }

    @Override
    protected String toJson(List<T> object) {
        System.out.println(object.toString());
        return JSONUtil.toJsonStr(object);
    }
}
