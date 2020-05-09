package me.coley.analysis.validation.fail;

import me.coley.analysis.TestUtils;
import me.coley.analysis.util.FrameUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import static org.junit.jupiter.api.Assertions.*;

public class TestCustomIllegalClasses extends TestUtils {
	@ParameterizedTest
	@ValueSource(strings =  {
			"bin/custom/illegal/vars/LongToInt.class",
			"bin/custom/illegal/vars/StringToInt.class"
	})
	public void testClasses(String classPath) {
		ClassNode node = getFromName(classPath);
		for (MethodNode mn : node.methods)
			assertThrows(AnalyzerException.class, () -> FrameUtil.getFrames(node.name, mn));
	}
}
