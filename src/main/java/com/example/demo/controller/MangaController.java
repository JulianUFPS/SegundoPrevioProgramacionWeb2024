import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.Manga;
import com.example.demo.repository.MangaRepository;
import com.example.demo.repository.PaisRepository;
import com.example.demo.repository.TipoRepository;

@RestController
@RequestMapping("/mangas")
@CrossOrigin(origins = "*")
public class MangaController {

    @Autowired
    private MangaRepository mangaRepository;

    @Autowired
    private PaisRepository paisRepository;

    @Autowired
    private TipoRepository tipoRepository;

    // Métodos anteriores...

    @PutMapping("/{id}")
    public ResponseEntity<?> updateManga(@PathVariable Long id, @RequestBody Manga mangaUpdate) {
        // Buscar el manga por ID
        Optional<Manga> mangaOptional = mangaRepository.findById(id);
        if (!mangaOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":true,\"msg\":\"Objeto no encontrado\"}");
        }

        Manga manga = mangaOptional.get();

        // Validar campos obligatorios y existencia de país y tipo
        ResponseEntity<?> validationResponse = validateMangaFields(mangaUpdate);
        if (validationResponse != null) {
            return validationResponse;
        }

        // Validar que el país y el tipo existen
        Optional<Pais> paisOptional = paisRepository.findById(mangaUpdate.getPais().getId());
        if (!paisOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":true,\"msg\":\"Pais no existe\"}");
        }

        Optional<Tipo> tipoOptional = tipoRepository.findById(mangaUpdate.getTipo().getId());
        if (!tipoOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":true,\"msg\":\"Tipo no existe\"}");
        }

        // Actualizar los campos del manga
        manga.setNombre(mangaUpdate.getNombre());
        manga.setFechaLanzamiento(mangaUpdate.getFechaLanzamiento());
        manga.setTemporadas(mangaUpdate.getTemporadas());
        manga.setAnime(mangaUpdate.isAnime());
        manga.setJuego(mangaUpdate.isJuego());
        manga.setPelicula(mangaUpdate.isPelicula());
        manga.setPais(paisOptional.get());
        manga.setTipo(tipoOptional.get());

        // Guardar el manga actualizado en la base de datos
        Manga updatedManga = mangaRepository.save(manga);

        // Crear la respuesta con el manga actualizado y los nombres de país y tipo
        Map<String, Object> response = new HashMap<>();
        response.put("id", updatedManga.getId());
        response.put("nombre", updatedManga.getNombre());
        response.put("pais", updatedManga.getPais().getNombre());
        response.put("tipo", updatedManga.getTipo().getNombre());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteManga(@PathVariable Long id) {
        // Buscar el manga por ID
        Optional<Manga> mangaOptional = mangaRepository.findById(id);
        if (!mangaOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":true,\"msg\":\"Objeto no encontrado\"}");
        }

        Manga manga = mangaOptional.get();

        // Validar si el manga tiene usuarios asociados
        if (!manga.getUsuarios().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":true,\"msg\":\"Manga tiene usuarios asociados\"}");
        }

        // Eliminar el manga de la base de datos
        mangaRepository.delete(manga);

        // Crear la respuesta con el manga eliminado
        Map<String, Object> response = new HashMap<>();
        response.put("id", manga.getId());
        response.put("nombre", manga.getNombre());
        response.put("pais", manga.getPais().getNombre());
        response.put("tipo", manga.getTipo().getNombre());

        return ResponseEntity.ok(response);
    }

    // Métodos auxiliares

    private ResponseEntity<?> validateMangaFields(Manga manga) {
        if (manga.getNombre() == null || manga.getNombre().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":true,\"msg\":\"El campo nombre es obligatorio\"}");
        }
        if (manga.getFechaLanzamiento() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":true,\"msg\":\"El campo fechaLanzamiento es obligatorio\"}");
        }
        if (manga.getTemporadas() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":true,\"msg\":\"El campo temporadas es obligatorio\"}");
        }
        if (manga.getPais() == null || manga.getPais().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":true,\"msg\":\"El campo paisId es obligatorio\"}");
        }
        if (manga.getTipo() == null || manga.getTipo().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":true,\"msg\":\"El campo tipoId es obligatorio\"}");
        }
        return null;
    }
}
