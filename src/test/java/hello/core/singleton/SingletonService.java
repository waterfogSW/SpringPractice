package hello.core.singleton;

public class SingletonService {
//    // 1. static 영역에 객체를 딱 1개만 생성해 둔다.
//    private static final SingletonService instance = new SingletonService();
//
//    // 2. public 으로 열어서 객체 인스턴스가 필요하면, static메서드를 통해서만 조회하도록 허용한다.
//    public static SingletonService getInstance() {
//        return instance;
//    }
//
//    // 3. 생성자를 private 으로 선언하여 외부에서 생성하지 못하게 한다.
//    // -> 좋은 설계는 컴파일 오류만으로 오류를 모두 잡을수 있도록 해야 한다.
//    private SingletonService() {}
//
//    public void logic() {
//        System.out.println("싱글톤 객체 로직 호출");
//    }

    private SingletonService() {};

    private static class LazyHolder {
        static final SingletonService SINGLETON_SERVICE = new SingletonService();
    }

    public static SingletonService getInstance() {
        return LazyHolder.SINGLETON_SERVICE;
    }
}
