package me.coley.analysis;

import me.coley.analysis.value.AbstractValue;

import java.util.List;

/**
 * Factory for generating {@link AbstractValue} from static method calls.
 *
 * @author Matt
 */
public class StaticInvokeFactory {
	/**
	 * @param owner
	 * 		Method owner.
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method descriptor.
	 * @param arguments
	 * 		Arguments on the stack.
	 *
	 * @return Value of invoke. {@code null} for void types.
	 */
	public AbstractValue invokeStatic(String owner, String name, String desc, List<?
			extends AbstractValue> arguments) {
		return null;
	}
}
