package kr.tinywind.springbootstreaming.model;

import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by tinywind on 2016-06-03.
 */
@Data
public class MaterialDataList<T> extends ArrayList<T> {
    private Material material;

    public MaterialDataList(Material material) {
        this.material = material;
    }

    public boolean add(T data) {
        try {
            Method setMaterial = data.getClass().getDeclaredMethod("setMaterial", Material.class);
            setMaterial.invoke(data, material);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return super.add(data);
    }
}
