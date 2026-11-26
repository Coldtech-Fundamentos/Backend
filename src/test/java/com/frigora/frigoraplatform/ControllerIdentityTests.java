package com.frigora.frigoraplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.frigora.frigoraplatform.assetsmanagement.domain.model.queries.GetSitesByOwnerQuery;
import com.frigora.frigoraplatform.assetsmanagement.domain.services.SiteCommandService;
import com.frigora.frigoraplatform.assetsmanagement.domain.services.SiteQueryService;
import com.frigora.frigoraplatform.assetsmanagement.interfaces.rest.SiteController;
import com.frigora.frigoraplatform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Alert;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsByEquipmentIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsByTenantIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetEquipmentsByOwnerQuery;
import com.frigora.frigoraplatform.monitoring.domain.services.*;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.*;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.transform.*;
import org.junit.jupiter.api.Test;
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
        var foreign = Alert.builder().tenantId(456L).equipmentId(50L).build();
        when(alerts.getAllByEquipmentId(new GetAllAlertsByEquipmentIdQuery(50L)))
                .thenReturn(List.of(owned, foreign));
        new AlertController(mock(AlertCommandService.class), alerts, assembler,
                mock(CreateAlertCommandFromResourceAssembler.class)).getAll(50L, identity());
        verify(assembler).toResources(List.of(owned));
    }
}
