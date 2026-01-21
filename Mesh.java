
import java.io.Serializable;

public record Mesh(Vec3[] points, Triangle[] triangles) implements Serializable {}