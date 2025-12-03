package com.andinobus.backendsmartcode.catalogos.domain.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long> {

    List<Terminal> findByActivoTrue();

    List<Terminal> findByProvinciaIgnoreCase(String provincia);

    List<Terminal> findByCantonIgnoreCase(String canton);

    List<Terminal> findByTipologia(String tipologia);

    Optional<Terminal> findByNombreIgnoreCase(String nombre);

    @Query("SELECT t FROM Terminal t WHERE t.activo = true ORDER BY t.provincia, t.canton, t.nombre")
    List<Terminal> findAllActivosOrdenados();

    @Query("SELECT t FROM Terminal t WHERE t.activo = true AND t.tipologia = :tipologia ORDER BY t.provincia, t.canton")
    List<Terminal> findByTipologiaActivos(@Param("tipologia") String tipologia);

    @Query("SELECT DISTINCT t.provincia FROM Terminal t WHERE t.activo = true ORDER BY t.provincia")
    List<String> findProvinciasConTerminales();

    @Query("SELECT t FROM Terminal t WHERE t.activo = true AND LOWER(t.provincia) = LOWER(:provincia) ORDER BY t.canton, t.nombre")
    List<Terminal> findByProvinciaActivos(@Param("provincia") String provincia);

    @Query("SELECT t FROM Terminal t WHERE t.activo = true AND " +
           "(LOWER(t.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(t.canton) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(t.provincia) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Terminal> buscarPorTexto(@Param("busqueda") String busqueda);

    @Query("SELECT COUNT(t) FROM Terminal t WHERE t.activo = true AND t.tipologia = :tipologia")
    long countByTipologia(@Param("tipologia") String tipologia);

    @Query("SELECT SUM(t.maxFrecuenciasDiarias) FROM Terminal t WHERE t.activo = true")
    Long sumMaxFrecuenciasTotales();

    boolean existsByNombreIgnoreCase(String nombre);
}
