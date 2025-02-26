package com.santa.secret;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

@Slf4j
@ExtensionMethod({ListUtils.class})
public class AbcTest {
    @Test
    public void test() {
        List<A> aList = Arrays.asList(
                new A("a1", Arrays.asList(new B("b1", Arrays.asList(new C("c1")))))
        );

        log.info("{}",  aList.safeGet(0).map(A::getBList).safeGet(0).map(B::getCList).safeGet(0).orElse(null));
        log.info("{}",  aList.safeGet(0).map(A::getBList).safeGet(1).map(B::getCList).safeGet(0).orElse(null));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class C {
        private String c;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class B {
        private String b;
        private List<C> cList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class A {
        private String a;
        private List<B> bList;
    }
}
