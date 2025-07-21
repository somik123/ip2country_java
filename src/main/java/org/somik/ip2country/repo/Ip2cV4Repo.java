package org.somik.ip2country.repo;

import java.math.BigInteger;
import java.util.List;

import org.somik.ip2country.entity.Ip2cV4;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface Ip2cV4Repo extends JpaRepository<Ip2cV4, Long> {
    // Additional query methods can be defined here if needed
    // @Query("SELECT i FROM Ip2cV4 i WHERE i.ipFrom <= ?1 AND i.ipTo >= ?1")
    @Query(value = "SELECT * FROM IP2CV4 ip WHERE ?1 BETWEEN ip.ip_from AND ip.ip_to", nativeQuery = true)
    List<Ip2cV4> findCountryByIpRange(BigInteger ip);
}