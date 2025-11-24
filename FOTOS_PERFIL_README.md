# Sistema de Fotos de Perfil para Personal de Cooperativa

## 📋 Descripción General

Este documento describe la implementación del sistema de fotos de perfil para usuarios del personal de cooperativa (ADMIN, OFICINISTA, CHOFER).

## 🗂️ Estructura de Archivos

### Backend

#### Entidades
- **UsuarioCooperativa.java**: Agregados campos `fotoUrl` y `fotoFilename`

#### DTOs
- **PersonalDtos.PersonalResponse**: Agregado campo `fotoUrl`
- **AuthDtos.AuthResponse**: Agregado campo `fotoUrl`
- **AuthDtos.MeResponse**: Agregado campo `fotoUrl`

#### Controllers
- **PersonalManagementController.java**: 
  - `POST /api/cooperativa/{id}/personal/{personalId}/foto` - Subir foto
  - `DELETE /api/cooperativa/{id}/personal/{personalId}/foto` - Eliminar foto

#### Services
- **PersonalService.java**: 
  - `uploadFoto()` - Procesa y guarda la foto
  - `deleteFoto()` - Elimina la foto del usuario
- **AuthService.java**: Actualizado para incluir `fotoUrl` en respuestas de login

#### Configuración
- **FileStorageConfig.java**: Configuración para servir archivos estáticos

#### Base de Datos
- **V21__add_foto_to_usuario_cooperativa.sql**: Migración para agregar columnas de foto

### Frontend

#### Componentes
- **PersonalManagementController** (`app/dashboard/Cooperativa/Admin/personal/page.tsx`):
  - Vista dual: Cards y Tabla
  - Upload de fotos con preview
  - Visualización de fotos en ambas vistas

#### Context/Types
- **types/user.ts**: Agregado campo `fotoUrl` a interfaces `User` y `AuthResponse`

#### Componentes Layout
- **DashboardNavbar.tsx**: Muestra foto de perfil en navbar (desktop y mobile)

#### API
- **lib/api.ts**: Agregado campo `fotoUrl` a `PersonalDetailResponse`

## 🔧 Endpoints de API

### Subir Foto de Perfil
```
POST /api/cooperativa/{cooperativaId}/personal/{personalId}/foto
Content-Type: multipart/form-data
Authorization: Bearer {token}

Body:
- foto: File (imagen)

Response: PersonalResponse con fotoUrl actualizado
```

### Eliminar Foto de Perfil
```
DELETE /api/cooperativa/{cooperativaId}/personal/{personalId}/foto
Authorization: Bearer {token}

Response: 204 No Content
```

## 📁 Estructura de Almacenamiento

Las fotos se almacenan en el sistema de archivos del servidor:

```
uploads/
└── personal/
    └── fotos/
        ├── personal_1_1700000000000.jpg
        ├── personal_2_1700000000001.png
        └── ...
```

### Formato de Nombres de Archivo
```
personal_{personalId}_{timestamp}.{extension}
```

### URLs de Acceso
```
http://localhost:8080/uploads/personal/fotos/personal_1_1700000000000.jpg
```

## ✅ Validaciones

