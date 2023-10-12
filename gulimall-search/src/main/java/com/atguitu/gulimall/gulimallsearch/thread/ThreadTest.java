package com.atguitu.gulimall.gulimallsearch.thread;

import java.util.concurrent.*;

public class ThreadTest {

    /**
     *
     *
     *
     *
     */
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("main start");

//        CompletableFuture runFuture = CompletableFuture.runAsync(()->{
//            runAs();
//        },executor);
//
//        Integer i = (Integer) runFuture.get();
//
//        System.out.println("runas result " + i);


//        CompletableFuture<Integer> runFuture2= CompletableFuture.supplyAsync(()->{
//             return supAs();
//        },executor).whenComplete((res, thr)->{
//            System.out.println("完成回调-> res" + res + "  thr-> " + thr ) ;
//
//        });



//        Integer s =  runFuture2.get();

//        whenComplate();

       // exceptionally();

//        handle();

//        thenRun();
//        thenAccept();
//        thenApply();

//        runAfterBoth();
        thenCombineBoth();
        System.out.println("maain end");

    }


    private static  CompletableFuture<String>  thenCombineBoth () throws ExecutionException, InterruptedException {
        CompletableFuture<String> runFuture= CompletableFuture.supplyAsync(()->{
            System.out.println("run second both");
            return "run second ";
        },executor);

        // normal
        CompletableFuture<String> runFuture2= CompletableFuture.supplyAsync(()->{
            return supAs();
        },executor).thenCombine(runFuture, (first, second)->{
            System.out.println(first + " -> "  + second);

            return "return " + first + " -> " + second;
        });

        System.out.println( runFuture2.get());

        return runFuture2;

    }
    private static  CompletableFuture<Void>  thenAcceptBoth ()
    {
        CompletableFuture<String> runFuture= CompletableFuture.supplyAsync(()->{
            System.out.println("run second both");
            return "run second ";
        },executor);

        // normal
        CompletableFuture<Void> runFuture2= CompletableFuture.supplyAsync(()->{
            return supAs();
        },executor).thenAcceptBoth(runFuture, (first, second)->{
            System.out.println(first + " -> "  + second);
        });

        return runFuture2;

    }

    private static  CompletableFuture<Void>  runAfterBoth ()
    {

        CompletableFuture<String> runFuture= CompletableFuture.supplyAsync(()->{
            System.out.println("run second both");
           return "run second ";
        },executor);

        // normal
        CompletableFuture<Void> runFuture2= CompletableFuture.supplyAsync(()->{
            return supAs();
        },executor).runAfterBoth(runFuture, ()->{
            System.out.println("run after both");

        } );
        return runFuture2;
    }

    private static  CompletableFuture<Void>  thenRun()
    {
        // normal
        CompletableFuture<Void> runFuture2= CompletableFuture.supplyAsync(()->{
            return supAs();
        },executor).thenRun( ()->{
            System.out.println("then run haha");
        } );

        return runFuture2;
    }

    private static  CompletableFuture<Void>  thenAccept()
    {
        // normal
        CompletableFuture<Void> runFuture2= CompletableFuture.supplyAsync(()->{
            return supAs();
        },executor).thenAccept( res->{
            System.out.println("then accept -> " + res + "haha");

        } );

        return runFuture2;
    }


    private static  CompletableFuture<Integer>  thenApply()
    {
        // normal
        CompletableFuture<Integer> runFuture2= CompletableFuture.supplyAsync(()->{
            return supAs();
        },executor).thenApply( res->{
            System.out.println("then apply -> " + res + "haha");
            return res;
        } );

        return runFuture2;
    }




    // handle 最终处理
    private static CompletableFuture<Integer> handle(){
        // normal
        CompletableFuture<Integer> runFuture2= CompletableFuture.supplyAsync(()->{
            return supAs();
        },executor).handle( (res, thr)->{

            System.out.println("handle block -> " + res + "  exp ->  " + thr);
            return res;
        } );

        CompletableFuture<Integer> runFuture3= CompletableFuture.supplyAsync(()->{
            System.out.println("exception supAs start");

            int i = 10/0;

            System.out.println("exception supAs end"+i);

            return i;
        },executor).handle( (res, thr)->{

            System.out.println("handle exp block -> " + res + "  exp ->  " + thr);
            return 0;
        } );


        return runFuture2;
    }


    private static CompletableFuture<Integer> exceptionally(){

        // normal
        CompletableFuture<Integer> runFuture2= CompletableFuture.supplyAsync(()->{
            return supAs();
        },executor).whenComplete((res, thr)->{
            System.out.println("完成回调-> res" + res + "  thr-> " + thr ) ;

        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return 10;
        });

        //exception
        CompletableFuture<Integer> runFuture3= CompletableFuture.supplyAsync(()->{
            System.out.println("exception supAs start");

            int i = 10/0;

            System.out.println("exception supAs end"+i);

            return i;
        },executor).whenComplete((res, thr)->{
            System.out.println("完成回调2-> res" + res + "  thr-> " + thr ) ;

        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return 10;
        });

        return runFuture3;
    }


    private static CompletableFuture<Integer> whenComplate()
    {
        // normal
        CompletableFuture<Integer> runFuture2= CompletableFuture.supplyAsync(()->{
            return supAs();
        },executor).whenComplete((res, thr)->{
            System.out.println("完成回调-> res" + res + "  thr-> " + thr ) ;

        });

        //exception
        CompletableFuture<Integer> runFuture3= CompletableFuture.supplyAsync(()->{
            System.out.println("exception supAs start");

            int i = 10/0;

            System.out.println("exception supAs end"+i);

            return i;
        },executor).whenComplete((res, thr)->{
            System.out.println("完成回调-> res" + res + "  thr-> " + thr ) ;

        });

        return runFuture2;
    }

    private static int runAs()
    {
        System.out.println("runas start");

        int i = 10/3;

        System.out.println("runas end"+i);

        return i;

    }


    private static int supAs()
    {
        System.out.println("supAs start");

        int i = 10/3;

        System.out.println("supAs end"+i);

        return i;


    }
}

