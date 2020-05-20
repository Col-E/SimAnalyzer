package me.coley.analysis.design;

import me.coley.analysis.SimAnalyzer;
import me.coley.analysis.SimInterpreter;
import me.coley.analysis.TestUtils;
import me.coley.analysis.TypeChecker;
import me.coley.analysis.cfg.BlockHandler;
import me.coley.analysis.exception.ResolvableAnalyzerException;
import me.coley.analysis.exception.ResolvableExceptionFactory;
import me.coley.analysis.exception.TypeMismatchKind;
import me.coley.analysis.exception.Validator;
import me.coley.analysis.value.AbstractValue;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestExceptionFactory extends TestUtils {
	@Test
	public void testThrowsByDefault() throws AnalyzerException {
		ClassNode node = getFromName("bin/custom/misc/WrongCallArgType.class");
		for(MethodNode mn : node.methods) {
			SimAnalyzer analyzer = new SimAnalyzer(new SimInterpreter());
			assertThrows(AnalyzerException.class, () -> analyzer.analyze(node.name, mn));
		}
	}

	@Test
	public void testCustomFactorySupppressesException() throws AnalyzerException {
		ClassNode node = getFromName("bin/custom/misc/WrongCallArgType.class");
		for(MethodNode mn : node.methods) {
			SimAnalyzer analyzer = new SimAnalyzer(new SimInterpreter()) {
				@Override
				protected ResolvableExceptionFactory createExceptionFactory() {
					return new ResolvableExceptionFactoryTestImpl(createTypeChecker(), getBlockHandler());
				}
			};
			assertDoesNotThrow(() -> analyzer.analyze(node.name, mn));
		}
	}

	static class ResolvableExceptionFactoryTestImpl  extends ResolvableExceptionFactory {
		public ResolvableExceptionFactoryTestImpl(TypeChecker typeChecker, BlockHandler blockHandler) {
			super(typeChecker, blockHandler);
		}

		@Override
		public AnalyzerException unexpectedMethodArgType(Type expectedType,
														 Type actualType,
														 AbstractInsnNode insn,
														 AbstractValue actualValue,
														 List<? extends AbstractValue> stackValues,
														 int argIndex,
														 TypeMismatchKind errorType) {
			return new ResolvableAnalyzerException((methodNode, frames) -> true, insn, "");
		}
	}
}
