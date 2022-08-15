package me.coley.analysis.exception;

/**
 * Situation where type mismatch occurred.
 *
 * @author Matt Coley
 */
public enum TypeMismatchKind {
	GETFIELD,
	PUTSTATIC,
	INVOKE_HOST_NULL,
	INVOKE_HOST_TYPE,
	INVOKE_ARG_TYPE,
	RETURN
}