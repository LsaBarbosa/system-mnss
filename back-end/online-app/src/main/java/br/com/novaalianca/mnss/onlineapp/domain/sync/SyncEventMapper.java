package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.sync.*;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SyncEventMapper {

    SyncEventDto toDto(SyncEventEntity entity);

    List<SyncEventDto> toDtoList(List<SyncEventEntity> entities);
}
