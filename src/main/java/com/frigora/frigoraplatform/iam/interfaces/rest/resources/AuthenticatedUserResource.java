package com.frigora.frigoraplatform.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(int id, String username, String token, String role) {

}
