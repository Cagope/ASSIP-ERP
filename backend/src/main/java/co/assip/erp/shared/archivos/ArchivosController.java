package co.assip.erp.shared.archivos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/shared/archivos")
public class ArchivosController {

    @Value("${assip.archivos.firmas}")
    private String rutaFirmas;

    @GetMapping("/firmas/{archivo:.+}")
    public ResponseEntity<Resource> obtenerFirma(
            @PathVariable String archivo
    ) {

        try {

            Path ruta =
                    Paths.get(rutaFirmas)
                            .resolve(archivo)
                            .normalize();

            Resource resource =
                    new UrlResource(ruta.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String nombre = resource.getFilename() != null
                    ? resource.getFilename().toLowerCase()
                    : "";

            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

            if (nombre.endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (
                    nombre.endsWith(".jpg")
                            || nombre.endsWith(".jpeg")
            ) {
                mediaType = MediaType.IMAGE_JPEG;
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);

        } catch (Exception e) {

            log.error(
                    "Error obteniendo firma {}",
                    archivo,
                    e
            );

            return ResponseEntity.notFound().build();
        }
    }
}