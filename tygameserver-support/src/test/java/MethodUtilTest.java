import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Test;

import com.netease.pangu.game.util.MethodUtil;

import javassist.NotFoundException;

public class MethodUtilTest {

	@Test
	public void test() throws NotFoundException {
		Method[] m = TestClaxx.class.getMethods();
		Map<Integer, String> params = MethodUtil.getParameterIndexMap(m[0]);
		System.out.println(params);
	}

}
