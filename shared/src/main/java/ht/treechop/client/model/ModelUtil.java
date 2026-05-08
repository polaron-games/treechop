package ht.treechop.client.model;

import ht.tuber.math.Vector3;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ModelUtil {

    public static BakedQuad trimQuad(BakedQuad quad, Vector3 corner1, Vector3 corner2) {
        // corner1/corner2 are in 0-16 block-pixel range; positions are in 0-1 range
        float minX = (float) Math.min(corner1.x(), corner2.x()) / 16f;
        float minY = (float) Math.min(corner1.y(), corner2.y()) / 16f;
        float minZ = (float) Math.min(corner1.z(), corner2.z()) / 16f;
        float maxX = (float) Math.max(corner1.x(), corner2.x()) / 16f;
        float maxY = (float) Math.max(corner1.y(), corner2.y()) / 16f;
        float maxZ = (float) Math.max(corner1.z(), corner2.z()) / 16f;

        Vector3fc p0 = clamp(quad.position0(), minX, minY, minZ, maxX, maxY, maxZ);
        Vector3fc p1 = clamp(quad.position1(), minX, minY, minZ, maxX, maxY, maxZ);
        Vector3fc p2 = clamp(quad.position2(), minX, minY, minZ, maxX, maxY, maxZ);
        Vector3fc p3 = clamp(quad.position3(), minX, minY, minZ, maxX, maxY, maxZ);

        return new BakedQuad(p0, p1, p2, p3,
                quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                quad.tintIndex(), quad.direction(), quad.sprite(), quad.shade(), quad.lightEmission());
    }

    public static BakedQuad translateQuad(BakedQuad quad, Vector3 translation) {
        // translation is in 0-16 range; positions are in 0-1 range
        float tx = (float) translation.x() / 16f;
        float ty = (float) translation.y() / 16f;
        float tz = (float) translation.z() / 16f;

        return new BakedQuad(
                add(quad.position0(), tx, ty, tz),
                add(quad.position1(), tx, ty, tz),
                add(quad.position2(), tx, ty, tz),
                add(quad.position3(), tx, ty, tz),
                quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                quad.tintIndex(), quad.direction(), quad.sprite(), quad.shade(), quad.lightEmission());
    }

    private static Vector3fc clamp(Vector3fc pos, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return new Vector3f(
                Math.max(minX, Math.min(maxX, pos.x())),
                Math.max(minY, Math.min(maxY, pos.y())),
                Math.max(minZ, Math.min(maxZ, pos.z()))
        );
    }

    private static Vector3fc add(Vector3fc pos, float tx, float ty, float tz) {
        return new Vector3f(pos.x() + tx, pos.y() + ty, pos.z() + tz);
    }
}
