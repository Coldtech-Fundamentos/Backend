package com.frigora.frigoraplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.frigora.frigoraplatform.assetsmanagement.domain.model.queries.GetSitesByOwnerQuery;
import com.frigora.frigoraplatform.assetsmanagement.domain.model.queries.GetSiteByIdQuery;
import com.frigora.frigoraplatform.assetsmanagement.domain.model.commands.DeleteSiteCommand;
import com.frigora.frigoraplatform.assetsmanagement.domain.services.SiteCommandService;
import com.frigora.frigoraplatform.assetsmanagement.domain.services.SiteQueryService;
import com.frigora.frigoraplatform.assetsmanagement.interfaces.rest.SiteController;
import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Alert;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.AcknowledgeAlertCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.DeleteAlertCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.DeleteEquipmentCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsByEquipmentIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsByTenantIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAlertByIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetEquipmentByIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetEquipmentsByOwnerQuery;
import com.frigora.frigoraplatform.monitoring.domain.services.*;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.*;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.transform.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.List;

class ControllerIdentityTests {
    private UsernamePasswordAuthenticationToken identity() {
        var user = UserDetailsImpl.withValidatedUserId(
                new UserDetailsImpl("iam-only", "", List.of()), 123);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void controllersScopeListsToVerifiedPrincipalWithoutIamRepositories() {
        var sites = mock(SiteQueryService.class);
        new SiteController(mock(SiteCommandService.class), sites).getAllSites(identity());
        verify(sites).handle(new GetSitesByOwnerQuery(123L));
        var equipment = mock(EquipmentQueryService.class);
        new EquipmentController(mock(EquipmentCommandService.class), equipment,
                mock(EquipmentResourceFromEntityAssembler.class),
                mock(CreateEquipmentCommandFromResourceAssembler.class)).getAll(identity());
        verify(equipment).getAllByOwner(new GetEquipmentsByOwnerQuery(123L));
        var alerts = mock(AlertQueryService.class);
        new AlertController(mock(AlertCommandService.class), alerts,
                mock(AlertResourceFromEntityAssembler.class),
                mock(CreateAlertCommandFromResourceAssembler.class)).getAll(null, identity());
        verify(alerts).getAllByTenantId(new GetAllAlertsByTenantIdQuery(123L));
    }

    @Test
    void equipmentFilterCannotExposeAlertsOwnedByAnotherUser() {
        var alerts = mock(AlertQueryService.class);
        var assembler = mock(AlertResourceFromEntityAssembler.class);
        var owned = Alert.builder().tenantId(123L).equipmentId(50L).build();
        when(alerts.getAllByEquipmentId(new GetAllAlertsByEquipmentIdQuery(50L, 123L)))
                .thenReturn(List.of(owned));
        new AlertController(mock(AlertCommandService.class), alerts, assembler,
                mock(CreateAlertCommandFromResourceAssembler.class)).getAll(50L, identity());
        verify(assembler).toResources(List.of(owned));
    }

    @Test
    void resourceReadsAndWritesCarryTheAuthenticatedOwnerId() {
        var authentication = identity();

        var siteQueries = mock(SiteQueryService.class);
        when(siteQueries.handle(any(GetSiteByIdQuery.class))).thenReturn(Optional.empty());
        when(siteQueries.handle(new GetSitesByOwnerQuery(123L))).thenReturn(List.of());
        var siteCommands = mock(SiteCommandService.class);
        var sites = new SiteController(siteCommands, siteQueries);
        sites.getSiteById(9, authentication);
        sites.getSitesByOwner(123L, authentication);
        sites.deleteSite(9, authentication);
        assertThat(sites.getSitesByOwner(456L, authentication).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        verify(siteQueries).handle(new GetSiteByIdQuery(9, 123L));
        verify(siteQueries).handle(new GetSitesByOwnerQuery(123L));
        verify(siteCommands).handle(new DeleteSiteCommand(9, 123L));
        verifyNoMoreInteractions(siteQueries);

        var equipmentQueries = mock(EquipmentQueryService.class);
        when(equipmentQueries.getById(any(GetEquipmentByIdQuery.class)))
                .thenReturn(Optional.empty());
        when(equipmentQueries.getAllByOwner(new GetEquipmentsByOwnerQuery(123L)))
                .thenReturn(List.of());
        var equipmentCommands = mock(EquipmentCommandService.class);
        var equipment =
                new EquipmentController(
                        equipmentCommands,
                        equipmentQueries,
                        mock(EquipmentResourceFromEntityAssembler.class),
                        mock(CreateEquipmentCommandFromResourceAssembler.class));
        equipment.getById(17L, authentication);
        equipment.getByOwner(123L, authentication);
        equipment.delete(17L, authentication);
        assertThat(equipment.getByOwner(456L, authentication).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        verify(equipmentQueries).getById(new GetEquipmentByIdQuery(17L, 123L));
        verify(equipmentQueries).getAllByOwner(new GetEquipmentsByOwnerQuery(123L));
        verify(equipmentCommands).delete(new DeleteEquipmentCommand(17L, 123L));
        verifyNoMoreInteractions(equipmentQueries);

        var alertQueries = mock(AlertQueryService.class);
        when(alertQueries.getById(any(GetAlertByIdQuery.class))).thenReturn(Optional.empty());
        var alertCommands = mock(AlertCommandService.class);
        when(alertCommands.acknowledge(any(AcknowledgeAlertCommand.class)))
                .thenReturn(Optional.empty());
        var alerts =
                new AlertController(
                        alertCommands,
                        alertQueries,
                        mock(AlertResourceFromEntityAssembler.class),
                        mock(CreateAlertCommandFromResourceAssembler.class));
        alerts.getById(24L, authentication);
        alerts.acknowledge(24L, authentication);
        alerts.delete(24L, authentication);
        verify(alertQueries).getById(new GetAlertByIdQuery(24L, 123L));
        verify(alertCommands).acknowledge(new AcknowledgeAlertCommand(24L, 123L));
        verify(alertCommands).delete(new DeleteAlertCommand(24L, 123L));
    }
}
