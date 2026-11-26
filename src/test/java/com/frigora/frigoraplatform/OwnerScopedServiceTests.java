package com.frigora.frigoraplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.frigora.frigoraplatform.assetsmanagement.application.internal.SiteCommandServiceImpl;
import com.frigora.frigoraplatform.assetsmanagement.application.internal.SiteQueryServiceImpl;
import com.frigora.frigoraplatform.assetsmanagement.domain.model.commands.DeleteSiteCommand;
import com.frigora.frigoraplatform.assetsmanagement.domain.model.queries.GetSiteByIdQuery;
import com.frigora.frigoraplatform.assetsmanagement.domain.repositories.SiteRepository;
import com.frigora.frigoraplatform.monitoring.application.internal.commandservices.AlertCommandServiceImpl;
import com.frigora.frigoraplatform.monitoring.application.internal.commandservices.EquipmentCommandServiceImpl;
import com.frigora.frigoraplatform.monitoring.application.internal.queryservices.AlertQueryServiceImpl;
import com.frigora.frigoraplatform.monitoring.application.internal.queryservices.EquipmentQueryServiceImpl;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.AcknowledgeAlertCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.DeleteAlertCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.DeleteEquipmentCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAlertByIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsByEquipmentIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetEquipmentByIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.repositories.AlertRepository;
import com.frigora.frigoraplatform.monitoring.domain.repositories.EquipmentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OwnerScopedServiceTests {
    private static final long OWNER_ID = 123L;

    @Test
    void siteReadsAndDeletesUseOwnerScopedRepositoryMethods() {
        var repository = mock(SiteRepository.class);
        var queryService = new SiteQueryServiceImpl(repository);
        var commandService = new SiteCommandServiceImpl(repository);
        when(repository.findByIdAndOwnerId(9, OWNER_ID)).thenReturn(Optional.empty());

        queryService.handle(new GetSiteByIdQuery(9, OWNER_ID));
        assertThat(commandService.handle(new DeleteSiteCommand(9, OWNER_ID))).isFalse();

        verify(repository, times(2)).findByIdAndOwnerId(9, OWNER_ID);
        verify(repository, never()).findById(9);
        verify(repository, never()).delete(any());
    }

    @Test
    void equipmentReadsAndDeletesUseOwnerScopedRepositoryMethods() {
        var repository = mock(EquipmentRepository.class);
        var queryService = new EquipmentQueryServiceImpl(repository);
        var commandService = new EquipmentCommandServiceImpl(repository);
        when(repository.findByIdAndOwnerId(17L, OWNER_ID)).thenReturn(Optional.empty());
        when(repository.deleteByIdAndOwnerId(17L, OWNER_ID)).thenReturn(0L);

        queryService.getById(new GetEquipmentByIdQuery(17L, OWNER_ID));
        assertThat(commandService.delete(new DeleteEquipmentCommand(17L, OWNER_ID))).isFalse();

        verify(repository).findByIdAndOwnerId(17L, OWNER_ID);
        verify(repository).deleteByIdAndOwnerId(17L, OWNER_ID);
        verify(repository, never()).findById(17L);
        verify(repository, never()).deleteById(17L);
    }

    @Test
    void alertReadsAndMutationsUseTenantScopedRepositoryMethods() {
        var repository = mock(AlertRepository.class);
        var queryService = new AlertQueryServiceImpl(repository);
        var commandService = new AlertCommandServiceImpl(repository);
        when(repository.findByIdAndTenantId(24L, OWNER_ID)).thenReturn(Optional.empty());
        when(repository.findByEquipmentIdAndTenantId(50L, OWNER_ID)).thenReturn(java.util.List.of());
        when(repository.deleteByIdAndTenantId(24L, OWNER_ID)).thenReturn(0L);

        queryService.getById(new GetAlertByIdQuery(24L, OWNER_ID));
        queryService.getAllByEquipmentId(new GetAllAlertsByEquipmentIdQuery(50L, OWNER_ID));
        assertThat(commandService.acknowledge(new AcknowledgeAlertCommand(24L, OWNER_ID)))
                .isEmpty();
        assertThat(commandService.delete(new DeleteAlertCommand(24L, OWNER_ID))).isFalse();

        verify(repository, times(2)).findByIdAndTenantId(24L, OWNER_ID);
        verify(repository).findByEquipmentIdAndTenantId(50L, OWNER_ID);
        verify(repository).deleteByIdAndTenantId(24L, OWNER_ID);
        verify(repository, never()).findById(24L);
        verify(repository, never()).deleteById(24L);
    }
}
