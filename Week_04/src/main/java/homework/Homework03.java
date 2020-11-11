package homework;

import java.util.concurrent.*;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 一个简单的代码参考：
 */
public class Homework03 {
    public static void main(String[] args) {
        // 方法一
        method1();
        // 方法二
        method2();
    }
    // 方法一
    private static void method1 () {
        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Object> future = executorService.submit(new Callable<Object>() {
            public Object call() throws Exception {
                return sum();
            }
        });
        executorService.shutdown();
        int result = 0;
        try {
            result = (Integer) future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    private static void method2 () {
        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        //方法二
        FutureTask<Integer> task = new FutureTask<Integer>(new Callable<Integer>() {
            public Integer call() throws Exception {
                return sum();
            }
        });
        new Thread(task).start();
        int result = 0;
        try {
            result = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
