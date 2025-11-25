package com.medivault.backend.service.auth;

import com.medivault.backend.domain.common.Role;
import com.medivault.backend.domain.user.User;
import com.medivault.backend.dto.auth.RegisterRequest;

import java.util.UUID;

public interface AuthService {

    User register(RegisterRequest request, Role role, UUID creatorId);

}
