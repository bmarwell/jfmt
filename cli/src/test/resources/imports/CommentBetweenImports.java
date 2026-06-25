import com.example.App;
// this comment sits between imports and is dropped on reorder
import static java.lang.Math.PI;
import java.util.List;

public class CommentBetweenImports {
    public double area(List<Integer> ignored) {
        App app = new App();
        return PI;
    }
}
