package jp.co.santa.soft.tui.config.mapper;

import jp.co.santa.soft.tui.config.UnitConfig;
import jp.co.santa.soft.tui.config.UnitConfigState;
import jp.co.santa.soft.tui.config.state.InstanceRecord;
import jp.co.santa.soft.tui.config.state.ServiceRecord;
import jp.co.santa.soft.tui.config.state.UserRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface UnitConfigMapper {
  UnitConfigMapper INSTANCE = Mappers.getMapper(UnitConfigMapper.class);

//  UnitConfigState toState(UnitConfig config);
//
//  UserRecord toUserRecord(UnitConfig.User user);
//
//  ServiceRecord toServiceRecord(UnitConfig.Service service);
//
//  InstanceRecord toInstanceRecord(UnitConfig.Instance instance);
}
