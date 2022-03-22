package ru.red.authenticationservice.mapper;

import ru.red.authenticationservice.dto.UserDTO;
import ru.red.authenticationservice.dto.UserDetachedDTO;
import ru.red.authenticationservice.dto.UserIdentityDTO;
import ru.red.authenticationservice.jooq.tables.records.UsersRecord;

public interface UserMapper {
    /**
     * Maps UserDTO to UserDetachedDTO
     *
     * @param dto input
     * @return {@link UserDetachedDTO}
     */
    UserDetachedDTO userDTOToUserDetachedDTO(UserDTO dto);

    /**
     * Maps UserDetachedDTO to UserDTO
     *
     * @param detached input
     * @return {@link UserDTO} with {@link UserDTO#getId()} being null
     */
    UserDTO userDetachedDTOToUserDTO(UserDetachedDTO detached);

    /**
     * Maps UsersRecord to UserDTO
     *
     * @param usersRecord input
     * @return {@link UserDTO}
     */
    UserDTO usersRecordToUserDTO(UsersRecord usersRecord);

    /**
     * Maps UserDTO to UsersRecord
     *
     * @param dto input
     * @return {@link UsersRecord} with {@link UsersRecord#getSalt()} being null
     */
    UsersRecord userDTOToUsersRecord(UserDTO dto);

    /**
     * Maps UsersRecord to UserDetachedDTO
     *
     * @param usersRecord input
     * @return {@link UserDetachedDTO}
     */
    UserDetachedDTO usersRecordToUserDetachedDTO(UsersRecord usersRecord);

    /**
     * Maps UserDetachedDTO to UsersRecord
     *
     * @param dto input
     * @return {@link UsersRecord}
     */
    UsersRecord userDetachedDtoToUsersRecord(UserDetachedDTO dto);

    /**
     * Maps User Detached DTO to User Identity DTO
     *
     * @param dto input
     * @return {@link UserIdentityDTO}
     */
    UserIdentityDTO userDetachedDTOToUserIdentityDTO(UserDetachedDTO dto);

    /**
     * Maps User DTO to User Identity DTO
     *
     * @param dto input
     * @return {@link UserIdentityDTO}
     */
    UserIdentityDTO userDTOToUserIdentityDTO(UserDTO dto);

    /**
     * Maps Users Record to User Identity DTO
     *
     * @param usersRecord input
     * @return {@link UserIdentityDTO}
     */
    UserIdentityDTO usersRecordToUserIdentityDTO(UsersRecord usersRecord);
}
