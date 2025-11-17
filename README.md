#Refactorer

## Descripción

Este proyecto es el Trabajo Fin de Grado (TFG) de Miguel, y consiste en el desarrollo de un plugin para Eclipse orientado a la mejora de la calidad del código mediante la reducción de la complejidad cognitiva en métodos y clases. El plugin automatiza y facilita el proceso de refactorización, ayudando a los desarrolladores a mantener un código más limpio, comprensible y mantenible.

## Objetivos

- **Reducir la complejidad cognitiva** de los métodos y clases de un proyecto Java.
- **Evitar la repetición de lógica** y reducir la longitud de los métodos mediante extracciones automáticas de código.
- **Sugerir refactorizaciones** cuando se detecten funciones que superan un umbral de complejidad cognitiva.
- **Ofrecer una visualización interactiva** para que el usuario pueda revisar y aprobar las sugerencias de refactorización de manera cómoda e intuitiva.
- **Ejecutar automáticamente las extracciones de código** aprobadas, asegurando la integridad funcional y semántica del proyecto.
- **Mostrar métricas e información** relevante sobre las acciones realizadas durante el proceso de refactorización.

## Funcionalidades principales

1. **Análisis de complejidad cognitiva**
    - Escanea el proyecto en busca de métodos y clases cuya complejidad cognitiva supere un umbral configurable.
    - Presenta una lista de candidatos a refactorización.

2. **Sugerencias de refactorización**
    - Propone extracciones de código para reducir la complejidad.
    - Permite al usuario revisar y modificar las sugerencias antes de aplicarlas.

3. **Visualización interactiva**
    - Interfaz gráfica integrada en Eclipse para facilitar la revisión y aprobación de los cambios.
    - Visualización de los fragmentos de código afectados y de las nuevas funciones generadas.

4. **Refactorización automática**
    - Aplica las extracciones de código aprobadas por el usuario.
    - Garantiza que el comportamiento del programa no se vea alterado.

5. **Métricas y reportes**
    - Muestra métricas antes y después de la refactorización (complejidad cognitiva, longitud de métodos, duplicidad de lógica, etc.).
    - Registro de las acciones realizadas para su posterior análisis.

## Requisitos

- **Eclipse IDE** (versión 2023-12 o superior)
- **Java 21** o superior
- **IBM ILOG CPLEX** (Community Edition o superior) - Necesario para los algoritmos de optimización ILP

## Instalación

1. Clona este repositorio:
    ```bash
    git clone https://github.com/tu-usuario/refactorer.git
    ```
2. Importa el proyecto en Eclipse como un plugin.
3. **Configura CPLEX** siguiendo las instrucciones en `CPLEX_SETUP.md`:
   - **macOS**: Ejecuta `./setup_cplex_macos.sh` para configuración automática
   - **Manual**: Configura la variable de entorno `CPLEX_LIBRARY_PATH`
4. Sigue las instrucciones del archivo `INSTALL.md` para completar la instalación.

## Uso

1. Abre tu proyecto Java en Eclipse.
2. Accede al menú del plugin Refactorer.
3. Ejecuta el análisis de complejidad cognitiva.
4. Revisa las sugerencias y aprueba las que consideres oportunas.
5. Aplica los cambios y consulta las métricas generadas.

## Tecnologías utilizadas

- Java
- Eclipse Plugin Development Environment (PDE)
- Herramientas de análisis estático de código

## Autor

Miguel Valadez Cano (MVCx9)
Trabajo Fin de Grado (TFG)  
ETSI Informática de Málaga (UMA)

## Licencia

Este proyecto está licenciado bajo la licencia MIT. Esto significa que puedes usar, copiar, modificar y distribuir el software libremente, siempre que incluyas una copia de este aviso de licencia y renuncies a cualquier responsabilidad del autor. El software se proporciona "tal cual", sin garantías de ningún tipo.
