# Configuración de CPLEX para el Plugin Refactorer

Este plugin utiliza IBM ILOG CPLEX para algoritmos de optimización ILP. Para que funcione correctamente, necesitas configurar la ubicación de las librerías nativas de CPLEX.

## Opción 1: Variable de Entorno (Recomendado)

### macOS / Linux

Añade la siguiente línea a tu archivo `~/.zshrc` (macOS) o `~/.bashrc` (Linux):

```bash
export CPLEX_LIBRARY_PATH="/ruta/a/tu/CPLEX_Studio/cplex/bin/x86-64_osx"
```

**Ejemplo para macOS:**
```bash
export CPLEX_LIBRARY_PATH="/Applications/CPLEX_Studio_Community2212/cplex/bin/x86-64_osx"
# O si está en tu carpeta de usuario:
export CPLEX_LIBRARY_PATH="$HOME/Applications/CPLEX_Studio_Community2212/cplex/bin/x86-64_osx"
```

**Ejemplo para Linux:**
```bash
export CPLEX_LIBRARY_PATH="/opt/ibm/ILOG/CPLEX_Studio2212/cplex/bin/x86-64_linux"
```

Luego recarga tu configuración:
```bash
source ~/.zshrc  # macOS
source ~/.bashrc # Linux
```

### Windows

1. Abre "Variables de entorno del sistema"
2. Añade una nueva variable de usuario o sistema:
   - Nombre: `CPLEX_LIBRARY_PATH`
   - Valor: `C:\Program Files\IBM\ILOG\CPLEX_Studio2212\cplex\bin\x64_win64`

## Opción 2: Propiedad del Sistema Java

Añade este argumento al lanzar Eclipse:

```bash
-Dcplex.library.path=/ruta/a/tu/CPLEX_Studio/cplex/bin/x86-64_osx
```

## Opción 3: Ubicaciones por Defecto

El plugin buscará automáticamente CPLEX en estas ubicaciones:

**macOS:**
- `/Applications/CPLEX_Studio_Community2212/cplex/bin/x86-64_osx`
- `~/Applications/CPLEX_Studio_Community2212/cplex/bin/x86-64_osx`
- `/opt/ibm/ILOG/CPLEX_Studio2212/cplex/bin/x86-64_osx`

**Linux:**
- `/opt/ibm/ILOG/CPLEX_Studio2212/cplex/bin/x86-64_linux`

**Windows:**
- `C:\Program Files\IBM\ILOG\CPLEX_Studio2212\cplex\bin\x64_win64`

Si CPLEX está instalado en alguna de estas ubicaciones, funcionará automáticamente sin configuración adicional.

## Verificación

Al iniciar Eclipse con el plugin, revisa la consola para ver mensajes como:

```
### Plugin Refactorer cargado
>> CPLEX library path configurado: /ruta/a/cplex/bin/...
>> Plugin Refactorer ACTIVADO correctamente
```

Si ves una advertencia, configura la variable de entorno según las instrucciones anteriores.

## Instalación de CPLEX

Si no tienes CPLEX instalado, puedes descargarlo desde:
https://www.ibm.com/academic/technology/data-science

La versión Community Edition es gratuita para uso académico.
