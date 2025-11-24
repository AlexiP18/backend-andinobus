# Sistema de Fotos de Buses

## Descripción General

Este documento describe la implementación del sistema de carga y gestión de fotos para los buses de la cooperativa. Los usuarios con rol COOPERATIVA pueden subir, visualizar y eliminar fotos de los buses.

## Estructura de Archivos

```
uploads/
  └── buses/
      └── fotos/
          ├── bus_1_1234567890.jpg
          ├── bus_2_1234567891.png
          └── ...
```

### Convención de Nombres
- Formato: `bus_{id}_{timestamp}.{extension}`
- Ejemplo: `bus_15_1703089234567.jpg`
- Extensiones soportadas: jpg, jpeg, png, gif, webp

## Endpoints del Backend

### 1. Subir Foto de Bus
```http
POST /api/cooperativa/{cooperativaId}/buses/{busId}/foto
Content-Type: multipart/form-data
Authorization: Bearer {token}

Body:
- foto: File (imagen)
```

**Respuesta Exitosa (200 OK):**
```json
{
  "id": 15,
  "cooperativaId": 1,
  "cooperativaNombre": "Trans Andina",
  "placa": "ABC-123",
  "numeroInterno": "001",
  "chasisMarca": "Mercedes-Benz",
  "carroceriaMarca": "Marcopolo",
  "fotoUrl": "/uploads/buses/fotos/bus_15_1703089234567.jpg",
  "capacidadAsientos": 45,
  "estado": "DISPONIBLE",
  "activo": true
}
```

**Errores:**
- `404 Not Found` - Bus no encontrado
- `400 Bad Request` - Archivo no es una imagen o excede 5MB
- `500 Internal Server Error` - Error al guardar el archivo

### 2. Eliminar Foto de Bus
```http
DELETE /api/cooperativa/{cooperativaId}/buses/{busId}/foto
Authorization: Bearer {token}
```

**Respuesta Exitosa:**
- `204 No Content` - Foto eliminada exitosamente

**Errores:**
- `404 Not Found` - Bus no encontrado

## Almacenamiento y Configuración

### Directorio de Almacenamiento
- Path: `uploads/buses/fotos/`
- Creación: Automática si no existe
- Permisos: Lectura/escritura para la aplicación

### Configuración de Servicio Estático (FileStorageConfig.java)
```java
registry.addResourceHandler("/uploads/buses/fotos/**")
        .addResourceLocations("file:uploads/buses/fotos/")
        .setCachePeriod(3600); // 1 hora de caché
```

### URLs de Acceso
- URL completa: `http://localhost:8080/uploads/buses/fotos/bus_15_1703089234567.jpg`
- Path relativo: `/uploads/buses/fotos/bus_15_1703089234567.jpg`

## Validaciones

### Lado del Servidor (BusService.java)
1. **Tipo de Archivo**: Solo imágenes (`image/*`)
2. **Tamaño Máximo**: 5 MB (5 * 1024 * 1024 bytes)
3. **Bus Existente**: Validación de ID válido

### Lado del Cliente (buses/page.tsx)
1. **Tipo de Archivo**: Solo imágenes (`image/*`)
2. **Tamaño Máximo**: 5 MB
3. **Retroalimentación**: Mensajes de error inmediatos

## Características del Frontend

### Vista de Tarjetas (Cards)
- Grid responsivo: 1 columna (móvil) → 3 columnas (desktop)
- Foto del bus: 100% ancho, altura 192px, cover, esquinas redondeadas
- Fallback: Ícono de bus en contenedor gris cuando no hay foto
- Información mostrada:
  - Placa del bus (título)
  - Número interno
  - Modelo (marca chasis o carrocería)
  - Capacidad de asientos
  - Estado (badge de color)
  - Estado activo/inactivo
- Acciones:
  - Gestionar Asientos (botón azul)
  - Editar (botón verde)
  - Desactivar (botón rojo)

