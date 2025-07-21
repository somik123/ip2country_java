package org.somik.ip2country.repo;

import java.math.BigInteger;
import java.util.List;

import org.somik.ip2country.entity.Ip2cV6;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface Ip2cV6Repo extends JpaRepository<Ip2cV6, Long> {
    // Additional query methods can be defined here if needed
    // @Query("SELECT i FROM Ip2cV6 i WHERE i.ipFrom <= ?1 AND i.ipTo >= ?1")
    @Query(value = "SELECT * FROM IP2CV6 ip WHERE ?1 BETWEEN ip.ip_from AND ip.ip_to", nativeQuery = true)
    List<Ip2cV6> findCountryByIpRange(BigInteger ip);
}