package com.frigora.frigoraplatform.assetsManagement.domain.services;

import com.frigora.frigoraplatform.assetsManagement.domain.model.aggregates.Site;
import com.frigora.frigoraplatform.assetsManagement.domain.model.commands.CreateSiteCommand;
import com.frigora.frigoraplatform.assetsManagement.domain.model.commands.DeleteSiteCommand;

import java.util.Optional;

public interface ISiteCommandService {
    /**
     * Handle the create site command
     * <p>
     * This method processes the CreateSiteCommand to create a new Site entity.
     * It performs necessary validations and persists the entity to the data store.
     * If it does not exist, it creates a new Site and returns it; otherwise, it returns empty.
     * </p>
     *
     * @param command The CreateSiteCommand containing the site details.
     * @return An Optional containing the newly created Site, or empty if it already exists.
     */
    Optional<Site> handle(CreateSiteCommand command);

    /**
     * Handle the delete site config command
     * <p>
     * Returns true if the site was deleted; otherwise false.
     * </p>
     *
     * @param command The DeleteSiteCommand containing the ID to delete.
     * @return true if the site was successfully deleted; otherwise false.
     */
    boolean handle(DeleteSiteCommand command);
}
