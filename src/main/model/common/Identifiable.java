package main.model.common;

/**
 * Indica que cada elemento debe tener un nombre que lo identifique
 */
public interface Identifiable {

	/** Nombre lógico del elemento (sin ruta, ni extensión, ni carpetas). */
	String getName();

	/**
	 * Devuelve el nombre en mayúsculas
	 */
	default String getNameUpper() {
		return getName() == null ? "" : getName().toUpperCase();
	}

}