### Vista de Tabla
- Columnas:
  1. **Bus** - Foto del bus (16x16, redondeada) o ícono
  2. Placa
  3. Número Interno
  4. Modelo
  5. Capacidad
  6. Estado
  7. Activo
  8. Acciones
- Hover effect en filas
- Responsive con scroll horizontal en móviles

### Modal de Edición/Creación
- **Sección de Foto** (parte superior):
  - Preview: 256x160px (bus con foto) o placeholder gris con ícono
  - Botón de carga: Ícono de upload en círculo verde (bottom-right)
  - Texto informativo: "Foto del bus (opcional) - Máximo 5MB"
- Preview en tiempo real al seleccionar archivo
- Validación de tipo y tamaño antes de mostrar preview
- Foto se sube automáticamente después de guardar el bus

### Alternar Vistas
- Botones en el header (derecha)
- Íconos: Grid3x3 (cards) y Table (tabla)
- Estado activo: Fondo verde, texto blanco
- Estado inactivo: Fondo transparente, texto gris
- Texto visible solo en pantallas SM+

## Modelo de Base de Datos

### Tabla: `bus`

```sql
CREATE TABLE bus (
  id BIGSERIAL PRIMARY KEY,
  cooperativa_id BIGINT NOT NULL REFERENCES cooperativa(id),
  numero_interno VARCHAR(50),
  placa VARCHAR(20) NOT NULL UNIQUE,
  chasis_marca VARCHAR(100),
  carroceria_marca VARCHAR(100),
  foto_url VARCHAR(500),           -- NEW: URL de la foto
  foto_filename VARCHAR(255),      -- NEW: Nombre del archivo
  capacidad_asientos INTEGER DEFAULT 40,
  estado VARCHAR(32) NOT NULL DEFAULT 'DISPONIBLE',
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

COMMENT ON COLUMN bus.foto_url IS 'URL pública de la foto del bus';
COMMENT ON COLUMN bus.foto_filename IS 'Nombre del archivo almacenado en el sistema de archivos';
```

### Migración: V22__add_foto_filename_to_bus.sql

```sql
ALTER TABLE bus ADD COLUMN foto_filename VARCHAR(255);
COMMENT ON COLUMN bus.foto_filename IS 'Nombre del archivo almacenado';
```

**Nota:** La columna `foto_url` ya existía, solo se agregó `foto_filename`.

## Flujo de Trabajo

### Subir Foto al Crear Bus
1. Usuario abre modal "Nuevo Bus"
2. Usuario completa formulario del bus
3. Usuario selecciona foto (opcional)
4. Vista previa se muestra inmediatamente
5. Usuario hace clic en "Guardar"
6. Backend crea el bus en la BD
7. Si hay foto seleccionada, frontend llama al endpoint de foto
8. Backend guarda la foto y actualiza el registro del bus
9. Frontend recarga la lista de buses
10. Foto aparece en la vista de tarjetas/tabla

### Subir Foto al Editar Bus
1. Usuario hace clic en "Editar" en un bus existente
2. Modal se abre con datos del bus
3. Si el bus tiene foto, se muestra en el preview
4. Usuario puede cambiar la foto o mantener la actual
5. Al seleccionar nueva foto, aparece preview
6. Usuario hace clic en "Guardar"
7. Backend actualiza el bus
8. Si hay nueva foto, se sube y reemplaza la anterior
9. Archivo viejo se elimina del sistema
10. Frontend recarga la lista con la foto actualizada

### Eliminar Foto
1. Backend: Endpoint `DELETE /{busId}/foto`
2. Se elimina archivo físico del servidor
3. Se actualiza BD: `foto_url = NULL`, `foto_filename = NULL`
4. Frontend muestra placeholder de ícono

## Manejo de Errores

### Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| 400 Bad Request | Archivo no es imagen o >5MB | Validar tipo y tamaño en cliente |
| 404 Not Found | Bus no existe | Verificar ID del bus |
| 500 Server Error | Permisos de escritura | Verificar permisos del directorio `uploads/` |
| CORS Error | Dominio no permitido | Configurar CORS en backend |

