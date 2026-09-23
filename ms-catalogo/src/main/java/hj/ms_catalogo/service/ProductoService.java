package hj.ms_catalogo.service;

import hj.ms_catalogo.dto.ProductoResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

  private final List<ProductoResponse> productos = List.of(
      new ProductoResponse(
          1L,
          "Escarabajo Hércules",
          "Uno de los insectos más grandes del mundo. El macho posee un par de "
              + "cuernos imponentes que usa para competir con otros machos.",
          25000L,
          8,
          "https://2.bp.blogspot.com/-jhC3EGqWyLA/WJXcSqvN7xI/AAAAAAAAAbY/hJTXely3t4Y78ur1ZVobfFeCg7V0GHH8QCLcB/s1600/2017-04-2--10-18-21.png",
          "TERRESTRE"
      ),
      new ProductoResponse(
          2L,
          "Mantis Religiosa",
          "Depredador experto con una postura característica que recuerda a "
              + "alguien rezando. Puede girar la cabeza casi 180 grados.",
          12000L,
          15,
          "https://misanimales.com/wp-content/uploads/2020/03/mantis-religiosa-defensa-1024x678.jpg",
          "TERRESTRE"
      ),
      new ProductoResponse(
          3L,
          "Mariposa Morpho Azul",
          "Sus alas de un azul iridiscente no tienen pigmento: el color nace "
              + "de la forma en que las escamas reflejan la luz.",
          18000L,
          10,
          "https://img.freepik.com/fotos-premium/mariposa-morfo-azul-sus-alas-abiertas-hoja_673637-801.jpg?w=2000",
          "VOLADOR"
      ),
      new ProductoResponse(
          4L,
          "Ciervo Volante",
          "Escarabajo robusto con mandíbulas grandes en forma de asta, que "
              + "usa principalmente para enfrentarse a otros machos.",
          22000L,
          6,
          "https://live.staticflickr.com/65535/52345776218_abf18a6d5d_b.jpg",
          "VOLADOR"
      ),
      new ProductoResponse(
          5L,
          "Mariquita",
          "Pequeño escarabajo de puntos característicos, aliado natural de "
              + "la agricultura por alimentarse de pulgones.",
          5000L,
          40,
          "https://www.lesinsectes.biz/wp-content/uploads/2024/12/les-insectes-1024x655.jpg",
          "VOLADOR"
      ),
      new ProductoResponse(
          6L,
          "Insecto Palo",
          "Maestro del camuflaje: su cuerpo alargado imita perfectamente "
              + "una ramita para pasar desapercibido ante depredadores.",
          9000L,
          20,
          "https://content.nationalgeographic.com.es/medio/2021/07/20/entre-los-fasmidos-los-machos-son-mas-pequenos-que-las-hembras-y-su-extremo-abdominal-es-distinto-en-la-imagen-copula-del-insecto-palo-de-borneo-phenacephorus-cornucervi_7386a9e5_1280x931.jpg",
          "TERRESTRE"
      ),
      new ProductoResponse(
          7L,
          "Escarabajo Buceador",
          "Vive bajo el agua y atrapa una burbuja de aire bajo sus élitros "
              + "para poder respirar mientras nada en busca de presas.",
          15000L,
          12,
          "https://tse4.mm.bing.net/th/id/OIP.eOcA1ydvbpJuhzE6Sbq_vQHaFH?r=0&w=700&h=484&rs=1&pid=ImgDetMain&o=7&rm=3",
          "ACUATICO"
      ),
      new ProductoResponse(
          1L,
          "Ninfa de Libelula",
          "La etapa previa de una libelula, la que habita bajo el agua hasta que se transforma en adulto y sale a volar. Son depredadores voraces.",
          12000L,
          28,
          "https://thumbs.dreamstime.com/b/ninfa-da-lib%C3%A9lula-36178034.jpg",
          "ACUATICO"
      ),
      new ProductoResponse(
          1L,
          "Luciernaga Bioluminiscente",
          "Un hermoso tipo de insecto que emite luz en la oscuridad, creando un espectáculo natural fascinante.",
          22000L,
          18,
          "https://www.beonloop.com/wp-content/uploads/2024/07/luciernaga-bioluminiscencia.webp",
          "VOLADOR"
      ),
      new ProductoResponse(
          1L,
          "Madre de la culebra",
          "Un gran insecto chileno que se encuentra en la zona central del país, conocido por su tamaño y fuerza.",
          50000L,
          7,
          "https://i.pinimg.com/originals/7b/1d/44/7b1d4448885c82c2d31c8d4031e0cb02.jpg",
          "TERRESTRE"
      ),
      new ProductoResponse(
          1L,
          "Opilion",
          "Un extraño insecto muy parecido a los arácnidos, con patas largas y delgadas, que se encuentra en diversas regiones del mundo.",
          1000L,
          100,
          "https://www.chilefauna.cl/wp-content/uploads/2023/09/Especies-Opiliones.jpg",
          "TERRESTRE"
      ),
      new ProductoResponse(
          1L,
          "Burrito",
          "Un insecto pequeño y curioso, conocido por su capacidad de enrollarse en una bola para protegerse de los depredadores.",
          3000L,
          68,
          "https://proyectodescartes.org/iCartesiLibri/materiales_didacticos/Coleopteros_escarabajos/imagenes/cap4/43.png",
          "TERRESTRE"
      )
  );

  public List<ProductoResponse> obtenerTodos() {
    return productos;
  }

  public ProductoResponse buscarPorId(Long id) {
    return productos.stream()
        .filter(p -> p.id().equals(id))
        .findFirst()
        .orElse(null);
 }
}
