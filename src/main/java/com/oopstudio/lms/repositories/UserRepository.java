package com.oopstudio.lms.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oopstudio.lms.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByUniqueTeacherId(String uniqueTeacherId);

	List<User> findByRoleOrderByLastNameAscFirstNameAsc(User.Role role);

	List<User> findBySupervisorId(Long supervisorId);

	List<User> findBySupervisorIdAndRoleOrderByLastNameAscFirstNameAsc(Long supervisorId, User.Role role);

	boolean existsByEmail(String email);

	boolean existsByUniqueTeacherId(String uniqueTeacherId);
}
