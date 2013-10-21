package foodev.jsondiff;

import java.util.Arrays;
import java.util.Collection;

import org.codehaus.jackson.node.NullNode;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.gson.JsonObject;

@RunWith(value = Parameterized.class)
public class JsonDiffTest extends JsonDiffTestMethods {

    public JsonDiffTest(Object hint) {

        JsonDiff.setHint(hint);

    }

    @Parameters
    public static Collection<Object[]> hints() {

        Object[][] data = new Object[][] { { new JsonObject() }, { NullNode.getInstance() } };
        return Arrays.asList(data);

    }
}
