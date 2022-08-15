package me.coley.analysis;

import org.objectweb.asm.Type;

import java.util.function.BiPredicate;

/**
 * Binary predicate that takes two types, a supposed parent and a supposed child.
 * Determines if the parent is actually the parent.
 *
 * @author Matt Coley
 */
public interface TypeChecker extends BiPredicate<Type, Type> {}
