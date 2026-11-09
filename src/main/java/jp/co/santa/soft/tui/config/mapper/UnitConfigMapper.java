package jp.co.santa.soft.tui.config.mapper;

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
