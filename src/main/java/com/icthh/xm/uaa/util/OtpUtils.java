package com.icthh.xm.uaa.util;

import com.icthh.xm.uaa.domain.OtpChannelType;
import com.icthh.xm.uaa.domain.UserLogin;
import com.icthh.xm.uaa.domain.UserLoginType;
import com.icthh.xm.uaa.service.dto.TfaOtpChannelSpec;
import com.icthh.xm.uaa.service.dto.UserDTO;
import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The {@link OtpUtils} class.
 */
public final class OtpUtils {

    private static final Map<UserLoginType, List<OtpChannelType>> LOGIN_TYPE_TO_CHANNELS;

    static {
        Map<UserLoginType, List<OtpChannelType>> map = new HashMap<>();
        map.put(UserLoginType.EMAIL, Collections.singletonList(OtpChannelType.EMAIL));
        map.put(UserLoginType.MSISDN, Arrays.asList(OtpChannelType.SMS, OtpChannelType.VIBER, OtpChannelType.WHATSAPP));

        LOGIN_TYPE_TO_CHANNELS = Collections.unmodifiableMap(map);
    }

    private static final Map<OtpChannelType, Collection<UserLoginType>> CHANNEL_TO_LOGIN_TYPES;

    static {
        Map<OtpChannelType, Collection<UserLoginType>> map = new HashMap<>();
        for (Map.Entry<UserLoginType, List<OtpChannelType>> entry : LOGIN_TYPE_TO_CHANNELS.entrySet()) {
            for (OtpChannelType channelType : entry.getValue()) {
                Collection<UserLoginType> dstLoginTypes = map.computeIfAbsent(channelType, otpChannelType -> new LinkedHashSet<>());
                dstLoginTypes.add(entry.getKey());
            }
        }
        CHANNEL_TO_LOGIN_TYPES = Collections.unmodifiableMap(map);
    }

    public static Optional<List<OtpChannelType>> getSupportedOtpChannelTypes(UserLoginType loginType) {
        return Optional.ofNullable(LOGIN_TYPE_TO_CHANNELS.get(loginType));
    }

    public static UserLoginType getRequiredLoginType(String loginTypeKeyStr) {
        return UserLoginType.valueOfType(loginTypeKeyStr).orElseThrow(
            () -> new IllegalArgumentException("Unsupported user login type: " + loginTypeKeyStr)
        );
    }

    public static Optional<Collection<UserLoginType>> getSupportedUserLoginTypes(OtpChannelType otpChannelType) {
        return Optional.ofNullable(CHANNEL_TO_LOGIN_TYPES.get(otpChannelType));
    }

    public static UserDTO enrichTfaOtpChannelSpec(UserDTO userDto) {
        if (userDto == null) {
            return null;
        }

        OtpChannelType tfaOtpChannelType = userDto.getTfaOtpChannelType();
        if (tfaOtpChannelType == null || CollectionUtils.isEmpty(userDto.getLogins())) {
            return userDto;
        }

        Optional<Collection<UserLoginType>> supportedUserLoginTypes = OtpUtils.getSupportedUserLoginTypes(tfaOtpChannelType);
        if (!supportedUserLoginTypes.isPresent()) {
            return userDto;
        }

        UserLogin userLoginForChannel = null;
        for (UserLoginType userLoginType : supportedUserLoginTypes.get()) {
            for (UserLogin userLogin : userDto.getLogins()) {
                if (userLoginType.getValue().equals(userLogin.getTypeKey())) {
                    userLoginForChannel = userLogin;
                    break;
                }
            }

            if (userLoginForChannel != null) {
                break;
            }
        }

        if (userLoginForChannel != null) {
            TfaOtpChannelSpec spec = new TfaOtpChannelSpec(tfaOtpChannelType, userLoginForChannel.getLogin());
            userDto.setTfaOtpChannelSpec(spec);
        }

        return userDto;
    }

}