### Backend
- **Tipo de archivo**: Solo imágenes (image/*)
- **Tamaño máximo**: 5 MB
- **Formatos soportados**: JPG, PNG, GIF, WEBP

### Frontend
- Validación de tipo de archivo antes de subir
- Validación de tamaño antes de subir
- Preview inmediato después de seleccionar

## 🎨 Características de UI

### Vista de Cards
- Foto circular con borde de color según rol
- Indicador de estado activo/inactivo
- Icono de usuario por defecto si no hay foto
- Información de contacto y licencia (para choferes)

### Vista de Tabla
- Columna con foto y nombre del usuario
- Fotos circulares con borde de color
- Diseño responsive con scroll horizontal

### Modal de Edición/Creación
- Sección de foto destacada en la parte superior
- Preview grande (128x128px) de la foto
- Botón flotante para cambiar/subir foto
- Indicaciones claras de formatos y tamaño

### Navbar
- Foto de perfil del usuario autenticado
- Visible en desktop y mobile
- Borde de color según rol (azul para CLIENTE, verde para COOPERATIVA)

## 🔒 Seguridad

1. **Autenticación**: Todos los endpoints requieren token Bearer
2. **Autorización**: Solo usuarios autenticados pueden subir/eliminar fotos
3. **Validación de archivos**: 
   - Tipo MIME verificado en backend
   - Tamaño limitado a 5MB
4. **Nombres únicos**: Timestamp incluido para evitar colisiones
5. **Limpieza**: Al subir nueva foto, se elimina la anterior automáticamente

## 🚀 Flujo de Trabajo

### Subir Foto

1. Usuario selecciona imagen desde input file
2. Frontend valida tipo y tamaño
3. Se muestra preview inmediato
4. Al guardar el formulario:
   - Se crea/actualiza el usuario
   - Se sube la foto mediante FormData
   - Backend guarda archivo y actualiza BD
5. Se recarga la lista de personal con la nueva foto

### Eliminar Foto

1. Usuario hace clic en eliminar foto (futuro endpoint)
2. Backend elimina archivo del sistema
3. BD actualiza campos a NULL
4. Frontend muestra icono por defecto

## 📊 Modelo de Datos

### Tabla: usuario_cooperativa

```sql
ALTER TABLE usuario_cooperativa
    ADD COLUMN foto_url VARCHAR(500),
    ADD COLUMN foto_filename VARCHAR(255);
```

- **foto_url**: URL pública para acceder a la foto
- **foto_filename**: Nombre del archivo en el servidor

## 🔄 Actualización de Contexto de Autenticación

Al hacer login, el sistema ahora incluye `fotoUrl` en:
- `AuthResponse` (respuesta de login)
- `MeResponse` (endpoint /users/me)
- User context del frontend

Esto permite que la foto aparezca en el navbar inmediatamente después del login.

## 📝 Notas de Implementación

1. **Cache**: Los archivos estáticos tienen cache de 1 hora
2. **Persistencia**: Las fotos se almacenan en sistema de archivos (considerar cloud storage para producción)
3. **Migración**: Usuarios existentes no tienen foto (NULL), se muestra icono por defecto
4. **Extensibilidad**: La misma estructura puede usarse para:
   - Fotos de clientes (`uploads/clientes/fotos/`)
   - Fotos de buses (`uploads/buses/fotos/`)

## 🐛 Manejo de Errores

### Backend
- Archivo no es imagen → 400 Bad Request
- Archivo muy grande → 400 Bad Request
- Usuario no encontrado → 404 Not Found
- Error al guardar archivo → 500 Internal Server Error

### Frontend
- Muestra mensaje de error amigable
- Usuario se crea/actualiza aunque falle la foto
- Fallback a icono por defecto si falta fotoUrl

## 🔮 Mejoras Futuras

1. **Cloud Storage**: Migrar a AWS S3, Azure Blob, o similar
2. **Optimización de Imágenes**: 
   - Redimensionar automáticamente
   - Generar thumbnails
   - Comprimir para web
3. **Edición de Fotos**: Recorte y ajustes en el frontend
4. **CDN**: Servir fotos desde CDN para mejor rendimiento
5. **Límite de Reintentos**: Evitar spam de uploads
6. **Auditoría**: Log de cambios de foto

## 📚 Dependencias

### Backend
- Spring Boot
- Spring MVC (MultipartFile)
- JPA/Hibernate
- PostgreSQL

### Frontend
- Next.js 16
- React 19
- TypeScript
- Lucide Icons
- TailwindCSS

## 🧪 Testing

### Backend (Manual)
```bash
# Subir foto
curl -X POST http://localhost:8080/api/cooperativa/1/personal/1/foto \
  -H "Authorization: Bearer {token}" \
  -F "foto=@/path/to/photo.jpg"

# Verificar foto
curl http://localhost:8080/uploads/personal/fotos/personal_1_xxx.jpg
```

### Frontend
1. Navegar a `/dashboard/Cooperativa/Admin/personal`
2. Crear/editar usuario
3. Seleccionar foto desde el input
4. Verificar preview inmediato
5. Guardar y verificar en lista (cards y tabla)
6. Verificar foto en navbar

## 📖 Documentación Relacionada

- [Documentación de Spring MultipartFile](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/multipart/MultipartFile.html)
- [Next.js File Upload](https://nextjs.org/docs/app/building-your-application/routing/route-handlers#formdata)
- [MDN FormData API](https://developer.mozilla.org/en-US/docs/Web/API/FormData)
