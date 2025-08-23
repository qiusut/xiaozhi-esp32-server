package xiaozhi.modules.bind.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备类型枚举
 */
@Getter
@AllArgsConstructor
public enum DeviceTypeEnum {

    AI("ai","ai设备"),
    TB("tb","tb设备");

    private final String code;
    private final String value;

    @Override
    public String toString() {
        return this.code;
    }

}
