package test.mother;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListMother {

    public List<String> emptyStringList() {
        return new ArrayList<>();
    }

    public List<String> singleStringList() {
        return new ArrayList<>(Arrays.asList("a"));
    }

    public List<String> stringListABC() {
        return new ArrayList<>(Arrays.asList("a", "b", "c"));
    }
}