### Mensajes de Error (Frontend)
```typescript
// Tipo de archivo inválido
"Por favor seleccione un archivo de imagen válido"

// Tamaño excedido
"La imagen no debe superar los 5MB"

// Error de red
"Error al subir la foto. Inténtelo nuevamente"
```

## Seguridad

### Validaciones Implementadas
1. ✅ Validación de tipo MIME en servidor (`image/*`)
2. ✅ Límite de tamaño (5 MB)
3. ✅ Nombres de archivo únicos (previene sobrescritura)
4. ✅ Autenticación requerida (Bearer token)
5. ✅ Verificación de pertenencia a cooperativa

### Mejoras Futuras Sugeridas
- [ ] Antivirus scanning de archivos subidos
- [ ] Rate limiting en endpoints de upload
- [ ] Compresión automática de imágenes grandes
- [ ] Generación de thumbnails
- [ ] Watermarking de imágenes
- [ ] Migración a S3/Azure Blob Storage para producción

## Testing

### Pruebas Manuales con Postman

**1. Crear Bus con Foto:**
```bash
# Paso 1: Crear bus
POST http://localhost:8080/api/cooperativa/1/buses
Headers:
  Authorization: Bearer {token}
  Content-Type: application/json
Body:
{
  "placa": "XYZ-789",
  "numeroInterno": "005",
  "chasisMarca": "Volvo",
  "carroceriaMarca": "Busscar",
  "capacidadAsientos": 50
}

# Paso 2: Subir foto
POST http://localhost:8080/api/cooperativa/1/buses/{busId}/foto
Headers:
  Authorization: Bearer {token}
Body (form-data):
  foto: [seleccionar archivo de imagen]
```

**2. Obtener Bus con Foto:**
```bash
GET http://localhost:8080/api/cooperativa/1/buses/{busId}
Headers:
  Authorization: Bearer {token}
```

**3. Eliminar Foto:**
```bash
DELETE http://localhost:8080/api/cooperativa/1/buses/{busId}/foto
Headers:
  Authorization: Bearer {token}
```

## Consideraciones de Producción

### Almacenamiento en Producción
- **Desarrollo**: Sistema de archivos local (`uploads/`)
- **Producción**: Migrar a servicio de almacenamiento en la nube:
  - AWS S3
  - Azure Blob Storage
  - Google Cloud Storage
  - Cloudinary (optimización automática)

### Configuración de Producción
```properties
# application-prod.properties
storage.location=https://tu-bucket.s3.amazonaws.com/buses/fotos/
storage.max-file-size=5MB
storage.allowed-extensions=jpg,jpeg,png,gif,webp
```

### CDN para Rendimiento
- Implementar CDN para servir imágenes
- Caché agresivo (1 año) con versionado de URLs
- Lazy loading en el frontend
- Responsive images con srcset

### Backup y Recuperación
- Backup automático del directorio `uploads/`
- Sincronización con almacenamiento redundante
- Plan de recuperación ante desastres

## Recursos Adicionales

### Archivos Relacionados

**Frontend:**
- `/app/dashboard/Cooperativa/Admin/buses/page.tsx` - Componente principal
- `/lib/api.ts` - Cliente API (BusDetailResponse)
- `/types/user.ts` - Tipos TypeScript

**Backend:**
- `/catalogos/domain/entities/Bus.java` - Entidad JPA
- `/catalogos/api/dto/BusDtos.java` - DTOs
- `/catalogos/api/controllers/BusManagementController.java` - Endpoints
- `/catalogos/application/services/BusService.java` - Lógica de negocio
- `/config/FileStorageConfig.java` - Configuración de archivos estáticos
- `/db/migration/V22__add_foto_filename_to_bus.sql` - Migración

### Patrones Implementados
- **Patrón**: Similar a `PersonalService` con fotos de personal
- **Convención**: Mismo estilo de nombres de archivo y estructura
- **Extensible**: Preparado para agregar fotos de clientes en el futuro

---

**Última Actualización:** 2024
**Versión:** 1.0
**Autor:** Sistema SmartCode - Andino Bus
