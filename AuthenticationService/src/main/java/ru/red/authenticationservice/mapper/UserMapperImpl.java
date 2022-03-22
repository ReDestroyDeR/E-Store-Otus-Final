package ru.red.authenticationservice.mapper;

import org.springframework.stereotype.Component;
import ru.red.authenticationservice.dto.UserDTO;
import ru.red.authenticationservice.dto.UserDetachedDTO;
import ru.red.authenticationservice.dto.UserIdentityDTO;
import ru.red.authenticationservice.jooq.tables.records.UsersRecord;

@Component
public class UserMapperImpl implements UserMapper {
    /**
     * Maps UserDTO to UserDetachedDTO
     *
     * @param dto input
     * @return {@link UserDetachedDTO}
     */
    @Override
    public UserDetachedDTO userDTOToUserDetachedDTO(UserDTO dto) {
        UserDetachedDTO detached = new UserDetachedDTO();
        detached.setEmail(dto.getEmail());
        detached.setPassword(dto.getPassword());
        return detached;
    }

    /**
     * Maps UserDetachedDTO to UserDTO
     *
     * @param detached input
     * @return {@link UserDTO} with {@link UserDTO#getId()} being null
     */
    @Override
    public UserDTO userDetachedDTOToUserDTO(UserDetachedDTO detached) {
        UserDTO dto = new UserDTO();
        dto.setEmail(detached.getEmail());
        dto.setPassword(detached.getPassword());
        return dto;
    }

    /**
     * Maps UsersRecord to UserDTO
     *
     * @param usersRecord input
     * @return {@link UserDTO}
     */
    @Override
    public UserDTO usersRecordToUserDTO(UsersRecord usersRecord) {
        UserDTO dto = new UserDTO();
        dto.setId(usersRecord.getId());
        dto.setEmail(usersRecord.getEmail());
        dto.setPassword(usersRecord.getPassword());
        return dto;
    }

    /**
     * Maps UserDTO to UsersRecord
     *
     * @param dto input
     * @return {@link UsersRecord} with {@link UsersRecord#getSalt()} being null
     */
    @Override
    public UsersRecord userDTOToUsersRecord(UserDTO dto) {
        UsersRecord usersRecord = new UsersRecord();
        usersRecord.setId(dto.getId());
        usersRecord.setUsername(dto.getEmail());
        usersRecord.setPassword(dto.getPassword());
        return usersRecord;
    }

    /**
     * Maps UsersRecord to UserDetachedDTO
     *
     * @param usersRecord input
     * @return {@link UserDetachedDTO}
     */
    @Override
    public UserDetachedDTO usersRecordToUserDetachedDTO(UsersRecord usersRecord) {
        UserDetachedDTO userDetachedDTO = new UserDetachedDTO();
        userDetachedDTO.setEmail(usersRecord.getEmail());
        userDetachedDTO.setPassword(usersRecord.getPassword());
        return userDetachedDTO;
    }

    /**
     * Maps UserDetachedDTO to UsersRecord
     *
     * @param dto input
     * @return {@link UsersRecord}
     */
    @Override
    public UsersRecord userDetachedDtoToUsersRecord(UserDetachedDTO dto) {
        UsersRecord usersRecord = new UsersRecord();
        usersRecord.setUsername(dto.getEmail());
        usersRecord.setPassword(dto.getPassword());
        return usersRecord;
    }

    /**
     * Maps User Detached DTO to User Identity DTO
     *
     * @param dto input
     * @return {@link UserIdentityDTO}
     */
    @Override
    public UserIdentityDTO userDetachedDTOToUserIdentityDTO(UserDetachedDTO dto) {
        UserIdentityDTO identityDTO = new UserIdentityDTO();
        identityDTO.setEmail(dto.getEmail());
        return identityDTO;
    }

    /**
     * Maps User DTO to User Identity DTO
     *
     * @param dto input
     * @return {@link UserIdentityDTO}
     */
    @Override
    public UserIdentityDTO userDTOToUserIdentityDTO(UserDTO dto) {
        UserIdentityDTO identityDTO = new UserIdentityDTO();
        identityDTO.setEmail(dto.getEmail());
        return identityDTO;
    }

    /**
     * Maps Users Record to User Identity DTO
     *
     * @param usersRecord input
     * @return {@link UserIdentityDTO}
     */
    @Override
    public UserIdentityDTO usersRecordToUserIdentityDTO(UsersRecord usersRecord) {
        UserIdentityDTO identityDTO = new UserIdentityDTO();
        identityDTO.setEmail(usersRecord.getEmail());
        return identityDTO;
    }
}
