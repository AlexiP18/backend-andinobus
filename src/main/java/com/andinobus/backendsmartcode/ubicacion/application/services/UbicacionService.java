package com.andinobus.backendsmartcode.ubicacion.application.services;

import com.andinobus.backendsmartcode.ubicacion.domain.entities.Canton;
import com.andinobus.backendsmartcode.ubicacion.domain.entities.Provincia;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UbicacionService {

    private static final List<Provincia> PROVINCIAS_ECUADOR = initProvincias();

    private static List<Provincia> initProvincias() {
        List<Provincia> provincias = new ArrayList<>();

        // Azuay
        provincias.add(Provincia.builder()
                .nombre("Azuay")
                .capital("Cuenca")
                .latitud(-2.9001)
                .longitud(-79.0059)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Cuenca").latitud(-2.9001).longitud(-79.0059).esCapital(true).build(),
                        Canton.builder().nombre("Girón").latitud(-3.1647).longitud(-79.1494).esCapital(false).build(),
                        Canton.builder().nombre("Gualaceo").latitud(-2.8925).longitud(-78.7794).esCapital(false).build(),
                        Canton.builder().nombre("Paute").latitud(-2.7769).longitud(-78.7572).esCapital(false).build(),
                        Canton.builder().nombre("Santa Isabel").latitud(-3.2667).longitud(-79.3167).esCapital(false).build()
                ))
                .build());

        // Bolívar
        provincias.add(Provincia.builder()
                .nombre("Bolívar")
                .capital("Guaranda")
                .latitud(-1.5897)
                .longitud(-79.0059)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Guaranda").latitud(-1.5897).longitud(-79.0059).esCapital(true).build(),
                        Canton.builder().nombre("Caluma").latitud(-1.6167).longitud(-79.2833).esCapital(false).build(),
                        Canton.builder().nombre("Chimbo").latitud(-1.6333).longitud(-79.0333).esCapital(false).build(),
                        Canton.builder().nombre("San Miguel").latitud(-1.7167).longitud(-79.0500).esCapital(false).build()
                ))
                .build());

        // Cañar
        provincias.add(Provincia.builder()
                .nombre("Cañar")
                .capital("Azogues")
                .latitud(-2.7394)
                .longitud(-78.8476)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Azogues").latitud(-2.7394).longitud(-78.8476).esCapital(true).build(),
                        Canton.builder().nombre("Cañar").latitud(-2.5589).longitud(-78.9394).esCapital(false).build(),
                        Canton.builder().nombre("La Troncal").latitud(-2.4231).longitud(-79.3394).esCapital(false).build()
                ))
                .build());

        // Carchi
        provincias.add(Provincia.builder()
                .nombre("Carchi")
                .capital("Tulcán")
                .latitud(0.8110)
                .longitud(-77.7178)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Tulcán").latitud(0.8110).longitud(-77.7178).esCapital(true).build(),
                        Canton.builder().nombre("Bolívar").latitud(0.4833).longitud(-77.8667).esCapital(false).build(),
                        Canton.builder().nombre("Espejo").latitud(0.6167).longitud(-77.8667).esCapital(false).build(),
                        Canton.builder().nombre("Mira").latitud(0.5667).longitud(-77.7167).esCapital(false).build()
                ))
                .build());

        // Chimborazo
        provincias.add(Provincia.builder()
                .nombre("Chimborazo")
                .capital("Riobamba")
                .latitud(-1.6635)
                .longitud(-78.6547)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Riobamba").latitud(-1.6635).longitud(-78.6547).esCapital(true).build(),
                        Canton.builder().nombre("Alausí").latitud(-2.2000).longitud(-78.8500).esCapital(false).build(),
                        Canton.builder().nombre("Guano").latitud(-1.6000).longitud(-78.6333).esCapital(false).build(),
                        Canton.builder().nombre("Pallatanga").latitud(-2.0000).longitud(-78.9667).esCapital(false).build()
                ))
                .build());

        // Cotopaxi
        provincias.add(Provincia.builder()
                .nombre("Cotopaxi")
                .capital("Latacunga")
                .latitud(-0.9346)
                .longitud(-78.6156)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Latacunga").latitud(-0.9346).longitud(-78.6156).esCapital(true).build(),
                        Canton.builder().nombre("La Maná").latitud(-0.9400).longitud(-79.2267).esCapital(false).build(),
                        Canton.builder().nombre("Pujilí").latitud(-0.9500).longitud(-78.7000).esCapital(false).build(),
                        Canton.builder().nombre("Salcedo").latitud(-1.0333).longitud(-78.5833).esCapital(false).build(),
                        Canton.builder().nombre("Saquisilí").latitud(-0.8333).longitud(-78.6667).esCapital(false).build()
                ))
                .build());

        // El Oro
        provincias.add(Provincia.builder()
                .nombre("El Oro")
                .capital("Machala")
                .latitud(-3.2581)
                .longitud(-79.9553)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Machala").latitud(-3.2581).longitud(-79.9553).esCapital(true).build(),
                        Canton.builder().nombre("Arenillas").latitud(-3.5500).longitud(-80.0667).esCapital(false).build(),
                        Canton.builder().nombre("El Guabo").latitud(-3.2333).longitud(-79.8333).esCapital(false).build(),
                        Canton.builder().nombre("Huaquillas").latitud(-3.4764).longitud(-80.2308).esCapital(false).build(),
                        Canton.builder().nombre("Pasaje").latitud(-3.3263).longitud(-79.8070).esCapital(false).build(),
                        Canton.builder().nombre("Santa Rosa").latitud(-3.4489).longitud(-79.9597).esCapital(false).build()
                ))
                .build());

        // Esmeraldas
        provincias.add(Provincia.builder()
                .nombre("Esmeraldas")
                .capital("Esmeraldas")
                .latitud(0.9682)
                .longitud(-79.6519)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Esmeraldas").latitud(0.9682).longitud(-79.6519).esCapital(true).build(),
                        Canton.builder().nombre("Atacames").latitud(0.8667).longitud(-79.8500).esCapital(false).build(),
                        Canton.builder().nombre("Muisne").latitud(0.6000).longitud(-80.0167).esCapital(false).build(),
                        Canton.builder().nombre("Quinindé").latitud(0.3167).longitud(-79.4667).esCapital(false).build(),
                        Canton.builder().nombre("San Lorenzo").latitud(1.2833).longitud(-78.8333).esCapital(false).build()
                ))
                .build());

        // Galápagos
        provincias.add(Provincia.builder()
                .nombre("Galápagos")
                .capital("Puerto Baquerizo Moreno")
                .latitud(-0.7436)
                .longitud(-90.3054)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Puerto Baquerizo Moreno").latitud(-0.7436).longitud(-90.3054).esCapital(true).build(),
                        Canton.builder().nombre("Puerto Ayora").latitud(-0.7397).longitud(-90.3147).esCapital(false).build(),
                        Canton.builder().nombre("Puerto Villamil").latitud(-0.9500).longitud(-90.9667).esCapital(false).build()
                ))
                .build());

        // Guayas
        provincias.add(Provincia.builder()
                .nombre("Guayas")
                .capital("Guayaquil")
                .latitud(-2.1709)
                .longitud(-79.9224)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Guayaquil").latitud(-2.1709).longitud(-79.9224).esCapital(true).build(),
                        Canton.builder().nombre("Daule").latitud(-1.8667).longitud(-79.9833).esCapital(false).build(),
                        Canton.builder().nombre("Durán").latitud(-2.1717).longitud(-79.8392).esCapital(false).build(),
                        Canton.builder().nombre("Milagro").latitud(-2.1344).longitud(-79.5944).esCapital(false).build(),
                        Canton.builder().nombre("Playas").latitud(-2.6333).longitud(-80.3833).esCapital(false).build(),
                        Canton.builder().nombre("Salinas").latitud(-2.2145).longitud(-80.9558).esCapital(false).build(),
                        Canton.builder().nombre("Samborondón").latitud(-1.9667).longitud(-79.7333).esCapital(false).build()
                ))
                .build());

        // Imbabura
        provincias.add(Provincia.builder()
                .nombre("Imbabura")
                .capital("Ibarra")
                .latitud(0.3499)
                .longitud(-78.1263)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Ibarra").latitud(0.3499).longitud(-78.1263).esCapital(true).build(),
                        Canton.builder().nombre("Antonio Ante").latitud(0.3333).longitud(-78.1500).esCapital(false).build(),
                        Canton.builder().nombre("Cotacachi").latitud(0.3000).longitud(-78.2667).esCapital(false).build(),
                        Canton.builder().nombre("Otavalo").latitud(0.2333).longitud(-78.2667).esCapital(false).build(),
                        Canton.builder().nombre("Pimampiro").latitud(0.3833).longitud(-77.9500).esCapital(false).build()
                ))
                .build());

        // Loja
        provincias.add(Provincia.builder()
                .nombre("Loja")
                .capital("Loja")
                .latitud(-3.9930)
                .longitud(-79.2040)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Loja").latitud(-3.9930).longitud(-79.2040).esCapital(true).build(),
                        Canton.builder().nombre("Catamayo").latitud(-3.9833).longitud(-79.3500).esCapital(false).build(),
                        Canton.builder().nombre("Cariamanga").latitud(-4.3333).longitud(-79.5500).esCapital(false).build(),
                        Canton.builder().nombre("Macará").latitud(-4.3833).longitud(-79.9500).esCapital(false).build(),
                        Canton.builder().nombre("Pindal").latitud(-3.8667).longitud(-79.9167).esCapital(false).build()
                ))
                .build());

        // Los Ríos
        provincias.add(Provincia.builder()
                .nombre("Los Ríos")
                .capital("Babahoyo")
                .latitud(-1.8015)
                .longitud(-79.5345)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Babahoyo").latitud(-1.8015).longitud(-79.5345).esCapital(true).build(),
                        Canton.builder().nombre("Baba").latitud(-1.7167).longitud(-79.5333).esCapital(false).build(),
                        Canton.builder().nombre("Montalvo").latitud(-1.7833).longitud(-79.2833).esCapital(false).build(),
                        Canton.builder().nombre("Quevedo").latitud(-1.0275).longitud(-79.4631).esCapital(false).build(),
                        Canton.builder().nombre("Ventanas").latitud(-1.4500).longitud(-79.4667).esCapital(false).build(),
                        Canton.builder().nombre("Vinces").latitud(-1.5500).longitud(-79.7500).esCapital(false).build()
                ))
                .build());

        // Manabí
        provincias.add(Provincia.builder()
                .nombre("Manabí")
                .capital("Portoviejo")
                .latitud(-1.0546)
                .longitud(-80.4549)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Portoviejo").latitud(-1.0546).longitud(-80.4549).esCapital(true).build(),
                        Canton.builder().nombre("Bahía de Caráquez").latitud(-0.5983).longitud(-80.4244).esCapital(false).build(),
                        Canton.builder().nombre("Chone").latitud(-0.6833).longitud(-80.1000).esCapital(false).build(),
                        Canton.builder().nombre("El Carmen").latitud(-0.2667).longitud(-79.4500).esCapital(false).build(),
                        Canton.builder().nombre("Jipijapa").latitud(-1.3483).longitud(-80.5789).esCapital(false).build(),
                        Canton.builder().nombre("Manta").latitud(-0.9500).longitud(-80.7333).esCapital(false).build(),
                        Canton.builder().nombre("Montecristi").latitud(-1.0500).longitud(-80.6667).esCapital(false).build()
                ))
                .build());

        // Morona Santiago
        provincias.add(Provincia.builder()
                .nombre("Morona Santiago")
                .capital("Macas")
                .latitud(-2.3088)
                .longitud(-78.1157)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Macas").latitud(-2.3088).longitud(-78.1157).esCapital(true).build(),
                        Canton.builder().nombre("Gualaquiza").latitud(-3.4000).longitud(-78.5667).esCapital(false).build(),
                        Canton.builder().nombre("Limón Indanza").latitud(-2.9667).longitud(-78.4167).esCapital(false).build(),
                        Canton.builder().nombre("Sucúa").latitud(-2.4500).longitud(-78.1667).esCapital(false).build()
                ))
                .build());

        // Napo
        provincias.add(Provincia.builder()
                .nombre("Napo")
                .capital("Tena")
                .latitud(-0.9950)
                .longitud(-77.8167)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Tena").latitud(-0.9950).longitud(-77.8167).esCapital(true).build(),
                        Canton.builder().nombre("Archidona").latitud(-0.9167).longitud(-77.8000).esCapital(false).build(),
                        Canton.builder().nombre("El Chaco").latitud(-0.3333).longitud(-77.8167).esCapital(false).build(),
                        Canton.builder().nombre("Quijos").latitud(-0.2667).longitud(-77.8667).esCapital(false).build()
                ))
                .build());

        // Orellana
        provincias.add(Provincia.builder()
                .nombre("Orellana")
                .capital("Francisco de Orellana")
                .latitud(-0.4664)
                .longitud(-76.9871)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Francisco de Orellana (Coca)").latitud(-0.4664).longitud(-76.9871).esCapital(true).build(),
                        Canton.builder().nombre("La Joya de los Sachas").latitud(-0.3500).longitud(-76.6167).esCapital(false).build(),
                        Canton.builder().nombre("Loreto").latitud(-0.7000).longitud(-77.2833).esCapital(false).build()
                ))
                .build());

        // Pastaza
        provincias.add(Provincia.builder()
                .nombre("Pastaza")
                .capital("Puyo")
                .latitud(-1.4877)
                .longitud(-78.0037)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Puyo").latitud(-1.4877).longitud(-78.0037).esCapital(true).build(),
                        Canton.builder().nombre("Mera").latitud(-1.4667).longitud(-78.1167).esCapital(false).build(),
                        Canton.builder().nombre("Santa Clara").latitud(-1.2500).longitud(-77.8667).esCapital(false).build()
                ))
                .build());

        // Pichincha
        provincias.add(Provincia.builder()
                .nombre("Pichincha")
                .capital("Quito")
                .latitud(-0.1807)
                .longitud(-78.4678)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Quito").latitud(-0.1807).longitud(-78.4678).esCapital(true).build(),
                        Canton.builder().nombre("Cayambe").latitud(0.0417).longitud(-78.1444).esCapital(false).build(),
                        Canton.builder().nombre("Machachi").latitud(-0.5100).longitud(-78.5667).esCapital(false).build(),
                        Canton.builder().nombre("Pedro Moncayo").latitud(0.1167).longitud(-78.1167).esCapital(false).build(),
                        Canton.builder().nombre("Rumiñahui").latitud(-0.3667).longitud(-78.4500).esCapital(false).build(),
                        Canton.builder().nombre("San Miguel de los Bancos").latitud(-0.0167).longitud(-78.9000).esCapital(false).build()
                ))
                .build());

        // Santa Elena
        provincias.add(Provincia.builder()
                .nombre("Santa Elena")
                .capital("Santa Elena")
                .latitud(-2.2267)
                .longitud(-80.8590)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Santa Elena").latitud(-2.2267).longitud(-80.8590).esCapital(true).build(),
                        Canton.builder().nombre("La Libertad").latitud(-2.2333).longitud(-80.9000).esCapital(false).build(),
                        Canton.builder().nombre("Salinas").latitud(-2.2145).longitud(-80.9558).esCapital(false).build()
                ))
                .build());

        // Santo Domingo de los Tsáchilas
        provincias.add(Provincia.builder()
                .nombre("Santo Domingo de los Tsáchilas")
                .capital("Santo Domingo")
                .latitud(-0.2521)
                .longitud(-79.1753)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Santo Domingo").latitud(-0.2521).longitud(-79.1753).esCapital(true).build()
                ))
                .build());

        // Sucumbíos
        provincias.add(Provincia.builder()
                .nombre("Sucumbíos")
                .capital("Nueva Loja")
                .latitud(0.0868)
                .longitud(-76.8873)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Nueva Loja (Lago Agrio)").latitud(0.0868).longitud(-76.8873).esCapital(true).build(),
                        Canton.builder().nombre("Cascales").latitud(0.1167).longitud(-77.2667).esCapital(false).build(),
                        Canton.builder().nombre("Cuyabeno").latitud(0.0000).longitud(-75.9167).esCapital(false).build(),
                        Canton.builder().nombre("Shushufindi").latitud(0.1833).longitud(-76.6500).esCapital(false).build()
                ))
                .build());

        // Tungurahua
        provincias.add(Provincia.builder()
                .nombre("Tungurahua")
                .capital("Ambato")
                .latitud(-1.2543)
                .longitud(-78.6226)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Ambato").latitud(-1.2543).longitud(-78.6226).esCapital(true).build(),
                        Canton.builder().nombre("Baños de Agua Santa").latitud(-1.3967).longitud(-78.4231).esCapital(false).build(),
                        Canton.builder().nombre("Cevallos").latitud(-1.3500).longitud(-78.6167).esCapital(false).build(),
                        Canton.builder().nombre("Mocha").latitud(-1.4833).longitud(-78.6500).esCapital(false).build(),
                        Canton.builder().nombre("Patate").latitud(-1.3167).longitud(-78.5000).esCapital(false).build(),
                        Canton.builder().nombre("Pelileo").latitud(-1.3333).longitud(-78.5500).esCapital(false).build(),
                        Canton.builder().nombre("Píllaro").latitud(-1.1667).longitud(-78.5333).esCapital(false).build(),
                        Canton.builder().nombre("Quero").latitud(-1.3833).longitud(-78.6167).esCapital(false).build(),
                        Canton.builder().nombre("Tisaleo").latitud(-1.3333).longitud(-78.6500).esCapital(false).build()
                ))
                .build());

        // Zamora Chinchipe
        provincias.add(Provincia.builder()
                .nombre("Zamora Chinchipe")
                .capital("Zamora")
                .latitud(-4.0672)
                .longitud(-78.9507)
                .cantones(Arrays.asList(
                        Canton.builder().nombre("Zamora").latitud(-4.0672).longitud(-78.9507).esCapital(true).build(),
                        Canton.builder().nombre("Chinchipe").latitud(-4.8833).longitud(-78.9667).esCapital(false).build(),
                        Canton.builder().nombre("Nangaritza").latitud(-4.2667).longitud(-78.6667).esCapital(false).build(),
                        Canton.builder().nombre("Yacuambi").latitud(-3.6167).longitud(-78.9000).esCapital(false).build()
                ))
                .build());

        return provincias;
    }

    public List<Provincia> getTodasLasProvincias() {
        log.info("Obteniendo todas las provincias y cantones de Ecuador");
        return PROVINCIAS_ECUADOR;
    }

    public Provincia getProvinciaPorNombre(String nombre) {
        log.info("Buscando provincia: {}", nombre);
        return PROVINCIAS_ECUADOR.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);
    }
}
